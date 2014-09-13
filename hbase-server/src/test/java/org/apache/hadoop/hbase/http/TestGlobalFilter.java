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
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|FilterChain
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|FilterConfig
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletResponse
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
name|HttpServletRequest
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
name|net
operator|.
name|NetUtils
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
name|TestGlobalFilter
extends|extends
name|HttpServerFunctionalTest
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HttpServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|RECORDS
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/** A very simple filter that records accessed uri's */
specifier|static
specifier|public
class|class
name|RecordingFilter
implements|implements
name|Filter
block|{
specifier|private
name|FilterConfig
name|filterConfig
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|FilterConfig
name|filterConfig
parameter_list|)
block|{
name|this
operator|.
name|filterConfig
operator|=
name|filterConfig
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|destroy
parameter_list|()
block|{
name|this
operator|.
name|filterConfig
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doFilter
parameter_list|(
name|ServletRequest
name|request
parameter_list|,
name|ServletResponse
name|response
parameter_list|,
name|FilterChain
name|chain
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServletException
block|{
if|if
condition|(
name|filterConfig
operator|==
literal|null
condition|)
return|return;
name|String
name|uri
init|=
operator|(
operator|(
name|HttpServletRequest
operator|)
name|request
operator|)
operator|.
name|getRequestURI
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"filtering "
operator|+
name|uri
argument_list|)
expr_stmt|;
name|RECORDS
operator|.
name|add
argument_list|(
name|uri
argument_list|)
expr_stmt|;
name|chain
operator|.
name|doFilter
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
comment|/** Configuration for RecordingFilter */
specifier|static
specifier|public
class|class
name|Initializer
extends|extends
name|FilterInitializer
block|{
specifier|public
name|Initializer
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|initFilter
parameter_list|(
name|FilterContainer
name|container
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|container
operator|.
name|addGlobalFilter
argument_list|(
literal|"recording"
argument_list|,
name|RecordingFilter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** access a url, ignoring some IOException such as the page does not exist */
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"access "
operator|+
name|urlstring
argument_list|)
expr_stmt|;
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
block|{
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
argument_list|)
argument_list|)
decl_stmt|;
try|try
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
empty_stmt|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|warn
argument_list|(
literal|"urlstring="
operator|+
name|urlstring
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testServletFilter
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
comment|//start a http server with CountingFilter
name|conf
operator|.
name|set
argument_list|(
name|HttpServer
operator|.
name|FILTER_INITIALIZERS_PROPERTY
argument_list|,
name|RecordingFilter
operator|.
name|Initializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HttpServer
name|http
init|=
name|createTestServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|http
operator|.
name|start
argument_list|()
expr_stmt|;
specifier|final
name|String
name|fsckURL
init|=
literal|"/fsck"
decl_stmt|;
specifier|final
name|String
name|stacksURL
init|=
literal|"/stacks"
decl_stmt|;
specifier|final
name|String
name|ajspURL
init|=
literal|"/a.jsp"
decl_stmt|;
specifier|final
name|String
name|listPathsURL
init|=
literal|"/listPaths"
decl_stmt|;
specifier|final
name|String
name|dataURL
init|=
literal|"/data"
decl_stmt|;
specifier|final
name|String
name|streamFile
init|=
literal|"/streamFile"
decl_stmt|;
specifier|final
name|String
name|rootURL
init|=
literal|"/"
decl_stmt|;
specifier|final
name|String
name|allURL
init|=
literal|"/*"
decl_stmt|;
specifier|final
name|String
name|outURL
init|=
literal|"/static/a.out"
decl_stmt|;
specifier|final
name|String
name|logURL
init|=
literal|"/logs/a.log"
decl_stmt|;
specifier|final
name|String
index|[]
name|urls
init|=
block|{
name|fsckURL
block|,
name|stacksURL
block|,
name|ajspURL
block|,
name|listPathsURL
block|,
name|dataURL
block|,
name|streamFile
block|,
name|rootURL
block|,
name|allURL
block|,
name|outURL
block|,
name|logURL
block|}
decl_stmt|;
comment|//access the urls
specifier|final
name|String
name|prefix
init|=
literal|"http://"
operator|+
name|NetUtils
operator|.
name|getHostPortString
argument_list|(
name|http
operator|.
name|getConnectorAddress
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|urls
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|access
argument_list|(
name|prefix
operator|+
name|urls
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|http
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"RECORDS = "
operator|+
name|RECORDS
argument_list|)
expr_stmt|;
comment|//verify records
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|urls
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|RECORDS
operator|.
name|remove
argument_list|(
name|urls
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|RECORDS
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

