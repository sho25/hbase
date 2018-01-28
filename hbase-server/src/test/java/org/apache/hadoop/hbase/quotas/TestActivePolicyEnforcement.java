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
name|assertNotNull
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
name|mock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|quotas
operator|.
name|policies
operator|.
name|MissingSnapshotViolationPolicyEnforcement
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

begin_comment
comment|/**  * Test class for {@link ActivePolicyEnforcement}.  */
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
name|TestActivePolicyEnforcement
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
name|TestActivePolicyEnforcement
operator|.
name|class
argument_list|)
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
block|{
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
name|testGetter
parameter_list|()
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
operator|new
name|NoWritesViolationPolicyEnforcement
argument_list|()
argument_list|)
expr_stmt|;
name|ActivePolicyEnforcement
name|ape
init|=
operator|new
name|ActivePolicyEnforcement
argument_list|(
name|map
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|map
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoPolicyReturnsNoopEnforcement
parameter_list|()
block|{
name|ActivePolicyEnforcement
name|ape
init|=
operator|new
name|ActivePolicyEnforcement
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|enforcement
init|=
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"nonexistent"
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|enforcement
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected an instance of MissingSnapshotViolationPolicyEnforcement, but got "
operator|+
name|enforcement
operator|.
name|getClass
argument_list|()
argument_list|,
name|enforcement
operator|instanceof
name|MissingSnapshotViolationPolicyEnforcement
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoBulkLoadChecksOnNoSnapshot
parameter_list|()
block|{
name|ActivePolicyEnforcement
name|ape
init|=
operator|new
name|ActivePolicyEnforcement
argument_list|(
operator|new
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
argument_list|()
argument_list|,
name|Collections
operator|.
expr|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
operator|>
name|emptyMap
argument_list|()
argument_list|,
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|enforcement
init|=
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"nonexistent"
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Should not check bulkloads"
argument_list|,
name|enforcement
operator|.
name|shouldCheckBulkLoads
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoQuotaReturnsSingletonPolicyEnforcement
parameter_list|()
block|{
specifier|final
name|ActivePolicyEnforcement
name|ape
init|=
operator|new
name|ActivePolicyEnforcement
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|rss
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"my_table"
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|policyEnforcement
init|=
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// This should be the same exact instance, the singleton
name|assertTrue
argument_list|(
name|policyEnforcement
operator|==
name|MissingSnapshotViolationPolicyEnforcement
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ape
operator|.
name|getLocallyCachedPolicies
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Entry
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|entry
init|=
name|ape
operator|.
name|getLocallyCachedPolicies
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|policyEnforcement
operator|==
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNonViolatingQuotaCachesPolicyEnforcment
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|snapshots
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"my_table"
argument_list|)
decl_stmt|;
name|snapshots
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|SpaceQuotaStatus
operator|.
name|notInViolation
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ActivePolicyEnforcement
name|ape
init|=
operator|new
name|ActivePolicyEnforcement
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|snapshots
argument_list|,
name|rss
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|policyEnforcement
init|=
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Found the wrong class: "
operator|+
name|policyEnforcement
operator|.
name|getClass
argument_list|()
argument_list|,
name|policyEnforcement
operator|instanceof
name|DefaultViolationPolicyEnforcement
argument_list|)
expr_stmt|;
name|SpaceViolationPolicyEnforcement
name|copy
init|=
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected the instance to be cached"
argument_list|,
name|policyEnforcement
operator|==
name|copy
argument_list|)
expr_stmt|;
name|Entry
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|entry
init|=
name|ape
operator|.
name|getLocallyCachedPolicies
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|policyEnforcement
operator|==
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testViolatingQuotaCachesNothing
parameter_list|()
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"my_table"
argument_list|)
decl_stmt|;
name|SpaceViolationPolicyEnforcement
name|policyEnforcement
init|=
name|mock
argument_list|(
name|SpaceViolationPolicyEnforcement
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|activePolicies
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|activePolicies
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|policyEnforcement
argument_list|)
expr_stmt|;
specifier|final
name|ActivePolicyEnforcement
name|ape
init|=
operator|new
name|ActivePolicyEnforcement
argument_list|(
name|activePolicies
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|rss
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ape
operator|.
name|getPolicyEnforcement
argument_list|(
name|tableName
argument_list|)
operator|==
name|policyEnforcement
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ape
operator|.
name|getLocallyCachedPolicies
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

