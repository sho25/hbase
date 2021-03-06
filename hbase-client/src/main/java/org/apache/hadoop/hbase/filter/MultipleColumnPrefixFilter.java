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
name|Comparator
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
name|java
operator|.
name|util
operator|.
name|TreeSet
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

begin_comment
comment|/**  * This filter is used for selecting only those keys with columns that matches  * a particular prefix. For example, if prefix is 'an', it will pass keys will  * columns like 'and', 'anti' but not keys with columns like 'ball', 'act'.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|MultipleColumnPrefixFilter
extends|extends
name|FilterBase
block|{
specifier|protected
name|byte
index|[]
name|hint
init|=
literal|null
decl_stmt|;
specifier|protected
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|sortedPrefixes
init|=
name|createTreeSet
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|MAX_LOG_PREFIXES
init|=
literal|5
decl_stmt|;
specifier|public
name|MultipleColumnPrefixFilter
parameter_list|(
specifier|final
name|byte
index|[]
index|[]
name|prefixes
parameter_list|)
block|{
if|if
condition|(
name|prefixes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|prefixes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|sortedPrefixes
operator|.
name|add
argument_list|(
name|prefixes
index|[
name|i
index|]
argument_list|)
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"prefixes must be distinct"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|byte
index|[]
index|[]
name|getPrefix
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|byte
index|[]
index|[]
name|temp
init|=
operator|new
name|byte
index|[
name|sortedPrefixes
operator|.
name|size
argument_list|()
index|]
index|[]
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|prefixes
range|:
name|sortedPrefixes
control|)
block|{
name|temp
index|[
name|count
operator|++
index|]
operator|=
name|prefixes
expr_stmt|;
block|}
return|return
name|temp
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
name|c
parameter_list|)
block|{
if|if
condition|(
name|sortedPrefixes
operator|.
name|isEmpty
argument_list|()
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
name|c
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
name|byte
index|[]
name|qualifier
init|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|lesserOrEqualPrefixes
init|=
operator|(
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
operator|)
name|sortedPrefixes
operator|.
name|headSet
argument_list|(
name|qualifier
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|lesserOrEqualPrefixes
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|byte
index|[]
name|largestPrefixSmallerThanQualifier
init|=
name|lesserOrEqualPrefixes
operator|.
name|last
argument_list|()
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|startsWith
argument_list|(
name|qualifier
argument_list|,
name|largestPrefixSmallerThanQualifier
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
if|if
condition|(
name|lesserOrEqualPrefixes
operator|.
name|size
argument_list|()
operator|==
name|sortedPrefixes
operator|.
name|size
argument_list|()
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
name|hint
operator|=
name|sortedPrefixes
operator|.
name|higher
argument_list|(
name|largestPrefixSmallerThanQualifier
argument_list|)
expr_stmt|;
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
block|}
else|else
block|{
name|hint
operator|=
name|sortedPrefixes
operator|.
name|first
argument_list|()
expr_stmt|;
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
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
name|byte
index|[]
index|[]
name|prefixes
init|=
operator|new
name|byte
index|[
name|filterArguments
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|filterArguments
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|prefixes
index|[
name|i
index|]
operator|=
name|columnPrefix
expr_stmt|;
block|}
return|return
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|prefixes
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
name|MultipleColumnPrefixFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|MultipleColumnPrefixFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|element
range|:
name|sortedPrefixes
control|)
block|{
if|if
condition|(
name|element
operator|!=
literal|null
condition|)
name|builder
operator|.
name|addSortedPrefixes
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|element
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * @param pbBytes A pb serialized {@link MultipleColumnPrefixFilter} instance    * @return An instance of {@link MultipleColumnPrefixFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|MultipleColumnPrefixFilter
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
name|MultipleColumnPrefixFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|MultipleColumnPrefixFilter
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
name|int
name|numPrefixes
init|=
name|proto
operator|.
name|getSortedPrefixesCount
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|prefixes
init|=
operator|new
name|byte
index|[
name|numPrefixes
index|]
index|[]
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
name|numPrefixes
condition|;
operator|++
name|i
control|)
block|{
name|prefixes
index|[
name|i
index|]
operator|=
name|proto
operator|.
name|getSortedPrefixes
argument_list|(
name|i
argument_list|)
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|prefixes
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
name|MultipleColumnPrefixFilter
operator|)
condition|)
return|return
literal|false
return|;
name|MultipleColumnPrefixFilter
name|other
init|=
operator|(
name|MultipleColumnPrefixFilter
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|sortedPrefixes
operator|.
name|equals
argument_list|(
name|other
operator|.
name|sortedPrefixes
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
name|hint
argument_list|,
literal|0
argument_list|,
name|hint
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|createTreeSet
parameter_list|()
block|{
return|return
operator|new
name|TreeSet
argument_list|<>
argument_list|(
operator|new
name|Comparator
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Object
name|o1
parameter_list|,
name|Object
name|o2
parameter_list|)
block|{
if|if
condition|(
name|o1
operator|==
literal|null
operator|||
name|o2
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"prefixes can't be null"
argument_list|)
throw|;
name|byte
index|[]
name|b1
init|=
operator|(
name|byte
index|[]
operator|)
name|o1
decl_stmt|;
name|byte
index|[]
name|b2
init|=
operator|(
name|byte
index|[]
operator|)
name|o2
decl_stmt|;
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|b1
argument_list|,
literal|0
argument_list|,
name|b1
operator|.
name|length
argument_list|,
name|b2
argument_list|,
literal|0
argument_list|,
name|b2
operator|.
name|length
argument_list|)
return|;
block|}
block|}
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
name|toString
argument_list|(
name|MAX_LOG_PREFIXES
argument_list|)
return|;
block|}
specifier|protected
name|String
name|toString
parameter_list|(
name|int
name|maxPrefixes
parameter_list|)
block|{
name|StringBuilder
name|prefixes
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|ba
range|:
name|this
operator|.
name|sortedPrefixes
control|)
block|{
if|if
condition|(
name|count
operator|>=
name|maxPrefixes
condition|)
block|{
break|break;
block|}
operator|++
name|count
expr_stmt|;
name|prefixes
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|ba
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|<
name|this
operator|.
name|sortedPrefixes
operator|.
name|size
argument_list|()
operator|&&
name|count
operator|<
name|maxPrefixes
condition|)
block|{
name|prefixes
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s (%d/%d): [%s]"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|count
argument_list|,
name|this
operator|.
name|sortedPrefixes
operator|.
name|size
argument_list|()
argument_list|,
name|prefixes
operator|.
name|toString
argument_list|()
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
name|this
operator|.
name|sortedPrefixes
argument_list|)
return|;
block|}
block|}
end_class

end_unit

