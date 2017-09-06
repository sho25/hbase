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
name|HRegionInfo
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
name|MetaTableAccessor
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
name|TableNotFoundException
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_comment
comment|/**  * Base class for all the Region procedures that want to use a StateMachine.  * It provides some basic helpers like basic locking, sync latch, and toStringClassDetails().  * Defaults to holding the lock for the life of the procedure.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractStateMachineRegionProcedure
parameter_list|<
name|TState
parameter_list|>
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|TState
argument_list|>
block|{
specifier|private
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|lock
init|=
literal|false
decl_stmt|;
specifier|public
name|AbstractStateMachineRegionProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|this
operator|.
name|hri
operator|=
name|hri
expr_stmt|;
block|}
specifier|public
name|AbstractStateMachineRegionProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return The HRegionInfo of the region we are operating on.    */
specifier|protected
name|HRegionInfo
name|getRegion
parameter_list|()
block|{
return|return
name|this
operator|.
name|hri
return|;
block|}
comment|/**    * Used when deserializing. Otherwise, DON'T TOUCH IT!    */
specifier|protected
name|void
name|setRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|this
operator|.
name|hri
operator|=
name|hri
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|getRegion
argument_list|()
operator|.
name|getTable
argument_list|()
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
name|super
operator|.
name|toStringClassDetails
argument_list|(
name|sb
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", region="
argument_list|)
operator|.
name|append
argument_list|(
name|getRegion
argument_list|()
operator|.
name|getShortNameToLog
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check whether a table is modifiable - exists and either offline or online with config set    * @param env MasterProcedureEnv    * @throws IOException    */
annotation|@
name|Override
specifier|protected
name|void
name|checkTableModifiable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Checks whether the table exists
if|if
condition|(
operator|!
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
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
name|LockState
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
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
if|if
condition|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitRegions
argument_list|(
name|this
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getRegion
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
name|this
operator|.
name|lock
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
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|this
operator|.
name|lock
operator|=
literal|false
expr_stmt|;
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeRegions
argument_list|(
name|this
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|hasLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|this
operator|.
name|lock
return|;
block|}
specifier|protected
name|void
name|setFailure
parameter_list|(
name|Throwable
name|cause
parameter_list|)
block|{
name|super
operator|.
name|setFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|cause
argument_list|)
expr_stmt|;
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
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|getRegion
argument_list|()
argument_list|)
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
name|this
operator|.
name|hri
operator|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|serializer
operator|.
name|deserialize
argument_list|(
name|HBaseProtos
operator|.
name|RegionInfo
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

