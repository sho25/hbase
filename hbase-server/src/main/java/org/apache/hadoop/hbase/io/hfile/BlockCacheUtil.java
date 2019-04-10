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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListSet
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
name|metrics
operator|.
name|impl
operator|.
name|FastLongHistogram
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
name|Bytes
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
name|GsonUtil
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
name|gson
operator|.
name|Gson
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
name|gson
operator|.
name|TypeAdapter
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
name|gson
operator|.
name|stream
operator|.
name|JsonReader
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
name|gson
operator|.
name|stream
operator|.
name|JsonWriter
import|;
end_import

begin_comment
comment|/**  * Utilty for aggregating counts in CachedBlocks and toString/toJSON CachedBlocks and BlockCaches.  * No attempt has been made at making this thread safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BlockCacheUtil
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
name|BlockCacheUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|NANOS_PER_SECOND
init|=
literal|1000000000
decl_stmt|;
comment|/**    * Needed generating JSON.    */
specifier|private
specifier|static
specifier|final
name|Gson
name|GSON
init|=
name|GsonUtil
operator|.
name|createGson
argument_list|()
operator|.
name|registerTypeAdapter
argument_list|(
name|FastLongHistogram
operator|.
name|class
argument_list|,
operator|new
name|TypeAdapter
argument_list|<
name|FastLongHistogram
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|JsonWriter
name|out
parameter_list|,
name|FastLongHistogram
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|AgeSnapshot
name|snapshot
init|=
operator|new
name|AgeSnapshot
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|out
operator|.
name|beginObject
argument_list|()
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"mean"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"min"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"max"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"75thPercentile"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|get75thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"95thPercentile"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"98thPercentile"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|get98thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"99thPercentile"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|name
argument_list|(
literal|"999thPercentile"
argument_list|)
operator|.
name|value
argument_list|(
name|snapshot
operator|.
name|get999thPercentile
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|FastLongHistogram
name|read
parameter_list|(
name|JsonReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
argument_list|)
operator|.
name|setPrettyPrinting
argument_list|()
operator|.
name|create
argument_list|()
decl_stmt|;
comment|/**    * @param cb    * @return The block content as String.    */
specifier|public
specifier|static
name|String
name|toString
parameter_list|(
specifier|final
name|CachedBlock
name|cb
parameter_list|,
specifier|final
name|long
name|now
parameter_list|)
block|{
return|return
literal|"filename="
operator|+
name|cb
operator|.
name|getFilename
argument_list|()
operator|+
literal|", "
operator|+
name|toStringMinusFileName
argument_list|(
name|cb
argument_list|,
name|now
argument_list|)
return|;
block|}
comment|/**    * Little data structure to hold counts for a file.    * Used doing a toJSON.    */
specifier|static
class|class
name|CachedBlockCountsPerFile
block|{
specifier|private
name|int
name|count
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|size
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|countData
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|sizeData
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|String
name|filename
decl_stmt|;
name|CachedBlockCountsPerFile
parameter_list|(
specifier|final
name|String
name|filename
parameter_list|)
block|{
name|this
operator|.
name|filename
operator|=
name|filename
expr_stmt|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|public
name|int
name|getCountData
parameter_list|()
block|{
return|return
name|countData
return|;
block|}
specifier|public
name|long
name|getSizeData
parameter_list|()
block|{
return|return
name|sizeData
return|;
block|}
specifier|public
name|String
name|getFilename
parameter_list|()
block|{
return|return
name|filename
return|;
block|}
block|}
comment|/**    * @return A JSON String of<code>filename</code> and counts of<code>blocks</code>    */
specifier|public
specifier|static
name|String
name|toJSON
parameter_list|(
name|String
name|filename
parameter_list|,
name|NavigableSet
argument_list|<
name|CachedBlock
argument_list|>
name|blocks
parameter_list|)
throws|throws
name|IOException
block|{
name|CachedBlockCountsPerFile
name|counts
init|=
operator|new
name|CachedBlockCountsPerFile
argument_list|(
name|filename
argument_list|)
decl_stmt|;
for|for
control|(
name|CachedBlock
name|cb
range|:
name|blocks
control|)
block|{
name|counts
operator|.
name|count
operator|++
expr_stmt|;
name|counts
operator|.
name|size
operator|+=
name|cb
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|BlockType
name|bt
init|=
name|cb
operator|.
name|getBlockType
argument_list|()
decl_stmt|;
if|if
condition|(
name|bt
operator|!=
literal|null
operator|&&
name|bt
operator|.
name|isData
argument_list|()
condition|)
block|{
name|counts
operator|.
name|countData
operator|++
expr_stmt|;
name|counts
operator|.
name|sizeData
operator|+=
name|cb
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|GSON
operator|.
name|toJson
argument_list|(
name|counts
argument_list|)
return|;
block|}
comment|/**    * @return JSON string of<code>cbsf</code> aggregated    */
specifier|public
specifier|static
name|String
name|toJSON
parameter_list|(
name|CachedBlocksByFile
name|cbsbf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|GSON
operator|.
name|toJson
argument_list|(
name|cbsbf
argument_list|)
return|;
block|}
comment|/**    * @return JSON string of<code>bc</code> content.    */
specifier|public
specifier|static
name|String
name|toJSON
parameter_list|(
name|BlockCache
name|bc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|GSON
operator|.
name|toJson
argument_list|(
name|bc
argument_list|)
return|;
block|}
comment|/**    * @param cb    * @return The block content of<code>bc</code> as a String minus the filename.    */
specifier|public
specifier|static
name|String
name|toStringMinusFileName
parameter_list|(
specifier|final
name|CachedBlock
name|cb
parameter_list|,
specifier|final
name|long
name|now
parameter_list|)
block|{
return|return
literal|"offset="
operator|+
name|cb
operator|.
name|getOffset
argument_list|()
operator|+
literal|", size="
operator|+
name|cb
operator|.
name|getSize
argument_list|()
operator|+
literal|", age="
operator|+
operator|(
name|now
operator|-
name|cb
operator|.
name|getCachedTime
argument_list|()
operator|)
operator|+
literal|", type="
operator|+
name|cb
operator|.
name|getBlockType
argument_list|()
operator|+
literal|", priority="
operator|+
name|cb
operator|.
name|getBlockPriority
argument_list|()
return|;
block|}
comment|/**    * Get a {@link CachedBlocksByFile} instance and load it up by iterating content in    * {@link BlockCache}.    * @param conf Used to read configurations    * @param bc Block Cache to iterate.    * @return Laoded up instance of CachedBlocksByFile    */
specifier|public
specifier|static
name|CachedBlocksByFile
name|getLoadedCachedBlocksByFile
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|BlockCache
name|bc
parameter_list|)
block|{
name|CachedBlocksByFile
name|cbsbf
init|=
operator|new
name|CachedBlocksByFile
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|CachedBlock
name|cb
range|:
name|bc
control|)
block|{
if|if
condition|(
name|cbsbf
operator|.
name|update
argument_list|(
name|cb
argument_list|)
condition|)
break|break;
block|}
return|return
name|cbsbf
return|;
block|}
specifier|private
specifier|static
name|int
name|compareCacheBlock
parameter_list|(
name|Cacheable
name|left
parameter_list|,
name|Cacheable
name|right
parameter_list|,
name|boolean
name|includeNextBlockMetadata
parameter_list|)
block|{
name|ByteBuffer
name|l
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|left
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
decl_stmt|;
name|left
operator|.
name|serialize
argument_list|(
name|l
argument_list|,
name|includeNextBlockMetadata
argument_list|)
expr_stmt|;
name|ByteBuffer
name|r
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|right
operator|.
name|getSerializedLength
argument_list|()
argument_list|)
decl_stmt|;
name|right
operator|.
name|serialize
argument_list|(
name|r
argument_list|,
name|includeNextBlockMetadata
argument_list|)
expr_stmt|;
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|l
operator|.
name|array
argument_list|()
argument_list|,
name|l
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|l
operator|.
name|limit
argument_list|()
argument_list|,
name|r
operator|.
name|array
argument_list|()
argument_list|,
name|r
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|r
operator|.
name|limit
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Validate that the existing and newBlock are the same without including the nextBlockMetadata,    * if not, throw an exception. If they are the same without the nextBlockMetadata,    * return the comparison.    *    * @param existing block that is existing in the cache.    * @param newBlock block that is trying to be cached.    * @param cacheKey the cache key of the blocks.    * @return comparison of the existing block to the newBlock.    */
specifier|public
specifier|static
name|int
name|validateBlockAddition
parameter_list|(
name|Cacheable
name|existing
parameter_list|,
name|Cacheable
name|newBlock
parameter_list|,
name|BlockCacheKey
name|cacheKey
parameter_list|)
block|{
name|int
name|comparison
init|=
name|compareCacheBlock
argument_list|(
name|existing
argument_list|,
name|newBlock
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparison
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cached block contents differ, which should not have happened."
operator|+
literal|"cacheKey:"
operator|+
name|cacheKey
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|existing
operator|instanceof
name|HFileBlock
operator|)
operator|&&
operator|(
name|newBlock
operator|instanceof
name|HFileBlock
operator|)
condition|)
block|{
name|comparison
operator|=
operator|(
operator|(
name|HFileBlock
operator|)
name|existing
operator|)
operator|.
name|getNextBlockOnDiskSize
argument_list|()
operator|-
operator|(
operator|(
name|HFileBlock
operator|)
name|newBlock
operator|)
operator|.
name|getNextBlockOnDiskSize
argument_list|()
expr_stmt|;
block|}
return|return
name|comparison
return|;
block|}
comment|/**    * Because of the region splitting, it's possible that the split key locate in the middle of a    * block. So it's possible that both the daughter regions load the same block from their parent    * HFile. When pread, we don't force the read to read all of the next block header. So when two    * threads try to cache the same block, it's possible that one thread read all of the next block    * header but the other one didn't. if the already cached block hasn't next block header but the    * new block to cache has, then we can replace the existing block with the new block for better    * performance.(HBASE-20447)    * @param blockCache BlockCache to check    * @param cacheKey the block cache key    * @param newBlock the new block which try to put into the block cache.    * @return true means need to replace existing block with new block for the same block cache key.    *         false means just keep the existing block.    */
specifier|public
specifier|static
name|boolean
name|shouldReplaceExistingCacheBlock
parameter_list|(
name|BlockCache
name|blockCache
parameter_list|,
name|BlockCacheKey
name|cacheKey
parameter_list|,
name|Cacheable
name|newBlock
parameter_list|)
block|{
comment|// NOTICE: The getBlock has retained the existingBlock inside.
name|Cacheable
name|existingBlock
init|=
name|blockCache
operator|.
name|getBlock
argument_list|(
name|cacheKey
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|comparison
init|=
name|BlockCacheUtil
operator|.
name|validateBlockAddition
argument_list|(
name|existingBlock
argument_list|,
name|newBlock
argument_list|,
name|cacheKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparison
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cached block contents differ by nextBlockOnDiskSize, the new block has "
operator|+
literal|"nextBlockOnDiskSize set. Caching new block."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|comparison
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cached block contents differ by nextBlockOnDiskSize, the existing block has "
operator|+
literal|"nextBlockOnDiskSize set, Keeping cached block."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caching an already cached block: {}. This is harmless and can happen in rare "
operator|+
literal|"cases (see HBASE-8547)"
argument_list|,
name|cacheKey
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
finally|finally
block|{
comment|// return the block since we need to decrement the count
name|blockCache
operator|.
name|returnBlock
argument_list|(
name|cacheKey
argument_list|,
name|existingBlock
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Use one of these to keep a running account of cached blocks by file.  Throw it away when done.    * This is different than metrics in that it is stats on current state of a cache.    * See getLoadedCachedBlocksByFile    */
specifier|public
specifier|static
class|class
name|CachedBlocksByFile
block|{
specifier|private
name|int
name|count
decl_stmt|;
specifier|private
name|int
name|dataBlockCount
decl_stmt|;
specifier|private
name|long
name|size
decl_stmt|;
specifier|private
name|long
name|dataSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|now
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
comment|/**      * How many blocks to look at before we give up.      * There could be many millions of blocks. We don't want the      * ui to freeze while we run through 1B blocks... users will      * think hbase dead. UI displays warning in red when stats      * are incomplete.      */
specifier|private
specifier|final
name|int
name|max
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX
init|=
literal|1000000
decl_stmt|;
name|CachedBlocksByFile
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|CachedBlocksByFile
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
name|this
operator|.
name|max
operator|=
name|c
operator|==
literal|null
condition|?
name|DEFAULT_MAX
else|:
name|c
operator|.
name|getInt
argument_list|(
literal|"hbase.ui.blockcache.by.file.max"
argument_list|,
name|DEFAULT_MAX
argument_list|)
expr_stmt|;
block|}
comment|/**      * Map by filename. use concurent utils because we want our Map and contained blocks sorted.      */
specifier|private
specifier|transient
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|CachedBlock
argument_list|>
argument_list|>
name|cachedBlockByFile
init|=
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|()
decl_stmt|;
name|FastLongHistogram
name|hist
init|=
operator|new
name|FastLongHistogram
argument_list|()
decl_stmt|;
comment|/**      * @param cb      * @return True if full.... if we won't be adding any more.      */
specifier|public
name|boolean
name|update
parameter_list|(
specifier|final
name|CachedBlock
name|cb
parameter_list|)
block|{
if|if
condition|(
name|isFull
argument_list|()
condition|)
return|return
literal|true
return|;
name|NavigableSet
argument_list|<
name|CachedBlock
argument_list|>
name|set
init|=
name|this
operator|.
name|cachedBlockByFile
operator|.
name|get
argument_list|(
name|cb
operator|.
name|getFilename
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
name|set
operator|=
operator|new
name|ConcurrentSkipListSet
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|cachedBlockByFile
operator|.
name|put
argument_list|(
name|cb
operator|.
name|getFilename
argument_list|()
argument_list|,
name|set
argument_list|)
expr_stmt|;
block|}
name|set
operator|.
name|add
argument_list|(
name|cb
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|+=
name|cb
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|this
operator|.
name|count
operator|++
expr_stmt|;
name|BlockType
name|bt
init|=
name|cb
operator|.
name|getBlockType
argument_list|()
decl_stmt|;
if|if
condition|(
name|bt
operator|!=
literal|null
operator|&&
name|bt
operator|.
name|isData
argument_list|()
condition|)
block|{
name|this
operator|.
name|dataBlockCount
operator|++
expr_stmt|;
name|this
operator|.
name|dataSize
operator|+=
name|cb
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
name|long
name|age
init|=
operator|(
name|this
operator|.
name|now
operator|-
name|cb
operator|.
name|getCachedTime
argument_list|()
operator|)
operator|/
name|NANOS_PER_SECOND
decl_stmt|;
name|this
operator|.
name|hist
operator|.
name|add
argument_list|(
name|age
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/**      * @return True if full; i.e. there are more items in the cache but we only loaded up      * the maximum set in configuration<code>hbase.ui.blockcache.by.file.max</code>      * (Default: DEFAULT_MAX).      */
specifier|public
name|boolean
name|isFull
parameter_list|()
block|{
return|return
name|this
operator|.
name|count
operator|>=
name|this
operator|.
name|max
return|;
block|}
specifier|public
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|CachedBlock
argument_list|>
argument_list|>
name|getCachedBlockStatsByFile
parameter_list|()
block|{
return|return
name|this
operator|.
name|cachedBlockByFile
return|;
block|}
comment|/**      * @return count of blocks in the cache      */
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
specifier|public
name|int
name|getDataCount
parameter_list|()
block|{
return|return
name|dataBlockCount
return|;
block|}
comment|/**      * @return size of blocks in the cache      */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
comment|/**      * @return Size of data.      */
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|dataSize
return|;
block|}
specifier|public
name|AgeSnapshot
name|getAgeInCacheSnapshot
parameter_list|()
block|{
return|return
operator|new
name|AgeSnapshot
argument_list|(
name|this
operator|.
name|hist
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|AgeSnapshot
name|snapshot
init|=
name|getAgeInCacheSnapshot
argument_list|()
decl_stmt|;
return|return
literal|"count="
operator|+
name|count
operator|+
literal|", dataBlockCount="
operator|+
name|dataBlockCount
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|", dataSize="
operator|+
name|getDataSize
argument_list|()
operator|+
literal|", mean age="
operator|+
name|snapshot
operator|.
name|getMean
argument_list|()
operator|+
literal|", min age="
operator|+
name|snapshot
operator|.
name|getMin
argument_list|()
operator|+
literal|", max age="
operator|+
name|snapshot
operator|.
name|getMax
argument_list|()
operator|+
literal|", 75th percentile age="
operator|+
name|snapshot
operator|.
name|get75thPercentile
argument_list|()
operator|+
literal|", 95th percentile age="
operator|+
name|snapshot
operator|.
name|get95thPercentile
argument_list|()
operator|+
literal|", 98th percentile age="
operator|+
name|snapshot
operator|.
name|get98thPercentile
argument_list|()
operator|+
literal|", 99th percentile age="
operator|+
name|snapshot
operator|.
name|get99thPercentile
argument_list|()
operator|+
literal|", 99.9th percentile age="
operator|+
name|snapshot
operator|.
name|get99thPercentile
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

