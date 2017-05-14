begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Cellersion 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY CellIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ByteBufferUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_comment
comment|/**  * CellChunkMap is an array of serialized representations of Cell  * (pointing to Chunks with full Cell data) and can be allocated both off-heap and on-heap.  *  * CellChunkMap is a byte array (chunk) holding all that is needed to access a Cell, which  * is actually saved on another deeper chunk.  * Per Cell we have a reference to this deeper byte array B (chunk ID, integer),  * offset in bytes in B (integer), length in bytes in B (integer) and seqID of the cell (long).  * In order to save reference to byte array we use the Chunk's ID given by ChunkCreator.  *  * The CellChunkMap memory layout on chunk A relevant to a deeper byte array B,  * holding the actual cell data:  *  *< header><---------------     first Cell     -----------------><-- second Cell ...  * --------------------------------------------------------------------------------------- ...  *  integer  | integer      | integer      | integer     | long     |  *  4 bytes  | 4 bytes      | 4 bytes      | 4 bytes     | 8 bytes  |  *  ChunkID  | chunkID of   | offset in B  | length of   | sequence |          ...  *  of this  | chunk B with | where Cell's | Cell's      | ID of    |  *  chunk A  | Cell data    | data starts  | data in B   | the Cell |  * --------------------------------------------------------------------------------------- ...  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CellChunkMap
extends|extends
name|CellFlatMap
block|{
specifier|private
specifier|final
name|Chunk
index|[]
name|chunks
decl_stmt|;
comment|// the array of chunks, on which the index is based
specifier|private
specifier|final
name|int
name|numOfCellsInsideChunk
decl_stmt|;
comment|// constant number of cell-representations in a chunk
comment|// each cell-representation requires three integers for chunkID (reference to the ByteBuffer),
comment|// offset and length, and one long for seqID
specifier|public
specifier|static
specifier|final
name|int
name|SIZEOF_CELL_REP
init|=
literal|3
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
decl_stmt|;
comment|/**    * C-tor for creating CellChunkMap from existing Chunk array, which must be ordered    * (decreasingly or increasingly according to parameter "descending")    * @param comparator a tool for comparing cells    * @param chunks ordered array of index chunk with cell representations    * @param min the index of the first cell (usually 0)    * @param max number of Cells or the index of the cell after the maximal cell    * @param descending the order of the given array    */
specifier|public
name|CellChunkMap
parameter_list|(
name|Comparator
argument_list|<
name|?
super|super
name|Cell
argument_list|>
name|comparator
parameter_list|,
name|Chunk
index|[]
name|chunks
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|boolean
name|descending
parameter_list|)
block|{
name|super
argument_list|(
name|comparator
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|descending
argument_list|)
expr_stmt|;
name|this
operator|.
name|chunks
operator|=
name|chunks
expr_stmt|;
name|this
operator|.
name|numOfCellsInsideChunk
operator|=
comment|// each chunk starts with its own ID following the cells data
operator|(
name|ChunkCreator
operator|.
name|getInstance
argument_list|()
operator|.
name|getChunkSize
argument_list|()
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|/
name|SIZEOF_CELL_REP
expr_stmt|;
block|}
comment|/* To be used by base (CellFlatMap) class only to create a sub-CellFlatMap   * Should be used only to create only CellChunkMap from CellChunkMap */
annotation|@
name|Override
specifier|protected
name|CellFlatMap
name|createSubCellFlatMap
parameter_list|(
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|boolean
name|descending
parameter_list|)
block|{
return|return
operator|new
name|CellChunkMap
argument_list|(
name|this
operator|.
name|comparator
argument_list|()
argument_list|,
name|this
operator|.
name|chunks
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|descending
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Cell
name|getCell
parameter_list|(
name|int
name|i
parameter_list|)
block|{
comment|// get the index of the relevant chunk inside chunk array
name|int
name|chunkIndex
init|=
operator|(
name|i
operator|/
name|numOfCellsInsideChunk
operator|)
decl_stmt|;
name|ByteBuffer
name|block
init|=
name|chunks
index|[
name|chunkIndex
index|]
operator|.
name|getData
argument_list|()
decl_stmt|;
comment|// get the ByteBuffer of the relevant chunk
name|int
name|j
init|=
name|i
operator|-
name|chunkIndex
operator|*
name|numOfCellsInsideChunk
decl_stmt|;
comment|// get the index of the cell-representation
comment|// find inside the offset inside the chunk holding the index, skip bytes for chunk id
name|int
name|offsetInBytes
init|=
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
name|j
operator|*
name|SIZEOF_CELL_REP
decl_stmt|;
comment|// find the chunk holding the data of the cell, the chunkID is stored first
name|int
name|chunkId
init|=
name|ByteBufferUtils
operator|.
name|toInt
argument_list|(
name|block
argument_list|,
name|offsetInBytes
argument_list|)
decl_stmt|;
name|Chunk
name|chunk
init|=
name|ChunkCreator
operator|.
name|getInstance
argument_list|()
operator|.
name|getChunk
argument_list|(
name|chunkId
argument_list|)
decl_stmt|;
if|if
condition|(
name|chunk
operator|==
literal|null
condition|)
block|{
comment|// this should not happen, putting an assertion here at least for the testing period
assert|assert
literal|false
assert|;
block|}
comment|// find the offset of the data of the cell, skip integer for chunkID, offset is stored second
name|int
name|offsetOfCell
init|=
name|ByteBufferUtils
operator|.
name|toInt
argument_list|(
name|block
argument_list|,
name|offsetInBytes
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
comment|// find the length of the data of the cell, skip two integers for chunkID and offset,
comment|// length is stored third
name|int
name|lengthOfCell
init|=
name|ByteBufferUtils
operator|.
name|toInt
argument_list|(
name|block
argument_list|,
name|offsetInBytes
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
comment|// find the seqID of the cell, skip three integers for chunkID, offset, and length
comment|// the seqID is plain written as part of the cell representation
name|long
name|cellSeqID
init|=
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|block
argument_list|,
name|offsetInBytes
operator|+
literal|3
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buf
init|=
name|chunk
operator|.
name|getData
argument_list|()
decl_stmt|;
comment|// get the ByteBuffer where the cell data is stored
if|if
condition|(
name|buf
operator|==
literal|null
condition|)
block|{
comment|// this should not happen, putting an assertion here at least for the testing period
assert|assert
literal|false
assert|;
block|}
return|return
operator|new
name|ByteBufferChunkCell
argument_list|(
name|buf
argument_list|,
name|offsetOfCell
argument_list|,
name|lengthOfCell
argument_list|,
name|cellSeqID
argument_list|)
return|;
block|}
block|}
end_class

end_unit

