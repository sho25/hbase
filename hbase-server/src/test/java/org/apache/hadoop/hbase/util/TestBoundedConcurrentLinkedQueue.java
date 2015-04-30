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
name|assertTrue
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
name|List
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
name|After
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
name|TestBoundedConcurrentLinkedQueue
block|{
specifier|private
specifier|final
specifier|static
name|int
name|CAPACITY
init|=
literal|16
decl_stmt|;
specifier|private
name|BoundedConcurrentLinkedQueue
argument_list|<
name|Long
argument_list|>
name|queue
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|queue
operator|=
operator|new
name|BoundedConcurrentLinkedQueue
argument_list|<
name|Long
argument_list|>
argument_list|(
name|CAPACITY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{   }
annotation|@
name|Test
specifier|public
name|void
name|testOfferAndPoll
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Offer
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|CAPACITY
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CAPACITY
operator|-
name|i
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Poll
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|CAPACITY
condition|;
operator|++
name|i
control|)
block|{
name|long
name|l
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|l
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CAPACITY
operator|-
name|i
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|queue
operator|.
name|poll
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDrain
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Offer
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|CAPACITY
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CAPACITY
operator|-
name|i
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Drain
name|List
argument_list|<
name|Long
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|queue
operator|.
name|drainTo
argument_list|(
name|list
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|queue
operator|.
name|poll
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CAPACITY
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

