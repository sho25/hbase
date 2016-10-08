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
name|procedure
package|;
end_package

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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|DispatchMergingRegionsState
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
name|TestDispatchMergingRegionsProcedure
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
name|TestDispatchMergingRegionsProcedure
operator|.
name|class
argument_list|)
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
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
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
literal|"testMergeTwoRegions"
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
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|HRegionInfo
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
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionsToMerge
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
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * This tests two concurrent region merges    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|90000
argument_list|)
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
literal|"testMergeTwoRegions"
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
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|regionsToMerge1
init|=
operator|new
name|HRegionInfo
index|[
literal|2
index|]
decl_stmt|;
name|HRegionInfo
index|[]
name|regionsToMerge2
init|=
operator|new
name|HRegionInfo
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
name|long
name|procId1
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
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
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
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
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMergeRegionsTwiceWithSameNonce
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
literal|"testMergeRegionsTwiceWithSameNonce"
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
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|HRegionInfo
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
name|procId1
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
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
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|procId1
argument_list|,
name|procId2
argument_list|)
expr_stmt|;
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
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId1
argument_list|)
expr_stmt|;
comment|// The second proc should succeed too - because it is the same proc.
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
name|procId2
argument_list|)
expr_stmt|;
name|assertRegionCount
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
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
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|3
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
name|HRegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|HRegionInfo
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
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
comment|// Restart the executor and execute the step twice
name|int
name|numberOfSteps
init|=
name|DispatchMergingRegionsState
operator|.
name|values
argument_list|()
operator|.
name|length
decl_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
name|numberOfSteps
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
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
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
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|3
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
name|HRegionInfo
index|[]
name|regionsToMerge
init|=
operator|new
name|HRegionInfo
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
name|DispatchMergingRegionsProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionsToMerge
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|numberOfSteps
init|=
name|DispatchMergingRegionsState
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|3
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
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|createTable
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
name|nregions
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
name|nregions
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HRegionInfo
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
name|HRegionInfo
argument_list|>
name|tableRegions
init|=
name|admin
operator|.
name|getTableRegions
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
block|}
end_class

end_unit

