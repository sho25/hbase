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
name|regionserver
operator|.
name|AbstractMultiFileWriter
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
name|AbstractMultiFileWriter
operator|.
name|WriterFactory
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
name|StoreFile
operator|.
name|Writer
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
name|StoreScanner
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Closeables
import|;
end_import

begin_comment
comment|/**  * Base class for implementing a Compactor which will generate multiple output files after  * compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractMultiOutputCompactor
parameter_list|<
name|T
extends|extends
name|AbstractMultiFileWriter
parameter_list|>
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
name|AbstractMultiOutputCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|AbstractMultiOutputCompactor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
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
specifier|protected
interface|interface
name|InternalScannerFactory
block|{
name|ScanType
name|getScanType
parameter_list|(
name|CompactionRequest
name|request
parameter_list|)
function_decl|;
name|InternalScanner
name|createScanner
parameter_list|(
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|protected
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|T
name|writer
parameter_list|,
specifier|final
name|CompactionRequest
name|request
parameter_list|,
name|InternalScannerFactory
name|scannerFactory
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
specifier|final
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
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|boolean
name|finished
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|/* Include deletes, unless we are doing a major compaction */
name|ScanType
name|scanType
init|=
name|scannerFactory
operator|.
name|getScanType
argument_list|(
name|request
argument_list|)
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
name|scannerFactory
operator|.
name|createScanner
argument_list|(
name|scanners
argument_list|,
name|scanType
argument_list|,
name|fd
argument_list|,
name|smallestReadPoint
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
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
return|;
block|}
name|boolean
name|cleanSeqId
init|=
literal|false
decl_stmt|;
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
comment|// Create the writer factory for compactions.
specifier|final
name|boolean
name|needMvcc
init|=
name|fd
operator|.
name|maxMVCCReadpoint
operator|>=
literal|0
decl_stmt|;
name|WriterFactory
name|writerFactory
init|=
operator|new
name|WriterFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Writer
name|createWriter
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|fd
operator|.
name|maxKeyCount
argument_list|,
name|compactionCompression
argument_list|,
literal|true
argument_list|,
name|needMvcc
argument_list|,
name|fd
operator|.
name|maxTagsLength
operator|>
literal|0
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
return|;
block|}
block|}
decl_stmt|;
comment|// Prepare multi-writer, and perform the compaction using scanner and writer.
comment|// It is ok here if storeScanner is null.
name|StoreScanner
name|storeScanner
init|=
operator|(
name|scanner
operator|instanceof
name|StoreScanner
operator|)
condition|?
operator|(
name|StoreScanner
operator|)
name|scanner
else|:
literal|null
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|storeScanner
argument_list|,
name|writerFactory
argument_list|)
expr_stmt|;
name|finished
operator|=
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
expr_stmt|;
if|if
condition|(
operator|!
name|finished
condition|)
block|{
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
name|Closeables
operator|.
name|close
argument_list|(
name|scanner
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
name|e
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
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|finished
condition|)
block|{
name|FileSystem
name|fs
init|=
name|store
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|leftoverFile
range|:
name|writer
operator|.
name|abortWriters
argument_list|()
control|)
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|leftoverFile
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
literal|"Failed to delete the leftover file "
operator|+
name|leftoverFile
operator|+
literal|" after an unfinished compaction."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
assert|assert
name|finished
operator|:
literal|"We should have exited the method on all error paths"
assert|;
return|return
name|commitMultiWriter
argument_list|(
name|writer
argument_list|,
name|fd
argument_list|,
name|request
argument_list|)
return|;
block|}
specifier|protected
specifier|abstract
name|List
argument_list|<
name|Path
argument_list|>
name|commitMultiWriter
parameter_list|(
name|T
name|writer
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

