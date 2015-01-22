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

begin_comment
comment|/**  *<p>Used to communicate with a single HBase table similar to {@link Table} but meant for  * batched, asynchronous puts. Obtain an instance from a {@link Connection} and call  * {@link #close()} afterwards. Customizations can be applied to the {@code BufferedMutator} via  * the {@link BufferedMutatorParams}.  *</p>  *  *<p>Exception handling with asynchronously via the {@link BufferedMutator.ExceptionListener}.  * The default implementation is to throw the exception upon receipt. This behavior can be  * overridden with a custom implementation, provided as a parameter with  * {@link BufferedMutatorParams#listener(BufferedMutator.ExceptionListener)}.</p>  *  *<p>Map/Reduce jobs are good use cases for using {@code BufferedMutator}. Map/reduce jobs  * benefit from batching, but have no natural flush point. {@code BufferedMutator} receives the  * puts from the M/R job and will batch puts based on some heuristic, such as the accumulated size  * of the puts, and submit batches of puts asynchronously so that the M/R logic can continue  * without interruption.  *</p>  *  *<p>{@code BufferedMutator} can also be used on more exotic circumstances. Map/Reduce batch jobs  * will have a single {@code BufferedMutator} per thread. A single {@code BufferedMutator} can  * also be effectively used in high volume online systems to batch puts, with the caveat that  * extreme circumstances, such as JVM or machine failure, may cause some data loss.</p>  *  *<p>NOTE: This class replaces the functionality that used to be available via  * {@link HTableInterface#setAutoFlush(boolean)} set to {@code false}.  *</p>  *  *<p>See also the {@code BufferedMutatorExample} in the hbase-examples module.</p>  * @see ConnectionFactory  * @see Connection  * @since 1.0.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|BufferedMutator
extends|extends
name|Closeable
block|{
comment|/**    * Gets the fully qualified table name instance of the table that this BufferedMutator writes to.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
comment|/**    * Returns the {@link org.apache.hadoop.conf.Configuration} object used by this instance.    *<p>    * The reference returned is not a copy, so any change made to it will    * affect this instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Sends a {@link Mutation} to the table. The mutations will be buffered and sent over the    * wire as part of a batch. Currently only supports {@link Put} and {@link Delete} mutations.    *    * @param mutation The data to send.    * @throws IOException if a remote or network exception occurs.    */
name|void
name|mutate
parameter_list|(
name|Mutation
name|mutation
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Send some {@link Mutation}s to the table. The mutations will be buffered and sent over the    * wire as part of a batch. There is no guarantee of sending entire content of {@code mutations}    * in a single batch; it will be broken up according to the write buffer capacity.    *    * @param mutations The data to send.    * @throws IOException if a remote or network exception occurs.    */
name|void
name|mutate
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Mutation
argument_list|>
name|mutations
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs a {@link #flush()} and releases any resources held.    *    * @throws IOException if a remote or network exception occurs.    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Executes all the buffered, asynchronous {@link Mutation} operations and waits until they    * are done.    *    * @throws IOException if a remote or network exception occurs.    */
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns the maximum size in bytes of the write buffer for this HTable.    *<p>    * The default value comes from the configuration parameter {@code hbase.client.write.buffer}.    * @return The size of the write buffer in bytes.    */
name|long
name|getWriteBufferSize
parameter_list|()
function_decl|;
comment|/**    * Listens for asynchronous exceptions on a {@link BufferedMutator}.    */
interface|interface
name|ExceptionListener
block|{
specifier|public
name|void
name|onException
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|exception
parameter_list|,
name|BufferedMutator
name|mutator
parameter_list|)
throws|throws
name|RetriesExhaustedWithDetailsException
function_decl|;
block|}
block|}
end_interface

end_unit

