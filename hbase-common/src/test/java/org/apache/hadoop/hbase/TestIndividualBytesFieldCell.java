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
name|ByteArrayOutputStream
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
name|junit
operator|.
name|BeforeClass
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
name|TestIndividualBytesFieldCell
block|{
specifier|private
specifier|static
name|IndividualBytesFieldCell
name|ic0
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|KeyValue
name|kv0
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|testConstructorAndVerify
parameter_list|()
block|{
comment|// Immutable inputs
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-row"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-family"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-qualifier"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-value"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tags
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-tags"
argument_list|)
decl_stmt|;
comment|// Other inputs
name|long
name|timestamp
init|=
literal|5000L
decl_stmt|;
name|long
name|seqId
init|=
literal|0L
decl_stmt|;
name|KeyValue
operator|.
name|Type
name|type
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|Put
decl_stmt|;
name|ic0
operator|=
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|seqId
argument_list|,
name|value
argument_list|,
name|tags
argument_list|)
expr_stmt|;
name|kv0
operator|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|value
argument_list|,
name|tags
argument_list|)
expr_stmt|;
comment|// Verify if no local copy is made for row, family, qualifier, value or tags.
name|assertTrue
argument_list|(
name|ic0
operator|.
name|getRowArray
argument_list|()
operator|==
name|row
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ic0
operator|.
name|getFamilyArray
argument_list|()
operator|==
name|family
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ic0
operator|.
name|getQualifierArray
argument_list|()
operator|==
name|qualifier
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ic0
operator|.
name|getValueArray
argument_list|()
operator|==
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ic0
operator|.
name|getTagsArray
argument_list|()
operator|==
name|tags
argument_list|)
expr_stmt|;
comment|// Verify others.
name|assertEquals
argument_list|(
name|timestamp
argument_list|,
name|ic0
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|seqId
argument_list|,
name|ic0
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|type
operator|.
name|getCode
argument_list|()
argument_list|,
name|ic0
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify offsets of backing byte arrays are always 0.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ic0
operator|.
name|getRowOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ic0
operator|.
name|getFamilyOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ic0
operator|.
name|getQualifierOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ic0
operator|.
name|getValueOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ic0
operator|.
name|getTagsOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Verify clone() and deepClone()
annotation|@
name|Test
specifier|public
name|void
name|testClone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
comment|// Verify clone. Only shadow copies are made for backing byte arrays.
name|IndividualBytesFieldCell
name|cloned
init|=
operator|(
name|IndividualBytesFieldCell
operator|)
name|ic0
operator|.
name|clone
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cloned
operator|.
name|getRowArray
argument_list|()
operator|==
name|ic0
operator|.
name|getRowArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cloned
operator|.
name|getFamilyArray
argument_list|()
operator|==
name|ic0
operator|.
name|getFamilyArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cloned
operator|.
name|getQualifierArray
argument_list|()
operator|==
name|ic0
operator|.
name|getQualifierArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cloned
operator|.
name|getValueArray
argument_list|()
operator|==
name|ic0
operator|.
name|getValueArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cloned
operator|.
name|getTagsArray
argument_list|()
operator|==
name|ic0
operator|.
name|getTagsArray
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify if deep clone returns a KeyValue object
name|assertTrue
argument_list|(
name|ic0
operator|.
name|deepClone
argument_list|()
operator|instanceof
name|KeyValue
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify KeyValue format related functions: write() and getSerializedSize().    * Should have the same behaviors as {@link KeyValue}.    */
annotation|@
name|Test
specifier|public
name|void
name|testWriteIntoKeyValueFormat
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Verify getSerializedSize().
name|assertEquals
argument_list|(
name|kv0
operator|.
name|getSerializedSize
argument_list|(
literal|true
argument_list|)
argument_list|,
name|ic0
operator|.
name|getSerializedSize
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// with tags
name|assertEquals
argument_list|(
name|kv0
operator|.
name|getSerializedSize
argument_list|(
literal|false
argument_list|)
argument_list|,
name|ic0
operator|.
name|getSerializedSize
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// without tags
comment|// Verify writing into ByteBuffer.
name|ByteBuffer
name|bbufIC
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|ic0
operator|.
name|getSerializedSize
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|ic0
operator|.
name|write
argument_list|(
name|bbufIC
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|ByteBuffer
name|bbufKV
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|kv0
operator|.
name|getSerializedSize
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|kv0
operator|.
name|write
argument_list|(
name|bbufKV
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bbufIC
operator|.
name|equals
argument_list|(
name|bbufKV
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify writing into OutputStream.
name|testWriteIntoOutputStream
argument_list|(
name|ic0
argument_list|,
name|kv0
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// with tags
name|testWriteIntoOutputStream
argument_list|(
name|ic0
argument_list|,
name|kv0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// without tags
block|}
comment|/**    * @param ic An instance of IndividualBytesFieldCell to compare.    * @param kv An instance of KeyValue to compare.    * @param withTags Whether to write tags.    * @throws IOException    */
specifier|private
name|void
name|testWriteIntoOutputStream
parameter_list|(
name|IndividualBytesFieldCell
name|ic
parameter_list|,
name|KeyValue
name|kv
parameter_list|,
name|boolean
name|withTags
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|outIC
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
name|ic
operator|.
name|getSerializedSize
argument_list|(
name|withTags
argument_list|)
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|outKV
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
name|kv
operator|.
name|getSerializedSize
argument_list|(
name|withTags
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|kv
operator|.
name|write
argument_list|(
name|outKV
argument_list|,
name|withTags
argument_list|)
argument_list|,
name|ic
operator|.
name|write
argument_list|(
name|outIC
argument_list|,
name|withTags
argument_list|)
argument_list|)
expr_stmt|;
comment|// compare the number of bytes written
name|assertArrayEquals
argument_list|(
name|outKV
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|outIC
operator|.
name|getBuffer
argument_list|()
argument_list|)
expr_stmt|;
comment|// compare the underlying byte array
block|}
comment|/**    * Verify getXXXArray() and getXXXLength() when family/qualifier/value/tags are null.    * Should have the same behaviors as {@link KeyValue}.    */
annotation|@
name|Test
specifier|public
name|void
name|testNullFamilyQualifierValueTags
parameter_list|()
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|long
name|timestamp
init|=
literal|5000L
decl_stmt|;
name|long
name|seqId
init|=
literal|0L
decl_stmt|;
name|KeyValue
operator|.
name|Type
name|type
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|Put
decl_stmt|;
comment|// Test when following fields are null.
name|byte
index|[]
name|family
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|value
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|tags
init|=
literal|null
decl_stmt|;
name|Cell
name|ic1
init|=
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|seqId
argument_list|,
name|value
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|Cell
name|kv1
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
name|timestamp
argument_list|,
name|type
argument_list|,
name|value
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyArrayInKV
init|=
name|Bytes
operator|.
name|copy
argument_list|(
name|kv1
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|kv1
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifierArrayInKV
init|=
name|Bytes
operator|.
name|copy
argument_list|(
name|kv1
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv1
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valueArrayInKV
init|=
name|Bytes
operator|.
name|copy
argument_list|(
name|kv1
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv1
operator|.
name|getValueLength
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tagsArrayInKV
init|=
name|Bytes
operator|.
name|copy
argument_list|(
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsLength
argument_list|()
argument_list|)
decl_stmt|;
comment|// getXXXArray() for family, qualifier, value and tags are supposed to return empty byte array, rather than null.
name|assertArrayEquals
argument_list|(
name|familyArrayInKV
argument_list|,
name|ic1
operator|.
name|getFamilyArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|qualifierArrayInKV
argument_list|,
name|ic1
operator|.
name|getQualifierArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|valueArrayInKV
argument_list|,
name|ic1
operator|.
name|getValueArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|tagsArrayInKV
argument_list|,
name|ic1
operator|.
name|getTagsArray
argument_list|()
argument_list|)
expr_stmt|;
comment|// getXXXLength() for family, qualifier, value and tags are supposed to return 0.
name|assertEquals
argument_list|(
name|kv1
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|ic1
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|kv1
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|ic1
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|kv1
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|ic1
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|kv1
operator|.
name|getTagsLength
argument_list|()
argument_list|,
name|ic1
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Verify if ExtendedCell interface is implemented
annotation|@
name|Test
specifier|public
name|void
name|testIfExtendedCellImplemented
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|ic0
operator|instanceof
name|ExtendedCell
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
name|testIllegalRow
parameter_list|()
block|{
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
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
name|testIllegalFamily
parameter_list|()
block|{
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
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
name|testIllegalQualifier
parameter_list|()
block|{
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|6
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
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
name|testIllegalTimestamp
parameter_list|()
block|{
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|6
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|9
argument_list|,
operator|-
literal|100
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
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
name|testIllegalValue
parameter_list|()
block|{
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|6
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|9
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
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
name|testIllegalTags
parameter_list|()
block|{
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|6
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|9
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tags"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteTag
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tags
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"---tags---"
argument_list|)
decl_stmt|;
name|int
name|tagOffset
init|=
literal|3
decl_stmt|;
name|int
name|length
init|=
literal|4
decl_stmt|;
name|IndividualBytesFieldCell
name|cell
init|=
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|6
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|9
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|,
name|tags
argument_list|,
name|tagOffset
argument_list|,
name|length
argument_list|)
decl_stmt|;
try|try
init|(
name|ByteArrayOutputStream
name|output
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|300
argument_list|)
init|)
block|{
name|cell
operator|.
name|write
argument_list|(
name|output
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buf
init|=
name|output
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|cell
operator|.
name|getSerializedSize
argument_list|(
literal|true
argument_list|)
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteValue
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"---value---"
argument_list|)
decl_stmt|;
name|int
name|valueOffset
init|=
literal|3
decl_stmt|;
name|int
name|valueLength
init|=
literal|5
decl_stmt|;
name|IndividualBytesFieldCell
name|cell
init|=
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|6
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|9
argument_list|,
literal|0L
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|0
argument_list|,
name|value
argument_list|,
name|valueOffset
argument_list|,
name|valueLength
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
try|try
init|(
name|ByteArrayOutputStream
name|output
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|300
argument_list|)
init|)
block|{
name|cell
operator|.
name|write
argument_list|(
name|output
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buf
init|=
name|output
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|cell
operator|.
name|getSerializedSize
argument_list|(
literal|true
argument_list|)
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

