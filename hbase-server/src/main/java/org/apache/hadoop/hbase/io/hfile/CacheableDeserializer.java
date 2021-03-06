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
name|io
operator|.
name|hfile
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
name|io
operator|.
name|ByteBuffAllocator
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
name|nio
operator|.
name|ByteBuff
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
comment|/**  * Interface for a deserializer. Throws an IOException if the serialized data is incomplete or  * wrong.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|CacheableDeserializer
parameter_list|<
name|T
extends|extends
name|Cacheable
parameter_list|>
block|{
comment|/**    * @param b ByteBuff to deserialize the Cacheable.    * @param allocator to manage NIO ByteBuffers for future allocation or de-allocation.    * @return T the deserialized object.    * @throws IOException    */
name|T
name|deserialize
parameter_list|(
name|ByteBuff
name|b
parameter_list|,
name|ByteBuffAllocator
name|allocator
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the identifier of this deserializer. Identifier is unique for each deserializer and    * generated by {@link CacheableDeserializerIdManager}    * @return identifier number of this cacheable deserializer    */
name|int
name|getDeserializerIdentifier
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

