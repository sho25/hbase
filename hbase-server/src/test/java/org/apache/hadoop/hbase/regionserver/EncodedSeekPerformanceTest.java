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
name|ArrayList
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
name|KeyValueUtil
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
name|LruBlockCache
import|;
end_import

begin_comment
comment|/**  * Test seek performance for encoded data blocks. Read an HFile and do several  * random seeks.  */
end_comment

begin_class
specifier|public
class|class
name|EncodedSeekPerformanceTest
block|{
specifier|private
specifier|static
specifier|final
name|double
name|NANOSEC_IN_SEC
init|=
literal|1000.0
operator|*
literal|1000.0
operator|*
literal|1000.0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|BYTES_IN_MEGABYTES
init|=
literal|1024.0
operator|*
literal|1024.0
decl_stmt|;
comment|/** Default number of seeks which will be used in benchmark. */
specifier|public
specifier|static
name|int
name|DEFAULT_NUMBER_OF_SEEKS
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|testingUtility
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|configuration
init|=
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
specifier|private
name|Random
name|randomizer
decl_stmt|;
specifier|private
name|int
name|numberOfSeeks
decl_stmt|;
comment|/** Use this benchmark with default options */
specifier|public
name|EncodedSeekPerformanceTest
parameter_list|()
block|{
name|configuration
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
literal|0.5f
argument_list|)
expr_stmt|;
name|randomizer
operator|=
operator|new
name|Random
argument_list|(
literal|42L
argument_list|)
expr_stmt|;
name|numberOfSeeks
operator|=
name|DEFAULT_NUMBER_OF_SEEKS
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Cell
argument_list|>
name|prepareListOfTestSeeks
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|allKeyValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// read all of the key values
name|HStoreFile
name|storeFile
init|=
operator|new
name|HStoreFile
argument_list|(
name|testingUtility
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|path
argument_list|,
name|configuration
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|storeFile
operator|.
name|initReader
argument_list|()
expr_stmt|;
name|StoreFileReader
name|reader
init|=
name|storeFile
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|StoreFileScanner
name|scanner
init|=
name|reader
operator|.
name|getStoreFileScanner
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Cell
name|current
decl_stmt|;
name|scanner
operator|.
name|seek
argument_list|(
name|KeyValue
operator|.
name|LOWESTKEY
argument_list|)
expr_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|current
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
condition|)
block|{
name|allKeyValues
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
name|storeFile
operator|.
name|closeStoreFile
argument_list|(
name|cacheConf
operator|.
name|shouldEvictOnClose
argument_list|()
argument_list|)
expr_stmt|;
comment|// pick seeks by random
name|List
argument_list|<
name|Cell
argument_list|>
name|seeks
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|numberOfSeeks
condition|;
operator|++
name|i
control|)
block|{
name|Cell
name|keyValue
init|=
name|allKeyValues
operator|.
name|get
argument_list|(
name|randomizer
operator|.
name|nextInt
argument_list|(
name|allKeyValues
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|seeks
operator|.
name|add
argument_list|(
name|keyValue
argument_list|)
expr_stmt|;
block|}
name|clearBlockCache
argument_list|()
expr_stmt|;
return|return
name|seeks
return|;
block|}
specifier|private
name|void
name|runTest
parameter_list|(
name|Path
name|path
parameter_list|,
name|DataBlockEncoding
name|blockEncoding
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|seeks
parameter_list|)
throws|throws
name|IOException
block|{
comment|// read all of the key values
name|HStoreFile
name|storeFile
init|=
operator|new
name|HStoreFile
argument_list|(
name|testingUtility
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|path
argument_list|,
name|configuration
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|storeFile
operator|.
name|initReader
argument_list|()
expr_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|StoreFileReader
name|reader
init|=
name|storeFile
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|StoreFileScanner
name|scanner
init|=
name|reader
operator|.
name|getStoreFileScanner
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|long
name|startReadingTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|Cell
name|current
decl_stmt|;
name|scanner
operator|.
name|seek
argument_list|(
name|KeyValue
operator|.
name|LOWESTKEY
argument_list|)
expr_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|current
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
condition|)
block|{
comment|// just iterate it!
if|if
condition|(
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|current
argument_list|)
operator|.
name|getLength
argument_list|()
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Negative KV size: "
operator|+
name|current
argument_list|)
throw|;
block|}
name|totalSize
operator|+=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|current
argument_list|)
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
name|long
name|finishReadingTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
comment|// do seeks
name|long
name|startSeeksTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|keyValue
range|:
name|seeks
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|keyValue
argument_list|)
expr_stmt|;
name|Cell
name|toVerify
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|keyValue
operator|.
name|equals
argument_list|(
name|toVerify
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"KeyValue doesn't match:\n"
operator|+
literal|"Orig key: %s\n"
operator|+
literal|"Ret key:  %s"
argument_list|,
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|keyValue
argument_list|)
operator|.
name|getKeyString
argument_list|()
argument_list|,
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|toVerify
argument_list|)
operator|.
name|getKeyString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|long
name|finishSeeksTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|finishSeeksTime
operator|<
name|startSeeksTime
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Finish time "
operator|+
name|finishSeeksTime
operator|+
literal|" is earlier than start time "
operator|+
name|startSeeksTime
argument_list|)
throw|;
block|}
comment|// write some stats
name|double
name|readInMbPerSec
init|=
operator|(
name|totalSize
operator|*
name|NANOSEC_IN_SEC
operator|)
operator|/
operator|(
name|BYTES_IN_MEGABYTES
operator|*
operator|(
name|finishReadingTime
operator|-
name|startReadingTime
operator|)
operator|)
decl_stmt|;
name|double
name|seeksPerSec
init|=
operator|(
name|seeks
operator|.
name|size
argument_list|()
operator|*
name|NANOSEC_IN_SEC
operator|)
operator|/
operator|(
name|finishSeeksTime
operator|-
name|startSeeksTime
operator|)
decl_stmt|;
name|storeFile
operator|.
name|closeStoreFile
argument_list|(
name|cacheConf
operator|.
name|shouldEvictOnClose
argument_list|()
argument_list|)
expr_stmt|;
name|clearBlockCache
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|blockEncoding
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"  Read speed:       %8.2f (MB/s)\n"
argument_list|,
name|readInMbPerSec
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"  Seeks per second: %8.2f (#/s)\n"
argument_list|,
name|seeksPerSec
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"  Total KV size:    %d\n"
argument_list|,
name|totalSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param path Path to the HFile which will be used.    * @param encodings the data block encoding algorithms to use    * @throws IOException if there is a bug while reading from disk    */
specifier|public
name|void
name|runTests
parameter_list|(
name|Path
name|path
parameter_list|,
name|DataBlockEncoding
index|[]
name|encodings
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|seeks
init|=
name|prepareListOfTestSeeks
argument_list|(
name|path
argument_list|)
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|blockEncoding
range|:
name|encodings
control|)
block|{
name|runTest
argument_list|(
name|path
argument_list|,
name|blockEncoding
argument_list|,
name|seeks
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Command line interface:    * @param args Takes one argument - file size.    * @throws IOException if there is a bug while reading from disk    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
comment|// TODO, this test doesn't work as expected any more. Need to fix.
name|EncodedSeekPerformanceTest
name|utility
init|=
operator|new
name|EncodedSeekPerformanceTest
argument_list|()
decl_stmt|;
name|utility
operator|.
name|runTests
argument_list|(
name|path
argument_list|,
name|DataBlockEncoding
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: one argument, name of the HFile"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|clearBlockCache
parameter_list|()
block|{
operator|(
operator|(
name|LruBlockCache
operator|)
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
operator|.
name|get
argument_list|()
operator|)
operator|.
name|clearCache
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

