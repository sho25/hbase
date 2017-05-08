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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
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
name|lang
operator|.
name|RandomStringUtils
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
name|ipc
operator|.
name|BlockingRpcClient
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
name|NettyRpcClient
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
name|NettyRpcServer
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
name|RpcServerFactory
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
name|SimpleRpcServer
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
name|SecurityTests
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
name|rules
operator|.
name|ExpectedException
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
name|mockito
operator|.
name|Mockito
import|;
end_import

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
name|SecurityTests
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
name|TestSecureIPC
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
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: rpcClientImpl={0}, rpcServerImpl={1}"
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
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rpcClientImpls
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|BlockingRpcClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|NettyRpcClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rpcServerImpls
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|SimpleRpcServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|NettyRpcServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|rpcClientImpl
range|:
name|rpcClientImpls
control|)
block|{
for|for
control|(
name|String
name|rpcServerImpl
range|:
name|rpcServerImpls
control|)
block|{
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|rpcClientImpl
block|,
name|rpcServerImpl
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|params
return|;
block|}
annotation|@
name|Parameter
argument_list|(
literal|0
argument_list|)
specifier|public
name|String
name|rpcClientImpl
decl_stmt|;
annotation|@
name|Parameter
argument_list|(
literal|1
argument_list|)
specifier|public
name|String
name|rpcServerImpl
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
name|KDC
operator|=
name|TEST_UTIL
operator|.
name|setupMiniKdc
argument_list|(
name|KEYTAB_FILE
argument_list|)
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
name|rpcClientImpl
argument_list|)
expr_stmt|;
name|serverConf
operator|=
name|getSecuredConfiguration
argument_list|()
expr_stmt|;
name|serverConf
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
name|setRpcProtection
argument_list|(
literal|"integrity,authentication"
argument_list|,
literal|"integrity,authentication"
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
literal|"privacy,authentication"
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
comment|/**    * Test sasl encryption with Crypto AES.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSaslWithCryptoAES
parameter_list|()
throws|throws
name|Exception
block|{
name|setRpcProtection
argument_list|(
literal|"privacy"
argument_list|,
literal|"privacy"
argument_list|)
expr_stmt|;
name|setCryptoAES
argument_list|(
literal|"true"
argument_list|,
literal|"true"
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
comment|/**    * Test various combinations of Server and Client configuration for Crypto AES.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testDifferentConfWithCryptoAES
parameter_list|()
throws|throws
name|Exception
block|{
name|setRpcProtection
argument_list|(
literal|"privacy"
argument_list|,
literal|"privacy"
argument_list|)
expr_stmt|;
name|setCryptoAES
argument_list|(
literal|"false"
argument_list|,
literal|"true"
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
name|setCryptoAES
argument_list|(
literal|"true"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
try|try
block|{
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
name|fail
argument_list|(
literal|"The exception should be thrown out for the rpc timeout."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore the expected exception
block|}
block|}
name|void
name|setCryptoAES
parameter_list|(
name|String
name|clientCryptoAES
parameter_list|,
name|String
name|serverCryptoAES
parameter_list|)
block|{
name|clientConf
operator|.
name|set
argument_list|(
literal|"hbase.rpc.crypto.encryption.aes.enabled"
argument_list|,
name|clientCryptoAES
argument_list|)
expr_stmt|;
name|serverConf
operator|.
name|set
argument_list|(
literal|"hbase.rpc.crypto.encryption.aes.enabled"
argument_list|,
name|serverCryptoAES
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
comment|/**    * Sets up a RPC Server and a Client. Does a RPC checks the result. If an exception is thrown from    * the stub, this function will throw root cause of that exception.    */
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
name|RpcServerFactory
operator|.
name|createRpcServer
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
operator|(
name|BlockingService
operator|)
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
name|BlockingInterface
name|stub
init|=
name|newBlockingStub
argument_list|(
name|rpcClient
argument_list|,
name|rpcServer
operator|.
name|getListenerAddress
argument_list|()
argument_list|,
name|clientUser
argument_list|)
decl_stmt|;
name|TestThread
name|th1
init|=
operator|new
name|TestThread
argument_list|(
name|stub
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
name|BlockingInterface
name|stub
decl_stmt|;
specifier|public
name|TestThread
parameter_list|(
name|BlockingInterface
name|stub
parameter_list|)
block|{
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|int
index|[]
name|messageSize
init|=
operator|new
name|int
index|[]
block|{
literal|100
block|,
literal|1000
block|,
literal|10000
block|}
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
name|messageSize
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|input
init|=
name|RandomStringUtils
operator|.
name|random
argument_list|(
name|messageSize
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|String
name|result
init|=
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
name|input
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|input
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
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
block|}
block|}
end_class

end_unit

