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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|AcidGuaranteesTestTool
operator|.
name|FAMILIES
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|AcidGuaranteesTestTool
operator|.
name|TABLE_NAME
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
name|stream
operator|.
name|Stream
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
name|client
operator|.
name|ColumnFamilyDescriptorBuilder
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
name|TableDescriptorBuilder
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
name|CompactingMemStore
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
name|ConstantSizeRegionSplitPolicy
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
name|MemStoreLAB
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Test case that uses multiple threads to read and write multifamily rows into a table, verifying  * that reads never see partially-complete writes. This can run as a junit test, or with a main()  * function which runs against a real cluster (eg for testing with failures, region movement, etc)  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestAcidGuaranteesWithNoInMemCompaction
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
name|TestAcidGuaranteesWithNoInMemCompaction
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
specifier|private
name|AcidGuaranteesTestTool
name|tool
init|=
operator|new
name|AcidGuaranteesTestTool
argument_list|()
decl_stmt|;
specifier|protected
name|MemoryCompactionPolicy
name|getMemoryCompactionPolicy
parameter_list|()
block|{
return|return
name|MemoryCompactionPolicy
operator|.
name|NONE
return|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set small flush size for minicluster so we exercise reseeking scanners
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|128
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
comment|// prevent aggressive region split
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|ConstantSizeRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// for mob tests
name|UTIL
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
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|MemoryCompactionPolicy
name|policy
init|=
name|getMemoryCompactionPolicy
argument_list|()
decl_stmt|;
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setValue
argument_list|(
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_TYPE_KEY
argument_list|,
name|policy
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|policy
operator|==
name|MemoryCompactionPolicy
operator|.
name|EAGER
condition|)
block|{
name|builder
operator|.
name|setValue
argument_list|(
name|MemStoreLAB
operator|.
name|USEMSLAB_KEY
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setValue
argument_list|(
name|CompactingMemStore
operator|.
name|IN_MEMORY_FLUSH_THRESHOLD_FACTOR_KEY
argument_list|,
literal|"0.9"
argument_list|)
expr_stmt|;
block|}
name|Stream
operator|.
name|of
argument_list|(
name|FAMILIES
argument_list|)
operator|.
name|map
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|::
name|of
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|builder
operator|::
name|addColumnFamily
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|tool
operator|.
name|setConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTestAtomicity
parameter_list|(
name|long
name|millisToRun
parameter_list|,
name|int
name|numWriters
parameter_list|,
name|int
name|numGetters
parameter_list|,
name|int
name|numScanners
parameter_list|,
name|int
name|numUniqueRows
parameter_list|)
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
name|millisToRun
argument_list|,
name|numWriters
argument_list|,
name|numGetters
argument_list|,
name|numScanners
argument_list|,
name|numUniqueRows
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTestAtomicity
parameter_list|(
name|long
name|millisToRun
parameter_list|,
name|int
name|numWriters
parameter_list|,
name|int
name|numGetters
parameter_list|,
name|int
name|numScanners
parameter_list|,
name|int
name|numUniqueRows
parameter_list|,
name|boolean
name|useMob
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"-millis"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|millisToRun
argument_list|)
argument_list|,
literal|"-numWriters"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numWriters
argument_list|)
argument_list|,
literal|"-numGetters"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numGetters
argument_list|)
argument_list|,
literal|"-numScanners"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numScanners
argument_list|)
argument_list|,
literal|"-numUniqueRows"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numUniqueRows
argument_list|)
argument_list|,
literal|"-crazyFlush"
argument_list|)
decl_stmt|;
if|if
condition|(
name|useMob
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-useMob"
argument_list|)
expr_stmt|;
block|}
name|tool
operator|.
name|run
argument_list|(
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
literal|20000
argument_list|,
literal|5
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
literal|20000
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMixedAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
literal|20000
argument_list|,
literal|5
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobGetAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
literal|20000
argument_list|,
literal|5
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobScanAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
literal|20000
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|,
literal|3
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobMixedAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestAtomicity
argument_list|(
literal|20000
argument_list|,
literal|5
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

