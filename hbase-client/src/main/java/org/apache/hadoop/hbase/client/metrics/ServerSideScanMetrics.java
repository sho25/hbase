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
name|HashMap
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_comment
comment|/**  * Provides server side metrics related to scan operations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ServerSideScanMetrics
block|{
comment|/**    * Hash to hold the String -&gt; Atomic Long mappings for each metric    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|counters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Create a new counter with the specified name    * @param counterName    * @return {@link AtomicLong} instance for the counter with counterName    */
specifier|protected
name|AtomicLong
name|createCounter
parameter_list|(
name|String
name|counterName
parameter_list|)
block|{
name|AtomicLong
name|c
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|counters
operator|.
name|put
argument_list|(
name|counterName
argument_list|,
name|c
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
specifier|public
specifier|static
specifier|final
name|String
name|COUNT_OF_ROWS_SCANNED_KEY_METRIC_NAME
init|=
literal|"ROWS_SCANNED"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COUNT_OF_ROWS_FILTERED_KEY_METRIC_NAME
init|=
literal|"ROWS_FILTERED"
decl_stmt|;
comment|/**    * number of rows filtered during scan RPC    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRowsFiltered
init|=
name|createCounter
argument_list|(
name|COUNT_OF_ROWS_FILTERED_KEY_METRIC_NAME
argument_list|)
decl_stmt|;
comment|/**    * number of rows scanned during scan RPC. Not every row scanned will be returned to the client    * since rows may be filtered.    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRowsScanned
init|=
name|createCounter
argument_list|(
name|COUNT_OF_ROWS_SCANNED_KEY_METRIC_NAME
argument_list|)
decl_stmt|;
comment|/**    * @param counterName    * @param value    */
specifier|public
name|void
name|setCounter
parameter_list|(
name|String
name|counterName
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|AtomicLong
name|c
init|=
name|this
operator|.
name|counters
operator|.
name|get
argument_list|(
name|counterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
name|c
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param counterName    * @return true if a counter exists with the counterName    */
specifier|public
name|boolean
name|hasCounter
parameter_list|(
name|String
name|counterName
parameter_list|)
block|{
return|return
name|this
operator|.
name|counters
operator|.
name|containsKey
argument_list|(
name|counterName
argument_list|)
return|;
block|}
comment|/**    * @param counterName    * @return {@link AtomicLong} instance for this counter name, null if counter does not exist.    */
specifier|public
name|AtomicLong
name|getCounter
parameter_list|(
name|String
name|counterName
parameter_list|)
block|{
return|return
name|this
operator|.
name|counters
operator|.
name|get
argument_list|(
name|counterName
argument_list|)
return|;
block|}
comment|/**    * @param counterName    * @param delta    */
specifier|public
name|void
name|addToCounter
parameter_list|(
name|String
name|counterName
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|AtomicLong
name|c
init|=
name|this
operator|.
name|counters
operator|.
name|get
argument_list|(
name|counterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
name|c
operator|.
name|addAndGet
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get all of the values since the last time this function was called. Calling this function will    * reset all AtomicLongs in the instance back to 0.    * @return A Map of String -&gt; Long for metrics    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getMetricsMap
parameter_list|()
block|{
return|return
name|getMetricsMap
argument_list|(
literal|true
argument_list|)
return|;
block|}
comment|/**    * Get all of the values. If reset is true, we will reset the all AtomicLongs back to 0.    * @param reset whether to reset the AtomicLongs to 0.    * @return A Map of String -&gt; Long for metrics    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getMetricsMap
parameter_list|(
name|boolean
name|reset
parameter_list|)
block|{
comment|// Create a builder
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|e
range|:
name|this
operator|.
name|counters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|long
name|value
init|=
name|reset
condition|?
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
else|:
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|// Build the immutable map so that people can't mess around with it.
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

