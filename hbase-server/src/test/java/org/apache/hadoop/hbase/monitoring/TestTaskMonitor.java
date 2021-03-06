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
name|monitoring
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
name|*
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
name|List
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
name|AtomicBoolean
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
name|client
operator|.
name|Mutation
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
name|Put
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
name|Query
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
name|Scan
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
name|MiscTests
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
name|SmallTests
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
name|EnvironmentEdgeManager
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTaskMonitor
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
name|TestTaskMonitor
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testTaskMonitorBasics
parameter_list|()
block|{
name|TaskMonitor
name|tm
init|=
operator|new
name|TaskMonitor
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Task monitor should start empty"
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make a task and fetch it back out
name|MonitoredTask
name|task
init|=
name|tm
operator|.
name|createStatus
argument_list|(
literal|"Test task"
argument_list|)
decl_stmt|;
name|MonitoredTask
name|taskFromTm
init|=
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Make sure the state is reasonable.
name|assertEquals
argument_list|(
name|task
operator|.
name|getDescription
argument_list|()
argument_list|,
name|taskFromTm
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|taskFromTm
operator|.
name|getCompletionTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|MonitoredTask
operator|.
name|State
operator|.
name|RUNNING
argument_list|,
name|taskFromTm
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
comment|// Mark it as finished
name|task
operator|.
name|markComplete
argument_list|(
literal|"Finished!"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|MonitoredTask
operator|.
name|State
operator|.
name|COMPLETE
argument_list|,
name|task
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
comment|// It should still show up in the TaskMonitor list
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// If we mark its completion time back a few minutes, it should get gced
name|task
operator|.
name|expireNow
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTasksGetAbortedOnLeak
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|TaskMonitor
name|tm
init|=
operator|new
name|TaskMonitor
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Task monitor should start empty"
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|threadSuccess
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Make a task in some other thread and leak it
name|Thread
name|t
init|=
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
name|MonitoredTask
name|task
init|=
name|tm
operator|.
name|createStatus
argument_list|(
literal|"Test task"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|MonitoredTask
operator|.
name|State
operator|.
name|RUNNING
argument_list|,
name|task
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|threadSuccess
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// Make sure the thread saw the correct state
name|assertTrue
argument_list|(
name|threadSuccess
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure the leaked reference gets cleared
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
comment|// Now it should be aborted
name|MonitoredTask
name|taskFromTm
init|=
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|MonitoredTask
operator|.
name|State
operator|.
name|ABORTED
argument_list|,
name|taskFromTm
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTaskLimit
parameter_list|()
throws|throws
name|Exception
block|{
name|TaskMonitor
name|tm
init|=
operator|new
name|TaskMonitor
argument_list|(
operator|new
name|Configuration
argument_list|()
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
name|TaskMonitor
operator|.
name|DEFAULT_MAX_TASKS
operator|+
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|tm
operator|.
name|createStatus
argument_list|(
literal|"task "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
comment|// Make sure it was limited correctly
name|assertEquals
argument_list|(
name|TaskMonitor
operator|.
name|DEFAULT_MAX_TASKS
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure we culled the earlier tasks, not later
comment|// (i.e. tasks 0 through 9 should have been deleted)
name|assertEquals
argument_list|(
literal|"task 10"
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoNotPurgeRPCTask
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|RPCTaskNums
init|=
literal|10
decl_stmt|;
name|TaskMonitor
name|tm
init|=
name|TaskMonitor
operator|.
name|get
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
name|RPCTaskNums
condition|;
name|i
operator|++
control|)
block|{
name|tm
operator|.
name|createRPCStatus
argument_list|(
literal|"PRCTask"
operator|+
name|i
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
name|TaskMonitor
operator|.
name|DEFAULT_MAX_TASKS
condition|;
name|i
operator|++
control|)
block|{
name|tm
operator|.
name|createStatus
argument_list|(
literal|"otherTask"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|int
name|remainRPCTask
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MonitoredTask
name|task
range|:
name|tm
operator|.
name|getTasks
argument_list|()
control|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|MonitoredRPCHandler
condition|)
block|{
name|remainRPCTask
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"RPC Tasks have been purged!"
argument_list|,
name|RPCTaskNums
argument_list|,
name|remainRPCTask
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWarnStuckTasks
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|RPC_WARN_TIME
init|=
literal|1500
decl_stmt|;
specifier|final
name|int
name|MONITOR_INTERVAL
init|=
literal|500
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|TaskMonitor
operator|.
name|RPC_WARN_TIME_KEY
argument_list|,
name|RPC_WARN_TIME
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|TaskMonitor
operator|.
name|MONITOR_INTERVAL_KEY
argument_list|,
name|MONITOR_INTERVAL
argument_list|)
expr_stmt|;
specifier|final
name|TaskMonitor
name|tm
init|=
operator|new
name|TaskMonitor
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|MonitoredRPCHandler
name|t
init|=
name|tm
operator|.
name|createRPCStatus
argument_list|(
literal|"test task"
argument_list|)
decl_stmt|;
name|long
name|beforeSetRPC
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Validating initialization assumption"
argument_list|,
name|t
operator|.
name|getWarnTime
argument_list|()
operator|<=
name|beforeSetRPC
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|MONITOR_INTERVAL
operator|*
literal|2
argument_list|)
expr_stmt|;
name|t
operator|.
name|setRPC
argument_list|(
literal|"testMethod"
argument_list|,
operator|new
name|Object
index|[
literal|0
index|]
argument_list|,
name|beforeSetRPC
argument_list|)
expr_stmt|;
name|long
name|afterSetRPC
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|MONITOR_INTERVAL
operator|*
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Validating no warn after starting RPC"
argument_list|,
name|t
operator|.
name|getWarnTime
argument_list|()
operator|<=
name|afterSetRPC
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|MONITOR_INTERVAL
operator|*
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Validating warn after RPC_WARN_TIME"
argument_list|,
name|t
operator|.
name|getWarnTime
argument_list|()
operator|>
name|afterSetRPC
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTasksWithFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|TaskMonitor
name|tm
init|=
operator|new
name|TaskMonitor
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Task monitor should start empty"
argument_list|,
name|tm
operator|.
name|getTasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create 5 general tasks
name|tm
operator|.
name|createStatus
argument_list|(
literal|"General task1"
argument_list|)
expr_stmt|;
name|tm
operator|.
name|createStatus
argument_list|(
literal|"General task2"
argument_list|)
expr_stmt|;
name|tm
operator|.
name|createStatus
argument_list|(
literal|"General task3"
argument_list|)
expr_stmt|;
name|tm
operator|.
name|createStatus
argument_list|(
literal|"General task4"
argument_list|)
expr_stmt|;
name|tm
operator|.
name|createStatus
argument_list|(
literal|"General task5"
argument_list|)
expr_stmt|;
comment|// Create 5 rpc tasks, and mark 1 completed
name|int
name|length
init|=
literal|5
decl_stmt|;
name|ArrayList
argument_list|<
name|MonitoredRPCHandler
argument_list|>
name|rpcHandlers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|length
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MonitoredRPCHandler
name|rpcHandler
init|=
name|tm
operator|.
name|createRPCStatus
argument_list|(
literal|"Rpc task"
operator|+
name|i
argument_list|)
decl_stmt|;
name|rpcHandlers
operator|.
name|add
argument_list|(
name|rpcHandler
argument_list|)
expr_stmt|;
block|}
comment|// Create rpc opertions
name|byte
index|[]
name|row
init|=
operator|new
name|byte
index|[]
block|{
literal|0x01
block|}
decl_stmt|;
name|Mutation
name|m
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Query
name|q
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|String
name|notOperation
init|=
literal|"for test"
decl_stmt|;
name|rpcHandlers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|setRPC
argument_list|(
literal|"operations"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|m
block|,
name|q
block|}
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|rpcHandlers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|setRPC
argument_list|(
literal|"operations"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|m
block|,
name|q
block|}
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|rpcHandlers
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|setRPC
argument_list|(
literal|"operations"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|m
block|,
name|q
block|}
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|rpcHandlers
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|setRPC
argument_list|(
literal|"operations"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|notOperation
block|}
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|rpcHandlers
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|setRPC
argument_list|(
literal|"operations"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|m
block|,
name|q
block|}
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|MonitoredRPCHandler
name|completed
init|=
name|rpcHandlers
operator|.
name|get
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|completed
operator|.
name|markComplete
argument_list|(
literal|"Completed!"
argument_list|)
expr_stmt|;
comment|// Test get tasks with filter
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|generalTasks
init|=
name|tm
operator|.
name|getTasks
argument_list|(
literal|"general"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|generalTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|handlerTasks
init|=
name|tm
operator|.
name|getTasks
argument_list|(
literal|"handler"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|handlerTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|rpcTasks
init|=
name|tm
operator|.
name|getTasks
argument_list|(
literal|"rpc"
argument_list|)
decl_stmt|;
comment|// The last rpc handler is stopped
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|rpcTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|operationTasks
init|=
name|tm
operator|.
name|getTasks
argument_list|(
literal|"operation"
argument_list|)
decl_stmt|;
comment|// Handler 3 doesn't handle Operation.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|operationTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStatusJournal
parameter_list|()
block|{
name|TaskMonitor
name|tm
init|=
operator|new
name|TaskMonitor
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|MonitoredTask
name|task
init|=
name|tm
operator|.
name|createStatus
argument_list|(
literal|"Test task"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|disableStatusJournal
argument_list|()
expr_stmt|;
name|task
operator|.
name|setStatus
argument_list|(
literal|"status1"
argument_list|)
expr_stmt|;
comment|// journal should be empty since it is disabled
name|assertTrue
argument_list|(
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|enableStatusJournal
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// check existing status entered in journal
name|assertEquals
argument_list|(
literal|"status1"
argument_list|,
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimeStamp
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|task
operator|.
name|disableStatusJournal
argument_list|()
expr_stmt|;
name|task
operator|.
name|setStatus
argument_list|(
literal|"status2"
argument_list|)
expr_stmt|;
comment|// check status 2 not added since disabled
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|enableStatusJournal
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// size should still be 1 since we didn't include current status
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|setStatus
argument_list|(
literal|"status3"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"status3"
argument_list|,
name|task
operator|.
name|getStatusJournal
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|tm
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

