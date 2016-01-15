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
name|client
package|;
end_package

begin_import
import|import
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Counter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Histogram
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricRegistry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|JmxReporter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|RatioGauge
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
name|ServerName
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MutateRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
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
name|Bytes
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
name|ConcurrentHashMap
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
name|ConcurrentSkipListMap
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
name|ConcurrentMap
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
name|ThreadPoolExecutor
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
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricRegistry
operator|.
name|name
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various connection statistics and publishing them through  * the metrics interfaces.  *  * This class manages its own {@link MetricRegistry} and {@link JmxReporter} so as to not  * conflict with other uses of Yammer Metrics within the client application. Instantiating  * this class implicitly creates and "starts" instances of these classes; be sure to call  * {@link #shutdown()} to terminate the thread pools they allocate.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsConnection
block|{
comment|/** Set this key to {@code true} to enable metrics collection of client requests. */
specifier|public
specifier|static
specifier|final
name|String
name|CLIENT_SIDE_METRICS_ENABLED_KEY
init|=
literal|"hbase.client.metrics.enable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DRTN_BASE
init|=
literal|"rpcCallDurationMs_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REQ_BASE
init|=
literal|"rpcCallRequestSizeBytes_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|RESP_BASE
init|=
literal|"rpcCallResponseSizeBytes_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MEMLOAD_BASE
init|=
literal|"memstoreLoad_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HEAP_BASE
init|=
literal|"heapOccupancy_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLIENT_SVC
init|=
name|ClientService
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|/** A container class for collecting details about the RPC call as it percolates. */
specifier|public
specifier|static
class|class
name|CallStats
block|{
specifier|private
name|long
name|requestSizeBytes
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|responseSizeBytes
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|startTime
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|callTimeMs
init|=
literal|0
decl_stmt|;
specifier|public
name|long
name|getRequestSizeBytes
parameter_list|()
block|{
return|return
name|requestSizeBytes
return|;
block|}
specifier|public
name|void
name|setRequestSizeBytes
parameter_list|(
name|long
name|requestSizeBytes
parameter_list|)
block|{
name|this
operator|.
name|requestSizeBytes
operator|=
name|requestSizeBytes
expr_stmt|;
block|}
specifier|public
name|long
name|getResponseSizeBytes
parameter_list|()
block|{
return|return
name|responseSizeBytes
return|;
block|}
specifier|public
name|void
name|setResponseSizeBytes
parameter_list|(
name|long
name|responseSizeBytes
parameter_list|)
block|{
name|this
operator|.
name|responseSizeBytes
operator|=
name|responseSizeBytes
expr_stmt|;
block|}
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
specifier|public
name|void
name|setStartTime
parameter_list|(
name|long
name|startTime
parameter_list|)
block|{
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
block|}
specifier|public
name|long
name|getCallTimeMs
parameter_list|()
block|{
return|return
name|callTimeMs
return|;
block|}
specifier|public
name|void
name|setCallTimeMs
parameter_list|(
name|long
name|callTimeMs
parameter_list|)
block|{
name|this
operator|.
name|callTimeMs
operator|=
name|callTimeMs
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
class|class
name|CallTracker
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|final
name|Timer
name|callTimer
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|final
name|Histogram
name|reqHist
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|final
name|Histogram
name|respHist
decl_stmt|;
specifier|private
name|CallTracker
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|subName
parameter_list|,
name|String
name|scope
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|CLIENT_SVC
argument_list|)
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
operator|.
name|append
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|subName
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
operator|.
name|append
argument_list|(
name|subName
argument_list|)
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|name
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|callTimer
operator|=
name|registry
operator|.
name|timer
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
name|DRTN_BASE
operator|+
name|this
operator|.
name|name
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|reqHist
operator|=
name|registry
operator|.
name|histogram
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
name|REQ_BASE
operator|+
name|this
operator|.
name|name
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|respHist
operator|=
name|registry
operator|.
name|histogram
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
name|RESP_BASE
operator|+
name|this
operator|.
name|name
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|CallTracker
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|scope
parameter_list|)
block|{
name|this
argument_list|(
name|registry
argument_list|,
name|name
argument_list|,
literal|null
argument_list|,
name|scope
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateRpc
parameter_list|(
name|CallStats
name|stats
parameter_list|)
block|{
name|this
operator|.
name|callTimer
operator|.
name|update
argument_list|(
name|stats
operator|.
name|getCallTimeMs
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|reqHist
operator|.
name|update
argument_list|(
name|stats
operator|.
name|getRequestSizeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|respHist
operator|.
name|update
argument_list|(
name|stats
operator|.
name|getResponseSizeBytes
argument_list|()
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
return|return
literal|"CallTracker:"
operator|+
name|name
return|;
block|}
block|}
specifier|protected
specifier|static
class|class
name|RegionStats
block|{
specifier|final
name|String
name|name
decl_stmt|;
specifier|final
name|Histogram
name|memstoreLoadHist
decl_stmt|;
specifier|final
name|Histogram
name|heapOccupancyHist
decl_stmt|;
specifier|public
name|RegionStats
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|memstoreLoadHist
operator|=
name|registry
operator|.
name|histogram
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
name|MEMLOAD_BASE
operator|+
name|this
operator|.
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOccupancyHist
operator|=
name|registry
operator|.
name|histogram
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
name|HEAP_BASE
operator|+
name|this
operator|.
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|ClientProtos
operator|.
name|RegionLoadStats
name|regionStatistics
parameter_list|)
block|{
name|this
operator|.
name|memstoreLoadHist
operator|.
name|update
argument_list|(
name|regionStatistics
operator|.
name|getMemstoreLoad
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOccupancyHist
operator|.
name|update
argument_list|(
name|regionStatistics
operator|.
name|getHeapOccupancy
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|protected
specifier|static
class|class
name|RunnerStats
block|{
specifier|final
name|Counter
name|normalRunners
decl_stmt|;
specifier|final
name|Counter
name|delayRunners
decl_stmt|;
specifier|final
name|Histogram
name|delayIntevalHist
decl_stmt|;
specifier|public
name|RunnerStats
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|)
block|{
name|this
operator|.
name|normalRunners
operator|=
name|registry
operator|.
name|counter
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
literal|"normalRunnersCount"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|delayRunners
operator|=
name|registry
operator|.
name|counter
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
literal|"delayRunnersCount"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|delayIntevalHist
operator|=
name|registry
operator|.
name|histogram
argument_list|(
name|name
argument_list|(
name|MetricsConnection
operator|.
name|class
argument_list|,
literal|"delayIntervalHist"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrNormalRunners
parameter_list|()
block|{
name|this
operator|.
name|normalRunners
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrDelayRunners
parameter_list|()
block|{
name|this
operator|.
name|delayRunners
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|updateDelayInterval
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
name|this
operator|.
name|delayIntevalHist
operator|.
name|update
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|ConcurrentHashMap
argument_list|<
name|ServerName
argument_list|,
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionStats
argument_list|>
argument_list|>
name|serverStats
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ServerName
argument_list|,
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionStats
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|updateServerStats
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
name|Object
name|r
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|r
operator|instanceof
name|Result
operator|)
condition|)
block|{
return|return;
block|}
name|Result
name|result
init|=
operator|(
name|Result
operator|)
name|r
decl_stmt|;
name|ClientProtos
operator|.
name|RegionLoadStats
name|stats
init|=
name|result
operator|.
name|getStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
name|name
init|=
name|serverName
operator|.
name|getServerName
argument_list|()
operator|+
literal|","
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionStats
argument_list|>
name|rsStats
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|serverStats
operator|.
name|containsKey
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|rsStats
operator|=
name|serverStats
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rsStats
operator|=
name|serverStats
operator|.
name|putIfAbsent
argument_list|(
name|serverName
argument_list|,
operator|new
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionStats
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|rsStats
operator|==
literal|null
condition|)
block|{
name|rsStats
operator|=
name|serverStats
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|RegionStats
name|regionStats
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|rsStats
operator|.
name|containsKey
argument_list|(
name|regionName
argument_list|)
condition|)
block|{
name|regionStats
operator|=
name|rsStats
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionStats
operator|=
name|rsStats
operator|.
name|putIfAbsent
argument_list|(
name|regionName
argument_list|,
operator|new
name|RegionStats
argument_list|(
name|this
operator|.
name|registry
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionStats
operator|==
literal|null
condition|)
block|{
name|regionStats
operator|=
name|rsStats
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
block|}
block|}
name|regionStats
operator|.
name|update
argument_list|(
name|stats
argument_list|)
expr_stmt|;
block|}
comment|/** A lambda for dispatching to the appropriate metric factory method */
specifier|private
specifier|static
interface|interface
name|NewMetric
parameter_list|<
name|T
parameter_list|>
block|{
name|T
name|newMetric
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|scope
parameter_list|)
function_decl|;
block|}
comment|/** Anticipated number of metric entries */
specifier|private
specifier|static
specifier|final
name|int
name|CAPACITY
init|=
literal|50
decl_stmt|;
comment|/** Default load factor from {@link java.util.HashMap#DEFAULT_LOAD_FACTOR} */
specifier|private
specifier|static
specifier|final
name|float
name|LOAD_FACTOR
init|=
literal|0.75f
decl_stmt|;
comment|/**    * Anticipated number of concurrent accessor threads, from    * {@link ConnectionImplementation#getBatchPool()}    */
specifier|private
specifier|static
specifier|final
name|int
name|CONCURRENCY_LEVEL
init|=
literal|256
decl_stmt|;
specifier|private
specifier|final
name|MetricRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|JmxReporter
name|reporter
decl_stmt|;
specifier|private
specifier|final
name|String
name|scope
decl_stmt|;
specifier|private
specifier|final
name|NewMetric
argument_list|<
name|Timer
argument_list|>
name|timerFactory
init|=
operator|new
name|NewMetric
argument_list|<
name|Timer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Timer
name|newMetric
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|scope
parameter_list|)
block|{
return|return
name|registry
operator|.
name|timer
argument_list|(
name|name
argument_list|(
name|clazz
argument_list|,
name|name
argument_list|,
name|scope
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|NewMetric
argument_list|<
name|Histogram
argument_list|>
name|histogramFactory
init|=
operator|new
name|NewMetric
argument_list|<
name|Histogram
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Histogram
name|newMetric
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|scope
parameter_list|)
block|{
return|return
name|registry
operator|.
name|histogram
argument_list|(
name|name
argument_list|(
name|clazz
argument_list|,
name|name
argument_list|,
name|scope
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// static metrics
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|Counter
name|metaCacheHits
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|Counter
name|metaCacheMisses
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|getTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|scanTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|appendTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|deleteTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|incrementTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|putTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|CallTracker
name|multiTracker
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|RunnerStats
name|runnerStats
decl_stmt|;
comment|// dynamic metrics
comment|// These maps are used to cache references to the metric instances that are managed by the
comment|// registry. I don't think their use perfectly removes redundant allocations, but it's
comment|// a big improvement over calling registry.newMetric each time.
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Timer
argument_list|>
name|rpcTimers
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|(
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|CONCURRENCY_LEVEL
argument_list|)
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|protected
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Histogram
argument_list|>
name|rpcHistograms
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|(
name|CAPACITY
operator|*
literal|2
comment|/* tracking both request and response sizes */
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|CONCURRENCY_LEVEL
argument_list|)
decl_stmt|;
specifier|public
name|MetricsConnection
parameter_list|(
specifier|final
name|ConnectionImplementation
name|conn
parameter_list|)
block|{
name|this
operator|.
name|scope
operator|=
name|conn
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|registry
operator|=
operator|new
name|MetricRegistry
argument_list|()
expr_stmt|;
specifier|final
name|ThreadPoolExecutor
name|batchPool
init|=
operator|(
name|ThreadPoolExecutor
operator|)
name|conn
operator|.
name|getCurrentBatchPool
argument_list|()
decl_stmt|;
specifier|final
name|ThreadPoolExecutor
name|metaPool
init|=
operator|(
name|ThreadPoolExecutor
operator|)
name|conn
operator|.
name|getCurrentMetaLookupPool
argument_list|()
decl_stmt|;
name|this
operator|.
name|registry
operator|.
name|register
argument_list|(
name|name
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"executorPoolActiveThreads"
argument_list|,
name|scope
argument_list|)
argument_list|,
operator|new
name|RatioGauge
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Ratio
name|getRatio
parameter_list|()
block|{
return|return
name|Ratio
operator|.
name|of
argument_list|(
name|batchPool
operator|.
name|getActiveCount
argument_list|()
argument_list|,
name|batchPool
operator|.
name|getMaximumPoolSize
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|registry
operator|.
name|register
argument_list|(
name|name
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"metaPoolActiveThreads"
argument_list|,
name|scope
argument_list|)
argument_list|,
operator|new
name|RatioGauge
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Ratio
name|getRatio
parameter_list|()
block|{
return|return
name|Ratio
operator|.
name|of
argument_list|(
name|metaPool
operator|.
name|getActiveCount
argument_list|()
argument_list|,
name|metaPool
operator|.
name|getMaximumPoolSize
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaCacheHits
operator|=
name|registry
operator|.
name|counter
argument_list|(
name|name
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"metaCacheHits"
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaCacheMisses
operator|=
name|registry
operator|.
name|counter
argument_list|(
name|name
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
literal|"metaCacheMisses"
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|getTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Get"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Scan"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|appendTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Mutate"
argument_list|,
literal|"Append"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|deleteTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Mutate"
argument_list|,
literal|"Delete"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|incrementTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Mutate"
argument_list|,
literal|"Increment"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|putTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Mutate"
argument_list|,
literal|"Put"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|multiTracker
operator|=
operator|new
name|CallTracker
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"Multi"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|this
operator|.
name|runnerStats
operator|=
operator|new
name|RunnerStats
argument_list|(
name|this
operator|.
name|registry
argument_list|)
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|JmxReporter
operator|.
name|forRegistry
argument_list|(
name|this
operator|.
name|registry
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|reporter
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|this
operator|.
name|reporter
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
comment|/** Produce an instance of {@link CallStats} for clients to attach to RPCs. */
specifier|public
specifier|static
name|CallStats
name|newCallStats
parameter_list|()
block|{
comment|// TODO: instance pool to reduce GC?
return|return
operator|new
name|CallStats
argument_list|()
return|;
block|}
comment|/** Increment the number of meta cache hits. */
specifier|public
name|void
name|incrMetaCacheHit
parameter_list|()
block|{
name|metaCacheHits
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
comment|/** Increment the number of meta cache misses. */
specifier|public
name|void
name|incrMetaCacheMiss
parameter_list|()
block|{
name|metaCacheMisses
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
comment|/** Increment the number of normal runner counts. */
specifier|public
name|void
name|incrNormalRunners
parameter_list|()
block|{
name|this
operator|.
name|runnerStats
operator|.
name|incrNormalRunners
argument_list|()
expr_stmt|;
block|}
comment|/** Increment the number of delay runner counts. */
specifier|public
name|void
name|incrDelayRunners
parameter_list|()
block|{
name|this
operator|.
name|runnerStats
operator|.
name|incrDelayRunners
argument_list|()
expr_stmt|;
block|}
comment|/** Update delay interval of delay runner. */
specifier|public
name|void
name|updateDelayInterval
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
name|this
operator|.
name|runnerStats
operator|.
name|updateDelayInterval
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a metric for {@code key} from {@code map}, or create it with {@code factory}.    */
specifier|private
parameter_list|<
name|T
parameter_list|>
name|T
name|getMetric
parameter_list|(
name|String
name|key
parameter_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|map
parameter_list|,
name|NewMetric
argument_list|<
name|T
argument_list|>
name|factory
parameter_list|)
block|{
name|T
name|t
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
name|t
operator|=
name|factory
operator|.
name|newMetric
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
name|key
argument_list|,
name|scope
argument_list|)
expr_stmt|;
name|map
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
comment|/** Update call stats for non-critical-path methods */
specifier|private
name|void
name|updateRpcGeneric
parameter_list|(
name|MethodDescriptor
name|method
parameter_list|,
name|CallStats
name|stats
parameter_list|)
block|{
specifier|final
name|String
name|methodName
init|=
name|method
operator|.
name|getService
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"_"
operator|+
name|method
operator|.
name|getName
argument_list|()
decl_stmt|;
name|getMetric
argument_list|(
name|DRTN_BASE
operator|+
name|methodName
argument_list|,
name|rpcTimers
argument_list|,
name|timerFactory
argument_list|)
operator|.
name|update
argument_list|(
name|stats
operator|.
name|getCallTimeMs
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|getMetric
argument_list|(
name|REQ_BASE
operator|+
name|methodName
argument_list|,
name|rpcHistograms
argument_list|,
name|histogramFactory
argument_list|)
operator|.
name|update
argument_list|(
name|stats
operator|.
name|getRequestSizeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|getMetric
argument_list|(
name|RESP_BASE
operator|+
name|methodName
argument_list|,
name|rpcHistograms
argument_list|,
name|histogramFactory
argument_list|)
operator|.
name|update
argument_list|(
name|stats
operator|.
name|getResponseSizeBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Report RPC context to metrics system. */
specifier|public
name|void
name|updateRpc
parameter_list|(
name|MethodDescriptor
name|method
parameter_list|,
name|Message
name|param
parameter_list|,
name|CallStats
name|stats
parameter_list|)
block|{
comment|// this implementation is tied directly to protobuf implementation details. would be better
comment|// if we could dispatch based on something static, ie, request Message type.
if|if
condition|(
name|method
operator|.
name|getService
argument_list|()
operator|==
name|ClientService
operator|.
name|getDescriptor
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|method
operator|.
name|getIndex
argument_list|()
condition|)
block|{
case|case
literal|0
case|:
assert|assert
literal|"Get"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
name|getTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
case|case
literal|1
case|:
assert|assert
literal|"Mutate"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
specifier|final
name|MutationType
name|mutationType
init|=
operator|(
operator|(
name|MutateRequest
operator|)
name|param
operator|)
operator|.
name|getMutation
argument_list|()
operator|.
name|getMutateType
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|mutationType
condition|)
block|{
case|case
name|APPEND
case|:
name|appendTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
case|case
name|DELETE
case|:
name|deleteTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
case|case
name|INCREMENT
case|:
name|incrementTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
case|case
name|PUT
case|:
name|putTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unrecognized mutation type "
operator|+
name|mutationType
argument_list|)
throw|;
block|}
case|case
literal|2
case|:
assert|assert
literal|"Scan"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
name|scanTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
case|case
literal|3
case|:
assert|assert
literal|"BulkLoadHFile"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
comment|// use generic implementation
break|break;
case|case
literal|4
case|:
assert|assert
literal|"ExecService"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
comment|// use generic implementation
break|break;
case|case
literal|5
case|:
assert|assert
literal|"ExecRegionServerService"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
comment|// use generic implementation
break|break;
case|case
literal|6
case|:
assert|assert
literal|"Multi"
operator|.
name|equals
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
name|multiTracker
operator|.
name|updateRpc
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unrecognized ClientService RPC type "
operator|+
name|method
operator|.
name|getFullName
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|// Fallback to dynamic registry lookup for DDL methods.
name|updateRpcGeneric
argument_list|(
name|method
argument_list|,
name|stats
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

