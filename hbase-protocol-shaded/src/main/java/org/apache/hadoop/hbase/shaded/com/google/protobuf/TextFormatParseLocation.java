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
name|Arrays
import|;
end_import

begin_comment
comment|/**  * A location in the source code.  *  *<p>A location is the starting line number and starting column number.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|TextFormatParseLocation
block|{
comment|/**    * The empty location.    */
specifier|public
specifier|static
specifier|final
name|TextFormatParseLocation
name|EMPTY
init|=
operator|new
name|TextFormatParseLocation
argument_list|(
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|/**    * Create a location.    *    * @param line the starting line number    * @param column the starting column number    * @return a {@code ParseLocation}    */
specifier|static
name|TextFormatParseLocation
name|create
parameter_list|(
name|int
name|line
parameter_list|,
name|int
name|column
parameter_list|)
block|{
if|if
condition|(
name|line
operator|==
operator|-
literal|1
operator|&&
name|column
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|EMPTY
return|;
block|}
if|if
condition|(
name|line
operator|<
literal|0
operator|||
name|column
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"line and column values must be>= 0: line %d, column: %d"
argument_list|,
name|line
argument_list|,
name|column
argument_list|)
argument_list|)
throw|;
block|}
return|return
operator|new
name|TextFormatParseLocation
argument_list|(
name|line
argument_list|,
name|column
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|int
name|line
decl_stmt|;
specifier|private
specifier|final
name|int
name|column
decl_stmt|;
specifier|private
name|TextFormatParseLocation
parameter_list|(
name|int
name|line
parameter_list|,
name|int
name|column
parameter_list|)
block|{
name|this
operator|.
name|line
operator|=
name|line
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
specifier|public
name|int
name|getLine
parameter_list|()
block|{
return|return
name|line
return|;
block|}
specifier|public
name|int
name|getColumn
parameter_list|()
block|{
return|return
name|column
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"ParseLocation{line=%d, column=%d}"
argument_list|,
name|line
argument_list|,
name|column
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|TextFormatParseLocation
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TextFormatParseLocation
name|that
init|=
operator|(
name|TextFormatParseLocation
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|this
operator|.
name|line
operator|==
name|that
operator|.
name|getLine
argument_list|()
operator|)
operator|&&
operator|(
name|this
operator|.
name|column
operator|==
name|that
operator|.
name|getColumn
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
index|[]
name|values
init|=
block|{
name|line
block|,
name|column
block|}
decl_stmt|;
return|return
name|Arrays
operator|.
name|hashCode
argument_list|(
name|values
argument_list|)
return|;
block|}
block|}
end_class

end_unit

