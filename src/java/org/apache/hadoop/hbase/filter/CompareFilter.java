begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|HbaseObjectWritable
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
comment|/**  * This is a generic filter to be used to filter by comparison.  It takes an   * operator (equal, greater, not equal, etc) and a byte [] comparator.  *<p>  * To filter by row key, use {@link RowFilter}.  *<p>  * To filter by column qualifier, use {@link QualifierFilter}.  *<p>  * To filter by value, use {@link SingleColumnValueFilter}.  *<p>  * These filters can be wrapped with {@link SkipFilter} and {@link WhileMatchFilter}  * to add more control.  *<p>  * Multiple filters can be combined using {@link FilterList}.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|CompareFilter
implements|implements
name|Filter
block|{
comment|/** Comparison operators. */
specifier|public
enum|enum
name|CompareOp
block|{
comment|/** less than */
name|LESS
block|,
comment|/** less than or equal to */
name|LESS_OR_EQUAL
block|,
comment|/** equals */
name|EQUAL
block|,
comment|/** not equal */
name|NOT_EQUAL
block|,
comment|/** greater than or equal to */
name|GREATER_OR_EQUAL
block|,
comment|/** greater than */
name|GREATER
block|;   }
specifier|protected
name|CompareOp
name|compareOp
decl_stmt|;
specifier|protected
name|WritableByteArrayComparable
name|comparator
decl_stmt|;
comment|/**    * Writable constructor, do not use.    */
specifier|public
name|CompareFilter
parameter_list|()
block|{   }
comment|/**    * Constructor.    * @param compareOp the compare op for row matching    * @param comparator the comparator for row matching    */
specifier|public
name|CompareFilter
parameter_list|(
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
specifier|public
name|void
name|reset
parameter_list|()
block|{   }
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|protected
name|boolean
name|doCompare
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|byte
index|[]
name|data
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
name|int
name|compareResult
init|=
name|comparator
operator|.
name|compareTo
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
argument_list|)
decl_stmt|;
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
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
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
name|HbaseObjectWritable
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
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

