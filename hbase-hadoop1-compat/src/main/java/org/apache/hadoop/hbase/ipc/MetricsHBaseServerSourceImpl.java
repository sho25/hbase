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
name|ipc
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
name|MetricsBuilder
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
name|MetricMutableCounterLong
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
name|MetricMutableHistogram
import|;
end_import

begin_class
specifier|public
class|class
name|MetricsHBaseServerSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsHBaseServerSource
block|{
specifier|private
specifier|final
name|MetricsHBaseServerWrapper
name|wrapper
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|authorizationSuccesses
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|authorizationFailures
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|authenticationSuccesses
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|authenticationFailures
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|sentBytes
decl_stmt|;
specifier|private
specifier|final
name|MetricMutableCounterLong
name|receivedBytes
decl_stmt|;
specifier|private
name|MetricMutableHistogram
name|queueCallTime
decl_stmt|;
specifier|private
name|MetricMutableHistogram
name|processCallTime
decl_stmt|;
specifier|public
name|MetricsHBaseServerSourceImpl
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
name|MetricsHBaseServerWrapper
name|wrapper
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
name|wrapper
operator|=
name|wrapper
expr_stmt|;
name|this
operator|.
name|authorizationSuccesses
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|AUTHORIZATION_SUCCESSES_NAME
argument_list|,
name|AUTHORIZATION_SUCCESSES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|this
operator|.
name|authorizationFailures
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|AUTHORIZATION_FAILURES_NAME
argument_list|,
name|AUTHORIZATION_FAILURES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|this
operator|.
name|authenticationSuccesses
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|AUTHENTICATION_SUCCESSES_NAME
argument_list|,
name|AUTHENTICATION_SUCCESSES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|this
operator|.
name|authenticationFailures
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|AUTHENTICATION_FAILURES_NAME
argument_list|,
name|AUTHENTICATION_FAILURES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|this
operator|.
name|sentBytes
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SENT_BYTES_NAME
argument_list|,
name|SENT_BYTES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|this
operator|.
name|receivedBytes
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|RECEIVED_BYTES_NAME
argument_list|,
name|RECEIVED_BYTES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|this
operator|.
name|queueCallTime
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|QUEUE_CALL_TIME_NAME
argument_list|,
name|QUEUE_CALL_TIME_DESC
argument_list|)
expr_stmt|;
name|this
operator|.
name|processCallTime
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newHistogram
argument_list|(
name|PROCESS_CALL_TIME_NAME
argument_list|,
name|PROCESS_CALL_TIME_DESC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorizationSuccess
parameter_list|()
block|{
name|authorizationSuccesses
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorizationFailure
parameter_list|()
block|{
name|authorizationFailures
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authenticationFailure
parameter_list|()
block|{
name|authenticationFailures
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authenticationSuccess
parameter_list|()
block|{
name|authenticationSuccesses
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sentBytes
parameter_list|(
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|sentBytes
operator|.
name|incr
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|receivedBytes
parameter_list|(
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|receivedBytes
operator|.
name|incr
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dequeuedCall
parameter_list|(
name|int
name|qTime
parameter_list|)
block|{
name|queueCallTime
operator|.
name|add
argument_list|(
name|qTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processedCall
parameter_list|(
name|int
name|processingTime
parameter_list|)
block|{
name|processCallTime
operator|.
name|add
argument_list|(
name|processingTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsBuilder
name|metricsBuilder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|mrb
init|=
name|metricsBuilder
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
if|if
condition|(
name|wrapper
operator|!=
literal|null
condition|)
block|{
name|mrb
operator|.
name|addGauge
argument_list|(
name|QUEUE_SIZE_NAME
argument_list|,
name|QUEUE_SIZE_DESC
argument_list|,
name|wrapper
operator|.
name|getTotalQueueSize
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|GENERAL_QUEUE_NAME
argument_list|,
name|GENERAL_QUEUE_DESC
argument_list|,
name|wrapper
operator|.
name|getGeneralQueueLength
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|REPLICATION_QUEUE_NAME
argument_list|,
name|REPLICATION_QUEUE_DESC
argument_list|,
name|wrapper
operator|.
name|getReplicationQueueLength
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|PRIORITY_QUEUE_NAME
argument_list|,
name|PRIORITY_QUEUE_DESC
argument_list|,
name|wrapper
operator|.
name|getPriorityQueueLength
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|NUM_OPEN_CONNECTIONS_NAME
argument_list|,
name|NUM_OPEN_CONNECTIONS_DESC
argument_list|,
name|wrapper
operator|.
name|getNumOpenConnections
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|metricsRegistry
operator|.
name|snapshot
argument_list|(
name|mrb
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

