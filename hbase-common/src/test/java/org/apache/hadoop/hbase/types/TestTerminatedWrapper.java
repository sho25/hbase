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
name|types
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
name|assertArrayEquals
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
name|assertEquals
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Order
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
name|PositionedByteRange
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
name|SimplePositionedMutableByteRange
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
name|TestTerminatedWrapper
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
name|TestTerminatedWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
index|[]
name|VALUES_STRINGS
init|=
operator|new
name|String
index|[]
block|{
literal|""
block|,
literal|"1"
block|,
literal|"22"
block|,
literal|"333"
block|,
literal|"4444"
block|,
literal|"55555"
block|,
literal|"666666"
block|,
literal|"7777777"
block|,
literal|"88888888"
block|,
literal|"999999999"
block|,   }
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|VALUES_BYTES
init|=
operator|new
name|byte
index|[
name|VALUES_STRINGS
operator|.
name|length
index|]
index|[]
decl_stmt|;
static|static
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
name|VALUES_STRINGS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|VALUES_BYTES
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUES_STRINGS
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|TERMINATORS
init|=
operator|new
name|byte
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
block|{
operator|-
literal|2
block|}
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
block|}
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testEmptyDelimiter
parameter_list|()
block|{
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNullDelimiter
parameter_list|()
block|{
operator|new
name|RawBytesTerminated
argument_list|(
operator|(
name|byte
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
comment|// new TerminatedWrapper<byte[]>(new RawBytes(), (byte[]) null);
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testEncodedValueContainsTerm
parameter_list|()
block|{
specifier|final
name|DataType
argument_list|<
name|byte
index|[]
argument_list|>
name|type
init|=
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
specifier|final
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|16
argument_list|)
decl_stmt|;
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hello foobar!"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadWriteSkippable
parameter_list|()
block|{
specifier|final
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|14
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|OrderedString
name|t
range|:
operator|new
name|OrderedString
index|[]
block|{
operator|new
name|OrderedString
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
block|,
operator|new
name|OrderedString
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|)
block|}
control|)
block|{
for|for
control|(
specifier|final
name|byte
index|[]
name|term
range|:
name|TERMINATORS
control|)
block|{
for|for
control|(
specifier|final
name|String
name|val
range|:
name|VALUES_STRINGS
control|)
block|{
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|DataType
argument_list|<
name|String
argument_list|>
name|type
init|=
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
name|t
argument_list|,
name|term
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|val
operator|.
name|length
argument_list|()
operator|+
literal|2
operator|+
name|term
operator|.
name|length
argument_list|,
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|val
argument_list|,
name|type
operator|.
name|decode
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|val
operator|.
name|length
argument_list|()
operator|+
literal|2
operator|+
name|term
operator|.
name|length
argument_list|,
name|buff
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadWriteNonSkippable
parameter_list|()
block|{
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|12
argument_list|)
decl_stmt|;
for|for
control|(
name|Order
name|ord
range|:
operator|new
name|Order
index|[]
block|{
name|Order
operator|.
name|ASCENDING
block|,
name|Order
operator|.
name|DESCENDING
block|}
control|)
block|{
for|for
control|(
name|byte
index|[]
name|term
range|:
name|TERMINATORS
control|)
block|{
for|for
control|(
name|byte
index|[]
name|val
range|:
name|VALUES_BYTES
control|)
block|{
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|DataType
argument_list|<
name|byte
index|[]
argument_list|>
name|type
init|=
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|ord
argument_list|)
argument_list|,
name|term
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|val
operator|.
name|length
operator|+
name|term
operator|.
name|length
argument_list|,
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|val
argument_list|,
name|type
operator|.
name|decode
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|val
operator|.
name|length
operator|+
name|term
operator|.
name|length
argument_list|,
name|buff
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkipSkippable
parameter_list|()
block|{
specifier|final
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|14
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|OrderedString
name|t
range|:
operator|new
name|OrderedString
index|[]
block|{
operator|new
name|OrderedString
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
block|,
operator|new
name|OrderedString
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|)
block|}
control|)
block|{
for|for
control|(
specifier|final
name|byte
index|[]
name|term
range|:
name|TERMINATORS
control|)
block|{
for|for
control|(
specifier|final
name|String
name|val
range|:
name|VALUES_STRINGS
control|)
block|{
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|DataType
argument_list|<
name|String
argument_list|>
name|type
init|=
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
name|t
argument_list|,
name|term
argument_list|)
decl_stmt|;
specifier|final
name|int
name|expected
init|=
name|val
operator|.
name|length
argument_list|()
operator|+
literal|2
operator|+
name|term
operator|.
name|length
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|type
operator|.
name|skip
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|buff
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkipNonSkippable
parameter_list|()
block|{
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|12
argument_list|)
decl_stmt|;
for|for
control|(
name|Order
name|ord
range|:
operator|new
name|Order
index|[]
block|{
name|Order
operator|.
name|ASCENDING
block|,
name|Order
operator|.
name|DESCENDING
block|}
control|)
block|{
for|for
control|(
name|byte
index|[]
name|term
range|:
name|TERMINATORS
control|)
block|{
for|for
control|(
name|byte
index|[]
name|val
range|:
name|VALUES_BYTES
control|)
block|{
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|DataType
argument_list|<
name|byte
index|[]
argument_list|>
name|type
init|=
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|ord
argument_list|)
argument_list|,
name|term
argument_list|)
decl_stmt|;
name|int
name|expected
init|=
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|val
argument_list|)
decl_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|type
operator|.
name|skip
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|buff
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testInvalidSkip
parameter_list|()
block|{
specifier|final
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|DataType
argument_list|<
name|byte
index|[]
argument_list|>
name|type
init|=
operator|new
name|TerminatedWrapper
argument_list|<>
argument_list|(
operator|new
name|RawBytes
argument_list|(
name|Order
operator|.
name|ASCENDING
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|)
decl_stmt|;
name|type
operator|.
name|skip
argument_list|(
name|buff
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

