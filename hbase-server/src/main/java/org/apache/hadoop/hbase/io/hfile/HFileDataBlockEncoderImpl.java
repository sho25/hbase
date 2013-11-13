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
name|encoding
operator|.
name|HFileBlockDecodingContext
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
name|HFileBlockDefaultDecodingContext
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
name|HFileBlockDefaultEncodingContext
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
name|HFileBlockEncodingContext
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
name|util
operator|.
name|Bytes
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
name|encoding
decl_stmt|;
comment|/**    * Do data block encoding with specified options.    * @param encoding What kind of data block encoding will be used.    */
specifier|public
name|HFileDataBlockEncoderImpl
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
operator|!=
literal|null
condition|?
name|encoding
else|:
name|DataBlockEncoding
operator|.
name|NONE
expr_stmt|;
block|}
specifier|public
specifier|static
name|HFileDataBlockEncoder
name|createFromFileInfo
parameter_list|(
name|FileInfo
name|fileInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|DataBlockEncoding
name|encoding
init|=
name|DataBlockEncoding
operator|.
name|NONE
decl_stmt|;
name|byte
index|[]
name|dataBlockEncodingType
init|=
name|fileInfo
operator|.
name|get
argument_list|(
name|DATA_BLOCK_ENCODING
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataBlockEncodingType
operator|!=
literal|null
condition|)
block|{
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
try|try
block|{
name|encoding
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
block|}
if|if
condition|(
name|encoding
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
return|return
name|NoOpDataBlockEncoder
operator|.
name|INSTANCE
return|;
block|}
return|return
operator|new
name|HFileDataBlockEncoderImpl
argument_list|(
name|encoding
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|saveMetadata
parameter_list|(
name|HFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|DATA_BLOCK_ENCODING
argument_list|,
name|encoding
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
name|getDataBlockEncoding
parameter_list|()
block|{
return|return
name|encoding
return|;
block|}
comment|/**    * Precondition: a non-encoded buffer. Postcondition: on-disk encoding.    *    * The encoded results can be stored in {@link HFileBlockEncodingContext}.    *    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|beforeWriteToDisk
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|HFileBlockEncodingContext
name|encodeCtx
parameter_list|,
name|BlockType
name|blockType
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|encoding
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
comment|// there is no need to encode the block before writing it to disk
operator|(
operator|(
name|HFileBlockDefaultEncodingContext
operator|)
name|encodeCtx
operator|)
operator|.
name|compressAfterEncodingWithBlockType
argument_list|(
name|in
operator|.
name|array
argument_list|()
argument_list|,
name|blockType
argument_list|)
expr_stmt|;
return|return;
block|}
name|encodeBufferToHFileBlockBuffer
argument_list|(
name|in
argument_list|,
name|encoding
argument_list|,
name|encodeCtx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|useEncodedScanner
parameter_list|()
block|{
return|return
name|encoding
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
return|;
block|}
comment|/**    * Encode a block of key value pairs.    *    * @param in input data to encode    * @param algo encoding algorithm    * @param encodeCtx where will the output data be stored    */
specifier|private
name|void
name|encodeBufferToHFileBlockBuffer
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|DataBlockEncoding
name|algo
parameter_list|,
name|HFileBlockEncodingContext
name|encodeCtx
parameter_list|)
block|{
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
name|encoder
operator|.
name|encodeKeyValues
argument_list|(
name|in
argument_list|,
name|encodeCtx
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
literal|"'%s', it probably requested too much data, "
operator|+
literal|"exception message: %s."
argument_list|,
name|algo
operator|.
name|toString
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
literal|"(encoding="
operator|+
name|encoding
operator|+
literal|")"
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockEncodingContext
name|newDataBlockEncodingContext
parameter_list|(
name|byte
index|[]
name|dummyHeader
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|)
block|{
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
if|if
condition|(
name|encoder
operator|!=
literal|null
condition|)
block|{
return|return
name|encoder
operator|.
name|newDataBlockEncodingContext
argument_list|(
name|encoding
argument_list|,
name|dummyHeader
argument_list|,
name|fileContext
argument_list|)
return|;
block|}
return|return
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
literal|null
argument_list|,
name|dummyHeader
argument_list|,
name|fileContext
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockDecodingContext
name|newDataBlockDecodingContext
parameter_list|(
name|HFileContext
name|fileContext
parameter_list|)
block|{
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
if|if
condition|(
name|encoder
operator|!=
literal|null
condition|)
block|{
return|return
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|fileContext
argument_list|)
return|;
block|}
return|return
operator|new
name|HFileBlockDefaultDecodingContext
argument_list|(
name|fileContext
argument_list|)
return|;
block|}
block|}
end_class

end_unit

