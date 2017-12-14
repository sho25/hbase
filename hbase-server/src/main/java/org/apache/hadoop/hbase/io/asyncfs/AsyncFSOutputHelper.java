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
name|asyncfs
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|CommonConfigurationKeysPublic
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|CommonFSUtils
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|shaded
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
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
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
import|;
end_import

begin_comment
comment|/**  * Helper class for creating AsyncFSOutput.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|AsyncFSOutputHelper
block|{
specifier|private
name|AsyncFSOutputHelper
parameter_list|()
block|{   }
comment|/**    * Create {@link FanOutOneBlockAsyncDFSOutput} for {@link DistributedFileSystem}, and a simple    * implementation for other {@link FileSystem} which wraps around a {@link FSDataOutputStream}.    */
specifier|public
specifier|static
name|AsyncFSOutput
name|createOutput
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|f
parameter_list|,
name|boolean
name|overwrite
parameter_list|,
name|boolean
name|createParent
parameter_list|,
name|short
name|replication
parameter_list|,
name|long
name|blockSize
parameter_list|,
name|EventLoopGroup
name|eventLoopGroup
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|channelClass
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
block|{
if|if
condition|(
name|fs
operator|instanceof
name|DistributedFileSystem
condition|)
block|{
return|return
name|FanOutOneBlockAsyncDFSOutputHelper
operator|.
name|createOutput
argument_list|(
operator|(
name|DistributedFileSystem
operator|)
name|fs
argument_list|,
name|f
argument_list|,
name|overwrite
argument_list|,
name|createParent
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
name|eventLoopGroup
argument_list|,
name|channelClass
argument_list|)
return|;
block|}
specifier|final
name|FSDataOutputStream
name|out
decl_stmt|;
name|int
name|bufferSize
init|=
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
name|CommonConfigurationKeysPublic
operator|.
name|IO_FILE_BUFFER_SIZE_KEY
argument_list|,
name|CommonConfigurationKeysPublic
operator|.
name|IO_FILE_BUFFER_SIZE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|createParent
condition|)
block|{
name|out
operator|=
name|fs
operator|.
name|create
argument_list|(
name|f
argument_list|,
name|overwrite
argument_list|,
name|bufferSize
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|=
name|fs
operator|.
name|createNonRecursive
argument_list|(
name|f
argument_list|,
name|overwrite
argument_list|,
name|bufferSize
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// After we create the stream but before we attempt to use it at all
comment|// ensure that we can provide the level of data safety we're configured
comment|// to provide.
if|if
condition|(
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|CommonFSUtils
operator|.
name|UNSAFE_STREAM_CAPABILITY_ENFORCE
argument_list|,
literal|true
argument_list|)
operator|&&
operator|!
operator|(
name|CommonFSUtils
operator|.
name|hasCapability
argument_list|(
name|out
argument_list|,
literal|"hflush"
argument_list|)
operator|&&
name|CommonFSUtils
operator|.
name|hasCapability
argument_list|(
name|out
argument_list|,
literal|"hsync"
argument_list|)
operator|)
condition|)
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
argument_list|(
literal|"hflush and hsync"
argument_list|)
throw|;
block|}
return|return
operator|new
name|WrapperAsyncFSOutput
argument_list|(
name|f
argument_list|,
name|out
argument_list|)
return|;
block|}
block|}
end_class

end_unit

