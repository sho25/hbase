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
name|CellUtil
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
name|ResultScanner
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
name|Scan
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
name|mapreduce
operator|.
name|SyncTable
operator|.
name|SyncMapper
operator|.
name|Counter
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
name|mapreduce
operator|.
name|Counters
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
name|Assert
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
import|;
end_import

begin_comment
comment|/**  * Basic test for the SyncTable M/R tool  */
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
name|TestSyncTable
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
name|TestSyncTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|generateSplits
parameter_list|(
name|int
name|numRows
parameter_list|,
name|int
name|numRegions
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|splitRows
init|=
operator|new
name|byte
index|[
name|numRegions
operator|-
literal|1
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numRegions
condition|;
name|i
operator|++
control|)
block|{
name|splitRows
index|[
name|i
operator|-
literal|1
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|numRows
operator|*
name|i
operator|/
name|numRegions
argument_list|)
expr_stmt|;
block|}
return|return
name|splitRows
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSyncTable
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|sourceTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testSourceTable"
argument_list|)
decl_stmt|;
name|TableName
name|targetTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testTargetTable"
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testSyncTable"
argument_list|)
decl_stmt|;
name|writeTestData
argument_list|(
name|sourceTableName
argument_list|,
name|targetTableName
argument_list|)
expr_stmt|;
name|hashSourceTable
argument_list|(
name|sourceTableName
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
name|Counters
name|syncCounters
init|=
name|syncTables
argument_list|(
name|sourceTableName
argument_list|,
name|targetTableName
argument_list|,
name|testDir
argument_list|)
decl_stmt|;
name|assertEqualTables
argument_list|(
literal|90
argument_list|,
name|sourceTableName
argument_list|,
name|targetTableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|syncCounters
operator|.
name|findCounter
argument_list|(
name|Counter
operator|.
name|ROWSWITHDIFFS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|syncCounters
operator|.
name|findCounter
argument_list|(
name|Counter
operator|.
name|SOURCEMISSINGROWS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|syncCounters
operator|.
name|findCounter
argument_list|(
name|Counter
operator|.
name|TARGETMISSINGROWS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|syncCounters
operator|.
name|findCounter
argument_list|(
name|Counter
operator|.
name|SOURCEMISSINGCELLS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|syncCounters
operator|.
name|findCounter
argument_list|(
name|Counter
operator|.
name|TARGETMISSINGCELLS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
argument_list|,
name|syncCounters
operator|.
name|findCounter
argument_list|(
name|Counter
operator|.
name|DIFFERENTCELLVALUES
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|sourceTableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|targetTableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|cleanupDataTestDirOnTestFS
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|assertEqualTables
parameter_list|(
name|int
name|expectedRows
parameter_list|,
name|TableName
name|sourceTableName
parameter_list|,
name|TableName
name|targetTableName
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|sourceTable
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|sourceTableName
argument_list|)
decl_stmt|;
name|Table
name|targetTable
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|targetTableName
argument_list|)
decl_stmt|;
name|ResultScanner
name|sourceScanner
init|=
name|sourceTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|ResultScanner
name|targetScanner
init|=
name|targetTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
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
name|expectedRows
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|sourceRow
init|=
name|sourceScanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|Result
name|targetRow
init|=
name|targetScanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"SOURCE row: "
operator|+
operator|(
name|sourceRow
operator|==
literal|null
condition|?
literal|"null"
else|:
name|Bytes
operator|.
name|toInt
argument_list|(
name|sourceRow
operator|.
name|getRow
argument_list|()
argument_list|)
operator|)
operator|+
literal|" cells:"
operator|+
name|sourceRow
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"TARGET row: "
operator|+
operator|(
name|targetRow
operator|==
literal|null
condition|?
literal|"null"
else|:
name|Bytes
operator|.
name|toInt
argument_list|(
name|targetRow
operator|.
name|getRow
argument_list|()
argument_list|)
operator|)
operator|+
literal|" cells:"
operator|+
name|targetRow
argument_list|)
expr_stmt|;
if|if
condition|(
name|sourceRow
operator|==
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Expected "
operator|+
name|expectedRows
operator|+
literal|" source rows but only found "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|targetRow
operator|==
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Expected "
operator|+
name|expectedRows
operator|+
literal|" target rows but only found "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|Cell
index|[]
name|sourceCells
init|=
name|sourceRow
operator|.
name|rawCells
argument_list|()
decl_stmt|;
name|Cell
index|[]
name|targetCells
init|=
name|targetRow
operator|.
name|rawCells
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceCells
operator|.
name|length
operator|!=
name|targetCells
operator|.
name|length
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Source cells: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|sourceCells
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Target cells: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|targetCells
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Row "
operator|+
name|Bytes
operator|.
name|toInt
argument_list|(
name|sourceRow
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|" has "
operator|+
name|sourceCells
operator|.
name|length
operator|+
literal|" cells in source table but "
operator|+
name|targetCells
operator|.
name|length
operator|+
literal|" cells in target table"
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
name|sourceCells
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Cell
name|sourceCell
init|=
name|sourceCells
index|[
name|j
index|]
decl_stmt|;
name|Cell
name|targetCell
init|=
name|targetCells
index|[
name|j
index|]
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|sourceCell
argument_list|,
name|targetCell
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Rows don't match"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|sourceCell
argument_list|,
name|targetCell
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Families don't match"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|sourceCell
argument_list|,
name|targetCell
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Qualifiers don't match"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingTimestamp
argument_list|(
name|sourceCell
argument_list|,
name|targetCell
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Timestamps don't match"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|sourceCell
argument_list|,
name|targetCell
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Values don't match"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Source cell: "
operator|+
name|sourceCell
operator|+
literal|" target cell: "
operator|+
name|targetCell
argument_list|)
expr_stmt|;
name|Throwables
operator|.
name|propagate
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Result
name|sourceRow
init|=
name|sourceScanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceRow
operator|!=
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Source table has more than "
operator|+
name|expectedRows
operator|+
literal|" rows.  Next row: "
operator|+
name|Bytes
operator|.
name|toInt
argument_list|(
name|sourceRow
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Result
name|targetRow
init|=
name|targetScanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|targetRow
operator|!=
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Target table has more than "
operator|+
name|expectedRows
operator|+
literal|" rows.  Next row: "
operator|+
name|Bytes
operator|.
name|toInt
argument_list|(
name|targetRow
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sourceScanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|targetScanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|sourceTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|targetTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Counters
name|syncTables
parameter_list|(
name|TableName
name|sourceTableName
parameter_list|,
name|TableName
name|targetTableName
parameter_list|,
name|Path
name|testDir
parameter_list|)
throws|throws
name|Exception
block|{
name|SyncTable
name|syncTable
init|=
operator|new
name|SyncTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|code
init|=
name|syncTable
operator|.
name|run
argument_list|(
operator|new
name|String
index|[]
block|{
name|testDir
operator|.
name|toString
argument_list|()
block|,
name|sourceTableName
operator|.
name|getNameAsString
argument_list|()
block|,
name|targetTableName
operator|.
name|getNameAsString
argument_list|()
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"sync table job failed"
argument_list|,
literal|0
argument_list|,
name|code
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sync tables completed"
argument_list|)
expr_stmt|;
return|return
name|syncTable
operator|.
name|counters
return|;
block|}
specifier|private
name|void
name|hashSourceTable
parameter_list|(
name|TableName
name|sourceTableName
parameter_list|,
name|Path
name|testDir
parameter_list|)
throws|throws
name|Exception
throws|,
name|IOException
block|{
name|int
name|numHashFiles
init|=
literal|3
decl_stmt|;
name|long
name|batchSize
init|=
literal|100
decl_stmt|;
comment|// should be 2 batches per region
name|int
name|scanBatch
init|=
literal|1
decl_stmt|;
name|HashTable
name|hashTable
init|=
operator|new
name|HashTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|code
init|=
name|hashTable
operator|.
name|run
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"--batchsize="
operator|+
name|batchSize
block|,
literal|"--numhashfiles="
operator|+
name|numHashFiles
block|,
literal|"--scanbatch="
operator|+
name|scanBatch
block|,
name|sourceTableName
operator|.
name|getNameAsString
argument_list|()
block|,
name|testDir
operator|.
name|toString
argument_list|()
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hash table job failed"
argument_list|,
literal|0
argument_list|,
name|code
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|HashTable
operator|.
name|TableHash
name|tableHash
init|=
name|HashTable
operator|.
name|TableHash
operator|.
name|read
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
argument_list|,
name|testDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|sourceTableName
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|tableHash
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|batchSize
argument_list|,
name|tableHash
operator|.
name|batchSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numHashFiles
argument_list|,
name|tableHash
operator|.
name|numHashFiles
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numHashFiles
operator|-
literal|1
argument_list|,
name|tableHash
operator|.
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Hash table completed"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeTestData
parameter_list|(
name|TableName
name|sourceTableName
parameter_list|,
name|TableName
name|targetTableName
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|column1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|column2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val3"
argument_list|)
decl_stmt|;
name|int
name|numRows
init|=
literal|100
decl_stmt|;
name|int
name|sourceRegions
init|=
literal|10
decl_stmt|;
name|int
name|targetRegions
init|=
literal|6
decl_stmt|;
name|Table
name|sourceTable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|sourceTableName
argument_list|,
name|family
argument_list|,
name|generateSplits
argument_list|(
name|numRows
argument_list|,
name|sourceRegions
argument_list|)
argument_list|)
decl_stmt|;
name|Table
name|targetTable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|targetTableName
argument_list|,
name|family
argument_list|,
name|generateSplits
argument_list|(
name|numRows
argument_list|,
name|targetRegions
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|timestamp
init|=
literal|1430764183454L
decl_stmt|;
name|int
name|rowIndex
init|=
literal|0
decl_stmt|;
comment|// a bunch of identical rows
for|for
control|(
init|;
name|rowIndex
operator|<
literal|40
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|sourcePut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|sourceTable
operator|.
name|put
argument_list|(
name|sourcePut
argument_list|)
expr_stmt|;
name|Put
name|targetPut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|targetTable
operator|.
name|put
argument_list|(
name|targetPut
argument_list|)
expr_stmt|;
block|}
comment|// some rows only in the source table
comment|// ROWSWITHDIFFS: 10
comment|// TARGETMISSINGROWS: 10
comment|// TARGETMISSINGCELLS: 20
for|for
control|(
init|;
name|rowIndex
operator|<
literal|50
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|sourceTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
comment|// some rows only in the target table
comment|// ROWSWITHDIFFS: 10
comment|// SOURCEMISSINGROWS: 10
comment|// SOURCEMISSINGCELLS: 20
for|for
control|(
init|;
name|rowIndex
operator|<
literal|60
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|targetTable
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
comment|// some rows with 1 missing cell in target table
comment|// ROWSWITHDIFFS: 10
comment|// TARGETMISSINGCELLS: 10
for|for
control|(
init|;
name|rowIndex
operator|<
literal|70
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|sourcePut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|sourceTable
operator|.
name|put
argument_list|(
name|sourcePut
argument_list|)
expr_stmt|;
name|Put
name|targetPut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|targetTable
operator|.
name|put
argument_list|(
name|targetPut
argument_list|)
expr_stmt|;
block|}
comment|// some rows with 1 missing cell in source table
comment|// ROWSWITHDIFFS: 10
comment|// SOURCEMISSINGCELLS: 10
for|for
control|(
init|;
name|rowIndex
operator|<
literal|80
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|sourcePut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|sourceTable
operator|.
name|put
argument_list|(
name|sourcePut
argument_list|)
expr_stmt|;
name|Put
name|targetPut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|targetTable
operator|.
name|put
argument_list|(
name|targetPut
argument_list|)
expr_stmt|;
block|}
comment|// some rows differing only in timestamp
comment|// ROWSWITHDIFFS: 10
comment|// SOURCEMISSINGCELLS: 20
comment|// TARGETMISSINGCELLS: 20
for|for
control|(
init|;
name|rowIndex
operator|<
literal|90
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|sourcePut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|column1
argument_list|)
expr_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|sourceTable
operator|.
name|put
argument_list|(
name|sourcePut
argument_list|)
expr_stmt|;
name|Put
name|targetPut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
operator|+
literal|1
argument_list|,
name|column1
argument_list|)
expr_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
operator|-
literal|1
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|targetTable
operator|.
name|put
argument_list|(
name|targetPut
argument_list|)
expr_stmt|;
block|}
comment|// some rows with different values
comment|// ROWSWITHDIFFS: 10
comment|// DIFFERENTCELLVALUES: 20
for|for
control|(
init|;
name|rowIndex
operator|<
name|numRows
condition|;
name|rowIndex
operator|++
control|)
block|{
name|Put
name|sourcePut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|sourcePut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|sourceTable
operator|.
name|put
argument_list|(
name|sourcePut
argument_list|)
expr_stmt|;
name|Put
name|targetPut
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
decl_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column1
argument_list|,
name|timestamp
argument_list|,
name|value3
argument_list|)
expr_stmt|;
name|targetPut
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|column2
argument_list|,
name|timestamp
argument_list|,
name|value3
argument_list|)
expr_stmt|;
name|targetTable
operator|.
name|put
argument_list|(
name|targetPut
argument_list|)
expr_stmt|;
block|}
name|sourceTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|targetTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

