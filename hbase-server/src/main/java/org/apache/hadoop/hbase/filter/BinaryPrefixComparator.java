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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
comment|/**  * A comparator which compares against a specified byte array, but only compares  * up to the length of this byte array. For the rest it is similar to  * {@link BinaryComparator}.  */
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
name|BinaryPrefixComparator
extends|extends
name|ByteArrayComparable
block|{
comment|/**    * Constructor    * @param value value    */
specifier|public
name|BinaryPrefixComparator
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|)
expr_stmt|;
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
name|compareTo
argument_list|(
name|this
operator|.
name|value
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|value
operator|.
name|length
argument_list|,
name|value
argument_list|,
name|offset
argument_list|,
name|this
operator|.
name|value
operator|.
name|length
operator|<=
name|length
condition|?
name|this
operator|.
name|value
operator|.
name|length
else|:
name|length
argument_list|)
return|;
block|}
comment|/**    * @return The comparator serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|ComparatorProtos
operator|.
name|BinaryPrefixComparator
operator|.
name|Builder
name|builder
init|=
name|ComparatorProtos
operator|.
name|BinaryPrefixComparator
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setComparable
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
comment|/**    * @param pbBytes A pb serialized {@link BinaryPrefixComparator} instance    * @return An instance of {@link BinaryPrefixComparator} made from<code>bytes</code>    * @throws DeserializationException    * @see {@link #toByteArray()}    */
specifier|public
specifier|static
name|BinaryPrefixComparator
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
name|BinaryPrefixComparator
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|ComparatorProtos
operator|.
name|BinaryPrefixComparator
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
name|BinaryPrefixComparator
argument_list|(
name|proto
operator|.
name|getComparable
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the comparator that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
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
name|BinaryPrefixComparator
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
name|other
argument_list|)
return|;
block|}
block|}
end_class

end_unit

