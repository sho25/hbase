begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|http
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|http
operator|.
name|TestHttpServer
operator|.
name|EchoServlet
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
name|http
operator|.
name|resource
operator|.
name|JerseyResource
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
name|MiscTests
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
name|HttpHost
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
name|HttpResponse
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
name|HttpClient
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
name|client
operator|.
name|methods
operator|.
name|HttpGet
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
name|protocol
operator|.
name|HttpClientContext
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
name|entity
operator|.
name|ByteArrayEntity
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
name|entity
operator|.
name|ContentType
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
name|HttpClients
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
name|util
operator|.
name|EntityUtils
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
name|KrbException
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

begin_comment
comment|/**  * Test class for SPNEGO authentication on the HttpServer. Uses Kerby's MiniKDC and Apache  * HttpComponents to verify that a simple Servlet is reachable via SPNEGO and unreachable w/o.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestSpnegoHttpServer
extends|extends
name|HttpServerFunctionalTest
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
name|TestSpnegoHttpServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KDC_SERVER_HOST
init|=
literal|"localhost"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLIENT_PRINCIPAL
init|=
literal|"client"
decl_stmt|;
specifier|private
specifier|static
name|HttpServer
name|server
decl_stmt|;
specifier|private
specifier|static
name|URL
name|baseUrl
decl_stmt|;
specifier|private
specifier|static
name|SimpleKdcServer
name|kdc
decl_stmt|;
specifier|private
specifier|static
name|File
name|infoServerKeytab
decl_stmt|;
specifier|private
specifier|static
name|File
name|clientKeytab
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupServer
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|serverPrincipal
init|=
literal|"HTTP/"
operator|+
name|KDC_SERVER_HOST
decl_stmt|;
specifier|final
name|File
name|target
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
argument_list|,
literal|"target"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|target
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
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
operator|new
name|File
argument_list|(
name|target
argument_list|,
name|TestSpnegoHttpServer
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"_keytabs"
argument_list|)
decl_stmt|;
if|if
condition|(
name|keytabDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|deleteRecursively
argument_list|(
name|keytabDir
argument_list|)
expr_stmt|;
block|}
name|keytabDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|infoServerKeytab
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
name|clientKeytab
operator|=
operator|new
name|File
argument_list|(
name|keytabDir
argument_list|,
name|CLIENT_PRINCIPAL
operator|+
literal|".keytab"
argument_list|)
expr_stmt|;
name|setupUser
argument_list|(
name|kdc
argument_list|,
name|clientKeytab
argument_list|,
name|CLIENT_PRINCIPAL
argument_list|)
expr_stmt|;
name|setupUser
argument_list|(
name|kdc
argument_list|,
name|infoServerKeytab
argument_list|,
name|serverPrincipal
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|buildSpnegoConfiguration
argument_list|(
name|serverPrincipal
argument_list|,
name|infoServerKeytab
argument_list|)
decl_stmt|;
name|server
operator|=
name|createTestServerWithSecurity
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|server
operator|.
name|addServlet
argument_list|(
literal|"echo"
argument_list|,
literal|"/echo"
argument_list|,
name|EchoServlet
operator|.
name|class
argument_list|)
expr_stmt|;
name|server
operator|.
name|addJerseyResourcePackage
argument_list|(
name|JerseyResource
operator|.
name|class
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|"/jersey/*"
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|baseUrl
operator|=
name|getServerURL
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HTTP server started: "
operator|+
name|baseUrl
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopServer
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
if|if
condition|(
literal|null
operator|!=
name|server
condition|)
block|{
name|server
operator|.
name|stop
argument_list|()
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
literal|"Failed to stop info server"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
specifier|private
specifier|static
name|void
name|setupUser
parameter_list|(
name|SimpleKdcServer
name|kdc
parameter_list|,
name|File
name|keytab
parameter_list|,
name|String
name|principal
parameter_list|)
throws|throws
name|KrbException
block|{
name|kdc
operator|.
name|createPrincipal
argument_list|(
name|principal
argument_list|)
expr_stmt|;
name|kdc
operator|.
name|exportPrincipal
argument_list|(
name|principal
argument_list|,
name|keytab
argument_list|)
expr_stmt|;
block|}
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
specifier|final
name|File
name|target
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
argument_list|,
literal|"target"
argument_list|)
decl_stmt|;
name|File
name|kdcDir
init|=
operator|new
name|File
argument_list|(
name|target
argument_list|,
name|TestSpnegoHttpServer
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|kdcDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|deleteRecursively
argument_list|(
name|kdcDir
argument_list|)
expr_stmt|;
block|}
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
name|KDC_SERVER_HOST
argument_list|)
expr_stmt|;
name|int
name|kdcPort
init|=
name|getFreePort
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
name|KDC_SERVER_HOST
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
name|Configuration
name|buildSpnegoConfiguration
parameter_list|(
name|String
name|serverPrincipal
parameter_list|,
name|File
name|serverKeytab
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|KerberosName
operator|.
name|setRules
argument_list|(
literal|"DEFAULT"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HttpServer
operator|.
name|HTTP_MAX_THREADS
argument_list|,
name|TestHttpServer
operator|.
name|MAX_THREADS
argument_list|)
expr_stmt|;
comment|// Enable Kerberos (pre-req)
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.security.authentication"
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HttpServer
operator|.
name|HTTP_UI_AUTHENTICATION
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HttpServer
operator|.
name|HTTP_SPNEGO_AUTHENTICATION_PRINCIPAL_KEY
argument_list|,
name|serverPrincipal
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HttpServer
operator|.
name|HTTP_SPNEGO_AUTHENTICATION_KEYTAB_KEY
argument_list|,
name|serverKeytab
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUnauthorizedClientsDisallowed
parameter_list|()
throws|throws
name|IOException
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|getServerURL
argument_list|(
name|server
argument_list|)
argument_list|,
literal|"/echo?a=b"
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|conn
init|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_UNAUTHORIZED
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAllowedClient
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the subject for the client
specifier|final
name|Subject
name|clientSubject
init|=
name|JaasKrbUtil
operator|.
name|loginUsingKeytab
argument_list|(
name|CLIENT_PRINCIPAL
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
name|tgt
argument_list|)
expr_stmt|;
comment|// The name of the principal
specifier|final
name|String
name|principalName
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
comment|// Run this code, logged in as the subject (the client)
name|HttpResponse
name|resp
init|=
name|Subject
operator|.
name|doAs
argument_list|(
name|clientSubject
argument_list|,
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|HttpResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HttpResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Logs in with Kerberos via GSS
name|GSSManager
name|gssManager
init|=
name|GSSManager
operator|.
name|getInstance
argument_list|()
decl_stmt|;
comment|// jGSS Kerberos login constant
name|Oid
name|oid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2"
argument_list|)
decl_stmt|;
name|GSSName
name|gssClient
init|=
name|gssManager
operator|.
name|createName
argument_list|(
name|principalName
argument_list|,
name|GSSName
operator|.
name|NT_USER_NAME
argument_list|)
decl_stmt|;
name|GSSCredential
name|credential
init|=
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
decl_stmt|;
name|HttpClientContext
name|context
init|=
name|HttpClientContext
operator|.
name|create
argument_list|()
decl_stmt|;
name|Lookup
argument_list|<
name|AuthSchemeProvider
argument_list|>
name|authRegistry
init|=
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
decl_stmt|;
name|HttpClient
name|client
init|=
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
name|build
argument_list|()
decl_stmt|;
name|BasicCredentialsProvider
name|credentialsProvider
init|=
operator|new
name|BasicCredentialsProvider
argument_list|()
decl_stmt|;
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
expr_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|getServerURL
argument_list|(
name|server
argument_list|)
argument_list|,
literal|"/echo?a=b"
argument_list|)
decl_stmt|;
name|context
operator|.
name|setTargetHost
argument_list|(
operator|new
name|HttpHost
argument_list|(
name|url
operator|.
name|getHost
argument_list|()
argument_list|,
name|url
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|setCredentialsProvider
argument_list|(
name|credentialsProvider
argument_list|)
expr_stmt|;
name|context
operator|.
name|setAuthSchemeRegistry
argument_list|(
name|authRegistry
argument_list|)
expr_stmt|;
name|HttpGet
name|get
init|=
operator|new
name|HttpGet
argument_list|(
name|url
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|client
operator|.
name|execute
argument_list|(
name|get
argument_list|,
name|context
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_OK
argument_list|,
name|resp
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a:b"
argument_list|,
name|EntityUtils
operator|.
name|toString
argument_list|(
name|resp
operator|.
name|getEntity
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testMissingConfigurationThrowsException
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HttpServer
operator|.
name|HTTP_MAX_THREADS
argument_list|,
name|TestHttpServer
operator|.
name|MAX_THREADS
argument_list|)
expr_stmt|;
comment|// Enable Kerberos (pre-req)
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.security.authentication"
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
comment|// Intentionally skip keytab and principal
name|HttpServer
name|customServer
init|=
name|createTestServerWithSecurity
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|customServer
operator|.
name|addServlet
argument_list|(
literal|"echo"
argument_list|,
literal|"/echo"
argument_list|,
name|EchoServlet
operator|.
name|class
argument_list|)
expr_stmt|;
name|customServer
operator|.
name|addJerseyResourcePackage
argument_list|(
name|JerseyResource
operator|.
name|class
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|"/jersey/*"
argument_list|)
expr_stmt|;
name|customServer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

