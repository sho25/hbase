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
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
import|;
end_import

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
name|TableName
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * The asynchronous version of Table. Obtain an instance from a {@link AsyncConnection}.  *<p>  * The implementation is NOT required to be thread safe. Do NOT access it from multiple threads  * concurrently.  *<p>  * Usually the implementations will not throw any exception directly, you need to get the exception  * from the returned {@link CompletableFuture}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|AsyncTable
block|{
comment|/**    * Gets the fully qualified table name instance of this table.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
comment|/**    * Returns the {@link org.apache.hadoop.conf.Configuration} object used by this instance.    *<p>    * The reference returned is not a copy, so any change made to it will affect this instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Set timeout of each rpc read request in operations of this Table instance, will override the    * value of {@code hbase.rpc.read.timeout} in configuration. If a rpc read request waiting too    * long, it will stop waiting and send a new request to retry until retries exhausted or operation    * timeout reached.    */
name|void
name|setReadRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Get timeout of each rpc read request in this Table instance.    */
name|long
name|getReadRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set timeout of each rpc write request in operations of this Table instance, will override the    * value of {@code hbase.rpc.write.timeout} in configuration. If a rpc write request waiting too    * long, it will stop waiting and send a new request to retry until retries exhausted or operation    * timeout reached.    */
name|void
name|setWriteRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Get timeout of each rpc write request in this Table instance.    */
name|long
name|getWriteRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Set timeout of each operation in this Table instance, will override the value of    * {@code hbase.client.operation.timeout} in configuration.    *<p>    * Operation timeout is a top-level restriction that makes sure an operation will not be blocked    * more than this. In each operation, if rpc request fails because of timeout or other reason, it    * will retry until success or throw a RetriesExhaustedException. But if the total time elapsed    * reach the operation timeout before retries exhausted, it will break early and throw    * SocketTimeoutException.    */
name|void
name|setOperationTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Get timeout of each operation in Table instance.    */
name|long
name|getOperationTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
function_decl|;
comment|/**    * Test for the existence of columns in the table, as specified by the Get.    *<p>    * This will return true if the Get matches one or more keys, false if not.    *<p>    * This is a server-side call so it prevents any data from being transfered to the client.    */
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
function_decl|;
comment|/**    * Extracts certain cells from a given row.    *<p>    * Return the data coming from the specified row, if it exists. If the row specified doesn't    * exist, the {@link Result} instance returned won't contain any    * {@link org.apache.hadoop.hbase.KeyValue}, as indicated by {@link Result#isEmpty()}.    * @param get The object that specifies what data to fetch and from which row.    */
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
function_decl|;
comment|/**    * Puts some data to the table.    * @param put The data to put.    */
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
function_decl|;
comment|/**    * Deletes the specified cells/row.    * @param delete The object that specifies what to delete.    */
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

