begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|AbstractMap
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
name|UUID
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
name|client
operator|.
name|Scan
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
name|ConfigurationUtil
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|Lists
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
name|Maps
import|;
end_import

begin_comment
comment|/**  * Shared implementation of mapreduce code over multiple table snapshots.  * Utilized by both mapreduce  * {@link org.apache.hadoop.hbase.mapreduce.MultiTableSnapshotInputFormat} and mapred  * {@link org.apache.hadoop.hbase.mapred.MultiTableSnapshotInputFormat} implementations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"HBase"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|MultiTableSnapshotInputFormatImpl
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
name|MultiTableSnapshotInputFormatImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RESTORE_DIRS_KEY
init|=
literal|"hbase.MultiTableSnapshotInputFormat.restore.snapshotDirMapping"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT_TO_SCANS_KEY
init|=
literal|"hbase.MultiTableSnapshotInputFormat.snapshotsToScans"
decl_stmt|;
comment|/**    * Configure conf to read from snapshotScans, with snapshots restored to a subdirectory of    * restoreDir.    * Sets: {@link #RESTORE_DIRS_KEY}, {@link #SNAPSHOT_TO_SCANS_KEY}    *    * @param conf    * @param snapshotScans    * @param restoreDir    * @throws IOException    */
specifier|public
name|void
name|setInput
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|snapshotScans
parameter_list|,
name|Path
name|restoreDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|setSnapshotToScans
argument_list|(
name|conf
argument_list|,
name|snapshotScans
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|restoreDirs
init|=
name|generateSnapshotToRestoreDirMapping
argument_list|(
name|snapshotScans
operator|.
name|keySet
argument_list|()
argument_list|,
name|restoreDir
argument_list|)
decl_stmt|;
name|setSnapshotDirs
argument_list|(
name|conf
argument_list|,
name|restoreDirs
argument_list|)
expr_stmt|;
name|restoreSnapshots
argument_list|(
name|conf
argument_list|,
name|restoreDirs
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return the list of splits extracted from the scans/snapshots pushed to conf by    * {@link    * #setInput(org.apache.hadoop.conf.Configuration, java.util.Map, org.apache.hadoop.fs.Path)}    *    * @param conf Configuration to determine splits from    * @return Return the list of splits extracted from the scans/snapshots pushed to conf    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
argument_list|>
name|rtn
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|snapshotsToScans
init|=
name|getSnapshotsToScans
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|snapshotsToRestoreDirs
init|=
name|getSnapshotDirs
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|entry
range|:
name|snapshotsToScans
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|snapshotName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Path
name|restoreDir
init|=
name|snapshotsToRestoreDirs
operator|.
name|get
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|manifest
init|=
name|TableSnapshotInputFormatImpl
operator|.
name|getSnapshotManifest
argument_list|(
name|conf
argument_list|,
name|snapshotName
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
name|TableSnapshotInputFormatImpl
operator|.
name|getRegionInfosFromManifest
argument_list|(
name|manifest
argument_list|)
decl_stmt|;
for|for
control|(
name|Scan
name|scan
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|List
argument_list|<
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
argument_list|>
name|splits
init|=
name|TableSnapshotInputFormatImpl
operator|.
name|getSplits
argument_list|(
name|scan
argument_list|,
name|manifest
argument_list|,
name|regionInfos
argument_list|,
name|restoreDir
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|rtn
operator|.
name|addAll
argument_list|(
name|splits
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rtn
return|;
block|}
comment|/**    * Retrieve the snapshot name -&gt; list&lt;scan&gt; mapping pushed to configuration by    * {@link #setSnapshotToScans(org.apache.hadoop.conf.Configuration, java.util.Map)}    *    * @param conf Configuration to extract name -&gt; list&lt;scan&gt; mappings from.    * @return the snapshot name -&gt; list&lt;scan&gt; mapping pushed to configuration    * @throws IOException    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|getSnapshotsToScans
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|rtn
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|ConfigurationUtil
operator|.
name|getKeyValues
argument_list|(
name|conf
argument_list|,
name|SNAPSHOT_TO_SCANS_KEY
argument_list|)
control|)
block|{
name|String
name|snapshotName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|scan
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|Scan
argument_list|>
name|snapshotScans
init|=
name|rtn
operator|.
name|get
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotScans
operator|==
literal|null
condition|)
block|{
name|snapshotScans
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|rtn
operator|.
name|put
argument_list|(
name|snapshotName
argument_list|,
name|snapshotScans
argument_list|)
expr_stmt|;
block|}
name|snapshotScans
operator|.
name|add
argument_list|(
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|scan
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rtn
return|;
block|}
comment|/**    * Push snapshotScans to conf (under the key {@link #SNAPSHOT_TO_SCANS_KEY})    *    * @param conf    * @param snapshotScans    * @throws IOException    */
specifier|public
name|void
name|setSnapshotToScans
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|snapshotScans
parameter_list|)
throws|throws
name|IOException
block|{
comment|// flatten out snapshotScans for serialization to the job conf
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|snapshotToSerializedScans
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|Scan
argument_list|>
argument_list|>
name|entry
range|:
name|snapshotScans
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|snapshotName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|Scan
argument_list|>
name|scans
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// serialize all scans and map them to the appropriate snapshot
for|for
control|(
name|Scan
name|scan
range|:
name|scans
control|)
block|{
name|snapshotToSerializedScans
operator|.
name|add
argument_list|(
operator|new
name|AbstractMap
operator|.
name|SimpleImmutableEntry
argument_list|<>
argument_list|(
name|snapshotName
argument_list|,
name|TableMapReduceUtil
operator|.
name|convertScanToString
argument_list|(
name|scan
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|ConfigurationUtil
operator|.
name|setKeyValues
argument_list|(
name|conf
argument_list|,
name|SNAPSHOT_TO_SCANS_KEY
argument_list|,
name|snapshotToSerializedScans
argument_list|)
expr_stmt|;
block|}
comment|/**    * Retrieve the directories into which snapshots have been restored from    * ({@link #RESTORE_DIRS_KEY})    *    * @param conf Configuration to extract restore directories from    * @return the directories into which snapshots have been restored from    * @throws IOException    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|getSnapshotDirs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|kvps
init|=
name|ConfigurationUtil
operator|.
name|getKeyValues
argument_list|(
name|conf
argument_list|,
name|RESTORE_DIRS_KEY
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|rtn
init|=
name|Maps
operator|.
name|newHashMapWithExpectedSize
argument_list|(
name|kvps
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kvp
range|:
name|kvps
control|)
block|{
name|rtn
operator|.
name|put
argument_list|(
name|kvp
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|kvp
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rtn
return|;
block|}
specifier|public
name|void
name|setSnapshotDirs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|snapshotDirs
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|toSet
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|entry
range|:
name|snapshotDirs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|toSet
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ConfigurationUtil
operator|.
name|setKeyValues
argument_list|(
name|conf
argument_list|,
name|RESTORE_DIRS_KEY
argument_list|,
name|toSet
operator|.
name|entrySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generate a random path underneath baseRestoreDir for each snapshot in snapshots and    * return a map from the snapshot to the restore directory.    *    * @param snapshots      collection of snapshot names to restore    * @param baseRestoreDir base directory under which all snapshots in snapshots will be restored    * @return a mapping from snapshot name to the directory in which that snapshot has been restored    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|generateSnapshotToRestoreDirMapping
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|snapshots
parameter_list|,
name|Path
name|baseRestoreDir
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|rtn
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|snapshotName
range|:
name|snapshots
control|)
block|{
name|Path
name|restoreSnapshotDir
init|=
operator|new
name|Path
argument_list|(
name|baseRestoreDir
argument_list|,
name|snapshotName
operator|+
literal|"__"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|rtn
operator|.
name|put
argument_list|(
name|snapshotName
argument_list|,
name|restoreSnapshotDir
argument_list|)
expr_stmt|;
block|}
return|return
name|rtn
return|;
block|}
comment|/**    * Restore each (snapshot name, restore directory) pair in snapshotToDir    *    * @param conf          configuration to restore with    * @param snapshotToDir mapping from snapshot names to restore directories    * @param fs            filesystem to do snapshot restoration on    * @throws IOException    */
specifier|public
name|void
name|restoreSnapshots
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|snapshotToDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: restore from record readers to parallelize.
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|entry
range|:
name|snapshotToDir
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|snapshotName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Path
name|restoreDir
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring snapshot "
operator|+
name|snapshotName
operator|+
literal|" into "
operator|+
name|restoreDir
operator|+
literal|" for MultiTableSnapshotInputFormat"
argument_list|)
expr_stmt|;
name|restoreSnapshot
argument_list|(
name|conf
argument_list|,
name|snapshotName
argument_list|,
name|rootDir
argument_list|,
name|restoreDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|restoreSnapshot
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|Path
name|restoreDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|RestoreSnapshotHelper
operator|.
name|copySnapshotForScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

