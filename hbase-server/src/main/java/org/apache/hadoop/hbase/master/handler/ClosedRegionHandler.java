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

begin_comment
comment|/**  * Handles CLOSED region event on Master.  *<p>  * If table is being disabled, deletes ZK unassigned node and removes from  * regions in transition.  *<p>  * Otherwise, assigns the region to another server.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClosedRegionHandler
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
name|ClosedRegionHandler
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
name|ClosedPriority
name|priority
decl_stmt|;
specifier|private
enum|enum
name|ClosedPriority
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
name|ClosedPriority
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
name|ClosedRegionHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|RS_ZK_REGION_CLOSED
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
name|ClosedPriority
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
name|ClosedPriority
operator|.
name|META
expr_stmt|;
block|}
else|else
block|{
name|priority
operator|=
name|ClosedPriority
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Handling CLOSED event for "
operator|+
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if this table is being disabled or not
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
name|this
operator|.
name|regionInfo
operator|.
name|getTableNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|assignmentManager
operator|.
name|offlineDisabledRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// ZK Node is in CLOSED state, assign it.
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
operator|.
name|updateRegionState
argument_list|(
name|regionInfo
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSED
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// This below has to do w/ online enable/disable of a table
name|assignmentManager
operator|.
name|removeClosedRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|assignmentManager
operator|.
name|assign
argument_list|(
name|regionInfo
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

