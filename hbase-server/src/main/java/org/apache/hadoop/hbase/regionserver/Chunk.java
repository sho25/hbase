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
name|yetus
operator|.
name|audience
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
name|Bytes
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
name|annotations
operator|.
name|VisibleForTesting
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A chunk of memory out of which allocations are sliced.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|Chunk
block|{
comment|/** Actual underlying data */
specifier|protected
name|ByteBuffer
name|data
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|UNINITIALIZED
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|OOM
init|=
operator|-
literal|2
decl_stmt|;
comment|/**    * Offset for the next allocation, or the sentinel value -1 which implies that the chunk is still    * uninitialized.    */
specifier|protected
name|AtomicInteger
name|nextFreeOffset
init|=
operator|new
name|AtomicInteger
argument_list|(
name|UNINITIALIZED
argument_list|)
decl_stmt|;
comment|/** Total number of allocations satisfied from this buffer */
specifier|protected
name|AtomicInteger
name|allocCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
comment|/** Size of chunk in bytes */
specifier|protected
specifier|final
name|int
name|size
decl_stmt|;
comment|// The unique id associated with the chunk.
specifier|private
specifier|final
name|int
name|id
decl_stmt|;
comment|// indicates if the chunk is formed by ChunkCreator#MemstorePool
specifier|private
specifier|final
name|boolean
name|fromPool
decl_stmt|;
comment|/**    * Create an uninitialized chunk. Note that memory is not allocated yet, so    * this is cheap.    * @param size in bytes    * @param id the chunk id    */
specifier|public
name|Chunk
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|this
argument_list|(
name|size
argument_list|,
name|id
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an uninitialized chunk. Note that memory is not allocated yet, so    * this is cheap.    * @param size in bytes    * @param id the chunk id    * @param fromPool if the chunk is formed by pool    */
specifier|public
name|Chunk
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|id
parameter_list|,
name|boolean
name|fromPool
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|fromPool
operator|=
name|fromPool
expr_stmt|;
block|}
name|int
name|getId
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
name|boolean
name|isFromPool
parameter_list|()
block|{
return|return
name|this
operator|.
name|fromPool
return|;
block|}
name|boolean
name|isJumbo
parameter_list|()
block|{
return|return
name|size
operator|>
name|ChunkCreator
operator|.
name|getInstance
argument_list|()
operator|.
name|getChunkSize
argument_list|()
return|;
block|}
name|boolean
name|isIndexChunk
parameter_list|()
block|{
return|return
name|size
operator|==
name|ChunkCreator
operator|.
name|getInstance
argument_list|()
operator|.
name|getChunkSize
argument_list|(
name|ChunkCreator
operator|.
name|ChunkType
operator|.
name|INDEX_CHUNK
argument_list|)
return|;
block|}
comment|/**    * Actually claim the memory for this chunk. This should only be called from the thread that    * constructed the chunk. It is thread-safe against other threads calling alloc(), who will block    * until the allocation is complete.    */
specifier|public
name|void
name|init
parameter_list|()
block|{
assert|assert
name|nextFreeOffset
operator|.
name|get
argument_list|()
operator|==
name|UNINITIALIZED
assert|;
try|try
block|{
name|allocateDataBuffer
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|OutOfMemoryError
name|e
parameter_list|)
block|{
name|boolean
name|failInit
init|=
name|nextFreeOffset
operator|.
name|compareAndSet
argument_list|(
name|UNINITIALIZED
argument_list|,
name|OOM
argument_list|)
decl_stmt|;
assert|assert
name|failInit
assert|;
comment|// should be true.
throw|throw
name|e
throw|;
block|}
comment|// Mark that it's ready for use
comment|// Move 4 bytes since the first 4 bytes are having the chunkid in it
name|boolean
name|initted
init|=
name|nextFreeOffset
operator|.
name|compareAndSet
argument_list|(
name|UNINITIALIZED
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
comment|// We should always succeed the above CAS since only one thread
comment|// calls init()!
name|Preconditions
operator|.
name|checkState
argument_list|(
name|initted
argument_list|,
literal|"Multiple threads tried to init same chunk"
argument_list|)
expr_stmt|;
block|}
specifier|abstract
name|void
name|allocateDataBuffer
parameter_list|()
function_decl|;
comment|/**    * Reset the offset to UNINITIALIZED before before reusing an old chunk    */
name|void
name|reset
parameter_list|()
block|{
if|if
condition|(
name|nextFreeOffset
operator|.
name|get
argument_list|()
operator|!=
name|UNINITIALIZED
condition|)
block|{
name|nextFreeOffset
operator|.
name|set
argument_list|(
name|UNINITIALIZED
argument_list|)
expr_stmt|;
name|allocCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Try to allocate<code>size</code> bytes from the chunk.    * If a chunk is tried to get allocated before init() call, the thread doing the allocation    * will be in busy-wait state as it will keep looping till the nextFreeOffset is set.    * @return the offset of the successful allocation, or -1 to indicate not-enough-space    */
specifier|public
name|int
name|alloc
parameter_list|(
name|int
name|size
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|oldOffset
init|=
name|nextFreeOffset
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldOffset
operator|==
name|UNINITIALIZED
condition|)
block|{
comment|// The chunk doesn't have its data allocated yet.
comment|// Since we found this in curChunk, we know that whoever
comment|// CAS-ed it there is allocating it right now. So spin-loop
comment|// shouldn't spin long!
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|oldOffset
operator|==
name|OOM
condition|)
block|{
comment|// doh we ran out of ram. return -1 to chuck this away.
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|oldOffset
operator|+
name|size
operator|>
name|data
operator|.
name|capacity
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
comment|// alloc doesn't fit
block|}
comment|// TODO : If seqID is to be written add 8 bytes here for nextFreeOFfset
comment|// Try to atomically claim this chunk
if|if
condition|(
name|nextFreeOffset
operator|.
name|compareAndSet
argument_list|(
name|oldOffset
argument_list|,
name|oldOffset
operator|+
name|size
argument_list|)
condition|)
block|{
comment|// we got the alloc
name|allocCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
name|oldOffset
return|;
block|}
comment|// we raced and lost alloc, try again
block|}
block|}
comment|/**    * @return This chunk's backing data.    */
name|ByteBuffer
name|getData
parameter_list|()
block|{
return|return
name|this
operator|.
name|data
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
literal|"Chunk@"
operator|+
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
operator|+
literal|" allocs="
operator|+
name|allocCount
operator|.
name|get
argument_list|()
operator|+
literal|"waste="
operator|+
operator|(
name|data
operator|.
name|capacity
argument_list|()
operator|-
name|nextFreeOffset
operator|.
name|get
argument_list|()
operator|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|int
name|getNextFreeOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|nextFreeOffset
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

