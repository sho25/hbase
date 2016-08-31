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
name|assertNull
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyObject
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
name|spy
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
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|internal
operator|.
name|verification
operator|.
name|VerificationModeFactory
operator|.
name|times
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
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
name|List
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
name|Cell
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
name|CellScanner
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
name|CellUtil
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
name|KeyValue
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
name|PauseRequestProto
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|Bytes
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
name|compress
operator|.
name|GzipCodec
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
name|StringUtils
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

begin_comment
comment|/**  * Some basic ipc tests.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestIPC
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
name|AbstractTestIPC
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CELL_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xyz"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|KeyValue
name|CELL
init|=
operator|new
name|KeyValue
argument_list|(
name|CELL_BYTES
argument_list|,
name|CELL_BYTES
argument_list|,
name|CELL_BYTES
argument_list|,
name|CELL_BYTES
argument_list|)
decl_stmt|;
specifier|static
name|byte
index|[]
name|BIG_CELL_BYTES
init|=
operator|new
name|byte
index|[
literal|10
operator|*
literal|1024
index|]
decl_stmt|;
specifier|static
name|KeyValue
name|BIG_CELL
init|=
operator|new
name|KeyValue
argument_list|(
name|CELL_BYTES
argument_list|,
name|CELL_BYTES
argument_list|,
name|CELL_BYTES
argument_list|,
name|BIG_CELL_BYTES
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Configuration
name|CONF
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|/**    * Instance of server. We actually don't do anything speical in here so could just use    * HBaseRpcServer directly.    */
specifier|static
class|class
name|TestRpcServer
extends|extends
name|RpcServer
block|{
name|TestRpcServer
parameter_list|()
throws|throws
name|IOException
block|{
name|this
argument_list|(
operator|new
name|FifoRpcScheduler
argument_list|(
name|CONF
argument_list|,
literal|1
argument_list|)
argument_list|,
name|CONF
argument_list|)
expr_stmt|;
block|}
name|TestRpcServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
operator|new
name|FifoRpcScheduler
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|TestRpcServer
parameter_list|(
name|RpcScheduler
name|scheduler
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
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
name|scheduler
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|abstract
name|AbstractRpcClient
name|createRpcClientNoCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * Ensure we do not HAVE TO HAVE a codec.    */
annotation|@
name|Test
specifier|public
name|void
name|testNoCodec
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClientNoCodec
argument_list|(
name|conf
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
name|PayloadCarryingRpcController
name|pcrc
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|()
decl_stmt|;
name|String
name|message
init|=
literal|"hello"
decl_stmt|;
name|assertEquals
argument_list|(
name|message
argument_list|,
name|stub
operator|.
name|echo
argument_list|(
name|pcrc
argument_list|,
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
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|pcrc
operator|.
name|cellScanner
argument_list|()
argument_list|)
expr_stmt|;
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
specifier|protected
specifier|abstract
name|AbstractRpcClient
name|createRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * It is hard to verify the compression is actually happening under the wraps. Hope that if    * unsupported, we'll get an exception out of some time (meantime, have to trace it manually to    * confirm that compression is happening down in the client and server).    */
annotation|@
name|Test
specifier|public
name|void
name|testCompressCellBlock
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.client.rpc.compressor"
argument_list|,
name|GzipCodec
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|3
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|cells
operator|.
name|add
argument_list|(
name|CELL
argument_list|)
expr_stmt|;
block|}
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
argument_list|(
name|conf
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
name|PayloadCarryingRpcController
name|pcrc
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|(
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|message
init|=
literal|"hello"
decl_stmt|;
name|assertEquals
argument_list|(
name|message
argument_list|,
name|stub
operator|.
name|echo
argument_list|(
name|pcrc
argument_list|,
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
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
name|CellScanner
name|cellScanner
init|=
name|pcrc
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|cellScanner
argument_list|)
expr_stmt|;
while|while
condition|(
name|cellScanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|assertEquals
argument_list|(
name|CELL
argument_list|,
name|cellScanner
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
name|index
argument_list|)
expr_stmt|;
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
specifier|protected
specifier|abstract
name|AbstractRpcClient
name|createRpcClientRTEDuringConnectionSetup
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Test
specifier|public
name|void
name|testRTEDuringConnectionSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClientRTEDuringConnectionSetup
argument_list|(
name|conf
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
name|ping
argument_list|(
literal|null
argument_list|,
name|EmptyRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected an exception to have been thrown!"
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
name|info
argument_list|(
literal|"Caught expected exception: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"Injected fault"
argument_list|)
argument_list|)
expr_stmt|;
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
comment|/**    * Tests that the rpc scheduler is called when requests arrive.    */
annotation|@
name|Test
specifier|public
name|void
name|testRpcScheduler
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
throws|,
name|InterruptedException
block|{
name|RpcScheduler
name|scheduler
init|=
name|spy
argument_list|(
operator|new
name|FifoRpcScheduler
argument_list|(
name|CONF
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|RpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|(
name|scheduler
argument_list|,
name|CONF
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|scheduler
argument_list|)
operator|.
name|init
argument_list|(
operator|(
name|RpcScheduler
operator|.
name|Context
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
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
name|verify
argument_list|(
name|scheduler
argument_list|)
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
literal|"hello"
argument_list|)
operator|.
name|build
argument_list|()
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
name|verify
argument_list|(
name|scheduler
argument_list|,
name|times
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|dispatch
argument_list|(
operator|(
name|CallRunner
operator|)
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|rpcServer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|scheduler
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** Tests that the rpc scheduler is called when requests arrive. */
annotation|@
name|Test
specifier|public
name|void
name|testRpcMaxRequestSize
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|RpcServer
operator|.
name|MAX_REQUEST_SIZE
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|RpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
argument_list|(
name|conf
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
name|StringBuilder
name|message
init|=
operator|new
name|StringBuilder
argument_list|(
literal|120
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
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|message
operator|.
name|append
argument_list|(
literal|"hello."
argument_list|)
expr_stmt|;
block|}
comment|// set total RPC size bigger than 100 bytes
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
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|stub
operator|.
name|echo
argument_list|(
operator|new
name|PayloadCarryingRpcController
argument_list|(
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|ImmutableList
operator|.
expr|<
name|Cell
operator|>
name|of
argument_list|(
name|CELL
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|param
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"RPC should have failed because it exceeds max request size"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Caught expected exception: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// the rpc server just close the connection so we can not get the detail message.
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
comment|/**    * Tests that the RpcServer creates& dispatches CallRunner object to scheduler with non-null    * remoteAddress set to its Call Object    * @throws ServiceException    */
annotation|@
name|Test
specifier|public
name|void
name|testRpcServerForNotNullRemoteAddressInCallObject
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|localAddr
init|=
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
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
name|assertEquals
argument_list|(
name|localAddr
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|,
name|stub
operator|.
name|addr
argument_list|(
literal|null
argument_list|,
name|EmptyRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
operator|.
name|getAddr
argument_list|()
argument_list|)
expr_stmt|;
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
annotation|@
name|Test
specifier|public
name|void
name|testRemoteError
parameter_list|()
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
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
name|error
argument_list|(
literal|null
argument_list|,
name|EmptyRequestProto
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Caught expected exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|IOException
name|ioe
init|=
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ioe
operator|instanceof
name|DoNotRetryIOException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ioe
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"server error!"
argument_list|)
argument_list|)
expr_stmt|;
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
annotation|@
name|Test
specifier|public
name|void
name|testTimeout
parameter_list|()
throws|throws
name|IOException
block|{
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
init|(
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
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
name|PayloadCarryingRpcController
name|pcrc
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|()
decl_stmt|;
name|int
name|ms
init|=
literal|1000
decl_stmt|;
name|int
name|timeout
init|=
literal|100
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
name|pcrc
operator|.
name|reset
argument_list|()
expr_stmt|;
name|pcrc
operator|.
name|setCallTimeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
try|try
block|{
name|stub
operator|.
name|pause
argument_list|(
name|pcrc
argument_list|,
name|PauseRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMs
argument_list|(
name|ms
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
name|long
name|waitTime
init|=
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000000
decl_stmt|;
comment|// expected
name|LOG
operator|.
name|info
argument_list|(
literal|"Caught expected exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|IOException
name|ioe
init|=
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ioe
operator|.
name|getCause
argument_list|()
operator|instanceof
name|CallTimeoutException
argument_list|)
expr_stmt|;
comment|// confirm that we got exception before the actual pause.
name|assertTrue
argument_list|(
name|waitTime
operator|<
name|ms
argument_list|)
expr_stmt|;
block|}
block|}
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

