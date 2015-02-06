begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mob
operator|.
name|filecompactions
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|ExecutorService
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
name|RejectedExecutionException
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
name|RejectedExecutionHandler
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
name|SynchronousQueue
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
name|Cell
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
name|HBaseTestingUtility
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
name|HColumnDescriptor
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
name|KeyValue
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
name|KeyValue
operator|.
name|Type
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
name|LargeTests
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
name|client
operator|.
name|Scan
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|io
operator|.
name|hfile
operator|.
name|HFileContext
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
name|io
operator|.
name|hfile
operator|.
name|HFileContextBuilder
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobFileName
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
name|mob
operator|.
name|MobUtils
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
name|mob
operator|.
name|filecompactions
operator|.
name|MobFileCompactionRequest
operator|.
name|CompactionType
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
name|mob
operator|.
name|filecompactions
operator|.
name|PartitionedMobFileCompactionRequest
operator|.
name|CompactionPartition
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
name|BloomType
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
name|HStore
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
name|ScanInfo
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
name|ScanType
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
name|StoreFile
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
name|StoreFileScanner
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
name|StoreScanner
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestPartitionedMobFileCompactor
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|family
init|=
literal|"family"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|qf
init|=
literal|"qf"
decl_stmt|;
specifier|private
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|mobFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|delFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|allFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|basePath
decl_stmt|;
specifier|private
name|String
name|mobSuffix
decl_stmt|;
specifier|private
name|String
name|delSuffix
decl_stmt|;
specifier|private
specifier|static
name|ExecutorService
name|pool
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.info.port.auto"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|pool
operator|=
name|createThreadPool
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|Exception
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
name|Path
name|testDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|mobTestDir
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
name|MobConstants
operator|.
name|MOB_DIR_NAME
argument_list|)
decl_stmt|;
name|basePath
operator|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|mobTestDir
argument_list|,
name|tableName
argument_list|)
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|mobSuffix
operator|=
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"-"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|delSuffix
operator|=
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"-"
argument_list|,
literal|""
argument_list|)
operator|+
literal|"_del"
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionSelectWithAllFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|resetConf
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
literal|"testCompactionSelectWithAllFiles"
decl_stmt|;
name|init
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|10
decl_stmt|;
comment|// create 10 mob files.
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
name|count
argument_list|,
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
comment|// create 10 del files
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
name|count
argument_list|,
name|Type
operator|.
name|Delete
argument_list|)
expr_stmt|;
name|listFiles
argument_list|()
expr_stmt|;
name|long
name|mergeSize
init|=
name|MobConstants
operator|.
name|DEFAULT_MOB_FILE_COMPACTION_MERGEABLE_THRESHOLD
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expectedStartKeys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|mobFiles
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getLen
argument_list|()
operator|<
name|mergeSize
condition|)
block|{
name|String
name|fileName
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|startKey
init|=
name|fileName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|32
argument_list|)
decl_stmt|;
name|expectedStartKeys
operator|.
name|add
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
block|}
block|}
name|testSelectFiles
argument_list|(
name|tableName
argument_list|,
name|CompactionType
operator|.
name|ALL_FILES
argument_list|,
name|expectedStartKeys
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionSelectWithPartFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|resetConf
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
literal|"testCompactionSelectWithPartFiles"
decl_stmt|;
name|init
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|10
decl_stmt|;
comment|// create 10 mob files.
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
name|count
argument_list|,
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
comment|// create 10 del files
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
name|count
argument_list|,
name|Type
operator|.
name|Delete
argument_list|)
expr_stmt|;
name|listFiles
argument_list|()
expr_stmt|;
name|long
name|mergeSize
init|=
literal|4000
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expectedStartKeys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|mobFiles
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getLen
argument_list|()
operator|<
literal|4000
condition|)
block|{
name|String
name|fileName
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|startKey
init|=
name|fileName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|32
argument_list|)
decl_stmt|;
name|expectedStartKeys
operator|.
name|add
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
block|}
block|}
comment|// set the mob file compaction mergeable threshold
name|conf
operator|.
name|setLong
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_COMPACTION_MERGEABLE_THRESHOLD
argument_list|,
name|mergeSize
argument_list|)
expr_stmt|;
name|testSelectFiles
argument_list|(
name|tableName
argument_list|,
name|CompactionType
operator|.
name|PART_FILES
argument_list|,
name|expectedStartKeys
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactDelFilesWithDefaultBatchSize
parameter_list|()
throws|throws
name|Exception
block|{
name|resetConf
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
literal|"testCompactDelFilesWithDefaultBatchSize"
decl_stmt|;
name|init
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// create 20 mob files.
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
literal|20
argument_list|,
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
comment|// create 13 del files
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
literal|13
argument_list|,
name|Type
operator|.
name|Delete
argument_list|)
expr_stmt|;
name|listFiles
argument_list|()
expr_stmt|;
name|testCompactDelFiles
argument_list|(
name|tableName
argument_list|,
literal|1
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactDelFilesWithSmallBatchSize
parameter_list|()
throws|throws
name|Exception
block|{
name|resetConf
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
literal|"testCompactDelFilesWithSmallBatchSize"
decl_stmt|;
name|init
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// create 20 mob files.
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
literal|20
argument_list|,
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
comment|// create 13 del files
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
literal|13
argument_list|,
name|Type
operator|.
name|Delete
argument_list|)
expr_stmt|;
name|listFiles
argument_list|()
expr_stmt|;
comment|// set the mob file compaction batch size
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_COMPACTION_BATCH_SIZE
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|testCompactDelFiles
argument_list|(
name|tableName
argument_list|,
literal|1
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactDelFilesChangeMaxDelFileCount
parameter_list|()
throws|throws
name|Exception
block|{
name|resetConf
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
literal|"testCompactDelFilesWithSmallBatchSize"
decl_stmt|;
name|init
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// create 20 mob files.
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
literal|20
argument_list|,
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
comment|// create 13 del files
name|createStoreFiles
argument_list|(
name|basePath
argument_list|,
name|family
argument_list|,
name|qf
argument_list|,
literal|13
argument_list|,
name|Type
operator|.
name|Delete
argument_list|)
expr_stmt|;
name|listFiles
argument_list|()
expr_stmt|;
comment|// set the max del file count
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_DELFILE_MAX_COUNT
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// set the mob file compaction batch size
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_COMPACTION_BATCH_SIZE
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|testCompactDelFiles
argument_list|(
name|tableName
argument_list|,
literal|4
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the selectFiles    * @param tableName the table name    * @param type the expected compaction type    * @param expected the expected start keys    */
specifier|private
name|void
name|testSelectFiles
parameter_list|(
name|String
name|tableName
parameter_list|,
specifier|final
name|CompactionType
name|type
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|PartitionedMobFileCompactor
name|compactor
init|=
operator|new
name|PartitionedMobFileCompactor
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|,
name|pool
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|files
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|PartitionedMobFileCompactionRequest
name|request
init|=
name|select
argument_list|(
name|files
argument_list|)
decl_stmt|;
comment|// assert the compaction type is ALL_FILES
name|Assert
operator|.
name|assertEquals
argument_list|(
name|type
argument_list|,
name|request
operator|.
name|type
argument_list|)
expr_stmt|;
comment|// assert get the right partitions
name|compareCompactedPartitions
argument_list|(
name|expected
argument_list|,
name|request
operator|.
name|compactionPartitions
argument_list|)
expr_stmt|;
comment|// assert get the right del files
name|compareDelFiles
argument_list|(
name|request
operator|.
name|delFiles
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|compactor
operator|.
name|compact
argument_list|(
name|allFiles
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the compacteDelFile    * @param tableName the table name    * @param expectedFileCount the expected file count    * @param expectedCellCount the expected cell count    */
specifier|private
name|void
name|testCompactDelFiles
parameter_list|(
name|String
name|tableName
parameter_list|,
specifier|final
name|int
name|expectedFileCount
parameter_list|,
specifier|final
name|int
name|expectedCellCount
parameter_list|)
throws|throws
name|IOException
block|{
name|PartitionedMobFileCompactor
name|compactor
init|=
operator|new
name|PartitionedMobFileCompactor
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|,
name|pool
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Path
argument_list|>
name|performCompaction
parameter_list|(
name|PartitionedMobFileCompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|delFilePaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|delFile
range|:
name|request
operator|.
name|delFiles
control|)
block|{
name|delFilePaths
operator|.
name|add
argument_list|(
name|delFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|newDelPaths
init|=
name|compactDelFiles
argument_list|(
name|request
argument_list|,
name|delFilePaths
argument_list|)
decl_stmt|;
comment|// assert the del files are merged.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedFileCount
argument_list|,
name|newDelPaths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedCellCount
argument_list|,
name|countDelCellsInDelFiles
argument_list|(
name|newDelPaths
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|compactor
operator|.
name|compact
argument_list|(
name|allFiles
argument_list|)
expr_stmt|;
block|}
comment|/**    * Lists the files in the path    */
specifier|private
name|void
name|listFiles
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|basePath
argument_list|)
control|)
block|{
name|allFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
if|if
condition|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"_del"
argument_list|)
condition|)
block|{
name|delFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mobFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Compares the compacted partitions.    * @param partitions the collection of CompactedPartitions    */
specifier|private
name|void
name|compareCompactedPartitions
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|,
name|Collection
argument_list|<
name|CompactionPartition
argument_list|>
name|partitions
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|actualKeys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|CompactionPartition
name|partition
range|:
name|partitions
control|)
block|{
name|actualKeys
operator|.
name|add
argument_list|(
name|partition
operator|.
name|getPartitionId
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|actualKeys
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|actualKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actualKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Compares the del files.    * @param allDelFiles all the del files    */
specifier|private
name|void
name|compareDelFiles
parameter_list|(
name|Collection
argument_list|<
name|FileStatus
argument_list|>
name|allDelFiles
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|allDelFiles
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|delFiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|file
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
comment|/**    * Creates store files.    * @param basePath the path to create file    * @family the family name    * @qualifier the column qualifier    * @count the store file number    * @type the key type    */
specifier|private
name|void
name|createStoreFiles
parameter_list|(
name|Path
name|basePath
parameter_list|,
name|String
name|family
parameter_list|,
name|String
name|qualifier
parameter_list|,
name|int
name|count
parameter_list|,
name|Type
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|8
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|startKey
init|=
literal|"row_"
decl_stmt|;
name|MobFileName
name|mobFileName
init|=
literal|null
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|startRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startKey
operator|+
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|Type
operator|.
name|Delete
argument_list|)
condition|)
block|{
name|mobFileName
operator|=
name|MobFileName
operator|.
name|create
argument_list|(
name|startRow
argument_list|,
name|MobUtils
operator|.
name|formatDate
argument_list|(
operator|new
name|Date
argument_list|()
argument_list|)
argument_list|,
name|delSuffix
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|Type
operator|.
name|Put
argument_list|)
condition|)
block|{
name|mobFileName
operator|=
name|MobFileName
operator|.
name|create
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startKey
operator|+
name|i
argument_list|)
argument_list|,
name|MobUtils
operator|.
name|formatDate
argument_list|(
operator|new
name|Date
argument_list|()
argument_list|)
argument_list|,
name|mobSuffix
argument_list|)
expr_stmt|;
block|}
name|StoreFile
operator|.
name|Writer
name|mobFileWriter
init|=
operator|new
name|StoreFile
operator|.
name|WriterBuilder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|withFilePath
argument_list|(
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
name|mobFileName
operator|.
name|getFileName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|writeStoreFile
argument_list|(
name|mobFileWriter
argument_list|,
name|startRow
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualifier
argument_list|)
argument_list|,
name|type
argument_list|,
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes data to store file.    * @param writer the store file writer    * @param row the row key    * @param family the family name    * @param qualifier the column qualifier    * @param type the key type    * @param size the size of value    */
specifier|private
specifier|static
name|void
name|writeStoreFile
parameter_list|(
specifier|final
name|StoreFile
operator|.
name|Writer
name|writer
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|Type
name|type
parameter_list|,
name|int
name|size
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|dummyData
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
operator|new
name|Random
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|dummyData
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|type
argument_list|,
name|dummyData
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Gets the number of del cell in the del files    * @param paths the del file paths    * @return the cell size    */
specifier|private
name|int
name|countDelCellsInDelFiles
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|paths
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|sfs
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
name|StoreFile
name|sf
init|=
operator|new
name|StoreFile
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|)
decl_stmt|;
name|sfs
operator|.
name|add
argument_list|(
name|sf
argument_list|)
expr_stmt|;
block|}
name|List
name|scanners
init|=
name|StoreFileScanner
operator|.
name|getScannersForStoreFiles
argument_list|(
name|sfs
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|hcd
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|timeToPurgeDeletes
init|=
name|Math
operator|.
name|max
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.hstore.time.to.purge.deletes"
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|long
name|ttl
init|=
name|HStore
operator|.
name|determineTTLFromFamily
argument_list|(
name|hcd
argument_list|)
decl_stmt|;
name|ScanInfo
name|scanInfo
init|=
operator|new
name|ScanInfo
argument_list|(
name|hcd
argument_list|,
name|ttl
argument_list|,
name|timeToPurgeDeletes
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|StoreScanner
name|scanner
init|=
operator|new
name|StoreScanner
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
literal|null
argument_list|,
name|scanners
argument_list|,
literal|0L
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|hasMore
condition|)
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|size
operator|+=
name|results
operator|.
name|size
argument_list|()
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|size
return|;
block|}
specifier|private
specifier|static
name|ExecutorService
name|createThreadPool
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|maxThreads
init|=
literal|10
decl_stmt|;
name|long
name|keepAliveTime
init|=
literal|60
decl_stmt|;
specifier|final
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
name|queue
init|=
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|maxThreads
argument_list|,
name|keepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|queue
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"MobFileCompactionChore"
argument_list|)
argument_list|,
operator|new
name|RejectedExecutionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|rejectedExecution
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|ThreadPoolExecutor
name|executor
parameter_list|)
block|{
try|try
block|{
comment|// waiting for a thread to pick up instead of throwing exceptions.
name|queue
operator|.
name|put
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RejectedExecutionException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|pool
operator|)
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|pool
return|;
block|}
comment|/**    * Resets the configuration.    */
specifier|private
name|void
name|resetConf
parameter_list|()
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_COMPACTION_MERGEABLE_THRESHOLD
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_FILE_COMPACTION_MERGEABLE_THRESHOLD
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_DELFILE_MAX_COUNT
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_DELFILE_MAX_COUNT
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_COMPACTION_BATCH_SIZE
argument_list|,
name|MobConstants
operator|.
name|DEFAULT_MOB_FILE_COMPACTION_BATCH_SIZE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

