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
name|stream
operator|.
name|Collectors
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
name|master
operator|.
name|assignment
operator|.
name|ServerState
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
name|ServerStateNode
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
name|procedure
operator|.
name|ServerCrashProcedure
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
name|procedure2
operator|.
name|Procedure
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
name|TestClusterRestartFailover
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
name|TestClusterRestartFailover
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
name|TestClusterRestartFailover
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
name|ServerStateNode
name|getServerStateNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getServerNode
argument_list|(
name|serverName
argument_list|)
return|;
block|}
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
literal|3
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
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
argument_list|)
expr_stmt|;
comment|// wait for all SCPs finished
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getProcedures
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|noneMatch
argument_list|(
name|p
lambda|->
name|p
operator|instanceof
name|ServerCrashProcedure
argument_list|)
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TABLES
index|[
literal|0
index|]
decl_stmt|;
name|ServerName
name|testServer
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|getServerStateNode
argument_list|(
name|testServer
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|ServerStateNode
name|serverNode
init|=
name|getServerStateNode
argument_list|(
name|testServer
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|serverNode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"serverNode should be ONLINE when cluster runs normally"
argument_list|,
name|serverNode
operator|.
name|isInState
argument_list|(
name|ServerState
operator|.
name|ONLINE
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|ports
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|serverName
lambda|->
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down cluster"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|killAll
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|waitUntilShutDown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster the second time"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|restartHBaseCluster
argument_list|(
literal|3
argument_list|,
name|ports
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
argument_list|)
expr_stmt|;
name|serverNode
operator|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getServerNode
argument_list|(
name|testServer
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"serverNode should not be null when restart whole cluster"
argument_list|,
name|serverNode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|serverNode
operator|.
name|isInState
argument_list|(
name|ServerState
operator|.
name|ONLINE
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"start to find the procedure of SCP for the severName we choose"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getProcedures
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|procedure
lambda|->
operator|(
name|procedure
operator|instanceof
name|ServerCrashProcedure
operator|)
operator|&&
operator|(
operator|(
name|ServerCrashProcedure
operator|)
name|procedure
operator|)
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|testServer
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"serverNode should not be ONLINE during SCP processing"
argument_list|,
name|serverNode
operator|.
name|isInState
argument_list|(
name|ServerState
operator|.
name|ONLINE
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"start to submit the SCP for the same serverName {} which should fail"
argument_list|,
name|testServer
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|expireServer
argument_list|(
name|testServer
argument_list|)
argument_list|)
expr_stmt|;
name|Procedure
argument_list|<
name|?
argument_list|>
name|procedure
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getProcedures
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|p
lambda|->
operator|(
name|p
operator|instanceof
name|ServerCrashProcedure
operator|)
operator|&&
operator|(
operator|(
name|ServerCrashProcedure
operator|)
name|p
operator|)
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|testServer
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|procedure
operator|.
name|isFinished
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"even when the SCP is finished, the duplicate SCP should not be scheduled for {}"
argument_list|,
name|testServer
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|expireServer
argument_list|(
name|testServer
argument_list|)
argument_list|)
expr_stmt|;
name|serverNode
operator|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getServerNode
argument_list|(
name|testServer
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
literal|"serverNode should be deleted after SCP finished"
argument_list|,
name|serverNode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

