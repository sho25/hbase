begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assignment
package|;
end_package

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
name|Optional
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Mutation
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
name|client
operator|.
name|RegionInfo
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|MasterCoprocessor
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|MasterObserver
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
name|coprocessor
operator|.
name|ObserverContext
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|TestMasterAbortWhileMergingTable
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
name|TestMasterAbortWhileMergingTable
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestMasterAbortWhileMergingTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|SPLITKEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbbbbbb"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|mergeCommitArrive
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MergeRegionObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|admin
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[
literal|1
index|]
index|[]
decl_stmt|;
name|splitKeys
index|[
literal|0
index|]
operator|=
name|SPLITKEY
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|CF
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|MergeTableRegionsProcedure
name|mergeTableRegionsProcedure
init|=
operator|new
name|MergeTableRegionsProcedure
argument_list|(
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|,
operator|new
name|RegionInfo
index|[]
block|{
name|regionInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
block|,
name|regionInfos
operator|.
name|get
argument_list|(
literal|1
argument_list|)
block|}
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|long
name|procID
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|submitProcedure
argument_list|(
name|mergeTableRegionsProcedure
argument_list|)
decl_stmt|;
name|mergeCommitArrive
operator|.
name|await
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|stopMaster
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|startMaster
argument_list|()
expr_stmt|;
comment|//wait until master initialized
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|!=
literal|null
operator|&&
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|isInitialized
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|isFinished
argument_list|(
name|procID
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Found region RIT, that's impossible! "
operator|+
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionsInTransition
argument_list|()
argument_list|,
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionsInTransition
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MergeRegionObserver
implements|implements
name|MasterCoprocessor
implements|,
name|MasterObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|MasterObserver
argument_list|>
name|getMasterObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preMergeRegionsCommitAction
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|RegionInfo
index|[]
name|regionsToMerge
parameter_list|,
name|List
argument_list|<
name|Mutation
argument_list|>
name|metaEntries
parameter_list|)
block|{
name|mergeCommitArrive
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"mergeCommitArrive countdown"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

