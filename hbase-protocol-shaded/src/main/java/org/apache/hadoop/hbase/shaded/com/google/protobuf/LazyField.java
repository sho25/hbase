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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_comment
comment|/**  * LazyField encapsulates the logic of lazily parsing message fields. It stores  * the message in a ByteString initially and then parse it on-demand.  *  * Most of key methods are implemented in {@link LazyFieldLite} but this class  * can contain default instance of the message to provide {@code hashCode()},  * {@code euqals()} and {@code toString()}.  *  * @author xiangl@google.com (Xiang Li)  */
end_comment

begin_class
specifier|public
class|class
name|LazyField
extends|extends
name|LazyFieldLite
block|{
comment|/**    * Carry a message's default instance which is used by {@code hashCode()}, {@code euqals()} and    * {@code toString()}.    */
specifier|private
specifier|final
name|MessageLite
name|defaultInstance
decl_stmt|;
specifier|public
name|LazyField
parameter_list|(
name|MessageLite
name|defaultInstance
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|,
name|ByteString
name|bytes
parameter_list|)
block|{
name|super
argument_list|(
name|extensionRegistry
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultInstance
operator|=
name|defaultInstance
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsDefaultInstance
parameter_list|()
block|{
return|return
name|super
operator|.
name|containsDefaultInstance
argument_list|()
operator|||
name|value
operator|==
name|defaultInstance
return|;
block|}
specifier|public
name|MessageLite
name|getValue
parameter_list|()
block|{
return|return
name|getValue
argument_list|(
name|defaultInstance
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|getValue
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|getValue
argument_list|()
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
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
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// ====================================================
comment|/**    * LazyEntry and LazyIterator are used to encapsulate the LazyField, when    * users iterate all fields from FieldSet.    */
specifier|static
class|class
name|LazyEntry
parameter_list|<
name|K
parameter_list|>
implements|implements
name|Entry
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
block|{
specifier|private
name|Entry
argument_list|<
name|K
argument_list|,
name|LazyField
argument_list|>
name|entry
decl_stmt|;
specifier|private
name|LazyEntry
parameter_list|(
name|Entry
argument_list|<
name|K
argument_list|,
name|LazyField
argument_list|>
name|entry
parameter_list|)
block|{
name|this
operator|.
name|entry
operator|=
name|entry
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|K
name|getKey
parameter_list|()
block|{
return|return
name|entry
operator|.
name|getKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getValue
parameter_list|()
block|{
name|LazyField
name|field
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|field
operator|.
name|getValue
argument_list|()
return|;
block|}
specifier|public
name|LazyField
name|getField
parameter_list|()
block|{
return|return
name|entry
operator|.
name|getValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|setValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|value
operator|instanceof
name|MessageLite
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"LazyField now only used for MessageSet, "
operator|+
literal|"and the value of MessageSet must be an instance of MessageLite"
argument_list|)
throw|;
block|}
return|return
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|setValue
argument_list|(
operator|(
name|MessageLite
operator|)
name|value
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|LazyIterator
parameter_list|<
name|K
parameter_list|>
implements|implements
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
argument_list|>
block|{
specifier|private
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
decl_stmt|;
specifier|public
name|LazyIterator
parameter_list|(
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
parameter_list|)
block|{
name|this
operator|.
name|iterator
operator|=
name|iterator
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Entry
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
name|next
parameter_list|()
block|{
name|Entry
argument_list|<
name|K
argument_list|,
name|?
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|LazyField
condition|)
block|{
return|return
operator|new
name|LazyEntry
argument_list|<
name|K
argument_list|>
argument_list|(
operator|(
name|Entry
argument_list|<
name|K
argument_list|,
name|LazyField
argument_list|>
operator|)
name|entry
argument_list|)
return|;
block|}
return|return
operator|(
name|Entry
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
operator|)
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

