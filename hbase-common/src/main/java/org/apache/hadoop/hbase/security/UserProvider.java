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
name|security
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
name|util
operator|.
name|LinkedHashSet
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
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|fs
operator|.
name|CommonConfigurationKeys
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
name|BaseConfigurable
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
name|Groups
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
name|util
operator|.
name|ReflectionUtils
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheLoader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|LoadingCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ListenableFuture
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ListeningExecutorService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|MoreExecutors
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * Provide an instance of a user. Allows custom {@link User} creation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UserProvider
extends|extends
name|BaseConfigurable
block|{
specifier|private
specifier|static
specifier|final
name|String
name|USER_PROVIDER_CONF_KEY
init|=
literal|"hbase.client.userprovider.class"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ListeningExecutorService
name|executor
init|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"group-cache-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|groupCache
init|=
literal|null
decl_stmt|;
specifier|static
name|Groups
name|groups
init|=
name|Groups
operator|.
name|getUserToGroupsMappingService
argument_list|()
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|Groups
name|getGroups
parameter_list|()
block|{
return|return
name|groups
return|;
block|}
specifier|public
specifier|static
name|void
name|setGroups
parameter_list|(
name|Groups
name|groups
parameter_list|)
block|{
name|UserProvider
operator|.
name|groups
operator|=
name|groups
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|UserProvider
operator|.
name|class
init|)
block|{
if|if
condition|(
operator|!
operator|(
name|groups
operator|instanceof
name|User
operator|.
name|TestingGroups
operator|)
condition|)
block|{
name|groups
operator|=
name|Groups
operator|.
name|getUserToGroupsMappingService
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|cacheTimeout
init|=
name|getConf
argument_list|()
operator|.
name|getLong
argument_list|(
name|CommonConfigurationKeys
operator|.
name|HADOOP_SECURITY_GROUPS_CACHE_SECS
argument_list|,
name|CommonConfigurationKeys
operator|.
name|HADOOP_SECURITY_GROUPS_CACHE_SECS_DEFAULT
argument_list|)
operator|*
literal|1000
decl_stmt|;
name|this
operator|.
name|groupCache
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
comment|// This is the same timeout that hadoop uses. So we'll follow suit.
operator|.
name|refreshAfterWrite
argument_list|(
name|cacheTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|expireAfterWrite
argument_list|(
literal|10
operator|*
name|cacheTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
comment|// Set concurrency level equal to the default number of handlers that
comment|// the simple handler spins up.
operator|.
name|concurrencyLevel
argument_list|(
literal|20
argument_list|)
comment|// create the loader
comment|// This just delegates to UGI.
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
argument_list|()
block|{
comment|// Since UGI's don't hash based on the user id
comment|// The cache needs to be keyed on the same thing that Hadoop's Groups class
comment|// uses. So this cache uses shortname.
annotation|@
name|Override
specifier|public
name|String
index|[]
name|load
parameter_list|(
name|String
name|ugi
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|getGroupStrings
argument_list|(
name|ugi
argument_list|)
return|;
block|}
specifier|private
name|String
index|[]
name|getGroupStrings
parameter_list|(
name|String
name|ugi
parameter_list|)
block|{
try|try
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|(
name|groups
operator|.
name|getGroups
argument_list|(
name|ugi
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|String
index|[
literal|0
index|]
return|;
block|}
block|}
comment|// Provide the reload function that uses the executor thread.
annotation|@
name|Override
specifier|public
name|ListenableFuture
argument_list|<
name|String
index|[]
argument_list|>
name|reload
parameter_list|(
specifier|final
name|String
name|k
parameter_list|,
name|String
index|[]
name|oldValue
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|String
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
index|[]
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|getGroupStrings
argument_list|(
name|k
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiate the {@link UserProvider} specified in the configuration and set the passed    * configuration via {@link UserProvider#setConf(Configuration)}    * @param conf to read and set on the created {@link UserProvider}    * @return a {@link UserProvider} ready for use.    */
specifier|public
specifier|static
name|UserProvider
name|instantiate
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|UserProvider
argument_list|>
name|clazz
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|USER_PROVIDER_CONF_KEY
argument_list|,
name|UserProvider
operator|.
name|class
argument_list|,
name|UserProvider
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|/**    * Set the {@link UserProvider} in the given configuration that should be instantiated    * @param conf to update    * @param provider class of the provider to set    */
specifier|public
specifier|static
name|void
name|setUserProviderForTesting
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|UserProvider
argument_list|>
name|provider
parameter_list|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|USER_PROVIDER_CONF_KEY
argument_list|,
name|provider
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the userName for the current logged-in user.    * @throws IOException if the underlying user cannot be obtained    */
specifier|public
name|String
name|getCurrentUserName
parameter_list|()
throws|throws
name|IOException
block|{
name|User
name|user
init|=
name|getCurrent
argument_list|()
decl_stmt|;
return|return
name|user
operator|==
literal|null
condition|?
literal|null
else|:
name|user
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * @return<tt>true</tt> if security is enabled,<tt>false</tt> otherwise    */
specifier|public
name|boolean
name|isHBaseSecurityEnabled
parameter_list|()
block|{
return|return
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|this
operator|.
name|getConf
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return whether or not Kerberos authentication is configured for Hadoop. For non-secure Hadoop,    *         this always returns<code>false</code>. For secure Hadoop, it will return the value    *         from {@code UserGroupInformation.isSecurityEnabled()}.    */
specifier|public
name|boolean
name|isHadoopSecurityEnabled
parameter_list|()
block|{
return|return
name|User
operator|.
name|isSecurityEnabled
argument_list|()
return|;
block|}
comment|/**    * In secure environment, if a user specified his keytab and principal,    * a hbase client will try to login with them. Otherwise, hbase client will try to obtain    * ticket(through kinit) from system.    */
specifier|public
name|boolean
name|shouldLoginFromKeytab
parameter_list|()
block|{
return|return
name|User
operator|.
name|shouldLoginFromKeytab
argument_list|(
name|this
operator|.
name|getConf
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return the current user within the current execution context    * @throws IOException if the user cannot be loaded    */
specifier|public
name|User
name|getCurrent
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|User
operator|.
name|getCurrent
argument_list|()
return|;
block|}
comment|/**    * Wraps an underlying {@code UserGroupInformation} instance.    * @param ugi The base Hadoop user    * @return User    */
specifier|public
name|User
name|create
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
if|if
condition|(
name|ugi
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|User
operator|.
name|SecureHadoopUser
argument_list|(
name|ugi
argument_list|,
name|groupCache
argument_list|)
return|;
block|}
comment|/**    * Log in the current process using the given configuration keys for the credential file and login    * principal. It is for SPN(Service Principal Name) login. SPN should be this format,    * servicename/fully.qualified.domain.name@REALM.    *<p>    *<strong>This is only applicable when running on secure Hadoop</strong> -- see    * org.apache.hadoop.security.SecurityUtil#login(Configuration,String,String,String). On regular    * Hadoop (without security features), this will safely be ignored.    *</p>    * @param fileConfKey Property key used to configure path to the credential file    * @param principalConfKey Property key used to configure login principal    * @param localhost Current hostname to use in any credentials    * @throws IOException underlying exception from SecurityUtil.login() call    */
specifier|public
name|void
name|login
parameter_list|(
name|String
name|fileConfKey
parameter_list|,
name|String
name|principalConfKey
parameter_list|,
name|String
name|localhost
parameter_list|)
throws|throws
name|IOException
block|{
name|User
operator|.
name|login
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|fileConfKey
argument_list|,
name|principalConfKey
argument_list|,
name|localhost
argument_list|)
expr_stmt|;
block|}
comment|/**    * Login with given keytab and principal. This can be used for both SPN(Service Principal Name)    * and UPN(User Principal Name) which format should be clientname@REALM.    * @param fileConfKey config name for client keytab    * @param principalConfKey config name for client principal    * @throws IOException underlying exception from UserGroupInformation.loginUserFromKeytab    */
specifier|public
name|void
name|login
parameter_list|(
name|String
name|fileConfKey
parameter_list|,
name|String
name|principalConfKey
parameter_list|)
throws|throws
name|IOException
block|{
name|User
operator|.
name|login
argument_list|(
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|fileConfKey
argument_list|)
argument_list|,
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|principalConfKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

