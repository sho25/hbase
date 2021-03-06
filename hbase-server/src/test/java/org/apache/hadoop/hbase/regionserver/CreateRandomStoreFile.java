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
name|Arrays
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
name|HBaseConfiguration
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
name|HFileBlockIndex
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
name|HFileContext
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
name|HFileContextBuilder
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
name|BloomFilterUtil
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLineParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|HelpFormatter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|ParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|PosixParser
import|;
end_import

begin_comment
comment|/**  * Creates an HFile with random key/value pairs.  */
end_comment

begin_class
specifier|public
class|class
name|CreateRandomStoreFile
block|{
comment|/**    * As much as this number of bytes can be added or subtracted from key/value    * lengths.    */
specifier|private
specifier|static
specifier|final
name|int
name|LEN_VARIATION
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CreateRandomStoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OUTPUT_DIR_OPTION
init|=
literal|"o"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NUM_KV_OPTION
init|=
literal|"n"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HFILE_VERSION_OPTION
init|=
literal|"h"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KEY_SIZE_OPTION
init|=
literal|"k"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_SIZE_OPTION
init|=
literal|"v"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMPRESSION_OPTION
init|=
literal|"c"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BLOOM_FILTER_OPTION
init|=
literal|"bf"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BLOOM_FILTER_PARAM_OPTION
init|=
literal|"bfp"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BLOCK_SIZE_OPTION
init|=
literal|"bs"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BLOOM_BLOCK_SIZE_OPTION
init|=
literal|"bfbs"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_BLOCK_SIZE_OPTION
init|=
literal|"ibs"
decl_stmt|;
comment|/** The exit code this command-line tool returns on failure */
specifier|private
specifier|static
specifier|final
name|int
name|EXIT_FAILURE
init|=
literal|1
decl_stmt|;
comment|/** The number of valid key types in a store file */
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
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
specifier|private
name|int
name|keyPrefixLen
decl_stmt|,
name|keyLen
decl_stmt|,
name|rowLen
decl_stmt|,
name|cfLen
decl_stmt|,
name|valueLen
decl_stmt|;
specifier|private
name|Random
name|rand
decl_stmt|;
comment|/**    * Runs the tools.    *    * @param args command-line arguments    * @return true in case of success    * @throws IOException    */
specifier|public
name|boolean
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|options
operator|.
name|addOption
argument_list|(
name|OUTPUT_DIR_OPTION
argument_list|,
literal|"output_dir"
argument_list|,
literal|true
argument_list|,
literal|"Output directory"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|NUM_KV_OPTION
argument_list|,
literal|"num_kv"
argument_list|,
literal|true
argument_list|,
literal|"Number of key/value pairs"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|KEY_SIZE_OPTION
argument_list|,
literal|"key_size"
argument_list|,
literal|true
argument_list|,
literal|"Average key size"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|VALUE_SIZE_OPTION
argument_list|,
literal|"value_size"
argument_list|,
literal|true
argument_list|,
literal|"Average value size"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|HFILE_VERSION_OPTION
argument_list|,
literal|"hfile_version"
argument_list|,
literal|true
argument_list|,
literal|"HFile version to create"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|COMPRESSION_OPTION
argument_list|,
literal|"compression"
argument_list|,
literal|true
argument_list|,
literal|" Compression type, one of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|BLOOM_FILTER_OPTION
argument_list|,
literal|"bloom_filter"
argument_list|,
literal|true
argument_list|,
literal|"Bloom filter type, one of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|BloomType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|BLOOM_FILTER_PARAM_OPTION
argument_list|,
literal|"bloom_param"
argument_list|,
literal|true
argument_list|,
literal|"the parameter of the bloom filter"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|BLOCK_SIZE_OPTION
argument_list|,
literal|"block_size"
argument_list|,
literal|true
argument_list|,
literal|"HFile block size"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|BLOOM_BLOCK_SIZE_OPTION
argument_list|,
literal|"bloom_block_size"
argument_list|,
literal|true
argument_list|,
literal|"Compound Bloom filters block size"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|INDEX_BLOCK_SIZE_OPTION
argument_list|,
literal|"index_block_size"
argument_list|,
literal|true
argument_list|,
literal|"Index block size"
argument_list|)
expr_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
name|CreateRandomStoreFile
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|CommandLineParser
name|parser
init|=
operator|new
name|PosixParser
argument_list|()
decl_stmt|;
name|CommandLine
name|cmdLine
decl_stmt|;
try|try
block|{
name|cmdLine
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|OUTPUT_DIR_OPTION
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Output directory is not specified"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|NUM_KV_OPTION
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The number of keys/values not specified"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|KEY_SIZE_OPTION
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Key size is not specified"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|VALUE_SIZE_OPTION
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Value size not specified"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Path
name|outputDir
init|=
operator|new
name|Path
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|OUTPUT_DIR_OPTION
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|numKV
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|NUM_KV_OPTION
argument_list|)
argument_list|)
decl_stmt|;
name|configureKeyValue
argument_list|(
name|numKV
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|KEY_SIZE_OPTION
argument_list|)
argument_list|)
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|VALUE_SIZE_OPTION
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Compression
operator|.
name|Algorithm
name|compr
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
decl_stmt|;
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|COMPRESSION_OPTION
argument_list|)
condition|)
block|{
name|compr
operator|=
name|Compression
operator|.
name|Algorithm
operator|.
name|valueOf
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|COMPRESSION_OPTION
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BloomType
name|bloomType
init|=
name|BloomType
operator|.
name|NONE
decl_stmt|;
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|BLOOM_FILTER_OPTION
argument_list|)
condition|)
block|{
name|bloomType
operator|=
name|BloomType
operator|.
name|valueOf
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|BLOOM_FILTER_OPTION
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bloomType
operator|==
name|BloomType
operator|.
name|ROWPREFIX_FIXED_LENGTH
condition|)
block|{
if|if
condition|(
operator|!
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|BLOOM_FILTER_PARAM_OPTION
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"the parameter of bloom filter is not specified"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
name|conf
operator|.
name|set
argument_list|(
name|BloomFilterUtil
operator|.
name|PREFIX_LENGTH_KEY
argument_list|,
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|BLOOM_FILTER_PARAM_OPTION
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|blockSize
init|=
name|HConstants
operator|.
name|DEFAULT_BLOCKSIZE
decl_stmt|;
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|BLOCK_SIZE_OPTION
argument_list|)
condition|)
name|blockSize
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|BLOCK_SIZE_OPTION
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|BLOOM_BLOCK_SIZE_OPTION
argument_list|)
condition|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|BloomFilterFactory
operator|.
name|IO_STOREFILE_BLOOM_BLOCK_SIZE
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|BLOOM_BLOCK_SIZE_OPTION
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|INDEX_BLOCK_SIZE_OPTION
argument_list|)
condition|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|HFileBlockIndex
operator|.
name|MAX_CHUNK_SIZE_KEY
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
name|INDEX_BLOCK_SIZE_OPTION
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withCompression
argument_list|(
name|compr
argument_list|)
operator|.
name|withBlockSize
argument_list|(
name|blockSize
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
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|,
name|fs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|outputDir
argument_list|)
operator|.
name|withBloomType
argument_list|(
name|bloomType
argument_list|)
operator|.
name|withMaxKeyCount
argument_list|(
name|numKV
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
name|rand
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing "
operator|+
name|numKV
operator|+
literal|" key/value pairs"
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numKV
condition|;
operator|++
name|i
control|)
block|{
name|sfw
operator|.
name|append
argument_list|(
name|generateKeyValue
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|numMetaBlocks
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|10
argument_list|)
operator|+
literal|1
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing "
operator|+
name|numMetaBlocks
operator|+
literal|" meta blocks"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|metaI
init|=
literal|0
init|;
name|metaI
operator|<
name|numMetaBlocks
condition|;
operator|++
name|metaI
control|)
block|{
name|sfw
operator|.
name|getHFileWriter
argument_list|()
operator|.
name|appendMetaBlock
argument_list|(
name|generateString
argument_list|()
argument_list|,
operator|new
name|BytesWritable
argument_list|(
name|generateValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sfw
operator|.
name|close
argument_list|()
expr_stmt|;
name|Path
name|storeFilePath
init|=
name|sfw
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|long
name|fileSize
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|storeFilePath
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created {}, {} bytes, compression={}"
argument_list|,
name|storeFilePath
argument_list|,
name|fileSize
argument_list|,
name|compr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|configureKeyValue
parameter_list|(
name|long
name|numKV
parameter_list|,
name|int
name|keyLen
parameter_list|,
name|int
name|valueLen
parameter_list|)
block|{
name|numKV
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|numKV
argument_list|)
expr_stmt|;
name|keyLen
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|keyLen
argument_list|)
expr_stmt|;
name|keyPrefixLen
operator|=
literal|0
expr_stmt|;
while|while
condition|(
name|numKV
operator|!=
literal|0
condition|)
block|{
name|numKV
operator|>>>=
literal|8
expr_stmt|;
operator|++
name|keyPrefixLen
expr_stmt|;
block|}
name|this
operator|.
name|keyLen
operator|=
name|Math
operator|.
name|max
argument_list|(
name|keyPrefixLen
argument_list|,
name|keyLen
argument_list|)
expr_stmt|;
name|this
operator|.
name|valueLen
operator|=
name|valueLen
expr_stmt|;
comment|// Arbitrarily split the key into row, column family, and qualifier.
name|rowLen
operator|=
name|keyPrefixLen
operator|/
literal|3
expr_stmt|;
name|cfLen
operator|=
name|keyPrefixLen
operator|/
literal|4
expr_stmt|;
block|}
specifier|private
name|int
name|nextInRange
parameter_list|(
name|int
name|range
parameter_list|)
block|{
return|return
name|rand
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|range
operator|+
literal|1
argument_list|)
operator|-
name|range
return|;
block|}
specifier|public
name|KeyValue
name|generateKeyValue
parameter_list|(
name|long
name|i
parameter_list|)
block|{
name|byte
index|[]
name|k
init|=
name|generateKey
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|byte
index|[]
name|v
init|=
name|generateValue
argument_list|()
decl_stmt|;
return|return
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
specifier|private
name|String
name|generateString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
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
name|rand
operator|.
name|nextInt
argument_list|(
literal|10
argument_list|)
condition|;
operator|++
name|i
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
call|(
name|char
call|)
argument_list|(
literal|'A'
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
literal|26
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|byte
index|[]
name|generateKey
parameter_list|(
name|long
name|i
parameter_list|)
block|{
name|byte
index|[]
name|k
init|=
operator|new
name|byte
index|[
name|Math
operator|.
name|max
argument_list|(
name|keyPrefixLen
argument_list|,
name|keyLen
operator|+
name|nextInRange
argument_list|(
name|LEN_VARIATION
argument_list|)
argument_list|)
index|]
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
name|keyPrefixLen
operator|-
literal|1
init|;
name|pos
operator|>=
literal|0
condition|;
operator|--
name|pos
control|)
block|{
name|k
index|[
name|pos
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|i
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|i
operator|>>>=
literal|8
expr_stmt|;
block|}
for|for
control|(
name|int
name|pos
init|=
name|keyPrefixLen
init|;
name|pos
operator|<
name|k
operator|.
name|length
condition|;
operator|++
name|pos
control|)
block|{
name|k
index|[
name|pos
index|]
operator|=
operator|(
name|byte
operator|)
name|rand
operator|.
name|nextInt
argument_list|(
literal|256
argument_list|)
expr_stmt|;
block|}
return|return
name|k
return|;
block|}
specifier|private
name|byte
index|[]
name|generateValue
parameter_list|()
block|{
name|byte
index|[]
name|v
init|=
operator|new
name|byte
index|[
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|valueLen
operator|+
name|nextInRange
argument_list|(
name|LEN_VARIATION
argument_list|)
argument_list|)
index|]
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
name|v
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|v
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|rand
operator|.
name|nextInt
argument_list|(
literal|256
argument_list|)
expr_stmt|;
block|}
return|return
name|v
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|CreateRandomStoreFile
name|app
init|=
operator|new
name|CreateRandomStoreFile
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|app
operator|.
name|run
argument_list|(
name|args
argument_list|)
condition|)
name|System
operator|.
name|exit
argument_list|(
name|EXIT_FAILURE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|EXIT_FAILURE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

