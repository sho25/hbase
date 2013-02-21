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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|FileStatus
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
name|fs
operator|.
name|PathFilter
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
name|errorhandling
operator|.
name|ForeignExceptionListener
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
name|TimeoutExceptionInjector
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|HStore
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
name|regionserver
operator|.
name|wal
operator|.
name|HLogUtil
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
name|FSUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|HashMultimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

begin_comment
comment|/**  * Utilities for useful when taking a snapshot  */
end_comment

begin_class
specifier|public
class|class
name|TakeSnapshotUtils
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
name|TakeSnapshotUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TakeSnapshotUtils
parameter_list|()
block|{
comment|// private constructor for util class
block|}
comment|/**    * Get the per-region snapshot description location.    *<p>    * Under the per-snapshot directory, specific files per-region are kept in a similar layout as per    * the current directory layout.    * @param desc description of the snapshot    * @param rootDir root directory for the hbase installation    * @param regionName encoded name of the region (see {@link HRegionInfo#encodeRegionName(byte[])})    * @return path to the per-region directory for the snapshot    */
specifier|public
specifier|static
name|Path
name|getRegionSnapshotDirectory
parameter_list|(
name|SnapshotDescription
name|desc
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|String
name|regionName
parameter_list|)
block|{
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|desc
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
return|return
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|snapshotDir
argument_list|,
name|regionName
argument_list|)
return|;
block|}
comment|/**    * Get the home directory for store-level snapshot files.    *<p>    * Specific files per store are kept in a similar layout as per the current directory layout.    * @param regionDir snapshot directory for the parent region,<b>not</b> the standard region    *          directory. See {@code #getRegionSnapshotDirectory(SnapshotDescription, Path, String)}    * @param family name of the store to snapshot    * @return path to the snapshot home directory for the store/family    */
specifier|public
specifier|static
name|Path
name|getStoreSnapshotDirectory
parameter_list|(
name|Path
name|regionDir
parameter_list|,
name|String
name|family
parameter_list|)
block|{
return|return
name|HStore
operator|.
name|getStoreHomedir
argument_list|(
name|regionDir
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Get the snapshot directory for each family to be added to the the snapshot    * @param snapshot description of the snapshot being take    * @param snapshotRegionDir directory in the snapshot where the region directory information    *          should be stored    * @param families families to be added (can be null)    * @return paths to the snapshot directory for each family, in the same order as the families    *         passed in    */
specifier|public
specifier|static
name|List
argument_list|<
name|Path
argument_list|>
name|getFamilySnapshotDirectories
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Path
name|snapshotRegionDir
parameter_list|,
name|FileStatus
index|[]
name|families
parameter_list|)
block|{
if|if
condition|(
name|families
operator|==
literal|null
operator|||
name|families
operator|.
name|length
operator|==
literal|0
condition|)
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
name|List
argument_list|<
name|Path
argument_list|>
name|familyDirs
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|(
name|families
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|family
range|:
name|families
control|)
block|{
comment|// build the reference directory name
name|familyDirs
operator|.
name|add
argument_list|(
name|getStoreSnapshotDirectory
argument_list|(
name|snapshotRegionDir
argument_list|,
name|family
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|familyDirs
return|;
block|}
comment|/**    * Create a snapshot timer for the master which notifies the monitor when an error occurs    * @param snapshot snapshot to monitor    * @param conf configuration to use when getting the max snapshot life    * @param monitor monitor to notify when the snapshot life expires    * @return the timer to use update to signal the start and end of the snapshot    */
specifier|public
specifier|static
name|TimeoutExceptionInjector
name|getMasterTimerAndBindToMonitor
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ForeignExceptionListener
name|monitor
parameter_list|)
block|{
name|long
name|maxTime
init|=
name|SnapshotDescriptionUtils
operator|.
name|getMaxMasterTimeout
argument_list|(
name|conf
argument_list|,
name|snapshot
operator|.
name|getType
argument_list|()
argument_list|,
name|SnapshotDescriptionUtils
operator|.
name|DEFAULT_MAX_WAIT_TIME
argument_list|)
decl_stmt|;
return|return
operator|new
name|TimeoutExceptionInjector
argument_list|(
name|monitor
argument_list|,
name|maxTime
argument_list|)
return|;
block|}
comment|/**    * Verify that all the expected logs got referenced    * @param fs filesystem where the logs live    * @param logsDir original logs directory    * @param serverNames names of the servers that involved in the snapshot    * @param snapshot description of the snapshot being taken    * @param snapshotLogDir directory for logs in the snapshot    * @throws IOException    */
specifier|public
specifier|static
name|void
name|verifyAllLogsGotReferenced
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|logsDir
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|serverNames
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Path
name|snapshotLogDir
parameter_list|)
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
name|snapshot
argument_list|,
literal|"Logs directory doesn't exist in snapshot"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|logsDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// for each of the server log dirs, make sure it matches the main directory
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|snapshotLogs
init|=
name|getMapOfServersAndLogs
argument_list|(
name|fs
argument_list|,
name|snapshotLogDir
argument_list|,
name|serverNames
argument_list|)
decl_stmt|;
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|realLogs
init|=
name|getMapOfServersAndLogs
argument_list|(
name|fs
argument_list|,
name|logsDir
argument_list|,
name|serverNames
argument_list|)
decl_stmt|;
if|if
condition|(
name|realLogs
operator|!=
literal|null
condition|)
block|{
name|assertNotNull
argument_list|(
name|snapshot
argument_list|,
literal|"No server logs added to snapshot"
argument_list|,
name|snapshotLogs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|snapshot
argument_list|,
literal|"Snapshotted server logs that don't exist"
argument_list|,
name|snapshotLogs
argument_list|)
expr_stmt|;
block|}
comment|// check the number of servers
name|Set
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|serverEntries
init|=
name|realLogs
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|snapshotEntries
init|=
name|snapshotLogs
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|snapshot
argument_list|,
literal|"Not the same number of snapshot and original server logs directories"
argument_list|,
name|serverEntries
operator|.
name|size
argument_list|()
argument_list|,
name|snapshotEntries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify we snapshotted each of the log files
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|serverLogs
range|:
name|serverEntries
control|)
block|{
comment|// if the server is not the snapshot, skip checking its logs
if|if
condition|(
operator|!
name|serverNames
operator|.
name|contains
argument_list|(
name|serverLogs
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
continue|continue;
name|Collection
argument_list|<
name|String
argument_list|>
name|snapshotServerLogs
init|=
name|snapshotLogs
operator|.
name|get
argument_list|(
name|serverLogs
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|snapshot
argument_list|,
literal|"Snapshots missing logs for server:"
operator|+
name|serverLogs
operator|.
name|getKey
argument_list|()
argument_list|,
name|snapshotServerLogs
argument_list|)
expr_stmt|;
comment|// check each of the log files
name|assertEquals
argument_list|(
name|snapshot
argument_list|,
literal|"Didn't reference all the log files for server:"
operator|+
name|serverLogs
operator|.
name|getKey
argument_list|()
argument_list|,
name|serverLogs
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|snapshotServerLogs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|log
range|:
name|serverLogs
operator|.
name|getValue
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|snapshot
argument_list|,
literal|"Snapshot logs didn't include "
operator|+
name|log
argument_list|,
name|snapshotServerLogs
operator|.
name|contains
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Verify one of a snapshot's region's recovered.edits, has been at the surface (file names,    * length), match the original directory.    * @param fs filesystem on which the snapshot had been taken    * @param rootDir full path to the root hbase directory    * @param regionInfo info for the region    * @param snapshot description of the snapshot that was taken    * @throws IOException if there is an unexpected error talking to the filesystem    */
specifier|public
specifier|static
name|void
name|verifyRecoveredEdits
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|rootDir
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
name|Path
name|editsDir
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regionDir
argument_list|)
decl_stmt|;
name|Path
name|snapshotRegionDir
init|=
name|TakeSnapshotUtils
operator|.
name|getRegionSnapshotDirectory
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|,
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|snapshotEditsDir
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|snapshotRegionDir
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|edits
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|editsDir
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|snapshotEdits
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|snapshotEditsDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|edits
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|snapshot
argument_list|,
literal|"Snapshot has edits but table doesn't"
argument_list|,
name|snapshotEdits
argument_list|)
expr_stmt|;
return|return;
block|}
name|assertNotNull
argument_list|(
name|snapshot
argument_list|,
literal|"Table has edits, but snapshot doesn't"
argument_list|,
name|snapshotEdits
argument_list|)
expr_stmt|;
comment|// check each of the files
name|assertEquals
argument_list|(
name|snapshot
argument_list|,
literal|"Not same number of edits in snapshot as table"
argument_list|,
name|edits
operator|.
name|length
argument_list|,
name|snapshotEdits
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// make sure we have a file with the same name as the original
comment|// it would be really expensive to verify the content matches the original
for|for
control|(
name|FileStatus
name|edit
range|:
name|edits
control|)
block|{
for|for
control|(
name|FileStatus
name|sEdit
range|:
name|snapshotEdits
control|)
block|{
if|if
condition|(
name|sEdit
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|edit
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
name|snapshot
argument_list|,
literal|"Snapshot file"
operator|+
name|sEdit
operator|.
name|getPath
argument_list|()
operator|+
literal|" length not equal to the original: "
operator|+
name|edit
operator|.
name|getPath
argument_list|()
argument_list|,
name|edit
operator|.
name|getLen
argument_list|()
argument_list|,
name|sEdit
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|snapshot
argument_list|,
literal|"No edit in snapshot with name:"
operator|+
name|edit
operator|.
name|getPath
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|assertNull
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|String
name|msg
parameter_list|,
name|Object
name|isNull
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
if|if
condition|(
name|isNull
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
operator|+
literal|", Expected "
operator|+
name|isNull
operator|+
literal|" to be null."
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|assertNotNull
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|String
name|msg
parameter_list|,
name|Object
name|notNull
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
if|if
condition|(
name|notNull
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
operator|+
literal|", Expected object to not be null, but was null."
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|assertTrue
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|String
name|msg
parameter_list|,
name|boolean
name|isTrue
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
if|if
condition|(
operator|!
name|isTrue
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
operator|+
literal|", Expected true, but was false"
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
comment|/**    * Assert that the expect matches the gotten amount    * @param msg message to add the to exception    * @param expected    * @param gotten    * @throws CorruptedSnapshotException thrown if the two elements don't match    */
specifier|private
specifier|static
name|void
name|assertEquals
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|String
name|msg
parameter_list|,
name|int
name|expected
parameter_list|,
name|int
name|gotten
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
if|if
condition|(
name|expected
operator|!=
name|gotten
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
operator|+
literal|". Expected:"
operator|+
name|expected
operator|+
literal|", got:"
operator|+
name|gotten
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
comment|/**    * Assert that the expect matches the gotten amount    * @param msg message to add the to exception    * @param expected    * @param gotten    * @throws CorruptedSnapshotException thrown if the two elements don't match    */
specifier|private
specifier|static
name|void
name|assertEquals
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|String
name|msg
parameter_list|,
name|long
name|expected
parameter_list|,
name|long
name|gotten
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
if|if
condition|(
name|expected
operator|!=
name|gotten
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
operator|+
literal|". Expected:"
operator|+
name|expected
operator|+
literal|", got:"
operator|+
name|gotten
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
comment|/**    * @param logdir    * @param toInclude list of servers to include. If empty or null, returns all servers    * @return maps of servers to all their log files. If there is no log directory, returns    *<tt>null</tt>    */
specifier|private
specifier|static
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getMapOfServersAndLogs
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|logdir
parameter_list|,
name|Collection
argument_list|<
name|String
argument_list|>
name|toInclude
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create a path filter based on the passed directories to include
name|PathFilter
name|filter
init|=
name|toInclude
operator|==
literal|null
operator|||
name|toInclude
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|?
literal|null
else|:
operator|new
name|MatchesDirectoryNames
argument_list|(
name|toInclude
argument_list|)
decl_stmt|;
comment|// get all the expected directories
name|FileStatus
index|[]
name|serverLogDirs
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|logdir
argument_list|,
name|filter
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverLogDirs
operator|==
literal|null
condition|)
return|return
literal|null
return|;
comment|// map those into a multimap of servername -> [log files]
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
name|HashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|server
range|:
name|serverLogDirs
control|)
block|{
name|FileStatus
index|[]
name|serverLogs
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|server
operator|.
name|getPath
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverLogs
operator|==
literal|null
condition|)
continue|continue;
for|for
control|(
name|FileStatus
name|log
range|:
name|serverLogs
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|server
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|log
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|map
return|;
block|}
comment|/**    * Path filter that only accepts paths where that have a {@link Path#getName()} that is contained    * in the specified collection.    */
specifier|private
specifier|static
class|class
name|MatchesDirectoryNames
implements|implements
name|PathFilter
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|paths
decl_stmt|;
specifier|public
name|MatchesDirectoryNames
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|dirNames
parameter_list|)
block|{
name|this
operator|.
name|paths
operator|=
name|dirNames
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|paths
operator|.
name|contains
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * Get the log directory for a specific snapshot    * @param snapshotDir directory where the specific snapshot will be store    * @param serverName name of the parent regionserver for the log files    * @return path to the log home directory for the archive files.    */
specifier|public
specifier|static
name|Path
name|getSnapshotHLogsDir
parameter_list|(
name|Path
name|snapshotDir
parameter_list|,
name|String
name|serverName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|HLogUtil
operator|.
name|getHLogDirectoryName
argument_list|(
name|serverName
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

