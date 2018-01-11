begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HeapSize
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

begin_comment
comment|/**  * Extension to {@link Cell} with server side required functions. Server side Cell implementations  * must implement this.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ExtendedCell
extends|extends
name|RawCell
extends|,
name|HeapSize
extends|,
name|Cloneable
block|{
name|int
name|CELL_NOT_BASED_ON_CHUNK
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Write this cell to an OutputStream in a {@link KeyValue} format.    *<br> KeyValue format<br>    *<code>&lt;4 bytes keylength&gt;&lt;4 bytes valuelength&gt;&lt;2 bytes rowlength&gt;    *&lt;row&gt;&lt;1 byte columnfamilylength&gt;&lt;columnfamily&gt;&lt;columnqualifier&gt;    *&lt;8 bytes timestamp&gt;&lt;1 byte keytype&gt;&lt;value&gt;&lt;2 bytes tagslength&gt;    *&lt;tags&gt;</code>    * @param out Stream to which cell has to be written    * @param withTags Whether to write tags.    * @return how many bytes are written.    * @throws IOException    */
comment|// TODO remove the boolean param once HBASE-16706 is done.
specifier|default
name|int
name|write
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|boolean
name|withTags
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Key length and then value length
name|ByteBufferUtils
operator|.
name|putInt
argument_list|(
name|out
argument_list|,
name|KeyValueUtil
operator|.
name|keyLength
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|putInt
argument_list|(
name|out
argument_list|,
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// Key
name|PrivateCellUtil
operator|.
name|writeFlatKey
argument_list|(
name|this
argument_list|,
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|getValueLength
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Value
name|out
operator|.
name|write
argument_list|(
name|getValueArray
argument_list|()
argument_list|,
name|getValueOffset
argument_list|()
argument_list|,
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Tags length and tags byte array
if|if
condition|(
name|withTags
operator|&&
name|getTagsLength
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Tags length
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|getTagsLength
argument_list|()
operator|>>
literal|8
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|getTagsLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Tags byte array
name|out
operator|.
name|write
argument_list|(
name|getTagsArray
argument_list|()
argument_list|,
name|getTagsOffset
argument_list|()
argument_list|,
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|getSerializedSize
argument_list|(
name|withTags
argument_list|)
return|;
block|}
comment|/**    * @param withTags Whether to write tags.    * @return Bytes count required to serialize this Cell in a {@link KeyValue} format.    *<br> KeyValue format<br>    *<code>&lt;4 bytes keylength&gt;&lt;4 bytes valuelength&gt;&lt;2 bytes rowlength&gt;    *&lt;row&gt;&lt;1 byte columnfamilylength&gt;&lt;columnfamily&gt;&lt;columnqualifier&gt;    *&lt;8 bytes timestamp&gt;&lt;1 byte keytype&gt;&lt;value&gt;&lt;2 bytes tagslength&gt;    *&lt;tags&gt;</code>    */
comment|// TODO remove the boolean param once HBASE-16706 is done.
specifier|default
name|int
name|getSerializedSize
parameter_list|(
name|boolean
name|withTags
parameter_list|)
block|{
return|return
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|getRowLength
argument_list|()
argument_list|,
name|getFamilyLength
argument_list|()
argument_list|,
name|getQualifierLength
argument_list|()
argument_list|,
name|getValueLength
argument_list|()
argument_list|,
name|getTagsLength
argument_list|()
argument_list|,
name|withTags
argument_list|)
return|;
block|}
comment|/**    * Write this Cell into the given buf's offset in a {@link KeyValue} format.    * @param buf The buffer where to write the Cell.    * @param offset The offset within buffer, to write the Cell.    */
specifier|default
name|void
name|write
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|KeyValueUtil
operator|.
name|appendTo
argument_list|(
name|this
argument_list|,
name|buf
argument_list|,
name|offset
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Does a deep copy of the contents to a new memory area and returns it as a new cell.    * @return The deep cloned cell    */
specifier|default
name|ExtendedCell
name|deepClone
parameter_list|()
block|{
comment|// When being added to the memstore, deepClone() is called and KeyValue has less heap overhead.
return|return
operator|new
name|KeyValue
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Extracts the id of the backing bytebuffer of this cell if it was obtained from fixed sized    * chunks as in case of MemstoreLAB    * @return the chunk id if the cell is backed by fixed sized Chunks, else return -1    */
specifier|default
name|int
name|getChunkId
parameter_list|()
block|{
return|return
name|CELL_NOT_BASED_ON_CHUNK
return|;
block|}
comment|/**    * Sets with the given seqId.    * @param seqId sequence ID    */
name|void
name|setSequenceId
parameter_list|(
name|long
name|seqId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Sets with the given timestamp.    * @param ts timestamp    */
name|void
name|setTimestamp
parameter_list|(
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Sets with the given timestamp.    * @param ts buffer containing the timestamp value    */
name|void
name|setTimestamp
parameter_list|(
name|byte
index|[]
name|ts
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * A region-specific unique monotonically increasing sequence ID given to each Cell. It always    * exists for cells in the memstore but is not retained forever. It will be kept for    * {@link HConstants#KEEP_SEQID_PERIOD} days, but generally becomes irrelevant after the cell's    * row is no longer involved in any operations that require strict consistency.    * @return seqId (always&gt; 0 if exists), or 0 if it no longer exists    */
name|long
name|getSequenceId
parameter_list|()
function_decl|;
comment|/**    * Contiguous raw bytes representing tags that may start at any index in the containing array.    * @return the tags byte array    */
name|byte
index|[]
name|getTagsArray
parameter_list|()
function_decl|;
comment|/**    * @return the first offset where the tags start in the Cell    */
name|int
name|getTagsOffset
parameter_list|()
function_decl|;
comment|/**    * HBase internally uses 2 bytes to store tags length in Cell. As the tags length is always a    * non-negative number, to make good use of the sign bit, the max of tags length is defined 2 *    * Short.MAX_VALUE + 1 = 65535. As a result, the return type is int, because a short is not    * capable of handling that. Please note that even if the return type is int, the max tags length    * is far less than Integer.MAX_VALUE.    * @return the total length of the tags in the Cell.    */
name|int
name|getTagsLength
parameter_list|()
function_decl|;
comment|/**    * @return The byte representation of the KeyValue.TYPE of this cell: one of Put, Delete, etc    */
name|byte
name|getTypeByte
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

