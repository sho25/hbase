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
name|CompoundConfiguration
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
name|HBaseConfiguration
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Helper class for replication.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ReplicationUtils
block|{
specifier|private
name|ReplicationUtils
parameter_list|()
block|{   }
specifier|public
specifier|static
name|Configuration
name|getPeerClusterConfiguration
parameter_list|(
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
name|Configuration
name|baseConf
parameter_list|)
throws|throws
name|ReplicationException
block|{
name|Configuration
name|otherConf
decl_stmt|;
try|try
block|{
name|otherConf
operator|=
name|HBaseConfiguration
operator|.
name|createClusterConf
argument_list|(
name|baseConf
argument_list|,
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ReplicationException
argument_list|(
literal|"Can't get peer configuration for peer "
operator|+
name|peerConfig
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|CompoundConfiguration
name|compound
init|=
operator|new
name|CompoundConfiguration
argument_list|()
decl_stmt|;
name|compound
operator|.
name|add
argument_list|(
name|otherConf
argument_list|)
expr_stmt|;
name|compound
operator|.
name|addStringMap
argument_list|(
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|compound
return|;
block|}
return|return
name|otherConf
return|;
block|}
specifier|public
specifier|static
name|void
name|removeAllQueues
parameter_list|(
name|ReplicationQueueStorage
name|queueStorage
parameter_list|,
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{
for|for
control|(
name|ServerName
name|replicator
range|:
name|queueStorage
operator|.
name|getListOfReplicators
argument_list|()
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queueIds
init|=
name|queueStorage
operator|.
name|getAllQueues
argument_list|(
name|replicator
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|queueId
range|:
name|queueIds
control|)
block|{
name|ReplicationQueueInfo
name|queueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|queueId
argument_list|)
decl_stmt|;
if|if
condition|(
name|queueInfo
operator|.
name|getPeerId
argument_list|()
operator|.
name|equals
argument_list|(
name|peerId
argument_list|)
condition|)
block|{
name|queueStorage
operator|.
name|removeQueue
argument_list|(
name|replicator
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
block|}
block|}
name|queueStorage
operator|.
name|removeReplicatorIfQueueIsEmpty
argument_list|(
name|replicator
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isCollectionEqual
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|c1
parameter_list|,
name|Collection
argument_list|<
name|String
argument_list|>
name|c2
parameter_list|)
block|{
if|if
condition|(
name|c1
operator|==
literal|null
condition|)
block|{
return|return
name|c2
operator|==
literal|null
return|;
block|}
if|if
condition|(
name|c2
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|c1
operator|.
name|size
argument_list|()
operator|==
name|c2
operator|.
name|size
argument_list|()
operator|&&
name|c1
operator|.
name|containsAll
argument_list|(
name|c2
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isNamespacesEqual
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|ns1
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|ns2
parameter_list|)
block|{
return|return
name|isCollectionEqual
argument_list|(
name|ns1
argument_list|,
name|ns2
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isTableCFsEqual
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFs1
parameter_list|,
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFs2
parameter_list|)
block|{
if|if
condition|(
name|tableCFs1
operator|==
literal|null
condition|)
block|{
return|return
name|tableCFs2
operator|==
literal|null
return|;
block|}
if|if
condition|(
name|tableCFs2
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|tableCFs1
operator|.
name|size
argument_list|()
operator|!=
name|tableCFs2
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry1
range|:
name|tableCFs1
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|table
init|=
name|entry1
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|tableCFs2
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|cfs1
init|=
name|entry1
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cfs2
init|=
name|tableCFs2
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isCollectionEqual
argument_list|(
name|cfs1
argument_list|,
name|cfs2
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isKeyConfigEqual
parameter_list|(
name|ReplicationPeerConfig
name|rpc1
parameter_list|,
name|ReplicationPeerConfig
name|rpc2
parameter_list|)
block|{
if|if
condition|(
name|rpc1
operator|.
name|replicateAllUserTables
argument_list|()
operator|!=
name|rpc2
operator|.
name|replicateAllUserTables
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|rpc1
operator|.
name|replicateAllUserTables
argument_list|()
condition|)
block|{
return|return
name|isNamespacesEqual
argument_list|(
name|rpc1
operator|.
name|getExcludeNamespaces
argument_list|()
argument_list|,
name|rpc2
operator|.
name|getExcludeNamespaces
argument_list|()
argument_list|)
operator|&&
name|isTableCFsEqual
argument_list|(
name|rpc1
operator|.
name|getExcludeTableCFsMap
argument_list|()
argument_list|,
name|rpc2
operator|.
name|getExcludeTableCFsMap
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|isNamespacesEqual
argument_list|(
name|rpc1
operator|.
name|getNamespaces
argument_list|()
argument_list|,
name|rpc2
operator|.
name|getNamespaces
argument_list|()
argument_list|)
operator|&&
name|isTableCFsEqual
argument_list|(
name|rpc1
operator|.
name|getTableCFsMap
argument_list|()
argument_list|,
name|rpc2
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

