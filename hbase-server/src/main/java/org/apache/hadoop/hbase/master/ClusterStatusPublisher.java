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
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|net
operator|.
name|Inet6Address
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
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
name|net
operator|.
name|NetworkInterface
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
name|Comparator
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
name|ConcurrentMap
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
name|ClusterMetrics
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
name|ClusterMetricsBuilder
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
name|HBaseInterfaceAudience
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
name|ScheduledChore
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
name|ServerName
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
name|Addressing
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
name|util
operator|.
name|ExceptionUtil
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
name|util
operator|.
name|ReflectionUtils
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|VersionInfo
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
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|bootstrap
operator|.
name|Bootstrap
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
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|Unpooled
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelException
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelFactory
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelOption
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|nio
operator|.
name|NioEventLoopGroup
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|DatagramChannel
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|DatagramPacket
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|InternetProtocolFamily
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioDatagramChannel
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
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|MessageToMessageEncoder
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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|internal
operator|.
name|StringUtil
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
comment|/**  * Class to publish the cluster status to the client. This allows them to know immediately  *  the dead region servers, hence to cut the connection they have with them, eventually stop  *  waiting on the socket. This improves the mean time to recover, and as well allows to increase  *  on the client the different timeouts, as the dead servers will be detected separately.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClusterStatusPublisher
extends|extends
name|ScheduledChore
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ClusterStatusPublisher
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * The implementation class used to publish the status. Default is null (no publish).    * Use org.apache.hadoop.hbase.master.ClusterStatusPublisher.MulticastPublisher to multicast the    * status.    */
specifier|public
specifier|static
specifier|final
name|String
name|STATUS_PUBLISHER_CLASS
init|=
literal|"hbase.status.publisher.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|ClusterStatusPublisher
operator|.
name|Publisher
argument_list|>
name|DEFAULT_STATUS_PUBLISHER_CLASS
init|=
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
name|ClusterStatusPublisher
operator|.
name|MulticastPublisher
operator|.
name|class
decl_stmt|;
comment|/**    * The minimum time between two status messages, in milliseconds.    */
specifier|public
specifier|static
specifier|final
name|String
name|STATUS_PUBLISH_PERIOD
init|=
literal|"hbase.status.publish.period"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_STATUS_PUBLISH_PERIOD
init|=
literal|10000
decl_stmt|;
specifier|private
name|long
name|lastMessageTime
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|private
specifier|final
name|int
name|messagePeriod
decl_stmt|;
comment|// time between two message
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
name|lastSent
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Publisher
name|publisher
decl_stmt|;
specifier|private
name|boolean
name|connected
init|=
literal|false
decl_stmt|;
comment|/**    * We want to limit the size of the protobuf message sent, do fit into a single packet.    * a reasonable size for ip / ethernet is less than 1Kb.    */
specifier|public
specifier|final
specifier|static
name|int
name|MAX_SERVER_PER_MESSAGE
init|=
literal|10
decl_stmt|;
comment|/**    * If a server dies, we're sending the information multiple times in case a receiver misses the    * message.    */
specifier|public
specifier|final
specifier|static
name|int
name|NB_SEND
init|=
literal|5
decl_stmt|;
specifier|public
name|ClusterStatusPublisher
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Publisher
argument_list|>
name|publisherClass
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
literal|"ClusterStatusPublisher for="
operator|+
name|master
operator|.
name|getName
argument_list|()
argument_list|,
name|master
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|STATUS_PUBLISH_PERIOD
argument_list|,
name|DEFAULT_STATUS_PUBLISH_PERIOD
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|messagePeriod
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|STATUS_PUBLISH_PERIOD
argument_list|,
name|DEFAULT_STATUS_PUBLISH_PERIOD
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|publisher
operator|=
name|publisherClass
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
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
literal|"Can't create publisher "
operator|+
name|publisherClass
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|publisher
operator|.
name|connect
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|connected
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", publisher="
operator|+
name|this
operator|.
name|publisher
operator|+
literal|", connected="
operator|+
name|this
operator|.
name|connected
return|;
block|}
comment|// For tests only
specifier|protected
name|ClusterStatusPublisher
parameter_list|()
block|{
name|master
operator|=
literal|null
expr_stmt|;
name|messagePeriod
operator|=
literal|0
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
operator|!
name|isConnected
argument_list|()
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|sns
init|=
name|generateDeadServersListToSend
argument_list|()
decl_stmt|;
if|if
condition|(
name|sns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Nothing to send. Done.
return|return;
block|}
specifier|final
name|long
name|curTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastMessageTime
operator|>
name|curTime
operator|-
name|messagePeriod
condition|)
block|{
comment|// We already sent something less than 10 second ago. Done.
return|return;
block|}
comment|// Ok, we're going to send something then.
name|lastMessageTime
operator|=
name|curTime
expr_stmt|;
comment|// We're reusing an existing protobuf message, but we don't send everything.
comment|// This could be extended in the future, for example if we want to send stuff like the
comment|//  hbase:meta server name.
name|publisher
operator|.
name|publish
argument_list|(
name|ClusterMetricsBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHBaseVersion
argument_list|(
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
operator|.
name|setClusterId
argument_list|(
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getClusterId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|setMasterName
argument_list|(
name|master
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|setDeadServerNames
argument_list|(
name|sns
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|void
name|cleanup
parameter_list|()
block|{
name|connected
operator|=
literal|false
expr_stmt|;
name|publisher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|boolean
name|isConnected
parameter_list|()
block|{
return|return
name|this
operator|.
name|connected
return|;
block|}
comment|/**    * Create the dead server to send. A dead server is sent NB_SEND times. We send at max    * MAX_SERVER_PER_MESSAGE at a time. if there are too many dead servers, we send the newly    * dead first.    */
specifier|protected
name|List
argument_list|<
name|ServerName
argument_list|>
name|generateDeadServersListToSend
parameter_list|()
block|{
comment|// We're getting the message sent since last time, and add them to the list
name|long
name|since
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|messagePeriod
operator|*
literal|2
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
name|dead
range|:
name|getDeadServers
argument_list|(
name|since
argument_list|)
control|)
block|{
name|lastSent
operator|.
name|putIfAbsent
argument_list|(
name|dead
operator|.
name|getFirst
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// We're sending the new deads first.
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|lastSent
operator|.
name|entrySet
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|entries
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
name|o1
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// With a limit of MAX_SERVER_PER_MESSAGE
name|int
name|max
init|=
name|entries
operator|.
name|size
argument_list|()
operator|>
name|MAX_SERVER_PER_MESSAGE
condition|?
name|MAX_SERVER_PER_MESSAGE
else|:
name|entries
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|max
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|max
condition|;
name|i
operator|++
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
name|toSend
init|=
name|entries
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|toSend
operator|.
name|getValue
argument_list|()
operator|>=
operator|(
name|NB_SEND
operator|-
literal|1
operator|)
condition|)
block|{
name|lastSent
operator|.
name|remove
argument_list|(
name|toSend
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lastSent
operator|.
name|replace
argument_list|(
name|toSend
operator|.
name|getKey
argument_list|()
argument_list|,
name|toSend
operator|.
name|getValue
argument_list|()
argument_list|,
name|toSend
operator|.
name|getValue
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|res
operator|.
name|add
argument_list|(
name|toSend
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
comment|/**    * Get the servers which died since a given timestamp.    * protected because it can be subclassed by the tests.    */
specifier|protected
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getDeadServers
parameter_list|(
name|long
name|since
parameter_list|)
block|{
if|if
condition|(
name|master
operator|.
name|getServerManager
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
return|return
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getDeadServers
argument_list|()
operator|.
name|copyDeadServersSince
argument_list|(
name|since
argument_list|)
return|;
block|}
specifier|public
interface|interface
name|Publisher
extends|extends
name|Closeable
block|{
name|void
name|connect
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|publish
parameter_list|(
name|ClusterMetrics
name|cs
parameter_list|)
function_decl|;
annotation|@
name|Override
name|void
name|close
parameter_list|()
function_decl|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
specifier|static
class|class
name|MulticastPublisher
implements|implements
name|Publisher
block|{
specifier|private
name|DatagramChannel
name|channel
decl_stmt|;
specifier|private
specifier|final
name|EventLoopGroup
name|group
init|=
operator|new
name|NioEventLoopGroup
argument_list|(
literal|1
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"hbase-master-clusterStatusPublisher"
argument_list|)
argument_list|)
decl_stmt|;
specifier|public
name|MulticastPublisher
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"channel="
operator|+
name|this
operator|.
name|channel
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connect
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|mcAddress
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|STATUS_MULTICAST_ADDRESS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_STATUS_MULTICAST_ADDRESS
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|STATUS_MULTICAST_PORT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_STATUS_MULTICAST_PORT
argument_list|)
decl_stmt|;
name|String
name|bindAddress
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|STATUS_MULTICAST_PUBLISHER_BIND_ADDRESS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_STATUS_MULTICAST_PUBLISHER_BIND_ADDRESS
argument_list|)
decl_stmt|;
name|String
name|niName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|STATUS_MULTICAST_NI_NAME
argument_list|)
decl_stmt|;
specifier|final
name|InetAddress
name|ina
decl_stmt|;
try|try
block|{
name|ina
operator|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|mcAddress
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|close
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't connect to "
operator|+
name|mcAddress
argument_list|,
name|e
argument_list|)
throw|;
block|}
specifier|final
name|InetSocketAddress
name|isa
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|mcAddress
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|InternetProtocolFamily
name|family
decl_stmt|;
name|NetworkInterface
name|ni
decl_stmt|;
if|if
condition|(
name|niName
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ina
operator|instanceof
name|Inet6Address
condition|)
block|{
name|family
operator|=
name|InternetProtocolFamily
operator|.
name|IPv6
expr_stmt|;
block|}
else|else
block|{
name|family
operator|=
name|InternetProtocolFamily
operator|.
name|IPv4
expr_stmt|;
block|}
name|ni
operator|=
name|NetworkInterface
operator|.
name|getByName
argument_list|(
name|niName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|InetAddress
name|localAddress
decl_stmt|;
if|if
condition|(
name|ina
operator|instanceof
name|Inet6Address
condition|)
block|{
name|localAddress
operator|=
name|Addressing
operator|.
name|getIp6Address
argument_list|()
expr_stmt|;
name|family
operator|=
name|InternetProtocolFamily
operator|.
name|IPv6
expr_stmt|;
block|}
else|else
block|{
name|localAddress
operator|=
name|Addressing
operator|.
name|getIp4Address
argument_list|()
expr_stmt|;
name|family
operator|=
name|InternetProtocolFamily
operator|.
name|IPv4
expr_stmt|;
block|}
name|ni
operator|=
name|NetworkInterface
operator|.
name|getByInetAddress
argument_list|(
name|localAddress
argument_list|)
expr_stmt|;
block|}
name|Bootstrap
name|b
init|=
operator|new
name|Bootstrap
argument_list|()
decl_stmt|;
name|b
operator|.
name|group
argument_list|(
name|group
argument_list|)
operator|.
name|channelFactory
argument_list|(
operator|new
name|HBaseDatagramChannelFactory
argument_list|<
name|Channel
argument_list|>
argument_list|(
name|NioDatagramChannel
operator|.
name|class
argument_list|,
name|family
argument_list|)
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|SO_REUSEADDR
argument_list|,
literal|true
argument_list|)
operator|.
name|handler
argument_list|(
operator|new
name|ClusterMetricsEncoder
argument_list|(
name|isa
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Channel bindAddress={}, networkInterface={}, INA={}"
argument_list|,
name|bindAddress
argument_list|,
name|ni
argument_list|,
name|ina
argument_list|)
expr_stmt|;
name|channel
operator|=
operator|(
name|DatagramChannel
operator|)
name|b
operator|.
name|bind
argument_list|(
name|bindAddress
argument_list|,
literal|0
argument_list|)
operator|.
name|sync
argument_list|()
operator|.
name|channel
argument_list|()
expr_stmt|;
name|channel
operator|.
name|joinGroup
argument_list|(
name|ina
argument_list|,
name|ni
argument_list|,
literal|null
argument_list|,
name|channel
operator|.
name|newPromise
argument_list|()
argument_list|)
operator|.
name|sync
argument_list|()
expr_stmt|;
name|channel
operator|.
name|connect
argument_list|(
name|isa
argument_list|)
operator|.
name|sync
argument_list|()
expr_stmt|;
comment|// Set into configuration in case many networks available. Do this for tests so that
comment|// server and client use same Interface (presuming share same Configuration).
comment|// TestAsyncTableRSCrashPublish was failing when connected to VPN because extra networks
comment|// available with Master binding on one Interface and client on another so test failed.
if|if
condition|(
name|ni
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|STATUS_MULTICAST_NI_NAME
argument_list|,
name|ni
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|close
argument_list|()
expr_stmt|;
throw|throw
name|ExceptionUtil
operator|.
name|asInterrupt
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|HBaseDatagramChannelFactory
parameter_list|<
name|T
extends|extends
name|Channel
parameter_list|>
implements|implements
name|ChannelFactory
argument_list|<
name|T
argument_list|>
block|{
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|clazz
decl_stmt|;
specifier|private
specifier|final
name|InternetProtocolFamily
name|family
decl_stmt|;
name|HBaseDatagramChannelFactory
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|clazz
parameter_list|,
name|InternetProtocolFamily
name|family
parameter_list|)
block|{
name|this
operator|.
name|clazz
operator|=
name|clazz
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|newChannel
parameter_list|()
block|{
try|try
block|{
return|return
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|InternetProtocolFamily
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|family
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|ChannelException
argument_list|(
literal|"Unable to create Channel from class "
operator|+
name|clazz
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|StringUtil
operator|.
name|simpleClassName
argument_list|(
name|clazz
argument_list|)
operator|+
literal|".class"
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|ClusterMetricsEncoder
extends|extends
name|MessageToMessageEncoder
argument_list|<
name|ClusterMetrics
argument_list|>
block|{
specifier|final
specifier|private
name|InetSocketAddress
name|isa
decl_stmt|;
specifier|private
name|ClusterMetricsEncoder
parameter_list|(
name|InetSocketAddress
name|isa
parameter_list|)
block|{
name|this
operator|.
name|isa
operator|=
name|isa
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|encode
parameter_list|(
name|ChannelHandlerContext
name|channelHandlerContext
parameter_list|,
name|ClusterMetrics
name|clusterStatus
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|objects
parameter_list|)
block|{
name|objects
operator|.
name|add
argument_list|(
operator|new
name|DatagramPacket
argument_list|(
name|Unpooled
operator|.
name|wrappedBuffer
argument_list|(
name|ClusterMetricsBuilder
operator|.
name|toClusterStatus
argument_list|(
name|clusterStatus
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|,
name|isa
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|publish
parameter_list|(
name|ClusterMetrics
name|cs
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"PUBLISH {}"
argument_list|,
name|cs
argument_list|)
expr_stmt|;
name|channel
operator|.
name|writeAndFlush
argument_list|(
name|cs
argument_list|)
operator|.
name|syncUninterruptibly
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|channel
operator|!=
literal|null
condition|)
block|{
name|channel
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|group
operator|.
name|shutdownGracefully
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

