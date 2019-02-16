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
name|util
operator|.
name|Iterator
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
operator|.
name|MemoryType
import|;
end_import

begin_comment
comment|/**  * Block cache interface. Anything that implements the {@link Cacheable}  * interface can be put in the cache.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|BlockCache
extends|extends
name|Iterable
argument_list|<
name|CachedBlock
argument_list|>
block|{
comment|/**    * Add block to cache.    * @param cacheKey The block's cache key.    * @param buf The block contents wrapped in a ByteBuffer.    * @param inMemory Whether block should be treated as in-memory    */
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
function_decl|;
comment|/**    * Add block to cache (defaults to not in-memory).    * @param cacheKey The block's cache key.    * @param buf The object to cache.    */
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|)
function_decl|;
comment|/**    * Fetch block from cache.    * @param cacheKey Block to fetch.    * @param caching Whether this request has caching enabled (used for stats)    * @param repeat Whether this is a repeat lookup for the same block    *        (used to avoid double counting cache misses when doing double-check locking)    * @param updateCacheMetrics Whether to update cache metrics or not    * @return Block or null if block is not in 2 cache.    */
name|Cacheable
name|getBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|boolean
name|caching
parameter_list|,
name|boolean
name|repeat
parameter_list|,
name|boolean
name|updateCacheMetrics
parameter_list|)
function_decl|;
comment|/**    * Evict block from cache.    * @param cacheKey Block to evict    * @return true if block existed and was evicted, false if not    */
name|boolean
name|evictBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
function_decl|;
comment|/**    * Evicts all blocks for the given HFile.    *    * @return the number of blocks evicted    */
name|int
name|evictBlocksByHfileName
parameter_list|(
name|String
name|hfileName
parameter_list|)
function_decl|;
comment|/**    * Get the statistics for this block cache.    * @return Stats    */
name|CacheStats
name|getStats
parameter_list|()
function_decl|;
comment|/**    * Shutdown the cache.    */
name|void
name|shutdown
parameter_list|()
function_decl|;
comment|/**    * Returns the total size of the block cache, in bytes.    * @return size of cache, in bytes    */
name|long
name|size
parameter_list|()
function_decl|;
comment|/**    * Returns the Max size of the block cache, in bytes.    * @return size of cache, in bytes    */
name|long
name|getMaxSize
parameter_list|()
function_decl|;
comment|/**    * Returns the free size of the block cache, in bytes.    * @return free space in cache, in bytes    */
name|long
name|getFreeSize
parameter_list|()
function_decl|;
comment|/**    * Returns the occupied size of the block cache, in bytes.    * @return occupied space in cache, in bytes    */
name|long
name|getCurrentSize
parameter_list|()
function_decl|;
comment|/**    * Returns the occupied size of data blocks, in bytes.    * @return occupied space in cache, in bytes    */
name|long
name|getCurrentDataSize
parameter_list|()
function_decl|;
comment|/**    * Returns the number of blocks currently cached in the block cache.    * @return number of blocks in the cache    */
name|long
name|getBlockCount
parameter_list|()
function_decl|;
comment|/**   * Returns the number of data blocks currently cached in the block cache.   * @return number of blocks in the cache   */
name|long
name|getDataBlockCount
parameter_list|()
function_decl|;
comment|/**    * @return Iterator over the blocks in the cache.    */
annotation|@
name|Override
name|Iterator
argument_list|<
name|CachedBlock
argument_list|>
name|iterator
parameter_list|()
function_decl|;
comment|/**    * @return The list of sub blockcaches that make up this one; returns null if no sub caches.    */
name|BlockCache
index|[]
name|getBlockCaches
parameter_list|()
function_decl|;
comment|/**    * Called when the scanner using the block decides to decrease refCnt of block and return the    * block once its usage is over. This API should be called after the block is used, failing to do    * so may have adverse effects by preventing the blocks from being evicted because of which it    * will prevent new hot blocks from getting added to the block cache. The implementation of the    * BlockCache will decide on what to be done with the block based on the memory type of the    * block's {@link MemoryType}.<br>    *<br>    * Note that if two handlers read from backingMap in off-heap BucketCache at the same time, BC    * will return two ByteBuff, which reference to the same memory area in buckets, but wrapped by    * two different ByteBuff, and each of them has its own independent refCnt(=1). so here, if    * returnBlock with different blocks in two handlers, it has no problem. but if both the two    * handlers returnBlock with the same block, then the refCnt exception will happen here.<br>    * TODO let's unify the ByteBuff's refCnt and BucketEntry's refCnt in HBASE-21957, after that    * we'll just call the Cacheable#release instead of calling release in some path and calling    * returnBlock in other paths in current version.    * @param cacheKey the cache key of the block    * @param block the hfileblock to be returned    */
specifier|default
name|void
name|returnBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|block
parameter_list|)
block|{
name|block
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
end_interface

end_unit

