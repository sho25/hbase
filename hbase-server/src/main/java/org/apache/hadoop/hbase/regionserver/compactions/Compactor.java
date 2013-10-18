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
name|java
operator|.
name|util
operator|.
name|Map
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
name|Cell
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
name|KeyValue
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
name|KeyValueUtil
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
name|io
operator|.
name|CellOutputStream
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
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|FileInfo
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
name|hfile
operator|.
name|HFileWriterV2
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
name|HStore
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
name|MultiVersionConsistencyControl
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * A compactor is a compaction algorithm associated a given policy. Base class also contains  * reusable parts for implementing compactors (what is common and what isn't is evolving).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
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
name|Compactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|CompactionProgress
name|progress
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|Store
name|store
decl_stmt|;
specifier|private
name|int
name|compactionKVMax
decl_stmt|;
specifier|protected
name|Compression
operator|.
name|Algorithm
name|compactionCompression
decl_stmt|;
comment|//TODO: depending on Store is not good but, realistically, all compactors currently do.
name|Compactor
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
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|compactionKVMax
operator|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|COMPACTION_KV_MAX
argument_list|,
name|HConstants
operator|.
name|COMPACTION_KV_MAX_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactionCompression
operator|=
operator|(
name|this
operator|.
name|store
operator|.
name|getFamily
argument_list|()
operator|==
literal|null
operator|)
condition|?
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
else|:
name|this
operator|.
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getCompactionCompression
argument_list|()
expr_stmt|;
block|}
comment|/**    * TODO: Replace this with {@link CellOutputStream} when StoreFile.Writer uses cells.    */
specifier|public
interface|interface
name|CellSink
block|{
name|void
name|append
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Do a minor/major compaction on an explicit set of storefiles from a Store.    * @param request the requested compaction    * @return Product of compaction or an empty list if all cells expired or deleted and nothing made    *         it through the compaction.    * @throws IOException    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
specifier|final
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Compact a list of files for testing. Creates a fake {@link CompactionRequest} to pass to    * {@link #compact(CompactionRequest)};    * @param filesToCompact the files to compact. These are used as the compactionSelection for the    *          generated {@link CompactionRequest}.    * @param isMajor true to major compact (prune all deletes, max versions, etc)    * @return Product of compaction or an empty list if all cells expired or deleted and nothing made    *         it through the compaction.    * @throws IOException    */
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
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|compact
argument_list|(
name|cr
argument_list|)
return|;
block|}
specifier|public
name|CompactionProgress
name|getProgress
parameter_list|()
block|{
return|return
name|this
operator|.
name|progress
return|;
block|}
comment|/** The sole reason this class exists is that java has no ref/out/pointer parameters. */
specifier|protected
specifier|static
class|class
name|FileDetails
block|{
comment|/** Maximum key count after compaction (for blooms) */
specifier|public
name|long
name|maxKeyCount
init|=
literal|0
decl_stmt|;
comment|/** Earliest put timestamp if major compaction */
specifier|public
name|long
name|earliestPutTs
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
comment|/** The last key in the files we're compacting. */
specifier|public
name|long
name|maxSeqId
init|=
literal|0
decl_stmt|;
comment|/** Latest memstore read point found in any of the involved files */
specifier|public
name|long
name|maxMVCCReadpoint
init|=
literal|0
decl_stmt|;
comment|/** Max tags length**/
specifier|public
name|int
name|maxTagsLength
init|=
literal|0
decl_stmt|;
block|}
specifier|protected
name|FileDetails
name|getFileDetails
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|,
name|boolean
name|calculatePutTs
parameter_list|)
throws|throws
name|IOException
block|{
name|FileDetails
name|fd
init|=
operator|new
name|FileDetails
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
name|file
range|:
name|filesToCompact
control|)
block|{
name|long
name|seqNum
init|=
name|file
operator|.
name|getMaxSequenceId
argument_list|()
decl_stmt|;
name|fd
operator|.
name|maxSeqId
operator|=
name|Math
operator|.
name|max
argument_list|(
name|fd
operator|.
name|maxSeqId
argument_list|,
name|seqNum
argument_list|)
expr_stmt|;
name|StoreFile
operator|.
name|Reader
name|r
init|=
name|file
operator|.
name|getReader
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Null reader for "
operator|+
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// NOTE: getFilterEntries could cause under-sized blooms if the user
comment|// switches bloom type (e.g. from ROW to ROWCOL)
name|long
name|keyCount
init|=
operator|(
name|r
operator|.
name|getBloomFilterType
argument_list|()
operator|==
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getBloomFilterType
argument_list|()
operator|)
condition|?
name|r
operator|.
name|getFilterEntries
argument_list|()
else|:
name|r
operator|.
name|getEntries
argument_list|()
decl_stmt|;
name|fd
operator|.
name|maxKeyCount
operator|+=
name|keyCount
expr_stmt|;
comment|// calculate the latest MVCC readpoint in any of the involved store files
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fileInfo
init|=
name|r
operator|.
name|loadFileInfo
argument_list|()
decl_stmt|;
name|byte
name|tmp
index|[]
init|=
name|fileInfo
operator|.
name|get
argument_list|(
name|HFileWriterV2
operator|.
name|MAX_MEMSTORE_TS_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmp
operator|!=
literal|null
condition|)
block|{
name|fd
operator|.
name|maxMVCCReadpoint
operator|=
name|Math
operator|.
name|max
argument_list|(
name|fd
operator|.
name|maxMVCCReadpoint
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|tmp
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tmp
operator|=
name|fileInfo
operator|.
name|get
argument_list|(
name|FileInfo
operator|.
name|MAX_TAGS_LEN
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|!=
literal|null
condition|)
block|{
name|fd
operator|.
name|maxTagsLength
operator|=
name|Math
operator|.
name|max
argument_list|(
name|fd
operator|.
name|maxTagsLength
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|tmp
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// If required, calculate the earliest put timestamp of all involved storefiles.
comment|// This is used to remove family delete marker during compaction.
name|long
name|earliestPutTs
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|calculatePutTs
condition|)
block|{
name|tmp
operator|=
name|fileInfo
operator|.
name|get
argument_list|(
name|StoreFile
operator|.
name|EARLIEST_PUT_TS
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|==
literal|null
condition|)
block|{
comment|// There's a file with no information, must be an old one
comment|// assume we have very old puts
name|fd
operator|.
name|earliestPutTs
operator|=
name|earliestPutTs
operator|=
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
expr_stmt|;
block|}
else|else
block|{
name|earliestPutTs
operator|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|fd
operator|.
name|earliestPutTs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|fd
operator|.
name|earliestPutTs
argument_list|,
name|earliestPutTs
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Compacting "
operator|+
name|file
operator|+
literal|", keycount="
operator|+
name|keyCount
operator|+
literal|", bloomtype="
operator|+
name|r
operator|.
name|getBloomFilterType
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|", size="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|r
operator|.
name|length
argument_list|()
argument_list|)
operator|+
literal|", encoding="
operator|+
name|r
operator|.
name|getHFileReader
argument_list|()
operator|.
name|getEncodingOnDisk
argument_list|()
operator|+
literal|", seqNum="
operator|+
name|seqNum
operator|+
operator|(
name|calculatePutTs
condition|?
literal|", earliestPutTs="
operator|+
name|earliestPutTs
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|fd
return|;
block|}
specifier|protected
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|createFileScanners
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|StoreFileScanner
operator|.
name|getScannersForStoreFiles
argument_list|(
name|filesToCompact
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|smallestReadPoint
argument_list|)
return|;
block|}
specifier|protected
name|long
name|getSmallestReadPoint
parameter_list|()
block|{
return|return
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
return|;
block|}
specifier|protected
name|InternalScanner
name|preCreateCoprocScanner
parameter_list|(
specifier|final
name|CompactionRequest
name|request
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preCompactScannerOpen
argument_list|(
name|store
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|earliestPutTs
argument_list|,
name|request
argument_list|)
return|;
block|}
specifier|protected
name|InternalScanner
name|postCreateCoprocScanner
parameter_list|(
specifier|final
name|CompactionRequest
name|request
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|==
literal|null
condition|)
return|return
name|scanner
return|;
return|return
name|store
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preCompact
argument_list|(
name|store
argument_list|,
name|scanner
argument_list|,
name|scanType
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|protected
name|boolean
name|performCompaction
parameter_list|(
name|InternalScanner
name|scanner
parameter_list|,
name|CellSink
name|writer
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|bytesWritten
init|=
literal|0
decl_stmt|;
comment|// Since scanner.next() can return 'false' but still be delivering data,
comment|// we have to use a do/while loop.
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
comment|// Limit to "hbase.hstore.compaction.kv.max" (default 10) to avoid OOME
name|int
name|closeCheckInterval
init|=
name|HStore
operator|.
name|getCloseCheckInterval
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
decl_stmt|;
do|do
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|kvs
argument_list|,
name|compactionKVMax
argument_list|)
expr_stmt|;
comment|// output to writer:
for|for
control|(
name|Cell
name|c
range|:
name|kvs
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|kv
operator|.
name|getMvccVersion
argument_list|()
operator|<=
name|smallestReadPoint
condition|)
block|{
name|kv
operator|.
name|setMvccVersion
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
operator|++
name|progress
operator|.
name|currentCompactedKVs
expr_stmt|;
comment|// check periodically to see if a system stop is requested
if|if
condition|(
name|closeCheckInterval
operator|>
literal|0
condition|)
block|{
name|bytesWritten
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
if|if
condition|(
name|bytesWritten
operator|>
name|closeCheckInterval
condition|)
block|{
name|bytesWritten
operator|=
literal|0
expr_stmt|;
if|if
condition|(
operator|!
name|store
operator|.
name|areWritesEnabled
argument_list|()
condition|)
block|{
name|progress
operator|.
name|cancel
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
name|progress
operator|.
name|complete
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|protected
name|void
name|abortWriter
parameter_list|(
specifier|final
name|StoreFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
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
block|}
comment|/**    * @param scanners Store file scanners.    * @param scanType Scan type.    * @param smallestReadPoint Smallest MVCC read point.    * @param earliestPutTs Earliest put across all files.    * @return A compaction scanner.    */
specifier|protected
name|InternalScanner
name|createScanner
parameter_list|(
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|,
name|long
name|earliestPutTs
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|store
operator|.
name|getScanInfo
argument_list|()
argument_list|,
name|scan
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|smallestReadPoint
argument_list|,
name|earliestPutTs
argument_list|)
return|;
block|}
block|}
end_class

end_unit

