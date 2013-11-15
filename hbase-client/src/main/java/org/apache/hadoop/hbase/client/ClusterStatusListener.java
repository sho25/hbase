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
name|client
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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|List
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
name|Threads
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
name|ExceptionEvent
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
name|MessageEvent
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
name|SimpleChannelUpstreamHandler
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
name|ProtobufDecoder
import|;
end_import

begin_comment
comment|/**  * A class that receives the cluster status, and provide it as a set of service to the client.  * Today, manages only the dead server list.  * The class is abstract to allow multiple implementations, from ZooKeeper to multicast based.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ClusterStatusListener
implements|implements
name|Closeable
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
name|ClusterStatusListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadServers
init|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|DeadServerHandler
name|deadServerHandler
decl_stmt|;
specifier|private
specifier|final
name|Listener
name|listener
decl_stmt|;
comment|/**    * The implementation class to use to read the status.    */
specifier|public
specifier|static
specifier|final
name|String
name|STATUS_LISTENER_CLASS
init|=
literal|"hbase.status.listener.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Listener
argument_list|>
name|DEFAULT_STATUS_LISTENER_CLASS
init|=
name|MulticastListener
operator|.
name|class
decl_stmt|;
comment|/**    * Class to be extended to manage a new dead server.    */
specifier|public
interface|interface
name|DeadServerHandler
block|{
comment|/**      * Called when a server is identified as dead. Called only once even if we receive the      * information multiple times.      *      * @param sn - the server name      */
name|void
name|newDead
parameter_list|(
name|ServerName
name|sn
parameter_list|)
function_decl|;
block|}
comment|/**    * The interface to be implemented by a listener of a cluster status event.    */
interface|interface
name|Listener
extends|extends
name|Closeable
block|{
comment|/**      * Called to close the resources, if any. Cannot throw an exception.      */
annotation|@
name|Override
name|void
name|close
parameter_list|()
function_decl|;
comment|/**      * Called to connect.      *      * @param conf Configuration to use.      * @throws IOException      */
name|void
name|connect
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
name|ClusterStatusListener
parameter_list|(
name|DeadServerHandler
name|dsh
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Listener
argument_list|>
name|listenerClass
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|deadServerHandler
operator|=
name|dsh
expr_stmt|;
try|try
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|Listener
argument_list|>
name|ctor
init|=
name|listenerClass
operator|.
name|getConstructor
argument_list|(
name|ClusterStatusListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|this
operator|.
name|listener
operator|=
name|ctor
operator|.
name|newInstance
argument_list|(
name|this
argument_list|)
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
literal|"Can't create listener "
operator|+
name|listenerClass
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
literal|"Can't create listener "
operator|+
name|listenerClass
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
name|NoSuchMethodException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
name|this
operator|.
name|listener
operator|.
name|connect
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Acts upon the reception of a new cluster status.    *    * @param ncs the cluster status    */
specifier|public
name|void
name|receive
parameter_list|(
name|ClusterStatus
name|ncs
parameter_list|)
block|{
if|if
condition|(
name|ncs
operator|.
name|getDeadServerNames
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ServerName
name|sn
range|:
name|ncs
operator|.
name|getDeadServerNames
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isDeadServer
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"There is a new dead server: "
operator|+
name|sn
argument_list|)
expr_stmt|;
name|deadServers
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
if|if
condition|(
name|deadServerHandler
operator|!=
literal|null
condition|)
block|{
name|deadServerHandler
operator|.
name|newDead
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|listener
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check if we know if a server is dead.    *    * @param sn the server name to check.    * @return true if we know for sure that the server is dead, false otherwise.    */
specifier|public
name|boolean
name|isDeadServer
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
if|if
condition|(
name|sn
operator|.
name|getStartcode
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|ServerName
name|dead
range|:
name|deadServers
control|)
block|{
if|if
condition|(
name|dead
operator|.
name|getStartcode
argument_list|()
operator|>=
name|sn
operator|.
name|getStartcode
argument_list|()
operator|&&
name|dead
operator|.
name|getPort
argument_list|()
operator|==
name|sn
operator|.
name|getPort
argument_list|()
operator|&&
name|dead
operator|.
name|getHostname
argument_list|()
operator|.
name|equals
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * An implementation using a multicast message between the master& the client.    */
class|class
name|MulticastListener
implements|implements
name|Listener
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
literal|"hbase-client-clusterStatus-multiCastListener"
argument_list|)
argument_list|)
decl_stmt|;
specifier|public
name|MulticastListener
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
name|ProtobufDecoder
argument_list|(
name|ClusterStatusProtos
operator|.
name|ClusterStatus
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ClusterStatusHandler
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|String
name|bindAddress
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|STATUS_MULTICAST_BIND_ADDRESS
argument_list|,
name|HConstants
operator|.
name|DEFAULT_STATUS_MULTICAST_BIND_ADDRESS
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
name|bindAddress
argument_list|,
name|port
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
name|channel
operator|=
literal|null
expr_stmt|;
block|}
name|service
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|/**      * Class, conforming to the Netty framework, that manages the message received.      */
specifier|private
class|class
name|ClusterStatusHandler
extends|extends
name|SimpleChannelUpstreamHandler
block|{
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|MessageEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
name|ClusterStatusProtos
operator|.
name|ClusterStatus
name|csp
init|=
operator|(
name|ClusterStatusProtos
operator|.
name|ClusterStatus
operator|)
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|ClusterStatus
name|ncs
init|=
name|ClusterStatus
operator|.
name|convert
argument_list|(
name|csp
argument_list|)
decl_stmt|;
name|receive
argument_list|(
name|ncs
argument_list|)
expr_stmt|;
block|}
comment|/**        * Invoked when an exception was raised by an I/O thread or a        * {@link org.jboss.netty.channel.ChannelHandler}.        */
annotation|@
name|Override
specifier|public
name|void
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ExceptionEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected exception, continuing."
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

