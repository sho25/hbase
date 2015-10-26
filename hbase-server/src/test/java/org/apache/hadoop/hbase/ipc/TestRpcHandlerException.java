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
name|mockito
operator|.
name|Mockito
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

begin_class
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
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestRpcHandlerException
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|String
name|example
init|=
literal|"xyz"
decl_stmt|;
specifier|static
name|byte
index|[]
name|CELL_BYTES
init|=
name|example
operator|.
name|getBytes
argument_list|()
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
name|RpcExecutor
name|rpcExecutor
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// We are using the test TestRpcServiceProtos generated classes and Service because they are
comment|// available and basic with methods like 'echo', and ping. Below we make a blocking service
comment|// by passing in implementation of blocking interface. We use this service in all tests that
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
name|Error
throws|,
name|RuntimeException
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
throw|throw
operator|new
name|StackOverflowError
argument_list|()
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|StackOverflowError
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
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
comment|/** Tests that the rpc scheduler is called when requests arrive.    *  When Rpc handler thread dies, the client will hang and the test will fail.    *  The test is meant to be a unit test to test the behavior.    *    * */
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
comment|/* This is a unit test to make sure to abort region server when the number of Rpc handler thread    * caught errors exceeds the threshold. Client will hang when RS aborts.    */
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
operator|new
name|TestRpcServer
argument_list|(
name|scheduler
argument_list|)
decl_stmt|;
name|RpcClientImpl
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
name|PayloadCarryingRpcController
name|controller
init|=
operator|new
name|PayloadCarryingRpcController
argument_list|(
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
name|client
operator|.
name|call
argument_list|(
name|controller
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

