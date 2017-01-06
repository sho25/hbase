begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|replication
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Deque
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
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|Callable
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
name|ExecutionException
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
name|Future
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
name|LinkedBlockingQueue
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
name|ThreadPoolExecutor
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
name|TimeUnit
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
name|FileUtil
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
name|permission
operator|.
name|FsPermission
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
name|HBaseConfiguration
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
name|HConstants
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
name|TableName
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
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|client
operator|.
name|RegionLocator
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
name|client
operator|.
name|Table
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
name|mapreduce
operator|.
name|LoadIncrementalHFiles
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
name|mapreduce
operator|.
name|LoadIncrementalHFiles
operator|.
name|LoadQueueItem
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
name|security
operator|.
name|User
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
name|security
operator|.
name|UserProvider
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
name|security
operator|.
name|token
operator|.
name|FsDelegationToken
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|FSUtils
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
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * It is used for replicating HFile entries. It will first copy parallely all the hfiles to a local  * staging directory and then it will use ({@link LoadIncrementalHFiles} to prepare a collection of  * {@link LoadQueueItem} which will finally be loaded(replicated) into the table of this cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileReplicator
block|{
comment|/** Maximum number of threads to allow in pool to copy hfiles during replication */
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATION_BULKLOAD_COPY_MAXTHREADS_KEY
init|=
literal|"hbase.replication.bulkload.copy.maxthreads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|REPLICATION_BULKLOAD_COPY_MAXTHREADS_DEFAULT
init|=
literal|10
decl_stmt|;
comment|/** Number of hfiles to copy per thread during replication */
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATION_BULKLOAD_COPY_HFILES_PERTHREAD_KEY
init|=
literal|"hbase.replication.bulkload.copy.hfiles.perthread"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|REPLICATION_BULKLOAD_COPY_HFILES_PERTHREAD_DEFAULT
init|=
literal|10
decl_stmt|;
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
name|HFileReplicator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|UNDERSCORE
init|=
literal|"_"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|FsPermission
name|PERM_ALL_ACCESS
init|=
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwxrwxrwx"
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|sourceClusterConf
decl_stmt|;
specifier|private
name|String
name|sourceBaseNamespaceDirPath
decl_stmt|;
specifier|private
name|String
name|sourceHFileArchiveDirPath
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|bulkLoadHFileMap
decl_stmt|;
specifier|private
name|FileSystem
name|sinkFs
decl_stmt|;
specifier|private
name|FsDelegationToken
name|fsDelegationToken
decl_stmt|;
specifier|private
name|UserProvider
name|userProvider
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Connection
name|connection
decl_stmt|;
specifier|private
name|Path
name|hbaseStagingDir
decl_stmt|;
specifier|private
name|ThreadPoolExecutor
name|exec
decl_stmt|;
specifier|private
name|int
name|maxCopyThreads
decl_stmt|;
specifier|private
name|int
name|copiesPerThread
decl_stmt|;
specifier|public
name|HFileReplicator
parameter_list|(
name|Configuration
name|sourceClusterConf
parameter_list|,
name|String
name|sourceBaseNamespaceDirPath
parameter_list|,
name|String
name|sourceHFileArchiveDirPath
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|tableQueueMap
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|sourceClusterConf
operator|=
name|sourceClusterConf
expr_stmt|;
name|this
operator|.
name|sourceBaseNamespaceDirPath
operator|=
name|sourceBaseNamespaceDirPath
expr_stmt|;
name|this
operator|.
name|sourceHFileArchiveDirPath
operator|=
name|sourceHFileArchiveDirPath
expr_stmt|;
name|this
operator|.
name|bulkLoadHFileMap
operator|=
name|tableQueueMap
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|userProvider
operator|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fsDelegationToken
operator|=
operator|new
name|FsDelegationToken
argument_list|(
name|userProvider
argument_list|,
literal|"renewer"
argument_list|)
expr_stmt|;
name|this
operator|.
name|hbaseStagingDir
operator|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|HConstants
operator|.
name|BULKLOAD_STAGING_DIR_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxCopyThreads
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
name|REPLICATION_BULKLOAD_COPY_MAXTHREADS_KEY
argument_list|,
name|REPLICATION_BULKLOAD_COPY_MAXTHREADS_DEFAULT
argument_list|)
expr_stmt|;
name|ThreadFactoryBuilder
name|builder
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setNameFormat
argument_list|(
literal|"HFileReplicationCallable-%1$d"
argument_list|)
expr_stmt|;
name|this
operator|.
name|exec
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|maxCopyThreads
argument_list|,
name|maxCopyThreads
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|exec
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|copiesPerThread
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|REPLICATION_BULKLOAD_COPY_HFILES_PERTHREAD_KEY
argument_list|,
name|REPLICATION_BULKLOAD_COPY_HFILES_PERTHREAD_DEFAULT
argument_list|)
expr_stmt|;
name|sinkFs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Void
name|replicate
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Copy all the hfiles to the local file system
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|tableStagingDirsMap
init|=
name|copyHFilesToStagingDir
argument_list|()
decl_stmt|;
name|int
name|maxRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|BULKLOAD_MAX_RETRIES_NUMBER
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|tableStagingDir
range|:
name|tableStagingDirsMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|tableNameString
init|=
name|tableStagingDir
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Path
name|stagingDir
init|=
name|tableStagingDir
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|LoadIncrementalHFiles
name|loadHFiles
init|=
literal|null
decl_stmt|;
try|try
block|{
name|loadHFiles
operator|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to initialize LoadIncrementalHFiles for replicating bulk loaded"
operator|+
literal|" data."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|Configuration
name|newConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|newConf
operator|.
name|set
argument_list|(
name|LoadIncrementalHFiles
operator|.
name|CREATE_TABLE_CONF_KEY
argument_list|,
literal|"no"
argument_list|)
expr_stmt|;
name|loadHFiles
operator|.
name|setConf
argument_list|(
name|newConf
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableNameString
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|this
operator|.
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// Prepare collection of queue of hfiles to be loaded(replicated)
name|Deque
argument_list|<
name|LoadQueueItem
argument_list|>
name|queue
init|=
operator|new
name|LinkedList
argument_list|<
name|LoadQueueItem
argument_list|>
argument_list|()
decl_stmt|;
name|loadHFiles
operator|.
name|prepareHFileQueue
argument_list|(
name|stagingDir
argument_list|,
name|table
argument_list|,
name|queue
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Replication process did not find any files to replicate in directory "
operator|+
name|stagingDir
operator|.
name|toUri
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
try|try
init|(
name|RegionLocator
name|locator
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|fsDelegationToken
operator|.
name|acquireDelegationToken
argument_list|(
name|sinkFs
argument_list|)
expr_stmt|;
comment|// Set the staging directory which will be used by LoadIncrementalHFiles for loading the
comment|// data
name|loadHFiles
operator|.
name|setBulkToken
argument_list|(
name|stagingDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|doBulkLoad
argument_list|(
name|loadHFiles
argument_list|,
name|table
argument_list|,
name|queue
argument_list|,
name|locator
argument_list|,
name|maxRetries
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanup
argument_list|(
name|stagingDir
operator|.
name|toString
argument_list|()
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|doBulkLoad
parameter_list|(
name|LoadIncrementalHFiles
name|loadHFiles
parameter_list|,
name|Table
name|table
parameter_list|,
name|Deque
argument_list|<
name|LoadQueueItem
argument_list|>
name|queue
parameter_list|,
name|RegionLocator
name|locator
parameter_list|,
name|int
name|maxRetries
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|startEndKeys
decl_stmt|;
while|while
condition|(
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// need to reload split keys each iteration.
name|startEndKeys
operator|=
name|locator
operator|.
name|getStartEndKeys
argument_list|()
expr_stmt|;
if|if
condition|(
name|count
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error occurred while replicating HFiles, retry attempt "
operator|+
name|count
operator|+
literal|" with "
operator|+
name|queue
operator|.
name|size
argument_list|()
operator|+
literal|" files still remaining to replicate."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxRetries
operator|!=
literal|0
operator|&&
name|count
operator|>=
name|maxRetries
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Retry attempted "
operator|+
name|count
operator|+
literal|" times without completing, bailing out."
argument_list|)
throw|;
block|}
name|count
operator|++
expr_stmt|;
comment|// Try bulk load
name|loadHFiles
operator|.
name|loadHFileQueue
argument_list|(
name|table
argument_list|,
name|connection
argument_list|,
name|queue
argument_list|,
name|startEndKeys
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanup
parameter_list|(
name|String
name|stagingDir
parameter_list|,
name|Table
name|table
parameter_list|)
block|{
comment|// Release the file system delegation token
name|fsDelegationToken
operator|.
name|releaseDelegationToken
argument_list|()
expr_stmt|;
comment|// Delete the staging directory
if|if
condition|(
name|stagingDir
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|sinkFs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|stagingDir
argument_list|)
argument_list|,
literal|true
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
literal|"Failed to delete the staging directory "
operator|+
name|stagingDir
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Do not close the file system
comment|/*      * if (sinkFs != null) { try { sinkFs.close(); } catch (IOException e) { LOG.warn(      * "Failed to close the file system"); } }      */
comment|// Close the table
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
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
literal|"Failed to close the table."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|copyHFilesToStagingDir
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|mapOfCopiedHFiles
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|familyHFilePathsPair
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hfilePaths
decl_stmt|;
name|byte
index|[]
name|family
decl_stmt|;
name|Path
name|familyStagingDir
decl_stmt|;
name|int
name|familyHFilePathsPairsListSize
decl_stmt|;
name|int
name|totalNoOfHFiles
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|familyHFilePathsPairsList
decl_stmt|;
name|FileSystem
name|sourceFs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Path
name|sourceClusterPath
init|=
operator|new
name|Path
argument_list|(
name|sourceBaseNamespaceDirPath
argument_list|)
decl_stmt|;
comment|/*        * Path#getFileSystem will by default get the FS from cache. If both source and sink cluster        * has same FS name service then it will return peer cluster FS. To avoid this we explicitly        * disable the loading of FS from cache, so that a new FS is created with source cluster        * configuration.        */
name|String
name|sourceScheme
init|=
name|sourceClusterPath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|disableCacheName
init|=
name|String
operator|.
name|format
argument_list|(
literal|"fs.%s.impl.disable.cache"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|sourceScheme
block|}
argument_list|)
decl_stmt|;
name|sourceClusterConf
operator|.
name|setBoolean
argument_list|(
name|disableCacheName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|sourceFs
operator|=
name|sourceClusterPath
operator|.
name|getFileSystem
argument_list|(
name|sourceClusterConf
argument_list|)
expr_stmt|;
name|User
name|user
init|=
name|userProvider
operator|.
name|getCurrent
argument_list|()
decl_stmt|;
comment|// For each table name in the map
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|tableEntry
range|:
name|bulkLoadHFileMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|tableName
init|=
name|tableEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// Create staging directory for each table
name|Path
name|stagingDir
init|=
name|createStagingDir
argument_list|(
name|hbaseStagingDir
argument_list|,
name|user
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|familyHFilePathsPairsList
operator|=
name|tableEntry
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|familyHFilePathsPairsListSize
operator|=
name|familyHFilePathsPairsList
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// For each list of family hfile paths pair in the table
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|familyHFilePathsPairsListSize
condition|;
name|i
operator|++
control|)
block|{
name|familyHFilePathsPair
operator|=
name|familyHFilePathsPairsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|family
operator|=
name|familyHFilePathsPair
operator|.
name|getFirst
argument_list|()
expr_stmt|;
name|hfilePaths
operator|=
name|familyHFilePathsPair
operator|.
name|getSecond
argument_list|()
expr_stmt|;
name|familyStagingDir
operator|=
operator|new
name|Path
argument_list|(
name|stagingDir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|totalNoOfHFiles
operator|=
name|hfilePaths
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// For each list of hfile paths for the family
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|c
decl_stmt|;
name|Future
argument_list|<
name|Void
argument_list|>
name|future
decl_stmt|;
name|int
name|currentCopied
init|=
literal|0
decl_stmt|;
comment|// Copy the hfiles parallely
while|while
condition|(
name|totalNoOfHFiles
operator|>
name|currentCopied
operator|+
name|this
operator|.
name|copiesPerThread
condition|)
block|{
name|c
operator|=
operator|new
name|Copier
argument_list|(
name|sourceFs
argument_list|,
name|familyStagingDir
argument_list|,
name|hfilePaths
operator|.
name|subList
argument_list|(
name|currentCopied
argument_list|,
name|currentCopied
operator|+
name|this
operator|.
name|copiesPerThread
argument_list|)
argument_list|)
expr_stmt|;
name|future
operator|=
name|exec
operator|.
name|submit
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|future
argument_list|)
expr_stmt|;
name|currentCopied
operator|+=
name|this
operator|.
name|copiesPerThread
expr_stmt|;
block|}
name|int
name|remaining
init|=
name|totalNoOfHFiles
operator|-
name|currentCopied
decl_stmt|;
if|if
condition|(
name|remaining
operator|>
literal|0
condition|)
block|{
name|c
operator|=
operator|new
name|Copier
argument_list|(
name|sourceFs
argument_list|,
name|familyStagingDir
argument_list|,
name|hfilePaths
operator|.
name|subList
argument_list|(
name|currentCopied
argument_list|,
name|currentCopied
operator|+
name|remaining
argument_list|)
argument_list|)
expr_stmt|;
name|future
operator|=
name|exec
operator|.
name|submit
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|future
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Future
argument_list|<
name|Void
argument_list|>
name|f
range|:
name|futures
control|)
block|{
try|try
block|{
name|f
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|InterruptedIOException
name|iioe
init|=
operator|new
name|InterruptedIOException
argument_list|(
literal|"Failed to copy HFiles to local file system. This will be retried again "
operator|+
literal|"by the source cluster."
argument_list|)
decl_stmt|;
name|iioe
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|iioe
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to copy HFiles to local file system. This will "
operator|+
literal|"be retried again by the source cluster."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// Add the staging directory to this table. Staging directory contains all the hfiles
comment|// belonging to this table
name|mapOfCopiedHFiles
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|stagingDir
argument_list|)
expr_stmt|;
block|}
return|return
name|mapOfCopiedHFiles
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|sourceFs
operator|!=
literal|null
condition|)
block|{
name|sourceFs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|exec
operator|!=
literal|null
condition|)
block|{
name|exec
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Path
name|createStagingDir
parameter_list|(
name|Path
name|baseDir
parameter_list|,
name|User
name|user
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|tblName
init|=
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|.
name|replace
argument_list|(
literal|":"
argument_list|,
name|UNDERSCORE
argument_list|)
decl_stmt|;
name|int
name|RANDOM_WIDTH
init|=
literal|320
decl_stmt|;
name|int
name|RANDOM_RADIX
init|=
literal|32
decl_stmt|;
name|String
name|doubleUnderScore
init|=
name|UNDERSCORE
operator|+
name|UNDERSCORE
decl_stmt|;
name|String
name|randomDir
init|=
name|user
operator|.
name|getShortName
argument_list|()
operator|+
name|doubleUnderScore
operator|+
name|tblName
operator|+
name|doubleUnderScore
operator|+
operator|(
operator|new
name|BigInteger
argument_list|(
name|RANDOM_WIDTH
argument_list|,
operator|new
name|SecureRandom
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|(
name|RANDOM_RADIX
argument_list|)
operator|)
decl_stmt|;
return|return
name|createStagingDir
argument_list|(
name|baseDir
argument_list|,
name|user
argument_list|,
name|randomDir
argument_list|)
return|;
block|}
specifier|private
name|Path
name|createStagingDir
parameter_list|(
name|Path
name|baseDir
parameter_list|,
name|User
name|user
parameter_list|,
name|String
name|randomDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|baseDir
argument_list|,
name|randomDir
argument_list|)
decl_stmt|;
name|sinkFs
operator|.
name|mkdirs
argument_list|(
name|p
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
name|sinkFs
operator|.
name|setPermission
argument_list|(
name|p
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
comment|/**    * This class will copy the given hfiles from the given source file system to the given local file    * system staging directory.    */
specifier|private
class|class
name|Copier
implements|implements
name|Callable
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
name|FileSystem
name|sourceFs
decl_stmt|;
specifier|private
name|Path
name|stagingDir
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|hfiles
decl_stmt|;
specifier|public
name|Copier
parameter_list|(
name|FileSystem
name|sourceFs
parameter_list|,
specifier|final
name|Path
name|stagingDir
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|hfiles
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|sourceFs
operator|=
name|sourceFs
expr_stmt|;
name|this
operator|.
name|stagingDir
operator|=
name|stagingDir
expr_stmt|;
name|this
operator|.
name|hfiles
operator|=
name|hfiles
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|sourceHFilePath
decl_stmt|;
name|Path
name|localHFilePath
decl_stmt|;
name|int
name|totalHFiles
init|=
name|hfiles
operator|.
name|size
argument_list|()
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
name|totalHFiles
condition|;
name|i
operator|++
control|)
block|{
name|sourceHFilePath
operator|=
operator|new
name|Path
argument_list|(
name|sourceBaseNamespaceDirPath
argument_list|,
name|hfiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|localHFilePath
operator|=
operator|new
name|Path
argument_list|(
name|stagingDir
argument_list|,
name|sourceHFilePath
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|FileUtil
operator|.
name|copy
argument_list|(
name|sourceFs
argument_list|,
name|sourceHFilePath
argument_list|,
name|sinkFs
argument_list|,
name|localHFilePath
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// If any other exception other than FNFE then we will fail the replication requests and
comment|// source will retry to replicate these data.
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to copy hfile from "
operator|+
name|sourceHFilePath
operator|+
literal|" to "
operator|+
name|localHFilePath
operator|+
literal|". Trying to copy from hfile archive directory."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|sourceHFilePath
operator|=
operator|new
name|Path
argument_list|(
name|sourceHFileArchiveDirPath
argument_list|,
name|hfiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|FileUtil
operator|.
name|copy
argument_list|(
name|sourceFs
argument_list|,
name|sourceHFilePath
argument_list|,
name|sinkFs
argument_list|,
name|localHFilePath
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e1
parameter_list|)
block|{
comment|// This will mean that the hfile does not exists any where in source cluster FS. So we
comment|// cannot do anything here just log and continue.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to copy hfile from "
operator|+
name|sourceHFilePath
operator|+
literal|" to "
operator|+
name|localHFilePath
operator|+
literal|". Hence ignoring this hfile from replication.."
argument_list|,
name|e1
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
name|sinkFs
operator|.
name|setPermission
argument_list|(
name|localHFilePath
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

