begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|encoding
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|HFileContext
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
name|nio
operator|.
name|ByteBuff
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
comment|/**  * Encoding of KeyValue. It aims to be fast and efficient using assumptions:  *<ul>  *<li>the KeyValues are stored sorted by key</li>  *<li>we know the structure of KeyValue</li>  *<li>the values are always iterated forward from beginning of block</li>  *<li>knowledge of Key Value format</li>  *</ul>  * It is designed to work fast enough to be feasible as in memory compression.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|DataBlockEncoder
block|{
comment|// TODO: This Interface should be deprecated and replaced. It presumes hfile and carnal knowledge of
comment|// Cell internals. It was done for a different time. Remove. Purge.
comment|/**    * Starts encoding for a block of KeyValues. Call    * {@link #endBlockEncoding(HFileBlockEncodingContext, DataOutputStream, byte[])} to finish    * encoding of a block.    */
name|void
name|startBlockEncoding
parameter_list|(
name|HFileBlockEncodingContext
name|encodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Encodes a KeyValue.    * @return unencoded kv size written    */
name|int
name|encode
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|HFileBlockEncodingContext
name|encodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Ends encoding for a block of KeyValues. Gives a chance for the encoder to do the finishing    * stuff for the encoded block. It must be called at the end of block encoding.    */
name|void
name|endBlockEncoding
parameter_list|(
name|HFileBlockEncodingContext
name|encodingCtx
parameter_list|,
name|DataOutputStream
name|out
parameter_list|,
name|byte
index|[]
name|uncompressedBytesWithHeader
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Decode.    * @param source Compressed stream of KeyValues.    * @return Uncompressed block of KeyValues.    * @throws IOException If there is an error in source.    */
name|ByteBuffer
name|decodeKeyValues
parameter_list|(
name|DataInputStream
name|source
parameter_list|,
name|HFileBlockDecodingContext
name|decodingCtx
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Return first key in block as a cell. Useful for indexing. Typically does not make    * a deep copy but returns a buffer wrapping a segment of the actual block's    * byte array. This is because the first key in block is usually stored    * unencoded.    * @param block encoded block we want index, the position will not change    * @return First key in block as a cell.    */
name|Cell
name|getFirstKeyCellInBlock
parameter_list|(
name|ByteBuff
name|block
parameter_list|)
function_decl|;
comment|/**    * Create a HFileBlock seeker which find KeyValues within a block.    * @return A newly created seeker.    */
name|EncodedSeeker
name|createSeeker
parameter_list|(
name|HFileBlockDecodingContext
name|decodingCtx
parameter_list|)
function_decl|;
comment|/**    * Creates a encoder specific encoding context    *    * @param encoding    *          encoding strategy used    * @param headerBytes    *          header bytes to be written, put a dummy header here if the header    *          is unknown    * @param meta    *          HFile meta data    * @return a newly created encoding context    */
name|HFileBlockEncodingContext
name|newDataBlockEncodingContext
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|,
name|byte
index|[]
name|headerBytes
parameter_list|,
name|HFileContext
name|meta
parameter_list|)
function_decl|;
comment|/**    * Creates an encoder specific decoding context, which will prepare the data    * before actual decoding    *    * @param meta    *          HFile meta data            * @return a newly created decoding context    */
name|HFileBlockDecodingContext
name|newDataBlockDecodingContext
parameter_list|(
name|HFileContext
name|meta
parameter_list|)
function_decl|;
comment|/**    * An interface which enable to seek while underlying data is encoded.    *    * It works on one HFileBlock, but it is reusable. See    * {@link #setCurrentBuffer(ByteBuff)}.    */
interface|interface
name|EncodedSeeker
block|{
comment|/**      * Set on which buffer there will be done seeking.      * @param buffer Used for seeking.      */
name|void
name|setCurrentBuffer
parameter_list|(
name|ByteBuff
name|buffer
parameter_list|)
function_decl|;
comment|/**      * From the current position creates a cell using the key part      * of the current buffer      * @return key at current position      */
name|Cell
name|getKey
parameter_list|()
function_decl|;
comment|/**      * Does a shallow copy of the value at the current position. A shallow      * copy is possible because the returned buffer refers to the backing array      * of the original encoded buffer.      * @return value at current position      */
name|ByteBuffer
name|getValueShallowCopy
parameter_list|()
function_decl|;
comment|/**      * @return the Cell at the current position. Includes memstore timestamp.      */
name|Cell
name|getCell
parameter_list|()
function_decl|;
comment|/** Set position to beginning of given block */
name|void
name|rewind
parameter_list|()
function_decl|;
comment|/**      * Move to next position      * @return true on success, false if there is no more positions.      */
name|boolean
name|next
parameter_list|()
function_decl|;
comment|/**      * Moves the seeker position within the current block to:      *<ul>      *<li>the last key that that is less than or equal to the given key if      *<code>seekBefore</code> is false</li>      *<li>the last key that is strictly less than the given key if<code>      * seekBefore</code> is true. The caller is responsible for loading the      * previous block if the requested key turns out to be the first key of the      * current block.</li>      *</ul>      * @param key - Cell to which the seek should happen      * @param seekBefore find the key strictly less than the given key in case      *          of an exact match. Does not matter in case of an inexact match.      * @return 0 on exact match, 1 on inexact match.      */
name|int
name|seekToKeyInBlock
parameter_list|(
name|Cell
name|key
parameter_list|,
name|boolean
name|seekBefore
parameter_list|)
function_decl|;
comment|/**      * Compare the given key against the current key      * @return -1 is the passed key is smaller than the current key, 0 if equal and 1 if greater      */
specifier|public
name|int
name|compareKey
parameter_list|(
name|CellComparator
name|comparator
parameter_list|,
name|Cell
name|key
parameter_list|)
function_decl|;
block|}
block|}
end_interface

end_unit

