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
name|coprocessor
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
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|HBaseTestingUtility
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
name|coprocessor
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|DummyRegionServerEndpointProtos
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
name|coprocessor
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|DummyRegionServerEndpointProtos
operator|.
name|DummyRequest
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
name|coprocessor
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|DummyRegionServerEndpointProtos
operator|.
name|DummyResponse
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
name|coprocessor
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|DummyRegionServerEndpointProtos
operator|.
name|DummyService
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
name|CoprocessorRpcUtils
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
name|RemoteWithExtrasException
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
name|ServerRpcController
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
name|testclassification
operator|.
name|CoprocessorTests
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
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
name|Service
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestRegionServerCoprocessorEndpoint
block|{
specifier|public
specifier|static
specifier|final
name|FileNotFoundException
name|WHAT_TO_THROW
init|=
operator|new
name|FileNotFoundException
argument_list|(
literal|"/file.txt"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|CONF
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DUMMY_VALUE
init|=
literal|"val"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|CONF
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|CONF
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|,
name|DummyRegionServerEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEndpoint
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ServerName
name|serverName
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
specifier|final
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
specifier|final
name|CoprocessorRpcUtils
operator|.
name|BlockingRpcCallback
argument_list|<
name|DummyRegionServerEndpointProtos
operator|.
name|DummyResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|CoprocessorRpcUtils
operator|.
name|BlockingRpcCallback
argument_list|<>
argument_list|()
decl_stmt|;
name|DummyRegionServerEndpointProtos
operator|.
name|DummyService
name|service
init|=
name|ProtobufUtil
operator|.
name|newServiceStub
argument_list|(
name|DummyRegionServerEndpointProtos
operator|.
name|DummyService
operator|.
name|class
argument_list|,
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|coprocessorService
argument_list|(
name|serverName
argument_list|)
argument_list|)
decl_stmt|;
name|service
operator|.
name|dummyCall
argument_list|(
name|controller
argument_list|,
name|DummyRegionServerEndpointProtos
operator|.
name|DummyRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DUMMY_VALUE
argument_list|,
name|rpcCallback
operator|.
name|get
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|controller
operator|.
name|failedOnException
argument_list|()
condition|)
block|{
throw|throw
name|controller
operator|.
name|getFailedOn
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEndpointExceptions
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ServerName
name|serverName
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
specifier|final
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
specifier|final
name|CoprocessorRpcUtils
operator|.
name|BlockingRpcCallback
argument_list|<
name|DummyRegionServerEndpointProtos
operator|.
name|DummyResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|CoprocessorRpcUtils
operator|.
name|BlockingRpcCallback
argument_list|<>
argument_list|()
decl_stmt|;
name|DummyRegionServerEndpointProtos
operator|.
name|DummyService
name|service
init|=
name|ProtobufUtil
operator|.
name|newServiceStub
argument_list|(
name|DummyRegionServerEndpointProtos
operator|.
name|DummyService
operator|.
name|class
argument_list|,
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|coprocessorService
argument_list|(
name|serverName
argument_list|)
argument_list|)
decl_stmt|;
name|service
operator|.
name|dummyThrow
argument_list|(
name|controller
argument_list|,
name|DummyRegionServerEndpointProtos
operator|.
name|DummyRequest
operator|.
name|getDefaultInstance
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|rpcCallback
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|controller
operator|.
name|failedOnException
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|WHAT_TO_THROW
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|,
operator|(
operator|(
name|RemoteWithExtrasException
operator|)
name|controller
operator|.
name|getFailedOn
argument_list|()
operator|.
name|getCause
argument_list|()
operator|)
operator|.
name|getClassName
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|DummyRegionServerEndpoint
extends|extends
name|DummyService
implements|implements
name|Coprocessor
implements|,
name|SingletonCoprocessorService
block|{
annotation|@
name|Override
specifier|public
name|Service
name|getService
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|void
name|dummyCall
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DummyRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|DummyResponse
argument_list|>
name|callback
parameter_list|)
block|{
name|callback
operator|.
name|run
argument_list|(
name|DummyResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setValue
argument_list|(
name|DUMMY_VALUE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dummyThrow
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DummyRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|DummyResponse
argument_list|>
name|done
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|WHAT_TO_THROW
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

