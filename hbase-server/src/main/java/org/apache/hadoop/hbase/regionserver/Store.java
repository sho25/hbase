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

begin_comment
comment|/**  * Interface for objects that hold a column family in a Region. Its a memstore and a set of zero or  * more StoreFiles, which stretch backwards over time.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
block|{
comment|/* The default priority for user-specified compaction requests.    * The user gets top priority unless we have blocking compactions. (Pri<= 0)    */
specifier|public
specifier|static
specifier|final
name|int
name|PRIORITY_USER
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|NO_PRIORITY
init|=
name|Integer
operator|.
name|MIN_VALUE
decl_stmt|;
comment|// General Accessors
specifier|public
name|KeyValue
operator|.
name|KVComparator
name|getComparator
parameter_list|()
function_decl|;
specifier|public
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getStorefiles
parameter_list|()
function_decl|;
comment|/**    * Close all the readers We don't need to worry about subsequent requests because the HRegion    * holds a write lock that will prevent any more reads or writes.    * @return the {@link StoreFile StoreFiles} that were previously being used.    * @throws IOException on failure    */
specifier|public
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
specifier|public
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
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get all scanners with no filtering based on TTL (that happens further down    * the line).    * @param cacheBlocks    * @param isGet    * @param isCompaction    * @param matcher    * @param startRow    * @param stopRow    * @return all scanners for this store    */
specifier|public
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
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|ScanInfo
name|getScanInfo
parameter_list|()
function_decl|;
comment|/**    * Adds or replaces the specified KeyValues.    *<p>    * For each KeyValue specified, if a cell with the same row, family, and qualifier exists in    * MemStore, it will be replaced. Otherwise, it will just be inserted to MemStore.    *<p>    * This operation is atomic on each KeyValue (row/family/qualifier) but not necessarily atomic    * across all of them.    * @param cells    * @param readpoint readpoint below which we can safely remove duplicate KVs     * @return memstore size delta    * @throws IOException    */
specifier|public
name|long
name|upsert
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
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
comment|/**    * Adds a value to the memstore    * @param kv    * @return memstore size delta    */
specifier|public
name|long
name|add
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
function_decl|;
comment|/**    * Removes a kv from the memstore. The KeyValue is removed only if its key& memstoreTS match the    * key& memstoreTS value of the kv parameter.    * @param kv    */
specifier|public
name|void
name|rollback
parameter_list|(
specifier|final
name|KeyValue
name|kv
parameter_list|)
function_decl|;
comment|/**    * Find the key that matches<i>row</i> exactly, or the one that immediately precedes it. WARNING:    * Only use this method on a table where writes occur with strictly increasing timestamps. This    * method assumes this pattern of writes in order to make it reasonably performant. Also our    * search is dependent on the axiom that deletes are for cells that are in the container that    * follows whether a memstore snapshot or a storefile, not for the current container: i.e. we'll    * see deletes before we come across cells we are to delete. Presumption is that the    * memstore#kvset is processed before memstore#snapshot and so on.    * @param row The row key of the targeted row.    * @return Found keyvalue or null if none found.    * @throws IOException    */
specifier|public
name|KeyValue
name|getRowKeyAtOrBefore
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
function_decl|;
comment|/*    * @param maxKeyCount    * @param compression Compression algorithm to use    * @param isCompaction whether we are creating a new file in a compaction    * @return Writer for a new StoreFile in the tmp dir.    */
specifier|public
name|StoreFile
operator|.
name|Writer
name|createWriterInTmp
parameter_list|(
name|int
name|maxKeyCount
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compression
parameter_list|,
name|boolean
name|isCompaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// Compaction oriented methods
specifier|public
name|boolean
name|throttleCompaction
parameter_list|(
name|long
name|compactionSize
parameter_list|)
function_decl|;
comment|/**    * getter for CompactionProgress object    * @return CompactionProgress object; can be null    */
specifier|public
name|CompactionProgress
name|getCompactionProgress
parameter_list|()
function_decl|;
specifier|public
name|CompactionContext
name|requestCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
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
specifier|public
name|void
name|cancelRequestedCompaction
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|)
function_decl|;
specifier|public
name|List
argument_list|<
name|StoreFile
argument_list|>
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return true if we should run a major compaction.    */
specifier|public
name|boolean
name|isMajorCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|triggerMajorCompaction
parameter_list|()
function_decl|;
comment|/**    * See if there's too much store files in this store    * @return true if number of store files is greater than the number defined in minFilesToCompact    */
specifier|public
name|boolean
name|needsCompaction
parameter_list|()
function_decl|;
specifier|public
name|int
name|getCompactPriority
parameter_list|()
function_decl|;
specifier|public
name|StoreFlusher
name|getStoreFlusher
parameter_list|(
name|long
name|cacheFlushId
parameter_list|)
function_decl|;
comment|// Split oriented methods
specifier|public
name|boolean
name|canSplit
parameter_list|()
function_decl|;
comment|/**    * Determines if Store should be split    * @return byte[] if store should be split, null otherwise.    */
specifier|public
name|byte
index|[]
name|getSplitPoint
parameter_list|()
function_decl|;
comment|// Bulk Load methods
comment|/**    * This throws a WrongRegionException if the HFile does not fit in this region, or an    * InvalidHFileException if the HFile is not valid.    */
specifier|public
name|void
name|assertBulkLoadHFileOk
parameter_list|(
name|Path
name|srcPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * This method should only be called from HRegion. It is assumed that the ranges of values in the    * HFile fit within the stores assigned region. (assertBulkLoadHFileOk checks this)    *     * @param srcPathStr    * @param sequenceId sequence Id associated with the HFile    */
specifier|public
name|void
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
specifier|public
name|boolean
name|hasReferences
parameter_list|()
function_decl|;
comment|/**    * @return The size of this store's memstore, in bytes    */
specifier|public
name|long
name|getMemStoreSize
parameter_list|()
function_decl|;
specifier|public
name|HColumnDescriptor
name|getFamily
parameter_list|()
function_decl|;
comment|/**    * @return The maximum memstoreTS in all store files.    */
specifier|public
name|long
name|getMaxMemstoreTS
parameter_list|()
function_decl|;
comment|/**    * @return the data block encoder    */
specifier|public
name|HFileDataBlockEncoder
name|getDataBlockEncoder
parameter_list|()
function_decl|;
comment|/** @return aggregate size of all HStores used in the last compaction */
specifier|public
name|long
name|getLastCompactSize
parameter_list|()
function_decl|;
comment|/** @return aggregate size of HStore */
specifier|public
name|long
name|getSize
parameter_list|()
function_decl|;
comment|/**    * @return Count of store files    */
specifier|public
name|int
name|getStorefilesCount
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store files, in bytes, uncompressed.    */
specifier|public
name|long
name|getStoreSizeUncompressed
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store files, in bytes.    */
specifier|public
name|long
name|getStorefilesSize
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store file indexes, in bytes.    */
specifier|public
name|long
name|getStorefilesIndexSize
parameter_list|()
function_decl|;
comment|/**    * Returns the total size of all index blocks in the data block indexes, including the root level,    * intermediate levels, and the leaf level for multi-level indexes, or just the root level for    * single-level indexes.    * @return the total size of block indexes in the store    */
specifier|public
name|long
name|getTotalStaticIndexSize
parameter_list|()
function_decl|;
comment|/**    * Returns the total byte size of all Bloom filter bit arrays. For compound Bloom filters even the    * Bloom blocks currently not loaded into the block cache are counted.    * @return the total size of all Bloom filters in the store    */
specifier|public
name|long
name|getTotalStaticBloomSize
parameter_list|()
function_decl|;
comment|// Test-helper methods
comment|/**    * Used for tests.    * @return cache configuration for this Store.    */
specifier|public
name|CacheConfig
name|getCacheConfig
parameter_list|()
function_decl|;
comment|/**    * @return the parent region info hosting this store    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
specifier|public
name|RegionCoprocessorHost
name|getCoprocessorHost
parameter_list|()
function_decl|;
specifier|public
name|boolean
name|areWritesEnabled
parameter_list|()
function_decl|;
comment|/**    * @return The smallest mvcc readPoint across all the scanners in this    * region. Writes older than this readPoint, are included  in every    * read operation.    */
specifier|public
name|long
name|getSmallestReadPoint
parameter_list|()
function_decl|;
specifier|public
name|String
name|getColumnFamilyName
parameter_list|()
function_decl|;
specifier|public
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/*    * @param o Observer who wants to know about changes in set of Readers    */
specifier|public
name|void
name|addChangedReaderObserver
parameter_list|(
name|ChangedReadersObserver
name|o
parameter_list|)
function_decl|;
comment|/*    * @param o Observer no longer interested in changes in set of Readers.    */
specifier|public
name|void
name|deleteChangedReaderObserver
parameter_list|(
name|ChangedReadersObserver
name|o
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

