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
name|PeerProcedureInterface
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
name|ProcedurePrepareLatch
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
name|StateMachineProcedure
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
name|PeerProcedureStateData
import|;
end_import

begin_comment
comment|/**  * The base class for all replication peer related procedure.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractPeerProcedure
parameter_list|<
name|TState
parameter_list|>
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|TState
argument_list|>
implements|implements
name|PeerProcedureInterface
block|{
specifier|protected
name|String
name|peerId
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|locked
decl_stmt|;
comment|// used to keep compatible with old client where we can only returns after updateStorage.
specifier|protected
name|ProcedurePrepareLatch
name|latch
decl_stmt|;
specifier|protected
name|AbstractPeerProcedure
parameter_list|()
block|{   }
specifier|protected
name|AbstractPeerProcedure
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|this
operator|.
name|peerId
operator|=
name|peerId
expr_stmt|;
name|this
operator|.
name|latch
operator|=
name|ProcedurePrepareLatch
operator|.
name|createLatch
argument_list|(
literal|2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ProcedurePrepareLatch
name|getLatch
parameter_list|()
block|{
return|return
name|latch
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
return|return
name|peerId
return|;
block|}
annotation|@
name|Override
specifier|protected
name|LockState
name|acquireLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitPeerExclusiveLock
argument_list|(
name|this
argument_list|,
name|peerId
argument_list|)
condition|)
block|{
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
name|locked
operator|=
literal|true
expr_stmt|;
return|return
name|LockState
operator|.
name|LOCK_ACQUIRED
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|locked
operator|=
literal|false
expr_stmt|;
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakePeerExclusiveLock
argument_list|(
name|this
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|holdLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|hasLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|locked
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
name|PeerProcedureStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setPeerId
argument_list|(
name|peerId
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
name|peerId
operator|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|PeerProcedureStateData
operator|.
name|class
argument_list|)
operator|.
name|getPeerId
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
