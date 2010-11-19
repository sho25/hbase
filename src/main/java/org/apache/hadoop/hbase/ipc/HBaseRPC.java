begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|DoNotRetryIOException
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
name|ipc
operator|.
name|VersionedProtocol
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
name|UserGroupInformation
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
name|ReflectionUtils
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
name|Method
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
name|Proxy
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
comment|/** A simple RPC mechanism.  *  * This is a local hbase copy of the hadoop RPC so we can do things like  * address HADOOP-414 for hbase-only and try other hbase-specific  * optimizations like using our own version of ObjectWritable.  Class has been  * renamed to avoid confusing it w/ hadoop versions.  *<p>  *  *  * A<i>protocol</i> is a Java interface.  All parameters and return types must  * be one of:  *  *<ul><li>a primitive type,<code>boolean</code>,<code>byte</code>,  *<code>char</code>,<code>short</code>,<code>int</code>,<code>long</code>,  *<code>float</code>,<code>double</code>, or<code>void</code>; or</li>  *  *<li>a {@link String}; or</li>  *  *<li>a {@link Writable}; or</li>  *  *<li>an array of the above types</li></ul>  *  * All methods in the protocol should throw only IOException.  No field data of  * the protocol instance is transmitted.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseRPC
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
literal|"org.apache.hadoop.ipc.HBaseRPC"
argument_list|)
decl_stmt|;
specifier|private
name|HBaseRPC
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|// no public ctor
specifier|private
specifier|static
specifier|final
name|String
name|RPC_ENGINE_PROP
init|=
literal|"hbase.rpc.engine"
decl_stmt|;
comment|// cache of RpcEngines by protocol
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|,
name|RpcEngine
argument_list|>
name|PROTOCOL_ENGINES
init|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|,
name|RpcEngine
argument_list|>
argument_list|()
decl_stmt|;
comment|// track what RpcEngine is used by a proxy class, for stopProxy()
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|,
name|RpcEngine
argument_list|>
name|PROXY_ENGINES
init|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|,
name|RpcEngine
argument_list|>
argument_list|()
decl_stmt|;
comment|// set a protocol to use a non-default RpcEngine
specifier|static
name|void
name|setProtocolEngine
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Class
name|protocol
parameter_list|,
name|Class
name|engine
parameter_list|)
block|{
name|conf
operator|.
name|setClass
argument_list|(
name|RPC_ENGINE_PROP
operator|+
literal|"."
operator|+
name|protocol
operator|.
name|getName
argument_list|()
argument_list|,
name|engine
argument_list|,
name|RpcEngine
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|// return the RpcEngine configured to handle a protocol
specifier|private
specifier|static
specifier|synchronized
name|RpcEngine
name|getProtocolEngine
parameter_list|(
name|Class
name|protocol
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|RpcEngine
name|engine
init|=
name|PROTOCOL_ENGINES
operator|.
name|get
argument_list|(
name|protocol
argument_list|)
decl_stmt|;
if|if
condition|(
name|engine
operator|==
literal|null
condition|)
block|{
comment|// check for a configured default engine
name|Class
argument_list|<
name|?
argument_list|>
name|defaultEngine
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|RPC_ENGINE_PROP
argument_list|,
name|WritableRpcEngine
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// check for a per interface override
name|Class
argument_list|<
name|?
argument_list|>
name|impl
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|RPC_ENGINE_PROP
operator|+
literal|"."
operator|+
name|protocol
operator|.
name|getName
argument_list|()
argument_list|,
name|defaultEngine
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using "
operator|+
name|impl
operator|.
name|getName
argument_list|()
operator|+
literal|" for "
operator|+
name|protocol
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|engine
operator|=
operator|(
name|RpcEngine
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|impl
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|protocol
operator|.
name|isInterface
argument_list|()
condition|)
name|PROXY_ENGINES
operator|.
name|put
argument_list|(
name|Proxy
operator|.
name|getProxyClass
argument_list|(
name|protocol
operator|.
name|getClassLoader
argument_list|()
argument_list|,
name|protocol
argument_list|)
argument_list|,
name|engine
argument_list|)
expr_stmt|;
name|PROTOCOL_ENGINES
operator|.
name|put
argument_list|(
name|protocol
argument_list|,
name|engine
argument_list|)
expr_stmt|;
block|}
return|return
name|engine
return|;
block|}
comment|// return the RpcEngine that handles a proxy object
specifier|private
specifier|static
specifier|synchronized
name|RpcEngine
name|getProxyEngine
parameter_list|(
name|Object
name|proxy
parameter_list|)
block|{
return|return
name|PROXY_ENGINES
operator|.
name|get
argument_list|(
name|proxy
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * A version mismatch for the RPC protocol.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|VersionMismatch
extends|extends
name|IOException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|interfaceName
decl_stmt|;
specifier|private
name|long
name|clientVersion
decl_stmt|;
specifier|private
name|long
name|serverVersion
decl_stmt|;
comment|/**      * Create a version mismatch exception      * @param interfaceName the name of the protocol mismatch      * @param clientVersion the client's version of the protocol      * @param serverVersion the server's version of the protocol      */
specifier|public
name|VersionMismatch
parameter_list|(
name|String
name|interfaceName
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|long
name|serverVersion
parameter_list|)
block|{
name|super
argument_list|(
literal|"Protocol "
operator|+
name|interfaceName
operator|+
literal|" version mismatch. (client = "
operator|+
name|clientVersion
operator|+
literal|", server = "
operator|+
name|serverVersion
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|this
operator|.
name|interfaceName
operator|=
name|interfaceName
expr_stmt|;
name|this
operator|.
name|clientVersion
operator|=
name|clientVersion
expr_stmt|;
name|this
operator|.
name|serverVersion
operator|=
name|serverVersion
expr_stmt|;
block|}
comment|/**      * Get the interface name      * @return the java class name      *          (eg. org.apache.hadoop.mapred.InterTrackerProtocol)      */
specifier|public
name|String
name|getInterfaceName
parameter_list|()
block|{
return|return
name|interfaceName
return|;
block|}
comment|/**      * @return the client's preferred version      */
specifier|public
name|long
name|getClientVersion
parameter_list|()
block|{
return|return
name|clientVersion
return|;
block|}
comment|/**      * @return the server's agreed to version.      */
specifier|public
name|long
name|getServerVersion
parameter_list|()
block|{
return|return
name|serverVersion
return|;
block|}
block|}
comment|/**    * An error requesting an RPC protocol that the server is not serving.    */
specifier|public
specifier|static
class|class
name|UnknownProtocolException
extends|extends
name|DoNotRetryIOException
block|{
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|protocol
decl_stmt|;
specifier|public
name|UnknownProtocolException
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|protocol
parameter_list|)
block|{
name|this
argument_list|(
name|protocol
argument_list|,
literal|"Server is not handling protocol "
operator|+
name|protocol
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|UnknownProtocolException
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|protocol
parameter_list|,
name|String
name|mesg
parameter_list|)
block|{
name|super
argument_list|(
name|mesg
argument_list|)
expr_stmt|;
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
block|}
specifier|public
name|Class
name|getProtocol
parameter_list|()
block|{
return|return
name|protocol
return|;
block|}
block|}
comment|/**    * @param protocol protocol interface    * @param clientVersion which client version we expect    * @param addr address of remote service    * @param conf configuration    * @param maxAttempts max attempts    * @param rpcTimeout timeout for each RPC    * @param timeout timeout in milliseconds    * @return proxy    * @throws IOException e    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|VersionedProtocol
name|waitForProxy
parameter_list|(
name|Class
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|int
name|maxAttempts
parameter_list|,
name|int
name|rpcTimeout
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
comment|// HBase does limited number of reconnects which is different from hadoop.
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|IOException
name|ioe
decl_stmt|;
name|int
name|reconnectAttempts
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
return|return
name|getProxy
argument_list|(
name|protocol
argument_list|,
name|clientVersion
argument_list|,
name|addr
argument_list|,
name|conf
argument_list|,
name|rpcTimeout
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ConnectException
name|se
parameter_list|)
block|{
comment|// namenode has not been started
name|ioe
operator|=
name|se
expr_stmt|;
if|if
condition|(
name|maxAttempts
operator|>=
literal|0
operator|&&
operator|++
name|reconnectAttempts
operator|>=
name|maxAttempts
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Server at "
operator|+
name|addr
operator|+
literal|" could not be reached after "
operator|+
name|reconnectAttempts
operator|+
literal|" tries, giving up."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RetriesExhaustedException
argument_list|(
literal|"Failed setting up proxy "
operator|+
name|protocol
operator|+
literal|" to "
operator|+
name|addr
operator|.
name|toString
argument_list|()
operator|+
literal|" after attempts="
operator|+
name|reconnectAttempts
argument_list|,
name|se
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|SocketTimeoutException
name|te
parameter_list|)
block|{
comment|// namenode is busy
name|LOG
operator|.
name|info
argument_list|(
literal|"Problem connecting to server: "
operator|+
name|addr
argument_list|)
expr_stmt|;
name|ioe
operator|=
name|te
expr_stmt|;
block|}
comment|// check if timed out
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|timeout
operator|>=
name|startTime
condition|)
block|{
throw|throw
name|ioe
throw|;
block|}
comment|// wait for retry
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// IGNORE
block|}
block|}
block|}
comment|/**    * Construct a client-side proxy object that implements the named protocol,    * talking to a server at the named address.    *    * @param protocol interface    * @param clientVersion version we are expecting    * @param addr remote address    * @param conf configuration    * @param factory socket factory    * @param rpcTimeout timeout for each RPC    * @return proxy    * @throws IOException e    */
specifier|public
specifier|static
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
return|return
name|getProxy
argument_list|(
name|protocol
argument_list|,
name|clientVersion
argument_list|,
name|addr
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|,
name|factory
argument_list|,
name|rpcTimeout
argument_list|)
return|;
block|}
comment|/**    * Construct a client-side proxy object that implements the named protocol,    * talking to a server at the named address.    *    * @param protocol interface    * @param clientVersion version we are expecting    * @param addr remote address    * @param ticket ticket    * @param conf configuration    * @param factory socket factory    * @param rpcTimeout timeout for each RPC    * @return proxy    * @throws IOException e    */
specifier|public
specifier|static
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
name|UserGroupInformation
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
name|VersionedProtocol
name|proxy
init|=
name|getProtocolEngine
argument_list|(
name|protocol
argument_list|,
name|conf
argument_list|)
operator|.
name|getProxy
argument_list|(
name|protocol
argument_list|,
name|clientVersion
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
operator|==
name|clientVersion
condition|)
block|{
return|return
name|proxy
return|;
block|}
throw|throw
operator|new
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
comment|/**    * Construct a client-side proxy object with the default SocketFactory    *    * @param protocol interface    * @param clientVersion version we are expecting    * @param addr remote address    * @param conf configuration    * @param rpcTimeout timeout for each RPC    * @return a proxy instance    * @throws IOException e    */
specifier|public
specifier|static
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
name|Configuration
name|conf
parameter_list|,
name|int
name|rpcTimeout
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getProxy
argument_list|(
name|protocol
argument_list|,
name|clientVersion
argument_list|,
name|addr
argument_list|,
name|conf
argument_list|,
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
argument_list|,
name|rpcTimeout
argument_list|)
return|;
block|}
comment|/**    * Stop this proxy and release its invoker's resource    * @param proxy the proxy to be stopped    */
specifier|public
specifier|static
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
name|getProxyEngine
argument_list|(
name|proxy
argument_list|)
operator|.
name|stopProxy
argument_list|(
name|proxy
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Expert: Make multiple, parallel calls to a set of servers.    *    * @param method method to invoke    * @param params array of parameters    * @param addrs array of addresses    * @param conf configuration    * @return values    * @throws IOException e    */
specifier|public
specifier|static
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
name|UserGroupInformation
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
return|return
name|getProtocolEngine
argument_list|(
name|protocol
argument_list|,
name|conf
argument_list|)
operator|.
name|call
argument_list|(
name|method
argument_list|,
name|params
argument_list|,
name|addrs
argument_list|,
name|protocol
argument_list|,
name|ticket
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|/**    * Construct a server for a protocol implementation instance listening on a    * port and address.    *    * @param instance instance    * @param bindAddress bind address    * @param port port to bind to    * @param numHandlers number of handlers to start    * @param verbose verbose flag    * @param conf configuration    * @return Server    * @throws IOException e    */
specifier|public
specifier|static
name|RpcServer
name|getServer
parameter_list|(
specifier|final
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
return|return
name|getServer
argument_list|(
name|instance
operator|.
name|getClass
argument_list|()
argument_list|,
name|instance
argument_list|,
name|ifaces
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
name|conf
argument_list|,
name|highPriorityLevel
argument_list|)
return|;
block|}
comment|/** Construct a server for a protocol implementation instance. */
specifier|public
specifier|static
name|RpcServer
name|getServer
parameter_list|(
name|Class
name|protocol
parameter_list|,
specifier|final
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
name|String
name|bindAddress
parameter_list|,
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
return|return
name|getProtocolEngine
argument_list|(
name|protocol
argument_list|,
name|conf
argument_list|)
operator|.
name|getServer
argument_list|(
name|protocol
argument_list|,
name|instance
argument_list|,
name|ifaces
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
name|conf
argument_list|,
name|highPriorityLevel
argument_list|)
return|;
block|}
block|}
end_class

end_unit

