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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|HeapSize
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
name|slab
operator|.
name|SlabCache
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * DoubleBlockCache is an abstraction layer that combines two caches, the  * smaller onHeapCache and the larger offHeapCache. CacheBlock attempts to cache  * the block in both caches, while readblock reads first from the faster on heap  * cache before looking for the block in the off heap cache. Metrics are the  * combined size and hits and misses of both caches.  *  **/
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DoubleBlockCache
implements|implements
name|ResizableBlockCache
implements|,
name|HeapSize
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DoubleBlockCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LruBlockCache
name|onHeapCache
decl_stmt|;
specifier|private
specifier|final
name|SlabCache
name|offHeapCache
decl_stmt|;
specifier|private
specifier|final
name|CacheStats
name|stats
decl_stmt|;
comment|/**    * Default constructor. Specify maximum size and expected average block size    * (approximation is fine).    *<p>    * All other factors will be calculated based on defaults specified in this    * class.    *    * @param onHeapSize maximum size of the onHeapCache, in bytes.    * @param offHeapSize maximum size of the offHeapCache, in bytes.    * @param onHeapBlockSize average block size of the on heap cache.    * @param offHeapBlockSize average block size for the off heap cache    * @param conf configuration file. currently used only by the off heap cache.    */
specifier|public
name|DoubleBlockCache
parameter_list|(
name|long
name|onHeapSize
parameter_list|,
name|long
name|offHeapSize
parameter_list|,
name|long
name|onHeapBlockSize
parameter_list|,
name|long
name|offHeapBlockSize
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating on-heap cache of size "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|onHeapSize
argument_list|)
operator|+
literal|" with an average block size of "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|onHeapBlockSize
argument_list|)
argument_list|)
expr_stmt|;
name|onHeapCache
operator|=
operator|new
name|LruBlockCache
argument_list|(
name|onHeapSize
argument_list|,
name|onHeapBlockSize
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating off-heap cache of size "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|offHeapSize
argument_list|)
operator|+
literal|"with an average block size of "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|offHeapBlockSize
argument_list|)
argument_list|)
expr_stmt|;
name|offHeapCache
operator|=
operator|new
name|SlabCache
argument_list|(
name|offHeapSize
argument_list|,
name|offHeapBlockSize
argument_list|)
expr_stmt|;
name|offHeapCache
operator|.
name|addSlabByConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|stats
operator|=
operator|new
name|CacheStats
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
name|onHeapCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|,
name|inMemory
argument_list|)
expr_stmt|;
name|offHeapCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|buf
parameter_list|)
block|{
name|onHeapCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|)
expr_stmt|;
name|offHeapCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|buf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
name|Cacheable
name|cachedBlock
decl_stmt|;
if|if
condition|(
operator|(
name|cachedBlock
operator|=
name|onHeapCache
operator|.
name|getBlock
argument_list|(
name|cacheKey
argument_list|,
name|caching
argument_list|,
name|repeat
argument_list|,
name|updateCacheMetrics
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|updateCacheMetrics
condition|)
name|stats
operator|.
name|hit
argument_list|(
name|caching
argument_list|)
expr_stmt|;
return|return
name|cachedBlock
return|;
block|}
elseif|else
if|if
condition|(
operator|(
name|cachedBlock
operator|=
name|offHeapCache
operator|.
name|getBlock
argument_list|(
name|cacheKey
argument_list|,
name|caching
argument_list|,
name|repeat
argument_list|,
name|updateCacheMetrics
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|caching
condition|)
block|{
name|onHeapCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|cachedBlock
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|updateCacheMetrics
condition|)
name|stats
operator|.
name|hit
argument_list|(
name|caching
argument_list|)
expr_stmt|;
return|return
name|cachedBlock
return|;
block|}
if|if
condition|(
operator|!
name|repeat
operator|&&
name|updateCacheMetrics
condition|)
name|stats
operator|.
name|miss
argument_list|(
name|caching
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|evictBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
name|stats
operator|.
name|evict
argument_list|()
expr_stmt|;
name|boolean
name|cacheA
init|=
name|onHeapCache
operator|.
name|evictBlock
argument_list|(
name|cacheKey
argument_list|)
decl_stmt|;
name|boolean
name|cacheB
init|=
name|offHeapCache
operator|.
name|evictBlock
argument_list|(
name|cacheKey
argument_list|)
decl_stmt|;
name|boolean
name|evicted
init|=
name|cacheA
operator|||
name|cacheB
decl_stmt|;
if|if
condition|(
name|evicted
condition|)
block|{
name|stats
operator|.
name|evicted
argument_list|()
expr_stmt|;
block|}
return|return
name|evicted
return|;
block|}
annotation|@
name|Override
specifier|public
name|CacheStats
name|getStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|stats
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|onHeapCache
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|offHeapCache
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|onHeapCache
operator|.
name|heapSize
argument_list|()
operator|+
name|offHeapCache
operator|.
name|heapSize
argument_list|()
return|;
block|}
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
name|onHeapCache
operator|.
name|size
argument_list|()
operator|+
name|offHeapCache
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
name|onHeapCache
operator|.
name|getFreeSize
argument_list|()
operator|+
name|offHeapCache
operator|.
name|getFreeSize
argument_list|()
return|;
block|}
specifier|public
name|long
name|getCurrentSize
parameter_list|()
block|{
return|return
name|onHeapCache
operator|.
name|getCurrentSize
argument_list|()
operator|+
name|offHeapCache
operator|.
name|getCurrentSize
argument_list|()
return|;
block|}
specifier|public
name|long
name|getEvictedCount
parameter_list|()
block|{
return|return
name|onHeapCache
operator|.
name|getEvictedCount
argument_list|()
operator|+
name|offHeapCache
operator|.
name|getEvictedCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|evictBlocksByHfileName
parameter_list|(
name|String
name|hfileName
parameter_list|)
block|{
name|onHeapCache
operator|.
name|evictBlocksByHfileName
argument_list|(
name|hfileName
argument_list|)
expr_stmt|;
name|offHeapCache
operator|.
name|evictBlocksByHfileName
argument_list|(
name|hfileName
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
annotation|@
name|Override
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
block|{
return|return
name|onHeapCache
operator|.
name|getBlockCacheColumnFamilySummaries
argument_list|(
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBlockCount
parameter_list|()
block|{
return|return
name|onHeapCache
operator|.
name|getBlockCount
argument_list|()
operator|+
name|offHeapCache
operator|.
name|getBlockCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMaxSize
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|this
operator|.
name|onHeapCache
operator|.
name|setMaxSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

