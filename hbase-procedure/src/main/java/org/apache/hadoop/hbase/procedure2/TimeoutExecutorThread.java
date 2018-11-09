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
name|concurrent
operator|.
name|DelayQueue
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
name|util
operator|.
name|DelayedUtil
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
name|util
operator|.
name|DelayedUtil
operator|.
name|DelayedWithTimeout
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
name|ProcedureProtos
operator|.
name|ProcedureState
import|;
end_import

begin_comment
comment|/**  * Runs task on a period such as check for stuck workers.  * @see InlineChore  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|TimeoutExecutorThread
parameter_list|<
name|TEnvironment
parameter_list|>
extends|extends
name|StoppableThread
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
name|TimeoutExecutorThread
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ProcedureExecutor
argument_list|<
name|TEnvironment
argument_list|>
name|executor
decl_stmt|;
specifier|private
specifier|final
name|DelayQueue
argument_list|<
name|DelayedWithTimeout
argument_list|>
name|queue
init|=
operator|new
name|DelayQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|TimeoutExecutorThread
parameter_list|(
name|ProcedureExecutor
argument_list|<
name|TEnvironment
argument_list|>
name|executor
parameter_list|,
name|ThreadGroup
name|group
parameter_list|)
block|{
name|super
argument_list|(
name|group
argument_list|,
literal|"ProcExecTimeout"
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendStopSignal
parameter_list|()
block|{
name|queue
operator|.
name|add
argument_list|(
name|DelayedUtil
operator|.
name|DELAYED_POISON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|executor
operator|.
name|isRunning
argument_list|()
condition|)
block|{
specifier|final
name|DelayedWithTimeout
name|task
init|=
name|DelayedUtil
operator|.
name|takeWithoutInterrupt
argument_list|(
name|queue
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|==
literal|null
operator|||
name|task
operator|==
name|DelayedUtil
operator|.
name|DELAYED_POISON
condition|)
block|{
comment|// the executor may be shutting down,
comment|// and the task is just the shutdown request
continue|continue;
block|}
name|LOG
operator|.
name|trace
argument_list|(
literal|"Executing {}"
argument_list|,
name|task
argument_list|)
expr_stmt|;
comment|// execute the task
if|if
condition|(
name|task
operator|instanceof
name|InlineChore
condition|)
block|{
name|execInlineChore
argument_list|(
operator|(
name|InlineChore
operator|)
name|task
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|task
operator|instanceof
name|DelayedProcedure
condition|)
block|{
name|execDelayedProcedure
argument_list|(
operator|(
name|DelayedProcedure
argument_list|<
name|TEnvironment
argument_list|>
operator|)
name|task
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"CODE-BUG unknown timeout task type {}"
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|add
parameter_list|(
name|InlineChore
name|chore
parameter_list|)
block|{
name|chore
operator|.
name|refreshTimeout
argument_list|()
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|chore
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
name|procedure
parameter_list|)
block|{
assert|assert
name|procedure
operator|.
name|getState
argument_list|()
operator|==
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
assert|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ADDED {}; timeout={}, timestamp={}"
argument_list|,
name|procedure
argument_list|,
name|procedure
operator|.
name|getTimeout
argument_list|()
argument_list|,
name|procedure
operator|.
name|getTimeoutTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
operator|new
name|DelayedProcedure
argument_list|<>
argument_list|(
name|procedure
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|remove
parameter_list|(
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
name|procedure
parameter_list|)
block|{
return|return
name|queue
operator|.
name|remove
argument_list|(
operator|new
name|DelayedProcedure
argument_list|<>
argument_list|(
name|procedure
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|execInlineChore
parameter_list|(
name|InlineChore
name|chore
parameter_list|)
block|{
name|chore
operator|.
name|run
argument_list|()
expr_stmt|;
name|add
argument_list|(
name|chore
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|execDelayedProcedure
parameter_list|(
name|DelayedProcedure
argument_list|<
name|TEnvironment
argument_list|>
name|delayed
parameter_list|)
block|{
comment|// TODO: treat this as a normal procedure, add it to the scheduler and
comment|// let one of the workers handle it.
comment|// Today we consider ProcedureInMemoryChore as InlineChores
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
name|procedure
init|=
name|delayed
operator|.
name|getObject
argument_list|()
decl_stmt|;
if|if
condition|(
name|procedure
operator|instanceof
name|ProcedureInMemoryChore
condition|)
block|{
name|executeInMemoryChore
argument_list|(
operator|(
name|ProcedureInMemoryChore
argument_list|<
name|TEnvironment
argument_list|>
operator|)
name|procedure
argument_list|)
expr_stmt|;
comment|// if the procedure is in a waiting state again, put it back in the queue
name|procedure
operator|.
name|updateTimestamp
argument_list|()
expr_stmt|;
if|if
condition|(
name|procedure
operator|.
name|isWaiting
argument_list|()
condition|)
block|{
name|delayed
operator|.
name|setTimeout
argument_list|(
name|procedure
operator|.
name|getTimeoutTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|delayed
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|executeTimedoutProcedure
argument_list|(
name|procedure
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|executeInMemoryChore
parameter_list|(
name|ProcedureInMemoryChore
argument_list|<
name|TEnvironment
argument_list|>
name|chore
parameter_list|)
block|{
if|if
condition|(
operator|!
name|chore
operator|.
name|isWaiting
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// The ProcedureInMemoryChore is a special case, and it acts as a chore.
comment|// instead of bringing the Chore class in, we reuse this timeout thread for
comment|// this special case.
try|try
block|{
name|chore
operator|.
name|periodicExecute
argument_list|(
name|executor
operator|.
name|getEnvironment
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Ignoring {} exception: {}"
argument_list|,
name|chore
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|executeTimedoutProcedure
parameter_list|(
name|Procedure
argument_list|<
name|TEnvironment
argument_list|>
name|proc
parameter_list|)
block|{
comment|// The procedure received a timeout. if the procedure itself does not handle it,
comment|// call abort() and add the procedure back in the queue for rollback.
if|if
condition|(
name|proc
operator|.
name|setTimeoutFailure
argument_list|(
name|executor
operator|.
name|getEnvironment
argument_list|()
argument_list|)
condition|)
block|{
name|long
name|rootProcId
init|=
name|executor
operator|.
name|getRootProcedureId
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|RootProcedureState
argument_list|<
name|TEnvironment
argument_list|>
name|procStack
init|=
name|executor
operator|.
name|getProcStack
argument_list|(
name|rootProcId
argument_list|)
decl_stmt|;
name|procStack
operator|.
name|abort
argument_list|()
expr_stmt|;
name|executor
operator|.
name|getStore
argument_list|()
operator|.
name|update
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|executor
operator|.
name|getScheduler
argument_list|()
operator|.
name|addFront
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

