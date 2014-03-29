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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|util
operator|.
name|Bytes
import|;
end_import

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
comment|/**  * Represents an authorization for access for the given actions, optionally  * restricted to the given column family or column qualifier, over the  * given table.  If the family property is<code>null</code>, it implies  * full table access.  */
end_comment

begin_class
specifier|public
class|class
name|TablePermission
extends|extends
name|Permission
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TablePermission
operator|.
name|class
argument_list|)
decl_stmt|;
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
comment|//TODO refactor this class
comment|//we need to refacting this into three classes (Global, Table, Namespace)
specifier|private
name|String
name|namespace
decl_stmt|;
comment|/** Nullary constructor for Writable, do not use */
specifier|public
name|TablePermission
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create a new permission for the given table and (optionally) column family,    * allowing the given actions.    * @param table the table    * @param family the family, can be null if a global permission on the table    * @param assigned the list of allowed actions    */
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
comment|/**    * Creates a new permission for the given table, restricted to the given    * column family and qualifier, allowing the assigned actions to be performed.    * @param table the table    * @param family the family, can be null if a global permission on the table    * @param assigned the list of allowed actions    */
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
block|}
comment|/**    * Creates a new permission for the given table, family and column qualifier,    * allowing the actions matching the provided byte codes to be performed.    * @param table the table    * @param family the family, can be null if a global permission on the table    * @param actionCodes the list of allowed action codes    */
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
block|}
comment|/**    * Creates a new permission for the given namespace or table, restricted to the given    * column family and qualifier, allowing the assigned actions to be performed.    * @param namespace    * @param table the table    * @param family the family, can be null if a global permission on the table    * @param assigned the list of allowed actions    */
specifier|public
name|TablePermission
parameter_list|(
name|String
name|namespace
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
name|namespace
operator|=
name|namespace
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
block|}
comment|/**    * Creates a new permission for the given namespace or table, family and column qualifier,    * allowing the actions matching the provided byte codes to be performed.    * @param namespace    * @param table the table    * @param family the family, can be null if a global permission on the table    * @param actionCodes the list of allowed action codes    */
specifier|public
name|TablePermission
parameter_list|(
name|String
name|namespace
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
name|namespace
operator|=
name|namespace
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
block|}
comment|/**    * Creates a new permission for the given namespace,    * allowing the actions matching the provided byte codes to be performed.    * @param namespace    * @param actionCodes the list of allowed action codes    */
specifier|public
name|TablePermission
parameter_list|(
name|String
name|namespace
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
name|namespace
operator|=
name|namespace
expr_stmt|;
block|}
comment|/**    * Create a new permission for the given namespace,    * allowing the given actions.    * @param namespace    * @param assigned the list of allowed actions    */
specifier|public
name|TablePermission
parameter_list|(
name|String
name|namespace
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
name|namespace
operator|=
name|namespace
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasTable
parameter_list|()
block|{
return|return
name|table
operator|!=
literal|null
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
name|boolean
name|hasNamespace
parameter_list|()
block|{
return|return
name|namespace
operator|!=
literal|null
return|;
block|}
specifier|public
name|String
name|getNamespace
parameter_list|()
block|{
return|return
name|namespace
return|;
block|}
comment|/**    * Checks that a given table operation is authorized by this permission    * instance.    *    * @param namespace the namespace where the operation is being performed    * @param action the action being requested    * @return<code>true</code> if the action within the given scope is allowed    *   by this permission,<code>false</code>    */
specifier|public
name|boolean
name|implies
parameter_list|(
name|String
name|namespace
parameter_list|,
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|namespace
operator|.
name|equals
argument_list|(
name|namespace
argument_list|)
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
comment|/**    * Checks that a given table operation is authorized by this permission    * instance.    *    * @param table the table where the operation is being performed    * @param family the column family to which the operation is restricted,    *   if<code>null</code> implies "all"    * @param qualifier the column qualifier to which the action is restricted,    *   if<code>null</code> implies "all"    * @param action the action being requested    * @return<code>true</code> if the action within the given scope is allowed    *   by this permission,<code>false</code>    */
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
operator|!
name|this
operator|.
name|table
operator|.
name|equals
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
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|.
name|qualifier
operator|!=
literal|null
operator|&&
operator|(
name|qualifier
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
operator|!
name|this
operator|.
name|table
operator|.
name|equals
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
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|family
argument_list|,
literal|0
argument_list|,
name|family
operator|.
name|length
argument_list|,
name|kv
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
operator|!=
literal|0
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
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|qualifier
argument_list|,
literal|0
argument_list|,
name|qualifier
operator|.
name|length
argument_list|,
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
operator|!=
literal|0
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
comment|/**    * Returns<code>true</code> if this permission matches the given column    * family at least.  This only indicates a partial match against the table    * and column family, however, and does not guarantee that implies() for the    * column same family would return<code>true</code>.  In the case of a    * column-qualifier specific permission, for example, implies() would still    * return false.    */
specifier|public
name|boolean
name|matchesFamily
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
operator|!
name|this
operator|.
name|table
operator|.
name|equals
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
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// ignore qualifier
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
comment|/**    * Returns if the given permission matches the given qualifier.    * @param table the table name to match    * @param family the column family to match    * @param qualifier the qualifier name to match    * @param action the action requested    * @return<code>true</code> if the table, family and qualifier match,    *   otherwise<code>false</code>    */
specifier|public
name|boolean
name|matchesFamilyQualifier
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
operator|!
name|matchesFamily
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
if|if
condition|(
name|this
operator|.
name|qualifier
operator|!=
literal|null
operator|&&
operator|(
name|qualifier
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
name|qualifier
argument_list|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
name|super
operator|.
name|implies
argument_list|(
name|action
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
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
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"NP_NULL_ON_SOME_PATH"
argument_list|,
name|justification
operator|=
literal|"Passed on construction except on constructor not to be used"
argument_list|)
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
if|if
condition|(
operator|!
operator|(
name|table
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|&&
operator|(
operator|(
name|family
operator|==
literal|null
operator|&&
name|other
operator|.
name|getFamily
argument_list|()
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
name|other
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|)
operator|&&
operator|(
operator|(
name|qualifier
operator|==
literal|null
operator|&&
name|other
operator|.
name|getQualifier
argument_list|()
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
name|other
operator|.
name|getQualifier
argument_list|()
argument_list|)
operator|)
operator|&&
operator|(
operator|(
name|namespace
operator|==
literal|null
operator|&&
name|other
operator|.
name|getNamespace
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|namespace
operator|!=
literal|null
operator|&&
name|namespace
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getNamespace
argument_list|()
argument_list|)
operator|)
operator|)
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
name|equals
argument_list|(
name|other
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
if|if
condition|(
name|namespace
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
name|namespace
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|str
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"[TablePermission: "
argument_list|)
decl_stmt|;
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
name|str
operator|.
name|append
argument_list|(
literal|"namespace="
argument_list|)
operator|.
name|append
argument_list|(
name|namespace
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|str
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
else|else
block|{
name|str
operator|.
name|append
argument_list|(
literal|"actions="
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|actions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|actions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|str
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
if|if
condition|(
name|actions
index|[
name|i
index|]
operator|!=
literal|null
condition|)
name|str
operator|.
name|append
argument_list|(
name|actions
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|str
operator|.
name|append
argument_list|(
literal|"NULL"
argument_list|)
expr_stmt|;
block|}
block|}
name|str
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|str
operator|.
name|toString
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
name|table
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableBytes
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|namespace
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
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
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
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
name|out
operator|.
name|writeBoolean
argument_list|(
name|namespace
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|namespace
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|namespace
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

