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
name|regionserver
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
name|ArrayList
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
name|ByteBufferKeyValue
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
name|Cell
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
name|KeyValue
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
name|KeyValueUtil
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
name|util
operator|.
name|MemorySizeUtil
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
name|RegionServerTests
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|collect
operator|.
name|Iterables
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
name|collect
operator|.
name|Lists
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
name|collect
operator|.
name|Maps
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
name|primitives
operator|.
name|Ints
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestMemStoreLAB
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
name|TestMemStoreLAB
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|rk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|ChunkCreator
operator|.
name|initialize
argument_list|(
literal|1
operator|*
literal|1024
argument_list|,
literal|false
argument_list|,
literal|50
operator|*
literal|1024000L
argument_list|,
literal|0.2f
argument_list|,
name|MemStoreLAB
operator|.
name|POOL_INITIAL_SIZE_DEFAULT
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|globalMemStoreLimit
init|=
call|(
name|long
call|)
argument_list|(
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
operator|*
name|MemorySizeUtil
operator|.
name|getGlobalMemStoreHeapPercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|,
name|globalMemStoreLimit
argument_list|,
literal|0.2f
argument_list|,
name|MemStoreLAB
operator|.
name|POOL_INITIAL_SIZE_DEFAULT
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a bunch of random allocations    */
annotation|@
name|Test
specifier|public
name|void
name|testLABRandomAllocation
parameter_list|()
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLABImpl
argument_list|()
decl_stmt|;
name|int
name|expectedOff
init|=
literal|0
decl_stmt|;
name|ByteBuffer
name|lastBuffer
init|=
literal|null
decl_stmt|;
name|int
name|lastChunkId
init|=
operator|-
literal|1
decl_stmt|;
comment|// 100K iterations by 0-1K alloc -> 50MB expected
comment|// should be reasonable for unit test and also cover wraparound
comment|// behavior
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100000
condition|;
name|i
operator|++
control|)
block|{
name|int
name|valSize
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rk
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
operator|new
name|byte
index|[
name|valSize
index|]
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|ByteBufferKeyValue
name|newKv
init|=
operator|(
name|ByteBufferKeyValue
operator|)
name|mslab
operator|.
name|copyCellInto
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|newKv
operator|.
name|getBuffer
argument_list|()
operator|!=
name|lastBuffer
condition|)
block|{
comment|// since we add the chunkID at the 0th offset of the chunk and the
comment|// chunkid is an int we need to account for those 4 bytes
name|expectedOff
operator|=
name|Bytes
operator|.
name|SIZEOF_INT
expr_stmt|;
name|lastBuffer
operator|=
name|newKv
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|int
name|chunkId
init|=
name|newKv
operator|.
name|getBuffer
argument_list|()
operator|.
name|getInt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"chunkid should be different"
argument_list|,
name|chunkId
operator|!=
name|lastChunkId
argument_list|)
expr_stmt|;
name|lastChunkId
operator|=
name|chunkId
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedOff
argument_list|,
name|newKv
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Allocation overruns buffer"
argument_list|,
name|newKv
operator|.
name|getOffset
argument_list|()
operator|+
name|size
operator|<=
name|newKv
operator|.
name|getBuffer
argument_list|()
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
name|expectedOff
operator|+=
name|size
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLABLargeAllocation
parameter_list|()
block|{
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLABImpl
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rk
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
operator|new
name|byte
index|[
literal|2
operator|*
literal|1024
operator|*
literal|1024
index|]
argument_list|)
decl_stmt|;
name|Cell
name|newCell
init|=
name|mslab
operator|.
name|copyCellInto
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"2MB allocation shouldn't be satisfied by LAB."
argument_list|,
name|newCell
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test allocation from lots of threads, making sure the results don't    * overlap in any way    */
annotation|@
name|Test
specifier|public
name|void
name|testLABThreading
parameter_list|()
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
name|totalAllocated
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLABImpl
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|AllocRecord
argument_list|>
argument_list|>
name|allocations
init|=
name|Lists
operator|.
name|newArrayList
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|List
argument_list|<
name|AllocRecord
argument_list|>
name|allocsByThisThread
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
name|allocations
operator|.
name|add
argument_list|(
name|allocsByThisThread
argument_list|)
expr_stmt|;
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
specifier|private
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|valSize
init|=
name|r
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rk
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
operator|new
name|byte
index|[
name|valSize
index|]
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|ByteBufferKeyValue
name|newCell
init|=
operator|(
name|ByteBufferKeyValue
operator|)
name|mslab
operator|.
name|copyCellInto
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|totalAllocated
operator|.
name|addAndGet
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|allocsByThisThread
operator|.
name|add
argument_list|(
operator|new
name|AllocRecord
argument_list|(
name|newCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|newCell
operator|.
name|getOffset
argument_list|()
argument_list|,
name|size
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
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
name|totalAllocated
operator|.
name|get
argument_list|()
operator|<
literal|50
operator|*
literal|1024
operator|*
literal|1000
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
comment|// Partition the allocations by the actual byte[] they point into,
comment|// make sure offsets are unique for each chunk
name|Map
argument_list|<
name|ByteBuffer
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|AllocRecord
argument_list|>
argument_list|>
name|mapsByChunk
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|int
name|sizeCounted
init|=
literal|0
decl_stmt|;
for|for
control|(
name|AllocRecord
name|rec
range|:
name|Iterables
operator|.
name|concat
argument_list|(
name|allocations
argument_list|)
control|)
block|{
name|sizeCounted
operator|+=
name|rec
operator|.
name|size
expr_stmt|;
if|if
condition|(
name|rec
operator|.
name|size
operator|==
literal|0
condition|)
continue|continue;
name|Map
argument_list|<
name|Integer
argument_list|,
name|AllocRecord
argument_list|>
name|mapForThisByteArray
init|=
name|mapsByChunk
operator|.
name|get
argument_list|(
name|rec
operator|.
name|alloc
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapForThisByteArray
operator|==
literal|null
condition|)
block|{
name|mapForThisByteArray
operator|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
expr_stmt|;
name|mapsByChunk
operator|.
name|put
argument_list|(
name|rec
operator|.
name|alloc
argument_list|,
name|mapForThisByteArray
argument_list|)
expr_stmt|;
block|}
name|AllocRecord
name|oldVal
init|=
name|mapForThisByteArray
operator|.
name|put
argument_list|(
name|rec
operator|.
name|offset
argument_list|,
name|rec
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Already had an entry "
operator|+
name|oldVal
operator|+
literal|" for allocation "
operator|+
name|rec
argument_list|,
name|oldVal
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Sanity check test"
argument_list|,
name|sizeCounted
argument_list|,
name|totalAllocated
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now check each byte array to make sure allocations don't overlap
for|for
control|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|AllocRecord
argument_list|>
name|allocsInChunk
range|:
name|mapsByChunk
operator|.
name|values
argument_list|()
control|)
block|{
comment|// since we add the chunkID at the 0th offset of the chunk and the
comment|// chunkid is an int we need to account for those 4 bytes
name|int
name|expectedOff
init|=
name|Bytes
operator|.
name|SIZEOF_INT
decl_stmt|;
for|for
control|(
name|AllocRecord
name|alloc
range|:
name|allocsInChunk
operator|.
name|values
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|expectedOff
argument_list|,
name|alloc
operator|.
name|offset
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Allocation overruns buffer"
argument_list|,
name|alloc
operator|.
name|offset
operator|+
name|alloc
operator|.
name|size
operator|<=
name|alloc
operator|.
name|alloc
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
name|expectedOff
operator|+=
name|alloc
operator|.
name|size
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Test frequent chunk retirement with chunk pool triggered by lots of threads, making sure    * there's no memory leak (HBASE-16195)    * @throws Exception if any error occurred    */
annotation|@
name|Test
specifier|public
name|void
name|testLABChunkQueue
parameter_list|()
throws|throws
name|Exception
block|{
name|ChunkCreator
name|oldInstance
init|=
literal|null
decl_stmt|;
try|try
block|{
name|MemStoreLABImpl
name|mslab
init|=
operator|new
name|MemStoreLABImpl
argument_list|()
decl_stmt|;
comment|// by default setting, there should be no chunks initialized in the pool
name|assertTrue
argument_list|(
name|mslab
operator|.
name|getPooledChunks
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|oldInstance
operator|=
name|ChunkCreator
operator|.
name|instance
expr_stmt|;
name|ChunkCreator
operator|.
name|instance
operator|=
literal|null
expr_stmt|;
comment|// reset mslab with chunk pool
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setDouble
argument_list|(
name|MemStoreLAB
operator|.
name|CHUNK_POOL_MAXSIZE_KEY
argument_list|,
literal|0.1
argument_list|)
expr_stmt|;
comment|// set chunk size to default max alloc size, so we could easily trigger chunk retirement
name|conf
operator|.
name|setLong
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_KEY
argument_list|,
name|MemStoreLABImpl
operator|.
name|MAX_ALLOC_DEFAULT
argument_list|)
expr_stmt|;
comment|// reconstruct mslab
name|long
name|globalMemStoreLimit
init|=
call|(
name|long
call|)
argument_list|(
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
operator|*
name|MemorySizeUtil
operator|.
name|getGlobalMemStoreHeapPercent
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|MAX_ALLOC_DEFAULT
argument_list|,
literal|false
argument_list|,
name|globalMemStoreLimit
argument_list|,
literal|0.1f
argument_list|,
name|MemStoreLAB
operator|.
name|POOL_INITIAL_SIZE_DEFAULT
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ChunkCreator
operator|.
name|clearDisableFlag
argument_list|()
expr_stmt|;
name|mslab
operator|=
operator|new
name|MemStoreLABImpl
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// launch multiple threads to trigger frequent chunk retirement
name|List
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
operator|new
name|byte
index|[
name|MemStoreLABImpl
operator|.
name|MAX_ALLOC_DEFAULT
operator|-
literal|32
index|]
argument_list|)
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|threads
operator|.
name|add
argument_list|(
name|getChunkQueueTestThread
argument_list|(
name|mslab
argument_list|,
literal|"testLABChunkQueue-"
operator|+
name|i
argument_list|,
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// let it run for some time
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
name|boolean
name|threadsRunning
init|=
literal|true
decl_stmt|;
name|boolean
name|alive
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|threadsRunning
condition|)
block|{
name|alive
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
if|if
condition|(
name|thread
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|alive
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|alive
condition|)
block|{
name|threadsRunning
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|// none of the chunkIds would have been returned back
name|assertTrue
argument_list|(
literal|"All the chunks must have been cleared"
argument_list|,
name|ChunkCreator
operator|.
name|instance
operator|.
name|numberOfMappedChunks
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|int
name|pooledChunksNum
init|=
name|mslab
operator|.
name|getPooledChunks
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// close the mslab
name|mslab
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// make sure all chunks where reclaimed back to pool
name|int
name|queueLength
init|=
name|mslab
operator|.
name|getNumOfChunksReturnedToPool
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"All chunks in chunk queue should be reclaimed or removed"
operator|+
literal|" after mslab closed but actually: "
operator|+
operator|(
name|pooledChunksNum
operator|-
name|queueLength
operator|)
argument_list|,
name|pooledChunksNum
operator|-
name|queueLength
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|ChunkCreator
operator|.
name|instance
operator|=
name|oldInstance
expr_stmt|;
block|}
block|}
specifier|private
name|Thread
name|getChunkQueueTestThread
parameter_list|(
specifier|final
name|MemStoreLABImpl
name|mslab
parameter_list|,
name|String
name|threadName
parameter_list|,
name|Cell
name|cellToCopyInto
parameter_list|)
block|{
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|()
block|{
specifier|volatile
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
comment|// keep triggering chunk retirement
name|mslab
operator|.
name|copyCellInto
argument_list|(
name|cellToCopyInto
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|interrupt
parameter_list|()
block|{
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
block|}
decl_stmt|;
name|thread
operator|.
name|setName
argument_list|(
name|threadName
argument_list|)
expr_stmt|;
name|thread
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|thread
return|;
block|}
specifier|private
specifier|static
class|class
name|AllocRecord
implements|implements
name|Comparable
argument_list|<
name|AllocRecord
argument_list|>
block|{
specifier|private
specifier|final
name|ByteBuffer
name|alloc
decl_stmt|;
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
specifier|public
name|AllocRecord
parameter_list|(
name|ByteBuffer
name|alloc
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|alloc
operator|=
name|alloc
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|AllocRecord
name|e
parameter_list|)
block|{
if|if
condition|(
name|alloc
operator|!=
name|e
operator|.
name|alloc
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can only compare within a particular array"
argument_list|)
throw|;
block|}
return|return
name|Ints
operator|.
name|compare
argument_list|(
name|this
operator|.
name|offset
argument_list|,
name|e
operator|.
name|offset
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
return|return
literal|"AllocRecord(offset="
operator|+
name|this
operator|.
name|offset
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

