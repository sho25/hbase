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
name|master
package|;
end_package

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
name|HServerAddress
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
name|HServerInfo
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
name|Put
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
name|ipc
operator|.
name|HRegionInterface
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**   * ProcessRegionOpen is instantiated when a region server reports that it is  * serving a region. This applies to all meta and user regions except the   * root region which is handled specially.  */
end_comment

begin_class
class|class
name|ProcessRegionOpen
extends|extends
name|ProcessRegionStatusChange
block|{
specifier|protected
specifier|final
name|HServerInfo
name|serverInfo
decl_stmt|;
comment|/**    * @param master    * @param info    * @param regionInfo    */
specifier|public
name|ProcessRegionOpen
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|HServerInfo
name|info
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"HServerInfo cannot be null; "
operator|+
literal|"hbase-958 debugging"
argument_list|)
throw|;
block|}
name|this
operator|.
name|serverInfo
operator|=
name|info
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"PendingOpenOperation from "
operator|+
name|serverInfo
operator|.
name|getServerName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|process
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO: The below check is way too convoluted!!!
if|if
condition|(
operator|!
name|metaRegionAvailable
argument_list|()
condition|)
block|{
comment|// We can't proceed unless the meta region we are going to update
comment|// is online. metaRegionAvailable() has put this operation on the
comment|// delayedToDoQueue, so return true so the operation is not put
comment|// back on the toDoQueue
return|return
literal|true
return|;
block|}
name|HRegionInterface
name|server
init|=
name|master
operator|.
name|getServerConnection
argument_list|()
operator|.
name|getHRegionConnection
argument_list|(
name|getMetaRegion
argument_list|()
operator|.
name|getServer
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" open on "
operator|+
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Register the newly-available Region's location.
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|CATALOG_FAMILY
argument_list|,
name|SERVER_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|CATALOG_FAMILY
argument_list|,
name|STARTCODE_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|serverInfo
operator|.
name|getStartCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|put
argument_list|(
name|metaRegionName
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Updated row "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" in region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|metaRegionName
argument_list|)
operator|+
literal|" with startcode="
operator|+
name|serverInfo
operator|.
name|getStartCode
argument_list|()
operator|+
literal|", server="
operator|+
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|master
operator|.
name|getRegionManager
argument_list|()
init|)
block|{
if|if
condition|(
name|isMetaTable
condition|)
block|{
comment|// It's a meta region.
name|MetaRegion
name|m
init|=
operator|new
name|MetaRegion
argument_list|(
operator|new
name|HServerAddress
argument_list|(
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
argument_list|)
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|isInitialMetaScanComplete
argument_list|()
condition|)
block|{
comment|// Put it on the queue to be scanned for the first time.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding "
operator|+
name|m
operator|.
name|toString
argument_list|()
operator|+
literal|" to regions to scan"
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|addMetaRegionToScan
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Add it to the online meta regions
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding to onlineMetaRegions: "
operator|+
name|m
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|putMetaRegionOnline
argument_list|(
name|m
argument_list|)
expr_stmt|;
comment|// Interrupting the Meta Scanner sleep so that it can
comment|// process regions right away
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|metaScannerThread
operator|.
name|triggerNow
argument_list|()
expr_stmt|;
block|}
block|}
comment|// If updated successfully, remove from pending list if the state
comment|// is consistent. For example, a disable could be called before the
comment|// synchronization.
if|if
condition|(
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|isOfflined
argument_list|(
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"We opened a region while it was asked to be closed."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|removeRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getPriority
parameter_list|()
block|{
return|return
literal|0
return|;
comment|// highest priority
block|}
block|}
end_class

end_unit

