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
name|client
operator|.
name|MetricsConnection
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
name|BlockingRpcChannel
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
name|BlockingService
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
name|Descriptors
operator|.
name|MethodDescriptor
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
name|Message
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
comment|// We are using the test TestRpcServiceProtos generated classes and Service because they are
comment|// available and basic with methods like 'echo', and ping. Below we make a blocking service
comment|// by passing in implementation of blocking interface. We use this service in all tests that
comment|// follow.
specifier|static
specifier|final
name|BlockingService
name|SERVICE
init|=
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|newReflectiveBlockingService
argument_list|(
operator|new
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|EmptyResponseProto
name|ping
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|EmptyRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|EmptyResponseProto
name|error
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|EmptyRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|EchoResponseProto
name|echo
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|EchoRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
if|if
condition|(
name|controller
operator|instanceof
name|PayloadCarryingRpcController
condition|)
block|{
name|PayloadCarryingRpcController
name|pcrc
init|=
operator|(
name|PayloadCarryingRpcController
operator|)
name|controller
decl_stmt|;
comment|// If cells, scan them to check we are able to iterate what we were given and since
comment|// this is
comment|// an echo, just put them back on the controller creating a new block. Tests our
comment|// block
comment|// building.
name|CellScanner
name|cellScanner
init|=
name|pcrc
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|list
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|cellScanner
operator|!=
literal|null
condition|)
block|{
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
name|cellScanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|cellScanner
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|cellScanner
operator|=
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|list
argument_list|)
expr_stmt|;
operator|(
operator|(
name|PayloadCarryingRpcController
operator|)
name|controller
operator|)
operator|.
name|setCellScanner
argument_list|(
name|cellScanner
argument_list|)
expr_stmt|;
block|}
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
block|}
argument_list|)
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
argument_list|)
expr_stmt|;
block|}
name|TestRpcServer
parameter_list|(
name|RpcScheduler
name|scheduler
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
name|CONF
argument_list|,
name|scheduler
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|BlockingService
name|service
parameter_list|,
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|long
name|receiveTime
parameter_list|,
name|MonitoredRPCHandler
name|status
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|call
argument_list|(
name|service
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
name|cellScanner
argument_list|,
name|receiveTime
argument_list|,
name|status
argument_list|)
return|;
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
comment|/**    * Ensure we do not HAVE TO HAVE a codec.    * @throws InterruptedException    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testNoCodec
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|AbstractRpcClient
name|client
init|=
name|createRpcClientNoCodec
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
block|{
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|MethodDescriptor
name|md
init|=
name|SERVICE
operator|.
name|getDescriptorForType
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"echo"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|message
init|=
literal|"hello"
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
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|r
init|=
name|client
operator|.
name|call
argument_list|(
literal|null
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
name|md
operator|.
name|getOutputType
argument_list|()
operator|.
name|toProto
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|address
argument_list|,
operator|new
name|MetricsConnection
operator|.
name|CallStats
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|getSecond
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
comment|// Silly assertion that the message is in the returned pb.
name|assertTrue
argument_list|(
name|r
operator|.
name|getFirst
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
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
comment|/**    * It is hard to verify the compression is actually happening under the wraps. Hope that if    * unsupported, we'll get an exception out of some time (meantime, have to trace it manually to    * confirm that compression is happening down in the client and server).    * @throws IOException    * @throws InterruptedException    * @throws SecurityException    * @throws NoSuchMethodException    */
annotation|@
name|Test
specifier|public
name|void
name|testCompressCellBlock
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|SecurityException
throws|,
name|NoSuchMethodException
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
argument_list|<
name|Cell
argument_list|>
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
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
try|try
block|{
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|MethodDescriptor
name|md
init|=
name|SERVICE
operator|.
name|getDescriptorForType
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"echo"
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
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|r
init|=
name|client
operator|.
name|call
argument_list|(
name|pcrc
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
name|md
operator|.
name|getOutputType
argument_list|()
operator|.
name|toProto
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|address
argument_list|,
operator|new
name|MetricsConnection
operator|.
name|CallStats
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|r
operator|.
name|getSecond
argument_list|()
operator|.
name|advance
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|CELL
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getSecond
argument_list|()
operator|.
name|current
argument_list|()
argument_list|)
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
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|AbstractRpcClient
name|client
init|=
name|createRpcClientRTEDuringConnectionSetup
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|MethodDescriptor
name|md
init|=
name|SERVICE
operator|.
name|getDescriptorForType
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"echo"
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
name|client
operator|.
name|call
argument_list|(
literal|null
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
literal|null
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|address
argument_list|,
operator|new
name|MetricsConnection
operator|.
name|CallStats
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
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|rpcServer
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
name|testRpcScheduler
parameter_list|()
throws|throws
name|IOException
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
name|AbstractRpcClient
name|client
init|=
name|createRpcClient
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
try|try
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
name|MethodDescriptor
name|md
init|=
name|SERVICE
operator|.
name|getDescriptorForType
argument_list|()
operator|.
name|findMethodByName
argument_list|(
literal|"echo"
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
name|client
operator|.
name|call
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
name|md
argument_list|,
name|param
argument_list|,
name|md
operator|.
name|getOutputType
argument_list|()
operator|.
name|toProto
argument_list|()
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|address
argument_list|,
operator|new
name|MetricsConnection
operator|.
name|CallStats
argument_list|()
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
comment|/**    * Instance of RpcServer that echoes client hostAddress back to client    */
specifier|static
class|class
name|TestRpcServer1
extends|extends
name|RpcServer
block|{
specifier|private
specifier|static
name|BlockingInterface
name|SERVICE1
init|=
operator|new
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
argument_list|()
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
specifier|final
name|InetAddress
name|remoteAddr
init|=
name|TestRpcServer1
operator|.
name|getRemoteAddress
argument_list|()
decl_stmt|;
specifier|final
name|String
name|message
init|=
name|remoteAddr
operator|==
literal|null
condition|?
literal|"NULL"
else|:
name|remoteAddr
operator|.
name|getHostAddress
argument_list|()
decl_stmt|;
return|return
name|EchoResponseProto
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
decl_stmt|;
name|TestRpcServer1
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
argument_list|)
expr_stmt|;
block|}
name|TestRpcServer1
parameter_list|(
name|RpcScheduler
name|scheduler
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|"testRemoteAddressInCallObject"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|BlockingServiceAndInterface
argument_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|newReflectiveBlockingService
argument_list|(
name|SERVICE1
argument_list|)
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
specifier|final
name|RpcScheduler
name|scheduler
init|=
operator|new
name|FifoRpcScheduler
argument_list|(
name|CONF
argument_list|,
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|TestRpcServer1
name|rpcServer
init|=
operator|new
name|TestRpcServer1
argument_list|(
name|scheduler
argument_list|)
decl_stmt|;
specifier|final
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
specifier|final
name|AbstractRpcClient
name|client
init|=
operator|new
name|RpcClientImpl
argument_list|(
name|CONF
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|,
name|localAddr
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
specifier|final
name|InetSocketAddress
name|isa
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
if|if
condition|(
name|isa
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
specifier|final
name|BlockingRpcChannel
name|channel
init|=
name|client
operator|.
name|createBlockingRpcChannel
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|isa
operator|.
name|getHostName
argument_list|()
argument_list|,
name|isa
operator|.
name|getPort
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
name|stub
init|=
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|newBlockingStub
argument_list|(
name|channel
argument_list|)
decl_stmt|;
specifier|final
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
literal|"GetRemoteAddress"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
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
name|Assert
operator|.
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
name|echoResponse
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
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

