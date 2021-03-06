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
name|concurrent
operator|.
name|TimeUnit
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
name|KeyPress
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
name|Terminal
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
name|TerminalPrinter
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|TestTerminalPrinter
block|{
specifier|private
name|TestTerminalPrinter
parameter_list|()
block|{   }
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
try|try
init|(
name|Terminal
name|terminal
init|=
operator|new
name|TerminalImpl
argument_list|()
init|)
block|{
name|terminal
operator|.
name|hideCursor
argument_list|()
expr_stmt|;
name|terminal
operator|.
name|refresh
argument_list|()
expr_stmt|;
name|TerminalPrinter
name|printer
init|=
name|terminal
operator|.
name|getTerminalPrinter
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"Normal string"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|startHighlight
argument_list|()
operator|.
name|print
argument_list|(
literal|"Highlighted string"
argument_list|)
operator|.
name|stopHighlight
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
literal|"Bold string"
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|startHighlight
argument_list|()
operator|.
name|startBold
argument_list|()
operator|.
name|print
argument_list|(
literal|"Highlighted bold string"
argument_list|)
operator|.
name|stopBold
argument_list|()
operator|.
name|stopHighlight
argument_list|()
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|printer
operator|.
name|print
argument_list|(
literal|"Press any key to finish"
argument_list|)
operator|.
name|endOfLine
argument_list|()
expr_stmt|;
name|terminal
operator|.
name|refresh
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|KeyPress
name|keyPress
init|=
name|terminal
operator|.
name|pollKeyPress
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyPress
operator|==
literal|null
condition|)
block|{
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
continue|continue;
block|}
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit

