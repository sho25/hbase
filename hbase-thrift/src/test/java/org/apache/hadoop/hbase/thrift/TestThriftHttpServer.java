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
name|INFOPORT_OPTION
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
name|thrift
operator|.
name|Constants
operator|.
name|PORT_OPTION
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
name|fail
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
name|hbase
operator|.
name|util
operator|.
name|EnvironmentEdgeManager
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
name|EnvironmentEdgeManagerTestHelper
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
name|IncrementingEnvironmentEdge
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
name|TableDescriptorChecker
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
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
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
name|Assert
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_comment
comment|/**  * Start the HBase Thrift HTTP server on a random port through the command-line  * interface and talk to it from client side.  */
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
name|TestThriftHttpServer
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
name|TestThriftHttpServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
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
name|Thread
name|httpServerThread
decl_stmt|;
specifier|private
specifier|volatile
name|Exception
name|httpServerException
decl_stmt|;
specifier|private
name|Exception
name|clientSideException
decl_stmt|;
specifier|private
name|ThriftServer
name|thriftServer
decl_stmt|;
name|int
name|port
decl_stmt|;
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.thrift.http"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|TableDescriptorChecker
operator|.
name|TABLE_SANITY_CHECKS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
comment|//ensure that server time increments every time we do an operation, otherwise
comment|//successive puts having the same timestamp will override each other
name|EnvironmentEdgeManagerTestHelper
operator|.
name|injectEdge
argument_list|(
operator|new
name|IncrementingEnvironmentEdge
argument_list|()
argument_list|)
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
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExceptionThrownWhenMisConfigured
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.thrift.security.qop"
argument_list|,
literal|"privacy"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.thrift.ssl.enabled"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ThriftServer
name|server
init|=
literal|null
decl_stmt|;
name|ExpectedException
name|thrown
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
try|try
block|{
name|thrown
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|thrown
operator|.
name|expectMessage
argument_list|(
literal|"Thrift HTTP Server's QoP is privacy, "
operator|+
literal|"but hbase.thrift.ssl.enabled is false"
argument_list|)
expr_stmt|;
name|server
operator|=
operator|new
name|ThriftServer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|server
operator|.
name|run
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Thrift HTTP Server starts up even with wrong security configurations."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
block|}
specifier|private
name|void
name|startHttpServerThread
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting HBase Thrift server with HTTP server: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|" "
argument_list|)
operator|.
name|join
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|httpServerException
operator|=
literal|null
expr_stmt|;
name|httpServerThread
operator|=
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|thriftServer
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|httpServerException
operator|=
name|e
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|httpServerThread
operator|.
name|setName
argument_list|(
name|ThriftServer
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-httpServer"
argument_list|)
expr_stmt|;
name|httpServerThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
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
name|Test
specifier|public
name|void
name|testRunThriftServerWithHeaderBufferLength
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Test thrift server with HTTP header length less than 64k
try|try
block|{
name|runThriftServer
argument_list|(
literal|1024
operator|*
literal|63
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|tex
parameter_list|)
block|{
name|assertFalse
argument_list|(
name|tex
operator|.
name|getMessage
argument_list|()
operator|.
name|equals
argument_list|(
literal|"HTTP Response code: 431"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Test thrift server with HTTP header length more than 64k, expect an exception
name|exception
operator|.
name|expect
argument_list|(
name|TTransportException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"HTTP Response code: 431"
argument_list|)
expr_stmt|;
name|runThriftServer
argument_list|(
literal|1024
operator|*
literal|64
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|ThriftServer
name|createThriftServer
parameter_list|()
block|{
return|return
operator|new
name|ThriftServer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRunThriftServer
parameter_list|()
throws|throws
name|Exception
block|{
name|runThriftServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|void
name|runThriftServer
parameter_list|(
name|int
name|customHeaderSize
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|port
operator|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|PORT_OPTION
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|port
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|INFOPORT_OPTION
argument_list|)
expr_stmt|;
name|int
name|infoPort
init|=
name|HBaseTestingUtility
operator|.
name|randomFreePort
argument_list|()
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|infoPort
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"start"
argument_list|)
expr_stmt|;
name|thriftServer
operator|=
name|createThriftServer
argument_list|()
expr_stmt|;
name|startHttpServerThread
argument_list|(
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|args
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait up to 10s for the server to start
name|HBaseTestingUtility
operator|.
name|waitForHostPort
argument_list|(
name|HConstants
operator|.
name|LOCALHOST
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|String
name|url
init|=
literal|"http://"
operator|+
name|HConstants
operator|.
name|LOCALHOST
operator|+
literal|":"
operator|+
name|port
decl_stmt|;
try|try
block|{
name|checkHttpMethods
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|talkToThriftServer
argument_list|(
name|url
argument_list|,
name|customHeaderSize
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|clientSideException
operator|=
name|ex
expr_stmt|;
block|}
finally|finally
block|{
name|stopHttpServerThread
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|clientSideException
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Thrift client threw an exception "
operator|+
name|clientSideException
argument_list|)
expr_stmt|;
if|if
condition|(
name|clientSideException
operator|instanceof
name|TTransportException
condition|)
block|{
throw|throw
name|clientSideException
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
name|clientSideException
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|checkHttpMethods
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|Exception
block|{
comment|// HTTP TRACE method should be disabled for security
comment|// See https://www.owasp.org/index.php/Cross_Site_Tracing
name|HttpURLConnection
name|conn
init|=
operator|(
name|HttpURLConnection
operator|)
operator|new
name|URL
argument_list|(
name|url
argument_list|)
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|conn
operator|.
name|setRequestMethod
argument_list|(
literal|"TRACE"
argument_list|)
expr_stmt|;
name|conn
operator|.
name|connect
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_FORBIDDEN
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
specifier|volatile
name|boolean
name|tableCreated
init|=
literal|false
decl_stmt|;
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
name|THttpClient
name|httpClient
init|=
operator|new
name|THttpClient
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|httpClient
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
name|httpClient
operator|.
name|setCustomHeader
argument_list|(
literal|"User-Agent"
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|TProtocol
name|prot
decl_stmt|;
name|prot
operator|=
operator|new
name|TBinaryProtocol
argument_list|(
name|httpClient
argument_list|)
expr_stmt|;
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
if|if
condition|(
operator|!
name|tableCreated
condition|)
block|{
name|TestThriftServer
operator|.
name|createTestTables
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|tableCreated
operator|=
literal|true
expr_stmt|;
block|}
name|TestThriftServer
operator|.
name|checkTableList
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|httpClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|stopHttpServerThread
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping "
operator|+
literal|" Thrift HTTP server"
argument_list|)
expr_stmt|;
name|thriftServer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|httpServerThread
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|httpServerException
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Command-line invocation of HBase Thrift server threw an "
operator|+
literal|"exception"
argument_list|,
name|httpServerException
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|httpServerException
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

