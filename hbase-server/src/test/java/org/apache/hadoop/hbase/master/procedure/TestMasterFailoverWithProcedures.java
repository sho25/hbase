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
name|fs
operator|.
name|Path
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
name|CategoryBasedTimeout
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
name|procedure2
operator|.
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|CreateTableState
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
name|DeleteTableState
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
name|DisableTableState
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
name|EnableTableState
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
name|TruncateTableState
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
name|FSUtils
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
name|ModifyRegionUtils
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
name|junit
operator|.
name|rules
operator|.
name|TestRule
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMasterFailoverWithProcedures
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
name|TestMasterFailoverWithProcedures
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|forClass
argument_list|(
name|TestMasterFailoverWithProcedures
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
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// don't waste time retrying with the roll, the test is already slow enough.
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|MAX_RETRIES_BEFORE_ROLL_CONF_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|WAIT_BEFORE_ROLL_CONF_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|ROLL_RETRIES_CONF_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|WALProcedureStore
operator|.
name|MAX_SYNC_FAILURE_ROLL_CONF_KEY
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
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
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
name|setToggleKillBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
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
comment|// ==========================================================================
comment|//  Test Create Table
comment|// ==========================================================================
annotation|@
name|Test
specifier|public
name|void
name|testCreateWithFailover
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: Should we try every step? (master failover takes long time)
comment|// It is already covered by TestCreateTableProcedure
comment|// but without the master restart, only the executor/store is restarted.
comment|// Without Master restart we may not find bug in the procedure code
comment|// like missing "wait" for resources to be available (e.g. RS)
name|testCreateWithFailoverAtStep
argument_list|(
name|CreateTableState
operator|.
name|CREATE_TABLE_ASSIGN_REGIONS
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testCreateWithFailoverAtStep
parameter_list|(
specifier|final
name|int
name|step
parameter_list|)
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
literal|"testCreateWithFailoverAtStep"
operator|+
name|step
argument_list|)
decl_stmt|;
comment|// create the table
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
name|setKillBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setToggleKillBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Create procedure&& kill the executor
name|byte
index|[]
index|[]
name|splitKeys
init|=
literal|null
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|MasterProcedureTestingUtility
operator|.
name|createHTD
argument_list|(
name|tableName
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|regions
init|=
name|ModifyRegionUtils
operator|.
name|createHRegionInfos
argument_list|(
name|htd
argument_list|,
name|splitKeys
argument_list|)
decl_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|CreateTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|regions
argument_list|)
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExecution
argument_list|(
name|UTIL
argument_list|,
name|procId
argument_list|,
name|step
argument_list|,
name|CreateTableState
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|validateTableCreation
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regions
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Test Delete Table
comment|// ==========================================================================
annotation|@
name|Test
specifier|public
name|void
name|testDeleteWithFailover
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: Should we try every step? (master failover takes long time)
comment|// It is already covered by TestDeleteTableProcedure
comment|// but without the master restart, only the executor/store is restarted.
comment|// Without Master restart we may not find bug in the procedure code
comment|// like missing "wait" for resources to be available (e.g. RS)
name|testDeleteWithFailoverAtStep
argument_list|(
name|DeleteTableState
operator|.
name|DELETE_TABLE_UNASSIGN_REGIONS
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testDeleteWithFailoverAtStep
parameter_list|(
specifier|final
name|int
name|step
parameter_list|)
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
literal|"testDeleteWithFailoverAtStep"
operator|+
name|step
argument_list|)
decl_stmt|;
comment|// create the table
name|byte
index|[]
index|[]
name|splitKeys
init|=
literal|null
decl_stmt|;
name|HRegionInfo
index|[]
name|regions
init|=
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|splitKeys
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|validateTableCreation
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regions
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
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
name|setKillBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setToggleKillBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Delete procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|DeleteTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExecution
argument_list|(
name|UTIL
argument_list|,
name|procId
argument_list|,
name|step
argument_list|,
name|DeleteTableState
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|validateTableDeletion
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Test Truncate Table
comment|// ==========================================================================
annotation|@
name|Test
specifier|public
name|void
name|testTruncateWithFailover
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: Should we try every step? (master failover takes long time)
comment|// It is already covered by TestTruncateTableProcedure
comment|// but without the master restart, only the executor/store is restarted.
comment|// Without Master restart we may not find bug in the procedure code
comment|// like missing "wait" for resources to be available (e.g. RS)
name|testTruncateWithFailoverAtStep
argument_list|(
literal|true
argument_list|,
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_ADD_TO_META
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testTruncateWithFailoverAtStep
parameter_list|(
specifier|final
name|boolean
name|preserveSplits
parameter_list|,
specifier|final
name|int
name|step
parameter_list|)
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
literal|"testTruncateWithFailoverAtStep"
operator|+
name|step
argument_list|)
decl_stmt|;
comment|// create the table
specifier|final
name|String
index|[]
name|families
init|=
operator|new
name|String
index|[]
block|{
literal|"f1"
block|,
literal|"f2"
block|}
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
block|}
decl_stmt|;
name|HRegionInfo
index|[]
name|regions
init|=
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|splitKeys
argument_list|,
name|families
argument_list|)
decl_stmt|;
comment|// load and verify that there are rows in the table
name|MasterProcedureTestingUtility
operator|.
name|loadData
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|100
argument_list|,
name|splitKeys
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|UTIL
operator|.
name|countRows
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// disable the table
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
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
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Truncate procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|TruncateTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|preserveSplits
argument_list|)
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExecution
argument_list|(
name|UTIL
argument_list|,
name|procId
argument_list|,
name|step
argument_list|,
name|TruncateTableState
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// validate the table regions and layout
name|regions
operator|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionInfo
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|preserveSplits
condition|)
block|{
name|assertEquals
argument_list|(
literal|1
operator|+
name|splitKeys
operator|.
name|length
argument_list|,
name|regions
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regions
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|MasterProcedureTestingUtility
operator|.
name|validateTableCreation
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regions
argument_list|,
name|families
argument_list|)
expr_stmt|;
comment|// verify that there are no rows in the table
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|UTIL
operator|.
name|countRows
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify that the table is read/writable
name|MasterProcedureTestingUtility
operator|.
name|loadData
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|50
argument_list|,
name|splitKeys
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|UTIL
operator|.
name|countRows
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Test Disable Table
comment|// ==========================================================================
annotation|@
name|Test
specifier|public
name|void
name|testDisableTableWithFailover
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: Should we try every step? (master failover takes long time)
comment|// It is already covered by TestDisableTableProcedure
comment|// but without the master restart, only the executor/store is restarted.
comment|// Without Master restart we may not find bug in the procedure code
comment|// like missing "wait" for resources to be available (e.g. RS)
name|testDisableTableWithFailoverAtStep
argument_list|(
name|DisableTableState
operator|.
name|DISABLE_TABLE_MARK_REGIONS_OFFLINE
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testDisableTableWithFailoverAtStep
parameter_list|(
specifier|final
name|int
name|step
parameter_list|)
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
literal|"testDisableTableWithFailoverAtStep"
operator|+
name|step
argument_list|)
decl_stmt|;
comment|// create the table
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
block|}
decl_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|splitKeys
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
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
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Delete procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|DisableTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExecution
argument_list|(
name|UTIL
argument_list|,
name|procId
argument_list|,
name|step
argument_list|,
name|DisableTableState
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|validateTableIsDisabled
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Test Enable Table
comment|// ==========================================================================
annotation|@
name|Test
specifier|public
name|void
name|testEnableTableWithFailover
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO: Should we try every step? (master failover takes long time)
comment|// It is already covered by TestEnableTableProcedure
comment|// but without the master restart, only the executor/store is restarted.
comment|// Without Master restart we may not find bug in the procedure code
comment|// like missing "wait" for resources to be available (e.g. RS)
name|testEnableTableWithFailoverAtStep
argument_list|(
name|EnableTableState
operator|.
name|ENABLE_TABLE_MARK_REGIONS_ONLINE
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testEnableTableWithFailoverAtStep
parameter_list|(
specifier|final
name|int
name|step
parameter_list|)
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
literal|"testEnableTableWithFailoverAtStep"
operator|+
name|step
argument_list|)
decl_stmt|;
comment|// create the table
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
block|}
decl_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|splitKeys
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
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
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Delete procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|EnableTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|testRecoveryAndDoubleExecution
argument_list|(
name|UTIL
argument_list|,
name|procId
argument_list|,
name|step
argument_list|,
name|EnableTableState
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|validateTableIsEnabled
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Test Helpers
comment|// ==========================================================================
specifier|public
specifier|static
parameter_list|<
name|TState
parameter_list|>
name|void
name|testRecoveryAndDoubleExecution
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|testUtil
parameter_list|,
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|int
name|lastStepBeforeFailover
parameter_list|,
name|TState
index|[]
name|states
parameter_list|)
throws|throws
name|Exception
block|{
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|testUtil
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|lastStepBeforeFailover
condition|;
operator|++
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart "
operator|+
name|i
operator|+
literal|" exec state: "
operator|+
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
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
block|}
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Trigger master failover"
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|masterFailover
argument_list|(
name|testUtil
argument_list|)
expr_stmt|;
name|procExec
operator|=
name|testUtil
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
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
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Helpers
comment|// ==========================================================================
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
name|Path
name|getRootDir
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
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
return|;
block|}
block|}
end_class

end_unit

