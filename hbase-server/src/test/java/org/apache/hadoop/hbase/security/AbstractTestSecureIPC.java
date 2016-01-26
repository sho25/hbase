begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
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
name|security
operator|.
name|HBaseKerberosUtils
operator|.
name|getKeytabFileForTesting
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
name|security
operator|.
name|HBaseKerberosUtils
operator|.
name|getPrincipalForTesting
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
name|security
operator|.
name|HBaseKerberosUtils
operator|.
name|getSecuredConfiguration
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
name|assertNotSame
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
name|assertSame
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
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
name|fs
operator|.
name|CommonConfigurationKeys
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
name|ipc
operator|.
name|FifoRpcScheduler
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
name|PayloadCarryingRpcController
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
name|RpcClient
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
name|RpcClientFactory
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
name|RpcServerInterface
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
name|minikdc
operator|.
name|MiniKdc
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
name|security
operator|.
name|UserGroupInformation
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
name|security
operator|.
name|UserGroupInformation
operator|.
name|AuthenticationMethod
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
name|Before
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
name|Rule
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
name|rules
operator|.
name|ExpectedException
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestSecureIPC
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|File
name|KEYTAB_FILE
init|=
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"keytab"
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
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
name|TestProtos
operator|.
name|EmptyResponseProto
name|ping
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|TestProtos
operator|.
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
name|TestProtos
operator|.
name|EmptyResponseProto
name|error
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|TestProtos
operator|.
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
name|TestProtos
operator|.
name|EchoResponseProto
name|echo
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|TestProtos
operator|.
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
name|TestProtos
operator|.
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
specifier|private
specifier|static
name|MiniKdc
name|KDC
decl_stmt|;
specifier|private
specifier|static
name|String
name|HOST
init|=
literal|"localhost"
decl_stmt|;
specifier|private
specifier|static
name|String
name|PRINCIPAL
decl_stmt|;
name|String
name|krbKeytab
decl_stmt|;
name|String
name|krbPrincipal
decl_stmt|;
name|UserGroupInformation
name|ugi
decl_stmt|;
name|Configuration
name|clientConf
decl_stmt|;
name|Configuration
name|serverConf
decl_stmt|;
specifier|abstract
name|Class
argument_list|<
name|?
extends|extends
name|RpcClient
argument_list|>
name|getRpcClientClass
parameter_list|()
function_decl|;
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|exception
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Properties
name|conf
init|=
name|MiniKdc
operator|.
name|createConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|put
argument_list|(
name|MiniKdc
operator|.
name|DEBUG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|KDC
operator|=
operator|new
name|MiniKdc
argument_list|(
name|conf
argument_list|,
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"kdc"
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|KDC
operator|.
name|start
argument_list|()
expr_stmt|;
name|PRINCIPAL
operator|=
literal|"hbase/"
operator|+
name|HOST
expr_stmt|;
name|KDC
operator|.
name|createPrincipal
argument_list|(
name|KEYTAB_FILE
argument_list|,
name|PRINCIPAL
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setKeytabFileForTesting
argument_list|(
name|KEYTAB_FILE
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setPrincipalForTesting
argument_list|(
name|PRINCIPAL
operator|+
literal|"@"
operator|+
name|KDC
operator|.
name|getRealm
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|KDC
operator|!=
literal|null
condition|)
block|{
name|KDC
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUpTest
parameter_list|()
throws|throws
name|Exception
block|{
name|krbKeytab
operator|=
name|getKeytabFileForTesting
argument_list|()
expr_stmt|;
name|krbPrincipal
operator|=
name|getPrincipalForTesting
argument_list|()
expr_stmt|;
name|ugi
operator|=
name|loginKerberosPrincipal
argument_list|(
name|krbKeytab
argument_list|,
name|krbPrincipal
argument_list|)
expr_stmt|;
name|clientConf
operator|=
name|getSecuredConfiguration
argument_list|()
expr_stmt|;
name|clientConf
operator|.
name|set
argument_list|(
name|RpcClientFactory
operator|.
name|CUSTOM_RPC_CLIENT_IMPL_CONF_KEY
argument_list|,
name|getRpcClientClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|serverConf
operator|=
name|getSecuredConfiguration
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRpcCallWithEnabledKerberosSaslAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|UserGroupInformation
name|ugi2
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
comment|// check that the login user is okay:
name|assertSame
argument_list|(
name|ugi
argument_list|,
name|ugi2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|AuthenticationMethod
operator|.
name|KERBEROS
argument_list|,
name|ugi
operator|.
name|getAuthenticationMethod
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|krbPrincipal
argument_list|,
name|ugi
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
name|callRpcService
argument_list|(
name|User
operator|.
name|create
argument_list|(
name|ugi2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRpcFallbackToSimpleAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|clientUsername
init|=
literal|"testuser"
decl_stmt|;
name|UserGroupInformation
name|clientUgi
init|=
name|UserGroupInformation
operator|.
name|createUserForTesting
argument_list|(
name|clientUsername
argument_list|,
operator|new
name|String
index|[]
block|{
name|clientUsername
block|}
argument_list|)
decl_stmt|;
comment|// check that the client user is insecure
name|assertNotSame
argument_list|(
name|ugi
argument_list|,
name|clientUgi
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|AuthenticationMethod
operator|.
name|SIMPLE
argument_list|,
name|clientUgi
operator|.
name|getAuthenticationMethod
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clientUsername
argument_list|,
name|clientUgi
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
name|clientConf
operator|.
name|set
argument_list|(
name|User
operator|.
name|HBASE_SECURITY_CONF_KEY
argument_list|,
literal|"simple"
argument_list|)
expr_stmt|;
name|serverConf
operator|.
name|setBoolean
argument_list|(
name|RpcServer
operator|.
name|FALLBACK_TO_INSECURE_CLIENT_AUTH
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|callRpcService
argument_list|(
name|User
operator|.
name|create
argument_list|(
name|clientUgi
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|setRpcProtection
parameter_list|(
name|String
name|clientProtection
parameter_list|,
name|String
name|serverProtection
parameter_list|)
block|{
name|clientConf
operator|.
name|set
argument_list|(
literal|"hbase.rpc.protection"
argument_list|,
name|clientProtection
argument_list|)
expr_stmt|;
name|serverConf
operator|.
name|set
argument_list|(
literal|"hbase.rpc.protection"
argument_list|,
name|serverProtection
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test various combinations of Server and Client qops.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSaslWithCommonQop
parameter_list|()
throws|throws
name|Exception
block|{
name|setRpcProtection
argument_list|(
literal|"privacy,authentication"
argument_list|,
literal|"authentication"
argument_list|)
expr_stmt|;
name|callRpcService
argument_list|(
name|User
operator|.
name|create
argument_list|(
name|ugi
argument_list|)
argument_list|)
expr_stmt|;
name|setRpcProtection
argument_list|(
literal|"authentication"
argument_list|,
literal|"privacy,authentication"
argument_list|)
expr_stmt|;
name|callRpcService
argument_list|(
name|User
operator|.
name|create
argument_list|(
name|ugi
argument_list|)
argument_list|)
expr_stmt|;
name|setRpcProtection
argument_list|(
literal|"integrity,authentication"
argument_list|,
literal|"privacy,authentication"
argument_list|)
expr_stmt|;
name|callRpcService
argument_list|(
name|User
operator|.
name|create
argument_list|(
name|ugi
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSaslNoCommonQop
parameter_list|()
throws|throws
name|Exception
block|{
name|exception
operator|.
name|expect
argument_list|(
name|SaslException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"No common protection layer between client and server"
argument_list|)
expr_stmt|;
name|setRpcProtection
argument_list|(
literal|"integrity"
argument_list|,
literal|"privacy"
argument_list|)
expr_stmt|;
name|callRpcService
argument_list|(
name|User
operator|.
name|create
argument_list|(
name|ugi
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|UserGroupInformation
name|loginKerberosPrincipal
parameter_list|(
name|String
name|krbKeytab
parameter_list|,
name|String
name|krbPrincipal
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|cnf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|cnf
operator|.
name|set
argument_list|(
name|CommonConfigurationKeys
operator|.
name|HADOOP_SECURITY_AUTHENTICATION
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|UserGroupInformation
operator|.
name|setConfiguration
argument_list|(
name|cnf
argument_list|)
expr_stmt|;
name|UserGroupInformation
operator|.
name|loginUserFromKeytab
argument_list|(
name|krbPrincipal
argument_list|,
name|krbKeytab
argument_list|)
expr_stmt|;
return|return
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
return|;
block|}
comment|/**    * Sets up a RPC Server and a Client. Does a RPC checks the result. If an exception is thrown    * from the stub, this function will throw root cause of that exception.    */
specifier|private
name|void
name|callRpcService
parameter_list|(
name|User
name|clientUser
parameter_list|)
throws|throws
name|Exception
block|{
name|SecurityInfo
name|securityInfoMock
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|SecurityInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|securityInfoMock
operator|.
name|getServerPrincipal
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HBaseKerberosUtils
operator|.
name|KRB_PRINCIPAL
argument_list|)
expr_stmt|;
name|SecurityInfo
operator|.
name|addInfo
argument_list|(
literal|"TestProtobufRpcProto"
argument_list|,
name|securityInfoMock
argument_list|)
expr_stmt|;
name|InetSocketAddress
name|isa
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|HOST
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|RpcServerInterface
name|rpcServer
init|=
operator|new
name|RpcServer
argument_list|(
literal|null
argument_list|,
literal|"AbstractTestSecureIPC"
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
name|isa
argument_list|,
name|serverConf
argument_list|,
operator|new
name|FifoRpcScheduler
argument_list|(
name|serverConf
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|rpcServer
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
init|(
name|RpcClient
name|rpcClient
init|=
name|RpcClientFactory
operator|.
name|createClient
argument_list|(
name|clientConf
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
operator|.
name|toString
argument_list|()
argument_list|)
init|)
block|{
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
name|BlockingRpcChannel
name|channel
init|=
name|rpcClient
operator|.
name|createBlockingRpcChannel
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|address
operator|.
name|getHostName
argument_list|()
argument_list|,
name|address
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
name|clientUser
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
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|TestThread
name|th1
init|=
operator|new
name|TestThread
argument_list|(
name|stub
argument_list|,
name|results
argument_list|)
decl_stmt|;
specifier|final
name|Throwable
name|exception
index|[]
init|=
operator|new
name|Throwable
index|[
literal|1
index|]
decl_stmt|;
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Throwable
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|UncaughtExceptionHandler
name|exceptionHandler
init|=
operator|new
name|Thread
operator|.
name|UncaughtExceptionHandler
argument_list|()
block|{
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|th
parameter_list|,
name|Throwable
name|ex
parameter_list|)
block|{
name|exception
index|[
literal|0
index|]
operator|=
name|ex
expr_stmt|;
block|}
block|}
decl_stmt|;
name|th1
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|exceptionHandler
argument_list|)
expr_stmt|;
name|th1
operator|.
name|start
argument_list|()
expr_stmt|;
name|th1
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|exception
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
comment|// throw root cause.
while|while
condition|(
name|exception
index|[
literal|0
index|]
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|exception
index|[
literal|0
index|]
operator|=
name|exception
index|[
literal|0
index|]
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
throw|throw
operator|(
name|Exception
operator|)
name|exception
index|[
literal|0
index|]
throw|;
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
specifier|public
specifier|static
class|class
name|TestThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|results
decl_stmt|;
specifier|public
name|TestThread
parameter_list|(
name|TestRpcServiceProtos
operator|.
name|TestProtobufRpcProto
operator|.
name|BlockingInterface
name|stub
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|results
parameter_list|)
block|{
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
name|this
operator|.
name|results
operator|=
name|results
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|String
name|result
decl_stmt|;
try|try
block|{
name|result
operator|=
name|stub
operator|.
name|echo
argument_list|(
literal|null
argument_list|,
name|TestProtos
operator|.
name|EchoRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMessage
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
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
if|if
condition|(
name|results
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|results
init|)
block|{
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

