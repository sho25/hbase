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
name|replication
package|;
end_package

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
name|Map
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
name|CellUtil
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
name|wal
operator|.
name|WALEdit
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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

begin_comment
comment|/**  * Filter a WAL Entry by namespaces and table-cfs config in the peer. It first filter entry  * by namespaces config, then filter entry by table-cfs config.  *  * 1. Set a namespace in peer config means that all tables in this namespace will be replicated.  * 2. If the namespaces config is null, then the table-cfs config decide which table's edit  *    can be replicated. If the table-cfs config is null, then the namespaces config decide  *    which table's edit can be replicated.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NamespaceTableCfWALEntryFilter
implements|implements
name|WALEntryFilter
implements|,
name|WALCellFilter
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
name|NamespaceTableCfWALEntryFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReplicationPeer
name|peer
decl_stmt|;
specifier|private
name|BulkLoadCellFilter
name|bulkLoadFilter
init|=
operator|new
name|BulkLoadCellFilter
argument_list|()
decl_stmt|;
specifier|public
name|NamespaceTableCfWALEntryFilter
parameter_list|(
name|ReplicationPeer
name|peer
parameter_list|)
block|{
name|this
operator|.
name|peer
operator|=
name|peer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Entry
name|filter
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|TableName
name|tabName
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTablename
argument_list|()
decl_stmt|;
name|String
name|namespace
init|=
name|tabName
operator|.
name|getNamespaceAsString
argument_list|()
decl_stmt|;
name|ReplicationPeerConfig
name|peerConfig
init|=
name|this
operator|.
name|peer
operator|.
name|getPeerConfig
argument_list|()
decl_stmt|;
if|if
condition|(
name|peerConfig
operator|.
name|replicateAllUserTables
argument_list|()
condition|)
block|{
comment|// replicate all user tables, so return entry directly
return|return
name|entry
return|;
block|}
else|else
block|{
comment|// Not replicate all user tables, so filter by namespaces and table-cfs config
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFs
init|=
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespaces
operator|==
literal|null
operator|&&
name|tableCFs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// First filter by namespaces config
comment|// If table's namespace in peer config, all the tables data are applicable for replication
if|if
condition|(
name|namespaces
operator|!=
literal|null
operator|&&
name|namespaces
operator|.
name|contains
argument_list|(
name|namespace
argument_list|)
condition|)
block|{
return|return
name|entry
return|;
block|}
comment|// Then filter by table-cfs config
comment|// return null(prevent replicating) if logKey's table isn't in this peer's
comment|// replicaable namespace list and table list
if|if
condition|(
name|tableCFs
operator|==
literal|null
operator|||
operator|!
name|tableCFs
operator|.
name|containsKey
argument_list|(
name|tabName
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|entry
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|filterCell
parameter_list|(
specifier|final
name|Entry
name|entry
parameter_list|,
name|Cell
name|cell
parameter_list|)
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|this
operator|.
name|peer
operator|.
name|getPeerConfig
argument_list|()
decl_stmt|;
if|if
condition|(
name|peerConfig
operator|.
name|replicateAllUserTables
argument_list|()
condition|)
block|{
comment|// replicate all user tables, so return cell directly
return|return
name|cell
return|;
block|}
else|else
block|{
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCfs
init|=
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableCfs
operator|==
literal|null
condition|)
block|{
return|return
name|cell
return|;
block|}
name|TableName
name|tabName
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTablename
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cfs
init|=
name|tableCfs
operator|.
name|get
argument_list|(
name|tabName
argument_list|)
decl_stmt|;
comment|// ignore(remove) kv if its cf isn't in the replicable cf list
comment|// (empty cfs means all cfs of this table are replicable)
if|if
condition|(
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|cell
argument_list|,
name|WALEdit
operator|.
name|METAFAMILY
argument_list|,
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
condition|)
block|{
name|cell
operator|=
name|bulkLoadFilter
operator|.
name|filterCell
argument_list|(
name|cell
argument_list|,
operator|new
name|Predicate
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|byte
index|[]
name|fam
parameter_list|)
block|{
if|if
condition|(
name|tableCfs
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cfs
init|=
name|tableCfs
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getTablename
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cfs
operator|!=
literal|null
operator|&&
operator|!
name|cfs
operator|.
name|contains
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fam
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|(
name|cfs
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|cfs
operator|.
name|contains
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
name|cell
return|;
block|}
block|}
block|}
end_class

end_unit

