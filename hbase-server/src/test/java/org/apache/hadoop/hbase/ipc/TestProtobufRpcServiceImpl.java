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
name|ServerName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|AddrResponseProto
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
name|Interface
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
name|Threads
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TestProtobufRpcServiceImpl
implements|implements
name|BlockingInterface
block|{
specifier|public
specifier|static
specifier|final
name|BlockingService
name|SERVICE
init|=
name|TestProtobufRpcProto
operator|.
name|newReflectiveBlockingService
argument_list|(
operator|new
name|TestProtobufRpcServiceImpl
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|BlockingInterface
name|newBlockingStub
parameter_list|(
name|RpcClient
name|client
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|newBlockingStub
argument_list|(
name|client
argument_list|,
name|addr
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|BlockingInterface
name|newBlockingStub
parameter_list|(
name|RpcClient
name|client
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TestProtobufRpcProto
operator|.
name|newBlockingStub
argument_list|(
name|client
operator|.
name|createBlockingRpcChannel
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|addr
operator|.
name|getHostName
argument_list|()
argument_list|,
name|addr
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
name|user
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Interface
name|newStub
parameter_list|(
name|RpcClient
name|client
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TestProtobufRpcProto
operator|.
name|newStub
argument_list|(
name|client
operator|.
name|createRpcChannel
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|addr
operator|.
name|getHostName
argument_list|()
argument_list|,
name|addr
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
argument_list|)
return|;
block|}
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
name|EmptyResponseProto
operator|.
name|getDefaultInstance
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
name|HBaseRpcController
condition|)
block|{
name|HBaseRpcController
name|pcrc
init|=
operator|(
name|HBaseRpcController
operator|)
name|controller
decl_stmt|;
comment|// If cells, scan them to check we are able to iterate what we were given and since this is an
comment|// echo, just put them back on the controller creating a new block. Tests our block building.
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
argument_list|<>
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
name|pcrc
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
throw|throw
operator|new
name|ServiceException
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"server error!"
argument_list|)
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|EmptyResponseProto
name|pause
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|PauseRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
name|request
operator|.
name|getMs
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|EmptyResponseProto
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|AddrResponseProto
name|addr
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
name|AddrResponseProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setAddr
argument_list|(
name|RpcServer
operator|.
name|getRemoteAddress
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

