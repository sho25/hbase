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
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|HRegionLocation
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
name|RegionLocations
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
name|Table
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
name|TableDescriptor
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
name|MasterFileSystem
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
name|MasterServices
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
name|RegionState
operator|.
name|State
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
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|util
operator|.
name|StringUtils
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
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|MetaTableLocator
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
name|zookeeper
operator|.
name|KeeperException
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Store Region State to hbase:meta table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionStateStore
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
name|RegionStateStore
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The delimiter for meta columns for replicaIds&gt; 0 */
specifier|protected
specifier|static
specifier|final
name|char
name|META_REPLICA_ID_DELIMITER
init|=
literal|'_'
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|master
decl_stmt|;
specifier|public
name|RegionStateStore
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
block|}
specifier|public
interface|interface
name|RegionStateVisitor
block|{
name|void
name|visitRegionState
parameter_list|(
name|Result
name|result
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|State
name|state
parameter_list|,
name|ServerName
name|regionLocation
parameter_list|,
name|ServerName
name|lastHost
parameter_list|,
name|long
name|openSeqNum
parameter_list|)
function_decl|;
block|}
specifier|public
name|void
name|visitMeta
parameter_list|(
specifier|final
name|RegionStateVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|fullScanRegions
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
operator|new
name|MetaTableAccessor
operator|.
name|Visitor
argument_list|()
block|{
specifier|final
name|boolean
name|isDebugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|r
operator|!=
literal|null
operator|&&
operator|!
name|r
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|st
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|st
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
name|visitMetaEntry
argument_list|(
name|visitor
argument_list|,
name|r
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|long
name|et
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"[T] LOAD META PERF "
operator|+
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|et
operator|-
name|st
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"NULL result from meta - ignoring but this is strange."
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Queries META table for the passed region encoded name,    * delegating action upon results to the<code>RegionStateVisitor</code>    * passed as second parameter.    * @param regionEncodedName encoded name for the Region we want to query META for.    * @param visitor The<code>RegionStateVisitor</code> instance to react over the query results.    * @throws IOException If some error occurs while querying META or parsing results.    */
specifier|public
name|void
name|visitMetaForRegion
parameter_list|(
specifier|final
name|String
name|regionEncodedName
parameter_list|,
specifier|final
name|RegionStateVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|Result
name|result
init|=
name|MetaTableAccessor
operator|.
name|scanByRegionEncodedName
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regionEncodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|visitMetaEntry
argument_list|(
name|visitor
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|visitMetaEntry
parameter_list|(
specifier|final
name|RegionStateVisitor
name|visitor
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|RegionLocations
name|rl
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|rl
operator|==
literal|null
condition|)
return|return;
specifier|final
name|HRegionLocation
index|[]
name|locations
init|=
name|rl
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
if|if
condition|(
name|locations
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|HRegionLocation
name|hrl
init|=
name|locations
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|hrl
operator|==
literal|null
condition|)
continue|continue;
specifier|final
name|RegionInfo
name|regionInfo
init|=
name|hrl
operator|.
name|getRegion
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
continue|continue;
specifier|final
name|int
name|replicaId
init|=
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
specifier|final
name|State
name|state
init|=
name|getRegionState
argument_list|(
name|result
argument_list|,
name|replicaId
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
specifier|final
name|ServerName
name|lastHost
init|=
name|hrl
operator|.
name|getServerName
argument_list|()
decl_stmt|;
specifier|final
name|ServerName
name|regionLocation
init|=
name|getRegionServer
argument_list|(
name|result
argument_list|,
name|replicaId
argument_list|)
decl_stmt|;
specifier|final
name|long
name|openSeqNum
init|=
name|hrl
operator|.
name|getSeqNum
argument_list|()
decl_stmt|;
comment|// TODO: move under trace, now is visible for debugging
name|LOG
operator|.
name|info
argument_list|(
literal|"Load hbase:meta entry region={}, regionState={}, lastHost={}, "
operator|+
literal|"regionLocation={}, openSeqNum={}"
argument_list|,
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|state
argument_list|,
name|lastHost
argument_list|,
name|regionLocation
argument_list|,
name|openSeqNum
argument_list|)
expr_stmt|;
name|visitor
operator|.
name|visitRegionState
argument_list|(
name|result
argument_list|,
name|regionInfo
argument_list|,
name|state
argument_list|,
name|regionLocation
argument_list|,
name|lastHost
argument_list|,
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|updateRegionLocation
parameter_list|(
name|RegionStateNode
name|regionStateNode
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|regionStateNode
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|updateMetaLocation
argument_list|(
name|regionStateNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|regionStateNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|,
name|regionStateNode
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|openSeqNum
init|=
name|regionStateNode
operator|.
name|getState
argument_list|()
operator|==
name|State
operator|.
name|OPEN
condition|?
name|regionStateNode
operator|.
name|getOpenSeqNum
argument_list|()
else|:
name|HConstants
operator|.
name|NO_SEQNUM
decl_stmt|;
name|updateUserRegionLocation
argument_list|(
name|regionStateNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|regionStateNode
operator|.
name|getState
argument_list|()
argument_list|,
name|regionStateNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|,
name|openSeqNum
argument_list|,
comment|// The regionStateNode may have no procedure in a test scenario; allow for this.
name|regionStateNode
operator|.
name|getProcedure
argument_list|()
operator|!=
literal|null
condition|?
name|regionStateNode
operator|.
name|getProcedure
argument_list|()
operator|.
name|getProcId
argument_list|()
else|:
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|updateMetaLocation
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|State
name|state
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|serverName
argument_list|,
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|updateUserRegionLocation
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|State
name|state
parameter_list|,
name|ServerName
name|regionLocation
parameter_list|,
name|long
name|openSeqNum
parameter_list|,
name|long
name|pid
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|time
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
specifier|final
name|int
name|replicaId
init|=
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
specifier|final
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|MetaTableAccessor
operator|.
name|getMetaKeyForRegion
argument_list|(
name|regionInfo
argument_list|)
argument_list|,
name|time
argument_list|)
decl_stmt|;
name|MetaTableAccessor
operator|.
name|addRegionInfo
argument_list|(
name|put
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
specifier|final
name|StringBuilder
name|info
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"pid="
argument_list|)
operator|.
name|append
argument_list|(
name|pid
argument_list|)
operator|.
name|append
argument_list|(
literal|" updating hbase:meta row="
argument_list|)
operator|.
name|append
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", regionState="
argument_list|)
operator|.
name|append
argument_list|(
name|state
argument_list|)
decl_stmt|;
if|if
condition|(
name|openSeqNum
operator|>=
literal|0
condition|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|state
operator|==
name|State
operator|.
name|OPEN
operator|&&
name|regionLocation
operator|!=
literal|null
argument_list|,
literal|"Open region should be on a server"
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|addLocation
argument_list|(
name|put
argument_list|,
name|regionLocation
argument_list|,
name|openSeqNum
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
comment|// only update replication barrier for default replica
if|if
condition|(
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
operator|==
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
operator|&&
name|hasGlobalReplicationScope
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|)
condition|)
block|{
name|MetaTableAccessor
operator|.
name|addReplicationBarrier
argument_list|(
name|put
argument_list|,
name|openSeqNum
argument_list|)
expr_stmt|;
name|info
operator|.
name|append
argument_list|(
literal|", repBarrier="
argument_list|)
operator|.
name|append
argument_list|(
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
name|info
operator|.
name|append
argument_list|(
literal|", openSeqNum="
argument_list|)
operator|.
name|append
argument_list|(
name|openSeqNum
argument_list|)
expr_stmt|;
name|info
operator|.
name|append
argument_list|(
literal|", regionLocation="
argument_list|)
operator|.
name|append
argument_list|(
name|regionLocation
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|regionLocation
operator|!=
literal|null
condition|)
block|{
comment|// Ideally, if no regionLocation, write null to the hbase:meta but this will confuse clients
comment|// currently; they want a server to hit. TODO: Make clients wait if no location.
name|put
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|setFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|getServerNameColumn
argument_list|(
name|replicaId
argument_list|)
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|put
operator|.
name|getTimestamp
argument_list|()
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionLocation
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|info
operator|.
name|append
argument_list|(
literal|", regionLocation="
argument_list|)
operator|.
name|append
argument_list|(
name|regionLocation
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|setFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|getStateColumn
argument_list|(
name|replicaId
argument_list|)
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|put
operator|.
name|getTimestamp
argument_list|()
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|state
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|updateRegionLocation
argument_list|(
name|regionInfo
argument_list|,
name|state
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updateRegionLocation
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|State
name|state
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|table
init|=
name|master
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// TODO: Revist!!!! Means that if a server is loaded, then we will abort our host!
comment|// In tests we abort the Master!
name|String
name|msg
init|=
name|String
operator|.
name|format
argument_list|(
literal|"FAILED persisting region=%s state=%s"
argument_list|,
name|regionInfo
operator|.
name|getShortNameToLog
argument_list|()
argument_list|,
name|state
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|master
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|long
name|getOpenSeqNumForParentRegion
parameter_list|(
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterFileSystem
name|fs
init|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|long
name|maxSeqId
init|=
name|WALSplitUtil
operator|.
name|getMaxRegionSequenceId
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|region
argument_list|,
name|fs
operator|::
name|getFileSystem
argument_list|,
name|fs
operator|::
name|getWALFileSystem
argument_list|)
decl_stmt|;
return|return
name|maxSeqId
operator|>
literal|0
condition|?
name|maxSeqId
operator|+
literal|1
else|:
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
comment|// ============================================================================================
comment|//  Update Region Splitting State helpers
comment|// ============================================================================================
specifier|public
name|void
name|splitRegion
parameter_list|(
name|RegionInfo
name|parent
parameter_list|,
name|RegionInfo
name|hriA
parameter_list|,
name|RegionInfo
name|hriB
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptor
name|htd
init|=
name|getDescriptor
argument_list|(
name|parent
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|parentOpenSeqNum
init|=
name|HConstants
operator|.
name|NO_SEQNUM
decl_stmt|;
if|if
condition|(
name|htd
operator|.
name|hasGlobalReplicationScope
argument_list|()
condition|)
block|{
name|parentOpenSeqNum
operator|=
name|getOpenSeqNumForParentRegion
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
name|MetaTableAccessor
operator|.
name|splitRegion
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|parent
argument_list|,
name|parentOpenSeqNum
argument_list|,
name|hriA
argument_list|,
name|hriB
argument_list|,
name|serverName
argument_list|,
name|getRegionReplication
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// ============================================================================================
comment|//  Update Region Merging State helpers
comment|// ============================================================================================
specifier|public
name|void
name|mergeRegions
parameter_list|(
name|RegionInfo
name|child
parameter_list|,
name|RegionInfo
index|[]
name|parents
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptor
name|htd
init|=
name|getDescriptor
argument_list|(
name|child
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|globalScope
init|=
name|htd
operator|.
name|hasGlobalReplicationScope
argument_list|()
decl_stmt|;
name|SortedMap
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|parentSeqNums
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|parents
control|)
block|{
name|parentSeqNums
operator|.
name|put
argument_list|(
name|ri
argument_list|,
name|globalScope
condition|?
name|getOpenSeqNumForParentRegion
argument_list|(
name|ri
argument_list|)
else|:
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|MetaTableAccessor
operator|.
name|mergeRegions
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|child
argument_list|,
name|parentSeqNums
argument_list|,
name|serverName
argument_list|,
name|getRegionReplication
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// ============================================================================================
comment|//  Delete Region State helpers
comment|// ============================================================================================
specifier|public
name|void
name|deleteRegion
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteRegions
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deleteRegions
parameter_list|(
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|deleteRegionInfos
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Table Descriptors helpers
comment|// ==========================================================================
specifier|private
name|boolean
name|hasGlobalReplicationScope
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|hasGlobalReplicationScope
argument_list|(
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|hasGlobalReplicationScope
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|)
block|{
return|return
name|htd
operator|!=
literal|null
condition|?
name|htd
operator|.
name|hasGlobalReplicationScope
argument_list|()
else|:
literal|false
return|;
block|}
specifier|private
name|int
name|getRegionReplication
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|)
block|{
return|return
name|htd
operator|!=
literal|null
condition|?
name|htd
operator|.
name|getRegionReplication
argument_list|()
else|:
literal|1
return|;
block|}
specifier|private
name|TableDescriptor
name|getDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|master
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
return|;
block|}
comment|// ==========================================================================
comment|//  Server Name
comment|// ==========================================================================
comment|/**    * Returns the {@link ServerName} from catalog table {@link Result}    * where the region is transitioning. It should be the same as    * {@link MetaTableAccessor#getServerName(Result,int)} if the server is at OPEN state.    * @param r Result to pull the transitioning server name from    * @return A ServerName instance or {@link MetaTableAccessor#getServerName(Result,int)}    * if necessary fields not found or empty.    */
specifier|static
name|ServerName
name|getRegionServer
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
specifier|final
name|Cell
name|cell
init|=
name|r
operator|.
name|getColumnLatestCell
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|getServerNameColumn
argument_list|(
name|replicaId
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
operator|||
name|cell
operator|.
name|getValueLength
argument_list|()
operator|==
literal|0
condition|)
block|{
name|RegionLocations
name|locations
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|locations
operator|!=
literal|null
condition|)
block|{
name|HRegionLocation
name|location
init|=
name|locations
operator|.
name|getRegionLocation
argument_list|(
name|replicaId
argument_list|)
decl_stmt|;
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
return|return
name|location
operator|.
name|getServerName
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
return|return
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getServerNameColumn
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
return|return
name|replicaId
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|SERVERNAME_QUALIFIER
else|:
name|Bytes
operator|.
name|toBytes
argument_list|(
name|HConstants
operator|.
name|SERVERNAME_QUALIFIER_STR
operator|+
name|META_REPLICA_ID_DELIMITER
operator|+
name|String
operator|.
name|format
argument_list|(
name|RegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
name|replicaId
argument_list|)
argument_list|)
return|;
block|}
comment|// ==========================================================================
comment|//  Region State
comment|// ==========================================================================
comment|/**    * Pull the region state from a catalog table {@link Result}.    * @param r Result to pull the region state from    * @return the region state, or null if unknown.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|State
name|getRegionState
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|,
name|int
name|replicaId
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|Cell
name|cell
init|=
name|r
operator|.
name|getColumnLatestCell
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|getStateColumn
argument_list|(
name|replicaId
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
operator|||
name|cell
operator|.
name|getValueLength
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|state
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|State
operator|.
name|valueOf
argument_list|(
name|state
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"BAD value {} in hbase:meta info:state column for region {} , "
operator|+
literal|"Consider using HBCK2 setRegionState ENCODED_REGION_NAME STATE"
argument_list|,
name|state
argument_list|,
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getStateColumn
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
return|return
name|replicaId
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|STATE_QUALIFIER
else|:
name|Bytes
operator|.
name|toBytes
argument_list|(
name|HConstants
operator|.
name|STATE_QUALIFIER_STR
operator|+
name|META_REPLICA_ID_DELIMITER
operator|+
name|String
operator|.
name|format
argument_list|(
name|RegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
name|replicaId
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

