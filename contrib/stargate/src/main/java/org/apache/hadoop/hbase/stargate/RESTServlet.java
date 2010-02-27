begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|hbase
operator|.
name|Chore
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
name|HColumnDescriptor
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
name|HTableInterface
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
name|HTablePool
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
name|auth
operator|.
name|Authenticator
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
name|auth
operator|.
name|HBCAuthenticator
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
name|auth
operator|.
name|HTableAuthenticator
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
name|auth
operator|.
name|JDBCAuthenticator
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
name|auth
operator|.
name|ZooKeeperAuthenticator
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
name|metrics
operator|.
name|StargateMetrics
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
name|util
operator|.
name|Pair
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
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
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
name|Watcher
operator|.
name|Event
operator|.
name|EventType
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
operator|.
name|Event
operator|.
name|KeeperState
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
name|JSONStringer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|server
operator|.
name|impl
operator|.
name|container
operator|.
name|servlet
operator|.
name|ServletAdaptor
import|;
end_import

begin_comment
comment|/**  * Singleton class encapsulating global REST servlet state and functions.  */
end_comment

begin_class
specifier|public
class|class
name|RESTServlet
extends|extends
name|ServletAdaptor
implements|implements
name|Constants
implements|,
name|Watcher
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
name|RESTServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
name|RESTServlet
name|instance
decl_stmt|;
class|class
name|StatusReporter
extends|extends
name|Chore
block|{
specifier|public
name|StatusReporter
parameter_list|(
name|int
name|period
parameter_list|,
name|AtomicBoolean
name|stopping
parameter_list|)
block|{
name|super
argument_list|(
name|period
argument_list|,
name|stopping
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
if|if
condition|(
name|wrapper
operator|!=
literal|null
condition|)
try|try
block|{
name|JSONStringer
name|status
init|=
operator|new
name|JSONStringer
argument_list|()
decl_stmt|;
name|status
operator|.
name|object
argument_list|()
expr_stmt|;
name|status
operator|.
name|key
argument_list|(
literal|"requests"
argument_list|)
operator|.
name|value
argument_list|(
name|metrics
operator|.
name|getRequests
argument_list|()
argument_list|)
expr_stmt|;
name|status
operator|.
name|key
argument_list|(
literal|"connectors"
argument_list|)
operator|.
name|array
argument_list|()
expr_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|e
range|:
name|connectors
control|)
block|{
name|status
operator|.
name|object
argument_list|()
operator|.
name|key
argument_list|(
literal|"host"
argument_list|)
operator|.
name|value
argument_list|(
name|e
operator|.
name|getFirst
argument_list|()
argument_list|)
operator|.
name|key
argument_list|(
literal|"port"
argument_list|)
operator|.
name|value
argument_list|(
name|e
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|status
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|status
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|updateNode
argument_list|(
name|wrapper
argument_list|,
name|znode
argument_list|,
name|CreateMode
operator|.
name|EPHEMERAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|status
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|final
name|String
name|znode
init|=
name|INSTANCE_ZNODE_ROOT
operator|+
literal|"/"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|transient
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|transient
specifier|final
name|HTablePool
name|pool
decl_stmt|;
specifier|transient
specifier|volatile
name|ZooKeeperWrapper
name|wrapper
decl_stmt|;
specifier|transient
name|Chore
name|statusReporter
decl_stmt|;
specifier|transient
name|Authenticator
name|authenticator
decl_stmt|;
name|AtomicBoolean
name|stopping
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|multiuser
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|maxAgeMap
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|connectors
init|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|StargateMetrics
name|metrics
init|=
operator|new
name|StargateMetrics
argument_list|()
decl_stmt|;
comment|/**    * @return the RESTServlet singleton instance    * @throws IOException    */
specifier|public
specifier|synchronized
specifier|static
name|RESTServlet
name|getInstance
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|RESTServlet
argument_list|()
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|static
name|boolean
name|ensureExists
parameter_list|(
specifier|final
name|ZooKeeperWrapper
name|zkw
parameter_list|,
specifier|final
name|String
name|znode
parameter_list|,
specifier|final
name|CreateMode
name|mode
parameter_list|)
throws|throws
name|IOException
block|{
name|ZooKeeper
name|zk
init|=
name|zkw
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
name|mode
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created ZNode "
operator|+
name|znode
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
name|zkw
argument_list|,
name|znode
argument_list|,
name|mode
argument_list|)
operator|&&
name|ensureExists
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|mode
argument_list|)
return|;
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
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
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
specifier|static
name|boolean
name|ensureParentExists
parameter_list|(
specifier|final
name|ZooKeeperWrapper
name|zkw
parameter_list|,
specifier|final
name|String
name|znode
parameter_list|,
specifier|final
name|CreateMode
name|mode
parameter_list|)
throws|throws
name|IOException
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
name|zkw
argument_list|,
name|znode
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
argument_list|,
name|mode
argument_list|)
return|;
block|}
specifier|static
name|void
name|updateNode
parameter_list|(
specifier|final
name|ZooKeeperWrapper
name|zkw
parameter_list|,
specifier|final
name|String
name|znode
parameter_list|,
specifier|final
name|CreateMode
name|mode
parameter_list|,
specifier|final
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureExists
argument_list|(
name|zkw
argument_list|,
name|znode
argument_list|,
name|mode
argument_list|)
expr_stmt|;
name|ZooKeeper
name|zk
init|=
name|zkw
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
try|try
block|{
name|zk
operator|.
name|setData
argument_list|(
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
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
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
name|ZooKeeperWrapper
name|initZooKeeperWrapper
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ZooKeeperWrapper
argument_list|(
name|conf
argument_list|,
name|this
argument_list|)
return|;
block|}
comment|/**    * Constructor    * @throws IOException    */
specifier|public
name|RESTServlet
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|this
operator|.
name|pool
operator|=
operator|new
name|HTablePool
argument_list|(
name|conf
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|this
operator|.
name|wrapper
operator|=
name|initZooKeeperWrapper
argument_list|()
expr_stmt|;
name|this
operator|.
name|statusReporter
operator|=
operator|new
name|StatusReporter
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|STATUS_REPORT_PERIOD_KEY
argument_list|,
literal|1000
operator|*
literal|60
argument_list|)
argument_list|,
name|stopping
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
operator|(
literal|"ZooKeeper.Watcher event "
operator|+
name|event
operator|.
name|getType
argument_list|()
operator|+
literal|" with path "
operator|+
name|event
operator|.
name|getPath
argument_list|()
operator|)
argument_list|)
expr_stmt|;
comment|// handle disconnection (or manual delete to test disconnection scenario)
if|if
condition|(
name|event
operator|.
name|getState
argument_list|()
operator|==
name|KeeperState
operator|.
name|Expired
operator|||
operator|(
name|event
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|NodeDeleted
argument_list|)
operator|&&
name|event
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|znode
argument_list|)
operator|)
condition|)
block|{
name|wrapper
operator|.
name|close
argument_list|()
expr_stmt|;
name|wrapper
operator|=
literal|null
expr_stmt|;
while|while
condition|(
operator|!
name|stopping
operator|.
name|get
argument_list|()
condition|)
try|try
block|{
name|wrapper
operator|=
name|initZooKeeperWrapper
argument_list|()
expr_stmt|;
break|break;
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{         }
block|}
block|}
block|}
name|HTablePool
name|getTablePool
parameter_list|()
block|{
return|return
name|pool
return|;
block|}
name|ZooKeeperWrapper
name|getZooKeeperWrapper
parameter_list|()
block|{
return|return
name|wrapper
return|;
block|}
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
name|StargateMetrics
name|getMetrics
parameter_list|()
block|{
return|return
name|metrics
return|;
block|}
name|void
name|addConnectorAddress
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|connectors
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param tableName the table name    * @return the maximum cache age suitable for use with this table, in    *  seconds     * @throws IOException    */
specifier|public
name|int
name|getMaxAge
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Integer
name|i
init|=
name|maxAgeMap
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|!=
literal|null
condition|)
block|{
return|return
name|i
operator|.
name|intValue
argument_list|()
return|;
block|}
name|HTableInterface
name|table
init|=
name|pool
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|maxAge
init|=
name|DEFAULT_MAX_AGE
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|family
range|:
name|table
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getFamilies
argument_list|()
control|)
block|{
name|int
name|ttl
init|=
name|family
operator|.
name|getTimeToLive
argument_list|()
decl_stmt|;
if|if
condition|(
name|ttl
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|ttl
operator|<
name|maxAge
condition|)
block|{
name|maxAge
operator|=
name|ttl
expr_stmt|;
block|}
block|}
name|maxAgeMap
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|maxAge
argument_list|)
expr_stmt|;
return|return
name|maxAge
return|;
block|}
finally|finally
block|{
name|pool
operator|.
name|putTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Signal that a previously calculated maximum cache age has been    * invalidated by a schema change.    * @param tableName the table name    */
specifier|public
name|void
name|invalidateMaxAge
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|maxAgeMap
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return true if the servlet should operate in multiuser mode    */
specifier|public
name|boolean
name|isMultiUser
parameter_list|()
block|{
return|return
name|multiuser
return|;
block|}
comment|/**    * @param flag true if the servlet should operate in multiuser mode     */
specifier|public
name|void
name|setMultiUser
parameter_list|(
name|boolean
name|multiuser
parameter_list|)
block|{
name|this
operator|.
name|multiuser
operator|=
name|multiuser
expr_stmt|;
block|}
comment|/**    * @return an authenticator    */
specifier|public
name|Authenticator
name|getAuthenticator
parameter_list|()
block|{
if|if
condition|(
name|authenticator
operator|==
literal|null
condition|)
block|{
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|AUTHENTICATOR_KEY
argument_list|,
name|HBCAuthenticator
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|endsWith
argument_list|(
name|HBCAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|className
operator|.
name|endsWith
argument_list|(
name|HTableAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|className
operator|.
name|endsWith
argument_list|(
name|JDBCAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|cons
init|=
name|c
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|authenticator
operator|=
operator|(
name|Authenticator
operator|)
name|cons
operator|.
name|newInstance
argument_list|(
operator|new
name|Object
index|[]
block|{
name|conf
block|}
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|className
operator|.
name|endsWith
argument_list|(
name|ZooKeeperAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|cons
init|=
name|c
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|,
name|ZooKeeperWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|authenticator
operator|=
operator|(
name|Authenticator
operator|)
name|cons
operator|.
name|newInstance
argument_list|(
operator|new
name|Object
index|[]
block|{
name|conf
block|,
name|wrapper
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authenticator
operator|=
operator|(
name|Authenticator
operator|)
name|c
operator|.
name|newInstance
argument_list|()
expr_stmt|;
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|authenticator
operator|==
literal|null
condition|)
block|{
name|authenticator
operator|=
operator|new
name|HBCAuthenticator
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|authenticator
return|;
block|}
comment|/**    * @param authenticator    */
specifier|public
name|void
name|setAuthenticator
parameter_list|(
name|Authenticator
name|authenticator
parameter_list|)
block|{
name|this
operator|.
name|authenticator
operator|=
name|authenticator
expr_stmt|;
block|}
block|}
end_class

end_unit

