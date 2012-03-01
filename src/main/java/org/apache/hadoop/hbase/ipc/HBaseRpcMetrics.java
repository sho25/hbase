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
name|ipc
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
name|ipc
operator|.
name|VersionedProtocol
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
name|*
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

begin_comment
comment|/**  *  * This class is for maintaining  the various RPC statistics  * and publishing them through the metrics interfaces.  * This also registers the JMX MBean for RPC.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values;  * for example:  *<p> {@link #rpcQueueTime}.inc(time)  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseRpcMetrics
implements|implements
name|Updater
block|{
specifier|public
specifier|static
specifier|final
name|String
name|NAME_DELIM
init|=
literal|"$"
decl_stmt|;
specifier|private
specifier|final
name|MetricsRegistry
name|registry
init|=
operator|new
name|MetricsRegistry
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|MetricsRecord
name|metricsRecord
decl_stmt|;
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseRpcMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseRPCStatistics
name|rpcStatistics
decl_stmt|;
specifier|public
name|HBaseRpcMetrics
parameter_list|(
name|String
name|hostName
parameter_list|,
name|String
name|port
parameter_list|)
block|{
name|MetricsContext
name|context
init|=
name|MetricsUtil
operator|.
name|getContext
argument_list|(
literal|"rpc"
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
literal|"metrics"
argument_list|)
expr_stmt|;
name|metricsRecord
operator|.
name|setTag
argument_list|(
literal|"port"
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing RPC Metrics with hostName="
operator|+
name|hostName
operator|+
literal|", port="
operator|+
name|port
argument_list|)
expr_stmt|;
name|context
operator|.
name|registerUpdater
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|initMethods
argument_list|(
name|HMasterInterface
operator|.
name|class
argument_list|)
expr_stmt|;
name|initMethods
argument_list|(
name|HMasterRegionInterface
operator|.
name|class
argument_list|)
expr_stmt|;
name|initMethods
argument_list|(
name|HRegionInterface
operator|.
name|class
argument_list|)
expr_stmt|;
name|rpcStatistics
operator|=
operator|new
name|HBaseRPCStatistics
argument_list|(
name|this
operator|.
name|registry
argument_list|,
name|hostName
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
comment|/**    * The metrics variables are public:    *  - they can be set directly by calling their set/inc methods    *  -they can also be read directly - e.g. JMX does this.    */
specifier|public
specifier|final
name|MetricsTimeVaryingLong
name|receivedBytes
init|=
operator|new
name|MetricsTimeVaryingLong
argument_list|(
literal|"ReceivedBytes"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingLong
name|sentBytes
init|=
operator|new
name|MetricsTimeVaryingLong
argument_list|(
literal|"SentBytes"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingRate
name|rpcQueueTime
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"RpcQueueTime"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
name|MetricsTimeVaryingRate
name|rpcProcessingTime
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"RpcProcessingTime"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsIntValue
name|numOpenConnections
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"NumOpenConnections"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsIntValue
name|callQueueLen
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"callQueueLen"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsIntValue
name|priorityCallQueueLen
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"priorityCallQueueLen"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingInt
name|authenticationFailures
init|=
operator|new
name|MetricsTimeVaryingInt
argument_list|(
literal|"rpcAuthenticationFailures"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingInt
name|authenticationSuccesses
init|=
operator|new
name|MetricsTimeVaryingInt
argument_list|(
literal|"rpcAuthenticationSuccesses"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingInt
name|authorizationFailures
init|=
operator|new
name|MetricsTimeVaryingInt
argument_list|(
literal|"rpcAuthorizationFailures"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricsTimeVaryingInt
name|authorizationSuccesses
init|=
operator|new
name|MetricsTimeVaryingInt
argument_list|(
literal|"rpcAuthorizationSuccesses"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|public
name|MetricsTimeVaryingRate
name|rpcSlowResponseTime
init|=
operator|new
name|MetricsTimeVaryingRate
argument_list|(
literal|"RpcSlowResponse"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
specifier|private
name|void
name|initMethods
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|)
block|{
for|for
control|(
name|Method
name|m
range|:
name|protocol
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|get
argument_list|(
name|m
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
name|create
argument_list|(
name|m
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|MetricsTimeVaryingRate
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
operator|(
name|MetricsTimeVaryingRate
operator|)
name|registry
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|private
name|MetricsTimeVaryingRate
name|create
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
operator|new
name|MetricsTimeVaryingRate
argument_list|(
name|key
argument_list|,
name|this
operator|.
name|registry
argument_list|)
return|;
block|}
specifier|public
name|void
name|inc
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|amt
parameter_list|)
block|{
name|MetricsTimeVaryingRate
name|m
init|=
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got inc() request for method that doesnt exist: "
operator|+
name|name
argument_list|)
expr_stmt|;
return|return;
comment|// ignore methods that dont exist.
block|}
name|m
operator|.
name|inc
argument_list|(
name|amt
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generate metrics entries for all the methods defined in the list of    * interfaces.  A {@link MetricsTimeVaryingRate} counter will be created for    * each {@code Class.getMethods().getName()} entry.    * @param ifaces Define metrics for all methods in the given classes    */
specifier|public
name|void
name|createMetrics
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|ifaces
parameter_list|)
block|{
name|createMetrics
argument_list|(
name|ifaces
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generate metrics entries for all the methods defined in the list of    * interfaces.  A {@link MetricsTimeVaryingRate} counter will be created for    * each {@code Class.getMethods().getName()} entry.    *    *<p>    * If {@code prefixWithClass} is {@code true}, each metric will be named as    * {@code [Class.getSimpleName()].[Method.getName()]}.  Otherwise each metric    * will just be named according to the method -- {@code Method.getName()}.    *</p>    * @param ifaces Define metrics for all methods in the given classes    * @param prefixWithClass If {@code true}, each metric will be named as    *     "classname.method"    */
specifier|public
name|void
name|createMetrics
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|ifaces
parameter_list|,
name|boolean
name|prefixWithClass
parameter_list|)
block|{
name|createMetrics
argument_list|(
name|ifaces
argument_list|,
name|prefixWithClass
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**   * Generate metrics entries for all the methods defined in the list of   * interfaces. A {@link MetricsTimeVaryingRate} counter will be created for   * each {@code Class.getMethods().getName()} entry.   *   *<p>   * If {@code prefixWithClass} is {@code true}, each metric will be named as   * {@code [Class.getSimpleName()].[Method.getName()]}. Otherwise each metric   * will just be named according to the method -- {@code Method.getName()}.   *</p>   *   *<p>   * Additionally, if {@code suffixes} is defined, additional metrics will be   * created for each method named as the original metric concatenated with   * the suffix.   *</p>   * @param ifaces Define metrics for all methods in the given classes   * @param prefixWithClass If {@code true}, each metric will be named as   * "classname.method"   * @param suffixes If not null, each method will get additional metrics ending   * in each of the suffixes.   */
specifier|public
name|void
name|createMetrics
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|ifaces
parameter_list|,
name|boolean
name|prefixWithClass
parameter_list|,
name|String
index|[]
name|suffixes
parameter_list|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|iface
range|:
name|ifaces
control|)
block|{
name|Method
index|[]
name|methods
init|=
name|iface
operator|.
name|getMethods
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|methods
control|)
block|{
name|String
name|attrName
init|=
name|prefixWithClass
condition|?
name|getMetricName
argument_list|(
name|iface
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|)
else|:
name|method
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|get
argument_list|(
name|attrName
argument_list|)
operator|==
literal|null
condition|)
name|create
argument_list|(
name|attrName
argument_list|)
expr_stmt|;
if|if
condition|(
name|suffixes
operator|!=
literal|null
condition|)
block|{
comment|// create metrics for each requested suffix
for|for
control|(
name|String
name|s
range|:
name|suffixes
control|)
block|{
name|String
name|metricName
init|=
name|attrName
operator|+
name|s
decl_stmt|;
if|if
condition|(
name|get
argument_list|(
name|metricName
argument_list|)
operator|==
literal|null
condition|)
name|create
argument_list|(
name|metricName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|String
name|getMetricName
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|,
name|String
name|method
parameter_list|)
block|{
return|return
name|c
operator|.
name|getSimpleName
argument_list|()
operator|+
name|NAME_DELIM
operator|+
name|method
return|;
block|}
comment|/**    * Push the metrics to the monitoring subsystem on doUpdate() call.    */
specifier|public
name|void
name|doUpdates
parameter_list|(
specifier|final
name|MetricsContext
name|context
parameter_list|)
block|{
comment|// Both getMetricsList() and pushMetric() are thread-safe
for|for
control|(
name|MetricsBase
name|m
range|:
name|registry
operator|.
name|getMetricsList
argument_list|()
control|)
block|{
name|m
operator|.
name|pushMetric
argument_list|(
name|metricsRecord
argument_list|)
expr_stmt|;
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
name|rpcStatistics
operator|!=
literal|null
condition|)
name|rpcStatistics
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

