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
name|assertTrue
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
name|TableNotEnabledException
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
name|ProcedureResult
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
name|Assert
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
name|TestDisableTableProcedure
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
name|TestDisableTableProcedure
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
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
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
literal|1
argument_list|)
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
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
literal|false
argument_list|)
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
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
literal|false
argument_list|)
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
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testDisableTable
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
literal|"testDisableTable"
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
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|procExec
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
comment|// Disable the table
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// Wait the completion
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
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testDisableTableMultipleTimes
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
literal|"testDisableTableMultipleTimes"
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
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|procExec
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
comment|// Disable the table
name|long
name|procId1
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// Wait the completion
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
comment|// Disable the table again - expect failure
name|long
name|procId2
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
argument_list|,
name|nonceGroup
operator|+
literal|1
argument_list|,
name|nonce
operator|+
literal|1
argument_list|)
decl_stmt|;
comment|// Wait the completion
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId2
argument_list|)
expr_stmt|;
name|ProcedureResult
name|result
init|=
name|procExec
operator|.
name|getResult
argument_list|(
name|procId2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Disable failed with exception: "
operator|+
name|result
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getException
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TableNotEnabledException
argument_list|)
expr_stmt|;
comment|// Disable the table - expect failure from ProcedurePrepareLatch
try|try
block|{
specifier|final
name|ProcedurePrepareLatch
name|prepareLatch
init|=
operator|new
name|ProcedurePrepareLatch
operator|.
name|CompatibilityLatch
argument_list|()
decl_stmt|;
name|long
name|procId3
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
argument_list|,
name|prepareLatch
argument_list|)
argument_list|,
name|nonceGroup
operator|+
literal|2
argument_list|,
name|nonce
operator|+
literal|2
argument_list|)
decl_stmt|;
name|prepareLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Disable should throw exception through latch."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotEnabledException
name|tnee
parameter_list|)
block|{
comment|// Expected
name|LOG
operator|.
name|debug
argument_list|(
literal|"Disable failed with expected exception."
argument_list|)
expr_stmt|;
block|}
comment|// Disable the table again with skipping table state check flag (simulate recovery scenario)
name|long
name|procId4
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
literal|true
argument_list|)
argument_list|)
decl_stmt|;
comment|// Wait the completion
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId4
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId4
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
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testDisableTableTwiceWithSameNonce
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
literal|"testDisableTableTwiceWithSameNonce"
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
name|MasterProcedureTestingUtility
operator|.
name|createTable
argument_list|(
name|procExec
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
comment|// Disable the table
name|long
name|procId1
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// Wait the completion
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
name|assertTrue
argument_list|(
name|procId1
operator|==
name|procId2
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
name|procExec
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
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Disable procedure&& kill the executor
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// Restart the executor and execute the step twice
name|int
name|numberOfSteps
init|=
name|DisableTableState
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

