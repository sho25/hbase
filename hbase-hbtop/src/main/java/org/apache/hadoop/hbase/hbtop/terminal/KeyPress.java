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
name|terminal
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
name|Nullable
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Represents the user pressing a key on the keyboard.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|KeyPress
block|{
specifier|public
enum|enum
name|Type
block|{
name|Character
block|,
name|Escape
block|,
name|Backspace
block|,
name|ArrowLeft
block|,
name|ArrowRight
block|,
name|ArrowUp
block|,
name|ArrowDown
block|,
name|Insert
block|,
name|Delete
block|,
name|Home
block|,
name|End
block|,
name|PageUp
block|,
name|PageDown
block|,
name|ReverseTab
block|,
name|Tab
block|,
name|Enter
block|,
name|F1
block|,
name|F2
block|,
name|F3
block|,
name|F4
block|,
name|F5
block|,
name|F6
block|,
name|F7
block|,
name|F8
block|,
name|F9
block|,
name|F10
block|,
name|F11
block|,
name|F12
block|,
name|Unknown
block|}
specifier|private
specifier|final
name|Type
name|type
decl_stmt|;
specifier|private
specifier|final
name|Character
name|character
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|alt
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|ctrl
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|shift
decl_stmt|;
specifier|public
name|KeyPress
parameter_list|(
name|Type
name|type
parameter_list|,
annotation|@
name|Nullable
name|Character
name|character
parameter_list|,
name|boolean
name|alt
parameter_list|,
name|boolean
name|ctrl
parameter_list|,
name|boolean
name|shift
parameter_list|)
block|{
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
name|this
operator|.
name|character
operator|=
name|character
expr_stmt|;
name|this
operator|.
name|alt
operator|=
name|alt
expr_stmt|;
name|this
operator|.
name|ctrl
operator|=
name|ctrl
expr_stmt|;
name|this
operator|.
name|shift
operator|=
name|shift
expr_stmt|;
block|}
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Nullable
specifier|public
name|Character
name|getCharacter
parameter_list|()
block|{
return|return
name|character
return|;
block|}
specifier|public
name|boolean
name|isAlt
parameter_list|()
block|{
return|return
name|alt
return|;
block|}
specifier|public
name|boolean
name|isCtrl
parameter_list|()
block|{
return|return
name|ctrl
return|;
block|}
specifier|public
name|boolean
name|isShift
parameter_list|()
block|{
return|return
name|shift
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
literal|"KeyPress{"
operator|+
literal|"type="
operator|+
name|type
operator|+
literal|", character="
operator|+
name|escape
argument_list|(
name|character
argument_list|)
operator|+
literal|", alt="
operator|+
name|alt
operator|+
literal|", ctrl="
operator|+
name|ctrl
operator|+
literal|", shift="
operator|+
name|shift
operator|+
literal|'}'
return|;
block|}
specifier|private
name|String
name|escape
parameter_list|(
name|Character
name|character
parameter_list|)
block|{
if|if
condition|(
name|character
operator|==
literal|null
condition|)
block|{
return|return
literal|"null"
return|;
block|}
switch|switch
condition|(
name|character
condition|)
block|{
case|case
literal|'\n'
case|:
return|return
literal|"\\n"
return|;
case|case
literal|'\b'
case|:
return|return
literal|"\\b"
return|;
case|case
literal|'\t'
case|:
return|return
literal|"\\t"
return|;
default|default:
return|return
name|character
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

