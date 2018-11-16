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
name|ArrayList
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
name|MiniHBaseCluster
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
name|impl
operator|.
name|BackupAdminImpl
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
name|regionserver
operator|.
name|HRegion
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|EnvironmentEdgeManager
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
name|hbase
operator|.
name|thirdparty
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
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestIncrementalBackup
extends|extends
name|TestBackupBase
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
name|TestIncrementalBackup
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
name|TestIncrementalBackup
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|provider
operator|=
literal|"multiwal"
expr_stmt|;
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|Boolean
operator|.
name|TRUE
block|}
argument_list|)
expr_stmt|;
return|return
name|params
return|;
block|}
specifier|public
name|TestIncrementalBackup
parameter_list|(
name|Boolean
name|b
parameter_list|)
block|{   }
comment|// implement all test cases in 1 test since incremental
comment|// backup/restore has dependencies
annotation|@
name|Test
specifier|public
name|void
name|TestIncBackupRestore
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|ADD_ROWS
init|=
literal|99
decl_stmt|;
comment|// #1 - create full backup for all tables
name|LOG
operator|.
name|info
argument_list|(
literal|"create full backup image for all tables"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table1
argument_list|,
name|table2
argument_list|)
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
name|mobName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mob"
argument_list|)
decl_stmt|;
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
name|HColumnDescriptor
name|mobHcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|mobName
argument_list|)
decl_stmt|;
name|mobHcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mobHcd
operator|.
name|setMobThreshold
argument_list|(
literal|5L
argument_list|)
expr_stmt|;
name|table1Desc
operator|.
name|addFamily
argument_list|(
name|mobHcd
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
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
init|)
block|{
name|int
name|NB_ROWS_FAM3
init|=
literal|6
decl_stmt|;
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|fam3Name
argument_list|,
literal|3
argument_list|,
name|NB_ROWS_FAM3
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|mobName
argument_list|,
literal|3
argument_list|,
name|NB_ROWS_FAM3
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
name|admin
operator|=
operator|(
name|HBaseAdmin
operator|)
name|conn
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|BackupAdminImpl
name|client
init|=
operator|new
name|BackupAdminImpl
argument_list|(
name|conn
argument_list|)
decl_stmt|;
name|BackupRequest
name|request
init|=
name|createBackupRequest
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
decl_stmt|;
name|String
name|backupIdFull
init|=
name|client
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupIdFull
argument_list|)
argument_list|)
expr_stmt|;
comment|// #2 - insert some data to table
name|HTable
name|t1
init|=
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|famName
argument_list|,
literal|1
argument_list|,
name|ADD_ROWS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"writing "
operator|+
name|ADD_ROWS
operator|+
literal|" rows to "
operator|+
name|table1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HBaseTestingUtility
operator|.
name|countRows
argument_list|(
name|t1
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
name|ADD_ROWS
operator|+
name|NB_ROWS_FAM3
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"written "
operator|+
name|ADD_ROWS
operator|+
literal|" rows to "
operator|+
name|table1
argument_list|)
expr_stmt|;
comment|// additionally, insert rows to MOB cf
name|int
name|NB_ROWS_MOB
init|=
literal|111
decl_stmt|;
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|mobName
argument_list|,
literal|3
argument_list|,
name|NB_ROWS_MOB
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"written "
operator|+
name|NB_ROWS_MOB
operator|+
literal|" rows to "
operator|+
name|table1
operator|+
literal|" to Mob enabled CF"
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HBaseTestingUtility
operator|.
name|countRows
argument_list|(
name|t1
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
name|ADD_ROWS
operator|+
name|NB_ROWS_MOB
argument_list|)
expr_stmt|;
name|HTable
name|t2
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table2
argument_list|)
decl_stmt|;
name|Put
name|p2
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|p2
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-t2"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p2
operator|.
name|addColumn
argument_list|(
name|famName
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
name|t2
operator|.
name|put
argument_list|(
name|p2
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NB_ROWS_IN_BATCH
operator|+
literal|5
argument_list|,
name|HBaseTestingUtility
operator|.
name|countRows
argument_list|(
name|t2
argument_list|)
argument_list|)
expr_stmt|;
name|t2
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"written "
operator|+
literal|5
operator|+
literal|" rows to "
operator|+
name|table2
argument_list|)
expr_stmt|;
comment|// split table1
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|cluster
operator|.
name|getRegions
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|name
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|long
name|startSplitTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|splitRegion
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// although split fail, this may not affect following check in current API,
comment|// exception will be thrown.
name|LOG
operator|.
name|debug
argument_list|(
literal|"region is not splittable, because "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|table1
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|long
name|endSplitTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// split finished
name|LOG
operator|.
name|debug
argument_list|(
literal|"split finished in ="
operator|+
operator|(
name|endSplitTime
operator|-
name|startSplitTime
operator|)
argument_list|)
expr_stmt|;
comment|// #3 - incremental backup for multiple tables
name|tables
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|table1
argument_list|,
name|table2
argument_list|)
expr_stmt|;
name|request
operator|=
name|createBackupRequest
argument_list|(
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
expr_stmt|;
name|String
name|backupIdIncMultiple
init|=
name|client
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupIdIncMultiple
argument_list|)
argument_list|)
expr_stmt|;
comment|// add column family f2 to table1
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
name|table1Desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam2Name
argument_list|)
argument_list|)
expr_stmt|;
comment|// drop column family f3
name|table1Desc
operator|.
name|removeFamily
argument_list|(
name|fam3Name
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
name|int
name|NB_ROWS_FAM2
init|=
literal|7
decl_stmt|;
name|HTable
name|t3
init|=
name|insertIntoTable
argument_list|(
name|conn
argument_list|,
name|table1
argument_list|,
name|fam2Name
argument_list|,
literal|2
argument_list|,
name|NB_ROWS_FAM2
argument_list|)
decl_stmt|;
name|t3
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Wait for 5 sec to make sure that old WALs were deleted
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// #4 - additional incremental backup for multiple tables
name|request
operator|=
name|createBackupRequest
argument_list|(
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
expr_stmt|;
name|String
name|backupIdIncMultiple2
init|=
name|client
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|checkSucceeded
argument_list|(
name|backupIdIncMultiple2
argument_list|)
argument_list|)
expr_stmt|;
comment|// #5 - restore full backup for all tables
name|TableName
index|[]
name|tablesRestoreFull
init|=
operator|new
name|TableName
index|[]
block|{
name|table1
block|,
name|table2
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
block|,
name|table2_restore
block|}
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Restoring full "
operator|+
name|backupIdFull
argument_list|)
expr_stmt|;
name|client
operator|.
name|restore
argument_list|(
name|BackupUtils
operator|.
name|createRestoreRequest
argument_list|(
name|BACKUP_ROOT_DIR
argument_list|,
name|backupIdFull
argument_list|,
literal|false
argument_list|,
name|tablesRestoreFull
argument_list|,
name|tablesMapFull
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// #6.1 - check tables for full restore
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
name|assertTrue
argument_list|(
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|table2_restore
argument_list|)
argument_list|)
expr_stmt|;
name|hAdmin
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// #6.2 - checking row count of tables for full restore
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
name|HBaseTestingUtility
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|)
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
name|NB_ROWS_FAM3
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|hTable
operator|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table2_restore
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NB_ROWS_IN_BATCH
argument_list|,
name|HBaseTestingUtility
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// #7 - restore incremental backup for multiple tables, with overwrite
name|TableName
index|[]
name|tablesRestoreIncMultiple
init|=
operator|new
name|TableName
index|[]
block|{
name|table1
block|,
name|table2
block|}
decl_stmt|;
name|TableName
index|[]
name|tablesMapIncMultiple
init|=
operator|new
name|TableName
index|[]
block|{
name|table1_restore
block|,
name|table2_restore
block|}
decl_stmt|;
name|client
operator|.
name|restore
argument_list|(
name|BackupUtils
operator|.
name|createRestoreRequest
argument_list|(
name|BACKUP_ROOT_DIR
argument_list|,
name|backupIdIncMultiple2
argument_list|,
literal|false
argument_list|,
name|tablesRestoreIncMultiple
argument_list|,
name|tablesMapIncMultiple
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table1_restore
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"After incremental restore: "
operator|+
name|hTable
operator|.
name|getDescriptor
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|countFamName
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|,
name|famName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"f1 has "
operator|+
name|countFamName
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|countFamName
argument_list|,
name|NB_ROWS_IN_BATCH
operator|+
name|ADD_ROWS
argument_list|)
expr_stmt|;
name|int
name|countFam2Name
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|,
name|fam2Name
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"f2 has "
operator|+
name|countFam2Name
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|countFam2Name
argument_list|,
name|NB_ROWS_FAM2
argument_list|)
expr_stmt|;
name|int
name|countMobName
init|=
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|,
name|mobName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"mob has "
operator|+
name|countMobName
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|countMobName
argument_list|,
name|NB_ROWS_MOB
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|hTable
operator|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table2_restore
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NB_ROWS_IN_BATCH
operator|+
literal|5
argument_list|,
name|HBaseTestingUtility
operator|.
name|countRows
argument_list|(
name|hTable
argument_list|)
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

