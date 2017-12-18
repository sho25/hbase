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
name|util
operator|.
name|ArrayList
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
name|Set
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|ProcedureProtos
operator|.
name|ProcedureState
import|;
end_import

begin_comment
comment|/**  * Internal state of the ProcedureExecutor that describes the state of a "Root Procedure".  * A "Root Procedure" is a Procedure without parent, each subprocedure will be  * added to the "Root Procedure" stack (or rollback-stack).  *  * RootProcedureState is used and managed only by the ProcedureExecutor.  *    Long rootProcId = getRootProcedureId(proc);  *    rollbackStack.get(rootProcId).acquire(proc)  *    rollbackStack.get(rootProcId).release(proc)  *    ...  */
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
class|class
name|RootProcedureState
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
name|RootProcedureState
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
enum|enum
name|State
block|{
name|RUNNING
block|,
comment|// The Procedure is running or ready to run
name|FAILED
block|,
comment|// The Procedure failed, waiting for the rollback executing
name|ROLLINGBACK
block|,
comment|// The Procedure failed and the execution was rolledback
block|}
specifier|private
name|Set
argument_list|<
name|Procedure
argument_list|>
name|subprocs
init|=
literal|null
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|Procedure
argument_list|>
name|subprocStack
init|=
literal|null
decl_stmt|;
specifier|private
name|State
name|state
init|=
name|State
operator|.
name|RUNNING
decl_stmt|;
specifier|private
name|int
name|running
init|=
literal|0
decl_stmt|;
specifier|public
specifier|synchronized
name|boolean
name|isFailed
parameter_list|()
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|ROLLINGBACK
case|:
case|case
name|FAILED
case|:
return|return
literal|true
return|;
default|default:
break|break;
block|}
return|return
literal|false
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isRollingback
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|ROLLINGBACK
return|;
block|}
comment|/**    * Called by the ProcedureExecutor to mark rollback execution    */
specifier|protected
specifier|synchronized
name|boolean
name|setRollback
parameter_list|()
block|{
if|if
condition|(
name|running
operator|==
literal|0
operator|&&
name|state
operator|==
name|State
operator|.
name|FAILED
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|ROLLINGBACK
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Called by the ProcedureExecutor to mark rollback execution    */
specifier|protected
specifier|synchronized
name|void
name|unsetRollback
parameter_list|()
block|{
assert|assert
name|state
operator|==
name|State
operator|.
name|ROLLINGBACK
assert|;
name|state
operator|=
name|State
operator|.
name|FAILED
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|long
index|[]
name|getSubprocedureIds
parameter_list|()
block|{
if|if
condition|(
name|subprocs
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|int
name|index
init|=
literal|0
decl_stmt|;
specifier|final
name|long
index|[]
name|subIds
init|=
operator|new
name|long
index|[
name|subprocs
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|Procedure
name|proc
range|:
name|subprocs
control|)
block|{
name|subIds
index|[
name|index
operator|++
index|]
operator|=
name|proc
operator|.
name|getProcId
argument_list|()
expr_stmt|;
block|}
return|return
name|subIds
return|;
block|}
specifier|protected
specifier|synchronized
name|List
argument_list|<
name|Procedure
argument_list|>
name|getSubproceduresStack
parameter_list|()
block|{
return|return
name|subprocStack
return|;
block|}
specifier|protected
specifier|synchronized
name|RemoteProcedureException
name|getException
parameter_list|()
block|{
if|if
condition|(
name|subprocStack
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Procedure
name|proc
range|:
name|subprocStack
control|)
block|{
if|if
condition|(
name|proc
operator|.
name|hasException
argument_list|()
condition|)
block|{
return|return
name|proc
operator|.
name|getException
argument_list|()
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Called by the ProcedureExecutor to mark the procedure step as running.    */
specifier|protected
specifier|synchronized
name|boolean
name|acquire
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
name|state
operator|!=
name|State
operator|.
name|RUNNING
condition|)
return|return
literal|false
return|;
name|running
operator|++
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**    * Called by the ProcedureExecutor to mark the procedure step as finished.    */
specifier|protected
specifier|synchronized
name|void
name|release
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
name|running
operator|--
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|void
name|abort
parameter_list|()
block|{
if|if
condition|(
name|state
operator|==
name|State
operator|.
name|RUNNING
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|FAILED
expr_stmt|;
block|}
block|}
comment|/**    * Called by the ProcedureExecutor after the procedure step is completed,    * to add the step to the rollback list (or procedure stack)    */
specifier|protected
specifier|synchronized
name|void
name|addRollbackStep
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
name|proc
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|FAILED
expr_stmt|;
block|}
if|if
condition|(
name|subprocStack
operator|==
literal|null
condition|)
block|{
name|subprocStack
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|proc
operator|.
name|addStackIndex
argument_list|(
name|subprocStack
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|subprocStack
operator|.
name|add
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|void
name|addSubProcedure
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
operator|!
name|proc
operator|.
name|hasParent
argument_list|()
condition|)
return|return;
if|if
condition|(
name|subprocs
operator|==
literal|null
condition|)
block|{
name|subprocs
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|subprocs
operator|.
name|add
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
comment|/**    * Called on store load by the ProcedureExecutor to load part of the stack.    *    * Each procedure has its own stack-positions. Which means we have to write    * to the store only the Procedure we executed, and nothing else.    * on load we recreate the full stack by aggregating each procedure stack-positions.    */
specifier|protected
specifier|synchronized
name|void
name|loadStack
parameter_list|(
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
name|addSubProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|int
index|[]
name|stackIndexes
init|=
name|proc
operator|.
name|getStackIndexes
argument_list|()
decl_stmt|;
if|if
condition|(
name|stackIndexes
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|subprocStack
operator|==
literal|null
condition|)
block|{
name|subprocStack
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|int
name|diff
init|=
operator|(
literal|1
operator|+
name|stackIndexes
index|[
name|stackIndexes
operator|.
name|length
operator|-
literal|1
index|]
operator|)
operator|-
name|subprocStack
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|diff
operator|>
literal|0
condition|)
block|{
name|subprocStack
operator|.
name|ensureCapacity
argument_list|(
literal|1
operator|+
name|stackIndexes
index|[
name|stackIndexes
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
while|while
condition|(
name|diff
operator|--
operator|>
literal|0
condition|)
name|subprocStack
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|stackIndexes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|subprocStack
operator|.
name|set
argument_list|(
name|stackIndexes
index|[
name|i
index|]
argument_list|,
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|proc
operator|.
name|getState
argument_list|()
operator|==
name|ProcedureState
operator|.
name|ROLLEDBACK
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|ROLLINGBACK
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|proc
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|FAILED
expr_stmt|;
block|}
block|}
comment|/**    * Called on store load by the ProcedureExecutor to validate the procedure stack.    */
specifier|protected
specifier|synchronized
name|boolean
name|isValid
parameter_list|()
block|{
if|if
condition|(
name|subprocStack
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Procedure
name|proc
range|:
name|subprocStack
control|)
block|{
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

