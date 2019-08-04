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
name|zookeeper
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
name|assertNotNull
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|Abortable
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
name|HBaseZKTestingUtility
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
name|Stoppable
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
name|log
operator|.
name|HBaseMarkers
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
name|ZKTests
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
name|Threads
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
name|BeforeClass
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
name|ZKTests
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
name|TestZKLeaderManager
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
name|TestZKLeaderManager
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
name|TestZKLeaderManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LEADER_ZNODE
init|=
literal|"/test/"
operator|+
name|TestZKLeaderManager
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
class|class
name|MockAbortable
implements|implements
name|Abortable
block|{
specifier|private
name|boolean
name|aborted
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|aborted
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|HBaseMarkers
operator|.
name|FATAL
argument_list|,
literal|"Aborting during test: "
operator|+
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Aborted during test: "
operator|+
name|why
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|aborted
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MockLeader
extends|extends
name|Thread
implements|implements
name|Stoppable
block|{
specifier|private
specifier|volatile
name|boolean
name|stopped
decl_stmt|;
specifier|private
name|ZKWatcher
name|watcher
decl_stmt|;
specifier|private
name|ZKLeaderManager
name|zkLeader
decl_stmt|;
specifier|private
name|AtomicBoolean
name|master
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|int
name|index
decl_stmt|;
name|MockLeader
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setName
argument_list|(
literal|"TestZKLeaderManager-leader-"
operator|+
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|watcher
operator|=
name|watcher
expr_stmt|;
name|this
operator|.
name|zkLeader
operator|=
operator|new
name|ZKLeaderManager
argument_list|(
name|watcher
argument_list|,
name|LEADER_ZNODE
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|index
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMaster
parameter_list|()
block|{
return|return
name|master
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|int
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
specifier|public
name|ZKWatcher
name|getWatcher
parameter_list|()
block|{
return|return
name|watcher
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
name|zkLeader
operator|.
name|start
argument_list|()
expr_stmt|;
name|zkLeader
operator|.
name|waitToBecomeLeader
argument_list|()
expr_stmt|;
name|master
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
while|while
condition|(
name|master
operator|.
name|get
argument_list|()
operator|&&
operator|!
name|stopped
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignored
parameter_list|)
block|{}
block|}
block|}
block|}
name|void
name|abdicate
parameter_list|()
block|{
name|zkLeader
operator|.
name|stepDownAsLeader
argument_list|()
expr_stmt|;
name|master
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|stopped
operator|=
literal|true
expr_stmt|;
name|abdicate
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|stopped
return|;
block|}
block|}
specifier|private
specifier|static
name|HBaseZKTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|static
name|MockLeader
index|[]
name|CANDIDATES
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseZKTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// use an abortable to fail the test in the case of any KeeperExceptions
name|MockAbortable
name|abortable
init|=
operator|new
name|MockAbortable
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|5
decl_stmt|;
name|CANDIDATES
operator|=
operator|new
name|MockLeader
index|[
name|count
index|]
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|ZKWatcher
name|watcher
init|=
name|newZK
argument_list|(
name|conf
argument_list|,
literal|"server"
operator|+
name|i
argument_list|,
name|abortable
argument_list|)
decl_stmt|;
name|CANDIDATES
index|[
name|i
index|]
operator|=
operator|new
name|MockLeader
argument_list|(
name|watcher
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|CANDIDATES
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLeaderSelection
parameter_list|()
throws|throws
name|Exception
block|{
name|MockLeader
name|currentLeader
init|=
name|getCurrentLeader
argument_list|()
decl_stmt|;
comment|// one leader should have been found
name|assertNotNull
argument_list|(
literal|"Leader should exist"
argument_list|,
name|currentLeader
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current leader index is "
operator|+
name|currentLeader
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|znodeData
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|currentLeader
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|LEADER_ZNODE
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Leader znode should contain leader index"
argument_list|,
name|znodeData
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Leader znode should not be empty"
argument_list|,
name|znodeData
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|int
name|storedIndex
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|znodeData
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stored leader index in ZK is "
operator|+
name|storedIndex
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Leader znode should match leader index"
argument_list|,
name|currentLeader
operator|.
name|getIndex
argument_list|()
argument_list|,
name|storedIndex
argument_list|)
expr_stmt|;
comment|// force a leader transition
name|currentLeader
operator|.
name|abdicate
argument_list|()
expr_stmt|;
comment|// check for new leader
name|currentLeader
operator|=
name|getCurrentLeader
argument_list|()
expr_stmt|;
comment|// one leader should have been found
name|assertNotNull
argument_list|(
literal|"New leader should exist after abdication"
argument_list|,
name|currentLeader
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New leader index is "
operator|+
name|currentLeader
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|znodeData
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|currentLeader
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|LEADER_ZNODE
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Leader znode should contain leader index"
argument_list|,
name|znodeData
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Leader znode should not be empty"
argument_list|,
name|znodeData
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|storedIndex
operator|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|znodeData
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stored leader index in ZK is "
operator|+
name|storedIndex
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Leader znode should match leader index"
argument_list|,
name|currentLeader
operator|.
name|getIndex
argument_list|()
argument_list|,
name|storedIndex
argument_list|)
expr_stmt|;
comment|// force another transition by stopping the current
name|currentLeader
operator|.
name|stop
argument_list|(
literal|"Stopping for test"
argument_list|)
expr_stmt|;
comment|// check for new leader
name|currentLeader
operator|=
name|getCurrentLeader
argument_list|()
expr_stmt|;
comment|// one leader should have been found
name|assertNotNull
argument_list|(
literal|"New leader should exist after stop"
argument_list|,
name|currentLeader
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New leader index is "
operator|+
name|currentLeader
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|znodeData
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|currentLeader
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|LEADER_ZNODE
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Leader znode should contain leader index"
argument_list|,
name|znodeData
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Leader znode should not be empty"
argument_list|,
name|znodeData
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|storedIndex
operator|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|znodeData
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stored leader index in ZK is "
operator|+
name|storedIndex
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Leader znode should match leader index"
argument_list|,
name|currentLeader
operator|.
name|getIndex
argument_list|()
argument_list|,
name|storedIndex
argument_list|)
expr_stmt|;
comment|// with a second stop we can guarantee that a previous leader has resumed leading
name|currentLeader
operator|.
name|stop
argument_list|(
literal|"Stopping for test"
argument_list|)
expr_stmt|;
comment|// check for new
name|currentLeader
operator|=
name|getCurrentLeader
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"New leader should exist"
argument_list|,
name|currentLeader
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MockLeader
name|getCurrentLeader
parameter_list|()
block|{
name|MockLeader
name|currentLeader
init|=
literal|null
decl_stmt|;
comment|// Wait up to 10 secs for initial leader
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|CANDIDATES
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|CANDIDATES
index|[
name|j
index|]
operator|.
name|isMaster
argument_list|()
condition|)
block|{
comment|// should only be one leader
if|if
condition|(
name|currentLeader
operator|!=
literal|null
condition|)
block|{
name|fail
argument_list|(
literal|"Both candidate "
operator|+
name|currentLeader
operator|.
name|getIndex
argument_list|()
operator|+
literal|" and "
operator|+
name|j
operator|+
literal|" claim to be leader!"
argument_list|)
expr_stmt|;
block|}
name|currentLeader
operator|=
name|CANDIDATES
index|[
name|j
index|]
expr_stmt|;
block|}
block|}
if|if
condition|(
name|currentLeader
operator|!=
literal|null
condition|)
block|{
break|break;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
return|return
name|currentLeader
return|;
block|}
specifier|private
specifier|static
name|ZKWatcher
name|newZK
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|Abortable
name|abort
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|copy
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
operator|new
name|ZKWatcher
argument_list|(
name|copy
argument_list|,
name|name
argument_list|,
name|abort
argument_list|)
return|;
block|}
block|}
end_class

end_unit

