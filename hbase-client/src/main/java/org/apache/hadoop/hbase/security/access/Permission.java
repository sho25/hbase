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
name|Arrays
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
name|hadoop
operator|.
name|io
operator|.
name|VersionedWritable
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_comment
comment|/**  * Base permissions instance representing the ability to perform a given set  * of actions.  *  * @see TablePermission  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|Permission
extends|extends
name|VersionedWritable
block|{
specifier|protected
specifier|static
specifier|final
name|byte
name|VERSION
init|=
literal|0
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|Action
block|{
name|READ
argument_list|(
literal|'R'
argument_list|)
block|,
name|WRITE
argument_list|(
literal|'W'
argument_list|)
block|,
name|EXEC
argument_list|(
literal|'X'
argument_list|)
block|,
name|CREATE
argument_list|(
literal|'C'
argument_list|)
block|,
name|ADMIN
argument_list|(
literal|'A'
argument_list|)
block|;
specifier|private
name|byte
name|code
decl_stmt|;
name|Action
parameter_list|(
name|char
name|code
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
operator|(
name|byte
operator|)
name|code
expr_stmt|;
block|}
specifier|public
name|byte
name|code
parameter_list|()
block|{
return|return
name|code
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Permission
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|Action
argument_list|>
name|ACTION_BY_CODE
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
specifier|protected
name|Action
index|[]
name|actions
decl_stmt|;
static|static
block|{
for|for
control|(
name|Action
name|a
range|:
name|Action
operator|.
name|values
argument_list|()
control|)
block|{
name|ACTION_BY_CODE
operator|.
name|put
argument_list|(
name|a
operator|.
name|code
argument_list|()
argument_list|,
name|a
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Empty constructor for Writable implementation.<b>Do not use.</b> */
specifier|public
name|Permission
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Permission
parameter_list|(
name|Action
modifier|...
name|assigned
parameter_list|)
block|{
if|if
condition|(
name|assigned
operator|!=
literal|null
operator|&&
name|assigned
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|actions
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|assigned
argument_list|,
name|assigned
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Permission
parameter_list|(
name|byte
index|[]
name|actionCodes
parameter_list|)
block|{
if|if
condition|(
name|actionCodes
operator|!=
literal|null
condition|)
block|{
name|Action
name|acts
index|[]
init|=
operator|new
name|Action
index|[
name|actionCodes
operator|.
name|length
index|]
decl_stmt|;
name|int
name|j
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|actionCodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|actionCodes
index|[
name|i
index|]
decl_stmt|;
name|Action
name|a
init|=
name|ACTION_BY_CODE
operator|.
name|get
argument_list|(
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Ignoring unknown action code '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
operator|new
name|byte
index|[]
block|{
name|b
block|}
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|acts
index|[
name|j
operator|++
index|]
operator|=
name|a
expr_stmt|;
block|}
name|this
operator|.
name|actions
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|acts
argument_list|,
name|j
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Action
index|[]
name|getActions
parameter_list|()
block|{
return|return
name|actions
return|;
block|}
specifier|public
name|boolean
name|implies
parameter_list|(
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|actions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Action
name|a
range|:
name|this
operator|.
name|actions
control|)
block|{
if|if
condition|(
name|a
operator|==
name|action
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|setActions
parameter_list|(
name|Action
index|[]
name|assigned
parameter_list|)
block|{
if|if
condition|(
name|assigned
operator|!=
literal|null
operator|&&
name|assigned
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|actions
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|assigned
argument_list|,
name|assigned
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
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
name|Permission
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Permission
name|other
init|=
operator|(
name|Permission
operator|)
name|obj
decl_stmt|;
comment|// check actions
if|if
condition|(
name|actions
operator|==
literal|null
operator|&&
name|other
operator|.
name|getActions
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|actions
operator|!=
literal|null
operator|&&
name|other
operator|.
name|getActions
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Action
index|[]
name|otherActions
init|=
name|other
operator|.
name|getActions
argument_list|()
decl_stmt|;
if|if
condition|(
name|actions
operator|.
name|length
operator|!=
name|otherActions
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
name|outer
label|:
for|for
control|(
name|Action
name|a
range|:
name|actions
control|)
block|{
for|for
control|(
name|Action
name|oa
range|:
name|otherActions
control|)
block|{
if|if
condition|(
name|a
operator|==
name|oa
condition|)
continue|continue
name|outer
continue|;
block|}
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
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
literal|23
decl_stmt|;
for|for
control|(
name|Action
name|a
range|:
name|actions
control|)
block|{
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|a
operator|.
name|code
argument_list|()
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
name|StringBuilder
name|str
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"[Permission: "
argument_list|)
operator|.
name|append
argument_list|(
literal|"actions="
argument_list|)
decl_stmt|;
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
comment|/** @return the object version number */
annotation|@
name|Override
specifier|public
name|byte
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
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
name|int
name|length
init|=
operator|(
name|int
operator|)
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|length
operator|>
literal|0
condition|)
block|{
name|actions
operator|=
operator|new
name|Action
index|[
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|Action
name|a
init|=
name|ACTION_BY_CODE
operator|.
name|get
argument_list|(
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown action code '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
operator|new
name|byte
index|[]
block|{
name|b
block|}
argument_list|)
operator|+
literal|"' in input"
argument_list|)
throw|;
block|}
name|this
operator|.
name|actions
index|[
name|i
index|]
operator|=
name|a
expr_stmt|;
block|}
block|}
else|else
block|{
name|actions
operator|=
operator|new
name|Action
index|[
literal|0
index|]
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
name|out
operator|.
name|writeByte
argument_list|(
name|actions
operator|!=
literal|null
condition|?
name|actions
operator|.
name|length
else|:
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|actions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Action
name|a
range|:
name|actions
control|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
name|a
operator|.
name|code
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

