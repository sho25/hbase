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
name|procedure2
operator|.
name|store
operator|.
name|wal
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
name|java
operator|.
name|util
operator|.
name|Random
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
name|TestProcedureStoreTracker
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
name|TestProcedureStoreTracker
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
name|TestProcedureStoreTracker
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSeqInsertAndDelete
parameter_list|()
block|{
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|MIN_PROC
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|MAX_PROC
init|=
literal|1
operator|<<
literal|10
decl_stmt|;
comment|// sequential insert
for|for
control|(
name|int
name|i
init|=
name|MIN_PROC
init|;
name|i
operator|<
name|MAX_PROC
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|insert
argument_list|(
name|i
argument_list|)
expr_stmt|;
comment|// All the proc that we inserted should not be deleted
for|for
control|(
name|int
name|j
init|=
name|MIN_PROC
init|;
name|j
operator|<=
name|i
condition|;
operator|++
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|NO
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// All the proc that are not yet inserted should be result as deleted
for|for
control|(
name|int
name|j
init|=
name|i
operator|+
literal|1
init|;
name|j
operator|<
name|MAX_PROC
condition|;
operator|++
name|j
control|)
block|{
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isDeleted
argument_list|(
name|j
argument_list|)
operator|!=
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|NO
argument_list|)
expr_stmt|;
block|}
block|}
comment|// sequential delete
for|for
control|(
name|int
name|i
init|=
name|MIN_PROC
init|;
name|i
operator|<
name|MAX_PROC
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|delete
argument_list|(
name|i
argument_list|)
expr_stmt|;
comment|// All the proc that we deleted should be deleted
for|for
control|(
name|int
name|j
init|=
name|MIN_PROC
init|;
name|j
operator|<=
name|i
condition|;
operator|++
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|YES
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// All the proc that are not yet deleted should be result as not deleted
for|for
control|(
name|int
name|j
init|=
name|i
operator|+
literal|1
init|;
name|j
operator|<
name|MAX_PROC
condition|;
operator|++
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|NO
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPartialTracker
parameter_list|()
block|{
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
name|tracker
operator|.
name|setPartialFlag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// nothing in the tracker, the state is unknown
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|579
argument_list|)
argument_list|)
expr_stmt|;
comment|// Mark 1 as deleted, now that is a known state
name|tracker
operator|.
name|setDeleted
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|dump
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|YES
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|579
argument_list|)
argument_list|)
expr_stmt|;
comment|// Mark 579 as non-deleted, now that is a known state
name|tracker
operator|.
name|setDeleted
argument_list|(
literal|579
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|YES
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|NO
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|579
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|577
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|MAYBE
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
literal|580
argument_list|)
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setDeleted
argument_list|(
literal|579
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setPartialFlag
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicCRUD
parameter_list|()
block|{
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|long
index|[]
name|procs
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|}
decl_stmt|;
name|tracker
operator|.
name|insert
argument_list|(
name|procs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|insert
argument_list|(
name|procs
index|[
literal|1
index|]
argument_list|,
operator|new
name|long
index|[]
block|{
name|procs
index|[
literal|2
index|]
block|,
name|procs
index|[
literal|3
index|]
block|,
name|procs
index|[
literal|4
index|]
block|}
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isAllModified
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|resetModified
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isAllModified
argument_list|()
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
literal|4
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|update
argument_list|(
name|procs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isAllModified
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|tracker
operator|.
name|update
argument_list|(
name|procs
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isAllModified
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|update
argument_list|(
name|procs
index|[
literal|5
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isAllModified
argument_list|()
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
literal|5
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|delete
argument_list|(
name|procs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isAllModified
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|tracker
operator|.
name|delete
argument_list|(
name|procs
index|[
literal|5
index|]
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandLoad
parameter_list|()
block|{
specifier|final
name|int
name|NPROCEDURES
init|=
literal|2500
decl_stmt|;
specifier|final
name|int
name|NRUNS
init|=
literal|5000
decl_stmt|;
specifier|final
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|1
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
name|NRUNS
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|count
operator|<
name|NPROCEDURES
condition|)
block|{
name|long
name|procId
init|=
name|rand
operator|.
name|nextLong
argument_list|()
decl_stmt|;
if|if
condition|(
name|procId
operator|<
literal|1
condition|)
block|{
continue|continue;
block|}
name|tracker
operator|.
name|setDeleted
argument_list|(
name|procId
argument_list|,
name|i
operator|%
literal|2
operator|==
literal|0
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|tracker
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLoad
parameter_list|()
block|{
specifier|final
name|int
name|MAX_PROCS
init|=
literal|1000
decl_stmt|;
specifier|final
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|numProcs
init|=
literal|1
init|;
name|numProcs
operator|<
name|MAX_PROCS
condition|;
operator|++
name|numProcs
control|)
block|{
for|for
control|(
name|int
name|start
init|=
literal|1
init|;
name|start
operator|<=
name|numProcs
condition|;
operator|++
name|start
control|)
block|{
name|assertTrue
argument_list|(
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"loading "
operator|+
name|numProcs
operator|+
literal|" procs from start="
operator|+
name|start
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<=
name|numProcs
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|setDeleted
argument_list|(
name|i
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|start
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|setDeleted
argument_list|(
name|i
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|tracker
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelete
parameter_list|()
block|{
specifier|final
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
name|long
index|[]
name|procIds
init|=
operator|new
name|long
index|[]
block|{
literal|65
block|,
literal|1
block|,
literal|193
block|}
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
name|procIds
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|insert
argument_list|(
name|procIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|dump
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
literal|64
operator|*
literal|4
operator|)
condition|;
operator|++
name|i
control|)
block|{
name|boolean
name|hasProc
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|procIds
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
if|if
condition|(
name|procIds
index|[
name|j
index|]
operator|==
name|i
condition|)
block|{
name|hasProc
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|hasProc
condition|)
block|{
name|assertEquals
argument_list|(
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|NO
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"procId="
operator|+
name|i
argument_list|,
name|ProcedureStoreTracker
operator|.
name|DeleteState
operator|.
name|YES
argument_list|,
name|tracker
operator|.
name|isDeleted
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetDeletedIfModified
parameter_list|()
block|{
specifier|final
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
specifier|final
name|long
index|[]
name|procIds
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|7
block|,
literal|152
block|,
literal|512
block|,
literal|1024
block|,
literal|1025
block|}
decl_stmt|;
comment|// test single proc
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|procIds
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|insert
argument_list|(
name|procIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|tracker
operator|.
name|isEmpty
argument_list|()
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
name|procIds
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|setDeletedIfModified
argument_list|(
name|procIds
index|[
name|i
index|]
operator|-
literal|1
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setDeletedIfModified
argument_list|(
name|procIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setDeletedIfModified
argument_list|(
name|procIds
index|[
name|i
index|]
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// test batch
name|tracker
operator|.
name|reset
argument_list|()
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
name|procIds
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|tracker
operator|.
name|insert
argument_list|(
name|procIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setDeletedIfModified
argument_list|(
name|procIds
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|tracker
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetActiveProcIds
parameter_list|()
block|{
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
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
literal|10000
condition|;
name|i
operator|++
control|)
block|{
name|tracker
operator|.
name|insert
argument_list|(
name|i
operator|*
literal|10
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10000
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|tracker
operator|.
name|delete
argument_list|(
name|i
operator|*
literal|10
argument_list|)
expr_stmt|;
block|}
name|long
index|[]
name|activeProcIds
init|=
name|tracker
operator|.
name|getAllActiveProcIds
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|5000
argument_list|,
name|activeProcIds
operator|.
name|length
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
literal|5000
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
operator|(
literal|2
operator|*
name|i
operator|+
literal|1
operator|)
operator|*
literal|10
argument_list|,
name|activeProcIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetActiveMinProcId
parameter_list|()
block|{
name|ProcedureStoreTracker
name|tracker
init|=
operator|new
name|ProcedureStoreTracker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|,
name|tracker
operator|.
name|getActiveMinProcId
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|100
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|=
literal|2
operator|*
name|i
operator|+
literal|1
control|)
block|{
name|tracker
operator|.
name|insert
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|100
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|=
literal|2
operator|*
name|i
operator|+
literal|1
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|tracker
operator|.
name|getActiveMinProcId
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|delete
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|,
name|tracker
operator|.
name|getActiveMinProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
