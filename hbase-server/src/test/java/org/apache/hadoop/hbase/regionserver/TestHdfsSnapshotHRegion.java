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
name|io
operator|.
name|IOException
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
name|lang3
operator|.
name|StringUtils
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
name|RegionServerTests
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
name|FSUtils
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
name|hdfs
operator|.
name|DFSClient
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
name|Assert
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
name|TestHdfsSnapshotHRegion
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
name|TestHdfsSnapshotHRegion
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
name|String
name|SNAPSHOT_NAME
init|=
literal|"foo_snapshot"
decl_stmt|;
specifier|private
name|Table
name|table
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
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
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
name|DFSClient
name|client
decl_stmt|;
specifier|private
name|String
name|baseDir
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
name|Configuration
name|c
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|c
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|table
operator|=
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
comment|// setup the hdfssnapshots
name|client
operator|=
operator|new
name|DFSClient
argument_list|(
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getURI
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|fullUrIPath
init|=
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|uriString
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|baseDir
operator|=
name|StringUtils
operator|.
name|removeStart
argument_list|(
name|fullUrIPath
argument_list|,
name|uriString
argument_list|)
expr_stmt|;
name|client
operator|.
name|allowSnapshot
argument_list|(
name|baseDir
argument_list|)
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
name|client
operator|.
name|deleteSnapshot
argument_list|(
name|baseDir
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
expr_stmt|;
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
name|testOpeningReadOnlyRegionBasic
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|snapshotDir
init|=
name|client
operator|.
name|createSnapshot
argument_list|(
name|baseDir
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
decl_stmt|;
name|RegionInfo
name|firstRegion
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|)
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|HRegion
name|snapshottedRegion
init|=
name|openSnapshotRegion
argument_list|(
name|firstRegion
argument_list|,
name|tableDir
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|snapshottedRegion
argument_list|)
expr_stmt|;
name|snapshottedRegion
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSnapshottingWithTmpSplitsAndMergeDirectoriesPresent
parameter_list|()
throws|throws
name|Exception
block|{
comment|// lets get a region and create those directories and make sure we ignore them
name|RegionInfo
name|firstRegion
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|String
name|encodedName
init|=
name|firstRegion
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Path
name|regionDirectoryPath
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|encodedName
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|regionDirectoryPath
argument_list|,
name|HRegionFileSystem
operator|.
name|REGION_TEMP_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|regionDirectoryPath
argument_list|,
name|HRegionFileSystem
operator|.
name|REGION_SPLITS_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|regionDirectoryPath
argument_list|,
name|HRegionFileSystem
operator|.
name|REGION_MERGES_DIR
argument_list|)
argument_list|)
expr_stmt|;
comment|// now snapshot
name|String
name|snapshotDir
init|=
name|client
operator|.
name|createSnapshot
argument_list|(
name|baseDir
argument_list|,
literal|"foo_snapshot"
argument_list|)
decl_stmt|;
comment|// everything should still open just fine
name|HRegion
name|snapshottedRegion
init|=
name|openSnapshotRegion
argument_list|(
name|firstRegion
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|)
argument_list|,
name|TABLE_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|snapshottedRegion
argument_list|)
expr_stmt|;
comment|// no errors and the region should open
name|snapshottedRegion
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HRegion
name|openSnapshotRegion
parameter_list|(
name|RegionInfo
name|firstRegion
parameter_list|,
name|Path
name|tableDir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|HRegion
operator|.
name|openReadOnlyFileSystemHRegion
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|tableDir
argument_list|,
name|firstRegion
argument_list|,
name|table
operator|.
name|getDescriptor
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

