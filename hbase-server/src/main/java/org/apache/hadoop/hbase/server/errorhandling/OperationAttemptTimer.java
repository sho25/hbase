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
name|server
operator|.
name|errorhandling
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimerTask
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
name|classification
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
name|server
operator|.
name|errorhandling
operator|.
name|exception
operator|.
name|OperationAttemptTimeoutException
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
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * Time a given process/operation and report a failure if the elapsed time exceeds the max allowed  * time.  *<p>  * The timer won't start tracking time until calling {@link #start()}. If {@link #complete()} or  * {@link #trigger()} is called before {@link #start()}, calls to {@link #start()} will fail.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|OperationAttemptTimer
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
name|OperationAttemptTimer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxTime
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|complete
decl_stmt|;
specifier|private
specifier|final
name|Timer
name|timer
decl_stmt|;
specifier|private
specifier|final
name|TimerTask
name|timerTask
decl_stmt|;
specifier|private
name|long
name|start
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Create a generic timer for a task/process.    * @param listener listener to notify if the process times out    * @param maxTime max allowed running time for the process. Timer starts on calls to    *          {@link #start()}    * @param info information about the process to pass along if the timer expires    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
name|OperationAttemptTimer
parameter_list|(
specifier|final
name|ExceptionListener
name|listener
parameter_list|,
specifier|final
name|long
name|maxTime
parameter_list|,
specifier|final
name|Object
modifier|...
name|info
parameter_list|)
block|{
name|this
operator|.
name|maxTime
operator|=
name|maxTime
expr_stmt|;
name|timer
operator|=
operator|new
name|Timer
argument_list|()
expr_stmt|;
name|timerTask
operator|=
operator|new
name|TimerTask
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// ensure we don't run this task multiple times
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// quick exit if we already marked the task complete
if|if
condition|(
name|OperationAttemptTimer
operator|.
name|this
operator|.
name|complete
condition|)
return|return;
comment|// mark the task is run, to avoid repeats
name|OperationAttemptTimer
operator|.
name|this
operator|.
name|complete
operator|=
literal|true
expr_stmt|;
block|}
name|long
name|end
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|listener
operator|.
name|receiveError
argument_list|(
literal|"Timeout elapsed!"
argument_list|,
operator|new
name|OperationAttemptTimeoutException
argument_list|(
name|start
argument_list|,
name|end
argument_list|,
name|maxTime
argument_list|)
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
comment|/**    * For all time forward, do not throw an error because the process has completed.    */
specifier|public
name|void
name|complete
parameter_list|()
block|{
comment|// warn if the timer is already marked complete. This isn't going to be thread-safe, but should
comment|// be good enough and its not worth locking just for a warning.
if|if
condition|(
name|this
operator|.
name|complete
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Timer already marked completed, ignoring!"
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Marking timer as complete - no error notifications will be received for this timer."
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|timerTask
init|)
block|{
name|this
operator|.
name|complete
operator|=
literal|true
expr_stmt|;
block|}
name|this
operator|.
name|timer
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
comment|/**    * Start a timer to fail a process if it takes longer than the expected time to complete.    *<p>    * Non-blocking.    * @throws IllegalStateException if the timer has already been marked done via {@link #complete()}    *           or {@link #trigger()}    */
specifier|public
specifier|synchronized
name|void
name|start
parameter_list|()
throws|throws
name|IllegalStateException
block|{
if|if
condition|(
name|this
operator|.
name|start
operator|>=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Timer already started, can't be started again. Ignoring second request."
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Scheduling process timer to run in: "
operator|+
name|maxTime
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|timer
operator|.
name|schedule
argument_list|(
name|timerTask
argument_list|,
name|maxTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|start
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
comment|/**    * Trigger the timer immediately.    *<p>    * Exposed for testing.    */
specifier|public
name|void
name|trigger
parameter_list|()
block|{
synchronized|synchronized
init|(
name|timerTask
init|)
block|{
if|if
condition|(
name|this
operator|.
name|complete
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Timer already completed, not triggering."
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Triggering timer immediately!"
argument_list|)
expr_stmt|;
name|this
operator|.
name|timer
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|this
operator|.
name|timerTask
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

