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
name|HeapSize
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
name|Bytes
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
name|ClassSize
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
comment|/**  * This carries the information on some of the meta data about the HFile. This  * meta data is used across the HFileWriter/Readers and the HFileBlocks.  * This helps to add new information to the HFile.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileContext
implements|implements
name|HeapSize
implements|,
name|Cloneable
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
comment|/** Whether checksum is enabled or not**/
specifier|private
name|boolean
name|usesHBaseChecksum
init|=
literal|true
decl_stmt|;
comment|/** Whether mvcc is to be included in the Read/Write**/
specifier|private
name|boolean
name|includesMvcc
init|=
literal|true
decl_stmt|;
comment|/**Whether tags are to be included in the Read/Write**/
specifier|private
name|boolean
name|includesTags
decl_stmt|;
comment|/**Compression algorithm used**/
specifier|private
name|Compression
operator|.
name|Algorithm
name|compressAlgo
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
decl_stmt|;
comment|/** Whether tags to be compressed or not**/
specifier|private
name|boolean
name|compressTags
decl_stmt|;
comment|/** the checksum type **/
specifier|private
name|ChecksumType
name|checksumType
init|=
name|ChecksumType
operator|.
name|getDefaultChecksumType
argument_list|()
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
comment|/** Encryption algorithm and key used */
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
specifier|private
name|long
name|fileCreateTime
decl_stmt|;
specifier|private
name|String
name|hfileName
decl_stmt|;
comment|//Empty constructor.  Go with setters
specifier|public
name|HFileContext
parameter_list|()
block|{   }
comment|/**    * Copy constructor    * @param context    */
specifier|public
name|HFileContext
parameter_list|(
name|HFileContext
name|context
parameter_list|)
block|{
name|this
operator|.
name|usesHBaseChecksum
operator|=
name|context
operator|.
name|usesHBaseChecksum
expr_stmt|;
name|this
operator|.
name|includesMvcc
operator|=
name|context
operator|.
name|includesMvcc
expr_stmt|;
name|this
operator|.
name|includesTags
operator|=
name|context
operator|.
name|includesTags
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|=
name|context
operator|.
name|compressAlgo
expr_stmt|;
name|this
operator|.
name|compressTags
operator|=
name|context
operator|.
name|compressTags
expr_stmt|;
name|this
operator|.
name|checksumType
operator|=
name|context
operator|.
name|checksumType
expr_stmt|;
name|this
operator|.
name|bytesPerChecksum
operator|=
name|context
operator|.
name|bytesPerChecksum
expr_stmt|;
name|this
operator|.
name|blocksize
operator|=
name|context
operator|.
name|blocksize
expr_stmt|;
name|this
operator|.
name|encoding
operator|=
name|context
operator|.
name|encoding
expr_stmt|;
name|this
operator|.
name|cryptoContext
operator|=
name|context
operator|.
name|cryptoContext
expr_stmt|;
name|this
operator|.
name|fileCreateTime
operator|=
name|context
operator|.
name|fileCreateTime
expr_stmt|;
name|this
operator|.
name|hfileName
operator|=
name|context
operator|.
name|hfileName
expr_stmt|;
block|}
name|HFileContext
parameter_list|(
name|boolean
name|useHBaseChecksum
parameter_list|,
name|boolean
name|includesMvcc
parameter_list|,
name|boolean
name|includesTags
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compressAlgo
parameter_list|,
name|boolean
name|compressTags
parameter_list|,
name|ChecksumType
name|checksumType
parameter_list|,
name|int
name|bytesPerChecksum
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|,
name|Encryption
operator|.
name|Context
name|cryptoContext
parameter_list|,
name|long
name|fileCreateTime
parameter_list|,
name|String
name|hfileName
parameter_list|)
block|{
name|this
operator|.
name|usesHBaseChecksum
operator|=
name|useHBaseChecksum
expr_stmt|;
name|this
operator|.
name|includesMvcc
operator|=
name|includesMvcc
expr_stmt|;
name|this
operator|.
name|includesTags
operator|=
name|includesTags
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|=
name|compressAlgo
expr_stmt|;
name|this
operator|.
name|compressTags
operator|=
name|compressTags
expr_stmt|;
name|this
operator|.
name|checksumType
operator|=
name|checksumType
expr_stmt|;
name|this
operator|.
name|bytesPerChecksum
operator|=
name|bytesPerChecksum
expr_stmt|;
name|this
operator|.
name|blocksize
operator|=
name|blockSize
expr_stmt|;
if|if
condition|(
name|encoding
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|encoding
operator|=
name|encoding
expr_stmt|;
block|}
name|this
operator|.
name|cryptoContext
operator|=
name|cryptoContext
expr_stmt|;
name|this
operator|.
name|fileCreateTime
operator|=
name|fileCreateTime
expr_stmt|;
name|this
operator|.
name|hfileName
operator|=
name|hfileName
expr_stmt|;
block|}
comment|/**    * @return true when on-disk blocks from this file are compressed, and/or encrypted;    * false otherwise.    */
specifier|public
name|boolean
name|isCompressedOrEncrypted
parameter_list|()
block|{
name|Compression
operator|.
name|Algorithm
name|compressAlgo
init|=
name|getCompression
argument_list|()
decl_stmt|;
name|boolean
name|compressed
init|=
name|compressAlgo
operator|!=
literal|null
operator|&&
name|compressAlgo
operator|!=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
decl_stmt|;
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|getEncryptionContext
argument_list|()
decl_stmt|;
name|boolean
name|encrypted
init|=
name|cryptoContext
operator|!=
literal|null
operator|&&
name|cryptoContext
operator|!=
name|Encryption
operator|.
name|Context
operator|.
name|NONE
decl_stmt|;
return|return
name|compressed
operator|||
name|encrypted
return|;
block|}
specifier|public
name|Compression
operator|.
name|Algorithm
name|getCompression
parameter_list|()
block|{
return|return
name|compressAlgo
return|;
block|}
specifier|public
name|boolean
name|isUseHBaseChecksum
parameter_list|()
block|{
return|return
name|usesHBaseChecksum
return|;
block|}
specifier|public
name|boolean
name|isIncludesMvcc
parameter_list|()
block|{
return|return
name|includesMvcc
return|;
block|}
specifier|public
name|void
name|setIncludesMvcc
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
block|}
specifier|public
name|boolean
name|isIncludesTags
parameter_list|()
block|{
return|return
name|includesTags
return|;
block|}
specifier|public
name|void
name|setIncludesTags
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
block|}
specifier|public
name|void
name|setFileCreateTime
parameter_list|(
name|long
name|fileCreateTime
parameter_list|)
block|{
name|this
operator|.
name|fileCreateTime
operator|=
name|fileCreateTime
expr_stmt|;
block|}
specifier|public
name|boolean
name|isCompressTags
parameter_list|()
block|{
return|return
name|compressTags
return|;
block|}
specifier|public
name|void
name|setCompressTags
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
block|}
specifier|public
name|ChecksumType
name|getChecksumType
parameter_list|()
block|{
return|return
name|checksumType
return|;
block|}
specifier|public
name|int
name|getBytesPerChecksum
parameter_list|()
block|{
return|return
name|bytesPerChecksum
return|;
block|}
specifier|public
name|int
name|getBlocksize
parameter_list|()
block|{
return|return
name|blocksize
return|;
block|}
specifier|public
name|long
name|getFileCreateTime
parameter_list|()
block|{
return|return
name|fileCreateTime
return|;
block|}
specifier|public
name|DataBlockEncoding
name|getDataBlockEncoding
parameter_list|()
block|{
return|return
name|encoding
return|;
block|}
specifier|public
name|Encryption
operator|.
name|Context
name|getEncryptionContext
parameter_list|()
block|{
return|return
name|cryptoContext
return|;
block|}
specifier|public
name|void
name|setEncryptionContext
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
block|}
specifier|public
name|String
name|getHFileName
parameter_list|()
block|{
return|return
name|this
operator|.
name|hfileName
return|;
block|}
comment|/**    * HeapSize implementation    * NOTE : The heapsize should be altered as and when new state variable are added    * @return heap size of the HFileContext    */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
name|long
name|size
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
comment|// Algorithm reference, encodingon, checksumtype, Encryption.Context reference
literal|5
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
comment|// usesHBaseChecksum, includesMvcc, includesTags and compressTags
literal|4
operator|*
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|hfileName
operator|!=
literal|null
condition|)
block|{
name|size
operator|+=
name|ClassSize
operator|.
name|STRING
operator|+
name|this
operator|.
name|hfileName
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileContext
name|clone
parameter_list|()
block|{
try|try
block|{
return|return
call|(
name|HFileContext
call|)
argument_list|(
name|super
operator|.
name|clone
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|CloneNotSupportedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
comment|// Won't happen
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"usesHBaseChecksum="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|usesHBaseChecksum
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", checksumType="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|checksumType
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", bytesPerChecksum="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|bytesPerChecksum
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", blocksize="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|blocksize
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", encoding="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|encoding
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", includesMvcc="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|includesMvcc
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", includesTags="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|includesTags
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", compressAlgo="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|compressAlgo
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", compressTags="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|compressTags
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", cryptoContext=["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|cryptoContext
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
if|if
condition|(
name|hfileName
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", name="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|hfileName
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

