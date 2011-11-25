begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|util
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
name|hbase
operator|.
name|SmallTests
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestThreads
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
name|TestThreads
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_TIME_MS
init|=
literal|5000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TOLERANCE_MS
init|=
call|(
name|int
call|)
argument_list|(
literal|0.05
operator|*
name|SLEEP_TIME_MS
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|wasInterrupted
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|6000
argument_list|)
specifier|public
name|void
name|testSleepWithoutInterrupt
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Thread
name|sleeper
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Sleeper thread: sleeping for "
operator|+
name|SLEEP_TIME_MS
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
name|SLEEP_TIME_MS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Sleeper thread: finished sleeping"
argument_list|)
expr_stmt|;
name|wasInterrupted
operator|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|isInterrupted
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting sleeper thread ("
operator|+
name|SLEEP_TIME_MS
operator|+
literal|" ms)"
argument_list|)
expr_stmt|;
name|sleeper
operator|.
name|start
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Main thread: sleeping for 500 ms"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupting the sleeper thread and sleeping for 2000 ms"
argument_list|)
expr_stmt|;
name|sleeper
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupting the sleeper thread and sleeping for 1000 ms"
argument_list|)
expr_stmt|;
name|sleeper
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupting the sleeper thread again"
argument_list|)
expr_stmt|;
name|sleeper
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|sleeper
operator|.
name|join
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"sleepWithoutInterrupt did not preserve the thread's "
operator|+
literal|"interrupted status"
argument_list|,
name|wasInterrupted
argument_list|)
expr_stmt|;
name|long
name|timeElapsed
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Elapsed time "
operator|+
name|timeElapsed
operator|+
literal|" ms is out of the expected "
operator|+
literal|"range of the sleep time "
operator|+
name|SLEEP_TIME_MS
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|timeElapsed
operator|-
name|SLEEP_TIME_MS
argument_list|)
operator|<
name|TOLERANCE_MS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Target sleep time: "
operator|+
name|SLEEP_TIME_MS
operator|+
literal|", time elapsed: "
operator|+
name|timeElapsed
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

