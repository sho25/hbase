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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|Sasl
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
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
name|io
operator|.
name|WritableUtils
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
name|ipc
operator|.
name|RemoteException
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
name|SaslInputStream
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
name|SaslOutputStream
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
name|TokenIdentifier
import|;
end_import

begin_comment
comment|/**  * A utility class that encapsulates SASL logic for RPC client. Copied from  *<code>org.apache.hadoop.security</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseSaslRpcClient
extends|extends
name|AbstractHBaseSaslRpcClient
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseSaslRpcClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|HBaseSaslRpcClient
parameter_list|(
name|AuthMethod
name|method
parameter_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|,
name|String
name|serverPrincipal
parameter_list|,
name|boolean
name|fallbackAllowed
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|method
argument_list|,
name|token
argument_list|,
name|serverPrincipal
argument_list|,
name|fallbackAllowed
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HBaseSaslRpcClient
parameter_list|(
name|AuthMethod
name|method
parameter_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
parameter_list|,
name|String
name|serverPrincipal
parameter_list|,
name|boolean
name|fallbackAllowed
parameter_list|,
name|String
name|rpcProtection
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|method
argument_list|,
name|token
argument_list|,
name|serverPrincipal
argument_list|,
name|fallbackAllowed
argument_list|,
name|rpcProtection
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|readStatus
parameter_list|(
name|DataInputStream
name|inStream
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|status
init|=
name|inStream
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// read status
if|if
condition|(
name|status
operator|!=
name|SaslStatus
operator|.
name|SUCCESS
operator|.
name|state
condition|)
block|{
throw|throw
operator|new
name|RemoteException
argument_list|(
name|WritableUtils
operator|.
name|readString
argument_list|(
name|inStream
argument_list|)
argument_list|,
name|WritableUtils
operator|.
name|readString
argument_list|(
name|inStream
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Do client side SASL authentication with server via the given InputStream and OutputStream    * @param inS InputStream to use    * @param outS OutputStream to use    * @return true if connection is set up, or false if needs to switch to simple Auth.    * @throws IOException    */
specifier|public
name|boolean
name|saslConnect
parameter_list|(
name|InputStream
name|inS
parameter_list|,
name|OutputStream
name|outS
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputStream
name|inStream
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|BufferedInputStream
argument_list|(
name|inS
argument_list|)
argument_list|)
decl_stmt|;
name|DataOutputStream
name|outStream
init|=
operator|new
name|DataOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|outS
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|saslToken
init|=
name|getInitialResponse
argument_list|()
decl_stmt|;
if|if
condition|(
name|saslToken
operator|!=
literal|null
condition|)
block|{
name|outStream
operator|.
name|writeInt
argument_list|(
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|saslToken
argument_list|,
literal|0
argument_list|,
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|flush
argument_list|()
expr_stmt|;
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
literal|"Have sent token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" from initSASLContext."
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|isComplete
argument_list|()
condition|)
block|{
name|readStatus
argument_list|(
name|inStream
argument_list|)
expr_stmt|;
name|int
name|len
init|=
name|inStream
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|==
name|SaslUtil
operator|.
name|SWITCH_TO_SIMPLE_AUTH
condition|)
block|{
if|if
condition|(
operator|!
name|fallbackAllowed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Server asks us to fall back to SIMPLE auth, "
operator|+
literal|"but this client is configured to only allow secure connections."
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
literal|"Server asks us to fall back to simple auth."
argument_list|)
expr_stmt|;
block|}
name|dispose
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|saslToken
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
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
literal|"Will read input token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" for processing by initSASLContext"
argument_list|)
expr_stmt|;
block|}
name|inStream
operator|.
name|readFully
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|isComplete
argument_list|()
condition|)
block|{
name|saslToken
operator|=
name|evaluateChallenge
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
if|if
condition|(
name|saslToken
operator|!=
literal|null
condition|)
block|{
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
literal|"Will send token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" from initSASLContext."
argument_list|)
expr_stmt|;
block|}
name|outStream
operator|.
name|writeInt
argument_list|(
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|saslToken
argument_list|,
literal|0
argument_list|,
name|saslToken
operator|.
name|length
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isComplete
argument_list|()
condition|)
block|{
name|readStatus
argument_list|(
name|inStream
argument_list|)
expr_stmt|;
name|saslToken
operator|=
operator|new
name|byte
index|[
name|inStream
operator|.
name|readInt
argument_list|()
index|]
expr_stmt|;
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
literal|"Will read input token of size "
operator|+
name|saslToken
operator|.
name|length
operator|+
literal|" for processing by initSASLContext"
argument_list|)
expr_stmt|;
block|}
name|inStream
operator|.
name|readFully
argument_list|(
name|saslToken
argument_list|)
expr_stmt|;
block|}
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
literal|"SASL client context established. Negotiated QoP: "
operator|+
name|saslClient
operator|.
name|getNegotiatedProperty
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
try|try
block|{
name|saslClient
operator|.
name|dispose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|ignored
parameter_list|)
block|{
comment|// ignore further exceptions during cleanup
block|}
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Get a SASL wrapped InputStream. Can be called only after saslConnect() has been called.    * @param in the InputStream to wrap    * @return a SASL wrapped InputStream    * @throws IOException    */
specifier|public
name|InputStream
name|getInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|saslClient
operator|.
name|isComplete
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Sasl authentication exchange hasn't completed yet"
argument_list|)
throw|;
block|}
return|return
operator|new
name|SaslInputStream
argument_list|(
name|in
argument_list|,
name|saslClient
argument_list|)
return|;
block|}
comment|/**    * Get a SASL wrapped OutputStream. Can be called only after saslConnect() has been called.    * @param out the OutputStream to wrap    * @return a SASL wrapped OutputStream    * @throws IOException    */
specifier|public
name|OutputStream
name|getOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|saslClient
operator|.
name|isComplete
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Sasl authentication exchange hasn't completed yet"
argument_list|)
throw|;
block|}
return|return
operator|new
name|SaslOutputStream
argument_list|(
name|out
argument_list|,
name|saslClient
argument_list|)
return|;
block|}
block|}
end_class

end_unit

