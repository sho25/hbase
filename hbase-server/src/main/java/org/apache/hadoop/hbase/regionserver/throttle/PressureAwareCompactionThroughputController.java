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
name|throttle
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|HBaseInterfaceAudience
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
name|ScheduledChore
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
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|RegionServerServices
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
name|OffPeakHours
import|;
end_import

begin_comment
comment|/**  * A throughput controller which uses the follow schema to limit throughput  *<ul>  *<li>If compaction pressure is greater than 1.0, no limitation.</li>  *<li>In off peak hours, use a fixed throughput limitation  * {@value #HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_OFFPEAK}</li>  *<li>In normal hours, the max throughput is tuned between  * {@value #HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_LOWER_BOUND} and  * {@value #HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_HIGHER_BOUND}, using the formula&quot;lower +  * (higer - lower) * compactionPressure&quot;, where compactionPressure is in range [0.0, 1.0]</li>  *</ul>  * @see org.apache.hadoop.hbase.regionserver.HStore#getCompactionPressure()  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|PressureAwareCompactionThroughputController
extends|extends
name|PressureAwareThroughputController
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PressureAwareCompactionThroughputController
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_HIGHER_BOUND
init|=
literal|"hbase.hstore.compaction.throughput.higher.bound"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_HIGHER_BOUND
init|=
literal|20L
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_LOWER_BOUND
init|=
literal|"hbase.hstore.compaction.throughput.lower.bound"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_LOWER_BOUND
init|=
literal|10L
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_OFFPEAK
init|=
literal|"hbase.hstore.compaction.throughput.offpeak"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_OFFPEAK
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_THROUGHPUT_TUNE_PERIOD
init|=
literal|"hbase.hstore.compaction.throughput.tune.period"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_HSTORE_COMPACTION_THROUGHPUT_TUNE_PERIOD
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
comment|// check compaction throughput every this size
specifier|private
specifier|static
specifier|final
name|String
name|HBASE_HSTORE_COMPACTION_THROUGHPUT_CONTROL_CHECK_INTERVAL
init|=
literal|"hbase.hstore.compaction.throughput.control.check.interval"
decl_stmt|;
specifier|private
name|long
name|maxThroughputOffpeak
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|(
specifier|final
name|RegionServerServices
name|server
parameter_list|)
block|{
name|server
operator|.
name|getChoreService
argument_list|()
operator|.
name|scheduleChore
argument_list|(
operator|new
name|ScheduledChore
argument_list|(
literal|"CompactionThroughputTuner"
argument_list|,
name|this
argument_list|,
name|tuningPeriod
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
name|tune
argument_list|(
name|server
operator|.
name|getCompactionPressure
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tune
parameter_list|(
name|double
name|compactionPressure
parameter_list|)
block|{
name|double
name|maxThroughputToSet
decl_stmt|;
if|if
condition|(
name|compactionPressure
operator|>
literal|1.0
condition|)
block|{
comment|// set to unlimited if some stores already reach the blocking store file count
name|maxThroughputToSet
operator|=
name|Double
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|offPeakHours
operator|.
name|isOffPeakHour
argument_list|()
condition|)
block|{
name|maxThroughputToSet
operator|=
name|maxThroughputOffpeak
expr_stmt|;
block|}
else|else
block|{
comment|// compactionPressure is between 0.0 and 1.0, we use a simple linear formula to
comment|// calculate the throughput limitation.
name|maxThroughputToSet
operator|=
name|maxThroughputLowerBound
operator|+
operator|(
name|maxThroughputUpperBound
operator|-
name|maxThroughputLowerBound
operator|)
operator|*
name|compactionPressure
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|Math
operator|.
name|abs
argument_list|(
name|maxThroughputToSet
operator|-
name|getMaxThroughput
argument_list|()
argument_list|)
operator|<
literal|.0000001
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"CompactionPressure is "
operator|+
name|compactionPressure
operator|+
literal|", tune throughput to "
operator|+
name|throughputDesc
argument_list|(
name|maxThroughputToSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
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
literal|"CompactionPressure is "
operator|+
name|compactionPressure
operator|+
literal|", keep throughput throttling to "
operator|+
name|throughputDesc
argument_list|(
name|maxThroughputToSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|setMaxThroughput
argument_list|(
name|maxThroughputToSet
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|maxThroughputUpperBound
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_HIGHER_BOUND
argument_list|,
name|DEFAULT_HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_HIGHER_BOUND
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxThroughputLowerBound
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_LOWER_BOUND
argument_list|,
name|DEFAULT_HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_LOWER_BOUND
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxThroughputOffpeak
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_OFFPEAK
argument_list|,
name|DEFAULT_HBASE_HSTORE_COMPACTION_MAX_THROUGHPUT_OFFPEAK
argument_list|)
expr_stmt|;
name|this
operator|.
name|offPeakHours
operator|=
name|OffPeakHours
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|controlPerSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HBASE_HSTORE_COMPACTION_THROUGHPUT_CONTROL_CHECK_INTERVAL
argument_list|,
name|this
operator|.
name|maxThroughputLowerBound
argument_list|)
expr_stmt|;
name|this
operator|.
name|setMaxThroughput
argument_list|(
name|this
operator|.
name|maxThroughputLowerBound
argument_list|)
expr_stmt|;
name|this
operator|.
name|tuningPeriod
operator|=
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
name|HBASE_HSTORE_COMPACTION_THROUGHPUT_TUNE_PERIOD
argument_list|,
name|DEFAULT_HSTORE_COMPACTION_THROUGHPUT_TUNE_PERIOD
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Compaction throughput configurations, higher bound: "
operator|+
name|throughputDesc
argument_list|(
name|maxThroughputUpperBound
argument_list|)
operator|+
literal|", lower bound "
operator|+
name|throughputDesc
argument_list|(
name|maxThroughputLowerBound
argument_list|)
operator|+
literal|", off peak: "
operator|+
name|throughputDesc
argument_list|(
name|maxThroughputOffpeak
argument_list|)
operator|+
literal|", tuning period: "
operator|+
name|tuningPeriod
operator|+
literal|" ms"
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
literal|"DefaultCompactionThroughputController [maxThroughput="
operator|+
name|throughputDesc
argument_list|(
name|getMaxThroughput
argument_list|()
argument_list|)
operator|+
literal|", activeCompactions="
operator|+
name|activeOperations
operator|.
name|size
argument_list|()
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|skipControl
parameter_list|(
name|long
name|deltaSize
parameter_list|,
name|long
name|controlSize
parameter_list|)
block|{
if|if
condition|(
name|deltaSize
operator|<
name|controlSize
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

