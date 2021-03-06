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
name|thrift
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A ThreadPoolExecutor customized for working with HBase thrift to update metrics before and  * after the execution of a task.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|THBaseThreadPoolExecutor
extends|extends
name|ThreadPoolExecutor
block|{
specifier|private
name|ThriftMetrics
name|metrics
decl_stmt|;
specifier|public
name|THBaseThreadPoolExecutor
parameter_list|(
name|int
name|corePoolSize
parameter_list|,
name|int
name|maxPoolSize
parameter_list|,
name|long
name|keepAliveTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|)
block|{
name|this
argument_list|(
name|corePoolSize
argument_list|,
name|maxPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|,
literal|null
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
block|}
specifier|public
name|THBaseThreadPoolExecutor
parameter_list|(
name|int
name|corePoolSize
parameter_list|,
name|int
name|maxPoolSize
parameter_list|,
name|long
name|keepAliveTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|,
name|ThreadFactory
name|threadFactory
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|)
block|{
name|super
argument_list|(
name|corePoolSize
argument_list|,
name|maxPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|)
expr_stmt|;
if|if
condition|(
name|threadFactory
operator|!=
literal|null
condition|)
block|{
name|setThreadFactory
argument_list|(
name|threadFactory
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|beforeExecute
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Runnable
name|r
parameter_list|)
block|{
name|super
operator|.
name|beforeExecute
argument_list|(
name|t
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incActiveWorkerCount
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|afterExecute
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|metrics
operator|.
name|decActiveWorkerCount
argument_list|()
expr_stmt|;
name|super
operator|.
name|afterExecute
argument_list|(
name|r
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

