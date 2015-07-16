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
name|io
operator|.
name|compress
operator|.
name|Compression
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
name|StripeMultiFileWriter
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * This is the placeholder for stripe compactor. The implementation,  * as well as the proper javadoc, will be added in HBASE-7967.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StripeCompactor
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
name|StripeCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|StripeCompactor
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
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|CompactionRequest
name|request
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|targetBoundaries
parameter_list|,
name|byte
index|[]
name|majorRangeFromRow
parameter_list|,
name|byte
index|[]
name|majorRangeToRow
parameter_list|,
name|CompactionThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Executing compaction with "
operator|+
name|targetBoundaries
operator|.
name|size
argument_list|()
operator|+
literal|" boundaries:"
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|tb
range|:
name|targetBoundaries
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tb
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|StripeMultiFileWriter
name|writer
init|=
operator|new
name|StripeMultiFileWriter
operator|.
name|BoundaryMultiWriter
argument_list|(
name|targetBoundaries
argument_list|,
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|)
decl_stmt|;
return|return
name|compactInternal
argument_list|(
name|writer
argument_list|,
name|request
argument_list|,
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|,
name|throughputController
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|CompactionRequest
name|request
parameter_list|,
name|int
name|targetCount
parameter_list|,
name|long
name|targetSize
parameter_list|,
name|byte
index|[]
name|left
parameter_list|,
name|byte
index|[]
name|right
parameter_list|,
name|byte
index|[]
name|majorRangeFromRow
parameter_list|,
name|byte
index|[]
name|majorRangeToRow
parameter_list|,
name|CompactionThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Executing compaction with "
operator|+
name|targetSize
operator|+
literal|" target file size, no more than "
operator|+
name|targetCount
operator|+
literal|" files, in ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|left
argument_list|)
operator|+
literal|"] ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|right
argument_list|)
operator|+
literal|"] range"
argument_list|)
expr_stmt|;
block|}
name|StripeMultiFileWriter
name|writer
init|=
operator|new
name|StripeMultiFileWriter
operator|.
name|SizeMultiWriter
argument_list|(
name|targetCount
argument_list|,
name|targetSize
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
return|return
name|compactInternal
argument_list|(
name|writer
argument_list|,
name|request
argument_list|,
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|,
name|throughputController
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|compactInternal
parameter_list|(
name|StripeMultiFileWriter
name|mw
parameter_list|,
specifier|final
name|CompactionRequest
name|request
parameter_list|,
name|byte
index|[]
name|majorRangeFromRow
parameter_list|,
name|byte
index|[]
name|majorRangeToRow
parameter_list|,
name|CompactionThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
init|=
name|request
operator|.
name|getFiles
argument_list|()
decl_stmt|;
specifier|final
name|FileDetails
name|fd
init|=
name|getFileDetails
argument_list|(
name|filesToCompact
argument_list|,
name|request
operator|.
name|isMajor
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
init|=
name|createFileScanners
argument_list|(
name|filesToCompact
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
decl_stmt|;
name|boolean
name|finished
init|=
literal|false
decl_stmt|;
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|boolean
name|cleanSeqId
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// Get scanner to use.
name|ScanType
name|coprocScanType
init|=
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
decl_stmt|;
name|scanner
operator|=
name|preCreateCoprocScanner
argument_list|(
name|request
argument_list|,
name|coprocScanType
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
operator|(
name|majorRangeFromRow
operator|==
literal|null
operator|)
condition|?
name|createScanner
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
name|smallestReadPoint
argument_list|,
name|fd
operator|.
name|earliestPutTs
argument_list|)
else|:
name|createScanner
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|smallestReadPoint
argument_list|,
name|fd
operator|.
name|earliestPutTs
argument_list|,
name|majorRangeFromRow
argument_list|,
name|majorRangeToRow
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|=
name|postCreateCoprocScanner
argument_list|(
name|request
argument_list|,
name|coprocScanType
argument_list|,
name|scanner
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
comment|// Create the writer factory for compactions.
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
specifier|final
name|boolean
name|needMvcc
init|=
name|fd
operator|.
name|maxMVCCReadpoint
operator|>
literal|0
decl_stmt|;
specifier|final
name|Compression
operator|.
name|Algorithm
name|compression
init|=
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompactionCompressionType
argument_list|()
decl_stmt|;
name|StripeMultiFileWriter
operator|.
name|WriterFactory
name|factory
init|=
operator|new
name|StripeMultiFileWriter
operator|.
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
name|compression
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
name|mw
operator|.
name|init
argument_list|(
name|storeScanner
argument_list|,
name|factory
argument_list|,
name|store
operator|.
name|getComparator
argument_list|()
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
name|mw
argument_list|,
name|smallestReadPoint
argument_list|,
name|cleanSeqId
argument_list|,
name|throughputController
argument_list|,
name|request
operator|.
name|isMajor
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
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Don't fail the compaction if this fails.
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close scanner after compaction."
argument_list|,
name|t
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
for|for
control|(
name|Path
name|leftoverFile
range|:
name|mw
operator|.
name|abortWriters
argument_list|()
control|)
block|{
try|try
block|{
name|store
operator|.
name|getFileSystem
argument_list|()
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
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to delete the leftover file after an unfinished compaction."
argument_list|,
name|ex
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
name|List
argument_list|<
name|Path
argument_list|>
name|newFiles
init|=
name|mw
operator|.
name|commitWriters
argument_list|(
name|fd
operator|.
name|maxSeqId
argument_list|,
name|request
operator|.
name|isMajor
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
operator|!
name|newFiles
operator|.
name|isEmpty
argument_list|()
operator|:
literal|"Should have produced an empty file to preserve metadata."
assert|;
return|return
name|newFiles
return|;
block|}
block|}
end_class

end_unit

