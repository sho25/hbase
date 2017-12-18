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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|shaded
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
name|shaded
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
name|ZNodePaths
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
name|ZKListener
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
name|ZKWatcher
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|ConcurrentSkipListMap
import|;
end_import

begin_comment
comment|/**  * Class servers two purposes:  *  * 1. Broadcast NamespaceDescriptor information via ZK  * (Done by the Master)  * 2. Consume broadcasted NamespaceDescriptor changes  * (Done by the RegionServers)  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKNamespaceManager
extends|extends
name|ZKListener
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
name|ZKNamespaceManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|nsZNode
decl_stmt|;
specifier|private
specifier|final
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|NamespaceDescriptor
argument_list|>
name|cache
decl_stmt|;
specifier|public
name|ZKNamespaceManager
parameter_list|(
name|ZKWatcher
name|zkw
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
name|nsZNode
operator|=
name|zkw
operator|.
name|znodePaths
operator|.
name|namespaceZNode
expr_stmt|;
name|cache
operator|=
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|nsZNode
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|ZKUtil
operator|.
name|NodeAndData
argument_list|>
name|existing
init|=
name|ZKUtil
operator|.
name|getChildDataAndWatchForNewChildren
argument_list|(
name|watcher
argument_list|,
name|nsZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
literal|null
condition|)
block|{
name|refreshNodes
argument_list|(
name|existing
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|nsZNode
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize ZKNamespaceManager"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|NamespaceDescriptor
name|get
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|cache
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|writeNamespace
argument_list|(
name|ns
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|ns
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|remove
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteNamespace
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|cache
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NavigableSet
argument_list|<
name|NamespaceDescriptor
argument_list|>
name|list
parameter_list|()
throws|throws
name|IOException
block|{
name|NavigableSet
argument_list|<
name|NamespaceDescriptor
argument_list|>
name|ret
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|(
name|NamespaceDescriptor
operator|.
name|NAMESPACE_DESCRIPTOR_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|NamespaceDescriptor
name|ns
range|:
name|cache
operator|.
name|values
argument_list|()
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|ns
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|nsZNode
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
try|try
block|{
name|List
argument_list|<
name|ZKUtil
operator|.
name|NodeAndData
argument_list|>
name|nodes
init|=
name|ZKUtil
operator|.
name|getChildDataAndWatchForNewChildren
argument_list|(
name|watcher
argument_list|,
name|nsZNode
argument_list|)
decl_stmt|;
name|refreshNodes
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error reading data from zookeeper"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error parsing data from zookeeper"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|nsZNode
operator|.
name|equals
argument_list|(
name|ZKUtil
operator|.
name|getParent
argument_list|(
name|path
argument_list|)
argument_list|)
condition|)
block|{
name|String
name|nsName
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|cache
operator|.
name|remove
argument_list|(
name|nsName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeDataChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|nsZNode
operator|.
name|equals
argument_list|(
name|ZKUtil
operator|.
name|getParent
argument_list|(
name|path
argument_list|)
argument_list|)
condition|)
block|{
try|try
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|NamespaceDescriptor
name|ns
init|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|HBaseProtos
operator|.
name|NamespaceDescriptor
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
argument_list|)
decl_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|ns
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error reading data from zookeeper for node "
operator|+
name|path
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|ke
argument_list|)
expr_stmt|;
comment|// only option is to abort
name|watcher
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error deserializing namespace: "
operator|+
name|path
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|nsZNode
operator|.
name|equals
argument_list|(
name|path
argument_list|)
condition|)
block|{
try|try
block|{
name|List
argument_list|<
name|ZKUtil
operator|.
name|NodeAndData
argument_list|>
name|nodes
init|=
name|ZKUtil
operator|.
name|getChildDataAndWatchForNewChildren
argument_list|(
name|watcher
argument_list|,
name|nsZNode
argument_list|)
decl_stmt|;
name|refreshNodes
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error reading data from zookeeper for path "
operator|+
name|path
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"ZooKeeper error get node children for path "
operator|+
name|path
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error deserializing namespace child from: "
operator|+
name|path
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error deserializing namespace child from: "
operator|+
name|path
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|deleteNamespace
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|zNode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|nsZNode
argument_list|,
name|name
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|watcher
argument_list|,
name|zNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|KeeperException
operator|.
name|NoNodeException
condition|)
block|{
comment|// If the node does not exist, it could be already deleted. Continue without fail.
name|LOG
operator|.
name|warn
argument_list|(
literal|"The ZNode "
operator|+
name|zNode
operator|+
literal|" for namespace "
operator|+
name|name
operator|+
literal|" does not exist."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed updating permissions for namespace "
operator|+
name|name
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed updating permissions for namespace "
operator|+
name|name
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|writeNamespace
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|zNode
init|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|nsZNode
argument_list|,
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|zNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|updateExistingNodeData
argument_list|(
name|watcher
argument_list|,
name|zNode
argument_list|,
name|ProtobufUtil
operator|.
name|toProtoNamespaceDescriptor
argument_list|(
name|ns
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed updating permissions for namespace "
operator|+
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed updating permissions for namespace "
operator|+
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|refreshNodes
parameter_list|(
name|List
argument_list|<
name|ZKUtil
operator|.
name|NodeAndData
argument_list|>
name|nodes
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|ZKUtil
operator|.
name|NodeAndData
name|n
range|:
name|nodes
control|)
block|{
if|if
condition|(
name|n
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
name|String
name|path
init|=
name|n
operator|.
name|getNode
argument_list|()
decl_stmt|;
name|String
name|namespace
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|byte
index|[]
name|nodeData
init|=
name|n
operator|.
name|getData
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Updating namespace cache from node "
operator|+
name|namespace
operator|+
literal|" with data: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|nodeData
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|NamespaceDescriptor
name|ns
init|=
name|ProtobufUtil
operator|.
name|toNamespaceDescriptor
argument_list|(
name|HBaseProtos
operator|.
name|NamespaceDescriptor
operator|.
name|parseFrom
argument_list|(
name|nodeData
argument_list|)
argument_list|)
decl_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|,
name|ns
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

