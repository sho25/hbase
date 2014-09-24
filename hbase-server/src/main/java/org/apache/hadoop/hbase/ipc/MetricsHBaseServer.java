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
name|ipc
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
name|hbase
operator|.
name|CompatibilitySingletonFactory
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsHBaseServer
block|{
specifier|private
name|MetricsHBaseServerSource
name|source
decl_stmt|;
specifier|public
name|MetricsHBaseServer
parameter_list|(
name|String
name|serverName
parameter_list|,
name|MetricsHBaseServerWrapper
name|wrapper
parameter_list|)
block|{
name|source
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsHBaseServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|create
argument_list|(
name|serverName
argument_list|,
name|wrapper
argument_list|)
expr_stmt|;
block|}
name|void
name|authorizationSuccess
parameter_list|()
block|{
name|source
operator|.
name|authorizationSuccess
argument_list|()
expr_stmt|;
block|}
name|void
name|authorizationFailure
parameter_list|()
block|{
name|source
operator|.
name|authorizationFailure
argument_list|()
expr_stmt|;
block|}
name|void
name|authenticationFailure
parameter_list|()
block|{
name|source
operator|.
name|authenticationFailure
argument_list|()
expr_stmt|;
block|}
name|void
name|authenticationSuccess
parameter_list|()
block|{
name|source
operator|.
name|authenticationSuccess
argument_list|()
expr_stmt|;
block|}
name|void
name|sentBytes
parameter_list|(
name|long
name|count
parameter_list|)
block|{
name|source
operator|.
name|sentBytes
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
name|void
name|receivedBytes
parameter_list|(
name|int
name|count
parameter_list|)
block|{
name|source
operator|.
name|receivedBytes
argument_list|(
name|count
argument_list|)
expr_stmt|;
block|}
name|void
name|dequeuedCall
parameter_list|(
name|int
name|qTime
parameter_list|)
block|{
name|source
operator|.
name|dequeuedCall
argument_list|(
name|qTime
argument_list|)
expr_stmt|;
block|}
name|void
name|processedCall
parameter_list|(
name|int
name|processingTime
parameter_list|)
block|{
name|source
operator|.
name|processedCall
argument_list|(
name|processingTime
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetricsHBaseServerSource
name|getMetricsSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
block|}
end_class

end_unit

