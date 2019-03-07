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
name|HBaseRpcController
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
name|HBaseRpcControllerImpl
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
name|testclassification
operator|.
name|RegionServerTests
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
name|protobuf
operator|.
name|ServiceException
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
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|ResponseConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanResponse
import|;
end_import

begin_comment
comment|/**  * Testcase to make sure that we do not close scanners if ScanRequest.numberOfRows is zero. See  * HBASE-18042 for more details.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestScanWithoutFetchingData
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
name|TestScanWithoutFetchingData
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CQ
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COUNT
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
name|RegionInfo
name|HRI
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnectionImpl
name|CONN
decl_stmt|;
specifier|private
specifier|static
name|ClientProtos
operator|.
name|ClientService
operator|.
name|Interface
name|STUB
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|CF
argument_list|)
init|)
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
name|COUNT
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|HRI
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|CONN
operator|=
operator|(
name|AsyncConnectionImpl
operator|)
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|STUB
operator|=
name|CONN
operator|.
name|getRegionServerStub
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|ScanResponse
name|scan
parameter_list|(
name|HBaseRpcController
name|hrc
parameter_list|,
name|ScanRequest
name|req
parameter_list|)
throws|throws
name|IOException
block|{
name|BlockingRpcCallback
argument_list|<
name|ScanResponse
argument_list|>
name|callback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<>
argument_list|()
decl_stmt|;
name|STUB
operator|.
name|scan
argument_list|(
name|hrc
argument_list|,
name|req
argument_list|,
name|callback
argument_list|)
expr_stmt|;
return|return
name|callback
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
name|void
name|assertResult
parameter_list|(
name|int
name|row
parameter_list|,
name|Result
name|result
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|row
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|row
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|ServiceException
throws|,
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ScanRequest
name|req
init|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|HRI
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|scan
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HBaseRpcController
name|hrc
init|=
operator|new
name|HBaseRpcControllerImpl
argument_list|()
decl_stmt|;
name|ScanResponse
name|resp
init|=
name|scan
argument_list|(
name|hrc
argument_list|,
name|req
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResults
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResultsInRegion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|hrc
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|resp
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|long
name|scannerId
init|=
name|resp
operator|.
name|getScannerId
argument_list|()
decl_stmt|;
name|int
name|nextCallSeq
init|=
literal|0
decl_stmt|;
comment|// test normal next
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|COUNT
operator|/
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|req
operator|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|scannerId
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
name|nextCallSeq
operator|++
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|hrc
operator|.
name|reset
argument_list|()
expr_stmt|;
name|resp
operator|=
name|scan
argument_list|(
name|hrc
argument_list|,
name|req
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResults
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResultsInRegion
argument_list|()
argument_list|)
expr_stmt|;
name|Result
index|[]
name|results
init|=
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|hrc
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|resp
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertResult
argument_list|(
name|i
argument_list|,
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|// test zero next
name|req
operator|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|scannerId
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
name|nextCallSeq
operator|++
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|hrc
operator|.
name|reset
argument_list|()
expr_stmt|;
name|resp
operator|=
name|scan
argument_list|(
name|hrc
argument_list|,
name|req
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResults
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResultsInRegion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|hrc
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|resp
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|COUNT
operator|/
literal|2
init|;
name|i
operator|<
name|COUNT
condition|;
name|i
operator|++
control|)
block|{
name|req
operator|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|scannerId
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
name|nextCallSeq
operator|++
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|hrc
operator|.
name|reset
argument_list|()
expr_stmt|;
name|resp
operator|=
name|scan
argument_list|(
name|hrc
argument_list|,
name|req
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|resp
operator|.
name|getMoreResults
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
operator|!=
name|COUNT
operator|-
literal|1
argument_list|,
name|resp
operator|.
name|getMoreResultsInRegion
argument_list|()
argument_list|)
expr_stmt|;
name|Result
index|[]
name|results
init|=
name|ResponseConverter
operator|.
name|getResults
argument_list|(
name|hrc
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|resp
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertResult
argument_list|(
name|i
argument_list|,
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|// close
name|req
operator|=
name|RequestConverter
operator|.
name|buildScanRequest
argument_list|(
name|scannerId
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hrc
operator|.
name|reset
argument_list|()
expr_stmt|;
name|resp
operator|=
name|scan
argument_list|(
name|hrc
argument_list|,
name|req
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

