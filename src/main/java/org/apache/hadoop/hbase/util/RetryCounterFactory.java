begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RetryCounterFactory
block|{
specifier|private
specifier|final
name|int
name|maxRetries
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryIntervalMillis
decl_stmt|;
specifier|public
name|RetryCounterFactory
parameter_list|(
name|int
name|maxRetries
parameter_list|,
name|int
name|retryIntervalMillis
parameter_list|)
block|{
name|this
operator|.
name|maxRetries
operator|=
name|maxRetries
expr_stmt|;
name|this
operator|.
name|retryIntervalMillis
operator|=
name|retryIntervalMillis
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
name|maxRetries
argument_list|,
name|retryIntervalMillis
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
block|}
end_class

end_unit

