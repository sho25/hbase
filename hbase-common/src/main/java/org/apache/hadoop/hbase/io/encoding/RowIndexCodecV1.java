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
name|CellComparator
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
name|CellComparatorImpl
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|nio
operator|.
name|SingleByteBuff
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
name|ByteBufferUtils
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
name|io
operator|.
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * Store cells following every row's start offset, so we can binary search to a row's cells.  *  * Format:  * flat cells  * integer: number of rows  * integer: row0's offset  * integer: row1's offset  * ....  * integer: dataSize  * */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RowIndexCodecV1
extends|extends
name|AbstractDataBlockEncoder
block|{
specifier|private
specifier|static
class|class
name|RowIndexEncodingState
extends|extends
name|EncodingState
block|{
name|RowIndexEncoderV1
name|encoder
init|=
literal|null
decl_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startBlockEncoding
parameter_list|(
name|HFileBlockEncodingContext
name|blkEncodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|blkEncodingCtx
operator|.
name|getClass
argument_list|()
operator|!=
name|HFileBlockDefaultEncodingContext
operator|.
name|class
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
literal|" as the "
operator|+
literal|"encoding context."
argument_list|)
throw|;
block|}
name|HFileBlockDefaultEncodingContext
name|encodingCtx
init|=
operator|(
name|HFileBlockDefaultEncodingContext
operator|)
name|blkEncodingCtx
decl_stmt|;
name|encodingCtx
operator|.
name|prepareEncoding
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|RowIndexEncoderV1
name|encoder
init|=
operator|new
name|RowIndexEncoderV1
argument_list|(
name|out
argument_list|,
name|encodingCtx
argument_list|)
decl_stmt|;
name|RowIndexEncodingState
name|state
init|=
operator|new
name|RowIndexEncodingState
argument_list|()
decl_stmt|;
name|state
operator|.
name|encoder
operator|=
name|encoder
expr_stmt|;
name|blkEncodingCtx
operator|.
name|setEncodingState
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
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
name|RowIndexEncodingState
name|state
init|=
operator|(
name|RowIndexEncodingState
operator|)
name|encodingCtx
operator|.
name|getEncodingState
argument_list|()
decl_stmt|;
name|RowIndexEncoderV1
name|encoder
init|=
name|state
operator|.
name|encoder
decl_stmt|;
return|return
name|encoder
operator|.
name|write
argument_list|(
name|cell
argument_list|)
return|;
block|}
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
parameter_list|)
throws|throws
name|IOException
block|{
name|RowIndexEncodingState
name|state
init|=
operator|(
name|RowIndexEncodingState
operator|)
name|encodingCtx
operator|.
name|getEncodingState
argument_list|()
decl_stmt|;
name|RowIndexEncoderV1
name|encoder
init|=
name|state
operator|.
name|encoder
decl_stmt|;
name|encoder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|postEncoding
argument_list|(
name|encodingCtx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|decodeKeyValues
parameter_list|(
name|DataInputStream
name|source
parameter_list|,
name|HFileBlockDecodingContext
name|decodingCtx
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|sourceAsBuffer
init|=
name|ByteBufferUtils
operator|.
name|drainInputStreamToBuffer
argument_list|(
name|source
argument_list|)
decl_stmt|;
comment|// waste
name|sourceAsBuffer
operator|.
name|mark
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|decodingCtx
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|sourceAsBuffer
operator|.
name|position
argument_list|(
name|sourceAsBuffer
operator|.
name|limit
argument_list|()
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|int
name|onDiskSize
init|=
name|sourceAsBuffer
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|sourceAsBuffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ByteBuffer
name|dup
init|=
name|sourceAsBuffer
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|dup
operator|.
name|position
argument_list|(
name|sourceAsBuffer
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|dup
operator|.
name|limit
argument_list|(
name|sourceAsBuffer
operator|.
name|position
argument_list|()
operator|+
name|onDiskSize
argument_list|)
expr_stmt|;
return|return
name|dup
operator|.
name|slice
argument_list|()
return|;
block|}
else|else
block|{
name|RowIndexSeekerV1
name|seeker
init|=
operator|new
name|RowIndexSeekerV1
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
argument_list|,
name|decodingCtx
argument_list|)
decl_stmt|;
name|seeker
operator|.
name|setCurrentBuffer
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|sourceAsBuffer
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
name|seeker
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|seeker
operator|.
name|next
argument_list|()
condition|)
block|{
name|kvs
operator|.
name|add
argument_list|(
name|seeker
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boolean
name|includesMvcc
init|=
name|decodingCtx
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isIncludesMvcc
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|kvs
control|)
block|{
name|KeyValue
name|currentCell
init|=
name|KeyValueUtil
operator|.
name|copyToNewKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|currentCell
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|currentCell
operator|.
name|getOffset
argument_list|()
argument_list|,
name|currentCell
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|includesMvcc
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
block|}
block|}
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|baos
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|baos
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getFirstKeyCellInBlock
parameter_list|(
name|ByteBuff
name|block
parameter_list|)
block|{
name|block
operator|.
name|mark
argument_list|()
expr_stmt|;
name|int
name|keyLength
init|=
name|block
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|block
operator|.
name|getInt
argument_list|()
expr_stmt|;
name|ByteBuffer
name|key
init|=
name|block
operator|.
name|asSubByteBuffer
argument_list|(
name|keyLength
argument_list|)
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|block
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|createFirstKeyCell
argument_list|(
name|key
argument_list|,
name|keyLength
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|EncodedSeeker
name|createSeeker
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|HFileBlockDecodingContext
name|decodingCtx
parameter_list|)
block|{
return|return
operator|new
name|RowIndexSeekerV1
argument_list|(
name|comparator
argument_list|,
name|decodingCtx
argument_list|)
return|;
block|}
block|}
end_class

end_unit

