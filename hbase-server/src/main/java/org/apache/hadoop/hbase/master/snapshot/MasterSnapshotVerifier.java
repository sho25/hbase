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
name|Map
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
name|client
operator|.
name|RegionReplicaUtil
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
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
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
name|snapshot
operator|.
name|SnapshotReferenceUtil
import|;
end_import

begin_comment
comment|/**  * General snapshot verification on the master.  *<p>  * This is a light-weight verification mechanism for all the files in a snapshot. It doesn't  * attempt to verify that the files are exact copies (that would be paramount to taking the  * snapshot again!), but instead just attempts to ensure that the files match the expected  * files and are the same length.  *<p>  * Taking an online snapshots can race against other operations and this is an last line of  * defense.  For example, if meta changes between when snapshots are taken not all regions of a  * table may be present.  This can be caused by a region split (daughters present on this scan,  * but snapshot took parent), or move (snapshots only checks lists of region servers, a move could  * have caused a region to be skipped or done twice).  *<p>  * Current snapshot files checked:  *<ol>  *<li>SnapshotDescription is readable</li>  *<li>Table info is readable</li>  *<li>Regions</li>  *<ul>  *<li>Matching regions in the snapshot as currently in the table</li>  *<li>{@link HRegionInfo} matches the current and stored regions</li>  *<li>All referenced hfiles have valid names</li>  *<li>All the hfiles are present (either in .archive directory in the region)</li>  *<li>All recovered.edits files are present (by name) and have the correct file size</li>  *</ul>  *</ol>  */
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
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MasterSnapshotVerifier
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|MasterServices
name|services
decl_stmt|;
comment|/**    * @param services services for the master    * @param snapshot snapshot to check    * @param rootDir root directory of the hbase installation.    */
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
name|SnapshotManifest
name|manifest
init|=
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|services
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshot
argument_list|)
decl_stmt|;
comment|// verify snapshot info matches
name|verifySnapshotDescription
argument_list|(
name|snapshotDir
argument_list|)
expr_stmt|;
comment|// check that tableinfo is a valid table description
name|verifyTableInfo
argument_list|(
name|manifest
argument_list|)
expr_stmt|;
comment|// check that each region is valid
name|verifyRegions
argument_list|(
name|manifest
argument_list|)
expr_stmt|;
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
comment|/**    * Check that the table descriptor for the snapshot is a valid table descriptor    * @param manifest snapshot manifest to inspect    */
specifier|private
name|void
name|verifyTableInfo
parameter_list|(
specifier|final
name|SnapshotManifest
name|manifest
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
name|manifest
operator|.
name|getTableDescriptor
argument_list|()
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
name|CorruptedSnapshotException
argument_list|(
literal|"Missing Table Descriptor"
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|htd
operator|.
name|getNameAsString
argument_list|()
operator|.
name|equals
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"Invalid Table Descriptor. Expected "
operator|+
name|snapshot
operator|.
name|getTable
argument_list|()
operator|+
literal|" name, got "
operator|+
name|htd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check that all the regions in the snapshot are valid, and accounted for.    * @param manifest snapshot manifest to inspect    * @throws IOException if we can't reach hbase:meta or read the files from the FS    */
specifier|private
name|void
name|verifyRegions
parameter_list|(
specifier|final
name|SnapshotManifest
name|manifest
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
name|tableName
argument_list|)
decl_stmt|;
comment|// Remove the non-default regions
name|RegionReplicaUtil
operator|.
name|removeNonDefaultRegions
argument_list|(
name|regions
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SnapshotRegionManifest
argument_list|>
name|regionManifests
init|=
name|manifest
operator|.
name|getRegionManifestsMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionManifests
operator|==
literal|null
condition|)
block|{
name|String
name|msg
init|=
literal|"Snapshot "
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" looks empty"
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
name|CorruptedSnapshotException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|String
name|errorMsg
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|regionManifests
operator|.
name|size
argument_list|()
operator|!=
name|regions
operator|.
name|size
argument_list|()
condition|)
block|{
name|errorMsg
operator|=
literal|"Regions moved during the snapshot '"
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|"'. expected="
operator|+
name|regions
operator|.
name|size
argument_list|()
operator|+
literal|" snapshotted="
operator|+
name|regionManifests
operator|.
name|size
argument_list|()
operator|+
literal|"."
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
comment|// Verify HRegionInfo
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|SnapshotRegionManifest
name|regionManifest
init|=
name|regionManifests
operator|.
name|get
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionManifest
operator|==
literal|null
condition|)
block|{
comment|// could happen due to a move or split race.
name|String
name|mesg
init|=
literal|" No snapshot region directory found for region:"
operator|+
name|region
decl_stmt|;
if|if
condition|(
name|errorMsg
operator|.
name|isEmpty
argument_list|()
condition|)
name|errorMsg
operator|=
name|mesg
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|mesg
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|verifyRegionInfo
argument_list|(
name|region
argument_list|,
name|regionManifest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|errorMsg
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
comment|// Verify Snapshot HFiles
name|SnapshotReferenceUtil
operator|.
name|verifySnapshot
argument_list|(
name|services
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that the regionInfo is valid    * @param region the region to check    * @param manifest snapshot manifest to inspect    */
specifier|private
name|void
name|verifyRegionInfo
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|,
specifier|final
name|SnapshotRegionManifest
name|manifest
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|manifestRegionInfo
init|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|manifest
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|equals
argument_list|(
name|manifestRegionInfo
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"Manifest region info "
operator|+
name|manifestRegionInfo
operator|+
literal|"doesn't match expected region:"
operator|+
name|region
decl_stmt|;
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
argument_list|,
name|snapshot
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

