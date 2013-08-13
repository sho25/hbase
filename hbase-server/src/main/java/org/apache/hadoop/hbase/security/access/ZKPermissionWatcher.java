begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|access
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
name|ZooKeeperListener
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

begin_comment
comment|/**  * Handles synchronization of access control list entries and updates  * throughout all nodes in the cluster.  The {@link AccessController} instance  * on the {@code _acl_} table regions, creates a znode for each table as  * {@code /hbase/acl/tablename}, with the znode data containing a serialized  * list of the permissions granted for the table.  The {@code AccessController}  * instances on all other cluster hosts watch the znodes for updates, which  * trigger updates in the {@link TableAuthManager} permission cache.  */
end_comment

begin_class
specifier|public
class|class
name|ZKPermissionWatcher
extends|extends
name|ZooKeeperListener
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZKPermissionWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// parent node for permissions lists
specifier|static
specifier|final
name|String
name|ACL_NODE
init|=
literal|"acl"
decl_stmt|;
name|TableAuthManager
name|authManager
decl_stmt|;
name|String
name|aclZNode
decl_stmt|;
specifier|public
name|ZKPermissionWatcher
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|TableAuthManager
name|authManager
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|authManager
operator|=
name|authManager
expr_stmt|;
name|String
name|aclZnodeParent
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.acl.parent"
argument_list|,
name|ACL_NODE
argument_list|)
decl_stmt|;
name|this
operator|.
name|aclZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|baseZNode
argument_list|,
name|aclZnodeParent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|KeeperException
block|{
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|aclZNode
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
name|aclZNode
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
name|path
operator|.
name|equals
argument_list|(
name|aclZNode
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
name|aclZNode
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
literal|"Error reading data from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
comment|// only option is to abort
name|watcher
operator|.
name|abort
argument_list|(
literal|"Zookeeper error obtaining acl node children"
argument_list|,
name|ke
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
name|aclZNode
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
name|table
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|AccessControlLists
operator|.
name|isNamespaceEntry
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|authManager
operator|.
name|removeNamespace
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authManager
operator|.
name|removeTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|aclZNode
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
comment|// update cache on an existing table node
name|String
name|entry
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
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
name|refreshAuthManager
argument_list|(
name|entry
argument_list|,
name|data
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
literal|"Error reading data from zookeeper for node "
operator|+
name|entry
argument_list|,
name|ke
argument_list|)
expr_stmt|;
comment|// only option is to abort
name|watcher
operator|.
name|abort
argument_list|(
literal|"Zookeeper error getting data for node "
operator|+
name|entry
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error reading permissions writables"
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
name|path
operator|.
name|equals
argument_list|(
name|aclZNode
argument_list|)
condition|)
block|{
comment|// table permissions changed
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
name|aclZNode
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
literal|"Zookeeper error get node children for path "
operator|+
name|path
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
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
name|entry
init|=
operator|(
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
operator|)
decl_stmt|;
try|try
block|{
name|refreshAuthManager
argument_list|(
name|entry
argument_list|,
name|n
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed parsing permissions for table '"
operator|+
name|entry
operator|+
literal|"' from zk"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|refreshAuthManager
parameter_list|(
name|String
name|entry
parameter_list|,
name|byte
index|[]
name|nodeData
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Updating permissions cache from node "
operator|+
name|entry
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
if|if
condition|(
name|AccessControlLists
operator|.
name|isNamespaceEntry
argument_list|(
name|entry
argument_list|)
condition|)
block|{
name|authManager
operator|.
name|refreshNamespaceCacheFromWritable
argument_list|(
name|AccessControlLists
operator|.
name|fromNamespaceEntry
argument_list|(
name|entry
argument_list|)
argument_list|,
name|nodeData
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authManager
operator|.
name|refreshTableCacheFromWritable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|entry
argument_list|)
argument_list|,
name|nodeData
argument_list|)
expr_stmt|;
block|}
block|}
comment|/***    * Write a table's access controls to the permissions mirror in zookeeper    * @param entry    * @param permsData    */
specifier|public
name|void
name|writeToZookeeper
parameter_list|(
name|byte
index|[]
name|entry
parameter_list|,
name|byte
index|[]
name|permsData
parameter_list|)
block|{
name|String
name|entryName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
argument_list|)
decl_stmt|;
name|String
name|zkNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|baseZNode
argument_list|,
name|ACL_NODE
argument_list|)
decl_stmt|;
name|zkNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkNode
argument_list|,
name|entryName
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|zkNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|updateExistingNodeData
argument_list|(
name|watcher
argument_list|,
name|zkNode
argument_list|,
name|permsData
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
literal|"Failed updating permissions for entry '"
operator|+
name|entryName
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Failed writing node "
operator|+
name|zkNode
operator|+
literal|" to zookeeper"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

