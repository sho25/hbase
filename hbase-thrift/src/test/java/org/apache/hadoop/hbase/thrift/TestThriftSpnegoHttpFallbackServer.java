begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|thrift
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
name|thrift
operator|.
name|Constants
operator|.
name|THRIFT_SUPPORT_PROXYUSER_KEY
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
name|assertFalse
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
name|nio
operator|.
name|file
operator|.
name|Paths
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Principal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|Subject
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|kerberos
operator|.
name|KerberosTicket
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
name|HBaseClassTestRule
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
name|security
operator|.
name|HBaseKerberosUtils
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
name|ClientTests
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
name|LargeTests
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
name|thrift
operator|.
name|generated
operator|.
name|Hbase
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
name|authentication
operator|.
name|util
operator|.
name|KerberosName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHeaders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|AuthSchemeProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|AuthScope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|KerberosCredentials
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|config
operator|.
name|AuthSchemes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|config
operator|.
name|Lookup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|config
operator|.
name|RegistryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|auth
operator|.
name|SPNegoSchemeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|BasicCredentialsProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|CloseableHttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|HttpClients
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kerby
operator|.
name|kerberos
operator|.
name|kerb
operator|.
name|client
operator|.
name|JaasKrbUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kerby
operator|.
name|kerberos
operator|.
name|kerb
operator|.
name|server
operator|.
name|SimpleKdcServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|THttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSCredential
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|Oid
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
name|ClassRule
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Start the HBase Thrift HTTP server on a random port through the command-line  * interface and talk to it from client side with SPNEGO security enabled.  *  * Supplemental test to TestThriftSpnegoHttpServer which falls back to the original  * Kerberos principal and keytab configuration properties, not the separate  * SPNEGO-specific properties.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestThriftSpnegoHttpFallbackServer
extends|extends
name|TestThriftHttpServer
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestThriftSpnegoHttpFallbackServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestThriftSpnegoHttpFallbackServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SimpleKdcServer
name|kdc
decl_stmt|;
specifier|private
specifier|static
name|File
name|serverKeytab
decl_stmt|;
specifier|private
specifier|static
name|File
name|clientKeytab
decl_stmt|;
specifier|private
specifier|static
name|String
name|clientPrincipal
decl_stmt|;
specifier|private
specifier|static
name|String
name|serverPrincipal
decl_stmt|;
specifier|private
specifier|static
name|String
name|spnegoServerPrincipal
decl_stmt|;
specifier|private
specifier|static
name|SimpleKdcServer
name|buildMiniKdc
parameter_list|()
throws|throws
name|Exception
block|{
name|SimpleKdcServer
name|kdc
init|=
operator|new
name|SimpleKdcServer
argument_list|()
decl_stmt|;
name|File
name|kdcDir
init|=
name|Paths
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getRandomDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toFile
argument_list|()
decl_stmt|;
name|kdcDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|kdc
operator|.
name|setWorkDir
argument_list|(
name|kdcDir
argument_list|)
expr_stmt|;
name|kdc
operator|.
name|setKdcHost
argument_list|(
name|HConstants
operator|.
name|LOCALHOST
argument_list|)
expr_stmt|;
name|int
name|kdcPort
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|kdc
operator|.
name|setAllowTcp
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|kdc
operator|.
name|setAllowUdp
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|kdc
operator|.
name|setKdcTcpPort
argument_list|(
name|kdcPort
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting KDC server at "
operator|+
name|HConstants
operator|.
name|LOCALHOST
operator|+
literal|":"
operator|+
name|kdcPort
argument_list|)
expr_stmt|;
name|kdc
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|kdc
return|;
block|}
specifier|private
specifier|static
name|void
name|addSecurityConfigurations
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|KerberosName
operator|.
name|setRules
argument_list|(
literal|"DEFAULT"
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setKeytabFileForTesting
argument_list|(
name|serverKeytab
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|THRIFT_SUPPORT_PROXYUSER_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|Constants
operator|.
name|USE_HTTP_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|THRIFT_KERBEROS_PRINCIPAL_KEY
argument_list|,
name|serverPrincipal
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|THRIFT_KEYTAB_FILE_KEY
argument_list|,
name|serverKeytab
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseKerberosUtils
operator|.
name|setSecuredConfiguration
argument_list|(
name|conf
argument_list|,
name|spnegoServerPrincipal
argument_list|,
name|spnegoServerPrincipal
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.proxyuser.HTTP.hosts"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.proxyuser.HTTP.groups"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|THRIFT_KERBEROS_PRINCIPAL_KEY
argument_list|,
name|spnegoServerPrincipal
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|kdc
operator|=
name|buildMiniKdc
argument_list|()
expr_stmt|;
name|kdc
operator|.
name|start
argument_list|()
expr_stmt|;
name|File
name|keytabDir
init|=
name|Paths
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getRandomDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toFile
argument_list|()
decl_stmt|;
name|keytabDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|clientPrincipal
operator|=
literal|"client@"
operator|+
name|kdc
operator|.
name|getKdcConfig
argument_list|()
operator|.
name|getKdcRealm
argument_list|()
expr_stmt|;
name|clientKeytab
operator|=
operator|new
name|File
argument_list|(
name|keytabDir
argument_list|,
name|clientPrincipal
operator|+
literal|".keytab"
argument_list|)
expr_stmt|;
name|kdc
operator|.
name|createAndExportPrincipals
argument_list|(
name|clientKeytab
argument_list|,
name|clientPrincipal
argument_list|)
expr_stmt|;
name|serverPrincipal
operator|=
literal|"hbase/"
operator|+
name|HConstants
operator|.
name|LOCALHOST
operator|+
literal|"@"
operator|+
name|kdc
operator|.
name|getKdcConfig
argument_list|()
operator|.
name|getKdcRealm
argument_list|()
expr_stmt|;
name|serverKeytab
operator|=
operator|new
name|File
argument_list|(
name|keytabDir
argument_list|,
name|serverPrincipal
operator|.
name|replace
argument_list|(
literal|'/'
argument_list|,
literal|'_'
argument_list|)
operator|+
literal|".keytab"
argument_list|)
expr_stmt|;
name|spnegoServerPrincipal
operator|=
literal|"HTTP/"
operator|+
name|HConstants
operator|.
name|LOCALHOST
operator|+
literal|"@"
operator|+
name|kdc
operator|.
name|getKdcConfig
argument_list|()
operator|.
name|getKdcRealm
argument_list|()
expr_stmt|;
comment|// Add SPNEGO principal to server keytab
name|kdc
operator|.
name|createAndExportPrincipals
argument_list|(
name|serverKeytab
argument_list|,
name|serverPrincipal
argument_list|,
name|spnegoServerPrincipal
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|Constants
operator|.
name|USE_HTTP_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|addSecurityConfigurations
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|TestThriftHttpServer
operator|.
name|setUpBeforeClass
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
name|TestThriftHttpServer
operator|.
name|tearDownAfterClass
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
literal|null
operator|!=
name|kdc
condition|)
block|{
name|kdc
operator|.
name|stop
argument_list|()
expr_stmt|;
name|kdc
operator|=
literal|null
expr_stmt|;
block|}
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
literal|"Failed to stop mini KDC"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|talkToThriftServer
parameter_list|(
name|String
name|url
parameter_list|,
name|int
name|customHeaderSize
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Close httpClient and THttpClient automatically on any failures
try|try
init|(
name|CloseableHttpClient
name|httpClient
init|=
name|createHttpClient
argument_list|()
init|;
name|THttpClient
name|tHttpClient
operator|=
operator|new
name|THttpClient
argument_list|(
name|url
argument_list|,
name|httpClient
argument_list|)
init|)
block|{
name|tHttpClient
operator|.
name|open
argument_list|()
expr_stmt|;
if|if
condition|(
name|customHeaderSize
operator|>
literal|0
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
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
name|customHeaderSize
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
name|tHttpClient
operator|.
name|setCustomHeader
argument_list|(
name|HttpHeaders
operator|.
name|USER_AGENT
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TProtocol
name|prot
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|tHttpClient
argument_list|)
decl_stmt|;
name|Hbase
operator|.
name|Client
name|client
init|=
operator|new
name|Hbase
operator|.
name|Client
argument_list|(
name|prot
argument_list|)
decl_stmt|;
name|TestThriftServer
operator|.
name|createTestTables
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|TestThriftServer
operator|.
name|checkTableList
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|TestThriftServer
operator|.
name|dropTestTables
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|CloseableHttpClient
name|createHttpClient
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Subject
name|clientSubject
init|=
name|JaasKrbUtil
operator|.
name|loginUsingKeytab
argument_list|(
name|clientPrincipal
argument_list|,
name|clientKeytab
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Principal
argument_list|>
name|clientPrincipals
init|=
name|clientSubject
operator|.
name|getPrincipals
argument_list|()
decl_stmt|;
comment|// Make sure the subject has a principal
name|assertFalse
argument_list|(
literal|"Found no client principals in the clientSubject."
argument_list|,
name|clientPrincipals
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Get a TGT for the subject (might have many, different encryption types). The first should
comment|// be the default encryption type.
name|Set
argument_list|<
name|KerberosTicket
argument_list|>
name|privateCredentials
init|=
name|clientSubject
operator|.
name|getPrivateCredentials
argument_list|(
name|KerberosTicket
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Found no private credentials in the clientSubject."
argument_list|,
name|privateCredentials
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|KerberosTicket
name|tgt
init|=
name|privateCredentials
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"No kerberos ticket found."
argument_list|,
name|tgt
argument_list|)
expr_stmt|;
comment|// The name of the principal
specifier|final
name|String
name|clientPrincipalName
init|=
name|clientPrincipals
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|Subject
operator|.
name|doAs
argument_list|(
name|clientSubject
argument_list|,
call|(
name|PrivilegedExceptionAction
argument_list|<
name|CloseableHttpClient
argument_list|>
call|)
argument_list|()
operator|->
block|{
comment|// Logs in with Kerberos via GSS
name|GSSManager
name|gssManager
operator|=
name|GSSManager
operator|.
name|getInstance
argument_list|()
block|;
comment|// jGSS Kerberos login constant
name|Oid
name|oid
operator|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2"
argument_list|)
block|;
name|GSSName
name|gssClient
operator|=
name|gssManager
operator|.
name|createName
argument_list|(
name|clientPrincipalName
argument_list|,
name|GSSName
operator|.
name|NT_USER_NAME
argument_list|)
block|;
name|GSSCredential
name|credential
operator|=
name|gssManager
operator|.
name|createCredential
argument_list|(
name|gssClient
argument_list|,
name|GSSCredential
operator|.
name|DEFAULT_LIFETIME
argument_list|,
name|oid
argument_list|,
name|GSSCredential
operator|.
name|INITIATE_ONLY
argument_list|)
block|;
name|Lookup
argument_list|<
name|AuthSchemeProvider
argument_list|>
name|authRegistry
operator|=
name|RegistryBuilder
operator|.
expr|<
name|AuthSchemeProvider
operator|>
name|create
argument_list|()
operator|.
name|register
argument_list|(
name|AuthSchemes
operator|.
name|SPNEGO
argument_list|,
operator|new
name|SPNegoSchemeFactory
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
block|;
name|BasicCredentialsProvider
name|credentialsProvider
operator|=
operator|new
name|BasicCredentialsProvider
argument_list|()
block|;
name|credentialsProvider
operator|.
name|setCredentials
argument_list|(
name|AuthScope
operator|.
name|ANY
argument_list|,
operator|new
name|KerberosCredentials
argument_list|(
name|credential
argument_list|)
argument_list|)
block|;
return|return
name|HttpClients
operator|.
name|custom
argument_list|()
operator|.
name|setDefaultAuthSchemeRegistry
argument_list|(
name|authRegistry
argument_list|)
operator|.
name|setDefaultCredentialsProvider
argument_list|(
name|credentialsProvider
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|)
function|;
block|}
end_class

unit|}
end_unit

