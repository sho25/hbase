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
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Abortable
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingService
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
name|RPCTests
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
name|SmallTests
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|RPCTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRpcHandlerException
block|{
specifier|private
specifier|final
specifier|static
name|Configuration
name|CONF
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|/**    * Tests that the rpc scheduler is called when requests arrive. When Rpc handler thread dies, the    * client will hang and the test will fail. The test is meant to be a unit test to test the    * behavior.    */
specifier|private
class|class
name|AbortServer
implements|implements
name|Abortable
block|{
specifier|private
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|aborted
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|aborted
return|;
block|}
block|}
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: rpcServerImpl={0}"
argument_list|)
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
name|SimpleRpcServer
operator|.
name|class
operator|.
name|getName
argument_list|()
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|NettyRpcServer
operator|.
name|class
operator|.
name|getName
argument_list|()
block|}
argument_list|)
return|;
block|}
annotation|@
name|Parameter
argument_list|(
literal|0
argument_list|)
specifier|public
name|String
name|rpcServerImpl
decl_stmt|;
comment|/*    * This is a unit test to make sure to abort region server when the number of Rpc handler thread    * caught errors exceeds the threshold. Client will hang when RS aborts.    */
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testRpcScheduler
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|PriorityFunction
name|qosFunction
init|=
name|mock
argument_list|(
name|PriorityFunction
operator|.
name|class
argument_list|)
decl_stmt|;
name|Abortable
name|abortable
init|=
operator|new
name|AbortServer
argument_list|()
decl_stmt|;
name|CONF
operator|.
name|set
argument_list|(
name|RpcServerFactory
operator|.
name|CUSTOM_RPC_SERVER_IMPL_CONF_KEY
argument_list|,
name|rpcServerImpl
argument_list|)
expr_stmt|;
name|RpcScheduler
name|scheduler
init|=
operator|new
name|SimpleRpcScheduler
argument_list|(
name|CONF
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|qosFunction
argument_list|,
name|abortable
argument_list|,
literal|0
argument_list|)
decl_stmt|;
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
operator|(
name|BlockingService
operator|)
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
name|CONF
argument_list|,
name|scheduler
argument_list|)
decl_stmt|;
try|try
init|(
name|BlockingRpcClient
name|client
init|=
operator|new
name|BlockingRpcClient
argument_list|(
name|CONF
argument_list|)
init|)
block|{
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|BlockingInterface
name|stub
init|=
name|newBlockingStub
argument_list|(
name|client
argument_list|,
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
argument_list|)
decl_stmt|;
name|stub
operator|.
name|echo
argument_list|(
literal|null
argument_list|,
name|EchoRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
literal|"hello"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
assert|assert
operator|(
name|abortable
operator|.
name|isAborted
argument_list|()
operator|==
literal|true
operator|)
assert|;
block|}
finally|finally
block|{
name|rpcServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

