begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|io
operator|.
name|DataInput
import|;
end_import

begin_comment
comment|/**  * This filter is used for selecting only those keys with columns that are  * between minColumn to maxColumn. For example, if minColumn is 'an', and  * maxColumn is 'be', it will pass keys with columns like 'ana', 'bad', but not  * keys with columns like 'bed', 'eye'  *  * If minColumn is null, there is no lower bound. If maxColumn is null, there is  * no upper bound.  *  * minColumnInclusive and maxColumnInclusive specify if the ranges are inclusive  * or not.  */
end_comment

begin_class
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
specifier|public
name|ColumnRangeFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
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
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
name|byte
index|[]
name|buffer
init|=
name|kv
operator|.
name|getBuffer
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
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// need to write out a flag for null value separately. Otherwise,
comment|// we will not be able to differentiate empty string and null
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|minColumn
operator|==
literal|null
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|minColumn
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|minColumnInclusive
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|maxColumn
operator|==
literal|null
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|maxColumn
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|maxColumnInclusive
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isMinColumnNull
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
name|this
operator|.
name|minColumn
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMinColumnNull
condition|)
block|{
name|this
operator|.
name|minColumn
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|minColumnInclusive
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|boolean
name|isMaxColumnNull
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
name|this
operator|.
name|maxColumn
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMaxColumnNull
condition|)
block|{
name|this
operator|.
name|maxColumn
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|maxColumnInclusive
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValue
name|getNextKeyHint
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
return|return
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|kv
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
name|this
operator|.
name|minColumn
operator|==
literal|null
condition|?
literal|0
else|:
name|this
operator|.
name|minColumn
operator|.
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

