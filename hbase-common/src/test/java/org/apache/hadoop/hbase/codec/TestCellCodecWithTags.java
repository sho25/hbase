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
name|codec
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
name|io
operator|.
name|IOException
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
name|ArrayBackedTag
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
name|HConstants
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
name|PrivateCellUtil
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
name|Tag
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|CountingInputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|CountingOutputStream
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
name|TestCellCodecWithTags
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
name|TestCellCodecWithTags
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCellWithTag
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|CountingOutputStream
name|cos
init|=
operator|new
name|CountingOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|cos
argument_list|)
decl_stmt|;
name|Codec
name|codec
init|=
operator|new
name|CellCodecWithTags
argument_list|()
decl_stmt|;
name|Codec
operator|.
name|Encoder
name|encoder
init|=
name|codec
operator|.
name|getEncoder
argument_list|(
name|dos
argument_list|)
decl_stmt|;
specifier|final
name|Cell
name|cell1
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
operator|new
name|Tag
index|[]
block|{
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
literal|"teststring1"
argument_list|)
argument_list|)
block|,
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
literal|"teststring2"
argument_list|)
argument_list|)
block|}
argument_list|)
decl_stmt|;
specifier|final
name|Cell
name|cell2
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|,
operator|new
name|Tag
index|[]
block|{
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
literal|"teststring3"
argument_list|)
argument_list|)
block|, }
argument_list|)
decl_stmt|;
specifier|final
name|Cell
name|cell3
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|,
operator|new
name|Tag
index|[]
block|{
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
literal|"teststring4"
argument_list|)
argument_list|)
block|,
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
literal|"teststring5"
argument_list|)
argument_list|)
block|,
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
literal|"teststring6"
argument_list|)
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|write
argument_list|(
name|cell1
argument_list|)
expr_stmt|;
name|encoder
operator|.
name|write
argument_list|(
name|cell2
argument_list|)
expr_stmt|;
name|encoder
operator|.
name|write
argument_list|(
name|cell3
argument_list|)
expr_stmt|;
name|encoder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|dos
operator|.
name|close
argument_list|()
expr_stmt|;
name|long
name|offset
init|=
name|cos
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|CountingInputStream
name|cis
init|=
operator|new
name|CountingInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|cis
argument_list|)
decl_stmt|;
name|Codec
operator|.
name|Decoder
name|decoder
init|=
name|codec
operator|.
name|getDecoder
argument_list|(
name|dis
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|decoder
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|c
init|=
name|decoder
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|equals
argument_list|(
name|c
argument_list|,
name|cell1
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|PrivateCellUtil
operator|.
name|getTags
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Tag
name|tag
init|=
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"teststring1"
argument_list|)
argument_list|,
name|Tag
operator|.
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tag
operator|=
name|tags
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"teststring2"
argument_list|)
argument_list|,
name|Tag
operator|.
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|decoder
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|c
operator|=
name|decoder
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|equals
argument_list|(
name|c
argument_list|,
name|cell2
argument_list|)
argument_list|)
expr_stmt|;
name|tags
operator|=
name|PrivateCellUtil
operator|.
name|getTags
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tag
operator|=
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"teststring3"
argument_list|)
argument_list|,
name|Tag
operator|.
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|decoder
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|c
operator|=
name|decoder
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|equals
argument_list|(
name|c
argument_list|,
name|cell3
argument_list|)
argument_list|)
expr_stmt|;
name|tags
operator|=
name|PrivateCellUtil
operator|.
name|getTags
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tag
operator|=
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"teststring4"
argument_list|)
argument_list|,
name|Tag
operator|.
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tag
operator|=
name|tags
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"teststring5"
argument_list|)
argument_list|,
name|Tag
operator|.
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tag
operator|=
name|tags
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tag
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"teststring6"
argument_list|)
argument_list|,
name|Tag
operator|.
name|cloneValue
argument_list|(
name|tag
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|decoder
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|offset
argument_list|,
name|cis
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

