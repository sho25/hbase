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
name|regionserver
operator|.
name|wal
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
name|util
operator|.
name|List
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
name|conf
operator|.
name|Configuration
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
name|wal
operator|.
name|WALProvider
operator|.
name|AsyncWriter
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
name|hbase
operator|.
name|thirdparty
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
name|hbase
operator|.
name|thirdparty
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
comment|/**  * An AsyncFSWAL which writes data to two filesystems.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DualAsyncFSWAL
extends|extends
name|AsyncFSWAL
block|{
specifier|private
specifier|final
name|FileSystem
name|remoteFs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|remoteWalDir
decl_stmt|;
specifier|public
name|DualAsyncFSWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileSystem
name|remoteFs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|Path
name|remoteRootDir
parameter_list|,
name|String
name|logDir
parameter_list|,
name|String
name|archiveDir
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|boolean
name|failIfWALExists
parameter_list|,
name|String
name|prefix
parameter_list|,
name|String
name|suffix
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
name|FailedLogCloseException
throws|,
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|logDir
argument_list|,
name|archiveDir
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|failIfWALExists
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|,
name|eventLoopGroup
argument_list|,
name|channelClass
argument_list|)
expr_stmt|;
name|this
operator|.
name|remoteFs
operator|=
name|remoteFs
expr_stmt|;
name|this
operator|.
name|remoteWalDir
operator|=
operator|new
name|Path
argument_list|(
name|remoteRootDir
argument_list|,
name|logDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|AsyncWriter
name|createWriterInstance
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|AsyncWriter
name|localWriter
init|=
name|super
operator|.
name|createWriterInstance
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|AsyncWriter
name|remoteWriter
decl_stmt|;
name|boolean
name|succ
init|=
literal|false
decl_stmt|;
try|try
block|{
name|remoteWriter
operator|=
name|createAsyncWriter
argument_list|(
name|remoteFs
argument_list|,
operator|new
name|Path
argument_list|(
name|remoteWalDir
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|succ
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|succ
condition|)
block|{
name|closeWriter
argument_list|(
name|localWriter
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|CombinedAsyncWriter
operator|.
name|create
argument_list|(
name|CombinedAsyncWriter
operator|.
name|Mode
operator|.
name|SEQUENTIAL
argument_list|,
name|remoteWriter
argument_list|,
name|localWriter
argument_list|)
return|;
block|}
block|}
end_class

end_unit

