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

begin_comment
comment|/**  * The {@link ByteBuffAllocator} won't allocate pooled heap {@link ByteBuff} now; at the same time,  * if allocate an off-heap {@link ByteBuff} from allocator, then it must be a pooled one. That's to  * say, an exclusive memory HFileBlock would must be an heap block and a shared memory HFileBlock  * would must be an off-heap block.  * @see org.apache.hadoop.hbase.io.hfile.ExclusiveMemHFileBlock  **/
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SharedMemHFileBlock
extends|extends
name|HFileBlock
block|{
name|SharedMemHFileBlock
parameter_list|(
name|BlockType
name|blockType
parameter_list|,
name|int
name|onDiskSizeWithoutHeader
parameter_list|,
name|int
name|uncompressedSizeWithoutHeader
parameter_list|,
name|long
name|prevBlockOffset
parameter_list|,
name|ByteBuff
name|buf
parameter_list|,
name|boolean
name|fillHeader
parameter_list|,
name|long
name|offset
parameter_list|,
name|int
name|nextBlockOnDiskSize
parameter_list|,
name|int
name|onDiskDataSizeWithHeader
parameter_list|,
name|HFileContext
name|fileContext
parameter_list|,
name|ByteBuffAllocator
name|alloc
parameter_list|)
block|{
name|super
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
name|alloc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSharedMem
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

