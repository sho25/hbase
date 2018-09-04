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
name|master
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
name|assertFalse
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|snapshot
operator|.
name|CorruptedSnapshotException
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
name|SnapshotDescriptionUtils
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
name|SnapshotReferenceUtil
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
comment|/**  * Test that the snapshot hfile cleaner finds hfiles referenced in a snapshot  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSnapshotHFileCleaner
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
name|TestSnapshotHFileCleaner
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
name|TestSnapshotFileCache
operator|.
name|class
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
specifier|static
specifier|final
name|String
name|TABLE_NAME_STR
init|=
literal|"testSnapshotManifest"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SNAPSHOT_NAME_STR
init|=
literal|"testSnapshotManifest-snapshot"
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootDir
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
comment|/**    * Setup the test environment    */
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|rootDir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanup
parameter_list|()
throws|throws
name|IOException
block|{
comment|// cleanup
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFindsSnapshotFilesWhenCleaning
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|conf
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|archivedHfileDir
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SnapshotHFileCleaner
name|cleaner
init|=
operator|new
name|SnapshotHFileCleaner
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// write an hfile to the snapshot directory
name|String
name|snapshotName
init|=
literal|"snapshot"
decl_stmt|;
name|byte
index|[]
name|snapshot
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotName
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|HRegionInfo
name|mockRegion
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Path
name|regionSnapshotDir
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|mockRegion
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|regionSnapshotDir
argument_list|,
literal|"family"
argument_list|)
decl_stmt|;
comment|// create a reference to a supposedly valid hfile
name|String
name|hfile
init|=
literal|"fd1e73e8a96c486090c5cec07b4894c4"
decl_stmt|;
name|Path
name|refFile
init|=
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
name|hfile
argument_list|)
decl_stmt|;
comment|// make sure the reference file exists
name|fs
operator|.
name|create
argument_list|(
name|refFile
argument_list|)
expr_stmt|;
comment|// create the hfile in the archive
name|fs
operator|.
name|mkdirs
argument_list|(
name|archivedHfileDir
argument_list|)
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|archivedHfileDir
argument_list|,
name|hfile
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure that the file isn't deletable
name|assertFalse
argument_list|(
name|cleaner
operator|.
name|isFileDeletable
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|refFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|SnapshotFiles
implements|implements
name|SnapshotFileCache
operator|.
name|SnapshotFileInspector
block|{
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|filesUnderSnapshot
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|files
operator|.
name|addAll
argument_list|(
name|SnapshotReferenceUtil
operator|.
name|getHFileNames
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
block|}
comment|/**    * If there is a corrupted region manifest, it should throw out CorruptedSnapshotException,    * instead of an IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testCorruptedRegionManifest
parameter_list|()
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotTestingUtils
operator|.
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
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
name|SNAPSHOT_NAME_STR
argument_list|,
name|TABLE_NAME_STR
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addRegionV2
argument_list|()
expr_stmt|;
name|builder
operator|.
name|corruptOneRegionManifest
argument_list|()
expr_stmt|;
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|cache
operator|.
name|getSnapshotsInProgress
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CorruptedSnapshotException
name|cse
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected exception "
operator|+
name|cse
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|fs
operator|.
name|delete
argument_list|(
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|rootDir
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * If there is a corrupted data manifest, it should throw out CorruptedSnapshotException,    * instead of an IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testCorruptedDataManifest
parameter_list|()
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotTestingUtils
operator|.
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
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
name|SNAPSHOT_NAME_STR
argument_list|,
name|TABLE_NAME_STR
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addRegionV2
argument_list|()
expr_stmt|;
comment|// consolidate to generate a data.manifest file
name|builder
operator|.
name|consolidate
argument_list|()
expr_stmt|;
name|builder
operator|.
name|corruptDataManifest
argument_list|()
expr_stmt|;
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|cache
operator|.
name|getSnapshotsInProgress
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CorruptedSnapshotException
name|cse
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected exception "
operator|+
name|cse
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|fs
operator|.
name|delete
argument_list|(
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|rootDir
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**   * HBASE-16464   */
annotation|@
name|Test
specifier|public
name|void
name|testMissedTmpSnapshot
parameter_list|()
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
name|snapshotMock
init|=
operator|new
name|SnapshotTestingUtils
operator|.
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
name|SnapshotTestingUtils
operator|.
name|SnapshotMock
operator|.
name|SnapshotBuilder
name|builder
init|=
name|snapshotMock
operator|.
name|createSnapshotV2
argument_list|(
name|SNAPSHOT_NAME_STR
argument_list|,
name|TABLE_NAME_STR
argument_list|)
decl_stmt|;
name|builder
operator|.
name|addRegionV2
argument_list|()
expr_stmt|;
name|builder
operator|.
name|missOneRegionSnapshotFile
argument_list|()
expr_stmt|;
name|long
name|period
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SnapshotFileCache
name|cache
init|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|period
argument_list|,
literal|10000000
argument_list|,
literal|"test-snapshot-file-cache-refresh"
argument_list|,
operator|new
name|SnapshotFiles
argument_list|()
argument_list|)
decl_stmt|;
name|cache
operator|.
name|getSnapshotsInProgress
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|builder
operator|.
name|getSnapshotsDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

