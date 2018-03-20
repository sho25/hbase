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
name|metrics
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Optional
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
name|MetricRegistryInfo
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
name|MetricsCollector
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
name|MetricsExecutor
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
name|MetricsSource
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
name|impl
operator|.
name|JmxCacheBuster
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
name|DefaultMetricsSystem
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
name|DefaultMetricsSystemHelper
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
name|MetricsExecutorImpl
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
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
comment|/**  * This class acts as an adapter to export the MetricRegistry's in the global registry. Each  * MetricRegistry will be registered or unregistered from the metric2 system. The collection will  * be performed via the MetricsSourceAdapter and the MetricRegistry will collected like a  * BaseSource instance for a group of metrics  (like WAL, RPC, etc) with the MetricRegistryInfo's  * JMX context.  *  *<p>Developer note:  * Unlike the current metrics2 based approach, the new metrics approach  * (hbase-metrics-api and hbase-metrics modules) work by having different MetricRegistries that are  * initialized and used from the code that lives in their respective modules (hbase-server, etc).  * There is no need to define BaseSource classes and do a lot of indirection. The MetricRegistry'es  * will be in the global MetricRegistriesImpl, and this class will iterate over  * MetricRegistries.global() and register adapters to the metrics2 subsystem. These adapters then  * report the actual values by delegating to  * {@link HBaseMetrics2HadoopMetricsAdapter#snapshotAllMetrics(MetricRegistry, MetricsCollector)}.  *  * We do not initialize the Hadoop Metrics2 system assuming that other BaseSources already do so  * (see BaseSourceImpl). Once the last BaseSource is moved to the new system, the metric2  * initialization should be moved here.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|GlobalMetricRegistriesAdapter
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GlobalMetricRegistriesAdapter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
class|class
name|MetricsSourceAdapter
implements|implements
name|MetricsSource
block|{
specifier|private
specifier|final
name|MetricRegistry
name|registry
decl_stmt|;
name|MetricsSourceAdapter
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|)
block|{
name|this
operator|.
name|registry
operator|=
name|registry
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsCollector
name|collector
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
name|metricsAdapter
operator|.
name|snapshotAllMetrics
argument_list|(
name|registry
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|MetricsExecutor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|stopped
decl_stmt|;
specifier|private
specifier|final
name|DefaultMetricsSystemHelper
name|helper
decl_stmt|;
specifier|private
specifier|final
name|HBaseMetrics2HadoopMetricsAdapter
name|metricsAdapter
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|MetricRegistryInfo
argument_list|,
name|MetricsSourceAdapter
argument_list|>
name|registeredSources
decl_stmt|;
specifier|private
name|GlobalMetricRegistriesAdapter
parameter_list|()
block|{
name|this
operator|.
name|executor
operator|=
operator|new
name|MetricsExecutorImpl
argument_list|()
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|metricsAdapter
operator|=
operator|new
name|HBaseMetrics2HadoopMetricsAdapter
argument_list|()
expr_stmt|;
name|this
operator|.
name|registeredSources
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|helper
operator|=
operator|new
name|DefaultMetricsSystemHelper
argument_list|()
expr_stmt|;
name|executor
operator|.
name|getExecutor
argument_list|()
operator|.
name|scheduleAtFixedRate
argument_list|(
parameter_list|()
lambda|->
name|this
operator|.
name|doRun
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure that this global MetricSource for hbase-metrics module based metrics are initialized.    * This should be called only once.    */
specifier|public
specifier|static
name|GlobalMetricRegistriesAdapter
name|init
parameter_list|()
block|{
return|return
operator|new
name|GlobalMetricRegistriesAdapter
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|stopped
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doRun
parameter_list|()
block|{
if|if
condition|(
name|stopped
operator|.
name|get
argument_list|()
condition|)
block|{
name|executor
operator|.
name|stop
argument_list|()
expr_stmt|;
return|return;
block|}
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
literal|"doRun called: "
operator|+
name|registeredSources
argument_list|)
expr_stmt|;
block|}
name|Collection
argument_list|<
name|MetricRegistry
argument_list|>
name|registries
init|=
name|MetricRegistries
operator|.
name|global
argument_list|()
operator|.
name|getMetricRegistries
argument_list|()
decl_stmt|;
for|for
control|(
name|MetricRegistry
name|registry
range|:
name|registries
control|)
block|{
name|MetricRegistryInfo
name|info
init|=
name|registry
operator|.
name|getMetricRegistryInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|info
operator|.
name|isExistingSource
argument_list|()
condition|)
block|{
comment|// If there is an already existing BaseSource for this MetricRegistry, skip it here. These
comment|// types of registries are there only due to existing BaseSource implementations in the
comment|// source code (like MetricsRegionServer, etc). This is to make sure that we can transition
comment|// iteratively to the new hbase-metrics system. These type of MetricRegistry metrics will be
comment|// exported from the BaseSource.getMetrics() call directly because there is already a
comment|// MetricRecordBuilder there (see MetricsRegionServerSourceImpl).
continue|continue;
block|}
if|if
condition|(
operator|!
name|registeredSources
operator|.
name|containsKey
argument_list|(
name|info
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Registering adapter for the MetricRegistry: "
operator|+
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// register this as a MetricSource under different JMX Context'es.
name|MetricsSourceAdapter
name|adapter
init|=
operator|new
name|MetricsSourceAdapter
argument_list|(
name|registry
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Registering "
operator|+
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
operator|+
literal|" "
operator|+
name|info
operator|.
name|getMetricsDescription
argument_list|()
argument_list|)
expr_stmt|;
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
operator|.
name|register
argument_list|(
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|,
name|info
operator|.
name|getMetricsDescription
argument_list|()
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
name|registeredSources
operator|.
name|put
argument_list|(
name|info
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
comment|// next collection will collect the newly registered MetricSource. Doing this here leads to
comment|// ConcurrentModificationException.
block|}
block|}
name|boolean
name|removed
init|=
literal|false
decl_stmt|;
comment|// Remove registered sources if it is removed from the global registry
for|for
control|(
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|MetricRegistryInfo
argument_list|,
name|MetricsSourceAdapter
argument_list|>
argument_list|>
name|it
init|=
name|registeredSources
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Entry
argument_list|<
name|MetricRegistryInfo
argument_list|,
name|MetricsSourceAdapter
argument_list|>
name|entry
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|MetricRegistryInfo
name|info
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Optional
argument_list|<
name|MetricRegistry
argument_list|>
name|found
init|=
name|MetricRegistries
operator|.
name|global
argument_list|()
operator|.
name|get
argument_list|(
name|info
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|found
operator|.
name|isPresent
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing adapter for the MetricRegistry: "
operator|+
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
init|)
block|{
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
operator|.
name|unregisterSource
argument_list|(
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|helper
operator|.
name|removeSourceName
argument_list|(
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|helper
operator|.
name|removeObjectName
argument_list|(
name|info
operator|.
name|getMetricsJmxContext
argument_list|()
argument_list|)
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|removed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|removed
condition|)
block|{
name|JmxCacheBuster
operator|.
name|clearJmxCache
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

