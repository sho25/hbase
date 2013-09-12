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
name|BlockType
import|;
end_import

begin_comment
comment|/**  * An encoding context that is created by a writer's encoder, and is shared  * across the writer's whole lifetime.  *  * @see HFileBlockDecodingContext for decoding  *  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HFileBlockEncodingContext
block|{
comment|/**    * @return OutputStream to which encoded data is written    */
name|OutputStream
name|getOutputStreamForEncoder
parameter_list|()
function_decl|;
comment|/**    * @return encoded and compressed bytes with header which are ready to write    *         out to disk    */
name|byte
index|[]
name|getOnDiskBytesWithHeader
parameter_list|()
function_decl|;
comment|/**    * @return encoded but not heavily compressed bytes with header which can be    *         cached in block cache    */
name|byte
index|[]
name|getUncompressedBytesWithHeader
parameter_list|()
function_decl|;
comment|/**    * @return the block type after encoding    */
name|BlockType
name|getBlockType
parameter_list|()
function_decl|;
comment|/**    * @return the compression algorithm used by this encoding context    */
name|Compression
operator|.
name|Algorithm
name|getCompression
parameter_list|()
function_decl|;
comment|/**    * sets the dummy header bytes    */
name|void
name|setDummyHeader
parameter_list|(
name|byte
index|[]
name|headerBytes
parameter_list|)
function_decl|;
comment|/**    * @return the {@link DataBlockEncoding} encoding used    */
name|DataBlockEncoding
name|getDataBlockEncoding
parameter_list|()
function_decl|;
comment|/**    * Do any action that needs to be performed after the encoding.    * Compression is also included if {@link #getCompression()} returns non-null    * compression algorithm    *    * @param blockType    * @throws IOException    */
name|void
name|postEncoding
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Releases the resources used.    */
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

