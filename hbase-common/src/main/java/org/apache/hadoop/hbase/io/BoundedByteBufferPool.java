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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Like Hadoops' ByteBufferPool only you do not specify desired size when getting a ByteBuffer.  * This pool keeps an upper bound on the count of ByteBuffers in the pool and on the maximum size  * of ByteBuffer that it will retain (Hence the pool is 'bounded' as opposed to, say,  * Hadoop's ElasticByteBuffferPool).  * If a ByteBuffer is bigger than the configured threshold, we will just let the ByteBuffer go  * rather than add it to the pool. If more ByteBuffers than the configured maximum instances,  * we will not add the passed ByteBuffer to the pool; we will just drop it  * (we will log a WARN in this case that we are at capacity).  *  *<p>The intended use case is a reservoir of bytebuffers that an RPC can reuse; buffers tend to  * achieve a particular 'run' size over time give or take a few extremes. Set TRACE level on this  * class for a couple of seconds to get reporting on how it is running when deployed.  *  *<p>This pool returns off heap ByteBuffers.  *  *<p>This class is thread safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BoundedByteBufferPool
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BoundedByteBufferPool
operator|.
name|class
argument_list|)
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
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|VisibleForTesting
name|int
name|getQueueSize
parameter_list|()
block|{
return|return
name|buffers
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
specifier|final
name|int
name|maxToCache
decl_stmt|;
comment|// Maximum size of a ByteBuffer to retain in pool
specifier|private
specifier|final
name|int
name|maxByteBufferSizeToCache
decl_stmt|;
comment|// A running average only it only rises, it never recedes
specifier|private
specifier|final
name|AtomicInteger
name|runningAverageRef
decl_stmt|;
annotation|@
name|VisibleForTesting
name|int
name|getRunningAverage
parameter_list|()
block|{
return|return
name|runningAverageRef
operator|.
name|get
argument_list|()
return|;
block|}
comment|// Count (lower 32bit) and total capacity (upper 32bit) of pooled bytebuffers.
comment|// Both are non-negative. They are equal to or larger than those of the actual
comment|// queued buffers in any transition.
specifier|private
specifier|final
name|AtomicLong
name|stateRef
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
name|int
name|toCountOfBuffers
parameter_list|(
name|long
name|state
parameter_list|)
block|{
return|return
operator|(
name|int
operator|)
name|state
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|int
name|toTotalCapacity
parameter_list|(
name|long
name|state
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|state
operator|>>>
literal|32
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|long
name|toState
parameter_list|(
name|int
name|countOfBuffers
parameter_list|,
name|int
name|totalCapacity
parameter_list|)
block|{
return|return
operator|(
operator|(
name|long
operator|)
name|totalCapacity
operator|<<
literal|32
operator|)
operator||
name|countOfBuffers
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|long
name|subtractOneBufferFromState
parameter_list|(
name|long
name|state
parameter_list|,
name|int
name|capacity
parameter_list|)
block|{
return|return
name|state
operator|-
operator|(
operator|(
name|long
operator|)
name|capacity
operator|<<
literal|32
operator|)
operator|-
literal|1
return|;
block|}
comment|// For reporting, only used in the log
specifier|private
specifier|final
name|AtomicLong
name|allocationsRef
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/**    * @param maxByteBufferSizeToCache    * @param initialByteBufferSize    * @param maxToCache    */
specifier|public
name|BoundedByteBufferPool
parameter_list|(
specifier|final
name|int
name|maxByteBufferSizeToCache
parameter_list|,
specifier|final
name|int
name|initialByteBufferSize
parameter_list|,
specifier|final
name|int
name|maxToCache
parameter_list|)
block|{
name|this
operator|.
name|maxByteBufferSizeToCache
operator|=
name|maxByteBufferSizeToCache
expr_stmt|;
name|this
operator|.
name|runningAverageRef
operator|=
operator|new
name|AtomicInteger
argument_list|(
name|initialByteBufferSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxToCache
operator|=
name|maxToCache
expr_stmt|;
block|}
specifier|public
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
name|long
name|state
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|prevState
init|=
name|stateRef
operator|.
name|get
argument_list|()
decl_stmt|;
name|state
operator|=
name|subtractOneBufferFromState
argument_list|(
name|prevState
argument_list|,
name|bb
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|stateRef
operator|.
name|compareAndSet
argument_list|(
name|prevState
argument_list|,
name|state
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
comment|// Clear sets limit == capacity. Postion == 0.
name|bb
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|int
name|countOfBuffers
init|=
name|toCountOfBuffers
argument_list|(
name|state
argument_list|)
decl_stmt|;
name|int
name|totalCapacity
init|=
name|toTotalCapacity
argument_list|(
name|state
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"totalCapacity="
operator|+
name|totalCapacity
operator|+
literal|", count="
operator|+
name|countOfBuffers
argument_list|)
expr_stmt|;
block|}
return|return
name|bb
return|;
block|}
name|int
name|runningAverage
init|=
name|runningAverageRef
operator|.
name|get
argument_list|()
decl_stmt|;
name|bb
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|runningAverage
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|long
name|allocations
init|=
name|allocationsRef
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"runningAverage="
operator|+
name|runningAverage
operator|+
literal|", alloctions="
operator|+
name|allocations
argument_list|)
expr_stmt|;
block|}
return|return
name|bb
return|;
block|}
specifier|public
name|void
name|putBuffer
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|)
block|{
comment|// If buffer is larger than we want to keep around, just let it go.
if|if
condition|(
name|bb
operator|.
name|capacity
argument_list|()
operator|>
name|maxByteBufferSizeToCache
condition|)
block|{
return|return;
block|}
name|int
name|countOfBuffers
decl_stmt|;
name|int
name|totalCapacity
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|prevState
init|=
name|stateRef
operator|.
name|get
argument_list|()
decl_stmt|;
name|countOfBuffers
operator|=
name|toCountOfBuffers
argument_list|(
name|prevState
argument_list|)
expr_stmt|;
if|if
condition|(
name|countOfBuffers
operator|>=
name|maxToCache
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isWarnEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"At capacity: "
operator|+
name|countOfBuffers
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|countOfBuffers
operator|++
expr_stmt|;
assert|assert
literal|0
operator|<
name|countOfBuffers
operator|&&
name|countOfBuffers
operator|<=
name|maxToCache
assert|;
name|totalCapacity
operator|=
name|toTotalCapacity
argument_list|(
name|prevState
argument_list|)
operator|+
name|bb
operator|.
name|capacity
argument_list|()
expr_stmt|;
if|if
condition|(
name|totalCapacity
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isWarnEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Overflowed total capacity."
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|long
name|state
init|=
name|toState
argument_list|(
name|countOfBuffers
argument_list|,
name|totalCapacity
argument_list|)
decl_stmt|;
if|if
condition|(
name|stateRef
operator|.
name|compareAndSet
argument_list|(
name|prevState
argument_list|,
name|state
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
comment|// ConcurrentLinkQueue#offer says "this method will never return false"
name|buffers
operator|.
name|offer
argument_list|(
name|bb
argument_list|)
expr_stmt|;
name|int
name|runningAverageUpdate
init|=
name|Math
operator|.
name|min
argument_list|(
name|totalCapacity
operator|/
name|countOfBuffers
argument_list|,
comment|// size will never be 0.
name|maxByteBufferSizeToCache
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|prev
init|=
name|runningAverageRef
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|prev
operator|>=
name|runningAverageUpdate
operator|||
comment|// only rises, never recedes
name|runningAverageRef
operator|.
name|compareAndSet
argument_list|(
name|prev
argument_list|,
name|runningAverageUpdate
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit

