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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|classification
operator|.
name|InterfaceAudience
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_comment
comment|/**  * Impl for exposing HMaster Information through JMX  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsMasterWrapperImpl
implements|implements
name|MetricsMasterWrapper
block|{
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|public
name|MetricsMasterWrapperImpl
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
name|ZooKeeperWatcher
name|zk
init|=
name|master
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
if|if
condition|(
name|zk
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|zk
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
name|getMasterCoprocessors
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStartTime
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
name|getActiveTime
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
name|String
name|getRegionServers
parameter_list|()
block|{
name|ServerManager
name|serverManager
init|=
name|this
operator|.
name|master
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverManager
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|serverManager
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|,
literal|";"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumRegionServers
parameter_list|()
block|{
name|ServerManager
name|serverManager
init|=
name|this
operator|.
name|master
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverManager
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|serverManager
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDeadRegionServers
parameter_list|()
block|{
name|ServerManager
name|serverManager
init|=
name|this
operator|.
name|master
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverManager
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|serverManager
operator|.
name|getDeadServers
argument_list|()
operator|.
name|copyServerNames
argument_list|()
argument_list|,
literal|";"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumDeadRegionServers
parameter_list|()
block|{
name|ServerManager
name|serverManager
init|=
name|this
operator|.
name|master
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverManager
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|serverManager
operator|.
name|getDeadServers
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
name|ServerName
name|serverName
init|=
name|master
operator|.
name|getServerName
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverName
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|serverName
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

