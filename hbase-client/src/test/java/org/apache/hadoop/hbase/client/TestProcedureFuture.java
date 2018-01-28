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
name|client
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
name|assertTrue
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
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|DoNotRetryIOException
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
name|ClientTests
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|GetProcedureResultRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|GetProcedureResultResponse
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
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
name|TestProcedureFuture
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
name|TestProcedureFuture
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
class|class
name|TestFuture
extends|extends
name|HBaseAdmin
operator|.
name|ProcedureFuture
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
name|boolean
name|postOperationResultCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|waitOperationResultCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|getProcedureResultCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|convertResultCalled
init|=
literal|false
decl_stmt|;
specifier|public
name|TestFuture
parameter_list|(
specifier|final
name|HBaseAdmin
name|admin
parameter_list|,
specifier|final
name|Long
name|procId
parameter_list|)
block|{
name|super
argument_list|(
name|admin
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasPostOperationResultCalled
parameter_list|()
block|{
return|return
name|postOperationResultCalled
return|;
block|}
specifier|public
name|boolean
name|wasWaitOperationResultCalled
parameter_list|()
block|{
return|return
name|waitOperationResultCalled
return|;
block|}
specifier|public
name|boolean
name|wasGetProcedureResultCalled
parameter_list|()
block|{
return|return
name|getProcedureResultCalled
return|;
block|}
specifier|public
name|boolean
name|wasConvertResultCalled
parameter_list|()
block|{
return|return
name|convertResultCalled
return|;
block|}
annotation|@
name|Override
specifier|protected
name|GetProcedureResultResponse
name|getProcedureResult
parameter_list|(
specifier|final
name|GetProcedureResultRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|getProcedureResultCalled
operator|=
literal|true
expr_stmt|;
return|return
name|GetProcedureResultResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setState
argument_list|(
name|GetProcedureResultResponse
operator|.
name|State
operator|.
name|FINISHED
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Void
name|convertResult
parameter_list|(
specifier|final
name|GetProcedureResultResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|convertResultCalled
operator|=
literal|true
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Void
name|waitOperationResult
parameter_list|(
specifier|final
name|long
name|deadlineTs
parameter_list|)
throws|throws
name|IOException
throws|,
name|TimeoutException
block|{
name|waitOperationResultCalled
operator|=
literal|true
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Void
name|postOperationResult
parameter_list|(
specifier|final
name|Void
name|result
parameter_list|,
specifier|final
name|long
name|deadlineTs
parameter_list|)
throws|throws
name|IOException
throws|,
name|TimeoutException
block|{
name|postOperationResultCalled
operator|=
literal|true
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
comment|/**    * When a master return a result with procId,    * we are skipping the waitOperationResult() call,    * since we are getting the procedure result.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWithProcId
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseAdmin
operator|.
name|class
argument_list|)
decl_stmt|;
name|TestFuture
name|f
init|=
operator|new
name|TestFuture
argument_list|(
name|admin
argument_list|,
literal|100L
argument_list|)
decl_stmt|;
name|f
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected getProcedureResult() to be called"
argument_list|,
name|f
operator|.
name|wasGetProcedureResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected convertResult() to be called"
argument_list|,
name|f
operator|.
name|wasConvertResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"unexpected waitOperationResult() called"
argument_list|,
name|f
operator|.
name|wasWaitOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected postOperationResult() to be called"
argument_list|,
name|f
operator|.
name|wasPostOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that the spin loop for the procedure running works.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWithProcIdAndSpinning
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|AtomicInteger
name|spinCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseAdmin
operator|.
name|class
argument_list|)
decl_stmt|;
name|TestFuture
name|f
init|=
operator|new
name|TestFuture
argument_list|(
name|admin
argument_list|,
literal|100L
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|GetProcedureResultResponse
name|getProcedureResult
parameter_list|(
specifier|final
name|GetProcedureResultRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|done
init|=
name|spinCount
operator|.
name|incrementAndGet
argument_list|()
operator|>=
literal|10
decl_stmt|;
return|return
name|GetProcedureResultResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setState
argument_list|(
name|done
condition|?
name|GetProcedureResultResponse
operator|.
name|State
operator|.
name|FINISHED
else|:
name|GetProcedureResultResponse
operator|.
name|State
operator|.
name|RUNNING
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|f
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|spinCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected convertResult() to be called"
argument_list|,
name|f
operator|.
name|wasConvertResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"unexpected waitOperationResult() called"
argument_list|,
name|f
operator|.
name|wasWaitOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected postOperationResult() to be called"
argument_list|,
name|f
operator|.
name|wasPostOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * When a master return a result without procId,    * we are skipping the getProcedureResult() call.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWithoutProcId
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseAdmin
operator|.
name|class
argument_list|)
decl_stmt|;
name|TestFuture
name|f
init|=
operator|new
name|TestFuture
argument_list|(
name|admin
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|f
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"unexpected getProcedureResult() called"
argument_list|,
name|f
operator|.
name|wasGetProcedureResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"unexpected convertResult() called"
argument_list|,
name|f
operator|.
name|wasConvertResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected waitOperationResult() to be called"
argument_list|,
name|f
operator|.
name|wasWaitOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected postOperationResult() to be called"
argument_list|,
name|f
operator|.
name|wasPostOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * When a new client with procedure support tries to ask an old-master without proc-support    * the procedure result we get a DoNotRetryIOException (which is an UnsupportedOperationException)    * The future should trap that and fallback to the waitOperationResult().    *    * This happens when the operation calls happens on a "new master" but while we are waiting    * the operation to be completed, we failover on an "old master".    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testOnServerWithNoProcedureSupport
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseAdmin
operator|.
name|class
argument_list|)
decl_stmt|;
name|TestFuture
name|f
init|=
operator|new
name|TestFuture
argument_list|(
name|admin
argument_list|,
literal|100L
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|GetProcedureResultResponse
name|getProcedureResult
parameter_list|(
specifier|final
name|GetProcedureResultRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|getProcedureResult
argument_list|(
name|request
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"getProcedureResult"
argument_list|)
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
name|f
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected getProcedureResult() to be called"
argument_list|,
name|f
operator|.
name|wasGetProcedureResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"unexpected convertResult() called"
argument_list|,
name|f
operator|.
name|wasConvertResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected waitOperationResult() to be called"
argument_list|,
name|f
operator|.
name|wasWaitOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected postOperationResult() to be called"
argument_list|,
name|f
operator|.
name|wasPostOperationResultCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

