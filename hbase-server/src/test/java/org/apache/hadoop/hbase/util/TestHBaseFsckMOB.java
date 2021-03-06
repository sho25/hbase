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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledThreadPoolExecutor
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|TestHFile
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
name|assignment
operator|.
name|AssignmentManager
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
name|hbck
operator|.
name|HFileCorruptionChecker
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
name|hbck
operator|.
name|HbckTestingUtil
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestHBaseFsckMOB
extends|extends
name|BaseTestHBaseFsck
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
name|TestHBaseFsckMOB
operator|.
name|class
argument_list|)
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
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MasterSyncCoprocessor
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
literal|"hbase.regionserver.handler.count"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.metahandler.count"
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.htable.threads.max"
argument_list|,
name|POOL_SIZE
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hconnection.threads.max"
argument_list|,
literal|2
operator|*
name|POOL_SIZE
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hbck.close.timeout"
argument_list|,
literal|2
operator|*
name|REGION_ONLINE_TIMEOUT
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
literal|8
operator|*
name|REGION_ONLINE_TIMEOUT
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|tableExecutorService
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|POOL_SIZE
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<>
argument_list|()
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"testhbck"
argument_list|)
argument_list|)
expr_stmt|;
name|hbfsckExecutorService
operator|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
name|POOL_SIZE
argument_list|)
expr_stmt|;
name|AssignmentManager
name|assignmentManager
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|regionStates
operator|=
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
expr_stmt|;
name|connection
operator|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
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
name|tableExecutorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|hbfsckExecutorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
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
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**    * This creates a table and then corrupts a mob file.  Hbck should quarantine the file.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testQuarantineCorruptMobFile
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|setupMobTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|mobFile
init|=
name|getFlushedMobFile
argument_list|(
name|fs
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// create new corrupt mob file.
name|String
name|corruptMobFile
init|=
name|createMobFileName
argument_list|(
name|mobFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|corrupt
init|=
operator|new
name|Path
argument_list|(
name|mobFile
operator|.
name|getParent
argument_list|()
argument_list|,
name|corruptMobFile
argument_list|)
decl_stmt|;
name|TestHFile
operator|.
name|truncateFile
argument_list|(
name|fs
argument_list|,
name|mobFile
argument_list|,
name|corrupt
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created corrupted mob file "
operator|+
name|corrupt
argument_list|)
expr_stmt|;
name|HBaseFsck
operator|.
name|debugLsr
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseFsck
operator|.
name|debugLsr
argument_list|(
name|conf
argument_list|,
name|MobUtils
operator|.
name|getMobHome
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
comment|// A corrupt mob file doesn't abort the start of regions, so we can enable the table.
name|admin
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|HBaseFsck
name|res
init|=
name|HbckTestingUtil
operator|.
name|doHFileQuarantine
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|res
operator|.
name|getRetCode
argument_list|()
argument_list|)
expr_stmt|;
name|HFileCorruptionChecker
name|hfcc
init|=
name|res
operator|.
name|getHFilecorruptionChecker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|hfcc
operator|.
name|getHFilesChecked
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getCorrupted
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getFailures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getQuarantined
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getMissing
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|hfcc
operator|.
name|getMobFilesChecked
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|hfcc
operator|.
name|getCorruptedMobFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getFailureMobFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|hfcc
operator|.
name|getQuarantinedMobFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getMissedMobFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|quarantinedMobFile
init|=
name|hfcc
operator|.
name|getQuarantinedMobFiles
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|corruptMobFile
argument_list|,
name|quarantinedMobFile
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

