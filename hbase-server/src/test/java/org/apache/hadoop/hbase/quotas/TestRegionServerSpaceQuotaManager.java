begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doNothing
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doThrow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
operator|.
name|SpaceQuotaStatus
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
name|quotas
operator|.
name|policies
operator|.
name|DisableTableViolationPolicyEnforcement
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
name|quotas
operator|.
name|policies
operator|.
name|NoInsertsViolationPolicyEnforcement
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
name|quotas
operator|.
name|policies
operator|.
name|NoWritesCompactionsViolationPolicyEnforcement
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
name|quotas
operator|.
name|policies
operator|.
name|NoWritesViolationPolicyEnforcement
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
name|quotas
operator|.
name|policies
operator|.
name|DefaultViolationPolicyEnforcement
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
name|regionserver
operator|.
name|RegionServerServices
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
name|Before
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

begin_comment
comment|/**  * Test class for {@link RegionServerSpaceQuotaManager}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionServerSpaceQuotaManager
block|{
specifier|private
name|RegionServerSpaceQuotaManager
name|quotaManager
decl_stmt|;
specifier|private
name|RegionServerServices
name|rss
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|quotaManager
operator|=
name|mock
argument_list|(
name|RegionServerSpaceQuotaManager
operator|.
name|class
argument_list|)
expr_stmt|;
name|rss
operator|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSpacePoliciesFromEnforcements
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|enforcements
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|expectedPolicies
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|quotaManager
operator|.
name|copyActiveEnforcements
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|enforcements
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|quotaManager
operator|.
name|getActivePoliciesAsMap
argument_list|()
argument_list|)
operator|.
name|thenCallRealMethod
argument_list|()
expr_stmt|;
name|NoInsertsViolationPolicyEnforcement
name|noInsertsPolicy
init|=
operator|new
name|NoInsertsViolationPolicyEnforcement
argument_list|()
decl_stmt|;
name|SpaceQuotaSnapshot
name|noInsertsSnapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
argument_list|)
argument_list|,
literal|256L
argument_list|,
literal|1024L
argument_list|)
decl_stmt|;
name|noInsertsPolicy
operator|.
name|initialize
argument_list|(
name|rss
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"no_inserts"
argument_list|)
argument_list|,
name|noInsertsSnapshot
argument_list|)
expr_stmt|;
name|enforcements
operator|.
name|put
argument_list|(
name|noInsertsPolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|noInsertsPolicy
argument_list|)
expr_stmt|;
name|expectedPolicies
operator|.
name|put
argument_list|(
name|noInsertsPolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|noInsertsSnapshot
argument_list|)
expr_stmt|;
name|NoWritesViolationPolicyEnforcement
name|noWritesPolicy
init|=
operator|new
name|NoWritesViolationPolicyEnforcement
argument_list|()
decl_stmt|;
name|SpaceQuotaSnapshot
name|noWritesSnapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES
argument_list|)
argument_list|,
literal|512L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
name|noWritesPolicy
operator|.
name|initialize
argument_list|(
name|rss
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"no_writes"
argument_list|)
argument_list|,
name|noWritesSnapshot
argument_list|)
expr_stmt|;
name|enforcements
operator|.
name|put
argument_list|(
name|noWritesPolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|noWritesPolicy
argument_list|)
expr_stmt|;
name|expectedPolicies
operator|.
name|put
argument_list|(
name|noWritesPolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|noWritesSnapshot
argument_list|)
expr_stmt|;
name|NoWritesCompactionsViolationPolicyEnforcement
name|noWritesCompactionsPolicy
init|=
operator|new
name|NoWritesCompactionsViolationPolicyEnforcement
argument_list|()
decl_stmt|;
name|SpaceQuotaSnapshot
name|noWritesCompactionsSnapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES_COMPACTIONS
argument_list|)
argument_list|,
literal|1024L
argument_list|,
literal|4096L
argument_list|)
decl_stmt|;
name|noWritesCompactionsPolicy
operator|.
name|initialize
argument_list|(
name|rss
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"no_writes_compactions"
argument_list|)
argument_list|,
name|noWritesCompactionsSnapshot
argument_list|)
expr_stmt|;
name|enforcements
operator|.
name|put
argument_list|(
name|noWritesCompactionsPolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|noWritesCompactionsPolicy
argument_list|)
expr_stmt|;
name|expectedPolicies
operator|.
name|put
argument_list|(
name|noWritesCompactionsPolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|noWritesCompactionsSnapshot
argument_list|)
expr_stmt|;
name|DisableTableViolationPolicyEnforcement
name|disablePolicy
init|=
operator|new
name|DisableTableViolationPolicyEnforcement
argument_list|()
decl_stmt|;
name|SpaceQuotaSnapshot
name|disableSnapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
argument_list|,
literal|2048L
argument_list|,
literal|8192L
argument_list|)
decl_stmt|;
name|disablePolicy
operator|.
name|initialize
argument_list|(
name|rss
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"disable"
argument_list|)
argument_list|,
name|disableSnapshot
argument_list|)
expr_stmt|;
name|enforcements
operator|.
name|put
argument_list|(
name|disablePolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|disablePolicy
argument_list|)
expr_stmt|;
name|expectedPolicies
operator|.
name|put
argument_list|(
name|disablePolicy
operator|.
name|getTableName
argument_list|()
argument_list|,
name|disableSnapshot
argument_list|)
expr_stmt|;
name|enforcements
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"no_policy"
argument_list|)
argument_list|,
operator|new
name|DefaultViolationPolicyEnforcement
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|actualPolicies
init|=
name|quotaManager
operator|.
name|getActivePoliciesAsMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedPolicies
argument_list|,
name|actualPolicies
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExceptionOnPolicyEnforcementEnable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|final
name|SpaceQuotaSnapshot
name|snapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
argument_list|,
literal|1024L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
name|RegionServerServices
name|rss
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcementFactory
name|factory
init|=
name|mock
argument_list|(
name|SpaceViolationPolicyEnforcementFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|enforcement
init|=
name|mock
argument_list|(
name|SpaceViolationPolicyEnforcement
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionServerSpaceQuotaManager
name|realManager
init|=
operator|new
name|RegionServerSpaceQuotaManager
argument_list|(
name|rss
argument_list|,
name|factory
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|factory
operator|.
name|create
argument_list|(
name|rss
argument_list|,
name|tableName
argument_list|,
name|snapshot
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|enforcement
argument_list|)
expr_stmt|;
name|doThrow
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Failed for test!"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|enforcement
argument_list|)
operator|.
name|enable
argument_list|()
expr_stmt|;
name|realManager
operator|.
name|enforceViolationPolicy
argument_list|(
name|tableName
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|enforcements
init|=
name|realManager
operator|.
name|copyActiveEnforcements
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected active enforcements to be empty, but were "
operator|+
name|enforcements
argument_list|,
name|enforcements
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExceptionOnPolicyEnforcementDisable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|final
name|SpaceQuotaSnapshot
name|snapshot
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
argument_list|,
literal|1024L
argument_list|,
literal|2048L
argument_list|)
decl_stmt|;
name|RegionServerServices
name|rss
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcementFactory
name|factory
init|=
name|mock
argument_list|(
name|SpaceViolationPolicyEnforcementFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|enforcement
init|=
name|mock
argument_list|(
name|SpaceViolationPolicyEnforcement
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionServerSpaceQuotaManager
name|realManager
init|=
operator|new
name|RegionServerSpaceQuotaManager
argument_list|(
name|rss
argument_list|,
name|factory
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|factory
operator|.
name|create
argument_list|(
name|rss
argument_list|,
name|tableName
argument_list|,
name|snapshot
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|enforcement
argument_list|)
expr_stmt|;
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|enforcement
argument_list|)
operator|.
name|enable
argument_list|()
expr_stmt|;
name|doThrow
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Failed for test!"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|enforcement
argument_list|)
operator|.
name|disable
argument_list|()
expr_stmt|;
comment|// Enabling should work
name|realManager
operator|.
name|enforceViolationPolicy
argument_list|(
name|tableName
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|enforcements
init|=
name|realManager
operator|.
name|copyActiveEnforcements
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|enforcements
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// If the disable fails, we should still treat it as "active"
name|realManager
operator|.
name|disableViolationPolicyEnforcement
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|enforcements
operator|=
name|realManager
operator|.
name|copyActiveEnforcements
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|enforcements
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

