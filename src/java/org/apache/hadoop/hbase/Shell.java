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
name|hbase
operator|.
name|hql
operator|.
name|HQLClient
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
name|hql
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
name|hql
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
name|hql
operator|.
name|HQLSecurityManager
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
name|hql
operator|.
name|TableFormatter
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
name|hql
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
name|hql
operator|.
name|formatter
operator|.
name|HtmlTableFormatter
import|;
end_import

begin_comment
comment|/**  * An hbase shell.  *   * @see<a  *      href="http://wiki.apache.org/lucene-hadoop/Hbase/HbaseShell">HbaseShell</a>  */
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
specifier|public
specifier|static
name|String
name|MASTER_ADDRESS
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|String
name|HTML_OPTION
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|int
name|RELAUNCH_FLAG
init|=
literal|7
decl_stmt|;
specifier|public
specifier|static
name|int
name|EXIT_FLAG
init|=
literal|9999
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
literal|"hql> "
else|:
literal|"  --> "
return|;
block|}
comment|/**    * @param watch true if execution time should be computed and returned    * @param start start of time interval    * @param end end of time interval    * @return a string of code execution time.    */
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
comment|/**    * Main method    *     * @param args not used    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
name|argumentParsing
argument_list|(
name|args
argument_list|)
expr_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|args
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"--help"
argument_list|)
operator|||
name|args
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: ./bin/hbase shell [--master:master_address:port] [--html]\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
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
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|HQLSecurityManager
argument_list|()
argument_list|)
expr_stmt|;
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
name|TableFormatter
name|tableFormater
init|=
operator|new
name|TableFormatterFactory
argument_list|(
name|out
argument_list|,
name|conf
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|MASTER_ADDRESS
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.master"
argument_list|,
name|MASTER_ADDRESS
operator|.
name|substring
argument_list|(
literal|9
argument_list|,
name|MASTER_ADDRESS
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HTML_OPTION
operator|!=
literal|null
condition|)
block|{
name|tableFormater
operator|=
operator|new
name|HtmlTableFormatter
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|HelpCommand
name|help
init|=
operator|new
name|HelpCommand
argument_list|(
name|out
argument_list|,
name|tableFormater
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|0
operator|||
operator|!
name|args
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|Shell
operator|.
name|RELAUNCH_FLAG
argument_list|)
argument_list|)
condition|)
block|{
name|help
operator|.
name|printVersion
argument_list|()
expr_stmt|;
block|}
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
name|HQLClient
name|hql
init|=
operator|new
name|HQLClient
argument_list|(
name|conf
argument_list|,
name|MASTER_ADDRESS
argument_list|,
name|out
argument_list|,
name|tableFormater
argument_list|)
decl_stmt|;
name|ReturnMsg
name|rs
init|=
name|hql
operator|.
name|executeQuery
argument_list|(
name|queryStr
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
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
elseif|else
if|if
condition|(
name|rs
operator|.
name|getType
argument_list|()
operator|==
operator|-
literal|9
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
specifier|private
specifier|static
name|void
name|argumentParsing
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
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
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"--master:"
argument_list|)
condition|)
block|{
name|MASTER_ADDRESS
operator|=
name|args
index|[
name|i
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"--html"
argument_list|)
condition|)
block|{
name|HTML_OPTION
operator|=
name|args
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

