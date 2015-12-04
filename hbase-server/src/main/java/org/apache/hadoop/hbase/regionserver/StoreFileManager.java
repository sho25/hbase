begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Iterator
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
name|KeyValue
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
name|collect
operator|.
name|ImmutableCollection
import|;
end_import

begin_comment
comment|/**  * Manages the store files and basic metadata about that that determines the logical structure  * (e.g. what files to return for scan, how to determine split point, and such).  * Does NOT affect the physical structure of files in HDFS.  * Example alternative structures - the default list of files by seqNum; levelDB one sorted  * by level and seqNum.  *  * Implementations are assumed to be not thread safe.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|StoreFileManager
block|{
comment|/**    * Loads the initial store files into empty StoreFileManager.    * @param storeFiles The files to load.    */
name|void
name|loadFiles
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
parameter_list|)
function_decl|;
comment|/**    * Adds new files, either for from MemStore flush or bulk insert, into the structure.    * @param sfs New store files.    */
name|void
name|insertNewFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|sfs
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds only the new compaction results into the structure.    * @param compactedFiles The input files for the compaction.    * @param results The resulting files for the compaction.    */
name|void
name|addCompactionResults
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|compactedFiles
parameter_list|,
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Remove the compacted files    * @param compactedFiles the list of compacted files    * @throws IOException    */
name|void
name|removeCompactedFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|compactedFiles
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Clears all the files currently in use and returns them.    * @return The files previously in use.    */
name|ImmutableCollection
argument_list|<
name|StoreFile
argument_list|>
name|clearFiles
parameter_list|()
function_decl|;
comment|/**    * Clears all the compacted files and returns them. This method is expected to be    * accessed single threaded.    * @return The files compacted previously.    */
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|clearCompactedFiles
parameter_list|()
function_decl|;
comment|/**    * Gets the snapshot of the store files currently in use. Can be used for things like metrics    * and checks; should not assume anything about relations between store files in the list.    * @return The list of StoreFiles.    */
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getStorefiles
parameter_list|()
function_decl|;
comment|/**    * List of compacted files inside this store that needs to be excluded in reads    * because further new reads will be using only the newly created files out of compaction.    * These compacted files will be deleted/cleared once all the existing readers on these    * compacted files are done.    * @return the list of compacted files    */
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getCompactedfiles
parameter_list|()
function_decl|;
comment|/**    * Returns the number of files currently in use.    * @return The number of files.    */
name|int
name|getStorefileCount
parameter_list|()
function_decl|;
comment|/**    * Gets the store files to scan for a Scan or Get request.    * @param isGet Whether it's a get.    * @param startRow Start row of the request.    * @param stopRow Stop row of the request.    * @return The list of files that are to be read for this request.    */
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getFilesForScanOrGet
parameter_list|(
name|boolean
name|isGet
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|)
function_decl|;
comment|/**    * Gets initial, full list of candidate store files to check for row-key-before.    * @param targetKey The key that is the basis of the search.    * @return The files that may have the key less than or equal to targetKey, in reverse    *         order of new-ness, and preference for target key.    */
name|Iterator
argument_list|<
name|StoreFile
argument_list|>
name|getCandidateFilesForRowKeyBefore
parameter_list|(
name|KeyValue
name|targetKey
parameter_list|)
function_decl|;
comment|/**    * Updates the candidate list for finding row key before. Based on the list of candidates    * remaining to check from getCandidateFilesForRowKeyBefore, targetKey and current candidate,    * may trim and reorder the list to remove the files where a better candidate cannot be found.    * @param candidateFiles The candidate files not yet checked for better candidates - return    *                       value from {@link #getCandidateFilesForRowKeyBefore(KeyValue)},    *                       with some files already removed.    * @param targetKey The key to search for.    * @param candidate The current best candidate found.    * @return The list to replace candidateFiles.    */
name|Iterator
argument_list|<
name|StoreFile
argument_list|>
name|updateCandidateFilesForRowKeyBefore
parameter_list|(
name|Iterator
argument_list|<
name|StoreFile
argument_list|>
name|candidateFiles
parameter_list|,
name|KeyValue
name|targetKey
parameter_list|,
name|Cell
name|candidate
parameter_list|)
function_decl|;
comment|/**    * Gets the split point for the split of this set of store files (approx. middle).    * @return The mid-point, or null if no split is possible.    * @throws IOException    */
name|byte
index|[]
name|getSplitPoint
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return The store compaction priority.    */
name|int
name|getStoreCompactionPriority
parameter_list|()
function_decl|;
comment|/**    * @param maxTs Maximum expired timestamp.    * @param filesCompacting Files that are currently compacting.    * @return The files which don't have any necessary data according to TTL and other criteria.    */
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getUnneededFiles
parameter_list|(
name|long
name|maxTs
parameter_list|,
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
function_decl|;
comment|/**    * @return the compaction pressure used for compaction throughput tuning.    * @see Store#getCompactionPressure()    */
name|double
name|getCompactionPressure
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

