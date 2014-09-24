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
comment|/**  * Represents an authorization for access over the given table, column family  * plus qualifier, for the given user.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UserPermission
extends|extends
name|TablePermission
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
name|UserPermission
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|user
decl_stmt|;
comment|/** Nullary constructor for Writable, do not use */
specifier|public
name|UserPermission
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user.    * @param user the user    * @param assigned the list of allowed actions    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|Action
modifier|...
name|assigned
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|assigned
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user,    * matching the actions with the given codes.    * @param user the user    * @param actionCodes the list of allowed action codes    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|byte
index|[]
name|actionCodes
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionCodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user.    * @param user the user    * @param namespace    * @param assigned the list of allowed actions    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
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
name|namespace
argument_list|,
name|assigned
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user,    * matching the actions with the given codes.    * @param user the user    * @param namespace    * @param actionCodes the list of allowed action codes    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
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
name|namespace
argument_list|,
name|actionCodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user, table and column family.    * @param user the user    * @param table the table    * @param family the family, can be null if action is allowed over the entire    *   table    * @param assigned the list of allowed actions    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
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
name|super
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|assigned
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new permission for the given user, table, column family and    * column qualifier.    * @param user the user    * @param table the table    * @param family the family, can be null if action is allowed over the entire    *   table    * @param qualifier the column qualifier, can be null if action is allowed    *   over the entire column family    * @param assigned the list of allowed actions    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
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
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|assigned
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user, table, column family and    * qualifier, matching the actions with the given codes.    * @param user the user    * @param table the table    * @param family the family, can be null if action is allowed over the entire    *   table    * @param qualifier the column qualifier, can be null if action is allowed    *   over the entire column family    * @param actionCodes the list of allowed action codes    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
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
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|actionCodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
comment|/**    * Creates a new instance for the given user, table, column family and    * qualifier, matching the actions with the given codes.    * @param user the user    * @param perm a TablePermission    */
specifier|public
name|UserPermission
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|TablePermission
name|perm
parameter_list|)
block|{
name|super
argument_list|(
name|perm
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|perm
operator|.
name|getTableName
argument_list|()
argument_list|,
name|perm
operator|.
name|getFamily
argument_list|()
argument_list|,
name|perm
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|perm
operator|.
name|actions
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
comment|/**    * Returns true if this permission describes a global user permission.    */
specifier|public
name|boolean
name|isGlobal
parameter_list|()
block|{
return|return
operator|(
operator|!
name|hasTable
argument_list|()
operator|&&
operator|!
name|hasNamespace
argument_list|()
operator|)
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
operator|!
operator|(
name|obj
operator|instanceof
name|UserPermission
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|UserPermission
name|other
init|=
operator|(
name|UserPermission
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|(
name|Bytes
operator|.
name|equals
argument_list|(
name|user
argument_list|,
name|other
operator|.
name|getUser
argument_list|()
argument_list|)
operator|&&
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
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
name|user
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
name|user
argument_list|)
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
literal|"UserPermission: "
argument_list|)
operator|.
name|append
argument_list|(
literal|"user="
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|user
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
name|super
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
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
name|user
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
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
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

