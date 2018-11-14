begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CellUtil
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
name|yetus
operator|.
name|audience
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Represents an authorization for access for the given actions, optionally  * restricted to the given column family or column qualifier, over the  * given table. If the family property is<code>null</code>, it implies  * full table access.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TablePermission
extends|extends
name|Permission
block|{
specifier|private
name|TableName
name|table
decl_stmt|;
specifier|private
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
comment|/** Nullary constructor for Writable, do not use */
specifier|public
name|TablePermission
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|scope
operator|=
name|Scope
operator|.
name|EMPTY
expr_stmt|;
block|}
comment|/**    * Construct a table permission.    * @param table table name    * @param assigned assigned actions    */
specifier|public
name|TablePermission
parameter_list|(
name|TableName
name|table
parameter_list|,
name|Action
modifier|...
name|assigned
parameter_list|)
block|{
name|this
argument_list|(
name|table
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|assigned
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a table:family permission.    * @param table table name    * @param family family name    * @param assigned assigned actions    */
specifier|public
name|TablePermission
parameter_list|(
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Action
modifier|...
name|assigned
parameter_list|)
block|{
name|this
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|assigned
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a table:family:qualifier permission.    * @param table table name    * @param family family name    * @param qualifier qualifier name    * @param assigned assigned actions    */
specifier|public
name|TablePermission
parameter_list|(
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
parameter_list|,
name|Action
modifier|...
name|assigned
parameter_list|)
block|{
name|super
argument_list|(
name|assigned
argument_list|)
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
name|scope
operator|=
name|Scope
operator|.
name|TABLE
expr_stmt|;
block|}
comment|/**    * Construct a table permission.    * @param table table name    * @param actionCodes assigned actions    */
specifier|public
name|TablePermission
parameter_list|(
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|actionCodes
parameter_list|)
block|{
name|this
argument_list|(
name|table
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionCodes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a table:family permission.    * @param table table name    * @param family family name    * @param actionCodes assigned actions    */
specifier|public
name|TablePermission
parameter_list|(
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|actionCodes
parameter_list|)
block|{
name|this
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|actionCodes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a table:family:qualifier permission.    * @param table table name    * @param family family name    * @param qualifier qualifier name    * @param actionCodes assigned actions    */
specifier|public
name|TablePermission
parameter_list|(
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
parameter_list|,
name|byte
index|[]
name|actionCodes
parameter_list|)
block|{
name|super
argument_list|(
name|actionCodes
argument_list|)
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
name|scope
operator|=
name|Scope
operator|.
name|TABLE
expr_stmt|;
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
name|void
name|setTableName
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasFamily
parameter_list|()
block|{
return|return
name|family
operator|!=
literal|null
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
name|boolean
name|hasQualifier
parameter_list|()
block|{
return|return
name|qualifier
operator|!=
literal|null
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
name|String
name|getNamespace
parameter_list|()
block|{
return|return
name|table
operator|.
name|getNamespaceAsString
argument_list|()
return|;
block|}
comment|/**    * Check if given action can performs on given table:family:qualifier.    * @param table table name    * @param family family name    * @param qualifier qualifier name    * @param action one of [Read, Write, Create, Exec, Admin]    * @return true if can, false otherwise    */
specifier|public
name|boolean
name|implies
parameter_list|(
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
parameter_list|,
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|failCheckTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|failCheckFamily
argument_list|(
name|family
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|failCheckQualifier
argument_list|(
name|qualifier
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|implies
argument_list|(
name|action
argument_list|)
return|;
block|}
comment|/**    * Check if given action can performs on given table:family.    * @param table table name    * @param family family name    * @param action one of [Read, Write, Create, Exec, Admin]    * @return true if can, false otherwise    */
specifier|public
name|boolean
name|implies
parameter_list|(
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|failCheckTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|failCheckFamily
argument_list|(
name|family
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|implies
argument_list|(
name|action
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|failCheckTable
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
return|return
name|this
operator|.
name|table
operator|==
literal|null
operator|||
operator|!
name|this
operator|.
name|table
operator|.
name|equals
argument_list|(
name|table
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|failCheckFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
name|this
operator|.
name|family
operator|!=
literal|null
operator|&&
operator|(
name|family
operator|==
literal|null
operator|||
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|family
argument_list|,
name|family
argument_list|)
operator|)
return|;
block|}
specifier|private
name|boolean
name|failCheckQualifier
parameter_list|(
name|byte
index|[]
name|qual
parameter_list|)
block|{
return|return
name|this
operator|.
name|qualifier
operator|!=
literal|null
operator|&&
operator|(
name|qual
operator|==
literal|null
operator|||
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|qualifier
argument_list|,
name|qual
argument_list|)
operator|)
return|;
block|}
comment|/**    * Checks if this permission grants access to perform the given action on    * the given table and key value.    * @param table the table on which the operation is being performed    * @param kv the KeyValue on which the operation is being requested    * @param action the action requested    * @return<code>true</code> if the action is allowed over the given scope    *   by this permission, otherwise<code>false</code>    */
specifier|public
name|boolean
name|implies
parameter_list|(
name|TableName
name|table
parameter_list|,
name|KeyValue
name|kv
parameter_list|,
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|failCheckTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|family
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|kv
argument_list|,
name|family
argument_list|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|qualifier
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|kv
argument_list|,
name|qualifier
argument_list|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// check actions
return|return
name|super
operator|.
name|implies
argument_list|(
name|action
argument_list|)
return|;
block|}
comment|/**    * Check if fields of table in table permission equals.    * @param tp to be checked table permission    * @return true if equals, false otherwise    */
specifier|public
name|boolean
name|tableFieldsEqual
parameter_list|(
name|TablePermission
name|tp
parameter_list|)
block|{
if|if
condition|(
name|tp
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|tEq
init|=
operator|(
name|table
operator|==
literal|null
operator|&&
name|tp
operator|.
name|table
operator|==
literal|null
operator|)
operator|||
operator|(
name|table
operator|!=
literal|null
operator|&&
name|table
operator|.
name|equals
argument_list|(
name|tp
operator|.
name|table
argument_list|)
operator|)
decl_stmt|;
name|boolean
name|fEq
init|=
operator|(
name|family
operator|==
literal|null
operator|&&
name|tp
operator|.
name|family
operator|==
literal|null
operator|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|family
argument_list|,
name|tp
operator|.
name|family
argument_list|)
decl_stmt|;
name|boolean
name|qEq
init|=
operator|(
name|qualifier
operator|==
literal|null
operator|&&
name|tp
operator|.
name|qualifier
operator|==
literal|null
operator|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|qualifier
argument_list|,
name|tp
operator|.
name|qualifier
argument_list|)
decl_stmt|;
return|return
name|tEq
operator|&&
name|fEq
operator|&&
name|qEq
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equalsExceptActions
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|TablePermission
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TablePermission
name|other
init|=
operator|(
name|TablePermission
operator|)
name|obj
decl_stmt|;
return|return
name|tableFieldsEqual
argument_list|(
name|other
argument_list|)
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
return|return
name|equalsExceptActions
argument_list|(
name|obj
argument_list|)
operator|&&
name|super
operator|.
name|equals
argument_list|(
name|obj
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
literal|37
decl_stmt|;
name|int
name|result
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|table
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|family
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Bytes
operator|.
name|hashCode
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Bytes
operator|.
name|hashCode
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[TablePermission: "
operator|+
name|rawExpression
argument_list|()
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|rawExpression
parameter_list|()
block|{
name|StringBuilder
name|raw
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|raw
operator|.
name|append
argument_list|(
literal|"table="
argument_list|)
operator|.
name|append
argument_list|(
name|table
argument_list|)
operator|.
name|append
argument_list|(
literal|", family="
argument_list|)
operator|.
name|append
argument_list|(
name|family
operator|==
literal|null
condition|?
literal|null
else|:
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|", qualifier="
argument_list|)
operator|.
name|append
argument_list|(
name|qualifier
operator|==
literal|null
condition|?
literal|null
else|:
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
return|return
name|raw
operator|.
name|toString
argument_list|()
operator|+
name|super
operator|.
name|rawExpression
argument_list|()
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
name|byte
index|[]
name|tableBytes
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableBytes
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|table
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableBytes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|family
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|qualifier
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
comment|// Explicitly writing null to maintain se/deserialize backward compatibility.
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|table
operator|==
literal|null
condition|?
literal|null
else|:
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|family
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|family
operator|!=
literal|null
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|qualifier
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

