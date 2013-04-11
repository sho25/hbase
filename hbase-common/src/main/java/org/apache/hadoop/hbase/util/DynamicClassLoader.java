begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_comment
comment|/**  * This is a class loader that can load classes dynamically from new  * jar files under a configured folder. It always uses its parent class  * loader to load a class at first. Only if its parent class loader  * can not load a class, we will try to load it using the logic here.  *<p>  * We can't unload a class already loaded. So we will use the existing  * jar files we already know to load any class which can't be loaded  * using the parent class loader. If we still can't load the class from  * the existing jar files, we will check if any new jar file is added,  * if so, we will load the new jar file and try to load the class again.  * If still failed, a class not found exception will be thrown.  *<p>  * Be careful in uploading new jar files and make sure all classes  * are consistent, otherwise, we may not be able to load your  * classes properly.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DynamicClassLoader
extends|extends
name|URLClassLoader
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DynamicClassLoader
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Dynamic jars are put under ${hbase.local.dir}/dynamic/jars/
specifier|private
specifier|static
specifier|final
name|String
name|DYNAMIC_JARS_DIR
init|=
name|File
operator|.
name|separator
operator|+
literal|"dynamic"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"jars"
operator|+
name|File
operator|.
name|separator
decl_stmt|;
comment|/**    * Parent class loader used to load any class at first.    */
specifier|private
specifier|final
name|ClassLoader
name|parent
decl_stmt|;
specifier|private
name|File
name|localDir
decl_stmt|;
comment|// FileSystem of the remote path, set only if remoteDir != null
specifier|private
name|FileSystem
name|remoteDirFs
decl_stmt|;
specifier|private
name|Path
name|remoteDir
decl_stmt|;
comment|// Last modified time of local jars
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|jarModifiedTime
decl_stmt|;
comment|/**    * Creates a DynamicClassLoader that can load classes dynamically    * from jar files under a specific folder.    *    * @param conf the configuration for the cluster.    * @param parent the parent ClassLoader to set.    */
specifier|public
name|DynamicClassLoader
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|ClassLoader
name|parent
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|URL
index|[]
block|{}
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|jarModifiedTime
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|String
name|localDirPath
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.local.dir"
argument_list|)
operator|+
name|DYNAMIC_JARS_DIR
decl_stmt|;
name|localDir
operator|=
operator|new
name|File
argument_list|(
name|localDirPath
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|localDir
operator|.
name|mkdirs
argument_list|()
operator|&&
operator|!
name|localDir
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create local dir "
operator|+
name|localDir
operator|.
name|getPath
argument_list|()
operator|+
literal|", DynamicClassLoader failed to init"
argument_list|)
throw|;
block|}
name|String
name|remotePath
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.dynamic.jars.dir"
argument_list|)
decl_stmt|;
if|if
condition|(
name|remotePath
operator|==
literal|null
operator|||
name|remotePath
operator|.
name|equals
argument_list|(
name|localDirPath
argument_list|)
condition|)
block|{
name|remoteDir
operator|=
literal|null
expr_stmt|;
comment|// ignore if it is the same as the local path
block|}
else|else
block|{
name|remoteDir
operator|=
operator|new
name|Path
argument_list|(
name|remotePath
argument_list|)
expr_stmt|;
try|try
block|{
name|remoteDirFs
operator|=
name|remoteDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to identify the fs of dir "
operator|+
name|remoteDir
operator|+
literal|", ignored"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|remoteDir
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
try|try
block|{
return|return
name|parent
operator|.
name|loadClass
argument_list|(
name|name
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Class "
operator|+
name|name
operator|+
literal|" not found - using dynamical class loader"
argument_list|)
expr_stmt|;
block|}
comment|// Check whether the class has already been loaded:
name|Class
argument_list|<
name|?
argument_list|>
name|clasz
init|=
name|findLoadedClass
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|clasz
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Class "
operator|+
name|name
operator|+
literal|" already loaded"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finding class: "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
name|clasz
operator|=
name|findClass
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
comment|// Load new jar files if any
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loading new jar files, if any"
argument_list|)
expr_stmt|;
block|}
name|loadNewJars
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finding class again: "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
name|clasz
operator|=
name|findClass
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|clasz
return|;
block|}
block|}
specifier|private
specifier|synchronized
name|void
name|loadNewJars
parameter_list|()
block|{
comment|// Refresh local jar file lists
for|for
control|(
name|File
name|file
range|:
name|localDir
operator|.
name|listFiles
argument_list|()
control|)
block|{
name|String
name|fileName
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|jarModifiedTime
operator|.
name|containsKey
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|file
operator|.
name|isFile
argument_list|()
operator|&&
name|fileName
operator|.
name|endsWith
argument_list|(
literal|".jar"
argument_list|)
condition|)
block|{
name|jarModifiedTime
operator|.
name|put
argument_list|(
name|fileName
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|file
operator|.
name|lastModified
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|URL
name|url
init|=
name|file
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
decl_stmt|;
name|addURL
argument_list|(
name|url
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|mue
parameter_list|)
block|{
comment|// This should not happen, just log it
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to load new jar "
operator|+
name|fileName
argument_list|,
name|mue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Check remote files
name|FileStatus
index|[]
name|statuses
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|remoteDir
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|statuses
operator|=
name|remoteDirFs
operator|.
name|listStatus
argument_list|(
name|remoteDir
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to check remote dir status "
operator|+
name|remoteDir
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|statuses
operator|==
literal|null
operator|||
name|statuses
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
comment|// no remote files at all
block|}
for|for
control|(
name|FileStatus
name|status
range|:
name|statuses
control|)
block|{
if|if
condition|(
name|status
operator|.
name|isDir
argument_list|()
condition|)
continue|continue;
comment|// No recursive lookup
name|Path
name|path
init|=
name|status
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|fileName
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fileName
operator|.
name|endsWith
argument_list|(
literal|".jar"
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignored non-jar file "
operator|+
name|fileName
argument_list|)
expr_stmt|;
block|}
continue|continue;
comment|// Ignore non-jar files
block|}
name|Long
name|cachedLastModificationTime
init|=
name|jarModifiedTime
operator|.
name|get
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|cachedLastModificationTime
operator|!=
literal|null
condition|)
block|{
name|long
name|lastModified
init|=
name|status
operator|.
name|getModificationTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastModified
operator|<
name|cachedLastModificationTime
operator|.
name|longValue
argument_list|()
condition|)
block|{
comment|// There could be some race, for example, someone uploads
comment|// a new one right in the middle the old one is copied to
comment|// local. We can check the size as well. But it is still
comment|// not guaranteed. This should be rare. Most likely,
comment|// we already have the latest one.
comment|// If you are unlucky to hit this race issue, you have
comment|// to touch the remote jar to update its last modified time
continue|continue;
block|}
block|}
try|try
block|{
comment|// Copy it to local
name|File
name|dst
init|=
operator|new
name|File
argument_list|(
name|localDir
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|remoteDirFs
operator|.
name|copyToLocalFile
argument_list|(
name|path
argument_list|,
operator|new
name|Path
argument_list|(
name|dst
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|jarModifiedTime
operator|.
name|put
argument_list|(
name|fileName
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|dst
operator|.
name|lastModified
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|URL
name|url
init|=
name|dst
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
decl_stmt|;
name|addURL
argument_list|(
name|url
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to load new jar "
operator|+
name|fileName
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

