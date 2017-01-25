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
name|encoding
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|Tag
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
name|ArrayBackedTag
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
name|Get
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
name|LruBlockCache
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
name|Region
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
name|IOTests
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
name|Strings
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
name|test
operator|.
name|LoadTestKVGenerator
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
comment|/**  * Tests encoded seekers by loading and reading values.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
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
name|TestEncodedSeekers
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"encodedSeekersTable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CF_NAME
init|=
literal|"encodedSeekersCF"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CF_NAME
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BLOCK_SIZE
init|=
literal|64
operator|*
literal|1024
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MIN_VALUE_SIZE
init|=
literal|30
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VALUE_SIZE
init|=
literal|60
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|1003
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLS_PER_ROW
init|=
literal|20
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_HFILES
init|=
literal|4
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS_PER_FLUSH
init|=
name|NUM_ROWS
operator|/
name|NUM_HFILES
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|testUtil
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|DataBlockEncoding
name|encoding
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|includeTags
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|compressTags
decl_stmt|;
comment|/** Enable when debugging */
specifier|private
specifier|static
specifier|final
name|boolean
name|VERBOSE
init|=
literal|false
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
name|paramList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|boolean
name|includeTags
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
for|for
control|(
name|boolean
name|compressTags
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
name|paramList
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|encoding
block|,
name|includeTags
block|,
name|compressTags
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|paramList
return|;
block|}
specifier|public
name|TestEncodedSeekers
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|,
name|boolean
name|includeTags
parameter_list|,
name|boolean
name|compressTags
parameter_list|)
block|{
name|this
operator|.
name|encoding
operator|=
name|encoding
expr_stmt|;
name|this
operator|.
name|includeTags
operator|=
name|includeTags
expr_stmt|;
name|this
operator|.
name|compressTags
operator|=
name|compressTags
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEncodedSeeker
parameter_list|()
throws|throws
name|IOException
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Testing encoded seekers for encoding : "
operator|+
name|encoding
operator|+
literal|", includeTags : "
operator|+
name|includeTags
operator|+
literal|", compressTags : "
operator|+
name|compressTags
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeTags
condition|)
block|{
name|testUtil
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
name|LruBlockCache
name|cache
init|=
operator|(
name|LruBlockCache
operator|)
operator|new
name|CacheConfig
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|cache
operator|.
name|clearCache
argument_list|()
expr_stmt|;
comment|// Need to disable default row bloom filter for this test to pass.
name|HColumnDescriptor
name|hcd
init|=
operator|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CF_NAME
argument_list|)
operator|)
operator|.
name|setMaxVersions
argument_list|(
name|MAX_VERSIONS
argument_list|)
operator|.
name|setDataBlockEncoding
argument_list|(
name|encoding
argument_list|)
operator|.
name|setBlocksize
argument_list|(
name|BLOCK_SIZE
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
operator|.
name|setCompressTags
argument_list|(
name|compressTags
argument_list|)
decl_stmt|;
name|Region
name|region
init|=
name|testUtil
operator|.
name|createTestRegion
argument_list|(
name|TABLE_NAME
argument_list|,
name|hcd
argument_list|)
decl_stmt|;
comment|//write the data, but leave some in the memstore
name|doPuts
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|//verify correctness when memstore contains data
name|doGets
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|//verify correctness again after compacting
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|doGets
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|DataBlockEncoding
argument_list|,
name|Integer
argument_list|>
name|encodingCounts
init|=
name|cache
operator|.
name|getEncodingCountsForTest
argument_list|()
decl_stmt|;
comment|// Ensure that compactions don't pollute the cache with unencoded blocks
comment|// in case of in-cache-only encoding.
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"encodingCounts="
operator|+
name|encodingCounts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|encodingCounts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|DataBlockEncoding
name|encodingInCache
init|=
name|encodingCounts
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|encoding
argument_list|,
name|encodingInCache
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|encodingCounts
operator|.
name|get
argument_list|(
name|encodingInCache
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doPuts
parameter_list|(
name|Region
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|LoadTestKVGenerator
name|dataGenerator
init|=
operator|new
name|LoadTestKVGenerator
argument_list|(
name|MIN_VALUE_SIZE
argument_list|,
name|MAX_VALUE_SIZE
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
name|NUM_ROWS
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|key
init|=
name|LoadTestKVGenerator
operator|.
name|md5PrefixedKey
argument_list|(
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|ASYNC_WAL
argument_list|)
expr_stmt|;
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|dataGenerator
operator|.
name|generateRandomSizeValue
argument_list|(
name|key
argument_list|,
name|col
argument_list|)
decl_stmt|;
if|if
condition|(
name|includeTags
condition|)
block|{
name|Tag
index|[]
name|tag
init|=
operator|new
name|Tag
index|[
literal|1
index|]
decl_stmt|;
name|tag
index|[
literal|0
index|]
operator|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"Visibility"
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|CF_BYTES
argument_list|,
name|col
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|,
name|tag
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|addColumn
argument_list|(
name|CF_BYTES
argument_list|,
name|col
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|KeyValue
name|kvPut
init|=
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|CF_BYTES
argument_list|,
name|col
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|Strings
operator|.
name|padFront
argument_list|(
name|i
operator|+
literal|""
argument_list|,
literal|' '
argument_list|,
literal|4
argument_list|)
operator|+
literal|" "
operator|+
name|kvPut
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
if|if
condition|(
name|i
operator|%
name|NUM_ROWS_PER_FLUSH
operator|==
literal|0
condition|)
block|{
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|doGets
parameter_list|(
name|Region
name|region
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_ROWS
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|rowKey
init|=
name|LoadTestKVGenerator
operator|.
name|md5PrefixedKey
argument_list|(
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_COLS_PER_ROW
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|String
name|qualStr
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|j
argument_list|)
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
literal|"Reading row "
operator|+
name|i
operator|+
literal|", column "
operator|+
name|j
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|rowKey
argument_list|)
operator|+
literal|"/"
operator|+
name|qualStr
argument_list|)
expr_stmt|;
block|}
specifier|final
name|byte
index|[]
name|qualBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualStr
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|CF_BYTES
argument_list|,
name|qualBytes
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|result
operator|.
name|getValue
argument_list|(
name|CF_BYTES
argument_list|,
name|qualBytes
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|LoadTestKVGenerator
operator|.
name|verify
argument_list|(
name|value
argument_list|,
name|rowKey
argument_list|,
name|qualBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

