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
name|HFileBlock
import|;
end_import

begin_comment
comment|/**  * A decoding context that is created by a reader's encoder, and is shared  * across the reader's all read operations.  *  * @see HFileBlockEncodingContext for encoding  */
end_comment

begin_interface
specifier|public
interface|interface
name|HFileBlockDecodingContext
block|{
comment|/**    * @return the compression algorithm used by this decoding context    */
specifier|public
name|Compression
operator|.
name|Algorithm
name|getCompression
parameter_list|()
function_decl|;
comment|/**    * Perform all actions that need to be done before the encoder's real    * decoding process. Decompression needs to be done if    * {@link #getCompression()} returns a valid compression algorithm.    *    * @param block HFile block object    * @param onDiskBlock on disk bytes to be decoded    * @param offset data start offset in onDiskBlock    * @throws IOException    */
specifier|public
name|void
name|prepareDecoding
parameter_list|(
name|HFileBlock
name|block
parameter_list|,
name|byte
index|[]
name|onDiskBlock
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

