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
name|JVMClusterUtil
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
name|TestRoundRobinAssignmentOnRestart
extends|extends
name|AbstractTestRestartCluster
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
name|TestRoundRobinAssignmentOnRestart
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
name|TestRoundRobinAssignmentOnRestart
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|boolean
name|splitWALCoordinatedByZk
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|private
specifier|final
name|int
name|regionNum
init|=
literal|10
decl_stmt|;
specifier|private
specifier|final
name|int
name|rsNum
init|=
literal|2
decl_stmt|;
comment|/**    * This tests retaining assignments on a cluster restart    */
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|rsNum
argument_list|)
expr_stmt|;
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
name|createMultiRegionTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|,
name|regionNum
argument_list|)
expr_stmt|;
block|}
comment|// Wait until all regions are assigned
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
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
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
name|ServerName
name|testServer
init|=
name|threads
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|testServer
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionsOnServer
argument_list|(
name|testServer
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"RegionServer {} has {} regions"
argument_list|,
name|testServer
argument_list|,
name|regionInfos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|regionInfos
operator|.
name|size
argument_list|()
operator|>=
operator|(
name|TABLES
operator|.
name|length
operator|*
name|regionNum
operator|/
name|rsNum
operator|)
argument_list|)
expr_stmt|;
comment|// Restart 1 regionserver
name|cluster
operator|.
name|stopRegionServer
argument_list|(
name|testServer
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForRegionServerToStop
argument_list|(
name|testServer
argument_list|,
literal|60000
argument_list|)
expr_stmt|;
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
name|port
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
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
name|ServerName
name|newTestServer
init|=
literal|null
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
name|getAddress
argument_list|()
operator|.
name|equals
argument_list|(
name|testServer
operator|.
name|getAddress
argument_list|()
argument_list|)
condition|)
block|{
name|newTestServer
operator|=
name|serverName
expr_stmt|;
break|break;
block|}
block|}
name|assertNotNull
argument_list|(
name|newTestServer
argument_list|)
expr_stmt|;
comment|// Wait until all regions are assigned
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
name|waitTableAvailable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegionInfos
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionsOnServer
argument_list|(
name|newTestServer
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"RegionServer {} has {} regions"
argument_list|,
name|newTestServer
argument_list|,
name|newRegionInfos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Should not retain all regions when restart"
argument_list|,
name|newRegionInfos
operator|.
name|size
argument_list|()
operator|<
name|regionInfos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

