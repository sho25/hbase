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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|MapReduceTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|HFileTestUtil
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

begin_comment
comment|/**  * Test cases for the "load" half of the HFileOutputFormat bulk load  * functionality. These tests run faster than the full MR cluster  * tests in TestHFileOutputFormat  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
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
name|String
name|NAMESPACE
init|=
literal|"bulkNS"
decl_stmt|;
specifier|static
specifier|final
name|String
name|EXPECTED_MSG_FOR_NON_EXISTING_FAMILY
init|=
literal|"Unmatched family names found"
decl_stmt|;
specifier|static
specifier|final
name|int
name|MAX_FILES_PER_REGION_PER_FAMILY
init|=
literal|4
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
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|LoadIncrementalHFiles
operator|.
name|MAX_FILES_PER_REGION_PER_FAMILY
argument_list|,
name|MAX_FILES_PER_REGION_PER_FAMILY
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|setupNamespace
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setupNamespace
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|NAMESPACE
argument_list|)
operator|.
name|build
argument_list|()
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
comment|/**    * Test case that creates some regions and loads HFiles that have    * different region boundaries than the table pre-split.    */
annotation|@
name|Test
specifier|public
name|void
name|testSimpleHFileSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"testHFileSplit"
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
block|,
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
literal|"jjj"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ppp"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"uuu"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|,         }
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
literal|"lll"
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
literal|"mmm"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|}
block|,         }
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test case that creates some regions and loads HFiles that cross the boundaries    * and have different region boundaries than the table pre-split.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCrossingHFileSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionCrossingHFileSplit
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test case that creates some regions and loads HFiles that cross the boundaries    * have a ROW bloom filter and a different region boundaries than the table pre-split.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCrossingHFileSplitRowBloom
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionCrossingHFileSplit
argument_list|(
name|BloomType
operator|.
name|ROW
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test case that creates some regions and loads HFiles that cross the boundaries    * have a ROWCOL bloom filter and a different region boundaries than the table pre-split.    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionCrossingHFileSplitRowColBloom
parameter_list|()
throws|throws
name|Exception
block|{
name|testRegionCrossingHFileSplit
argument_list|(
name|BloomType
operator|.
name|ROWCOL
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testRegionCrossingHFileSplit
parameter_list|(
name|BloomType
name|bloomType
parameter_list|)
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"testHFileSplit"
operator|+
name|bloomType
operator|+
literal|"Bloom"
argument_list|,
name|bloomType
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
block|,
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
literal|"jjj"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ppp"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"uuu"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
block|,         }
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
block|,         }
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
name|runTest
argument_list|(
name|testName
argument_list|,
name|bloomType
argument_list|,
literal|null
argument_list|,
name|hfileRanges
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
name|tableSplitKeys
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
specifier|final
name|byte
index|[]
name|TABLE_NAME
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
specifier|final
name|boolean
name|preCreateTable
init|=
name|tableSplitKeys
operator|!=
literal|null
decl_stmt|;
comment|// Run the test bulkloading the table to the default namespace
specifier|final
name|TableName
name|TABLE_WITHOUT_NS
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
name|testName
argument_list|,
name|TABLE_WITHOUT_NS
argument_list|,
name|bloomType
argument_list|,
name|preCreateTable
argument_list|,
name|tableSplitKeys
argument_list|,
name|hfileRanges
argument_list|)
expr_stmt|;
comment|// Run the test bulkloading the table to the specified namespace
specifier|final
name|TableName
name|TABLE_WITH_NS
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|NAMESPACE
argument_list|)
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
name|testName
argument_list|,
name|TABLE_WITH_NS
argument_list|,
name|bloomType
argument_list|,
name|preCreateTable
argument_list|,
name|tableSplitKeys
argument_list|,
name|hfileRanges
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
name|TableName
name|tableName
parameter_list|,
name|BloomType
name|bloomType
parameter_list|,
name|boolean
name|preCreateTable
parameter_list|,
name|byte
index|[]
index|[]
name|tableSplitKeys
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
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
name|runTest
argument_list|(
name|testName
argument_list|,
name|htd
argument_list|,
name|bloomType
argument_list|,
name|preCreateTable
argument_list|,
name|tableSplitKeys
argument_list|,
name|hfileRanges
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
name|HTableDescriptor
name|htd
parameter_list|,
name|BloomType
name|bloomType
parameter_list|,
name|boolean
name|preCreateTable
parameter_list|,
name|byte
index|[]
index|[]
name|tableSplitKeys
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
name|getDataTestDirOnTestFS
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
name|HFileTestUtil
operator|.
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
if|if
condition|(
name|preCreateTable
condition|)
block|{
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|tableSplitKeys
argument_list|)
expr_stmt|;
block|}
specifier|final
name|TableName
name|tableName
init|=
name|htd
operator|.
name|getTableName
argument_list|()
decl_stmt|;
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
name|String
index|[]
name|args
init|=
block|{
name|dir
operator|.
name|toString
argument_list|()
block|,
name|tableName
operator|.
name|toString
argument_list|()
block|}
decl_stmt|;
name|loader
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|Table
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
try|try
block|{
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
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test loading into a column family that does not exist.    */
annotation|@
name|Test
specifier|public
name|void
name|testNonexistentColumnFamilyLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|testName
init|=
literal|"testNonexistentColumnFamilyLoad"
decl_stmt|;
name|byte
index|[]
index|[]
index|[]
name|hFileRanges
init|=
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
literal|"aaa"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE
argument_list|)
argument_list|)
decl_stmt|;
comment|// set real family name to upper case in purpose to simulate the case that
comment|// family name in HFiles is invalid
name|HColumnDescriptor
name|family
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|String
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|toUpperCase
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
try|try
block|{
name|runTest
argument_list|(
name|testName
argument_list|,
name|htd
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|,
name|SPLIT_KEYS
argument_list|,
name|hFileRanges
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Loading into table with non-existent family should have failed"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"IOException expected"
argument_list|,
name|e
operator|instanceof
name|IOException
argument_list|)
expr_stmt|;
comment|// further check whether the exception message is correct
name|String
name|errMsg
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect exception message, expected message: ["
operator|+
name|EXPECTED_MSG_FOR_NON_EXISTING_FAMILY
operator|+
literal|"], current message: ["
operator|+
name|errMsg
operator|+
literal|"]"
argument_list|,
name|errMsg
operator|.
name|contains
argument_list|(
name|EXPECTED_MSG_FOR_NON_EXISTING_FAMILY
argument_list|)
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
name|getDataTestDirOnTestFS
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
name|HFileTestUtil
operator|.
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
argument_list|,
name|conf
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
name|Test
specifier|public
name|void
name|testLoadTooMayHFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|dir
init|=
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testLoadTooMayHFiles"
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
name|byte
index|[]
name|from
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"begin"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|to
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"end"
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
operator|<=
name|MAX_FILES_PER_REGION_PER_FAMILY
condition|;
name|i
operator|++
control|)
block|{
name|HFileTestUtil
operator|.
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
name|i
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
name|String
index|[]
name|args
init|=
block|{
name|dir
operator|.
name|toString
argument_list|()
block|,
literal|"mytable_testLoadTooMayHFiles"
block|}
decl_stmt|;
try|try
block|{
name|loader
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Bulk loading too many files should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|ie
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Trying to load more than "
operator|+
name|MAX_FILES_PER_REGION_PER_FAMILY
operator|+
literal|" hfiles"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

