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
name|procedure2
package|;
end_package

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
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
name|HBaseCommonTestingUtility
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
name|procedure2
operator|.
name|ProcedureTestingUtility
operator|.
name|NoopProcedure
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
name|procedure2
operator|.
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|MasterTests
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
name|AfterClass
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_comment
comment|/**  * Testcase for HBASE-20973  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestProcedureRollbackAIOOB
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
name|TestProcedureRollbackAIOOB
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseCommonTestingUtility
name|UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|ParentProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|scheduled
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|Void
argument_list|>
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
if|if
condition|(
name|scheduled
condition|)
block|{
return|return
literal|null
return|;
block|}
name|scheduled
operator|=
literal|true
expr_stmt|;
return|return
operator|new
name|Procedure
index|[]
block|{
operator|new
name|SubProcedure
argument_list|()
block|}
return|;
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|SubProcedure
extends|extends
name|NoopProcedure
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
name|Void
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|ProcedureSuspendedException
throws|,
name|InterruptedException
block|{
name|setFailure
argument_list|(
literal|"Inject error"
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"Inject error"
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|WALProcedureStore
name|procStore
decl_stmt|;
specifier|private
name|ProcedureExecutor
argument_list|<
name|Void
argument_list|>
name|procExec
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|procStore
operator|=
name|ProcedureTestingUtility
operator|.
name|createWalStore
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getDataTestDir
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|procStore
operator|.
name|start
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|procExec
operator|=
operator|new
name|ProcedureExecutor
argument_list|<
name|Void
argument_list|>
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|,
name|procStore
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|initAndStartWorkers
argument_list|(
name|procExec
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|procExec
operator|.
name|stop
argument_list|()
expr_stmt|;
name|procStore
operator|.
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrayIndexOutOfBounds
parameter_list|()
block|{
name|ParentProcedure
name|proc
init|=
operator|new
name|ParentProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|long
name|noopProcId
init|=
operator|-
literal|1L
decl_stmt|;
comment|// make sure that the sub procedure will have a new BitSetNode
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|Long
operator|.
name|SIZE
operator|-
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|noopProcId
operator|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
operator|new
name|NoopProcedure
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|lastNoopProcId
init|=
name|noopProcId
decl_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|procExec
operator|.
name|isFinished
argument_list|(
name|lastNoopProcId
argument_list|)
argument_list|)
expr_stmt|;
name|proc
operator|.
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|10000
argument_list|,
parameter_list|()
lambda|->
name|procExec
operator|.
name|isFinished
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

