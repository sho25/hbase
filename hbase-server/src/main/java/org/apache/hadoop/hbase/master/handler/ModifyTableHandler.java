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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|hbase
operator|.
name|HConstants
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
name|HTableDescriptor
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
name|HTable
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|Table
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ModifyTableHandler
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
name|ModifyTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|public
name|ModifyTableHandler
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
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
name|C_M_MODIFY_TABLE
argument_list|,
name|tableName
argument_list|,
name|server
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
comment|// This is the new schema we are going to write out as this modification.
name|this
operator|.
name|htd
operator|=
name|htd
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
name|super
operator|.
name|prepareWithTableLock
argument_list|()
expr_stmt|;
comment|// Check operation is possible on the table in its current state
comment|// Also checks whether the table exists
if|if
condition|(
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|this
operator|.
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
operator|&&
name|this
operator|.
name|htd
operator|.
name|getRegionReplication
argument_list|()
operator|!=
name|getTableDescriptor
argument_list|()
operator|.
name|getHTableDescriptor
argument_list|()
operator|.
name|getRegionReplication
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"REGION_REPLICATION change is not supported for enabled tables"
argument_list|)
throw|;
block|}
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
name|hris
parameter_list|)
throws|throws
name|IOException
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
name|preModifyTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|htd
argument_list|)
expr_stmt|;
block|}
comment|// Update descriptor
name|HTableDescriptor
name|oldDescriptor
init|=
name|this
operator|.
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|this
operator|.
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|deleteFamilyFromFS
argument_list|(
name|hris
argument_list|,
name|oldDescriptor
operator|.
name|getFamiliesKeys
argument_list|()
argument_list|)
expr_stmt|;
name|removeReplicaColumnsIfNeeded
argument_list|(
name|this
operator|.
name|htd
operator|.
name|getRegionReplication
argument_list|()
argument_list|,
name|oldDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|,
name|this
operator|.
name|htd
operator|.
name|getTableName
argument_list|()
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
name|postModifyTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|htd
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|removeReplicaColumnsIfNeeded
parameter_list|(
name|int
name|newReplicaCount
parameter_list|,
name|int
name|oldReplicaCount
parameter_list|,
name|TableName
name|table
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|newReplicaCount
operator|>=
name|oldReplicaCount
condition|)
return|return;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|tableRows
init|=
operator|new
name|HashSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|Table
name|htable
init|=
literal|null
decl_stmt|;
try|try
block|{
name|htable
operator|=
operator|new
name|HTable
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|ResultScanner
name|resScanner
init|=
name|htable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|resScanner
control|)
block|{
name|tableRows
operator|.
name|add
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MetaTableAccessor
operator|.
name|removeRegionReplicasFromMeta
argument_list|(
name|tableRows
argument_list|,
name|newReplicaCount
argument_list|,
name|oldReplicaCount
operator|-
name|newReplicaCount
argument_list|,
name|masterServices
operator|.
name|getShortCircuitConnection
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|htable
operator|!=
literal|null
condition|)
block|{
name|htable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Removes from hdfs the families that are not longer present in the new table descriptor.    */
specifier|private
name|void
name|deleteFamilyFromFS
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
parameter_list|,
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|oldFamilies
parameter_list|)
block|{
try|try
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|newFamilies
init|=
name|this
operator|.
name|htd
operator|.
name|getFamiliesKeys
argument_list|()
decl_stmt|;
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
for|for
control|(
name|byte
index|[]
name|familyName
range|:
name|oldFamilies
control|)
block|{
if|if
condition|(
operator|!
name|newFamilies
operator|.
name|contains
argument_list|(
name|familyName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing family="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
operator|+
literal|" from table="
operator|+
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
comment|// Delete the family directory in FS for all the regions one by one
name|mfs
operator|.
name|deleteFamilyFromFS
argument_list|(
name|hri
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to remove on-disk directories for the removed families"
argument_list|,
name|e
argument_list|)
expr_stmt|;
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

