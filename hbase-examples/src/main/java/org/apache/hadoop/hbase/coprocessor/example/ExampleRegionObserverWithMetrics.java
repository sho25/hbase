begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|List
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
name|ThreadLocalRandom
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
name|Cell
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
name|client
operator|.
name|Get
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|RegionCoprocessorEnvironment
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
name|RegionObserver
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
name|regionserver
operator|.
name|FlushLifeCycleTracker
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
name|Store
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
name|StoreFile
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
name|compactions
operator|.
name|CompactionLifeCycleTracker
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
name|compactions
operator|.
name|CompactionRequest
import|;
end_import

begin_comment
comment|/**  * An example coprocessor that collects some metrics to demonstrate the usage of exporting custom  * metrics from the coprocessor.  *<p>  * These metrics will be available through the regular Hadoop metrics2 sinks (ganglia, opentsdb,  * etc) as well as JMX output. You can view a snapshot of the metrics by going to the http web UI  * of the regionserver page, something like http://myregionserverhost:16030/jmx  *</p>  *  * @see ExampleMasterObserverWithMetrics  */
end_comment

begin_class
specifier|public
class|class
name|ExampleRegionObserverWithMetrics
implements|implements
name|RegionCoprocessor
block|{
specifier|private
name|Counter
name|preGetCounter
decl_stmt|;
specifier|private
name|Counter
name|flushCounter
decl_stmt|;
specifier|private
name|Counter
name|filesCompactedCounter
decl_stmt|;
specifier|private
name|Timer
name|costlyOperationTimer
decl_stmt|;
specifier|private
name|ExampleRegionObserver
name|observer
decl_stmt|;
class|class
name|ExampleRegionObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|Get
name|get
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Increment the Counter whenever the coprocessor is called
name|preGetCounter
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|Get
name|get
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do a costly (high latency) operation which we want to measure how long it takes by
comment|// using a Timer (which is a Meter and a Histogram).
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
name|performCostlyOperation
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|costlyOperationTimer
operator|.
name|updateNanos
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
name|flushCounter
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|StoreFile
name|resultFile
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
name|flushCounter
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompactSelection
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|StoreFile
argument_list|>
name|selected
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|selected
operator|!=
literal|null
condition|)
block|{
name|filesCompactedCounter
operator|.
name|increment
argument_list|(
name|selected
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|performCostlyOperation
parameter_list|()
block|{
try|try
block|{
comment|// simulate the operation by sleeping.
name|Thread
operator|.
name|sleep
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignore
parameter_list|)
block|{       }
block|}
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|observer
argument_list|)
return|;
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
comment|// start for the RegionServerObserver will be called only once in the lifetime of the
comment|// server. We will construct and register all metrics that we will track across method
comment|// invocations.
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
comment|// Obtain the MetricRegistry for the RegionServer. Metrics from this registry will be reported
comment|// at the region server level per-regionserver.
name|MetricRegistry
name|registry
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
operator|)
operator|.
name|getMetricRegistryForRegionServer
argument_list|()
decl_stmt|;
name|observer
operator|=
operator|new
name|ExampleRegionObserver
argument_list|()
expr_stmt|;
if|if
condition|(
name|preGetCounter
operator|==
literal|null
condition|)
block|{
comment|// Create a new Counter, or get the already registered counter.
comment|// It is much better to only call this once and save the Counter as a class field instead
comment|// of creating the counter every time a coprocessor method is invoked. This will negate
comment|// any performance bottleneck coming from map lookups tracking metrics in the registry.
comment|// Returned counter instance is shared by all coprocessors of the same class in the same
comment|// region server.
name|preGetCounter
operator|=
name|registry
operator|.
name|counter
argument_list|(
literal|"preGetRequests"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|costlyOperationTimer
operator|==
literal|null
condition|)
block|{
comment|// Create a Timer to track execution times for the costly operation.
name|costlyOperationTimer
operator|=
name|registry
operator|.
name|timer
argument_list|(
literal|"costlyOperation"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|flushCounter
operator|==
literal|null
condition|)
block|{
comment|// Track the number of flushes that have completed
name|flushCounter
operator|=
name|registry
operator|.
name|counter
argument_list|(
literal|"flushesCompleted"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filesCompactedCounter
operator|==
literal|null
condition|)
block|{
comment|// Track the number of files that were compacted (many files may be rewritten in a single
comment|// compaction).
name|filesCompactedCounter
operator|=
name|registry
operator|.
name|counter
argument_list|(
literal|"filesCompacted"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we should NOT remove / deregister the metrics in stop(). The whole registry will be
comment|// removed when the last region of the table is closed.
block|}
block|}
end_class

end_unit

