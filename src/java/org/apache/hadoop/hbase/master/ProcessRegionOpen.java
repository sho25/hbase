begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|IOException
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
name|RegionHistorian
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
name|io
operator|.
name|BatchUpdate
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
comment|/**    * @param master    * @param info    * @param regionInfo    * @throws IOException    */
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
throws|throws
name|IOException
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
name|HServerInfo
operator|.
name|getServerName
argument_list|(
name|serverInfo
argument_list|)
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
name|Boolean
name|result
init|=
operator|new
name|RetryableMetaOperation
argument_list|<
name|Boolean
argument_list|>
argument_list|(
name|getMetaRegion
argument_list|()
argument_list|,
name|this
operator|.
name|master
argument_list|)
block|{
specifier|private
specifier|final
name|RegionHistorian
name|historian
init|=
name|RegionHistorian
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|IOException
block|{
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
comment|// Register the newly-available Region's location.
name|LOG
operator|.
name|info
argument_list|(
literal|"updating row "
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
literal|" with "
operator|+
literal|" with startcode "
operator|+
name|serverInfo
operator|.
name|getStartCode
argument_list|()
operator|+
literal|" and server "
operator|+
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|COL_SERVER
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
name|b
operator|.
name|put
argument_list|(
name|COL_STARTCODE
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
name|batchUpdate
argument_list|(
name|metaRegionName
argument_list|,
name|b
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|historian
operator|.
name|isOnline
argument_list|()
condition|)
block|{
comment|// This is safest place to do the onlining of the historian in
comment|// the master.  When we get to here, we know there is a .META.
comment|// for the historian to go against.
name|this
operator|.
name|historian
operator|.
name|online
argument_list|(
name|this
operator|.
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|historian
operator|.
name|addRegionOpen
argument_list|(
name|regionInfo
argument_list|,
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
name|regionManager
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
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|master
operator|.
name|regionManager
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
name|regionManager
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
name|regionManager
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
name|regionManager
operator|.
name|metaScannerThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
comment|// If updated successfully, remove from pending list.
name|master
operator|.
name|regionManager
operator|.
name|removeRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
operator|.
name|doWithRetries
argument_list|()
decl_stmt|;
return|return
name|result
operator|==
literal|null
condition|?
literal|true
else|:
name|result
return|;
block|}
block|}
end_class

end_unit

