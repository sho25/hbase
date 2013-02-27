begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|ArrayList
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
name|util
operator|.
name|Shell
operator|.
name|ExitCodeException
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
name|Shell
operator|.
name|ShellCommandExecutor
import|;
end_import

begin_comment
comment|/**  * A utility for executing an external script that checks the health of  * the node. An example script can be found at  *<tt>src/main/sh/healthcheck/healthcheck.sh</tt> in the  *<tt>hbase-examples</tt> module.  */
end_comment

begin_class
class|class
name|HealthChecker
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HealthChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ShellCommandExecutor
name|shexec
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|exceptionStackTrace
decl_stmt|;
comment|/** Pattern used for searching in the output of the node health script */
specifier|static
specifier|private
specifier|final
name|String
name|ERROR_PATTERN
init|=
literal|"ERROR"
decl_stmt|;
specifier|private
name|String
name|healthCheckScript
decl_stmt|;
specifier|private
name|long
name|scriptTimeout
decl_stmt|;
enum|enum
name|HealthCheckerExitStatus
block|{
name|SUCCESS
block|,
name|TIMED_OUT
block|,
name|FAILED_WITH_EXIT_CODE
block|,
name|FAILED_WITH_EXCEPTION
block|,
name|FAILED
block|}
comment|/**    * Initialize.    *    * @param configuration    */
specifier|public
name|void
name|init
parameter_list|(
name|String
name|location
parameter_list|,
name|long
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|healthCheckScript
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|scriptTimeout
operator|=
name|timeout
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|execScript
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|execScript
operator|.
name|add
argument_list|(
name|healthCheckScript
argument_list|)
expr_stmt|;
name|this
operator|.
name|shexec
operator|=
operator|new
name|ShellCommandExecutor
argument_list|(
name|execScript
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|execScript
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|scriptTimeout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HealthChecker initialized with script at "
operator|+
name|this
operator|.
name|healthCheckScript
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HealthReport
name|checkHealth
parameter_list|()
block|{
name|HealthCheckerExitStatus
name|status
init|=
name|HealthCheckerExitStatus
operator|.
name|SUCCESS
decl_stmt|;
try|try
block|{
comment|// Calling this execute leaves around running executor threads.
name|shexec
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitCodeException
name|e
parameter_list|)
block|{
comment|// ignore the exit code of the script
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught exception : "
operator|+
name|e
argument_list|)
expr_stmt|;
name|status
operator|=
name|HealthCheckerExitStatus
operator|.
name|FAILED_WITH_EXIT_CODE
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught exception : "
operator|+
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|shexec
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
name|status
operator|=
name|HealthCheckerExitStatus
operator|.
name|FAILED_WITH_EXCEPTION
expr_stmt|;
name|exceptionStackTrace
operator|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|status
operator|=
name|HealthCheckerExitStatus
operator|.
name|TIMED_OUT
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|status
operator|==
name|HealthCheckerExitStatus
operator|.
name|SUCCESS
condition|)
block|{
if|if
condition|(
name|hasErrors
argument_list|(
name|shexec
operator|.
name|getOutput
argument_list|()
argument_list|)
condition|)
block|{
name|status
operator|=
name|HealthCheckerExitStatus
operator|.
name|FAILED
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|HealthReport
argument_list|(
name|status
argument_list|,
name|getHealthReport
argument_list|(
name|status
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|hasErrors
parameter_list|(
name|String
name|output
parameter_list|)
block|{
name|String
index|[]
name|splits
init|=
name|output
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|split
range|:
name|splits
control|)
block|{
if|if
condition|(
name|split
operator|.
name|startsWith
argument_list|(
name|ERROR_PATTERN
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|String
name|getHealthReport
parameter_list|(
name|HealthCheckerExitStatus
name|status
parameter_list|)
block|{
name|String
name|healthReport
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|status
condition|)
block|{
case|case
name|SUCCESS
case|:
name|healthReport
operator|=
literal|"Server is healthy."
expr_stmt|;
break|break;
case|case
name|TIMED_OUT
case|:
name|healthReport
operator|=
literal|"Health script timed out"
expr_stmt|;
break|break;
case|case
name|FAILED_WITH_EXCEPTION
case|:
name|healthReport
operator|=
name|exceptionStackTrace
expr_stmt|;
break|break;
case|case
name|FAILED_WITH_EXIT_CODE
case|:
name|healthReport
operator|=
literal|"Health script failed with exit code."
expr_stmt|;
break|break;
case|case
name|FAILED
case|:
name|healthReport
operator|=
name|shexec
operator|.
name|getOutput
argument_list|()
expr_stmt|;
break|break;
block|}
return|return
name|healthReport
return|;
block|}
block|}
end_class

end_unit

