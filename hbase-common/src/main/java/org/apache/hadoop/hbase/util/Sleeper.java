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
operator|.
name|util
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
name|Stoppable
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

begin_comment
comment|/**  * Sleeper for current thread.  * Sleeps for passed period.  Also checks passed boolean and if interrupted,  * will return if the flag is set (rather than go back to sleep until its  * sleep time is up).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Sleeper
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
name|Sleeper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|period
decl_stmt|;
specifier|private
specifier|final
name|Stoppable
name|stopper
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|MINIMAL_DELTA_FOR_LOGGING
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|final
name|Object
name|sleepLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|triggerWake
init|=
literal|false
decl_stmt|;
comment|/**    * @param sleep sleep time in milliseconds    * @param stopper When {@link Stoppable#isStopped()} is true, this thread will    *    cleanup and exit cleanly.    */
specifier|public
name|Sleeper
parameter_list|(
specifier|final
name|int
name|sleep
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|)
block|{
name|this
operator|.
name|period
operator|=
name|sleep
expr_stmt|;
name|this
operator|.
name|stopper
operator|=
name|stopper
expr_stmt|;
block|}
comment|/**    * If currently asleep, stops sleeping; if not asleep, will skip the next    * sleep cycle.    */
specifier|public
name|void
name|skipSleepCycle
parameter_list|()
block|{
synchronized|synchronized
init|(
name|sleepLock
init|)
block|{
name|triggerWake
operator|=
literal|true
expr_stmt|;
name|sleepLock
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Sleep for period.    */
specifier|public
name|void
name|sleep
parameter_list|()
block|{
name|sleep
argument_list|(
name|this
operator|.
name|period
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|sleep
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
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
return|return;
block|}
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|currentSleepTime
init|=
name|sleepTime
decl_stmt|;
while|while
condition|(
name|currentSleepTime
operator|>
literal|0
condition|)
block|{
name|long
name|woke
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
synchronized|synchronized
init|(
name|sleepLock
init|)
block|{
if|if
condition|(
name|triggerWake
condition|)
block|{
break|break;
block|}
name|sleepLock
operator|.
name|wait
argument_list|(
name|currentSleepTime
argument_list|)
expr_stmt|;
block|}
name|woke
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|long
name|slept
init|=
name|woke
operator|-
name|now
decl_stmt|;
if|if
condition|(
name|slept
operator|-
name|this
operator|.
name|period
operator|>
name|MINIMAL_DELTA_FOR_LOGGING
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"We slept {}ms instead of {}ms, this is likely due to a long "
operator|+
literal|"garbage collecting pause and it's usually bad, see "
operator|+
literal|"http://hbase.apache.org/book.html#trouble.rs.runtime.zkexpired"
argument_list|,
name|slept
argument_list|,
name|this
operator|.
name|period
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|iex
parameter_list|)
block|{
comment|// We we interrupted because we're meant to stop?  If not, just
comment|// continue ignoring the interruption
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
return|return;
block|}
block|}
comment|// Recalculate waitTime.
name|woke
operator|=
operator|(
name|woke
operator|==
operator|-
literal|1
operator|)
condition|?
name|System
operator|.
name|currentTimeMillis
argument_list|()
else|:
name|woke
expr_stmt|;
name|currentSleepTime
operator|=
name|this
operator|.
name|period
operator|-
operator|(
name|woke
operator|-
name|now
operator|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|sleepLock
init|)
block|{
name|triggerWake
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**    * @return the sleep period in milliseconds    */
specifier|public
specifier|final
name|int
name|getPeriod
parameter_list|()
block|{
return|return
name|period
return|;
block|}
block|}
end_class

end_unit

