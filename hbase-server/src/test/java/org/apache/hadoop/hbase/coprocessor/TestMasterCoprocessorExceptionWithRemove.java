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
name|coprocessor
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
name|Optional
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
name|RegionInfo
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
name|testclassification
operator|.
name|CoprocessorTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKNodeTracker
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
name|ZKWatcher
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

begin_comment
comment|/**  * Tests unhandled exceptions thrown by coprocessors running on master.  * Expected result is that the master will remove the buggy coprocessor from  * its set of coprocessors and throw a org.apache.hadoop.hbase.exceptions.DoNotRetryIOException  * back to the client.  * (HBASE-4014).  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestMasterCoprocessorExceptionWithRemove
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
name|TestMasterCoprocessorExceptionWithRemove
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|MasterTracker
extends|extends
name|ZKNodeTracker
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
name|ZKWatcher
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
name|BuggyMasterObserver
implements|implements
name|MasterCoprocessor
implements|,
name|MasterObserver
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
name|Optional
argument_list|<
name|MasterObserver
argument_list|>
name|getMasterObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"null"
argument_list|)
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
name|TableDescriptor
name|desc
parameter_list|,
name|RegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Cause a NullPointerException and don't catch it: this should cause the
comment|// master to throw an o.apache.hadoop.hbase.DoNotRetryIOException to the
comment|// client.
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
name|TEST_TABLE1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"observed_table1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_FAMILY1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_TABLE2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_FAMILY2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam2"
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|CoprocessorHost
operator|.
name|ABORT_ON_ERROR_KEY
argument_list|,
literal|false
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
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
name|BuggyMasterObserver
name|cp
init|=
name|host
operator|.
name|findCoprocessor
argument_list|(
name|BuggyMasterObserver
operator|.
name|class
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
comment|// Set a watch on the zookeeper /hbase/master node. If the master dies,
comment|// the node will be deleted.
comment|// Master should *NOT* die:
comment|// we are testing that the default setting of hbase.coprocessor.abortonerror
comment|// =false
comment|// is respected.
name|ZKWatcher
name|zkw
init|=
operator|new
name|ZKWatcher
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
literal|"Fatal ZooKeeper tracker error, why="
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
name|String
name|coprocessorName
init|=
name|BuggyMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|HMaster
operator|.
name|getLoadedCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|coprocessorName
argument_list|)
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd1
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TEST_TABLE1
argument_list|)
argument_list|)
decl_stmt|;
name|htd1
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY1
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|threwDNRE
init|=
literal|false
decl_stmt|;
try|try
block|{
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"org.apache.hadoop.hbase.DoNotRetryIOException"
argument_list|)
condition|)
block|{
name|threwDNRE
operator|=
literal|true
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|threwDNRE
argument_list|)
expr_stmt|;
block|}
comment|// wait for a few seconds to make sure that the Master hasn't aborted.
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|3000
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
literal|"InterruptedException while sleeping."
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
literal|"Master survived coprocessor NPE, as expected."
argument_list|,
name|masterTracker
operator|.
name|masterZKNodeWasDeleted
argument_list|)
expr_stmt|;
name|String
name|loadedCoprocessors
init|=
name|HMaster
operator|.
name|getLoadedCoprocessors
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|loadedCoprocessors
operator|.
name|contains
argument_list|(
name|coprocessorName
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify that BuggyMasterObserver has been removed due to its misbehavior
comment|// by creating another table: should not have a problem this time.
name|HTableDescriptor
name|htd2
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TEST_TABLE2
argument_list|)
argument_list|)
decl_stmt|;
name|htd2
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY2
argument_list|)
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Failed to create table after buggy coprocessor removal: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

