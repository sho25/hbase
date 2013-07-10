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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|HeapSize
import|;
end_import

begin_comment
comment|/**  * Cacheable is an interface that allows for an object to be cached. If using an  * on heap cache, just use heapsize. If using an off heap cache, Cacheable  * provides methods for serialization of the object.  *  * Some objects cannot be moved off heap, those objects will return a  * getSerializedLength() of 0.  *  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Cacheable
extends|extends
name|HeapSize
block|{
comment|/**    * Returns the length of the ByteBuffer required to serialized the object. If the    * object cannot be serialized, it should also return 0.    *    * @return int length in bytes of the serialized form.    */
name|int
name|getSerializedLength
parameter_list|()
function_decl|;
comment|/**    * Serializes its data into destination.    */
name|void
name|serialize
parameter_list|(
name|ByteBuffer
name|destination
parameter_list|)
function_decl|;
comment|/**    * Returns CacheableDeserializer instance which reconstructs original object from ByteBuffer.    *    * @return CacheableDeserialzer instance.    */
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|getDeserializer
parameter_list|()
function_decl|;
comment|/**    * @return the block type of this cached HFile block    */
name|BlockType
name|getBlockType
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

