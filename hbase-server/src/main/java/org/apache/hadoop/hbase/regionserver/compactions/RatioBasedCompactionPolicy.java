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
name|HStore
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
name|HStoreFile
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
name|util
operator|.
name|DNS
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|SortedCompactionPolicy
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
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
comment|/*    * @param filesToCompact Files to compact. Can be null.    * @return True if we should run a major compaction.    */
annotation|@
name|Override
specifier|public
name|boolean
name|shouldPerformMajorCompaction
parameter_list|(
name|Collection
argument_list|<
name|HStoreFile
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
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|lowTimestamp
operator|>
literal|0L
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
name|String
name|regionInfo
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|storeConfigInfo
operator|!=
literal|null
operator|&&
name|this
operator|.
name|storeConfigInfo
operator|instanceof
name|HStore
condition|)
block|{
name|regionInfo
operator|=
operator|(
operator|(
name|HStore
operator|)
name|this
operator|.
name|storeConfigInfo
operator|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|regionInfo
operator|=
name|this
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
comment|// Major compaction time has elapsed.
name|long
name|cfTTL
init|=
name|HConstants
operator|.
name|FOREVER
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|storeConfigInfo
operator|!=
literal|null
condition|)
block|{
name|cfTTL
operator|=
name|this
operator|.
name|storeConfigInfo
operator|.
name|getStoreFileTtl
argument_list|()
expr_stmt|;
block|}
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
name|HStoreFile
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
name|OptionalLong
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
name|minTimestamp
operator|.
name|isPresent
argument_list|()
condition|?
name|now
operator|-
name|minTimestamp
operator|.
name|getAsLong
argument_list|()
else|:
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
if|if
condition|(
name|sf
operator|.
name|isMajorCompactionResult
argument_list|()
operator|&&
operator|(
name|cfTTL
operator|==
name|Long
operator|.
name|MAX_VALUE
operator|||
name|oldest
operator|<
name|cfTTL
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
name|DNS
operator|.
name|getHostname
argument_list|(
name|comConf
operator|.
name|conf
argument_list|,
name|DNS
operator|.
name|ServerType
operator|.
name|REGIONSERVER
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Major compaction triggered on only store "
operator|+
name|regionInfo
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
name|result
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping major compaction of "
operator|+
name|regionInfo
operator|+
literal|" because one (major) compacted file only, oldestTime "
operator|+
name|oldest
operator|+
literal|"ms is< TTL="
operator|+
name|cfTTL
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
elseif|else
if|if
condition|(
name|cfTTL
operator|!=
name|HConstants
operator|.
name|FOREVER
operator|&&
name|oldest
operator|>
name|cfTTL
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Major compaction triggered on store "
operator|+
name|regionInfo
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Major compaction triggered on store "
operator|+
name|regionInfo
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
annotation|@
name|Override
specifier|protected
name|CompactionRequestImpl
name|createCompactionRequest
parameter_list|(
name|ArrayList
argument_list|<
name|HStoreFile
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
block|{
if|if
condition|(
operator|!
name|tryingMajor
condition|)
block|{
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
argument_list|,
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|CompactionRequestImpl
argument_list|(
name|candidateSelection
argument_list|)
return|;
block|}
comment|/**     * -- Default minor compaction selection algorithm:     * choose CompactSelection from candidates --     * First exclude bulk-load files if indicated in configuration.     * Start at the oldest file and stop when you find the first file that     * meets compaction criteria:     * (1) a recently-flushed, small file (i.e.<= minCompactSize)     * OR     * (2) within the compactRatio of sum(newer_files)     * Given normal skew, any newer files will also meet this criteria     *<p/>     * Additional Note:     * If fileSizes.size()>> maxFilesToCompact, we will recurse on     * compact().  Consider the oldest files first to avoid a     * situation where we always compact [end-threshold,end).  Then, the     * last file becomes an aggregate of the previous compactions.     *     * normal skew:     *     *         older ----> newer (increasing seqID)     *     _     *    | |   _     *    | |  | |   _     *  --|-|- |-|- |-|---_-------_-------  minCompactSize     *    | |  | |  | |  | |  _  | |     *    | |  | |  | |  | | | | | |     *    | |  | |  | |  | | | | | |     * @param candidates pre-filtrate     * @return filtered subset     */
specifier|protected
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|applyCompactionPolicy
parameter_list|(
name|ArrayList
argument_list|<
name|HStoreFile
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
name|HStoreFile
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
comment|/**    * A heuristic method to decide whether to schedule a compaction request    * @param storeFiles files in the store.    * @param filesCompacting files being scheduled to compact.    * @return true to schedule a request.    */
annotation|@
name|Override
specifier|public
name|boolean
name|needsCompaction
parameter_list|(
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|storeFiles
parameter_list|,
name|List
argument_list|<
name|HStoreFile
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
comment|/**    * Overwrite min threshold for compaction    */
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

