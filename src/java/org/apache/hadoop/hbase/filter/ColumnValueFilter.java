begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|DataInput
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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
name|HBaseConfiguration
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
name|ObjectWritable
import|;
end_import

begin_comment
comment|/**  * This filter is used to filter based on the value of a given column. It takes  * an operator (equal, greater, not equal, etc) and either a byte [] value or a  * byte [] comparator. If we have a byte [] value then we just do a  * lexicographic compare. If this is not sufficient (eg you want to deserialize  * a long and then compare it to a fixed long value, then you can pass in your  * own comparator instead.  */
end_comment

begin_class
specifier|public
class|class
name|ColumnValueFilter
implements|implements
name|RowFilterInterface
block|{
comment|/** Comparison operator. */
specifier|public
enum|enum
name|CompareOp
block|{
name|LESS
block|,
name|LESS_OR_EQUAL
block|,
name|EQUAL
block|,
name|NOT_EQUAL
block|,
name|GREATER_OR_EQUAL
block|,
name|GREATER
block|;   }
specifier|private
name|byte
index|[]
name|columnName
decl_stmt|;
specifier|private
name|CompareOp
name|compareOp
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
name|WritableByteArrayComparable
name|comparator
decl_stmt|;
name|ColumnValueFilter
parameter_list|()
block|{
comment|// for Writable
block|}
comment|/**    * Constructor.    *     * @param columnName name of column    * @param compareOp operator    * @param value value to compare column values against    */
specifier|public
name|ColumnValueFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|columnName
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|compareOp
operator|=
name|compareOp
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Constructor.    *     * @param columnName name of column    * @param compareOp operator    * @param comparator Comparator to use.    */
specifier|public
name|ColumnValueFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|columnName
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|compareOp
operator|=
name|compareOp
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|filterColumn
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|,
specifier|final
name|byte
index|[]
name|colKey
parameter_list|,
specifier|final
name|byte
index|[]
name|data
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|colKey
argument_list|,
name|columnName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|compareResult
decl_stmt|;
if|if
condition|(
name|comparator
operator|!=
literal|null
condition|)
block|{
name|compareResult
operator|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|compareResult
operator|=
name|compare
argument_list|(
name|value
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
name|compareOp
condition|)
block|{
case|case
name|LESS
case|:
return|return
name|compareResult
operator|<=
literal|0
return|;
case|case
name|LESS_OR_EQUAL
case|:
return|return
name|compareResult
operator|<
literal|0
return|;
case|case
name|EQUAL
case|:
return|return
name|compareResult
operator|!=
literal|0
return|;
case|case
name|NOT_EQUAL
case|:
return|return
name|compareResult
operator|==
literal|0
return|;
case|case
name|GREATER_OR_EQUAL
case|:
return|return
name|compareResult
operator|>
literal|0
return|;
case|case
name|GREATER
case|:
return|return
name|compareResult
operator|>=
literal|0
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown Compare op "
operator|+
name|compareOp
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|filterRow
parameter_list|(
specifier|final
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
parameter_list|)
block|{
comment|// Don't let rows through if they don't have the column we are checking
return|return
operator|!
name|columns
operator|.
name|containsKey
argument_list|(
name|columnName
argument_list|)
return|;
block|}
specifier|private
name|int
name|compare
parameter_list|(
specifier|final
name|byte
index|[]
name|b1
parameter_list|,
specifier|final
name|byte
index|[]
name|b2
parameter_list|)
block|{
name|int
name|len
init|=
name|Math
operator|.
name|min
argument_list|(
name|b1
operator|.
name|length
argument_list|,
name|b2
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|b1
index|[
name|i
index|]
operator|!=
name|b2
index|[
name|i
index|]
condition|)
block|{
return|return
name|b1
index|[
name|i
index|]
operator|-
name|b2
index|[
name|i
index|]
return|;
block|}
block|}
return|return
name|b1
operator|.
name|length
operator|-
name|b2
operator|.
name|length
return|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|boolean
name|processAlways
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|reset
parameter_list|()
block|{
comment|// Nothing.
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|rowProcessed
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|boolean
name|filtered
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|byte
index|[]
name|key
parameter_list|)
block|{
comment|// Nothing
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|validate
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|byte
index|[]
index|[]
name|columns
parameter_list|)
block|{
comment|// Nothing
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|valueLen
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|valueLen
operator|>
literal|0
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|valueLen
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|columnName
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|compareOp
operator|=
name|CompareOp
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
name|comparator
operator|=
operator|(
name|WritableByteArrayComparable
operator|)
name|ObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|columnName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|compareOp
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|ObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|comparator
argument_list|,
name|WritableByteArrayComparable
operator|.
name|class
argument_list|,
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

