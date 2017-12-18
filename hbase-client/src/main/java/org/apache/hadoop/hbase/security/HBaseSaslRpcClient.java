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
name|FilterInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterOutputStream
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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|crypto
operator|.
name|aes
operator|.
name|CryptoAES
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HBaseSaslRpcClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|cryptoAesEnable
decl_stmt|;
specifier|private
name|CryptoAES
name|cryptoAES
decl_stmt|;
specifier|private
name|InputStream
name|saslInputStream
decl_stmt|;
specifier|private
name|InputStream
name|cryptoInputStream
decl_stmt|;
specifier|private
name|OutputStream
name|saslOutputStream
decl_stmt|;
specifier|private
name|OutputStream
name|cryptoOutputStream
decl_stmt|;
specifier|private
name|boolean
name|initStreamForCrypto
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
parameter_list|,
name|boolean
name|initStreamForCrypto
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
name|this
operator|.
name|initStreamForCrypto
operator|=
name|initStreamForCrypto
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
comment|// initial the inputStream, outputStream for both Sasl encryption
comment|// and Crypto AES encryption if necessary
comment|// if Crypto AES encryption enabled, the saslInputStream/saslOutputStream is
comment|// only responsible for connection header negotiation,
comment|// cryptoInputStream/cryptoOutputStream is responsible for rpc encryption with Crypto AES
name|saslInputStream
operator|=
operator|new
name|SaslInputStream
argument_list|(
name|inS
argument_list|,
name|saslClient
argument_list|)
expr_stmt|;
name|saslOutputStream
operator|=
operator|new
name|SaslOutputStream
argument_list|(
name|outS
argument_list|,
name|saslClient
argument_list|)
expr_stmt|;
if|if
condition|(
name|initStreamForCrypto
condition|)
block|{
name|cryptoInputStream
operator|=
operator|new
name|WrappedInputStream
argument_list|(
name|inS
argument_list|)
expr_stmt|;
name|cryptoOutputStream
operator|=
operator|new
name|WrappedOutputStream
argument_list|(
name|outS
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
specifier|public
name|String
name|getSaslQOP
parameter_list|()
block|{
return|return
operator|(
name|String
operator|)
name|saslClient
operator|.
name|getNegotiatedProperty
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|)
return|;
block|}
specifier|public
name|void
name|initCryptoCipher
parameter_list|(
name|RPCProtos
operator|.
name|CryptoCipherMeta
name|cryptoCipherMeta
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create SaslAES for client
name|cryptoAES
operator|=
name|EncryptionUtil
operator|.
name|createCryptoAES
argument_list|(
name|cryptoCipherMeta
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|cryptoAesEnable
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Get a SASL wrapped InputStream. Can be called only after saslConnect() has been called.    * @return a SASL wrapped InputStream    * @throws IOException    */
specifier|public
name|InputStream
name|getInputStream
parameter_list|()
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
comment|// If Crypto AES is enabled, return cryptoInputStream which unwrap the data with Crypto AES.
if|if
condition|(
name|cryptoAesEnable
operator|&&
name|cryptoInputStream
operator|!=
literal|null
condition|)
block|{
return|return
name|cryptoInputStream
return|;
block|}
return|return
name|saslInputStream
return|;
block|}
class|class
name|WrappedInputStream
extends|extends
name|FilterInputStream
block|{
specifier|private
name|ByteBuffer
name|unwrappedRpcBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|WrappedInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
name|int
name|n
init|=
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
return|return
operator|(
name|n
operator|!=
operator|-
literal|1
operator|)
condition|?
name|b
index|[
literal|0
index|]
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
name|b
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|int
name|read
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
comment|// fill the buffer with the next RPC message
if|if
condition|(
name|unwrappedRpcBuffer
operator|.
name|remaining
argument_list|()
operator|==
literal|0
condition|)
block|{
name|readNextRpcPacket
argument_list|()
expr_stmt|;
block|}
comment|// satisfy as much of the request as possible
name|int
name|readLen
init|=
name|Math
operator|.
name|min
argument_list|(
name|len
argument_list|,
name|unwrappedRpcBuffer
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|unwrappedRpcBuffer
operator|.
name|get
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|readLen
argument_list|)
expr_stmt|;
return|return
name|readLen
return|;
block|}
comment|// unwrap messages with Crypto AES
specifier|private
name|void
name|readNextRpcPacket
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"reading next wrapped RPC packet"
argument_list|)
expr_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|rpcLen
init|=
name|dis
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|rpcBuf
init|=
operator|new
name|byte
index|[
name|rpcLen
index|]
decl_stmt|;
name|dis
operator|.
name|readFully
argument_list|(
name|rpcBuf
argument_list|)
expr_stmt|;
comment|// unwrap with Crypto AES
name|rpcBuf
operator|=
name|cryptoAES
operator|.
name|unwrap
argument_list|(
name|rpcBuf
argument_list|,
literal|0
argument_list|,
name|rpcBuf
operator|.
name|length
argument_list|)
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
literal|"unwrapping token of length:"
operator|+
name|rpcBuf
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|unwrappedRpcBuffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|rpcBuf
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get a SASL wrapped OutputStream. Can be called only after saslConnect() has been called.    * @return a SASL wrapped OutputStream    * @throws IOException    */
specifier|public
name|OutputStream
name|getOutputStream
parameter_list|()
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
comment|// If Crypto AES is enabled, return cryptoOutputStream which wrap the data with Crypto AES.
if|if
condition|(
name|cryptoAesEnable
operator|&&
name|cryptoOutputStream
operator|!=
literal|null
condition|)
block|{
return|return
name|cryptoOutputStream
return|;
block|}
return|return
name|saslOutputStream
return|;
block|}
class|class
name|WrappedOutputStream
extends|extends
name|FilterOutputStream
block|{
specifier|public
name|WrappedOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
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
literal|"wrapping token of length:"
operator|+
name|len
argument_list|)
expr_stmt|;
block|}
comment|// wrap with Crypto AES
name|byte
index|[]
name|wrapped
init|=
name|cryptoAES
operator|.
name|wrap
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|DataOutputStream
name|dob
init|=
operator|new
name|DataOutputStream
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|dob
operator|.
name|writeInt
argument_list|(
name|wrapped
operator|.
name|length
argument_list|)
expr_stmt|;
name|dob
operator|.
name|write
argument_list|(
name|wrapped
argument_list|,
literal|0
argument_list|,
name|wrapped
operator|.
name|length
argument_list|)
expr_stmt|;
name|dob
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

