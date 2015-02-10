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
name|CoordinatedStateException
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
name|TableDescriptor
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
name|MetaTableAccessor
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
name|TableState
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
name|util
operator|.
name|FSTableDescriptors
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
name|FSUtils
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
name|ModifyRegionUtils
import|;
end_import

begin_comment
comment|/**  * Truncate the table by removing META and the HDFS files and recreating it.  * If the 'preserveSplits' option is set to true, the region splits are preserved on recreate.  *  * If the operation fails in the middle it may require hbck to fix the system state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TruncateTableHandler
extends|extends
name|DeleteTableHandler
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
name|TruncateTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|preserveSplits
decl_stmt|;
specifier|public
name|TruncateTableHandler
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|,
name|boolean
name|preserveSplits
parameter_list|)
block|{
name|super
argument_list|(
name|tableName
argument_list|,
name|server
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
name|this
operator|.
name|preserveSplits
operator|=
name|preserveSplits
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
name|CoordinatedStateException
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
name|getMasterCoprocessorHost
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
name|preTruncateTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// 1. Wait because of region in transition
name|waitRegionInTransition
argument_list|(
name|regions
argument_list|)
expr_stmt|;
comment|// 2. Remove table from hbase:meta and HDFS
name|removeTableData
argument_list|(
name|regions
argument_list|)
expr_stmt|;
comment|// -----------------------------------------------------------------------
comment|// PONR: At this point the table is deleted.
comment|//       If the recreate fails, the user can only re-create the table.
comment|// -----------------------------------------------------------------------
comment|// 3. Recreate the regions
name|recreateTable
argument_list|(
name|regions
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postTruncateTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|recreateTable
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
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
name|tempdir
init|=
name|mfs
operator|.
name|getTempDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|mfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|AssignmentManager
name|assignmentManager
init|=
name|this
operator|.
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
comment|// 1. Create Table Descriptor
name|TableDescriptor
name|underConstruction
init|=
operator|new
name|TableDescriptor
argument_list|(
name|this
operator|.
name|hTableDescriptor
argument_list|)
decl_stmt|;
name|Path
name|tempTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|tempdir
argument_list|,
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
operator|(
call|(
name|FSTableDescriptors
call|)
argument_list|(
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
argument_list|)
operator|)
operator|.
name|createTableDescriptorForTableDirectory
argument_list|(
name|tempTableDir
argument_list|,
name|underConstruction
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|newRegions
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|preserveSplits
condition|)
block|{
name|newRegions
operator|=
name|regions
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionInfo
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Truncate will preserve "
operator|+
name|newRegions
operator|.
name|length
operator|+
literal|" regions"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newRegions
operator|=
operator|new
name|HRegionInfo
index|[
literal|1
index|]
expr_stmt|;
name|newRegions
index|[
literal|0
index|]
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Truncate will not preserve the regions"
argument_list|)
expr_stmt|;
block|}
comment|// 2. Create Regions
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
name|ModifyRegionUtils
operator|.
name|createRegions
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tempdir
argument_list|,
name|this
operator|.
name|hTableDescriptor
argument_list|,
name|newRegions
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// 3. Move Table temp directory to the hbase root location
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|tempTableDir
argument_list|,
name|tableDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to move table from temp="
operator|+
name|tempTableDir
operator|+
literal|" to hbase root="
operator|+
name|tableDir
argument_list|)
throw|;
block|}
comment|// populate descriptors cache to be visible in getAll
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
expr_stmt|;
comment|// 4. Add regions to META
name|MetaTableAccessor
operator|.
name|addRegionsToMeta
argument_list|(
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regionInfos
argument_list|,
name|hTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
expr_stmt|;
comment|// 5. Trigger immediate assignment of the regions in round-robin fashion
name|ModifyRegionUtils
operator|.
name|assignRegions
argument_list|(
name|assignmentManager
argument_list|,
name|regionInfos
argument_list|)
expr_stmt|;
comment|// 6. Set table enabled flag up in zk.
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

