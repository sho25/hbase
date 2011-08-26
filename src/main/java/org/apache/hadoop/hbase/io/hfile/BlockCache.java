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

begin_comment
comment|/**  * Block cache interface. Anything that implements the {@link Cacheable}  * interface can be put in the cache.  *  * TODO: Add filename or hash of filename to block cache key.  */
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
name|Cacheable
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
function_decl|;
comment|/**    * Add block to cache (defaults to not in-memory).    * @param blockName Zero-based file block number.    * @param buf The object to cache.    */
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|Cacheable
name|buf
parameter_list|)
function_decl|;
comment|/**    * Fetch block from cache.    * @param blockName Block number to fetch.    * @param caching Whether this request has caching enabled (used for stats)    * @return Block or null if block is not in 2 cache.    */
specifier|public
name|Cacheable
name|getBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|boolean
name|caching
parameter_list|)
function_decl|;
comment|/**    * Evict block from cache.    * @param blockName Block name to evict    * @return true if block existed and was evicted, false if not    */
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|String
name|blockName
parameter_list|)
function_decl|;
comment|/**    * Evicts all blocks with name starting with the given prefix. This is    * necessary in cases we need to evict all blocks that belong to a particular    * HFile. In HFile v2 all blocks consist of the storefile name (UUID), an    * underscore, and the block offset in the file. An efficient implementation    * would avoid scanning all blocks in the cache.    *    * @return the number of blocks evicted    */
specifier|public
name|int
name|evictBlocksByPrefix
parameter_list|(
name|String
name|string
parameter_list|)
function_decl|;
comment|/**    * Get the statistics for this block cache.    * @return    */
specifier|public
name|CacheStats
name|getStats
parameter_list|()
function_decl|;
comment|/**    * Shutdown the cache.    */
specifier|public
name|void
name|shutdown
parameter_list|()
function_decl|;
specifier|public
name|long
name|size
parameter_list|()
function_decl|;
specifier|public
name|long
name|getFreeSize
parameter_list|()
function_decl|;
specifier|public
name|long
name|getCurrentSize
parameter_list|()
function_decl|;
specifier|public
name|long
name|getEvictedCount
parameter_list|()
function_decl|;
comment|/**    * Performs a BlockCache summary and returns a List of BlockCacheColumnFamilySummary objects.    * This method could be fairly heavyweight in that it evaluates the entire HBase file-system    * against what is in the RegionServer BlockCache.    *<br><br>    * The contract of this interface is to return the List in sorted order by Table name, then    * ColumnFamily.    *    * @param conf HBaseConfiguration    * @return List of BlockCacheColumnFamilySummary    * @throws IOException exception    */
specifier|public
name|List
argument_list|<
name|BlockCacheColumnFamilySummary
argument_list|>
name|getBlockCacheColumnFamilySummaries
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

