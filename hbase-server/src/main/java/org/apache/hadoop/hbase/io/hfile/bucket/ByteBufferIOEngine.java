begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ByteBufferAllocator
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
name|util
operator|.
name|ByteBufferArray
import|;
end_import

begin_comment
comment|/**  * IO engine that stores data in memory using an array of ByteBuffers {@link ByteBufferArray}.  *<p>  *<h2>How it Works</h2> First, see {@link ByteBufferArray} and how it gives a view across multiple  * ByteBuffers managed by it internally. This class does the physical BB create and the write and  * read to the underlying BBs. So we will create N BBs based on the total BC capacity specified on  * create of the ByteBufferArray. So say we have 10 GB of off heap BucketCache, we will create 2560  * such BBs inside our ByteBufferArray.<br>  *<p>  * Now the way BucketCache works is that the entire 10 GB is split into diff sized buckets: by  * default from 5 KB to 513 KB. Within each bucket of a particular size, there are usually more than  * one bucket 'block'. The way it is calculate in bucketcache is that the total bucketcache size is  * divided by 4 (hard-coded currently) * max size option. So using defaults, buckets will be is 4 *  * 513kb (the biggest default value) = 2052kb. A bucket of 2052kb at offset zero will serve out  * bucket 'blocks' of 5kb, the next bucket will do the next size up and so on up to the maximum  * (default) of 513kb).<br>  *<p>  * When we write blocks to the bucketcache, we will see which bucket size group it best fits. So a 4  * KB block size goes to the 5 KB size group. Each of the block writes, writes within its  * appropriate bucket. Though the bucket is '4kb' in size, it will occupy one of the 5 KB bucket  * 'blocks' (even if actual size of the bucket is less). Bucket 'blocks' will not span buckets.<br>  *<p>  * But you can see the physical memory under the bucket 'blocks' can be split across the underlying  * backing BBs from ByteBufferArray. All is split into 4 MB sized BBs.<br>  *<p>  * Each Bucket knows its offset in the entire space of BC and when block is written the offset  * arrives at ByteBufferArray and it figures which BB to write to. It may so happen that the entire  * block to be written does not fit a particular backing ByteBufferArray so the remainder goes to  * another BB. See {@link ByteBufferArray#write(long, ByteBuff)}.<br>  * So said all these, when we read a block it may be possible that the bytes of that blocks is  * physically placed in 2 adjucent BBs. In such case also, we avoid any copy need by having the  * MBB...  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteBufferIOEngine
implements|implements
name|IOEngine
block|{
specifier|private
name|ByteBufferArray
name|bufferArray
decl_stmt|;
specifier|private
specifier|final
name|long
name|capacity
decl_stmt|;
comment|/**    * Construct the ByteBufferIOEngine with the given capacity    * @param capacity    * @throws IOException ideally here no exception to be thrown from the allocator    */
specifier|public
name|ByteBufferIOEngine
parameter_list|(
name|long
name|capacity
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|capacity
operator|=
name|capacity
expr_stmt|;
name|ByteBufferAllocator
name|allocator
init|=
parameter_list|(
name|size
parameter_list|)
lambda|->
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
operator|(
name|int
operator|)
name|size
argument_list|)
decl_stmt|;
name|bufferArray
operator|=
operator|new
name|ByteBufferArray
argument_list|(
name|capacity
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ioengine="
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|", capacity="
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%,d"
argument_list|,
name|this
operator|.
name|capacity
argument_list|)
return|;
block|}
comment|/**    * Memory IO engine is always unable to support persistent storage for the    * cache    * @return false    */
annotation|@
name|Override
specifier|public
name|boolean
name|isPersistent
parameter_list|()
block|{
return|return
literal|false
return|;
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
comment|// lead to corruption of results. The readers using this block are aware of this fact and do the
comment|// necessary action to prevent eviction till the results are either consumed or copied
return|return
name|be
operator|.
name|wrapAsCacheable
argument_list|(
name|buffers
argument_list|)
return|;
block|}
comment|/**    * Transfers data from the given {@link ByteBuffer} to the buffer array. Position of source will    * be advanced by the {@link ByteBuffer#remaining()}.    * @param src the given byte buffer from which bytes are to be read.    * @param offset The offset in the ByteBufferArray of the first byte to be written    * @throws IOException throws IOException if writing to the array throws exception    */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|src
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
name|bufferArray
operator|.
name|write
argument_list|(
name|offset
argument_list|,
name|ByteBuff
operator|.
name|wrap
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Transfers data from the given {@link ByteBuff} to the buffer array. Position of source will be    * advanced by the {@link ByteBuffer#remaining()}.    * @param src the given byte buffer from which bytes are to be read.    * @param offset The offset in the ByteBufferArray of the first byte to be written    * @throws IOException throws IOException if writing to the array throws exception    */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuff
name|src
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
name|bufferArray
operator|.
name|write
argument_list|(
name|offset
argument_list|,
name|src
argument_list|)
expr_stmt|;
block|}
comment|/**    * No operation for the sync in the memory IO engine    */
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
block|{
comment|// Nothing to do.
block|}
comment|/**    * No operation for the shutdown in the memory IO engine    */
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
comment|// Nothing to do.
block|}
block|}
end_class

end_unit

