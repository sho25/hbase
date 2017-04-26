begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|normalizer
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
name|HConstants
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
name|HTableDescriptor
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
name|MetaTableAccessor
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
name|MiniHBaseCluster
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
name|client
operator|.
name|Table
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
name|HMaster
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
name|TableNamespaceManager
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
name|normalizer
operator|.
name|NormalizationPlan
operator|.
name|PlanType
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
name|namespace
operator|.
name|TestNamespaceAuditor
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
name|QuotaUtil
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
name|HRegion
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
name|Region
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|test
operator|.
name|LoadTestKVGenerator
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
name|BeforeClass
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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

begin_comment
comment|/**  * Testing {@link SimpleRegionNormalizer} on minicluster.  */
end_comment

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
name|TestSimpleRegionNormalizerOnCluster
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestSimpleRegionNormalizerOnCluster
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
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYNAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
comment|// we will retry operations when PleaseHoldException is thrown
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Start a cluster of two regionservers.
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|TestNamespaceAuditor
operator|.
name|waitForQuotaEnabled
argument_list|(
name|TEST_UTIL
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
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
name|Test
argument_list|(
name|timeout
operator|=
literal|90000
argument_list|)
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testRegionNormalizationSplitOnCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionNormalizationSplitOnCluster
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testRegionNormalizationSplitOnCluster
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|void
name|testRegionNormalizationSplitOnCluster
parameter_list|(
name|boolean
name|limitedByQuota
parameter_list|)
throws|throws
name|Exception
block|{
name|TableName
name|TABLENAME
decl_stmt|;
if|if
condition|(
name|limitedByQuota
condition|)
block|{
name|String
name|nsp
init|=
literal|"np2"
decl_stmt|;
name|NamespaceDescriptor
name|nspDesc
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|nsp
argument_list|)
operator|.
name|addConfiguration
argument_list|(
name|TableNamespaceManager
operator|.
name|KEY_MAX_REGIONS
argument_list|,
literal|"5"
argument_list|)
operator|.
name|addConfiguration
argument_list|(
name|TableNamespaceManager
operator|.
name|KEY_MAX_TABLES
argument_list|,
literal|"2"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|nspDesc
argument_list|)
expr_stmt|;
name|TABLENAME
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|nsp
operator|+
name|TableName
operator|.
name|NAMESPACE_DELIM
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TABLENAME
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|TABLENAME
argument_list|,
name|FAMILYNAME
argument_list|,
literal|5
argument_list|)
init|)
block|{
comment|// Need to get sorted list of regions here
name|List
argument_list|<
name|HRegion
argument_list|>
name|generatedRegions
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|generatedRegions
argument_list|,
operator|new
name|Comparator
argument_list|<
name|HRegion
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HRegion
name|o1
parameter_list|,
name|HRegion
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setNormalizationEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TABLENAME
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|MetaTableAccessor
operator|.
name|getRegionCount
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now trigger a split and stop when the split is in progress
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// to let region load to update
name|m
operator|.
name|normalizeRegions
argument_list|()
expr_stmt|;
if|if
condition|(
name|limitedByQuota
condition|)
block|{
name|long
name|skippedSplitcnt
init|=
literal|0
decl_stmt|;
do|do
block|{
name|skippedSplitcnt
operator|=
name|m
operator|.
name|getRegionNormalizer
argument_list|()
operator|.
name|getSkippedCount
argument_list|(
name|PlanType
operator|.
name|SPLIT
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|skippedSplitcnt
operator|==
literal|0L
condition|)
do|;
assert|assert
operator|(
name|skippedSplitcnt
operator|>
literal|0
operator|)
assert|;
block|}
else|else
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|int
name|cnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|String
name|regionName
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionName
operator|.
name|startsWith
argument_list|(
literal|"testRegionNormalizationSplitOnCluster,zzzzz"
argument_list|)
condition|)
block|{
name|cnt
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cnt
operator|>=
literal|2
condition|)
block|{
break|break;
block|}
block|}
block|}
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|testRegionNormalizationMergeOnCluster
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
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// create 5 regions with sizes to trigger merge of small regions
try|try
init|(
name|Table
name|ht
init|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
name|FAMILYNAME
argument_list|,
literal|5
argument_list|)
init|)
block|{
comment|// Need to get sorted list of regions here
name|List
argument_list|<
name|HRegion
argument_list|>
name|generatedRegions
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|generatedRegions
argument_list|,
operator|new
name|Comparator
argument_list|<
name|HRegion
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HRegion
name|o1
parameter_list|,
name|HRegion
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|region
operator|=
name|generatedRegions
operator|.
name|get
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|generateTestData
argument_list|(
name|region
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setNormalizationEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|tableName
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|MetaTableAccessor
operator|.
name|getRegionCount
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now trigger a merge and stop when the merge is in progress
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// to let region load to update
name|m
operator|.
name|normalizeRegions
argument_list|()
expr_stmt|;
while|while
condition|(
name|MetaTableAccessor
operator|.
name|getRegionCount
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
operator|>
literal|4
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for normalization merge to complete"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|MetaTableAccessor
operator|.
name|getRegionCount
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|generateTestData
parameter_list|(
name|Region
name|region
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
comment|// generating 1Mb values
name|LoadTestKVGenerator
name|dataGenerator
init|=
operator|new
name|LoadTestKVGenerator
argument_list|(
literal|1024
operator|*
literal|1024
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRows
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|1
condition|;
operator|++
name|j
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|dataGenerator
operator|.
name|generateRandomSizeValue
argument_list|(
name|key
argument_list|,
name|col
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILYNAME
argument_list|,
name|col
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

