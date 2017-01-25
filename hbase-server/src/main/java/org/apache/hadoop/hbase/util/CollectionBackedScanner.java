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
name|util
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
name|Collections
import|;
end_import

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
name|SortedSet
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
name|Cell
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
name|CellComparator
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
name|regionserver
operator|.
name|NonReversedNonLazyKeyValueScanner
import|;
end_import

begin_comment
comment|/**  * Utility scanner that wraps a sortable collection and serves as a KeyValueScanner.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CollectionBackedScanner
extends|extends
name|NonReversedNonLazyKeyValueScanner
block|{
specifier|final
specifier|private
name|Iterable
argument_list|<
name|Cell
argument_list|>
name|data
decl_stmt|;
specifier|final
name|CellComparator
name|comparator
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
name|CollectionBackedScanner
parameter_list|(
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|set
parameter_list|)
block|{
name|this
argument_list|(
name|set
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CollectionBackedScanner
parameter_list|(
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|set
parameter_list|,
name|CellComparator
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|data
operator|=
name|set
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CollectionBackedScanner
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|list
parameter_list|)
block|{
name|this
argument_list|(
name|list
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CollectionBackedScanner
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|list
parameter_list|,
name|CellComparator
name|comparator
parameter_list|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|data
operator|=
name|list
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CollectionBackedScanner
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|Cell
modifier|...
name|array
parameter_list|)
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|array
operator|.
name|length
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|tmp
argument_list|,
name|array
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|tmp
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|data
operator|=
name|tmp
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
block|{
name|iter
operator|=
name|data
operator|.
name|iterator
argument_list|()
expr_stmt|;
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
name|iter
operator|=
name|data
operator|.
name|iterator
argument_list|()
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
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Cell
name|next
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|ret
init|=
name|comparator
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
comment|/**    * @see org.apache.hadoop.hbase.regionserver.KeyValueScanner#getScannerOrder()    */
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
comment|// do nothing
block|}
block|}
end_class

end_unit

