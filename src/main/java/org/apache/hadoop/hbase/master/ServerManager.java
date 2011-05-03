begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
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
name|net
operator|.
name|InetAddress
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
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
name|ClockOutOfSyncException
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
name|HServerAddress
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
name|HServerLoad
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
name|PleaseHoldException
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
name|Server
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
name|YouAreDeadException
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
name|ZooKeeperConnectionException
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
name|catalog
operator|.
name|CatalogTracker
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
name|HConnectionManager
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
name|HRegionInterface
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
name|handler
operator|.
name|MetaServerShutdownHandler
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
name|handler
operator|.
name|ServerShutdownHandler
import|;
end_import

begin_comment
comment|/**  * The ServerManager class manages info about region servers.  *<p>  * Maintains lists of online and dead servers.  Processes the startups,  * shutdowns, and deaths of region servers.  *<p>  * Servers are distinguished in two different ways.  A given server has a  * location, specified by hostname and port, and of which there can only be one  * online at any given time.  A server instance is specified by the location  * (hostname and port) as well as the startcode (timestamp from when the server  * was started).  This is used to differentiate a restarted instance of a given  * server from the original instance.  */
end_comment

begin_class
specifier|public
class|class
name|ServerManager
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
name|ServerManager
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Set if we are to shutdown the cluster.
specifier|private
specifier|volatile
name|boolean
name|clusterShutdown
init|=
literal|false
decl_stmt|;
comment|/** Map of registered servers to their current load */
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|HServerLoad
argument_list|>
name|onlineServers
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ServerName
argument_list|,
name|HServerLoad
argument_list|>
argument_list|()
decl_stmt|;
comment|// TODO: This is strange to have two maps but HSI above is used on both sides
comment|/**    * Map from full server-instance name to the RPC connection for this server.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|HRegionInterface
argument_list|>
name|serverConnections
init|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|HRegionInterface
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Server
name|master
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|DeadServer
name|deadservers
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxSkew
decl_stmt|;
comment|/**    * Constructor.    * @param master    * @param services    * @throws ZooKeeperConnectionException    */
specifier|public
name|ServerManager
parameter_list|(
specifier|final
name|Server
name|master
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
block|{
name|this
argument_list|(
name|master
argument_list|,
name|services
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|ServerManager
parameter_list|(
specifier|final
name|Server
name|master
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|boolean
name|connect
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|Configuration
name|c
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|maxSkew
operator|=
name|c
operator|.
name|getLong
argument_list|(
literal|"hbase.master.maxclockskew"
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadservers
operator|=
operator|new
name|DeadServer
argument_list|()
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connect
condition|?
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|c
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
comment|/**    * Let the server manager know a new regionserver has come online    * @param ia The remote address    * @param port The remote port    * @param serverStartcode    * @param serverCurrentTime The current time of the region server in ms    * @return The ServerName we know this server as.    * @throws IOException    */
name|ServerName
name|regionServerStartup
parameter_list|(
specifier|final
name|InetAddress
name|ia
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|long
name|serverStartcode
parameter_list|,
name|long
name|serverCurrentTime
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Test for case where we get a region startup message from a regionserver
comment|// that has been quickly restarted but whose znode expiration handler has
comment|// not yet run, or from a server whose fail we are currently processing.
comment|// Test its host+port combo is present in serverAddresstoServerInfo.  If it
comment|// is, reject the server and trigger its expiration. The next time it comes
comment|// in, it should have been removed from serverAddressToServerInfo and queued
comment|// for processing by ProcessServerShutdown.
name|ServerName
name|sn
init|=
operator|new
name|ServerName
argument_list|(
name|ia
operator|.
name|getHostName
argument_list|()
argument_list|,
name|port
argument_list|,
name|serverStartcode
argument_list|)
decl_stmt|;
name|checkClockSkew
argument_list|(
name|sn
argument_list|,
name|serverCurrentTime
argument_list|)
expr_stmt|;
name|checkIsDead
argument_list|(
name|sn
argument_list|,
literal|"STARTUP"
argument_list|)
expr_stmt|;
name|checkAlreadySameHostPort
argument_list|(
name|sn
argument_list|)
expr_stmt|;
name|recordNewServer
argument_list|(
name|sn
argument_list|,
name|HServerLoad
operator|.
name|EMPTY_HSERVERLOAD
argument_list|)
expr_stmt|;
return|return
name|sn
return|;
block|}
name|void
name|regionServerReport
parameter_list|(
name|ServerName
name|sn
parameter_list|,
name|HServerLoad
name|hsl
parameter_list|)
throws|throws
name|YouAreDeadException
throws|,
name|PleaseHoldException
block|{
name|checkIsDead
argument_list|(
name|sn
argument_list|,
literal|"REPORT"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|onlineServers
operator|.
name|containsKey
argument_list|(
name|sn
argument_list|)
condition|)
block|{
comment|// Already have this host+port combo and its just different start code?
name|checkAlreadySameHostPort
argument_list|(
name|sn
argument_list|)
expr_stmt|;
comment|// Just let the server in. Presume master joining a running cluster.
comment|// recordNewServer is what happens at the end of reportServerStartup.
comment|// The only thing we are skipping is passing back to the regionserver
comment|// the ServerName to use. Here we presume a master has already done
comment|// that so we'll press on with whatever it gave us for ServerName.
name|recordNewServer
argument_list|(
name|sn
argument_list|,
name|hsl
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|onlineServers
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|hsl
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test to see if we have a server of same host and port already.    * @param serverName    * @throws PleaseHoldException    */
name|void
name|checkAlreadySameHostPort
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|PleaseHoldException
block|{
name|ServerName
name|existingServer
init|=
name|ServerName
operator|.
name|findServerWithSameHostnamePort
argument_list|(
name|getOnlineServersList
argument_list|()
argument_list|,
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingServer
operator|!=
literal|null
condition|)
block|{
name|String
name|message
init|=
literal|"Server serverName="
operator|+
name|serverName
operator|+
literal|" rejected; we already have "
operator|+
name|existingServer
operator|.
name|toString
argument_list|()
operator|+
literal|" registered with same hostname and port"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|message
argument_list|)
expr_stmt|;
if|if
condition|(
name|existingServer
operator|.
name|getStartcode
argument_list|()
operator|<
name|serverName
operator|.
name|getStartcode
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Triggering server recovery; existingServer "
operator|+
name|existingServer
operator|+
literal|" looks stale"
argument_list|)
expr_stmt|;
name|expireServer
argument_list|(
name|existingServer
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|PleaseHoldException
argument_list|(
name|message
argument_list|)
throw|;
block|}
block|}
comment|/**    * Checks if the clock skew between the server and the master. If the clock    * skew is too much it will throw an Exception.    * @param serverName Incoming servers's name    * @param serverCurrentTime    * @throws ClockOutOfSyncException    */
specifier|private
name|void
name|checkClockSkew
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|long
name|serverCurrentTime
parameter_list|)
throws|throws
name|ClockOutOfSyncException
block|{
name|long
name|skew
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|serverCurrentTime
decl_stmt|;
if|if
condition|(
name|skew
operator|>
name|maxSkew
condition|)
block|{
name|String
name|message
init|=
literal|"Server "
operator|+
name|serverName
operator|+
literal|" has been "
operator|+
literal|"rejected; Reported time is too far out of sync with master.  "
operator|+
literal|"Time difference of "
operator|+
name|skew
operator|+
literal|"ms> max allowed of "
operator|+
name|maxSkew
operator|+
literal|"ms"
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|message
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ClockOutOfSyncException
argument_list|(
name|message
argument_list|)
throw|;
block|}
block|}
comment|/**    * If this server is on the dead list, reject it with a YouAreDeadException.    * If it was dead but came back with a new start code, remove the old entry    * from the dead list.    * @param serverName    * @param what START or REPORT    * @throws YouAreDeadException    */
specifier|private
name|void
name|checkIsDead
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|String
name|what
parameter_list|)
throws|throws
name|YouAreDeadException
block|{
if|if
condition|(
name|this
operator|.
name|deadservers
operator|.
name|isDeadServer
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// host name, port and start code all match with existing one of the
comment|// dead servers. So, this server must be dead.
name|String
name|message
init|=
literal|"Server "
operator|+
name|what
operator|+
literal|" rejected; currently processing "
operator|+
name|serverName
operator|+
literal|" as dead server"
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|message
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|YouAreDeadException
argument_list|(
name|message
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|deadservers
operator|.
name|cleanPreviousInstance
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// This server has now become alive after we marked it as dead.
comment|// We removed it's previous entry from the dead list to reflect it.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Server "
operator|+
name|serverName
operator|+
literal|" came back up, removed it from the"
operator|+
literal|" dead servers list"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Adds the onlineServers list.    * @param hsl    * @param serverName The remote servers name.    */
name|void
name|recordNewServer
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|HServerLoad
name|hsl
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Registering server="
operator|+
name|serverName
argument_list|)
expr_stmt|;
name|this
operator|.
name|onlineServers
operator|.
name|put
argument_list|(
name|serverName
argument_list|,
name|hsl
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverConnections
operator|.
name|remove
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param serverName    * @return HServerLoad if serverName is known else null    */
specifier|public
name|HServerLoad
name|getLoad
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|this
operator|.
name|onlineServers
operator|.
name|get
argument_list|(
name|serverName
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param serverName    * @return HServerLoad if serverName is known else null    * @deprecated Use {@link #getLoad(HServerAddress)}    */
specifier|public
name|HServerLoad
name|getLoad
parameter_list|(
specifier|final
name|HServerAddress
name|address
parameter_list|)
block|{
name|ServerName
name|sn
init|=
operator|new
name|ServerName
argument_list|(
name|address
operator|.
name|toString
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|ServerName
name|actual
init|=
name|ServerName
operator|.
name|findServerWithSameHostnamePort
argument_list|(
name|this
operator|.
name|getOnlineServersList
argument_list|()
argument_list|,
name|sn
argument_list|)
decl_stmt|;
return|return
name|actual
operator|==
literal|null
condition|?
literal|null
else|:
name|getLoad
argument_list|(
name|actual
argument_list|)
return|;
block|}
comment|/**    * Compute the average load across all region servers.    * Currently, this uses a very naive computation - just uses the number of    * regions being served, ignoring stats about number of requests.    * @return the average load    */
specifier|public
name|double
name|getAverageLoad
parameter_list|()
block|{
name|int
name|totalLoad
init|=
literal|0
decl_stmt|;
name|int
name|numServers
init|=
literal|0
decl_stmt|;
name|double
name|averageLoad
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|HServerLoad
name|hsl
range|:
name|this
operator|.
name|onlineServers
operator|.
name|values
argument_list|()
control|)
block|{
name|numServers
operator|++
expr_stmt|;
name|totalLoad
operator|+=
name|hsl
operator|.
name|getNumberOfRegions
argument_list|()
expr_stmt|;
block|}
name|averageLoad
operator|=
operator|(
name|double
operator|)
name|totalLoad
operator|/
operator|(
name|double
operator|)
name|numServers
expr_stmt|;
return|return
name|averageLoad
return|;
block|}
comment|/** @return the count of active regionservers */
name|int
name|countOfRegionServers
parameter_list|()
block|{
comment|// Presumes onlineServers is a concurrent map
return|return
name|this
operator|.
name|onlineServers
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return Read-only map of servers to serverinfo    */
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|HServerLoad
argument_list|>
name|getOnlineServers
parameter_list|()
block|{
comment|// Presumption is that iterating the returned Map is OK.
synchronized|synchronized
init|(
name|this
operator|.
name|onlineServers
init|)
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|this
operator|.
name|onlineServers
argument_list|)
return|;
block|}
block|}
specifier|public
name|Set
argument_list|<
name|ServerName
argument_list|>
name|getDeadServers
parameter_list|()
block|{
return|return
name|this
operator|.
name|deadservers
operator|.
name|clone
argument_list|()
return|;
block|}
comment|/**    * Checks if any dead servers are currently in progress.    * @return true if any RS are being processed as dead, false if not    */
specifier|public
name|boolean
name|areDeadServersInProgress
parameter_list|()
block|{
return|return
name|this
operator|.
name|deadservers
operator|.
name|areDeadServersInProgress
argument_list|()
return|;
block|}
name|void
name|letRegionServersShutdown
parameter_list|()
block|{
synchronized|synchronized
init|(
name|onlineServers
init|)
block|{
while|while
condition|(
operator|!
name|onlineServers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|key
range|:
name|this
operator|.
name|onlineServers
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on regionserver(s) to go down "
operator|+
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|onlineServers
operator|.
name|wait
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
comment|/*    * Expire the passed server.  Add it to list of deadservers and queue a    * shutdown processing.    */
specifier|public
specifier|synchronized
name|void
name|expireServer
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|onlineServers
operator|.
name|containsKey
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received expiration of "
operator|+
name|serverName
operator|+
literal|" but server is not currently online"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|this
operator|.
name|deadservers
operator|.
name|contains
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// TODO: Can this happen?  It shouldn't be online in this case?
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received expiration of "
operator|+
name|serverName
operator|+
literal|" but server shutdown is already in progress"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Remove the server from the known servers lists and update load info BUT
comment|// add to deadservers first; do this so it'll show in dead servers list if
comment|// not in online servers list.
name|this
operator|.
name|deadservers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|this
operator|.
name|onlineServers
operator|.
name|remove
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverConnections
operator|.
name|remove
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
comment|// If cluster is going down, yes, servers are going to be expiring; don't
comment|// process as a dead server
if|if
condition|(
name|this
operator|.
name|clusterShutdown
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster shutdown set; "
operator|+
name|serverName
operator|+
literal|" expired; onlineServers="
operator|+
name|this
operator|.
name|onlineServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|onlineServers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|master
operator|.
name|stop
argument_list|(
literal|"Cluster shutdown set; onlineServer=0"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|CatalogTracker
name|ct
init|=
name|this
operator|.
name|master
operator|.
name|getCatalogTracker
argument_list|()
decl_stmt|;
comment|// Was this server carrying root?
name|boolean
name|carryingRoot
decl_stmt|;
try|try
block|{
name|ServerName
name|address
init|=
name|ct
operator|.
name|getRootLocation
argument_list|()
decl_stmt|;
name|carryingRoot
operator|=
name|address
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Interrupted"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Was this server carrying meta?  Can't ask CatalogTracker because it
comment|// may have reset the meta location as null already (it may have already
comment|// run into fact that meta is dead).  I can ask assignment manager. It
comment|// has an inmemory list of who has what.  This list will be cleared as we
comment|// process the dead server but should be  find asking it now.
name|ServerName
name|address
init|=
name|ct
operator|.
name|getMetaLocation
argument_list|()
decl_stmt|;
name|boolean
name|carryingMeta
init|=
name|address
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|carryingRoot
operator|||
name|carryingMeta
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
operator|new
name|MetaServerShutdownHandler
argument_list|(
name|this
operator|.
name|master
argument_list|,
name|this
operator|.
name|services
argument_list|,
name|this
operator|.
name|deadservers
argument_list|,
name|serverName
argument_list|,
name|carryingRoot
argument_list|,
name|carryingMeta
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
operator|new
name|ServerShutdownHandler
argument_list|(
name|this
operator|.
name|master
argument_list|,
name|this
operator|.
name|services
argument_list|,
name|this
operator|.
name|deadservers
argument_list|,
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Added="
operator|+
name|serverName
operator|+
literal|" to dead servers, submitted shutdown handler to be executed, root="
operator|+
name|carryingRoot
operator|+
literal|", meta="
operator|+
name|carryingMeta
argument_list|)
expr_stmt|;
block|}
comment|// RPC methods to region servers
comment|/**    * Sends an OPEN RPC to the specified server to open the specified region.    *<p>    * Open should not fail but can if server just crashed.    *<p>    * @param server server to open a region    * @param region region to open    */
specifier|public
name|void
name|sendRegionOpen
parameter_list|(
specifier|final
name|ServerName
name|server
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInterface
name|hri
init|=
name|getServerConnection
argument_list|(
name|server
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempting to send OPEN RPC to server "
operator|+
name|server
operator|.
name|toString
argument_list|()
operator|+
literal|" failed because no RPC connection found to this server"
argument_list|)
expr_stmt|;
return|return;
block|}
name|hri
operator|.
name|openRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sends an OPEN RPC to the specified server to open the specified region.    *<p>    * Open should not fail but can if server just crashed.    *<p>    * @param server server to open a region    * @param regions regions to open    */
specifier|public
name|void
name|sendRegionOpen
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInterface
name|hri
init|=
name|getServerConnection
argument_list|(
name|server
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempting to send OPEN RPC to server "
operator|+
name|server
operator|.
name|toString
argument_list|()
operator|+
literal|" failed because no RPC connection found to this server"
argument_list|)
expr_stmt|;
return|return;
block|}
name|hri
operator|.
name|openRegions
argument_list|(
name|regions
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sends an CLOSE RPC to the specified server to close the specified region.    *<p>    * A region server could reject the close request because it either does not    * have the specified region or the region is being split.    * @param server server to open a region    * @param region region to open    * @return true if server acknowledged close, false if not    * @throws IOException    */
specifier|public
name|boolean
name|sendRegionClose
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|server
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Passed server is null"
argument_list|)
throw|;
name|HRegionInterface
name|hri
init|=
name|getServerConnection
argument_list|(
name|server
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Attempting to send CLOSE RPC to server "
operator|+
name|server
operator|.
name|toString
argument_list|()
operator|+
literal|" for region "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" failed because no RPC connection found to this server"
argument_list|)
throw|;
block|}
return|return
name|hri
operator|.
name|closeRegion
argument_list|(
name|region
argument_list|)
return|;
block|}
comment|/**    * @param sn    * @return    * @throws IOException    * @throws RetriesExhaustedException wrapping a ConnectException if failed    * putting up proxy.    */
specifier|private
name|HRegionInterface
name|getServerConnection
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInterface
name|hri
init|=
name|this
operator|.
name|serverConnections
operator|.
name|get
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"New connection to "
operator|+
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hri
operator|=
name|this
operator|.
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverConnections
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
return|return
name|hri
return|;
block|}
comment|/**    * Waits for the regionservers to report in.    * @throws InterruptedException    */
specifier|public
name|void
name|waitForRegionServers
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|long
name|interval
init|=
name|this
operator|.
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.master.wait.on.regionservers.interval"
argument_list|,
literal|3000
argument_list|)
decl_stmt|;
comment|// So, number of regionservers> 0 and its been n since last check in, break,
comment|// else just stall here
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|oldcount
init|=
name|countOfRegionServers
argument_list|()
init|;
operator|!
name|this
operator|.
name|master
operator|.
name|isStopped
argument_list|()
condition|;
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|interval
argument_list|)
expr_stmt|;
name|count
operator|=
name|countOfRegionServers
argument_list|()
expr_stmt|;
if|if
condition|(
name|count
operator|==
name|oldcount
operator|&&
name|count
operator|>
literal|0
condition|)
break|break;
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on regionserver(s) to checkin"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on regionserver(s) count to settle; currently="
operator|+
name|count
argument_list|)
expr_stmt|;
block|}
name|oldcount
operator|=
name|count
expr_stmt|;
block|}
block|}
comment|/**    * @return A copy of the internal list of online servers.    */
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getOnlineServersList
parameter_list|()
block|{
comment|// TODO: optimize the load balancer call so we don't need to make a new list
comment|// TODO: FIX. THIS IS POPULAR CALL.
return|return
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|this
operator|.
name|onlineServers
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isServerOnline
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|onlineServers
operator|.
name|containsKey
argument_list|(
name|serverName
argument_list|)
return|;
block|}
specifier|public
name|void
name|shutdownCluster
parameter_list|()
block|{
name|this
operator|.
name|clusterShutdown
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|master
operator|.
name|stop
argument_list|(
literal|"Cluster shutdown requested"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isClusterShutdown
parameter_list|()
block|{
return|return
name|this
operator|.
name|clusterShutdown
return|;
block|}
comment|/**    * Stop the ServerManager.  Currently closes the connection to the master.    */
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|connection
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
literal|"Attempt to close connection to master failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

