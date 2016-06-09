begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|NotImplementedException
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
name|Abortable
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
name|ResultScanner
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
name|Scan
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
name|HashSet
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Implements the ReplicationQueuesClient interface on top of the Replication Table. It utilizes  * the ReplicationTableBase to access the Replication Table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableBasedReplicationQueuesClientImpl
extends|extends
name|ReplicationTableBase
implements|implements
name|ReplicationQueuesClient
block|{
specifier|public
name|TableBasedReplicationQueuesClientImpl
parameter_list|(
name|ReplicationQueuesClientArguments
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|args
operator|.
name|getConf
argument_list|()
argument_list|,
name|args
operator|.
name|getAbortable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableBasedReplicationQueuesClientImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|ReplicationException
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getListOfReplicators
parameter_list|()
block|{
return|return
name|super
operator|.
name|getListOfReplicators
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getLogsInQueue
parameter_list|(
name|String
name|serverName
parameter_list|,
name|String
name|queueId
parameter_list|)
block|{
return|return
name|super
operator|.
name|getLogsInQueue
argument_list|(
name|serverName
argument_list|,
name|queueId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllQueues
parameter_list|(
name|String
name|serverName
parameter_list|)
block|{
return|return
name|super
operator|.
name|getAllQueues
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getAllWALs
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|allWals
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ResultScanner
name|allQueues
init|=
literal|null
decl_stmt|;
try|try
block|{
name|allQueues
operator|=
name|replicationTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|queue
range|:
name|allQueues
control|)
block|{
for|for
control|(
name|String
name|wal
range|:
name|readWALsFromResult
argument_list|(
name|queue
argument_list|)
control|)
block|{
name|allWals
operator|.
name|add
argument_list|(
name|wal
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|errMsg
init|=
literal|"Failed getting all WAL's in Replication Table"
decl_stmt|;
name|abortable
operator|.
name|abort
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|allQueues
operator|!=
literal|null
condition|)
block|{
name|allQueues
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|allWals
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getHFileRefsNodeChangeVersion
parameter_list|()
throws|throws
name|KeeperException
block|{
comment|// TODO
throw|throw
operator|new
name|NotImplementedException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllPeersFromHFileRefsQueue
parameter_list|()
throws|throws
name|KeeperException
block|{
comment|// TODO
throw|throw
operator|new
name|NotImplementedException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getReplicableHFiles
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|KeeperException
block|{
comment|// TODO
throw|throw
operator|new
name|NotImplementedException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

