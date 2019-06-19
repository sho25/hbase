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
name|snapshot
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|LocalFileSystem
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
name|snapshot
operator|.
name|SnapshotManager
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
comment|/**  * Test Export Snapshot Tool  */
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
name|TestExportSnapshotNoCluster
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
name|TestExportSnapshotNoCluster
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
name|TestExportSnapshotNoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseCommonTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|testDir
decl_stmt|;
specifier|public
specifier|static
name|void
name|setUpBaseConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.map.maxattempts"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|testDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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
comment|// Make sure testDir is on LocalFileSystem
name|testDir
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|makeQualified
argument_list|(
name|URI
operator|.
name|create
argument_list|(
literal|"file:///"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"FileSystem '"
operator|+
name|fs
operator|+
literal|"' is not local"
argument_list|,
name|fs
operator|instanceof
name|LocalFileSystem
argument_list|)
expr_stmt|;
name|setUpBaseConf
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Mock a snapshot with files in the archive dir,    * two regions, and one reference file.    */
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotWithRefsExportFileSystemState
parameter_list|()
throws|throws
name|Exception
block|{
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotMock
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|testDir
argument_list|)
decl_stmt|;
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
literal|"tableWithRefsV1"
argument_list|,
literal|"tableWithRefsV1"
argument_list|)
decl_stmt|;
name|testSnapshotWithRefsExportFileSystemState
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|snapshotMock
operator|=
operator|new
name|SnapshotMock
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
name|builder
operator|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
literal|"tableWithRefsV2"
argument_list|,
literal|"tableWithRefsV2"
argument_list|)
expr_stmt|;
name|testSnapshotWithRefsExportFileSystemState
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generates a couple of regions for the specified SnapshotMock,    * and then it will run the export and verification.    */
specifier|private
name|void
name|testSnapshotWithRefsExportFileSystemState
parameter_list|(
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
index|[]
name|r1Files
init|=
name|builder
operator|.
name|addRegion
argument_list|()
decl_stmt|;
name|Path
index|[]
name|r2Files
init|=
name|builder
operator|.
name|addRegion
argument_list|()
decl_stmt|;
name|builder
operator|.
name|commit
argument_list|()
expr_stmt|;
name|int
name|snapshotFilesCount
init|=
name|r1Files
operator|.
name|length
operator|+
name|r2Files
operator|.
name|length
decl_stmt|;
name|String
name|snapshotName
init|=
name|builder
operator|.
name|getSnapshotDescription
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|TableName
name|tableName
init|=
name|builder
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|TestExportSnapshot
operator|.
name|testExportFileSystemState
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotFilesCount
argument_list|,
name|testDir
argument_list|,
name|getDestinationDir
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Path
name|getDestinationDir
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"export-test"
argument_list|)
argument_list|,
literal|"export-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HDFS export destination path: "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
block|}
end_class

end_unit

