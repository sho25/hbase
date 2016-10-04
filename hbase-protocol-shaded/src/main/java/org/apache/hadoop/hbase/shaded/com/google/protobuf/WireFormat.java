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
comment|/**  * This class is used internally by the Protocol Buffer library and generated  * message implementations.  It is public only because those generated messages  * do not reside in the {@code protobuf} package.  Others should not use this  * class directly.  *  * This class contains constants and helper functions useful for dealing with  * the Protocol Buffer wire format.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|WireFormat
block|{
comment|// Do not allow instantiation.
specifier|private
name|WireFormat
parameter_list|()
block|{}
specifier|static
specifier|final
name|int
name|FIXED_32_SIZE
init|=
literal|4
decl_stmt|;
specifier|static
specifier|final
name|int
name|FIXED_64_SIZE
init|=
literal|8
decl_stmt|;
specifier|static
specifier|final
name|int
name|MAX_VARINT_SIZE
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WIRETYPE_VARINT
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WIRETYPE_FIXED64
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WIRETYPE_LENGTH_DELIMITED
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WIRETYPE_START_GROUP
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WIRETYPE_END_GROUP
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WIRETYPE_FIXED32
init|=
literal|5
decl_stmt|;
specifier|static
specifier|final
name|int
name|TAG_TYPE_BITS
init|=
literal|3
decl_stmt|;
specifier|static
specifier|final
name|int
name|TAG_TYPE_MASK
init|=
operator|(
literal|1
operator|<<
name|TAG_TYPE_BITS
operator|)
operator|-
literal|1
decl_stmt|;
comment|/** Given a tag value, determines the wire type (the lower 3 bits). */
specifier|public
specifier|static
name|int
name|getTagWireType
parameter_list|(
specifier|final
name|int
name|tag
parameter_list|)
block|{
return|return
name|tag
operator|&
name|TAG_TYPE_MASK
return|;
block|}
comment|/** Given a tag value, determines the field number (the upper 29 bits). */
specifier|public
specifier|static
name|int
name|getTagFieldNumber
parameter_list|(
specifier|final
name|int
name|tag
parameter_list|)
block|{
return|return
name|tag
operator|>>>
name|TAG_TYPE_BITS
return|;
block|}
comment|/** Makes a tag value given a field number and wire type. */
specifier|static
name|int
name|makeTag
parameter_list|(
specifier|final
name|int
name|fieldNumber
parameter_list|,
specifier|final
name|int
name|wireType
parameter_list|)
block|{
return|return
operator|(
name|fieldNumber
operator|<<
name|TAG_TYPE_BITS
operator|)
operator||
name|wireType
return|;
block|}
comment|/**    * Lite equivalent to {@link Descriptors.FieldDescriptor.JavaType}.  This is    * only here to support the lite runtime and should not be used by users.    */
specifier|public
enum|enum
name|JavaType
block|{
name|INT
argument_list|(
literal|0
argument_list|)
block|,
name|LONG
argument_list|(
literal|0L
argument_list|)
block|,
name|FLOAT
argument_list|(
literal|0F
argument_list|)
block|,
name|DOUBLE
argument_list|(
literal|0D
argument_list|)
block|,
name|BOOLEAN
argument_list|(
literal|false
argument_list|)
block|,
name|STRING
argument_list|(
literal|""
argument_list|)
block|,
name|BYTE_STRING
parameter_list|(
name|ByteString
operator|.
name|EMPTY
parameter_list|)
operator|,
constructor|ENUM(null
block|)
enum|,
name|MESSAGE
argument_list|(
literal|null
argument_list|)
enum|;
name|JavaType
parameter_list|(
specifier|final
name|Object
name|defaultDefault
parameter_list|)
block|{
name|this
operator|.
name|defaultDefault
operator|=
name|defaultDefault
expr_stmt|;
block|}
comment|/**      * The default default value for fields of this type, if it's a primitive      * type.      */
name|Object
name|getDefaultDefault
parameter_list|()
block|{
return|return
name|defaultDefault
return|;
block|}
specifier|private
specifier|final
name|Object
name|defaultDefault
decl_stmt|;
block|}
end_class

begin_comment
comment|/**    * Lite equivalent to {@link Descriptors.FieldDescriptor.Type}.  This is    * only here to support the lite runtime and should not be used by users.    */
end_comment

begin_enum
specifier|public
enum|enum
name|FieldType
block|{
name|DOUBLE
parameter_list|(
name|JavaType
operator|.
name|DOUBLE
parameter_list|,
name|WIRETYPE_FIXED64
parameter_list|)
operator|,
constructor|FLOAT   (JavaType.FLOAT
operator|,
constructor|WIRETYPE_FIXED32
block|)
enum|,
name|INT64
argument_list|(
name|JavaType
operator|.
name|LONG
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|UINT64
argument_list|(
name|JavaType
operator|.
name|LONG
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|INT32
argument_list|(
name|JavaType
operator|.
name|INT
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|FIXED64
argument_list|(
name|JavaType
operator|.
name|LONG
argument_list|,
name|WIRETYPE_FIXED64
argument_list|)
operator|,
name|FIXED32
argument_list|(
name|JavaType
operator|.
name|INT
argument_list|,
name|WIRETYPE_FIXED32
argument_list|)
operator|,
name|BOOL
argument_list|(
name|JavaType
operator|.
name|BOOLEAN
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|STRING
argument_list|(
name|JavaType
operator|.
name|STRING
argument_list|,
name|WIRETYPE_LENGTH_DELIMITED
argument_list|)
block|{       @
name|Override
specifier|public
name|boolean
name|isPackable
argument_list|()
block|{
return|return
literal|false
return|;
block|}
end_enum

begin_expr_stmt
unit|},
name|GROUP
argument_list|(
name|JavaType
operator|.
name|MESSAGE
argument_list|,
name|WIRETYPE_START_GROUP
argument_list|)
block|{       @
name|Override
specifier|public
name|boolean
name|isPackable
argument_list|()
block|{
return|return
literal|false
return|;
block|}
end_expr_stmt

begin_expr_stmt
unit|},
name|MESSAGE
argument_list|(
name|JavaType
operator|.
name|MESSAGE
argument_list|,
name|WIRETYPE_LENGTH_DELIMITED
argument_list|)
block|{       @
name|Override
specifier|public
name|boolean
name|isPackable
argument_list|()
block|{
return|return
literal|false
return|;
block|}
end_expr_stmt

begin_expr_stmt
unit|},
name|BYTES
argument_list|(
name|JavaType
operator|.
name|BYTE_STRING
argument_list|,
name|WIRETYPE_LENGTH_DELIMITED
argument_list|)
block|{       @
name|Override
specifier|public
name|boolean
name|isPackable
argument_list|()
block|{
return|return
literal|false
return|;
block|}
end_expr_stmt

begin_expr_stmt
unit|},
name|UINT32
argument_list|(
name|JavaType
operator|.
name|INT
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|ENUM
argument_list|(
name|JavaType
operator|.
name|ENUM
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|SFIXED32
argument_list|(
name|JavaType
operator|.
name|INT
argument_list|,
name|WIRETYPE_FIXED32
argument_list|)
operator|,
name|SFIXED64
argument_list|(
name|JavaType
operator|.
name|LONG
argument_list|,
name|WIRETYPE_FIXED64
argument_list|)
operator|,
name|SINT32
argument_list|(
name|JavaType
operator|.
name|INT
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
operator|,
name|SINT64
argument_list|(
name|JavaType
operator|.
name|LONG
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|FieldType
argument_list|(
name|final
name|JavaType
name|javaType
argument_list|,
name|final
name|int
name|wireType
argument_list|)
block|{
name|this
operator|.
name|javaType
operator|=
name|javaType
block|;
name|this
operator|.
name|wireType
operator|=
name|wireType
block|;     }
specifier|private
name|final
name|JavaType
name|javaType
expr_stmt|;
end_expr_stmt

begin_decl_stmt
specifier|private
specifier|final
name|int
name|wireType
decl_stmt|;
end_decl_stmt

begin_function
specifier|public
name|JavaType
name|getJavaType
parameter_list|()
block|{
return|return
name|javaType
return|;
block|}
end_function

begin_function
specifier|public
name|int
name|getWireType
parameter_list|()
block|{
return|return
name|wireType
return|;
block|}
end_function

begin_function
specifier|public
name|boolean
name|isPackable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
end_function

begin_comment
unit|}
comment|// Field numbers for fields in MessageSet wire format.
end_comment

begin_decl_stmt
unit|static
specifier|final
name|int
name|MESSAGE_SET_ITEM
init|=
literal|1
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|int
name|MESSAGE_SET_TYPE_ID
init|=
literal|2
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|int
name|MESSAGE_SET_MESSAGE
init|=
literal|3
decl_stmt|;
end_decl_stmt

begin_comment
comment|// Tag numbers.
end_comment

begin_decl_stmt
specifier|static
specifier|final
name|int
name|MESSAGE_SET_ITEM_TAG
init|=
name|makeTag
argument_list|(
name|MESSAGE_SET_ITEM
argument_list|,
name|WIRETYPE_START_GROUP
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|int
name|MESSAGE_SET_ITEM_END_TAG
init|=
name|makeTag
argument_list|(
name|MESSAGE_SET_ITEM
argument_list|,
name|WIRETYPE_END_GROUP
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|int
name|MESSAGE_SET_TYPE_ID_TAG
init|=
name|makeTag
argument_list|(
name|MESSAGE_SET_TYPE_ID
argument_list|,
name|WIRETYPE_VARINT
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|int
name|MESSAGE_SET_MESSAGE_TAG
init|=
name|makeTag
argument_list|(
name|MESSAGE_SET_MESSAGE
argument_list|,
name|WIRETYPE_LENGTH_DELIMITED
argument_list|)
decl_stmt|;
end_decl_stmt

begin_comment
comment|/**    * Validation level for handling incoming string field data which potentially    * contain non-UTF8 bytes.    */
end_comment

begin_enum
enum|enum
name|Utf8Validation
block|{
comment|/** Eagerly parses to String; silently accepts invalid UTF8 bytes. */
name|LOOSE
block|{
annotation|@
name|Override
name|Object
name|readString
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|input
operator|.
name|readString
argument_list|()
return|;
block|}
block|}
block|,
comment|/** Eagerly parses to String; throws an IOException on invalid bytes. */
name|STRICT
block|{
annotation|@
name|Override
name|Object
name|readString
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|input
operator|.
name|readStringRequireUtf8
argument_list|()
return|;
block|}
block|}
block|,
comment|/** Keep data as ByteString; validation/conversion to String is lazy. */
name|LAZY
block|{
annotation|@
name|Override
name|Object
name|readString
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|input
operator|.
name|readBytes
argument_list|()
return|;
block|}
block|}
block|;
comment|/** Read a string field from the input with the proper UTF8 validation. */
specifier|abstract
name|Object
name|readString
parameter_list|(
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_enum

begin_comment
comment|/**    * Read a field of any primitive type for immutable messages from a    * CodedInputStream. Enums, groups, and embedded messages are not handled by    * this method.    *    * @param input The stream from which to read.    * @param type Declared type of the field.    * @param utf8Validation Different string UTF8 validation level for handling    *                       string fields.    * @return An object representing the field's value, of the exact    *         type which would be returned by    *         {@link Message#getField(Descriptors.FieldDescriptor)} for    *         this field.    */
end_comment

begin_function
specifier|static
name|Object
name|readPrimitiveField
parameter_list|(
name|CodedInputStream
name|input
parameter_list|,
name|FieldType
name|type
parameter_list|,
name|Utf8Validation
name|utf8Validation
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DOUBLE
case|:
return|return
name|input
operator|.
name|readDouble
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
name|input
operator|.
name|readFloat
argument_list|()
return|;
case|case
name|INT64
case|:
return|return
name|input
operator|.
name|readInt64
argument_list|()
return|;
case|case
name|UINT64
case|:
return|return
name|input
operator|.
name|readUInt64
argument_list|()
return|;
case|case
name|INT32
case|:
return|return
name|input
operator|.
name|readInt32
argument_list|()
return|;
case|case
name|FIXED64
case|:
return|return
name|input
operator|.
name|readFixed64
argument_list|()
return|;
case|case
name|FIXED32
case|:
return|return
name|input
operator|.
name|readFixed32
argument_list|()
return|;
case|case
name|BOOL
case|:
return|return
name|input
operator|.
name|readBool
argument_list|()
return|;
case|case
name|BYTES
case|:
return|return
name|input
operator|.
name|readBytes
argument_list|()
return|;
case|case
name|UINT32
case|:
return|return
name|input
operator|.
name|readUInt32
argument_list|()
return|;
case|case
name|SFIXED32
case|:
return|return
name|input
operator|.
name|readSFixed32
argument_list|()
return|;
case|case
name|SFIXED64
case|:
return|return
name|input
operator|.
name|readSFixed64
argument_list|()
return|;
case|case
name|SINT32
case|:
return|return
name|input
operator|.
name|readSInt32
argument_list|()
return|;
case|case
name|SINT64
case|:
return|return
name|input
operator|.
name|readSInt64
argument_list|()
return|;
case|case
name|STRING
case|:
return|return
name|utf8Validation
operator|.
name|readString
argument_list|(
name|input
argument_list|)
return|;
case|case
name|GROUP
case|:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"readPrimitiveField() cannot handle nested groups."
argument_list|)
throw|;
case|case
name|MESSAGE
case|:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"readPrimitiveField() cannot handle embedded messages."
argument_list|)
throw|;
case|case
name|ENUM
case|:
comment|// We don't handle enums because we don't know what to do if the
comment|// value is not recognized.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"readPrimitiveField() cannot handle enums."
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"There is no way to get here, but the compiler thinks otherwise."
argument_list|)
throw|;
block|}
end_function

unit|}
end_unit

