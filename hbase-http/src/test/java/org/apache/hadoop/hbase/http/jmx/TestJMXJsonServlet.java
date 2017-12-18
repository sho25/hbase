begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|jmx
package|;
end_package

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
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
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
name|hbase
operator|.
name|http
operator|.
name|HttpServer
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
name|HttpServerFunctionalTest
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
name|TestJMXJsonServlet
extends|extends
name|HttpServerFunctionalTest
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
name|TestJMXJsonServlet
operator|.
name|class
argument_list|)
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
comment|// Eclipse doesn't pick this up correctly from the plugin
comment|// configuration in the pom.
name|System
operator|.
name|setProperty
argument_list|(
name|HttpServerFunctionalTest
operator|.
name|TEST_BUILD_WEBAPPS
argument_list|,
literal|"target/test-classes/webapps"
argument_list|)
expr_stmt|;
name|server
operator|=
name|createTestServer
argument_list|()
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
block|}
specifier|public
specifier|static
name|void
name|assertReFind
parameter_list|(
name|String
name|re
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|Pattern
name|p
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|re
argument_list|)
decl_stmt|;
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"'"
operator|+
name|p
operator|+
literal|"' does not match "
operator|+
name|value
argument_list|,
name|m
operator|.
name|find
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|result
init|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?qry=java.lang:type=Runtime"
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx?qry=java.lang:type=Runtime RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"name\"\\s*:\\s*\"java.lang:type=Runtime\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"modelerType\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?qry=java.lang:type=Memory"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx?qry=java.lang:type=Memory RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"name\"\\s*:\\s*\"java.lang:type=Memory\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"modelerType\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"name\"\\s*:\\s*\"java.lang:type=Memory\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// test to get an attribute of a mbean
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?get=java.lang:type=Memory::HeapMemoryUsage"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"name\"\\s*:\\s*\"java.lang:type=Memory\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"committed\"\\s*:"
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// negative test to get an attribute of a mbean
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?get=java.lang:type=Memory::"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"ERROR\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// test to get JSONP result
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?qry=java.lang:type=Memory&callback=mycallback1"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx?qry=java.lang:type=Memory&callback=mycallback RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"^mycallback1\\(\\{"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\\}\\);$"
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// negative test to get an attribute of a mbean as JSONP
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?get=java.lang:type=Memory::&callback=mycallback2"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"^mycallback2\\(\\{"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"ERROR\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\\}\\);$"
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// test to get an attribute of a mbean as JSONP
name|result
operator|=
name|readOutput
argument_list|(
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?get=java.lang:type=Memory::HeapMemoryUsage&callback=mycallback3"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"/jmx RESULT: "
operator|+
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"^mycallback3\\(\\{"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"name\"\\s*:\\s*\"java.lang:type=Memory\""
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\"committed\"\\s*:"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertReFind
argument_list|(
literal|"\\}\\);$"
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDisallowedJSONPCallback
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|callback
init|=
literal|"function(){alert('bigproblems!')};foo"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?qry=java.lang:type=Memory&callback="
operator|+
name|URLEncoder
operator|.
name|encode
argument_list|(
name|callback
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|cnxn
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
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|,
name|cnxn
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
name|testUnderscoresInJSONPCallback
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|callback
init|=
literal|"my_function"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
literal|"/jmx?qry=java.lang:type=Memory&callback="
operator|+
name|URLEncoder
operator|.
name|encode
argument_list|(
name|callback
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|cnxn
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
name|HttpServletResponse
operator|.
name|SC_OK
argument_list|,
name|cnxn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

