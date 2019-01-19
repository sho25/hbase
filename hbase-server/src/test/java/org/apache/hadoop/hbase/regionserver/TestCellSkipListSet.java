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
name|SortedSet
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|CellComparatorImpl
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
name|CellUtil
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
name|HBaseClassTestRule
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
name|KeyValue
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
name|RegionServerTests
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
name|junit
operator|.
name|ClassRule
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
name|RegionServerTests
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
name|TestCellSkipListSet
extends|extends
name|TestCase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestCellSkipListSet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|CellSet
name|csls
init|=
operator|new
name|CellSet
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|csls
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|bytes
argument_list|,
name|bytes
argument_list|)
decl_stmt|;
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|this
operator|.
name|csls
operator|.
name|contains
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|this
operator|.
name|csls
operator|.
name|getDelegatee
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|first
init|=
name|this
operator|.
name|csls
operator|.
name|first
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|kv
operator|.
name|equals
argument_list|(
name|first
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|first
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|first
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|first
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now try overwritting
name|byte
index|[]
name|overwriteValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"overwrite"
argument_list|)
decl_stmt|;
name|KeyValue
name|overwrite
init|=
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|bytes
argument_list|,
name|overwriteValue
argument_list|)
decl_stmt|;
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
name|overwrite
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|this
operator|.
name|csls
operator|.
name|getDelegatee
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
name|this
operator|.
name|csls
operator|.
name|first
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|overwrite
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|overwrite
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|overwrite
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|first
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|first
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|first
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|overwrite
argument_list|)
argument_list|,
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testIterator
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|total
init|=
literal|3
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
name|total
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Assert that we added 'total' values and that they are in order
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|this
operator|.
name|csls
control|)
block|{
name|assertEquals
argument_list|(
literal|""
operator|+
name|count
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|value1
argument_list|,
literal|0
argument_list|,
name|value1
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|total
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// Now overwrite with a new value.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|total
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Assert that we added 'total' values and that they are in order and that
comment|// we are getting back value2
name|count
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|this
operator|.
name|csls
control|)
block|{
name|assertEquals
argument_list|(
literal|""
operator|+
name|count
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|value2
argument_list|,
literal|0
argument_list|,
name|value2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|total
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testDescendingIterator
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|total
init|=
literal|3
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
name|total
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Assert that we added 'total' values and that they are in order
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|i
init|=
name|this
operator|.
name|csls
operator|.
name|descendingIterator
argument_list|()
init|;
name|i
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Cell
name|kv
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|""
operator|+
operator|(
name|total
operator|-
operator|(
name|count
operator|+
literal|1
operator|)
operator|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|value1
argument_list|,
literal|0
argument_list|,
name|value1
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|total
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// Now overwrite with a new value.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|total
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Assert that we added 'total' values and that they are in order and that
comment|// we are getting back value2
name|count
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|i
init|=
name|this
operator|.
name|csls
operator|.
name|descendingIterator
argument_list|()
init|;
name|i
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Cell
name|kv
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|""
operator|+
operator|(
name|total
operator|-
operator|(
name|count
operator|+
literal|1
operator|)
operator|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|value2
argument_list|,
literal|0
argument_list|,
name|value2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|total
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHeadTail
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|total
init|=
literal|3
decl_stmt|;
name|KeyValue
name|splitter
init|=
literal|null
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
name|total
condition|;
name|i
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|,
name|value1
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|1
condition|)
name|splitter
operator|=
name|kv
expr_stmt|;
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tail
init|=
name|this
operator|.
name|csls
operator|.
name|tailSet
argument_list|(
name|splitter
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tail
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|head
init|=
name|this
operator|.
name|csls
operator|.
name|headSet
argument_list|(
name|splitter
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|head
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now ensure that we get back right answer even when we do tail or head.
comment|// Now overwrite with a new value.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|total
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|csls
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|bytes
argument_list|,
name|bytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tail
operator|=
name|this
operator|.
name|csls
operator|.
name|tailSet
argument_list|(
name|splitter
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|tail
operator|.
name|first
argument_list|()
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|tail
operator|.
name|first
argument_list|()
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|tail
operator|.
name|first
argument_list|()
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|value2
argument_list|,
literal|0
argument_list|,
name|value2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|head
operator|=
name|this
operator|.
name|csls
operator|.
name|headSet
argument_list|(
name|splitter
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|head
operator|.
name|first
argument_list|()
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|head
operator|.
name|first
argument_list|()
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|head
operator|.
name|first
argument_list|()
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|value2
argument_list|,
literal|0
argument_list|,
name|value2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

