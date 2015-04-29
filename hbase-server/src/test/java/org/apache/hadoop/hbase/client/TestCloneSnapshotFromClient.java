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
name|client
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
name|NamespaceDescriptor
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
name|NamespaceNotFoundException
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
name|snapshot
operator|.
name|SnapshotDoesNotExistException
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
name|ClientTests
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
comment|/**  * Test clone snapshots from the client  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCloneSnapshotFromClient
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCloneSnapshotFromClient
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
name|emptySnapshot
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
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|Admin
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
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10
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
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HIGH_PRIORITY_HANDLER_COUNT
argument_list|,
literal|40
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
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testtb-"
operator|+
name|tid
argument_list|)
expr_stmt|;
name|emptySnapshot
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"emptySnaptb-"
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
name|SnapshotTestingUtils
operator|.
name|createTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// take an empty snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|emptySnapshot
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// enable table and insert data
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|snapshot0Rows
operator|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName0
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// enable table and insert more data
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|snapshot1Rows
operator|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// take a snapshot of the updated table
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// re-enable table
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|int
name|getNumReplicas
parameter_list|()
block|{
return|return
literal|1
return|;
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
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|TEST_UTIL
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
argument_list|(
name|expected
operator|=
name|NamespaceNotFoundException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testCloneOnMissingNamespace
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
literal|"unknownNS:clonetb"
argument_list|)
decl_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName1
argument_list|,
name|clonedTableName
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
name|testCloneSnapshot
argument_list|(
name|clonedTableName
argument_list|,
name|emptySnapshot
argument_list|,
literal|0
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
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
name|snapshotRows
argument_list|)
expr_stmt|;
name|verifyReplicasCameOnline
argument_list|(
name|tableName
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
specifier|protected
name|void
name|verifyReplicasCameOnline
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|verifyReplicasCameOnline
argument_list|(
name|tableName
argument_list|,
name|admin
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloneSnapshotCrossNamespace
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|nsName
init|=
literal|"testCloneSnapshotCrossNamespace"
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|nsName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|nsName
argument_list|,
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
name|testCloneSnapshot
argument_list|(
name|clonedTableName
argument_list|,
name|emptySnapshot
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that tables created from the snapshot are still alive after source table deletion.    */
annotation|@
name|Test
specifier|public
name|void
name|testCloneLinksAfterDelete
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Clone a table from the first snapshot
name|TableName
name|clonedTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"clonedtb1-"
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
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
comment|// Take a snapshot of this cloned table.
name|admin
operator|.
name|disableTable
argument_list|(
name|clonedTableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName2
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
comment|// Clone the snapshot of the cloned table
name|TableName
name|clonedTableName2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"clonedtb2-"
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
name|snapshotName2
argument_list|,
name|clonedTableName2
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName2
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|clonedTableName2
argument_list|)
expr_stmt|;
comment|// Remove the original table
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|waitCleanerRun
argument_list|()
expr_stmt|;
comment|// Verify the first cloned table
name|admin
operator|.
name|enableTable
argument_list|(
name|clonedTableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
comment|// Verify the second cloned table
name|admin
operator|.
name|enableTable
argument_list|(
name|clonedTableName2
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName2
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|clonedTableName2
argument_list|)
expr_stmt|;
comment|// Delete the first cloned table
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|clonedTableName
argument_list|)
expr_stmt|;
name|waitCleanerRun
argument_list|()
expr_stmt|;
comment|// Verify the second cloned table
name|admin
operator|.
name|enableTable
argument_list|(
name|clonedTableName2
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName2
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
comment|// Clone a new table from cloned
name|TableName
name|clonedTableName3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"clonedtb3-"
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
name|snapshotName2
argument_list|,
name|clonedTableName3
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|clonedTableName3
argument_list|,
name|snapshot0Rows
argument_list|)
expr_stmt|;
comment|// Delete the cloned tables
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|clonedTableName2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|clonedTableName3
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName2
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Helpers
comment|// ==========================================================================
specifier|private
name|void
name|waitCleanerRun
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getHFileCleaner
argument_list|()
operator|.
name|choreForTesting
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

