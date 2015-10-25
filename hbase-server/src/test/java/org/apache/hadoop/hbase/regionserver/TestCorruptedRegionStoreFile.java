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
name|regionserver
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
name|ArrayList
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
name|Durability
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|FSVisitor
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
name|TestTableName
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
name|Before
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestCorruptedRegionStoreFile
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
name|TestCorruptedRegionStoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
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
name|String
name|FAMILY_NAME_STR
init|=
literal|"f"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY_NAME_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_FILES
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_PER_FILE
init|=
literal|2000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
name|NUM_FILES
operator|*
name|ROW_PER_FILE
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestTableName
name|TEST_TABLE
init|=
operator|new
name|TestTableName
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|storeFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|tableDir
decl_stmt|;
specifier|private
name|int
name|rowCount
decl_stmt|;
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Disable compaction so the store file count stays constant
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
name|NUM_FILES
operator|+
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
name|NUM_FILES
operator|*
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// load the table
name|Table
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY_NAME
argument_list|)
decl_stmt|;
try|try
block|{
name|rowCount
operator|=
literal|0
expr_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
while|while
condition|(
name|rowCount
operator|<
name|NUM_ROWS
condition|)
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
name|String
operator|.
name|format
argument_list|(
literal|"%010d"
argument_list|,
name|rowCount
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY_NAME
argument_list|,
name|q
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|rowCount
operator|++
operator|%
name|ROW_PER_FILE
operator|)
operator|==
literal|0
condition|)
block|{
comment|// flush it
operator|(
operator|(
name|HTable
operator|)
name|table
operator|)
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
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
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|rowCount
argument_list|)
expr_stmt|;
comment|// get the store file paths
name|storeFiles
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tableDir
operator|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|FSVisitor
operator|.
name|visitTableStoreFiles
argument_list|(
name|getFileSystem
argument_list|()
argument_list|,
name|tableDir
argument_list|,
operator|new
name|FSVisitor
operator|.
name|StoreFileVisitor
argument_list|()
block|{
annotation|@
name|Override
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
name|HFileLink
name|link
init|=
name|HFileLink
operator|.
name|build
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|region
argument_list|,
name|family
argument_list|,
name|hfile
argument_list|)
decl_stmt|;
name|storeFiles
operator|.
name|add
argument_list|(
name|link
operator|.
name|getOriginPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected at least "
operator|+
name|NUM_FILES
operator|+
literal|" store files"
argument_list|,
name|storeFiles
operator|.
name|size
argument_list|()
operator|>=
name|NUM_FILES
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Store files: "
operator|+
name|storeFiles
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
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
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|setupTable
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
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
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testLosingFileDuringScan
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|rowCount
argument_list|,
name|fullScanAndCount
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|getFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tmpStoreFilePath
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"corruptedHFile"
argument_list|)
decl_stmt|;
comment|// try to query with the missing file
name|int
name|count
init|=
name|fullScanAndCount
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|new
name|ScanInjector
argument_list|()
block|{
specifier|private
name|boolean
name|hasFile
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|beforeScanNext
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|Exception
block|{
comment|// move the path away (now the region is corrupted)
if|if
condition|(
name|hasFile
condition|)
block|{
name|fs
operator|.
name|copyToLocalFile
argument_list|(
literal|true
argument_list|,
name|storeFiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|tmpStoreFilePath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Move file to local"
argument_list|)
expr_stmt|;
name|evictHFileCache
argument_list|(
name|storeFiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|hasFile
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected one file lost: rowCount="
operator|+
name|count
operator|+
literal|" lostRows="
operator|+
operator|(
name|NUM_ROWS
operator|-
name|count
operator|)
argument_list|,
name|count
operator|>=
operator|(
name|NUM_ROWS
operator|-
name|ROW_PER_FILE
operator|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testLosingFileAfterScannerInit
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|rowCount
argument_list|,
name|fullScanAndCount
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|getFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tmpStoreFilePath
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"corruptedHFile"
argument_list|)
decl_stmt|;
comment|// try to query with the missing file
name|int
name|count
init|=
name|fullScanAndCount
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|new
name|ScanInjector
argument_list|()
block|{
specifier|private
name|boolean
name|hasFile
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|beforeScan
parameter_list|(
name|Table
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
block|{
comment|// move the path away (now the region is corrupted)
if|if
condition|(
name|hasFile
condition|)
block|{
name|fs
operator|.
name|copyToLocalFile
argument_list|(
literal|true
argument_list|,
name|storeFiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|tmpStoreFilePath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Move file to local"
argument_list|)
expr_stmt|;
name|evictHFileCache
argument_list|(
name|storeFiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|hasFile
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"expected one file lost: rowCount="
operator|+
name|count
operator|+
literal|" lostRows="
operator|+
operator|(
name|NUM_ROWS
operator|-
name|count
operator|)
argument_list|,
name|count
operator|>=
operator|(
name|NUM_ROWS
operator|-
name|ROW_PER_FILE
operator|)
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Helpers
comment|// ==========================================================================
specifier|private
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
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
name|getFileSystem
argument_list|()
return|;
block|}
specifier|private
name|Path
name|getRootDir
parameter_list|()
block|{
return|return
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
return|;
block|}
specifier|private
name|void
name|evictHFileCache
parameter_list|(
specifier|final
name|Path
name|hfile
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|RegionServerThread
name|rst
range|:
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|HRegionServer
name|rs
init|=
name|rst
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|rs
operator|.
name|getCacheConfig
argument_list|()
operator|.
name|getBlockCache
argument_list|()
operator|.
name|evictBlocksByHfileName
argument_list|(
name|hfile
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|6000
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|fullScanAndCount
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|fullScanAndCount
argument_list|(
name|tableName
argument_list|,
operator|new
name|ScanInjector
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|int
name|fullScanAndCount
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ScanInjector
name|injector
parameter_list|)
throws|throws
name|Exception
block|{
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
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCaching
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|injector
operator|.
name|beforeScan
argument_list|(
name|table
argument_list|,
name|scan
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|injector
operator|.
name|beforeScanNext
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|injector
operator|.
name|afterScanNext
argument_list|(
name|table
argument_list|,
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
break|break;
if|if
condition|(
operator|(
name|count
operator|++
operator|%
operator|(
name|ROW_PER_FILE
operator|/
literal|2
operator|)
operator|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"scan next "
operator|+
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|injector
operator|.
name|afterScan
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|private
class|class
name|ScanInjector
block|{
specifier|protected
name|void
name|beforeScan
parameter_list|(
name|Table
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
block|{}
specifier|protected
name|void
name|beforeScanNext
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|Exception
block|{}
specifier|protected
name|void
name|afterScanNext
parameter_list|(
name|Table
name|table
parameter_list|,
name|Result
name|result
parameter_list|)
throws|throws
name|Exception
block|{}
specifier|protected
name|void
name|afterScan
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|Exception
block|{}
block|}
block|}
end_class

end_unit

