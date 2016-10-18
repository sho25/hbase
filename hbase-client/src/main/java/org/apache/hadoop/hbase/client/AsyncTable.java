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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
comment|/**    * Test for the existence of columns in the table, as specified by the Get.    *<p>    * This will return true if the Get matches one or more keys, false if not.    *<p>    * This is a server-side call so it prevents any data from being transfered to the client.    * @return true if the specified Get matches one or more keys, false if not. The return value will    *         be wrapped by a {@link CompletableFuture}.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
block|{
if|if
condition|(
operator|!
name|get
operator|.
name|isCheckExistenceOnly
argument_list|()
condition|)
block|{
name|get
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|get
operator|.
name|getClass
argument_list|()
argument_list|,
name|get
argument_list|)
expr_stmt|;
name|get
operator|.
name|setCheckExistenceOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|get
argument_list|(
name|get
argument_list|)
operator|.
name|thenApply
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getExists
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Extracts certain cells from a given row.    * @param get The object that specifies what data to fetch and from which row.    * @return The data coming from the specified row, if it exists. If the row specified doesn't    *         exist, the {@link Result} instance returned won't contain any    *         {@link org.apache.hadoop.hbase.KeyValue}, as indicated by {@link Result#isEmpty()}. The    *         return value will be wrapped by a {@link CompletableFuture}.    */
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
comment|/**    * Puts some data to the table.    * @param put The data to put.    * @return A {@link CompletableFuture} that always returns null when complete normally.    */
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
comment|/**    * Deletes the specified cells/row.    * @param delete The object that specifies what to delete.    * @return A {@link CompletableFuture} that always returns null when complete normally.    */
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
comment|/**    * Appends values to one or more columns within a single row.    *<p>    * This operation does not appear atomic to readers. Appends are done under a single row lock, so    * write operations to a row are synchronized, but readers do not take row locks so get and scan    * operations can see this operation partially completed.    * @param append object that specifies the columns and amounts to be used for the increment    *          operations    * @return values of columns after the append operation (maybe null). The return value will be    *         wrapped by a {@link CompletableFuture}.    */
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
name|append
parameter_list|(
name|Append
name|append
parameter_list|)
function_decl|;
comment|/**    * Increments one or more columns within a single row.    *<p>    * This operation does not appear atomic to readers. Increments are done under a single row lock,    * so write operations to a row are synchronized, but readers do not take row locks so get and    * scan operations can see this operation partially completed.    * @param increment object that specifies the columns and amounts to be used for the increment    *          operations    * @return values of columns after the increment. The return value will be wrapped by a    *         {@link CompletableFuture}.    */
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
name|increment
parameter_list|(
name|Increment
name|increment
parameter_list|)
function_decl|;
comment|/**    * See {@link #incrementColumnValue(byte[], byte[], byte[], long, Durability)}    *<p>    * The {@link Durability} is defaulted to {@link Durability#SYNC_WAL}.    * @param row The row that contains the cell to increment.    * @param family The column family of the cell to increment.    * @param qualifier The column qualifier of the cell to increment.    * @param amount The amount to increment the cell with (or decrement, if the amount is negative).    * @return The new value, post increment. The return value will be wrapped by a    *         {@link CompletableFuture}.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
return|return
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|,
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
return|;
block|}
comment|/**    * Atomically increments a column value. If the column value already exists and is not a    * big-endian long, this could throw an exception. If the column value does not yet exist it is    * initialized to<code>amount</code> and written to the specified column.    *<p>    * Setting durability to {@link Durability#SKIP_WAL} means that in a fail scenario you will lose    * any increments that have not been flushed.    * @param row The row that contains the cell to increment.    * @param family The column family of the cell to increment.    * @param qualifier The column qualifier of the cell to increment.    * @param amount The amount to increment the cell with (or decrement, if the amount is negative).    * @param durability The persistence guarantee for this increment.    * @return The new value, post increment. The return value will be wrapped by a    *         {@link CompletableFuture}.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Long
argument_list|>
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|Durability
name|durability
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|row
argument_list|,
literal|"row is null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|family
argument_list|,
literal|"family is null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|qualifier
argument_list|,
literal|"qualifier is null"
argument_list|)
expr_stmt|;
return|return
name|increment
argument_list|(
operator|new
name|Increment
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|)
operator|.
name|setDurability
argument_list|(
name|durability
argument_list|)
argument_list|)
operator|.
name|thenApply
argument_list|(
name|r
lambda|->
name|Bytes
operator|.
name|toLong
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_interface

end_unit

