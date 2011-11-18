begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|token
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
name|hbase
operator|.
name|HConstants
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
name|HTable
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
name|io
operator|.
name|Text
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
comment|/**  * Utility methods for obtaining authentication tokens.  */
end_comment

begin_class
specifier|public
class|class
name|TokenUtil
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TokenUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Obtain and return an authentication token for the current user.    * @param conf The configuration for connecting to the cluster    * @return the authentication token instance    */
specifier|public
specifier|static
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|obtainToken
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
literal|null
decl_stmt|;
try|try
block|{
name|meta
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
literal|".META."
argument_list|)
expr_stmt|;
name|AuthenticationProtocol
name|prot
init|=
name|meta
operator|.
name|coprocessorProxy
argument_list|(
name|AuthenticationProtocol
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
return|return
name|prot
operator|.
name|getAuthenticationToken
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|meta
operator|!=
literal|null
condition|)
block|{
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|Text
name|getClusterId
parameter_list|(
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|token
operator|.
name|getService
argument_list|()
operator|!=
literal|null
condition|?
name|token
operator|.
name|getService
argument_list|()
else|:
operator|new
name|Text
argument_list|(
literal|"default"
argument_list|)
return|;
block|}
comment|/**    * Obtain an authentication token for the given user and add it to the    * user's credentials.    * @param conf The configuration for connecting to the cluster    * @param user The user for whom to obtain the token    * @throws IOException If making a remote call to the {@link TokenProvider} fails    * @throws InterruptedException If executing as the given user is interrupted    */
specifier|public
specifier|static
name|void
name|obtainAndCacheToken
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|UserGroupInformation
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
try|try
block|{
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
init|=
name|user
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|obtainToken
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No token returned for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Obtained token "
operator|+
name|token
operator|.
name|getKind
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|user
operator|.
name|addToken
argument_list|(
name|token
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
literal|"Unexpected exception obtaining token for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Obtain an authentication token on behalf of the given user and add it to    * the credentials for the given map reduce job.    * @param conf The configuration for connecting to the cluster    * @param user The user for whom to obtain the token    * @param job The job instance in which the token should be stored    * @throws IOException If making a remote call to the {@link TokenProvider} fails    * @throws InterruptedException If executing as the given user is interrupted    */
specifier|public
specifier|static
name|void
name|obtainTokenForJob
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|UserGroupInformation
name|user
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
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
init|=
name|user
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|obtainToken
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No token returned for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
throw|;
block|}
name|Text
name|clusterId
init|=
name|getClusterId
argument_list|(
name|token
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Obtained token "
operator|+
name|token
operator|.
name|getKind
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
operator|+
literal|" on cluster "
operator|+
name|clusterId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addToken
argument_list|(
name|clusterId
argument_list|,
name|token
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
literal|"Unexpected exception obtaining token for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Obtain an authentication token on behalf of the given user and add it to    * the credentials for the given map reduce job.    * @param user The user for whom to obtain the token    * @param job The job configuration in which the token should be stored    * @throws IOException If making a remote call to the {@link TokenProvider} fails    * @throws InterruptedException If executing as the given user is interrupted    */
specifier|public
specifier|static
name|void
name|obtainTokenForJob
parameter_list|(
specifier|final
name|JobConf
name|job
parameter_list|,
name|UserGroupInformation
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
try|try
block|{
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|token
init|=
name|user
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|Token
argument_list|<
name|AuthenticationTokenIdentifier
argument_list|>
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|obtainToken
argument_list|(
name|job
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No token returned for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
throw|;
block|}
name|Text
name|clusterId
init|=
name|getClusterId
argument_list|(
name|token
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Obtained token "
operator|+
name|token
operator|.
name|getKind
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
operator|+
literal|" on cluster "
operator|+
name|clusterId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addToken
argument_list|(
name|clusterId
argument_list|,
name|token
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
literal|"Unexpected exception obtaining token for user "
operator|+
name|user
operator|.
name|getUserName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

