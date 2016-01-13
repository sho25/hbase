begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import static
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
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
import|;
end_import

begin_import
import|import static
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
name|HeapMemoryManager
operator|.
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
import|;
end_import

begin_import
import|import static
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
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MAX_RANGE_KEY
import|;
end_import

begin_import
import|import static
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
name|HeapMemoryManager
operator|.
name|MEMSTORE_SIZE_MIN_RANGE_KEY
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
name|HConstants
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
name|io
operator|.
name|util
operator|.
name|HeapMemorySizeUtil
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
name|HeapMemoryManager
operator|.
name|TunerContext
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
name|HeapMemoryManager
operator|.
name|TunerResult
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
name|RollingStatCalculator
import|;
end_import

begin_comment
comment|/**  * The default implementation for the HeapMemoryTuner. This will do statistical checks on  * number of evictions, cache misses and flushes to decide whether there should be changes  * in the heap size of memstore/block cache. During each tuner operation tuner takes a step  * which can either be INCREASE_BLOCK_CACHE_SIZE (increase block cache size),  * INCREASE_MEMSTORE_SIZE (increase memstore size) and by default it is NEUTRAL (no change).  * We say block cache is sufficient when there is no block cache eviction at all or major amount of  * memory allocated to block cache is empty, similarly we say memory allocated for memstore is  * sufficient when there is no memstore flushes because of heap pressure or major amount of  * memory allocated to memstore is empty. If both are sufficient we do nothing, if exactly one of  * them is found to be sufficient we decrease its size by<i>step</i> and increase the other by  * same amount. If none of them is sufficient we do statistical analysis on number of cache misses  * and flushes to determine tuner direction. Based on these statistics we decide the tuner  * direction. If we are not confident about which step direction to take we do nothing and wait for  * next iteration. On expectation we will be tuning for at least 10% tuner calls. The number of  * past periods to consider for statistics calculation can be specified in config by  *<i>hbase.regionserver.heapmemory.autotuner.lookup.periods</i>. Also these many initial calls to  * tuner will be ignored (cache is warming up and we leave the system to reach steady state).  * After the tuner takes a step, in next call we insure that last call was indeed helpful and did  * not do us any harm. If not then we revert the previous step. The step size is dynamic and it  * changes based on current and past few tuning directions and their step sizes. We maintain a  * parameter<i>decayingAvgTunerStepSize</i> which is sum of past tuner steps with  * sign(positive for increase in memstore and negative for increase in block cache). But rather  * than simple sum it is calculated by giving more priority to the recent tuning steps.  * When last few tuner steps were NETURAL then we assume we are restarting the tuning process and  * step size is updated to maximum allowed size which can be specified  in config by  *<i>hbase.regionserver.heapmemory.autotuner.step.max</i>. If in a particular tuning operation  * the step direction is opposite to what indicated by<i>decayingTunerStepSizeSum</i>  * we decrease the step size by half. Step size does not change in other tuning operations.  * When step size gets below a certain threshold then the following tuner operations are  * considered to be neutral. The minimum step size can be specified  in config by  *<i>hbase.regionserver.heapmemory.autotuner.step.min</i>.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|DefaultHeapMemoryTuner
implements|implements
name|HeapMemoryTuner
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MAX_STEP_KEY
init|=
literal|"hbase.regionserver.heapmemory.autotuner.step.max"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIN_STEP_KEY
init|=
literal|"hbase.regionserver.heapmemory.autotuner.step.min"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SUFFICIENT_MEMORY_LEVEL_KEY
init|=
literal|"hbase.regionserver.heapmemory.autotuner.sufficient.memory.level"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LOOKUP_PERIODS_KEY
init|=
literal|"hbase.regionserver.heapmemory.autotuner.lookup.periods"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NUM_PERIODS_TO_IGNORE
init|=
literal|"hbase.regionserver.heapmemory.autotuner.ignored.periods"
decl_stmt|;
comment|// Maximum step size that the tuner can take
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_MAX_STEP_VALUE
init|=
literal|0.04f
decl_stmt|;
comment|// 4%
comment|// Minimum step size that the tuner can take
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_MIN_STEP_VALUE
init|=
literal|0.00125f
decl_stmt|;
comment|// 0.125%
comment|// If current block cache size or memstore size in use is below this level relative to memory
comment|// provided to it then corresponding component will be considered to have sufficient memory
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_SUFFICIENT_MEMORY_LEVEL_VALUE
init|=
literal|0.5f
decl_stmt|;
comment|// 50%
comment|// Number of tuner periods that will be considered while calculating mean and deviation
comment|// If set to zero, all stats will be calculated from the start
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_LOOKUP_PERIODS
init|=
literal|60
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_PERIODS_IGNORED
init|=
literal|60
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TunerResult
name|NO_OP_TUNER_RESULT
init|=
operator|new
name|TunerResult
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// If deviation of tuner step size gets below this value then it means past few periods were
comment|// NEUTRAL(given that last tuner period was also NEUTRAL).
specifier|private
specifier|static
specifier|final
name|double
name|TUNER_STEP_EPS
init|=
literal|1e-6
decl_stmt|;
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DefaultHeapMemoryTuner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TunerResult
name|TUNER_RESULT
init|=
operator|new
name|TunerResult
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|float
name|sufficientMemoryLevel
init|=
name|DEFAULT_SUFFICIENT_MEMORY_LEVEL_VALUE
decl_stmt|;
specifier|private
name|float
name|maximumStepSize
init|=
name|DEFAULT_MAX_STEP_VALUE
decl_stmt|;
specifier|private
name|float
name|minimumStepSize
init|=
name|DEFAULT_MIN_STEP_VALUE
decl_stmt|;
specifier|private
name|int
name|tunerLookupPeriods
init|=
name|DEFAULT_LOOKUP_PERIODS
decl_stmt|;
specifier|private
name|int
name|numPeriodsToIgnore
init|=
name|DEFAULT_NUM_PERIODS_IGNORED
decl_stmt|;
comment|// Counter to ignore few initial periods while cache is still warming up
comment|// Memory tuner will do no operation for the first "tunerLookupPeriods"
specifier|private
name|int
name|ignoreInitialPeriods
init|=
literal|0
decl_stmt|;
specifier|private
name|float
name|globalMemStorePercentMinRange
decl_stmt|;
specifier|private
name|float
name|globalMemStorePercentMaxRange
decl_stmt|;
specifier|private
name|float
name|blockCachePercentMinRange
decl_stmt|;
specifier|private
name|float
name|blockCachePercentMaxRange
decl_stmt|;
comment|// Store statistics about the corresponding parameters for memory tuning
specifier|private
name|RollingStatCalculator
name|rollingStatsForCacheMisses
decl_stmt|;
specifier|private
name|RollingStatCalculator
name|rollingStatsForFlushes
decl_stmt|;
specifier|private
name|RollingStatCalculator
name|rollingStatsForEvictions
decl_stmt|;
specifier|private
name|RollingStatCalculator
name|rollingStatsForTunerSteps
decl_stmt|;
comment|// Set step size to max value for tuning, this step size will adjust dynamically while tuning
specifier|private
name|float
name|step
init|=
name|DEFAULT_MAX_STEP_VALUE
decl_stmt|;
specifier|private
name|StepDirection
name|prevTuneDirection
init|=
name|StepDirection
operator|.
name|NEUTRAL
decl_stmt|;
comment|//positive means memstore's size was increased
comment|//It is not just arithmetic sum of past tuner periods. More priority is given to recent
comment|//tuning steps.
specifier|private
name|double
name|decayingTunerStepSizeSum
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|TunerResult
name|tune
parameter_list|(
name|TunerContext
name|context
parameter_list|)
block|{
name|float
name|curMemstoreSize
init|=
name|context
operator|.
name|getCurMemStoreSize
argument_list|()
decl_stmt|;
name|float
name|curBlockCacheSize
init|=
name|context
operator|.
name|getCurBlockCacheSize
argument_list|()
decl_stmt|;
name|addToRollingStats
argument_list|(
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|ignoreInitialPeriods
operator|<
name|numPeriodsToIgnore
condition|)
block|{
comment|// Ignoring the first few tuner periods
name|ignoreInitialPeriods
operator|++
expr_stmt|;
name|rollingStatsForTunerSteps
operator|.
name|insertDataValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|NO_OP_TUNER_RESULT
return|;
block|}
name|StepDirection
name|newTuneDirection
init|=
name|getTuneDirection
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|float
name|newMemstoreSize
decl_stmt|;
name|float
name|newBlockCacheSize
decl_stmt|;
comment|// Adjusting step size for tuning to get to steady state or restart from steady state.
comment|// Even if the step size was 4% and 32 GB memory size, we will be shifting 1 GB back and forth
comment|// per tuner operation and it can affect the performance of cluster so we keep on decreasing
comment|// step size until everything settles.
if|if
condition|(
name|prevTuneDirection
operator|==
name|StepDirection
operator|.
name|NEUTRAL
operator|&&
name|newTuneDirection
operator|!=
name|StepDirection
operator|.
name|NEUTRAL
operator|&&
name|rollingStatsForTunerSteps
operator|.
name|getDeviation
argument_list|()
operator|<
name|TUNER_STEP_EPS
condition|)
block|{
comment|// Restarting the tuning from steady state and setting step size to maximum.
comment|// The deviation cannot be that low if last period was neutral and some recent periods were
comment|// not neutral.
name|step
operator|=
name|maximumStepSize
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|newTuneDirection
operator|==
name|StepDirection
operator|.
name|INCREASE_MEMSTORE_SIZE
operator|&&
name|decayingTunerStepSizeSum
operator|<
literal|0
operator|)
operator|||
operator|(
name|newTuneDirection
operator|==
name|StepDirection
operator|.
name|INCREASE_BLOCK_CACHE_SIZE
operator|&&
name|decayingTunerStepSizeSum
operator|>
literal|0
operator|)
condition|)
block|{
comment|// Current step is opposite of past tuner actions so decrease the step size to reach steady
comment|// state.
name|step
operator|=
name|step
operator|/
literal|2.00f
expr_stmt|;
block|}
if|if
condition|(
name|step
operator|<
name|minimumStepSize
condition|)
block|{
comment|// If step size is too small then we do nothing.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Tuner step size is too low; we will not perform any tuning this time."
argument_list|)
expr_stmt|;
name|step
operator|=
literal|0.0f
expr_stmt|;
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|NEUTRAL
expr_stmt|;
block|}
comment|// Increase / decrease the memstore / block cahce sizes depending on new tuner step.
name|float
name|globalMemstoreLowerMark
init|=
name|HeapMemorySizeUtil
operator|.
name|getGlobalMemStoreLowerMark
argument_list|(
name|conf
argument_list|,
name|curMemstoreSize
argument_list|)
decl_stmt|;
comment|// We don't want to exert immediate pressure on memstore. So, we decrease its size gracefully;
comment|// we set a minimum bar in the middle of the total memstore size and the lower limit.
name|float
name|minMemstoreSize
init|=
operator|(
operator|(
name|globalMemstoreLowerMark
operator|+
literal|1
operator|)
operator|*
name|curMemstoreSize
operator|)
operator|/
literal|2.00f
decl_stmt|;
switch|switch
condition|(
name|newTuneDirection
condition|)
block|{
case|case
name|INCREASE_BLOCK_CACHE_SIZE
case|:
if|if
condition|(
name|curMemstoreSize
operator|-
name|step
operator|<
name|minMemstoreSize
condition|)
block|{
name|step
operator|=
name|curMemstoreSize
operator|-
name|minMemstoreSize
expr_stmt|;
block|}
name|newMemstoreSize
operator|=
name|curMemstoreSize
operator|-
name|step
expr_stmt|;
name|newBlockCacheSize
operator|=
name|curBlockCacheSize
operator|+
name|step
expr_stmt|;
name|rollingStatsForTunerSteps
operator|.
name|insertDataValue
argument_list|(
operator|-
call|(
name|int
call|)
argument_list|(
name|step
operator|*
literal|100000
argument_list|)
argument_list|)
expr_stmt|;
name|decayingTunerStepSizeSum
operator|=
operator|(
name|decayingTunerStepSizeSum
operator|-
name|step
operator|)
operator|/
literal|2.00f
expr_stmt|;
break|break;
case|case
name|INCREASE_MEMSTORE_SIZE
case|:
name|newBlockCacheSize
operator|=
name|curBlockCacheSize
operator|-
name|step
expr_stmt|;
name|newMemstoreSize
operator|=
name|curMemstoreSize
operator|+
name|step
expr_stmt|;
name|rollingStatsForTunerSteps
operator|.
name|insertDataValue
argument_list|(
call|(
name|int
call|)
argument_list|(
name|step
operator|*
literal|100000
argument_list|)
argument_list|)
expr_stmt|;
name|decayingTunerStepSizeSum
operator|=
operator|(
name|decayingTunerStepSizeSum
operator|+
name|step
operator|)
operator|/
literal|2.00f
expr_stmt|;
break|break;
default|default:
name|prevTuneDirection
operator|=
name|StepDirection
operator|.
name|NEUTRAL
expr_stmt|;
name|rollingStatsForTunerSteps
operator|.
name|insertDataValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|decayingTunerStepSizeSum
operator|=
operator|(
name|decayingTunerStepSizeSum
operator|)
operator|/
literal|2.00f
expr_stmt|;
return|return
name|NO_OP_TUNER_RESULT
return|;
block|}
comment|// Check we are within max/min bounds.
if|if
condition|(
name|newMemstoreSize
operator|>
name|globalMemStorePercentMaxRange
condition|)
block|{
name|newMemstoreSize
operator|=
name|globalMemStorePercentMaxRange
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newMemstoreSize
operator|<
name|globalMemStorePercentMinRange
condition|)
block|{
name|newMemstoreSize
operator|=
name|globalMemStorePercentMinRange
expr_stmt|;
block|}
if|if
condition|(
name|newBlockCacheSize
operator|>
name|blockCachePercentMaxRange
condition|)
block|{
name|newBlockCacheSize
operator|=
name|blockCachePercentMaxRange
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newBlockCacheSize
operator|<
name|blockCachePercentMinRange
condition|)
block|{
name|newBlockCacheSize
operator|=
name|blockCachePercentMinRange
expr_stmt|;
block|}
name|TUNER_RESULT
operator|.
name|setBlockCacheSize
argument_list|(
name|newBlockCacheSize
argument_list|)
expr_stmt|;
name|TUNER_RESULT
operator|.
name|setMemstoreSize
argument_list|(
name|newMemstoreSize
argument_list|)
expr_stmt|;
name|prevTuneDirection
operator|=
name|newTuneDirection
expr_stmt|;
return|return
name|TUNER_RESULT
return|;
block|}
comment|/**    * Determine best direction of tuning base on given context.    * @param context The tuner context.    * @return tuning direction.    */
specifier|private
name|StepDirection
name|getTuneDirection
parameter_list|(
name|TunerContext
name|context
parameter_list|)
block|{
name|StepDirection
name|newTuneDirection
init|=
name|StepDirection
operator|.
name|NEUTRAL
decl_stmt|;
name|long
name|blockedFlushCount
init|=
name|context
operator|.
name|getBlockedFlushCount
argument_list|()
decl_stmt|;
name|long
name|unblockedFlushCount
init|=
name|context
operator|.
name|getUnblockedFlushCount
argument_list|()
decl_stmt|;
name|long
name|evictCount
init|=
name|context
operator|.
name|getEvictCount
argument_list|()
decl_stmt|;
name|long
name|cacheMissCount
init|=
name|context
operator|.
name|getCacheMissCount
argument_list|()
decl_stmt|;
name|long
name|totalFlushCount
init|=
name|blockedFlushCount
operator|+
name|unblockedFlushCount
decl_stmt|;
name|float
name|curMemstoreSize
init|=
name|context
operator|.
name|getCurMemStoreSize
argument_list|()
decl_stmt|;
name|float
name|curBlockCacheSize
init|=
name|context
operator|.
name|getCurBlockCacheSize
argument_list|()
decl_stmt|;
name|StringBuilder
name|tunerLog
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|// We can consider memstore or block cache to be sufficient if
comment|// we are using only a minor fraction of what have been already provided to it.
name|boolean
name|earlyMemstoreSufficientCheck
init|=
name|totalFlushCount
operator|==
literal|0
operator|||
name|context
operator|.
name|getCurMemStoreUsed
argument_list|()
operator|<
name|curMemstoreSize
operator|*
name|sufficientMemoryLevel
decl_stmt|;
name|boolean
name|earlyBlockCacheSufficientCheck
init|=
name|evictCount
operator|==
literal|0
operator|||
name|context
operator|.
name|getCurBlockCacheUsed
argument_list|()
operator|<
name|curBlockCacheSize
operator|*
name|sufficientMemoryLevel
decl_stmt|;
if|if
condition|(
name|earlyMemstoreSufficientCheck
operator|&&
name|earlyBlockCacheSufficientCheck
condition|)
block|{
comment|// Both memstore and block cache memory seems to be sufficient. No operation required.
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|NEUTRAL
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|earlyMemstoreSufficientCheck
condition|)
block|{
comment|// Increase the block cache size and corresponding decrease in memstore size.
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_BLOCK_CACHE_SIZE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|earlyBlockCacheSufficientCheck
condition|)
block|{
comment|// Increase the memstore size and corresponding decrease in block cache size.
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_MEMSTORE_SIZE
expr_stmt|;
block|}
else|else
block|{
comment|// Early checks for sufficient memory failed. Tuning memory based on past statistics.
comment|// Boolean indicator to show if we need to revert previous step or not.
name|boolean
name|isReverting
init|=
literal|false
decl_stmt|;
switch|switch
condition|(
name|prevTuneDirection
condition|)
block|{
comment|// Here we are using number of evictions rather than cache misses because it is more
comment|// strong indicator for deficient cache size. Improving caching is what we
comment|// would like to optimize for in steady state.
case|case
name|INCREASE_BLOCK_CACHE_SIZE
case|:
if|if
condition|(
operator|(
name|double
operator|)
name|evictCount
operator|>
name|rollingStatsForEvictions
operator|.
name|getMean
argument_list|()
operator|||
operator|(
name|double
operator|)
name|totalFlushCount
operator|>
name|rollingStatsForFlushes
operator|.
name|getMean
argument_list|()
operator|+
name|rollingStatsForFlushes
operator|.
name|getDeviation
argument_list|()
operator|/
literal|2.00
condition|)
block|{
comment|// Reverting previous step as it was not useful.
comment|// Tuning failed to decrease evictions or tuning resulted in large number of flushes.
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_MEMSTORE_SIZE
expr_stmt|;
name|tunerLog
operator|.
name|append
argument_list|(
literal|"We will revert previous tuning"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|double
operator|)
name|evictCount
operator|>
name|rollingStatsForEvictions
operator|.
name|getMean
argument_list|()
condition|)
block|{
name|tunerLog
operator|.
name|append
argument_list|(
literal|" because we could not decrease evictions sufficiently."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tunerLog
operator|.
name|append
argument_list|(
literal|" because the number of flushes rose significantly."
argument_list|)
expr_stmt|;
block|}
name|isReverting
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
name|INCREASE_MEMSTORE_SIZE
case|:
if|if
condition|(
operator|(
name|double
operator|)
name|totalFlushCount
operator|>
name|rollingStatsForFlushes
operator|.
name|getMean
argument_list|()
operator|||
operator|(
name|double
operator|)
name|evictCount
operator|>
name|rollingStatsForEvictions
operator|.
name|getMean
argument_list|()
operator|+
name|rollingStatsForEvictions
operator|.
name|getDeviation
argument_list|()
operator|/
literal|2.00
condition|)
block|{
comment|// Reverting previous step as it was not useful.
comment|// Tuning failed to decrease flushes or tuning resulted in large number of evictions.
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_BLOCK_CACHE_SIZE
expr_stmt|;
name|tunerLog
operator|.
name|append
argument_list|(
literal|"We will revert previous tuning"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|double
operator|)
name|totalFlushCount
operator|>
name|rollingStatsForFlushes
operator|.
name|getMean
argument_list|()
condition|)
block|{
name|tunerLog
operator|.
name|append
argument_list|(
literal|" because we could not decrease flushes sufficiently."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tunerLog
operator|.
name|append
argument_list|(
literal|" because number of evictions rose significantly."
argument_list|)
expr_stmt|;
block|}
name|isReverting
operator|=
literal|true
expr_stmt|;
block|}
break|break;
default|default:
comment|// Last step was neutral, revert doesn't not apply here.
break|break;
block|}
comment|// If we are not reverting. We try to tune memory sizes by looking at cache misses / flushes.
if|if
condition|(
operator|!
name|isReverting
condition|)
block|{
comment|// mean +- deviation*0.8 is considered to be normal
comment|// below it its consider low and above it is considered high.
comment|// We can safely assume that the number cache misses, flushes are normally distributed over
comment|// past periods and hence on all the above mentioned classes (normal, high and low)
comment|// are likely to occur with probability 56%, 22%, 22% respectively. Hence there is at
comment|// least ~10% probability that we will not fall in NEUTRAL step.
comment|// This optimization solution is feedback based and we revert when we
comment|// dont find our steps helpful. Hence we want to do tuning only when we have clear
comment|// indications because too many unnecessary tuning may affect the performance of cluster.
if|if
condition|(
operator|(
name|double
operator|)
name|cacheMissCount
operator|<
name|rollingStatsForCacheMisses
operator|.
name|getMean
argument_list|()
operator|-
name|rollingStatsForCacheMisses
operator|.
name|getDeviation
argument_list|()
operator|*
literal|0.80
operator|&&
operator|(
name|double
operator|)
name|totalFlushCount
operator|<
name|rollingStatsForFlushes
operator|.
name|getMean
argument_list|()
operator|-
name|rollingStatsForFlushes
operator|.
name|getDeviation
argument_list|()
operator|*
literal|0.80
condition|)
block|{
comment|// Everything is fine no tuning required
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|NEUTRAL
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|double
operator|)
name|cacheMissCount
operator|>
name|rollingStatsForCacheMisses
operator|.
name|getMean
argument_list|()
operator|+
name|rollingStatsForCacheMisses
operator|.
name|getDeviation
argument_list|()
operator|*
literal|0.80
operator|&&
operator|(
name|double
operator|)
name|totalFlushCount
operator|<
name|rollingStatsForFlushes
operator|.
name|getMean
argument_list|()
operator|-
name|rollingStatsForFlushes
operator|.
name|getDeviation
argument_list|()
operator|*
literal|0.80
condition|)
block|{
comment|// more misses , increasing cache size
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_BLOCK_CACHE_SIZE
expr_stmt|;
name|tunerLog
operator|.
name|append
argument_list|(
literal|"Going to increase block cache size due to increase in number of cache misses."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|double
operator|)
name|cacheMissCount
argument_list|<
name|rollingStatsForCacheMisses
operator|.
name|getMean
operator|(
operator|)
operator|-
name|rollingStatsForCacheMisses
operator|.
name|getDeviation
operator|(
operator|)
operator|*
literal|0.80
operator|&&
operator|(
name|double
operator|)
name|totalFlushCount
argument_list|>
name|rollingStatsForFlushes
operator|.
name|getMean
argument_list|()
operator|+
name|rollingStatsForFlushes
operator|.
name|getDeviation
argument_list|()
operator|*
literal|0.80
condition|)
block|{
comment|// more flushes , increasing memstore size
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_MEMSTORE_SIZE
expr_stmt|;
name|tunerLog
operator|.
name|append
argument_list|(
literal|"Going to increase memstore size due to increase in number of flushes."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|blockedFlushCount
operator|>
literal|0
operator|&&
name|prevTuneDirection
operator|==
name|StepDirection
operator|.
name|NEUTRAL
condition|)
block|{
comment|// we do not want blocked flushes
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|INCREASE_MEMSTORE_SIZE
expr_stmt|;
name|tunerLog
operator|.
name|append
argument_list|(
literal|"Going to increase memstore size due to"
operator|+
name|blockedFlushCount
operator|+
literal|" blocked flushes."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Default. Not enough facts to do tuning.
name|tunerLog
operator|.
name|append
argument_list|(
literal|"Going to do nothing because we "
operator|+
literal|"could not determine best tuning direction"
argument_list|)
expr_stmt|;
name|newTuneDirection
operator|=
name|StepDirection
operator|.
name|NEUTRAL
expr_stmt|;
block|}
block|}
block|}
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
name|tunerLog
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|newTuneDirection
return|;
block|}
comment|/**    * Add the given context to the rolling tuner stats.    * @param context The tuner context.    */
specifier|private
name|void
name|addToRollingStats
parameter_list|(
name|TunerContext
name|context
parameter_list|)
block|{
name|rollingStatsForCacheMisses
operator|.
name|insertDataValue
argument_list|(
name|context
operator|.
name|getCacheMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|rollingStatsForFlushes
operator|.
name|insertDataValue
argument_list|(
name|context
operator|.
name|getBlockedFlushCount
argument_list|()
operator|+
name|context
operator|.
name|getUnblockedFlushCount
argument_list|()
argument_list|)
expr_stmt|;
name|rollingStatsForEvictions
operator|.
name|insertDataValue
argument_list|(
name|context
operator|.
name|getEvictCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
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
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|maximumStepSize
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|MAX_STEP_KEY
argument_list|,
name|DEFAULT_MAX_STEP_VALUE
argument_list|)
expr_stmt|;
name|this
operator|.
name|minimumStepSize
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|MIN_STEP_KEY
argument_list|,
name|DEFAULT_MIN_STEP_VALUE
argument_list|)
expr_stmt|;
name|this
operator|.
name|step
operator|=
name|this
operator|.
name|maximumStepSize
expr_stmt|;
name|this
operator|.
name|sufficientMemoryLevel
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|SUFFICIENT_MEMORY_LEVEL_KEY
argument_list|,
name|DEFAULT_SUFFICIENT_MEMORY_LEVEL_VALUE
argument_list|)
expr_stmt|;
name|this
operator|.
name|tunerLookupPeriods
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|LOOKUP_PERIODS_KEY
argument_list|,
name|DEFAULT_LOOKUP_PERIODS
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCachePercentMinRange
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|BLOCK_CACHE_SIZE_MIN_RANGE_KEY
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCachePercentMaxRange
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|BLOCK_CACHE_SIZE_MAX_RANGE_KEY
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|globalMemStorePercentMinRange
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|MEMSTORE_SIZE_MIN_RANGE_KEY
argument_list|,
name|HeapMemorySizeUtil
operator|.
name|getGlobalMemStorePercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|globalMemStorePercentMaxRange
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|MEMSTORE_SIZE_MAX_RANGE_KEY
argument_list|,
name|HeapMemorySizeUtil
operator|.
name|getGlobalMemStorePercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Default value of periods to ignore is number of lookup periods
name|this
operator|.
name|numPeriodsToIgnore
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|NUM_PERIODS_TO_IGNORE
argument_list|,
name|this
operator|.
name|tunerLookupPeriods
argument_list|)
expr_stmt|;
name|this
operator|.
name|rollingStatsForCacheMisses
operator|=
operator|new
name|RollingStatCalculator
argument_list|(
name|this
operator|.
name|tunerLookupPeriods
argument_list|)
expr_stmt|;
name|this
operator|.
name|rollingStatsForFlushes
operator|=
operator|new
name|RollingStatCalculator
argument_list|(
name|this
operator|.
name|tunerLookupPeriods
argument_list|)
expr_stmt|;
name|this
operator|.
name|rollingStatsForEvictions
operator|=
operator|new
name|RollingStatCalculator
argument_list|(
name|this
operator|.
name|tunerLookupPeriods
argument_list|)
expr_stmt|;
name|this
operator|.
name|rollingStatsForTunerSteps
operator|=
operator|new
name|RollingStatCalculator
argument_list|(
name|this
operator|.
name|tunerLookupPeriods
argument_list|)
expr_stmt|;
block|}
specifier|private
enum|enum
name|StepDirection
block|{
comment|// block cache size was increased
name|INCREASE_BLOCK_CACHE_SIZE
block|,
comment|// memstore size was increased
name|INCREASE_MEMSTORE_SIZE
block|,
comment|// no operation was performed
name|NEUTRAL
block|}
block|}
end_class

end_unit

