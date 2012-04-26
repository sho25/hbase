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
operator|.
name|metrics
package|;
end_package

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
name|atomic
operator|.
name|AtomicInteger
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
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * This class if for maintaining the maps used to power metrics for hfiles,  * regions, and regionservers. It has methods to mutate and get state of metrics  * numbers. These numbers are exposed to Hadoop metrics through  * RegionServerDynamicMetrics.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionMetricsStorage
block|{
comment|// for simple numeric metrics (# of blocks read from block cache)
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|numericMetrics
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
argument_list|()
decl_stmt|;
comment|// for simple numeric metrics (current block cache size)
comment|// These ones are not reset to zero when queried, unlike the previous.
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|numericPersistentMetrics
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Used for metrics where we want track a metrics (such as latency) over a    * number of operations.    */
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
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
name|timeVaryingMetrics
init|=
operator|new
name|ConcurrentHashMap
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
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|getNumericMetrics
parameter_list|()
block|{
return|return
name|numericMetrics
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|getNumericPersistentMetrics
parameter_list|()
block|{
return|return
name|numericPersistentMetrics
return|;
block|}
specifier|public
specifier|static
name|Map
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
name|getTimeVaryingMetrics
parameter_list|()
block|{
return|return
name|timeVaryingMetrics
return|;
block|}
specifier|public
specifier|static
name|void
name|incrNumericMetric
parameter_list|(
name|String
name|key
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
name|AtomicLong
name|oldVal
init|=
name|numericMetrics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
name|oldVal
operator|=
name|numericMetrics
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
operator|new
name|AtomicLong
argument_list|(
name|amount
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
return|return;
block|}
name|oldVal
operator|.
name|addAndGet
argument_list|(
name|amount
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|incrTimeVaryingMetric
parameter_list|(
name|String
name|key
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
name|Pair
argument_list|<
name|AtomicLong
argument_list|,
name|AtomicInteger
argument_list|>
name|oldVal
init|=
name|timeVaryingMetrics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
name|oldVal
operator|=
name|timeVaryingMetrics
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
operator|new
name|Pair
argument_list|<
name|AtomicLong
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|(
operator|new
name|AtomicLong
argument_list|(
name|amount
argument_list|)
argument_list|,
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
return|return;
block|}
name|oldVal
operator|.
name|getFirst
argument_list|()
operator|.
name|addAndGet
argument_list|(
name|amount
argument_list|)
expr_stmt|;
comment|// total time
name|oldVal
operator|.
name|getSecond
argument_list|()
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
comment|// increment ops by 1
block|}
specifier|public
specifier|static
name|void
name|incrNumericPersistentMetric
parameter_list|(
name|String
name|key
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
name|AtomicLong
name|oldVal
init|=
name|numericPersistentMetrics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
name|oldVal
operator|=
name|numericPersistentMetrics
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
operator|new
name|AtomicLong
argument_list|(
name|amount
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
return|return;
block|}
name|oldVal
operator|.
name|addAndGet
argument_list|(
name|amount
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setNumericMetric
parameter_list|(
name|String
name|key
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
name|numericMetrics
operator|.
name|put
argument_list|(
name|key
argument_list|,
operator|new
name|AtomicLong
argument_list|(
name|amount
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|long
name|getNumericMetric
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|AtomicLong
name|m
init|=
name|numericMetrics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
condition|)
return|return
literal|0
return|;
return|return
name|m
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Pair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|getTimeVaryingMetric
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|Pair
argument_list|<
name|AtomicLong
argument_list|,
name|AtomicInteger
argument_list|>
name|pair
init|=
name|timeVaryingMetrics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|pair
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|(
literal|0L
argument_list|,
literal|0
argument_list|)
return|;
block|}
return|return
operator|new
name|Pair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|getNumericPersistentMetric
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|AtomicLong
name|m
init|=
name|numericPersistentMetrics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
condition|)
return|return
literal|0
return|;
return|return
name|m
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Clear all copies of the metrics this stores.    */
specifier|public
specifier|static
name|void
name|clear
parameter_list|()
block|{
name|timeVaryingMetrics
operator|.
name|clear
argument_list|()
expr_stmt|;
name|numericMetrics
operator|.
name|clear
argument_list|()
expr_stmt|;
name|numericPersistentMetrics
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

