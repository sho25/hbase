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
name|assertFalse
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestModifyTableProcedure
extends|extends
name|TestTableDDLProcedureBase
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
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
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testModifyTable
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
literal|"cf"
argument_list|)
expr_stmt|;
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
comment|// Modify the table descriptor
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
comment|// Test 1: Modify 1 property
name|long
name|newMaxFileSize
init|=
name|htd
operator|.
name|getMaxFileSize
argument_list|()
operator|*
literal|2
decl_stmt|;
name|htd
operator|.
name|setMaxFileSize
argument_list|(
name|newMaxFileSize
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|long
name|procId1
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|)
argument_list|)
decl_stmt|;
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
name|HTableDescriptor
name|currentHtd
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|newMaxFileSize
argument_list|,
name|currentHtd
operator|.
name|getMaxFileSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test 2: Modify multiple properties
name|boolean
name|newReadOnlyOption
init|=
name|htd
operator|.
name|isReadOnly
argument_list|()
condition|?
literal|false
else|:
literal|true
decl_stmt|;
name|long
name|newMemStoreFlushSize
init|=
name|htd
operator|.
name|getMemStoreFlushSize
argument_list|()
operator|*
literal|2
decl_stmt|;
name|htd
operator|.
name|setReadOnly
argument_list|(
name|newReadOnlyOption
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
name|newMemStoreFlushSize
argument_list|)
expr_stmt|;
name|long
name|procId2
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|)
argument_list|)
decl_stmt|;
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
name|currentHtd
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newReadOnlyOption
argument_list|,
name|currentHtd
operator|.
name|isReadOnly
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMemStoreFlushSize
argument_list|,
name|currentHtd
operator|.
name|getMemStoreFlushSize
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
name|testModifyTableAddCF
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
literal|"cf1"
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|currentHtd
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test 1: Modify the table descriptor online
name|String
name|cf2
init|=
literal|"cf2"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|cf2
argument_list|)
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
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|)
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
name|currentHtd
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf2
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test 2: Modify the table descriptor offline
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
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|String
name|cf3
init|=
literal|"cf3"
decl_stmt|;
name|HTableDescriptor
name|htd2
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd2
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|cf3
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|procId2
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd2
argument_list|)
argument_list|)
decl_stmt|;
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
name|currentHtd
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf3
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
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
name|testModifyTableDeleteCF
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
name|String
name|cf1
init|=
literal|"cf1"
decl_stmt|;
specifier|final
name|String
name|cf2
init|=
literal|"cf2"
decl_stmt|;
specifier|final
name|String
name|cf3
init|=
literal|"cf3"
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
name|cf1
argument_list|,
name|cf2
argument_list|,
name|cf3
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|currentHtd
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test 1: Modify the table descriptor
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|removeFamily
argument_list|(
name|cf2
operator|.
name|getBytes
argument_list|()
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
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|)
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
name|currentHtd
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf2
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test 2: Modify the table descriptor offline
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
name|ProcedureTestingUtility
operator|.
name|waitNoProcedureRunning
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd2
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd2
operator|.
name|removeFamily
argument_list|(
name|cf3
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// Disable Sanity check
name|htd2
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
name|long
name|procId2
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd2
argument_list|)
argument_list|)
decl_stmt|;
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
name|currentHtd
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf3
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//Removing the last family will fail
name|HTableDescriptor
name|htd3
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd3
operator|.
name|removeFamily
argument_list|(
name|cf1
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|procId3
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd3
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
name|procId3
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
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf1
operator|.
name|getBytes
argument_list|()
argument_list|)
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
name|testRecoveryAndDoubleExecutionOffline
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
name|String
name|cf2
init|=
literal|"cf2"
decl_stmt|;
specifier|final
name|String
name|cf3
init|=
literal|"cf3"
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
comment|// create the table
name|HRegionInfo
index|[]
name|regions
init|=
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
literal|"cf1"
argument_list|,
name|cf3
argument_list|)
decl_stmt|;
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
comment|// Modify multiple properties of the table.
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|newCompactionEnableOption
init|=
name|htd
operator|.
name|isCompactionEnabled
argument_list|()
condition|?
literal|false
else|:
literal|true
decl_stmt|;
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
name|newCompactionEnableOption
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|cf2
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|removeFamily
argument_list|(
name|cf3
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// Start the Modify procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
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
comment|// Validate descriptor
name|HTableDescriptor
name|currentHtd
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|newCompactionEnableOption
argument_list|,
name|currentHtd
operator|.
name|isCompactionEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// cf2 should be added cf3 should be removed
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
literal|false
argument_list|,
literal|"cf1"
argument_list|,
name|cf2
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
name|testRecoveryAndDoubleExecutionOnline
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
name|String
name|cf2
init|=
literal|"cf2"
decl_stmt|;
specifier|final
name|String
name|cf3
init|=
literal|"cf3"
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
comment|// create the table
name|HRegionInfo
index|[]
name|regions
init|=
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
literal|"cf1"
argument_list|,
name|cf3
argument_list|)
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
comment|// Modify multiple properties of the table.
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|newCompactionEnableOption
init|=
name|htd
operator|.
name|isCompactionEnabled
argument_list|()
condition|?
literal|false
else|:
literal|true
decl_stmt|;
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
name|newCompactionEnableOption
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|cf2
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|removeFamily
argument_list|(
name|cf3
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// Start the Modify procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
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
comment|// Validate descriptor
name|HTableDescriptor
name|currentHtd
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|newCompactionEnableOption
argument_list|,
name|currentHtd
operator|.
name|isCompactionEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|currentHtd
operator|.
name|getFamiliesKeys
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf2
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|currentHtd
operator|.
name|hasFamily
argument_list|(
name|cf3
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// cf2 should be added cf3 should be removed
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
literal|"cf1"
argument_list|,
name|cf2
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
name|testRollbackAndDoubleExecutionOnline
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
name|String
name|familyName
init|=
literal|"cf2"
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
comment|// create the table
name|HRegionInfo
index|[]
name|regions
init|=
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
literal|"cf1"
argument_list|)
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|newCompactionEnableOption
init|=
name|htd
operator|.
name|isCompactionEnabled
argument_list|()
condition|?
literal|false
else|:
literal|true
decl_stmt|;
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
name|newCompactionEnableOption
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
comment|// Start the Modify procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|numberOfSteps
init|=
literal|0
decl_stmt|;
comment|// failing at pre operation
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
comment|// cf2 should not be present
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
literal|"cf1"
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
name|testRollbackAndDoubleExecutionOffline
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
name|String
name|familyName
init|=
literal|"cf2"
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
comment|// create the table
name|HRegionInfo
index|[]
name|regions
init|=
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
literal|"cf1"
argument_list|)
decl_stmt|;
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|newCompactionEnableOption
init|=
name|htd
operator|.
name|isCompactionEnabled
argument_list|()
condition|?
literal|false
else|:
literal|true
decl_stmt|;
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
name|newCompactionEnableOption
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// Start the Modify procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|ModifyTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|)
argument_list|)
decl_stmt|;
comment|// Restart the executor and rollback the step twice
name|int
name|numberOfSteps
init|=
literal|0
decl_stmt|;
comment|// failing at pre operation
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
comment|// cf2 should not be present
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
literal|"cf1"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

