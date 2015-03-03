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
import|import static
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
operator|.
name|len
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
name|classification
operator|.
name|InterfaceStability
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
name|ByteStringer
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
comment|/**  * This filter is used for selecting only those keys with columns that are  * between minColumn to maxColumn. For example, if minColumn is 'an', and  * maxColumn is 'be', it will pass keys with columns like 'ana', 'bad', but not  * keys with columns like 'bed', 'eye'  *  * If minColumn is null, there is no lower bound. If maxColumn is null, there is  * no upper bound.  *  * minColumnInclusive and maxColumnInclusive specify if the ranges are inclusive  * or not.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|ColumnRangeFilter
extends|extends
name|FilterBase
block|{
specifier|protected
name|byte
index|[]
name|minColumn
init|=
literal|null
decl_stmt|;
specifier|protected
name|boolean
name|minColumnInclusive
init|=
literal|true
decl_stmt|;
specifier|protected
name|byte
index|[]
name|maxColumn
init|=
literal|null
decl_stmt|;
specifier|protected
name|boolean
name|maxColumnInclusive
init|=
literal|false
decl_stmt|;
comment|/**    * Create a filter to select those keys with columns that are between minColumn    * and maxColumn.    * @param minColumn minimum value for the column range. If if it's null,    * there is no lower bound.    * @param minColumnInclusive if true, include minColumn in the range.    * @param maxColumn maximum value for the column range. If it's null,    * @param maxColumnInclusive if true, include maxColumn in the range.    * there is no upper bound.    */
specifier|public
name|ColumnRangeFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|minColumn
parameter_list|,
name|boolean
name|minColumnInclusive
parameter_list|,
specifier|final
name|byte
index|[]
name|maxColumn
parameter_list|,
name|boolean
name|maxColumnInclusive
parameter_list|)
block|{
name|this
operator|.
name|minColumn
operator|=
name|minColumn
expr_stmt|;
name|this
operator|.
name|minColumnInclusive
operator|=
name|minColumnInclusive
expr_stmt|;
name|this
operator|.
name|maxColumn
operator|=
name|maxColumn
expr_stmt|;
name|this
operator|.
name|maxColumnInclusive
operator|=
name|maxColumnInclusive
expr_stmt|;
block|}
comment|/**    * @return if min column range is inclusive.    */
specifier|public
name|boolean
name|isMinColumnInclusive
parameter_list|()
block|{
return|return
name|minColumnInclusive
return|;
block|}
comment|/**    * @return if max column range is inclusive.    */
specifier|public
name|boolean
name|isMaxColumnInclusive
parameter_list|()
block|{
return|return
name|maxColumnInclusive
return|;
block|}
comment|/**    * @return the min column range for the filter    */
specifier|public
name|byte
index|[]
name|getMinColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|minColumn
return|;
block|}
comment|/**    * @return true if min column is inclusive, false otherwise    */
specifier|public
name|boolean
name|getMinColumnInclusive
parameter_list|()
block|{
return|return
name|this
operator|.
name|minColumnInclusive
return|;
block|}
comment|/**    * @return the max column range for the filter    */
specifier|public
name|byte
index|[]
name|getMaxColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxColumn
return|;
block|}
comment|/**    * @return true if max column is inclusive, false otherwise    */
specifier|public
name|boolean
name|getMaxColumnInclusive
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxColumnInclusive
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|kv
parameter_list|)
block|{
comment|// TODO have a column compare method in Cell
name|byte
index|[]
name|buffer
init|=
name|kv
operator|.
name|getQualifierArray
argument_list|()
decl_stmt|;
name|int
name|qualifierOffset
init|=
name|kv
operator|.
name|getQualifierOffset
argument_list|()
decl_stmt|;
name|int
name|qualifierLength
init|=
name|kv
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
name|int
name|cmpMin
init|=
literal|1
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|minColumn
operator|!=
literal|null
condition|)
block|{
name|cmpMin
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|buffer
argument_list|,
name|qualifierOffset
argument_list|,
name|qualifierLength
argument_list|,
name|this
operator|.
name|minColumn
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|minColumn
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmpMin
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
if|if
condition|(
operator|!
name|this
operator|.
name|minColumnInclusive
operator|&&
name|cmpMin
operator|==
literal|0
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
if|if
condition|(
name|this
operator|.
name|maxColumn
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
name|int
name|cmpMax
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|buffer
argument_list|,
name|qualifierOffset
argument_list|,
name|qualifierLength
argument_list|,
name|this
operator|.
name|maxColumn
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|maxColumn
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|maxColumnInclusive
operator|&&
name|cmpMax
operator|<=
literal|0
operator|||
operator|!
name|this
operator|.
name|maxColumnInclusive
operator|&&
name|cmpMax
operator|<
literal|0
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
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
literal|4
argument_list|,
literal|"Expected 4 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|minColumn
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
name|boolean
name|minColumnInclusive
init|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|maxColumn
init|=
name|ParseFilter
operator|.
name|removeQuotesFromByteArray
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|maxColumnInclusive
init|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|minColumn
operator|.
name|length
operator|==
literal|0
condition|)
name|minColumn
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|maxColumn
operator|.
name|length
operator|==
literal|0
condition|)
name|maxColumn
operator|=
literal|null
expr_stmt|;
return|return
operator|new
name|ColumnRangeFilter
argument_list|(
name|minColumn
argument_list|,
name|minColumnInclusive
argument_list|,
name|maxColumn
argument_list|,
name|maxColumnInclusive
argument_list|)
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
name|ColumnRangeFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|ColumnRangeFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|minColumn
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setMinColumn
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|this
operator|.
name|minColumn
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMinColumnInclusive
argument_list|(
name|this
operator|.
name|minColumnInclusive
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|maxColumn
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setMaxColumn
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|this
operator|.
name|maxColumn
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMaxColumnInclusive
argument_list|(
name|this
operator|.
name|maxColumnInclusive
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
comment|/**    * @param pbBytes A pb serialized {@link ColumnRangeFilter} instance    * @return An instance of {@link ColumnRangeFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|ColumnRangeFilter
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
name|ColumnRangeFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|ColumnRangeFilter
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
name|ColumnRangeFilter
argument_list|(
name|proto
operator|.
name|hasMinColumn
argument_list|()
condition|?
name|proto
operator|.
name|getMinColumn
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|,
name|proto
operator|.
name|getMinColumnInclusive
argument_list|()
argument_list|,
name|proto
operator|.
name|hasMaxColumn
argument_list|()
condition|?
name|proto
operator|.
name|getMaxColumn
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|,
name|proto
operator|.
name|getMaxColumnInclusive
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
name|ColumnRangeFilter
operator|)
condition|)
return|return
literal|false
return|;
name|ColumnRangeFilter
name|other
init|=
operator|(
name|ColumnRangeFilter
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
name|getMinColumn
argument_list|()
argument_list|,
name|other
operator|.
name|getMinColumn
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|getMinColumnInclusive
argument_list|()
operator|==
name|other
operator|.
name|getMinColumnInclusive
argument_list|()
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getMaxColumn
argument_list|()
argument_list|,
name|other
operator|.
name|getMaxColumn
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|getMaxColumnInclusive
argument_list|()
operator|==
name|other
operator|.
name|getMaxColumnInclusive
argument_list|()
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
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|this
operator|.
name|minColumn
argument_list|,
literal|0
argument_list|,
name|len
argument_list|(
name|this
operator|.
name|minColumn
argument_list|)
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
operator|(
name|this
operator|.
name|minColumnInclusive
condition|?
literal|"["
else|:
literal|"("
operator|)
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|minColumn
argument_list|)
operator|+
literal|", "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|maxColumn
argument_list|)
operator|+
operator|(
name|this
operator|.
name|maxColumnInclusive
condition|?
literal|"]"
else|:
literal|")"
operator|)
return|;
block|}
block|}
end_class

end_unit

