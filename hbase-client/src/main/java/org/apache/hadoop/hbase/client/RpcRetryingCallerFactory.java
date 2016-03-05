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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Factory to create an {@link RpcRetryingCaller}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RpcRetryingCallerFactory
block|{
comment|/** Configuration key for a custom {@link RpcRetryingCaller} */
specifier|public
specifier|static
specifier|final
name|String
name|CUSTOM_CALLER_CONF_KEY
init|=
literal|"hbase.rpc.callerfactory.class"
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|long
name|pause
decl_stmt|;
specifier|private
specifier|final
name|int
name|retries
decl_stmt|;
specifier|private
specifier|final
name|RetryingCallerInterceptor
name|interceptor
decl_stmt|;
specifier|private
specifier|final
name|int
name|startLogErrorsCnt
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|enableBackPressure
decl_stmt|;
specifier|private
name|ServerStatisticTracker
name|stats
decl_stmt|;
specifier|public
name|RpcRetryingCallerFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|RetryingCallerInterceptorFactory
operator|.
name|NO_OP_INTERCEPTOR
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RpcRetryingCallerFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|RetryingCallerInterceptor
name|interceptor
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|pause
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_PAUSE
argument_list|)
expr_stmt|;
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
name|startLogErrorsCnt
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|AsyncProcess
operator|.
name|START_LOG_ERRORS_AFTER_COUNT_KEY
argument_list|,
name|AsyncProcess
operator|.
name|DEFAULT_START_LOG_ERRORS_AFTER_COUNT
argument_list|)
expr_stmt|;
name|this
operator|.
name|interceptor
operator|=
name|interceptor
expr_stmt|;
name|enableBackPressure
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_CLIENT_BACKPRESSURE
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ENABLE_CLIENT_BACKPRESSURE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the tracker that should be used for tracking statistics about the server    */
specifier|public
name|void
name|setStatisticTracker
parameter_list|(
name|ServerStatisticTracker
name|statisticTracker
parameter_list|)
block|{
name|this
operator|.
name|stats
operator|=
name|statisticTracker
expr_stmt|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|RpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|newCaller
parameter_list|()
block|{
comment|// We store the values in the factory instance. This way, constructing new objects
comment|//  is cheap as it does not require parsing a complex structure.
name|RpcRetryingCaller
argument_list|<
name|T
argument_list|>
name|caller
init|=
operator|new
name|RpcRetryingCallerImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|pause
argument_list|,
name|retries
argument_list|,
name|interceptor
argument_list|,
name|startLogErrorsCnt
argument_list|)
decl_stmt|;
return|return
name|caller
return|;
block|}
specifier|public
specifier|static
name|RpcRetryingCallerFactory
name|instantiate
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
return|return
name|instantiate
argument_list|(
name|configuration
argument_list|,
name|RetryingCallerInterceptorFactory
operator|.
name|NO_OP_INTERCEPTOR
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RpcRetryingCallerFactory
name|instantiate
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|ServerStatisticTracker
name|stats
parameter_list|)
block|{
return|return
name|instantiate
argument_list|(
name|configuration
argument_list|,
name|RetryingCallerInterceptorFactory
operator|.
name|NO_OP_INTERCEPTOR
argument_list|,
name|stats
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RpcRetryingCallerFactory
name|instantiate
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|RetryingCallerInterceptor
name|interceptor
parameter_list|,
name|ServerStatisticTracker
name|stats
parameter_list|)
block|{
name|String
name|clazzName
init|=
name|RpcRetryingCallerFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|rpcCallerFactoryClazz
init|=
name|configuration
operator|.
name|get
argument_list|(
name|RpcRetryingCallerFactory
operator|.
name|CUSTOM_CALLER_CONF_KEY
argument_list|,
name|clazzName
argument_list|)
decl_stmt|;
name|RpcRetryingCallerFactory
name|factory
decl_stmt|;
if|if
condition|(
name|rpcCallerFactoryClazz
operator|.
name|equals
argument_list|(
name|clazzName
argument_list|)
condition|)
block|{
name|factory
operator|=
operator|new
name|RpcRetryingCallerFactory
argument_list|(
name|configuration
argument_list|,
name|interceptor
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|factory
operator|=
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|rpcCallerFactoryClazz
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|configuration
block|}
argument_list|)
expr_stmt|;
block|}
comment|// setting for backwards compat with existing caller factories, rather than in the ctor
name|factory
operator|.
name|setStatisticTracker
argument_list|(
name|stats
argument_list|)
expr_stmt|;
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit

