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
name|hfile
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
name|encoding
operator|.
name|HFileBlockDecodingContext
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
name|HFileBlockEncodingContext
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

begin_comment
comment|/**  * Controls what kind of data block encoding is used. If data block encoding is  * not set or the given block is not a data block (encoded or not), methods  * should just return the unmodified block.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HFileDataBlockEncoder
block|{
comment|/** Type of encoding used for data blocks in HFile. Stored in file info. */
name|byte
index|[]
name|DATA_BLOCK_ENCODING
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"DATA_BLOCK_ENCODING"
argument_list|)
decl_stmt|;
comment|/**    * Should be called before an encoded or unencoded data block is written to    * disk.    * @param in KeyValues next to each other    * @param encodingResult the encoded result    * @param blockType block type    * @throws IOException    */
name|void
name|beforeWriteToDisk
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|HFileBlockEncodingContext
name|encodingResult
parameter_list|,
name|BlockType
name|blockType
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Decides whether we should use a scanner over encoded blocks.    * @return Whether to use encoded scanner.    */
name|boolean
name|useEncodedScanner
parameter_list|()
function_decl|;
comment|/**    * Save metadata in HFile which will be written to disk    * @param writer writer for a given HFile    * @exception IOException on disk problems    */
name|void
name|saveMetadata
parameter_list|(
name|HFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** @return the data block encoding */
name|DataBlockEncoding
name|getDataBlockEncoding
parameter_list|()
function_decl|;
comment|/**    * Create an encoder specific encoding context object for writing. And the    * encoding context should also perform compression if compressionAlgorithm is    * valid.    *    * @param headerBytes header bytes    * @param fileContext HFile meta data    * @return a new {@link HFileBlockEncodingContext} object    */
name|HFileBlockEncodingContext
name|newDataBlockEncodingContext
parameter_list|(
name|byte
index|[]
name|headerBytes
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|)
function_decl|;
comment|/**    * create a encoder specific decoding context for reading. And the    * decoding context should also do decompression if compressionAlgorithm    * is valid.    *    * @param fileContext - HFile meta data    * @return a new {@link HFileBlockDecodingContext} object    */
name|HFileBlockDecodingContext
name|newDataBlockDecodingContext
parameter_list|(
name|HFileContext
name|fileContext
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

