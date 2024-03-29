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
name|nio
operator|.
name|channels
operator|.
name|SocketChannel
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
name|HBaseClassTestRule
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
name|nio
operator|.
name|ByteBuff
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
name|MediumTests
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
name|RPCTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RPCTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestBlockingIPC
extends|extends
name|AbstractTestIPC
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestBlockingIPC
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|RpcServer
name|createRpcServer
parameter_list|(
name|Server
name|server
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|RpcServer
operator|.
name|BlockingServiceAndInterface
argument_list|>
name|services
parameter_list|,
name|InetSocketAddress
name|bindAddress
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RpcScheduler
name|scheduler
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|RpcServerFactory
operator|.
name|createRpcServer
argument_list|(
name|server
argument_list|,
name|name
argument_list|,
name|services
argument_list|,
name|bindAddress
argument_list|,
name|conf
argument_list|,
name|scheduler
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|BlockingRpcClient
name|createRpcClientNoCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|BlockingRpcClient
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
annotation|@
name|Override
specifier|protected
name|BlockingRpcClient
name|createRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|BlockingRpcClient
argument_list|(
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|BlockingRpcClient
name|createRpcClientRTEDuringConnectionSetup
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|BlockingRpcClient
argument_list|(
name|conf
argument_list|)
block|{
annotation|@
name|Override
name|boolean
name|isTcpNoDelay
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Injected fault"
argument_list|)
throw|;
block|}
block|}
return|;
block|}
specifier|private
specifier|static
class|class
name|TestFailingRpcServer
extends|extends
name|SimpleRpcServer
block|{
name|TestFailingRpcServer
parameter_list|(
name|Server
name|server
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|RpcServer
operator|.
name|BlockingServiceAndInterface
argument_list|>
name|services
parameter_list|,
name|InetSocketAddress
name|bindAddress
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RpcScheduler
name|scheduler
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|server
argument_list|,
name|name
argument_list|,
name|services
argument_list|,
name|bindAddress
argument_list|,
name|conf
argument_list|,
name|scheduler
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|final
class|class
name|FailingConnection
extends|extends
name|SimpleServerRpcConnection
block|{
specifier|private
name|FailingConnection
parameter_list|(
name|TestFailingRpcServer
name|rpcServer
parameter_list|,
name|SocketChannel
name|channel
parameter_list|,
name|long
name|lastContact
parameter_list|)
block|{
name|super
argument_list|(
name|rpcServer
argument_list|,
name|channel
argument_list|,
name|lastContact
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processRequest
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// this will throw exception after the connection header is read, and an RPC is sent
comment|// from client
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Failing for test"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|SimpleServerRpcConnection
name|getConnection
parameter_list|(
name|SocketChannel
name|channel
parameter_list|,
name|long
name|time
parameter_list|)
block|{
return|return
operator|new
name|FailingConnection
argument_list|(
name|this
argument_list|,
name|channel
argument_list|,
name|time
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|RpcServer
name|createTestFailingRpcServer
parameter_list|(
name|Server
name|server
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|RpcServer
operator|.
name|BlockingServiceAndInterface
argument_list|>
name|services
parameter_list|,
name|InetSocketAddress
name|bindAddress
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RpcScheduler
name|scheduler
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TestFailingRpcServer
argument_list|(
name|server
argument_list|,
name|name
argument_list|,
name|services
argument_list|,
name|bindAddress
argument_list|,
name|conf
argument_list|,
name|scheduler
argument_list|)
return|;
block|}
block|}
end_class

end_unit

