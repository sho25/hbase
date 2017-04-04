begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|exceptions
operator|.
name|DeserializationException
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * A filter that will only return the key component of each KV (the value will  * be rewritten as empty).  *<p>  * This filter can be used to grab all of the keys without having to also grab  * the values.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|KeyOnlyFilter
extends|extends
name|FilterBase
block|{
name|boolean
name|lenAsVal
decl_stmt|;
specifier|public
name|KeyOnlyFilter
parameter_list|()
block|{
name|this
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|KeyOnlyFilter
parameter_list|(
name|boolean
name|lenAsVal
parameter_list|)
block|{
name|this
operator|.
name|lenAsVal
operator|=
name|lenAsVal
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Impl in FilterBase might do unnecessary copy for Off heap backed Cells.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|transformCell
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|createKeyOnlyCell
argument_list|(
name|cell
argument_list|)
return|;
block|}
specifier|private
name|Cell
name|createKeyOnlyCell
parameter_list|(
name|Cell
name|c
parameter_list|)
block|{
if|if
condition|(
name|c
operator|instanceof
name|ByteBufferCell
condition|)
block|{
return|return
operator|new
name|KeyOnlyByteBufferCell
argument_list|(
operator|(
name|ByteBufferCell
operator|)
name|c
argument_list|,
name|lenAsVal
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|KeyOnlyCell
argument_list|(
name|c
argument_list|,
name|lenAsVal
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|ignored
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|public
specifier|static
name|Filter
name|createFilterFromArguments
parameter_list|(
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|filterArguments
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|(
name|filterArguments
operator|.
name|isEmpty
argument_list|()
operator|||
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
argument_list|,
literal|"Expected: 0 or 1 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|KeyOnlyFilter
name|filter
init|=
operator|new
name|KeyOnlyFilter
argument_list|()
decl_stmt|;
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|filter
operator|.
name|lenAsVal
operator|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|KeyOnlyFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|KeyOnlyFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setLenAsVal
argument_list|(
name|this
operator|.
name|lenAsVal
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link KeyOnlyFilter} instance    * @return An instance of {@link KeyOnlyFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|KeyOnlyFilter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|FilterProtos
operator|.
name|KeyOnlyFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|KeyOnlyFilter
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|KeyOnlyFilter
argument_list|(
name|proto
operator|.
name|getLenAsVal
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|KeyOnlyFilter
operator|)
condition|)
return|return
literal|false
return|;
name|KeyOnlyFilter
name|other
init|=
operator|(
name|KeyOnlyFilter
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|lenAsVal
operator|==
name|other
operator|.
name|lenAsVal
return|;
block|}
specifier|static
class|class
name|KeyOnlyCell
implements|implements
name|Cell
block|{
specifier|private
name|Cell
name|cell
decl_stmt|;
specifier|private
name|boolean
name|lenAsVal
decl_stmt|;
specifier|public
name|KeyOnlyCell
parameter_list|(
name|Cell
name|c
parameter_list|,
name|boolean
name|lenAsVal
parameter_list|)
block|{
name|this
operator|.
name|cell
operator|=
name|c
expr_stmt|;
name|this
operator|.
name|lenAsVal
operator|=
name|lenAsVal
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
literal|0
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
if|if
condition|(
name|lenAsVal
condition|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
if|if
condition|(
name|lenAsVal
condition|)
block|{
return|return
name|Bytes
operator|.
name|SIZEOF_INT
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
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
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
literal|0
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
literal|0
return|;
block|}
block|}
specifier|static
class|class
name|KeyOnlyByteBufferCell
extends|extends
name|ByteBufferCell
block|{
specifier|private
name|ByteBufferCell
name|cell
decl_stmt|;
specifier|private
name|boolean
name|lenAsVal
decl_stmt|;
specifier|public
name|KeyOnlyByteBufferCell
parameter_list|(
name|ByteBufferCell
name|c
parameter_list|,
name|boolean
name|lenAsVal
parameter_list|)
block|{
name|this
operator|.
name|cell
operator|=
name|c
expr_stmt|;
name|this
operator|.
name|lenAsVal
operator|=
name|lenAsVal
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
literal|0
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
if|if
condition|(
name|lenAsVal
condition|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
if|if
condition|(
name|lenAsVal
condition|)
block|{
return|return
name|Bytes
operator|.
name|SIZEOF_INT
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
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
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
literal|0
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
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getRowByteBuffer
parameter_list|()
block|{
return|return
name|cell
operator|.
name|getRowByteBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowPosition
parameter_list|()
block|{
return|return
name|cell
operator|.
name|getRowPosition
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getFamilyByteBuffer
parameter_list|()
block|{
return|return
name|cell
operator|.
name|getFamilyByteBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyPosition
parameter_list|()
block|{
return|return
name|cell
operator|.
name|getFamilyPosition
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getQualifierByteBuffer
parameter_list|()
block|{
return|return
name|cell
operator|.
name|getQualifierByteBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierPosition
parameter_list|()
block|{
return|return
name|cell
operator|.
name|getQualifierPosition
argument_list|()
return|;
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
name|lenAsVal
condition|)
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_BUFFER
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
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getTagsByteBuffer
parameter_list|()
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_BUFFER
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsPosition
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

