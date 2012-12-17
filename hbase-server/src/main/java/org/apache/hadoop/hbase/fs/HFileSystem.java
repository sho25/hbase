begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|fs
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Proxy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|UndeclaredThrowableException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
name|FilterFileSystem
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
name|FSDataOutputStream
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
name|LocalFileSystem
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
name|hbase
operator|.
name|ServerName
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|regionserver
operator|.
name|wal
operator|.
name|HLogUtil
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
name|hdfs
operator|.
name|DFSClient
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|hdfs
operator|.
name|protocol
operator|.
name|ClientProtocol
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
name|hdfs
operator|.
name|protocol
operator|.
name|DatanodeInfo
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
name|hdfs
operator|.
name|protocol
operator|.
name|LocatedBlock
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
name|hdfs
operator|.
name|protocol
operator|.
name|LocatedBlocks
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
name|util
operator|.
name|ReflectionUtils
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
name|util
operator|.
name|Progressable
import|;
end_import

begin_comment
comment|/**  * An encapsulation for the FileSystem object that hbase uses to access  * data. This class allows the flexibility of using    * separate filesystem objects for reading and writing hfiles and hlogs.  * In future, if we want to make hlogs be in a different filesystem,  * this is the place to make it happen.  */
end_comment

begin_class
specifier|public
class|class
name|HFileSystem
extends|extends
name|FilterFileSystem
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|noChecksumFs
decl_stmt|;
comment|// read hfile data from storage
specifier|private
specifier|final
name|boolean
name|useHBaseChecksum
decl_stmt|;
comment|/**    * Create a FileSystem object for HBase regionservers.    * @param conf The configuration to be used for the filesystem    * @param useHBaseChecksum if true, then use    *        checksum verfication in hbase, otherwise    *        delegate checksum verification to the FileSystem.    */
specifier|public
name|HFileSystem
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|useHBaseChecksum
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create the default filesystem with checksum verification switched on.
comment|// By default, any operation to this FilterFileSystem occurs on
comment|// the underlying filesystem that has checksums switched on.
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|useHBaseChecksum
operator|=
name|useHBaseChecksum
expr_stmt|;
name|fs
operator|.
name|initialize
argument_list|(
name|getDefaultUri
argument_list|(
name|conf
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|addLocationsOrderInterceptor
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// If hbase checksum verification is switched on, then create a new
comment|// filesystem object that has cksum verification turned off.
comment|// We will avoid verifying checksums in the fs client, instead do it
comment|// inside of hbase.
comment|// If this is the local file system hadoop has a bug where seeks
comment|// do not go to the correct location if setVerifyChecksum(false) is called.
comment|// This manifests itself in that incorrect data is read and HFileBlocks won't be able to read
comment|// their header magic numbers. See HBASE-5885
if|if
condition|(
name|useHBaseChecksum
operator|&&
operator|!
operator|(
name|fs
operator|instanceof
name|LocalFileSystem
operator|)
condition|)
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"dfs.client.read.shortcircuit.skip.checksum"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|noChecksumFs
operator|=
name|newInstanceFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|noChecksumFs
operator|.
name|setVerifyChecksum
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|noChecksumFs
operator|=
name|fs
expr_stmt|;
block|}
block|}
comment|/**    * Wrap a FileSystem object within a HFileSystem. The noChecksumFs and    * writefs are both set to be the same specified fs.     * Do not verify hbase-checksums while reading data from filesystem.    * @param fs Set the noChecksumFs and writeFs to this specified filesystem.    */
specifier|public
name|HFileSystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|noChecksumFs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|useHBaseChecksum
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Returns the filesystem that is specially setup for     * doing reads from storage. This object avoids doing     * checksum verifications for reads.    * @return The FileSystem object that can be used to read data    *         from files.    */
specifier|public
name|FileSystem
name|getNoChecksumFs
parameter_list|()
block|{
return|return
name|noChecksumFs
return|;
block|}
comment|/**    * Returns the underlying filesystem    * @return The underlying FileSystem for this FilterFileSystem object.    */
specifier|public
name|FileSystem
name|getBackingFs
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|fs
return|;
block|}
comment|/**    * Are we verifying checksums in HBase?    * @return True, if hbase is configured to verify checksums,    *         otherwise false.    */
specifier|public
name|boolean
name|useHBaseChecksum
parameter_list|()
block|{
return|return
name|useHBaseChecksum
return|;
block|}
comment|/**    * Close this filesystem object    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|noChecksumFs
operator|!=
name|fs
condition|)
block|{
name|this
operator|.
name|noChecksumFs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Returns a brand new instance of the FileSystem. It does not use    * the FileSystem.Cache. In newer versions of HDFS, we can directly    * invoke FileSystem.newInstance(Configuration).    *     * @param conf Configuration    * @return A new instance of the filesystem    */
specifier|private
specifier|static
name|FileSystem
name|newInstanceFileSystem
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|URI
name|uri
init|=
name|FileSystem
operator|.
name|getDefaultUri
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"fs."
operator|+
name|uri
operator|.
name|getScheme
argument_list|()
operator|+
literal|".impl"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|clazz
operator|!=
literal|null
condition|)
block|{
comment|// This will be true for Hadoop 1.0, or 0.20.
name|fs
operator|=
operator|(
name|FileSystem
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|.
name|initialize
argument_list|(
name|uri
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// For Hadoop 2.0, we have to go through FileSystem for the filesystem
comment|// implementation to be loaded by the service loader in case it has not
comment|// been loaded yet.
name|Configuration
name|clone
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|clone
operator|.
name|setBoolean
argument_list|(
literal|"fs."
operator|+
name|uri
operator|.
name|getScheme
argument_list|()
operator|+
literal|".impl.disable.cache"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|uri
argument_list|,
name|clone
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fs
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No FileSystem for scheme: "
operator|+
name|uri
operator|.
name|getScheme
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|fs
return|;
block|}
specifier|public
specifier|static
name|boolean
name|addLocationsOrderInterceptor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|addLocationsOrderInterceptor
argument_list|(
name|conf
argument_list|,
operator|new
name|ReorderWALBlocks
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Add an interceptor on the calls to the namenode#getBlockLocations from the DFSClient    * linked to this FileSystem. See HBASE-6435 for the background.    *<p/>    * There should be no reason, except testing, to create a specific ReorderBlocks.    *    * @return true if the interceptor was added, false otherwise.    */
specifier|static
name|boolean
name|addLocationsOrderInterceptor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|ReorderBlocks
name|lrb
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting addLocationsOrderInterceptor with class "
operator|+
name|lrb
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.filesystem.reorder.blocks"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// activated by default
name|LOG
operator|.
name|debug
argument_list|(
literal|"addLocationsOrderInterceptor configured to false"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't get the file system from the conf."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The file system is not a DistributedFileSystem."
operator|+
literal|"Not adding block location reordering"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fs
decl_stmt|;
name|DFSClient
name|dfsc
init|=
name|dfs
operator|.
name|getClient
argument_list|()
decl_stmt|;
if|if
condition|(
name|dfsc
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The DistributedFileSystem does not contain a DFSClient. Can't add the location "
operator|+
literal|"block reordering interceptor. Continuing, but this is unexpected."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
try|try
block|{
name|Field
name|nf
init|=
name|DFSClient
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"namenode"
argument_list|)
decl_stmt|;
name|nf
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Field
name|modifiersField
init|=
name|Field
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"modifiers"
argument_list|)
decl_stmt|;
name|modifiersField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|modifiersField
operator|.
name|setInt
argument_list|(
name|nf
argument_list|,
name|nf
operator|.
name|getModifiers
argument_list|()
operator|&
operator|~
name|Modifier
operator|.
name|FINAL
argument_list|)
expr_stmt|;
name|ClientProtocol
name|namenode
init|=
operator|(
name|ClientProtocol
operator|)
name|nf
operator|.
name|get
argument_list|(
name|dfsc
argument_list|)
decl_stmt|;
if|if
condition|(
name|namenode
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The DFSClient is not linked to a namenode. Can't add the location block"
operator|+
literal|" reordering interceptor. Continuing, but this is unexpected."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|ClientProtocol
name|cp1
init|=
name|createReorderingProxy
argument_list|(
name|namenode
argument_list|,
name|lrb
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|nf
operator|.
name|set
argument_list|(
name|dfsc
argument_list|,
name|cp1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added intercepting call to namenode#getBlockLocations"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't modify the DFSClient#namenode field to add the location reorder."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't modify the DFSClient#namenode field to add the location reorder."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|ClientProtocol
name|createReorderingProxy
parameter_list|(
specifier|final
name|ClientProtocol
name|cp
parameter_list|,
specifier|final
name|ReorderBlocks
name|lrb
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|(
name|ClientProtocol
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|cp
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|ClientProtocol
operator|.
name|class
block|}
argument_list|,
operator|new
name|InvocationHandler
argument_list|()
block|{
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
try|try
block|{
name|Object
name|res
init|=
name|method
operator|.
name|invoke
argument_list|(
name|cp
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|!=
literal|null
operator|&&
name|args
operator|!=
literal|null
operator|&&
name|args
operator|.
name|length
operator|==
literal|3
operator|&&
literal|"getBlockLocations"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
operator|&&
name|res
operator|instanceof
name|LocatedBlocks
operator|&&
name|args
index|[
literal|0
index|]
operator|instanceof
name|String
operator|&&
name|args
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
name|lrb
operator|.
name|reorderBlocks
argument_list|(
name|conf
argument_list|,
operator|(
name|LocatedBlocks
operator|)
name|res
argument_list|,
operator|(
name|String
operator|)
name|args
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
comment|// We will have this for all the exception, checked on not, sent
comment|//  by any layer, including the functional exception
name|Throwable
name|cause
init|=
name|ite
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Proxy invocation failed and getCause is null"
argument_list|,
name|ite
argument_list|)
throw|;
block|}
if|if
condition|(
name|cause
operator|instanceof
name|UndeclaredThrowableException
condition|)
block|{
name|Throwable
name|causeCause
init|=
name|cause
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|causeCause
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"UndeclaredThrowableException had null cause!"
argument_list|)
throw|;
block|}
name|cause
operator|=
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
throw|throw
name|cause
throw|;
block|}
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * Interface to implement to add a specific reordering logic in hdfs.    */
specifier|static
interface|interface
name|ReorderBlocks
block|{
comment|/**      *      * @param conf - the conf to use      * @param lbs - the LocatedBlocks to reorder      * @param src - the file name currently read      * @throws IOException - if something went wrong      */
specifier|public
name|void
name|reorderBlocks
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LocatedBlocks
name|lbs
parameter_list|,
name|String
name|src
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * We're putting at lowest priority the hlog files blocks that are on the same datanode    * as the original regionserver which created these files. This because we fear that the    * datanode is actually dead, so if we use it it will timeout.    */
specifier|static
class|class
name|ReorderWALBlocks
implements|implements
name|ReorderBlocks
block|{
specifier|public
name|void
name|reorderBlocks
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LocatedBlocks
name|lbs
parameter_list|,
name|String
name|src
parameter_list|)
throws|throws
name|IOException
block|{
name|ServerName
name|sn
init|=
name|HLogUtil
operator|.
name|getServerNameFromHLogDirectoryName
argument_list|(
name|conf
argument_list|,
name|src
argument_list|)
decl_stmt|;
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
block|{
comment|// It's not an HLOG
return|return;
block|}
comment|// Ok, so it's an HLog
name|String
name|hostName
init|=
name|sn
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|src
operator|+
literal|" is an HLog file, so reordering blocks, last hostname will be:"
operator|+
name|hostName
argument_list|)
expr_stmt|;
comment|// Just check for all blocks
for|for
control|(
name|LocatedBlock
name|lb
range|:
name|lbs
operator|.
name|getLocatedBlocks
argument_list|()
control|)
block|{
name|DatanodeInfo
index|[]
name|dnis
init|=
name|lb
operator|.
name|getLocations
argument_list|()
decl_stmt|;
if|if
condition|(
name|dnis
operator|!=
literal|null
operator|&&
name|dnis
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dnis
operator|.
name|length
operator|-
literal|1
operator|&&
operator|!
name|found
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hostName
operator|.
name|equals
argument_list|(
name|dnis
index|[
name|i
index|]
operator|.
name|getHostName
argument_list|()
argument_list|)
condition|)
block|{
comment|// advance the other locations by one and put this one at the last place.
name|DatanodeInfo
name|toLast
init|=
name|dnis
index|[
name|i
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|dnis
argument_list|,
name|i
operator|+
literal|1
argument_list|,
name|dnis
argument_list|,
name|i
argument_list|,
name|dnis
operator|.
name|length
operator|-
name|i
operator|-
literal|1
argument_list|)
expr_stmt|;
name|dnis
index|[
name|dnis
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|toLast
expr_stmt|;
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
comment|/**    * Create a new HFileSystem object, similar to FileSystem.get().    * This returns a filesystem object that avoids checksum    * verification in the filesystem for hfileblock-reads.    * For these blocks, checksum verification is done by HBase.    */
specifier|static
specifier|public
name|FileSystem
name|get
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HFileSystem
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Wrap a LocalFileSystem within a HFileSystem.    */
specifier|static
specifier|public
name|FileSystem
name|getLocalFs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HFileSystem
argument_list|(
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * The org.apache.hadoop.fs.FilterFileSystem does not yet support     * createNonRecursive. This is a hadoop bug and when it is fixed in Hadoop,    * this definition will go away.    */
specifier|public
name|FSDataOutputStream
name|createNonRecursive
parameter_list|(
name|Path
name|f
parameter_list|,
name|boolean
name|overwrite
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|short
name|replication
parameter_list|,
name|long
name|blockSize
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|createNonRecursive
argument_list|(
name|f
argument_list|,
name|overwrite
argument_list|,
name|bufferSize
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
name|progress
argument_list|)
return|;
block|}
block|}
end_class

end_unit

