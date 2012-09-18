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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * This class extends MetricsTimeVaryingRate to let the metrics  * persist past a pushMetric() call  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PersistentMetricsTimeVaryingRate
extends|extends
name|MetricsTimeVaryingRate
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hbase.metrics"
argument_list|)
decl_stmt|;
specifier|protected
name|boolean
name|reset
init|=
literal|false
decl_stmt|;
specifier|protected
name|long
name|lastOper
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|totalOps
init|=
literal|0
decl_stmt|;
comment|/**    * Constructor - create a new metric    * @param nam the name of the metrics to be used to publish the metric    * @param registry - where the metrics object will be registered    * @param description metrics description    */
specifier|public
name|PersistentMetricsTimeVaryingRate
parameter_list|(
specifier|final
name|String
name|nam
parameter_list|,
specifier|final
name|MetricsRegistry
name|registry
parameter_list|,
specifier|final
name|String
name|description
parameter_list|)
block|{
name|super
argument_list|(
name|nam
argument_list|,
name|registry
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor - create a new metric    * @param nam the name of the metrics to be used to publish the metric    * @param registry - where the metrics object will be registered    */
specifier|public
name|PersistentMetricsTimeVaryingRate
parameter_list|(
specifier|final
name|String
name|nam
parameter_list|,
name|MetricsRegistry
name|registry
parameter_list|)
block|{
name|this
argument_list|(
name|nam
argument_list|,
name|registry
argument_list|,
name|NO_DESCRIPTION
argument_list|)
expr_stmt|;
block|}
comment|/**    * Push updated metrics to the mr.    *     * Note this does NOT push to JMX    * (JMX gets the info via {@link #getPreviousIntervalAverageTime()} and    * {@link #getPreviousIntervalNumOps()}    *    * @param mr owner of this metric    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|pushMetric
parameter_list|(
specifier|final
name|MetricsRecord
name|mr
parameter_list|)
block|{
comment|// this will reset the currentInterval& num_ops += prevInterval()
name|super
operator|.
name|pushMetric
argument_list|(
name|mr
argument_list|)
expr_stmt|;
comment|// since we're retaining prevInterval(), we don't want to do the incr
comment|// instead, we want to set that value because we have absolute ops
try|try
block|{
name|mr
operator|.
name|setMetric
argument_list|(
name|getName
argument_list|()
operator|+
literal|"_num_ops"
argument_list|,
name|totalOps
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
name|info
argument_list|(
literal|"pushMetric failed for "
operator|+
name|getName
argument_list|()
operator|+
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reset
condition|)
block|{
comment|// use the previous avg as our starting min/max/avg
name|super
operator|.
name|inc
argument_list|(
name|getPreviousIntervalAverageTime
argument_list|()
argument_list|)
expr_stmt|;
name|reset
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// maintain the stats that pushMetric() cleared
name|maintainStats
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Increment the metrics for numOps operations    * @param numOps - number of operations    * @param time - time for numOps operations    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|inc
parameter_list|(
specifier|final
name|int
name|numOps
parameter_list|,
specifier|final
name|long
name|time
parameter_list|)
block|{
name|super
operator|.
name|inc
argument_list|(
name|numOps
argument_list|,
name|time
argument_list|)
expr_stmt|;
name|totalOps
operator|+=
name|numOps
expr_stmt|;
block|}
comment|/**    * Increment the metrics for numOps operations    * @param time - time for numOps operations    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|inc
parameter_list|(
specifier|final
name|long
name|time
parameter_list|)
block|{
name|super
operator|.
name|inc
argument_list|(
name|time
argument_list|)
expr_stmt|;
operator|++
name|totalOps
expr_stmt|;
block|}
comment|/**    * Rollover to a new interval    * NOTE: does not reset numOps.  this is an absolute value    */
specifier|public
specifier|synchronized
name|void
name|resetMinMaxAvg
parameter_list|()
block|{
name|reset
operator|=
literal|true
expr_stmt|;
block|}
comment|/* MetricsTimeVaryingRate will reset every time pushMetric() is called    * This is annoying for long-running stats that might not get a single     * operation in the polling period.  This function ensures that values    * for those stat entries don't get reset.    */
specifier|protected
name|void
name|maintainStats
parameter_list|()
block|{
name|int
name|curOps
init|=
name|this
operator|.
name|getPreviousIntervalNumOps
argument_list|()
decl_stmt|;
if|if
condition|(
name|curOps
operator|>
literal|0
condition|)
block|{
name|long
name|curTime
init|=
name|this
operator|.
name|getPreviousIntervalAverageTime
argument_list|()
decl_stmt|;
name|long
name|totalTime
init|=
name|curTime
operator|*
name|curOps
decl_stmt|;
if|if
condition|(
name|curTime
operator|==
literal|0
operator|||
name|totalTime
operator|/
name|curTime
operator|==
name|curOps
condition|)
block|{
name|super
operator|.
name|inc
argument_list|(
name|curOps
argument_list|,
name|totalTime
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stats for "
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|" overflowed! resetting"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

