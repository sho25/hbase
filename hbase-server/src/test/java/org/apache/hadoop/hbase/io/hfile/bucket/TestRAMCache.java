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
operator|.
name|bucket
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
name|AtomicBoolean
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
name|HConstants
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
name|BlockCacheKey
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
name|HFileBlock
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
name|HFileContext
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
name|HFileContextBuilder
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
name|bucket
operator|.
name|BucketCache
operator|.
name|RAMCache
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
name|bucket
operator|.
name|BucketCache
operator|.
name|RAMQueueEntry
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
name|nio
operator|.
name|ByteBuff
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
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRAMCache
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
name|TestRAMCache
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestRAMCache
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Define a mock HFileBlock.
specifier|private
specifier|static
class|class
name|MockHFileBlock
extends|extends
name|HFileBlock
block|{
specifier|private
specifier|volatile
name|CountDownLatch
name|latch
decl_stmt|;
name|MockHFileBlock
parameter_list|(
name|BlockType
name|blockType
parameter_list|,
name|int
name|onDiskSizeWithoutHeader
parameter_list|,
name|int
name|uncompressedSizeWithoutHeader
parameter_list|,
name|long
name|prevBlockOffset
parameter_list|,
name|ByteBuffer
name|b
parameter_list|,
name|boolean
name|fillHeader
parameter_list|,
name|long
name|offset
parameter_list|,
name|int
name|nextBlockOnDiskSize
parameter_list|,
name|int
name|onDiskDataSizeWithHeader
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|,
name|ByteBuffAllocator
name|allocator
parameter_list|)
block|{
name|super
argument_list|(
name|blockType
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|,
name|uncompressedSizeWithoutHeader
argument_list|,
name|prevBlockOffset
argument_list|,
name|ByteBuff
operator|.
name|wrap
argument_list|(
name|b
argument_list|)
argument_list|,
name|fillHeader
argument_list|,
name|offset
argument_list|,
name|nextBlockOnDiskSize
argument_list|,
name|onDiskDataSizeWithHeader
argument_list|,
name|fileContext
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setLatch
parameter_list|(
name|CountDownLatch
name|latch
parameter_list|)
block|{
name|this
operator|.
name|latch
operator|=
name|latch
expr_stmt|;
block|}
specifier|public
name|MockHFileBlock
name|retain
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|latch
operator|!=
literal|null
condition|)
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Interrupted exception error: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|retain
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAtomicRAMCache
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|size
init|=
literal|100
decl_stmt|;
name|int
name|length
init|=
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE
operator|+
name|size
decl_stmt|;
name|byte
index|[]
name|byteArr
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|RAMCache
name|cache
init|=
operator|new
name|RAMCache
argument_list|()
decl_stmt|;
name|BlockCacheKey
name|key
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"file-1"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|MockHFileBlock
name|blk
init|=
operator|new
name|MockHFileBlock
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|size
argument_list|,
name|size
argument_list|,
operator|-
literal|1
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|byteArr
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
operator|-
literal|1
argument_list|,
literal|52
argument_list|,
operator|-
literal|1
argument_list|,
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
name|ByteBuffAllocator
operator|.
name|HEAP
argument_list|)
decl_stmt|;
name|RAMQueueEntry
name|re
init|=
operator|new
name|RAMQueueEntry
argument_list|(
name|key
argument_list|,
name|blk
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
name|ByteBuffAllocator
operator|.
name|NONE
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|cache
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|re
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|cache
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|re
argument_list|)
argument_list|,
name|re
argument_list|)
expr_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|blk
operator|.
name|setLatch
argument_list|(
name|latch
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|error
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
name|t1
init|=
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|error
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|t1
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|removed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
name|t2
init|=
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
block|{
name|cache
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|removed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|t2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|removed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|removed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|error
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

