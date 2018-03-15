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
name|master
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
name|fail
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
name|ColumnFamilyDescriptorBuilder
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
name|CoprocessorDescriptorBuilder
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|MasterTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestAssignmentManagerMetrics
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
name|TestAssignmentManagerMetrics
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
name|TestAssignmentManagerMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MetricsAssertHelper
name|METRICS_HELPER
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
specifier|private
specifier|static
name|MiniHBaseCluster
name|CLUSTER
decl_stmt|;
specifier|private
specifier|static
name|HMaster
name|MASTER
decl_stmt|;
specifier|private
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
name|int
name|MSG_INTERVAL
init|=
literal|1000
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
name|startCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster"
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Disable sanity check for coprocessor
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.table.sanity.checks"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// set RIT stuck warning threshold to a small value
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|METRICS_RIT_STUCK_WARNING_THRESHOLD
argument_list|,
literal|20
argument_list|)
expr_stmt|;
comment|// set msgInterval to 1 second
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
name|MSG_INTERVAL
argument_list|)
expr_stmt|;
comment|// set tablesOnMaster to none
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.balancer.tablesOnMaster"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
comment|// set client sync wait timeout to 5sec
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.sync.wait.timeout.msec"
argument_list|,
literal|5000
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
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
literal|2500
argument_list|)
expr_stmt|;
comment|// set a small interval for updating rit metrics
name|conf
operator|.
name|setInt
argument_list|(
name|AssignmentManager
operator|.
name|RIT_CHORE_INTERVAL_MSEC_CONF_KEY
argument_list|,
name|MSG_INTERVAL
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|CLUSTER
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|MASTER
operator|=
name|CLUSTER
operator|.
name|getMaster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
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
name|testRITAssignmentManagerMetrics
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|TABLENAME
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
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
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
name|TABLENAME
argument_list|,
name|FAMILY
argument_list|)
init|)
block|{
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
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
literal|"qualifier"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Sleep 3 seconds, wait for doMetrics chore catching up
name|Thread
operator|.
name|sleep
argument_list|(
name|MSG_INTERVAL
operator|*
literal|3
argument_list|)
expr_stmt|;
comment|// check the RIT is 0
name|MetricsAssignmentManagerSource
name|amSource
init|=
name|MASTER
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getAssignmentManagerMetrics
argument_list|()
operator|.
name|getMetricsProcSource
argument_list|()
decl_stmt|;
name|METRICS_HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsAssignmentManagerSource
operator|.
name|RIT_COUNT_NAME
argument_list|,
literal|0
argument_list|,
name|amSource
argument_list|)
expr_stmt|;
name|METRICS_HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsAssignmentManagerSource
operator|.
name|RIT_COUNT_OVER_THRESHOLD_NAME
argument_list|,
literal|0
argument_list|,
name|amSource
argument_list|)
expr_stmt|;
comment|// alter table with a non-existing coprocessor
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLENAME
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
operator|.
name|setCoprocessor
argument_list|(
name|CoprocessorDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
literal|"com.foo.FooRegionObserver"
argument_list|)
operator|.
name|setJarPath
argument_list|(
literal|"hdfs:///foo.jar"
argument_list|)
operator|.
name|setPriority
argument_list|(
literal|1001
argument_list|)
operator|.
name|setProperty
argument_list|(
literal|"arg1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setProperty
argument_list|(
literal|"arg2"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|modifyTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected region failed to open"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// expected, the RS will crash and the assignment will spin forever waiting for a RS
comment|// to assign the region. the region will not go to FAILED_OPEN because in this case
comment|// we have just one RS and it will do one retry.
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// Sleep 3 seconds, wait for doMetrics chore catching up
name|Thread
operator|.
name|sleep
argument_list|(
name|MSG_INTERVAL
operator|*
literal|3
argument_list|)
expr_stmt|;
name|METRICS_HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsAssignmentManagerSource
operator|.
name|RIT_COUNT_NAME
argument_list|,
literal|1
argument_list|,
name|amSource
argument_list|)
expr_stmt|;
name|METRICS_HELPER
operator|.
name|assertGauge
argument_list|(
name|MetricsAssignmentManagerSource
operator|.
name|RIT_COUNT_OVER_THRESHOLD_NAME
argument_list|,
literal|1
argument_list|,
name|amSource
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

