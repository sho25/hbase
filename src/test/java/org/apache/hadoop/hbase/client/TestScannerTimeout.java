begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|After
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
name|Before
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
name|fail
import|;
end_import

begin_comment
comment|/**  * Test various scanner timeout issues.  */
end_comment

begin_class
specifier|public
class|class
name|TestScannerTimeout
block|{
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
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|SOME_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|NB_ROWS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|SCANNER_TIMEOUT
init|=
literal|6000
decl_stmt|;
specifier|private
specifier|static
name|HTable
name|table
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|c
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|c
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.lease.period"
argument_list|,
name|SCANNER_TIMEOUT
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t"
argument_list|)
argument_list|,
name|SOME_BYTES
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NB_ROWS
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|SOME_BYTES
argument_list|,
name|SOME_BYTES
argument_list|,
name|SOME_BYTES
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
comment|/**    * @throws java.lang.Exception    */
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
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that we do get a ScannerTimeoutException    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|test2481
parameter_list|()
throws|throws
name|Exception
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|r
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Result
name|res
init|=
name|r
operator|.
name|next
argument_list|()
decl_stmt|;
while|while
condition|(
name|res
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|count
operator|==
literal|5
condition|)
block|{
comment|// Sleep just a bit more to be sure
name|Thread
operator|.
name|sleep
argument_list|(
name|SCANNER_TIMEOUT
operator|+
literal|100
argument_list|)
expr_stmt|;
block|}
name|res
operator|=
name|r
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ScannerTimeoutException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got the timeout "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|fail
argument_list|(
literal|"We should be timing out"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that scanner can continue even if the region server it was reading    * from failed. Before 2772, it reused the same scanner id.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|test2772
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|rs
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getServerWith
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
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
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|r
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
comment|// This takes exactly 5 seconds
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|rs
argument_list|)
operator|.
name|abort
argument_list|(
literal|"die!"
argument_list|)
expr_stmt|;
name|Result
index|[]
name|results
init|=
name|r
operator|.
name|next
argument_list|(
name|NB_ROWS
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|NB_ROWS
argument_list|,
name|results
operator|.
name|length
argument_list|)
expr_stmt|;
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

