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
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * Helps generate {@link String} representations of {@link MessageLite} protos.  */
end_comment

begin_comment
comment|// TODO(dweis): Fix map fields.
end_comment

begin_class
specifier|final
class|class
name|MessageLiteToString
block|{
specifier|private
specifier|static
specifier|final
name|String
name|LIST_SUFFIX
init|=
literal|"List"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BUILDER_LIST_SUFFIX
init|=
literal|"OrBuilderList"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BYTES_SUFFIX
init|=
literal|"Bytes"
decl_stmt|;
comment|/**    * Returns a {@link String} representation of the {@link MessageLite} object.  The first line of    * the {@code String} representation representation includes a comment string to uniquely identify    * the objcet instance. This acts as an indicator that this should not be relied on for    * comparisons.    *    *<p>For use by generated code only.    */
specifier|static
name|String
name|toString
parameter_list|(
name|MessageLite
name|messageLite
parameter_list|,
name|String
name|commentString
parameter_list|)
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"# "
argument_list|)
operator|.
name|append
argument_list|(
name|commentString
argument_list|)
expr_stmt|;
name|reflectivePrintWithIndent
argument_list|(
name|messageLite
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Reflectively prints the {@link MessageLite} to the buffer at given {@code indent} level.    *    * @param buffer the buffer to write to    * @param indent the number of spaces to indent the proto by    */
specifier|private
specifier|static
name|void
name|reflectivePrintWithIndent
parameter_list|(
name|MessageLite
name|messageLite
parameter_list|,
name|StringBuilder
name|buffer
parameter_list|,
name|int
name|indent
parameter_list|)
block|{
comment|// Build a map of method name to method. We're looking for methods like getFoo(), hasFoo(), and
comment|// getFooList() which might be useful for building an object's string representation.
name|Map
argument_list|<
name|String
argument_list|,
name|Method
argument_list|>
name|nameToNoArgMethod
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Method
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Method
argument_list|>
name|nameToMethod
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Method
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|getters
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|messageLite
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
name|nameToMethod
operator|.
name|put
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|,
name|method
argument_list|)
expr_stmt|;
if|if
condition|(
name|method
operator|.
name|getParameterTypes
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|nameToNoArgMethod
operator|.
name|put
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|,
name|method
argument_list|)
expr_stmt|;
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"get"
argument_list|)
condition|)
block|{
name|getters
operator|.
name|add
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|getter
range|:
name|getters
control|)
block|{
name|String
name|suffix
init|=
name|getter
operator|.
name|replaceFirst
argument_list|(
literal|"get"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|suffix
operator|.
name|endsWith
argument_list|(
name|LIST_SUFFIX
argument_list|)
operator|&&
operator|!
name|suffix
operator|.
name|endsWith
argument_list|(
name|BUILDER_LIST_SUFFIX
argument_list|)
condition|)
block|{
name|String
name|camelCase
init|=
name|suffix
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|+
name|suffix
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|suffix
operator|.
name|length
argument_list|()
operator|-
name|LIST_SUFFIX
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
comment|// Try to reflectively get the value and toString() the field as if it were repeated. This
comment|// only works if the method names have not be proguarded out or renamed.
name|Method
name|listMethod
init|=
name|nameToNoArgMethod
operator|.
name|get
argument_list|(
literal|"get"
operator|+
name|suffix
argument_list|)
decl_stmt|;
if|if
condition|(
name|listMethod
operator|!=
literal|null
operator|&&
name|listMethod
operator|.
name|getReturnType
argument_list|()
operator|.
name|equals
argument_list|(
name|List
operator|.
name|class
argument_list|)
condition|)
block|{
name|printField
argument_list|(
name|buffer
argument_list|,
name|indent
argument_list|,
name|camelCaseToSnakeCase
argument_list|(
name|camelCase
argument_list|)
argument_list|,
name|GeneratedMessageLite
operator|.
name|invokeOrDie
argument_list|(
name|listMethod
argument_list|,
name|messageLite
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
name|Method
name|setter
init|=
name|nameToMethod
operator|.
name|get
argument_list|(
literal|"set"
operator|+
name|suffix
argument_list|)
decl_stmt|;
if|if
condition|(
name|setter
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|suffix
operator|.
name|endsWith
argument_list|(
name|BYTES_SUFFIX
argument_list|)
operator|&&
name|nameToNoArgMethod
operator|.
name|containsKey
argument_list|(
literal|"get"
operator|+
name|suffix
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|suffix
operator|.
name|length
argument_list|()
operator|-
literal|"Bytes"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
comment|// Heuristic to skip bytes based accessors for string fields.
continue|continue;
block|}
name|String
name|camelCase
init|=
name|suffix
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|+
name|suffix
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// Try to reflectively get the value and toString() the field as if it were optional. This
comment|// only works if the method names have not be proguarded out or renamed.
name|Method
name|getMethod
init|=
name|nameToNoArgMethod
operator|.
name|get
argument_list|(
literal|"get"
operator|+
name|suffix
argument_list|)
decl_stmt|;
name|Method
name|hasMethod
init|=
name|nameToNoArgMethod
operator|.
name|get
argument_list|(
literal|"has"
operator|+
name|suffix
argument_list|)
decl_stmt|;
comment|// TODO(dweis): Fix proto3 semantics.
if|if
condition|(
name|getMethod
operator|!=
literal|null
condition|)
block|{
name|Object
name|value
init|=
name|GeneratedMessageLite
operator|.
name|invokeOrDie
argument_list|(
name|getMethod
argument_list|,
name|messageLite
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|hasValue
init|=
name|hasMethod
operator|==
literal|null
condition|?
operator|!
name|isDefaultValue
argument_list|(
name|value
argument_list|)
else|:
operator|(
name|Boolean
operator|)
name|GeneratedMessageLite
operator|.
name|invokeOrDie
argument_list|(
name|hasMethod
argument_list|,
name|messageLite
argument_list|)
decl_stmt|;
comment|// TODO(dweis): This doesn't stop printing oneof case twice: value and enum style.
if|if
condition|(
name|hasValue
condition|)
block|{
name|printField
argument_list|(
name|buffer
argument_list|,
name|indent
argument_list|,
name|camelCaseToSnakeCase
argument_list|(
name|camelCase
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
block|}
if|if
condition|(
name|messageLite
operator|instanceof
name|GeneratedMessageLite
operator|.
name|ExtendableMessage
condition|)
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|GeneratedMessageLite
operator|.
name|ExtensionDescriptor
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iter
init|=
operator|(
operator|(
name|GeneratedMessageLite
operator|.
name|ExtendableMessage
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|messageLite
operator|)
operator|.
name|extensions
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|GeneratedMessageLite
operator|.
name|ExtensionDescriptor
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|printField
argument_list|(
name|buffer
argument_list|,
name|indent
argument_list|,
literal|"["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getNumber
argument_list|()
operator|+
literal|"]"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|(
operator|(
name|GeneratedMessageLite
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|messageLite
operator|)
operator|.
name|unknownFields
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|GeneratedMessageLite
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|messageLite
operator|)
operator|.
name|unknownFields
operator|.
name|printWithIndent
argument_list|(
name|buffer
argument_list|,
name|indent
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isDefaultValue
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|Boolean
condition|)
block|{
return|return
operator|!
operator|(
operator|(
name|Boolean
operator|)
name|o
operator|)
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|Integer
condition|)
block|{
return|return
operator|(
operator|(
name|Integer
operator|)
name|o
operator|)
operator|==
literal|0
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|Float
condition|)
block|{
return|return
operator|(
operator|(
name|Float
operator|)
name|o
operator|)
operator|==
literal|0f
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|Double
condition|)
block|{
return|return
operator|(
operator|(
name|Double
operator|)
name|o
operator|)
operator|==
literal|0d
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|String
condition|)
block|{
return|return
name|o
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|ByteString
condition|)
block|{
return|return
name|o
operator|.
name|equals
argument_list|(
name|ByteString
operator|.
name|EMPTY
argument_list|)
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|MessageLite
condition|)
block|{
comment|// Can happen in oneofs.
return|return
name|o
operator|==
operator|(
operator|(
name|MessageLite
operator|)
name|o
operator|)
operator|.
name|getDefaultInstanceForType
argument_list|()
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|java
operator|.
name|lang
operator|.
name|Enum
argument_list|<
name|?
argument_list|>
condition|)
block|{
comment|// Catches oneof enums.
return|return
operator|(
operator|(
name|java
operator|.
name|lang
operator|.
name|Enum
argument_list|<
name|?
argument_list|>
operator|)
name|o
operator|)
operator|.
name|ordinal
argument_list|()
operator|==
literal|0
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Formats a text proto field.    *    *<p>For use by generated code only.    *    * @param buffer the buffer to write to    * @param indent the number of spaces the proto should be indented by    * @param name the field name (in lower underscore case)    * @param object the object value of the field    */
specifier|static
specifier|final
name|void
name|printField
parameter_list|(
name|StringBuilder
name|buffer
parameter_list|,
name|int
name|indent
parameter_list|,
name|String
name|name
parameter_list|,
name|Object
name|object
parameter_list|)
block|{
if|if
condition|(
name|object
operator|instanceof
name|List
argument_list|<
name|?
argument_list|>
condition|)
block|{
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|object
decl_stmt|;
for|for
control|(
name|Object
name|entry
operator|:
name|list
control|)
block|{
name|printField
argument_list|(
name|buffer
argument_list|,
name|indent
argument_list|,
name|name
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indent
condition|;
name|i
operator|++
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
parameter_list|(
name|name
parameter_list|)
constructor_decl|;
if|if
condition|(
name|object
operator|instanceof
name|String
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|": \""
argument_list|)
operator|.
name|append
argument_list|(
name|TextFormatEscaper
operator|.
name|escapeText
argument_list|(
operator|(
name|String
operator|)
name|object
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|object
operator|instanceof
name|ByteString
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|": \""
argument_list|)
operator|.
name|append
argument_list|(
name|TextFormatEscaper
operator|.
name|escapeBytes
argument_list|(
operator|(
name|ByteString
operator|)
name|object
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|object
operator|instanceof
name|GeneratedMessageLite
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|" {"
argument_list|)
expr_stmt|;
name|reflectivePrintWithIndent
argument_list|(
operator|(
name|GeneratedMessageLite
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|object
argument_list|,
name|buffer
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indent
condition|;
name|i
operator|++
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|": "
argument_list|)
operator|.
name|append
argument_list|(
name|object
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_function
specifier|private
specifier|static
specifier|final
name|String
name|camelCaseToSnakeCase
parameter_list|(
name|String
name|camelCase
parameter_list|)
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|camelCase
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|char
name|ch
init|=
name|camelCase
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|Character
operator|.
name|isUpperCase
argument_list|(
name|ch
argument_list|)
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|Character
operator|.
name|toLowerCase
argument_list|(
name|ch
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
end_function

unit|}
end_unit

