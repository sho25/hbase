begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|RpcClient
operator|.
name|SPECIFIC_WRITE_THREAD
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|TestProtobufRpcServiceImpl
operator|.
name|SERVICE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|TestProtobufRpcServiceImpl
operator|.
name|newBlockingStub
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|Random
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
name|Callable
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|locks
operator|.
name|ReadWriteLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|codec
operator|.
name|Codec
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
name|ipc
operator|.
name|RpcServer
operator|.
name|BlockingServiceAndInterface
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
name|shaded
operator|.
name|ipc
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TestProtos
operator|.
name|EchoRequestProto
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
name|shaded
operator|.
name|ipc
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TestProtos
operator|.
name|EchoResponseProto
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
name|shaded
operator|.
name|ipc
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
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
name|testclassification
operator|.
name|IntegrationTests
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
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestRpcClient
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
name|IntegrationTestRpcClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|int
name|numIterations
init|=
literal|10
decl_stmt|;
specifier|public
name|IntegrationTestRpcClient
parameter_list|()
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|AbstractRpcClient
argument_list|<
name|?
argument_list|>
name|createRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|isSyncClient
parameter_list|)
block|{
return|return
name|isSyncClient
condition|?
operator|new
name|BlockingRpcClient
argument_list|(
name|conf
argument_list|)
else|:
operator|new
name|NettyRpcClient
argument_list|(
name|conf
argument_list|)
block|{
annotation|@
name|Override
name|Codec
name|getCodec
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
specifier|static
name|String
name|BIG_PAYLOAD
decl_stmt|;
static|static
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|<
literal|1024
operator|*
literal|1024
condition|)
block|{
comment|// 2 MB
name|builder
operator|.
name|append
argument_list|(
literal|"big.payload."
argument_list|)
expr_stmt|;
block|}
name|BIG_PAYLOAD
operator|=
name|builder
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
class|class
name|Cluster
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|ReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|InetSocketAddress
argument_list|,
name|RpcServer
argument_list|>
name|rpcServers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RpcServer
argument_list|>
name|serverList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|maxServers
decl_stmt|;
name|int
name|minServers
decl_stmt|;
name|Cluster
parameter_list|(
name|int
name|minServers
parameter_list|,
name|int
name|maxServers
parameter_list|)
block|{
name|this
operator|.
name|minServers
operator|=
name|minServers
expr_stmt|;
name|this
operator|.
name|maxServers
operator|=
name|maxServers
expr_stmt|;
block|}
name|RpcServer
name|startServer
parameter_list|()
throws|throws
name|IOException
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|rpcServers
operator|.
name|size
argument_list|()
operator|>=
name|maxServers
condition|)
block|{
return|return
literal|null
return|;
block|}
name|RpcServer
name|rpcServer
init|=
name|RpcServerFactory
operator|.
name|createRpcServer
argument_list|(
literal|null
argument_list|,
literal|"testRpcServer"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|BlockingServiceAndInterface
argument_list|(
name|SERVICE
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|conf
argument_list|,
operator|new
name|FifoRpcScheduler
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Listener channel is closed"
argument_list|)
throw|;
block|}
name|rpcServers
operator|.
name|put
argument_list|(
name|address
argument_list|,
name|rpcServer
argument_list|)
expr_stmt|;
name|serverList
operator|.
name|add
argument_list|(
name|rpcServer
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started server: "
operator|+
name|address
argument_list|)
expr_stmt|;
return|return
name|rpcServer
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|stopRandomServer
parameter_list|()
throws|throws
name|Exception
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
name|RpcServer
name|rpcServer
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|rpcServers
operator|.
name|size
argument_list|()
operator|<=
name|minServers
condition|)
block|{
return|return;
block|}
name|int
name|size
init|=
name|rpcServers
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|rand
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|rpcServer
operator|=
name|serverList
operator|.
name|remove
argument_list|(
name|rand
argument_list|)
expr_stmt|;
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
if|if
condition|(
name|address
operator|==
literal|null
condition|)
block|{
comment|// Throw exception here. We can't remove this instance from the server map because
comment|// we no longer have access to its map key
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Listener channel is closed"
argument_list|)
throw|;
block|}
name|rpcServers
operator|.
name|remove
argument_list|(
name|address
argument_list|)
expr_stmt|;
if|if
condition|(
name|rpcServer
operator|!=
literal|null
condition|)
block|{
name|stopServer
argument_list|(
name|rpcServer
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|stopServer
parameter_list|(
name|RpcServer
name|rpcServer
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping server: "
operator|+
name|address
argument_list|)
expr_stmt|;
name|rpcServer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|rpcServer
operator|.
name|join
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopped server: "
operator|+
name|address
argument_list|)
expr_stmt|;
block|}
name|void
name|stopRunning
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|RpcServer
name|rpcServer
range|:
name|serverList
control|)
block|{
name|stopServer
argument_list|(
name|rpcServer
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
name|RpcServer
name|getRandomServer
parameter_list|()
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|size
init|=
name|rpcServers
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|rand
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|size
argument_list|)
decl_stmt|;
return|return
name|serverList
operator|.
name|get
argument_list|(
name|rand
argument_list|)
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|MiniChaosMonkey
extends|extends
name|Thread
block|{
name|AtomicBoolean
name|running
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Cluster
name|cluster
decl_stmt|;
specifier|public
name|MiniChaosMonkey
parameter_list|(
name|Cluster
name|cluster
parameter_list|)
block|{
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|random
operator|.
name|nextInt
argument_list|()
operator|%
literal|2
condition|)
block|{
case|case
literal|0
case|:
comment|//start a server
try|try
block|{
name|cluster
operator|.
name|startServer
argument_list|()
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
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|exception
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|1
case|:
comment|// stop a server
try|try
block|{
name|cluster
operator|.
name|stopRandomServer
argument_list|()
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
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|exception
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
default|default:
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|stopRunning
parameter_list|()
block|{
name|running
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|void
name|rethrowException
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|exception
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
name|exception
operator|.
name|get
argument_list|()
throw|;
block|}
block|}
block|}
specifier|static
class|class
name|SimpleClient
extends|extends
name|Thread
block|{
name|AbstractRpcClient
argument_list|<
name|?
argument_list|>
name|rpcClient
decl_stmt|;
name|AtomicBoolean
name|running
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|sending
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Cluster
name|cluster
decl_stmt|;
name|String
name|id
decl_stmt|;
name|long
name|numCalls
init|=
literal|0
decl_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|SimpleClient
parameter_list|(
name|Cluster
name|cluster
parameter_list|,
name|AbstractRpcClient
argument_list|<
name|?
argument_list|>
name|rpcClient
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
name|this
operator|.
name|rpcClient
operator|=
name|rpcClient
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|setName
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
name|boolean
name|isBigPayload
init|=
name|random
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|String
name|message
init|=
name|isBigPayload
condition|?
name|BIG_PAYLOAD
else|:
name|id
operator|+
name|numCalls
decl_stmt|;
name|EchoRequestProto
name|param
init|=
name|EchoRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
name|message
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|EchoResponseProto
name|ret
decl_stmt|;
name|RpcServer
name|server
init|=
name|cluster
operator|.
name|getRandomServer
argument_list|()
decl_stmt|;
try|try
block|{
name|sending
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|BlockingInterface
name|stub
init|=
name|newBlockingStub
argument_list|(
name|rpcClient
argument_list|,
name|server
operator|.
name|getListenerAddress
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|=
name|stub
operator|.
name|echo
argument_list|(
literal|null
argument_list|,
name|param
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
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
continue|continue;
comment|// expected in case connection is closing or closed
block|}
try|try
block|{
name|assertNotNull
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|message
argument_list|,
name|ret
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|exception
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|numCalls
operator|++
expr_stmt|;
block|}
block|}
name|void
name|stopRunning
parameter_list|()
block|{
name|running
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isSending
parameter_list|()
block|{
return|return
name|sending
operator|.
name|get
argument_list|()
return|;
block|}
name|void
name|rethrowException
parameter_list|()
throws|throws
name|Throwable
block|{
if|if
condition|(
name|exception
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
name|exception
operator|.
name|get
argument_list|()
throw|;
block|}
block|}
block|}
comment|/*   Test that not started connections are successfully removed from connection pool when   rpc client is closing.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|30000
argument_list|)
specifier|public
name|void
name|testRpcWithWriteThread
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
literal|"Starting test"
argument_list|)
expr_stmt|;
name|Cluster
name|cluster
init|=
operator|new
name|Cluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|startServer
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|SPECIFIC_WRITE_THREAD
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|AbstractRpcClient
argument_list|<
name|?
argument_list|>
name|rpcClient
init|=
name|createRpcClient
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|SimpleClient
name|client
init|=
operator|new
name|SimpleClient
argument_list|(
name|cluster
argument_list|,
name|rpcClient
argument_list|,
literal|"Client1"
argument_list|)
decl_stmt|;
name|client
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|client
operator|.
name|isSending
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|stopRunning
argument_list|()
expr_stmt|;
name|rpcClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|1800000
argument_list|)
specifier|public
name|void
name|testRpcWithChaosMonkeyWithSyncClient
parameter_list|()
throws|throws
name|Throwable
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numIterations
condition|;
name|i
operator|++
control|)
block|{
name|TimeoutThread
operator|.
name|runWithTimeout
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|testRpcWithChaosMonkey
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|Exception
condition|)
block|{
throw|throw
operator|(
name|Exception
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|,
literal|180000
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|900000
argument_list|)
annotation|@
name|Ignore
comment|// TODO: test fails with async client
specifier|public
name|void
name|testRpcWithChaosMonkeyWithAsyncClient
parameter_list|()
throws|throws
name|Throwable
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numIterations
condition|;
name|i
operator|++
control|)
block|{
name|TimeoutThread
operator|.
name|runWithTimeout
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|testRpcWithChaosMonkey
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|Exception
condition|)
block|{
throw|throw
operator|(
name|Exception
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|,
literal|90000
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|TimeoutThread
extends|extends
name|Thread
block|{
name|long
name|timeout
decl_stmt|;
specifier|public
name|TimeoutThread
parameter_list|(
name|long
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|printThreadInfo
argument_list|(
name|System
operator|.
name|err
argument_list|,
literal|"TEST TIMEOUT STACK DUMP"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// a timeout happened
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// this is what we want
block|}
block|}
comment|// runs in the same thread context but injects a timeout thread which will exit the JVM on
comment|// timeout
specifier|static
name|void
name|runWithTimeout
parameter_list|(
name|Callable
argument_list|<
name|?
argument_list|>
name|callable
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|Exception
block|{
name|TimeoutThread
name|thread
init|=
operator|new
name|TimeoutThread
argument_list|(
name|timeout
argument_list|)
decl_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
name|callable
operator|.
name|call
argument_list|()
expr_stmt|;
name|thread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testRpcWithChaosMonkey
parameter_list|(
name|boolean
name|isSyncClient
parameter_list|)
throws|throws
name|Throwable
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting test"
argument_list|)
expr_stmt|;
name|Cluster
name|cluster
init|=
operator|new
name|Cluster
argument_list|(
literal|10
argument_list|,
literal|100
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|cluster
operator|.
name|startServer
argument_list|()
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|SimpleClient
argument_list|>
name|clients
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|30
argument_list|)
decl_stmt|;
comment|// all threads should share the same rpc client
name|AbstractRpcClient
argument_list|<
name|?
argument_list|>
name|rpcClient
init|=
name|createRpcClient
argument_list|(
name|conf
argument_list|,
name|isSyncClient
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
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|String
name|clientId
init|=
literal|"client_"
operator|+
name|i
operator|+
literal|"_"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting client: "
operator|+
name|clientId
argument_list|)
expr_stmt|;
name|SimpleClient
name|client
init|=
operator|new
name|SimpleClient
argument_list|(
name|cluster
argument_list|,
name|rpcClient
argument_list|,
name|clientId
argument_list|)
decl_stmt|;
name|client
operator|.
name|start
argument_list|()
expr_stmt|;
name|clients
operator|.
name|add
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting MiniChaosMonkey"
argument_list|)
expr_stmt|;
name|MiniChaosMonkey
name|cm
init|=
operator|new
name|MiniChaosMonkey
argument_list|(
name|cluster
argument_list|)
decl_stmt|;
name|cm
operator|.
name|start
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|30000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping MiniChaosMonkey"
argument_list|)
expr_stmt|;
name|cm
operator|.
name|stopRunning
argument_list|()
expr_stmt|;
name|cm
operator|.
name|join
argument_list|()
expr_stmt|;
name|cm
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping clients"
argument_list|)
expr_stmt|;
for|for
control|(
name|SimpleClient
name|client
range|:
name|clients
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping client: "
operator|+
name|client
operator|.
name|id
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|client
operator|.
name|id
operator|+
literal|" numCalls:"
operator|+
name|client
operator|.
name|numCalls
argument_list|)
expr_stmt|;
name|client
operator|.
name|stopRunning
argument_list|()
expr_stmt|;
name|client
operator|.
name|join
argument_list|()
expr_stmt|;
name|client
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|client
operator|.
name|numCalls
operator|>
literal|10
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping RpcClient"
argument_list|)
expr_stmt|;
name|rpcClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping Cluster"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|stopRunning
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

