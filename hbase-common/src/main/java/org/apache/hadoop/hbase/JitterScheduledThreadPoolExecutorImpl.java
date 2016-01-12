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
name|Delayed
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
name|RunnableScheduledFuture
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
name|ThreadFactory
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
name|ThreadLocalRandom
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

begin_comment
comment|/**  * ScheduledThreadPoolExecutor that will add some jitter to the RunnableScheduledFuture.getDelay.  *  * This will spread out things on a distributed cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|JitterScheduledThreadPoolExecutorImpl
extends|extends
name|ScheduledThreadPoolExecutor
block|{
specifier|private
specifier|final
name|double
name|spread
decl_stmt|;
comment|/**    * Main constructor.    * @param spread The percent up and down that RunnableScheduledFuture.getDelay should be jittered.    */
specifier|public
name|JitterScheduledThreadPoolExecutorImpl
parameter_list|(
name|int
name|corePoolSize
parameter_list|,
name|ThreadFactory
name|threadFactory
parameter_list|,
name|double
name|spread
parameter_list|)
block|{
name|super
argument_list|(
name|corePoolSize
argument_list|,
name|threadFactory
argument_list|)
expr_stmt|;
name|this
operator|.
name|spread
operator|=
name|spread
expr_stmt|;
block|}
specifier|protected
parameter_list|<
name|V
parameter_list|>
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
name|decorateTask
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
name|task
parameter_list|)
block|{
return|return
operator|new
name|JitteredRunnableScheduledFuture
argument_list|<>
argument_list|(
name|task
argument_list|)
return|;
block|}
specifier|protected
parameter_list|<
name|V
parameter_list|>
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
name|decorateTask
parameter_list|(
name|Callable
argument_list|<
name|V
argument_list|>
name|callable
parameter_list|,
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
name|task
parameter_list|)
block|{
return|return
operator|new
name|JitteredRunnableScheduledFuture
argument_list|<>
argument_list|(
name|task
argument_list|)
return|;
block|}
comment|/**    * Class that basically just defers to the wrapped future.    * The only exception is getDelay    */
specifier|protected
class|class
name|JitteredRunnableScheduledFuture
parameter_list|<
name|V
parameter_list|>
implements|implements
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
block|{
specifier|private
specifier|final
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
name|wrapped
decl_stmt|;
name|JitteredRunnableScheduledFuture
parameter_list|(
name|RunnableScheduledFuture
argument_list|<
name|V
argument_list|>
name|wrapped
parameter_list|)
block|{
name|this
operator|.
name|wrapped
operator|=
name|wrapped
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPeriodic
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|isPeriodic
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDelay
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
name|long
name|baseDelay
init|=
name|wrapped
operator|.
name|getDelay
argument_list|(
name|unit
argument_list|)
decl_stmt|;
name|long
name|spreadTime
init|=
call|(
name|long
call|)
argument_list|(
name|baseDelay
operator|*
name|spread
argument_list|)
decl_stmt|;
name|long
name|delay
init|=
name|spreadTime
operator|<=
literal|0
condition|?
name|baseDelay
else|:
name|baseDelay
operator|+
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
operator|-
name|spreadTime
argument_list|,
name|spreadTime
argument_list|)
decl_stmt|;
comment|// Ensure that we don't roll over for nanoseconds.
return|return
operator|(
name|delay
operator|<
literal|0
operator|)
condition|?
name|baseDelay
else|:
name|delay
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Delayed
name|o
parameter_list|)
block|{
return|return
name|wrapped
operator|.
name|compareTo
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|obj
operator|instanceof
name|Delayed
condition|?
name|compareTo
argument_list|(
operator|(
name|Delayed
operator|)
name|obj
argument_list|)
operator|==
literal|0
else|:
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|wrapped
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|wrapped
operator|.
name|run
argument_list|()
expr_stmt|;
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
return|return
name|wrapped
operator|.
name|cancel
argument_list|(
name|mayInterruptIfRunning
argument_list|)
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
name|wrapped
operator|.
name|isCancelled
argument_list|()
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
name|wrapped
operator|.
name|isDone
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|get
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
return|return
name|wrapped
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
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
return|return
name|wrapped
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

