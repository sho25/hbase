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
name|mapreduce
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
name|Arrays
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
name|Cell
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
name|CellScanner
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
name|Result
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|HFileArchiveUtil
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
name|Test
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

begin_class
specifier|public
specifier|abstract
class|class
name|TableSnapshotInputFormatTestBase
block|{
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
name|TableSnapshotInputFormatTestBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NUM_REGION_SERVERS
init|=
literal|2
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f2"
argument_list|)
block|}
decl_stmt|;
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
name|Path
name|rootDir
decl_stmt|;
specifier|public
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|StartMiniClusterOption
name|option
init|=
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|numRegionServers
argument_list|(
name|NUM_REGION_SERVERS
argument_list|)
operator|.
name|numDataNodes
argument_list|(
name|NUM_REGION_SERVERS
argument_list|)
operator|.
name|createRootDir
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|option
argument_list|)
expr_stmt|;
name|rootDir
operator|=
name|UTIL
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
expr_stmt|;
name|fs
operator|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|tearDownCluster
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
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Enable snapshot
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
block|}
specifier|protected
specifier|abstract
name|void
name|testWithMockedMapReduce
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numSplitsPerRegion
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|,
name|boolean
name|setLocalityEnabledTo
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|protected
specifier|abstract
name|void
name|testWithMapReduceImpl
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|tableDir
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numSplitsPerRegion
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|,
name|boolean
name|shutdownCluster
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|protected
specifier|abstract
name|byte
index|[]
name|getStartRow
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|byte
index|[]
name|getEndRow
parameter_list|()
function_decl|;
annotation|@
name|Test
specifier|public
name|void
name|testWithMockedMapReduceSingleRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMockedMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMockedMapReduceSingleRegion"
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithMockedMapReduceMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMockedMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMockedMapReduceMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithMapReduceSingleRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMapReduceSingleRegion"
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithMapReduceMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMapReduceMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// run the MR job while HBase is offline
specifier|public
name|void
name|testWithMapReduceAndOfflineHBaseMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMapReduceAndOfflineHBaseMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Test that snapshot restore does not create back references in the HBase root dir.
annotation|@
name|Test
specifier|public
name|void
name|testRestoreSnapshotDoesNotCreateBackRefLinks
parameter_list|()
throws|throws
name|Exception
block|{
name|setupCluster
argument_list|()
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRestoreSnapshotDoesNotCreateBackRefLinks"
argument_list|)
decl_stmt|;
name|String
name|snapshotName
init|=
literal|"foo"
decl_stmt|;
try|try
block|{
name|createTableAndSnapshot
argument_list|(
name|UTIL
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|getStartRow
argument_list|()
argument_list|,
name|getEndRow
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Path
name|tmpTableDir
init|=
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|testRestoreSnapshotDoesNotCreateBackRefLinksInit
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|tmpTableDir
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
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
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|tableName
argument_list|)
argument_list|)
control|)
block|{
for|for
control|(
name|Path
name|storeDir
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
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|storeDir
argument_list|)
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|StoreFileInfo
operator|.
name|isValid
argument_list|(
name|status
argument_list|)
condition|)
block|{
name|Path
name|archiveStoreDir
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|regionDir
operator|.
name|getName
argument_list|()
argument_list|,
name|storeDir
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|HFileLink
operator|.
name|getBackReferencesDir
argument_list|(
name|storeDir
argument_list|,
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// assert back references directory is empty
name|assertFalse
argument_list|(
literal|"There is a back reference in "
operator|+
name|path
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|path
operator|=
name|HFileLink
operator|.
name|getBackReferencesDir
argument_list|(
name|archiveStoreDir
argument_list|,
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// assert back references directory is empty
name|assertFalse
argument_list|(
literal|"There is a back reference in "
operator|+
name|path
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
finally|finally
block|{
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tearDownCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|abstract
name|void
name|testRestoreSnapshotDoesNotCreateBackRefLinksInit
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|tmpTableDir
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|protected
name|void
name|testWithMapReduce
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|numSplitsPerRegion
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|,
name|boolean
name|shutdownCluster
parameter_list|)
throws|throws
name|Exception
block|{
name|setupCluster
argument_list|()
expr_stmt|;
try|try
block|{
name|Path
name|tableDir
init|=
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testWithMapReduce"
argument_list|)
decl_stmt|;
name|testWithMapReduceImpl
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|tableDir
argument_list|,
name|numRegions
argument_list|,
name|numSplitsPerRegion
argument_list|,
name|expectedNumSplits
argument_list|,
name|shutdownCluster
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tearDownCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|verifyRowFromMap
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|row
init|=
name|key
operator|.
name|get
argument_list|()
decl_stmt|;
name|CellScanner
name|scanner
init|=
name|result
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|Cell
name|cell
init|=
name|scanner
operator|.
name|current
argument_list|()
decl_stmt|;
comment|//assert that all Cells in the Result have the same key
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|byte
index|[]
name|family
range|:
name|FAMILIES
control|)
block|{
name|byte
index|[]
name|actual
init|=
name|result
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
literal|"Row in snapshot does not match, expected:"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
operator|+
literal|" ,actual:"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|actual
argument_list|)
argument_list|,
name|row
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|createTableAndSnapshot
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
name|int
name|numRegions
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ensuring table doesn't exist."
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"creating table '"
operator|+
name|tableName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
if|if
condition|(
name|numRegions
operator|>
literal|1
condition|)
block|{
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILIES
argument_list|,
literal|1
argument_list|,
name|startRow
argument_list|,
name|endRow
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
block|}
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"put some stuff in the table"
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|util
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"snapshot"
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|tableName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|FAMILIES
argument_list|)
argument_list|,
literal|null
argument_list|,
name|snapshotName
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"load different values"
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"after_snapshot_value"
argument_list|)
decl_stmt|;
name|util
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAMILIES
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"cause flush to create new files in the region"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

