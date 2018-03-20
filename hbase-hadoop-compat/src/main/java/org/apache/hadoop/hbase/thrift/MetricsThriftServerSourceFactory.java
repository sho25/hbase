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
name|thrift
package|;
end_package

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

begin_comment
comment|/**  * Factory that will be used to create metrics sources for the two diffent types of thrift servers.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsThriftServerSourceFactory
block|{
name|String
name|METRICS_NAME
init|=
literal|"Thrift"
decl_stmt|;
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Thrift Server Metrics"
decl_stmt|;
name|String
name|THRIFT_ONE_METRICS_CONTEXT
init|=
literal|"thrift-one"
decl_stmt|;
name|String
name|THRIFT_ONE_JMX_CONTEXT
init|=
literal|"Thrift,sub=ThriftOne"
decl_stmt|;
name|String
name|THRIFT_TWO_METRICS_CONTEXT
init|=
literal|"thrift-two"
decl_stmt|;
name|String
name|THRIFT_TWO_JMX_CONTEXT
init|=
literal|"Thrift,sub=ThriftTwo"
decl_stmt|;
comment|/** Create a Source for a thrift one server */
name|MetricsThriftServerSource
name|createThriftOneSource
parameter_list|()
function_decl|;
comment|/** Create a Source for a thrift two server */
name|MetricsThriftServerSource
name|createThriftTwoSource
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

