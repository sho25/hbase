begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ByteString
import|;
end_import

begin_comment
comment|/** Base class for byte array comparators */
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
specifier|abstract
class|class
name|WritableByteArrayComparable
implements|implements
name|Comparable
argument_list|<
name|byte
index|[]
argument_list|>
block|{
name|byte
index|[]
name|value
decl_stmt|;
comment|/**    * Constructor.    * @param value the value to compare against    */
specifier|public
name|WritableByteArrayComparable
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**    * @return The comparator serialized using pb    */
specifier|public
specifier|abstract
name|byte
index|[]
name|toByteArray
parameter_list|()
function_decl|;
name|ComparatorProtos
operator|.
name|ByteArrayComparable
name|convert
parameter_list|()
block|{
name|ComparatorProtos
operator|.
name|ByteArrayComparable
operator|.
name|Builder
name|builder
init|=
name|ComparatorProtos
operator|.
name|ByteArrayComparable
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setValue
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|value
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
comment|/**    * @param pbBytes A pb serialized {@link WritableByteArrayComparable} instance    * @return An instance of {@link WritableByteArrayComparable} made from<code>bytes</code>    * @throws DeserializationException    * @see {@link #toByteArray()}    */
specifier|public
specifier|static
name|WritableByteArrayComparable
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
throw|throw
operator|new
name|DeserializationException
argument_list|(
literal|"parseFrom called on base WritableByteArrayComparable, but should be called on derived type"
argument_list|)
throw|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the comparator that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|WritableByteArrayComparable
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
name|WritableByteArrayComparable
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getValue
argument_list|()
argument_list|,
name|o
operator|.
name|getValue
argument_list|()
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
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Special compareTo method for subclasses, to avoid    * copying byte[] unnecessarily.    * @param value byte[] to compare    * @param offset offset into value    * @param length number of bytes to compare    * @return a negative integer, zero, or a positive integer as this object    *         is less than, equal to, or greater than the specified object.    */
specifier|public
specifier|abstract
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
function_decl|;
block|}
end_class

end_unit

