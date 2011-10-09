begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|slab
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
name|hfile
operator|.
name|CacheTestUtils
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
operator|.
name|SlabStats
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
name|Ignore
import|;
end_import

begin_comment
comment|/**  * Basic test of SlabCache. Puts and gets.  *<p>  *  * Tests will ensure that blocks that are uncached are identical to the ones  * being cached, and that the cache never exceeds its capacity. Note that its  * fine if the cache evicts before it reaches max capacity - Guava Mapmaker may  * choose to evict at any time.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestSlabCache
block|{
specifier|static
specifier|final
name|int
name|CACHE_SIZE
init|=
literal|1000000
decl_stmt|;
specifier|static
specifier|final
name|int
name|NUM_BLOCKS
init|=
literal|101
decl_stmt|;
specifier|static
specifier|final
name|int
name|BLOCK_SIZE
init|=
name|CACHE_SIZE
operator|/
name|NUM_BLOCKS
decl_stmt|;
specifier|static
specifier|final
name|int
name|NUM_THREADS
init|=
literal|50
decl_stmt|;
specifier|static
specifier|final
name|int
name|NUM_QUERIES
init|=
literal|10000
decl_stmt|;
name|SlabCache
name|cache
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|cache
operator|=
operator|new
name|SlabCache
argument_list|(
name|CACHE_SIZE
operator|+
name|BLOCK_SIZE
operator|*
literal|2
argument_list|,
name|BLOCK_SIZE
argument_list|)
expr_stmt|;
name|cache
operator|.
name|addSlabByConf
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|cache
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testElementPlacement
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|cache
operator|.
name|getHigherBlock
argument_list|(
name|BLOCK_SIZE
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|,
operator|(
name|BLOCK_SIZE
operator|*
literal|11
operator|/
literal|10
operator|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cache
operator|.
name|getHigherBlock
argument_list|(
operator|(
name|BLOCK_SIZE
operator|*
literal|2
operator|)
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|,
operator|(
name|BLOCK_SIZE
operator|*
literal|21
operator|/
literal|10
operator|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testCacheSimple
parameter_list|()
throws|throws
name|Exception
block|{
name|CacheTestUtils
operator|.
name|testCacheSimple
argument_list|(
name|cache
argument_list|,
name|BLOCK_SIZE
argument_list|,
name|NUM_QUERIES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testCacheMultiThreaded
parameter_list|()
throws|throws
name|Exception
block|{
name|CacheTestUtils
operator|.
name|testCacheMultiThreaded
argument_list|(
name|cache
argument_list|,
name|BLOCK_SIZE
argument_list|,
name|NUM_THREADS
argument_list|,
name|NUM_QUERIES
argument_list|,
literal|0.80
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testCacheMultiThreadedSingleKey
parameter_list|()
throws|throws
name|Exception
block|{
name|CacheTestUtils
operator|.
name|hammerSingleKey
argument_list|(
name|cache
argument_list|,
name|BLOCK_SIZE
argument_list|,
name|NUM_THREADS
argument_list|,
name|NUM_QUERIES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testCacheMultiThreadedEviction
parameter_list|()
throws|throws
name|Exception
block|{
name|CacheTestUtils
operator|.
name|hammerEviction
argument_list|(
name|cache
argument_list|,
name|BLOCK_SIZE
argument_list|,
literal|10
argument_list|,
name|NUM_QUERIES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
comment|/*Just checks if ranges overlap*/
specifier|public
name|void
name|testStatsArithmetic
parameter_list|()
block|{
name|SlabStats
name|test
init|=
name|cache
operator|.
name|requestStats
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
name|test
operator|.
name|NUMDIVISIONS
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
literal|"Upper for index "
operator|+
name|i
operator|+
literal|" is "
operator|+
name|test
operator|.
name|getUpperBound
argument_list|(
name|i
argument_list|)
operator|+
literal|" lower "
operator|+
name|test
operator|.
name|getLowerBound
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|,
name|test
operator|.
name|getUpperBound
argument_list|(
name|i
argument_list|)
operator|<=
name|test
operator|.
name|getLowerBound
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testHeapSizeChanges
parameter_list|()
block|{
name|CacheTestUtils
operator|.
name|testHeapSizeChanges
argument_list|(
name|cache
argument_list|,
name|BLOCK_SIZE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

