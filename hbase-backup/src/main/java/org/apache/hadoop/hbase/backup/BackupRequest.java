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

begin_comment
comment|/**  * POJO class for backup request  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|BackupRequest
block|{
specifier|public
specifier|static
class|class
name|Builder
block|{
name|BackupRequest
name|request
decl_stmt|;
specifier|public
name|Builder
parameter_list|()
block|{
name|request
operator|=
operator|new
name|BackupRequest
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Builder
name|withBackupType
parameter_list|(
name|BackupType
name|type
parameter_list|)
block|{
name|request
operator|.
name|setBackupType
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withTableList
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|)
block|{
name|request
operator|.
name|setTableList
argument_list|(
name|tables
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withTargetRootDir
parameter_list|(
name|String
name|backupDir
parameter_list|)
block|{
name|request
operator|.
name|setTargetRootDir
argument_list|(
name|backupDir
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withBackupSetName
parameter_list|(
name|String
name|setName
parameter_list|)
block|{
name|request
operator|.
name|setBackupSetName
argument_list|(
name|setName
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withTotalTasks
parameter_list|(
name|int
name|numTasks
parameter_list|)
block|{
name|request
operator|.
name|setTotalTasks
argument_list|(
name|numTasks
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withBandwidthPerTasks
parameter_list|(
name|int
name|bandwidth
parameter_list|)
block|{
name|request
operator|.
name|setBandwidth
argument_list|(
name|bandwidth
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withYarnPoolName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|request
operator|.
name|setYarnPoolName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BackupRequest
name|build
parameter_list|()
block|{
return|return
name|request
return|;
block|}
block|}
specifier|private
name|BackupType
name|type
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableName
argument_list|>
name|tableList
decl_stmt|;
specifier|private
name|String
name|targetRootDir
decl_stmt|;
specifier|private
name|int
name|totalTasks
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|bandwidth
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|String
name|backupSetName
decl_stmt|;
specifier|private
name|String
name|yarnPoolName
decl_stmt|;
specifier|private
name|BackupRequest
parameter_list|()
block|{   }
specifier|private
name|BackupRequest
name|setBackupType
parameter_list|(
name|BackupType
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|BackupType
name|getBackupType
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
specifier|private
name|BackupRequest
name|setTableList
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tableList
parameter_list|)
block|{
name|this
operator|.
name|tableList
operator|=
name|tableList
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|List
argument_list|<
name|TableName
argument_list|>
name|getTableList
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableList
return|;
block|}
specifier|private
name|BackupRequest
name|setTargetRootDir
parameter_list|(
name|String
name|targetRootDir
parameter_list|)
block|{
name|this
operator|.
name|targetRootDir
operator|=
name|targetRootDir
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getTargetRootDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|targetRootDir
return|;
block|}
specifier|private
name|BackupRequest
name|setTotalTasks
parameter_list|(
name|int
name|totalTasks
parameter_list|)
block|{
name|this
operator|.
name|totalTasks
operator|=
name|totalTasks
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|int
name|getTotalTasks
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalTasks
return|;
block|}
specifier|private
name|BackupRequest
name|setBandwidth
parameter_list|(
name|long
name|bandwidth
parameter_list|)
block|{
name|this
operator|.
name|bandwidth
operator|=
name|bandwidth
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|long
name|getBandwidth
parameter_list|()
block|{
return|return
name|this
operator|.
name|bandwidth
return|;
block|}
specifier|public
name|String
name|getBackupSetName
parameter_list|()
block|{
return|return
name|backupSetName
return|;
block|}
specifier|private
name|BackupRequest
name|setBackupSetName
parameter_list|(
name|String
name|backupSetName
parameter_list|)
block|{
name|this
operator|.
name|backupSetName
operator|=
name|backupSetName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getYarnPoolName
parameter_list|()
block|{
return|return
name|yarnPoolName
return|;
block|}
specifier|public
name|void
name|setYarnPoolName
parameter_list|(
name|String
name|yarnPoolName
parameter_list|)
block|{
name|this
operator|.
name|yarnPoolName
operator|=
name|yarnPoolName
expr_stmt|;
block|}
block|}
end_class

end_unit

