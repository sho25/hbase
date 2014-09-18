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
name|util
operator|.
name|HasThread
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
name|Sleeper
import|;
end_import

begin_comment
comment|/**  * Chore is a task performed on a period in hbase.  The chore is run in its own  * thread. This base abstract class provides while loop and sleeping facility.  * If an unhandled exception, the threads exit is logged.  * Implementers just need to add checking if there is work to be done and if  * so, do it.  Its the base of most of the chore threads in hbase.  *  *<p>Don't subclass Chore if the task relies on being woken up for something to  * do, such as an entry being added to a queue, etc.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|Chore
extends|extends
name|HasThread
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Sleeper
name|sleeper
decl_stmt|;
specifier|protected
specifier|final
name|Stoppable
name|stopper
decl_stmt|;
comment|/**    * @param p Period at which we should run.  Will be adjusted appropriately    * should we find work and it takes time to complete.    * @param stopper When {@link Stoppable#isStopped()} is true, this thread will    * cleanup and exit cleanly.    */
specifier|public
name|Chore
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|int
name|p
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|stopper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"stopper cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|sleeper
operator|=
operator|new
name|Sleeper
argument_list|(
name|p
argument_list|,
name|stopper
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopper
operator|=
name|stopper
expr_stmt|;
block|}
comment|/**    * This constructor is for test only. It allows to create an object and to call chore() on    *  it. There is no sleeper nor stoppable.    */
specifier|protected
name|Chore
parameter_list|()
block|{
name|sleeper
operator|=
literal|null
expr_stmt|;
name|stopper
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @return the sleep period in milliseconds    */
specifier|public
specifier|final
name|int
name|getPeriod
parameter_list|()
block|{
return|return
name|sleeper
operator|.
name|getPeriod
argument_list|()
return|;
block|}
comment|/**    * @see java.lang.Thread#run()    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|boolean
name|initialChoreComplete
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|this
operator|.
name|stopper
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught exception"
argument_list|,
name|e
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
continue|continue;
block|}
block|}
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
name|getName
argument_list|()
operator|+
literal|"error"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|LOG
operator|.
name|info
argument_list|(
name|getName
argument_list|()
operator|+
literal|" exiting"
argument_list|)
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * If the thread is currently sleeping, trigger the core to happen immediately.    * If it's in the middle of its operation, will begin another operation    * immediately after finishing this one.    */
specifier|public
name|void
name|triggerNow
parameter_list|()
block|{
name|this
operator|.
name|sleeper
operator|.
name|skipSleepCycle
argument_list|()
expr_stmt|;
block|}
comment|/*    * Exposed for TESTING!    * calls directly the chore method, from the current thread.    */
specifier|public
name|void
name|choreForTesting
parameter_list|()
block|{
name|chore
argument_list|()
expr_stmt|;
block|}
comment|/**    * Override to run a task before we start looping.    * @return true if initial chore was successful    */
specifier|protected
name|boolean
name|initialChore
parameter_list|()
block|{
comment|// Default does nothing.
return|return
literal|true
return|;
block|}
comment|/**    * Look for chores.  If any found, do them else just return.    */
specifier|protected
specifier|abstract
name|void
name|chore
parameter_list|()
function_decl|;
comment|/**    * Sleep for period.    */
specifier|protected
name|void
name|sleep
parameter_list|()
block|{
name|this
operator|.
name|sleeper
operator|.
name|sleep
argument_list|()
expr_stmt|;
block|}
comment|/**    * Called when the chore has completed, allowing subclasses to cleanup any    * extra overhead    */
specifier|protected
name|void
name|cleanup
parameter_list|()
block|{   }
block|}
end_class

end_unit

