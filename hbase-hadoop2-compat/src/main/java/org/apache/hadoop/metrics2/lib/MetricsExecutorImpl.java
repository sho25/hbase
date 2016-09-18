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
name|metrics2
operator|.
name|lib
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
name|ScheduledExecutorService
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
name|atomic
operator|.
name|AtomicInteger
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
name|metrics2
operator|.
name|MetricsExecutor
import|;
end_import

begin_comment
comment|/**  *  Class to handle the ScheduledExecutorService{@link ScheduledExecutorService} used by  *  MetricsRegionAggregateSourceImpl, and  *  JmxCacheBuster  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsExecutorImpl
implements|implements
name|MetricsExecutor
block|{
annotation|@
name|Override
specifier|public
name|ScheduledExecutorService
name|getExecutor
parameter_list|()
block|{
return|return
name|ExecutorSingleton
operator|.
name|INSTANCE
operator|.
name|scheduler
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|getExecutor
argument_list|()
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|getExecutor
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
enum|enum
name|ExecutorSingleton
block|{
name|INSTANCE
block|;
specifier|private
specifier|final
specifier|transient
name|ScheduledExecutorService
name|scheduler
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadPoolExecutorThreadFactory
argument_list|(
literal|"HBase-Metrics2-"
argument_list|)
argument_list|)
decl_stmt|;
block|}
specifier|private
specifier|final
specifier|static
class|class
name|ThreadPoolExecutorThreadFactory
implements|implements
name|ThreadFactory
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|threadNumber
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|ThreadPoolExecutorThreadFactory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|runnable
parameter_list|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|runnable
argument_list|,
name|name
operator|+
name|threadNumber
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
decl_stmt|;
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
block|}
block|}
end_class

end_unit

