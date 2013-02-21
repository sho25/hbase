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
name|LargeTests
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
name|client
operator|.
name|Put
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
name|MasterFileSystem
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
name|MD5Hash
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
comment|/**  * Test clone/restore snapshots from the client  *  * TODO This is essentially a clone of TestRestoreSnapshotFromClient.  This is worth refactoring  * this because there will be a few more flavors of snapshots that need to run these tests.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRestoreFlushSnapshotFromClient
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
name|snapshotName0
decl_stmt|;
specifier|private
name|byte
index|[]
name|snapshotName1
decl_stmt|;
specifier|private
name|byte
index|[]
name|snapshotName2
decl_stmt|;
specifier|private
name|int
name|snapshot0Rows
decl_stmt|;
specifier|private
name|int
name|snapshot1Rows
decl_stmt|;
specifier|private
name|byte
index|[]
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
literal|"hbase.online.schema.update.enable"
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
literal|"hbase.client.retries.number"
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
comment|// Enable snapshot
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
comment|/**    * Initialize the tests with a table filled with some data    * and two snapshots (snapshotName0, snapshotName1) of different states.    * The tableName, snapshotNames and the number of rows in the snapshot are initialized.    */
annotation|@
name|Before
specifier|public
name|void
name|setup
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testtb-"
operator|+
name|tid
argument_list|)
expr_stmt|;
name|snapshotName0
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
name|snapshotName1
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"snaptb1-"
operator|+
name|tid
argument_list|)
expr_stmt|;
name|snapshotName2
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"snaptb2-"
operator|+
name|tid
argument_list|)
expr_stmt|;
comment|// create Table and disable it
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
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
try|try
block|{
name|loadData
argument_list|(
name|table
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|snapshot0Rows
operator|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"=== before snapshot with 500 rows"
argument_list|)
expr_stmt|;
name|logFSTree
argument_list|()
expr_stmt|;
comment|// take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotName0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|SnapshotDescription
operator|.
name|Type
operator|.
name|FLUSH
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"=== after snapshot with 500 rows"
argument_list|)
expr_stmt|;
name|logFSTree
argument_list|()
expr_stmt|;
comment|// insert more data
name|loadData
argument_list|(
name|table
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|snapshot1Rows
operator|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"=== before snapshot with 1000 rows"
argument_list|)
expr_stmt|;
name|logFSTree
argument_list|()
expr_stmt|;
comment|// take a snapshot of the updated table
name|admin
operator|.
name|snapshot
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotName1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|SnapshotDescription
operator|.
name|Type
operator|.
name|FLUSH
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"=== after snapshot with 1000 rows"
argument_list|)
expr_stmt|;
name|logFSTree
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName0
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName1
argument_list|)
expr_stmt|;
comment|// Ensure the archiver to be empty
name|MasterFileSystem
name|mfs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|mfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTakeFlushSnapshot
parameter_list|()
throws|throws
name|IOException
block|{
comment|// taking happens in setup.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestoreSnapshot
parameter_list|()
throws|throws
name|IOException
block|{
name|verifyRowCount
argument_list|(
name|tableName
argument_list|,
name|snapshot1Rows
argument_list|)
expr_stmt|;
comment|// Restore from snapshot-0
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|restoreSnapshot
argument_list|(
name|snapshotName0
argument_list|)
expr_stmt|;
name|logFSTree
argument_list|()
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"=== after restore with 500 row snapshot"
argument_list|)
expr_stmt|;
name|logFSTree
argument_list|()
expr_stmt|;
name|verifyRowCount
argument_list|(
name|tableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
comment|// Restore from snapshot-1
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|restoreSnapshot
argument_list|(
name|snapshotName1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|verifyRowCount
argument_list|(
name|tableName
argument_list|,
name|snapshot1Rows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SnapshotDoesNotExistException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testCloneNonExistentSnapshot
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|snapshotName
init|=
literal|"random-snapshot-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
literal|"random-table-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloneSnapshot
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|byte
index|[]
name|clonedTableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"clonedtb-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|testCloneSnapshot
argument_list|(
name|clonedTableName
argument_list|,
name|snapshotName0
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|testCloneSnapshot
argument_list|(
name|clonedTableName
argument_list|,
name|snapshotName1
argument_list|,
name|snapshot1Rows
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testCloneSnapshot
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|snapshotName
parameter_list|,
name|int
name|snapshotRows
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// create a new table from snapshot
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|verifyRowCount
argument_list|(
name|tableName
argument_list|,
name|snapshotRows
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestoreSnapshotOfCloned
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|byte
index|[]
name|clonedTableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"clonedtb-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName0
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
name|verifyRowCount
argument_list|(
name|clonedTableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotName2
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|clonedTableName
argument_list|)
argument_list|,
name|SnapshotDescription
operator|.
name|Type
operator|.
name|FLUSH
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|clonedTableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName2
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
name|verifyRowCount
argument_list|(
name|clonedTableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|clonedTableName
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Helpers
comment|// ==========================================================================
specifier|private
name|void
name|createTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[
literal|16
index|]
index|[]
decl_stmt|;
name|byte
index|[]
name|hex
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"0123456789abcdef"
argument_list|)
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
literal|16
condition|;
operator|++
name|i
control|)
block|{
name|splitKeys
index|[
name|i
index|]
operator|=
operator|new
name|byte
index|[]
block|{
name|hex
index|[
name|i
index|]
block|}
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|loadData
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|,
name|int
name|rows
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|)
expr_stmt|;
while|while
condition|(
name|rows
operator|--
operator|>
literal|0
condition|)
block|{
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rows
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|MD5Hash
operator|.
name|getMD5AsHex
argument_list|(
name|value
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|put
operator|.
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|put
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|logFSTree
parameter_list|()
throws|throws
name|IOException
block|{
name|MasterFileSystem
name|mfs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|mfs
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyRowCount
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|long
name|expectedRows
parameter_list|)
throws|throws
name|IOException
block|{
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
name|assertEquals
argument_list|(
name|expectedRows
argument_list|,
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
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

