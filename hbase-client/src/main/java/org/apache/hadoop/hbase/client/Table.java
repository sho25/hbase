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
name|client
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|HTableDescriptor
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
name|client
operator|.
name|coprocessor
operator|.
name|Batch
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
name|ipc
operator|.
name|CoprocessorRpcChannel
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Used to communicate with a single HBase table.  * Obtain an instance from an {@link HConnection}.  *  * @since 0.99.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
interface|interface
name|Table
extends|extends
name|Closeable
block|{
comment|/**    * Gets the fully qualified table name instance of this table.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
comment|/**    * Returns the {@link org.apache.hadoop.conf.Configuration} object used by this instance.    *<p>    * The reference returned is not a copy, so any change made to it will    * affect this instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Gets the {@link org.apache.hadoop.hbase.HTableDescriptor table descriptor} for this table.    * @throws java.io.IOException if a remote or network exception occurs.    */
name|HTableDescriptor
name|getTableDescriptor
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Test for the existence of columns in the table, as specified by the Get.    *<p>    *    * This will return true if the Get matches one or more keys, false if not.    *<p>    *    * This is a server-side call so it prevents any data from being transfered to    * the client.    *    * @param get the Get    * @return true if the specified Get matches one or more keys, false if not    * @throws IOException e    */
name|boolean
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Test for the existence of columns in the table, as specified by the Gets.    *<p>    *    * This will return an array of booleans. Each value will be true if the related Get matches    * one or more keys, false if not.    *<p>    *    * This is a server-side call so it prevents any data from being transferred to    * the client.    *    * @param gets the Gets    * @return Array of boolean.  True if the specified Get matches one or more keys, false if not.    * @throws IOException e    */
name|boolean
index|[]
name|existsAll
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Method that does a batch call on Deletes, Gets, Puts, Increments and Appends.    * The ordering of execution of the actions is not defined. Meaning if you do a Put and a    * Get in the same {@link #batch} call, you will not necessarily be    * guaranteed that the Get returns what the Put had put.    *    * @param actions list of Get, Put, Delete, Increment, Append objects    * @param results Empty Object[], same size as actions. Provides access to partial    *                results, in case an exception is thrown. A null in the result array means that    *                the call for that action failed, even after retries    * @throws IOException    * @since 0.90.0    */
name|void
name|batch
parameter_list|(
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
specifier|final
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Same as {@link #batch(List, Object[])}, but returns an array of    * results instead of using a results parameter reference.    *    * @param actions list of Get, Put, Delete, Increment, Append objects    * @return the results from the actions. A null in the return array means that    *         the call for that action failed, even after retries    * @throws IOException    * @since 0.90.0    * @deprecated If any exception is thrown by one of the actions, there is no way to    * retrieve the partially executed results. Use {@link #batch(List, Object[])} instead.    */
annotation|@
name|Deprecated
name|Object
index|[]
name|batch
parameter_list|(
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Same as {@link #batch(List, Object[])}, but with a callback.    * @since 0.96.0    */
parameter_list|<
name|R
parameter_list|>
name|void
name|batchCallback
parameter_list|(
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
specifier|final
name|Object
index|[]
name|results
parameter_list|,
specifier|final
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Same as {@link #batch(List)}, but with a callback.    *    * @since 0.96.0    * @deprecated If any exception is thrown by one of the actions, there is no way to retrieve the    * partially executed results. Use {@link #batchCallback(List, Object[],    * org.apache.hadoop.hbase.client.coprocessor.Batch.Callback)} instead.    */
annotation|@
name|Deprecated
argument_list|<
name|R
argument_list|>
name|Object
index|[]
name|batchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Extracts certain cells from a given row.    * @param get The object that specifies what data to fetch and from which row.    * @return The data coming from the specified row, if it exists.  If the row    * specified doesn't exist, the {@link Result} instance returned won't    * contain any {@link org.apache.hadoop.hbase.KeyValue}, as indicated by {@link Result#isEmpty()}.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|Result
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Extracts certain cells from the given rows, in batch.    *    * @param gets The objects that specify what data to fetch and from which rows.    * @return The data coming from the specified rows, if it exists.  If the row specified doesn't    * exist, the {@link Result} instance returned won't contain any {@link    * org.apache.hadoop.hbase.KeyValue}, as indicated by {@link Result#isEmpty()}. If there are any    * failures even after retries, there will be a null in the results array for those Gets, AND an    * exception will be thrown.    * @throws IOException if a remote or network exception occurs.    * @since 0.90.0    */
name|Result
index|[]
name|get
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns a scanner on the current table as specified by the {@link Scan}    * object.    * Note that the passed {@link Scan}'s start row and caching properties    * maybe changed.    *    * @param scan A configured {@link Scan} object.    * @return A scanner.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets a scanner on the current table for the given family.    *    * @param family The column family to scan.    * @return A scanner.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets a scanner on the current table for the given family and qualifier.    *    * @param family The column family to scan.    * @param qualifier The column qualifier to scan.    * @return A scanner.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Puts some data in the table.    *<p>    * If {@link #isAutoFlush isAutoFlush} is false, the update is buffered    * until the internal buffer is full.    * @param put The data to put.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|void
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Puts some data in the table, in batch.    *<p>    * If {@link #isAutoFlush isAutoFlush} is false, the update is buffered    * until the internal buffer is full.    *<p>    * This can be used for group commit, or for submitting user defined    * batches.  The writeBuffer will be periodically inspected while the List    * is processed, so depending on the List size the writeBuffer may flush    * not at all, or more than once.    * @param puts The list of mutations to apply. The batch put is done by    * aggregating the iteration of the Puts over the write buffer    * at the client-side for a single RPC call.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|void
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected    * value. If it does, it adds the put.  If the passed value is null, the check    * is for the lack of column (ie: non-existance)    *    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param value the expected value    * @param put data to put if check succeeds    * @throws IOException e    * @return true if the new put was executed, false otherwise    */
name|boolean
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
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected    * value. If it does, it adds the put.  If the passed value is null, the check    * is for the lack of column (ie: non-existance)    *    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp comparison operator to use    * @param value the expected value    * @param put data to put if check succeeds    * @throws IOException e    * @return true if the new put was executed, false otherwise    */
name|boolean
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
name|CompareFilter
operator|.
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
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes the specified cells/row.    *    * @param delete The object that specifies what to delete.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    */
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes the specified cells/rows in bulk.    * @param deletes List of things to delete.  List gets modified by this    * method (in particular it gets re-ordered, so the order in which the elements    * are inserted in the list gives no guarantee as to the order in which the    * {@link Delete}s are executed).    * @throws IOException if a remote or network exception occurs. In that case    * the {@code deletes} argument will contain the {@link Delete} instances    * that have not be successfully applied.    * @since 0.20.1    */
name|void
name|delete
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected    * value. If it does, it adds the delete.  If the passed value is null, the    * check is for the lack of column (ie: non-existance)    *    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param value the expected value    * @param delete data to delete if check succeeds    * @throws IOException e    * @return true if the new delete was executed, false otherwise    */
name|boolean
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
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected    * value. If it does, it adds the delete.  If the passed value is null, the    * check is for the lack of column (ie: non-existance)    *    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp comparison operator to use    * @param value the expected value    * @param delete data to delete if check succeeds    * @throws IOException e    * @return true if the new delete was executed, false otherwise    */
name|boolean
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
name|CompareFilter
operator|.
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
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs multiple mutations atomically on a single row. Currently    * {@link Put} and {@link Delete} are supported.    *    * @param rm object that specifies the set of mutations to perform atomically    * @throws IOException    */
name|void
name|mutateRow
parameter_list|(
specifier|final
name|RowMutations
name|rm
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Appends values to one or more columns within a single row.    *<p>    * This operation does not appear atomic to readers.  Appends are done    * under a single row lock, so write operations to a row are synchronized, but    * readers do not take row locks so get and scan operations can see this    * operation partially completed.    *    * @param append object that specifies the columns and amounts to be used    *                  for the increment operations    * @throws IOException e    * @return values of columns after the append operation (maybe null)    */
name|Result
name|append
parameter_list|(
specifier|final
name|Append
name|append
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Increments one or more columns within a single row.    *<p>    * This operation does not appear atomic to readers.  Increments are done    * under a single row lock, so write operations to a row are synchronized, but    * readers do not take row locks so get and scan operations can see this    * operation partially completed.    *    * @param increment object that specifies the columns and amounts to be used    *                  for the increment operations    * @throws IOException e    * @return values of columns after the increment    */
name|Result
name|increment
parameter_list|(
specifier|final
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * See {@link #incrementColumnValue(byte[], byte[], byte[], long, Durability)}    *<p>    * The {@link Durability} is defaulted to {@link Durability#SYNC_WAL}.    * @param row The row that contains the cell to increment.    * @param family The column family of the cell to increment.    * @param qualifier The column qualifier of the cell to increment.    * @param amount The amount to increment the cell with (or decrement, if the    * amount is negative).    * @return The new value, post increment.    * @throws IOException if a remote or network exception occurs.    */
name|long
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
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically increments a column value. If the column value already exists    * and is not a big-endian long, this could throw an exception. If the column    * value does not yet exist it is initialized to<code>amount</code> and    * written to the specified column.    *    *<p>Setting durability to {@link Durability#SKIP_WAL} means that in a fail    * scenario you will lose any increments that have not been flushed.    * @param row The row that contains the cell to increment.    * @param family The column family of the cell to increment.    * @param qualifier The column qualifier of the cell to increment.    * @param amount The amount to increment the cell with (or decrement, if the    * amount is negative).    * @param durability The persistence guarantee for this increment.    * @return The new value, post increment.    * @throws IOException if a remote or network exception occurs.    */
name|long
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
throws|throws
name|IOException
function_decl|;
comment|/**    * Releases any resources held or pending changes in internal buffers.    *    * @throws IOException if a remote or network exception occurs.    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Creates and returns a {@link com.google.protobuf.RpcChannel} instance connected to the    * table region containing the specified row.  The row given does not actually have    * to exist.  Whichever region would contain the row based on start and end keys will    * be used.  Note that the {@code row} parameter is also not passed to the    * coprocessor handler registered for this protocol, unless the {@code row}    * is separately passed as an argument in the service request.  The parameter    * here is only used to locate the region used to handle the call.    *    *<p>    * The obtained {@link com.google.protobuf.RpcChannel} instance can be used to access a published    * coprocessor {@link com.google.protobuf.Service} using standard protobuf service invocations:    *</p>    *    *<div style="background-color: #cccccc; padding: 2px">    *<blockquote><pre>    * CoprocessorRpcChannel channel = myTable.coprocessorService(rowkey);    * MyService.BlockingInterface service = MyService.newBlockingStub(channel);    * MyCallRequest request = MyCallRequest.newBuilder()    *     ...    *     .build();    * MyCallResponse response = service.myCall(null, request);    *</pre></blockquote></div>    *    * @param row The row key used to identify the remote region location    * @return A CoprocessorRpcChannel instance    */
name|CoprocessorRpcChannel
name|coprocessorService
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
function_decl|;
comment|/**    * Creates an instance of the given {@link com.google.protobuf.Service} subclass for each table    * region spanning the range from the {@code startKey} row to {@code endKey} row (inclusive), and    * invokes the passed {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Call#call} method    * with each {@link com.google.protobuf.Service} instance.    *    * @param service the protocol buffer {@code Service} implementation to call    * @param startKey start region selection with region containing this row.  If {@code null}, the    * selection will start with the first table region.    * @param endKey select regions up to and including the region containing this row. If {@code    * null}, selection will continue through the last table region.    * @param callable this instance's {@link org.apache.hadoop.hbase.client.coprocessor.Batch    * .Call#call}    * method will be invoked once per table region, using the {@link com.google.protobuf.Service}    * instance connected to that region.    * @param<T> the {@link com.google.protobuf.Service} subclass to connect to    * @param<R> Return type for the {@code callable} parameter's {@link    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call#call} method    * @return a map of result values keyed by region name    */
parameter_list|<
name|T
extends|extends
name|Service
parameter_list|,
name|R
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|coprocessorService
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
specifier|final
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
function_decl|;
comment|/**    * Creates an instance of the given {@link com.google.protobuf.Service} subclass for each table    * region spanning the range from the {@code startKey} row to {@code endKey} row (inclusive), and    * invokes the passed {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Call#call} method    * with each {@link Service} instance.    *    *<p> The given {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Callback#update(byte[],    * byte[], Object)} method will be called with the return value from each region's {@link    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call#call} invocation.</p>    *    * @param service the protocol buffer {@code Service} implementation to call    * @param startKey start region selection with region containing this row.  If {@code null}, the    * selection will start with the first table region.    * @param endKey select regions up to and including the region containing this row. If {@code    * null}, selection will continue through the last table region.    * @param callable this instance's {@link org.apache.hadoop.hbase.client.coprocessor.Batch    * .Call#call}    * method will be invoked once per table region, using the {@link Service} instance connected to    * that region.    * @param callback    * @param<T> the {@link Service} subclass to connect to    * @param<R> Return type for the {@code callable} parameter's {@link    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call#call} method    */
parameter_list|<
name|T
extends|extends
name|Service
parameter_list|,
name|R
parameter_list|>
name|void
name|coprocessorService
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
specifier|final
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|,
specifier|final
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
function_decl|;
comment|/**    * Tells whether or not 'auto-flush' is turned on.    *    * @return {@code true} if 'auto-flush' is enabled (default), meaning    * {@link Put} operations don't get buffered/delayed and are immediately    * executed.    */
name|boolean
name|isAutoFlush
parameter_list|()
function_decl|;
comment|/**    * Executes all the buffered {@link Put} operations.    *<p>    * This method gets called once automatically for every {@link Put} or batch    * of {@link Put}s (when<code>put(List<Put>)</code> is used) when    * {@link #isAutoFlush} is {@code true}.    * @throws IOException if a remote or network exception occurs.    */
name|void
name|flushCommits
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Set the autoFlush behavior, without changing the value of {@code clearBufferOnFail}    */
name|void
name|setAutoFlushTo
parameter_list|(
name|boolean
name|autoFlush
parameter_list|)
function_decl|;
comment|/**    * Returns the maximum size in bytes of the write buffer for this HTable.    *<p>    * The default value comes from the configuration parameter    * {@code hbase.client.write.buffer}.    * @return The size of the write buffer in bytes.    */
name|long
name|getWriteBufferSize
parameter_list|()
function_decl|;
comment|/**    * Sets the size of the buffer in bytes.    *<p>    * If the new size is less than the current amount of data in the    * write buffer, the buffer gets flushed.    * @param writeBufferSize The new write buffer size, in bytes.    * @throws IOException if a remote or network exception occurs.    */
name|void
name|setWriteBufferSize
parameter_list|(
name|long
name|writeBufferSize
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Creates an instance of the given {@link com.google.protobuf.Service} subclass for each table    * region spanning the range from the {@code startKey} row to {@code endKey} row (inclusive), all    * the invocations to the same region server will be batched into one call. The coprocessor    * service is invoked according to the service instance, method name and parameters.    *    * @param methodDescriptor    *          the descriptor for the protobuf service method to call.    * @param request    *          the method call parameters    * @param startKey    *          start region selection with region containing this row. If {@code null}, the    *          selection will start with the first table region.    * @param endKey    *          select regions up to and including the region containing this row. If {@code null},    *          selection will continue through the last table region.    * @param responsePrototype    *          the proto type of the response of the method in Service.    * @param<R>    *          the response type for the coprocessor Service method    * @throws ServiceException    * @throws Throwable    * @return a map of result values keyed by region name    */
parameter_list|<
name|R
extends|extends
name|Message
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|batchCoprocessorService
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|methodDescriptor
parameter_list|,
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
function_decl|;
comment|/**    * Creates an instance of the given {@link com.google.protobuf.Service} subclass for each table    * region spanning the range from the {@code startKey} row to {@code endKey} row (inclusive), all    * the invocations to the same region server will be batched into one call. The coprocessor    * service is invoked according to the service instance, method name and parameters.    *    *<p>    * The given    * {@link org.apache.hadoop.hbase.client.coprocessor.Batch.Callback#update(byte[],byte[],Object)}    * method will be called with the return value from each region's invocation.    *</p>    *    * @param methodDescriptor    *          the descriptor for the protobuf service method to call.    * @param request    *          the method call parameters    * @param startKey    *          start region selection with region containing this row. If {@code null}, the    *          selection will start with the first table region.    * @param endKey    *          select regions up to and including the region containing this row. If {@code null},    *          selection will continue through the last table region.    * @param responsePrototype    *          the proto type of the response of the method in Service.    * @param callback    *          callback to invoke with the response for each region    * @param<R>    *          the response type for the coprocessor Service method    * @throws ServiceException    * @throws Throwable    */
parameter_list|<
name|R
extends|extends
name|Message
parameter_list|>
name|void
name|batchCoprocessorService
parameter_list|(
name|Descriptors
operator|.
name|MethodDescriptor
name|methodDescriptor
parameter_list|,
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
function_decl|;
block|}
end_interface

end_unit

