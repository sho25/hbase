begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ScheduledThreadPoolExecutor
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

begin_import
import|import
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
comment|/**  * ScheduledChore is a task performed on a period in hbase. ScheduledChores become active once  * scheduled with a {@link ChoreService} via {@link ChoreService#scheduleChore(ScheduledChore)}. The  * chore is run in a {@link ScheduledThreadPoolExecutor} and competes with other ScheduledChores for  * access to the threads in the core thread pool. If an unhandled exception occurs, the chore  * cancellation is logged. Implementers should consider whether or not the Chore will be able to  * execute within the defined period. It is bad practice to define a ScheduledChore whose execution  * time exceeds its period since it will try to hog one of the threads in the {@link ChoreService}'s  * thread pool.  *<p>  * Don't subclass ScheduledChore if the task relies on being woken up for something to do, such as  * an entry being added to a queue, etc.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|ScheduledChore
implements|implements
name|Runnable
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
name|ScheduledChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
comment|/**    * Default values for scheduling parameters should they be excluded during construction    */
specifier|private
specifier|final
specifier|static
name|TimeUnit
name|DEFAULT_TIME_UNIT
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|long
name|DEFAULT_INITIAL_DELAY
init|=
literal|0
decl_stmt|;
comment|/**    * Scheduling parameters. Used by ChoreService when scheduling the chore to run periodically    */
specifier|private
specifier|final
name|int
name|period
decl_stmt|;
comment|// in TimeUnit units
specifier|private
specifier|final
name|TimeUnit
name|timeUnit
decl_stmt|;
specifier|private
specifier|final
name|long
name|initialDelay
decl_stmt|;
comment|// in TimeUnit units
comment|/**    * Interface to the ChoreService that this ScheduledChore is scheduled with. null if the chore is    * not scheduled.    */
specifier|private
name|ChoreServicer
name|choreServicer
decl_stmt|;
comment|/**    * Variables that encapsulate the meaningful state information    */
specifier|private
name|long
name|timeOfLastRun
init|=
operator|-
literal|1
decl_stmt|;
comment|// system time millis
specifier|private
name|long
name|timeOfThisRun
init|=
operator|-
literal|1
decl_stmt|;
comment|// system time millis
specifier|private
name|boolean
name|initialChoreComplete
init|=
literal|false
decl_stmt|;
comment|/**    * A means by which a ScheduledChore can be stopped. Once a chore recognizes that it has been    * stopped, it will cancel itself. This is particularly useful in the case where a single stopper    * instance is given to multiple chores. In such a case, a single {@link Stoppable#stop(String)}    * command can cause many chores to stop together.    */
specifier|private
specifier|final
name|Stoppable
name|stopper
decl_stmt|;
interface|interface
name|ChoreServicer
block|{
comment|/**      * Cancel any ongoing schedules that this chore has with the implementer of this interface.      */
specifier|public
name|void
name|cancelChore
parameter_list|(
name|ScheduledChore
name|chore
parameter_list|)
function_decl|;
specifier|public
name|void
name|cancelChore
parameter_list|(
name|ScheduledChore
name|chore
parameter_list|,
name|boolean
name|mayInterruptIfRunning
parameter_list|)
function_decl|;
comment|/**      * @return true when the chore is scheduled with the implementer of this interface      */
specifier|public
name|boolean
name|isChoreScheduled
parameter_list|(
name|ScheduledChore
name|chore
parameter_list|)
function_decl|;
comment|/**      * This method tries to execute the chore immediately. If the chore is executing at the time of      * this call, the chore will begin another execution as soon as the current execution finishes      *<p>      * If the chore is not scheduled with a ChoreService, this call will fail.      * @return false when the chore could not be triggered immediately      */
specifier|public
name|boolean
name|triggerNow
parameter_list|(
name|ScheduledChore
name|chore
parameter_list|)
function_decl|;
comment|/**      * A callback that tells the implementer of this interface that one of the scheduled chores is      * missing its start time. The implication of a chore missing its start time is that the      * service's current means of scheduling may not be sufficient to handle the number of ongoing      * chores (the other explanation is that the chore's execution time is greater than its      * scheduled period). The service should try to increase its concurrency when this callback is      * received.      * @param chore The chore that missed its start time      */
specifier|public
name|void
name|onChoreMissedStartTime
parameter_list|(
name|ScheduledChore
name|chore
parameter_list|)
function_decl|;
block|}
comment|/**    * This constructor is for test only. It allows us to create an object and to call chore() on it.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|VisibleForTesting
specifier|protected
name|ScheduledChore
parameter_list|()
block|{
name|this
operator|.
name|name
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|stopper
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|period
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|initialDelay
operator|=
name|DEFAULT_INITIAL_DELAY
expr_stmt|;
name|this
operator|.
name|timeUnit
operator|=
name|DEFAULT_TIME_UNIT
expr_stmt|;
block|}
comment|/**    * @param name Name assigned to Chore. Useful for identification amongst chores of the same type    * @param stopper When {@link Stoppable#isStopped()} is true, this chore will cancel and cleanup    * @param period Period in millis with which this Chore repeats execution when scheduled.    */
specifier|public
name|ScheduledChore
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|int
name|period
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|stopper
argument_list|,
name|period
argument_list|,
name|DEFAULT_INITIAL_DELAY
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param name Name assigned to Chore. Useful for identification amongst chores of the same type    * @param stopper When {@link Stoppable#isStopped()} is true, this chore will cancel and cleanup    * @param period Period in millis with which this Chore repeats execution when scheduled.    * @param initialDelay Delay before this Chore begins to execute once it has been scheduled. A    *          value of 0 means the chore will begin to execute immediately. Negative delays are    *          invalid and will be corrected to a value of 0.    */
specifier|public
name|ScheduledChore
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|long
name|initialDelay
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|stopper
argument_list|,
name|period
argument_list|,
name|initialDelay
argument_list|,
name|DEFAULT_TIME_UNIT
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param name Name assigned to Chore. Useful for identification amongst chores of the same type    * @param stopper When {@link Stoppable#isStopped()} is true, this chore will cancel and cleanup    * @param period Period in Timeunit unit with which this Chore repeats execution when scheduled.    * @param initialDelay Delay in Timeunit unit before this Chore begins to execute once it has been    *          scheduled. A value of 0 means the chore will begin to execute immediately. Negative    *          delays are invalid and will be corrected to a value of 0.    * @param unit The unit that is used to measure period and initialDelay    */
specifier|public
name|ScheduledChore
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
name|Stoppable
name|stopper
parameter_list|,
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|long
name|initialDelay
parameter_list|,
specifier|final
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|stopper
operator|=
name|stopper
expr_stmt|;
name|this
operator|.
name|period
operator|=
name|period
expr_stmt|;
name|this
operator|.
name|initialDelay
operator|=
name|initialDelay
operator|<
literal|0
condition|?
literal|0
else|:
name|initialDelay
expr_stmt|;
name|this
operator|.
name|timeUnit
operator|=
name|unit
expr_stmt|;
block|}
comment|/**    * @see java.lang.Runnable#run()    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|updateTimeTrackingBeforeRun
argument_list|()
expr_stmt|;
if|if
condition|(
name|missedStartTime
argument_list|()
operator|&&
name|isScheduled
argument_list|()
condition|)
block|{
name|onChoreMissedStartTime
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|info
argument_list|(
literal|"Chore: "
operator|+
name|getName
argument_list|()
operator|+
literal|" missed its start time"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|stopper
operator|.
name|isStopped
argument_list|()
operator|||
operator|!
name|isScheduled
argument_list|()
condition|)
block|{
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|info
argument_list|(
literal|"Chore: "
operator|+
name|getName
argument_list|()
operator|+
literal|" was stopped"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
if|if
condition|(
operator|!
name|initialChoreComplete
condition|)
block|{
name|initialChoreComplete
operator|=
name|initialChore
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|chore
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isErrorEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught error"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|stopper
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Update our time tracking members. Called at the start of an execution of this chore's run()    * method so that a correct decision can be made as to whether or not we missed the start time    */
specifier|private
specifier|synchronized
name|void
name|updateTimeTrackingBeforeRun
parameter_list|()
block|{
name|timeOfLastRun
operator|=
name|timeOfThisRun
expr_stmt|;
name|timeOfThisRun
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
comment|/**    * Notify the ChoreService that this chore has missed its start time. Allows the ChoreService to    * make the decision as to whether or not it would be worthwhile to increase the number of core    * pool threads    */
specifier|private
specifier|synchronized
name|void
name|onChoreMissedStartTime
parameter_list|()
block|{
if|if
condition|(
name|choreServicer
operator|!=
literal|null
condition|)
name|choreServicer
operator|.
name|onChoreMissedStartTime
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return How long in millis has it been since this chore last run. Useful for checking if the    *         chore has missed its scheduled start time by too large of a margin    */
specifier|synchronized
name|long
name|getTimeBetweenRuns
parameter_list|()
block|{
return|return
name|timeOfThisRun
operator|-
name|timeOfLastRun
return|;
block|}
comment|/**    * @return true when the time between runs exceeds the acceptable threshold    */
specifier|private
specifier|synchronized
name|boolean
name|missedStartTime
parameter_list|()
block|{
return|return
name|isValidTime
argument_list|(
name|timeOfLastRun
argument_list|)
operator|&&
name|isValidTime
argument_list|(
name|timeOfThisRun
argument_list|)
operator|&&
name|getTimeBetweenRuns
argument_list|()
operator|>
name|getMaximumAllowedTimeBetweenRuns
argument_list|()
return|;
block|}
comment|/**    * @return max allowed time in millis between runs.    */
specifier|private
name|double
name|getMaximumAllowedTimeBetweenRuns
parameter_list|()
block|{
comment|// Threshold used to determine if the Chore's current run started too late
return|return
literal|1.5
operator|*
name|timeUnit
operator|.
name|toMillis
argument_list|(
name|period
argument_list|)
return|;
block|}
comment|/**    * @param time in system millis    * @return true if time is earlier or equal to current milli time    */
specifier|private
specifier|synchronized
name|boolean
name|isValidTime
parameter_list|(
specifier|final
name|long
name|time
parameter_list|)
block|{
return|return
name|time
operator|>
literal|0
operator|&&
name|time
operator|<=
name|System
operator|.
name|currentTimeMillis
argument_list|()
return|;
block|}
comment|/**    * @return false when the Chore is not currently scheduled with a ChoreService    */
specifier|public
specifier|synchronized
name|boolean
name|triggerNow
parameter_list|()
block|{
if|if
condition|(
name|choreServicer
operator|!=
literal|null
condition|)
block|{
return|return
name|choreServicer
operator|.
name|triggerNow
argument_list|(
name|this
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|synchronized
name|void
name|setChoreServicer
parameter_list|(
name|ChoreServicer
name|service
parameter_list|)
block|{
comment|// Chores should only ever be scheduled with a single ChoreService. If the choreServicer
comment|// is changing, cancel any existing schedules of this chore.
if|if
condition|(
name|choreServicer
operator|!=
literal|null
operator|&&
name|choreServicer
operator|!=
name|service
condition|)
block|{
name|choreServicer
operator|.
name|cancelChore
argument_list|(
name|this
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|choreServicer
operator|=
name|service
expr_stmt|;
name|timeOfThisRun
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|cancel
parameter_list|()
block|{
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|cancel
parameter_list|(
name|boolean
name|mayInterruptIfRunning
parameter_list|)
block|{
if|if
condition|(
name|isScheduled
argument_list|()
condition|)
name|choreServicer
operator|.
name|cancelChore
argument_list|(
name|this
argument_list|,
name|mayInterruptIfRunning
argument_list|)
expr_stmt|;
name|choreServicer
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|Stoppable
name|getStopper
parameter_list|()
block|{
return|return
name|stopper
return|;
block|}
comment|/**    * @return period to execute chore in getTimeUnit() units    */
specifier|public
name|int
name|getPeriod
parameter_list|()
block|{
return|return
name|period
return|;
block|}
comment|/**    * @return initial delay before executing chore in getTimeUnit() units    */
specifier|public
name|long
name|getInitialDelay
parameter_list|()
block|{
return|return
name|initialDelay
return|;
block|}
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|timeUnit
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isInitialChoreComplete
parameter_list|()
block|{
return|return
name|initialChoreComplete
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|synchronized
name|ChoreServicer
name|getChoreServicer
parameter_list|()
block|{
return|return
name|choreServicer
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|synchronized
name|long
name|getTimeOfLastRun
parameter_list|()
block|{
return|return
name|timeOfLastRun
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|synchronized
name|long
name|getTimeOfThisRun
parameter_list|()
block|{
return|return
name|timeOfThisRun
return|;
block|}
comment|/**    * @return true when this Chore is scheduled with a ChoreService    */
specifier|public
specifier|synchronized
name|boolean
name|isScheduled
parameter_list|()
block|{
return|return
name|choreServicer
operator|!=
literal|null
operator|&&
name|choreServicer
operator|.
name|isChoreScheduled
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|VisibleForTesting
specifier|public
specifier|synchronized
name|void
name|choreForTesting
parameter_list|()
block|{
name|chore
argument_list|()
expr_stmt|;
block|}
comment|/**    * The task to execute on each scheduled execution of the Chore    */
specifier|protected
specifier|abstract
name|void
name|chore
parameter_list|()
function_decl|;
comment|/**    * Override to run a task before we start looping.    * @return true if initial chore was successful    */
specifier|protected
name|boolean
name|initialChore
parameter_list|()
block|{
comment|// Default does nothing
return|return
literal|true
return|;
block|}
comment|/**    * Override to run cleanup tasks when the Chore encounters an error and must stop running    */
specifier|protected
specifier|synchronized
name|void
name|cleanup
parameter_list|()
block|{   }
comment|/**    * A summation of this chore in human readable format. Downstream users should not presume    * parsing of this string can relaibly be done between versions. Instead, they should rely    * on the public accessor methods to get the information they desire.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[ScheduledChore: Name: "
operator|+
name|getName
argument_list|()
operator|+
literal|" Period: "
operator|+
name|getPeriod
argument_list|()
operator|+
literal|" Unit: "
operator|+
name|getTimeUnit
argument_list|()
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

