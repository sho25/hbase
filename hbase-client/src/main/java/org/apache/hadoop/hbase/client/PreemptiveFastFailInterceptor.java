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
name|util
operator|.
name|CollectionUtils
operator|.
name|computeIfAbsent
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
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|Map
operator|.
name|Entry
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
name|ServerName
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
name|exceptions
operator|.
name|ClientExceptionsUtil
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
name|ipc
operator|.
name|CallTimeoutException
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * The concrete {@link RetryingCallerInterceptor} class that implements the preemptive fast fail  * feature.  *<p>  * The motivation is as follows : In case where a large number of clients try and talk to a  * particular region server in hbase, if the region server goes down due to network problems, we  * might end up in a scenario where the clients would go into a state where they all start to retry.  * This behavior will set off many of the threads in pretty much the same path and they all would be  * sleeping giving rise to a state where the client either needs to create more threads to send new  * requests to other hbase machines or block because the client cannot create anymore threads.  *<p>  * In most cases the clients might prefer to have a bound on the number of threads that are created  * in order to send requests to hbase. This would mostly result in the client thread starvation.  *<p>  * To circumvent this problem, the approach that is being taken here under is to let 1 of the many  * threads who are trying to contact the regionserver with connection problems and let the other  * threads get a {@link PreemptiveFastFailException} so that they can move on and take other  * requests.  *<p>  * This would give the client more flexibility on the kind of action he would want to take in cases  * where the regionserver is down. He can either discard the requests and send a nack upstream  * faster or have an application level retry or buffer the requests up so as to send them down to  * hbase later.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|PreemptiveFastFailInterceptor
extends|extends
name|RetryingCallerInterceptor
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
name|PreemptiveFastFailInterceptor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// amount of time to wait before we consider a server to be in fast fail
comment|// mode
specifier|protected
specifier|final
name|long
name|fastFailThresholdMilliSec
decl_stmt|;
comment|// Keeps track of failures when we cannot talk to a server. Helps in
comment|// fast failing clients if the server is down for a long time.
specifier|protected
specifier|final
name|ConcurrentMap
argument_list|<
name|ServerName
argument_list|,
name|FailureInfo
argument_list|>
name|repeatedFailuresMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// We populate repeatedFailuresMap every time there is a failure. So, to
comment|// keep it from growing unbounded, we garbage collect the failure information
comment|// every cleanupInterval.
specifier|protected
specifier|final
name|long
name|failureMapCleanupIntervalMilliSec
decl_stmt|;
specifier|protected
specifier|volatile
name|long
name|lastFailureMapCleanupTimeMilliSec
decl_stmt|;
comment|// clear failure Info. Used to clean out all entries.
comment|// A safety valve, in case the client does not exit the
comment|// fast fail mode for any reason.
specifier|private
name|long
name|fastFailClearingTimeMilliSec
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|MutableBoolean
argument_list|>
name|threadRetryingInFastFailMode
init|=
operator|new
name|ThreadLocal
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|PreemptiveFastFailInterceptor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|fastFailThresholdMilliSec
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_THREASHOLD_MS
argument_list|,
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_THREASHOLD_MS_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|failureMapCleanupIntervalMilliSec
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_CLEANUP_MS_DURATION_MS
argument_list|,
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_CLEANUP_DURATION_MS_DEFAULT
argument_list|)
expr_stmt|;
name|lastFailureMapCleanupTimeMilliSec
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|intercept
parameter_list|(
name|FastFailInterceptorContext
name|context
parameter_list|)
throws|throws
name|PreemptiveFastFailException
block|{
name|context
operator|.
name|setFailureInfo
argument_list|(
name|repeatedFailuresMap
operator|.
name|get
argument_list|(
name|context
operator|.
name|getServer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|inFastFailMode
argument_list|(
name|context
operator|.
name|getServer
argument_list|()
argument_list|)
operator|&&
operator|!
name|currentThreadInFastFailMode
argument_list|()
condition|)
block|{
comment|// In Fast-fail mode, all but one thread will fast fail. Check
comment|// if we are that one chosen thread.
name|context
operator|.
name|setRetryDespiteFastFailMode
argument_list|(
name|shouldRetryInspiteOfFastFail
argument_list|(
name|context
operator|.
name|getFailureInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|isRetryDespiteFastFailMode
argument_list|()
condition|)
block|{
comment|// we don't have to retry
name|LOG
operator|.
name|debug
argument_list|(
literal|"Throwing PFFE : "
operator|+
name|context
operator|.
name|getFailureInfo
argument_list|()
operator|+
literal|" tries : "
operator|+
name|context
operator|.
name|getTries
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|PreemptiveFastFailException
argument_list|(
name|context
operator|.
name|getFailureInfo
argument_list|()
operator|.
name|numConsecutiveFailures
operator|.
name|get
argument_list|()
argument_list|,
name|context
operator|.
name|getFailureInfo
argument_list|()
operator|.
name|timeOfFirstFailureMilliSec
argument_list|,
name|context
operator|.
name|getFailureInfo
argument_list|()
operator|.
name|timeOfLatestAttemptMilliSec
argument_list|,
name|context
operator|.
name|getServer
argument_list|()
argument_list|,
name|context
operator|.
name|getGuaranteedClientSideOnly
argument_list|()
operator|.
name|isTrue
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|context
operator|.
name|setDidTry
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|handleFailure
parameter_list|(
name|FastFailInterceptorContext
name|context
parameter_list|,
name|Throwable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
name|handleThrowable
argument_list|(
name|t
argument_list|,
name|context
operator|.
name|getServer
argument_list|()
argument_list|,
name|context
operator|.
name|getCouldNotCommunicateWithServer
argument_list|()
argument_list|,
name|context
operator|.
name|getGuaranteedClientSideOnly
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateFailureInfo
parameter_list|(
name|FastFailInterceptorContext
name|context
parameter_list|)
block|{
name|updateFailureInfoForServer
argument_list|(
name|context
operator|.
name|getServer
argument_list|()
argument_list|,
name|context
operator|.
name|getFailureInfo
argument_list|()
argument_list|,
name|context
operator|.
name|didTry
argument_list|()
argument_list|,
name|context
operator|.
name|getCouldNotCommunicateWithServer
argument_list|()
operator|.
name|booleanValue
argument_list|()
argument_list|,
name|context
operator|.
name|isRetryDespiteFastFailMode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Handles failures encountered when communicating with a server.    *    * Updates the FailureInfo in repeatedFailuresMap to reflect the failure.    * Throws RepeatedConnectException if the client is in Fast fail mode.    *    * @param serverName    * @param t    *          - the throwable to be handled.    * @throws PreemptiveFastFailException    */
annotation|@
name|VisibleForTesting
specifier|protected
name|void
name|handleFailureToServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|serverName
operator|==
literal|null
operator|||
name|t
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|FailureInfo
name|fInfo
init|=
name|computeIfAbsent
argument_list|(
name|repeatedFailuresMap
argument_list|,
name|serverName
argument_list|,
parameter_list|()
lambda|->
operator|new
name|FailureInfo
argument_list|(
name|currentTime
argument_list|)
argument_list|)
decl_stmt|;
name|fInfo
operator|.
name|timeOfLatestAttemptMilliSec
operator|=
name|currentTime
expr_stmt|;
name|fInfo
operator|.
name|numConsecutiveFailures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|handleThrowable
parameter_list|(
name|Throwable
name|t1
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|MutableBoolean
name|couldNotCommunicateWithServer
parameter_list|,
name|MutableBoolean
name|guaranteedClientSideOnly
parameter_list|)
throws|throws
name|IOException
block|{
name|Throwable
name|t2
init|=
name|ClientExceptionsUtil
operator|.
name|translatePFFE
argument_list|(
name|t1
argument_list|)
decl_stmt|;
name|boolean
name|isLocalException
init|=
operator|!
operator|(
name|t2
operator|instanceof
name|RemoteException
operator|)
decl_stmt|;
if|if
condition|(
operator|(
name|isLocalException
operator|&&
name|ClientExceptionsUtil
operator|.
name|isConnectionException
argument_list|(
name|t2
argument_list|)
operator|)
condition|)
block|{
name|couldNotCommunicateWithServer
operator|.
name|setValue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|guaranteedClientSideOnly
operator|.
name|setValue
argument_list|(
operator|!
operator|(
name|t2
operator|instanceof
name|CallTimeoutException
operator|)
argument_list|)
expr_stmt|;
name|handleFailureToServer
argument_list|(
name|serverName
argument_list|,
name|t2
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Occasionally cleans up unused information in repeatedFailuresMap.    *    * repeatedFailuresMap stores the failure information for all remote hosts    * that had failures. In order to avoid these from growing indefinitely,    * occassionallyCleanupFailureInformation() will clear these up once every    * cleanupInterval ms.    */
specifier|protected
name|void
name|occasionallyCleanupFailureInformation
parameter_list|()
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|now
operator|>
name|lastFailureMapCleanupTimeMilliSec
operator|+
name|failureMapCleanupIntervalMilliSec
operator|)
condition|)
return|return;
comment|// remove entries that haven't been attempted in a while
comment|// No synchronization needed. It is okay if multiple threads try to
comment|// remove the entry again and again from a concurrent hash map.
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|FailureInfo
argument_list|>
name|entry
range|:
name|repeatedFailuresMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|now
operator|>
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|timeOfLatestAttemptMilliSec
operator|+
name|failureMapCleanupIntervalMilliSec
condition|)
block|{
comment|// no recent failures
name|repeatedFailuresMap
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|now
operator|>
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|timeOfFirstFailureMilliSec
operator|+
name|this
operator|.
name|fastFailClearingTimeMilliSec
condition|)
block|{
comment|// been failing for a long
comment|// time
name|LOG
operator|.
name|error
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" been failing for a long time. clearing out."
operator|+
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|repeatedFailuresMap
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" failing "
argument_list|)
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Preemptive failure enabled for : "
operator|+
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|lastFailureMapCleanupTimeMilliSec
operator|=
name|now
expr_stmt|;
block|}
comment|/**    * Checks to see if we are in the Fast fail mode for requests to the server.    *    * If a client is unable to contact a server for more than    * fastFailThresholdMilliSec the client will get into fast fail mode.    *    * @param server    * @return true if the client is in fast fail mode for the server.    */
specifier|private
name|boolean
name|inFastFailMode
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
name|FailureInfo
name|fInfo
init|=
name|repeatedFailuresMap
operator|.
name|get
argument_list|(
name|server
argument_list|)
decl_stmt|;
comment|// if fInfo is null --> The server is considered good.
comment|// If the server is bad, wait long enough to believe that the server is
comment|// down.
return|return
operator|(
name|fInfo
operator|!=
literal|null
operator|&&
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|>
operator|(
name|fInfo
operator|.
name|timeOfFirstFailureMilliSec
operator|+
name|this
operator|.
name|fastFailThresholdMilliSec
operator|)
operator|)
return|;
block|}
comment|/**    * Checks to see if the current thread is already in FastFail mode for *some*    * server.    *    * @return true, if the thread is already in FF mode.    */
specifier|private
name|boolean
name|currentThreadInFastFailMode
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|threadRetryingInFastFailMode
operator|.
name|get
argument_list|()
operator|!=
literal|null
operator|&&
operator|(
name|this
operator|.
name|threadRetryingInFastFailMode
operator|.
name|get
argument_list|()
operator|.
name|booleanValue
argument_list|()
operator|==
literal|true
operator|)
operator|)
return|;
block|}
comment|/**    * Check to see if the client should try to connnect to the server, inspite of    * knowing that it is in the fast fail mode.    *    * The idea here is that we want just one client thread to be actively trying    * to reconnect, while all the other threads trying to reach the server will    * short circuit.    *    * @param fInfo    * @return true if the client should try to connect to the server.    */
specifier|protected
name|boolean
name|shouldRetryInspiteOfFastFail
parameter_list|(
name|FailureInfo
name|fInfo
parameter_list|)
block|{
comment|// We believe that the server is down, But, we want to have just one
comment|// client
comment|// actively trying to connect. If we are the chosen one, we will retry
comment|// and not throw an exception.
if|if
condition|(
name|fInfo
operator|!=
literal|null
operator|&&
name|fInfo
operator|.
name|exclusivelyRetringInspiteOfFastFail
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|MutableBoolean
name|threadAlreadyInFF
init|=
name|this
operator|.
name|threadRetryingInFastFailMode
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|threadAlreadyInFF
operator|==
literal|null
condition|)
block|{
name|threadAlreadyInFF
operator|=
operator|new
name|MutableBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|threadRetryingInFastFailMode
operator|.
name|set
argument_list|(
name|threadAlreadyInFF
argument_list|)
expr_stmt|;
block|}
name|threadAlreadyInFF
operator|.
name|setValue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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
comment|/**    *    * This function updates the Failure info for a particular server after the    * attempt to     *    * @param server    * @param fInfo    * @param couldNotCommunicate    * @param retryDespiteFastFailMode    */
specifier|private
name|void
name|updateFailureInfoForServer
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|FailureInfo
name|fInfo
parameter_list|,
name|boolean
name|didTry
parameter_list|,
name|boolean
name|couldNotCommunicate
parameter_list|,
name|boolean
name|retryDespiteFastFailMode
parameter_list|)
block|{
if|if
condition|(
name|server
operator|==
literal|null
operator|||
name|fInfo
operator|==
literal|null
operator|||
name|didTry
operator|==
literal|false
condition|)
return|return;
comment|// If we were able to connect to the server, reset the failure
comment|// information.
if|if
condition|(
name|couldNotCommunicate
operator|==
literal|false
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Clearing out PFFE for server "
operator|+
name|server
argument_list|)
expr_stmt|;
name|repeatedFailuresMap
operator|.
name|remove
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// update time of last attempt
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|fInfo
operator|.
name|timeOfLatestAttemptMilliSec
operator|=
name|currentTime
expr_stmt|;
comment|// Release the lock if we were retrying inspite of FastFail
if|if
condition|(
name|retryDespiteFastFailMode
condition|)
block|{
name|fInfo
operator|.
name|exclusivelyRetringInspiteOfFastFail
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|threadRetryingInFastFailMode
operator|.
name|get
argument_list|()
operator|.
name|setValue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|occasionallyCleanupFailureInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|intercept
parameter_list|(
name|RetryingCallerInterceptorContext
name|context
parameter_list|)
throws|throws
name|PreemptiveFastFailException
block|{
if|if
condition|(
name|context
operator|instanceof
name|FastFailInterceptorContext
condition|)
block|{
name|intercept
argument_list|(
operator|(
name|FastFailInterceptorContext
operator|)
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleFailure
parameter_list|(
name|RetryingCallerInterceptorContext
name|context
parameter_list|,
name|Throwable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|instanceof
name|FastFailInterceptorContext
condition|)
block|{
name|handleFailure
argument_list|(
operator|(
name|FastFailInterceptorContext
operator|)
name|context
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateFailureInfo
parameter_list|(
name|RetryingCallerInterceptorContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|instanceof
name|FastFailInterceptorContext
condition|)
block|{
name|updateFailureInfo
argument_list|(
operator|(
name|FastFailInterceptorContext
operator|)
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RetryingCallerInterceptorContext
name|createEmptyContext
parameter_list|()
block|{
return|return
operator|new
name|FastFailInterceptorContext
argument_list|()
return|;
block|}
specifier|protected
name|boolean
name|isServerInFailureMap
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|this
operator|.
name|repeatedFailuresMap
operator|.
name|containsKey
argument_list|(
name|serverName
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
return|return
literal|"PreemptiveFastFailInterceptor"
return|;
block|}
block|}
end_class

end_unit

