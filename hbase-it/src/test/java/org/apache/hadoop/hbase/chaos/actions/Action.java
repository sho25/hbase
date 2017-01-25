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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|lang
operator|.
name|math
operator|.
name|RandomUtils
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
name|ClusterStatus
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
name|HBaseCluster
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
name|IntegrationTestingUtility
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * A (possibly mischievous) action that the ChaosMonkey can perform.  */
end_comment

begin_class
specifier|public
class|class
name|Action
block|{
specifier|public
specifier|static
specifier|final
name|String
name|KILL_MASTER_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.killmastertimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|START_MASTER_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.startmastertimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KILL_RS_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.killrstimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|START_RS_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.startrstimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KILL_ZK_NODE_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.killzknodetimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|START_ZK_NODE_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.startzknodetimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KILL_DATANODE_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.killdatanodetimeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|START_DATANODE_TIMEOUT_KEY
init|=
literal|"hbase.chaosmonkey.action.startdatanodetimeout"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Action
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|KILL_MASTER_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|START_MASTER_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|KILL_RS_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|START_RS_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|KILL_ZK_NODE_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|START_ZK_NODE_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|KILL_DATANODE_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|START_DATANODE_TIMEOUT_DEFAULT
init|=
name|PolicyBasedChaosMonkey
operator|.
name|TIMEOUT
decl_stmt|;
specifier|protected
name|ActionContext
name|context
decl_stmt|;
specifier|protected
name|HBaseCluster
name|cluster
decl_stmt|;
specifier|protected
name|ClusterStatus
name|initialStatus
decl_stmt|;
specifier|protected
name|ServerName
index|[]
name|initialServers
decl_stmt|;
specifier|protected
name|long
name|killMasterTimeout
decl_stmt|;
specifier|protected
name|long
name|startMasterTimeout
decl_stmt|;
specifier|protected
name|long
name|killRsTimeout
decl_stmt|;
specifier|protected
name|long
name|startRsTimeout
decl_stmt|;
specifier|protected
name|long
name|killZkNodeTimeout
decl_stmt|;
specifier|protected
name|long
name|startZkNodeTimeout
decl_stmt|;
specifier|protected
name|long
name|killDataNodeTimeout
decl_stmt|;
specifier|protected
name|long
name|startDataNodeTimeout
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|ActionContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|cluster
operator|=
name|context
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|initialStatus
operator|=
name|cluster
operator|.
name|getInitialClusterStatus
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
name|initialStatus
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|initialServers
operator|=
name|regionServers
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|regionServers
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|killMasterTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|KILL_MASTER_TIMEOUT_KEY
argument_list|,
name|KILL_MASTER_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|startMasterTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|START_MASTER_TIMEOUT_KEY
argument_list|,
name|START_MASTER_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|killRsTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|KILL_RS_TIMEOUT_KEY
argument_list|,
name|KILL_RS_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|startRsTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|START_RS_TIMEOUT_KEY
argument_list|,
name|START_RS_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|killZkNodeTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|KILL_ZK_NODE_TIMEOUT_KEY
argument_list|,
name|KILL_ZK_NODE_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|startZkNodeTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|START_ZK_NODE_TIMEOUT_KEY
argument_list|,
name|START_ZK_NODE_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|killDataNodeTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|KILL_DATANODE_TIMEOUT_KEY
argument_list|,
name|KILL_DATANODE_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
name|startDataNodeTimeout
operator|=
name|cluster
operator|.
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|START_DATANODE_TIMEOUT_KEY
argument_list|,
name|START_DATANODE_TIMEOUT_DEFAULT
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{ }
comment|/** Returns current region servers - active master */
specifier|protected
name|ServerName
index|[]
name|getCurrentServers
parameter_list|()
throws|throws
name|IOException
block|{
name|ClusterStatus
name|clusterStatus
init|=
name|cluster
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|regionServers
init|=
name|clusterStatus
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|int
name|count
init|=
name|regionServers
operator|==
literal|null
condition|?
literal|0
else|:
name|regionServers
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|count
operator|<=
literal|0
condition|)
block|{
return|return
operator|new
name|ServerName
index|[]
block|{}
return|;
block|}
name|ServerName
name|master
init|=
name|clusterStatus
operator|.
name|getMaster
argument_list|()
decl_stmt|;
if|if
condition|(
name|master
operator|==
literal|null
operator|||
operator|!
name|regionServers
operator|.
name|contains
argument_list|(
name|master
argument_list|)
condition|)
block|{
return|return
name|regionServers
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|count
index|]
argument_list|)
return|;
block|}
if|if
condition|(
name|count
operator|==
literal|1
condition|)
block|{
return|return
operator|new
name|ServerName
index|[]
block|{}
return|;
block|}
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|tmp
operator|.
name|addAll
argument_list|(
name|regionServers
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|remove
argument_list|(
name|master
argument_list|)
expr_stmt|;
return|return
name|tmp
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|count
operator|-
literal|1
index|]
argument_list|)
return|;
block|}
specifier|protected
name|void
name|killMaster
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing master:"
operator|+
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|killMaster
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForMasterToStop
argument_list|(
name|server
argument_list|,
name|killMasterTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killed master server:"
operator|+
name|server
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|startMaster
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting master:"
operator|+
name|server
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startMaster
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
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|(
name|startMasterTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started master: "
operator|+
name|server
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|killRs
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
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
name|cluster
operator|.
name|waitForRegionServerToStop
argument_list|(
name|server
argument_list|,
name|killRsTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killed region server:"
operator|+
name|server
operator|+
literal|". Reported num of rs:"
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
specifier|protected
name|void
name|startRs
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
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
name|startRsTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started region server:"
operator|+
name|server
operator|+
literal|". Reported num of rs:"
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
specifier|protected
name|void
name|killZKNode
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing zookeeper node:"
operator|+
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|killZkNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForZkNodeToStop
argument_list|(
name|server
argument_list|,
name|killZkNodeTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killed zookeeper node:"
operator|+
name|server
operator|+
literal|". Reported num of rs:"
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
specifier|protected
name|void
name|startZKNode
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting zookeeper node:"
operator|+
name|server
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startZkNode
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
name|cluster
operator|.
name|waitForZkNodeToStart
argument_list|(
name|server
argument_list|,
name|startZkNodeTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started zookeeper node:"
operator|+
name|server
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|killDataNode
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing datanode:"
operator|+
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|killDataNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForDataNodeToStop
argument_list|(
name|server
argument_list|,
name|killDataNodeTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killed datanode:"
operator|+
name|server
operator|+
literal|". Reported num of rs:"
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
specifier|protected
name|void
name|startDataNode
parameter_list|(
name|ServerName
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting datanode:"
operator|+
name|server
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startDataNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForDataNodeToStart
argument_list|(
name|server
argument_list|,
name|startDataNodeTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started datanode:"
operator|+
name|server
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|unbalanceRegions
parameter_list|(
name|ClusterStatus
name|clusterStatus
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|fromServers
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|toServers
parameter_list|,
name|double
name|fractionOfRegions
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|victimRegions
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|fromServers
control|)
block|{
name|ServerLoad
name|serverLoad
init|=
name|clusterStatus
operator|.
name|getLoad
argument_list|(
name|server
argument_list|)
decl_stmt|;
comment|// Ugh.
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|regions
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|(
name|serverLoad
operator|.
name|getRegionsLoad
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|victimRegionCount
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|fractionOfRegions
operator|*
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing "
operator|+
name|victimRegionCount
operator|+
literal|" regions from "
operator|+
name|server
operator|.
name|getServerName
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
name|victimRegionCount
condition|;
operator|++
name|i
control|)
block|{
name|int
name|victimIx
init|=
name|RandomUtils
operator|.
name|nextInt
argument_list|(
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|regionId
init|=
name|HRegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|regions
operator|.
name|remove
argument_list|(
name|victimIx
argument_list|)
argument_list|)
decl_stmt|;
name|victimRegions
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving "
operator|+
name|victimRegions
operator|.
name|size
argument_list|()
operator|+
literal|" regions from "
operator|+
name|fromServers
operator|.
name|size
argument_list|()
operator|+
literal|" servers to "
operator|+
name|toServers
operator|.
name|size
argument_list|()
operator|+
literal|" different servers"
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|this
operator|.
name|context
operator|.
name|getHBaseIntegrationTestingUtility
argument_list|()
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|victimRegion
range|:
name|victimRegions
control|)
block|{
comment|// Don't keep moving regions if we're
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
name|int
name|targetIx
init|=
name|RandomUtils
operator|.
name|nextInt
argument_list|(
name|toServers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|move
argument_list|(
name|victimRegion
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|toServers
operator|.
name|get
argument_list|(
name|targetIx
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|forceBalancer
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|this
operator|.
name|context
operator|.
name|getHBaseIntegrationTestingUtility
argument_list|()
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|boolean
name|result
init|=
literal|false
decl_stmt|;
try|try
block|{
name|result
operator|=
name|admin
operator|.
name|balancer
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got exception while doing balance "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|result
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Balancer didn't succeed"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|cluster
operator|.
name|getConf
argument_list|()
return|;
block|}
comment|/**    * Context for Action's    */
specifier|public
specifier|static
class|class
name|ActionContext
block|{
specifier|private
name|IntegrationTestingUtility
name|util
decl_stmt|;
specifier|public
name|ActionContext
parameter_list|(
name|IntegrationTestingUtility
name|util
parameter_list|)
block|{
name|this
operator|.
name|util
operator|=
name|util
expr_stmt|;
block|}
specifier|public
name|IntegrationTestingUtility
name|getHBaseIntegrationTestingUtility
parameter_list|()
block|{
return|return
name|util
return|;
block|}
specifier|public
name|HBaseCluster
name|getHBaseCluster
parameter_list|()
block|{
return|return
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

