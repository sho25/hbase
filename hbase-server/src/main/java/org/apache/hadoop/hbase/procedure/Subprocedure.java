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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|CountDownLatch
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|errorhandling
operator|.
name|ForeignExceptionListener
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
name|errorhandling
operator|.
name|ForeignExceptionSnare
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
name|errorhandling
operator|.
name|TimeoutExceptionInjector
import|;
end_import

begin_comment
comment|/**  * Distributed procedure member's Subprocedure.  A procedure is sarted on a ProcedureCoordinator  * which communicates with ProcedureMembers who create and start its part of the Procedure.  This  * sub part is called a Subprocedure  *  * Users should subclass this and implement {@link #acquireBarrier()} (get local barrier for this  * member), {@link #insideBarrier()} (execute while globally barriered and release barrier) and  * {@link #cleanup(Exception)} (release state associated with subprocedure.)  *  * When submitted to a ProcedureMemeber, the call method is executed in a separate thread.  * Latches are use too block its progress and trigger continuations when barrier conditions are  * met.  *  * Exception that makes it out of calls to {@link #acquireBarrier()} or {@link #insideBarrier()}  * gets converted into {@link ForeignException}, which will get propagated to the  * {@link ProcedureCoordinator}.  *  * There is a category of procedure (ex: online-snapshots), and a user-specified instance-specific  * barrierName. (ex: snapshot121126).  */
end_comment

begin_class
specifier|abstract
specifier|public
class|class
name|Subprocedure
implements|implements
name|Callable
argument_list|<
name|Void
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
name|Subprocedure
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Name of the procedure
specifier|final
specifier|private
name|String
name|barrierName
decl_stmt|;
comment|//
comment|// Execution state
comment|//
comment|/** wait on before allowing the in barrier phase to proceed */
specifier|private
specifier|final
name|CountDownLatch
name|inGlobalBarrier
decl_stmt|;
comment|/** counted down when the Subprocedure has completed */
specifier|private
specifier|final
name|CountDownLatch
name|releasedLocalBarrier
decl_stmt|;
comment|//
comment|// Error handling
comment|//
comment|/** monitor to check for errors */
specifier|protected
specifier|final
name|ForeignExceptionDispatcher
name|monitor
decl_stmt|;
comment|/** frequency to check for errors (ms) */
specifier|protected
specifier|final
name|long
name|wakeFrequency
decl_stmt|;
specifier|protected
specifier|final
name|TimeoutExceptionInjector
name|executionTimeoutTimer
decl_stmt|;
specifier|protected
specifier|final
name|ProcedureMemberRpcs
name|rpcs
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|complete
init|=
literal|false
decl_stmt|;
comment|/**    * @param member reference to the member managing this subprocedure    * @param procName name of the procedure this subprocedure is associated with    * @param monitor notified if there is an error in the subprocedure    * @param wakeFrequency time in millis to wake to check if there is an error via the monitor (in    *          milliseconds).    * @param timeout time in millis that will trigger a subprocedure abort if it has not completed    */
specifier|public
name|Subprocedure
parameter_list|(
name|ProcedureMember
name|member
parameter_list|,
name|String
name|procName
parameter_list|,
name|ForeignExceptionDispatcher
name|monitor
parameter_list|,
name|long
name|wakeFrequency
parameter_list|,
name|long
name|timeout
parameter_list|)
block|{
comment|// Asserts should be caught during unit testing
assert|assert
name|member
operator|!=
literal|null
operator|:
literal|"procedure member should be non-null"
assert|;
assert|assert
name|member
operator|.
name|getRpcs
argument_list|()
operator|!=
literal|null
operator|:
literal|"rpc handlers should be non-null"
assert|;
assert|assert
name|procName
operator|!=
literal|null
operator|:
literal|"procedure name should be non-null"
assert|;
assert|assert
name|monitor
operator|!=
literal|null
operator|:
literal|"monitor should be non-null"
assert|;
comment|// Default to a very large timeout
name|this
operator|.
name|rpcs
operator|=
name|member
operator|.
name|getRpcs
argument_list|()
expr_stmt|;
name|this
operator|.
name|barrierName
operator|=
name|procName
expr_stmt|;
name|this
operator|.
name|monitor
operator|=
name|monitor
expr_stmt|;
comment|// forward any failures to coordinator.  Since this is a dispatcher, resend loops should not be
comment|// possible.
name|this
operator|.
name|monitor
operator|.
name|addListener
argument_list|(
operator|new
name|ForeignExceptionListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|receive
parameter_list|(
name|ForeignException
name|ee
parameter_list|)
block|{
comment|// if this is a notification from a remote source, just log
if|if
condition|(
name|ee
operator|.
name|isRemote
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Was remote foreign exception, not redispatching error"
argument_list|,
name|ee
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// if it is local, then send it to the coordinator
try|try
block|{
name|rpcs
operator|.
name|sendMemberAborted
argument_list|(
name|Subprocedure
operator|.
name|this
argument_list|,
name|ee
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// this will fail all the running procedures, since the connection is down
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't reach controller, not propagating error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|wakeFrequency
operator|=
name|wakeFrequency
expr_stmt|;
name|this
operator|.
name|inGlobalBarrier
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|releasedLocalBarrier
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// accept error from timer thread, this needs to be started.
name|this
operator|.
name|executionTimeoutTimer
operator|=
operator|new
name|TimeoutExceptionInjector
argument_list|(
name|monitor
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|barrierName
return|;
block|}
specifier|public
name|String
name|getMemberName
parameter_list|()
block|{
return|return
name|rpcs
operator|.
name|getMemberName
argument_list|()
return|;
block|}
specifier|private
name|void
name|rethrowException
parameter_list|()
throws|throws
name|ForeignException
block|{
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
comment|/**    * Execute the Subprocedure {@link #acquireBarrier()} and {@link #insideBarrier()} methods    * while keeping some state for other threads to access.    *    * This would normally be executed by the ProcedureMemeber when a acquire message comes from the    * coordinator.  Rpcs are used to spend message back to the coordinator after different phases    * are executed.  Any exceptions caught during the execution (except for InterruptedException) get    * converted and propagated to coordinator via {@link ProcedureMemberRpcs#sendMemberAborted(    * Subprocedure, ForeignException)}.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"finally"
argument_list|)
specifier|final
specifier|public
name|Void
name|call
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' with timeout "
operator|+
name|executionTimeoutTimer
operator|.
name|getMaxTime
argument_list|()
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
comment|// start the execution timeout timer
name|executionTimeoutTimer
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
comment|// start by checking for error first
name|rethrowException
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' starting 'acquire' stage"
argument_list|)
expr_stmt|;
name|acquireBarrier
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' locally acquired"
argument_list|)
expr_stmt|;
name|rethrowException
argument_list|()
expr_stmt|;
comment|// vote yes to coordinator about being prepared
name|rpcs
operator|.
name|sendMemberAcquired
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' coordinator notified of 'acquire', waiting on"
operator|+
literal|" 'reached' or 'abort' from coordinator"
argument_list|)
expr_stmt|;
comment|// wait for the procedure to reach global barrier before proceding
name|waitForReachedGlobalBarrier
argument_list|()
expr_stmt|;
name|rethrowException
argument_list|()
expr_stmt|;
comment|// if Coordinator aborts, will bail from here with exception
comment|// In traditional 2PC, if a member reaches this state the TX has been committed and the
comment|// member is responsible for rolling forward and recovering and completing the subsequent
comment|// operations in the case of failure.  It cannot rollback.
comment|//
comment|// This implementation is not 2PC since it can still rollback here, and thus has different
comment|// semantics.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' received 'reached' from coordinator."
argument_list|)
expr_stmt|;
name|byte
index|[]
name|dataToCoordinator
init|=
name|insideBarrier
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' locally completed"
argument_list|)
expr_stmt|;
name|rethrowException
argument_list|()
expr_stmt|;
comment|// Ack that the member has executed and released local barrier
name|rpcs
operator|.
name|sendMemberCompleted
argument_list|(
name|this
argument_list|,
name|dataToCoordinator
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' has notified controller of completion"
argument_list|)
expr_stmt|;
comment|// make sure we didn't get an external exception
name|rethrowException
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|InterruptedException
condition|)
block|{
name|msg
operator|=
literal|"Procedure '"
operator|+
name|barrierName
operator|+
literal|"' aborting due to interrupt!"
operator|+
literal|" Likely due to pool shutdown."
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|ForeignException
condition|)
block|{
name|msg
operator|=
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' aborting due to a ForeignException!"
expr_stmt|;
block|}
else|else
block|{
name|msg
operator|=
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' failed!"
expr_stmt|;
block|}
name|cancel
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' running cleanup."
argument_list|)
expr_stmt|;
name|cleanup
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|releasedLocalBarrier
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// tell the timer we are done, if we get here successfully
name|executionTimeoutTimer
operator|.
name|complete
argument_list|()
expr_stmt|;
name|complete
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Subprocedure '"
operator|+
name|barrierName
operator|+
literal|"' completed."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
name|boolean
name|isComplete
parameter_list|()
block|{
return|return
name|complete
return|;
block|}
comment|/**    * exposed for testing.    */
name|ForeignExceptionSnare
name|getErrorCheckable
parameter_list|()
block|{
return|return
name|this
operator|.
name|monitor
return|;
block|}
comment|/**    * The implementation of this method should gather and hold required resources (locks, disk    * space, etc) to satisfy the Procedures barrier condition.  For example, this would be where    * to make all the regions on a RS on the quiescent for an procedure that required all regions    * to be globally quiesed.    *    * Users should override this method.  If a quiescent is not required, this is overkill but    * can still be used to execute a procedure on all members and to propagate any exceptions.    *    * @throws ForeignException    */
specifier|abstract
specifier|public
name|void
name|acquireBarrier
parameter_list|()
throws|throws
name|ForeignException
function_decl|;
comment|/**    * The implementation of this method should act with the assumption that the barrier condition    * has been satisfied.  Continuing the previous example, a condition could be that all RS's    * globally have been quiesced, and procedures that require this precondition could be    * implemented here.    * The implementation should also collect the result of the subprocedure as data to be returned    * to the coordinator upon successful completion.    * Users should override this method.    * @return the data the subprocedure wants to return to coordinator side.    * @throws ForeignException    */
specifier|abstract
specifier|public
name|byte
index|[]
name|insideBarrier
parameter_list|()
throws|throws
name|ForeignException
function_decl|;
comment|/**    * Users should override this method. This implementation of this method should rollback and    * cleanup any temporary or partially completed state that the {@link #acquireBarrier()} may have    * created.    * @param e    */
specifier|abstract
specifier|public
name|void
name|cleanup
parameter_list|(
name|Exception
name|e
parameter_list|)
function_decl|;
comment|/**    * Method to cancel the Subprocedure by injecting an exception from and external source.    * @param cause    */
specifier|public
name|void
name|cancel
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|complete
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|ForeignException
condition|)
block|{
name|monitor
operator|.
name|receive
argument_list|(
operator|(
name|ForeignException
operator|)
name|cause
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|monitor
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
name|getMemberName
argument_list|()
argument_list|,
name|cause
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Callback for the member rpcs to call when the global barrier has been reached.  This    * unblocks the main subprocedure exectuion thread so that the Subprocedure's    * {@link #insideBarrier()} method can be run.    */
specifier|public
name|void
name|receiveReachedGlobalBarrier
parameter_list|()
block|{
name|inGlobalBarrier
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
comment|//
comment|// Subprocedure Internal State interface
comment|//
comment|/**    * Wait for the reached global barrier notification.    *    * Package visibility for testing    *    * @throws ForeignException    * @throws InterruptedException    */
name|void
name|waitForReachedGlobalBarrier
parameter_list|()
throws|throws
name|ForeignException
throws|,
name|InterruptedException
block|{
name|Procedure
operator|.
name|waitForLatch
argument_list|(
name|inGlobalBarrier
argument_list|,
name|monitor
argument_list|,
name|wakeFrequency
argument_list|,
name|barrierName
operator|+
literal|":remote acquired"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Waits until the entire procedure has globally completed, or has been aborted.    * @throws ForeignException    * @throws InterruptedException    */
specifier|public
name|void
name|waitForLocallyCompleted
parameter_list|()
throws|throws
name|ForeignException
throws|,
name|InterruptedException
block|{
name|Procedure
operator|.
name|waitForLatch
argument_list|(
name|releasedLocalBarrier
argument_list|,
name|monitor
argument_list|,
name|wakeFrequency
argument_list|,
name|barrierName
operator|+
literal|":completed"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Empty Subprocedure for testing.    *    * Must be public for stubbing used in testing to work.    */
specifier|public
specifier|static
class|class
name|SubprocedureImpl
extends|extends
name|Subprocedure
block|{
specifier|public
name|SubprocedureImpl
parameter_list|(
name|ProcedureMember
name|member
parameter_list|,
name|String
name|opName
parameter_list|,
name|ForeignExceptionDispatcher
name|monitor
parameter_list|,
name|long
name|wakeFrequency
parameter_list|,
name|long
name|timeout
parameter_list|)
block|{
name|super
argument_list|(
name|member
argument_list|,
name|opName
argument_list|,
name|monitor
argument_list|,
name|wakeFrequency
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|acquireBarrier
parameter_list|()
throws|throws
name|ForeignException
block|{}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|insideBarrier
parameter_list|()
throws|throws
name|ForeignException
block|{
return|return
operator|new
name|byte
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
block|}
empty_stmt|;
block|}
end_class

end_unit

