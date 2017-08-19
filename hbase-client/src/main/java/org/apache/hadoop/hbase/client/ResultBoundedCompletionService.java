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
name|client
package|;
end_package

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
name|concurrent
operator|.
name|CancellationException
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
name|ExecutionException
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
name|Executor
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
name|RunnableFuture
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|htrace
operator|.
name|Trace
import|;
end_import

begin_comment
comment|/**  * A completion service for the RpcRetryingCallerFactory.  * Keeps the list of the futures, and allows to cancel them all.  * This means as well that it can be used for a small set of tasks only.  *<br>Implementation is not Thread safe.  *  * CompletedTasks is implemented as a queue, the entry is added based on the time order. I.e,  * when the first task completes (whether it is a success or failure), it is added as a first  * entry in the queue, the next completed task is added as a second entry in the queue, ...  * When iterating through the queue, we know it is based on time order. If the first  * completed task succeeds, it is returned. If it is failure, the iteration goes on until it  * finds a success.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ResultBoundedCompletionService
parameter_list|<
name|V
parameter_list|>
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
name|ResultBoundedCompletionService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RpcRetryingCallerFactory
name|retryingCallerFactory
decl_stmt|;
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|QueueingFuture
argument_list|<
name|V
argument_list|>
index|[]
name|tasks
decl_stmt|;
comment|// all the tasks
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|QueueingFuture
argument_list|>
name|completedTasks
decl_stmt|;
comment|// completed tasks
specifier|private
specifier|volatile
name|boolean
name|cancelled
init|=
literal|false
decl_stmt|;
class|class
name|QueueingFuture
parameter_list|<
name|T
parameter_list|>
implements|implements
name|RunnableFuture
argument_list|<
name|T
argument_list|>
block|{
specifier|private
specifier|final
name|RetryingCallable
argument_list|<
name|T
argument_list|>
name|future
decl_stmt|;
specifier|private
name|T
name|result
init|=
literal|null
decl_stmt|;
specifier|private
name|ExecutionException
name|exeEx
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|cancelled
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|int
name|callTimeout
decl_stmt|;
specifier|private
specifier|final
name|RpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|retryingCaller
decl_stmt|;
specifier|private
name|boolean
name|resultObtained
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|int
name|replicaId
decl_stmt|;
comment|// replica id
specifier|public
name|QueueingFuture
parameter_list|(
name|RetryingCallable
argument_list|<
name|T
argument_list|>
name|future
parameter_list|,
name|int
name|callTimeout
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|this
operator|.
name|future
operator|=
name|future
expr_stmt|;
name|this
operator|.
name|callTimeout
operator|=
name|callTimeout
expr_stmt|;
name|this
operator|.
name|retryingCaller
operator|=
name|retryingCallerFactory
operator|.
expr|<
name|T
operator|>
name|newCaller
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|id
expr_stmt|;
block|}
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
try|try
block|{
if|if
condition|(
operator|!
name|cancelled
condition|)
block|{
name|result
operator|=
name|this
operator|.
name|retryingCaller
operator|.
name|callWithRetries
argument_list|(
name|future
argument_list|,
name|callTimeout
argument_list|)
expr_stmt|;
name|resultObtained
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|exeEx
operator|=
operator|new
name|ExecutionException
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
synchronized|synchronized
init|(
name|tasks
init|)
block|{
comment|// If this wasn't canceled then store the result.
if|if
condition|(
operator|!
name|cancelled
condition|)
block|{
name|completedTasks
operator|.
name|add
argument_list|(
name|QueueingFuture
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
comment|// Notify just in case there was someone waiting and this was canceled.
comment|// That shouldn't happen but better safe than sorry.
name|tasks
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|cancel
parameter_list|(
name|boolean
name|mayInterruptIfRunning
parameter_list|)
block|{
if|if
condition|(
name|resultObtained
operator|||
name|exeEx
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
name|retryingCaller
operator|.
name|cancel
argument_list|()
expr_stmt|;
if|if
condition|(
name|future
operator|instanceof
name|Cancellable
condition|)
operator|(
operator|(
name|Cancellable
operator|)
name|future
operator|)
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|cancelled
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCancelled
parameter_list|()
block|{
return|return
name|cancelled
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|resultObtained
operator|||
name|exeEx
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|get
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
try|try
block|{
return|return
name|get
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"You did wait for 1000 days here?"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|T
name|get
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
synchronized|synchronized
init|(
name|tasks
init|)
block|{
if|if
condition|(
name|resultObtained
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|exeEx
operator|!=
literal|null
condition|)
block|{
throw|throw
name|exeEx
throw|;
block|}
name|unit
operator|.
name|timedWait
argument_list|(
name|tasks
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|resultObtained
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|exeEx
operator|!=
literal|null
condition|)
block|{
throw|throw
name|exeEx
throw|;
block|}
throw|throw
operator|new
name|TimeoutException
argument_list|(
literal|"timeout="
operator|+
name|timeout
operator|+
literal|", "
operator|+
name|unit
argument_list|)
throw|;
block|}
specifier|public
name|int
name|getReplicaId
parameter_list|()
block|{
return|return
name|replicaId
return|;
block|}
specifier|public
name|ExecutionException
name|getExeEx
parameter_list|()
block|{
return|return
name|exeEx
return|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|ResultBoundedCompletionService
parameter_list|(
name|RpcRetryingCallerFactory
name|retryingCallerFactory
parameter_list|,
name|Executor
name|executor
parameter_list|,
name|int
name|maxTasks
parameter_list|)
block|{
name|this
operator|.
name|retryingCallerFactory
operator|=
name|retryingCallerFactory
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
operator|new
name|QueueingFuture
index|[
name|maxTasks
index|]
expr_stmt|;
name|this
operator|.
name|completedTasks
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|maxTasks
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|submit
parameter_list|(
name|RetryingCallable
argument_list|<
name|V
argument_list|>
name|task
parameter_list|,
name|int
name|callTimeout
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|newFuture
init|=
operator|new
name|QueueingFuture
argument_list|<>
argument_list|(
name|task
argument_list|,
name|callTimeout
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|Trace
operator|.
name|wrap
argument_list|(
name|newFuture
argument_list|)
argument_list|)
expr_stmt|;
name|tasks
index|[
name|id
index|]
operator|=
name|newFuture
expr_stmt|;
block|}
specifier|public
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|take
parameter_list|()
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|tasks
init|)
block|{
while|while
condition|(
operator|!
name|cancelled
operator|&&
operator|(
name|completedTasks
operator|.
name|size
argument_list|()
operator|<
literal|1
operator|)
condition|)
name|tasks
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
return|return
name|completedTasks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|/**    * Poll for the first completed task whether it is a success or execution exception.    *    * @param timeout  - time to wait before it times out    * @param unit  - time unit for timeout    */
specifier|public
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|poll
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
name|pollForSpecificCompletedTask
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|/**    * Poll for the first successfully completed task whose completed order is in startIndex,    * endIndex(exclusive) range    *    * @param timeout  - time to wait before it times out    * @param unit  - time unit for timeout    * @param startIndex - start index, starting from 0, inclusive    * @param endIndex - end index, exclusive    *    * @return If within timeout time, there is no successfully completed task, return null; If all    *         tasks get execution exception, it will throw out the last execution exception,    *         otherwise return the first successfully completed task's result.    */
specifier|public
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|pollForFirstSuccessfullyCompletedTask
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|int
name|startIndex
parameter_list|,
name|int
name|endIndex
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|CancellationException
throws|,
name|ExecutionException
block|{
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|f
decl_stmt|;
name|long
name|start
decl_stmt|,
name|duration
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startIndex
init|;
name|i
operator|<
name|endIndex
condition|;
name|i
operator|++
control|)
block|{
name|start
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
name|f
operator|=
name|pollForSpecificCompletedTask
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|duration
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|start
expr_stmt|;
comment|// Even with operationTimeout less than 0, still loop through the rest as there could
comment|// be other completed tasks before operationTimeout.
name|timeout
operator|-=
name|duration
expr_stmt|;
if|if
condition|(
name|f
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|f
operator|.
name|getExeEx
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// we continue here as we need to loop through all the results.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Replica "
operator|+
operator|(
operator|(
name|f
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|f
operator|.
name|getReplicaId
argument_list|()
operator|)
operator|+
literal|" returns "
operator|+
name|f
operator|.
name|getExeEx
argument_list|()
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|==
operator|(
name|endIndex
operator|-
literal|1
operator|)
condition|)
block|{
comment|// Rethrow this exception
throw|throw
name|f
operator|.
name|getExeEx
argument_list|()
throw|;
block|}
continue|continue;
block|}
return|return
name|f
return|;
block|}
comment|// impossible to reach
return|return
literal|null
return|;
block|}
comment|/**    * Poll for the Nth completed task (index starts from 0 (the 1st), 1 (the second)...)    *    * @param timeout  - time to wait before it times out    * @param unit  - time unit for timeout    * @param index - the index(th) completed task, index starting from 0    */
specifier|private
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|pollForSpecificCompletedTask
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
synchronized|synchronized
init|(
name|tasks
init|)
block|{
if|if
condition|(
operator|!
name|cancelled
operator|&&
operator|(
name|completedTasks
operator|.
name|size
argument_list|()
operator|<=
name|index
operator|)
condition|)
name|unit
operator|.
name|timedWait
argument_list|(
name|tasks
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
if|if
condition|(
name|completedTasks
operator|.
name|size
argument_list|()
operator|<=
name|index
condition|)
return|return
literal|null
return|;
block|}
return|return
name|completedTasks
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
specifier|public
name|void
name|cancelAll
parameter_list|()
block|{
comment|// Grab the lock on tasks so that cancelled is visible everywhere
synchronized|synchronized
init|(
name|tasks
init|)
block|{
name|cancelled
operator|=
literal|true
expr_stmt|;
block|}
for|for
control|(
name|QueueingFuture
argument_list|<
name|V
argument_list|>
name|future
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|future
operator|!=
literal|null
condition|)
name|future
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

