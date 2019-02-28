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
name|master
operator|.
name|procedure
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
name|master
operator|.
name|procedure
operator|.
name|ServerProcedureInterface
operator|.
name|ServerOperationType
operator|.
name|SWITCH_RPC_THROTTLE
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
name|NavigableMap
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
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|ConcurrentSkipListMap
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
name|Executors
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
name|ScheduledExecutorService
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
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
name|ServerName
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
name|RegionInfoBuilder
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
name|master
operator|.
name|assignment
operator|.
name|MockMasterServices
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
name|OpenRegionProcedure
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
name|ProcedureStateSerializer
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
name|RemoteProcedureDispatcher
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
name|RemoteProcedureException
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
name|Assert
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
name|ExpectedException
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|AdminProtos
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestServerRemoteProcedure
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
name|TestServerRemoteProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestServerRemoteProcedure
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
annotation|@
name|Rule
specifier|public
specifier|final
name|ExpectedException
name|exception
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
specifier|protected
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|protected
name|MockRSProcedureDispatcher
name|rsDispatcher
decl_stmt|;
specifier|protected
name|MockMasterServices
name|master
decl_stmt|;
specifier|protected
name|AssignmentManager
name|am
decl_stmt|;
specifier|protected
name|NavigableMap
argument_list|<
name|ServerName
argument_list|,
name|SortedSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|regionsToRegionServers
init|=
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Simple executor to run some simple tasks.
specifier|protected
name|ScheduledExecutorService
name|executor
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|Executors
operator|.
name|newSingleThreadScheduledExecutor
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setUncaughtExceptionHandler
argument_list|(
parameter_list|(
name|t
parameter_list|,
name|e
parameter_list|)
lambda|->
name|LOG
operator|.
name|warn
argument_list|(
literal|"Uncaught: "
argument_list|,
name|e
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|=
operator|new
name|MockMasterServices
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|regionsToRegionServers
argument_list|)
expr_stmt|;
name|rsDispatcher
operator|=
operator|new
name|MockRSProcedureDispatcher
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|rsDispatcher
operator|.
name|setMockRsExecutor
argument_list|(
operator|new
name|NoopRSExecutor
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|start
argument_list|(
literal|2
argument_list|,
name|rsDispatcher
argument_list|)
expr_stmt|;
name|am
operator|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
expr_stmt|;
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|serverName
lambda|->
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getOrCreateServer
argument_list|(
name|serverName
argument_list|)
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
name|master
operator|.
name|stop
argument_list|(
literal|"tearDown"
argument_list|)
expr_stmt|;
name|this
operator|.
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitWALAndCrashBeforeResponse
parameter_list|()
throws|throws
name|Exception
block|{
name|ServerName
name|worker
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ServerName
name|crashedWorker
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ServerRemoteProcedure
name|splitWALRemoteProcedure
init|=
operator|new
name|SplitWALRemoteProcedure
argument_list|(
name|worker
argument_list|,
name|crashedWorker
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|byte
index|[]
argument_list|>
name|future
init|=
name|submitProcedure
argument_list|(
name|splitWALRemoteProcedure
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|expireServer
argument_list|(
name|worker
argument_list|)
expr_stmt|;
comment|// if remoteCallFailed is called for this procedure, this procedure should be finished.
name|future
operator|.
name|get
argument_list|(
literal|5000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|splitWALRemoteProcedure
operator|.
name|isSuccess
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoteCompleteAndFailedAtTheSameTime
parameter_list|()
throws|throws
name|Exception
block|{
name|ServerName
name|worker
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ServerRemoteProcedure
name|noopServerRemoteProcedure
init|=
operator|new
name|NoopServerRemoteProcedure
argument_list|(
name|worker
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|byte
index|[]
argument_list|>
name|future
init|=
name|submitProcedure
argument_list|(
name|noopServerRemoteProcedure
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|// complete the process and fail the process at the same time
name|ExecutorService
name|threadPool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|noopServerRemoteProcedure
operator|.
name|remoteOperationDone
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
name|noopServerRemoteProcedure
operator|.
name|remoteCallFailed
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|worker
argument_list|,
operator|new
name|IOException
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|future
operator|.
name|get
argument_list|(
literal|2000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|noopServerRemoteProcedure
operator|.
name|isSuccess
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionOpenProcedureIsNotHandledByDisPatcher
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRegionOpenProcedureIsNotHandledByDisPatcher"
argument_list|)
decl_stmt|;
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|setSplit
argument_list|(
literal|false
argument_list|)
operator|.
name|setRegionId
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getOrCreateRegionStateNode
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|ServerName
name|worker
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|OpenRegionProcedure
name|openRegionProcedure
init|=
operator|new
name|OpenRegionProcedure
argument_list|(
name|hri
argument_list|,
name|worker
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|byte
index|[]
argument_list|>
name|future
init|=
name|submitProcedure
argument_list|(
name|openRegionProcedure
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|rsDispatcher
operator|.
name|removeNode
argument_list|(
name|worker
argument_list|)
expr_stmt|;
try|try
block|{
name|future
operator|.
name|get
argument_list|(
literal|2000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"timeout is expected"
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|openRegionProcedure
operator|.
name|isFinished
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Future
argument_list|<
name|byte
index|[]
argument_list|>
name|submitProcedure
parameter_list|(
specifier|final
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|proc
parameter_list|)
block|{
return|return
name|ProcedureSyncWait
operator|.
name|submitProcedure
argument_list|(
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
name|proc
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|NoopServerRemoteProcedure
extends|extends
name|ServerRemoteProcedure
implements|implements
name|ServerProcedureInterface
block|{
specifier|public
name|NoopServerRemoteProcedure
parameter_list|(
name|ServerName
name|targetServer
parameter_list|)
block|{
name|this
operator|.
name|targetServer
operator|=
name|targetServer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
return|return;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
name|ProcedureStateSerializer
name|serializer
parameter_list|)
throws|throws
name|IOException
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|RemoteProcedureDispatcher
operator|.
name|RemoteOperation
name|remoteCallBuild
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
operator|new
name|RSProcedureDispatcher
operator|.
name|ServerOperation
argument_list|(
literal|null
argument_list|,
literal|0L
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|remoteOperationCompleted
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|complete
argument_list|(
name|env
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|remoteOperationFailed
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RemoteProcedureException
name|error
parameter_list|)
block|{
name|complete
argument_list|(
name|env
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|complete
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
name|this
operator|.
name|succ
operator|=
literal|true
expr_stmt|;
return|return;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|targetServer
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasMetaTableRegion
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerOperationType
name|getServerOperationType
parameter_list|()
block|{
return|return
name|SWITCH_RPC_THROTTLE
return|;
block|}
block|}
specifier|protected
interface|interface
name|MockRSExecutor
block|{
name|AdminProtos
operator|.
name|ExecuteProceduresResponse
name|sendRequest
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|AdminProtos
operator|.
name|ExecuteProceduresRequest
name|req
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|protected
specifier|static
class|class
name|NoopRSExecutor
implements|implements
name|MockRSExecutor
block|{
annotation|@
name|Override
specifier|public
name|AdminProtos
operator|.
name|ExecuteProceduresResponse
name|sendRequest
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|AdminProtos
operator|.
name|ExecuteProceduresRequest
name|req
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|req
operator|.
name|getOpenRegionCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|AdminProtos
operator|.
name|OpenRegionRequest
name|request
range|:
name|req
operator|.
name|getOpenRegionList
argument_list|()
control|)
block|{
for|for
control|(
name|AdminProtos
operator|.
name|OpenRegionRequest
operator|.
name|RegionOpenInfo
name|openReq
range|:
name|request
operator|.
name|getOpenInfoList
argument_list|()
control|)
block|{
name|execOpenRegion
argument_list|(
name|server
argument_list|,
name|openReq
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|AdminProtos
operator|.
name|ExecuteProceduresResponse
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
specifier|protected
name|AdminProtos
operator|.
name|OpenRegionResponse
operator|.
name|RegionOpeningState
name|execOpenRegion
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|AdminProtos
operator|.
name|OpenRegionRequest
operator|.
name|RegionOpenInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|protected
specifier|static
class|class
name|MockRSProcedureDispatcher
extends|extends
name|RSProcedureDispatcher
block|{
specifier|private
name|MockRSExecutor
name|mockRsExec
decl_stmt|;
specifier|public
name|MockRSProcedureDispatcher
parameter_list|(
specifier|final
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
specifier|public
name|void
name|setMockRsExecutor
parameter_list|(
specifier|final
name|MockRSExecutor
name|mockRsExec
parameter_list|)
block|{
name|this
operator|.
name|mockRsExec
operator|=
name|mockRsExec
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|remoteDispatch
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|remoteProcedures
parameter_list|)
block|{
name|submitTask
argument_list|(
operator|new
name|MockRSProcedureDispatcher
operator|.
name|MockRemoteCall
argument_list|(
name|serverName
argument_list|,
name|remoteProcedures
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|MockRemoteCall
extends|extends
name|ExecuteProceduresRemoteCall
block|{
specifier|public
name|MockRemoteCall
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|final
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|operations
parameter_list|)
block|{
name|super
argument_list|(
name|serverName
argument_list|,
name|operations
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|AdminProtos
operator|.
name|ExecuteProceduresResponse
name|sendRequest
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|AdminProtos
operator|.
name|ExecuteProceduresRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|mockRsExec
operator|.
name|sendRequest
argument_list|(
name|serverName
argument_list|,
name|request
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

