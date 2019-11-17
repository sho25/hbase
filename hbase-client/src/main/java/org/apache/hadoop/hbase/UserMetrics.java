begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Bytes
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**   * Encapsulates per-user load metrics.   */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|UserMetrics
block|{
interface|interface
name|ClientMetrics
block|{
name|String
name|getHostName
parameter_list|()
function_decl|;
name|long
name|getReadRequestsCount
parameter_list|()
function_decl|;
name|long
name|getWriteRequestsCount
parameter_list|()
function_decl|;
name|long
name|getFilteredReadRequestsCount
parameter_list|()
function_decl|;
block|}
comment|/**    * @return the user name    */
name|byte
index|[]
name|getUserName
parameter_list|()
function_decl|;
comment|/**    * @return the number of read requests made by user    */
name|long
name|getReadRequestCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of write requests made by user    */
name|long
name|getWriteRequestCount
parameter_list|()
function_decl|;
comment|/**    * @return the number of write requests and read requests and coprocessor    *         service requests made by the user    */
specifier|default
name|long
name|getRequestCount
parameter_list|()
block|{
return|return
name|getReadRequestCount
argument_list|()
operator|+
name|getWriteRequestCount
argument_list|()
return|;
block|}
comment|/**    * @return the user name as a string    */
specifier|default
name|String
name|getNameAsString
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getUserName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return metrics per client(hostname)    */
name|Map
argument_list|<
name|String
argument_list|,
name|ClientMetrics
argument_list|>
name|getClientMetrics
parameter_list|()
function_decl|;
comment|/**    * @return count of filtered read requests for a user    */
name|long
name|getFilteredReadRequests
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

