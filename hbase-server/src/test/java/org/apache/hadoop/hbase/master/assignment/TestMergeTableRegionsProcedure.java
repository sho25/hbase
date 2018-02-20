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
operator|.
name|assignment
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
name|util
operator|.
name|List
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
name|RegionInfo
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
name|procedure
operator|.
name|MasterProcedureConstants
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure
operator|.
name|MasterProcedureTestingUtility
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
name|procedure2
operator|.
name|ProcedureExecutor
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
name|procedure2
operator|.
name|ProcedureMetrics
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
name|procedure2
operator|.
name|ProcedureTestingUtility
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|TestMergeTableRegionsProcedure
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
name|TestMergeTableRegionsProcedure
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
name|TestMergeTableRegionsProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|protected
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
name|long
name|nonceGroup
init|=
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
specifier|private
specifier|static
name|long
name|nonce
init|=
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|initialRegionCount
init|=
literal|4
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"FAMILY"
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
specifier|private
name|AssignmentManager
name|am
decl_stmt|;
specifier|private
name|ProcedureMetrics
name|mergeProcMetrics
decl_stmt|;
specifier|private
name|ProcedureMetrics
name|assignProcMetrics
decl_stmt|;
specifier|private
name|ProcedureMetrics
name|unassignProcMetrics
decl_stmt|;
specifier|private
name|long
name|mergeSubmittedCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|mergeFailedCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|assignSubmittedCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|assignFailedCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|unassignSubmittedCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|unassignFailedCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Reduce the maximum attempts to speed up the test
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.assignment.maximum.attempts"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.maximum.ping.server.attempts"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.ping.server.retry.sleep.interval"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MasterProcedureConstants
operator|.
name|MASTER_PROCEDURE_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|admin
operator|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|resetProcExecutorTestingKillFlag
argument_list|()
expr_stmt|;
name|nonceGroup
operator|=
name|MasterProcedureTestingUtility
operator|.
name|generateNonceGroup
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
name|nonce
operator|=
name|MasterProcedureTestingUtility
operator|.
name|generateNonce
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
comment|// Turn off balancer so it doesn't cut in and mess up our placements.
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|setBalancerRunning
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Turn off the meta scanner so it don't remove parent on us.
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|setCatalogJanitorEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|resetProcExecutorTestingKillFlag
argument_list|()
expr_stmt|;
name|am
operator|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
expr_stmt|;
name|mergeProcMetrics
operator|=
name|am
operator|.
name|getAssignmentManagerMetrics
argument_list|()
operator|.
name|getMergeProcMetrics
argument_list|()
expr_stmt|;
name|assignProcMetrics
operator|=
name|am
operator|.
name|getAssignmentManagerMetrics
argument_list|()
operator|.
name|getAssignProcMetrics
argument_list|()
expr_stmt|;
name|unassignProcMetrics
operator|=
name|am
operator|.
name|getAssignmentManagerMetrics
argument_list|()
operator|.
name|getUnassignProcMetrics
argument_list|()
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
name|resetProcExecutorTestingKillFlag
argument_list|()
expr_stmt|;
for|for
control|(
name|HTableDescriptor
name|htd
range|:
name|UTIL
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
name|UTIL
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
specifier|private
name|void
name|resetProcExecutorTestingKillFlag
parameter_list|()
block|{
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected executor to be running"
argument_list|,
name|procExec
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * This tests two region merges    */
annotation|@
name|Test
specifier|public
name|void
name|testMergeTwoRegions
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
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|RegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|RegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|regionsToMerge
index|[
literal|0
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|regionsToMerge
index|[
literal|1
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// collect AM metrics before test
name|collectAssignmentManagerMetrics
argument_list|()
expr_stmt|;
name|MergeTableRegionsProcedure
name|proc
init|=
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertRegionCount
argument_list|(
name|tableName
argument_list|,
name|initialRegionCount
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergeSubmittedCount
operator|+
literal|1
argument_list|,
name|mergeProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergeFailedCount
argument_list|,
name|mergeProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|assignSubmittedCount
operator|+
literal|1
argument_list|,
name|assignProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|assignFailedCount
argument_list|,
name|assignProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|unassignSubmittedCount
operator|+
literal|2
argument_list|,
name|unassignProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|unassignFailedCount
argument_list|,
name|unassignProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
name|pair
init|=
name|MetaTableAccessor
operator|.
name|getRegionsFromMergeQualifier
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|proc
operator|.
name|getMergedRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
operator|!=
literal|null
operator|&&
name|pair
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|// Can I purge the merged regions from hbase:meta? Check that all went
comment|// well by looking at the merged row up in hbase:meta. It should have no
comment|// more mention of the merged regions; they are purged as last step in
comment|// the merged regions cleanup.
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|setCatalogJanitorEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getCatalogJanitor
argument_list|()
operator|.
name|triggerNow
argument_list|()
expr_stmt|;
while|while
condition|(
name|pair
operator|!=
literal|null
operator|&&
name|pair
operator|.
name|getFirst
argument_list|()
operator|!=
literal|null
operator|&&
name|pair
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|pair
operator|=
name|MetaTableAccessor
operator|.
name|getRegionsFromMergeQualifier
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|proc
operator|.
name|getMergedRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This tests two concurrent region merges    */
annotation|@
name|Test
specifier|public
name|void
name|testMergeRegionsConcurrently
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
literal|"testMergeRegionsConcurrently"
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|RegionInfo
index|[]
name|regionsToMerge1
init|=
operator|new
name|RegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|RegionInfo
index|[]
name|regionsToMerge2
init|=
operator|new
name|RegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|regionsToMerge1
index|[
literal|0
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|regionsToMerge1
index|[
literal|1
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|regionsToMerge2
index|[
literal|0
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|regionsToMerge2
index|[
literal|1
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// collect AM metrics before test
name|collectAssignmentManagerMetrics
argument_list|()
expr_stmt|;
name|long
name|procId1
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|regionsToMerge1
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|procId2
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|regionsToMerge2
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId1
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId2
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId1
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId2
argument_list|)
expr_stmt|;
name|assertRegionCount
argument_list|(
name|tableName
argument_list|,
name|initialRegionCount
operator|-
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergeSubmittedCount
operator|+
literal|2
argument_list|,
name|mergeProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergeFailedCount
argument_list|,
name|mergeProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|assignSubmittedCount
operator|+
literal|2
argument_list|,
name|assignProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|assignFailedCount
argument_list|,
name|assignProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|unassignSubmittedCount
operator|+
literal|4
argument_list|,
name|unassignProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|unassignFailedCount
argument_list|,
name|unassignProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecution
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
literal|"testRecoveryAndDoubleExecution"
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|RegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|RegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|regionsToMerge
index|[
literal|0
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|regionsToMerge
index|[
literal|1
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
comment|// Restart the executor and execute the step twice
name|MasterProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertRegionCount
argument_list|(
name|tableName
argument_list|,
name|initialRegionCount
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRollbackAndDoubleExecution
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
literal|"testRollbackAndDoubleExecution"
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|RegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|RegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|regionsToMerge
index|[
literal|0
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|regionsToMerge
index|[
literal|1
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
comment|// Failing before MERGE_TABLE_REGIONS_CREATE_MERGED_REGION we should trigger the rollback
comment|// NOTE: the 5 (number before MERGE_TABLE_REGIONS_CREATE_MERGED_REGION step) is
comment|// hardcoded, so you have to look at this test at least once when you add a new step.
name|int
name|numberOfSteps
init|=
literal|5
decl_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|testRollbackAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
name|numberOfSteps
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeWithoutPONR
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
literal|"testMergeWithoutPONR"
argument_list|)
decl_stmt|;
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|RegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|RegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|regionsToMerge
index|[
literal|0
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|regionsToMerge
index|[
literal|1
index|]
operator|=
name|tableRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
comment|// Execute until step 9 of split procedure
comment|// NOTE: step 9 is after step MERGE_TABLE_REGIONS_UPDATE_META
name|MasterProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
literal|9
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Unset Toggle Kill and make ProcExec work correctly
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|restartMasterProcedureExecutor
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertRegionCount
argument_list|(
name|tableName
argument_list|,
name|initialRegionCount
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|splitRows
init|=
operator|new
name|byte
index|[
name|initialRegionCount
operator|-
literal|1
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
name|splitRows
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|splitRows
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|splitRows
argument_list|)
expr_stmt|;
return|return
name|assertRegionCount
argument_list|(
name|tableName
argument_list|,
name|initialRegionCount
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|assertRegionCount
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|int
name|nregions
parameter_list|)
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|tableRegions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|nregions
argument_list|,
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tableRegions
return|;
block|}
specifier|private
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
parameter_list|()
block|{
return|return
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
return|;
block|}
specifier|private
name|void
name|collectAssignmentManagerMetrics
parameter_list|()
block|{
name|mergeSubmittedCount
operator|=
name|mergeProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|mergeFailedCount
operator|=
name|mergeProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|assignSubmittedCount
operator|=
name|assignProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|assignFailedCount
operator|=
name|assignProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|unassignSubmittedCount
operator|=
name|unassignProcMetrics
operator|.
name|getSubmittedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|unassignFailedCount
operator|=
name|unassignProcMetrics
operator|.
name|getFailedCounter
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

