begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|UserGroupInformation
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|InvocationTargetException
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
name|Method
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_comment
comment|/**  * Wrapper to abstract out usage of user and group information in HBase.  *  *<p>  * This class provides a common interface for interacting with user and group  * information across changing APIs in different versions of Hadoop.  It only  * provides access to the common set of functionality in  * {@link org.apache.hadoop.security.UserGroupInformation} currently needed by  * HBase, but can be extended as needs change.  *</p>  *  *<p>  * Note: this class does not attempt to support any of the Kerberos  * authentication methods exposed in security-enabled Hadoop (for the moment  * at least), as they're not yet needed.  Properly supporting  * authentication is left up to implementation in secure HBase.  *</p>  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|User
block|{
specifier|private
specifier|static
name|boolean
name|IS_SECURE_HADOOP
init|=
literal|true
decl_stmt|;
static|static
block|{
try|try
block|{
name|UserGroupInformation
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"isSecurityEnabled"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
name|IS_SECURE_HADOOP
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|User
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|UserGroupInformation
name|ugi
decl_stmt|;
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
if|if
condition|(
name|IS_SECURE_HADOOP
condition|)
block|{
return|return
operator|new
name|SecureHadoopUser
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|HadoopUser
argument_list|()
return|;
block|}
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
if|if
condition|(
name|IS_SECURE_HADOOP
condition|)
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
return|return
name|HadoopUser
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
if|if
condition|(
name|IS_SECURE_HADOOP
condition|)
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
else|else
block|{
name|HadoopUser
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
comment|/* Concrete implementations */
comment|/**    * Bridges {@link User} calls to invocations of the appropriate methods    * in {@link org.apache.hadoop.security.UserGroupInformation} in regular    * Hadoop 0.20 (ASF Hadoop and other versions without the backported security    * features).    */
specifier|private
specifier|static
class|class
name|HadoopUser
extends|extends
name|User
block|{
specifier|private
name|HadoopUser
parameter_list|()
block|{
try|try
block|{
name|ugi
operator|=
operator|(
name|UserGroupInformation
operator|)
name|callStatic
argument_list|(
literal|"getCurrentUGI"
argument_list|)
expr_stmt|;
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
literal|"Unexpected exception HadoopUser<init>"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|HadoopUser
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
return|return
name|ugi
operator|.
name|getUserName
argument_list|()
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
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
name|action
parameter_list|)
block|{
name|T
name|result
init|=
literal|null
decl_stmt|;
name|UserGroupInformation
name|previous
init|=
literal|null
decl_stmt|;
try|try
block|{
name|previous
operator|=
operator|(
name|UserGroupInformation
operator|)
name|callStatic
argument_list|(
literal|"getCurrentUGI"
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ugi
operator|!=
literal|null
condition|)
block|{
name|callStatic
argument_list|(
literal|"setCurrentUser"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|UserGroupInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|ugi
block|}
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|action
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|callStatic
argument_list|(
literal|"setCurrentUser"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|UserGroupInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|previous
block|}
argument_list|)
expr_stmt|;
block|}
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
literal|"Unexpected exception in runAs()"
argument_list|)
throw|;
block|}
return|return
name|result
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
name|T
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|UserGroupInformation
name|previous
init|=
operator|(
name|UserGroupInformation
operator|)
name|callStatic
argument_list|(
literal|"getCurrentUGI"
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ugi
operator|!=
literal|null
condition|)
block|{
name|callStatic
argument_list|(
literal|"setCurrentUGI"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|UserGroupInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|ugi
block|}
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|action
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|callStatic
argument_list|(
literal|"setCurrentUGI"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|UserGroupInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|previous
block|}
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|e
throw|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|InterruptedException
condition|)
block|{
throw|throw
operator|(
name|InterruptedException
operator|)
name|e
throw|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|RuntimeException
condition|)
block|{
throw|throw
operator|(
name|RuntimeException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|UndeclaredThrowableException
argument_list|(
name|e
argument_list|,
literal|"Unknown exception in runAs()"
argument_list|)
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
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
try|try
block|{
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.security.UnixUserGroupInformation"
argument_list|)
decl_stmt|;
name|Constructor
name|constructor
init|=
name|c
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|String
index|[]
operator|.
expr|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|constructor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(              )
throw|;
block|}
name|UserGroupInformation
name|newUser
init|=
operator|(
name|UserGroupInformation
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|name
argument_list|,
name|groups
argument_list|)
decl_stmt|;
comment|// set user in configuration -- hack for regular hadoop
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.job.ugi"
argument_list|,
name|newUser
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HadoopUser
argument_list|(
name|newUser
argument_list|)
return|;
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
literal|"UnixUserGroupInformation not found, is this secure Hadoop?"
argument_list|,
name|cnfe
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"No valid constructor found for UnixUserGroupInformation!"
argument_list|,
name|nsme
argument_list|)
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
literal|"Unexpected exception instantiating new UnixUserGroupInformation"
argument_list|)
throw|;
block|}
block|}
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping login, not running on secure Hadoop"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Bridges {@code User} invocations to underlying calls to    * {@link org.apache.hadoop.security.UserGroupInformation} for secure Hadoop    * 0.20 and versions 0.21 and above.    */
specifier|private
specifier|static
class|class
name|SecureHadoopUser
extends|extends
name|User
block|{
specifier|private
name|SecureHadoopUser
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|ugi
operator|=
operator|(
name|UserGroupInformation
operator|)
name|callStatic
argument_list|(
literal|"getCurrentUser"
argument_list|)
expr_stmt|;
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
literal|"Unexpected exception getting current secure user"
argument_list|)
throw|;
block|}
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
try|try
block|{
return|return
operator|(
name|String
operator|)
name|call
argument_list|(
name|ugi
argument_list|,
literal|"getShortUserName"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
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
literal|"Unexpected error getting user short name"
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
try|try
block|{
return|return
operator|(
name|T
operator|)
name|call
argument_list|(
name|ugi
argument_list|,
literal|"doAs"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|PrivilegedAction
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|action
block|}
argument_list|)
return|;
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
literal|"Unexpected exception in runAs()"
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
try|try
block|{
return|return
operator|(
name|T
operator|)
name|call
argument_list|(
name|ugi
argument_list|,
literal|"doAs"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|PrivilegedExceptionAction
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|action
block|}
argument_list|)
return|;
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
literal|"Unexpected exception in runAs(PrivilegedExceptionAction)"
argument_list|)
throw|;
block|}
block|}
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
try|try
block|{
return|return
operator|new
name|SecureHadoopUser
argument_list|(
operator|(
name|UserGroupInformation
operator|)
name|callStatic
argument_list|(
literal|"createUserForTesting"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|String
operator|.
name|class
block|,
name|String
index|[]
operator|.
expr|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|name
block|,
name|groups
block|}
argument_list|)
argument_list|)
return|;
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
literal|"Error creating secure test user"
argument_list|)
throw|;
block|}
block|}
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
comment|// check for SecurityUtil class
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
name|Configuration
operator|.
name|class
block|,
name|String
operator|.
name|class
block|,
name|String
operator|.
name|class
block|,
name|String
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
name|conf
block|,
name|fileConfKey
block|,
name|principalConfKey
block|,
name|localhost
block|}
decl_stmt|;
name|call
argument_list|(
name|c
argument_list|,
literal|null
argument_list|,
literal|"login"
argument_list|,
name|types
argument_list|,
name|args
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
literal|"Unable to login using "
operator|+
literal|"org.apache.hadoop.security.Security.login(). SecurityUtil class "
operator|+
literal|"was not found!  Is this a version of secure Hadoop?"
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
literal|"Unhandled exception in User.login()"
argument_list|)
throw|;
block|}
block|}
block|}
comment|/* Reflection helper methods */
specifier|private
specifier|static
name|Object
name|callStatic
parameter_list|(
name|String
name|methodName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|call
argument_list|(
literal|null
argument_list|,
name|methodName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Object
name|callStatic
parameter_list|(
name|String
name|methodName
parameter_list|,
name|Class
index|[]
name|types
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|call
argument_list|(
literal|null
argument_list|,
name|methodName
argument_list|,
name|types
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Object
name|call
parameter_list|(
name|UserGroupInformation
name|instance
parameter_list|,
name|String
name|methodName
parameter_list|,
name|Class
index|[]
name|types
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|call
argument_list|(
name|UserGroupInformation
operator|.
name|class
argument_list|,
name|instance
argument_list|,
name|methodName
argument_list|,
name|types
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Object
name|call
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
name|T
name|instance
parameter_list|,
name|String
name|methodName
parameter_list|,
name|Class
index|[]
name|types
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|Method
name|m
init|=
name|clazz
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|,
name|types
argument_list|)
decl_stmt|;
return|return
name|m
operator|.
name|invoke
argument_list|(
name|instance
argument_list|,
name|args
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|arge
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Constructed invalid call. class="
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" method="
operator|+
name|methodName
operator|+
literal|" types="
operator|+
name|stringify
argument_list|(
name|types
argument_list|)
argument_list|,
name|arge
argument_list|)
expr_stmt|;
throw|throw
name|arge
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't find method "
operator|+
name|methodName
operator|+
literal|" in "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"!"
argument_list|,
name|nsme
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
comment|// unwrap the underlying exception and rethrow
if|if
condition|(
name|ite
operator|.
name|getTargetException
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ite
operator|.
name|getTargetException
argument_list|()
operator|instanceof
name|Exception
condition|)
block|{
throw|throw
operator|(
name|Exception
operator|)
name|ite
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|ite
operator|.
name|getTargetException
argument_list|()
operator|instanceof
name|Error
condition|)
block|{
throw|throw
operator|(
name|Error
operator|)
name|ite
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
block|}
throw|throw
operator|new
name|UndeclaredThrowableException
argument_list|(
name|ite
argument_list|,
literal|"Unknown exception invoking "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|methodName
operator|+
literal|"()"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|iae
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Denied access calling "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|methodName
operator|+
literal|"()"
argument_list|,
name|iae
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|se
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"SecurityException calling method. class="
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" method="
operator|+
name|methodName
operator|+
literal|" types="
operator|+
name|stringify
argument_list|(
name|types
argument_list|)
argument_list|,
name|se
argument_list|)
expr_stmt|;
throw|throw
name|se
throw|;
block|}
block|}
specifier|private
specifier|static
name|String
name|stringify
parameter_list|(
name|Class
index|[]
name|classes
parameter_list|)
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|classes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Class
name|c
range|:
name|classes
control|)
block|{
if|if
condition|(
name|buf
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|c
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"NULL"
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

