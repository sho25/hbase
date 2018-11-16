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
name|procedure2
operator|.
name|store
package|;
end_package

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
name|Arrays
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|store
operator|.
name|ProcedureStoreTracker
operator|.
name|DeleteState
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * A bitmap which can grow/merge with other {@link BitSetNode} (if certain conditions are met).  * Boundaries of bitmap are aligned to multiples of {@link BitSetNode#BITS_PER_WORD}. So the range  * of a {@link BitSetNode} is from [x * K, y * K) where x and y are integers, y> x and K is  * BITS_PER_WORD.  *<p/>  * We have two main bit sets to describe the state of procedures, the meanings are:  *  *<pre>  *  ----------------------  * | modified | deleted |  meaning  * |     0    |   0     |  proc exists, but hasn't been updated since last resetUpdates().  * |     1    |   0     |  proc was updated (but not deleted).  * |     1    |   1     |  proc was deleted.  * |     0    |   1     |  proc doesn't exist (maybe never created, maybe deleted in past).  * ----------------------  *</pre>  *  * The meaning of modified is that, we have modified the state of the procedure, no matter insert,  * update, or delete. And if it is an insert or update, we will set the deleted to 0, if not we will  * set the delete to 1.  *<p/>  * For a non-partial BitSetNode, the initial modified value is 0 and deleted value is 1. For the  * partial one, the initial modified value is 0 and the initial deleted value is also 0. In  * {@link #unsetPartialFlag()} we will reset the deleted to 1 if it is not modified.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|BitSetNode
block|{
specifier|private
specifier|static
specifier|final
name|long
name|WORD_MASK
init|=
literal|0xffffffffffffffffL
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ADDRESS_BITS_PER_WORD
init|=
literal|6
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BITS_PER_WORD
init|=
literal|1
operator|<<
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
comment|// The BitSetNode itself has 48 bytes overhead, which is the size of 6 longs, so here we use a max
comment|// node size 4, which is 8 longs since we have an array for modified and also an array for
comment|// deleted. The assumption here is that most procedures will be deleted soon so we'd better keep
comment|// the BitSetNode small.
specifier|private
specifier|static
specifier|final
name|int
name|MAX_NODE_SIZE
init|=
literal|4
operator|<<
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
comment|/**    * Mimics {@link ProcedureStoreTracker#partial}. It will effect how we fill the new deleted bits    * when growing.    */
specifier|private
name|boolean
name|partial
decl_stmt|;
comment|/**    * Set of procedures which have been modified since last {@link #resetModified()}. Useful to track    * procedures which have been modified since last WAL write.    */
specifier|private
name|long
index|[]
name|modified
decl_stmt|;
comment|/**    * Keeps track of procedure ids which belong to this bitmap's range and have been deleted. This    * represents global state since it's not reset on WAL rolls.    */
specifier|private
name|long
index|[]
name|deleted
decl_stmt|;
comment|/**    * Offset of bitmap i.e. procedure id corresponding to first bit.    */
specifier|private
name|long
name|start
decl_stmt|;
specifier|public
name|void
name|dump
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%06d:%06d min=%d max=%d%n"
argument_list|,
name|getStart
argument_list|()
argument_list|,
name|getEnd
argument_list|()
argument_list|,
name|getActiveMinProcId
argument_list|()
argument_list|,
name|getActiveMaxProcId
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Modified:"
argument_list|)
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
name|modified
operator|.
name|length
condition|;
operator|++
name|i
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
name|BITS_PER_WORD
condition|;
operator|++
name|j
control|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
operator|(
name|modified
index|[
name|i
index|]
operator|&
operator|(
literal|1L
operator|<<
name|j
operator|)
operator|)
operator|!=
literal|0
condition|?
literal|"1"
else|:
literal|"0"
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Delete:"
argument_list|)
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
name|deleted
operator|.
name|length
condition|;
operator|++
name|i
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
name|BITS_PER_WORD
condition|;
operator|++
name|j
control|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
operator|(
name|deleted
index|[
name|i
index|]
operator|&
operator|(
literal|1L
operator|<<
name|j
operator|)
operator|)
operator|!=
literal|0
condition|?
literal|"1"
else|:
literal|"0"
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
specifier|public
name|BitSetNode
parameter_list|(
name|long
name|procId
parameter_list|,
name|boolean
name|partial
parameter_list|)
block|{
name|start
operator|=
name|alignDown
argument_list|(
name|procId
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|1
decl_stmt|;
name|modified
operator|=
operator|new
name|long
index|[
name|count
index|]
expr_stmt|;
name|deleted
operator|=
operator|new
name|long
index|[
name|count
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|partial
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|deleted
argument_list|,
name|WORD_MASK
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|partial
operator|=
name|partial
expr_stmt|;
name|updateState
argument_list|(
name|procId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BitSetNode
parameter_list|(
name|ProcedureProtos
operator|.
name|ProcedureStoreTracker
operator|.
name|TrackerNode
name|data
parameter_list|)
block|{
name|start
operator|=
name|data
operator|.
name|getStartId
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|data
operator|.
name|getUpdatedCount
argument_list|()
decl_stmt|;
assert|assert
name|size
operator|==
name|data
operator|.
name|getDeletedCount
argument_list|()
assert|;
name|modified
operator|=
operator|new
name|long
index|[
name|size
index|]
expr_stmt|;
name|deleted
operator|=
operator|new
name|long
index|[
name|size
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|modified
index|[
name|i
index|]
operator|=
name|data
operator|.
name|getUpdated
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|deleted
index|[
name|i
index|]
operator|=
name|data
operator|.
name|getDeleted
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|partial
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|BitSetNode
parameter_list|(
name|BitSetNode
name|other
parameter_list|,
name|boolean
name|resetDelete
parameter_list|)
block|{
name|this
operator|.
name|start
operator|=
name|other
operator|.
name|start
expr_stmt|;
comment|// The resetDelete will be set to true when building cleanup tracker.
comment|// as we will reset deleted flags for all the unmodified bits to 1, the partial flag is useless
comment|// so set it to false for not confusing the developers when debugging.
name|this
operator|.
name|partial
operator|=
name|resetDelete
condition|?
literal|false
else|:
name|other
operator|.
name|partial
expr_stmt|;
name|this
operator|.
name|modified
operator|=
name|other
operator|.
name|modified
operator|.
name|clone
argument_list|()
expr_stmt|;
comment|// The intention here is that, if a procedure is not modified in this tracker, then we do not
comment|// need to take care of it, so we will set deleted to true for these bits, i.e, if modified is
comment|// 0, then we set deleted to 1, otherwise keep it as is. So here, the equation is
comment|// deleted |= ~modified, i.e,
if|if
condition|(
name|resetDelete
condition|)
block|{
name|this
operator|.
name|deleted
operator|=
operator|new
name|long
index|[
name|other
operator|.
name|deleted
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
name|deleted
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|this
operator|.
name|deleted
index|[
name|i
index|]
operator||=
operator|~
operator|(
name|other
operator|.
name|modified
index|[
name|i
index|]
operator|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|deleted
operator|=
name|other
operator|.
name|deleted
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|insertOrUpdate
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
name|updateState
argument_list|(
name|procId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
name|updateState
argument_list|(
name|procId
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getStart
parameter_list|()
block|{
return|return
name|start
return|;
block|}
comment|/**    * Inclusive.    */
specifier|public
name|long
name|getEnd
parameter_list|()
block|{
return|return
name|start
operator|+
operator|(
name|modified
operator|.
name|length
operator|<<
name|ADDRESS_BITS_PER_WORD
operator|)
operator|-
literal|1
return|;
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
return|return
name|start
operator|<=
name|procId
operator|&&
name|procId
operator|<=
name|getEnd
argument_list|()
return|;
block|}
specifier|public
name|DeleteState
name|isDeleted
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
name|int
name|bitmapIndex
init|=
name|getBitmapIndex
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|int
name|wordIndex
init|=
name|bitmapIndex
operator|>>
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
if|if
condition|(
name|wordIndex
operator|>=
name|deleted
operator|.
name|length
condition|)
block|{
return|return
name|DeleteState
operator|.
name|MAYBE
return|;
block|}
comment|// The left shift of java only takes care of the lowest several bits(5 for int and 6 for long),
comment|// so here we can use bitmapIndex directly, without mod 64
return|return
operator|(
name|deleted
index|[
name|wordIndex
index|]
operator|&
operator|(
literal|1L
operator|<<
name|bitmapIndex
operator|)
operator|)
operator|!=
literal|0
condition|?
name|DeleteState
operator|.
name|YES
else|:
name|DeleteState
operator|.
name|NO
return|;
block|}
specifier|public
name|boolean
name|isModified
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
name|int
name|bitmapIndex
init|=
name|getBitmapIndex
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|int
name|wordIndex
init|=
name|bitmapIndex
operator|>>
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
if|if
condition|(
name|wordIndex
operator|>=
name|modified
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// The left shift of java only takes care of the lowest several bits(5 for int and 6 for long),
comment|// so here we can use bitmapIndex directly, without mod 64
return|return
operator|(
name|modified
index|[
name|wordIndex
index|]
operator|&
operator|(
literal|1L
operator|<<
name|bitmapIndex
operator|)
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**    * @return true, if all the procedures has been modified.    */
specifier|public
name|boolean
name|isAllModified
parameter_list|()
block|{
comment|// TODO: cache the value
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|modified
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|(
name|modified
index|[
name|i
index|]
operator||
name|deleted
index|[
name|i
index|]
operator|)
operator|!=
name|WORD_MASK
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * @return all the active procedure ids in this bit set.    */
specifier|public
name|long
index|[]
name|getActiveProcIds
parameter_list|()
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|procIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|wordIndex
init|=
literal|0
init|;
name|wordIndex
operator|<
name|modified
operator|.
name|length
condition|;
name|wordIndex
operator|++
control|)
block|{
if|if
condition|(
name|deleted
index|[
name|wordIndex
index|]
operator|==
name|WORD_MASK
operator|||
name|modified
index|[
name|wordIndex
index|]
operator|==
literal|0
condition|)
block|{
comment|// This should be the common case, where most procedures has been deleted.
continue|continue;
block|}
name|long
name|baseProcId
init|=
name|getStart
argument_list|()
operator|+
operator|(
name|wordIndex
operator|<<
name|ADDRESS_BITS_PER_WORD
operator|)
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
operator|(
literal|1
operator|<<
name|ADDRESS_BITS_PER_WORD
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|long
name|mask
init|=
literal|1L
operator|<<
name|i
decl_stmt|;
if|if
condition|(
operator|(
name|deleted
index|[
name|wordIndex
index|]
operator|&
name|mask
operator|)
operator|==
literal|0
operator|&&
operator|(
name|modified
index|[
name|wordIndex
index|]
operator|&
name|mask
operator|)
operator|!=
literal|0
condition|)
block|{
name|procIds
operator|.
name|add
argument_list|(
name|baseProcId
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|procIds
operator|.
name|stream
argument_list|()
operator|.
name|mapToLong
argument_list|(
name|Long
operator|::
name|longValue
argument_list|)
operator|.
name|toArray
argument_list|()
return|;
block|}
comment|/**    * @return true, if there are no active procedures in this BitSetNode, else false.    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
comment|// TODO: cache the value
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|deleted
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|deleted
index|[
name|i
index|]
operator|!=
name|WORD_MASK
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|resetModified
parameter_list|()
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|modified
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|unsetPartialFlag
parameter_list|()
block|{
name|partial
operator|=
literal|false
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
name|modified
operator|.
name|length
condition|;
operator|++
name|i
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
name|BITS_PER_WORD
condition|;
operator|++
name|j
control|)
block|{
if|if
condition|(
operator|(
name|modified
index|[
name|i
index|]
operator|&
operator|(
literal|1L
operator|<<
name|j
operator|)
operator|)
operator|==
literal|0
condition|)
block|{
name|deleted
index|[
name|i
index|]
operator||=
operator|(
literal|1L
operator|<<
name|j
operator|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Convert to    * org.apache.hadoop.hbase.protobuf.generated.ProcedureProtos.ProcedureStoreTracker.TrackerNode    * protobuf.    */
specifier|public
name|ProcedureProtos
operator|.
name|ProcedureStoreTracker
operator|.
name|TrackerNode
name|convert
parameter_list|()
block|{
name|ProcedureProtos
operator|.
name|ProcedureStoreTracker
operator|.
name|TrackerNode
operator|.
name|Builder
name|builder
init|=
name|ProcedureProtos
operator|.
name|ProcedureStoreTracker
operator|.
name|TrackerNode
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setStartId
argument_list|(
name|start
argument_list|)
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
name|modified
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|builder
operator|.
name|addUpdated
argument_list|(
name|modified
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addDeleted
argument_list|(
name|deleted
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|// ========================================================================
comment|// Grow/Merge Helpers
comment|// ========================================================================
specifier|public
name|boolean
name|canGrow
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
if|if
condition|(
name|procId
operator|<=
name|start
condition|)
block|{
return|return
name|getEnd
argument_list|()
operator|-
name|procId
operator|<
name|MAX_NODE_SIZE
return|;
block|}
else|else
block|{
return|return
name|procId
operator|-
name|start
operator|<
name|MAX_NODE_SIZE
return|;
block|}
block|}
specifier|public
name|boolean
name|canMerge
parameter_list|(
name|BitSetNode
name|rightNode
parameter_list|)
block|{
comment|// Can just compare 'starts' since boundaries are aligned to multiples of BITS_PER_WORD.
assert|assert
name|start
operator|<
name|rightNode
operator|.
name|start
assert|;
return|return
operator|(
name|rightNode
operator|.
name|getEnd
argument_list|()
operator|-
name|start
operator|)
operator|<
name|MAX_NODE_SIZE
return|;
block|}
specifier|public
name|void
name|grow
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
comment|// make sure you have call canGrow first before calling this method
assert|assert
name|canGrow
argument_list|(
name|procId
argument_list|)
assert|;
if|if
condition|(
name|procId
operator|<
name|start
condition|)
block|{
comment|// grow to left
name|long
name|newStart
init|=
name|alignDown
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|int
name|delta
init|=
call|(
name|int
call|)
argument_list|(
name|start
operator|-
name|newStart
argument_list|)
operator|>>
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
name|start
operator|=
name|newStart
expr_stmt|;
name|long
index|[]
name|newModified
init|=
operator|new
name|long
index|[
name|modified
operator|.
name|length
operator|+
name|delta
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|modified
argument_list|,
literal|0
argument_list|,
name|newModified
argument_list|,
name|delta
argument_list|,
name|modified
operator|.
name|length
argument_list|)
expr_stmt|;
name|modified
operator|=
name|newModified
expr_stmt|;
name|long
index|[]
name|newDeleted
init|=
operator|new
name|long
index|[
name|deleted
operator|.
name|length
operator|+
name|delta
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|partial
condition|)
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
name|delta
condition|;
name|i
operator|++
control|)
block|{
name|newDeleted
index|[
name|i
index|]
operator|=
name|WORD_MASK
expr_stmt|;
block|}
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|deleted
argument_list|,
literal|0
argument_list|,
name|newDeleted
argument_list|,
name|delta
argument_list|,
name|deleted
operator|.
name|length
argument_list|)
expr_stmt|;
name|deleted
operator|=
name|newDeleted
expr_stmt|;
block|}
else|else
block|{
comment|// grow to right
name|long
name|newEnd
init|=
name|alignUp
argument_list|(
name|procId
operator|+
literal|1
argument_list|)
decl_stmt|;
name|int
name|delta
init|=
call|(
name|int
call|)
argument_list|(
name|newEnd
operator|-
name|getEnd
argument_list|()
argument_list|)
operator|>>
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
name|int
name|newSize
init|=
name|modified
operator|.
name|length
operator|+
name|delta
decl_stmt|;
name|long
index|[]
name|newModified
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|modified
argument_list|,
name|newSize
argument_list|)
decl_stmt|;
name|modified
operator|=
name|newModified
expr_stmt|;
name|long
index|[]
name|newDeleted
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|deleted
argument_list|,
name|newSize
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|partial
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|deleted
operator|.
name|length
init|;
name|i
operator|<
name|newSize
condition|;
name|i
operator|++
control|)
block|{
name|newDeleted
index|[
name|i
index|]
operator|=
name|WORD_MASK
expr_stmt|;
block|}
block|}
name|deleted
operator|=
name|newDeleted
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|merge
parameter_list|(
name|BitSetNode
name|rightNode
parameter_list|)
block|{
assert|assert
name|start
operator|<
name|rightNode
operator|.
name|start
assert|;
name|int
name|newSize
init|=
call|(
name|int
call|)
argument_list|(
name|rightNode
operator|.
name|getEnd
argument_list|()
operator|-
name|start
operator|+
literal|1
argument_list|)
operator|>>
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
name|long
index|[]
name|newModified
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|modified
argument_list|,
name|newSize
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|rightNode
operator|.
name|modified
argument_list|,
literal|0
argument_list|,
name|newModified
argument_list|,
name|newSize
operator|-
name|rightNode
operator|.
name|modified
operator|.
name|length
argument_list|,
name|rightNode
operator|.
name|modified
operator|.
name|length
argument_list|)
expr_stmt|;
name|long
index|[]
name|newDeleted
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|deleted
argument_list|,
name|newSize
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|rightNode
operator|.
name|deleted
argument_list|,
literal|0
argument_list|,
name|newDeleted
argument_list|,
name|newSize
operator|-
name|rightNode
operator|.
name|deleted
operator|.
name|length
argument_list|,
name|rightNode
operator|.
name|deleted
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|partial
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|deleted
operator|.
name|length
init|,
name|n
init|=
name|newSize
operator|-
name|rightNode
operator|.
name|deleted
operator|.
name|length
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|newDeleted
index|[
name|i
index|]
operator|=
name|WORD_MASK
expr_stmt|;
block|}
block|}
name|modified
operator|=
name|newModified
expr_stmt|;
name|deleted
operator|=
name|newDeleted
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"BitSetNode("
operator|+
name|getStart
argument_list|()
operator|+
literal|"-"
operator|+
name|getEnd
argument_list|()
operator|+
literal|")"
return|;
block|}
comment|// ========================================================================
comment|// Min/Max Helpers
comment|// ========================================================================
specifier|public
name|long
name|getActiveMinProcId
parameter_list|()
block|{
name|long
name|minProcId
init|=
name|start
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
name|deleted
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|deleted
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
return|return
name|minProcId
return|;
block|}
if|if
condition|(
name|deleted
index|[
name|i
index|]
operator|!=
name|WORD_MASK
condition|)
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
name|BITS_PER_WORD
condition|;
operator|++
name|j
control|)
block|{
if|if
condition|(
operator|(
name|deleted
index|[
name|i
index|]
operator|&
operator|(
literal|1L
operator|<<
name|j
operator|)
operator|)
operator|==
literal|0
condition|)
block|{
return|return
name|minProcId
operator|+
name|j
return|;
block|}
block|}
block|}
name|minProcId
operator|+=
name|BITS_PER_WORD
expr_stmt|;
block|}
return|return
name|Procedure
operator|.
name|NO_PROC_ID
return|;
block|}
specifier|public
name|long
name|getActiveMaxProcId
parameter_list|()
block|{
name|long
name|maxProcId
init|=
name|getEnd
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|deleted
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
if|if
condition|(
name|deleted
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
return|return
name|maxProcId
return|;
block|}
if|if
condition|(
name|deleted
index|[
name|i
index|]
operator|!=
name|WORD_MASK
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
name|BITS_PER_WORD
operator|-
literal|1
init|;
name|j
operator|>=
literal|0
condition|;
operator|--
name|j
control|)
block|{
if|if
condition|(
operator|(
name|deleted
index|[
name|i
index|]
operator|&
operator|(
literal|1L
operator|<<
name|j
operator|)
operator|)
operator|==
literal|0
condition|)
block|{
return|return
name|maxProcId
operator|-
operator|(
name|BITS_PER_WORD
operator|-
literal|1
operator|-
name|j
operator|)
return|;
block|}
block|}
block|}
name|maxProcId
operator|-=
name|BITS_PER_WORD
expr_stmt|;
block|}
return|return
name|Procedure
operator|.
name|NO_PROC_ID
return|;
block|}
comment|// ========================================================================
comment|// Bitmap Helpers
comment|// ========================================================================
specifier|private
name|int
name|getBitmapIndex
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|procId
operator|-
name|start
argument_list|)
return|;
block|}
name|void
name|updateState
parameter_list|(
name|long
name|procId
parameter_list|,
name|boolean
name|isDeleted
parameter_list|)
block|{
name|int
name|bitmapIndex
init|=
name|getBitmapIndex
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|int
name|wordIndex
init|=
name|bitmapIndex
operator|>>
name|ADDRESS_BITS_PER_WORD
decl_stmt|;
name|long
name|value
init|=
operator|(
literal|1L
operator|<<
name|bitmapIndex
operator|)
decl_stmt|;
name|modified
index|[
name|wordIndex
index|]
operator||=
name|value
expr_stmt|;
if|if
condition|(
name|isDeleted
condition|)
block|{
name|deleted
index|[
name|wordIndex
index|]
operator||=
name|value
expr_stmt|;
block|}
else|else
block|{
name|deleted
index|[
name|wordIndex
index|]
operator|&=
operator|~
name|value
expr_stmt|;
block|}
block|}
comment|// ========================================================================
comment|// Helpers
comment|// ========================================================================
comment|/**    * @return upper boundary (aligned to multiple of BITS_PER_WORD) of bitmap range x belongs to.    */
specifier|private
specifier|static
name|long
name|alignUp
parameter_list|(
specifier|final
name|long
name|x
parameter_list|)
block|{
return|return
operator|(
name|x
operator|+
operator|(
name|BITS_PER_WORD
operator|-
literal|1
operator|)
operator|)
operator|&
operator|-
name|BITS_PER_WORD
return|;
block|}
comment|/**    * @return lower boundary (aligned to multiple of BITS_PER_WORD) of bitmap range x belongs to.    */
specifier|private
specifier|static
name|long
name|alignDown
parameter_list|(
specifier|final
name|long
name|x
parameter_list|)
block|{
return|return
name|x
operator|&
operator|-
name|BITS_PER_WORD
return|;
block|}
block|}
end_class

end_unit
