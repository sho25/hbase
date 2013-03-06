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
name|util
operator|.
name|hbck
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
name|ArrayList
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|ServerName
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
name|catalog
operator|.
name|MetaEditor
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
name|Delete
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
name|HConnectionManager
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
name|zookeeper
operator|.
name|KeeperException
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * This testing base class creates a minicluster and testing table table  * and shuts down the cluster afterwards. It also provides methods wipes out  * meta and to inject errors into meta and the file system.  *  * Tests should generally break stuff, then attempt to rebuild the meta table  * offline, then restart hbase, and finally perform checks.  *  * NOTE: This is a slow set of tests which takes ~30s each needs to run on a  * relatively beefy machine. It seems necessary to have each test in a new jvm  * since minicluster startup and tear downs seem to leak file handles and  * eventually cause out of file handle exceptions.  */
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
name|OfflineMetaRebuildTestCore
block|{
specifier|protected
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|OfflineMetaRebuildTestCore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
comment|// for the instance, reset every test run
specifier|protected
name|HTable
name|htbl
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
index|[]
name|splits
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"B"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"C"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TABLE_BASE
init|=
literal|"tableMetaRebuild"
decl_stmt|;
specifier|private
specifier|static
name|int
name|tableIdx
init|=
literal|0
decl_stmt|;
specifier|protected
name|String
name|table
init|=
literal|"tableMetaRebuild"
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUpBefore
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.datanode.max.xceivers"
argument_list|,
literal|9192
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// setup the table
name|table
operator|=
name|TABLE_BASE
operator|+
literal|"-"
operator|+
name|tableIdx
expr_stmt|;
name|tableIdx
operator|++
expr_stmt|;
name|htbl
operator|=
name|setupTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|populateTable
argument_list|(
name|htbl
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|scanMeta
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|" has "
operator|+
name|tableRowCount
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
operator|+
literal|" entries."
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|16
argument_list|,
name|tableRowCount
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listTables
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfter
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|HConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Setup a clean table before we start mucking with it.    *    * @throws IOException    * @throws InterruptedException    * @throws KeeperException    */
specifier|private
name|HTable
name|setupTable
parameter_list|(
name|String
name|tablename
parameter_list|)
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tablename
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAM
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
comment|// If a table has no CF's it doesn't get checked
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|splits
argument_list|)
expr_stmt|;
return|return
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tablename
argument_list|)
return|;
block|}
specifier|private
name|void
name|dumpMeta
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|metaRows
init|=
name|TEST_UTIL
operator|.
name|getMetaTableRows
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|row
range|:
name|metaRows
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|populateTable
parameter_list|(
name|HTable
name|tbl
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|values
init|=
block|{
literal|'A'
block|,
literal|'B'
block|,
literal|'C'
block|,
literal|'D'
block|}
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
name|values
index|[
name|i
index|]
block|,
name|values
index|[
name|j
index|]
block|}
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{}
argument_list|,
operator|new
name|byte
index|[]
block|{
name|values
index|[
name|i
index|]
block|,
name|values
index|[
name|j
index|]
block|}
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
name|tbl
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
comment|/**    * delete table in preparation for next test    *    * @param tablename    * @throws IOException    */
name|void
name|deleteTable
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|String
name|tablename
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|byte
index|[]
name|tbytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tablename
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tbytes
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tbytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Do nothing.
block|}
block|}
specifier|protected
name|void
name|deleteRegion
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|HTable
name|tbl
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Before delete:"
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|tbl
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|dumpMeta
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|hris
init|=
name|tbl
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|e
range|:
name|hris
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ServerName
name|hsa
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|startKey
argument_list|)
operator|==
literal|0
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|endKey
argument_list|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"RegionName: "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|deleteRow
init|=
name|hri
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|unassign
argument_list|(
name|deleteRow
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"deleting hdfs data: "
operator|+
name|hri
operator|.
name|toString
argument_list|()
operator|+
name|hsa
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|rootDir
operator|+
literal|"/"
operator|+
name|htd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|deleteRow
argument_list|)
decl_stmt|;
name|meta
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|hri
operator|.
name|toString
argument_list|()
operator|+
name|hsa
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getMetaTableRows
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"After delete:"
argument_list|)
expr_stmt|;
name|dumpMeta
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HRegionInfo
name|createRegion
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|HTable
name|htbl
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|htbl
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"manually adding regioninfo and hdfs data: "
operator|+
name|hri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|rootDir
operator|+
literal|"/"
operator|+
name|htd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Path
name|riPath
init|=
operator|new
name|Path
argument_list|(
name|p
argument_list|,
name|HRegionFileSystem
operator|.
name|REGION_INFO_FILE
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|riPath
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|hri
operator|.
name|toDelimitedByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// add to meta.
name|MetaEditor
operator|.
name|addRegionToMeta
argument_list|(
name|meta
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|hri
return|;
block|}
specifier|protected
name|void
name|wipeOutMeta
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Mess it up by blowing up meta.
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|meta
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Delete
argument_list|>
name|dels
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|scanner
control|)
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|dels
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|admin
operator|.
name|unassign
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|meta
operator|.
name|delete
argument_list|(
name|dels
argument_list|)
expr_stmt|;
name|meta
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns the number of rows in a given table. HBase must be up and the table    * should be present (will wait for timeout for a while otherwise)    *    * @return # of rows in the specified table    */
specifier|protected
name|int
name|tableRowCount
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|Scan
name|st
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|rst
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|st
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Result
name|rt
range|:
name|rst
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
comment|/**    * Dumps .META. table info    *    * @return # of entries in meta.    */
specifier|protected
name|int
name|scanMeta
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|meta
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|meta
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|scanner
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
block|}
end_class

end_unit

