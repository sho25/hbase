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
name|errorhandling
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Test that we propagate errors through an dispatcher exactly once via different failure  * injection mechanisms.  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestForeignExceptionDispatcher
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestForeignExceptionDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Exception thrown from the test    */
specifier|final
name|ForeignException
name|EXTEXN
init|=
operator|new
name|ForeignException
argument_list|(
literal|"FORTEST"
argument_list|,
operator|new
name|IllegalArgumentException
argument_list|(
literal|"FORTEST"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|ForeignException
name|EXTEXN2
init|=
operator|new
name|ForeignException
argument_list|(
literal|"FORTEST2"
argument_list|,
operator|new
name|IllegalArgumentException
argument_list|(
literal|"FORTEST2"
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * Tests that a dispatcher only dispatches only the first exception, and does not propagate    * subsequent exceptions.    */
annotation|@
name|Test
specifier|public
name|void
name|testErrorPropagation
parameter_list|()
block|{
name|ForeignExceptionListener
name|listener1
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ForeignExceptionListener
name|listener2
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ForeignExceptionDispatcher
name|dispatcher
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
comment|// add the listeners
name|dispatcher
operator|.
name|addListener
argument_list|(
name|listener1
argument_list|)
expr_stmt|;
name|dispatcher
operator|.
name|addListener
argument_list|(
name|listener2
argument_list|)
expr_stmt|;
comment|// create an artificial error
name|dispatcher
operator|.
name|receive
argument_list|(
name|EXTEXN
argument_list|)
expr_stmt|;
comment|// make sure the listeners got the error
name|Mockito
operator|.
name|verify
argument_list|(
name|listener1
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|receive
argument_list|(
name|EXTEXN
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|listener2
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|receive
argument_list|(
name|EXTEXN
argument_list|)
expr_stmt|;
comment|// make sure that we get an exception
try|try
block|{
name|dispatcher
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Monitor should have thrown an exception after getting error."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ForeignException
name|ex
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Got an unexpected exception:"
operator|+
name|ex
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|==
name|EXTEXN
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got the testing exception!"
argument_list|)
expr_stmt|;
block|}
comment|// push another error, which should be not be passed to listeners
name|dispatcher
operator|.
name|receive
argument_list|(
name|EXTEXN2
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|listener1
argument_list|,
name|Mockito
operator|.
name|never
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|EXTEXN2
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|listener2
argument_list|,
name|Mockito
operator|.
name|never
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|EXTEXN2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleDispatcherWithTimer
parameter_list|()
block|{
name|ForeignExceptionListener
name|listener1
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ForeignExceptionListener
name|listener2
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ForeignExceptionDispatcher
name|monitor
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
comment|// add the listeners
name|monitor
operator|.
name|addListener
argument_list|(
name|listener1
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|addListener
argument_list|(
name|listener2
argument_list|)
expr_stmt|;
name|TimeoutExceptionInjector
name|timer
init|=
operator|new
name|TimeoutExceptionInjector
argument_list|(
name|monitor
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|timer
operator|.
name|start
argument_list|()
expr_stmt|;
name|timer
operator|.
name|trigger
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Monitor didn't get timeout"
argument_list|,
name|monitor
operator|.
name|hasException
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify that that we propagated the error
name|Mockito
operator|.
name|verify
argument_list|(
name|listener1
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|listener2
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the dispatcher can receive an error via the timer mechanism.    */
annotation|@
name|Test
specifier|public
name|void
name|testAttemptTimer
parameter_list|()
block|{
name|ForeignExceptionListener
name|listener1
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ForeignExceptionListener
name|listener2
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ForeignExceptionDispatcher
name|orchestrator
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
comment|// add the listeners
name|orchestrator
operator|.
name|addListener
argument_list|(
name|listener1
argument_list|)
expr_stmt|;
name|orchestrator
operator|.
name|addListener
argument_list|(
name|listener2
argument_list|)
expr_stmt|;
comment|// now create a timer and check for that error
name|TimeoutExceptionInjector
name|timer
init|=
operator|new
name|TimeoutExceptionInjector
argument_list|(
name|orchestrator
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|timer
operator|.
name|start
argument_list|()
expr_stmt|;
name|timer
operator|.
name|trigger
argument_list|()
expr_stmt|;
comment|// make sure that we got the timer error
name|Mockito
operator|.
name|verify
argument_list|(
name|listener1
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|listener2
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

