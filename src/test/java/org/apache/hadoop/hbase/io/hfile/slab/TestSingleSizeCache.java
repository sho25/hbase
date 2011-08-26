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
name|SingleSizeCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Tests SingleSlabCache.  *<p>  *  * Tests will ensure that evictions operate when they're supposed to and do what  * they should, and that cached blocks are accessible when expected to be.  */
end_comment

begin_class
specifier|public
class|class
name|TestSingleSizeCache
block|{
name|SingleSizeCache
name|cache
decl_stmt|;
specifier|final
name|int
name|CACHE_SIZE
init|=
literal|1000000
decl_stmt|;
specifier|final
name|int
name|NUM_BLOCKS
init|=
literal|100
decl_stmt|;
specifier|final
name|int
name|BLOCK_SIZE
init|=
name|CACHE_SIZE
operator|/
name|NUM_BLOCKS
decl_stmt|;
specifier|final
name|int
name|NUM_THREADS
init|=
literal|100
decl_stmt|;
specifier|final
name|int
name|NUM_QUERIES
init|=
literal|10000
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
name|SingleSizeCache
argument_list|(
name|BLOCK_SIZE
argument_list|,
name|NUM_BLOCKS
argument_list|,
literal|null
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
block|}
end_class

end_unit

