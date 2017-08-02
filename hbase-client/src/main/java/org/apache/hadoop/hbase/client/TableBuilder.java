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

begin_comment
comment|/**  * For creating {@link Table} instance.  *<p>  * The implementation should have default configurations set before returning the builder to user.  * So users are free to only set the configurations they care about to create a new  * Table instance.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|TableBuilder
block|{
comment|/**    * Set timeout for a whole operation such as get, put or delete. Notice that scan will not be    * effected by this value, see scanTimeoutNs.    *<p>    * Operation timeout and max attempt times(or max retry times) are both limitations for retrying,    * we will stop retrying when we reach any of the limitations.    */
name|TableBuilder
name|setOperationTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
function_decl|;
comment|/**    * Set timeout for each rpc request.    *<p>    * Notice that this will<strong>NOT</strong> change the rpc timeout for read(get, scan) request    * and write request(put, delete).    */
name|TableBuilder
name|setRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
function_decl|;
comment|/**    * Set timeout for each read(get, scan) rpc request.    */
name|TableBuilder
name|setReadRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
function_decl|;
comment|/**    * Set timeout for each write(put, delete) rpc request.    */
name|TableBuilder
name|setWriteRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
function_decl|;
comment|/**    * Create the {@link Table} instance.    */
name|Table
name|build
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

