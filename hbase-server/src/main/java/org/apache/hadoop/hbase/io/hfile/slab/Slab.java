begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|LinkedBlockingQueue
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
name|classification
operator|.
name|InterfaceAudience
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
name|ClassSize
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
name|DirectMemoryUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Slab is a class which is designed to allocate blocks of a certain size.  * Constructor creates a number of DirectByteBuffers and slices them into the  * requisite size, then puts them all in a buffer.  **/
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|Slab
implements|implements
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
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Slab
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** This is where our items, or blocks of the slab, are stored. */
specifier|private
name|LinkedBlockingQueue
argument_list|<
name|ByteBuffer
argument_list|>
name|buffers
decl_stmt|;
comment|/** This is where our Slabs are stored */
specifier|private
name|ConcurrentLinkedQueue
argument_list|<
name|ByteBuffer
argument_list|>
name|slabs
decl_stmt|;
specifier|private
specifier|final
name|int
name|blockSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|numBlocks
decl_stmt|;
specifier|private
name|long
name|heapSize
decl_stmt|;
name|Slab
parameter_list|(
name|int
name|blockSize
parameter_list|,
name|int
name|numBlocks
parameter_list|)
block|{
name|buffers
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
expr_stmt|;
name|slabs
operator|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|blockSize
operator|=
name|blockSize
expr_stmt|;
name|this
operator|.
name|numBlocks
operator|=
name|numBlocks
expr_stmt|;
name|this
operator|.
name|heapSize
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
name|maxBlocksPerSlab
init|=
name|Integer
operator|.
name|MAX_VALUE
operator|/
name|blockSize
decl_stmt|;
name|int
name|maxSlabSize
init|=
name|maxBlocksPerSlab
operator|*
name|blockSize
decl_stmt|;
name|int
name|numFullSlabs
init|=
name|numBlocks
operator|/
name|maxBlocksPerSlab
decl_stmt|;
name|int
name|partialSlabSize
init|=
operator|(
name|numBlocks
operator|%
name|maxBlocksPerSlab
operator|)
operator|*
name|blockSize
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
name|numFullSlabs
condition|;
name|i
operator|++
control|)
block|{
name|allocateAndSlice
argument_list|(
name|maxSlabSize
argument_list|,
name|blockSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partialSlabSize
operator|>
literal|0
condition|)
block|{
name|allocateAndSlice
argument_list|(
name|partialSlabSize
argument_list|,
name|blockSize
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|allocateAndSlice
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|sliceSize
parameter_list|)
block|{
name|ByteBuffer
name|newSlab
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|slabs
operator|.
name|add
argument_list|(
name|newSlab
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|newSlab
operator|.
name|capacity
argument_list|()
condition|;
name|j
operator|+=
name|sliceSize
control|)
block|{
name|newSlab
operator|.
name|limit
argument_list|(
name|j
operator|+
name|sliceSize
argument_list|)
operator|.
name|position
argument_list|(
name|j
argument_list|)
expr_stmt|;
name|ByteBuffer
name|aSlice
init|=
name|newSlab
operator|.
name|slice
argument_list|()
decl_stmt|;
name|buffers
operator|.
name|add
argument_list|(
name|aSlice
argument_list|)
expr_stmt|;
name|heapSize
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|aSlice
operator|.
name|getClass
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Shutdown deallocates the memory for all the DirectByteBuffers. Each    * DirectByteBuffer has a "cleaner" method, which is similar to a    * deconstructor in C++.    */
name|void
name|shutdown
parameter_list|()
block|{
for|for
control|(
name|ByteBuffer
name|aSlab
range|:
name|slabs
control|)
block|{
try|try
block|{
name|DirectMemoryUtils
operator|.
name|destroyDirectByteBuffer
argument_list|(
name|aSlab
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to deallocate direct memory during shutdown"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|int
name|getBlockSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|blockSize
return|;
block|}
name|int
name|getBlockCapacity
parameter_list|()
block|{
return|return
name|this
operator|.
name|numBlocks
return|;
block|}
name|int
name|getBlocksRemaining
parameter_list|()
block|{
return|return
name|this
operator|.
name|buffers
operator|.
name|size
argument_list|()
return|;
block|}
comment|/*    * Throws an exception if you try to allocate a    * bigger size than the allocator can handle. Alloc will block until a buffer is available.    */
name|ByteBuffer
name|alloc
parameter_list|(
name|int
name|bufferSize
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|int
name|newCapacity
init|=
name|Preconditions
operator|.
name|checkPositionIndex
argument_list|(
name|bufferSize
argument_list|,
name|blockSize
argument_list|)
decl_stmt|;
name|ByteBuffer
name|returnedBuffer
init|=
name|buffers
operator|.
name|take
argument_list|()
decl_stmt|;
name|returnedBuffer
operator|.
name|clear
argument_list|()
operator|.
name|limit
argument_list|(
name|newCapacity
argument_list|)
expr_stmt|;
return|return
name|returnedBuffer
return|;
block|}
name|void
name|free
parameter_list|(
name|ByteBuffer
name|toBeFreed
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|toBeFreed
operator|.
name|capacity
argument_list|()
operator|==
name|blockSize
argument_list|)
expr_stmt|;
name|buffers
operator|.
name|add
argument_list|(
name|toBeFreed
argument_list|)
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
name|heapSize
return|;
block|}
block|}
end_class

end_unit

