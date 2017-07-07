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

begin_comment
comment|/**  * {@code SingleFieldBuilder} implements a structure that a protocol  * message uses to hold a single field of another protocol message. It supports  * the classical use case of setting an immutable {@link Message} as the value  * of the field and is highly optimized around this.  *<br>  * It also supports the additional use case of setting a {@link Message.Builder}  * as the field and deferring conversion of that {@code Builder}  * to an immutable {@code Message}. In this way, it's possible to maintain  * a tree of {@code Builder}'s that acts as a fully read/write data  * structure.  *<br>  * Logically, one can think of a tree of builders as converting the entire tree  * to messages when build is called on the root or when any method is called  * that desires a Message instead of a Builder. In terms of the implementation,  * the {@code SingleFieldBuilder} and {@code RepeatedFieldBuilder}  * classes cache messages that were created so that messages only need to be  * created when some change occurred in its builder or a builder for one of its  * descendants.  *  * @param<MType> the type of message for the field  * @param<BType> the type of builder for the field  * @param<IType> the common interface for the message and the builder  *  * @author jonp@google.com (Jon Perlow)  */
end_comment

begin_class
specifier|public
class|class
name|SingleFieldBuilder
parameter_list|<
name|MType
extends|extends
name|GeneratedMessage
parameter_list|,
name|BType
extends|extends
name|GeneratedMessage
operator|.
name|Builder
parameter_list|,
name|IType
extends|extends
name|MessageOrBuilder
parameter_list|>
implements|implements
name|GeneratedMessage
operator|.
name|BuilderParent
block|{
comment|// Parent to send changes to.
specifier|private
name|GeneratedMessage
operator|.
name|BuilderParent
name|parent
decl_stmt|;
comment|// Invariant: one of builder or message fields must be non-null.
comment|// If set, this is the case where we are backed by a builder. In this case,
comment|// message field represents a cached message for the builder (or null if
comment|// there is no cached message).
specifier|private
name|BType
name|builder
decl_stmt|;
comment|// If builder is non-null, this represents a cached message from the builder.
comment|// If builder is null, this is the authoritative message for the field.
specifier|private
name|MType
name|message
decl_stmt|;
comment|// Indicates that we've built a message and so we are now obligated
comment|// to dispatch dirty invalidations. See GeneratedMessage.BuilderListener.
specifier|private
name|boolean
name|isClean
decl_stmt|;
specifier|public
name|SingleFieldBuilder
parameter_list|(
name|MType
name|message
parameter_list|,
name|GeneratedMessage
operator|.
name|BuilderParent
name|parent
parameter_list|,
name|boolean
name|isClean
parameter_list|)
block|{
if|if
condition|(
name|message
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|isClean
operator|=
name|isClean
expr_stmt|;
block|}
specifier|public
name|void
name|dispose
parameter_list|()
block|{
comment|// Null out parent so we stop sending it invalidations.
name|parent
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Get the message for the field. If the message is currently stored    * as a {@code Builder}, it is converted to a {@code Message} by    * calling {@link Message.Builder#buildPartial} on it. If no message has    * been set, returns the default instance of the message.    *    * @return the message for the field    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|MType
name|getMessage
parameter_list|()
block|{
if|if
condition|(
name|message
operator|==
literal|null
condition|)
block|{
comment|// If message is null, the invariant is that we must be have a builder.
name|message
operator|=
operator|(
name|MType
operator|)
name|builder
operator|.
name|buildPartial
argument_list|()
expr_stmt|;
block|}
return|return
name|message
return|;
block|}
comment|/**    * Builds the message and returns it.    *    * @return the message    */
specifier|public
name|MType
name|build
parameter_list|()
block|{
comment|// Now that build has been called, we are required to dispatch
comment|// invalidations.
name|isClean
operator|=
literal|true
expr_stmt|;
return|return
name|getMessage
argument_list|()
return|;
block|}
comment|/**    * Gets a builder for the field. If no builder has been created yet, a    * builder is created on demand by calling {@link Message#toBuilder}.    *    * @return The builder for the field    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|BType
name|getBuilder
parameter_list|()
block|{
if|if
condition|(
name|builder
operator|==
literal|null
condition|)
block|{
comment|// builder.mergeFrom() on a fresh builder
comment|// does not create any sub-objects with independent clean/dirty states,
comment|// therefore setting the builder itself to clean without actually calling
comment|// build() cannot break any invariants.
name|builder
operator|=
operator|(
name|BType
operator|)
name|message
operator|.
name|newBuilderForType
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|builder
operator|.
name|mergeFrom
argument_list|(
name|message
argument_list|)
expr_stmt|;
comment|// no-op if message is the default message
name|builder
operator|.
name|markClean
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
comment|/**    * Gets the base class interface for the field. This may either be a builder    * or a message. It will return whatever is more efficient.    *    * @return the message or builder for the field as the base class interface    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|IType
name|getMessageOrBuilder
parameter_list|()
block|{
if|if
condition|(
name|builder
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
name|IType
operator|)
name|builder
return|;
block|}
else|else
block|{
return|return
operator|(
name|IType
operator|)
name|message
return|;
block|}
block|}
comment|/**    * Sets a  message for the field replacing any existing value.    *    * @param message the message to set    * @return the builder    */
specifier|public
name|SingleFieldBuilder
argument_list|<
name|MType
argument_list|,
name|BType
argument_list|,
name|IType
argument_list|>
name|setMessage
parameter_list|(
name|MType
name|message
parameter_list|)
block|{
if|if
condition|(
name|message
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
if|if
condition|(
name|builder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|dispose
argument_list|()
expr_stmt|;
name|builder
operator|=
literal|null
expr_stmt|;
block|}
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Merges the field from another field.    *    * @param value the value to merge from    * @return the builder    */
specifier|public
name|SingleFieldBuilder
argument_list|<
name|MType
argument_list|,
name|BType
argument_list|,
name|IType
argument_list|>
name|mergeFrom
parameter_list|(
name|MType
name|value
parameter_list|)
block|{
if|if
condition|(
name|builder
operator|==
literal|null
operator|&&
name|message
operator|==
name|message
operator|.
name|getDefaultInstanceForType
argument_list|()
condition|)
block|{
name|message
operator|=
name|value
expr_stmt|;
block|}
else|else
block|{
name|getBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Clears the value of the field.    *    * @return the builder    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|SingleFieldBuilder
argument_list|<
name|MType
argument_list|,
name|BType
argument_list|,
name|IType
argument_list|>
name|clear
parameter_list|()
block|{
name|message
operator|=
call|(
name|MType
call|)
argument_list|(
name|message
operator|!=
literal|null
condition|?
name|message
operator|.
name|getDefaultInstanceForType
argument_list|()
else|:
name|builder
operator|.
name|getDefaultInstanceForType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|dispose
argument_list|()
expr_stmt|;
name|builder
operator|=
literal|null
expr_stmt|;
block|}
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Called when a the builder or one of its nested children has changed    * and any parent should be notified of its invalidation.    */
specifier|private
name|void
name|onChanged
parameter_list|()
block|{
comment|// If builder is null, this is the case where onChanged is being called
comment|// from setMessage or clear.
if|if
condition|(
name|builder
operator|!=
literal|null
condition|)
block|{
name|message
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|isClean
operator|&&
name|parent
operator|!=
literal|null
condition|)
block|{
name|parent
operator|.
name|markDirty
argument_list|()
expr_stmt|;
comment|// Don't keep dispatching invalidations until build is called again.
name|isClean
operator|=
literal|false
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|markDirty
parameter_list|()
block|{
name|onChanged
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

