begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|types
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertArrayEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|java
operator|.
name|util
operator|.
name|Collection
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
operator|.
name|SmallTests
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
name|util
operator|.
name|Order
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
name|PositionedByteRange
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
name|SimplePositionedMutableByteRange
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * This class both tests and demonstrates how to construct compound rowkeys  * from a POJO. The code under test is {@link Struct}.  * {@link SpecializedPojo1Type1} demonstrates how one might create their own  * custom data type extension for an application POJO.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestStruct
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestStruct
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
name|value
operator|=
literal|0
argument_list|)
specifier|public
name|Struct
name|generic
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
name|value
operator|=
literal|1
argument_list|)
specifier|public
name|DataType
name|specialized
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
name|value
operator|=
literal|2
argument_list|)
specifier|public
name|Object
index|[]
index|[]
name|constructorArgs
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|params
parameter_list|()
block|{
name|Object
index|[]
index|[]
name|pojo1Args
init|=
block|{
operator|new
name|Object
index|[]
block|{
literal|"foo"
block|,
literal|5
block|,
literal|10.001
block|}
block|,
operator|new
name|Object
index|[]
block|{
literal|"foo"
block|,
literal|100
block|,
literal|7.0
block|}
block|,
operator|new
name|Object
index|[]
block|{
literal|"foo"
block|,
literal|100
block|,
literal|10.001
block|}
block|,
operator|new
name|Object
index|[]
block|{
literal|"bar"
block|,
literal|5
block|,
literal|10.001
block|}
block|,
operator|new
name|Object
index|[]
block|{
literal|"bar"
block|,
literal|100
block|,
literal|10.001
block|}
block|,
operator|new
name|Object
index|[]
block|{
literal|"baz"
block|,
literal|5
block|,
literal|10.001
block|}
block|,     }
decl_stmt|;
name|Object
index|[]
index|[]
name|pojo2Args
init|=
block|{
operator|new
name|Object
index|[]
block|{
operator|new
name|byte
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"it"
argument_list|)
block|,
literal|"was"
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"the"
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"best"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
literal|"of"
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"times,"
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"it"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"was"
argument_list|)
block|,
literal|""
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"the"
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"worst"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"of"
argument_list|)
block|,
literal|"times,"
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|byte
index|[
literal|0
index|]
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
literal|""
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|}
block|,     }
decl_stmt|;
name|Object
index|[]
index|[]
name|params
init|=
operator|new
name|Object
index|[]
index|[]
block|{
block|{
name|SpecializedPojo1Type1
operator|.
name|GENERIC
block|,
operator|new
name|SpecializedPojo1Type1
argument_list|()
block|,
name|pojo1Args
block|}
block|,
block|{
name|SpecializedPojo2Type1
operator|.
name|GENERIC
block|,
operator|new
name|SpecializedPojo2Type1
argument_list|()
block|,
name|pojo2Args
block|}
block|,     }
decl_stmt|;
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|params
argument_list|)
return|;
block|}
specifier|static
specifier|final
name|Comparator
argument_list|<
name|byte
index|[]
argument_list|>
name|NULL_SAFE_BYTES_COMPARATOR
init|=
operator|new
name|Comparator
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|o1
parameter_list|,
name|byte
index|[]
name|o2
parameter_list|)
block|{
if|if
condition|(
name|o1
operator|==
name|o2
condition|)
return|return
literal|0
return|;
if|if
condition|(
literal|null
operator|==
name|o1
condition|)
return|return
operator|-
literal|1
return|;
if|if
condition|(
literal|null
operator|==
name|o2
condition|)
return|return
literal|1
return|;
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * A simple object to serialize.    */
specifier|private
specifier|static
class|class
name|Pojo1
implements|implements
name|Comparable
argument_list|<
name|Pojo1
argument_list|>
block|{
specifier|final
name|String
name|stringFieldAsc
decl_stmt|;
specifier|final
name|int
name|intFieldAsc
decl_stmt|;
specifier|final
name|double
name|doubleFieldAsc
decl_stmt|;
specifier|final
specifier|transient
name|String
name|str
decl_stmt|;
specifier|public
name|Pojo1
parameter_list|(
name|Object
modifier|...
name|argv
parameter_list|)
block|{
name|stringFieldAsc
operator|=
operator|(
name|String
operator|)
name|argv
index|[
literal|0
index|]
expr_stmt|;
name|intFieldAsc
operator|=
operator|(
name|Integer
operator|)
name|argv
index|[
literal|1
index|]
expr_stmt|;
name|doubleFieldAsc
operator|=
operator|(
name|Double
operator|)
name|argv
index|[
literal|2
index|]
expr_stmt|;
name|str
operator|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
literal|"{ "
argument_list|)
operator|.
name|append
argument_list|(
literal|null
operator|==
name|stringFieldAsc
condition|?
literal|""
else|:
literal|"\""
argument_list|)
operator|.
name|append
argument_list|(
name|stringFieldAsc
argument_list|)
operator|.
name|append
argument_list|(
literal|null
operator|==
name|stringFieldAsc
condition|?
literal|""
else|:
literal|"\""
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
name|intFieldAsc
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
name|doubleFieldAsc
argument_list|)
operator|.
name|append
argument_list|(
literal|" }"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|str
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Pojo1
name|o
parameter_list|)
block|{
name|int
name|cmp
init|=
name|stringFieldAsc
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|stringFieldAsc
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
name|cmp
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|intFieldAsc
argument_list|)
operator|.
name|compareTo
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|o
operator|.
name|intFieldAsc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
return|return
name|Double
operator|.
name|compare
argument_list|(
name|doubleFieldAsc
argument_list|,
name|o
operator|.
name|doubleFieldAsc
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
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|long
name|temp
decl_stmt|;
name|temp
operator|=
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|doubleFieldAsc
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|temp
operator|^
operator|(
name|temp
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|intFieldAsc
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|stringFieldAsc
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|stringFieldAsc
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
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
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Pojo1
name|other
init|=
operator|(
name|Pojo1
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|doubleFieldAsc
argument_list|)
operator|!=
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|other
operator|.
name|doubleFieldAsc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|intFieldAsc
operator|!=
name|other
operator|.
name|intFieldAsc
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|stringFieldAsc
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|stringFieldAsc
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|stringFieldAsc
operator|.
name|equals
argument_list|(
name|other
operator|.
name|stringFieldAsc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * A simple object to serialize.    */
specifier|private
specifier|static
class|class
name|Pojo2
implements|implements
name|Comparable
argument_list|<
name|Pojo2
argument_list|>
block|{
specifier|final
name|byte
index|[]
name|byteField1Asc
decl_stmt|;
specifier|final
name|byte
index|[]
name|byteField2Dsc
decl_stmt|;
specifier|final
name|String
name|stringFieldDsc
decl_stmt|;
specifier|final
name|byte
index|[]
name|byteField3Dsc
decl_stmt|;
specifier|final
specifier|transient
name|String
name|str
decl_stmt|;
specifier|public
name|Pojo2
parameter_list|(
name|Object
modifier|...
name|vals
parameter_list|)
block|{
name|byteField1Asc
operator|=
name|vals
operator|.
name|length
operator|>
literal|0
condition|?
operator|(
name|byte
index|[]
operator|)
name|vals
index|[
literal|0
index|]
else|:
literal|null
expr_stmt|;
name|byteField2Dsc
operator|=
name|vals
operator|.
name|length
operator|>
literal|1
condition|?
operator|(
name|byte
index|[]
operator|)
name|vals
index|[
literal|1
index|]
else|:
literal|null
expr_stmt|;
name|stringFieldDsc
operator|=
name|vals
operator|.
name|length
operator|>
literal|2
condition|?
operator|(
name|String
operator|)
name|vals
index|[
literal|2
index|]
else|:
literal|null
expr_stmt|;
name|byteField3Dsc
operator|=
name|vals
operator|.
name|length
operator|>
literal|3
condition|?
operator|(
name|byte
index|[]
operator|)
name|vals
index|[
literal|3
index|]
else|:
literal|null
expr_stmt|;
name|str
operator|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
literal|"{ "
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|byteField1Asc
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|byteField2Dsc
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
literal|null
operator|==
name|stringFieldDsc
condition|?
literal|""
else|:
literal|"\""
argument_list|)
operator|.
name|append
argument_list|(
name|stringFieldDsc
argument_list|)
operator|.
name|append
argument_list|(
literal|null
operator|==
name|stringFieldDsc
condition|?
literal|""
else|:
literal|"\""
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|byteField3Dsc
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|" }"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|str
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Pojo2
name|o
parameter_list|)
block|{
name|int
name|cmp
init|=
name|NULL_SAFE_BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|byteField1Asc
argument_list|,
name|o
operator|.
name|byteField1Asc
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
name|cmp
operator|=
operator|-
name|NULL_SAFE_BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|byteField2Dsc
argument_list|,
name|o
operator|.
name|byteField2Dsc
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
if|if
condition|(
literal|null
operator|==
name|stringFieldDsc
condition|)
block|{
name|cmp
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|null
operator|==
name|o
operator|.
name|stringFieldDsc
condition|)
block|{
name|cmp
operator|=
operator|-
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|stringFieldDsc
operator|.
name|equals
argument_list|(
name|o
operator|.
name|stringFieldDsc
argument_list|)
condition|)
block|{
name|cmp
operator|=
literal|0
expr_stmt|;
block|}
else|else
name|cmp
operator|=
operator|-
name|stringFieldDsc
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|stringFieldDsc
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
return|return
operator|-
name|NULL_SAFE_BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|byteField3Dsc
argument_list|,
name|o
operator|.
name|byteField3Dsc
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
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|byteField1Asc
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|byteField2Dsc
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|byteField3Dsc
argument_list|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|stringFieldDsc
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|stringFieldDsc
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
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
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Pojo2
name|other
init|=
operator|(
name|Pojo2
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|byteField1Asc
argument_list|,
name|other
operator|.
name|byteField1Asc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|byteField2Dsc
argument_list|,
name|other
operator|.
name|byteField2Dsc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|byteField3Dsc
argument_list|,
name|other
operator|.
name|byteField3Dsc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|stringFieldDsc
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|stringFieldDsc
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|stringFieldDsc
operator|.
name|equals
argument_list|(
name|other
operator|.
name|stringFieldDsc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * A custom data type implementation specialized for {@link Pojo1}.    */
specifier|private
specifier|static
class|class
name|SpecializedPojo1Type1
implements|implements
name|DataType
argument_list|<
name|Pojo1
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|RawStringTerminated
name|stringField
init|=
operator|new
name|RawStringTerminated
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|RawInteger
name|intField
init|=
operator|new
name|RawInteger
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|RawDouble
name|doubleField
init|=
operator|new
name|RawDouble
argument_list|()
decl_stmt|;
comment|/**      * The {@link Struct} equivalent of this type.      */
specifier|public
specifier|static
name|Struct
name|GENERIC
init|=
operator|new
name|StructBuilder
argument_list|()
operator|.
name|add
argument_list|(
name|stringField
argument_list|)
operator|.
name|add
argument_list|(
name|intField
argument_list|)
operator|.
name|add
argument_list|(
name|doubleField
argument_list|)
operator|.
name|toStruct
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isOrderPreserving
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Order
name|getOrder
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNullable
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSkippable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|encodedLength
parameter_list|(
name|Pojo1
name|val
parameter_list|)
block|{
return|return
name|stringField
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|stringFieldAsc
argument_list|)
operator|+
name|intField
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|intFieldAsc
argument_list|)
operator|+
name|doubleField
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|doubleFieldAsc
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|Pojo1
argument_list|>
name|encodedClass
parameter_list|()
block|{
return|return
name|Pojo1
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|skip
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
name|int
name|skipped
init|=
name|stringField
operator|.
name|skip
argument_list|(
name|src
argument_list|)
decl_stmt|;
name|skipped
operator|+=
name|intField
operator|.
name|skip
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|skipped
operator|+=
name|doubleField
operator|.
name|skip
argument_list|(
name|src
argument_list|)
expr_stmt|;
return|return
name|skipped
return|;
block|}
annotation|@
name|Override
specifier|public
name|Pojo1
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
name|Object
index|[]
name|ret
init|=
operator|new
name|Object
index|[
literal|3
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|stringField
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|ret
index|[
literal|1
index|]
operator|=
name|intField
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|ret
index|[
literal|2
index|]
operator|=
name|doubleField
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pojo1
argument_list|(
name|ret
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|encode
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|Pojo1
name|val
parameter_list|)
block|{
name|int
name|written
init|=
name|stringField
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|stringFieldAsc
argument_list|)
decl_stmt|;
name|written
operator|+=
name|intField
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|intFieldAsc
argument_list|)
expr_stmt|;
name|written
operator|+=
name|doubleField
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|doubleFieldAsc
argument_list|)
expr_stmt|;
return|return
name|written
return|;
block|}
block|}
comment|/**    * A custom data type implementation specialized for {@link Pojo2}.    */
specifier|private
specifier|static
class|class
name|SpecializedPojo2Type1
implements|implements
name|DataType
argument_list|<
name|Pojo2
argument_list|>
block|{
specifier|private
specifier|static
name|RawBytesTerminated
name|byteField1
init|=
operator|new
name|RawBytesTerminated
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RawBytesTerminated
name|byteField2
init|=
operator|new
name|RawBytesTerminated
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RawStringTerminated
name|stringField
init|=
operator|new
name|RawStringTerminated
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RawBytes
name|byteField3
init|=
name|RawBytes
operator|.
name|DESCENDING
decl_stmt|;
comment|/**      * The {@link Struct} equivalent of this type.      */
specifier|public
specifier|static
name|Struct
name|GENERIC
init|=
operator|new
name|StructBuilder
argument_list|()
operator|.
name|add
argument_list|(
name|byteField1
argument_list|)
operator|.
name|add
argument_list|(
name|byteField2
argument_list|)
operator|.
name|add
argument_list|(
name|stringField
argument_list|)
operator|.
name|add
argument_list|(
name|byteField3
argument_list|)
operator|.
name|toStruct
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isOrderPreserving
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Order
name|getOrder
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNullable
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSkippable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|encodedLength
parameter_list|(
name|Pojo2
name|val
parameter_list|)
block|{
return|return
name|byteField1
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|byteField1Asc
argument_list|)
operator|+
name|byteField2
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|byteField2Dsc
argument_list|)
operator|+
name|stringField
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|stringFieldDsc
argument_list|)
operator|+
name|byteField3
operator|.
name|encodedLength
argument_list|(
name|val
operator|.
name|byteField3Dsc
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|Pojo2
argument_list|>
name|encodedClass
parameter_list|()
block|{
return|return
name|Pojo2
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|skip
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
name|int
name|skipped
init|=
name|byteField1
operator|.
name|skip
argument_list|(
name|src
argument_list|)
decl_stmt|;
name|skipped
operator|+=
name|byteField2
operator|.
name|skip
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|skipped
operator|+=
name|stringField
operator|.
name|skip
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|skipped
operator|+=
name|byteField3
operator|.
name|skip
argument_list|(
name|src
argument_list|)
expr_stmt|;
return|return
name|skipped
return|;
block|}
annotation|@
name|Override
specifier|public
name|Pojo2
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
name|Object
index|[]
name|ret
init|=
operator|new
name|Object
index|[
literal|4
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|byteField1
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|ret
index|[
literal|1
index|]
operator|=
name|byteField2
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|ret
index|[
literal|2
index|]
operator|=
name|stringField
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|ret
index|[
literal|3
index|]
operator|=
name|byteField3
operator|.
name|decode
argument_list|(
name|src
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pojo2
argument_list|(
name|ret
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|encode
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|Pojo2
name|val
parameter_list|)
block|{
name|int
name|written
init|=
name|byteField1
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|byteField1Asc
argument_list|)
decl_stmt|;
name|written
operator|+=
name|byteField2
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|byteField2Dsc
argument_list|)
expr_stmt|;
name|written
operator|+=
name|stringField
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|stringFieldDsc
argument_list|)
expr_stmt|;
name|written
operator|+=
name|byteField3
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|val
operator|.
name|byteField3Dsc
argument_list|)
expr_stmt|;
return|return
name|written
return|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|testOrderPreservation
parameter_list|()
throws|throws
name|Exception
block|{
name|Object
index|[]
name|vals
init|=
operator|new
name|Object
index|[
name|constructorArgs
operator|.
name|length
index|]
decl_stmt|;
name|PositionedByteRange
index|[]
name|encodedGeneric
init|=
operator|new
name|PositionedByteRange
index|[
name|constructorArgs
operator|.
name|length
index|]
decl_stmt|;
name|PositionedByteRange
index|[]
name|encodedSpecialized
init|=
operator|new
name|PositionedByteRange
index|[
name|constructorArgs
operator|.
name|length
index|]
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
init|=
name|specialized
operator|.
name|encodedClass
argument_list|()
operator|.
name|getConstructor
argument_list|(
name|Object
index|[]
operator|.
expr|class
argument_list|)
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
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|vals
index|[
name|i
index|]
operator|=
name|ctor
operator|.
name|newInstance
argument_list|(
operator|new
name|Object
index|[]
block|{
name|constructorArgs
index|[
name|i
index|]
block|}
argument_list|)
expr_stmt|;
name|encodedGeneric
index|[
name|i
index|]
operator|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
name|generic
operator|.
name|encodedLength
argument_list|(
name|constructorArgs
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|encodedSpecialized
index|[
name|i
index|]
operator|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
name|specialized
operator|.
name|encodedLength
argument_list|(
name|vals
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// populate our arrays
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|generic
operator|.
name|encode
argument_list|(
name|encodedGeneric
index|[
name|i
index|]
argument_list|,
name|constructorArgs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|encodedGeneric
index|[
name|i
index|]
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|specialized
operator|.
name|encode
argument_list|(
name|encodedSpecialized
index|[
name|i
index|]
argument_list|,
name|vals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|encodedSpecialized
index|[
name|i
index|]
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|encodedGeneric
index|[
name|i
index|]
operator|.
name|getBytes
argument_list|()
argument_list|,
name|encodedSpecialized
index|[
name|i
index|]
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Arrays
operator|.
name|sort
argument_list|(
name|vals
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|encodedGeneric
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|encodedSpecialized
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|"Struct encoder does not preserve sort order at position "
operator|+
name|i
argument_list|,
name|vals
index|[
name|i
index|]
argument_list|,
name|ctor
operator|.
name|newInstance
argument_list|(
operator|new
name|Object
index|[]
block|{
name|generic
operator|.
name|decode
argument_list|(
name|encodedGeneric
index|[
name|i
index|]
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Specialized encoder does not preserve sort order at position "
operator|+
name|i
argument_list|,
name|vals
index|[
name|i
index|]
argument_list|,
name|specialized
operator|.
name|decode
argument_list|(
name|encodedSpecialized
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

