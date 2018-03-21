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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|IOTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
block|{
name|IOTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCachedBlockQueue
extends|extends
name|TestCase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestCachedBlockQueue
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|void
name|testQueue
parameter_list|()
throws|throws
name|Exception
block|{
name|CachedBlock
name|cb1
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb1"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb2
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb2"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb3
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb3"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb4
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb4"
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb5
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb5"
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb6
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1750
argument_list|,
literal|"cb6"
argument_list|,
literal|6
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb7
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb7"
argument_list|,
literal|7
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb8
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb8"
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb9
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb9"
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb10
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb10"
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|LruCachedBlockQueue
name|queue
init|=
operator|new
name|LruCachedBlockQueue
argument_list|(
literal|10000
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb1
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb2
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb3
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb4
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb5
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb6
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb7
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb8
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb9
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb10
argument_list|)
expr_stmt|;
comment|// We expect cb1 through cb8 to be in the queue
name|long
name|expectedSize
init|=
name|cb1
operator|.
name|heapSize
argument_list|()
operator|+
name|cb2
operator|.
name|heapSize
argument_list|()
operator|+
name|cb3
operator|.
name|heapSize
argument_list|()
operator|+
name|cb4
operator|.
name|heapSize
argument_list|()
operator|+
name|cb5
operator|.
name|heapSize
argument_list|()
operator|+
name|cb6
operator|.
name|heapSize
argument_list|()
operator|+
name|cb7
operator|.
name|heapSize
argument_list|()
operator|+
name|cb8
operator|.
name|heapSize
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|queue
operator|.
name|heapSize
argument_list|()
argument_list|,
name|expectedSize
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|8
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|queue
operator|.
name|pollLast
argument_list|()
operator|.
name|getCacheKey
argument_list|()
operator|.
name|getHfileName
argument_list|()
argument_list|,
literal|"cb"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testQueueSmallBlockEdgeCase
parameter_list|()
throws|throws
name|Exception
block|{
name|CachedBlock
name|cb1
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb1"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb2
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb2"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb3
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb3"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb4
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb4"
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb5
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb5"
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb6
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1750
argument_list|,
literal|"cb6"
argument_list|,
literal|6
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb7
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb7"
argument_list|,
literal|7
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb8
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb8"
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb9
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1000
argument_list|,
literal|"cb9"
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|CachedBlock
name|cb10
init|=
operator|new
name|CachedBlock
argument_list|(
literal|1500
argument_list|,
literal|"cb10"
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|LruCachedBlockQueue
name|queue
init|=
operator|new
name|LruCachedBlockQueue
argument_list|(
literal|10000
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb1
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb2
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb3
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb4
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb5
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb6
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb7
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb8
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb9
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb10
argument_list|)
expr_stmt|;
name|CachedBlock
name|cb0
init|=
operator|new
name|CachedBlock
argument_list|(
literal|10
operator|+
name|CachedBlock
operator|.
name|PER_BLOCK_OVERHEAD
argument_list|,
literal|"cb0"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|queue
operator|.
name|add
argument_list|(
name|cb0
argument_list|)
expr_stmt|;
comment|// This is older so we must include it, but it will not end up kicking
comment|// anything out because (heapSize - cb8.heapSize + cb0.heapSize< maxSize)
comment|// and we must always maintain heapSize>= maxSize once we achieve it.
comment|// We expect cb0 through cb8 to be in the queue
name|long
name|expectedSize
init|=
name|cb1
operator|.
name|heapSize
argument_list|()
operator|+
name|cb2
operator|.
name|heapSize
argument_list|()
operator|+
name|cb3
operator|.
name|heapSize
argument_list|()
operator|+
name|cb4
operator|.
name|heapSize
argument_list|()
operator|+
name|cb5
operator|.
name|heapSize
argument_list|()
operator|+
name|cb6
operator|.
name|heapSize
argument_list|()
operator|+
name|cb7
operator|.
name|heapSize
argument_list|()
operator|+
name|cb8
operator|.
name|heapSize
argument_list|()
operator|+
name|cb0
operator|.
name|heapSize
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|queue
operator|.
name|heapSize
argument_list|()
argument_list|,
name|expectedSize
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
literal|8
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|queue
operator|.
name|pollLast
argument_list|()
operator|.
name|getCacheKey
argument_list|()
operator|.
name|getHfileName
argument_list|()
argument_list|,
literal|"cb"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|CachedBlock
extends|extends
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
name|LruCachedBlock
block|{
specifier|public
name|CachedBlock
parameter_list|(
specifier|final
name|long
name|heapSize
parameter_list|,
name|String
name|name
parameter_list|,
name|long
name|accessTime
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|BlockCacheKey
argument_list|(
name|name
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|Cacheable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
operator|(
call|(
name|int
call|)
argument_list|(
name|heapSize
operator|-
name|CachedBlock
operator|.
name|PER_BLOCK_OVERHEAD
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedLength
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|ByteBuffer
name|destination
parameter_list|,
name|boolean
name|includeNextBlockMetadata
parameter_list|)
block|{             }
annotation|@
name|Override
specifier|public
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|getDeserializer
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|BlockType
name|getBlockType
parameter_list|()
block|{
return|return
name|BlockType
operator|.
name|DATA
return|;
block|}
annotation|@
name|Override
specifier|public
name|MemoryType
name|getMemoryType
parameter_list|()
block|{
return|return
name|MemoryType
operator|.
name|EXCLUSIVE
return|;
block|}
block|}
argument_list|,
name|accessTime
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

