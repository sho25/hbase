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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

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
name|PrintWriter
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HealthChecker
operator|.
name|HealthCheckerExitStatus
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
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestNodeHealthCheckChore
block|{
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
name|TestNodeHealthCheckChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|File
name|healthScriptFile
decl_stmt|;
specifier|private
name|String
name|eol
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|cleanUp
parameter_list|()
throws|throws
name|IOException
block|{
comment|// delete and recreate the test directory, ensuring a clean test dir between tests
name|Path
name|testDir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|testDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|testDir
argument_list|)
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed mkdir "
operator|+
name|testDir
argument_list|)
throw|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHealthChecker
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|config
init|=
name|getConfForNodeHealthScript
argument_list|()
decl_stmt|;
name|config
operator|.
name|addResource
argument_list|(
name|healthScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|location
init|=
name|healthScriptFile
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
name|long
name|timeout
init|=
name|config
operator|.
name|getLong
argument_list|(
name|HConstants
operator|.
name|HEALTH_SCRIPT_TIMEOUT
argument_list|,
literal|2000
argument_list|)
decl_stmt|;
name|HealthChecker
name|checker
init|=
operator|new
name|HealthChecker
argument_list|()
decl_stmt|;
name|checker
operator|.
name|init
argument_list|(
name|location
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
name|String
name|normalScript
init|=
literal|"echo \"I am all fine\""
decl_stmt|;
name|createScript
argument_list|(
name|normalScript
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HealthReport
name|report
init|=
name|checker
operator|.
name|checkHealth
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Health Status:"
operator|+
name|report
operator|.
name|getHealthReport
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HealthCheckerExitStatus
operator|.
name|SUCCESS
argument_list|,
name|report
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|errorScript
init|=
literal|"echo ERROR"
operator|+
name|eol
operator|+
literal|"echo \"Server not healthy\""
decl_stmt|;
name|createScript
argument_list|(
name|errorScript
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|report
operator|=
name|checker
operator|.
name|checkHealth
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Health Status:"
operator|+
name|report
operator|.
name|getHealthReport
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HealthCheckerExitStatus
operator|.
name|FAILED
argument_list|,
name|report
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|timeOutScript
init|=
literal|"sleep 4"
operator|+
name|eol
operator|+
literal|"echo \"I am fine\""
decl_stmt|;
name|createScript
argument_list|(
name|timeOutScript
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|report
operator|=
name|checker
operator|.
name|checkHealth
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Health Status:"
operator|+
name|report
operator|.
name|getHealthReport
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HealthCheckerExitStatus
operator|.
name|TIMED_OUT
argument_list|,
name|report
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|healthScriptFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRSHealthChore
parameter_list|()
throws|throws
name|Exception
block|{
name|Stoppable
name|stop
init|=
operator|new
name|StoppableImplementation
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|getConfForNodeHealthScript
argument_list|()
decl_stmt|;
name|String
name|errorScript
init|=
literal|"echo ERROR"
operator|+
name|eol
operator|+
literal|" echo \"Server not healthy\""
decl_stmt|;
name|createScript
argument_list|(
name|errorScript
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HealthCheckChore
name|rsChore
init|=
operator|new
name|HealthCheckChore
argument_list|(
literal|100
argument_list|,
name|stop
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
comment|//Default threshold is three.
name|rsChore
operator|.
name|chore
argument_list|()
expr_stmt|;
name|rsChore
operator|.
name|chore
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Stoppable must not be stopped."
argument_list|,
name|stop
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
name|rsChore
operator|.
name|chore
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Stoppable must have been stopped."
argument_list|,
name|stop
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stop
operator|.
name|stop
argument_list|(
literal|"Finished w/ test"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createScript
parameter_list|(
name|String
name|scriptStr
parameter_list|,
name|boolean
name|setExecutable
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|healthScriptFile
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|healthScriptFile
operator|.
name|createNewFile
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed create of "
operator|+
name|this
operator|.
name|healthScriptFile
argument_list|)
throw|;
block|}
block|}
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|healthScriptFile
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|pw
operator|.
name|println
argument_list|(
name|scriptStr
argument_list|)
expr_stmt|;
name|pw
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|pw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|healthScriptFile
operator|.
name|setExecutable
argument_list|(
name|setExecutable
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created "
operator|+
name|this
operator|.
name|healthScriptFile
operator|+
literal|", executable="
operator|+
name|setExecutable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Configuration
name|getConfForNodeHealthScript
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|File
name|tempDir
init|=
operator|new
name|File
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tempDir
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|tempDir
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed mkdirs "
operator|+
name|tempDir
argument_list|)
throw|;
block|}
block|}
name|String
name|scriptName
init|=
name|Shell
operator|.
name|WINDOWS
condition|?
literal|"HealthScript.cmd"
else|:
literal|"HealthScript.sh"
decl_stmt|;
name|healthScriptFile
operator|=
operator|new
name|File
argument_list|(
name|tempDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|scriptName
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HEALTH_SCRIPT_LOC
argument_list|,
name|healthScriptFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HEALTH_FAILURE_THRESHOLD
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HEALTH_SCRIPT_TIMEOUT
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Simple helper class that just keeps track of whether or not its stopped.    */
specifier|private
specifier|static
class|class
name|StoppableImplementation
implements|implements
name|Stoppable
block|{
specifier|private
specifier|volatile
name|boolean
name|stop
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|this
operator|.
name|stop
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stop
return|;
block|}
block|}
block|}
end_class

end_unit

