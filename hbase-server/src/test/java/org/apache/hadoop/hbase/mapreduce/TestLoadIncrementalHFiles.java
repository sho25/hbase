begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertArrayEquals
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
name|TreeMap
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
name|*
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|Compression
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
name|HFileScanner
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
name|StoreFile
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
name|StoreFile
operator|.
name|BloomType
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
name|*
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
comment|/**  * Test cases for the "load" half of the HFileOutputFormat bulk load  * functionality. These tests run faster than the full MR cluster  * tests in TestHFileOutputFormat  */
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
name|TestLoadIncrementalHFiles
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myqual"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myfam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|SPLIT_KEYS
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
literal|"ddd"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ppp"
argument_list|)
block|}
decl_stmt|;
specifier|public
specifier|static
name|int
name|BLOCKSIZE
init|=
literal|64
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
name|String
name|COMPRESSION
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|util
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
name|util
operator|.
name|startMiniCluster
argument_list|()
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
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test case that creates some regions and loads    * HFiles that fit snugly inside those regions    */
annotation|@
name|Test
specifier|public
name|void
name|testSimpleLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"testSimpleLoad"
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cccc"
argument_list|)
block|}
block|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ooo"
argument_list|)
block|}
block|,     }
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test case that creates some regions and loads    * HFiles that cross the boundaries of those regions    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCrossingLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"testRegionCrossingLoad"
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
block|}
block|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|}
block|,     }
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test loading into a column family that has a ROW bloom filter.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCrossingRowBloom
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"testRegionCrossingLoadRowBloom"
argument_list|,
name|BloomType
operator|.
name|ROW
argument_list|,
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
block|}
block|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|}
block|,     }
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test loading into a column family that has a ROWCOL bloom filter.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCrossingRowColBloom
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"testRegionCrossingLoadRowColBloom"
argument_list|,
name|BloomType
operator|.
name|ROWCOL
argument_list|,
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
block|}
block|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|}
block|,     }
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTest
parameter_list|(
name|String
name|testName
parameter_list|,
name|BloomType
name|bloomType
parameter_list|,
name|byte
index|[]
index|[]
index|[]
name|hfileRanges
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|dir
init|=
name|util
operator|.
name|getDataTestDir
argument_list|(
name|testName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|util
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|dir
operator|=
name|dir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|hfileIdx
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
index|[]
name|range
range|:
name|hfileRanges
control|)
block|{
name|byte
index|[]
name|from
init|=
name|range
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|to
init|=
name|range
index|[
literal|1
index|]
decl_stmt|;
name|createHFile
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
literal|"hfile_"
operator|+
name|hfileIdx
operator|++
argument_list|)
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
name|int
name|expectedRows
init|=
name|hfileIdx
operator|*
literal|1000
decl_stmt|;
specifier|final
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mytable_"
operator|+
name|testName
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|familyDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|familyDesc
operator|.
name|setBloomFilterType
argument_list|(
name|bloomType
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|familyDesc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|SPLIT_KEYS
argument_list|)
expr_stmt|;
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
name|TABLE
argument_list|)
decl_stmt|;
name|util
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|LoadIncrementalHFiles
name|loader
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|loader
operator|.
name|doBulkLoad
argument_list|(
name|dir
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedRows
argument_list|,
name|util
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyAssignedSequenceNumber
parameter_list|(
name|String
name|testName
parameter_list|,
name|byte
index|[]
index|[]
index|[]
name|hfileRanges
parameter_list|,
name|boolean
name|nonZero
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|dir
init|=
name|util
operator|.
name|getDataTestDir
argument_list|(
name|testName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|util
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|dir
operator|=
name|dir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|hfileIdx
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
index|[]
name|range
range|:
name|hfileRanges
control|)
block|{
name|byte
index|[]
name|from
init|=
name|range
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|to
init|=
name|range
index|[
literal|1
index|]
decl_stmt|;
name|createHFile
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
literal|"hfile_"
operator|+
name|hfileIdx
operator|++
argument_list|)
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
specifier|final
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mytable_"
operator|+
name|testName
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|familyDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|familyDesc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|SPLIT_KEYS
argument_list|)
expr_stmt|;
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
name|TABLE
argument_list|)
decl_stmt|;
name|util
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|LoadIncrementalHFiles
name|loader
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Do a dummy put to increase the hlog sequence number
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
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|loader
operator|.
name|doBulkLoad
argument_list|(
name|dir
argument_list|,
name|table
argument_list|)
expr_stmt|;
comment|// Get the store files
name|List
argument_list|<
name|StoreFile
argument_list|>
name|files
init|=
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStore
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|getStorefiles
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
name|file
range|:
name|files
control|)
block|{
comment|// the sequenceId gets initialized during createReader
name|file
operator|.
name|createReader
argument_list|()
expr_stmt|;
if|if
condition|(
name|nonZero
condition|)
name|assertTrue
argument_list|(
name|file
operator|.
name|getMaxSequenceId
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
else|else
name|assertTrue
argument_list|(
name|file
operator|.
name|getMaxSequenceId
argument_list|()
operator|==
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitStoreFile
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|dir
init|=
name|util
operator|.
name|getDataTestDir
argument_list|(
literal|"testSplitHFile"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|util
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|testIn
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"testhfile"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|familyDesc
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|createHFile
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|testIn
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|Path
name|bottomOut
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"bottom.out"
argument_list|)
decl_stmt|;
name|Path
name|topOut
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"top.out"
argument_list|)
decl_stmt|;
name|LoadIncrementalHFiles
operator|.
name|splitStoreFile
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|testIn
argument_list|,
name|familyDesc
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ggg"
argument_list|)
argument_list|,
name|bottomOut
argument_list|,
name|topOut
argument_list|)
expr_stmt|;
name|int
name|rowCount
init|=
name|verifyHFile
argument_list|(
name|bottomOut
argument_list|)
decl_stmt|;
name|rowCount
operator|+=
name|verifyHFile
argument_list|(
name|topOut
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|rowCount
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|verifyHFile
parameter_list|(
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|p
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
do|do
block|{
name|count
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
condition|)
do|;
name|assertTrue
argument_list|(
name|count
operator|>
literal|0
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|count
return|;
block|}
comment|/**    * Create an HFile with the given number of rows between a given    * start key and end key.    * TODO put me in an HFileTestUtil or something?    */
specifier|static
name|void
name|createHFile
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|conf
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|withBlockSize
argument_list|(
name|BLOCKSIZE
argument_list|)
operator|.
name|withCompression
argument_list|(
name|COMPRESSION
argument_list|)
operator|.
name|withComparator
argument_list|(
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
comment|// subtract 2 since iterateOnSplits doesn't include boundary keys
for|for
control|(
name|byte
index|[]
name|key
range|:
name|Bytes
operator|.
name|iterateOnSplits
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numRows
operator|-
literal|2
argument_list|)
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|key
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
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
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addStartEndKeysForTest
parameter_list|(
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|map
parameter_list|,
name|byte
index|[]
name|first
parameter_list|,
name|byte
index|[]
name|last
parameter_list|)
block|{
name|Integer
name|value
init|=
name|map
operator|.
name|containsKey
argument_list|(
name|first
argument_list|)
condition|?
operator|(
name|Integer
operator|)
name|map
operator|.
name|get
argument_list|(
name|first
argument_list|)
else|:
literal|0
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|first
argument_list|,
name|value
operator|+
literal|1
argument_list|)
expr_stmt|;
name|value
operator|=
name|map
operator|.
name|containsKey
argument_list|(
name|last
argument_list|)
condition|?
operator|(
name|Integer
operator|)
name|map
operator|.
name|get
argument_list|(
name|last
argument_list|)
else|:
literal|0
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|last
argument_list|,
name|value
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInferBoundaries
parameter_list|()
block|{
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|map
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/* Toy example      *     c---------i            o------p          s---------t     v------x      * a------e    g-----k   m-------------q   r----s            u----w      *      * Should be inferred as:      * a-----------------k   m-------------q   r--------------t  u---------x      *       * The output should be (m,r,u)       */
name|String
name|first
decl_stmt|;
name|String
name|last
decl_stmt|;
name|first
operator|=
literal|"a"
expr_stmt|;
name|last
operator|=
literal|"e"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"r"
expr_stmt|;
name|last
operator|=
literal|"s"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"o"
expr_stmt|;
name|last
operator|=
literal|"p"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"g"
expr_stmt|;
name|last
operator|=
literal|"k"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"v"
expr_stmt|;
name|last
operator|=
literal|"x"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"c"
expr_stmt|;
name|last
operator|=
literal|"i"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"m"
expr_stmt|;
name|last
operator|=
literal|"q"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"s"
expr_stmt|;
name|last
operator|=
literal|"t"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|"u"
expr_stmt|;
name|last
operator|=
literal|"w"
expr_stmt|;
name|addStartEndKeysForTest
argument_list|(
name|map
argument_list|,
name|first
operator|.
name|getBytes
argument_list|()
argument_list|,
name|last
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|keysArray
init|=
name|LoadIncrementalHFiles
operator|.
name|inferBoundaries
argument_list|(
name|map
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|compare
init|=
operator|new
name|byte
index|[
literal|3
index|]
index|[]
decl_stmt|;
name|compare
index|[
literal|0
index|]
operator|=
literal|"m"
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|compare
index|[
literal|1
index|]
operator|=
literal|"r"
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|compare
index|[
literal|2
index|]
operator|=
literal|"u"
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|keysArray
operator|.
name|length
argument_list|,
literal|3
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|row
init|=
literal|0
init|;
name|row
operator|<
name|keysArray
operator|.
name|length
condition|;
name|row
operator|++
control|)
block|{
name|assertArrayEquals
argument_list|(
name|keysArray
index|[
name|row
index|]
argument_list|,
name|compare
index|[
name|row
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

