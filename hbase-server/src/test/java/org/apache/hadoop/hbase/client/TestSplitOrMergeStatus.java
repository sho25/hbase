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
name|client
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
name|assertTrue
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
name|fail
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
name|List
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
name|ExecutionException
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
name|Future
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
name|TimeUnit
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
name|HRegionInfo
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
name|testclassification
operator|.
name|ClientTests
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
name|Threads
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
name|Ignore
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
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSplitOrMergeStatus
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
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
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitSwitch
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
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|FAMILY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|RegionLocator
name|locator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|originalCount
init|=
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|initSwitchStatus
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|boolean
index|[]
name|results
init|=
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|originalCount
operator|==
name|count
argument_list|)
expr_stmt|;
name|results
operator|=
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
operator|(
name|count
operator|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
operator|)
operator|==
name|originalCount
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|count
operator|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|originalCount
operator|<
name|count
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testMergeSwitch
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
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|FAMILY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|int
name|originalCount
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|initSwitchStatus
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|postSplitCount
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
operator|(
name|postSplitCount
operator|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
operator|)
operator|==
name|originalCount
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"originalCount="
operator|+
name|originalCount
operator|+
literal|", newCount="
operator|+
name|postSplitCount
argument_list|,
name|originalCount
operator|!=
name|postSplitCount
argument_list|)
expr_stmt|;
comment|// Merge switch is off so merge should NOT succeed.
name|boolean
index|[]
name|results
init|=
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|regions
operator|.
name|size
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
name|Future
argument_list|<
name|?
argument_list|>
name|f
init|=
name|admin
operator|.
name|mergeRegionsAsync
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|f
operator|.
name|get
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not get here."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
comment|// Expected.
block|}
name|int
name|count
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"newCount="
operator|+
name|postSplitCount
operator|+
literal|", count="
operator|+
name|count
argument_list|,
name|postSplitCount
operator|==
name|count
argument_list|)
expr_stmt|;
name|results
operator|=
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
expr_stmt|;
name|regions
operator|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|f
operator|=
name|admin
operator|.
name|mergeRegionsAsync
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|f
operator|.
name|get
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|count
operator|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|postSplitCount
operator|/
literal|2
comment|/*Merge*/
operator|)
operator|==
name|count
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiSwitches
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|boolean
index|[]
name|switches
init|=
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|,
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
decl_stmt|;
for|for
control|(
name|boolean
name|s
range|:
name|switches
control|)
block|{
name|assertTrue
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|admin
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initSwitchStatus
parameter_list|(
name|Admin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|admin
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
condition|)
block|{
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|admin
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
condition|)
block|{
name|admin
operator|.
name|setSplitOrMergeEnabled
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|admin
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|MasterSwitchType
operator|.
name|SPLIT
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|MasterSwitchType
operator|.
name|MERGE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

