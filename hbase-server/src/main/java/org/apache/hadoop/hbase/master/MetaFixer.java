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
name|master
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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|MetaTableAccessor
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
name|RegionInfoBuilder
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
name|MergeRegionException
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
name|master
operator|.
name|assignment
operator|.
name|TransitRegionStateProcedure
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
name|Bytes
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

begin_comment
comment|/**  * Server-side fixing of bad or inconsistent state in hbase:meta.  * Distinct from MetaTableAccessor because {@link MetaTableAccessor} is about low-level  * manipulations driven by the Master. This class MetaFixer is  * employed by the Master and it 'knows' about holes and orphans  * and encapsulates their fixing on behalf of the Master.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|MetaFixer
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
name|MetaFixer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MAX_MERGE_COUNT_KEY
init|=
literal|"hbase.master.metafixer.max.merge.count"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_MERGE_COUNT_DEFAULT
init|=
literal|10
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|masterServices
decl_stmt|;
comment|/**    * Maximum for many regions to merge at a time.    */
specifier|private
specifier|final
name|int
name|maxMergeCount
decl_stmt|;
name|MetaFixer
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|this
operator|.
name|masterServices
operator|=
name|masterServices
expr_stmt|;
name|this
operator|.
name|maxMergeCount
operator|=
name|this
operator|.
name|masterServices
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|MAX_MERGE_COUNT_KEY
argument_list|,
name|MAX_MERGE_COUNT_DEFAULT
argument_list|)
expr_stmt|;
block|}
name|void
name|fix
parameter_list|()
throws|throws
name|IOException
block|{
name|CatalogJanitor
operator|.
name|Report
name|report
init|=
name|this
operator|.
name|masterServices
operator|.
name|getCatalogJanitor
argument_list|()
operator|.
name|getLastReport
argument_list|()
decl_stmt|;
if|if
condition|(
name|report
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"CatalogJanitor has not generated a report yet; run 'catalogjanitor_run' in "
operator|+
literal|"shell or wait until CatalogJanitor chore runs."
argument_list|)
expr_stmt|;
return|return;
block|}
name|fixHoles
argument_list|(
name|report
argument_list|)
expr_stmt|;
name|fixOverlaps
argument_list|(
name|report
argument_list|)
expr_stmt|;
comment|// Run the ReplicationBarrierCleaner here; it may clear out rep_barrier rows which
comment|// can help cleaning up damaged hbase:meta.
name|this
operator|.
name|masterServices
operator|.
name|runReplicationBarrierCleaner
argument_list|()
expr_stmt|;
block|}
comment|/**    * If hole, it papers it over by adding a region in the filesystem and to hbase:meta.    * Does not assign.    */
name|void
name|fixHoles
parameter_list|(
name|CatalogJanitor
operator|.
name|Report
name|report
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
argument_list|>
name|holes
init|=
name|report
operator|.
name|getHoles
argument_list|()
decl_stmt|;
if|if
condition|(
name|holes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"CatalogJanitor Report contains no holes to fix. Skipping."
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Identified {} region holes to fix. Detailed fixup progress logged at DEBUG."
argument_list|,
name|holes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegionInfos
init|=
name|createRegionInfosForHoles
argument_list|(
name|holes
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newMetaEntries
init|=
name|createMetaEntries
argument_list|(
name|masterServices
argument_list|,
name|newRegionInfos
argument_list|)
decl_stmt|;
specifier|final
name|TransitRegionStateProcedure
index|[]
name|assignProcedures
init|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createRoundRobinAssignProcedures
argument_list|(
name|newMetaEntries
argument_list|)
decl_stmt|;
name|masterServices
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedures
argument_list|(
name|assignProcedures
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Scheduled {}/{} new regions for assignment."
argument_list|,
name|assignProcedures
operator|.
name|length
argument_list|,
name|holes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new {@link RegionInfo} corresponding to each provided "hole" pair.    */
specifier|private
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createRegionInfosForHoles
parameter_list|(
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
argument_list|>
name|holes
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegionInfos
init|=
name|holes
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|MetaFixer
operator|::
name|getHoleCover
argument_list|)
operator|.
name|filter
argument_list|(
name|Optional
operator|::
name|isPresent
argument_list|)
operator|.
name|map
argument_list|(
name|Optional
operator|::
name|get
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Constructed {}/{} RegionInfo descriptors corresponding to identified holes."
argument_list|,
name|newRegionInfos
operator|.
name|size
argument_list|()
argument_list|,
name|holes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newRegionInfos
return|;
block|}
comment|/**    * @return Attempts to calculate a new {@link RegionInfo} that covers the region range described    *   in {@code hole}.    */
specifier|private
specifier|static
name|Optional
argument_list|<
name|RegionInfo
argument_list|>
name|getHoleCover
parameter_list|(
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
name|hole
parameter_list|)
block|{
specifier|final
name|RegionInfo
name|left
init|=
name|hole
operator|.
name|getFirst
argument_list|()
decl_stmt|;
specifier|final
name|RegionInfo
name|right
init|=
name|hole
operator|.
name|getSecond
argument_list|()
decl_stmt|;
if|if
condition|(
name|left
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|right
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
block|{
comment|// Simple case.
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|right
operator|.
name|getStartKey
argument_list|()
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping hole fix; left-side endKey is not less than right-side startKey;"
operator|+
literal|" left=<{}>, right=<{}>"
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
return|return
name|Optional
operator|.
name|of
argument_list|(
name|buildRegionInfo
argument_list|(
name|left
operator|.
name|getTable
argument_list|()
argument_list|,
name|left
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|right
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|final
name|boolean
name|leftUndefined
init|=
name|left
operator|.
name|equals
argument_list|(
name|RegionInfo
operator|.
name|UNDEFINED
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|rightUndefined
init|=
name|right
operator|.
name|equals
argument_list|(
name|RegionInfo
operator|.
name|UNDEFINED
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|last
init|=
name|left
operator|.
name|isLast
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|first
init|=
name|right
operator|.
name|isFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|leftUndefined
operator|&&
name|rightUndefined
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping hole fix; both the hole left-side and right-side RegionInfos are "
operator|+
literal|"UNDEFINED; left=<{}>, right=<{}>"
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
if|if
condition|(
name|leftUndefined
operator|||
name|last
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|buildRegionInfo
argument_list|(
name|right
operator|.
name|getTable
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|right
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|rightUndefined
operator|||
name|first
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|buildRegionInfo
argument_list|(
name|left
operator|.
name|getTable
argument_list|()
argument_list|,
name|left
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
argument_list|)
return|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping hole fix; don't know what to do with left=<{}>, right=<{}>"
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|RegionInfo
name|buildRegionInfo
parameter_list|(
name|TableName
name|tn
parameter_list|,
name|byte
index|[]
name|start
parameter_list|,
name|byte
index|[]
name|end
parameter_list|)
block|{
return|return
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|start
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|end
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Create entries in the {@code hbase:meta} for each provided {@link RegionInfo}. Best effort.    * @param masterServices used to connect to {@code hbase:meta}    * @param newRegionInfos the new {@link RegionInfo} entries to add to the filesystem    * @return a list of {@link RegionInfo} entries for which {@code hbase:meta} entries were    *   successfully created    */
specifier|private
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createMetaEntries
parameter_list|(
specifier|final
name|MasterServices
name|masterServices
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegionInfos
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|Either
argument_list|<
name|RegionInfo
argument_list|,
name|IOException
argument_list|>
argument_list|>
name|addMetaEntriesResults
init|=
name|newRegionInfos
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|regionInfo
lambda|->
block|{
try|try
block|{
name|MetaTableAccessor
operator|.
name|addRegionToMeta
argument_list|(
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|updateRegionState
argument_list|(
name|regionInfo
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
return|return
name|Either
operator|.
expr|<
name|RegionInfo
operator|,
name|IOException
operator|>
name|ofLeft
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
name|Either
operator|.
expr|<
name|RegionInfo
operator|,
name|IOException
operator|>
name|ofRight
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createMetaEntriesSuccesses
init|=
name|addMetaEntriesResults
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|Either
operator|::
name|hasLeft
argument_list|)
operator|.
name|map
argument_list|(
name|Either
operator|::
name|getLeft
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|IOException
argument_list|>
name|createMetaEntriesFailures
init|=
name|addMetaEntriesResults
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|Either
operator|::
name|hasRight
argument_list|)
operator|.
name|map
argument_list|(
name|Either
operator|::
name|getRight
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Added {}/{} entries to hbase:meta"
argument_list|,
name|createMetaEntriesSuccesses
operator|.
name|size
argument_list|()
argument_list|,
name|newRegionInfos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|createMetaEntriesFailures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to create entries in hbase:meta for {}/{} RegionInfo descriptors. First"
operator|+
literal|" failure message included; full list of failures with accompanying stack traces is"
operator|+
literal|" available at log level DEBUG. message={}"
argument_list|,
name|createMetaEntriesFailures
operator|.
name|size
argument_list|()
argument_list|,
name|addMetaEntriesResults
operator|.
name|size
argument_list|()
argument_list|,
name|createMetaEntriesFailures
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|createMetaEntriesFailures
operator|.
name|forEach
argument_list|(
name|ioe
lambda|->
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempt to fix region hole in hbase:meta failed."
argument_list|,
name|ioe
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|createMetaEntriesSuccesses
return|;
block|}
comment|/**    * Fix overlaps noted in CJ consistency report.    */
name|void
name|fixOverlaps
parameter_list|(
name|CatalogJanitor
operator|.
name|Report
name|report
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Set
argument_list|<
name|RegionInfo
argument_list|>
name|regions
range|:
name|calculateMerges
argument_list|(
name|maxMergeCount
argument_list|,
name|report
operator|.
name|getOverlaps
argument_list|()
argument_list|)
control|)
block|{
name|RegionInfo
index|[]
name|regionsArray
init|=
name|regions
operator|.
name|toArray
argument_list|(
operator|new
name|RegionInfo
index|[]
block|{}
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|masterServices
operator|.
name|mergeRegions
argument_list|(
name|regionsArray
argument_list|,
literal|false
argument_list|,
name|HConstants
operator|.
name|NO_NONCE
argument_list|,
name|HConstants
operator|.
name|NO_NONCE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MergeRegionException
name|mre
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed overlap fix of {}"
argument_list|,
name|regionsArray
argument_list|,
name|mre
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Run through<code>overlaps</code> and return a list of merges to run.    * Presumes overlaps are ordered (which they are coming out of the CatalogJanitor    * consistency report).    * @param maxMergeCount Maximum regions to merge at a time (avoid merging    *   100k regions in one go!)    */
annotation|@
name|VisibleForTesting
specifier|static
name|List
argument_list|<
name|SortedSet
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|calculateMerges
parameter_list|(
name|int
name|maxMergeCount
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
argument_list|>
name|overlaps
parameter_list|)
block|{
if|if
condition|(
name|overlaps
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No overlaps."
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|SortedSet
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|merges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|SortedSet
argument_list|<
name|RegionInfo
argument_list|>
name|currentMergeSet
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|regionInfoWithlargestEndKey
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
name|pair
range|:
name|overlaps
control|)
block|{
if|if
condition|(
name|regionInfoWithlargestEndKey
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|isOverlap
argument_list|(
name|regionInfoWithlargestEndKey
argument_list|,
name|pair
argument_list|)
operator|||
name|currentMergeSet
operator|.
name|size
argument_list|()
operator|>=
name|maxMergeCount
condition|)
block|{
name|merges
operator|.
name|add
argument_list|(
name|currentMergeSet
argument_list|)
expr_stmt|;
name|currentMergeSet
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
block|}
name|currentMergeSet
operator|.
name|add
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|currentMergeSet
operator|.
name|add
argument_list|(
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|regionInfoWithlargestEndKey
operator|=
name|getRegionInfoWithLargestEndKey
argument_list|(
name|getRegionInfoWithLargestEndKey
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|,
name|regionInfoWithlargestEndKey
argument_list|)
expr_stmt|;
block|}
name|merges
operator|.
name|add
argument_list|(
name|currentMergeSet
argument_list|)
expr_stmt|;
return|return
name|merges
return|;
block|}
comment|/**    * @return Either<code>a</code> or<code>b</code>, whichever has the    *   endkey that is furthest along in the Table.    */
annotation|@
name|VisibleForTesting
specifier|static
name|RegionInfo
name|getRegionInfoWithLargestEndKey
parameter_list|(
name|RegionInfo
name|a
parameter_list|,
name|RegionInfo
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
comment|// b may be null.
return|return
name|b
return|;
block|}
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
comment|// Both are null. The return is not-defined.
return|return
name|a
return|;
block|}
if|if
condition|(
operator|!
name|a
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|b
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
block|{
comment|// This is an odd one. This should be the right answer.
return|return
name|b
return|;
block|}
if|if
condition|(
name|a
operator|.
name|isLast
argument_list|()
condition|)
block|{
return|return
name|a
return|;
block|}
if|if
condition|(
name|b
operator|.
name|isLast
argument_list|()
condition|)
block|{
return|return
name|b
return|;
block|}
name|int
name|compare
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|a
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|b
operator|.
name|getEndKey
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|compare
operator|==
literal|0
operator|||
name|compare
operator|>
literal|0
condition|?
name|a
else|:
name|b
return|;
block|}
comment|/**    * @return True if an overlap found between passed in<code>ri</code> and    *   the<code>pair</code>. Does NOT check the pairs themselves overlap.    */
annotation|@
name|VisibleForTesting
specifier|static
name|boolean
name|isOverlap
parameter_list|(
name|RegionInfo
name|ri
parameter_list|,
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|RegionInfo
argument_list|>
name|pair
parameter_list|)
block|{
if|if
condition|(
name|ri
operator|==
literal|null
operator|||
name|pair
operator|==
literal|null
condition|)
block|{
comment|// Can't be an overlap in either of these cases.
return|return
literal|false
return|;
block|}
return|return
name|ri
operator|.
name|isOverlap
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|)
operator|||
name|ri
operator|.
name|isOverlap
argument_list|(
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * A union over {@link L} and {@link R}.    */
specifier|private
specifier|static
class|class
name|Either
parameter_list|<
name|L
parameter_list|,
name|R
parameter_list|>
block|{
specifier|private
specifier|final
name|L
name|left
decl_stmt|;
specifier|private
specifier|final
name|R
name|right
decl_stmt|;
specifier|public
specifier|static
parameter_list|<
name|L
parameter_list|,
name|R
parameter_list|>
name|Either
argument_list|<
name|L
argument_list|,
name|R
argument_list|>
name|ofLeft
parameter_list|(
name|L
name|left
parameter_list|)
block|{
return|return
operator|new
name|Either
argument_list|<>
argument_list|(
name|left
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|L
parameter_list|,
name|R
parameter_list|>
name|Either
argument_list|<
name|L
argument_list|,
name|R
argument_list|>
name|ofRight
parameter_list|(
name|R
name|right
parameter_list|)
block|{
return|return
operator|new
name|Either
argument_list|<>
argument_list|(
literal|null
argument_list|,
name|right
argument_list|)
return|;
block|}
name|Either
parameter_list|(
name|L
name|left
parameter_list|,
name|R
name|right
parameter_list|)
block|{
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasLeft
parameter_list|()
block|{
return|return
name|left
operator|!=
literal|null
return|;
block|}
specifier|public
name|L
name|getLeft
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasLeft
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Either contains no left."
argument_list|)
throw|;
block|}
return|return
name|left
return|;
block|}
specifier|public
name|boolean
name|hasRight
parameter_list|()
block|{
return|return
name|right
operator|!=
literal|null
return|;
block|}
specifier|public
name|R
name|getRight
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasRight
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Either contains no right."
argument_list|)
throw|;
block|}
return|return
name|right
return|;
block|}
block|}
block|}
end_class

end_unit

