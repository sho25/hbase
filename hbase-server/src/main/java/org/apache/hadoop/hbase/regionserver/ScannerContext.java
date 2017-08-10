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
package|;
end_package

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
operator|.
name|ServerSideScanMetrics
import|;
end_import

begin_comment
comment|/**  * ScannerContext instances encapsulate limit tracking AND progress towards those limits during  * invocations of {@link InternalScanner#next(java.util.List)} and  * {@link RegionScanner#next(java.util.List)}.  *<p>  * A ScannerContext instance should be updated periodically throughout execution whenever progress  * towards a limit has been made. Each limit can be checked via the appropriate checkLimit method.  *<p>  * Once a limit has been reached, the scan will stop. The invoker of  * {@link InternalScanner#next(java.util.List)} or {@link RegionScanner#next(java.util.List)} can  * use the appropriate check*Limit methods to see exactly which limits have been reached.  * Alternatively, {@link #checkAnyLimitReached(LimitScope)} is provided to see if ANY limit was  * reached  *<p>  * {@link NoLimitScannerContext#NO_LIMIT} is an immutable static definition that can be used  * whenever a {@link ScannerContext} is needed but limits do not need to be enforced.  *<p>  * NOTE: It is important that this class only ever expose setter methods that can be safely skipped  * when limits should be NOT enforced. This is because of the necessary immutability of the class  * {@link NoLimitScannerContext}. If a setter cannot be safely skipped, the immutable nature of  * {@link NoLimitScannerContext} will lead to incorrect behavior.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ScannerContext
block|{
comment|/**    * Two sets of the same fields. One for the limits, another for the progress towards those limits    */
name|LimitFields
name|limits
decl_stmt|;
name|LimitFields
name|progress
decl_stmt|;
comment|/**    * The state of the scanner after the invocation of {@link InternalScanner#next(java.util.List)}    * or {@link RegionScanner#next(java.util.List)}.    */
name|NextState
name|scannerState
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|NextState
name|DEFAULT_STATE
init|=
name|NextState
operator|.
name|MORE_VALUES
decl_stmt|;
comment|/**    * Used as an indication to invocations of {@link InternalScanner#next(java.util.List)} and    * {@link RegionScanner#next(java.util.List)} that, if true, the progress tracked within this    * {@link ScannerContext} instance should be considered while evaluating the limits. Useful for    * enforcing a set of limits across multiple calls (i.e. the limit may not be reached in a single    * invocation, but any progress made should be considered in future invocations)    *<p>    * Defaulting this value to false means that, by default, any tracked progress will be wiped clean    * on invocations to {@link InternalScanner#next(java.util.List)} and    * {@link RegionScanner#next(java.util.List)} and the call will be treated as though no progress    * has been made towards the limits so far.    *<p>    * This is an important mechanism. Users of Internal/Region scanners expect that they can define    * some limits and then repeatedly invoke {@link InternalScanner#next(List)} or    * {@link RegionScanner#next(List)} where each invocation respects these limits separately.    *<p>    * For example:<pre> {@code    * ScannerContext context = new ScannerContext.newBuilder().setBatchLimit(5).build();    * RegionScanner scanner = ...    * List<Cell> results = new ArrayList<Cell>();    * while(scanner.next(results, context)) {    *   // Do something with a batch of 5 cells    * }    * }</pre> However, in the case of RPCs, the server wants to be able to define a set of    * limits for a particular RPC request and have those limits respected across multiple    * invocations. This means that the progress made towards the limits in earlier calls will be    * saved and considered in future invocations    */
name|boolean
name|keepProgress
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|DEFAULT_KEEP_PROGRESS
init|=
literal|false
decl_stmt|;
specifier|private
name|Cell
name|lastPeekedCell
init|=
literal|null
decl_stmt|;
comment|/**    * Tracks the relevant server side metrics during scans. null when metrics should not be tracked    */
specifier|final
name|ServerSideScanMetrics
name|metrics
decl_stmt|;
name|ScannerContext
parameter_list|(
name|boolean
name|keepProgress
parameter_list|,
name|LimitFields
name|limitsToCopy
parameter_list|,
name|boolean
name|trackMetrics
parameter_list|)
block|{
name|this
operator|.
name|limits
operator|=
operator|new
name|LimitFields
argument_list|()
expr_stmt|;
if|if
condition|(
name|limitsToCopy
operator|!=
literal|null
condition|)
name|this
operator|.
name|limits
operator|.
name|copy
argument_list|(
name|limitsToCopy
argument_list|)
expr_stmt|;
comment|// Progress fields are initialized to 0
name|progress
operator|=
operator|new
name|LimitFields
argument_list|(
literal|0
argument_list|,
name|LimitFields
operator|.
name|DEFAULT_SCOPE
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|LimitFields
operator|.
name|DEFAULT_SCOPE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|keepProgress
operator|=
name|keepProgress
expr_stmt|;
name|this
operator|.
name|scannerState
operator|=
name|DEFAULT_STATE
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|trackMetrics
condition|?
operator|new
name|ServerSideScanMetrics
argument_list|()
else|:
literal|null
expr_stmt|;
block|}
name|boolean
name|isTrackingMetrics
parameter_list|()
block|{
return|return
name|this
operator|.
name|metrics
operator|!=
literal|null
return|;
block|}
comment|/**    * Get the metrics instance. Should only be called after a call to {@link #isTrackingMetrics()}    * has been made to confirm that metrics are indeed being tracked.    * @return {@link ServerSideScanMetrics} instance that is tracking metrics for this scan    */
name|ServerSideScanMetrics
name|getMetrics
parameter_list|()
block|{
assert|assert
name|isTrackingMetrics
argument_list|()
assert|;
return|return
name|this
operator|.
name|metrics
return|;
block|}
comment|/**    * @return true if the progress tracked so far in this instance will be considered during an    *         invocation of {@link InternalScanner#next(java.util.List)} or    *         {@link RegionScanner#next(java.util.List)}. false when the progress tracked so far    *         should not be considered and should instead be wiped away via {@link #clearProgress()}    */
name|boolean
name|getKeepProgress
parameter_list|()
block|{
return|return
name|keepProgress
return|;
block|}
name|void
name|setKeepProgress
parameter_list|(
name|boolean
name|keepProgress
parameter_list|)
block|{
name|this
operator|.
name|keepProgress
operator|=
name|keepProgress
expr_stmt|;
block|}
comment|/**    * Progress towards the batch limit has been made. Increment internal tracking of batch progress    */
name|void
name|incrementBatchProgress
parameter_list|(
name|int
name|batch
parameter_list|)
block|{
name|int
name|currentBatch
init|=
name|progress
operator|.
name|getBatch
argument_list|()
decl_stmt|;
name|progress
operator|.
name|setBatch
argument_list|(
name|currentBatch
operator|+
name|batch
argument_list|)
expr_stmt|;
block|}
comment|/**    * Progress towards the size limit has been made. Increment internal tracking of size progress    */
name|void
name|incrementSizeProgress
parameter_list|(
name|long
name|dataSize
parameter_list|,
name|long
name|heapSize
parameter_list|)
block|{
name|long
name|curDataSize
init|=
name|progress
operator|.
name|getDataSize
argument_list|()
decl_stmt|;
name|progress
operator|.
name|setDataSize
argument_list|(
name|curDataSize
operator|+
name|dataSize
argument_list|)
expr_stmt|;
name|long
name|curHeapSize
init|=
name|progress
operator|.
name|getHeapSize
argument_list|()
decl_stmt|;
name|progress
operator|.
name|setHeapSize
argument_list|(
name|curHeapSize
operator|+
name|heapSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the time progress with {@link System#currentTimeMillis()}    */
name|void
name|updateTimeProgress
parameter_list|()
block|{
name|progress
operator|.
name|setTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|getBatchProgress
parameter_list|()
block|{
return|return
name|progress
operator|.
name|getBatch
argument_list|()
return|;
block|}
name|long
name|getDataSizeProgress
parameter_list|()
block|{
return|return
name|progress
operator|.
name|getDataSize
argument_list|()
return|;
block|}
name|long
name|getHeapSizeProgress
parameter_list|()
block|{
return|return
name|progress
operator|.
name|getHeapSize
argument_list|()
return|;
block|}
name|long
name|getTimeProgress
parameter_list|()
block|{
return|return
name|progress
operator|.
name|getTime
argument_list|()
return|;
block|}
name|void
name|setProgress
parameter_list|(
name|int
name|batchProgress
parameter_list|,
name|long
name|sizeProgress
parameter_list|,
name|long
name|heapSizeProgress
parameter_list|,
name|long
name|timeProgress
parameter_list|)
block|{
name|setBatchProgress
argument_list|(
name|batchProgress
argument_list|)
expr_stmt|;
name|setSizeProgress
argument_list|(
name|sizeProgress
argument_list|,
name|heapSizeProgress
argument_list|)
expr_stmt|;
name|setTimeProgress
argument_list|(
name|timeProgress
argument_list|)
expr_stmt|;
block|}
name|void
name|setSizeProgress
parameter_list|(
name|long
name|dataSizeProgress
parameter_list|,
name|long
name|heapSizeProgress
parameter_list|)
block|{
name|progress
operator|.
name|setDataSize
argument_list|(
name|dataSizeProgress
argument_list|)
expr_stmt|;
name|progress
operator|.
name|setHeapSize
argument_list|(
name|heapSizeProgress
argument_list|)
expr_stmt|;
block|}
name|void
name|setBatchProgress
parameter_list|(
name|int
name|batchProgress
parameter_list|)
block|{
name|progress
operator|.
name|setBatch
argument_list|(
name|batchProgress
argument_list|)
expr_stmt|;
block|}
name|void
name|setTimeProgress
parameter_list|(
name|long
name|timeProgress
parameter_list|)
block|{
name|progress
operator|.
name|setTime
argument_list|(
name|timeProgress
argument_list|)
expr_stmt|;
block|}
comment|/**    * Clear away any progress that has been made so far. All progress fields are reset to initial    * values    */
name|void
name|clearProgress
parameter_list|()
block|{
name|progress
operator|.
name|setFields
argument_list|(
literal|0
argument_list|,
name|LimitFields
operator|.
name|DEFAULT_SCOPE
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|LimitFields
operator|.
name|DEFAULT_SCOPE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Note that this is not a typical setter. This setter returns the {@link NextState} that was    * passed in so that methods can be invoked against the new state. Furthermore, this pattern    * allows the {@link NoLimitScannerContext} to cleanly override this setter and simply return the    * new state, thus preserving the immutability of {@link NoLimitScannerContext}    * @param state    * @return The state that was passed in.    */
name|NextState
name|setScannerState
parameter_list|(
name|NextState
name|state
parameter_list|)
block|{
if|if
condition|(
operator|!
name|NextState
operator|.
name|isValidState
argument_list|(
name|state
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot set to invalid state: "
operator|+
name|state
argument_list|)
throw|;
block|}
name|this
operator|.
name|scannerState
operator|=
name|state
expr_stmt|;
return|return
name|state
return|;
block|}
comment|/**    * @return true when we have more cells for the current row. This usually because we have reached    *         a limit in the middle of a row    */
name|boolean
name|mayHaveMoreCellsInRow
parameter_list|()
block|{
return|return
name|scannerState
operator|==
name|NextState
operator|.
name|SIZE_LIMIT_REACHED_MID_ROW
operator|||
name|scannerState
operator|==
name|NextState
operator|.
name|TIME_LIMIT_REACHED_MID_ROW
operator|||
name|scannerState
operator|==
name|NextState
operator|.
name|BATCH_LIMIT_REACHED
return|;
block|}
comment|/**    * @param checkerScope    * @return true if the batch limit can be enforced in the checker's scope    */
name|boolean
name|hasBatchLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|limits
operator|.
name|canEnforceBatchLimitFromScope
argument_list|(
name|checkerScope
argument_list|)
operator|&&
name|limits
operator|.
name|getBatch
argument_list|()
operator|>
literal|0
return|;
block|}
comment|/**    * @param checkerScope    * @return true if the size limit can be enforced in the checker's scope    */
name|boolean
name|hasSizeLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|limits
operator|.
name|canEnforceSizeLimitFromScope
argument_list|(
name|checkerScope
argument_list|)
operator|&&
operator|(
name|limits
operator|.
name|getDataSize
argument_list|()
operator|>
literal|0
operator|||
name|limits
operator|.
name|getHeapSize
argument_list|()
operator|>
literal|0
operator|)
return|;
block|}
comment|/**    * @param checkerScope    * @return true if the time limit can be enforced in the checker's scope    */
name|boolean
name|hasTimeLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|limits
operator|.
name|canEnforceTimeLimitFromScope
argument_list|(
name|checkerScope
argument_list|)
operator|&&
name|limits
operator|.
name|getTime
argument_list|()
operator|>
literal|0
return|;
block|}
comment|/**    * @param checkerScope    * @return true if any limit can be enforced within the checker's scope    */
name|boolean
name|hasAnyLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|hasBatchLimit
argument_list|(
name|checkerScope
argument_list|)
operator|||
name|hasSizeLimit
argument_list|(
name|checkerScope
argument_list|)
operator|||
name|hasTimeLimit
argument_list|(
name|checkerScope
argument_list|)
return|;
block|}
comment|/**    * @param scope The scope in which the size limit will be enforced    */
name|void
name|setSizeLimitScope
parameter_list|(
name|LimitScope
name|scope
parameter_list|)
block|{
name|limits
operator|.
name|setSizeScope
argument_list|(
name|scope
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param scope The scope in which the time limit will be enforced    */
name|void
name|setTimeLimitScope
parameter_list|(
name|LimitScope
name|scope
parameter_list|)
block|{
name|limits
operator|.
name|setTimeScope
argument_list|(
name|scope
argument_list|)
expr_stmt|;
block|}
name|int
name|getBatchLimit
parameter_list|()
block|{
return|return
name|limits
operator|.
name|getBatch
argument_list|()
return|;
block|}
name|long
name|getDataSizeLimit
parameter_list|()
block|{
return|return
name|limits
operator|.
name|getDataSize
argument_list|()
return|;
block|}
name|long
name|getTimeLimit
parameter_list|()
block|{
return|return
name|limits
operator|.
name|getTime
argument_list|()
return|;
block|}
comment|/**    * @param checkerScope The scope that the limit is being checked from    * @return true when the limit is enforceable from the checker's scope and it has been reached    */
name|boolean
name|checkBatchLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|hasBatchLimit
argument_list|(
name|checkerScope
argument_list|)
operator|&&
name|progress
operator|.
name|getBatch
argument_list|()
operator|>=
name|limits
operator|.
name|getBatch
argument_list|()
return|;
block|}
comment|/**    * @param checkerScope The scope that the limit is being checked from    * @return true when the limit is enforceable from the checker's scope and it has been reached    */
name|boolean
name|checkSizeLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|hasSizeLimit
argument_list|(
name|checkerScope
argument_list|)
operator|&&
operator|(
name|progress
operator|.
name|getDataSize
argument_list|()
operator|>=
name|limits
operator|.
name|getDataSize
argument_list|()
operator|||
name|progress
operator|.
name|getHeapSize
argument_list|()
operator|>=
name|limits
operator|.
name|getHeapSize
argument_list|()
operator|)
return|;
block|}
comment|/**    * @param checkerScope The scope that the limit is being checked from. The time limit is always    *          checked against {@link System#currentTimeMillis()}    * @return true when the limit is enforceable from the checker's scope and it has been reached    */
name|boolean
name|checkTimeLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|hasTimeLimit
argument_list|(
name|checkerScope
argument_list|)
operator|&&
name|progress
operator|.
name|getTime
argument_list|()
operator|>=
name|limits
operator|.
name|getTime
argument_list|()
return|;
block|}
comment|/**    * @param checkerScope The scope that the limits are being checked from    * @return true when some limit is enforceable from the checker's scope and it has been reached    */
name|boolean
name|checkAnyLimitReached
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|checkSizeLimit
argument_list|(
name|checkerScope
argument_list|)
operator|||
name|checkBatchLimit
argument_list|(
name|checkerScope
argument_list|)
operator|||
name|checkTimeLimit
argument_list|(
name|checkerScope
argument_list|)
return|;
block|}
name|Cell
name|getLastPeekedCell
parameter_list|()
block|{
return|return
name|lastPeekedCell
return|;
block|}
name|void
name|setLastPeekedCell
parameter_list|(
name|Cell
name|lastPeekedCell
parameter_list|)
block|{
name|this
operator|.
name|lastPeekedCell
operator|=
name|lastPeekedCell
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"limits:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|limits
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", progress:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|progress
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", keepProgress:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|keepProgress
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", state:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|scannerState
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Builder
name|newBuilder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Builder
name|newBuilder
parameter_list|(
name|boolean
name|keepProgress
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|keepProgress
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|Builder
block|{
name|boolean
name|keepProgress
init|=
name|DEFAULT_KEEP_PROGRESS
decl_stmt|;
name|boolean
name|trackMetrics
init|=
literal|false
decl_stmt|;
name|LimitFields
name|limits
init|=
operator|new
name|LimitFields
argument_list|()
decl_stmt|;
specifier|private
name|Builder
parameter_list|()
block|{     }
specifier|private
name|Builder
parameter_list|(
name|boolean
name|keepProgress
parameter_list|)
block|{
name|this
operator|.
name|keepProgress
operator|=
name|keepProgress
expr_stmt|;
block|}
specifier|public
name|Builder
name|setKeepProgress
parameter_list|(
name|boolean
name|keepProgress
parameter_list|)
block|{
name|this
operator|.
name|keepProgress
operator|=
name|keepProgress
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setTrackMetrics
parameter_list|(
name|boolean
name|trackMetrics
parameter_list|)
block|{
name|this
operator|.
name|trackMetrics
operator|=
name|trackMetrics
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setSizeLimit
parameter_list|(
name|LimitScope
name|sizeScope
parameter_list|,
name|long
name|dataSizeLimit
parameter_list|,
name|long
name|heapSizeLimit
parameter_list|)
block|{
name|limits
operator|.
name|setDataSize
argument_list|(
name|dataSizeLimit
argument_list|)
expr_stmt|;
name|limits
operator|.
name|setHeapSize
argument_list|(
name|heapSizeLimit
argument_list|)
expr_stmt|;
name|limits
operator|.
name|setSizeScope
argument_list|(
name|sizeScope
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setTimeLimit
parameter_list|(
name|LimitScope
name|timeScope
parameter_list|,
name|long
name|timeLimit
parameter_list|)
block|{
name|limits
operator|.
name|setTime
argument_list|(
name|timeLimit
argument_list|)
expr_stmt|;
name|limits
operator|.
name|setTimeScope
argument_list|(
name|timeScope
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setBatchLimit
parameter_list|(
name|int
name|batchLimit
parameter_list|)
block|{
name|limits
operator|.
name|setBatch
argument_list|(
name|batchLimit
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ScannerContext
name|build
parameter_list|()
block|{
return|return
operator|new
name|ScannerContext
argument_list|(
name|keepProgress
argument_list|,
name|limits
argument_list|,
name|trackMetrics
argument_list|)
return|;
block|}
block|}
comment|/**    * The possible states a scanner may be in following a call to {@link InternalScanner#next(List)}    */
specifier|public
enum|enum
name|NextState
block|{
name|MORE_VALUES
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
block|,
name|NO_MORE_VALUES
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
block|,
name|SIZE_LIMIT_REACHED
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
block|,
comment|/**      * Special case of size limit reached to indicate that the size limit was reached in the middle      * of a row and thus a partial results was formed      */
name|SIZE_LIMIT_REACHED_MID_ROW
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
block|,
name|TIME_LIMIT_REACHED
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
block|,
comment|/**      * Special case of time limit reached to indicate that the time limit was reached in the middle      * of a row and thus a partial results was formed      */
name|TIME_LIMIT_REACHED_MID_ROW
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
block|,
name|BATCH_LIMIT_REACHED
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
block|;
specifier|private
name|boolean
name|moreValues
decl_stmt|;
specifier|private
name|boolean
name|limitReached
decl_stmt|;
specifier|private
name|NextState
parameter_list|(
name|boolean
name|moreValues
parameter_list|,
name|boolean
name|limitReached
parameter_list|)
block|{
name|this
operator|.
name|moreValues
operator|=
name|moreValues
expr_stmt|;
name|this
operator|.
name|limitReached
operator|=
name|limitReached
expr_stmt|;
block|}
comment|/**      * @return true when the state indicates that more values may follow those that have been      *         returned      */
specifier|public
name|boolean
name|hasMoreValues
parameter_list|()
block|{
return|return
name|this
operator|.
name|moreValues
return|;
block|}
comment|/**      * @return true when the state indicates that a limit has been reached and scan should stop      */
specifier|public
name|boolean
name|limitReached
parameter_list|()
block|{
return|return
name|this
operator|.
name|limitReached
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isValidState
parameter_list|(
name|NextState
name|state
parameter_list|)
block|{
return|return
name|state
operator|!=
literal|null
return|;
block|}
specifier|public
specifier|static
name|boolean
name|hasMoreValues
parameter_list|(
name|NextState
name|state
parameter_list|)
block|{
return|return
name|isValidState
argument_list|(
name|state
argument_list|)
operator|&&
name|state
operator|.
name|hasMoreValues
argument_list|()
return|;
block|}
block|}
comment|/**    * The various scopes where a limit can be enforced. Used to differentiate when a limit should be    * enforced or not.    */
specifier|public
enum|enum
name|LimitScope
block|{
comment|/**      * Enforcing a limit between rows means that the limit will not be considered until all the      * cells for a particular row have been retrieved      */
name|BETWEEN_ROWS
argument_list|(
literal|0
argument_list|)
block|,
comment|/**      * Enforcing a limit between cells means that the limit will be considered after each full cell      * has been retrieved      */
name|BETWEEN_CELLS
argument_list|(
literal|1
argument_list|)
block|;
comment|/**      * When enforcing a limit, we must check that the scope is appropriate for enforcement.      *<p>      * To communicate this concept, each scope has a depth. A limit will be enforced if the depth of      * the checker's scope is less than or equal to the limit's scope. This means that when checking      * limits, the checker must know their own scope (i.e. are they checking the limits between      * rows, between cells, etc...)      */
name|int
name|depth
decl_stmt|;
name|LimitScope
parameter_list|(
name|int
name|depth
parameter_list|)
block|{
name|this
operator|.
name|depth
operator|=
name|depth
expr_stmt|;
block|}
name|int
name|depth
parameter_list|()
block|{
return|return
name|depth
return|;
block|}
comment|/**      * @param checkerScope The scope in which the limit is being checked      * @return true when the checker is in a scope that indicates the limit can be enforced. Limits      *         can be enforced from "higher or equal" scopes (i.e. the checker's scope is at a      *         lesser depth than the limit)      */
name|boolean
name|canEnforceLimitFromScope
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|checkerScope
operator|!=
literal|null
operator|&&
name|checkerScope
operator|.
name|depth
argument_list|()
operator|<=
name|depth
return|;
block|}
block|}
comment|/**    * The different fields that can be used as limits in calls to    * {@link InternalScanner#next(java.util.List)} and {@link RegionScanner#next(java.util.List)}    */
specifier|private
specifier|static
class|class
name|LimitFields
block|{
comment|/**      * Default values of the limit fields. Defined such that if a field does NOT change from its      * default, it will not be enforced      */
specifier|private
specifier|static
name|int
name|DEFAULT_BATCH
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
name|long
name|DEFAULT_SIZE
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
specifier|static
name|long
name|DEFAULT_TIME
init|=
operator|-
literal|1L
decl_stmt|;
comment|/**      * Default scope that is assigned to a limit if a scope is not specified.      */
specifier|private
specifier|static
specifier|final
name|LimitScope
name|DEFAULT_SCOPE
init|=
name|LimitScope
operator|.
name|BETWEEN_ROWS
decl_stmt|;
comment|// The batch limit will always be enforced between cells, thus, there isn't a field to hold the
comment|// batch scope
name|int
name|batch
init|=
name|DEFAULT_BATCH
decl_stmt|;
name|LimitScope
name|sizeScope
init|=
name|DEFAULT_SCOPE
decl_stmt|;
comment|// The sum of cell data sizes(key + value). The Cell data might be in on heap or off heap area.
name|long
name|dataSize
init|=
name|DEFAULT_SIZE
decl_stmt|;
comment|// The sum of heap space occupied by all tracked cells. This includes Cell POJO's overhead as
comment|// such AND data cells of Cells which are in on heap area.
name|long
name|heapSize
init|=
name|DEFAULT_SIZE
decl_stmt|;
name|LimitScope
name|timeScope
init|=
name|DEFAULT_SCOPE
decl_stmt|;
name|long
name|time
init|=
name|DEFAULT_TIME
decl_stmt|;
comment|/**      * Fields keep their default values.      */
name|LimitFields
parameter_list|()
block|{     }
name|LimitFields
parameter_list|(
name|int
name|batch
parameter_list|,
name|LimitScope
name|sizeScope
parameter_list|,
name|long
name|size
parameter_list|,
name|long
name|heapSize
parameter_list|,
name|LimitScope
name|timeScope
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|setFields
argument_list|(
name|batch
argument_list|,
name|sizeScope
argument_list|,
name|size
argument_list|,
name|heapSize
argument_list|,
name|timeScope
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
name|void
name|copy
parameter_list|(
name|LimitFields
name|limitsToCopy
parameter_list|)
block|{
if|if
condition|(
name|limitsToCopy
operator|!=
literal|null
condition|)
block|{
name|setFields
argument_list|(
name|limitsToCopy
operator|.
name|getBatch
argument_list|()
argument_list|,
name|limitsToCopy
operator|.
name|getSizeScope
argument_list|()
argument_list|,
name|limitsToCopy
operator|.
name|getDataSize
argument_list|()
argument_list|,
name|limitsToCopy
operator|.
name|getHeapSize
argument_list|()
argument_list|,
name|limitsToCopy
operator|.
name|getTimeScope
argument_list|()
argument_list|,
name|limitsToCopy
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Set all fields together.      * @param batch      * @param sizeScope      * @param dataSize      */
name|void
name|setFields
parameter_list|(
name|int
name|batch
parameter_list|,
name|LimitScope
name|sizeScope
parameter_list|,
name|long
name|dataSize
parameter_list|,
name|long
name|heapSize
parameter_list|,
name|LimitScope
name|timeScope
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|setBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|setSizeScope
argument_list|(
name|sizeScope
argument_list|)
expr_stmt|;
name|setDataSize
argument_list|(
name|dataSize
argument_list|)
expr_stmt|;
name|setHeapSize
argument_list|(
name|heapSize
argument_list|)
expr_stmt|;
name|setTimeScope
argument_list|(
name|timeScope
argument_list|)
expr_stmt|;
name|setTime
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
name|int
name|getBatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|batch
return|;
block|}
name|void
name|setBatch
parameter_list|(
name|int
name|batch
parameter_list|)
block|{
name|this
operator|.
name|batch
operator|=
name|batch
expr_stmt|;
block|}
comment|/**      * @param checkerScope      * @return true when the limit can be enforced from the scope of the checker      */
name|boolean
name|canEnforceBatchLimitFromScope
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|LimitScope
operator|.
name|BETWEEN_CELLS
operator|.
name|canEnforceLimitFromScope
argument_list|(
name|checkerScope
argument_list|)
return|;
block|}
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|dataSize
return|;
block|}
name|long
name|getHeapSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|heapSize
return|;
block|}
name|void
name|setDataSize
parameter_list|(
name|long
name|dataSize
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|=
name|dataSize
expr_stmt|;
block|}
name|void
name|setHeapSize
parameter_list|(
name|long
name|heapSize
parameter_list|)
block|{
name|this
operator|.
name|heapSize
operator|=
name|heapSize
expr_stmt|;
block|}
comment|/**      * @return {@link LimitScope} indicating scope in which the size limit is enforced      */
name|LimitScope
name|getSizeScope
parameter_list|()
block|{
return|return
name|this
operator|.
name|sizeScope
return|;
block|}
comment|/**      * Change the scope in which the size limit is enforced      */
name|void
name|setSizeScope
parameter_list|(
name|LimitScope
name|scope
parameter_list|)
block|{
name|this
operator|.
name|sizeScope
operator|=
name|scope
expr_stmt|;
block|}
comment|/**      * @param checkerScope      * @return true when the limit can be enforced from the scope of the checker      */
name|boolean
name|canEnforceSizeLimitFromScope
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|this
operator|.
name|sizeScope
operator|.
name|canEnforceLimitFromScope
argument_list|(
name|checkerScope
argument_list|)
return|;
block|}
name|long
name|getTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|time
return|;
block|}
name|void
name|setTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|this
operator|.
name|time
operator|=
name|time
expr_stmt|;
block|}
comment|/**      * @return {@link LimitScope} indicating scope in which the time limit is enforced      */
name|LimitScope
name|getTimeScope
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeScope
return|;
block|}
comment|/**      * Change the scope in which the time limit is enforced      */
name|void
name|setTimeScope
parameter_list|(
name|LimitScope
name|scope
parameter_list|)
block|{
name|this
operator|.
name|timeScope
operator|=
name|scope
expr_stmt|;
block|}
comment|/**      * @param checkerScope      * @return true when the limit can be enforced from the scope of the checker      */
name|boolean
name|canEnforceTimeLimitFromScope
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
name|this
operator|.
name|timeScope
operator|.
name|canEnforceLimitFromScope
argument_list|(
name|checkerScope
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"batch:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", dataSize:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|dataSize
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", heapSize:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|heapSize
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", sizeScope:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|sizeScope
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", time:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|time
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", timeScope:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|timeScope
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

