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
name|util
operator|.
name|compaction
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
name|Connection
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
name|testclassification
operator|.
name|MiscTests
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
name|Sets
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestMajorCompactor
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
name|TestMajorCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|utility
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|utility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|utility
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hfile.compaction.discharger.interval"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|utility
operator|.
name|startMiniCluster
argument_list|()
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
name|utility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactingATable
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestMajorCompactor"
argument_list|)
decl_stmt|;
name|utility
operator|.
name|createMultiRegionTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|utility
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|utility
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// write data and flush multiple store files:
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|utility
operator|.
name|loadRandomRows
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|,
literal|50
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|utility
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|numberOfRegions
init|=
name|utility
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|numHFiles
init|=
name|utility
operator|.
name|getNumHFiles
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
comment|// we should have a table with more store files than we would before we major compacted.
name|assertTrue
argument_list|(
name|numberOfRegions
operator|<
name|numHFiles
argument_list|)
expr_stmt|;
name|MajorCompactor
name|compactor
init|=
operator|new
name|MajorCompactor
argument_list|(
name|utility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|200
argument_list|)
decl_stmt|;
name|compactor
operator|.
name|initializeWorkQueues
argument_list|()
expr_stmt|;
name|compactor
operator|.
name|compactAllRegions
argument_list|()
expr_stmt|;
name|compactor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
comment|// verify that the store has been completely major compacted.
name|numberOfRegions
operator|=
name|utility
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
name|numHFiles
operator|=
name|utility
operator|.
name|getNumHFiles
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numHFiles
argument_list|,
name|numberOfRegions
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
