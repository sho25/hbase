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
name|regionserver
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

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
name|AtomicLong
import|;
end_import

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
name|AtomicInteger
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
name|regionserver
operator|.
name|HRegion
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
name|HRegionServer
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
name|util
operator|.
name|MetricsBase
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

begin_comment
comment|/**  *  * This class is for maintaining  the various RPC statistics  * and publishing them through the metrics interfaces.  * This also registers the JMX MBean for RPC.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values;  * for example: rpcQueueTime.inc(time)  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServerDynamicMetrics
implements|implements
name|Updater
block|{
specifier|private
specifier|static
specifier|final
name|String
name|UNABLE_TO_CLEAR
init|=
literal|"Unable to clear RegionServerDynamicMetrics"
decl_stmt|;
specifier|private
name|MetricsRecord
name|metricsRecord
decl_stmt|;
specifier|private
name|MetricsContext
name|context
decl_stmt|;
specifier|private
specifier|final
name|RegionServerDynamicStatistics
name|rsDynamicStatistics
decl_stmt|;
specifier|private
name|Method
name|updateMbeanInfoIfMetricsListChanged
init|=
literal|null
decl_stmt|;
specifier|private
name|HRegionServer
name|regionServer
decl_stmt|;
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
name|RegionServerDynamicStatistics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|reflectionInitialized
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|needsUpdateMessage
init|=
literal|false
decl_stmt|;
specifier|private
name|Field
name|recordMetricMapField
decl_stmt|;
specifier|private
name|Field
name|registryMetricMapField
decl_stmt|;
comment|/**    * The metrics variables are public:    *  - they can be set directly by calling their set/inc methods    *  -they can also be read directly - e.g. JMX does this.    */
specifier|public
specifier|final
name|MetricsRegistry
name|registry
init|=
operator|new
name|MetricsRegistry
argument_list|()
decl_stmt|;
specifier|private
name|RegionServerDynamicMetrics
parameter_list|(
name|HRegionServer
name|regionServer
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|MetricsUtil
operator|.
name|getContext
argument_list|(
literal|"hbase-dynamic"
argument_list|)
expr_stmt|;
name|this
operator|.
name|metricsRecord
operator|=
name|MetricsUtil
operator|.
name|createRecord
argument_list|(
name|this
operator|.
name|context
argument_list|,
literal|"RegionServerDynamicStatistics"
argument_list|)
expr_stmt|;
name|context
operator|.
name|registerUpdater
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsDynamicStatistics
operator|=
operator|new
name|RegionServerDynamicStatistics
argument_list|(
name|this
operator|.
name|registry
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServer
operator|=
name|regionServer
expr_stmt|;
try|try
block|{
name|updateMbeanInfoIfMetricsListChanged
operator|=
name|this
operator|.
name|rsDynamicStatistics
operator|.
name|getClass
argument_list|()
operator|.
name|getSuperclass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"updateMbeanInfoIfMetricsListChanged"
argument_list|,
operator|new
name|Class
index|[]
block|{}
argument_list|)
expr_stmt|;
name|updateMbeanInfoIfMetricsListChanged
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|RegionServerDynamicMetrics
name|newInstance
parameter_list|(
name|HRegionServer
name|regionServer
parameter_list|)
block|{
name|RegionServerDynamicMetrics
name|metrics
init|=
operator|new
name|RegionServerDynamicMetrics
argument_list|(
name|regionServer
argument_list|)
decl_stmt|;
return|return
name|metrics
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setNumericMetric
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|amt
parameter_list|)
block|{
name|MetricsLongValue
name|m
init|=
operator|(
name|MetricsLongValue
operator|)
name|registry
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
condition|)
block|{
name|m
operator|=
operator|new
name|MetricsLongValue
argument_list|(
name|name
argument_list|,
name|this
operator|.
name|registry
argument_list|)
expr_stmt|;
name|this
operator|.
name|needsUpdateMessage
operator|=
literal|true
expr_stmt|;
block|}
name|m
operator|.
name|set
argument_list|(
name|amt
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|incrTimeVaryingMetric
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|amt
parameter_list|,
name|int
name|numOps
parameter_list|)
block|{
name|MetricsTimeVaryingRate
name|m
init|=
operator|(
name|MetricsTimeVaryingRate
operator|)
name|registry
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
condition|)
block|{
name|m
operator|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
name|name
argument_list|,
name|this
operator|.
name|registry
argument_list|)
expr_stmt|;
name|this
operator|.
name|needsUpdateMessage
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|numOps
operator|>
literal|0
condition|)
block|{
name|m
operator|.
name|inc
argument_list|(
name|numOps
argument_list|,
name|amt
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Clear all metrics this exposes.     * Uses reflection to clear them from hadoop metrics side as well.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|needsUpdateMessage
operator|=
literal|true
expr_stmt|;
comment|// If this is the first clear use reflection to get the two maps that hold copies of our
comment|// metrics on the hadoop metrics side. We have to use reflection because there is not
comment|// remove metrics on the hadoop side. If we can't get them then clearing old metrics
comment|// is not possible and bailing out early is our best option.
if|if
condition|(
operator|!
name|this
operator|.
name|reflectionInitialized
condition|)
block|{
name|this
operator|.
name|reflectionInitialized
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|this
operator|.
name|recordMetricMapField
operator|=
name|this
operator|.
name|metricsRecord
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"metricTable"
argument_list|)
expr_stmt|;
name|this
operator|.
name|recordMetricMapField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|this
operator|.
name|registryMetricMapField
operator|=
name|this
operator|.
name|registry
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"metricsList"
argument_list|)
expr_stmt|;
name|this
operator|.
name|registryMetricMapField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|//If we found both fields then try and clear the maps.
if|if
condition|(
name|this
operator|.
name|recordMetricMapField
operator|!=
literal|null
operator|&&
name|this
operator|.
name|registryMetricMapField
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Map
name|recordMap
init|=
operator|(
name|Map
operator|)
name|this
operator|.
name|recordMetricMapField
operator|.
name|get
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
decl_stmt|;
name|recordMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Map
name|registryMap
init|=
operator|(
name|Map
operator|)
name|this
operator|.
name|registryMetricMapField
operator|.
name|get
argument_list|(
name|this
operator|.
name|registry
argument_list|)
decl_stmt|;
name|registryMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|UNABLE_TO_CLEAR
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Push the metrics to the monitoring subsystem on doUpdate() call.    * @param context ctx    */
specifier|public
name|void
name|doUpdates
parameter_list|(
name|MetricsContext
name|context
parameter_list|)
block|{
comment|/* get dynamically created numeric metrics, and push the metrics */
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|entry
range|:
name|RegionMetricsStorage
operator|.
name|getNumericMetrics
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|setNumericMetric
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/* export estimated size of all response queues */
if|if
condition|(
name|regionServer
operator|!=
literal|null
condition|)
block|{
name|long
name|responseQueueSize
init|=
name|regionServer
operator|.
name|getResponseQueueSize
argument_list|()
decl_stmt|;
name|this
operator|.
name|setNumericMetric
argument_list|(
literal|"responseQueuesSize"
argument_list|,
name|responseQueueSize
argument_list|)
expr_stmt|;
block|}
comment|/* get dynamically created numeric metrics, and push the metrics.      * These ones aren't to be reset; they are cumulative. */
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|entry
range|:
name|RegionMetricsStorage
operator|.
name|getNumericPersistentMetrics
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|setNumericMetric
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/* get dynamically created time varying metrics, and push the metrics */
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|AtomicLong
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|>
name|entry
range|:
name|RegionMetricsStorage
operator|.
name|getTimeVaryingMetrics
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Pair
argument_list|<
name|AtomicLong
argument_list|,
name|AtomicInteger
argument_list|>
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|this
operator|.
name|incrTimeVaryingMetric
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
operator|.
name|getFirst
argument_list|()
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
argument_list|,
name|value
operator|.
name|getSecond
argument_list|()
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// If there are new metrics sending this message to jmx tells it to update everything.
comment|// This is not ideal we should just move to metrics2 that has full support for dynamic metrics.
if|if
condition|(
name|needsUpdateMessage
condition|)
block|{
try|try
block|{
if|if
condition|(
name|updateMbeanInfoIfMetricsListChanged
operator|!=
literal|null
condition|)
block|{
name|updateMbeanInfoIfMetricsListChanged
operator|.
name|invoke
argument_list|(
name|this
operator|.
name|rsDynamicStatistics
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|needsUpdateMessage
operator|=
literal|false
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|registry
init|)
block|{
comment|// Iterate through the registry to propagate the different rpc metrics.
for|for
control|(
name|String
name|metricName
range|:
name|registry
operator|.
name|getKeyList
argument_list|()
control|)
block|{
name|MetricsBase
name|value
init|=
name|registry
operator|.
name|get
argument_list|(
name|metricName
argument_list|)
decl_stmt|;
name|value
operator|.
name|pushMetric
argument_list|(
name|metricsRecord
argument_list|)
expr_stmt|;
block|}
block|}
name|metricsRecord
operator|.
name|update
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|rsDynamicStatistics
operator|!=
literal|null
condition|)
name|rsDynamicStatistics
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

