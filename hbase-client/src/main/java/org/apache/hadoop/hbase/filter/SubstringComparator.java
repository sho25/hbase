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
name|util
operator|.
name|Locale
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
name|ComparatorProtos
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
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * This comparator is for use with SingleColumnValueFilter, for filtering based on  * the value of a given column. Use it to test if a given substring appears  * in a cell value in the column. The comparison is case insensitive.  *<p>  * Only EQUAL or NOT_EQUAL tests are valid with this comparator.  *<p>  * For example:  *<p>  *<pre>  * SingleColumnValueFilter scvf =  *   new SingleColumnValueFilter("col", CompareOp.EQUAL,  *     new SubstringComparator("substr"));  *</pre>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|SuppressWarnings
argument_list|(
literal|"ComparableType"
argument_list|)
comment|// Should this move to Comparator usage?
specifier|public
class|class
name|SubstringComparator
extends|extends
name|ByteArrayComparable
block|{
specifier|private
name|String
name|substr
decl_stmt|;
comment|/**    * Constructor    * @param substr the substring    */
specifier|public
name|SubstringComparator
parameter_list|(
name|String
name|substr
parameter_list|)
block|{
name|super
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|substr
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|substr
operator|=
name|substr
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|substr
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|contains
argument_list|(
name|substr
argument_list|)
condition|?
literal|0
else|:
literal|1
return|;
block|}
comment|/**    * @return The comparator serialized using pb    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|ComparatorProtos
operator|.
name|SubstringComparator
operator|.
name|Builder
name|builder
init|=
name|ComparatorProtos
operator|.
name|SubstringComparator
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setSubstr
argument_list|(
name|this
operator|.
name|substr
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
comment|/**    * @param pbBytes A pb serialized {@link SubstringComparator} instance    * @return An instance of {@link SubstringComparator} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|SubstringComparator
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
name|ComparatorProtos
operator|.
name|SubstringComparator
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|ComparatorProtos
operator|.
name|SubstringComparator
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
name|SubstringComparator
argument_list|(
name|proto
operator|.
name|getSubstr
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the comparator that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
annotation|@
name|Override
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|ByteArrayComparable
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
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
name|other
operator|instanceof
name|SubstringComparator
operator|)
condition|)
return|return
literal|false
return|;
name|SubstringComparator
name|comparator
init|=
operator|(
name|SubstringComparator
operator|)
name|other
decl_stmt|;
return|return
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|comparator
argument_list|)
operator|&&
name|this
operator|.
name|substr
operator|.
name|equals
argument_list|(
name|comparator
operator|.
name|substr
argument_list|)
return|;
block|}
block|}
end_class

end_unit

