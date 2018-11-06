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
name|procedure2
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
name|Objects
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
name|User
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
name|NonceKey
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
name|ProcedureProtos
operator|.
name|ProcedureState
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FailedProcedure
parameter_list|<
name|TEnvironment
parameter_list|>
extends|extends
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
block|{
specifier|private
name|String
name|procName
decl_stmt|;
specifier|public
name|FailedProcedure
parameter_list|()
block|{   }
specifier|public
name|FailedProcedure
parameter_list|(
name|long
name|procId
parameter_list|,
name|String
name|procName
parameter_list|,
name|User
name|owner
parameter_list|,
name|NonceKey
name|nonceKey
parameter_list|,
name|IOException
name|exception
parameter_list|)
block|{
name|this
operator|.
name|procName
operator|=
name|procName
expr_stmt|;
name|setProcId
argument_list|(
name|procId
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureState
operator|.
name|ROLLEDBACK
argument_list|)
expr_stmt|;
name|setOwner
argument_list|(
name|owner
argument_list|)
expr_stmt|;
name|setNonceKey
argument_list|(
name|nonceKey
argument_list|)
expr_stmt|;
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|setSubmittedTime
argument_list|(
name|currentTime
argument_list|)
expr_stmt|;
name|setLastUpdate
argument_list|(
name|currentTime
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|""
argument_list|)
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getProcName
parameter_list|()
block|{
return|return
name|procName
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
index|[]
name|execute
parameter_list|(
name|TEnvironment
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|TEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|TEnvironment
name|env
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
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
block|{   }
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
block|{   }
block|}
end_class

end_unit

