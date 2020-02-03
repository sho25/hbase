begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|conf
operator|.
name|Configuration
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
name|NamespaceDescriptor
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
name|Put
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
name|LargeTests
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
name|Before
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSpaceQuotasWithRegionReplicas
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
name|TestSpaceQuotasWithRegionReplicas
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
name|TestSpaceQuotasWithRegionReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|SpaceQuotaHelperForTests
name|helper
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|SpaceQuotaHelperForTests
operator|.
name|updateConfigForQuotas
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|removeAllQuotas
parameter_list|()
throws|throws
name|Exception
block|{
name|helper
operator|=
operator|new
name|SpaceQuotaHelperForTests
argument_list|(
name|TEST_UTIL
argument_list|,
name|testName
argument_list|,
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|helper
operator|.
name|removeAllQuotas
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaWithRegionReplicaSingleRegion
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|SpaceViolationPolicy
name|policy
range|:
name|SpaceViolationPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|setQuotaAndVerifyForRegionReplication
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaWithRegionReplicaMultipleRegion
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|SpaceViolationPolicy
name|policy
range|:
name|SpaceViolationPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|setQuotaAndVerifyForRegionReplication
argument_list|(
literal|6
argument_list|,
literal|3
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaWithSingleRegionZeroRegionReplica
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|SpaceViolationPolicy
name|policy
range|:
name|SpaceViolationPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|setQuotaAndVerifyForRegionReplication
argument_list|(
literal|1
argument_list|,
literal|0
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaWithMultipleRegionZeroRegionReplicas
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|SpaceViolationPolicy
name|policy
range|:
name|SpaceViolationPolicy
operator|.
name|values
argument_list|()
control|)
block|{
name|setQuotaAndVerifyForRegionReplication
argument_list|(
literal|6
argument_list|,
literal|0
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setQuotaAndVerifyForRegionReplication
parameter_list|(
name|int
name|region
parameter_list|,
name|int
name|replicatedRegion
parameter_list|,
name|SpaceViolationPolicy
name|policy
parameter_list|)
throws|throws
name|Exception
block|{
name|TableName
name|tn
init|=
name|helper
operator|.
name|createTableWithRegions
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
argument_list|,
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE_NAME_STR
argument_list|,
name|region
argument_list|,
name|replicatedRegion
argument_list|)
decl_stmt|;
name|helper
operator|.
name|setQuotaLimit
argument_list|(
name|tn
argument_list|,
name|policy
argument_list|,
literal|5L
argument_list|)
expr_stmt|;
name|helper
operator|.
name|writeData
argument_list|(
name|tn
argument_list|,
literal|5L
operator|*
name|SpaceQuotaHelperForTests
operator|.
name|ONE_MEGABYTE
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"to_reject"
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|SpaceQuotaHelperForTests
operator|.
name|F1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"to"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"reject"
argument_list|)
argument_list|)
expr_stmt|;
name|helper
operator|.
name|verifyViolation
argument_list|(
name|policy
argument_list|,
name|tn
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

