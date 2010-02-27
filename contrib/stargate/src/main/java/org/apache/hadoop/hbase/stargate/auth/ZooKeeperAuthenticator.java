begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
operator|.
name|auth
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
name|stargate
operator|.
name|Constants
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
name|ZooKeeperWrapper
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
name|CreateMode
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
name|WatchedEvent
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
name|Watcher
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
name|ZooKeeper
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
name|ZooDefs
operator|.
name|Ids
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
name|data
operator|.
name|Stat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
import|;
end_import

begin_comment
comment|/**  * A simple authenticator module for ZooKeeper.  *<pre>  *   /stargate/  *     users/  *&lt;token&gt;</pre>   * Where<tt>&lt;token&gt;</tt> is a JSON formatted user record with the keys  * 'name' (String, required), 'token' (String, optional), 'admin' (boolean,  * optional), and 'disabled' (boolean, optional).  */
end_comment

begin_class
specifier|public
class|class
name|ZooKeeperAuthenticator
extends|extends
name|Authenticator
implements|implements
name|Constants
block|{
specifier|final
name|String
name|usersZNode
decl_stmt|;
name|ZooKeeperWrapper
name|wrapper
decl_stmt|;
specifier|private
name|boolean
name|ensureParentExists
parameter_list|(
specifier|final
name|String
name|znode
parameter_list|)
block|{
name|int
name|index
init|=
name|znode
operator|.
name|lastIndexOf
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<=
literal|0
condition|)
block|{
comment|// Parent is root, which always exists.
return|return
literal|true
return|;
block|}
return|return
name|ensureExists
argument_list|(
name|znode
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|ensureExists
parameter_list|(
specifier|final
name|String
name|znode
parameter_list|)
block|{
name|ZooKeeper
name|zk
init|=
name|wrapper
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
try|try
block|{
name|Stat
name|stat
init|=
name|zk
operator|.
name|exists
argument_list|(
name|znode
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|stat
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
name|zk
operator|.
name|create
argument_list|(
name|znode
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NodeExistsException
name|e
parameter_list|)
block|{
return|return
literal|true
return|;
comment|// ok, move on.
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|e
parameter_list|)
block|{
return|return
name|ensureParentExists
argument_list|(
name|znode
argument_list|)
operator|&&
name|ensureExists
argument_list|(
name|znode
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{     }
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{     }
return|return
literal|false
return|;
block|}
comment|/**    * Constructor    * @param conf    * @throws IOException    */
specifier|public
name|ZooKeeperAuthenticator
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
operator|new
name|ZooKeeperWrapper
argument_list|(
name|conf
argument_list|,
operator|new
name|Watcher
argument_list|()
block|{
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{ }
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|ensureExists
argument_list|(
name|USERS_ZNODE_ROOT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param conf    * @param wrapper    */
specifier|public
name|ZooKeeperAuthenticator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeperWrapper
name|wrapper
parameter_list|)
block|{
name|this
operator|.
name|usersZNode
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"stargate.auth.zk.users"
argument_list|,
name|USERS_ZNODE_ROOT
argument_list|)
expr_stmt|;
name|this
operator|.
name|wrapper
operator|=
name|wrapper
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|User
name|getUserForToken
parameter_list|(
name|String
name|token
parameter_list|)
throws|throws
name|IOException
block|{
name|ZooKeeper
name|zk
init|=
name|wrapper
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
name|zk
operator|.
name|getData
argument_list|(
name|usersZNode
operator|+
literal|"/"
operator|+
name|token
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|JSONObject
name|o
init|=
operator|new
name|JSONObject
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|o
operator|.
name|has
argument_list|(
literal|"name"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"invalid record, missing 'name'"
argument_list|)
throw|;
block|}
name|String
name|name
init|=
name|o
operator|.
name|getString
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|boolean
name|admin
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|o
operator|.
name|has
argument_list|(
literal|"admin"
argument_list|)
condition|)
block|{
name|admin
operator|=
name|o
operator|.
name|getBoolean
argument_list|(
literal|"admin"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|disabled
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|o
operator|.
name|has
argument_list|(
literal|"disabled"
argument_list|)
condition|)
block|{
name|disabled
operator|=
name|o
operator|.
name|getBoolean
argument_list|(
literal|"disabled"
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|User
argument_list|(
name|name
argument_list|,
name|token
argument_list|,
name|admin
argument_list|,
name|disabled
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

