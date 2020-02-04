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
name|http
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|HttpsURLConnection
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
name|FileUtil
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
name|HBaseCommonTestingUtility
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
name|http
operator|.
name|ssl
operator|.
name|KeyStoreTestUtil
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
name|io
operator|.
name|IOUtils
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
name|security
operator|.
name|ssl
operator|.
name|SSLFactory
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
comment|/**  * This testcase issues SSL certificates configures the HttpServer to serve  * HTTPS using the created certficates and calls an echo servlet using the  * corresponding HTTPS URL.  */
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSSLHttpServer
extends|extends
name|HttpServerFunctionalTest
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
name|TestSSLHttpServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BASEDIR
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.build.dir"
argument_list|,
literal|"target/test-dir"
argument_list|)
operator|+
literal|"/"
operator|+
name|TestSSLHttpServer
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
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
name|TestSSLHttpServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|serverConf
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
name|File
name|keystoresDir
decl_stmt|;
specifier|private
specifier|static
name|String
name|sslConfDir
decl_stmt|;
specifier|private
specifier|static
name|SSLFactory
name|clientSslFactory
decl_stmt|;
specifier|private
specifier|static
name|HBaseCommonTestingUtility
name|HTU
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
expr_stmt|;
name|serverConf
operator|=
name|HTU
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|serverConf
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
name|keystoresDir
operator|=
operator|new
name|File
argument_list|(
name|HTU
operator|.
name|getDataTestDir
argument_list|(
literal|"keystore"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|keystoresDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|sslConfDir
operator|=
name|KeyStoreTestUtil
operator|.
name|getClasspathDir
argument_list|(
name|TestSSLHttpServer
operator|.
name|class
argument_list|)
expr_stmt|;
name|KeyStoreTestUtil
operator|.
name|setupSSLConfig
argument_list|(
name|keystoresDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|sslConfDir
argument_list|,
name|serverConf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Configuration
name|clientConf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|clientConf
operator|.
name|addResource
argument_list|(
name|serverConf
operator|.
name|get
argument_list|(
name|SSLFactory
operator|.
name|SSL_CLIENT_CONF_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|serverConf
operator|.
name|addResource
argument_list|(
name|serverConf
operator|.
name|get
argument_list|(
name|SSLFactory
operator|.
name|SSL_SERVER_CONF_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|clientConf
operator|.
name|set
argument_list|(
name|SSLFactory
operator|.
name|SSL_CLIENT_CONF_KEY
argument_list|,
name|serverConf
operator|.
name|get
argument_list|(
name|SSLFactory
operator|.
name|SSL_CLIENT_CONF_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|clientSslFactory
operator|=
operator|new
name|SSLFactory
argument_list|(
name|SSLFactory
operator|.
name|Mode
operator|.
name|CLIENT
argument_list|,
name|clientConf
argument_list|)
expr_stmt|;
name|clientSslFactory
operator|.
name|init
argument_list|()
expr_stmt|;
name|server
operator|=
operator|new
name|HttpServer
operator|.
name|Builder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addEndpoint
argument_list|(
operator|new
name|URI
argument_list|(
literal|"https://localhost"
argument_list|)
argument_list|)
operator|.
name|setConf
argument_list|(
name|serverConf
argument_list|)
operator|.
name|keyPassword
argument_list|(
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|serverConf
argument_list|,
literal|"ssl.server.keystore.keypassword"
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|keyStore
argument_list|(
name|serverConf
operator|.
name|get
argument_list|(
literal|"ssl.server.keystore.location"
argument_list|)
argument_list|,
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|serverConf
argument_list|,
literal|"ssl.server.keystore.password"
argument_list|,
literal|null
argument_list|)
argument_list|,
name|clientConf
operator|.
name|get
argument_list|(
literal|"ssl.server.keystore.type"
argument_list|,
literal|"jks"
argument_list|)
argument_list|)
operator|.
name|trustStore
argument_list|(
name|serverConf
operator|.
name|get
argument_list|(
literal|"ssl.server.truststore.location"
argument_list|)
argument_list|,
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|serverConf
argument_list|,
literal|"ssl.server.truststore.password"
argument_list|,
literal|null
argument_list|)
argument_list|,
name|serverConf
operator|.
name|get
argument_list|(
literal|"ssl.server.truststore.type"
argument_list|,
literal|"jks"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|server
operator|.
name|addUnprivilegedServlet
argument_list|(
literal|"echo"
argument_list|,
literal|"/echo"
argument_list|,
name|TestHttpServer
operator|.
name|EchoServlet
operator|.
name|class
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|baseUrl
operator|=
operator|new
name|URL
argument_list|(
literal|"https://"
operator|+
name|NetUtils
operator|.
name|getHostPortString
argument_list|(
name|server
operator|.
name|getConnectorAddress
argument_list|(
literal|0
argument_list|)
argument_list|)
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
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
operator|new
name|File
argument_list|(
name|HTU
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|KeyStoreTestUtil
operator|.
name|cleanupSSLConfig
argument_list|(
name|serverConf
argument_list|)
expr_stmt|;
name|clientSslFactory
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEcho
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|"a:b\nc:d\n"
argument_list|,
name|readOut
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/echo?a=b&c=d"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a:b\nc&lt;:d\ne:&gt;\n"
argument_list|,
name|readOut
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/echo?a=b&c<=d&e=>"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|readOut
parameter_list|(
name|URL
name|url
parameter_list|)
throws|throws
name|Exception
block|{
name|HttpsURLConnection
name|conn
init|=
operator|(
name|HttpsURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|conn
operator|.
name|setSSLSocketFactory
argument_list|(
name|clientSslFactory
operator|.
name|createSSLSocketFactory
argument_list|()
argument_list|)
expr_stmt|;
name|InputStream
name|in
init|=
name|conn
operator|.
name|getInputStream
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|IOUtils
operator|.
name|copyBytes
argument_list|(
name|in
argument_list|,
name|out
argument_list|,
literal|1024
argument_list|)
expr_stmt|;
return|return
name|out
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

