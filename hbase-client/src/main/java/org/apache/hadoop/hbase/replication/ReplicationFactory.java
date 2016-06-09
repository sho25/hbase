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
name|reflect
operator|.
name|ConstructorUtils
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
name|Stoppable
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

begin_comment
comment|/**  * A factory class for instantiating replication objects that deal with replication state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationFactory
block|{
specifier|public
specifier|static
name|ReplicationQueues
name|getReplicationQueues
parameter_list|(
name|ReplicationQueuesArguments
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|classToBuild
init|=
name|args
operator|.
name|getConf
argument_list|()
operator|.
name|getClass
argument_list|(
literal|"hbase.region.replica."
operator|+
literal|"replication.ReplicationQueuesType"
argument_list|,
name|ReplicationQueuesZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|(
name|ReplicationQueues
operator|)
name|ConstructorUtils
operator|.
name|invokeConstructor
argument_list|(
name|classToBuild
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ReplicationQueuesClient
name|getReplicationQueuesClient
parameter_list|(
name|ReplicationQueuesClientArguments
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|classToBuild
init|=
name|args
operator|.
name|getConf
argument_list|()
operator|.
name|getClass
argument_list|(
literal|"hbase.region.replica."
operator|+
literal|"replication.ReplicationQueuesClientType"
argument_list|,
name|ReplicationQueuesClientZKImpl
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|(
name|ReplicationQueuesClient
operator|)
name|ConstructorUtils
operator|.
name|invokeConstructor
argument_list|(
name|classToBuild
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ReplicationPeers
name|getReplicationPeers
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
return|return
name|getReplicationPeers
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
name|abortable
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ReplicationPeers
name|getReplicationPeers
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zk
parameter_list|,
name|Configuration
name|conf
parameter_list|,
specifier|final
name|ReplicationQueuesClient
name|queuesClient
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
return|return
operator|new
name|ReplicationPeersZKImpl
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|,
name|queuesClient
argument_list|,
name|abortable
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ReplicationTracker
name|getReplicationTracker
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|,
specifier|final
name|ReplicationPeers
name|replicationPeers
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
name|Stoppable
name|stopper
parameter_list|)
block|{
return|return
operator|new
name|ReplicationTrackerZKImpl
argument_list|(
name|zookeeper
argument_list|,
name|replicationPeers
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|,
name|stopper
argument_list|)
return|;
block|}
block|}
end_class

end_unit

