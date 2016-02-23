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
name|io
operator|.
name|RandomAccessFile
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
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
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
name|io
operator|.
name|hfile
operator|.
name|Cacheable
operator|.
name|MemoryType
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
name|CacheableDeserializer
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
name|nio
operator|.
name|SingleByteBuff
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * IO engine that stores data to a file on the local file system using memory mapping  * mechanism  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FileMmapEngine
implements|implements
name|IOEngine
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FileMmapEngine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
specifier|private
name|long
name|size
decl_stmt|;
specifier|private
name|ByteBufferArray
name|bufferArray
decl_stmt|;
specifier|private
specifier|final
name|FileChannel
name|fileChannel
decl_stmt|;
specifier|private
name|RandomAccessFile
name|raf
init|=
literal|null
decl_stmt|;
specifier|public
name|FileMmapEngine
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
name|this
operator|.
name|path
operator|=
name|filePath
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|capacity
expr_stmt|;
name|long
name|fileSize
init|=
literal|0
decl_stmt|;
try|try
block|{
name|raf
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|filePath
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
name|fileSize
operator|=
name|roundUp
argument_list|(
name|capacity
argument_list|,
name|ByteBufferArray
operator|.
name|DEFAULT_BUFFER_SIZE
argument_list|)
expr_stmt|;
name|raf
operator|.
name|setLength
argument_list|(
name|fileSize
argument_list|)
expr_stmt|;
name|fileChannel
operator|=
name|raf
operator|.
name|getChannel
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Allocating "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|fileSize
argument_list|)
operator|+
literal|", on the path:"
operator|+
name|filePath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
name|fex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't create bucket cache file "
operator|+
name|filePath
argument_list|,
name|fex
argument_list|)
expr_stmt|;
throw|throw
name|fex
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't extend bucket cache file; insufficient space for "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|fileSize
argument_list|)
argument_list|,
name|ioex
argument_list|)
expr_stmt|;
name|shutdown
argument_list|()
expr_stmt|;
throw|throw
name|ioex
throw|;
block|}
name|ByteBufferAllocator
name|allocator
init|=
operator|new
name|ByteBufferAllocator
argument_list|()
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|allocate
parameter_list|(
name|long
name|size
parameter_list|,
name|boolean
name|directByteBuffer
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|buffer
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|directByteBuffer
condition|)
block|{
name|buffer
operator|=
name|fileChannel
operator|.
name|map
argument_list|(
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
operator|.
name|MapMode
operator|.
name|READ_WRITE
argument_list|,
name|pos
operator|*
name|size
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only Direct Bytebuffers allowed with FileMMap engine"
argument_list|)
throw|;
block|}
name|pos
operator|++
expr_stmt|;
return|return
name|buffer
return|;
block|}
block|}
decl_stmt|;
name|bufferArray
operator|=
operator|new
name|ByteBufferArray
argument_list|(
name|fileSize
argument_list|,
literal|true
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|roundUp
parameter_list|(
name|long
name|n
parameter_list|,
name|long
name|to
parameter_list|)
block|{
return|return
operator|(
operator|(
name|n
operator|+
name|to
operator|-
literal|1
operator|)
operator|/
name|to
operator|)
operator|*
name|to
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
literal|", path="
operator|+
name|this
operator|.
name|path
operator|+
literal|", size="
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%,d"
argument_list|,
name|this
operator|.
name|size
argument_list|)
return|;
block|}
comment|/**    * File IO engine is always able to support persistent storage for the cache    * @return true    */
annotation|@
name|Override
specifier|public
name|boolean
name|isPersistent
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
name|long
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|CacheableDeserializer
argument_list|<
name|Cacheable
argument_list|>
name|deserializer
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|dst
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|bufferArray
operator|.
name|getMultiple
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|dst
argument_list|)
expr_stmt|;
return|return
name|deserializer
operator|.
name|deserialize
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|dst
argument_list|)
argument_list|)
argument_list|,
literal|true
argument_list|,
name|MemoryType
operator|.
name|EXCLUSIVE
argument_list|)
return|;
block|}
comment|/**    * Transfers data from the given byte buffer to file    * @param srcBuffer the given byte buffer from which bytes are to be read    * @param offset The offset in the file where the first byte to be written    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuffer
name|srcBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|srcBuffer
operator|.
name|hasArray
argument_list|()
assert|;
name|bufferArray
operator|.
name|putMultiple
argument_list|(
name|offset
argument_list|,
name|srcBuffer
operator|.
name|remaining
argument_list|()
argument_list|,
name|srcBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|srcBuffer
operator|.
name|arrayOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|ByteBuff
name|srcBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
comment|// This singleByteBuff can be considered to be array backed
assert|assert
name|srcBuffer
operator|.
name|hasArray
argument_list|()
assert|;
name|bufferArray
operator|.
name|putMultiple
argument_list|(
name|offset
argument_list|,
name|srcBuffer
operator|.
name|remaining
argument_list|()
argument_list|,
name|srcBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|srcBuffer
operator|.
name|arrayOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sync the data to file after writing    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|fileChannel
operator|!=
literal|null
condition|)
block|{
name|fileChannel
operator|.
name|force
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Close the file    */
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
try|try
block|{
name|fileChannel
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't shutdown cleanly"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|raf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't shutdown cleanly"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

