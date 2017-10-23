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
name|conf
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
name|java
operator|.
name|io
operator|.
name|Writer
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
import|;
end_import

begin_comment
comment|/**  * A servlet to print out the running configuration data.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"HBase"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
class|class
name|ConfServlet
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
specifier|private
specifier|static
specifier|final
name|String
name|FORMAT_JSON
init|=
literal|"json"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FORMAT_XML
init|=
literal|"xml"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FORMAT_PARAM
init|=
literal|"format"
decl_stmt|;
comment|/**    * Return the Configuration of the daemon hosting this servlet.    * This is populated when the HttpServer starts.    */
specifier|private
name|Configuration
name|getConfFromContext
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|(
name|Configuration
operator|)
name|getServletContext
argument_list|()
operator|.
name|getAttribute
argument_list|(
name|HttpServer
operator|.
name|CONF_CONTEXT_ATTRIBUTE
argument_list|)
decl_stmt|;
assert|assert
name|conf
operator|!=
literal|null
assert|;
return|return
name|conf
return|;
block|}
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
if|if
condition|(
operator|!
name|HttpServer
operator|.
name|isInstrumentationAccessAllowed
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
name|String
name|format
init|=
name|request
operator|.
name|getParameter
argument_list|(
name|FORMAT_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|format
condition|)
block|{
name|format
operator|=
name|FORMAT_XML
expr_stmt|;
block|}
if|if
condition|(
name|FORMAT_XML
operator|.
name|equals
argument_list|(
name|format
argument_list|)
condition|)
block|{
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/xml; charset=utf-8"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|FORMAT_JSON
operator|.
name|equals
argument_list|(
name|format
argument_list|)
condition|)
block|{
name|response
operator|.
name|setContentType
argument_list|(
literal|"application/json; charset=utf-8"
argument_list|)
expr_stmt|;
block|}
name|Writer
name|out
init|=
name|response
operator|.
name|getWriter
argument_list|()
decl_stmt|;
try|try
block|{
name|writeResponse
argument_list|(
name|getConfFromContext
argument_list|()
argument_list|,
name|out
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BadFormatException
name|bfe
parameter_list|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_BAD_REQUEST
argument_list|,
name|bfe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Guts of the servlet - extracted for easy testing.    */
specifier|static
name|void
name|writeResponse
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Writer
name|out
parameter_list|,
name|String
name|format
parameter_list|)
throws|throws
name|IOException
throws|,
name|BadFormatException
block|{
if|if
condition|(
name|FORMAT_JSON
operator|.
name|equals
argument_list|(
name|format
argument_list|)
condition|)
block|{
name|Configuration
operator|.
name|dumpConfiguration
argument_list|(
name|conf
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|FORMAT_XML
operator|.
name|equals
argument_list|(
name|format
argument_list|)
condition|)
block|{
name|conf
operator|.
name|writeXml
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|BadFormatException
argument_list|(
literal|"Bad format: "
operator|+
name|format
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
class|class
name|BadFormatException
extends|extends
name|Exception
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|BadFormatException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
