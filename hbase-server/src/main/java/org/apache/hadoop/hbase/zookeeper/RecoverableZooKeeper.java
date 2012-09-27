begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
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
name|management
operator|.
name|ManagementFactory
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
name|hadoop
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
name|classification
operator|.
name|InterfaceStability
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
name|RetryCounter
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
name|RetryCounterFactory
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
name|AsyncCallback
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
name|ZooKeeper
operator|.
name|States
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
name|ACL
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

begin_comment
comment|/**  * A zookeeper that can handle 'recoverable' errors.  * To handle recoverable errors, developers need to realize that there are two   * classes of requests: idempotent and non-idempotent requests. Read requests   * and unconditional sets and deletes are examples of idempotent requests, they   * can be reissued with the same results.   * (Although, the delete may throw a NoNodeException on reissue its effect on   * the ZooKeeper state is the same.) Non-idempotent requests need special   * handling, application and library writers need to keep in mind that they may   * need to encode information in the data or name of znodes to detect   * retries. A simple example is a create that uses a sequence flag.   * If a process issues a create("/x-", ..., SEQUENCE) and gets a connection   * loss exception, that process will reissue another   * create("/x-", ..., SEQUENCE) and get back x-111. When the process does a   * getChildren("/"), it sees x-1,x-30,x-109,x-110,x-111, now it could be   * that x-109 was the result of the previous create, so the process actually   * owns both x-109 and x-111. An easy way around this is to use "x-process id-"   * when doing the create. If the process is using an id of 352, before reissuing  * the create it will do a getChildren("/") and see "x-222-1", "x-542-30",   * "x-352-109", x-333-110". The process will know that the original create   * succeeded an the znode it created is "x-352-109".  * @see "http://wiki.apache.org/hadoop/ZooKeeper/ErrorHandling"  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|RecoverableZooKeeper
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
name|RecoverableZooKeeper
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// the actual ZooKeeper client instance
specifier|private
name|ZooKeeper
name|zk
decl_stmt|;
specifier|private
specifier|final
name|RetryCounterFactory
name|retryCounterFactory
decl_stmt|;
comment|// An identifier of this process in the cluster
specifier|private
specifier|final
name|String
name|identifier
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|id
decl_stmt|;
specifier|private
name|Watcher
name|watcher
decl_stmt|;
specifier|private
name|int
name|sessionTimeout
decl_stmt|;
specifier|private
name|String
name|quorumServers
decl_stmt|;
comment|// The metadata attached to each piece of data has the
comment|// format:
comment|//<magic> 1-byte constant
comment|//<id length> 4-byte big-endian integer (length of next field)
comment|//<id> identifier corresponding uniquely to this process
comment|// It is prepended to the data supplied by the user.
comment|// the magic number is to be backward compatible
specifier|private
specifier|static
specifier|final
name|byte
name|MAGIC
init|=
operator|(
name|byte
operator|)
literal|0XFF
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAGIC_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_BYTE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ID_LENGTH_OFFSET
init|=
name|MAGIC_SIZE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ID_LENGTH_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_INT
decl_stmt|;
specifier|public
name|RecoverableZooKeeper
parameter_list|(
name|String
name|quorumServers
parameter_list|,
name|int
name|sessionTimeout
parameter_list|,
name|Watcher
name|watcher
parameter_list|,
name|int
name|maxRetries
parameter_list|,
name|int
name|retryIntervalMillis
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|zk
operator|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryCounterFactory
operator|=
operator|new
name|RetryCounterFactory
argument_list|(
name|maxRetries
argument_list|,
name|retryIntervalMillis
argument_list|)
expr_stmt|;
comment|// the identifier = processID@hostName
name|this
operator|.
name|identifier
operator|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The identifier of this process is "
operator|+
name|identifier
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|identifier
argument_list|)
expr_stmt|;
name|this
operator|.
name|watcher
operator|=
name|watcher
expr_stmt|;
name|this
operator|.
name|sessionTimeout
operator|=
name|sessionTimeout
expr_stmt|;
name|this
operator|.
name|quorumServers
operator|=
name|quorumServers
expr_stmt|;
block|}
specifier|public
name|void
name|reconnectAfterExpiration
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing dead ZooKeeper connection, session"
operator|+
literal|" was: 0x"
operator|+
name|Long
operator|.
name|toHexString
argument_list|(
name|zk
operator|.
name|getSessionId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|zk
operator|=
operator|new
name|ZooKeeper
argument_list|(
name|this
operator|.
name|quorumServers
argument_list|,
name|this
operator|.
name|sessionTimeout
argument_list|,
name|this
operator|.
name|watcher
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Recreated a ZooKeeper, session"
operator|+
literal|" is: 0x"
operator|+
name|Long
operator|.
name|toHexString
argument_list|(
name|zk
operator|.
name|getSessionId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * delete is an idempotent operation. Retry before throwing exception.    * This function will not throw NoNodeException if the path does not    * exist.    */
specifier|public
name|void
name|delete
parameter_list|(
name|String
name|path
parameter_list|,
name|int
name|version
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|KeeperException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
name|boolean
name|isRetry
init|=
literal|false
decl_stmt|;
comment|// False for first attempt, true for all retries.
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|zk
operator|.
name|delete
argument_list|(
name|path
argument_list|,
name|version
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|NONODE
case|:
if|if
condition|(
name|isRetry
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Node "
operator|+
name|path
operator|+
literal|" already deleted. Assuming a "
operator|+
literal|"previous attempt succeeded."
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Node "
operator|+
name|path
operator|+
literal|" already deleted, retry="
operator|+
name|isRetry
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"delete"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
name|isRetry
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|/**    * exists is an idempotent operation. Retry before throwing exception    * @return A Stat instance    */
specifier|public
name|Stat
name|exists
parameter_list|(
name|String
name|path
parameter_list|,
name|Watcher
name|watcher
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|exists
argument_list|(
name|path
argument_list|,
name|watcher
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"exists"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * exists is an idempotent operation. Retry before throwing exception    * @return A Stat instance    */
specifier|public
name|Stat
name|exists
parameter_list|(
name|String
name|path
parameter_list|,
name|boolean
name|watch
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|exists
argument_list|(
name|path
argument_list|,
name|watch
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"exists"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|retryOrThrow
parameter_list|(
name|RetryCounter
name|retryCounter
parameter_list|,
name|KeeperException
name|e
parameter_list|,
name|String
name|opName
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Possibly transient ZooKeeper exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|retryCounter
operator|.
name|shouldRetry
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZooKeeper "
operator|+
name|opName
operator|+
literal|" failed after "
operator|+
name|retryCounter
operator|.
name|getMaxRetries
argument_list|()
operator|+
literal|" retries"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * getChildren is an idempotent operation. Retry before throwing exception    * @return List of children znodes    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getChildren
parameter_list|(
name|String
name|path
parameter_list|,
name|Watcher
name|watcher
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|getChildren
argument_list|(
name|path
argument_list|,
name|watcher
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"getChildren"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * getChildren is an idempotent operation. Retry before throwing exception    * @return List of children znodes    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getChildren
parameter_list|(
name|String
name|path
parameter_list|,
name|boolean
name|watch
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|getChildren
argument_list|(
name|path
argument_list|,
name|watch
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"getChildren"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * getData is an idempotent operation. Retry before throwing exception    * @return Data    */
specifier|public
name|byte
index|[]
name|getData
parameter_list|(
name|String
name|path
parameter_list|,
name|Watcher
name|watcher
parameter_list|,
name|Stat
name|stat
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|byte
index|[]
name|revData
init|=
name|zk
operator|.
name|getData
argument_list|(
name|path
argument_list|,
name|watcher
argument_list|,
name|stat
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|removeMetaData
argument_list|(
name|revData
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"getData"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * getData is an idemnpotent operation. Retry before throwing exception    * @return Data    */
specifier|public
name|byte
index|[]
name|getData
parameter_list|(
name|String
name|path
parameter_list|,
name|boolean
name|watch
parameter_list|,
name|Stat
name|stat
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|byte
index|[]
name|revData
init|=
name|zk
operator|.
name|getData
argument_list|(
name|path
argument_list|,
name|watch
argument_list|,
name|stat
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|removeMetaData
argument_list|(
name|revData
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"getData"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * setData is NOT an idempotent operation. Retry may cause BadVersion Exception    * Adding an identifier field into the data to check whether     * badversion is caused by the result of previous correctly setData    * @return Stat instance    */
specifier|public
name|Stat
name|setData
parameter_list|(
name|String
name|path
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|int
name|version
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
name|byte
index|[]
name|newData
init|=
name|appendMetaData
argument_list|(
name|data
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|setData
argument_list|(
name|path
argument_list|,
name|newData
argument_list|,
name|version
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"setData"
argument_list|)
expr_stmt|;
break|break;
case|case
name|BADVERSION
case|:
comment|// try to verify whether the previous setData success or not
try|try
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|byte
index|[]
name|revData
init|=
name|zk
operator|.
name|getData
argument_list|(
name|path
argument_list|,
literal|false
argument_list|,
name|stat
argument_list|)
decl_stmt|;
name|int
name|idLength
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|revData
argument_list|,
name|ID_LENGTH_SIZE
argument_list|)
decl_stmt|;
name|int
name|dataLength
init|=
name|revData
operator|.
name|length
operator|-
name|ID_LENGTH_SIZE
operator|-
name|idLength
decl_stmt|;
name|int
name|dataOffset
init|=
name|ID_LENGTH_SIZE
operator|+
name|idLength
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|revData
argument_list|,
name|ID_LENGTH_SIZE
argument_list|,
name|id
operator|.
name|length
argument_list|,
name|revData
argument_list|,
name|dataOffset
argument_list|,
name|dataLength
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// the bad version is caused by previous successful setData
return|return
name|stat
return|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|keeperException
parameter_list|)
block|{
comment|// the ZK is not reliable at this moment. just throwing exception
throw|throw
name|keeperException
throw|;
block|}
comment|// throw other exceptions and verified bad version exceptions
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    *<p>    * NONSEQUENTIAL create is idempotent operation.     * Retry before throwing exceptions.    * But this function will not throw the NodeExist exception back to the    * application.    *</p>    *<p>    * But SEQUENTIAL is NOT idempotent operation. It is necessary to add     * identifier to the path to verify, whether the previous one is successful     * or not.    *</p>    *     * @return Path    */
specifier|public
name|String
name|create
parameter_list|(
name|String
name|path
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|List
argument_list|<
name|ACL
argument_list|>
name|acl
parameter_list|,
name|CreateMode
name|createMode
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|byte
index|[]
name|newData
init|=
name|appendMetaData
argument_list|(
name|data
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|createMode
condition|)
block|{
case|case
name|EPHEMERAL
case|:
case|case
name|PERSISTENT
case|:
return|return
name|createNonSequential
argument_list|(
name|path
argument_list|,
name|newData
argument_list|,
name|acl
argument_list|,
name|createMode
argument_list|)
return|;
case|case
name|EPHEMERAL_SEQUENTIAL
case|:
case|case
name|PERSISTENT_SEQUENTIAL
case|:
return|return
name|createSequential
argument_list|(
name|path
argument_list|,
name|newData
argument_list|,
name|acl
argument_list|,
name|createMode
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unrecognized CreateMode: "
operator|+
name|createMode
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|createNonSequential
parameter_list|(
name|String
name|path
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|List
argument_list|<
name|ACL
argument_list|>
name|acl
parameter_list|,
name|CreateMode
name|createMode
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
name|boolean
name|isRetry
init|=
literal|false
decl_stmt|;
comment|// False for first attempt, true for all retries.
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|create
argument_list|(
name|path
argument_list|,
name|data
argument_list|,
name|acl
argument_list|,
name|createMode
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|NODEEXISTS
case|:
if|if
condition|(
name|isRetry
condition|)
block|{
comment|// If the connection was lost, there is still a possibility that
comment|// we have successfully created the node at our previous attempt,
comment|// so we read the node and compare.
name|byte
index|[]
name|currentData
init|=
name|zk
operator|.
name|getData
argument_list|(
name|path
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentData
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|currentData
argument_list|,
name|data
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// We successfully created a non-sequential node
return|return
name|path
return|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Node "
operator|+
name|path
operator|+
literal|" already exists with "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|currentData
argument_list|)
operator|+
literal|", could not write "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Node "
operator|+
name|path
operator|+
literal|" already exists and this is not a "
operator|+
literal|"retry"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"create"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
name|isRetry
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|createSequential
parameter_list|(
name|String
name|path
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|List
argument_list|<
name|ACL
argument_list|>
name|acl
parameter_list|,
name|CreateMode
name|createMode
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|RetryCounter
name|retryCounter
init|=
name|retryCounterFactory
operator|.
name|create
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|String
name|newPath
init|=
name|path
operator|+
name|this
operator|.
name|identifier
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
comment|// Check if we succeeded on a previous attempt
name|String
name|previousResult
init|=
name|findPreviousSequentialNode
argument_list|(
name|newPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|previousResult
operator|!=
literal|null
condition|)
block|{
return|return
name|previousResult
return|;
block|}
block|}
name|first
operator|=
literal|false
expr_stmt|;
return|return
name|zk
operator|.
name|create
argument_list|(
name|newPath
argument_list|,
name|data
argument_list|,
name|acl
argument_list|,
name|createMode
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
switch|switch
condition|(
name|e
operator|.
name|code
argument_list|()
condition|)
block|{
case|case
name|CONNECTIONLOSS
case|:
case|case
name|SESSIONEXPIRED
case|:
case|case
name|OPERATIONTIMEOUT
case|:
name|retryOrThrow
argument_list|(
name|retryCounter
argument_list|,
name|e
argument_list|,
literal|"create"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|e
throw|;
block|}
block|}
name|retryCounter
operator|.
name|sleepUntilNextRetry
argument_list|()
expr_stmt|;
name|retryCounter
operator|.
name|useRetry
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|findPreviousSequentialNode
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|int
name|lastSlashIdx
init|=
name|path
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|lastSlashIdx
operator|!=
operator|-
literal|1
operator|)
assert|;
name|String
name|parent
init|=
name|path
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|lastSlashIdx
argument_list|)
decl_stmt|;
name|String
name|nodePrefix
init|=
name|path
operator|.
name|substring
argument_list|(
name|lastSlashIdx
operator|+
literal|1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|zk
operator|.
name|getChildren
argument_list|(
name|parent
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|matching
init|=
name|filterByPrefix
argument_list|(
name|nodes
argument_list|,
name|nodePrefix
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|node
range|:
name|matching
control|)
block|{
name|String
name|nodePath
init|=
name|parent
operator|+
literal|"/"
operator|+
name|node
decl_stmt|;
name|Stat
name|stat
init|=
name|zk
operator|.
name|exists
argument_list|(
name|nodePath
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
name|nodePath
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|byte
index|[]
name|removeMetaData
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
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
return|return
name|data
return|;
block|}
comment|// check the magic data; to be backward compatible
name|byte
name|magic
init|=
name|data
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|magic
operator|!=
name|MAGIC
condition|)
block|{
return|return
name|data
return|;
block|}
name|int
name|idLength
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|data
argument_list|,
name|ID_LENGTH_OFFSET
argument_list|)
decl_stmt|;
name|int
name|dataLength
init|=
name|data
operator|.
name|length
operator|-
name|MAGIC_SIZE
operator|-
name|ID_LENGTH_SIZE
operator|-
name|idLength
decl_stmt|;
name|int
name|dataOffset
init|=
name|MAGIC_SIZE
operator|+
name|ID_LENGTH_SIZE
operator|+
name|idLength
decl_stmt|;
name|byte
index|[]
name|newData
init|=
operator|new
name|byte
index|[
name|dataLength
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|data
argument_list|,
name|dataOffset
argument_list|,
name|newData
argument_list|,
literal|0
argument_list|,
name|dataLength
argument_list|)
expr_stmt|;
return|return
name|newData
return|;
block|}
specifier|private
name|byte
index|[]
name|appendMetaData
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
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
return|return
name|data
return|;
block|}
name|byte
index|[]
name|newData
init|=
operator|new
name|byte
index|[
name|MAGIC_SIZE
operator|+
name|ID_LENGTH_SIZE
operator|+
name|id
operator|.
name|length
operator|+
name|data
operator|.
name|length
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putByte
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|MAGIC
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|id
operator|.
name|length
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putBytes
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|id
argument_list|,
literal|0
argument_list|,
name|id
operator|.
name|length
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putBytes
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|newData
return|;
block|}
specifier|public
name|long
name|getSessionId
parameter_list|()
block|{
return|return
name|zk
operator|.
name|getSessionId
argument_list|()
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|States
name|getState
parameter_list|()
block|{
return|return
name|zk
operator|.
name|getState
argument_list|()
return|;
block|}
specifier|public
name|ZooKeeper
name|getZooKeeper
parameter_list|()
block|{
return|return
name|zk
return|;
block|}
specifier|public
name|byte
index|[]
name|getSessionPasswd
parameter_list|()
block|{
return|return
name|zk
operator|.
name|getSessionPasswd
argument_list|()
return|;
block|}
specifier|public
name|void
name|sync
parameter_list|(
name|String
name|path
parameter_list|,
name|AsyncCallback
operator|.
name|VoidCallback
name|cb
parameter_list|,
name|Object
name|ctx
parameter_list|)
block|{
name|this
operator|.
name|zk
operator|.
name|sync
argument_list|(
name|path
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Filters the given node list by the given prefixes.    * This method is all-inclusive--if any element in the node list starts    * with any of the given prefixes, then it is included in the result.    *    * @param nodes the nodes to filter    * @param prefixes the prefixes to include in the result    * @return list of every element that starts with one of the prefixes    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|filterByPrefix
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|nodes
parameter_list|,
name|String
modifier|...
name|prefixes
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|lockChildren
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|child
range|:
name|nodes
control|)
block|{
for|for
control|(
name|String
name|prefix
range|:
name|prefixes
control|)
block|{
if|if
condition|(
name|child
operator|.
name|startsWith
argument_list|(
name|prefix
argument_list|)
condition|)
block|{
name|lockChildren
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|lockChildren
return|;
block|}
block|}
end_class

end_unit

