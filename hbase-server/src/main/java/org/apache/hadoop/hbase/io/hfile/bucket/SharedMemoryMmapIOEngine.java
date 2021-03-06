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
operator|.
name|bucket
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

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|hfile
operator|.
name|Cacheable
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
comment|/**  * IO engine that stores data in pmem devices such as DCPMM. This engine also mmaps the file from  * the given path. But note that this path has to be a path on the pmem device so that when mmapped  * the file's address is mapped to the Pmem's address space and not in the DRAM. Since this address  * space is exclusive for the Pmem device there is no swapping out of the mmapped contents that  * generally happens when DRAM's free space is not enough to hold the specified file's mmapped  * contents. This gives us the option of using the {@code MemoryType#SHARED} type when serving the  * data from this pmem address space. We need not copy the blocks to the onheap space as we need to  * do for the case of {@code ExclusiveMemoryMmapIOEngine}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SharedMemoryMmapIOEngine
extends|extends
name|FileMmapIOEngine
block|{
comment|// TODO this will support only one path over Pmem. To make use of multiple Pmem devices mounted,
comment|// we need to support multiple paths like files IOEngine. Support later.
specifier|public
name|SharedMemoryMmapIOEngine
parameter_list|(
name|String
name|filePath
parameter_list|,
name|long
name|capacity
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|filePath
argument_list|,
name|capacity
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|usesSharedMemory
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|read
parameter_list|(
name|BucketEntry
name|be
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
index|[]
name|buffers
init|=
name|bufferArray
operator|.
name|asSubByteBuffers
argument_list|(
name|be
operator|.
name|offset
argument_list|()
argument_list|,
name|be
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
comment|// Here the buffer that is created directly refers to the buffer in the actual buckets.
comment|// When any cell is referring to the blocks created out of these buckets then it means that
comment|// those cells are referring to a shared memory area which if evicted by the BucketCache would
comment|// lead to corruption of results. The readers using this block are aware of this fact and do
comment|// the necessary action to prevent eviction till the results are either consumed or copied
return|return
name|be
operator|.
name|wrapAsCacheable
argument_list|(
name|buffers
argument_list|)
return|;
block|}
block|}
end_class

end_unit

