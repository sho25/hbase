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
name|hfile
operator|.
name|HFileContext
import|;
end_import

begin_comment
comment|/**  * Does not perform any kind of encoding/decoding.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NoOpDataBlockEncoder
implements|implements
name|HFileDataBlockEncoder
block|{
specifier|public
specifier|static
specifier|final
name|NoOpDataBlockEncoder
name|INSTANCE
init|=
operator|new
name|NoOpDataBlockEncoder
argument_list|()
decl_stmt|;
comment|/** Cannot be instantiated. Use {@link #INSTANCE} instead. */
specifier|private
name|NoOpDataBlockEncoder
parameter_list|()
block|{   }
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
name|ENCODED_DATA
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unexpected encoded block"
argument_list|)
throw|;
block|}
return|return
name|block
return|;
block|}
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
operator|!
operator|(
name|encodeCtx
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HFileBlockDefaultEncodingContext
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" only accepts "
operator|+
name|HFileBlockDefaultEncodingContext
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"."
argument_list|)
throw|;
block|}
name|HFileBlockDefaultEncodingContext
name|defaultContext
init|=
operator|(
name|HFileBlockDefaultEncodingContext
operator|)
name|encodeCtx
decl_stmt|;
name|defaultContext
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
return|return
literal|false
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
block|{   }
annotation|@
name|Override
specifier|public
name|DataBlockEncoding
name|getEncodingOnDisk
parameter_list|()
block|{
return|return
name|DataBlockEncoding
operator|.
name|NONE
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
name|DataBlockEncoding
operator|.
name|NONE
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
return|return
name|DataBlockEncoding
operator|.
name|NONE
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
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockEncodingContext
name|newOnDiskDataBlockEncodingContext
parameter_list|(
name|byte
index|[]
name|dummyHeader
parameter_list|,
name|HFileContext
name|meta
parameter_list|)
block|{
return|return
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
literal|null
argument_list|,
name|dummyHeader
argument_list|,
name|meta
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockDecodingContext
name|newOnDiskDataBlockDecodingContext
parameter_list|(
name|HFileContext
name|meta
parameter_list|)
block|{
return|return
operator|new
name|HFileBlockDefaultDecodingContext
argument_list|(
name|meta
argument_list|)
return|;
block|}
block|}
end_class

end_unit

