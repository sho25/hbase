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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|master
operator|.
name|AssignmentManager
operator|.
name|RegionState
import|;
end_import

begin_comment
comment|/**  * Impl for exposing HMaster Information through JMX  */
end_comment

begin_class
specifier|public
class|class
name|MXBeanImpl
implements|implements
name|MXBean
block|{
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|private
specifier|static
name|MXBeanImpl
name|instance
init|=
literal|null
decl_stmt|;
specifier|public
specifier|synchronized
specifier|static
name|MXBeanImpl
name|init
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|)
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|MXBeanImpl
argument_list|(
name|master
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|protected
name|MXBeanImpl
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getAverageLoad
parameter_list|()
block|{
return|return
name|master
operator|.
name|getAverageLoad
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|master
operator|.
name|getClusterId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getZookeeperQuorum
parameter_list|()
block|{
return|return
name|master
operator|.
name|getZooKeeperWatcher
argument_list|()
operator|.
name|getQuorum
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getCoprocessors
parameter_list|()
block|{
return|return
name|master
operator|.
name|getCoprocessors
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMasterStartTime
parameter_list|()
block|{
return|return
name|master
operator|.
name|getMasterStartTime
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMasterActiveTime
parameter_list|()
block|{
return|return
name|master
operator|.
name|getMasterActiveTime
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ServerLoad
argument_list|>
name|getRegionServers
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ServerLoad
argument_list|>
name|data
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ServerLoad
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|entry
range|:
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|data
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|data
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getDeadRegionServers
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|deadServers
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|name
range|:
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getDeadServers
argument_list|()
control|)
block|{
name|deadServers
operator|.
name|add
argument_list|(
name|name
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|deadServers
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionsInTransitionInfo
index|[]
name|getRegionsInTransition
parameter_list|()
block|{
name|List
argument_list|<
name|RegionsInTransitionInfo
argument_list|>
name|info
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionsInTransitionInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Entry
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|entry
range|:
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionsInTransition
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|RegionsInTransitionInfo
name|innerinfo
init|=
operator|new
name|RegionsInTransitionInfo
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|getRegionState
parameter_list|()
block|{
return|return
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRegionName
parameter_list|()
block|{
return|return
name|entry
operator|.
name|getKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastUpdateTime
parameter_list|()
block|{
return|return
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getStamp
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRegionServerName
parameter_list|()
block|{
name|ServerName
name|serverName
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverName
operator|!=
literal|null
condition|)
block|{
return|return
name|serverName
operator|.
name|getServerName
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|""
return|;
block|}
block|}
block|}
decl_stmt|;
name|info
operator|.
name|add
argument_list|(
name|innerinfo
argument_list|)
expr_stmt|;
block|}
name|RegionsInTransitionInfo
index|[]
name|data
init|=
operator|new
name|RegionsInTransitionInfo
index|[
name|info
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|info
operator|.
name|toArray
argument_list|(
name|data
argument_list|)
expr_stmt|;
return|return
name|data
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
return|return
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getIsActiveMaster
parameter_list|()
block|{
return|return
name|master
operator|.
name|isActiveMaster
argument_list|()
return|;
block|}
block|}
end_class

end_unit

