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
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|allOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionUtils
operator|.
name|toCheckExistenceOnly
import|;
end_import

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
name|List
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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

begin_comment
comment|/**  * The base interface for asynchronous version of Table. Obtain an instance from a  * {@link AsyncConnection}.  *<p>  * The implementation is required to be thread safe.  *<p>  * Usually the implementation will not throw any exception directly. You need to get the exception  * from the returned {@link CompletableFuture}.  */
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
name|AsyncTableBase
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
comment|/**    * Get timeout of each rpc request in this Table instance. It will be overridden by a more    * specific rpc timeout config such as readRpcTimeout or writeRpcTimeout.    * @see #getReadRpcTimeout(TimeUnit)    * @see #getWriteRpcTimeout(TimeUnit)    */
name|long
name|getRpcTimeout
parameter_list|(
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
comment|/**    * Get timeout of each rpc write request in this Table instance.    */
name|long
name|getWriteRpcTimeout
parameter_list|(
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
comment|/**    * Get the timeout of a single operation in a scan. It works like operation timeout for other    * operations.    */
name|long
name|getScanTimeout
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
return|return
name|get
argument_list|(
name|toCheckExistenceOnly
argument_list|(
name|get
argument_list|)
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
comment|/**    * Atomically checks if a row/family/qualifier value equals to the expected value. If it does, it    * adds the put. If the passed value is null, the check is for the lack of column (ie:    * non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param value the expected value    * @param put data to put if check succeeds    * @return true if the new put was executed, false otherwise. The return value will be wrapped by    *         a {@link CompletableFuture}.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndPut
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
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
block|{
return|return
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
return|;
block|}
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected value. If it does, it    * adds the put. If the passed value is null, the check is for the lack of column (ie:    * non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp comparison operator to use    * @param value the expected value    * @param put data to put if check succeeds    * @return true if the new put was executed, false otherwise. The return value will be wrapped by    *         a {@link CompletableFuture}.    */
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndPut
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
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value equals to the expected value. If it does, it    * adds the delete. If the passed value is null, the check is for the lack of column (ie:    * non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param value the expected value    * @param delete data to delete if check succeeds    * @return true if the new delete was executed, false otherwise. The return value will be wrapped    *         by a {@link CompletableFuture}.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndDelete
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
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
block|{
return|return
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
return|;
block|}
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected value. If it does, it    * adds the delete. If the passed value is null, the check is for the lack of column (ie:    * non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp comparison operator to use    * @param value the expected value    * @param delete data to delete if check succeeds    * @return true if the new delete was executed, false otherwise. The return value will be wrapped    *         by a {@link CompletableFuture}.    */
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndDelete
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
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
function_decl|;
comment|/**    * Performs multiple mutations atomically on a single row. Currently {@link Put} and    * {@link Delete} are supported.    * @param mutation object that specifies the set of mutations to perform atomically    * @return A {@link CompletableFuture} that always returns null when complete normally.    */
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|mutateRow
parameter_list|(
name|RowMutations
name|mutation
parameter_list|)
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value equals to the expected value. If it does, it    * performs the row mutations. If the passed value is null, the check is for the lack of column    * (ie: non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param value the expected value    * @param mutation mutations to perform if check succeeds    * @return true if the new put was executed, false otherwise. The return value will be wrapped by    *         a {@link CompletableFuture}.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndMutate
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
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|mutation
parameter_list|)
block|{
return|return
name|checkAndMutate
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|value
argument_list|,
name|mutation
argument_list|)
return|;
block|}
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected value. If it does, it    * performs the row mutations. If the passed value is null, the check is for the lack of column    * (ie: non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp the comparison operator    * @param value the expected value    * @param mutation mutations to perform if check succeeds    * @return true if the new put was executed, false otherwise. The return value will be wrapped by    *         a {@link CompletableFuture}.    */
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
name|checkAndMutate
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
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|mutation
parameter_list|)
function_decl|;
comment|/**    * Just call {@link #smallScan(Scan, int)} with {@link Integer#MAX_VALUE}.    * @see #smallScan(Scan, int)    */
specifier|default
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|Result
argument_list|>
argument_list|>
name|smallScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
return|return
name|smallScan
argument_list|(
name|scan
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
comment|/**    * Return all the results that match the given scan object. The number of the returned results    * will not be greater than {@code limit}.    *<p>    * Notice that the scan must be small, and should not use batch or allowPartialResults. The    * {@code caching} property of the scan object is also ignored as we will use {@code limit}    * instead.    * @param scan A configured {@link Scan} object.    * @param limit the limit of results count    * @return The results of this small scan operation. The return value will be wrapped by a    *         {@link CompletableFuture}.    */
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|Result
argument_list|>
argument_list|>
name|smallScan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|int
name|limit
parameter_list|)
function_decl|;
comment|/**    * Test for the existence of columns in the table, as specified by the Gets.    *<p>    * This will return a list of booleans. Each value will be true if the related Get matches one or    * more keys, false if not.    *<p>    * This is a server-side call so it prevents any data from being transferred to the client.    * @param gets the Gets    * @return A list of {@link CompletableFuture}s that represent the existence for each get.    */
specifier|default
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
argument_list|>
name|exists
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|toCheckExistenceOnly
argument_list|(
name|gets
argument_list|)
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
operator|<
name|CompletableFuture
argument_list|<
name|Boolean
argument_list|>
operator|>
name|map
argument_list|(
name|f
lambda|->
name|f
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
argument_list|)
operator|.
name|collect
argument_list|(
name|toList
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * A simple version for batch exists. It will fail if there are any failures and you will get the    * whole result boolean list at once if the operation is succeeded.    * @param gets the Gets    * @return A {@link CompletableFuture} that wrapper the result boolean list.    */
specifier|default
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|Boolean
argument_list|>
argument_list|>
name|existsAll
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
block|{
return|return
name|allOf
argument_list|(
name|exists
argument_list|(
name|gets
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Extracts certain cells from the given rows, in batch.    *<p>    * Notice that you may not get all the results with this function, which means some of the    * returned {@link CompletableFuture}s may succeed while some of the other returned    * {@link CompletableFuture}s may fail.    * @param gets The objects that specify what data to fetch and from which rows.    * @return A list of {@link CompletableFuture}s that represent the result for each get.    */
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Result
argument_list|>
argument_list|>
name|get
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
function_decl|;
comment|/**    * A simple version for batch get. It will fail if there are any failures and you will get the    * whole result list at once if the operation is succeeded.    * @param gets The objects that specify what data to fetch and from which rows.    * @return A {@link CompletableFuture} that wrapper the result list.    */
specifier|default
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|Result
argument_list|>
argument_list|>
name|getAll
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
block|{
return|return
name|allOf
argument_list|(
name|get
argument_list|(
name|gets
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Puts some data in the table, in batch.    * @param puts The list of mutations to apply.    * @return A list of {@link CompletableFuture}s that represent the result for each put.    */
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
function_decl|;
comment|/**    * A simple version of batch put. It will fail if there are any failures.    * @param puts The list of mutations to apply.    * @return A {@link CompletableFuture} that always returns null when complete normally.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|putAll
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
block|{
return|return
name|allOf
argument_list|(
name|put
argument_list|(
name|puts
argument_list|)
argument_list|)
operator|.
name|thenApply
argument_list|(
name|r
lambda|->
literal|null
argument_list|)
return|;
block|}
comment|/**    * Deletes the specified cells/rows in bulk.    * @param deletes list of things to delete.    * @return A list of {@link CompletableFuture}s that represent the result for each delete.    */
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|delete
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
function_decl|;
comment|/**    * A simple version of batch delete. It will fail if there are any failures.    * @param deletes list of things to delete.    * @return A {@link CompletableFuture} that always returns null when complete normally.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|deleteAll
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
block|{
return|return
name|allOf
argument_list|(
name|delete
argument_list|(
name|deletes
argument_list|)
argument_list|)
operator|.
name|thenApply
argument_list|(
name|r
lambda|->
literal|null
argument_list|)
return|;
block|}
comment|/**    * Method that does a batch call on Deletes, Gets, Puts, Increments and Appends. The ordering of    * execution of the actions is not defined. Meaning if you do a Put and a Get in the same    * {@link #batch} call, you will not necessarily be guaranteed that the Get returns what the Put    * had put.    * @param actions list of Get, Put, Delete, Increment, Append objects    * @return A list of {@link CompletableFuture}s that represent the result for each action.    */
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
name|batch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|)
function_decl|;
comment|/**    * A simple version of batch. It will fail if there are any failures and you will get the whole    * result list at once if the operation is succeeded.    * @param actions list of Get, Put, Delete, Increment, Append objects    * @return A list of the result for the actions. Wrapped by a {@link CompletableFuture}.    */
specifier|default
parameter_list|<
name|T
parameter_list|>
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|T
argument_list|>
argument_list|>
name|batchAll
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|)
block|{
return|return
name|allOf
argument_list|(
name|batch
argument_list|(
name|actions
argument_list|)
argument_list|)
return|;
block|}
block|}
end_interface

end_unit

