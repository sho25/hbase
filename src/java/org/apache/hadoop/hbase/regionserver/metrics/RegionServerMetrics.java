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
name|metrics
package|;
end_package

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
name|MetricsTimeVaryingRate
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

begin_comment
comment|/**   * This class is for maintaining the various regionserver statistics  * and publishing them through the metrics interfaces.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values.  */
end_comment

begin_class
specifier|public
class|class
name|RegionServerMetrics
implements|implements
name|Updater
block|{
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
comment|/**    * Sum of all the memcache sizes in this regionserver in MB    */
specifier|public
specifier|final
name|MetricsIntValue
name|memcacheSizeMB
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"memcacheSizeMB"
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
comment|// nought to do.
block|}
comment|/**    * Since this object is a registered updater, this method will be called    * periodically, e.g. every 5 seconds.    * @param unused     */
specifier|public
name|void
name|doUpdates
parameter_list|(
name|MetricsContext
name|unused
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
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
name|memcacheSizeMB
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
block|}
name|this
operator|.
name|metricsRecord
operator|.
name|update
argument_list|()
expr_stmt|;
name|this
operator|.
name|lastUpdate
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|resetAllMinMax
parameter_list|()
block|{
comment|// Nothing to do
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
name|getRequests
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
literal|"memcacheSize"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|memcacheSizeMB
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

