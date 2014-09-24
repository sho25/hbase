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
specifier|private
specifier|final
name|RandomAccessFile
name|raf
decl_stmt|;
specifier|private
specifier|final
name|FileChannel
name|fileChannel
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
specifier|public
name|FileIOEngine
parameter_list|(
name|String
name|filePath
parameter_list|,
name|long
name|fileSize
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
name|fileSize
expr_stmt|;
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
try|try
block|{
name|raf
operator|.
name|setLength
argument_list|(
name|fileSize
argument_list|)
expr_stmt|;
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
name|raf
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
name|ioex
throw|;
block|}
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
comment|/**    * Transfers data from file to the given byte buffer    * @param dstBuffer the given byte buffer into which bytes are to be written    * @param offset The offset in the file where the first byte to be read    * @return number of bytes read    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|ByteBuffer
name|dstBuffer
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fileChannel
operator|.
name|read
argument_list|(
name|dstBuffer
argument_list|,
name|offset
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
name|fileChannel
operator|.
name|write
argument_list|(
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
name|fileChannel
operator|.
name|force
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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

