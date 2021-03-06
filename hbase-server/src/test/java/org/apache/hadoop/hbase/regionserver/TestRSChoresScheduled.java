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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|ExecutorStatusChore
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
name|ScheduledChore
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
name|StartMiniClusterOption
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
name|RegionServerTests
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
name|Assert
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

begin_comment
comment|/**  * Tests to validate if HRegionServer default chores are scheduled  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestRSChoresScheduled
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
name|TestRSChoresScheduled
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|hRegionServer
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numRegionServers
argument_list|(
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hRegionServer
operator|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
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
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|TestChoreField
parameter_list|<
name|E
extends|extends
name|ScheduledChore
parameter_list|>
block|{
specifier|private
name|E
name|getChoreObj
parameter_list|(
name|String
name|fieldName
parameter_list|)
throws|throws
name|NoSuchFieldException
throws|,
name|IllegalAccessException
block|{
name|Field
name|hRegionServerField
init|=
name|HRegionServer
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|hRegionServerField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|E
name|choreFieldVal
init|=
operator|(
name|E
operator|)
name|hRegionServerField
operator|.
name|get
argument_list|(
name|hRegionServer
argument_list|)
decl_stmt|;
return|return
name|choreFieldVal
return|;
block|}
specifier|private
name|void
name|testIfChoreScheduled
parameter_list|(
name|E
name|choreObj
parameter_list|)
block|{
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|choreObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hRegionServer
operator|.
name|getChoreService
argument_list|()
operator|.
name|isChoreScheduled
argument_list|(
name|choreObj
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultScheduledChores
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test if movedRegionsCleaner chore is scheduled by default in HRegionServer init
name|TestChoreField
argument_list|<
name|HRegionServer
operator|.
name|MovedRegionsCleaner
argument_list|>
name|movedRegionsCleanerTestChoreField
init|=
operator|new
name|TestChoreField
argument_list|<>
argument_list|()
decl_stmt|;
name|HRegionServer
operator|.
name|MovedRegionsCleaner
name|movedRegionsCleaner
init|=
name|movedRegionsCleanerTestChoreField
operator|.
name|getChoreObj
argument_list|(
literal|"movedRegionsCleaner"
argument_list|)
decl_stmt|;
name|movedRegionsCleanerTestChoreField
operator|.
name|testIfChoreScheduled
argument_list|(
name|movedRegionsCleaner
argument_list|)
expr_stmt|;
comment|// test if compactedHFilesDischarger chore is scheduled by default in HRegionServer init
name|TestChoreField
argument_list|<
name|CompactedHFilesDischarger
argument_list|>
name|compactedHFilesDischargerTestChoreField
init|=
operator|new
name|TestChoreField
argument_list|<>
argument_list|()
decl_stmt|;
name|CompactedHFilesDischarger
name|compactedHFilesDischarger
init|=
name|compactedHFilesDischargerTestChoreField
operator|.
name|getChoreObj
argument_list|(
literal|"compactedFileDischarger"
argument_list|)
decl_stmt|;
name|compactedHFilesDischargerTestChoreField
operator|.
name|testIfChoreScheduled
argument_list|(
name|compactedHFilesDischarger
argument_list|)
expr_stmt|;
comment|// test if compactionChecker chore is scheduled by default in HRegionServer init
name|TestChoreField
argument_list|<
name|ScheduledChore
argument_list|>
name|compactionCheckerTestChoreField
init|=
operator|new
name|TestChoreField
argument_list|<>
argument_list|()
decl_stmt|;
name|ScheduledChore
name|compactionChecker
init|=
name|compactionCheckerTestChoreField
operator|.
name|getChoreObj
argument_list|(
literal|"compactionChecker"
argument_list|)
decl_stmt|;
name|compactionCheckerTestChoreField
operator|.
name|testIfChoreScheduled
argument_list|(
name|compactionChecker
argument_list|)
expr_stmt|;
comment|// test if periodicFlusher chore is scheduled by default in HRegionServer init
name|TestChoreField
argument_list|<
name|ScheduledChore
argument_list|>
name|periodicMemstoreFlusherTestChoreField
init|=
operator|new
name|TestChoreField
argument_list|<>
argument_list|()
decl_stmt|;
name|ScheduledChore
name|periodicFlusher
init|=
name|periodicMemstoreFlusherTestChoreField
operator|.
name|getChoreObj
argument_list|(
literal|"periodicFlusher"
argument_list|)
decl_stmt|;
name|periodicMemstoreFlusherTestChoreField
operator|.
name|testIfChoreScheduled
argument_list|(
name|periodicFlusher
argument_list|)
expr_stmt|;
comment|// test if nonceManager chore is scheduled by default in HRegionServer init
name|TestChoreField
argument_list|<
name|ScheduledChore
argument_list|>
name|nonceManagerTestChoreField
init|=
operator|new
name|TestChoreField
argument_list|<>
argument_list|()
decl_stmt|;
name|ScheduledChore
name|nonceManagerChore
init|=
name|nonceManagerTestChoreField
operator|.
name|getChoreObj
argument_list|(
literal|"nonceManagerChore"
argument_list|)
decl_stmt|;
name|nonceManagerTestChoreField
operator|.
name|testIfChoreScheduled
argument_list|(
name|nonceManagerChore
argument_list|)
expr_stmt|;
comment|// test if executorStatusChore chore is scheduled by default in HRegionServer init
name|TestChoreField
argument_list|<
name|ExecutorStatusChore
argument_list|>
name|executorStatusChoreTestChoreField
init|=
operator|new
name|TestChoreField
argument_list|<>
argument_list|()
decl_stmt|;
name|ExecutorStatusChore
name|executorStatusChore
init|=
name|executorStatusChoreTestChoreField
operator|.
name|getChoreObj
argument_list|(
literal|"executorStatusChore"
argument_list|)
decl_stmt|;
name|executorStatusChoreTestChoreField
operator|.
name|testIfChoreScheduled
argument_list|(
name|executorStatusChore
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

