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
name|filter
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
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|regex
operator|.
name|Pattern
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
name|ByteBufferKeyValue
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
name|CompareOperator
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|FilterTests
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
name|Before
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

begin_comment
comment|/**  * Tests the value filter  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|FilterTests
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
name|TestSingleColumnValueFilter
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ab"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abcd"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FULLSTRING_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"The quick brown fox jumps over the lazy dog."
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FULLSTRING_2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"The slow grey fox trips over the lazy dog."
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QUICK_SUBSTR
init|=
literal|"quick"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QUICK_REGEX
init|=
literal|".+quick.+"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|QUICK_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"QuIcK"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator||
name|Pattern
operator|.
name|DOTALL
argument_list|)
decl_stmt|;
name|Filter
name|basicFilter
decl_stmt|;
name|Filter
name|nullFilter
decl_stmt|;
name|Filter
name|substrFilter
decl_stmt|;
name|Filter
name|regexFilter
decl_stmt|;
name|Filter
name|regexPatternFilter
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|basicFilter
operator|=
name|basicFilterNew
argument_list|()
expr_stmt|;
name|nullFilter
operator|=
name|nullFilterNew
argument_list|()
expr_stmt|;
name|substrFilter
operator|=
name|substrFilterNew
argument_list|()
expr_stmt|;
name|regexFilter
operator|=
name|regexFilterNew
argument_list|()
expr_stmt|;
name|regexPatternFilter
operator|=
name|regexFilterNew
argument_list|(
name|QUICK_PATTERN
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Filter
name|basicFilterNew
parameter_list|()
block|{
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|GREATER_OR_EQUAL
argument_list|,
name|VAL_2
argument_list|)
return|;
block|}
specifier|private
name|Filter
name|nullFilterNew
parameter_list|()
block|{
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|NOT_EQUAL
argument_list|,
operator|new
name|NullComparator
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|Filter
name|substrFilterNew
parameter_list|()
block|{
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|SubstringComparator
argument_list|(
name|QUICK_SUBSTR
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Filter
name|regexFilterNew
parameter_list|()
block|{
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|RegexStringComparator
argument_list|(
name|QUICK_REGEX
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Filter
name|regexFilterNew
parameter_list|(
name|Pattern
name|pattern
parameter_list|)
block|{
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|RegexStringComparator
argument_list|(
name|pattern
operator|.
name|pattern
argument_list|()
argument_list|,
name|pattern
operator|.
name|flags
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLongComparator
parameter_list|()
throws|throws
name|IOException
block|{
name|Filter
name|filter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|CompareOperator
operator|.
name|GREATER
argument_list|,
operator|new
name|LongComparator
argument_list|(
literal|100L
argument_list|)
argument_list|)
decl_stmt|;
name|KeyValue
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1L
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"less than"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|cell
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|Cell
name|c
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"less than"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Equals 100"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Equals 100"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|120L
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"include 120"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"include 120"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|basicFilterTests
parameter_list|(
name|SingleColumnValueFilter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter1"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|cell
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|Cell
name|c
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter1"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter2"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter2"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_4
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter3"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter3"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"basicFilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter4"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter4"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter4"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter4"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|NEXT_ROW
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"basicFilterAllRemaining"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|filter
operator|.
name|setLatestVersionOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter5"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter5"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter5"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter5"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"basicFilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|nullFilterTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
operator|(
operator|(
name|SingleColumnValueFilter
operator|)
name|filter
operator|)
operator|.
name|setFilterIfMissing
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|KeyValue
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|FULLSTRING_1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"null1"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|cell
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|Cell
name|c
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"null1"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"null1FilterRow"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual2"
argument_list|)
argument_list|,
name|FULLSTRING_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"null2"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"null2"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"null2FilterRow"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|substrFilterTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|FULLSTRING_1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"substrTrue"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|cell
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|Cell
name|c
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"substrTrue"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|FULLSTRING_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"substrFalse"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"substrFalse"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"substrFilterAllRemaining"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"substrFilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|regexFilterTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|FULLSTRING_1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"regexTrue"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|cell
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|Cell
name|c
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"regexTrue"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|cell
operator|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|FULLSTRING_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"regexFalse"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cell
operator|.
name|getBuffer
argument_list|()
expr_stmt|;
name|c
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"regexFalse"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"regexFilterAllRemaining"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"regexFilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|regexPatternFilterTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|FULLSTRING_1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"regexTrue"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|cell
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|Cell
name|c
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"regexTrue"
argument_list|,
name|filter
operator|.
name|filterCell
argument_list|(
name|c
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"regexFilterAllRemaining"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"regexFilterNotNull"
argument_list|,
name|filter
operator|.
name|filterRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Filter
name|serializationTest
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Decompose filter to bytes.
name|byte
index|[]
name|buffer
init|=
name|filter
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
comment|// Recompose filter.
name|Filter
name|newFilter
init|=
name|SingleColumnValueFilter
operator|.
name|parseFrom
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
return|return
name|newFilter
return|;
block|}
comment|/**    * Tests identification of the stop row    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testStop
parameter_list|()
throws|throws
name|Exception
block|{
name|basicFilterTests
argument_list|(
operator|(
name|SingleColumnValueFilter
operator|)
name|basicFilter
argument_list|)
expr_stmt|;
name|nullFilterTests
argument_list|(
name|nullFilter
argument_list|)
expr_stmt|;
name|substrFilterTests
argument_list|(
name|substrFilter
argument_list|)
expr_stmt|;
name|regexFilterTests
argument_list|(
name|regexFilter
argument_list|)
expr_stmt|;
name|regexPatternFilterTests
argument_list|(
name|regexPatternFilter
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests serialization    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|newFilter
init|=
name|serializationTest
argument_list|(
name|basicFilter
argument_list|)
decl_stmt|;
name|basicFilterTests
argument_list|(
operator|(
name|SingleColumnValueFilter
operator|)
name|newFilter
argument_list|)
expr_stmt|;
name|newFilter
operator|=
name|serializationTest
argument_list|(
name|nullFilter
argument_list|)
expr_stmt|;
name|nullFilterTests
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
name|newFilter
operator|=
name|serializationTest
argument_list|(
name|substrFilter
argument_list|)
expr_stmt|;
name|substrFilterTests
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
name|newFilter
operator|=
name|serializationTest
argument_list|(
name|regexFilter
argument_list|)
expr_stmt|;
name|regexFilterTests
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
name|newFilter
operator|=
name|serializationTest
argument_list|(
name|regexPatternFilter
argument_list|)
expr_stmt|;
name|regexPatternFilterTests
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

