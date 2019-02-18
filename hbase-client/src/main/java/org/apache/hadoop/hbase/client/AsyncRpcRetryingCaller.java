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
name|SLEEP_DELTA_NS
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
name|translateException
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
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
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
name|TableNotEnabledException
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
name|TableNotFoundException
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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|FutureUtils
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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|Timer
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AsyncRpcRetryingCaller
parameter_list|<
name|T
parameter_list|>
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
name|AsyncRpcRetryingCaller
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Timer
name|retryTimer
decl_stmt|;
specifier|private
specifier|final
name|int
name|priority
decl_stmt|;
specifier|private
specifier|final
name|long
name|startNs
decl_stmt|;
specifier|private
specifier|final
name|long
name|pauseNs
decl_stmt|;
specifier|private
name|int
name|tries
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxAttempts
decl_stmt|;
specifier|private
specifier|final
name|int
name|startLogErrorsCnt
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
specifier|final
name|long
name|rpcTimeoutNs
decl_stmt|;
specifier|protected
specifier|final
name|long
name|operationTimeoutNs
decl_stmt|;
specifier|protected
specifier|final
name|AsyncConnectionImpl
name|conn
decl_stmt|;
specifier|protected
specifier|final
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
decl_stmt|;
specifier|protected
specifier|final
name|HBaseRpcController
name|controller
decl_stmt|;
specifier|public
name|AsyncRpcRetryingCaller
parameter_list|(
name|Timer
name|retryTimer
parameter_list|,
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|int
name|priority
parameter_list|,
name|long
name|pauseNs
parameter_list|,
name|int
name|maxAttempts
parameter_list|,
name|long
name|operationTimeoutNs
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
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|priority
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
name|operationTimeoutNs
operator|=
name|operationTimeoutNs
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
name|controller
operator|.
name|setPriority
argument_list|(
name|priority
argument_list|)
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
name|this
operator|.
name|startNs
operator|=
name|System
operator|.
name|nanoTime
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
name|startNs
argument_list|)
return|;
block|}
specifier|protected
specifier|final
name|long
name|remainingTimeNs
parameter_list|()
block|{
return|return
name|operationTimeoutNs
operator|-
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startNs
operator|)
return|;
block|}
specifier|protected
specifier|final
name|void
name|completeExceptionally
parameter_list|()
block|{
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
specifier|protected
specifier|final
name|void
name|resetCallTimeout
parameter_list|()
block|{
name|long
name|callTimeoutNs
decl_stmt|;
if|if
condition|(
name|operationTimeoutNs
operator|>
literal|0
condition|)
block|{
name|callTimeoutNs
operator|=
name|remainingTimeNs
argument_list|()
expr_stmt|;
if|if
condition|(
name|callTimeoutNs
operator|<=
literal|0
condition|)
block|{
name|completeExceptionally
argument_list|()
expr_stmt|;
return|return;
block|}
name|callTimeoutNs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|callTimeoutNs
argument_list|,
name|rpcTimeoutNs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|callTimeoutNs
operator|=
name|rpcTimeoutNs
expr_stmt|;
block|}
name|resetController
argument_list|(
name|controller
argument_list|,
name|callTimeoutNs
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tryScheduleRetry
parameter_list|(
name|Throwable
name|error
parameter_list|,
name|Consumer
argument_list|<
name|Throwable
argument_list|>
name|updateCachedLocation
parameter_list|)
block|{
name|long
name|delayNs
decl_stmt|;
if|if
condition|(
name|operationTimeoutNs
operator|>
literal|0
condition|)
block|{
name|long
name|maxDelayNs
init|=
name|remainingTimeNs
argument_list|()
operator|-
name|SLEEP_DELTA_NS
decl_stmt|;
if|if
condition|(
name|maxDelayNs
operator|<=
literal|0
condition|)
block|{
name|completeExceptionally
argument_list|()
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
name|tries
operator|++
expr_stmt|;
name|retryTimer
operator|.
name|newTimeout
argument_list|(
name|t
lambda|->
name|doCall
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
specifier|protected
name|Optional
argument_list|<
name|TableName
argument_list|>
name|getTableName
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
specifier|protected
specifier|final
name|void
name|onError
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|Supplier
argument_list|<
name|String
argument_list|>
name|errMsg
parameter_list|,
name|Consumer
argument_list|<
name|Throwable
argument_list|>
name|updateCachedLocation
parameter_list|)
block|{
if|if
condition|(
name|future
operator|.
name|isDone
argument_list|()
condition|)
block|{
comment|// Give up if the future is already done, this is possible if user has already canceled the
comment|// future. And for timeline consistent read, we will also cancel some requests if we have
comment|// already get one of the responses.
name|LOG
operator|.
name|debug
argument_list|(
literal|"The future is already done, canceled={}, give up retrying"
argument_list|,
name|future
operator|.
name|isCancelled
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|Throwable
name|error
init|=
name|translateException
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|error
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
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
name|errMsg
operator|.
name|get
argument_list|()
operator|+
literal|", tries = "
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
name|operationTimeoutNs
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
name|updateCachedLocation
operator|.
name|accept
argument_list|(
name|error
argument_list|)
expr_stmt|;
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
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// check whether the table has been disabled, notice that the check will introduce a request to
comment|// meta, so here we only check for disabled for some specific exception types.
if|if
condition|(
name|error
operator|instanceof
name|NotServingRegionException
operator|||
name|error
operator|instanceof
name|RegionOfflineException
condition|)
block|{
name|Optional
argument_list|<
name|TableName
argument_list|>
name|tableName
init|=
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableName
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|FutureUtils
operator|.
name|addListener
argument_list|(
name|conn
operator|.
name|getAdmin
argument_list|()
operator|.
name|isTableDisabled
argument_list|(
name|tableName
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
parameter_list|(
name|disabled
parameter_list|,
name|e
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|TableNotFoundException
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// failed to test whether the table is disabled, not a big deal, continue retrying
name|tryScheduleRetry
argument_list|(
name|error
argument_list|,
name|updateCachedLocation
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
name|disabled
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|TableNotEnabledException
argument_list|(
name|tableName
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tryScheduleRetry
argument_list|(
name|error
argument_list|,
name|updateCachedLocation
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|tryScheduleRetry
argument_list|(
name|error
argument_list|,
name|updateCachedLocation
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|doCall
parameter_list|()
function_decl|;
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|call
parameter_list|()
block|{
name|doCall
argument_list|()
expr_stmt|;
return|return
name|future
return|;
block|}
block|}
end_class

end_unit

