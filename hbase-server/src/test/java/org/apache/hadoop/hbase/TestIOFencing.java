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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|List
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
name|CountDownLatch
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
name|AtomicInteger
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
name|hbase
operator|.
name|client
operator|.
name|Admin
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
name|RegionInfo
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
name|regionserver
operator|.
name|CompactingMemStore
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
name|ConstantSizeRegionSplitPolicy
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
name|HRegion
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
name|HRegionServer
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
name|HStoreFile
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
name|Region
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
name|RegionServerServices
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
name|Store
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
name|compactions
operator|.
name|CompactionContext
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
name|throttle
operator|.
name|ThroughputController
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
name|WALUtil
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
name|testclassification
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
name|testclassification
operator|.
name|MiscTests
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|wal
operator|.
name|WAL
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
name|Lists
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|CompactionDescriptor
import|;
end_import

begin_comment
comment|/**  * Test for the case where a regionserver going down has enough cycles to do damage to regions that  * have actually been assigned elsehwere.  *<p>  * If we happen to assign a region before it fully done with in its old location -- i.e. it is on  * two servers at the same time -- all can work fine until the case where the region on the dying  * server decides to compact or otherwise change the region file set. The region in its new location  * will then get a surprise when it tries to do something w/ a file removed by the region in its old  * location on dying server.  *<p>  * Making a test for this case is a little tough in that even if a file is deleted up on the  * namenode, if the file was opened before the delete, it will continue to let reads happen until  * something changes the state of cached blocks in the dfsclient that was already open (a block from  * the deleted file is cleaned from the datanode by NN).  *<p>  * What we will do below is do an explicit check for existence on the files listed in the region  * that has had some files removed because of a compaction. This sort of hurry's along and makes  * certain what is a chance occurance.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestIOFencing
block|{
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
name|TestIOFencing
operator|.
name|class
argument_list|)
decl_stmt|;
static|static
block|{
comment|// Uncomment the following lines if more verbosity is needed for
comment|// debugging (see HBASE-12285 for details).
comment|//((Log4JLogger)FSNamesystem.LOG).getLogger().setLevel(Level.ALL);
comment|//((Log4JLogger)DataNode.LOG).getLogger().setLevel(Level.ALL);
comment|//((Log4JLogger)LeaseManager.LOG).getLogger().setLevel(Level.ALL);
comment|//((Log4JLogger)LogFactory.getLog("org.apache.hadoop.hdfs.server.namenode.FSNamesystem"))
comment|//    .getLogger().setLevel(Level.ALL);
comment|//((Log4JLogger)DFSClient.LOG).getLogger().setLevel(Level.ALL);
block|}
specifier|public
specifier|abstract
specifier|static
class|class
name|CompactionBlockerRegion
extends|extends
name|HRegion
block|{
name|AtomicInteger
name|compactCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|volatile
name|CountDownLatch
name|compactionsBlocked
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|volatile
name|CountDownLatch
name|compactionsWaiting
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|0
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|CompactionBlockerRegion
parameter_list|(
name|Path
name|tableDir
parameter_list|,
name|WAL
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|confParam
parameter_list|,
name|RegionInfo
name|info
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|super
argument_list|(
name|tableDir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|confParam
argument_list|,
name|info
argument_list|,
name|htd
argument_list|,
name|rsServices
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stopCompactions
parameter_list|()
block|{
name|compactionsBlocked
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|compactionsWaiting
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|allowCompactions
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"allowing compactions"
argument_list|)
expr_stmt|;
name|compactionsBlocked
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|waitForCompactionToBlock
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"waiting for compaction to block"
argument_list|)
expr_stmt|;
name|compactionsWaiting
operator|.
name|await
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"compaction block reached"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|,
name|HStore
name|store
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|super
operator|.
name|compact
argument_list|(
name|compaction
argument_list|,
name|store
argument_list|,
name|throughputController
argument_list|)
return|;
block|}
finally|finally
block|{
name|compactCount
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|,
name|HStore
name|store
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|super
operator|.
name|compact
argument_list|(
name|compaction
argument_list|,
name|store
argument_list|,
name|throughputController
argument_list|,
name|user
argument_list|)
return|;
block|}
finally|finally
block|{
name|compactCount
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|countStoreFiles
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HStore
name|store
range|:
name|stores
operator|.
name|values
argument_list|()
control|)
block|{
name|count
operator|+=
name|store
operator|.
name|getStorefilesCount
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
block|}
comment|/**    * An override of HRegion that allows us park compactions in a holding pattern and    * then when appropriate for the test, allow them proceed again.    */
specifier|public
specifier|static
class|class
name|BlockCompactionsInPrepRegion
extends|extends
name|CompactionBlockerRegion
block|{
specifier|public
name|BlockCompactionsInPrepRegion
parameter_list|(
name|Path
name|tableDir
parameter_list|,
name|WAL
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|confParam
parameter_list|,
name|RegionInfo
name|info
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|super
argument_list|(
name|tableDir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|confParam
argument_list|,
name|info
argument_list|,
name|htd
argument_list|,
name|rsServices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doRegionCompactionPrep
parameter_list|()
throws|throws
name|IOException
block|{
name|compactionsWaiting
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|compactionsBlocked
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|()
throw|;
block|}
name|super
operator|.
name|doRegionCompactionPrep
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * An override of HRegion that allows us park compactions in a holding pattern and    * then when appropriate for the test, allow them proceed again. This allows the compaction    * entry to go the WAL before blocking, but blocks afterwards    */
specifier|public
specifier|static
class|class
name|BlockCompactionsInCompletionRegion
extends|extends
name|CompactionBlockerRegion
block|{
specifier|public
name|BlockCompactionsInCompletionRegion
parameter_list|(
name|Path
name|tableDir
parameter_list|,
name|WAL
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|confParam
parameter_list|,
name|RegionInfo
name|info
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|super
argument_list|(
name|tableDir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|confParam
argument_list|,
name|info
argument_list|,
name|htd
argument_list|,
name|rsServices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|HStore
name|instantiateHStore
parameter_list|(
specifier|final
name|ColumnFamilyDescriptor
name|family
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|BlockCompactionsInCompletionHStore
argument_list|(
name|this
argument_list|,
name|family
argument_list|,
name|this
operator|.
name|conf
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|BlockCompactionsInCompletionHStore
extends|extends
name|HStore
block|{
name|CompactionBlockerRegion
name|r
decl_stmt|;
specifier|protected
name|BlockCompactionsInCompletionHStore
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|ColumnFamilyDescriptor
name|family
parameter_list|,
name|Configuration
name|confParam
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|region
argument_list|,
name|family
argument_list|,
name|confParam
argument_list|)
expr_stmt|;
name|r
operator|=
operator|(
name|CompactionBlockerRegion
operator|)
name|region
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|completeCompaction
parameter_list|(
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|compactedFiles
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|r
operator|.
name|compactionsWaiting
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|r
operator|.
name|compactionsBlocked
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
name|super
operator|.
name|completeCompaction
argument_list|(
name|compactedFiles
argument_list|)
expr_stmt|;
block|}
block|}
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
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tabletest"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
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
specifier|final
name|int
name|FIRST_BATCH_COUNT
init|=
literal|4000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SECOND_BATCH_COUNT
init|=
name|FIRST_BATCH_COUNT
decl_stmt|;
comment|/**    * Test that puts up a regionserver, starts a compaction on a loaded region but holds the    * compaction until after we have killed the server and the region has come up on    * a new regionserver altogether.  This fakes the double assignment case where region in one    * location changes the files out from underneath a region being served elsewhere.    */
annotation|@
name|Test
specifier|public
name|void
name|testFencingAroundCompaction
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|MemoryCompactionPolicy
name|policy
range|:
name|MemoryCompactionPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|doTest
argument_list|(
name|BlockCompactionsInPrepRegion
operator|.
name|class
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test that puts up a regionserver, starts a compaction on a loaded region but holds the    * compaction completion until after we have killed the server and the region has come up on    * a new regionserver altogether.  This fakes the double assignment case where region in one    * location changes the files out from underneath a region being served elsewhere.    */
annotation|@
name|Test
specifier|public
name|void
name|testFencingAroundCompactionAfterWALSync
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|MemoryCompactionPolicy
name|policy
range|:
name|MemoryCompactionPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|doTest
argument_list|(
name|BlockCompactionsInCompletionRegion
operator|.
name|class
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|doTest
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|regionClass
parameter_list|,
name|MemoryCompactionPolicy
name|policy
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|c
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Insert our custom region
name|c
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|REGION_IMPL
argument_list|,
name|regionClass
argument_list|,
name|HRegion
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Encourage plenty of flushes
name|c
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|ConstantSizeRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Only run compaction when we tell it to
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|c
operator|.
name|setLong
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Compact quickly after we tell it to!
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.thread.splitcompactcheckfrequency"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_TYPE_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|policy
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting mini cluster"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|CompactionBlockerRegion
name|compactingRegion
init|=
literal|null
decl_stmt|;
name|Admin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating admin"
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Loading test table"
argument_list|)
expr_stmt|;
comment|// Find the region
name|List
argument_list|<
name|HRegion
argument_list|>
name|testRegions
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|findRegionsForTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|testRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|compactingRegion
operator|=
operator|(
name|CompactionBlockerRegion
operator|)
name|testRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Blocking compactions"
argument_list|)
expr_stmt|;
name|compactingRegion
operator|.
name|stopCompactions
argument_list|()
expr_stmt|;
name|long
name|lastFlushTime
init|=
name|compactingRegion
operator|.
name|getEarliestFlushTimeForAllStores
argument_list|()
decl_stmt|;
comment|// Load some rows
name|TEST_UTIL
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|,
literal|0
argument_list|,
name|FIRST_BATCH_COUNT
argument_list|)
expr_stmt|;
comment|// add a compaction from an older (non-existing) region to see whether we successfully skip
comment|// those entries
name|HRegionInfo
name|oldHri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|CompactionDescriptor
name|compactionDescriptor
init|=
name|ProtobufUtil
operator|.
name|toCompactionDescriptor
argument_list|(
name|oldHri
argument_list|,
name|FAMILY
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/a"
argument_list|)
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/b"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"store_dir"
argument_list|)
argument_list|)
decl_stmt|;
name|WALUtil
operator|.
name|writeCompactionMarker
argument_list|(
name|compactingRegion
operator|.
name|getWAL
argument_list|()
argument_list|,
operator|(
operator|(
name|HRegion
operator|)
name|compactingRegion
operator|)
operator|.
name|getReplicationScope
argument_list|()
argument_list|,
name|oldHri
argument_list|,
name|compactionDescriptor
argument_list|,
name|compactingRegion
operator|.
name|getMVCC
argument_list|()
argument_list|)
expr_stmt|;
comment|// Wait till flush has happened, otherwise there won't be multiple store files
name|long
name|startWaitTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|compactingRegion
operator|.
name|getEarliestFlushTimeForAllStores
argument_list|()
operator|<=
name|lastFlushTime
operator|||
name|compactingRegion
operator|.
name|countStoreFiles
argument_list|()
operator|<=
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for the region to flush "
operator|+
name|compactingRegion
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Timed out waiting for the region to flush"
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startWaitTime
operator|<
literal|30000
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|compactingRegion
operator|.
name|countStoreFiles
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|byte
name|REGION_NAME
index|[]
init|=
name|compactingRegion
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Asking for compaction"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for compaction to be about to start"
argument_list|)
expr_stmt|;
name|compactingRegion
operator|.
name|waitForCompactionToBlock
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting a new server"
argument_list|)
expr_stmt|;
name|RegionServerThread
name|newServerThread
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
decl_stmt|;
specifier|final
name|HRegionServer
name|newServer
init|=
name|newServerThread
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing region server ZK lease"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|expireRegionServerSession
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|CompactionBlockerRegion
name|newRegion
init|=
literal|null
decl_stmt|;
name|startWaitTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for the new server to pick up the region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|REGION_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait for region to be assigned and to go out of log replay if applicable
name|Waiter
operator|.
name|waitFor
argument_list|(
name|c
argument_list|,
literal|60000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|Region
name|newRegion
init|=
name|newServer
operator|.
name|getOnlineRegion
argument_list|(
name|REGION_NAME
argument_list|)
decl_stmt|;
return|return
name|newRegion
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|newRegion
operator|=
operator|(
name|CompactionBlockerRegion
operator|)
name|newServer
operator|.
name|getOnlineRegion
argument_list|(
name|REGION_NAME
argument_list|)
expr_stmt|;
comment|// After compaction of old region finishes on the server that was going down, make sure that
comment|// all the files we expect are still working when region is up in new location.
name|FileSystem
name|fs
init|=
name|newRegion
operator|.
name|getFilesystem
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|f
range|:
name|newRegion
operator|.
name|getStoreFileList
argument_list|(
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
literal|"After compaction, does not exist: "
operator|+
name|f
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|f
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Allowing compaction to proceed"
argument_list|)
expr_stmt|;
name|compactingRegion
operator|.
name|allowCompactions
argument_list|()
expr_stmt|;
while|while
condition|(
name|compactingRegion
operator|.
name|compactCount
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|// The server we killed stays up until the compaction that was started before it was killed
comment|// completes. In logs you should see the old regionserver now going down.
name|LOG
operator|.
name|info
argument_list|(
literal|"Compaction finished"
argument_list|)
expr_stmt|;
comment|// If we survive the split keep going...
comment|// Now we make sure that the region isn't totally confused.  Load up more rows.
name|TEST_UTIL
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|,
name|FIRST_BATCH_COUNT
argument_list|,
name|FIRST_BATCH_COUNT
operator|+
name|SECOND_BATCH_COUNT
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|startWaitTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
while|while
condition|(
name|newRegion
operator|.
name|compactCount
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"New region never compacted"
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startWaitTime
operator|<
literal|180000
argument_list|)
expr_stmt|;
block|}
name|int
name|count
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|count
operator|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
comment|// wait up to 30s
if|if
condition|(
name|i
operator|>=
literal|30
operator|||
operator|!
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"File does not exist"
argument_list|)
condition|)
block|{
throw|throw
name|e
throw|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|policy
operator|==
name|MemoryCompactionPolicy
operator|.
name|EAGER
operator|||
name|policy
operator|==
name|MemoryCompactionPolicy
operator|.
name|ADAPTIVE
condition|)
block|{
name|assertTrue
argument_list|(
name|FIRST_BATCH_COUNT
operator|+
name|SECOND_BATCH_COUNT
operator|>=
name|count
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|FIRST_BATCH_COUNT
operator|+
name|SECOND_BATCH_COUNT
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|compactingRegion
operator|!=
literal|null
condition|)
block|{
name|compactingRegion
operator|.
name|allowCompactions
argument_list|()
expr_stmt|;
block|}
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

