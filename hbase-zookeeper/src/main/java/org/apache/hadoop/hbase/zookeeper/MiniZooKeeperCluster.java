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
name|zookeeper
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|client
operator|.
name|FourLetterWordMain
operator|.
name|send4LetterWord
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|BindException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ConnectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|java
operator|.
name|util
operator|.
name|Random
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
name|Threads
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
name|zookeeper
operator|.
name|server
operator|.
name|NIOServerCnxnFactory
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
name|server
operator|.
name|ZooKeeperServer
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
name|server
operator|.
name|persistence
operator|.
name|FileTxnLog
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
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

begin_comment
comment|/**  * TODO: Most of the code in this class is ripped from ZooKeeper tests. Instead  * of redoing it, we should contribute updates to their code which let us more  * easily access testing helper objects.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|MiniZooKeeperCluster
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
name|MiniZooKeeperCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TICK_TIME
init|=
literal|2000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TIMEOUT
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_CONNECTION_TIMEOUT
init|=
literal|30000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|STATIC_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"stat"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|connectionTimeout
decl_stmt|;
specifier|private
name|boolean
name|started
decl_stmt|;
comment|/**    * The default port. If zero, we use a random port.    */
specifier|private
name|int
name|defaultClientPort
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|NIOServerCnxnFactory
argument_list|>
name|standaloneServerFactoryList
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ZooKeeperServer
argument_list|>
name|zooKeeperServers
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|clientPortList
decl_stmt|;
specifier|private
name|int
name|activeZKServerIndex
decl_stmt|;
specifier|private
name|int
name|tickTime
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
specifier|public
name|MiniZooKeeperCluster
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MiniZooKeeperCluster
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|started
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|configuration
operator|=
name|configuration
expr_stmt|;
name|activeZKServerIndex
operator|=
operator|-
literal|1
expr_stmt|;
name|zooKeeperServers
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|clientPortList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|standaloneServerFactoryList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|connectionTimeout
operator|=
name|configuration
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
operator|+
literal|".localHBaseCluster"
argument_list|,
name|DEFAULT_CONNECTION_TIMEOUT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a client port to the list.    *    * @param clientPort the specified port    */
specifier|public
name|void
name|addClientPort
parameter_list|(
name|int
name|clientPort
parameter_list|)
block|{
name|clientPortList
operator|.
name|add
argument_list|(
name|clientPort
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the list of client ports.    *    * @return clientPortList the client port list    */
annotation|@
name|VisibleForTesting
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getClientPortList
parameter_list|()
block|{
return|return
name|clientPortList
return|;
block|}
comment|/**    * Check whether the client port in a specific position of the client port list is valid.    *    * @param index the specified position    */
specifier|private
name|boolean
name|hasValidClientPortInList
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
operator|(
name|clientPortList
operator|.
name|size
argument_list|()
operator|>
name|index
operator|&&
name|clientPortList
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|>
literal|0
operator|)
return|;
block|}
specifier|public
name|void
name|setDefaultClientPort
parameter_list|(
name|int
name|clientPort
parameter_list|)
block|{
if|if
condition|(
name|clientPort
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid default ZK client port: "
operator|+
name|clientPort
argument_list|)
throw|;
block|}
name|this
operator|.
name|defaultClientPort
operator|=
name|clientPort
expr_stmt|;
block|}
comment|/**    * Selects a ZK client port.    *    * @param seedPort the seed port to start with; -1 means first time.    * @return a valid and unused client port    */
specifier|private
name|int
name|selectClientPort
parameter_list|(
name|int
name|seedPort
parameter_list|)
block|{
name|int
name|i
decl_stmt|;
name|int
name|returnClientPort
init|=
name|seedPort
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|returnClientPort
operator|==
literal|0
condition|)
block|{
comment|// If the new port is invalid, find one - starting with the default client port.
comment|// If the default client port is not specified, starting with a random port.
comment|// The random port is selected from the range between 49152 to 65535. These ports cannot be
comment|// registered with IANA and are intended for dynamic allocation (see http://bit.ly/dynports).
if|if
condition|(
name|defaultClientPort
operator|>
literal|0
condition|)
block|{
name|returnClientPort
operator|=
name|defaultClientPort
expr_stmt|;
block|}
else|else
block|{
name|returnClientPort
operator|=
literal|0xc000
operator|+
operator|new
name|Random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|0x3f00
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Make sure that the port is unused.
comment|// break when an unused port is found
do|do
block|{
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|clientPortList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|returnClientPort
operator|==
name|clientPortList
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
comment|// Already used. Update the port and retry.
name|returnClientPort
operator|++
expr_stmt|;
break|break;
block|}
block|}
block|}
do|while
condition|(
name|i
operator|!=
name|clientPortList
operator|.
name|size
argument_list|()
condition|)
do|;
return|return
name|returnClientPort
return|;
block|}
specifier|public
name|void
name|setTickTime
parameter_list|(
name|int
name|tickTime
parameter_list|)
block|{
name|this
operator|.
name|tickTime
operator|=
name|tickTime
expr_stmt|;
block|}
specifier|public
name|int
name|getBackupZooKeeperServerNum
parameter_list|()
block|{
return|return
name|zooKeeperServers
operator|.
name|size
argument_list|()
operator|-
literal|1
return|;
block|}
specifier|public
name|int
name|getZooKeeperServerNum
parameter_list|()
block|{
return|return
name|zooKeeperServers
operator|.
name|size
argument_list|()
return|;
block|}
comment|// / XXX: From o.a.zk.t.ClientBase
specifier|private
specifier|static
name|void
name|setupTestEnv
parameter_list|()
block|{
comment|// during the tests we run with 100K prealloc in the logs.
comment|// on windows systems prealloc of 64M was seen to take ~15seconds
comment|// resulting in test failure (client timeout on first session).
comment|// set env and directly in order to handle static init/gc issues
name|System
operator|.
name|setProperty
argument_list|(
literal|"zookeeper.preAllocSize"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
name|FileTxnLog
operator|.
name|setPreallocSize
argument_list|(
literal|100
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// allow all 4 letter words
name|System
operator|.
name|setProperty
argument_list|(
literal|"zookeeper.4lw.commands.whitelist"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|startup
parameter_list|(
name|File
name|baseDir
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|int
name|numZooKeeperServers
init|=
name|clientPortList
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|numZooKeeperServers
operator|==
literal|0
condition|)
block|{
name|numZooKeeperServers
operator|=
literal|1
expr_stmt|;
comment|// need at least 1 ZK server for testing
block|}
return|return
name|startup
argument_list|(
name|baseDir
argument_list|,
name|numZooKeeperServers
argument_list|)
return|;
block|}
comment|/**    * @param baseDir             the base directory to use    * @param numZooKeeperServers the number of ZooKeeper servers    * @return ClientPort server bound to, -1 if there was a binding problem and we couldn't pick    *   another port.    * @throws IOException          if an operation fails during the startup    * @throws InterruptedException if the startup fails    */
specifier|public
name|int
name|startup
parameter_list|(
name|File
name|baseDir
parameter_list|,
name|int
name|numZooKeeperServers
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|numZooKeeperServers
operator|<=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|setupTestEnv
argument_list|()
expr_stmt|;
name|shutdown
argument_list|()
expr_stmt|;
name|int
name|tentativePort
init|=
operator|-
literal|1
decl_stmt|;
comment|// the seed port
name|int
name|currentClientPort
decl_stmt|;
comment|// running all the ZK servers
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numZooKeeperServers
condition|;
name|i
operator|++
control|)
block|{
name|File
name|dir
init|=
operator|new
name|File
argument_list|(
name|baseDir
argument_list|,
literal|"zookeeper_"
operator|+
name|i
argument_list|)
operator|.
name|getAbsoluteFile
argument_list|()
decl_stmt|;
name|createDir
argument_list|(
name|dir
argument_list|)
expr_stmt|;
name|int
name|tickTimeToUse
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|tickTime
operator|>
literal|0
condition|)
block|{
name|tickTimeToUse
operator|=
name|this
operator|.
name|tickTime
expr_stmt|;
block|}
else|else
block|{
name|tickTimeToUse
operator|=
name|TICK_TIME
expr_stmt|;
block|}
comment|// Set up client port - if we have already had a list of valid ports, use it.
if|if
condition|(
name|hasValidClientPortInList
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|currentClientPort
operator|=
name|clientPortList
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tentativePort
operator|=
name|selectClientPort
argument_list|(
name|tentativePort
argument_list|)
expr_stmt|;
comment|// update the seed
name|currentClientPort
operator|=
name|tentativePort
expr_stmt|;
block|}
name|ZooKeeperServer
name|server
init|=
operator|new
name|ZooKeeperServer
argument_list|(
name|dir
argument_list|,
name|dir
argument_list|,
name|tickTimeToUse
argument_list|)
decl_stmt|;
comment|// Setting {min,max}SessionTimeout defaults to be the same as in Zookeeper
name|server
operator|.
name|setMinSessionTimeout
argument_list|(
name|configuration
operator|.
name|getInt
argument_list|(
literal|"hbase.zookeeper.property.minSessionTimeout"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|setMaxSessionTimeout
argument_list|(
name|configuration
operator|.
name|getInt
argument_list|(
literal|"hbase.zookeeper.property.maxSessionTimeout"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|NIOServerCnxnFactory
name|standaloneServerFactory
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|standaloneServerFactory
operator|=
operator|new
name|NIOServerCnxnFactory
argument_list|()
expr_stmt|;
name|standaloneServerFactory
operator|.
name|configure
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|currentClientPort
argument_list|)
argument_list|,
name|configuration
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_MAX_CLIENT_CNXNS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_MAX_CLIENT_CNXNS
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BindException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed binding ZK Server to client port: "
operator|+
name|currentClientPort
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// We're told to use some port but it's occupied, fail
if|if
condition|(
name|hasValidClientPortInList
argument_list|(
name|i
argument_list|)
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// This port is already in use, try to use another.
name|tentativePort
operator|=
name|selectClientPort
argument_list|(
name|tentativePort
argument_list|)
expr_stmt|;
name|currentClientPort
operator|=
name|tentativePort
expr_stmt|;
continue|continue;
block|}
break|break;
block|}
comment|// Start up this ZK server. Dump its stats.
name|standaloneServerFactory
operator|.
name|startup
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started connectionTimeout={}, dir={}, {}"
argument_list|,
name|connectionTimeout
argument_list|,
name|dir
argument_list|,
name|getServerConfigurationOnOneLine
argument_list|(
name|server
argument_list|)
argument_list|)
expr_stmt|;
comment|// Runs a 'stat' against the servers.
if|if
condition|(
operator|!
name|waitForServerUp
argument_list|(
name|currentClientPort
argument_list|,
name|connectionTimeout
argument_list|)
condition|)
block|{
name|Threads
operator|.
name|printThreadInfo
argument_list|(
name|System
operator|.
name|out
argument_list|,
literal|"Why is zk standalone server not coming up?"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Waiting for startup of standalone server; "
operator|+
literal|"server isRunning="
operator|+
name|server
operator|.
name|isRunning
argument_list|()
argument_list|)
throw|;
block|}
comment|// We have selected a port as a client port.  Update clientPortList if necessary.
if|if
condition|(
name|clientPortList
operator|.
name|size
argument_list|()
operator|<=
name|i
condition|)
block|{
comment|// it is not in the list, add the port
name|clientPortList
operator|.
name|add
argument_list|(
name|currentClientPort
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|clientPortList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|<=
literal|0
condition|)
block|{
comment|// the list has invalid port, update with valid port
name|clientPortList
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|clientPortList
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|currentClientPort
argument_list|)
expr_stmt|;
block|}
name|standaloneServerFactoryList
operator|.
name|add
argument_list|(
name|standaloneServerFactory
argument_list|)
expr_stmt|;
name|zooKeeperServers
operator|.
name|add
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
comment|// set the first one to be active ZK; Others are backups
name|activeZKServerIndex
operator|=
literal|0
expr_stmt|;
name|started
operator|=
literal|true
expr_stmt|;
name|int
name|clientPort
init|=
name|clientPortList
operator|.
name|get
argument_list|(
name|activeZKServerIndex
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started MiniZooKeeperCluster and ran 'stat' on client port={}"
argument_list|,
name|clientPort
argument_list|)
expr_stmt|;
return|return
name|clientPort
return|;
block|}
specifier|private
name|String
name|getServerConfigurationOnOneLine
parameter_list|(
name|ZooKeeperServer
name|server
parameter_list|)
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
try|try
init|(
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
name|sw
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|println
parameter_list|(
name|int
name|x
parameter_list|)
block|{
name|super
operator|.
name|print
argument_list|(
name|x
argument_list|)
expr_stmt|;
name|super
operator|.
name|print
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|println
parameter_list|(
name|String
name|x
parameter_list|)
block|{
name|super
operator|.
name|print
argument_list|(
name|x
argument_list|)
expr_stmt|;
name|super
operator|.
name|print
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
block|}
init|)
block|{
name|server
operator|.
name|dumpConf
argument_list|(
name|pw
argument_list|)
expr_stmt|;
block|}
return|return
name|sw
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|createDir
parameter_list|(
name|File
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|dir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|dir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"creating dir: "
operator|+
name|dir
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @throws IOException if waiting for the shutdown of a server fails    */
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
comment|// shut down all the zk servers
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|standaloneServerFactoryList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|NIOServerCnxnFactory
name|standaloneServerFactory
init|=
name|standaloneServerFactoryList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|clientPort
init|=
name|clientPortList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|standaloneServerFactory
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|waitForServerDown
argument_list|(
name|clientPort
argument_list|,
name|connectionTimeout
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Waiting for shutdown of standalone server"
argument_list|)
throw|;
block|}
block|}
name|standaloneServerFactoryList
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|ZooKeeperServer
name|zkServer
range|:
name|zooKeeperServers
control|)
block|{
comment|//explicitly close ZKDatabase since ZookeeperServer does not close them
name|zkServer
operator|.
name|getZKDatabase
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|zooKeeperServers
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// clear everything
if|if
condition|(
name|started
condition|)
block|{
name|started
operator|=
literal|false
expr_stmt|;
name|activeZKServerIndex
operator|=
literal|0
expr_stmt|;
name|clientPortList
operator|.
name|clear
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutdown MiniZK cluster with all ZK servers"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return clientPort return clientPort if there is another ZK backup can run    *         when killing the current active; return -1, if there is no backups.    * @throws IOException if waiting for the shutdown of a server fails    */
specifier|public
name|int
name|killCurrentActiveZooKeeperServer
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
operator|!
name|started
operator|||
name|activeZKServerIndex
operator|<
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// Shutdown the current active one
name|NIOServerCnxnFactory
name|standaloneServerFactory
init|=
name|standaloneServerFactoryList
operator|.
name|get
argument_list|(
name|activeZKServerIndex
argument_list|)
decl_stmt|;
name|int
name|clientPort
init|=
name|clientPortList
operator|.
name|get
argument_list|(
name|activeZKServerIndex
argument_list|)
decl_stmt|;
name|standaloneServerFactory
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|waitForServerDown
argument_list|(
name|clientPort
argument_list|,
name|connectionTimeout
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Waiting for shutdown of standalone server"
argument_list|)
throw|;
block|}
name|zooKeeperServers
operator|.
name|get
argument_list|(
name|activeZKServerIndex
argument_list|)
operator|.
name|getZKDatabase
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// remove the current active zk server
name|standaloneServerFactoryList
operator|.
name|remove
argument_list|(
name|activeZKServerIndex
argument_list|)
expr_stmt|;
name|clientPortList
operator|.
name|remove
argument_list|(
name|activeZKServerIndex
argument_list|)
expr_stmt|;
name|zooKeeperServers
operator|.
name|remove
argument_list|(
name|activeZKServerIndex
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Kill the current active ZK servers in the cluster on client port: {}"
argument_list|,
name|clientPort
argument_list|)
expr_stmt|;
if|if
condition|(
name|standaloneServerFactoryList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// there is no backup servers;
return|return
operator|-
literal|1
return|;
block|}
name|clientPort
operator|=
name|clientPortList
operator|.
name|get
argument_list|(
name|activeZKServerIndex
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Activate a backup zk server in the cluster on client port: {}"
argument_list|,
name|clientPort
argument_list|)
expr_stmt|;
comment|// return the next back zk server's port
return|return
name|clientPort
return|;
block|}
comment|/**    * Kill one back up ZK servers.    *    * @throws IOException if waiting for the shutdown of a server fails    */
specifier|public
name|void
name|killOneBackupZooKeeperServer
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
operator|!
name|started
operator|||
name|activeZKServerIndex
operator|<
literal|0
operator|||
name|standaloneServerFactoryList
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return ;
block|}
name|int
name|backupZKServerIndex
init|=
name|activeZKServerIndex
operator|+
literal|1
decl_stmt|;
comment|// Shutdown the current active one
name|NIOServerCnxnFactory
name|standaloneServerFactory
init|=
name|standaloneServerFactoryList
operator|.
name|get
argument_list|(
name|backupZKServerIndex
argument_list|)
decl_stmt|;
name|int
name|clientPort
init|=
name|clientPortList
operator|.
name|get
argument_list|(
name|backupZKServerIndex
argument_list|)
decl_stmt|;
name|standaloneServerFactory
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|waitForServerDown
argument_list|(
name|clientPort
argument_list|,
name|connectionTimeout
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Waiting for shutdown of standalone server"
argument_list|)
throw|;
block|}
name|zooKeeperServers
operator|.
name|get
argument_list|(
name|backupZKServerIndex
argument_list|)
operator|.
name|getZKDatabase
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// remove this backup zk server
name|standaloneServerFactoryList
operator|.
name|remove
argument_list|(
name|backupZKServerIndex
argument_list|)
expr_stmt|;
name|clientPortList
operator|.
name|remove
argument_list|(
name|backupZKServerIndex
argument_list|)
expr_stmt|;
name|zooKeeperServers
operator|.
name|remove
argument_list|(
name|backupZKServerIndex
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Kill one backup ZK servers in the cluster on client port: {}"
argument_list|,
name|clientPort
argument_list|)
expr_stmt|;
block|}
comment|// XXX: From o.a.zk.t.ClientBase. We just dropped the check for ssl/secure.
specifier|private
specifier|static
name|boolean
name|waitForServerDown
parameter_list|(
name|int
name|port
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|send4LetterWord
argument_list|(
literal|"localhost"
argument_list|,
name|port
argument_list|,
literal|"stat"
argument_list|,
operator|(
name|int
operator|)
name|timeout
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|start
operator|+
name|timeout
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|// XXX: From o.a.zk.t.ClientBase. Its in the test jar but we don't depend on zk test jar.
comment|// We remove the SSL/secure bit. Not used in here.
specifier|private
specifier|static
name|boolean
name|waitForServerUp
parameter_list|(
name|int
name|port
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|String
name|result
init|=
name|send4LetterWord
argument_list|(
literal|"localhost"
argument_list|,
name|port
argument_list|,
literal|"stat"
argument_list|,
operator|(
name|int
operator|)
name|timeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|startsWith
argument_list|(
literal|"Zookeeper version:"
argument_list|)
operator|&&
operator|!
name|result
operator|.
name|contains
argument_list|(
literal|"READ-ONLY"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Read {}"
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ConnectException
name|e
parameter_list|)
block|{
comment|// ignore as this is expected, do not log stacktrace
name|LOG
operator|.
name|info
argument_list|(
literal|"localhost:{} not up: {}"
argument_list|,
name|port
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore as this is expected
name|LOG
operator|.
name|info
argument_list|(
literal|"localhost:{} not up"
argument_list|,
name|port
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|start
operator|+
name|timeout
condition|)
block|{
break|break;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|TIMEOUT
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|int
name|getClientPort
parameter_list|()
block|{
return|return
name|activeZKServerIndex
operator|<
literal|0
operator|||
name|activeZKServerIndex
operator|>=
name|clientPortList
operator|.
name|size
argument_list|()
condition|?
operator|-
literal|1
else|:
name|clientPortList
operator|.
name|get
argument_list|(
name|activeZKServerIndex
argument_list|)
return|;
block|}
name|List
argument_list|<
name|ZooKeeperServer
argument_list|>
name|getZooKeeperServers
parameter_list|()
block|{
return|return
name|zooKeeperServers
return|;
block|}
block|}
end_class

end_unit

