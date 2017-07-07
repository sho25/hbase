begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|backup
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
name|concurrent
operator|.
name|CountDownLatch
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
name|backup
operator|.
name|util
operator|.
name|BackupUtils
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
name|Connection
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
name|ConnectionFactory
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
name|snapshot
operator|.
name|MobSnapshotTestingUtils
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

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
name|TestRemoteBackup
extends|extends
name|TestBackupBase
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
name|TestRemoteBackup
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|useSecondCluster
operator|=
literal|true
expr_stmt|;
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that a remote full backup is created on a single table with data correctly.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFullBackupRemote
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"test remote full backup on a single table"
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NB_ROWS_IN_FAM3
init|=
literal|6
decl_stmt|;
specifier|final
name|byte
index|[]
name|fam3Name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f3"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|fam2Name
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f2"
argument_list|)
decl_stmt|;
specifier|final
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{         }
try|try
block|{
name|HTable
name|t1
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|Put
name|p1
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
name|NB_ROWS_IN_FAM3
condition|;
name|i
operator|++
control|)
block|{
name|p1
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-t1"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p1
operator|.
name|addColumn
argument_list|(
name|fam3Name
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p1
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Wrote "
operator|+
name|NB_ROWS_IN_FAM3
operator|+
literal|" rows into family3"
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|table1Desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam3Name
argument_list|)
argument_list|)
expr_stmt|;
comment|// family 2 is MOB enabled
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|fam2Name
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
name|table1Desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|modifyTableSync
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
argument_list|,
name|table1Desc
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|TEST_UTIL
argument_list|,
name|table1
argument_list|,
literal|50
argument_list|,
name|fam2Name
argument_list|)
expr_stmt|;
name|HTable
name|t1
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|int
name|rows0
init|=
name|MobSnapshotTestingUtils
operator|.
name|countMobRows
argument_list|(
name|t1
argument_list|,
name|fam2Name
argument_list|)
decl_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|String
name|backupId
init|=
name|backupTables
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table1
argument_list|)
argument_list|,
name|BACKUP_REMOTE_ROOT_DIR
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupId
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"backup complete "
operator|+
name|backupId
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|t1
argument_list|,
name|famName
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
argument_list|)
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|t1
argument_list|,
name|fam3Name
argument_list|)
argument_list|,
name|NB_ROWS_IN_FAM3
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|TableName
index|[]
name|tablesRestoreFull
init|=
operator|new
name|TableName
index|[]
block|{
name|table1
block|}
decl_stmt|;
name|TableName
index|[]
name|tablesMapFull
init|=
operator|new
name|TableName
index|[]
block|{
name|table1_restore
block|}
decl_stmt|;
name|BackupAdmin
name|client
init|=
name|getBackupAdmin
argument_list|()
decl_stmt|;
name|client
operator|.
name|restore
argument_list|(
name|BackupUtils
operator|.
name|createRestoreRequest
argument_list|(
name|BACKUP_REMOTE_ROOT_DIR
argument_list|,
name|backupId
argument_list|,
literal|false
argument_list|,
name|tablesRestoreFull
argument_list|,
name|tablesMapFull
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// check tables for full restore
name|HBaseAdmin
name|hAdmin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|table1_restore
argument_list|)
argument_list|)
expr_stmt|;
comment|// #5.2 - checking row count of tables for full restore
name|HTable
name|hTable
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1_restore
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|,
name|famName
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
argument_list|)
expr_stmt|;
name|int
name|cnt3
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|,
name|fam3Name
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|cnt3
operator|>=
literal|0
operator|&&
name|cnt3
operator|<=
name|NB_ROWS_IN_FAM3
argument_list|)
expr_stmt|;
name|int
name|rows1
init|=
name|MobSnapshotTestingUtils
operator|.
name|countMobRows
argument_list|(
name|t1
argument_list|,
name|fam2Name
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rows0
argument_list|,
name|rows1
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|hAdmin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

