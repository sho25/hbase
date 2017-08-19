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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|StateMachineProcedureData
import|;
end_import

begin_comment
comment|/**  * Procedure described by a series of steps.  *  *<p>The procedure implementor must have an enum of 'states', describing  * the various step of the procedure.  * Once the procedure is running, the procedure-framework will call executeFromState()  * using the 'state' provided by the user. The first call to executeFromState()  * will be performed with 'state = null'. The implementor can jump between  * states using setNextState(MyStateEnum.ordinal()).  * The rollback will call rollbackState() for each state that was executed, in reverse order.  */
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
name|StateMachineProcedure
parameter_list|<
name|TEnvironment
parameter_list|,
name|TState
parameter_list|>
extends|extends
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|StateMachineProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EOF_STATE
init|=
name|Integer
operator|.
name|MIN_VALUE
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|aborted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|Flow
name|stateFlow
init|=
name|Flow
operator|.
name|HAS_MORE_STATE
decl_stmt|;
specifier|private
name|int
name|stateCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
index|[]
name|states
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
argument_list|>
name|subProcList
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|int
name|getCycles
parameter_list|()
block|{
return|return
name|cycles
return|;
block|}
comment|/**    * Cycles on same state. Good for figuring if we are stuck.    */
specifier|private
name|int
name|cycles
init|=
literal|0
decl_stmt|;
comment|/**    * Ordinal of the previous state. So we can tell if we are progressing or not.    */
specifier|private
name|int
name|previousState
decl_stmt|;
specifier|protected
enum|enum
name|Flow
block|{
name|HAS_MORE_STATE
block|,
name|NO_MORE_STATE
block|,   }
comment|/**    * called to perform a single step of the specified 'state' of the procedure    * @param state state to execute    * @return Flow.NO_MORE_STATE if the procedure is completed,    *         Flow.HAS_MORE_STATE if there is another step.    */
specifier|protected
specifier|abstract
name|Flow
name|executeFromState
parameter_list|(
name|TEnvironment
name|env
parameter_list|,
name|TState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * called to perform the rollback of the specified state    * @param state state to rollback    * @throws IOException temporary failure, the rollback will retry later    */
specifier|protected
specifier|abstract
name|void
name|rollbackState
parameter_list|(
name|TEnvironment
name|env
parameter_list|,
name|TState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Convert an ordinal (or state id) to an Enum (or more descriptive) state object.    * @param stateId the ordinal() of the state enum (or state id)    * @return the state enum object    */
specifier|protected
specifier|abstract
name|TState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
function_decl|;
comment|/**    * Convert the Enum (or more descriptive) state object to an ordinal (or state id).    * @param state the state enum object    * @return stateId the ordinal() of the state enum (or state id)    */
specifier|protected
specifier|abstract
name|int
name|getStateId
parameter_list|(
name|TState
name|state
parameter_list|)
function_decl|;
comment|/**    * Return the initial state object that will be used for the first call to executeFromState().    * @return the initial state enum object    */
specifier|protected
specifier|abstract
name|TState
name|getInitialState
parameter_list|()
function_decl|;
comment|/**    * Set the next state for the procedure.    * @param state the state enum object    */
specifier|protected
name|void
name|setNextState
parameter_list|(
specifier|final
name|TState
name|state
parameter_list|)
block|{
name|setNextState
argument_list|(
name|getStateId
argument_list|(
name|state
argument_list|)
argument_list|)
expr_stmt|;
name|failIfAborted
argument_list|()
expr_stmt|;
block|}
comment|/**    * By default, the executor will try ro run all the steps of the procedure start to finish.    * Return true to make the executor yield between execution steps to    * give other procedures time to run their steps.    * @param state the state we are going to execute next.    * @return Return true if the executor should yield before the execution of the specified step.    *         Defaults to return false.    */
specifier|protected
name|boolean
name|isYieldBeforeExecuteFromState
parameter_list|(
name|TEnvironment
name|env
parameter_list|,
name|TState
name|state
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Add a child procedure to execute    * @param subProcedure the child procedure    */
specifier|protected
name|void
name|addChildProcedure
parameter_list|(
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
modifier|...
name|subProcedure
parameter_list|)
block|{
if|if
condition|(
name|subProcedure
operator|==
literal|null
condition|)
return|return;
specifier|final
name|int
name|len
init|=
name|subProcedure
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|len
operator|==
literal|0
condition|)
return|return;
if|if
condition|(
name|subProcList
operator|==
literal|null
condition|)
block|{
name|subProcList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|len
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
name|len
condition|;
operator|++
name|i
control|)
block|{
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
name|proc
init|=
name|subProcedure
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|proc
operator|.
name|hasOwner
argument_list|()
condition|)
name|proc
operator|.
name|setOwner
argument_list|(
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
name|subProcList
operator|.
name|add
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
specifier|final
name|TEnvironment
name|env
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
name|updateTimestamp
argument_list|()
expr_stmt|;
try|try
block|{
name|failIfAborted
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|hasMoreState
argument_list|()
operator|||
name|isFailed
argument_list|()
condition|)
return|return
literal|null
return|;
name|TState
name|state
init|=
name|getCurrentState
argument_list|()
decl_stmt|;
if|if
condition|(
name|stateCount
operator|==
literal|0
condition|)
block|{
name|setNextState
argument_list|(
name|getStateId
argument_list|(
name|state
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|state
operator|+
literal|" "
operator|+
name|this
operator|+
literal|"; cycles="
operator|+
name|this
operator|.
name|cycles
argument_list|)
expr_stmt|;
block|}
comment|// Keep running count of cycles
if|if
condition|(
name|getStateId
argument_list|(
name|state
argument_list|)
operator|!=
name|this
operator|.
name|previousState
condition|)
block|{
name|this
operator|.
name|previousState
operator|=
name|getStateId
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|this
operator|.
name|cycles
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|cycles
operator|++
expr_stmt|;
block|}
name|stateFlow
operator|=
name|executeFromState
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hasMoreState
argument_list|()
condition|)
name|setNextState
argument_list|(
name|EOF_STATE
argument_list|)
expr_stmt|;
if|if
condition|(
name|subProcList
operator|!=
literal|null
operator|&&
operator|!
name|subProcList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Procedure
index|[]
name|subProcedures
init|=
name|subProcList
operator|.
name|toArray
argument_list|(
operator|new
name|Procedure
index|[
name|subProcList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|subProcList
operator|=
literal|null
expr_stmt|;
return|return
name|subProcedures
return|;
block|}
return|return
operator|(
name|isWaiting
argument_list|()
operator|||
name|isFailed
argument_list|()
operator|||
operator|!
name|hasMoreState
argument_list|()
operator|)
condition|?
literal|null
else|:
operator|new
name|Procedure
index|[]
block|{
name|this
block|}
return|;
block|}
finally|finally
block|{
name|updateTimestamp
argument_list|()
expr_stmt|;
block|}
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
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|isEofState
argument_list|()
condition|)
name|stateCount
operator|--
expr_stmt|;
try|try
block|{
name|updateTimestamp
argument_list|()
expr_stmt|;
name|rollbackState
argument_list|(
name|env
argument_list|,
name|getCurrentState
argument_list|()
argument_list|)
expr_stmt|;
name|stateCount
operator|--
expr_stmt|;
block|}
finally|finally
block|{
name|updateTimestamp
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|isEofState
parameter_list|()
block|{
return|return
name|stateCount
operator|>
literal|0
operator|&&
name|states
index|[
name|stateCount
operator|-
literal|1
index|]
operator|==
name|EOF_STATE
return|;
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
specifier|final
name|boolean
name|isDebugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
specifier|final
name|TState
name|state
init|=
name|getCurrentState
argument_list|()
decl_stmt|;
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"abort requested for "
operator|+
name|this
operator|+
literal|" state="
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasMoreState
argument_list|()
condition|)
block|{
name|aborted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ignoring abort request on state="
operator|+
name|state
operator|+
literal|" for "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * If procedure has more states then abort it otherwise procedure is finished and abort can be    * ignored.    */
specifier|protected
specifier|final
name|void
name|failIfAborted
parameter_list|()
block|{
if|if
condition|(
name|aborted
operator|.
name|get
argument_list|()
condition|)
block|{
if|if
condition|(
name|hasMoreState
argument_list|()
condition|)
block|{
name|setAbortFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|"abort requested"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring abort request on state='"
operator|+
name|getCurrentState
argument_list|()
operator|+
literal|"' for "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Used by the default implementation of abort() to know if the current state can be aborted    * and rollback can be triggered.    */
specifier|protected
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|TState
name|state
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
name|isYieldAfterExecutionStep
parameter_list|(
specifier|final
name|TEnvironment
name|env
parameter_list|)
block|{
return|return
name|isYieldBeforeExecuteFromState
argument_list|(
name|env
argument_list|,
name|getCurrentState
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|hasMoreState
parameter_list|()
block|{
return|return
name|stateFlow
operator|!=
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
specifier|protected
name|TState
name|getCurrentState
parameter_list|()
block|{
return|return
name|stateCount
operator|>
literal|0
condition|?
name|getState
argument_list|(
name|states
index|[
name|stateCount
operator|-
literal|1
index|]
argument_list|)
else|:
name|getInitialState
argument_list|()
return|;
block|}
comment|/**    * Set the next state for the procedure.    * @param stateId the ordinal() of the state enum (or state id)    */
specifier|private
name|void
name|setNextState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
if|if
condition|(
name|states
operator|==
literal|null
operator|||
name|states
operator|.
name|length
operator|==
name|stateCount
condition|)
block|{
name|int
name|newCapacity
init|=
name|stateCount
operator|+
literal|8
decl_stmt|;
if|if
condition|(
name|states
operator|!=
literal|null
condition|)
block|{
name|states
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|states
argument_list|,
name|newCapacity
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|states
operator|=
operator|new
name|int
index|[
name|newCapacity
index|]
expr_stmt|;
block|}
block|}
name|states
index|[
name|stateCount
operator|++
index|]
operator|=
name|stateId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|toStringState
parameter_list|(
name|StringBuilder
name|builder
parameter_list|)
block|{
name|super
operator|.
name|toStringState
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isFinished
argument_list|()
operator|&&
operator|!
name|isEofState
argument_list|()
operator|&&
name|getCurrentState
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|getCurrentState
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|StateMachineProcedureData
operator|.
name|Builder
name|data
init|=
name|StateMachineProcedureData
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|stateCount
condition|;
operator|++
name|i
control|)
block|{
name|data
operator|.
name|addState
argument_list|(
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|serialize
argument_list|(
name|data
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
name|StateMachineProcedureData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|StateMachineProcedureData
operator|.
name|class
argument_list|)
decl_stmt|;
name|stateCount
operator|=
name|data
operator|.
name|getStateCount
argument_list|()
expr_stmt|;
if|if
condition|(
name|stateCount
operator|>
literal|0
condition|)
block|{
name|states
operator|=
operator|new
name|int
index|[
name|stateCount
index|]
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
name|stateCount
condition|;
operator|++
name|i
control|)
block|{
name|states
index|[
name|i
index|]
operator|=
name|data
operator|.
name|getState
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isEofState
argument_list|()
condition|)
block|{
name|stateFlow
operator|=
name|Flow
operator|.
name|NO_MORE_STATE
expr_stmt|;
block|}
block|}
else|else
block|{
name|states
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

