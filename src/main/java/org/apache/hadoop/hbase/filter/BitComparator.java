begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * A bit comparator which performs the specified bitwise operation on each of the bytes  * with the specified byte array. Then returns whether the result is non-zero.  */
end_comment

begin_class
specifier|public
class|class
name|BitComparator
extends|extends
name|WritableByteArrayComparable
block|{
comment|/** Nullary constructor for Writable, do not use */
specifier|public
name|BitComparator
parameter_list|()
block|{}
comment|/** Bit operators. */
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
comment|/**    * Constructor    * @param value value    * @param BitwiseOp bitOperator - the operator to use on the bit comparison    */
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
annotation|@
name|Override
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
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|bitOperator
operator|=
name|BitwiseOp
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|bitOperator
operator|.
name|name
argument_list|()
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
name|value
operator|.
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

