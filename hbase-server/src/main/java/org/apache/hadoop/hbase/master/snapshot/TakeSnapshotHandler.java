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
name|net
operator|.
name|URI
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
name|FileUtil
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
name|client
operator|.
name|RegionInfo
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
name|MetricsSnapshot
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
name|locking
operator|.
name|LockManager
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
name|locking
operator|.
name|LockManager
operator|.
name|MasterLock
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
name|procedure2
operator|.
name|LockType
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
name|SnapshotManifest
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
name|Pair
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
name|zookeeper
operator|.
name|MetaTableLocator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|SnapshotProtos
operator|.
name|SnapshotDescription
import|;
end_import

begin_comment
comment|/**  * A handler for taking snapshots from the master.  *  * This is not a subclass of TableEventHandler because using that would incur an extra hbase:meta scan.  *  * The {@link #snapshotRegions(List)} call should get implemented for each snapshot flavor.  */
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
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
name|MetricsSnapshot
name|metricsSnapshot
init|=
operator|new
name|MetricsSnapshot
argument_list|()
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
name|rootFs
decl_stmt|;
specifier|protected
specifier|final
name|FileSystem
name|workingDirFs
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
specifier|private
specifier|final
name|LockManager
operator|.
name|MasterLock
name|tableLock
decl_stmt|;
specifier|protected
specifier|final
name|MonitoredTask
name|status
decl_stmt|;
specifier|protected
specifier|final
name|TableName
name|snapshotTable
decl_stmt|;
specifier|protected
specifier|final
name|SnapshotManifest
name|snapshotManifest
decl_stmt|;
specifier|protected
specifier|final
name|SnapshotManager
name|snapshotManager
decl_stmt|;
specifier|protected
name|TableDescriptor
name|htd
decl_stmt|;
comment|/**    * @param snapshot descriptor of the snapshot to take    * @param masterServices master services provider    * @throws IllegalArgumentException if the working snapshot directory set from the    *   configuration is the same as the completed snapshot directory    * @throws IOException if the file system of the working snapshot directory cannot be    *   determined    */
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
name|SnapshotManager
name|snapshotManager
parameter_list|)
throws|throws
name|IOException
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
name|workingDir
operator|=
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|SnapshotDescriptionUtils
operator|.
name|isSubDirectoryOf
argument_list|(
name|workingDir
argument_list|,
name|rootDir
argument_list|)
operator|||
name|SnapshotDescriptionUtils
operator|.
name|isWithinDefaultWorkingDir
argument_list|(
name|workingDir
argument_list|,
name|conf
argument_list|)
argument_list|,
literal|"The working directory "
operator|+
name|workingDir
operator|+
literal|" cannot be in the root directory unless it is "
operator|+
literal|"within the default working directory"
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
name|this
operator|.
name|snapshotManager
operator|=
name|snapshotManager
expr_stmt|;
name|this
operator|.
name|snapshotTable
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootFs
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
name|workingDirFs
operator|=
name|this
operator|.
name|workingDir
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|conf
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
name|snapshotManifest
operator|=
name|SnapshotManifest
operator|.
name|create
argument_list|(
name|conf
argument_list|,
name|rootFs
argument_list|,
name|workingDir
argument_list|,
name|snapshot
argument_list|,
name|monitor
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableLock
operator|=
name|master
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|snapshotTable
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": take snapshot "
operator|+
name|snapshot
operator|.
name|getName
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
name|workingDirFs
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
name|snapshotTable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TableDescriptor
name|loadTableDescriptor
parameter_list|()
throws|throws
name|FileNotFoundException
throws|,
name|IOException
block|{
name|TableDescriptor
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
name|snapshotTable
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
literal|"TableDescriptor missing for "
operator|+
name|snapshotTable
argument_list|)
throw|;
block|}
return|return
name|htd
return|;
block|}
annotation|@
name|Override
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
comment|// after this, you should ensure to release this lock in case of exceptions
name|this
operator|.
name|tableLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|htd
operator|=
name|loadTableDescriptor
argument_list|()
expr_stmt|;
comment|// check that .tableinfo is present
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|this
operator|.
name|tableLock
operator|.
name|release
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|this
return|;
block|}
comment|/**    * Execute the core common portions of taking a snapshot. The {@link #snapshotRegions(List)}    * call should get implemented for each snapshot flavor.    */
annotation|@
name|Override
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"REC_CATCH_EXCEPTION"
argument_list|,
name|justification
operator|=
literal|"Intentional"
argument_list|)
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
name|snapshotTable
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|MasterLock
name|tableLockToRelease
init|=
name|this
operator|.
name|tableLock
decl_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|downgradeToSharedTableLock
argument_list|()
condition|)
block|{
comment|// release the exclusive lock and hold the shared lock instead
name|tableLockToRelease
operator|=
name|master
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|snapshotTable
argument_list|,
name|LockType
operator|.
name|SHARED
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": take snapshot "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tableLock
operator|.
name|release
argument_list|()
expr_stmt|;
name|tableLockToRelease
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
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
name|workingDirFs
argument_list|)
expr_stmt|;
name|snapshotManifest
operator|.
name|addTableDescriptor
argument_list|(
name|this
operator|.
name|htd
argument_list|)
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
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|regionsAndLocations
decl_stmt|;
if|if
condition|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|equals
argument_list|(
name|snapshotTable
argument_list|)
condition|)
block|{
name|regionsAndLocations
operator|=
name|MetaTableLocator
operator|.
name|getMetaRegionsAndLocations
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionsAndLocations
operator|=
name|MetaTableAccessor
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|snapshotTable
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
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
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|p
range|:
name|regionsAndLocations
control|)
block|{
if|if
condition|(
name|p
operator|!=
literal|null
operator|&&
name|p
operator|.
name|getFirst
argument_list|()
operator|!=
literal|null
operator|&&
name|p
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|RegionInfo
name|hri
init|=
name|p
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|hri
operator|.
name|isOffline
argument_list|()
operator|&&
operator|(
name|hri
operator|.
name|isSplit
argument_list|()
operator|||
name|hri
operator|.
name|isSplitParent
argument_list|()
operator|)
condition|)
continue|continue;
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
block|}
comment|// flush the in-memory state, and write the single manifest
name|status
operator|.
name|setStatus
argument_list|(
literal|"Consolidate snapshot: "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|snapshotManifest
operator|.
name|consolidate
argument_list|()
expr_stmt|;
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
name|rootFs
argument_list|,
name|this
operator|.
name|workingDirFs
argument_list|)
expr_stmt|;
name|msg
operator|=
literal|"Snapshot "
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" of table "
operator|+
name|snapshotTable
operator|+
literal|" completed"
expr_stmt|;
name|status
operator|.
name|markComplete
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|metricsSnapshot
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
if|if
condition|(
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postCompletedSnapshotAction
argument_list|(
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshot
argument_list|)
argument_list|,
name|this
operator|.
name|htd
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// FindBugs: REC_CATCH_EXCEPTION
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
name|snapshotTable
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
name|reason
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
operator|!
name|workingDirFs
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
name|tableLockToRelease
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Reset the manager to allow another snapshot to proceed.    * Commits the snapshot process by moving the working snapshot    * to the finalized filepath    *    * @param snapshotDir The file path of the completed snapshots    * @param workingDir  The file path of the in progress snapshots    * @param fs The file system of the completed snapshots    * @param workingDirFs The file system of the in progress snapshots    *    * @throws SnapshotCreationException if the snapshot could not be moved    * @throws IOException the filesystem could not be reached    */
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
parameter_list|,
name|FileSystem
name|workingDirFs
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
comment|// If the working and completed snapshot directory are on the same file system, attempt
comment|// to rename the working snapshot directory to the completed location. If that fails,
comment|// or the file systems differ, attempt to copy the directory over, throwing an exception
comment|// if this fails
name|URI
name|workingURI
init|=
name|workingDirFs
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|URI
name|rootURI
init|=
name|fs
operator|.
name|getUri
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
operator|!
name|workingURI
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
name|rootURI
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|||
name|workingURI
operator|.
name|getAuthority
argument_list|()
operator|==
literal|null
operator|||
operator|!
name|workingURI
operator|.
name|getAuthority
argument_list|()
operator|.
name|equals
argument_list|(
name|rootURI
operator|.
name|getAuthority
argument_list|()
argument_list|)
operator|||
name|workingURI
operator|.
name|getUserInfo
argument_list|()
operator|==
literal|null
operator|||
operator|!
name|workingURI
operator|.
name|getUserInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|rootURI
operator|.
name|getUserInfo
argument_list|()
argument_list|)
operator|||
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|workingDir
argument_list|,
name|snapshotDir
argument_list|)
operator|)
operator|&&
operator|!
name|FileUtil
operator|.
name|copy
argument_list|(
name|workingDirFs
argument_list|,
name|workingDir
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|this
operator|.
name|conf
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SnapshotCreationException
argument_list|(
literal|"Failed to copy working directory("
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
comment|/**    * When taking snapshot, first we must acquire the exclusive table lock to confirm that there are    * no ongoing merge/split procedures. But later, we should try our best to release the exclusive    * lock as this may hurt the availability, because we need to hold the shared lock when assigning    * regions.    *<p/>    * See HBASE-21480 for more details.    */
specifier|protected
specifier|abstract
name|boolean
name|downgradeToSharedTableLock
parameter_list|()
function_decl|;
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
name|RegionInfo
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
comment|/**    * Take a snapshot of the specified disabled region    */
specifier|protected
name|void
name|snapshotDisabledRegion
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|snapshotManifest
operator|.
name|addRegion
argument_list|(
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|snapshotTable
argument_list|)
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
literal|"Completed referencing HFiles for offline region "
operator|+
name|regionInfo
operator|.
name|toString
argument_list|()
operator|+
literal|" of table: "
operator|+
name|snapshotTable
argument_list|)
expr_stmt|;
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

