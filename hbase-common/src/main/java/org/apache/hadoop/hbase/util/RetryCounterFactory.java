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
name|util
package|;
end_package

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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|RetryCounter
operator|.
name|ExponentialBackoffPolicyWithLimit
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
name|RetryCounter
operator|.
name|RetryConfig
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RetryCounterFactory
block|{
specifier|private
specifier|final
name|RetryConfig
name|retryConfig
decl_stmt|;
specifier|public
name|RetryCounterFactory
parameter_list|(
name|int
name|sleepIntervalMillis
parameter_list|)
block|{
name|this
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|sleepIntervalMillis
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RetryCounterFactory
parameter_list|(
name|int
name|maxAttempts
parameter_list|,
name|int
name|sleepIntervalMillis
parameter_list|)
block|{
name|this
argument_list|(
name|maxAttempts
argument_list|,
name|sleepIntervalMillis
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RetryCounterFactory
parameter_list|(
name|int
name|maxAttempts
parameter_list|,
name|int
name|sleepIntervalMillis
parameter_list|,
name|int
name|maxSleepTime
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|RetryConfig
argument_list|(
name|maxAttempts
argument_list|,
name|sleepIntervalMillis
argument_list|,
name|maxSleepTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|new
name|ExponentialBackoffPolicyWithLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RetryCounterFactory
parameter_list|(
name|RetryConfig
name|retryConfig
parameter_list|)
block|{
name|this
operator|.
name|retryConfig
operator|=
name|retryConfig
expr_stmt|;
block|}
specifier|public
name|RetryCounter
name|create
parameter_list|()
block|{
return|return
operator|new
name|RetryCounter
argument_list|(
name|retryConfig
argument_list|)
return|;
block|}
block|}
end_class

end_unit

