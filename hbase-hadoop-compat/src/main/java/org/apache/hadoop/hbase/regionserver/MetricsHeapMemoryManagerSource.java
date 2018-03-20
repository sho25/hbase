begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|BaseSource
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
comment|/**  * This interface will be implemented by a MetricsSource that will export metrics from  * HeapMemoryManager in RegionServer into the hadoop metrics system.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsHeapMemoryManagerSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"Memory"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"regionserver"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase RegionServer's memory"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
comment|/**    * Update/Set the blocked flush count histogram/gauge    * @param blockedFlushCount the number of blocked flush since last tuning.    */
name|void
name|updateBlockedFlushCount
parameter_list|(
name|long
name|blockedFlushCount
parameter_list|)
function_decl|;
comment|/**    * Update/Set the unblocked flush count histogram/gauge    * @param unblockedFlushCount the number of unblocked flush since last tuning.    */
name|void
name|updateUnblockedFlushCount
parameter_list|(
name|long
name|unblockedFlushCount
parameter_list|)
function_decl|;
comment|/**    * Set the current blockcache size used gauge    * @param blockCacheSize the current memory usage in blockcache, in bytes.    */
name|void
name|setCurBlockCacheSizeGauge
parameter_list|(
name|long
name|blockCacheSize
parameter_list|)
function_decl|;
comment|/**    * Set the current global memstore size used gauge    * @param memStoreSize the current memory usage in memstore, in bytes.    */
name|void
name|setCurMemStoreSizeGauge
parameter_list|(
name|long
name|memStoreSize
parameter_list|)
function_decl|;
comment|/**    * Update the increase/decrease memstore size histogram    * @param memStoreDeltaSize the tuning result of memstore.    */
name|void
name|updateMemStoreDeltaSizeHistogram
parameter_list|(
name|int
name|memStoreDeltaSize
parameter_list|)
function_decl|;
comment|/**    * Update the increase/decrease blockcache size histogram    * @param blockCacheDeltaSize the tuning result of blockcache.    */
name|void
name|updateBlockCacheDeltaSizeHistogram
parameter_list|(
name|int
name|blockCacheDeltaSize
parameter_list|)
function_decl|;
comment|/**    * Increase the counter for tuner neither expanding memstore global size limit nor expanding    * blockcache max size.    */
name|void
name|increaseTunerDoNothingCounter
parameter_list|()
function_decl|;
comment|/**    * Increase the counter for heap occupancy percent above low watermark    */
name|void
name|increaseAboveHeapOccupancyLowWatermarkCounter
parameter_list|()
function_decl|;
comment|// Histograms
name|String
name|BLOCKED_FLUSH_NAME
init|=
literal|"blockedFlushes"
decl_stmt|;
name|String
name|BLOCKED_FLUSH_DESC
init|=
literal|"Histogram for the number of blocked flushes in the memstore"
decl_stmt|;
name|String
name|UNBLOCKED_FLUSH_NAME
init|=
literal|"unblockedFlushes"
decl_stmt|;
name|String
name|UNBLOCKED_FLUSH_DESC
init|=
literal|"Histogram for the number of unblocked flushes in the memstore"
decl_stmt|;
name|String
name|INC_MEMSTORE_TUNING_NAME
init|=
literal|"increaseMemStoreSize"
decl_stmt|;
name|String
name|INC_MEMSTORE_TUNING_DESC
init|=
literal|"Histogram for the heap memory tuner expanding memstore global size limit in bytes"
decl_stmt|;
name|String
name|DEC_MEMSTORE_TUNING_NAME
init|=
literal|"decreaseMemStoreSize"
decl_stmt|;
name|String
name|DEC_MEMSTORE_TUNING_DESC
init|=
literal|"Histogram for the heap memory tuner shrinking memstore global size limit in bytes"
decl_stmt|;
name|String
name|INC_BLOCKCACHE_TUNING_NAME
init|=
literal|"increaseBlockCacheSize"
decl_stmt|;
name|String
name|INC_BLOCKCACHE_TUNING_DESC
init|=
literal|"Histogram for the heap memory tuner expanding blockcache max heap size in bytes"
decl_stmt|;
name|String
name|DEC_BLOCKCACHE_TUNING_NAME
init|=
literal|"decreaseBlockCacheSize"
decl_stmt|;
name|String
name|DEC_BLOCKCACHE_TUNING_DESC
init|=
literal|"Histogram for the heap memory tuner shrinking blockcache max heap size in bytes"
decl_stmt|;
comment|// Gauges
name|String
name|BLOCKED_FLUSH_GAUGE_NAME
init|=
literal|"blockedFlushGauge"
decl_stmt|;
name|String
name|BLOCKED_FLUSH_GAUGE_DESC
init|=
literal|"Gauge for the blocked flush count before tuning"
decl_stmt|;
name|String
name|UNBLOCKED_FLUSH_GAUGE_NAME
init|=
literal|"unblockedFlushGauge"
decl_stmt|;
name|String
name|UNBLOCKED_FLUSH_GAUGE_DESC
init|=
literal|"Gauge for the unblocked flush count before tuning"
decl_stmt|;
name|String
name|MEMSTORE_SIZE_GAUGE_NAME
init|=
literal|"memStoreSize"
decl_stmt|;
name|String
name|MEMSTORE_SIZE_GAUGE_DESC
init|=
literal|"Global MemStore used in bytes by the RegionServer"
decl_stmt|;
name|String
name|BLOCKCACHE_SIZE_GAUGE_NAME
init|=
literal|"blockCacheSize"
decl_stmt|;
name|String
name|BLOCKCACHE_SIZE_GAUGE_DESC
init|=
literal|"BlockCache used in bytes by the RegionServer"
decl_stmt|;
comment|// Counters
name|String
name|DO_NOTHING_COUNTER_NAME
init|=
literal|"tunerDoNothingCounter"
decl_stmt|;
name|String
name|DO_NOTHING_COUNTER_DESC
init|=
literal|"The number of times that tuner neither expands memstore global size limit nor expands "
operator|+
literal|"blockcache max size"
decl_stmt|;
name|String
name|ABOVE_HEAP_LOW_WATERMARK_COUNTER_NAME
init|=
literal|"aboveHeapOccupancyLowWaterMarkCounter"
decl_stmt|;
name|String
name|ABOVE_HEAP_LOW_WATERMARK_COUNTER_DESC
init|=
literal|"The number of times that heap occupancy percent is above low watermark"
decl_stmt|;
block|}
end_interface

end_unit

