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
name|Closeable
import|;
end_import

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
name|Collection
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
name|ConcurrentMap
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
name|ExecutorService
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
name|Future
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
name|RejectedExecutionException
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
name|SynchronousQueue
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
name|ThreadPoolExecutor
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
name|DaemonThreadFactory
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|MapMaker
import|;
end_import

begin_comment
comment|/**  * Process to kick off and manage a running {@link Subprocedure} on a member. This is the  * specialized part of a {@link Procedure} that actually does procedure type-specific work  * and reports back to the coordinator as it completes each phase.  *<p>  * If there is a connection error ({@link #controllerConnectionFailure(String, IOException)}), all  * currently running subprocedures are notify to failed since there is no longer a way to reach any  * other members or coordinators since the rpcs are down.  */
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
name|ProcedureMember
implements|implements
name|Closeable
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
name|ProcedureMember
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SubprocedureFactory
name|builder
decl_stmt|;
specifier|private
specifier|final
name|ProcedureMemberRpcs
name|rpcs
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Subprocedure
argument_list|>
name|subprocs
init|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|concurrencyLevel
argument_list|(
literal|4
argument_list|)
operator|.
name|weakValues
argument_list|()
operator|.
name|makeMap
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|pool
decl_stmt|;
comment|/**    * Instantiate a new ProcedureMember.  This is a slave that executes subprocedures.    *    * @param rpcs controller used to send notifications to the procedure coordinator    * @param pool thread pool to submit subprocedures    * @param factory class that creates instances of a subprocedure.    */
specifier|public
name|ProcedureMember
parameter_list|(
name|ProcedureMemberRpcs
name|rpcs
parameter_list|,
name|ThreadPoolExecutor
name|pool
parameter_list|,
name|SubprocedureFactory
name|factory
parameter_list|)
block|{
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|rpcs
operator|=
name|rpcs
expr_stmt|;
name|this
operator|.
name|builder
operator|=
name|factory
expr_stmt|;
block|}
specifier|public
specifier|static
name|ThreadPoolExecutor
name|defaultPool
parameter_list|(
name|long
name|wakeFrequency
parameter_list|,
name|long
name|keepAlive
parameter_list|,
name|int
name|procThreads
parameter_list|,
name|String
name|memberName
parameter_list|)
block|{
return|return
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|procThreads
argument_list|,
name|keepAlive
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
operator|new
name|DaemonThreadFactory
argument_list|(
literal|"member: '"
operator|+
name|memberName
operator|+
literal|"' subprocedure-pool"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Package exposed.  Not for public use.    *    * @return reference to the Procedure member's rpcs object    */
name|ProcedureMemberRpcs
name|getRpcs
parameter_list|()
block|{
return|return
name|rpcs
return|;
block|}
comment|/**    * This is separated from execution so that we can detect and handle the case where the    * subprocedure is invalid and inactionable due to bad info (like DISABLED snapshot type being    * sent here)    * @param opName    * @param data    * @return subprocedure    */
specifier|public
name|Subprocedure
name|createSubprocedure
parameter_list|(
name|String
name|opName
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|builder
operator|.
name|buildSubprocedure
argument_list|(
name|opName
argument_list|,
name|data
argument_list|)
return|;
block|}
comment|/**    * Submit an subprocedure for execution.  This starts the local acquire phase.    * @param subproc the subprocedure to execute.    * @return<tt>true</tt> if the subprocedure was started correctly,<tt>false</tt> if it    *         could not be started. In the latter case, the subprocedure holds a reference to    *         the exception that caused the failure.    */
specifier|public
name|boolean
name|submitSubprocedure
parameter_list|(
name|Subprocedure
name|subproc
parameter_list|)
block|{
comment|// if the submitted subprocedure was null, bail.
if|if
condition|(
name|subproc
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Submitted null subprocedure, nothing to run here."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|String
name|procName
init|=
name|subproc
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|procName
operator|==
literal|null
operator|||
name|procName
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Subproc name cannot be null or the empty string"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// make sure we aren't already running an subprocedure of that name
name|Subprocedure
name|rsub
decl_stmt|;
synchronized|synchronized
init|(
name|subprocs
init|)
block|{
name|rsub
operator|=
name|subprocs
operator|.
name|get
argument_list|(
name|procName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rsub
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|rsub
operator|.
name|isComplete
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Subproc '"
operator|+
name|procName
operator|+
literal|"' is already running. Bailing out"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"A completed old subproc "
operator|+
name|procName
operator|+
literal|" is still present, removing"
argument_list|)
expr_stmt|;
name|subprocs
operator|.
name|remove
argument_list|(
name|procName
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Submitting new Subprocedure:"
operator|+
name|procName
argument_list|)
expr_stmt|;
comment|// kick off the subprocedure
name|Future
argument_list|<
name|Void
argument_list|>
name|future
init|=
literal|null
decl_stmt|;
try|try
block|{
name|future
operator|=
name|this
operator|.
name|pool
operator|.
name|submit
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|subprocs
init|)
block|{
name|subprocs
operator|.
name|put
argument_list|(
name|procName
argument_list|,
name|subproc
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|e
parameter_list|)
block|{
comment|// the thread pool is full and we can't run the subprocedure
name|String
name|msg
init|=
literal|"Subprocedure pool is full!"
decl_stmt|;
name|subproc
operator|.
name|cancel
argument_list|(
name|msg
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
comment|// cancel all subprocedures proactively
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
block|{
name|future
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to start subprocedure '"
operator|+
name|procName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/**     * Notification that procedure coordinator has reached the global barrier     * @param procName name of the subprocedure that should start running the the in-barrier phase     */
specifier|public
name|void
name|receivedReachedGlobalBarrier
parameter_list|(
name|String
name|procName
parameter_list|)
block|{
name|Subprocedure
name|subproc
init|=
name|subprocs
operator|.
name|get
argument_list|(
name|procName
argument_list|)
decl_stmt|;
if|if
condition|(
name|subproc
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unexpected reached glabal barrier message for Sub-Procedure '"
operator|+
name|procName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
return|return;
block|}
name|subproc
operator|.
name|receiveReachedGlobalBarrier
argument_list|()
expr_stmt|;
block|}
comment|/**    * Best effort attempt to close the threadpool via Thread.interrupt.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// have to use shutdown now to break any latch waiting
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shutdown the threadpool, and wait for upto timeoutMs millis before bailing    * @param timeoutMs timeout limit in millis    * @return true if successfully, false if bailed due to timeout.    * @throws InterruptedException    */
name|boolean
name|closeAndWait
parameter_list|(
name|long
name|timeoutMs
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
return|return
name|pool
operator|.
name|awaitTermination
argument_list|(
name|timeoutMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
comment|/**    * The connection to the rest of the procedure group (member and coordinator) has been    * broken/lost/failed. This should fail any interested subprocedure, but not attempt to notify    * other members since we cannot reach them anymore.    * @param message description of the error    * @param cause the actual cause of the failure    *    * TODO i'm tempted to just remove this code completely and treat it like any other abort.    * Implementation wise, if this happens it is a ZK failure which means the RS will abort.    */
specifier|public
name|void
name|controllerConnectionFailure
parameter_list|(
specifier|final
name|String
name|message
parameter_list|,
specifier|final
name|IOException
name|cause
parameter_list|)
block|{
name|Collection
argument_list|<
name|Subprocedure
argument_list|>
name|toNotify
init|=
name|subprocs
operator|.
name|values
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|,
name|cause
argument_list|)
expr_stmt|;
for|for
control|(
name|Subprocedure
name|sub
range|:
name|toNotify
control|)
block|{
comment|// TODO notify the elements, if they aren't null
name|sub
operator|.
name|cancel
argument_list|(
name|message
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Send abort to the specified procedure    * @param procName name of the procedure to about    * @param ee exception information about the abort    */
specifier|public
name|void
name|receiveAbortProcedure
parameter_list|(
name|String
name|procName
parameter_list|,
name|ForeignException
name|ee
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Request received to abort procedure "
operator|+
name|procName
argument_list|,
name|ee
argument_list|)
expr_stmt|;
comment|// if we know about the procedure, notify it
name|Subprocedure
name|sub
init|=
name|subprocs
operator|.
name|get
argument_list|(
name|procName
argument_list|)
decl_stmt|;
if|if
condition|(
name|sub
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received abort on procedure with no local subprocedure "
operator|+
name|procName
operator|+
literal|", ignoring it."
argument_list|,
name|ee
argument_list|)
expr_stmt|;
return|return;
comment|// Procedure has already completed
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Propagating foreign exception to subprocedure "
operator|+
name|sub
operator|.
name|getName
argument_list|()
argument_list|,
name|ee
argument_list|)
expr_stmt|;
name|sub
operator|.
name|monitor
operator|.
name|receive
argument_list|(
name|ee
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

