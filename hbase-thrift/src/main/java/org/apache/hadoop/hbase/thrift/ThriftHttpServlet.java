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
name|thrift
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|commons
operator|.
name|net
operator|.
name|util
operator|.
name|Base64
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
name|hbase
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
name|authorize
operator|.
name|AuthorizationException
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServlet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSCredential
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|Oid
import|;
end_import

begin_comment
comment|/**  * Thrift Http Servlet is used for performing Kerberos authentication if security is enabled and  * also used for setting the user specified in "doAs" parameter.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThriftHttpServlet
extends|extends
name|TServlet
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
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ThriftHttpServlet
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|UserGroupInformation
name|realUser
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|securityEnabled
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|doAsEnabled
decl_stmt|;
specifier|private
specifier|transient
name|ThriftServerRunner
operator|.
name|HBaseHandler
name|hbaseHandler
decl_stmt|;
specifier|public
name|ThriftHttpServlet
parameter_list|(
name|TProcessor
name|processor
parameter_list|,
name|TProtocolFactory
name|protocolFactory
parameter_list|,
name|UserGroupInformation
name|realUser
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ThriftServerRunner
operator|.
name|HBaseHandler
name|hbaseHandler
parameter_list|,
name|boolean
name|securityEnabled
parameter_list|,
name|boolean
name|doAsEnabled
parameter_list|)
block|{
name|super
argument_list|(
name|processor
argument_list|,
name|protocolFactory
argument_list|)
expr_stmt|;
name|this
operator|.
name|realUser
operator|=
name|realUser
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|hbaseHandler
operator|=
name|hbaseHandler
expr_stmt|;
name|this
operator|.
name|securityEnabled
operator|=
name|securityEnabled
expr_stmt|;
name|this
operator|.
name|doAsEnabled
operator|=
name|doAsEnabled
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doPost
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
name|String
name|effectiveUser
init|=
name|realUser
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
if|if
condition|(
name|securityEnabled
condition|)
block|{
try|try
block|{
comment|// As Thrift HTTP transport doesn't support SPNEGO yet (THRIFT-889),
comment|// Kerberos authentication is being done at servlet level.
name|effectiveUser
operator|=
name|doKerberosAuth
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HttpAuthenticationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Kerberos Authentication failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// Send a 401 to the client
name|response
operator|.
name|setStatus
argument_list|(
name|HttpServletResponse
operator|.
name|SC_UNAUTHORIZED
argument_list|)
expr_stmt|;
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|println
argument_list|(
literal|"Authentication Error: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|String
name|doAsUserFromQuery
init|=
name|request
operator|.
name|getHeader
argument_list|(
literal|"doAs"
argument_list|)
decl_stmt|;
if|if
condition|(
name|doAsUserFromQuery
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|doAsEnabled
condition|)
block|{
throw|throw
operator|new
name|ServletException
argument_list|(
literal|"Support for proxyuser is not configured"
argument_list|)
throw|;
block|}
comment|// create and attempt to authorize a proxy user (the client is attempting
comment|// to do proxy user)
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|doAsUserFromQuery
argument_list|,
name|realUser
argument_list|)
decl_stmt|;
comment|// validate the proxy user authorization
try|try
block|{
name|ProxyUsers
operator|.
name|authorize
argument_list|(
name|ugi
argument_list|,
name|request
operator|.
name|getRemoteAddr
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServletException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|effectiveUser
operator|=
name|doAsUserFromQuery
expr_stmt|;
block|}
name|hbaseHandler
operator|.
name|setEffectiveUser
argument_list|(
name|effectiveUser
argument_list|)
expr_stmt|;
name|super
operator|.
name|doPost
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
comment|/**    * Do the GSS-API kerberos authentication.    * We already have a logged in subject in the form of serviceUGI,    * which GSS-API will extract information from.    */
specifier|private
name|String
name|doKerberosAuth
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|)
throws|throws
name|HttpAuthenticationException
block|{
try|try
block|{
return|return
name|realUser
operator|.
name|doAs
argument_list|(
operator|new
name|HttpKerberosServerAction
argument_list|(
name|request
argument_list|,
name|realUser
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to perform authentication"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HttpAuthenticationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
class|class
name|HttpKerberosServerAction
implements|implements
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
block|{
name|HttpServletRequest
name|request
decl_stmt|;
name|UserGroupInformation
name|serviceUGI
decl_stmt|;
name|HttpKerberosServerAction
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|UserGroupInformation
name|serviceUGI
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|serviceUGI
operator|=
name|serviceUGI
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|HttpAuthenticationException
block|{
comment|// Get own Kerberos credentials for accepting connection
name|GSSManager
name|manager
init|=
name|GSSManager
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|GSSContext
name|gssContext
init|=
literal|null
decl_stmt|;
name|String
name|serverPrincipal
init|=
name|SecurityUtil
operator|.
name|getPrincipalWithoutRealm
argument_list|(
name|serviceUGI
operator|.
name|getUserName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|// This Oid for Kerberos GSS-API mechanism.
name|Oid
name|kerberosMechOid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2"
argument_list|)
decl_stmt|;
comment|// Oid for SPNego GSS-API mechanism.
name|Oid
name|spnegoMechOid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.3.6.1.5.5.2"
argument_list|)
decl_stmt|;
comment|// Oid for kerberos principal name
name|Oid
name|krb5PrincipalOid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2.1"
argument_list|)
decl_stmt|;
comment|// GSS name for server
name|GSSName
name|serverName
init|=
name|manager
operator|.
name|createName
argument_list|(
name|serverPrincipal
argument_list|,
name|krb5PrincipalOid
argument_list|)
decl_stmt|;
comment|// GSS credentials for server
name|GSSCredential
name|serverCreds
init|=
name|manager
operator|.
name|createCredential
argument_list|(
name|serverName
argument_list|,
name|GSSCredential
operator|.
name|DEFAULT_LIFETIME
argument_list|,
operator|new
name|Oid
index|[]
block|{
name|kerberosMechOid
block|,
name|spnegoMechOid
block|}
argument_list|,
name|GSSCredential
operator|.
name|ACCEPT_ONLY
argument_list|)
decl_stmt|;
comment|// Create a GSS context
name|gssContext
operator|=
name|manager
operator|.
name|createContext
argument_list|(
name|serverCreds
argument_list|)
expr_stmt|;
comment|// Get service ticket from the authorization header
name|String
name|serviceTicketBase64
init|=
name|getAuthHeader
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|byte
index|[]
name|inToken
init|=
name|Base64
operator|.
name|decodeBase64
argument_list|(
name|serviceTicketBase64
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|gssContext
operator|.
name|acceptSecContext
argument_list|(
name|inToken
argument_list|,
literal|0
argument_list|,
name|inToken
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Authenticate or deny based on its context completion
if|if
condition|(
operator|!
name|gssContext
operator|.
name|isEstablished
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HttpAuthenticationException
argument_list|(
literal|"Kerberos authentication failed: "
operator|+
literal|"unable to establish context with the service ticket "
operator|+
literal|"provided by the client."
argument_list|)
throw|;
block|}
return|return
name|SecurityUtil
operator|.
name|getUserFromPrincipal
argument_list|(
name|gssContext
operator|.
name|getSrcName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|GSSException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HttpAuthenticationException
argument_list|(
literal|"Kerberos authentication failed: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|gssContext
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|gssContext
operator|.
name|dispose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|GSSException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error while disposing GSS Context"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Returns the base64 encoded auth header payload      *      * @throws HttpAuthenticationException if a remote or network exception occurs      */
specifier|private
name|String
name|getAuthHeader
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|)
throws|throws
name|HttpAuthenticationException
block|{
name|String
name|authHeader
init|=
name|request
operator|.
name|getHeader
argument_list|(
literal|"Authorization"
argument_list|)
decl_stmt|;
comment|// Each http request must have an Authorization header
if|if
condition|(
name|authHeader
operator|==
literal|null
operator|||
name|authHeader
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HttpAuthenticationException
argument_list|(
literal|"Authorization header received "
operator|+
literal|"from the client is empty."
argument_list|)
throw|;
block|}
name|String
name|authHeaderBase64String
decl_stmt|;
name|int
name|beginIndex
init|=
operator|(
literal|"Negotiate "
operator|)
operator|.
name|length
argument_list|()
decl_stmt|;
name|authHeaderBase64String
operator|=
name|authHeader
operator|.
name|substring
argument_list|(
name|beginIndex
argument_list|)
expr_stmt|;
comment|// Authorization header must have a payload
if|if
condition|(
name|authHeaderBase64String
operator|==
literal|null
operator|||
name|authHeaderBase64String
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HttpAuthenticationException
argument_list|(
literal|"Authorization header received "
operator|+
literal|"from the client does not contain any data."
argument_list|)
throw|;
block|}
return|return
name|authHeaderBase64String
return|;
block|}
block|}
block|}
end_class

end_unit

