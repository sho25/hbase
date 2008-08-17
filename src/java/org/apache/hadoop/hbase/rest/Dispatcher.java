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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|util
operator|.
name|InfoServer
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
name|mapred
operator|.
name|StatusHttpServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|http
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
name|http
operator|.
name|SocketListener
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
name|servlet
operator|.
name|WebApplicationContext
import|;
end_import

begin_comment
comment|/**  * Servlet implementation class for hbase REST interface.  * Presumes container ensures single thread through here at any one time  * (Usually the default configuration).  In other words, code is not  * written thread-safe.  *<p>This servlet has explicit dependency on Jetty server; it uses the  * jetty implementation of MultipartResponse.  *   *<p>TODO:  *<ul>  *<li>multipart/related response is not correct; the servlet setContentType  * is broken.  I am unable to add parameters such as boundary or start to  * multipart/related.  They get stripped.</li>  *<li>Currently creating a scanner, need to specify a column.  Need to make  * it so the HTable instance has current table's metadata to-hand so easy to  * find the list of all column families so can make up list of columns if none  * specified.</li>  *<li>Minor items are we are decoding URLs in places where probably already  * done and how to timeout scanners that are in the scanner list.</li>  *</ul>  * @see<a href="http://wiki.apache.org/lucene-hadoop/Hbase/HbaseRest">Hbase REST Specification</a>  */
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
implements|implements
name|javax
operator|.
name|servlet
operator|.
name|Servlet
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
name|Dispatcher
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|MetaHandler
name|metaHandler
decl_stmt|;
specifier|private
name|TableHandler
name|tableHandler
decl_stmt|;
specifier|private
name|RowHandler
name|rowHandler
decl_stmt|;
specifier|private
name|ScannerHandler
name|scannerHandler
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SCANNER
init|=
literal|"scanner"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW
init|=
literal|"row"
decl_stmt|;
comment|/**    * Default constructor    */
specifier|public
name|Dispatcher
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
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
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
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
name|metaHandler
operator|=
operator|new
name|MetaHandler
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|tableHandler
operator|=
operator|new
name|TableHandler
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|rowHandler
operator|=
operator|new
name|RowHandler
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
name|scannerHandler
operator|=
operator|new
name|ScannerHandler
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
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
name|String
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
operator|||
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
comment|// if it was a root request, then get some metadata about
comment|// the entire instance.
name|metaHandler
operator|.
name|doGet
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
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
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|&&
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|ROW
argument_list|)
condition|)
block|{
comment|// if it has table name and row path segments
name|rowHandler
operator|.
name|doGet
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise, it must be a GET request suitable for the
comment|// table handler.
name|tableHandler
operator|.
name|doGet
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|String
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
operator|||
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
comment|// if it was a root request, it must be a create table request
name|tableHandler
operator|.
name|doPost
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
comment|// there should be at least two path segments (table name and row or
comment|// scanner or disable/enable operation)
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
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|SCANNER
argument_list|)
operator|&&
name|pathSegments
operator|.
name|length
operator|>=
literal|2
condition|)
block|{
name|scannerHandler
operator|.
name|doPost
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
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
name|rowHandler
operator|.
name|doPost
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
operator|(
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|TableHandler
operator|.
name|DISABLE
argument_list|)
operator|||
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|TableHandler
operator|.
name|ENABLE
argument_list|)
operator|)
operator|&&
name|pathSegments
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|tableHandler
operator|.
name|doPost
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
comment|// if we get to this point, then no handler was matched this request.
name|GenericHandler
operator|.
name|doNotFound
argument_list|(
name|response
argument_list|,
literal|"No handler for "
operator|+
name|request
operator|.
name|getPathInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|String
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
literal|1
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// if it has only table name
name|tableHandler
operator|.
name|doPut
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
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
name|String
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
literal|1
operator|&&
name|pathSegments
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// if it only has only table name
name|tableHandler
operator|.
name|doDelete
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
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
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// must be at least two path segments (table name and row or scanner)
if|if
condition|(
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
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
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// DELETE to a scanner requires at least three path segments
name|scannerHandler
operator|.
name|doDelete
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|pathSegments
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
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
name|rowHandler
operator|.
name|doDelete
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|pathSegments
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|// if we reach this point, then no handler exists for this request.
name|GenericHandler
operator|.
name|doNotFound
argument_list|(
name|response
argument_list|,
literal|"No handler"
argument_list|)
expr_stmt|;
block|}
comment|/*    * @param request    * @return request pathinfo split on the '/' ignoring the first '/' so first    * element in pathSegment is not the empty string.    */
specifier|private
name|String
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
return|return
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
return|;
block|}
comment|//
comment|// Main program and support routines
comment|//
specifier|private
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
specifier|private
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
comment|/*    * Start up the REST servlet in standalone mode.    * @param args    */
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
name|SocketListener
name|listener
init|=
operator|new
name|SocketListener
argument_list|()
decl_stmt|;
name|listener
operator|.
name|setPort
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|listener
operator|.
name|setHost
argument_list|(
name|bindAddress
argument_list|)
expr_stmt|;
name|listener
operator|.
name|setMaxThreads
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|addListener
argument_list|(
name|listener
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
name|webServer
operator|.
name|setRequestLog
argument_list|(
name|ncsa
argument_list|)
expr_stmt|;
name|WebApplicationContext
name|context
init|=
name|webServer
operator|.
name|addWebApplication
argument_list|(
literal|"/api"
argument_list|,
name|InfoServer
operator|.
name|getWebAppDir
argument_list|(
literal|"rest"
argument_list|)
argument_list|)
decl_stmt|;
name|context
operator|.
name|addServlet
argument_list|(
literal|"stacks"
argument_list|,
literal|"/stacks"
argument_list|,
name|StatusHttpServer
operator|.
name|StackServlet
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|addServlet
argument_list|(
literal|"logLevel"
argument_list|,
literal|"/logLevel"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|log
operator|.
name|LogLevel
operator|.
name|Servlet
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param args    * @throws Exception     */
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
name|doMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

