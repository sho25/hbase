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
name|FSUtils
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
name|ServerRegionReplicaUtil
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

begin_comment
comment|/**  * Base class for all the Table procedures that want to use a StateMachineProcedure.  * It provides helpers like basic locking, sync latch, and toStringClassDetails().  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractStateMachineTableProcedure
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
comment|// used for compatibility with old clients
specifier|private
specifier|final
name|ProcedurePrepareLatch
name|syncLatch
decl_stmt|;
specifier|private
name|User
name|user
decl_stmt|;
specifier|protected
name|AbstractStateMachineTableProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|syncLatch
operator|=
literal|null
expr_stmt|;
block|}
specifier|protected
name|AbstractStateMachineTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param env Uses this to set Procedure Owner at least.    */
specifier|protected
name|AbstractStateMachineTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ProcedurePrepareLatch
name|latch
parameter_list|)
block|{
if|if
condition|(
name|env
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|user
operator|=
name|env
operator|.
name|getRequestUser
argument_list|()
expr_stmt|;
name|this
operator|.
name|setOwner
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
comment|// used for compatibility with clients without procedures
comment|// they need a sync TableExistsException, TableNotFoundException, TableNotDisabledException, ...
name|this
operator|.
name|syncLatch
operator|=
name|latch
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|TableName
name|getTableName
parameter_list|()
function_decl|;
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
literal|" table="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
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
name|waitTableExclusiveLock
argument_list|(
name|this
argument_list|,
name|getTableName
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
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeTableExclusiveLock
argument_list|(
name|this
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|User
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
specifier|protected
name|void
name|setUser
parameter_list|(
specifier|final
name|User
name|user
parameter_list|)
block|{
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
specifier|protected
name|void
name|releaseSyncLatch
parameter_list|()
block|{
name|ProcedurePrepareLatch
operator|.
name|releaseLatch
argument_list|(
name|syncLatch
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check whether a table is modifiable - exists and either offline or online with config set    * @param env MasterProcedureEnv    * @throws IOException    */
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
specifier|protected
specifier|final
name|Path
name|getRegionDir
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterFileSystem
name|mfs
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|ServerRegionReplicaUtil
operator|.
name|getRegionInfoForFs
argument_list|(
name|region
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

