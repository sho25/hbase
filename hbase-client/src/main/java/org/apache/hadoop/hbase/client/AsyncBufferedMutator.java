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
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_comment
comment|/**  * Used to communicate with a single HBase table in batches. Obtain an instance from a  * {@link AsyncConnection} and call {@link #close()} afterwards.  *<p>  * The implementation is required to be thread safe.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|AsyncBufferedMutator
extends|extends
name|Closeable
block|{
comment|/**    * Gets the fully qualified table name instance of the table that this    * {@code AsyncBufferedMutator} writes to.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
comment|/**    * Returns the {@link org.apache.hadoop.conf.Configuration} object used by this instance.    *<p>    * The reference returned is not a copy, so any change made to it will affect this instance.    */
name|Configuration
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Sends a {@link Mutation} to the table. The mutations will be buffered and sent over the wire as    * part of a batch. Currently only supports {@link Put} and {@link Delete} mutations.    * @param mutation The data to send.    */
specifier|default
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
name|mutate
parameter_list|(
name|Mutation
name|mutation
parameter_list|)
block|{
return|return
name|Iterables
operator|.
name|getOnlyElement
argument_list|(
name|mutate
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|mutation
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Send some {@link Mutation}s to the table. The mutations will be buffered and sent over the wire    * as part of a batch. There is no guarantee of sending entire content of {@code mutations} in a    * single batch, the implementations are free to break it up according to the write buffer    * capacity.    * @param mutations The data to send.    */
name|List
argument_list|<
name|CompletableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
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
function_decl|;
comment|/**    * Executes all the buffered, asynchronous operations.    */
name|void
name|flush
parameter_list|()
function_decl|;
comment|/**    * Performs a {@link #flush()} and releases any resources held.    */
annotation|@
name|Override
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Returns the maximum size in bytes of the write buffer.    *<p>    * The default value comes from the configuration parameter {@code hbase.client.write.buffer}.    * @return The size of the write buffer in bytes.    */
name|long
name|getWriteBufferSize
parameter_list|()
function_decl|;
comment|/**    * Returns the periodical flush interval, 0 means disabled.    */
specifier|default
name|long
name|getPeriodicalFlushTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
block|}
end_interface

end_unit

