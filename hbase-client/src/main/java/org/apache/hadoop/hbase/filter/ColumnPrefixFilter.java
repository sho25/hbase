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
name|ByteBufferExtendedCell
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
name|hbase
operator|.
name|thirdparty
operator|.
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
name|hbase
operator|.
name|thirdparty
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
import|;
end_import

begin_comment
comment|/**  * This filter is used for selecting only those keys with columns that matches  * a particular prefix. For example, if prefix is 'an', it will pass keys with  * columns like 'and', 'anti' but not keys with columns like 'ball', 'act'.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|ColumnPrefixFilter
extends|extends
name|FilterBase
block|{
specifier|protected
name|byte
index|[]
name|prefix
init|=
literal|null
decl_stmt|;
specifier|public
name|ColumnPrefixFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|prefix
parameter_list|)
block|{
name|this
operator|.
name|prefix
operator|=
name|prefix
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getPrefix
parameter_list|()
block|{
return|return
name|prefix
return|;
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
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|prefix
operator|==
literal|null
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
else|else
block|{
return|return
name|filterColumn
argument_list|(
name|cell
argument_list|)
return|;
block|}
block|}
specifier|public
name|ReturnCode
name|filterColumn
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|int
name|qualifierLength
init|=
name|cell
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|qualifierLength
operator|<
name|prefix
operator|.
name|length
condition|)
block|{
name|int
name|cmp
init|=
name|compareQualifierPart
argument_list|(
name|cell
argument_list|,
name|qualifierLength
argument_list|,
name|this
operator|.
name|prefix
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<=
literal|0
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
else|else
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
block|}
else|else
block|{
name|int
name|cmp
init|=
name|compareQualifierPart
argument_list|(
name|cell
argument_list|,
name|this
operator|.
name|prefix
operator|.
name|length
argument_list|,
name|this
operator|.
name|prefix
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
else|else
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
block|}
block|}
specifier|private
specifier|static
name|int
name|compareQualifierPart
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|int
name|length
parameter_list|,
name|byte
index|[]
name|prefix
parameter_list|)
block|{
if|if
condition|(
name|cell
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|cell
operator|)
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|cell
operator|)
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|length
argument_list|,
name|prefix
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
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
name|length
argument_list|,
name|prefix
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
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
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|,
literal|"Expected 1 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|columnPrefix
init|=
name|ParseFilter
operator|.
name|removeQuotesFromByteArray
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ColumnPrefixFilter
argument_list|(
name|columnPrefix
argument_list|)
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|ColumnPrefixFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|ColumnPrefixFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|prefix
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setPrefix
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|this
operator|.
name|prefix
argument_list|)
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
comment|/**    * @param pbBytes A pb serialized {@link ColumnPrefixFilter} instance    * @return An instance of {@link ColumnPrefixFilter} made from<code>bytes</code>    * @throws org.apache.hadoop.hbase.exceptions.DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|ColumnPrefixFilter
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
name|ColumnPrefixFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|ColumnPrefixFilter
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
name|ColumnPrefixFilter
argument_list|(
name|proto
operator|.
name|getPrefix
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param o the other filter to compare with    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
annotation|@
name|Override
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
name|ColumnPrefixFilter
operator|)
condition|)
return|return
literal|false
return|;
name|ColumnPrefixFilter
name|other
init|=
operator|(
name|ColumnPrefixFilter
operator|)
name|o
decl_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getPrefix
argument_list|()
argument_list|,
name|other
operator|.
name|getPrefix
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextCellHint
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
name|PrivateCellUtil
operator|.
name|createFirstOnRowCol
argument_list|(
name|cell
argument_list|,
name|prefix
argument_list|,
literal|0
argument_list|,
name|prefix
operator|.
name|length
argument_list|)
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
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|prefix
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|obj
operator|instanceof
name|Filter
operator|&&
name|areSerializedFieldsEqual
argument_list|(
operator|(
name|Filter
operator|)
name|obj
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|getPrefix
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

