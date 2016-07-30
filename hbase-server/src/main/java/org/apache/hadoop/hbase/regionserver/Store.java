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
name|NavigableSet
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
name|CellComparator
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
name|HBaseInterfaceAudience
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
name|HColumnDescriptor
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
name|conf
operator|.
name|PropagatingConfigurationObserver
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
name|HeapSize
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
name|CacheConfig
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
name|HFileDataBlockEncoder
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
name|WALProtos
operator|.
name|CompactionDescriptor
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
name|compactions
operator|.
name|CompactionContext
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
name|compactions
operator|.
name|CompactionProgress
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
name|compactions
operator|.
name|CompactionRequest
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
name|querymatcher
operator|.
name|ScanQueryMatcher
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
comment|/**  * Interface for objects that hold a column family in a Region. Its a memstore and a set of zero or  * more StoreFiles, which stretch backwards over time.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|Store
extends|extends
name|HeapSize
extends|,
name|StoreConfigInformation
extends|,
name|PropagatingConfigurationObserver
block|{
comment|/* The default priority for user-specified compaction requests.    * The user gets top priority unless we have blocking compactions. (Pri<= 0)    */
name|int
name|PRIORITY_USER
init|=
literal|1
decl_stmt|;
name|int
name|NO_PRIORITY
init|=
name|Integer
operator|.
name|MIN_VALUE
decl_stmt|;
comment|// General Accessors
name|CellComparator
name|getComparator
parameter_list|()
function_decl|;
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getStorefiles
parameter_list|()
function_decl|;
comment|/**    * Close all the readers We don't need to worry about subsequent requests because the Region    * holds a write lock that will prevent any more reads or writes.    * @return the {@link StoreFile StoreFiles} that were previously being used.    * @throws IOException on failure    */
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Return a scanner for both the memstore and the HStore files. Assumes we are not in a    * compaction.    * @param scan Scan to apply when scanning the stores    * @param targetCols columns to scan    * @return a scanner over the current key values    * @throws IOException on failure    */
name|KeyValueScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|targetCols
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get all scanners with no filtering based on TTL (that happens further down    * the line).    * @param cacheBlocks    * @param isGet    * @param usePread    * @param isCompaction    * @param matcher    * @param startRow    * @param stopRow    * @param readPt    * @return all scanners for this store    */
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|isGet
parameter_list|,
name|boolean
name|usePread
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|ScanQueryMatcher
name|matcher
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create scanners on the given files and if needed on the memstore with no filtering based on TTL    * (that happens further down the line).    * @param files the list of files on which the scanners has to be created    * @param cacheBlocks cache the blocks or not    * @param isGet true if it is get, false if not    * @param usePread true to use pread, false if not    * @param isCompaction true if the scanner is created for compaction    * @param matcher the scan query matcher    * @param startRow the start row    * @param stopRow the stop row    * @param readPt the read point of the current scan    * @param includeMemstoreScanner true if memstore has to be included    * @return scanners on the given files and on the memstore if specified    */
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|getScanners
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|isGet
parameter_list|,
name|boolean
name|usePread
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|ScanQueryMatcher
name|matcher
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|,
name|long
name|readPt
parameter_list|,
name|boolean
name|includeMemstoreScanner
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|ScanInfo
name|getScanInfo
parameter_list|()
function_decl|;
comment|/**    * Adds or replaces the specified KeyValues.    *<p>    * For each KeyValue specified, if a cell with the same row, family, and qualifier exists in    * MemStore, it will be replaced. Otherwise, it will just be inserted to MemStore.    *<p>    * This operation is atomic on each KeyValue (row/family/qualifier) but not necessarily atomic    * across all of them.    * @param cells    * @param readpoint readpoint below which we can safely remove duplicate KVs    * @return memstore size delta    * @throws IOException    */
name|long
name|upsert
parameter_list|(
name|Iterable
argument_list|<
name|Cell
argument_list|>
name|cells
parameter_list|,
name|long
name|readpoint
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds a value to the memstore    * @param cell    * @return memstore size delta    */
name|long
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|)
function_decl|;
comment|/**    * When was the last edit done in the memstore    */
name|long
name|timeOfOldestEdit
parameter_list|()
function_decl|;
name|FileSystem
name|getFileSystem
parameter_list|()
function_decl|;
comment|/**    * @param maxKeyCount    * @param compression Compression algorithm to use    * @param isCompaction whether we are creating a new file in a compaction    * @param includeMVCCReadpoint whether we should out the MVCC readpoint    * @return Writer for a new StoreFile in the tmp dir.    */
name|StoreFileWriter
name|createWriterInTmp
parameter_list|(
name|long
name|maxKeyCount
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compression
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|includeMVCCReadpoint
parameter_list|,
name|boolean
name|includesTags
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param maxKeyCount    * @param compression Compression algorithm to use    * @param isCompaction whether we are creating a new file in a compaction    * @param includeMVCCReadpoint whether we should out the MVCC readpoint    * @param shouldDropBehind should the writer drop caches behind writes    * @return Writer for a new StoreFile in the tmp dir.    */
name|StoreFileWriter
name|createWriterInTmp
parameter_list|(
name|long
name|maxKeyCount
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compression
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|includeMVCCReadpoint
parameter_list|,
name|boolean
name|includesTags
parameter_list|,
name|boolean
name|shouldDropBehind
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param maxKeyCount    * @param compression Compression algorithm to use    * @param isCompaction whether we are creating a new file in a compaction    * @param includeMVCCReadpoint whether we should out the MVCC readpoint    * @param shouldDropBehind should the writer drop caches behind writes    * @param trt Ready-made timetracker to use.    * @return Writer for a new StoreFile in the tmp dir.    */
name|StoreFileWriter
name|createWriterInTmp
parameter_list|(
name|long
name|maxKeyCount
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compression
parameter_list|,
name|boolean
name|isCompaction
parameter_list|,
name|boolean
name|includeMVCCReadpoint
parameter_list|,
name|boolean
name|includesTags
parameter_list|,
name|boolean
name|shouldDropBehind
parameter_list|,
specifier|final
name|TimeRangeTracker
name|trt
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// Compaction oriented methods
name|boolean
name|throttleCompaction
parameter_list|(
name|long
name|compactionSize
parameter_list|)
function_decl|;
comment|/**    * getter for CompactionProgress object    * @return CompactionProgress object; can be null    */
name|CompactionProgress
name|getCompactionProgress
parameter_list|()
function_decl|;
name|CompactionContext
name|requestCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @deprecated see requestCompaction(int, CompactionRequest, User)    */
annotation|@
name|Deprecated
name|CompactionContext
name|requestCompaction
parameter_list|(
name|int
name|priority
parameter_list|,
name|CompactionRequest
name|baseRequest
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|CompactionContext
name|requestCompaction
parameter_list|(
name|int
name|priority
parameter_list|,
name|CompactionRequest
name|baseRequest
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|cancelRequestedCompaction
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|)
function_decl|;
comment|/**    * @deprecated see compact(CompactionContext, ThroughputController, User)    */
annotation|@
name|Deprecated
name|List
argument_list|<
name|StoreFile
argument_list|>
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|List
argument_list|<
name|StoreFile
argument_list|>
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return true if we should run a major compaction.    */
name|boolean
name|isMajorCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|triggerMajorCompaction
parameter_list|()
function_decl|;
comment|/**    * See if there's too much store files in this store    * @return true if number of store files is greater than the number defined in minFilesToCompact    */
name|boolean
name|needsCompaction
parameter_list|()
function_decl|;
name|int
name|getCompactPriority
parameter_list|()
function_decl|;
name|StoreFlushContext
name|createFlushContext
parameter_list|(
name|long
name|cacheFlushId
parameter_list|)
function_decl|;
comment|/**    * Call to complete a compaction. Its for the case where we find in the WAL a compaction    * that was not finished.  We could find one recovering a WAL after a regionserver crash.    * See HBASE-2331.    * @param compaction the descriptor for compaction    * @param pickCompactionFiles whether or not pick up the new compaction output files and    * add it to the store    * @param removeFiles whether to remove/archive files from filesystem    */
name|void
name|replayCompactionMarker
parameter_list|(
name|CompactionDescriptor
name|compaction
parameter_list|,
name|boolean
name|pickCompactionFiles
parameter_list|,
name|boolean
name|removeFiles
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// Split oriented methods
name|boolean
name|canSplit
parameter_list|()
function_decl|;
comment|/**    * Determines if Store should be split    * @return byte[] if store should be split, null otherwise.    */
name|byte
index|[]
name|getSplitPoint
parameter_list|()
function_decl|;
comment|// Bulk Load methods
comment|/**    * This throws a WrongRegionException if the HFile does not fit in this region, or an    * InvalidHFileException if the HFile is not valid.    */
name|void
name|assertBulkLoadHFileOk
parameter_list|(
name|Path
name|srcPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * This method should only be called from Region. It is assumed that the ranges of values in the    * HFile fit within the stores assigned region. (assertBulkLoadHFileOk checks this)    *    * @param srcPathStr    * @param sequenceId sequence Id associated with the HFile    */
name|Path
name|bulkLoadHFile
parameter_list|(
name|String
name|srcPathStr
parameter_list|,
name|long
name|sequenceId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// General accessors into the state of the store
comment|// TODO abstract some of this out into a metrics class
comment|/**    * @return<tt>true</tt> if the store has any underlying reference files to older HFiles    */
name|boolean
name|hasReferences
parameter_list|()
function_decl|;
comment|/**    * @return The size of this store's memstore, in bytes    */
name|long
name|getMemStoreSize
parameter_list|()
function_decl|;
comment|/**    * @return The amount of memory we could flush from this memstore; usually this is equal to    * {@link #getMemStoreSize()} unless we are carrying snapshots and then it will be the size of    * outstanding snapshots.    */
name|long
name|getFlushableSize
parameter_list|()
function_decl|;
comment|/**    * Returns the memstore snapshot size    * @return size of the memstore snapshot    */
name|long
name|getSnapshotSize
parameter_list|()
function_decl|;
name|HColumnDescriptor
name|getFamily
parameter_list|()
function_decl|;
comment|/**    * @return The maximum sequence id in all store files.    */
name|long
name|getMaxSequenceId
parameter_list|()
function_decl|;
comment|/**    * @return The maximum memstoreTS in all store files.    */
name|long
name|getMaxMemstoreTS
parameter_list|()
function_decl|;
comment|/**    * @return the data block encoder    */
name|HFileDataBlockEncoder
name|getDataBlockEncoder
parameter_list|()
function_decl|;
comment|/** @return aggregate size of all HStores used in the last compaction */
name|long
name|getLastCompactSize
parameter_list|()
function_decl|;
comment|/** @return aggregate size of HStore */
name|long
name|getSize
parameter_list|()
function_decl|;
comment|/**    * @return Count of store files    */
name|int
name|getStorefilesCount
parameter_list|()
function_decl|;
comment|/**    * @return Max age of store files in this store    */
name|long
name|getMaxStoreFileAge
parameter_list|()
function_decl|;
comment|/**    * @return Min age of store files in this store    */
name|long
name|getMinStoreFileAge
parameter_list|()
function_decl|;
comment|/**    *  @return Average age of store files in this store, 0 if no store files    */
name|long
name|getAvgStoreFileAge
parameter_list|()
function_decl|;
comment|/**    *  @return Number of reference files in this store    */
name|long
name|getNumReferenceFiles
parameter_list|()
function_decl|;
comment|/**    *  @return Number of HFiles in this store    */
name|long
name|getNumHFiles
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store files, in bytes, uncompressed.    */
name|long
name|getStoreSizeUncompressed
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store files, in bytes.    */
name|long
name|getStorefilesSize
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store file indexes, in bytes.    */
name|long
name|getStorefilesIndexSize
parameter_list|()
function_decl|;
comment|/**    * Returns the total size of all index blocks in the data block indexes, including the root level,    * intermediate levels, and the leaf level for multi-level indexes, or just the root level for    * single-level indexes.    * @return the total size of block indexes in the store    */
name|long
name|getTotalStaticIndexSize
parameter_list|()
function_decl|;
comment|/**    * Returns the total byte size of all Bloom filter bit arrays. For compound Bloom filters even the    * Bloom blocks currently not loaded into the block cache are counted.    * @return the total size of all Bloom filters in the store    */
name|long
name|getTotalStaticBloomSize
parameter_list|()
function_decl|;
comment|// Test-helper methods
comment|/**    * Used for tests.    * @return cache configuration for this Store.    */
name|CacheConfig
name|getCacheConfig
parameter_list|()
function_decl|;
comment|/**    * @return the parent region info hosting this store    */
name|HRegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
name|RegionCoprocessorHost
name|getCoprocessorHost
parameter_list|()
function_decl|;
name|boolean
name|areWritesEnabled
parameter_list|()
function_decl|;
comment|/**    * @return The smallest mvcc readPoint across all the scanners in this    * region. Writes older than this readPoint, are included  in every    * read operation.    */
name|long
name|getSmallestReadPoint
parameter_list|()
function_decl|;
name|String
name|getColumnFamilyName
parameter_list|()
function_decl|;
name|TableName
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * @return The number of cells flushed to disk    */
name|long
name|getFlushedCellsCount
parameter_list|()
function_decl|;
comment|/**    * @return The total size of data flushed to disk, in bytes    */
name|long
name|getFlushedCellsSize
parameter_list|()
function_decl|;
comment|/**    * @return The total size of out output files on disk, in bytes    */
name|long
name|getFlushedOutputFileSize
parameter_list|()
function_decl|;
comment|/**    * @return The number of cells processed during minor compactions    */
name|long
name|getCompactedCellsCount
parameter_list|()
function_decl|;
comment|/**    * @return The total amount of data processed during minor compactions, in bytes    */
name|long
name|getCompactedCellsSize
parameter_list|()
function_decl|;
comment|/**    * @return The number of cells processed during major compactions    */
name|long
name|getMajorCompactedCellsCount
parameter_list|()
function_decl|;
comment|/**    * @return The total amount of data processed during major compactions, in bytes    */
name|long
name|getMajorCompactedCellsSize
parameter_list|()
function_decl|;
comment|/*    * @param o Observer who wants to know about changes in set of Readers    */
name|void
name|addChangedReaderObserver
parameter_list|(
name|ChangedReadersObserver
name|o
parameter_list|)
function_decl|;
comment|/*    * @param o Observer no longer interested in changes in set of Readers.    */
name|void
name|deleteChangedReaderObserver
parameter_list|(
name|ChangedReadersObserver
name|o
parameter_list|)
function_decl|;
comment|/**    * @return Whether this store has too many store files.    */
name|boolean
name|hasTooManyStoreFiles
parameter_list|()
function_decl|;
comment|/**    * Checks the underlying store files, and opens the files that  have not    * been opened, and removes the store file readers for store files no longer    * available. Mainly used by secondary region replicas to keep up to date with    * the primary region files.    * @throws IOException    */
name|void
name|refreshStoreFiles
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * This value can represent the degree of emergency of compaction for this store. It should be    * greater than or equal to 0.0, any value greater than 1.0 means we have too many store files.    *<ul>    *<li>if getStorefilesCount&lt;= getMinFilesToCompact, return 0.0</li>    *<li>return (getStorefilesCount - getMinFilesToCompact) / (blockingFileCount -    * getMinFilesToCompact)</li>    *</ul>    *<p>    * And for striped stores, we should calculate this value by the files in each stripe separately    * and return the maximum value.    *<p>    * It is similar to {@link #getCompactPriority()} except that it is more suitable to use in a    * linear formula.    */
name|double
name|getCompactionPressure
parameter_list|()
function_decl|;
comment|/**     * Replaces the store files that the store has with the given files. Mainly used by     * secondary region replicas to keep up to date with     * the primary region files.     * @throws IOException     */
name|void
name|refreshStoreFiles
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|newFiles
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|bulkLoadHFile
parameter_list|(
name|StoreFileInfo
name|fileInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|boolean
name|isPrimaryReplicaStore
parameter_list|()
function_decl|;
comment|/**    * Closes and archives the compacted files under this store    */
name|void
name|closeAndArchiveCompactedFiles
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * This method is called when it is clear that the flush to disk is completed.    * The store may do any post-flush actions at this point.    * One example is to update the wal with sequence number that is known only at the store level.    */
name|void
name|finalizeFlush
parameter_list|()
function_decl|;
name|MemStore
name|getMemStore
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

