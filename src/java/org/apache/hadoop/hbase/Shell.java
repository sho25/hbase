begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|OutputStreamWriter
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
name|jline
operator|.
name|ConsoleReader
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
name|conf
operator|.
name|Configuration
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
name|Command
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
name|HelpCommand
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
name|ReturnMsg
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
name|TableFormatterFactory
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
name|generated
operator|.
name|ParseException
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
name|generated
operator|.
name|Parser
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
name|generated
operator|.
name|TokenMgrError
import|;
end_import

begin_comment
comment|/**  * An hbase shell.  *   * @see<a href="http://wiki.apache.org/lucene-hadoop/Hbase/HbaseShell">HbaseShell</a>  */
end_comment

begin_class
specifier|public
class|class
name|Shell
block|{
comment|/** audible keyboard bells */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_BELL_ENABLED
init|=
literal|true
decl_stmt|;
comment|/** Return the boolean value indicating whether end of command or not */
specifier|static
name|boolean
name|isEndOfCommand
parameter_list|(
name|String
name|line
parameter_list|)
block|{
return|return
operator|(
name|line
operator|.
name|lastIndexOf
argument_list|(
literal|';'
argument_list|)
operator|>
operator|-
literal|1
operator|)
condition|?
literal|true
else|:
literal|false
return|;
block|}
comment|/** Return the string of prompt start string */
specifier|private
specifier|static
name|String
name|getPrompt
parameter_list|(
specifier|final
name|StringBuilder
name|queryStr
parameter_list|)
block|{
return|return
operator|(
name|queryStr
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
operator|)
condition|?
literal|"Hbase> "
else|:
literal|"   --> "
return|;
block|}
comment|/**    * @param watch true if execution time should be computed and returned    * @param start start of time interval    * @param end end of time interval    * @return a string of code execution time. */
specifier|public
specifier|static
name|String
name|executeTime
parameter_list|(
name|boolean
name|watch
parameter_list|,
name|long
name|start
parameter_list|,
name|long
name|end
parameter_list|)
block|{
return|return
name|watch
condition|?
literal|" ("
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%.2f"
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
operator|(
name|end
operator|-
name|start
operator|)
operator|*
literal|0.001
argument_list|)
argument_list|)
operator|+
literal|" sec)"
else|:
literal|""
return|;
block|}
comment|/**    * Main method    * @param args not used    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|ConsoleReader
name|reader
init|=
operator|new
name|ConsoleReader
argument_list|()
decl_stmt|;
name|reader
operator|.
name|setBellEnabled
argument_list|(
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbaseshell.jline.bell.enabled"
argument_list|,
name|DEFAULT_BELL_ENABLED
argument_list|)
argument_list|)
expr_stmt|;
name|Writer
name|out
init|=
operator|new
name|OutputStreamWriter
argument_list|(
name|System
operator|.
name|out
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|TableFormatterFactory
name|tff
init|=
operator|new
name|TableFormatterFactory
argument_list|(
name|out
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HelpCommand
name|help
init|=
operator|new
name|HelpCommand
argument_list|(
name|out
argument_list|,
name|tff
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|help
operator|.
name|printVersion
argument_list|()
expr_stmt|;
name|StringBuilder
name|queryStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|String
name|extendedLine
decl_stmt|;
while|while
condition|(
operator|(
name|extendedLine
operator|=
name|reader
operator|.
name|readLine
argument_list|(
name|getPrompt
argument_list|(
name|queryStr
argument_list|)
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isEndOfCommand
argument_list|(
name|extendedLine
argument_list|)
condition|)
block|{
name|queryStr
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|extendedLine
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Parser
name|parser
init|=
operator|new
name|Parser
argument_list|(
name|queryStr
operator|.
name|toString
argument_list|()
argument_list|,
name|out
argument_list|,
name|tff
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|ReturnMsg
name|rs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Command
name|cmd
init|=
name|parser
operator|.
name|terminatedCommand
argument_list|()
decl_stmt|;
if|if
condition|(
name|cmd
operator|!=
literal|null
condition|)
block|{
name|rs
operator|=
name|cmd
operator|.
name|execute
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ParseException
name|pe
parameter_list|)
block|{
name|String
index|[]
name|msg
init|=
name|pe
operator|.
name|getMessage
argument_list|()
operator|.
name|split
argument_list|(
literal|"[\n]"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Syntax error : Type 'help;' for usage.\nMessage : "
operator|+
name|msg
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TokenMgrError
name|te
parameter_list|)
block|{
name|String
index|[]
name|msg
init|=
name|te
operator|.
name|getMessage
argument_list|()
operator|.
name|split
argument_list|(
literal|"[\n]"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Lexical error : Type 'help;' for usage.\nMessage : "
operator|+
name|msg
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|rs
operator|!=
literal|null
operator|&&
name|rs
operator|.
name|getType
argument_list|()
operator|>
operator|-
literal|1
condition|)
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|rs
operator|.
name|getMsg
argument_list|()
operator|+
name|executeTime
argument_list|(
operator|(
name|rs
operator|.
name|getType
argument_list|()
operator|==
literal|1
operator|)
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
expr_stmt|;
name|queryStr
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|queryStr
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|extendedLine
argument_list|)
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

