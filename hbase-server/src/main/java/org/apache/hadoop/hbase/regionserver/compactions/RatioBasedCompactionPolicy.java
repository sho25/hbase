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
name|Random
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
name|RSRpcServices
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
name|StoreConfigInformation
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
name|StoreUtils
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
name|base
operator|.
name|Preconditions
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
name|base
operator|.
name|Predicate
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
name|Collections2
import|;
end_import

begin_comment
comment|/**  * The default algorithm for selecting files for compaction.  * Combines the compaction configuration and the provisional file selection that  * it's given to produce the list of suitable candidates for compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RatioBasedCompactionPolicy
extends|extends
name|CompactionPolicy
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
name|RatioBasedCompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|RatioBasedCompactionPolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|storeConfigInfo
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|getCurrentEligibleFiles
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidateFiles
parameter_list|,
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
block|{
comment|// candidates = all storefiles not already in compaction queue
if|if
condition|(
operator|!
name|filesCompacting
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// exclude all files older than the newest file we're currently
comment|// compacting. this allows us to preserve contiguity (HBASE-2856)
name|StoreFile
name|last
init|=
name|filesCompacting
operator|.
name|get
argument_list|(
name|filesCompacting
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
name|candidateFiles
operator|.
name|indexOf
argument_list|(
name|last
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|idx
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
name|candidateFiles
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|idx
operator|+
literal|1
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|candidateFiles
return|;
block|}
specifier|public
name|List
argument_list|<
name|StoreFile
argument_list|>
name|preSelectCompactionForCoprocessor
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
block|{
return|return
name|getCurrentEligibleFiles
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|candidates
argument_list|)
argument_list|,
name|filesCompacting
argument_list|)
return|;
block|}
comment|/**    * @param candidateFiles candidate files, ordered from oldest to newest by seqId. We rely on    *   DefaultStoreFileManager to sort the files by seqId to guarantee contiguous compaction based    *   on seqId for data consistency.    * @return subset copy of candidate list that meets compaction criteria    * @throws java.io.IOException    */
specifier|public
name|CompactionRequest
name|selectCompaction
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|candidateFiles
parameter_list|,
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|,
specifier|final
name|boolean
name|isUserCompaction
parameter_list|,
specifier|final
name|boolean
name|mayUseOffPeak
parameter_list|,
specifier|final
name|boolean
name|forceMajor
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Preliminary compaction subject to filters
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidateSelection
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|candidateFiles
argument_list|)
decl_stmt|;
comment|// Stuck and not compacting enough (estimate). It is not guaranteed that we will be
comment|// able to compact more if stuck and compacting, because ratio policy excludes some
comment|// non-compacting files from consideration during compaction (see getCurrentEligibleFiles).
name|int
name|futureFiles
init|=
name|filesCompacting
operator|.
name|isEmpty
argument_list|()
condition|?
literal|0
else|:
literal|1
decl_stmt|;
name|boolean
name|mayBeStuck
init|=
operator|(
name|candidateFiles
operator|.
name|size
argument_list|()
operator|-
name|filesCompacting
operator|.
name|size
argument_list|()
operator|+
name|futureFiles
operator|)
operator|>=
name|storeConfigInfo
operator|.
name|getBlockingFileCount
argument_list|()
decl_stmt|;
name|candidateSelection
operator|=
name|getCurrentEligibleFiles
argument_list|(
name|candidateSelection
argument_list|,
name|filesCompacting
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Selecting compaction from "
operator|+
name|candidateFiles
operator|.
name|size
argument_list|()
operator|+
literal|" store files, "
operator|+
name|filesCompacting
operator|.
name|size
argument_list|()
operator|+
literal|" compacting, "
operator|+
name|candidateSelection
operator|.
name|size
argument_list|()
operator|+
literal|" eligible, "
operator|+
name|storeConfigInfo
operator|.
name|getBlockingFileCount
argument_list|()
operator|+
literal|" blocking"
argument_list|)
expr_stmt|;
comment|// If we can't have all files, we cannot do major anyway
name|boolean
name|isAllFiles
init|=
name|candidateFiles
operator|.
name|size
argument_list|()
operator|==
name|candidateSelection
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|forceMajor
operator|&&
name|isAllFiles
operator|)
condition|)
block|{
name|candidateSelection
operator|=
name|skipLargeFiles
argument_list|(
name|candidateSelection
argument_list|,
name|mayUseOffPeak
argument_list|)
expr_stmt|;
name|isAllFiles
operator|=
name|candidateFiles
operator|.
name|size
argument_list|()
operator|==
name|candidateSelection
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
comment|// Try a major compaction if this is a user-requested major compaction,
comment|// or if we do not have too many files to compact and this was requested as a major compaction
name|boolean
name|isTryingMajor
init|=
operator|(
name|forceMajor
operator|&&
name|isAllFiles
operator|&&
name|isUserCompaction
operator|)
operator|||
operator|(
operator|(
operator|(
name|forceMajor
operator|&&
name|isAllFiles
operator|)
operator|||
name|isMajorCompaction
argument_list|(
name|candidateSelection
argument_list|)
operator|)
operator|&&
operator|(
name|candidateSelection
operator|.
name|size
argument_list|()
operator|<
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
operator|)
operator|)
decl_stmt|;
comment|// Or, if there are any references among the candidates.
name|boolean
name|isAfterSplit
init|=
name|StoreUtils
operator|.
name|hasReferences
argument_list|(
name|candidateSelection
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isTryingMajor
operator|&&
operator|!
name|isAfterSplit
condition|)
block|{
comment|// We're are not compacting all files, let's see what files are applicable
name|candidateSelection
operator|=
name|filterBulk
argument_list|(
name|candidateSelection
argument_list|)
expr_stmt|;
name|candidateSelection
operator|=
name|applyCompactionPolicy
argument_list|(
name|candidateSelection
argument_list|,
name|mayUseOffPeak
argument_list|,
name|mayBeStuck
argument_list|)
expr_stmt|;
name|candidateSelection
operator|=
name|checkMinFilesCriteria
argument_list|(
name|candidateSelection
argument_list|)
expr_stmt|;
block|}
name|candidateSelection
operator|=
name|removeExcessFiles
argument_list|(
name|candidateSelection
argument_list|,
name|isUserCompaction
argument_list|,
name|isTryingMajor
argument_list|)
expr_stmt|;
comment|// Now we have the final file list, so we can determine if we can do major/all files.
name|isAllFiles
operator|=
operator|(
name|candidateFiles
operator|.
name|size
argument_list|()
operator|==
name|candidateSelection
operator|.
name|size
argument_list|()
operator|)
expr_stmt|;
name|CompactionRequest
name|result
init|=
operator|new
name|CompactionRequest
argument_list|(
name|candidateSelection
argument_list|)
decl_stmt|;
name|result
operator|.
name|setOffPeak
argument_list|(
operator|!
name|candidateSelection
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|isAllFiles
operator|&&
name|mayUseOffPeak
argument_list|)
expr_stmt|;
name|result
operator|.
name|setIsMajor
argument_list|(
name|isTryingMajor
operator|&&
name|isAllFiles
argument_list|,
name|isAllFiles
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * exclude all files above maxCompactSize    * Also save all references. We MUST compact them    */
specifier|protected
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|skipLargeFiles
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
name|boolean
name|mayUseOffpeak
parameter_list|)
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|pos
operator|<
name|candidates
operator|.
name|size
argument_list|()
operator|&&
operator|!
name|candidates
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|isReference
argument_list|()
operator|&&
operator|(
name|candidates
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
operator|>
name|comConf
operator|.
name|getMaxCompactSize
argument_list|(
name|mayUseOffpeak
argument_list|)
operator|)
condition|)
block|{
operator|++
name|pos
expr_stmt|;
block|}
if|if
condition|(
name|pos
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Some files are too large. Excluding "
operator|+
name|pos
operator|+
literal|" files from compaction candidates"
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|pos
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|candidates
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * exclude all bulk load files if configured    */
specifier|protected
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|filterBulk
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|)
block|{
name|candidates
operator|.
name|removeAll
argument_list|(
name|Collections2
operator|.
name|filter
argument_list|(
name|candidates
argument_list|,
operator|new
name|Predicate
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|StoreFile
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|excludeFromMinorCompaction
argument_list|()
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|candidates
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * take upto maxFilesToCompact from the start    */
specifier|private
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|removeExcessFiles
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
name|boolean
name|isUserCompaction
parameter_list|,
name|boolean
name|isMajorCompaction
parameter_list|)
block|{
name|int
name|excess
init|=
name|candidates
operator|.
name|size
argument_list|()
operator|-
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
decl_stmt|;
if|if
condition|(
name|excess
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|isMajorCompaction
operator|&&
name|isUserCompaction
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Warning, compacting more than "
operator|+
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
operator|+
literal|" files because of a user-requested major compaction"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Too many admissible files. Excluding "
operator|+
name|excess
operator|+
literal|" files from compaction candidates"
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|subList
argument_list|(
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
argument_list|,
name|candidates
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|candidates
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * forget the compactionSelection if we don't have enough files    */
specifier|protected
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|checkMinFilesCriteria
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|)
block|{
name|int
name|minFiles
init|=
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
decl_stmt|;
if|if
condition|(
name|candidates
operator|.
name|size
argument_list|()
operator|<
name|minFiles
condition|)
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
literal|"Not compacting files because we only have "
operator|+
name|candidates
operator|.
name|size
argument_list|()
operator|+
literal|" files ready for compaction. Need "
operator|+
name|minFiles
operator|+
literal|" to initiate."
argument_list|)
expr_stmt|;
block|}
name|candidates
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|candidates
return|;
block|}
comment|/**     * @param candidates pre-filtrate     * @return filtered subset     * -- Default minor compaction selection algorithm:     * choose CompactSelection from candidates --     * First exclude bulk-load files if indicated in configuration.     * Start at the oldest file and stop when you find the first file that     * meets compaction criteria:     * (1) a recently-flushed, small file (i.e.<= minCompactSize)     * OR     * (2) within the compactRatio of sum(newer_files)     * Given normal skew, any newer files will also meet this criteria     *<p/>     * Additional Note:     * If fileSizes.size()>> maxFilesToCompact, we will recurse on     * compact().  Consider the oldest files first to avoid a     * situation where we always compact [end-threshold,end).  Then, the     * last file becomes an aggregate of the previous compactions.     *     * normal skew:     *     *         older ----> newer (increasing seqID)     *     _     *    | |   _     *    | |  | |   _     *  --|-|- |-|- |-|---_-------_-------  minCompactSize     *    | |  | |  | |  | |  _  | |     *    | |  | |  | |  | | | | | |     *    | |  | |  | |  | | | | | |     */
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|applyCompactionPolicy
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
name|boolean
name|mayUseOffPeak
parameter_list|,
name|boolean
name|mayBeStuck
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|candidates
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|candidates
return|;
block|}
comment|// we're doing a minor compaction, let's see what files are applicable
name|int
name|start
init|=
literal|0
decl_stmt|;
name|double
name|ratio
init|=
name|comConf
operator|.
name|getCompactionRatio
argument_list|()
decl_stmt|;
if|if
condition|(
name|mayUseOffPeak
condition|)
block|{
name|ratio
operator|=
name|comConf
operator|.
name|getCompactionRatioOffPeak
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running an off-peak compaction, selection ratio = "
operator|+
name|ratio
argument_list|)
expr_stmt|;
block|}
comment|// get store file sizes for incremental compacting selection.
specifier|final
name|int
name|countOfFiles
init|=
name|candidates
operator|.
name|size
argument_list|()
decl_stmt|;
name|long
index|[]
name|fileSizes
init|=
operator|new
name|long
index|[
name|countOfFiles
index|]
decl_stmt|;
name|long
index|[]
name|sumSize
init|=
operator|new
name|long
index|[
name|countOfFiles
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|countOfFiles
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|StoreFile
name|file
init|=
name|candidates
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|fileSizes
index|[
name|i
index|]
operator|=
name|file
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
comment|// calculate the sum of fileSizes[i,i+maxFilesToCompact-1) for algo
name|int
name|tooFar
init|=
name|i
operator|+
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
operator|-
literal|1
decl_stmt|;
name|sumSize
index|[
name|i
index|]
operator|=
name|fileSizes
index|[
name|i
index|]
operator|+
operator|(
operator|(
name|i
operator|+
literal|1
operator|<
name|countOfFiles
operator|)
condition|?
name|sumSize
index|[
name|i
operator|+
literal|1
index|]
else|:
literal|0
operator|)
operator|-
operator|(
operator|(
name|tooFar
operator|<
name|countOfFiles
operator|)
condition|?
name|fileSizes
index|[
name|tooFar
index|]
else|:
literal|0
operator|)
expr_stmt|;
block|}
while|while
condition|(
name|countOfFiles
operator|-
name|start
operator|>=
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
operator|&&
name|fileSizes
index|[
name|start
index|]
operator|>
name|Math
operator|.
name|max
argument_list|(
name|comConf
operator|.
name|getMinCompactSize
argument_list|()
argument_list|,
call|(
name|long
call|)
argument_list|(
name|sumSize
index|[
name|start
operator|+
literal|1
index|]
operator|*
name|ratio
argument_list|)
argument_list|)
condition|)
block|{
operator|++
name|start
expr_stmt|;
block|}
if|if
condition|(
name|start
operator|<
name|countOfFiles
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Default compaction algorithm has selected "
operator|+
operator|(
name|countOfFiles
operator|-
name|start
operator|)
operator|+
literal|" files from "
operator|+
name|countOfFiles
operator|+
literal|" candidates"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mayBeStuck
condition|)
block|{
comment|// We may be stuck. Compact the latest files if we can.
name|int
name|filesToLeave
init|=
name|candidates
operator|.
name|size
argument_list|()
operator|-
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
decl_stmt|;
if|if
condition|(
name|filesToLeave
operator|>=
literal|0
condition|)
block|{
name|start
operator|=
name|filesToLeave
expr_stmt|;
block|}
block|}
name|candidates
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|start
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|candidates
return|;
block|}
comment|/*    * @param filesToCompact Files to compact. Can be null.    * @return True if we should run a major compaction.    */
annotation|@
name|Override
specifier|public
name|boolean
name|isMajorCompaction
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|long
name|mcTime
init|=
name|getNextMajorCompactTime
argument_list|(
name|filesToCompact
argument_list|)
decl_stmt|;
if|if
condition|(
name|filesToCompact
operator|==
literal|null
operator|||
name|filesToCompact
operator|.
name|isEmpty
argument_list|()
operator|||
name|mcTime
operator|==
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
comment|// TODO: Use better method for determining stamp of last major (HBASE-2990)
name|long
name|lowTimestamp
init|=
name|StoreUtils
operator|.
name|getLowestTimestamp
argument_list|(
name|filesToCompact
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|lowTimestamp
operator|>
literal|0l
operator|&&
name|lowTimestamp
operator|<
operator|(
name|now
operator|-
name|mcTime
operator|)
condition|)
block|{
comment|// Major compaction time has elapsed.
name|long
name|cfTtl
init|=
name|this
operator|.
name|storeConfigInfo
operator|.
name|getStoreFileTtl
argument_list|()
decl_stmt|;
if|if
condition|(
name|filesToCompact
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// Single file
name|StoreFile
name|sf
init|=
name|filesToCompact
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Long
name|minTimestamp
init|=
name|sf
operator|.
name|getMinimumTimestamp
argument_list|()
decl_stmt|;
name|long
name|oldest
init|=
operator|(
name|minTimestamp
operator|==
literal|null
operator|)
condition|?
name|Long
operator|.
name|MIN_VALUE
else|:
name|now
operator|-
name|minTimestamp
operator|.
name|longValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|sf
operator|.
name|isMajorCompaction
argument_list|()
operator|&&
operator|(
name|cfTtl
operator|==
name|HConstants
operator|.
name|FOREVER
operator|||
name|oldest
operator|<
name|cfTtl
operator|)
condition|)
block|{
name|float
name|blockLocalityIndex
init|=
name|sf
operator|.
name|getHDFSBlockDistribution
argument_list|()
operator|.
name|getBlockLocalityIndex
argument_list|(
name|RSRpcServices
operator|.
name|getHostname
argument_list|(
name|comConf
operator|.
name|conf
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockLocalityIndex
operator|<
name|comConf
operator|.
name|getMinLocalityToForceCompact
argument_list|()
condition|)
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
literal|"Major compaction triggered on only store "
operator|+
name|this
operator|+
literal|"; to make hdfs blocks local, current blockLocalityIndex is "
operator|+
name|blockLocalityIndex
operator|+
literal|" (min "
operator|+
name|comConf
operator|.
name|getMinLocalityToForceCompact
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
literal|true
expr_stmt|;
block|}
else|else
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
literal|"Skipping major compaction of "
operator|+
name|this
operator|+
literal|" because one (major) compacted file only, oldestTime "
operator|+
name|oldest
operator|+
literal|"ms is< ttl="
operator|+
name|cfTtl
operator|+
literal|" and blockLocalityIndex is "
operator|+
name|blockLocalityIndex
operator|+
literal|" (min "
operator|+
name|comConf
operator|.
name|getMinLocalityToForceCompact
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|cfTtl
operator|!=
name|HConstants
operator|.
name|FOREVER
operator|&&
name|oldest
operator|>
name|cfTtl
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Major compaction triggered on store "
operator|+
name|this
operator|+
literal|", because keyvalues outdated; time since last major compaction "
operator|+
operator|(
name|now
operator|-
name|lowTimestamp
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|result
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
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
literal|"Major compaction triggered on store "
operator|+
name|this
operator|+
literal|"; time since last major compaction "
operator|+
operator|(
name|now
operator|-
name|lowTimestamp
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Used calculation jitter    */
specifier|private
specifier|final
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|/**    * @param filesToCompact    * @return When to run next major compaction    */
specifier|public
name|long
name|getNextMajorCompactTime
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|)
block|{
comment|// default = 24hrs
name|long
name|ret
init|=
name|comConf
operator|.
name|getMajorCompactionPeriod
argument_list|()
decl_stmt|;
if|if
condition|(
name|ret
operator|>
literal|0
condition|)
block|{
comment|// default = 20% = +/- 4.8 hrs
name|double
name|jitterPct
init|=
name|comConf
operator|.
name|getMajorCompactionJitter
argument_list|()
decl_stmt|;
if|if
condition|(
name|jitterPct
operator|>
literal|0
condition|)
block|{
name|long
name|jitter
init|=
name|Math
operator|.
name|round
argument_list|(
name|ret
operator|*
name|jitterPct
argument_list|)
decl_stmt|;
comment|// deterministic jitter avoids a major compaction storm on restart
name|Integer
name|seed
init|=
name|StoreUtils
operator|.
name|getDeterministicRandomSeed
argument_list|(
name|filesToCompact
argument_list|)
decl_stmt|;
if|if
condition|(
name|seed
operator|!=
literal|null
condition|)
block|{
comment|// Synchronized to ensure one user of random instance at a time.
name|double
name|rnd
init|=
operator|-
literal|1
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|this
operator|.
name|random
operator|.
name|setSeed
argument_list|(
name|seed
argument_list|)
expr_stmt|;
name|rnd
operator|=
name|this
operator|.
name|random
operator|.
name|nextDouble
argument_list|()
expr_stmt|;
block|}
name|ret
operator|+=
name|jitter
operator|-
name|Math
operator|.
name|round
argument_list|(
literal|2L
operator|*
name|jitter
operator|*
name|rnd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ret
operator|=
literal|0
expr_stmt|;
comment|// If seed is null, then no storefiles == no major compaction
block|}
block|}
block|}
return|return
name|ret
return|;
block|}
comment|/**    * @param compactionSize Total size of some compaction    * @return whether this should be a large or small compaction    */
annotation|@
name|Override
specifier|public
name|boolean
name|throttleCompaction
parameter_list|(
name|long
name|compactionSize
parameter_list|)
block|{
return|return
name|compactionSize
operator|>
name|comConf
operator|.
name|getThrottlePoint
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|needsCompaction
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
parameter_list|,
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesCompacting
parameter_list|)
block|{
name|int
name|numCandidates
init|=
name|storeFiles
operator|.
name|size
argument_list|()
operator|-
name|filesCompacting
operator|.
name|size
argument_list|()
decl_stmt|;
return|return
name|numCandidates
operator|>=
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
return|;
block|}
comment|/**    * Overwrite min threshold for compaction    * @param minThreshold min to update to    */
specifier|public
name|void
name|setMinThreshold
parameter_list|(
name|int
name|minThreshold
parameter_list|)
block|{
name|comConf
operator|.
name|setMinFilesToCompact
argument_list|(
name|minThreshold
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

