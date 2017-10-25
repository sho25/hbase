begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|CompareOperator
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
name|client
operator|.
name|Append
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
name|CompactionState
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
name|Delete
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
name|Get
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
name|Increment
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
name|Mutation
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
name|Put
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Result
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
name|RowMutations
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
name|client
operator|.
name|TableDescriptor
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
name|ConfigurationObserver
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|filter
operator|.
name|ByteArrayComparable
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
name|CompactionLifeCycleTracker
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
comment|/**  * Region is a subset of HRegion with operations required for the {@link RegionCoprocessor  * Coprocessors}. The operations include ability to do mutations, requesting compaction, getting  * different counters/sizes, locking rows and getting access to {@linkplain Store}s.  */
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
name|Region
extends|extends
name|ConfigurationObserver
block|{
comment|///////////////////////////////////////////////////////////////////////////
comment|// Region state
comment|/** @return region information for this region */
name|RegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
comment|/** @return table descriptor for this region */
name|TableDescriptor
name|getTableDescriptor
parameter_list|()
function_decl|;
comment|/** @return true if region is available (not closed and not closing) */
name|boolean
name|isAvailable
parameter_list|()
function_decl|;
comment|/** @return true if region is closed */
name|boolean
name|isClosed
parameter_list|()
function_decl|;
comment|/** @return True if closing process has started */
name|boolean
name|isClosing
parameter_list|()
function_decl|;
comment|/** @return True if region is in recovering state */
name|boolean
name|isRecovering
parameter_list|()
function_decl|;
comment|/** @return True if region is read only */
name|boolean
name|isReadOnly
parameter_list|()
function_decl|;
comment|/** @return true if region is splittable */
name|boolean
name|isSplittable
parameter_list|()
function_decl|;
comment|/**    * @return true if region is mergeable    */
name|boolean
name|isMergeable
parameter_list|()
function_decl|;
comment|/**    * Return the list of Stores managed by this region    *<p>Use with caution.  Exposed for use of fixup utilities.    * @return a list of the Stores managed by this region    */
name|List
argument_list|<
name|?
extends|extends
name|Store
argument_list|>
name|getStores
parameter_list|()
function_decl|;
comment|/**    * Return the Store for the given family    *<p>Use with caution.  Exposed for use of fixup utilities.    * @return the Store for the given family    */
name|Store
name|getStore
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
function_decl|;
comment|/** @return list of store file names for the given families */
name|List
argument_list|<
name|String
argument_list|>
name|getStoreFileList
parameter_list|(
name|byte
index|[]
index|[]
name|columns
parameter_list|)
function_decl|;
comment|/**    * Check the region's underlying store files, open the files that have not    * been opened yet, and remove the store file readers for store files no    * longer available.    * @throws IOException    */
name|boolean
name|refreshStoreFiles
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/** @return the max sequence id of flushed data on this region; no edit in memory will have    * a sequence id that is less that what is returned here.    */
name|long
name|getMaxFlushedSeqId
parameter_list|()
function_decl|;
comment|/**    * This can be used to determine the last time all files of this region were major compacted.    * @param majorCompactionOnly Only consider HFile that are the result of major compaction    * @return the timestamp of the oldest HFile for all stores of this region    */
name|long
name|getOldestHfileTs
parameter_list|(
name|boolean
name|majorCompactionOnly
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return map of column family names to max sequence id that was read from storage when this    * region was opened    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|getMaxStoreSeqId
parameter_list|()
function_decl|;
comment|/**    * @return The earliest time a store in the region was flushed. All    *         other stores in the region would have been flushed either at, or    *         after this time.    */
name|long
name|getEarliestFlushTimeForAllStores
parameter_list|()
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Metrics
comment|/** @return read requests count for this region */
name|long
name|getReadRequestsCount
parameter_list|()
function_decl|;
comment|/** @return filtered read requests count for this region */
name|long
name|getFilteredReadRequestsCount
parameter_list|()
function_decl|;
comment|/** @return write request count for this region */
name|long
name|getWriteRequestsCount
parameter_list|()
function_decl|;
comment|/**    * @return memstore size for this region, in bytes. It just accounts data size of cells added to    *         the memstores of this Region. Means size in bytes for key, value and tags within Cells.    *         It wont consider any java heap overhead for the cell objects or any other.    */
name|long
name|getMemStoreSize
parameter_list|()
function_decl|;
comment|/** @return the number of mutations processed bypassing the WAL */
name|long
name|getNumMutationsWithoutWAL
parameter_list|()
function_decl|;
comment|/** @return the size of data processed bypassing the WAL, in bytes */
name|long
name|getDataInMemoryWithoutWAL
parameter_list|()
function_decl|;
comment|/** @return the number of blocked requests */
name|long
name|getBlockedRequestsCount
parameter_list|()
function_decl|;
comment|/** @return the number of checkAndMutate guards that passed */
name|long
name|getCheckAndMutateChecksPassed
parameter_list|()
function_decl|;
comment|/** @return the number of failed checkAndMutate guards */
name|long
name|getCheckAndMutateChecksFailed
parameter_list|()
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Locking
comment|// Region read locks
comment|/**    * Operation enum is used in {@link Region#startRegionOperation} and elsewhere to provide    * context for various checks.    */
enum|enum
name|Operation
block|{
name|ANY
block|,
name|GET
block|,
name|PUT
block|,
name|DELETE
block|,
name|SCAN
block|,
name|APPEND
block|,
name|INCREMENT
block|,
name|SPLIT_REGION
block|,
name|MERGE_REGION
block|,
name|BATCH_MUTATE
block|,
name|REPLAY_BATCH_MUTATE
block|,
name|COMPACT_REGION
block|,
name|REPLAY_EVENT
block|,
name|SNAPSHOT
block|}
comment|/**    * This method needs to be called before any public call that reads or    * modifies data.    * Acquires a read lock and checks if the region is closing or closed.    *<p>{@link #closeRegionOperation} MUST then always be called after    * the operation has completed, whether it succeeded or failed.    * @throws IOException    */
comment|// TODO Exposing this and closeRegionOperation() as we have getRowLock() exposed.
comment|// Remove if we get rid of exposing getRowLock().
name|void
name|startRegionOperation
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * This method needs to be called before any public call that reads or    * modifies data.    * Acquires a read lock and checks if the region is closing or closed.    *<p>{@link #closeRegionOperation} MUST then always be called after    * the operation has completed, whether it succeeded or failed.    * @param op The operation is about to be taken on the region    * @throws IOException    */
name|void
name|startRegionOperation
parameter_list|(
name|Operation
name|op
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the region operation lock.    * @throws IOException    */
name|void
name|closeRegionOperation
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the region operation lock. This needs to be called in the finally block corresponding    * to the try block of {@link #startRegionOperation(Operation)}    * @throws IOException    */
name|void
name|closeRegionOperation
parameter_list|(
name|Operation
name|op
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// Row write locks
comment|/**    * Row lock held by a given thread.    * One thread may acquire multiple locks on the same row simultaneously.    * The locks must be released by calling release() from the same thread.    */
specifier|public
interface|interface
name|RowLock
block|{
comment|/**      * Release the given lock.  If there are no remaining locks held by the current thread      * then unlock the row and allow other threads to acquire the lock.      * @throws IllegalArgumentException if called by a different thread than the lock owning      *     thread      */
name|void
name|release
parameter_list|()
function_decl|;
block|}
comment|/**    *    * Get a row lock for the specified row. All locks are reentrant.    *    * Before calling this function make sure that a region operation has already been    * started (the calling thread has already acquired the region-close-guard lock).    *<p>    * The obtained locks should be released after use by {@link RowLock#release()}    *<p>    * NOTE: the boolean passed here has changed. It used to be a boolean that    * stated whether or not to wait on the lock. Now it is whether it an exclusive    * lock is requested.    *    * @param row The row actions will be performed against    * @param readLock is the lock reader or writer. True indicates that a non-exclusive    * lock is requested    * @see #startRegionOperation()    * @see #startRegionOperation(Operation)    */
comment|// TODO this needs to be exposed as we have RowProcessor now. If RowProcessor is removed, we can
comment|// remove this too..
name|RowLock
name|getRowLock
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|readLock
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Region operations
comment|/**    * Perform one or more append operations on a row.    * @param append    * @return result of the operation    * @throws IOException    */
name|Result
name|append
parameter_list|(
name|Append
name|append
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Perform a batch of mutations.    *<p>    * Note this supports only Put and Delete mutations and will ignore other types passed.    * @param mutations the list of mutations    * @return an array of OperationStatus which internally contains the    *         OperationStatusCode and the exceptionMessage if any.    * @throws IOException    */
name|OperationStatus
index|[]
name|batchMutate
parameter_list|(
name|Mutation
index|[]
name|mutations
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected value and if it does,    * it performs the mutation. If the passed value is null, the lack of column value    * (ie: non-existence) is used. See checkAndRowMutate to do many checkAndPuts at a time on a    * single row.    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param op the comparison operator    * @param comparator    * @param mutation    * @param writeToWAL    * @return true if mutation was applied, false otherwise    * @throws IOException    */
name|boolean
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|ByteArrayComparable
name|comparator
parameter_list|,
name|Mutation
name|mutation
parameter_list|,
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected values and if it does,    * it performs the row mutations. If the passed value is null, the lack of column value    * (ie: non-existence) is used. Use to do many mutations on a single row. Use checkAndMutate    * to do one checkAndMutate at a time.    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param op the comparison operator    * @param comparator    * @param mutations    * @param writeToWAL    * @return true if mutations were applied, false otherwise    * @throws IOException    */
name|boolean
name|checkAndRowMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|ByteArrayComparable
name|comparator
parameter_list|,
name|RowMutations
name|mutations
parameter_list|,
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes the specified cells/row.    * @param delete    * @throws IOException    */
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Do a get based on the get parameter.    * @param get query parameters    * @return result of the operation    */
name|Result
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Do a get based on the get parameter.    * @param get query parameters    * @param withCoprocessor invoke coprocessor or not. We don't want to    * always invoke cp.    * @return list of cells resulting from the operation    */
name|List
argument_list|<
name|Cell
argument_list|>
name|get
parameter_list|(
name|Get
name|get
parameter_list|,
name|boolean
name|withCoprocessor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Return an iterator that scans over the HRegion, returning the indicated    * columns and rows specified by the {@link Scan}.    *<p>    * This Iterator must be closed by the caller.    *    * @param scan configured {@link Scan}    * @return RegionScanner    * @throws IOException read exceptions    */
name|RegionScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Return an iterator that scans over the HRegion, returning the indicated columns and rows    * specified by the {@link Scan}. The scanner will also include the additional scanners passed    * along with the scanners for the specified Scan instance. Should be careful with the usage to    * pass additional scanners only within this Region    *<p>    * This Iterator must be closed by the caller.    *    * @param scan configured {@link Scan}    * @param additionalScanners Any additional scanners to be used    * @return RegionScanner    * @throws IOException read exceptions    */
name|RegionScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|additionalScanners
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** The comparator to be used with the region */
name|CellComparator
name|getCellComparator
parameter_list|()
function_decl|;
comment|/**    * Perform one or more increment operations on a row.    * @param increment    * @return result of the operation    * @throws IOException    */
name|Result
name|increment
parameter_list|(
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs multiple mutations atomically on a single row. Currently    * {@link Put} and {@link Delete} are supported.    *    * @param mutations object that specifies the set of mutations to perform atomically    * @throws IOException    */
name|void
name|mutateRow
parameter_list|(
name|RowMutations
name|mutations
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Perform atomic mutations within the region.    *    * @param mutations The list of mutations to perform.    *<code>mutations</code> can contain operations for multiple rows.    * Caller has to ensure that all rows are contained in this region.    * @param rowsToLock Rows to lock    * @param nonceGroup Optional nonce group of the operation (client Id)    * @param nonce Optional nonce of the operation (unique random id to ensure "more idempotence")    * If multiple rows are locked care should be taken that    *<code>rowsToLock</code> is sorted in order to avoid deadlocks.    * @throws IOException    */
comment|// TODO Should not be exposing with params nonceGroup, nonce. Change when doing the jira for
comment|// Changing processRowsWithLocks and RowProcessor
name|void
name|mutateRowsWithLocks
parameter_list|(
name|Collection
argument_list|<
name|Mutation
argument_list|>
name|mutations
parameter_list|,
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|rowsToLock
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs atomic multiple reads and writes on a given row.    *    * @param processor The object defines the reads and writes to a row.    */
name|void
name|processRowsWithLocks
parameter_list|(
name|RowProcessor
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|processor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs atomic multiple reads and writes on a given row.    *    * @param processor The object defines the reads and writes to a row.    * @param nonceGroup Optional nonce group of the operation (client Id)    * @param nonce Optional nonce of the operation (unique random id to ensure "more idempotence")    */
comment|// TODO Should not be exposing with params nonceGroup, nonce. Change when doing the jira for
comment|// Changing processRowsWithLocks and RowProcessor
name|void
name|processRowsWithLocks
parameter_list|(
name|RowProcessor
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|processor
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Performs atomic multiple reads and writes on a given row.    *    * @param processor The object defines the reads and writes to a row.    * @param timeout The timeout of the processor.process() execution    *                Use a negative number to switch off the time bound    * @param nonceGroup Optional nonce group of the operation (client Id)    * @param nonce Optional nonce of the operation (unique random id to ensure "more idempotence")    */
comment|// TODO Should not be exposing with params nonceGroup, nonce. Change when doing the jira for
comment|// Changing processRowsWithLocks and RowProcessor
name|void
name|processRowsWithLocks
parameter_list|(
name|RowProcessor
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|processor
parameter_list|,
name|long
name|timeout
parameter_list|,
name|long
name|nonceGroup
parameter_list|,
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Puts some data in the table.    * @param put    * @throws IOException    */
name|void
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Flushes, compactions, splits, etc.
comment|// Wizards only, please
comment|/**    * @return if a given region is in compaction now.    */
name|CompactionState
name|getCompactionState
parameter_list|()
function_decl|;
comment|/**    * Request compaction on this region.    */
name|void
name|requestCompaction
parameter_list|(
name|String
name|why
parameter_list|,
name|int
name|priority
parameter_list|,
name|boolean
name|major
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Request compaction for the given family    */
name|void
name|requestCompaction
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|String
name|why
parameter_list|,
name|int
name|priority
parameter_list|,
name|boolean
name|major
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Request flush on this region.    */
name|void
name|requestFlush
parameter_list|(
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

