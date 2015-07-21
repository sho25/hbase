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
name|HDFSBlocksDistribution
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
name|HTableDescriptor
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
name|IsolationLevel
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
name|exceptions
operator|.
name|FailedSanityCheckException
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|AdminProtos
operator|.
name|GetRegionInfoResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|CoprocessorServiceCall
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
name|Pair
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
name|wal
operator|.
name|WALSplitter
operator|.
name|MutationReplay
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_comment
comment|/**  * Regions store data for a certain region of a table.  It stores all columns  * for each row. A given table consists of one or more Regions.  *  *<p>An Region is defined by its table and its key extent.  *  *<p>Locking at the Region level serves only one purpose: preventing the  * region from being closed (and consequently split) while other operations  * are ongoing. Each row level operation obtains both a row lock and a region  * read lock for the duration of the operation. While a scanner is being  * constructed, getScanner holds a read lock. If the scanner is successfully  * constructed, it holds a read lock until it is closed. A close takes out a  * write lock and consequently will block for ongoing operations and will block  * new operations from starting while the close is in progress.  */
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
name|HRegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
comment|/** @return table descriptor for this region */
name|HTableDescriptor
name|getTableDesc
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
comment|/**    * Return the list of Stores managed by this region    *<p>Use with caution.  Exposed for use of fixup utilities.    * @return a list of the Stores managed by this region    */
name|List
argument_list|<
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
comment|/** @return the latest sequence number that was read from storage when this region was opened */
name|long
name|getOpenSeqNum
parameter_list|()
function_decl|;
comment|/** @return the max sequence id of flushed data on this region; no edit in memory will have    * a sequence id that is less that what is returned here.    */
name|long
name|getMaxFlushedSeqId
parameter_list|()
function_decl|;
comment|/** @return the oldest flushed sequence id for the given family; can be beyond    * {@link #getMaxFlushedSeqId()} in case where we've flushed a subset of a regions column    * families    * @deprecated Since version 1.2.0. Exposes too much about our internals; shutting it down.    * Do not use.    */
annotation|@
name|VisibleForTesting
annotation|@
name|Deprecated
specifier|public
name|long
name|getOldestSeqIdOfStore
parameter_list|(
name|byte
index|[]
name|familyName
parameter_list|)
function_decl|;
comment|/**    * This can be used to determine the last time all files of this region were major compacted.    * @param majorCompactioOnly Only consider HFile that are the result of major compaction    * @return the timestamp of the oldest HFile for all stores of this region    */
name|long
name|getOldestHfileTs
parameter_list|(
name|boolean
name|majorCompactioOnly
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
comment|/** @return true if loading column families on demand by default */
name|boolean
name|isLoadingCfsOnDemandDefault
parameter_list|()
function_decl|;
comment|/** @return readpoint considering given IsolationLevel */
name|long
name|getReadpoint
parameter_list|(
name|IsolationLevel
name|isolationLevel
parameter_list|)
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
comment|/**    * Update the read request count for this region    * @param i increment    */
name|void
name|updateReadRequestsCount
parameter_list|(
name|long
name|i
parameter_list|)
function_decl|;
comment|/** @return write request count for this region */
name|long
name|getWriteRequestsCount
parameter_list|()
function_decl|;
comment|/**    * Update the write request count for this region    * @param i increment    */
name|void
name|updateWriteRequestsCount
parameter_list|(
name|long
name|i
parameter_list|)
function_decl|;
comment|/** @return memstore size for this region, in bytes */
name|long
name|getMemstoreSize
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
comment|/** @return the MetricsRegion for this region */
name|MetricsRegion
name|getMetrics
parameter_list|()
function_decl|;
comment|/** @return the block distribution for all Stores managed by this region */
name|HDFSBlocksDistribution
name|getHDFSBlocksDistribution
parameter_list|()
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Locking
comment|// Region read locks
comment|/**    * Operation enum is used in {@link Region#startRegionOperation} to provide context for    * various checks before any region operation begins.    */
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
block|}
comment|/**    * This method needs to be called before any public call that reads or    * modifies data.    * Acquires a read lock and checks if the region is closing or closed.    *<p>{@link #closeRegionOperation} MUST then always be called after    * the operation has completed, whether it succeeded or failed.    * @throws IOException    */
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
comment|/**    * Tries to acquire a lock on the given row.    * @param waitForLock if true, will block until the lock is available.    *        Otherwise, just tries to obtain the lock and returns    *        false if unavailable.    * @return the row lock if acquired,    *   null if waitForLock was false and the lock was not acquired    * @throws IOException if waitForLock was true and the lock could not be acquired after waiting    */
name|RowLock
name|getRowLock
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|waitForLock
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * If the given list of row locks is not null, releases all locks.    */
name|void
name|releaseRowLocks
parameter_list|(
name|List
argument_list|<
name|RowLock
argument_list|>
name|rowLocks
parameter_list|)
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Region operations
comment|/**    * Perform one or more append operations on a row.    * @param append    * @param nonceGroup    * @param nonce    * @return result of the operation    * @throws IOException    */
name|Result
name|append
parameter_list|(
name|Append
name|append
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
comment|/**    * Perform a batch of mutations.    *<p>    * Note this supports only Put and Delete mutations and will ignore other types passed.    * @param mutations the list of mutations    * @param nonceGroup    * @param nonce    * @return an array of OperationStatus which internally contains the    *         OperationStatusCode and the exceptionMessage if any.    * @throws IOException    */
name|OperationStatus
index|[]
name|batchMutate
parameter_list|(
name|Mutation
index|[]
name|mutations
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
comment|/**    * Replay a batch of mutations.    * @param mutations mutations to replay.    * @param replaySeqId    * @return an array of OperationStatus which internally contains the    *         OperationStatusCode and the exceptionMessage if any.    * @throws IOException    */
name|OperationStatus
index|[]
name|batchReplay
parameter_list|(
name|MutationReplay
index|[]
name|mutations
parameter_list|,
name|long
name|replaySeqId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected val    * If it does, it performs the row mutations.  If the passed value is null, t    * is for the lack of column (ie: non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp the comparison operator    * @param comparator    * @param mutation    * @param writeToWAL    * @return true if mutation was applied, false otherwise    * @throws IOException    */
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
name|CompareOp
name|compareOp
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
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected val    * If it does, it performs the row mutations.  If the passed value is null, t    * is for the lack of column (ie: non-existence)    * @param row to check    * @param family column family to check    * @param qualifier column qualifier to check    * @param compareOp the comparison operator    * @param comparator    * @param mutations    * @param writeToWAL    * @return true if mutation was applied, false otherwise    * @throws IOException    */
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
name|CompareOp
name|compareOp
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
comment|/** The comparator to be used with the region */
name|CellComparator
name|getCellCompartor
parameter_list|()
function_decl|;
comment|/**    * Perform one or more increment operations on a row.    * @param increment    * @param nonceGroup    * @param nonce    * @return result of the operation    * @throws IOException    */
name|Result
name|increment
parameter_list|(
name|Increment
name|increment
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
comment|/**    * Listener class to enable callers of    * bulkLoadHFile() to perform any necessary    * pre/post processing of a given bulkload call    */
interface|interface
name|BulkLoadListener
block|{
comment|/**      * Called before an HFile is actually loaded      * @param family family being loaded to      * @param srcPath path of HFile      * @return final path to be used for actual loading      * @throws IOException      */
name|String
name|prepareBulkLoad
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|String
name|srcPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Called after a successful HFile load      * @param family family being loaded to      * @param srcPath path of HFile      * @throws IOException      */
name|void
name|doneBulkLoad
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|String
name|srcPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Called after a failed HFile load      * @param family family being loaded to      * @param srcPath path of HFile      * @throws IOException      */
name|void
name|failedBulkLoad
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|String
name|srcPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Attempts to atomically load a group of hfiles.  This is critical for loading    * rows with multiple column families atomically.    *    * @param familyPaths List of Pair&lt;byte[] column family, String hfilePath&gt;    * @param bulkLoadListener Internal hooks enabling massaging/preparation of a    * file about to be bulk loaded    * @param assignSeqId    * @return true if successful, false if failed recoverably    * @throws IOException if failed unrecoverably.    */
name|boolean
name|bulkLoadHFiles
parameter_list|(
name|Collection
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
parameter_list|,
name|boolean
name|assignSeqId
parameter_list|,
name|BulkLoadListener
name|bulkLoadListener
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Coprocessors
comment|/** @return the coprocessor host */
name|RegionCoprocessorHost
name|getCoprocessorHost
parameter_list|()
function_decl|;
comment|/**    * Executes a single protocol buffer coprocessor endpoint {@link Service} method using    * the registered protocol handlers.  {@link Service} implementations must be registered via the    * {@link Region#registerService(com.google.protobuf.Service)}    * method before they are available.    *    * @param controller an {@code RpcContoller} implementation to pass to the invoked service    * @param call a {@code CoprocessorServiceCall} instance identifying the service, method,    *     and parameters for the method invocation    * @return a protocol buffer {@code Message} instance containing the method's result    * @throws IOException if no registered service handler is found or an error    *     occurs during the invocation    * @see org.apache.hadoop.hbase.regionserver.Region#registerService(com.google.protobuf.Service)    */
name|Message
name|execService
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CoprocessorServiceCall
name|call
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Registers a new protocol buffer {@link Service} subclass as a coprocessor endpoint to    * be available for handling    * {@link Region#execService(com.google.protobuf.RpcController,    *    org.apache.hadoop.hbase.protobuf.generated.ClientProtos.CoprocessorServiceCall)}} calls.    *    *<p>    * Only a single instance may be registered per region for a given {@link Service} subclass (the    * instances are keyed on {@link com.google.protobuf.Descriptors.ServiceDescriptor#getFullName()}.    * After the first registration, subsequent calls with the same service name will fail with    * a return value of {@code false}.    *</p>    * @param instance the {@code Service} subclass instance to expose as a coprocessor endpoint    * @return {@code true} if the registration was successful, {@code false}    * otherwise    */
name|boolean
name|registerService
parameter_list|(
name|Service
name|instance
parameter_list|)
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// RowMutation processor support
comment|/**    * Check the collection of families for validity.    * @param families    * @throws NoSuchColumnFamilyException    */
name|void
name|checkFamilies
parameter_list|(
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|families
parameter_list|)
throws|throws
name|NoSuchColumnFamilyException
function_decl|;
comment|/**    * Check the collection of families for valid timestamps    * @param familyMap    * @param now current timestamp    * @throws FailedSanityCheckException    */
name|void
name|checkTimestamps
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
name|long
name|now
parameter_list|)
throws|throws
name|FailedSanityCheckException
function_decl|;
comment|/**    * Prepare a delete for a row mutation processor    * @param delete The passed delete is modified by this method. WARNING!    * @throws IOException    */
name|void
name|prepareDelete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Set up correct timestamps in the KVs in Delete object.    *<p>Caller should have the row and region locks.    * @param mutation    * @param familyCellMap    * @param now    * @throws IOException    */
name|void
name|prepareDeleteTimestamps
parameter_list|(
name|Mutation
name|mutation
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyCellMap
parameter_list|,
name|byte
index|[]
name|now
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Replace any cell timestamps set to {@link org.apache.hadoop.hbase.HConstants#LATEST_TIMESTAMP}    * provided current timestamp.    * @param values    * @param now    */
name|void
name|updateCellTimestamps
parameter_list|(
specifier|final
name|Iterable
argument_list|<
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|values
parameter_list|,
specifier|final
name|byte
index|[]
name|now
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|///////////////////////////////////////////////////////////////////////////
comment|// Flushes, compactions, splits, etc.
comment|// Wizards only, please
interface|interface
name|FlushResult
block|{
enum|enum
name|Result
block|{
name|FLUSHED_NO_COMPACTION_NEEDED
block|,
name|FLUSHED_COMPACTION_NEEDED
block|,
comment|// Special case where a flush didn't run because there's nothing in the memstores. Used when
comment|// bulk loading to know when we can still load even if a flush didn't happen.
name|CANNOT_FLUSH_MEMSTORE_EMPTY
block|,
name|CANNOT_FLUSH
block|}
comment|/** @return the detailed result code */
name|Result
name|getResult
parameter_list|()
function_decl|;
comment|/** @return true if the memstores were flushed, else false */
name|boolean
name|isFlushSucceeded
parameter_list|()
function_decl|;
comment|/** @return True if the flush requested a compaction, else false */
name|boolean
name|isCompactionNeeded
parameter_list|()
function_decl|;
block|}
comment|/**    * Flush the cache.    *    *<p>When this method is called the cache will be flushed unless:    *<ol>    *<li>the cache is empty</li>    *<li>the region is closed.</li>    *<li>a flush is already in progress</li>    *<li>writes are disabled</li>    *</ol>    *    *<p>This method may block for some time, so it should not be called from a    * time-sensitive thread.    * @param force whether we want to force a flush of all stores    * @return FlushResult indicating whether the flush was successful or not and if    * the region needs compacting    *    * @throws IOException general io exceptions    * because a snapshot was not properly persisted.    */
name|FlushResult
name|flush
parameter_list|(
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Synchronously compact all stores in the region.    *<p>This operation could block for a long time, so don't call it from a    * time-sensitive thread.    *<p>Note that no locks are taken to prevent possible conflicts between    * compaction and splitting activities. The regionserver does not normally compact    * and split in parallel. However by calling this method you may introduce    * unexpected and unhandled concurrency. Don't do this unless you know what    * you are doing.    *    * @param majorCompaction True to force a major compaction regardless of thresholds    * @throws IOException    */
name|void
name|compact
parameter_list|(
specifier|final
name|boolean
name|majorCompaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Trigger major compaction on all stores in the region.    *<p>    * Compaction will be performed asynchronously to this call by the RegionServer's    * CompactSplitThread. See also {@link Store#triggerMajorCompaction()}    * @throws IOException    */
name|void
name|triggerMajorCompaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return if a given region is in compaction now.    */
name|CompactionState
name|getCompactionState
parameter_list|()
function_decl|;
comment|/** Wait for all current flushes and compactions of the region to complete */
name|void
name|waitForFlushesAndCompactions
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

