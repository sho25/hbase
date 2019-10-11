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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|executor
operator|.
name|ExecutorService
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
name|executor
operator|.
name|ExecutorService
operator|.
name|ExecutorStatus
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
name|regionserver
operator|.
name|MetricsRegionServerSource
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
name|regionserver
operator|.
name|MetricsRegionServerSourceImpl
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
name|Pair
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
name|lib
operator|.
name|DynamicMetricsRegistry
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
name|lib
operator|.
name|MutableGaugeLong
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
name|util
operator|.
name|StringUtils
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * The Class ExecutorStatusChore for collect Executor status info periodically  * and report to metrics system  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExecutorStatusChore
extends|extends
name|ScheduledChore
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
name|HealthCheckChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WAKE_FREQ
init|=
literal|"hbase.executors.status.collect.period"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_WAKE_FREQ
init|=
literal|60000
decl_stmt|;
specifier|private
name|ExecutorService
name|service
decl_stmt|;
specifier|private
name|DynamicMetricsRegistry
name|metricsRegistry
decl_stmt|;
specifier|public
name|ExecutorStatusChore
parameter_list|(
name|int
name|sleepTime
parameter_list|,
name|Stoppable
name|stopper
parameter_list|,
name|ExecutorService
name|service
parameter_list|,
name|MetricsRegionServerSource
name|metrics
parameter_list|)
block|{
name|super
argument_list|(
literal|"ExecutorStatusChore"
argument_list|,
name|stopper
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ExecutorStatusChore runs every {} "
argument_list|,
name|StringUtils
operator|.
name|formatTime
argument_list|(
name|sleepTime
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
name|this
operator|.
name|metricsRegistry
operator|=
operator|(
operator|(
name|MetricsRegionServerSourceImpl
operator|)
name|metrics
operator|)
operator|.
name|getMetricsRegistry
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
try|try
block|{
comment|// thread pool monitor
name|Map
argument_list|<
name|String
argument_list|,
name|ExecutorStatus
argument_list|>
name|statuses
init|=
name|service
operator|.
name|getAllExecutorStatuses
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ExecutorStatus
argument_list|>
name|statusEntry
range|:
name|statuses
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|statusEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// Executor's name is generate by ExecutorType#getExecutorName
comment|// include ExecutorType& Servername(split by '-'), here we only need the ExecutorType
name|String
name|poolName
init|=
name|name
operator|.
name|split
argument_list|(
literal|"-"
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|ExecutorStatus
name|status
init|=
name|statusEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|MutableGaugeLong
name|queued
init|=
name|metricsRegistry
operator|.
name|getGauge
argument_list|(
name|poolName
operator|+
literal|"_queued"
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|MutableGaugeLong
name|running
init|=
name|metricsRegistry
operator|.
name|getGauge
argument_list|(
name|poolName
operator|+
literal|"_running"
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|int
name|queueSize
init|=
name|status
operator|.
name|getQueuedEvents
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|runningSize
init|=
name|status
operator|.
name|getRunning
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|queueSize
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"{}'s size info, queued: {}, running: {}"
argument_list|,
name|poolName
argument_list|,
name|queueSize
argument_list|,
name|runningSize
argument_list|)
expr_stmt|;
block|}
name|queued
operator|.
name|set
argument_list|(
name|queueSize
argument_list|)
expr_stmt|;
name|running
operator|.
name|set
argument_list|(
name|runningSize
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|Pair
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|getExecutorStatus
parameter_list|(
name|String
name|poolName
parameter_list|)
block|{
name|MutableGaugeLong
name|running
init|=
name|metricsRegistry
operator|.
name|getGauge
argument_list|(
name|poolName
operator|+
literal|"_running"
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|MutableGaugeLong
name|queued
init|=
name|metricsRegistry
operator|.
name|getGauge
argument_list|(
name|poolName
operator|+
literal|"_queued"
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|(
name|running
operator|.
name|value
argument_list|()
argument_list|,
name|queued
operator|.
name|value
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

