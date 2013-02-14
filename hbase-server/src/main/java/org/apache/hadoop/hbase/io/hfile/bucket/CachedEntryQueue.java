begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|util
operator|.
name|Comparator
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
name|Map
operator|.
name|Entry
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
name|bucket
operator|.
name|BucketCache
operator|.
name|BucketEntry
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
name|collect
operator|.
name|MinMaxPriorityQueue
import|;
end_import

begin_comment
comment|/**  * A memory-bound queue that will grow until an element brings total size larger  * than maxSize. From then on, only entries that are sorted larger than the  * smallest current entry will be inserted/replaced.  *   *<p>  * Use this when you want to find the largest elements (according to their  * ordering, not their heap size) that consume as close to the specified maxSize  * as possible. Default behavior is to grow just above rather than just below  * specified max.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CachedEntryQueue
block|{
specifier|private
name|MinMaxPriorityQueue
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
argument_list|>
name|queue
decl_stmt|;
specifier|private
name|long
name|cacheSize
decl_stmt|;
specifier|private
name|long
name|maxSize
decl_stmt|;
comment|/**    * @param maxSize the target size of elements in the queue    * @param blockSize expected average size of blocks    */
specifier|public
name|CachedEntryQueue
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
name|orderedBy
argument_list|(
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
name|entry1
parameter_list|,
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
name|entry2
parameter_list|)
block|{
return|return
name|entry1
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|entry2
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|expectedSize
argument_list|(
name|initialSize
argument_list|)
operator|.
name|create
argument_list|()
expr_stmt|;
name|cacheSize
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
comment|/**    * Attempt to add the specified entry to this queue.    *     *<p>    * If the queue is smaller than the max size, or if the specified element is    * ordered after the smallest element in the queue, the element will be added    * to the queue. Otherwise, there is no side effect of this call.    * @param entry a bucket entry with key to try to add to the queue    */
specifier|public
name|void
name|add
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
name|entry
parameter_list|)
block|{
if|if
condition|(
name|cacheSize
operator|<
name|maxSize
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|cacheSize
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|BucketEntry
name|head
init|=
name|queue
operator|.
name|peek
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|head
argument_list|)
operator|>
literal|0
condition|)
block|{
name|cacheSize
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|cacheSize
operator|-=
name|head
operator|.
name|getLength
argument_list|()
expr_stmt|;
if|if
condition|(
name|cacheSize
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
name|cacheSize
operator|+=
name|head
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
name|queue
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return The next element in this queue, or {@code null} if the queue is    *         empty.    */
specifier|public
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
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
comment|/**    * @return The last element in this queue, or {@code null} if the queue is    *         empty.    */
specifier|public
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
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
name|cacheSize
parameter_list|()
block|{
return|return
name|cacheSize
return|;
block|}
block|}
end_class

end_unit

