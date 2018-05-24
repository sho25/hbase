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
import|import static
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
name|AbstractFSWALProvider
operator|.
name|getWALArchiveDirectoryName
import|;
end_import

begin_import
import|import static
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
name|AbstractFSWALProvider
operator|.
name|getWALDirectoryName
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|Lock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiPredicate
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
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
name|hbase
operator|.
name|TableName
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
name|client
operator|.
name|RegionInfo
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
name|DualAsyncFSWAL
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
name|WALActionsListener
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
name|replication
operator|.
name|ReplicationUtils
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
name|replication
operator|.
name|SyncReplicationState
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
name|replication
operator|.
name|regionserver
operator|.
name|PeerActionListener
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
name|replication
operator|.
name|regionserver
operator|.
name|SyncReplicationPeerInfoProvider
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
name|EnvironmentEdgeManager
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
name|KeyLocker
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
name|annotations
operator|.
name|VisibleForTesting
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Streams
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
comment|/**  * The special {@link WALProvider} for synchronous replication.  *<p>  * It works like an interceptor, when getting WAL, first it will check if the given region should be  * replicated synchronously, if so it will return a special WAL for it, otherwise it will delegate  * the request to the normal {@link WALProvider}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SyncReplicationWALProvider
implements|implements
name|WALProvider
implements|,
name|PeerActionListener
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
name|SyncReplicationWALProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// only for injecting errors for testcase, do not use it for other purpose.
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
specifier|final
name|String
name|DUAL_WAL_IMPL
init|=
literal|"hbase.wal.sync.impl"
decl_stmt|;
specifier|private
specifier|final
name|WALProvider
name|provider
decl_stmt|;
specifier|private
name|SyncReplicationPeerInfoProvider
name|peerInfoProvider
init|=
operator|new
name|DefaultSyncReplicationPeerInfoProvider
argument_list|()
decl_stmt|;
specifier|private
name|WALFactory
name|factory
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
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
specifier|private
name|AtomicBoolean
name|initialized
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// when switching from A to DA, we will put a Optional.empty into this map if there is no WAL for
comment|// the peer yet. When getting WAL from this map the caller should know that it should not use
comment|// DualAsyncFSWAL any more.
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Optional
argument_list|<
name|DualAsyncFSWAL
argument_list|>
argument_list|>
name|peerId2WAL
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|KeyLocker
argument_list|<
name|String
argument_list|>
name|createLock
init|=
operator|new
name|KeyLocker
argument_list|<>
argument_list|()
decl_stmt|;
name|SyncReplicationWALProvider
parameter_list|(
name|WALProvider
name|provider
parameter_list|)
block|{
name|this
operator|.
name|provider
operator|=
name|provider
expr_stmt|;
block|}
specifier|public
name|void
name|setPeerInfoProvider
parameter_list|(
name|SyncReplicationPeerInfoProvider
name|peerInfoProvider
parameter_list|)
block|{
name|this
operator|.
name|peerInfoProvider
operator|=
name|peerInfoProvider
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|WALFactory
name|factory
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|initialized
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"WALProvider.init should only be called once."
argument_list|)
throw|;
block|}
name|provider
operator|.
name|init
argument_list|(
name|factory
argument_list|,
name|conf
argument_list|,
name|providerId
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|factory
operator|=
name|factory
expr_stmt|;
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
comment|// Use a timestamp to make it identical. That means, after we transit the peer to DA/S and then
comment|// back to A, the log prefix will be changed. This is used to simplify the implementation for
comment|// replication source, where we do not need to consider that a terminated shipper could be added
comment|// back.
specifier|private
name|String
name|getLogPrefix
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|factory
operator|.
name|factoryId
operator|+
literal|"-"
operator|+
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|+
literal|"-"
operator|+
name|peerId
return|;
block|}
specifier|private
name|DualAsyncFSWAL
name|createWAL
parameter_list|(
name|String
name|peerId
parameter_list|,
name|String
name|remoteWALDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|DualAsyncFSWAL
argument_list|>
name|clazz
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|DUAL_WAL_IMPL
argument_list|,
name|DualAsyncFSWAL
operator|.
name|class
argument_list|,
name|DualAsyncFSWAL
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Constructor
argument_list|<
name|?
argument_list|>
name|c
range|:
name|clazz
operator|.
name|getDeclaredConstructors
argument_list|()
control|)
block|{
if|if
condition|(
name|c
operator|.
name|getParameterCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|constructor
operator|=
name|c
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|constructor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No valid constructor provided for class "
operator|+
name|clazz
argument_list|)
throw|;
block|}
name|constructor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|(
name|DualAsyncFSWAL
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|CommonFSUtils
operator|.
name|getWALFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|ReplicationUtils
operator|.
name|getRemoteWALFileSystem
argument_list|(
name|conf
argument_list|,
name|remoteWALDir
argument_list|)
argument_list|,
name|CommonFSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|ReplicationUtils
operator|.
name|getPeerRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
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
name|getLogPrefix
argument_list|(
name|peerId
argument_list|)
argument_list|,
name|ReplicationUtils
operator|.
name|SYNC_WAL_SUFFIX
argument_list|,
name|eventLoopGroup
argument_list|,
name|channelClass
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|e
operator|.
name|getTargetException
argument_list|()
decl_stmt|;
name|Throwables
operator|.
name|propagateIfPossible
argument_list|(
name|cause
argument_list|,
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|cause
argument_list|)
throw|;
block|}
block|}
specifier|private
name|DualAsyncFSWAL
name|getWAL
parameter_list|(
name|String
name|peerId
parameter_list|,
name|String
name|remoteWALDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Optional
argument_list|<
name|DualAsyncFSWAL
argument_list|>
name|opt
init|=
name|peerId2WAL
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|opt
operator|!=
literal|null
condition|)
block|{
return|return
name|opt
operator|.
name|orElse
argument_list|(
literal|null
argument_list|)
return|;
block|}
name|Lock
name|lock
init|=
name|createLock
operator|.
name|acquireLock
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
try|try
block|{
name|opt
operator|=
name|peerId2WAL
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
if|if
condition|(
name|opt
operator|!=
literal|null
condition|)
block|{
return|return
name|opt
operator|.
name|orElse
argument_list|(
literal|null
argument_list|)
return|;
block|}
name|DualAsyncFSWAL
name|wal
init|=
name|createWAL
argument_list|(
name|peerId
argument_list|,
name|remoteWALDir
argument_list|)
decl_stmt|;
name|boolean
name|succ
init|=
literal|false
decl_stmt|;
try|try
block|{
name|wal
operator|.
name|init
argument_list|()
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
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|peerId2WAL
operator|.
name|put
argument_list|(
name|peerId
argument_list|,
name|Optional
operator|.
name|of
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|wal
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|WAL
name|getWAL
parameter_list|(
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|region
operator|==
literal|null
condition|)
block|{
return|return
name|provider
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
return|;
block|}
name|WAL
name|wal
init|=
literal|null
decl_stmt|;
name|Optional
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|peerIdAndRemoteWALDir
init|=
name|peerInfoProvider
operator|.
name|getPeerIdAndRemoteWALDir
argument_list|(
name|region
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|peerIdAndRemoteWALDir
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pair
init|=
name|peerIdAndRemoteWALDir
operator|.
name|get
argument_list|()
decl_stmt|;
name|wal
operator|=
name|getWAL
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|wal
operator|!=
literal|null
condition|?
name|wal
else|:
name|provider
operator|.
name|getWAL
argument_list|(
name|region
argument_list|)
return|;
block|}
specifier|private
name|Stream
argument_list|<
name|WAL
argument_list|>
name|getWALStream
parameter_list|()
block|{
return|return
name|Streams
operator|.
name|concat
argument_list|(
name|peerId2WAL
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|Optional
operator|::
name|isPresent
argument_list|)
operator|.
name|map
argument_list|(
name|Optional
operator|::
name|get
argument_list|)
argument_list|,
name|provider
operator|.
name|getWALs
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|WAL
argument_list|>
name|getWALs
parameter_list|()
block|{
return|return
name|getWALStream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
comment|// save the last exception and rethrow
name|IOException
name|failure
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Optional
argument_list|<
name|DualAsyncFSWAL
argument_list|>
name|wal
range|:
name|peerId2WAL
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|wal
operator|.
name|isPresent
argument_list|()
condition|)
block|{
try|try
block|{
name|wal
operator|.
name|get
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Shutdown WAL failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|failure
operator|=
name|e
expr_stmt|;
block|}
block|}
block|}
name|provider
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|failure
operator|!=
literal|null
condition|)
block|{
throw|throw
name|failure
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// save the last exception and rethrow
name|IOException
name|failure
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Optional
argument_list|<
name|DualAsyncFSWAL
argument_list|>
name|wal
range|:
name|peerId2WAL
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|wal
operator|.
name|isPresent
argument_list|()
condition|)
block|{
try|try
block|{
name|wal
operator|.
name|get
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Close WAL failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|failure
operator|=
name|e
expr_stmt|;
block|}
block|}
block|}
name|provider
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|failure
operator|!=
literal|null
condition|)
block|{
throw|throw
name|failure
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumLogFiles
parameter_list|()
block|{
return|return
name|peerId2WAL
operator|.
name|size
argument_list|()
operator|+
name|provider
operator|.
name|getNumLogFiles
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLogFileSize
parameter_list|()
block|{
return|return
name|peerId2WAL
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|Optional
operator|::
name|isPresent
argument_list|)
operator|.
name|map
argument_list|(
name|Optional
operator|::
name|get
argument_list|)
operator|.
name|mapToLong
argument_list|(
name|DualAsyncFSWAL
operator|::
name|getLogFileSize
argument_list|)
operator|.
name|sum
argument_list|()
operator|+
name|provider
operator|.
name|getLogFileSize
argument_list|()
return|;
block|}
specifier|private
name|void
name|safeClose
parameter_list|(
name|WAL
name|wal
parameter_list|)
block|{
if|if
condition|(
name|wal
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Close WAL failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addWALActionsListener
parameter_list|(
name|WALActionsListener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|provider
operator|.
name|addWALActionsListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|peerSyncReplicationStateChange
parameter_list|(
name|String
name|peerId
parameter_list|,
name|SyncReplicationState
name|from
parameter_list|,
name|SyncReplicationState
name|to
parameter_list|,
name|int
name|stage
parameter_list|)
block|{
if|if
condition|(
name|from
operator|==
name|SyncReplicationState
operator|.
name|ACTIVE
condition|)
block|{
if|if
condition|(
name|stage
operator|==
literal|0
condition|)
block|{
name|Lock
name|lock
init|=
name|createLock
operator|.
name|acquireLock
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
try|try
block|{
name|Optional
argument_list|<
name|DualAsyncFSWAL
argument_list|>
name|opt
init|=
name|peerId2WAL
operator|.
name|get
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|opt
operator|!=
literal|null
condition|)
block|{
name|opt
operator|.
name|ifPresent
argument_list|(
name|DualAsyncFSWAL
operator|::
name|skipRemoteWal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// add a place holder to tell the getWAL caller do not use DualAsyncFSWAL any more.
name|peerId2WAL
operator|.
name|put
argument_list|(
name|peerId
argument_list|,
name|Optional
operator|.
name|empty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|stage
operator|==
literal|1
condition|)
block|{
name|peerId2WAL
operator|.
name|remove
argument_list|(
name|peerId
argument_list|)
operator|.
name|ifPresent
argument_list|(
name|this
operator|::
name|safeClose
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|DefaultSyncReplicationPeerInfoProvider
implements|implements
name|SyncReplicationPeerInfoProvider
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPeerIdAndRemoteWALDir
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkState
parameter_list|(
name|TableName
name|table
parameter_list|,
name|BiPredicate
argument_list|<
name|SyncReplicationState
argument_list|,
name|SyncReplicationState
argument_list|>
name|checker
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|Pattern
name|LOG_PREFIX_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*-\\d+-(.+)"
argument_list|)
decl_stmt|;
comment|/**    *<p>    * Returns the peer id if the wal file name is in the special group for a sync replication peer.    *</p>    *<p>    * The prefix format is&lt;factoryId&gt;-&lt;ts&gt;-&lt;peerId&gt;.    *</p>    */
specifier|public
specifier|static
name|Optional
argument_list|<
name|String
argument_list|>
name|getSyncReplicationPeerIdFromWALName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
operator|!
name|name
operator|.
name|endsWith
argument_list|(
name|ReplicationUtils
operator|.
name|SYNC_WAL_SUFFIX
argument_list|)
condition|)
block|{
comment|// fast path to return earlier if the name is not for a sync replication peer.
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
name|String
name|logPrefix
init|=
name|AbstractFSWALProvider
operator|.
name|getWALPrefixFromWALName
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|Matcher
name|matcher
init|=
name|LOG_PREFIX_PATTERN
operator|.
name|matcher
argument_list|(
name|logPrefix
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

