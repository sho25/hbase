begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestTaskMonitor
block|{
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
argument_list|()
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
name|taskFromTm
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
operator|(
operator|(
name|MonitoredTaskImpl
operator|)
name|taskFromTm
operator|)
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
argument_list|()
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
name|TaskMonitor
operator|.
name|MAX_TASKS
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
name|MAX_TASKS
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
block|}
block|}
end_class

end_unit

