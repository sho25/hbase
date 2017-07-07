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
name|List
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
comment|/**  * Base interface for methods common to {@link Message} and  * {@link Message.Builder} to provide type equivalency.  *  * @author jonp@google.com (Jon Perlow)  */
end_comment

begin_interface
specifier|public
interface|interface
name|MessageOrBuilder
extends|extends
name|MessageLiteOrBuilder
block|{
comment|// (From MessageLite, re-declared here only for return type covariance.)
annotation|@
name|Override
name|Message
name|getDefaultInstanceForType
parameter_list|()
function_decl|;
comment|/**    * Returns a list of field paths (e.g. "foo.bar.baz") of required fields    * which are not set in this message.  You should call    * {@link MessageLiteOrBuilder#isInitialized()} first to check if there    * are any missing fields, as that method is likely to be much faster    * than this one even when the message is fully-initialized.    */
name|List
argument_list|<
name|String
argument_list|>
name|findInitializationErrors
parameter_list|()
function_decl|;
comment|/**    * Returns a comma-delimited list of required fields which are not set    * in this message object.  You should call    * {@link MessageLiteOrBuilder#isInitialized()} first to check if there    * are any missing fields, as that method is likely to be much faster    * than this one even when the message is fully-initialized.    */
name|String
name|getInitializationErrorString
parameter_list|()
function_decl|;
comment|/**    * Get the message's type's descriptor.  This differs from the    * {@code getDescriptor()} method of generated message classes in that    * this method is an abstract method of the {@code Message} interface    * whereas {@code getDescriptor()} is a static method of a specific class.    * They return the same thing.    */
name|Descriptors
operator|.
name|Descriptor
name|getDescriptorForType
parameter_list|()
function_decl|;
comment|/**    * Returns a collection of all the fields in this message which are set    * and their corresponding values.  A singular ("required" or "optional")    * field is set iff hasField() returns true for that field.  A "repeated"    * field is set iff getRepeatedFieldCount() is greater than zero.  The    * values are exactly what would be returned by calling    * {@link #getField(Descriptors.FieldDescriptor)} for each field.  The map    * is guaranteed to be a sorted map, so iterating over it will return fields    * in order by field number.    *<br>    * If this is for a builder, the returned map may or may not reflect future    * changes to the builder.  Either way, the returned map is itself    * unmodifiable.    */
name|Map
argument_list|<
name|Descriptors
operator|.
name|FieldDescriptor
argument_list|,
name|Object
argument_list|>
name|getAllFields
parameter_list|()
function_decl|;
comment|/**    * Returns true if the given oneof is set.    * @throws IllegalArgumentException if    *           {@code oneof.getContainingType() != getDescriptorForType()}.    */
name|boolean
name|hasOneof
parameter_list|(
name|Descriptors
operator|.
name|OneofDescriptor
name|oneof
parameter_list|)
function_decl|;
comment|/**    * Obtains the FieldDescriptor if the given oneof is set. Returns null    * if no field is set.    */
name|Descriptors
operator|.
name|FieldDescriptor
name|getOneofFieldDescriptor
parameter_list|(
name|Descriptors
operator|.
name|OneofDescriptor
name|oneof
parameter_list|)
function_decl|;
comment|/**    * Returns true if the given field is set.  This is exactly equivalent to    * calling the generated "has" accessor method corresponding to the field.    * @throws IllegalArgumentException The field is a repeated field, or    *           {@code field.getContainingType() != getDescriptorForType()}.    */
name|boolean
name|hasField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|)
function_decl|;
comment|/**    * Obtains the value of the given field, or the default value if it is    * not set.  For primitive fields, the boxed primitive value is returned.    * For enum fields, the EnumValueDescriptor for the value is returned. For    * embedded message fields, the sub-message is returned.  For repeated    * fields, a java.util.List is returned.    */
name|Object
name|getField
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|)
function_decl|;
comment|/**    * Gets the number of elements of a repeated field.  This is exactly    * equivalent to calling the generated "Count" accessor method corresponding    * to the field.    * @throws IllegalArgumentException The field is not a repeated field, or    *           {@code field.getContainingType() != getDescriptorForType()}.    */
name|int
name|getRepeatedFieldCount
parameter_list|(
name|Descriptors
operator|.
name|FieldDescriptor
name|field
parameter_list|)
function_decl|;
comment|/**    * Gets an element of a repeated field.  For primitive fields, the boxed    * primitive value is returned.  For enum fields, the EnumValueDescriptor    * for the value is returned. For embedded message fields, the sub-message    * is returned.    * @throws IllegalArgumentException The field is not a repeated field, or    *           {@code field.getContainingType() != getDescriptorForType()}.    */
name|Object
name|getRepeatedField
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
comment|/** Get the {@link UnknownFieldSet} for this message. */
name|UnknownFieldSet
name|getUnknownFields
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

