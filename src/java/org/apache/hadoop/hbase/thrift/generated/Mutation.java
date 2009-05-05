begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * Autogenerated by Thrift  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  */
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
name|thrift
operator|.
name|generated
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A Mutation object is used to either update or delete a column-value.  */
end_comment

begin_class
specifier|public
class|class
name|Mutation
implements|implements
name|TBase
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
implements|,
name|Cloneable
block|{
specifier|private
specifier|static
specifier|final
name|TStruct
name|STRUCT_DESC
init|=
operator|new
name|TStruct
argument_list|(
literal|"Mutation"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|IS_DELETE_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"isDelete"
argument_list|,
name|TType
operator|.
name|BOOL
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|COLUMN_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"column"
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
operator|(
name|short
operator|)
literal|2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|VALUE_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"value"
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
operator|(
name|short
operator|)
literal|3
argument_list|)
decl_stmt|;
specifier|public
name|boolean
name|isDelete
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ISDELETE
init|=
literal|1
decl_stmt|;
specifier|public
name|byte
index|[]
name|column
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|COLUMN
init|=
literal|2
decl_stmt|;
specifier|public
name|byte
index|[]
name|value
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|VALUE
init|=
literal|3
decl_stmt|;
specifier|private
specifier|final
name|Isset
name|__isset
init|=
operator|new
name|Isset
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|Isset
implements|implements
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
specifier|public
name|boolean
name|isDelete
init|=
literal|false
decl_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|FieldMetaData
argument_list|>
name|metaDataMap
init|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|FieldMetaData
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|ISDELETE
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"isDelete"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|BOOL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|COLUMN
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"column"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|VALUE
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"value"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
static|static
block|{
name|FieldMetaData
operator|.
name|addStructMetaDataMap
argument_list|(
name|Mutation
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Mutation
parameter_list|()
block|{
name|this
operator|.
name|isDelete
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|Mutation
parameter_list|(
name|boolean
name|isDelete
parameter_list|,
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|isDelete
operator|=
name|isDelete
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|Mutation
parameter_list|(
name|Mutation
name|other
parameter_list|)
block|{
name|__isset
operator|.
name|isDelete
operator|=
name|other
operator|.
name|__isset
operator|.
name|isDelete
expr_stmt|;
name|this
operator|.
name|isDelete
operator|=
name|other
operator|.
name|isDelete
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|isSetColumn
argument_list|()
condition|)
block|{
name|this
operator|.
name|column
operator|=
name|other
operator|.
name|column
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|other
operator|.
name|value
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Mutation
name|clone
parameter_list|()
block|{
return|return
operator|new
name|Mutation
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isIsDelete
parameter_list|()
block|{
return|return
name|this
operator|.
name|isDelete
return|;
block|}
specifier|public
name|void
name|setIsDelete
parameter_list|(
name|boolean
name|isDelete
parameter_list|)
block|{
name|this
operator|.
name|isDelete
operator|=
name|isDelete
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetIsDelete
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|isDelete
operator|=
literal|false
expr_stmt|;
block|}
comment|// Returns true if field isDelete is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetIsDelete
parameter_list|()
block|{
return|return
name|this
operator|.
name|__isset
operator|.
name|isDelete
return|;
block|}
specifier|public
name|void
name|setIsDeleteIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|__isset
operator|.
name|isDelete
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|column
return|;
block|}
specifier|public
name|void
name|setColumn
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
specifier|public
name|void
name|unsetColumn
parameter_list|()
block|{
name|this
operator|.
name|column
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field column is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|column
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setColumnIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
name|this
operator|.
name|column
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|value
return|;
block|}
specifier|public
name|void
name|setValue
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
name|void
name|unsetValue
parameter_list|()
block|{
name|this
operator|.
name|value
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field value is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|value
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setValueIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
name|this
operator|.
name|value
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setFieldValue
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|fieldID
condition|)
block|{
case|case
name|ISDELETE
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetIsDelete
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setIsDelete
argument_list|(
operator|(
name|Boolean
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|COLUMN
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetColumn
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setColumn
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|VALUE
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setValue
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldID
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Object
name|getFieldValue
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
switch|switch
condition|(
name|fieldID
condition|)
block|{
case|case
name|ISDELETE
case|:
return|return
operator|new
name|Boolean
argument_list|(
name|isIsDelete
argument_list|()
argument_list|)
return|;
case|case
name|COLUMN
case|:
return|return
name|getColumn
argument_list|()
return|;
case|case
name|VALUE
case|:
return|return
name|getValue
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldID
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
block|}
block|}
comment|// Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSet
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
switch|switch
condition|(
name|fieldID
condition|)
block|{
case|case
name|ISDELETE
case|:
return|return
name|isSetIsDelete
argument_list|()
return|;
case|case
name|COLUMN
case|:
return|return
name|isSetColumn
argument_list|()
return|;
case|case
name|VALUE
case|:
return|return
name|isSetValue
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldID
operator|+
literal|" doesn't exist!"
argument_list|)
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
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|that
operator|instanceof
name|Mutation
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|Mutation
operator|)
name|that
argument_list|)
return|;
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Mutation
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|boolean
name|this_present_isDelete
init|=
literal|true
decl_stmt|;
name|boolean
name|that_present_isDelete
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this_present_isDelete
operator|||
name|that_present_isDelete
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_isDelete
operator|&&
name|that_present_isDelete
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|isDelete
operator|!=
name|that
operator|.
name|isDelete
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_column
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetColumn
argument_list|()
decl_stmt|;
name|boolean
name|that_present_column
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetColumn
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_column
operator|||
name|that_present_column
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_column
operator|&&
name|that_present_column
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|column
argument_list|,
name|that
operator|.
name|column
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_value
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetValue
argument_list|()
decl_stmt|;
name|boolean
name|that_present_value
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_value
operator|||
name|that_present_value
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_value
operator|&&
name|that_present_value
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|value
argument_list|,
name|that
operator|.
name|value
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
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
literal|0
return|;
block|}
specifier|public
name|void
name|read
parameter_list|(
name|TProtocol
name|iprot
parameter_list|)
throws|throws
name|TException
block|{
name|TField
name|field
decl_stmt|;
name|iprot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|field
operator|=
name|iprot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STOP
condition|)
block|{
break|break;
block|}
switch|switch
condition|(
name|field
operator|.
name|id
condition|)
block|{
case|case
name|ISDELETE
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|BOOL
condition|)
block|{
name|this
operator|.
name|isDelete
operator|=
name|iprot
operator|.
name|readBool
argument_list|()
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|COLUMN
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRING
condition|)
block|{
name|this
operator|.
name|column
operator|=
name|iprot
operator|.
name|readBinary
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|VALUE
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRING
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|iprot
operator|.
name|readBinary
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
break|break;
block|}
name|iprot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
block|}
name|iprot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
comment|// check for required fields of primitive type, which can't be checked in the validate method
name|validate
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|TException
block|{
name|validate
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructBegin
argument_list|(
name|STRUCT_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|IS_DELETE_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeBool
argument_list|(
name|this
operator|.
name|isDelete
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|column
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|COLUMN_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeBinary
argument_list|(
name|this
operator|.
name|column
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|value
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|VALUE_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeBinary
argument_list|(
name|this
operator|.
name|value
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldStop
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructEnd
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Mutation("
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"isDelete:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|isDelete
argument_list|)
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"column:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|column
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|column
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"value:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|value
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|validate
parameter_list|()
throws|throws
name|TException
block|{
comment|// check for required fields
comment|// check that fields of type enum have valid values
block|}
block|}
end_class

end_unit

