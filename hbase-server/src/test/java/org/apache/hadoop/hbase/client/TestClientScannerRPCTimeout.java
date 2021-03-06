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
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterRegionServer
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
name|regionserver
operator|.
name|HRegionServer
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
name|regionserver
operator|.
name|RSRpcServices
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|RpcController
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
comment|/**  * Test the scenario where a HRegionServer#scan() call, while scanning, timeout at client side and  * getting retried. This scenario should not result in some data being skipped at RS side.  */
end_comment

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
name|TestClientScannerRPCTimeout
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
name|TestClientScannerRPCTimeout
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestClientScannerRPCTimeout
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
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
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
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
literal|"testValue"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|rpcTimeout
init|=
literal|2
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|CLIENT_RETRIES_NUMBER
init|=
literal|3
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Don't report so often so easier to see other rpcs
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|3
operator|*
literal|10000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|rpcTimeout
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|RegionServerWithScanTimeout
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
name|CLIENT_RETRIES_NUMBER
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_PAUSE
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|testScannerNextRPCTimesout
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
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|r0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-0"
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
literal|"row-1"
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
literal|"row-2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|r3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-3"
argument_list|)
decl_stmt|;
name|putToTable
argument_list|(
name|ht
argument_list|,
name|r0
argument_list|)
expr_stmt|;
name|putToTable
argument_list|(
name|ht
argument_list|,
name|r1
argument_list|)
expr_stmt|;
name|putToTable
argument_list|(
name|ht
argument_list|,
name|r2
argument_list|)
expr_stmt|;
name|putToTable
argument_list|(
name|ht
argument_list|,
name|r3
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Wrote our three values"
argument_list|)
expr_stmt|;
name|RSRpcServicesWithScanTimeout
operator|.
name|seqNoToSleepOn
operator|=
literal|1
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|ht
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// fetched when openScanner
name|assertTrue
argument_list|(
literal|"Expected row: row-0"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|r0
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected row: row-1"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|r1
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Got expected first row"
argument_list|)
expr_stmt|;
name|long
name|t1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|t1
operator|)
operator|>
name|rpcTimeout
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected row: row-2"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|r2
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|RSRpcServicesWithScanTimeout
operator|.
name|seqNoToSleepOn
operator|=
operator|-
literal|1
expr_stmt|;
comment|// No need of sleep
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected row: row-3"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|r3
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// test the case that RPC is always timesout
name|scanner
operator|=
name|ht
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|RSRpcServicesWithScanTimeout
operator|.
name|sleepAlways
operator|=
literal|true
expr_stmt|;
name|RSRpcServicesWithScanTimeout
operator|.
name|tryNumber
operator|=
literal|0
expr_stmt|;
try|try
block|{
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// catch the exception after max retry number
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed after maximal attempts="
operator|+
name|CLIENT_RETRIES_NUMBER
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Expected maximal try number="
operator|+
name|CLIENT_RETRIES_NUMBER
operator|+
literal|", actual ="
operator|+
name|RSRpcServicesWithScanTimeout
operator|.
name|tryNumber
argument_list|,
name|RSRpcServicesWithScanTimeout
operator|.
name|tryNumber
operator|<=
name|CLIENT_RETRIES_NUMBER
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|putToTable
parameter_list|(
name|Table
name|ht
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowkey
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|RegionServerWithScanTimeout
extends|extends
name|MiniHBaseClusterRegionServer
block|{
specifier|public
name|RegionServerWithScanTimeout
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RSRpcServices
name|createRpcServices
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|RSRpcServicesWithScanTimeout
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|RSRpcServicesWithScanTimeout
extends|extends
name|RSRpcServices
block|{
specifier|private
name|long
name|tableScannerId
decl_stmt|;
specifier|private
name|boolean
name|slept
decl_stmt|;
specifier|private
specifier|static
name|long
name|seqNoToSleepOn
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|sleepAlways
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|int
name|tryNumber
init|=
literal|0
decl_stmt|;
specifier|public
name|RSRpcServicesWithScanTimeout
parameter_list|(
name|HRegionServer
name|rs
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|rs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ScanResponse
name|scan
parameter_list|(
specifier|final
name|RpcController
name|controller
parameter_list|,
specifier|final
name|ScanRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
if|if
condition|(
name|request
operator|.
name|hasScannerId
argument_list|()
condition|)
block|{
name|ScanResponse
name|scanResponse
init|=
name|super
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|tableScannerId
operator|==
name|request
operator|.
name|getScannerId
argument_list|()
operator|&&
operator|(
name|sleepAlways
operator|||
operator|(
operator|!
name|slept
operator|&&
name|seqNoToSleepOn
operator|==
name|request
operator|.
name|getNextCallSeq
argument_list|()
operator|)
operator|)
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SLEEPING "
operator|+
operator|(
name|rpcTimeout
operator|+
literal|500
operator|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|rpcTimeout
operator|+
literal|500
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{           }
name|slept
operator|=
literal|true
expr_stmt|;
name|tryNumber
operator|++
expr_stmt|;
if|if
condition|(
name|tryNumber
operator|>
literal|2
operator|*
name|CLIENT_RETRIES_NUMBER
condition|)
block|{
name|sleepAlways
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
name|scanResponse
return|;
block|}
else|else
block|{
name|ScanResponse
name|scanRes
init|=
name|super
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|String
name|regionName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|regionName
operator|.
name|contains
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|tableScannerId
operator|=
name|scanRes
operator|.
name|getScannerId
argument_list|()
expr_stmt|;
block|}
return|return
name|scanRes
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

