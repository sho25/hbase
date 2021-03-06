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
name|Admin
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
name|Table
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
name|filter
operator|.
name|ParseFilter
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
name|security
operator|.
name|UserProvider
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
name|ConnectionCache
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
name|JvmPauseMonitor
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
name|UserGroupInformation
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
name|authorize
operator|.
name|ProxyUsers
import|;
end_import

begin_comment
comment|/**  * Singleton class encapsulating global REST servlet state and functions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RESTServlet
implements|implements
name|Constants
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
name|RESTServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RESTServlet
name|INSTANCE
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|MetricsREST
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|ConnectionCache
name|connectionCache
decl_stmt|;
specifier|private
specifier|final
name|UserGroupInformation
name|realUser
decl_stmt|;
specifier|private
specifier|final
name|JvmPauseMonitor
name|pauseMonitor
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CLEANUP_INTERVAL
init|=
literal|"hbase.rest.connection.cleanup-interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_IDLETIME
init|=
literal|"hbase.rest.connection.max-idletime"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HBASE_REST_SUPPORT_PROXYUSER
init|=
literal|"hbase.rest.support.proxyuser"
decl_stmt|;
name|UserGroupInformation
name|getRealUser
parameter_list|()
block|{
return|return
name|realUser
return|;
block|}
comment|/**    * @return the RESTServlet singleton instance    */
specifier|public
specifier|synchronized
specifier|static
name|RESTServlet
name|getInstance
parameter_list|()
block|{
assert|assert
operator|(
name|INSTANCE
operator|!=
literal|null
operator|)
assert|;
return|return
name|INSTANCE
return|;
block|}
comment|/**    * @return the ConnectionCache instance    */
specifier|public
name|ConnectionCache
name|getConnectionCache
parameter_list|()
block|{
return|return
name|connectionCache
return|;
block|}
comment|/**    * @param conf Existing configuration to use in rest servlet    * @param userProvider the login user provider    * @return the RESTServlet singleton instance    * @throws IOException    */
specifier|public
specifier|synchronized
specifier|static
name|RESTServlet
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|UserProvider
name|userProvider
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|INSTANCE
operator|==
literal|null
condition|)
block|{
name|INSTANCE
operator|=
operator|new
name|RESTServlet
argument_list|(
name|conf
argument_list|,
name|userProvider
argument_list|)
expr_stmt|;
block|}
return|return
name|INSTANCE
return|;
block|}
specifier|public
specifier|synchronized
specifier|static
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|INSTANCE
operator|!=
literal|null
condition|)
block|{
name|INSTANCE
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|INSTANCE
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Constructor with existing configuration    * @param conf existing configuration    * @param userProvider the login user provider    * @throws IOException    */
name|RESTServlet
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|UserProvider
name|userProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|realUser
operator|=
name|userProvider
operator|.
name|getCurrent
argument_list|()
operator|.
name|getUGI
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|registerCustomFilter
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|cleanInterval
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CLEANUP_INTERVAL
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|maxIdleTime
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_IDLETIME
argument_list|,
literal|10
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|connectionCache
operator|=
operator|new
name|ConnectionCache
argument_list|(
name|conf
argument_list|,
name|userProvider
argument_list|,
name|cleanInterval
argument_list|,
name|maxIdleTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|supportsProxyuser
argument_list|()
condition|)
block|{
name|ProxyUsers
operator|.
name|refreshSuperUserGroupsConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|metrics
operator|=
operator|new
name|MetricsREST
argument_list|()
expr_stmt|;
name|pauseMonitor
operator|=
operator|new
name|JvmPauseMonitor
argument_list|(
name|conf
argument_list|,
name|metrics
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|pauseMonitor
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|connectionCache
operator|.
name|getAdmin
argument_list|()
return|;
block|}
comment|/**    * Caller closes the table afterwards.    */
name|Table
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connectionCache
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
name|MetricsREST
name|getMetrics
parameter_list|()
block|{
return|return
name|metrics
return|;
block|}
comment|/**    * Helper method to determine if server should    * only respond to GET HTTP method requests.    * @return boolean for server read-only state    */
name|boolean
name|isReadOnly
parameter_list|()
block|{
return|return
name|getConfiguration
argument_list|()
operator|.
name|getBoolean
argument_list|(
literal|"hbase.rest.readonly"
argument_list|,
literal|false
argument_list|)
return|;
block|}
name|void
name|setEffectiveUser
parameter_list|(
name|String
name|effectiveUser
parameter_list|)
block|{
name|connectionCache
operator|.
name|setEffectiveUser
argument_list|(
name|effectiveUser
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shutdown any services that need to stop    */
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|pauseMonitor
operator|!=
literal|null
condition|)
name|pauseMonitor
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|connectionCache
operator|!=
literal|null
condition|)
name|connectionCache
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|boolean
name|supportsProxyuser
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|HBASE_REST_SUPPORT_PROXYUSER
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|private
name|void
name|registerCustomFilter
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
index|[]
name|filterList
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|Constants
operator|.
name|CUSTOM_FILTERS
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterList
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|filterClass
range|:
name|filterList
control|)
block|{
name|String
index|[]
name|filterPart
init|=
name|filterClass
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterPart
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid filter specification "
operator|+
name|filterClass
operator|+
literal|" - skipping"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ParseFilter
operator|.
name|registerFilter
argument_list|(
name|filterPart
index|[
literal|0
index|]
argument_list|,
name|filterPart
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

