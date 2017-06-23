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
name|Comparator
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
name|java
operator|.
name|util
operator|.
name|Optional
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
name|compactions
operator|.
name|CompactionConfiguration
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
name|ImmutableList
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
name|Lists
import|;
end_import

begin_comment
comment|/**  * Default implementation of StoreFileManager. Not thread-safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|DefaultStoreFileManager
implements|implements
name|StoreFileManager
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
name|DefaultStoreFileManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|CellComparator
name|kvComparator
decl_stmt|;
specifier|private
specifier|final
name|CompactionConfiguration
name|comConf
decl_stmt|;
specifier|private
specifier|final
name|int
name|blockingFileCount
decl_stmt|;
specifier|private
specifier|final
name|Comparator
argument_list|<
name|StoreFile
argument_list|>
name|storeFileComparator
decl_stmt|;
comment|/**    * List of store files inside this store. This is an immutable list that    * is atomically replaced when its contents change.    */
specifier|private
specifier|volatile
name|ImmutableList
argument_list|<
name|StoreFile
argument_list|>
name|storefiles
init|=
literal|null
decl_stmt|;
comment|/**    * List of compacted files inside this store that needs to be excluded in reads    * because further new reads will be using only the newly created files out of compaction.    * These compacted files will be deleted/cleared once all the existing readers on these    * compacted files are done.    */
specifier|private
specifier|volatile
name|List
argument_list|<
name|StoreFile
argument_list|>
name|compactedfiles
init|=
literal|null
decl_stmt|;
specifier|public
name|DefaultStoreFileManager
parameter_list|(
name|CellComparator
name|kvComparator
parameter_list|,
name|Comparator
argument_list|<
name|StoreFile
argument_list|>
name|storeFileComparator
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|CompactionConfiguration
name|comConf
parameter_list|)
block|{
name|this
operator|.
name|kvComparator
operator|=
name|kvComparator
expr_stmt|;
name|this
operator|.
name|storeFileComparator
operator|=
name|storeFileComparator
expr_stmt|;
name|this
operator|.
name|comConf
operator|=
name|comConf
expr_stmt|;
name|this
operator|.
name|blockingFileCount
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HStore
operator|.
name|BLOCKING_STOREFILES_KEY
argument_list|,
name|HStore
operator|.
name|DEFAULT_BLOCKING_STOREFILE_COUNT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|loadFiles
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
parameter_list|)
block|{
name|sortAndSetStoreFiles
argument_list|(
name|storeFiles
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getStorefiles
parameter_list|()
block|{
comment|// TODO: I can return a null list of StoreFiles? That'll mess up clients. St.Ack 20151111
return|return
name|storefiles
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getCompactedfiles
parameter_list|()
block|{
return|return
name|compactedfiles
return|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|newFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|storefiles
argument_list|)
decl_stmt|;
name|newFiles
operator|.
name|addAll
argument_list|(
name|sfs
argument_list|)
expr_stmt|;
name|sortAndSetStoreFiles
argument_list|(
name|newFiles
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ImmutableCollection
argument_list|<
name|StoreFile
argument_list|>
name|clearFiles
parameter_list|()
block|{
name|ImmutableList
argument_list|<
name|StoreFile
argument_list|>
name|result
init|=
name|storefiles
decl_stmt|;
name|storefiles
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|clearCompactedFiles
parameter_list|()
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|result
init|=
name|compactedfiles
decl_stmt|;
name|compactedfiles
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|getStorefileCount
parameter_list|()
block|{
return|return
name|storefiles
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|getCompactedFilesCount
parameter_list|()
block|{
if|if
condition|(
name|compactedfiles
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|compactedfiles
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addCompactionResults
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|newCompactedfiles
parameter_list|,
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|results
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|newStoreFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|storefiles
argument_list|)
decl_stmt|;
name|newStoreFiles
operator|.
name|removeAll
argument_list|(
name|newCompactedfiles
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|newStoreFiles
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
name|sortAndSetStoreFiles
argument_list|(
name|newStoreFiles
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|updatedCompactedfiles
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|compactedfiles
operator|!=
literal|null
condition|)
block|{
name|updatedCompactedfiles
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|this
operator|.
name|compactedfiles
argument_list|)
expr_stmt|;
name|updatedCompactedfiles
operator|.
name|addAll
argument_list|(
name|newCompactedfiles
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updatedCompactedfiles
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|newCompactedfiles
argument_list|)
expr_stmt|;
block|}
name|markCompactedAway
argument_list|(
name|newCompactedfiles
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactedfiles
operator|=
name|sortCompactedfiles
argument_list|(
name|updatedCompactedfiles
argument_list|)
expr_stmt|;
block|}
comment|// Mark the files as compactedAway once the storefiles and compactedfiles list is finalized
comment|// Let a background thread close the actual reader on these compacted files and also
comment|// ensure to evict the blocks from block cache so that they are no longer in
comment|// cache
specifier|private
name|void
name|markCompactedAway
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|compactedFiles
parameter_list|)
block|{
for|for
control|(
name|StoreFile
name|file
range|:
name|compactedFiles
control|)
block|{
name|file
operator|.
name|markCompactedAway
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeCompactedFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|removedCompactedfiles
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|updatedCompactedfiles
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|compactedfiles
operator|!=
literal|null
condition|)
block|{
name|updatedCompactedfiles
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|this
operator|.
name|compactedfiles
argument_list|)
expr_stmt|;
name|updatedCompactedfiles
operator|.
name|removeAll
argument_list|(
name|removedCompactedfiles
argument_list|)
expr_stmt|;
name|this
operator|.
name|compactedfiles
operator|=
name|sortCompactedfiles
argument_list|(
name|updatedCompactedfiles
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Iterator
argument_list|<
name|StoreFile
argument_list|>
name|getCandidateFilesForRowKeyBefore
parameter_list|(
specifier|final
name|KeyValue
name|targetKey
parameter_list|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Lists
operator|.
name|reverse
argument_list|(
name|this
operator|.
name|storefiles
argument_list|)
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
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
specifier|final
name|KeyValue
name|targetKey
parameter_list|,
specifier|final
name|Cell
name|candidate
parameter_list|)
block|{
comment|// Default store has nothing useful to do here.
comment|// TODO: move this comment when implementing Level:
comment|// Level store can trim the list by range, removing all the files which cannot have
comment|// any useful candidates less than "candidate".
return|return
name|candidateFiles
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|byte
index|[]
name|getSplitPoint
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storefiles
init|=
name|this
operator|.
name|storefiles
decl_stmt|;
if|if
condition|(
name|storefiles
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Optional
argument_list|<
name|StoreFile
argument_list|>
name|largestFile
init|=
name|StoreUtils
operator|.
name|getLargestFile
argument_list|(
name|storefiles
argument_list|)
decl_stmt|;
return|return
name|largestFile
operator|.
name|isPresent
argument_list|()
condition|?
name|StoreUtils
operator|.
name|getFileSplitPoint
argument_list|(
name|largestFile
operator|.
name|get
argument_list|()
argument_list|,
name|kvComparator
argument_list|)
operator|.
name|orElse
argument_list|(
literal|null
argument_list|)
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getFilesForScan
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|boolean
name|includeStartRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|,
name|boolean
name|includeStopRow
parameter_list|)
block|{
comment|// We cannot provide any useful input and already have the files sorted by seqNum.
return|return
name|getStorefiles
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getStoreCompactionPriority
parameter_list|()
block|{
name|int
name|priority
init|=
name|blockingFileCount
operator|-
name|storefiles
operator|.
name|size
argument_list|()
decl_stmt|;
return|return
operator|(
name|priority
operator|==
name|HStore
operator|.
name|PRIORITY_USER
operator|)
condition|?
name|priority
operator|+
literal|1
else|:
name|priority
return|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|expiredStoreFiles
init|=
literal|null
decl_stmt|;
name|ImmutableList
argument_list|<
name|StoreFile
argument_list|>
name|files
init|=
name|storefiles
decl_stmt|;
comment|// 1) We can never get rid of the last file which has the maximum seqid.
comment|// 2) Files that are not the latest can't become one due to (1), so the rest are fair game.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
operator|++
name|i
control|)
block|{
name|StoreFile
name|sf
init|=
name|files
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|long
name|fileTs
init|=
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|getMaxTimestamp
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileTs
operator|<
name|maxTs
operator|&&
operator|!
name|filesCompacting
operator|.
name|contains
argument_list|(
name|sf
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found an expired store file: "
operator|+
name|sf
operator|.
name|getPath
argument_list|()
operator|+
literal|" whose maxTimeStamp is "
operator|+
name|fileTs
operator|+
literal|", which is below "
operator|+
name|maxTs
argument_list|)
expr_stmt|;
if|if
condition|(
name|expiredStoreFiles
operator|==
literal|null
condition|)
block|{
name|expiredStoreFiles
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|expiredStoreFiles
operator|.
name|add
argument_list|(
name|sf
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|expiredStoreFiles
return|;
block|}
specifier|private
name|void
name|sortAndSetStoreFiles
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
parameter_list|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|storeFiles
argument_list|,
name|storeFileComparator
argument_list|)
expr_stmt|;
name|storefiles
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|storeFiles
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|StoreFile
argument_list|>
name|sortCompactedfiles
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storefiles
parameter_list|)
block|{
comment|// Sorting may not be really needed here for the compacted files?
name|Collections
operator|.
name|sort
argument_list|(
name|storefiles
argument_list|,
name|storeFileComparator
argument_list|)
expr_stmt|;
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|storefiles
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getCompactionPressure
parameter_list|()
block|{
name|int
name|storefileCount
init|=
name|getStorefileCount
argument_list|()
decl_stmt|;
name|int
name|minFilesToCompact
init|=
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
decl_stmt|;
if|if
condition|(
name|storefileCount
operator|<=
name|minFilesToCompact
condition|)
block|{
return|return
literal|0.0
return|;
block|}
return|return
call|(
name|double
call|)
argument_list|(
name|storefileCount
operator|-
name|minFilesToCompact
argument_list|)
operator|/
operator|(
name|blockingFileCount
operator|-
name|minFilesToCompact
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|StoreFile
argument_list|>
name|getStoreFileComparator
parameter_list|()
block|{
return|return
name|storeFileComparator
return|;
block|}
block|}
end_class

end_unit

