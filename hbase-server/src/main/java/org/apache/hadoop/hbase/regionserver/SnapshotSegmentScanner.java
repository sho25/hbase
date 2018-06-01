begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Iterator
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A basic SegmentScanner used against an ImmutableScanner snapshot  * Used flushing where we do a single pass, no reverse scanning or  * inserts happening. Its a dumbed-down Scanner that can go fast.  * Like {@link org.apache.hadoop.hbase.util.CollectionBackedScanner}  * (but making it know about Segments was onerous).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SnapshotSegmentScanner
extends|extends
name|NonReversedNonLazyKeyValueScanner
block|{
specifier|private
specifier|final
name|ImmutableSegment
name|segment
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iter
decl_stmt|;
specifier|private
name|Cell
name|current
decl_stmt|;
specifier|public
name|SnapshotSegmentScanner
parameter_list|(
name|ImmutableSegment
name|segment
parameter_list|)
block|{
name|this
operator|.
name|segment
operator|=
name|segment
expr_stmt|;
name|this
operator|.
name|segment
operator|.
name|incScannerCount
argument_list|()
expr_stmt|;
name|this
operator|.
name|iter
operator|=
name|createIterator
argument_list|(
name|this
operator|.
name|segment
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|this
operator|.
name|current
operator|=
name|this
operator|.
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|createIterator
parameter_list|(
name|Segment
name|segment
parameter_list|)
block|{
return|return
name|segment
operator|.
name|getCellSet
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|peek
parameter_list|()
block|{
return|return
name|current
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|next
parameter_list|()
block|{
name|Cell
name|oldCurrent
init|=
name|current
decl_stmt|;
if|if
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|oldCurrent
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seek
parameter_list|(
name|Cell
name|seekCell
parameter_list|)
block|{
comment|// restart iterator
name|this
operator|.
name|iter
operator|=
name|createIterator
argument_list|(
name|this
operator|.
name|segment
argument_list|)
expr_stmt|;
return|return
name|reseek
argument_list|(
name|seekCell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reseek
parameter_list|(
name|Cell
name|seekCell
parameter_list|)
block|{
while|while
condition|(
name|this
operator|.
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Cell
name|next
init|=
name|this
operator|.
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|ret
init|=
name|this
operator|.
name|segment
operator|.
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|next
argument_list|,
name|seekCell
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|>=
literal|0
condition|)
block|{
name|this
operator|.
name|current
operator|=
name|next
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @see KeyValueScanner#getScannerOrder()    */
annotation|@
name|Override
specifier|public
name|long
name|getScannerOrder
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|segment
operator|.
name|decScannerCount
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

