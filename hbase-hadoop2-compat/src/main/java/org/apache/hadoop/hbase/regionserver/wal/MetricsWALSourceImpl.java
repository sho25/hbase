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
name|MutableFastCounter
import|;
end_import

begin_comment
comment|/**  * Class that transitions metrics from MetricsWAL into the metrics subsystem.  *  * Implements BaseSource through BaseSourceImpl, following the pattern.  * @see org.apache.hadoop.hbase.regionserver.wal.MetricsWAL  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsWALSourceImpl
extends|extends
name|BaseSourceImpl
implements|implements
name|MetricsWALSource
block|{
specifier|private
specifier|final
name|MetricHistogram
name|appendSizeHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|appendTimeHisto
decl_stmt|;
specifier|private
specifier|final
name|MetricHistogram
name|syncTimeHisto
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|appendCount
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|slowAppendCount
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|logRollRequested
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|lowReplicationLogRollRequested
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|writtenBytes
decl_stmt|;
specifier|public
name|MetricsWALSourceImpl
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
name|MetricsWALSourceImpl
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
comment|//Create and store the metrics that will be used.
name|appendTimeHisto
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|APPEND_TIME
argument_list|,
name|APPEND_TIME_DESC
argument_list|)
expr_stmt|;
name|appendSizeHisto
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newSizeHistogram
argument_list|(
name|APPEND_SIZE
argument_list|,
name|APPEND_SIZE_DESC
argument_list|)
expr_stmt|;
name|appendCount
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|APPEND_COUNT
argument_list|,
name|APPEND_COUNT_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|slowAppendCount
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|SLOW_APPEND_COUNT
argument_list|,
name|SLOW_APPEND_COUNT_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
name|syncTimeHisto
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newTimeHistogram
argument_list|(
name|SYNC_TIME
argument_list|,
name|SYNC_TIME_DESC
argument_list|)
expr_stmt|;
name|logRollRequested
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|ROLL_REQUESTED
argument_list|,
name|ROLL_REQUESTED_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|lowReplicationLogRollRequested
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|LOW_REPLICA_ROLL_REQUESTED
argument_list|,
name|LOW_REPLICA_ROLL_REQUESTED_DESC
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|writtenBytes
operator|=
name|this
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|newCounter
argument_list|(
name|WRITTEN_BYTES
argument_list|,
name|WRITTEN_BYTES_DESC
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementAppendSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|appendSizeHisto
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
name|incrementAppendTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|appendTimeHisto
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
name|incrementAppendCount
parameter_list|()
block|{
name|appendCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSlowAppendCount
parameter_list|()
block|{
name|slowAppendCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementSyncTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|syncTimeHisto
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
name|incrementLogRollRequested
parameter_list|()
block|{
name|logRollRequested
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementLowReplicationLogRoll
parameter_list|()
block|{
name|lowReplicationLogRollRequested
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSlowAppendCount
parameter_list|()
block|{
return|return
name|slowAppendCount
operator|.
name|value
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementWrittenBytes
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|writtenBytes
operator|.
name|incr
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWrittenBytes
parameter_list|()
block|{
return|return
name|writtenBytes
operator|.
name|value
argument_list|()
return|;
block|}
block|}
end_class

end_unit

