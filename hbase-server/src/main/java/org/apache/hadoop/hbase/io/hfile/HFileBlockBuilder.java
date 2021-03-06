begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import static
name|javax
operator|.
name|swing
operator|.
name|Spring
operator|.
name|UNSET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|ByteBuffAllocator
operator|.
name|HEAP
import|;
end_import

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
name|io
operator|.
name|ByteBuffAllocator
import|;
end_import

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
name|nio
operator|.
name|ByteBuff
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileBlockBuilder
block|{
specifier|private
name|BlockType
name|blockType
decl_stmt|;
specifier|private
name|int
name|onDiskSizeWithoutHeader
decl_stmt|;
specifier|private
name|int
name|onDiskDataSizeWithHeader
decl_stmt|;
specifier|private
name|int
name|uncompressedSizeWithoutHeader
decl_stmt|;
specifier|private
name|long
name|prevBlockOffset
decl_stmt|;
specifier|private
name|ByteBuff
name|buf
decl_stmt|;
specifier|private
name|boolean
name|fillHeader
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|offset
init|=
name|UNSET
decl_stmt|;
specifier|private
name|int
name|nextBlockOnDiskSize
init|=
name|UNSET
decl_stmt|;
specifier|private
name|HFileContext
name|fileContext
decl_stmt|;
specifier|private
name|ByteBuffAllocator
name|allocator
init|=
name|HEAP
decl_stmt|;
specifier|private
name|boolean
name|isShared
decl_stmt|;
specifier|public
name|HFileBlockBuilder
name|withBlockType
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
name|this
operator|.
name|blockType
operator|=
name|blockType
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withOnDiskSizeWithoutHeader
parameter_list|(
name|int
name|onDiskSizeWithoutHeader
parameter_list|)
block|{
name|this
operator|.
name|onDiskSizeWithoutHeader
operator|=
name|onDiskSizeWithoutHeader
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withOnDiskDataSizeWithHeader
parameter_list|(
name|int
name|onDiskDataSizeWithHeader
parameter_list|)
block|{
name|this
operator|.
name|onDiskDataSizeWithHeader
operator|=
name|onDiskDataSizeWithHeader
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withUncompressedSizeWithoutHeader
parameter_list|(
name|int
name|uncompressedSizeWithoutHeader
parameter_list|)
block|{
name|this
operator|.
name|uncompressedSizeWithoutHeader
operator|=
name|uncompressedSizeWithoutHeader
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withPrevBlockOffset
parameter_list|(
name|long
name|prevBlockOffset
parameter_list|)
block|{
name|this
operator|.
name|prevBlockOffset
operator|=
name|prevBlockOffset
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withByteBuff
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withFillHeader
parameter_list|(
name|boolean
name|fillHeader
parameter_list|)
block|{
name|this
operator|.
name|fillHeader
operator|=
name|fillHeader
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withOffset
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withNextBlockOnDiskSize
parameter_list|(
name|int
name|nextBlockOnDiskSize
parameter_list|)
block|{
name|this
operator|.
name|nextBlockOnDiskSize
operator|=
name|nextBlockOnDiskSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withHFileContext
parameter_list|(
name|HFileContext
name|fileContext
parameter_list|)
block|{
name|this
operator|.
name|fileContext
operator|=
name|fileContext
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withByteBuffAllocator
parameter_list|(
name|ByteBuffAllocator
name|allocator
parameter_list|)
block|{
name|this
operator|.
name|allocator
operator|=
name|allocator
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlockBuilder
name|withShared
parameter_list|(
name|boolean
name|isShared
parameter_list|)
block|{
name|this
operator|.
name|isShared
operator|=
name|isShared
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HFileBlock
name|build
parameter_list|()
block|{
if|if
condition|(
name|isShared
condition|)
block|{
return|return
operator|new
name|SharedMemHFileBlock
argument_list|(
name|blockType
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|,
name|uncompressedSizeWithoutHeader
argument_list|,
name|prevBlockOffset
argument_list|,
name|buf
argument_list|,
name|fillHeader
argument_list|,
name|offset
argument_list|,
name|nextBlockOnDiskSize
argument_list|,
name|onDiskDataSizeWithHeader
argument_list|,
name|fileContext
argument_list|,
name|allocator
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ExclusiveMemHFileBlock
argument_list|(
name|blockType
argument_list|,
name|onDiskSizeWithoutHeader
argument_list|,
name|uncompressedSizeWithoutHeader
argument_list|,
name|prevBlockOffset
argument_list|,
name|buf
argument_list|,
name|fillHeader
argument_list|,
name|offset
argument_list|,
name|nextBlockOnDiskSize
argument_list|,
name|onDiskDataSizeWithHeader
argument_list|,
name|fileContext
argument_list|,
name|allocator
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

