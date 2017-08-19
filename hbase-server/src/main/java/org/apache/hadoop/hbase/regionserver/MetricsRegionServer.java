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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CompatibilitySingletonFactory
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|MetricRegistries
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
name|MetricRegistry
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
name|Timer
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
name|shaded
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
comment|/**  *<p>  * This class is for maintaining the various regionserver statistics  * and publishing them through the metrics interfaces.  *</p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values.  */
end_comment

begin_class
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsRegionServer
block|{
specifier|private
name|MetricsRegionServerSource
name|serverSource
decl_stmt|;
specifier|private
name|MetricsRegionServerWrapper
name|regionServerWrapper
decl_stmt|;
specifier|private
name|MetricRegistry
name|metricRegistry
decl_stmt|;
specifier|private
name|Timer
name|bulkLoadTimer
decl_stmt|;
specifier|public
name|MetricsRegionServer
parameter_list|(
name|MetricsRegionServerWrapper
name|regionServerWrapper
parameter_list|)
block|{
name|this
argument_list|(
name|regionServerWrapper
argument_list|,
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|createServer
argument_list|(
name|regionServerWrapper
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create hbase-metrics module based metrics. The registry should already be registered by the
comment|// MetricsRegionServerSource
name|metricRegistry
operator|=
name|MetricRegistries
operator|.
name|global
argument_list|()
operator|.
name|get
argument_list|(
name|serverSource
operator|.
name|getMetricRegistryInfo
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// create and use metrics from the new hbase-metrics based registry.
name|bulkLoadTimer
operator|=
name|metricRegistry
operator|.
name|timer
argument_list|(
literal|"Bulkload"
argument_list|)
expr_stmt|;
block|}
name|MetricsRegionServer
parameter_list|(
name|MetricsRegionServerWrapper
name|regionServerWrapper
parameter_list|,
name|MetricsRegionServerSource
name|serverSource
parameter_list|)
block|{
name|this
operator|.
name|regionServerWrapper
operator|=
name|regionServerWrapper
expr_stmt|;
name|this
operator|.
name|serverSource
operator|=
name|serverSource
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|MetricsRegionServerSource
name|getMetricsSource
parameter_list|()
block|{
return|return
name|serverSource
return|;
block|}
specifier|public
name|MetricsRegionServerWrapper
name|getRegionServerWrapper
parameter_list|()
block|{
return|return
name|regionServerWrapper
return|;
block|}
specifier|public
name|void
name|updatePutBatch
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|>
literal|1000
condition|)
block|{
name|serverSource
operator|.
name|incrSlowPut
argument_list|()
expr_stmt|;
block|}
name|serverSource
operator|.
name|updatePutBatch
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updatePut
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updatePut
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateDelete
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updateDelete
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateDeleteBatch
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|>
literal|1000
condition|)
block|{
name|serverSource
operator|.
name|incrSlowDelete
argument_list|()
expr_stmt|;
block|}
name|serverSource
operator|.
name|updateDeleteBatch
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateCheckAndDelete
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updateCheckAndDelete
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateCheckAndPut
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updateCheckAndPut
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateGet
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|>
literal|1000
condition|)
block|{
name|serverSource
operator|.
name|incrSlowGet
argument_list|()
expr_stmt|;
block|}
name|serverSource
operator|.
name|updateGet
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateIncrement
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|>
literal|1000
condition|)
block|{
name|serverSource
operator|.
name|incrSlowIncrement
argument_list|()
expr_stmt|;
block|}
name|serverSource
operator|.
name|updateIncrement
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateAppend
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|>
literal|1000
condition|)
block|{
name|serverSource
operator|.
name|incrSlowAppend
argument_list|()
expr_stmt|;
block|}
name|serverSource
operator|.
name|updateAppend
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateReplay
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updateReplay
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateScanSize
parameter_list|(
name|long
name|scanSize
parameter_list|)
block|{
name|serverSource
operator|.
name|updateScanSize
argument_list|(
name|scanSize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateScanTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updateScanTime
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateSplitTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|serverSource
operator|.
name|updateSplitTime
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrSplitRequest
parameter_list|()
block|{
name|serverSource
operator|.
name|incrSplitRequest
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrSplitSuccess
parameter_list|()
block|{
name|serverSource
operator|.
name|incrSplitSuccess
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|updateFlush
parameter_list|(
name|long
name|t
parameter_list|,
name|long
name|memstoreSize
parameter_list|,
name|long
name|fileSize
parameter_list|)
block|{
name|serverSource
operator|.
name|updateFlushTime
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|serverSource
operator|.
name|updateFlushMemstoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|serverSource
operator|.
name|updateFlushOutputSize
argument_list|(
name|fileSize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateCompaction
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|long
name|t
parameter_list|,
name|int
name|inputFileCount
parameter_list|,
name|int
name|outputFileCount
parameter_list|,
name|long
name|inputBytes
parameter_list|,
name|long
name|outputBytes
parameter_list|)
block|{
name|serverSource
operator|.
name|updateCompactionTime
argument_list|(
name|isMajor
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|serverSource
operator|.
name|updateCompactionInputFileCount
argument_list|(
name|isMajor
argument_list|,
name|inputFileCount
argument_list|)
expr_stmt|;
name|serverSource
operator|.
name|updateCompactionOutputFileCount
argument_list|(
name|isMajor
argument_list|,
name|outputFileCount
argument_list|)
expr_stmt|;
name|serverSource
operator|.
name|updateCompactionInputSize
argument_list|(
name|isMajor
argument_list|,
name|inputBytes
argument_list|)
expr_stmt|;
name|serverSource
operator|.
name|updateCompactionOutputSize
argument_list|(
name|isMajor
argument_list|,
name|outputBytes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateBulkLoad
parameter_list|(
name|long
name|millis
parameter_list|)
block|{
name|this
operator|.
name|bulkLoadTimer
operator|.
name|updateMillis
argument_list|(
name|millis
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

