begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|HBaseProtos
operator|.
name|CompareType
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
name|hbase
operator|.
name|shaded
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

begin_comment
comment|/**  * This is a generic filter to be used to filter by comparison.  It takes an  * operator (equal, greater, not equal, etc) and a byte [] comparator.  *<p>  * To filter by row key, use {@link RowFilter}.  *<p>  * To filter by column qualifier, use {@link QualifierFilter}.  *<p>  * To filter by value, use {@link SingleColumnValueFilter}.  *<p>  * These filters can be wrapped with {@link SkipFilter} and {@link WhileMatchFilter}  * to add more control.  *<p>  * Multiple filters can be combined using {@link FilterList}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|CompareFilter
extends|extends
name|FilterBase
block|{
comment|/** Comparison operators. */
annotation|@
name|InterfaceAudience
operator|.
name|Public
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
block|,
comment|/** no operation */
name|NO_OP
block|,   }
specifier|protected
name|CompareOp
name|compareOp
decl_stmt|;
specifier|protected
name|ByteArrayComparable
name|comparator
decl_stmt|;
comment|/**    * Constructor.    * @param compareOp the compare op for row matching    * @param comparator the comparator for row matching    */
specifier|public
name|CompareFilter
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|ByteArrayComparable
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
comment|/**    * @return operator    */
specifier|public
name|CompareOp
name|getOperator
parameter_list|()
block|{
return|return
name|compareOp
return|;
block|}
comment|/**    * @return the comparator    */
specifier|public
name|ByteArrayComparable
name|getComparator
parameter_list|()
block|{
return|return
name|comparator
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
specifier|protected
name|boolean
name|compareRow
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|ByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|compareOp
operator|==
name|CompareOp
operator|.
name|NO_OP
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|compareResult
init|=
name|CellComparator
operator|.
name|compareRow
argument_list|(
name|cell
argument_list|,
name|comparator
argument_list|)
decl_stmt|;
return|return
name|compare
argument_list|(
name|compareOp
argument_list|,
name|compareResult
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|compareFamily
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|ByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|compareOp
operator|==
name|CompareOp
operator|.
name|NO_OP
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|compareResult
init|=
name|CellComparator
operator|.
name|compareFamily
argument_list|(
name|cell
argument_list|,
name|comparator
argument_list|)
decl_stmt|;
return|return
name|compare
argument_list|(
name|compareOp
argument_list|,
name|compareResult
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|compareQualifier
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|ByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|compareOp
operator|==
name|CompareOp
operator|.
name|NO_OP
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|compareResult
init|=
name|CellComparator
operator|.
name|compareQualifier
argument_list|(
name|cell
argument_list|,
name|comparator
argument_list|)
decl_stmt|;
return|return
name|compare
argument_list|(
name|compareOp
argument_list|,
name|compareResult
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|compareValue
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|ByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|compareOp
operator|==
name|CompareOp
operator|.
name|NO_OP
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|compareResult
init|=
name|CellComparator
operator|.
name|compareValue
argument_list|(
name|cell
argument_list|,
name|comparator
argument_list|)
decl_stmt|;
return|return
name|compare
argument_list|(
name|compareOp
argument_list|,
name|compareResult
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|compare
parameter_list|(
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
name|int
name|compareResult
parameter_list|)
block|{
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
comment|// returns an array of heterogeneous objects
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|extractArguments
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
literal|2
argument_list|,
literal|"Expected 2 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|CompareOp
name|compareOp
init|=
name|ParseFilter
operator|.
name|createCompareOp
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|comparator
init|=
name|ParseFilter
operator|.
name|createComparator
argument_list|(
name|ParseFilter
operator|.
name|removeQuotesFromByteArray
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparator
operator|instanceof
name|RegexStringComparator
operator|||
name|comparator
operator|instanceof
name|SubstringComparator
condition|)
block|{
if|if
condition|(
name|compareOp
operator|!=
name|CompareOp
operator|.
name|EQUAL
operator|&&
name|compareOp
operator|!=
name|CompareOp
operator|.
name|NOT_EQUAL
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"A regexstring comparator and substring comparator"
operator|+
literal|" can only be used with EQUAL and NOT_EQUAL"
argument_list|)
throw|;
block|}
block|}
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|arguments
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|arguments
operator|.
name|add
argument_list|(
name|compareOp
argument_list|)
expr_stmt|;
name|arguments
operator|.
name|add
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
return|return
name|arguments
return|;
block|}
comment|/**    * @return A pb instance to represent this instance.    */
name|FilterProtos
operator|.
name|CompareFilter
name|convert
parameter_list|()
block|{
name|FilterProtos
operator|.
name|CompareFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|CompareFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|HBaseProtos
operator|.
name|CompareType
name|compareOp
init|=
name|CompareType
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|compareOp
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setCompareOp
argument_list|(
name|compareOp
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|comparator
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|this
operator|.
name|comparator
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    *    * @param o    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
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
name|CompareFilter
operator|)
condition|)
return|return
literal|false
return|;
name|CompareFilter
name|other
init|=
operator|(
name|CompareFilter
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|getOperator
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getOperator
argument_list|()
argument_list|)
operator|&&
operator|(
name|this
operator|.
name|getComparator
argument_list|()
operator|==
name|other
operator|.
name|getComparator
argument_list|()
operator|||
name|this
operator|.
name|getComparator
argument_list|()
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
operator|.
name|getComparator
argument_list|()
argument_list|)
operator|)
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
name|String
operator|.
name|format
argument_list|(
literal|"%s (%s, %s)"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|this
operator|.
name|compareOp
operator|.
name|name
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|comparator
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

