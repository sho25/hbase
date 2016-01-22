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
name|javax
operator|.
name|crypto
operator|.
name|SecretKey
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
name|Iterator
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
name|ConcurrentHashMap
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
name|AtomicLong
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
name|EnvironmentEdgeManager
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
name|ZKClusterId
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
name|ZKLeaderManager
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
name|io
operator|.
name|Text
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
name|security
operator|.
name|token
operator|.
name|SecretManager
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
name|security
operator|.
name|token
operator|.
name|Token
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
comment|/**  * Manages an internal list of secret keys used to sign new authentication  * tokens as they are generated, and to valid existing tokens used for  * authentication.  *  *<p>  * A single instance of {@code AuthenticationTokenSecretManager} will be  * running as the "leader" in a given HBase cluster.  The leader is responsible  * for periodically generating new secret keys, which are then distributed to  * followers via ZooKeeper, and for expiring previously used secret keys that  * are no longer needed (as any tokens using them have expired).  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AuthenticationTokenSecretManager
extends|extends
name|SecretManager
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
block|{
specifier|static
specifier|final
name|String
name|NAME_PREFIX
init|=
literal|"SecretManager-"
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
name|AuthenticationTokenSecretManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|long
name|lastKeyUpdate
decl_stmt|;
specifier|private
name|long
name|keyUpdateInterval
decl_stmt|;
specifier|private
name|long
name|tokenMaxLifetime
decl_stmt|;
specifier|private
name|ZKSecretWatcher
name|zkWatcher
decl_stmt|;
specifier|private
name|LeaderElector
name|leaderElector
decl_stmt|;
specifier|private
name|ZKClusterId
name|clusterId
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|AuthenticationKey
argument_list|>
name|allKeys
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Integer
argument_list|,
name|AuthenticationKey
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|AuthenticationKey
name|currentKey
decl_stmt|;
specifier|private
name|int
name|idSeq
decl_stmt|;
specifier|private
name|AtomicLong
name|tokenSeq
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
comment|/**    * Create a new secret manager instance for generating keys.    * @param conf Configuration to use    * @param zk Connection to zookeeper for handling leader elections    * @param keyUpdateInterval Time (in milliseconds) between rolling a new master key for token signing    * @param tokenMaxLifetime Maximum age (in milliseconds) before a token expires and is no longer valid    */
comment|/* TODO: Restrict access to this constructor to make rogues instances more difficult.    * For the moment this class is instantiated from    * org.apache.hadoop.hbase.ipc.SecureServer so public access is needed.    */
specifier|public
name|AuthenticationTokenSecretManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeperWatcher
name|zk
parameter_list|,
name|String
name|serverName
parameter_list|,
name|long
name|keyUpdateInterval
parameter_list|,
name|long
name|tokenMaxLifetime
parameter_list|)
block|{
name|this
operator|.
name|zkWatcher
operator|=
operator|new
name|ZKSecretWatcher
argument_list|(
name|conf
argument_list|,
name|zk
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyUpdateInterval
operator|=
name|keyUpdateInterval
expr_stmt|;
name|this
operator|.
name|tokenMaxLifetime
operator|=
name|tokenMaxLifetime
expr_stmt|;
name|this
operator|.
name|leaderElector
operator|=
operator|new
name|LeaderElector
argument_list|(
name|zk
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|NAME_PREFIX
operator|+
name|serverName
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
operator|new
name|ZKClusterId
argument_list|(
name|zk
argument_list|,
name|zk
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
try|try
block|{
comment|// populate any existing keys
name|this
operator|.
name|zkWatcher
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// try to become leader
name|this
operator|.
name|leaderElector
operator|.
name|start
argument_list|()
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
literal|"Zookeeper initialization failed"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|this
operator|.
name|leaderElector
operator|.
name|stop
argument_list|(
literal|"SecretManager stopping"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMaster
parameter_list|()
block|{
return|return
name|leaderElector
operator|.
name|isMaster
argument_list|()
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|byte
index|[]
name|createPassword
parameter_list|(
name|AuthenticationTokenIdentifier
name|identifier
parameter_list|)
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|AuthenticationKey
name|secretKey
init|=
name|currentKey
decl_stmt|;
name|identifier
operator|.
name|setKeyId
argument_list|(
name|secretKey
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|identifier
operator|.
name|setIssueDate
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|identifier
operator|.
name|setExpirationDate
argument_list|(
name|now
operator|+
name|tokenMaxLifetime
argument_list|)
expr_stmt|;
name|identifier
operator|.
name|setSequenceNumber
argument_list|(
name|tokenSeq
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|createPassword
argument_list|(
name|identifier
operator|.
name|getBytes
argument_list|()
argument_list|,
name|secretKey
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|retrievePassword
parameter_list|(
name|AuthenticationTokenIdentifier
name|identifier
parameter_list|)
throws|throws
name|InvalidToken
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|identifier
operator|.
name|getExpirationDate
argument_list|()
operator|<
name|now
condition|)
block|{
throw|throw
operator|new
name|InvalidToken
argument_list|(
literal|"Token has expired"
argument_list|)
throw|;
block|}
name|AuthenticationKey
name|masterKey
init|=
name|allKeys
operator|.
name|get
argument_list|(
name|identifier
operator|.
name|getKeyId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|masterKey
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|zkWatcher
operator|.
name|getWatcher
argument_list|()
operator|.
name|isAborted
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZookeeperWatcher is abort"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidToken
argument_list|(
literal|"Token keys could not be sync from zookeeper"
operator|+
literal|" because of ZookeeperWatcher abort"
argument_list|)
throw|;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
operator|!
name|leaderElector
operator|.
name|isAlive
argument_list|()
operator|||
name|leaderElector
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Thread leaderElector["
operator|+
name|leaderElector
operator|.
name|getName
argument_list|()
operator|+
literal|":"
operator|+
name|leaderElector
operator|.
name|getId
argument_list|()
operator|+
literal|"] is stoped or not alive"
argument_list|)
expr_stmt|;
name|leaderElector
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Thread leaderElector ["
operator|+
name|leaderElector
operator|.
name|getName
argument_list|()
operator|+
literal|":"
operator|+
name|leaderElector
operator|.
name|getId
argument_list|()
operator|+
literal|"] is started"
argument_list|)
expr_stmt|;
block|}
block|}
name|zkWatcher
operator|.
name|refreshKeys
argument_list|()
expr_stmt|;
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
literal|"Sync token keys from zookeeper"
argument_list|)
expr_stmt|;
block|}
name|masterKey
operator|=
name|allKeys
operator|.
name|get
argument_list|(
name|identifier
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|masterKey
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidToken
argument_list|(
literal|"Unknown master key for token (id="
operator|+
name|identifier
operator|.
name|getKeyId
argument_list|()
operator|+
literal|")"
argument_list|)
throw|;
block|}
comment|// regenerate the password
return|return
name|createPassword
argument_list|(
name|identifier
operator|.
name|getBytes
argument_list|()
argument_list|,
name|masterKey
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AuthenticationTokenIdentifier
name|createIdentifier
parameter_list|()
block|{
return|return
operator|new
name|AuthenticationTokenIdentifier
argument_list|()
return|;
block|}
specifier|public
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|generateToken
parameter_list|(
name|String
name|username
parameter_list|)
block|{
name|AuthenticationTokenIdentifier
name|ident
init|=
operator|new
name|AuthenticationTokenIdentifier
argument_list|(
name|username
argument_list|)
decl_stmt|;
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
init|=
operator|new
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
argument_list|(
name|ident
argument_list|,
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterId
operator|.
name|hasId
argument_list|()
condition|)
block|{
name|token
operator|.
name|setService
argument_list|(
operator|new
name|Text
argument_list|(
name|clusterId
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|token
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|addKey
parameter_list|(
name|AuthenticationKey
name|key
parameter_list|)
throws|throws
name|IOException
block|{
comment|// ignore zk changes when running as master
if|if
condition|(
name|leaderElector
operator|.
name|isMaster
argument_list|()
condition|)
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
literal|"Running as master, ignoring new key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
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
literal|"Adding key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|allKeys
operator|.
name|put
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentKey
operator|==
literal|null
operator|||
name|key
operator|.
name|getKeyId
argument_list|()
operator|>
name|currentKey
operator|.
name|getKeyId
argument_list|()
condition|)
block|{
name|currentKey
operator|=
name|key
expr_stmt|;
block|}
comment|// update current sequence
if|if
condition|(
name|key
operator|.
name|getKeyId
argument_list|()
operator|>
name|idSeq
condition|)
block|{
name|idSeq
operator|=
name|key
operator|.
name|getKeyId
argument_list|()
expr_stmt|;
block|}
block|}
specifier|synchronized
name|boolean
name|removeKey
parameter_list|(
name|Integer
name|keyId
parameter_list|)
block|{
comment|// ignore zk changes when running as master
if|if
condition|(
name|leaderElector
operator|.
name|isMaster
argument_list|()
condition|)
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
literal|"Running as master, ignoring removed key "
operator|+
name|keyId
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
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
literal|"Removing key "
operator|+
name|keyId
argument_list|)
expr_stmt|;
block|}
name|allKeys
operator|.
name|remove
argument_list|(
name|keyId
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|synchronized
name|AuthenticationKey
name|getCurrentKey
parameter_list|()
block|{
return|return
name|currentKey
return|;
block|}
name|AuthenticationKey
name|getKey
parameter_list|(
name|int
name|keyId
parameter_list|)
block|{
return|return
name|allKeys
operator|.
name|get
argument_list|(
name|keyId
argument_list|)
return|;
block|}
specifier|synchronized
name|void
name|removeExpiredKeys
parameter_list|()
block|{
if|if
condition|(
operator|!
name|leaderElector
operator|.
name|isMaster
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping removeExpiredKeys() because not running as master."
argument_list|)
expr_stmt|;
return|return;
block|}
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|AuthenticationKey
argument_list|>
name|iter
init|=
name|allKeys
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|AuthenticationKey
name|key
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|getExpiration
argument_list|()
operator|<
name|now
condition|)
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
literal|"Removing expired key "
operator|+
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
name|zkWatcher
operator|.
name|removeKeyFromZK
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|synchronized
name|boolean
name|isCurrentKeyRolled
parameter_list|()
block|{
return|return
name|currentKey
operator|!=
literal|null
return|;
block|}
specifier|synchronized
name|void
name|rollCurrentKey
parameter_list|()
block|{
if|if
condition|(
operator|!
name|leaderElector
operator|.
name|isMaster
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping rollCurrentKey() because not running as master."
argument_list|)
expr_stmt|;
return|return;
block|}
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|AuthenticationKey
name|prev
init|=
name|currentKey
decl_stmt|;
name|AuthenticationKey
name|newKey
init|=
operator|new
name|AuthenticationKey
argument_list|(
operator|++
name|idSeq
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
comment|// don't allow to expire until it's replaced by a new key
name|generateSecret
argument_list|()
argument_list|)
decl_stmt|;
name|allKeys
operator|.
name|put
argument_list|(
name|newKey
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|newKey
argument_list|)
expr_stmt|;
name|currentKey
operator|=
name|newKey
expr_stmt|;
name|zkWatcher
operator|.
name|addKeyToZK
argument_list|(
name|newKey
argument_list|)
expr_stmt|;
name|lastKeyUpdate
operator|=
name|now
expr_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
comment|// make sure previous key is still stored
name|prev
operator|.
name|setExpiration
argument_list|(
name|now
operator|+
name|tokenMaxLifetime
argument_list|)
expr_stmt|;
name|allKeys
operator|.
name|put
argument_list|(
name|prev
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|prev
argument_list|)
expr_stmt|;
name|zkWatcher
operator|.
name|updateKeyInZK
argument_list|(
name|prev
argument_list|)
expr_stmt|;
block|}
block|}
specifier|synchronized
name|long
name|getLastKeyUpdate
parameter_list|()
block|{
return|return
name|lastKeyUpdate
return|;
block|}
specifier|public
specifier|static
name|SecretKey
name|createSecretKey
parameter_list|(
name|byte
index|[]
name|raw
parameter_list|)
block|{
return|return
name|SecretManager
operator|.
name|createSecretKey
argument_list|(
name|raw
argument_list|)
return|;
block|}
specifier|private
class|class
name|LeaderElector
extends|extends
name|Thread
implements|implements
name|Stoppable
block|{
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
comment|/** Flag indicating whether we're in charge of rolling/expiring keys */
specifier|private
name|boolean
name|isMaster
init|=
literal|false
decl_stmt|;
specifier|private
name|ZKLeaderManager
name|zkLeader
decl_stmt|;
specifier|public
name|LeaderElector
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|serverName
parameter_list|)
block|{
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setName
argument_list|(
literal|"ZKSecretWatcher-leaderElector"
argument_list|)
expr_stmt|;
name|zkLeader
operator|=
operator|new
name|ZKLeaderManager
argument_list|(
name|watcher
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|getRootKeyZNode
argument_list|()
argument_list|,
literal|"keymaster"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|serverName
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMaster
parameter_list|()
block|{
return|return
name|isMaster
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|stopped
condition|)
block|{
return|return;
block|}
name|stopped
operator|=
literal|true
expr_stmt|;
comment|// prevent further key generation when stopping
if|if
condition|(
name|isMaster
condition|)
block|{
name|zkLeader
operator|.
name|stepDownAsLeader
argument_list|()
expr_stmt|;
block|}
name|isMaster
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping leader election, because: "
operator|+
name|reason
argument_list|)
expr_stmt|;
name|interrupt
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|zkLeader
operator|.
name|start
argument_list|()
expr_stmt|;
name|zkLeader
operator|.
name|waitToBecomeLeader
argument_list|()
expr_stmt|;
name|isMaster
operator|=
literal|true
expr_stmt|;
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// clear any expired
name|removeExpiredKeys
argument_list|()
expr_stmt|;
name|long
name|localLastKeyUpdate
init|=
name|getLastKeyUpdate
argument_list|()
decl_stmt|;
if|if
condition|(
name|localLastKeyUpdate
operator|+
name|keyUpdateInterval
operator|<
name|now
condition|)
block|{
comment|// roll a new master key
name|rollCurrentKey
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
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
literal|"Interrupted waiting for next update"
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

