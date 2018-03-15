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
name|master
operator|.
name|cleaner
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|Set
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
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|master
operator|.
name|HMaster
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
name|master
operator|.
name|snapshot
operator|.
name|DisabledTableSnapshotHandler
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
name|master
operator|.
name|snapshot
operator|.
name|SnapshotHFileCleaner
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
name|master
operator|.
name|snapshot
operator|.
name|SnapshotManager
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
name|CompactedHFilesDischarger
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotReferenceUtil
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|snapshot
operator|.
name|UnknownSnapshotException
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
name|MasterTests
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Before
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
name|mockito
operator|.
name|Mockito
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
name|generated
operator|.
name|MasterProtos
operator|.
name|DeleteSnapshotRequest
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
name|MasterProtos
operator|.
name|GetCompletedSnapshotsRequest
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
name|MasterProtos
operator|.
name|GetCompletedSnapshotsResponse
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
name|MasterProtos
operator|.
name|IsSnapshotDoneRequest
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
name|MasterProtos
operator|.
name|IsSnapshotDoneResponse
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
name|SnapshotProtos
operator|.
name|SnapshotDescription
import|;
end_import

begin_comment
comment|/**  * Test the master-related aspects of a snapshot  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestSnapshotFromMaster
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
name|TestSnapshotFromMaster
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
name|TestSnapshotFromMaster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootDir
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|HMaster
name|master
decl_stmt|;
comment|// for hfile archiving test.
specifier|private
specifier|static
name|Path
name|archiveDir
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
comment|// refresh the cache every 1/2 second
specifier|private
specifier|static
specifier|final
name|long
name|cacheRefreshPeriod
init|=
literal|500
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|blockingStoreFiles
init|=
literal|12
decl_stmt|;
comment|/**    * Setup the config for the cluster    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_RS
argument_list|)
expr_stmt|;
name|fs
operator|=
name|UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|master
operator|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|rootDir
operator|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
expr_stmt|;
name|archiveDir
operator|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// disable the ui
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionsever.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// change the flush size to a small amount, regulating number of store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// so make sure we get a compaction when doing a load, but keep around some
comment|// files in the store
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// block writes if we get to 12 store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
name|blockingStoreFiles
argument_list|)
expr_stmt|;
comment|// Ensure no extra cleaners on by default (e.g. TimeToLiveHFileCleaner)
name|conf
operator|.
name|set
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_LOGCLEANER_PLUGINS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
comment|// Enable snapshot
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|SnapshotHFileCleaner
operator|.
name|HFILE_CACHE_REFRESH_PERIOD_CONF_KEY
argument_list|,
name|cacheRefreshPeriod
argument_list|)
expr_stmt|;
name|conf
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hfile.compactions.cleaner.interval"
argument_list|,
literal|20
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|master
operator|.
name|getSnapshotManager
argument_list|()
operator|.
name|setSnapshotHandlerForTesting
argument_list|(
name|TABLE_NAME
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|UTIL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// NOOP;
block|}
block|}
comment|/**    * Test that the contract from the master for checking on a snapshot are valid.    *<p>    *<ol>    *<li>If a snapshot fails with an error, we expect to get the source error.</li>    *<li>If there is no snapshot name supplied, we should get an error.</li>    *<li>If asking about a snapshot has hasn't occurred, you should get an error.</li>    *</ol>    */
annotation|@
name|Test
specifier|public
name|void
name|testIsDoneContract
parameter_list|()
throws|throws
name|Exception
block|{
name|IsSnapshotDoneRequest
operator|.
name|Builder
name|builder
init|=
name|IsSnapshotDoneRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|String
name|snapshotName
init|=
literal|"asyncExpectedFailureTest"
decl_stmt|;
comment|// check that we get an exception when looking up snapshot where one hasn't happened
name|SnapshotTestingUtils
operator|.
name|expectSnapshotDoneException
argument_list|(
name|master
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|UnknownSnapshotException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// and that we get the same issue, even if we specify a name
name|SnapshotDescription
name|desc
init|=
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|snapshotName
argument_list|)
operator|.
name|setTable
argument_list|(
name|TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setSnapshot
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|expectSnapshotDoneException
argument_list|(
name|master
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|UnknownSnapshotException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// set a mock handler to simulate a snapshot
name|DisabledTableSnapshotHandler
name|mockHandler
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|DisabledTableSnapshotHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockHandler
operator|.
name|getException
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockHandler
operator|.
name|getSnapshot
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockHandler
operator|.
name|isFinished
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockHandler
operator|.
name|getCompletionTimestamp
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|getSnapshotManager
argument_list|()
operator|.
name|setSnapshotHandlerForTesting
argument_list|(
name|TABLE_NAME
argument_list|,
name|mockHandler
argument_list|)
expr_stmt|;
comment|// if we do a lookup without a snapshot name, we should fail - you should always know your name
name|builder
operator|=
name|IsSnapshotDoneRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|expectSnapshotDoneException
argument_list|(
name|master
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|UnknownSnapshotException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// then do the lookup for the snapshot that it is done
name|builder
operator|.
name|setSnapshot
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|IsSnapshotDoneResponse
name|response
init|=
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|isSnapshotDone
argument_list|(
literal|null
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Snapshot didn't complete when it should have."
argument_list|,
name|response
operator|.
name|getDone
argument_list|()
argument_list|)
expr_stmt|;
comment|// now try the case where we are looking for a snapshot we didn't take
name|builder
operator|.
name|setSnapshot
argument_list|(
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"Not A Snapshot"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|expectSnapshotDoneException
argument_list|(
name|master
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|UnknownSnapshotException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// then create a snapshot to the fs and make sure that we can find it when checking done
name|snapshotName
operator|=
literal|"completed"
expr_stmt|;
name|desc
operator|=
name|createSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setSnapshot
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|response
operator|=
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|isSnapshotDone
argument_list|(
literal|null
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Completed, on-disk snapshot not found"
argument_list|,
name|response
operator|.
name|getDone
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetCompletedSnapshots
parameter_list|()
throws|throws
name|Exception
block|{
comment|// first check when there are no snapshots
name|GetCompletedSnapshotsRequest
name|request
init|=
name|GetCompletedSnapshotsRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|GetCompletedSnapshotsResponse
name|response
init|=
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|getCompletedSnapshots
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Found unexpected number of snapshots"
argument_list|,
literal|0
argument_list|,
name|response
operator|.
name|getSnapshotsCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// write one snapshot to the fs
name|String
name|snapshotName
init|=
literal|"completed"
decl_stmt|;
name|SnapshotDescription
name|snapshot
init|=
name|createSnapshot
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
comment|// check that we get one snapshot
name|response
operator|=
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|getCompletedSnapshots
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Found unexpected number of snapshots"
argument_list|,
literal|1
argument_list|,
name|response
operator|.
name|getSnapshotsCount
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|snapshots
init|=
name|response
operator|.
name|getSnapshotsList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|expected
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|snapshot
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Returned snapshots don't match created snapshots"
argument_list|,
name|expected
argument_list|,
name|snapshots
argument_list|)
expr_stmt|;
comment|// write a second snapshot
name|snapshotName
operator|=
literal|"completed_two"
expr_stmt|;
name|snapshot
operator|=
name|createSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
comment|// check that we get one snapshot
name|response
operator|=
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|getCompletedSnapshots
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Found unexpected number of snapshots"
argument_list|,
literal|2
argument_list|,
name|response
operator|.
name|getSnapshotsCount
argument_list|()
argument_list|)
expr_stmt|;
name|snapshots
operator|=
name|response
operator|.
name|getSnapshotsList
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Returned snapshots don't match created snapshots"
argument_list|,
name|expected
argument_list|,
name|snapshots
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|snapshotName
init|=
literal|"completed"
decl_stmt|;
name|SnapshotDescription
name|snapshot
init|=
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|snapshotName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DeleteSnapshotRequest
name|request
init|=
name|DeleteSnapshotRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSnapshot
argument_list|(
name|snapshot
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Master didn't throw exception when attempting to delete snapshot that doesn't exist"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
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
name|protobuf
operator|.
name|ServiceException
name|e
parameter_list|)
block|{
comment|// Expected
block|}
comment|// write one snapshot to the fs
name|createSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
comment|// then delete the existing snapshot,which shouldn't cause an exception to be thrown
name|master
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the snapshot hfile archive cleaner works correctly. HFiles that are in snapshots    * should be retained, while those that are not in a snapshot should be deleted.    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotHFileArchiving
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// make sure we don't fail on listing snapshots
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
comment|// recreate test table with disabled compactions; otherwise compaction may happen before
comment|// snapshot, the call after snapshot will be a no-op and checks will fail
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|TableDescriptor
name|td
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|TEST_FAM
argument_list|)
argument_list|)
operator|.
name|setCompactionEnabled
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|td
argument_list|)
expr_stmt|;
comment|// load the table
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|blockingStoreFiles
operator|/
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|UTIL
operator|.
name|loadTable
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|flush
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|// disable the table so we can take a snapshot
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
comment|// take a snapshot of the table
name|String
name|snapshotName
init|=
literal|"snapshot"
decl_stmt|;
name|byte
index|[]
name|snapshotNameBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotNameBytes
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After snapshot File-System state"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// ensure we only have one snapshot
name|SnapshotTestingUtils
operator|.
name|assertOneSnapshotThatMatches
argument_list|(
name|admin
argument_list|,
name|snapshotNameBytes
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|td
operator|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|td
argument_list|)
operator|.
name|setCompactionEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// enable compactions now
name|admin
operator|.
name|modifyTable
argument_list|(
name|td
argument_list|)
expr_stmt|;
comment|// renable the table so we can compact the regions
name|admin
operator|.
name|enableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
comment|// compact the files so we get some archived files for the table we just snapshotted
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|region
operator|.
name|waitForFlushesAndCompactions
argument_list|()
expr_stmt|;
comment|// enable can trigger a compaction, wait for it.
name|region
operator|.
name|compactStores
argument_list|()
expr_stmt|;
comment|// min is 2 so will compact and archive
block|}
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|regionServerThreads
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
decl_stmt|;
name|HRegionServer
name|hrs
init|=
literal|null
decl_stmt|;
for|for
control|(
name|RegionServerThread
name|rs
range|:
name|regionServerThreads
control|)
block|{
if|if
condition|(
operator|!
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|hrs
operator|=
name|rs
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|CompactedHFilesDischarger
name|cleaner
init|=
operator|new
name|CompactedHFilesDischarger
argument_list|(
literal|100
argument_list|,
literal|null
argument_list|,
name|hrs
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After compaction File-System state"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// make sure the cleaner has run
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running hfile cleaners"
argument_list|)
expr_stmt|;
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After cleaners File-System state: "
operator|+
name|rootDir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// get the snapshot files for the table
name|Path
name|snapshotTable
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotName
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotHFiles
init|=
name|SnapshotReferenceUtil
operator|.
name|getHFileNames
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|snapshotTable
argument_list|)
decl_stmt|;
comment|// check that the files in the archive contain the ones that we need for the snapshot
name|LOG
operator|.
name|debug
argument_list|(
literal|"Have snapshot hfiles:"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|fileName
range|:
name|snapshotHFiles
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
comment|// get the archived files for the table
name|Collection
argument_list|<
name|String
argument_list|>
name|archives
init|=
name|getHFiles
argument_list|(
name|archiveDir
argument_list|,
name|fs
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// get the hfiles for the table
name|Collection
argument_list|<
name|String
argument_list|>
name|hfiles
init|=
name|getHFiles
argument_list|(
name|rootDir
argument_list|,
name|fs
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// and make sure that there is a proper subset
for|for
control|(
name|String
name|fileName
range|:
name|snapshotHFiles
control|)
block|{
name|boolean
name|exist
init|=
name|archives
operator|.
name|contains
argument_list|(
name|fileName
argument_list|)
operator|||
name|hfiles
operator|.
name|contains
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Archived hfiles "
operator|+
name|archives
operator|+
literal|" and table hfiles "
operator|+
name|hfiles
operator|+
literal|" is missing snapshot file:"
operator|+
name|fileName
argument_list|,
name|exist
argument_list|)
expr_stmt|;
block|}
comment|// delete the existing snapshot
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotNameBytes
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
comment|// make sure that we don't keep around the hfiles that aren't in a snapshot
comment|// make sure we wait long enough to refresh the snapshot hfile
name|List
argument_list|<
name|BaseHFileCleanerDelegate
argument_list|>
name|delegates
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getHFileCleaner
argument_list|()
operator|.
name|cleanersChain
decl_stmt|;
for|for
control|(
name|BaseHFileCleanerDelegate
name|delegate
range|:
name|delegates
control|)
block|{
if|if
condition|(
name|delegate
operator|instanceof
name|SnapshotHFileCleaner
condition|)
block|{
operator|(
operator|(
name|SnapshotHFileCleaner
operator|)
name|delegate
operator|)
operator|.
name|getFileCacheForTesting
argument_list|()
operator|.
name|triggerCacheRefreshForTesting
argument_list|()
expr_stmt|;
block|}
block|}
comment|// run the cleaner again
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running hfile cleaners"
argument_list|)
expr_stmt|;
name|ensureHFileCleanersRun
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After delete snapshot cleaners run File-System state"
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|archives
operator|=
name|getHFiles
argument_list|(
name|archiveDir
argument_list|,
name|fs
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Still have some hfiles in the archive, when their snapshot has been deleted."
argument_list|,
literal|0
argument_list|,
name|archives
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return all the HFiles for a given table in the specified dir    * @throws IOException on expected failure    */
specifier|private
specifier|final
name|Collection
argument_list|<
name|String
argument_list|>
name|getHFiles
parameter_list|(
name|Path
name|dir
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|dir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
return|return
name|SnapshotTestingUtils
operator|.
name|listHFileNames
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|)
return|;
block|}
comment|/**    * Make sure the {@link HFileCleaner HFileCleaners} run at least once    */
specifier|private
specifier|static
name|void
name|ensureHFileCleanersRun
parameter_list|()
block|{
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getHFileCleaner
argument_list|()
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
specifier|private
name|SnapshotDescription
name|createSnapshot
parameter_list|(
specifier|final
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
name|snapshotName
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|builder
operator|.
name|commit
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|getSnapshotDescription
argument_list|()
return|;
block|}
block|}
end_class

end_unit

