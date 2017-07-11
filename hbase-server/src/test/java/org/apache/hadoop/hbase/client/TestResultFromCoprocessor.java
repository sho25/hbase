begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
import|;
end_import

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
name|CoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|TestResultFromCoprocessor
block|{
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
literal|"normal_row"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fm"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|100L
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FIXED_VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fixed_value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Cell
name|FIXED_CELL
init|=
name|CellUtil
operator|.
name|createCell
argument_list|(
name|ROW
argument_list|,
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
literal|0
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|FIXED_VALUE
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Result
name|FIXED_RESULT
init|=
name|Result
operator|.
name|create
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|FIXED_CELL
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestResultFromCoprocessor"
argument_list|)
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
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|addCoprocessor
argument_list|(
name|MyObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
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
name|testAppend
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertRowAndValue
argument_list|(
name|t
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
argument_list|,
name|ROW
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|Append
name|append
init|=
operator|new
name|Append
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|append
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|FIXED_VALUE
argument_list|)
expr_stmt|;
name|assertRowAndValue
argument_list|(
name|t
operator|.
name|append
argument_list|(
name|append
argument_list|)
argument_list|,
name|ROW
argument_list|,
name|FIXED_VALUE
argument_list|)
expr_stmt|;
name|assertRowAndValue
argument_list|(
name|t
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
argument_list|,
name|ROW
argument_list|,
name|Bytes
operator|.
name|add
argument_list|(
name|VALUE
argument_list|,
name|FIXED_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrement
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertRowAndValue
argument_list|(
name|t
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
argument_list|,
name|ROW
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
literal|99
argument_list|)
expr_stmt|;
name|assertRowAndValue
argument_list|(
name|t
operator|.
name|increment
argument_list|(
name|inc
argument_list|)
argument_list|,
name|ROW
argument_list|,
name|FIXED_VALUE
argument_list|)
expr_stmt|;
name|assertRowAndValue
argument_list|(
name|t
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
argument_list|,
name|ROW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|199L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|assertRowAndValue
parameter_list|(
name|Result
name|r
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
for|for
control|(
name|Cell
name|c
range|:
name|r
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|c
argument_list|)
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|c
argument_list|)
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|MyObserver
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Result
name|postAppend
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Append
name|append
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
block|{
return|return
name|FIXED_RESULT
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|postIncrement
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Increment
name|increment
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
block|{
return|return
name|FIXED_RESULT
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
block|}
end_class

end_unit

