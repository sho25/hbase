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
name|CellUtil
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
name|io
operator|.
name|WritableUtils
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
name|int
name|encode
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|HFileBlockEncodingContext
name|encodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|klength
init|=
name|KeyValueUtil
operator|.
name|keyLength
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|int
name|vlength
init|=
name|cell
operator|.
name|getValueLength
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|klength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|vlength
argument_list|)
expr_stmt|;
name|CellUtil
operator|.
name|writeFlatKey
argument_list|(
name|cell
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
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
name|vlength
argument_list|)
expr_stmt|;
name|int
name|encodedKvSize
init|=
name|klength
operator|+
name|vlength
operator|+
name|KeyValue
operator|.
name|KEYVALUE_INFRASTRUCTURE_SIZE
decl_stmt|;
comment|// Write the additional tag into the stream
if|if
condition|(
name|encodingCtx
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|int
name|tagsLength
init|=
name|cell
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeShort
argument_list|(
name|tagsLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|out
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
name|tagsLength
argument_list|)
expr_stmt|;
block|}
name|encodedKvSize
operator|+=
name|tagsLength
operator|+
name|KeyValue
operator|.
name|TAGS_LENGTH_SIZE
expr_stmt|;
block|}
if|if
condition|(
name|encodingCtx
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isIncludesMvcc
argument_list|()
condition|)
block|{
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|out
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
name|encodedKvSize
operator|+=
name|WritableUtils
operator|.
name|getVIntSize
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|encodedKvSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|useEncodedScanner
parameter_list|()
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
name|getDataBlockEncoding
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
name|newDataBlockEncodingContext
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
name|newDataBlockDecodingContext
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
annotation|@
name|Override
specifier|public
name|void
name|startBlockEncoding
parameter_list|(
name|HFileBlockEncodingContext
name|encodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|endBlockEncoding
parameter_list|(
name|HFileBlockEncodingContext
name|encodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|,
name|byte
index|[]
name|uncompressedBytesWithHeader
parameter_list|,
name|BlockType
name|blockType
parameter_list|)
throws|throws
name|IOException
block|{
name|encodingCtx
operator|.
name|postEncoding
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

