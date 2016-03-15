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
name|DoNotRetryIOException
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
name|ProcedureInfo
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
name|TableExistsException
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
name|TestCreateTableProcedure
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
name|TestCreateTableProcedure
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
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testSimpleCreate
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
literal|"testSimpleCreate"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
init|=
literal|null
decl_stmt|;
name|testSimpleCreate
argument_list|(
name|tableName
argument_list|,
name|splitKeys
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
name|testSimpleCreateWithSplits
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
literal|"testSimpleCreateWithSplits"
argument_list|)
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
name|testSimpleCreate
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testSimpleCreate
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|Exception
block|{
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
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testCreateWithoutColumnFamily
parameter_list|()
throws|throws
name|Exception
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
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testCreateWithoutColumnFamily"
argument_list|)
decl_stmt|;
comment|// create table with 0 families will fail
specifier|final
name|HTableDescriptor
name|htd
init|=
name|MasterProcedureTestingUtility
operator|.
name|createHTD
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// disable sanity check
name|htd
operator|.
name|setConfiguration
argument_list|(
literal|"hbase.table.sanity.checks"
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
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
literal|null
argument_list|)
decl_stmt|;
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
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
specifier|final
name|ProcedureInfo
name|result
init|=
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|result
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|ProcedureTestingUtility
operator|.
name|getExceptionCause
argument_list|(
name|result
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected DoNotRetryIOException, got "
operator|+
name|cause
argument_list|,
name|cause
operator|instanceof
name|DoNotRetryIOException
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|,
name|expected
operator|=
name|TableExistsException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testCreateExisting
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
literal|"testCreateExisting"
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
name|HTableDescriptor
name|htd
init|=
name|MasterProcedureTestingUtility
operator|.
name|createHTD
argument_list|(
name|tableName
argument_list|,
literal|"f"
argument_list|)
decl_stmt|;
specifier|final
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
literal|null
argument_list|)
decl_stmt|;
comment|// create the table
name|long
name|procId1
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// create another with the same name
name|ProcedurePrepareLatch
name|latch2
init|=
operator|new
name|ProcedurePrepareLatch
operator|.
name|CompatibilityLatch
argument_list|()
decl_stmt|;
name|long
name|procId2
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
argument_list|,
name|latch2
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
operator|.
name|getResult
argument_list|(
name|procId1
argument_list|)
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
name|latch2
operator|.
name|await
argument_list|()
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
name|testCreateTwiceWithSameNonce
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
literal|"testCreateTwiceWithSameNonce"
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
name|HTableDescriptor
name|htd
init|=
name|MasterProcedureTestingUtility
operator|.
name|createHTD
argument_list|(
name|tableName
argument_list|,
literal|"f"
argument_list|)
decl_stmt|;
specifier|final
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
literal|null
argument_list|)
decl_stmt|;
comment|// create the table
name|long
name|procId1
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// create another with the same name
name|long
name|procId2
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
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
name|assertProcNotFailed
argument_list|(
name|procExec
operator|.
name|getResult
argument_list|(
name|procId1
argument_list|)
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
operator|.
name|getResult
argument_list|(
name|procId2
argument_list|)
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
comment|// create the table
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// Restart the executor and execute the step twice
comment|// NOTE: the 6 (number of CreateTableState steps) is hardcoded,
comment|//       so you have to look at this test at least once when you add a new step.
name|MasterProcedureTestingUtility
operator|.
name|testRecoveryAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
literal|6
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
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|90000
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
name|testRollbackAndDoubleExecution
argument_list|(
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|90000
argument_list|)
specifier|public
name|void
name|testRollbackAndDoubleExecutionOnMobTable
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
literal|"testRollbackAndDoubleExecutionOnMobTable"
argument_list|)
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
name|htd
operator|.
name|getFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|)
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testRollbackAndDoubleExecution
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|90000
argument_list|)
specifier|public
name|void
name|testRollbackRetriableFailure
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
literal|"testRollbackRetriableFailure"
argument_list|)
decl_stmt|;
comment|// create the table
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
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Create procedure&& kill the executor
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
name|FaultyCreateTableProcedure
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// NOTE: the 4 (number of CreateTableState steps) is hardcoded,
comment|//       so you have to look at this test at least once when you add a new step.
name|MasterProcedureTestingUtility
operator|.
name|testRollbackRetriableFailure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
literal|4
argument_list|,
name|CreateTableState
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
argument_list|,
name|regions
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
comment|// are we able to create the table after a rollback?
name|resetProcExecutorTestingKillFlag
argument_list|()
expr_stmt|;
name|testSimpleCreate
argument_list|(
name|tableName
argument_list|,
name|splitKeys
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
specifier|public
specifier|static
class|class
name|FaultyCreateTableProcedure
extends|extends
name|CreateTableProcedure
block|{
specifier|private
name|int
name|retries
init|=
literal|0
decl_stmt|;
specifier|public
name|FaultyCreateTableProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
block|}
specifier|public
name|FaultyCreateTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|newRegions
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|env
argument_list|,
name|hTableDescriptor
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|CreateTableState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|retries
operator|++
operator|<
literal|3
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"inject rollback failure state="
operator|+
name|state
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"injected failure number "
operator|+
name|retries
argument_list|)
throw|;
block|}
else|else
block|{
name|super
operator|.
name|rollbackState
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|retries
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|testRollbackAndDoubleExecution
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|Exception
block|{
comment|// create the table
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
literal|true
argument_list|)
expr_stmt|;
comment|// Start the Create procedure&& kill the executor
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
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
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
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
argument_list|)
decl_stmt|;
comment|// NOTE: the 4 (number of CreateTableState steps) is hardcoded,
comment|//       so you have to look at this test at least once when you add a new step.
name|MasterProcedureTestingUtility
operator|.
name|testRollbackAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
literal|4
argument_list|,
name|CreateTableState
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|htd
operator|.
name|getTableName
argument_list|()
decl_stmt|;
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
argument_list|,
name|regions
argument_list|,
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
expr_stmt|;
comment|// are we able to create the table after a rollback?
name|resetProcExecutorTestingKillFlag
argument_list|()
expr_stmt|;
name|testSimpleCreate
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

