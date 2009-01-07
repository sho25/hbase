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
name|HashSet
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
name|Writables
import|;
end_import

begin_comment
comment|/** Instantiated to enable or disable a table */
end_comment

begin_class
class|class
name|ChangeTableState
extends|extends
name|TableOperation
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|online
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|servedRegions
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|long
name|lockid
decl_stmt|;
name|ChangeTableState
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|boolean
name|onLine
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|online
operator|=
name|onLine
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processScanItem
parameter_list|(
name|String
name|serverName
parameter_list|,
name|long
name|startCode
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
block|{
if|if
condition|(
name|isBeingServed
argument_list|(
name|serverName
argument_list|,
name|startCode
argument_list|)
condition|)
block|{
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|servedRegions
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|==
literal|null
condition|)
block|{
name|regions
operator|=
operator|new
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|regions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
name|servedRegions
operator|.
name|put
argument_list|(
name|serverName
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|postProcessMeta
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HRegionInterface
name|server
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Process regions not being served
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
literal|"processing unserved regions"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HRegionInfo
name|i
range|:
name|unservedRegions
control|)
block|{
if|if
condition|(
name|i
operator|.
name|isOffline
argument_list|()
operator|&&
name|i
operator|.
name|isSplit
argument_list|()
condition|)
block|{
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
literal|"Skipping region "
operator|+
name|i
operator|.
name|toString
argument_list|()
operator|+
literal|" because it is offline because it has been split"
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
comment|// Update meta table
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|i
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|updateRegionInfo
argument_list|(
name|b
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_SERVER
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_STARTCODE
argument_list|)
expr_stmt|;
name|server
operator|.
name|batchUpdate
argument_list|(
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|b
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
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
literal|"Updated columns in row: "
operator|+
name|i
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|master
operator|.
name|regionManager
init|)
block|{
if|if
condition|(
name|online
condition|)
block|{
comment|// Bring offline regions on-line
if|if
condition|(
operator|!
name|master
operator|.
name|regionManager
operator|.
name|assignable
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|master
operator|.
name|regionManager
operator|.
name|setUnassigned
argument_list|(
name|i
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Prevent region from getting assigned.
name|master
operator|.
name|regionManager
operator|.
name|removeRegion
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Process regions currently being served
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
literal|"processing regions currently being served"
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|master
operator|.
name|regionManager
init|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|e
range|:
name|servedRegions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|serverName
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|online
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Already online"
argument_list|)
expr_stmt|;
continue|continue;
comment|// Already being served
block|}
comment|// Cause regions being served to be taken off-line and disabled
for|for
control|(
name|HRegionInfo
name|i
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
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
literal|"adding region "
operator|+
name|i
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" to kill list"
argument_list|)
expr_stmt|;
block|}
comment|// this marks the regions to be closed
name|master
operator|.
name|regionManager
operator|.
name|setClosing
argument_list|(
name|serverName
argument_list|,
name|i
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|servedRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|updateRegionInfo
parameter_list|(
specifier|final
name|BatchUpdate
name|b
parameter_list|,
specifier|final
name|HRegionInfo
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|i
operator|.
name|setOffline
argument_list|(
operator|!
name|online
argument_list|)
expr_stmt|;
name|b
operator|.
name|put
argument_list|(
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

