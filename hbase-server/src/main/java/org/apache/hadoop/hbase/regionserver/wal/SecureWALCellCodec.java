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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|security
operator|.
name|SecureRandom
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
name|Cell
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
name|KeyValue
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
name|KeyValueUtil
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
name|codec
operator|.
name|KeyValueCodec
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
name|crypto
operator|.
name|Encryptor
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
name|StreamUtils
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

begin_comment
comment|/**  * A WALCellCodec that encrypts the WALedits.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SecureWALCellCodec
extends|extends
name|WALCellCodec
block|{
specifier|private
name|Encryptor
name|encryptor
decl_stmt|;
specifier|private
name|Decryptor
name|decryptor
decl_stmt|;
specifier|public
name|SecureWALCellCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|compression
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SecureWALCellCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Encryptor
name|encryptor
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|encryptor
operator|=
name|encryptor
expr_stmt|;
block|}
specifier|public
name|SecureWALCellCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Decryptor
name|decryptor
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|decryptor
operator|=
name|decryptor
expr_stmt|;
block|}
specifier|static
class|class
name|EncryptedKvDecoder
extends|extends
name|KeyValueCodec
operator|.
name|KeyValueDecoder
block|{
specifier|private
name|Decryptor
name|decryptor
decl_stmt|;
specifier|private
name|byte
index|[]
name|iv
decl_stmt|;
specifier|public
name|EncryptedKvDecoder
parameter_list|(
name|InputStream
name|in
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
specifier|public
name|EncryptedKvDecoder
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Decryptor
name|decryptor
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|decryptor
operator|=
name|decryptor
expr_stmt|;
if|if
condition|(
name|decryptor
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|iv
operator|=
operator|new
name|byte
index|[
name|decryptor
operator|.
name|getIvLength
argument_list|()
index|]
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Cell
name|parseCell
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|decryptor
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|parseCell
argument_list|()
return|;
block|}
name|int
name|ivLength
init|=
literal|0
decl_stmt|;
name|ivLength
operator|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|// TODO: An IV length of 0 could signify an unwrapped cell, when the
comment|// encoder supports that just read the remainder in directly
if|if
condition|(
name|ivLength
operator|!=
name|this
operator|.
name|iv
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Incorrect IV length: expected="
operator|+
name|iv
operator|.
name|length
operator|+
literal|" have="
operator|+
name|ivLength
argument_list|)
throw|;
block|}
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|this
operator|.
name|iv
argument_list|)
expr_stmt|;
name|int
name|codedLength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|byte
index|[]
name|codedBytes
init|=
operator|new
name|byte
index|[
name|codedLength
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|codedBytes
argument_list|)
expr_stmt|;
name|decryptor
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
name|decryptor
operator|.
name|reset
argument_list|()
expr_stmt|;
name|InputStream
name|cin
init|=
name|decryptor
operator|.
name|createDecryptionStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|codedBytes
argument_list|)
argument_list|)
decl_stmt|;
comment|// TODO: Add support for WAL compression
name|int
name|keylength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|cin
argument_list|)
decl_stmt|;
name|int
name|vlength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|cin
argument_list|)
decl_stmt|;
name|int
name|tagsLength
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|cin
argument_list|)
decl_stmt|;
name|int
name|length
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|tagsLength
operator|==
literal|0
condition|)
block|{
name|length
operator|=
name|KeyValue
operator|.
name|KEYVALUE_INFRASTRUCTURE_SIZE
operator|+
name|keylength
operator|+
name|vlength
expr_stmt|;
block|}
else|else
block|{
name|length
operator|=
name|KeyValue
operator|.
name|KEYVALUE_WITH_TAGS_INFRASTRUCTURE_SIZE
operator|+
name|keylength
operator|+
name|vlength
operator|+
name|tagsLength
expr_stmt|;
block|}
name|byte
index|[]
name|backingArray
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
name|keylength
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
comment|// Row
name|int
name|elemLen
init|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|cin
argument_list|)
decl_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putShort
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
operator|(
name|short
operator|)
name|elemLen
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|cin
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|elemLen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|elemLen
expr_stmt|;
comment|// Family
name|elemLen
operator|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|cin
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putByte
argument_list|(
name|backingArray
argument_list|,
name|pos
argument_list|,
operator|(
name|byte
operator|)
name|elemLen
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|cin
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|elemLen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|elemLen
expr_stmt|;
comment|// Qualifier
name|elemLen
operator|=
name|StreamUtils
operator|.
name|readRawVarint32
argument_list|(
name|cin
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|cin
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|elemLen
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|elemLen
expr_stmt|;
comment|// Remainder
name|IOUtils
operator|.
name|readFully
argument_list|(
name|cin
argument_list|,
name|backingArray
argument_list|,
name|pos
argument_list|,
name|length
operator|-
name|pos
argument_list|)
expr_stmt|;
return|return
operator|new
name|KeyValue
argument_list|(
name|backingArray
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|EncryptedKvEncoder
extends|extends
name|KeyValueCodec
operator|.
name|KeyValueEncoder
block|{
specifier|private
name|Encryptor
name|encryptor
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|byte
index|[]
argument_list|>
name|iv
init|=
operator|new
name|ThreadLocal
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|byte
index|[]
name|initialValue
parameter_list|()
block|{
name|byte
index|[]
name|iv
init|=
operator|new
name|byte
index|[
name|encryptor
operator|.
name|getIvLength
argument_list|()
index|]
decl_stmt|;
operator|new
name|SecureRandom
argument_list|()
operator|.
name|nextBytes
argument_list|(
name|iv
argument_list|)
expr_stmt|;
return|return
name|iv
return|;
block|}
block|}
decl_stmt|;
specifier|protected
name|byte
index|[]
name|nextIv
parameter_list|()
block|{
name|byte
index|[]
name|b
init|=
name|iv
operator|.
name|get
argument_list|()
decl_stmt|,
name|ret
init|=
operator|new
name|byte
index|[
name|b
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|protected
name|void
name|incrementIv
parameter_list|(
name|int
name|v
parameter_list|)
block|{
name|Encryption
operator|.
name|incrementIv
argument_list|(
name|iv
operator|.
name|get
argument_list|()
argument_list|,
literal|1
operator|+
operator|(
name|v
operator|/
name|encryptor
operator|.
name|getBlockSize
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|EncryptedKvEncoder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
block|{
name|super
argument_list|(
name|os
argument_list|)
expr_stmt|;
block|}
specifier|public
name|EncryptedKvEncoder
parameter_list|(
name|OutputStream
name|os
parameter_list|,
name|Encryptor
name|encryptor
parameter_list|)
block|{
name|super
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|this
operator|.
name|encryptor
operator|=
name|encryptor
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|encryptor
operator|==
literal|null
condition|)
block|{
name|super
operator|.
name|write
argument_list|(
name|cell
argument_list|)
expr_stmt|;
return|return;
block|}
name|byte
index|[]
name|iv
init|=
name|nextIv
argument_list|()
decl_stmt|;
name|encryptor
operator|.
name|setIv
argument_list|(
name|iv
argument_list|)
expr_stmt|;
name|encryptor
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// TODO: Check if this is a cell for an encrypted CF. If not, we can
comment|// write a 0 here to signal an unwrapped cell and just dump the KV bytes
comment|// afterward
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|iv
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|iv
argument_list|)
expr_stmt|;
comment|// TODO: Add support for WAL compression
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|OutputStream
name|cout
init|=
name|encryptor
operator|.
name|createEncryptionStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|int
name|tlen
init|=
name|cell
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
comment|// Write the KeyValue infrastructure as VInts.
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|cout
argument_list|,
name|KeyValueUtil
operator|.
name|keyLength
argument_list|(
name|cell
argument_list|)
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|cout
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// To support tags
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|cout
argument_list|,
name|tlen
argument_list|)
expr_stmt|;
comment|// Write row, qualifier, and family
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|cout
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|cout
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|cout
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// Write the rest ie. ts, type, value and tags parts
name|StreamUtils
operator|.
name|writeLong
argument_list|(
name|cout
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
name|cout
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tlen
operator|>
literal|0
condition|)
block|{
name|cout
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tlen
argument_list|)
expr_stmt|;
block|}
name|cout
operator|.
name|close
argument_list|()
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|baos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|baos
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// Increment IV given the final payload length
name|incrementIv
argument_list|(
name|baos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Decoder
name|getDecoder
parameter_list|(
name|InputStream
name|is
parameter_list|)
block|{
return|return
operator|new
name|EncryptedKvDecoder
argument_list|(
name|is
argument_list|,
name|decryptor
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Encoder
name|getEncoder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
block|{
return|return
operator|new
name|EncryptedKvEncoder
argument_list|(
name|os
argument_list|,
name|encryptor
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|WALCellCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Encryptor
name|encryptor
parameter_list|)
block|{
return|return
operator|new
name|SecureWALCellCodec
argument_list|(
name|conf
argument_list|,
name|encryptor
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|WALCellCodec
name|getCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Decryptor
name|decryptor
parameter_list|)
block|{
return|return
operator|new
name|SecureWALCellCodec
argument_list|(
name|conf
argument_list|,
name|decryptor
argument_list|)
return|;
block|}
block|}
end_class

end_unit

