begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashMap
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
name|HConstants
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
name|ServerLoad
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
name|ServerName
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
name|Table
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
name|HRegion
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
name|Region
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|JVMClusterUtil
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
name|DrainingServerTracker
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
name|RegionServerTracker
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
name|ZKUtil
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
name|Assert
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
name|mockito
operator|.
name|Mockito
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
name|TestAssignmentListener
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
name|TestAssignmentListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Abortable
name|abortable
init|=
operator|new
name|Abortable
argument_list|()
block|{
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
block|{     }
block|}
decl_stmt|;
specifier|static
class|class
name|DummyListener
block|{
specifier|protected
name|AtomicInteger
name|modified
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|void
name|awaitModifications
parameter_list|(
name|int
name|count
parameter_list|)
throws|throws
name|InterruptedException
block|{
while|while
condition|(
operator|!
name|modified
operator|.
name|compareAndSet
argument_list|(
name|count
argument_list|,
literal|0
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|DummyAssignmentListener
extends|extends
name|DummyListener
implements|implements
name|AssignmentListener
block|{
specifier|private
name|AtomicInteger
name|closeCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|AtomicInteger
name|openCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|DummyAssignmentListener
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|regionOpened
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Assignment open region="
operator|+
name|regionInfo
operator|+
literal|" server="
operator|+
name|serverName
argument_list|)
expr_stmt|;
name|openCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|modified
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|regionClosed
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Assignment close region="
operator|+
name|regionInfo
argument_list|)
expr_stmt|;
name|closeCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|modified
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|openCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|closeCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getLoadCount
parameter_list|()
block|{
return|return
name|openCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|int
name|getCloseCount
parameter_list|()
block|{
return|return
name|closeCount
operator|.
name|get
argument_list|()
return|;
block|}
block|}
specifier|static
class|class
name|DummyServerListener
extends|extends
name|DummyListener
implements|implements
name|ServerListener
block|{
specifier|private
name|AtomicInteger
name|removedCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|AtomicInteger
name|addedCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|DummyServerListener
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|serverAdded
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Server added "
operator|+
name|serverName
argument_list|)
expr_stmt|;
name|addedCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|modified
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serverRemoved
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Server removed "
operator|+
name|serverName
argument_list|)
expr_stmt|;
name|removedCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|modified
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|addedCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|removedCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getAddedCount
parameter_list|()
block|{
return|return
name|addedCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|int
name|getRemovedCount
parameter_list|()
block|{
return|return
name|removedCount
operator|.
name|get
argument_list|()
return|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
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
literal|60000
argument_list|)
specifier|public
name|void
name|testServerListener
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ServerManager
name|serverManager
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
name|DummyServerListener
name|listener
init|=
operator|new
name|DummyServerListener
argument_list|()
decl_stmt|;
name|serverManager
operator|.
name|registerListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
try|try
block|{
name|MiniHBaseCluster
name|miniCluster
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
comment|// Start a new Region Server
name|miniCluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getAddedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|listener
operator|.
name|getRemovedCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Start another Region Server
name|listener
operator|.
name|reset
argument_list|()
expr_stmt|;
name|miniCluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getAddedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|listener
operator|.
name|getRemovedCount
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|nrs
init|=
name|miniCluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// Stop a Region Server
name|listener
operator|.
name|reset
argument_list|()
expr_stmt|;
name|miniCluster
operator|.
name|stopRegionServer
argument_list|(
name|nrs
operator|-
literal|1
argument_list|)
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|listener
operator|.
name|getAddedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getRemovedCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Stop another Region Server
name|listener
operator|.
name|reset
argument_list|()
expr_stmt|;
name|miniCluster
operator|.
name|stopRegionServer
argument_list|(
name|nrs
operator|-
literal|2
argument_list|)
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|listener
operator|.
name|getAddedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getRemovedCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|serverManager
operator|.
name|unregisterListener
argument_list|(
name|listener
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
name|testAssignmentListener
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|AssignmentManager
name|am
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|DummyAssignmentListener
name|listener
init|=
operator|new
name|DummyAssignmentListener
argument_list|()
decl_stmt|;
name|am
operator|.
name|registerListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|String
name|TABLE_NAME_STR
init|=
literal|"testtb"
decl_stmt|;
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME_STR
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
comment|// Create a new table, with a single region
name|LOG
operator|.
name|info
argument_list|(
literal|"Create Table"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getLoadCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|listener
operator|.
name|getCloseCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add some data
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-"
operator|+
name|i
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Split the table in two
name|LOG
operator|.
name|info
argument_list|(
literal|"Split Table"
argument_list|)
expr_stmt|;
name|listener
operator|.
name|reset
argument_list|()
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|TABLE_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-3"
argument_list|)
argument_list|)
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|listener
operator|.
name|getLoadCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// daughters added
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getCloseCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// parent removed
comment|// Wait for the Regions to be mergeable
name|MiniHBaseCluster
name|miniCluster
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
name|int
name|mergeable
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|mergeable
operator|<
literal|2
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|mergeable
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|regionThread
range|:
name|miniCluster
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
for|for
control|(
name|Region
name|region
range|:
name|regionThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
control|)
block|{
name|mergeable
operator|+=
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|isMergeable
argument_list|()
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
block|}
comment|// Merge the two regions
name|LOG
operator|.
name|info
argument_list|(
literal|"Merge Regions"
argument_list|)
expr_stmt|;
name|listener
operator|.
name|reset
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|sameServer
init|=
name|areAllRegionsLocatedOnSameServer
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// If the regions are located by different server, we need to move
comment|// regions to same server before merging. So the expected modifications
comment|// will increaes to 5. (open + close)
specifier|final
name|int
name|expectedModifications
init|=
name|sameServer
condition|?
literal|3
else|:
literal|5
decl_stmt|;
specifier|final
name|int
name|expectedLoadCount
init|=
name|sameServer
condition|?
literal|1
else|:
literal|2
decl_stmt|;
specifier|final
name|int
name|expectedCloseCount
init|=
name|sameServer
condition|?
literal|2
else|:
literal|3
decl_stmt|;
name|admin
operator|.
name|mergeRegionsAsync
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
name|expectedModifications
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|admin
operator|.
name|getTableRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedLoadCount
argument_list|,
name|listener
operator|.
name|getLoadCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// new merged region added
name|assertEquals
argument_list|(
name|expectedCloseCount
argument_list|,
name|listener
operator|.
name|getCloseCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// daughters removed
comment|// Delete the table
name|LOG
operator|.
name|info
argument_list|(
literal|"Drop Table"
argument_list|)
expr_stmt|;
name|listener
operator|.
name|reset
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|listener
operator|.
name|awaitModifications
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|listener
operator|.
name|getLoadCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|listener
operator|.
name|getCloseCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|am
operator|.
name|unregisterListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|areAllRegionsLocatedOnSameServer
parameter_list|(
name|TableName
name|TABLE_NAME
parameter_list|)
block|{
name|MiniHBaseCluster
name|miniCluster
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
name|int
name|serverCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|regionThread
range|:
name|miniCluster
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|regionThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
operator|++
name|serverCount
expr_stmt|;
block|}
if|if
condition|(
name|serverCount
operator|>
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
name|serverCount
operator|==
literal|1
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddNewServerThatExistsInDraining
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Under certain circumstances, such as when we failover to the Backup
comment|// HMaster, the DrainingServerTracker is started with existing servers in
comment|// draining before all of the Region Servers register with the
comment|// ServerManager as "online".  This test is to ensure that Region Servers
comment|// are properly added to the ServerManager.drainingServers when they
comment|// register with the ServerManager under these circumstances.
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|ZooKeeperWatcher
name|zooKeeper
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"zkWatcher-NewServerDrainTest"
argument_list|,
name|abortable
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|String
name|baseZNode
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
name|String
name|drainingZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.draining.rs"
argument_list|,
literal|"draining"
argument_list|)
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HMaster
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ServerName
name|SERVERNAME_A
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"mockserverbulk_a.org"
argument_list|,
literal|1000
argument_list|,
literal|8000
argument_list|)
decl_stmt|;
name|ServerName
name|SERVERNAME_B
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"mockserverbulk_b.org"
argument_list|,
literal|1001
argument_list|,
literal|8000
argument_list|)
decl_stmt|;
name|ServerName
name|SERVERNAME_C
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"mockserverbulk_c.org"
argument_list|,
literal|1002
argument_list|,
literal|8000
argument_list|)
decl_stmt|;
comment|// We'll start with 2 servers in draining that existed before the
comment|// HMaster started.
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|drainingServers
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|drainingServers
operator|.
name|add
argument_list|(
name|SERVERNAME_A
argument_list|)
expr_stmt|;
name|drainingServers
operator|.
name|add
argument_list|(
name|SERVERNAME_B
argument_list|)
expr_stmt|;
comment|// We'll have 2 servers that come online AFTER the DrainingServerTracker
comment|// is started (just as we see when we failover to the Backup HMaster).
comment|// One of these will already be a draining server.
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|onlineServers
init|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
argument_list|()
decl_stmt|;
name|onlineServers
operator|.
name|put
argument_list|(
name|SERVERNAME_A
argument_list|,
name|ServerLoad
operator|.
name|EMPTY_SERVERLOAD
argument_list|)
expr_stmt|;
name|onlineServers
operator|.
name|put
argument_list|(
name|SERVERNAME_C
argument_list|,
name|ServerLoad
operator|.
name|EMPTY_SERVERLOAD
argument_list|)
expr_stmt|;
comment|// Create draining znodes for the draining servers, which would have been
comment|// performed when the previous HMaster was running.
for|for
control|(
name|ServerName
name|sn
range|:
name|drainingServers
control|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|drainingZNode
argument_list|,
name|sn
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zooKeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
comment|// Now, we follow the same order of steps that the HMaster does to setup
comment|// the ServerManager, RegionServerTracker, and DrainingServerTracker.
name|ServerManager
name|serverManager
init|=
operator|new
name|ServerManager
argument_list|(
name|master
argument_list|)
decl_stmt|;
name|RegionServerTracker
name|regionServerTracker
init|=
operator|new
name|RegionServerTracker
argument_list|(
name|zooKeeper
argument_list|,
name|master
argument_list|,
name|serverManager
argument_list|)
decl_stmt|;
name|regionServerTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|DrainingServerTracker
name|drainingServerTracker
init|=
operator|new
name|DrainingServerTracker
argument_list|(
name|zooKeeper
argument_list|,
name|master
argument_list|,
name|serverManager
argument_list|)
decl_stmt|;
name|drainingServerTracker
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Confirm our ServerManager lists are empty.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|serverManager
operator|.
name|getOnlineServers
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|serverManager
operator|.
name|getDrainingServersList
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
comment|// checkAndRecordNewServer() is how servers are added to the ServerManager.
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|onlineDrainingServers
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|onlineServers
operator|.
name|keySet
argument_list|()
control|)
block|{
comment|// Here's the actual test.
name|serverManager
operator|.
name|checkAndRecordNewServer
argument_list|(
name|sn
argument_list|,
name|onlineServers
operator|.
name|get
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|drainingServers
operator|.
name|contains
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|onlineDrainingServers
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
comment|// keeping track for later verification
block|}
block|}
comment|// Verify the ServerManager lists are correctly updated.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|serverManager
operator|.
name|getOnlineServers
argument_list|()
argument_list|,
name|onlineServers
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|serverManager
operator|.
name|getDrainingServersList
argument_list|()
argument_list|,
name|onlineDrainingServers
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

