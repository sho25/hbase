begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|OptionalInt
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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
name|shaded
operator|.
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
comment|/**  * An abstract compaction policy that select files on seq id order.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|SortedCompactionPolicy
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
name|SortedCompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|SortedCompactionPolicy
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
argument_list|<>
argument_list|(
name|candidates
argument_list|)
argument_list|,
name|filesCompacting
argument_list|)
return|;
block|}
comment|/**    * @param candidateFiles candidate files, ordered from oldest to newest by seqId. We rely on    *   DefaultStoreFileManager to sort the files by seqId to guarantee contiguous compaction based    *   on seqId for data consistency.    * @return subset copy of candidate list that meets compaction criteria    */
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
argument_list|<>
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
name|shouldPerformMajorCompaction
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
name|CompactionRequest
name|result
init|=
name|createCompactionRequest
argument_list|(
name|candidateSelection
argument_list|,
name|isTryingMajor
operator|||
name|isAfterSplit
argument_list|,
name|mayUseOffPeak
argument_list|,
name|mayBeStuck
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|result
operator|.
name|getFiles
argument_list|()
argument_list|)
decl_stmt|;
name|removeExcessFiles
argument_list|(
name|filesToCompact
argument_list|,
name|isUserCompaction
argument_list|,
name|isTryingMajor
argument_list|)
expr_stmt|;
name|result
operator|.
name|updateFiles
argument_list|(
name|filesToCompact
argument_list|)
expr_stmt|;
name|isAllFiles
operator|=
operator|(
name|candidateFiles
operator|.
name|size
argument_list|()
operator|==
name|filesToCompact
operator|.
name|size
argument_list|()
operator|)
expr_stmt|;
name|result
operator|.
name|setOffPeak
argument_list|(
operator|!
name|filesToCompact
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
specifier|protected
specifier|abstract
name|CompactionRequest
name|createCompactionRequest
parameter_list|(
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidateSelection
parameter_list|,
name|boolean
name|tryingMajor
parameter_list|,
name|boolean
name|mayUseOffPeak
parameter_list|,
name|boolean
name|mayBeStuck
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * @param filesToCompact Files to compact. Can be null.    * @return True if we should run a major compaction.    */
specifier|public
specifier|abstract
name|boolean
name|shouldPerformMajorCompaction
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
function_decl|;
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
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|)
block|{
comment|// default = 24hrs
name|long
name|period
init|=
name|comConf
operator|.
name|getMajorCompactionPeriod
argument_list|()
decl_stmt|;
if|if
condition|(
name|period
operator|<=
literal|0
condition|)
block|{
return|return
name|period
return|;
block|}
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
operator|<=
literal|0
condition|)
block|{
return|return
name|period
return|;
block|}
comment|// deterministic jitter avoids a major compaction storm on restart
name|OptionalInt
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
operator|.
name|isPresent
argument_list|()
condition|)
block|{
comment|// Synchronized to ensure one user of random instance at a time.
name|double
name|rnd
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
operator|.
name|getAsInt
argument_list|()
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
name|long
name|jitter
init|=
name|Math
operator|.
name|round
argument_list|(
name|period
operator|*
name|jitterPct
argument_list|)
decl_stmt|;
return|return
name|period
operator|+
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
return|;
block|}
else|else
block|{
return|return
literal|0L
return|;
block|}
block|}
comment|/**    * @param compactionSize Total size of some compaction    * @return whether this should be a large or small compaction    */
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
specifier|abstract
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
function_decl|;
specifier|protected
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
comment|/**    * @param candidates pre-filtrate    * @return filtered subset exclude all files above maxCompactSize    *   Also save all references. We MUST compact them    */
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
comment|/**    * @param candidates pre-filtrate    * @return filtered subset exclude all bulk load files if configured    */
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
comment|/**    * @param candidates pre-filtrate    */
specifier|protected
name|void
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
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset forget the compactionSelection if we don't have enough files    */
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
parameter_list|,
name|int
name|minFiles
parameter_list|)
block|{
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
block|}
end_class

end_unit

