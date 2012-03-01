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
name|hfile
package|;
end_package

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
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoder
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
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|FileInfo
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
name|regionserver
operator|.
name|StoreFile
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
name|Pair
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Do different kinds of data block encoding according to column family  * options.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileDataBlockEncoderImpl
implements|implements
name|HFileDataBlockEncoder
block|{
specifier|private
specifier|final
name|DataBlockEncoding
name|onDisk
decl_stmt|;
specifier|private
specifier|final
name|DataBlockEncoding
name|inCache
decl_stmt|;
specifier|public
name|HFileDataBlockEncoderImpl
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|)
block|{
name|this
argument_list|(
name|encoding
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
block|}
comment|/**    * Do data block encoding with specified options.    * @param onDisk What kind of data block encoding will be used before writing    *          HFileBlock to disk. This must be either the same as inCache or    *          {@link DataBlockEncoding#NONE}.    * @param inCache What kind of data block encoding will be used in block    *          cache.    */
specifier|public
name|HFileDataBlockEncoderImpl
parameter_list|(
name|DataBlockEncoding
name|onDisk
parameter_list|,
name|DataBlockEncoding
name|inCache
parameter_list|)
block|{
name|this
operator|.
name|onDisk
operator|=
name|onDisk
operator|!=
literal|null
condition|?
name|onDisk
else|:
name|DataBlockEncoding
operator|.
name|NONE
expr_stmt|;
name|this
operator|.
name|inCache
operator|=
name|inCache
operator|!=
literal|null
condition|?
name|inCache
else|:
name|DataBlockEncoding
operator|.
name|NONE
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|onDisk
operator|==
name|DataBlockEncoding
operator|.
name|NONE
operator|||
name|onDisk
operator|==
name|inCache
argument_list|,
literal|"on-disk encoding ("
operator|+
name|onDisk
operator|+
literal|") must be "
operator|+
literal|"either the same as in-cache encoding ("
operator|+
name|inCache
operator|+
literal|") or "
operator|+
name|DataBlockEncoding
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|HFileDataBlockEncoder
name|createFromFileInfo
parameter_list|(
name|FileInfo
name|fileInfo
parameter_list|,
name|DataBlockEncoding
name|preferredEncodingInCache
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|dataBlockEncodingType
init|=
name|fileInfo
operator|.
name|get
argument_list|(
name|StoreFile
operator|.
name|DATA_BLOCK_ENCODING
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataBlockEncodingType
operator|==
literal|null
condition|)
block|{
return|return
name|NoOpDataBlockEncoder
operator|.
name|INSTANCE
return|;
block|}
name|String
name|dataBlockEncodingStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|dataBlockEncodingType
argument_list|)
decl_stmt|;
name|DataBlockEncoding
name|onDisk
decl_stmt|;
try|try
block|{
name|onDisk
operator|=
name|DataBlockEncoding
operator|.
name|valueOf
argument_list|(
name|dataBlockEncodingStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid data block encoding type in file info: "
operator|+
name|dataBlockEncodingStr
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|DataBlockEncoding
name|inCache
decl_stmt|;
if|if
condition|(
name|onDisk
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
comment|// This is an "in-cache-only" encoding or fully-unencoded scenario.
comment|// Either way, we use the given encoding (possibly NONE) specified by
comment|// the column family in cache.
name|inCache
operator|=
name|preferredEncodingInCache
expr_stmt|;
block|}
else|else
block|{
comment|// Leave blocks in cache encoded the same way as they are on disk.
comment|// If we switch encoding type for the CF or the in-cache-only encoding
comment|// flag, old files will keep their encoding both on disk and in cache,
comment|// but new files will be generated with the new encoding.
name|inCache
operator|=
name|onDisk
expr_stmt|;
block|}
return|return
operator|new
name|HFileDataBlockEncoderImpl
argument_list|(
name|onDisk
argument_list|,
name|inCache
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|saveMetadata
parameter_list|(
name|StoreFile
operator|.
name|Writer
name|storeFileWriter
parameter_list|)
throws|throws
name|IOException
block|{
name|storeFileWriter
operator|.
name|appendFileInfo
argument_list|(
name|StoreFile
operator|.
name|DATA_BLOCK_ENCODING
argument_list|,
name|onDisk
operator|.
name|getNameInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DataBlockEncoding
name|getEncodingOnDisk
parameter_list|()
block|{
return|return
name|onDisk
return|;
block|}
annotation|@
name|Override
specifier|public
name|DataBlockEncoding
name|getEncodingInCache
parameter_list|()
block|{
return|return
name|inCache
return|;
block|}
annotation|@
name|Override
specifier|public
name|DataBlockEncoding
name|getEffectiveEncodingInCache
parameter_list|(
name|boolean
name|isCompaction
parameter_list|)
block|{
if|if
condition|(
operator|!
name|useEncodedScanner
argument_list|(
name|isCompaction
argument_list|)
condition|)
block|{
return|return
name|DataBlockEncoding
operator|.
name|NONE
return|;
block|}
return|return
name|inCache
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlock
name|diskToCacheFormat
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|boolean
name|isCompaction
parameter_list|)
block|{
if|if
condition|(
name|block
operator|.
name|getBlockType
argument_list|()
operator|==
name|BlockType
operator|.
name|DATA
condition|)
block|{
if|if
condition|(
operator|!
name|useEncodedScanner
argument_list|(
name|isCompaction
argument_list|)
condition|)
block|{
comment|// Unencoded block, and we don't want to encode in cache.
return|return
name|block
return|;
block|}
comment|// Encode the unencoded block with the in-cache encoding.
return|return
name|encodeDataBlock
argument_list|(
name|block
argument_list|,
name|inCache
argument_list|,
name|block
operator|.
name|doesIncludeMemstoreTS
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|block
operator|.
name|getBlockType
argument_list|()
operator|==
name|BlockType
operator|.
name|ENCODED_DATA
condition|)
block|{
if|if
condition|(
name|block
operator|.
name|getDataBlockEncodingId
argument_list|()
operator|==
name|onDisk
operator|.
name|getId
argument_list|()
condition|)
block|{
comment|// The block is already in the desired in-cache encoding.
return|return
name|block
return|;
block|}
comment|// We don't want to re-encode a block in a different encoding. The HFile
comment|// reader should have been instantiated in such a way that we would not
comment|// have to do this.
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Expected on-disk data block encoding "
operator|+
name|onDisk
operator|+
literal|", got "
operator|+
name|block
operator|.
name|getDataBlockEncoding
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|block
return|;
block|}
comment|/**    * Precondition: a non-encoded buffer.    * Postcondition: on-disk encoding.    */
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|ByteBuffer
argument_list|,
name|BlockType
argument_list|>
name|beforeWriteToDisk
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
if|if
condition|(
name|onDisk
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
comment|// there is no need to encode the block before writing it to disk
return|return
operator|new
name|Pair
argument_list|<
name|ByteBuffer
argument_list|,
name|BlockType
argument_list|>
argument_list|(
name|in
argument_list|,
name|BlockType
operator|.
name|DATA
argument_list|)
return|;
block|}
name|ByteBuffer
name|encodedBuffer
init|=
name|encodeBufferToHFileBlockBuffer
argument_list|(
name|in
argument_list|,
name|onDisk
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|ByteBuffer
argument_list|,
name|BlockType
argument_list|>
argument_list|(
name|encodedBuffer
argument_list|,
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|useEncodedScanner
parameter_list|(
name|boolean
name|isCompaction
parameter_list|)
block|{
if|if
condition|(
name|isCompaction
operator|&&
name|onDisk
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|inCache
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
return|;
block|}
specifier|private
name|ByteBuffer
name|encodeBufferToHFileBlockBuffer
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|DataBlockEncoding
name|algo
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
name|ByteArrayOutputStream
name|encodedStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dataOut
init|=
operator|new
name|DataOutputStream
argument_list|(
name|encodedStream
argument_list|)
decl_stmt|;
name|DataBlockEncoder
name|encoder
init|=
name|algo
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
try|try
block|{
name|encodedStream
operator|.
name|write
argument_list|(
name|HFileBlock
operator|.
name|DUMMY_HEADER
argument_list|)
expr_stmt|;
name|algo
operator|.
name|writeIdInBytes
argument_list|(
name|dataOut
argument_list|)
expr_stmt|;
name|encoder
operator|.
name|compressKeyValues
argument_list|(
name|dataOut
argument_list|,
name|in
argument_list|,
name|includesMemstoreTS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Bug in data block encoder "
operator|+
literal|"'%s', it probably requested too much data"
argument_list|,
name|algo
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|encodedStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|HFileBlock
name|encodeDataBlock
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|DataBlockEncoding
name|algo
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
name|ByteBuffer
name|compressedBuffer
init|=
name|encodeBufferToHFileBlockBuffer
argument_list|(
name|block
operator|.
name|getBufferWithoutHeader
argument_list|()
argument_list|,
name|algo
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|int
name|sizeWithoutHeader
init|=
name|compressedBuffer
operator|.
name|limit
argument_list|()
operator|-
name|HFileBlock
operator|.
name|HEADER_SIZE
decl_stmt|;
name|HFileBlock
name|encodedBlock
init|=
operator|new
name|HFileBlock
argument_list|(
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|,
name|block
operator|.
name|getOnDiskSizeWithoutHeader
argument_list|()
argument_list|,
name|sizeWithoutHeader
argument_list|,
name|block
operator|.
name|getPrevBlockOffset
argument_list|()
argument_list|,
name|compressedBuffer
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
name|block
operator|.
name|getOffset
argument_list|()
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|block
operator|.
name|passSchemaMetricsTo
argument_list|(
name|encodedBlock
argument_list|)
expr_stmt|;
return|return
name|encodedBlock
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"(onDisk="
operator|+
name|onDisk
operator|+
literal|", inCache="
operator|+
name|inCache
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

