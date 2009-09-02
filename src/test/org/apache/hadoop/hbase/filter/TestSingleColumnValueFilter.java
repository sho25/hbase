begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|util
operator|.
name|Bytes
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

begin_comment
comment|/**  * Tests the value filter  */
end_comment

begin_class
specifier|public
class|class
name|TestSingleColumnValueFilter
extends|extends
name|TestCase
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
name|Filter
name|basicFilter
decl_stmt|;
name|Filter
name|substrFilter
decl_stmt|;
name|Filter
name|regexFilter
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
name|basicFilter
operator|=
name|basicFilterNew
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
name|CompareOp
operator|.
name|GREATER_OR_EQUAL
argument_list|,
name|VAL_2
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
name|CompareOp
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
name|CompareOp
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
name|void
name|basicFilterTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|kv
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
name|VAL_1
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"basicFilter1"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|kv
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
literal|"basicFilter2"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|kv
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
literal|"basicFilter3"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|kv
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
literal|"basicFilter4"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
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
literal|"basicFilterAllRemaining"
argument_list|,
name|filter
operator|.
name|filterAllRemaining
argument_list|()
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
name|substrFilterTests
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|kv
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
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|kv
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
name|assertFalse
argument_list|(
literal|"substrFalse"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
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
name|kv
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
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|kv
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
name|assertFalse
argument_list|(
literal|"regexFalse"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
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
name|ByteArrayOutputStream
name|stream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|filter
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|stream
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
comment|// Recompose filter.
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|buffer
argument_list|)
argument_list|)
decl_stmt|;
name|Filter
name|newFilter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|()
decl_stmt|;
name|newFilter
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|newFilter
return|;
block|}
comment|/**    * Tests identification of the stop row    * @throws Exception    */
specifier|public
name|void
name|testStop
parameter_list|()
throws|throws
name|Exception
block|{
name|basicFilterTests
argument_list|(
name|basicFilter
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
block|}
comment|/**    * Tests serialization    * @throws Exception    */
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
block|}
block|}
end_class

end_unit

