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
name|java
operator|.
name|util
operator|.
name|Objects
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
name|CompareOperator
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

begin_comment
comment|/**  * Different from {@link SingleColumnValueFilter} which returns an<b>entire</b> row  * when specified condition is matched, {@link ColumnValueFilter} return the matched cell only.  *<p>  * This filter is used to filter cells based on column and value.  * It takes a {@link org.apache.hadoop.hbase.CompareOperator} operator (<,<=, =, !=,>,>=), and  * and a {@link ByteArrayComparable} comparator.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|ColumnValueFilter
extends|extends
name|FilterBase
block|{
specifier|private
specifier|final
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
specifier|final
name|CompareOperator
name|op
decl_stmt|;
specifier|private
specifier|final
name|ByteArrayComparable
name|comparator
decl_stmt|;
comment|// This flag is used to speed up seeking cells when matched column is found, such that following
comment|// columns in the same row can be skipped faster by NEXT_ROW instead of NEXT_COL.
specifier|private
name|boolean
name|columnFound
init|=
literal|false
decl_stmt|;
specifier|public
name|ColumnValueFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOperator
name|op
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ColumnValueFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOperator
name|op
parameter_list|,
specifier|final
name|ByteArrayComparable
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|family
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|family
argument_list|,
literal|"family should not be null."
argument_list|)
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
name|qualifier
operator|==
literal|null
condition|?
operator|new
name|byte
index|[
literal|0
index|]
else|:
name|qualifier
expr_stmt|;
name|this
operator|.
name|op
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|op
argument_list|,
literal|"CompareOperator should not be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|comparator
argument_list|,
literal|"Comparator should not be null"
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return operator    */
specifier|public
name|CompareOperator
name|getCompareOperator
parameter_list|()
block|{
return|return
name|op
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
comment|/**    * @return the column family    */
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|family
return|;
block|}
comment|/**    * @return the qualifier    */
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|qualifier
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|columnFound
operator|=
literal|false
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
name|Cell
name|c
parameter_list|)
throws|throws
name|IOException
block|{
comment|// 1. Check column match
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingColumn
argument_list|(
name|c
argument_list|,
name|this
operator|.
name|family
argument_list|,
name|this
operator|.
name|qualifier
argument_list|)
condition|)
block|{
return|return
name|columnFound
condition|?
name|ReturnCode
operator|.
name|NEXT_ROW
else|:
name|ReturnCode
operator|.
name|NEXT_COL
return|;
block|}
comment|// Column found
name|columnFound
operator|=
literal|true
expr_stmt|;
comment|// 2. Check value match:
comment|// True means filter out, just skip this cell, else include it.
return|return
name|compareValue
argument_list|(
name|getCompareOperator
argument_list|()
argument_list|,
name|getComparator
argument_list|()
argument_list|,
name|c
argument_list|)
condition|?
name|ReturnCode
operator|.
name|SKIP
else|:
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
comment|/**    * This method is used to determine a cell should be included or filtered out.    * @param op one of operators {@link CompareOperator}    * @param comparator comparator used to compare cells.    * @param cell cell to be compared.    * @return true means cell should be filtered out, included otherwise.    */
specifier|private
name|boolean
name|compareValue
parameter_list|(
specifier|final
name|CompareOperator
name|op
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
name|op
operator|==
name|CompareOperator
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
name|PrivateCellUtil
operator|.
name|compareValue
argument_list|(
name|cell
argument_list|,
name|comparator
argument_list|)
decl_stmt|;
return|return
name|CompareFilter
operator|.
name|compare
argument_list|(
name|op
argument_list|,
name|compareResult
argument_list|)
return|;
block|}
comment|/**    * Creating this filter by reflection, it is used by {@link ParseFilter},    * @param filterArguments arguments for creating a ColumnValueFilter    * @return a ColumnValueFilter    */
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
literal|"Expect 4 arguments: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|family
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
name|byte
index|[]
name|qualifier
init|=
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
decl_stmt|;
name|CompareOperator
name|operator
init|=
name|ParseFilter
operator|.
name|createCompareOperator
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|2
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
literal|3
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
name|operator
operator|!=
name|CompareOperator
operator|.
name|EQUAL
operator|&&
name|operator
operator|!=
name|CompareOperator
operator|.
name|NOT_EQUAL
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"A regexstring comparator and substring comparator "
operator|+
literal|"can only be used with EQUAL and NOT_EQUAL"
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|ColumnValueFilter
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|operator
argument_list|,
name|comparator
argument_list|)
return|;
block|}
comment|/**    * @return A pb instance to represent this instance.    */
name|FilterProtos
operator|.
name|ColumnValueFilter
name|convert
parameter_list|()
block|{
name|FilterProtos
operator|.
name|ColumnValueFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|ColumnValueFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setFamily
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|this
operator|.
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setQualifier
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|this
operator|.
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCompareOp
argument_list|(
name|HBaseProtos
operator|.
name|CompareType
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|op
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
comment|/**    * Parse protobuf bytes to a ColumnValueFilter    * @param pbBytes pbBytes    * @return a ColumnValueFilter    * @throws DeserializationException deserialization exception    */
specifier|public
specifier|static
name|ColumnValueFilter
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
name|ColumnValueFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|ColumnValueFilter
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
specifier|final
name|CompareOperator
name|compareOp
init|=
name|CompareOperator
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getCompareOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ByteArrayComparable
name|comparator
decl_stmt|;
try|try
block|{
name|comparator
operator|=
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|proto
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
return|return
operator|new
name|ColumnValueFilter
argument_list|(
name|proto
operator|.
name|getFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|proto
operator|.
name|getQualifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|convert
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
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
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ColumnValueFilter
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ColumnValueFilter
name|other
init|=
operator|(
name|ColumnValueFilter
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
name|getFamily
argument_list|()
argument_list|,
name|other
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|other
operator|.
name|getQualifier
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|getCompareOperator
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getCompareOperator
argument_list|()
argument_list|)
operator|&&
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
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFamilyEssential
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|name
argument_list|,
name|this
operator|.
name|family
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
name|String
operator|.
name|format
argument_list|(
literal|"%s (%s, %s, %s, %s)"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|qualifier
argument_list|)
argument_list|,
name|this
operator|.
name|op
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
name|Objects
operator|.
name|hash
argument_list|(
name|Bytes
operator|.
name|hashCode
argument_list|(
name|getFamily
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|hashCode
argument_list|(
name|getQualifier
argument_list|()
argument_list|)
argument_list|,
name|getCompareOperator
argument_list|()
argument_list|,
name|getComparator
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

