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
name|assertFalse
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
name|hbase
operator|.
name|CompatibilityFactory
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
name|ipc
operator|.
name|MetricsHBaseServerSource
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
name|test
operator|.
name|MetricsAssertHelper
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestLeaseRenewal
block|{
specifier|public
name|MetricsAssertHelper
name|HELPER
init|=
name|CompatibilityFactory
operator|.
name|getInstance
argument_list|(
name|MetricsAssertHelper
operator|.
name|class
argument_list|)
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
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
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
name|ANOTHERROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"anotherrow"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|COL_QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|VAL_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|ROW_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|leaseTimeout
init|=
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
operator|/
literal|4
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|leaseTimeout
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
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
comment|// Nothing to do.
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|HTableDescriptor
name|htd
range|:
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listTables
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Tear down, remove table="
operator|+
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLeaseRenewal
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testLeaseRenewal"
argument_list|)
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW_BYTES
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COL_QUAL
argument_list|,
name|VAL_BYTES
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ANOTHERROW
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COL_QUAL
argument_list|,
name|VAL_BYTES
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ResultScanner
name|rs
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// make sure that calling renewLease does not impact the scan results
name|assertTrue
argument_list|(
name|rs
operator|.
name|renewLease
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|rs
operator|.
name|next
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|,
name|ANOTHERROW
argument_list|)
argument_list|)
expr_stmt|;
comment|// renew the lease a few times, long enough to be sure
comment|// the lease would have expired otherwise
name|Thread
operator|.
name|sleep
argument_list|(
name|leaseTimeout
operator|/
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|renewLease
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|leaseTimeout
operator|/
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|renewLease
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|leaseTimeout
operator|/
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|renewLease
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure we haven't advanced the scanner
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|rs
operator|.
name|next
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|,
name|ROW_BYTES
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|renewLease
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure scanner is exhausted now
name|assertNull
argument_list|(
name|rs
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
comment|// renewLease should return false now
name|assertFalse
argument_list|(
name|rs
operator|.
name|renewLease
argument_list|()
argument_list|)
expr_stmt|;
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|MetricsHBaseServerSource
name|serverSource
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRpcServer
argument_list|()
operator|.
name|getMetrics
argument_list|()
operator|.
name|getMetricsSource
argument_list|()
decl_stmt|;
name|HELPER
operator|.
name|assertCounter
argument_list|(
literal|"exceptions.OutOfOrderScannerNextException"
argument_list|,
literal|0
argument_list|,
name|serverSource
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

