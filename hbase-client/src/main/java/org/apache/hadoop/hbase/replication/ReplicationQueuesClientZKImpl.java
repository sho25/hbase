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
name|java
operator|.
name|util
operator|.
name|List
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
name|zookeeper
operator|.
name|ZKUtil
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
name|ZooKeeperWatcher
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationQueuesClientZKImpl
extends|extends
name|ReplicationStateZKBase
implements|implements
name|ReplicationQueuesClient
block|{
specifier|public
name|ReplicationQueuesClientZKImpl
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zk
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|zk
argument_list|,
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
try|try
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|this
operator|.
name|queuesZNode
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
name|ReplicationException
argument_list|(
literal|"Internal error while initializing a queues client"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|serverName
argument_list|)
decl_stmt|;
name|znode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|queueId
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to get list of wals for queueId="
operator|+
name|queueId
operator|+
literal|" and serverName="
operator|+
name|serverName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|result
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
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|queuesZNode
argument_list|,
name|serverName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|zookeeper
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|.
name|abort
argument_list|(
literal|"Failed to get list of queues for serverName="
operator|+
name|serverName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

