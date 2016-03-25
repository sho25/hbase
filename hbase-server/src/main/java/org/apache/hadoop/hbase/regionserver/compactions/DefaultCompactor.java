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
name|regionserver
operator|.
name|compactions
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
name|io
operator|.
name|InterruptedIOException
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
name|regionserver
operator|.
name|InternalScanner
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
name|ScanType
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
name|Store
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
name|regionserver
operator|.
name|StoreFileScanner
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
name|throttle
operator|.
name|NoLimitThroughputController
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
name|throttle
operator|.
name|ThroughputController
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

begin_comment
comment|/**  * Compact passed set of files. Create an instance and then call  * {@link #compact(CompactionRequest, ThroughputController, User)}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultCompactor
extends|extends
name|Compactor
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
name|DefaultCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|DefaultCompactor
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
comment|/**    * Do a minor/major compaction on an explicit set of storefiles from a Store.    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
specifier|final
name|CompactionRequest
name|request
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|FileDetails
name|fd
init|=
name|getFileDetails
argument_list|(
name|request
operator|.
name|getFiles
argument_list|()
argument_list|,
name|request
operator|.
name|isAllFiles
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|progress
operator|=
operator|new
name|CompactionProgress
argument_list|(
name|fd
operator|.
name|maxKeyCount
argument_list|)
expr_stmt|;
comment|// Find the smallest read point across all the Scanners.
name|long
name|smallestReadPoint
init|=
name|getSmallestReadPoint
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
decl_stmt|;
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|readersToClose
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.regionserver.compaction.private.readers"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// clone all StoreFiles, so we'll do the compaction on a independent copy of StoreFiles,
comment|// HFiles, and their readers
name|readersToClose
operator|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|request
operator|.
name|getFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|StoreFile
name|f
range|:
name|request
operator|.
name|getFiles
argument_list|()
control|)
block|{
name|readersToClose
operator|.
name|add
argument_list|(
name|f
operator|.
name|cloneForReader
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|scanners
operator|=
name|createFileScanners
argument_list|(
name|readersToClose
argument_list|,
name|smallestReadPoint
argument_list|,
name|store
operator|.
name|throttleCompaction
argument_list|(
name|request
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|readersToClose
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
name|scanners
operator|=
name|createFileScanners
argument_list|(
name|request
operator|.
name|getFiles
argument_list|()
argument_list|,
name|smallestReadPoint
argument_list|,
name|store
operator|.
name|throttleCompaction
argument_list|(
name|request
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StoreFile
operator|.
name|Writer
name|writer
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|newFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|cleanSeqId
init|=
literal|false
decl_stmt|;
name|IOException
name|e
init|=
literal|null
decl_stmt|;
try|try
block|{
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|/* Include deletes, unless we are doing a compaction of all files */
name|ScanType
name|scanType
init|=
name|request
operator|.
name|isRetainDeleteMarkers
argument_list|()
condition|?
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
else|:
name|ScanType
operator|.
name|COMPACT_DROP_DELETES
decl_stmt|;
name|scanner
operator|=
name|preCreateCoprocScanner
argument_list|(
name|request
argument_list|,
name|scanType
argument_list|,
name|fd
operator|.
name|earliestPutTs
argument_list|,
name|scanners
argument_list|,
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
name|scanner
operator|=
name|createScanner
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|smallestReadPoint
argument_list|,
name|fd
operator|.
name|earliestPutTs
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|=
name|postCreateCoprocScanner
argument_list|(
name|request
argument_list|,
name|scanType
argument_list|,
name|scanner
argument_list|,
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
comment|// NULL scanner returned from coprocessor hooks means skip normal processing.
return|return
name|newFiles
return|;
block|}
comment|// Create the writer even if no kv(Empty store file is also ok),
comment|// because we need record the max seq id for the store file, see HBASE-6059
if|if
condition|(
name|fd
operator|.
name|minSeqIdToKeep
operator|>
literal|0
condition|)
block|{
name|smallestReadPoint
operator|=
name|Math
operator|.
name|min
argument_list|(
name|fd
operator|.
name|minSeqIdToKeep
argument_list|,
name|smallestReadPoint
argument_list|)
expr_stmt|;
name|cleanSeqId
operator|=
literal|true
expr_stmt|;
block|}
name|writer
operator|=
name|createTmpWriter
argument_list|(
name|fd
argument_list|,
name|store
operator|.
name|throttleCompaction
argument_list|(
name|request
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|finished
init|=
name|performCompaction
argument_list|(
name|fd
argument_list|,
name|scanner
argument_list|,
name|writer
argument_list|,
name|smallestReadPoint
argument_list|,
name|cleanSeqId
argument_list|,
name|throughputController
argument_list|,
name|request
operator|.
name|isAllFiles
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|finished
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|store
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|writer
operator|=
literal|null
expr_stmt|;
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Aborting compaction of store "
operator|+
name|store
operator|+
literal|" in region "
operator|+
name|store
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" because it was interrupted."
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|e
operator|=
name|ioe
expr_stmt|;
comment|// Throw the exception
throw|throw
name|ioe
throw|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|appendMetadata
argument_list|(
name|fd
operator|.
name|maxSeqId
argument_list|,
name|request
operator|.
name|isAllFiles
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|newFiles
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
for|for
control|(
name|StoreFile
name|f
range|:
name|readersToClose
control|)
block|{
try|try
block|{
name|f
operator|.
name|closeReader
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception closing "
operator|+
name|f
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|newFiles
return|;
block|}
comment|/**    * Creates a writer for a new file in a temporary directory.    * @param fd The file details.    * @return Writer for a new StoreFile in the tmp dir.    * @throws IOException    */
specifier|protected
name|StoreFile
operator|.
name|Writer
name|createTmpWriter
parameter_list|(
name|FileDetails
name|fd
parameter_list|,
name|boolean
name|shouldDropBehind
parameter_list|)
throws|throws
name|IOException
block|{
comment|// When all MVCC readpoints are 0, don't write them.
comment|// See HBASE-8166, HBASE-12600, and HBASE-13389.
return|return
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|fd
operator|.
name|maxKeyCount
argument_list|,
name|this
operator|.
name|compactionCompression
argument_list|,
comment|/* isCompaction = */
literal|true
argument_list|,
comment|/* includeMVCCReadpoint = */
name|fd
operator|.
name|maxMVCCReadpoint
operator|>
literal|0
argument_list|,
comment|/* includesTags = */
name|fd
operator|.
name|maxTagsLength
operator|>
literal|0
argument_list|,
comment|/* shouldDropBehind = */
name|shouldDropBehind
argument_list|)
return|;
block|}
comment|/**    * Compact a list of files for testing. Creates a fake {@link CompactionRequest} to pass to    * {@link #compact(CompactionRequest, ThroughputController, User)};    * @param filesToCompact the files to compact. These are used as the compactionSelection for    *          the generated {@link CompactionRequest}.    * @param isMajor true to major compact (prune all deletes, max versions, etc)    * @return Product of compaction or an empty list if all cells expired or deleted and nothing \    *         made it through the compaction.    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compactForTesting
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|,
name|boolean
name|isMajor
parameter_list|)
throws|throws
name|IOException
block|{
name|CompactionRequest
name|cr
init|=
operator|new
name|CompactionRequest
argument_list|(
name|filesToCompact
argument_list|)
decl_stmt|;
name|cr
operator|.
name|setIsMajor
argument_list|(
name|isMajor
argument_list|,
name|isMajor
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|compact
argument_list|(
name|cr
argument_list|,
name|NoLimitThroughputController
operator|.
name|INSTANCE
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

