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
comment|/**  * The interface responsible for printing to the terminal.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|TerminalPrinter
block|{
name|TerminalPrinter
name|print
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|char
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|Character
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|short
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|Short
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|Float
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|print
parameter_list|(
name|double
name|value
parameter_list|)
block|{
name|print
argument_list|(
name|Double
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|default
name|TerminalPrinter
name|printFormat
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|print
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|TerminalPrinter
name|startHighlight
parameter_list|()
function_decl|;
name|TerminalPrinter
name|stopHighlight
parameter_list|()
function_decl|;
name|TerminalPrinter
name|startBold
parameter_list|()
function_decl|;
name|TerminalPrinter
name|stopBold
parameter_list|()
function_decl|;
name|void
name|endOfLine
parameter_list|()
function_decl|;
block|}
end_interface

end_unit
