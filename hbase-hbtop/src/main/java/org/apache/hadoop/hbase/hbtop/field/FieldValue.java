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
name|hbtop
operator|.
name|field
package|;
end_package

begin_import
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|NonNull
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
name|Size
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

begin_comment
comment|/**  * Represents a value of a field.  *  * The type of a value is defined by {@link FieldValue}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|FieldValue
implements|implements
name|Comparable
argument_list|<
name|FieldValue
argument_list|>
block|{
specifier|private
specifier|final
name|Object
name|value
decl_stmt|;
specifier|private
specifier|final
name|FieldValueType
name|type
decl_stmt|;
name|FieldValue
parameter_list|(
name|Object
name|value
parameter_list|,
name|FieldValueType
name|type
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
break|break;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
case|case
name|INTEGER
case|:
if|if
condition|(
name|value
operator|instanceof
name|Integer
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
case|case
name|LONG
case|:
if|if
condition|(
name|value
operator|instanceof
name|Long
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
case|case
name|FLOAT
case|:
if|if
condition|(
name|value
operator|instanceof
name|Float
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|Float
operator|.
name|valueOf
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
case|case
name|SIZE
case|:
if|if
condition|(
name|value
operator|instanceof
name|Size
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|optimizeSize
argument_list|(
operator|(
name|Size
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|optimizeSize
argument_list|(
name|parseSizeString
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
case|case
name|PERCENT
case|:
if|if
condition|(
name|value
operator|instanceof
name|Float
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|parsePercentString
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
specifier|private
name|Size
name|optimizeSize
parameter_list|(
name|Size
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|BYTE
argument_list|)
operator|<
literal|1024d
condition|)
block|{
return|return
name|size
operator|.
name|getUnit
argument_list|()
operator|==
name|Size
operator|.
name|Unit
operator|.
name|BYTE
condition|?
name|size
else|:
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|BYTE
argument_list|)
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|BYTE
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
operator|<
literal|1024d
condition|)
block|{
return|return
name|size
operator|.
name|getUnit
argument_list|()
operator|==
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
condition|?
name|size
else|:
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|KILOBYTE
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
operator|<
literal|1024d
condition|)
block|{
return|return
name|size
operator|.
name|getUnit
argument_list|()
operator|==
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
condition|?
name|size
else|:
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|MEGABYTE
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|GIGABYTE
argument_list|)
operator|<
literal|1024d
condition|)
block|{
return|return
name|size
operator|.
name|getUnit
argument_list|()
operator|==
name|Size
operator|.
name|Unit
operator|.
name|GIGABYTE
condition|?
name|size
else|:
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|GIGABYTE
argument_list|)
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|GIGABYTE
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|TERABYTE
argument_list|)
operator|<
literal|1024d
condition|)
block|{
return|return
name|size
operator|.
name|getUnit
argument_list|()
operator|==
name|Size
operator|.
name|Unit
operator|.
name|TERABYTE
condition|?
name|size
else|:
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|TERABYTE
argument_list|)
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|TERABYTE
argument_list|)
return|;
block|}
return|return
name|size
operator|.
name|getUnit
argument_list|()
operator|==
name|Size
operator|.
name|Unit
operator|.
name|PETABYTE
condition|?
name|size
else|:
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|Size
operator|.
name|Unit
operator|.
name|PETABYTE
argument_list|)
argument_list|,
name|Size
operator|.
name|Unit
operator|.
name|PETABYTE
argument_list|)
return|;
block|}
specifier|private
name|Size
name|parseSizeString
parameter_list|(
name|String
name|sizeString
parameter_list|)
block|{
if|if
condition|(
name|sizeString
operator|.
name|length
argument_list|()
operator|<
literal|3
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid size"
argument_list|)
throw|;
block|}
name|String
name|valueString
init|=
name|sizeString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sizeString
operator|.
name|length
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
name|String
name|unitSimpleName
init|=
name|sizeString
operator|.
name|substring
argument_list|(
name|sizeString
operator|.
name|length
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
return|return
operator|new
name|Size
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|valueString
argument_list|)
argument_list|,
name|convertToUnit
argument_list|(
name|unitSimpleName
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Size
operator|.
name|Unit
name|convertToUnit
parameter_list|(
name|String
name|unitSimpleName
parameter_list|)
block|{
for|for
control|(
name|Size
operator|.
name|Unit
name|unit
range|:
name|Size
operator|.
name|Unit
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|unitSimpleName
operator|.
name|equals
argument_list|(
name|unit
operator|.
name|getSimpleName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|unit
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid size"
argument_list|)
throw|;
block|}
specifier|private
name|Float
name|parsePercentString
parameter_list|(
name|String
name|percentString
parameter_list|)
block|{
if|if
condition|(
name|percentString
operator|.
name|endsWith
argument_list|(
literal|"%"
argument_list|)
condition|)
block|{
name|percentString
operator|=
name|percentString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|percentString
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|Float
operator|.
name|valueOf
argument_list|(
name|percentString
argument_list|)
return|;
block|}
specifier|public
name|String
name|asString
parameter_list|()
block|{
return|return
name|toString
argument_list|()
return|;
block|}
specifier|public
name|int
name|asInt
parameter_list|()
block|{
return|return
operator|(
name|Integer
operator|)
name|value
return|;
block|}
specifier|public
name|long
name|asLong
parameter_list|()
block|{
return|return
operator|(
name|Long
operator|)
name|value
return|;
block|}
specifier|public
name|float
name|asFloat
parameter_list|()
block|{
return|return
operator|(
name|Float
operator|)
name|value
return|;
block|}
specifier|public
name|Size
name|asSize
parameter_list|()
block|{
return|return
operator|(
name|Size
operator|)
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
case|case
name|INTEGER
case|:
case|case
name|LONG
case|:
case|case
name|FLOAT
case|:
case|case
name|SIZE
case|:
return|return
name|value
operator|.
name|toString
argument_list|()
return|;
case|case
name|PERCENT
case|:
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2f"
argument_list|,
operator|(
name|Float
operator|)
name|value
argument_list|)
operator|+
literal|"%"
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
annotation|@
name|NonNull
name|FieldValue
name|o
parameter_list|)
block|{
if|if
condition|(
name|type
operator|!=
name|o
operator|.
name|type
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
return|return
operator|(
operator|(
name|String
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|String
operator|)
name|o
operator|.
name|value
argument_list|)
return|;
case|case
name|INTEGER
case|:
return|return
operator|(
operator|(
name|Integer
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Integer
operator|)
name|o
operator|.
name|value
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Long
operator|)
name|o
operator|.
name|value
argument_list|)
return|;
case|case
name|FLOAT
case|:
case|case
name|PERCENT
case|:
return|return
operator|(
operator|(
name|Float
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Float
operator|)
name|o
operator|.
name|value
argument_list|)
return|;
case|case
name|SIZE
case|:
return|return
operator|(
operator|(
name|Size
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Size
operator|)
name|o
operator|.
name|value
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|FieldValue
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FieldValue
name|that
init|=
operator|(
name|FieldValue
operator|)
name|o
decl_stmt|;
return|return
name|value
operator|.
name|equals
argument_list|(
name|that
operator|.
name|value
argument_list|)
operator|&&
name|type
operator|==
name|that
operator|.
name|type
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
name|value
argument_list|,
name|type
argument_list|)
return|;
block|}
specifier|public
name|FieldValue
name|plus
parameter_list|(
name|FieldValue
name|o
parameter_list|)
block|{
if|if
condition|(
name|type
operator|!=
name|o
operator|.
name|type
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
return|return
operator|new
name|FieldValue
argument_list|(
operator|(
operator|(
name|String
operator|)
name|value
operator|)
operator|.
name|concat
argument_list|(
operator|(
name|String
operator|)
name|o
operator|.
name|value
argument_list|)
argument_list|,
name|type
argument_list|)
return|;
case|case
name|INTEGER
case|:
return|return
operator|new
name|FieldValue
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|value
operator|)
operator|+
operator|(
operator|(
name|Integer
operator|)
name|o
operator|.
name|value
operator|)
argument_list|,
name|type
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|FieldValue
argument_list|(
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|+
operator|(
operator|(
name|Long
operator|)
name|o
operator|.
name|value
operator|)
argument_list|,
name|type
argument_list|)
return|;
case|case
name|FLOAT
case|:
case|case
name|PERCENT
case|:
return|return
operator|new
name|FieldValue
argument_list|(
operator|(
operator|(
name|Float
operator|)
name|value
operator|)
operator|+
operator|(
operator|(
name|Float
operator|)
name|o
operator|.
name|value
operator|)
argument_list|,
name|type
argument_list|)
return|;
case|case
name|SIZE
case|:
name|Size
name|size
init|=
operator|(
name|Size
operator|)
name|value
decl_stmt|;
name|Size
name|oSize
init|=
operator|(
name|Size
operator|)
name|o
operator|.
name|value
decl_stmt|;
name|Size
operator|.
name|Unit
name|unit
init|=
name|size
operator|.
name|getUnit
argument_list|()
decl_stmt|;
return|return
operator|new
name|FieldValue
argument_list|(
operator|new
name|Size
argument_list|(
name|size
operator|.
name|get
argument_list|(
name|unit
argument_list|)
operator|+
name|oSize
operator|.
name|get
argument_list|(
name|unit
argument_list|)
argument_list|,
name|unit
argument_list|)
argument_list|,
name|type
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
specifier|public
name|int
name|compareToIgnoreCase
parameter_list|(
name|FieldValue
name|o
parameter_list|)
block|{
if|if
condition|(
name|type
operator|!=
name|o
operator|.
name|type
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid type"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
return|return
operator|(
operator|(
name|String
operator|)
name|value
operator|)
operator|.
name|compareToIgnoreCase
argument_list|(
operator|(
name|String
operator|)
name|o
operator|.
name|value
argument_list|)
return|;
case|case
name|INTEGER
case|:
case|case
name|LONG
case|:
case|case
name|FLOAT
case|:
case|case
name|SIZE
case|:
case|case
name|PERCENT
case|:
return|return
name|compareTo
argument_list|(
name|o
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

