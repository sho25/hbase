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
comment|/**  * A decoding context that is created by a reader's encoder, and is shared  * across all of the reader's read operations.  *  * @see HFileBlockEncodingContext for encoding  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HFileBlockDecodingContext
block|{
comment|/**    * Perform all actions that need to be done before the encoder's real decoding    * process. Decompression needs to be done if    * {@link HFileContext#getCompression()} returns a valid compression    * algorithm.    *    * @param onDiskSizeWithoutHeader    *          numBytes after block and encoding headers    * @param uncompressedSizeWithoutHeader    *          numBytes without header required to store the block after    *          decompressing (not decoding)    * @param blockBufferWithoutHeader    *          ByteBuffer pointed after the header but before the data    * @param onDiskBlock    *          on disk data to be decoded    */
name|void
name|prepareDecoding
parameter_list|(
name|int
name|onDiskSizeWithoutHeader
parameter_list|,
name|int
name|uncompressedSizeWithoutHeader
parameter_list|,
name|ByteBuff
name|blockBufferWithoutHeader
parameter_list|,
name|ByteBuff
name|onDiskBlock
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return HFile meta information    */
name|HFileContext
name|getHFileContext
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

