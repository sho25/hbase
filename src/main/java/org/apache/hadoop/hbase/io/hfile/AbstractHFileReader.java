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
name|io
operator|.
name|hfile
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|fs
operator|.
name|FSDataInputStream
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
name|hfile
operator|.
name|HFile
operator|.
name|FileInfo
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
operator|.
name|Reader
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
name|RawComparator
import|;
end_import

begin_comment
comment|/**  * Common functionality needed by all versions of {@link HFile} readers.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractHFileReader
implements|implements
name|HFile
operator|.
name|Reader
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
name|AbstractHFileReader
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Filesystem-level block reader for this HFile format version. */
specifier|protected
name|HFileBlock
operator|.
name|FSReader
name|fsBlockReader
decl_stmt|;
comment|/** Stream to read from. */
specifier|protected
name|FSDataInputStream
name|istream
decl_stmt|;
comment|/**    * True if we should close the input stream when done. We don't close it if we    * didn't open it.    */
specifier|protected
specifier|final
name|boolean
name|closeIStream
decl_stmt|;
comment|/** Data block index reader keeping the root data index in memory */
specifier|protected
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|dataBlockIndexReader
decl_stmt|;
comment|/** Meta block index reader -- always single level */
specifier|protected
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|metaBlockIndexReader
decl_stmt|;
specifier|protected
specifier|final
name|FixedFileTrailer
name|trailer
decl_stmt|;
comment|/** Filled when we read in the trailer. */
specifier|protected
specifier|final
name|Compression
operator|.
name|Algorithm
name|compressAlgo
decl_stmt|;
comment|/** Last key in the file. Filled in when we read in the file info */
specifier|protected
name|byte
index|[]
name|lastKey
init|=
literal|null
decl_stmt|;
comment|/** Average key length read from file info */
specifier|protected
name|int
name|avgKeyLen
init|=
operator|-
literal|1
decl_stmt|;
comment|/** Average value length read from file info */
specifier|protected
name|int
name|avgValueLen
init|=
operator|-
literal|1
decl_stmt|;
comment|/** Key comparator */
specifier|protected
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|comparator
decl_stmt|;
comment|/** Size of this file. */
specifier|protected
specifier|final
name|long
name|fileSize
decl_stmt|;
comment|/** Block cache to use. */
specifier|protected
specifier|final
name|BlockCache
name|blockCache
decl_stmt|;
specifier|protected
name|AtomicLong
name|cacheHits
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|protected
name|AtomicLong
name|blockLoads
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|protected
name|AtomicLong
name|metaLoads
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|/**    * Whether file is from in-memory store (comes from column family    * configuration).    */
specifier|protected
name|boolean
name|inMemory
init|=
literal|false
decl_stmt|;
comment|/**    * Whether blocks of file should be evicted from the block cache when the    * file is being closed    */
specifier|protected
specifier|final
name|boolean
name|evictOnClose
decl_stmt|;
comment|/** Path of file */
specifier|protected
specifier|final
name|Path
name|path
decl_stmt|;
comment|/** File name to be used for block names */
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
specifier|protected
name|FileInfo
name|fileInfo
decl_stmt|;
comment|/** Prefix of the form cf.<column_family_name> for statistics counters. */
specifier|private
specifier|final
name|String
name|cfStatsPrefix
decl_stmt|;
specifier|protected
name|AbstractHFileReader
parameter_list|(
name|Path
name|path
parameter_list|,
name|FixedFileTrailer
name|trailer
parameter_list|,
specifier|final
name|FSDataInputStream
name|fsdis
parameter_list|,
specifier|final
name|long
name|fileSize
parameter_list|,
specifier|final
name|boolean
name|closeIStream
parameter_list|,
specifier|final
name|BlockCache
name|blockCache
parameter_list|,
specifier|final
name|boolean
name|inMemory
parameter_list|,
specifier|final
name|boolean
name|evictOnClose
parameter_list|)
block|{
name|this
operator|.
name|trailer
operator|=
name|trailer
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|=
name|trailer
operator|.
name|getCompressionCodec
argument_list|()
expr_stmt|;
name|this
operator|.
name|blockCache
operator|=
name|blockCache
expr_stmt|;
name|this
operator|.
name|fileSize
operator|=
name|fileSize
expr_stmt|;
name|this
operator|.
name|istream
operator|=
name|fsdis
expr_stmt|;
name|this
operator|.
name|closeIStream
operator|=
name|closeIStream
expr_stmt|;
name|this
operator|.
name|inMemory
operator|=
name|inMemory
expr_stmt|;
name|this
operator|.
name|evictOnClose
operator|=
name|evictOnClose
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|path
operator|.
name|getName
argument_list|()
expr_stmt|;
name|cfStatsPrefix
operator|=
literal|"cf."
operator|+
name|parseCfNameFromPath
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|BlockIndexNotLoadedException
extends|extends
name|IllegalStateException
block|{
specifier|public
name|BlockIndexNotLoadedException
parameter_list|()
block|{
comment|// Add a message in case anyone relies on it as opposed to class name.
name|super
argument_list|(
literal|"Block index not loaded"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|String
name|toStringFirstKey
parameter_list|()
block|{
return|return
name|KeyValue
operator|.
name|keyToString
argument_list|(
name|getFirstKey
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|String
name|toStringLastKey
parameter_list|()
block|{
return|return
name|KeyValue
operator|.
name|keyToString
argument_list|(
name|getLastKey
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Parse the HFile path to figure out which table and column family    * it belongs to. This is used to maintain read statistics on a    * per-column-family basis.    *    * @param path HFile path name    */
specifier|public
specifier|static
name|String
name|parseCfNameFromPath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|String
name|splits
index|[]
init|=
name|path
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|splits
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not determine the table and column family of the "
operator|+
literal|"HFile path "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
literal|"unknown"
return|;
block|}
return|return
name|splits
index|[
name|splits
operator|.
name|length
operator|-
literal|2
index|]
return|;
block|}
specifier|public
specifier|abstract
name|boolean
name|isFileInfoLoaded
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"reader="
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
operator|(
operator|!
name|isFileInfoLoaded
argument_list|()
condition|?
literal|""
else|:
literal|", compression="
operator|+
name|compressAlgo
operator|.
name|getName
argument_list|()
operator|+
literal|", inMemory="
operator|+
name|inMemory
operator|+
literal|", firstKey="
operator|+
name|toStringFirstKey
argument_list|()
operator|+
literal|", lastKey="
operator|+
name|toStringLastKey
argument_list|()
operator|)
operator|+
literal|", avgKeyLen="
operator|+
name|avgKeyLen
operator|+
literal|", avgValueLen="
operator|+
name|avgValueLen
operator|+
literal|", entries="
operator|+
name|trailer
operator|.
name|getEntryCount
argument_list|()
operator|+
literal|", length="
operator|+
name|fileSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|length
parameter_list|()
block|{
return|return
name|fileSize
return|;
block|}
comment|/**    * Create a Scanner on this file. No seeks or reads are done on creation. Call    * {@link HFileScanner#seekTo(byte[])} to position an start the read. There is    * nothing to clean up in a Scanner. Letting go of your references to the    * scanner is sufficient. NOTE: Do not use this overload of getScanner for    * compactions.    *    * @param cacheBlocks True if we should cache blocks read in by this scanner.    * @param pread Use positional read rather than seek+read if true (pread is    *          better for random reads, seek+read is better scanning).    * @return Scanner on this file.    */
annotation|@
name|Override
specifier|public
name|HFileScanner
name|getScanner
parameter_list|(
name|boolean
name|cacheBlocks
parameter_list|,
specifier|final
name|boolean
name|pread
parameter_list|)
block|{
return|return
name|getScanner
argument_list|(
name|cacheBlocks
argument_list|,
name|pread
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * @return the first key in the file. May be null if file has no entries. Note    *         that this is not the first row key, but rather the byte form of the    *         first KeyValue.    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFirstKey
parameter_list|()
block|{
if|if
condition|(
name|dataBlockIndexReader
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BlockIndexNotLoadedException
argument_list|()
throw|;
block|}
return|return
name|dataBlockIndexReader
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
name|dataBlockIndexReader
operator|.
name|getRootBlockKey
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|/**    * TODO left from {@HFile} version 1: move this to StoreFile after Ryan's    * patch goes in to eliminate {@link KeyValue} here.    *    * @return the first row key, or null if the file is empty.    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFirstRowKey
parameter_list|()
block|{
name|byte
index|[]
name|firstKey
init|=
name|getFirstKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstKey
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|KeyValue
operator|.
name|createKeyValueFromKey
argument_list|(
name|firstKey
argument_list|)
operator|.
name|getRow
argument_list|()
return|;
block|}
comment|/**    * TODO left from {@HFile} version 1: move this to StoreFile after    * Ryan's patch goes in to eliminate {@link KeyValue} here.    *    * @return the last row key, or null if the file is empty.    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getLastRowKey
parameter_list|()
block|{
name|byte
index|[]
name|lastKey
init|=
name|getLastKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastKey
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|KeyValue
operator|.
name|createKeyValueFromKey
argument_list|(
name|lastKey
argument_list|)
operator|.
name|getRow
argument_list|()
return|;
block|}
comment|/** @return number of KV entries in this HFile */
annotation|@
name|Override
specifier|public
name|long
name|getEntries
parameter_list|()
block|{
return|return
name|trailer
operator|.
name|getEntryCount
argument_list|()
return|;
block|}
comment|/** @return comparator */
annotation|@
name|Override
specifier|public
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|getComparator
parameter_list|()
block|{
return|return
name|comparator
return|;
block|}
comment|/** @return compression algorithm */
annotation|@
name|Override
specifier|public
name|Compression
operator|.
name|Algorithm
name|getCompressionAlgorithm
parameter_list|()
block|{
return|return
name|compressAlgo
return|;
block|}
comment|/**    * @return the total heap size of data and meta block indexes in bytes. Does    *         not take into account non-root blocks of a multilevel data index.    */
specifier|public
name|long
name|indexSize
parameter_list|()
block|{
return|return
operator|(
name|dataBlockIndexReader
operator|!=
literal|null
condition|?
name|dataBlockIndexReader
operator|.
name|heapSize
argument_list|()
else|:
literal|0
operator|)
operator|+
operator|(
operator|(
name|metaBlockIndexReader
operator|!=
literal|null
operator|)
condition|?
name|metaBlockIndexReader
operator|.
name|heapSize
argument_list|()
else|:
literal|0
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|getDataBlockIndexReader
parameter_list|()
block|{
return|return
name|dataBlockIndexReader
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getColumnFamilyName
parameter_list|()
block|{
return|return
name|cfStatsPrefix
return|;
block|}
annotation|@
name|Override
specifier|public
name|FixedFileTrailer
name|getTrailer
parameter_list|()
block|{
return|return
name|trailer
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileInfo
name|loadFileInfo
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|fileInfo
return|;
block|}
comment|/**    * An exception thrown when an operation requiring a scanner to be seeked    * is invoked on a scanner that is not seeked.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|NotSeekedException
extends|extends
name|IllegalStateException
block|{
specifier|public
name|NotSeekedException
parameter_list|()
block|{
name|super
argument_list|(
literal|"Not seeked to a key/value"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
specifier|abstract
class|class
name|Scanner
implements|implements
name|HFileScanner
block|{
specifier|protected
name|HFile
operator|.
name|Reader
name|reader
decl_stmt|;
specifier|protected
name|ByteBuffer
name|blockBuffer
decl_stmt|;
specifier|protected
name|boolean
name|cacheBlocks
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|pread
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|isCompaction
decl_stmt|;
specifier|protected
name|int
name|currKeyLen
decl_stmt|;
specifier|protected
name|int
name|currValueLen
decl_stmt|;
specifier|protected
name|int
name|blockFetches
decl_stmt|;
specifier|public
name|Scanner
parameter_list|(
specifier|final
name|HFile
operator|.
name|Reader
name|reader
parameter_list|,
specifier|final
name|boolean
name|cacheBlocks
parameter_list|,
specifier|final
name|boolean
name|pread
parameter_list|,
specifier|final
name|boolean
name|isCompaction
parameter_list|)
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|cacheBlocks
operator|=
name|cacheBlocks
expr_stmt|;
name|this
operator|.
name|pread
operator|=
name|pread
expr_stmt|;
name|this
operator|.
name|isCompaction
operator|=
name|isCompaction
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Reader
name|getReader
parameter_list|()
block|{
return|return
name|reader
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSeeked
parameter_list|()
block|{
return|return
name|blockBuffer
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HFileScanner for reader "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|reader
argument_list|)
return|;
block|}
specifier|protected
name|void
name|assertSeeked
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isSeeked
argument_list|()
condition|)
throw|throw
operator|new
name|NotSeekedException
argument_list|()
throw|;
block|}
block|}
comment|/** For testing */
name|HFileBlock
operator|.
name|FSReader
name|getUncachedBlockReader
parameter_list|()
block|{
return|return
name|fsBlockReader
return|;
block|}
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|path
return|;
block|}
block|}
end_class

end_unit

