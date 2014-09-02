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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Used to communicate with a single HBase table.  * Obtain an instance from an {@link HConnection}.  *  * @since 0.21.0  * @deprecated use {@link org.apache.hadoop.hbase.client.Table} instead  */
end_comment

begin_interface
annotation|@
name|Deprecated
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
comment|/**    * @deprecated Use {@link #incrementColumnValue(byte[], byte[], byte[], long, Durability)}    */
annotation|@
name|Deprecated
name|long
name|incrementColumnValue
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|long
name|amount
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @deprecated Use {@link #existsAll(java.util.List)}  instead.    */
annotation|@
name|Deprecated
name|Boolean
index|[]
name|exists
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
comment|/**    * See {@link #setAutoFlush(boolean, boolean)}    *    * @param autoFlush    *          Whether or not to enable 'auto-flush'.    * @deprecated in 0.96. When called with setAutoFlush(false), this function also    *  set clearBufferOnFail to true, which is unexpected but kept for historical reasons.    *  Replace it with setAutoFlush(false, false) if this is exactly what you want, or by    *  {@link #setAutoFlushTo(boolean)} for all other cases.    */
annotation|@
name|Deprecated
name|void
name|setAutoFlush
parameter_list|(
name|boolean
name|autoFlush
parameter_list|)
function_decl|;
comment|/**    * Turns 'auto-flush' on or off.    *<p>    * When enabled (default), {@link Put} operations don't get buffered/delayed    * and are immediately executed. Failed operations are not retried. This is    * slower but safer.    *<p>    * Turning off {@code #autoFlush} means that multiple {@link Put}s will be    * accepted before any RPC is actually sent to do the write operations. If the    * application dies before pending writes get flushed to HBase, data will be    * lost.    *<p>    * When you turn {@code #autoFlush} off, you should also consider the    * {@code #clearBufferOnFail} option. By default, asynchronous {@link Put}    * requests will be retried on failure until successful. However, this can    * pollute the writeBuffer and slow down batching performance. Additionally,    * you may want to issue a number of Put requests and call    * {@link #flushCommits()} as a barrier. In both use cases, consider setting    * clearBufferOnFail to true to erase the buffer after {@link #flushCommits()}    * has been called, regardless of success.    *<p>    * In other words, if you call {@code #setAutoFlush(false)}; HBase will retry N time for each    *  flushCommit, including the last one when closing the table. This is NOT recommended,    *  most of the time you want to call {@code #setAutoFlush(false, true)}.    *    * @param autoFlush    *          Whether or not to enable 'auto-flush'.    * @param clearBufferOnFail    *          Whether to keep Put failures in the writeBuffer. If autoFlush is true, then    *          the value of this parameter is ignored and clearBufferOnFail is set to true.    *          Setting clearBufferOnFail to false is deprecated since 0.96.    * @deprecated in 0.99 since setting clearBufferOnFail is deprecated. Use    *  {@link #setAutoFlushTo(boolean)}} instead.    * @see #flushCommits    */
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
comment|/**    * Return the row that matches<i>row</i> exactly,    * or the one that immediately precedes it.    *    * @param row A row key.    * @param family Column family to include in the {@link Result}.    * @throws IOException if a remote or network exception occurs.    * @since 0.20.0    *    * @deprecated As of version 0.92 this method is deprecated without    * replacement. Since version 0.96+, you can use reversed scan.    * getRowOrBefore is used internally to find entries in hbase:meta and makes    * various assumptions about the table (which are true for hbase:meta but not    * in general) to be efficient.    */
annotation|@
name|Deprecated
name|Result
name|getRowOrBefore
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

