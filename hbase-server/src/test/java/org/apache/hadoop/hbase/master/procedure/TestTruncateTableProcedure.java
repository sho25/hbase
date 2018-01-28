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
name|TableNotDisabledException
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
name|TableNotFoundException
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
name|procedure2
operator|.
name|Procedure
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
name|TestTruncateTableProcedure
extends|extends
name|TestTableDDLProcedureBase
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
name|TestTruncateTableProcedure
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
name|TestTruncateTableProcedure
operator|.
name|class
argument_list|)
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
name|Test
specifier|public
name|void
name|testTruncateNotExistentTable
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
name|TruncateTableProcedure
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
comment|// Second delete should fail with TableNotFound
name|Procedure
argument_list|<
name|?
argument_list|>
name|result
init|=
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
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
literal|"Truncate failed with exception: "
operator|+
name|result
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ProcedureTestingUtility
operator|.
name|getExceptionCause
argument_list|(
name|result
argument_list|)
operator|instanceof
name|TableNotFoundException
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTruncateNotDisabledTable
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
literal|"f"
argument_list|)
expr_stmt|;
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
name|TruncateTableProcedure
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
comment|// Second delete should fail with TableNotDisabled
name|Procedure
argument_list|<
name|?
argument_list|>
name|result
init|=
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
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
literal|"Truncate failed with exception: "
operator|+
name|result
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ProcedureTestingUtility
operator|.
name|getExceptionCause
argument_list|(
name|result
argument_list|)
operator|instanceof
name|TableNotDisabledException
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleTruncatePreserveSplits
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
name|testSimpleTruncate
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleTruncateNoPreserveSplits
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
name|testSimpleTruncate
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testSimpleTruncate
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|boolean
name|preserveSplits
parameter_list|)
throws|throws
name|Exception
block|{
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
name|RegionInfo
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
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// truncate the table
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
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// If truncate procedure completed successfully, it means all regions were assigned correctly
comment|// and table is enabled now.
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
name|getAdmin
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
name|RegionInfo
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
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionPreserveSplits
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
name|testRecoveryAndDoubleExecution
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecoveryAndDoubleExecutionNoPreserveSplits
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
name|testRecoveryAndDoubleExecution
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testRecoveryAndDoubleExecution
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|boolean
name|preserveSplits
parameter_list|)
throws|throws
name|Exception
block|{
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
name|RegionInfo
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
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
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
name|getAdmin
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
name|RegionInfo
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
block|}
end_class

end_unit

