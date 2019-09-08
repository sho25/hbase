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
import|import static
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
operator|.
name|EscapeSequences
operator|.
name|clearRemainingLine
import|;
end_import

begin_import
import|import static
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
operator|.
name|EscapeSequences
operator|.
name|color
import|;
end_import

begin_import
import|import static
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
operator|.
name|EscapeSequences
operator|.
name|cursor
import|;
end_import

begin_import
import|import static
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
operator|.
name|EscapeSequences
operator|.
name|moveCursor
import|;
end_import

begin_import
import|import static
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
operator|.
name|EscapeSequences
operator|.
name|normal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|CursorPosition
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
comment|/**  * Represents a buffer of the terminal screen for double-buffering.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScreenBuffer
block|{
specifier|private
name|int
name|columns
decl_stmt|;
specifier|private
name|int
name|rows
decl_stmt|;
specifier|private
name|Cell
index|[]
index|[]
name|buffer
decl_stmt|;
specifier|private
name|Cell
index|[]
index|[]
name|physical
decl_stmt|;
specifier|private
name|boolean
name|cursorVisible
decl_stmt|;
specifier|private
name|int
name|cursorColumn
decl_stmt|;
specifier|private
name|int
name|cursorRow
decl_stmt|;
specifier|public
name|void
name|reallocate
parameter_list|(
name|int
name|columns
parameter_list|,
name|int
name|rows
parameter_list|)
block|{
name|buffer
operator|=
operator|new
name|Cell
index|[
name|columns
index|]
index|[
name|rows
index|]
expr_stmt|;
name|physical
operator|=
operator|new
name|Cell
index|[
name|columns
index|]
index|[
name|rows
index|]
expr_stmt|;
for|for
control|(
name|int
name|row
init|=
literal|0
init|;
name|row
operator|<
name|rows
condition|;
name|row
operator|++
control|)
block|{
for|for
control|(
name|int
name|column
init|=
literal|0
init|;
name|column
operator|<
name|columns
condition|;
name|column
operator|++
control|)
block|{
name|buffer
index|[
name|column
index|]
index|[
name|row
index|]
operator|=
operator|new
name|Cell
argument_list|()
expr_stmt|;
name|physical
index|[
name|column
index|]
index|[
name|row
index|]
operator|=
operator|new
name|Cell
argument_list|()
expr_stmt|;
name|physical
index|[
name|column
index|]
index|[
name|row
index|]
operator|.
name|unset
argument_list|()
expr_stmt|;
block|}
block|}
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|this
operator|.
name|rows
operator|=
name|rows
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
for|for
control|(
name|int
name|row
init|=
literal|0
init|;
name|row
operator|<
name|rows
condition|;
name|row
operator|++
control|)
block|{
for|for
control|(
name|int
name|col
init|=
literal|0
init|;
name|col
operator|<
name|columns
condition|;
name|col
operator|++
control|)
block|{
name|buffer
index|[
name|col
index|]
index|[
name|row
index|]
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|flush
parameter_list|(
name|PrintWriter
name|output
parameter_list|)
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
name|normal
argument_list|()
argument_list|)
expr_stmt|;
name|Attributes
name|attributes
init|=
operator|new
name|Attributes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|row
init|=
literal|0
init|;
name|row
operator|<
name|rows
condition|;
name|row
operator|++
control|)
block|{
name|flushRow
argument_list|(
name|row
argument_list|,
name|sb
argument_list|,
name|attributes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cursorVisible
operator|&&
name|cursorRow
operator|>=
literal|0
operator|&&
name|cursorColumn
operator|>=
literal|0
operator|&&
name|cursorRow
operator|<
name|rows
operator|&&
name|cursorColumn
operator|<
name|columns
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|cursor
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|moveCursor
argument_list|(
name|cursorColumn
argument_list|,
name|cursorRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|cursor
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|write
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|flushRow
parameter_list|(
name|int
name|row
parameter_list|,
name|StringBuilder
name|sb
parameter_list|,
name|Attributes
name|lastAttributes
parameter_list|)
block|{
name|int
name|lastColumn
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|column
init|=
literal|0
init|;
name|column
operator|<
name|columns
condition|;
name|column
operator|++
control|)
block|{
name|Cell
name|cell
init|=
name|buffer
index|[
name|column
index|]
index|[
name|row
index|]
decl_stmt|;
name|Cell
name|pCell
init|=
name|physical
index|[
name|column
index|]
index|[
name|row
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|cell
operator|.
name|equals
argument_list|(
name|pCell
argument_list|)
condition|)
block|{
if|if
condition|(
name|lastColumn
operator|!=
name|column
operator|-
literal|1
operator|||
name|lastColumn
operator|==
operator|-
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|moveCursor
argument_list|(
name|column
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cell
operator|.
name|isEndOfLine
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|column
init|;
name|i
operator|<
name|columns
condition|;
name|i
operator|++
control|)
block|{
name|physical
index|[
name|i
index|]
index|[
name|row
index|]
operator|.
name|set
argument_list|(
name|buffer
index|[
name|i
index|]
index|[
name|row
index|]
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|clearRemainingLine
argument_list|()
argument_list|)
expr_stmt|;
name|lastAttributes
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|cell
operator|.
name|getAttributes
argument_list|()
operator|.
name|equals
argument_list|(
name|lastAttributes
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|color
argument_list|(
name|cell
operator|.
name|getForegroundColor
argument_list|()
argument_list|,
name|cell
operator|.
name|getBackgroundColor
argument_list|()
argument_list|,
name|cell
operator|.
name|isBold
argument_list|()
argument_list|,
name|cell
operator|.
name|isReverse
argument_list|()
argument_list|,
name|cell
operator|.
name|isBlink
argument_list|()
argument_list|,
name|cell
operator|.
name|isUnderline
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|cell
operator|.
name|getChar
argument_list|()
argument_list|)
expr_stmt|;
name|lastColumn
operator|=
name|column
expr_stmt|;
name|lastAttributes
operator|.
name|set
argument_list|(
name|cell
operator|.
name|getAttributes
argument_list|()
argument_list|)
expr_stmt|;
name|physical
index|[
name|column
index|]
index|[
name|row
index|]
operator|.
name|set
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|CursorPosition
name|getCursorPosition
parameter_list|()
block|{
return|return
operator|new
name|CursorPosition
argument_list|(
name|cursorColumn
argument_list|,
name|cursorRow
argument_list|)
return|;
block|}
specifier|public
name|void
name|setCursorPosition
parameter_list|(
name|int
name|column
parameter_list|,
name|int
name|row
parameter_list|)
block|{
name|cursorVisible
operator|=
literal|true
expr_stmt|;
name|cursorColumn
operator|=
name|column
expr_stmt|;
name|cursorRow
operator|=
name|row
expr_stmt|;
block|}
specifier|public
name|void
name|hideCursor
parameter_list|()
block|{
name|cursorVisible
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|putString
parameter_list|(
name|int
name|column
parameter_list|,
name|int
name|row
parameter_list|,
name|String
name|string
parameter_list|,
name|Attributes
name|attributes
parameter_list|)
block|{
name|int
name|i
init|=
name|column
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|string
operator|.
name|length
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|char
name|ch
init|=
name|string
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
decl_stmt|;
name|putChar
argument_list|(
name|i
argument_list|,
name|row
argument_list|,
name|ch
argument_list|,
name|attributes
argument_list|)
expr_stmt|;
name|i
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|columns
condition|)
block|{
break|break;
block|}
block|}
block|}
specifier|public
name|void
name|putChar
parameter_list|(
name|int
name|column
parameter_list|,
name|int
name|row
parameter_list|,
name|char
name|ch
parameter_list|,
name|Attributes
name|attributes
parameter_list|)
block|{
if|if
condition|(
name|column
operator|>=
literal|0
operator|&&
name|column
operator|<
name|columns
operator|&&
name|row
operator|>=
literal|0
operator|&&
name|row
operator|<
name|rows
condition|)
block|{
name|buffer
index|[
name|column
index|]
index|[
name|row
index|]
operator|.
name|setAttributes
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
name|buffer
index|[
name|column
index|]
index|[
name|row
index|]
operator|.
name|setChar
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|endOfLine
parameter_list|(
name|int
name|column
parameter_list|,
name|int
name|row
parameter_list|)
block|{
if|if
condition|(
name|column
operator|>=
literal|0
operator|&&
name|column
operator|<
name|columns
operator|&&
name|row
operator|>=
literal|0
operator|&&
name|row
operator|<
name|rows
condition|)
block|{
name|buffer
index|[
name|column
index|]
index|[
name|row
index|]
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|column
operator|+
literal|1
init|;
name|i
operator|<
name|columns
condition|;
name|i
operator|++
control|)
block|{
name|buffer
index|[
name|i
index|]
index|[
name|row
index|]
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit
