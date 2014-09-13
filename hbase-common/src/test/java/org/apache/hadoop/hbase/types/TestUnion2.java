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
name|assertEquals
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
name|assertTrue
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

begin_class
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
name|TestUnion2
block|{
comment|/**    * An example<code>Union</code>    */
specifier|private
specifier|static
class|class
name|SampleUnion1
extends|extends
name|Union2
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|IS_INTEGER
init|=
literal|0x00
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|IS_STRING
init|=
literal|0x01
decl_stmt|;
specifier|public
name|SampleUnion1
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|RawInteger
argument_list|()
argument_list|,
operator|new
name|RawStringTerminated
argument_list|(
name|Order
operator|.
name|DESCENDING
argument_list|,
literal|"."
argument_list|)
argument_list|)
expr_stmt|;
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
switch|switch
condition|(
name|src
operator|.
name|get
argument_list|()
condition|)
block|{
case|case
name|IS_INTEGER
case|:
return|return
literal|1
operator|+
name|typeA
operator|.
name|skip
argument_list|(
name|src
argument_list|)
return|;
case|case
name|IS_STRING
case|:
return|return
literal|1
operator|+
name|typeB
operator|.
name|skip
argument_list|(
name|src
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unrecognized encoding format."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
block|{
switch|switch
condition|(
name|src
operator|.
name|get
argument_list|()
condition|)
block|{
case|case
name|IS_INTEGER
case|:
return|return
name|typeA
operator|.
name|decode
argument_list|(
name|src
argument_list|)
return|;
case|case
name|IS_STRING
case|:
return|return
name|typeB
operator|.
name|decode
argument_list|(
name|src
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unrecognized encoding format."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|encodedLength
parameter_list|(
name|Object
name|val
parameter_list|)
block|{
name|Integer
name|i
init|=
literal|null
decl_stmt|;
name|String
name|s
init|=
literal|null
decl_stmt|;
try|try
block|{
name|i
operator|=
operator|(
name|Integer
operator|)
name|val
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{}
try|try
block|{
name|s
operator|=
operator|(
name|String
operator|)
name|val
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{}
if|if
condition|(
literal|null
operator|!=
name|i
condition|)
return|return
literal|1
operator|+
name|typeA
operator|.
name|encodedLength
argument_list|(
name|i
argument_list|)
return|;
if|if
condition|(
literal|null
operator|!=
name|s
condition|)
return|return
literal|1
operator|+
name|typeB
operator|.
name|encodedLength
argument_list|(
name|s
argument_list|)
return|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"val is not a valid member of this union."
argument_list|)
throw|;
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
name|Object
name|val
parameter_list|)
block|{
name|Integer
name|i
init|=
literal|null
decl_stmt|;
name|String
name|s
init|=
literal|null
decl_stmt|;
try|try
block|{
name|i
operator|=
operator|(
name|Integer
operator|)
name|val
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{}
try|try
block|{
name|s
operator|=
operator|(
name|String
operator|)
name|val
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{}
if|if
condition|(
literal|null
operator|!=
name|i
condition|)
block|{
name|dst
operator|.
name|put
argument_list|(
name|IS_INTEGER
argument_list|)
expr_stmt|;
return|return
literal|1
operator|+
name|typeA
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|i
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|null
operator|!=
name|s
condition|)
block|{
name|dst
operator|.
name|put
argument_list|(
name|IS_STRING
argument_list|)
expr_stmt|;
return|return
literal|1
operator|+
name|typeB
operator|.
name|encode
argument_list|(
name|dst
argument_list|,
name|s
argument_list|)
return|;
block|}
else|else
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"val is not of a supported type."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEncodeDecode
parameter_list|()
block|{
name|Integer
name|intVal
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|strVal
init|=
literal|"hello"
decl_stmt|;
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|SampleUnion1
name|type
init|=
operator|new
name|SampleUnion1
argument_list|()
decl_stmt|;
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|intVal
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|0
operator|==
name|intVal
operator|.
name|compareTo
argument_list|(
name|type
operator|.
name|decodeA
argument_list|(
name|buff
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|strVal
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|0
operator|==
name|strVal
operator|.
name|compareTo
argument_list|(
name|type
operator|.
name|decodeB
argument_list|(
name|buff
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkip
parameter_list|()
block|{
name|Integer
name|intVal
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|strVal
init|=
literal|"hello"
decl_stmt|;
name|PositionedByteRange
name|buff
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|SampleUnion1
name|type
init|=
operator|new
name|SampleUnion1
argument_list|()
decl_stmt|;
name|int
name|len
init|=
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|intVal
argument_list|)
decl_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|len
argument_list|,
name|type
operator|.
name|skip
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|len
operator|=
name|type
operator|.
name|encode
argument_list|(
name|buff
argument_list|,
name|strVal
argument_list|)
expr_stmt|;
name|buff
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|len
argument_list|,
name|type
operator|.
name|skip
argument_list|(
name|buff
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

