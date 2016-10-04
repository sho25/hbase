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
comment|/**  * Lite interface that generated extensions implement.  *<p>  * Methods are for use by generated code only. You can hold a reference to  * extensions using this type name.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ExtensionLite
parameter_list|<
name|ContainingType
extends|extends
name|MessageLite
parameter_list|,
name|Type
parameter_list|>
block|{
comment|/** Returns the field number of the extension. */
specifier|public
specifier|abstract
name|int
name|getNumber
parameter_list|()
function_decl|;
comment|/** Returns the type of the field. */
specifier|public
specifier|abstract
name|WireFormat
operator|.
name|FieldType
name|getLiteType
parameter_list|()
function_decl|;
comment|/** Returns whether it is a repeated field. */
specifier|public
specifier|abstract
name|boolean
name|isRepeated
parameter_list|()
function_decl|;
comment|/** Returns the default value of the extension field. */
specifier|public
specifier|abstract
name|Type
name|getDefaultValue
parameter_list|()
function_decl|;
comment|/**    * Returns the default instance of the extension field, if it's a message    * extension.    */
specifier|public
specifier|abstract
name|MessageLite
name|getMessageDefaultInstance
parameter_list|()
function_decl|;
comment|/** Returns whether or not this extension is a Lite Extension. */
name|boolean
name|isLite
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

