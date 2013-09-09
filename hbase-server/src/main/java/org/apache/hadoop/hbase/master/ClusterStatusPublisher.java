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
name|master
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
name|ClusterStatus
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
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
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
name|jboss
operator|.
name|netty
operator|.
name|bootstrap
operator|.
name|ConnectionlessBootstrap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
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
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelUpstreamHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channels
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
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
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|DatagramChannelFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|oio
operator|.
name|OioDatagramChannelFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|protobuf
operator|.
name|ProtobufEncoder
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
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
name|Executors
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
name|Chore
block|{
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
argument_list|<
name|ServerName
argument_list|,
name|Integer
argument_list|>
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
specifier|static
name|int
name|MAX_SERVER_PER_MESSAGE
init|=
literal|10
decl_stmt|;
comment|/**    * If a server dies, we're sending the information multiple times in case a receiver misses the    * message.    */
specifier|public
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
literal|"HBase clusterStatusPublisher for "
operator|+
name|master
operator|.
name|getName
argument_list|()
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|STATUS_PUBLISH_PERIOD
argument_list|,
name|DEFAULT_STATUS_PUBLISH_PERIOD
argument_list|)
argument_list|,
name|master
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
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
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
catch|catch
parameter_list|(
name|IllegalAccessException
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
name|connected
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
name|currentTimeMillis
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
name|ClusterStatus
name|cs
init|=
operator|new
name|ClusterStatus
argument_list|(
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|,
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
argument_list|,
literal|null
argument_list|,
name|sns
argument_list|,
name|master
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|publisher
operator|.
name|publish
argument_list|(
name|cs
argument_list|)
expr_stmt|;
block|}
specifier|protected
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
name|currentTimeMillis
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
decl_stmt|;
name|entries
operator|.
name|addAll
argument_list|(
name|lastSent
operator|.
name|entrySet
argument_list|()
argument_list|)
expr_stmt|;
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
argument_list|<
name|ServerName
argument_list|>
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
name|ClusterStatus
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
name|ExecutorService
name|service
init|=
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|(
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"hbase-master-clusterStatus-worker"
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
comment|// Can't be NiO with Netty today => not implemented in Netty.
name|DatagramChannelFactory
name|f
init|=
operator|new
name|OioDatagramChannelFactory
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|ConnectionlessBootstrap
name|b
init|=
operator|new
name|ConnectionlessBootstrap
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|b
operator|.
name|setPipeline
argument_list|(
name|Channels
operator|.
name|pipeline
argument_list|(
operator|new
name|ProtobufEncoder
argument_list|()
argument_list|,
operator|new
name|ChannelUpstreamHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleUpstream
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ChannelEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
comment|// We're just writing here. Discard any incoming data. See HBASE-8466.
block|}
block|}
argument_list|)
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
operator|new
name|InetSocketAddress
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|channel
operator|.
name|getConfig
argument_list|()
operator|.
name|setReuseAddress
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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
name|channel
operator|.
name|joinGroup
argument_list|(
name|ina
argument_list|)
expr_stmt|;
name|channel
operator|.
name|connect
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|mcAddress
argument_list|,
name|port
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|publish
parameter_list|(
name|ClusterStatus
name|cs
parameter_list|)
block|{
name|ClusterStatusProtos
operator|.
name|ClusterStatus
name|csp
init|=
name|cs
operator|.
name|convert
argument_list|()
decl_stmt|;
name|channel
operator|.
name|write
argument_list|(
name|csp
argument_list|)
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
name|service
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

