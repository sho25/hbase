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
name|generated
operator|.
name|RPCProtos
operator|.
name|RpcRequestBody
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
name|protobuf
operator|.
name|Message
import|;
end_import

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
specifier|private
specifier|static
class|class
name|TestRpcServer
extends|extends
name|HBaseServer
block|{
name|TestRpcServer
parameter_list|()
throws|throws
name|IOException
block|{
name|super
argument_list|(
literal|"0.0.0.0"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
literal|"TestRpcServer"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Message
name|call
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|IpcProtocol
argument_list|>
name|protocol
parameter_list|,
name|RpcRequestBody
name|param
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
name|param
return|;
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
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|HBaseClient
name|client
init|=
operator|new
name|HBaseClient
argument_list|(
name|conf
argument_list|,
name|spyFactory
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
try|try
block|{
name|client
operator|.
name|call
argument_list|(
name|RpcRequestBody
operator|.
name|getDefaultInstance
argument_list|()
argument_list|,
name|address
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
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
block|}
block|}
end_class

end_unit

