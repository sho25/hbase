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
name|generated
operator|.
name|ComparatorProtos
import|;
end_import

begin_comment
comment|/**  * A bit comparator which performs the specified bitwise operation on each of the bytes  * with the specified byte array. Then returns whether the result is non-zero.  */
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
name|BitComparator
extends|extends
name|ByteArrayComparable
block|{
comment|/** Bit operators. */
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
enum|enum
name|BitwiseOp
block|{
comment|/** and */
name|AND
block|,
comment|/** or */
name|OR
block|,
comment|/** xor */
name|XOR
block|}
specifier|protected
name|BitwiseOp
name|bitOperator
decl_stmt|;
comment|/**    * Constructor    * @param value value    * @param bitOperator operator to use on the bit comparison    */
specifier|public
name|BitComparator
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|BitwiseOp
name|bitOperator
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|this
operator|.
name|bitOperator
operator|=
name|bitOperator
expr_stmt|;
block|}
comment|/**    * @return the bitwise operator    */
specifier|public
name|BitwiseOp
name|getOperator
parameter_list|()
block|{
return|return
name|bitOperator
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
name|BitComparator
operator|.
name|Builder
name|builder
init|=
name|ComparatorProtos
operator|.
name|BitComparator
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
name|ComparatorProtos
operator|.
name|BitComparator
operator|.
name|BitwiseOp
name|bitwiseOpPb
init|=
name|ComparatorProtos
operator|.
name|BitComparator
operator|.
name|BitwiseOp
operator|.
name|valueOf
argument_list|(
name|bitOperator
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setBitwiseOp
argument_list|(
name|bitwiseOpPb
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
comment|/**    * @param pbBytes A pb serialized {@link BitComparator} instance    * @return An instance of {@link BitComparator} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|BitComparator
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
name|BitComparator
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|ComparatorProtos
operator|.
name|BitComparator
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
name|BitwiseOp
name|bitwiseOp
init|=
name|BitwiseOp
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getBitwiseOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|BitComparator
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
argument_list|,
name|bitwiseOp
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
name|BitComparator
operator|)
condition|)
return|return
literal|false
return|;
name|BitComparator
name|comparator
init|=
operator|(
name|BitComparator
operator|)
name|other
decl_stmt|;
return|return
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
argument_list|)
operator|&&
name|this
operator|.
name|getOperator
argument_list|()
operator|.
name|equals
argument_list|(
name|comparator
operator|.
name|getOperator
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
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|length
operator|!=
name|this
operator|.
name|value
operator|.
name|length
condition|)
block|{
return|return
literal|1
return|;
block|}
name|int
name|b
init|=
literal|0
decl_stmt|;
comment|//Iterating backwards is faster because we can quit after one non-zero byte.
for|for
control|(
name|int
name|i
init|=
name|length
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
operator|&&
name|b
operator|==
literal|0
condition|;
name|i
operator|--
control|)
block|{
switch|switch
condition|(
name|bitOperator
condition|)
block|{
case|case
name|AND
case|:
name|b
operator|=
operator|(
name|this
operator|.
name|value
index|[
name|i
index|]
operator|&
name|value
index|[
name|i
operator|+
name|offset
index|]
operator|)
operator|&
literal|0xff
expr_stmt|;
break|break;
case|case
name|OR
case|:
name|b
operator|=
operator|(
name|this
operator|.
name|value
index|[
name|i
index|]
operator||
name|value
index|[
name|i
operator|+
name|offset
index|]
operator|)
operator|&
literal|0xff
expr_stmt|;
break|break;
case|case
name|XOR
case|:
name|b
operator|=
operator|(
name|this
operator|.
name|value
index|[
name|i
index|]
operator|^
name|value
index|[
name|i
operator|+
name|offset
index|]
operator|)
operator|&
literal|0xff
expr_stmt|;
break|break;
block|}
block|}
return|return
name|b
operator|==
literal|0
condition|?
literal|1
else|:
literal|0
return|;
block|}
block|}
end_class

end_unit

