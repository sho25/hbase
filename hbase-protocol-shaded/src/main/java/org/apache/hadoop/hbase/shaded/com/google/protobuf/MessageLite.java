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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_comment
comment|/**  * Abstract interface implemented by Protocol Message objects.  *  *<p>This interface is implemented by all protocol message objects.  Non-lite  * messages additionally implement the Message interface, which is a subclass  * of MessageLite.  Use MessageLite instead when you only need the subset of  * features which it supports -- namely, nothing that uses descriptors or  * reflection.  You can instruct the protocol compiler to generate classes  * which implement only MessageLite, not the full Message interface, by adding  * the follow line to the .proto file:  *<pre>  *   option optimize_for = LITE_RUNTIME;  *</pre>  *  *<p>This is particularly useful on resource-constrained systems where the  * full protocol buffers runtime library is too big.  *  *<p>Note that on non-constrained systems (e.g. servers) when you need to link  * in lots of protocol definitions, a better way to reduce total code footprint  * is to use {@code optimize_for = CODE_SIZE}.  This will make the generated  * code smaller while still supporting all the same features (at the expense of  * speed).  {@code optimize_for = LITE_RUNTIME} is best when you only have a  * small number of message types linked into your binary, in which case the  * size of the protocol buffers runtime itself is the biggest problem.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_interface
specifier|public
interface|interface
name|MessageLite
extends|extends
name|MessageLiteOrBuilder
block|{
comment|/**    * Serializes the message and writes it to {@code output}.  This does not    * flush or close the stream.    */
name|void
name|writeTo
parameter_list|(
name|CodedOutputStream
name|output
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the number of bytes required to encode this message.  The result    * is only computed on the first call and memoized after that.    */
name|int
name|getSerializedSize
parameter_list|()
function_decl|;
comment|/**    * Gets the parser for a message of the same type as this message.    */
name|Parser
argument_list|<
name|?
extends|extends
name|MessageLite
argument_list|>
name|getParserForType
parameter_list|()
function_decl|;
comment|// -----------------------------------------------------------------
comment|// Convenience methods.
comment|/**    * Serializes the message to a {@code ByteString} and returns it. This is    * just a trivial wrapper around    * {@link #writeTo(CodedOutputStream)}.    */
name|ByteString
name|toByteString
parameter_list|()
function_decl|;
comment|/**    * Serializes the message to a {@code byte} array and returns it.  This is    * just a trivial wrapper around    * {@link #writeTo(CodedOutputStream)}.    */
name|byte
index|[]
name|toByteArray
parameter_list|()
function_decl|;
comment|/**    * Serializes the message and writes it to {@code output}.  This is just a    * trivial wrapper around {@link #writeTo(CodedOutputStream)}.  This does    * not flush or close the stream.    *<p>    * NOTE:  Protocol Buffers are not self-delimiting.  Therefore, if you write    * any more data to the stream after the message, you must somehow ensure    * that the parser on the receiving end does not interpret this as being    * part of the protocol message.  This can be done e.g. by writing the size    * of the message before the data, then making sure to limit the input to    * that size on the receiving end (e.g. by wrapping the InputStream in one    * which limits the input).  Alternatively, just use    * {@link #writeDelimitedTo(OutputStream)}.    */
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|output
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Like {@link #writeTo(OutputStream)}, but writes the size of the message    * as a varint before writing the data.  This allows more data to be written    * to the stream after the message without the need to delimit the message    * data yourself.  Use {@link Builder#mergeDelimitedFrom(InputStream)} (or    * the static method {@code YourMessageType.parseDelimitedFrom(InputStream)})    * to parse messages written by this method.    */
name|void
name|writeDelimitedTo
parameter_list|(
name|OutputStream
name|output
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|// =================================================================
comment|// Builders
comment|/**    * Constructs a new builder for a message of the same type as this message.    */
name|Builder
name|newBuilderForType
parameter_list|()
function_decl|;
comment|/**    * Constructs a builder initialized with the current message.  Use this to    * derive a new message from the current one.    */
name|Builder
name|toBuilder
parameter_list|()
function_decl|;
comment|/**    * Abstract interface implemented by Protocol Message builders.    */
interface|interface
name|Builder
extends|extends
name|MessageLiteOrBuilder
extends|,
name|Cloneable
block|{
comment|/** Resets all fields to their default values. */
name|Builder
name|clear
parameter_list|()
function_decl|;
comment|/**      * Constructs the message based on the state of the Builder. Subsequent      * changes to the Builder will not affect the returned message.      * @throws UninitializedMessageException The message is missing one or more      *         required fields (i.e. {@link #isInitialized()} returns false).      *         Use {@link #buildPartial()} to bypass this check.      */
name|MessageLite
name|build
parameter_list|()
function_decl|;
comment|/**      * Like {@link #build()}, but does not throw an exception if the message      * is missing required fields.  Instead, a partial message is returned.      * Subsequent changes to the Builder will not affect the returned message.      */
name|MessageLite
name|buildPartial
parameter_list|()
function_decl|;
comment|/**      * Clones the Builder.      * @see Object#clone()      */
name|Builder
name|clone
parameter_list|()
function_decl|;
comment|/**      * Parses a message of this type from the input and merges it with this      * message.      *      *<p>Warning:  This does not verify that all required fields are present in      * the input message.  If you call {@link #build()} without setting all      * required fields, it will throw an {@link UninitializedMessageException},      * which is a {@code RuntimeException} and thus might not be caught.  There      * are a few good ways to deal with this:      *<ul>      *<li>Call {@link #isInitialized()} to verify that all required fields      *       are set before building.      *<li>Use {@code buildPartial()} to build, which ignores missing      *       required fields.      *</ul>      *      *<p>Note:  The caller should call      * {@link CodedInputStream#checkLastTagWas(int)} after calling this to      * verify that the last tag seen was the appropriate end-group tag,      * or zero for EOF.      */
name|Builder
name|mergeFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Like {@link Builder#mergeFrom(CodedInputStream)}, but also      * parses extensions.  The extensions that you want to be able to parse      * must be registered in {@code extensionRegistry}.  Extensions not in      * the registry will be treated as unknown fields.      */
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
comment|// ---------------------------------------------------------------
comment|// Convenience methods.
comment|/**      * Parse {@code data} as a message of this type and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream)}.      *      * @return this      */
name|Builder
name|mergeFrom
parameter_list|(
name|ByteString
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**      * Parse {@code data} as a message of this type and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream,ExtensionRegistryLite)}.      *      * @return this      */
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
comment|/**      * Parse {@code data} as a message of this type and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream)}.      *      * @return this      */
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
comment|/**      * Parse {@code data} as a message of this type and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream)}.      *      * @return this      */
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
comment|/**      * Parse {@code data} as a message of this type and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream,ExtensionRegistryLite)}.      *      * @return this      */
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
comment|/**      * Parse {@code data} as a message of this type and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream,ExtensionRegistryLite)}.      *      * @return this      */
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
comment|/**      * Parse a message of this type from {@code input} and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream)}.  Note that this method always      * reads the<i>entire</i> input (unless it throws an exception).  If you      * want it to stop earlier, you will need to wrap your input in some      * wrapper stream that limits reading.  Or, use      * {@link MessageLite#writeDelimitedTo(OutputStream)} to write your message      * and {@link #mergeDelimitedFrom(InputStream)} to read it.      *<p>      * Despite usually reading the entire input, this does not close the stream.      *      * @return this      */
name|Builder
name|mergeFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Parse a message of this type from {@code input} and merge it with the      * message being built.  This is just a small wrapper around      * {@link #mergeFrom(CodedInputStream,ExtensionRegistryLite)}.      *      * @return this      */
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
comment|/**      * Merge {@code other} into the message being built.  {@code other} must      * have the exact same type as {@code this} (i.e.      * {@code getClass().equals(getDefaultInstanceForType().getClass())}).      *      * Merging occurs as follows.  For each field:<br>      * * For singular primitive fields, if the field is set in {@code other},      *   then {@code other}'s value overwrites the value in this message.<br>      * * For singular message fields, if the field is set in {@code other},      *   it is merged into the corresponding sub-message of this message      *   using the same merging rules.<br>      * * For repeated fields, the elements in {@code other} are concatenated      *   with the elements in this message.      * * For oneof groups, if the other message has one of the fields set,      *   the group of this message is cleared and replaced by the field      *   of the other message, so that the oneof constraint is preserved.      *      * This is equivalent to the {@code Message::MergeFrom} method in C++.      */
name|Builder
name|mergeFrom
parameter_list|(
name|MessageLite
name|other
parameter_list|)
function_decl|;
comment|/**      * Like {@link #mergeFrom(InputStream)}, but does not read until EOF.      * Instead, the size of the message (encoded as a varint) is read first,      * then the message data.  Use      * {@link MessageLite#writeDelimitedTo(OutputStream)} to write messages in      * this format.      *      * @return True if successful, or false if the stream is at EOF when the      *         method starts.  Any other error (including reaching EOF during      *         parsing) will cause an exception to be thrown.      */
name|boolean
name|mergeDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Like {@link #mergeDelimitedFrom(InputStream)} but supporting extensions.      */
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

