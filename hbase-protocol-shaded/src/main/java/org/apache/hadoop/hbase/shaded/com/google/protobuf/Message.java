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

begin_comment
comment|// TODO(kenton):  Use generics?  E.g. Builder<BuilderType extends Builder>, then
end_comment

begin_comment
comment|//   mergeFrom*() could return BuilderType for better type-safety.
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
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Abstract interface implemented by Protocol Message objects.  *<p>  * See also {@link MessageLite}, which defines most of the methods that typical  * users care about.  {@link Message} adds to it methods that are not available  * in the "lite" runtime.  The biggest added features are introspection and  * reflection -- i.e., getting descriptors for the message type and accessing  * the field values dynamically.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_interface
specifier|public
interface|interface
name|Message
extends|extends
name|MessageLite
extends|,
name|MessageOrBuilder
block|{
comment|// (From MessageLite, re-declared here only for return type covariance.)
annotation|@
name|Override
name|Parser
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
name|getParserForType
parameter_list|()
function_decl|;
comment|// -----------------------------------------------------------------
comment|// Comparison and hashing
comment|/**    * Compares the specified object with this message for equality.  Returns    * {@code true} if the given object is a message of the same type (as    * defined by {@code getDescriptorForType()}) and has identical values for    * all of its fields.  Subclasses must implement this; inheriting    * {@code Object.equals()} is incorrect.    *    * @param other object to be compared for equality with this message    * @return {@code true} if the specified object is equal to this message    */
annotation|@
name|Override
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
function_decl|;
comment|/**    * Returns the hash code value for this message.  The hash code of a message    * should mix the message's type (object identity of the descriptor) with its    * contents (known and unknown field values).  Subclasses must implement this;    * inheriting {@code Object.hashCode()} is incorrect.    *    * @return the hash code value for this message    * @see Map#hashCode()    */
annotation|@
name|Override
name|int
name|hashCode
parameter_list|()
function_decl|;
comment|// -----------------------------------------------------------------
comment|// Convenience methods.
comment|/**    * Converts the message to a string in protocol buffer text format. This is    * just a trivial wrapper around {@link    * TextFormat#printToString(MessageOrBuilder)}.    */
annotation|@
name|Override
name|String
name|toString
parameter_list|()
function_decl|;
comment|// =================================================================
comment|// Builders
comment|// (From MessageLite, re-declared here only for return type covariance.)
annotation|@
name|Override
name|Builder
name|newBuilderForType
parameter_list|()
function_decl|;
annotation|@
name|Override
name|Builder
name|toBuilder
parameter_list|()
function_decl|;
comment|/**    * Abstract interface implemented by Protocol Message builders.    */
interface|interface
name|Builder
extends|extends
name|MessageLite
operator|.
name|Builder
extends|,
name|MessageOrBuilder
block|{
comment|// (From MessageLite.Builder, re-declared here only for return type
comment|// covariance.)
annotation|@
name|Override
name|Builder
name|clear
parameter_list|()
function_decl|;
comment|/**      * Merge {@code other} into the message being built.  {@code other} must      * have the exact same type as {@code this} (i.e.      * {@code getDescriptorForType() == other.getDescriptorForType()}).      *      * Merging occurs as follows.  For each field:<br>      * * For singular primitive fields, if the field is set in {@code other},      *   then {@code other}'s value overwrites the value in this message.<br>      * * For singular message fields, if the field is set in {@code other},      *   it is merged into the corresponding sub-message of this message      *   using the same merging rules.<br>      * * For repeated fields, the elements in {@code other} are concatenated      *   with the elements in this message.      * * For oneof groups, if the other message has one of the fields set,      *   the group of this message is cleared and replaced by the field      *   of the other message, so that the oneof constraint is preserved.      *      * This is equivalent to the {@code Message::MergeFrom} method in C++.      */
name|Builder
name|mergeFrom
parameter_list|(
name|Message
name|other
parameter_list|)
function_decl|;
comment|// (From MessageLite.Builder, re-declared here only for return type
comment|// covariance.)
annotation|@
name|Override
name|Message
name|build
parameter_list|()
function_decl|;
annotation|@
name|Override
name|Message
name|buildPartial
parameter_list|()
function_decl|;
annotation|@
name|Override
name|Builder
name|clone
parameter_list|()
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Get the message's type's descriptor.      * See {@link Message#getDescriptorForType()}.      */
annotation|@
name|Override
name|Descriptors
operator|.
name|Descriptor
name|getDescriptorForType
parameter_list|()
function_decl|;
comment|/**      * Create a Builder for messages of the appropriate type for the given      * field.  Messages built with this can then be passed to setField(),      * setRepeatedField(), or addRepeatedField().      */
name|Builder
name|newBuilderForField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|)
function_decl|;
comment|/**      * Get a nested builder instance for the given field.      *<p>      * Normally, we hold a reference to the immutable message object for the      * message type field. Some implementations(the generated message builders),      * however, can also hold a reference to the builder object (a nested      * builder) for the field.      *<p>      * If the field is already backed up by a nested builder, the nested builder      * will be returned. Otherwise, a new field builder will be created and      * returned. The original message field (if exist) will be merged into the      * field builder, which will then be nested into its parent builder.      *<p>      * NOTE: implementations that do not support nested builders will throw      *<code>UnsupportedOperationException</code>.      */
name|Builder
name|getFieldBuilder
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|)
function_decl|;
comment|/**      * Get a nested builder instance for the given repeated field instance.      *<p>      * Normally, we hold a reference to the immutable message object for the      * message type field. Some implementations(the generated message builders),      * however, can also hold a reference to the builder object (a nested      * builder) for the field.      *<p>      * If the field is already backed up by a nested builder, the nested builder      * will be returned. Otherwise, a new field builder will be created and      * returned. The original message field (if exist) will be merged into the      * field builder, which will then be nested into its parent builder.      *<p>      * NOTE: implementations that do not support nested builders will throw      *<code>UnsupportedOperationException</code>.      */
name|Builder
name|getRepeatedFieldBuilder
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|,
name|int
name|index
parameter_list|)
function_decl|;
comment|/**      * Sets a field to the given value.  The value must be of the correct type      * for this field, i.e. the same type that      * {@link Message#getField(Descriptors.FieldDescriptor)} would return.      */
name|Builder
name|setField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/**      * Clears the field.  This is exactly equivalent to calling the generated      * "clear" accessor method corresponding to the field.      */
name|Builder
name|clearField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|)
function_decl|;
comment|/**      * Clears the oneof.  This is exactly equivalent to calling the generated      * "clear" accessor method corresponding to the oneof.      */
name|Builder
name|clearOneof
parameter_list|(
name|Descriptors
operator|.
name|OneofDescriptor
name|oneof
parameter_list|)
function_decl|;
comment|/**      * Sets an element of a repeated field to the given value.  The value must      * be of the correct type for this field, i.e. the same type that      * {@link Message#getRepeatedField(Descriptors.FieldDescriptor,int)} would      * return.      * @throws IllegalArgumentException The field is not a repeated field, or      *           {@code field.getContainingType() != getDescriptorForType()}.      */
name|Builder
name|setRepeatedField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|,
name|int
name|index
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/**      * Like {@code setRepeatedField}, but appends the value as a new element.      * @throws IllegalArgumentException The field is not a repeated field, or      *           {@code field.getContainingType() != getDescriptorForType()}.      */
name|Builder
name|addRepeatedField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/** Set the {@link UnknownFieldSet} for this message. */
name|Builder
name|setUnknownFields
parameter_list|(
name|UnknownFieldSet
name|unknownFields
parameter_list|)
function_decl|;
comment|/**      * Merge some unknown fields into the {@link UnknownFieldSet} for this      * message.      */
name|Builder
name|mergeUnknownFields
parameter_list|(
name|UnknownFieldSet
name|unknownFields
parameter_list|)
function_decl|;
comment|// ---------------------------------------------------------------
comment|// Convenience methods.
comment|// (From MessageLite.Builder, re-declared here only for return type
comment|// covariance.)
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|ByteString
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|ByteString
name|data
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
name|Builder
name|mergeFrom
parameter_list|(
name|InputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
name|boolean
name|mergeDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
name|boolean
name|mergeDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_interface

end_unit

