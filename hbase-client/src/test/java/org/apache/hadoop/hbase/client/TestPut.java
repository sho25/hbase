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
name|client
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
name|assertNotEquals
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
name|Cell
operator|.
name|DataType
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|ClientTests
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
name|SmallTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestPut
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCopyConstructor
parameter_list|()
block|{
name|Put
name|origin
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ROW-01"
argument_list|)
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
literal|"CF-01"
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
literal|"Q-01"
argument_list|)
decl_stmt|;
name|origin
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"V-01"
argument_list|)
argument_list|)
expr_stmt|;
name|Put
name|clone
init|=
operator|new
name|Put
argument_list|(
name|origin
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|origin
operator|.
name|getCellList
argument_list|(
name|family
argument_list|)
argument_list|,
name|clone
operator|.
name|getCellList
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|origin
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"V-02"
argument_list|)
argument_list|)
expr_stmt|;
comment|//They should have different cell lists
name|assertNotEquals
argument_list|(
name|origin
operator|.
name|getCellList
argument_list|(
name|family
argument_list|)
argument_list|,
name|clone
operator|.
name|getCellList
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// HBASE-14881
annotation|@
name|Test
specifier|public
name|void
name|testRowIsImmutableOrNot
parameter_list|()
block|{
name|byte
index|[]
name|rowKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable"
argument_list|)
decl_stmt|;
comment|// Test when row key is immutable
name|Put
name|putRowIsImmutable
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rowKey
operator|==
name|putRowIsImmutable
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// No local copy is made
comment|// Test when row key is not immutable
name|Put
name|putRowIsNotImmutable
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|,
literal|1000L
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rowKey
operator|!=
name|putRowIsNotImmutable
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// A local copy is made
block|}
comment|// HBASE-14882
annotation|@
name|Test
specifier|public
name|void
name|testAddImmutable
parameter_list|()
throws|throws
name|IOException
block|{
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
name|qualifier0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-qualifier-0"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-value-0"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"immutable-qualifier-1"
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
literal|"immutable-value-1"
argument_list|)
decl_stmt|;
name|long
name|ts1
init|=
literal|5000L
decl_stmt|;
comment|// "true" indicates that the input row is immutable
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setFamily
argument_list|(
name|family
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|qualifier0
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|put
operator|.
name|getTimeStamp
argument_list|()
argument_list|)
operator|.
name|setType
argument_list|(
name|DataType
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|value0
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setFamily
argument_list|(
name|family
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|qualifier1
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|ts1
argument_list|)
operator|.
name|setType
argument_list|(
name|DataType
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|value1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify the cell of family:qualifier0
name|Cell
name|cell0
init|=
name|put
operator|.
name|get
argument_list|(
name|family
argument_list|,
name|qualifier0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Verify no local copy is made for family, qualifier or value
name|assertTrue
argument_list|(
name|cell0
operator|.
name|getFamilyArray
argument_list|()
operator|==
name|family
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cell0
operator|.
name|getQualifierArray
argument_list|()
operator|==
name|qualifier0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cell0
operator|.
name|getValueArray
argument_list|()
operator|==
name|value0
argument_list|)
expr_stmt|;
comment|// Verify timestamp
name|assertTrue
argument_list|(
name|cell0
operator|.
name|getTimestamp
argument_list|()
operator|==
name|put
operator|.
name|getTimeStamp
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify the cell of family:qualifier1
name|Cell
name|cell1
init|=
name|put
operator|.
name|get
argument_list|(
name|family
argument_list|,
name|qualifier1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Verify no local copy is made for family, qualifier or value
name|assertTrue
argument_list|(
name|cell1
operator|.
name|getFamilyArray
argument_list|()
operator|==
name|family
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cell1
operator|.
name|getQualifierArray
argument_list|()
operator|==
name|qualifier1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cell1
operator|.
name|getValueArray
argument_list|()
operator|==
name|value1
argument_list|)
expr_stmt|;
comment|// Verify timestamp
name|assertTrue
argument_list|(
name|cell1
operator|.
name|getTimestamp
argument_list|()
operator|==
name|ts1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

