begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|encoding
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|IOUtils
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
name|ByteBuffInputStream
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
name|TagCompressionContext
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
name|compress
operator|.
name|Compression
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
name|io
operator|.
name|hfile
operator|.
name|HFileContext
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
name|util
operator|.
name|BlockIOUtils
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
name|nio
operator|.
name|ByteBuff
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
name|Bytes
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

begin_comment
comment|/**  * A default implementation of {@link HFileBlockDecodingContext}. It assumes the  * block data section is compressed as a whole.  *  * @see HFileBlockDefaultEncodingContext for the default compression context  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileBlockDefaultDecodingContext
implements|implements
name|HFileBlockDecodingContext
block|{
specifier|private
specifier|final
name|HFileContext
name|fileContext
decl_stmt|;
specifier|private
name|TagCompressionContext
name|tagCompressionContext
decl_stmt|;
specifier|public
name|HFileBlockDefaultDecodingContext
parameter_list|(
name|HFileContext
name|fileContext
parameter_list|)
block|{
name|this
operator|.
name|fileContext
operator|=
name|fileContext
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareDecoding
parameter_list|(
name|int
name|onDiskSizeWithoutHeader
parameter_list|,
name|int
name|uncompressedSizeWithoutHeader
parameter_list|,
name|ByteBuff
name|blockBufferWithoutHeader
parameter_list|,
name|ByteBuff
name|onDiskBlock
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ByteBuffInputStream
name|byteBuffInputStream
init|=
operator|new
name|ByteBuffInputStream
argument_list|(
name|onDiskBlock
argument_list|)
decl_stmt|;
name|InputStream
name|dataInputStream
init|=
operator|new
name|DataInputStream
argument_list|(
name|byteBuffInputStream
argument_list|)
decl_stmt|;
try|try
block|{
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|fileContext
operator|.
name|getEncryptionContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|cryptoContext
operator|!=
name|Encryption
operator|.
name|Context
operator|.
name|NONE
condition|)
block|{
name|Cipher
name|cipher
init|=
name|cryptoContext
operator|.
name|getCipher
argument_list|()
decl_stmt|;
name|Decryptor
name|decryptor
init|=
name|cipher
operator|.
name|getDecryptor
argument_list|()
decl_stmt|;
name|decryptor
operator|.
name|setKey
argument_list|(
name|cryptoContext
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// Encrypted block format:
comment|// +--------------------------+
comment|// | byte iv length           |
comment|// +--------------------------+
comment|// | iv data ...              |
comment|// +--------------------------+
comment|// | encrypted block data ... |
comment|// +--------------------------+
name|int
name|ivLength
init|=
name|dataInputStream
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|ivLength
operator|>
literal|0
condition|)
block|{
name|byte
index|[]
name|iv
init|=
operator|new
name|byte
index|[
name|ivLength
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|dataInputStream
argument_list|,
name|iv
argument_list|)
expr_stmt|;
name|decryptor
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
comment|// All encrypted blocks will have a nonzero IV length. If we see an IV
comment|// length of zero, this means the encoding context had 0 bytes of
comment|// plaintext to encode.
name|decryptor
operator|.
name|reset
argument_list|()
expr_stmt|;
name|dataInputStream
operator|=
name|decryptor
operator|.
name|createDecryptionStream
argument_list|(
name|dataInputStream
argument_list|)
expr_stmt|;
block|}
name|onDiskSizeWithoutHeader
operator|-=
name|Bytes
operator|.
name|SIZEOF_BYTE
operator|+
name|ivLength
expr_stmt|;
block|}
name|Compression
operator|.
name|Algorithm
name|compression
init|=
name|fileContext
operator|.
name|getCompression
argument_list|()
decl_stmt|;
if|if
condition|(
name|compression
operator|!=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
condition|)
block|{
name|Compression
operator|.
name|decompress
argument_list|(
name|blockBufferWithoutHeader
argument_list|,
name|dataInputStream
argument_list|,
name|uncompressedSizeWithoutHeader
argument_list|,
name|compression
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|BlockIOUtils
operator|.
name|readFullyWithHeapBuffer
argument_list|(
name|dataInputStream
argument_list|,
name|blockBufferWithoutHeader
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|byteBuffInputStream
operator|.
name|close
argument_list|()
expr_stmt|;
name|dataInputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HFileContext
name|getHFileContext
parameter_list|()
block|{
return|return
name|this
operator|.
name|fileContext
return|;
block|}
specifier|public
name|TagCompressionContext
name|getTagCompressionContext
parameter_list|()
block|{
return|return
name|tagCompressionContext
return|;
block|}
specifier|public
name|void
name|setTagCompressionContext
parameter_list|(
name|TagCompressionContext
name|tagCompressionContext
parameter_list|)
block|{
name|this
operator|.
name|tagCompressionContext
operator|=
name|tagCompressionContext
expr_stmt|;
block|}
block|}
end_class

end_unit

