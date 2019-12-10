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
name|util
operator|.
name|Optional
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CacheConfig
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Disabled cache configuration    */
specifier|public
specifier|static
specifier|final
name|CacheConfig
name|DISABLED
init|=
operator|new
name|CacheConfig
argument_list|()
decl_stmt|;
comment|/**    * Configuration key to cache data blocks on read. Bloom blocks and index blocks are always be    * cached if the block cache is enabled.    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_DATA_ON_READ_KEY
init|=
literal|"hbase.block.data.cacheonread"
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
comment|/**    * Configuration key to cache data blocks in compressed and/or encrypted format.    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
init|=
literal|"hbase.block.data.cachecompressed"
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
comment|/**    * Configuration key to prefetch all blocks of a given file into the block cache    * when the file is opened.    */
specifier|public
specifier|static
specifier|final
name|String
name|PREFETCH_BLOCKS_ON_OPEN_KEY
init|=
literal|"hbase.rs.prefetchblocksonopen"
decl_stmt|;
comment|/**    * Configuration key to cache blocks when a compacted file is written    */
specifier|public
specifier|static
specifier|final
name|String
name|CACHE_COMPACTED_BLOCKS_ON_WRITE_KEY
init|=
literal|"hbase.rs.cachecompactedblocksonwrite"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_BEHIND_CACHE_COMPACTION_KEY
init|=
literal|"hbase.hfile.drop.behind.compaction"
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
name|DEFAULT_CACHE_DATA_COMPRESSED
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
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_CACHE_COMPACTED_BLOCKS_ON_WRITE
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DROP_BEHIND_CACHE_COMPACTION_DEFAULT
init|=
literal|true
decl_stmt|;
comment|/**    * Whether blocks should be cached on read (default is on if there is a    * cache but this can be turned off on a per-family or per-request basis).    * If off we will STILL cache meta blocks; i.e. INDEX and BLOOM types.    * This cannot be disabled.    */
specifier|private
specifier|final
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
comment|/** Whether data blocks should be stored in compressed and/or encrypted form in the cache */
specifier|private
specifier|final
name|boolean
name|cacheDataCompressed
decl_stmt|;
comment|/** Whether data blocks should be prefetched into the cache */
specifier|private
specifier|final
name|boolean
name|prefetchOnOpen
decl_stmt|;
comment|/**    * Whether data blocks should be cached when compacted file is written    */
specifier|private
specifier|final
name|boolean
name|cacheCompactedDataOnWrite
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|dropBehindCompaction
decl_stmt|;
comment|// Local reference to the block cache
specifier|private
specifier|final
name|BlockCache
name|blockCache
decl_stmt|;
specifier|private
specifier|final
name|ByteBuffAllocator
name|byteBuffAllocator
decl_stmt|;
comment|/**    * Create a cache configuration using the specified configuration object and    * defaults for family level settings. Only use if no column family context.    * @param conf hbase configuration    */
specifier|public
name|CacheConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CacheConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|BlockCache
name|blockCache
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
name|blockCache
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a cache configuration using the specified configuration object and    * family descriptor.    * @param conf hbase configuration    * @param family column family configuration    */
specifier|public
name|CacheConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ColumnFamilyDescriptor
name|family
parameter_list|,
name|BlockCache
name|blockCache
parameter_list|,
name|ByteBuffAllocator
name|byteBuffAllocator
parameter_list|)
block|{
name|this
operator|.
name|cacheDataOnRead
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_DATA_ON_READ_KEY
argument_list|,
name|DEFAULT_CACHE_DATA_ON_READ
argument_list|)
operator|&&
operator|(
name|family
operator|==
literal|null
condition|?
literal|true
else|:
name|family
operator|.
name|isBlockCacheEnabled
argument_list|()
operator|)
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
name|family
operator|==
literal|null
condition|?
name|DEFAULT_IN_MEMORY
else|:
name|family
operator|.
name|isInMemory
argument_list|()
expr_stmt|;
name|this
operator|.
name|cacheDataCompressed
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_DATA_BLOCKS_COMPRESSED_KEY
argument_list|,
name|DEFAULT_CACHE_DATA_COMPRESSED
argument_list|)
expr_stmt|;
name|this
operator|.
name|dropBehindCompaction
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|DROP_BEHIND_CACHE_COMPACTION_KEY
argument_list|,
name|DROP_BEHIND_CACHE_COMPACTION_DEFAULT
argument_list|)
expr_stmt|;
comment|// For the following flags we enable them regardless of per-schema settings
comment|// if they are enabled in the global configuration.
name|this
operator|.
name|cacheDataOnWrite
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_DATA_ON_WRITE
argument_list|)
operator|||
operator|(
name|family
operator|==
literal|null
condition|?
literal|false
else|:
name|family
operator|.
name|isCacheDataOnWrite
argument_list|()
operator|)
expr_stmt|;
name|this
operator|.
name|cacheIndexesOnWrite
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_INDEXES_ON_WRITE
argument_list|)
operator|||
operator|(
name|family
operator|==
literal|null
condition|?
literal|false
else|:
name|family
operator|.
name|isCacheIndexesOnWrite
argument_list|()
operator|)
expr_stmt|;
name|this
operator|.
name|cacheBloomsOnWrite
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_BLOOMS_ON_WRITE
argument_list|)
operator|||
operator|(
name|family
operator|==
literal|null
condition|?
literal|false
else|:
name|family
operator|.
name|isCacheBloomsOnWrite
argument_list|()
operator|)
expr_stmt|;
name|this
operator|.
name|evictOnClose
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|EVICT_BLOCKS_ON_CLOSE_KEY
argument_list|,
name|DEFAULT_EVICT_ON_CLOSE
argument_list|)
operator|||
operator|(
name|family
operator|==
literal|null
condition|?
literal|false
else|:
name|family
operator|.
name|isEvictBlocksOnClose
argument_list|()
operator|)
expr_stmt|;
name|this
operator|.
name|prefetchOnOpen
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|PREFETCH_BLOCKS_ON_OPEN_KEY
argument_list|,
name|DEFAULT_PREFETCH_ON_OPEN
argument_list|)
operator|||
operator|(
name|family
operator|==
literal|null
condition|?
literal|false
else|:
name|family
operator|.
name|isPrefetchBlocksOnOpen
argument_list|()
operator|)
expr_stmt|;
name|this
operator|.
name|cacheCompactedDataOnWrite
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CACHE_COMPACTED_BLOCKS_ON_WRITE_KEY
argument_list|,
name|DEFAULT_CACHE_COMPACTED_BLOCKS_ON_WRITE
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockCache
operator|=
name|blockCache
expr_stmt|;
name|this
operator|.
name|byteBuffAllocator
operator|=
name|byteBuffAllocator
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created cacheConfig: "
operator|+
name|this
operator|+
operator|(
name|family
operator|==
literal|null
condition|?
literal|""
else|:
literal|" for family "
operator|+
name|family
operator|)
operator|+
literal|" with blockCache="
operator|+
name|blockCache
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
operator|.
name|cacheDataOnRead
operator|=
name|cacheConf
operator|.
name|cacheDataOnRead
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
name|cacheConf
operator|.
name|inMemory
expr_stmt|;
name|this
operator|.
name|cacheDataOnWrite
operator|=
name|cacheConf
operator|.
name|cacheDataOnWrite
expr_stmt|;
name|this
operator|.
name|cacheIndexesOnWrite
operator|=
name|cacheConf
operator|.
name|cacheIndexesOnWrite
expr_stmt|;
name|this
operator|.
name|cacheBloomsOnWrite
operator|=
name|cacheConf
operator|.
name|cacheBloomsOnWrite
expr_stmt|;
name|this
operator|.
name|evictOnClose
operator|=
name|cacheConf
operator|.
name|evictOnClose
expr_stmt|;
name|this
operator|.
name|cacheDataCompressed
operator|=
name|cacheConf
operator|.
name|cacheDataCompressed
expr_stmt|;
name|this
operator|.
name|prefetchOnOpen
operator|=
name|cacheConf
operator|.
name|prefetchOnOpen
expr_stmt|;
name|this
operator|.
name|cacheCompactedDataOnWrite
operator|=
name|cacheConf
operator|.
name|cacheCompactedDataOnWrite
expr_stmt|;
name|this
operator|.
name|dropBehindCompaction
operator|=
name|cacheConf
operator|.
name|dropBehindCompaction
expr_stmt|;
name|this
operator|.
name|blockCache
operator|=
name|cacheConf
operator|.
name|blockCache
expr_stmt|;
name|this
operator|.
name|byteBuffAllocator
operator|=
name|cacheConf
operator|.
name|byteBuffAllocator
expr_stmt|;
block|}
specifier|private
name|CacheConfig
parameter_list|()
block|{
name|this
operator|.
name|cacheDataOnRead
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cacheDataOnWrite
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cacheIndexesOnWrite
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cacheBloomsOnWrite
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|evictOnClose
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cacheDataCompressed
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|prefetchOnOpen
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cacheCompactedDataOnWrite
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|dropBehindCompaction
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|blockCache
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|byteBuffAllocator
operator|=
name|ByteBuffAllocator
operator|.
name|HEAP
expr_stmt|;
block|}
comment|/**    * Returns whether the DATA blocks of this HFile should be cached on read or not (we always    * cache the meta blocks, the INDEX and BLOOM blocks).    * @return true if blocks should be cached on read, false if not    */
specifier|public
name|boolean
name|shouldCacheDataOnRead
parameter_list|()
block|{
return|return
name|cacheDataOnRead
return|;
block|}
specifier|public
name|boolean
name|shouldDropBehindCompaction
parameter_list|()
block|{
return|return
name|dropBehindCompaction
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
return|return
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
return|;
block|}
comment|/**    * @return true if blocks in this file should be flagged as in-memory    */
specifier|public
name|boolean
name|isInMemory
parameter_list|()
block|{
return|return
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
name|this
operator|.
name|cacheDataOnWrite
return|;
block|}
comment|/**    * @param cacheDataOnWrite whether data blocks should be written to the cache    *                         when an HFile is written    */
annotation|@
name|VisibleForTesting
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
name|this
operator|.
name|evictOnClose
return|;
block|}
comment|/**    * Only used for testing.    * @param evictOnClose whether blocks should be evicted from the cache when an    *                     HFile reader is closed    */
annotation|@
name|VisibleForTesting
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
comment|/**    * @return true if data blocks should be compressed in the cache, false if not    */
specifier|public
name|boolean
name|shouldCacheDataCompressed
parameter_list|()
block|{
return|return
name|this
operator|.
name|cacheDataOnRead
operator|&&
name|this
operator|.
name|cacheDataCompressed
return|;
block|}
comment|/**    * @return true if this {@link BlockCategory} should be compressed in blockcache, false otherwise    */
specifier|public
name|boolean
name|shouldCacheCompressed
parameter_list|(
name|BlockCategory
name|category
parameter_list|)
block|{
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|DATA
case|:
return|return
name|this
operator|.
name|cacheDataOnRead
operator|&&
name|this
operator|.
name|cacheDataCompressed
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
comment|/**    * @return true if blocks should be prefetched into the cache on open, false if not    */
specifier|public
name|boolean
name|shouldPrefetchOnOpen
parameter_list|()
block|{
return|return
name|this
operator|.
name|prefetchOnOpen
return|;
block|}
comment|/**    * @return true if blocks should be cached while writing during compaction, false if not    */
specifier|public
name|boolean
name|shouldCacheCompactedBlocksOnWrite
parameter_list|()
block|{
return|return
name|this
operator|.
name|cacheCompactedDataOnWrite
return|;
block|}
comment|/**    * Return true if we may find this type of block in block cache.    *<p>    * TODO: today {@code family.isBlockCacheEnabled()} only means {@code cacheDataOnRead}, so here we    * consider lots of other configurations such as {@code cacheDataOnWrite}. We should fix this in    * the future, {@code cacheDataOnWrite} should honor the CF level {@code isBlockCacheEnabled}    * configuration.    */
specifier|public
name|boolean
name|shouldReadBlockFromCache
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
if|if
condition|(
name|cacheDataOnRead
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|prefetchOnOpen
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|cacheDataOnWrite
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|blockType
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|blockType
operator|.
name|getCategory
argument_list|()
operator|==
name|BlockCategory
operator|.
name|BLOOM
operator|||
name|blockType
operator|.
name|getCategory
argument_list|()
operator|==
name|BlockCategory
operator|.
name|INDEX
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * If we make sure the block could not be cached, we will not acquire the lock    * otherwise we will acquire lock    */
specifier|public
name|boolean
name|shouldLockOnCacheMiss
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
if|if
condition|(
name|blockType
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|shouldCacheBlockOnRead
argument_list|(
name|blockType
operator|.
name|getCategory
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns the block cache.    *    * @return the block cache, or null if caching is completely disabled    */
specifier|public
name|Optional
argument_list|<
name|BlockCache
argument_list|>
name|getBlockCache
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|ofNullable
argument_list|(
name|this
operator|.
name|blockCache
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isCombinedBlockCache
parameter_list|()
block|{
return|return
name|blockCache
operator|instanceof
name|CombinedBlockCache
return|;
block|}
specifier|public
name|ByteBuffAllocator
name|getByteBuffAllocator
parameter_list|()
block|{
return|return
name|this
operator|.
name|byteBuffAllocator
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"cacheDataOnRead="
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
literal|", cacheDataCompressed="
operator|+
name|shouldCacheDataCompressed
argument_list|()
operator|+
literal|", prefetchOnOpen="
operator|+
name|shouldPrefetchOnOpen
argument_list|()
return|;
block|}
block|}
end_class

end_unit

