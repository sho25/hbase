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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InvalidObjectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * A {@link ByteString} that wraps around a {@link ByteInput}.  */
end_comment

begin_class
specifier|final
class|class
name|ByteInputByteString
extends|extends
name|ByteString
operator|.
name|LeafByteString
block|{
specifier|private
specifier|final
name|ByteInput
name|buffer
decl_stmt|;
specifier|private
specifier|final
name|int
name|offset
decl_stmt|,
name|length
decl_stmt|;
name|ByteInputByteString
parameter_list|(
name|ByteInput
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"buffer"
argument_list|)
throw|;
block|}
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
comment|// =================================================================
comment|// Serializable
comment|/**    * Magic method that lets us override serialization behavior.    */
specifier|private
name|Object
name|writeReplace
parameter_list|()
block|{
return|return
name|ByteString
operator|.
name|wrap
argument_list|(
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Magic method that lets us override deserialization behavior.    */
specifier|private
name|void
name|readObject
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|ObjectInputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|InvalidObjectException
argument_list|(
literal|"ByteInputByteString instances are not to be serialized directly"
argument_list|)
throw|;
comment|// TODO check here
block|}
comment|// =================================================================
annotation|@
name|Override
specifier|public
name|byte
name|byteAt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|buffer
operator|.
name|read
argument_list|(
name|getAbsoluteOffset
argument_list|(
name|index
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|int
name|getAbsoluteOffset
parameter_list|(
name|int
name|relativeOffset
parameter_list|)
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|relativeOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteString
name|substring
parameter_list|(
name|int
name|beginIndex
parameter_list|,
name|int
name|endIndex
parameter_list|)
block|{
if|if
condition|(
name|beginIndex
operator|<
literal|0
operator|||
name|beginIndex
operator|>=
name|size
argument_list|()
operator|||
name|endIndex
operator|<
name|beginIndex
operator|||
name|endIndex
operator|>=
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Invalid indices [%d, %d]"
argument_list|,
name|beginIndex
argument_list|,
name|endIndex
argument_list|)
argument_list|)
throw|;
block|}
return|return
operator|new
name|ByteInputByteString
argument_list|(
name|this
operator|.
name|buffer
argument_list|,
name|getAbsoluteOffset
argument_list|(
name|beginIndex
argument_list|)
argument_list|,
name|endIndex
operator|-
name|beginIndex
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|copyToInternal
parameter_list|(
name|byte
index|[]
name|target
parameter_list|,
name|int
name|sourceOffset
parameter_list|,
name|int
name|targetOffset
parameter_list|,
name|int
name|numberToCopy
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|.
name|read
argument_list|(
name|getAbsoluteOffset
argument_list|(
name|sourceOffset
argument_list|)
argument_list|,
name|target
argument_list|,
name|targetOffset
argument_list|,
name|numberToCopy
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyTo
parameter_list|(
name|ByteBuffer
name|target
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|.
name|read
argument_list|(
name|this
operator|.
name|offset
argument_list|,
name|target
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO
block|}
annotation|@
name|Override
name|boolean
name|equalsRange
parameter_list|(
name|ByteString
name|other
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|substring
argument_list|(
literal|0
argument_list|,
name|length
argument_list|)
operator|.
name|equals
argument_list|(
name|other
operator|.
name|substring
argument_list|(
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
name|void
name|writeToInternal
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|int
name|sourceOffset
parameter_list|,
name|int
name|numberToWrite
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|buf
init|=
name|ByteBufferWriter
operator|.
name|getOrCreateBuffer
argument_list|(
name|numberToWrite
argument_list|)
decl_stmt|;
name|this
operator|.
name|buffer
operator|.
name|read
argument_list|(
name|getAbsoluteOffset
argument_list|(
name|sourceOffset
argument_list|)
argument_list|,
name|buf
argument_list|,
literal|0
argument_list|,
name|numberToWrite
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|numberToWrite
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|writeTo
parameter_list|(
name|ByteOutput
name|output
parameter_list|)
throws|throws
name|IOException
block|{
name|output
operator|.
name|writeLazy
argument_list|(
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|asReadOnlyByteBuffer
parameter_list|()
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|asReadOnlyBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|asReadOnlyByteBufferList
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|toStringInternal
parameter_list|(
name|Charset
name|charset
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|toByteArray
argument_list|()
decl_stmt|;
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
name|charset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isValidUtf8
parameter_list|()
block|{
return|return
name|Utf8
operator|.
name|isValidUtf8
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|partialIsValidUtf8
parameter_list|(
name|int
name|state
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|int
name|off
init|=
name|getAbsoluteOffset
argument_list|(
name|offset
argument_list|)
decl_stmt|;
return|return
name|Utf8
operator|.
name|partialIsValidUtf8
argument_list|(
name|state
argument_list|,
name|buffer
argument_list|,
name|off
argument_list|,
name|off
operator|+
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|ByteString
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ByteString
name|otherString
init|=
operator|(
operator|(
name|ByteString
operator|)
name|other
operator|)
decl_stmt|;
if|if
condition|(
name|size
argument_list|()
operator|!=
name|otherString
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|other
operator|instanceof
name|RopeByteString
condition|)
block|{
return|return
name|other
operator|.
name|equals
argument_list|(
name|this
argument_list|)
return|;
block|}
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|otherString
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|partialHash
parameter_list|(
name|int
name|h
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|offset
operator|=
name|getAbsoluteOffset
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|int
name|end
init|=
name|offset
operator|+
name|length
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|offset
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|h
operator|=
name|h
operator|*
literal|31
operator|+
name|buffer
operator|.
name|read
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|h
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputStream
name|newInput
parameter_list|()
block|{
return|return
operator|new
name|InputStream
argument_list|()
block|{
specifier|private
specifier|final
name|ByteInput
name|buf
init|=
name|buffer
decl_stmt|;
specifier|private
name|int
name|pos
init|=
name|offset
decl_stmt|;
specifier|private
name|int
name|limit
init|=
name|pos
operator|+
name|length
decl_stmt|;
specifier|private
name|int
name|mark
init|=
name|pos
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|mark
parameter_list|(
name|int
name|readlimit
parameter_list|)
block|{
name|this
operator|.
name|mark
operator|=
name|readlimit
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|markSupported
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|pos
operator|=
name|this
operator|.
name|mark
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|limit
operator|-
name|this
operator|.
name|pos
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|available
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|this
operator|.
name|buf
operator|.
name|read
argument_list|(
name|pos
operator|++
argument_list|)
operator|&
literal|0xFF
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|remain
init|=
name|available
argument_list|()
decl_stmt|;
if|if
condition|(
name|remain
operator|<=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|len
operator|=
name|Math
operator|.
name|min
argument_list|(
name|len
argument_list|,
name|remain
argument_list|)
expr_stmt|;
name|buf
operator|.
name|read
argument_list|(
name|pos
argument_list|,
name|bytes
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|len
expr_stmt|;
return|return
name|len
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|CodedInputStream
name|newCodedInput
parameter_list|()
block|{
comment|// We trust CodedInputStream not to modify the bytes, or to give anyone
comment|// else access to them.
return|return
name|CodedInputStream
operator|.
name|newInstance
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
end_class

end_unit

