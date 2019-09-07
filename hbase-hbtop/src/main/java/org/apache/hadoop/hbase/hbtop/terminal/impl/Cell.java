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
operator|.
name|impl
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
name|hadoop
operator|.
name|hbase
operator|.
name|hbtop
operator|.
name|terminal
operator|.
name|Attributes
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
name|hbtop
operator|.
name|terminal
operator|.
name|Color
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
comment|/**  * Represents a single text cell of the terminal.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Cell
block|{
specifier|private
specifier|static
specifier|final
name|char
name|UNSET_VALUE
init|=
operator|(
name|char
operator|)
literal|65535
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|END_OF_LINE
init|=
literal|'\0'
decl_stmt|;
specifier|private
specifier|final
name|Attributes
name|attributes
decl_stmt|;
specifier|private
name|char
name|ch
decl_stmt|;
specifier|public
name|Cell
parameter_list|()
block|{
name|attributes
operator|=
operator|new
name|Attributes
argument_list|()
expr_stmt|;
name|ch
operator|=
literal|' '
expr_stmt|;
block|}
specifier|public
name|char
name|getChar
parameter_list|()
block|{
return|return
name|ch
return|;
block|}
specifier|public
name|void
name|setChar
parameter_list|(
name|char
name|ch
parameter_list|)
block|{
name|this
operator|.
name|ch
operator|=
name|ch
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|attributes
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ch
operator|=
literal|' '
expr_stmt|;
block|}
specifier|public
name|void
name|unset
parameter_list|()
block|{
name|attributes
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ch
operator|=
name|UNSET_VALUE
expr_stmt|;
block|}
specifier|public
name|void
name|endOfLine
parameter_list|()
block|{
name|attributes
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ch
operator|=
name|END_OF_LINE
expr_stmt|;
block|}
specifier|public
name|boolean
name|isEndOfLine
parameter_list|()
block|{
return|return
name|ch
operator|==
name|END_OF_LINE
return|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|attributes
operator|.
name|set
argument_list|(
name|cell
operator|.
name|attributes
argument_list|)
expr_stmt|;
name|this
operator|.
name|ch
operator|=
name|cell
operator|.
name|ch
expr_stmt|;
block|}
specifier|public
name|Attributes
name|getAttributes
parameter_list|()
block|{
return|return
operator|new
name|Attributes
argument_list|(
name|attributes
argument_list|)
return|;
block|}
specifier|public
name|void
name|setAttributes
parameter_list|(
name|Attributes
name|attributes
parameter_list|)
block|{
name|this
operator|.
name|attributes
operator|.
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
name|attributes
operator|.
name|isBold
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isBlink
parameter_list|()
block|{
return|return
name|attributes
operator|.
name|isBlink
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isReverse
parameter_list|()
block|{
return|return
name|attributes
operator|.
name|isReverse
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isUnderline
parameter_list|()
block|{
return|return
name|attributes
operator|.
name|isUnderline
argument_list|()
return|;
block|}
specifier|public
name|Color
name|getForegroundColor
parameter_list|()
block|{
return|return
name|attributes
operator|.
name|getForegroundColor
argument_list|()
return|;
block|}
specifier|public
name|Color
name|getBackgroundColor
parameter_list|()
block|{
return|return
name|attributes
operator|.
name|getBackgroundColor
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
name|Cell
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Cell
name|cell
init|=
operator|(
name|Cell
operator|)
name|o
decl_stmt|;
return|return
name|ch
operator|==
name|cell
operator|.
name|ch
operator|&&
name|attributes
operator|.
name|equals
argument_list|(
name|cell
operator|.
name|attributes
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
name|attributes
argument_list|,
name|ch
argument_list|)
return|;
block|}
block|}
end_class

end_unit

