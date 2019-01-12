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
name|ProcedureSuspendedException
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
name|ProcedureUtil
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
name|ProcedureYieldException
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
name|StateMachineProcedure
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
name|quotas
operator|.
name|RpcThrottleStorage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|SwitchRpcThrottleState
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
name|MasterProcedureProtos
operator|.
name|SwitchRpcThrottleStateData
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
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * The procedure to switch rpc throttle  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SwitchRpcThrottleProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|SwitchRpcThrottleState
argument_list|>
implements|implements
name|ServerProcedureInterface
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SwitchRpcThrottleProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
name|RpcThrottleStorage
name|rpcThrottleStorage
decl_stmt|;
name|boolean
name|rpcThrottleEnabled
decl_stmt|;
name|ProcedurePrepareLatch
name|syncLatch
decl_stmt|;
name|ServerName
name|serverName
decl_stmt|;
name|int
name|attempts
decl_stmt|;
specifier|public
name|SwitchRpcThrottleProcedure
parameter_list|()
block|{   }
specifier|public
name|SwitchRpcThrottleProcedure
parameter_list|(
name|RpcThrottleStorage
name|rpcThrottleStorage
parameter_list|,
name|boolean
name|rpcThrottleEnabled
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|ProcedurePrepareLatch
name|syncLatch
parameter_list|)
block|{
name|this
operator|.
name|rpcThrottleStorage
operator|=
name|rpcThrottleStorage
expr_stmt|;
name|this
operator|.
name|syncLatch
operator|=
name|syncLatch
expr_stmt|;
name|this
operator|.
name|rpcThrottleEnabled
operator|=
name|rpcThrottleEnabled
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Flow
name|executeFromState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|SwitchRpcThrottleState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|UPDATE_SWITCH_RPC_THROTTLE_STORAGE
case|:
try|try
block|{
name|switchThrottleState
argument_list|(
name|env
argument_list|,
name|rpcThrottleEnabled
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|this
operator|.
name|attempts
operator|++
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to store rpc throttle value {}, sleep {} secs and retry"
argument_list|,
name|rpcThrottleEnabled
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setTimeout
argument_list|(
name|Math
operator|.
name|toIntExact
argument_list|(
name|backoff
argument_list|)
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
name|skipPersistence
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
name|setNextState
argument_list|(
name|SwitchRpcThrottleState
operator|.
name|SWITCH_RPC_THROTTLE_ON_RS
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SWITCH_RPC_THROTTLE_ON_RS
case|:
name|SwitchRpcThrottleRemoteProcedure
index|[]
name|subProcedures
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
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
name|map
argument_list|(
name|sn
lambda|->
operator|new
name|SwitchRpcThrottleRemoteProcedure
argument_list|(
name|sn
argument_list|,
name|rpcThrottleEnabled
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|(
name|SwitchRpcThrottleRemoteProcedure
index|[]
operator|::
operator|new
argument_list|)
decl_stmt|;
name|addChildProcedure
argument_list|(
name|subProcedures
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|SwitchRpcThrottleState
operator|.
name|POST_SWITCH_RPC_THROTTLE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|POST_SWITCH_RPC_THROTTLE
case|:
name|ProcedurePrepareLatch
operator|.
name|releaseLatch
argument_list|(
name|syncLatch
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"unhandled state="
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|SwitchRpcThrottleState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{   }
annotation|@
name|Override
specifier|protected
name|SwitchRpcThrottleState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|SwitchRpcThrottleState
operator|.
name|forNumber
argument_list|(
name|stateId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
name|SwitchRpcThrottleState
name|throttleState
parameter_list|)
block|{
return|return
name|throttleState
operator|.
name|getNumber
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|SwitchRpcThrottleState
name|getInitialState
parameter_list|()
block|{
return|return
name|SwitchRpcThrottleState
operator|.
name|UPDATE_SWITCH_RPC_THROTTLE_STORAGE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|SwitchRpcThrottleState
name|getCurrentState
parameter_list|()
block|{
return|return
name|super
operator|.
name|getCurrentState
argument_list|()
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
name|super
operator|.
name|serializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|SwitchRpcThrottleStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRpcThrottleEnabled
argument_list|(
name|rpcThrottleEnabled
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
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
name|super
operator|.
name|deserializeStateData
argument_list|(
name|serializer
argument_list|)
expr_stmt|;
name|SwitchRpcThrottleStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|SwitchRpcThrottleStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|rpcThrottleEnabled
operator|=
name|data
operator|.
name|getRpcThrottleEnabled
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|serverName
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
name|ServerOperationType
operator|.
name|SWITCH_RPC_THROTTLE
return|;
block|}
specifier|public
name|void
name|switchThrottleState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|boolean
name|rpcThrottleEnabled
parameter_list|)
throws|throws
name|IOException
block|{
name|rpcThrottleStorage
operator|.
name|switchRpcThrottle
argument_list|(
name|rpcThrottleEnabled
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|toStringClassDetails
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" server="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", rpcThrottleEnabled="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rpcThrottleEnabled
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
