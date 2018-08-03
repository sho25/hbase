begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|wal
operator|.
name|FSHLog
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
name|ProtobufLogWriter
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
name|FSUtils
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

begin_comment
comment|/**  * A WAL provider that use {@link FSHLog}.  */
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
name|FSHLogProvider
extends|extends
name|AbstractFSWALProvider
argument_list|<
name|FSHLog
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
name|FSHLogProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Only public so classes back in regionserver.wal can access
specifier|public
interface|interface
name|Writer
extends|extends
name|WALProvider
operator|.
name|Writer
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
comment|/**    * Public because of FSHLog. Should be package-private    * @param overwritable if the created writer can overwrite. For recovered edits, it is true and    *          for WAL it is false. Thus we can distinguish WAL and recovered edits by this.    */
specifier|public
specifier|static
name|Writer
name|createWriter
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|boolean
name|overwritable
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createWriter
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
argument_list|,
name|overwritable
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Public because of FSHLog. Should be package-private    */
specifier|public
specifier|static
name|Writer
name|createWriter
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|boolean
name|overwritable
parameter_list|,
name|long
name|blocksize
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Configuration already does caching for the Class lookup.
name|Class
argument_list|<
name|?
extends|extends
name|Writer
argument_list|>
name|logWriterClass
init|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.regionserver.hlog.writer.impl"
argument_list|,
name|ProtobufLogWriter
operator|.
name|class
argument_list|,
name|Writer
operator|.
name|class
argument_list|)
decl_stmt|;
name|Writer
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|writer
operator|=
name|logWriterClass
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|FileSystem
name|rootFs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|rootFs
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
literal|"The RegionServer write ahead log provider for FileSystem implementations "
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
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ee
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"cannot close log writer"
argument_list|,
name|ee
argument_list|)
expr_stmt|;
block|}
block|}
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
annotation|@
name|Override
specifier|protected
name|FSHLog
name|createWAL
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
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
block|{   }
block|}
end_class

end_unit

