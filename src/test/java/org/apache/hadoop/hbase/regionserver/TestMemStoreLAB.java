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
name|*
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
name|AtomicInteger
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
name|MultithreadedTestUtil
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
name|MultithreadedTestUtil
operator|.
name|TestThread
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
name|regionserver
operator|.
name|MemStoreLAB
operator|.
name|Allocation
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Ints
import|;
end_import

begin_class
specifier|public
class|class
name|TestMemStoreLAB
block|{
comment|/**    * Test a bunch of random allocations    */
annotation|@
name|Test
specifier|public
name|void
name|testLABRandomAllocation
parameter_list|()
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLAB
argument_list|()
decl_stmt|;
name|int
name|expectedOff
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|lastBuffer
init|=
literal|null
decl_stmt|;
comment|// 100K iterations by 0-1K alloc -> 50MB expected
comment|// should be reasonable for unit test and also cover wraparound
comment|// behavior
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100000
condition|;
name|i
operator|++
control|)
block|{
name|int
name|size
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
name|Allocation
name|alloc
init|=
name|mslab
operator|.
name|allocateBytes
argument_list|(
name|size
argument_list|)
decl_stmt|;
if|if
condition|(
name|alloc
operator|.
name|getData
argument_list|()
operator|!=
name|lastBuffer
condition|)
block|{
name|expectedOff
operator|=
literal|0
expr_stmt|;
name|lastBuffer
operator|=
name|alloc
operator|.
name|getData
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedOff
argument_list|,
name|alloc
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Allocation "
operator|+
name|alloc
operator|+
literal|" overruns buffer"
argument_list|,
name|alloc
operator|.
name|getOffset
argument_list|()
operator|+
name|size
operator|<=
name|alloc
operator|.
name|getData
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|expectedOff
operator|+=
name|size
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLABLargeAllocation
parameter_list|()
block|{
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLAB
argument_list|()
decl_stmt|;
name|Allocation
name|alloc
init|=
name|mslab
operator|.
name|allocateBytes
argument_list|(
literal|2
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"2MB allocation shouldn't be satisfied by LAB."
argument_list|,
name|alloc
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test allocation from lots of threads, making sure the results don't    * overlap in any way    */
annotation|@
name|Test
specifier|public
name|void
name|testLABThreading
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|MultithreadedTestUtil
operator|.
name|TestContext
name|ctx
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|TestContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|totalAllocated
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|MemStoreLAB
name|mslab
init|=
operator|new
name|MemStoreLAB
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|AllocRecord
argument_list|>
argument_list|>
name|allocations
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|List
argument_list|<
name|AllocRecord
argument_list|>
name|allocsByThisThread
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
name|allocations
operator|.
name|add
argument_list|(
name|allocsByThisThread
argument_list|)
expr_stmt|;
name|TestThread
name|t
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|RepeatingTestThread
argument_list|(
name|ctx
argument_list|)
block|{
specifier|private
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|size
init|=
name|r
operator|.
name|nextInt
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
name|Allocation
name|alloc
init|=
name|mslab
operator|.
name|allocateBytes
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|totalAllocated
operator|.
name|addAndGet
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|allocsByThisThread
operator|.
name|add
argument_list|(
operator|new
name|AllocRecord
argument_list|(
name|alloc
argument_list|,
name|size
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
while|while
condition|(
name|totalAllocated
operator|.
name|get
argument_list|()
operator|<
literal|50
operator|*
literal|1024
operator|*
literal|1024
operator|&&
name|ctx
operator|.
name|shouldRun
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// Partition the allocations by the actual byte[] they point into,
comment|// make sure offsets are unique for each chunk
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|AllocRecord
argument_list|>
argument_list|>
name|mapsByChunk
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|int
name|sizeCounted
init|=
literal|0
decl_stmt|;
for|for
control|(
name|AllocRecord
name|rec
range|:
name|Iterables
operator|.
name|concat
argument_list|(
name|allocations
argument_list|)
control|)
block|{
name|sizeCounted
operator|+=
name|rec
operator|.
name|size
expr_stmt|;
if|if
condition|(
name|rec
operator|.
name|size
operator|==
literal|0
condition|)
continue|continue;
name|Map
argument_list|<
name|Integer
argument_list|,
name|AllocRecord
argument_list|>
name|mapForThisByteArray
init|=
name|mapsByChunk
operator|.
name|get
argument_list|(
name|rec
operator|.
name|alloc
operator|.
name|getData
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapForThisByteArray
operator|==
literal|null
condition|)
block|{
name|mapForThisByteArray
operator|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
expr_stmt|;
name|mapsByChunk
operator|.
name|put
argument_list|(
name|rec
operator|.
name|alloc
operator|.
name|getData
argument_list|()
argument_list|,
name|mapForThisByteArray
argument_list|)
expr_stmt|;
block|}
name|AllocRecord
name|oldVal
init|=
name|mapForThisByteArray
operator|.
name|put
argument_list|(
name|rec
operator|.
name|alloc
operator|.
name|getOffset
argument_list|()
argument_list|,
name|rec
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Already had an entry "
operator|+
name|oldVal
operator|+
literal|" for allocation "
operator|+
name|rec
argument_list|,
name|oldVal
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Sanity check test"
argument_list|,
name|sizeCounted
argument_list|,
name|totalAllocated
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now check each byte array to make sure allocations don't overlap
for|for
control|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|AllocRecord
argument_list|>
name|allocsInChunk
range|:
name|mapsByChunk
operator|.
name|values
argument_list|()
control|)
block|{
name|int
name|expectedOff
init|=
literal|0
decl_stmt|;
for|for
control|(
name|AllocRecord
name|alloc
range|:
name|allocsInChunk
operator|.
name|values
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|expectedOff
argument_list|,
name|alloc
operator|.
name|alloc
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Allocation "
operator|+
name|alloc
operator|+
literal|" overruns buffer"
argument_list|,
name|alloc
operator|.
name|alloc
operator|.
name|getOffset
argument_list|()
operator|+
name|alloc
operator|.
name|size
operator|<=
name|alloc
operator|.
name|alloc
operator|.
name|getData
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|expectedOff
operator|+=
name|alloc
operator|.
name|size
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|AllocRecord
implements|implements
name|Comparable
argument_list|<
name|AllocRecord
argument_list|>
block|{
specifier|private
specifier|final
name|Allocation
name|alloc
decl_stmt|;
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
specifier|public
name|AllocRecord
parameter_list|(
name|Allocation
name|alloc
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|alloc
operator|=
name|alloc
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|AllocRecord
name|e
parameter_list|)
block|{
if|if
condition|(
name|alloc
operator|.
name|getData
argument_list|()
operator|!=
name|e
operator|.
name|alloc
operator|.
name|getData
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can only compare within a particular array"
argument_list|)
throw|;
block|}
return|return
name|Ints
operator|.
name|compare
argument_list|(
name|alloc
operator|.
name|getOffset
argument_list|()
argument_list|,
name|e
operator|.
name|alloc
operator|.
name|getOffset
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AllocRecord(alloc="
operator|+
name|alloc
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

