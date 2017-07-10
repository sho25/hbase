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
name|retries2Attempts
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
name|lang
operator|.
name|reflect
operator|.
name|UndeclaredThrowableException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|CallQueueTooBigException
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
name|PreemptiveFastFailException
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|ExceptionUtil
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * Runs an rpc'ing {@link RetryingCallable}. Sets into rpc client  * threadlocal outstanding timeouts as so we don't persist too much.  * Dynamic rather than static so can set the generic appropriately.  *  * This object has a state. It should not be used by in parallel by different threads.  * Reusing it is possible however, even between multiple threads. However, the user will  *  have to manage the synchronization on its side: there is no synchronization inside the class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RpcRetryingCallerImpl
parameter_list|<
name|T
parameter_list|>
implements|implements
name|RpcRetryingCaller
argument_list|<
name|T
argument_list|>
block|{
comment|// LOG is being used in TestMultiRowRangeFilter, hence leaving it public
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RpcRetryingCallerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** How many retries are allowed before we start to log */
specifier|private
specifier|final
name|int
name|startLogErrorsCnt
decl_stmt|;
specifier|private
specifier|final
name|long
name|pause
decl_stmt|;
specifier|private
specifier|final
name|long
name|pauseForCQTBE
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxAttempts
decl_stmt|;
comment|// how many times to try
specifier|private
specifier|final
name|int
name|rpcTimeout
decl_stmt|;
comment|// timeout for each rpc request
specifier|private
specifier|final
name|AtomicBoolean
name|cancelled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RetryingCallerInterceptor
name|interceptor
decl_stmt|;
specifier|private
specifier|final
name|RetryingCallerInterceptorContext
name|context
decl_stmt|;
specifier|private
specifier|final
name|RetryingTimeTracker
name|tracker
decl_stmt|;
specifier|public
name|RpcRetryingCallerImpl
parameter_list|(
name|long
name|pause
parameter_list|,
name|long
name|pauseForCQTBE
parameter_list|,
name|int
name|retries
parameter_list|,
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
argument_list|(
name|pause
argument_list|,
name|pauseForCQTBE
argument_list|,
name|retries
argument_list|,
name|RetryingCallerInterceptorFactory
operator|.
name|NO_OP_INTERCEPTOR
argument_list|,
name|startLogErrorsCnt
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RpcRetryingCallerImpl
parameter_list|(
name|long
name|pause
parameter_list|,
name|long
name|pauseForCQTBE
parameter_list|,
name|int
name|retries
parameter_list|,
name|RetryingCallerInterceptor
name|interceptor
parameter_list|,
name|int
name|startLogErrorsCnt
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
block|{
name|this
operator|.
name|pause
operator|=
name|pause
expr_stmt|;
name|this
operator|.
name|pauseForCQTBE
operator|=
name|pauseForCQTBE
expr_stmt|;
name|this
operator|.
name|maxAttempts
operator|=
name|retries2Attempts
argument_list|(
name|retries
argument_list|)
expr_stmt|;
name|this
operator|.
name|interceptor
operator|=
name|interceptor
expr_stmt|;
name|context
operator|=
name|interceptor
operator|.
name|createEmptyContext
argument_list|()
expr_stmt|;
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
name|this
operator|.
name|tracker
operator|=
operator|new
name|RetryingTimeTracker
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcTimeout
operator|=
name|rpcTimeout
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|()
block|{
name|cancelled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|cancelled
init|)
block|{
name|cancelled
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|T
name|callWithRetries
parameter_list|(
name|RetryingCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
block|{
name|List
argument_list|<
name|RetriesExhaustedException
operator|.
name|ThrowableWithExtraContext
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|tracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|tries
init|=
literal|0
init|;
condition|;
name|tries
operator|++
control|)
block|{
name|long
name|expectedSleep
decl_stmt|;
try|try
block|{
comment|// bad cache entries are cleared in the call to RetryingCallable#throwable() in catch block
name|callable
operator|.
name|prepare
argument_list|(
name|tries
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|interceptor
operator|.
name|intercept
argument_list|(
name|context
operator|.
name|prepare
argument_list|(
name|callable
argument_list|,
name|tries
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|callable
operator|.
name|call
argument_list|(
name|getTimeout
argument_list|(
name|callTimeout
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|PreemptiveFastFailException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|Throwable
name|e
init|=
name|t
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|ExceptionUtil
operator|.
name|rethrowIfInterrupt
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|t
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
comment|// Fail fast
throw|throw
operator|(
name|DoNotRetryIOException
operator|)
name|cause
throw|;
block|}
comment|// translateException throws exception when should not retry: i.e. when request is bad.
name|interceptor
operator|.
name|handleFailure
argument_list|(
name|context
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|t
operator|=
name|translateException
argument_list|(
name|t
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
name|info
argument_list|(
literal|"Call exception, tries="
operator|+
name|tries
operator|+
literal|", maxAttempts="
operator|+
name|maxAttempts
operator|+
literal|", started="
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|tracker
operator|.
name|getStartTime
argument_list|()
operator|)
operator|+
literal|" ms ago, "
operator|+
literal|"cancelled="
operator|+
name|cancelled
operator|.
name|get
argument_list|()
operator|+
literal|", msg="
operator|+
name|t
operator|.
name|getMessage
argument_list|()
operator|+
literal|" "
operator|+
name|callable
operator|.
name|getExceptionMessageAdditionalDetail
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|callable
operator|.
name|throwable
argument_list|(
name|t
argument_list|,
name|maxAttempts
operator|!=
literal|1
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
name|t
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|toString
argument_list|()
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
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|RetriesExhaustedException
argument_list|(
name|tries
argument_list|,
name|exceptions
argument_list|)
throw|;
block|}
comment|// If the server is dead, we need to wait a little before retrying, to give
comment|// a chance to the regions to be moved
comment|// get right pause time, start by RETRY_BACKOFF[0] * pauseBase, where pauseBase might be
comment|// special when encountering CallQueueTooBigException, see #HBASE-17114
name|long
name|pauseBase
init|=
operator|(
name|t
operator|instanceof
name|CallQueueTooBigException
operator|)
condition|?
name|pauseForCQTBE
else|:
name|pause
decl_stmt|;
name|expectedSleep
operator|=
name|callable
operator|.
name|sleep
argument_list|(
name|pauseBase
argument_list|,
name|tries
argument_list|)
expr_stmt|;
comment|// If, after the planned sleep, there won't be enough time left, we stop now.
name|long
name|duration
init|=
name|singleCallDuration
argument_list|(
name|expectedSleep
argument_list|)
decl_stmt|;
if|if
condition|(
name|duration
operator|>
name|callTimeout
condition|)
block|{
name|String
name|msg
init|=
literal|"callTimeout="
operator|+
name|callTimeout
operator|+
literal|", callDuration="
operator|+
name|duration
operator|+
literal|": "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
operator|+
literal|" "
operator|+
name|callable
operator|.
name|getExceptionMessageAdditionalDetail
argument_list|()
decl_stmt|;
throw|throw
call|(
name|SocketTimeoutException
call|)
argument_list|(
operator|new
name|SocketTimeoutException
argument_list|(
name|msg
argument_list|)
operator|.
name|initCause
argument_list|(
name|t
argument_list|)
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|interceptor
operator|.
name|updateFailureInfo
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|expectedSleep
operator|>
literal|0
condition|)
block|{
synchronized|synchronized
init|(
name|cancelled
init|)
block|{
if|if
condition|(
name|cancelled
operator|.
name|get
argument_list|()
condition|)
return|return
literal|null
return|;
name|cancelled
operator|.
name|wait
argument_list|(
name|expectedSleep
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cancelled
operator|.
name|get
argument_list|()
condition|)
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Interrupted after "
operator|+
name|tries
operator|+
literal|" tries while maxAttempts="
operator|+
name|maxAttempts
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * @return Calculate how long a single call took    */
specifier|private
name|long
name|singleCallDuration
parameter_list|(
specifier|final
name|long
name|expectedSleep
parameter_list|)
block|{
return|return
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|tracker
operator|.
name|getStartTime
argument_list|()
operator|)
operator|+
name|expectedSleep
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|callWithoutRetries
parameter_list|(
name|RetryingCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|int
name|callTimeout
parameter_list|)
throws|throws
name|IOException
throws|,
name|RuntimeException
block|{
comment|// The code of this method should be shared with withRetries.
try|try
block|{
name|callable
operator|.
name|prepare
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|callable
operator|.
name|call
argument_list|(
name|callTimeout
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|Throwable
name|t2
init|=
name|translateException
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|ExceptionUtil
operator|.
name|rethrowIfInterrupt
argument_list|(
name|t2
argument_list|)
expr_stmt|;
comment|// It would be nice to clear the location cache here.
if|if
condition|(
name|t2
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|t2
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|t2
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Get the good or the remote exception if any, throws the DoNotRetryIOException.    * @param t the throwable to analyze    * @return the translated exception, if it's not a DoNotRetryIOException    * @throws DoNotRetryIOException - if we find it, we throw it instead of translating.    */
specifier|static
name|Throwable
name|translateException
parameter_list|(
name|Throwable
name|t
parameter_list|)
throws|throws
name|DoNotRetryIOException
block|{
if|if
condition|(
name|t
operator|instanceof
name|UndeclaredThrowableException
condition|)
block|{
if|if
condition|(
name|t
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|t
operator|=
name|t
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|t
operator|instanceof
name|RemoteException
condition|)
block|{
name|t
operator|=
operator|(
operator|(
name|RemoteException
operator|)
name|t
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|LinkageError
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|ServiceException
condition|)
block|{
name|ServiceException
name|se
init|=
operator|(
name|ServiceException
operator|)
name|t
decl_stmt|;
name|Throwable
name|cause
init|=
name|se
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
throw|throw
operator|(
name|DoNotRetryIOException
operator|)
name|cause
throw|;
block|}
comment|// Don't let ServiceException out; its rpc specific.
name|t
operator|=
name|cause
expr_stmt|;
comment|// t could be a RemoteException so go around again.
name|translateException
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
throw|throw
operator|(
name|DoNotRetryIOException
operator|)
name|t
throw|;
block|}
return|return
name|t
return|;
block|}
specifier|private
name|int
name|getTimeout
parameter_list|(
name|int
name|callTimeout
parameter_list|)
block|{
name|int
name|timeout
init|=
name|tracker
operator|.
name|getRemainingTime
argument_list|(
name|callTimeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeout
operator|<=
literal|0
operator|||
name|rpcTimeout
operator|>
literal|0
operator|&&
name|rpcTimeout
operator|<
name|timeout
condition|)
block|{
name|timeout
operator|=
name|rpcTimeout
expr_stmt|;
block|}
return|return
name|timeout
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"RpcRetryingCaller{"
operator|+
literal|"globalStartTime="
operator|+
name|tracker
operator|.
name|getStartTime
argument_list|()
operator|+
literal|", pause="
operator|+
name|pause
operator|+
literal|", maxAttempts="
operator|+
name|maxAttempts
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit

