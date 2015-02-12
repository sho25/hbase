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
name|replication
operator|.
name|regionserver
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

begin_class
specifier|public
class|class
name|MetricsReplicationGlobalSourceSource
implements|implements
name|MetricsReplicationSourceSource
block|{
specifier|private
specifier|final
name|MetricsReplicationSourceImpl
name|rms
decl_stmt|;
specifier|private
specifier|final
name|MutableGaugeLong
name|ageOfLastShippedOpGauge
decl_stmt|;
specifier|private
specifier|final
name|MutableGaugeLong
name|sizeOfLogQueueGauge
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|logReadInEditsCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|logEditsFilteredCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|shippedBatchesCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|shippedOpsCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|shippedKBsCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|logReadInBytesCounter
decl_stmt|;
specifier|public
name|MetricsReplicationGlobalSourceSource
parameter_list|(
name|MetricsReplicationSourceImpl
name|rms
parameter_list|)
block|{
name|this
operator|.
name|rms
operator|=
name|rms
expr_stmt|;
name|ageOfLastShippedOpGauge
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongGauge
argument_list|(
name|SOURCE_AGE_OF_LAST_SHIPPED_OP
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|sizeOfLogQueueGauge
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongGauge
argument_list|(
name|SOURCE_SIZE_OF_LOG_QUEUE
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|shippedBatchesCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SOURCE_SHIPPED_BATCHES
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|shippedOpsCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SOURCE_SHIPPED_OPS
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|shippedKBsCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SOURCE_SHIPPED_KBS
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|logReadInBytesCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SOURCE_LOG_READ_IN_BYTES
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|logReadInEditsCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SOURCE_LOG_READ_IN_EDITS
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|logEditsFilteredCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getLongCounter
argument_list|(
name|SOURCE_LOG_EDITS_FILTERED
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setLastShippedAge
parameter_list|(
name|long
name|age
parameter_list|)
block|{
name|ageOfLastShippedOpGauge
operator|.
name|set
argument_list|(
name|age
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSizeOfLogQueue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|sizeOfLogQueueGauge
operator|.
name|set
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrSizeOfLogQueue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|sizeOfLogQueueGauge
operator|.
name|incr
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|decrSizeOfLogQueue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|sizeOfLogQueueGauge
operator|.
name|decr
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrLogReadInEdits
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|logReadInEditsCounter
operator|.
name|incr
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrLogEditsFiltered
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|logEditsFilteredCounter
operator|.
name|incr
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrBatchesShipped
parameter_list|(
name|int
name|batches
parameter_list|)
block|{
name|shippedBatchesCounter
operator|.
name|incr
argument_list|(
name|batches
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrOpsShipped
parameter_list|(
name|long
name|ops
parameter_list|)
block|{
name|shippedOpsCounter
operator|.
name|incr
argument_list|(
name|ops
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrShippedKBs
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|shippedKBsCounter
operator|.
name|incr
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrLogReadInBytes
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|logReadInBytesCounter
operator|.
name|incr
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|long
name|getLastShippedAge
parameter_list|()
block|{
return|return
name|ageOfLastShippedOpGauge
operator|.
name|value
argument_list|()
return|;
block|}
block|}
end_class

end_unit

