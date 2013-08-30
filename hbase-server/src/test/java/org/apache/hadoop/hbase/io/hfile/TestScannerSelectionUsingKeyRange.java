begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|hfile
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Set
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
name|SmallTests
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
name|regionserver
operator|.
name|InternalScanner
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
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Test the optimization that does not scan files where all key ranges are excluded.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestScannerSelectionUsingKeyRange
block|{
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
specifier|private
specifier|static
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"myTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|FAMILY
init|=
literal|"myCF"
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_FILES
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Object
argument_list|,
name|Integer
argument_list|>
name|TYPE_COUNT
init|=
operator|new
name|HashMap
argument_list|<
name|Object
argument_list|,
name|Integer
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
static|static
block|{
name|TYPE_COUNT
operator|.
name|put
argument_list|(
name|BloomType
operator|.
name|ROWCOL
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TYPE_COUNT
operator|.
name|put
argument_list|(
name|BloomType
operator|.
name|ROW
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TYPE_COUNT
operator|.
name|put
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|BloomType
name|bloomType
decl_stmt|;
specifier|private
name|int
name|expectedCount
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|type
range|:
name|TYPE_COUNT
operator|.
name|keySet
argument_list|()
control|)
block|{
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|type
block|,
name|TYPE_COUNT
operator|.
name|get
argument_list|(
name|type
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|params
return|;
block|}
specifier|public
name|TestScannerSelectionUsingKeyRange
parameter_list|(
name|Object
name|type
parameter_list|,
name|Object
name|count
parameter_list|)
block|{
name|bloomType
operator|=
operator|(
name|BloomType
operator|)
name|type
expr_stmt|;
name|expectedCount
operator|=
operator|(
name|Integer
operator|)
name|count
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
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScannerSelection
parameter_list|()
throws|throws
name|IOException
block|{
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
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_BYTES
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|bloomType
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
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|iFile
init|=
literal|0
init|;
name|iFile
operator|<
name|NUM_FILES
condition|;
operator|++
name|iFile
control|)
block|{
for|for
control|(
name|int
name|iRow
init|=
literal|0
init|;
name|iRow
operator|<
name|NUM_ROWS
condition|;
operator|++
name|iRow
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
literal|"row"
operator|+
name|iRow
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|iCol
init|=
literal|0
init|;
name|iCol
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|iCol
control|)
block|{
name|put
operator|.
name|add
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|iCol
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|iFile
operator|+
literal|"_"
operator|+
name|iRow
operator|+
literal|"_"
operator|+
name|iCol
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
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
literal|"aaz"
argument_list|)
argument_list|)
decl_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LruBlockCache
name|cache
init|=
operator|(
name|LruBlockCache
operator|)
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|cache
operator|.
name|clearCache
argument_list|()
expr_stmt|;
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
block|{     }
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|accessedFiles
init|=
name|cache
operator|.
name|getCachedFileNamesForTest
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedCount
argument_list|,
name|accessedFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

