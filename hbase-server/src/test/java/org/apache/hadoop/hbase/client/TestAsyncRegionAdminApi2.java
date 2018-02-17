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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|AsyncMetaTableAccessor
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
name|HRegionLocation
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
name|ClassRule
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
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Optional
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
name|TableName
operator|.
name|META_TABLE_NAME
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

begin_comment
comment|/**  * Class to test asynchronous region admin operations.  * @see TestAsyncRegionAdminApi This test and it used to be joined it was taking longer than our  * ten minute timeout so they were split.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
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
name|TestAsyncRegionAdminApi2
extends|extends
name|TestAsyncAdminBase
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
name|TestAsyncRegionAdminApi2
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testGetRegionLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|RawAsyncHBaseAdmin
name|rawAdmin
init|=
operator|(
name|RawAsyncHBaseAdmin
operator|)
name|ASYNC_CONN
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|AsyncTableRegionLocator
name|locator
init|=
name|ASYNC_CONN
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HRegionLocation
name|regionLocation
init|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mmm"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|RegionInfo
name|region
init|=
name|regionLocation
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|byte
index|[]
name|regionName
init|=
name|regionLocation
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|HRegionLocation
name|location
init|=
name|rawAdmin
operator|.
name|getRegionLocation
argument_list|(
name|regionName
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|regionName
argument_list|,
name|location
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|location
operator|=
name|rawAdmin
operator|.
name|getRegionLocation
argument_list|(
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|regionName
argument_list|,
name|location
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
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
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|FAMILY
block|}
decl_stmt|;
specifier|final
name|int
name|rows
init|=
literal|10000
decl_stmt|;
name|TestAsyncRegionAdminApi
operator|.
name|loadData
argument_list|(
name|tableName
argument_list|,
name|families
argument_list|,
name|rows
argument_list|)
expr_stmt|;
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|metaTable
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionLocations
init|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|originalCount
init|=
name|regionLocations
operator|.
name|size
argument_list|()
decl_stmt|;
name|initSplitMergeSwitch
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|splitSwitch
argument_list|(
literal|false
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|split
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rows
operator|/
literal|2
argument_list|)
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|//Expected
block|}
name|int
name|count
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
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
name|assertFalse
argument_list|(
name|admin
operator|.
name|splitSwitch
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|tableName
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
name|count
operator|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
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
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|originalCount
operator|<
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
comment|// It was ignored in TestSplitOrMergeStatus, too
specifier|public
name|void
name|testMergeSwitch
parameter_list|()
throws|throws
name|Exception
block|{
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|FAMILY
block|}
decl_stmt|;
name|TestAsyncRegionAdminApi
operator|.
name|loadData
argument_list|(
name|tableName
argument_list|,
name|families
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|metaTable
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionLocations
init|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|originalCount
init|=
name|regionLocations
operator|.
name|size
argument_list|()
decl_stmt|;
name|initSplitMergeSwitch
argument_list|()
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|tableName
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|int
name|postSplitCount
init|=
name|originalCount
decl_stmt|;
while|while
condition|(
operator|(
name|postSplitCount
operator|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
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
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"originalCount="
operator|+
name|originalCount
operator|+
literal|", postSplitCount="
operator|+
name|postSplitCount
argument_list|,
name|originalCount
operator|!=
name|postSplitCount
argument_list|)
expr_stmt|;
comment|// Merge switch is off so merge should NOT succeed.
name|assertTrue
argument_list|(
name|admin
operator|.
name|mergeSwitch
argument_list|(
literal|false
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
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
name|admin
operator|.
name|mergeRegions
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"postSplitCount="
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
comment|// Merge switch is on so merge should succeed.
name|assertFalse
argument_list|(
name|admin
operator|.
name|mergeSwitch
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|mergeRegions
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
name|count
operator|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
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
operator|)
operator|==
name|count
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initSplitMergeSwitch
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|admin
operator|.
name|isSplitEnabled
argument_list|()
operator|.
name|get
argument_list|()
condition|)
block|{
name|admin
operator|.
name|splitSwitch
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|admin
operator|.
name|isMergeEnabled
argument_list|()
operator|.
name|get
argument_list|()
condition|)
block|{
name|admin
operator|.
name|mergeSwitch
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|admin
operator|.
name|isSplitEnabled
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isMergeEnabled
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeRegions
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
index|[]
name|splitRows
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"6"
argument_list|)
block|}
decl_stmt|;
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|,
name|splitRows
argument_list|)
expr_stmt|;
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|metaTable
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionLocations
init|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|RegionInfo
name|regionA
decl_stmt|;
name|RegionInfo
name|regionB
decl_stmt|;
comment|// merge with full name
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|regionLocations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|regionA
operator|=
name|regionLocations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegion
argument_list|()
expr_stmt|;
name|regionB
operator|=
name|regionLocations
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegion
argument_list|()
expr_stmt|;
name|admin
operator|.
name|mergeRegions
argument_list|(
name|regionA
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionB
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|regionLocations
operator|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|regionLocations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge with encoded name
name|regionA
operator|=
name|regionLocations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegion
argument_list|()
expr_stmt|;
name|regionB
operator|=
name|regionLocations
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegion
argument_list|()
expr_stmt|;
name|admin
operator|.
name|mergeRegions
argument_list|(
name|regionA
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionB
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|regionLocations
operator|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regionLocations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitTable
parameter_list|()
throws|throws
name|Exception
block|{
name|initSplitMergeSwitch
argument_list|()
expr_stmt|;
name|splitTest
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSplitTable"
argument_list|)
argument_list|,
literal|3000
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|splitTest
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSplitTableWithSplitPoint"
argument_list|)
argument_list|,
literal|3000
argument_list|,
literal|false
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|splitTest
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSplitTableRegion"
argument_list|)
argument_list|,
literal|3000
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|splitTest
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSplitTableRegionWithSplitPoint2"
argument_list|)
argument_list|,
literal|3000
argument_list|,
literal|true
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|splitTest
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|int
name|rowCount
parameter_list|,
name|boolean
name|isSplitRegion
parameter_list|,
name|byte
index|[]
name|splitPoint
parameter_list|)
throws|throws
name|Exception
block|{
comment|// create table
name|createTableWithDefaultConf
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|AsyncTable
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|metaTable
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionLocations
init|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regionLocations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|AsyncTable
argument_list|<
name|?
argument_list|>
name|table
init|=
name|ASYNC_CONN
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|rowCount
condition|;
name|i
operator|++
control|)
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
literal|null
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|putAll
argument_list|(
name|puts
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
if|if
condition|(
name|isSplitRegion
condition|)
block|{
if|if
condition|(
name|splitPoint
operator|==
literal|null
condition|)
block|{
name|admin
operator|.
name|splitRegion
argument_list|(
name|regionLocations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|splitRegion
argument_list|(
name|regionLocations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|splitPoint
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|splitPoint
operator|==
literal|null
condition|)
block|{
name|admin
operator|.
name|split
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|split
argument_list|(
name|tableName
argument_list|,
name|splitPoint
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|count
init|=
literal|0
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
literal|45
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|regionLocations
operator|=
name|AsyncMetaTableAccessor
operator|.
name|getTableHRegionLocations
argument_list|(
name|metaTable
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|count
operator|=
name|regionLocations
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|count
operator|>=
literal|2
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000L
argument_list|)
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
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
