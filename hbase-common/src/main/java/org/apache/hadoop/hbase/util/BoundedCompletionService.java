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
name|List
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
name|ArrayBlockingQueue
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
name|BlockingQueue
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
name|FutureTask
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

begin_comment
comment|/**  * A completion service, close to the one available in the JDK 1.7  * However, this ones keeps the list of the future, and allows to cancel them all.  * This means as well that it can be used for a small set of tasks only.  *<br>Implementation is not Thread safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BoundedCompletionService
parameter_list|<
name|V
parameter_list|>
block|{
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Future
argument_list|<
name|V
argument_list|>
argument_list|>
name|tasks
decl_stmt|;
comment|// alls the tasks
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|Future
argument_list|<
name|V
argument_list|>
argument_list|>
name|completed
decl_stmt|;
comment|// all the tasks that are completed
class|class
name|QueueingFuture
extends|extends
name|FutureTask
argument_list|<
name|V
argument_list|>
block|{
specifier|public
name|QueueingFuture
parameter_list|(
name|Callable
argument_list|<
name|V
argument_list|>
name|callable
parameter_list|)
block|{
name|super
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|done
parameter_list|()
block|{
name|completed
operator|.
name|add
argument_list|(
name|QueueingFuture
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|BoundedCompletionService
parameter_list|(
name|Executor
name|executor
parameter_list|,
name|int
name|maxTasks
parameter_list|)
block|{
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
name|ArrayList
argument_list|<>
argument_list|(
name|maxTasks
argument_list|)
expr_stmt|;
name|this
operator|.
name|completed
operator|=
operator|new
name|ArrayBlockingQueue
argument_list|<>
argument_list|(
name|maxTasks
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Future
argument_list|<
name|V
argument_list|>
name|submit
parameter_list|(
name|Callable
argument_list|<
name|V
argument_list|>
name|task
parameter_list|)
block|{
name|QueueingFuture
name|newFuture
init|=
operator|new
name|QueueingFuture
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|newFuture
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|newFuture
argument_list|)
expr_stmt|;
return|return
name|newFuture
return|;
block|}
specifier|public
name|Future
argument_list|<
name|V
argument_list|>
name|take
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|completed
operator|.
name|take
argument_list|()
return|;
block|}
specifier|public
name|Future
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
name|completed
operator|.
name|poll
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
specifier|public
name|void
name|cancelAll
parameter_list|(
name|boolean
name|interrupt
parameter_list|)
block|{
for|for
control|(
name|Future
argument_list|<
name|V
argument_list|>
name|future
range|:
name|tasks
control|)
block|{
name|future
operator|.
name|cancel
argument_list|(
name|interrupt
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

