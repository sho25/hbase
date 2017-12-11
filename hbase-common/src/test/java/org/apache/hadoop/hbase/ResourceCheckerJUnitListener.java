begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ResourceChecker
operator|.
name|Phase
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
name|util
operator|.
name|JVM
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|notification
operator|.
name|RunListener
import|;
end_import

begin_comment
comment|/**  * Listen to the test progress and check the usage of:  * - threads  * - open file descriptor  * - max open file descriptor  *<p/>  * When surefire forkMode=once/always/perthread, this code is executed on the forked process.  */
end_comment

begin_class
specifier|public
class|class
name|ResourceCheckerJUnitListener
extends|extends
name|RunListener
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ResourceChecker
argument_list|>
name|rcs
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
class|class
name|ThreadResourceAnalyzer
extends|extends
name|ResourceChecker
operator|.
name|ResourceAnalyzer
block|{
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|initialThreadNames
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|stringsToLog
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|getVal
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
name|Map
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|stackTraces
init|=
name|Thread
operator|.
name|getAllStackTraces
argument_list|()
decl_stmt|;
if|if
condition|(
name|phase
operator|==
name|Phase
operator|.
name|INITIAL
condition|)
block|{
name|stringsToLog
operator|=
literal|null
expr_stmt|;
for|for
control|(
name|Thread
name|t
range|:
name|stackTraces
operator|.
name|keySet
argument_list|()
control|)
block|{
name|initialThreadNames
operator|.
name|add
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|phase
operator|==
name|Phase
operator|.
name|END
condition|)
block|{
if|if
condition|(
name|stackTraces
operator|.
name|size
argument_list|()
operator|>
name|initialThreadNames
operator|.
name|size
argument_list|()
condition|)
block|{
name|stringsToLog
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|Thread
name|t
range|:
name|stackTraces
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|initialThreadNames
operator|.
name|contains
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|stringsToLog
operator|.
name|add
argument_list|(
literal|"\nPotentially hanging thread: "
operator|+
name|t
operator|.
name|getName
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|StackTraceElement
index|[]
name|stackElements
init|=
name|stackTraces
operator|.
name|get
argument_list|(
name|t
argument_list|)
decl_stmt|;
for|for
control|(
name|StackTraceElement
name|ele
range|:
name|stackElements
control|)
block|{
name|stringsToLog
operator|.
name|add
argument_list|(
literal|"\t"
operator|+
name|ele
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|stackTraces
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMax
parameter_list|()
block|{
return|return
literal|500
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getStringsToLog
parameter_list|()
block|{
return|return
name|stringsToLog
return|;
block|}
block|}
specifier|static
class|class
name|OpenFileDescriptorResourceAnalyzer
extends|extends
name|ResourceChecker
operator|.
name|ResourceAnalyzer
block|{
annotation|@
name|Override
specifier|public
name|int
name|getVal
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
if|if
condition|(
operator|!
name|JVM
operator|.
name|isUnix
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
name|JVM
name|jvm
init|=
operator|new
name|JVM
argument_list|()
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|jvm
operator|.
name|getOpenFileDescriptorCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMax
parameter_list|()
block|{
return|return
literal|1024
return|;
block|}
block|}
specifier|static
class|class
name|MaxFileDescriptorResourceAnalyzer
extends|extends
name|ResourceChecker
operator|.
name|ResourceAnalyzer
block|{
annotation|@
name|Override
specifier|public
name|int
name|getVal
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
if|if
condition|(
operator|!
name|JVM
operator|.
name|isUnix
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
name|JVM
name|jvm
init|=
operator|new
name|JVM
argument_list|()
decl_stmt|;
return|return
operator|(
name|int
operator|)
name|jvm
operator|.
name|getMaxFileDescriptorCount
argument_list|()
return|;
block|}
block|}
specifier|static
class|class
name|SystemLoadAverageResourceAnalyzer
extends|extends
name|ResourceChecker
operator|.
name|ResourceAnalyzer
block|{
annotation|@
name|Override
specifier|public
name|int
name|getVal
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
if|if
condition|(
operator|!
name|JVM
operator|.
name|isUnix
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
call|(
name|int
call|)
argument_list|(
operator|new
name|JVM
argument_list|()
operator|.
name|getSystemLoadAverage
argument_list|()
operator|*
literal|100
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|ProcessCountResourceAnalyzer
extends|extends
name|ResourceChecker
operator|.
name|ResourceAnalyzer
block|{
annotation|@
name|Override
specifier|public
name|int
name|getVal
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
if|if
condition|(
operator|!
name|JVM
operator|.
name|isUnix
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
operator|new
name|JVM
argument_list|()
operator|.
name|getNumberOfRunningProcess
argument_list|()
return|;
block|}
block|}
specifier|static
class|class
name|AvailableMemoryMBResourceAnalyzer
extends|extends
name|ResourceChecker
operator|.
name|ResourceAnalyzer
block|{
annotation|@
name|Override
specifier|public
name|int
name|getVal
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
if|if
condition|(
operator|!
name|JVM
operator|.
name|isUnix
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
call|(
name|int
call|)
argument_list|(
operator|new
name|JVM
argument_list|()
operator|.
name|getFreeMemory
argument_list|()
operator|/
operator|(
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|)
return|;
block|}
block|}
comment|/**    * To be implemented by sub classes if they want to add specific ResourceAnalyzer.    */
specifier|protected
name|void
name|addResourceAnalyzer
parameter_list|(
name|ResourceChecker
name|rc
parameter_list|)
block|{   }
specifier|private
name|void
name|start
parameter_list|(
name|String
name|testName
parameter_list|)
block|{
name|ResourceChecker
name|rc
init|=
operator|new
name|ResourceChecker
argument_list|(
name|testName
argument_list|)
decl_stmt|;
name|rc
operator|.
name|addResourceAnalyzer
argument_list|(
operator|new
name|ThreadResourceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|rc
operator|.
name|addResourceAnalyzer
argument_list|(
operator|new
name|OpenFileDescriptorResourceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|rc
operator|.
name|addResourceAnalyzer
argument_list|(
operator|new
name|MaxFileDescriptorResourceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|rc
operator|.
name|addResourceAnalyzer
argument_list|(
operator|new
name|SystemLoadAverageResourceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|rc
operator|.
name|addResourceAnalyzer
argument_list|(
operator|new
name|ProcessCountResourceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|rc
operator|.
name|addResourceAnalyzer
argument_list|(
operator|new
name|AvailableMemoryMBResourceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|addResourceAnalyzer
argument_list|(
name|rc
argument_list|)
expr_stmt|;
name|rcs
operator|.
name|put
argument_list|(
name|testName
argument_list|,
name|rc
argument_list|)
expr_stmt|;
name|rc
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|end
parameter_list|(
name|String
name|testName
parameter_list|)
block|{
name|ResourceChecker
name|rc
init|=
name|rcs
operator|.
name|remove
argument_list|(
name|testName
argument_list|)
decl_stmt|;
assert|assert
name|rc
operator|!=
literal|null
assert|;
name|rc
operator|.
name|end
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the test name from the JUnit Description    *    * @return the string for the short test name    */
specifier|private
name|String
name|descriptionToShortTestName
parameter_list|(
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
name|description
parameter_list|)
block|{
specifier|final
name|int
name|toRemove
init|=
literal|"org.apache.hadoop.hbase."
operator|.
name|length
argument_list|()
decl_stmt|;
return|return
name|description
operator|.
name|getTestClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
name|toRemove
argument_list|)
operator|+
literal|"#"
operator|+
name|description
operator|.
name|getMethodName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|testStarted
parameter_list|(
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
name|description
parameter_list|)
throws|throws
name|java
operator|.
name|lang
operator|.
name|Exception
block|{
name|start
argument_list|(
name|descriptionToShortTestName
argument_list|(
name|description
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|testFinished
parameter_list|(
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
name|description
parameter_list|)
throws|throws
name|java
operator|.
name|lang
operator|.
name|Exception
block|{
name|end
argument_list|(
name|descriptionToShortTestName
argument_list|(
name|description
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

