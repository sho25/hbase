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
name|regionserver
operator|.
name|compactions
operator|.
name|CompactSelection
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
name|EnvironmentEdgeManager
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
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|GregorianCalendar
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompactionManager
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
name|CompactionManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Calendar
name|calendar
init|=
operator|new
name|GregorianCalendar
argument_list|()
decl_stmt|;
specifier|private
name|Store
name|store
decl_stmt|;
name|CompactionConfiguration
name|comConf
decl_stmt|;
name|CompactionManager
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Store
name|store
parameter_list|)
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|comConf
operator|=
operator|new
name|CompactionConfiguration
argument_list|(
name|configuration
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param candidateFiles candidate files, ordered from oldest to newest    * @return subset copy of candidate list that meets compaction criteria    * @throws java.io.IOException    */
name|CompactSelection
name|selectCompaction
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|candidateFiles
parameter_list|,
name|int
name|priority
parameter_list|,
name|boolean
name|forceMajor
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Prelimanry compaction subject to filters
name|CompactSelection
name|candidateSelection
init|=
operator|new
name|CompactSelection
argument_list|(
name|candidateFiles
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|forceMajor
condition|)
block|{
comment|// If there are expired files, only select them so that compaction deletes them
if|if
condition|(
name|comConf
operator|.
name|shouldDeleteExpired
argument_list|()
operator|&&
operator|(
name|store
operator|.
name|getTtl
argument_list|()
operator|!=
name|Long
operator|.
name|MAX_VALUE
operator|)
condition|)
block|{
name|CompactSelection
name|expiredSelection
init|=
name|selectExpiredSFs
argument_list|(
name|candidateSelection
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|store
operator|.
name|getTtl
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|expiredSelection
operator|!=
literal|null
condition|)
block|{
return|return
name|expiredSelection
return|;
block|}
block|}
name|candidateSelection
operator|=
name|skipLargeFiles
argument_list|(
name|candidateSelection
argument_list|)
expr_stmt|;
block|}
comment|// Force a major compaction if this is a user-requested major compaction,
comment|// or if we do not have too many files to compact and this was requested
comment|// as a major compaction.
comment|// Or, if there are any references among the candidates.
name|boolean
name|isUserCompaction
init|=
operator|(
name|priority
operator|==
name|Store
operator|.
name|PRIORITY_USER
operator|)
decl_stmt|;
name|boolean
name|majorCompaction
init|=
operator|(
operator|(
name|forceMajor
operator|&&
name|isUserCompaction
operator|)
operator|||
operator|(
operator|(
name|forceMajor
operator|||
name|isMajorCompaction
argument_list|(
name|candidateSelection
operator|.
name|getFilesToCompact
argument_list|()
argument_list|)
operator|)
operator|&&
operator|(
name|candidateSelection
operator|.
name|getFilesToCompact
argument_list|()
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
operator|||
name|store
operator|.
name|hasReferences
argument_list|(
name|candidateSelection
operator|.
name|getFilesToCompact
argument_list|()
argument_list|)
operator|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" - "
operator|+
name|store
operator|.
name|getColumnFamilyName
argument_list|()
operator|+
literal|": Initiating "
operator|+
operator|(
name|majorCompaction
condition|?
literal|"major"
else|:
literal|"minor"
operator|)
operator|+
literal|"compaction"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|majorCompaction
condition|)
block|{
comment|// we're doing a minor compaction, let's see what files are applicable
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
name|majorCompaction
argument_list|)
expr_stmt|;
return|return
name|candidateSelection
return|;
block|}
comment|/**    * Select the expired store files to compact    *    * @param candidates the initial set of storeFiles    * @param maxExpiredTimeStamp    *          The store file will be marked as expired if its max time stamp is    *          less than this maxExpiredTimeStamp.    * @return A CompactSelection contains the expired store files as    *         filesToCompact    */
specifier|private
name|CompactSelection
name|selectExpiredSFs
parameter_list|(
name|CompactSelection
name|candidates
parameter_list|,
name|long
name|maxExpiredTimeStamp
parameter_list|)
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
init|=
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
decl_stmt|;
if|if
condition|(
name|filesToCompact
operator|==
literal|null
operator|||
name|filesToCompact
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
return|return
literal|null
return|;
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|expiredStoreFiles
init|=
literal|null
decl_stmt|;
name|boolean
name|hasExpiredStoreFiles
init|=
literal|false
decl_stmt|;
name|CompactSelection
name|expiredSFSelection
init|=
literal|null
decl_stmt|;
for|for
control|(
name|StoreFile
name|storeFile
range|:
name|filesToCompact
control|)
block|{
if|if
condition|(
name|storeFile
operator|.
name|getReader
argument_list|()
operator|.
name|getMaxTimestamp
argument_list|()
operator|<
name|maxExpiredTimeStamp
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting the expired store file by compaction: "
operator|+
name|storeFile
operator|.
name|getPath
argument_list|()
operator|+
literal|" whose maxTimeStamp is "
operator|+
name|storeFile
operator|.
name|getReader
argument_list|()
operator|.
name|getMaxTimestamp
argument_list|()
operator|+
literal|" while the max expired timestamp is "
operator|+
name|maxExpiredTimeStamp
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hasExpiredStoreFiles
condition|)
block|{
name|expiredStoreFiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
expr_stmt|;
name|hasExpiredStoreFiles
operator|=
literal|true
expr_stmt|;
block|}
name|expiredStoreFiles
operator|.
name|add
argument_list|(
name|storeFile
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|hasExpiredStoreFiles
condition|)
block|{
name|expiredSFSelection
operator|=
operator|new
name|CompactSelection
argument_list|(
name|expiredStoreFiles
argument_list|)
expr_stmt|;
block|}
return|return
name|expiredSFSelection
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * exclude all files above maxCompactSize    * Also save all references. We MUST compact them    */
specifier|private
name|CompactSelection
name|skipLargeFiles
parameter_list|(
name|CompactSelection
name|candidates
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
name|getFilesToCompact
argument_list|()
operator|.
name|size
argument_list|()
operator|&&
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
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
argument_list|()
operator|&&
operator|!
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|isReference
argument_list|()
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
name|clearSubList
argument_list|(
literal|0
argument_list|,
name|pos
argument_list|)
expr_stmt|;
block|}
return|return
name|candidates
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * exclude all bulk load files if configured    */
specifier|private
name|CompactSelection
name|filterBulk
parameter_list|(
name|CompactSelection
name|candidates
parameter_list|)
block|{
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
operator|.
name|removeAll
argument_list|(
name|Collections2
operator|.
name|filter
argument_list|(
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
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
name|CompactSelection
name|removeExcessFiles
parameter_list|(
name|CompactSelection
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
name|getFilesToCompact
argument_list|()
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
name|clearSubList
argument_list|(
name|comConf
operator|.
name|getMaxFilesToCompact
argument_list|()
argument_list|,
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|candidates
return|;
block|}
comment|/**    * @param candidates pre-filtrate    * @return filtered subset    * forget the compactionSelection if we don't have enough files    */
specifier|private
name|CompactSelection
name|checkMinFilesCriteria
parameter_list|(
name|CompactSelection
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
name|getFilesToCompact
argument_list|()
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
name|getFilesToCompact
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" files ready for compaction.  Need "
operator|+
name|minFiles
operator|+
literal|" to initiate."
argument_list|)
expr_stmt|;
block|}
name|candidates
operator|.
name|emptyFileList
argument_list|()
expr_stmt|;
block|}
return|return
name|candidates
return|;
block|}
comment|/**     * @param candidates pre-filtrate     * @return filtered subset     * -- Default minor compaction selection algorithm: Choose CompactSelection from candidates --     * First exclude bulk-load files if indicated in configuration.     * Start at the oldest file and stop when you find the first file that     * meets compaction criteria:     * (1) a recently-flushed, small file (i.e.<= minCompactSize)     * OR     * (2) within the compactRatio of sum(newer_files)     * Given normal skew, any newer files will also meet this criteria     *<p/>     * Additional Note:     * If fileSizes.size()>> maxFilesToCompact, we will recurse on     * compact().  Consider the oldest files first to avoid a     * situation where we always compact [end-threshold,end).  Then, the     * last file becomes an aggregate of the previous compactions.     *     * normal skew:     *     *         older ----> newer (increasing seqID)     *     _     *    | |   _     *    | |  | |   _     *  --|-|- |-|- |-|---_-------_-------  minCompactSize     *    | |  | |  | |  | |  _  | |     *    | |  | |  | |  | | | | | |     *    | |  | |  | |  | | | | | |     */
name|CompactSelection
name|applyCompactionPolicy
parameter_list|(
name|CompactSelection
name|candidates
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
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
name|isOffPeakHour
argument_list|()
operator|&&
name|candidates
operator|.
name|trySetOffpeak
argument_list|()
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
operator|+
literal|", numOutstandingOffPeakCompactions is now "
operator|+
name|CompactSelection
operator|.
name|getNumOutStandingOffPeakCompactions
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// get store file sizes for incremental compacting selection.
name|int
name|countOfFiles
init|=
name|candidates
operator|.
name|getFilesToCompact
argument_list|()
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
name|getFilesToCompact
argument_list|()
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
name|candidates
operator|=
name|candidates
operator|.
name|getSubList
argument_list|(
name|start
argument_list|,
name|countOfFiles
argument_list|)
expr_stmt|;
return|return
name|candidates
return|;
block|}
comment|/*    * @param filesToCompact Files to compact. Can be null.    * @return True if we should run a major compaction.    */
name|boolean
name|isMajorCompaction
parameter_list|(
specifier|final
name|List
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
argument_list|()
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
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|oldest
init|=
operator|(
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|timeRangeTracker
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
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|timeRangeTracker
operator|.
name|minimumTimestamp
decl_stmt|;
if|if
condition|(
name|sf
operator|.
name|isMajorCompaction
argument_list|()
operator|&&
operator|(
name|store
operator|.
name|getTtl
argument_list|()
operator|==
name|HConstants
operator|.
name|FOREVER
operator|||
name|oldest
operator|<
name|store
operator|.
name|getTtl
argument_list|()
operator|)
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
literal|"Skipping major compaction of "
operator|+
name|this
operator|+
literal|" because one (major) compacted file only and oldestTime "
operator|+
name|oldest
operator|+
literal|"ms is< ttl="
operator|+
name|store
operator|.
name|getTtl
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|store
operator|.
name|getTtl
argument_list|()
operator|!=
name|HConstants
operator|.
name|FOREVER
operator|&&
name|oldest
operator|>
name|store
operator|.
name|getTtl
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
name|long
name|getNextMajorCompactTime
parameter_list|()
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
name|String
name|strCompactionTime
init|=
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|)
decl_stmt|;
if|if
condition|(
name|strCompactionTime
operator|!=
literal|null
condition|)
block|{
name|ret
operator|=
operator|(
operator|new
name|Long
argument_list|(
name|strCompactionTime
argument_list|)
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
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
name|store
operator|.
name|getDeterministicRandomSeed
argument_list|()
decl_stmt|;
if|if
condition|(
name|seed
operator|!=
literal|null
condition|)
block|{
name|double
name|rnd
init|=
operator|(
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
operator|)
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
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
comment|// no storefiles == no major compaction
block|}
block|}
block|}
return|return
name|ret
return|;
block|}
comment|/*    * Gets lowest timestamp from candidate StoreFiles    *    * @param fs    * @param dir    * @throws IOException    */
specifier|static
name|long
name|getLowestTimestamp
parameter_list|(
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|minTs
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|StoreFile
name|storeFile
range|:
name|candidates
control|)
block|{
name|minTs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minTs
argument_list|,
name|storeFile
operator|.
name|getModificationTimeStamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|minTs
return|;
block|}
comment|/**    * @param compactionSize Total size of some compaction    * @return whether this should be a large or small compaction    */
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
comment|/**    * @param numCandidates Number of candidate store files    * @return whether a compactionSelection is possible    */
name|boolean
name|needsCompaction
parameter_list|(
name|int
name|numCandidates
parameter_list|)
block|{
return|return
name|numCandidates
operator|>
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
return|;
block|}
comment|/**    * @return whether this is off-peak hour    */
specifier|private
name|boolean
name|isOffPeakHour
parameter_list|()
block|{
name|int
name|currentHour
init|=
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|)
decl_stmt|;
name|int
name|startHour
init|=
name|comConf
operator|.
name|getOffPeakStartHour
argument_list|()
decl_stmt|;
name|int
name|endHour
init|=
name|comConf
operator|.
name|getOffPeakEndHour
argument_list|()
decl_stmt|;
comment|// If offpeak time checking is disabled just return false.
if|if
condition|(
name|startHour
operator|==
name|endHour
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|startHour
operator|<
name|endHour
condition|)
block|{
return|return
operator|(
name|currentHour
operator|>=
name|startHour
operator|&&
name|currentHour
operator|<
name|endHour
operator|)
return|;
block|}
return|return
operator|(
name|currentHour
operator|>=
name|startHour
operator|||
name|currentHour
operator|<
name|endHour
operator|)
return|;
block|}
block|}
end_class

end_unit

