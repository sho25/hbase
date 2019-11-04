begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Map
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
name|FileStatus
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
name|regionserver
operator|.
name|HRegionFileSystem
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
name|StoreFileInfo
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
name|FSUtils
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|SnapshotProtos
operator|.
name|SnapshotDescription
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
import|;
end_import

begin_comment
comment|/**  * Validate if storefile length match  * both snapshop manifest and filesystem.  */
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
name|TestSnapshotStoreFileSize
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
name|TestSnapshotStoreFileSize
operator|.
name|class
argument_list|)
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
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
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
name|String
name|SNAPSHOT_NAME
init|=
literal|"s1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY_NAME
init|=
literal|"cf"
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardown
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
annotation|@
name|Test
specifier|public
name|void
name|testIsStoreFileSizeMatchFilesystemAndManifest
parameter_list|()
throws|throws
name|IOException
block|{
name|admin
operator|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|fs
operator|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_NAME
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|admin
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|loadRandomRows
argument_list|(
name|table
argument_list|,
name|FAMILY_NAME
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|3
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|SNAPSHOT_NAME
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|storeFileInfoFromManifest
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|storeFileInfoFromFS
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|storeFileName
init|=
literal|""
decl_stmt|;
name|long
name|storeFilesize
init|=
literal|0L
decl_stmt|;
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|SNAPSHOT_NAME
argument_list|,
name|UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|)
decl_stmt|;
name|SnapshotDescription
name|snapshotDesc
init|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|snaphotManifest
init|=
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshotDesc
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|regionManifest
init|=
name|snaphotManifest
operator|.
name|getRegionManifests
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
name|regionManifest
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
name|family
init|=
name|regionManifest
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFamilyFiles
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SnapshotRegionManifest
operator|.
name|StoreFile
argument_list|>
name|storeFiles
init|=
name|family
operator|.
name|getStoreFilesList
argument_list|()
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
name|storeFiles
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|storeFileName
operator|=
name|storeFiles
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getName
argument_list|()
expr_stmt|;
name|storeFilesize
operator|=
name|storeFiles
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getFileSize
argument_list|()
expr_stmt|;
name|storeFileInfoFromManifest
operator|.
name|put
argument_list|(
name|storeFileName
argument_list|,
name|storeFilesize
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionsInfo
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|regionInfo
range|:
name|regionsInfo
control|)
block|{
name|HRegionFileSystem
name|hRegionFileSystem
init|=
name|HRegionFileSystem
operator|.
name|openRegionFromFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|regionInfo
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFilesFS
init|=
name|hRegionFileSystem
operator|.
name|getStoreFiles
argument_list|(
name|FAMILY_NAME
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|StoreFileInfo
argument_list|>
name|sfIterator
init|=
name|storeFilesFS
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|sfIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|StoreFileInfo
name|sfi
init|=
name|sfIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|fileStatus
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|sfi
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|storeFileName
operator|=
name|fileStatus
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|storeFilesize
operator|=
name|fileStatus
index|[
literal|0
index|]
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|storeFileInfoFromFS
operator|.
name|put
argument_list|(
name|storeFileName
argument_list|,
name|storeFilesize
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|storeFileInfoFromManifest
argument_list|,
name|storeFileInfoFromFS
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
