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
operator|.
name|log
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
name|io
operator|.
name|PrintWriter
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
name|HttpServlet
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
name|impl
operator|.
name|Jdk14Logger
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
name|impl
operator|.
name|Log4JLogger
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
name|util
operator|.
name|ServletUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|LogManager
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|slf4j
operator|.
name|impl
operator|.
name|Log4jLoggerAdapter
import|;
end_import

begin_comment
comment|/**  * Change log level in runtime.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|LogLevel
block|{
specifier|public
specifier|static
specifier|final
name|String
name|USAGES
init|=
literal|"\nUsage: General options are:\n"
operator|+
literal|"\t[-getlevel<host:httpPort><name>]\n"
operator|+
literal|"\t[-setlevel<host:httpPort><name><level>]\n"
decl_stmt|;
comment|/**    * A command line implementation    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|3
operator|&&
literal|"-getlevel"
operator|.
name|equals
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|process
argument_list|(
literal|"http://"
operator|+
name|args
index|[
literal|1
index|]
operator|+
literal|"/logLevel?log="
operator|+
name|args
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|4
operator|&&
literal|"-setlevel"
operator|.
name|equals
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|process
argument_list|(
literal|"http://"
operator|+
name|args
index|[
literal|1
index|]
operator|+
literal|"/logLevel?log="
operator|+
name|args
index|[
literal|2
index|]
operator|+
literal|"&level="
operator|+
name|args
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
return|return;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|USAGES
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|process
parameter_list|(
name|String
name|urlstring
parameter_list|)
block|{
try|try
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connecting to "
operator|+
name|url
argument_list|)
expr_stmt|;
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
name|InputStreamReader
name|streamReader
init|=
operator|new
name|InputStreamReader
argument_list|(
name|connection
operator|.
name|getInputStream
argument_list|()
argument_list|)
init|;
name|BufferedReader
name|bufferedReader
operator|=
operator|new
name|BufferedReader
argument_list|(
name|streamReader
argument_list|)
init|)
block|{
for|for
control|(
name|String
name|line
init|;
operator|(
name|line
operator|=
name|bufferedReader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
if|if
condition|(
name|line
operator|.
name|startsWith
argument_list|(
name|MARKER
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|TAG
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
operator|+
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
specifier|final
name|String
name|MARKER
init|=
literal|"<!-- OUTPUT -->"
decl_stmt|;
specifier|static
specifier|final
name|Pattern
name|TAG
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"<[^>]*>"
argument_list|)
decl_stmt|;
comment|/**    * A servlet implementation    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"HDFS"
block|,
literal|"MapReduce"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
specifier|static
class|class
name|Servlet
extends|extends
name|HttpServlet
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|public
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
name|ServletException
throws|,
name|IOException
block|{
comment|// Do the authorization
if|if
condition|(
operator|!
name|HttpServer
operator|.
name|hasAdministratorAccess
argument_list|(
name|getServletContext
argument_list|()
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
condition|)
block|{
return|return;
block|}
name|PrintWriter
name|out
init|=
name|ServletUtil
operator|.
name|initHTML
argument_list|(
name|response
argument_list|,
literal|"Log Level"
argument_list|)
decl_stmt|;
name|String
name|logName
init|=
name|ServletUtil
operator|.
name|getParameter
argument_list|(
name|request
argument_list|,
literal|"log"
argument_list|)
decl_stmt|;
name|String
name|level
init|=
name|ServletUtil
operator|.
name|getParameter
argument_list|(
name|request
argument_list|,
literal|"level"
argument_list|)
decl_stmt|;
if|if
condition|(
name|logName
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"<br /><hr /><h3>Results</h3>"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Submitted Log Name:<b>"
operator|+
name|logName
operator|+
literal|"</b><br />"
argument_list|)
expr_stmt|;
name|Logger
name|log
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|logName
argument_list|)
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Log Class:<b>"
operator|+
name|log
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"</b><br />"
argument_list|)
expr_stmt|;
if|if
condition|(
name|level
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Submitted Level:<b>"
operator|+
name|level
operator|+
literal|"</b><br />"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|log
operator|instanceof
name|Log4JLogger
condition|)
block|{
name|process
argument_list|(
operator|(
operator|(
name|Log4JLogger
operator|)
name|log
operator|)
operator|.
name|getLogger
argument_list|()
argument_list|,
name|level
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|log
operator|instanceof
name|Jdk14Logger
condition|)
block|{
name|process
argument_list|(
operator|(
operator|(
name|Jdk14Logger
operator|)
name|log
operator|)
operator|.
name|getLogger
argument_list|()
argument_list|,
name|level
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|log
operator|instanceof
name|Log4jLoggerAdapter
condition|)
block|{
name|process
argument_list|(
name|LogManager
operator|.
name|getLogger
argument_list|(
name|logName
argument_list|)
argument_list|,
name|level
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Sorry, "
operator|+
name|log
operator|.
name|getClass
argument_list|()
operator|+
literal|" not supported.<br />"
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|println
argument_list|(
name|FORMS
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|ServletUtil
operator|.
name|HTML_TAIL
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|final
name|String
name|FORMS
init|=
literal|"\n<br /><hr /><h3>Get / Set</h3>"
operator|+
literal|"\n<form>Log:<input type='text' size='50' name='log' /> "
operator|+
literal|"<input type='submit' value='Get Log Level' />"
operator|+
literal|"</form>"
operator|+
literal|"\n<form>Log:<input type='text' size='50' name='log' /> "
operator|+
literal|"Level:<input type='text' name='level' /> "
operator|+
literal|"<input type='submit' value='Set Log Level' />"
operator|+
literal|"</form>"
decl_stmt|;
specifier|private
specifier|static
name|void
name|process
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
name|log
parameter_list|,
name|String
name|level
parameter_list|,
name|PrintWriter
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|level
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|level
operator|.
name|equals
argument_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
operator|.
name|toLevel
argument_list|(
name|level
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Bad level :<b>"
operator|+
name|level
operator|+
literal|"</b><br />"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|log
operator|.
name|setLevel
argument_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
operator|.
name|toLevel
argument_list|(
name|level
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Setting Level to "
operator|+
name|level
operator|+
literal|" ...<br />"
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Effective level:<b>"
operator|+
name|log
operator|.
name|getEffectiveLevel
argument_list|()
operator|+
literal|"</b><br />"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|process
parameter_list|(
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Logger
name|log
parameter_list|,
name|String
name|level
parameter_list|,
name|PrintWriter
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|level
operator|!=
literal|null
condition|)
block|{
name|log
operator|.
name|setLevel
argument_list|(
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Level
operator|.
name|parse
argument_list|(
name|level
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Setting Level to "
operator|+
name|level
operator|+
literal|" ...<br />"
argument_list|)
expr_stmt|;
block|}
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Level
name|lev
decl_stmt|;
for|for
control|(
init|;
operator|(
name|lev
operator|=
name|log
operator|.
name|getLevel
argument_list|()
operator|)
operator|==
literal|null
condition|;
name|log
operator|=
name|log
operator|.
name|getParent
argument_list|()
control|)
empty_stmt|;
name|out
operator|.
name|println
argument_list|(
name|MARKER
operator|+
literal|"Effective level:<b>"
operator|+
name|lev
operator|+
literal|"</b><br />"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|LogLevel
parameter_list|()
block|{}
block|}
end_class

end_unit

