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
name|FSDataInputStream
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
name|FSDataOutputStream
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
name|permission
operator|.
name|FsPermission
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
name|exceptions
operator|.
name|CorruptedSnapshotException
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
name|util
operator|.
name|EnvironmentEdgeManager
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

begin_comment
comment|/**  * Utility class to help manage {@link SnapshotDescription SnapshotDesriptions}.  *<p>  * Snapshots are laid out on disk like this:  *  *<pre>  * /hbase/.snapshots  *          /.tmp<---- working directory  *          /[snapshot name]<----- completed snapshot  *</pre>  *  * A completed snapshot named 'completed' then looks like (multiple regions, servers, files, etc.  * signified by '...' on the same directory depth).  *  *<pre>  * /hbase/.snapshots/completed  *                   .snapshotinfo<--- Description of the snapshot  *                   .tableinfo<--- Copy of the tableinfo  *                    /.logs  *                        /[server_name]  *                            /... [log files]  *                         ...  *                   /[region name]<---- All the region's information  *                   .regioninfo<---- Copy of the HRegionInfo  *                      /[column family name]  *                          /[hfile name]<--- name of the hfile in the real region  *                          ...  *                      ...  *                    ...  *</pre>  *  * Utility methods in this class are useful for getting the correct locations for different parts of  * the snapshot, as well as moving completed snapshots into place (see  * {@link #completeSnapshot}, and writing the  * {@link SnapshotDescription} to the working snapshot directory.  */
end_comment

begin_class
specifier|public
class|class
name|SnapshotDescriptionUtils
block|{
comment|/**    * Filter that only accepts completed snapshot directories    */
specifier|public
specifier|static
class|class
name|CompletedSnaphotDirectoriesFilter
extends|extends
name|FSUtils
operator|.
name|DirFilter
block|{
comment|/**      * @param fs      */
specifier|public
name|CompletedSnaphotDirectoriesFilter
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
name|super
argument_list|(
name|fs
argument_list|)
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
comment|// only accept directories that aren't the tmp directory
if|if
condition|(
name|super
operator|.
name|accept
argument_list|(
name|path
argument_list|)
condition|)
block|{
return|return
operator|!
name|path
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|SNAPSHOT_TMP_DIR_NAME
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
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
name|SnapshotDescriptionUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Version of the fs layout for a snapshot. Future snapshots may have different file layouts,    * which we may need to read in differently.    */
specifier|public
specifier|static
specifier|final
name|int
name|SNAPSHOT_LAYOUT_VERSION
init|=
literal|0
decl_stmt|;
comment|// snapshot directory constants
comment|/**    * The file contains the snapshot basic information and it is under the directory of a snapshot.    */
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOTINFO_FILE
init|=
literal|".snapshotinfo"
decl_stmt|;
comment|/** Temporary directory under the snapshot directory to store in-progress snapshots */
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_TMP_DIR_NAME
init|=
literal|".tmp"
decl_stmt|;
comment|// snapshot operation values
comment|/** Default value if no start time is specified */
specifier|public
specifier|static
specifier|final
name|long
name|NO_SNAPSHOT_START_TIME_SPECIFIED
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_SNAPSHOT_TIMEOUT_MILLIS
init|=
literal|"hbase.snapshot.master.timeout.millis"
decl_stmt|;
comment|/** By default, wait 60 seconds for a snapshot to complete */
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_WAIT_TIME
init|=
literal|60000
decl_stmt|;
specifier|private
name|SnapshotDescriptionUtils
parameter_list|()
block|{
comment|// private constructor for utility class
block|}
comment|/**    * @param conf {@link Configuration} from which to check for the timeout    * @param type type of snapshot being taken    * @param defaultMaxWaitTime Default amount of time to wait, if none is in the configuration    * @return the max amount of time the master should wait for a snapshot to complete    */
specifier|public
specifier|static
name|long
name|getMaxMasterTimeout
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SnapshotDescription
operator|.
name|Type
name|type
parameter_list|,
name|long
name|defaultMaxWaitTime
parameter_list|)
block|{
name|String
name|confKey
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DISABLED
case|:
default|default:
name|confKey
operator|=
name|MASTER_SNAPSHOT_TIMEOUT_MILLIS
expr_stmt|;
block|}
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|confKey
argument_list|,
name|defaultMaxWaitTime
argument_list|)
return|;
block|}
comment|/**    * Get the snapshot root directory. All the snapshots are kept under this directory, i.e.    * ${hbase.rootdir}/.snapshot    * @param rootDir hbase root directory    * @return the base directory in which all snapshots are kept    */
specifier|public
specifier|static
name|Path
name|getSnapshotRootDir
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
return|;
block|}
comment|/**    * Get the directory for a specified snapshot. This directory is a sub-directory of snapshot root    * directory and all the data files for a snapshot are kept under this directory.    * @param snapshot snapshot being taken    * @param rootDir hbase root directory    * @return the final directory for the completed snapshot    */
specifier|public
specifier|static
name|Path
name|getCompletedSnapshotDir
parameter_list|(
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|,
name|rootDir
argument_list|)
return|;
block|}
comment|/**    * Get the directory for a completed snapshot. This directory is a sub-directory of snapshot root    * directory and all the data files for a snapshot are kept under this directory.    * @param snapshotName name of the snapshot being taken    * @param rootDir hbase root directory    * @return the final directory for the completed snapshot    */
specifier|public
specifier|static
name|Path
name|getCompletedSnapshotDir
parameter_list|(
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
name|getCompletedSnapshotDir
argument_list|(
name|getSnapshotsDir
argument_list|(
name|rootDir
argument_list|)
argument_list|,
name|snapshotName
argument_list|)
return|;
block|}
comment|/**    * Get the general working directory for snapshots - where they are built, where they are    * temporarily copied on export, etc.    * @param rootDir root directory of the HBase installation    * @return Path to the snapshot tmp directory, relative to the passed root directory    */
specifier|public
specifier|static
name|Path
name|getWorkingSnapshotDir
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|getSnapshotsDir
argument_list|(
name|rootDir
argument_list|)
argument_list|,
name|SNAPSHOT_TMP_DIR_NAME
argument_list|)
return|;
block|}
comment|/**    * Get the directory to build a snapshot, before it is finalized    * @param snapshot snapshot that will be built    * @param rootDir root directory of the hbase installation    * @return {@link Path} where one can build a snapshot    */
specifier|public
specifier|static
name|Path
name|getWorkingSnapshotDir
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
name|getCompletedSnapshotDir
argument_list|(
name|getWorkingSnapshotDir
argument_list|(
name|rootDir
argument_list|)
argument_list|,
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get the directory to build a snapshot, before it is finalized    * @param snapshotName name of the snapshot    * @param rootDir root directory of the hbase installation    * @return {@link Path} where one can build a snapshot    */
specifier|public
specifier|static
name|Path
name|getWorkingSnapshotDir
parameter_list|(
name|String
name|snapshotName
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
name|getCompletedSnapshotDir
argument_list|(
name|getWorkingSnapshotDir
argument_list|(
name|rootDir
argument_list|)
argument_list|,
name|snapshotName
argument_list|)
return|;
block|}
comment|/**    * Get the directory to store the snapshot instance    * @param snapshotsDir hbase-global directory for storing all snapshots    * @param snapshotName name of the snapshot to take    * @return    */
specifier|private
specifier|static
specifier|final
name|Path
name|getCompletedSnapshotDir
parameter_list|(
specifier|final
name|Path
name|snapshotsDir
parameter_list|,
name|String
name|snapshotName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|snapshotsDir
argument_list|,
name|snapshotName
argument_list|)
return|;
block|}
comment|/**    * @param rootDir hbase root directory    * @return the directory for all completed snapshots;    */
specifier|public
specifier|static
specifier|final
name|Path
name|getSnapshotsDir
parameter_list|(
name|Path
name|rootDir
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
return|;
block|}
comment|/**    * Convert the passed snapshot description into a 'full' snapshot description based on default    * parameters, if none have been supplied. This resolves any 'optional' parameters that aren't    * supplied to their default values.    * @param snapshot general snapshot descriptor    * @param conf Configuration to read configured snapshot defaults if snapshot is not complete    * @return a valid snapshot description    * @throws IllegalArgumentException if the {@link SnapshotDescription} is not a complete    *           {@link SnapshotDescription}.    */
specifier|public
specifier|static
name|SnapshotDescription
name|validate
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
operator|!
name|snapshot
operator|.
name|hasTable
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Descriptor doesn't apply to a table, so we can't build it."
argument_list|)
throw|;
block|}
comment|// set the creation time, if one hasn't been set
name|long
name|time
init|=
name|snapshot
operator|.
name|getCreationTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|time
operator|==
name|SnapshotDescriptionUtils
operator|.
name|NO_SNAPSHOT_START_TIME_SPECIFIED
condition|)
block|{
name|time
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creation time not specified, setting to:"
operator|+
name|time
operator|+
literal|" (current time:"
operator|+
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|")."
argument_list|)
expr_stmt|;
name|SnapshotDescription
operator|.
name|Builder
name|builder
init|=
name|snapshot
operator|.
name|toBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setCreationTime
argument_list|(
name|time
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
return|return
name|snapshot
return|;
block|}
comment|/**    * Write the snapshot description into the working directory of a snapshot    * @param snapshot description of the snapshot being taken    * @param workingDir working directory of the snapshot    * @param fs {@link FileSystem} on which the snapshot should be taken    * @throws IOException if we can't reach the filesystem and the file cannot be cleaned up on    *           failure    */
specifier|public
specifier|static
name|void
name|writeSnapshotInfo
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Path
name|workingDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|FsPermission
name|perms
init|=
name|FSUtils
operator|.
name|getFilePermissions
argument_list|(
name|fs
argument_list|,
name|fs
operator|.
name|getConf
argument_list|()
argument_list|,
name|HConstants
operator|.
name|DATA_FILE_UMASK_KEY
argument_list|)
decl_stmt|;
name|Path
name|snapshotInfo
init|=
operator|new
name|Path
argument_list|(
name|workingDir
argument_list|,
name|SnapshotDescriptionUtils
operator|.
name|SNAPSHOTINFO_FILE
argument_list|)
decl_stmt|;
try|try
block|{
name|FSDataOutputStream
name|out
init|=
name|FSUtils
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|snapshotInfo
argument_list|,
name|perms
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|snapshot
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// if we get an exception, try to remove the snapshot info
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|snapshotInfo
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"Couldn't delete snapshot info file: "
operator|+
name|snapshotInfo
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Read in the {@link SnapshotDescription} stored for the snapshot in the passed directory    * @param fs filesystem where the snapshot was taken    * @param snapshotDir directory where the snapshot was stored    * @return the stored snapshot description    * @throws org.apache.hadoop.hbase.exceptions.CorruptedSnapshotException if the snapshot cannot be read    */
specifier|public
specifier|static
name|SnapshotDescription
name|readSnapshotInfo
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
name|Path
name|snapshotInfo
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|SNAPSHOTINFO_FILE
argument_list|)
decl_stmt|;
try|try
block|{
name|FSDataInputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|in
operator|=
name|fs
operator|.
name|open
argument_list|(
name|snapshotInfo
argument_list|)
expr_stmt|;
return|return
name|SnapshotDescription
operator|.
name|parseFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"Couldn't read snapshot info from:"
operator|+
name|snapshotInfo
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Move the finished snapshot to its final, publicly visible directory - this marks the snapshot    * as 'complete'.    * @param snapshot description of the snapshot being tabken    * @param rootdir root directory of the hbase installation    * @param workingDir directory where the in progress snapshot was built    * @param fs {@link FileSystem} where the snapshot was built    * @throws org.apache.hadoop.hbase.exceptions.SnapshotCreationException if the snapshot could not be moved    * @throws IOException the filesystem could not be reached    */
specifier|public
specifier|static
name|void
name|completeSnapshot
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Path
name|rootdir
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
name|Path
name|finishedDir
init|=
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Snapshot is done, just moving the snapshot from "
operator|+
name|workingDir
operator|+
literal|" to "
operator|+
name|finishedDir
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
name|finishedDir
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
name|finishedDir
operator|+
literal|")."
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

