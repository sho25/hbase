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
name|Set
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
name|classification
operator|.
name|InterfaceStability
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
operator|.
name|Type
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
name|StoreFile
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
name|server
operator|.
name|snapshot
operator|.
name|TakeSnapshotUtils
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
name|exception
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
name|hbase
operator|.
name|util
operator|.
name|HFileArchiveUtil
import|;
end_import

begin_comment
comment|/**  * General snapshot verification on the master.  *<p>  * This is a light-weight verification mechanism for all the files in a snapshot. It doesn't attempt  * to verify that the files are exact copies (that would be paramount to taking the snapshot  * again!), but instead just attempts to ensure that the files match the expected files and are the  * same length.  *<p>  * Current snapshot files checked:  *<ol>  *<li>SnapshotDescription is readable</li>  *<li>Table info is readable</li>  *<li>Regions</li>  *<ul>  *<li>Matching regions in the snapshot as currently in the table</li>  *<li>{@link HRegionInfo} matches the current and stored regions</li>  *<li>All referenced hfiles have valid names</li>  *<li>All the hfiles are present (either in .archive directory in the region)</li>  *<li>All recovered.edits files are present (by name) and have the correct file size</li>  *</ul>  *<li>HLogs for each server running the snapshot have been referenced  *<ul>  *<li>Only checked for {@link Type#GLOBAL} snapshots</li>  *</ul>  *</li>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
specifier|final
class|class
name|MasterSnapshotVerifier
block|{
specifier|private
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|rootDir
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|MasterServices
name|services
decl_stmt|;
comment|/**    * Build a util for the given snapshot    * @param services services for the master    * @param snapshot snapshot to check    * @param rootDir root directory of the hbase installation.    */
specifier|public
name|MasterSnapshotVerifier
parameter_list|(
name|MasterServices
name|services
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Path
name|rootDir
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
name|this
operator|.
name|rootDir
operator|=
name|rootDir
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|snapshot
operator|.
name|getTable
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that the snapshot in the directory is a valid snapshot    * @param snapshotDir snapshot directory to check    * @param snapshotServers {@link ServerName} of the servers that are involved in the snapshot    * @throws CorruptedSnapshotException if the snapshot is invalid    * @throws IOException if there is an unexpected connection issue to the filesystem    */
specifier|public
name|void
name|verifySnapshot
parameter_list|(
name|Path
name|snapshotDir
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotServers
parameter_list|)
throws|throws
name|CorruptedSnapshotException
throws|,
name|IOException
block|{
comment|// verify snapshot info matches
name|verifySnapshotDescription
argument_list|(
name|snapshotDir
argument_list|)
expr_stmt|;
comment|// check that tableinfo is a valid table description
name|verifyTableInfo
argument_list|(
name|snapshotDir
argument_list|)
expr_stmt|;
comment|// check that each region is valid
name|verifyRegions
argument_list|(
name|snapshotDir
argument_list|)
expr_stmt|;
comment|// check that the hlogs, if they exist, are valid
if|if
condition|(
name|shouldCheckLogs
argument_list|(
name|snapshot
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
name|verifyLogs
argument_list|(
name|snapshotDir
argument_list|,
name|snapshotServers
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Check to see if the snapshot should verify the logs directory based on the type of the logs.    * @param type type of snapshot being taken    * @return<tt>true</tt> if the logs directory should be verified,<tt>false</tt> otherwise    */
specifier|private
name|boolean
name|shouldCheckLogs
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
comment|// This is better handled in the Type enum via type, but since its PB based, this is the
comment|// simplest way to handle it
return|return
name|type
operator|.
name|equals
argument_list|(
name|Type
operator|.
name|GLOBAL
argument_list|)
return|;
block|}
comment|/**    * Check that the snapshot description written in the filesystem matches the current snapshot    * @param snapshotDir snapshot directory to check    */
specifier|private
name|void
name|verifySnapshotDescription
parameter_list|(
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|CorruptedSnapshotException
block|{
name|SnapshotDescription
name|found
init|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|snapshot
operator|.
name|equals
argument_list|(
name|found
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"Snapshot read ("
operator|+
name|found
operator|+
literal|") doesn't equal snapshot we ran ("
operator|+
name|snapshot
operator|+
literal|")."
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check that the table descriptor for the snapshot is a valid table descriptor    * @param snapshotDir snapshot directory to check    */
specifier|private
name|void
name|verifyTableInfo
parameter_list|(
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
block|{
name|FSTableDescriptors
operator|.
name|getTableDescriptor
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that all the regions in the the snapshot are valid    * @param snapshotDir snapshot directory to check    * @throws IOException if we can't reach .META. or read the files from the FS    */
specifier|private
name|void
name|verifyRegions
parameter_list|(
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|this
operator|.
name|services
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
comment|// if offline split parent, skip it
if|if
condition|(
name|region
operator|.
name|isOffline
argument_list|()
operator|||
name|region
operator|.
name|isSplit
argument_list|()
operator|||
name|region
operator|.
name|isSplitParent
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|verifyRegion
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verify that the region (regioninfo, hfiles) are valid    * @param snapshotDir snapshot directory to check    * @param region the region to check    */
specifier|private
name|void
name|verifyRegion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|snapshotDir
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
comment|// make sure we have region in the snapshot
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"No region directory found for region:"
operator|+
name|region
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
comment|// make sure we have the region info in the snapshot
name|Path
name|regionInfo
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|HRegion
operator|.
name|REGIONINFO_FILE
argument_list|)
decl_stmt|;
comment|// make sure the file exists
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|regionInfo
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"No region info found for region:"
operator|+
name|region
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
name|FSDataInputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|regionInfo
argument_list|)
decl_stmt|;
name|HRegionInfo
name|found
init|=
name|HRegionInfo
operator|.
name|parseFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|equals
argument_list|(
name|found
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"Found region info ("
operator|+
name|found
operator|+
literal|") doesn't match expected region:"
operator|+
name|region
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
comment|// make sure we have the expected recovered edits files
name|TakeSnapshotUtils
operator|.
name|verifyRecoveredEdits
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|found
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
comment|// check for the existance of each hfile
name|PathFilter
name|familiesDirs
init|=
operator|new
name|FSUtils
operator|.
name|FamilyDirFilter
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|columnFamilies
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|,
name|familiesDirs
argument_list|)
decl_stmt|;
comment|// should we do some checking here to make sure the cfs are correct?
if|if
condition|(
name|columnFamilies
operator|==
literal|null
condition|)
return|return;
comment|// setup the suffixes for the snapshot directories
name|Path
name|tableNameSuffix
init|=
operator|new
name|Path
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Path
name|regionNameSuffix
init|=
operator|new
name|Path
argument_list|(
name|tableNameSuffix
argument_list|,
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
comment|// get the potential real paths
name|Path
name|archivedRegion
init|=
operator|new
name|Path
argument_list|(
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|services
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|regionNameSuffix
argument_list|)
decl_stmt|;
name|Path
name|realRegion
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|regionNameSuffix
argument_list|)
decl_stmt|;
comment|// loop through each cf and check we can find each of the hfiles
for|for
control|(
name|FileStatus
name|cf
range|:
name|columnFamilies
control|)
block|{
name|FileStatus
index|[]
name|hfiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|cf
operator|.
name|getPath
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// should we check if there should be hfiles?
if|if
condition|(
name|hfiles
operator|==
literal|null
operator|||
name|hfiles
operator|.
name|length
operator|==
literal|0
condition|)
continue|continue;
name|Path
name|realCfDir
init|=
operator|new
name|Path
argument_list|(
name|realRegion
argument_list|,
name|cf
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|archivedCfDir
init|=
operator|new
name|Path
argument_list|(
name|archivedRegion
argument_list|,
name|cf
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|hfile
range|:
name|hfiles
control|)
block|{
comment|// make sure the name is correct
if|if
condition|(
operator|!
name|StoreFile
operator|.
name|validateStoreFileName
argument_list|(
name|hfile
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"HFile: "
operator|+
name|hfile
operator|.
name|getPath
argument_list|()
operator|+
literal|" is not a valid hfile name."
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
comment|// check to see if hfile is present in the real table
name|String
name|fileName
init|=
name|hfile
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|realCfDir
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|Path
name|archived
init|=
operator|new
name|Path
argument_list|(
name|archivedCfDir
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|equals
argument_list|(
name|archived
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"Can't find hfile: "
operator|+
name|hfile
operator|.
name|getPath
argument_list|()
operator|+
literal|" in the real ("
operator|+
name|archivedCfDir
operator|+
literal|") or archive ("
operator|+
name|archivedCfDir
operator|+
literal|") directory for the primary table."
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/**    * Check that the logs stored in the log directory for the snapshot are valid - it contains all    * the expected logs for all servers involved in the snapshot.    * @param snapshotDir snapshot directory to check    * @param snapshotServers list of the names of servers involved in the snapshot.    * @throws CorruptedSnapshotException if the hlogs in the snapshot are not correct    * @throws IOException if we can't reach the filesystem    */
specifier|private
name|void
name|verifyLogs
parameter_list|(
name|Path
name|snapshotDir
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotServers
parameter_list|)
throws|throws
name|CorruptedSnapshotException
throws|,
name|IOException
block|{
name|Path
name|snapshotLogDir
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|Path
name|logsDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|TakeSnapshotUtils
operator|.
name|verifyAllLogsGotReferenced
argument_list|(
name|fs
argument_list|,
name|logsDir
argument_list|,
name|snapshotServers
argument_list|,
name|snapshot
argument_list|,
name|snapshotLogDir
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

