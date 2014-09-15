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
name|util
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
name|HashMap
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
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
name|conf
operator|.
name|Configured
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
name|TableState
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
name|exceptions
operator|.
name|DeserializationException
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
name|snapshot
operator|.
name|SnapshotManager
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
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
name|ReplicationStateZKBase
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
name|hadoop
operator|.
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
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
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|NoNodeException
import|;
end_import

begin_comment
comment|/**  * Tool to migrate zookeeper data of older hbase versions(<0.95.0) to PB.  */
end_comment

begin_class
specifier|public
class|class
name|ZKDataMigrator
extends|extends
name|Configured
implements|implements
name|Tool
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
name|ZKDataMigrator
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|as
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|getConf
argument_list|()
argument_list|,
literal|"Migrate ZK data to PB."
argument_list|,
operator|new
name|ZKDataMigratorAbortable
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|baseZNode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No hbase related data available in zookeeper. returning.."
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|baseZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No child nodes to mirgrate. returning.."
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|String
name|childPath
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|childPath
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|baseZNode
argument_list|,
name|child
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.rootserver"
argument_list|,
literal|"root-region-server"
argument_list|)
argument_list|)
condition|)
block|{
comment|// -ROOT- region no longer present from 0.95.0, so we can remove this
comment|// znode
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
comment|// TODO delete root table path from file system.
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.rs"
argument_list|,
literal|"rs"
argument_list|)
argument_list|)
condition|)
block|{
comment|// Since there is no live region server instance during migration, we
comment|// can remove this znode as well.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.draining.rs"
argument_list|,
literal|"draining"
argument_list|)
argument_list|)
condition|)
block|{
comment|// If we want to migrate to 0.95.0 from older versions we need to stop
comment|// the existing cluster. So there wont be any draining servers so we
comment|// can
comment|// remove it.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.master"
argument_list|,
literal|"master"
argument_list|)
argument_list|)
condition|)
block|{
comment|// Since there is no live master instance during migration, we can
comment|// remove this znode as well.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.backup.masters"
argument_list|,
literal|"backup-masters"
argument_list|)
argument_list|)
condition|)
block|{
comment|// Since there is no live backup master instances during migration, we
comment|// can remove this znode as well.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.state"
argument_list|,
literal|"shutdown"
argument_list|)
argument_list|)
condition|)
block|{
comment|// shutdown node is not present from 0.95.0 onwards. Its renamed to
comment|// "running". We can delete it.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.unassigned"
argument_list|,
literal|"unassigned"
argument_list|)
argument_list|)
condition|)
block|{
comment|// Any way during clean cluster startup we will remove all unassigned
comment|// region nodes. we can delete all children nodes as well. This znode
comment|// is
comment|// renamed to "regions-in-transition" from 0.95.0 onwards.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.tableEnableDisable"
argument_list|,
literal|"table"
argument_list|)
argument_list|)
operator|||
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.masterTableEnableDisable"
argument_list|,
literal|"table"
argument_list|)
argument_list|)
condition|)
block|{
name|checkAndMigrateTableStatesToPB
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.masterTableEnableDisable92"
argument_list|,
literal|"table92"
argument_list|)
argument_list|)
condition|)
block|{
comment|// This is replica of table states from tableZnode so we can remove
comment|// this.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.splitlog"
argument_list|,
literal|"splitlog"
argument_list|)
argument_list|)
condition|)
block|{
comment|// This znode no longer available from 0.95.0 onwards, we can remove
comment|// it.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication"
argument_list|,
literal|"replication"
argument_list|)
argument_list|)
condition|)
block|{
name|checkAndMigrateReplicationNodesToPB
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.clusterId"
argument_list|,
literal|"hbaseid"
argument_list|)
argument_list|)
condition|)
block|{
comment|// it will be re-created by master.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|SnapshotManager
operator|.
name|ONLINE_SNAPSHOT_CONTROLLER_DESCRIPTION
argument_list|)
condition|)
block|{
comment|// not needed as it is transient.
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.acl.parent"
argument_list|,
literal|"acl"
argument_list|)
argument_list|)
condition|)
block|{
comment|// it will be re-created when hbase:acl is re-opened
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got exception while updating znodes "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|zkw
operator|!=
literal|null
condition|)
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|checkAndMigrateTableStatesToPB
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|tableZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No table present to migrate table state to PB. returning.."
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|String
name|table
range|:
name|tables
control|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|tableZNode
argument_list|,
name|table
argument_list|)
decl_stmt|;
comment|// Delete -ROOT- table state znode since its no longer present in 0.95.0
comment|// onwards.
if|if
condition|(
name|table
operator|.
name|equals
argument_list|(
literal|"-ROOT-"
argument_list|)
operator|||
name|table
operator|.
name|equals
argument_list|(
literal|".META."
argument_list|)
condition|)
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
decl_stmt|;
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
continue|continue;
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setState
argument_list|(
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|State
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|data
operator|=
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkAndMigrateReplicationNodesToPB
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|String
name|replicationZnodeName
init|=
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication"
argument_list|,
literal|"replication"
argument_list|)
decl_stmt|;
name|String
name|replicationPath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|baseZNode
argument_list|,
name|replicationZnodeName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|replicationZnodes
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|replicationPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|replicationZnodes
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No replication related znodes present to migrate. returning.."
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|String
name|child
range|:
name|replicationZnodes
control|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|replicationPath
argument_list|,
name|child
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.peers"
argument_list|,
literal|"peers"
argument_list|)
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|peers
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
decl_stmt|;
if|if
condition|(
name|peers
operator|==
literal|null
operator|||
name|peers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No peers present to migrate. returning.."
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|checkAndMigratePeerZnodesToPB
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|peers
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.state"
argument_list|,
literal|"state"
argument_list|)
argument_list|)
condition|)
block|{
comment|// This is no longer used in>=0.95.x
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|equals
argument_list|(
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.rs"
argument_list|,
literal|"rs"
argument_list|)
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rsList
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsList
operator|==
literal|null
operator|||
name|rsList
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
for|for
control|(
name|String
name|rs
range|:
name|rsList
control|)
block|{
name|checkAndMigrateQueuesToPB
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|checkAndMigrateQueuesToPB
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|znode
parameter_list|,
name|String
name|rs
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|NoNodeException
throws|,
name|InterruptedException
block|{
name|String
name|rsPath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|rs
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|peers
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|rsPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|peers
operator|==
literal|null
operator|||
name|peers
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
name|String
name|peerPath
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|peer
range|:
name|peers
control|)
block|{
name|peerPath
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|rsPath
argument_list|,
name|peer
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|files
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|peerPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|files
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
name|String
name|filePath
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|file
range|:
name|files
control|)
block|{
name|filePath
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|peerPath
argument_list|,
name|file
argument_list|)
expr_stmt|;
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|filePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|data
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
condition|)
continue|continue;
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
continue|continue;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
name|filePath
argument_list|,
name|ZKUtil
operator|.
name|positionToByteArray
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|checkAndMigratePeerZnodesToPB
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|znode
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|peers
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|NoNodeException
throws|,
name|InterruptedException
block|{
for|for
control|(
name|String
name|peer
range|:
name|peers
control|)
block|{
name|String
name|peerZnode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|znode
argument_list|,
name|peer
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerZnode
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|migrateClusterKeyToPB
argument_list|(
name|zkw
argument_list|,
name|peerZnode
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
name|String
name|peerStatePath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|peerZnode
argument_list|,
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.replication.peers.state"
argument_list|,
literal|"peer-state"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|peerStatePath
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|peerStatePath
argument_list|)
expr_stmt|;
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
continue|continue;
name|migratePeerStateToPB
argument_list|(
name|zkw
argument_list|,
name|data
argument_list|,
name|peerStatePath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|migrateClusterKeyToPB
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|peerZnode
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|NoNodeException
block|{
name|ReplicationPeer
name|peer
init|=
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClusterkey
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
name|peerZnode
argument_list|,
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|peer
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|migratePeerStateToPB
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|String
name|peerStatePath
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|NoNodeException
block|{
name|String
name|state
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|ENABLED
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
name|peerStatePath
argument_list|,
name|ReplicationStateZKBase
operator|.
name|ENABLED_ZNODE_BYTES
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ZooKeeperProtos
operator|.
name|ReplicationState
operator|.
name|State
operator|.
name|DISABLED
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zkw
argument_list|,
name|peerStatePath
argument_list|,
name|ReplicationStateZKBase
operator|.
name|DISABLED_ZNODE_BYTES
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Method for table states migration.    * Reading state from zk, applying them to internal state    * and delete.    * Used by master to clean migration from zk based states to    * table descriptor based states.    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|Map
argument_list|<
name|TableName
argument_list|,
name|TableState
operator|.
name|State
argument_list|>
name|queryForTableStates
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|Map
argument_list|<
name|TableName
argument_list|,
name|TableState
operator|.
name|State
argument_list|>
name|rv
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|tableZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
return|return
name|rv
return|;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|child
argument_list|)
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|State
name|state
init|=
name|getTableState
argument_list|(
name|zkw
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TableState
operator|.
name|State
name|newState
init|=
name|TableState
operator|.
name|State
operator|.
name|ENABLED
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|ENABLED
case|:
name|newState
operator|=
name|TableState
operator|.
name|State
operator|.
name|ENABLED
expr_stmt|;
break|break;
case|case
name|DISABLED
case|:
name|newState
operator|=
name|TableState
operator|.
name|State
operator|.
name|DISABLED
expr_stmt|;
break|break;
case|case
name|DISABLING
case|:
name|newState
operator|=
name|TableState
operator|.
name|State
operator|.
name|DISABLING
expr_stmt|;
break|break;
case|case
name|ENABLING
case|:
name|newState
operator|=
name|TableState
operator|.
name|State
operator|.
name|ENABLING
expr_stmt|;
break|break;
default|default:
block|}
block|}
name|rv
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|newState
argument_list|)
expr_stmt|;
block|}
return|return
name|rv
return|;
block|}
comment|/**    * Gets table state from ZK.    * @param zkw ZooKeeperWatcher instance to use    * @param tableName table we're checking    * @return Null or {@link ZooKeeperProtos.DeprecatedTableState.State} found in znode.    * @throws KeeperException    */
annotation|@
name|Deprecated
specifier|private
specifier|static
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|State
name|getTableState
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|tableZNode
argument_list|,
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|null
return|;
try|try
block|{
name|ProtobufUtil
operator|.
name|expectPBMagicPrefix
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|int
name|magicLen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|DeprecatedTableState
name|t
init|=
name|builder
operator|.
name|mergeFrom
argument_list|(
name|data
argument_list|,
name|magicLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|magicLen
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|t
operator|.
name|getState
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
name|KeeperException
name|ke
init|=
operator|new
name|KeeperException
operator|.
name|DataInconsistencyException
argument_list|()
decl_stmt|;
name|ke
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ke
throw|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
name|ZKUtil
operator|.
name|convert
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|exit
argument_list|(
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|ZKDataMigrator
argument_list|()
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|ZKDataMigratorAbortable
implements|implements
name|Abortable
block|{
specifier|private
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got aborted with reason: "
operator|+
name|why
operator|+
literal|", and error: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|aborted
return|;
block|}
block|}
block|}
end_class

end_unit

