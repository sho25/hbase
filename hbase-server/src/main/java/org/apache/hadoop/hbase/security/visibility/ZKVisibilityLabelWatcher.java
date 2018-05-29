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
name|security
operator|.
name|visibility
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * A zk watcher that watches the labels table znode. This would create a znode  * /hbase/visibility_labels and will have a serialized form of a set of labels in the system.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKVisibilityLabelWatcher
extends|extends
name|ZKListener
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ZKVisibilityLabelWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VISIBILITY_LABEL_ZK_PATH
init|=
literal|"zookeeper.znode.visibility.label.parent"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_VISIBILITY_LABEL_NODE
init|=
literal|"visibility/labels"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VISIBILITY_USER_AUTHS_ZK_PATH
init|=
literal|"zookeeper.znode.visibility.user.auths.parent"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_VISIBILITY_USER_AUTHS_NODE
init|=
literal|"visibility/user_auths"
decl_stmt|;
specifier|private
name|VisibilityLabelsCache
name|labelsCache
decl_stmt|;
specifier|private
name|String
name|labelZnode
decl_stmt|;
specifier|private
name|String
name|userAuthsZnode
decl_stmt|;
specifier|public
name|ZKVisibilityLabelWatcher
parameter_list|(
name|ZKWatcher
name|watcher
parameter_list|,
name|VisibilityLabelsCache
name|labelsCache
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
name|labelsCache
operator|=
name|labelsCache
expr_stmt|;
name|String
name|labelZnodeParent
init|=
name|conf
operator|.
name|get
argument_list|(
name|VISIBILITY_LABEL_ZK_PATH
argument_list|,
name|DEFAULT_VISIBILITY_LABEL_NODE
argument_list|)
decl_stmt|;
name|String
name|userAuthsZnodeParent
init|=
name|conf
operator|.
name|get
argument_list|(
name|VISIBILITY_USER_AUTHS_ZK_PATH
argument_list|,
name|DEFAULT_VISIBILITY_USER_AUTHS_NODE
argument_list|)
decl_stmt|;
name|this
operator|.
name|labelZnode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|baseZNode
argument_list|,
name|labelZnodeParent
argument_list|)
expr_stmt|;
name|this
operator|.
name|userAuthsZnode
operator|=
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|getZNodePaths
argument_list|()
operator|.
name|baseZNode
argument_list|,
name|userAuthsZnodeParent
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
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|labelZnode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|userAuthsZnode
argument_list|)
expr_stmt|;
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
name|labelZnode
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|!=
literal|null
operator|&&
name|data
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|refreshVisibilityLabelsCache
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
name|data
operator|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|watcher
argument_list|,
name|userAuthsZnode
argument_list|)
expr_stmt|;
if|if
condition|(
name|data
operator|!=
literal|null
operator|&&
name|data
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|refreshUserAuthsCache
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|refreshVisibilityLabelsCache
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|labelsCache
operator|.
name|refreshLabelsCache
argument_list|(
name|data
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
literal|"Failed parsing data from labels table "
operator|+
literal|" from zk"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|refreshUserAuthsCache
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|labelsCache
operator|.
name|refreshUserAuthsCache
argument_list|(
name|data
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
literal|"Failed parsing data from labels table "
operator|+
literal|" from zk"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
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
name|labelZnode
argument_list|)
operator|||
name|path
operator|.
name|equals
argument_list|(
name|userAuthsZnode
argument_list|)
condition|)
block|{
try|try
block|{
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|watcher
argument_list|,
name|path
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
literal|"Error setting watcher on node "
operator|+
name|path
argument_list|,
name|ke
argument_list|)
expr_stmt|;
comment|// only option is to abort
name|watcher
operator|.
name|abort
argument_list|(
literal|"ZooKeeper error obtaining label node children"
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
comment|// There is no case of visibility labels path to get deleted.
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
name|path
operator|.
name|equals
argument_list|(
name|labelZnode
argument_list|)
operator|||
name|path
operator|.
name|equals
argument_list|(
name|userAuthsZnode
argument_list|)
condition|)
block|{
try|try
block|{
name|watcher
operator|.
name|sync
argument_list|(
name|path
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|labelZnode
argument_list|)
condition|)
block|{
name|refreshVisibilityLabelsCache
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|refreshUserAuthsCache
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
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
name|path
argument_list|,
name|ke
argument_list|)
expr_stmt|;
comment|// only option is to abort
name|watcher
operator|.
name|abort
argument_list|(
literal|"ZooKeeper error getting data for node "
operator|+
name|path
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
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// We are not dealing with child nodes under the label znode or userauths znode.
block|}
comment|/**    * Write a labels mirror or user auths mirror into zookeeper    *    * @param data    * @param labelsOrUserAuths true for writing labels and false for user auths.    */
specifier|public
name|void
name|writeToZookeeper
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|boolean
name|labelsOrUserAuths
parameter_list|)
block|{
name|String
name|znode
init|=
name|this
operator|.
name|labelZnode
decl_stmt|;
if|if
condition|(
operator|!
name|labelsOrUserAuths
condition|)
block|{
name|znode
operator|=
name|this
operator|.
name|userAuthsZnode
expr_stmt|;
block|}
try|try
block|{
name|ZKUtil
operator|.
name|updateExistingNodeData
argument_list|(
name|watcher
argument_list|,
name|znode
argument_list|,
name|data
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
literal|"Failed writing to "
operator|+
name|znode
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
name|znode
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

