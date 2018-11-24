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
import|import static
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
name|MasterProcedureProtos
operator|.
name|RecoverStandbyState
operator|.
name|DISPATCH_WALS_VALUE
import|;
end_import

begin_import
import|import static
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
name|MasterProcedureProtos
operator|.
name|RecoverStandbyState
operator|.
name|UNREGISTER_PEER_FROM_WORKER_STORAGE_VALUE
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
name|UncheckedIOException
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
name|replication
operator|.
name|SyncReplicationTestBase
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
name|JVMClusterUtil
operator|.
name|MasterThread
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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

begin_comment
comment|/**  * Testcase for HBASE-21494.  */
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegisterPeerWorkerWhenRestarting
extends|extends
name|SyncReplicationTestBase
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
name|TestRegisterPeerWorkerWhenRestarting
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|boolean
name|FAIL
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|HMasterForTest
extends|extends
name|HMaster
block|{
specifier|public
name|HMasterForTest
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remoteProcedureCompleted
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
if|if
condition|(
name|FAIL
operator|&&
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getProcedure
argument_list|(
name|procId
argument_list|)
operator|instanceof
name|SyncReplicationReplayWALRemoteProcedure
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Inject error"
argument_list|)
throw|;
block|}
name|super
operator|.
name|remoteProcedureCompleted
argument_list|(
name|procId
argument_list|)
expr_stmt|;
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
name|UTIL2
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|HMasterForTest
operator|.
name|class
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
name|SyncReplicationTestBase
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|transitReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|,
name|SyncReplicationState
operator|.
name|STANDBY
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|getAdmin
argument_list|()
operator|.
name|transitReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|,
name|SyncReplicationState
operator|.
name|ACTIVE
argument_list|)
expr_stmt|;
name|UTIL1
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableReplicationPeer
argument_list|(
name|PEER_ID
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|UTIL1
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|// peer is disabled so no data have been replicated
name|verifyNotReplicatedThroughRegion
argument_list|(
name|UTIL2
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
comment|// transit the A to DA first to avoid too many error logs.
name|UTIL1
operator|.
name|getAdmin
argument_list|()
operator|.
name|transitReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|,
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|)
expr_stmt|;
name|HMaster
name|master
init|=
name|UTIL2
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// make sure the transiting can not succeed
name|FAIL
operator|=
literal|true
expr_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
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
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|transitReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|,
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// wait until we are in the states where we need to register peer worker when restarting
name|UTIL2
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|procExec
operator|.
name|getProcedures
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|p
lambda|->
name|p
operator|instanceof
name|RecoverStandbyProcedure
argument_list|)
operator|.
name|map
argument_list|(
name|p
lambda|->
operator|(
name|RecoverStandbyProcedure
operator|)
name|p
argument_list|)
operator|.
name|anyMatch
argument_list|(
name|p
lambda|->
name|p
operator|.
name|getCurrentStateId
argument_list|()
operator|==
name|DISPATCH_WALS_VALUE
operator|||
name|p
operator|.
name|getCurrentStateId
argument_list|()
operator|==
name|UNREGISTER_PEER_FROM_WORKER_STORAGE_VALUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// failover to another master
name|MasterThread
name|mt
init|=
name|UTIL2
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThread
argument_list|()
decl_stmt|;
name|mt
operator|.
name|getMaster
argument_list|()
operator|.
name|abort
argument_list|(
literal|"for testing"
argument_list|)
expr_stmt|;
name|mt
operator|.
name|join
argument_list|()
expr_stmt|;
name|FAIL
operator|=
literal|false
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// make sure the new master can finish the transition
name|UTIL2
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|UTIL2
operator|.
name|getAdmin
argument_list|()
operator|.
name|getReplicationPeerSyncReplicationState
argument_list|(
name|PEER_ID
argument_list|)
operator|==
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|UTIL2
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

