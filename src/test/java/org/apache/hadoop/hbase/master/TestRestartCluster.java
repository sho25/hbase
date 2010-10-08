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
name|List
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
name|client
operator|.
name|MetaScanner
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
name|executor
operator|.
name|EventHandler
operator|.
name|EventType
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
name|ZKAssign
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

begin_class
specifier|public
class|class
name|TestRestartCluster
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
name|TestRestartCluster
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|ZooKeeperWatcher
name|zooKeeper
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLENAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"master_transitions"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|TABLES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"restartTableOne"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"restartTableTwo"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
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
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{   }
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
throws|throws
name|IOException
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
name|testRestartClusterAfterKill
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|zooKeeper
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"cluster1"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// create the unassigned region, throw up a region opened state for META
name|String
name|unassignedZNode
init|=
name|zooKeeper
operator|.
name|assignmentZNode
decl_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zooKeeper
argument_list|,
name|unassignedZNode
argument_list|)
expr_stmt|;
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|zooKeeper
argument_list|,
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
argument_list|,
name|HMaster
operator|.
name|MASTER
argument_list|)
expr_stmt|;
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|zooKeeper
argument_list|,
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|HMaster
operator|.
name|MASTER
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created UNASSIGNED zNode for ROOT and META regions in state "
operator|+
name|EventType
operator|.
name|M_ZK_REGION_OFFLINE
argument_list|)
expr_stmt|;
comment|// start the HB cluster
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting HBase cluster..."
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created a table, waiting for table to be available..."
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLENAME
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master deleted unassigned region and started up successfully."
argument_list|)
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
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nCreating tables"
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
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
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|allRegions
init|=
name|MetaScanner
operator|.
name|listAllRegions
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
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
name|getHBaseCluster
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|join
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
comment|// Need to use a new 'Configuration' so we make a new HConnection.
comment|// Otherwise we're reusing an HConnection that has gone stale because
comment|// the shutdown of the cluster also called shut of the connection.
name|allRegions
operator|=
name|MetaScanner
operator|.
name|listAllRegions
argument_list|(
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
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
name|byte
index|[]
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
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

