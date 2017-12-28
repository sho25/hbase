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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadInfo
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|CountDownLatch
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
name|TimeoutException
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|FileUtil
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
name|Time
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * Test provides some very generic helpers which might be used across the tests  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|GenericTestUtils
block|{
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|sequence
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
comment|/**    * Extracts the name of the method where the invocation has happened    * @return String name of the invoking method    */
specifier|public
specifier|static
name|String
name|getMethodName
parameter_list|()
block|{
return|return
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
index|[
literal|2
index|]
operator|.
name|getMethodName
argument_list|()
return|;
block|}
comment|/**    * Generates a process-wide unique sequence number.    * @return an unique sequence number    */
specifier|public
specifier|static
name|int
name|uniqueSequenceId
parameter_list|()
block|{
return|return
name|sequence
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
comment|/**    * Assert that a given file exists.    */
specifier|public
specifier|static
name|void
name|assertExists
parameter_list|(
name|File
name|f
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"File "
operator|+
name|f
operator|+
literal|" should exist"
argument_list|,
name|f
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * List all of the files in 'dir' that match the regex 'pattern'.    * Then check that this list is identical to 'expectedMatches'.    * @throws IOException if the dir is inaccessible    */
specifier|public
specifier|static
name|void
name|assertGlobEquals
parameter_list|(
name|File
name|dir
parameter_list|,
name|String
name|pattern
parameter_list|,
name|String
modifier|...
name|expectedMatches
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|found
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|f
range|:
name|FileUtil
operator|.
name|listFiles
argument_list|(
name|dir
argument_list|)
control|)
block|{
if|if
condition|(
name|f
operator|.
name|getName
argument_list|()
operator|.
name|matches
argument_list|(
name|pattern
argument_list|)
condition|)
block|{
name|found
operator|.
name|add
argument_list|(
name|f
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|expectedSet
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|expectedMatches
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Bad files matching "
operator|+
name|pattern
operator|+
literal|" in "
operator|+
name|dir
argument_list|,
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|expectedSet
argument_list|)
argument_list|,
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|found
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|waitFor
parameter_list|(
name|Supplier
argument_list|<
name|Boolean
argument_list|>
name|check
parameter_list|,
name|int
name|checkEveryMillis
parameter_list|,
name|int
name|waitForMillis
parameter_list|)
throws|throws
name|TimeoutException
throws|,
name|InterruptedException
block|{
name|long
name|st
init|=
name|Time
operator|.
name|now
argument_list|()
decl_stmt|;
do|do
block|{
name|boolean
name|result
init|=
name|check
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
condition|)
block|{
return|return;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|checkEveryMillis
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|Time
operator|.
name|now
argument_list|()
operator|-
name|st
operator|<
name|waitForMillis
condition|)
do|;
throw|throw
operator|new
name|TimeoutException
argument_list|(
literal|"Timed out waiting for condition. "
operator|+
literal|"Thread diagnostics:\n"
operator|+
name|TimedOutTestsListener
operator|.
name|buildThreadDiagnosticString
argument_list|()
argument_list|)
throw|;
block|}
comment|/**    * Mockito answer helper that triggers one latch as soon as the    * method is called, then waits on another before continuing.    */
specifier|public
specifier|static
class|class
name|DelayAnswer
implements|implements
name|Answer
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
specifier|final
name|Logger
name|LOG
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|fireLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|waitLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|resultLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|fireCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|resultCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Result fields set after proceed() is called.
specifier|private
specifier|volatile
name|Throwable
name|thrown
decl_stmt|;
specifier|private
specifier|volatile
name|Object
name|returnValue
decl_stmt|;
specifier|public
name|DelayAnswer
parameter_list|(
name|Logger
name|log
parameter_list|)
block|{
name|this
operator|.
name|LOG
operator|=
name|log
expr_stmt|;
block|}
comment|/**      * Wait until the method is called.      */
specifier|public
name|void
name|waitForCall
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|fireLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
comment|/**      * Tell the method to proceed.      * This should only be called after waitForCall()      */
specifier|public
name|void
name|proceed
parameter_list|()
block|{
name|waitLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"DelayAnswer firing fireLatch"
argument_list|)
expr_stmt|;
name|fireCounter
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|fireLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"DelayAnswer waiting on waitLatch"
argument_list|)
expr_stmt|;
name|waitLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"DelayAnswer delay complete"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Interrupted waiting on latch"
argument_list|,
name|ie
argument_list|)
throw|;
block|}
return|return
name|passThrough
argument_list|(
name|invocation
argument_list|)
return|;
block|}
specifier|protected
name|Object
name|passThrough
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
try|try
block|{
name|Object
name|ret
init|=
name|invocation
operator|.
name|callRealMethod
argument_list|()
decl_stmt|;
name|returnValue
operator|=
name|ret
expr_stmt|;
return|return
name|ret
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|thrown
operator|=
name|t
expr_stmt|;
throw|throw
name|t
throw|;
block|}
finally|finally
block|{
name|resultCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|resultLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * After calling proceed(), this will wait until the call has      * completed and a result has been returned to the caller.      */
specifier|public
name|void
name|waitForResult
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|resultLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
comment|/**      * After the call has gone through, return any exception that      * was thrown, or null if no exception was thrown.      */
specifier|public
name|Throwable
name|getThrown
parameter_list|()
block|{
return|return
name|thrown
return|;
block|}
comment|/**      * After the call has gone through, return the call's return value,      * or null in case it was void or an exception was thrown.      */
specifier|public
name|Object
name|getReturnValue
parameter_list|()
block|{
return|return
name|returnValue
return|;
block|}
specifier|public
name|int
name|getFireCount
parameter_list|()
block|{
return|return
name|fireCounter
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|int
name|getResultCount
parameter_list|()
block|{
return|return
name|resultCounter
operator|.
name|get
argument_list|()
return|;
block|}
block|}
comment|/**    * An Answer implementation that simply forwards all calls through    * to a delegate.    *    * This is useful as the default Answer for a mock object, to create    * something like a spy on an RPC proxy. For example:    *<code>    *    NamenodeProtocol origNNProxy = secondary.getNameNode();    *    NamenodeProtocol spyNNProxy = Mockito.mock(NameNodeProtocol.class,    *        new DelegateAnswer(origNNProxy);    *    doThrow(...).when(spyNNProxy).getBlockLocations(...);    *    ...    *</code>    */
specifier|public
specifier|static
class|class
name|DelegateAnswer
implements|implements
name|Answer
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
specifier|final
name|Object
name|delegate
decl_stmt|;
specifier|private
specifier|final
name|Logger
name|log
decl_stmt|;
specifier|public
name|DelegateAnswer
parameter_list|(
name|Object
name|delegate
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|delegate
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DelegateAnswer
parameter_list|(
name|Logger
name|log
parameter_list|,
name|Object
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|log
operator|=
name|log
expr_stmt|;
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
try|try
block|{
if|if
condition|(
name|log
operator|!=
literal|null
condition|)
block|{
name|log
operator|.
name|info
argument_list|(
literal|"Call to "
operator|+
name|invocation
operator|+
literal|" on "
operator|+
name|delegate
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"TRACE"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|invocation
operator|.
name|getMethod
argument_list|()
operator|.
name|invoke
argument_list|(
name|delegate
argument_list|,
name|invocation
operator|.
name|getArguments
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
throw|throw
name|ite
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
block|}
comment|/**    * An Answer implementation which sleeps for a random number of milliseconds    * between 0 and a configurable value before delegating to the real    * implementation of the method. This can be useful for drawing out race    * conditions.    */
specifier|public
specifier|static
class|class
name|SleepAnswer
implements|implements
name|Answer
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|maxSleepTime
decl_stmt|;
specifier|private
specifier|static
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|SleepAnswer
parameter_list|(
name|int
name|maxSleepTime
parameter_list|)
block|{
name|this
operator|.
name|maxSleepTime
operator|=
name|maxSleepTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|r
operator|.
name|nextInt
argument_list|(
name|maxSleepTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|interrupted
operator|=
literal|true
expr_stmt|;
block|}
try|try
block|{
return|return
name|invocation
operator|.
name|callRealMethod
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|interrupted
condition|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|assertMatches
parameter_list|(
name|String
name|output
parameter_list|,
name|String
name|pattern
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Expected output to match /"
operator|+
name|pattern
operator|+
literal|"/"
operator|+
literal|" but got:\n"
operator|+
name|output
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|pattern
argument_list|)
operator|.
name|matcher
argument_list|(
name|output
argument_list|)
operator|.
name|find
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertValueNear
parameter_list|(
name|long
name|expected
parameter_list|,
name|long
name|actual
parameter_list|,
name|long
name|allowedError
parameter_list|)
block|{
name|assertValueWithinRange
argument_list|(
name|expected
operator|-
name|allowedError
argument_list|,
name|expected
operator|+
name|allowedError
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertValueWithinRange
parameter_list|(
name|long
name|expectedMin
parameter_list|,
name|long
name|expectedMax
parameter_list|,
name|long
name|actual
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Expected "
operator|+
name|actual
operator|+
literal|" to be in range ("
operator|+
name|expectedMin
operator|+
literal|","
operator|+
name|expectedMax
operator|+
literal|")"
argument_list|,
name|expectedMin
operator|<=
name|actual
operator|&&
name|actual
operator|<=
name|expectedMax
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert that there are no threads running whose name matches the    * given regular expression.    * @param regex the regex to match against    */
specifier|public
specifier|static
name|void
name|assertNoThreadsMatching
parameter_list|(
name|String
name|regex
parameter_list|)
block|{
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|)
decl_stmt|;
name|ThreadMXBean
name|threadBean
init|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
decl_stmt|;
name|ThreadInfo
index|[]
name|infos
init|=
name|threadBean
operator|.
name|getThreadInfo
argument_list|(
name|threadBean
operator|.
name|getAllThreadIds
argument_list|()
argument_list|,
literal|20
argument_list|)
decl_stmt|;
for|for
control|(
name|ThreadInfo
name|info
range|:
name|infos
control|)
block|{
if|if
condition|(
name|info
operator|==
literal|null
condition|)
continue|continue;
if|if
condition|(
name|pattern
operator|.
name|matcher
argument_list|(
name|info
operator|.
name|getThreadName
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Leaked thread: "
operator|+
name|info
operator|+
literal|"\n"
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|join
argument_list|(
name|info
operator|.
name|getStackTrace
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

