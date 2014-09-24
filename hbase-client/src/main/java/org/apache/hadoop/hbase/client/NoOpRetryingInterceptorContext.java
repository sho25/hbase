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
name|NoOpRetryingInterceptorContext
extends|extends
name|RetryingCallerInterceptorContext
block|{
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
comment|// Do Nothing
block|}
annotation|@
name|Override
specifier|public
name|RetryingCallerInterceptorContext
name|prepare
parameter_list|(
name|RetryingCallable
argument_list|<
name|?
argument_list|>
name|callable
parameter_list|)
block|{
comment|// Do Nothing
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|RetryingCallerInterceptorContext
name|prepare
parameter_list|(
name|RetryingCallable
argument_list|<
name|?
argument_list|>
name|callable
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
comment|// Do Nothing
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

