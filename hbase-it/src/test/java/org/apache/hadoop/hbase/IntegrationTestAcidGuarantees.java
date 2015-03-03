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
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|testclassification
operator|.
name|IntegrationTests
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ToolRunner
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * This Integration Test verifies acid guarantees across column families by frequently writing  * values to rows with multiple column families and concurrently reading entire rows that expect all  * column families.  *  *<p>  * Sample usage:  *<pre>  * hbase org.apache.hadoop.hbase.IntegrationTestAcidGuarantees -Dmillis=10000 -DnumWriters=50  * -DnumGetters=2 -DnumScanners=2 -DnumUniqueRows=5  *</pre>  */
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
name|IntegrationTestAcidGuarantees
extends|extends
name|IntegrationTestBase
block|{
specifier|private
specifier|static
specifier|final
name|int
name|SERVER_COUNT
init|=
literal|1
decl_stmt|;
comment|// number of slaves for the smallest cluster
comment|// The unit test version.
name|TestAcidGuarantees
name|tag
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|runTestFromCommandLine
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|c
init|=
name|getConf
argument_list|()
decl_stmt|;
name|int
name|millis
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"millis"
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
name|int
name|numWriters
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"numWriters"
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|int
name|numGetters
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"numGetters"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|int
name|numScanners
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"numScanners"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|int
name|numUniqueRows
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"numUniqueRows"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|tag
operator|.
name|runTestAtomicity
argument_list|(
name|millis
argument_list|,
name|numWriters
argument_list|,
name|numGetters
argument_list|,
name|numScanners
argument_list|,
name|numUniqueRows
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set small flush size for minicluster so we exercise reseeking scanners
name|util
operator|=
name|getTestingUtil
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|initializeCluster
argument_list|(
name|SERVER_COUNT
argument_list|)
expr_stmt|;
name|conf
operator|=
name|getConf
argument_list|()
expr_stmt|;
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
name|this
operator|.
name|setConf
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// replace the HBaseTestingUtility in the unit test with the integration test's
comment|// IntegrationTestingUtility
name|tag
operator|=
operator|new
name|TestAcidGuarantees
argument_list|()
expr_stmt|;
name|tag
operator|.
name|setHBaseTestingUtil
argument_list|(
name|util
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTablename
parameter_list|()
block|{
return|return
name|TestAcidGuarantees
operator|.
name|TABLE_NAME
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getColumnFamilies
parameter_list|()
block|{
return|return
name|Sets
operator|.
name|newHashSet
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|TestAcidGuarantees
operator|.
name|FAMILY_A
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TestAcidGuarantees
operator|.
name|FAMILY_B
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TestAcidGuarantees
operator|.
name|FAMILY_C
argument_list|)
argument_list|)
return|;
block|}
comment|// ***** Actual integration tests
annotation|@
name|Test
specifier|public
name|void
name|testGetAtomicity
parameter_list|()
throws|throws
name|Exception
block|{
name|tag
operator|.
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
name|tag
operator|.
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
name|tag
operator|.
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
comment|// **** Command line hook
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestAcidGuarantees
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

