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
name|regionserver
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
name|nio
operator|.
name|ByteBuffer
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
name|Map
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
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
name|DoNotRetryIOException
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
name|HBaseClassTestRule
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
name|AsyncClusterConnection
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
name|ColumnFamilyDescriptor
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
name|Get
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
name|Put
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
name|Result
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
name|client
operator|.
name|TableDescriptor
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
name|ByteBuffAllocator
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
name|compress
operator|.
name|Compression
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
name|crypto
operator|.
name|Encryption
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
name|HFile
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|RegionServerTests
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
name|tool
operator|.
name|BulkLoadHFilesTool
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
name|EnvironmentEdgeManager
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
name|ClassRule
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSecureBulkLoadManager
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestSecureBulkLoadManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestSecureBulkLoadManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestSecureBulkLoadManager"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|key1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|key2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|key3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|value3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|SPLIT_ROWKEY
init|=
name|key2
decl_stmt|;
specifier|private
name|Thread
name|ealierBulkload
decl_stmt|;
specifier|private
name|Thread
name|laterBulkload
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
name|testUtil
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|testUtil
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|testUtil
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|testUtil
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
comment|/**    * After a secure bulkload finished , there is a clean-up for FileSystems used in the bulkload.    * Sometimes, FileSystems used in the finished bulkload might also be used in other bulkload    * calls, or there are other FileSystems created by the same user, they could be closed by a    * FileSystem.closeAllForUGI call. So during the clean-up, those FileSystems need to be used    * later can not get closed ,or else a race condition occurs.    *    * testForRaceCondition tests the case that two secure bulkload calls from the same UGI go    * into two different regions and one bulkload finishes earlier when the other bulkload still    * needs its FileSystems, checks that both bulkloads succeed.    */
annotation|@
name|Test
specifier|public
name|void
name|testForRaceCondition
parameter_list|()
throws|throws
name|Exception
block|{
name|Consumer
argument_list|<
name|HRegion
argument_list|>
name|fsCreatedListener
init|=
operator|new
name|Consumer
argument_list|<
name|HRegion
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|accept
parameter_list|(
name|HRegion
name|hRegion
parameter_list|)
block|{
if|if
condition|(
name|hRegion
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|containsRow
argument_list|(
name|key3
argument_list|)
condition|)
block|{
name|Threads
operator|.
name|shutdown
argument_list|(
name|ealierBulkload
argument_list|)
expr_stmt|;
comment|/// wait util the other bulkload finished
block|}
block|}
block|}
decl_stmt|;
name|testUtil
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getSecureBulkLoadManager
argument_list|()
operator|.
name|setFsCreatedListener
argument_list|(
name|fsCreatedListener
argument_list|)
expr_stmt|;
comment|/// create table
name|testUtil
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|,
name|Bytes
operator|.
name|toByteArrays
argument_list|(
name|SPLIT_ROWKEY
argument_list|)
argument_list|)
expr_stmt|;
comment|/// prepare files
name|Path
name|rootdir
init|=
name|testUtil
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getDataRootDir
argument_list|()
decl_stmt|;
name|Path
name|dir1
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
literal|"dir1"
argument_list|)
decl_stmt|;
name|prepareHFile
argument_list|(
name|dir1
argument_list|,
name|key1
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|Path
name|dir2
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
literal|"dir2"
argument_list|)
decl_stmt|;
name|prepareHFile
argument_list|(
name|dir2
argument_list|,
name|key3
argument_list|,
name|value3
argument_list|)
expr_stmt|;
comment|/// do bulkload
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|t1Exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|t2Exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ealierBulkload
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|doBulkloadWithoutRetry
argument_list|(
name|dir1
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
literal|"bulk load failed ."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|t1Exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|laterBulkload
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|doBulkloadWithoutRetry
argument_list|(
name|dir2
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
literal|"bulk load failed ."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|t2Exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|ealierBulkload
operator|.
name|start
argument_list|()
expr_stmt|;
name|laterBulkload
operator|.
name|start
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|ealierBulkload
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|laterBulkload
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|t1Exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|t2Exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|/// check bulkload ok
name|Get
name|get1
init|=
operator|new
name|Get
argument_list|(
name|key1
argument_list|)
decl_stmt|;
name|Get
name|get3
init|=
operator|new
name|Get
argument_list|(
name|key3
argument_list|)
decl_stmt|;
name|Table
name|t
init|=
name|testUtil
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|t
operator|.
name|get
argument_list|(
name|get1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|)
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|get3
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|)
argument_list|,
name|value3
argument_list|)
expr_stmt|;
block|}
comment|/**    * A trick is used to make sure server-side failures( if any ) not being covered up by a client    * retry. Since BulkLoadHFilesTool.bulkLoad keeps performing bulkload calls as long as the    * HFile queue is not empty, while server-side exceptions in the doAs block do not lead    * to a client exception, a bulkload will always succeed in this case by default, thus client    * will never be aware that failures have ever happened . To avoid this kind of retry ,    * a MyExceptionToAvoidRetry exception is thrown after bulkLoadPhase finished and caught    * silently outside the doBulkLoad call, so that the bulkLoadPhase would be called exactly    * once, and server-side failures, if any ,can be checked via data.    */
class|class
name|MyExceptionToAvoidRetry
extends|extends
name|DoNotRetryIOException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|6802760664998771151L
decl_stmt|;
block|}
specifier|private
name|void
name|doBulkloadWithoutRetry
parameter_list|(
name|Path
name|dir
parameter_list|)
throws|throws
name|Exception
block|{
name|BulkLoadHFilesTool
name|h
init|=
operator|new
name|BulkLoadHFilesTool
argument_list|(
name|conf
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|bulkLoadPhase
parameter_list|(
name|AsyncClusterConnection
name|conn
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Deque
argument_list|<
name|LoadQueueItem
argument_list|>
name|queue
parameter_list|,
name|Multimap
argument_list|<
name|ByteBuffer
argument_list|,
name|LoadQueueItem
argument_list|>
name|regionGroups
parameter_list|,
name|boolean
name|copyFiles
parameter_list|,
name|Map
argument_list|<
name|LoadQueueItem
argument_list|,
name|ByteBuffer
argument_list|>
name|item2RegionMap
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|bulkLoadPhase
argument_list|(
name|conn
argument_list|,
name|tableName
argument_list|,
name|queue
argument_list|,
name|regionGroups
argument_list|,
name|copyFiles
argument_list|,
name|item2RegionMap
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MyExceptionToAvoidRetry
argument_list|()
throw|;
comment|// throw exception to avoid retry
block|}
block|}
decl_stmt|;
try|try
block|{
name|h
operator|.
name|bulkLoad
argument_list|(
name|TABLE
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"MyExceptionToAvoidRetry is expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MyExceptionToAvoidRetry
name|e
parameter_list|)
block|{
comment|//expected
block|}
block|}
specifier|private
name|void
name|prepareHFile
parameter_list|(
name|Path
name|dir
parameter_list|,
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|TableDescriptor
name|desc
init|=
name|testUtil
operator|.
name|getAdmin
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|family
init|=
name|desc
operator|.
name|getColumnFamily
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|Compression
operator|.
name|Algorithm
name|compression
init|=
name|HFile
operator|.
name|DEFAULT_COMPRESSION_ALGORITHM
decl_stmt|;
name|CacheConfig
name|writerCacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
decl_stmt|;
name|writerCacheConf
operator|.
name|setCacheDataOnWrite
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|HFileContext
name|hFileContext
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withIncludesMvcc
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
literal|true
argument_list|)
operator|.
name|withCompression
argument_list|(
name|compression
argument_list|)
operator|.
name|withCompressTags
argument_list|(
name|family
operator|.
name|isCompressTags
argument_list|()
argument_list|)
operator|.
name|withChecksumType
argument_list|(
name|HStore
operator|.
name|getChecksumType
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|withBytesPerCheckSum
argument_list|(
name|HStore
operator|.
name|getBytesPerChecksum
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|withBlockSize
argument_list|(
name|family
operator|.
name|getBlocksize
argument_list|()
argument_list|)
operator|.
name|withHBaseCheckSum
argument_list|(
literal|true
argument_list|)
operator|.
name|withDataBlockEncoding
argument_list|(
name|family
operator|.
name|getDataBlockEncoding
argument_list|()
argument_list|)
operator|.
name|withEncryptionContext
argument_list|(
name|Encryption
operator|.
name|Context
operator|.
name|NONE
argument_list|)
operator|.
name|withCreateTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
operator|.
name|Builder
name|builder
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|writerCacheConf
argument_list|,
name|dir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|withOutputDir
argument_list|(
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|family
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|withBloomType
argument_list|(
name|family
operator|.
name|getBloomFilterType
argument_list|()
argument_list|)
operator|.
name|withMaxKeyCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|hFileContext
argument_list|)
decl_stmt|;
name|StoreFileWriter
name|writer
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|,
name|value
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|c
range|:
name|put
operator|.
name|get
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|)
control|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

