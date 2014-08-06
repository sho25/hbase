begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|LinkedList
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|Objects
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
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonIgnoreProperties
import|;
end_import

begin_comment
comment|/**  * This class is used to allocate a block with specified size and free the block  * when evicting. It manages an array of buckets, each bucket is associated with  * a size and caches elements up to this size. For a completely empty bucket, this  * size could be re-specified dynamically.  *   * This class is not thread safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|JsonIgnoreProperties
argument_list|(
block|{
literal|"indexStatistics"
block|,
literal|"freeSize"
block|,
literal|"usedSize"
block|}
argument_list|)
specifier|public
specifier|final
class|class
name|BucketAllocator
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
name|BucketAllocator
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|JsonIgnoreProperties
argument_list|(
block|{
literal|"completelyFree"
block|,
literal|"uninstantiated"
block|}
argument_list|)
specifier|public
specifier|final
specifier|static
class|class
name|Bucket
block|{
specifier|private
name|long
name|baseOffset
decl_stmt|;
specifier|private
name|int
name|itemAllocationSize
decl_stmt|,
name|sizeIndex
decl_stmt|;
specifier|private
name|int
name|itemCount
decl_stmt|;
specifier|private
name|int
name|freeList
index|[]
decl_stmt|;
specifier|private
name|int
name|freeCount
decl_stmt|,
name|usedCount
decl_stmt|;
specifier|public
name|Bucket
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|baseOffset
operator|=
name|offset
expr_stmt|;
name|sizeIndex
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|void
name|reconfigure
parameter_list|(
name|int
name|sizeIndex
parameter_list|,
name|int
index|[]
name|bucketSizes
parameter_list|,
name|long
name|bucketCapacity
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkElementIndex
argument_list|(
name|sizeIndex
argument_list|,
name|bucketSizes
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|sizeIndex
operator|=
name|sizeIndex
expr_stmt|;
name|itemAllocationSize
operator|=
name|bucketSizes
index|[
name|sizeIndex
index|]
expr_stmt|;
name|itemCount
operator|=
call|(
name|int
call|)
argument_list|(
name|bucketCapacity
operator|/
operator|(
name|long
operator|)
name|itemAllocationSize
argument_list|)
expr_stmt|;
name|freeCount
operator|=
name|itemCount
expr_stmt|;
name|usedCount
operator|=
literal|0
expr_stmt|;
name|freeList
operator|=
operator|new
name|int
index|[
name|itemCount
index|]
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
name|freeCount
condition|;
operator|++
name|i
control|)
name|freeList
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
specifier|public
name|boolean
name|isUninstantiated
parameter_list|()
block|{
return|return
name|sizeIndex
operator|==
operator|-
literal|1
return|;
block|}
specifier|public
name|int
name|sizeIndex
parameter_list|()
block|{
return|return
name|sizeIndex
return|;
block|}
specifier|public
name|int
name|getItemAllocationSize
parameter_list|()
block|{
return|return
name|itemAllocationSize
return|;
block|}
specifier|public
name|boolean
name|hasFreeSpace
parameter_list|()
block|{
return|return
name|freeCount
operator|>
literal|0
return|;
block|}
specifier|public
name|boolean
name|isCompletelyFree
parameter_list|()
block|{
return|return
name|usedCount
operator|==
literal|0
return|;
block|}
specifier|public
name|int
name|freeCount
parameter_list|()
block|{
return|return
name|freeCount
return|;
block|}
specifier|public
name|int
name|usedCount
parameter_list|()
block|{
return|return
name|usedCount
return|;
block|}
specifier|public
name|int
name|getFreeBytes
parameter_list|()
block|{
return|return
name|freeCount
operator|*
name|itemAllocationSize
return|;
block|}
specifier|public
name|int
name|getUsedBytes
parameter_list|()
block|{
return|return
name|usedCount
operator|*
name|itemAllocationSize
return|;
block|}
specifier|public
name|long
name|getBaseOffset
parameter_list|()
block|{
return|return
name|baseOffset
return|;
block|}
comment|/**      * Allocate a block in this bucket, return the offset representing the      * position in physical space      * @return the offset in the IOEngine      */
specifier|public
name|long
name|allocate
parameter_list|()
block|{
assert|assert
name|freeCount
operator|>
literal|0
assert|;
comment|// Else should not have been called
assert|assert
name|sizeIndex
operator|!=
operator|-
literal|1
assert|;
operator|++
name|usedCount
expr_stmt|;
name|long
name|offset
init|=
name|baseOffset
operator|+
operator|(
name|freeList
index|[
operator|--
name|freeCount
index|]
operator|*
name|itemAllocationSize
operator|)
decl_stmt|;
assert|assert
name|offset
operator|>=
literal|0
assert|;
return|return
name|offset
return|;
block|}
specifier|public
name|void
name|addAllocation
parameter_list|(
name|long
name|offset
parameter_list|)
throws|throws
name|BucketAllocatorException
block|{
name|offset
operator|-=
name|baseOffset
expr_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0
operator|||
name|offset
operator|%
name|itemAllocationSize
operator|!=
literal|0
condition|)
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Attempt to add allocation for bad offset: "
operator|+
name|offset
operator|+
literal|" base="
operator|+
name|baseOffset
operator|+
literal|", bucket size="
operator|+
name|itemAllocationSize
argument_list|)
throw|;
name|int
name|idx
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
name|itemAllocationSize
argument_list|)
decl_stmt|;
name|boolean
name|matchFound
init|=
literal|false
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
name|freeCount
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|matchFound
condition|)
name|freeList
index|[
name|i
operator|-
literal|1
index|]
operator|=
name|freeList
index|[
name|i
index|]
expr_stmt|;
elseif|else
if|if
condition|(
name|freeList
index|[
name|i
index|]
operator|==
name|idx
condition|)
name|matchFound
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|matchFound
condition|)
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Couldn't find match for index "
operator|+
name|idx
operator|+
literal|" in free list"
argument_list|)
throw|;
operator|++
name|usedCount
expr_stmt|;
operator|--
name|freeCount
expr_stmt|;
block|}
specifier|private
name|void
name|free
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|offset
operator|-=
name|baseOffset
expr_stmt|;
assert|assert
name|offset
operator|>=
literal|0
assert|;
assert|assert
name|offset
operator|<
name|itemCount
operator|*
name|itemAllocationSize
assert|;
assert|assert
name|offset
operator|%
name|itemAllocationSize
operator|==
literal|0
assert|;
assert|assert
name|usedCount
operator|>
literal|0
assert|;
assert|assert
name|freeCount
operator|<
name|itemCount
assert|;
comment|// Else duplicate free
name|int
name|item
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
operator|(
name|long
operator|)
name|itemAllocationSize
argument_list|)
decl_stmt|;
assert|assert
operator|!
name|freeListContains
argument_list|(
name|item
argument_list|)
assert|;
operator|--
name|usedCount
expr_stmt|;
name|freeList
index|[
name|freeCount
operator|++
index|]
operator|=
name|item
expr_stmt|;
block|}
specifier|private
name|boolean
name|freeListContains
parameter_list|(
name|int
name|blockNo
parameter_list|)
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
name|freeCount
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|freeList
index|[
name|i
index|]
operator|==
name|blockNo
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|final
class|class
name|BucketSizeInfo
block|{
comment|// Free bucket means it has space to allocate a block;
comment|// Completely free bucket means it has no block.
specifier|private
name|List
argument_list|<
name|Bucket
argument_list|>
name|bucketList
decl_stmt|,
name|freeBuckets
decl_stmt|,
name|completelyFreeBuckets
decl_stmt|;
specifier|private
name|int
name|sizeIndex
decl_stmt|;
name|BucketSizeInfo
parameter_list|(
name|int
name|sizeIndex
parameter_list|)
block|{
name|bucketList
operator|=
operator|new
name|LinkedList
argument_list|<
name|Bucket
argument_list|>
argument_list|()
expr_stmt|;
name|freeBuckets
operator|=
operator|new
name|LinkedList
argument_list|<
name|Bucket
argument_list|>
argument_list|()
expr_stmt|;
name|completelyFreeBuckets
operator|=
operator|new
name|LinkedList
argument_list|<
name|Bucket
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|sizeIndex
operator|=
name|sizeIndex
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|instantiateBucket
parameter_list|(
name|Bucket
name|b
parameter_list|)
block|{
assert|assert
name|b
operator|.
name|isUninstantiated
argument_list|()
operator|||
name|b
operator|.
name|isCompletelyFree
argument_list|()
assert|;
name|b
operator|.
name|reconfigure
argument_list|(
name|sizeIndex
argument_list|,
name|bucketSizes
argument_list|,
name|bucketCapacity
argument_list|)
expr_stmt|;
name|bucketList
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|freeBuckets
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|completelyFreeBuckets
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|sizeIndex
parameter_list|()
block|{
return|return
name|sizeIndex
return|;
block|}
comment|/**      * Find a bucket to allocate a block      * @return the offset in the IOEngine      */
specifier|public
name|long
name|allocateBlock
parameter_list|()
block|{
name|Bucket
name|b
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|freeBuckets
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
comment|// Use up an existing one first...
name|b
operator|=
name|freeBuckets
operator|.
name|get
argument_list|(
name|freeBuckets
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
name|b
operator|=
name|grabGlobalCompletelyFreeBucket
argument_list|()
expr_stmt|;
if|if
condition|(
name|b
operator|!=
literal|null
condition|)
name|instantiateBucket
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|b
operator|==
literal|null
condition|)
return|return
operator|-
literal|1
return|;
name|long
name|result
init|=
name|b
operator|.
name|allocate
argument_list|()
decl_stmt|;
name|blockAllocated
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
name|void
name|blockAllocated
parameter_list|(
name|Bucket
name|b
parameter_list|)
block|{
if|if
condition|(
operator|!
name|b
operator|.
name|isCompletelyFree
argument_list|()
condition|)
name|completelyFreeBuckets
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|b
operator|.
name|hasFreeSpace
argument_list|()
condition|)
name|freeBuckets
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Bucket
name|findAndRemoveCompletelyFreeBucket
parameter_list|()
block|{
name|Bucket
name|b
init|=
literal|null
decl_stmt|;
assert|assert
name|bucketList
operator|.
name|size
argument_list|()
operator|>
literal|0
assert|;
if|if
condition|(
name|bucketList
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// So we never get complete starvation of a bucket for a size
return|return
literal|null
return|;
block|}
if|if
condition|(
name|completelyFreeBuckets
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|b
operator|=
name|completelyFreeBuckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|removeBucket
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
return|return
name|b
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|removeBucket
parameter_list|(
name|Bucket
name|b
parameter_list|)
block|{
assert|assert
name|b
operator|.
name|isCompletelyFree
argument_list|()
assert|;
name|bucketList
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|freeBuckets
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|completelyFreeBuckets
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|freeBlock
parameter_list|(
name|Bucket
name|b
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
assert|assert
name|bucketList
operator|.
name|contains
argument_list|(
name|b
argument_list|)
assert|;
comment|// else we shouldn't have anything to free...
assert|assert
operator|(
operator|!
name|completelyFreeBuckets
operator|.
name|contains
argument_list|(
name|b
argument_list|)
operator|)
assert|;
name|b
operator|.
name|free
argument_list|(
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|freeBuckets
operator|.
name|contains
argument_list|(
name|b
argument_list|)
condition|)
name|freeBuckets
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
name|b
operator|.
name|isCompletelyFree
argument_list|()
condition|)
name|completelyFreeBuckets
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|IndexStatistics
name|statistics
parameter_list|()
block|{
name|long
name|free
init|=
literal|0
decl_stmt|,
name|used
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Bucket
name|b
range|:
name|bucketList
control|)
block|{
name|free
operator|+=
name|b
operator|.
name|freeCount
argument_list|()
expr_stmt|;
name|used
operator|+=
name|b
operator|.
name|usedCount
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|IndexStatistics
argument_list|(
name|free
argument_list|,
name|used
argument_list|,
name|bucketSizes
index|[
name|sizeIndex
index|]
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
name|Objects
operator|.
name|toStringHelper
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"sizeIndex"
argument_list|,
name|sizeIndex
argument_list|)
operator|.
name|add
argument_list|(
literal|"bucketSize"
argument_list|,
name|bucketSizes
index|[
name|sizeIndex
index|]
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|// Default block size is 64K, so we choose more sizes near 64K, you'd better
comment|// reset it according to your cluster's block size distribution
comment|// TODO Support the view of block size distribution statistics
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_BUCKET_SIZES
index|[]
init|=
block|{
literal|4
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|8
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|16
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|32
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|40
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|48
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|56
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|64
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|96
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|128
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|192
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|256
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|384
operator|*
literal|1024
operator|+
literal|1024
block|,
literal|512
operator|*
literal|1024
operator|+
literal|1024
block|}
decl_stmt|;
comment|/**    * Round up the given block size to bucket size, and get the corresponding    * BucketSizeInfo    */
specifier|public
name|BucketSizeInfo
name|roundUpToBucketSizeInfo
parameter_list|(
name|int
name|blockSize
parameter_list|)
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
name|bucketSizes
operator|.
name|length
condition|;
operator|++
name|i
control|)
if|if
condition|(
name|blockSize
operator|<=
name|bucketSizes
index|[
name|i
index|]
condition|)
return|return
name|bucketSizeInfos
index|[
name|i
index|]
return|;
return|return
literal|null
return|;
block|}
specifier|static
specifier|public
specifier|final
name|int
name|FEWEST_ITEMS_IN_BUCKET
init|=
literal|4
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|bucketSizes
decl_stmt|;
specifier|private
specifier|final
name|int
name|bigItemSize
decl_stmt|;
comment|// The capacity size for each bucket
specifier|private
specifier|final
name|long
name|bucketCapacity
decl_stmt|;
specifier|private
name|Bucket
index|[]
name|buckets
decl_stmt|;
specifier|private
name|BucketSizeInfo
index|[]
name|bucketSizeInfos
decl_stmt|;
specifier|private
specifier|final
name|long
name|totalSize
decl_stmt|;
specifier|private
name|long
name|usedSize
init|=
literal|0
decl_stmt|;
name|BucketAllocator
parameter_list|(
name|long
name|availableSpace
parameter_list|,
name|int
index|[]
name|bucketSizes
parameter_list|)
throws|throws
name|BucketAllocatorException
block|{
name|this
operator|.
name|bucketSizes
operator|=
name|bucketSizes
operator|==
literal|null
condition|?
name|DEFAULT_BUCKET_SIZES
else|:
name|bucketSizes
expr_stmt|;
name|int
name|largestBucket
init|=
name|this
operator|.
name|bucketSizes
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
range|:
name|this
operator|.
name|bucketSizes
control|)
block|{
name|largestBucket
operator|=
name|Math
operator|.
name|max
argument_list|(
name|largestBucket
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|bigItemSize
operator|=
name|largestBucket
expr_stmt|;
name|this
operator|.
name|bucketCapacity
operator|=
name|FEWEST_ITEMS_IN_BUCKET
operator|*
name|bigItemSize
expr_stmt|;
name|buckets
operator|=
operator|new
name|Bucket
index|[
call|(
name|int
call|)
argument_list|(
name|availableSpace
operator|/
name|bucketCapacity
argument_list|)
index|]
expr_stmt|;
if|if
condition|(
name|buckets
operator|.
name|length
operator|<
name|this
operator|.
name|bucketSizes
operator|.
name|length
condition|)
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Bucket allocator size too small - must have room for at least "
operator|+
name|this
operator|.
name|bucketSizes
operator|.
name|length
operator|+
literal|" buckets"
argument_list|)
throw|;
name|bucketSizeInfos
operator|=
operator|new
name|BucketSizeInfo
index|[
name|this
operator|.
name|bucketSizes
operator|.
name|length
index|]
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
name|this
operator|.
name|bucketSizes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|bucketSizeInfos
index|[
name|i
index|]
operator|=
operator|new
name|BucketSizeInfo
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|buckets
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|buckets
index|[
name|i
index|]
operator|=
operator|new
name|Bucket
argument_list|(
name|bucketCapacity
operator|*
name|i
argument_list|)
expr_stmt|;
name|bucketSizeInfos
index|[
name|i
operator|<
name|this
operator|.
name|bucketSizes
operator|.
name|length
condition|?
name|i
else|:
name|this
operator|.
name|bucketSizes
operator|.
name|length
operator|-
literal|1
index|]
operator|.
name|instantiateBucket
argument_list|(
name|buckets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|totalSize
operator|=
operator|(
operator|(
name|long
operator|)
name|buckets
operator|.
name|length
operator|)
operator|*
name|bucketCapacity
expr_stmt|;
block|}
comment|/**    * Rebuild the allocator's data structures from a persisted map.    * @param availableSpace capacity of cache    * @param map A map stores the block key and BucketEntry(block's meta data    *          like offset, length)    * @param realCacheSize cached data size statistics for bucket cache    * @throws BucketAllocatorException    */
name|BucketAllocator
parameter_list|(
name|long
name|availableSpace
parameter_list|,
name|int
index|[]
name|bucketSizes
parameter_list|,
name|Map
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
name|map
parameter_list|,
name|AtomicLong
name|realCacheSize
parameter_list|)
throws|throws
name|BucketAllocatorException
block|{
name|this
argument_list|(
name|availableSpace
argument_list|,
name|bucketSizes
argument_list|)
expr_stmt|;
comment|// each bucket has an offset, sizeindex. probably the buckets are too big
comment|// in our default state. so what we do is reconfigure them according to what
comment|// we've found. we can only reconfigure each bucket once; if more than once,
comment|// we know there's a bug, so we just log the info, throw, and start again...
name|boolean
index|[]
name|reconfigured
init|=
operator|new
name|boolean
index|[
name|buckets
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketEntry
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|long
name|foundOffset
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|offset
argument_list|()
decl_stmt|;
name|int
name|foundLen
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|int
name|bucketSizeIndex
init|=
operator|-
literal|1
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
name|bucketSizes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|foundLen
operator|<=
name|bucketSizes
index|[
name|i
index|]
condition|)
block|{
name|bucketSizeIndex
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|bucketSizeIndex
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Can't match bucket size for the block with size "
operator|+
name|foundLen
argument_list|)
throw|;
block|}
name|int
name|bucketNo
init|=
call|(
name|int
call|)
argument_list|(
name|foundOffset
operator|/
name|bucketCapacity
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketNo
operator|<
literal|0
operator|||
name|bucketNo
operator|>=
name|buckets
operator|.
name|length
condition|)
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Can't find bucket "
operator|+
name|bucketNo
operator|+
literal|", total buckets="
operator|+
name|buckets
operator|.
name|length
operator|+
literal|"; did you shrink the cache?"
argument_list|)
throw|;
name|Bucket
name|b
init|=
name|buckets
index|[
name|bucketNo
index|]
decl_stmt|;
if|if
condition|(
name|reconfigured
index|[
name|bucketNo
index|]
condition|)
block|{
if|if
condition|(
name|b
operator|.
name|sizeIndex
argument_list|()
operator|!=
name|bucketSizeIndex
condition|)
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Inconsistent allocation in bucket map;"
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|b
operator|.
name|isCompletelyFree
argument_list|()
condition|)
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Reconfiguring bucket "
operator|+
name|bucketNo
operator|+
literal|" but it's already allocated; corrupt data"
argument_list|)
throw|;
comment|// Need to remove the bucket from whichever list it's currently in at
comment|// the moment...
name|BucketSizeInfo
name|bsi
init|=
name|bucketSizeInfos
index|[
name|bucketSizeIndex
index|]
decl_stmt|;
name|BucketSizeInfo
name|oldbsi
init|=
name|bucketSizeInfos
index|[
name|b
operator|.
name|sizeIndex
argument_list|()
index|]
decl_stmt|;
name|oldbsi
operator|.
name|removeBucket
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|bsi
operator|.
name|instantiateBucket
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|reconfigured
index|[
name|bucketNo
index|]
operator|=
literal|true
expr_stmt|;
block|}
name|realCacheSize
operator|.
name|addAndGet
argument_list|(
name|foundLen
argument_list|)
expr_stmt|;
name|buckets
index|[
name|bucketNo
index|]
operator|.
name|addAllocation
argument_list|(
name|foundOffset
argument_list|)
expr_stmt|;
name|usedSize
operator|+=
name|buckets
index|[
name|bucketNo
index|]
operator|.
name|getItemAllocationSize
argument_list|()
expr_stmt|;
name|bucketSizeInfos
index|[
name|bucketSizeIndex
index|]
operator|.
name|blockAllocated
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|1024
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
name|buckets
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Bucket
name|b
init|=
name|buckets
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"bucket."
argument_list|)
operator|.
name|append
argument_list|(
name|i
argument_list|)
operator|.
name|append
argument_list|(
literal|": size="
argument_list|)
operator|.
name|append
argument_list|(
name|b
operator|.
name|getItemAllocationSize
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", freeCount="
argument_list|)
operator|.
name|append
argument_list|(
name|b
operator|.
name|freeCount
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", used="
argument_list|)
operator|.
name|append
argument_list|(
name|b
operator|.
name|usedCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|long
name|getUsedSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|usedSize
return|;
block|}
specifier|public
name|long
name|getFreeSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalSize
operator|-
name|getUsedSize
argument_list|()
return|;
block|}
specifier|public
name|long
name|getTotalSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalSize
return|;
block|}
comment|/**    * Allocate a block with specified size. Return the offset    * @param blockSize size of block    * @throws BucketAllocatorException,CacheFullException    * @return the offset in the IOEngine    */
specifier|public
specifier|synchronized
name|long
name|allocateBlock
parameter_list|(
name|int
name|blockSize
parameter_list|)
throws|throws
name|CacheFullException
throws|,
name|BucketAllocatorException
block|{
assert|assert
name|blockSize
operator|>
literal|0
assert|;
name|BucketSizeInfo
name|bsi
init|=
name|roundUpToBucketSizeInfo
argument_list|(
name|blockSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|bsi
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BucketAllocatorException
argument_list|(
literal|"Allocation too big size="
operator|+
name|blockSize
argument_list|)
throw|;
block|}
name|long
name|offset
init|=
name|bsi
operator|.
name|allocateBlock
argument_list|()
decl_stmt|;
comment|// Ask caller to free up space and try again!
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
throw|throw
operator|new
name|CacheFullException
argument_list|(
name|blockSize
argument_list|,
name|bsi
operator|.
name|sizeIndex
argument_list|()
argument_list|)
throw|;
name|usedSize
operator|+=
name|bucketSizes
index|[
name|bsi
operator|.
name|sizeIndex
argument_list|()
index|]
expr_stmt|;
return|return
name|offset
return|;
block|}
specifier|private
name|Bucket
name|grabGlobalCompletelyFreeBucket
parameter_list|()
block|{
for|for
control|(
name|BucketSizeInfo
name|bsi
range|:
name|bucketSizeInfos
control|)
block|{
name|Bucket
name|b
init|=
name|bsi
operator|.
name|findAndRemoveCompletelyFreeBucket
argument_list|()
decl_stmt|;
if|if
condition|(
name|b
operator|!=
literal|null
condition|)
return|return
name|b
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Free a block with the offset    * @param offset block's offset    * @return size freed    */
specifier|public
specifier|synchronized
name|int
name|freeBlock
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|int
name|bucketNo
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
name|bucketCapacity
argument_list|)
decl_stmt|;
assert|assert
name|bucketNo
operator|>=
literal|0
operator|&&
name|bucketNo
operator|<
name|buckets
operator|.
name|length
assert|;
name|Bucket
name|targetBucket
init|=
name|buckets
index|[
name|bucketNo
index|]
decl_stmt|;
name|bucketSizeInfos
index|[
name|targetBucket
operator|.
name|sizeIndex
argument_list|()
index|]
operator|.
name|freeBlock
argument_list|(
name|targetBucket
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|usedSize
operator|-=
name|targetBucket
operator|.
name|getItemAllocationSize
argument_list|()
expr_stmt|;
return|return
name|targetBucket
operator|.
name|getItemAllocationSize
argument_list|()
return|;
block|}
specifier|public
name|int
name|sizeIndexOfAllocation
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|int
name|bucketNo
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
name|bucketCapacity
argument_list|)
decl_stmt|;
assert|assert
name|bucketNo
operator|>=
literal|0
operator|&&
name|bucketNo
operator|<
name|buckets
operator|.
name|length
assert|;
name|Bucket
name|targetBucket
init|=
name|buckets
index|[
name|bucketNo
index|]
decl_stmt|;
return|return
name|targetBucket
operator|.
name|sizeIndex
argument_list|()
return|;
block|}
specifier|public
name|int
name|sizeOfAllocation
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|int
name|bucketNo
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
name|bucketCapacity
argument_list|)
decl_stmt|;
assert|assert
name|bucketNo
operator|>=
literal|0
operator|&&
name|bucketNo
operator|<
name|buckets
operator|.
name|length
assert|;
name|Bucket
name|targetBucket
init|=
name|buckets
index|[
name|bucketNo
index|]
decl_stmt|;
return|return
name|targetBucket
operator|.
name|getItemAllocationSize
argument_list|()
return|;
block|}
specifier|static
class|class
name|IndexStatistics
block|{
specifier|private
name|long
name|freeCount
decl_stmt|,
name|usedCount
decl_stmt|,
name|itemSize
decl_stmt|,
name|totalCount
decl_stmt|;
specifier|public
name|long
name|freeCount
parameter_list|()
block|{
return|return
name|freeCount
return|;
block|}
specifier|public
name|long
name|usedCount
parameter_list|()
block|{
return|return
name|usedCount
return|;
block|}
specifier|public
name|long
name|totalCount
parameter_list|()
block|{
return|return
name|totalCount
return|;
block|}
specifier|public
name|long
name|freeBytes
parameter_list|()
block|{
return|return
name|freeCount
operator|*
name|itemSize
return|;
block|}
specifier|public
name|long
name|usedBytes
parameter_list|()
block|{
return|return
name|usedCount
operator|*
name|itemSize
return|;
block|}
specifier|public
name|long
name|totalBytes
parameter_list|()
block|{
return|return
name|totalCount
operator|*
name|itemSize
return|;
block|}
specifier|public
name|long
name|itemSize
parameter_list|()
block|{
return|return
name|itemSize
return|;
block|}
specifier|public
name|IndexStatistics
parameter_list|(
name|long
name|free
parameter_list|,
name|long
name|used
parameter_list|,
name|long
name|itemSize
parameter_list|)
block|{
name|setTo
argument_list|(
name|free
argument_list|,
name|used
argument_list|,
name|itemSize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IndexStatistics
parameter_list|()
block|{
name|setTo
argument_list|(
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setTo
parameter_list|(
name|long
name|free
parameter_list|,
name|long
name|used
parameter_list|,
name|long
name|itemSize
parameter_list|)
block|{
name|this
operator|.
name|itemSize
operator|=
name|itemSize
expr_stmt|;
name|this
operator|.
name|freeCount
operator|=
name|free
expr_stmt|;
name|this
operator|.
name|usedCount
operator|=
name|used
expr_stmt|;
name|this
operator|.
name|totalCount
operator|=
name|free
operator|+
name|used
expr_stmt|;
block|}
block|}
specifier|public
name|Bucket
index|[]
name|getBuckets
parameter_list|()
block|{
return|return
name|this
operator|.
name|buckets
return|;
block|}
name|void
name|logStatistics
parameter_list|()
block|{
name|IndexStatistics
name|total
init|=
operator|new
name|IndexStatistics
argument_list|()
decl_stmt|;
name|IndexStatistics
index|[]
name|stats
init|=
name|getIndexStatistics
argument_list|(
name|total
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Bucket allocator statistics follow:\n"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"  Free bytes="
operator|+
name|total
operator|.
name|freeBytes
argument_list|()
operator|+
literal|"+; used bytes="
operator|+
name|total
operator|.
name|usedBytes
argument_list|()
operator|+
literal|"; total bytes="
operator|+
name|total
operator|.
name|totalBytes
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexStatistics
name|s
range|:
name|stats
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"  Object size "
operator|+
name|s
operator|.
name|itemSize
argument_list|()
operator|+
literal|" used="
operator|+
name|s
operator|.
name|usedCount
argument_list|()
operator|+
literal|"; free="
operator|+
name|s
operator|.
name|freeCount
argument_list|()
operator|+
literal|"; total="
operator|+
name|s
operator|.
name|totalCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|IndexStatistics
index|[]
name|getIndexStatistics
parameter_list|(
name|IndexStatistics
name|grandTotal
parameter_list|)
block|{
name|IndexStatistics
index|[]
name|stats
init|=
name|getIndexStatistics
argument_list|()
decl_stmt|;
name|long
name|totalfree
init|=
literal|0
decl_stmt|,
name|totalused
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IndexStatistics
name|stat
range|:
name|stats
control|)
block|{
name|totalfree
operator|+=
name|stat
operator|.
name|freeBytes
argument_list|()
expr_stmt|;
name|totalused
operator|+=
name|stat
operator|.
name|usedBytes
argument_list|()
expr_stmt|;
block|}
name|grandTotal
operator|.
name|setTo
argument_list|(
name|totalfree
argument_list|,
name|totalused
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
name|IndexStatistics
index|[]
name|getIndexStatistics
parameter_list|()
block|{
name|IndexStatistics
index|[]
name|stats
init|=
operator|new
name|IndexStatistics
index|[
name|bucketSizes
operator|.
name|length
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
name|stats
operator|.
name|length
condition|;
operator|++
name|i
control|)
name|stats
index|[
name|i
index|]
operator|=
name|bucketSizeInfos
index|[
name|i
index|]
operator|.
name|statistics
argument_list|()
expr_stmt|;
return|return
name|stats
return|;
block|}
specifier|public
name|long
name|freeBlock
parameter_list|(
name|long
name|freeList
index|[]
parameter_list|)
block|{
name|long
name|sz
init|=
literal|0
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
name|freeList
operator|.
name|length
condition|;
operator|++
name|i
control|)
name|sz
operator|+=
name|freeBlock
argument_list|(
name|freeList
index|[
name|i
index|]
argument_list|)
expr_stmt|;
return|return
name|sz
return|;
block|}
block|}
end_class

end_unit

