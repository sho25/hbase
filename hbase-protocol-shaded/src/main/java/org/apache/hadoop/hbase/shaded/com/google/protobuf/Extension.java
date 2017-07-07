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
comment|/**  * Interface that generated extensions implement.  *  * @author liujisi@google.com (Jisi Liu)  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Extension
parameter_list|<
name|ContainingType
extends|extends
name|MessageLite
parameter_list|,
name|Type
parameter_list|>
extends|extends
name|ExtensionLite
argument_list|<
name|ContainingType
argument_list|,
name|Type
argument_list|>
block|{
comment|/** Returns the descriptor of the extension. */
specifier|public
specifier|abstract
name|Descriptors
operator|.
name|FieldDescriptor
name|getDescriptor
parameter_list|()
function_decl|;
comment|/** Returns whether or not this extension is a Lite Extension. */
annotation|@
name|Override
specifier|final
name|boolean
name|isLite
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|// All the methods below are extension implementation details.
comment|/**    * The API type that the extension is used for.    */
specifier|protected
enum|enum
name|ExtensionType
block|{
name|IMMUTABLE
block|,
name|MUTABLE
block|,
name|PROTO1
block|,   }
specifier|protected
name|ExtensionType
name|getExtensionType
parameter_list|()
block|{
comment|// TODO(liujisi): make this abstract after we fix proto1.
return|return
name|ExtensionType
operator|.
name|IMMUTABLE
return|;
block|}
comment|/**    * Type of a message extension.    */
specifier|public
enum|enum
name|MessageType
block|{
name|PROTO1
block|,
name|PROTO2
block|,   }
comment|/**    * If the extension is a message extension (i.e., getLiteType() == MESSAGE),    * returns the type of the message, otherwise undefined.    */
specifier|public
name|MessageType
name|getMessageType
parameter_list|()
block|{
return|return
name|MessageType
operator|.
name|PROTO2
return|;
block|}
specifier|protected
specifier|abstract
name|Object
name|fromReflectionType
parameter_list|(
name|Object
name|value
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|singularFromReflectionType
parameter_list|(
name|Object
name|value
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|toReflectionType
parameter_list|(
name|Object
name|value
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|singularToReflectionType
parameter_list|(
name|Object
name|value
parameter_list|)
function_decl|;
block|}
end_class

end_unit

