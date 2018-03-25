begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|bucket
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
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|Cacheable
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
name|hfile
operator|.
name|CacheableDeserializer
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

begin_comment
comment|/**  * A class implementing IOEngine interface supports data services for  * {@link BucketCache}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|IOEngine
block|{
comment|/**    * @return true if persistent storage is supported for the cache when shutdown    */
name|boolean
name|isPersistent
parameter_list|()
function_decl|;
comment|/**    * IOEngine uses shared memory means, when reading Cacheable from it, those refers to the same    * memory area as used by the Engine for caching it.    * @return true when IOEngine using shared memory.    */
specifier|default
name|boolean
name|usesSharedMemory
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Transfers data from IOEngine to a Cacheable object.    * @param length How many bytes to be read from the offset    * @param offset The offset in the IO engine where the first byte to be read    * @param deserializer The deserializer to be used to make a Cacheable from the data.    * @return Cacheable    * @throws IOException    * @throws RuntimeException when the length of the ByteBuff read is less than 'len'    */
name|Cacheable
name|read
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|deserializer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Transfers data from the given byte buffer to IOEngine    * @param srcBuffer the given byte buffer from which bytes are to be read    * @param offset The offset in the IO engine where the first byte to be    *          written    * @throws IOException    */
name|void
name|write
parameter_list|(
name|ByteBuffer
name|srcBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Transfers the data from the given MultiByteBuffer to IOEngine    * @param srcBuffer the given MultiBytebufffers from which bytes are to be read    * @param offset the offset in the IO engine where the first byte to be written    * @throws IOException    */
name|void
name|write
parameter_list|(
name|ByteBuff
name|srcBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Sync the data to IOEngine after writing    * @throws IOException    */
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Shutdown the IOEngine    */
name|void
name|shutdown
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

