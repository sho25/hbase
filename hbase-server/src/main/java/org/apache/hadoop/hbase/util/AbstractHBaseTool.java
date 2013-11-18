begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|util
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
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|cli
operator|.
name|BasicParser
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
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|CommandLineParser
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
name|cli
operator|.
name|HelpFormatter
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
name|cli
operator|.
name|Options
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
name|cli
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
name|HBaseConfiguration
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * Common base class used for HBase command-line tools. Simplifies workflow and  * command-line argument parsing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractHBaseTool
implements|implements
name|Tool
block|{
specifier|protected
specifier|static
specifier|final
name|int
name|EXIT_SUCCESS
init|=
literal|0
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|EXIT_FAILURE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SHORT_HELP_OPTION
init|=
literal|"h"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LONG_HELP_OPTION
init|=
literal|"help"
decl_stmt|;
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
name|AbstractHBaseTool
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
specifier|protected
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|requiredOptions
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|String
index|[]
name|cmdLineArgs
init|=
literal|null
decl_stmt|;
comment|/**    * Override this to add command-line options using {@link #addOptWithArg}    * and similar methods.    */
specifier|protected
specifier|abstract
name|void
name|addOptions
parameter_list|()
function_decl|;
comment|/**    * This method is called to process the options after they have been parsed.    */
specifier|protected
specifier|abstract
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
function_decl|;
comment|/** The "main function" of the tool */
specifier|protected
specifier|abstract
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
function_decl|;
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Tool configuration is not initialized"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"conf"
argument_list|)
throw|;
block|}
name|CommandLine
name|cmd
decl_stmt|;
try|try
block|{
comment|// parse the command line arguments
name|cmd
operator|=
name|parseArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|cmdLineArgs
operator|=
name|args
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error when parsing command-line arguemnts"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|SHORT_HELP_OPTION
argument_list|)
operator|||
name|cmd
operator|.
name|hasOption
argument_list|(
name|LONG_HELP_OPTION
argument_list|)
operator|||
operator|!
name|sanityCheckOptions
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
name|processOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|EXIT_FAILURE
decl_stmt|;
try|try
block|{
name|ret
operator|=
name|doWork
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error running command-line tool"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|boolean
name|sanityCheckOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|boolean
name|success
init|=
literal|true
decl_stmt|;
for|for
control|(
name|String
name|reqOpt
range|:
name|requiredOptions
control|)
block|{
if|if
condition|(
operator|!
name|cmd
operator|.
name|hasOption
argument_list|(
name|reqOpt
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Required option -"
operator|+
name|reqOpt
operator|+
literal|" is missing"
argument_list|)
expr_stmt|;
name|success
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
name|success
return|;
block|}
specifier|protected
name|CommandLine
name|parseArgs
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|ParseException
block|{
name|options
operator|.
name|addOption
argument_list|(
name|SHORT_HELP_OPTION
argument_list|,
name|LONG_HELP_OPTION
argument_list|,
literal|false
argument_list|,
literal|"Show usage"
argument_list|)
expr_stmt|;
name|addOptions
argument_list|()
expr_stmt|;
name|CommandLineParser
name|parser
init|=
operator|new
name|BasicParser
argument_list|()
decl_stmt|;
return|return
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|protected
name|void
name|printUsage
parameter_list|()
block|{
name|HelpFormatter
name|helpFormatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|helpFormatter
operator|.
name|setWidth
argument_list|(
literal|80
argument_list|)
expr_stmt|;
name|String
name|usageHeader
init|=
literal|"Options:"
decl_stmt|;
name|String
name|usageFooter
init|=
literal|""
decl_stmt|;
name|String
name|usageStr
init|=
literal|"bin/hbase "
operator|+
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"<options>"
decl_stmt|;
name|helpFormatter
operator|.
name|printHelp
argument_list|(
name|usageStr
argument_list|,
name|usageHeader
argument_list|,
name|options
argument_list|,
name|usageFooter
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addRequiredOptWithArg
parameter_list|(
name|String
name|opt
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|requiredOptions
operator|.
name|add
argument_list|(
name|opt
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|opt
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addRequiredOptWithArg
parameter_list|(
name|String
name|shortOpt
parameter_list|,
name|String
name|longOpt
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|requiredOptions
operator|.
name|add
argument_list|(
name|longOpt
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|shortOpt
argument_list|,
name|longOpt
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addOptNoArg
parameter_list|(
name|String
name|opt
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|options
operator|.
name|addOption
argument_list|(
name|opt
argument_list|,
literal|false
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addOptNoArg
parameter_list|(
name|String
name|shortOpt
parameter_list|,
name|String
name|longOpt
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|options
operator|.
name|addOption
argument_list|(
name|shortOpt
argument_list|,
name|longOpt
argument_list|,
literal|false
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addOptWithArg
parameter_list|(
name|String
name|opt
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|options
operator|.
name|addOption
argument_list|(
name|opt
argument_list|,
literal|true
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addOptWithArg
parameter_list|(
name|String
name|shortOpt
parameter_list|,
name|String
name|longOpt
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|options
operator|.
name|addOption
argument_list|(
name|shortOpt
argument_list|,
name|longOpt
argument_list|,
literal|true
argument_list|,
name|description
argument_list|)
expr_stmt|;
block|}
comment|/**    * Parse a number and enforce a range.    */
specifier|public
specifier|static
name|long
name|parseLong
parameter_list|(
name|String
name|s
parameter_list|,
name|long
name|minValue
parameter_list|,
name|long
name|maxValue
parameter_list|)
block|{
name|long
name|l
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|l
argument_list|<
name|minValue
operator|||
name|l
argument_list|>
name|maxValue
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The value "
operator|+
name|l
operator|+
literal|" is out of range ["
operator|+
name|minValue
operator|+
literal|", "
operator|+
name|maxValue
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|l
return|;
block|}
specifier|public
specifier|static
name|int
name|parseInt
parameter_list|(
name|String
name|s
parameter_list|,
name|int
name|minValue
parameter_list|,
name|int
name|maxValue
parameter_list|)
block|{
return|return
operator|(
name|int
operator|)
name|parseLong
argument_list|(
name|s
argument_list|,
name|minValue
argument_list|,
name|maxValue
argument_list|)
return|;
block|}
comment|/** Call this from the concrete tool class's main function. */
specifier|protected
name|void
name|doStaticMain
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
block|{
name|int
name|ret
decl_stmt|;
try|try
block|{
name|ret
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|this
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error running command-line tool"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|ret
operator|=
name|EXIT_FAILURE
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

