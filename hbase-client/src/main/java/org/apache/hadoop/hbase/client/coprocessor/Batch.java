begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|coprocessor
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A collection of interfaces and utilities used for interacting with custom RPC  * interfaces exposed by Coprocessors.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|Batch
block|{
comment|/**    * Defines a unit of work to be executed.    *    *<p>    * When used with    * {@link org.apache.hadoop.hbase.client.Table#coprocessorService(Class, byte[], byte[],    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call)}    * the implementations {@link Batch.Call#call(Object)} method will be invoked    * with a proxy to each region's coprocessor {@link com.google.protobuf.Service} implementation.    *</p>    * @see org.apache.hadoop.hbase.client.coprocessor.Batch    * @see org.apache.hadoop.hbase.client.Table#coprocessorService(byte[])    * @see org.apache.hadoop.hbase.client.Table#coprocessorService(Class, byte[], byte[],    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call)    * @param<T> the instance type to be passed to    * {@link Batch.Call#call(Object)}    * @param<R> the return type from {@link Batch.Call#call(Object)}    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Call
parameter_list|<
name|T
parameter_list|,
name|R
parameter_list|>
block|{
name|R
name|call
parameter_list|(
name|T
name|instance
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Defines a generic callback to be triggered for each {@link Batch.Call#call(Object)}    * result.    *    *<p>    * When used with    * {@link org.apache.hadoop.hbase.client.Table#coprocessorService(Class, byte[], byte[],    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call)}    * the implementation's {@link Batch.Callback#update(byte[], byte[], Object)}    * method will be called with the {@link Batch.Call#call(Object)} return value    * from each region in the selected range.    *</p>    * @param<R> the return type from the associated {@link Batch.Call#call(Object)}    * @see org.apache.hadoop.hbase.client.Table#coprocessorService(Class, byte[], byte[],    * org.apache.hadoop.hbase.client.coprocessor.Batch.Call)    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Callback
parameter_list|<
name|R
parameter_list|>
block|{
name|void
name|update
parameter_list|(
name|byte
index|[]
name|region
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|R
name|result
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

