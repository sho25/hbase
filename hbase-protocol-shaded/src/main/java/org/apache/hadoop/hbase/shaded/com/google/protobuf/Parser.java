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
name|InputStream
import|;
end_import

begin_comment
comment|/**  * Abstract interface for parsing Protocol Messages.  *  * The implementation should be stateless and thread-safe.  *  *<p>All methods may throw {@link InvalidProtocolBufferException}. In the event of invalid data,  * like an encoding error, the cause of the thrown exception will be {@code null}. However, if an  * I/O problem occurs, an exception is thrown with an {@link java.io.IOException} cause.  *  * @author liujisi@google.com (Pherl Liu)  */
end_comment

begin_interface
specifier|public
interface|interface
name|Parser
parameter_list|<
name|MessageType
parameter_list|>
block|{
comment|// NB(jh): Other parts of the protobuf API that parse messages distinguish between an I/O problem
comment|// (like failure reading bytes from a socket) and invalid data (encoding error) via the type of
comment|// thrown exception. But it would be source-incompatible to make the methods in this interface do
comment|// so since they were originally spec'ed to only throw InvalidProtocolBufferException. So callers
comment|// must inspect the cause of the exception to distinguish these two cases.
comment|/**    * Parses a message of {@code MessageType} from the input.    *    *<p>Note:  The caller should call    * {@link CodedInputStream#checkLastTagWas(int)} after calling this to    * verify that the last tag seen was the appropriate end-group tag,    * or zero for EOF.    */
specifier|public
name|MessageType
name|parseFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(CodedInputStream)}, but also parses extensions.    * The extensions that you want to be able to parse must be registered in    * {@code extensionRegistry}. Extensions not in the registry will be treated    * as unknown fields.    */
specifier|public
name|MessageType
name|parseFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(CodedInputStream)}, but does not throw an    * exception if the message is missing required fields. Instead, a partial    * message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(CodedInputStream input, ExtensionRegistryLite)},    * but does not throw an exception if the message is missing required fields.    * Instead, a partial message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|// ---------------------------------------------------------------
comment|// Convenience methods.
comment|/**    * Parses {@code data} as a message of {@code MessageType}.    * This is just a small wrapper around {@link #parseFrom(CodedInputStream)}.    */
specifier|public
name|MessageType
name|parseFrom
parameter_list|(
name|ByteString
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Parses {@code data} as a message of {@code MessageType}.    * This is just a small wrapper around    * {@link #parseFrom(CodedInputStream, ExtensionRegistryLite)}.    */
specifier|public
name|MessageType
name|parseFrom
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
comment|/**    * Like {@link #parseFrom(ByteString)}, but does not throw an    * exception if the message is missing required fields. Instead, a partial    * message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
parameter_list|(
name|ByteString
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(ByteString, ExtensionRegistryLite)},    * but does not throw an exception if the message is missing required fields.    * Instead, a partial message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
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
comment|/**    * Parses {@code data} as a message of {@code MessageType}.    * This is just a small wrapper around {@link #parseFrom(CodedInputStream)}.    */
specifier|public
name|MessageType
name|parseFrom
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
comment|/**    * Parses {@code data} as a message of {@code MessageType}.    * This is just a small wrapper around    * {@link #parseFrom(CodedInputStream, ExtensionRegistryLite)}.    */
specifier|public
name|MessageType
name|parseFrom
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
comment|/**    * Parses {@code data} as a message of {@code MessageType}.    * This is just a small wrapper around {@link #parseFrom(CodedInputStream)}.    */
specifier|public
name|MessageType
name|parseFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Parses {@code data} as a message of {@code MessageType}.    * This is just a small wrapper around    * {@link #parseFrom(CodedInputStream, ExtensionRegistryLite)}.    */
specifier|public
name|MessageType
name|parseFrom
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
comment|/**    * Like {@link #parseFrom(byte[], int, int)}, but does not throw an    * exception if the message is missing required fields. Instead, a partial    * message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
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
comment|/**    * Like {@link #parseFrom(ByteString, ExtensionRegistryLite)},    * but does not throw an exception if the message is missing required fields.    * Instead, a partial message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
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
comment|/**    * Like {@link #parseFrom(byte[])}, but does not throw an    * exception if the message is missing required fields. Instead, a partial    * message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(byte[], ExtensionRegistryLite)},    * but does not throw an exception if the message is missing required fields.    * Instead, a partial message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
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
comment|/**    * Parse a message of {@code MessageType} from {@code input}.    * This is just a small wrapper around {@link #parseFrom(CodedInputStream)}.    * Note that this method always reads the<i>entire</i> input (unless it    * throws an exception).  If you want it to stop earlier, you will need to    * wrap your input in some wrapper stream that limits reading.  Or, use    * {@link MessageLite#writeDelimitedTo(java.io.OutputStream)} to write your    * message and {@link #parseDelimitedFrom(InputStream)} to read it.    *<p>    * Despite usually reading the entire input, this does not close the stream.    */
specifier|public
name|MessageType
name|parseFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Parses a message of {@code MessageType} from {@code input}.    * This is just a small wrapper around    * {@link #parseFrom(CodedInputStream, ExtensionRegistryLite)}.    */
specifier|public
name|MessageType
name|parseFrom
parameter_list|(
name|InputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(InputStream)}, but does not throw an    * exception if the message is missing required fields. Instead, a partial    * message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(InputStream, ExtensionRegistryLite)},    * but does not throw an exception if the message is missing required fields.    * Instead, a partial message is returned.    */
specifier|public
name|MessageType
name|parsePartialFrom
parameter_list|(
name|InputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseFrom(InputStream)}, but does not read util EOF.    * Instead, the size of message (encoded as a varint) is read first,    * then the message data. Use    * {@link MessageLite#writeDelimitedTo(java.io.OutputStream)} to write    * messages in this format.    *    * @return Parsed message if successful, or null if the stream is at EOF when    *         the method starts. Any other error (including reaching EOF during    *         parsing) will cause an exception to be thrown.    */
specifier|public
name|MessageType
name|parseDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseDelimitedFrom(InputStream)} but supporting extensions.    */
specifier|public
name|MessageType
name|parseDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseDelimitedFrom(InputStream)}, but does not throw an    * exception if the message is missing required fields. Instead, a partial    * message is returned.    */
specifier|public
name|MessageType
name|parsePartialDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
comment|/**    * Like {@link #parseDelimitedFrom(InputStream, ExtensionRegistryLite)},    * but does not throw an exception if the message is missing required fields.    * Instead, a partial message is returned.    */
specifier|public
name|MessageType
name|parsePartialDelimitedFrom
parameter_list|(
name|InputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
function_decl|;
block|}
end_interface

end_unit

