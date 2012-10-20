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
operator|.
name|handler
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
name|Server
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
name|executor
operator|.
name|EventHandler
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
name|RegionState
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Handles OPENED region event on Master.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|OpenedRegionHandler
extends|extends
name|EventHandler
implements|implements
name|TotesHRegionInfo
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
name|OpenedRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|sn
decl_stmt|;
specifier|private
specifier|final
name|OpenedPriority
name|priority
decl_stmt|;
specifier|private
specifier|final
name|int
name|expectedVersion
decl_stmt|;
specifier|private
enum|enum
name|OpenedPriority
block|{
name|ROOT
argument_list|(
literal|1
argument_list|)
block|,
name|META
argument_list|(
literal|2
argument_list|)
block|,
name|USER
argument_list|(
literal|3
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
name|OpenedPriority
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
empty_stmt|;
specifier|public
name|OpenedRegionHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|int
name|expectedVersion
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|)
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|sn
operator|=
name|sn
expr_stmt|;
name|this
operator|.
name|expectedVersion
operator|=
name|expectedVersion
expr_stmt|;
if|if
condition|(
name|regionInfo
operator|.
name|isRootRegion
argument_list|()
condition|)
block|{
name|priority
operator|=
name|OpenedPriority
operator|.
name|ROOT
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|regionInfo
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|priority
operator|=
name|OpenedPriority
operator|.
name|META
expr_stmt|;
block|}
else|else
block|{
name|priority
operator|=
name|OpenedPriority
operator|.
name|USER
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|priority
operator|.
name|getValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionInfo
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|"UnknownServerName"
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
comment|// Code to defend against case where we get SPLIT before region open
comment|// processing completes; temporary till we make SPLITs go via zk -- 0.92.
name|RegionState
name|regionState
init|=
name|this
operator|.
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionTransitionState
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|openedNodeDeleted
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|regionState
operator|!=
literal|null
operator|&&
name|regionState
operator|.
name|isOpened
argument_list|()
condition|)
block|{
name|openedNodeDeleted
operator|=
name|deleteOpenedNode
argument_list|(
name|expectedVersion
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|openedNodeDeleted
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The znode of region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" could not be deleted."
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping the onlining of "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" because regions is NOT in RIT -- presuming this is because it SPLIT"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|openedNodeDeleted
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|assignmentManager
operator|.
name|getZKTable
argument_list|()
operator|.
name|isDisablingOrDisabledTable
argument_list|(
name|regionInfo
operator|.
name|getTableNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|debugLog
argument_list|(
name|regionInfo
argument_list|,
literal|"Opened region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" but "
operator|+
literal|"this table is disabled, triggering close of region"
argument_list|)
expr_stmt|;
name|assignmentManager
operator|.
name|unassign
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|boolean
name|deleteOpenedNode
parameter_list|(
name|int
name|expectedVersion
parameter_list|)
block|{
name|debugLog
argument_list|(
name|regionInfo
argument_list|,
literal|"Handling OPENED event for "
operator|+
name|this
operator|.
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" from "
operator|+
name|this
operator|.
name|sn
operator|.
name|toString
argument_list|()
operator|+
literal|"; deleting unassigned node"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// delete the opened znode only if the version matches.
return|return
name|ZKAssign
operator|.
name|deleteNode
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_OPENED
argument_list|,
name|expectedVersion
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|e
parameter_list|)
block|{
comment|// Getting no node exception here means that already the region has been opened.
name|LOG
operator|.
name|warn
argument_list|(
literal|"The znode of the region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" would have already been deleted"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|server
operator|.
name|abort
argument_list|(
literal|"Error deleting OPENED node in ZK ("
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|debugLog
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|String
name|string
parameter_list|)
block|{
if|if
condition|(
name|region
operator|.
name|isMetaTable
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

