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
name|*
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
name|Random
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
name|CellComparator
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
name|fs
operator|.
name|HFileSystem
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
name|StoreFileWriter
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
name|SmallTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestPrefetch
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
name|int
name|NUM_VALID_KEY_TYPES
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DATA_BLOCK_SIZE
init|=
literal|2048
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_KV
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Random
name|RNG
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|CacheConfig
name|cacheConf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
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
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|PREFETCH_BLOCKS_ON_OPEN_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|=
name|HFileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|CacheConfig
operator|.
name|blockCacheDisabled
operator|=
literal|false
expr_stmt|;
name|cacheConf
operator|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testPrefetch
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|storeFile
init|=
name|writeStoreFile
argument_list|()
decl_stmt|;
name|readStoreFile
argument_list|(
name|storeFile
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|readStoreFile
parameter_list|(
name|Path
name|storeFilePath
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Open the file
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|storeFilePath
argument_list|,
name|cacheConf
argument_list|,
name|conf
argument_list|)
decl_stmt|;
while|while
condition|(
operator|!
name|reader
operator|.
name|prefetchComplete
argument_list|()
condition|)
block|{
comment|// Sleep for a bit
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|// Check that all of the data blocks were preloaded
name|BlockCache
name|blockCache
init|=
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|long
name|offset
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|reader
operator|.
name|getTrailer
argument_list|()
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
condition|)
block|{
name|HFileBlock
name|block
init|=
name|reader
operator|.
name|readBlock
argument_list|(
name|offset
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|BlockCacheKey
name|blockCacheKey
init|=
operator|new
name|BlockCacheKey
argument_list|(
name|reader
operator|.
name|getName
argument_list|()
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|boolean
name|isCached
init|=
name|blockCache
operator|.
name|getBlock
argument_list|(
name|blockCacheKey
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
operator|!=
literal|null
decl_stmt|;
if|if
condition|(
name|block
operator|.
name|getBlockType
argument_list|()
operator|==
name|BlockType
operator|.
name|DATA
operator|||
name|block
operator|.
name|getBlockType
argument_list|()
operator|==
name|BlockType
operator|.
name|ROOT_INDEX
operator|||
name|block
operator|.
name|getBlockType
argument_list|()
operator|==
name|BlockType
operator|.
name|INTERMEDIATE_INDEX
condition|)
block|{
name|assertTrue
argument_list|(
name|isCached
argument_list|)
expr_stmt|;
block|}
name|offset
operator|+=
name|block
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Path
name|writeStoreFile
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|storeFileParentDir
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"TestPrefetch"
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
name|DATA_BLOCK_SIZE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
name|sfw
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|storeFileParentDir
argument_list|)
operator|.
name|withComparator
argument_list|(
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|int
name|rowLen
init|=
literal|32
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
name|NUM_KV
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|k
init|=
name|RandomKeyValueUtil
operator|.
name|randomOrderedKey
argument_list|(
name|RNG
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|byte
index|[]
name|v
init|=
name|RandomKeyValueUtil
operator|.
name|randomValue
argument_list|(
name|RNG
argument_list|)
decl_stmt|;
name|int
name|cfLen
init|=
name|RNG
operator|.
name|nextInt
argument_list|(
name|k
operator|.
name|length
operator|-
name|rowLen
operator|+
literal|1
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|k
argument_list|,
literal|0
argument_list|,
name|rowLen
argument_list|,
name|k
argument_list|,
name|rowLen
argument_list|,
name|cfLen
argument_list|,
name|k
argument_list|,
name|rowLen
operator|+
name|cfLen
argument_list|,
name|k
operator|.
name|length
operator|-
name|rowLen
operator|-
name|cfLen
argument_list|,
name|RNG
operator|.
name|nextLong
argument_list|()
argument_list|,
name|generateKeyType
argument_list|(
name|RNG
argument_list|)
argument_list|,
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|)
decl_stmt|;
name|sfw
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|sfw
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|sfw
operator|.
name|getPath
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|KeyValue
operator|.
name|Type
name|generateKeyType
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
if|if
condition|(
name|rand
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// Let's make half of KVs puts.
return|return
name|KeyValue
operator|.
name|Type
operator|.
name|Put
return|;
block|}
else|else
block|{
name|KeyValue
operator|.
name|Type
name|keyType
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|values
argument_list|()
index|[
literal|1
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
name|NUM_VALID_KEY_TYPES
argument_list|)
index|]
decl_stmt|;
if|if
condition|(
name|keyType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Minimum
operator|||
name|keyType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Maximum
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Generated an invalid key type: "
operator|+
name|keyType
operator|+
literal|". "
operator|+
literal|"Probably the layout of KeyValue.Type has changed."
argument_list|)
throw|;
block|}
return|return
name|keyType
return|;
block|}
block|}
block|}
end_class

end_unit

