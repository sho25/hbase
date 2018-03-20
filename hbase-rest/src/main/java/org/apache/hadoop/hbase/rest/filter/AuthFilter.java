begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|filter
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
name|rest
operator|.
name|Constants
operator|.
name|REST_AUTHENTICATION_PRINCIPAL
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
name|rest
operator|.
name|Constants
operator|.
name|REST_DNS_INTERFACE
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
name|rest
operator|.
name|Constants
operator|.
name|REST_DNS_NAMESERVER
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|DNS
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
name|Strings
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
name|security
operator|.
name|SecurityUtil
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
name|security
operator|.
name|authentication
operator|.
name|server
operator|.
name|AuthenticationFilter
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
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AuthFilter
extends|extends
name|AuthenticationFilter
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
name|AuthFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REST_PREFIX
init|=
literal|"hbase.rest.authentication."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REST_PREFIX_LEN
init|=
name|REST_PREFIX
operator|.
name|length
argument_list|()
decl_stmt|;
comment|/**    * Returns the configuration to be used by the authentication filter    * to initialize the authentication handler.    *    * This filter retrieves all HBase configurations and passes those started    * with REST_PREFIX to the authentication handler.  It is useful to support    * plugging different authentication handlers.   */
annotation|@
name|Override
specifier|protected
name|Properties
name|getConfiguration
parameter_list|(
name|String
name|configPrefix
parameter_list|,
name|FilterConfig
name|filterConfig
parameter_list|)
throws|throws
name|ServletException
block|{
name|Properties
name|props
init|=
name|super
operator|.
name|getConfiguration
argument_list|(
name|configPrefix
argument_list|,
name|filterConfig
argument_list|)
decl_stmt|;
comment|//setting the cookie path to root '/' so it is used for all resources.
name|props
operator|.
name|setProperty
argument_list|(
name|AuthenticationFilter
operator|.
name|COOKIE_PATH
argument_list|,
literal|"/"
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|conf
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
name|REST_PREFIX
argument_list|)
condition|)
block|{
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|REST_AUTHENTICATION_PRINCIPAL
argument_list|)
condition|)
block|{
try|try
block|{
name|String
name|machineName
init|=
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
name|DNS
operator|.
name|getDefaultHost
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|REST_DNS_INTERFACE
argument_list|,
literal|"default"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|REST_DNS_NAMESERVER
argument_list|,
literal|"default"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|value
operator|=
name|SecurityUtil
operator|.
name|getServerPrincipal
argument_list|(
name|value
argument_list|,
name|machineName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|ServletException
argument_list|(
literal|"Failed to retrieve server principal"
argument_list|,
name|ie
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Setting property "
operator|+
name|name
operator|+
literal|"="
operator|+
name|value
argument_list|)
expr_stmt|;
block|}
name|name
operator|=
name|name
operator|.
name|substring
argument_list|(
name|REST_PREFIX_LEN
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|props
return|;
block|}
block|}
end_class

end_unit

