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
name|types
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
name|assertNotNull
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
name|assertNull
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
name|concurrent
operator|.
name|ConcurrentNavigableMap
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
name|ConcurrentSkipListMap
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
name|testclassification
operator|.
name|MiscTests
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
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCopyOnWriteMaps
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MAX_RAND
init|=
literal|10
operator|*
literal|1000
operator|*
literal|1000
decl_stmt|;
specifier|private
name|ConcurrentNavigableMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|m
decl_stmt|;
specifier|private
name|ConcurrentSkipListMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|csm
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|m
operator|=
operator|new
name|CopyOnWriteArrayMap
argument_list|<>
argument_list|()
expr_stmt|;
name|csm
operator|=
operator|new
name|ConcurrentSkipListMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10000
condition|;
name|i
operator|++
control|)
block|{
name|long
name|o
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|MAX_RAND
argument_list|)
decl_stmt|;
name|m
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|o
argument_list|)
expr_stmt|;
name|csm
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|o
argument_list|)
expr_stmt|;
block|}
name|long
name|o
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|MAX_RAND
argument_list|)
decl_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|0L
argument_list|,
name|o
argument_list|)
expr_stmt|;
name|csm
operator|.
name|put
argument_list|(
literal|0L
argument_list|,
name|o
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSize
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|"Size should always be equal"
argument_list|,
name|m
operator|.
name|size
argument_list|()
argument_list|,
name|csm
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIsEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|m
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|100L
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|m
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|m
operator|.
name|remove
argument_list|(
literal|100L
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFindOnEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|m
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|m
operator|.
name|get
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|m
operator|.
name|containsKey
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|m
operator|.
name|tailMap
argument_list|(
literal|100L
argument_list|)
operator|.
name|entrySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLowerKey
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|lowerKey
argument_list|(
literal|400L
argument_list|)
argument_list|,
name|m
operator|.
name|lowerKey
argument_list|(
literal|400L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|lowerKey
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|,
name|m
operator|.
name|lowerKey
argument_list|(
operator|-
literal|1L
argument_list|)
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Long
name|key
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|lowerKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|m
operator|.
name|lowerKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloorEntry
parameter_list|()
throws|throws
name|Exception
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Long
name|key
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|floorEntry
argument_list|(
name|key
argument_list|)
argument_list|,
name|m
operator|.
name|floorEntry
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloorKey
parameter_list|()
throws|throws
name|Exception
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Long
name|key
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|floorKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|m
operator|.
name|floorKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCeilingKey
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|ceilingKey
argument_list|(
literal|4000L
argument_list|)
argument_list|,
name|m
operator|.
name|ceilingKey
argument_list|(
literal|4000L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|ceilingKey
argument_list|(
literal|400L
argument_list|)
argument_list|,
name|m
operator|.
name|ceilingKey
argument_list|(
literal|400L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|ceilingKey
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|,
name|m
operator|.
name|ceilingKey
argument_list|(
operator|-
literal|1L
argument_list|)
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Long
name|key
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|ceilingKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|m
operator|.
name|ceilingKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHigherKey
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|higherKey
argument_list|(
literal|4000L
argument_list|)
argument_list|,
name|m
operator|.
name|higherKey
argument_list|(
literal|4000L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|higherKey
argument_list|(
literal|400L
argument_list|)
argument_list|,
name|m
operator|.
name|higherKey
argument_list|(
literal|400L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|higherKey
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|,
name|m
operator|.
name|higherKey
argument_list|(
operator|-
literal|1L
argument_list|)
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|Long
name|key
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|higherKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|m
operator|.
name|higherKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemove
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|csm
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|remove
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|m
operator|.
name|remove
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|m
operator|.
name|remove
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplace
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|csm
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Long
name|newValue
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|replace
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|newValue
argument_list|)
argument_list|,
name|m
operator|.
name|replace
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|newValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|m
operator|.
name|replace
argument_list|(
name|MAX_RAND
operator|+
literal|100L
argument_list|,
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplace1
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|csm
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Long
name|newValue
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|replace
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|1
argument_list|,
name|newValue
argument_list|)
argument_list|,
name|m
operator|.
name|replace
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|+
literal|1
argument_list|,
name|newValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|replace
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|newValue
argument_list|)
argument_list|,
name|m
operator|.
name|replace
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|newValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newValue
argument_list|,
name|m
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|m
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|m
operator|.
name|replace
argument_list|(
name|MAX_RAND
operator|+
literal|100L
argument_list|,
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiAdd
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
literal|10
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
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|5000
condition|;
name|j
operator|++
control|)
block|{
name|m
operator|.
name|put
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|,
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|thread1
range|:
name|threads
control|)
block|{
name|thread1
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|thread2
range|:
name|threads
control|)
block|{
name|thread2
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFirstKey
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|firstKey
argument_list|()
argument_list|,
name|m
operator|.
name|firstKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLastKey
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|lastKey
argument_list|()
argument_list|,
name|m
operator|.
name|lastKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFirstEntry
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|firstEntry
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|,
name|m
operator|.
name|firstEntry
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|m
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|firstEntry
argument_list|()
argument_list|,
name|m
operator|.
name|firstEntry
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLastEntry
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|lastEntry
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|,
name|m
operator|.
name|lastEntry
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|lastEntry
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|m
operator|.
name|lastEntry
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|lastEntry
argument_list|()
argument_list|,
name|m
operator|.
name|lastEntry
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKeys
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Long
name|key
range|:
name|csm
operator|.
name|keySet
argument_list|()
control|)
block|{
comment|//assertTrue(m.containsKey(key));
name|assertNotNull
argument_list|(
name|m
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|m
operator|.
name|remove
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|m
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testValues
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Long
name|value
range|:
name|m
operator|.
name|values
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|csm
operator|.
name|values
argument_list|()
operator|.
name|contains
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTailMap
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|fromCsm
init|=
name|csm
operator|.
name|tailMap
argument_list|(
literal|50L
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|fromM
init|=
name|m
operator|.
name|tailMap
argument_list|(
literal|50L
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|fromCsm
argument_list|,
name|fromM
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|value
range|:
name|m
operator|.
name|keySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|csm
operator|.
name|tailMap
argument_list|(
name|value
argument_list|)
argument_list|,
name|m
operator|.
name|tailMap
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|long
name|o
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|MAX_RAND
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|csm
operator|.
name|tailMap
argument_list|(
name|o
argument_list|)
argument_list|,
name|m
operator|.
name|tailMap
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTailMapExclusive
parameter_list|()
throws|throws
name|Exception
block|{
name|m
operator|.
name|clear
argument_list|()
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|100L
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|101L
argument_list|,
literal|101L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|101L
argument_list|,
literal|101L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|103L
argument_list|,
literal|103L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|99L
argument_list|,
literal|99L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|102L
argument_list|,
literal|102L
argument_list|)
expr_stmt|;
name|long
name|n
init|=
literal|100L
decl_stmt|;
name|CopyOnWriteArrayMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|tm99
init|=
operator|(
name|CopyOnWriteArrayMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
operator|)
name|m
operator|.
name|tailMap
argument_list|(
literal|99L
argument_list|,
literal|false
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|tm99
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|n
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTailMapInclusive
parameter_list|()
throws|throws
name|Exception
block|{
name|m
operator|.
name|clear
argument_list|()
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|100L
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|101L
argument_list|,
literal|101L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|101L
argument_list|,
literal|101L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|103L
argument_list|,
literal|103L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|99L
argument_list|,
literal|99L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|102L
argument_list|,
literal|102L
argument_list|)
expr_stmt|;
name|long
name|n
init|=
literal|102
decl_stmt|;
name|CopyOnWriteArrayMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|tm102
init|=
operator|(
name|CopyOnWriteArrayMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
operator|)
name|m
operator|.
name|tailMap
argument_list|(
literal|102L
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|tm102
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|n
operator|++
expr_stmt|;
block|}
name|n
operator|=
literal|99
expr_stmt|;
name|CopyOnWriteArrayMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|tm98
init|=
operator|(
name|CopyOnWriteArrayMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
operator|)
name|m
operator|.
name|tailMap
argument_list|(
literal|98L
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|tm98
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|n
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPut
parameter_list|()
throws|throws
name|Exception
block|{
name|m
operator|.
name|clear
argument_list|()
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|100L
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|101L
argument_list|,
literal|101L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|101L
argument_list|,
literal|101L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|103L
argument_list|,
literal|103L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|99L
argument_list|,
literal|99L
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|102L
argument_list|,
literal|102L
argument_list|)
expr_stmt|;
name|long
name|n
init|=
literal|99
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|m
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|n
argument_list|)
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|n
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|m
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|m
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

