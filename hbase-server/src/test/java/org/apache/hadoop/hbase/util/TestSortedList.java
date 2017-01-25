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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

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
name|ListIterator
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
name|Lists
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSortedList
block|{
specifier|static
class|class
name|StringComparator
implements|implements
name|Comparator
argument_list|<
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|String
name|o1
parameter_list|,
name|String
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|compareTo
argument_list|(
name|o2
argument_list|)
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSorting
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedList
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|SortedList
argument_list|<>
argument_list|(
operator|new
name|StringComparator
argument_list|()
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"d"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"b"
block|,
literal|"c"
block|,
literal|"d"
block|}
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|4
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"b"
block|,
literal|"c"
block|,
literal|"c"
block|,
literal|"d"
block|}
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|5
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test that removal from head or middle maintains sort
name|list
operator|.
name|remove
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"c"
block|,
literal|"c"
block|,
literal|"d"
block|}
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|4
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|remove
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"c"
block|,
literal|"d"
block|}
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|3
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|remove
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"c"
block|,
literal|"d"
block|}
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|2
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadOnlyIterators
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedList
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|SortedList
argument_list|<>
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|,
literal|"d"
argument_list|,
literal|"e"
argument_list|)
argument_list|,
operator|new
name|StringComparator
argument_list|()
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|i
init|=
name|list
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|i
operator|.
name|next
argument_list|()
expr_stmt|;
try|try
block|{
name|i
operator|.
name|remove
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Iterator should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// ok
block|}
name|ListIterator
argument_list|<
name|String
argument_list|>
name|li
init|=
name|list
operator|.
name|listIterator
argument_list|()
decl_stmt|;
name|li
operator|.
name|next
argument_list|()
expr_stmt|;
try|try
block|{
name|li
operator|.
name|add
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Iterator should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// ok
block|}
try|try
block|{
name|li
operator|.
name|set
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Iterator should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// ok
block|}
try|try
block|{
name|li
operator|.
name|remove
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Iterator should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// ok
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIteratorIsolation
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedList
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|SortedList
argument_list|<>
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|,
literal|"d"
argument_list|,
literal|"e"
argument_list|)
argument_list|,
operator|new
name|StringComparator
argument_list|()
argument_list|)
decl_stmt|;
comment|// isolation of remove()
name|Iterator
argument_list|<
name|String
argument_list|>
name|iter
init|=
name|list
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|list
operator|.
name|remove
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|found
condition|)
block|{
name|found
operator|=
literal|"c"
operator|.
name|equals
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
name|iter
operator|=
name|list
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|found
operator|=
literal|false
expr_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|found
condition|)
block|{
name|found
operator|=
literal|"c"
operator|.
name|equals
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|found
argument_list|)
expr_stmt|;
comment|// isolation of add()
name|iter
operator|=
name|list
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"f"
argument_list|)
expr_stmt|;
name|found
operator|=
literal|false
expr_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|found
condition|)
block|{
name|String
name|next
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|found
operator|=
literal|"f"
operator|.
name|equals
argument_list|(
name|next
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|found
argument_list|)
expr_stmt|;
comment|// isolation of addAll()
name|iter
operator|=
name|list
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|list
operator|.
name|addAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"g"
argument_list|,
literal|"h"
argument_list|,
literal|"i"
argument_list|)
argument_list|)
expr_stmt|;
name|found
operator|=
literal|false
expr_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|found
condition|)
block|{
name|String
name|next
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|found
operator|=
literal|"g"
operator|.
name|equals
argument_list|(
name|next
argument_list|)
operator|||
literal|"h"
operator|.
name|equals
argument_list|(
name|next
argument_list|)
operator|||
literal|"i"
operator|.
name|equals
argument_list|(
name|next
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|found
argument_list|)
expr_stmt|;
comment|// isolation of clear()
name|iter
operator|=
name|list
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|size
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
name|size
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|size
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomAccessIsolation
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedList
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|SortedList
argument_list|<>
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|,
operator|new
name|StringComparator
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|innerList
init|=
name|list
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|innerList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|innerList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|innerList
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

