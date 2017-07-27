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
name|util
package|;
end_package

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
name|client
operator|.
name|ClusterConnection
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|master
operator|.
name|RegionState
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
name|testclassification
operator|.
name|MiscTests
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
name|Ignore
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
name|Collection
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
name|NavigableMap
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
name|ScheduledThreadPoolExecutor
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
name|SynchronousQueue
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
name|ThreadPoolExecutor
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
name|TimeUnit
import|;
end_import

begin_import
import|import static
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
name|hbck
operator|.
name|HbckTestingUtil
operator|.
name|*
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
name|*
import|;
end_import

begin_class
annotation|@
name|Ignore
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHBaseFsckReplicas
extends|extends
name|BaseTestHBaseFsck
block|{
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MasterSyncObserver
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
literal|"hbase.regionserver.handler.count"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.metahandler.count"
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.htable.threads.max"
argument_list|,
name|POOL_SIZE
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hconnection.threads.max"
argument_list|,
literal|2
operator|*
name|POOL_SIZE
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hbck.close.timeout"
argument_list|,
literal|2
operator|*
name|REGION_ONLINE_TIMEOUT
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
literal|8
operator|*
name|REGION_ONLINE_TIMEOUT
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|tableExecutorService
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|1
argument_list|,
name|POOL_SIZE
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<>
argument_list|()
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"testhbck"
argument_list|)
argument_list|)
expr_stmt|;
name|hbfsckExecutorService
operator|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
name|POOL_SIZE
argument_list|)
expr_stmt|;
name|AssignmentManager
name|assignmentManager
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
decl_stmt|;
name|regionStates
operator|=
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
expr_stmt|;
name|connection
operator|=
operator|(
name|ClusterConnection
operator|)
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
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
name|tableExecutorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|hbfsckExecutorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|admin
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/*  * This creates a table with region_replica> 1, do a split, check  * that hbck will not report split replica parent as lingering split parent  */
annotation|@
name|Test
specifier|public
name|void
name|testHbckReportReplicaLingeringSplitParent
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testHbckReportReplicaLingeringSplitParent"
argument_list|)
decl_stmt|;
try|try
block|{
name|setupTableWithRegionReplica
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// disable catalog janitor
name|admin
operator|.
name|enableCatalogJanitor
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|table
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A1"
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// run hbck again to make sure we don't see any errors
name|assertNoErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// enable catalog janitor
name|admin
operator|.
name|enableCatalogJanitor
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*  * This creates a table with region_replica> 1 and verifies hbck runs  * successfully  */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testHbckWithRegionReplica
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
try|try
block|{
name|setupTableWithRegionReplica
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertNoErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testHbckWithFewerReplica
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
try|try
block|{
name|setupTableWithRegionReplica
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertNoErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
name|deleteRegion
argument_list|(
name|conf
argument_list|,
name|tbl
operator|.
name|getTableDescriptor
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// unassign one replica
comment|// check that problem exists
name|HBaseFsck
name|hbck
init|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertErrors
argument_list|(
name|hbck
argument_list|,
operator|new
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
index|[]
block|{
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|NOT_DEPLOYED
block|}
argument_list|)
expr_stmt|;
comment|// fix the problem
name|hbck
operator|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// run hbck again to make sure we don't see any errors
name|hbck
operator|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertErrors
argument_list|(
name|hbck
argument_list|,
operator|new
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testHbckWithExcessReplica
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
try|try
block|{
name|setupTableWithRegionReplica
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertNoErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// the next few lines inject a location in meta for a replica, and then
comment|// asks the master to assign the replica (the meta needs to be injected
comment|// for the master to treat the request for assignment as valid; the master
comment|// checks the region is valid either from its memory or meta)
name|Table
name|meta
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|tableExecutorService
argument_list|)
decl_stmt|;
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
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|startKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|metaKey
init|=
literal|null
decl_stmt|;
name|HRegionInfo
name|newHri
init|=
literal|null
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|h
range|:
name|regions
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|h
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|startKey
argument_list|)
operator|==
literal|0
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|h
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|endKey
argument_list|)
operator|==
literal|0
operator|&&
name|h
operator|.
name|getReplicaId
argument_list|()
operator|==
name|HRegionInfo
operator|.
name|DEFAULT_REPLICA_ID
condition|)
block|{
name|metaKey
operator|=
name|h
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
comment|//create a hri with replicaId as 2 (since we already have replicas with replicaid 0 and 1)
name|newHri
operator|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|h
argument_list|,
literal|2
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|metaKey
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|var
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|ServerName
name|sn
init|=
name|var
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|var
operator|.
name|size
argument_list|()
index|]
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
comment|//add a location with replicaId as 2 (since we already have replicas with replicaid 0 and 1)
name|MetaTableAccessor
operator|.
name|addLocation
argument_list|(
name|put
argument_list|,
name|sn
argument_list|,
name|sn
operator|.
name|getStartcode
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|meta
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// assign the new replica
name|HBaseFsckRepair
operator|.
name|fixUnassigned
argument_list|(
name|admin
argument_list|,
name|newHri
argument_list|)
expr_stmt|;
name|HBaseFsckRepair
operator|.
name|waitUntilAssigned
argument_list|(
name|admin
argument_list|,
name|newHri
argument_list|)
expr_stmt|;
comment|// now reset the meta row to its original value
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|metaKey
argument_list|)
decl_stmt|;
name|delete
operator|.
name|addColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getServerColumn
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|delete
operator|.
name|addColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getStartCodeColumn
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|delete
operator|.
name|addColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getSeqNumColumn
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|meta
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// check that problem exists
name|HBaseFsck
name|hbck
init|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertErrors
argument_list|(
name|hbck
argument_list|,
operator|new
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
index|[]
block|{
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|NOT_IN_META
block|}
argument_list|)
expr_stmt|;
comment|// fix the problem
name|hbck
operator|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// run hbck again to make sure we don't see any errors
name|hbck
operator|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertErrors
argument_list|(
name|hbck
argument_list|,
operator|new
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This creates and fixes a bad table with a region that is in meta but has    * no deployment or data hdfs. The table has region_replication set to 2.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testNotInHdfsWithReplicas
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
try|try
block|{
name|HRegionInfo
index|[]
name|oldHris
init|=
operator|new
name|HRegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|setupTableWithRegionReplica
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|map
init|=
name|MetaTableAccessor
operator|.
name|allTableRegions
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tbl
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
comment|// store the HRIs of the regions we will mess up
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|m
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getKey
argument_list|()
operator|.
name|getStartKey
argument_list|()
operator|.
name|length
operator|>
literal|0
operator|&&
name|m
operator|.
name|getKey
argument_list|()
operator|.
name|getStartKey
argument_list|()
index|[
literal|0
index|]
operator|==
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
index|[
literal|0
index|]
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initially server hosting "
operator|+
name|m
operator|.
name|getKey
argument_list|()
operator|+
literal|" is "
operator|+
name|m
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|oldHris
index|[
name|i
operator|++
index|]
operator|=
name|m
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
block|}
comment|// make sure data in regions
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// Mess it up by leaving a hole in the hdfs data
name|deleteRegion
argument_list|(
name|conf
argument_list|,
name|tbl
operator|.
name|getTableDescriptor
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// don't rm meta
name|HBaseFsck
name|hbck
init|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertErrors
argument_list|(
name|hbck
argument_list|,
operator|new
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
index|[]
block|{
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|NOT_IN_HDFS
block|}
argument_list|)
expr_stmt|;
comment|// fix hole
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// check that hole fixed
name|assertNoErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
operator|-
literal|2
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// the following code checks whether the old primary/secondary has
comment|// been unassigned and the new primary/secondary has been assigned
name|i
operator|=
literal|0
expr_stmt|;
name|HRegionInfo
index|[]
name|newHris
init|=
operator|new
name|HRegionInfo
index|[
literal|2
index|]
decl_stmt|;
comment|// get all table's regions from meta
name|map
operator|=
name|MetaTableAccessor
operator|.
name|allTableRegions
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tbl
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// get the HRIs of the new regions (hbck created new regions for fixing the hdfs mess-up)
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|m
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getKey
argument_list|()
operator|.
name|getStartKey
argument_list|()
operator|.
name|length
operator|>
literal|0
operator|&&
name|m
operator|.
name|getKey
argument_list|()
operator|.
name|getStartKey
argument_list|()
index|[
literal|0
index|]
operator|==
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
index|[
literal|0
index|]
condition|)
block|{
name|newHris
index|[
name|i
operator|++
index|]
operator|=
name|m
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
block|}
comment|// get all the online regions in the regionservers
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|onlineRegions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|s
range|:
name|servers
control|)
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|list
init|=
name|admin
operator|.
name|getOnlineRegions
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|onlineRegions
operator|.
name|addAll
argument_list|(
name|list
argument_list|)
expr_stmt|;
block|}
comment|// the new HRIs must be a subset of the online regions
name|assertTrue
argument_list|(
name|onlineRegions
operator|.
name|containsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|newHris
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// the old HRIs must not be part of the set (removeAll would return false if
comment|// the set didn't change)
name|assertFalse
argument_list|(
name|onlineRegions
operator|.
name|removeAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|oldHris
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Creates and fixes a bad table with a successful split that have a deployed    * start and end keys and region replicas enabled    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testSplitAndDupeRegionWithRegionReplica
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSplitAndDupeRegionWithRegionReplica"
argument_list|)
decl_stmt|;
name|Table
name|meta
init|=
literal|null
decl_stmt|;
try|try
block|{
name|setupTableWithRegionReplica
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertNoErrors
argument_list|(
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// No Catalog Janitor running
name|admin
operator|.
name|enableCatalogJanitor
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|meta
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|tableExecutorService
argument_list|)
expr_stmt|;
name|HRegionLocation
name|loc
init|=
name|this
operator|.
name|connection
operator|.
name|getRegionLocation
argument_list|(
name|table
argument_list|,
name|SPLITS
index|[
literal|0
index|]
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hriParent
init|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
comment|// Split Region A just before B
name|this
operator|.
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|split
argument_list|(
name|table
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A@"
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// We need to make sure the parent region is not in a split state, so we put it in CLOSED state.
name|regionStates
operator|.
name|updateRegionState
argument_list|(
name|hriParent
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|assignRegion
argument_list|(
name|hriParent
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|addRegionToMeta
argument_list|(
name|meta
argument_list|,
name|hriParent
argument_list|)
expr_stmt|;
name|ServerName
name|server
init|=
name|regionStates
operator|.
name|getRegionServerOfRegion
argument_list|(
name|hriParent
argument_list|)
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
name|TEST_UTIL
operator|.
name|assertRegionOnServer
argument_list|(
name|hriParent
argument_list|,
name|server
argument_list|,
name|REGION_ONLINE_TIMEOUT
argument_list|)
expr_stmt|;
while|while
condition|(
name|findDeployedHSI
argument_list|(
name|getDeployedHRIs
argument_list|(
operator|(
name|HBaseAdmin
operator|)
name|admin
argument_list|)
argument_list|,
name|hriParent
argument_list|)
operator|==
literal|null
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|250
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished assignment of parent region"
argument_list|)
expr_stmt|;
comment|// TODO why is dupe region different from dupe start keys?
name|HBaseFsck
name|hbck
init|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertErrors
argument_list|(
name|hbck
argument_list|,
operator|new
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
index|[]
block|{
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|NOT_DEPLOYED
block|,
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|DUPE_STARTKEYS
block|,
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|DUPE_STARTKEYS
block|,
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
operator|.
name|OVERLAP_IN_REGION_CHAIN
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|hbck
operator|.
name|getOverlapGroups
argument_list|(
name|table
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// fix the degenerate region.
name|hbck
operator|=
operator|new
name|HBaseFsck
argument_list|(
name|conf
argument_list|,
name|hbfsckExecutorService
argument_list|)
expr_stmt|;
name|hbck
operator|.
name|setDisplayFullReport
argument_list|()
expr_stmt|;
comment|// i.e. -details
name|hbck
operator|.
name|setTimeLag
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|hbck
operator|.
name|setFixHdfsOverlaps
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hbck
operator|.
name|setRemoveParents
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hbck
operator|.
name|setFixReferenceFiles
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hbck
operator|.
name|setFixHFileLinks
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hbck
operator|.
name|connect
argument_list|()
expr_stmt|;
name|hbck
operator|.
name|onlineHbck
argument_list|()
expr_stmt|;
name|hbck
operator|.
name|close
argument_list|()
expr_stmt|;
name|hbck
operator|=
name|doFsck
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertNoErrors
argument_list|(
name|hbck
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hbck
operator|.
name|getOverlapGroups
argument_list|(
name|table
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ROWKEYS
operator|.
name|length
argument_list|,
name|countRows
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

