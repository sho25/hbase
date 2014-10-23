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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|HTableDescriptor
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
name|SmallTests
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|io
operator|.
name|HFileLink
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
name|mob
operator|.
name|MobConstants
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
name|monitoring
operator|.
name|MonitoredTask
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|snapshot
operator|.
name|MobSnapshotTestingUtils
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
name|util
operator|.
name|FSTableDescriptors
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Test the restore/clone operation from a file-system point of view.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMobRestoreSnapshotHelper
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
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
specifier|final
specifier|static
name|String
name|TEST_HFILE
init|=
literal|"abc"
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Path
name|archiveDir
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|rootDir
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|rootDir
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"testRestore"
argument_list|)
expr_stmt|;
name|archiveDir
operator|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
expr_stmt|;
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_CACHE_SIZE_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|conf
argument_list|,
name|rootDir
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
name|fs
operator|.
name|delete
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestore
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Test Rolling-Upgrade like Snapshot.
comment|// half machines writing using v1 and the others using v2 format.
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
name|rootDir
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
literal|"snapshot"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addRegionV1
argument_list|()
expr_stmt|;
name|builder
operator|.
name|addRegionV2
argument_list|()
expr_stmt|;
name|builder
operator|.
name|addRegionV2
argument_list|()
expr_stmt|;
name|builder
operator|.
name|addRegionV1
argument_list|()
expr_stmt|;
name|Path
name|snapshotDir
init|=
name|builder
operator|.
name|commit
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|builder
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|SnapshotDescription
name|desc
init|=
name|builder
operator|.
name|getSnapshotDescription
argument_list|()
decl_stmt|;
comment|// Test clone a snapshot
name|HTableDescriptor
name|htdClone
init|=
name|snapshotMock
operator|.
name|createHtd
argument_list|(
literal|"testtb-clone"
argument_list|)
decl_stmt|;
name|testRestore
argument_list|(
name|snapshotDir
argument_list|,
name|desc
argument_list|,
name|htdClone
argument_list|)
expr_stmt|;
name|verifyRestore
argument_list|(
name|rootDir
argument_list|,
name|htd
argument_list|,
name|htdClone
argument_list|)
expr_stmt|;
comment|// Test clone a clone ("link to link")
name|SnapshotDescription
name|cloneDesc
init|=
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"cloneSnapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|"testtb-clone"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Path
name|cloneDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|htdClone
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htdClone2
init|=
name|snapshotMock
operator|.
name|createHtd
argument_list|(
literal|"testtb-clone2"
argument_list|)
decl_stmt|;
name|testRestore
argument_list|(
name|cloneDir
argument_list|,
name|cloneDesc
argument_list|,
name|htdClone2
argument_list|)
expr_stmt|;
name|verifyRestore
argument_list|(
name|rootDir
argument_list|,
name|htd
argument_list|,
name|htdClone2
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyRestore
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|HTableDescriptor
name|sourceHtd
parameter_list|,
specifier|final
name|HTableDescriptor
name|htdClone
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|files
init|=
name|SnapshotTestingUtils
operator|.
name|listHFileNames
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|htdClone
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|files
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|length
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|String
name|linkFile
init|=
name|files
index|[
name|i
index|]
decl_stmt|;
name|String
name|refFile
init|=
name|files
index|[
name|i
operator|+
literal|1
index|]
decl_stmt|;
name|assertTrue
argument_list|(
name|linkFile
operator|+
literal|" should be a HFileLink"
argument_list|,
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|linkFile
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refFile
operator|+
literal|" should be a Referene"
argument_list|,
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|refFile
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sourceHtd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HFileLink
operator|.
name|getReferencedTableName
argument_list|(
name|linkFile
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|refPath
init|=
name|getReferredToFile
argument_list|(
name|refFile
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"get reference name for file "
operator|+
name|refFile
operator|+
literal|" = "
operator|+
name|refPath
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refPath
operator|.
name|getName
argument_list|()
operator|+
literal|" should be a HFileLink"
argument_list|,
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|refPath
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|linkFile
argument_list|,
name|refPath
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Execute the restore operation    * @param snapshotDir The snapshot directory to use as "restore source"    * @param sd The snapshot descriptor    * @param htdClone The HTableDescriptor of the table to restore/clone.    */
specifier|public
name|void
name|testRestore
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|,
specifier|final
name|SnapshotDescription
name|sd
parameter_list|,
specifier|final
name|HTableDescriptor
name|htdClone
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"pre-restore table="
operator|+
name|htdClone
operator|.
name|getTableName
argument_list|()
operator|+
literal|" snapshot="
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
operator|new
name|FSTableDescriptors
argument_list|(
name|conf
argument_list|)
operator|.
name|createTableDescriptor
argument_list|(
name|htdClone
argument_list|)
expr_stmt|;
name|RestoreSnapshotHelper
name|helper
init|=
name|getRestoreHelper
argument_list|(
name|rootDir
argument_list|,
name|snapshotDir
argument_list|,
name|sd
argument_list|,
name|htdClone
argument_list|)
decl_stmt|;
name|helper
operator|.
name|restoreHdfsRegions
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"post-restore table="
operator|+
name|htdClone
operator|.
name|getTableName
argument_list|()
operator|+
literal|" snapshot="
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize the restore helper, based on the snapshot and table information provided.    */
specifier|private
name|RestoreSnapshotHelper
name|getRestoreHelper
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|Path
name|snapshotDir
parameter_list|,
specifier|final
name|SnapshotDescription
name|sd
parameter_list|,
specifier|final
name|HTableDescriptor
name|htdClone
parameter_list|)
throws|throws
name|IOException
block|{
name|ForeignExceptionDispatcher
name|monitor
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
name|MonitoredTask
name|status
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MonitoredTask
operator|.
name|class
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|manifest
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
name|sd
argument_list|)
decl_stmt|;
return|return
operator|new
name|RestoreSnapshotHelper
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|,
name|htdClone
argument_list|,
name|rootDir
argument_list|,
name|monitor
argument_list|,
name|status
argument_list|)
return|;
block|}
specifier|private
name|Path
name|getReferredToFile
parameter_list|(
specifier|final
name|String
name|referenceName
parameter_list|)
block|{
name|Path
name|fakeBasePath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
literal|"table"
argument_list|,
literal|"region"
argument_list|)
argument_list|,
literal|"cf"
argument_list|)
decl_stmt|;
return|return
name|StoreFileInfo
operator|.
name|getReferredToFile
argument_list|(
operator|new
name|Path
argument_list|(
name|fakeBasePath
argument_list|,
name|referenceName
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

