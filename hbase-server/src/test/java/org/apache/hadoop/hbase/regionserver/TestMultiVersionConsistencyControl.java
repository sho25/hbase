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
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|SmallTests
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * This is a hammer test that verifies MultiVersionConsistencyControl in a  * multiple writer single reader scenario.  */
end_comment

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
name|TestMultiVersionConsistencyControl
extends|extends
name|TestCase
block|{
specifier|static
class|class
name|Writer
implements|implements
name|Runnable
block|{
specifier|final
name|AtomicBoolean
name|finished
decl_stmt|;
specifier|final
name|MultiVersionConsistencyControl
name|mvcc
decl_stmt|;
specifier|final
name|AtomicBoolean
name|status
decl_stmt|;
name|Writer
parameter_list|(
name|AtomicBoolean
name|finished
parameter_list|,
name|MultiVersionConsistencyControl
name|mvcc
parameter_list|,
name|AtomicBoolean
name|status
parameter_list|)
block|{
name|this
operator|.
name|finished
operator|=
name|finished
expr_stmt|;
name|this
operator|.
name|mvcc
operator|=
name|mvcc
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
specifier|private
name|Random
name|rnd
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|boolean
name|failed
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|run
parameter_list|()
block|{
name|AtomicLong
name|startPoint
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|finished
operator|.
name|get
argument_list|()
condition|)
block|{
name|MultiVersionConsistencyControl
operator|.
name|WriteEntry
name|e
init|=
name|mvcc
operator|.
name|beginMemstoreInsertWithSeqNum
argument_list|(
name|startPoint
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
decl_stmt|;
comment|// System.out.println("Begin write: " + e.getWriteNumber());
comment|// 10 usec - 500usec (including 0)
name|int
name|sleepTime
init|=
name|rnd
operator|.
name|nextInt
argument_list|(
literal|500
argument_list|)
decl_stmt|;
comment|// 500 * 1000 = 500,000ns = 500 usec
comment|// 1 * 100 = 100ns = 1usec
try|try
block|{
if|if
condition|(
name|sleepTime
operator|>
literal|0
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
literal|0
argument_list|,
name|sleepTime
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{         }
try|try
block|{
name|mvcc
operator|.
name|completeMemstoreInsert
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
comment|// got failure
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|status
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return;
comment|// Report failure if possible.
block|}
block|}
block|}
block|}
specifier|public
name|void
name|testParallelism
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|MultiVersionConsistencyControl
name|mvcc
init|=
operator|new
name|MultiVersionConsistencyControl
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|finished
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// fail flag for the reader thread
specifier|final
name|AtomicBoolean
name|readerFailed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|AtomicLong
name|failedAt
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|Runnable
name|reader
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|prev
init|=
name|mvcc
operator|.
name|memstoreReadPoint
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|finished
operator|.
name|get
argument_list|()
condition|)
block|{
name|long
name|newPrev
init|=
name|mvcc
operator|.
name|memstoreReadPoint
argument_list|()
decl_stmt|;
if|if
condition|(
name|newPrev
operator|<
name|prev
condition|)
block|{
comment|// serious problem.
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Reader got out of order, prev: "
operator|+
name|prev
operator|+
literal|" next was: "
operator|+
name|newPrev
argument_list|)
expr_stmt|;
name|readerFailed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// might as well give up
name|failedAt
operator|.
name|set
argument_list|(
name|newPrev
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
decl_stmt|;
comment|// writer thread parallelism.
name|int
name|n
init|=
literal|20
decl_stmt|;
name|Thread
index|[]
name|writers
init|=
operator|new
name|Thread
index|[
name|n
index|]
decl_stmt|;
name|AtomicBoolean
index|[]
name|statuses
init|=
operator|new
name|AtomicBoolean
index|[
name|n
index|]
decl_stmt|;
name|Thread
name|readThread
init|=
operator|new
name|Thread
argument_list|(
name|reader
argument_list|)
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
name|n
condition|;
operator|++
name|i
control|)
block|{
name|statuses
index|[
name|i
index|]
operator|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|writers
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Writer
argument_list|(
name|finished
argument_list|,
name|mvcc
argument_list|,
name|statuses
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|writers
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|readThread
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{     }
name|finished
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|readThread
operator|.
name|join
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|writers
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
comment|// check failure.
name|assertFalse
argument_list|(
name|readerFailed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|statuses
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

