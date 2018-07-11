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
name|Iterator
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
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|ReentrantLock
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
name|TimeUnit
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractProcedureScheduler
implements|implements
name|ProcedureScheduler
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
name|AbstractProcedureScheduler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|schedulerLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Condition
name|schedWaitCond
init|=
name|schedulerLock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|running
init|=
literal|false
decl_stmt|;
comment|// TODO: metrics
specifier|private
name|long
name|pollCalls
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|nullPollCalls
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
name|running
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
name|running
operator|=
literal|false
expr_stmt|;
name|schedWaitCond
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|signalAll
parameter_list|()
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
name|schedWaitCond
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|//  Add related
comment|// ==========================================================================
comment|/**    * Add the procedure to the queue.    * NOTE: this method is called with the sched lock held.    * @param procedure the Procedure to add    * @param addFront true if the item should be added to the front of the queue    */
specifier|protected
specifier|abstract
name|void
name|enqueue
parameter_list|(
name|Procedure
name|procedure
parameter_list|,
name|boolean
name|addFront
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|addFront
parameter_list|(
specifier|final
name|Procedure
name|procedure
parameter_list|)
block|{
name|push
argument_list|(
name|procedure
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addFront
parameter_list|(
name|Iterator
argument_list|<
name|Procedure
argument_list|>
name|procedureIterator
parameter_list|)
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|procedureIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Procedure
name|procedure
init|=
name|procedureIterator
operator|.
name|next
argument_list|()
decl_stmt|;
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
literal|"Wake "
operator|+
name|procedure
argument_list|)
expr_stmt|;
block|}
name|push
argument_list|(
name|procedure
argument_list|,
comment|/* addFront= */
literal|true
argument_list|,
comment|/* notify= */
literal|false
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|wakePollIfNeeded
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addBack
parameter_list|(
specifier|final
name|Procedure
name|procedure
parameter_list|)
block|{
name|push
argument_list|(
name|procedure
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|push
parameter_list|(
specifier|final
name|Procedure
name|procedure
parameter_list|,
specifier|final
name|boolean
name|addFront
parameter_list|,
specifier|final
name|boolean
name|notify
parameter_list|)
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
name|enqueue
argument_list|(
name|procedure
argument_list|,
name|addFront
argument_list|)
expr_stmt|;
if|if
condition|(
name|notify
condition|)
block|{
name|schedWaitCond
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|//  Poll related
comment|// ==========================================================================
comment|/**    * Fetch one Procedure from the queue    * NOTE: this method is called with the sched lock held.    * @return the Procedure to execute, or null if nothing is available.    */
specifier|protected
specifier|abstract
name|Procedure
name|dequeue
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|Procedure
name|poll
parameter_list|()
block|{
return|return
name|poll
argument_list|(
operator|-
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Procedure
name|poll
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|poll
argument_list|(
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
literal|"WA_AWAIT_NOT_IN_LOOP"
argument_list|)
specifier|public
name|Procedure
name|poll
parameter_list|(
specifier|final
name|long
name|nanos
parameter_list|)
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|running
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"the scheduler is not running"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|queueHasRunnables
argument_list|()
condition|)
block|{
comment|// WA_AWAIT_NOT_IN_LOOP: we are not in a loop because we want the caller
comment|// to take decisions after a wake/interruption.
if|if
condition|(
name|nanos
operator|<
literal|0
condition|)
block|{
name|schedWaitCond
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|schedWaitCond
operator|.
name|awaitNanos
argument_list|(
name|nanos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|queueHasRunnables
argument_list|()
condition|)
block|{
name|nullPollCalls
operator|++
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|final
name|Procedure
name|pollResult
init|=
name|dequeue
argument_list|()
decl_stmt|;
name|pollCalls
operator|++
expr_stmt|;
name|nullPollCalls
operator|+=
operator|(
name|pollResult
operator|==
literal|null
operator|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
return|return
name|pollResult
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|nullPollCalls
operator|++
expr_stmt|;
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|//  Utils
comment|// ==========================================================================
comment|/**    * Returns the number of elements in this queue.    * NOTE: this method is called with the sched lock held.    * @return the number of elements in this queue.    */
specifier|protected
specifier|abstract
name|int
name|queueSize
parameter_list|()
function_decl|;
comment|/**    * Returns true if there are procedures available to process.    * NOTE: this method is called with the sched lock held.    * @return true if there are procedures available to process, otherwise false.    */
specifier|protected
specifier|abstract
name|boolean
name|queueHasRunnables
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|queueSize
argument_list|()
return|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasRunnables
parameter_list|()
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|queueHasRunnables
argument_list|()
return|;
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// ============================================================================
comment|//  TODO: Metrics
comment|// ============================================================================
specifier|public
name|long
name|getPollCalls
parameter_list|()
block|{
return|return
name|pollCalls
return|;
block|}
specifier|public
name|long
name|getNullPollCalls
parameter_list|()
block|{
return|return
name|nullPollCalls
return|;
block|}
comment|// ==========================================================================
comment|//  Procedure Events
comment|// ==========================================================================
comment|/**    * Wake up all of the given events.    * Note that we first take scheduler lock and then wakeInternal() synchronizes on the event.    * Access should remain package-private. Use ProcedureEvent class to wake/suspend events.    * @param events the list of events to wake    */
name|void
name|wakeEvents
parameter_list|(
name|ProcedureEvent
index|[]
name|events
parameter_list|)
block|{
name|schedLock
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|ProcedureEvent
name|event
range|:
name|events
control|)
block|{
if|if
condition|(
name|event
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|event
operator|.
name|wakeInternal
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|schedUnlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Wakes up given waiting procedures by pushing them back into scheduler queues.    * @return size of given {@code waitQueue}.    */
specifier|protected
name|int
name|wakeWaitingProcedures
parameter_list|(
name|LockAndQueue
name|lockAndQueue
parameter_list|)
block|{
return|return
name|lockAndQueue
operator|.
name|wakeWaitingProcedures
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|protected
name|void
name|waitProcedure
parameter_list|(
name|LockAndQueue
name|lockAndQueue
parameter_list|,
specifier|final
name|Procedure
name|proc
parameter_list|)
block|{
name|lockAndQueue
operator|.
name|addLast
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|wakeProcedure
parameter_list|(
specifier|final
name|Procedure
name|procedure
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Wake {}"
argument_list|,
name|procedure
argument_list|)
expr_stmt|;
name|push
argument_list|(
name|procedure
argument_list|,
comment|/* addFront= */
literal|true
argument_list|,
comment|/* notify= */
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Internal helpers
comment|// ==========================================================================
specifier|protected
name|void
name|schedLock
parameter_list|()
block|{
name|schedulerLock
operator|.
name|lock
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|schedUnlock
parameter_list|()
block|{
name|schedulerLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|wakePollIfNeeded
parameter_list|(
specifier|final
name|int
name|waitingCount
parameter_list|)
block|{
if|if
condition|(
name|waitingCount
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|waitingCount
operator|==
literal|1
condition|)
block|{
name|schedWaitCond
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|schedWaitCond
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

