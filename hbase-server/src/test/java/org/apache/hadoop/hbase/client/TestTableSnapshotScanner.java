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
name|mapreduce
operator|.
name|TestTableSnapshotInputFormat
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
name|TestTableSnapshotScanner
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
name|TestTableSnapshotInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_REGION_SERVERS
init|=
literal|2
decl_stmt|;
specifier|private
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
specifier|public
specifier|static
name|byte
index|[]
name|bbb
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|byte
index|[]
name|yyy
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"yyy"
argument_list|)
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_REGION_SERVERS
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
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|public
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
name|int
name|numRegions
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
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
name|bbb
argument_list|,
name|yyy
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
name|HBaseAdmin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// put some stuff in the table
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
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
comment|// load different values
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
comment|// cause flush to create new files in the region
name|admin
operator|.
name|flush
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithSingleRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testScanner
argument_list|(
name|UTIL
argument_list|,
literal|"testWithSingleRegion"
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
name|testWithMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testScanner
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithOfflineHBaseMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testScanner
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMultiRegion"
argument_list|,
literal|20
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testScanner
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
name|boolean
name|shutdownCluster
parameter_list|)
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
literal|"testScanner"
argument_list|)
decl_stmt|;
try|try
block|{
name|createTableAndSnapshot
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
if|if
condition|(
name|shutdownCluster
condition|)
block|{
name|util
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
block|}
name|Path
name|restoreDir
init|=
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|bbb
argument_list|,
name|yyy
argument_list|)
decl_stmt|;
comment|// limit the scan
name|TableSnapshotScanner
name|scanner
init|=
operator|new
name|TableSnapshotScanner
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|verifyScanner
argument_list|(
name|scanner
argument_list|,
name|bbb
argument_list|,
name|yyy
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|shutdownCluster
condition|)
block|{
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|util
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
block|}
specifier|private
name|void
name|verifyScanner
parameter_list|(
name|ResultScanner
name|scanner
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HBaseTestingUtility
operator|.
name|SeenRowTracker
name|rowTracker
init|=
operator|new
name|HBaseTestingUtility
operator|.
name|SeenRowTracker
argument_list|(
name|startRow
argument_list|,
name|stopRow
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|verifyRow
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|rowTracker
operator|.
name|addRow
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// validate all rows are seen
name|rowTracker
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|verifyRow
parameter_list|(
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
name|result
operator|.
name|getRow
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
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|FAMILIES
operator|.
name|length
condition|;
name|j
operator|++
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
name|FAMILIES
index|[
name|j
index|]
argument_list|,
literal|null
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
block|}
end_class

end_unit

