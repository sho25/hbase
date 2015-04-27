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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|MinMaxPriorityQueue
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

begin_comment
comment|/**  * A memory-bound queue that will grow until an element brings  * total size&gt;= maxSize.  From then on, only entries that are sorted larger  * than the smallest current entry will be inserted/replaced.  *  *<p>Use this when you want to find the largest elements (according to their  * ordering, not their heap size) that consume as close to the specified  * maxSize as possible.  Default behavior is to grow just above rather than  * just below specified max.  *  *<p>Object used in this queue must implement {@link HeapSize} as well as  * {@link Comparable}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LruCachedBlockQueue
implements|implements
name|HeapSize
block|{
specifier|private
name|MinMaxPriorityQueue
argument_list|<
name|LruCachedBlock
argument_list|>
name|queue
decl_stmt|;
specifier|private
name|long
name|heapSize
decl_stmt|;
specifier|private
name|long
name|maxSize
decl_stmt|;
comment|/**    * @param maxSize the target size of elements in the queue    * @param blockSize expected average size of blocks    */
specifier|public
name|LruCachedBlockQueue
parameter_list|(
name|long
name|maxSize
parameter_list|,
name|long
name|blockSize
parameter_list|)
block|{
name|int
name|initialSize
init|=
call|(
name|int
call|)
argument_list|(
name|maxSize
operator|/
name|blockSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|initialSize
operator|==
literal|0
condition|)
name|initialSize
operator|++
expr_stmt|;
name|queue
operator|=
name|MinMaxPriorityQueue
operator|.
name|expectedSize
argument_list|(
name|initialSize
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
name|heapSize
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|maxSize
operator|=
name|maxSize
expr_stmt|;
block|}
comment|/**    * Attempt to add the specified cached block to this queue.    *    *<p>If the queue is smaller than the max size, or if the specified element    * is ordered before the smallest element in the queue, the element will be    * added to the queue.  Otherwise, there is no side effect of this call.    * @param cb block to try to add to the queue    */
specifier|public
name|void
name|add
parameter_list|(
name|LruCachedBlock
name|cb
parameter_list|)
block|{
if|if
condition|(
name|heapSize
operator|<
name|maxSize
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|cb
argument_list|)
expr_stmt|;
name|heapSize
operator|+=
name|cb
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LruCachedBlock
name|head
init|=
name|queue
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|cb
operator|.
name|compareTo
argument_list|(
name|head
argument_list|)
operator|>
literal|0
condition|)
block|{
name|heapSize
operator|+=
name|cb
operator|.
name|heapSize
argument_list|()
expr_stmt|;
name|heapSize
operator|-=
name|head
operator|.
name|heapSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|heapSize
operator|>
name|maxSize
condition|)
block|{
name|queue
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|heapSize
operator|+=
name|head
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
name|queue
operator|.
name|add
argument_list|(
name|cb
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return The next element in this queue, or {@code null} if the queue is    * empty.    */
specifier|public
name|LruCachedBlock
name|poll
parameter_list|()
block|{
return|return
name|queue
operator|.
name|poll
argument_list|()
return|;
block|}
comment|/**    * @return The last element in this queue, or {@code null} if the queue is    * empty.    */
specifier|public
name|LruCachedBlock
name|pollLast
parameter_list|()
block|{
return|return
name|queue
operator|.
name|pollLast
argument_list|()
return|;
block|}
comment|/**    * Total size of all elements in this queue.    * @return size of all elements currently in queue, in bytes    */
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

