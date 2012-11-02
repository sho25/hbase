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
name|ByteArrayInputStream
import|;
end_import

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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
comment|/**  * A default implementation of {@link HFileBlockDecodingContext}. It assumes the  * block data section is compressed as a whole.  *  * @see HFileBlockDefaultEncodingContext for the default compression context  *  */
end_comment

begin_class
specifier|public
class|class
name|HFileBlockDefaultDecodingContext
implements|implements
name|HFileBlockDecodingContext
block|{
specifier|private
specifier|final
name|Compression
operator|.
name|Algorithm
name|compressAlgo
decl_stmt|;
specifier|public
name|HFileBlockDefaultDecodingContext
parameter_list|(
name|Compression
operator|.
name|Algorithm
name|compressAlgo
parameter_list|)
block|{
name|this
operator|.
name|compressAlgo
operator|=
name|compressAlgo
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareDecoding
parameter_list|(
name|int
name|onDiskSizeWithoutHeader
parameter_list|,
name|int
name|uncompressedSizeWithoutHeader
parameter_list|,
name|ByteBuffer
name|blockBufferWithoutHeader
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
block|{
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|onDiskBlock
argument_list|,
name|offset
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|)
argument_list|)
decl_stmt|;
name|Compression
operator|.
name|decompress
argument_list|(
name|blockBufferWithoutHeader
operator|.
name|array
argument_list|()
argument_list|,
name|blockBufferWithoutHeader
operator|.
name|arrayOffset
argument_list|()
argument_list|,
operator|(
name|InputStream
operator|)
name|dis
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|,
name|uncompressedSizeWithoutHeader
argument_list|,
name|compressAlgo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Algorithm
name|getCompression
parameter_list|()
block|{
return|return
name|compressAlgo
return|;
block|}
block|}
end_class

end_unit

