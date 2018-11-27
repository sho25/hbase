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
name|LocatedFileStatus
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
name|fs
operator|.
name|RemoteIterator
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
name|client
operator|.
name|TableDescriptor
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
name|MobUtils
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
name|CommonFSUtils
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
name|Before
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
name|mockito
operator|.
name|Mockito
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

begin_comment
comment|/**  * Test the restore/clone operation from a file-system point of view.  */
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
name|TestRestoreSnapshotHelper
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
name|TestRestoreSnapshotHelper
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
name|TestRestoreSnapshotHelper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|String
name|TEST_HFILE
init|=
literal|"abc"
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|Path
name|archiveDir
decl_stmt|;
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
name|Path
name|rootDir
decl_stmt|;
specifier|protected
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{   }
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownCluster
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
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|setupConf
argument_list|(
name|conf
argument_list|)
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
specifier|protected
name|SnapshotMock
name|createSnapshotMock
parameter_list|()
throws|throws
name|IOException
block|{
return|return
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
return|;
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
name|restoreAndVerify
argument_list|(
literal|"snapshot"
argument_list|,
literal|"testRestore"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestoreWithNamespace
parameter_list|()
throws|throws
name|IOException
block|{
name|restoreAndVerify
argument_list|(
literal|"snapshot"
argument_list|,
literal|"namespace1:testRestoreWithNamespace"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoHFileLinkInRootDir
parameter_list|()
throws|throws
name|IOException
block|{
name|rootDir
operator|=
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
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
name|fs
operator|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testNoHFileLinkInRootDir"
argument_list|)
decl_stmt|;
name|String
name|snapshotName
init|=
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"-snapshot"
decl_stmt|;
name|createTableAndSnapshot
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
name|Path
name|restoreDir
init|=
operator|new
name|Path
argument_list|(
literal|"/hbase/.tmp-restore"
argument_list|)
decl_stmt|;
name|RestoreSnapshotHelper
operator|.
name|copySnapshotForScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
name|checkNoHFileLinkInTableDir
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|createTableAndSnapshot
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|column
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|column
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|column
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkNoHFileLinkInTableDir
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
index|[]
name|tableDirs
init|=
operator|new
name|Path
index|[]
block|{
name|CommonFSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|tableName
argument_list|)
block|,
name|CommonFSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
argument_list|,
name|tableName
argument_list|)
block|,
name|CommonFSUtils
operator|.
name|getTableDir
argument_list|(
name|MobUtils
operator|.
name|getMobHome
argument_list|(
name|rootDir
argument_list|)
argument_list|,
name|tableName
argument_list|)
block|}
decl_stmt|;
for|for
control|(
name|Path
name|tableDir
range|:
name|tableDirs
control|)
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|hasHFileLink
argument_list|(
name|tableDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|hasHFileLink
parameter_list|(
name|Path
name|tableDir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
condition|)
block|{
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|iterator
init|=
name|fs
operator|.
name|listFiles
argument_list|(
name|tableDir
argument_list|,
literal|true
argument_list|)
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LocatedFileStatus
name|fileStatus
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileStatus
operator|.
name|isFile
argument_list|()
operator|&&
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|restoreAndVerify
parameter_list|(
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Test Rolling-Upgrade like Snapshot.
comment|// half machines writing using v1 and the others using v2 format.
name|SnapshotMock
name|snapshotMock
init|=
name|createSnapshotMock
argument_list|()
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
argument_list|,
name|tableName
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
name|TableDescriptor
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
name|TableDescriptor
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
name|TableDescriptor
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
name|TableDescriptor
name|sourceHtd
parameter_list|,
specifier|final
name|TableDescriptor
name|htdClone
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
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
name|size
argument_list|()
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
name|size
argument_list|()
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
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|refFile
init|=
name|files
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
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
specifier|private
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
name|TableDescriptor
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
name|TableDescriptor
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

