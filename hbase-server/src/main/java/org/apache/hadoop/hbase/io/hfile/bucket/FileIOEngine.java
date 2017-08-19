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
name|File
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
comment|/**  * IO engine that stores data to a file on the local file system.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FileIOEngine
implements|implements
name|IOEngine
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FileIOEngine
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILE_DELIMITER
init|=
literal|","
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|filePaths
decl_stmt|;
specifier|private
specifier|final
name|FileChannel
index|[]
name|fileChannels
decl_stmt|;
specifier|private
specifier|final
name|RandomAccessFile
index|[]
name|rafs
decl_stmt|;
specifier|private
specifier|final
name|long
name|sizePerFile
decl_stmt|;
specifier|private
specifier|final
name|long
name|capacity
decl_stmt|;
specifier|private
name|FileReadAccessor
name|readAccessor
init|=
operator|new
name|FileReadAccessor
argument_list|()
decl_stmt|;
specifier|private
name|FileWriteAccessor
name|writeAccessor
init|=
operator|new
name|FileWriteAccessor
argument_list|()
decl_stmt|;
specifier|public
name|FileIOEngine
parameter_list|(
name|long
name|capacity
parameter_list|,
name|boolean
name|maintainPersistence
parameter_list|,
name|String
modifier|...
name|filePaths
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|sizePerFile
operator|=
name|capacity
operator|/
name|filePaths
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|capacity
operator|=
name|this
operator|.
name|sizePerFile
operator|*
name|filePaths
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|filePaths
operator|=
name|filePaths
expr_stmt|;
name|this
operator|.
name|fileChannels
operator|=
operator|new
name|FileChannel
index|[
name|filePaths
operator|.
name|length
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|maintainPersistence
condition|)
block|{
for|for
control|(
name|String
name|filePath
range|:
name|filePaths
control|)
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"File "
operator|+
name|filePath
operator|+
literal|" already exists. Deleting!!"
argument_list|)
expr_stmt|;
block|}
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
comment|// If deletion fails still we can manage with the writes
block|}
block|}
block|}
name|this
operator|.
name|rafs
operator|=
operator|new
name|RandomAccessFile
index|[
name|filePaths
operator|.
name|length
index|]
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
name|filePaths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|filePath
init|=
name|filePaths
index|[
name|i
index|]
decl_stmt|;
try|try
block|{
name|rafs
index|[
name|i
index|]
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|filePath
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
name|long
name|totalSpace
init|=
operator|new
name|File
argument_list|(
name|filePath
argument_list|)
operator|.
name|getTotalSpace
argument_list|()
decl_stmt|;
if|if
condition|(
name|totalSpace
operator|<
name|sizePerFile
condition|)
block|{
comment|// The next setting length will throw exception,logging this message
comment|// is just used for the detail reason of exception，
name|String
name|msg
init|=
literal|"Only "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|totalSpace
argument_list|)
operator|+
literal|" total space under "
operator|+
name|filePath
operator|+
literal|", not enough for requested "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|sizePerFile
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
name|rafs
index|[
name|i
index|]
operator|.
name|setLength
argument_list|(
name|sizePerFile
argument_list|)
expr_stmt|;
name|fileChannels
index|[
name|i
index|]
operator|=
name|rafs
index|[
name|i
index|]
operator|.
name|getChannel
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Allocating cache "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|sizePerFile
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
name|IOException
name|fex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed allocating cache on "
operator|+
name|filePath
argument_list|,
name|fex
argument_list|)
expr_stmt|;
name|shutdown
argument_list|()
expr_stmt|;
throw|throw
name|fex
throw|;
block|}
block|}
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
literal|", paths="
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|filePaths
argument_list|)
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
comment|/**    * Transfers data from file to the given byte buffer    * @param offset The offset in the file where the first byte to be read    * @param length The length of buffer that should be allocated for reading    *               from the file channel    * @return number of bytes read    * @throws IOException    */
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
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|length
operator|>=
literal|0
argument_list|,
literal|"Length of read can not be less than 0."
argument_list|)
expr_stmt|;
name|ByteBuffer
name|dstBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|length
operator|!=
literal|0
condition|)
block|{
name|accessFile
argument_list|(
name|readAccessor
argument_list|,
name|dstBuffer
argument_list|,
name|offset
argument_list|)
expr_stmt|;
comment|// The buffer created out of the fileChannel is formed by copying the data from the file
comment|// Hence in this case there is no shared memory that we point to. Even if the BucketCache evicts
comment|// this buffer from the file the data is already copied and there is no need to ensure that
comment|// the results are not corrupted before consuming them.
if|if
condition|(
name|dstBuffer
operator|.
name|limit
argument_list|()
operator|!=
name|length
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Only "
operator|+
name|dstBuffer
operator|.
name|limit
argument_list|()
operator|+
literal|" bytes read, "
operator|+
name|length
operator|+
literal|" expected"
argument_list|)
throw|;
block|}
block|}
return|return
name|deserializer
operator|.
name|deserialize
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|dstBuffer
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
if|if
condition|(
operator|!
name|srcBuffer
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
return|return;
block|}
name|accessFile
argument_list|(
name|writeAccessor
argument_list|,
name|srcBuffer
argument_list|,
name|offset
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fileChannels
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
if|if
condition|(
name|fileChannels
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|fileChannels
index|[
name|i
index|]
operator|.
name|force
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed syncing data to "
operator|+
name|this
operator|.
name|filePaths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
throw|throw
name|ie
throw|;
block|}
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filePaths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
if|if
condition|(
name|fileChannels
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|fileChannels
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|rafs
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|rafs
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
literal|"Failed closing "
operator|+
name|filePaths
index|[
name|i
index|]
operator|+
literal|" when shudown the IOEngine"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
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
comment|// When caching block into BucketCache there will be single buffer backing for this HFileBlock.
assert|assert
name|srcBuffer
operator|.
name|hasArray
argument_list|()
assert|;
name|write
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|srcBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|srcBuffer
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|srcBuffer
operator|.
name|remaining
argument_list|()
argument_list|)
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|accessFile
parameter_list|(
name|FileAccessor
name|accessor
parameter_list|,
name|ByteBuffer
name|buffer
parameter_list|,
name|long
name|globalOffset
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|startFileNum
init|=
name|getFileNum
argument_list|(
name|globalOffset
argument_list|)
decl_stmt|;
name|int
name|remainingAccessDataLen
init|=
name|buffer
operator|.
name|remaining
argument_list|()
decl_stmt|;
name|int
name|endFileNum
init|=
name|getFileNum
argument_list|(
name|globalOffset
operator|+
name|remainingAccessDataLen
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|accessFileNum
init|=
name|startFileNum
decl_stmt|;
name|long
name|accessOffset
init|=
name|getAbsoluteOffsetInFile
argument_list|(
name|accessFileNum
argument_list|,
name|globalOffset
argument_list|)
decl_stmt|;
name|int
name|bufLimit
init|=
name|buffer
operator|.
name|limit
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|FileChannel
name|fileChannel
init|=
name|fileChannels
index|[
name|accessFileNum
index|]
decl_stmt|;
if|if
condition|(
name|endFileNum
operator|>
name|accessFileNum
condition|)
block|{
comment|// short the limit;
name|buffer
operator|.
name|limit
argument_list|(
call|(
name|int
call|)
argument_list|(
name|buffer
operator|.
name|limit
argument_list|()
operator|-
name|remainingAccessDataLen
operator|+
name|sizePerFile
operator|-
name|accessOffset
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|accessLen
init|=
name|accessor
operator|.
name|access
argument_list|(
name|fileChannel
argument_list|,
name|buffer
argument_list|,
name|accessOffset
argument_list|)
decl_stmt|;
comment|// recover the limit
name|buffer
operator|.
name|limit
argument_list|(
name|bufLimit
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessLen
operator|<
name|remainingAccessDataLen
condition|)
block|{
name|remainingAccessDataLen
operator|-=
name|accessLen
expr_stmt|;
name|accessFileNum
operator|++
expr_stmt|;
name|accessOffset
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
if|if
condition|(
name|accessFileNum
operator|>=
name|fileChannels
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Required data len "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
operator|+
literal|" exceed the engine's capacity "
operator|+
name|StringUtils
operator|.
name|byteDesc
argument_list|(
name|capacity
argument_list|)
operator|+
literal|" where offset="
operator|+
name|globalOffset
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Get the absolute offset in given file with the relative global offset.    * @param fileNum    * @param globalOffset    * @return the absolute offset    */
specifier|private
name|long
name|getAbsoluteOffsetInFile
parameter_list|(
name|int
name|fileNum
parameter_list|,
name|long
name|globalOffset
parameter_list|)
block|{
return|return
name|globalOffset
operator|-
name|fileNum
operator|*
name|sizePerFile
return|;
block|}
specifier|private
name|int
name|getFileNum
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unexpected offset "
operator|+
name|offset
argument_list|)
throw|;
block|}
name|int
name|fileNum
init|=
call|(
name|int
call|)
argument_list|(
name|offset
operator|/
name|sizePerFile
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileNum
operator|>=
name|fileChannels
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not expected offset "
operator|+
name|offset
operator|+
literal|" where capacity="
operator|+
name|capacity
argument_list|)
throw|;
block|}
return|return
name|fileNum
return|;
block|}
specifier|private
specifier|static
interface|interface
name|FileAccessor
block|{
name|int
name|access
parameter_list|(
name|FileChannel
name|fileChannel
parameter_list|,
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|long
name|accessOffset
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
specifier|static
class|class
name|FileReadAccessor
implements|implements
name|FileAccessor
block|{
annotation|@
name|Override
specifier|public
name|int
name|access
parameter_list|(
name|FileChannel
name|fileChannel
parameter_list|,
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|long
name|accessOffset
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fileChannel
operator|.
name|read
argument_list|(
name|byteBuffer
argument_list|,
name|accessOffset
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FileWriteAccessor
implements|implements
name|FileAccessor
block|{
annotation|@
name|Override
specifier|public
name|int
name|access
parameter_list|(
name|FileChannel
name|fileChannel
parameter_list|,
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|long
name|accessOffset
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fileChannel
operator|.
name|write
argument_list|(
name|byteBuffer
argument_list|,
name|accessOffset
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

