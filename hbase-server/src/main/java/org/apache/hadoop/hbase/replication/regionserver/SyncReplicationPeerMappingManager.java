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
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
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
name|ReplicationPeerConfig
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
comment|/**  * Used to map region to sync replication peer id.  *<p>  * TODO: now only support include table options.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SyncReplicationPeerMappingManager
block|{
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|table2PeerId
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|void
name|add
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
block|{
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|tn
lambda|->
name|table2PeerId
operator|.
name|put
argument_list|(
name|tn
argument_list|,
name|peerId
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|remove
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
block|{
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|table2PeerId
operator|::
name|remove
argument_list|)
expr_stmt|;
block|}
name|String
name|getPeerId
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|table2PeerId
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

