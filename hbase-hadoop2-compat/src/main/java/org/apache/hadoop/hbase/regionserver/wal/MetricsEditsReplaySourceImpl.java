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
name|regionserver
operator|.
name|wal
package|;
end_package

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
name|MetricHistogram
import|;
end_import

begin_comment
comment|/**  * Hadoop1 implementation of MetricsMasterSource. Implements BaseSource through BaseSourceImpl,  * following the pattern  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsEditsReplaySourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsEditsReplaySource
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
name|MetricsEditsReplaySourceImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|MetricHistogram
name|replayTimeHisto
decl_stmt|;
specifier|private
name|MetricHistogram
name|replayBatchSizeHisto
decl_stmt|;
specifier|private
name|MetricHistogram
name|replayDataSizeHisto
decl_stmt|;
specifier|public
name|MetricsEditsReplaySourceImpl
parameter_list|()
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
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsEditsReplaySourceImpl
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
name|replayTimeHisto
operator|=
name|metricsRegistry
operator|.
name|newHistogram
argument_list|(
name|REPLAY_TIME_NAME
argument_list|,
name|REPLAY_TIME_DESC
argument_list|)
expr_stmt|;
name|replayBatchSizeHisto
operator|=
name|metricsRegistry
operator|.
name|newHistogram
argument_list|(
name|REPLAY_BATCH_SIZE_NAME
argument_list|,
name|REPLAY_BATCH_SIZE_DESC
argument_list|)
expr_stmt|;
name|replayDataSizeHisto
operator|=
name|metricsRegistry
operator|.
name|newHistogram
argument_list|(
name|REPLAY_DATA_SIZE_NAME
argument_list|,
name|REPLAY_DATA_SIZE_DESC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateReplayTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|replayTimeHisto
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateReplayBatchSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|replayBatchSizeHisto
operator|.
name|add
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateReplayDataSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|replayDataSizeHisto
operator|.
name|add
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

