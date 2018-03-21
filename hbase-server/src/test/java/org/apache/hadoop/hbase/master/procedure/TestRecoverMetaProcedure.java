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
name|master
operator|.
name|procedure
package|;
end_package

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
name|HBaseTestingUtility
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
name|master
operator|.
name|MasterServices
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
name|master
operator|.
name|assignment
operator|.
name|MockMasterServices
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
name|ProcedureSuspendedException
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
name|ProcedureYieldException
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
name|StateMachineProcedure
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
name|MasterProcedureProtos
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|TestRecoverMetaProcedure
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRecoverMetaProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestRecoverMetaProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Test the new prepare step.    * Here we test that our Mock is faking out the precedure well-enough for it to progress past the    * first prepare stage.    */
annotation|@
name|Test
specifier|public
name|void
name|testPrepare
parameter_list|()
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
throws|,
name|IOException
block|{
name|RecoverMetaProcedure
name|rmp
init|=
operator|new
name|RecoverMetaProcedure
argument_list|()
decl_stmt|;
name|MasterProcedureEnv
name|env
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterProcedureEnv
operator|.
name|class
argument_list|)
decl_stmt|;
name|MasterServices
name|masterServices
init|=
operator|new
name|MockMasterServices
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|StateMachineProcedure
operator|.
name|Flow
operator|.
name|HAS_MORE_STATE
argument_list|,
name|rmp
operator|.
name|executeFromState
argument_list|(
name|env
argument_list|,
name|rmp
operator|.
name|getInitialState
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|stateId
init|=
name|rmp
operator|.
name|getCurrentStateId
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
operator|.
name|RECOVER_META_SPLIT_LOGS_VALUE
argument_list|,
name|rmp
operator|.
name|getCurrentStateId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the new prepare step.    * If Master is stopping, procedure should skip the assign by returning NO_MORE_STATE    */
annotation|@
name|Test
specifier|public
name|void
name|testPrepareWithMasterStopping
parameter_list|()
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
throws|,
name|IOException
block|{
name|RecoverMetaProcedure
name|rmp
init|=
operator|new
name|RecoverMetaProcedure
argument_list|()
decl_stmt|;
name|MasterProcedureEnv
name|env
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterProcedureEnv
operator|.
name|class
argument_list|)
decl_stmt|;
name|MasterServices
name|masterServices
init|=
operator|new
name|MockMasterServices
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|StateMachineProcedure
operator|.
name|Flow
operator|.
name|NO_MORE_STATE
argument_list|,
name|rmp
operator|.
name|executeFromState
argument_list|(
name|env
argument_list|,
name|rmp
operator|.
name|getInitialState
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the new prepare step.    * If cluster is down, procedure should skip the assign by returning NO_MORE_STATE    */
annotation|@
name|Test
specifier|public
name|void
name|testPrepareWithNoCluster
parameter_list|()
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
throws|,
name|IOException
block|{
name|RecoverMetaProcedure
name|rmp
init|=
operator|new
name|RecoverMetaProcedure
argument_list|()
decl_stmt|;
name|MasterProcedureEnv
name|env
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterProcedureEnv
operator|.
name|class
argument_list|)
decl_stmt|;
name|MasterServices
name|masterServices
init|=
operator|new
name|MockMasterServices
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isClusterUp
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|StateMachineProcedure
operator|.
name|Flow
operator|.
name|NO_MORE_STATE
argument_list|,
name|rmp
operator|.
name|executeFromState
argument_list|(
name|env
argument_list|,
name|rmp
operator|.
name|getInitialState
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
