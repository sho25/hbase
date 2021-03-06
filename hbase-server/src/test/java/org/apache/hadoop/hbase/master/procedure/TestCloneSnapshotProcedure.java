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
name|stream
operator|.
name|Stream
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
name|client
operator|.
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|SnapshotProtos
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
name|TestCloneSnapshotProcedure
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
name|SnapshotProtos
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
name|getAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|snapshot
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|SnapshotProtos
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
name|String
name|snapshotName
init|=
literal|"snapshot-"
operator|+
name|tid
decl_stmt|;
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
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
name|TableDescriptor
name|createTableDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
modifier|...
name|family
parameter_list|)
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Stream
operator|.
name|of
argument_list|(
name|family
argument_list|)
operator|.
name|map
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|::
name|of
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|builder
operator|::
name|setColumnFamily
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
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
name|TableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|SnapshotProtos
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
specifier|public
name|void
name|testCloneSnapshotToSameTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// take the snapshot
name|SnapshotProtos
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
name|TableDescriptor
name|htd
init|=
name|createTableDescriptor
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
literal|"Clone snapshot failed with exception: "
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
name|TableExistsException
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
name|TableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|SnapshotProtos
operator|.
name|SnapshotDescription
name|snapshotDesc
init|=
name|getSnapshot
argument_list|()
decl_stmt|;
comment|// Here if you enable this then we will enter an infinite loop, as we will fail either after
comment|// TRSP.openRegion or after OpenRegionProcedure.execute, so we can never finish the TRSP...
name|ProcedureTestingUtility
operator|.
name|setKillIfHasParent
argument_list|(
name|procExec
argument_list|,
literal|false
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
name|TableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|clonedTableName
argument_list|,
name|CF
argument_list|)
decl_stmt|;
comment|// take the snapshot
name|SnapshotProtos
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
argument_list|)
decl_stmt|;
name|int
name|lastStep
init|=
literal|2
decl_stmt|;
comment|// failing before CLONE_SNAPSHOT_WRITE_FS_LAYOUT
name|MasterProcedureTestingUtility
operator|.
name|testRollbackAndDoubleExecution
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|,
name|lastStep
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

