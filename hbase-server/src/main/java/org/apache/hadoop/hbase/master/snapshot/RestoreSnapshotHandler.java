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
name|snapshot
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CancellationException
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
name|catalog
operator|.
name|CatalogTracker
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|exceptions
operator|.
name|RestoreSnapshotException
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
name|MetricsMaster
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
name|SnapshotSentinel
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
name|handler
operator|.
name|TableEventHandler
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
name|monitoring
operator|.
name|MonitoredTask
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
name|monitoring
operator|.
name|TaskMonitor
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
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|snapshot
operator|.
name|ClientSnapshotDescriptionUtils
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
name|snapshot
operator|.
name|RestoreSnapshotHelper
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
comment|/**  * Handler to Restore a snapshot.  *  *<p>Uses {@link RestoreSnapshotHelper} to replace the table content with the  * data available in the snapshot.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RestoreSnapshotHandler
extends|extends
name|TableEventHandler
implements|implements
name|SnapshotSentinel
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
name|RestoreSnapshotHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HTableDescriptor
name|hTableDescriptor
decl_stmt|;
specifier|private
specifier|final
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|private
specifier|final
name|ForeignExceptionDispatcher
name|monitor
decl_stmt|;
specifier|private
specifier|final
name|MetricsMaster
name|metricsMaster
decl_stmt|;
specifier|private
specifier|final
name|MonitoredTask
name|status
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|public
name|RestoreSnapshotHandler
parameter_list|(
specifier|final
name|MasterServices
name|masterServices
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|MetricsMaster
name|metricsMaster
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|EventType
operator|.
name|C_M_RESTORE_SNAPSHOT
argument_list|,
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|masterServices
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
name|this
operator|.
name|metricsMaster
operator|=
name|metricsMaster
expr_stmt|;
comment|// Snapshot information
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
comment|// Monitor
name|this
operator|.
name|monitor
operator|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
expr_stmt|;
comment|// Check table exists.
name|getTableDescriptor
argument_list|()
expr_stmt|;
comment|// This is the new schema we are going to write out as this modification.
name|this
operator|.
name|hTableDescriptor
operator|=
name|htd
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
literal|"Restoring  snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"' to table "
operator|+
name|hTableDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RestoreSnapshotHandler
name|prepare
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
name|RestoreSnapshotHandler
operator|)
name|super
operator|.
name|prepare
argument_list|()
return|;
block|}
comment|/**    * The restore table is executed in place.    *  - The on-disk data will be restored - reference files are put in place without moving data    *  -  [if something fail here: you need to delete the table and re-run the restore]    *  - META will be updated    *  -  [if something fail here: you need to run hbck to fix META entries]    * The passed in list gets changed in this method    */
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
name|MasterFileSystem
name|fileSystemManager
init|=
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|CatalogTracker
name|catalogTracker
init|=
name|masterServices
operator|.
name|getCatalogTracker
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|fileSystemManager
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootDir
init|=
name|fileSystemManager
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|byte
index|[]
name|tableName
init|=
name|hTableDescriptor
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|HTableDescriptor
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
comment|// 1. Update descriptor
name|this
operator|.
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|add
argument_list|(
name|hTableDescriptor
argument_list|)
expr_stmt|;
comment|// 2. Execute the on-disk Restore
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|RestoreSnapshotHelper
name|restoreHelper
init|=
operator|new
name|RestoreSnapshotHelper
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|snapshot
argument_list|,
name|snapshotDir
argument_list|,
name|hTableDescriptor
argument_list|,
name|tableDir
argument_list|,
name|monitor
argument_list|,
name|status
argument_list|)
decl_stmt|;
name|RestoreSnapshotHelper
operator|.
name|RestoreMetaChanges
name|metaChanges
init|=
name|restoreHelper
operator|.
name|restoreHdfsRegions
argument_list|()
decl_stmt|;
comment|// 3. Applies changes to .META.
name|hris
operator|.
name|clear
argument_list|()
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
literal|"Preparing to restore each region"
argument_list|)
expr_stmt|;
if|if
condition|(
name|metaChanges
operator|.
name|hasRegionsToAdd
argument_list|()
condition|)
name|hris
operator|.
name|addAll
argument_list|(
name|metaChanges
operator|.
name|getRegionsToAdd
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|metaChanges
operator|.
name|hasRegionsToRestore
argument_list|()
condition|)
name|hris
operator|.
name|addAll
argument_list|(
name|metaChanges
operator|.
name|getRegionsToRestore
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hrisToRemove
init|=
name|metaChanges
operator|.
name|getRegionsToRemove
argument_list|()
decl_stmt|;
name|MetaEditor
operator|.
name|mutateRegions
argument_list|(
name|catalogTracker
argument_list|,
name|hrisToRemove
argument_list|,
name|hris
argument_list|)
expr_stmt|;
comment|// At this point the restore is complete. Next step is enabling the table.
name|LOG
operator|.
name|info
argument_list|(
literal|"Restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" on table="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" completed!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" failed. Try re-running the restore command."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
name|masterServices
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RestoreSnapshotException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|completed
parameter_list|(
specifier|final
name|Throwable
name|exception
parameter_list|)
block|{
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
block|{
name|status
operator|.
name|abort
argument_list|(
literal|"Restore snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"' failed because "
operator|+
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|status
operator|.
name|markComplete
argument_list|(
literal|"Restore snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"'!"
argument_list|)
expr_stmt|;
block|}
name|metricsMaster
operator|.
name|addSnapshotRestore
argument_list|(
name|status
operator|.
name|getCompletionTimestamp
argument_list|()
operator|-
name|status
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|completed
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFinished
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompletionTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|status
operator|.
name|getCompletionTimestamp
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SnapshotDescription
name|getSnapshot
parameter_list|()
block|{
return|return
name|snapshot
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|(
name|String
name|why
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|stopped
condition|)
return|return;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|String
name|msg
init|=
literal|"Stopping restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" because: "
operator|+
name|why
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|CancellationException
name|ce
init|=
operator|new
name|CancellationException
argument_list|(
name|why
argument_list|)
decl_stmt|;
name|this
operator|.
name|monitor
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
name|masterServices
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|ce
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ForeignException
name|getExceptionIfFailed
parameter_list|()
block|{
return|return
name|this
operator|.
name|monitor
operator|.
name|getException
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rethrowExceptionIfFailed
parameter_list|()
throws|throws
name|ForeignException
block|{
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

