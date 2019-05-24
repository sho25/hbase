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
name|InputStream
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
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
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
name|java
operator|.
name|net
operator|.
name|URLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|HttpServer
operator|.
name|Builder
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
name|authorize
operator|.
name|AccessControlList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
comment|/**  * This is a base class for functional tests of the {@link HttpServer}.  * The methods are static for other classes to import statically.  */
end_comment

begin_class
specifier|public
class|class
name|HttpServerFunctionalTest
extends|extends
name|Assert
block|{
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
name|HttpServerFunctionalTest
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** JVM property for the webapp test dir : {@value} */
specifier|public
specifier|static
specifier|final
name|String
name|TEST_BUILD_WEBAPPS
init|=
literal|"test.build.webapps"
decl_stmt|;
comment|/** expected location of the test.build.webapps dir: {@value} */
specifier|private
specifier|static
specifier|final
name|String
name|BUILD_WEBAPPS_DIR
init|=
literal|"src/main/resources/hbase-webapps"
decl_stmt|;
comment|/** name of the test webapp: {@value} */
specifier|private
specifier|static
specifier|final
name|String
name|TEST
init|=
literal|"test"
decl_stmt|;
comment|/**    * Create but do not start the test webapp server. The test webapp dir is    * prepared/checked in advance.    *    * @return the server instance    *    * @throws IOException if a problem occurs    * @throws AssertionError if a condition was not met    */
specifier|public
specifier|static
name|HttpServer
name|createTestServer
parameter_list|()
throws|throws
name|IOException
block|{
name|prepareTestWebapp
argument_list|()
expr_stmt|;
return|return
name|createServer
argument_list|(
name|TEST
argument_list|)
return|;
block|}
comment|/**    * Create but do not start the test webapp server. The test webapp dir is    * prepared/checked in advance.    * @param conf the server configuration to use    * @return the server instance    *    * @throws IOException if a problem occurs    * @throws AssertionError if a condition was not met    */
specifier|public
specifier|static
name|HttpServer
name|createTestServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|prepareTestWebapp
argument_list|()
expr_stmt|;
return|return
name|createServer
argument_list|(
name|TEST
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HttpServer
name|createTestServer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|AccessControlList
name|adminsAcl
parameter_list|)
throws|throws
name|IOException
block|{
name|prepareTestWebapp
argument_list|()
expr_stmt|;
return|return
name|createServer
argument_list|(
name|TEST
argument_list|,
name|conf
argument_list|,
name|adminsAcl
argument_list|)
return|;
block|}
comment|/**    * Create but do not start the test webapp server. The test webapp dir is    * prepared/checked in advance.    * @param conf the server configuration to use    * @return the server instance    *    * @throws IOException if a problem occurs    * @throws AssertionError if a condition was not met    */
specifier|public
specifier|static
name|HttpServer
name|createTestServer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|pathSpecs
parameter_list|)
throws|throws
name|IOException
block|{
name|prepareTestWebapp
argument_list|()
expr_stmt|;
return|return
name|createServer
argument_list|(
name|TEST
argument_list|,
name|conf
argument_list|,
name|pathSpecs
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HttpServer
name|createTestServerWithSecurity
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|prepareTestWebapp
argument_list|()
expr_stmt|;
return|return
name|localServerBuilder
argument_list|(
name|TEST
argument_list|)
operator|.
name|setFindPort
argument_list|(
literal|true
argument_list|)
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
operator|.
name|setSecurityEnabled
argument_list|(
literal|true
argument_list|)
comment|// InfoServer normally sets these for us
operator|.
name|setUsernameConfKey
argument_list|(
name|HttpServer
operator|.
name|HTTP_SPNEGO_AUTHENTICATION_PRINCIPAL_KEY
argument_list|)
operator|.
name|setKeytabConfKey
argument_list|(
name|HttpServer
operator|.
name|HTTP_SPNEGO_AUTHENTICATION_KEYTAB_KEY
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Prepare the test webapp by creating the directory from the test properties    * fail if the directory cannot be created.    * @throws AssertionError if a condition was not met    */
specifier|protected
specifier|static
name|void
name|prepareTestWebapp
parameter_list|()
block|{
name|String
name|webapps
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|TEST_BUILD_WEBAPPS
argument_list|,
name|BUILD_WEBAPPS_DIR
argument_list|)
decl_stmt|;
name|File
name|testWebappDir
init|=
operator|new
name|File
argument_list|(
name|webapps
operator|+
name|File
operator|.
name|separatorChar
operator|+
name|TEST
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|testWebappDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|fail
argument_list|(
literal|"Test webapp dir "
operator|+
name|testWebappDir
operator|.
name|getCanonicalPath
argument_list|()
operator|+
literal|" missing"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{     }
block|}
comment|/**    * Create an HttpServer instance on the given address for the given webapp    * @param host to bind    * @param port to bind    * @return the server    * @throws IOException if it could not be created    */
specifier|public
specifier|static
name|HttpServer
name|createServer
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|prepareTestWebapp
argument_list|()
expr_stmt|;
return|return
operator|new
name|HttpServer
operator|.
name|Builder
argument_list|()
operator|.
name|setName
argument_list|(
name|TEST
argument_list|)
operator|.
name|addEndpoint
argument_list|(
name|URI
operator|.
name|create
argument_list|(
literal|"http://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
argument_list|)
argument_list|)
operator|.
name|setFindPort
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Create an HttpServer instance for the given webapp    * @param webapp the webapp to work with    * @return the server    * @throws IOException if it could not be created    */
specifier|public
specifier|static
name|HttpServer
name|createServer
parameter_list|(
name|String
name|webapp
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|localServerBuilder
argument_list|(
name|webapp
argument_list|)
operator|.
name|setFindPort
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Create an HttpServer instance for the given webapp    * @param webapp the webapp to work with    * @param conf the configuration to use for the server    * @return the server    * @throws IOException if it could not be created    */
specifier|public
specifier|static
name|HttpServer
name|createServer
parameter_list|(
name|String
name|webapp
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|localServerBuilder
argument_list|(
name|webapp
argument_list|)
operator|.
name|setFindPort
argument_list|(
literal|true
argument_list|)
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|HttpServer
name|createServer
parameter_list|(
name|String
name|webapp
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|AccessControlList
name|adminsAcl
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|localServerBuilder
argument_list|(
name|webapp
argument_list|)
operator|.
name|setFindPort
argument_list|(
literal|true
argument_list|)
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
operator|.
name|setACL
argument_list|(
name|adminsAcl
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|Builder
name|localServerBuilder
parameter_list|(
name|String
name|webapp
parameter_list|)
block|{
return|return
operator|new
name|HttpServer
operator|.
name|Builder
argument_list|()
operator|.
name|setName
argument_list|(
name|webapp
argument_list|)
operator|.
name|addEndpoint
argument_list|(
name|URI
operator|.
name|create
argument_list|(
literal|"http://localhost:0"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Create an HttpServer instance for the given webapp    * @param webapp the webapp to work with    * @param conf the configuration to use for the server    * @param pathSpecs the paths specifications the server will service    * @return the server    * @throws IOException if it could not be created    */
specifier|public
specifier|static
name|HttpServer
name|createServer
parameter_list|(
name|String
name|webapp
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|pathSpecs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|localServerBuilder
argument_list|(
name|webapp
argument_list|)
operator|.
name|setFindPort
argument_list|(
literal|true
argument_list|)
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
operator|.
name|setPathSpec
argument_list|(
name|pathSpecs
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Create and start a server with the test webapp    *    * @return the newly started server    *    * @throws IOException on any failure    * @throws AssertionError if a condition was not met    */
specifier|public
specifier|static
name|HttpServer
name|createAndStartTestServer
parameter_list|()
throws|throws
name|IOException
block|{
name|HttpServer
name|server
init|=
name|createTestServer
argument_list|()
decl_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|server
return|;
block|}
comment|/**    * If the server is non null, stop it    * @param server to stop    * @throws Exception on any failure    */
specifier|public
specifier|static
name|void
name|stop
parameter_list|(
name|HttpServer
name|server
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Pass in a server, return a URL bound to localhost and its port    * @param server server    * @return a URL bonded to the base of the server    * @throws MalformedURLException if the URL cannot be created.    */
specifier|public
specifier|static
name|URL
name|getServerURL
parameter_list|(
name|HttpServer
name|server
parameter_list|)
throws|throws
name|MalformedURLException
block|{
name|assertNotNull
argument_list|(
literal|"No server"
argument_list|,
name|server
argument_list|)
expr_stmt|;
return|return
operator|new
name|URL
argument_list|(
literal|"http://"
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
return|;
block|}
comment|/**    * Read in the content from a URL    * @param url URL To read    * @return the text from the output    * @throws IOException if something went wrong    */
specifier|protected
specifier|static
name|String
name|readOutput
parameter_list|(
name|URL
name|url
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|out
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|InputStream
name|in
init|=
name|url
operator|.
name|openConnection
argument_list|()
operator|.
name|getInputStream
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
literal|64
operator|*
literal|1024
index|]
decl_stmt|;
name|int
name|len
init|=
name|in
operator|.
name|read
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|append
argument_list|(
operator|new
name|String
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
argument_list|)
expr_stmt|;
name|len
operator|=
name|in
operator|.
name|read
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
return|return
name|out
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Recursively deletes a {@link File}.    */
specifier|protected
specifier|static
name|void
name|deleteRecursively
parameter_list|(
name|File
name|d
parameter_list|)
block|{
if|if
condition|(
name|d
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|name
range|:
name|d
operator|.
name|list
argument_list|()
control|)
block|{
name|File
name|child
init|=
operator|new
name|File
argument_list|(
name|d
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|.
name|isFile
argument_list|()
condition|)
block|{
name|child
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|deleteRecursively
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|d
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|/**    * Picks a free port on the host by binding a Socket to '0'.    */
specifier|protected
specifier|static
name|int
name|getFreePort
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerSocket
name|s
init|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
decl_stmt|;
try|try
block|{
name|s
operator|.
name|setReuseAddress
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|s
operator|.
name|getLocalPort
argument_list|()
decl_stmt|;
return|return
name|port
return|;
block|}
finally|finally
block|{
if|if
condition|(
literal|null
operator|!=
name|s
condition|)
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * access a url, ignoring some IOException such as the page does not exist    */
specifier|public
specifier|static
name|void
name|access
parameter_list|(
name|String
name|urlstring
parameter_list|)
throws|throws
name|IOException
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|urlstring
argument_list|)
decl_stmt|;
name|URLConnection
name|connection
init|=
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|connection
operator|.
name|connect
argument_list|()
expr_stmt|;
try|try
init|(
name|BufferedReader
name|in
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|connection
operator|.
name|getInputStream
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
init|)
block|{
for|for
control|(
init|;
name|in
operator|.
name|readLine
argument_list|()
operator|!=
literal|null
condition|;
control|)
block|{
continue|continue;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got exception: "
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

