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
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|max
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|min
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|SoftReference
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
comment|/**  * Utility class to provide efficient writing of {@link ByteBuffer}s to {@link OutputStream}s.  */
end_comment

begin_class
specifier|final
class|class
name|ByteBufferWriter
block|{
specifier|private
name|ByteBufferWriter
parameter_list|()
block|{}
comment|/**    * Minimum size for a cached buffer. This prevents us from allocating buffers that are too    * small to be easily reused.    */
comment|// TODO(nathanmittler): tune this property or allow configuration?
specifier|private
specifier|static
specifier|final
name|int
name|MIN_CACHED_BUFFER_SIZE
init|=
literal|1024
decl_stmt|;
comment|/**    * Maximum size for a cached buffer. If a larger buffer is required, it will be allocated    * but not cached.    */
comment|// TODO(nathanmittler): tune this property or allow configuration?
specifier|private
specifier|static
specifier|final
name|int
name|MAX_CACHED_BUFFER_SIZE
init|=
literal|16
operator|*
literal|1024
decl_stmt|;
comment|/**    * The fraction of the requested buffer size under which the buffer will be reallocated.    */
comment|// TODO(nathanmittler): tune this property or allow configuration?
specifier|private
specifier|static
specifier|final
name|float
name|BUFFER_REALLOCATION_THRESHOLD
init|=
literal|0.5f
decl_stmt|;
comment|/**    * Keeping a soft reference to a thread-local buffer. This buffer is used for writing a    * {@link ByteBuffer} to an {@link OutputStream} when no zero-copy alternative was available.    * Using a "soft" reference since VMs may keep this reference around longer than "weak"    * (e.g. HotSpot will maintain soft references until memory pressure warrants collection).    */
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|SoftReference
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|BUFFER
init|=
operator|new
name|ThreadLocal
argument_list|<
name|SoftReference
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * For testing purposes only. Clears the cached buffer to force a new allocation on the next    * invocation.    */
specifier|static
name|void
name|clearCachedBuffer
parameter_list|()
block|{
name|BUFFER
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes the remaining content of the buffer to the given stream. The buffer {@code position}    * will remain unchanged by this method.    */
specifier|static
name|void
name|write
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|OutputStream
name|output
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|initialPos
init|=
name|buffer
operator|.
name|position
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|buffer
operator|.
name|hasArray
argument_list|()
condition|)
block|{
comment|// Optimized write for array-backed buffers.
comment|// Note that we're taking the risk that a malicious OutputStream could modify the array.
name|output
operator|.
name|write
argument_list|(
name|buffer
operator|.
name|array
argument_list|()
argument_list|,
name|buffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|buffer
operator|.
name|position
argument_list|()
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|output
operator|instanceof
name|FileOutputStream
condition|)
block|{
comment|// Use a channel to write out the ByteBuffer. This will automatically empty the buffer.
operator|(
operator|(
name|FileOutputStream
operator|)
name|output
operator|)
operator|.
name|getChannel
argument_list|()
operator|.
name|write
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Read all of the data from the buffer to an array.
comment|// TODO(nathanmittler): Consider performance improvements for other "known" stream types.
specifier|final
name|byte
index|[]
name|array
init|=
name|getOrCreateBuffer
argument_list|(
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|buffer
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
name|int
name|length
init|=
name|min
argument_list|(
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|,
name|array
operator|.
name|length
argument_list|)
decl_stmt|;
name|buffer
operator|.
name|get
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
comment|// Restore the initial position.
name|buffer
operator|.
name|position
argument_list|(
name|initialPos
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|byte
index|[]
name|getOrCreateBuffer
parameter_list|(
name|int
name|requestedSize
parameter_list|)
block|{
name|requestedSize
operator|=
name|max
argument_list|(
name|requestedSize
argument_list|,
name|MIN_CACHED_BUFFER_SIZE
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
name|getBuffer
argument_list|()
decl_stmt|;
comment|// Only allocate if we need to.
if|if
condition|(
name|buffer
operator|==
literal|null
operator|||
name|needToReallocate
argument_list|(
name|requestedSize
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
condition|)
block|{
name|buffer
operator|=
operator|new
name|byte
index|[
name|requestedSize
index|]
expr_stmt|;
comment|// Only cache the buffer if it's not too big.
if|if
condition|(
name|requestedSize
operator|<=
name|MAX_CACHED_BUFFER_SIZE
condition|)
block|{
name|setBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|buffer
return|;
block|}
specifier|private
specifier|static
name|boolean
name|needToReallocate
parameter_list|(
name|int
name|requestedSize
parameter_list|,
name|int
name|bufferLength
parameter_list|)
block|{
comment|// First check against just the requested length to avoid the multiply.
return|return
name|bufferLength
operator|<
name|requestedSize
operator|&&
name|bufferLength
operator|<
name|requestedSize
operator|*
name|BUFFER_REALLOCATION_THRESHOLD
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getBuffer
parameter_list|()
block|{
name|SoftReference
argument_list|<
name|byte
index|[]
argument_list|>
name|sr
init|=
name|BUFFER
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|sr
operator|==
literal|null
condition|?
literal|null
else|:
name|sr
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|setBuffer
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|BUFFER
operator|.
name|set
argument_list|(
operator|new
name|SoftReference
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

