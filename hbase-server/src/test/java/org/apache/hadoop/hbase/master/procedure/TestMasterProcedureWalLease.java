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
name|procedure
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
name|concurrent
operator|.
name|CountDownLatch
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
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|ProcedureExecutor
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
name|procedure2
operator|.
name|ProcedureTestingUtility
operator|.
name|TestProcedure
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStore
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
name|procedure2
operator|.
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|util
operator|.
name|ModifyRegionUtils
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
name|Before
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
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
annotation|@
name|Ignore
specifier|public
class|class
name|TestMasterProcedureWalLease
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
name|TestMasterProcedureWalLease
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
name|TestMasterProcedureWalLease
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|protected
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
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// don't waste time retrying with the roll, the test is already slow enough.
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|MAX_RETRIES_BEFORE_ROLL_CONF_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|WAIT_BEFORE_ROLL_CONF_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|ROLL_RETRIES_CONF_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|MAX_SYNC_FAILURE_ROLL_CONF_KEY
argument_list|,
literal|1
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
literal|2
argument_list|,
literal|3
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWalRecoverLease
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ProcedureStore
name|masterStore
init|=
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getStore
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected WALStore for this test"
argument_list|,
name|masterStore
operator|instanceof
name|WALProcedureStore
argument_list|)
expr_stmt|;
name|HMaster
name|firstMaster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// Abort Latch for the master store
specifier|final
name|CountDownLatch
name|masterStoreAbort
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|masterStore
operator|.
name|registerListener
argument_list|(
operator|new
name|ProcedureStore
operator|.
name|ProcedureStoreListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|postSync
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|abortProcess
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Abort store of Master"
argument_list|)
expr_stmt|;
name|masterStoreAbort
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// startup a fake master the new WAL store will take the lease
comment|// and the active master should abort.
name|HMaster
name|backupMaster3
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HMaster
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|firstMaster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|backupMaster3
argument_list|)
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|backupMaster3
argument_list|)
operator|.
name|isActiveMaster
argument_list|()
expr_stmt|;
specifier|final
name|WALProcedureStore
name|backupStore3
init|=
operator|new
name|WALProcedureStore
argument_list|(
name|firstMaster
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|(
operator|(
name|WALProcedureStore
operator|)
name|masterStore
operator|)
operator|.
name|getWALDir
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|MasterProcedureEnv
operator|.
name|WALStoreLeaseRecovery
argument_list|(
name|backupMaster3
argument_list|)
argument_list|)
decl_stmt|;
comment|// Abort Latch for the test store
specifier|final
name|CountDownLatch
name|backupStore3Abort
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|backupStore3
operator|.
name|registerListener
argument_list|(
operator|new
name|ProcedureStore
operator|.
name|ProcedureStoreListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|postSync
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|abortProcess
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Abort store of backupMaster3"
argument_list|)
expr_stmt|;
name|backupStore3Abort
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|backupStore3
operator|.
name|stop
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|backupStore3
operator|.
name|start
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|backupStore3
operator|.
name|recoverLease
argument_list|()
expr_stmt|;
comment|// Try to trigger a command on the master (WAL lease expired on the active one)
name|TableDescriptor
name|htd
init|=
name|MasterProcedureTestingUtility
operator|.
name|createHTD
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
literal|"f"
argument_list|)
decl_stmt|;
name|RegionInfo
index|[]
name|regions
init|=
name|ModifyRegionUtils
operator|.
name|createRegionInfos
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"submit proc"
argument_list|)
expr_stmt|;
try|try
block|{
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedure
argument_list|(
operator|new
name|CreateTableProcedure
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|regions
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected RuntimeException 'sync aborted'"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"got "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"wait master store abort"
argument_list|)
expr_stmt|;
name|masterStoreAbort
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// Now the real backup master should start up
name|LOG
operator|.
name|debug
argument_list|(
literal|"wait backup master to startup"
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|waitBackupMaster
argument_list|(
name|UTIL
argument_list|,
name|firstMaster
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|firstMaster
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait the store in here to abort (the test will fail due to timeout if it doesn't)
name|LOG
operator|.
name|debug
argument_list|(
literal|"wait the store to abort"
argument_list|)
expr_stmt|;
name|backupStore3
operator|.
name|getStoreTracker
argument_list|()
operator|.
name|setDeleted
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|backupStore3
operator|.
name|delete
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected RuntimeException 'sync aborted'"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"got "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|backupStore3Abort
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests proper fencing in case the current WAL store is fenced    */
annotation|@
name|Test
specifier|public
name|void
name|testWALfencingWithoutWALRolling
parameter_list|()
throws|throws
name|IOException
block|{
name|testWALfencing
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests proper fencing in case the current WAL store does not receive writes until after the    * new WAL does a couple of WAL rolls.    */
annotation|@
name|Test
specifier|public
name|void
name|testWALfencingWithWALRolling
parameter_list|()
throws|throws
name|IOException
block|{
name|testWALfencing
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testWALfencing
parameter_list|(
name|boolean
name|walRolls
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ProcedureStore
name|procStore
init|=
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getStore
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected WALStore for this test"
argument_list|,
name|procStore
operator|instanceof
name|WALProcedureStore
argument_list|)
expr_stmt|;
name|HMaster
name|firstMaster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// cause WAL rolling after a delete in WAL:
name|firstMaster
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|WALProcedureStore
operator|.
name|ROLL_THRESHOLD_CONF_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|HMaster
name|backupMaster3
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HMaster
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|firstMaster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|backupMaster3
argument_list|)
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
literal|true
argument_list|)
operator|.
name|when
argument_list|(
name|backupMaster3
argument_list|)
operator|.
name|isActiveMaster
argument_list|()
expr_stmt|;
specifier|final
name|WALProcedureStore
name|procStore2
init|=
operator|new
name|WALProcedureStore
argument_list|(
name|firstMaster
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|(
operator|(
name|WALProcedureStore
operator|)
name|procStore
operator|)
operator|.
name|getWALDir
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|MasterProcedureEnv
operator|.
name|WALStoreLeaseRecovery
argument_list|(
name|backupMaster3
argument_list|)
argument_list|)
decl_stmt|;
comment|// start a second store which should fence the first one out
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting new WALProcedureStore"
argument_list|)
expr_stmt|;
name|procStore2
operator|.
name|start
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|procStore2
operator|.
name|recoverLease
argument_list|()
expr_stmt|;
comment|// before writing back to the WAL store, optionally do a couple of WAL rolls (which causes
comment|// to delete the old WAL files).
if|if
condition|(
name|walRolls
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Inserting into second WALProcedureStore, causing WAL rolls"
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
literal|512
condition|;
name|i
operator|++
control|)
block|{
comment|// insert something to the second store then delete it, causing a WAL roll(s)
name|Procedure
name|proc2
init|=
operator|new
name|TestProcedure
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|procStore2
operator|.
name|insert
argument_list|(
name|proc2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|procStore2
operator|.
name|delete
argument_list|(
name|proc2
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
comment|// delete the procedure so that the WAL is removed later
block|}
block|}
comment|// Now, insert something to the first store, should fail.
comment|// If the store does a WAL roll and continue with another logId without checking higher logIds
comment|// it will incorrectly succeed.
name|LOG
operator|.
name|info
argument_list|(
literal|"Inserting into first WALProcedureStore"
argument_list|)
expr_stmt|;
try|try
block|{
name|procStore
operator|.
name|insert
argument_list|(
operator|new
name|TestProcedure
argument_list|(
literal|11
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Inserting into Procedure Store should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received expected exception"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|//  Helpers
comment|// ==========================================================================
specifier|private
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
parameter_list|()
block|{
return|return
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
return|;
block|}
block|}
end_class

end_unit

