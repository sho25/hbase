begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertArrayEquals
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
name|assertNull
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|ConcurrentLinkedQueue
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
name|atomic
operator|.
name|AtomicInteger
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
name|MultithreadedTestUtil
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
name|MultithreadedTestUtil
operator|.
name|TestThread
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
name|util
operator|.
name|ChecksumType
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
name|metrics
operator|.
name|SchemaMetrics
import|;
end_import

begin_class
specifier|public
class|class
name|CacheTestUtils
block|{
specifier|private
specifier|static
specifier|final
name|boolean
name|includesMemstoreTS
init|=
literal|true
decl_stmt|;
comment|/**    * Just checks if heapsize grows when something is cached, and gets smaller    * when the same object is evicted    */
specifier|public
specifier|static
name|void
name|testHeapSizeChanges
parameter_list|(
specifier|final
name|BlockCache
name|toBeTested
parameter_list|,
specifier|final
name|int
name|blockSize
parameter_list|)
block|{
name|HFileBlockPair
index|[]
name|blocks
init|=
name|generateHFileBlocks
argument_list|(
name|blockSize
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|long
name|heapSize
init|=
operator|(
operator|(
name|HeapSize
operator|)
name|toBeTested
operator|)
operator|.
name|heapSize
argument_list|()
decl_stmt|;
name|toBeTested
operator|.
name|cacheBlock
argument_list|(
name|blocks
index|[
literal|0
index|]
operator|.
name|blockName
argument_list|,
name|blocks
index|[
literal|0
index|]
operator|.
name|block
argument_list|)
expr_stmt|;
comment|/*When we cache something HeapSize should always increase */
name|assertTrue
argument_list|(
name|heapSize
operator|<
operator|(
operator|(
name|HeapSize
operator|)
name|toBeTested
operator|)
operator|.
name|heapSize
argument_list|()
argument_list|)
expr_stmt|;
name|toBeTested
operator|.
name|evictBlock
argument_list|(
name|blocks
index|[
literal|0
index|]
operator|.
name|blockName
argument_list|)
expr_stmt|;
comment|/*Post eviction, heapsize should be the same */
name|assertEquals
argument_list|(
name|heapSize
argument_list|,
operator|(
operator|(
name|HeapSize
operator|)
name|toBeTested
operator|)
operator|.
name|heapSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|testCacheMultiThreaded
parameter_list|(
specifier|final
name|BlockCache
name|toBeTested
parameter_list|,
specifier|final
name|int
name|blockSize
parameter_list|,
specifier|final
name|int
name|numThreads
parameter_list|,
specifier|final
name|int
name|numQueries
parameter_list|,
specifier|final
name|double
name|passingScore
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|MultithreadedTestUtil
operator|.
name|TestContext
name|ctx
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|TestContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|totalQueries
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|ConcurrentLinkedQueue
argument_list|<
name|HFileBlockPair
argument_list|>
name|blocksToTest
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|HFileBlockPair
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|hits
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|miss
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|HFileBlockPair
index|[]
name|blocks
init|=
name|generateHFileBlocks
argument_list|(
name|numQueries
argument_list|,
name|blockSize
argument_list|)
decl_stmt|;
name|blocksToTest
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|blocks
argument_list|)
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
operator|<
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|TestThread
name|t
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|RepeatingTestThread
argument_list|(
name|ctx
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|blocksToTest
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|HFileBlockPair
name|ourBlock
init|=
name|blocksToTest
operator|.
name|poll
argument_list|()
decl_stmt|;
comment|// if we run out of blocks to test, then we should stop the tests.
if|if
condition|(
name|ourBlock
operator|==
literal|null
condition|)
block|{
name|ctx
operator|.
name|setStopFlag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
name|toBeTested
operator|.
name|cacheBlock
argument_list|(
name|ourBlock
operator|.
name|blockName
argument_list|,
name|ourBlock
operator|.
name|block
argument_list|)
expr_stmt|;
name|Cacheable
name|retrievedBlock
init|=
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|ourBlock
operator|.
name|blockName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|retrievedBlock
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|ourBlock
operator|.
name|block
argument_list|,
name|retrievedBlock
argument_list|)
expr_stmt|;
name|toBeTested
operator|.
name|evictBlock
argument_list|(
name|ourBlock
operator|.
name|blockName
argument_list|)
expr_stmt|;
name|hits
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|ourBlock
operator|.
name|blockName
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|miss
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|totalQueries
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|blocksToTest
operator|.
name|isEmpty
argument_list|()
operator|&&
name|ctx
operator|.
name|shouldRun
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|hits
operator|.
name|get
argument_list|()
operator|/
operator|(
operator|(
name|double
operator|)
name|hits
operator|.
name|get
argument_list|()
operator|+
operator|(
name|double
operator|)
name|miss
operator|.
name|get
argument_list|()
operator|)
operator|<
name|passingScore
condition|)
block|{
name|fail
argument_list|(
literal|"Too many nulls returned. Hits: "
operator|+
name|hits
operator|.
name|get
argument_list|()
operator|+
literal|" Misses: "
operator|+
name|miss
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|testCacheSimple
parameter_list|(
name|BlockCache
name|toBeTested
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|int
name|numBlocks
parameter_list|)
throws|throws
name|Exception
block|{
name|HFileBlockPair
index|[]
name|blocks
init|=
name|generateHFileBlocks
argument_list|(
name|numBlocks
argument_list|,
name|blockSize
argument_list|)
decl_stmt|;
comment|// Confirm empty
for|for
control|(
name|HFileBlockPair
name|block
range|:
name|blocks
control|)
block|{
name|assertNull
argument_list|(
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|block
operator|.
name|blockName
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Add blocks
for|for
control|(
name|HFileBlockPair
name|block
range|:
name|blocks
control|)
block|{
name|toBeTested
operator|.
name|cacheBlock
argument_list|(
name|block
operator|.
name|blockName
argument_list|,
name|block
operator|.
name|block
argument_list|)
expr_stmt|;
block|}
comment|// Check if all blocks are properly cached and contain the right
comment|// information, or the blocks are null.
comment|// MapMaker makes no guarantees when it will evict, so neither can we.
for|for
control|(
name|HFileBlockPair
name|block
range|:
name|blocks
control|)
block|{
name|HFileBlock
name|buf
init|=
operator|(
name|HFileBlock
operator|)
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|block
operator|.
name|blockName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|buf
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|block
operator|.
name|block
argument_list|,
name|buf
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Re-add some duplicate blocks. Hope nothing breaks.
for|for
control|(
name|HFileBlockPair
name|block
range|:
name|blocks
control|)
block|{
try|try
block|{
if|if
condition|(
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|block
operator|.
name|blockName
argument_list|,
literal|true
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|toBeTested
operator|.
name|cacheBlock
argument_list|(
name|block
operator|.
name|blockName
argument_list|,
name|block
operator|.
name|block
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Cache should not allow re-caching a block"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|re
parameter_list|)
block|{
comment|// expected
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|hammerSingleKey
parameter_list|(
specifier|final
name|BlockCache
name|toBeTested
parameter_list|,
name|int
name|BlockSize
parameter_list|,
name|int
name|numThreads
parameter_list|,
name|int
name|numQueries
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|BlockCacheKey
name|key
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
literal|5
operator|*
literal|1024
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|buf
argument_list|,
operator|(
name|byte
operator|)
literal|5
argument_list|)
expr_stmt|;
specifier|final
name|ByteArrayCacheable
name|bac
init|=
operator|new
name|ByteArrayCacheable
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|MultithreadedTestUtil
operator|.
name|TestContext
name|ctx
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|TestContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|totalQueries
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|toBeTested
operator|.
name|cacheBlock
argument_list|(
name|key
argument_list|,
name|bac
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
operator|<
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|TestThread
name|t
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|RepeatingTestThread
argument_list|(
name|ctx
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayCacheable
name|returned
init|=
operator|(
name|ByteArrayCacheable
operator|)
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|key
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|buf
argument_list|,
name|returned
operator|.
name|buf
argument_list|)
expr_stmt|;
name|totalQueries
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
while|while
condition|(
name|totalQueries
operator|.
name|get
argument_list|()
operator|<
name|numQueries
operator|&&
name|ctx
operator|.
name|shouldRun
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|hammerEviction
parameter_list|(
specifier|final
name|BlockCache
name|toBeTested
parameter_list|,
name|int
name|BlockSize
parameter_list|,
name|int
name|numThreads
parameter_list|,
name|int
name|numQueries
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|MultithreadedTestUtil
operator|.
name|TestContext
name|ctx
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|TestContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|totalQueries
init|=
operator|new
name|AtomicInteger
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
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|finalI
init|=
name|i
decl_stmt|;
specifier|final
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
literal|5
operator|*
literal|1024
index|]
decl_stmt|;
name|TestThread
name|t
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|RepeatingTestThread
argument_list|(
name|ctx
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|100
condition|;
name|j
operator|++
control|)
block|{
name|BlockCacheKey
name|key
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"key_"
operator|+
name|finalI
operator|+
literal|"_"
operator|+
name|j
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|buf
argument_list|,
call|(
name|byte
call|)
argument_list|(
name|finalI
operator|*
name|j
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ByteArrayCacheable
name|bac
init|=
operator|new
name|ByteArrayCacheable
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|ByteArrayCacheable
name|gotBack
init|=
operator|(
name|ByteArrayCacheable
operator|)
name|toBeTested
operator|.
name|getBlock
argument_list|(
name|key
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|gotBack
operator|!=
literal|null
condition|)
block|{
name|assertArrayEquals
argument_list|(
name|gotBack
operator|.
name|buf
argument_list|,
name|bac
operator|.
name|buf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|toBeTested
operator|.
name|cacheBlock
argument_list|(
name|key
argument_list|,
name|bac
argument_list|)
expr_stmt|;
block|}
block|}
name|totalQueries
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
while|while
condition|(
name|totalQueries
operator|.
name|get
argument_list|()
operator|<
name|numQueries
operator|&&
name|ctx
operator|.
name|shouldRun
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|toBeTested
operator|.
name|getStats
argument_list|()
operator|.
name|getEvictedCount
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|ByteArrayCacheable
implements|implements
name|Cacheable
block|{
specifier|final
name|byte
index|[]
name|buf
decl_stmt|;
specifier|public
name|ByteArrayCacheable
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
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
literal|4
operator|+
name|buf
operator|.
name|length
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
literal|4
operator|+
name|buf
operator|.
name|length
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
parameter_list|)
block|{
name|destination
operator|.
name|putInt
argument_list|(
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
name|destination
operator|.
name|put
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|destination
operator|.
name|rewind
argument_list|()
expr_stmt|;
block|}
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
return|return
operator|new
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Cacheable
name|deserialize
parameter_list|(
name|ByteBuffer
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|len
init|=
name|b
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
name|byte
name|buf
index|[]
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|b
operator|.
name|get
argument_list|(
name|buf
argument_list|)
expr_stmt|;
return|return
operator|new
name|ByteArrayCacheable
argument_list|(
name|buf
argument_list|)
return|;
block|}
block|}
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
name|SchemaMetrics
name|getSchemaMetrics
parameter_list|()
block|{
return|return
name|SchemaMetrics
operator|.
name|getUnknownInstanceForTest
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
name|HFileBlockPair
index|[]
name|generateHFileBlocks
parameter_list|(
name|int
name|blockSize
parameter_list|,
name|int
name|numBlocks
parameter_list|)
block|{
name|HFileBlockPair
index|[]
name|returnedBlocks
init|=
operator|new
name|HFileBlockPair
index|[
name|numBlocks
index|]
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|HashSet
argument_list|<
name|String
argument_list|>
name|usedStrings
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
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
name|numBlocks
condition|;
name|i
operator|++
control|)
block|{
comment|// The buffer serialized size needs to match the size of BlockSize. So we
comment|// declare our data size to be smaller than it by the serialization space
comment|// required.
name|ByteBuffer
name|cachedBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|blockSize
operator|-
name|HFileBlock
operator|.
name|EXTRA_SERIALIZATION_SPACE
argument_list|)
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|cachedBuffer
operator|.
name|array
argument_list|()
argument_list|)
expr_stmt|;
name|cachedBuffer
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|int
name|onDiskSizeWithoutHeader
init|=
name|blockSize
operator|-
name|HFileBlock
operator|.
name|EXTRA_SERIALIZATION_SPACE
decl_stmt|;
name|int
name|uncompressedSizeWithoutHeader
init|=
name|blockSize
operator|-
name|HFileBlock
operator|.
name|EXTRA_SERIALIZATION_SPACE
decl_stmt|;
name|long
name|prevBlockOffset
init|=
name|rand
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|BlockType
operator|.
name|DATA
operator|.
name|write
argument_list|(
name|cachedBuffer
argument_list|)
expr_stmt|;
name|cachedBuffer
operator|.
name|putInt
argument_list|(
name|onDiskSizeWithoutHeader
argument_list|)
expr_stmt|;
name|cachedBuffer
operator|.
name|putInt
argument_list|(
name|uncompressedSizeWithoutHeader
argument_list|)
expr_stmt|;
name|cachedBuffer
operator|.
name|putLong
argument_list|(
name|prevBlockOffset
argument_list|)
expr_stmt|;
name|cachedBuffer
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|HFileBlock
name|generated
init|=
operator|new
name|HFileBlock
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|,
name|uncompressedSizeWithoutHeader
argument_list|,
name|prevBlockOffset
argument_list|,
name|cachedBuffer
argument_list|,
name|HFileBlock
operator|.
name|DONT_FILL_HEADER
argument_list|,
name|blockSize
argument_list|,
name|includesMemstoreTS
argument_list|,
name|HFileBlock
operator|.
name|MINOR_VERSION_NO_CHECKSUM
argument_list|,
literal|0
argument_list|,
name|ChecksumType
operator|.
name|NULL
operator|.
name|getCode
argument_list|()
argument_list|,
name|onDiskSizeWithoutHeader
operator|+
name|HFileBlock
operator|.
name|HEADER_SIZE
argument_list|)
decl_stmt|;
name|String
name|strKey
decl_stmt|;
comment|/* No conflicting keys */
for|for
control|(
name|strKey
operator|=
operator|new
name|Long
argument_list|(
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
init|;
operator|!
name|usedStrings
operator|.
name|add
argument_list|(
name|strKey
argument_list|)
condition|;
name|strKey
operator|=
operator|new
name|Long
argument_list|(
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
control|)
empty_stmt|;
name|returnedBlocks
index|[
name|i
index|]
operator|=
operator|new
name|HFileBlockPair
argument_list|()
expr_stmt|;
name|returnedBlocks
index|[
name|i
index|]
operator|.
name|blockName
operator|=
operator|new
name|BlockCacheKey
argument_list|(
name|strKey
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|returnedBlocks
index|[
name|i
index|]
operator|.
name|block
operator|=
name|generated
expr_stmt|;
block|}
return|return
name|returnedBlocks
return|;
block|}
specifier|private
specifier|static
class|class
name|HFileBlockPair
block|{
name|BlockCacheKey
name|blockName
decl_stmt|;
name|HFileBlock
name|block
decl_stmt|;
block|}
block|}
end_class

end_unit

