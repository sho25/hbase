begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|HBaseTestingUtility
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
name|MediumTests
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
name|Get
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
name|Put
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
name|compress
operator|.
name|Compression
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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|regionserver
operator|.
name|BloomType
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
name|regionserver
operator|.
name|HRegion
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Make sure we always cache important block types, such as index blocks, as  * long as we have a block cache, even though block caching might be disabled  * for the column family.  *   *<p>TODO: This test writes a lot of data and only tests the most basic of metrics.  Cache stats  * need to reveal more about what is being cached whether DATA or INDEX blocks and then we could  * do more verification in this test.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestForceCacheImportantBlocks
block|{
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"myTable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CF
init|=
literal|"myCF"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CF
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_HFILES
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROWS_PER_HFILE
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
name|NUM_HFILES
operator|*
name|ROWS_PER_HFILE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_TIMESTAMPS_PER_COL
init|=
literal|50
decl_stmt|;
comment|/** Extremely small block size, so that we can get some index blocks */
specifier|private
specifier|static
specifier|final
name|int
name|BLOCK_SIZE
init|=
literal|256
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Algorithm
name|COMPRESSION_ALGORITHM
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|BloomType
name|BLOOM_TYPE
init|=
name|BloomType
operator|.
name|ROW
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
comment|// Currently unused.
specifier|private
specifier|final
name|int
name|hfileVersion
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|cfCacheEnabled
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
comment|// HFile versions
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|2
block|,
literal|true
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|2
block|,
literal|false
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|3
block|,
literal|true
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|3
block|,
literal|false
block|}
argument_list|)
return|;
block|}
specifier|public
name|TestForceCacheImportantBlocks
parameter_list|(
name|int
name|hfileVersion
parameter_list|,
name|boolean
name|cfCacheEnabled
parameter_list|)
block|{
name|this
operator|.
name|hfileVersion
operator|=
name|hfileVersion
expr_stmt|;
name|this
operator|.
name|cfCacheEnabled
operator|=
name|cfCacheEnabled
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
name|hfileVersion
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
comment|// Make sure we make a new one each time.
name|CacheConfig
operator|.
name|GLOBAL_BLOCK_CACHE_INSTANCE
operator|=
literal|null
expr_stmt|;
name|HFile
operator|.
name|dataBlockReadCnt
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCacheBlocks
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Set index block size to be the same as normal block size.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HFileBlockIndex
operator|.
name|MAX_CHUNK_SIZE_KEY
argument_list|,
name|BLOCK_SIZE
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CF
argument_list|)
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|MAX_VERSIONS
argument_list|)
operator|.
name|setCompressionType
argument_list|(
name|COMPRESSION_ALGORITHM
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|BLOOM_TYPE
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
name|BLOCK_SIZE
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
name|cfCacheEnabled
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|TEST_UTIL
operator|.
name|createTestRegion
argument_list|(
name|TABLE
argument_list|,
name|hcd
argument_list|)
decl_stmt|;
name|BlockCache
name|cache
init|=
name|region
operator|.
name|getStore
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getCacheConfig
argument_list|()
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|CacheStats
name|stats
init|=
name|cache
operator|.
name|getStats
argument_list|()
decl_stmt|;
name|writeTestData
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HFile
operator|.
name|dataBlockReadCnt
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// Do a single get, take count of caches.  If we are NOT caching DATA blocks, the miss
comment|// count should go up.  Otherwise, all should be cached and the miss count should not rise.
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|stats
operator|.
name|getHitCount
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HFile
operator|.
name|dataBlockReadCnt
operator|.
name|get
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|long
name|missCount
init|=
name|stats
operator|.
name|getMissCount
argument_list|()
decl_stmt|;
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|cfCacheEnabled
condition|)
name|assertEquals
argument_list|(
name|missCount
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|assertTrue
argument_list|(
name|stats
operator|.
name|getMissCount
argument_list|()
operator|>
name|missCount
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeTestData
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_ROWS
condition|;
operator|++
name|i
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
for|for
control|(
name|long
name|ts
init|=
literal|1
init|;
name|ts
operator|<
name|NUM_TIMESTAMPS_PER_COL
condition|;
operator|++
name|ts
control|)
block|{
name|put
operator|.
name|add
argument_list|(
name|CF_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|j
argument_list|)
argument_list|,
name|ts
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|i
operator|+
literal|"_"
operator|+
name|j
operator|+
literal|"_"
operator|+
name|ts
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
name|ROWS_PER_HFILE
operator|==
literal|0
condition|)
block|{
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

