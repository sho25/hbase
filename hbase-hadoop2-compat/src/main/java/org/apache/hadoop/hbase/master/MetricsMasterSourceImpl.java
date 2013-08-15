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
name|master
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
name|metrics
operator|.
name|BaseSourceImpl
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
name|MetricsCollector
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
name|MetricsRecordBuilder
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
name|Interns
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
name|MutableCounterLong
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableHistogram
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
name|MutableStat
import|;
end_import

begin_comment
comment|/**  * Hadoop2 implementation of MetricsMasterSource.  *  * Implements BaseSource through BaseSourceImpl, following the pattern  */
end_comment

begin_class
specifier|public
class|class
name|MetricsMasterSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsMasterSource
block|{
specifier|private
specifier|final
name|MetricsMasterWrapper
name|masterWrapper
decl_stmt|;
specifier|private
name|MutableCounterLong
name|clusterRequestsCounter
decl_stmt|;
specifier|public
name|MetricsMasterSourceImpl
parameter_list|(
name|MetricsMasterWrapper
name|masterWrapper
parameter_list|)
block|{
name|this
argument_list|(
name|METRICS_NAME
argument_list|,
name|METRICS_DESCRIPTION
argument_list|,
name|METRICS_CONTEXT
argument_list|,
name|METRICS_JMX_CONTEXT
argument_list|,
name|masterWrapper
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsMasterSourceImpl
parameter_list|(
name|String
name|metricsName
parameter_list|,
name|String
name|metricsDescription
parameter_list|,
name|String
name|metricsContext
parameter_list|,
name|String
name|metricsJmxContext
parameter_list|,
name|MetricsMasterWrapper
name|masterWrapper
parameter_list|)
block|{
name|super
argument_list|(
name|metricsName
argument_list|,
name|metricsDescription
argument_list|,
name|metricsContext
argument_list|,
name|metricsJmxContext
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterWrapper
operator|=
name|masterWrapper
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|super
operator|.
name|init
argument_list|()
expr_stmt|;
name|clusterRequestsCounter
operator|=
name|metricsRegistry
operator|.
name|newCounter
argument_list|(
name|CLUSTER_REQUESTS_NAME
argument_list|,
literal|""
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incRequests
parameter_list|(
specifier|final
name|int
name|inc
parameter_list|)
block|{
name|this
operator|.
name|clusterRequestsCounter
operator|.
name|incr
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsCollector
name|metricsCollector
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|metricsRecordBuilder
init|=
name|metricsCollector
operator|.
name|addRecord
argument_list|(
name|metricsName
argument_list|)
operator|.
name|setContext
argument_list|(
name|metricsContext
argument_list|)
decl_stmt|;
comment|// masterWrapper can be null because this function is called inside of init.
if|if
condition|(
name|masterWrapper
operator|!=
literal|null
condition|)
block|{
name|metricsRecordBuilder
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MASTER_ACTIVE_TIME_NAME
argument_list|,
name|MASTER_ACTIVE_TIME_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getActiveTime
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|MASTER_START_TIME_NAME
argument_list|,
name|MASTER_START_TIME_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getStartTime
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|AVERAGE_LOAD_NAME
argument_list|,
name|AVERAGE_LOAD_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getAverageLoad
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|NUM_REGION_SERVERS_NAME
argument_list|,
name|NUMBER_OF_REGION_SERVERS_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getRegionServers
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|NUM_DEAD_REGION_SERVERS_NAME
argument_list|,
name|NUMBER_OF_DEAD_REGION_SERVERS_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getDeadRegionServers
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|ZOOKEEPER_QUORUM_NAME
argument_list|,
name|ZOOKEEPER_QUORUM_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getZookeeperQuorum
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|SERVER_NAME_NAME
argument_list|,
name|SERVER_NAME_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|CLUSTER_ID_NAME
argument_list|,
name|CLUSTER_ID_DESC
argument_list|)
argument_list|,
name|masterWrapper
operator|.
name|getClusterId
argument_list|()
argument_list|)
operator|.
name|tag
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|IS_ACTIVE_MASTER_NAME
argument_list|,
name|IS_ACTIVE_MASTER_DESC
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|masterWrapper
operator|.
name|getIsActiveMaster
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|metricsRegistry
operator|.
name|snapshot
argument_list|(
name|metricsRecordBuilder
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

