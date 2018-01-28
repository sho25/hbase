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
name|Random
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
name|Ignore
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
name|Ignore
comment|// See HBASE-19742 for issue on reenabling.
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
name|TestMemstoreLABWithoutPool
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
name|TestMemstoreLABWithoutPool
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
literal|0.8
argument_list|)
decl_stmt|;
comment|// disable pool
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|,
literal|false
argument_list|,
name|globalMemStoreLimit
argument_list|,
literal|0.0f
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
literal|1000
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
comment|/**    * Test frequent chunk retirement with chunk pool triggered by lots of threads, making sure    * there's no memory leak (HBASE-16195)    * @throws Exception if any error occurred    */
annotation|@
name|Test
specifier|public
name|void
name|testLABChunkQueueWithMultipleMSLABs
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|MemStoreLABImpl
index|[]
name|mslab
init|=
operator|new
name|MemStoreLABImpl
index|[
literal|10
index|]
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
name|mslab
index|[
name|i
index|]
operator|=
operator|new
name|MemStoreLABImpl
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
comment|// create smaller sized kvs
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
literal|0
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
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|10
condition|;
name|j
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
index|[
name|i
index|]
argument_list|,
literal|"testLABChunkQueue-"
operator|+
name|j
argument_list|,
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
literal|3000
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
comment|// close the mslab
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
name|mslab
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// all of the chunkIds would have been returned back
name|assertTrue
argument_list|(
literal|"All the chunks must have been cleared"
argument_list|,
name|ChunkCreator
operator|.
name|INSTANCE
operator|.
name|numberOfMappedChunks
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

