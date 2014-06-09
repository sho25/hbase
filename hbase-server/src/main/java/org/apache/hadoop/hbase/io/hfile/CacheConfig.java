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
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryUsage
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
name|HColumnDescriptor
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
name|HConstants
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
name|BlockType
operator|.
name|BlockCategory
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
name|bucket
operator|.
name|BucketCache
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
name|hbase
operator|.
name|util
operator|.
name|DirectMemoryUtils
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Stores all of the cache objects and configuration for a single HFile.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CacheConfig
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CacheConfig
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Configuration key to cache data blocks on write. There are separate    * switches for bloom blocks and non-root index blocks.    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_BLOCKS_ON_WRITE_KEY
init|=
literal|"hbase.rs.cacheblocksonwrite"
decl_stmt|;
comment|/**    * Configuration key to cache leaf and intermediate-level index blocks on    * write.    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
init|=
literal|"hfile.block.index.cacheonwrite"
decl_stmt|;
comment|/**    * Configuration key to cache compound bloom filter blocks on write.    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
init|=
literal|"hfile.block.bloom.cacheonwrite"
decl_stmt|;
comment|/**    * TODO: Implement this (jgray)    * Configuration key to cache data blocks in compressed format.    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
init|=
literal|"hbase.rs.blockcache.cachedatacompressed"
decl_stmt|;
comment|/**    * Configuration key to evict all blocks of a given file from the block cache    * when the file is closed.    */
specifier|public
specifier|static
specifier|final
name|String
name|EVICT_BLOCKS_ON_CLOSE_KEY
init|=
literal|"hbase.rs.evictblocksonclose"
decl_stmt|;
comment|/**    * Configuration keys for Bucket cache    */
comment|/**    * Current ioengine options in include: heap, offheap and file:PATH (where PATH is the path    * to the file that will host the file-based cache.  See BucketCache#getIOEngineFromName() for    * list of supported ioengine options.    *     *<p>Set this option and a non-zero {@link #BUCKET_CACHE_SIZE_KEY} to enable bucket cache.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_IOENGINE_KEY
init|=
literal|"hbase.bucketcache.ioengine"
decl_stmt|;
comment|/**    * When using bucket cache, this is a float that EITHER represents a percentage of total heap    * memory size to give to the cache (if< 1.0) OR, it is the capacity in megabytes of the cache.    *     *<p>The resultant size is further divided if {@link #BUCKET_CACHE_COMBINED_KEY} is set (It is    * set by default. When false, bucket cache serves as an "L2" cache to the "L1"    * {@link LruBlockCache}).  The percentage is set in    * with {@link #BUCKET_CACHE_COMBINED_PERCENTAGE_KEY} float.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_SIZE_KEY
init|=
literal|"hbase.bucketcache.size"
decl_stmt|;
comment|/**    * If the chosen ioengine can persist its state across restarts, the path to the file to    * persist to.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_PERSISTENT_PATH_KEY
init|=
literal|"hbase.bucketcache.persistent.path"
decl_stmt|;
comment|/**    * If the bucket cache is used in league with the lru on-heap block cache (meta blocks such    * as indices and blooms are kept in the lru blockcache and the data blocks in the    * bucket cache).    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_COMBINED_KEY
init|=
literal|"hbase.bucketcache.combinedcache.enabled"
decl_stmt|;
comment|/**    * A float which designates how much of the overall cache to give to bucket cache    * and how much to on-heap lru cache when {@link #BUCKET_CACHE_COMBINED_KEY} is set.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_COMBINED_PERCENTAGE_KEY
init|=
literal|"hbase.bucketcache.percentage.in.combinedcache"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_WRITER_THREADS_KEY
init|=
literal|"hbase.bucketcache.writer.threads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_WRITER_QUEUE_KEY
init|=
literal|"hbase.bucketcache.writer.queuelength"
decl_stmt|;
comment|/**    * A comma-delimited array of values for use as bucket sizes.    */
specifier|public
specifier|static
specifier|final
name|String
name|BUCKET_CACHE_BUCKETS_KEY
init|=
literal|"hbase.bucketcache.bucket.sizes"
decl_stmt|;
comment|/**    * Defaults for Bucket cache    */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_BUCKET_CACHE_COMBINED
init|=
literal|true
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BUCKET_CACHE_WRITER_THREADS
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BUCKET_CACHE_WRITER_QUEUE
init|=
literal|64
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_BUCKET_CACHE_COMBINED_PERCENTAGE
init|=
literal|0.9f
decl_stmt|;
comment|/**    * Setting this float to a non-null value turns on {@link DoubleBlockCache}    * which makes use of the {@link LruBlockCache} and {@link SlabCache}.    *     * The float value of between 0 and 1 will be multiplied against the setting for    *<code>-XX:MaxDirectMemorySize</code> to figure what size of the offheap allocation to give    * over to slab cache.    *     * Slab cache has been little used and is likely to be deprecated in the near future.    */
specifier|public
specifier|static
specifier|final
name|String
name|SLAB_CACHE_OFFHEAP_PERCENTAGE_KEY
init|=
literal|"hbase.offheapcache.percentage"
decl_stmt|;
comment|/**    * Configuration key to prefetch all blocks of a given file into the block cache    * when the file is opened.    */
specifier|public
specifier|static
specifier|final
name|String
name|PREFETCH_BLOCKS_ON_OPEN_KEY
init|=
literal|"hbase.rs.prefetchblocksonopen"
decl_stmt|;
comment|// Defaults
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_CACHE_DATA_ON_READ
init|=
literal|true
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_CACHE_DATA_ON_WRITE
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_IN_MEMORY
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_CACHE_INDEXES_ON_WRITE
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_CACHE_BLOOMS_ON_WRITE
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_EVICT_ON_CLOSE
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_COMPRESSED_CACHE
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_PREFETCH_ON_OPEN
init|=
literal|false
decl_stmt|;
comment|/** Local reference to the block cache, null if completely disabled */
specifier|private
specifier|final
name|BlockCache
name|blockCache
decl_stmt|;
comment|/**    * Whether blocks should be cached on read (default is on if there is a    * cache but this can be turned off on a per-family or per-request basis).    * If off we will STILL cache meta blocks; i.e. INDEX and BLOOM types.    * This cannot be disabled.    */
specifier|private
name|boolean
name|cacheDataOnRead
decl_stmt|;
comment|/** Whether blocks should be flagged as in-memory when being cached */
specifier|private
specifier|final
name|boolean
name|inMemory
decl_stmt|;
comment|/** Whether data blocks should be cached when new files are written */
specifier|private
name|boolean
name|cacheDataOnWrite
decl_stmt|;
comment|/** Whether index blocks should be cached when new files are written */
specifier|private
specifier|final
name|boolean
name|cacheIndexesOnWrite
decl_stmt|;
comment|/** Whether compound bloom filter blocks should be cached on write */
specifier|private
specifier|final
name|boolean
name|cacheBloomsOnWrite
decl_stmt|;
comment|/** Whether blocks of a file should be evicted when the file is closed */
specifier|private
name|boolean
name|evictOnClose
decl_stmt|;
comment|/** Whether data blocks should be stored in compressed form in the cache */
specifier|private
specifier|final
name|boolean
name|cacheCompressed
decl_stmt|;
comment|/** Whether data blocks should be prefetched into the cache */
specifier|private
specifier|final
name|boolean
name|prefetchOnOpen
decl_stmt|;
comment|/**    * Create a cache configuration using the specified configuration object and    * family descriptor.    * @param conf hbase configuration    * @param family column family configuration    */
specifier|public
name|CacheConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HColumnDescriptor
name|family
parameter_list|)
block|{
name|this
argument_list|(
name|CacheConfig
operator|.
name|instantiateBlockCache
argument_list|(
name|conf
argument_list|)
argument_list|,
name|family
operator|.
name|isBlockCacheEnabled
argument_list|()
argument_list|,
name|family
operator|.
name|isInMemory
argument_list|()
argument_list|,
comment|// For the following flags we enable them regardless of per-schema settings
comment|// if they are enabled in the global configuration.
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_DATA_ON_WRITE
argument_list|)
operator|||
name|family
operator|.
name|shouldCacheDataOnWrite
argument_list|()
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_INDEXES_ON_WRITE
argument_list|)
operator|||
name|family
operator|.
name|shouldCacheIndexesOnWrite
argument_list|()
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_BLOOMS_ON_WRITE
argument_list|)
operator|||
name|family
operator|.
name|shouldCacheBloomsOnWrite
argument_list|()
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|EVICT_BLOCKS_ON_CLOSE_KEY
argument_list|,
name|DEFAULT_EVICT_ON_CLOSE
argument_list|)
operator|||
name|family
operator|.
name|shouldEvictBlocksOnClose
argument_list|()
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
argument_list|,
name|DEFAULT_COMPRESSED_CACHE
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|PREFETCH_BLOCKS_ON_OPEN_KEY
argument_list|,
name|DEFAULT_PREFETCH_ON_OPEN
argument_list|)
operator|||
name|family
operator|.
name|shouldPrefetchBlocksOnOpen
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a cache configuration using the specified configuration object and    * defaults for family level settings.    * @param conf hbase configuration    */
specifier|public
name|CacheConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|CacheConfig
operator|.
name|instantiateBlockCache
argument_list|(
name|conf
argument_list|)
argument_list|,
name|DEFAULT_CACHE_DATA_ON_READ
argument_list|,
name|DEFAULT_IN_MEMORY
argument_list|,
comment|// This is a family-level setting so can't be set
comment|// strictly from conf
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_DATA_ON_WRITE
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_INDEXES_ON_WRITE
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_BLOOMS_ON_WRITE
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|EVICT_BLOCKS_ON_CLOSE_KEY
argument_list|,
name|DEFAULT_EVICT_ON_CLOSE
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
argument_list|,
name|DEFAULT_COMPRESSED_CACHE
argument_list|)
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|PREFETCH_BLOCKS_ON_OPEN_KEY
argument_list|,
name|DEFAULT_PREFETCH_ON_OPEN
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a block cache configuration with the specified cache and    * configuration parameters.    * @param blockCache reference to block cache, null if completely disabled    * @param cacheDataOnRead whether DATA blocks should be cached on read (we always cache INDEX    * blocks and BLOOM blocks; this cannot be disabled).    * @param inMemory whether blocks should be flagged as in-memory    * @param cacheDataOnWrite whether data blocks should be cached on write    * @param cacheIndexesOnWrite whether index blocks should be cached on write    * @param cacheBloomsOnWrite whether blooms should be cached on write    * @param evictOnClose whether blocks should be evicted when HFile is closed    * @param cacheCompressed whether to store blocks as compressed in the cache    * @param prefetchOnOpen whether to prefetch blocks upon open    */
name|CacheConfig
parameter_list|(
specifier|final
name|BlockCache
name|blockCache
parameter_list|,
specifier|final
name|boolean
name|cacheDataOnRead
parameter_list|,
specifier|final
name|boolean
name|inMemory
parameter_list|,
specifier|final
name|boolean
name|cacheDataOnWrite
parameter_list|,
specifier|final
name|boolean
name|cacheIndexesOnWrite
parameter_list|,
specifier|final
name|boolean
name|cacheBloomsOnWrite
parameter_list|,
specifier|final
name|boolean
name|evictOnClose
parameter_list|,
specifier|final
name|boolean
name|cacheCompressed
parameter_list|,
specifier|final
name|boolean
name|prefetchOnOpen
parameter_list|)
block|{
name|this
operator|.
name|blockCache
operator|=
name|blockCache
expr_stmt|;
name|this
operator|.
name|cacheDataOnRead
operator|=
name|cacheDataOnRead
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
name|inMemory
expr_stmt|;
name|this
operator|.
name|cacheDataOnWrite
operator|=
name|cacheDataOnWrite
expr_stmt|;
name|this
operator|.
name|cacheIndexesOnWrite
operator|=
name|cacheIndexesOnWrite
expr_stmt|;
name|this
operator|.
name|cacheBloomsOnWrite
operator|=
name|cacheBloomsOnWrite
expr_stmt|;
name|this
operator|.
name|evictOnClose
operator|=
name|evictOnClose
expr_stmt|;
name|this
operator|.
name|cacheCompressed
operator|=
name|cacheCompressed
expr_stmt|;
name|this
operator|.
name|prefetchOnOpen
operator|=
name|prefetchOnOpen
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a cache configuration copied from the specified configuration.    * @param cacheConf    */
specifier|public
name|CacheConfig
parameter_list|(
name|CacheConfig
name|cacheConf
parameter_list|)
block|{
name|this
argument_list|(
name|cacheConf
operator|.
name|blockCache
argument_list|,
name|cacheConf
operator|.
name|cacheDataOnRead
argument_list|,
name|cacheConf
operator|.
name|inMemory
argument_list|,
name|cacheConf
operator|.
name|cacheDataOnWrite
argument_list|,
name|cacheConf
operator|.
name|cacheIndexesOnWrite
argument_list|,
name|cacheConf
operator|.
name|cacheBloomsOnWrite
argument_list|,
name|cacheConf
operator|.
name|evictOnClose
argument_list|,
name|cacheConf
operator|.
name|cacheCompressed
argument_list|,
name|cacheConf
operator|.
name|prefetchOnOpen
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks whether the block cache is enabled.    */
specifier|public
name|boolean
name|isBlockCacheEnabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|blockCache
operator|!=
literal|null
return|;
block|}
comment|/**    * Returns the block cache.    * @return the block cache, or null if caching is completely disabled    */
specifier|public
name|BlockCache
name|getBlockCache
parameter_list|()
block|{
return|return
name|this
operator|.
name|blockCache
return|;
block|}
comment|/**    * Returns whether the DATA blocks of this HFile should be cached on read or not (we always    * cache the meta blocks, the INDEX and BLOOM blocks).    * @return true if blocks should be cached on read, false if not    */
specifier|public
name|boolean
name|shouldCacheDataOnRead
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|cacheDataOnRead
return|;
block|}
comment|/**    * Should we cache a block of a particular category? We always cache    * important blocks such as index blocks, as long as the block cache is    * available.    */
specifier|public
name|boolean
name|shouldCacheBlockOnRead
parameter_list|(
name|BlockCategory
name|category
parameter_list|)
block|{
name|boolean
name|shouldCache
init|=
name|isBlockCacheEnabled
argument_list|()
operator|&&
operator|(
name|cacheDataOnRead
operator|||
name|category
operator|==
name|BlockCategory
operator|.
name|INDEX
operator|||
name|category
operator|==
name|BlockCategory
operator|.
name|BLOOM
operator|||
operator|(
name|prefetchOnOpen
operator|&&
operator|(
name|category
operator|!=
name|BlockCategory
operator|.
name|META
operator|&&
name|category
operator|!=
name|BlockCategory
operator|.
name|UNKNOWN
operator|)
operator|)
operator|)
decl_stmt|;
return|return
name|shouldCache
return|;
block|}
comment|/**    * @return true if blocks in this file should be flagged as in-memory    */
specifier|public
name|boolean
name|isInMemory
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|inMemory
return|;
block|}
comment|/**    * @return true if data blocks should be written to the cache when an HFile is    *         written, false if not    */
specifier|public
name|boolean
name|shouldCacheDataOnWrite
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|cacheDataOnWrite
return|;
block|}
comment|/**    * Only used for testing.    * @param cacheDataOnWrite whether data blocks should be written to the cache    *                         when an HFile is written    */
specifier|public
name|void
name|setCacheDataOnWrite
parameter_list|(
name|boolean
name|cacheDataOnWrite
parameter_list|)
block|{
name|this
operator|.
name|cacheDataOnWrite
operator|=
name|cacheDataOnWrite
expr_stmt|;
block|}
comment|/**    * @return true if index blocks should be written to the cache when an HFile    *         is written, false if not    */
specifier|public
name|boolean
name|shouldCacheIndexesOnWrite
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|cacheIndexesOnWrite
return|;
block|}
comment|/**    * @return true if bloom blocks should be written to the cache when an HFile    *         is written, false if not    */
specifier|public
name|boolean
name|shouldCacheBloomsOnWrite
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|cacheBloomsOnWrite
return|;
block|}
comment|/**    * @return true if blocks should be evicted from the cache when an HFile    *         reader is closed, false if not    */
specifier|public
name|boolean
name|shouldEvictOnClose
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|evictOnClose
return|;
block|}
comment|/**    * Only used for testing.    * @param evictOnClose whether blocks should be evicted from the cache when an    *                     HFile reader is closed    */
specifier|public
name|void
name|setEvictOnClose
parameter_list|(
name|boolean
name|evictOnClose
parameter_list|)
block|{
name|this
operator|.
name|evictOnClose
operator|=
name|evictOnClose
expr_stmt|;
block|}
comment|/**    * @return true if blocks should be compressed in the cache, false if not    */
specifier|public
name|boolean
name|shouldCacheCompressed
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|cacheCompressed
return|;
block|}
comment|/**    * @return true if blocks should be prefetched into the cache on open, false if not    */
specifier|public
name|boolean
name|shouldPrefetchOnOpen
parameter_list|()
block|{
return|return
name|isBlockCacheEnabled
argument_list|()
operator|&&
name|this
operator|.
name|prefetchOnOpen
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isBlockCacheEnabled
argument_list|()
condition|)
block|{
return|return
literal|"CacheConfig:disabled"
return|;
block|}
return|return
literal|"blockCache="
operator|+
name|getBlockCache
argument_list|()
operator|+
literal|", cacheDataOnRead="
operator|+
name|shouldCacheDataOnRead
argument_list|()
operator|+
literal|", cacheDataOnWrite="
operator|+
name|shouldCacheDataOnWrite
argument_list|()
operator|+
literal|", cacheIndexesOnWrite="
operator|+
name|shouldCacheIndexesOnWrite
argument_list|()
operator|+
literal|", cacheBloomsOnWrite="
operator|+
name|shouldCacheBloomsOnWrite
argument_list|()
operator|+
literal|", cacheEvictOnClose="
operator|+
name|shouldEvictOnClose
argument_list|()
operator|+
literal|", cacheCompressed="
operator|+
name|shouldCacheCompressed
argument_list|()
operator|+
literal|", prefetchOnOpen="
operator|+
name|shouldPrefetchOnOpen
argument_list|()
return|;
block|}
comment|// Static block cache reference and methods
comment|/**    * Static reference to the block cache, or null if no caching should be used    * at all.    */
comment|// Clear this if in tests you'd make more than one block cache instance.
annotation|@
name|VisibleForTesting
specifier|static
name|BlockCache
name|GLOBAL_BLOCK_CACHE_INSTANCE
decl_stmt|;
comment|/** Boolean whether we have disabled the block cache entirely. */
annotation|@
name|VisibleForTesting
specifier|static
name|boolean
name|blockCacheDisabled
init|=
literal|false
decl_stmt|;
comment|/**    * Returns the block cache or<code>null</code> in case none should be used.    *    * @param conf  The current configuration.    * @return The block cache or<code>null</code>.    */
specifier|public
specifier|static
specifier|synchronized
name|BlockCache
name|instantiateBlockCache
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|!=
literal|null
condition|)
return|return
name|GLOBAL_BLOCK_CACHE_INSTANCE
return|;
if|if
condition|(
name|blockCacheDisabled
condition|)
return|return
literal|null
return|;
name|float
name|cachePercentage
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|cachePercentage
operator|<=
literal|0.0001f
condition|)
block|{
name|blockCacheDisabled
operator|=
literal|true
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|cachePercentage
operator|>
literal|1.0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
operator|+
literal|" must be between 0.0 and 1.0, and not> 1.0"
argument_list|)
throw|;
block|}
comment|// Calculate the amount of heap to give the heap.
name|MemoryUsage
name|mu
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
decl_stmt|;
name|long
name|lruCacheSize
init|=
call|(
name|long
call|)
argument_list|(
name|mu
operator|.
name|getMax
argument_list|()
operator|*
name|cachePercentage
argument_list|)
decl_stmt|;
name|int
name|blockSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.offheapcache.minblocksize"
argument_list|,
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|)
decl_stmt|;
name|long
name|slabCacheOffHeapCacheSize
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|SLAB_CACHE_OFFHEAP_PERCENTAGE_KEY
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|?
literal|0
else|:
call|(
name|long
call|)
argument_list|(
name|conf
operator|.
name|getFloat
argument_list|(
name|SLAB_CACHE_OFFHEAP_PERCENTAGE_KEY
argument_list|,
operator|(
name|float
operator|)
literal|0
argument_list|)
operator|*
name|DirectMemoryUtils
operator|.
name|getDirectMemorySize
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|slabCacheOffHeapCacheSize
operator|<=
literal|0
condition|)
block|{
name|String
name|bucketCacheIOEngineName
init|=
name|conf
operator|.
name|get
argument_list|(
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|float
name|bucketCachePercentage
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
literal|0F
argument_list|)
decl_stmt|;
comment|// A percentage of max heap size or a absolute value with unit megabytes
name|long
name|bucketCacheSize
init|=
call|(
name|long
call|)
argument_list|(
name|bucketCachePercentage
operator|<
literal|1
condition|?
name|mu
operator|.
name|getMax
argument_list|()
operator|*
name|bucketCachePercentage
else|:
name|bucketCachePercentage
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
decl_stmt|;
name|boolean
name|combinedWithLru
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|BUCKET_CACHE_COMBINED_KEY
argument_list|,
name|DEFAULT_BUCKET_CACHE_COMBINED
argument_list|)
decl_stmt|;
name|BucketCache
name|bucketCache
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|bucketCacheIOEngineName
operator|!=
literal|null
operator|&&
name|bucketCacheSize
operator|>
literal|0
condition|)
block|{
name|int
name|writerThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|BUCKET_CACHE_WRITER_THREADS_KEY
argument_list|,
name|DEFAULT_BUCKET_CACHE_WRITER_THREADS
argument_list|)
decl_stmt|;
name|int
name|writerQueueLen
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|BUCKET_CACHE_WRITER_QUEUE_KEY
argument_list|,
name|DEFAULT_BUCKET_CACHE_WRITER_QUEUE
argument_list|)
decl_stmt|;
name|String
name|persistentPath
init|=
name|conf
operator|.
name|get
argument_list|(
name|BUCKET_CACHE_PERSISTENT_PATH_KEY
argument_list|)
decl_stmt|;
name|float
name|combinedPercentage
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|BUCKET_CACHE_COMBINED_PERCENTAGE_KEY
argument_list|,
name|DEFAULT_BUCKET_CACHE_COMBINED_PERCENTAGE
argument_list|)
decl_stmt|;
name|String
index|[]
name|configuredBucketSizes
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|BUCKET_CACHE_BUCKETS_KEY
argument_list|)
decl_stmt|;
name|int
index|[]
name|bucketSizes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|configuredBucketSizes
operator|!=
literal|null
condition|)
block|{
name|bucketSizes
operator|=
operator|new
name|int
index|[
name|configuredBucketSizes
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|configuredBucketSizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|bucketSizes
index|[
name|i
index|]
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|configuredBucketSizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|combinedWithLru
condition|)
block|{
name|lruCacheSize
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
literal|1
operator|-
name|combinedPercentage
operator|)
operator|*
name|bucketCacheSize
argument_list|)
expr_stmt|;
name|bucketCacheSize
operator|=
call|(
name|long
call|)
argument_list|(
name|combinedPercentage
operator|*
name|bucketCacheSize
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|int
name|ioErrorsTolerationDuration
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.bucketcache.ioengine.errors.tolerated.duration"
argument_list|,
name|BucketCache
operator|.
name|DEFAULT_ERROR_TOLERATION_DURATION
argument_list|)
decl_stmt|;
name|bucketCache
operator|=
operator|new
name|BucketCache
argument_list|(
name|bucketCacheIOEngineName
argument_list|,
name|bucketCacheSize
argument_list|,
name|blockSize
argument_list|,
name|bucketSizes
argument_list|,
name|writerThreads
argument_list|,
name|writerQueueLen
argument_list|,
name|persistentPath
argument_list|,
name|ioErrorsTolerationDuration
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't instantiate bucket cache"
argument_list|,
name|ioex
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioex
argument_list|)
throw|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Allocating LruBlockCache size="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|lruCacheSize
argument_list|)
operator|+
literal|", blockSize="
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|blockSize
argument_list|)
argument_list|)
expr_stmt|;
name|LruBlockCache
name|lruCache
init|=
operator|new
name|LruBlockCache
argument_list|(
name|lruCacheSize
argument_list|,
name|blockSize
argument_list|)
decl_stmt|;
name|lruCache
operator|.
name|setVictimCache
argument_list|(
name|bucketCache
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketCache
operator|!=
literal|null
operator|&&
name|combinedWithLru
condition|)
block|{
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
operator|new
name|CombinedBlockCache
argument_list|(
name|lruCache
argument_list|,
name|bucketCache
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
name|lruCache
expr_stmt|;
block|}
block|}
else|else
block|{
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
operator|new
name|DoubleBlockCache
argument_list|(
name|lruCacheSize
argument_list|,
name|slabCacheOffHeapCacheSize
argument_list|,
name|blockSize
argument_list|,
name|blockSize
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|GLOBAL_BLOCK_CACHE_INSTANCE
return|;
block|}
block|}
end_class

end_unit

