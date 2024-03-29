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
name|regionserver
operator|.
name|wal
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
name|Key
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|KeyException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|fs
operator|.
name|FSDataInputStream
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
name|HBaseInterfaceAudience
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
name|io
operator|.
name|crypto
operator|.
name|Cipher
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
name|Decryptor
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
name|Encryption
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
name|WALProtos
operator|.
name|WALHeader
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
name|EncryptionUtil
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
name|User
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
name|EncryptionTest
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|SecureProtobufLogReader
extends|extends
name|ProtobufLogReader
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
name|SecureProtobufLogReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Decryptor
name|decryptor
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|writerClsNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|writerClsNames
operator|.
name|add
argument_list|(
name|ProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|writerClsNames
operator|.
name|add
argument_list|(
name|SecureProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|writerClsNames
operator|.
name|add
argument_list|(
name|AsyncProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|writerClsNames
operator|.
name|add
argument_list|(
name|SecureAsyncProtobufLogWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getWriterClsNames
parameter_list|()
block|{
return|return
name|writerClsNames
return|;
block|}
annotation|@
name|Override
specifier|protected
name|WALHdrContext
name|readHeader
parameter_list|(
name|WALHeader
operator|.
name|Builder
name|builder
parameter_list|,
name|FSDataInputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|WALHdrContext
name|hdrCtxt
init|=
name|super
operator|.
name|readHeader
argument_list|(
name|builder
argument_list|,
name|stream
argument_list|)
decl_stmt|;
name|WALHdrResult
name|result
init|=
name|hdrCtxt
operator|.
name|getResult
argument_list|()
decl_stmt|;
comment|// We need to unconditionally handle the case where the WAL has a key in
comment|// the header, meaning it is encrypted, even if ENABLE_WAL_ENCRYPTION is
comment|// no longer set in the site configuration.
if|if
condition|(
name|result
operator|==
name|WALHdrResult
operator|.
name|SUCCESS
operator|&&
name|builder
operator|.
name|hasEncryptionKey
argument_list|()
condition|)
block|{
comment|// Serialized header data has been merged into the builder from the
comment|// stream.
name|EncryptionTest
operator|.
name|testKeyProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|EncryptionTest
operator|.
name|testCipherProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Retrieve a usable key
name|byte
index|[]
name|keyBytes
init|=
name|builder
operator|.
name|getEncryptionKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|Key
name|key
init|=
literal|null
decl_stmt|;
name|String
name|walKeyName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_WAL_KEY_NAME_CONF_KEY
argument_list|)
decl_stmt|;
comment|// First try the WAL key, if one is configured
if|if
condition|(
name|walKeyName
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|key
operator|=
name|EncryptionUtil
operator|.
name|unwrapWALKey
argument_list|(
name|conf
argument_list|,
name|walKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|e
parameter_list|)
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
literal|"Unable to unwrap key with WAL key '"
operator|+
name|walKeyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|key
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
name|String
name|masterKeyName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Then, try the cluster master key
name|key
operator|=
name|EncryptionUtil
operator|.
name|unwrapWALKey
argument_list|(
name|conf
argument_list|,
name|masterKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|e
parameter_list|)
block|{
comment|// If the current master key fails to unwrap, try the alternate, if
comment|// one is configured
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
literal|"Unable to unwrap key with current master key '"
operator|+
name|masterKeyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|String
name|alternateKeyName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_ALTERNATE_NAME_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|alternateKeyName
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|key
operator|=
name|EncryptionUtil
operator|.
name|unwrapWALKey
argument_list|(
name|conf
argument_list|,
name|alternateKeyName
argument_list|,
name|keyBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeyException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
else|else
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
block|}
comment|// Use the algorithm the key wants
name|Cipher
name|cipher
init|=
name|Encryption
operator|.
name|getCipher
argument_list|(
name|conf
argument_list|,
name|key
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cipher
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cipher '"
operator|+
name|key
operator|.
name|getAlgorithm
argument_list|()
operator|+
literal|"' is not available"
argument_list|)
throw|;
block|}
comment|// Set up the decryptor for this WAL
name|decryptor
operator|=
name|cipher
operator|.
name|getDecryptor
argument_list|()
expr_stmt|;
name|decryptor
operator|.
name|setKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
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
literal|"Initialized secure protobuf WAL: cipher="
operator|+
name|cipher
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|hdrCtxt
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initAfterCompression
parameter_list|(
name|String
name|cellCodecClsName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|decryptor
operator|!=
literal|null
operator|&&
name|cellCodecClsName
operator|.
name|equals
argument_list|(
name|SecureWALCellCodec
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|WALCellCodec
name|codec
init|=
name|SecureWALCellCodec
operator|.
name|getCodec
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|decryptor
argument_list|)
decl_stmt|;
name|this
operator|.
name|cellDecoder
operator|=
name|codec
operator|.
name|getDecoder
argument_list|(
name|this
operator|.
name|inputStream
argument_list|)
expr_stmt|;
comment|// We do not support compression with WAL encryption
name|this
operator|.
name|compressionContext
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|byteStringUncompressor
operator|=
name|WALCellCodec
operator|.
name|getNoneUncompressor
argument_list|()
expr_stmt|;
name|this
operator|.
name|hasCompression
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|initAfterCompression
argument_list|(
name|cellCodecClsName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

