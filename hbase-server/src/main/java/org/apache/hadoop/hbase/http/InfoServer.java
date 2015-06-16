begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
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
name|classification
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_comment
comment|/**  * Create a Jetty embedded server to answer http requests. The primary goal  * is to serve up status information for the server.  * There are three contexts:  *   "/stacks/" -&gt; points to stack trace  *   "/static/" -&gt; points to common static files (src/hbase-webapps/static)  *   "/" -&gt; the jsp server code from (src/hbase-webapps/&lt;name&gt;)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|InfoServer
block|{
specifier|private
specifier|static
specifier|final
name|String
name|HBASE_APP_DIR
init|=
literal|"hbase-webapps"
decl_stmt|;
specifier|private
specifier|final
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
name|httpServer
decl_stmt|;
comment|/**    * Create a status server on the given port.    * The jsp scripts are taken from src/hbase-webapps/<code>name</code>.    * @param name The name of the server    * @param bindAddress address to bind to    * @param port The port to use on the server    * @param findPort whether the server should start at the given port and    * increment by 1 until it finds a free port.    * @throws IOException e    */
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
parameter_list|,
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|HttpConfig
name|httpConfig
init|=
operator|new
name|HttpConfig
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|HttpServer
operator|.
name|Builder
name|builder
init|=
operator|new
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
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setName
argument_list|(
name|name
argument_list|)
operator|.
name|addEndpoint
argument_list|(
name|URI
operator|.
name|create
argument_list|(
name|httpConfig
operator|.
name|getSchemePrefix
argument_list|()
operator|+
name|bindAddress
operator|+
literal|":"
operator|+
name|port
argument_list|)
argument_list|)
operator|.
name|setAppDir
argument_list|(
name|HBASE_APP_DIR
argument_list|)
operator|.
name|setFindPort
argument_list|(
name|findPort
argument_list|)
operator|.
name|setConf
argument_list|(
name|c
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|setLogDir
argument_list|(
name|logDir
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|httpConfig
operator|.
name|isSecure
argument_list|()
condition|)
block|{
name|builder
operator|.
name|keyPassword
argument_list|(
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|c
argument_list|,
literal|"ssl.server.keystore.keypassword"
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|keyStore
argument_list|(
name|c
operator|.
name|get
argument_list|(
literal|"ssl.server.keystore.location"
argument_list|)
argument_list|,
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|c
argument_list|,
literal|"ssl.server.keystore.password"
argument_list|,
literal|null
argument_list|)
argument_list|,
name|c
operator|.
name|get
argument_list|(
literal|"ssl.server.keystore.type"
argument_list|,
literal|"jks"
argument_list|)
argument_list|)
operator|.
name|trustStore
argument_list|(
name|c
operator|.
name|get
argument_list|(
literal|"ssl.server.truststore.location"
argument_list|)
argument_list|,
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|c
argument_list|,
literal|"ssl.server.truststore.password"
argument_list|,
literal|null
argument_list|)
argument_list|,
name|c
operator|.
name|get
argument_list|(
literal|"ssl.server.truststore.type"
argument_list|,
literal|"jks"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|httpServer
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addServlet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|pathSpec
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
name|clazz
parameter_list|)
block|{
name|this
operator|.
name|httpServer
operator|.
name|addServlet
argument_list|(
name|name
argument_list|,
name|pathSpec
argument_list|,
name|clazz
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setAttribute
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|httpServer
operator|.
name|setAttribute
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|httpServer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Deprecated
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|httpServer
operator|.
name|getPort
argument_list|()
return|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|httpServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

