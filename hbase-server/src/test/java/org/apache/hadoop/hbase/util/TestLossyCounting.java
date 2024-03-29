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
name|TestLossyCounting
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
name|TestLossyCounting
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testBucketSize
parameter_list|()
block|{
name|LossyCounting
argument_list|<
name|?
argument_list|>
name|lossyCounting
init|=
operator|new
name|LossyCounting
argument_list|<>
argument_list|(
literal|"testBucketSize"
argument_list|,
literal|0.01
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|100L
argument_list|,
name|lossyCounting
operator|.
name|getBucketSize
argument_list|()
argument_list|)
expr_stmt|;
name|LossyCounting
argument_list|<
name|?
argument_list|>
name|lossyCounting2
init|=
operator|new
name|LossyCounting
argument_list|<>
argument_list|(
literal|"testBucketSize2"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|50L
argument_list|,
name|lossyCounting2
operator|.
name|getBucketSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddByOne
parameter_list|()
block|{
name|LossyCounting
argument_list|<
name|String
argument_list|>
name|lossyCounting
init|=
operator|new
name|LossyCounting
argument_list|<>
argument_list|(
literal|"testAddByOne"
argument_list|,
literal|0.01
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|""
operator|+
name|i
decl_stmt|;
name|lossyCounting
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|100L
argument_list|,
name|lossyCounting
operator|.
name|getDataSize
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|""
operator|+
name|i
decl_stmt|;
name|assertTrue
argument_list|(
name|lossyCounting
operator|.
name|contains
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
name|testSweep1
parameter_list|()
throws|throws
name|Exception
block|{
name|LossyCounting
argument_list|<
name|String
argument_list|>
name|lossyCounting
init|=
operator|new
name|LossyCounting
argument_list|<>
argument_list|(
literal|"testSweep1"
argument_list|,
literal|0.01
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
literal|400
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|""
operator|+
name|i
decl_stmt|;
name|lossyCounting
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|4L
argument_list|,
name|lossyCounting
operator|.
name|getCurrentTerm
argument_list|()
argument_list|)
expr_stmt|;
name|waitForSweep
argument_list|(
name|lossyCounting
argument_list|)
expr_stmt|;
comment|//Do last one sweep as some sweep will be skipped when first one was running
name|lossyCounting
operator|.
name|sweep
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|lossyCounting
operator|.
name|getBucketSize
argument_list|()
operator|-
literal|1
argument_list|,
name|lossyCounting
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|waitForSweep
parameter_list|(
name|LossyCounting
argument_list|<
name|?
argument_list|>
name|lossyCounting
parameter_list|)
throws|throws
name|InterruptedException
block|{
comment|//wait for sweep thread to complete
name|int
name|retry
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|lossyCounting
operator|.
name|getSweepFuture
argument_list|()
operator|.
name|isDone
argument_list|()
operator|&&
name|retry
operator|<
literal|10
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|retry
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSweep2
parameter_list|()
throws|throws
name|Exception
block|{
name|LossyCounting
argument_list|<
name|String
argument_list|>
name|lossyCounting
init|=
operator|new
name|LossyCounting
argument_list|<>
argument_list|(
literal|"testSweep2"
argument_list|,
literal|0.1
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|""
operator|+
name|i
decl_stmt|;
name|lossyCounting
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|waitForSweep
argument_list|(
name|lossyCounting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|lossyCounting
operator|.
name|getDataSize
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|"1"
decl_stmt|;
name|lossyCounting
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|waitForSweep
argument_list|(
name|lossyCounting
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|lossyCounting
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

