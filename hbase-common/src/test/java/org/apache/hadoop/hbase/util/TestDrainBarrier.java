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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|TestDrainBarrier
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
name|TestDrainBarrier
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testBeginEndStopWork
parameter_list|()
throws|throws
name|Exception
block|{
name|DrainBarrier
name|barrier
init|=
operator|new
name|DrainBarrier
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|barrier
operator|.
name|beginOp
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|barrier
operator|.
name|beginOp
argument_list|()
argument_list|)
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|stopAndDrainOps
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|barrier
operator|.
name|beginOp
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUnmatchedEndAssert
parameter_list|()
throws|throws
name|Exception
block|{
name|DrainBarrier
name|barrier
init|=
operator|new
name|DrainBarrier
argument_list|()
decl_stmt|;
try|try
block|{
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|Error
argument_list|(
literal|"Should have asserted"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{     }
name|barrier
operator|.
name|beginOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|beginOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
try|try
block|{
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|Error
argument_list|(
literal|"Should have asserted"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStopWithoutOpsDoesntBlock
parameter_list|()
throws|throws
name|Exception
block|{
name|DrainBarrier
name|barrier
init|=
operator|new
name|DrainBarrier
argument_list|()
decl_stmt|;
name|barrier
operator|.
name|stopAndDrainOpsOnce
argument_list|()
expr_stmt|;
name|barrier
operator|=
operator|new
name|DrainBarrier
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|beginOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|stopAndDrainOpsOnce
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
comment|/** This test tests blocking and can have false positives in very bad timing cases. */
specifier|public
name|void
name|testStopIsBlockedByOps
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|DrainBarrier
name|barrier
init|=
operator|new
name|DrainBarrier
argument_list|()
decl_stmt|;
name|barrier
operator|.
name|beginOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|beginOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|beginOp
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|Thread
name|stoppingThread
init|=
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
try|try
block|{
name|barrier
operator|.
name|stopAndDrainOpsOnce
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Should not have happened"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|stoppingThread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// First "end" should not unblock the thread, but the second should.
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|stoppingThread
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|stoppingThread
operator|.
name|isAlive
argument_list|()
argument_list|)
expr_stmt|;
name|barrier
operator|.
name|endOp
argument_list|()
expr_stmt|;
name|stoppingThread
operator|.
name|join
argument_list|(
literal|30000
argument_list|)
expr_stmt|;
comment|// When not broken, will be a very fast wait; set safe value.
name|assertFalse
argument_list|(
name|stoppingThread
operator|.
name|isAlive
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleStopOnceAssert
parameter_list|()
throws|throws
name|Exception
block|{
name|DrainBarrier
name|barrier
init|=
operator|new
name|DrainBarrier
argument_list|()
decl_stmt|;
name|barrier
operator|.
name|stopAndDrainOpsOnce
argument_list|()
expr_stmt|;
try|try
block|{
name|barrier
operator|.
name|stopAndDrainOpsOnce
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|Error
argument_list|(
literal|"Should have asserted"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleSloppyStopsHaveNoEffect
parameter_list|()
throws|throws
name|Exception
block|{
name|DrainBarrier
name|barrier
init|=
operator|new
name|DrainBarrier
argument_list|()
decl_stmt|;
name|barrier
operator|.
name|stopAndDrainOps
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|stopAndDrainOps
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

