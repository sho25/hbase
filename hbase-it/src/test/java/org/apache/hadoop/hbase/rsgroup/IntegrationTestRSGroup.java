begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rsgroup
package|;
end_package

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
name|IntegrationTestingUtility
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
name|Waiter
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
name|IntegrationTests
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Runs all of the units tests defined in TestGroupBase  * as an integration test.  * Requires TestRSGroupBase.NUM_SLAVE_BASE servers to run.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestRSGroup
extends|extends
name|TestRSGroupsBase
block|{
comment|//Integration specific
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IntegrationTestRSGroup
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|beforeMethod
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting up IntegrationTestGroup"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing cluster with "
operator|+
name|NUM_SLAVES_BASE
operator|+
literal|" servers"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|()
expr_stmt|;
operator|(
operator|(
name|IntegrationTestingUtility
operator|)
name|TEST_UTIL
operator|)
operator|.
name|initializeCluster
argument_list|(
name|NUM_SLAVES_BASE
argument_list|)
expr_stmt|;
comment|//set shared configs
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseClusterInterface
argument_list|()
expr_stmt|;
name|rsGroupAdmin
operator|=
operator|new
name|VerifyingRSGroupAdminClient
argument_list|(
name|rsGroupAdmin
operator|.
name|newClient
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|)
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done initializing cluster"
argument_list|)
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
comment|//cluster may not be clean
comment|//cleanup when initializing
name|afterMethod
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|afterMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaning up previous test run"
argument_list|)
expr_stmt|;
comment|//cleanup previous artifacts
name|deleteTableIfNecessary
argument_list|()
expr_stmt|;
name|deleteNamespaceIfNecessary
argument_list|()
expr_stmt|;
name|deleteGroups
argument_list|()
expr_stmt|;
name|admin
operator|.
name|setBalancerRunning
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring the cluster"
argument_list|)
expr_stmt|;
operator|(
operator|(
name|IntegrationTestingUtility
operator|)
name|TEST_UTIL
operator|)
operator|.
name|restoreCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done restoring the cluster"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for cleanup to finish "
operator|+
name|rsGroupAdmin
operator|.
name|listRSGroups
argument_list|()
argument_list|)
expr_stmt|;
comment|//Might be greater since moving servers back to default
comment|//is after starting a server
return|return
name|rsGroupAdmin
operator|.
name|getRSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|)
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|NUM_SLAVES_BASE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
name|WAIT_TIMEOUT
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for regionservers to be registered "
operator|+
name|rsGroupAdmin
operator|.
name|listRSGroups
argument_list|()
argument_list|)
expr_stmt|;
comment|//Might be greater since moving servers back to default
comment|//is after starting a server
return|return
name|rsGroupAdmin
operator|.
name|getRSGroupInfo
argument_list|(
name|RSGroupInfo
operator|.
name|DEFAULT_GROUP
argument_list|)
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|getNumServers
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done cleaning up previous test run"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

