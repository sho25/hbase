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
name|getPauseTime
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
name|noMoreResultsForReverseScan
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
name|noMoreResultsForScan
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
name|resetController
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
name|retries2Attempts
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
name|translateException
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|HashedWheelTimer
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
name|util
operator|.
name|ArrayList
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
name|CompletableFuture
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
name|TimeUnit
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
name|HRegionLocation
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
name|HBaseRpcController
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
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|ResponseConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
operator|.
name|Interface
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanResponse
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
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|/**  * Retry caller for scanning a region.  *<p>  * We will modify the {@link Scan} object passed in directly. The upper layer should store the  * reference of this object and use it to open new single region scanners.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncScanSingleRegionRpcRetryingCaller
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
name|AsyncScanSingleRegionRpcRetryingCaller
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HashedWheelTimer
name|retryTimer
decl_stmt|;
specifier|private
specifier|final
name|Scan
name|scan
decl_stmt|;
specifier|private
specifier|final
name|long
name|scannerId
decl_stmt|;
specifier|private
specifier|final
name|ScanResultCache
name|resultCache
decl_stmt|;
specifier|private
specifier|final
name|RawScanResultConsumer
name|consumer
decl_stmt|;
specifier|private
specifier|final
name|ClientService
operator|.
name|Interface
name|stub
decl_stmt|;
specifier|private
specifier|final
name|HRegionLocation
name|loc
decl_stmt|;
specifier|private
specifier|final
name|long
name|pauseNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxAttempts
decl_stmt|;
specifier|private
specifier|final
name|long
name|scanTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|rpcTimeoutNs
decl_stmt|;
specifier|private
specifier|final
name|int
name|startLogErrorsCnt
decl_stmt|;
specifier|private
specifier|final
name|Runnable
name|completeWhenNoMoreResultsInRegion
decl_stmt|;
specifier|private
specifier|final
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|future
decl_stmt|;
specifier|private
specifier|final
name|HBaseRpcController
name|controller
decl_stmt|;
specifier|private
name|byte
index|[]
name|nextStartRowWhenError
decl_stmt|;
specifier|private
name|boolean
name|includeNextStartRowWhenError
decl_stmt|;
specifier|private
name|long
name|nextCallStartNs
decl_stmt|;
specifier|private
name|int
name|tries
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
argument_list|>
name|exceptions
decl_stmt|;
specifier|private
name|long
name|nextCallSeq
init|=
operator|-
literal|1L
decl_stmt|;
specifier|public
name|AsyncScanSingleRegionRpcRetryingCaller
parameter_list|(
name|HashedWheelTimer
name|retryTimer
parameter_list|,
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|long
name|scannerId
parameter_list|,
name|ScanResultCache
name|resultCache
parameter_list|,
name|RawScanResultConsumer
name|consumer
parameter_list|,
name|Interface
name|stub
parameter_list|,
name|HRegionLocation
name|loc
parameter_list|,
name|long
name|pauseNs
parameter_list|,
name|int
name|maxAttempts
parameter_list|,
name|long
name|scanTimeoutNs
parameter_list|,
name|long
name|rpcTimeoutNs
parameter_list|,
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|retryTimer
operator|=
name|retryTimer
expr_stmt|;
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|scannerId
operator|=
name|scannerId
expr_stmt|;
name|this
operator|.
name|resultCache
operator|=
name|resultCache
expr_stmt|;
name|this
operator|.
name|consumer
operator|=
name|consumer
expr_stmt|;
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
name|this
operator|.
name|loc
operator|=
name|loc
expr_stmt|;
name|this
operator|.
name|pauseNs
operator|=
name|pauseNs
expr_stmt|;
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
name|this
operator|.
name|scanTimeoutNs
operator|=
name|scanTimeoutNs
expr_stmt|;
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|rpcTimeoutNs
expr_stmt|;
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
if|if
condition|(
name|scan
operator|.
name|isReversed
argument_list|()
condition|)
block|{
name|completeWhenNoMoreResultsInRegion
operator|=
name|this
operator|::
name|completeReversedWhenNoMoreResultsInRegion
expr_stmt|;
block|}
else|else
block|{
name|completeWhenNoMoreResultsInRegion
operator|=
name|this
operator|::
name|completeWhenNoMoreResultsInRegion
expr_stmt|;
block|}
name|this
operator|.
name|future
operator|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|conn
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
expr_stmt|;
name|this
operator|.
name|exceptions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|private
name|long
name|elapsedMs
parameter_list|()
block|{
return|return
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|nextCallStartNs
argument_list|)
return|;
block|}
specifier|private
name|void
name|closeScanner
parameter_list|()
block|{
name|resetController
argument_list|(
name|controller
argument_list|,
name|rpcTimeoutNs
argument_list|)
expr_stmt|;
name|ScanRequest
name|req
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|this
operator|.
name|scannerId
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|stub
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|req
argument_list|,
name|resp
lambda|->
block|{
if|if
condition|(
name|controller
operator|.
name|failed
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Call to "
operator|+
name|loc
operator|.
name|getServerName
argument_list|()
operator|+
literal|" for closing scanner id = "
operator|+
name|scannerId
operator|+
literal|" for "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" of "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|+
literal|" failed, ignore, probably already closed"
argument_list|,
name|controller
operator|.
name|getFailed
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
name|completeExceptionally
parameter_list|(
name|boolean
name|closeScanner
parameter_list|)
block|{
name|resultCache
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|closeScanner
condition|)
block|{
name|closeScanner
argument_list|()
expr_stmt|;
block|}
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RetriesExhaustedException
argument_list|(
name|tries
operator|-
literal|1
argument_list|,
name|exceptions
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|completeNoMoreResults
parameter_list|()
block|{
name|future
operator|.
name|complete
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|completeWithNextStartRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|inclusive
parameter_list|)
block|{
name|scan
operator|.
name|withStartRow
argument_list|(
name|row
argument_list|,
name|inclusive
argument_list|)
expr_stmt|;
name|future
operator|.
name|complete
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|completeWhenError
parameter_list|(
name|boolean
name|closeScanner
parameter_list|)
block|{
name|resultCache
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|closeScanner
condition|)
block|{
name|closeScanner
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|nextStartRowWhenError
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|withStartRow
argument_list|(
name|nextStartRowWhenError
argument_list|,
name|includeNextStartRowWhenError
argument_list|)
expr_stmt|;
block|}
name|future
operator|.
name|complete
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|onError
parameter_list|(
name|Throwable
name|error
parameter_list|)
block|{
name|error
operator|=
name|translateException
argument_list|(
name|error
argument_list|)
expr_stmt|;
if|if
condition|(
name|tries
operator|>
name|startLogErrorsCnt
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Call to "
operator|+
name|loc
operator|.
name|getServerName
argument_list|()
operator|+
literal|" for scanner id = "
operator|+
name|scannerId
operator|+
literal|" for "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" of "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|+
literal|" failed, , tries = "
operator|+
name|tries
operator|+
literal|", maxAttempts = "
operator|+
name|maxAttempts
operator|+
literal|", timeout = "
operator|+
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|scanTimeoutNs
argument_list|)
operator|+
literal|" ms, time elapsed = "
operator|+
name|elapsedMs
argument_list|()
operator|+
literal|" ms"
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
name|boolean
name|scannerClosed
init|=
name|error
operator|instanceof
name|UnknownScannerException
operator|||
name|error
operator|instanceof
name|NotServingRegionException
operator|||
name|error
operator|instanceof
name|RegionServerStoppedException
decl_stmt|;
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
name|qt
init|=
operator|new
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
argument_list|(
name|error
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|exceptions
operator|.
name|add
argument_list|(
name|qt
argument_list|)
expr_stmt|;
if|if
condition|(
name|tries
operator|>=
name|maxAttempts
condition|)
block|{
name|completeExceptionally
argument_list|(
operator|!
name|scannerClosed
argument_list|)
expr_stmt|;
return|return;
block|}
name|long
name|delayNs
decl_stmt|;
if|if
condition|(
name|scanTimeoutNs
operator|>
literal|0
condition|)
block|{
name|long
name|maxDelayNs
init|=
name|scanTimeoutNs
operator|-
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|nextCallStartNs
operator|)
decl_stmt|;
if|if
condition|(
name|maxDelayNs
operator|<=
literal|0
condition|)
block|{
name|completeExceptionally
argument_list|(
operator|!
name|scannerClosed
argument_list|)
expr_stmt|;
return|return;
block|}
name|delayNs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxDelayNs
argument_list|,
name|getPauseTime
argument_list|(
name|pauseNs
argument_list|,
name|tries
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delayNs
operator|=
name|getPauseTime
argument_list|(
name|pauseNs
argument_list|,
name|tries
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scannerClosed
condition|)
block|{
name|completeWhenError
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|error
operator|instanceof
name|OutOfOrderScannerNextException
operator|||
name|error
operator|instanceof
name|ScannerResetException
condition|)
block|{
name|completeWhenError
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|error
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
name|completeExceptionally
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
name|tries
operator|++
expr_stmt|;
name|retryTimer
operator|.
name|newTimeout
argument_list|(
name|t
lambda|->
name|call
argument_list|()
argument_list|,
name|delayNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updateNextStartRowWhenError
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|nextStartRowWhenError
operator|=
name|result
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|includeNextStartRowWhenError
operator|=
name|scan
operator|.
name|getBatch
argument_list|()
operator|>
literal|0
operator|||
name|result
operator|.
name|isPartial
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|completeWhenNoMoreResultsInRegion
parameter_list|()
block|{
if|if
condition|(
name|noMoreResultsForScan
argument_list|(
name|scan
argument_list|,
name|loc
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|completeNoMoreResults
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|completeWithNextStartRow
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|completeReversedWhenNoMoreResultsInRegion
parameter_list|()
block|{
if|if
condition|(
name|noMoreResultsForReverseScan
argument_list|(
name|scan
argument_list|,
name|loc
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|completeNoMoreResults
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|completeWithNextStartRow
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|onComplete
parameter_list|(
name|ScanResponse
name|resp
parameter_list|)
block|{
if|if
condition|(
name|controller
operator|.
name|failed
argument_list|()
condition|)
block|{
name|onError
argument_list|(
name|controller
operator|.
name|getFailed
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|isHeartbeatMessage
init|=
name|resp
operator|.
name|hasHeartbeatMessage
argument_list|()
operator|&&
name|resp
operator|.
name|getHeartbeatMessage
argument_list|()
decl_stmt|;
name|Result
index|[]
name|results
decl_stmt|;
try|try
block|{
name|results
operator|=
name|resultCache
operator|.
name|addAndGet
argument_list|(
name|Optional
operator|.
name|ofNullable
argument_list|(
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|controller
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|resp
argument_list|)
argument_list|)
operator|.
name|orElse
argument_list|(
name|ScanResultCache
operator|.
name|EMPTY_RESULT_ARRAY
argument_list|)
argument_list|,
name|isHeartbeatMessage
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// We can not retry here. The server has responded normally and the call sequence has been
comment|// increased so a new scan with the same call sequence will cause an
comment|// OutOfOrderScannerNextException. Let the upper layer open a new scanner.
name|LOG
operator|.
name|warn
argument_list|(
literal|"decode scan response failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|completeWhenError
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|stopByUser
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// if we have nothing to return then this must be a heartbeat message.
name|stopByUser
operator|=
operator|!
name|consumer
operator|.
name|onHeartbeat
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|updateNextStartRowWhenError
argument_list|(
name|results
index|[
name|results
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|stopByUser
operator|=
operator|!
name|consumer
operator|.
name|onNext
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|resp
operator|.
name|hasMoreResults
argument_list|()
operator|&&
operator|!
name|resp
operator|.
name|getMoreResults
argument_list|()
condition|)
block|{
comment|// RS tells us there is no more data for the whole scan
name|completeNoMoreResults
argument_list|()
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|stopByUser
condition|)
block|{
if|if
condition|(
name|resp
operator|.
name|getMoreResultsInRegion
argument_list|()
condition|)
block|{
comment|// we have more results in region but user request to stop the scan, so we need to close the
comment|// scanner explicitly.
name|closeScanner
argument_list|()
expr_stmt|;
block|}
name|completeNoMoreResults
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// as in 2.0 this value will always be set
if|if
condition|(
operator|!
name|resp
operator|.
name|getMoreResultsInRegion
argument_list|()
condition|)
block|{
name|completeWhenNoMoreResultsInRegion
operator|.
name|run
argument_list|()
expr_stmt|;
return|return;
block|}
name|next
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|call
parameter_list|()
block|{
name|resetController
argument_list|(
name|controller
argument_list|,
name|rpcTimeoutNs
argument_list|)
expr_stmt|;
name|ScanRequest
name|req
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|scannerId
argument_list|,
name|scan
operator|.
name|getCaching
argument_list|()
argument_list|,
literal|false
argument_list|,
name|nextCallSeq
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|stub
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|req
argument_list|,
name|this
operator|::
name|onComplete
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|next
parameter_list|()
block|{
name|nextCallSeq
operator|++
expr_stmt|;
name|tries
operator|=
literal|0
expr_stmt|;
name|exceptions
operator|.
name|clear
argument_list|()
expr_stmt|;
name|nextCallStartNs
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|call
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return {@code true} if we should continue, otherwise {@code false}.    */
specifier|public
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|start
parameter_list|()
block|{
name|next
argument_list|()
expr_stmt|;
return|return
name|future
return|;
block|}
block|}
end_class

end_unit

