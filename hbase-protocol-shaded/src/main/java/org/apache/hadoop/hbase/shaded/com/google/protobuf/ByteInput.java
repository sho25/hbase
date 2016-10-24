begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Protocol Buffers - Google's data interchange format
end_comment

begin_comment
comment|// Copyright 2008 Google Inc.  All rights reserved.
end_comment

begin_comment
comment|// https://developers.google.com/protocol-buffers/
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// Redistribution and use in source and binary forms, with or without
end_comment

begin_comment
comment|// modification, are permitted provided that the following conditions are
end_comment

begin_comment
comment|// met:
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|//     * Redistributions of source code must retain the above copyright
end_comment

begin_comment
comment|// notice, this list of conditions and the following disclaimer.
end_comment

begin_comment
comment|//     * Redistributions in binary form must reproduce the above
end_comment

begin_comment
comment|// copyright notice, this list of conditions and the following disclaimer
end_comment

begin_comment
comment|// in the documentation and/or other materials provided with the
end_comment

begin_comment
comment|// distribution.
end_comment

begin_comment
comment|//     * Neither the name of Google Inc. nor the names of its
end_comment

begin_comment
comment|// contributors may be used to endorse or promote products derived from
end_comment

begin_comment
comment|// this software without specific prior written permission.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
end_comment

begin_comment
comment|// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
end_comment

begin_comment
comment|// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
end_comment

begin_comment
comment|// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
end_comment

begin_comment
comment|// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
end_comment

begin_comment
comment|// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
end_comment

begin_comment
comment|// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
end_comment

begin_comment
comment|// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
end_comment

begin_comment
comment|// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
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

begin_comment
comment|/**  * An input for raw bytes. This is similar to an InputStream but it is offset addressable. All the  * read APIs are relative.  */
end_comment

begin_class
annotation|@
name|ExperimentalApi
specifier|public
specifier|abstract
class|class
name|ByteInput
block|{
comment|/**    * Reads a single byte from the given offset.    * @param offset The offset from where byte to be read    * @return The byte of data at given offset    */
specifier|public
specifier|abstract
name|byte
name|read
parameter_list|(
name|int
name|offset
parameter_list|)
function_decl|;
comment|/**    * Reads bytes of data from the given offset into an array of bytes.    * @param offset The src offset within this ByteInput from where data to be read.    * @param out Destination byte array to read data into.    * @return The number of bytes read from ByteInput    */
specifier|public
name|int
name|read
parameter_list|(
name|int
name|offset
parameter_list|,
name|byte
name|b
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|read
argument_list|(
name|offset
argument_list|,
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Reads up to<code>len</code> bytes of data from the given offset into an array of bytes.    * @param offset The src offset within this ByteInput from where data to be read.    * @param out Destination byte array to read data into.    * @param outOffset Offset within the the out byte[] where data to be read into.    * @param len The number of bytes to read.    * @return The number of bytes read from ByteInput    */
specifier|public
specifier|abstract
name|int
name|read
parameter_list|(
name|int
name|offset
parameter_list|,
name|byte
index|[]
name|out
parameter_list|,
name|int
name|outOffset
parameter_list|,
name|int
name|len
parameter_list|)
function_decl|;
comment|/**    * Reads bytes of data from the given offset into given {@link ByteBuffer}.    * @param offset he src offset within this ByteInput from where data to be read.    * @param out Destination {@link ByteBuffer} to read data into.    * @return The number of bytes read from ByteInput    */
specifier|public
specifier|abstract
name|int
name|read
parameter_list|(
name|int
name|offset
parameter_list|,
name|ByteBuffer
name|out
parameter_list|)
function_decl|;
comment|/**    * @return Total number of bytes in this ByteInput.    */
specifier|public
specifier|abstract
name|int
name|size
parameter_list|()
function_decl|;
block|}
end_class

end_unit

