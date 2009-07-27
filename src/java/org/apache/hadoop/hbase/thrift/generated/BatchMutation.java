begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * A BatchMutation object is used to apply a number of Mutations to a single row.  */
end_comment

begin_class
specifier|public
class|class
name|BatchMutation
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
literal|"BatchMutation"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|ROW_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"row"
argument_list|,
name|TType
operator|.
name|STRING
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
name|MUTATIONS_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"mutations"
argument_list|,
name|TType
operator|.
name|LIST
argument_list|,
operator|(
name|short
operator|)
literal|2
argument_list|)
decl_stmt|;
specifier|public
name|byte
index|[]
name|row
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ROW
init|=
literal|1
decl_stmt|;
specifier|public
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MUTATIONS
init|=
literal|2
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
block|{   }
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
name|ROW
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"row"
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
name|MUTATIONS
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"mutations"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|ListMetaData
argument_list|(
name|TType
operator|.
name|LIST
argument_list|,
operator|new
name|StructMetaData
argument_list|(
name|TType
operator|.
name|STRUCT
argument_list|,
name|Mutation
operator|.
name|class
argument_list|)
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
name|BatchMutation
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BatchMutation
parameter_list|()
block|{   }
specifier|public
name|BatchMutation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|mutations
operator|=
name|mutations
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|BatchMutation
parameter_list|(
name|BatchMutation
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetRow
argument_list|()
condition|)
block|{
name|this
operator|.
name|row
operator|=
name|other
operator|.
name|row
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetMutations
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Mutation
argument_list|>
name|__this__mutations
init|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Mutation
name|other_element
range|:
name|other
operator|.
name|mutations
control|)
block|{
name|__this__mutations
operator|.
name|add
argument_list|(
operator|new
name|Mutation
argument_list|(
name|other_element
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|mutations
operator|=
name|__this__mutations
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|BatchMutation
name|clone
parameter_list|()
block|{
return|return
operator|new
name|BatchMutation
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
specifier|public
name|void
name|setRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
specifier|public
name|void
name|unsetRow
parameter_list|()
block|{
name|this
operator|.
name|row
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field row is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setRowIsSet
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
name|row
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getMutationsSize
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|mutations
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|this
operator|.
name|mutations
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|Iterator
argument_list|<
name|Mutation
argument_list|>
name|getMutationsIterator
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|mutations
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|this
operator|.
name|mutations
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|void
name|addToMutations
parameter_list|(
name|Mutation
name|elem
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|mutations
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|mutations
operator|.
name|add
argument_list|(
name|elem
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Mutation
argument_list|>
name|getMutations
parameter_list|()
block|{
return|return
name|this
operator|.
name|mutations
return|;
block|}
specifier|public
name|void
name|setMutations
parameter_list|(
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
parameter_list|)
block|{
name|this
operator|.
name|mutations
operator|=
name|mutations
expr_stmt|;
block|}
specifier|public
name|void
name|unsetMutations
parameter_list|()
block|{
name|this
operator|.
name|mutations
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field mutations is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetMutations
parameter_list|()
block|{
return|return
name|this
operator|.
name|mutations
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setMutationsIsSet
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
name|mutations
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
name|ROW
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetRow
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setRow
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
name|MUTATIONS
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetMutations
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setMutations
argument_list|(
operator|(
name|List
argument_list|<
name|Mutation
argument_list|>
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
name|ROW
case|:
return|return
name|getRow
argument_list|()
return|;
case|case
name|MUTATIONS
case|:
return|return
name|getMutations
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
name|ROW
case|:
return|return
name|isSetRow
argument_list|()
return|;
case|case
name|MUTATIONS
case|:
return|return
name|isSetMutations
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
name|BatchMutation
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|BatchMutation
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
name|BatchMutation
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
name|this_present_row
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetRow
argument_list|()
decl_stmt|;
name|boolean
name|that_present_row
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_row
operator|||
name|that_present_row
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_row
operator|&&
name|that_present_row
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
name|row
argument_list|,
name|that
operator|.
name|row
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_mutations
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetMutations
argument_list|()
decl_stmt|;
name|boolean
name|that_present_mutations
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetMutations
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_mutations
operator|||
name|that_present_mutations
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_mutations
operator|&&
name|that_present_mutations
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|mutations
operator|.
name|equals
argument_list|(
name|that
operator|.
name|mutations
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
name|ROW
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
name|row
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
name|MUTATIONS
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|LIST
condition|)
block|{
block|{
name|TList
name|_list0
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|this
operator|.
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|(
name|_list0
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i1
init|=
literal|0
init|;
name|_i1
operator|<
name|_list0
operator|.
name|size
condition|;
operator|++
name|_i1
control|)
block|{
name|Mutation
name|_elem2
decl_stmt|;
name|_elem2
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|_elem2
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|this
operator|.
name|mutations
operator|.
name|add
argument_list|(
name|_elem2
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
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
if|if
condition|(
name|this
operator|.
name|row
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|ROW_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeBinary
argument_list|(
name|this
operator|.
name|row
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
name|mutations
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|MUTATIONS_FIELD_DESC
argument_list|)
expr_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
argument_list|(
operator|new
name|TList
argument_list|(
name|TType
operator|.
name|STRUCT
argument_list|,
name|this
operator|.
name|mutations
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Mutation
name|_iter3
range|:
name|this
operator|.
name|mutations
control|)
block|{
name|_iter3
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
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
literal|"BatchMutation("
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
literal|"row:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|row
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
name|row
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
literal|"mutations:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|mutations
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
name|mutations
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

