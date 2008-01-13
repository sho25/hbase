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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
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
name|HashSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A RegionDescriptor contains informationa about an HTable region.  * Currently, this is just the startKey of the region.  */
end_comment

begin_class
specifier|public
class|class
name|RegionDescriptor
implements|implements
name|TBase
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
specifier|public
name|byte
index|[]
name|startKey
decl_stmt|;
specifier|public
specifier|final
name|Isset
name|__isset
init|=
operator|new
name|Isset
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|Isset
block|{
specifier|public
name|boolean
name|startKey
init|=
literal|false
decl_stmt|;
block|}
specifier|public
name|RegionDescriptor
parameter_list|()
block|{   }
specifier|public
name|RegionDescriptor
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|startKey
operator|=
literal|true
expr_stmt|;
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
literal|1
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
name|startKey
operator|=
name|iprot
operator|.
name|readBinary
argument_list|()
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|startKey
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
name|TStruct
name|struct
init|=
operator|new
name|TStruct
argument_list|(
literal|"RegionDescriptor"
argument_list|)
decl_stmt|;
name|oprot
operator|.
name|writeStructBegin
argument_list|(
name|struct
argument_list|)
expr_stmt|;
name|TField
name|field
init|=
operator|new
name|TField
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|startKey
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"startKey"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|STRING
expr_stmt|;
name|field
operator|.
name|id
operator|=
literal|1
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeBinary
argument_list|(
name|this
operator|.
name|startKey
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
literal|"RegionDescriptor("
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"startKey:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|startKey
argument_list|)
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
block|}
end_class

end_unit

