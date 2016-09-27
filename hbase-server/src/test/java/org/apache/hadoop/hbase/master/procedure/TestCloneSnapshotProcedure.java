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
name|protobuf
operator|.
name|ProtobufUtil
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
name|HBaseProtos
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
name|SnapshotDescription
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
name|CloneSnapshotState
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|TestCloneSnapshotProcedure
extends|extends
name|TestTableDDLProcedureBase
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
name|TestCloneSnapshotProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseProtos
operator|.
name|SnapshotDescription
name|snapshot
init|=
literal|null
decl_stmt|;
annotation|@
name|After
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|snapshot
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|HBaseProtos
operator|.
name|SnapshotDescription
name|getSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|snapshot
operator|==
literal|null
condition|)
block|{
specifier|final
name|TableName
name|snapshotTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testCloneSnapshot"
argument_list|)
decl_stmt|;
name|long
name|tid
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"snapshot-"
operator|+
name|tid
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// create Table
name|SnapshotTestingUtils
operator|.
name|createTable
argument_list|(
name|UTIL
argument_list|,
name|snapshotTableName
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|,
name|CF
argument_list|)
expr_stmt|;
comment|// Load data
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|UTIL
argument_list|,
name|snapshotTableName
argument_list|,
literal|500
argument_list|,
name|CF
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|snapshotTableName
argument_list|)
expr_stmt|;
comment|// take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|snapshotTableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|snapshotTableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|snapshotList
init|=
name|admin
operator|.
name|listSnapshots
argument_list|()
decl_stmt|;
name|snapshot
operator|=
name|ProtobufUtil
operator|.
name|createHBaseProtosSnapshotDesc
argument_list|(
name|snapshotList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|snapshot
return|;
block|}
specifier|private
name|int
name|getNumReplicas
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
specifier|public
specifier|static
name|HTableDescriptor
name|createHTableDescriptor
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|family
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
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
name|family
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|htd
return|;
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
name|testCloneSnapshot
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
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testCloneSnapshot2"
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|createHTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|HBaseProtos
operator|.
name|SnapshotDescription
name|snapshotDesc
init|=
name|getSnapshot
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
name|CloneSnapshotProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|snapshotDesc
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
name|clonedTableName
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
name|testCloneSnapshotTwiceWithSameNonce
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
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testCloneSnapshotTwiceWithSameNonce"
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|createHTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|HBaseProtos
operator|.
name|SnapshotDescription
name|snapshotDesc
init|=
name|getSnapshot
argument_list|()
decl_stmt|;
name|long
name|procId1
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|CloneSnapshotProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|snapshotDesc
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
name|CloneSnapshotProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|snapshotDesc
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
name|testCloneSnapshotToSameTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// take the snapshot
name|HBaseProtos
operator|.
name|SnapshotDescription
name|snapshotDesc
init|=
name|getSnapshot
argument_list|()
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
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshotDesc
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|createHTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
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
name|CloneSnapshotProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|snapshotDesc
argument_list|)
argument_list|)
decl_stmt|;
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
literal|"Clone snapshot failed with exception: "
operator|+
name|result
operator|.
name|getExceptionFullMessage
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
name|TableExistsException
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
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRecoveryAndDoubleExecution"
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|createHTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|HBaseProtos
operator|.
name|SnapshotDescription
name|snapshotDesc
init|=
name|getSnapshot
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
comment|// Start the Clone snapshot procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|CloneSnapshotProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|snapshotDesc
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
name|CloneSnapshotState
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
name|clonedTableName
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
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRollbackAndDoubleExecution"
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|createHTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|HBaseProtos
operator|.
name|SnapshotDescription
name|snapshotDesc
init|=
name|getSnapshot
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
comment|// Start the Clone snapshot procedure&& kill the executor
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|CloneSnapshotProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|snapshotDesc
argument_list|)
argument_list|,
name|nonceGroup
argument_list|,
name|nonce
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
name|clonedTableName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

