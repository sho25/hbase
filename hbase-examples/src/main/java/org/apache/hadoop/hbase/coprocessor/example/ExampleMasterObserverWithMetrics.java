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
name|coprocessor
operator|.
name|example
package|;
end_package

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
name|CoprocessorEnvironment
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
name|HRegionInfo
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
name|TableName
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
name|client
operator|.
name|TableDescriptor
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
name|coprocessor
operator|.
name|MasterObserver
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
name|Counter
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
name|Gauge
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

begin_comment
comment|/**  * An example coprocessor that collects some metrics to demonstrate the usage of exporting custom  * metrics from the coprocessor.  *  *<p>  * These metrics will be available through the regular Hadoop metrics2 sinks (ganglia, opentsdb,  * etc) as well as JMX output. You can view a snapshot of the metrics by going to the http web UI  * of the master page, something like http://mymasterhost:16010/jmx  *</p>  * @see ExampleRegionObserverWithMetrics  */
end_comment

begin_class
specifier|public
class|class
name|ExampleMasterObserverWithMetrics
implements|implements
name|MasterObserver
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
name|ExampleMasterObserverWithMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** This is the Timer metric object to keep track of the current count across invocations */
specifier|private
name|Timer
name|createTableTimer
decl_stmt|;
specifier|private
name|long
name|createTableStartTime
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
comment|/** This is a Counter object to keep track of disableTable operations */
specifier|private
name|Counter
name|disableTableCounter
decl_stmt|;
comment|/** Returns the total memory of the process. We will use this to define a gauge metric */
specifier|private
name|long
name|getTotalMemory
parameter_list|()
block|{
return|return
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|totalMemory
argument_list|()
return|;
block|}
comment|/** Returns the max memory of the process. We will use this to define a gauge metric */
specifier|private
name|long
name|getMaxMemory
parameter_list|()
block|{
return|return
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|maxMemory
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|TableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we rely on the fact that there is only 1 instance of our MasterObserver. We keep track of
comment|// when the operation starts before the operation is executing.
name|this
operator|.
name|createTableStartTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|TableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|createTableStartTime
operator|>
literal|0
condition|)
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|this
operator|.
name|createTableStartTime
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Create table took: "
operator|+
name|time
argument_list|)
expr_stmt|;
comment|// Update the timer metric for the create table operation duration.
name|createTableTimer
operator|.
name|updateMillis
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preDisableTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Increment the Counter for disable table operations
name|this
operator|.
name|disableTableCounter
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// start for the MasterObserver will be called only once in the lifetime of the
comment|// server. We will construct and register all metrics that we will track across method
comment|// invocations.
if|if
condition|(
name|env
operator|instanceof
name|MasterCoprocessorEnvironment
condition|)
block|{
comment|// Obtain the MetricRegistry for the Master. Metrics from this registry will be reported
comment|// at the master level per-server.
name|MetricRegistry
name|registry
init|=
operator|(
operator|(
name|MasterCoprocessorEnvironment
operator|)
name|env
operator|)
operator|.
name|getMetricRegistryForMaster
argument_list|()
decl_stmt|;
if|if
condition|(
name|createTableTimer
operator|==
literal|null
condition|)
block|{
comment|// Create a new Counter, or get the already registered counter.
comment|// It is much better to only call this once and save the Counter as a class field instead
comment|// of creating the counter every time a coprocessor method is invoked. This will negate
comment|// any performance bottleneck coming from map lookups tracking metrics in the registry.
name|createTableTimer
operator|=
name|registry
operator|.
name|timer
argument_list|(
literal|"CreateTable"
argument_list|)
expr_stmt|;
comment|// on stop(), we can remove these registered metrics via calling registry.remove(). But
comment|// it is not needed for coprocessors at the master level. If coprocessor is stopped,
comment|// the server is stopping anyway, so there will not be any resource leaks.
block|}
if|if
condition|(
name|disableTableCounter
operator|==
literal|null
condition|)
block|{
name|disableTableCounter
operator|=
name|registry
operator|.
name|counter
argument_list|(
literal|"DisableTable"
argument_list|)
expr_stmt|;
block|}
comment|// Register a custom gauge. The Gauge object will be registered in the metrics registry and
comment|// periodically the getValue() is invoked to obtain the snapshot.
name|registry
operator|.
name|register
argument_list|(
literal|"totalMemory"
argument_list|,
operator|new
name|Gauge
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|getValue
parameter_list|()
block|{
return|return
name|getTotalMemory
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Register a custom gauge using Java-8 lambdas (Supplier converted into Gauge)
name|registry
operator|.
name|register
argument_list|(
literal|"maxMemory"
argument_list|,
name|this
operator|::
name|getMaxMemory
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

