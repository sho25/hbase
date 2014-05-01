begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|access
package|;
end_package

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
name|Map
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
name|KeyValue
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
name|TableName
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
name|security
operator|.
name|User
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

begin_comment
comment|/**  * Represents the result of an authorization check for logging and error  * reporting.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AuthResult
block|{
specifier|private
name|boolean
name|allowed
decl_stmt|;
specifier|private
specifier|final
name|String
name|namespace
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|table
decl_stmt|;
specifier|private
specifier|final
name|Permission
operator|.
name|Action
name|action
decl_stmt|;
specifier|private
specifier|final
name|String
name|request
decl_stmt|;
specifier|private
name|String
name|reason
decl_stmt|;
specifier|private
specifier|final
name|User
name|user
decl_stmt|;
comment|// "family" and "qualifier" should only be used if "families" is null.
specifier|private
specifier|final
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|?
argument_list|>
argument_list|>
name|families
decl_stmt|;
specifier|public
name|AuthResult
parameter_list|(
name|boolean
name|allowed
parameter_list|,
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|this
operator|.
name|allowed
operator|=
name|allowed
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
name|qualifier
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|families
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|namespace
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|AuthResult
parameter_list|(
name|boolean
name|allowed
parameter_list|,
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|?
argument_list|>
argument_list|>
name|families
parameter_list|)
block|{
name|this
operator|.
name|allowed
operator|=
name|allowed
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|family
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|families
operator|=
name|families
expr_stmt|;
name|this
operator|.
name|namespace
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|AuthResult
parameter_list|(
name|boolean
name|allowed
parameter_list|,
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|String
name|namespace
parameter_list|)
block|{
name|this
operator|.
name|allowed
operator|=
name|allowed
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|namespace
operator|=
name|namespace
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|table
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|family
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|families
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAllowed
parameter_list|()
block|{
return|return
name|allowed
return|;
block|}
specifier|public
name|User
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
specifier|public
name|String
name|getReason
parameter_list|()
block|{
return|return
name|reason
return|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|family
return|;
block|}
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|qualifier
return|;
block|}
specifier|public
name|Permission
operator|.
name|Action
name|getAction
parameter_list|()
block|{
return|return
name|action
return|;
block|}
specifier|public
name|String
name|getRequest
parameter_list|()
block|{
return|return
name|request
return|;
block|}
specifier|public
name|void
name|setAllowed
parameter_list|(
name|boolean
name|allowed
parameter_list|)
block|{
name|this
operator|.
name|allowed
operator|=
name|allowed
expr_stmt|;
block|}
specifier|public
name|void
name|setReason
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
block|}
name|String
name|toFamilyString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|families
operator|!=
literal|null
condition|)
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|?
argument_list|>
argument_list|>
name|entry
range|:
name|families
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|familyName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Object
name|o
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|String
name|qualifier
decl_stmt|;
if|if
condition|(
name|o
operator|instanceof
name|byte
index|[]
condition|)
block|{
name|qualifier
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|o
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|KeyValue
condition|)
block|{
name|byte
index|[]
name|rawQualifier
init|=
operator|(
operator|(
name|KeyValue
operator|)
name|o
operator|)
operator|.
name|getQualifier
argument_list|()
decl_stmt|;
name|qualifier
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|rawQualifier
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Shouldn't really reach this?
name|qualifier
operator|=
name|o
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"|"
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
name|familyName
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"|"
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
name|familyName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|family
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|toContextString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"(user="
argument_list|)
operator|.
name|append
argument_list|(
name|user
operator|!=
literal|null
condition|?
name|user
operator|.
name|getName
argument_list|()
else|:
literal|"UNKNOWN"
argument_list|)
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
literal|"scope="
argument_list|)
operator|.
name|append
argument_list|(
name|namespace
operator|!=
literal|null
condition|?
name|namespace
else|:
name|table
operator|==
literal|null
condition|?
literal|"GLOBAL"
else|:
name|table
argument_list|)
expr_stmt|;
if|if
condition|(
name|namespace
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
literal|"family="
argument_list|)
operator|.
name|append
argument_list|(
name|toFamilyString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"action="
argument_list|)
operator|.
name|append
argument_list|(
name|action
operator|!=
literal|null
condition|?
name|action
operator|.
name|toString
argument_list|()
else|:
literal|""
argument_list|)
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
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AuthResult"
operator|+
name|toContextString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|AuthResult
name|allow
parameter_list|(
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|String
name|namespace
parameter_list|)
block|{
return|return
operator|new
name|AuthResult
argument_list|(
literal|true
argument_list|,
name|request
argument_list|,
name|reason
argument_list|,
name|user
argument_list|,
name|action
argument_list|,
name|namespace
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AuthResult
name|allow
parameter_list|(
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
return|return
operator|new
name|AuthResult
argument_list|(
literal|true
argument_list|,
name|request
argument_list|,
name|reason
argument_list|,
name|user
argument_list|,
name|action
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AuthResult
name|allow
parameter_list|(
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|?
argument_list|>
argument_list|>
name|families
parameter_list|)
block|{
return|return
operator|new
name|AuthResult
argument_list|(
literal|true
argument_list|,
name|request
argument_list|,
name|reason
argument_list|,
name|user
argument_list|,
name|action
argument_list|,
name|table
argument_list|,
name|families
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AuthResult
name|deny
parameter_list|(
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|String
name|namespace
parameter_list|)
block|{
return|return
operator|new
name|AuthResult
argument_list|(
literal|false
argument_list|,
name|request
argument_list|,
name|reason
argument_list|,
name|user
argument_list|,
name|action
argument_list|,
name|namespace
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AuthResult
name|deny
parameter_list|(
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
return|return
operator|new
name|AuthResult
argument_list|(
literal|false
argument_list|,
name|request
argument_list|,
name|reason
argument_list|,
name|user
argument_list|,
name|action
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AuthResult
name|deny
parameter_list|(
name|String
name|request
parameter_list|,
name|String
name|reason
parameter_list|,
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|?
argument_list|>
argument_list|>
name|families
parameter_list|)
block|{
return|return
operator|new
name|AuthResult
argument_list|(
literal|false
argument_list|,
name|request
argument_list|,
name|reason
argument_list|,
name|user
argument_list|,
name|action
argument_list|,
name|table
argument_list|,
name|families
argument_list|)
return|;
block|}
block|}
end_class

end_unit

