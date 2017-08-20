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
name|client
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
name|UUID
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
name|CellUtil
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
name|util
operator|.
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * A Scanner which performs a scan over snapshot files. Using this class requires copying the  * snapshot to a temporary empty directory, which will copy the snapshot reference files into that  * directory. Actual data files are not copied.  *  *<p>  * This also allows one to run the scan from an  * online or offline hbase cluster. The snapshot files can be exported by using the  * org.apache.hadoop.hbase.snapshot.ExportSnapshot tool,  * to a pure-hdfs cluster, and this scanner can be used to  * run the scan directly over the snapshot files. The snapshot should not be deleted while there  * are open scanners reading from snapshot files.  *  *<p>  * An internal RegionScanner is used to execute the {@link Scan} obtained  * from the user for each region in the snapshot.  *<p>  * HBase owns all the data and snapshot files on the filesystem. Only the HBase user can read from  * snapshot files and data files. HBase also enforces security because all the requests are handled  * by the server layer, and the user cannot read from the data files directly. To read from snapshot  * files directly from the file system, the user who is running the MR job must have sufficient  * permissions to access snapshot and reference files. This means that to run mapreduce over  * snapshot files, the job has to be run as the HBase user or the user must have group or other  * priviledges in the filesystem (See HBASE-8369). Note that, given other users access to read from  * snapshot/data files will completely circumvent the access control enforced by HBase.  * See org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormat.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|TableSnapshotScanner
extends|extends
name|AbstractClientScanner
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
name|TableSnapshotScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|String
name|snapshotName
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
name|Path
name|restoreDir
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
decl_stmt|;
specifier|private
name|TableDescriptor
name|htd
decl_stmt|;
specifier|private
name|ClientSideRegionScanner
name|currentRegionScanner
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|currentRegion
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Creates a TableSnapshotScanner.    * @param conf the configuration    * @param restoreDir a temporary directory to copy the snapshot files into. Current user should    * have write permissions to this directory, and this should not be a subdirectory of rootdir.    * The scanner deletes the contents of the directory once the scanner is closed.    * @param snapshotName the name of the snapshot to read from    * @param scan a Scan representing scan parameters    * @throws IOException in case of error    */
specifier|public
name|TableSnapshotScanner
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|restoreDir
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|,
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a TableSnapshotScanner.    * @param conf the configuration    * @param rootDir root directory for HBase.    * @param restoreDir a temporary directory to copy the snapshot files into. Current user should    * have write permissions to this directory, and this should not be a subdirectory of rootdir.    * The scanner deletes the contents of the directory once the scanner is closed.    * @param snapshotName the name of the snapshot to read from    * @param scan a Scan representing scan parameters    * @throws IOException in case of error    */
specifier|public
name|TableSnapshotScanner
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|Path
name|restoreDir
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Scan
name|scan
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
name|this
operator|.
name|snapshotName
operator|=
name|snapshotName
expr_stmt|;
name|this
operator|.
name|rootDir
operator|=
name|rootDir
expr_stmt|;
comment|// restoreDir will be deleted in close(), use a unique sub directory
name|this
operator|.
name|restoreDir
operator|=
operator|new
name|Path
argument_list|(
name|restoreDir
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|RestoreSnapshotHelper
operator|.
name|RestoreMetaChanges
name|meta
init|=
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
decl_stmt|;
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|restoredRegions
init|=
name|meta
operator|.
name|getRegionsToAdd
argument_list|()
decl_stmt|;
name|htd
operator|=
name|meta
operator|.
name|getTableDescriptor
argument_list|()
expr_stmt|;
name|regions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|restoredRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|restoredRegions
control|)
block|{
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
block|{
continue|continue;
block|}
if|if
condition|(
name|CellUtil
operator|.
name|overlappingKeys
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|scan
operator|.
name|getStopRow
argument_list|()
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|)
condition|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
block|}
comment|// sort for regions according to startKey.
name|Collections
operator|.
name|sort
argument_list|(
name|regions
argument_list|)
expr_stmt|;
name|initScanMetrics
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|currentRegionScanner
operator|==
literal|null
condition|)
block|{
name|currentRegion
operator|++
expr_stmt|;
if|if
condition|(
name|currentRegion
operator|>=
name|regions
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HRegionInfo
name|hri
init|=
name|regions
operator|.
name|get
argument_list|(
name|currentRegion
argument_list|)
decl_stmt|;
name|currentRegionScanner
operator|=
operator|new
name|ClientSideRegionScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|restoreDir
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|,
name|scan
argument_list|,
name|scanMetrics
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|scanMetrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanMetrics
operator|.
name|countOfRegions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
try|try
block|{
name|result
operator|=
name|currentRegionScanner
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|currentRegionScanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|currentRegionScanner
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|currentRegionScanner
operator|!=
literal|null
condition|)
block|{
name|currentRegionScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|this
operator|.
name|restoreDir
argument_list|,
literal|true
argument_list|)
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
literal|"Could not delete restore directory for the snapshot:"
operator|+
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|renewLease
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

