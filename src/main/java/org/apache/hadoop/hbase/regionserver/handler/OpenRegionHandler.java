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
name|regionserver
operator|.
name|handler
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|RegionServerServices
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
name|util
operator|.
name|Progressable
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|Code
import|;
end_import

begin_comment
comment|/**  * Handles opening of a region on a region server.  *<p>  * This is executed after receiving an OPEN RPC from the master or client.  */
end_comment

begin_class
specifier|public
class|class
name|OpenRegionHandler
extends|extends
name|EventHandler
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
name|OpenRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|public
name|OpenRegionHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
argument_list|(
name|server
argument_list|,
name|rsServices
argument_list|,
name|regionInfo
argument_list|,
name|EventType
operator|.
name|M_RS_OPEN_REGION
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|OpenRegionHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
name|EventType
name|eventType
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|eventType
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
block|}
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing open of "
operator|+
name|name
argument_list|)
expr_stmt|;
specifier|final
name|String
name|encodedName
init|=
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
comment|// TODO: Previously we would check for root region availability (but only that it
comment|// was initially available, does not check if it later went away)
comment|// Do we need to wait on both root and meta to be available to open a region
comment|// now since we edit meta?
comment|// Check that this region is not already online
name|HRegion
name|region
init|=
name|this
operator|.
name|rsServices
operator|.
name|getFromOnlineRegions
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempting open of "
operator|+
name|name
operator|+
literal|" but it's already online on this server"
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|openingVersion
init|=
name|transitionZookeeperOfflineToOpening
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|openingVersion
operator|==
operator|-
literal|1
condition|)
return|return;
comment|// Open the region
specifier|final
name|AtomicInteger
name|openingInteger
init|=
operator|new
name|AtomicInteger
argument_list|(
name|openingVersion
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Instantiate the region.  This also periodically updates OPENING.
name|region
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|regionInfo
argument_list|,
name|this
operator|.
name|rsServices
operator|.
name|getWAL
argument_list|()
argument_list|,
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|rsServices
operator|.
name|getFlushRequester
argument_list|()
argument_list|,
operator|new
name|Progressable
argument_list|()
block|{
specifier|public
name|void
name|progress
parameter_list|()
block|{
try|try
block|{
name|int
name|vsn
init|=
name|ZKAssign
operator|.
name|retransitionNodeOpening
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|openingInteger
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|vsn
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
name|KeeperException
operator|.
name|create
argument_list|(
name|Code
operator|.
name|BADVERSION
argument_list|)
throw|;
block|}
name|openingInteger
operator|.
name|set
argument_list|(
name|vsn
argument_list|)
expr_stmt|;
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
literal|"ZK exception refreshing OPENING node; "
operator|+
name|name
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"IOException instantiating region for "
operator|+
name|regionInfo
operator|+
literal|"; resetting state of transition node from OPENING to OFFLINE"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// TODO: We should rely on the master timing out OPENING instead of this
comment|// TODO: What if this was a split open?  The RS made the OFFLINE
comment|// znode, not the master.
name|ZKAssign
operator|.
name|forceNodeOffline
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error forcing node back to OFFLINE from OPENING; "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Region is now open. Close it if error.
comment|// Re-transition node to OPENING again to verify no one has stomped on us
name|openingVersion
operator|=
name|openingInteger
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|(
name|openingVersion
operator|=
name|ZKAssign
operator|.
name|retransitionNodeOpening
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|openingVersion
argument_list|)
operator|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Completed the OPEN of region "
operator|+
name|name
operator|+
literal|" but when transitioning from "
operator|+
literal|" OPENING to OPENING got a version mismatch, someone else clashed "
operator|+
literal|"-- closing region"
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed transitioning node "
operator|+
name|name
operator|+
literal|" from OPENING to OPENED -- closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close region "
operator|+
name|name
operator|+
literal|" after failing to transition -- closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Update ZK, ROOT or META
try|try
block|{
name|this
operator|.
name|rsServices
operator|.
name|postOpenDeployTasks
argument_list|(
name|region
argument_list|,
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error updating "
operator|+
name|name
operator|+
literal|" location in catalog table -- "
operator|+
literal|"closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// TODO: rollback the open?
name|LOG
operator|.
name|error
argument_list|(
literal|"ZK Error updating "
operator|+
name|name
operator|+
literal|" location in catalog "
operator|+
literal|"table -- closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Finally, Transition ZK node to OPENED
try|try
block|{
if|if
condition|(
name|ZKAssign
operator|.
name|transitionNodeOpened
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|openingVersion
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Completed the OPEN of region "
operator|+
name|name
operator|+
literal|" but when transitioning from "
operator|+
literal|" OPENING to OPENED got a version mismatch, someone else clashed "
operator|+
literal|"so now unassigning -- closing region"
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed transitioning node "
operator|+
name|name
operator|+
literal|" from OPENING to OPENED -- closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close "
operator|+
name|name
operator|+
literal|" after failing to transition -- closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Done!  Successful region open
name|LOG
operator|.
name|debug
argument_list|(
literal|"Opened "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|cleanupFailedOpen
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|rsServices
operator|.
name|removeFromOnlineRegions
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|transitionZookeeperOfflineToOpening
parameter_list|(
specifier|final
name|String
name|encodedName
parameter_list|)
block|{
comment|// Transition ZK node from OFFLINE to OPENING
comment|// TODO: should also handle transition from CLOSED?
name|int
name|openingVersion
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|(
name|openingVersion
operator|=
name|ZKAssign
operator|.
name|transitionNodeOpening
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error transitioning node from OFFLINE to OPENING, "
operator|+
literal|"aborting open"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error transitioning node from OFFLINE to OPENING for region "
operator|+
name|encodedName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|openingVersion
return|;
block|}
block|}
end_class

end_unit

