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
name|fail
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
name|shaded
operator|.
name|ipc
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TestProtos
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
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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

begin_comment
comment|/**  * Test for testing protocol buffer based RPC mechanism. This test depends on test.proto definition  * of types in<code>src/test/protobuf/test.proto</code> and protobuf service definition from  *<code>src/test/protobuf/test_rpc_service.proto</code>  */
end_comment

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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestProtoBufRpc
block|{
specifier|public
specifier|final
specifier|static
name|String
name|ADDRESS
init|=
literal|"localhost"
decl_stmt|;
specifier|public
specifier|static
name|int
name|PORT
init|=
literal|0
decl_stmt|;
specifier|private
name|InetSocketAddress
name|isa
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|RpcServerInterface
name|server
decl_stmt|;
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
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Setup server for both protocols
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
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
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.ipc.HBaseServer"
argument_list|)
decl_stmt|;
name|log
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
name|log
operator|=
name|Logger
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.ipc.HBaseServer.trace"
argument_list|)
expr_stmt|;
name|log
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
comment|// Create server side implementation
comment|// Get RPC server for server side implementation
name|this
operator|.
name|server
operator|=
name|RpcServerFactory
operator|.
name|createRpcServer
argument_list|(
literal|null
argument_list|,
literal|"testrpc"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|RpcServer
operator|.
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
name|ADDRESS
argument_list|,
name|PORT
argument_list|)
argument_list|,
name|conf
argument_list|,
operator|new
name|FifoRpcScheduler
argument_list|(
name|conf
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|InetSocketAddress
name|address
init|=
name|server
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
name|this
operator|.
name|isa
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
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
name|protobuf
operator|.
name|ServiceException
operator|.
name|class
comment|/*Thrown when we call stub.error*/
argument_list|)
specifier|public
name|void
name|testProtoBufRpc
parameter_list|()
throws|throws
name|Exception
block|{
name|RpcClient
name|rpcClient
init|=
name|RpcClientFactory
operator|.
name|createClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|)
decl_stmt|;
try|try
block|{
name|BlockingInterface
name|stub
init|=
name|newBlockingStub
argument_list|(
name|rpcClient
argument_list|,
name|this
operator|.
name|isa
argument_list|)
decl_stmt|;
comment|// Test ping method
name|TestProtos
operator|.
name|EmptyRequestProto
name|emptyRequest
init|=
name|TestProtos
operator|.
name|EmptyRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|stub
operator|.
name|ping
argument_list|(
literal|null
argument_list|,
name|emptyRequest
argument_list|)
expr_stmt|;
comment|// Test echo method
name|EchoRequestProto
name|echoRequest
init|=
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
decl_stmt|;
name|EchoResponseProto
name|echoResponse
init|=
name|stub
operator|.
name|echo
argument_list|(
literal|null
argument_list|,
name|echoRequest
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|echoResponse
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"hello"
argument_list|)
expr_stmt|;
name|stub
operator|.
name|error
argument_list|(
literal|null
argument_list|,
name|emptyRequest
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected exception is not thrown"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|rpcClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

