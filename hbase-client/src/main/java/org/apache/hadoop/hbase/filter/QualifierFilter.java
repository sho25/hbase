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
name|client
operator|.
name|Get
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
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
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
comment|/**  * This filter is used to filter based on the column qualifier. It takes an  * operator (equal, greater, not equal, etc) and a byte [] comparator for the  * column qualifier portion of a key.  *<p>  * This filter can be wrapped with {@link WhileMatchFilter} and {@link SkipFilter}  * to add more control.  *<p>  * Multiple filters can be combined using {@link FilterList}.  *<p>  * If an already known column qualifier is looked for, use {@link Get#addColumn}  * directly rather than a filter.  */
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
name|QualifierFilter
extends|extends
name|CompareFilter
block|{
comment|/**    * Constructor.    * @param op the compare op for column qualifier matching    * @param qualifierComparator the comparator for column qualifier matching    */
specifier|public
name|QualifierFilter
parameter_list|(
specifier|final
name|CompareOp
name|op
parameter_list|,
specifier|final
name|ByteArrayComparable
name|qualifierComparator
parameter_list|)
block|{
name|super
argument_list|(
name|op
argument_list|,
name|qualifierComparator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|v
parameter_list|)
block|{
name|int
name|qualifierLength
init|=
name|v
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|qualifierLength
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|doCompare
argument_list|(
name|this
operator|.
name|compareOp
argument_list|,
name|this
operator|.
name|comparator
argument_list|,
name|v
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|v
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|qualifierLength
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
block|}
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
name|ArrayList
argument_list|<
name|?
argument_list|>
name|arguments
init|=
name|CompareFilter
operator|.
name|extractArguments
argument_list|(
name|filterArguments
argument_list|)
decl_stmt|;
name|CompareOp
name|compareOp
init|=
operator|(
name|CompareOp
operator|)
name|arguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|comparator
init|=
operator|(
name|ByteArrayComparable
operator|)
name|arguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
return|return
operator|new
name|QualifierFilter
argument_list|(
name|compareOp
argument_list|,
name|comparator
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
name|QualifierFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|QualifierFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setCompareFilter
argument_list|(
name|super
operator|.
name|convert
argument_list|()
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
comment|/**    * @param pbBytes A pb serialized {@link QualifierFilter} instance    * @return An instance of {@link QualifierFilter} made from<code>bytes</code>    * @throws org.apache.hadoop.hbase.exceptions.DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|QualifierFilter
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
name|QualifierFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|QualifierFilter
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
name|CompareOp
name|valueCompareOp
init|=
name|CompareOp
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|getCompareOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|valueComparator
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|hasComparator
argument_list|()
condition|)
block|{
name|valueComparator
operator|=
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|QualifierFilter
argument_list|(
name|valueCompareOp
argument_list|,
name|valueComparator
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
name|QualifierFilter
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|o
argument_list|)
return|;
block|}
block|}
end_class

end_unit

