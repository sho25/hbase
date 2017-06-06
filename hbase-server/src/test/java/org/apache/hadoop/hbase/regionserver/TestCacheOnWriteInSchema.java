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
name|Random
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
name|BlockCacheKey
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
name|BlockType
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
name|HFileBlock
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
name|io
operator|.
name|hfile
operator|.
name|RandomKeyValueUtil
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|FSUtils
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
name|wal
operator|.
name|AbstractFSWALProvider
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
name|wal
operator|.
name|WALFactory
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
name|Rule
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
name|rules
operator|.
name|TestName
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
comment|/**  * Tests {@link HFile} cache-on-write functionality for data blocks, non-root  * index blocks, and Bloom filter blocks, as specified by the column family.  */
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
name|TestCacheOnWriteInSchema
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
name|TestCacheOnWriteInSchema
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestCacheOnWriteInSchema"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|table
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_KV
init|=
literal|25000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|12983177L
argument_list|)
decl_stmt|;
comment|/** The number of valid key types possible in a store file */
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
enum|enum
name|CacheOnWriteType
block|{
name|DATA_BLOCKS
parameter_list|(
name|BlockType
operator|.
name|DATA
parameter_list|,
name|BlockType
operator|.
name|ENCODED_DATA
parameter_list|)
operator|,
constructor|BLOOM_BLOCKS(BlockType.BLOOM_CHUNK
block|)
enum|,
name|INDEX_BLOCKS
parameter_list|(
name|BlockType
operator|.
name|LEAF_INDEX
parameter_list|,
name|BlockType
operator|.
name|INTERMEDIATE_INDEX
parameter_list|)
constructor_decl|;
specifier|private
specifier|final
name|BlockType
name|blockType1
decl_stmt|;
specifier|private
specifier|final
name|BlockType
name|blockType2
decl_stmt|;
specifier|private
name|CacheOnWriteType
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
name|this
argument_list|(
name|blockType
argument_list|,
name|blockType
argument_list|)
expr_stmt|;
block|}
specifier|private
name|CacheOnWriteType
parameter_list|(
name|BlockType
name|blockType1
parameter_list|,
name|BlockType
name|blockType2
parameter_list|)
block|{
name|this
operator|.
name|blockType1
operator|=
name|blockType1
expr_stmt|;
name|this
operator|.
name|blockType2
operator|=
name|blockType2
expr_stmt|;
block|}
specifier|public
name|boolean
name|shouldBeCached
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
return|return
name|blockType
operator|==
name|blockType1
operator|||
name|blockType
operator|==
name|blockType2
return|;
block|}
specifier|public
name|void
name|modifyFamilySchema
parameter_list|(
name|HColumnDescriptor
name|family
parameter_list|)
block|{
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|DATA_BLOCKS
case|:
name|family
operator|.
name|setCacheDataOnWrite
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
name|BLOOM_BLOCKS
case|:
name|family
operator|.
name|setCacheBloomsOnWrite
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
name|INDEX_BLOCKS
case|:
name|family
operator|.
name|setCacheIndexesOnWrite
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
end_class

begin_decl_stmt
specifier|private
specifier|final
name|CacheOnWriteType
name|cowType
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|Configuration
name|conf
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|final
name|String
name|testDescription
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|HRegion
name|region
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|HStore
name|store
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|WALFactory
name|walFactory
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
name|FileSystem
name|fs
decl_stmt|;
end_decl_stmt

begin_constructor
specifier|public
name|TestCacheOnWriteInSchema
parameter_list|(
name|CacheOnWriteType
name|cowType
parameter_list|)
block|{
name|this
operator|.
name|cowType
operator|=
name|cowType
expr_stmt|;
name|testDescription
operator|=
literal|"[cacheOnWrite="
operator|+
name|cowType
operator|+
literal|"]"
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
block|}
end_constructor

begin_function
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|getParameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|cowTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|CacheOnWriteType
name|cowType
range|:
name|CacheOnWriteType
operator|.
name|values
argument_list|()
control|)
block|{
name|cowTypes
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|cowType
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|cowTypes
return|;
block|}
end_function

begin_function
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
comment|// parameterized tests add [#] suffix get rid of [ and ].
name|table
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[\\[\\]]"
argument_list|,
literal|"_"
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
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
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_INDEX_BLOCKS_ON_WRITE_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOOM_BLOCKS_ON_WRITE_KEY
argument_list|,
literal|false
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
comment|// Create the schema
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|ROWCOL
argument_list|)
expr_stmt|;
name|cowType
operator|.
name|modifyFamilySchema
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
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
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
comment|// Create a store based on the schema
specifier|final
name|String
name|id
init|=
name|TestCacheOnWriteInSchema
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|logdir
init|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getWALDirectoryName
argument_list|(
name|id
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|logdir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|walFactory
operator|=
operator|new
name|WALFactory
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|region
operator|=
name|TEST_UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|info
argument_list|,
name|htd
argument_list|,
name|walFactory
operator|.
name|getWAL
argument_list|(
name|info
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|info
operator|.
name|getTable
argument_list|()
operator|.
name|getNamespace
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|store
operator|=
operator|new
name|HStore
argument_list|(
name|region
argument_list|,
name|hcd
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|IOException
name|ex
init|=
literal|null
decl_stmt|;
try|try
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
try|try
block|{
name|walFactory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|DIR
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not delete "
operator|+
name|DIR
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
if|if
condition|(
name|ex
operator|!=
literal|null
condition|)
block|{
throw|throw
name|ex
throw|;
block|}
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testCacheOnWriteInSchema
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Write some random data into the store
name|StoreFileWriter
name|writer
init|=
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HFile
operator|.
name|DEFAULT_COMPRESSION_ALGORITHM
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|writeStoreFile
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Verify the block types of interest were cached on write
name|readStoreFile
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
specifier|private
name|void
name|readStoreFile
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|CacheConfig
name|cacheConf
init|=
name|store
operator|.
name|getCacheConfig
argument_list|()
decl_stmt|;
name|BlockCache
name|cache
init|=
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
decl_stmt|;
name|StoreFile
name|sf
init|=
operator|new
name|HStoreFile
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|ROWCOL
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|sf
operator|.
name|initReader
argument_list|()
expr_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|getHFileReader
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Open a scanner with (on read) caching disabled
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
name|assertTrue
argument_list|(
name|testDescription
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|()
argument_list|)
expr_stmt|;
comment|// Cribbed from io.hfile.TestCacheOnWrite
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
comment|// Flags: don't cache the block, use pread, this is not a compaction.
comment|// Also, pass null for expected block type to avoid checking it.
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
name|DataBlockEncoding
operator|.
name|NONE
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
name|cache
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
name|boolean
name|shouldBeCached
init|=
name|cowType
operator|.
name|shouldBeCached
argument_list|(
name|block
operator|.
name|getBlockType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shouldBeCached
operator|!=
name|isCached
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"shouldBeCached: "
operator|+
name|shouldBeCached
operator|+
literal|"\n"
operator|+
literal|"isCached: "
operator|+
name|isCached
operator|+
literal|"\n"
operator|+
literal|"Test description: "
operator|+
name|testDescription
operator|+
literal|"\n"
operator|+
literal|"block: "
operator|+
name|block
operator|+
literal|"\n"
operator|+
literal|"blockCacheKey: "
operator|+
name|blockCacheKey
argument_list|)
throw|;
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
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_function

begin_function
specifier|private
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
end_function

begin_function
specifier|private
name|void
name|writeStoreFile
parameter_list|(
name|StoreFileWriter
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
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
name|rand
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
name|rand
argument_list|)
decl_stmt|;
name|int
name|cfLen
init|=
name|rand
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
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|,
name|generateKeyType
argument_list|(
name|rand
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
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
end_function

unit|}
end_unit

