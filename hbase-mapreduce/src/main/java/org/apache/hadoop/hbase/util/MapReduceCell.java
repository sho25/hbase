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
name|util
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
name|io
operator|.
name|OutputStream
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
name|hbase
operator|.
name|ByteBufferCell
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
name|ExtendedCell
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
name|PrivateCellUtil
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
comment|/**  * A wrapper for a cell to be used with mapreduce, as the output value class for mappers/reducers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MapReduceCell
extends|extends
name|ByteBufferCell
implements|implements
name|ExtendedCell
block|{
specifier|private
specifier|final
name|Cell
name|cell
decl_stmt|;
specifier|public
name|MapReduceCell
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|this
operator|.
name|cell
operator|=
name|cell
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRowArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getRowArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getRowOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getRowLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getRowLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFamilyArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getFamilyArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getFamilyOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getFamilyLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getFamilyLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getQualifierArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getQualifierArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getQualifierOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getQualifierLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getTimestamp
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getTypeByte
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getTypeByte
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceId
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getSequenceId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getValueArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getValueOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getValueLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getTagsArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getTagsOffset
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|cell
operator|.
name|getTagsLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getRowByteBuffer
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|this
operator|.
name|cell
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowPosition
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getRowPosition
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getFamilyByteBuffer
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getFamilyByteBuffer
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|this
operator|.
name|cell
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyPosition
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getFamilyPosition
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getQualifierByteBuffer
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getQualifierByteBuffer
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|this
operator|.
name|cell
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierPosition
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getQualifierPosition
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getValueByteBuffer
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|this
operator|.
name|cell
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValuePosition
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getValuePosition
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getTagsByteBuffer
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getTagsByteBuffer
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneTags
argument_list|(
name|this
operator|.
name|cell
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsPosition
parameter_list|()
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufferCell
operator|)
name|this
operator|.
name|cell
operator|)
operator|.
name|getTagsPosition
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
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
name|this
operator|.
name|cell
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSequenceId
parameter_list|(
name|long
name|seqId
parameter_list|)
throws|throws
name|IOException
block|{
name|PrivateCellUtil
operator|.
name|setSequenceId
argument_list|(
name|cell
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|PrivateCellUtil
operator|.
name|setTimestamp
argument_list|(
name|cell
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|byte
index|[]
name|ts
parameter_list|,
name|int
name|tsOffset
parameter_list|)
throws|throws
name|IOException
block|{
name|PrivateCellUtil
operator|.
name|setTimestamp
argument_list|(
name|cell
argument_list|,
name|ts
argument_list|,
name|tsOffset
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|PrivateCellUtil
operator|.
name|estimatedHeapSizeOf
argument_list|(
name|cell
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|write
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|boolean
name|withTags
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PrivateCellUtil
operator|.
name|writeCell
argument_list|(
name|cell
argument_list|,
name|out
argument_list|,
name|withTags
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedSize
parameter_list|(
name|boolean
name|withTags
parameter_list|)
block|{
return|return
name|PrivateCellUtil
operator|.
name|estimatedSerializedSizeOf
argument_list|(
name|cell
argument_list|)
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|PrivateCellUtil
operator|.
name|writeCellToBuffer
argument_list|(
name|cell
argument_list|,
name|buf
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCell
name|deepClone
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|ExtendedCell
operator|)
name|PrivateCellUtil
operator|.
name|deepClone
argument_list|(
name|cell
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

