begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   *   * Licensed to the Apache Software Foundation (ASF) under one   * or more contributor license agreements.  See the NOTICE file   * distributed with this work for additional information   * regarding copyright ownership.  The ASF licenses this file   * to you under the Apache License, Version 2.0 (the   * "License"); you may not use this file except in compliance   * with the License.  You may obtain a copy of the License at   *   *     http://www.apache.org/licenses/LICENSE-2.0   *   * Unless required by applicable law or agreed to in writing, software   * distributed under the License is distributed on an "AS IS" BASIS,   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   * See the License for the specific language governing permissions and   * limitations under the License.   */
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
name|anyInt
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
name|doThrow
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
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
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
name|javax
operator|.
name|net
operator|.
name|SocketFactory
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
name|CellScannable
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
name|HRegionInfo
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
name|KeyValueUtil
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
name|SmallTests
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
name|Put
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
name|RowMutations
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
name|protobuf
operator|.
name|RequestConverter
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
name|ClientProtos
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
name|ClientProtos
operator|.
name|MutationProto
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
name|ClientProtos
operator|.
name|RegionAction
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
name|HBaseProtos
operator|.
name|RegionSpecifier
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
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|ByteString
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
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestIPC
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestIPC
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|static
name|Cell
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
name|Cell
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
comment|// We are using the test TestRpcServiceProtos generated classes and Service because they are
comment|// available and basic with methods like 'echo', and ping.  Below we make a blocking service
comment|// by passing in implementation of blocking interface.  We use this service in all tests that
comment|// follow.
specifier|private
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
comment|// TODO Auto-generated method stub
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
comment|// TODO Auto-generated method stub
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
comment|// If cells, scan them to check we are able to iterate what we were given and since this is
comment|// an echo, just put them back on the controller creating a new block.  Tests our block
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
comment|/**    * Instance of server.  We actually don't do anything speical in here so could just use    * HBaseRpcServer directly.    */
specifier|private
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
literal|"0.0.0.0"
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
name|RpcClient
name|client
init|=
operator|new
name|RpcClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
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
name|InetSocketAddress
name|address
init|=
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
decl_stmt|;
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
literal|null
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
literal|0
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
name|stop
argument_list|()
expr_stmt|;
name|rpcServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * It is hard to verify the compression is actually happening under the wraps.  Hope that if    * unsupported, we'll get an exception out of some time (meantime, have to trace it manually    * to confirm that compression is happening down in the client and server).    * @throws IOException    * @throws InterruptedException    * @throws SecurityException    * @throws NoSuchMethodException    */
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
name|doSimpleTest
argument_list|(
name|conf
argument_list|,
operator|new
name|RpcClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doSimpleTest
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|RpcClient
name|client
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
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
name|cells
operator|.
name|add
argument_list|(
name|CELL
argument_list|)
expr_stmt|;
try|try
block|{
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
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
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
literal|0
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
name|stop
argument_list|()
expr_stmt|;
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
name|SocketFactory
name|spyFactory
init|=
name|spy
argument_list|(
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Socket
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Socket
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Socket
name|s
init|=
name|spy
argument_list|(
operator|(
name|Socket
operator|)
name|invocation
operator|.
name|callRealMethod
argument_list|()
argument_list|)
decl_stmt|;
name|doThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Injected fault"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|s
argument_list|)
operator|.
name|setSoTimeout
argument_list|(
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|spyFactory
argument_list|)
operator|.
name|createSocket
argument_list|()
expr_stmt|;
name|TestRpcServer
name|rpcServer
init|=
operator|new
name|TestRpcServer
argument_list|()
decl_stmt|;
name|RpcClient
name|client
init|=
operator|new
name|RpcClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|,
name|spyFactory
argument_list|)
decl_stmt|;
try|try
block|{
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
literal|null
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|,
name|address
argument_list|,
literal|0
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
name|stop
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
name|RpcClient
name|client
init|=
operator|new
name|RpcClient
argument_list|(
name|CONF
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
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
literal|null
argument_list|,
name|md
argument_list|,
name|param
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|CELL
argument_list|)
argument_list|)
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
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
argument_list|,
literal|0
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
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|SecurityException
throws|,
name|NoSuchMethodException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: TestIPC<CYCLES><CELLS_PER_CYCLE>"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// ((Log4JLogger)HBaseServer.LOG).getLogger().setLevel(Level.INFO);
comment|// ((Log4JLogger)HBaseClient.LOG).getLogger().setLevel(Level.INFO);
name|int
name|cycles
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|int
name|cellcount
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
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
name|RpcClient
name|client
init|=
operator|new
name|RpcClient
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|BIG_CELL
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
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
name|cellcount
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
try|try
block|{
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
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|User
name|user
init|=
name|User
operator|.
name|getCurrent
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
name|cycles
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<
name|CellScannable
argument_list|>
argument_list|()
decl_stmt|;
comment|// Message param = RequestConverter.buildMultiRequest(HConstants.EMPTY_BYTE_ARRAY, rm);
name|ClientProtos
operator|.
name|RegionAction
operator|.
name|Builder
name|builder
init|=
name|RequestConverter
operator|.
name|buildNoDataRegionAction
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|rm
argument_list|,
name|cells
argument_list|,
name|RegionAction
operator|.
name|newBuilder
argument_list|()
argument_list|,
name|ClientProtos
operator|.
name|Action
operator|.
name|newBuilder
argument_list|()
argument_list|,
name|MutationProto
operator|.
name|newBuilder
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setRegion
argument_list|(
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|)
operator|.
name|setValue
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|100000
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|""
operator|+
name|i
argument_list|)
expr_stmt|;
comment|// Uncomment this for a thread dump every so often.
comment|// ReflectionUtils.printThreadInfo(new PrintWriter(System.out),
comment|//  "Thread dump " + Thread.currentThread().getName());
block|}
name|CellScanner
name|cellScanner
init|=
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cells
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|response
init|=
name|client
operator|.
name|call
argument_list|(
literal|null
argument_list|,
name|md
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|cellScanner
argument_list|,
name|param
argument_list|,
name|user
argument_list|,
name|address
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|/*         int count = 0;         while (p.getSecond().advance()) {           count++;         }         assertEquals(cells.size(), count);*/
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Cycled "
operator|+
name|cycles
operator|+
literal|" time(s) with "
operator|+
name|cellcount
operator|+
literal|" cell(s) in "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|client
operator|.
name|stop
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

