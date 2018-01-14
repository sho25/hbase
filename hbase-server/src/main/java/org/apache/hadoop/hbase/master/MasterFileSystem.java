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
name|fs
operator|.
name|permission
operator|.
name|FsAction
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
name|ClusterId
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
name|backup
operator|.
name|HFileArchiver
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
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|RegionInfoBuilder
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|DeserializationException
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
name|fs
operator|.
name|HFileSystem
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
name|log
operator|.
name|HBaseMarkers
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
name|mob
operator|.
name|MobConstants
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
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|FSTableDescriptors
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
name|ipc
operator|.
name|RemoteException
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

begin_comment
comment|/**  * This class abstracts a bunch of operations the HMaster needs to interact with  * the underlying file system like creating the initial layout, checking file  * system status, etc.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MasterFileSystem
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
name|MasterFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Parameter name for HBase instance root directory permission*/
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_DIR_PERMS
init|=
literal|"hbase.rootdir.perms"
decl_stmt|;
comment|/** Parameter name for HBase WAL directory permission*/
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_WAL_DIR_PERMS
init|=
literal|"hbase.wal.dir.perms"
decl_stmt|;
comment|// HBase configuration
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// Persisted unique cluster ID
specifier|private
name|ClusterId
name|clusterId
decl_stmt|;
comment|// Keep around for convenience.
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|// Keep around for convenience.
specifier|private
specifier|final
name|FileSystem
name|walFs
decl_stmt|;
comment|// root log directory on the FS
specifier|private
specifier|final
name|Path
name|rootdir
decl_stmt|;
comment|// hbase temp directory used for table construction and deletion
specifier|private
specifier|final
name|Path
name|tempdir
decl_stmt|;
comment|// root hbase directory on the FS
specifier|private
specifier|final
name|Path
name|walRootDir
decl_stmt|;
comment|/*    * In a secure env, the protected sub-directories and files under the HBase rootDir    * would be restricted. The sub-directory will have '700' except the bulk load staging dir,    * which will have '711'.  The default '700' can be overwritten by setting the property    * 'hbase.rootdir.perms'. The protected files (version file, clusterId file) will have '600'.    * The rootDir itself will be created with HDFS default permissions if it does not exist.    * We will check the rootDir permissions to make sure it has 'x' for all to ensure access    * to the staging dir. If it does not, we will add it.    */
comment|// Permissions for the directories under rootDir that need protection
specifier|private
specifier|final
name|FsPermission
name|secureRootSubDirPerms
decl_stmt|;
comment|// Permissions for the files under rootDir that need protection
specifier|private
specifier|final
name|FsPermission
name|secureRootFilePerms
init|=
operator|new
name|FsPermission
argument_list|(
literal|"600"
argument_list|)
decl_stmt|;
comment|// Permissions for bulk load staging directory under rootDir
specifier|private
specifier|final
name|FsPermission
name|HiddenDirPerms
init|=
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwx--x--x"
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|isSecurityEnabled
decl_stmt|;
specifier|public
name|MasterFileSystem
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// Set filesystem to be that of this.rootdir else we get complaints about
comment|// mismatched filesystems if hbase.rootdir is hdfs and fs.defaultFS is
comment|// default localfs.  Presumption is that rootdir is fully-qualified before
comment|// we get to here with appropriate fs scheme.
name|this
operator|.
name|rootdir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|tempdir
operator|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|HConstants
operator|.
name|HBASE_TEMP_DIRECTORY
argument_list|)
expr_stmt|;
comment|// Cover both bases, the old way of setting default fs and the new.
comment|// We're supposed to run on 0.20 and 0.21 anyways.
name|this
operator|.
name|fs
operator|=
name|this
operator|.
name|rootdir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|walRootDir
operator|=
name|FSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|walFs
operator|=
name|FSUtils
operator|.
name|getWALFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|this
operator|.
name|walFs
operator|.
name|getUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|walFs
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setFsDefault
argument_list|(
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|this
operator|.
name|fs
operator|.
name|getUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure the fs has the same conf
name|fs
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|secureRootSubDirPerms
operator|=
operator|new
name|FsPermission
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.rootdir.perms"
argument_list|,
literal|"700"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|isSecurityEnabled
operator|=
literal|"kerberos"
operator|.
name|equalsIgnoreCase
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.security.authentication"
argument_list|)
argument_list|)
expr_stmt|;
comment|// setup the filesystem variable
name|createInitialFileSystemLayout
argument_list|()
expr_stmt|;
name|HFileSystem
operator|.
name|addLocationsOrderInterceptor
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create initial layout in filesystem.    *<ol>    *<li>Check if the meta region exists and is readable, if not create it.    * Create hbase.version and the hbase:meta directory if not one.    *</li>    *</ol>    * Idempotent.    */
specifier|private
name|void
name|createInitialFileSystemLayout
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
index|[]
name|protectedSubDirs
init|=
operator|new
name|String
index|[]
block|{
name|HConstants
operator|.
name|BASE_NAMESPACE_DIR
block|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
block|,
name|HConstants
operator|.
name|HBCK_SIDELINEDIR_NAME
block|,
name|MobConstants
operator|.
name|MOB_DIR_NAME
block|}
decl_stmt|;
specifier|final
name|String
index|[]
name|protectedSubLogDirs
init|=
operator|new
name|String
index|[]
block|{
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
block|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
block|,
name|HConstants
operator|.
name|CORRUPT_DIR_NAME
block|,
name|WALProcedureStore
operator|.
name|MASTER_PROCEDURE_LOGDIR
block|}
decl_stmt|;
comment|// check if the root directory exists
name|checkRootDir
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
comment|// Check the directories under rootdir.
name|checkTempDir
argument_list|(
name|this
operator|.
name|tempdir
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|subDir
range|:
name|protectedSubDirs
control|)
block|{
name|checkSubDir
argument_list|(
operator|new
name|Path
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|subDir
argument_list|)
argument_list|,
name|HBASE_DIR_PERMS
argument_list|)
expr_stmt|;
block|}
specifier|final
name|String
name|perms
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|walRootDir
operator|.
name|equals
argument_list|(
name|this
operator|.
name|rootdir
argument_list|)
condition|)
block|{
name|perms
operator|=
name|HBASE_WAL_DIR_PERMS
expr_stmt|;
block|}
else|else
block|{
name|perms
operator|=
name|HBASE_DIR_PERMS
expr_stmt|;
block|}
for|for
control|(
name|String
name|subDir
range|:
name|protectedSubLogDirs
control|)
block|{
name|checkSubDir
argument_list|(
operator|new
name|Path
argument_list|(
name|this
operator|.
name|walRootDir
argument_list|,
name|subDir
argument_list|)
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
name|checkStagingDir
argument_list|()
expr_stmt|;
comment|// Handle the last few special files and set the final rootDir permissions
comment|// rootDir needs 'x' for all to support bulk load staging dir
if|if
condition|(
name|isSecurityEnabled
condition|)
block|{
name|fs
operator|.
name|setPermission
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|VERSION_FILE_NAME
argument_list|)
argument_list|,
name|secureRootFilePerms
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_FILE_NAME
argument_list|)
argument_list|,
name|secureRootFilePerms
argument_list|)
expr_stmt|;
block|}
name|FsPermission
name|currentRootPerms
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|this
operator|.
name|rootdir
argument_list|)
operator|.
name|getPermission
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|currentRootPerms
operator|.
name|getUserAction
argument_list|()
operator|.
name|implies
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
operator|||
operator|!
name|currentRootPerms
operator|.
name|getGroupAction
argument_list|()
operator|.
name|implies
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
operator|||
operator|!
name|currentRootPerms
operator|.
name|getOtherAction
argument_list|()
operator|.
name|implies
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"rootdir permissions do not contain 'excute' for user, group or other. "
operator|+
literal|"Automatically adding 'excute' permission for all"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
operator|new
name|FsPermission
argument_list|(
name|currentRootPerms
operator|.
name|getUserAction
argument_list|()
operator|.
name|or
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
argument_list|,
name|currentRootPerms
operator|.
name|getGroupAction
argument_list|()
operator|.
name|or
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
argument_list|,
name|currentRootPerms
operator|.
name|getOtherAction
argument_list|()
operator|.
name|or
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
name|this
operator|.
name|fs
return|;
block|}
specifier|protected
name|FileSystem
name|getWALFileSystem
parameter_list|()
block|{
return|return
name|this
operator|.
name|walFs
return|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
comment|/**    * @return HBase root dir.    */
specifier|public
name|Path
name|getRootDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|rootdir
return|;
block|}
comment|/**    * @return HBase root log dir.    */
specifier|public
name|Path
name|getWALRootDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|walRootDir
return|;
block|}
comment|/**    * @return HBase temp dir.    */
specifier|public
name|Path
name|getTempDir
parameter_list|()
block|{
return|return
name|this
operator|.
name|tempdir
return|;
block|}
comment|/**    * @return The unique identifier generated for this cluster    */
specifier|public
name|ClusterId
name|getClusterId
parameter_list|()
block|{
return|return
name|clusterId
return|;
block|}
comment|/**    * Get the rootdir.  Make sure its wholesome and exists before returning.    * @param rd    * @param c    * @param fs    * @return hbase.rootdir (after checks for existence and bootstrapping if    * needed populating the directory with necessary bootup files).    * @throws IOException    */
specifier|private
name|Path
name|checkRootDir
parameter_list|(
specifier|final
name|Path
name|rd
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If FS is in safe mode wait till out of it.
name|FSUtils
operator|.
name|waitOnSafeMode
argument_list|(
name|c
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
comment|// Filesystem is good. Go ahead and check for hbase.rootdir.
try|try
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|rd
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|rd
argument_list|)
expr_stmt|;
comment|// DFS leaves safe mode with 0 DNs when there are 0 blocks.
comment|// We used to handle this by checking the current DN count and waiting until
comment|// it is nonzero. With security, the check for datanode count doesn't work --
comment|// it is a privileged op. So instead we adopt the strategy of the jobtracker
comment|// and simply retry file creation during bootstrap indefinitely. As soon as
comment|// there is one datanode it will succeed. Permission problems should have
comment|// already been caught by mkdirs above.
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|VERSION_FILE_WRITE_ATTEMPTS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_VERSION_FILE_WRITE_ATTEMPTS
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|isDirectory
argument_list|(
name|rd
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|rd
operator|.
name|toString
argument_list|()
operator|+
literal|" is not a directory"
argument_list|)
throw|;
block|}
comment|// as above
name|FSUtils
operator|.
name|checkVersion
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
literal|true
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|VERSION_FILE_WRITE_ATTEMPTS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_VERSION_FILE_WRITE_ATTEMPTS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|de
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|HBaseMarkers
operator|.
name|FATAL
argument_list|,
literal|"Please fix invalid configuration for "
operator|+
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|de
argument_list|)
expr_stmt|;
name|IOException
name|ioe
init|=
operator|new
name|IOException
argument_list|()
decl_stmt|;
name|ioe
operator|.
name|initCause
argument_list|(
name|de
argument_list|)
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|HBaseMarkers
operator|.
name|FATAL
argument_list|,
literal|"Please fix invalid configuration for "
operator|+
name|HConstants
operator|.
name|HBASE_DIR
operator|+
literal|" "
operator|+
name|rd
operator|.
name|toString
argument_list|()
argument_list|,
name|iae
argument_list|)
expr_stmt|;
throw|throw
name|iae
throw|;
block|}
comment|// Make sure cluster ID exists
if|if
condition|(
operator|!
name|FSUtils
operator|.
name|checkClusterIdExists
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
argument_list|)
condition|)
block|{
name|FSUtils
operator|.
name|setClusterId
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|,
operator|new
name|ClusterId
argument_list|()
argument_list|,
name|c
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|clusterId
operator|=
name|FSUtils
operator|.
name|getClusterId
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|)
expr_stmt|;
comment|// Make sure the meta region directory exists!
if|if
condition|(
operator|!
name|FSUtils
operator|.
name|metaRegionExists
argument_list|(
name|fs
argument_list|,
name|rd
argument_list|)
condition|)
block|{
name|bootstrap
argument_list|(
name|rd
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
comment|// Create tableinfo-s for hbase:meta if not already there.
comment|// assume, created table descriptor is for enabling table
comment|// meta table is a system table, so descriptors are predefined,
comment|// we should get them from registry.
name|FSTableDescriptors
name|fsd
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|c
argument_list|,
name|fs
argument_list|,
name|rd
argument_list|)
decl_stmt|;
name|fsd
operator|.
name|createTableDescriptor
argument_list|(
name|fsd
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|rd
return|;
block|}
comment|/**    * Make sure the hbase temp directory exists and is empty.    * NOTE that this method is only executed once just after the master becomes the active one.    */
specifier|private
name|void
name|checkTempDir
parameter_list|(
specifier|final
name|Path
name|tmpdir
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If the temp directory exists, clear the content (left over, from the previous run)
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tmpdir
argument_list|)
condition|)
block|{
comment|// Archive table in temp, maybe left over from failed deletion,
comment|// if not the cleaner will take care of them.
for|for
control|(
name|Path
name|tabledir
range|:
name|FSUtils
operator|.
name|getTableDirs
argument_list|(
name|fs
argument_list|,
name|tmpdir
argument_list|)
control|)
block|{
for|for
control|(
name|Path
name|regiondir
range|:
name|FSUtils
operator|.
name|getRegionDirs
argument_list|(
name|fs
argument_list|,
name|tabledir
argument_list|)
control|)
block|{
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|rootdir
argument_list|,
name|tabledir
argument_list|,
name|regiondir
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|tmpdir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to clean the temp directory: "
operator|+
name|tmpdir
argument_list|)
throw|;
block|}
block|}
comment|// Create the temp directory
if|if
condition|(
name|isSecurityEnabled
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|tmpdir
argument_list|,
name|secureRootSubDirPerms
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase temp directory '"
operator|+
name|tmpdir
operator|+
literal|"' creation failure."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|tmpdir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase temp directory '"
operator|+
name|tmpdir
operator|+
literal|"' creation failure."
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Make sure the directories under rootDir have good permissions. Create if necessary.    * @param p    * @throws IOException    */
specifier|private
name|void
name|checkSubDir
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|String
name|dirPermsConfName
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FsPermission
name|dirPerms
init|=
operator|new
name|FsPermission
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|dirPermsConfName
argument_list|,
literal|"700"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
if|if
condition|(
name|isSecurityEnabled
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|p
argument_list|,
name|secureRootSubDirPerms
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase directory '"
operator|+
name|p
operator|+
literal|"' creation failure."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|p
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase directory '"
operator|+
name|p
operator|+
literal|"' creation failure."
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|isSecurityEnabled
operator|&&
operator|!
name|dirPerms
operator|.
name|equals
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|getPermission
argument_list|()
argument_list|)
condition|)
block|{
comment|// check whether the permission match
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found HBase directory permissions NOT matching expected permissions for "
operator|+
name|p
operator|.
name|toString
argument_list|()
operator|+
literal|" permissions="
operator|+
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|getPermission
argument_list|()
operator|+
literal|", expecting "
operator|+
name|dirPerms
operator|+
literal|". Automatically setting the permissions. "
operator|+
literal|"You can change the permissions by setting \""
operator|+
name|dirPermsConfName
operator|+
literal|"\" in hbase-site.xml "
operator|+
literal|"and restarting the master"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|p
argument_list|,
name|dirPerms
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Check permissions for bulk load staging directory. This directory has special hidden    * permissions. Create it if necessary.    * @throws IOException    */
specifier|private
name|void
name|checkStagingDir
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|rootdir
argument_list|,
name|HConstants
operator|.
name|BULKLOAD_STAGING_DIR_NAME
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|mkdirs
argument_list|(
name|p
argument_list|,
name|HiddenDirPerms
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create staging directory "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|fs
operator|.
name|setPermission
argument_list|(
name|p
argument_list|,
name|HiddenDirPerms
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
literal|"Failed to create or set permission on staging directory "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create or set permission on staging directory "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|bootstrap
parameter_list|(
specifier|final
name|Path
name|rd
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"BOOTSTRAP: creating hbase:meta region"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Bootstrapping, make sure blockcache is off.  Else, one will be
comment|// created here in bootstrap and it'll need to be cleaned up.  Better to
comment|// not make it in first place.  Turn off block caching for bootstrap.
comment|// Enable after.
name|TableDescriptor
name|metaDescriptor
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|c
argument_list|)
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|HRegion
name|meta
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|rd
argument_list|,
name|c
argument_list|,
name|setInfoFamilyCachingForMeta
argument_list|(
name|metaDescriptor
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|e
operator|instanceof
name|RemoteException
condition|?
operator|(
operator|(
name|RemoteException
operator|)
name|e
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
else|:
name|e
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"bootstrap"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Enable in memory caching for hbase:meta    */
specifier|public
specifier|static
name|TableDescriptor
name|setInfoFamilyCachingForMeta
parameter_list|(
name|TableDescriptor
name|metaDescriptor
parameter_list|,
specifier|final
name|boolean
name|b
parameter_list|)
block|{
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|metaDescriptor
argument_list|)
decl_stmt|;
for|for
control|(
name|ColumnFamilyDescriptor
name|hcd
range|:
name|metaDescriptor
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
condition|)
block|{
name|builder
operator|.
name|modifyColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|hcd
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
name|b
argument_list|)
operator|.
name|setInMemory
argument_list|(
name|b
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
name|void
name|deleteFamilyFromFS
parameter_list|(
name|RegionInfo
name|region
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteFamilyFromFS
argument_list|(
name|rootdir
argument_list|,
name|region
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deleteFamilyFromFS
parameter_list|(
name|Path
name|rootDir
parameter_list|,
name|RegionInfo
name|region
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// archive family store files
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|HFileArchiver
operator|.
name|archiveFamily
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|region
argument_list|,
name|tableDir
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
comment|// delete the family folder
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
operator|new
name|Path
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|delete
argument_list|(
name|familyDir
argument_list|,
literal|true
argument_list|)
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|familyDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not delete family "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
operator|+
literal|" from FileSystem for region "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|"("
operator|+
name|region
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|")"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{   }
specifier|public
name|void
name|logFileSystemState
parameter_list|(
name|Logger
name|log
parameter_list|)
throws|throws
name|IOException
block|{
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|log
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

