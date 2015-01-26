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
name|Arrays
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
name|HRegionInfo
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
name|Server
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
name|regionserver
operator|.
name|HRegion
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
name|RegionServerServices
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
name|MultiHConnection
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
name|zookeeper
operator|.
name|KeeperException
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

begin_comment
comment|/**  * A helper to persist region state in meta. We may change this class  * to StateStore later if we also use it to store other states in meta  */
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionStateStore
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The delimiter for meta columns for replicaIds> 0 */
specifier|protected
specifier|static
specifier|final
name|char
name|META_REPLICA_ID_DELIMITER
init|=
literal|'_'
decl_stmt|;
specifier|private
specifier|volatile
name|HRegion
name|metaRegion
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|initialized
decl_stmt|;
specifier|private
name|MultiHConnection
name|multiHConnection
decl_stmt|;
specifier|private
specifier|final
name|Server
name|server
decl_stmt|;
comment|/**    * Returns the {@link ServerName} from catalog table {@link Result}    * where the region is transitioning. It should be the same as    * {@link HRegionInfo#getServerName(Result)} if the server is at OPEN state.    * @param r Result to pull the transitioning server name from    * @return A ServerName instance or {@link HRegionInfo#getServerName(Result)}    * if necessary fields not found or empty.    */
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
name|HRegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
name|replicaId
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Pull the region state from a catalog table {@link Result}.    * @param r Result to pull the region state from    * @return the region state, or OPEN if there's no value written.    */
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
return|return
name|State
operator|.
name|OPEN
return|;
return|return
name|State
operator|.
name|valueOf
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
name|HRegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
name|replicaId
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Check if we should persist a state change in meta. Generally it's    * better to persist all state changes. However, we should not do that    * if the region is not in meta at all. Based on the state and the    * previous state, we can identify if a user region has an entry    * in meta. For example, merged regions are deleted from meta;    * New merging parents, or splitting daughters are    * not created in meta yet.    */
specifier|private
name|boolean
name|shouldPersistStateChange
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|RegionState
name|state
parameter_list|,
name|RegionState
name|oldState
parameter_list|)
block|{
return|return
operator|!
name|hri
operator|.
name|isMetaRegion
argument_list|()
operator|&&
operator|!
name|RegionStates
operator|.
name|isOneOfStates
argument_list|(
name|state
argument_list|,
name|State
operator|.
name|MERGING_NEW
argument_list|,
name|State
operator|.
name|SPLITTING_NEW
argument_list|,
name|State
operator|.
name|MERGED
argument_list|)
operator|&&
operator|!
operator|(
name|RegionStates
operator|.
name|isOneOfStates
argument_list|(
name|state
argument_list|,
name|State
operator|.
name|OFFLINE
argument_list|)
operator|&&
name|RegionStates
operator|.
name|isOneOfStates
argument_list|(
name|oldState
argument_list|,
name|State
operator|.
name|MERGING_NEW
argument_list|,
name|State
operator|.
name|SPLITTING_NEW
argument_list|,
name|State
operator|.
name|MERGED
argument_list|)
operator|)
return|;
block|}
name|RegionStateStore
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|initialized
operator|=
literal|false
expr_stmt|;
block|}
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|server
operator|instanceof
name|RegionServerServices
condition|)
block|{
name|metaRegion
operator|=
operator|(
operator|(
name|RegionServerServices
operator|)
name|server
operator|)
operator|.
name|getFromOnlineRegions
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// When meta is not colocated on master
if|if
condition|(
name|metaRegion
operator|==
literal|null
condition|)
block|{
name|Configuration
name|conf
init|=
name|server
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Config to determine the no of HConnections to META.
comment|// A single HConnection should be sufficient in most cases. Only if
comment|// you are doing lot of writes (>1M) to META,
comment|// increasing this value might improve the write throughput.
name|multiHConnection
operator|=
operator|new
name|MultiHConnection
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionstatestore.meta.connection"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
name|void
name|stop
parameter_list|()
block|{
name|initialized
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|multiHConnection
operator|!=
literal|null
condition|)
block|{
name|multiHConnection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|updateRegionState
parameter_list|(
name|long
name|openSeqNum
parameter_list|,
name|RegionState
name|newState
parameter_list|,
name|RegionState
name|oldState
parameter_list|)
block|{
try|try
block|{
name|HRegionInfo
name|hri
init|=
name|newState
operator|.
name|getRegion
argument_list|()
decl_stmt|;
comment|// update meta before checking for initialization.
comment|// meta state stored in zk.
if|if
condition|(
name|hri
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
comment|// persist meta state in MetaTableLocator (which in turn is zk storage currently)
try|try
block|{
name|MetaTableLocator
operator|.
name|setMetaLocation
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|newState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|newState
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
return|return;
comment|// Done
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
literal|"Failed to update meta ZNode"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|initialized
operator|||
operator|!
name|shouldPersistStateChange
argument_list|(
name|hri
argument_list|,
name|newState
argument_list|,
name|oldState
argument_list|)
condition|)
block|{
return|return;
block|}
name|ServerName
name|oldServer
init|=
name|oldState
operator|!=
literal|null
condition|?
name|oldState
operator|.
name|getServerName
argument_list|()
else|:
literal|null
decl_stmt|;
name|ServerName
name|serverName
init|=
name|newState
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|State
name|state
init|=
name|newState
operator|.
name|getState
argument_list|()
decl_stmt|;
name|int
name|replicaId
init|=
name|hri
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
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
name|hri
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuilder
name|info
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Updating row "
argument_list|)
decl_stmt|;
name|info
operator|.
name|append
argument_list|(
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" with state="
argument_list|)
operator|.
name|append
argument_list|(
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|serverName
operator|!=
literal|null
operator|&&
operator|!
name|serverName
operator|.
name|equals
argument_list|(
name|oldServer
argument_list|)
condition|)
block|{
name|put
operator|.
name|addImmutable
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|getServerNameColumn
argument_list|(
name|replicaId
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|info
operator|.
name|append
argument_list|(
literal|"&sn="
argument_list|)
operator|.
name|append
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
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
name|serverName
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
name|serverName
argument_list|,
name|openSeqNum
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
name|info
operator|.
name|append
argument_list|(
literal|"&openSeqNum="
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
literal|"&server="
argument_list|)
operator|.
name|append
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|addImmutable
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|getStateColumn
argument_list|(
name|replicaId
argument_list|)
argument_list|,
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
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|info
argument_list|)
expr_stmt|;
comment|// Persist the state change to meta
if|if
condition|(
name|metaRegion
operator|!=
literal|null
condition|)
block|{
try|try
block|{
comment|// Assume meta is pinned to master.
comment|// At least, that's what we want.
name|metaRegion
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
return|return;
comment|// Done here
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// In unit tests, meta could be moved away by intention
comment|// So, the shortcut is gone. We won't try to establish the
comment|// shortcut any more because we prefer meta to be pinned
comment|// to the master
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|metaRegion
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Meta region shortcut failed"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|multiHConnection
operator|==
literal|null
condition|)
block|{
name|multiHConnection
operator|=
operator|new
name|MultiHConnection
argument_list|(
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|metaRegion
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Called when meta is not on master
name|multiHConnection
operator|.
name|processBatchCallback
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|put
argument_list|)
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to persist region state "
operator|+
name|newState
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|server
operator|.
name|abort
argument_list|(
literal|"Failed to update region location"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|splitRegion
parameter_list|(
name|HRegionInfo
name|p
parameter_list|,
name|HRegionInfo
name|a
parameter_list|,
name|HRegionInfo
name|b
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|int
name|regionReplication
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|splitRegion
argument_list|(
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|p
argument_list|,
name|a
argument_list|,
name|b
argument_list|,
name|sn
argument_list|,
name|regionReplication
argument_list|)
expr_stmt|;
block|}
name|void
name|mergeRegions
parameter_list|(
name|HRegionInfo
name|p
parameter_list|,
name|HRegionInfo
name|a
parameter_list|,
name|HRegionInfo
name|b
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|int
name|regionReplication
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|mergeRegions
argument_list|(
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|p
argument_list|,
name|a
argument_list|,
name|b
argument_list|,
name|sn
argument_list|,
name|regionReplication
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

