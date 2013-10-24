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
name|regionserver
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
name|LocalHBaseCluster
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|NameStringPair
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|RegionServerStartupResponse
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
operator|.
name|MasterThread
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
comment|/**  * Tests region server termination during startup.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRSKilledWhenInitializing
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
name|TestRSKilledWhenInitializing
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|masterActive
init|=
literal|false
decl_stmt|;
comment|/**    * Test verifies whether a region server is removing from online servers list in master if it went    * down after registering with master.    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testRSTermnationAfterRegisteringToMasterBeforeCreatingEphemeralNod
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|NUM_RS
init|=
literal|1
decl_stmt|;
comment|// Create config to use for this cluster
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Start the cluster
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
specifier|final
name|LocalHBaseCluster
name|cluster
init|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|conf
argument_list|,
name|NUM_MASTERS
argument_list|,
name|NUM_RS
argument_list|,
name|HMaster
operator|.
name|class
argument_list|,
name|MockedRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|MasterThread
name|master
init|=
name|cluster
operator|.
name|getMasters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|master
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignored
parameter_list|)
block|{         }
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|startTime
operator|+
literal|30000
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Master not active after 30 seconds"
argument_list|)
throw|;
block|}
block|}
name|masterActive
operator|=
literal|true
expr_stmt|;
name|cluster
operator|.
name|getRegionServers
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|onlineServersList
init|=
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|onlineServersList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|onlineServersList
operator|=
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|onlineServersList
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|getMaster
argument_list|()
operator|.
name|stop
argument_list|(
literal|"stopping master"
argument_list|)
expr_stmt|;
name|master
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|masterActive
operator|=
literal|false
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|MockedRegionServer
extends|extends
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterRegionServer
block|{
specifier|public
name|MockedRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|handleReportForDutyResponse
parameter_list|(
name|RegionServerStartupResponse
name|c
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|NameStringPair
name|e
range|:
name|c
operator|.
name|getMapEntriesList
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|e
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// The hostname the master sees us as.
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|KEY_FOR_HOSTNAME_SEEN_BY_MASTER
argument_list|)
condition|)
block|{
name|String
name|hostnameFromMasterPOV
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|super
operator|.
name|getRpcServer
argument_list|()
operator|.
name|getListenerAddress
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|,
name|hostnameFromMasterPOV
argument_list|)
expr_stmt|;
block|}
block|}
while|while
condition|(
operator|!
name|masterActive
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
name|super
operator|.
name|kill
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

