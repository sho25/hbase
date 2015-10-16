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
name|chaos
operator|.
name|actions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
import|;
end_import

begin_comment
comment|/**  * Restarts a ratio of the running regionservers at the same time  */
end_comment

begin_class
specifier|public
class|class
name|BatchRestartRsAction
extends|extends
name|RestartActionBaseAction
block|{
name|float
name|ratio
decl_stmt|;
comment|//ratio of regionservers to restart
specifier|public
name|BatchRestartRsAction
parameter_list|(
name|long
name|sleepTime
parameter_list|,
name|float
name|ratio
parameter_list|)
block|{
name|super
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|ratio
operator|=
name|ratio
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Performing action: Batch restarting %d%% of region servers"
argument_list|,
call|(
name|int
call|)
argument_list|(
name|ratio
operator|*
literal|100
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|selectedServers
init|=
name|PolicyBasedChaosMonkey
operator|.
name|selectRandomItems
argument_list|(
name|getCurrentServers
argument_list|()
argument_list|,
name|ratio
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|killedServers
init|=
operator|new
name|HashSet
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|selectedServers
control|)
block|{
comment|// Don't keep killing servers if we're
comment|// trying to stop the monkey.
if|if
condition|(
name|context
operator|.
name|isStopping
argument_list|()
condition|)
block|{
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing region server:"
operator|+
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|killRegionServer
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|killedServers
operator|.
name|add
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ServerName
name|server
range|:
name|killedServers
control|)
block|{
name|cluster
operator|.
name|waitForRegionServerToStop
argument_list|(
name|server
argument_list|,
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Killed "
operator|+
name|killedServers
operator|.
name|size
argument_list|()
operator|+
literal|" region servers. Reported num of rs:"
operator|+
name|cluster
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|killedServers
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting region server:"
operator|+
name|server
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startRegionServer
argument_list|(
name|server
operator|.
name|getHostname
argument_list|()
argument_list|,
name|server
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ServerName
name|server
range|:
name|killedServers
control|)
block|{
name|cluster
operator|.
name|waitForRegionServerToStart
argument_list|(
name|server
operator|.
name|getHostname
argument_list|()
argument_list|,
name|server
operator|.
name|getPort
argument_list|()
argument_list|,
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|killedServers
operator|.
name|size
argument_list|()
operator|+
literal|" region servers. Reported num of rs:"
operator|+
name|cluster
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

