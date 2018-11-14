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
name|NamespaceDescriptor
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Represents an authorization for access for the given namespace.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NamespacePermission
extends|extends
name|Permission
block|{
specifier|private
name|String
name|namespace
init|=
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
decl_stmt|;
comment|/** Default constructor for Writable, do not use */
specifier|public
name|NamespacePermission
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
comment|/**    * Construct a namespace permission.    * @param namespace namespace's name    * @param assigned assigned actions    */
specifier|public
name|NamespacePermission
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
name|this
operator|.
name|scope
operator|=
name|Scope
operator|.
name|NAMESPACE
expr_stmt|;
block|}
comment|/**    * Construct a namespace permission.    * @param namespace namespace's name    * @param actionCode assigned actions    */
specifier|public
name|NamespacePermission
parameter_list|(
name|String
name|namespace
parameter_list|,
name|byte
index|[]
name|actionCode
parameter_list|)
block|{
name|super
argument_list|(
name|actionCode
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
name|scope
operator|=
name|Scope
operator|.
name|NAMESPACE
expr_stmt|;
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
comment|/**    * check if given action is granted in given namespace.    * @param namespace namespace's name    * @param action action to be checked    * @return true if granted, false otherwise    */
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
return|return
name|namespace
operator|.
name|equals
argument_list|(
name|this
operator|.
name|namespace
argument_list|)
operator|&&
name|implies
argument_list|(
name|action
argument_list|)
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
name|NamespacePermission
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|NamespacePermission
name|gp
init|=
operator|(
name|NamespacePermission
operator|)
name|obj
decl_stmt|;
return|return
name|namespace
operator|.
name|equals
argument_list|(
name|gp
operator|.
name|namespace
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
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|namespace
argument_list|)
operator|+
name|super
operator|.
name|hashCode
argument_list|()
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
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[NamespacePermission: "
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
decl_stmt|;
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
end_class

end_unit

