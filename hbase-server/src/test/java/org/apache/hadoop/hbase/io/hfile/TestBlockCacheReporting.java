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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

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
name|Map
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
name|HBaseConfiguration
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
name|SmallTests
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
name|TestCacheConfig
operator|.
name|DataCacheEntry
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
name|TestCacheConfig
operator|.
name|IndexCacheEntry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|JsonMappingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBlockCacheReporting
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
name|TestBlockCacheReporting
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|CacheConfig
operator|.
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Let go of current block cache.
name|CacheConfig
operator|.
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|void
name|addDataAndHits
parameter_list|(
specifier|final
name|BlockCache
name|bc
parameter_list|,
specifier|final
name|int
name|count
parameter_list|)
block|{
name|Cacheable
name|dce
init|=
operator|new
name|DataCacheEntry
argument_list|()
decl_stmt|;
name|Cacheable
name|ice
init|=
operator|new
name|IndexCacheEntry
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|BlockCacheKey
name|bckd
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"f"
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|BlockCacheKey
name|bcki
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"f"
argument_list|,
name|i
operator|+
name|count
argument_list|)
decl_stmt|;
name|bc
operator|.
name|getBlock
argument_list|(
name|bckd
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|bc
operator|.
name|cacheBlock
argument_list|(
name|bckd
argument_list|,
name|dce
argument_list|)
expr_stmt|;
name|bc
operator|.
name|cacheBlock
argument_list|(
name|bcki
argument_list|,
name|ice
argument_list|)
expr_stmt|;
name|bc
operator|.
name|getBlock
argument_list|(
name|bckd
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|bc
operator|.
name|getBlock
argument_list|(
name|bcki
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
operator|*
name|count
comment|/*Data and Index blocks*/
argument_list|,
name|bc
operator|.
name|getStats
argument_list|()
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBucketCache
parameter_list|()
throws|throws
name|JsonGenerationException
throws|,
name|JsonMappingException
throws|,
name|IOException
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|CacheConfig
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"offheap"
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|CacheConfig
operator|.
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|CacheConfig
name|cc
init|=
operator|new
name|CacheConfig
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|instanceof
name|CombinedBlockCache
argument_list|)
expr_stmt|;
name|logPerBlock
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|count
init|=
literal|3
decl_stmt|;
name|addDataAndHits
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// The below has no asserts.  It is just exercising toString and toJSON code.
name|LOG
operator|.
name|info
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
name|BlockCacheUtil
operator|.
name|CachedBlocksByFile
name|cbsbf
init|=
name|logPerBlock
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|cbsbf
argument_list|)
expr_stmt|;
name|logPerFile
argument_list|(
name|cbsbf
argument_list|)
expr_stmt|;
name|bucketCacheReport
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|BlockCacheUtil
operator|.
name|toJSON
argument_list|(
name|cbsbf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLruBlockCache
parameter_list|()
throws|throws
name|JsonGenerationException
throws|,
name|JsonMappingException
throws|,
name|IOException
block|{
name|CacheConfig
name|cc
init|=
operator|new
name|CacheConfig
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|isBlockCacheEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CacheConfig
operator|.
name|DEFAULT_IN_MEMORY
operator|==
name|cc
operator|.
name|isInMemory
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|instanceof
name|LruBlockCache
argument_list|)
expr_stmt|;
name|logPerBlock
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|)
expr_stmt|;
name|addDataAndHits
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// The below has no asserts.  It is just exercising toString and toJSON code.
name|BlockCache
name|bc
init|=
name|cc
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"count="
operator|+
name|bc
operator|.
name|getBlockCount
argument_list|()
operator|+
literal|", currentSize="
operator|+
name|bc
operator|.
name|getCurrentSize
argument_list|()
operator|+
literal|", freeSize="
operator|+
name|bc
operator|.
name|getFreeSize
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
name|BlockCacheUtil
operator|.
name|CachedBlocksByFile
name|cbsbf
init|=
name|logPerBlock
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|cbsbf
argument_list|)
expr_stmt|;
name|logPerFile
argument_list|(
name|cbsbf
argument_list|)
expr_stmt|;
name|bucketCacheReport
argument_list|(
name|cc
operator|.
name|getBlockCache
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|BlockCacheUtil
operator|.
name|toJSON
argument_list|(
name|cbsbf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|bucketCacheReport
parameter_list|(
specifier|final
name|BlockCache
name|bc
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|bc
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": "
operator|+
name|bc
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
name|BlockCache
index|[]
name|bcs
init|=
name|bc
operator|.
name|getBlockCaches
argument_list|()
decl_stmt|;
if|if
condition|(
name|bcs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BlockCache
name|sbc
range|:
name|bc
operator|.
name|getBlockCaches
argument_list|()
control|)
block|{
name|bucketCacheReport
argument_list|(
name|sbc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|logPerFile
parameter_list|(
specifier|final
name|BlockCacheUtil
operator|.
name|CachedBlocksByFile
name|cbsbf
parameter_list|)
throws|throws
name|JsonGenerationException
throws|,
name|JsonMappingException
throws|,
name|IOException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|CachedBlock
argument_list|>
argument_list|>
name|e
range|:
name|cbsbf
operator|.
name|getCachedBlockStatsByFile
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|long
name|size
init|=
literal|0
decl_stmt|;
name|int
name|countData
init|=
literal|0
decl_stmt|;
name|long
name|sizeData
init|=
literal|0
decl_stmt|;
for|for
control|(
name|CachedBlock
name|cb
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
name|count
operator|++
expr_stmt|;
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
name|countData
operator|++
expr_stmt|;
name|sizeData
operator|+=
name|cb
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"filename="
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|", count="
operator|+
name|count
operator|+
literal|", countData="
operator|+
name|countData
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|", sizeData="
operator|+
name|sizeData
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|BlockCacheUtil
operator|.
name|toJSON
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|BlockCacheUtil
operator|.
name|CachedBlocksByFile
name|logPerBlock
parameter_list|(
specifier|final
name|BlockCache
name|bc
parameter_list|)
throws|throws
name|JsonGenerationException
throws|,
name|JsonMappingException
throws|,
name|IOException
block|{
name|BlockCacheUtil
operator|.
name|CachedBlocksByFile
name|cbsbf
init|=
operator|new
name|BlockCacheUtil
operator|.
name|CachedBlocksByFile
argument_list|()
decl_stmt|;
for|for
control|(
name|CachedBlock
name|cb
range|:
name|bc
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|cb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|BlockCacheUtil
operator|.
name|toJSON
argument_list|(
name|bc
argument_list|)
argument_list|)
expr_stmt|;
name|cbsbf
operator|.
name|update
argument_list|(
name|cb
argument_list|)
expr_stmt|;
block|}
return|return
name|cbsbf
return|;
block|}
block|}
end_class

end_unit

