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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|security
operator|.
name|User
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
name|security
operator|.
name|access
operator|.
name|PermissionStorage
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
name|security
operator|.
name|access
operator|.
name|ShadedAccessControlUtil
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
name|security
operator|.
name|access
operator|.
name|UserPermission
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
name|collect
operator|.
name|ListMultimap
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
comment|/**  * Utility class to help manage {@link SnapshotDescription SnapshotDesriptions}.  *<p>  * Snapshots are laid out on disk like this:  *  *<pre>  * /hbase/.snapshots  *          /.tmp&lt;---- working directory  *          /[snapshot name]&lt;----- completed snapshot  *</pre>  *  * A completed snapshot named 'completed' then looks like (multiple regions, servers, files, etc.  * signified by '...' on the same directory depth).  *  *<pre>  * /hbase/.snapshots/completed  *                   .snapshotinfo&lt;--- Description of the snapshot  *                   .tableinfo&lt;--- Copy of the tableinfo  *                    /.logs  *                        /[server_name]  *                            /... [log files]  *                         ...  *                   /[region name]&lt;---- All the region's information  *                   .regioninfo&lt;---- Copy of the HRegionInfo  *                      /[column family name]  *                          /[hfile name]&lt;--- name of the hfile in the real region  *                          ...  *                      ...  *                    ...  *</pre>  *  * Utility methods in this class are useful for getting the correct locations for different parts of  * the snapshot, as well as moving completed snapshots into place (see  * {@link #completeSnapshot}, and writing the  * {@link SnapshotDescription} to the working snapshot directory.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
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
name|BlackListDirFilter
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
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|SNAPSHOT_TMP_DIR_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|SnapshotManifestV2
operator|.
name|DESCRIPTOR_VERSION
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
comment|/**    * The configuration property that determines the filepath of the snapshot    * base working directory    */
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_WORKING_DIR
init|=
literal|"hbase.snapshot.working.dir"
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
comment|/** By default, wait 300 seconds for a snapshot to complete */
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_WAIT_TIME
init|=
literal|60000
operator|*
literal|5
decl_stmt|;
comment|/**    * By default, check to see if the snapshot is complete (ms)    * @deprecated Use {@link #DEFAULT_MAX_WAIT_TIME} instead.    * */
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|int
name|SNAPSHOT_TIMEOUT_MILLIS_DEFAULT
init|=
literal|60000
operator|*
literal|5
decl_stmt|;
comment|/**    * Conf key for # of ms elapsed before injecting a snapshot timeout error when waiting for    * completion.    * @deprecated Use {@link #MASTER_SNAPSHOT_TIMEOUT_MILLIS} instead.    */
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
init|=
literal|"hbase.snapshot.master.timeoutMillis"
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
name|Math
operator|.
name|max
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
name|confKey
argument_list|,
name|defaultMaxWaitTime
argument_list|)
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
argument_list|,
name|defaultMaxWaitTime
argument_list|)
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
name|getSpecifiedSnapshotDir
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
comment|/**    * Get the general working directory for snapshots - where they are built, where they are    * temporarily copied on export, etc.    * @param rootDir root directory of the HBase installation    * @param conf Configuration of the HBase instance    * @return Path to the snapshot tmp directory, relative to the passed root directory    */
specifier|public
specifier|static
name|Path
name|getWorkingSnapshotDir
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SNAPSHOT_WORKING_DIR
argument_list|,
name|getDefaultWorkingSnapshotDir
argument_list|(
name|rootDir
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Get the directory to build a snapshot, before it is finalized    * @param snapshot snapshot that will be built    * @param rootDir root directory of the hbase installation    * @param conf Configuration of the HBase instance    * @return {@link Path} where one can build a snapshot    */
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
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getWorkingSnapshotDir
argument_list|(
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|,
name|rootDir
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|/**    * Get the directory to build a snapshot, before it is finalized    * @param snapshotName name of the snapshot    * @param rootDir root directory of the hbase installation    * @param conf Configuration of the HBase instance    * @return {@link Path} where one can build a snapshot    */
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
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getSpecifiedSnapshotDir
argument_list|(
name|getWorkingSnapshotDir
argument_list|(
name|rootDir
argument_list|,
name|conf
argument_list|)
argument_list|,
name|snapshotName
argument_list|)
return|;
block|}
comment|/**    * Get the directory within the given filepath to store the snapshot instance    * @param snapshotsDir directory to store snapshot directory within    * @param snapshotName name of the snapshot to take    * @return the final directory for the snapshot in the given filepath    */
specifier|private
specifier|static
specifier|final
name|Path
name|getSpecifiedSnapshotDir
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
comment|/**    * Determines if the given workingDir is a subdirectory of the given "root directory"    * @param workingDir a directory to check    * @param rootDir root directory of the HBase installation    * @return true if the given workingDir is a subdirectory of the given root directory,    *   false otherwise    */
specifier|public
specifier|static
name|boolean
name|isSubDirectoryOf
parameter_list|(
specifier|final
name|Path
name|workingDir
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|)
block|{
return|return
name|workingDir
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|rootDir
operator|.
name|toString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
argument_list|)
return|;
block|}
comment|/**    * Determines if the given workingDir is a subdirectory of the default working snapshot directory    * @param workingDir a directory to check    * @param conf configuration for the HBase cluster    * @return true if the given workingDir is a subdirectory of the default working directory for    *   snapshots, false otherwise    * @throws IOException if we can't get the root dir    */
specifier|public
specifier|static
name|boolean
name|isWithinDefaultWorkingDir
parameter_list|(
specifier|final
name|Path
name|workingDir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|defaultWorkingDir
init|=
name|getDefaultWorkingSnapshotDir
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|workingDir
operator|.
name|equals
argument_list|(
name|defaultWorkingDir
argument_list|)
operator|||
name|isSubDirectoryOf
argument_list|(
name|workingDir
argument_list|,
name|defaultWorkingDir
argument_list|)
return|;
block|}
comment|/**    * Get the default working directory for snapshots - where they are built, where they are    * temporarily copied on export, etc.    * @param rootDir root directory of the HBase installation    * @return Path to the default snapshot tmp directory, relative to the passed root directory    */
specifier|private
specifier|static
name|Path
name|getDefaultWorkingSnapshotDir
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
throws|,
name|IOException
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
name|currentTime
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
name|currentTime
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
comment|// set the acl to snapshot if security feature is enabled.
if|if
condition|(
name|isSecurityAvailable
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|snapshot
operator|=
name|writeAclToSnapshotDescription
argument_list|(
name|snapshot
argument_list|,
name|conf
argument_list|)
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
comment|/**    * Read in the {@link org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription} stored for the snapshot in the passed directory    * @param fs filesystem where the snapshot was taken    * @param snapshotDir directory where the snapshot was stored    * @return the stored snapshot description    * @throws CorruptedSnapshotException if the    * snapshot cannot be read    */
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
name|SnapshotDescription
name|desc
init|=
name|SnapshotDescription
operator|.
name|parseFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
return|return
name|desc
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
comment|/**    * Move the finished snapshot to its final, publicly visible directory - this marks the snapshot    * as 'complete'.    * @param snapshot description of the snapshot being tabken    * @param rootdir root directory of the hbase installation    * @param workingDir directory where the in progress snapshot was built    * @param fs {@link FileSystem} where the snapshot was built    * @throws org.apache.hadoop.hbase.snapshot.SnapshotCreationException if the    * snapshot could not be moved    * @throws IOException the filesystem could not be reached    */
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
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshot
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check if the user is this table snapshot's owner    * @param snapshot the table snapshot description    * @param user the user    * @return true if the user is the owner of the snapshot,    *         false otherwise or the snapshot owner field is not present.    */
specifier|public
specifier|static
name|boolean
name|isSnapshotOwner
parameter_list|(
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
name|SnapshotDescription
name|snapshot
parameter_list|,
name|User
name|user
parameter_list|)
block|{
if|if
condition|(
name|user
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|user
operator|.
name|getShortName
argument_list|()
operator|.
name|equals
argument_list|(
name|snapshot
operator|.
name|getOwner
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isSecurityAvailable
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|)
block|{
return|return
name|admin
operator|.
name|tableExists
argument_list|(
name|PermissionStorage
operator|.
name|ACL_TABLE_NAME
argument_list|)
return|;
block|}
block|}
block|}
specifier|private
specifier|static
name|SnapshotDescription
name|writeAclToSnapshotDescription
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|UserPermission
argument_list|>
name|perms
init|=
name|User
operator|.
name|runAsLoginUser
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|UserPermission
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|UserPermission
argument_list|>
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|PermissionStorage
operator|.
name|getTablePermissions
argument_list|(
name|conf
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
return|return
name|snapshot
operator|.
name|toBuilder
argument_list|()
operator|.
name|setUsersAndPermissions
argument_list|(
name|ShadedAccessControlUtil
operator|.
name|toUserTablePermissions
argument_list|(
name|perms
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

