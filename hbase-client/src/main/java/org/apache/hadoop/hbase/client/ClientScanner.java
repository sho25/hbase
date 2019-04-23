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
name|client
operator|.
name|ConnectionUtils
operator|.
name|calcEstimatedSize
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
name|client
operator|.
name|ConnectionUtils
operator|.
name|createScanResultCache
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
name|client
operator|.
name|ConnectionUtils
operator|.
name|incRegionCountMetrics
import|;
end_import

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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ExecutorService
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
name|lang3
operator|.
name|mutable
operator|.
name|MutableBoolean
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
name|DoNotRetryIOException
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
name|NotServingRegionException
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
name|UnknownScannerException
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
name|ScannerCallable
operator|.
name|MoreResults
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
name|exceptions
operator|.
name|OutOfOrderScannerNextException
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
name|exceptions
operator|.
name|ScannerResetException
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
name|RpcControllerFactory
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
name|LeaseException
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
name|RegionServerStoppedException
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
name|Bytes
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
comment|/**  * Implements the scanner interface for the HBase client. If there are multiple regions in a table,  * this scanner will iterate through them all.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ClientScanner
extends|extends
name|AbstractClientScanner
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
name|ClientScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|Scan
name|scan
decl_stmt|;
specifier|protected
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
comment|// Current region scanner is against. Gets cleared if current region goes
comment|// wonky: e.g. if it splits on us.
specifier|protected
name|HRegionInfo
name|currentRegion
init|=
literal|null
decl_stmt|;
specifier|protected
name|ScannerCallableWithReplicas
name|callable
init|=
literal|null
decl_stmt|;
specifier|protected
name|Queue
argument_list|<
name|Result
argument_list|>
name|cache
decl_stmt|;
specifier|private
specifier|final
name|ScanResultCache
name|scanResultCache
decl_stmt|;
specifier|protected
specifier|final
name|int
name|caching
decl_stmt|;
specifier|protected
name|long
name|lastNext
decl_stmt|;
comment|// Keep lastResult returned successfully in case we have to reset scanner.
specifier|protected
name|Result
name|lastResult
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|long
name|maxScannerResultSize
decl_stmt|;
specifier|private
specifier|final
name|ClusterConnection
name|connection
decl_stmt|;
specifier|protected
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|protected
specifier|final
name|int
name|scannerTimeout
decl_stmt|;
specifier|protected
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
decl_stmt|;
specifier|protected
name|RpcControllerFactory
name|rpcControllerFactory
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
comment|// The timeout on the primary. Applicable if there are multiple replicas for a region
comment|// In that case, we will only wait for this much timeout on the primary before going
comment|// to the replicas and trying the same scan. Note that the retries will still happen
comment|// on each replica and the first successful results will be taken. A timeout of 0 is
comment|// disallowed.
specifier|protected
specifier|final
name|int
name|primaryOperationTimeout
decl_stmt|;
specifier|private
name|int
name|retries
decl_stmt|;
specifier|protected
specifier|final
name|ExecutorService
name|pool
decl_stmt|;
comment|/**    * Create a new ClientScanner for the specified table Note that the passed {@link Scan}'s start    * row maybe changed changed.    * @param conf The {@link Configuration} to use.    * @param scan {@link Scan} to use in this scanner    * @param tableName The table that we wish to scan    * @param connection Connection identifying the cluster    * @throws IOException    */
specifier|public
name|ClientScanner
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|ClusterConnection
name|connection
parameter_list|,
name|RpcRetryingCallerFactory
name|rpcFactory
parameter_list|,
name|RpcControllerFactory
name|controllerFactory
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|int
name|primaryOperationTimeout
parameter_list|)
throws|throws
name|IOException
block|{
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
literal|"Scan table="
operator|+
name|tableName
operator|+
literal|", startRow="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|lastNext
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|primaryOperationTimeout
operator|=
name|primaryOperationTimeout
expr_stmt|;
name|this
operator|.
name|retries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_RETRIES_NUMBER
argument_list|)
expr_stmt|;
if|if
condition|(
name|scan
operator|.
name|getMaxResultSize
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|maxScannerResultSize
operator|=
name|scan
operator|.
name|getMaxResultSize
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|maxScannerResultSize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scannerTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|)
expr_stmt|;
comment|// check if application wants to collect scan metrics
name|initScanMetrics
argument_list|(
name|scan
argument_list|)
expr_stmt|;
comment|// Use the caching from the Scan. If not set, use the default cache setting for this table.
if|if
condition|(
name|this
operator|.
name|scan
operator|.
name|getCaching
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|caching
operator|=
name|this
operator|.
name|scan
operator|.
name|getCaching
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|caching
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_CACHING
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_CACHING
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|caller
operator|=
name|rpcFactory
operator|.
expr|<
name|Result
index|[]
operator|>
name|newCaller
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcControllerFactory
operator|=
name|controllerFactory
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|scanResultCache
operator|=
name|createScanResultCache
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|initCache
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
name|this
operator|.
name|connection
return|;
block|}
specifier|protected
name|TableName
name|getTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
block|}
specifier|protected
name|int
name|getRetries
parameter_list|()
block|{
return|return
name|this
operator|.
name|retries
return|;
block|}
specifier|protected
name|int
name|getScannerTimeout
parameter_list|()
block|{
return|return
name|this
operator|.
name|scannerTimeout
return|;
block|}
specifier|protected
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
specifier|protected
name|Scan
name|getScan
parameter_list|()
block|{
return|return
name|scan
return|;
block|}
specifier|protected
name|ExecutorService
name|getPool
parameter_list|()
block|{
return|return
name|pool
return|;
block|}
specifier|protected
name|int
name|getPrimaryOperationTimeout
parameter_list|()
block|{
return|return
name|primaryOperationTimeout
return|;
block|}
specifier|protected
name|int
name|getCaching
parameter_list|()
block|{
return|return
name|caching
return|;
block|}
specifier|protected
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|lastNext
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|long
name|getMaxResultSize
parameter_list|()
block|{
return|return
name|maxScannerResultSize
return|;
block|}
specifier|private
name|void
name|closeScanner
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|callable
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
name|call
argument_list|(
name|callable
argument_list|,
name|caller
argument_list|,
name|scannerTimeout
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|callable
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Will be called in moveToNextRegion when currentRegion is null. Abstract because for normal    * scan, we will start next scan from the endKey of the currentRegion, and for reversed scan, we    * will start next scan from the startKey of the currentRegion.    * @return {@code false} if we have reached the stop row. Otherwise {@code true}.    */
specifier|protected
specifier|abstract
name|boolean
name|setNewStartKey
parameter_list|()
function_decl|;
comment|/**    * Will be called in moveToNextRegion to create ScannerCallable. Abstract because for reversed    * scan we need to create a ReversedScannerCallable.    */
specifier|protected
specifier|abstract
name|ScannerCallable
name|createScannerCallable
parameter_list|()
function_decl|;
comment|/**    * Close the previous scanner and create a new ScannerCallable for the next scanner.    *<p>    * Marked as protected only because TestClientScanner need to override this method.    * @return false if we should terminate the scan. Otherwise    */
annotation|@
name|VisibleForTesting
specifier|protected
name|boolean
name|moveToNextRegion
parameter_list|()
block|{
comment|// Close the previous scanner if it's open
try|try
block|{
name|closeScanner
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// not a big deal continue
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
literal|"close scanner for "
operator|+
name|currentRegion
operator|+
literal|" failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|currentRegion
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|setNewStartKey
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|scan
operator|.
name|resetMvccReadPoint
argument_list|()
expr_stmt|;
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
literal|"Finished "
operator|+
name|this
operator|.
name|currentRegion
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
name|this
operator|.
name|currentRegion
operator|!=
literal|null
condition|)
block|{
comment|// Only worth logging if NOT first region in scan.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Advancing internal scanner to startKey at '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
operator|+
literal|"', "
operator|+
operator|(
name|scan
operator|.
name|includeStartRow
argument_list|()
condition|?
literal|"inclusive"
else|:
literal|"exclusive"
operator|)
argument_list|)
expr_stmt|;
block|}
comment|// clear the current region, we will set a new value to it after the first call of the new
comment|// callable.
name|this
operator|.
name|currentRegion
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|callable
operator|=
operator|new
name|ScannerCallableWithReplicas
argument_list|(
name|getTable
argument_list|()
argument_list|,
name|getConnection
argument_list|()
argument_list|,
name|createScannerCallable
argument_list|()
argument_list|,
name|pool
argument_list|,
name|primaryOperationTimeout
argument_list|,
name|scan
argument_list|,
name|getRetries
argument_list|()
argument_list|,
name|scannerTimeout
argument_list|,
name|caching
argument_list|,
name|conf
argument_list|,
name|caller
argument_list|)
expr_stmt|;
name|this
operator|.
name|callable
operator|.
name|setCaching
argument_list|(
name|this
operator|.
name|caching
argument_list|)
expr_stmt|;
name|incRegionCountMetrics
argument_list|(
name|scanMetrics
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|VisibleForTesting
name|boolean
name|isAnyRPCcancelled
parameter_list|()
block|{
return|return
name|callable
operator|.
name|isAnyRPCcancelled
argument_list|()
return|;
block|}
specifier|private
name|Result
index|[]
name|call
parameter_list|(
name|ScannerCallableWithReplicas
name|callable
parameter_list|,
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
parameter_list|,
name|int
name|scannerTimeout
parameter_list|,
name|boolean
name|updateCurrentRegion
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
comment|// callWithoutRetries is at this layer. Within the ScannerCallableWithReplicas,
comment|// we do a callWithRetries
name|Result
index|[]
name|rrs
init|=
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callable
argument_list|,
name|scannerTimeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentRegion
operator|==
literal|null
operator|&&
name|updateCurrentRegion
condition|)
block|{
name|currentRegion
operator|=
name|callable
operator|.
name|getHRegionInfo
argument_list|()
expr_stmt|;
block|}
return|return
name|rrs
return|;
block|}
specifier|protected
name|void
name|initSyncCache
parameter_list|()
block|{
name|cache
operator|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|Result
name|nextWithSyncCache
parameter_list|()
throws|throws
name|IOException
block|{
name|Result
name|result
init|=
name|cache
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
comment|// If there is nothing left in the cache and the scanner is closed,
comment|// return a no-op
if|if
condition|(
name|this
operator|.
name|closed
condition|)
block|{
return|return
literal|null
return|;
block|}
name|loadCache
argument_list|()
expr_stmt|;
comment|// try again to load from cache
name|result
operator|=
name|cache
operator|.
name|poll
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getCacheSize
parameter_list|()
block|{
return|return
name|cache
operator|!=
literal|null
condition|?
name|cache
operator|.
name|size
argument_list|()
else|:
literal|0
return|;
block|}
specifier|private
name|boolean
name|scanExhausted
parameter_list|(
name|Result
index|[]
name|values
parameter_list|)
block|{
return|return
name|callable
operator|.
name|moreResultsForScan
argument_list|()
operator|==
name|MoreResults
operator|.
name|NO
return|;
block|}
specifier|private
name|boolean
name|regionExhausted
parameter_list|(
name|Result
index|[]
name|values
parameter_list|)
block|{
comment|// 1. Not a heartbeat message and we get nothing, this means the region is exhausted. And in the
comment|// old time we always return empty result for a open scanner operation so we add a check here to
comment|// keep compatible with the old logic. Should remove the isOpenScanner in the future.
comment|// 2. Server tells us that it has no more results for this region.
return|return
operator|(
name|values
operator|.
name|length
operator|==
literal|0
operator|&&
operator|!
name|callable
operator|.
name|isHeartbeatMessage
argument_list|()
operator|)
operator|||
name|callable
operator|.
name|moreResultsInRegion
argument_list|()
operator|==
name|MoreResults
operator|.
name|NO
return|;
block|}
specifier|private
name|void
name|closeScannerIfExhausted
parameter_list|(
name|boolean
name|exhausted
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|exhausted
condition|)
block|{
name|closeScanner
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleScanError
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|,
name|MutableBoolean
name|retryAfterOutOfOrderException
parameter_list|,
name|int
name|retriesLeft
parameter_list|)
throws|throws
name|DoNotRetryIOException
block|{
comment|// An exception was thrown which makes any partial results that we were collecting
comment|// invalid. The scanner will need to be reset to the beginning of a row.
name|scanResultCache
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Unfortunately, DNRIOE is used in two different semantics.
comment|// (1) The first is to close the client scanner and bubble up the exception all the way
comment|// to the application. This is preferred when the exception is really un-recoverable
comment|// (like CorruptHFileException, etc). Plain DoNotRetryIOException also falls into this
comment|// bucket usually.
comment|// (2) Second semantics is to close the current region scanner only, but continue the
comment|// client scanner by overriding the exception. This is usually UnknownScannerException,
comment|// OutOfOrderScannerNextException, etc where the region scanner has to be closed, but the
comment|// application-level ClientScanner has to continue without bubbling up the exception to
comment|// the client. See RSRpcServices to see how it throws DNRIOE's.
comment|// See also: HBASE-16604, HBASE-17187
comment|// If exception is any but the list below throw it back to the client; else setup
comment|// the scanner and retry.
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|NotServingRegionException
operator|)
operator|||
operator|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|RegionServerStoppedException
operator|)
operator|||
name|e
operator|instanceof
name|OutOfOrderScannerNextException
operator|||
name|e
operator|instanceof
name|UnknownScannerException
operator|||
name|e
operator|instanceof
name|ScannerResetException
operator|||
name|e
operator|instanceof
name|LeaseException
condition|)
block|{
comment|// Pass. It is easier writing the if loop test as list of what is allowed rather than
comment|// as a list of what is not allowed... so if in here, it means we do not throw.
if|if
condition|(
name|retriesLeft
operator|<=
literal|0
condition|)
block|{
throw|throw
name|e
throw|;
comment|// no more retries
block|}
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
comment|// Else, its signal from depths of ScannerCallable that we need to reset the scanner.
if|if
condition|(
name|this
operator|.
name|lastResult
operator|!=
literal|null
condition|)
block|{
comment|// The region has moved. We need to open a brand new scanner at the new location.
comment|// Reset the startRow to the row we've seen last so that the new scanner starts at
comment|// the correct row. Otherwise we may see previously returned rows again.
comment|// If the lastRow is not partial, then we should start from the next row. As now we can
comment|// exclude the start row, the logic here is the same for both normal scan and reversed scan.
comment|// If lastResult is partial then include it, otherwise exclude it.
name|scan
operator|.
name|withStartRow
argument_list|(
name|lastResult
operator|.
name|getRow
argument_list|()
argument_list|,
name|lastResult
operator|.
name|mayHaveMoreCellsInRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|OutOfOrderScannerNextException
condition|)
block|{
if|if
condition|(
name|retryAfterOutOfOrderException
operator|.
name|isTrue
argument_list|()
condition|)
block|{
name|retryAfterOutOfOrderException
operator|.
name|setValue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: Why wrap this in a DNRIOE when it already is a DNRIOE?
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Failed after retry of OutOfOrderScannerNextException: was there a rpc timeout?"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Clear region.
name|this
operator|.
name|currentRegion
operator|=
literal|null
expr_stmt|;
comment|// Set this to zero so we don't try and do an rpc and close on remote server when
comment|// the exception we got was UnknownScanner or the Server is going down.
name|callable
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Contact the servers to load more {@link Result}s in the cache.    */
specifier|protected
name|void
name|loadCache
parameter_list|()
throws|throws
name|IOException
block|{
comment|// check if scanner was closed during previous prefetch
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|long
name|remainingResultSize
init|=
name|maxScannerResultSize
decl_stmt|;
name|int
name|countdown
init|=
name|this
operator|.
name|caching
decl_stmt|;
comment|// This is possible if we just stopped at the boundary of a region in the previous call.
if|if
condition|(
name|callable
operator|==
literal|null
operator|&&
operator|!
name|moveToNextRegion
argument_list|()
condition|)
block|{
name|closed
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// This flag is set when we want to skip the result returned. We do
comment|// this when we reset scanner because it split under us.
name|MutableBoolean
name|retryAfterOutOfOrderException
init|=
operator|new
name|MutableBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|// Even if we are retrying due to UnknownScannerException, ScannerResetException, etc. we should
comment|// make sure that we are not retrying indefinitely.
name|int
name|retriesLeft
init|=
name|getRetries
argument_list|()
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|Result
index|[]
name|values
decl_stmt|;
try|try
block|{
comment|// Server returns a null values if scanning is to stop. Else,
comment|// returns an empty array if scanning is to go on and we've just
comment|// exhausted current region.
comment|// now we will also fetch data when openScanner, so do not make a next call again if values
comment|// is already non-null.
name|values
operator|=
name|call
argument_list|(
name|callable
argument_list|,
name|caller
argument_list|,
name|scannerTimeout
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// When the replica switch happens, we need to do certain operations again.
comment|// The callable will openScanner with the right startkey but we need to pick up
comment|// from there. Bypass the rest of the loop and let the catch-up happen in the beginning
comment|// of the loop as it happens for the cases where we see exceptions.
if|if
condition|(
name|callable
operator|.
name|switchedToADifferentReplica
argument_list|()
condition|)
block|{
comment|// Any accumulated partial results are no longer valid since the callable will
comment|// openScanner with the correct startkey and we must pick up from there
name|scanResultCache
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|currentRegion
operator|=
name|callable
operator|.
name|getHRegionInfo
argument_list|()
expr_stmt|;
block|}
name|retryAfterOutOfOrderException
operator|.
name|setValue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
name|handleScanError
argument_list|(
name|e
argument_list|,
name|retryAfterOutOfOrderException
argument_list|,
name|retriesLeft
operator|--
argument_list|)
expr_stmt|;
comment|// reopen the scanner
if|if
condition|(
operator|!
name|moveToNextRegion
argument_list|()
condition|)
block|{
break|break;
block|}
continue|continue;
block|}
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|sumOfMillisSecBetweenNexts
operator|.
name|addAndGet
argument_list|(
name|currentTime
operator|-
name|lastNext
argument_list|)
expr_stmt|;
block|}
name|lastNext
operator|=
name|currentTime
expr_stmt|;
comment|// Groom the array of Results that we received back from the server before adding that
comment|// Results to the scanner's cache. If partial results are not allowed to be seen by the
comment|// caller, all book keeping will be performed within this method.
name|int
name|numberOfCompleteRowsBefore
init|=
name|scanResultCache
operator|.
name|numberOfCompleteRows
argument_list|()
decl_stmt|;
name|Result
index|[]
name|resultsToAddToCache
init|=
name|scanResultCache
operator|.
name|addAndGet
argument_list|(
name|values
argument_list|,
name|callable
operator|.
name|isHeartbeatMessage
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numberOfCompleteRows
init|=
name|scanResultCache
operator|.
name|numberOfCompleteRows
argument_list|()
operator|-
name|numberOfCompleteRowsBefore
decl_stmt|;
for|for
control|(
name|Result
name|rs
range|:
name|resultsToAddToCache
control|)
block|{
name|cache
operator|.
name|add
argument_list|(
name|rs
argument_list|)
expr_stmt|;
name|long
name|estimatedHeapSizeOfResult
init|=
name|calcEstimatedSize
argument_list|(
name|rs
argument_list|)
decl_stmt|;
name|countdown
operator|--
expr_stmt|;
name|remainingResultSize
operator|-=
name|estimatedHeapSizeOfResult
expr_stmt|;
name|addEstimatedSize
argument_list|(
name|estimatedHeapSizeOfResult
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastResult
operator|=
name|rs
expr_stmt|;
block|}
if|if
condition|(
name|scan
operator|.
name|getLimit
argument_list|()
operator|>
literal|0
condition|)
block|{
name|int
name|newLimit
init|=
name|scan
operator|.
name|getLimit
argument_list|()
operator|-
name|numberOfCompleteRows
decl_stmt|;
assert|assert
name|newLimit
operator|>=
literal|0
assert|;
name|scan
operator|.
name|setLimit
argument_list|(
name|newLimit
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scan
operator|.
name|getLimit
argument_list|()
operator|==
literal|0
operator|||
name|scanExhausted
argument_list|(
name|values
argument_list|)
condition|)
block|{
name|closeScanner
argument_list|()
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|boolean
name|regionExhausted
init|=
name|regionExhausted
argument_list|(
name|values
argument_list|)
decl_stmt|;
if|if
condition|(
name|callable
operator|.
name|isHeartbeatMessage
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|cache
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Caller of this method just wants a Result. If we see a heartbeat message, it means
comment|// processing of the scan is taking a long time server side. Rather than continue to
comment|// loop until a limit (e.g. size or caching) is reached, break out early to avoid causing
comment|// unnecesary delays to the caller
name|LOG
operator|.
name|trace
argument_list|(
literal|"Heartbeat message received and cache contains Results. "
operator|+
literal|"Breaking out of scan loop"
argument_list|)
expr_stmt|;
comment|// we know that the region has not been exhausted yet so just break without calling
comment|// closeScannerIfExhausted
break|break;
block|}
block|}
if|if
condition|(
name|cache
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|closed
operator|&&
name|scan
operator|.
name|isNeedCursorResult
argument_list|()
condition|)
block|{
if|if
condition|(
name|callable
operator|.
name|isHeartbeatMessage
argument_list|()
operator|&&
name|callable
operator|.
name|getCursor
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// Use cursor row key from server
name|cache
operator|.
name|add
argument_list|(
name|Result
operator|.
name|createCursorResult
argument_list|(
name|callable
operator|.
name|getCursor
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|values
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// It is size limit exceed and we need return the last Result's row.
comment|// When user setBatch and the scanner is reopened, the server may return Results that
comment|// user has seen and the last Result can not be seen because the number is not enough.
comment|// So the row keys of results may not be same, we must use the last one.
name|cache
operator|.
name|add
argument_list|(
name|Result
operator|.
name|createCursorResult
argument_list|(
operator|new
name|Cursor
argument_list|(
name|values
index|[
name|values
operator|.
name|length
operator|-
literal|1
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|countdown
operator|<=
literal|0
condition|)
block|{
comment|// we have enough result.
name|closeScannerIfExhausted
argument_list|(
name|regionExhausted
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|remainingResultSize
operator|<=
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|cache
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|closeScannerIfExhausted
argument_list|(
name|regionExhausted
argument_list|)
expr_stmt|;
break|break;
block|}
else|else
block|{
comment|// we have reached the max result size but we still can not find anything to return to the
comment|// user. Reset the maxResultSize and try again.
name|remainingResultSize
operator|=
name|maxScannerResultSize
expr_stmt|;
block|}
block|}
comment|// we are done with the current region
if|if
condition|(
name|regionExhausted
condition|)
block|{
if|if
condition|(
operator|!
name|moveToNextRegion
argument_list|()
condition|)
block|{
name|closed
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
specifier|protected
name|void
name|addEstimatedSize
parameter_list|(
name|long
name|estimatedHeapSizeOfResult
parameter_list|)
block|{
return|return;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getCacheCount
parameter_list|()
block|{
return|return
name|cache
operator|!=
literal|null
condition|?
name|cache
operator|.
name|size
argument_list|()
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|callable
operator|!=
literal|null
condition|)
block|{
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
try|try
block|{
name|call
argument_list|(
name|callable
argument_list|,
name|caller
argument_list|,
name|scannerTimeout
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownScannerException
name|e
parameter_list|)
block|{
comment|// We used to catch this error, interpret, and rethrow. However, we
comment|// have since decided that it's not nice for a scanner's close to
comment|// throw exceptions. Chances are it was just due to lease time out.
name|LOG
operator|.
name|debug
argument_list|(
literal|"scanner failed to close"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|/* An exception other than UnknownScanner is unexpected. */
name|LOG
operator|.
name|warn
argument_list|(
literal|"scanner failed to close."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|callable
operator|=
literal|null
expr_stmt|;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|renewLease
parameter_list|()
block|{
if|if
condition|(
name|callable
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// do not return any rows, do not advance the scanner
name|callable
operator|.
name|setRenew
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callable
argument_list|,
name|this
operator|.
name|scannerTimeout
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"scanner failed to renew lease"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
finally|finally
block|{
name|callable
operator|.
name|setRenew
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|initCache
parameter_list|()
block|{
name|initSyncCache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|nextWithSyncCache
argument_list|()
return|;
block|}
block|}
end_class

end_unit

