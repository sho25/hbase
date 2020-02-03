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
name|regionserver
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
name|assertTrue
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
name|ThreadLocalRandom
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
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|RegionServerTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSyncTimeRangeTracker
extends|extends
name|TestSimpleTimeRangeTracker
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestSyncTimeRangeTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_KEYS
init|=
literal|10000000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_OF_THREADS
init|=
literal|20
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|()
block|{
return|return
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|SYNC
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|(
name|long
name|min
parameter_list|,
name|long
name|max
parameter_list|)
block|{
return|return
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|SYNC
argument_list|,
name|min
argument_list|,
name|max
argument_list|)
return|;
block|}
comment|/**    * Run a bunch of threads against a single TimeRangeTracker and ensure we arrive    * at right range.  Here we do ten threads each incrementing over 100k at an offset    * of the thread index; max is 10 * 10k and min is 0.    */
annotation|@
name|Test
specifier|public
name|void
name|testArriveAtRightAnswer
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|TimeRangeTracker
name|trr
init|=
name|getTimeRangeTracker
argument_list|()
decl_stmt|;
specifier|final
name|int
name|threadCount
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|calls
init|=
literal|1000
operator|*
literal|1000
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|threadCount
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
literal|""
operator|+
name|i
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|int
name|offset
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|even
init|=
name|offset
operator|%
literal|2
operator|==
literal|0
decl_stmt|;
if|if
condition|(
name|even
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
operator|(
name|offset
operator|*
name|calls
operator|)
init|;
name|i
operator|<
name|calls
condition|;
name|i
operator|++
control|)
block|{
name|trr
operator|.
name|includeTimestamp
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|int
name|base
init|=
name|offset
operator|*
name|calls
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|base
operator|+
name|calls
init|;
name|i
operator|>=
name|base
condition|;
name|i
operator|--
control|)
block|{
name|trr
operator|.
name|includeTimestamp
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|=
name|t
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|trr
operator|.
name|getMax
argument_list|()
operator|==
name|calls
operator|*
name|threadCount
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|trr
operator|.
name|getMin
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|RandomTestData
block|{
specifier|private
name|long
index|[]
name|keys
init|=
operator|new
name|long
index|[
name|NUM_KEYS
index|]
decl_stmt|;
specifier|private
name|long
name|min
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|long
name|max
init|=
literal|0
decl_stmt|;
specifier|public
name|RandomTestData
parameter_list|()
block|{
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|NUM_OF_THREADS
argument_list|)
operator|%
literal|2
operator|==
literal|0
condition|)
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
name|NUM_KEYS
condition|;
name|i
operator|++
control|)
block|{
name|keys
index|[
name|i
index|]
operator|=
name|i
operator|+
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|NUM_OF_THREADS
argument_list|)
expr_stmt|;
if|if
condition|(
name|keys
index|[
name|i
index|]
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|keys
index|[
name|i
index|]
expr_stmt|;
block|}
if|if
condition|(
name|keys
index|[
name|i
index|]
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|keys
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
name|NUM_KEYS
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|keys
index|[
name|i
index|]
operator|=
name|i
operator|+
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|NUM_OF_THREADS
argument_list|)
expr_stmt|;
if|if
condition|(
name|keys
index|[
name|i
index|]
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|keys
index|[
name|i
index|]
expr_stmt|;
block|}
if|if
condition|(
name|keys
index|[
name|i
index|]
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|keys
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
name|long
name|getMax
parameter_list|()
block|{
return|return
name|this
operator|.
name|max
return|;
block|}
specifier|public
name|long
name|getMin
parameter_list|()
block|{
return|return
name|this
operator|.
name|min
return|;
block|}
block|}
specifier|static
class|class
name|TrtUpdateRunnable
implements|implements
name|Runnable
block|{
specifier|private
name|TimeRangeTracker
name|trt
decl_stmt|;
specifier|private
name|RandomTestData
name|data
decl_stmt|;
specifier|public
name|TrtUpdateRunnable
parameter_list|(
specifier|final
name|TimeRangeTracker
name|trt
parameter_list|,
specifier|final
name|RandomTestData
name|data
parameter_list|)
block|{
name|this
operator|.
name|trt
operator|=
name|trt
expr_stmt|;
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|long
name|key
range|:
name|data
operator|.
name|keys
control|)
block|{
name|trt
operator|.
name|includeTimestamp
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Run a bunch of threads against a single TimeRangeTracker and ensure we arrive    * at right range.  The data chosen is going to ensure that there are lots collisions, i.e,    * some other threads may already update the value while one tries to update min/max value.    */
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentIncludeTimestampCorrectness
parameter_list|()
block|{
name|RandomTestData
index|[]
name|testData
init|=
operator|new
name|RandomTestData
index|[
name|NUM_OF_THREADS
index|]
decl_stmt|;
name|long
name|min
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|,
name|max
init|=
literal|0
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
name|NUM_OF_THREADS
condition|;
name|i
operator|++
control|)
block|{
name|testData
index|[
name|i
index|]
operator|=
operator|new
name|RandomTestData
argument_list|()
expr_stmt|;
if|if
condition|(
name|testData
index|[
name|i
index|]
operator|.
name|getMin
argument_list|()
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|testData
index|[
name|i
index|]
operator|.
name|getMin
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|testData
index|[
name|i
index|]
operator|.
name|getMax
argument_list|()
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|testData
index|[
name|i
index|]
operator|.
name|getMax
argument_list|()
expr_stmt|;
block|}
block|}
name|TimeRangeTracker
name|trt
init|=
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|SYNC
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|t
init|=
operator|new
name|Thread
index|[
name|NUM_OF_THREADS
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
name|NUM_OF_THREADS
condition|;
name|i
operator|++
control|)
block|{
name|t
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|TrtUpdateRunnable
argument_list|(
name|trt
argument_list|,
name|testData
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|t
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|thread
range|:
name|t
control|)
block|{
try|try
block|{
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|min
operator|==
name|trt
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|max
operator|==
name|trt
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

