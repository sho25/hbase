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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotEquals
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|MetaTableAccessor
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
name|TableExistsException
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
name|master
operator|.
name|assignment
operator|.
name|RegionStates
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
name|LargeTests
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
name|After
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRestartCluster
block|{
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
name|TestRestartCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
index|[]
name|TABLES
init|=
block|{
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"restartTableOne"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"restartTableTwo"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"restartTableThree"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|tearDown
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
literal|300000
argument_list|)
specifier|public
name|void
name|testClusterRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nCreating tables"
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|TABLE
range|:
name|TABLES
control|)
block|{
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|TableName
name|TABLE
range|:
name|TABLES
control|)
block|{
name|UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|allRegions
init|=
name|MetaTableAccessor
operator|.
name|getAllRegions
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|allRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nShutting down cluster"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nSleeping a bit"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nStarting cluster the second time"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|restartHBaseCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// Need to use a new 'Configuration' so we make a new Connection.
comment|// Otherwise we're reusing an Connection that has gone stale because
comment|// the shutdown of the cluster also called shut of the connection.
name|allRegions
operator|=
name|MetaTableAccessor
operator|.
name|getAllRegions
argument_list|(
name|UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|allRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nWaiting for tables to be available"
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|TABLE
range|:
name|TABLES
control|)
block|{
try|try
block|{
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Able to create table that should already exist"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|tee
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table already exists as expected"
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This tests retaining assignments on a cluster restart    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testRetainAssignmentOnRestart
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Turn off balancer
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterRpcServices
argument_list|()
operator|.
name|synchronousBalanceSwitch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nCreating tables"
argument_list|)
expr_stmt|;
for|for
control|(
name|TableName
name|TABLE
range|:
name|TABLES
control|)
block|{
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|TableName
name|TABLE
range|:
name|TABLES
control|)
block|{
name|UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
block|}
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|120000
argument_list|)
expr_stmt|;
comment|// We don't have to use SnapshotOfRegionAssignmentFromMeta.
comment|// We use it here because AM used to use it to load all user region placements
name|SnapshotOfRegionAssignmentFromMeta
name|snapshot
init|=
operator|new
name|SnapshotOfRegionAssignmentFromMeta
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|)
decl_stmt|;
name|snapshot
operator|.
name|initialize
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionToRegionServerMap
init|=
name|snapshot
operator|.
name|getRegionToRegionServerMap
argument_list|()
decl_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|threads
init|=
name|cluster
operator|.
name|getLiveRegionServerThreads
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|threads
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
index|[]
name|rsPorts
init|=
operator|new
name|int
index|[
literal|3
index|]
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|rsPorts
index|[
name|i
index|]
operator|=
name|threads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
expr_stmt|;
block|}
name|rsPorts
index|[
literal|2
index|]
operator|=
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getPort
argument_list|()
expr_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|regionToRegionServerMap
operator|.
name|values
argument_list|()
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
comment|// Test only, no need to optimize
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
literal|3
operator|&&
operator|!
name|found
condition|;
name|k
operator|++
control|)
block|{
name|found
operator|=
name|serverName
operator|.
name|getPort
argument_list|()
operator|==
name|rsPorts
index|[
name|k
index|]
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nShutting down HBase cluster"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|waitUntilShutDown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nSleeping a bit"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nStarting cluster the second time with the same ports"
argument_list|)
expr_stmt|;
try|try
block|{
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|master
operator|=
name|cluster
operator|.
name|startMaster
argument_list|()
operator|.
name|getMaster
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
name|rsPorts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// Reset region server port so as not to conflict with other tests
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|setInt
argument_list|(
name|ServerManager
operator|.
name|WAIT_ON_REGIONSERVERS_MINTOSTART
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|// Make sure live regionservers are on the same host/port
name|List
argument_list|<
name|ServerName
argument_list|>
name|localServers
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|localServers
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
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|localServers
control|)
block|{
if|if
condition|(
name|serverName
operator|.
name|getPort
argument_list|()
operator|==
name|rsPorts
index|[
name|i
index|]
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
block|}
comment|// Wait till master is initialized and all regions are assigned
name|RegionStates
name|regionStates
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
name|int
name|expectedRegions
init|=
name|regionToRegionServerMap
operator|.
name|size
argument_list|()
operator|+
literal|1
decl_stmt|;
while|while
condition|(
operator|!
name|master
operator|.
name|isInitialized
argument_list|()
operator|||
name|regionStates
operator|.
name|getRegionAssignments
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
name|expectedRegions
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|snapshot
operator|=
operator|new
name|SnapshotOfRegionAssignmentFromMeta
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|initialize
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|newRegionToRegionServerMap
init|=
name|snapshot
operator|.
name|getRegionToRegionServerMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|regionToRegionServerMap
operator|.
name|size
argument_list|()
argument_list|,
name|newRegionToRegionServerMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|entry
range|:
name|newRegionToRegionServerMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
continue|continue;
name|ServerName
name|oldServer
init|=
name|regionToRegionServerMap
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|ServerName
name|currentServer
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Key="
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" oldServer="
operator|+
name|oldServer
operator|+
literal|", currentServer="
operator|+
name|currentServer
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|oldServer
operator|.
name|getAddress
argument_list|()
argument_list|,
name|currentServer
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|oldServer
operator|.
name|getStartcode
argument_list|()
argument_list|,
name|currentServer
operator|.
name|getStartcode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

