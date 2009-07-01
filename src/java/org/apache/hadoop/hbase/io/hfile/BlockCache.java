begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Block cache interface.  * TODO: Add filename or hash of filename to block cache key.  */
end_comment

begin_interface
specifier|public
interface|interface
name|BlockCache
block|{
comment|/**    * Add block to cache.    * @param blockName Zero-based file block number.    * @param buf The block contents wrapped in a ByteBuffer.    * @param inMemory Whether block should be treated as in-memory    */
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|ByteBuffer
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
function_decl|;
comment|/**    * Add block to cache (defaults to not in-memory).    * @param blockName Zero-based file block number.    * @param buf The block contents wrapped in a ByteBuffer.    */
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|ByteBuffer
name|buf
parameter_list|)
function_decl|;
comment|/**    * Fetch block from cache.    * @param blockName Block number to fetch.    * @return Block or null if block is not in the cache.    */
specifier|public
name|ByteBuffer
name|getBlock
parameter_list|(
name|String
name|blockName
parameter_list|)
function_decl|;
comment|/**    * Shutdown the cache.    */
specifier|public
name|void
name|shutdown
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

