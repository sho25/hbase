begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metrics
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|HBaseInfo
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
name|MetricsRate
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
name|PersistentMetricsTimeVaryingRate
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
name|wal
operator|.
name|HLog
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
name|hbase
operator|.
name|util
operator|.
name|Strings
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
name|metrics
operator|.
name|ContextFactory
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
name|metrics
operator|.
name|MetricsContext
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
name|metrics
operator|.
name|MetricsRecord
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
name|metrics
operator|.
name|MetricsUtil
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
name|metrics
operator|.
name|Updater
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
name|metrics
operator|.
name|jvm
operator|.
name|JvmMetrics
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
name|metrics
operator|.
name|util
operator|.
name|MetricsIntValue
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
name|metrics
operator|.
name|util
operator|.
name|MetricsLongValue
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
name|metrics
operator|.
name|util
operator|.
name|MetricsRegistry
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
name|metrics
operator|.
name|util
operator|.
name|MetricsTimeVaryingRate
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryUsage
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various regionserver statistics  * and publishing them through the metrics interfaces.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values.  */
end_comment

begin_class
specifier|public
class|class
name|RegionServerMetrics
implements|implements
name|Updater
block|{
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"FieldCanBeLocal"
block|}
argument_list|)
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MetricsRecord
name|metricsRecord
decl_stmt|;
specifier|private
name|long
name|lastUpdate
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
name|long
name|lastExtUpdate
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
name|long
name|extendedPeriod
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MB
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|private
name|MetricsRegistry
name|registry
init|=
operator|new
name|MetricsRegistry
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|RegionServerStatistics
name|statistics
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingRate
name|atomicIncrementTime
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"atomicIncrementTime"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Count of regions carried by this regionserver    */
specifier|public
specifier|final
name|MetricsIntValue
name|regions
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"regions"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Block cache size.    */
specifier|public
specifier|final
name|MetricsLongValue
name|blockCacheSize
init|=
operator|new
name|MetricsLongValue
argument_list|(
literal|"blockCacheSize"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Block cache free size.    */
specifier|public
specifier|final
name|MetricsLongValue
name|blockCacheFree
init|=
operator|new
name|MetricsLongValue
argument_list|(
literal|"blockCacheFree"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Block cache item count.    */
specifier|public
specifier|final
name|MetricsLongValue
name|blockCacheCount
init|=
operator|new
name|MetricsLongValue
argument_list|(
literal|"blockCacheCount"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Block hit ratio.    */
specifier|public
specifier|final
name|MetricsIntValue
name|blockCacheHitRatio
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"blockCacheHitRatio"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Block hit caching ratio.  This only includes the requests to the block    * cache where caching was turned on.  See HBASE-2253.    */
specifier|public
specifier|final
name|MetricsIntValue
name|blockCacheHitCachingRatio
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"blockCacheHitCachingRatio"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/*    * Count of requests to the regionservers since last call to metrics update    */
specifier|private
specifier|final
name|MetricsRate
name|requests
init|=
operator|new
name|MetricsRate
argument_list|(
literal|"requests"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Count of stores open on the regionserver.    */
specifier|public
specifier|final
name|MetricsIntValue
name|stores
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"stores"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Count of storefiles open on the regionserver.    */
specifier|public
specifier|final
name|MetricsIntValue
name|storefiles
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"storefiles"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Sum of all the storefile index sizes in this regionserver in MB    */
specifier|public
specifier|final
name|MetricsIntValue
name|storefileIndexSizeMB
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"storefileIndexSizeMB"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Sum of all the memstore sizes in this regionserver in MB    */
specifier|public
specifier|final
name|MetricsIntValue
name|memstoreSizeMB
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"memstoreSizeMB"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Size of the compaction queue.    */
specifier|public
specifier|final
name|MetricsIntValue
name|compactionQueueSize
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"compactionQueueSize"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * filesystem read latency    */
specifier|public
specifier|final
name|MetricsTimeVaryingRate
name|fsReadLatency
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"fsReadLatency"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * filesystem write latency    */
specifier|public
specifier|final
name|MetricsTimeVaryingRate
name|fsWriteLatency
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"fsWriteLatency"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * filesystem sync latency    */
specifier|public
specifier|final
name|MetricsTimeVaryingRate
name|fsSyncLatency
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"fsSyncLatency"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * time each scheduled compaction takes    */
specifier|protected
specifier|final
name|PersistentMetricsTimeVaryingRate
name|compactionTime
init|=
operator|new
name|PersistentMetricsTimeVaryingRate
argument_list|(
literal|"compactionTime"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|PersistentMetricsTimeVaryingRate
name|compactionSize
init|=
operator|new
name|PersistentMetricsTimeVaryingRate
argument_list|(
literal|"compactionSize"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * time each scheduled flush takes    */
specifier|protected
specifier|final
name|PersistentMetricsTimeVaryingRate
name|flushTime
init|=
operator|new
name|PersistentMetricsTimeVaryingRate
argument_list|(
literal|"flushTime"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|PersistentMetricsTimeVaryingRate
name|flushSize
init|=
operator|new
name|PersistentMetricsTimeVaryingRate
argument_list|(
literal|"flushSize"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
name|RegionServerMetrics
parameter_list|()
block|{
name|MetricsContext
name|context
init|=
name|MetricsUtil
operator|.
name|getContext
argument_list|(
literal|"hbase"
argument_list|)
decl_stmt|;
name|metricsRecord
operator|=
name|MetricsUtil
operator|.
name|createRecord
argument_list|(
name|context
argument_list|,
literal|"regionserver"
argument_list|)
expr_stmt|;
name|String
name|name
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|metricsRecord
operator|.
name|setTag
argument_list|(
literal|"RegionServer"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|context
operator|.
name|registerUpdater
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// Add jvmmetrics.
name|JvmMetrics
operator|.
name|init
argument_list|(
literal|"RegionServer"
argument_list|,
name|name
argument_list|)
expr_stmt|;
comment|// Add Hbase Info metrics
name|HBaseInfo
operator|.
name|init
argument_list|()
expr_stmt|;
comment|// export for JMX
name|statistics
operator|=
operator|new
name|RegionServerStatistics
argument_list|(
name|this
operator|.
name|registry
argument_list|,
name|name
argument_list|)
expr_stmt|;
comment|// get custom attributes
try|try
block|{
name|Object
name|m
init|=
name|ContextFactory
operator|.
name|getFactory
argument_list|()
operator|.
name|getAttribute
argument_list|(
literal|"hbase.extendedperiod"
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|extendedPeriod
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
operator|(
name|String
operator|)
name|m
argument_list|)
operator|*
literal|1000
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Couldn't load ContextFactory for Metrics config info"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialized"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|statistics
operator|!=
literal|null
condition|)
name|statistics
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Since this object is a registered updater, this method will be called    * periodically, e.g. every 5 seconds.    * @param caller the metrics context that this responsible for calling us    */
specifier|public
name|void
name|doUpdates
parameter_list|(
name|MetricsContext
name|caller
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|this
operator|.
name|lastUpdate
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
comment|// has the extended period for long-living stats elapsed?
if|if
condition|(
name|this
operator|.
name|extendedPeriod
operator|>
literal|0
operator|&&
name|this
operator|.
name|lastUpdate
operator|-
name|this
operator|.
name|lastExtUpdate
operator|>=
name|this
operator|.
name|extendedPeriod
condition|)
block|{
name|this
operator|.
name|lastExtUpdate
operator|=
name|this
operator|.
name|lastUpdate
expr_stmt|;
name|this
operator|.
name|compactionTime
operator|.
name|resetMinMaxAvg
argument_list|()
expr_stmt|;
name|this
operator|.
name|compactionSize
operator|.
name|resetMinMaxAvg
argument_list|()
expr_stmt|;
name|this
operator|.
name|flushTime
operator|.
name|resetMinMaxAvg
argument_list|()
expr_stmt|;
name|this
operator|.
name|flushSize
operator|.
name|resetMinMaxAvg
argument_list|()
expr_stmt|;
name|this
operator|.
name|resetAllMinMax
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|stores
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|storefiles
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|storefileIndexSizeMB
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|memstoreSizeMB
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|regions
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|requests
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactionQueueSize
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCacheSize
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCacheFree
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCacheCount
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCacheHitRatio
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCacheHitCachingRatio
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
comment|// Mix in HFile and HLog metrics
comment|// Be careful. Here is code for MTVR from up in hadoop:
comment|// public synchronized void inc(final int numOps, final long time) {
comment|//   currentData.numOperations += numOps;
comment|//   currentData.time += time;
comment|//   long timePerOps = time/numOps;
comment|//    minMax.update(timePerOps);
comment|// }
comment|// Means you can't pass a numOps of zero or get a ArithmeticException / by zero.
name|int
name|ops
init|=
operator|(
name|int
operator|)
name|HFile
operator|.
name|getReadOps
argument_list|()
decl_stmt|;
if|if
condition|(
name|ops
operator|!=
literal|0
condition|)
name|this
operator|.
name|fsReadLatency
operator|.
name|inc
argument_list|(
name|ops
argument_list|,
name|HFile
operator|.
name|getReadTime
argument_list|()
argument_list|)
expr_stmt|;
name|ops
operator|=
operator|(
name|int
operator|)
name|HFile
operator|.
name|getWriteOps
argument_list|()
expr_stmt|;
if|if
condition|(
name|ops
operator|!=
literal|0
condition|)
name|this
operator|.
name|fsWriteLatency
operator|.
name|inc
argument_list|(
name|ops
argument_list|,
name|HFile
operator|.
name|getWriteTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// mix in HLog metrics
name|ops
operator|=
operator|(
name|int
operator|)
name|HLog
operator|.
name|getWriteOps
argument_list|()
expr_stmt|;
if|if
condition|(
name|ops
operator|!=
literal|0
condition|)
name|this
operator|.
name|fsWriteLatency
operator|.
name|inc
argument_list|(
name|ops
argument_list|,
name|HLog
operator|.
name|getWriteTime
argument_list|()
argument_list|)
expr_stmt|;
name|ops
operator|=
operator|(
name|int
operator|)
name|HLog
operator|.
name|getSyncOps
argument_list|()
expr_stmt|;
if|if
condition|(
name|ops
operator|!=
literal|0
condition|)
name|this
operator|.
name|fsSyncLatency
operator|.
name|inc
argument_list|(
name|ops
argument_list|,
name|HLog
operator|.
name|getSyncTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// push the result
name|this
operator|.
name|fsReadLatency
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|fsWriteLatency
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|fsSyncLatency
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactionTime
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactionSize
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|flushTime
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|flushSize
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|metricsRecord
operator|.
name|update
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|resetAllMinMax
parameter_list|()
block|{
name|this
operator|.
name|atomicIncrementTime
operator|.
name|resetMinMax
argument_list|()
expr_stmt|;
name|this
operator|.
name|fsReadLatency
operator|.
name|resetMinMax
argument_list|()
expr_stmt|;
name|this
operator|.
name|fsWriteLatency
operator|.
name|resetMinMax
argument_list|()
expr_stmt|;
name|this
operator|.
name|fsSyncLatency
operator|.
name|resetMinMax
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return Count of requests.    */
specifier|public
name|float
name|getRequests
parameter_list|()
block|{
return|return
name|this
operator|.
name|requests
operator|.
name|getPreviousIntervalValue
argument_list|()
return|;
block|}
comment|/**    * @param compact history in<time, size>    */
specifier|public
specifier|synchronized
name|void
name|addCompaction
parameter_list|(
specifier|final
name|Pair
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|compact
parameter_list|)
block|{
name|this
operator|.
name|compactionTime
operator|.
name|inc
argument_list|(
name|compact
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactionSize
operator|.
name|inc
argument_list|(
name|compact
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param flushes history in<time, size>    */
specifier|public
specifier|synchronized
name|void
name|addFlush
parameter_list|(
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|>
name|flushes
parameter_list|)
block|{
for|for
control|(
name|Pair
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|f
range|:
name|flushes
control|)
block|{
name|this
operator|.
name|flushTime
operator|.
name|inc
argument_list|(
name|f
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|flushSize
operator|.
name|inc
argument_list|(
name|f
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param inc How much to add to requests.    */
specifier|public
name|void
name|incrementRequests
parameter_list|(
specifier|final
name|int
name|inc
parameter_list|)
block|{
name|this
operator|.
name|requests
operator|.
name|inc
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|seconds
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|this
operator|.
name|lastUpdate
operator|)
operator|/
literal|1000
argument_list|)
decl_stmt|;
if|if
condition|(
name|seconds
operator|==
literal|0
condition|)
block|{
name|seconds
operator|=
literal|1
expr_stmt|;
block|}
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"request"
argument_list|,
name|Float
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|requests
operator|.
name|getPreviousIntervalValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"regions"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|regions
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"stores"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|stores
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storefiles"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storefiles
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"storefileIndexSize"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|storefileIndexSizeMB
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"memstoreSize"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|memstoreSizeMB
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"compactionQueueSize"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|compactionQueueSize
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Duplicate from jvmmetrics because metrics are private there so
comment|// inaccessible.
name|MemoryUsage
name|memory
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
decl_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"usedHeap"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|memory
operator|.
name|getUsed
argument_list|()
operator|/
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"maxHeap"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|memory
operator|.
name|getMax
argument_list|()
operator|/
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
name|this
operator|.
name|blockCacheSize
operator|.
name|getName
argument_list|()
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|blockCacheSize
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
name|this
operator|.
name|blockCacheFree
operator|.
name|getName
argument_list|()
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|blockCacheFree
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
name|this
operator|.
name|blockCacheCount
operator|.
name|getName
argument_list|()
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|blockCacheCount
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
name|this
operator|.
name|blockCacheHitRatio
operator|.
name|getName
argument_list|()
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|blockCacheHitRatio
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
name|this
operator|.
name|blockCacheHitCachingRatio
operator|.
name|getName
argument_list|()
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|blockCacheHitCachingRatio
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

