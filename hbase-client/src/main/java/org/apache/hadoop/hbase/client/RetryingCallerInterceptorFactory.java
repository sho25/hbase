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
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Factory implementation to provide the {@link HConnectionImplementation} with  * the implementation of the {@link RetryingCallerInterceptor} that we would use  * to intercept the {@link RpcRetryingCaller} during the course of their calls.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|RetryingCallerInterceptorFactory
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
name|RetryingCallerInterceptorFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|failFast
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RetryingCallerInterceptor
name|NO_OP_INTERCEPTOR
init|=
operator|new
name|NoOpRetryableCallerInterceptor
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|public
name|RetryingCallerInterceptorFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|failFast
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_MODE_ENABLED
argument_list|,
name|HConstants
operator|.
name|HBASE_CLIENT_ENABLE_FAST_FAIL_MODE_DEFAULT
argument_list|)
expr_stmt|;
block|}
comment|/**    * This builds the implementation of {@link RetryingCallerInterceptor} that we    * specify in the conf and returns the same.    *     * To use {@link PreemptiveFastFailInterceptor}, set HBASE_CLIENT_ENABLE_FAST_FAIL_MODE to true.    * HBASE_CLIENT_FAST_FAIL_INTERCEPTOR_IMPL is defaulted to {@link PreemptiveFastFailInterceptor}    *     * @return The factory build method which creates the    *         {@link RetryingCallerInterceptor} object according to the    *         configuration.    */
specifier|public
name|RetryingCallerInterceptor
name|build
parameter_list|()
block|{
name|RetryingCallerInterceptor
name|ret
init|=
name|NO_OP_INTERCEPTOR
decl_stmt|;
if|if
condition|(
name|failFast
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_FAST_FAIL_INTERCEPTOR_IMPL
argument_list|,
name|PreemptiveFastFailInterceptor
operator|.
name|class
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
name|c
operator|.
name|getDeclaredConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|constructor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ret
operator|=
operator|(
name|RetryingCallerInterceptor
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ret
operator|=
operator|new
name|PreemptiveFastFailInterceptor
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|trace
argument_list|(
literal|"Using "
operator|+
name|ret
operator|.
name|toString
argument_list|()
operator|+
literal|" for intercepting the RpcRetryingCaller"
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

