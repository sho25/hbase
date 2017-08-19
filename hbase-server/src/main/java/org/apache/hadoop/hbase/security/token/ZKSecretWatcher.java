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
name|token
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
name|annotations
operator|.
name|VisibleForTesting
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
name|util
operator|.
name|Writables
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

begin_comment
comment|/**  * Synchronizes token encryption keys across cluster nodes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKSecretWatcher
extends|extends
name|ZooKeeperListener
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_ROOT_NODE
init|=
literal|"tokenauth"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_KEYS_PARENT
init|=
literal|"keys"
decl_stmt|;
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
name|ZKSecretWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AuthenticationTokenSecretManager
name|secretManager
decl_stmt|;
specifier|private
name|String
name|baseKeyZNode
decl_stmt|;
specifier|private
name|String
name|keysParentZNode
decl_stmt|;
specifier|public
name|ZKSecretWatcher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|AuthenticationTokenSecretManager
name|secretManager
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|secretManager
operator|=
name|secretManager
expr_stmt|;
name|String
name|keyZNodeParent
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"zookeeper.znode.tokenauth.parent"
argument_list|,
name|DEFAULT_ROOT_NODE
argument_list|)
decl_stmt|;
name|this
operator|.
name|baseKeyZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|,
name|keyZNodeParent
argument_list|)
expr_stmt|;
name|this
operator|.
name|keysParentZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseKeyZNode
argument_list|,
name|DEFAULT_KEYS_PARENT
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
comment|// make sure the base node exists
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|keysParentZNode
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
name|keysParentZNode
argument_list|)
condition|)
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
name|keysParentZNode
argument_list|)
decl_stmt|;
name|refreshNodes
argument_list|(
name|nodes
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
name|keysParentZNode
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
name|keysParentZNode
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
name|fatal
argument_list|(
literal|"Error reading data from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error reading new key znode "
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
name|nodeDeleted
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|keysParentZNode
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
name|keyId
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
name|Integer
name|id
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|keyId
argument_list|)
decl_stmt|;
name|secretManager
operator|.
name|removeKey
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Invalid znode name for key ID '"
operator|+
name|keyId
operator|+
literal|"'"
argument_list|,
name|nfe
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
name|keysParentZNode
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
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring empty node "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return;
block|}
name|AuthenticationKey
name|key
init|=
operator|(
name|AuthenticationKey
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|data
argument_list|,
operator|new
name|AuthenticationKey
argument_list|()
argument_list|)
decl_stmt|;
name|secretManager
operator|.
name|addKey
argument_list|(
name|key
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
name|fatal
argument_list|(
literal|"Error reading data from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error reading updated key znode "
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
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Error reading key writables"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error reading key writables from znode "
operator|+
name|path
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
name|keysParentZNode
argument_list|)
condition|)
block|{
comment|// keys changed
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
name|keysParentZNode
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
name|fatal
argument_list|(
literal|"Error reading data from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error reading changed keys from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|String
name|getRootKeyZNode
parameter_list|()
block|{
return|return
name|baseKeyZNode
return|;
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
name|String
name|path
init|=
name|n
operator|.
name|getNode
argument_list|()
decl_stmt|;
name|String
name|keyId
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
name|n
operator|.
name|getData
argument_list|()
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
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring empty node "
operator|+
name|path
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|AuthenticationKey
name|key
init|=
operator|(
name|AuthenticationKey
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|data
argument_list|,
operator|new
name|AuthenticationKey
argument_list|()
argument_list|)
decl_stmt|;
name|secretManager
operator|.
name|addKey
argument_list|(
name|key
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
name|fatal
argument_list|(
literal|"Failed reading new secret key for id '"
operator|+
name|keyId
operator|+
literal|"' from zk"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error deserializing key from znode "
operator|+
name|path
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|String
name|getKeyNode
parameter_list|(
name|int
name|keyId
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|keysParentZNode
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|keyId
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|removeKeyFromZK
parameter_list|(
name|AuthenticationKey
name|key
parameter_list|)
block|{
name|String
name|keyZNode
init|=
name|getKeyNode
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
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
name|keyZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Non-existent znode "
operator|+
name|keyZNode
operator|+
literal|" for key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|nne
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
name|fatal
argument_list|(
literal|"Failed removing znode "
operator|+
name|keyZNode
operator|+
literal|" for key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Unhandled zookeeper error removing znode "
operator|+
name|keyZNode
operator|+
literal|" for key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addKeyToZK
parameter_list|(
name|AuthenticationKey
name|key
parameter_list|)
block|{
name|String
name|keyZNode
init|=
name|getKeyNode
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|keyData
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// TODO: is there any point in retrying beyond what ZK client does?
name|ZKUtil
operator|.
name|createSetData
argument_list|(
name|watcher
argument_list|,
name|keyZNode
argument_list|,
name|keyData
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
name|fatal
argument_list|(
literal|"Unable to synchronize master key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
operator|+
literal|" to znode "
operator|+
name|keyZNode
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Unable to synchronize secret key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
operator|+
literal|" in zookeeper"
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
comment|// this can only happen from an error serializing the key
name|watcher
operator|.
name|abort
argument_list|(
literal|"Failed serializing key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|updateKeyInZK
parameter_list|(
name|AuthenticationKey
name|key
parameter_list|)
block|{
name|String
name|keyZNode
init|=
name|getKeyNode
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|keyData
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|updateExistingNodeData
argument_list|(
name|watcher
argument_list|,
name|keyZNode
argument_list|,
name|keyData
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|ne
parameter_list|)
block|{
comment|// node was somehow removed, try adding it back
name|ZKUtil
operator|.
name|createSetData
argument_list|(
name|watcher
argument_list|,
name|keyZNode
argument_list|,
name|keyData
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
name|fatal
argument_list|(
literal|"Unable to update master key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
operator|+
literal|" in znode "
operator|+
name|keyZNode
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Unable to synchronize secret key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
operator|+
literal|" in zookeeper"
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
comment|// this can only happen from an error serializing the key
name|watcher
operator|.
name|abort
argument_list|(
literal|"Failed serializing key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * refresh keys    */
specifier|synchronized
name|void
name|refreshKeys
parameter_list|()
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
name|keysParentZNode
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
name|fatal
argument_list|(
literal|"Error reading data from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|abort
argument_list|(
literal|"Error reading changed keys from zookeeper"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * get token keys parent node    * @return token keys parent node    */
annotation|@
name|VisibleForTesting
name|String
name|getKeysParentZNode
parameter_list|()
block|{
return|return
name|keysParentZNode
return|;
block|}
block|}
end_class

end_unit

