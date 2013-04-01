begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RpcController
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
name|Collections
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
name|java
operator|.
name|util
operator|.
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|HRegionInfo
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
name|ServerName
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
name|HBaseAdmin
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
name|HTable
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
name|BlockingRpcCallback
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
name|CoprocessorRpcChannel
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
name|ServerRpcController
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
name|protobuf
operator|.
name|generated
operator|.
name|TestProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|TestRpcServiceProtos
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
name|assertNotNull
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
name|assertNull
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_comment
comment|/**  * TestEndpoint: test cases to verify coprocessor Endpoint  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCoprocessorEndpoint
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCoprocessorEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestTable"
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
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
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
comment|// set configure to indicate which cp should be loaded
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
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
argument_list|,
name|ProtobufCoprocessorService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|ProtobufCoprocessorService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
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
name|util
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TEST_TABLE
argument_list|)
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
name|add
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
name|table
operator|.
name|close
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
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
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
name|HTable
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
name|BlockingRpcCallback
argument_list|<
name|ColumnAggregationProtos
operator|.
name|SumResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|ColumnAggregationProtos
operator|.
name|SumResponse
argument_list|>
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
annotation|@
name|Test
specifier|public
name|void
name|testAggregation
parameter_list|()
throws|throws
name|Throwable
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Got value "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|" for region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// scan: for region 2 and region 3
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Got value "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|" for region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCoprocessorService
parameter_list|()
throws|throws
name|Throwable
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
init|=
name|table
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
specifier|final
name|TestProtos
operator|.
name|EchoRequestProto
name|request
init|=
name|TestProtos
operator|.
name|EchoRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
literal|"hello"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
comment|// scan: for all regions
specifier|final
name|RpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|table
operator|.
name|coprocessorService
argument_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|class
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
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
argument_list|,
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
block|{
specifier|public
name|TestProtos
operator|.
name|EchoResponseProto
name|call
parameter_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Default response is "
operator|+
name|TestProtos
operator|.
name|EchoRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|BlockingRpcCallback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
decl_stmt|;
name|instance
operator|.
name|echo
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|callback
argument_list|)
expr_stmt|;
name|TestProtos
operator|.
name|EchoResponseProto
name|response
init|=
name|callback
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Batch.Call returning result "
operator|+
name|response
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
block|}
argument_list|,
operator|new
name|Batch
operator|.
name|Callback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
block|{
specifier|public
name|void
name|update
parameter_list|(
name|byte
index|[]
name|region
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|TestProtos
operator|.
name|EchoResponseProto
name|result
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|result
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|region
argument_list|,
name|result
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
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
name|String
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got value "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|" for region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|info
range|:
name|regions
operator|.
name|navigableKeySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region info is "
operator|+
name|info
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|results
operator|.
name|containsKey
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// scan: for region 2 and region 3
name|table
operator|.
name|coprocessorService
argument_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|class
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
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
argument_list|,
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
block|{
specifier|public
name|TestProtos
operator|.
name|EchoResponseProto
name|call
parameter_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Default response is "
operator|+
name|TestProtos
operator|.
name|EchoRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|BlockingRpcCallback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
decl_stmt|;
name|instance
operator|.
name|echo
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|callback
argument_list|)
expr_stmt|;
name|TestProtos
operator|.
name|EchoResponseProto
name|response
init|=
name|callback
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Batch.Call returning result "
operator|+
name|response
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
block|}
argument_list|,
operator|new
name|Batch
operator|.
name|Callback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
block|{
specifier|public
name|void
name|update
parameter_list|(
name|byte
index|[]
name|region
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|TestProtos
operator|.
name|EchoResponseProto
name|result
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|result
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|region
argument_list|,
name|result
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
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
name|String
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got value "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|" for region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|results
operator|.
name|size
argument_list|()
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
annotation|@
name|Test
specifier|public
name|void
name|testCoprocessorServiceNullResponse
parameter_list|()
throws|throws
name|Throwable
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
init|=
name|table
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
specifier|final
name|TestProtos
operator|.
name|EchoRequestProto
name|request
init|=
name|TestProtos
operator|.
name|EchoRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
literal|"hello"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
comment|// scan: for all regions
specifier|final
name|RpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
comment|// test that null results are supported
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|class
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
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
name|BlockingRpcCallback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|TestProtos
operator|.
name|EchoResponseProto
argument_list|>
argument_list|()
decl_stmt|;
name|instance
operator|.
name|echo
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|callback
argument_list|)
expr_stmt|;
name|TestProtos
operator|.
name|EchoResponseProto
name|response
init|=
name|callback
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Batch.Call got result "
operator|+
name|response
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
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
name|String
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got value "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|" for region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|info
range|:
name|regions
operator|.
name|navigableKeySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region info is "
operator|+
name|info
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|results
operator|.
name|containsKey
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
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
annotation|@
name|Test
specifier|public
name|void
name|testMasterCoprocessorService
parameter_list|()
throws|throws
name|Throwable
block|{
name|HBaseAdmin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
specifier|final
name|TestProtos
operator|.
name|EchoRequestProto
name|request
init|=
name|TestProtos
operator|.
name|EchoRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
literal|"hello"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
name|service
init|=
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|newBlockingStub
argument_list|(
name|admin
operator|.
name|coprocessorService
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|service
operator|.
name|echo
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCoprocessorError
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Make it not retry forever
name|configuration
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|configuration
argument_list|,
name|TEST_TABLE
argument_list|)
decl_stmt|;
try|try
block|{
name|CoprocessorRpcChannel
name|protocol
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
name|service
init|=
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|newBlockingStub
argument_list|(
name|protocol
argument_list|)
decl_stmt|;
name|service
operator|.
name|error
argument_list|(
literal|null
argument_list|,
name|TestProtos
operator|.
name|EmptyRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{     }
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterCoprocessorError
parameter_list|()
throws|throws
name|Throwable
block|{
name|HBaseAdmin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
name|service
init|=
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|newBlockingStub
argument_list|(
name|admin
operator|.
name|coprocessorService
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|error
argument_list|(
literal|null
argument_list|,
name|TestProtos
operator|.
name|EmptyRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{     }
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
block|}
end_class

end_unit

