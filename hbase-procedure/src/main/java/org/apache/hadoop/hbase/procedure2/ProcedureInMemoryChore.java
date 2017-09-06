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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Special procedure used as a chore.  * Instead of bringing the Chore class in (dependencies reason),  * we reuse the executor timeout thread for this special case.  *  * The assumption is that procedure is used as hook to dispatch other procedures  * or trigger some cleanups. It does not store state in the ProcedureStore.  * this is just for in-memory chore executions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|ProcedureInMemoryChore
parameter_list|<
name|TEnvironment
parameter_list|>
extends|extends
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
block|{
specifier|protected
name|ProcedureInMemoryChore
parameter_list|(
specifier|final
name|int
name|timeoutMsec
parameter_list|)
block|{
name|setTimeout
argument_list|(
name|timeoutMsec
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|void
name|periodicExecute
parameter_list|(
specifier|final
name|TEnvironment
name|env
parameter_list|)
function_decl|;
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
specifier|final
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
name|rollback
parameter_list|(
specifier|final
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
name|boolean
name|abort
parameter_list|(
specifier|final
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

