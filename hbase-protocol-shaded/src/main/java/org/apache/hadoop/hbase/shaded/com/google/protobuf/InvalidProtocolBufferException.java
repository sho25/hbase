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

begin_comment
comment|/**  * Thrown when a protocol message being parsed is invalid in some way,  * e.g. it contains a malformed varint or a negative byte length.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_class
specifier|public
class|class
name|InvalidProtocolBufferException
extends|extends
name|IOException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|1616151763072450476L
decl_stmt|;
specifier|private
name|MessageLite
name|unfinishedMessage
init|=
literal|null
decl_stmt|;
specifier|public
name|InvalidProtocolBufferException
parameter_list|(
specifier|final
name|String
name|description
parameter_list|)
block|{
name|super
argument_list|(
name|description
argument_list|)
expr_stmt|;
block|}
specifier|public
name|InvalidProtocolBufferException
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|super
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Attaches an unfinished message to the exception to support best-effort    * parsing in {@code Parser} interface.    *    * @return this    */
specifier|public
name|InvalidProtocolBufferException
name|setUnfinishedMessage
parameter_list|(
name|MessageLite
name|unfinishedMessage
parameter_list|)
block|{
name|this
operator|.
name|unfinishedMessage
operator|=
name|unfinishedMessage
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Returns the unfinished message attached to the exception, or null if    * no message is attached.    */
specifier|public
name|MessageLite
name|getUnfinishedMessage
parameter_list|()
block|{
return|return
name|unfinishedMessage
return|;
block|}
comment|/**    * Unwraps the underlying {@link IOException} if this exception was caused by an I/O    * problem. Otherwise, returns {@code this}.    */
specifier|public
name|IOException
name|unwrapIOException
parameter_list|()
block|{
return|return
name|getCause
argument_list|()
operator|instanceof
name|IOException
condition|?
operator|(
name|IOException
operator|)
name|getCause
argument_list|()
else|:
name|this
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|truncatedMessage
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"While parsing a protocol message, the input ended unexpectedly "
operator|+
literal|"in the middle of a field.  This could mean either that the "
operator|+
literal|"input has been truncated or that an embedded message "
operator|+
literal|"misreported its own length."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|negativeSize
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"CodedInputStream encountered an embedded string or message "
operator|+
literal|"which claimed to have negative size."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|malformedVarint
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"CodedInputStream encountered a malformed varint."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|invalidTag
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"Protocol message contained an invalid tag (zero)."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|invalidEndTag
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"Protocol message end-group tag did not match expected tag."
argument_list|)
return|;
block|}
specifier|static
name|InvalidWireTypeException
name|invalidWireType
parameter_list|()
block|{
return|return
operator|new
name|InvalidWireTypeException
argument_list|(
literal|"Protocol message tag had invalid wire type."
argument_list|)
return|;
block|}
comment|/**    * Exception indicating that and unexpected wire type was encountered for a field.    */
annotation|@
name|ExperimentalApi
specifier|public
specifier|static
class|class
name|InvalidWireTypeException
extends|extends
name|InvalidProtocolBufferException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|3283890091615336259L
decl_stmt|;
specifier|public
name|InvalidWireTypeException
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|super
argument_list|(
name|description
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|InvalidProtocolBufferException
name|recursionLimitExceeded
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"Protocol message had too many levels of nesting.  May be malicious.  "
operator|+
literal|"Use CodedInputStream.setRecursionLimit() to increase the depth limit."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|sizeLimitExceeded
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"Protocol message was too large.  May be malicious.  "
operator|+
literal|"Use CodedInputStream.setSizeLimit() to increase the size limit."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|parseFailure
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"Failed to parse the message."
argument_list|)
return|;
block|}
specifier|static
name|InvalidProtocolBufferException
name|invalidUtf8
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
literal|"Protocol message had invalid UTF-8."
argument_list|)
return|;
block|}
block|}
end_class

end_unit

