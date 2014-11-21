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
name|java
operator|.
name|io
operator|.
name|IOException
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
specifier|public
class|class
name|DelegatingRetryingCallable
parameter_list|<
name|T
parameter_list|,
name|D
extends|extends
name|RetryingCallable
parameter_list|<
name|T
parameter_list|>
parameter_list|>
implements|implements
name|RetryingCallable
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
specifier|final
name|D
name|delegate
decl_stmt|;
specifier|public
name|DelegatingRetryingCallable
parameter_list|(
name|D
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|call
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|delegate
operator|.
name|call
argument_list|(
name|callTimeout
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|prepare
argument_list|(
name|reload
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|throwable
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
block|{
name|delegate
operator|.
name|throwable
argument_list|(
name|t
argument_list|,
name|retrying
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getExceptionMessageAdditionalDetail
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|sleep
parameter_list|(
name|long
name|pause
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
return|return
name|delegate
operator|.
name|sleep
argument_list|(
name|pause
argument_list|,
name|tries
argument_list|)
return|;
block|}
block|}
end_class

end_unit

