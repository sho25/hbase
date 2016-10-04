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
comment|/**  * An output target for raw bytes. This interface provides semantics that support two types of  * writing:  *  *<p><b>Traditional write operations:</b>  * (as defined by {@link java.io.OutputStream}) where the target method is responsible for either  * copying the data or completing the write before returning from the method call.  *  *<p><b>Lazy write operations:</b> where the caller guarantees that it will never modify the  * provided buffer and it can therefore be considered immutable. The target method is free to  * maintain a reference to the buffer beyond the scope of the method call (e.g. until the write  * operation completes).  */
end_comment

begin_class
annotation|@
name|ExperimentalApi
specifier|public
specifier|abstract
class|class
name|ByteOutput
block|{
comment|/**    * Writes a single byte.    *    * @param value the byte to be written    * @throws IOException thrown if an error occurred while writing    */
specifier|public
specifier|abstract
name|void
name|write
parameter_list|(
name|byte
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Writes a sequence of bytes. The {@link ByteOutput} must copy {@code value} if it will    * not be processed prior to the return of this method call, since {@code value} may be    * reused/altered by the caller.    *    *<p>NOTE: This method<strong>MUST NOT</strong> modify the {@code value}. Doing so is a    * programming error and will lead to data corruption which will be difficult to debug.    *    * @param value the bytes to be written    * @param offset the offset of the start of the writable range    * @param length the number of bytes to write starting from {@code offset}    * @throws IOException thrown if an error occurred while writing    */
specifier|public
specifier|abstract
name|void
name|write
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Writes a sequence of bytes. The {@link ByteOutput} is free to retain a reference to the value    * beyond the scope of this method call (e.g. write later) since it is considered immutable and is    * guaranteed not to change by the caller.    *    *<p>NOTE: This method<strong>MUST NOT</strong> modify the {@code value}. Doing so is a    * programming error and will lead to data corruption which will be difficult to debug.    *    * @param value the bytes to be written    * @param offset the offset of the start of the writable range    * @param length the number of bytes to write starting from {@code offset}    * @throws IOException thrown if an error occurred while writing    */
specifier|public
specifier|abstract
name|void
name|writeLazy
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Writes a sequence of bytes. The {@link ByteOutput} must copy {@code value} if it will    * not be processed prior to the return of this method call, since {@code value} may be    * reused/altered by the caller.    *    *<p>NOTE: This method<strong>MUST NOT</strong> modify the {@code value}. Doing so is a    * programming error and will lead to data corruption which will be difficult to debug.    *    * @param value the bytes to be written. Upon returning from this call, the {@code position} of    * this buffer will be set to the {@code limit}    * @throws IOException thrown if an error occurred while writing    */
specifier|public
specifier|abstract
name|void
name|write
parameter_list|(
name|ByteBuffer
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Writes a sequence of bytes. The {@link ByteOutput} is free to retain a reference to the value    * beyond the scope of this method call (e.g. write later) since it is considered immutable and is    * guaranteed not to change by the caller.    *    *<p>NOTE: This method<strong>MUST NOT</strong> modify the {@code value}. Doing so is a    * programming error and will lead to data corruption which will be difficult to debug.    *    * @param value the bytes to be written. Upon returning from this call, the {@code position} of    * this buffer will be set to the {@code limit}    * @throws IOException thrown if an error occurred while writing    */
specifier|public
specifier|abstract
name|void
name|writeLazy
parameter_list|(
name|ByteBuffer
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

