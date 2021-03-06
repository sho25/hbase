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
comment|/**  * The attributes of text in the terminal.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Attributes
block|{
specifier|private
name|boolean
name|bold
decl_stmt|;
specifier|private
name|boolean
name|blink
decl_stmt|;
specifier|private
name|boolean
name|reverse
decl_stmt|;
specifier|private
name|boolean
name|underline
decl_stmt|;
specifier|private
name|Color
name|foregroundColor
decl_stmt|;
specifier|private
name|Color
name|backgroundColor
decl_stmt|;
specifier|public
name|Attributes
parameter_list|()
block|{
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Attributes
parameter_list|(
name|Attributes
name|attributes
parameter_list|)
block|{
name|set
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isBold
parameter_list|()
block|{
return|return
name|bold
return|;
block|}
specifier|public
name|void
name|setBold
parameter_list|(
name|boolean
name|bold
parameter_list|)
block|{
name|this
operator|.
name|bold
operator|=
name|bold
expr_stmt|;
block|}
specifier|public
name|boolean
name|isBlink
parameter_list|()
block|{
return|return
name|blink
return|;
block|}
specifier|public
name|void
name|setBlink
parameter_list|(
name|boolean
name|blink
parameter_list|)
block|{
name|this
operator|.
name|blink
operator|=
name|blink
expr_stmt|;
block|}
specifier|public
name|boolean
name|isReverse
parameter_list|()
block|{
return|return
name|reverse
return|;
block|}
specifier|public
name|void
name|setReverse
parameter_list|(
name|boolean
name|reverse
parameter_list|)
block|{
name|this
operator|.
name|reverse
operator|=
name|reverse
expr_stmt|;
block|}
specifier|public
name|boolean
name|isUnderline
parameter_list|()
block|{
return|return
name|underline
return|;
block|}
specifier|public
name|void
name|setUnderline
parameter_list|(
name|boolean
name|underline
parameter_list|)
block|{
name|this
operator|.
name|underline
operator|=
name|underline
expr_stmt|;
block|}
specifier|public
name|Color
name|getForegroundColor
parameter_list|()
block|{
return|return
name|foregroundColor
return|;
block|}
specifier|public
name|void
name|setForegroundColor
parameter_list|(
name|Color
name|foregroundColor
parameter_list|)
block|{
name|this
operator|.
name|foregroundColor
operator|=
name|foregroundColor
expr_stmt|;
block|}
specifier|public
name|Color
name|getBackgroundColor
parameter_list|()
block|{
return|return
name|backgroundColor
return|;
block|}
specifier|public
name|void
name|setBackgroundColor
parameter_list|(
name|Color
name|backgroundColor
parameter_list|)
block|{
name|this
operator|.
name|backgroundColor
operator|=
name|backgroundColor
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|bold
operator|=
literal|false
expr_stmt|;
name|blink
operator|=
literal|false
expr_stmt|;
name|reverse
operator|=
literal|false
expr_stmt|;
name|underline
operator|=
literal|false
expr_stmt|;
name|foregroundColor
operator|=
name|Color
operator|.
name|WHITE
expr_stmt|;
name|backgroundColor
operator|=
name|Color
operator|.
name|BLACK
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|Attributes
name|attributes
parameter_list|)
block|{
name|bold
operator|=
name|attributes
operator|.
name|bold
expr_stmt|;
name|blink
operator|=
name|attributes
operator|.
name|blink
expr_stmt|;
name|reverse
operator|=
name|attributes
operator|.
name|reverse
expr_stmt|;
name|underline
operator|=
name|attributes
operator|.
name|underline
expr_stmt|;
name|foregroundColor
operator|=
name|attributes
operator|.
name|foregroundColor
expr_stmt|;
name|backgroundColor
operator|=
name|attributes
operator|.
name|backgroundColor
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|Attributes
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Attributes
name|that
init|=
operator|(
name|Attributes
operator|)
name|o
decl_stmt|;
return|return
name|bold
operator|==
name|that
operator|.
name|bold
operator|&&
name|blink
operator|==
name|that
operator|.
name|blink
operator|&&
name|reverse
operator|==
name|that
operator|.
name|reverse
operator|&&
name|underline
operator|==
name|that
operator|.
name|underline
operator|&&
name|foregroundColor
operator|==
name|that
operator|.
name|foregroundColor
operator|&&
name|backgroundColor
operator|==
name|that
operator|.
name|backgroundColor
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
name|bold
argument_list|,
name|blink
argument_list|,
name|reverse
argument_list|,
name|underline
argument_list|,
name|foregroundColor
argument_list|,
name|backgroundColor
argument_list|)
return|;
block|}
block|}
end_class

end_unit

