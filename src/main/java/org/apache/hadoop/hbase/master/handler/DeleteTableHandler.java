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
name|catalog
operator|.
name|MetaEditor
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
name|MasterServices
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
name|Threads
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

begin_class
specifier|public
class|class
name|DeleteTableHandler
extends|extends
name|TableEventHandler
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
name|DeleteTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|DeleteTableHandler
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|EventType
operator|.
name|C_M_DELETE_TABLE
argument_list|,
name|tableName
argument_list|,
name|server
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|handleTableOperation
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|AssignmentManager
name|am
init|=
name|this
operator|.
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|long
name|waitTime
init|=
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.master.wait.on.region"
argument_list|,
literal|5
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|long
name|done
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|waitTime
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|done
condition|)
block|{
name|AssignmentManager
operator|.
name|RegionState
name|rs
init|=
name|am
operator|.
name|isRegionInTransition
argument_list|(
name|region
argument_list|)
decl_stmt|;
if|if
condition|(
name|rs
operator|==
literal|null
condition|)
break|break;
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting on  region to clear regions in transition; "
operator|+
name|rs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|am
operator|.
name|isRegionInTransition
argument_list|(
name|region
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Waited hbase.master.wait.on.region ("
operator|+
name|waitTime
operator|+
literal|"ms) for region to leave region "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" in transitions"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting region "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" from META and FS"
argument_list|)
expr_stmt|;
comment|// Remove region from META
name|MetaEditor
operator|.
name|deleteRegion
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|region
argument_list|)
expr_stmt|;
comment|// Delete region from FS
name|this
operator|.
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|deleteRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
comment|// Delete table from FS
name|this
operator|.
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// If entry for this table in zk, and up in AssignmentManager, remove it.
comment|// Call to undisableTable does this. TODO: Make a more formal purge table.
name|am
operator|.
name|getZKTable
argument_list|()
operator|.
name|setEnabledTable
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

