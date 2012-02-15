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
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|HeapSize
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
name|encoding
operator|.
name|RedundantKVGenerator
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
name|metrics
operator|.
name|SchemaConfigured
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
name|metrics
operator|.
name|SchemaMetrics
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
name|Pair
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
name|TestHFileDataBlockEncoder
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|HFileDataBlockEncoderImpl
name|blockEncoder
decl_stmt|;
specifier|private
name|RedundantKVGenerator
name|generator
init|=
operator|new
name|RedundantKVGenerator
argument_list|()
decl_stmt|;
specifier|private
name|SchemaConfigured
name|UNKNOWN_TABLE_AND_CF
init|=
name|SchemaConfigured
operator|.
name|createUnknown
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|includesMemstoreTS
decl_stmt|;
comment|/**    * Create test for given data block encoding configuration.    * @param blockEncoder What kind of encoding policy will be used.    */
specifier|public
name|TestHFileDataBlockEncoder
parameter_list|(
name|HFileDataBlockEncoderImpl
name|blockEncoder
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
name|this
operator|.
name|blockEncoder
operator|=
name|blockEncoder
expr_stmt|;
name|this
operator|.
name|includesMemstoreTS
operator|=
name|includesMemstoreTS
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"On-disk encoding: "
operator|+
name|blockEncoder
operator|.
name|getEncodingOnDisk
argument_list|()
operator|+
literal|", in-cache encoding: "
operator|+
name|blockEncoder
operator|.
name|getEncodingInCache
argument_list|()
operator|+
literal|", includesMemstoreTS: "
operator|+
name|includesMemstoreTS
argument_list|)
expr_stmt|;
block|}
comment|/**    * Preparation before JUnit test.    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|SchemaMetrics
operator|.
name|configureGlobally
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Cleanup after JUnit test.    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test putting and taking out blocks into cache with different    * encoding options.    */
annotation|@
name|Test
specifier|public
name|void
name|testEncodingWithCache
parameter_list|()
block|{
name|HFileBlock
name|block
init|=
name|getSampleHFileBlock
argument_list|()
decl_stmt|;
name|LruBlockCache
name|blockCache
init|=
operator|new
name|LruBlockCache
argument_list|(
literal|8
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|32
operator|*
literal|1024
argument_list|)
decl_stmt|;
name|HFileBlock
name|cacheBlock
init|=
name|blockEncoder
operator|.
name|diskToCacheFormat
argument_list|(
name|block
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BlockCacheKey
name|cacheKey
init|=
operator|new
name|BlockCacheKey
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|blockCache
operator|.
name|cacheBlock
argument_list|(
name|cacheKey
argument_list|,
name|cacheBlock
argument_list|)
expr_stmt|;
name|HeapSize
name|heapSize
init|=
name|blockCache
operator|.
name|getBlock
argument_list|(
name|cacheKey
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|heapSize
operator|instanceof
name|HFileBlock
argument_list|)
expr_stmt|;
name|HFileBlock
name|returnedBlock
init|=
operator|(
name|HFileBlock
operator|)
name|heapSize
decl_stmt|;
empty_stmt|;
if|if
condition|(
name|blockEncoder
operator|.
name|getEncodingInCache
argument_list|()
operator|==
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
name|assertEquals
argument_list|(
name|block
operator|.
name|getBufferWithHeader
argument_list|()
argument_list|,
name|returnedBlock
operator|.
name|getBufferWithHeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|BlockType
operator|.
name|ENCODED_DATA
operator|!=
name|returnedBlock
operator|.
name|getBlockType
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|blockEncoder
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|,
name|returnedBlock
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test writing to disk.    */
annotation|@
name|Test
specifier|public
name|void
name|testEncodingWritePath
parameter_list|()
block|{
comment|// usually we have just block without headers, but don't complicate that
name|HFileBlock
name|block
init|=
name|getSampleHFileBlock
argument_list|()
decl_stmt|;
name|Pair
argument_list|<
name|ByteBuffer
argument_list|,
name|BlockType
argument_list|>
name|result
init|=
name|blockEncoder
operator|.
name|beforeWriteToDisk
argument_list|(
name|block
operator|.
name|getBufferWithoutHeader
argument_list|()
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|result
operator|.
name|getFirst
argument_list|()
operator|.
name|limit
argument_list|()
operator|-
name|HFileBlock
operator|.
name|HEADER_SIZE
decl_stmt|;
name|HFileBlock
name|blockOnDisk
init|=
operator|new
name|HFileBlock
argument_list|(
name|result
operator|.
name|getSecond
argument_list|()
argument_list|,
name|size
argument_list|,
name|size
argument_list|,
operator|-
literal|1
argument_list|,
name|result
operator|.
name|getFirst
argument_list|()
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
literal|0
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockEncoder
operator|.
name|getEncodingOnDisk
argument_list|()
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|ENCODED_DATA
argument_list|,
name|blockOnDisk
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|blockEncoder
operator|.
name|getEncodingOnDisk
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|blockOnDisk
operator|.
name|getDataBlockEncodingId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|blockOnDisk
operator|.
name|getBlockType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test converting blocks from disk to cache format.    */
annotation|@
name|Test
specifier|public
name|void
name|testEncodingReadPath
parameter_list|()
block|{
name|HFileBlock
name|origBlock
init|=
name|getSampleHFileBlock
argument_list|()
decl_stmt|;
name|blockEncoder
operator|.
name|diskToCacheFormat
argument_list|(
name|origBlock
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HFileBlock
name|getSampleHFileBlock
parameter_list|()
block|{
name|ByteBuffer
name|keyValues
init|=
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
literal|60
argument_list|)
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|keyValues
operator|.
name|limit
argument_list|()
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
operator|+
name|HFileBlock
operator|.
name|HEADER_SIZE
argument_list|)
decl_stmt|;
name|buf
operator|.
name|position
argument_list|(
name|HFileBlock
operator|.
name|HEADER_SIZE
argument_list|)
expr_stmt|;
name|keyValues
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|keyValues
argument_list|)
expr_stmt|;
name|HFileBlock
name|b
init|=
operator|new
name|HFileBlock
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
name|size
argument_list|,
name|size
argument_list|,
operator|-
literal|1
argument_list|,
name|buf
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
literal|0
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
name|UNKNOWN_TABLE_AND_CF
operator|.
name|passSchemaMetricsTo
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
comment|/**    * @return All possible data block encoding configurations    */
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|getAllConfigurations
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|configurations
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
name|DataBlockEncoding
name|diskAlgo
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|DataBlockEncoding
name|cacheAlgo
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|diskAlgo
operator|!=
name|cacheAlgo
operator|&&
name|diskAlgo
operator|!=
name|DataBlockEncoding
operator|.
name|NONE
condition|)
block|{
comment|// We allow (1) the same encoding on disk and in cache, and
comment|// (2) some encoding in cache but no encoding on disk (for testing).
continue|continue;
block|}
for|for
control|(
name|boolean
name|includesMemstoreTS
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
name|configurations
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|HFileDataBlockEncoderImpl
argument_list|(
name|diskAlgo
argument_list|,
name|cacheAlgo
argument_list|)
block|,
operator|new
name|Boolean
argument_list|(
name|includesMemstoreTS
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|configurations
return|;
block|}
block|}
end_class

end_unit

