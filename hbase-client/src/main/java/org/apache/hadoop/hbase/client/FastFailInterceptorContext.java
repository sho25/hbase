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
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|FastFailInterceptorContext
extends|extends
name|RetryingCallerInterceptorContext
block|{
comment|// The variable that indicates whether we were able to connect with the server
comment|// in the last run
specifier|private
name|MutableBoolean
name|couldNotCommunicateWithServer
init|=
operator|new
name|MutableBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// If set, we guarantee that no modifications went to server
specifier|private
name|MutableBoolean
name|guaranteedClientSideOnly
init|=
operator|new
name|MutableBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// The variable which indicates whether this was a retry or the first time
specifier|private
name|boolean
name|didTry
init|=
literal|false
decl_stmt|;
comment|// The failure info that is associated with the machine which we are trying to
comment|// contact as part of this attempt.
specifier|private
name|FailureInfo
name|fInfo
init|=
literal|null
decl_stmt|;
comment|// Variable indicating that the thread that is currently executing the
comment|// operation is in a mode where it would retry instead of failing fast, so
comment|// that we can figure out whether making contact with the server is
comment|// possible or not.
specifier|private
name|boolean
name|retryDespiteFastFailMode
init|=
literal|false
decl_stmt|;
comment|// The server that would be contacted to successfully complete this operation.
specifier|private
name|ServerName
name|server
decl_stmt|;
comment|// The number of the retry we are currenty doing.
specifier|private
name|int
name|tries
decl_stmt|;
specifier|public
name|MutableBoolean
name|getCouldNotCommunicateWithServer
parameter_list|()
block|{
return|return
name|couldNotCommunicateWithServer
return|;
block|}
specifier|public
name|MutableBoolean
name|getGuaranteedClientSideOnly
parameter_list|()
block|{
return|return
name|guaranteedClientSideOnly
return|;
block|}
specifier|public
name|FailureInfo
name|getFailureInfo
parameter_list|()
block|{
return|return
name|fInfo
return|;
block|}
specifier|public
name|ServerName
name|getServer
parameter_list|()
block|{
return|return
name|server
return|;
block|}
specifier|public
name|int
name|getTries
parameter_list|()
block|{
return|return
name|tries
return|;
block|}
specifier|public
name|boolean
name|didTry
parameter_list|()
block|{
return|return
name|didTry
return|;
block|}
specifier|public
name|boolean
name|isRetryDespiteFastFailMode
parameter_list|()
block|{
return|return
name|retryDespiteFastFailMode
return|;
block|}
specifier|public
name|void
name|setCouldNotCommunicateWithServer
parameter_list|(
name|MutableBoolean
name|couldNotCommunicateWithServer
parameter_list|)
block|{
name|this
operator|.
name|couldNotCommunicateWithServer
operator|=
name|couldNotCommunicateWithServer
expr_stmt|;
block|}
specifier|public
name|void
name|setGuaranteedClientSideOnly
parameter_list|(
name|MutableBoolean
name|guaranteedClientSideOnly
parameter_list|)
block|{
name|this
operator|.
name|guaranteedClientSideOnly
operator|=
name|guaranteedClientSideOnly
expr_stmt|;
block|}
specifier|public
name|void
name|setDidTry
parameter_list|(
name|boolean
name|didTry
parameter_list|)
block|{
name|this
operator|.
name|didTry
operator|=
name|didTry
expr_stmt|;
block|}
specifier|public
name|void
name|setFailureInfo
parameter_list|(
name|FailureInfo
name|fInfo
parameter_list|)
block|{
name|this
operator|.
name|fInfo
operator|=
name|fInfo
expr_stmt|;
block|}
specifier|public
name|void
name|setRetryDespiteFastFailMode
parameter_list|(
name|boolean
name|retryDespiteFastFailMode
parameter_list|)
block|{
name|this
operator|.
name|retryDespiteFastFailMode
operator|=
name|retryDespiteFastFailMode
expr_stmt|;
block|}
specifier|public
name|void
name|setServer
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
block|}
specifier|public
name|void
name|setTries
parameter_list|(
name|int
name|tries
parameter_list|)
block|{
name|this
operator|.
name|tries
operator|=
name|tries
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|server
operator|=
literal|null
expr_stmt|;
name|fInfo
operator|=
literal|null
expr_stmt|;
name|didTry
operator|=
literal|false
expr_stmt|;
name|couldNotCommunicateWithServer
operator|.
name|setValue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|guaranteedClientSideOnly
operator|.
name|setValue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|retryDespiteFastFailMode
operator|=
literal|false
expr_stmt|;
name|tries
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|FastFailInterceptorContext
name|prepare
parameter_list|(
name|RetryingCallableBase
name|callable
parameter_list|)
block|{
return|return
name|prepare
argument_list|(
name|callable
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|public
name|FastFailInterceptorContext
name|prepare
parameter_list|(
name|RetryingCallableBase
name|callable
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
if|if
condition|(
name|callable
operator|instanceof
name|RegionServerCallable
condition|)
block|{
name|RegionServerCallable
argument_list|<
name|?
argument_list|>
name|retryingCallable
init|=
operator|(
name|RegionServerCallable
argument_list|<
name|?
argument_list|>
operator|)
name|callable
decl_stmt|;
name|server
operator|=
name|retryingCallable
operator|.
name|getLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|tries
operator|=
name|tries
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

