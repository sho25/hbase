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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|TableName
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
name|backup
operator|.
name|HFileArchiver
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
name|executor
operator|.
name|EventType
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
name|HMaster
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
name|MasterCoprocessorHost
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
name|MasterFileSystem
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
name|master
operator|.
name|RegionStates
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
operator|.
name|State
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
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
name|TableName
name|tableName
parameter_list|,
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
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
name|prepareWithTableLock
parameter_list|()
throws|throws
name|IOException
block|{
comment|// The next call fails if no such table.
name|getTableDescriptor
argument_list|()
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
name|MasterCoprocessorHost
name|cpHost
init|=
operator|(
operator|(
name|HMaster
operator|)
name|this
operator|.
name|server
operator|)
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|preDeleteTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// 1. Wait because of region in transition
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
name|RegionStates
name|states
init|=
name|am
operator|.
name|getRegionStates
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
if|if
condition|(
name|states
operator|.
name|isRegionInState
argument_list|(
name|region
argument_list|,
name|State
operator|.
name|FAILED_OPEN
argument_list|)
condition|)
block|{
name|am
operator|.
name|regionOffline
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|states
operator|.
name|isRegionInTransition
argument_list|(
name|region
argument_list|)
condition|)
break|break;
name|Threads
operator|.
name|sleep
argument_list|(
name|waitingTimeForEvents
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting on region to clear regions in transition; "
operator|+
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionTransitionState
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|states
operator|.
name|isRegionInTransition
argument_list|(
name|region
argument_list|)
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
block|}
comment|// 2. Remove regions from META
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting regions from META"
argument_list|)
expr_stmt|;
name|MetaEditor
operator|.
name|deleteRegions
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|regions
argument_list|)
expr_stmt|;
comment|// 3. Move the table in /hbase/.tmp
name|MasterFileSystem
name|mfs
init|=
name|this
operator|.
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|Path
name|tempTableDir
init|=
name|mfs
operator|.
name|moveTableToTemp
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
comment|// 4. Delete regions from FS (temp directory)
name|FileSystem
name|fs
init|=
name|mfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Archiving region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" from FS"
argument_list|)
expr_stmt|;
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|fs
argument_list|,
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tempTableDir
argument_list|,
operator|new
name|Path
argument_list|(
name|tempTableDir
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// 5. Delete table from FS (temp directory)
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|tempTableDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't delete "
operator|+
name|tempTableDir
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table '"
operator|+
name|tableName
operator|+
literal|"' archived!"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// 6. Update table descriptor cache
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing '"
operator|+
name|tableName
operator|+
literal|"' descriptor."
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// 7. If entry for this table in zk, and up in AssignmentManager, remove it.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Marking '"
operator|+
name|tableName
operator|+
literal|"' as deleted."
argument_list|)
expr_stmt|;
name|am
operator|.
name|getZKTable
argument_list|()
operator|.
name|setDeletedTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postDeleteTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseTableLock
parameter_list|()
block|{
name|super
operator|.
name|releaseTableLock
argument_list|()
expr_stmt|;
try|try
block|{
name|masterServices
operator|.
name|getTableLockManager
argument_list|()
operator|.
name|tableDeleted
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received exception from TableLockManager.tableDeleted:"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
comment|//not critical
block|}
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
operator|+
literal|"-"
operator|+
name|tableName
return|;
block|}
block|}
end_class

end_unit

