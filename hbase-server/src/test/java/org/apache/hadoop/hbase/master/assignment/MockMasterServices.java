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
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|any
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
name|HashSet
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
name|Map
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
name|SortedSet
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
name|CoordinatedStateManager
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
name|ServerLoad
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
name|ServerMetricsBuilder
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
name|TableDescriptors
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
name|YouAreDeadException
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
name|ClusterConnection
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
name|HConnectionTestingUtility
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
name|client
operator|.
name|TableState
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
name|LoadBalancer
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
name|MasterFileSystem
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
name|MasterWalManager
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
name|MockNoopMasterServices
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
name|ServerManager
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
name|TableStateManager
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
name|balancer
operator|.
name|LoadBalancerFactory
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
name|MasterProcedureConstants
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
name|master
operator|.
name|procedure
operator|.
name|RSProcedureDispatcher
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
name|ProcedureEvent
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
name|store
operator|.
name|NoopProcedureStore
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
name|security
operator|.
name|Superusers
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
name|protobuf
operator|.
name|ServiceException
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
name|AdminProtos
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
name|ClientProtos
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
name|ClientProtos
operator|.
name|MultiRequest
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
name|ClientProtos
operator|.
name|MultiResponse
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
name|ClientProtos
operator|.
name|MutateResponse
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
name|ClientProtos
operator|.
name|RegionAction
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
name|ClientProtos
operator|.
name|RegionActionResult
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
name|ClientProtos
operator|.
name|ResultOrException
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_comment
comment|/**  * A mocked master services.  * Tries to fake it. May not always work.  */
end_comment

begin_class
specifier|public
class|class
name|MockMasterServices
extends|extends
name|MockNoopMasterServices
block|{
specifier|private
specifier|final
name|MasterFileSystem
name|fileSystemManager
decl_stmt|;
specifier|private
specifier|final
name|MasterWalManager
name|walManager
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|TableStateManager
name|tableStateManager
decl_stmt|;
specifier|private
name|MasterProcedureEnv
name|procedureEnv
decl_stmt|;
specifier|private
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procedureExecutor
decl_stmt|;
specifier|private
name|ProcedureStore
name|procedureStore
decl_stmt|;
specifier|private
specifier|final
name|ClusterConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|LoadBalancer
name|balancer
decl_stmt|;
specifier|private
specifier|final
name|ServerManager
name|serverManager
decl_stmt|;
comment|// Set of regions on a 'server'. Populated externally. Used in below faking 'cluster'.
specifier|private
specifier|final
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
decl_stmt|;
specifier|private
specifier|final
name|ProcedureEvent
name|initialized
init|=
operator|new
name|ProcedureEvent
argument_list|(
literal|"master initialized"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_COLUMN_FAMILY_NAME
init|=
literal|"cf"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ServerName
name|MOCK_MASTER_SERVERNAME
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"mockmaster.example.org"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
specifier|public
name|MockMasterServices
parameter_list|(
name|Configuration
name|conf
parameter_list|,
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
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionsToRegionServers
operator|=
name|regionsToRegionServers
expr_stmt|;
name|Superusers
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|fileSystemManager
operator|=
operator|new
name|MasterFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|walManager
operator|=
operator|new
name|MasterWalManager
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// Mock an AM.
name|this
operator|.
name|assignmentManager
operator|=
operator|new
name|AssignmentManager
argument_list|(
name|this
argument_list|,
operator|new
name|MockRegionStateStore
argument_list|(
name|this
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTableDisabled
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|waitServerReportEvent
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|Procedure
name|proc
parameter_list|)
block|{
comment|// Make a report with current state of the server 'serverName' before we call wait..
name|SortedSet
argument_list|<
name|byte
index|[]
argument_list|>
name|regions
init|=
name|regionsToRegionServers
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
try|try
block|{
name|getAssignmentManager
argument_list|()
operator|.
name|reportOnlineRegions
argument_list|(
name|serverName
argument_list|,
literal|0
argument_list|,
name|regions
operator|==
literal|null
condition|?
operator|new
name|HashSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
else|:
name|regions
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|YouAreDeadException
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
return|return
name|super
operator|.
name|waitServerReportEvent
argument_list|(
name|serverName
argument_list|,
name|proc
argument_list|)
return|;
block|}
block|}
expr_stmt|;
name|this
operator|.
name|balancer
operator|=
name|LoadBalancerFactory
operator|.
name|getLoadBalancer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverManager
operator|=
operator|new
name|ServerManager
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableStateManager
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TableStateManager
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|this
operator|.
name|tableStateManager
operator|.
name|getTableState
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|TableState
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"AnyTableNameSetInMockMasterServcies"
argument_list|)
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
argument_list|)
expr_stmt|;
comment|// Mock up a Client Interface
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|ri
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
decl_stmt|;
name|MutateResponse
operator|.
name|Builder
name|builder
init|=
name|MutateResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setProcessed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|ri
operator|.
name|mutate
argument_list|(
name|any
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
try|try
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|ri
operator|.
name|multi
argument_list|(
name|any
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|MultiResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|MultiResponse
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|buildMultiResponse
argument_list|(
name|invocation
operator|.
name|getArgument
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
comment|// Mock n ClusterConnection and an AdminProtocol implementation. Have the
comment|// ClusterConnection return the HRI.  Have the HRI return a few mocked up responses
comment|// to make our test work.
name|this
operator|.
name|connection
operator|=
name|HConnectionTestingUtility
operator|.
name|getMockedConnectionAndDecorate
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|Mockito
operator|.
name|mock
argument_list|(
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
argument_list|,
name|ri
argument_list|,
name|MOCK_MASTER_SERVERNAME
argument_list|,
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
comment|// Set hbase.rootdir into test dir.
name|Path
name|rootdir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|rootdir
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|mock
argument_list|(
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|(
specifier|final
name|int
name|numServes
parameter_list|,
specifier|final
name|RSProcedureDispatcher
name|remoteDispatcher
parameter_list|)
throws|throws
name|IOException
block|{
name|startProcedureExecutor
argument_list|(
name|remoteDispatcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|.
name|start
argument_list|()
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
name|numServes
condition|;
operator|++
name|i
control|)
block|{
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|100
operator|+
name|i
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|serverManager
operator|.
name|regionServerReport
argument_list|(
name|sn
argument_list|,
operator|new
name|ServerLoad
argument_list|(
name|ServerMetricsBuilder
operator|.
name|of
argument_list|(
name|sn
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|procedureExecutor
operator|.
name|getEnvironment
argument_list|()
operator|.
name|setEventReady
argument_list|(
name|initialized
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Call this restart method only after running MockMasterServices#start()    * The RSs can be differentiated by the port number, see    * ServerName in MockMasterServices#start() method above.    * Restart of region server will have new startcode in server name    *    * @param serverName Server name to be restarted    */
specifier|public
name|void
name|restartRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ServerName
argument_list|>
name|onlineServers
init|=
name|serverManager
operator|.
name|getOnlineServersList
argument_list|()
decl_stmt|;
name|long
name|startCode
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|ServerName
name|s
range|:
name|onlineServers
control|)
block|{
if|if
condition|(
name|s
operator|.
name|getAddress
argument_list|()
operator|.
name|equals
argument_list|(
name|serverName
operator|.
name|getAddress
argument_list|()
argument_list|)
condition|)
block|{
name|startCode
operator|=
name|s
operator|.
name|getStartcode
argument_list|()
operator|+
literal|1
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|startCode
operator|==
operator|-
literal|1
condition|)
block|{
return|return;
block|}
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|serverName
operator|.
name|getAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|startCode
argument_list|)
decl_stmt|;
name|serverManager
operator|.
name|regionServerReport
argument_list|(
name|sn
argument_list|,
operator|new
name|ServerLoad
argument_list|(
name|ServerMetricsBuilder
operator|.
name|of
argument_list|(
name|sn
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|stopProcedureExecutor
argument_list|()
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|startProcedureExecutor
parameter_list|(
specifier|final
name|RSProcedureDispatcher
name|remoteDispatcher
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Configuration
name|conf
init|=
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|logDir
init|=
operator|new
name|Path
argument_list|(
name|fileSystemManager
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|WALProcedureStore
operator|.
name|MASTER_PROCEDURE_LOGDIR
argument_list|)
decl_stmt|;
name|this
operator|.
name|procedureStore
operator|=
operator|new
name|NoopProcedureStore
argument_list|()
expr_stmt|;
name|this
operator|.
name|procedureStore
operator|.
name|registerListener
argument_list|(
operator|new
name|MasterProcedureEnv
operator|.
name|MasterProcedureStoreListener
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|procedureEnv
operator|=
operator|new
name|MasterProcedureEnv
argument_list|(
name|this
argument_list|,
name|remoteDispatcher
operator|!=
literal|null
condition|?
name|remoteDispatcher
else|:
operator|new
name|RSProcedureDispatcher
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|procedureExecutor
operator|=
operator|new
name|ProcedureExecutor
argument_list|(
name|conf
argument_list|,
name|procedureEnv
argument_list|,
name|procedureStore
argument_list|,
name|procedureEnv
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MasterProcedureConstants
operator|.
name|MASTER_PROCEDURE_THREADS
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
argument_list|,
name|MasterProcedureConstants
operator|.
name|DEFAULT_MIN_MASTER_PROCEDURE_THREADS
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|abortOnCorruption
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|MasterProcedureConstants
operator|.
name|EXECUTOR_ABORT_ON_CORRUPTION
argument_list|,
name|MasterProcedureConstants
operator|.
name|DEFAULT_EXECUTOR_ABORT_ON_CORRUPTION
argument_list|)
decl_stmt|;
name|this
operator|.
name|procedureStore
operator|.
name|start
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
name|this
operator|.
name|procedureExecutor
operator|.
name|start
argument_list|(
name|numThreads
argument_list|,
name|abortOnCorruption
argument_list|)
expr_stmt|;
name|this
operator|.
name|procedureEnv
operator|.
name|getRemoteDispatcher
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|stopProcedureExecutor
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|procedureEnv
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|procedureEnv
operator|.
name|getRemoteDispatcher
argument_list|()
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|procedureExecutor
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|procedureExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|procedureStore
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|procedureStore
operator|.
name|stop
argument_list|(
name|isAborted
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProcedureEvent
name|getInitializedEvent
parameter_list|()
block|{
return|return
name|this
operator|.
name|initialized
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterFileSystem
name|getMasterFileSystem
parameter_list|()
block|{
return|return
name|fileSystemManager
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterWalManager
name|getMasterWalManager
parameter_list|()
block|{
return|return
name|walManager
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
parameter_list|()
block|{
return|return
name|procedureExecutor
return|;
block|}
annotation|@
name|Override
specifier|public
name|LoadBalancer
name|getLoadBalancer
parameter_list|()
block|{
return|return
name|balancer
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerManager
name|getServerManager
parameter_list|()
block|{
return|return
name|serverManager
return|;
block|}
annotation|@
name|Override
specifier|public
name|AssignmentManager
name|getAssignmentManager
parameter_list|()
block|{
return|return
name|assignmentManager
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableStateManager
name|getTableStateManager
parameter_list|()
block|{
return|return
name|tableStateManager
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
name|this
operator|.
name|connection
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|MOCK_MASTER_SERVERNAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
name|super
operator|.
name|getCoordinatedStateManager
argument_list|()
return|;
block|}
specifier|private
specifier|static
class|class
name|MockRegionStateStore
extends|extends
name|RegionStateStore
block|{
specifier|public
name|MockRegionStateStore
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
annotation|@
name|Override
specifier|public
name|void
name|updateRegionLocation
parameter_list|(
name|RegionStates
operator|.
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
annotation|@
name|Override
specifier|public
name|TableDescriptors
name|getTableDescriptors
parameter_list|()
block|{
return|return
operator|new
name|TableDescriptors
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TableDescriptor
name|remove
parameter_list|(
name|TableName
name|tablename
parameter_list|)
throws|throws
name|IOException
block|{
comment|// noop
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|getAll
parameter_list|()
throws|throws
name|IOException
block|{
comment|// noop
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableDescriptor
name|get
parameter_list|(
name|TableName
name|tablename
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tablename
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|DEFAULT_COLUMN_FAMILY_NAME
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|getByNamespace
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
comment|// noop
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCacheOn
parameter_list|()
throws|throws
name|IOException
block|{       }
annotation|@
name|Override
specifier|public
name|void
name|setCacheOff
parameter_list|()
throws|throws
name|IOException
block|{       }
block|}
return|;
block|}
specifier|private
specifier|static
name|MultiResponse
name|buildMultiResponse
parameter_list|(
name|MultiRequest
name|req
parameter_list|)
block|{
name|MultiResponse
operator|.
name|Builder
name|builder
init|=
name|MultiResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|RegionActionResult
operator|.
name|Builder
name|regionActionResultBuilder
init|=
name|RegionActionResult
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ResultOrException
operator|.
name|Builder
name|roeBuilder
init|=
name|ResultOrException
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionAction
name|regionAction
range|:
name|req
operator|.
name|getRegionActionList
argument_list|()
control|)
block|{
name|regionActionResultBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|ClientProtos
operator|.
name|Action
name|action
range|:
name|regionAction
operator|.
name|getActionList
argument_list|()
control|)
block|{
name|roeBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|roeBuilder
operator|.
name|setResult
argument_list|(
name|ClientProtos
operator|.
name|Result
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|roeBuilder
operator|.
name|setIndex
argument_list|(
name|action
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|regionActionResultBuilder
operator|.
name|addResultOrException
argument_list|(
name|roeBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addRegionActionResult
argument_list|(
name|regionActionResultBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

