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
name|IpcProtocol
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
name|ipc
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TestProtos
operator|.
name|EmptyRequestProto
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
name|protobuf
operator|.
name|generated
operator|.
name|TestProtos
operator|.
name|EmptyResponseProto
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
name|protobuf
operator|.
name|generated
operator|.
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|Before
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Test for testing protocol buffer based RPC mechanism.  * This test depends on test.proto definition of types in   * hbase-server/src/test/protobuf/test.proto  * and protobuf service definition from   * hbase-server/src/test/protobuf/test_rpc_service.proto  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
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
literal|"0.0.0.0"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|PORT
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
name|InetSocketAddress
name|addr
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|RpcServer
name|server
decl_stmt|;
specifier|public
interface|interface
name|TestRpcService
extends|extends
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
extends|,
name|IpcProtocol
block|{
specifier|public
name|long
name|VERSION
init|=
literal|1
decl_stmt|;
block|}
specifier|public
specifier|static
class|class
name|PBServerImpl
implements|implements
name|TestRpcService
block|{
annotation|@
name|Override
specifier|public
name|EmptyResponseProto
name|ping
parameter_list|(
name|RpcController
name|unused
parameter_list|,
name|EmptyRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|EmptyResponseProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|EchoResponseProto
name|echo
parameter_list|(
name|RpcController
name|unused
parameter_list|,
name|EchoRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|EchoResponseProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
name|request
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|EmptyResponseProto
name|error
parameter_list|(
name|RpcController
name|unused
parameter_list|,
name|EmptyRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"error"
argument_list|,
operator|new
name|IOException
argument_list|(
literal|"error"
argument_list|)
argument_list|)
throw|;
block|}
block|}
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
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
comment|// Create server side implementation
name|PBServerImpl
name|serverImpl
init|=
operator|new
name|PBServerImpl
argument_list|()
decl_stmt|;
comment|// Get RPC server for server side implementation
name|server
operator|=
name|HBaseServerRPC
operator|.
name|getServer
argument_list|(
name|TestRpcService
operator|.
name|class
argument_list|,
name|serverImpl
argument_list|,
operator|new
name|Class
index|[]
block|{
name|TestRpcService
operator|.
name|class
block|}
argument_list|,
name|ADDRESS
argument_list|,
name|PORT
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addr
operator|=
name|server
operator|.
name|getListenerAddress
argument_list|()
expr_stmt|;
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
specifier|public
name|void
name|testProtoBufRpc
parameter_list|()
throws|throws
name|Exception
block|{
name|ProtobufRpcClientEngine
name|clientEngine
init|=
operator|new
name|ProtobufRpcClientEngine
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|TestRpcService
name|client
init|=
name|clientEngine
operator|.
name|getProxy
argument_list|(
name|TestRpcService
operator|.
name|class
argument_list|,
name|addr
argument_list|,
name|conf
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
comment|// Test ping method
name|EmptyRequestProto
name|emptyRequest
init|=
name|EmptyRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
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
name|client
operator|.
name|echo
argument_list|(
literal|null
argument_list|,
name|echoRequest
argument_list|)
decl_stmt|;
name|Assert
operator|.
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
comment|// Test error method - error should be thrown as RemoteException
try|try
block|{
name|client
operator|.
name|error
argument_list|(
literal|null
argument_list|,
name|emptyRequest
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Expected exception is not thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{       }
block|}
finally|finally
block|{
name|clientEngine
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

