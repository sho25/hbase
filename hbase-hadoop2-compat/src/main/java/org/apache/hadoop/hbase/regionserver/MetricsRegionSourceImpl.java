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
name|atomic
operator|.
name|AtomicBoolean
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
name|MutableHistogram
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsRegionSourceImpl
implements|implements
name|MetricsRegionSource
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
name|MetricsRegionSourceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Non-final so that we can null out the wrapper
comment|// This is just paranoia. We really really don't want to
comment|// leak a whole region by way of keeping the
comment|// regionWrapper around too long.
specifier|private
name|MetricsRegionWrapper
name|regionWrapper
decl_stmt|;
specifier|private
specifier|final
name|MetricsRegionAggregateSourceImpl
name|agg
decl_stmt|;
specifier|private
specifier|final
name|DynamicMetricsRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionNamePrefix
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionPutKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionDeleteKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionGetKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionIncrementKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionAppendKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionScanNextKey
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|regionPut
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|regionDelete
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|regionIncrement
decl_stmt|;
specifier|private
specifier|final
name|MutableCounterLong
name|regionAppend
decl_stmt|;
specifier|private
specifier|final
name|MutableHistogram
name|regionGet
decl_stmt|;
specifier|private
specifier|final
name|MutableHistogram
name|regionScanNext
decl_stmt|;
specifier|private
specifier|final
name|int
name|hashCode
decl_stmt|;
specifier|public
name|MetricsRegionSourceImpl
parameter_list|(
name|MetricsRegionWrapper
name|regionWrapper
parameter_list|,
name|MetricsRegionAggregateSourceImpl
name|aggregate
parameter_list|)
block|{
name|this
operator|.
name|regionWrapper
operator|=
name|regionWrapper
expr_stmt|;
name|agg
operator|=
name|aggregate
expr_stmt|;
name|agg
operator|.
name|register
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating new MetricsRegionSourceImpl for table "
operator|+
name|regionWrapper
operator|.
name|getTableName
argument_list|()
operator|+
literal|" "
operator|+
name|regionWrapper
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|registry
operator|=
name|agg
operator|.
name|getMetricsRegistry
argument_list|()
expr_stmt|;
name|regionNamePrefix
operator|=
literal|"Namespace_"
operator|+
name|regionWrapper
operator|.
name|getNamespace
argument_list|()
operator|+
literal|"_table_"
operator|+
name|regionWrapper
operator|.
name|getTableName
argument_list|()
operator|+
literal|"_region_"
operator|+
name|regionWrapper
operator|.
name|getRegionName
argument_list|()
operator|+
literal|"_metric_"
expr_stmt|;
name|String
name|suffix
init|=
literal|"Count"
decl_stmt|;
name|regionPutKey
operator|=
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|MUTATE_KEY
operator|+
name|suffix
expr_stmt|;
name|regionPut
operator|=
name|registry
operator|.
name|getLongCounter
argument_list|(
name|regionPutKey
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|regionDeleteKey
operator|=
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|DELETE_KEY
operator|+
name|suffix
expr_stmt|;
name|regionDelete
operator|=
name|registry
operator|.
name|getLongCounter
argument_list|(
name|regionDeleteKey
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|regionIncrementKey
operator|=
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|INCREMENT_KEY
operator|+
name|suffix
expr_stmt|;
name|regionIncrement
operator|=
name|registry
operator|.
name|getLongCounter
argument_list|(
name|regionIncrementKey
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|regionAppendKey
operator|=
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|APPEND_KEY
operator|+
name|suffix
expr_stmt|;
name|regionAppend
operator|=
name|registry
operator|.
name|getLongCounter
argument_list|(
name|regionAppendKey
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|regionGetKey
operator|=
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|GET_KEY
expr_stmt|;
name|regionGet
operator|=
name|registry
operator|.
name|newTimeHistogram
argument_list|(
name|regionGetKey
argument_list|)
expr_stmt|;
name|regionScanNextKey
operator|=
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|SCAN_NEXT_KEY
expr_stmt|;
name|regionScanNext
operator|=
name|registry
operator|.
name|newTimeHistogram
argument_list|(
name|regionScanNextKey
argument_list|)
expr_stmt|;
name|hashCode
operator|=
name|regionWrapper
operator|.
name|getRegionHashCode
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|boolean
name|wasClosed
init|=
name|closed
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|// Has someone else already closed this for us?
if|if
condition|(
name|wasClosed
condition|)
block|{
return|return;
block|}
comment|// Before removing the metrics remove this region from the aggregate region bean.
comment|// This should mean that it's unlikely that snapshot and close happen at the same time.
name|agg
operator|.
name|deregister
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// While it's un-likely that snapshot and close happen at the same time it's still possible.
comment|// So grab the lock to ensure that all calls to snapshot are done before we remove the metrics
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Removing region Metrics: "
operator|+
name|regionWrapper
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|registry
operator|.
name|removeMetric
argument_list|(
name|regionPutKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeMetric
argument_list|(
name|regionDeleteKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeMetric
argument_list|(
name|regionIncrementKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeMetric
argument_list|(
name|regionAppendKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeMetric
argument_list|(
name|regionGetKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeMetric
argument_list|(
name|regionScanNextKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeHistogramMetrics
argument_list|(
name|regionGetKey
argument_list|)
expr_stmt|;
name|registry
operator|.
name|removeHistogramMetrics
argument_list|(
name|regionScanNextKey
argument_list|)
expr_stmt|;
name|regionWrapper
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|updatePut
parameter_list|()
block|{
name|regionPut
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateDelete
parameter_list|()
block|{
name|regionDelete
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateGet
parameter_list|(
name|long
name|getSize
parameter_list|)
block|{
name|regionGet
operator|.
name|add
argument_list|(
name|getSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateScan
parameter_list|(
name|long
name|scanSize
parameter_list|)
block|{
name|regionScanNext
operator|.
name|add
argument_list|(
name|scanSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateIncrement
parameter_list|()
block|{
name|regionIncrement
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateAppend
parameter_list|()
block|{
name|regionAppend
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MetricsRegionAggregateSource
name|getAggregateSource
parameter_list|()
block|{
return|return
name|agg
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|MetricsRegionSource
name|source
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|source
operator|instanceof
name|MetricsRegionSourceImpl
operator|)
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|MetricsRegionSourceImpl
name|impl
init|=
operator|(
name|MetricsRegionSourceImpl
operator|)
name|source
decl_stmt|;
if|if
condition|(
name|impl
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|Long
operator|.
name|compare
argument_list|(
name|hashCode
argument_list|,
name|impl
operator|.
name|hashCode
argument_list|)
return|;
block|}
name|void
name|snapshot
parameter_list|(
name|MetricsRecordBuilder
name|mrb
parameter_list|,
name|boolean
name|ignored
parameter_list|)
block|{
comment|// If there is a close that started be double extra sure
comment|// that we're not getting any locks and not putting data
comment|// into the metrics that should be removed. So early out
comment|// before even getting the lock.
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// Grab the read
comment|// This ensures that removes of the metrics
comment|// can't happen while we are putting them back in.
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// It's possible that a close happened between checking
comment|// the closed variable and getting the lock.
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|STORE_COUNT
argument_list|,
name|MetricsRegionServerSource
operator|.
name|STORE_COUNT_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getNumStores
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|STOREFILE_COUNT
argument_list|,
name|MetricsRegionServerSource
operator|.
name|STOREFILE_COUNT_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getNumStoreFiles
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|MEMSTORE_SIZE
argument_list|,
name|MetricsRegionServerSource
operator|.
name|MEMSTORE_SIZE_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getMemstoreSize
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|MAX_STORE_FILE_AGE
argument_list|,
name|MetricsRegionServerSource
operator|.
name|MAX_STORE_FILE_AGE_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getMaxStoreFileAge
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|MIN_STORE_FILE_AGE
argument_list|,
name|MetricsRegionServerSource
operator|.
name|MIN_STORE_FILE_AGE_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getMinStoreFileAge
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|AVG_STORE_FILE_AGE
argument_list|,
name|MetricsRegionServerSource
operator|.
name|AVG_STORE_FILE_AGE_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getAvgStoreFileAge
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|NUM_REFERENCE_FILES
argument_list|,
name|MetricsRegionServerSource
operator|.
name|NUM_REFERENCE_FILES_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getNumReferenceFiles
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addGauge
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|STOREFILE_SIZE
argument_list|,
name|MetricsRegionServerSource
operator|.
name|STOREFILE_SIZE_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getStoreFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionSource
operator|.
name|COMPACTIONS_COMPLETED_COUNT
argument_list|,
name|MetricsRegionSource
operator|.
name|COMPACTIONS_COMPLETED_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getNumCompactionsCompleted
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionSource
operator|.
name|NUM_BYTES_COMPACTED_COUNT
argument_list|,
name|MetricsRegionSource
operator|.
name|NUM_BYTES_COMPACTED_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getNumBytesCompacted
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionSource
operator|.
name|NUM_FILES_COMPACTED_COUNT
argument_list|,
name|MetricsRegionSource
operator|.
name|NUM_FILES_COMPACTED_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getNumFilesCompacted
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|READ_REQUEST_COUNT
argument_list|,
name|MetricsRegionServerSource
operator|.
name|READ_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getReadRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|FILTERED_READ_REQUEST_COUNT
argument_list|,
name|MetricsRegionServerSource
operator|.
name|FILTERED_READ_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getFilteredReadRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionServerSource
operator|.
name|WRITE_REQUEST_COUNT
argument_list|,
name|MetricsRegionServerSource
operator|.
name|WRITE_REQUEST_COUNT_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getWriteRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|mrb
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|regionNamePrefix
operator|+
name|MetricsRegionSource
operator|.
name|REPLICA_ID
argument_list|,
name|MetricsRegionSource
operator|.
name|REPLICA_ID_DESC
argument_list|)
argument_list|,
name|this
operator|.
name|regionWrapper
operator|.
name|getReplicaId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashCode
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
return|return
name|obj
operator|==
name|this
operator|||
operator|(
name|obj
operator|instanceof
name|MetricsRegionSourceImpl
operator|&&
name|compareTo
argument_list|(
operator|(
name|MetricsRegionSourceImpl
operator|)
name|obj
argument_list|)
operator|==
literal|0
operator|)
return|;
block|}
block|}
end_class

end_unit

