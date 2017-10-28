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
name|TestComparators
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCellFieldsCompare
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|r0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row0"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|r1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|r2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|f
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|q1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|q2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|q3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
argument_list|)
decl_stmt|;
name|long
name|l1
init|=
literal|1234L
decl_stmt|;
name|byte
index|[]
name|v1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|l1
argument_list|)
decl_stmt|;
name|long
name|l2
init|=
literal|2000L
decl_stmt|;
name|byte
index|[]
name|v2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|l2
argument_list|)
decl_stmt|;
comment|// Row compare
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|r1
argument_list|,
name|f
argument_list|,
name|q1
argument_list|,
name|v1
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
decl_stmt|;
name|Cell
name|bbCell
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|comparable
init|=
operator|new
name|BinaryComparator
argument_list|(
name|r1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareRow
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareRow
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|r0
argument_list|,
name|f
argument_list|,
name|q1
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|bbCell
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareRow
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareRow
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|r2
argument_list|,
name|f
argument_list|,
name|q1
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|bbCell
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareRow
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareRow
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
comment|// Qualifier compare
name|comparable
operator|=
operator|new
name|BinaryPrefixComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareQualifier
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareQualifier
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|r2
argument_list|,
name|f
argument_list|,
name|q2
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|bbCell
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareQualifier
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareQualifier
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|r2
argument_list|,
name|f
argument_list|,
name|q3
argument_list|,
name|v1
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|bbCell
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareQualifier
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareQualifier
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
comment|// Value compare
name|comparable
operator|=
operator|new
name|LongComparator
argument_list|(
name|l1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareValue
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareValue
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|r1
argument_list|,
name|f
argument_list|,
name|q1
argument_list|,
name|v2
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|bbCell
operator|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareValue
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|compareValue
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
comment|// Family compare
name|comparable
operator|=
operator|new
name|SubstringComparator
argument_list|(
literal|"cf"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareFamily
argument_list|(
name|bbCell
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|PrivateCellUtil
operator|.
name|compareFamily
argument_list|(
name|kv
argument_list|,
name|comparable
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

