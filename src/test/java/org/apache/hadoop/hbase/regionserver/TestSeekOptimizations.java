begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
operator|.
name|assertKVListsEqual
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
name|ArrayList
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
name|Collections
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
name|HashSet
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
name|Random
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
comment|/**  * Test various seek optimizations for correctness and check if they are  * actually saving I/O operations.  */
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSeekOptimizations
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
name|TestSeekOptimizations
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Constants
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"myCF"
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
name|PUTS_PER_ROW_COL
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DELETES_PER_ROW_COL
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|VERBOSE
init|=
literal|false
decl_stmt|;
comment|/**    * Disable this when this test fails hopelessly and you need to debug a    * simpler case.    */
specifier|private
specifier|static
specifier|final
name|boolean
name|USE_MANY_STORE_FILES
init|=
literal|true
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
index|[]
index|[]
name|COLUMN_SETS
init|=
operator|new
name|int
index|[]
index|[]
block|{
block|{}
block|,
comment|// All columns
block|{
literal|0
block|}
block|,
block|{
literal|1
block|}
block|,
block|{
literal|0
block|,
literal|2
block|}
block|,
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
block|,   }
decl_stmt|;
comment|// Both start row and end row are inclusive here for the purposes of this
comment|// test.
specifier|private
specifier|static
specifier|final
name|int
index|[]
index|[]
name|ROW_RANGES
init|=
operator|new
name|int
index|[]
index|[]
block|{
block|{
operator|-
literal|1
block|,
operator|-
literal|1
block|}
block|,
block|{
literal|0
block|,
literal|1
block|}
block|,
block|{
literal|1
block|,
literal|1
block|}
block|,
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|0
block|,
literal|2
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
index|[]
name|MAX_VERSIONS_VALUES
init|=
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|2
block|}
decl_stmt|;
comment|// Instance variables
specifier|private
name|HRegion
name|region
decl_stmt|;
specifier|private
name|Put
name|put
decl_stmt|;
specifier|private
name|Delete
name|del
decl_stmt|;
specifier|private
name|Random
name|rand
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Long
argument_list|>
name|putTimestamps
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Long
argument_list|>
name|delTimestamps
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|expectedKVs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Compression
operator|.
name|Algorithm
name|comprAlgo
decl_stmt|;
specifier|private
name|StoreFile
operator|.
name|BloomType
name|bloomType
decl_stmt|;
specifier|private
name|long
name|totalSeekDiligent
decl_stmt|,
name|totalSeekLazy
decl_stmt|;
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
name|Parameters
specifier|public
specifier|static
specifier|final
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
return|return
name|HBaseTestingUtility
operator|.
name|BLOOM_AND_COMPRESSION_COMBINATIONS
return|;
block|}
specifier|public
name|TestSeekOptimizations
parameter_list|(
name|Compression
operator|.
name|Algorithm
name|comprAlgo
parameter_list|,
name|StoreFile
operator|.
name|BloomType
name|bloomType
parameter_list|)
block|{
name|this
operator|.
name|comprAlgo
operator|=
name|comprAlgo
expr_stmt|;
name|this
operator|.
name|bloomType
operator|=
name|bloomType
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|rand
operator|=
operator|new
name|Random
argument_list|(
literal|91238123L
argument_list|)
expr_stmt|;
name|expectedKVs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleTimestampRanges
parameter_list|()
throws|throws
name|IOException
block|{
name|region
operator|=
name|TEST_UTIL
operator|.
name|createTestRegion
argument_list|(
name|TestSeekOptimizations
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|FAMILY
argument_list|,
name|comprAlgo
argument_list|,
name|bloomType
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HFile
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|,
name|DataBlockEncoding
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Delete the given timestamp and everything before.
specifier|final
name|long
name|latestDelTS
init|=
name|USE_MANY_STORE_FILES
condition|?
literal|1397
else|:
operator|-
literal|1
decl_stmt|;
name|createTimestampRange
argument_list|(
literal|1
argument_list|,
literal|50
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|createTimestampRange
argument_list|(
literal|51
argument_list|,
literal|100
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|USE_MANY_STORE_FILES
condition|)
block|{
name|createTimestampRange
argument_list|(
literal|100
argument_list|,
literal|500
argument_list|,
literal|127
argument_list|)
expr_stmt|;
name|createTimestampRange
argument_list|(
literal|900
argument_list|,
literal|1300
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|createTimestampRange
argument_list|(
literal|1301
argument_list|,
literal|2500
argument_list|,
name|latestDelTS
argument_list|)
expr_stmt|;
name|createTimestampRange
argument_list|(
literal|2502
argument_list|,
literal|2598
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|createTimestampRange
argument_list|(
literal|2599
argument_list|,
literal|2999
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|prepareExpectedKVs
argument_list|(
name|latestDelTS
argument_list|)
expr_stmt|;
for|for
control|(
name|int
index|[]
name|columnArr
range|:
name|COLUMN_SETS
control|)
block|{
for|for
control|(
name|int
index|[]
name|rowRange
range|:
name|ROW_RANGES
control|)
block|{
for|for
control|(
name|int
name|maxVersions
range|:
name|MAX_VERSIONS_VALUES
control|)
block|{
for|for
control|(
name|boolean
name|lazySeekEnabled
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
name|testScan
argument_list|(
name|columnArr
argument_list|,
name|lazySeekEnabled
argument_list|,
name|rowRange
index|[
literal|0
index|]
argument_list|,
name|rowRange
index|[
literal|1
index|]
argument_list|,
name|maxVersions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|final
name|double
name|seekSavings
init|=
literal|1
operator|-
name|totalSeekLazy
operator|*
literal|1.0
operator|/
name|totalSeekDiligent
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For bloom="
operator|+
name|bloomType
operator|+
literal|", compr="
operator|+
name|comprAlgo
operator|+
literal|" total seeks without optimization: "
operator|+
name|totalSeekDiligent
operator|+
literal|", with optimization: "
operator|+
name|totalSeekLazy
operator|+
literal|" ("
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%.2f%%"
argument_list|,
name|totalSeekLazy
operator|*
literal|100.0
operator|/
name|totalSeekDiligent
argument_list|)
operator|+
literal|"), savings: "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%.2f%%"
argument_list|,
literal|100.0
operator|*
name|seekSavings
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
comment|// Test that lazy seeks are buying us something. Without the actual
comment|// implementation of the lazy seek optimization this will be 0.
specifier|final
name|double
name|expectedSeekSavings
init|=
literal|0.0
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Lazy seek is only saving "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%.2f%%"
argument_list|,
name|seekSavings
operator|*
literal|100
argument_list|)
operator|+
literal|" seeks but should "
operator|+
literal|"save at least "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%.2f%%"
argument_list|,
name|expectedSeekSavings
operator|*
literal|100
argument_list|)
argument_list|,
name|seekSavings
operator|>=
name|expectedSeekSavings
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testScan
parameter_list|(
specifier|final
name|int
index|[]
name|columnArr
parameter_list|,
specifier|final
name|boolean
name|lazySeekEnabled
parameter_list|,
specifier|final
name|int
name|startRow
parameter_list|,
specifier|final
name|int
name|endRow
parameter_list|,
name|int
name|maxVersions
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreScanner
operator|.
name|enableLazySeekGlobally
argument_list|(
name|lazySeekEnabled
argument_list|)
expr_stmt|;
specifier|final
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|qualSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|iColumn
range|:
name|columnArr
control|)
block|{
name|String
name|qualStr
init|=
name|getQualStr
argument_list|(
name|iColumn
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualStr
argument_list|)
argument_list|)
expr_stmt|;
name|qualSet
operator|.
name|add
argument_list|(
name|qualStr
argument_list|)
expr_stmt|;
block|}
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
name|rowBytes
argument_list|(
name|startRow
argument_list|)
argument_list|)
expr_stmt|;
comment|// Adjust for the fact that for multi-row queries the end row is exclusive.
block|{
specifier|final
name|byte
index|[]
name|scannerStopRow
init|=
name|rowBytes
argument_list|(
name|endRow
operator|+
operator|(
name|startRow
operator|!=
name|endRow
condition|?
literal|1
else|:
literal|0
operator|)
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setStopRow
argument_list|(
name|scannerStopRow
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|initialSeekCount
init|=
name|StoreFileScanner
operator|.
name|getSeekCount
argument_list|()
decl_stmt|;
specifier|final
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
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|actualKVs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
comment|// Such a clumsy do-while loop appears to be the official way to use an
comment|// internalScanner. scanner.next() return value refers to the _next_
comment|// result, not to the one already returned in results.
name|boolean
name|hasNext
decl_stmt|;
do|do
block|{
name|hasNext
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|actualKVs
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasNext
condition|)
do|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|filteredKVs
init|=
name|filterExpectedResults
argument_list|(
name|qualSet
argument_list|,
name|rowBytes
argument_list|(
name|startRow
argument_list|)
argument_list|,
name|rowBytes
argument_list|(
name|endRow
argument_list|)
argument_list|,
name|maxVersions
argument_list|)
decl_stmt|;
specifier|final
name|String
name|rowRestrictionStr
init|=
operator|(
name|startRow
operator|==
operator|-
literal|1
operator|&&
name|endRow
operator|==
operator|-
literal|1
operator|)
condition|?
literal|"all rows"
else|:
operator|(
name|startRow
operator|==
name|endRow
condition|?
operator|(
literal|"row="
operator|+
name|startRow
operator|)
else|:
operator|(
literal|"startRow="
operator|+
name|startRow
operator|+
literal|", "
operator|+
literal|"endRow="
operator|+
name|endRow
operator|)
operator|)
decl_stmt|;
specifier|final
name|String
name|columnRestrictionStr
init|=
name|columnArr
operator|.
name|length
operator|==
literal|0
condition|?
literal|"all columns"
else|:
operator|(
literal|"columns="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|columnArr
argument_list|)
operator|)
decl_stmt|;
specifier|final
name|String
name|testDesc
init|=
literal|"Bloom="
operator|+
name|bloomType
operator|+
literal|", compr="
operator|+
name|comprAlgo
operator|+
literal|", "
operator|+
operator|(
name|scan
operator|.
name|isGetScan
argument_list|()
condition|?
literal|"Get"
else|:
literal|"Scan"
operator|)
operator|+
literal|": "
operator|+
name|columnRestrictionStr
operator|+
literal|", "
operator|+
name|rowRestrictionStr
operator|+
literal|", maxVersions="
operator|+
name|maxVersions
operator|+
literal|", lazySeek="
operator|+
name|lazySeekEnabled
decl_stmt|;
name|long
name|seekCount
init|=
name|StoreFileScanner
operator|.
name|getSeekCount
argument_list|()
operator|-
name|initialSeekCount
decl_stmt|;
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Seek count: "
operator|+
name|seekCount
operator|+
literal|", KVs returned: "
operator|+
name|actualKVs
operator|.
name|size
argument_list|()
operator|+
literal|". "
operator|+
name|testDesc
operator|+
operator|(
name|lazySeekEnabled
condition|?
literal|"\n"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lazySeekEnabled
condition|)
block|{
name|totalSeekLazy
operator|+=
name|seekCount
expr_stmt|;
block|}
else|else
block|{
name|totalSeekDiligent
operator|+=
name|seekCount
expr_stmt|;
block|}
name|assertKVListsEqual
argument_list|(
name|testDesc
argument_list|,
name|filteredKVs
argument_list|,
name|actualKVs
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|filterExpectedResults
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|qualSet
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
name|int
name|maxVersions
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|filteredKVs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|verCount
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|expectedKVs
control|)
block|{
if|if
condition|(
name|startRow
operator|.
name|length
operator|>
literal|0
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|startRow
argument_list|,
literal|0
argument_list|,
name|startRow
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// In this unit test the end row is always inclusive.
if|if
condition|(
name|endRow
operator|.
name|length
operator|>
literal|0
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|endRow
argument_list|,
literal|0
argument_list|,
name|endRow
operator|.
name|length
argument_list|)
operator|>
literal|0
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|qualSet
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|FAMILY_BYTES
argument_list|,
literal|0
argument_list|,
name|FAMILY_BYTES
operator|.
name|length
argument_list|)
operator|!=
literal|0
operator|||
operator|!
name|qualSet
operator|.
name|contains
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
operator|)
condition|)
block|{
continue|continue;
block|}
specifier|final
name|String
name|rowColStr
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"/"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Integer
name|curNumVer
init|=
name|verCount
operator|.
name|get
argument_list|(
name|rowColStr
argument_list|)
decl_stmt|;
specifier|final
name|int
name|newNumVer
init|=
name|curNumVer
operator|!=
literal|null
condition|?
operator|(
name|curNumVer
operator|+
literal|1
operator|)
else|:
literal|1
decl_stmt|;
if|if
condition|(
name|newNumVer
operator|<=
name|maxVersions
condition|)
block|{
name|filteredKVs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|verCount
operator|.
name|put
argument_list|(
name|rowColStr
argument_list|,
name|newNumVer
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|filteredKVs
return|;
block|}
specifier|private
name|void
name|prepareExpectedKVs
parameter_list|(
name|long
name|latestDelTS
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|filteredKVs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|expectedKVs
control|)
block|{
if|if
condition|(
name|kv
operator|.
name|getTimestamp
argument_list|()
operator|>
name|latestDelTS
operator|||
name|latestDelTS
operator|==
operator|-
literal|1
condition|)
block|{
name|filteredKVs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|expectedKVs
operator|=
name|filteredKVs
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|expectedKVs
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|String
name|qual
parameter_list|,
name|long
name|ts
parameter_list|)
block|{
if|if
condition|(
operator|!
name|putTimestamps
operator|.
name|contains
argument_list|(
name|ts
argument_list|)
condition|)
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
name|qual
argument_list|)
argument_list|,
name|ts
argument_list|,
name|createValue
argument_list|(
name|ts
argument_list|)
argument_list|)
expr_stmt|;
name|putTimestamps
operator|.
name|add
argument_list|(
name|ts
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"put: row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|", cf "
operator|+
name|FAMILY
operator|+
literal|", qualifier "
operator|+
name|qual
operator|+
literal|", ts "
operator|+
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|createValue
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|ts
argument_list|)
return|;
block|}
specifier|public
name|void
name|delAtTimestamp
parameter_list|(
name|String
name|qual
parameter_list|,
name|long
name|ts
parameter_list|)
block|{
name|del
operator|.
name|deleteColumn
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qual
argument_list|)
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|logDelete
argument_list|(
name|qual
argument_list|,
name|ts
argument_list|,
literal|"at"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|logDelete
parameter_list|(
name|String
name|qual
parameter_list|,
name|long
name|ts
parameter_list|,
name|String
name|delType
parameter_list|)
block|{
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"del "
operator|+
name|delType
operator|+
literal|": row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|", cf "
operator|+
name|FAMILY
operator|+
literal|", qualifier "
operator|+
name|qual
operator|+
literal|", ts "
operator|+
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|delUpToTimestamp
parameter_list|(
name|String
name|qual
parameter_list|,
name|long
name|upToTS
parameter_list|)
block|{
name|del
operator|.
name|deleteColumns
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qual
argument_list|)
argument_list|,
name|upToTS
argument_list|)
expr_stmt|;
name|logDelete
argument_list|(
name|qual
argument_list|,
name|upToTS
argument_list|,
literal|"up to and including"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|randLong
parameter_list|(
name|long
name|n
parameter_list|)
block|{
name|long
name|l
init|=
name|rand
operator|.
name|nextLong
argument_list|()
decl_stmt|;
if|if
condition|(
name|l
operator|==
name|Long
operator|.
name|MIN_VALUE
condition|)
name|l
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
return|return
name|Math
operator|.
name|abs
argument_list|(
name|l
argument_list|)
operator|%
name|n
return|;
block|}
specifier|private
name|long
name|randBetween
parameter_list|(
name|long
name|a
parameter_list|,
name|long
name|b
parameter_list|)
block|{
name|long
name|x
init|=
name|a
operator|+
name|randLong
argument_list|(
name|b
operator|-
name|a
operator|+
literal|1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|a
operator|<=
name|x
operator|&&
name|x
operator|<=
name|b
argument_list|)
expr_stmt|;
return|return
name|x
return|;
block|}
specifier|private
specifier|final
name|String
name|rowStr
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
operator|(
literal|"row"
operator|+
name|i
operator|)
operator|.
name|intern
argument_list|()
return|;
block|}
specifier|private
specifier|final
name|byte
index|[]
name|rowBytes
parameter_list|(
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
return|;
block|}
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowStr
argument_list|(
name|i
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|String
name|getQualStr
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
operator|(
literal|"qual"
operator|+
name|i
operator|)
operator|.
name|intern
argument_list|()
return|;
block|}
specifier|public
name|void
name|createTimestampRange
parameter_list|(
name|long
name|minTS
parameter_list|,
name|long
name|maxTS
parameter_list|,
name|long
name|deleteUpToTS
parameter_list|)
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
name|minTS
operator|<
name|maxTS
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|deleteUpToTS
operator|==
operator|-
literal|1
operator|||
operator|(
name|minTS
operator|<=
name|deleteUpToTS
operator|&&
name|deleteUpToTS
operator|<=
name|maxTS
operator|)
argument_list|)
expr_stmt|;
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
specifier|final
name|String
name|row
init|=
name|rowStr
argument_list|(
name|iRow
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|rowBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
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
name|NUM_COLS
condition|;
operator|++
name|iCol
control|)
block|{
specifier|final
name|String
name|qual
init|=
name|getQualStr
argument_list|(
name|iCol
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|qualBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qual
argument_list|)
decl_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|rowBytes
argument_list|)
expr_stmt|;
name|putTimestamps
operator|.
name|clear
argument_list|()
expr_stmt|;
name|put
argument_list|(
name|qual
argument_list|,
name|minTS
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|qual
argument_list|,
name|maxTS
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PUTS_PER_ROW_COL
condition|;
operator|++
name|i
control|)
block|{
name|put
argument_list|(
name|qual
argument_list|,
name|randBetween
argument_list|(
name|minTS
argument_list|,
name|maxTS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
index|[]
name|putTimestampList
init|=
operator|new
name|long
index|[
name|putTimestamps
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|ts
range|:
name|putTimestamps
control|)
block|{
name|putTimestampList
index|[
name|i
operator|++
index|]
operator|=
name|ts
expr_stmt|;
block|}
block|}
comment|// Delete a predetermined number of particular timestamps
name|delTimestamps
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|putTimestampList
operator|.
name|length
operator|>=
name|DELETES_PER_ROW_COL
argument_list|)
expr_stmt|;
name|int
name|numToDel
init|=
name|DELETES_PER_ROW_COL
decl_stmt|;
name|int
name|tsRemaining
init|=
name|putTimestampList
operator|.
name|length
decl_stmt|;
name|del
operator|=
operator|new
name|Delete
argument_list|(
name|rowBytes
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|ts
range|:
name|putTimestampList
control|)
block|{
if|if
condition|(
name|rand
operator|.
name|nextInt
argument_list|(
name|tsRemaining
argument_list|)
operator|<
name|numToDel
condition|)
block|{
name|delAtTimestamp
argument_list|(
name|qual
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|putTimestamps
operator|.
name|remove
argument_list|(
name|ts
argument_list|)
expr_stmt|;
operator|--
name|numToDel
expr_stmt|;
block|}
if|if
condition|(
operator|--
name|tsRemaining
operator|==
literal|0
condition|)
block|{
break|break;
block|}
block|}
comment|// Another type of delete: everything up to the given timestamp.
if|if
condition|(
name|deleteUpToTS
operator|!=
operator|-
literal|1
condition|)
block|{
name|delUpToTimestamp
argument_list|(
name|qual
argument_list|,
name|deleteUpToTS
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
if|if
condition|(
operator|!
name|del
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|region
operator|.
name|delete
argument_list|(
name|del
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Add remaining timestamps (those we have not deleted) to expected
comment|// results
for|for
control|(
name|long
name|ts
range|:
name|putTimestamps
control|)
block|{
name|expectedKVs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowBytes
argument_list|,
name|FAMILY_BYTES
argument_list|,
name|qualBytes
argument_list|,
name|ts
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
comment|// We have to re-set the lazy seek flag back to the default so that other
comment|// unit tests are not affected.
name|StoreScanner
operator|.
name|enableLazySeekGlobally
argument_list|(
name|StoreScanner
operator|.
name|LAZY_SEEK_ENABLED_BY_DEFAULT
argument_list|)
expr_stmt|;
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

