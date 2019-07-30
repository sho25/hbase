begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
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
name|client
operator|.
name|RegionInfo
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
name|RegionInfoBuilder
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
block|{
name|MasterTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCatalogJanitorCluster
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
name|TestCatalogJanitorCluster
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
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
specifier|final
name|TableName
name|T1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|T2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|T3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"t3"
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|T1
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|HConstants
operator|.
name|CATALOG_FAMILY
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|T2
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|HConstants
operator|.
name|CATALOG_FAMILY
block|}
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|T3
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|HConstants
operator|.
name|CATALOG_FAMILY
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
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
comment|/**    * Fat method where we start with a fat hbase:meta and then gradually intro    * problems running catalogjanitor for each to ensure it triggers complaint.    * Do one big method because takes a while to build up the context we need.    * We create three tables and then make holes, overlaps, add unknown servers    * and empty out regioninfo columns. Each should up counts in the    * CatalogJanitor.Report produced.    */
annotation|@
name|Test
specifier|public
name|void
name|testConsistency
parameter_list|()
throws|throws
name|IOException
block|{
name|CatalogJanitor
name|janitor
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getCatalogJanitor
argument_list|()
decl_stmt|;
name|int
name|gc
init|=
name|janitor
operator|.
name|scan
argument_list|()
decl_stmt|;
name|CatalogJanitor
operator|.
name|Report
name|report
init|=
name|janitor
operator|.
name|getLastReport
argument_list|()
decl_stmt|;
comment|// Assert no problems.
name|assertTrue
argument_list|(
name|report
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now remove first region in table t2 to see if catalogjanitor scan notices.
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|t2Ris
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|T2
argument_list|)
decl_stmt|;
name|MetaTableAccessor
operator|.
name|deleteRegionInfo
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|t2Ris
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|gc
operator|=
name|janitor
operator|.
name|scan
argument_list|()
expr_stmt|;
name|report
operator|=
name|janitor
operator|.
name|getLastReport
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|report
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|report
operator|.
name|holes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|report
operator|.
name|holes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|regionInfo
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|T1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|report
operator|.
name|holes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|regionInfo
operator|.
name|isLast
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|report
operator|.
name|holes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSecond
argument_list|()
operator|.
name|regionInfo
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|T2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|report
operator|.
name|overlaps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Next, add overlaps to first row in t3
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|t3Ris
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|T3
argument_list|)
decl_stmt|;
name|RegionInfo
name|ri
init|=
name|t3Ris
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RegionInfo
name|newRi1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|ri
operator|.
name|getTable
argument_list|()
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|incrementRow
argument_list|(
name|ri
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|incrementRow
argument_list|(
name|ri
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Put
name|p1
init|=
name|MetaTableAccessor
operator|.
name|makePutFromRegionInfo
argument_list|(
name|newRi1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|RegionInfo
name|newRi2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|newRi1
operator|.
name|getTable
argument_list|()
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|incrementRow
argument_list|(
name|newRi1
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|incrementRow
argument_list|(
name|newRi1
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Put
name|p2
init|=
name|MetaTableAccessor
operator|.
name|makePutFromRegionInfo
argument_list|(
name|newRi2
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|MetaTableAccessor
operator|.
name|putsToMetaTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
argument_list|)
expr_stmt|;
name|gc
operator|=
name|janitor
operator|.
name|scan
argument_list|()
expr_stmt|;
name|report
operator|=
name|janitor
operator|.
name|getLastReport
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|report
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// We added two overlaps so total three.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|report
operator|.
name|overlaps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assert hole is still there.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|report
operator|.
name|holes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assert other attributes are empty still.
name|assertTrue
argument_list|(
name|report
operator|.
name|emptyRegionInfo
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|report
operator|.
name|unknownServers
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now make bad server in t1.
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|t1Ris
init|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|T1
argument_list|)
decl_stmt|;
name|RegionInfo
name|t1Ri1
init|=
name|t1Ris
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Put
name|pServer
init|=
operator|new
name|Put
argument_list|(
name|t1Ri1
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|pServer
operator|.
name|addColumn
argument_list|(
name|MetaTableAccessor
operator|.
name|getCatalogFamily
argument_list|()
argument_list|,
name|MetaTableAccessor
operator|.
name|getServerColumn
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bad.server.example.org:1234"
argument_list|)
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|putsToMetaTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|pServer
argument_list|)
argument_list|)
expr_stmt|;
name|gc
operator|=
name|janitor
operator|.
name|scan
argument_list|()
expr_stmt|;
name|report
operator|=
name|janitor
operator|.
name|getLastReport
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|report
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|report
operator|.
name|unknownServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Finally, make an empty regioninfo in t1.
name|RegionInfo
name|t1Ri2
init|=
name|t1Ris
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Put
name|pEmptyRI
init|=
operator|new
name|Put
argument_list|(
name|t1Ri2
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|pEmptyRI
operator|.
name|addColumn
argument_list|(
name|MetaTableAccessor
operator|.
name|getCatalogFamily
argument_list|()
argument_list|,
name|MetaTableAccessor
operator|.
name|getRegionInfoColumn
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|putsToMetaTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|pEmptyRI
argument_list|)
argument_list|)
expr_stmt|;
name|gc
operator|=
name|janitor
operator|.
name|scan
argument_list|()
expr_stmt|;
name|report
operator|=
name|janitor
operator|.
name|getLastReport
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|report
operator|.
name|emptyRegionInfo
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Take last byte and add one to it.    */
specifier|private
specifier|static
name|byte
index|[]
name|incrementRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
if|if
condition|(
name|row
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|byte
index|[]
block|{
literal|'0'
block|}
return|;
block|}
name|row
index|[
name|row
operator|.
name|length
operator|-
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
operator|(
name|int
operator|)
name|row
index|[
name|row
operator|.
name|length
operator|-
literal|1
index|]
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
return|return
name|row
return|;
block|}
block|}
end_class

end_unit

