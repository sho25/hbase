begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|List
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
name|KeyValue
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
name|TableNotFoundException
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
name|RegionLocator
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|hfile
operator|.
name|HFileContext
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
name|LoadIncrementalHFiles
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
name|MediumTests
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
name|AfterClass
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
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestScannerWithBulkload
block|{
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
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|createTable
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"col"
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testBulkLoad"
argument_list|)
decl_stmt|;
name|long
name|l
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|createTable
argument_list|(
name|admin
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
name|createScan
argument_list|()
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|init
argument_list|(
name|admin
argument_list|,
name|l
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// use bulkload
specifier|final
name|Path
name|hfilePath
init|=
name|writeToHFile
argument_list|(
name|l
argument_list|,
literal|"/temp/testBulkLoad/"
argument_list|,
literal|"/temp/testBulkLoad/col/file"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.mapreduce.bulkload.assign.sequenceNumbers"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|LoadIncrementalHFiles
name|bulkload
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|bulkload
operator|.
name|doBulkLoad
argument_list|(
name|hfilePath
argument_list|,
name|admin
argument_list|,
name|table
argument_list|,
name|locator
argument_list|)
expr_stmt|;
block|}
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
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|result
operator|=
name|scanAfterBulkLoad
argument_list|(
name|scanner
argument_list|,
name|result
argument_list|,
literal|"version2"
argument_list|)
expr_stmt|;
name|Put
name|put0
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|put0
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version3"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put0
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
while|while
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|result
operator|.
name|getColumnCells
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|_c
range|:
name|cells
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowLength
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"row1"
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"version3"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Result
name|scanAfterBulkLoad
parameter_list|(
name|ResultScanner
name|scanner
parameter_list|,
name|Result
name|result
parameter_list|,
name|String
name|expctedVal
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|result
operator|.
name|getColumnCells
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|_c
range|:
name|cells
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowLength
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"row1"
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expctedVal
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|// If nativeHFile is true, we will set cell seq id and MAX_SEQ_ID_KEY in the file.
comment|// Else, we will set BULKLOAD_TIME_KEY.
specifier|private
name|Path
name|writeToHFile
parameter_list|(
name|long
name|l
parameter_list|,
name|String
name|hFilePath
parameter_list|,
name|String
name|pathStr
parameter_list|,
name|boolean
name|nativeHFile
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|hfilePath
init|=
operator|new
name|Path
argument_list|(
name|hFilePath
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|hfilePath
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|pathStr
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|WriterFactory
name|wf
init|=
name|HFile
operator|.
name|getWriterFactoryNoCache
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|wf
argument_list|)
expr_stmt|;
name|HFileContext
name|context
init|=
operator|new
name|HFileContext
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|wf
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version2"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Set cell seq id to test bulk load native hfiles.
if|if
condition|(
name|nativeHFile
condition|)
block|{
comment|// Set a big seq id. Scan should not look at this seq id in a bulk loaded file.
comment|// Scan should only look at the seq id appended at the bulk load time, and not skip
comment|// this kv.
name|kv
operator|.
name|setSequenceId
argument_list|(
literal|9999999
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
name|nativeHFile
condition|)
block|{
comment|// Set a big MAX_SEQ_ID_KEY. Scan should not look at this seq id in a bulk loaded file.
comment|// Scan should only look at the seq id appended at the bulk load time, and not skip its
comment|// kv.
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|StoreFile
operator|.
name|MAX_SEQ_ID_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|Long
argument_list|(
literal|9999999
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|StoreFile
operator|.
name|BULKLOAD_TIME_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|hfilePath
return|;
block|}
specifier|private
name|Table
name|init
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|long
name|l
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
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
decl_stmt|;
name|Put
name|put0
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|put0
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version0"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put0
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version0"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|put0
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
expr_stmt|;
name|put0
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put0
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|compact
argument_list|(
name|tableName
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
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|result
operator|.
name|getColumnCells
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|cells
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|_c
init|=
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"version1"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|table
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkLoadWithParallelScan
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testBulkLoadWithParallelScan"
argument_list|)
decl_stmt|;
specifier|final
name|long
name|l
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|createTable
argument_list|(
name|admin
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
name|createScan
argument_list|()
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|init
argument_list|(
name|admin
argument_list|,
name|l
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// use bulkload
specifier|final
name|Path
name|hfilePath
init|=
name|writeToHFile
argument_list|(
name|l
argument_list|,
literal|"/temp/testBulkLoadWithParallelScan/"
argument_list|,
literal|"/temp/testBulkLoadWithParallelScan/col/file"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.mapreduce.bulkload.assign.sequenceNumbers"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|LoadIncrementalHFiles
name|bulkload
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|conf
argument_list|)
decl_stmt|;
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
comment|// Create a scanner and then do bulk load
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
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row5"
argument_list|)
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row5"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version0"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|bulkload
operator|.
name|doBulkLoad
argument_list|(
name|hfilePath
argument_list|,
name|admin
argument_list|,
name|table
argument_list|,
name|locator
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{         }
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{         }
block|}
block|}
operator|.
name|start
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// By the time we do next() the bulk loaded files are also added to the kv
comment|// scanner
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|scanAfterBulkLoad
argument_list|(
name|scanner
argument_list|,
name|result
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
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
name|testBulkLoadNativeHFile
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testBulkLoadNativeHFile"
argument_list|)
decl_stmt|;
name|long
name|l
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|createTable
argument_list|(
name|admin
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
name|createScan
argument_list|()
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|init
argument_list|(
name|admin
argument_list|,
name|l
argument_list|,
name|scan
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// use bulkload
specifier|final
name|Path
name|hfilePath
init|=
name|writeToHFile
argument_list|(
name|l
argument_list|,
literal|"/temp/testBulkLoadNativeHFile/"
argument_list|,
literal|"/temp/testBulkLoadNativeHFile/col/file"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.mapreduce.bulkload.assign.sequenceNumbers"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|LoadIncrementalHFiles
name|bulkload
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|bulkload
operator|.
name|doBulkLoad
argument_list|(
name|hfilePath
argument_list|,
name|admin
argument_list|,
name|table
argument_list|,
name|locator
argument_list|)
expr_stmt|;
block|}
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
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// We had 'version0', 'version1' for 'row1,col:q' in the table.
comment|// Bulk load added 'version2'  scanner should be able to see 'version2'
name|result
operator|=
name|scanAfterBulkLoad
argument_list|(
name|scanner
argument_list|,
name|result
argument_list|,
literal|"version2"
argument_list|)
expr_stmt|;
name|Put
name|put0
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
name|put0
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|l
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"version3"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put0
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
while|while
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|result
operator|.
name|getColumnCells
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|_c
range|:
name|cells
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowLength
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"row1"
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"version3"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|_c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|_c
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Scan
name|createScan
parameter_list|()
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
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
return|return
name|scan
return|;
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
block|}
end_class

end_unit

