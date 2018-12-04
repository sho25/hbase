begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rsgroup
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|net
operator|.
name|ConnectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|NoRouteToHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|NotAllMetaRegionsOnlineException
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
name|ServerName
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
name|ClusterConnection
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
name|client
operator|.
name|RegionInfoBuilder
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
name|RegionReplicaUtil
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
name|RetriesExhaustedException
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
name|ipc
operator|.
name|FailedServerException
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
name|ipc
operator|.
name|HBaseRpcController
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
name|ipc
operator|.
name|ServerNotRunningYetException
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
name|master
operator|.
name|MasterServices
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
name|net
operator|.
name|Address
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
name|RegionServerStoppedException
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
name|Bytes
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
name|zookeeper
operator|.
name|MetaTableLocator
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
name|zookeeper
operator|.
name|ZKWatcher
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
name|ipc
operator|.
name|RemoteException
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|AdminService
import|;
end_import

begin_comment
comment|/**  * Utility for this RSGroup package in hbase-rsgroup.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|Utility
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
name|Utility
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Utility
parameter_list|()
block|{   }
comment|/**    * @param master the master to get online servers for    * @return Set of online Servers named for their hostname and port (not ServerName).    */
specifier|static
name|Set
argument_list|<
name|Address
argument_list|>
name|getOnlineServers
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|)
block|{
name|Set
argument_list|<
name|Address
argument_list|>
name|onlineServers
init|=
operator|new
name|HashSet
argument_list|<
name|Address
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|master
operator|==
literal|null
condition|)
block|{
return|return
name|onlineServers
return|;
block|}
for|for
control|(
name|ServerName
name|server
range|:
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServers
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|onlineServers
operator|.
name|add
argument_list|(
name|server
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|onlineServers
return|;
block|}
comment|/**    * Verify<code>hbase:meta</code> is deployed and accessible.    * @param hConnection the connection to use    * @param zkw reference to the {@link ZKWatcher} which also contains configuration and operation    * @param timeout How long to wait on zk for meta address (passed through to the internal call to    *          {@link #getMetaServerConnection}.    * @return True if the<code>hbase:meta</code> location is healthy.    * @throws IOException if the number of retries for getting the connection is exceeded    * @throws InterruptedException if waiting for the socket operation fails    */
specifier|public
specifier|static
name|boolean
name|verifyMetaRegionLocation
parameter_list|(
name|ClusterConnection
name|hConnection
parameter_list|,
name|ZKWatcher
name|zkw
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
return|return
name|verifyMetaRegionLocation
argument_list|(
name|hConnection
argument_list|,
name|zkw
argument_list|,
name|timeout
argument_list|,
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
argument_list|)
return|;
block|}
comment|/**    * Verify<code>hbase:meta</code> is deployed and accessible.    * @param connection the connection to use    * @param zkw reference to the {@link ZKWatcher} which also contains configuration and operation    * @param timeout How long to wait on zk for meta address (passed through to    * @param replicaId the ID of the replica    * @return True if the<code>hbase:meta</code> location is healthy.    * @throws InterruptedException if waiting for the socket operation fails    * @throws IOException if the number of retries for getting the connection is exceeded    */
specifier|public
specifier|static
name|boolean
name|verifyMetaRegionLocation
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|ZKWatcher
name|zkw
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|,
name|int
name|replicaId
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|service
init|=
literal|null
decl_stmt|;
try|try
block|{
name|service
operator|=
name|getMetaServerConnection
argument_list|(
name|connection
argument_list|,
name|zkw
argument_list|,
name|timeout
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotAllMetaRegionsOnlineException
name|e
parameter_list|)
block|{
comment|// Pass
block|}
catch|catch
parameter_list|(
name|ServerNotRunningYetException
name|e
parameter_list|)
block|{
comment|// Pass -- remote server is not up so can't be carrying root
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
comment|// Pass -- server name doesn't resolve so it can't be assigned anything.
block|}
catch|catch
parameter_list|(
name|RegionServerStoppedException
name|e
parameter_list|)
block|{
comment|// Pass -- server name sends us to a server that is dying or already dead.
block|}
return|return
operator|(
name|service
operator|!=
literal|null
operator|)
operator|&&
name|verifyRegionLocation
argument_list|(
name|connection
argument_list|,
name|service
argument_list|,
name|MetaTableLocator
operator|.
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|,
name|replicaId
argument_list|)
argument_list|,
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|replicaId
argument_list|)
operator|.
name|getRegionName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Verify we can connect to<code>hostingServer</code> and that its carrying    *<code>regionName</code>.    * @param hostingServer Interface to the server hosting<code>regionName</code>    * @param address The servername that goes with the<code>metaServer</code> interface. Used    *          logging.    * @param regionName The regionname we are interested in.    * @return True if we were able to verify the region located at other side of the interface.    */
comment|// TODO: We should be able to get the ServerName from the AdminProtocol
comment|// rather than have to pass it in. Its made awkward by the fact that the
comment|// HRI is likely a proxy against remote server so the getServerName needs
comment|// to be fixed to go to a local method or to a cache before we can do this.
specifier|private
specifier|static
name|boolean
name|verifyRegionLocation
parameter_list|(
specifier|final
name|ClusterConnection
name|connection
parameter_list|,
name|AdminService
operator|.
name|BlockingInterface
name|hostingServer
parameter_list|,
specifier|final
name|ServerName
name|address
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|)
block|{
if|if
condition|(
name|hostingServer
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Passed hostingServer is null"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Throwable
name|t
decl_stmt|;
name|HBaseRpcController
name|controller
init|=
name|connection
operator|.
name|getRpcControllerFactory
argument_list|()
operator|.
name|newController
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Try and get regioninfo from the hosting server.
return|return
name|ProtobufUtil
operator|.
name|getRegionInfo
argument_list|(
name|controller
argument_list|,
name|hostingServer
argument_list|,
name|regionName
argument_list|)
operator|!=
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|ConnectException
name|e
parameter_list|)
block|{
name|t
operator|=
name|e
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
name|t
operator|=
name|e
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
name|IOException
name|ioe
init|=
name|e
operator|.
name|unwrapRemoteException
argument_list|()
decl_stmt|;
name|t
operator|=
name|ioe
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|EOFException
condition|)
block|{
name|t
operator|=
name|cause
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Connection reset"
argument_list|)
condition|)
block|{
name|t
operator|=
name|cause
expr_stmt|;
block|}
else|else
block|{
name|t
operator|=
name|e
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed verification of "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
operator|+
literal|" at address="
operator|+
name|address
operator|+
literal|", exception="
operator|+
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/**    * Gets a connection to the server hosting meta, as reported by ZooKeeper, waiting up to the    * specified timeout for availability.    *<p>    * WARNING: Does not retry. Use an {@link org.apache.hadoop.hbase.client.HTable} instead.    * @param connection the connection to use    * @param zkw reference to the {@link ZKWatcher} which also contains configuration and operation    * @param timeout How long to wait on meta location    * @param replicaId the ID of the replica    * @return connection to server hosting meta    * @throws InterruptedException if waiting for the socket operation fails    * @throws IOException if the number of retries for getting the connection is exceeded    */
specifier|private
specifier|static
name|AdminService
operator|.
name|BlockingInterface
name|getMetaServerConnection
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|ZKWatcher
name|zkw
parameter_list|,
name|long
name|timeout
parameter_list|,
name|int
name|replicaId
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
return|return
name|getCachedConnection
argument_list|(
name|connection
argument_list|,
name|MetaTableLocator
operator|.
name|waitMetaRegionLocation
argument_list|(
name|zkw
argument_list|,
name|replicaId
argument_list|,
name|timeout
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param sn ServerName to get a connection against.    * @return The AdminProtocol we got when we connected to<code>sn</code> May have come from cache,    *         may not be good, may have been setup by this invocation, or may be null.    * @throws IOException if the number of retries for getting the connection is exceeded    */
specifier|private
specifier|static
name|AdminService
operator|.
name|BlockingInterface
name|getCachedConnection
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|AdminService
operator|.
name|BlockingInterface
name|service
init|=
literal|null
decl_stmt|;
try|try
block|{
name|service
operator|=
name|connection
operator|.
name|getAdmin
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ConnectException
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Catch this; presume it means the cached connection has gone bad."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|SocketTimeoutException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Timed out connecting to "
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoRouteToHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connecting to "
operator|+
name|sn
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SocketException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception connecting to "
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unknown host exception connecting to  "
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedServerException
name|e
parameter_list|)
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
literal|"Server "
operator|+
name|sn
operator|+
literal|" is in failed server list."
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|ioe
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|ioe
operator|instanceof
name|ConnectException
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Catch. Connect refused."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|EOFException
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Catch. Other end disconnected us."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|contains
argument_list|(
literal|"connection reset"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Catch. Connection reset."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|ioe
throw|;
block|}
block|}
return|return
name|service
return|;
block|}
block|}
end_class

end_unit

