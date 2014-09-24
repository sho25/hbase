begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hfile
package|;
end_package

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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|ChecksumType
import|;
end_import

begin_comment
comment|/**  * A builder that helps in building up the HFileContext   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileContextBuilder
block|{
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_BYTES_PER_CHECKSUM
init|=
literal|16
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ChecksumType
name|DEFAULT_CHECKSUM_TYPE
init|=
name|ChecksumType
operator|.
name|CRC32
decl_stmt|;
comment|/** Whether checksum is enabled or not **/
specifier|private
name|boolean
name|usesHBaseChecksum
init|=
literal|true
decl_stmt|;
comment|/** Whether mvcc is to be included in the Read/Write **/
specifier|private
name|boolean
name|includesMvcc
init|=
literal|true
decl_stmt|;
comment|/** Whether tags are to be included in the Read/Write **/
specifier|private
name|boolean
name|includesTags
decl_stmt|;
comment|/** Compression algorithm used **/
specifier|private
name|Algorithm
name|compression
init|=
name|Algorithm
operator|.
name|NONE
decl_stmt|;
comment|/** Whether tags to be compressed or not **/
specifier|private
name|boolean
name|compressTags
init|=
literal|false
decl_stmt|;
comment|/** the checksum type **/
specifier|private
name|ChecksumType
name|checksumType
init|=
name|DEFAULT_CHECKSUM_TYPE
decl_stmt|;
comment|/** the number of bytes per checksum value **/
specifier|private
name|int
name|bytesPerChecksum
init|=
name|DEFAULT_BYTES_PER_CHECKSUM
decl_stmt|;
comment|/** Number of uncompressed bytes we allow per block. */
specifier|private
name|int
name|blocksize
init|=
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
decl_stmt|;
specifier|private
name|DataBlockEncoding
name|encoding
init|=
name|DataBlockEncoding
operator|.
name|NONE
decl_stmt|;
comment|/** Crypto context */
specifier|private
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|Encryption
operator|.
name|Context
operator|.
name|NONE
decl_stmt|;
specifier|public
name|HFileContextBuilder
name|withHBaseCheckSum
parameter_list|(
name|boolean
name|useHBaseCheckSum
parameter_list|)
block|{
name|this
operator|.
name|usesHBaseChecksum
operator|=
name|useHBaseCheckSum
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withIncludesMvcc
parameter_list|(
name|boolean
name|includesMvcc
parameter_list|)
block|{
name|this
operator|.
name|includesMvcc
operator|=
name|includesMvcc
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withIncludesTags
parameter_list|(
name|boolean
name|includesTags
parameter_list|)
block|{
name|this
operator|.
name|includesTags
operator|=
name|includesTags
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withCompression
parameter_list|(
name|Algorithm
name|compression
parameter_list|)
block|{
name|this
operator|.
name|compression
operator|=
name|compression
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withCompressTags
parameter_list|(
name|boolean
name|compressTags
parameter_list|)
block|{
name|this
operator|.
name|compressTags
operator|=
name|compressTags
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withChecksumType
parameter_list|(
name|ChecksumType
name|checkSumType
parameter_list|)
block|{
name|this
operator|.
name|checksumType
operator|=
name|checkSumType
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withBytesPerCheckSum
parameter_list|(
name|int
name|bytesPerChecksum
parameter_list|)
block|{
name|this
operator|.
name|bytesPerChecksum
operator|=
name|bytesPerChecksum
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withBlockSize
parameter_list|(
name|int
name|blockSize
parameter_list|)
block|{
name|this
operator|.
name|blocksize
operator|=
name|blockSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withDataBlockEncoding
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|)
block|{
name|this
operator|.
name|encoding
operator|=
name|encoding
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContextBuilder
name|withEncryptionContext
parameter_list|(
name|Encryption
operator|.
name|Context
name|cryptoContext
parameter_list|)
block|{
name|this
operator|.
name|cryptoContext
operator|=
name|cryptoContext
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileContext
name|build
parameter_list|()
block|{
return|return
operator|new
name|HFileContext
argument_list|(
name|usesHBaseChecksum
argument_list|,
name|includesMvcc
argument_list|,
name|includesTags
argument_list|,
name|compression
argument_list|,
name|compressTags
argument_list|,
name|checksumType
argument_list|,
name|bytesPerChecksum
argument_list|,
name|blocksize
argument_list|,
name|encoding
argument_list|,
name|cryptoContext
argument_list|)
return|;
block|}
block|}
end_class

end_unit

