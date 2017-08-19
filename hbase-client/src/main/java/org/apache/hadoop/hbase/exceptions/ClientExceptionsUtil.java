begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|exceptions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|SyncFailedException
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
name|ConnectException
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
name|nio
operator|.
name|channels
operator|.
name|ClosedChannelException
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
name|TimeoutException
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
name|CallDroppedException
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
name|MultiActionResultTooLarge
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
name|RegionTooBusyException
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
name|RetryImmediatelyException
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
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|ipc
operator|.
name|FailedServerException
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
name|quotas
operator|.
name|ThrottlingException
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|ClientExceptionsUtil
block|{
specifier|private
name|ClientExceptionsUtil
parameter_list|()
block|{}
specifier|public
specifier|static
name|boolean
name|isMetaClearingException
parameter_list|(
name|Throwable
name|cur
parameter_list|)
block|{
name|cur
operator|=
name|findException
argument_list|(
name|cur
argument_list|)
expr_stmt|;
if|if
condition|(
name|cur
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
operator|!
name|isSpecialException
argument_list|(
name|cur
argument_list|)
operator|||
operator|(
name|cur
operator|instanceof
name|RegionMovedException
operator|)
operator|||
name|cur
operator|instanceof
name|NotServingRegionException
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isSpecialException
parameter_list|(
name|Throwable
name|cur
parameter_list|)
block|{
return|return
operator|(
name|cur
operator|instanceof
name|RegionMovedException
operator|||
name|cur
operator|instanceof
name|RegionOpeningException
operator|||
name|cur
operator|instanceof
name|RegionTooBusyException
operator|||
name|cur
operator|instanceof
name|ThrottlingException
operator|||
name|cur
operator|instanceof
name|MultiActionResultTooLarge
operator|||
name|cur
operator|instanceof
name|RetryImmediatelyException
operator|||
name|cur
operator|instanceof
name|CallQueueTooBigException
operator|||
name|cur
operator|instanceof
name|CallDroppedException
operator|||
name|cur
operator|instanceof
name|NotServingRegionException
operator|||
name|cur
operator|instanceof
name|RequestTooBigException
operator|)
return|;
block|}
comment|/**    * Look for an exception we know in the remote exception:    * - hadoop.ipc wrapped exceptions    * - nested exceptions    *    * Looks for: RegionMovedException / RegionOpeningException / RegionTooBusyException /    *            ThrottlingException    * @return null if we didn't find the exception, the exception otherwise.    */
specifier|public
specifier|static
name|Throwable
name|findException
parameter_list|(
name|Object
name|exception
parameter_list|)
block|{
if|if
condition|(
name|exception
operator|==
literal|null
operator|||
operator|!
operator|(
name|exception
operator|instanceof
name|Throwable
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Throwable
name|cur
init|=
operator|(
name|Throwable
operator|)
name|exception
decl_stmt|;
while|while
condition|(
name|cur
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isSpecialException
argument_list|(
name|cur
argument_list|)
condition|)
block|{
return|return
name|cur
return|;
block|}
if|if
condition|(
name|cur
operator|instanceof
name|RemoteException
condition|)
block|{
name|RemoteException
name|re
init|=
operator|(
name|RemoteException
operator|)
name|cur
decl_stmt|;
name|cur
operator|=
name|re
operator|.
name|unwrapRemoteException
argument_list|()
expr_stmt|;
comment|// unwrapRemoteException can return the exception given as a parameter when it cannot
comment|//  unwrap it. In this case, there is no need to look further
comment|// noinspection ObjectEquality
if|if
condition|(
name|cur
operator|==
name|re
condition|)
block|{
return|return
name|cur
return|;
block|}
comment|// When we receive RemoteException which wraps IOException which has a cause as
comment|// RemoteException we can get into infinite loop here; so if the cause of the exception
comment|// is RemoteException, we shouldn't look further.
block|}
elseif|else
if|if
condition|(
name|cur
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|cur
operator|.
name|getCause
argument_list|()
operator|instanceof
name|RemoteException
operator|)
condition|)
block|{
name|cur
operator|=
name|cur
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
else|else
block|{
return|return
name|cur
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Checks if the exception is CallQueueTooBig exception (maybe wrapped    * into some RemoteException).    * @param t exception to check    * @return true if it's a CQTBE, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isCallQueueTooBigException
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|t
operator|=
name|findException
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
operator|(
name|t
operator|instanceof
name|CallQueueTooBigException
operator|)
return|;
block|}
comment|/**    * Checks if the exception is CallDroppedException (maybe wrapped    * into some RemoteException).    * @param t exception to check    * @return true if it's a CQTBE, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isCallDroppedException
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|t
operator|=
name|findException
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
operator|(
name|t
operator|instanceof
name|CallDroppedException
operator|)
return|;
block|}
comment|/**    * Check if the exception is something that indicates that we cannot    * contact/communicate with the server.    *    * @param e exception to check    * @return true when exception indicates that the client wasn't able to make contact with server    */
specifier|public
specifier|static
name|boolean
name|isConnectionException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// This list covers most connectivity exceptions but not all.
comment|// For example, in SocketOutputStream a plain IOException is thrown
comment|// at times when the channel is closed.
return|return
operator|(
name|e
operator|instanceof
name|SocketTimeoutException
operator|||
name|e
operator|instanceof
name|ConnectException
operator|||
name|e
operator|instanceof
name|ClosedChannelException
operator|||
name|e
operator|instanceof
name|SyncFailedException
operator|||
name|e
operator|instanceof
name|EOFException
operator|||
name|e
operator|instanceof
name|TimeoutException
operator|||
name|e
operator|instanceof
name|CallTimeoutException
operator|||
name|e
operator|instanceof
name|ConnectionClosingException
operator|||
name|e
operator|instanceof
name|FailedServerException
operator|)
return|;
block|}
comment|/**    * Translates exception for preemptive fast fail checks.    * @param t exception to check    * @return translated exception    * @throws IOException    */
specifier|public
specifier|static
name|Throwable
name|translatePFFE
parameter_list|(
name|Throwable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|t
operator|instanceof
name|NoSuchMethodError
condition|)
block|{
comment|// We probably can't recover from this exception by retrying.
throw|throw
operator|(
name|NoSuchMethodError
operator|)
name|t
throw|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|NullPointerException
condition|)
block|{
comment|// The same here. This is probably a bug.
throw|throw
operator|(
name|NullPointerException
operator|)
name|t
throw|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|UndeclaredThrowableException
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
if|if
condition|(
name|t
operator|instanceof
name|Error
condition|)
block|{
throw|throw
operator|(
name|Error
operator|)
name|t
throw|;
block|}
return|return
name|t
return|;
block|}
block|}
end_class

end_unit

