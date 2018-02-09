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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Get notification for replication peer events. Mainly used for telling the  * {@link org.apache.hadoop.hbase.wal.SyncReplicationWALProvider} to close some WAL if not used any  * more.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|PeerActionListener
block|{
specifier|static
specifier|final
name|PeerActionListener
name|DUMMY
init|=
operator|new
name|PeerActionListener
argument_list|()
block|{}
decl_stmt|;
specifier|default
name|void
name|peerSyncReplicationStateChange
parameter_list|(
name|String
name|peerId
parameter_list|,
name|SyncReplicationState
name|from
parameter_list|,
name|SyncReplicationState
name|to
parameter_list|,
name|int
name|stage
parameter_list|)
block|{}
block|}
end_interface

end_unit

