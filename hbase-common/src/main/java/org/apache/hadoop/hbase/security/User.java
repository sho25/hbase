begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lang
operator|.
name|reflect
operator|.
name|UndeclaredThrowableException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Methods
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
name|JobConf
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
name|mapreduce
operator|.
name|Job
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
name|token
operator|.
name|Token
import|;
end_import

begin_comment
comment|/**  * Wrapper to abstract out usage of user and group information in HBase.  *  *<p>  * This class provides a common interface for interacting with user and group  * information across changing APIs in different versions of Hadoop.  It only  * provides access to the common set of functionality in  * {@link org.apache.hadoop.security.UserGroupInformation} currently needed by  * HBase, but can be extended as needs change.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|User
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_SECURITY_CONF_KEY
init|=
literal|"hbase.security.authentication"
decl_stmt|;
specifier|protected
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|public
name|UserGroupInformation
name|getUGI
parameter_list|()
block|{
return|return
name|ugi
return|;
block|}
comment|/**    * Returns the full user name.  For Kerberos principals this will include    * the host and realm portions of the principal name.    * @return User full name.    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|ugi
operator|.
name|getUserName
argument_list|()
return|;
block|}
comment|/**    * Returns the list of groups of which this user is a member.  On secure    * Hadoop this returns the group information for the user as resolved on the    * server.  For 0.20 based Hadoop, the group names are passed from the client.    */
specifier|public
name|String
index|[]
name|getGroupNames
parameter_list|()
block|{
return|return
name|ugi
operator|.
name|getGroupNames
argument_list|()
return|;
block|}
comment|/**    * Returns the shortened version of the user name -- the portion that maps    * to an operating system user name.    * @return Short name    */
specifier|public
specifier|abstract
name|String
name|getShortName
parameter_list|()
function_decl|;
comment|/**    * Executes the given action within the context of this user.    */
specifier|public
specifier|abstract
parameter_list|<
name|T
parameter_list|>
name|T
name|runAs
parameter_list|(
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
name|action
parameter_list|)
function_decl|;
comment|/**    * Executes the given action within the context of this user.    */
specifier|public
specifier|abstract
parameter_list|<
name|T
parameter_list|>
name|T
name|runAs
parameter_list|(
name|PrivilegedExceptionAction
argument_list|<
name|T
argument_list|>
name|action
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Requests an authentication token for this user and stores it in the    * user's credentials.    *    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|obtainAuthTokenForJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Requests an authentication token for this user and stores it in the    * user's credentials.    *    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|obtainAuthTokenForJob
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Returns the Token of the specified kind associated with this user,    * or null if the Token is not present.    *    * @param kind the kind of token    * @param service service on which the token is supposed to be used    * @return the token of the specified kind.    */
specifier|public
name|Token
argument_list|<
name|?
argument_list|>
name|getToken
parameter_list|(
name|String
name|kind
parameter_list|,
name|String
name|service
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Token
argument_list|<
name|?
argument_list|>
name|token
range|:
name|ugi
operator|.
name|getTokens
argument_list|()
control|)
block|{
if|if
condition|(
name|token
operator|.
name|getKind
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|kind
argument_list|)
operator|&&
operator|(
name|service
operator|!=
literal|null
operator|&&
name|token
operator|.
name|getService
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|service
argument_list|)
operator|)
condition|)
block|{
return|return
name|token
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|ugi
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|User
operator|)
name|o
operator|)
operator|.
name|ugi
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|ugi
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|ugi
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Returns the {@code User} instance within current execution context.    */
specifier|public
specifier|static
name|User
name|getCurrent
parameter_list|()
throws|throws
name|IOException
block|{
name|User
name|user
init|=
operator|new
name|SecureHadoopUser
argument_list|()
decl_stmt|;
if|if
condition|(
name|user
operator|.
name|getUGI
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|user
return|;
block|}
comment|/**    * Executes the given action as the login user    * @param action    * @return the result of the action    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|runAsLoginUser
parameter_list|(
name|PrivilegedExceptionAction
argument_list|<
name|T
argument_list|>
name|action
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.security.SecurityUtil"
argument_list|)
decl_stmt|;
name|Class
index|[]
name|types
init|=
operator|new
name|Class
index|[]
block|{
name|PrivilegedExceptionAction
operator|.
name|class
block|}
decl_stmt|;
name|Object
index|[]
name|args
init|=
operator|new
name|Object
index|[]
block|{
name|action
block|}
decl_stmt|;
return|return
operator|(
name|T
operator|)
name|Methods
operator|.
name|call
argument_list|(
name|c
argument_list|,
literal|null
argument_list|,
literal|"doAsLoginUser"
argument_list|,
name|types
argument_list|,
name|args
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Wraps an underlying {@code UserGroupInformation} instance.    * @param ugi The base Hadoop user    * @return User    */
specifier|public
specifier|static
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
name|SecureHadoopUser
argument_list|(
name|ugi
argument_list|)
return|;
block|}
comment|/**    * Generates a new {@code User} instance specifically for use in test code.    * @param name the full username    * @param groups the group names to which the test user will belong    * @return a new<code>User</code> instance    */
specifier|public
specifier|static
name|User
name|createUserForTesting
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
index|[]
name|groups
parameter_list|)
block|{
return|return
name|SecureHadoopUser
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
name|name
argument_list|,
name|groups
argument_list|)
return|;
block|}
comment|/**    * Log in the current process using the given configuration keys for the    * credential file and login principal.    *    *<p><strong>This is only applicable when    * running on secure Hadoop</strong> -- see    * org.apache.hadoop.security.SecurityUtil#login(Configuration,String,String,String).    * On regular Hadoop (without security features), this will safely be ignored.    *</p>    *    * @param conf The configuration data to use    * @param fileConfKey Property key used to configure path to the credential file    * @param principalConfKey Property key used to configure login principal    * @param localhost Current hostname to use in any credentials    * @throws IOException underlying exception from SecurityUtil.login() call    */
specifier|public
specifier|static
name|void
name|login
parameter_list|(
name|Configuration
name|conf
parameter_list|,
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
name|SecureHadoopUser
operator|.
name|login
argument_list|(
name|conf
argument_list|,
name|fileConfKey
argument_list|,
name|principalConfKey
argument_list|,
name|localhost
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns whether or not Kerberos authentication is configured for Hadoop.    * For non-secure Hadoop, this always returns<code>false</code>.    * For secure Hadoop, it will return the value from    * {@code UserGroupInformation.isSecurityEnabled()}.    */
specifier|public
specifier|static
name|boolean
name|isSecurityEnabled
parameter_list|()
block|{
return|return
name|SecureHadoopUser
operator|.
name|isSecurityEnabled
argument_list|()
return|;
block|}
comment|/**    * Returns whether or not secure authentication is enabled for HBase. Note that    * HBase security requires HDFS security to provide any guarantees, so it is    * recommended that secure HBase should run on secure HDFS.    */
specifier|public
specifier|static
name|boolean
name|isHBaseSecurityEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
literal|"kerberos"
operator|.
name|equalsIgnoreCase
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HBASE_SECURITY_CONF_KEY
argument_list|)
argument_list|)
return|;
block|}
comment|/* Concrete implementations */
comment|/**    * Bridges {@code User} invocations to underlying calls to    * {@link org.apache.hadoop.security.UserGroupInformation} for secure Hadoop    * 0.20 and versions 0.21 and above.    */
specifier|private
specifier|static
class|class
name|SecureHadoopUser
extends|extends
name|User
block|{
specifier|private
name|String
name|shortName
decl_stmt|;
specifier|private
name|SecureHadoopUser
parameter_list|()
throws|throws
name|IOException
block|{
name|ugi
operator|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
expr_stmt|;
block|}
specifier|private
name|SecureHadoopUser
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
name|this
operator|.
name|ugi
operator|=
name|ugi
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getShortName
parameter_list|()
block|{
if|if
condition|(
name|shortName
operator|!=
literal|null
condition|)
return|return
name|shortName
return|;
try|try
block|{
name|shortName
operator|=
name|ugi
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
return|return
name|shortName
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
name|RuntimeException
argument_list|(
literal|"Unexpected error getting user short name"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|runAs
parameter_list|(
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
name|action
parameter_list|)
block|{
return|return
name|ugi
operator|.
name|doAs
argument_list|(
name|action
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|runAs
parameter_list|(
name|PrivilegedExceptionAction
argument_list|<
name|T
argument_list|>
name|action
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|ugi
operator|.
name|doAs
argument_list|(
name|action
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|obtainAuthTokenForJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hbase.security.token.TokenUtil"
argument_list|)
decl_stmt|;
name|Methods
operator|.
name|call
argument_list|(
name|c
argument_list|,
literal|null
argument_list|,
literal|"obtainTokenForJob"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|,
name|UserGroupInformation
operator|.
name|class
block|,
name|Job
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|conf
block|,
name|ugi
block|,
name|job
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failure loading TokenUtil class, "
operator|+
literal|"is secure RPC available?"
argument_list|,
name|cnfe
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
name|ioe
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
name|ie
throw|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|re
parameter_list|)
block|{
throw|throw
name|re
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UndeclaredThrowableException
argument_list|(
name|e
argument_list|,
literal|"Unexpected error calling TokenUtil.obtainAndCacheToken()"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|obtainAuthTokenForJob
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hbase.security.token.TokenUtil"
argument_list|)
decl_stmt|;
name|Methods
operator|.
name|call
argument_list|(
name|c
argument_list|,
literal|null
argument_list|,
literal|"obtainTokenForJob"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|JobConf
operator|.
name|class
block|,
name|UserGroupInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|job
block|,
name|ugi
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failure loading TokenUtil class, "
operator|+
literal|"is secure RPC available?"
argument_list|,
name|cnfe
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
name|ioe
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
name|ie
throw|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|re
parameter_list|)
block|{
throw|throw
name|re
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UndeclaredThrowableException
argument_list|(
name|e
argument_list|,
literal|"Unexpected error calling TokenUtil.obtainAndCacheToken()"
argument_list|)
throw|;
block|}
block|}
comment|/** @see User#createUserForTesting(org.apache.hadoop.conf.Configuration, String, String[]) */
specifier|public
specifier|static
name|User
name|createUserForTesting
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
index|[]
name|groups
parameter_list|)
block|{
return|return
operator|new
name|SecureHadoopUser
argument_list|(
name|UserGroupInformation
operator|.
name|createUserForTesting
argument_list|(
name|name
argument_list|,
name|groups
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Obtain credentials for the current process using the configured      * Kerberos keytab file and principal.      * @see User#login(org.apache.hadoop.conf.Configuration, String, String, String)      *      * @param conf the Configuration to use      * @param fileConfKey Configuration property key used to store the path      * to the keytab file      * @param principalConfKey Configuration property key used to store the      * principal name to login as      * @param localhost the local hostname      */
specifier|public
specifier|static
name|void
name|login
parameter_list|(
name|Configuration
name|conf
parameter_list|,
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
if|if
condition|(
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|SecurityUtil
operator|.
name|login
argument_list|(
name|conf
argument_list|,
name|fileConfKey
argument_list|,
name|principalConfKey
argument_list|,
name|localhost
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Returns the result of {@code UserGroupInformation.isSecurityEnabled()}.      */
specifier|public
specifier|static
name|boolean
name|isSecurityEnabled
parameter_list|()
block|{
return|return
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

