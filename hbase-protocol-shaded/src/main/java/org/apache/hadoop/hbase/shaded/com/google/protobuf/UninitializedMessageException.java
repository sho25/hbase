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
comment|/**  * Thrown when attempting to build a protocol message that is missing required  * fields.  This is a {@code RuntimeException} because it normally represents  * a programming error:  it happens when some code which constructs a message  * fails to set all the fields.  {@code parseFrom()} methods<b>do not</b>  * throw this; they throw an {@link InvalidProtocolBufferException} if  * required fields are missing, because it is not a programming error to  * receive an incomplete message.  In other words,  * {@code UninitializedMessageException} should never be thrown by correct  * code, but {@code InvalidProtocolBufferException} might be.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_class
specifier|public
class|class
name|UninitializedMessageException
extends|extends
name|RuntimeException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|7466929953374883507L
decl_stmt|;
specifier|public
name|UninitializedMessageException
parameter_list|(
specifier|final
name|MessageLite
name|message
parameter_list|)
block|{
name|super
argument_list|(
literal|"Message was missing required fields.  (Lite runtime could not "
operator|+
literal|"determine which fields were missing)."
argument_list|)
expr_stmt|;
name|missingFields
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|UninitializedMessageException
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|missingFields
parameter_list|)
block|{
name|super
argument_list|(
name|buildDescription
argument_list|(
name|missingFields
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|missingFields
operator|=
name|missingFields
expr_stmt|;
block|}
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|missingFields
decl_stmt|;
comment|/**    * Get a list of human-readable names of required fields missing from this    * message.  Each name is a full path to a field, e.g. "foo.bar[5].baz".    * Returns null if the lite runtime was used, since it lacks the ability to    * find missing fields.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getMissingFields
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|missingFields
argument_list|)
return|;
block|}
comment|/**    * Converts this exception to an {@link InvalidProtocolBufferException}.    * When a parsed message is missing required fields, this should be thrown    * instead of {@code UninitializedMessageException}.    */
specifier|public
name|InvalidProtocolBufferException
name|asInvalidProtocolBufferException
parameter_list|()
block|{
return|return
operator|new
name|InvalidProtocolBufferException
argument_list|(
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
comment|/** Construct the description string for this exception. */
specifier|private
specifier|static
name|String
name|buildDescription
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|missingFields
parameter_list|)
block|{
specifier|final
name|StringBuilder
name|description
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Message missing required fields: "
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|field
range|:
name|missingFields
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|description
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|description
operator|.
name|append
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
return|return
name|description
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

