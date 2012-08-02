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
operator|.
name|metrics
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
name|BaseMetricsSourceImpl
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

begin_comment
comment|/**  * Hadoop2 implementation of MasterMetricsSource.  */
end_comment

begin_class
specifier|public
class|class
name|MasterMetricsSourceImpl
extends|extends
name|BaseMetricsSourceImpl
implements|implements
name|MasterMetricsSource
block|{
name|MutableCounterLong
name|clusterRequestsCounter
decl_stmt|;
name|MutableGaugeLong
name|ritGauge
decl_stmt|;
name|MutableGaugeLong
name|ritCountOverThresholdGauge
decl_stmt|;
name|MutableGaugeLong
name|ritOldestAgeGauge
decl_stmt|;
specifier|public
name|MasterMetricsSourceImpl
parameter_list|()
block|{
name|this
argument_list|(
name|METRICS_NAME
argument_list|,
name|METRICS_DESCRIPTION
argument_list|,
name|METRICS_CONTEXT
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MasterMetricsSourceImpl
parameter_list|(
name|String
name|metricsName
parameter_list|,
name|String
name|metricsDescription
parameter_list|,
name|String
name|metricsContext
parameter_list|)
block|{
name|super
argument_list|(
name|metricsName
argument_list|,
name|metricsDescription
argument_list|,
name|metricsContext
argument_list|)
expr_stmt|;
name|clusterRequestsCounter
operator|=
name|getLongCounter
argument_list|(
literal|"cluster_requests"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|ritGauge
operator|=
name|getLongGauge
argument_list|(
literal|"ritCount"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|ritCountOverThresholdGauge
operator|=
name|getLongGauge
argument_list|(
literal|"ritCountOverThreshold"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|ritOldestAgeGauge
operator|=
name|getLongGauge
argument_list|(
literal|"ritOldestAge"
argument_list|,
literal|0
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
specifier|public
name|void
name|setRIT
parameter_list|(
name|int
name|ritCount
parameter_list|)
block|{
name|ritGauge
operator|.
name|set
argument_list|(
name|ritCount
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRITCountOverThreshold
parameter_list|(
name|int
name|ritCount
parameter_list|)
block|{
name|ritCountOverThresholdGauge
operator|.
name|set
argument_list|(
name|ritCount
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRITOldestAge
parameter_list|(
name|long
name|ritCount
parameter_list|)
block|{
name|ritCountOverThresholdGauge
operator|.
name|set
argument_list|(
name|ritCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

