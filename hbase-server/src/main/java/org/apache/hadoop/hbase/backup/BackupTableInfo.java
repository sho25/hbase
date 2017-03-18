begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|backup
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|BackupProtos
import|;
end_import

begin_comment
comment|/**  * Backup related information encapsulated for a table. At this moment only target directory,  * snapshot name and table name are encapsulated here.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BackupTableInfo
block|{
comment|/*    *  Table name for backup    */
specifier|private
name|TableName
name|table
decl_stmt|;
comment|/*    *  Snapshot name for offline/online snapshot    */
specifier|private
name|String
name|snapshotName
init|=
literal|null
decl_stmt|;
specifier|public
name|BackupTableInfo
parameter_list|()
block|{    }
specifier|public
name|BackupTableInfo
parameter_list|(
name|TableName
name|table
parameter_list|,
name|String
name|targetRootDir
parameter_list|,
name|String
name|backupId
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|String
name|getSnapshotName
parameter_list|()
block|{
return|return
name|snapshotName
return|;
block|}
specifier|public
name|void
name|setSnapshotName
parameter_list|(
name|String
name|snapshotName
parameter_list|)
block|{
name|this
operator|.
name|snapshotName
operator|=
name|snapshotName
expr_stmt|;
block|}
specifier|public
name|TableName
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
specifier|static
name|BackupTableInfo
name|convert
parameter_list|(
name|BackupProtos
operator|.
name|BackupTableInfo
name|proto
parameter_list|)
block|{
name|BackupTableInfo
name|bs
init|=
operator|new
name|BackupTableInfo
argument_list|()
decl_stmt|;
name|bs
operator|.
name|table
operator|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|proto
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasSnapshotName
argument_list|()
condition|)
block|{
name|bs
operator|.
name|snapshotName
operator|=
name|proto
operator|.
name|getSnapshotName
argument_list|()
expr_stmt|;
block|}
return|return
name|bs
return|;
block|}
specifier|public
name|BackupProtos
operator|.
name|BackupTableInfo
name|toProto
parameter_list|()
block|{
name|BackupProtos
operator|.
name|BackupTableInfo
operator|.
name|Builder
name|builder
init|=
name|BackupProtos
operator|.
name|BackupTableInfo
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshotName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setSnapshotName
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setTableName
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

