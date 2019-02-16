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
name|Queue
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
name|nio
operator|.
name|MultiByteBuff
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
name|SingleByteBuff
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

begin_comment
comment|/**  * ByteBuffAllocator is used for allocating/freeing the ByteBuffers from/to NIO ByteBuffer pool, and  * it provide high-level interfaces for upstream. when allocating desired memory size, it will  * return {@link ByteBuff}, if we are sure that those ByteBuffers have reached the end of life  * cycle, we must do the {@link ByteBuff#release()} to return back the buffers to the pool,  * otherwise ByteBuffers leak will happen, and the NIO ByteBuffer pool may be exhausted. there's  * possible that the desired memory size is large than ByteBufferPool has, we'll downgrade to  * allocate ByteBuffers from heap which meaning the GC pressure may increase again. Of course, an  * better way is increasing the ByteBufferPool size if we detected this case.<br/>  *<br/>  * On the other hand, for better memory utilization, we have set an lower bound named  * minSizeForReservoirUse in this allocator, and if the desired size is less than  * minSizeForReservoirUse, the allocator will just allocate the ByteBuffer from heap and let the JVM  * free its memory, because it's too wasting to allocate a single fixed-size ByteBuffer for some  * small objects.<br/>  *<br/>  * We recommend to use this class to allocate/free {@link ByteBuff} in the RPC layer or the entire  * read/write path, because it hide the details of memory management and its APIs are more friendly  * to the upper layer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteBuffAllocator
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
name|ByteBuffAllocator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_BUFFER_COUNT_KEY
init|=
literal|"hbase.ipc.server.allocator.max.buffer.count"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BUFFER_SIZE_KEY
init|=
literal|"hbase.ipc.server.allocator.buffer.size"
decl_stmt|;
comment|// 64 KB. Making it same as the chunk size what we will write/read to/from the socket channel.
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BUFFER_SIZE
init|=
literal|64
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIN_ALLOCATE_SIZE_KEY
init|=
literal|"hbase.ipc.server.reservoir.minimal.allocating.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Recycler
name|NONE
init|=
parameter_list|()
lambda|->
block|{   }
decl_stmt|;
specifier|public
interface|interface
name|Recycler
block|{
name|void
name|free
parameter_list|()
function_decl|;
block|}
specifier|private
specifier|final
name|boolean
name|reservoirEnabled
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxBufCount
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|usedBufCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|maxPoolSizeInfoLevelLogged
init|=
literal|false
decl_stmt|;
comment|// If the desired size is at least this size, it'll allocated from ByteBufferPool, otherwise it'll
comment|// allocated from heap for better utilization. We make this to be 1/6th of the pool buffer size.
specifier|private
specifier|final
name|int
name|minSizeForReservoirUse
decl_stmt|;
specifier|private
specifier|final
name|Queue
argument_list|<
name|ByteBuffer
argument_list|>
name|buffers
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Initialize an {@link ByteBuffAllocator} which will try to allocate ByteBuffers from off-heap if    * reservoir is enabled and the reservoir has enough buffers, otherwise the allocator will just    * allocate the insufficient buffers from on-heap to meet the requirement.    * @param conf which get the arguments to initialize the allocator.    * @param reservoirEnabled indicate whether the reservoir is enabled or disabled.    * @return ByteBuffAllocator to manage the byte buffers.    */
specifier|public
specifier|static
name|ByteBuffAllocator
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|reservoirEnabled
parameter_list|)
block|{
name|int
name|poolBufSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|BUFFER_SIZE_KEY
argument_list|,
name|DEFAULT_BUFFER_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|reservoirEnabled
condition|)
block|{
comment|// The max number of buffers to be pooled in the ByteBufferPool. The default value been
comment|// selected based on the #handlers configured. When it is read request, 2 MB is the max size
comment|// at which we will send back one RPC request. Means max we need 2 MB for creating the
comment|// response cell block. (Well it might be much lesser than this because in 2 MB size calc, we
comment|// include the heap size overhead of each cells also.) Considering 2 MB, we will need
comment|// (2 * 1024 * 1024) / poolBufSize buffers to make the response cell block. Pool buffer size
comment|// is by default 64 KB.
comment|// In case of read request, at the end of the handler process, we will make the response
comment|// cellblock and add the Call to connection's response Q and a single Responder thread takes
comment|// connections and responses from that one by one and do the socket write. So there is chances
comment|// that by the time a handler originated response is actually done writing to socket and so
comment|// released the BBs it used, the handler might have processed one more read req. On an avg 2x
comment|// we consider and consider that also for the max buffers to pool
name|int
name|bufsForTwoMB
init|=
operator|(
literal|2
operator|*
literal|1024
operator|*
literal|1024
operator|)
operator|/
name|poolBufSize
decl_stmt|;
name|int
name|maxBuffCount
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_BUFFER_COUNT_KEY
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HANDLER_COUNT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGION_SERVER_HANDLER_COUNT
argument_list|)
operator|*
name|bufsForTwoMB
operator|*
literal|2
argument_list|)
decl_stmt|;
name|int
name|minSizeForReservoirUse
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MIN_ALLOCATE_SIZE_KEY
argument_list|,
name|poolBufSize
operator|/
literal|6
argument_list|)
decl_stmt|;
return|return
operator|new
name|ByteBuffAllocator
argument_list|(
literal|true
argument_list|,
name|maxBuffCount
argument_list|,
name|poolBufSize
argument_list|,
name|minSizeForReservoirUse
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ByteBuffAllocator
argument_list|(
literal|false
argument_list|,
literal|0
argument_list|,
name|poolBufSize
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
comment|/**    * Initialize an {@link ByteBuffAllocator} which only allocate ByteBuffer from on-heap, it's    * designed for testing purpose or disabled reservoir case.    * @return allocator to allocate on-heap ByteBuffer.    */
specifier|public
specifier|static
name|ByteBuffAllocator
name|createOnHeap
parameter_list|()
block|{
return|return
operator|new
name|ByteBuffAllocator
argument_list|(
literal|false
argument_list|,
literal|0
argument_list|,
name|DEFAULT_BUFFER_SIZE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|ByteBuffAllocator
parameter_list|(
name|boolean
name|reservoirEnabled
parameter_list|,
name|int
name|maxBufCount
parameter_list|,
name|int
name|bufSize
parameter_list|,
name|int
name|minSizeForReservoirUse
parameter_list|)
block|{
name|this
operator|.
name|reservoirEnabled
operator|=
name|reservoirEnabled
expr_stmt|;
name|this
operator|.
name|maxBufCount
operator|=
name|maxBufCount
expr_stmt|;
name|this
operator|.
name|bufSize
operator|=
name|bufSize
expr_stmt|;
name|this
operator|.
name|minSizeForReservoirUse
operator|=
name|minSizeForReservoirUse
expr_stmt|;
block|}
specifier|public
name|boolean
name|isReservoirEnabled
parameter_list|()
block|{
return|return
name|reservoirEnabled
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getQueueSize
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
comment|/**    * Allocate an buffer with buffer size from ByteBuffAllocator, Note to call the    * {@link ByteBuff#release()} if no need any more, otherwise the memory leak happen in NIO    * ByteBuffer pool.    * @return an ByteBuff with the buffer size.    */
specifier|public
name|SingleByteBuff
name|allocateOneBuffer
parameter_list|()
block|{
if|if
condition|(
name|isReservoirEnabled
argument_list|()
condition|)
block|{
name|ByteBuffer
name|bb
init|=
name|getBuffer
argument_list|()
decl_stmt|;
if|if
condition|(
name|bb
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|SingleByteBuff
argument_list|(
parameter_list|()
lambda|->
name|putbackBuffer
argument_list|(
name|bb
argument_list|)
argument_list|,
name|bb
argument_list|)
return|;
block|}
block|}
comment|// Allocated from heap, let the JVM free its memory.
return|return
operator|new
name|SingleByteBuff
argument_list|(
name|NONE
argument_list|,
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|this
operator|.
name|bufSize
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Allocate size bytes from the ByteBufAllocator, Note to call the {@link ByteBuff#release()} if    * no need any more, otherwise the memory leak happen in NIO ByteBuffer pool.    * @param size to allocate    * @return an ByteBuff with the desired size.    */
specifier|public
name|ByteBuff
name|allocate
parameter_list|(
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"size to allocate should>=0"
argument_list|)
throw|;
block|}
comment|// If disabled the reservoir, just allocate it from on-heap.
if|if
condition|(
operator|!
name|isReservoirEnabled
argument_list|()
operator|||
name|size
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|SingleByteBuff
argument_list|(
name|NONE
argument_list|,
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
argument_list|)
argument_list|)
return|;
block|}
name|int
name|reminder
init|=
name|size
operator|%
name|bufSize
decl_stmt|;
name|int
name|len
init|=
name|size
operator|/
name|bufSize
operator|+
operator|(
name|reminder
operator|>
literal|0
condition|?
literal|1
else|:
literal|0
operator|)
decl_stmt|;
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|bbs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|len
argument_list|)
decl_stmt|;
comment|// Allocate from ByteBufferPool until the remaining is less than minSizeForReservoirUse or
comment|// reservoir is exhausted.
name|int
name|remain
init|=
name|size
decl_stmt|;
while|while
condition|(
name|remain
operator|>=
name|minSizeForReservoirUse
condition|)
block|{
name|ByteBuffer
name|bb
init|=
name|this
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
if|if
condition|(
name|bb
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|bbs
operator|.
name|add
argument_list|(
name|bb
argument_list|)
expr_stmt|;
name|remain
operator|-=
name|bufSize
expr_stmt|;
block|}
name|int
name|lenFromReservoir
init|=
name|bbs
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|remain
operator|>
literal|0
condition|)
block|{
comment|// If the last ByteBuffer is too small or the reservoir can not provide more ByteBuffers, we
comment|// just allocate the ByteBuffer from on-heap.
name|bbs
operator|.
name|add
argument_list|(
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|remain
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ByteBuff
name|bb
init|=
name|wrap
argument_list|(
name|bbs
argument_list|,
parameter_list|()
lambda|->
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
name|lenFromReservoir
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|putbackBuffer
argument_list|(
name|bbs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|bb
operator|.
name|limit
argument_list|(
name|size
argument_list|)
expr_stmt|;
return|return
name|bb
return|;
block|}
specifier|public
specifier|static
name|ByteBuff
name|wrap
parameter_list|(
name|ByteBuffer
index|[]
name|buffers
parameter_list|,
name|Recycler
name|recycler
parameter_list|)
block|{
if|if
condition|(
name|buffers
operator|==
literal|null
operator|||
name|buffers
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"buffers shouldn't be null or empty"
argument_list|)
throw|;
block|}
return|return
name|buffers
operator|.
name|length
operator|==
literal|1
condition|?
operator|new
name|SingleByteBuff
argument_list|(
name|recycler
argument_list|,
name|buffers
index|[
literal|0
index|]
argument_list|)
else|:
operator|new
name|MultiByteBuff
argument_list|(
name|recycler
argument_list|,
name|buffers
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ByteBuff
name|wrap
parameter_list|(
name|ByteBuffer
index|[]
name|buffers
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|buffers
argument_list|,
name|NONE
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ByteBuff
name|wrap
parameter_list|(
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|buffers
parameter_list|,
name|Recycler
name|recycler
parameter_list|)
block|{
if|if
condition|(
name|buffers
operator|==
literal|null
operator|||
name|buffers
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"buffers shouldn't be null or empty"
argument_list|)
throw|;
block|}
return|return
name|buffers
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
operator|new
name|SingleByteBuff
argument_list|(
name|recycler
argument_list|,
name|buffers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
else|:
operator|new
name|MultiByteBuff
argument_list|(
name|recycler
argument_list|,
name|buffers
operator|.
name|toArray
argument_list|(
operator|new
name|ByteBuffer
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ByteBuff
name|wrap
parameter_list|(
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|buffers
parameter_list|)
block|{
return|return
name|wrap
argument_list|(
name|buffers
argument_list|,
name|NONE
argument_list|)
return|;
block|}
comment|/**    * @return One free DirectByteBuffer from the pool. If no free ByteBuffer and we have not reached    *         the maximum pool size, it will create a new one and return. In case of max pool size    *         also reached, will return null. When pool returned a ByteBuffer, make sure to return it    *         back to pool after use.    */
specifier|private
name|ByteBuffer
name|getBuffer
parameter_list|()
block|{
name|ByteBuffer
name|bb
init|=
name|buffers
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|bb
operator|!=
literal|null
condition|)
block|{
comment|// To reset the limit to capacity and position to 0, must clear here.
name|bb
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|bb
return|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|c
init|=
name|this
operator|.
name|usedBufCount
operator|.
name|intValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|c
operator|>=
name|this
operator|.
name|maxBufCount
condition|)
block|{
if|if
condition|(
operator|!
name|maxPoolSizeInfoLevelLogged
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Pool already reached its max capacity : {} and no free buffers now. Consider "
operator|+
literal|"increasing the value for '{}' ?"
argument_list|,
name|maxBufCount
argument_list|,
name|MAX_BUFFER_COUNT_KEY
argument_list|)
expr_stmt|;
name|maxPoolSizeInfoLevelLogged
operator|=
literal|true
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|this
operator|.
name|usedBufCount
operator|.
name|compareAndSet
argument_list|(
name|c
argument_list|,
name|c
operator|+
literal|1
argument_list|)
condition|)
block|{
continue|continue;
block|}
return|return
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|bufSize
argument_list|)
return|;
block|}
block|}
comment|/**    * Return back a ByteBuffer after its use. Don't read/write the ByteBuffer after the returning.    * @param buf ByteBuffer to return.    */
specifier|private
name|void
name|putbackBuffer
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|.
name|capacity
argument_list|()
operator|!=
name|bufSize
operator|||
operator|(
name|reservoirEnabled
operator|^
name|buf
operator|.
name|isDirect
argument_list|()
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Trying to put a buffer, not created by this pool! Will be just ignored"
argument_list|)
expr_stmt|;
return|return;
block|}
name|buffers
operator|.
name|offer
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

