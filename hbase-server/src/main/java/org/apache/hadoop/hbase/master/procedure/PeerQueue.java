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
name|procedure
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
name|master
operator|.
name|procedure
operator|.
name|PeerProcedureInterface
operator|.
name|PeerOperationType
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
name|LockStatus
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
name|PeerQueue
extends|extends
name|Queue
argument_list|<
name|String
argument_list|>
block|{
specifier|public
name|PeerQueue
parameter_list|(
name|String
name|peerId
parameter_list|,
name|LockStatus
name|lockStatus
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|,
name|lockStatus
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requireExclusiveLock
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
block|{
return|return
name|requirePeerExclusiveLock
argument_list|(
operator|(
name|PeerProcedureInterface
operator|)
name|proc
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|requirePeerExclusiveLock
parameter_list|(
name|PeerProcedureInterface
name|proc
parameter_list|)
block|{
comment|// These procedures will only be used as sub procedures, and if they are scheduled, it always
comment|// means that the root procedure holds the xlock, so we do not need to hold any locks.
return|return
name|proc
operator|.
name|getPeerOperationType
argument_list|()
operator|!=
name|PeerOperationType
operator|.
name|REFRESH
operator|&&
name|proc
operator|.
name|getPeerOperationType
argument_list|()
operator|!=
name|PeerOperationType
operator|.
name|RECOVER_STANDBY
operator|&&
name|proc
operator|.
name|getPeerOperationType
argument_list|()
operator|!=
name|PeerOperationType
operator|.
name|SYNC_REPLICATION_REPLAY_WAL
operator|&&
name|proc
operator|.
name|getPeerOperationType
argument_list|()
operator|!=
name|PeerOperationType
operator|.
name|SYNC_REPLICATION_REPLAY_WAL_REMOTE
return|;
block|}
block|}
end_class

end_unit

