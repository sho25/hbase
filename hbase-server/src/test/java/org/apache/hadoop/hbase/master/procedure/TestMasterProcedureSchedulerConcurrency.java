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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|HBaseConfiguration
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
name|MemoryCompactionPolicy
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
name|master
operator|.
name|procedure
operator|.
name|TestMasterProcedureScheduler
operator|.
name|TestTableProcedure
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
name|regionserver
operator|.
name|CompactingMemStore
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
name|testclassification
operator|.
name|MasterTests
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
name|TestMasterProcedureSchedulerConcurrency
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
name|TestMasterProcedureSchedulerConcurrency
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MasterProcedureScheduler
name|queue
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_TYPE_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|MemoryCompactionPolicy
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|MasterProcedureScheduler
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|queue
operator|.
name|start
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
name|IOException
block|{
name|assertEquals
argument_list|(
literal|"proc-queue expected to be empty"
argument_list|,
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|stop
argument_list|()
expr_stmt|;
name|queue
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that "write" operations for a single table are serialized,    * but different tables can be executed in parallel.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testConcurrentWriteOps
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TestTableProcSet
name|procSet
init|=
operator|new
name|TestTableProcSet
argument_list|(
name|queue
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NUM_ITEMS
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|NUM_TABLES
init|=
literal|4
decl_stmt|;
specifier|final
name|AtomicInteger
name|opsCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
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
name|NUM_TABLES
condition|;
operator|++
name|i
control|)
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"testtb-%04d"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|NUM_ITEMS
condition|;
operator|++
name|j
control|)
block|{
name|procSet
operator|.
name|addBack
argument_list|(
operator|new
name|TestTableProcedure
argument_list|(
name|i
operator|*
literal|100
operator|+
name|j
argument_list|,
name|tableName
argument_list|,
name|TableProcedureInterface
operator|.
name|TableOperationType
operator|.
name|EDIT
argument_list|)
argument_list|)
expr_stmt|;
name|opsCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|opsCount
operator|.
name|get
argument_list|()
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|NUM_TABLES
operator|*
literal|2
index|]
decl_stmt|;
specifier|final
name|HashSet
argument_list|<
name|TableName
argument_list|>
name|concurrentTables
init|=
operator|new
name|HashSet
argument_list|<
name|TableName
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|ArrayList
argument_list|<
name|String
argument_list|>
name|failures
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|concurrentCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
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
name|threads
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|opsCount
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|Procedure
name|proc
init|=
name|procSet
operator|.
name|acquire
argument_list|()
decl_stmt|;
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
name|queue
operator|.
name|signalAll
argument_list|()
expr_stmt|;
if|if
condition|(
name|opsCount
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
continue|continue;
block|}
break|break;
block|}
name|TableName
name|tableId
init|=
name|procSet
operator|.
name|getTableName
argument_list|(
name|proc
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|concurrentTables
init|)
block|{
name|assertTrue
argument_list|(
literal|"unexpected concurrency on "
operator|+
name|tableId
argument_list|,
name|concurrentTables
operator|.
name|add
argument_list|(
name|tableId
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|opsCount
operator|.
name|decrementAndGet
argument_list|()
operator|>=
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
name|long
name|procId
init|=
name|proc
operator|.
name|getProcId
argument_list|()
decl_stmt|;
name|int
name|concurrent
init|=
name|concurrentCount
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"inc-concurrent="
operator|+
name|concurrent
operator|+
literal|" 1<= concurrent<= "
operator|+
name|NUM_TABLES
argument_list|,
name|concurrent
operator|>=
literal|1
operator|&&
name|concurrent
operator|<=
name|NUM_TABLES
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"[S] tableId="
operator|+
name|tableId
operator|+
literal|" procId="
operator|+
name|procId
operator|+
literal|" concurrent="
operator|+
name|concurrent
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|concurrent
operator|=
name|concurrentCount
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"[E] tableId="
operator|+
name|tableId
operator|+
literal|" procId="
operator|+
name|procId
operator|+
literal|" concurrent="
operator|+
name|concurrent
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"dec-concurrent="
operator|+
name|concurrent
argument_list|,
name|concurrent
operator|<
name|NUM_TABLES
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
synchronized|synchronized
init|(
name|concurrentTables
init|)
block|{
name|assertTrue
argument_list|(
name|concurrentTables
operator|.
name|remove
argument_list|(
name|tableId
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|procSet
operator|.
name|release
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|failures
init|)
block|{
name|failures
operator|.
name|add
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|queue
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
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
name|threads
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|failures
operator|.
name|toString
argument_list|()
argument_list|,
name|failures
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|opsCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_TABLES
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"testtb-%04d"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|TestTableProcedure
name|dummyProc
init|=
operator|new
name|TestTableProcedure
argument_list|(
literal|100
argument_list|,
name|table
argument_list|,
name|TableProcedureInterface
operator|.
name|TableOperationType
operator|.
name|DELETE
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"queue should be deleted, table="
operator|+
name|table
argument_list|,
name|queue
operator|.
name|markTableAsDeleted
argument_list|(
name|table
argument_list|,
name|dummyProc
argument_list|)
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
name|testMasterProcedureSchedulerPerformanceEvaluation
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Make sure the tool does not get stuck
name|MasterProcedureSchedulerPerformanceEvaluation
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-num_ops"
block|,
literal|"1000"
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestTableProcSet
block|{
specifier|private
specifier|final
name|MasterProcedureScheduler
name|queue
decl_stmt|;
specifier|public
name|TestTableProcSet
parameter_list|(
specifier|final
name|MasterProcedureScheduler
name|queue
parameter_list|)
block|{
name|this
operator|.
name|queue
operator|=
name|queue
expr_stmt|;
block|}
specifier|public
name|void
name|addBack
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
name|queue
operator|.
name|addBack
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addFront
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
name|queue
operator|.
name|addFront
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Procedure
name|acquire
parameter_list|()
block|{
name|Procedure
name|proc
init|=
literal|null
decl_stmt|;
name|boolean
name|waiting
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|waiting
operator|&&
name|queue
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|proc
operator|=
name|queue
operator|.
name|poll
argument_list|(
literal|100000000L
argument_list|)
expr_stmt|;
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
continue|continue;
switch|switch
condition|(
name|getTableOperationType
argument_list|(
name|proc
argument_list|)
condition|)
block|{
case|case
name|CREATE
case|:
case|case
name|DELETE
case|:
case|case
name|EDIT
case|:
name|waiting
operator|=
name|queue
operator|.
name|waitTableExclusiveLock
argument_list|(
name|proc
argument_list|,
name|getTableName
argument_list|(
name|proc
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|READ
case|:
name|waiting
operator|=
name|queue
operator|.
name|waitTableSharedLock
argument_list|(
name|proc
argument_list|,
name|getTableName
argument_list|(
name|proc
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|return
name|proc
return|;
block|}
specifier|public
name|void
name|release
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
switch|switch
condition|(
name|getTableOperationType
argument_list|(
name|proc
argument_list|)
condition|)
block|{
case|case
name|CREATE
case|:
case|case
name|DELETE
case|:
case|case
name|EDIT
case|:
name|queue
operator|.
name|wakeTableExclusiveLock
argument_list|(
name|proc
argument_list|,
name|getTableName
argument_list|(
name|proc
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|READ
case|:
name|queue
operator|.
name|wakeTableSharedLock
argument_list|(
name|proc
argument_list|,
name|getTableName
argument_list|(
name|proc
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
return|return
operator|(
operator|(
name|TableProcedureInterface
operator|)
name|proc
operator|)
operator|.
name|getTableName
argument_list|()
return|;
block|}
specifier|public
name|TableProcedureInterface
operator|.
name|TableOperationType
name|getTableOperationType
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
return|return
operator|(
operator|(
name|TableProcedureInterface
operator|)
name|proc
operator|)
operator|.
name|getTableOperationType
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

