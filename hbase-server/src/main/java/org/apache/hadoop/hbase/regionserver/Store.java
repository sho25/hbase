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
name|OptionalDouble
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|OptionalLong
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|RegionInfo
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
block|{
comment|/**    * The default priority for user-specified compaction requests.    * The user gets top priority unless we have blocking compactions. (Pri<= 0)    */
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
name|?
extends|extends
name|StoreFile
argument_list|>
name|getStorefiles
parameter_list|()
function_decl|;
name|Collection
argument_list|<
name|?
extends|extends
name|StoreFile
argument_list|>
name|getCompactedFiles
parameter_list|()
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
comment|/**    * Tests whether we should run a major compaction. For example, if the configured major compaction    * interval is reached.    * @return true if we should run a major compaction.    */
name|boolean
name|shouldPerformMajorCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * See if there's too much store files in this store    * @return<code>true</code> if number of store files is greater than the number defined in    *         minFilesToCompact    */
name|boolean
name|needsCompaction
parameter_list|()
function_decl|;
name|int
name|getCompactPriority
parameter_list|()
function_decl|;
comment|/**    * Returns whether this store is splittable, i.e., no reference file in this store.    */
name|boolean
name|canSplit
parameter_list|()
function_decl|;
comment|/**    * @return<code>true</code> if the store has any underlying reference files to older HFiles    */
name|boolean
name|hasReferences
parameter_list|()
function_decl|;
comment|/**    * @return The size of this store's memstore.    */
name|MemStoreSize
name|getMemStoreSize
parameter_list|()
function_decl|;
comment|/**    * @return The amount of memory we could flush from this memstore; usually this is equal to    * {@link #getMemStoreSize()} unless we are carrying snapshots and then it will be the size of    * outstanding snapshots.    */
name|MemStoreSize
name|getFlushableSize
parameter_list|()
function_decl|;
comment|/**    * @return size of the memstore snapshot    */
name|MemStoreSize
name|getSnapshotSize
parameter_list|()
function_decl|;
name|ColumnFamilyDescriptor
name|getColumnFamilyDescriptor
parameter_list|()
function_decl|;
comment|/**    * @return The maximum sequence id in all store files.    */
name|OptionalLong
name|getMaxSequenceId
parameter_list|()
function_decl|;
comment|/**    * @return The maximum memstoreTS in all store files.    */
name|OptionalLong
name|getMaxMemStoreTS
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
comment|/**    * @return Count of compacted store files    */
name|int
name|getCompactedFilesCount
parameter_list|()
function_decl|;
comment|/**    * @return Max age of store files in this store    */
name|OptionalLong
name|getMaxStoreFileAge
parameter_list|()
function_decl|;
comment|/**    * @return Min age of store files in this store    */
name|OptionalLong
name|getMinStoreFileAge
parameter_list|()
function_decl|;
comment|/**    *  @return Average age of store files in this store    */
name|OptionalDouble
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
comment|/**    * @return The size of only the store files which are HFiles, in bytes.    */
name|long
name|getHFilesSize
parameter_list|()
function_decl|;
comment|/**    * @return The size of the store file root-level indexes, in bytes.    */
name|long
name|getStorefilesRootLevelIndexSize
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
comment|/**    * @return the parent region info hosting this store    */
name|RegionInfo
name|getRegionInfo
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
comment|/**    * @return Whether this store has too many store files.    */
name|boolean
name|hasTooManyStoreFiles
parameter_list|()
function_decl|;
comment|/**    * Checks the underlying store files, and opens the files that have not been opened, and removes    * the store file readers for store files no longer available. Mainly used by secondary region    * replicas to keep up to date with the primary region files.    * @throws IOException    */
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
name|boolean
name|isPrimaryReplicaStore
parameter_list|()
function_decl|;
comment|/**    * @return true if the memstore may need some extra memory space    */
name|boolean
name|isSloppyMemStore
parameter_list|()
function_decl|;
name|int
name|getCurrentParallelPutCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

