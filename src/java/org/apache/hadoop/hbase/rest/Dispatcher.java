begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
package|;
end_package

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
name|client
operator|.
name|HBaseAdmin
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
name|rest
operator|.
name|exception
operator|.
name|HBaseRestException
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
name|rest
operator|.
name|parser
operator|.
name|HBaseRestParserFactory
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
name|rest
operator|.
name|parser
operator|.
name|IHBaseRestParser
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
name|rest
operator|.
name|serializer
operator|.
name|RestSerializerFactory
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
name|Bytes
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
name|InfoServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Connector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|NCSARequestLog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|bio
operator|.
name|SocketConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|handler
operator|.
name|RequestLogHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|webapp
operator|.
name|WebAppContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|thread
operator|.
name|QueuedThreadPool
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Servlet implementation class for hbase REST interface. Presumes container  * ensures single thread through here at any one time (Usually the default  * configuration). In other words, code is not written thread-safe.  *<p>  * This servlet has explicit dependency on Jetty server; it uses the jetty  * implementation of MultipartResponse.  *   *<p>  * TODO:  *<ul>  *<li>multipart/related response is not correct; the servlet setContentType is  * broken. I am unable to add parameters such as boundary or start to  * multipart/related. They get stripped.</li>  *<li>Currently creating a scanner, need to specify a column. Need to make it  * so the HTable instance has current table's metadata to-hand so easy to find  * the list of all column families so can make up list of columns if none  * specified.</li>  *<li>Minor items are we are decoding URLs in places where probably already  * done and how to timeout scanners that are in the scanner list.</li>  *</ul>  *   * @see<a href="http://wiki.apache.org/lucene-hadoop/Hbase/HbaseRest">Hbase  *      REST Specification</a>  */
end_comment

begin_class
specifier|public
class|class
name|Dispatcher
extends|extends
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
block|{
comment|/**    *     */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|8075335435797071569L
decl_stmt|;
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
name|Dispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|DatabaseController
name|dbController
decl_stmt|;
specifier|protected
name|TableController
name|tableController
decl_stmt|;
specifier|protected
name|RowController
name|rowController
decl_stmt|;
specifier|protected
name|ScannerController
name|scannercontroller
decl_stmt|;
specifier|protected
name|TimestampController
name|tsController
decl_stmt|;
specifier|private
name|HBaseConfiguration
name|conf
init|=
literal|null
decl_stmt|;
specifier|public
enum|enum
name|ContentType
block|{
name|XML
argument_list|(
literal|"text/xml"
argument_list|)
block|,
name|JSON
argument_list|(
literal|"application/json"
argument_list|)
block|,
name|PLAIN
argument_list|(
literal|"text/plain"
argument_list|)
block|,
name|MIME
argument_list|(
literal|"multipart/related"
argument_list|)
block|,
name|NOT_ACCEPTABLE
argument_list|(
literal|""
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
specifier|private
name|ContentType
parameter_list|(
specifier|final
name|String
name|t
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|t
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
comment|/**      * Utility method used looking at Accept header content.      *       * @param t      *          The content type to examine.      * @return The enum that matches the prefix of<code>t</code> or the default      *         enum if<code>t</code> is empty. If unsupported type, we return      *         NOT_ACCEPTABLE.      */
specifier|public
specifier|static
name|ContentType
name|getContentType
parameter_list|(
specifier|final
name|String
name|t
parameter_list|)
block|{
comment|// Default to text/plain. Curl sends */*.
if|if
condition|(
name|t
operator|==
literal|null
operator|||
name|t
operator|.
name|equals
argument_list|(
literal|"*/*"
argument_list|)
condition|)
block|{
return|return
name|ContentType
operator|.
name|XML
return|;
block|}
name|String
name|lowerCased
init|=
name|t
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|ContentType
index|[]
name|values
init|=
name|ContentType
operator|.
name|values
argument_list|()
decl_stmt|;
name|ContentType
name|result
init|=
literal|null
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|lowerCased
operator|.
name|startsWith
argument_list|(
name|values
index|[
name|i
index|]
operator|.
name|type
argument_list|)
condition|)
block|{
name|result
operator|=
name|values
index|[
name|i
index|]
expr_stmt|;
break|break;
block|}
block|}
return|return
name|result
operator|==
literal|null
condition|?
name|NOT_ACCEPTABLE
else|:
name|result
return|;
block|}
block|}
comment|/**    * Default constructor    */
specifier|public
name|Dispatcher
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|ServletException
block|{
name|super
operator|.
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|HBaseConfiguration
argument_list|()
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|createControllers
argument_list|()
expr_stmt|;
name|dbController
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|tableController
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|rowController
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|tsController
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|scannercontroller
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"no errors in init."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ServletException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|createControllers
parameter_list|()
block|{
name|dbController
operator|=
operator|new
name|DatabaseController
argument_list|()
expr_stmt|;
name|tableController
operator|=
operator|new
name|TableController
argument_list|()
expr_stmt|;
name|rowController
operator|=
operator|new
name|RowController
argument_list|()
expr_stmt|;
name|tsController
operator|=
operator|new
name|TimestampController
argument_list|()
expr_stmt|;
name|scannercontroller
operator|=
operator|new
name|ScannerController
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServletException
block|{
try|try
block|{
name|Status
name|s
init|=
name|this
operator|.
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|pathSegments
init|=
name|getPathSegments
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
init|=
name|request
operator|.
name|getParameterMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|0
operator|||
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
comment|// if it was a root request, then get some metadata about
comment|// the entire instance.
name|dbController
operator|.
name|get
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|>=
literal|2
operator|&&
name|pathSegments
operator|.
name|length
operator|<=
literal|3
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
operator|>
literal|0
operator|&&
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|ROW
argument_list|)
condition|)
block|{
comment|// if it has table name and row path segments
name|rowController
operator|.
name|get
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|4
operator|&&
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|ROW
argument_list|)
condition|)
block|{
name|tsController
operator|.
name|get
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise, it must be a GET request suitable for the
comment|// table handler.
name|tableController
operator|.
name|get
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"GET - No Error"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"GET - Error: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Status
name|sError
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|sError
operator|.
name|setInternalError
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|sError
operator|.
name|respond
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|f
parameter_list|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|doPost
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServletException
block|{
try|try
block|{
name|Status
name|s
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|pathSegments
init|=
name|getPathSegments
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
init|=
name|request
operator|.
name|getParameterMap
argument_list|()
decl_stmt|;
name|byte
index|[]
name|input
init|=
name|readInputBuffer
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|IHBaseRestParser
name|parser
init|=
name|this
operator|.
name|getParser
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|pathSegments
operator|.
name|length
operator|>=
literal|0
operator|&&
name|pathSegments
operator|.
name|length
operator|<=
literal|1
operator|)
operator|||
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|ENABLE
argument_list|)
operator|||
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|DISABLE
argument_list|)
condition|)
block|{
comment|// this is a table request
name|tableController
operator|.
name|post
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|,
name|input
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// there should be at least two path segments (table name and row or
comment|// scanner)
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|>=
literal|2
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|SCANNER
argument_list|)
condition|)
block|{
name|scannercontroller
operator|.
name|post
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|,
name|input
argument_list|,
name|parser
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|ROW
argument_list|)
operator|&&
name|pathSegments
operator|.
name|length
operator|>=
literal|3
condition|)
block|{
name|rowController
operator|.
name|post
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|,
name|input
argument_list|,
name|parser
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"POST - Error: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Status
name|s_error
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|s_error
operator|.
name|setInternalError
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|s_error
operator|.
name|respond
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|f
parameter_list|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|doPut
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
try|try
block|{
name|byte
index|[]
index|[]
name|pathSegments
init|=
name|getPathSegments
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HBaseRestException
argument_list|(
literal|"method not supported"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|1
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// if it has only table name
name|Status
name|s
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
init|=
name|request
operator|.
name|getParameterMap
argument_list|()
decl_stmt|;
name|IHBaseRestParser
name|parser
init|=
name|this
operator|.
name|getParser
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|byte
index|[]
name|input
init|=
name|readInputBuffer
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|tableController
operator|.
name|put
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|,
name|input
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Equate PUT with a POST.
name|doPost
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|e
parameter_list|)
block|{
try|try
block|{
name|Status
name|s_error
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|s_error
operator|.
name|setInternalError
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|s_error
operator|.
name|respond
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|f
parameter_list|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|doDelete
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServletException
block|{
try|try
block|{
name|Status
name|s
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|pathSegments
init|=
name|getPathSegments
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|queryMap
init|=
name|request
operator|.
name|getParameterMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HBaseRestException
argument_list|(
literal|"method not supported"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|1
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// if it only has only table name
name|tableController
operator|.
name|delete
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|>=
literal|3
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// must be at least two path segments (table name and row or scanner)
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|SCANNER
argument_list|)
operator|&&
name|pathSegments
operator|.
name|length
operator|==
literal|3
operator|&&
name|pathSegments
index|[
literal|2
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// DELETE to a scanner requires at least three path segments
name|scannercontroller
operator|.
name|delete
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|pathSegments
index|[
literal|1
index|]
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|RESTConstants
operator|.
name|ROW
argument_list|)
operator|&&
name|pathSegments
operator|.
name|length
operator|>=
literal|3
condition|)
block|{
name|rowController
operator|.
name|delete
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|pathSegments
operator|.
name|length
operator|==
literal|4
condition|)
block|{
name|tsController
operator|.
name|delete
argument_list|(
name|s
argument_list|,
name|pathSegments
argument_list|,
name|queryMap
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"POST - Error: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Status
name|s_error
init|=
name|createStatus
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
name|s_error
operator|.
name|setInternalError
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|s_error
operator|.
name|respond
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HBaseRestException
name|f
parameter_list|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This method will get the path segments from the HttpServletRequest.  Please    * note that if the first segment of the path is /api this is removed from the     * returning byte array.    *     * @param request    *     * @return request pathinfo split on the '/' ignoring the first '/' so first    * element in pathSegment is not the empty string.    */
specifier|protected
name|byte
index|[]
index|[]
name|getPathSegments
parameter_list|(
specifier|final
name|HttpServletRequest
name|request
parameter_list|)
block|{
name|int
name|context_len
init|=
name|request
operator|.
name|getContextPath
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
decl_stmt|;
name|byte
index|[]
index|[]
name|pathSegments
init|=
name|Bytes
operator|.
name|toByteArrays
argument_list|(
name|request
operator|.
name|getRequestURI
argument_list|()
operator|.
name|substring
argument_list|(
name|context_len
argument_list|)
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|apiAsBytes
init|=
literal|"api"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|apiAsBytes
argument_list|,
name|pathSegments
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|byte
index|[]
index|[]
name|newPathSegments
init|=
operator|new
name|byte
index|[
name|pathSegments
operator|.
name|length
operator|-
literal|1
index|]
index|[]
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
name|newPathSegments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|newPathSegments
index|[
name|i
index|]
operator|=
name|pathSegments
index|[
name|i
operator|+
literal|1
index|]
expr_stmt|;
block|}
name|pathSegments
operator|=
name|newPathSegments
expr_stmt|;
block|}
return|return
name|pathSegments
return|;
block|}
specifier|protected
name|byte
index|[]
name|readInputBuffer
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|)
throws|throws
name|HBaseRestException
block|{
try|try
block|{
name|String
name|resultant
init|=
literal|""
decl_stmt|;
name|BufferedReader
name|r
init|=
name|request
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|int
name|defaultmaxlength
init|=
literal|10
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
name|int
name|maxLength
init|=
name|this
operator|.
name|conf
operator|==
literal|null
condition|?
name|defaultmaxlength
else|:
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.input.limit"
argument_list|,
name|defaultmaxlength
argument_list|)
decl_stmt|;
name|int
name|bufferLength
init|=
literal|640
decl_stmt|;
comment|// TODO make s maxLength and c size values in configuration
if|if
condition|(
operator|!
name|r
operator|.
name|ready
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// If r is not ready wait 1 second
if|if
condition|(
operator|!
name|r
operator|.
name|ready
argument_list|()
condition|)
block|{
comment|// If r still is not ready something is wrong, return
comment|// blank.
return|return
operator|new
name|byte
index|[
literal|0
index|]
return|;
block|}
block|}
name|char
index|[]
name|c
decl_stmt|;
comment|// 40 characters * sizeof(UTF16)
while|while
condition|(
name|r
operator|.
name|ready
argument_list|()
condition|)
block|{
name|c
operator|=
operator|new
name|char
index|[
name|bufferLength
index|]
expr_stmt|;
name|int
name|n
init|=
name|r
operator|.
name|read
argument_list|(
name|c
argument_list|,
literal|0
argument_list|,
name|bufferLength
argument_list|)
decl_stmt|;
name|resultant
operator|+=
operator|new
name|String
argument_list|(
name|c
argument_list|)
expr_stmt|;
if|if
condition|(
name|n
operator|!=
name|bufferLength
condition|)
block|{
break|break;
block|}
elseif|else
if|if
condition|(
name|resultant
operator|.
name|length
argument_list|()
operator|>
name|maxLength
condition|)
block|{
name|resultant
operator|=
name|resultant
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|maxLength
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|resultant
operator|.
name|trim
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HBaseRestException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|IHBaseRestParser
name|getParser
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|)
block|{
return|return
name|HBaseRestParserFactory
operator|.
name|getParser
argument_list|(
name|ContentType
operator|.
name|getContentType
argument_list|(
name|request
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|Status
name|createStatus
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|HBaseRestException
block|{
return|return
operator|new
name|Status
argument_list|(
name|response
argument_list|,
name|RestSerializerFactory
operator|.
name|getSerializer
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
argument_list|,
name|this
operator|.
name|getPathSegments
argument_list|(
name|request
argument_list|)
argument_list|)
return|;
block|}
comment|//
comment|// Main program and support routines
comment|//
specifier|protected
specifier|static
name|void
name|printUsageAndExit
parameter_list|()
block|{
name|printUsageAndExit
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|printUsageAndExit
parameter_list|(
specifier|final
name|String
name|message
parameter_list|)
block|{
if|if
condition|(
name|message
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: java org.apache.hadoop.hbase.rest.Dispatcher "
operator|+
literal|"--help | [--port=PORT] [--bind=ADDR] start"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Arguments:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" start Start REST server"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" stop  Stop REST server"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Options:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" port  Port to listen on. Default: 60050."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" bind  Address to bind on. Default: 0.0.0.0."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" max-num-threads  The maximum number of threads for Jetty to run. Defaults to 256."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" help  Print this message and exit."
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/*    * Start up the REST servlet in standalone mode.    *     * @param args    */
specifier|protected
specifier|static
name|void
name|doMain
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
name|int
name|port
init|=
literal|60050
decl_stmt|;
name|String
name|bindAddress
init|=
literal|"0.0.0.0"
decl_stmt|;
name|int
name|numThreads
init|=
literal|256
decl_stmt|;
comment|// Process command-line args. TODO: Better cmd-line processing
comment|// (but hopefully something not as painful as cli options).
specifier|final
name|String
name|addressArgKey
init|=
literal|"--bind="
decl_stmt|;
specifier|final
name|String
name|portArgKey
init|=
literal|"--port="
decl_stmt|;
specifier|final
name|String
name|numThreadsKey
init|=
literal|"--max-num-threads="
decl_stmt|;
for|for
control|(
name|String
name|cmd
range|:
name|args
control|)
block|{
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|addressArgKey
argument_list|)
condition|)
block|{
name|bindAddress
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|addressArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|portArgKey
argument_list|)
condition|)
block|{
name|port
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|portArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"--help"
argument_list|)
operator|||
name|cmd
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
condition|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"start"
argument_list|)
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"stop"
argument_list|)
condition|)
block|{
name|printUsageAndExit
argument_list|(
literal|"To shutdown the REST server run "
operator|+
literal|"bin/hbase-daemon.sh stop rest or send a kill signal to "
operator|+
literal|"the REST server pid"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|numThreadsKey
argument_list|)
condition|)
block|{
name|numThreads
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|numThreadsKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Print out usage if we get to here.
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Server
name|webServer
init|=
operator|new
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Server
argument_list|()
decl_stmt|;
name|Connector
name|connector
init|=
operator|new
name|SocketConnector
argument_list|()
decl_stmt|;
name|connector
operator|.
name|setPort
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setHost
argument_list|(
name|bindAddress
argument_list|)
expr_stmt|;
name|QueuedThreadPool
name|pool
init|=
operator|new
name|QueuedThreadPool
argument_list|()
decl_stmt|;
name|pool
operator|.
name|setMaxThreads
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|addConnector
argument_list|(
name|connector
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|setThreadPool
argument_list|(
name|pool
argument_list|)
expr_stmt|;
name|WebAppContext
name|wac
init|=
operator|new
name|WebAppContext
argument_list|()
decl_stmt|;
name|wac
operator|.
name|setContextPath
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|wac
operator|.
name|setWar
argument_list|(
name|InfoServer
operator|.
name|getWebAppDir
argument_list|(
literal|"rest"
argument_list|)
argument_list|)
expr_stmt|;
name|NCSARequestLog
name|ncsa
init|=
operator|new
name|NCSARequestLog
argument_list|()
decl_stmt|;
name|ncsa
operator|.
name|setLogLatency
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|RequestLogHandler
name|rlh
init|=
operator|new
name|RequestLogHandler
argument_list|()
decl_stmt|;
name|rlh
operator|.
name|setRequestLog
argument_list|(
name|ncsa
argument_list|)
expr_stmt|;
name|rlh
operator|.
name|setHandler
argument_list|(
name|wac
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|addHandler
argument_list|(
name|rlh
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param args    * @throws Exception    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting restServer"
argument_list|)
expr_stmt|;
name|doMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

