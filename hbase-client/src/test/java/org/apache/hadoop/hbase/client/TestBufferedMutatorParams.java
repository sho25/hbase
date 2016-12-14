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
name|util
operator|.
name|Collection
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
name|concurrent
operator|.
name|Callable
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
name|ExecutionException
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
name|ExecutorService
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
name|Future
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
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
name|TestBufferedMutatorParams
block|{
comment|/**    * Just to create in instance, this doesn't actually function.    */
specifier|private
class|class
name|MockExecutorService
implements|implements
name|ExecutorService
block|{
specifier|public
name|void
name|execute
parameter_list|(
name|Runnable
name|command
parameter_list|)
block|{     }
specifier|public
name|void
name|shutdown
parameter_list|()
block|{     }
specifier|public
name|List
argument_list|<
name|Runnable
argument_list|>
name|shutdownNow
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|isShutdown
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|isTerminated
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|awaitTermination
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
literal|false
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Future
argument_list|<
name|T
argument_list|>
name|submit
parameter_list|(
name|Callable
argument_list|<
name|T
argument_list|>
name|task
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Future
argument_list|<
name|T
argument_list|>
name|submit
parameter_list|(
name|Runnable
name|task
parameter_list|,
name|T
name|result
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|Future
argument_list|<
name|?
argument_list|>
name|submit
parameter_list|(
name|Runnable
name|task
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|Future
argument_list|<
name|T
argument_list|>
argument_list|>
name|invokeAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Callable
argument_list|<
name|T
argument_list|>
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
literal|null
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|Future
argument_list|<
name|T
argument_list|>
argument_list|>
name|invokeAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Callable
argument_list|<
name|T
argument_list|>
argument_list|>
name|tasks
parameter_list|,
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
literal|null
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|invokeAny
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Callable
argument_list|<
name|T
argument_list|>
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
return|return
literal|null
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|invokeAny
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Callable
argument_list|<
name|T
argument_list|>
argument_list|>
name|tasks
parameter_list|,
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Just to create an instance, this doesn't actually function.    */
specifier|private
class|class
name|MockExceptionListener
implements|implements
name|BufferedMutator
operator|.
name|ExceptionListener
block|{
specifier|public
name|void
name|onException
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|exception
parameter_list|,
name|BufferedMutator
name|mutator
parameter_list|)
throws|throws
name|RetriesExhaustedWithDetailsException
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClone
parameter_list|()
block|{
name|ExecutorService
name|pool
init|=
operator|new
name|MockExecutorService
argument_list|()
decl_stmt|;
name|BufferedMutatorParams
name|bmp
init|=
operator|new
name|BufferedMutatorParams
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"SomeTableName"
argument_list|)
argument_list|)
decl_stmt|;
name|BufferedMutator
operator|.
name|ExceptionListener
name|listener
init|=
operator|new
name|MockExceptionListener
argument_list|()
decl_stmt|;
name|bmp
operator|.
name|writeBufferSize
argument_list|(
literal|17
argument_list|)
operator|.
name|maxKeyValueSize
argument_list|(
literal|13
argument_list|)
operator|.
name|pool
argument_list|(
name|pool
argument_list|)
operator|.
name|listener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|BufferedMutatorParams
name|clone
init|=
name|bmp
operator|.
name|clone
argument_list|()
decl_stmt|;
comment|// Confirm some literals
name|assertEquals
argument_list|(
literal|"SomeTableName"
argument_list|,
name|clone
operator|.
name|getTableName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|17
argument_list|,
name|clone
operator|.
name|getWriteBufferSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|13
argument_list|,
name|clone
operator|.
name|getMaxKeyValueSize
argument_list|()
argument_list|)
expr_stmt|;
name|cloneTest
argument_list|(
name|bmp
argument_list|,
name|clone
argument_list|)
expr_stmt|;
name|BufferedMutatorParams
name|cloneWars
init|=
name|clone
operator|.
name|clone
argument_list|()
decl_stmt|;
name|cloneTest
argument_list|(
name|clone
argument_list|,
name|cloneWars
argument_list|)
expr_stmt|;
name|cloneTest
argument_list|(
name|bmp
argument_list|,
name|cloneWars
argument_list|)
expr_stmt|;
block|}
comment|/**    * Confirm all fields are equal.    * @param some some instance    * @param clone a clone of that instance, but not the same instance.    */
specifier|private
name|void
name|cloneTest
parameter_list|(
name|BufferedMutatorParams
name|some
parameter_list|,
name|BufferedMutatorParams
name|clone
parameter_list|)
block|{
name|assertFalse
argument_list|(
name|some
operator|==
name|clone
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|some
operator|.
name|getTableName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|clone
operator|.
name|getTableName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|some
operator|.
name|getWriteBufferSize
argument_list|()
argument_list|,
name|clone
operator|.
name|getWriteBufferSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|some
operator|.
name|getMaxKeyValueSize
argument_list|()
argument_list|,
name|clone
operator|.
name|getMaxKeyValueSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|some
operator|.
name|getListener
argument_list|()
operator|==
name|clone
operator|.
name|getListener
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|some
operator|.
name|getPool
argument_list|()
operator|==
name|clone
operator|.
name|getPool
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

