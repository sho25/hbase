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
name|master
operator|.
name|RegionState
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
name|assignment
operator|.
name|RegionStates
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|CloneSnapshotFromClientAfterSplittingRegionTestBase
extends|extends
name|CloneSnapshotFromClientTestBase
block|{
specifier|private
name|void
name|splitRegion
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|splitPoints
init|=
name|Bytes
operator|.
name|split
argument_list|(
name|regionInfo
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getEndKey
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|,
name|splitPoints
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloneSnapshotAfterSplittingRegion
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Turn off the CatalogJanitor
name|admin
operator|.
name|catalogJanitorSwitch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|RegionReplicaUtil
operator|.
name|removeNonDefaultRegions
argument_list|(
name|regionInfos
argument_list|)
expr_stmt|;
comment|// Split the first region
name|splitRegion
argument_list|(
name|regionInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName2
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Clone the snapshot to another table
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|getValidMethodName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName2
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|waitForTableToBeOnline
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
name|RegionStates
name|regionStates
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
comment|// The region count of the cloned table should be the same as the one of the original table
name|int
name|openRegionCountOfOriginalTable
init|=
name|regionStates
operator|.
name|getRegionByStateOfTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|openRegionCountOfClonedTable
init|=
name|regionStates
operator|.
name|getRegionByStateOfTable
argument_list|(
name|clonedTableName
argument_list|)
operator|.
name|get
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|openRegionCountOfOriginalTable
argument_list|,
name|openRegionCountOfClonedTable
argument_list|)
expr_stmt|;
name|int
name|splitRegionCountOfOriginalTable
init|=
name|regionStates
operator|.
name|getRegionByStateOfTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|SPLIT
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|splitRegionCountOfClonedTable
init|=
name|regionStates
operator|.
name|getRegionByStateOfTable
argument_list|(
name|clonedTableName
argument_list|)
operator|.
name|get
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|SPLIT
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|splitRegionCountOfOriginalTable
argument_list|,
name|splitRegionCountOfClonedTable
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|clonedTableName
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|catalogJanitorSwitch
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
