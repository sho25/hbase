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
name|net
operator|.
name|URI
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
name|Arrays
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
name|Set
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
name|FSDataOutputStream
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
name|HBaseAdmin
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
name|HTable
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
name|hbase
operator|.
name|util
operator|.
name|Pair
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
comment|/**  * Test Export Snapshot Tool  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestExportSnapshot
block|{
specifier|private
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
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|emptySnapshotName
decl_stmt|;
specifier|private
name|byte
index|[]
name|snapshotName
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
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
comment|/**    * Create a table and take a snapshot of the table used by the export test.    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|long
name|tid
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testtb-"
operator|+
name|tid
argument_list|)
expr_stmt|;
name|snapshotName
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"snaptb0-"
operator|+
name|tid
argument_list|)
expr_stmt|;
name|emptySnapshotName
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"emptySnaptb0-"
operator|+
name|tid
argument_list|)
expr_stmt|;
comment|// create Table
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Take an empty snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|emptySnapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Add some rows
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
comment|// take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
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
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|TEST_UTIL
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verfy the result of getBalanceSplits() method.    * The result are groups of files, used as input list for the "export" mappers.    * All the groups should have similar amount of data.    *    * The input list is a pair of file path and length.    * The getBalanceSplits() function sort it by length,    * and assign to each group a file, going back and forth through the groups.    */
annotation|@
name|Test
specifier|public
name|void
name|testBalanceSplit
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create a list of files
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Long
argument_list|>
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<=
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|files
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|Path
argument_list|,
name|Long
argument_list|>
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file-"
operator|+
name|i
argument_list|)
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Create 5 groups (total size 210)
comment|//    group 0: 20, 11, 10,  1 (total size: 42)
comment|//    group 1: 19, 12,  9,  2 (total size: 42)
comment|//    group 2: 18, 13,  8,  3 (total size: 42)
comment|//    group 3: 17, 12,  7,  4 (total size: 42)
comment|//    group 4: 16, 11,  6,  5 (total size: 42)
name|List
argument_list|<
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
name|splits
init|=
name|ExportSnapshot
operator|.
name|getBalancedSplits
argument_list|(
name|files
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file-20"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-11"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-10"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-1"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-0"
argument_list|)
argument_list|)
argument_list|,
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file-19"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-12"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-9"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-2"
argument_list|)
argument_list|)
argument_list|,
name|splits
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file-18"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-13"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-8"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-3"
argument_list|)
argument_list|)
argument_list|,
name|splits
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file-17"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-14"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-7"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-4"
argument_list|)
argument_list|)
argument_list|,
name|splits
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file-16"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-15"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-6"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file-5"
argument_list|)
argument_list|)
argument_list|,
name|splits
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify if exported snapshot and copied files matches the original one.    */
annotation|@
name|Test
specifier|public
name|void
name|testExportFileSystemState
parameter_list|()
throws|throws
name|Exception
block|{
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmptyExportFileSystemState
parameter_list|()
throws|throws
name|Exception
block|{
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|emptySnapshotName
argument_list|,
literal|1
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tableWithRefsName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tableWithRefs"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|snapshotName
init|=
literal|"tableWithRefs"
decl_stmt|;
specifier|final
name|String
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|final
name|String
name|TEST_HFILE
init|=
literal|"abc"
decl_stmt|;
specifier|final
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
name|snapshotName
argument_list|)
operator|.
name|setTable
argument_list|(
name|tableWithRefsName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootDir
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|Path
name|archiveDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
comment|// First region, simple with one plain hfile.
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableWithRefsName
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
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|storeFile
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Test Data"
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
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
name|tableWithRefsName
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
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
operator|.
name|getNameAsString
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
name|out
operator|=
name|fs
operator|.
name|create
argument_list|(
name|storeFile
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Test Data"
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
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
name|tableWithRefsName
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
name|SnapshotDescriptionUtils
operator|.
name|writeSnapshotInfo
argument_list|(
name|sd
argument_list|,
name|snapshotDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|testExportFileSystemState
argument_list|(
name|tableWithRefsName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotName
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test ExportSnapshot    */
specifier|private
name|void
name|testExportFileSystemState
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|snapshotName
parameter_list|,
name|int
name|filesExpected
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|copyDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"export-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|URI
name|hdfsUri
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|copyDir
operator|.
name|toUri
argument_list|()
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|copyDir
operator|=
name|copyDir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
comment|// Export Snapshot
name|int
name|res
init|=
name|ExportSnapshot
operator|.
name|innerMain
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-snapshot"
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotName
argument_list|)
block|,
literal|"-copy-to"
block|,
name|copyDir
operator|.
name|toString
argument_list|()
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|res
argument_list|)
expr_stmt|;
comment|// Verify File-System state
name|FileStatus
index|[]
name|rootFiles
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|copyDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|filesExpected
argument_list|,
name|rootFiles
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|rootFiles
control|)
block|{
name|String
name|name
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|fileStatus
operator|.
name|isDir
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|name
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// compare the snapshot metadata and verify the hfiles
specifier|final
name|FileSystem
name|hdfs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hdfsUri
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|snapshotDir
init|=
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotName
argument_list|)
argument_list|)
decl_stmt|;
name|verifySnapshot
argument_list|(
name|hdfs
argument_list|,
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|snapshotDir
argument_list|)
argument_list|,
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|copyDir
argument_list|,
name|snapshotDir
argument_list|)
argument_list|)
expr_stmt|;
name|verifyArchive
argument_list|(
name|fs
argument_list|,
name|copyDir
argument_list|,
name|tableName
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotName
argument_list|)
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|hdfs
argument_list|,
name|snapshotDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// Remove the exported dir
name|fs
operator|.
name|delete
argument_list|(
name|copyDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/*    * verify if the snapshot folder on file-system 1 match the one on file-system 2    */
specifier|private
name|void
name|verifySnapshot
parameter_list|(
specifier|final
name|FileSystem
name|fs1
parameter_list|,
specifier|final
name|Path
name|root1
parameter_list|,
specifier|final
name|FileSystem
name|fs2
parameter_list|,
specifier|final
name|Path
name|root2
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|s
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|listFiles
argument_list|(
name|fs1
argument_list|,
name|root1
argument_list|,
name|root1
argument_list|)
argument_list|,
name|listFiles
argument_list|(
name|fs2
argument_list|,
name|root2
argument_list|,
name|root2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Verify if the files exists    */
specifier|private
name|void
name|verifyArchive
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|exportedSnapshot
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|,
name|snapshotName
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|exportedArchive
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|listFiles
argument_list|(
name|fs
argument_list|,
name|exportedArchive
argument_list|,
name|exportedArchive
argument_list|)
argument_list|)
expr_stmt|;
name|SnapshotReferenceUtil
operator|.
name|visitReferencedFiles
argument_list|(
name|fs
argument_list|,
name|exportedSnapshot
argument_list|,
operator|new
name|SnapshotReferenceUtil
operator|.
name|FileVisitor
argument_list|()
block|{
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|String
name|hfile
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyNonEmptyFile
argument_list|(
operator|new
name|Path
argument_list|(
name|exportedArchive
argument_list|,
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
literal|"./"
argument_list|)
argument_list|,
name|tableName
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|region
argument_list|,
operator|new
name|Path
argument_list|(
name|family
argument_list|,
name|hfile
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|recoveredEdits
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyNonEmptyFile
argument_list|(
operator|new
name|Path
argument_list|(
name|exportedSnapshot
argument_list|,
operator|new
name|Path
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|region
argument_list|,
name|logfile
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|logFile
parameter_list|(
specifier|final
name|String
name|server
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyNonEmptyFile
argument_list|(
operator|new
name|Path
argument_list|(
name|exportedSnapshot
argument_list|,
operator|new
name|Path
argument_list|(
name|server
argument_list|,
name|logfile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyNonEmptyFile
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
name|path
operator|+
literal|" should exists"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|path
operator|+
literal|" should not be empty"
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|listFiles
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|root
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|rootPrefix
init|=
name|root
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|list
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileStatus
name|fstat
range|:
name|list
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|fstat
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|fstat
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|files
operator|.
name|addAll
argument_list|(
name|listFiles
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|fstat
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|files
operator|.
name|add
argument_list|(
name|fstat
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|substring
argument_list|(
name|rootPrefix
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|files
return|;
block|}
block|}
end_class

end_unit

