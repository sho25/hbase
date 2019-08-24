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
name|TestSpaceQuotaRemoval
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
name|TestSpaceQuotaRemoval
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
name|testSetQuotaAndThenRemoveInOneWithNoInserts
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemoveInOneAmongTwoTables
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveInOneWithNoWrite
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemoveInOneAmongTwoTables
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveInOneWithNoWritesCompaction
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemoveInOneAmongTwoTables
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES_COMPACTIONS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveInOneWithDisable
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemoveInOneAmongTwoTables
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveWithNoInserts
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemove
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveWithNoWrite
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemove
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveWithNoWritesCompactions
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemove
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES_COMPACTIONS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenRemoveWithDisable
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaAndThenRemove
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenDisableIncrEnableWithNoInserts
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaNextDisableThenIncreaseFinallyEnable
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenDisableIncrEnableWithNoWrite
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaNextDisableThenIncreaseFinallyEnable
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenDisableIncrEnableWithNoWritesCompaction
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaNextDisableThenIncreaseFinallyEnable
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES_COMPACTIONS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetQuotaAndThenDisableIncrEnableWithDisable
parameter_list|()
throws|throws
name|Exception
block|{
name|setQuotaNextDisableThenIncreaseFinallyEnable
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setQuotaAndThenRemove
parameter_list|(
name|SpaceViolationPolicy
name|policy
parameter_list|)
throws|throws
name|Exception
block|{
name|Put
name|put
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
name|put
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
comment|// Do puts until we violate space policy
specifier|final
name|TableName
name|tn
init|=
name|helper
operator|.
name|writeUntilViolationAndVerifyViolation
argument_list|(
name|policy
argument_list|,
name|put
argument_list|)
decl_stmt|;
comment|// Now, remove the quota
name|helper
operator|.
name|removeQuotaFromtable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
comment|// Put some rows now: should not violate as quota settings removed
name|helper
operator|.
name|verifyNoViolation
argument_list|(
name|tn
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setQuotaAndThenRemoveInOneAmongTwoTables
parameter_list|(
name|SpaceViolationPolicy
name|policy
parameter_list|)
throws|throws
name|Exception
block|{
name|Put
name|put
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
name|put
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
comment|// Do puts until we violate space policy on table tn1
specifier|final
name|TableName
name|tn1
init|=
name|helper
operator|.
name|writeUntilViolationAndVerifyViolation
argument_list|(
name|policy
argument_list|,
name|put
argument_list|)
decl_stmt|;
comment|// Do puts until we violate space policy on table tn2
specifier|final
name|TableName
name|tn2
init|=
name|helper
operator|.
name|writeUntilViolationAndVerifyViolation
argument_list|(
name|policy
argument_list|,
name|put
argument_list|)
decl_stmt|;
comment|// Now, remove the quota from table tn1
name|helper
operator|.
name|removeQuotaFromtable
argument_list|(
name|tn1
argument_list|)
expr_stmt|;
comment|// Put a new row now on tn1: should not violate as quota settings removed
name|helper
operator|.
name|verifyNoViolation
argument_list|(
name|tn1
argument_list|,
name|put
argument_list|)
expr_stmt|;
comment|// Put a new row now on tn2: should violate as quota settings exists
name|helper
operator|.
name|verifyViolation
argument_list|(
name|policy
argument_list|,
name|tn2
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setQuotaNextDisableThenIncreaseFinallyEnable
parameter_list|(
name|SpaceViolationPolicy
name|policy
parameter_list|)
throws|throws
name|Exception
block|{
name|Put
name|put
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
name|put
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
comment|// Do puts until we violate space policy
specifier|final
name|TableName
name|tn
init|=
name|helper
operator|.
name|writeUntilViolationAndVerifyViolation
argument_list|(
name|policy
argument_list|,
name|put
argument_list|)
decl_stmt|;
comment|// Disable the table; in case of SpaceViolationPolicy.DISABLE already disabled
if|if
condition|(
operator|!
name|policy
operator|.
name|equals
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableDisabled
argument_list|(
name|tn
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
block|}
comment|// Now, increase limit and perform put
name|helper
operator|.
name|setQuotaLimit
argument_list|(
name|tn
argument_list|,
name|policy
argument_list|,
literal|4L
argument_list|)
expr_stmt|;
comment|// in case of disable policy quota manager will enable it
if|if
condition|(
operator|!
name|policy
operator|.
name|equals
argument_list|(
name|SpaceViolationPolicy
operator|.
name|DISABLE
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|tn
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
comment|// Put some row now: should not violate as quota limit increased
name|helper
operator|.
name|verifyNoViolation
argument_list|(
name|tn
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

