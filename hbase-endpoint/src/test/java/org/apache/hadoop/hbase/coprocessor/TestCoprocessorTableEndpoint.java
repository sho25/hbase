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
name|coprocessor
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
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|Map
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|Table
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
name|client
operator|.
name|coprocessor
operator|.
name|Batch
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
name|protobuf
operator|.
name|generated
operator|.
name|ColumnAggregationProtos
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
name|ipc
operator|.
name|CoprocessorRpcUtils
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
name|CoprocessorTests
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
name|CoprocessorTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCoprocessorTableEndpoint
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
name|TestCoprocessorTableEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestQualifier"
argument_list|)
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
literal|"testRow"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROWSIZE
init|=
literal|20
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|rowSeperator1
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|rowSeperator2
init|=
literal|12
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|ROWS
init|=
name|makeN
argument_list|(
name|ROW
argument_list|,
name|ROWSIZE
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
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
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
name|testCoprocessorTableEndpoint
parameter_list|()
throws|throws
name|Throwable
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
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addCoprocessor
argument_list|(
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
name|ColumnAggregationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|verifyTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDynamicCoprocessorTableEndpoint
parameter_list|()
throws|throws
name|Throwable
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
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addCoprocessor
argument_list|(
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
name|ColumnAggregationEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|updateTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|verifyTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|makeN
parameter_list|(
name|byte
index|[]
name|base
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|ret
init|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|base
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%02d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|sum
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|start
parameter_list|,
specifier|final
name|byte
index|[]
name|end
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
return|return
name|table
operator|.
name|coprocessorService
argument_list|(
name|ColumnAggregationProtos
operator|.
name|ColumnAggregationService
operator|.
name|class
argument_list|,
name|start
argument_list|,
name|end
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|ColumnAggregationProtos
operator|.
name|ColumnAggregationService
argument_list|,
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|(
name|ColumnAggregationProtos
operator|.
name|ColumnAggregationService
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
name|CoprocessorRpcUtils
operator|.
name|BlockingRpcCallback
argument_list|<
name|ColumnAggregationProtos
operator|.
name|SumResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|CoprocessorRpcUtils
operator|.
name|BlockingRpcCallback
argument_list|<>
argument_list|()
decl_stmt|;
name|ColumnAggregationProtos
operator|.
name|SumRequest
operator|.
name|Builder
name|builder
init|=
name|ColumnAggregationProtos
operator|.
name|SumRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setFamily
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|qualifier
operator|!=
literal|null
operator|&&
name|qualifier
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|setQualifier
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|instance
operator|.
name|sum
argument_list|(
literal|null
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
return|return
name|rpcCallback
operator|.
name|get
argument_list|()
operator|.
name|getSum
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|ROWS
index|[
name|rowSeperator1
index|]
block|,
name|ROWS
index|[
name|rowSeperator2
index|]
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
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
name|ROWSIZE
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROWS
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|TEST_FAMILY
argument_list|,
name|TEST_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
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
block|}
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|updateTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|desc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|void
name|verifyTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Throwable
block|{
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|results
init|=
name|sum
argument_list|(
name|table
argument_list|,
name|TEST_FAMILY
argument_list|,
name|TEST_QUALIFIER
argument_list|,
name|ROWS
index|[
literal|0
index|]
argument_list|,
name|ROWS
index|[
name|ROWS
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
decl_stmt|;
name|int
name|sumResult
init|=
literal|0
decl_stmt|;
name|int
name|expectedResult
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sumResult
operator|+=
name|e
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ROWSIZE
condition|;
name|i
operator|++
control|)
block|{
name|expectedResult
operator|+=
name|i
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Invalid result"
argument_list|,
name|expectedResult
argument_list|,
name|sumResult
argument_list|)
expr_stmt|;
comment|// scan: for region 2 and region 3
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
name|results
operator|=
name|sum
argument_list|(
name|table
argument_list|,
name|TEST_FAMILY
argument_list|,
name|TEST_QUALIFIER
argument_list|,
name|ROWS
index|[
name|rowSeperator1
index|]
argument_list|,
name|ROWS
index|[
name|ROWS
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|sumResult
operator|=
literal|0
expr_stmt|;
name|expectedResult
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sumResult
operator|+=
name|e
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|rowSeperator1
init|;
name|i
operator|<
name|ROWSIZE
condition|;
name|i
operator|++
control|)
block|{
name|expectedResult
operator|+=
name|i
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Invalid result"
argument_list|,
name|expectedResult
argument_list|,
name|sumResult
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

