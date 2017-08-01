begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|assertFalse
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|Map
operator|.
name|Entry
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
name|Waiter
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
name|Connection
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
name|Result
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
name|ResultScanner
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
name|master
operator|.
name|HMaster
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
name|Before
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

begin_comment
comment|/**  * A test case to verify that region reports are expired when they are not sent.  */
end_comment

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
name|TestQuotaObserverChoreRegionReports
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
name|TestQuotaObserverChoreRegionReports
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
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
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
comment|// Increase the frequency of some of the chores for responsiveness of the test
name|SpaceQuotaHelperForTests
operator|.
name|updateConfigForQuotas
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|QuotaObserverChore
operator|.
name|REGION_REPORT_RETENTION_DURATION_KEY
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
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
name|testReportExpiration
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
comment|// Send reports every 30 seconds
name|conf
operator|.
name|setInt
argument_list|(
name|FileSystemUtilizationChore
operator|.
name|FS_UTILIZATION_CHORE_PERIOD_KEY
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// Expire the reports after 5 seconds
name|conf
operator|.
name|setInt
argument_list|(
name|QuotaObserverChore
operator|.
name|REGION_REPORT_RETENTION_DURATION_KEY
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|String
name|FAM1
init|=
literal|"f1"
decl_stmt|;
specifier|final
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// Wait for the master to finish initialization.
while|while
condition|(
name|master
operator|.
name|getMasterQuotaManager
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"MasterQuotaManager is null, waiting..."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
specifier|final
name|MasterQuotaManager
name|quotaManager
init|=
name|master
operator|.
name|getMasterQuotaManager
argument_list|()
decl_stmt|;
comment|// Create a table
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"reportExpiration"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAM1
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
comment|// No reports right after we created this table.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|getRegionReportsForTable
argument_list|(
name|quotaManager
operator|.
name|snapshotRegionSizes
argument_list|()
argument_list|,
name|tn
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set a quota
specifier|final
name|long
name|sizeLimit
init|=
literal|100L
operator|*
name|SpaceQuotaHelperForTests
operator|.
name|ONE_MEGABYTE
decl_stmt|;
specifier|final
name|SpaceViolationPolicy
name|violationPolicy
init|=
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
decl_stmt|;
name|QuotaSettings
name|settings
init|=
name|QuotaSettingsFactory
operator|.
name|limitTableSpace
argument_list|(
name|tn
argument_list|,
name|sizeLimit
argument_list|,
name|violationPolicy
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|setQuota
argument_list|(
name|settings
argument_list|)
expr_stmt|;
comment|// We should get one report for the one region we have.
name|Waiter
operator|.
name|waitFor
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|45000
argument_list|,
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numReports
init|=
name|getRegionReportsForTable
argument_list|(
name|quotaManager
operator|.
name|snapshotRegionSizes
argument_list|()
argument_list|,
name|tn
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Saw "
operator|+
name|numReports
operator|+
literal|" reports for "
operator|+
name|tn
operator|+
literal|" while waiting for 1"
argument_list|)
expr_stmt|;
return|return
name|numReports
operator|==
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// We should then see no reports for the single region
name|Waiter
operator|.
name|waitFor
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|15000
argument_list|,
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numReports
init|=
name|getRegionReportsForTable
argument_list|(
name|quotaManager
operator|.
name|snapshotRegionSizes
argument_list|()
argument_list|,
name|tn
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Saw "
operator|+
name|numReports
operator|+
literal|" reports for "
operator|+
name|tn
operator|+
literal|" while waiting for none"
argument_list|)
expr_stmt|;
return|return
name|numReports
operator|==
literal|0
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMissingReportsRemovesQuota
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
comment|// Expire the reports after 5 seconds
name|conf
operator|.
name|setInt
argument_list|(
name|QuotaObserverChore
operator|.
name|REGION_REPORT_RETENTION_DURATION_KEY
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|String
name|FAM1
init|=
literal|"f1"
decl_stmt|;
comment|// Create a table
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"quotaAcceptanceWithoutReports"
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAM1
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
comment|// Set a quota
specifier|final
name|long
name|sizeLimit
init|=
literal|1L
operator|*
name|SpaceQuotaHelperForTests
operator|.
name|ONE_KILOBYTE
decl_stmt|;
specifier|final
name|SpaceViolationPolicy
name|violationPolicy
init|=
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
decl_stmt|;
name|QuotaSettings
name|settings
init|=
name|QuotaSettingsFactory
operator|.
name|limitTableSpace
argument_list|(
name|tn
argument_list|,
name|sizeLimit
argument_list|,
name|violationPolicy
argument_list|)
decl_stmt|;
specifier|final
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
name|setQuota
argument_list|(
name|settings
argument_list|)
expr_stmt|;
specifier|final
name|Connection
name|conn
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
decl_stmt|;
comment|// Write enough data to invalidate the quota
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|bytes
argument_list|,
operator|(
name|byte
operator|)
literal|2
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
literal|200
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAM1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
operator|+
name|i
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
block|}
name|conn
operator|.
name|getTable
argument_list|(
name|tn
argument_list|)
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tn
argument_list|)
expr_stmt|;
comment|// Wait for the table to move into violation
name|Waiter
operator|.
name|waitFor
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|30000
argument_list|,
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|SpaceQuotaSnapshot
name|snapshot
init|=
name|getSnapshotForTable
argument_list|(
name|conn
argument_list|,
name|tn
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshot
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|snapshot
operator|.
name|getQuotaStatus
argument_list|()
operator|.
name|isInViolation
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Close the region, prevent the server from sending new status reports.
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|admin
operator|.
name|unassign
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// We should see this table move out of violation after the report expires.
name|Waiter
operator|.
name|waitFor
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|30000
argument_list|,
literal|1000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|SpaceQuotaSnapshot
name|snapshot
init|=
name|getSnapshotForTable
argument_list|(
name|conn
argument_list|,
name|tn
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshot
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|!
name|snapshot
operator|.
name|getQuotaStatus
argument_list|()
operator|.
name|isInViolation
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// The QuotaObserverChore's memory should also show it not in violation.
specifier|final
name|HMaster
name|master
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|QuotaSnapshotStore
argument_list|<
name|TableName
argument_list|>
name|tableStore
init|=
name|master
operator|.
name|getQuotaObserverChore
argument_list|()
operator|.
name|getTableSnapshotStore
argument_list|()
decl_stmt|;
name|SpaceQuotaSnapshot
name|snapshot
init|=
name|tableStore
operator|.
name|getCurrentState
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Quota should not be in violation"
argument_list|,
name|snapshot
operator|.
name|getQuotaStatus
argument_list|()
operator|.
name|isInViolation
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|SpaceQuotaSnapshot
name|getSnapshotForTable
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|TableName
name|tn
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|quotaTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
init|;
name|ResultScanner
name|scanner
operator|=
name|quotaTable
operator|.
name|getScanner
argument_list|(
name|QuotaTableUtil
operator|.
name|makeQuotaSnapshotScan
argument_list|()
argument_list|)
init|)
block|{
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|activeViolations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
try|try
block|{
name|QuotaTableUtil
operator|.
name|extractQuotaSnapshot
argument_list|(
name|result
argument_list|,
name|activeViolations
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
specifier|final
name|String
name|msg
init|=
literal|"Failed to parse result for row "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|activeViolations
operator|.
name|get
argument_list|(
name|tn
argument_list|)
return|;
block|}
block|}
specifier|private
name|int
name|getRegionReportsForTable
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Long
argument_list|>
name|reports
parameter_list|,
name|TableName
name|tn
parameter_list|)
block|{
name|int
name|numReports
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|reports
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|tn
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
block|{
name|numReports
operator|++
expr_stmt|;
block|}
block|}
return|return
name|numReports
return|;
block|}
block|}
end_class

end_unit

