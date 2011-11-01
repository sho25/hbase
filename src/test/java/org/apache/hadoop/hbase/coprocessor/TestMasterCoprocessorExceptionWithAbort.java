begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|io
operator|.
name|InterruptedIOException
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
name|MiniHBaseCluster
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
name|HBaseAdmin
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
name|CoprocessorEnvironment
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
name|HMaster
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
name|MasterCoprocessorHost
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
name|zookeeper
operator|.
name|ZooKeeperNodeTracker
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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

begin_comment
comment|/**  * Tests unhandled exceptions thrown by coprocessors running on master.  * Expected result is that the master will abort with an informative  * error message describing the set of its loaded coprocessors for crash diagnosis.  * (HBASE-4014).  */
end_comment

begin_class
specifier|public
class|class
name|TestMasterCoprocessorExceptionWithAbort
block|{
specifier|public
specifier|static
class|class
name|MasterTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
specifier|public
name|boolean
name|masterZKNodeWasDeleted
init|=
literal|false
decl_stmt|;
specifier|public
name|MasterTracker
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|masterNode
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|zkw
argument_list|,
name|masterNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
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
literal|"/hbase/master"
argument_list|)
condition|)
block|{
name|masterZKNodeWasDeleted
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|CreateTableThread
extends|extends
name|Thread
block|{
name|HBaseTestingUtility
name|UTIL
decl_stmt|;
specifier|public
name|CreateTableThread
parameter_list|(
name|HBaseTestingUtility
name|UTIL
parameter_list|)
block|{
name|this
operator|.
name|UTIL
operator|=
name|UTIL
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// create a table : master coprocessor will throw an exception and not
comment|// catch it.
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"BuggyMasterObserver failed to throw an exception."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"HBaseAdmin threw an interrupted IOException as expected."
argument_list|,
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|"java.io.InterruptedIOException"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|BuggyMasterObserver
extends|extends
name|BaseMasterObserver
block|{
specifier|private
name|boolean
name|preCreateTableCalled
decl_stmt|;
specifier|private
name|boolean
name|postCreateTableCalled
decl_stmt|;
specifier|private
name|boolean
name|startCalled
decl_stmt|;
specifier|private
name|boolean
name|postStartMasterCalled
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|postCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// cause a NullPointerException and don't catch it: this will cause the
comment|// master to abort().
name|Integer
name|i
decl_stmt|;
name|i
operator|=
literal|null
expr_stmt|;
name|i
operator|=
name|i
operator|++
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasCreateTableCalled
parameter_list|()
block|{
return|return
name|preCreateTableCalled
operator|&&
name|postCreateTableCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postStartMaster
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|postStartMasterCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasStartMasterCalled
parameter_list|()
block|{
return|return
name|postStartMasterCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|startCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasStarted
parameter_list|()
block|{
return|return
name|startCalled
return|;
block|}
block|}
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"observed_table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
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
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|BuggyMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.abortonerror"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testExceptionFromCoprocessorWhenCreatingTable
parameter_list|()
throws|throws
name|IOException
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|MasterCoprocessorHost
name|host
init|=
name|master
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|BuggyMasterObserver
name|cp
init|=
operator|(
name|BuggyMasterObserver
operator|)
name|host
operator|.
name|findCoprocessor
argument_list|(
name|BuggyMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"No table created yet"
argument_list|,
name|cp
operator|.
name|wasCreateTableCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// set a watch on the zookeeper /hbase/master node. If the master dies,
comment|// the node will be deleted.
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"unittest"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Fatal ZK error: "
operator|+
name|why
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
argument_list|)
decl_stmt|;
name|MasterTracker
name|masterTracker
init|=
operator|new
name|MasterTracker
argument_list|(
name|zkw
argument_list|,
literal|"/hbase/master"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Fatal ZK master tracker error, why="
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
argument_list|)
decl_stmt|;
name|masterTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|zkw
operator|.
name|registerListener
argument_list|(
name|masterTracker
argument_list|)
expr_stmt|;
comment|// Test (part of the) output that should have be printed by master when it aborts:
comment|// (namely the part that shows the set of loaded coprocessors).
comment|// In this test, there is only a single coprocessor (BuggyMasterObserver).
name|assertTrue
argument_list|(
name|master
operator|.
name|getLoadedCoprocessors
argument_list|()
operator|.
name|equals
argument_list|(
literal|"["
operator|+
name|TestMasterCoprocessorExceptionWithAbort
operator|.
name|BuggyMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
name|CreateTableThread
name|createTableThread
init|=
operator|new
name|CreateTableThread
argument_list|(
name|UTIL
argument_list|)
decl_stmt|;
comment|// Attempting to create a table (using createTableThread above) triggers an NPE in BuggyMasterObserver.
comment|// Master will then abort and the /hbase/master zk node will be deleted.
name|createTableThread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Wait up to 30 seconds for master's /hbase/master zk node to go away after master aborts.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|masterTracker
operator|.
name|masterZKNodeWasDeleted
operator|==
literal|true
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"InterruptedException while waiting for master zk node to "
operator|+
literal|"be deleted."
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Master aborted on coprocessor exception, as expected."
argument_list|,
name|masterTracker
operator|.
name|masterZKNodeWasDeleted
argument_list|)
expr_stmt|;
name|createTableThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
try|try
block|{
name|createTableThread
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Ignoring InterruptedException while waiting for "
operator|+
literal|" createTableThread.join()."
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

