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
operator|.
name|Internal
operator|.
name|ProtobufList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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

begin_comment
comment|/**  * Implements {@link ProtobufList} for non-primitive and {@link String} types.  */
end_comment

begin_class
specifier|final
class|class
name|ProtobufArrayList
parameter_list|<
name|E
parameter_list|>
extends|extends
name|AbstractProtobufList
argument_list|<
name|E
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|ProtobufArrayList
argument_list|<
name|Object
argument_list|>
name|EMPTY_LIST
init|=
operator|new
name|ProtobufArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|EMPTY_LIST
operator|.
name|makeImmutable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// Guaranteed safe by runtime.
specifier|public
specifier|static
parameter_list|<
name|E
parameter_list|>
name|ProtobufArrayList
argument_list|<
name|E
argument_list|>
name|emptyList
parameter_list|()
block|{
return|return
operator|(
name|ProtobufArrayList
argument_list|<
name|E
argument_list|>
operator|)
name|EMPTY_LIST
return|;
block|}
specifier|private
specifier|final
name|List
argument_list|<
name|E
argument_list|>
name|list
decl_stmt|;
name|ProtobufArrayList
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|E
argument_list|>
argument_list|(
name|DEFAULT_CAPACITY
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ProtobufArrayList
parameter_list|(
name|List
argument_list|<
name|E
argument_list|>
name|list
parameter_list|)
block|{
name|this
operator|.
name|list
operator|=
name|list
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ProtobufArrayList
argument_list|<
name|E
argument_list|>
name|mutableCopyWithCapacity
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
if|if
condition|(
name|capacity
operator|<
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
name|List
argument_list|<
name|E
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<
name|E
argument_list|>
argument_list|(
name|capacity
argument_list|)
decl_stmt|;
name|newList
operator|.
name|addAll
argument_list|(
name|list
argument_list|)
expr_stmt|;
return|return
operator|new
name|ProtobufArrayList
argument_list|<
name|E
argument_list|>
argument_list|(
name|newList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|int
name|index
parameter_list|,
name|E
name|element
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
expr_stmt|;
name|modCount
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|remove
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
name|E
name|toReturn
init|=
name|list
operator|.
name|remove
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|modCount
operator|++
expr_stmt|;
return|return
name|toReturn
return|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|E
name|element
parameter_list|)
block|{
name|ensureIsMutable
argument_list|()
expr_stmt|;
name|E
name|toReturn
init|=
name|list
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
decl_stmt|;
name|modCount
operator|++
expr_stmt|;
return|return
name|toReturn
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|list
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

