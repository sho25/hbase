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
name|assignment
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
name|PleaseHoldException
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
name|StartMiniClusterOption
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
name|MasterServices
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
name|RegionPlan
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
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
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
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
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
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
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
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionResponse
import|;
end_import

begin_comment
comment|/**  * See HBASE-22060 and HBASE-22074 for more details.  */
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
name|TestOpenRegionProcedureHang
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
name|TestOpenRegionProcedureHang
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
name|TestOpenRegionProcedureHang
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|ARRIVE
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|RESUME
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|FINISH
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|ABORT
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|AssignmentManagerForTest
extends|extends
name|AssignmentManager
block|{
specifier|public
name|AssignmentManagerForTest
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReportRegionStateTransitionResponse
name|reportRegionStateTransition
parameter_list|(
name|ReportRegionStateTransitionRequest
name|req
parameter_list|)
throws|throws
name|PleaseHoldException
block|{
name|RegionStateTransition
name|transition
init|=
name|req
operator|.
name|getTransition
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|transition
operator|.
name|getTransitionCode
argument_list|()
operator|==
name|TransitionCode
operator|.
name|OPENED
operator|&&
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|transition
operator|.
name|getRegionInfo
argument_list|(
literal|0
argument_list|)
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|NAME
argument_list|)
operator|&&
name|ARRIVE
operator|!=
literal|null
condition|)
block|{
name|ARRIVE
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|RESUME
operator|.
name|await
argument_list|()
expr_stmt|;
name|RESUME
operator|=
literal|null
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|super
operator|.
name|reportRegionStateTransition
argument_list|(
name|req
argument_list|)
return|;
block|}
finally|finally
block|{
name|FINISH
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
return|return
name|super
operator|.
name|reportRegionStateTransition
argument_list|(
name|req
argument_list|)
return|;
block|}
block|}
block|}
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
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|AssignmentManager
name|createAssignmentManager
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
return|return
operator|new
name|AssignmentManagerForTest
argument_list|(
name|master
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
comment|// hang here so we can finish the reportRegionStateTransition call, which is the most
comment|// important part to reproduce the bug
if|if
condition|(
name|ABORT
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|ABORT
operator|.
name|await
argument_list|()
expr_stmt|;
name|ABORT
operator|=
literal|null
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|abort
argument_list|(
name|reason
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
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
name|TableName
name|NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"Open"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
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
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
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
comment|// make sure we do not timeout when caling reportRegionStateTransition
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
literal|10
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_SHORTOPERATION_TIMEOUT_KEY
argument_list|,
literal|10
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numMasters
argument_list|(
literal|2
argument_list|)
operator|.
name|numRegionServers
argument_list|(
literal|3
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|NAME
argument_list|,
name|CF
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
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
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|KeeperException
throws|,
name|IOException
block|{
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|AssignmentManager
name|am
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|HRegionServer
name|rs1
init|=
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs2
init|=
name|UTIL
operator|.
name|getOtherRegionServer
argument_list|(
name|rs1
argument_list|)
decl_stmt|;
name|ARRIVE
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|RESUME
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|FINISH
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ABORT
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|am
operator|.
name|moveAsync
argument_list|(
operator|new
name|RegionPlan
argument_list|(
name|region
argument_list|,
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|,
name|rs2
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ARRIVE
operator|.
name|await
argument_list|()
expr_stmt|;
name|ARRIVE
operator|=
literal|null
expr_stmt|;
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|master
operator|.
name|getZooKeeper
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
block|{
for|for
control|(
name|MasterThread
name|mt
range|:
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMasterThreads
argument_list|()
control|)
block|{
if|if
condition|(
name|mt
operator|.
name|getMaster
argument_list|()
operator|!=
name|master
operator|&&
name|mt
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
return|return
name|mt
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
argument_list|)
expr_stmt|;
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
name|OpenRegionProcedure
argument_list|)
operator|.
name|map
argument_list|(
name|p
lambda|->
operator|(
name|OpenRegionProcedure
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
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|NAME
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|OpenRegionProcedure
name|proc
init|=
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
name|OpenRegionProcedure
argument_list|)
operator|.
name|map
argument_list|(
name|p
lambda|->
operator|(
name|OpenRegionProcedure
operator|)
name|p
argument_list|)
operator|.
name|filter
argument_list|(
name|p
lambda|->
name|p
operator|.
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|NAME
argument_list|)
argument_list|)
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// wait a bit to let the OpenRegionProcedure send out the request
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|RESUME
operator|.
name|countDown
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|FINISH
operator|.
name|await
argument_list|(
literal|15
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Wait reportRegionStateTransition to finish timed out, this is possible if"
operator|+
literal|" we update the procedure store, as the WALProcedureStore"
operator|+
literal|" will retry forever to roll the writer if it is not closed"
argument_list|)
expr_stmt|;
block|}
name|FINISH
operator|=
literal|null
expr_stmt|;
comment|// if the reportRegionTransition is finished, wait a bit to let it return the data to RS
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|ABORT
operator|.
name|countDown
argument_list|()
expr_stmt|;
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
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|proc
operator|.
name|getParentProcId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

