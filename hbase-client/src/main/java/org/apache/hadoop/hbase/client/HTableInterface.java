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
name|java
operator|.
name|io
operator|.
name|IOException
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
comment|/**  * Used to communicate with a single HBase table.  * Obtain an instance from an {@link HConnection}.  *  * @since 0.21.0  * @deprecated use {@link org.apache.hadoop.hbase.client.Table} instead  */
end_comment

begin_interface
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
interface|interface
name|HTableInterface
extends|extends
name|Table
block|{
comment|/**    * Gets the name of this table.    *    * @return the table name.    * @deprecated Use {@link #getName()} instead    */
annotation|@
name|Deprecated
name|byte
index|[]
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Turns 'auto-flush' on or off.    *<p>    * When enabled (default), {@link Put} operations don't get buffered/delayed    * and are immediately executed. Failed operations are not retried. This is    * slower but safer.    *<p>    * Turning off {@code #autoFlush} means that multiple {@link Put}s will be    * accepted before any RPC is actually sent to do the write operations. If the    * application dies before pending writes get flushed to HBase, data will be    * lost.    *<p>    * When you turn {@code #autoFlush} off, you should also consider the    * {@code #clearBufferOnFail} option. By default, asynchronous {@link Put}    * requests will be retried on failure until successful. However, this can    * pollute the writeBuffer and slow down batching performance. Additionally,    * you may want to issue a number of Put requests and call    * {@link #flushCommits()} as a barrier. In both use cases, consider setting    * clearBufferOnFail to true to erase the buffer after {@link #flushCommits()}    * has been called, regardless of success.    *<p>    * In other words, if you call {@code #setAutoFlush(false)}; HBase will retry N time for each    *  flushCommit, including the last one when closing the table. This is NOT recommended,    *  most of the time you want to call {@code #setAutoFlush(false, true)}.    *    * @param autoFlush    *          Whether or not to enable 'auto-flush'.    * @param clearBufferOnFail    *          Whether to keep Put failures in the writeBuffer. If autoFlush is true, then    *          the value of this parameter is ignored and clearBufferOnFail is set to true.    *          Setting clearBufferOnFail to false is deprecated since 0.96.    * @deprecated in 0.99 since setting clearBufferOnFail is deprecated.    * @see BufferedMutator#flush()    */
annotation|@
name|Deprecated
name|void
name|setAutoFlush
parameter_list|(
name|boolean
name|autoFlush
parameter_list|,
name|boolean
name|clearBufferOnFail
parameter_list|)
function_decl|;
comment|/**    * Set the autoFlush behavior, without changing the value of {@code clearBufferOnFail}.    * @deprecated in 0.99 since setting clearBufferOnFail is deprecated. Move on to    *             {@link BufferedMutator}    */
annotation|@
name|Deprecated
name|void
name|setAutoFlushTo
parameter_list|(
name|boolean
name|autoFlush
parameter_list|)
function_decl|;
comment|/**    * Tells whether or not 'auto-flush' is turned on.    *    * @return {@code true} if 'auto-flush' is enabled (default), meaning    * {@link Put} operations don't get buffered/delayed and are immediately    * executed.    * @deprecated as of 1.0.0. Replaced by {@link BufferedMutator}    */
annotation|@
name|Deprecated
name|boolean
name|isAutoFlush
parameter_list|()
function_decl|;
comment|/**    * Executes all the buffered {@link Put} operations.    *<p>    * This method gets called once automatically for every {@link Put} or batch    * of {@link Put}s (when<code>put(List&lt;Put&gt;)</code> is used) when    * {@link #isAutoFlush} is {@code true}.    * @throws IOException if a remote or network exception occurs.    * @deprecated as of 1.0.0. Replaced by {@link BufferedMutator#flush()}    */
annotation|@
name|Deprecated
name|void
name|flushCommits
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns the maximum size in bytes of the write buffer for this HTable.    *<p>    * The default value comes from the configuration parameter    * {@code hbase.client.write.buffer}.    * @return The size of the write buffer in bytes.    * @deprecated as of 1.0.0. Replaced by {@link BufferedMutator#getWriteBufferSize()}    */
annotation|@
name|Deprecated
name|long
name|getWriteBufferSize
parameter_list|()
function_decl|;
comment|/**    * Sets the size of the buffer in bytes.    *<p>    * If the new size is less than the current amount of data in the    * write buffer, the buffer gets flushed.    * @param writeBufferSize The new write buffer size, in bytes.    * @throws IOException if a remote or network exception occurs.    * @deprecated as of 1.0.0. Replaced by {@link BufferedMutator} and    * {@link BufferedMutatorParams#writeBufferSize(long)}    */
annotation|@
name|Deprecated
name|void
name|setWriteBufferSize
parameter_list|(
name|long
name|writeBufferSize
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

