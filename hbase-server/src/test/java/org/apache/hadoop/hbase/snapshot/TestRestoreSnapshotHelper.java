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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|FileUtil
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|catalog
operator|.
name|CatalogTracker
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
name|TestRestoreSnapshotHelper
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
name|TEST_FAMILY
init|=
literal|"cf"
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
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
literal|"testtb"
argument_list|)
decl_stmt|;
name|Path
name|snapshotDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
literal|"snapshot"
argument_list|)
decl_stmt|;
name|createSnapshot
argument_list|(
name|rootDir
argument_list|,
name|snapshotDir
argument_list|,
name|htd
argument_list|)
expr_stmt|;
comment|// Test clone a snapshot
name|HTableDescriptor
name|htdClone
init|=
name|createTableDescriptor
argument_list|(
literal|"testtb-clone"
argument_list|)
decl_stmt|;
name|testRestore
argument_list|(
name|snapshotDir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
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
name|createTableDescriptor
argument_list|(
literal|"testtb-clone2"
argument_list|)
decl_stmt|;
name|testRestore
argument_list|(
name|cloneDir
argument_list|,
name|htdClone
operator|.
name|getTableName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
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
name|getHFiles
argument_list|(
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
literal|2
argument_list|,
name|files
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|files
index|[
literal|0
index|]
operator|+
literal|" should be a HFileLink"
argument_list|,
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|files
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|files
index|[
literal|1
index|]
operator|+
literal|" should be a Referene"
argument_list|,
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|files
index|[
literal|1
index|]
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
name|files
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TEST_HFILE
argument_list|,
name|HFileLink
operator|.
name|getReferencedHFileName
argument_list|(
name|files
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|refPath
init|=
name|getReferredToFile
argument_list|(
name|files
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
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
name|files
index|[
literal|0
index|]
argument_list|,
name|refPath
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Execute the restore operation    * @param snapshotDir The snapshot directory to use as "restore source"    * @param sourceTableName The name of the snapshotted table    * @param htdClone The HTableDescriptor of the table to restore/clone.    */
specifier|public
name|void
name|testRestore
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|,
specifier|final
name|String
name|sourceTableName
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
name|sourceTableName
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
name|String
name|sourceTableName
parameter_list|,
specifier|final
name|HTableDescriptor
name|htdClone
parameter_list|)
throws|throws
name|IOException
block|{
name|CatalogTracker
name|catalogTracker
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|CatalogTracker
operator|.
name|class
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDescriptor
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HTableDescriptor
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|SnapshotDescription
name|sd
init|=
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
name|sourceTableName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
operator|new
name|RestoreSnapshotHelper
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|sd
argument_list|,
name|snapshotDir
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
name|void
name|createSnapshot
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
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
comment|// First region, simple with one plain hfile.
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|r0fs
init|=
name|HRegionFileSystem
operator|.
name|createRegionOnFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|archiveDir
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|hri
argument_list|)
decl_stmt|;
name|Path
name|storeFile
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|TEST_HFILE
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|storeFile
argument_list|)
expr_stmt|;
name|r0fs
operator|.
name|commitStoreFile
argument_list|(
name|TEST_FAMILY
argument_list|,
name|storeFile
argument_list|)
expr_stmt|;
comment|// Second region, used to test the split case.
comment|// This region contains a reference to the hfile in the first region.
name|hri
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|HRegionFileSystem
name|r1fs
init|=
name|HRegionFileSystem
operator|.
name|createRegionOnFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|archiveDir
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|hri
argument_list|)
decl_stmt|;
name|storeFile
operator|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|TEST_HFILE
operator|+
literal|'.'
operator|+
name|r0fs
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|storeFile
argument_list|)
expr_stmt|;
name|r1fs
operator|.
name|commitStoreFile
argument_list|(
name|TEST_FAMILY
argument_list|,
name|storeFile
argument_list|)
expr_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|archiveDir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|FileUtil
operator|.
name|copy
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|htd
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
specifier|private
name|String
index|[]
name|getHFiles
parameter_list|(
specifier|final
name|Path
name|tableDir
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
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|regionDir
range|:
name|FSUtils
operator|.
name|getRegionDirs
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|)
control|)
block|{
for|for
control|(
name|Path
name|familyDir
range|:
name|FSUtils
operator|.
name|getFamilyDirs
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
control|)
block|{
for|for
control|(
name|FileStatus
name|file
range|:
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|familyDir
argument_list|)
control|)
block|{
name|files
operator|.
name|add
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|files
argument_list|)
expr_stmt|;
return|return
name|files
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|files
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

