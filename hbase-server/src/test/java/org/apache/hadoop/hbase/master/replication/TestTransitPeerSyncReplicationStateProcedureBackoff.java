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
name|replication
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
name|ProcedureTestUtil
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|SyncReplicationState
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
name|MasterTests
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
name|TestTransitPeerSyncReplicationStateProcedureBackoff
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
name|TestTransitPeerSyncReplicationStateProcedureBackoff
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
name|boolean
name|FAIL
init|=
literal|true
decl_stmt|;
specifier|public
specifier|static
class|class
name|TestTransitPeerSyncReplicationStateProcedure
extends|extends
name|TransitPeerSyncReplicationStateProcedure
block|{
specifier|public
name|TestTransitPeerSyncReplicationStateProcedure
parameter_list|()
block|{     }
specifier|public
name|TestTransitPeerSyncReplicationStateProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|SyncReplicationState
name|state
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tryFail
parameter_list|()
throws|throws
name|ReplicationException
block|{
synchronized|synchronized
init|(
name|TestTransitPeerSyncReplicationStateProcedureBackoff
operator|.
name|class
init|)
block|{
if|if
condition|(
name|FAIL
condition|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Inject error"
argument_list|)
throw|;
block|}
name|FAIL
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
parameter_list|<
name|T
extends|extends
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
parameter_list|>
name|void
name|addChildProcedure
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|T
modifier|...
name|subProcedure
parameter_list|)
block|{
comment|// Make it a no-op
block|}
annotation|@
name|Override
specifier|protected
name|void
name|preTransit
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|fromState
operator|=
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setPeerNewSyncReplicationState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|tryFail
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|removeAllReplicationQueues
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|tryFail
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reopenRegions
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// do nothing;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|transitPeerSyncReplicationState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|tryFail
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|createDirForRemoteWAL
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|tryFail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
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
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|assertBackoffIncrease
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ProcedureTestUtil
operator|.
name|waitUntilProcedureWaitingTimeout
argument_list|(
name|UTIL
argument_list|,
name|TestTransitPeerSyncReplicationStateProcedure
operator|.
name|class
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|ProcedureTestUtil
operator|.
name|waitUntilProcedureTimeoutIncrease
argument_list|(
name|UTIL
argument_list|,
name|TestTransitPeerSyncReplicationStateProcedure
operator|.
name|class
argument_list|,
literal|2
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|TestTransitPeerSyncReplicationStateProcedure
operator|.
name|class
init|)
block|{
name|FAIL
operator|=
literal|false
expr_stmt|;
block|}
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|FAIL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDowngradeActiveToActive
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
comment|// Test procedure: DOWNGRADE_ACTIVE ==> ACTIVE
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TestTransitPeerSyncReplicationStateProcedure
argument_list|(
literal|"1"
argument_list|,
name|SyncReplicationState
operator|.
name|ACTIVE
argument_list|)
argument_list|)
decl_stmt|;
comment|// No retry for PRE_PEER_SYNC_REPLICATION_STATE_TRANSITION
comment|// SET_PEER_NEW_SYNC_REPLICATION_STATE
name|assertBackoffIncrease
argument_list|()
expr_stmt|;
comment|// No retry for REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_BEGIN
comment|// No retry for REOPEN_ALL_REGIONS_IN_PEER
comment|// TRANSIT_PEER_NEW_SYNC_REPLICATION_STATE
name|assertBackoffIncrease
argument_list|()
expr_stmt|;
comment|// No retry for REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_END
comment|// No retry for POST_PEER_SYNC_REPLICATION_STATE_TRANSITION
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|procExec
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDowngradeActiveToStandby
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
comment|// Test procedure: DOWNGRADE_ACTIVE ==> ACTIVE
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TestTransitPeerSyncReplicationStateProcedure
argument_list|(
literal|"2"
argument_list|,
name|SyncReplicationState
operator|.
name|STANDBY
argument_list|)
argument_list|)
decl_stmt|;
comment|// No retry for PRE_PEER_SYNC_REPLICATION_STATE_TRANSITION
comment|// SET_PEER_NEW_SYNC_REPLICATION_STATE
name|assertBackoffIncrease
argument_list|()
expr_stmt|;
comment|// No retry for REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_BEGIN
comment|// REMOVE_ALL_REPLICATION_QUEUES_IN_PEER
name|assertBackoffIncrease
argument_list|()
expr_stmt|;
comment|// TRANSIT_PEER_NEW_SYNC_REPLICATION_STATE
name|assertBackoffIncrease
argument_list|()
expr_stmt|;
comment|// No retry for REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_END
comment|// CREATE_DIR_FOR_REMOTE_WAL
name|assertBackoffIncrease
argument_list|()
expr_stmt|;
comment|// No retry for POST_PEER_SYNC_REPLICATION_STATE_TRANSITION
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|procExec
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

