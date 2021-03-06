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
name|MetaTableAccessor
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
name|LargeTests
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

begin_comment
comment|/**  * Test various scanner timeout issues.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
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
name|TestScannerTimeout
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
name|TestScannerTimeout
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestScannerTimeout
operator|.
name|class
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
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
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
comment|// Be careful w/ what you set this timer to... it can get in the way of
comment|// the mini cluster coming up -- the verification in particular.
specifier|private
specifier|final
specifier|static
name|int
name|THREAD_WAKE_FREQUENCY
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|SCANNER_TIMEOUT
init|=
literal|15000
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|SCANNER_CACHING
init|=
literal|5
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
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|SCANNER_TIMEOUT
argument_list|)
expr_stmt|;
name|c
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
name|THREAD_WAKE_FREQUENCY
argument_list|)
expr_stmt|;
comment|// We need more than one region server for this test
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|SOME_BYTES
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
name|addColumn
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
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|ensureSomeNonStoppedRegionServersAvailable
argument_list|(
literal|2
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
name|LOG
operator|.
name|info
argument_list|(
literal|"START************ test2772"
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Set a very high timeout, we want to test what happens when a RS
comment|// fails but the region is recovered before the lease times out.
comment|// Since the RS is already created, this conf is client-side only for
comment|// this new table
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|SCANNER_TIMEOUT
operator|*
literal|100
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|higherScanTimeoutTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|r
init|=
name|higherScanTimeoutTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
comment|// This takes way less than SCANNER_TIMEOUT*100
name|rs
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
name|higherScanTimeoutTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"END ************ test2772"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that scanner won't miss any rows if the region server it was reading    * from failed. Before 3686, it would skip rows in the scan.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|test3686a
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"START ************ TEST3686A---1"
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"START ************ TEST3686A---1111"
argument_list|)
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
name|SCANNER_CACHING
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"************ TEST3686A"
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|fullScanMetaAndPrint
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set a very high timeout, we want to test what happens when a RS
comment|// fails but the region is recovered before the lease times out.
comment|// Since the RS is already created, this conf is client-side only for
comment|// this new table
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|SCANNER_TIMEOUT
operator|*
literal|100
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"START ************ TEST3686A---22"
argument_list|)
expr_stmt|;
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
name|LOG
operator|.
name|info
argument_list|(
literal|"START ************ TEST3686A---33"
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|1
decl_stmt|;
name|r
operator|.
name|next
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"START ************ TEST3686A---44"
argument_list|)
expr_stmt|;
comment|// Kill after one call to next(), which got 5 rows.
name|rs
operator|.
name|abort
argument_list|(
literal|"die!"
argument_list|)
expr_stmt|;
while|while
condition|(
name|r
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|NB_ROWS
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"************ END TEST3686A"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure that no rows are lost if the scanner timeout is longer on the    * client than the server, and the scan times out on the server but not the    * client.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|test3686b
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"START ************ test3686b"
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
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
name|SCANNER_CACHING
argument_list|)
expr_stmt|;
comment|// Set a very high timeout, we want to test what happens when a RS
comment|// fails but the region is recovered before the lease times out.
comment|// Since the RS is already created, this conf is client-side only for
comment|// this new table
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|SCANNER_TIMEOUT
operator|*
literal|100
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|higherScanTimeoutTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|r
init|=
name|higherScanTimeoutTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|1
decl_stmt|;
name|r
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// Sleep, allowing the scan to timeout on the server but not on the client.
name|Thread
operator|.
name|sleep
argument_list|(
name|SCANNER_TIMEOUT
operator|+
literal|2000
argument_list|)
expr_stmt|;
while|while
condition|(
name|r
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|NB_ROWS
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|higherScanTimeoutTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"END ************ END test3686b"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

