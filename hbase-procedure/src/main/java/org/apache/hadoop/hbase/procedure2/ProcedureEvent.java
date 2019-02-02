begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Basic ProcedureEvent that contains an "object", which can be a description or a reference to the  * resource to wait on, and a queue for suspended procedures.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ProcedureEvent
parameter_list|<
name|T
parameter_list|>
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
name|ProcedureEvent
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|T
name|object
decl_stmt|;
specifier|private
name|boolean
name|ready
init|=
literal|false
decl_stmt|;
specifier|private
name|ProcedureDeque
name|suspendedProcedures
init|=
operator|new
name|ProcedureDeque
argument_list|()
decl_stmt|;
specifier|public
name|ProcedureEvent
parameter_list|(
specifier|final
name|T
name|object
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isReady
parameter_list|()
block|{
return|return
name|ready
return|;
block|}
comment|/**    * @return true if event is not ready and adds procedure to suspended queue, else returns false.    */
specifier|public
specifier|synchronized
name|boolean
name|suspendIfNotReady
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ready
condition|)
block|{
name|suspendedProcedures
operator|.
name|addLast
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
return|return
operator|!
name|ready
return|;
block|}
comment|/** Mark the event as not ready. */
specifier|public
specifier|synchronized
name|void
name|suspend
parameter_list|()
block|{
name|ready
operator|=
literal|false
expr_stmt|;
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
literal|"Suspend "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Wakes up the suspended procedures by pushing them back into scheduler queues and sets the    * event as ready.    * See {@link #wakeInternal(AbstractProcedureScheduler)} for why this is not synchronized.    */
specifier|public
name|void
name|wake
parameter_list|(
name|AbstractProcedureScheduler
name|procedureScheduler
parameter_list|)
block|{
name|procedureScheduler
operator|.
name|wakeEvents
argument_list|(
operator|new
name|ProcedureEvent
index|[]
block|{
name|this
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Wakes up the suspended procedures only if the given {@code proc} is waiting on this event.    *<p/>    * Mainly used by region assignment to reject stale OpenRegionProcedure/CloseRegionProcedure. Use    * with caution as it will cause performance issue if there are lots of procedures waiting on the    * event.    */
specifier|public
specifier|synchronized
name|boolean
name|wakeIfSuspended
parameter_list|(
name|AbstractProcedureScheduler
name|procedureScheduler
parameter_list|,
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
block|{
if|if
condition|(
name|suspendedProcedures
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|p
lambda|->
name|p
operator|.
name|getProcId
argument_list|()
operator|==
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
condition|)
block|{
name|wake
argument_list|(
name|procedureScheduler
argument_list|)
block|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
end_class

begin_comment
comment|/**    * Wakes up all the given events and puts the procedures waiting on them back into    * ProcedureScheduler queues.    */
end_comment

begin_function
specifier|public
specifier|static
name|void
name|wakeEvents
parameter_list|(
name|AbstractProcedureScheduler
name|scheduler
parameter_list|,
name|ProcedureEvent
modifier|...
name|events
parameter_list|)
block|{
name|scheduler
operator|.
name|wakeEvents
argument_list|(
name|events
argument_list|)
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * Only to be used by ProcedureScheduler implementations.    * Reason: To wake up multiple events, locking sequence is    * schedLock --> synchronized (event)    * To wake up an event, both schedLock() and synchronized(event) are required.    * The order is schedLock() --> synchronized(event) because when waking up multiple events    * simultaneously, we keep the scheduler locked until all procedures suspended on these events    * have been added back to the queue (Maybe it's not required? Evaluate!)    * To avoid deadlocks, we want to keep the locking order same even when waking up single event.    * That's why, {@link #wake(AbstractProcedureScheduler)} above uses the same code path as used    * when waking up multiple events.    * Access should remain package-private.    */
end_comment

begin_function
annotation|@
name|VisibleForTesting
specifier|public
specifier|synchronized
name|void
name|wakeInternal
parameter_list|(
name|AbstractProcedureScheduler
name|procedureScheduler
parameter_list|)
block|{
if|if
condition|(
name|ready
operator|&&
operator|!
name|suspendedProcedures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found procedures suspended in a ready event! Size="
operator|+
name|suspendedProcedures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ready
operator|=
literal|true
expr_stmt|;
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
literal|"Unsuspend "
operator|+
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// wakeProcedure adds to the front of queue, so we start from last in the
comment|// waitQueue' queue, so that the procedure which was added first goes in the front for
comment|// the scheduler queue.
name|procedureScheduler
operator|.
name|addFront
argument_list|(
name|suspendedProcedures
operator|.
name|descendingIterator
argument_list|()
argument_list|)
expr_stmt|;
name|suspendedProcedures
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * Access to suspendedProcedures is 'synchronized' on this object, but it's fine to return it    * here for tests.    */
end_comment

begin_function
annotation|@
name|VisibleForTesting
specifier|public
name|ProcedureDeque
name|getSuspendedProcedures
parameter_list|()
block|{
return|return
name|suspendedProcedures
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" for "
operator|+
name|object
operator|+
literal|", ready="
operator|+
name|isReady
argument_list|()
operator|+
literal|", "
operator|+
name|suspendedProcedures
return|;
block|}
end_function

unit|}
end_unit

