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
name|IOException
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

begin_comment
comment|/**  * A way to write "inline" blocks into an {@link HFile}. Inline blocks are  * interspersed with data blocks. For example, Bloom filter chunks and  * leaf-level blocks of a multi-level block index are stored as inline blocks.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|InlineBlockWriter
block|{
comment|/**    * Determines whether there is a new block to be written out.    *    * @param closing    *          whether the file is being closed, in which case we need to write    *          out all available data and not wait to accumulate another block    */
name|boolean
name|shouldWriteBlock
parameter_list|(
name|boolean
name|closing
parameter_list|)
function_decl|;
comment|/**    * Writes the block to the provided stream. Must not write any magic records.    * Called only if {@link #shouldWriteBlock(boolean)} returned true.    *    * @param out    *          a stream (usually a compressing stream) to write the block to    */
name|void
name|writeInlineBlock
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after a block has been written, and its offset, raw size, and    * compressed size have been determined. Can be used to add an entry to a    * block index. If this type of inline blocks needs a block index, the inline    * block writer is responsible for maintaining it.    *    * @param offset the offset of the block in the stream    * @param onDiskSize the on-disk size of the block    * @param uncompressedSize the uncompressed size of the block    */
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
function_decl|;
comment|/**    * The type of blocks this block writer produces.    */
name|BlockType
name|getInlineBlockType
parameter_list|()
function_decl|;
comment|/**    * @return true if inline blocks produced by this writer should be cached    */
name|boolean
name|cacheOnWrite
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

