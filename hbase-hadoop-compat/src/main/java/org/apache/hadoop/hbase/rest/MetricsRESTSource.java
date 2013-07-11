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
name|rest
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
name|metrics
operator|.
name|BaseSource
import|;
end_import

begin_comment
comment|/**  * Interface of the Metrics Source that will export data to Hadoop's Metrics2 system.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsRESTSource
extends|extends
name|BaseSource
block|{
name|String
name|METRICS_NAME
init|=
literal|"REST"
decl_stmt|;
name|String
name|CONTEXT
init|=
literal|"rest"
decl_stmt|;
name|String
name|JMX_CONTEXT
init|=
literal|"REST"
decl_stmt|;
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about the HBase REST server"
decl_stmt|;
name|String
name|REQUEST_KEY
init|=
literal|"requests"
decl_stmt|;
name|String
name|SUCCESSFUL_GET_KEY
init|=
literal|"successfulGet"
decl_stmt|;
name|String
name|SUCCESSFUL_PUT_KEY
init|=
literal|"successfulPut"
decl_stmt|;
name|String
name|SUCCESSFUL_DELETE_KEY
init|=
literal|"successfulDelete"
decl_stmt|;
name|String
name|FAILED_GET_KEY
init|=
literal|"failedGet"
decl_stmt|;
name|String
name|FAILED_PUT_KEY
init|=
literal|"failedPut"
decl_stmt|;
name|String
name|FAILED_DELETE_KEY
init|=
literal|"failedDelete"
decl_stmt|;
comment|/**    * Increment the number of requests    *    * @param inc Ammount to increment by    */
name|void
name|incrementRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Increment the number of successful Get requests.    *    * @param inc Number of successful get requests.    */
name|void
name|incrementSucessfulGetRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Increment the number of successful Put requests.    *    * @param inc Number of successful put requests.    */
name|void
name|incrementSucessfulPutRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Increment the number of successful Delete requests.    *    * @param inc    */
name|void
name|incrementSucessfulDeleteRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Increment the number of failed Put Requests.    *    * @param inc Number of failed Put requests.    */
name|void
name|incrementFailedPutRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Increment the number of failed Get requests.    *    * @param inc The number of failed Get Requests.    */
name|void
name|incrementFailedGetRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
comment|/**    * Increment the number of failed Delete requests.    *    * @param inc The number of failed delete requests.    */
name|void
name|incrementFailedDeleteRequests
parameter_list|(
name|int
name|inc
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

