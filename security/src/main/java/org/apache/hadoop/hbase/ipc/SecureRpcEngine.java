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
name|ipc
package|;
end_package

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
name|Configurable
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
name|Server
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
name|RetriesExhaustedException
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
name|io
operator|.
name|HbaseObjectWritable
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
name|monitoring
operator|.
name|MonitoredRPCHandler
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
name|security
operator|.
name|HBasePolicyProvider
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
name|security
operator|.
name|HBaseSaslRpcServer
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
name|security
operator|.
name|User
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
name|security
operator|.
name|token
operator|.
name|AuthenticationTokenSecretManager
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
name|Objects
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
name|Writable
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
name|metrics
operator|.
name|util
operator|.
name|MetricsTimeVaryingRate
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
name|net
operator|.
name|NetUtils
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
name|authorize
operator|.
name|ServiceAuthorizationManager
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|SocketFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|*
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
name|net
operator|.
name|SocketTimeoutException
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
name|Map
import|;
end_import

begin_comment
comment|/**  * A loadable RPC engine supporting SASL authentication of connections, using  * GSSAPI for Kerberos authentication or DIGEST-MD5 for authentication via  * signed tokens.  *  *<p>  * This is a fork of the {@code org.apache.hadoop.ipc.WriteableRpcEngine} from  * secure Hadoop, reworked to eliminate code duplication with the existing  * HBase {@link WritableRpcEngine}.  *</p>  *  * @see SecureClient  * @see SecureServer  */
end_comment

begin_class
specifier|public
class|class
name|SecureRpcEngine
implements|implements
name|RpcEngine
block|{
comment|// Leave this out in the hadoop ipc package but keep class name.  Do this
comment|// so that we dont' get the logging of this class's invocations by doing our
comment|// blanket enabling DEBUG on the o.a.h.h. package.
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.ipc.SecureRpcEngine"
argument_list|)
decl_stmt|;
specifier|private
name|SecureRpcEngine
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|// no public ctor
comment|/* Cache a client using its socket factory as the hash key */
specifier|static
specifier|private
class|class
name|ClientCache
block|{
specifier|private
name|Map
argument_list|<
name|SocketFactory
argument_list|,
name|SecureClient
argument_list|>
name|clients
init|=
operator|new
name|HashMap
argument_list|<
name|SocketFactory
argument_list|,
name|SecureClient
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|ClientCache
parameter_list|()
block|{}
comment|/**      * Construct& cache an IPC client with the user-provided SocketFactory      * if no cached client exists.      *      * @param conf Configuration      * @param factory socket factory      * @return an IPC client      */
specifier|protected
specifier|synchronized
name|SecureClient
name|getClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SocketFactory
name|factory
parameter_list|)
block|{
comment|// Construct& cache client.  The configuration is only used for timeout,
comment|// and Clients have connection pools.  So we can either (a) lose some
comment|// connection pooling and leak sockets, or (b) use the same timeout for all
comment|// configurations.  Since the IPC is usually intended globally, not
comment|// per-job, we choose (a).
name|SecureClient
name|client
init|=
name|clients
operator|.
name|get
argument_list|(
name|factory
argument_list|)
decl_stmt|;
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
comment|// Make an hbase client instead of hadoop Client.
name|client
operator|=
operator|new
name|SecureClient
argument_list|(
name|HbaseObjectWritable
operator|.
name|class
argument_list|,
name|conf
argument_list|,
name|factory
argument_list|)
expr_stmt|;
name|clients
operator|.
name|put
argument_list|(
name|factory
argument_list|,
name|client
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|client
operator|.
name|incCount
argument_list|()
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
comment|/**      * Construct& cache an IPC client with the default SocketFactory      * if no cached client exists.      *      * @param conf Configuration      * @return an IPC client      */
specifier|protected
specifier|synchronized
name|SecureClient
name|getClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getClient
argument_list|(
name|conf
argument_list|,
name|SocketFactory
operator|.
name|getDefault
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Stop a RPC client connection      * A RPC client is closed only when its reference count becomes zero.      * @param client client to stop      */
specifier|protected
name|void
name|stopClient
parameter_list|(
name|SecureClient
name|client
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|client
operator|.
name|decCount
argument_list|()
expr_stmt|;
if|if
condition|(
name|client
operator|.
name|isZeroReference
argument_list|()
condition|)
block|{
name|clients
operator|.
name|remove
argument_list|(
name|client
operator|.
name|getSocketFactory
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|client
operator|.
name|isZeroReference
argument_list|()
condition|)
block|{
name|client
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|final
specifier|static
name|ClientCache
name|CLIENTS
init|=
operator|new
name|ClientCache
argument_list|()
decl_stmt|;
specifier|private
specifier|static
class|class
name|Invoker
implements|implements
name|InvocationHandler
block|{
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
decl_stmt|;
specifier|private
name|InetSocketAddress
name|address
decl_stmt|;
specifier|private
name|User
name|ticket
decl_stmt|;
specifier|private
name|SecureClient
name|client
decl_stmt|;
specifier|private
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
specifier|final
specifier|private
name|int
name|rpcTimeout
decl_stmt|;
specifier|public
name|Invoker
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|,
name|InetSocketAddress
name|address
parameter_list|,
name|User
name|ticket
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|SocketFactory
name|factory
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
block|{
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|ticket
operator|=
name|ticket
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|CLIENTS
operator|.
name|getClient
argument_list|(
name|conf
argument_list|,
name|factory
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcTimeout
operator|=
name|rpcTimeout
expr_stmt|;
block|}
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
specifier|final
name|boolean
name|logDebug
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
name|long
name|startTime
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|logDebug
condition|)
block|{
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
name|HbaseObjectWritable
name|value
init|=
operator|(
name|HbaseObjectWritable
operator|)
name|client
operator|.
name|call
argument_list|(
operator|new
name|Invocation
argument_list|(
name|method
argument_list|,
name|args
argument_list|)
argument_list|,
name|address
argument_list|,
name|protocol
argument_list|,
name|ticket
argument_list|,
name|rpcTimeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|logDebug
condition|)
block|{
name|long
name|callTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Call: "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|callTime
argument_list|)
expr_stmt|;
block|}
return|return
name|value
operator|.
name|get
argument_list|()
return|;
block|}
comment|/* close the IPC client that's responsible for this invoker's RPCs */
specifier|synchronized
specifier|protected
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isClosed
condition|)
block|{
name|isClosed
operator|=
literal|true
expr_stmt|;
name|CLIENTS
operator|.
name|stopClient
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Construct a client-side proxy object that implements the named protocol,    * talking to a server at the named address.    *    * @param protocol interface    * @param clientVersion version we are expecting    * @param addr remote address    * @param ticket ticket    * @param conf configuration    * @param factory socket factory    * @return proxy    * @throws java.io.IOException e    */
specifier|public
name|VersionedProtocol
name|getProxy
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|,
name|User
name|ticket
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|SocketFactory
name|factory
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|User
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|HBaseSaslRpcServer
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|VersionedProtocol
name|proxy
init|=
operator|(
name|VersionedProtocol
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|protocol
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|protocol
block|}
argument_list|,
operator|new
name|Invoker
argument_list|(
name|protocol
argument_list|,
name|addr
argument_list|,
name|ticket
argument_list|,
name|conf
argument_list|,
name|factory
argument_list|,
name|rpcTimeout
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|serverVersion
init|=
name|proxy
operator|.
name|getProtocolVersion
argument_list|(
name|protocol
operator|.
name|getName
argument_list|()
argument_list|,
name|clientVersion
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverVersion
operator|!=
name|clientVersion
condition|)
block|{
throw|throw
operator|new
name|HBaseRPC
operator|.
name|VersionMismatch
argument_list|(
name|protocol
operator|.
name|getName
argument_list|()
argument_list|,
name|clientVersion
argument_list|,
name|serverVersion
argument_list|)
throw|;
block|}
return|return
name|proxy
return|;
block|}
comment|/**    * Stop this proxy and release its invoker's resource    * @param proxy the proxy to be stopped    */
specifier|public
name|void
name|stopProxy
parameter_list|(
name|VersionedProtocol
name|proxy
parameter_list|)
block|{
if|if
condition|(
name|proxy
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|Invoker
operator|)
name|Proxy
operator|.
name|getInvocationHandler
argument_list|(
name|proxy
argument_list|)
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** Expert: Make multiple, parallel calls to a set of servers. */
specifier|public
name|Object
index|[]
name|call
parameter_list|(
name|Method
name|method
parameter_list|,
name|Object
index|[]
index|[]
name|params
parameter_list|,
name|InetSocketAddress
index|[]
name|addrs
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|,
name|User
name|ticket
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Invocation
index|[]
name|invocations
init|=
operator|new
name|Invocation
index|[
name|params
operator|.
name|length
index|]
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
name|params
operator|.
name|length
condition|;
name|i
operator|++
control|)
name|invocations
index|[
name|i
index|]
operator|=
operator|new
name|Invocation
argument_list|(
name|method
argument_list|,
name|params
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|SecureClient
name|client
init|=
name|CLIENTS
operator|.
name|getClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|Writable
index|[]
name|wrappedValues
init|=
name|client
operator|.
name|call
argument_list|(
name|invocations
argument_list|,
name|addrs
argument_list|,
name|protocol
argument_list|,
name|ticket
argument_list|)
decl_stmt|;
if|if
condition|(
name|method
operator|.
name|getReturnType
argument_list|()
operator|==
name|Void
operator|.
name|TYPE
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
index|[]
name|values
init|=
operator|(
name|Object
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|method
operator|.
name|getReturnType
argument_list|()
argument_list|,
name|wrappedValues
operator|.
name|length
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
if|if
condition|(
name|wrappedValues
index|[
name|i
index|]
operator|!=
literal|null
condition|)
name|values
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|HbaseObjectWritable
operator|)
name|wrappedValues
index|[
name|i
index|]
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
return|return
name|values
return|;
block|}
finally|finally
block|{
name|CLIENTS
operator|.
name|stopClient
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Construct a server for a protocol implementation instance listening on a    * port and address, with a secret manager. */
specifier|public
name|Server
name|getServer
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|,
specifier|final
name|Object
name|instance
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|ifaces
parameter_list|,
specifier|final
name|String
name|bindAddress
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|int
name|numHandlers
parameter_list|,
name|int
name|metaHandlerCount
parameter_list|,
specifier|final
name|boolean
name|verbose
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|int
name|highPriorityLevel
parameter_list|)
throws|throws
name|IOException
block|{
name|Server
name|server
init|=
operator|new
name|Server
argument_list|(
name|instance
argument_list|,
name|ifaces
argument_list|,
name|conf
argument_list|,
name|bindAddress
argument_list|,
name|port
argument_list|,
name|numHandlers
argument_list|,
name|metaHandlerCount
argument_list|,
name|verbose
argument_list|,
name|highPriorityLevel
argument_list|)
decl_stmt|;
return|return
name|server
return|;
block|}
comment|/** An RPC Server. */
specifier|public
specifier|static
class|class
name|Server
extends|extends
name|SecureServer
block|{
specifier|private
name|Object
name|instance
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|implementation
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|ifaces
decl_stmt|;
specifier|private
name|boolean
name|verbose
decl_stmt|;
specifier|private
specifier|static
name|String
name|classNameBase
parameter_list|(
name|String
name|className
parameter_list|)
block|{
name|String
index|[]
name|names
init|=
name|className
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|==
literal|null
operator|||
name|names
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|className
return|;
block|}
return|return
name|names
index|[
name|names
operator|.
name|length
operator|-
literal|1
index|]
return|;
block|}
comment|/** Construct an RPC server.      * @param instance the instance whose methods will be called      * @param conf the configuration to use      * @param bindAddress the address to bind on to listen for connection      * @param port the port to listen for connections on      * @param numHandlers the number of method handler threads to run      * @param verbose whether each call should be logged      * @throws java.io.IOException e      */
specifier|public
name|Server
parameter_list|(
name|Object
name|instance
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|ifaces
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|bindAddress
parameter_list|,
name|int
name|port
parameter_list|,
name|int
name|numHandlers
parameter_list|,
name|int
name|metaHandlerCount
parameter_list|,
name|boolean
name|verbose
parameter_list|,
name|int
name|highPriorityLevel
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|bindAddress
argument_list|,
name|port
argument_list|,
name|Invocation
operator|.
name|class
argument_list|,
name|numHandlers
argument_list|,
name|metaHandlerCount
argument_list|,
name|conf
argument_list|,
name|classNameBase
argument_list|(
name|instance
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|highPriorityLevel
argument_list|)
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
name|this
operator|.
name|implementation
operator|=
name|instance
operator|.
name|getClass
argument_list|()
expr_stmt|;
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
name|this
operator|.
name|ifaces
operator|=
name|ifaces
expr_stmt|;
comment|// create metrics for the advertised interfaces this server implements.
name|this
operator|.
name|rpcMetrics
operator|.
name|createMetrics
argument_list|(
name|this
operator|.
name|ifaces
argument_list|)
expr_stmt|;
block|}
specifier|public
name|AuthenticationTokenSecretManager
name|createSecretManager
parameter_list|()
block|{
if|if
condition|(
name|instance
operator|instanceof
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Server
condition|)
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Server
name|server
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Server
operator|)
name|instance
decl_stmt|;
name|Configuration
name|conf
init|=
name|server
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|long
name|keyUpdateInterval
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.auth.key.update.interval"
argument_list|,
literal|24
operator|*
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|long
name|maxAge
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.auth.token.max.lifetime"
argument_list|,
literal|7
operator|*
literal|24
operator|*
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
return|return
operator|new
name|AuthenticationTokenSecretManager
argument_list|(
name|conf
argument_list|,
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|keyUpdateInterval
argument_list|,
name|maxAge
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startThreads
parameter_list|()
block|{
name|AuthenticationTokenSecretManager
name|mgr
init|=
name|createSecretManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|mgr
operator|!=
literal|null
condition|)
block|{
name|setSecretManager
argument_list|(
name|mgr
argument_list|)
expr_stmt|;
name|mgr
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|authManager
operator|=
operator|new
name|ServiceAuthorizationManager
argument_list|()
expr_stmt|;
name|HBasePolicyProvider
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|authManager
argument_list|)
expr_stmt|;
comment|// continue with base startup
name|super
operator|.
name|startThreads
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|call
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|,
name|Writable
name|param
parameter_list|,
name|long
name|receivedTime
parameter_list|,
name|MonitoredRPCHandler
name|status
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Invocation
name|call
init|=
operator|(
name|Invocation
operator|)
name|param
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|getMethodName
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not find requested method, the usual "
operator|+
literal|"cause is a version mismatch between client and server."
argument_list|)
throw|;
block|}
if|if
condition|(
name|verbose
condition|)
name|log
argument_list|(
literal|"Call: "
operator|+
name|call
argument_list|)
expr_stmt|;
name|Method
name|method
init|=
name|protocol
operator|.
name|getMethod
argument_list|(
name|call
operator|.
name|getMethodName
argument_list|()
argument_list|,
name|call
operator|.
name|getParameterClasses
argument_list|()
argument_list|)
decl_stmt|;
name|method
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|impl
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|protocol
operator|.
name|isAssignableFrom
argument_list|(
name|this
operator|.
name|implementation
argument_list|)
condition|)
block|{
name|impl
operator|=
name|this
operator|.
name|instance
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HBaseRPC
operator|.
name|UnknownProtocolException
argument_list|(
name|protocol
argument_list|)
throw|;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Object
index|[]
name|params
init|=
name|call
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
name|method
operator|.
name|invoke
argument_list|(
name|impl
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|int
name|processingTime
init|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
argument_list|)
decl_stmt|;
name|int
name|qTime
init|=
call|(
name|int
call|)
argument_list|(
name|startTime
operator|-
name|receivedTime
argument_list|)
decl_stmt|;
if|if
condition|(
name|TRACELOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|TRACELOG
operator|.
name|debug
argument_list|(
literal|"Call #"
operator|+
name|CurCall
operator|.
name|get
argument_list|()
operator|.
name|id
operator|+
literal|"; Served: "
operator|+
name|protocol
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"#"
operator|+
name|call
operator|.
name|getMethodName
argument_list|()
operator|+
literal|" queueTime="
operator|+
name|qTime
operator|+
literal|" processingTime="
operator|+
name|processingTime
operator|+
literal|" contents="
operator|+
name|Objects
operator|.
name|describeQuantity
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|rpcMetrics
operator|.
name|rpcQueueTime
operator|.
name|inc
argument_list|(
name|qTime
argument_list|)
expr_stmt|;
name|rpcMetrics
operator|.
name|rpcProcessingTime
operator|.
name|inc
argument_list|(
name|processingTime
argument_list|)
expr_stmt|;
name|rpcMetrics
operator|.
name|inc
argument_list|(
name|call
operator|.
name|getMethodName
argument_list|()
argument_list|,
name|processingTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
name|log
argument_list|(
literal|"Return: "
operator|+
name|value
argument_list|)
expr_stmt|;
return|return
operator|new
name|HbaseObjectWritable
argument_list|(
name|method
operator|.
name|getReturnType
argument_list|()
argument_list|,
name|value
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|Throwable
name|target
init|=
name|e
operator|.
name|getTargetException
argument_list|()
decl_stmt|;
if|if
condition|(
name|target
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|target
throw|;
block|}
name|IOException
name|ioe
init|=
operator|new
name|IOException
argument_list|(
name|target
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ioe
operator|.
name|setStackTrace
argument_list|(
name|target
operator|.
name|getStackTrace
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|e
operator|instanceof
name|IOException
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected throwable object "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|IOException
name|ioe
init|=
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ioe
operator|.
name|setStackTrace
argument_list|(
name|e
operator|.
name|getStackTrace
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|ioe
throw|;
block|}
block|}
block|}
specifier|protected
specifier|static
name|void
name|log
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|String
name|v
init|=
name|value
decl_stmt|;
if|if
condition|(
name|v
operator|!=
literal|null
operator|&&
name|v
operator|.
name|length
argument_list|()
operator|>
literal|55
condition|)
name|v
operator|=
name|v
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|55
argument_list|)
operator|+
literal|"..."
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

