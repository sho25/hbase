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
name|Collections
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
name|util
operator|.
name|ReflectionUtils
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|PeekingIterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|math
operator|.
name|LongMath
import|;
end_import

begin_comment
comment|/**  * HBASE-15181 This is a simple implementation of date-based tiered compaction similar to  * Cassandra's for the following benefits:  *<ol>  *<li>Improve date-range-based scan by structuring store files in date-based tiered layout.</li>  *<li>Reduce compaction overhead.</li>  *<li>Improve TTL efficiency.</li>  *</ol>  * Perfect fit for the use cases that:  *<ol>  *<li>has mostly date-based data write and scan and a focus on the most recent data.</li>  *</ol>  * Out-of-order writes are handled gracefully. Time range overlapping among store files is tolerated  * and the performance impact is minimized. Configuration can be set at hbase-site or overridden at  * per-table or per-column-family level by hbase shell. Design spec is at  * https://docs.google.com/document/d/1_AmlNb2N8Us1xICsTeGDLKIqL6T-oHoRLZ323MG_uy8/  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|DateTieredCompactionPolicy
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
name|DateTieredCompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RatioBasedCompactionPolicy
name|compactionPolicyPerWindow
decl_stmt|;
specifier|private
specifier|final
name|CompactionWindowFactory
name|windowFactory
decl_stmt|;
specifier|public
name|DateTieredCompactionPolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|StoreConfigInformation
name|storeConfigInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|storeConfigInfo
argument_list|)
expr_stmt|;
try|try
block|{
name|compactionPolicyPerWindow
operator|=
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|comConf
operator|.
name|getCompactionPolicyForDateTieredWindow
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|,
name|StoreConfigInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|conf
block|,
name|storeConfigInfo
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to load configured compaction policy '"
operator|+
name|comConf
operator|.
name|getCompactionPolicyForDateTieredWindow
argument_list|()
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|windowFactory
operator|=
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|comConf
operator|.
name|getDateTieredCompactionWindowFactory
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|CompactionConfiguration
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|comConf
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to load configured window factory '"
operator|+
name|comConf
operator|.
name|getDateTieredCompactionWindowFactory
argument_list|()
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Heuristics for guessing whether we need minor compaction.    */
annotation|@
name|Override
annotation|@
name|VisibleForTesting
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
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|storeFiles
argument_list|)
decl_stmt|;
try|try
block|{
return|return
operator|!
name|selectMinorCompaction
argument_list|(
name|candidates
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
operator|.
name|getFiles
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can not check for compaction: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
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
name|mcTime
operator|==
literal|0
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
literal|"filesToCompact: "
operator|+
name|filesToCompact
operator|+
literal|" mcTime: "
operator|+
name|mcTime
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
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
operator|<=
literal|0L
operator|||
name|lowTimestamp
operator|>=
operator|(
name|now
operator|-
name|mcTime
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
literal|"lowTimestamp: "
operator|+
name|lowTimestamp
operator|+
literal|" lowTimestamp: "
operator|+
name|lowTimestamp
operator|+
literal|" now: "
operator|+
name|now
operator|+
literal|" mcTime: "
operator|+
name|mcTime
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|long
name|cfTTL
init|=
name|this
operator|.
name|storeConfigInfo
operator|.
name|getStoreFileTtl
argument_list|()
decl_stmt|;
name|HDFSBlocksDistribution
name|hdfsBlocksDistribution
init|=
operator|new
name|HDFSBlocksDistribution
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
init|=
name|getCompactBoundariesForMajor
argument_list|(
name|filesToCompact
argument_list|,
name|now
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|filesInWindow
init|=
operator|new
name|boolean
index|[
name|boundaries
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|filesToCompact
control|)
block|{
name|OptionalLong
name|minTimestamp
init|=
name|file
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
name|cfTTL
operator|!=
name|Long
operator|.
name|MAX_VALUE
operator|&&
name|oldest
operator|>=
name|cfTTL
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
literal|"; for TTL maintenance"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|file
operator|.
name|isMajorCompactionResult
argument_list|()
operator|||
name|file
operator|.
name|isBulkLoadResult
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
literal|", because there are new files and time since last major compaction "
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
return|return
literal|true
return|;
block|}
name|int
name|lowerWindowIndex
init|=
name|Collections
operator|.
name|binarySearch
argument_list|(
name|boundaries
argument_list|,
name|minTimestamp
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|upperWindowIndex
init|=
name|Collections
operator|.
name|binarySearch
argument_list|(
name|boundaries
argument_list|,
name|file
operator|.
name|getMaximumTimestamp
argument_list|()
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
decl_stmt|;
comment|// Handle boundary conditions and negative values of binarySearch
name|lowerWindowIndex
operator|=
operator|(
name|lowerWindowIndex
operator|<
literal|0
operator|)
condition|?
name|Math
operator|.
name|abs
argument_list|(
name|lowerWindowIndex
operator|+
literal|2
argument_list|)
else|:
name|lowerWindowIndex
expr_stmt|;
name|upperWindowIndex
operator|=
operator|(
name|upperWindowIndex
operator|<
literal|0
operator|)
condition|?
name|Math
operator|.
name|abs
argument_list|(
name|upperWindowIndex
operator|+
literal|2
argument_list|)
else|:
name|upperWindowIndex
expr_stmt|;
if|if
condition|(
name|lowerWindowIndex
operator|!=
name|upperWindowIndex
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
literal|"; because file "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|+
literal|" has data with timestamps cross window boundaries"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|filesInWindow
index|[
name|upperWindowIndex
index|]
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
literal|"; because there are more than one file in some windows"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
name|filesInWindow
index|[
name|upperWindowIndex
index|]
operator|=
literal|true
expr_stmt|;
block|}
name|hdfsBlocksDistribution
operator|.
name|add
argument_list|(
name|file
operator|.
name|getHDFSBlockDistribution
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|float
name|blockLocalityIndex
init|=
name|hdfsBlocksDistribution
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
literal|"Major compaction triggered on store "
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
return|return
literal|true
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping major compaction of "
operator|+
name|this
operator|+
literal|", because the files are already major compacted"
argument_list|)
expr_stmt|;
return|return
literal|false
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
name|CompactionRequestImpl
name|result
init|=
name|tryingMajor
condition|?
name|selectMajorCompaction
argument_list|(
name|candidateSelection
argument_list|)
else|:
name|selectMinorCompaction
argument_list|(
name|candidateSelection
argument_list|,
name|mayUseOffPeak
argument_list|,
name|mayBeStuck
argument_list|)
decl_stmt|;
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
literal|"Generated compaction request: "
operator|+
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|CompactionRequestImpl
name|selectMajorCompaction
parameter_list|(
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|candidateSelection
parameter_list|)
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
return|return
operator|new
name|DateTieredCompactionRequest
argument_list|(
name|candidateSelection
argument_list|,
name|this
operator|.
name|getCompactBoundariesForMajor
argument_list|(
name|candidateSelection
argument_list|,
name|now
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * We receive store files sorted in ascending order by seqId then scan the list of files. If the    * current file has a maxTimestamp older than last known maximum, treat this file as it carries    * the last known maximum. This way both seqId and timestamp are in the same order. If files carry    * the same maxTimestamps, they are ordered by seqId. We then reverse the list so they are ordered    * by seqId and maxTimestamp in descending order and build the time windows. All the out-of-order    * data into the same compaction windows, guaranteeing contiguous compaction based on sequence id.    */
specifier|public
name|CompactionRequestImpl
name|selectMinorCompaction
parameter_list|(
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|candidateSelection
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
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|oldestToCompact
init|=
name|getOldestToCompact
argument_list|(
name|comConf
operator|.
name|getDateTieredMaxStoreFileAgeMillis
argument_list|()
argument_list|,
name|now
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|HStoreFile
argument_list|,
name|Long
argument_list|>
argument_list|>
name|storefileMaxTimestampPairs
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|candidateSelection
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|maxTimestampSeen
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
for|for
control|(
name|HStoreFile
name|storeFile
range|:
name|candidateSelection
control|)
block|{
comment|// if there is out-of-order data,
comment|// we put them in the same window as the last file in increasing order
name|maxTimestampSeen
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxTimestampSeen
argument_list|,
name|storeFile
operator|.
name|getMaximumTimestamp
argument_list|()
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|storefileMaxTimestampPairs
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|storeFile
argument_list|,
name|maxTimestampSeen
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|reverse
argument_list|(
name|storefileMaxTimestampPairs
argument_list|)
expr_stmt|;
name|CompactionWindow
name|window
init|=
name|getIncomingWindow
argument_list|(
name|now
argument_list|)
decl_stmt|;
name|int
name|minThreshold
init|=
name|comConf
operator|.
name|getDateTieredIncomingWindowMin
argument_list|()
decl_stmt|;
name|PeekingIterator
argument_list|<
name|Pair
argument_list|<
name|HStoreFile
argument_list|,
name|Long
argument_list|>
argument_list|>
name|it
init|=
name|Iterators
operator|.
name|peekingIterator
argument_list|(
name|storefileMaxTimestampPairs
operator|.
name|iterator
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
name|window
operator|.
name|compareToTimestamp
argument_list|(
name|oldestToCompact
argument_list|)
operator|<
literal|0
condition|)
block|{
break|break;
block|}
name|int
name|compResult
init|=
name|window
operator|.
name|compareToTimestamp
argument_list|(
name|it
operator|.
name|peek
argument_list|()
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compResult
operator|>
literal|0
condition|)
block|{
comment|// If the file is too old for the window, switch to the next window
name|window
operator|=
name|window
operator|.
name|nextEarlierWindow
argument_list|()
expr_stmt|;
name|minThreshold
operator|=
name|comConf
operator|.
name|getMinFilesToCompact
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// The file is within the target window
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|fileList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// Add all files in the same window. For incoming window
comment|// we tolerate files with future data although it is sub-optimal
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
operator|&&
name|window
operator|.
name|compareToTimestamp
argument_list|(
name|it
operator|.
name|peek
argument_list|()
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|<=
literal|0
condition|)
block|{
name|fileList
operator|.
name|add
argument_list|(
name|it
operator|.
name|next
argument_list|()
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fileList
operator|.
name|size
argument_list|()
operator|>=
name|minThreshold
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
literal|"Processing files: "
operator|+
name|fileList
operator|+
literal|" for window: "
operator|+
name|window
argument_list|)
expr_stmt|;
block|}
name|DateTieredCompactionRequest
name|request
init|=
name|generateCompactionRequest
argument_list|(
name|fileList
argument_list|,
name|window
argument_list|,
name|mayUseOffPeak
argument_list|,
name|mayBeStuck
argument_list|,
name|minThreshold
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|!=
literal|null
condition|)
block|{
return|return
name|request
return|;
block|}
block|}
block|}
block|}
comment|// A non-null file list is expected by HStore
return|return
operator|new
name|CompactionRequestImpl
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|DateTieredCompactionRequest
name|generateCompactionRequest
parameter_list|(
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|storeFiles
parameter_list|,
name|CompactionWindow
name|window
parameter_list|,
name|boolean
name|mayUseOffPeak
parameter_list|,
name|boolean
name|mayBeStuck
parameter_list|,
name|int
name|minThreshold
parameter_list|)
throws|throws
name|IOException
block|{
comment|// The files has to be in ascending order for ratio-based compaction to work right
comment|// and removeExcessFile to exclude youngest files.
name|Collections
operator|.
name|reverse
argument_list|(
name|storeFiles
argument_list|)
expr_stmt|;
comment|// Compact everything in the window if have more files than comConf.maxBlockingFiles
name|compactionPolicyPerWindow
operator|.
name|setMinThreshold
argument_list|(
name|minThreshold
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|HStoreFile
argument_list|>
name|storeFileSelection
init|=
name|mayBeStuck
condition|?
name|storeFiles
else|:
name|compactionPolicyPerWindow
operator|.
name|applyCompactionPolicy
argument_list|(
name|storeFiles
argument_list|,
name|mayUseOffPeak
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|storeFileSelection
operator|!=
literal|null
operator|&&
operator|!
name|storeFileSelection
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// If there is any file in the window excluded from compaction,
comment|// only one file will be output from compaction.
name|boolean
name|singleOutput
init|=
name|storeFiles
operator|.
name|size
argument_list|()
operator|!=
name|storeFileSelection
operator|.
name|size
argument_list|()
operator|||
name|comConf
operator|.
name|useDateTieredSingleOutputForMinorCompaction
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
init|=
name|getCompactionBoundariesForMinor
argument_list|(
name|window
argument_list|,
name|singleOutput
argument_list|)
decl_stmt|;
name|DateTieredCompactionRequest
name|result
init|=
operator|new
name|DateTieredCompactionRequest
argument_list|(
name|storeFileSelection
argument_list|,
name|boundaries
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Return a list of boundaries for multiple compaction output in ascending order.    */
specifier|private
name|List
argument_list|<
name|Long
argument_list|>
name|getCompactBoundariesForMajor
parameter_list|(
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|filesToCompact
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|long
name|minTimestamp
init|=
name|filesToCompact
operator|.
name|stream
argument_list|()
operator|.
name|mapToLong
argument_list|(
name|f
lambda|->
name|f
operator|.
name|getMinimumTimestamp
argument_list|()
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
operator|.
name|min
argument_list|()
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Add startMillis of all windows between now and min timestamp
for|for
control|(
name|CompactionWindow
name|window
init|=
name|getIncomingWindow
argument_list|(
name|now
argument_list|)
init|;
name|window
operator|.
name|compareToTimestamp
argument_list|(
name|minTimestamp
argument_list|)
operator|>
literal|0
condition|;
name|window
operator|=
name|window
operator|.
name|nextEarlierWindow
argument_list|()
control|)
block|{
name|boundaries
operator|.
name|add
argument_list|(
name|window
operator|.
name|startMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boundaries
operator|.
name|add
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|reverse
argument_list|(
name|boundaries
argument_list|)
expr_stmt|;
return|return
name|boundaries
return|;
block|}
comment|/**    * @return a list of boundaries for multiple compaction output from minTimestamp to maxTimestamp.    */
specifier|private
specifier|static
name|List
argument_list|<
name|Long
argument_list|>
name|getCompactionBoundariesForMinor
parameter_list|(
name|CompactionWindow
name|window
parameter_list|,
name|boolean
name|singleOutput
parameter_list|)
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boundaries
operator|.
name|add
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|singleOutput
condition|)
block|{
name|boundaries
operator|.
name|add
argument_list|(
name|window
operator|.
name|startMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|boundaries
return|;
block|}
specifier|private
name|CompactionWindow
name|getIncomingWindow
parameter_list|(
name|long
name|now
parameter_list|)
block|{
return|return
name|windowFactory
operator|.
name|newIncomingWindow
argument_list|(
name|now
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|getOldestToCompact
parameter_list|(
name|long
name|maxAgeMillis
parameter_list|,
name|long
name|now
parameter_list|)
block|{
try|try
block|{
return|return
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
name|now
argument_list|,
name|maxAgeMillis
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|ae
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Value for "
operator|+
name|CompactionConfiguration
operator|.
name|DATE_TIERED_MAX_AGE_MILLIS_KEY
operator|+
literal|": "
operator|+
name|maxAgeMillis
operator|+
literal|". All the files will be eligible for minor compaction."
argument_list|)
expr_stmt|;
return|return
name|Long
operator|.
name|MIN_VALUE
return|;
block|}
block|}
block|}
end_class

end_unit

