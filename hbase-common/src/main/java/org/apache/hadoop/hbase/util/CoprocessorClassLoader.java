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
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|Lock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarEntry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarFile
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|FileUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|io
operator|.
name|IOUtils
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
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|MapMaker
import|;
end_import

begin_comment
comment|/**  * ClassLoader used to load classes for Coprocessor instances.  *<p>  * This ClassLoader always tries to load classes from the specified coprocessor  * jar first actually using URLClassLoader logic before delegating to the parent  * ClassLoader, thus avoiding dependency conflicts between HBase's classpath and  * classes in the coprocessor jar.  *<p>  * Certain classes are exempt from being loaded by this ClassLoader because it  * would prevent them from being cast to the equivalent classes in the region  * server.  For example, the Coprocessor interface needs to be loaded by the  * region server's ClassLoader to prevent a ClassCastException when casting the  * coprocessor implementation.  *<p>  * A HDFS path can be used to specify the coprocessor jar. In this case, the jar  * will be copied to local at first under some folder under ${hbase.local.dir}/jars/tmp/.  * The local copy will be removed automatically when the HBase server instance is  * stopped.  *<p>  * This ClassLoader also handles resource loading.  In most cases this  * ClassLoader will attempt to load resources from the coprocessor jar first  * before delegating to the parent.  However, like in class loading,  * some resources need to be handled differently.  For all of the Hadoop  * default configurations (e.g. hbase-default.xml) we will check the parent  * ClassLoader first to prevent issues such as failing the HBase default  * configuration version check.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CoprocessorClassLoader
extends|extends
name|ClassLoaderBase
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
name|CoprocessorClassLoader
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// A temporary place ${hbase.local.dir}/jars/tmp/ to store the local
comment|// copy of the jar file and the libraries contained in the jar.
specifier|private
specifier|static
specifier|final
name|String
name|TMP_JARS_DIR
init|=
name|File
operator|.
name|separator
operator|+
literal|"jars"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
operator|+
name|File
operator|.
name|separator
decl_stmt|;
comment|/**    * External class loaders cache keyed by external jar path.    * ClassLoader instance is stored as a weak-reference    * to allow GC'ing when it is not used    * (@see HBASE-7205)    */
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|Path
argument_list|,
name|CoprocessorClassLoader
argument_list|>
name|classLoadersCache
init|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|concurrencyLevel
argument_list|(
literal|3
argument_list|)
operator|.
name|weakValues
argument_list|()
operator|.
name|makeMap
argument_list|()
decl_stmt|;
comment|/**    * If the class being loaded starts with any of these strings, we will skip    * trying to load it from the coprocessor jar and instead delegate    * directly to the parent ClassLoader.    */
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|CLASS_PREFIX_EXEMPTIONS
init|=
operator|new
name|String
index|[]
block|{
comment|// Java standard library:
literal|"com.sun."
block|,
literal|"java."
block|,
literal|"javax."
block|,
literal|"org.ietf"
block|,
literal|"org.omg"
block|,
literal|"org.w3c"
block|,
literal|"org.xml"
block|,
literal|"sunw."
block|,
comment|// logging
literal|"org.apache.commons.logging"
block|,
literal|"org.apache.log4j"
block|,
literal|"com.hadoop"
block|,
comment|// HBase:
literal|"org.apache.hadoop.hbase"
block|,   }
decl_stmt|;
comment|/**    * If the resource being loaded matches any of these patterns, we will first    * attempt to load the resource with the parent ClassLoader.  Only if the    * resource is not found by the parent do we attempt to load it from the coprocessor jar.    */
specifier|private
specifier|static
specifier|final
name|Pattern
index|[]
name|RESOURCE_LOAD_PARENT_FIRST_PATTERNS
init|=
operator|new
name|Pattern
index|[]
block|{
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^[^-]+-default\\.xml$"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|libJarPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[/]?lib/([^/]+\\.jar)"
argument_list|)
decl_stmt|;
comment|/**    * A locker used to synchronize class loader initialization per coprocessor jar file    */
specifier|private
specifier|static
specifier|final
name|KeyLocker
argument_list|<
name|String
argument_list|>
name|locker
init|=
operator|new
name|KeyLocker
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * A set used to synchronized parent path clean up.  Generally, there    * should be only one parent path, but using a set so that we can support more.    */
specifier|static
specifier|final
name|HashSet
argument_list|<
name|String
argument_list|>
name|parentDirLockSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Creates a JarClassLoader that loads classes from the given paths.    */
specifier|private
name|CoprocessorClassLoader
parameter_list|(
name|ClassLoader
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
name|Path
name|pathPattern
parameter_list|,
name|String
name|pathPrefix
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Copy the jar to the local filesystem
name|String
name|parentDirStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|LOCAL_DIR_KEY
argument_list|,
name|DEFAULT_LOCAL_DIR
argument_list|)
operator|+
name|TMP_JARS_DIR
decl_stmt|;
synchronized|synchronized
init|(
name|parentDirLockSet
init|)
block|{
if|if
condition|(
operator|!
name|parentDirLockSet
operator|.
name|contains
argument_list|(
name|parentDirStr
argument_list|)
condition|)
block|{
name|Path
name|parentDir
init|=
operator|new
name|Path
argument_list|(
name|parentDirStr
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|parentDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// it's ok if the dir doesn't exist now
name|parentDirLockSet
operator|.
name|add
argument_list|(
name|parentDirStr
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|parentDir
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|getFileStatus
argument_list|(
name|parentDir
argument_list|)
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
name|parentDirStr
operator|+
literal|", CoprocessorClassLoader failed to init"
argument_list|)
throw|;
block|}
block|}
block|}
name|FileSystem
name|fs
init|=
name|pathPattern
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|pathPattern1
init|=
name|fs
operator|.
name|isDirectory
argument_list|(
name|pathPattern
argument_list|)
condition|?
operator|new
name|Path
argument_list|(
name|pathPattern
argument_list|,
literal|"*.jar"
argument_list|)
else|:
name|pathPattern
decl_stmt|;
comment|// append "*.jar" if a directory is specified
name|FileStatus
index|[]
name|fileStatuses
init|=
name|fs
operator|.
name|globStatus
argument_list|(
name|pathPattern1
argument_list|)
decl_stmt|;
comment|// return all files that match the pattern
if|if
condition|(
name|fileStatuses
operator|==
literal|null
operator|||
name|fileStatuses
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// if no one matches
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|pathPattern1
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|boolean
name|validFileEncountered
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|FileUtil
operator|.
name|stat2Paths
argument_list|(
name|fileStatuses
argument_list|)
control|)
block|{
comment|// for each file that match the pattern
if|if
condition|(
name|fs
operator|.
name|isFile
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// only process files, skip for directories
name|File
name|dst
init|=
operator|new
name|File
argument_list|(
name|parentDirStr
argument_list|,
literal|"."
operator|+
name|pathPrefix
operator|+
literal|"."
operator|+
name|path
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|".jar"
argument_list|)
decl_stmt|;
name|fs
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
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|dst
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|addURL
argument_list|(
name|dst
operator|.
name|getCanonicalFile
argument_list|()
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
expr_stmt|;
name|JarFile
name|jarFile
init|=
operator|new
name|JarFile
argument_list|(
name|dst
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Enumeration
argument_list|<
name|JarEntry
argument_list|>
name|entries
init|=
name|jarFile
operator|.
name|entries
argument_list|()
decl_stmt|;
comment|// get entries inside a jar file
while|while
condition|(
name|entries
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|JarEntry
name|entry
init|=
name|entries
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|Matcher
name|m
init|=
name|libJarPattern
operator|.
name|matcher
argument_list|(
name|entry
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|parentDirStr
argument_list|,
literal|"."
operator|+
name|pathPrefix
operator|+
literal|"."
operator|+
name|path
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|"."
operator|+
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|FileOutputStream
name|outStream
init|=
operator|new
name|FileOutputStream
argument_list|(
name|file
argument_list|)
init|)
block|{
name|IOUtils
operator|.
name|copyBytes
argument_list|(
name|jarFile
operator|.
name|getInputStream
argument_list|(
name|entry
argument_list|)
argument_list|,
name|outStream
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|file
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|addURL
argument_list|(
name|file
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|jarFile
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|validFileEncountered
operator|=
literal|true
expr_stmt|;
comment|// Set to true when encountering a file
block|}
block|}
if|if
condition|(
name|validFileEncountered
operator|==
literal|false
condition|)
block|{
comment|// all items returned by globStatus() are directories
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"No file found matching "
operator|+
name|pathPattern1
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
comment|// This method is used in unit test
specifier|public
specifier|static
name|CoprocessorClassLoader
name|getIfCached
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|path
argument_list|,
literal|"The jar path is null!"
argument_list|)
expr_stmt|;
return|return
name|classLoadersCache
operator|.
name|get
argument_list|(
name|path
argument_list|)
return|;
block|}
comment|// This method is used in unit test
specifier|public
specifier|static
name|Collection
argument_list|<
name|?
extends|extends
name|ClassLoader
argument_list|>
name|getAllCached
parameter_list|()
block|{
return|return
name|classLoadersCache
operator|.
name|values
argument_list|()
return|;
block|}
comment|// This method is used in unit test
specifier|public
specifier|static
name|void
name|clearCache
parameter_list|()
block|{
name|classLoadersCache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get a CoprocessorClassLoader for a coprocessor jar path from cache.    * If not in cache, create one.    *    * @param path the path to the coprocessor jar file to load classes from    * @param parent the parent class loader for exempted classes    * @param pathPrefix a prefix used in temp path name to store the jar file locally    * @param conf the configuration used to create the class loader, if needed    * @return a CoprocessorClassLoader for the coprocessor jar path    * @throws IOException    */
specifier|public
specifier|static
name|CoprocessorClassLoader
name|getClassLoader
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|ClassLoader
name|parent
parameter_list|,
specifier|final
name|String
name|pathPrefix
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|CoprocessorClassLoader
name|cl
init|=
name|getIfCached
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|String
name|pathStr
init|=
name|path
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|cl
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found classloader "
operator|+
name|cl
operator|+
literal|" for "
operator|+
name|pathStr
argument_list|)
expr_stmt|;
return|return
name|cl
return|;
block|}
if|if
condition|(
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|isFile
argument_list|(
name|path
argument_list|)
operator|&&
operator|!
name|pathStr
operator|.
name|endsWith
argument_list|(
literal|".jar"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|pathStr
operator|+
literal|": not a jar file?"
argument_list|)
throw|;
block|}
name|Lock
name|lock
init|=
name|locker
operator|.
name|acquireLock
argument_list|(
name|pathStr
argument_list|)
decl_stmt|;
try|try
block|{
name|cl
operator|=
name|getIfCached
argument_list|(
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|cl
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found classloader "
operator|+
name|cl
operator|+
literal|" for "
operator|+
name|pathStr
argument_list|)
expr_stmt|;
return|return
name|cl
return|;
block|}
name|cl
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|CoprocessorClassLoader
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|CoprocessorClassLoader
name|run
parameter_list|()
block|{
return|return
operator|new
name|CoprocessorClassLoader
argument_list|(
name|parent
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|cl
operator|.
name|init
argument_list|(
name|path
argument_list|,
name|pathPrefix
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// Cache class loader as a weak value, will be GC'ed when no reference left
name|CoprocessorClassLoader
name|prev
init|=
name|classLoadersCache
operator|.
name|putIfAbsent
argument_list|(
name|path
argument_list|,
name|cl
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
comment|// Lost update race, use already added class loader
name|LOG
operator|.
name|warn
argument_list|(
literal|"THIS SHOULD NOT HAPPEN, a class loader"
operator|+
literal|" is already cached for "
operator|+
name|pathStr
argument_list|)
expr_stmt|;
name|cl
operator|=
name|prev
expr_stmt|;
block|}
return|return
name|cl
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
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
return|return
name|loadClass
argument_list|(
name|name
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|includedClassPrefixes
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
comment|// Delegate to the parent immediately if this class is exempt
if|if
condition|(
name|isClassExempt
argument_list|(
name|name
argument_list|,
name|includedClassPrefixes
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
literal|"Skipping exempt class "
operator|+
name|name
operator|+
literal|" - delegating directly to parent"
argument_list|)
expr_stmt|;
block|}
return|return
name|parent
operator|.
name|loadClass
argument_list|(
name|name
argument_list|)
return|;
block|}
synchronized|synchronized
init|(
name|getClassLoadingLock
argument_list|(
name|name
argument_list|)
init|)
block|{
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
comment|// Try to find this class using the URLs passed to this ClassLoader
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
name|e
parameter_list|)
block|{
comment|// Class not found using this ClassLoader, so delegate to parent
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
literal|" not found - delegating to parent"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|clasz
operator|=
name|parent
operator|.
name|loadClass
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e2
parameter_list|)
block|{
comment|// Class not found in this ClassLoader or in the parent ClassLoader
comment|// Log some debug output before re-throwing ClassNotFoundException
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
literal|" not found in parent loader"
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e2
throw|;
block|}
block|}
block|}
return|return
name|clasz
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|URL
name|getResource
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|URL
name|resource
init|=
literal|null
decl_stmt|;
name|boolean
name|parentLoaded
init|=
literal|false
decl_stmt|;
comment|// Delegate to the parent first if necessary
if|if
condition|(
name|loadResourceUsingParentFirst
argument_list|(
name|name
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
literal|"Checking parent first for resource "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
name|resource
operator|=
name|super
operator|.
name|getResource
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|parentLoaded
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|resource
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|getClassLoadingLock
argument_list|(
name|name
argument_list|)
init|)
block|{
comment|// Try to find the resource in this jar
name|resource
operator|=
name|findResource
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|resource
operator|==
literal|null
operator|)
operator|&&
operator|!
name|parentLoaded
condition|)
block|{
comment|// Not found in this jar and we haven't attempted to load
comment|// the resource in the parent yet; fall back to the parent
name|resource
operator|=
name|super
operator|.
name|getResource
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|resource
return|;
block|}
comment|/**    * Determines whether the given class should be exempt from being loaded    * by this ClassLoader.    * @param name the name of the class to test.    * @return true if the class should *not* be loaded by this ClassLoader;    * false otherwise.    */
specifier|protected
name|boolean
name|isClassExempt
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|includedClassPrefixes
parameter_list|)
block|{
if|if
condition|(
name|includedClassPrefixes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|clsName
range|:
name|includedClassPrefixes
control|)
block|{
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
name|clsName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
for|for
control|(
name|String
name|exemptPrefix
range|:
name|CLASS_PREFIX_EXEMPTIONS
control|)
block|{
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
name|exemptPrefix
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Determines whether we should attempt to load the given resource using the    * parent first before attempting to load the resource using this ClassLoader.    * @param name the name of the resource to test.    * @return true if we should attempt to load the resource using the parent    * first; false if we should attempt to load the resource using this    * ClassLoader first.    */
specifier|protected
name|boolean
name|loadResourceUsingParentFirst
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|Pattern
name|resourcePattern
range|:
name|RESOURCE_LOAD_PARENT_FIRST_PATTERNS
control|)
block|{
if|if
condition|(
name|resourcePattern
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

