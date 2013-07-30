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
name|FileNotFoundException
import|;
end_import

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
name|conf
operator|.
name|Configuration
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
name|ServerName
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
name|MetaReader
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
name|errorhandling
operator|.
name|ForeignExceptionSnare
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
name|EventHandler
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
name|TableLockManager
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
name|TableLockManager
operator|.
name|TableLock
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
name|SnapshotCreationException
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
name|snapshot
operator|.
name|TableInfoCopyTask
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
name|Pair
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

begin_comment
comment|/**  * A handler for taking snapshots from the master.  *  * This is not a subclass of TableEventHandler because using that would incur an extra META scan.  *  * The {@link #snapshotRegions(List)} call should get implemented for each snapshot flavor.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|TakeSnapshotHandler
extends|extends
name|EventHandler
implements|implements
name|SnapshotSentinel
implements|,
name|ForeignExceptionSnare
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
name|TakeSnapshotHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|finished
decl_stmt|;
comment|// none of these should ever be null
specifier|protected
specifier|final
name|MasterServices
name|master
decl_stmt|;
specifier|protected
specifier|final
name|MetricsMaster
name|metricsMaster
decl_stmt|;
specifier|protected
specifier|final
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
specifier|final
name|Path
name|rootDir
decl_stmt|;
specifier|private
specifier|final
name|Path
name|snapshotDir
decl_stmt|;
specifier|protected
specifier|final
name|Path
name|workingDir
decl_stmt|;
specifier|private
specifier|final
name|MasterSnapshotVerifier
name|verifier
decl_stmt|;
specifier|protected
specifier|final
name|ForeignExceptionDispatcher
name|monitor
decl_stmt|;
specifier|protected
specifier|final
name|TableLockManager
name|tableLockManager
decl_stmt|;
specifier|protected
specifier|final
name|TableLock
name|tableLock
decl_stmt|;
specifier|protected
specifier|final
name|MonitoredTask
name|status
decl_stmt|;
comment|/**    * @param snapshot descriptor of the snapshot to take    * @param masterServices master services provider    */
specifier|public
name|TakeSnapshotHandler
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|,
specifier|final
name|MetricsMaster
name|metricsMaster
parameter_list|)
block|{
name|super
argument_list|(
name|masterServices
argument_list|,
name|EventType
operator|.
name|C_M_SNAPSHOT_TABLE
argument_list|)
expr_stmt|;
assert|assert
name|snapshot
operator|!=
literal|null
operator|:
literal|"SnapshotDescription must not be nul1"
assert|;
assert|assert
name|masterServices
operator|!=
literal|null
operator|:
literal|"MasterServices must not be nul1"
assert|;
name|this
operator|.
name|master
operator|=
name|masterServices
expr_stmt|;
name|this
operator|.
name|metricsMaster
operator|=
name|metricsMaster
expr_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|this
operator|.
name|master
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|this
operator|.
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|this
operator|.
name|rootDir
operator|=
name|this
operator|.
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
expr_stmt|;
name|this
operator|.
name|snapshotDir
operator|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|workingDir
operator|=
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|monitor
operator|=
operator|new
name|ForeignExceptionDispatcher
argument_list|(
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableLockManager
operator|=
name|master
operator|.
name|getTableLockManager
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableLock
operator|=
name|this
operator|.
name|tableLockManager
operator|.
name|writeLock
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|EventType
operator|.
name|C_M_SNAPSHOT_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// prepare the verify
name|this
operator|.
name|verifier
operator|=
operator|new
name|MasterSnapshotVerifier
argument_list|(
name|masterServices
argument_list|,
name|snapshot
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
comment|// update the running tasks
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
literal|"Taking "
operator|+
name|snapshot
operator|.
name|getType
argument_list|()
operator|+
literal|" snapshot on table: "
operator|+
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HTableDescriptor
name|loadTableDescriptor
parameter_list|()
throws|throws
name|FileNotFoundException
throws|,
name|IOException
block|{
specifier|final
name|String
name|name
init|=
name|snapshot
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|this
operator|.
name|master
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|htd
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HTableDescriptor missing for "
operator|+
name|name
argument_list|)
throw|;
block|}
return|return
name|htd
return|;
block|}
specifier|public
name|TakeSnapshotHandler
name|prepare
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|prepare
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
comment|// after this, you should ensure to release this lock in
comment|// case of exceptions
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|loadTableDescriptor
argument_list|()
expr_stmt|;
comment|// check that .tableinfo is present
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|releaseTableLock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|this
return|;
block|}
comment|/**    * Execute the core common portions of taking a snapshot. The {@link #snapshotRegions(List)}    * call should get implemented for each snapshot flavor.    */
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
name|String
name|msg
init|=
literal|"Running "
operator|+
name|snapshot
operator|.
name|getType
argument_list|()
operator|+
literal|" table snapshot "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|eventType
operator|+
literal|" on table "
operator|+
name|snapshot
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
try|try
block|{
comment|// If regions move after this meta scan, the region specific snapshot should fail, triggering
comment|// an external exception that gets captured here.
comment|// write down the snapshot info in the working directory
name|SnapshotDescriptionUtils
operator|.
name|writeSnapshotInfo
argument_list|(
name|snapshot
argument_list|,
name|workingDir
argument_list|,
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
operator|new
name|TableInfoCopyTask
argument_list|(
name|monitor
argument_list|,
name|snapshot
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|)
operator|.
name|call
argument_list|()
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|regionsAndLocations
init|=
name|MetaReader
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// run the snapshot
name|snapshotRegions
argument_list|(
name|regionsAndLocations
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// extract each pair to separate lists
name|Set
argument_list|<
name|String
argument_list|>
name|serverNames
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|p
range|:
name|regionsAndLocations
control|)
block|{
name|serverNames
operator|.
name|add
argument_list|(
name|p
operator|.
name|getSecond
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// verify the snapshot is valid
name|status
operator|.
name|setStatus
argument_list|(
literal|"Verifying snapshot: "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|verifier
operator|.
name|verifySnapshot
argument_list|(
name|this
operator|.
name|workingDir
argument_list|,
name|serverNames
argument_list|)
expr_stmt|;
comment|// complete the snapshot, atomically moving from tmp to .snapshot dir.
name|completeSnapshot
argument_list|(
name|this
operator|.
name|snapshotDir
argument_list|,
name|this
operator|.
name|workingDir
argument_list|,
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
name|status
operator|.
name|markComplete
argument_list|(
literal|"Snapshot "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" of table "
operator|+
name|snapshot
operator|.
name|getTable
argument_list|()
operator|+
literal|" completed"
argument_list|)
expr_stmt|;
name|metricsMaster
operator|.
name|addSnapshot
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|status
operator|.
name|abort
argument_list|(
literal|"Failed to complete snapshot "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" on table "
operator|+
name|snapshot
operator|.
name|getTable
argument_list|()
operator|+
literal|" because "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|reason
init|=
literal|"Failed taking snapshot "
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" due to exception:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|reason
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ForeignException
name|ee
init|=
operator|new
name|ForeignException
argument_list|(
name|reason
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|receive
argument_list|(
name|ee
argument_list|)
expr_stmt|;
comment|// need to mark this completed to close off and allow cleanup to happen.
name|cancel
argument_list|(
literal|"Failed to take snapshot '"
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|"' due to exception"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Launching cleanup of working dir:"
operator|+
name|workingDir
argument_list|)
expr_stmt|;
try|try
block|{
comment|// if the working dir is still present, the snapshot has failed.  it is present we delete
comment|// it.
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|workingDir
argument_list|)
operator|&&
operator|!
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|workingDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't delete snapshot working directory:"
operator|+
name|workingDir
argument_list|)
expr_stmt|;
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
name|error
argument_list|(
literal|"Couldn't delete snapshot working directory:"
operator|+
name|workingDir
argument_list|)
expr_stmt|;
block|}
name|releaseTableLock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|releaseTableLock
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|tableLock
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|tableLock
operator|.
name|release
argument_list|()
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
literal|"Could not release the table lock"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Reset the manager to allow another snapshot to proceed    *    * @param snapshotDir final path of the snapshot    * @param workingDir directory where the in progress snapshot was built    * @param fs {@link FileSystem} where the snapshot was built    * @throws SnapshotCreationException if the snapshot could not be moved    * @throws IOException the filesystem could not be reached    */
specifier|public
name|void
name|completeSnapshot
parameter_list|(
name|Path
name|snapshotDir
parameter_list|,
name|Path
name|workingDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|SnapshotCreationException
throws|,
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Sentinel is done, just moving the snapshot from "
operator|+
name|workingDir
operator|+
literal|" to "
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|workingDir
argument_list|,
name|snapshotDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SnapshotCreationException
argument_list|(
literal|"Failed to move working directory("
operator|+
name|workingDir
operator|+
literal|") to completed directory("
operator|+
name|snapshotDir
operator|+
literal|")."
argument_list|)
throw|;
block|}
name|finished
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Snapshot the specified regions    */
specifier|protected
specifier|abstract
name|void
name|snapshotRegions
parameter_list|(
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
function_decl|;
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
name|finished
condition|)
return|return;
name|this
operator|.
name|finished
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stop taking snapshot="
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
name|monitor
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
name|master
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
name|boolean
name|isFinished
parameter_list|()
block|{
return|return
name|finished
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
name|ForeignException
name|getExceptionIfFailed
parameter_list|()
block|{
return|return
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
annotation|@
name|Override
specifier|public
name|void
name|rethrowException
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
annotation|@
name|Override
specifier|public
name|boolean
name|hasException
parameter_list|()
block|{
return|return
name|monitor
operator|.
name|hasException
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ForeignException
name|getException
parameter_list|()
block|{
return|return
name|monitor
operator|.
name|getException
argument_list|()
return|;
block|}
block|}
end_class

end_unit

