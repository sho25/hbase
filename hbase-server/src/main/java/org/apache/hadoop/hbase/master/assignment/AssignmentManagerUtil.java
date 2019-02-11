begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|assignment
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
name|ListIterator
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
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|Stream
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
name|lang3
operator|.
name|ArrayUtils
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
name|HBaseIOException
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
name|ServerName
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
name|AsyncRegionServerAdmin
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
name|RegionReplicaUtil
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
name|favored
operator|.
name|FavoredNodesManager
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|FutureUtils
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
name|WALSplitUtil
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoResponse
import|;
end_import

begin_comment
comment|/**  * Utility for this assignment package only.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|AssignmentManagerUtil
block|{
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_REGION_REPLICA
init|=
literal|1
decl_stmt|;
specifier|private
name|AssignmentManagerUtil
parameter_list|()
block|{   }
comment|/**    * Raw call to remote regionserver to get info on a particular region.    * @throws IOException Let it out so can report this IOE as reason for failure    */
specifier|static
name|GetRegionInfoResponse
name|getRegionInfoResponse
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|regionLocation
parameter_list|,
specifier|final
name|RegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRegionInfoResponse
argument_list|(
name|env
argument_list|,
name|regionLocation
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|static
name|GetRegionInfoResponse
name|getRegionInfoResponse
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|regionLocation
parameter_list|,
specifier|final
name|RegionInfo
name|hri
parameter_list|,
name|boolean
name|includeBestSplitRow
parameter_list|)
throws|throws
name|IOException
block|{
name|AsyncRegionServerAdmin
name|admin
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAsyncClusterConnection
argument_list|()
operator|.
name|getRegionServerAdmin
argument_list|(
name|regionLocation
argument_list|)
decl_stmt|;
name|GetRegionInfoRequest
name|request
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|includeBestSplitRow
condition|)
block|{
name|request
operator|=
name|RequestConverter
operator|.
name|buildGetRegionInfoRequest
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|=
name|RequestConverter
operator|.
name|buildGetRegionInfoRequest
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|FutureUtils
operator|.
name|get
argument_list|(
name|admin
operator|.
name|getRegionInfo
argument_list|(
name|request
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|lock
parameter_list|(
name|List
argument_list|<
name|RegionStateNode
argument_list|>
name|regionNodes
parameter_list|)
block|{
name|regionNodes
operator|.
name|iterator
argument_list|()
operator|.
name|forEachRemaining
argument_list|(
name|RegionStateNode
operator|::
name|lock
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|unlock
parameter_list|(
name|List
argument_list|<
name|RegionStateNode
argument_list|>
name|regionNodes
parameter_list|)
block|{
for|for
control|(
name|ListIterator
argument_list|<
name|RegionStateNode
argument_list|>
name|iter
init|=
name|regionNodes
operator|.
name|listIterator
argument_list|(
name|regionNodes
operator|.
name|size
argument_list|()
argument_list|)
init|;
name|iter
operator|.
name|hasPrevious
argument_list|()
condition|;
control|)
block|{
name|iter
operator|.
name|previous
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
name|TransitRegionStateProcedure
index|[]
name|createUnassignProceduresForSplitOrMerge
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|Stream
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
name|int
name|regionReplication
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|RegionStateNode
argument_list|>
name|regionNodes
init|=
name|regions
operator|.
name|flatMap
argument_list|(
name|hri
lambda|->
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|regionReplication
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|::
name|getOrCreateRegionStateNode
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
name|TransitRegionStateProcedure
index|[]
name|procs
init|=
operator|new
name|TransitRegionStateProcedure
index|[
name|regionNodes
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|boolean
name|rollback
init|=
literal|true
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
comment|// hold the lock at once, and then release it in finally. This is important as SCP may jump in
comment|// if we release the lock in the middle when we want to do rollback, and cause problems.
name|lock
argument_list|(
name|regionNodes
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
init|;
name|i
operator|<
name|procs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|RegionStateNode
name|regionNode
init|=
name|regionNodes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TransitRegionStateProcedure
name|proc
init|=
name|TransitRegionStateProcedure
operator|.
name|unassign
argument_list|(
name|env
argument_list|,
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionNode
operator|.
name|getProcedure
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
literal|"The parent region "
operator|+
name|regionNode
operator|+
literal|" is currently in transition, give up"
argument_list|)
throw|;
block|}
name|regionNode
operator|.
name|setProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
name|procs
index|[
name|i
index|]
operator|=
name|proc
expr_stmt|;
block|}
comment|// all succeeded, set rollback to false
name|rollback
operator|=
literal|false
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|rollback
condition|)
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
name|i
operator|--
expr_stmt|;
if|if
condition|(
name|i
operator|<
literal|0
condition|)
block|{
break|break;
block|}
name|RegionStateNode
name|regionNode
init|=
name|regionNodes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|regionNode
operator|.
name|unsetProcedure
argument_list|(
name|procs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|unlock
argument_list|(
name|regionNodes
argument_list|)
expr_stmt|;
block|}
return|return
name|procs
return|;
block|}
comment|/**    * Create assign procedures for the give regions, according to the {@code regionReplication}.    *<p/>    * For rolling back, we will submit procedures directly to the {@code ProcedureExecutor}, so it is    * possible that we persist the newly scheduled procedures, and then crash before persisting the    * rollback state, so when we arrive here the second time, it is possible that some regions have    * already been associated with a TRSP.    * @param ignoreIfInTransition if true, will skip creating TRSP for the given region if it is    *          already in transition, otherwise we will add an assert that it should not in    *          transition.    */
specifier|private
specifier|static
name|TransitRegionStateProcedure
index|[]
name|createAssignProcedures
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
name|int
name|regionReplication
parameter_list|,
name|ServerName
name|targetServer
parameter_list|,
name|boolean
name|ignoreIfInTransition
parameter_list|)
block|{
comment|// create the assign procs only for the primary region using the targetServer
name|TransitRegionStateProcedure
index|[]
name|primaryRegionProcs
init|=
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|::
name|getOrCreateRegionStateNode
argument_list|)
operator|.
name|map
argument_list|(
name|regionNode
lambda|->
block|{
name|TransitRegionStateProcedure
name|proc
init|=
name|TransitRegionStateProcedure
operator|.
name|assign
argument_list|(
name|env
argument_list|,
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|targetServer
argument_list|)
decl_stmt|;
name|regionNode
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ignoreIfInTransition
condition|)
block|{
if|if
condition|(
name|regionNode
operator|.
name|isInTransition
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
comment|// should never fail, as we have the exclusive region lock, and the region is newly
comment|// created, or has been successfully closed so should not be on any servers, so SCP
comment|// will
comment|// not process it either.
assert|assert
operator|!
name|regionNode
operator|.
name|isInTransition
argument_list|()
assert|;
block|}
name|regionNode
operator|.
name|setProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|regionNode
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|proc
return|;
block|}
argument_list|)
operator|.
name|filter
argument_list|(
name|p
lambda|->
name|p
operator|!=
literal|null
argument_list|)
operator|.
name|toArray
argument_list|(
name|TransitRegionStateProcedure
index|[]
operator|::
operator|new
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionReplication
operator|==
name|DEFAULT_REGION_REPLICA
condition|)
block|{
comment|// this is the default case
return|return
name|primaryRegionProcs
return|;
block|}
comment|// collect the replica region infos
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|replicaRegionInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|regions
operator|.
name|size
argument_list|()
operator|*
operator|(
name|regionReplication
operator|-
literal|1
operator|)
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|hri
range|:
name|regions
control|)
block|{
comment|// start the index from 1
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|regionReplication
condition|;
name|i
operator|++
control|)
block|{
name|replicaRegionInfos
operator|.
name|add
argument_list|(
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// create round robin procs. Note that we exclude the primary region's target server
name|TransitRegionStateProcedure
index|[]
name|replicaRegionAssignProcs
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createRoundRobinAssignProcedures
argument_list|(
name|replicaRegionInfos
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|targetServer
argument_list|)
argument_list|)
decl_stmt|;
comment|// combine both the procs and return the result
return|return
name|ArrayUtils
operator|.
name|addAll
argument_list|(
name|primaryRegionProcs
argument_list|,
name|replicaRegionAssignProcs
argument_list|)
return|;
block|}
specifier|static
name|TransitRegionStateProcedure
index|[]
name|createAssignProceduresForOpeningNewRegions
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
name|int
name|regionReplication
parameter_list|,
name|ServerName
name|targetServer
parameter_list|)
block|{
return|return
name|createAssignProcedures
argument_list|(
name|env
argument_list|,
name|regions
argument_list|,
name|regionReplication
argument_list|,
name|targetServer
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|static
name|void
name|reopenRegionsForRollback
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
name|int
name|regionReplication
parameter_list|,
name|ServerName
name|targetServer
parameter_list|)
block|{
name|TransitRegionStateProcedure
index|[]
name|procs
init|=
name|createAssignProcedures
argument_list|(
name|env
argument_list|,
name|regions
argument_list|,
name|regionReplication
argument_list|,
name|targetServer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|procs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedures
argument_list|(
name|procs
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|removeNonDefaultReplicas
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|Stream
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|,
name|int
name|regionReplication
parameter_list|)
block|{
comment|// Remove from in-memory states
name|regions
operator|.
name|flatMap
argument_list|(
name|hri
lambda|->
name|IntStream
operator|.
name|range
argument_list|(
literal|1
argument_list|,
name|regionReplication
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|hri
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|hri
lambda|->
block|{
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|deleteRegion
argument_list|(
name|hri
argument_list|)
argument_list|;
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|removeRegion
argument_list|(
name|hri
argument_list|)
argument_list|;
name|FavoredNodesManager
name|fnm
operator|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getFavoredNodesManager
argument_list|()
argument_list|;         if
operator|(
name|fnm
operator|!=
literal|null
operator|)
block|{
name|fnm
operator|.
name|deleteFavoredNodesForRegions
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|hri
argument_list|)
argument_list|)
block|;         }
block|}
block|)
class|;
end_class

begin_function
unit|}    static
name|void
name|checkClosedRegion
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|WALSplitUtil
operator|.
name|hasRecoveredEdits
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|,
name|regionInfo
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Recovered.edits are found in Region: "
operator|+
name|regionInfo
operator|+
literal|", abort split/merge to prevent data loss"
argument_list|)
throw|;
block|}
block|}
end_function

unit|}
end_unit

