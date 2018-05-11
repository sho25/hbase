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
package|;
end_package

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertTrue
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
name|ArrayList
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
operator|.
name|Type
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
name|ByteBufferUtils
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
name|TestByteBufferKeyValue
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
name|TestByteBufferKeyValue
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QUAL2
init|=
literal|"qual2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAM2
init|=
literal|"fam2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QUAL1
init|=
literal|"qual1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAM1
init|=
literal|"fam1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW1
init|=
literal|"row1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAM1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|fam2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAM2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qual1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|QUAL1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|qual2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|QUAL2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Tag
name|t1
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TAG1"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Tag
name|t2
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TAG2"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ArrayList
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|tags
operator|.
name|add
argument_list|(
name|t1
argument_list|)
expr_stmt|;
name|tags
operator|.
name|add
argument_list|(
name|t2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompare
parameter_list|()
block|{
name|Cell
name|cell1
init|=
name|getOffheapCell
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|)
decl_stmt|;
name|Cell
name|cell2
init|=
name|getOffheapCell
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qual2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|cell1
argument_list|,
name|cell2
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|Cell
name|cell3
init|=
name|getOffheapCell
argument_list|(
name|row1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"wide_family"
argument_list|)
argument_list|,
name|qual2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|cell1
argument_list|,
name|cell3
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|Cell
name|cell4
init|=
name|getOffheapCell
argument_list|(
name|row1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|qual2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|cell1
argument_list|,
name|cell4
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Cell
name|getOffheapCell
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|KeyValue
name|kvCell
init|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
literal|0L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|buf
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|,
literal|0L
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testByteBufferBackedKeyValue
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
name|kvCell
init|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
literal|0L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|row1
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|buf
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|ByteBufferExtendedCell
name|offheapKV
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FAM1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|QUAL1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValuePosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|offheapKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
comment|// Use the array() APIs
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FAM1
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|QUAL1
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|offheapKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
name|kvCell
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam2
argument_list|,
name|qual2
argument_list|,
literal|0L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|row1
argument_list|)
expr_stmt|;
name|buf
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|buf
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|offheapKV
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FAM2
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|QUAL2
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|nullQualifier
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|kvCell
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|nullQualifier
argument_list|,
literal|0L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|row1
argument_list|)
expr_stmt|;
name|buf
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|buf
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|offheapKV
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FAM1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValuePosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|offheapKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testByteBufferBackedKeyValueWithTags
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
name|kvCell
init|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
literal|0L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|row1
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|buf
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|ByteBufferKeyValue
name|offheapKV
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FAM1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|QUAL1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKV
operator|.
name|getValueByteBuffer
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValuePosition
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|offheapKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|offheapKV
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
comment|// change tags to handle both onheap and offheap stuff
name|List
argument_list|<
name|Tag
argument_list|>
name|resTags
init|=
name|PrivateCellUtil
operator|.
name|getTags
argument_list|(
name|offheapKV
argument_list|)
decl_stmt|;
name|Tag
name|tag1
init|=
name|resTags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|t1
operator|.
name|getType
argument_list|()
argument_list|,
name|tag1
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Tag
operator|.
name|getValueAsString
argument_list|(
name|t1
argument_list|)
argument_list|,
name|Tag
operator|.
name|getValueAsString
argument_list|(
name|tag1
argument_list|)
argument_list|)
expr_stmt|;
name|Tag
name|tag2
init|=
name|resTags
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tag2
operator|.
name|getType
argument_list|()
argument_list|,
name|tag2
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Tag
operator|.
name|getValueAsString
argument_list|(
name|t2
argument_list|)
argument_list|,
name|Tag
operator|.
name|getValueAsString
argument_list|(
name|tag2
argument_list|)
argument_list|)
expr_stmt|;
name|Tag
name|res
init|=
name|PrivateCellUtil
operator|.
name|getTag
argument_list|(
name|offheapKV
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Tag
operator|.
name|getValueAsString
argument_list|(
name|t2
argument_list|)
argument_list|,
name|Tag
operator|.
name|getValueAsString
argument_list|(
name|tag2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|PrivateCellUtil
operator|.
name|getTag
argument_list|(
name|offheapKV
argument_list|,
operator|(
name|byte
operator|)
literal|3
argument_list|)
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetKeyMethods
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
name|kvCell
init|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qual1
argument_list|,
literal|0L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|row1
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kvCell
operator|.
name|getKeyLength
argument_list|()
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|buf
argument_list|,
name|kvCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kvCell
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|kvCell
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBufferExtendedCell
name|offheapKeyOnlyKV
init|=
operator|new
name|ByteBufferKeyOnlyKeyValue
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|capacity
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ROW1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKeyOnlyKV
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FAM1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKeyOnlyKV
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|QUAL1
argument_list|,
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|offheapKeyOnlyKV
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|offheapKeyOnlyKV
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

