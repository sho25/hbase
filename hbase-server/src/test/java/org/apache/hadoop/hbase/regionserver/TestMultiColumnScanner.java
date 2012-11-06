begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertFalse
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
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|lang
operator|.
name|ArrayUtils
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
name|KeyValueTestUtil
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
name|compress
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
comment|/**  * Tests optimized scanning of multiple columns.  */
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
name|TestMultiColumnScanner
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
name|TestMultiColumnScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
name|TestMultiColumnScanner
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|50
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"CF"
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
comment|/**    * The size of the column qualifier set used. Increasing this parameter    * exponentially increases test time.    */
specifier|private
specifier|static
specifier|final
name|int
name|NUM_COLUMNS
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_COLUMN_BIT_MASK
init|=
literal|1
operator|<<
name|NUM_COLUMNS
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_FLUSHES
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|20
decl_stmt|;
comment|/** A large value of type long for use as a timestamp */
specifier|private
specifier|static
specifier|final
name|long
name|BIG_LONG
init|=
literal|9111222333444555666L
decl_stmt|;
comment|/**    * Timestamps to test with. Cannot use {@link Long#MAX_VALUE} here, because    * it will be replaced by an timestamp auto-generated based on the time.    */
specifier|private
specifier|static
specifier|final
name|long
index|[]
name|TIMESTAMPS
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|5
block|,
name|Integer
operator|.
name|MAX_VALUE
block|,
name|BIG_LONG
block|,
name|Long
operator|.
name|MAX_VALUE
operator|-
literal|1
block|}
decl_stmt|;
comment|/** The probability that a column is skipped in a store file. */
specifier|private
specifier|static
specifier|final
name|double
name|COLUMN_SKIP_IN_STORE_FILE_PROB
init|=
literal|0.7
decl_stmt|;
comment|/** The probability of skipping a column in a single row */
specifier|private
specifier|static
specifier|final
name|double
name|COLUMN_SKIP_IN_ROW_PROB
init|=
literal|0.1
decl_stmt|;
comment|/** The probability of skipping a column everywhere */
specifier|private
specifier|static
specifier|final
name|double
name|COLUMN_SKIP_EVERYWHERE_PROB
init|=
literal|0.1
decl_stmt|;
comment|/** The probability to delete a row/column pair */
specifier|private
specifier|static
specifier|final
name|double
name|DELETE_PROBABILITY
init|=
literal|0.02
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
specifier|private
specifier|final
name|Compression
operator|.
name|Algorithm
name|comprAlgo
decl_stmt|;
specifier|private
specifier|final
name|StoreFile
operator|.
name|BloomType
name|bloomType
decl_stmt|;
specifier|private
specifier|final
name|DataBlockEncoding
name|dataBlockEncoding
decl_stmt|;
comment|// Some static sanity-checking.
static|static
block|{
name|assertTrue
argument_list|(
name|BIG_LONG
operator|>
literal|0.9
operator|*
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
comment|// Guard against typos.
comment|// Ensure TIMESTAMPS are sorted.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|TIMESTAMPS
operator|.
name|length
operator|-
literal|1
condition|;
operator|++
name|i
control|)
name|assertTrue
argument_list|(
name|TIMESTAMPS
index|[
name|i
index|]
operator|<
name|TIMESTAMPS
index|[
name|i
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
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
index|[]
name|bloomAndCompressionParams
range|:
name|HBaseTestingUtility
operator|.
name|BLOOM_AND_COMPRESSION_COMBINATIONS
control|)
block|{
for|for
control|(
name|boolean
name|useDataBlockEncoding
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
name|parameters
operator|.
name|add
argument_list|(
name|ArrayUtils
operator|.
name|add
argument_list|(
name|bloomAndCompressionParams
argument_list|,
name|useDataBlockEncoding
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|parameters
return|;
block|}
specifier|public
name|TestMultiColumnScanner
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
parameter_list|,
name|boolean
name|useDataBlockEncoding
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
name|this
operator|.
name|dataBlockEncoding
operator|=
name|useDataBlockEncoding
condition|?
name|DataBlockEncoding
operator|.
name|PREFIX
else|:
name|DataBlockEncoding
operator|.
name|NONE
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiColumnScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
name|TEST_UTIL
operator|.
name|createTestRegion
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setCompressionType
argument_list|(
name|comprAlgo
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|bloomType
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|MAX_VERSIONS
argument_list|)
operator|.
name|setDataBlockEncoding
argument_list|(
name|dataBlockEncoding
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|sequentialStrings
argument_list|(
literal|"row"
argument_list|,
name|NUM_ROWS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|qualifiers
init|=
name|sequentialStrings
argument_list|(
literal|"qual"
argument_list|,
name|NUM_COLUMNS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|keySet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// A map from<row>_<qualifier> to the most recent delete timestamp for
comment|// that column.
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastDelTimeMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|29372937L
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|rowQualSkip
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Skip some columns in some rows. We need to test scanning over a set
comment|// of columns when some of the columns are not there.
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
for|for
control|(
name|String
name|qual
range|:
name|qualifiers
control|)
if|if
condition|(
name|rand
operator|.
name|nextDouble
argument_list|()
operator|<
name|COLUMN_SKIP_IN_ROW_PROB
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping "
operator|+
name|qual
operator|+
literal|" in row "
operator|+
name|row
argument_list|)
expr_stmt|;
name|rowQualSkip
operator|.
name|add
argument_list|(
name|rowQualKey
argument_list|(
name|row
argument_list|,
name|qual
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Also skip some columns in all rows.
for|for
control|(
name|String
name|qual
range|:
name|qualifiers
control|)
if|if
condition|(
name|rand
operator|.
name|nextDouble
argument_list|()
operator|<
name|COLUMN_SKIP_EVERYWHERE_PROB
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping "
operator|+
name|qual
operator|+
literal|" in all rows"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
name|rowQualSkip
operator|.
name|add
argument_list|(
name|rowQualKey
argument_list|(
name|row
argument_list|,
name|qual
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|iFlush
init|=
literal|0
init|;
name|iFlush
operator|<
name|NUM_FLUSHES
condition|;
operator|++
name|iFlush
control|)
block|{
for|for
control|(
name|String
name|qual
range|:
name|qualifiers
control|)
block|{
comment|// This is where we decide to include or not include this column into
comment|// this store file, regardless of row and timestamp.
if|if
condition|(
name|rand
operator|.
name|nextDouble
argument_list|()
operator|<
name|COLUMN_SKIP_IN_STORE_FILE_PROB
condition|)
continue|continue;
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
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|ts
range|:
name|TIMESTAMPS
control|)
block|{
name|String
name|value
init|=
name|createValue
argument_list|(
name|row
argument_list|,
name|qual
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|FAMILY
argument_list|,
name|qual
argument_list|,
name|ts
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|String
name|keyAsString
init|=
name|kv
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|keySet
operator|.
name|contains
argument_list|(
name|keyAsString
argument_list|)
condition|)
block|{
name|keySet
operator|.
name|add
argument_list|(
name|keyAsString
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|deletedSomething
init|=
literal|false
decl_stmt|;
for|for
control|(
name|long
name|ts
range|:
name|TIMESTAMPS
control|)
if|if
condition|(
name|rand
operator|.
name|nextDouble
argument_list|()
operator|<
name|DELETE_PROBABILITY
condition|)
block|{
name|d
operator|.
name|deleteColumns
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|qualBytes
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|String
name|rowAndQual
init|=
name|row
operator|+
literal|"_"
operator|+
name|qual
decl_stmt|;
name|Long
name|whenDeleted
init|=
name|lastDelTimeMap
operator|.
name|get
argument_list|(
name|rowAndQual
argument_list|)
decl_stmt|;
name|lastDelTimeMap
operator|.
name|put
argument_list|(
name|rowAndQual
argument_list|,
name|whenDeleted
operator|==
literal|null
condition|?
name|ts
else|:
name|Math
operator|.
name|max
argument_list|(
name|ts
argument_list|,
name|whenDeleted
argument_list|)
argument_list|)
expr_stmt|;
name|deletedSomething
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|deletedSomething
condition|)
name|region
operator|.
name|delete
argument_list|(
name|d
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|maxVersions
init|=
literal|1
init|;
name|maxVersions
operator|<=
name|TIMESTAMPS
operator|.
name|length
condition|;
operator|++
name|maxVersions
control|)
block|{
for|for
control|(
name|int
name|columnBitMask
init|=
literal|1
init|;
name|columnBitMask
operator|<=
name|MAX_COLUMN_BIT_MASK
condition|;
operator|++
name|columnBitMask
control|)
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
name|maxVersions
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|qualSet
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
block|{
name|int
name|columnMaskTmp
init|=
name|columnBitMask
decl_stmt|;
for|for
control|(
name|String
name|qual
range|:
name|qualifiers
control|)
block|{
if|if
condition|(
operator|(
name|columnMaskTmp
operator|&
literal|1
operator|)
operator|!=
literal|0
condition|)
block|{
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
name|qual
argument_list|)
argument_list|)
expr_stmt|;
name|qualSet
operator|.
name|add
argument_list|(
name|qual
argument_list|)
expr_stmt|;
block|}
name|columnMaskTmp
operator|>>=
literal|1
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|columnMaskTmp
argument_list|)
expr_stmt|;
block|}
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
name|int
name|kvPos
init|=
literal|0
decl_stmt|;
name|int
name|numResults
init|=
literal|0
decl_stmt|;
name|String
name|queryInfo
init|=
literal|"columns queried: "
operator|+
name|qualSet
operator|+
literal|" (columnBitMask="
operator|+
name|columnBitMask
operator|+
literal|"), maxVersions="
operator|+
name|maxVersions
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
operator|||
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
while|while
condition|(
name|kvPos
operator|<
name|kvs
operator|.
name|size
argument_list|()
operator|&&
operator|!
name|matchesQuery
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
name|kvPos
argument_list|)
argument_list|,
name|qualSet
argument_list|,
name|maxVersions
argument_list|,
name|lastDelTimeMap
argument_list|)
condition|)
block|{
operator|++
name|kvPos
expr_stmt|;
block|}
name|String
name|rowQual
init|=
name|getRowQualStr
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|String
name|deleteInfo
init|=
literal|""
decl_stmt|;
name|Long
name|lastDelTS
init|=
name|lastDelTimeMap
operator|.
name|get
argument_list|(
name|rowQual
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastDelTS
operator|!=
literal|null
condition|)
block|{
name|deleteInfo
operator|=
literal|"; last timestamp when row/column "
operator|+
name|rowQual
operator|+
literal|" was deleted: "
operator|+
name|lastDelTS
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Scanner returned additional key/value: "
operator|+
name|kv
operator|+
literal|", "
operator|+
name|queryInfo
operator|+
name|deleteInfo
operator|+
literal|";"
argument_list|,
name|kvPos
operator|<
name|kvs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Scanner returned wrong key/value; "
operator|+
name|queryInfo
operator|+
name|deleteInfo
operator|+
literal|";"
argument_list|,
name|kvs
operator|.
name|get
argument_list|(
name|kvPos
argument_list|)
argument_list|,
name|kv
argument_list|)
expr_stmt|;
operator|++
name|kvPos
expr_stmt|;
operator|++
name|numResults
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
for|for
control|(
init|;
name|kvPos
operator|<
name|kvs
operator|.
name|size
argument_list|()
condition|;
operator|++
name|kvPos
control|)
block|{
name|KeyValue
name|remainingKV
init|=
name|kvs
operator|.
name|get
argument_list|(
name|kvPos
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Matching column not returned by scanner: "
operator|+
name|remainingKV
operator|+
literal|", "
operator|+
name|queryInfo
operator|+
literal|", results returned: "
operator|+
name|numResults
argument_list|,
name|matchesQuery
argument_list|(
name|remainingKV
argument_list|,
name|qualSet
argument_list|,
name|maxVersions
argument_list|,
name|lastDelTimeMap
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
literal|"This test is supposed to delete at least some row/column "
operator|+
literal|"pairs"
argument_list|,
name|lastDelTimeMap
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of row/col pairs deleted at least once: "
operator|+
name|lastDelTimeMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|getRowQualStr
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
name|String
name|rowStr
init|=
name|Bytes
operator|.
name|toString
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
argument_list|)
decl_stmt|;
name|String
name|qualStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|rowStr
operator|+
literal|"_"
operator|+
name|qualStr
return|;
block|}
specifier|private
specifier|static
name|boolean
name|matchesQuery
parameter_list|(
name|KeyValue
name|kv
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|qualSet
parameter_list|,
name|int
name|maxVersions
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastDelTimeMap
parameter_list|)
block|{
name|Long
name|lastDelTS
init|=
name|lastDelTimeMap
operator|.
name|get
argument_list|(
name|getRowQualStr
argument_list|(
name|kv
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
name|kv
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
return|return
name|qualSet
operator|.
name|contains
argument_list|(
name|qualStr
argument_list|(
name|kv
argument_list|)
argument_list|)
operator|&&
name|ts
operator|>=
name|TIMESTAMPS
index|[
name|TIMESTAMPS
operator|.
name|length
operator|-
name|maxVersions
index|]
operator|&&
operator|(
name|lastDelTS
operator|==
literal|null
operator|||
name|ts
operator|>
name|lastDelTS
operator|)
return|;
block|}
specifier|private
specifier|static
name|String
name|qualStr
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|rowQualKey
parameter_list|(
name|String
name|row
parameter_list|,
name|String
name|qual
parameter_list|)
block|{
return|return
name|row
operator|+
literal|"_"
operator|+
name|qual
return|;
block|}
specifier|static
name|String
name|createValue
parameter_list|(
name|String
name|row
parameter_list|,
name|String
name|qual
parameter_list|,
name|long
name|ts
parameter_list|)
block|{
return|return
literal|"value_for_"
operator|+
name|row
operator|+
literal|"_"
operator|+
name|qual
operator|+
literal|"_"
operator|+
name|ts
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|sequentialStrings
parameter_list|(
name|String
name|prefix
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|lst
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
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
name|n
condition|;
operator|++
name|i
control|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|prefix
operator|+
name|i
argument_list|)
expr_stmt|;
comment|// Make column length depend on i.
name|int
name|iBitShifted
init|=
name|i
decl_stmt|;
while|while
condition|(
name|iBitShifted
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
name|iBitShifted
operator|&
literal|1
operator|)
operator|==
literal|0
condition|?
literal|'a'
else|:
literal|'b'
argument_list|)
expr_stmt|;
name|iBitShifted
operator|>>=
literal|1
expr_stmt|;
block|}
name|lst
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|lst
return|;
block|}
block|}
end_class

end_unit

