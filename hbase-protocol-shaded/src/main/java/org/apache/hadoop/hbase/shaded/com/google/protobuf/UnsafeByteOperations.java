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
comment|/**  * Provides a number of unsafe byte operations to be used by advanced applications with high  * performance requirements. These methods are referred to as "unsafe" due to the fact that they  * potentially expose the backing buffer of a {@link ByteString} to the application.  *  *<p><strong>DISCLAIMER:</strong> The methods in this class should only be called if it is  * guaranteed that the buffer backing the {@link ByteString} will never change! Mutation of a  * {@link ByteString} can lead to unexpected and undesirable consequences in your application,  * and will likely be difficult to debug. Proceed with caution!  *  *<p>This can have a number of significant side affects that have  * spooky-action-at-a-distance-like behavior. In particular, if the bytes value changes out from  * under a Protocol Buffer:  *<ul>  *<li>serialization may throw  *<li>serialization may succeed but the wrong bytes may be written out  *<li>messages are no longer threadsafe  *<li>hashCode may be incorrect  *<ul>  *<li>can result in a permanent memory leak when used as a key in a long-lived HashMap  *<li> the semantics of many programs may be violated if this is the case  *</ul>  *</ul>  * Each of these issues will occur in parts of the code base that are entirely distinct from the  * parts of the code base modifying the buffer. In fact, both parts of the code base may be correct  * - it is the bridging with the unsafe operations that was in error!  */
end_comment

begin_class
annotation|@
name|ExperimentalApi
specifier|public
specifier|final
class|class
name|UnsafeByteOperations
block|{
specifier|private
name|UnsafeByteOperations
parameter_list|()
block|{}
comment|/**    * An unsafe operation that returns a {@link ByteString} that is backed by the provided buffer.    *    * @param buffer the buffer to be wrapped    * @return a {@link ByteString} backed by the provided buffer    */
specifier|public
specifier|static
name|ByteString
name|unsafeWrap
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|)
block|{
return|return
name|ByteString
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
return|;
block|}
comment|/**    * An unsafe operation that returns a {@link ByteString} that is backed by a subregion of the    * provided buffer.    *    * @param buffer the buffer to be wrapped    * @param offset the offset of the wrapped region    * @param length the number of bytes of the wrapped region    * @return a {@link ByteString} backed by the provided buffer    */
specifier|public
specifier|static
name|ByteString
name|unsafeWrap
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|ByteString
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|/**    * An unsafe operation that returns a {@link ByteString} that is backed by the provided buffer.    *    * @param buffer the Java NIO buffer to be wrapped    * @return a {@link ByteString} backed by the provided buffer    */
specifier|public
specifier|static
name|ByteString
name|unsafeWrap
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|)
block|{
return|return
name|ByteString
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
return|;
block|}
comment|/**    * An unsafe operation that returns a {@link ByteString} that is backed by the provided buffer.    * @param buffer the ByteInput buffer to be wrapped    * @param offset the offset of the wrapped byteinput    * @param length the number of bytes of the byteinput    * @return a {@link ByteString} backed by the provided buffer    */
specifier|public
specifier|static
name|ByteString
name|unsafeWrap
parameter_list|(
name|ByteInput
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
return|return
name|ByteString
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
return|;
block|}
comment|/**    * Writes the given {@link ByteString} to the provided {@link ByteOutput}. Calling this method may    * result in multiple operations on the target {@link ByteOutput}    * (i.e. for roped {@link ByteString}s).    *    *<p>This method exposes the internal backing buffer(s) of the {@link ByteString} to the {@link    * ByteOutput} in order to avoid additional copying overhead. It would be possible for a malicious    * {@link ByteOutput} to corrupt the {@link ByteString}. Use with caution!    *    *<p> NOTE: The {@link ByteOutput}<strong>MUST NOT</strong> modify the provided buffers. Doing    * so may result in corrupted data, which would be difficult to debug.    *    * @param bytes the {@link ByteString} to be written    * @param  output  the output to receive the bytes    * @throws IOException  if an I/O error occurs    */
specifier|public
specifier|static
name|void
name|unsafeWriteTo
parameter_list|(
name|ByteString
name|bytes
parameter_list|,
name|ByteOutput
name|output
parameter_list|)
throws|throws
name|IOException
block|{
name|bytes
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

