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
name|assertEquals
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
name|Collection
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|HRegionLocation
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
name|RegionLocations
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
name|MetaTableAccessor
operator|.
name|Visitor
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
name|ConnectionFactory
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
name|Delete
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
name|RegionReplicaUtil
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
name|TestMasterOperationsForRegionReplicas
block|{
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestRegionPlacement
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
name|Connection
name|CONNECTION
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|ADMIN
decl_stmt|;
specifier|private
specifier|static
name|int
name|numSlaves
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
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
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.tests.use.shortcircuit.reads"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|numSlaves
argument_list|)
expr_stmt|;
name|CONNECTION
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|ADMIN
operator|=
name|CONNECTION
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
while|while
condition|(
name|ADMIN
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|numSlaves
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|ADMIN
operator|!=
literal|null
condition|)
name|ADMIN
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|CONNECTION
operator|!=
literal|null
operator|&&
operator|!
name|CONNECTION
operator|.
name|isClosed
argument_list|()
condition|)
name|CONNECTION
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|testCreateTableWithSingleReplica
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numRegions
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|numReplica
init|=
literal|1
decl_stmt|;
specifier|final
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"singleReplicaTable"
argument_list|)
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|desc
operator|.
name|setRegionReplication
argument_list|(
name|numReplica
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family"
argument_list|)
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Z"
argument_list|)
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
name|validateNumberOfRowsInMeta
argument_list|(
name|table
argument_list|,
name|numRegions
argument_list|,
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|,
name|table
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|hris
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
name|numReplica
operator|)
assert|;
block|}
finally|finally
block|{
name|ADMIN
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateTableWithMultipleReplicas
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"fooTable"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numRegions
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|numReplica
init|=
literal|2
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|desc
operator|.
name|setRegionReplication
argument_list|(
name|numReplica
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family"
argument_list|)
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Z"
argument_list|)
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|validateNumberOfRowsInMeta
argument_list|(
name|table
argument_list|,
name|numRegions
argument_list|,
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|,
name|table
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|hris
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
name|numReplica
operator|)
assert|;
comment|// check that the master created expected number of RegionState objects
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRegions
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numReplica
condition|;
name|j
operator|++
control|)
block|{
name|HRegionInfo
name|replica
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hris
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|j
argument_list|)
decl_stmt|;
name|RegionState
name|state
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|replica
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|state
operator|!=
literal|null
operator|)
assert|;
block|}
block|}
name|List
argument_list|<
name|Result
argument_list|>
name|metaRows
init|=
name|MetaTableAccessor
operator|.
name|fullScanOfMeta
argument_list|(
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numRows
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|metaRows
control|)
block|{
name|RegionLocations
name|locations
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|result
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|locations
operator|.
name|getRegionLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|hri
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|table
argument_list|)
condition|)
continue|continue;
name|numRows
operator|+=
literal|1
expr_stmt|;
name|HRegionLocation
index|[]
name|servers
init|=
name|locations
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
comment|// have two locations for the replicas of a region, and the locations should be different
assert|assert
operator|(
name|servers
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
assert|assert
operator|(
operator|!
name|servers
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|servers
index|[
literal|1
index|]
argument_list|)
operator|)
assert|;
block|}
assert|assert
operator|(
name|numRows
operator|==
name|numRegions
operator|)
assert|;
comment|// The same verification of the meta as above but with the SnapshotOfRegionAssignmentFromMeta
comment|// class
name|validateFromSnapshotFromMeta
argument_list|(
name|TEST_UTIL
argument_list|,
name|table
argument_list|,
name|numRegions
argument_list|,
name|numReplica
argument_list|,
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now kill the master, restart it and see if the assignments are kept
name|ServerName
name|master
init|=
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|stopMaster
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|waitForMasterToStop
argument_list|(
name|master
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|startMaster
argument_list|(
name|master
operator|.
name|getHostname
argument_list|()
argument_list|,
name|master
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
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
name|numRegions
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numReplica
condition|;
name|j
operator|++
control|)
block|{
name|HRegionInfo
name|replica
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hris
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|j
argument_list|)
decl_stmt|;
name|RegionState
name|state
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|replica
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|state
operator|!=
literal|null
operator|)
assert|;
block|}
block|}
name|validateFromSnapshotFromMeta
argument_list|(
name|TEST_UTIL
argument_list|,
name|table
argument_list|,
name|numRegions
argument_list|,
name|numReplica
argument_list|,
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now shut the whole cluster down, and verify the assignments are kept so that the
comment|// availability constraints are met.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.startup.retainassign"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
name|numSlaves
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|validateFromSnapshotFromMeta
argument_list|(
name|TEST_UTIL
argument_list|,
name|table
argument_list|,
name|numRegions
argument_list|,
name|numReplica
argument_list|,
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now shut the whole cluster down, and verify regions are assigned even if there is only
comment|// one server running
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|validateSingleRegionServerAssignment
argument_list|(
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|,
name|numRegions
argument_list|,
name|numReplica
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numSlaves
condition|;
name|i
operator|++
control|)
block|{
comment|//restore the cluster
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
block|}
comment|//check on alter table
name|ADMIN
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|ADMIN
operator|.
name|isTableDisabled
argument_list|(
name|table
argument_list|)
operator|)
assert|;
comment|//increase the replica
name|desc
operator|.
name|setRegionReplication
argument_list|(
name|numReplica
operator|+
literal|1
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|modifyTable
argument_list|(
name|table
argument_list|,
name|desc
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|ADMIN
operator|.
name|isTableEnabled
argument_list|(
name|table
argument_list|)
operator|)
assert|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|regions
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
operator|(
name|numReplica
operator|+
literal|1
operator|)
operator|)
assert|;
comment|//decrease the replica(earlier, table was modified to have a replica count of numReplica + 1)
name|ADMIN
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setRegionReplication
argument_list|(
name|numReplica
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|modifyTable
argument_list|(
name|table
argument_list|,
name|desc
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|ADMIN
operator|.
name|isTableEnabled
argument_list|(
name|table
argument_list|)
operator|)
assert|;
name|regions
operator|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|regions
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
name|numReplica
operator|)
assert|;
comment|//also make sure the meta table has the replica locations removed
name|hris
operator|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|,
name|table
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|hris
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
name|numReplica
operator|)
assert|;
comment|//just check that the number of default replica regions in the meta table are the same
comment|//as the number of regions the table was created with, and the count of the
comment|//replicas is numReplica for each region
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Integer
argument_list|>
name|defaultReplicas
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
name|Integer
name|i
decl_stmt|;
name|HRegionInfo
name|regionReplica0
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForDefaultReplica
argument_list|(
name|hri
argument_list|)
decl_stmt|;
name|defaultReplicas
operator|.
name|put
argument_list|(
name|regionReplica0
argument_list|,
operator|(
name|i
operator|=
name|defaultReplicas
operator|.
name|get
argument_list|(
name|regionReplica0
argument_list|)
operator|)
operator|==
literal|null
condition|?
literal|1
else|:
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
assert|assert
operator|(
name|defaultReplicas
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|)
assert|;
name|Collection
argument_list|<
name|Integer
argument_list|>
name|counts
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|defaultReplicas
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|counts
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|counts
operator|.
name|contains
argument_list|(
operator|new
name|Integer
argument_list|(
name|numReplica
argument_list|)
argument_list|)
operator|)
assert|;
block|}
finally|finally
block|{
name|ADMIN
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
comment|//@Test (TODO: enable when we have support for alter_table- HBASE-10361).
specifier|public
name|void
name|testIncompleteMetaTableReplicaInformation
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"fooTableTest1"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numRegions
init|=
literal|3
decl_stmt|;
specifier|final
name|int
name|numReplica
init|=
literal|2
decl_stmt|;
try|try
block|{
comment|// Create a table and let the meta table be updated with the location of the
comment|// region locations.
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|desc
operator|.
name|setRegionReplication
argument_list|(
name|numReplica
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family"
argument_list|)
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Z"
argument_list|)
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|tableRows
init|=
operator|new
name|HashSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|ADMIN
operator|.
name|getConnection
argument_list|()
argument_list|,
name|table
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
name|tableRows
operator|.
name|add
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ADMIN
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// now delete one replica info from all the rows
comment|// this is to make the meta appear to be only partially updated
name|Table
name|metaTable
init|=
name|ADMIN
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|row
range|:
name|tableRows
control|)
block|{
name|Delete
name|deleteOneReplicaLocation
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|deleteOneReplicaLocation
operator|.
name|deleteColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getServerColumn
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|deleteOneReplicaLocation
operator|.
name|deleteColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getSeqNumColumn
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|deleteOneReplicaLocation
operator|.
name|deleteColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getStartCodeColumn
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|metaTable
operator|.
name|delete
argument_list|(
name|deleteOneReplicaLocation
argument_list|)
expr_stmt|;
block|}
name|metaTable
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// even if the meta table is partly updated, when we re-enable the table, we should
comment|// get back the desired number of replicas for the regions
name|ADMIN
operator|.
name|enableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|ADMIN
operator|.
name|isTableEnabled
argument_list|(
name|table
argument_list|)
operator|)
assert|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|regions
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
name|numReplica
operator|)
assert|;
block|}
finally|finally
block|{
name|ADMIN
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|ADMIN
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|printRegions
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|StringBuffer
name|strBuf
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|r
range|:
name|regions
control|)
block|{
name|strBuf
operator|.
name|append
argument_list|(
literal|" ____ "
operator|+
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|strBuf
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|validateNumberOfRowsInMeta
parameter_list|(
specifier|final
name|TableName
name|table
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|ADMIN
operator|.
name|tableExists
argument_list|(
name|table
argument_list|)
operator|)
assert|;
specifier|final
name|AtomicInteger
name|count
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|Visitor
name|visitor
init|=
operator|new
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|HRegionInfo
operator|.
name|getHRegionInfo
argument_list|(
name|r
argument_list|)
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|table
argument_list|)
condition|)
name|count
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|MetaTableAccessor
operator|.
name|fullScan
argument_list|(
name|connection
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|count
operator|.
name|get
argument_list|()
operator|==
name|numRegions
operator|)
assert|;
block|}
specifier|private
name|void
name|validateFromSnapshotFromMeta
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|table
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numReplica
parameter_list|,
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotOfRegionAssignmentFromMeta
name|snapshot
init|=
operator|new
name|SnapshotOfRegionAssignmentFromMeta
argument_list|(
name|connection
argument_list|)
decl_stmt|;
name|snapshot
operator|.
name|initialize
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionToServerMap
init|=
name|snapshot
operator|.
name|getRegionToRegionServerMap
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|regionToServerMap
operator|.
name|size
argument_list|()
operator|==
name|numRegions
operator|*
name|numReplica
operator|+
literal|1
operator|)
assert|;
comment|//'1' for the namespace
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serverToRegionMap
init|=
name|snapshot
operator|.
name|getRegionServerToRegionMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|entry
range|:
name|serverToRegionMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|setOfStartKeys
init|=
operator|new
name|HashSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|byte
index|[]
name|startKey
init|=
name|region
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|setOfStartKeys
operator|.
name|add
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
comment|//ignore other tables
name|LOG
operator|.
name|info
argument_list|(
literal|"--STARTKEY "
operator|+
operator|new
name|String
argument_list|(
name|startKey
argument_list|)
operator|+
literal|"--"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// the number of startkeys will be equal to the number of regions hosted in each server
comment|// (each server will be hosting one replica of a region)
name|assertEquals
argument_list|(
name|numRegions
argument_list|,
name|setOfStartKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|validateSingleRegionServerAssignment
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numReplica
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotOfRegionAssignmentFromMeta
name|snapshot
init|=
operator|new
name|SnapshotOfRegionAssignmentFromMeta
argument_list|(
name|connection
argument_list|)
decl_stmt|;
name|snapshot
operator|.
name|initialize
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionToServerMap
init|=
name|snapshot
operator|.
name|getRegionToRegionServerMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|regionToServerMap
operator|.
name|size
argument_list|()
argument_list|,
name|numRegions
operator|*
name|numReplica
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|//'1' for the namespace
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serverToRegionMap
init|=
name|snapshot
operator|.
name|getRegionServerToRegionMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|serverToRegionMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// 1 rs + 1 master
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|entry
range|:
name|serverToRegionMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|assertEquals
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|numRegions
operator|*
name|numReplica
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

