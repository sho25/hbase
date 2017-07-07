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
comment|/**  * Provide text format escaping support for proto2 instances.  */
end_comment

begin_class
specifier|final
class|class
name|TextFormatEscaper
block|{
specifier|private
name|TextFormatEscaper
parameter_list|()
block|{}
specifier|private
interface|interface
name|ByteSequence
block|{
name|int
name|size
parameter_list|()
function_decl|;
name|byte
name|byteAt
parameter_list|(
name|int
name|offset
parameter_list|)
function_decl|;
block|}
comment|/**    * Escapes bytes in the format used in protocol buffer text format, which    * is the same as the format used for C string literals.  All bytes    * that are not printable 7-bit ASCII characters are escaped, as well as    * backslash, single-quote, and double-quote characters.  Characters for    * which no defined short-hand escape sequence is defined will be escaped    * using 3-digit octal sequences.    */
specifier|static
name|String
name|escapeBytes
parameter_list|(
specifier|final
name|ByteSequence
name|input
parameter_list|)
block|{
specifier|final
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
name|input
operator|.
name|size
argument_list|()
argument_list|)
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
name|input
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|byte
name|b
init|=
name|input
operator|.
name|byteAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|b
condition|)
block|{
comment|// Java does not recognize \a or \v, apparently.
case|case
literal|0x07
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\a"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\b'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\b"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\f'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\f"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\n'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\n"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\r'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\r"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\t"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|0x0b
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\v"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\\'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\\\"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\''
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\\'"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'"'
case|:
name|builder
operator|.
name|append
argument_list|(
literal|"\\\""
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// Only ASCII characters between 0x20 (space) and 0x7e (tilde) are
comment|// printable.  Other byte values must be escaped.
if|if
condition|(
name|b
operator|>=
literal|0x20
operator|&&
name|b
operator|<=
literal|0x7e
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
operator|(
name|char
operator|)
name|b
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
call|(
name|char
call|)
argument_list|(
literal|'0'
operator|+
operator|(
operator|(
name|b
operator|>>>
literal|6
operator|)
operator|&
literal|3
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
call|(
name|char
call|)
argument_list|(
literal|'0'
operator|+
operator|(
operator|(
name|b
operator|>>>
literal|3
operator|)
operator|&
literal|7
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
call|(
name|char
call|)
argument_list|(
literal|'0'
operator|+
operator|(
name|b
operator|&
literal|7
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Escapes bytes in the format used in protocol buffer text format, which    * is the same as the format used for C string literals.  All bytes    * that are not printable 7-bit ASCII characters are escaped, as well as    * backslash, single-quote, and double-quote characters.  Characters for    * which no defined short-hand escape sequence is defined will be escaped    * using 3-digit octal sequences.    */
specifier|static
name|String
name|escapeBytes
parameter_list|(
specifier|final
name|ByteString
name|input
parameter_list|)
block|{
return|return
name|escapeBytes
argument_list|(
operator|new
name|ByteSequence
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|input
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|byteAt
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
return|return
name|input
operator|.
name|byteAt
argument_list|(
name|offset
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * Like {@link #escapeBytes(ByteString)}, but used for byte array.    */
specifier|static
name|String
name|escapeBytes
parameter_list|(
specifier|final
name|byte
index|[]
name|input
parameter_list|)
block|{
return|return
name|escapeBytes
argument_list|(
operator|new
name|ByteSequence
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|input
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|byteAt
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
return|return
name|input
index|[
name|offset
index|]
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * Like {@link #escapeBytes(ByteString)}, but escapes a text string.    * Non-ASCII characters are first encoded as UTF-8, then each byte is escaped    * individually as a 3-digit octal escape.  Yes, it's weird.    */
specifier|static
name|String
name|escapeText
parameter_list|(
specifier|final
name|String
name|input
parameter_list|)
block|{
return|return
name|escapeBytes
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|input
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Escape double quotes and backslashes in a String for unicode output of a message.    */
specifier|static
name|String
name|escapeDoubleQuotesAndBackslashes
parameter_list|(
specifier|final
name|String
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|replace
argument_list|(
literal|"\\"
argument_list|,
literal|"\\\\"
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\""
argument_list|,
literal|"\\\""
argument_list|)
return|;
block|}
block|}
end_class

end_unit

