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
operator|.
name|regionserver
package|;
end_package

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
name|function
operator|.
name|BiPredicate
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
name|replication
operator|.
name|ReplicationPeerImpl
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
name|replication
operator|.
name|ReplicationPeers
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
name|replication
operator|.
name|SyncReplicationState
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SyncReplicationPeerInfoProviderImpl
implements|implements
name|SyncReplicationPeerInfoProvider
block|{
specifier|private
specifier|final
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
specifier|private
specifier|final
name|SyncReplicationPeerMappingManager
name|mapping
decl_stmt|;
name|SyncReplicationPeerInfoProviderImpl
parameter_list|(
name|ReplicationPeers
name|replicationPeers
parameter_list|,
name|SyncReplicationPeerMappingManager
name|mapping
parameter_list|)
block|{
name|this
operator|.
name|replicationPeers
operator|=
name|replicationPeers
expr_stmt|;
name|this
operator|.
name|mapping
operator|=
name|mapping
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPeerIdAndRemoteWALDir
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
name|String
name|peerId
init|=
name|mapping
operator|.
name|getPeerId
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|peerId
operator|==
literal|null
condition|)
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
name|ReplicationPeerImpl
name|peer
init|=
name|replicationPeers
operator|.
name|getPeer
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|peer
operator|==
literal|null
condition|)
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
name|Pair
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
name|states
init|=
name|peer
operator|.
name|getSyncReplicationStateAndNewState
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|states
operator|.
name|getFirst
argument_list|()
operator|==
name|SyncReplicationState
operator|.
name|ACTIVE
operator|&&
name|states
operator|.
name|getSecond
argument_list|()
operator|==
name|SyncReplicationState
operator|.
name|NONE
operator|)
operator|||
operator|(
name|states
operator|.
name|getFirst
argument_list|()
operator|==
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
operator|&&
name|states
operator|.
name|getSecond
argument_list|()
operator|==
name|SyncReplicationState
operator|.
name|ACTIVE
operator|)
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|Pair
operator|.
name|newPair
argument_list|(
name|peerId
argument_list|,
name|peer
operator|.
name|getPeerConfig
argument_list|()
operator|.
name|getRemoteWALDir
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkState
parameter_list|(
name|TableName
name|table
parameter_list|,
name|BiPredicate
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
name|checker
parameter_list|)
block|{
name|String
name|peerId
init|=
name|mapping
operator|.
name|getPeerId
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|peerId
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ReplicationPeerImpl
name|peer
init|=
name|replicationPeers
operator|.
name|getPeer
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|peer
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Pair
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
name|states
init|=
name|peer
operator|.
name|getSyncReplicationStateAndNewState
argument_list|()
decl_stmt|;
return|return
name|checker
operator|.
name|test
argument_list|(
name|states
operator|.
name|getFirst
argument_list|()
argument_list|,
name|states
operator|.
name|getSecond
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

