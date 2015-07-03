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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|ServerCrashState
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
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureState
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
name|TestProcedureToString
block|{
comment|/**    * A do-nothing environment for BasicProcedure.    */
specifier|static
class|class
name|BasicProcedureEnv
block|{}
empty_stmt|;
comment|/**    * A do-nothing basic procedure just for testing toString.    */
specifier|static
class|class
name|BasicProcedure
extends|extends
name|Procedure
argument_list|<
name|BasicProcedureEnv
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Procedure
argument_list|<
name|?
argument_list|>
index|[]
name|execute
parameter_list|(
name|BasicProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|Procedure
index|[]
block|{
name|this
block|}
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
name|BasicProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{     }
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|BasicProcedureEnv
name|env
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeStateData
parameter_list|(
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|protected
name|void
name|deserializeStateData
parameter_list|(
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
comment|/**    * A do-nothing basic procedure that overrides the toStringState method. It just doubles the    * current state string.    */
specifier|static
class|class
name|DoublingStateStringBasicProcedure
extends|extends
name|BasicProcedure
block|{
annotation|@
name|Override
specifier|protected
name|void
name|toStringState
parameter_list|(
name|StringBuilder
name|builder
parameter_list|)
block|{
comment|// Call twice to get the state string twice as our state value.
name|super
operator|.
name|toStringState
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|super
operator|.
name|toStringState
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test that I can override the toString for its state value.    * @throws ProcedureYieldException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testBasicToString
parameter_list|()
throws|throws
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
name|BasicProcedure
name|p
init|=
operator|new
name|BasicProcedure
argument_list|()
decl_stmt|;
name|ProcedureState
name|state
init|=
name|ProcedureState
operator|.
name|RUNNABLE
decl_stmt|;
name|p
operator|.
name|setState
argument_list|(
name|state
argument_list|)
expr_stmt|;
comment|// Just assert that the toString basically works and has state in it.
name|assertTrue
argument_list|(
name|p
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|DoublingStateStringBasicProcedure
argument_list|()
expr_stmt|;
name|p
operator|.
name|setState
argument_list|(
name|state
argument_list|)
expr_stmt|;
comment|// Assert our override works and that we get double the state...
name|String
name|testStr
init|=
name|state
operator|.
name|toString
argument_list|()
operator|+
name|state
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|p
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|testStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Do-nothing SimpleMachineProcedure for checking its toString.    */
specifier|static
class|class
name|SimpleStateMachineProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|BasicProcedureEnv
argument_list|,
name|ServerCrashState
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
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
name|StateMachineProcedure
operator|.
name|Flow
name|executeFromState
parameter_list|(
name|BasicProcedureEnv
name|env
parameter_list|,
name|ServerCrashState
name|state
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|BasicProcedureEnv
name|env
parameter_list|,
name|ServerCrashState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{     }
annotation|@
name|Override
specifier|protected
name|ServerCrashState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|ServerCrashState
operator|.
name|valueOf
argument_list|(
name|stateId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
name|ServerCrashState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|getNumber
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|ServerCrashState
name|getInitialState
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
name|BasicProcedureEnv
name|env
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStateMachineProcedure
parameter_list|()
block|{
name|SimpleStateMachineProcedure
name|p
init|=
operator|new
name|SimpleStateMachineProcedure
argument_list|()
decl_stmt|;
name|ProcedureState
name|state
init|=
name|ProcedureState
operator|.
name|RUNNABLE
decl_stmt|;
name|p
operator|.
name|setState
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|p
operator|.
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_ASSIGN
argument_list|)
expr_stmt|;
comment|// Just assert that the toString basically works and has state in it.
name|assertTrue
argument_list|(
name|p
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|p
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_ASSIGN
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

