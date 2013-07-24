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
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|List
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
name|classification
operator|.
name|InterfaceAudience
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
name|FSDataOutputStream
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
name|KeyValue
operator|.
name|KeyComparator
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
name|HFile
operator|.
name|Writer
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
operator|.
name|BlockWritable
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
name|ChecksumType
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
name|BloomFilterWriter
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
name|io
operator|.
name|Writable
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
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * Writes HFile format version 2.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileWriterV2
extends|extends
name|AbstractHFileWriter
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HFileWriterV2
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Max memstore (mvcc) timestamp in FileInfo */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|MAX_MEMSTORE_TS_KEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"MAX_MEMSTORE_TS_KEY"
argument_list|)
decl_stmt|;
comment|/** KeyValue version in FileInfo */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|KEY_VALUE_VERSION
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY_VALUE_VERSION"
argument_list|)
decl_stmt|;
comment|/** Version for KeyValue which includes memstore timestamp */
specifier|public
specifier|static
specifier|final
name|int
name|KEY_VALUE_VER_WITH_MEMSTORE
init|=
literal|1
decl_stmt|;
comment|/** Inline block writers for multi-level block index and compound Blooms. */
specifier|private
name|List
argument_list|<
name|InlineBlockWriter
argument_list|>
name|inlineBlockWriters
init|=
operator|new
name|ArrayList
argument_list|<
name|InlineBlockWriter
argument_list|>
argument_list|()
decl_stmt|;
comment|/** Unified version 2 block writer */
specifier|private
name|HFileBlock
operator|.
name|Writer
name|fsBlockWriter
decl_stmt|;
specifier|private
name|HFileBlockIndex
operator|.
name|BlockIndexWriter
name|dataBlockIndexWriter
decl_stmt|;
specifier|private
name|HFileBlockIndex
operator|.
name|BlockIndexWriter
name|metaBlockIndexWriter
decl_stmt|;
comment|/** The offset of the first data block or -1 if the file is empty. */
specifier|private
name|long
name|firstDataBlockOffset
init|=
operator|-
literal|1
decl_stmt|;
comment|/** The offset of the last data block or 0 if the file is empty. */
specifier|private
name|long
name|lastDataBlockOffset
decl_stmt|;
comment|/** The last(stop) Key of the previous data block. */
specifier|private
name|byte
index|[]
name|lastKeyOfPreviousBlock
init|=
literal|null
decl_stmt|;
comment|/** Additional data items to be written to the "load-on-open" section. */
specifier|private
name|List
argument_list|<
name|BlockWritable
argument_list|>
name|additionalLoadOnOpenData
init|=
operator|new
name|ArrayList
argument_list|<
name|BlockWritable
argument_list|>
argument_list|()
decl_stmt|;
comment|/** Checksum related settings */
specifier|private
name|ChecksumType
name|checksumType
init|=
name|HFile
operator|.
name|DEFAULT_CHECKSUM_TYPE
decl_stmt|;
specifier|private
name|int
name|bytesPerChecksum
init|=
name|HFile
operator|.
name|DEFAULT_BYTES_PER_CHECKSUM
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|includeMemstoreTS
decl_stmt|;
specifier|private
name|long
name|maxMemstoreTS
init|=
literal|0
decl_stmt|;
specifier|static
class|class
name|WriterFactoryV2
extends|extends
name|HFile
operator|.
name|WriterFactory
block|{
name|WriterFactoryV2
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Writer
name|createWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|FSDataOutputStream
name|ostream
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compress
parameter_list|,
name|HFileDataBlockEncoder
name|blockEncoder
parameter_list|,
specifier|final
name|KeyComparator
name|comparator
parameter_list|,
specifier|final
name|ChecksumType
name|checksumType
parameter_list|,
specifier|final
name|int
name|bytesPerChecksum
parameter_list|,
name|boolean
name|includeMVCCReadpoint
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HFileWriterV2
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|ostream
argument_list|,
name|blockSize
argument_list|,
name|compress
argument_list|,
name|blockEncoder
argument_list|,
name|comparator
argument_list|,
name|checksumType
argument_list|,
name|bytesPerChecksum
argument_list|,
name|includeMVCCReadpoint
argument_list|)
return|;
block|}
block|}
comment|/** Constructor that takes a path, creates and closes the output stream. */
specifier|public
name|HFileWriterV2
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|FSDataOutputStream
name|ostream
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compressAlgo
parameter_list|,
name|HFileDataBlockEncoder
name|blockEncoder
parameter_list|,
specifier|final
name|KeyComparator
name|comparator
parameter_list|,
specifier|final
name|ChecksumType
name|checksumType
parameter_list|,
specifier|final
name|int
name|bytesPerChecksum
parameter_list|,
specifier|final
name|boolean
name|includeMVCCReadpoint
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|cacheConf
argument_list|,
name|ostream
operator|==
literal|null
condition|?
name|createOutputStream
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
literal|null
argument_list|)
else|:
name|ostream
argument_list|,
name|path
argument_list|,
name|blockSize
argument_list|,
name|compressAlgo
argument_list|,
name|blockEncoder
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|checksumType
operator|=
name|checksumType
expr_stmt|;
name|this
operator|.
name|bytesPerChecksum
operator|=
name|bytesPerChecksum
expr_stmt|;
name|this
operator|.
name|includeMemstoreTS
operator|=
name|includeMVCCReadpoint
expr_stmt|;
name|finishInit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/** Additional initialization steps */
specifier|private
name|void
name|finishInit
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|fsBlockWriter
operator|!=
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"finishInit called twice"
argument_list|)
throw|;
comment|// HFile filesystem-level (non-caching) block writer
name|fsBlockWriter
operator|=
operator|new
name|HFileBlock
operator|.
name|Writer
argument_list|(
name|compressAlgo
argument_list|,
name|blockEncoder
argument_list|,
name|includeMemstoreTS
argument_list|,
name|checksumType
argument_list|,
name|bytesPerChecksum
argument_list|)
expr_stmt|;
comment|// Data block index writer
name|boolean
name|cacheIndexesOnWrite
init|=
name|cacheConf
operator|.
name|shouldCacheIndexesOnWrite
argument_list|()
decl_stmt|;
name|dataBlockIndexWriter
operator|=
operator|new
name|HFileBlockIndex
operator|.
name|BlockIndexWriter
argument_list|(
name|fsBlockWriter
argument_list|,
name|cacheIndexesOnWrite
condition|?
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
else|:
literal|null
argument_list|,
name|cacheIndexesOnWrite
condition|?
name|name
else|:
literal|null
argument_list|)
expr_stmt|;
name|dataBlockIndexWriter
operator|.
name|setMaxChunkSize
argument_list|(
name|HFileBlockIndex
operator|.
name|getMaxChunkSize
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|inlineBlockWriters
operator|.
name|add
argument_list|(
name|dataBlockIndexWriter
argument_list|)
expr_stmt|;
comment|// Meta data block index writer
name|metaBlockIndexWriter
operator|=
operator|new
name|HFileBlockIndex
operator|.
name|BlockIndexWriter
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
literal|"Initialized with "
operator|+
name|cacheConf
argument_list|)
expr_stmt|;
block|}
comment|/**    * At a block boundary, write all the inline blocks and opens new block.    *    * @throws IOException    */
specifier|private
name|void
name|checkBlockBoundary
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|fsBlockWriter
operator|.
name|blockSizeWritten
argument_list|()
operator|<
name|blockSize
condition|)
return|return;
name|finishBlock
argument_list|()
expr_stmt|;
name|writeInlineBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|newBlock
argument_list|()
expr_stmt|;
block|}
comment|/** Clean up the current block */
specifier|private
name|void
name|finishBlock
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fsBlockWriter
operator|.
name|isWriting
argument_list|()
operator|||
name|fsBlockWriter
operator|.
name|blockSizeWritten
argument_list|()
operator|==
literal|0
condition|)
return|return;
name|long
name|startTimeNs
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
comment|// Update the first data block offset for scanning.
if|if
condition|(
name|firstDataBlockOffset
operator|==
operator|-
literal|1
condition|)
block|{
name|firstDataBlockOffset
operator|=
name|outputStream
operator|.
name|getPos
argument_list|()
expr_stmt|;
block|}
comment|// Update the last data block offset
name|lastDataBlockOffset
operator|=
name|outputStream
operator|.
name|getPos
argument_list|()
expr_stmt|;
name|fsBlockWriter
operator|.
name|writeHeaderAndData
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|int
name|onDiskSize
init|=
name|fsBlockWriter
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
decl_stmt|;
comment|// Generate a shorter faked key into index block. For example, consider a block boundary
comment|// between the keys "the quick brown fox" and "the who test text".  We can use "the r" as the
comment|// key for the index block entry since it is> all entries in the previous block and<= all
comment|// entries in subsequent blocks.
if|if
condition|(
name|comparator
operator|instanceof
name|KeyComparator
condition|)
block|{
name|byte
index|[]
name|fakeKey
init|=
operator|(
operator|(
name|KeyComparator
operator|)
name|comparator
operator|)
operator|.
name|getShortMidpointKey
argument_list|(
name|lastKeyOfPreviousBlock
argument_list|,
name|firstKeyInBlock
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparator
operator|.
name|compare
argument_list|(
name|fakeKey
argument_list|,
name|firstKeyInBlock
argument_list|)
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected getShortMidpointKey result, fakeKey:"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|fakeKey
argument_list|)
operator|+
literal|", firstKeyInBlock:"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|firstKeyInBlock
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|lastKeyOfPreviousBlock
operator|!=
literal|null
operator|&&
name|comparator
operator|.
name|compare
argument_list|(
name|lastKeyOfPreviousBlock
argument_list|,
name|fakeKey
argument_list|)
operator|>=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected getShortMidpointKey result, lastKeyOfPreviousBlock:"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|lastKeyOfPreviousBlock
argument_list|)
operator|+
literal|", fakeKey:"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|fakeKey
argument_list|)
argument_list|)
throw|;
block|}
name|dataBlockIndexWriter
operator|.
name|addEntry
argument_list|(
name|fakeKey
argument_list|,
name|lastDataBlockOffset
argument_list|,
name|onDiskSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dataBlockIndexWriter
operator|.
name|addEntry
argument_list|(
name|firstKeyInBlock
argument_list|,
name|lastDataBlockOffset
argument_list|,
name|onDiskSize
argument_list|)
expr_stmt|;
block|}
name|totalUncompressedBytes
operator|+=
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithHeader
argument_list|()
expr_stmt|;
name|HFile
operator|.
name|offerWriteLatency
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTimeNs
argument_list|)
expr_stmt|;
if|if
condition|(
name|cacheConf
operator|.
name|shouldCacheDataOnWrite
argument_list|()
condition|)
block|{
name|doCacheOnWrite
argument_list|(
name|lastDataBlockOffset
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Gives inline block writers an opportunity to contribute blocks. */
specifier|private
name|void
name|writeInlineBlocks
parameter_list|(
name|boolean
name|closing
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|InlineBlockWriter
name|ibw
range|:
name|inlineBlockWriters
control|)
block|{
while|while
condition|(
name|ibw
operator|.
name|shouldWriteBlock
argument_list|(
name|closing
argument_list|)
condition|)
block|{
name|long
name|offset
init|=
name|outputStream
operator|.
name|getPos
argument_list|()
decl_stmt|;
name|boolean
name|cacheThisBlock
init|=
name|ibw
operator|.
name|getCacheOnWrite
argument_list|()
decl_stmt|;
name|ibw
operator|.
name|writeInlineBlock
argument_list|(
name|fsBlockWriter
operator|.
name|startWriting
argument_list|(
name|ibw
operator|.
name|getInlineBlockType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fsBlockWriter
operator|.
name|writeHeaderAndData
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|ibw
operator|.
name|blockWritten
argument_list|(
name|offset
argument_list|,
name|fsBlockWriter
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
argument_list|,
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithoutHeader
argument_list|()
argument_list|)
expr_stmt|;
name|totalUncompressedBytes
operator|+=
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithHeader
argument_list|()
expr_stmt|;
if|if
condition|(
name|cacheThisBlock
condition|)
block|{
name|doCacheOnWrite
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Caches the last written HFile block.    * @param offset the offset of the block we want to cache. Used to determine    *          the cache key.    */
specifier|private
name|void
name|doCacheOnWrite
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
comment|// We don't cache-on-write data blocks on compaction, so assume this is not
comment|// a compaction.
specifier|final
name|boolean
name|isCompaction
init|=
literal|false
decl_stmt|;
name|HFileBlock
name|cacheFormatBlock
init|=
name|blockEncoder
operator|.
name|diskToCacheFormat
argument_list|(
name|fsBlockWriter
operator|.
name|getBlockForCaching
argument_list|()
argument_list|,
name|isCompaction
argument_list|)
decl_stmt|;
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
operator|.
name|cacheBlock
argument_list|(
operator|new
name|BlockCacheKey
argument_list|(
name|name
argument_list|,
name|offset
argument_list|,
name|blockEncoder
operator|.
name|getEncodingInCache
argument_list|()
argument_list|,
name|cacheFormatBlock
operator|.
name|getBlockType
argument_list|()
argument_list|)
argument_list|,
name|cacheFormatBlock
argument_list|)
expr_stmt|;
block|}
comment|/**    * Ready a new block for writing.    *    * @throws IOException    */
specifier|private
name|void
name|newBlock
parameter_list|()
throws|throws
name|IOException
block|{
comment|// This is where the next block begins.
name|fsBlockWriter
operator|.
name|startWriting
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|)
expr_stmt|;
name|firstKeyInBlock
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|lastKeyLength
operator|>
literal|0
condition|)
block|{
name|lastKeyOfPreviousBlock
operator|=
operator|new
name|byte
index|[
name|lastKeyLength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|lastKeyBuffer
argument_list|,
name|lastKeyOffset
argument_list|,
name|lastKeyOfPreviousBlock
argument_list|,
literal|0
argument_list|,
name|lastKeyLength
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add a meta block to the end of the file. Call before close(). Metadata    * blocks are expensive. Fill one with a bunch of serialized data rather than    * do a metadata block per metadata instance. If metadata is small, consider    * adding to file info using {@link #appendFileInfo(byte[], byte[])}    *    * @param metaBlockName    *          name of the block    * @param content    *          will call readFields to get data later (DO NOT REUSE)    */
annotation|@
name|Override
specifier|public
name|void
name|appendMetaBlock
parameter_list|(
name|String
name|metaBlockName
parameter_list|,
name|Writable
name|content
parameter_list|)
block|{
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|metaBlockName
argument_list|)
decl_stmt|;
name|int
name|i
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|metaNames
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
comment|// stop when the current key is greater than our own
name|byte
index|[]
name|cur
init|=
name|metaNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|BYTES_RAWCOMPARATOR
operator|.
name|compare
argument_list|(
name|cur
argument_list|,
literal|0
argument_list|,
name|cur
operator|.
name|length
argument_list|,
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|)
operator|>
literal|0
condition|)
block|{
break|break;
block|}
block|}
name|metaNames
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|metaData
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|content
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    *    * @param kv    *          KeyValue to add. Cannot be empty nor null.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
name|append
argument_list|(
name|kv
operator|.
name|getMemstoreTS
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMemstoreTS
operator|=
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|maxMemstoreTS
argument_list|,
name|kv
operator|.
name|getMemstoreTS
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    *    * @param key    *          Key to add. Cannot be empty nor null.    * @param value    *          Value to add. Cannot be empty nor null.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|byte
index|[]
name|key
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|append
argument_list|(
literal|0
argument_list|,
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    *    * @param key    * @param koffset    * @param klength    * @param value    * @param voffset    * @param vlength    * @throws IOException    */
specifier|private
name|void
name|append
parameter_list|(
specifier|final
name|long
name|memstoreTS
parameter_list|,
specifier|final
name|byte
index|[]
name|key
parameter_list|,
specifier|final
name|int
name|koffset
parameter_list|,
specifier|final
name|int
name|klength
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|int
name|voffset
parameter_list|,
specifier|final
name|int
name|vlength
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|dupKey
init|=
name|checkKey
argument_list|(
name|key
argument_list|,
name|koffset
argument_list|,
name|klength
argument_list|)
decl_stmt|;
name|checkValue
argument_list|(
name|value
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|dupKey
condition|)
block|{
name|checkBlockBoundary
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|fsBlockWriter
operator|.
name|isWriting
argument_list|()
condition|)
name|newBlock
argument_list|()
expr_stmt|;
comment|// Write length of key and value and then actual key and value bytes.
comment|// Additionally, we may also write down the memstoreTS.
block|{
name|DataOutputStream
name|out
init|=
name|fsBlockWriter
operator|.
name|getUserDataStream
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|klength
argument_list|)
expr_stmt|;
name|totalKeyLength
operator|+=
name|klength
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|vlength
argument_list|)
expr_stmt|;
name|totalValueLength
operator|+=
name|vlength
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|koffset
argument_list|,
name|klength
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|,
name|voffset
argument_list|,
name|vlength
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|includeMemstoreTS
condition|)
block|{
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|out
argument_list|,
name|memstoreTS
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Are we the first key in this block?
if|if
condition|(
name|firstKeyInBlock
operator|==
literal|null
condition|)
block|{
comment|// Copy the key.
name|firstKeyInBlock
operator|=
operator|new
name|byte
index|[
name|klength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|key
argument_list|,
name|koffset
argument_list|,
name|firstKeyInBlock
argument_list|,
literal|0
argument_list|,
name|klength
argument_list|)
expr_stmt|;
block|}
name|lastKeyBuffer
operator|=
name|key
expr_stmt|;
name|lastKeyOffset
operator|=
name|koffset
expr_stmt|;
name|lastKeyLength
operator|=
name|klength
expr_stmt|;
name|entryCount
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|outputStream
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// Save data block encoder metadata in the file info.
name|blockEncoder
operator|.
name|saveMetadata
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// Write out the end of the data blocks, then write meta data blocks.
comment|// followed by fileinfo, data block index and meta block index.
name|finishBlock
argument_list|()
expr_stmt|;
name|writeInlineBlocks
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FixedFileTrailer
name|trailer
init|=
operator|new
name|FixedFileTrailer
argument_list|(
literal|2
argument_list|,
name|HFileReaderV2
operator|.
name|MAX_MINOR_VERSION
argument_list|)
decl_stmt|;
comment|// Write out the metadata blocks if any.
if|if
condition|(
operator|!
name|metaNames
operator|.
name|isEmpty
argument_list|()
condition|)
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
name|metaNames
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
comment|// store the beginning offset
name|long
name|offset
init|=
name|outputStream
operator|.
name|getPos
argument_list|()
decl_stmt|;
comment|// write the metadata content
name|DataOutputStream
name|dos
init|=
name|fsBlockWriter
operator|.
name|startWriting
argument_list|(
name|BlockType
operator|.
name|META
argument_list|)
decl_stmt|;
name|metaData
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|write
argument_list|(
name|dos
argument_list|)
expr_stmt|;
name|fsBlockWriter
operator|.
name|writeHeaderAndData
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|totalUncompressedBytes
operator|+=
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithHeader
argument_list|()
expr_stmt|;
comment|// Add the new meta block to the meta index.
name|metaBlockIndexWriter
operator|.
name|addEntry
argument_list|(
name|metaNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|offset
argument_list|,
name|fsBlockWriter
operator|.
name|getOnDiskSizeWithHeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Load-on-open section.
comment|// Data block index.
comment|//
comment|// In version 2, this section of the file starts with the root level data
comment|// block index. We call a function that writes intermediate-level blocks
comment|// first, then root level, and returns the offset of the root level block
comment|// index.
name|long
name|rootIndexOffset
init|=
name|dataBlockIndexWriter
operator|.
name|writeIndexBlocks
argument_list|(
name|outputStream
argument_list|)
decl_stmt|;
name|trailer
operator|.
name|setLoadOnOpenOffset
argument_list|(
name|rootIndexOffset
argument_list|)
expr_stmt|;
comment|// Meta block index.
name|metaBlockIndexWriter
operator|.
name|writeSingleLevelIndex
argument_list|(
name|fsBlockWriter
operator|.
name|startWriting
argument_list|(
name|BlockType
operator|.
name|ROOT_INDEX
argument_list|)
argument_list|,
literal|"meta"
argument_list|)
expr_stmt|;
name|fsBlockWriter
operator|.
name|writeHeaderAndData
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|totalUncompressedBytes
operator|+=
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithHeader
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|includeMemstoreTS
condition|)
block|{
name|appendFileInfo
argument_list|(
name|MAX_MEMSTORE_TS_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|maxMemstoreTS
argument_list|)
argument_list|)
expr_stmt|;
name|appendFileInfo
argument_list|(
name|KEY_VALUE_VERSION
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|KEY_VALUE_VER_WITH_MEMSTORE
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// File info
name|writeFileInfo
argument_list|(
name|trailer
argument_list|,
name|fsBlockWriter
operator|.
name|startWriting
argument_list|(
name|BlockType
operator|.
name|FILE_INFO
argument_list|)
argument_list|)
expr_stmt|;
name|fsBlockWriter
operator|.
name|writeHeaderAndData
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|totalUncompressedBytes
operator|+=
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithHeader
argument_list|()
expr_stmt|;
comment|// Load-on-open data supplied by higher levels, e.g. Bloom filters.
for|for
control|(
name|BlockWritable
name|w
range|:
name|additionalLoadOnOpenData
control|)
block|{
name|fsBlockWriter
operator|.
name|writeBlock
argument_list|(
name|w
argument_list|,
name|outputStream
argument_list|)
expr_stmt|;
name|totalUncompressedBytes
operator|+=
name|fsBlockWriter
operator|.
name|getUncompressedSizeWithHeader
argument_list|()
expr_stmt|;
block|}
comment|// Now finish off the trailer.
name|trailer
operator|.
name|setNumDataIndexLevels
argument_list|(
name|dataBlockIndexWriter
operator|.
name|getNumLevels
argument_list|()
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setUncompressedDataIndexSize
argument_list|(
name|dataBlockIndexWriter
operator|.
name|getTotalUncompressedSize
argument_list|()
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setFirstDataBlockOffset
argument_list|(
name|firstDataBlockOffset
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setLastDataBlockOffset
argument_list|(
name|lastDataBlockOffset
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setComparatorClass
argument_list|(
name|comparator
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|trailer
operator|.
name|setDataIndexCount
argument_list|(
name|dataBlockIndexWriter
operator|.
name|getNumRootEntries
argument_list|()
argument_list|)
expr_stmt|;
name|finishClose
argument_list|(
name|trailer
argument_list|)
expr_stmt|;
name|fsBlockWriter
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addInlineBlockWriter
parameter_list|(
name|InlineBlockWriter
name|ibw
parameter_list|)
block|{
name|inlineBlockWriters
operator|.
name|add
argument_list|(
name|ibw
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addGeneralBloomFilter
parameter_list|(
specifier|final
name|BloomFilterWriter
name|bfw
parameter_list|)
block|{
name|this
operator|.
name|addBloomFilter
argument_list|(
name|bfw
argument_list|,
name|BlockType
operator|.
name|GENERAL_BLOOM_META
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addDeleteFamilyBloomFilter
parameter_list|(
specifier|final
name|BloomFilterWriter
name|bfw
parameter_list|)
block|{
name|this
operator|.
name|addBloomFilter
argument_list|(
name|bfw
argument_list|,
name|BlockType
operator|.
name|DELETE_FAMILY_BLOOM_META
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addBloomFilter
parameter_list|(
specifier|final
name|BloomFilterWriter
name|bfw
parameter_list|,
specifier|final
name|BlockType
name|blockType
parameter_list|)
block|{
if|if
condition|(
name|bfw
operator|.
name|getKeyCount
argument_list|()
operator|<=
literal|0
condition|)
return|return;
if|if
condition|(
name|blockType
operator|!=
name|BlockType
operator|.
name|GENERAL_BLOOM_META
operator|&&
name|blockType
operator|!=
name|BlockType
operator|.
name|DELETE_FAMILY_BLOOM_META
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Block Type: "
operator|+
name|blockType
operator|.
name|toString
argument_list|()
operator|+
literal|"is not supported"
argument_list|)
throw|;
block|}
name|additionalLoadOnOpenData
operator|.
name|add
argument_list|(
operator|new
name|BlockWritable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BlockType
name|getBlockType
parameter_list|()
block|{
return|return
name|blockType
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeToBlock
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|bfw
operator|.
name|getMetaWriter
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|Writable
name|dataWriter
init|=
name|bfw
operator|.
name|getDataWriter
argument_list|()
decl_stmt|;
if|if
condition|(
name|dataWriter
operator|!=
literal|null
condition|)
name|dataWriter
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

