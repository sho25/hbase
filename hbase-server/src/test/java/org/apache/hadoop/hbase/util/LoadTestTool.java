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
name|util
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
name|PerformanceEvaluation
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
name|HBaseAdmin
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
name|regionserver
operator|.
name|StoreFile
import|;
end_import

begin_comment
comment|/**  * A command-line utility that reads, writes, and verifies data. Unlike  * {@link PerformanceEvaluation}, this tool validates the data written,  * and supports simultaneously writing and reading the same set of keys.  */
end_comment

begin_class
specifier|public
class|class
name|LoadTestTool
extends|extends
name|AbstractHBaseTool
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
name|LoadTestTool
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Table name for the test */
specifier|private
name|byte
index|[]
name|tableName
decl_stmt|;
comment|/** Table name to use of not overridden on the command line */
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_TABLE_NAME
init|=
literal|"cluster_test"
decl_stmt|;
comment|/** Column family used by the test */
specifier|static
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test_cf"
argument_list|)
decl_stmt|;
comment|/** Column families used by the test */
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|COLUMN_FAMILIES
init|=
block|{
name|COLUMN_FAMILY
block|}
decl_stmt|;
comment|/** The number of reader/writer threads if not specified */
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_THREADS
init|=
literal|20
decl_stmt|;
comment|/** Usage string for the load option */
specifier|private
specifier|static
specifier|final
name|String
name|OPT_USAGE_LOAD
init|=
literal|"<avg_cols_per_key>:<avg_data_size>"
operator|+
literal|"[:<#threads="
operator|+
name|DEFAULT_NUM_THREADS
operator|+
literal|">]"
decl_stmt|;
comment|/** Usa\ge string for the read option */
specifier|private
specifier|static
specifier|final
name|String
name|OPT_USAGE_READ
init|=
literal|"<verify_percent>[:<#threads="
operator|+
name|DEFAULT_NUM_THREADS
operator|+
literal|">]"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_USAGE_BLOOM
init|=
literal|"Bloom filter type, one of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|StoreFile
operator|.
name|BloomType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_USAGE_COMPRESSION
init|=
literal|"Compression type, "
operator|+
literal|"one of "
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
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_DATA_BLOCK_ENCODING_USAGE
init|=
literal|"Encoding algorithm (e.g. prefix "
operator|+
literal|"compression) to use for data blocks in the test column family, "
operator|+
literal|"one of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|DataBlockEncoding
operator|.
name|values
argument_list|()
argument_list|)
operator|+
literal|"."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_BLOOM
init|=
literal|"bloom"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_COMPRESSION
init|=
literal|"compression"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_DATA_BLOCK_ENCODING
init|=
name|HColumnDescriptor
operator|.
name|DATA_BLOCK_ENCODING
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_ENCODE_IN_CACHE_ONLY
init|=
literal|"encode_in_cache_only"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_ENCODE_IN_CACHE_ONLY_USAGE
init|=
literal|"If this is specified, data blocks will only be encoded in block "
operator|+
literal|"cache but not on disk"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_KEY_WINDOW
init|=
literal|"key_window"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_WRITE
init|=
literal|"write"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_MAX_READ_ERRORS
init|=
literal|"max_read_errors"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_MULTIPUT
init|=
literal|"multiput"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_NUM_KEYS
init|=
literal|"num_keys"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_READ
init|=
literal|"read"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_START_KEY
init|=
literal|"start_key"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_TABLE_NAME
init|=
literal|"tn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_ZK_QUORUM
init|=
literal|"zk"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_START_KEY
init|=
literal|0
decl_stmt|;
comment|/** This will be removed as we factor out the dependency on command line */
specifier|private
name|CommandLine
name|cmd
decl_stmt|;
specifier|private
name|MultiThreadedWriter
name|writerThreads
init|=
literal|null
decl_stmt|;
specifier|private
name|MultiThreadedReader
name|readerThreads
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|startKey
decl_stmt|,
name|endKey
decl_stmt|;
specifier|private
name|boolean
name|isWrite
decl_stmt|,
name|isRead
decl_stmt|;
comment|// Column family options
specifier|private
name|DataBlockEncoding
name|dataBlockEncodingAlgo
decl_stmt|;
specifier|private
name|boolean
name|encodeInCacheOnly
decl_stmt|;
specifier|private
name|Compression
operator|.
name|Algorithm
name|compressAlgo
decl_stmt|;
specifier|private
name|StoreFile
operator|.
name|BloomType
name|bloomType
decl_stmt|;
comment|// Writer options
specifier|private
name|int
name|numWriterThreads
init|=
name|DEFAULT_NUM_THREADS
decl_stmt|;
specifier|private
name|long
name|minColsPerKey
decl_stmt|,
name|maxColsPerKey
decl_stmt|;
specifier|private
name|int
name|minColDataSize
decl_stmt|,
name|maxColDataSize
decl_stmt|;
specifier|private
name|boolean
name|isMultiPut
decl_stmt|;
comment|// Reader options
specifier|private
name|int
name|numReaderThreads
init|=
name|DEFAULT_NUM_THREADS
decl_stmt|;
specifier|private
name|int
name|keyWindow
init|=
name|MultiThreadedReader
operator|.
name|DEFAULT_KEY_WINDOW
decl_stmt|;
specifier|private
name|int
name|maxReadErrors
init|=
name|MultiThreadedReader
operator|.
name|DEFAULT_MAX_ERRORS
decl_stmt|;
specifier|private
name|int
name|verifyPercent
decl_stmt|;
specifier|private
name|String
index|[]
name|splitColonSeparated
parameter_list|(
name|String
name|option
parameter_list|,
name|int
name|minNumCols
parameter_list|,
name|int
name|maxNumCols
parameter_list|)
block|{
name|String
name|optVal
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|option
argument_list|)
decl_stmt|;
name|String
index|[]
name|cols
init|=
name|optVal
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|cols
operator|.
name|length
argument_list|<
name|minNumCols
operator|||
name|cols
operator|.
name|length
argument_list|>
name|maxNumCols
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected at least "
operator|+
name|minNumCols
operator|+
literal|" columns but no more than "
operator|+
name|maxNumCols
operator|+
literal|" in the colon-separated value '"
operator|+
name|optVal
operator|+
literal|"' of the "
operator|+
literal|"-"
operator|+
name|option
operator|+
literal|" option"
argument_list|)
throw|;
block|}
return|return
name|cols
return|;
block|}
specifier|private
name|int
name|getNumThreads
parameter_list|(
name|String
name|numThreadsStr
parameter_list|)
block|{
return|return
name|parseInt
argument_list|(
name|numThreadsStr
argument_list|,
literal|1
argument_list|,
name|Short
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
comment|/**    * Apply column family options such as Bloom filters, compression, and data    * block encoding.    */
specifier|private
name|void
name|applyColumnFamilyOptions
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|columnFamilies
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabling table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|cf
range|:
name|columnFamilies
control|)
block|{
name|HColumnDescriptor
name|columnDesc
init|=
name|tableDesc
operator|.
name|getFamily
argument_list|(
name|cf
argument_list|)
decl_stmt|;
name|boolean
name|isNewCf
init|=
name|columnDesc
operator|==
literal|null
decl_stmt|;
if|if
condition|(
name|isNewCf
condition|)
block|{
name|columnDesc
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|cf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bloomType
operator|!=
literal|null
condition|)
block|{
name|columnDesc
operator|.
name|setBloomFilterType
argument_list|(
name|bloomType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compressAlgo
operator|!=
literal|null
condition|)
block|{
name|columnDesc
operator|.
name|setCompressionType
argument_list|(
name|compressAlgo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dataBlockEncodingAlgo
operator|!=
literal|null
condition|)
block|{
name|columnDesc
operator|.
name|setDataBlockEncoding
argument_list|(
name|dataBlockEncodingAlgo
argument_list|)
expr_stmt|;
name|columnDesc
operator|.
name|setEncodeOnDisk
argument_list|(
operator|!
name|encodeInCacheOnly
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isNewCf
condition|)
block|{
name|admin
operator|.
name|addColumn
argument_list|(
name|tableName
argument_list|,
name|columnDesc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|admin
operator|.
name|modifyColumn
argument_list|(
name|tableName
argument_list|,
name|columnDesc
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabling table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOptWithArg
argument_list|(
name|OPT_ZK_QUORUM
argument_list|,
literal|"ZK quorum as comma-separated host names "
operator|+
literal|"without port numbers"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_TABLE_NAME
argument_list|,
literal|"The name of the table to read or write"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_WRITE
argument_list|,
name|OPT_USAGE_LOAD
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_READ
argument_list|,
name|OPT_USAGE_READ
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_BLOOM
argument_list|,
name|OPT_USAGE_BLOOM
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_COMPRESSION
argument_list|,
name|OPT_USAGE_COMPRESSION
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_DATA_BLOCK_ENCODING
argument_list|,
name|OPT_DATA_BLOCK_ENCODING_USAGE
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_MAX_READ_ERRORS
argument_list|,
literal|"The maximum number of read errors "
operator|+
literal|"to tolerate before terminating all reader threads. The default is "
operator|+
name|MultiThreadedReader
operator|.
name|DEFAULT_MAX_ERRORS
operator|+
literal|"."
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_KEY_WINDOW
argument_list|,
literal|"The 'key window' to maintain between "
operator|+
literal|"reads and writes for concurrent write/read workload. The default "
operator|+
literal|"is "
operator|+
name|MultiThreadedReader
operator|.
name|DEFAULT_KEY_WINDOW
operator|+
literal|"."
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
name|OPT_MULTIPUT
argument_list|,
literal|"Whether to use multi-puts as opposed to "
operator|+
literal|"separate puts for every column in a row"
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
name|OPT_ENCODE_IN_CACHE_ONLY
argument_list|,
name|OPT_ENCODE_IN_CACHE_ONLY_USAGE
argument_list|)
expr_stmt|;
name|addRequiredOptWithArg
argument_list|(
name|OPT_NUM_KEYS
argument_list|,
literal|"The number of keys to read/write"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|OPT_START_KEY
argument_list|,
literal|"The first key to read/write "
operator|+
literal|"(a 0-based index). The default value is "
operator|+
name|DEFAULT_START_KEY
operator|+
literal|"."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|this
operator|.
name|cmd
operator|=
name|cmd
expr_stmt|;
name|tableName
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_TABLE_NAME
argument_list|,
name|DEFAULT_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|startKey
operator|=
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_START_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|DEFAULT_START_KEY
argument_list|)
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|long
name|numKeys
init|=
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_NUM_KEYS
argument_list|)
argument_list|,
literal|1
argument_list|,
name|Long
operator|.
name|MAX_VALUE
operator|-
name|startKey
argument_list|)
decl_stmt|;
name|endKey
operator|=
name|startKey
operator|+
name|numKeys
expr_stmt|;
name|isWrite
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_WRITE
argument_list|)
expr_stmt|;
name|isRead
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_READ
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isWrite
operator|&&
operator|!
name|isRead
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Either -"
operator|+
name|OPT_WRITE
operator|+
literal|" or "
operator|+
literal|"-"
operator|+
name|OPT_READ
operator|+
literal|" has to be specified"
argument_list|)
throw|;
block|}
name|encodeInCacheOnly
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_ENCODE_IN_CACHE_ONLY
argument_list|)
expr_stmt|;
name|parseColumnFamilyOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
if|if
condition|(
name|isWrite
condition|)
block|{
name|String
index|[]
name|writeOpts
init|=
name|splitColonSeparated
argument_list|(
name|OPT_WRITE
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|int
name|colIndex
init|=
literal|0
decl_stmt|;
name|minColsPerKey
operator|=
literal|1
expr_stmt|;
name|maxColsPerKey
operator|=
literal|2
operator|*
name|Long
operator|.
name|parseLong
argument_list|(
name|writeOpts
index|[
name|colIndex
operator|++
index|]
argument_list|)
expr_stmt|;
name|int
name|avgColDataSize
init|=
name|parseInt
argument_list|(
name|writeOpts
index|[
name|colIndex
operator|++
index|]
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|minColDataSize
operator|=
name|avgColDataSize
operator|/
literal|2
expr_stmt|;
name|maxColDataSize
operator|=
name|avgColDataSize
operator|*
literal|3
operator|/
literal|2
expr_stmt|;
if|if
condition|(
name|colIndex
operator|<
name|writeOpts
operator|.
name|length
condition|)
block|{
name|numWriterThreads
operator|=
name|getNumThreads
argument_list|(
name|writeOpts
index|[
name|colIndex
operator|++
index|]
argument_list|)
expr_stmt|;
block|}
name|isMultiPut
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_MULTIPUT
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Multi-puts: "
operator|+
name|isMultiPut
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Columns per key: "
operator|+
name|minColsPerKey
operator|+
literal|".."
operator|+
name|maxColsPerKey
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Data size per column: "
operator|+
name|minColDataSize
operator|+
literal|".."
operator|+
name|maxColDataSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isRead
condition|)
block|{
name|String
index|[]
name|readOpts
init|=
name|splitColonSeparated
argument_list|(
name|OPT_READ
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|int
name|colIndex
init|=
literal|0
decl_stmt|;
name|verifyPercent
operator|=
name|parseInt
argument_list|(
name|readOpts
index|[
name|colIndex
operator|++
index|]
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
if|if
condition|(
name|colIndex
operator|<
name|readOpts
operator|.
name|length
condition|)
block|{
name|numReaderThreads
operator|=
name|getNumThreads
argument_list|(
name|readOpts
index|[
name|colIndex
operator|++
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_MAX_READ_ERRORS
argument_list|)
condition|)
block|{
name|maxReadErrors
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_MAX_READ_ERRORS
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_KEY_WINDOW
argument_list|)
condition|)
block|{
name|keyWindow
operator|=
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_KEY_WINDOW
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Percent of keys to verify: "
operator|+
name|verifyPercent
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Reader threads: "
operator|+
name|numReaderThreads
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Key range: ["
operator|+
name|startKey
operator|+
literal|".."
operator|+
operator|(
name|endKey
operator|-
literal|1
operator|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|parseColumnFamilyOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|String
name|dataBlockEncodingStr
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_DATA_BLOCK_ENCODING
argument_list|)
decl_stmt|;
name|dataBlockEncodingAlgo
operator|=
name|dataBlockEncodingStr
operator|==
literal|null
condition|?
literal|null
else|:
name|DataBlockEncoding
operator|.
name|valueOf
argument_list|(
name|dataBlockEncodingStr
argument_list|)
expr_stmt|;
if|if
condition|(
name|dataBlockEncodingAlgo
operator|==
name|DataBlockEncoding
operator|.
name|NONE
operator|&&
name|encodeInCacheOnly
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"-"
operator|+
name|OPT_ENCODE_IN_CACHE_ONLY
operator|+
literal|" "
operator|+
literal|"does not make sense when data block encoding is not used"
argument_list|)
throw|;
block|}
name|String
name|compressStr
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_COMPRESSION
argument_list|)
decl_stmt|;
name|compressAlgo
operator|=
name|compressStr
operator|==
literal|null
condition|?
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
else|:
name|Compression
operator|.
name|Algorithm
operator|.
name|valueOf
argument_list|(
name|compressStr
argument_list|)
expr_stmt|;
name|String
name|bloomStr
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_BLOOM
argument_list|)
decl_stmt|;
name|bloomType
operator|=
name|bloomStr
operator|==
literal|null
condition|?
literal|null
else|:
name|StoreFile
operator|.
name|BloomType
operator|.
name|valueOf
argument_list|(
name|bloomStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_ZK_QUORUM
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_ZK_QUORUM
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HBaseTestingUtility
operator|.
name|createPreSplitLoadTestTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|compressAlgo
argument_list|,
name|dataBlockEncodingAlgo
argument_list|)
expr_stmt|;
name|applyColumnFamilyOptions
argument_list|(
name|tableName
argument_list|,
name|COLUMN_FAMILIES
argument_list|)
expr_stmt|;
if|if
condition|(
name|isWrite
condition|)
block|{
name|writerThreads
operator|=
operator|new
name|MultiThreadedWriter
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
name|writerThreads
operator|.
name|setMultiPut
argument_list|(
name|isMultiPut
argument_list|)
expr_stmt|;
name|writerThreads
operator|.
name|setColumnsPerKey
argument_list|(
name|minColsPerKey
argument_list|,
name|maxColsPerKey
argument_list|)
expr_stmt|;
name|writerThreads
operator|.
name|setDataSize
argument_list|(
name|minColDataSize
argument_list|,
name|maxColDataSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isRead
condition|)
block|{
name|readerThreads
operator|=
operator|new
name|MultiThreadedReader
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|verifyPercent
argument_list|)
expr_stmt|;
name|readerThreads
operator|.
name|setMaxErrors
argument_list|(
name|maxReadErrors
argument_list|)
expr_stmt|;
name|readerThreads
operator|.
name|setKeyWindow
argument_list|(
name|keyWindow
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isRead
operator|&&
name|isWrite
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Concurrent read/write workload: making readers aware of the "
operator|+
literal|"write point"
argument_list|)
expr_stmt|;
name|readerThreads
operator|.
name|linkToWriter
argument_list|(
name|writerThreads
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isWrite
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting to write data..."
argument_list|)
expr_stmt|;
name|writerThreads
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numWriterThreads
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isRead
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting to read data..."
argument_list|)
expr_stmt|;
name|readerThreads
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numReaderThreads
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isWrite
condition|)
block|{
name|writerThreads
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isRead
condition|)
block|{
name|readerThreads
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
block|}
name|boolean
name|success
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|isWrite
condition|)
block|{
name|success
operator|=
name|success
operator|&&
name|writerThreads
operator|.
name|getNumWriteFailures
argument_list|()
operator|==
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|isRead
condition|)
block|{
name|success
operator|=
name|success
operator|&&
name|readerThreads
operator|.
name|getNumReadErrors
argument_list|()
operator|==
literal|0
operator|&&
name|readerThreads
operator|.
name|getNumReadFailures
argument_list|()
operator|==
literal|0
expr_stmt|;
block|}
return|return
name|success
condition|?
literal|0
else|:
literal|1
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
operator|new
name|LoadTestTool
argument_list|()
operator|.
name|doStaticMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

