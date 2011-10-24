begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|FileStatus
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
name|fs
operator|.
name|PathFilter
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
name|HbaseMapWritable
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
name|io
operator|.
name|RawComparator
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

begin_comment
comment|/**  * File format for hbase.  * A file of sorted key/value pairs. Both keys and values are byte arrays.  *<p>  * The memory footprint of a HFile includes the following (below is taken from the  *<a  * href=https://issues.apache.org/jira/browse/HADOOP-3315>TFile</a> documentation  * but applies also to HFile):  *<ul>  *<li>Some constant overhead of reading or writing a compressed block.  *<ul>  *<li>Each compressed block requires one compression/decompression codec for  * I/O.  *<li>Temporary space to buffer the key.  *<li>Temporary space to buffer the value.  *</ul>  *<li>HFile index, which is proportional to the total number of Data Blocks.  * The total amount of memory needed to hold the index can be estimated as  * (56+AvgKeySize)*NumBlocks.  *</ul>  * Suggestions on performance optimization.  *<ul>  *<li>Minimum block size. We recommend a setting of minimum block size between  * 8KB to 1MB for general usage. Larger block size is preferred if files are  * primarily for sequential access. However, it would lead to inefficient random  * access (because there are more data to decompress). Smaller blocks are good  * for random access, but require more memory to hold the block index, and may  * be slower to create (because we must flush the compressor stream at the  * conclusion of each data block, which leads to an FS I/O flush). Further, due  * to the internal caching in Compression codec, the smallest possible block  * size would be around 20KB-30KB.  *<li>The current implementation does not offer true multi-threading for  * reading. The implementation uses FSDataInputStream seek()+read(), which is  * shown to be much faster than positioned-read call in single thread mode.  * However, it also means that if multiple threads attempt to access the same  * HFile (using multiple scanners) simultaneously, the actual I/O is carried out  * sequentially even if they access different DFS blocks (Reexamine! pread seems  * to be 10% faster than seek+read in my testing -- stack).  *<li>Compression codec. Use "none" if the data is not very compressable (by  * compressable, I mean a compression ratio at least 2:1). Generally, use "lzo"  * as the starting point for experimenting. "gz" overs slightly better  * compression ratio over "lzo" but requires 4x CPU to compress and 2x CPU to  * decompress, comparing to "lzo".  *</ul>  *  * For more on the background behind HFile, see<a  * href=https://issues.apache.org/jira/browse/HBASE-61>HBASE-61</a>.  *<p>  * File is made of data blocks followed by meta data blocks (if any), a fileinfo  * block, data block index, meta data block index, and a fixed size trailer  * which records the offsets at which file changes content type.  *<pre>&lt;data blocks>&lt;meta blocks>&lt;fileinfo>&lt;data index>&lt;meta index>&lt;trailer></pre>  * Each block has a bit of magic at its start.  Block are comprised of  * key/values.  In data blocks, they are both byte arrays.  Metadata blocks are  * a String key and a byte array value.  An empty file looks like this:  *<pre>&lt;fileinfo>&lt;trailer></pre>.  That is, there are not data nor meta  * blocks present.  *<p>  * TODO: Do scanners need to be able to take a start and end row?  * TODO: Should BlockIndex know the name of its file?  Should it have a Path  * that points at its file say for the case where an index lives apart from  * an HFile instance?  */
end_comment

begin_class
specifier|public
class|class
name|HFile
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
name|HFile
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Maximum length of key in HFile.    */
specifier|public
specifier|final
specifier|static
name|int
name|MAXIMUM_KEY_LENGTH
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Default block size for an HFile.    */
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_BLOCKSIZE
init|=
literal|64
operator|*
literal|1024
decl_stmt|;
comment|/**    * Default compression: none.    */
specifier|public
specifier|final
specifier|static
name|Compression
operator|.
name|Algorithm
name|DEFAULT_COMPRESSION_ALGORITHM
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
decl_stmt|;
comment|/** Minimum supported HFile format version */
specifier|public
specifier|static
specifier|final
name|int
name|MIN_FORMAT_VERSION
init|=
literal|1
decl_stmt|;
comment|/** Maximum supported HFile format version */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_FORMAT_VERSION
init|=
literal|2
decl_stmt|;
comment|/** Default compression name: none. */
specifier|public
specifier|final
specifier|static
name|String
name|DEFAULT_COMPRESSION
init|=
name|DEFAULT_COMPRESSION_ALGORITHM
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|/** Separator between HFile name and offset in block cache key */
specifier|static
specifier|final
name|char
name|CACHE_KEY_SEPARATOR
init|=
literal|'_'
decl_stmt|;
comment|// For measuring latency of "typical" reads and writes
specifier|static
specifier|volatile
name|AtomicLong
name|readOps
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|static
specifier|volatile
name|AtomicLong
name|readTimeNano
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|static
specifier|volatile
name|AtomicLong
name|writeOps
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|static
specifier|volatile
name|AtomicLong
name|writeTimeNano
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|// for test purpose
specifier|public
specifier|static
specifier|volatile
name|AtomicLong
name|dataBlockReadCnt
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|getReadOps
parameter_list|()
block|{
return|return
name|readOps
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|getReadTimeMs
parameter_list|()
block|{
return|return
name|readTimeNano
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
operator|/
literal|1000000
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|getWriteOps
parameter_list|()
block|{
return|return
name|writeOps
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|getWriteTimeMs
parameter_list|()
block|{
return|return
name|writeTimeNano
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
operator|/
literal|1000000
return|;
block|}
comment|/** API required to write an {@link HFile} */
specifier|public
interface|interface
name|Writer
extends|extends
name|Closeable
block|{
comment|/** Add an element to the file info map. */
name|void
name|appendFileInfo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|append
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|append
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** @return the path to this {@link HFile} */
name|Path
name|getPath
parameter_list|()
function_decl|;
name|String
name|getColumnFamilyName
parameter_list|()
function_decl|;
name|void
name|appendMetaBlock
parameter_list|(
name|String
name|bloomFilterMetaKey
parameter_list|,
name|Writable
name|metaWriter
parameter_list|)
function_decl|;
comment|/**      * Adds an inline block writer such as a multi-level block index writer or      * a compound Bloom filter writer.      */
name|void
name|addInlineBlockWriter
parameter_list|(
name|InlineBlockWriter
name|bloomWriter
parameter_list|)
function_decl|;
comment|/**      * Store general Bloom filter in the file. This does not deal with Bloom filter      * internals but is necessary, since Bloom filters are stored differently      * in HFile version 1 and version 2.      */
name|void
name|addGeneralBloomFilter
parameter_list|(
name|BloomFilterWriter
name|bfw
parameter_list|)
function_decl|;
comment|/**      * Store delete family Bloom filter in the file, which is only supported in      * HFile V2.      */
name|void
name|addDeleteFamilyBloomFilter
parameter_list|(
name|BloomFilterWriter
name|bfw
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * This variety of ways to construct writers is used throughout the code, and    * we want to be able to swap writer implementations.    */
specifier|public
specifier|static
specifier|abstract
class|class
name|WriterFactory
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|CacheConfig
name|cacheConf
decl_stmt|;
name|WriterFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|cacheConf
operator|=
name|cacheConf
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|Writer
name|createWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|abstract
name|Writer
name|createWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|Compression
operator|.
name|Algorithm
name|compress
parameter_list|,
specifier|final
name|KeyComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|abstract
name|Writer
name|createWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|String
name|compress
parameter_list|,
specifier|final
name|KeyComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|abstract
name|Writer
name|createWriter
parameter_list|(
specifier|final
name|FSDataOutputStream
name|ostream
parameter_list|,
specifier|final
name|int
name|blockSize
parameter_list|,
specifier|final
name|String
name|compress
parameter_list|,
specifier|final
name|KeyComparator
name|comparator
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|abstract
name|Writer
name|createWriter
parameter_list|(
specifier|final
name|FSDataOutputStream
name|ostream
parameter_list|,
specifier|final
name|int
name|blockSize
parameter_list|,
specifier|final
name|Compression
operator|.
name|Algorithm
name|compress
parameter_list|,
specifier|final
name|KeyComparator
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/** The configuration key for HFile version to use for new files */
specifier|public
specifier|static
specifier|final
name|String
name|FORMAT_VERSION_KEY
init|=
literal|"hfile.format.version"
decl_stmt|;
specifier|public
specifier|static
name|int
name|getFormatVersion
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|version
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|FORMAT_VERSION_KEY
argument_list|,
name|MAX_FORMAT_VERSION
argument_list|)
decl_stmt|;
name|checkFormatVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
return|return
name|version
return|;
block|}
comment|/**    * Returns the factory to be used to create {@link HFile} writers. Should    * always be {@link HFileWriterV2#WRITER_FACTORY_V2} in production, but    * can also be {@link HFileWriterV1#WRITER_FACTORY_V1} in testing.    */
specifier|public
specifier|static
specifier|final
name|WriterFactory
name|getWriterFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|conf
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns the factory to be used to create {@link HFile} writers. Should    * always be {@link HFileWriterV2#WRITER_FACTORY_V2} in production, but    * can also be {@link HFileWriterV1#WRITER_FACTORY_V1} in testing.    */
specifier|public
specifier|static
specifier|final
name|WriterFactory
name|getWriterFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
block|{
name|int
name|version
init|=
name|getFormatVersion
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using HFile format version "
operator|+
name|version
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|version
condition|)
block|{
case|case
literal|1
case|:
return|return
operator|new
name|HFileWriterV1
operator|.
name|WriterFactoryV1
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
return|;
case|case
literal|2
case|:
return|return
operator|new
name|HFileWriterV2
operator|.
name|WriterFactoryV2
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot create writer for HFile "
operator|+
literal|"format version "
operator|+
name|version
argument_list|)
throw|;
block|}
block|}
comment|/** An abstraction used by the block index */
specifier|public
interface|interface
name|CachingBlockReader
block|{
name|HFileBlock
name|readBlock
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|onDiskBlockSize
parameter_list|,
name|boolean
name|cacheBlock
parameter_list|,
specifier|final
name|boolean
name|pread
parameter_list|,
specifier|final
name|boolean
name|isCompaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/** An interface used by clients to open and iterate an {@link HFile}. */
specifier|public
interface|interface
name|Reader
extends|extends
name|Closeable
extends|,
name|CachingBlockReader
block|{
comment|/**      * Returns this reader's "name". Usually the last component of the path.      * Needs to be constant as the file is being moved to support caching on      * write.      */
name|String
name|getName
parameter_list|()
function_decl|;
name|String
name|getColumnFamilyName
parameter_list|()
function_decl|;
name|RawComparator
argument_list|<
name|byte
index|[]
argument_list|>
name|getComparator
parameter_list|()
function_decl|;
name|HFileScanner
name|getScanner
parameter_list|(
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
function_decl|;
name|ByteBuffer
name|getMetaBlock
parameter_list|(
name|String
name|metaBlockName
parameter_list|,
name|boolean
name|cacheBlock
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|loadFileInfo
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|byte
index|[]
name|getLastKey
parameter_list|()
function_decl|;
name|byte
index|[]
name|midkey
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|long
name|length
parameter_list|()
function_decl|;
name|long
name|getEntries
parameter_list|()
function_decl|;
name|byte
index|[]
name|getFirstKey
parameter_list|()
function_decl|;
name|long
name|indexSize
parameter_list|()
function_decl|;
name|byte
index|[]
name|getFirstRowKey
parameter_list|()
function_decl|;
name|byte
index|[]
name|getLastRowKey
parameter_list|()
function_decl|;
name|FixedFileTrailer
name|getTrailer
parameter_list|()
function_decl|;
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|getDataBlockIndexReader
parameter_list|()
function_decl|;
name|HFileScanner
name|getScanner
parameter_list|(
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|pread
parameter_list|)
function_decl|;
name|Compression
operator|.
name|Algorithm
name|getCompressionAlgorithm
parameter_list|()
function_decl|;
comment|/**      * Retrieves general Bloom filter metadata as appropriate for each      * {@link HFile} version.      * Knows nothing about how that metadata is structured.      */
name|DataInput
name|getGeneralBloomFilterMetadata
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Retrieves delete family Bloom filter metadata as appropriate for each      * {@link HFile}  version.      * Knows nothing about how that metadata is structured.      */
name|DataInput
name|getDeleteBloomFilterMetadata
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|Path
name|getPath
parameter_list|()
function_decl|;
comment|/** Close method with optional evictOnClose */
name|void
name|close
parameter_list|(
name|boolean
name|evictOnClose
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
specifier|static
name|Reader
name|pickReaderVersion
parameter_list|(
name|Path
name|path
parameter_list|,
name|FSDataInputStream
name|fsdis
parameter_list|,
name|long
name|size
parameter_list|,
name|boolean
name|closeIStream
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
throws|throws
name|IOException
block|{
name|FixedFileTrailer
name|trailer
init|=
name|FixedFileTrailer
operator|.
name|readFromStream
argument_list|(
name|fsdis
argument_list|,
name|size
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|trailer
operator|.
name|getVersion
argument_list|()
condition|)
block|{
case|case
literal|1
case|:
return|return
operator|new
name|HFileReaderV1
argument_list|(
name|path
argument_list|,
name|trailer
argument_list|,
name|fsdis
argument_list|,
name|size
argument_list|,
name|closeIStream
argument_list|,
name|cacheConf
argument_list|)
return|;
case|case
literal|2
case|:
return|return
operator|new
name|HFileReaderV2
argument_list|(
name|path
argument_list|,
name|trailer
argument_list|,
name|fsdis
argument_list|,
name|size
argument_list|,
name|closeIStream
argument_list|,
name|cacheConf
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot instantiate reader for HFile version "
operator|+
name|trailer
operator|.
name|getVersion
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|Reader
name|createReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|pickReaderVersion
argument_list|(
name|path
argument_list|,
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|)
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
literal|true
argument_list|,
name|cacheConf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Reader
name|createReader
parameter_list|(
name|Path
name|path
parameter_list|,
name|FSDataInputStream
name|fsdis
parameter_list|,
name|long
name|size
parameter_list|,
name|CacheConfig
name|cacheConf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|pickReaderVersion
argument_list|(
name|path
argument_list|,
name|fsdis
argument_list|,
name|size
argument_list|,
literal|false
argument_list|,
name|cacheConf
argument_list|)
return|;
block|}
comment|/*    * Metadata for this file.  Conjured by the writer.  Read in by the reader.    */
specifier|static
class|class
name|FileInfo
extends|extends
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
block|{
specifier|static
specifier|final
name|String
name|RESERVED_PREFIX
init|=
literal|"hfile."
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|RESERVED_PREFIX_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|LASTKEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"LASTKEY"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|AVG_KEY_LEN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"AVG_KEY_LEN"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|AVG_VALUE_LEN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"AVG_VALUE_LEN"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|COMPARATOR
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|RESERVED_PREFIX
operator|+
literal|"COMPARATOR"
argument_list|)
decl_stmt|;
comment|/**      * Append the given key/value pair to the file info, optionally checking the      * key prefix.      *      * @param k key to add      * @param v value to add      * @param checkPrefix whether to check that the provided key does not start      *          with the reserved prefix      * @return this file info object      * @throws IOException if the key or value is invalid      */
specifier|public
name|FileInfo
name|append
parameter_list|(
specifier|final
name|byte
index|[]
name|k
parameter_list|,
specifier|final
name|byte
index|[]
name|v
parameter_list|,
specifier|final
name|boolean
name|checkPrefix
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|k
operator|==
literal|null
operator|||
name|v
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Key nor value may be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|checkPrefix
operator|&&
name|isReservedFileInfoKey
argument_list|(
name|k
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Keys with a "
operator|+
name|FileInfo
operator|.
name|RESERVED_PREFIX
operator|+
literal|" are reserved"
argument_list|)
throw|;
block|}
name|put
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
comment|/** Return true if the given file info key is reserved for internal use. */
specifier|public
specifier|static
name|boolean
name|isReservedFileInfoKey
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|startsWith
argument_list|(
name|key
argument_list|,
name|FileInfo
operator|.
name|RESERVED_PREFIX_BYTES
argument_list|)
return|;
block|}
comment|/**    * Get names of supported compression algorithms. The names are acceptable by    * HFile.Writer.    *    * @return Array of strings, each represents a supported compression    *         algorithm. Currently, the following compression algorithms are    *         supported.    *<ul>    *<li>"none" - No compression.    *<li>"gz" - GZIP compression.    *</ul>    */
specifier|public
specifier|static
name|String
index|[]
name|getSupportedCompressionAlgorithms
parameter_list|()
block|{
return|return
name|Compression
operator|.
name|getSupportedAlgorithms
argument_list|()
return|;
block|}
comment|// Utility methods.
comment|/*    * @param l Long to convert to an int.    * @return<code>l</code> cast as an int.    */
specifier|static
name|int
name|longToInt
parameter_list|(
specifier|final
name|long
name|l
parameter_list|)
block|{
comment|// Expecting the size() of a block not exceeding 4GB. Assuming the
comment|// size() will wrap to negative integer if it exceeds 2GB (From tfile).
return|return
call|(
name|int
call|)
argument_list|(
name|l
operator|&
literal|0x00000000ffffffffL
argument_list|)
return|;
block|}
comment|/**    * Returns all files belonging to the given region directory. Could return an    * empty list.    *    * @param fs  The file system reference.    * @param regionDir  The region directory to scan.    * @return The list of files found.    * @throws IOException When scanning the files fails.    */
specifier|static
name|List
argument_list|<
name|Path
argument_list|>
name|getStoreFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|regionDir
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|PathFilter
name|dirFilter
init|=
operator|new
name|FSUtils
operator|.
name|DirFilter
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|familyDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|regionDir
argument_list|,
name|dirFilter
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|dir
range|:
name|familyDirs
control|)
block|{
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
operator|!
name|file
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|res
operator|.
name|add
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|res
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
throws|throws
name|IOException
block|{
name|HFilePrettyPrinter
name|prettyPrinter
init|=
operator|new
name|HFilePrettyPrinter
argument_list|()
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|prettyPrinter
operator|.
name|run
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getBlockCacheKey
parameter_list|(
name|String
name|hfileName
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
return|return
name|hfileName
operator|+
name|CACHE_KEY_SEPARATOR
operator|+
name|offset
return|;
block|}
comment|/**    * Checks the given {@link HFile} format version, and throws an exception if    * invalid. Note that if the version number comes from an input file and has    * not been verified, the caller needs to re-throw an {@link IOException} to    * indicate that this is not a software error, but corrupted input.    *    * @param version an HFile version    * @throws IllegalArgumentException if the version is invalid    */
specifier|public
specifier|static
name|void
name|checkFormatVersion
parameter_list|(
name|int
name|version
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|version
argument_list|<
name|MIN_FORMAT_VERSION
operator|||
name|version
argument_list|>
name|MAX_FORMAT_VERSION
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid HFile version: "
operator|+
name|version
operator|+
literal|" (expected to be "
operator|+
literal|"between "
operator|+
name|MIN_FORMAT_VERSION
operator|+
literal|" and "
operator|+
name|MAX_FORMAT_VERSION
operator|+
literal|")"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

