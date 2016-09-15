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
name|procedure2
operator|.
name|StateMachineProcedure
import|;
end_import

begin_comment
comment|/**  * Base class for all the Namespace procedures that want to use a StateMachineProcedure.  * It provide some basic helpers like basic locking and basic toStringClassDetails().  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractStateMachineNamespaceProcedure
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
name|TableProcedureInterface
block|{
specifier|protected
name|AbstractStateMachineNamespaceProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
block|}
specifier|protected
name|AbstractStateMachineNamespaceProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|this
operator|.
name|setOwner
argument_list|(
name|env
operator|.
name|getRequestUser
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|String
name|getNamespaceName
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|TableOperationType
name|getTableOperationType
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|toStringClassDetails
parameter_list|(
specifier|final
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
literal|" (namespace="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getNamespaceName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|env
operator|.
name|waitInitialized
argument_list|(
name|this
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
name|env
operator|.
name|getProcedureQueue
argument_list|()
operator|.
name|tryAcquireNamespaceExclusiveLock
argument_list|(
name|this
argument_list|,
name|getNamespaceName
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureQueue
argument_list|()
operator|.
name|releaseNamespaceExclusiveLock
argument_list|(
name|this
argument_list|,
name|getNamespaceName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

