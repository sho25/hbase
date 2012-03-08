begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNotNull
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
name|ArrayList
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
name|List
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
name|hfile
operator|.
name|BlockCache
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
name|NoOpDataBlockEncoder
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
name|TestHFileWriterV2
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
name|BloomFilterFactory
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
name|ByteBloomFilter
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
name|CompoundBloomFilter
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
name|CompoundBloomFilterBase
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
name|CompoundBloomFilterWriter
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

begin_comment
comment|/**  * Tests writing Bloom filter blocks in the same part of the file as data  * blocks.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCompoundBloomFilter
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
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCompoundBloomFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_TESTS
init|=
literal|9
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|BloomType
name|BLOOM_TYPES
index|[]
init|=
block|{
name|BloomType
operator|.
name|ROW
block|,
name|BloomType
operator|.
name|ROW
block|,
name|BloomType
operator|.
name|ROWCOL
block|,
name|BloomType
operator|.
name|ROWCOL
block|,
name|BloomType
operator|.
name|ROW
block|,
name|BloomType
operator|.
name|ROWCOL
block|,
name|BloomType
operator|.
name|ROWCOL
block|,
name|BloomType
operator|.
name|ROWCOL
block|,
name|BloomType
operator|.
name|ROW
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_KV
index|[]
decl_stmt|;
static|static
block|{
specifier|final
name|int
name|N
init|=
literal|10000
decl_stmt|;
comment|// Only used in initialization.
name|NUM_KV
operator|=
operator|new
name|int
index|[]
block|{
literal|21870
block|,
name|N
block|,
name|N
block|,
name|N
block|,
name|N
block|,
literal|1000
block|,
name|N
block|,
literal|7500
block|,
literal|7500
block|}
expr_stmt|;
assert|assert
name|NUM_KV
operator|.
name|length
operator|==
name|NUM_TESTS
assert|;
block|}
specifier|private
specifier|static
specifier|final
name|int
name|BLOCK_SIZES
index|[]
decl_stmt|;
static|static
block|{
specifier|final
name|int
name|blkSize
init|=
literal|65536
decl_stmt|;
name|BLOCK_SIZES
operator|=
operator|new
name|int
index|[]
block|{
literal|512
block|,
literal|1000
block|,
name|blkSize
block|,
name|blkSize
block|,
name|blkSize
block|,
literal|128
block|,
literal|300
block|,
name|blkSize
block|,
name|blkSize
block|}
expr_stmt|;
assert|assert
name|BLOCK_SIZES
operator|.
name|length
operator|==
name|NUM_TESTS
assert|;
block|}
comment|/**    * Be careful not to specify too high a Bloom filter block size, otherwise    * there will only be one oversized chunk and the observed false positive    * rate will be too low.    */
specifier|private
specifier|static
specifier|final
name|int
name|BLOOM_BLOCK_SIZES
index|[]
init|=
block|{
literal|1000
block|,
literal|4096
block|,
literal|4096
block|,
literal|4096
block|,
literal|8192
block|,
literal|128
block|,
literal|1024
block|,
literal|600
block|,
literal|600
block|}
decl_stmt|;
static|static
block|{
assert|assert
name|BLOOM_BLOCK_SIZES
operator|.
name|length
operator|==
name|NUM_TESTS
assert|;
block|}
specifier|private
specifier|static
specifier|final
name|double
name|TARGET_ERROR_RATES
index|[]
init|=
block|{
literal|0.025
block|,
literal|0.01
block|,
literal|0.015
block|,
literal|0.01
block|,
literal|0.03
block|,
literal|0.01
block|,
literal|0.01
block|,
literal|0.07
block|,
literal|0.07
block|}
decl_stmt|;
static|static
block|{
assert|assert
name|TARGET_ERROR_RATES
operator|.
name|length
operator|==
name|NUM_TESTS
assert|;
block|}
comment|/** A false positive rate that is obviously too high. */
specifier|private
specifier|static
specifier|final
name|double
name|TOO_HIGH_ERROR_RATE
decl_stmt|;
static|static
block|{
name|double
name|m
init|=
literal|0
decl_stmt|;
for|for
control|(
name|double
name|errorRate
range|:
name|TARGET_ERROR_RATES
control|)
name|m
operator|=
name|Math
operator|.
name|max
argument_list|(
name|m
argument_list|,
name|errorRate
argument_list|)
expr_stmt|;
name|TOO_HIGH_ERROR_RATE
operator|=
name|m
operator|+
literal|0.03
expr_stmt|;
block|}
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|CacheConfig
name|cacheConf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|BlockCache
name|blockCache
decl_stmt|;
comment|/** A message of the form "in test#<number>:" to include in logging. */
specifier|private
name|String
name|testIdMsg
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|GENERATION_SEED
init|=
literal|2319
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EVALUATION_SEED
init|=
literal|135
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
comment|// This test requires the most recent HFile format (i.e. v2).
name|conf
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
name|HFile
operator|.
name|MAX_FORMAT_VERSION
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cacheConf
operator|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|blockCache
operator|=
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|blockCache
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|createSortedKeyValues
parameter_list|(
name|Random
name|rand
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|n
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
name|n
condition|;
operator|++
name|i
control|)
name|kvList
operator|.
name|add
argument_list|(
name|TestHFileWriterV2
operator|.
name|randomKeyValue
argument_list|(
name|rand
argument_list|)
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|kvList
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
return|return
name|kvList
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompoundBloomFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|BloomFilterFactory
operator|.
name|IO_STOREFILE_BLOOM_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|t
init|=
literal|0
init|;
name|t
operator|<
name|NUM_TESTS
condition|;
operator|++
name|t
control|)
block|{
name|conf
operator|.
name|setFloat
argument_list|(
name|BloomFilterFactory
operator|.
name|IO_STOREFILE_BLOOM_ERROR_RATE
argument_list|,
operator|(
name|float
operator|)
name|TARGET_ERROR_RATES
index|[
name|t
index|]
argument_list|)
expr_stmt|;
name|testIdMsg
operator|=
literal|"in test #"
operator|+
name|t
operator|+
literal|":"
expr_stmt|;
name|Random
name|generationRand
init|=
operator|new
name|Random
argument_list|(
name|GENERATION_SEED
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|createSortedKeyValues
argument_list|(
name|generationRand
argument_list|,
name|NUM_KV
index|[
name|t
index|]
argument_list|)
decl_stmt|;
name|BloomType
name|bt
init|=
name|BLOOM_TYPES
index|[
name|t
index|]
decl_stmt|;
name|Path
name|sfPath
init|=
name|writeStoreFile
argument_list|(
name|t
argument_list|,
name|bt
argument_list|,
name|kvs
argument_list|)
decl_stmt|;
name|readStoreFile
argument_list|(
name|t
argument_list|,
name|bt
argument_list|,
name|kvs
argument_list|,
name|sfPath
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Validates the false positive ratio by computing its z-value and comparing    * it to the provided threshold.    *    * @param falsePosRate experimental positive rate    * @param nTrials the number of Bloom filter checks    * @param zValueBoundary z-value boundary, positive for an upper bound and    *          negative for a lower bound    * @param cbf the compound Bloom filter we are using    * @param additionalMsg additional message to include in log output and    *          assertion failures    */
specifier|private
name|void
name|validateFalsePosRate
parameter_list|(
name|double
name|falsePosRate
parameter_list|,
name|int
name|nTrials
parameter_list|,
name|double
name|zValueBoundary
parameter_list|,
name|CompoundBloomFilter
name|cbf
parameter_list|,
name|String
name|additionalMsg
parameter_list|)
block|{
name|double
name|p
init|=
name|BloomFilterFactory
operator|.
name|getErrorRate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|double
name|zValue
init|=
operator|(
name|falsePosRate
operator|-
name|p
operator|)
operator|/
name|Math
operator|.
name|sqrt
argument_list|(
name|p
operator|*
operator|(
literal|1
operator|-
name|p
operator|)
operator|/
name|nTrials
argument_list|)
decl_stmt|;
name|String
name|assortedStatsStr
init|=
literal|" (targetErrorRate="
operator|+
name|p
operator|+
literal|", falsePosRate="
operator|+
name|falsePosRate
operator|+
literal|", nTrials="
operator|+
name|nTrials
operator|+
literal|")"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"z-value is "
operator|+
name|zValue
operator|+
name|assortedStatsStr
argument_list|)
expr_stmt|;
name|boolean
name|isUpperBound
init|=
name|zValueBoundary
operator|>
literal|0
decl_stmt|;
if|if
condition|(
name|isUpperBound
operator|&&
name|zValue
operator|>
name|zValueBoundary
operator|||
operator|!
name|isUpperBound
operator|&&
name|zValue
operator|<
name|zValueBoundary
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"False positive rate z-value "
operator|+
name|zValue
operator|+
literal|" is "
operator|+
operator|(
name|isUpperBound
condition|?
literal|"higher"
else|:
literal|"lower"
operator|)
operator|+
literal|" than "
operator|+
name|zValueBoundary
operator|+
name|assortedStatsStr
operator|+
literal|". Per-chunk stats:\n"
operator|+
name|cbf
operator|.
name|formatTestingStats
argument_list|()
decl_stmt|;
name|fail
argument_list|(
name|errorMsg
operator|+
name|additionalMsg
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readStoreFile
parameter_list|(
name|int
name|t
parameter_list|,
name|BloomType
name|bt
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|,
name|Path
name|sfPath
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreFile
name|sf
init|=
operator|new
name|StoreFile
argument_list|(
name|fs
argument_list|,
name|sfPath
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|bt
argument_list|,
name|NoOpDataBlockEncoder
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|StoreFile
operator|.
name|Reader
name|r
init|=
name|sf
operator|.
name|createReader
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|pread
init|=
literal|true
decl_stmt|;
comment|// does not really matter
name|StoreFileScanner
name|scanner
init|=
name|r
operator|.
name|getStoreFileScanner
argument_list|(
literal|true
argument_list|,
name|pread
argument_list|)
decl_stmt|;
block|{
comment|// Test for false negatives (not allowed).
name|int
name|numChecked
init|=
literal|0
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|byte
index|[]
name|row
init|=
name|kv
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|boolean
name|present
init|=
name|isInBloom
argument_list|(
name|scanner
argument_list|,
name|row
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|testIdMsg
operator|+
literal|" Bloom filter false negative on row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|" after "
operator|+
name|numChecked
operator|+
literal|" successful checks"
argument_list|,
name|present
argument_list|)
expr_stmt|;
operator|++
name|numChecked
expr_stmt|;
block|}
block|}
comment|// Test for false positives (some percentage allowed). We test in two modes:
comment|// "fake lookup" which ignores the key distribution, and production mode.
for|for
control|(
name|boolean
name|fakeLookupEnabled
range|:
operator|new
name|boolean
index|[]
block|{
literal|true
block|,
literal|false
block|}
control|)
block|{
name|ByteBloomFilter
operator|.
name|setFakeLookupMode
argument_list|(
name|fakeLookupEnabled
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|fakeLookupModeStr
init|=
literal|", fake lookup is "
operator|+
operator|(
name|fakeLookupEnabled
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
decl_stmt|;
name|CompoundBloomFilter
name|cbf
init|=
operator|(
name|CompoundBloomFilter
operator|)
name|r
operator|.
name|getGeneralBloomFilter
argument_list|()
decl_stmt|;
name|cbf
operator|.
name|enableTestingStats
argument_list|()
expr_stmt|;
name|int
name|numFalsePos
init|=
literal|0
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|EVALUATION_SEED
argument_list|)
decl_stmt|;
name|int
name|nTrials
init|=
name|NUM_KV
index|[
name|t
index|]
operator|*
literal|10
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
name|nTrials
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|query
init|=
name|TestHFileWriterV2
operator|.
name|randomRowOrQualifier
argument_list|(
name|rand
argument_list|)
decl_stmt|;
if|if
condition|(
name|isInBloom
argument_list|(
name|scanner
argument_list|,
name|query
argument_list|,
name|bt
argument_list|,
name|rand
argument_list|)
condition|)
block|{
name|numFalsePos
operator|+=
literal|1
expr_stmt|;
block|}
block|}
name|double
name|falsePosRate
init|=
name|numFalsePos
operator|*
literal|1.0
operator|/
name|nTrials
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|testIdMsg
operator|+
literal|" False positives: %d out of %d (%f)"
argument_list|,
name|numFalsePos
argument_list|,
name|nTrials
argument_list|,
name|falsePosRate
argument_list|)
operator|+
name|fakeLookupModeStr
argument_list|)
expr_stmt|;
comment|// Check for obvious Bloom filter crashes.
name|assertTrue
argument_list|(
literal|"False positive is too high: "
operator|+
name|falsePosRate
operator|+
literal|" (greater "
operator|+
literal|"than "
operator|+
name|TOO_HIGH_ERROR_RATE
operator|+
literal|")"
operator|+
name|fakeLookupModeStr
argument_list|,
name|falsePosRate
operator|<
name|TOO_HIGH_ERROR_RATE
argument_list|)
expr_stmt|;
comment|// Now a more precise check to see if the false positive rate is not
comment|// too high. The reason we use a relaxed restriction for the real-world
comment|// case as opposed to the "fake lookup" case is that our hash functions
comment|// are not completely independent.
name|double
name|maxZValue
init|=
name|fakeLookupEnabled
condition|?
literal|1.96
else|:
literal|2.5
decl_stmt|;
name|validateFalsePosRate
argument_list|(
name|falsePosRate
argument_list|,
name|nTrials
argument_list|,
name|maxZValue
argument_list|,
name|cbf
argument_list|,
name|fakeLookupModeStr
argument_list|)
expr_stmt|;
comment|// For checking the lower bound we need to eliminate the last chunk,
comment|// because it is frequently smaller and the false positive rate in it
comment|// is too low. This does not help if there is only one under-sized
comment|// chunk, though.
name|int
name|nChunks
init|=
name|cbf
operator|.
name|getNumChunks
argument_list|()
decl_stmt|;
if|if
condition|(
name|nChunks
operator|>
literal|1
condition|)
block|{
name|numFalsePos
operator|-=
name|cbf
operator|.
name|getNumPositivesForTesting
argument_list|(
name|nChunks
operator|-
literal|1
argument_list|)
expr_stmt|;
name|nTrials
operator|-=
name|cbf
operator|.
name|getNumQueriesForTesting
argument_list|(
name|nChunks
operator|-
literal|1
argument_list|)
expr_stmt|;
name|falsePosRate
operator|=
name|numFalsePos
operator|*
literal|1.0
operator|/
name|nTrials
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testIdMsg
operator|+
literal|" False positive rate without last chunk is "
operator|+
name|falsePosRate
operator|+
name|fakeLookupModeStr
argument_list|)
expr_stmt|;
block|}
name|validateFalsePosRate
argument_list|(
name|falsePosRate
argument_list|,
name|nTrials
argument_list|,
operator|-
literal|2.58
argument_list|,
name|cbf
argument_list|,
name|fakeLookupModeStr
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|ByteBloomFilter
operator|.
name|setFakeLookupMode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|r
operator|.
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// end of test so evictOnClose
block|}
specifier|private
name|boolean
name|isInBloom
parameter_list|(
name|StoreFileScanner
name|scanner
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|BloomType
name|bt
parameter_list|,
name|Random
name|rand
parameter_list|)
block|{
return|return
name|isInBloom
argument_list|(
name|scanner
argument_list|,
name|row
argument_list|,
name|TestHFileWriterV2
operator|.
name|randomRowOrQualifier
argument_list|(
name|rand
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isInBloom
parameter_list|(
name|StoreFileScanner
name|scanner
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|row
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
return|return
name|scanner
operator|.
name|shouldUseScanner
argument_list|(
name|scan
argument_list|,
name|columns
argument_list|,
name|Long
operator|.
name|MIN_VALUE
argument_list|)
return|;
block|}
specifier|private
name|Path
name|writeStoreFile
parameter_list|(
name|int
name|t
parameter_list|,
name|BloomType
name|bt
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|BloomFilterFactory
operator|.
name|IO_STOREFILE_BLOOM_BLOCK_SIZE
argument_list|,
name|BLOOM_BLOCK_SIZES
index|[
name|t
index|]
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|cacheConf
operator|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|StoreFile
operator|.
name|Writer
name|w
init|=
operator|new
name|StoreFile
operator|.
name|WriterBuilder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|,
name|BLOCK_SIZES
index|[
name|t
index|]
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
operator|.
name|withBloomType
argument_list|(
name|bt
argument_list|)
operator|.
name|withChecksumType
argument_list|(
name|HFile
operator|.
name|DEFAULT_CHECKSUM_TYPE
argument_list|)
operator|.
name|withBytesPerChecksum
argument_list|(
name|HFile
operator|.
name|DEFAULT_BYTES_PER_CHECKSUM
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|w
operator|.
name|hasGeneralBloom
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|w
operator|.
name|getGeneralBloomWriter
argument_list|()
operator|instanceof
name|CompoundBloomFilterWriter
argument_list|)
expr_stmt|;
name|CompoundBloomFilterWriter
name|cbbf
init|=
operator|(
name|CompoundBloomFilterWriter
operator|)
name|w
operator|.
name|getGeneralBloomWriter
argument_list|()
decl_stmt|;
name|int
name|keyCount
init|=
literal|0
decl_stmt|;
name|KeyValue
name|prev
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Total keys/values to insert: "
operator|+
name|kvs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|w
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
comment|// Validate the key count in the Bloom filter.
name|boolean
name|newKey
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|newKey
operator|=
operator|!
operator|(
name|bt
operator|==
name|BloomType
operator|.
name|ROW
condition|?
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|matchingRows
argument_list|(
name|kv
argument_list|,
name|prev
argument_list|)
else|:
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|matchingRowColumn
argument_list|(
name|kv
argument_list|,
name|prev
argument_list|)
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|newKey
condition|)
operator|++
name|keyCount
expr_stmt|;
name|assertEquals
argument_list|(
name|keyCount
argument_list|,
name|cbbf
operator|.
name|getKeyCount
argument_list|()
argument_list|)
expr_stmt|;
name|prev
operator|=
name|kv
expr_stmt|;
block|}
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|w
operator|.
name|getPath
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompoundBloomSizing
parameter_list|()
block|{
name|int
name|bloomBlockByteSize
init|=
literal|4096
decl_stmt|;
name|int
name|bloomBlockBitSize
init|=
name|bloomBlockByteSize
operator|*
literal|8
decl_stmt|;
name|double
name|targetErrorRate
init|=
literal|0.01
decl_stmt|;
name|long
name|maxKeysPerChunk
init|=
name|ByteBloomFilter
operator|.
name|idealMaxKeys
argument_list|(
name|bloomBlockBitSize
argument_list|,
name|targetErrorRate
argument_list|)
decl_stmt|;
name|long
name|bloomSize1
init|=
name|bloomBlockByteSize
operator|*
literal|8
decl_stmt|;
name|long
name|bloomSize2
init|=
name|ByteBloomFilter
operator|.
name|computeBitSize
argument_list|(
name|maxKeysPerChunk
argument_list|,
name|targetErrorRate
argument_list|)
decl_stmt|;
name|double
name|bloomSizeRatio
init|=
operator|(
name|bloomSize2
operator|*
literal|1.0
operator|/
name|bloomSize1
operator|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|bloomSizeRatio
operator|-
literal|0.9999
argument_list|)
operator|<
literal|0.0001
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateKey
parameter_list|()
block|{
name|CompoundBloomFilterBase
name|cbfb
init|=
operator|new
name|CompoundBloomFilterBase
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
literal|"myRow"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
literal|"myQualifier"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|rowKey
init|=
name|cbfb
operator|.
name|createBloomKey
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|row
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|byte
index|[]
name|rowColKey
init|=
name|cbfb
operator|.
name|createBloomKey
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|qualifier
argument_list|,
literal|0
argument_list|,
name|qualifier
operator|.
name|length
argument_list|)
decl_stmt|;
name|KeyValue
name|rowKV
init|=
name|KeyValue
operator|.
name|createKeyValueFromKey
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|KeyValue
name|rowColKV
init|=
name|KeyValue
operator|.
name|createKeyValueFromKey
argument_list|(
name|rowColKey
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rowKV
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|rowColKV
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rowKV
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rowColKV
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rowKV
operator|.
name|getQualifier
argument_list|()
operator|.
name|length
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

