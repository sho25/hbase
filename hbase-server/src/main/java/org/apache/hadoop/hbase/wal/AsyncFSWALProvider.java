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
name|io
operator|.
name|asyncfs
operator|.
name|FanOutOneBlockAsyncDFSOutput
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
name|asyncfs
operator|.
name|FanOutOneBlockAsyncDFSOutputHelper
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
name|asyncfs
operator|.
name|FanOutOneBlockAsyncDFSOutputSaslHelper
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
name|regionserver
operator|.
name|wal
operator|.
name|AsyncFSWAL
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
name|regionserver
operator|.
name|wal
operator|.
name|AsyncProtobufLogWriter
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
name|regionserver
operator|.
name|wal
operator|.
name|WALUtil
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
name|hbase
operator|.
name|util
operator|.
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
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
name|Pair
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
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
comment|/**  * A WAL provider that use {@link AsyncFSWAL}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|AsyncFSWALProvider
extends|extends
name|AbstractFSWALProvider
argument_list|<
name|AsyncFSWAL
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AsyncFSWALProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Only public so classes back in regionserver.wal can access
specifier|public
interface|interface
name|AsyncWriter
extends|extends
name|WALProvider
operator|.
name|AsyncWriter
block|{
comment|/**      * @throws IOException if something goes wrong initializing an output stream      * @throws StreamLacksCapabilityException if the given FileSystem can't provide streams that      *         meet the needs of the given Writer implementation.      */
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|c
parameter_list|,
name|boolean
name|overwritable
parameter_list|,
name|long
name|blocksize
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
function_decl|;
block|}
specifier|private
name|EventLoopGroup
name|eventLoopGroup
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|channelClass
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|AsyncFSWAL
name|createWAL
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|AsyncFSWAL
argument_list|(
name|CommonFSUtils
operator|.
name|getWALFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|CommonFSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|getWALDirectoryName
argument_list|(
name|factory
operator|.
name|factoryId
argument_list|)
argument_list|,
name|getWALArchiveDirectoryName
argument_list|(
name|conf
argument_list|,
name|factory
operator|.
name|factoryId
argument_list|)
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
literal|true
argument_list|,
name|logPrefix
argument_list|,
name|META_WAL_PROVIDER_ID
operator|.
name|equals
argument_list|(
name|providerId
argument_list|)
condition|?
name|META_WAL_PROVIDER_ID
else|:
literal|null
argument_list|,
name|eventLoopGroup
argument_list|,
name|channelClass
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
name|eventLoopGroupAndChannelClass
init|=
name|NettyAsyncFSWALConfigHelper
operator|.
name|getEventLoopConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|eventLoopGroup
operator|=
name|eventLoopGroupAndChannelClass
operator|.
name|getFirst
argument_list|()
expr_stmt|;
name|channelClass
operator|=
name|eventLoopGroupAndChannelClass
operator|.
name|getSecond
argument_list|()
expr_stmt|;
block|}
comment|/**    * Public because of AsyncFSWAL. Should be package-private    */
specifier|public
specifier|static
name|AsyncWriter
name|createAsyncWriter
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|overwritable
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
block|{
return|return
name|createAsyncWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|overwritable
argument_list|,
name|WALUtil
operator|.
name|getWALBlockSize
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|)
argument_list|,
name|eventLoopGroup
argument_list|,
name|channelClass
argument_list|)
return|;
block|}
comment|/**    * Public because of AsyncFSWAL. Should be package-private    */
specifier|public
specifier|static
name|AsyncWriter
name|createAsyncWriter
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|overwritable
parameter_list|,
name|long
name|blocksize
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
block|{
comment|// Configuration already does caching for the Class lookup.
name|Class
argument_list|<
name|?
extends|extends
name|AsyncWriter
argument_list|>
name|logWriterClass
init|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.regionserver.hlog.async.writer.impl"
argument_list|,
name|AsyncProtobufLogWriter
operator|.
name|class
argument_list|,
name|AsyncWriter
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|AsyncWriter
name|writer
init|=
name|logWriterClass
operator|.
name|getConstructor
argument_list|(
name|EventLoopGroup
operator|.
name|class
argument_list|,
name|Class
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|eventLoopGroup
argument_list|,
name|channelClass
argument_list|)
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|overwritable
argument_list|,
name|blocksize
argument_list|)
expr_stmt|;
return|return
name|writer
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The RegionServer async write ahead log provider "
operator|+
literal|"relies on the ability to call "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" for proper operation during "
operator|+
literal|"component failures, but the current FileSystem does not support doing so. Please "
operator|+
literal|"check the config value of '"
operator|+
name|CommonFSUtils
operator|.
name|HBASE_WAL_DIR
operator|+
literal|"' and ensure "
operator|+
literal|"it points to a FileSystem mount that has suitable capabilities for output streams."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Error instantiating log writer."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Throwables
operator|.
name|propagateIfPossible
argument_list|(
name|e
argument_list|,
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"cannot get log writer"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Test whether we can load the helper classes for async dfs output.    */
specifier|public
specifier|static
name|boolean
name|load
parameter_list|()
block|{
try|try
block|{
name|Class
operator|.
name|forName
argument_list|(
name|FanOutOneBlockAsyncDFSOutput
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
name|FanOutOneBlockAsyncDFSOutputHelper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
name|FanOutOneBlockAsyncDFSOutputSaslHelper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

