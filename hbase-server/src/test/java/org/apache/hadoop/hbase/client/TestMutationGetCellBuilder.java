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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|CellBuilder
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
name|HBaseTestingUtility
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
name|TableName
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
name|MediumTests
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
name|AfterClass
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
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
name|TestMutationGetCellBuilder
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
name|TestMutationGetCellBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMutationGetCellBuilder
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|rowKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"12345678"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|uselessRowKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"123"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|final
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
init|)
block|{
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
comment|// put one row
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|CellBuilder
name|cellBuilder
init|=
name|put
operator|.
name|getCellBuilder
argument_list|()
operator|.
name|setQualifier
argument_list|(
name|qualifier
argument_list|)
operator|.
name|setFamily
argument_list|(
name|family
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|now
argument_list|)
decl_stmt|;
comment|//setRow is useless
name|cellBuilder
operator|.
name|setRow
argument_list|(
name|uselessRowKey
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|cellBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|cloneRow
init|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|cellBuilder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"setRow must be useless"
argument_list|,
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|cloneRow
argument_list|,
name|uselessRowKey
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// get the row back and assert the values
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|get
operator|.
name|setTimestamp
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"row key must be same"
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|,
name|rowKey
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Column foo value should be bar"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
comment|//Delete that row
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|cellBuilder
operator|=
name|delete
operator|.
name|getCellBuilder
argument_list|()
operator|.
name|setQualifier
argument_list|(
name|qualifier
argument_list|)
operator|.
name|setFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
comment|//if this row has been deleted,then can check setType is useless.
name|cellBuilder
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
expr_stmt|;
name|delete
operator|.
name|add
argument_list|(
name|cellBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
comment|//check this row whether exist
name|get
operator|=
operator|new
name|Get
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
name|get
operator|.
name|setTimestamp
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|result
operator|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Column foo should not exist"
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

