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

begin_comment
comment|/**  * Extension to {@link Cell} with server side required functions. Server side Cell implementations  * must implement this.  * @see SettableSequenceId  * @see SettableTimestamp  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
specifier|public
interface|interface
name|ExtendedCell
extends|extends
name|Cell
extends|,
name|SettableSequenceId
extends|,
name|SettableTimestamp
extends|,
name|HeapSize
extends|,
name|Cloneable
block|{
specifier|public
specifier|static
name|int
name|CELL_NOT_BASED_ON_CHUNK
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Write this cell to an OutputStream in a {@link KeyValue} format.    *<br> KeyValue format<br>    *<code>&lt;4 bytes keylength&gt;&lt;4 bytes valuelength&gt;&lt;2 bytes rowlength&gt;    *&lt;row&gt;&lt;1 byte columnfamilylength&gt;&lt;columnfamily&gt;&lt;columnqualifier&gt;    *&lt;8 bytes timestamp&gt;&lt;1 byte keytype&gt;&lt;value&gt;&lt;2 bytes tagslength&gt;    *&lt;tags&gt;</code>    * @param out Stream to which cell has to be written    * @param withTags Whether to write tags.    * @return how many bytes are written.    * @throws IOException    */
comment|// TODO remove the boolean param once HBASE-16706 is done.
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
function_decl|;
comment|/**    * @param withTags Whether to write tags.    * @return Bytes count required to serialize this Cell in a {@link KeyValue} format.    *<br> KeyValue format<br>    *<code>&lt;4 bytes keylength&gt;&lt;4 bytes valuelength&gt;&lt;2 bytes rowlength&gt;    *&lt;row&gt;&lt;1 byte columnfamilylength&gt;&lt;columnfamily&gt;&lt;columnqualifier&gt;    *&lt;8 bytes timestamp&gt;&lt;1 byte keytype&gt;&lt;value&gt;&lt;2 bytes tagslength&gt;    *&lt;tags&gt;</code>    */
comment|// TODO remove the boolean param once HBASE-16706 is done.
name|int
name|getSerializedSize
parameter_list|(
name|boolean
name|withTags
parameter_list|)
function_decl|;
comment|/**    * Write this Cell into the given buf's offset in a {@link KeyValue} format.    * @param buf The buffer where to write the Cell.    * @param offset The offset within buffer, to write the Cell.    */
name|void
name|write
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|)
function_decl|;
comment|/**    * Does a deep copy of the contents to a new memory area and returns it as a new cell.    * @return The deep cloned cell    */
name|Cell
name|deepClone
parameter_list|()
function_decl|;
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
block|}
end_interface

end_unit

