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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|List
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|lang
operator|.
name|StringUtils
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
name|backup
operator|.
name|util
operator|.
name|BackupUtils
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
operator|.
name|BackupInfo
operator|.
name|Builder
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
comment|/**  * An object to encapsulate the information for each backup session  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BackupInfo
implements|implements
name|Comparable
argument_list|<
name|BackupInfo
argument_list|>
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
name|BackupInfo
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
interface|interface
name|Filter
block|{
comment|/**      * Filter interface      * @param info backup info      * @return true if info passes filter, false otherwise      */
specifier|public
name|boolean
name|apply
parameter_list|(
name|BackupInfo
name|info
parameter_list|)
function_decl|;
block|}
comment|/**    * Backup session states    */
specifier|public
specifier|static
enum|enum
name|BackupState
block|{
name|RUNNING
block|,
name|COMPLETE
block|,
name|FAILED
block|,
name|ANY
block|;   }
comment|/**    * BackupPhase - phases of an ACTIVE backup session (running), when state of a backup session is    * BackupState.RUNNING    */
specifier|public
specifier|static
enum|enum
name|BackupPhase
block|{
name|REQUEST
block|,
name|SNAPSHOT
block|,
name|PREPARE_INCREMENTAL
block|,
name|SNAPSHOTCOPY
block|,
name|INCREMENTAL_COPY
block|,
name|STORE_MANIFEST
block|;   }
comment|/**    * Backup id    */
specifier|private
name|String
name|backupId
decl_stmt|;
comment|/**    * Backup type, full or incremental    */
specifier|private
name|BackupType
name|type
decl_stmt|;
comment|/**    * Target root directory for storing the backup files    */
specifier|private
name|String
name|backupRootDir
decl_stmt|;
comment|/**    * Backup state    */
specifier|private
name|BackupState
name|state
decl_stmt|;
comment|/**    * Backup phase    */
specifier|private
name|BackupPhase
name|phase
init|=
name|BackupPhase
operator|.
name|REQUEST
decl_stmt|;
comment|/**    * Backup failure message    */
specifier|private
name|String
name|failedMsg
decl_stmt|;
comment|/**    * Backup status map for all tables    */
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
name|backupTableInfoMap
decl_stmt|;
comment|/**    * Actual start timestamp of a backup process    */
specifier|private
name|long
name|startTs
decl_stmt|;
comment|/**    * Actual end timestamp of the backup process    */
specifier|private
name|long
name|completeTs
decl_stmt|;
comment|/**    * Total bytes of incremental logs copied    */
specifier|private
name|long
name|totalBytesCopied
decl_stmt|;
comment|/**    * For incremental backup, a location of a backed-up hlogs    */
specifier|private
name|String
name|hlogTargetDir
init|=
literal|null
decl_stmt|;
comment|/**    * Incremental backup file list    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|incrBackupFileList
decl_stmt|;
comment|/**    * New region server log timestamps for table set after distributed log roll key - table name,    * value - map of RegionServer hostname -> last log rolled timestamp    */
specifier|private
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|tableSetTimestampMap
decl_stmt|;
comment|/**    * Backup progress in %% (0-100)    */
specifier|private
name|int
name|progress
decl_stmt|;
comment|/**    * Number of parallel workers. -1 - system defined    */
specifier|private
name|int
name|workers
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Bandwidth per worker in MB per sec. -1 - unlimited    */
specifier|private
name|long
name|bandwidth
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|BackupInfo
parameter_list|()
block|{
name|backupTableInfoMap
operator|=
operator|new
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|BackupInfo
parameter_list|(
name|String
name|backupId
parameter_list|,
name|BackupType
name|type
parameter_list|,
name|TableName
index|[]
name|tables
parameter_list|,
name|String
name|targetRootDir
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|backupId
operator|=
name|backupId
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|backupRootDir
operator|=
name|targetRootDir
expr_stmt|;
name|this
operator|.
name|addTables
argument_list|(
name|tables
argument_list|)
expr_stmt|;
if|if
condition|(
name|type
operator|==
name|BackupType
operator|.
name|INCREMENTAL
condition|)
block|{
name|setHLogTargetDir
argument_list|(
name|BackupUtils
operator|.
name|getLogBackupDir
argument_list|(
name|targetRootDir
argument_list|,
name|backupId
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|startTs
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|completeTs
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|int
name|getWorkers
parameter_list|()
block|{
return|return
name|workers
return|;
block|}
specifier|public
name|void
name|setWorkers
parameter_list|(
name|int
name|workers
parameter_list|)
block|{
name|this
operator|.
name|workers
operator|=
name|workers
expr_stmt|;
block|}
specifier|public
name|long
name|getBandwidth
parameter_list|()
block|{
return|return
name|bandwidth
return|;
block|}
specifier|public
name|void
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
block|}
specifier|public
name|void
name|setBackupTableInfoMap
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
name|backupTableInfoMap
parameter_list|)
block|{
name|this
operator|.
name|backupTableInfoMap
operator|=
name|backupTableInfoMap
expr_stmt|;
block|}
specifier|public
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getTableSetTimestampMap
parameter_list|()
block|{
return|return
name|tableSetTimestampMap
return|;
block|}
specifier|public
name|void
name|setTableSetTimestampMap
parameter_list|(
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|tableSetTimestampMap
parameter_list|)
block|{
name|this
operator|.
name|tableSetTimestampMap
operator|=
name|tableSetTimestampMap
expr_stmt|;
block|}
specifier|public
name|void
name|setType
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
block|}
specifier|public
name|void
name|setBackupRootDir
parameter_list|(
name|String
name|targetRootDir
parameter_list|)
block|{
name|this
operator|.
name|backupRootDir
operator|=
name|targetRootDir
expr_stmt|;
block|}
specifier|public
name|void
name|setTotalBytesCopied
parameter_list|(
name|long
name|totalBytesCopied
parameter_list|)
block|{
name|this
operator|.
name|totalBytesCopied
operator|=
name|totalBytesCopied
expr_stmt|;
block|}
comment|/**    * Set progress (0-100%)    * @param p progress value    */
specifier|public
name|void
name|setProgress
parameter_list|(
name|int
name|p
parameter_list|)
block|{
name|this
operator|.
name|progress
operator|=
name|p
expr_stmt|;
block|}
comment|/**    * Get current progress    */
specifier|public
name|int
name|getProgress
parameter_list|()
block|{
return|return
name|progress
return|;
block|}
specifier|public
name|String
name|getBackupId
parameter_list|()
block|{
return|return
name|backupId
return|;
block|}
specifier|public
name|void
name|setBackupId
parameter_list|(
name|String
name|backupId
parameter_list|)
block|{
name|this
operator|.
name|backupId
operator|=
name|backupId
expr_stmt|;
block|}
specifier|public
name|BackupTableInfo
name|getBackupTableInfo
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
return|return
name|this
operator|.
name|backupTableInfoMap
operator|.
name|get
argument_list|(
name|table
argument_list|)
return|;
block|}
specifier|public
name|String
name|getFailedMsg
parameter_list|()
block|{
return|return
name|failedMsg
return|;
block|}
specifier|public
name|void
name|setFailedMsg
parameter_list|(
name|String
name|failedMsg
parameter_list|)
block|{
name|this
operator|.
name|failedMsg
operator|=
name|failedMsg
expr_stmt|;
block|}
specifier|public
name|long
name|getStartTs
parameter_list|()
block|{
return|return
name|startTs
return|;
block|}
specifier|public
name|void
name|setStartTs
parameter_list|(
name|long
name|startTs
parameter_list|)
block|{
name|this
operator|.
name|startTs
operator|=
name|startTs
expr_stmt|;
block|}
specifier|public
name|long
name|getCompleteTs
parameter_list|()
block|{
return|return
name|completeTs
return|;
block|}
specifier|public
name|void
name|setCompleteTs
parameter_list|(
name|long
name|endTs
parameter_list|)
block|{
name|this
operator|.
name|completeTs
operator|=
name|endTs
expr_stmt|;
block|}
specifier|public
name|long
name|getTotalBytesCopied
parameter_list|()
block|{
return|return
name|totalBytesCopied
return|;
block|}
specifier|public
name|BackupState
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|void
name|setState
parameter_list|(
name|BackupState
name|flag
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|flag
expr_stmt|;
block|}
specifier|public
name|BackupPhase
name|getPhase
parameter_list|()
block|{
return|return
name|phase
return|;
block|}
specifier|public
name|void
name|setPhase
parameter_list|(
name|BackupPhase
name|phase
parameter_list|)
block|{
name|this
operator|.
name|phase
operator|=
name|phase
expr_stmt|;
block|}
specifier|public
name|BackupType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|void
name|setSnapshotName
parameter_list|(
name|TableName
name|table
parameter_list|,
name|String
name|snapshotName
parameter_list|)
block|{
name|this
operator|.
name|backupTableInfoMap
operator|.
name|get
argument_list|(
name|table
argument_list|)
operator|.
name|setSnapshotName
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getSnapshotName
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
return|return
name|this
operator|.
name|backupTableInfoMap
operator|.
name|get
argument_list|(
name|table
argument_list|)
operator|.
name|getSnapshotName
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSnapshotNames
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|snapshotNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BackupTableInfo
name|backupStatus
range|:
name|this
operator|.
name|backupTableInfoMap
operator|.
name|values
argument_list|()
control|)
block|{
name|snapshotNames
operator|.
name|add
argument_list|(
name|backupStatus
operator|.
name|getSnapshotName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|snapshotNames
return|;
block|}
specifier|public
name|Set
argument_list|<
name|TableName
argument_list|>
name|getTables
parameter_list|()
block|{
return|return
name|this
operator|.
name|backupTableInfoMap
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|TableName
argument_list|>
name|getTableNames
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|TableName
argument_list|>
argument_list|(
name|backupTableInfoMap
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|addTables
parameter_list|(
name|TableName
index|[]
name|tables
parameter_list|)
block|{
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|BackupTableInfo
name|backupStatus
init|=
operator|new
name|BackupTableInfo
argument_list|(
name|table
argument_list|,
name|this
operator|.
name|backupRootDir
argument_list|,
name|this
operator|.
name|backupId
argument_list|)
decl_stmt|;
name|this
operator|.
name|backupTableInfoMap
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|backupStatus
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setTables
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|)
block|{
name|this
operator|.
name|backupTableInfoMap
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|TableName
name|table
range|:
name|tables
control|)
block|{
name|BackupTableInfo
name|backupStatus
init|=
operator|new
name|BackupTableInfo
argument_list|(
name|table
argument_list|,
name|this
operator|.
name|backupRootDir
argument_list|,
name|this
operator|.
name|backupId
argument_list|)
decl_stmt|;
name|this
operator|.
name|backupTableInfoMap
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|backupStatus
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getBackupRootDir
parameter_list|()
block|{
return|return
name|backupRootDir
return|;
block|}
specifier|public
name|String
name|getTableBackupDir
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|BackupUtils
operator|.
name|getTableBackupDir
argument_list|(
name|backupRootDir
argument_list|,
name|backupId
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|void
name|setHLogTargetDir
parameter_list|(
name|String
name|hlogTagetDir
parameter_list|)
block|{
name|this
operator|.
name|hlogTargetDir
operator|=
name|hlogTagetDir
expr_stmt|;
block|}
specifier|public
name|String
name|getHLogTargetDir
parameter_list|()
block|{
return|return
name|hlogTargetDir
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getIncrBackupFileList
parameter_list|()
block|{
return|return
name|incrBackupFileList
return|;
block|}
specifier|public
name|void
name|setIncrBackupFileList
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|incrBackupFileList
parameter_list|)
block|{
name|this
operator|.
name|incrBackupFileList
operator|=
name|incrBackupFileList
expr_stmt|;
block|}
comment|/**    * Set the new region server log timestamps after distributed log roll    * @param newTableSetTimestampMap table timestamp map    */
specifier|public
name|void
name|setIncrTimestampMap
parameter_list|(
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|newTableSetTimestampMap
parameter_list|)
block|{
name|this
operator|.
name|tableSetTimestampMap
operator|=
name|newTableSetTimestampMap
expr_stmt|;
block|}
comment|/**    * Get new region server log timestamps after distributed log roll    * @return new region server log timestamps    */
specifier|public
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getIncrTimestampMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableSetTimestampMap
return|;
block|}
specifier|public
name|TableName
name|getTableBySnapshot
parameter_list|(
name|String
name|snapshotName
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
name|entry
range|:
name|this
operator|.
name|backupTableInfoMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|snapshotName
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getSnapshotName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|entry
operator|.
name|getKey
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|BackupProtos
operator|.
name|BackupInfo
name|toProtosBackupInfo
parameter_list|()
block|{
name|BackupProtos
operator|.
name|BackupInfo
operator|.
name|Builder
name|builder
init|=
name|BackupProtos
operator|.
name|BackupInfo
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setBackupId
argument_list|(
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
name|setBackupTableInfoMap
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCompleteTs
argument_list|(
name|getCompleteTs
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|getFailedMsg
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setFailedMessage
argument_list|(
name|getFailedMsg
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getState
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setBackupState
argument_list|(
name|BackupProtos
operator|.
name|BackupInfo
operator|.
name|BackupState
operator|.
name|valueOf
argument_list|(
name|getState
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getPhase
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setBackupPhase
argument_list|(
name|BackupProtos
operator|.
name|BackupInfo
operator|.
name|BackupPhase
operator|.
name|valueOf
argument_list|(
name|getPhase
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setProgress
argument_list|(
name|getProgress
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setStartTs
argument_list|(
name|getStartTs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBackupRootDir
argument_list|(
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBackupType
argument_list|(
name|BackupProtos
operator|.
name|BackupType
operator|.
name|valueOf
argument_list|(
name|getType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setWorkersNumber
argument_list|(
name|workers
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBandwidth
argument_list|(
name|bandwidth
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|33
operator|*
name|type
operator|.
name|hashCode
argument_list|()
operator|+
name|backupId
operator|!=
literal|null
condition|?
name|backupId
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
if|if
condition|(
name|backupRootDir
operator|!=
literal|null
condition|)
block|{
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
name|backupRootDir
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
name|state
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
name|phase
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
call|(
name|int
call|)
argument_list|(
name|startTs
operator|^
operator|(
name|startTs
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
call|(
name|int
call|)
argument_list|(
name|completeTs
operator|^
operator|(
name|completeTs
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
call|(
name|int
call|)
argument_list|(
name|totalBytesCopied
operator|^
operator|(
name|totalBytesCopied
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|hlogTargetDir
operator|!=
literal|null
condition|)
block|{
name|hash
operator|=
literal|33
operator|*
name|hash
operator|+
name|hlogTargetDir
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
return|return
name|hash
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|instanceof
name|BackupInfo
condition|)
block|{
name|BackupInfo
name|other
init|=
operator|(
name|BackupInfo
operator|)
name|obj
decl_stmt|;
try|try
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|toByteArray
argument_list|()
argument_list|,
name|other
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
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
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|backupId
return|;
block|}
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|toProtosBackupInfo
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
specifier|private
name|void
name|setBackupTableInfoMap
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
name|entry
range|:
name|backupTableInfoMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|addBackupTableInfo
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toProto
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|BackupInfo
name|fromByteArray
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromProto
argument_list|(
name|BackupProtos
operator|.
name|BackupInfo
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|BackupInfo
name|fromStream
parameter_list|(
specifier|final
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromProto
argument_list|(
name|BackupProtos
operator|.
name|BackupInfo
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|BackupInfo
name|fromProto
parameter_list|(
name|BackupProtos
operator|.
name|BackupInfo
name|proto
parameter_list|)
block|{
name|BackupInfo
name|context
init|=
operator|new
name|BackupInfo
argument_list|()
decl_stmt|;
name|context
operator|.
name|setBackupId
argument_list|(
name|proto
operator|.
name|getBackupId
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|setBackupTableInfoMap
argument_list|(
name|toMap
argument_list|(
name|proto
operator|.
name|getBackupTableInfoList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|setCompleteTs
argument_list|(
name|proto
operator|.
name|getCompleteTs
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasFailedMessage
argument_list|()
condition|)
block|{
name|context
operator|.
name|setFailedMsg
argument_list|(
name|proto
operator|.
name|getFailedMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasBackupState
argument_list|()
condition|)
block|{
name|context
operator|.
name|setState
argument_list|(
name|BackupInfo
operator|.
name|BackupState
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getBackupState
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|setHLogTargetDir
argument_list|(
name|BackupUtils
operator|.
name|getLogBackupDir
argument_list|(
name|proto
operator|.
name|getBackupRootDir
argument_list|()
argument_list|,
name|proto
operator|.
name|getBackupId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasBackupPhase
argument_list|()
condition|)
block|{
name|context
operator|.
name|setPhase
argument_list|(
name|BackupPhase
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getBackupPhase
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasProgress
argument_list|()
condition|)
block|{
name|context
operator|.
name|setProgress
argument_list|(
name|proto
operator|.
name|getProgress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|setStartTs
argument_list|(
name|proto
operator|.
name|getStartTs
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|setBackupRootDir
argument_list|(
name|proto
operator|.
name|getBackupRootDir
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|setType
argument_list|(
name|BackupType
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getBackupType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|setWorkers
argument_list|(
name|proto
operator|.
name|getWorkersNumber
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|setBandwidth
argument_list|(
name|proto
operator|.
name|getBandwidth
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|context
return|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
name|toMap
parameter_list|(
name|List
argument_list|<
name|BackupProtos
operator|.
name|BackupTableInfo
argument_list|>
name|list
parameter_list|)
block|{
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|BackupTableInfo
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|BackupProtos
operator|.
name|BackupTableInfo
name|tbs
range|:
name|list
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|tbs
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|BackupTableInfo
operator|.
name|convert
argument_list|(
name|tbs
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
specifier|public
name|String
name|getShortDescription
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"ID="
operator|+
name|backupId
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Type="
operator|+
name|getType
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Tables="
operator|+
name|getTableListAsString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"State="
operator|+
name|getState
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|Date
name|date
init|=
literal|null
decl_stmt|;
name|Calendar
name|cal
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal
operator|.
name|setTimeInMillis
argument_list|(
name|getStartTs
argument_list|()
argument_list|)
expr_stmt|;
name|date
operator|=
name|cal
operator|.
name|getTime
argument_list|()
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Start time="
operator|+
name|date
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
if|if
condition|(
name|state
operator|==
name|BackupState
operator|.
name|FAILED
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Failed message="
operator|+
name|getFailedMsg
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|BackupState
operator|.
name|RUNNING
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Phase="
operator|+
name|getPhase
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|BackupState
operator|.
name|COMPLETE
condition|)
block|{
name|cal
operator|=
name|Calendar
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|cal
operator|.
name|setTimeInMillis
argument_list|(
name|getCompleteTs
argument_list|()
argument_list|)
expr_stmt|;
name|date
operator|=
name|cal
operator|.
name|getTime
argument_list|()
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"End time="
operator|+
name|date
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"Progress="
operator|+
name|getProgress
argument_list|()
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getStatusAndProgressAsString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"id: "
argument_list|)
operator|.
name|append
argument_list|(
name|getBackupId
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" state: "
argument_list|)
operator|.
name|append
argument_list|(
name|getState
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" progress: "
argument_list|)
operator|.
name|append
argument_list|(
name|getProgress
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getTableListAsString
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|backupTableInfoMap
operator|.
name|keySet
argument_list|()
argument_list|,
literal|","
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * We use only time stamps to compare objects during sort operation    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|BackupInfo
name|o
parameter_list|)
block|{
name|Long
name|thisTS
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|getBackupId
argument_list|()
operator|.
name|substring
argument_list|(
name|this
operator|.
name|getBackupId
argument_list|()
operator|.
name|lastIndexOf
argument_list|(
literal|"_"
argument_list|)
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|Long
name|otherTS
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|o
operator|.
name|getBackupId
argument_list|()
operator|.
name|substring
argument_list|(
name|o
operator|.
name|getBackupId
argument_list|()
operator|.
name|lastIndexOf
argument_list|(
literal|"_"
argument_list|)
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|thisTS
operator|.
name|compareTo
argument_list|(
name|otherTS
argument_list|)
return|;
block|}
block|}
end_class

end_unit

