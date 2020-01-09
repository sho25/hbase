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
name|http
package|;
end_package

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
name|ServletException
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|DefaultServlet
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
comment|/**  * Servlet to serve files generated by {@link ProfileServlet}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ProfileOutputServlet
extends|extends
name|DefaultServlet
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
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
name|ProfileOutputServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REFRESH_PERIOD
init|=
literal|2
decl_stmt|;
comment|// Alphanumeric characters, plus percent (url-encoding), equals, ampersand, dot and hyphen
specifier|private
specifier|static
specifier|final
name|Pattern
name|ALPHA_NUMERIC
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[a-zA-Z0-9%=&.\\-]*"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|doGet
parameter_list|(
specifier|final
name|HttpServletRequest
name|req
parameter_list|,
specifier|final
name|HttpServletResponse
name|resp
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|String
name|absoluteDiskPath
init|=
name|getServletContext
argument_list|()
operator|.
name|getRealPath
argument_list|(
name|req
operator|.
name|getPathInfo
argument_list|()
argument_list|)
decl_stmt|;
name|File
name|requestedFile
init|=
operator|new
name|File
argument_list|(
name|absoluteDiskPath
argument_list|)
decl_stmt|;
comment|// async-profiler version 1.4 writes 'Started [cpu] profiling' to output file when profiler is
comment|// running which gets replaced by final output. If final output is not ready yet, the file size
comment|// will be<100 bytes (in all modes).
if|if
condition|(
name|requestedFile
operator|.
name|length
argument_list|()
operator|<
literal|100
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|requestedFile
operator|+
literal|" is incomplete. Sending auto-refresh header."
argument_list|)
expr_stmt|;
name|String
name|refreshUrl
init|=
name|req
operator|.
name|getRequestURI
argument_list|()
decl_stmt|;
comment|// Rebuild the query string (if we have one)
if|if
condition|(
name|req
operator|.
name|getQueryString
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|refreshUrl
operator|+=
literal|"?"
operator|+
name|sanitize
argument_list|(
name|req
operator|.
name|getQueryString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ProfileServlet
operator|.
name|setResponseHeader
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setHeader
argument_list|(
literal|"Refresh"
argument_list|,
name|REFRESH_PERIOD
operator|+
literal|";"
operator|+
name|refreshUrl
argument_list|)
expr_stmt|;
name|resp
operator|.
name|getWriter
argument_list|()
operator|.
name|write
argument_list|(
literal|"This page will be auto-refreshed every "
operator|+
name|REFRESH_PERIOD
operator|+
literal|" seconds until the output file is ready. Redirecting to "
operator|+
name|refreshUrl
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|doGet
argument_list|(
name|req
argument_list|,
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|String
name|sanitize
parameter_list|(
name|String
name|input
parameter_list|)
block|{
comment|// Basic test to try to avoid any XSS attacks or HTML content showing up.
comment|// Duplicates HtmlQuoting a little, but avoid destroying ampersand.
if|if
condition|(
name|ALPHA_NUMERIC
operator|.
name|matcher
argument_list|(
name|input
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|input
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Non-alphanumeric data found in input, aborting."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

