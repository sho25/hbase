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
name|SnapshotType
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
name|snapshot
operator|.
name|RegionServerSnapshotManager
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
comment|/**  * Test clone/restore snapshots from the client  *  * TODO This is essentially a clone of TestRestoreSnapshotFromClient.  This is worth refactoring  * this because there will be a few more flavors of snapshots that need to run these tests.  */
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRestoreFlushSnapshotFromClient
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
name|TestRestoreFlushSnapshotFromClient
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
name|TestRestoreFlushSnapshotFromClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
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
specifier|protected
name|String
name|snapshotName0
decl_stmt|;
specifier|protected
name|String
name|snapshotName1
decl_stmt|;
specifier|protected
name|String
name|snapshotName2
decl_stmt|;
specifier|protected
name|int
name|snapshot0Rows
decl_stmt|;
specifier|protected
name|int
name|snapshot1Rows
decl_stmt|;
specifier|protected
name|TableName
name|tableName
decl_stmt|;
specifier|protected
name|Admin
name|admin
decl_stmt|;
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
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|UTIL
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
name|UTIL
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
name|UTIL
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
name|UTIL
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
name|UTIL
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|RegionServerSnapshotManager
operator|.
name|SNAPSHOT_TIMEOUT_MILLIS_KEY
argument_list|,
name|RegionServerSnapshotManager
operator|.
name|SNAPSHOT_TIMEOUT_MILLIS_DEFAULT
operator|*
literal|2
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
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|SnapshotTestingUtils
operator|.
name|createTable
argument_list|(
name|UTIL
argument_list|,
name|tableName
argument_list|,
name|FAMILY
argument_list|)
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
name|UTIL
operator|.
name|getAdmin
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
name|snapshotName0
operator|=
literal|"snaptb0-"
operator|+
name|tid
expr_stmt|;
name|snapshotName1
operator|=
literal|"snaptb1-"
operator|+
name|tid
expr_stmt|;
name|snapshotName2
operator|=
literal|"snaptb2-"
operator|+
name|tid
expr_stmt|;
comment|// create Table and disable it
name|createTable
argument_list|()
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|UTIL
argument_list|,
name|tableName
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|snapshot0Rows
operator|=
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
name|snapshotName0
argument_list|,
name|tableName
argument_list|,
name|SnapshotType
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
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|UTIL
argument_list|,
name|tableName
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|snapshot1Rows
operator|=
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
name|snapshotName1
argument_list|,
name|tableName
argument_list|,
name|SnapshotType
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
name|table
operator|.
name|close
argument_list|()
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
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|UTIL
operator|.
name|getAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|UTIL
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
name|UTIL
argument_list|,
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
name|UTIL
argument_list|,
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
name|UTIL
argument_list|,
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
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"random-table-"
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
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
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
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
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
name|UTIL
argument_list|,
name|tableName
argument_list|,
name|snapshotRows
argument_list|)
expr_stmt|;
name|UTIL
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
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
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
name|UTIL
argument_list|,
name|clonedTableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName2
argument_list|,
name|clonedTableName
argument_list|,
name|SnapshotType
operator|.
name|FLUSH
argument_list|)
expr_stmt|;
name|UTIL
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
name|UTIL
argument_list|,
name|clonedTableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|UTIL
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
name|logFSTree
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|logFileSystemState
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|verifyRowCount
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|util
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|long
name|expectedRows
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|expectedRows
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|int
name|countRows
parameter_list|(
specifier|final
name|Table
name|table
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
return|return
name|UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|,
name|families
argument_list|)
return|;
block|}
block|}
end_class

end_unit

