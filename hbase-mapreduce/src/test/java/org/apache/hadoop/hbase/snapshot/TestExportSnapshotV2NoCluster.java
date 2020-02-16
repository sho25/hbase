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
name|snapshot
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
name|fs
operator|.
name|Path
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
name|HBaseCommonTestingUtility
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
operator|.
name|SnapshotMock
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
name|MapReduceTests
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Test Export Snapshot Tool; tests v2 snapshots.  * @see TestExportSnapshotV1NoCluster  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
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
name|TestExportSnapshotV2NoCluster
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
name|TestExportSnapshotV2NoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestExportSnapshotV2NoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HBaseCommonTestingUtility
name|testUtil
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|testDir
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
name|this
operator|.
name|testDir
operator|=
name|TestExportSnapshotV1NoCluster
operator|.
name|setup
argument_list|(
name|this
operator|.
name|testUtil
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotWithRefsExportFileSystemState
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotMock
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|testDir
argument_list|)
decl_stmt|;
specifier|final
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
literal|"tableWithRefsV2"
argument_list|,
literal|"tableWithRefsV2"
argument_list|)
decl_stmt|;
name|TestExportSnapshotV1NoCluster
operator|.
name|testSnapshotWithRefsExportFileSystemState
argument_list|(
name|builder
argument_list|,
name|this
operator|.
name|testUtil
argument_list|,
name|this
operator|.
name|testDir
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
