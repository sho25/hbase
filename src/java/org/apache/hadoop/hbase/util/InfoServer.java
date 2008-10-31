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
name|util
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
name|FileNotFoundException
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
name|net
operator|.
name|URL
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
name|http
operator|.
name|HttpServer
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
name|HttpContext
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
name|handler
operator|.
name|ResourceHandler
import|;
end_import

begin_comment
comment|/**  * Create a Jetty embedded server to answer http requests. The primary goal  * is to serve up status information for the server.  * There are three contexts:  *   "/stacks/" -> points to stack trace  *   "/static/" -> points to common static files (src/webapps/static)  *   "/" -> the jsp server code from (src/webapps/<name>)  */
end_comment

begin_class
specifier|public
class|class
name|InfoServer
extends|extends
name|HttpServer
block|{
comment|/**    * Create a status server on the given port.    * The jsp scripts are taken from src/webapps/<code>name<code>.    * @param name The name of the server    * @param port The port to use on the server    * @param findPort whether the server should start at the given port and     * increment by 1 until it finds a free port.    */
specifier|public
name|InfoServer
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bindAddress
parameter_list|,
name|int
name|port
parameter_list|,
name|boolean
name|findPort
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|bindAddress
argument_list|,
name|port
argument_list|,
name|findPort
argument_list|)
expr_stmt|;
comment|// Set up the context for "/logs/" if "hbase.log.dir" property is defined.
name|String
name|logDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"hbase.log.dir"
argument_list|)
decl_stmt|;
if|if
condition|(
name|logDir
operator|!=
literal|null
condition|)
block|{
name|HttpContext
name|logContext
init|=
operator|new
name|HttpContext
argument_list|()
decl_stmt|;
name|logContext
operator|.
name|setContextPath
argument_list|(
literal|"/logs/*"
argument_list|)
expr_stmt|;
name|logContext
operator|.
name|setResourceBase
argument_list|(
name|logDir
argument_list|)
expr_stmt|;
name|logContext
operator|.
name|addHandler
argument_list|(
operator|new
name|ResourceHandler
argument_list|()
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|addContext
argument_list|(
name|logContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"master"
argument_list|)
condition|)
block|{
comment|// Put up the rest webapp.
name|this
operator|.
name|webServer
operator|.
name|addWebApplication
argument_list|(
literal|"/api"
argument_list|,
name|getWebAppDir
argument_list|(
literal|"rest"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**   * Get the pathname to the<code>path</code> files.   * @param path Path to find.   * @return the pathname as a URL   */
specifier|private
specifier|static
name|String
name|getWebAppsPath
parameter_list|(
specifier|final
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|URL
name|url
init|=
name|InfoServer
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|url
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"webapps not found in CLASSPATH: "
operator|+
name|path
argument_list|)
throw|;
return|return
name|url
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Get the path for this web app    * @param webappName web app    * @return path    * @throws IOException    */
specifier|public
specifier|static
name|String
name|getWebAppDir
parameter_list|(
specifier|final
name|String
name|webappName
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|webappDir
init|=
literal|null
decl_stmt|;
try|try
block|{
name|webappDir
operator|=
name|getWebAppsPath
argument_list|(
literal|"webapps"
operator|+
name|File
operator|.
name|separator
operator|+
name|webappName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
comment|// Retry.  Resource may be inside jar on a windows machine.
name|webappDir
operator|=
name|getWebAppsPath
argument_list|(
literal|"webapps/"
operator|+
name|webappName
argument_list|)
expr_stmt|;
block|}
return|return
name|webappDir
return|;
block|}
block|}
end_class

end_unit

