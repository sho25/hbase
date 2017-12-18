begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Set
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
name|BlockingQueue
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
name|ConcurrentSkipListSet
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
name|LinkedBlockingQueue
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
name|AtomicBoolean
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
name|AtomicInteger
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
name|AtomicReference
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
name|locks
operator|.
name|ReentrantLock
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
name|ExtendedCell
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A memstore-local allocation buffer.  *<p>  * The MemStoreLAB is basically a bump-the-pointer allocator that allocates  * big (2MB) byte[] chunks from and then doles it out to threads that request  * slices into the array.  *<p>  * The purpose of this class is to combat heap fragmentation in the  * regionserver. By ensuring that all Cells in a given memstore refer  * only to large chunks of contiguous memory, we ensure that large blocks  * get freed up when the memstore is flushed.  *<p>  * Without the MSLAB, the byte array allocated during insertion end up  * interleaved throughout the heap, and the old generation gets progressively  * more fragmented until a stop-the-world compacting collection occurs.  *<p>  * TODO: we should probably benchmark whether word-aligning the allocations  * would provide a performance improvement - probably would speed up the  * Bytes.toLong/Bytes.toInt calls in KeyValue, but some of those are cached  * anyway.  * The chunks created by this MemStoreLAB can get pooled at {@link ChunkCreator}.  * When the Chunk comes from pool, it can be either an on heap or an off heap backed chunk. The chunks,  * which this MemStoreLAB creates on its own (when no chunk available from pool), those will be  * always on heap backed.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemStoreLABImpl
implements|implements
name|MemStoreLAB
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MemStoreLABImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AtomicReference
argument_list|<
name|Chunk
argument_list|>
name|curChunk
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Lock to manage multiple handlers requesting for a chunk
specifier|private
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
comment|// A set of chunks contained by this memstore LAB
annotation|@
name|VisibleForTesting
name|Set
argument_list|<
name|Integer
argument_list|>
name|chunks
init|=
operator|new
name|ConcurrentSkipListSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|chunkSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxAlloc
decl_stmt|;
specifier|private
specifier|final
name|ChunkCreator
name|chunkCreator
decl_stmt|;
specifier|private
specifier|final
name|CompactingMemStore
operator|.
name|IndexType
name|idxType
decl_stmt|;
comment|// what index is used for corresponding segment
comment|// This flag is for closing this instance, its set when clearing snapshot of
comment|// memstore
specifier|private
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
comment|// This flag is for reclaiming chunks. Its set when putting chunks back to
comment|// pool
specifier|private
name|AtomicBoolean
name|reclaimed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Current count of open scanners which reading data from this MemStoreLAB
specifier|private
specifier|final
name|AtomicInteger
name|openScannerCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
comment|// Used in testing
specifier|public
name|MemStoreLABImpl
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MemStoreLABImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|chunkSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|CHUNK_SIZE_KEY
argument_list|,
name|CHUNK_SIZE_DEFAULT
argument_list|)
expr_stmt|;
name|maxAlloc
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_ALLOC_KEY
argument_list|,
name|MAX_ALLOC_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|chunkCreator
operator|=
name|ChunkCreator
operator|.
name|getInstance
argument_list|()
expr_stmt|;
comment|// if we don't exclude allocations>CHUNK_SIZE, we'd infiniteloop on one!
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|maxAlloc
operator|<=
name|chunkSize
argument_list|,
name|MAX_ALLOC_KEY
operator|+
literal|" must be less than "
operator|+
name|CHUNK_SIZE_KEY
argument_list|)
expr_stmt|;
name|idxType
operator|=
name|CompactingMemStore
operator|.
name|IndexType
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_INDEX_KEY
argument_list|,
name|CompactingMemStore
operator|.
name|COMPACTING_MEMSTORE_INDEX_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|copyCellInto
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|int
name|size
init|=
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|size
operator|>=
literal|0
argument_list|,
literal|"negative size"
argument_list|)
expr_stmt|;
comment|// Callers should satisfy large allocations directly from JVM since they
comment|// don't cause fragmentation as badly.
if|if
condition|(
name|size
operator|>
name|maxAlloc
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Chunk
name|c
init|=
literal|null
decl_stmt|;
name|int
name|allocOffset
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|// Try to get the chunk
name|c
operator|=
name|getOrMakeChunk
argument_list|()
expr_stmt|;
comment|// we may get null because the some other thread succeeded in getting the lock
comment|// and so the current thread has to try again to make its chunk or grab the chunk
comment|// that the other thread created
comment|// Try to allocate from this chunk
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
name|allocOffset
operator|=
name|c
operator|.
name|alloc
argument_list|(
name|size
argument_list|)
expr_stmt|;
if|if
condition|(
name|allocOffset
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// We succeeded - this is the common case - small alloc
comment|// from a big buffer
break|break;
block|}
comment|// not enough space!
comment|// try to retire this chunk
name|tryRetireChunk
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|copyToChunkCell
argument_list|(
name|cell
argument_list|,
name|c
operator|.
name|getData
argument_list|()
argument_list|,
name|allocOffset
argument_list|,
name|size
argument_list|)
return|;
block|}
comment|/**    * Clone the passed cell by copying its data into the passed buf and create a cell with a chunkid    * out of it    */
specifier|private
name|Cell
name|copyToChunkCell
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|int
name|tagsLen
init|=
name|cell
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|cell
operator|instanceof
name|ExtendedCell
condition|)
block|{
operator|(
operator|(
name|ExtendedCell
operator|)
name|cell
operator|)
operator|.
name|write
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Normally all Cell impls within Server will be of type ExtendedCell. Just considering the
comment|// other case also. The data fragments within Cell is copied into buf as in KeyValue
comment|// serialization format only.
name|KeyValueUtil
operator|.
name|appendTo
argument_list|(
name|cell
argument_list|,
name|buf
argument_list|,
name|offset
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// TODO : write the seqid here. For writing seqId we should create a new cell type so
comment|// that seqId is not used as the state
if|if
condition|(
name|tagsLen
operator|==
literal|0
condition|)
block|{
comment|// When tagsLen is 0, make a NoTagsByteBufferKeyValue version. This is an optimized class
comment|// which directly return tagsLen as 0. So we avoid parsing many length components in
comment|// reading the tagLength stored in the backing buffer. The Memstore addition of every Cell
comment|// call getTagsLength().
return|return
operator|new
name|NoTagByteBufferChunkCell
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|len
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ByteBufferChunkCell
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|len
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * Close this instance since it won't be used any more, try to put the chunks    * back to pool    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|closed
operator|=
literal|true
expr_stmt|;
comment|// We could put back the chunks to pool for reusing only when there is no
comment|// opening scanner which will read their data
name|int
name|count
init|=
name|openScannerCount
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
name|recycleChunks
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Called when opening a scanner on the data of this MemStoreLAB    */
annotation|@
name|Override
specifier|public
name|void
name|incScannerCount
parameter_list|()
block|{
name|this
operator|.
name|openScannerCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
comment|/**    * Called when closing a scanner on the data of this MemStoreLAB    */
annotation|@
name|Override
specifier|public
name|void
name|decScannerCount
parameter_list|()
block|{
name|int
name|count
init|=
name|this
operator|.
name|openScannerCount
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|closed
operator|&&
name|count
operator|==
literal|0
condition|)
block|{
name|recycleChunks
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|recycleChunks
parameter_list|()
block|{
if|if
condition|(
name|reclaimed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|chunkCreator
operator|.
name|putbackChunks
argument_list|(
name|chunks
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Try to retire the current chunk if it is still    *<code>c</code>. Postcondition is that curChunk.get()    * != c    * @param c the chunk to retire    * @return true if we won the race to retire the chunk    */
specifier|private
name|void
name|tryRetireChunk
parameter_list|(
name|Chunk
name|c
parameter_list|)
block|{
name|curChunk
operator|.
name|compareAndSet
argument_list|(
name|c
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// If the CAS succeeds, that means that we won the race
comment|// to retire the chunk. We could use this opportunity to
comment|// update metrics on external fragmentation.
comment|//
comment|// If the CAS fails, that means that someone else already
comment|// retired the chunk for us.
block|}
comment|/**    * Get the current chunk, or, if there is no current chunk,    * allocate a new one from the JVM.    */
specifier|private
name|Chunk
name|getOrMakeChunk
parameter_list|()
block|{
comment|// Try to get the chunk
name|Chunk
name|c
init|=
name|curChunk
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
return|return
name|c
return|;
block|}
comment|// No current chunk, so we want to allocate one. We race
comment|// against other allocators to CAS in an uninitialized chunk
comment|// (which is cheap to allocate)
if|if
condition|(
name|lock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
try|try
block|{
comment|// once again check inside the lock
name|c
operator|=
name|curChunk
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
return|return
name|c
return|;
block|}
name|c
operator|=
name|this
operator|.
name|chunkCreator
operator|.
name|getChunk
argument_list|(
name|idxType
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
comment|// set the curChunk. No need of CAS as only one thread will be here
name|curChunk
operator|.
name|set
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|chunks
operator|.
name|add
argument_list|(
name|c
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/* Creating chunk to be used as index chunk in CellChunkMap, part of the chunks array.   ** Returning a new chunk, without replacing current chunk,   ** meaning MSLABImpl does not make the returned chunk as CurChunk.   ** The space on this chunk will be allocated externally.   ** The interface is only for external callers   */
annotation|@
name|Override
specifier|public
name|Chunk
name|getNewExternalChunk
parameter_list|()
block|{
comment|// the new chunk is going to be part of the chunk array and will always be referenced
name|Chunk
name|c
init|=
name|this
operator|.
name|chunkCreator
operator|.
name|getChunk
argument_list|()
decl_stmt|;
name|chunks
operator|.
name|add
argument_list|(
name|c
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
comment|/* Creating chunk to be used as data chunk in CellChunkMap.   ** This chunk is bigger than normal constant chunk size, and thus called JumboChunk.   ** JumboChunk is used for jumbo cell (which size is bigger than normal chunk). It is allocated   ** once per cell. So even if there is space this is not reused.   ** Jumbo Chunks are used only for CCM and thus are created only in   ** CompactingMemStore.IndexType.CHUNK_MAP type.   ** Returning a new chunk, without replacing current chunk,   ** meaning MSLABImpl does not make the returned chunk as CurChunk.   ** The space on this chunk will be allocated externally.   ** The interface is only for external callers   */
annotation|@
name|Override
specifier|public
name|Chunk
name|getNewExternalJumboChunk
parameter_list|(
name|int
name|size
parameter_list|)
block|{
comment|// the new chunk is going to hold the jumbo cell data and need to be referenced by a strong map
comment|// thus giving the CCM index type
name|Chunk
name|c
init|=
name|this
operator|.
name|chunkCreator
operator|.
name|getJumboChunk
argument_list|(
name|CompactingMemStore
operator|.
name|IndexType
operator|.
name|CHUNK_MAP
argument_list|,
name|size
argument_list|)
decl_stmt|;
name|chunks
operator|.
name|add
argument_list|(
name|c
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
annotation|@
name|VisibleForTesting
name|Chunk
name|getCurrentChunk
parameter_list|()
block|{
return|return
name|this
operator|.
name|curChunk
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|BlockingQueue
argument_list|<
name|Chunk
argument_list|>
name|getPooledChunks
parameter_list|()
block|{
name|BlockingQueue
argument_list|<
name|Chunk
argument_list|>
name|pooledChunks
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|id
range|:
name|this
operator|.
name|chunks
control|)
block|{
name|Chunk
name|chunk
init|=
name|chunkCreator
operator|.
name|getChunk
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|chunk
operator|!=
literal|null
operator|&&
name|chunk
operator|.
name|isFromPool
argument_list|()
condition|)
block|{
name|pooledChunks
operator|.
name|add
argument_list|(
name|chunk
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|pooledChunks
return|;
block|}
annotation|@
name|VisibleForTesting
name|Integer
name|getNumOfChunksReturnedToPool
parameter_list|()
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Integer
name|id
range|:
name|this
operator|.
name|chunks
control|)
block|{
if|if
condition|(
name|chunkCreator
operator|.
name|isChunkInPool
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
block|}
return|return
name|i
return|;
block|}
block|}
end_class

end_unit

