begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNull
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
name|Random
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
name|Semaphore
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
name|master
operator|.
name|TestActiveMasterManager
operator|.
name|NodeDeletionListener
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
name|apache
operator|.
name|zookeeper
operator|.
name|CreateMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|Watcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
operator|.
name|Ids
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestZooKeeperNodeTracker
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
name|TestZooKeeperNodeTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
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
comment|/**    * Test that we can interrupt a node that is blocked on a wait.    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testInterruptible
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Abortable
name|abortable
init|=
operator|new
name|StubAbortable
argument_list|()
decl_stmt|;
name|ZooKeeperWatcher
name|zk
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testInterruptible"
argument_list|,
name|abortable
argument_list|)
decl_stmt|;
specifier|final
name|TestTracker
name|tracker
init|=
operator|new
name|TestTracker
argument_list|(
name|zk
argument_list|,
literal|"/xyz"
argument_list|,
name|abortable
argument_list|)
decl_stmt|;
name|tracker
operator|.
name|start
argument_list|()
expr_stmt|;
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
try|try
block|{
name|tracker
operator|.
name|blockUntilAvailable
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|t
operator|.
name|isAlive
argument_list|()
condition|)
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|stop
argument_list|()
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// If it wasn't interruptible, we'd never get to here.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNodeTracker
parameter_list|()
throws|throws
name|Exception
block|{
name|Abortable
name|abortable
init|=
operator|new
name|StubAbortable
argument_list|()
decl_stmt|;
name|ZooKeeperWatcher
name|zk
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"testNodeTracker"
argument_list|,
name|abortable
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zk
argument_list|,
name|zk
operator|.
name|baseZNode
argument_list|)
expr_stmt|;
specifier|final
name|String
name|node
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zk
operator|.
name|baseZNode
argument_list|,
operator|new
name|Long
argument_list|(
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|dataOne
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dataOne"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|dataTwo
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dataTwo"
argument_list|)
decl_stmt|;
comment|// Start a ZKNT with no node currently available
name|TestTracker
name|localTracker
init|=
operator|new
name|TestTracker
argument_list|(
name|zk
argument_list|,
name|node
argument_list|,
name|abortable
argument_list|)
decl_stmt|;
name|localTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|localTracker
argument_list|)
expr_stmt|;
comment|// Make sure we don't have a node
name|assertNull
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
comment|// Spin up a thread with another ZKNT and have it block
name|WaitToGetDataThread
name|thread
init|=
operator|new
name|WaitToGetDataThread
argument_list|(
name|zk
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Verify the thread doesn't have a node
name|assertFalse
argument_list|(
name|thread
operator|.
name|hasData
argument_list|)
expr_stmt|;
comment|// Now, start a new ZKNT with the node already available
name|TestTracker
name|secondTracker
init|=
operator|new
name|TestTracker
argument_list|(
name|zk
argument_list|,
name|node
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|secondTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|secondTracker
argument_list|)
expr_stmt|;
comment|// Put up an additional zk listener so we know when zk event is done
name|TestingZKListener
name|zkListener
init|=
operator|new
name|TestingZKListener
argument_list|(
name|zk
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|zkListener
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|zkListener
operator|.
name|createdLock
operator|.
name|availablePermits
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create a completely separate zk connection for test triggers and avoid
comment|// any weird watcher interactions from the test
specifier|final
name|ZooKeeper
name|zkconn
init|=
operator|new
name|ZooKeeper
argument_list|(
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
literal|60000
argument_list|,
operator|new
name|StubWatcher
argument_list|()
argument_list|)
decl_stmt|;
comment|// Add the node with data one
name|zkconn
operator|.
name|create
argument_list|(
name|node
argument_list|,
name|dataOne
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
comment|// Wait for the zk event to be processed
name|zkListener
operator|.
name|waitForCreation
argument_list|()
expr_stmt|;
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// Both trackers should have the node available with data one
name|assertNotNull
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|localTracker
operator|.
name|blockUntilAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataOne
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|thread
operator|.
name|hasData
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|thread
operator|.
name|tracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataOne
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully got data one"
argument_list|)
expr_stmt|;
comment|// Make sure it's available and with the expected data
name|assertNotNull
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|secondTracker
operator|.
name|blockUntilAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataOne
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully got data one with the second tracker"
argument_list|)
expr_stmt|;
comment|// Drop the node
name|zkconn
operator|.
name|delete
argument_list|(
name|node
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|zkListener
operator|.
name|waitForDeletion
argument_list|()
expr_stmt|;
comment|// Create a new thread but with the existing thread's tracker to wait
name|TestTracker
name|threadTracker
init|=
name|thread
operator|.
name|tracker
decl_stmt|;
name|thread
operator|=
operator|new
name|WaitToGetDataThread
argument_list|(
name|zk
argument_list|,
name|node
argument_list|,
name|threadTracker
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Verify other guys don't have data
name|assertFalse
argument_list|(
name|thread
operator|.
name|hasData
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully made unavailable"
argument_list|)
expr_stmt|;
comment|// Create with second data
name|zkconn
operator|.
name|create
argument_list|(
name|node
argument_list|,
name|dataTwo
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
comment|// Wait for the zk event to be processed
name|zkListener
operator|.
name|waitForCreation
argument_list|()
expr_stmt|;
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// All trackers should have the node available with data two
name|assertNotNull
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|localTracker
operator|.
name|blockUntilAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataTwo
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|secondTracker
operator|.
name|blockUntilAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataTwo
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|thread
operator|.
name|hasData
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|thread
operator|.
name|tracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataTwo
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully got data two on all trackers and threads"
argument_list|)
expr_stmt|;
comment|// Change the data back to data one
name|zkconn
operator|.
name|setData
argument_list|(
name|node
argument_list|,
name|dataOne
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// Wait for zk event to be processed
name|zkListener
operator|.
name|waitForDataChange
argument_list|()
expr_stmt|;
comment|// All trackers should have the node available with data one
name|assertNotNull
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|localTracker
operator|.
name|blockUntilAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|localTracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataOne
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|secondTracker
operator|.
name|blockUntilAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|secondTracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataOne
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|thread
operator|.
name|hasData
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|thread
operator|.
name|tracker
operator|.
name|getData
argument_list|()
argument_list|,
name|dataOne
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully got data one following a data change on all trackers and threads"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|WaitToGetDataThread
extends|extends
name|Thread
block|{
name|TestTracker
name|tracker
decl_stmt|;
name|boolean
name|hasData
decl_stmt|;
specifier|public
name|WaitToGetDataThread
parameter_list|(
name|ZooKeeperWatcher
name|zk
parameter_list|,
name|String
name|node
parameter_list|)
block|{
name|tracker
operator|=
operator|new
name|TestTracker
argument_list|(
name|zk
argument_list|,
name|node
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|zk
operator|.
name|registerListener
argument_list|(
name|tracker
argument_list|)
expr_stmt|;
name|hasData
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|WaitToGetDataThread
parameter_list|(
name|ZooKeeperWatcher
name|zk
parameter_list|,
name|String
name|node
parameter_list|,
name|TestTracker
name|tracker
parameter_list|)
block|{
name|this
operator|.
name|tracker
operator|=
name|tracker
expr_stmt|;
name|hasData
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for data to be available in WaitToGetDataThread"
argument_list|)
expr_stmt|;
try|try
block|{
name|tracker
operator|.
name|blockUntilAvailable
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Data now available in tracker from WaitToGetDataThread"
argument_list|)
expr_stmt|;
name|hasData
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
specifier|public
name|TestTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|node
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestingZKListener
extends|extends
name|ZooKeeperListener
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
name|NodeDeletionListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Semaphore
name|deletedLock
decl_stmt|;
specifier|private
name|Semaphore
name|createdLock
decl_stmt|;
specifier|private
name|Semaphore
name|changedLock
decl_stmt|;
specifier|private
name|String
name|node
decl_stmt|;
specifier|public
name|TestingZKListener
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|node
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|deletedLock
operator|=
operator|new
name|Semaphore
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|createdLock
operator|=
operator|new
name|Semaphore
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|changedLock
operator|=
operator|new
name|Semaphore
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"nodeDeleted("
operator|+
name|path
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|deletedLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"nodeCreated("
operator|+
name|path
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|createdLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDataChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"nodeDataChanged("
operator|+
name|path
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|changedLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|waitForDeletion
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|deletedLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|waitForCreation
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|createdLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|waitForDataChange
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|changedLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|StubAbortable
implements|implements
name|Abortable
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
specifier|final
name|String
name|msg
parameter_list|,
specifier|final
name|Throwable
name|t
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|StubWatcher
implements|implements
name|Watcher
block|{
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{}
block|}
block|}
end_class

end_unit

