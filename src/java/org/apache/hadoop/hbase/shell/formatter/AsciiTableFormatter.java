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
name|shell
operator|.
name|formatter
package|;
end_package

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
name|io
operator|.
name|Writer
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
name|shell
operator|.
name|TableFormatter
import|;
end_import

begin_comment
comment|/**  * Formatter that outputs data inside an ASCII table.  * If only a single cell result, then no formatting is done.  Presumption is  * that client manages serial access outputting tables. Does not close passed  * {@link Writer}.  */
end_comment

begin_class
specifier|public
class|class
name|AsciiTableFormatter
implements|implements
name|TableFormatter
block|{
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_DELIMITER
init|=
literal|"| "
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_CLOSER
init|=
literal|"|"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_COLUMN_WIDTH
init|=
literal|26
decl_stmt|;
comment|// Width is a line of content + delimiter
specifier|private
name|int
name|columnWidth
init|=
name|DEFAULT_COLUMN_WIDTH
decl_stmt|;
comment|// Amount of width to use for a line of content.
specifier|private
name|int
name|columnContentWidth
init|=
name|DEFAULT_COLUMN_WIDTH
operator|-
name|COLUMN_DELIMITER
operator|.
name|length
argument_list|()
decl_stmt|;
comment|// COLUMN_LINE is put at head and foot of a column and per column, is drawn
comment|// as row delimiter
specifier|private
name|String
name|columnHorizLine
decl_stmt|;
specifier|private
specifier|final
name|String
name|COLUMN_HORIZ_LINE_CLOSER
init|=
literal|"+"
decl_stmt|;
comment|// Used padding content to fill column
specifier|private
specifier|final
name|String
name|PADDING_CHAR
init|=
literal|" "
decl_stmt|;
comment|// True if we are to output no formatting.
specifier|private
name|boolean
name|noFormatting
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|Writer
name|out
decl_stmt|;
specifier|private
specifier|final
name|String
name|LINE_SEPARATOR
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
decl_stmt|;
comment|// Not instantiable
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|AsciiTableFormatter
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|AsciiTableFormatter
parameter_list|(
specifier|final
name|Writer
name|o
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|o
expr_stmt|;
block|}
specifier|public
name|Writer
name|getOut
parameter_list|()
block|{
return|return
name|this
operator|.
name|out
return|;
block|}
comment|/**    * @param titles List of titles.  Pass null if no formatting (i.e.    * no header, no footer, etc.    * @throws IOException     */
specifier|public
name|void
name|header
parameter_list|(
name|String
index|[]
name|titles
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|titles
operator|==
literal|null
condition|)
block|{
comment|// print nothing.
name|setNoFormatting
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Calculate width of columns.
name|this
operator|.
name|columnWidth
operator|=
name|titles
operator|.
name|length
operator|==
literal|1
condition|?
literal|3
operator|*
name|DEFAULT_COLUMN_WIDTH
else|:
name|titles
operator|.
name|length
operator|==
literal|2
condition|?
literal|39
else|:
name|DEFAULT_COLUMN_WIDTH
expr_stmt|;
name|this
operator|.
name|columnContentWidth
operator|=
name|this
operator|.
name|columnWidth
operator|-
name|COLUMN_DELIMITER
operator|.
name|length
argument_list|()
expr_stmt|;
comment|// Create the horizontal line to draw across the top of each column.
name|this
operator|.
name|columnHorizLine
operator|=
name|calculateColumnHorizLine
argument_list|(
name|this
operator|.
name|columnWidth
argument_list|)
expr_stmt|;
comment|// Print out a column topper per column.
name|printRowDelimiter
argument_list|(
name|titles
operator|.
name|length
argument_list|)
expr_stmt|;
name|row
argument_list|(
name|titles
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|row
parameter_list|(
name|String
index|[]
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isNoFormatting
argument_list|()
condition|)
block|{
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|cells
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// Ok.  Output cells a line at a time w/ delimiters between cells.
name|int
index|[]
name|indexes
init|=
operator|new
name|int
index|[
name|cells
operator|.
name|length
index|]
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
name|indexes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indexes
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
block|}
name|int
name|allFinished
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|allFinished
operator|<
name|indexes
operator|.
name|length
condition|)
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
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
name|cells
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|COLUMN_DELIMITER
argument_list|)
expr_stmt|;
name|int
name|offset
init|=
name|indexes
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|offset
operator|+
name|this
operator|.
name|columnContentWidth
operator|>=
name|cells
index|[
name|i
index|]
operator|.
name|length
argument_list|()
condition|)
block|{
name|String
name|substr
init|=
name|cells
index|[
name|i
index|]
operator|.
name|substring
argument_list|(
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|substr
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// This column is finished
name|allFinished
operator|++
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|substr
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|this
operator|.
name|columnContentWidth
operator|-
name|substr
operator|.
name|length
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|PADDING_CHAR
argument_list|)
expr_stmt|;
block|}
name|indexes
index|[
name|i
index|]
operator|=
name|cells
index|[
name|i
index|]
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|String
name|substr
init|=
name|cells
index|[
name|i
index|]
operator|.
name|substring
argument_list|(
name|indexes
index|[
name|i
index|]
argument_list|,
name|indexes
index|[
name|i
index|]
operator|+
name|this
operator|.
name|columnContentWidth
argument_list|)
decl_stmt|;
name|indexes
index|[
name|i
index|]
operator|+=
name|this
operator|.
name|columnContentWidth
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|substr
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
name|COLUMN_CLOSER
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|LINE_SEPARATOR
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
name|printRowDelimiter
argument_list|(
name|cells
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|footer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isNoFormatting
argument_list|()
condition|)
block|{
comment|// If no formatting, output a newline to delimit cell and the
comment|// result summary output at end of every command.
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|LINE_SEPARATOR
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
comment|// We're done. Clear flag.
name|setNoFormatting
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printRowDelimiter
parameter_list|(
specifier|final
name|int
name|columnCount
parameter_list|)
throws|throws
name|IOException
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
name|columnCount
condition|;
name|i
operator|++
control|)
block|{
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|this
operator|.
name|columnHorizLine
argument_list|)
expr_stmt|;
block|}
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|COLUMN_HORIZ_LINE_CLOSER
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|write
argument_list|(
name|LINE_SEPARATOR
argument_list|)
expr_stmt|;
name|getOut
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|calculateColumnHorizLine
parameter_list|(
specifier|final
name|int
name|width
parameter_list|)
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|width
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isNoFormatting
parameter_list|()
block|{
return|return
name|this
operator|.
name|noFormatting
return|;
block|}
specifier|public
name|void
name|setNoFormatting
parameter_list|(
name|boolean
name|noFormatting
parameter_list|)
block|{
name|this
operator|.
name|noFormatting
operator|=
name|noFormatting
expr_stmt|;
block|}
block|}
end_class

end_unit

