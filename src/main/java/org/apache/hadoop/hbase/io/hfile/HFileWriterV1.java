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
name|ByteArrayOutputStream
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
name|io
operator|.
name|OutputStream
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
name|Compression
operator|.
name|Algorithm
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
name|regionserver
operator|.
name|MemStore
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
name|compress
operator|.
name|Compressor
import|;
end_import

begin_comment
comment|/**  * Writes version 1 HFiles. Mainly used for testing backwards-compatibility.  */
end_comment

begin_class
specifier|public
class|class
name|HFileWriterV1
extends|extends
name|AbstractHFileWriter
block|{
comment|/** Meta data block name for bloom filter parameters. */
specifier|static
specifier|final
name|String
name|BLOOM_FILTER_META_KEY
init|=
literal|"BLOOM_FILTER_META"
decl_stmt|;
comment|/** Meta data block name for bloom filter bits. */
specifier|public
specifier|static
specifier|final
name|String
name|BLOOM_FILTER_DATA_KEY
init|=
literal|"BLOOM_FILTER_DATA"
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
name|HFileWriterV1
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// A stream made per block written.
specifier|private
name|DataOutputStream
name|out
decl_stmt|;
comment|// Offset where the current block began.
specifier|private
name|long
name|blockBegin
decl_stmt|;
comment|// First keys of every block.
specifier|private
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|blockKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
comment|// Block offset in backing stream.
specifier|private
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|blockOffsets
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
comment|// Raw (decompressed) data size.
specifier|private
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|blockDataSizes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Compressor
name|compressor
decl_stmt|;
comment|// Additional byte array output stream used to fill block cache
specifier|private
name|ByteArrayOutputStream
name|baos
decl_stmt|;
specifier|private
name|DataOutputStream
name|baosDos
decl_stmt|;
specifier|private
name|int
name|blockNumber
init|=
literal|0
decl_stmt|;
specifier|static
class|class
name|WriterFactoryV1
extends|extends
name|HFile
operator|.
name|WriterFactory
block|{
name|WriterFactoryV1
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
name|Algorithm
name|compressAlgo
parameter_list|,
name|HFileDataBlockEncoder
name|dataBlockEncoder
parameter_list|,
name|KeyComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HFileWriterV1
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
name|compressAlgo
argument_list|,
name|dataBlockEncoder
argument_list|,
name|comparator
argument_list|)
return|;
block|}
block|}
comment|/** Constructor that takes a path, creates and closes the output stream. */
specifier|public
name|HFileWriterV1
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
name|compress
parameter_list|,
name|HFileDataBlockEncoder
name|blockEncoder
parameter_list|,
specifier|final
name|KeyComparator
name|comparator
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
argument_list|)
else|:
name|ostream
argument_list|,
name|path
argument_list|,
name|blockSize
argument_list|,
name|compress
argument_list|,
name|blockEncoder
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
name|SchemaMetrics
operator|.
name|configureGlobally
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * If at block boundary, opens new block.    *    * @throws IOException    */
specifier|private
name|void
name|checkBlockBoundary
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|out
operator|!=
literal|null
operator|&&
name|this
operator|.
name|out
operator|.
name|size
argument_list|()
operator|<
name|blockSize
condition|)
return|return;
name|finishBlock
argument_list|()
expr_stmt|;
name|newBlock
argument_list|()
expr_stmt|;
block|}
comment|/**    * Do the cleanup if a current block.    *    * @throws IOException    */
specifier|private
name|void
name|finishBlock
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|out
operator|==
literal|null
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
name|int
name|size
init|=
name|releaseCompressingStream
argument_list|(
name|this
operator|.
name|out
argument_list|)
decl_stmt|;
name|this
operator|.
name|out
operator|=
literal|null
expr_stmt|;
name|blockKeys
operator|.
name|add
argument_list|(
name|firstKeyInBlock
argument_list|)
expr_stmt|;
name|blockOffsets
operator|.
name|add
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|blockBegin
argument_list|)
argument_list|)
expr_stmt|;
name|blockDataSizes
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|size
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|totalUncompressedBytes
operator|+=
name|size
expr_stmt|;
name|HFile
operator|.
name|writeTimeNano
operator|.
name|addAndGet
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTimeNs
argument_list|)
expr_stmt|;
name|HFile
operator|.
name|writeOps
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|cacheConf
operator|.
name|shouldCacheDataOnWrite
argument_list|()
condition|)
block|{
name|baosDos
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// we do not do data block encoding on disk for HFile v1
name|byte
index|[]
name|bytes
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|HFileBlock
name|block
init|=
operator|new
name|HFileBlock
argument_list|(
name|BlockType
operator|.
name|DATA
argument_list|,
call|(
name|int
call|)
argument_list|(
name|outputStream
operator|.
name|getPos
argument_list|()
operator|-
name|blockBegin
argument_list|)
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
operator|-
literal|1
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
argument_list|,
name|HFileBlock
operator|.
name|FILL_HEADER
argument_list|,
name|blockBegin
argument_list|,
name|MemStore
operator|.
name|NO_PERSISTENT_TS
argument_list|)
decl_stmt|;
name|block
operator|=
name|blockEncoder
operator|.
name|diskToCacheFormat
argument_list|(
name|block
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|passSchemaMetricsTo
argument_list|(
name|block
argument_list|)
expr_stmt|;
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
name|blockBegin
argument_list|,
name|DataBlockEncoding
operator|.
name|NONE
argument_list|,
name|block
operator|.
name|getBlockType
argument_list|()
argument_list|)
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|baosDos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|blockNumber
operator|++
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
name|blockBegin
operator|=
name|outputStream
operator|.
name|getPos
argument_list|()
expr_stmt|;
name|this
operator|.
name|out
operator|=
name|getCompressingStream
argument_list|()
expr_stmt|;
name|BlockType
operator|.
name|DATA
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|firstKeyInBlock
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|cacheConf
operator|.
name|shouldCacheDataOnWrite
argument_list|()
condition|)
block|{
name|this
operator|.
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|this
operator|.
name|baosDos
operator|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|baosDos
operator|.
name|write
argument_list|(
name|HFileBlock
operator|.
name|DUMMY_HEADER
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets up a compressor and creates a compression stream on top of    * this.outputStream. Get one per block written.    *    * @return A compressing stream; if 'none' compression, returned stream does    * not compress.    *    * @throws IOException    *    * @see {@link #releaseCompressingStream(DataOutputStream)}    */
specifier|private
name|DataOutputStream
name|getCompressingStream
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|compressor
operator|=
name|compressAlgo
operator|.
name|getCompressor
argument_list|()
expr_stmt|;
comment|// Get new DOS compression stream. In tfile, the DOS, is not closed,
comment|// just finished, and that seems to be fine over there. TODO: Check
comment|// no memory retention of the DOS. Should I disable the 'flush' on the
comment|// DOS as the BCFile over in tfile does? It wants to make it so flushes
comment|// don't go through to the underlying compressed stream. Flush on the
comment|// compressed downstream should be only when done. I was going to but
comment|// looks like when we call flush in here, its legitimate flush that
comment|// should go through to the compressor.
name|OutputStream
name|os
init|=
name|this
operator|.
name|compressAlgo
operator|.
name|createCompressionStream
argument_list|(
name|this
operator|.
name|outputStream
argument_list|,
name|this
operator|.
name|compressor
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
operator|new
name|DataOutputStream
argument_list|(
name|os
argument_list|)
return|;
block|}
comment|/**    * Let go of block compressor and compressing stream gotten in call {@link    * #getCompressingStream}.    *    * @param dos    *    * @return How much was written on this stream since it was taken out.    *    * @see #getCompressingStream()    *    * @throws IOException    */
specifier|private
name|int
name|releaseCompressingStream
parameter_list|(
specifier|final
name|DataOutputStream
name|dos
parameter_list|)
throws|throws
name|IOException
block|{
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|this
operator|.
name|compressAlgo
operator|.
name|returnCompressor
argument_list|(
name|this
operator|.
name|compressor
argument_list|)
expr_stmt|;
name|this
operator|.
name|compressor
operator|=
literal|null
expr_stmt|;
return|return
name|dos
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Add a meta block to the end of the file. Call before close(). Metadata    * blocks are expensive. Fill one with a bunch of serialized data rather than    * do a metadata block per metadata instance. If metadata is small, consider    * adding to file info using {@link #appendFileInfo(byte[], byte[])}    *    * @param metaBlockName    *          name of the block    * @param content    *          will call readFields to get data later (DO NOT REUSE)    */
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
block|}
comment|/**    * Add key/value to file. Keys must be added in an order that agrees with the    * Comparator passed on construction.    *    * @param key    *          Key to add. Cannot be empty nor null.    * @param value    *          Value to add. Cannot be empty nor null.    * @throws IOException    */
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
comment|// Write length of key and value and then actual key and value bytes.
name|this
operator|.
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
name|this
operator|.
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
name|this
operator|.
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
name|this
operator|.
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
comment|// Are we the first key in this block?
if|if
condition|(
name|this
operator|.
name|firstKeyInBlock
operator|==
literal|null
condition|)
block|{
comment|// Copy the key.
name|this
operator|.
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
name|this
operator|.
name|firstKeyInBlock
argument_list|,
literal|0
argument_list|,
name|klength
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|lastKeyBuffer
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|lastKeyOffset
operator|=
name|koffset
expr_stmt|;
name|this
operator|.
name|lastKeyLength
operator|=
name|klength
expr_stmt|;
name|this
operator|.
name|entryCount
operator|++
expr_stmt|;
comment|// If we are pre-caching blocks on write, fill byte array stream
if|if
condition|(
name|cacheConf
operator|.
name|shouldCacheDataOnWrite
argument_list|()
condition|)
block|{
name|this
operator|.
name|baosDos
operator|.
name|writeInt
argument_list|(
name|klength
argument_list|)
expr_stmt|;
name|this
operator|.
name|baosDos
operator|.
name|writeInt
argument_list|(
name|vlength
argument_list|)
expr_stmt|;
name|this
operator|.
name|baosDos
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
name|this
operator|.
name|baosDos
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
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|outputStream
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// Write out the end of the data blocks, then write meta data blocks.
comment|// followed by fileinfo, data block index and meta block index.
name|finishBlock
argument_list|()
expr_stmt|;
name|FixedFileTrailer
name|trailer
init|=
operator|new
name|FixedFileTrailer
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// Write out the metadata blocks if any.
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|metaOffsets
init|=
literal|null
decl_stmt|;
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|metaDataSizes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|metaNames
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|metaOffsets
operator|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|(
name|metaNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|metaDataSizes
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|metaNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|curPos
init|=
name|outputStream
operator|.
name|getPos
argument_list|()
decl_stmt|;
name|metaOffsets
operator|.
name|add
argument_list|(
name|curPos
argument_list|)
expr_stmt|;
comment|// write the metadata content
name|DataOutputStream
name|dos
init|=
name|getCompressingStream
argument_list|()
decl_stmt|;
name|BlockType
operator|.
name|META
operator|.
name|write
argument_list|(
name|dos
argument_list|)
expr_stmt|;
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
name|int
name|size
init|=
name|releaseCompressingStream
argument_list|(
name|dos
argument_list|)
decl_stmt|;
comment|// store the metadata size
name|metaDataSizes
operator|.
name|add
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
name|writeFileInfo
argument_list|(
name|trailer
argument_list|,
name|outputStream
argument_list|)
expr_stmt|;
comment|// Write the data block index.
name|trailer
operator|.
name|setLoadOnOpenOffset
argument_list|(
name|writeBlockIndex
argument_list|(
name|this
operator|.
name|outputStream
argument_list|,
name|this
operator|.
name|blockKeys
argument_list|,
name|this
operator|.
name|blockOffsets
argument_list|,
name|this
operator|.
name|blockDataSizes
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Wrote a version 1 block index with "
operator|+
name|this
operator|.
name|blockKeys
operator|.
name|size
argument_list|()
operator|+
literal|" keys"
argument_list|)
expr_stmt|;
if|if
condition|(
name|metaNames
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Write the meta index.
name|writeBlockIndex
argument_list|(
name|this
operator|.
name|outputStream
argument_list|,
name|metaNames
argument_list|,
name|metaOffsets
argument_list|,
name|metaDataSizes
argument_list|)
expr_stmt|;
block|}
comment|// Now finish off the trailer.
name|trailer
operator|.
name|setDataIndexCount
argument_list|(
name|blockKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|finishClose
argument_list|(
name|trailer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|finishFileInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|finishFileInfo
argument_list|()
expr_stmt|;
comment|// In version 1, we store comparator name in the file info.
name|fileInfo
operator|.
name|append
argument_list|(
name|FileInfo
operator|.
name|COMPARATOR
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|comparator
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addInlineBlockWriter
parameter_list|(
name|InlineBlockWriter
name|bloomWriter
parameter_list|)
block|{
comment|// Inline blocks only exist in HFile format version 2.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Version 1 general Bloom filters are stored in two meta blocks with two different    * keys.    */
annotation|@
name|Override
specifier|public
name|void
name|addGeneralBloomFilter
parameter_list|(
name|BloomFilterWriter
name|bfw
parameter_list|)
block|{
name|appendMetaBlock
argument_list|(
name|BLOOM_FILTER_META_KEY
argument_list|,
name|bfw
operator|.
name|getMetaWriter
argument_list|()
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
block|{
name|appendMetaBlock
argument_list|(
name|BLOOM_FILTER_DATA_KEY
argument_list|,
name|dataWriter
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addDeleteFamilyBloomFilter
parameter_list|(
name|BloomFilterWriter
name|bfw
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Delete Bloom filter is not supported in HFile V1"
argument_list|)
throw|;
block|}
comment|/**    * Write out the index in the version 1 format. This conforms to the legacy    * version 1 format, but can still be read by    * {@link HFileBlockIndex.BlockIndexReader#readRootIndex(java.io.DataInputStream,    * int)}.    *    * @param out the stream to write to    * @param keys    * @param offsets    * @param uncompressedSizes in contrast with a version 2 root index format,    *          the sizes stored in the version 1 are uncompressed sizes    * @return    * @throws IOException    */
specifier|private
specifier|static
name|long
name|writeBlockIndex
parameter_list|(
specifier|final
name|FSDataOutputStream
name|out
parameter_list|,
specifier|final
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|keys
parameter_list|,
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|offsets
parameter_list|,
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|uncompressedSizes
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|pos
init|=
name|out
operator|.
name|getPos
argument_list|()
decl_stmt|;
comment|// Don't write an index if nothing in the index.
if|if
condition|(
name|keys
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|BlockType
operator|.
name|INDEX_V1
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// Write the index.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|offsets
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|uncompressedSizes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|key
init|=
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|pos
return|;
block|}
block|}
end_class

end_unit

