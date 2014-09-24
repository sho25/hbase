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
name|zookeeper
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
name|rmi
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|HConstants
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
name|HRegionInfo
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
name|HConnection
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
name|exceptions
operator|.
name|DeserializationException
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
name|RpcClient
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
name|RegionState
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|AdminService
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
operator|.
name|MetaRegionServer
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Stopwatch
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Utility class to perform operation (get/wait for/verify/set/delete) on znode in ZooKeeper  * which keeps hbase:meta region server location.  *  * Stateless class with a bunch of static methods. Doesn't manage resources passed in  * (e.g. HConnection, ZooKeeperWatcher etc).  *  * Meta region location is set by<code>RegionServerServices</code>.  * This class doesn't use ZK watchers, rather accesses ZK directly.  *  * This class it stateless. The only reason it's not made a non-instantiable util class  * with a collection of static methods is that it'd be rather hard to mock properly in tests.  *  * TODO: rewrite using RPC calls to master to find out about hbase:meta.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaTableLocator
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
name|MetaTableLocator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|META_REGION_NAME
init|=
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
comment|// only needed to allow non-timeout infinite waits to stop when cluster shuts down
specifier|private
specifier|volatile
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
comment|/**    * Checks if the meta region location is available.    * @return true if meta region location is available, false if not    */
specifier|public
name|boolean
name|isLocationAvailable
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
block|{
return|return
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * @param zkw ZooKeeper watcher to be used    * @return meta table regions and their locations.    */
specifier|public
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|getMetaRegionsAndLocations
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
block|{
name|ServerName
name|serverName
init|=
operator|new
name|MetaTableLocator
argument_list|()
operator|.
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
comment|/**    * @param zkw ZooKeeper watcher to be used    * @return List of meta regions    */
specifier|public
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getMetaRegions
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|result
decl_stmt|;
name|result
operator|=
name|getMetaRegionsAndLocations
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
return|return
name|getListOfHRegionInfos
argument_list|(
name|result
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getListOfHRegionInfos
parameter_list|(
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|pairs
parameter_list|)
block|{
if|if
condition|(
name|pairs
operator|==
literal|null
operator|||
name|pairs
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|null
return|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|pairs
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|pair
range|:
name|pairs
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Gets the meta region location, if available.  Does not block.    * @param zkw zookeeper connection to use    * @return server name or null if we failed to get the data.    */
annotation|@
name|Nullable
specifier|public
name|ServerName
name|getMetaRegionLocation
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|)
block|{
try|try
block|{
name|RegionState
name|state
init|=
name|getMetaRegionState
argument_list|(
name|zkw
argument_list|)
decl_stmt|;
return|return
name|state
operator|.
name|isOpened
argument_list|()
condition|?
name|state
operator|.
name|getServerName
argument_list|()
else|:
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Gets the meta region location, if available, and waits for up to the    * specified timeout if not immediately available.    * Given the zookeeper notification could be delayed, we will try to    * get the latest data.    * @param timeout maximum time to wait, in millis    * @return server name for server hosting meta region formatted as per    * {@link ServerName}, or null if none available    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|ServerName
name|waitMetaRegionLocation
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|NotAllMetaRegionsOnlineException
block|{
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|baseZNode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Check the value configured in 'zookeeper.znode.parent'. "
operator|+
literal|"There could be a mismatch with the one configured in the master."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"KeeperException while trying to check baseZNode:"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|ServerName
name|sn
init|=
name|blockUntilAvailable
argument_list|(
name|zkw
argument_list|,
name|timeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NotAllMetaRegionsOnlineException
argument_list|(
literal|"Timed out; "
operator|+
name|timeout
operator|+
literal|"ms"
argument_list|)
throw|;
block|}
return|return
name|sn
return|;
block|}
comment|/**    * Waits indefinitely for availability of<code>hbase:meta</code>.  Used during    * cluster startup.  Does not verify meta, just that something has been    * set up in zk.    * @see #waitMetaRegionLocation(org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher, long)    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|void
name|waitMetaRegionLocation
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|Stopwatch
name|stopwatch
init|=
operator|new
name|Stopwatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
try|try
block|{
if|if
condition|(
name|waitMetaRegionLocation
argument_list|(
name|zkw
argument_list|,
literal|100
argument_list|)
operator|!=
literal|null
condition|)
break|break;
name|long
name|sleepTime
init|=
name|stopwatch
operator|.
name|elapsedMillis
argument_list|()
decl_stmt|;
comment|// +1 in case sleepTime=0
if|if
condition|(
operator|(
name|sleepTime
operator|+
literal|1
operator|)
operator|%
literal|10000
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Have been waiting for meta to be assigned for "
operator|+
name|sleepTime
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NotAllMetaRegionsOnlineException
name|e
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"hbase:meta still not available, sleeping and retrying."
operator|+
literal|" Reason: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Verify<code>hbase:meta</code> is deployed and accessible.    * @param timeout How long to wait on zk for meta address (passed through to    * the internal call to {@link #getMetaServerConnection}.    * @return True if the<code>hbase:meta</code> location is healthy.    * @throws java.io.IOException    * @throws InterruptedException    */
specifier|public
name|boolean
name|verifyMetaRegionLocation
parameter_list|(
name|HConnection
name|hConnection
parameter_list|,
name|ZooKeeperWatcher
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
name|hConnection
argument_list|,
name|zkw
argument_list|,
name|timeout
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
name|service
argument_list|,
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|)
argument_list|,
name|META_REGION_NAME
argument_list|)
return|;
block|}
comment|/**    * Verify we can connect to<code>hostingServer</code> and that its carrying    *<code>regionName</code>.    * @param hostingServer Interface to the server hosting<code>regionName</code>    * @param address The servername that goes with the<code>metaServer</code>    * Interface.  Used logging.    * @param regionName The regionname we are interested in.    * @return True if we were able to verify the region located at other side of    * the Interface.    * @throws IOException    */
comment|// TODO: We should be able to get the ServerName from the AdminProtocol
comment|// rather than have to pass it in.  Its made awkward by the fact that the
comment|// HRI is likely a proxy against remote server so the getServerName needs
comment|// to be fixed to go to a local method or to a cache before we can do this.
specifier|private
name|boolean
name|verifyRegionLocation
parameter_list|(
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
throws|throws
name|IOException
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
try|try
block|{
comment|// Try and get regioninfo from the hosting server.
return|return
name|ProtobufUtil
operator|.
name|getRegionInfo
argument_list|(
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
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/**    * Gets a connection to the server hosting meta, as reported by ZooKeeper,    * waiting up to the specified timeout for availability.    *<p>WARNING: Does not retry.  Use an {@link org.apache.hadoop.hbase.client.HTable} instead.    * @param timeout How long to wait on meta location    * @return connection to server hosting meta    * @throws InterruptedException    * @throws NotAllMetaRegionsOnlineException if timed out waiting    * @throws IOException    */
specifier|private
name|AdminService
operator|.
name|BlockingInterface
name|getMetaServerConnection
parameter_list|(
name|HConnection
name|hConnection
parameter_list|,
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
return|return
name|getCachedConnection
argument_list|(
name|hConnection
argument_list|,
name|waitMetaRegionLocation
argument_list|(
name|zkw
argument_list|,
name|timeout
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param sn ServerName to get a connection against.    * @return The AdminProtocol we got when we connected to<code>sn</code>    * May have come from cache, may not be good, may have been setup by this    * invocation, or may be null.    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
specifier|static
name|AdminService
operator|.
name|BlockingInterface
name|getCachedConnection
parameter_list|(
name|HConnection
name|hConnection
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
name|hConnection
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
comment|// Catch this; presume it means the cached connection has gone bad.
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
name|RpcClient
operator|.
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
comment|// Catch. Connect refused.
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
comment|// Catch. Other end disconnected us.
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
argument_list|()
operator|.
name|contains
argument_list|(
literal|"connection reset"
argument_list|)
condition|)
block|{
comment|// Catch. Connection reset.
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
comment|/**    * Sets the location of<code>hbase:meta</code> in ZooKeeper to the    * specified server address.    * @param zookeeper zookeeper reference    * @param serverName The server hosting<code>hbase:meta</code>    * @param state The region transition state    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
specifier|static
name|void
name|setMetaLocation
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|RegionState
operator|.
name|State
name|state
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting hbase:meta region location in ZooKeeper as "
operator|+
name|serverName
argument_list|)
expr_stmt|;
comment|// Make the MetaRegionServer pb and then get its bytes and save this as
comment|// the znode content.
name|MetaRegionServer
name|pbrsr
init|=
name|MetaRegionServer
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
operator|.
name|setRpcVersion
argument_list|(
name|HConstants
operator|.
name|RPC_CURRENT_VERSION
argument_list|)
operator|.
name|setState
argument_list|(
name|state
operator|.
name|convert
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|pbrsr
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|zookeeper
argument_list|,
name|zookeeper
operator|.
name|metaServerZNode
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"META region location doesn't existed, create it"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|zookeeper
argument_list|,
name|zookeeper
operator|.
name|metaServerZNode
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Load the meta region state from the meta server ZNode.    */
specifier|public
specifier|static
name|RegionState
name|getMetaRegionState
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
block|{
name|RegionState
operator|.
name|State
name|state
init|=
name|RegionState
operator|.
name|State
operator|.
name|OPEN
decl_stmt|;
name|ServerName
name|serverName
init|=
literal|null
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|zkw
operator|.
name|metaServerZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|!=
literal|null
operator|&&
name|data
operator|.
name|length
operator|>
literal|0
operator|&&
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
try|try
block|{
name|int
name|prefixLen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|MetaRegionServer
name|rl
init|=
name|ZooKeeperProtos
operator|.
name|MetaRegionServer
operator|.
name|PARSER
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|,
name|prefixLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|prefixLen
argument_list|)
decl_stmt|;
if|if
condition|(
name|rl
operator|.
name|hasState
argument_list|()
condition|)
block|{
name|state
operator|=
name|RegionState
operator|.
name|State
operator|.
name|convert
argument_list|(
name|rl
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HBaseProtos
operator|.
name|ServerName
name|sn
init|=
name|rl
operator|.
name|getServer
argument_list|()
decl_stmt|;
name|serverName
operator|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|sn
operator|.
name|getHostName
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|,
name|sn
operator|.
name|getStartCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
literal|"Unable to parse meta region location"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// old style of meta region location?
name|serverName
operator|=
name|ServerName
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
name|ZKUtil
operator|.
name|convert
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|serverName
operator|==
literal|null
condition|)
block|{
name|state
operator|=
name|RegionState
operator|.
name|State
operator|.
name|OFFLINE
expr_stmt|;
block|}
return|return
operator|new
name|RegionState
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|state
argument_list|,
name|serverName
argument_list|)
return|;
block|}
comment|/**    * Deletes the location of<code>hbase:meta</code> in ZooKeeper.    * @param zookeeper zookeeper reference    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|void
name|deleteMetaLocation
parameter_list|(
name|ZooKeeperWatcher
name|zookeeper
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting hbase:meta region location in ZooKeeper"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Just delete the node.  Don't need any watches.
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zookeeper
argument_list|,
name|zookeeper
operator|.
name|metaServerZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
comment|// Has already been deleted
block|}
block|}
comment|/**    * Wait until the meta region is available and is not in transition.    * @param zkw zookeeper connection to use    * @param timeout maximum time to wait, in millis    * @return ServerName or null if we timed out.    * @throws InterruptedException    */
specifier|public
name|ServerName
name|blockUntilAvailable
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|timeout
operator|<
literal|0
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
if|if
condition|(
name|zkw
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
name|Stopwatch
name|sw
init|=
operator|new
name|Stopwatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|ServerName
name|sn
init|=
literal|null
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|sn
operator|=
name|getMetaRegionLocation
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
if|if
condition|(
name|sn
operator|!=
literal|null
operator|||
name|sw
operator|.
name|elapsedMillis
argument_list|()
operator|>
name|timeout
operator|-
name|HConstants
operator|.
name|SOCKET_RETRY_WAIT_MS
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|HConstants
operator|.
name|SOCKET_RETRY_WAIT_MS
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|sw
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
return|return
name|sn
return|;
block|}
comment|/**    * Stop working.    * Interrupts any ongoing waits.    */
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|stopped
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping MetaTableLocator"
argument_list|)
expr_stmt|;
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

