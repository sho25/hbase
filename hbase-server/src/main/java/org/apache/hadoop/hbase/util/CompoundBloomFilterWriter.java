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
name|util
package|;
end_package

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
name|DataOutput
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|InlineBlockWriter
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
comment|/**  * Adds methods required for writing a compound Bloom filter to the data  * section of an {@link org.apache.hadoop.hbase.io.hfile.HFile} to the  * {@link CompoundBloomFilter} class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompoundBloomFilterWriter
extends|extends
name|CompoundBloomFilterBase
implements|implements
name|BloomFilterWriter
implements|,
name|InlineBlockWriter
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
name|CompoundBloomFilterWriter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The current chunk being written to */
specifier|private
name|ByteBloomFilter
name|chunk
decl_stmt|;
comment|/** Previous chunk, so that we can create another similar chunk */
specifier|private
name|ByteBloomFilter
name|prevChunk
decl_stmt|;
comment|/** Maximum fold factor */
specifier|private
name|int
name|maxFold
decl_stmt|;
comment|/** The size of individual Bloom filter chunks to create */
specifier|private
name|int
name|chunkByteSize
decl_stmt|;
comment|/** A Bloom filter chunk enqueued for writing */
specifier|private
specifier|static
class|class
name|ReadyChunk
block|{
name|int
name|chunkId
decl_stmt|;
name|byte
index|[]
name|firstKey
decl_stmt|;
name|ByteBloomFilter
name|chunk
decl_stmt|;
block|}
specifier|private
name|Queue
argument_list|<
name|ReadyChunk
argument_list|>
name|readyChunks
init|=
operator|new
name|LinkedList
argument_list|<
name|ReadyChunk
argument_list|>
argument_list|()
decl_stmt|;
comment|/** The first key in the current Bloom filter chunk. */
specifier|private
name|byte
index|[]
name|firstKeyInChunk
init|=
literal|null
decl_stmt|;
specifier|private
name|HFileBlockIndex
operator|.
name|BlockIndexWriter
name|bloomBlockIndexWriter
init|=
operator|new
name|HFileBlockIndex
operator|.
name|BlockIndexWriter
argument_list|()
decl_stmt|;
comment|/** Whether to cache-on-write compound Bloom filter chunks */
specifier|private
name|boolean
name|cacheOnWrite
decl_stmt|;
comment|/**    * @param chunkByteSizeHint    *          each chunk's size in bytes. The real chunk size might be different    *          as required by the fold factor.    * @param errorRate    *          target false positive rate    * @param hashType    *          hash function type to use    * @param maxFold    *          maximum degree of folding allowed    */
specifier|public
name|CompoundBloomFilterWriter
parameter_list|(
name|int
name|chunkByteSizeHint
parameter_list|,
name|float
name|errorRate
parameter_list|,
name|int
name|hashType
parameter_list|,
name|int
name|maxFold
parameter_list|,
name|boolean
name|cacheOnWrite
parameter_list|,
name|CellComparator
name|comparator
parameter_list|)
block|{
name|chunkByteSize
operator|=
name|ByteBloomFilter
operator|.
name|computeFoldableByteSize
argument_list|(
name|chunkByteSizeHint
operator|*
literal|8L
argument_list|,
name|maxFold
argument_list|)
expr_stmt|;
name|this
operator|.
name|errorRate
operator|=
name|errorRate
expr_stmt|;
name|this
operator|.
name|hashType
operator|=
name|hashType
expr_stmt|;
name|this
operator|.
name|maxFold
operator|=
name|maxFold
expr_stmt|;
name|this
operator|.
name|cacheOnWrite
operator|=
name|cacheOnWrite
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldWriteBlock
parameter_list|(
name|boolean
name|closing
parameter_list|)
block|{
name|enqueueReadyChunk
argument_list|(
name|closing
argument_list|)
expr_stmt|;
return|return
operator|!
name|readyChunks
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * Enqueue the current chunk if it is ready to be written out.    *    * @param closing true if we are closing the file, so we do not expect new    *        keys to show up    */
specifier|private
name|void
name|enqueueReadyChunk
parameter_list|(
name|boolean
name|closing
parameter_list|)
block|{
if|if
condition|(
name|chunk
operator|==
literal|null
operator|||
operator|(
name|chunk
operator|.
name|getKeyCount
argument_list|()
operator|<
name|chunk
operator|.
name|getMaxKeys
argument_list|()
operator|&&
operator|!
name|closing
operator|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|firstKeyInChunk
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Trying to enqueue a chunk, "
operator|+
literal|"but first key is null: closing="
operator|+
name|closing
operator|+
literal|", keyCount="
operator|+
name|chunk
operator|.
name|getKeyCount
argument_list|()
operator|+
literal|", maxKeys="
operator|+
name|chunk
operator|.
name|getMaxKeys
argument_list|()
argument_list|)
throw|;
block|}
name|ReadyChunk
name|readyChunk
init|=
operator|new
name|ReadyChunk
argument_list|()
decl_stmt|;
name|readyChunk
operator|.
name|chunkId
operator|=
name|numChunks
operator|-
literal|1
expr_stmt|;
name|readyChunk
operator|.
name|chunk
operator|=
name|chunk
expr_stmt|;
name|readyChunk
operator|.
name|firstKey
operator|=
name|firstKeyInChunk
expr_stmt|;
name|readyChunks
operator|.
name|add
argument_list|(
name|readyChunk
argument_list|)
expr_stmt|;
name|long
name|prevMaxKeys
init|=
name|chunk
operator|.
name|getMaxKeys
argument_list|()
decl_stmt|;
name|long
name|prevByteSize
init|=
name|chunk
operator|.
name|getByteSize
argument_list|()
decl_stmt|;
name|chunk
operator|.
name|compactBloom
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
operator|&&
name|prevByteSize
operator|!=
name|chunk
operator|.
name|getByteSize
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Compacted Bloom chunk #"
operator|+
name|readyChunk
operator|.
name|chunkId
operator|+
literal|" from ["
operator|+
name|prevMaxKeys
operator|+
literal|" max keys, "
operator|+
name|prevByteSize
operator|+
literal|" bytes] to ["
operator|+
name|chunk
operator|.
name|getMaxKeys
argument_list|()
operator|+
literal|" max keys, "
operator|+
name|chunk
operator|.
name|getByteSize
argument_list|()
operator|+
literal|" bytes]"
argument_list|)
expr_stmt|;
block|}
name|totalMaxKeys
operator|+=
name|chunk
operator|.
name|getMaxKeys
argument_list|()
expr_stmt|;
name|totalByteSize
operator|+=
name|chunk
operator|.
name|getByteSize
argument_list|()
expr_stmt|;
name|firstKeyInChunk
operator|=
literal|null
expr_stmt|;
name|prevChunk
operator|=
name|chunk
expr_stmt|;
name|chunk
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Adds a Bloom filter key. This key must be greater than the previous key,    * as defined by the comparator this compound Bloom filter is configured    * with. For efficiency, key monotonicity is not checked here. See    * {@link org.apache.hadoop.hbase.regionserver.StoreFile.Writer#append(    * org.apache.hadoop.hbase.Cell)} for the details of deduplication.    */
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|bloomKey
parameter_list|,
name|int
name|keyOffset
parameter_list|,
name|int
name|keyLength
parameter_list|)
block|{
if|if
condition|(
name|bloomKey
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
name|enqueueReadyChunk
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|chunk
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|firstKeyInChunk
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"First key in chunk already set: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|firstKeyInChunk
argument_list|)
argument_list|)
throw|;
block|}
name|firstKeyInChunk
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bloomKey
argument_list|,
name|keyOffset
argument_list|,
name|keyOffset
operator|+
name|keyLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|prevChunk
operator|==
literal|null
condition|)
block|{
comment|// First chunk
name|chunk
operator|=
name|ByteBloomFilter
operator|.
name|createBySize
argument_list|(
name|chunkByteSize
argument_list|,
name|errorRate
argument_list|,
name|hashType
argument_list|,
name|maxFold
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Use the same parameters as the last chunk, but a new array and
comment|// a zero key count.
name|chunk
operator|=
name|prevChunk
operator|.
name|createAnother
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|chunk
operator|.
name|getKeyCount
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"keyCount="
operator|+
name|chunk
operator|.
name|getKeyCount
argument_list|()
operator|+
literal|"> 0"
argument_list|)
throw|;
block|}
name|chunk
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
operator|++
name|numChunks
expr_stmt|;
block|}
name|chunk
operator|.
name|add
argument_list|(
name|bloomKey
argument_list|,
name|keyOffset
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
operator|++
name|totalKeyCount
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeInlineBlock
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// We don't remove the chunk from the queue here, because we might need it
comment|// again for cache-on-write.
name|ReadyChunk
name|readyChunk
init|=
name|readyChunks
operator|.
name|peek
argument_list|()
decl_stmt|;
name|ByteBloomFilter
name|readyChunkBloom
init|=
name|readyChunk
operator|.
name|chunk
decl_stmt|;
name|readyChunkBloom
operator|.
name|getDataWriter
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|blockWritten
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|onDiskSize
parameter_list|,
name|int
name|uncompressedSize
parameter_list|)
block|{
name|ReadyChunk
name|readyChunk
init|=
name|readyChunks
operator|.
name|remove
argument_list|()
decl_stmt|;
name|bloomBlockIndexWriter
operator|.
name|addEntry
argument_list|(
name|readyChunk
operator|.
name|firstKey
argument_list|,
name|offset
argument_list|,
name|onDiskSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|BlockType
name|getInlineBlockType
parameter_list|()
block|{
return|return
name|BlockType
operator|.
name|BLOOM_CHUNK
return|;
block|}
specifier|private
class|class
name|MetaWriter
implements|implements
name|Writable
block|{
specifier|protected
name|MetaWriter
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cant read with this class."
argument_list|)
throw|;
block|}
comment|/**      * This is modeled after {@link ByteBloomFilter.MetaWriter} for simplicity,      * although the two metadata formats do not have to be consistent. This      * does have to be consistent with how {@link      * CompoundBloomFilter#CompoundBloomFilter(DataInput,      * org.apache.hadoop.hbase.io.hfile.HFile.Reader)} reads fields.      */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|VERSION
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|getByteSize
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|prevChunk
operator|.
name|getHashCount
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|prevChunk
operator|.
name|getHashType
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|getKeyCount
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|getMaxKeys
argument_list|()
argument_list|)
expr_stmt|;
comment|// Fields that don't have equivalents in ByteBloomFilter.
name|out
operator|.
name|writeInt
argument_list|(
name|numChunks
argument_list|)
expr_stmt|;
if|if
condition|(
name|comparator
operator|!=
literal|null
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
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
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Internally writes a 0 vint if the byte[] is null
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Write a single-level index without compression or block header.
name|bloomBlockIndexWriter
operator|.
name|writeSingleLevelIndex
argument_list|(
name|out
argument_list|,
literal|"Bloom filter"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getMetaWriter
parameter_list|()
block|{
return|return
operator|new
name|MetaWriter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|compactBloom
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|allocBloom
parameter_list|()
block|{
comment|// Nothing happens here. All allocation happens on demand.
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getDataWriter
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getCacheOnWrite
parameter_list|()
block|{
return|return
name|cacheOnWrite
return|;
block|}
block|}
end_class

end_unit

