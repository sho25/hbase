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
name|Chore
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
name|HMsg
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
name|HServerInfo
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
name|NotServingRegionException
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
name|Stoppable
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
name|client
operator|.
name|ServerConnection
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
name|ServerConnectionManager
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
name|ServerShutdownHandler
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
name|metrics
operator|.
name|MasterMetrics
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
name|Leases
operator|.
name|LeaseStillHeldException
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
name|Threads
import|;
end_import

begin_comment
comment|/**  * The ServerManager class manages info about region servers - HServerInfo,  * load numbers, dying servers, etc.  *<p>  * Maintains lists of online and dead servers.  Processes the startups,  * shutdowns, and deaths of region servers.  *<p>  * Servers are distinguished in two different ways.  A given server has a  * location, specified by hostname and port, and of which there can only be one  * online at any given time.  A server instance is specified by the location  * (hostname and port) as well as the startcode (timestamp from when the server  * was started).  This is used to differentiate a restarted instance of a given  * server from the original instance.  */
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
comment|/** The map of known server names to server info */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HServerInfo
argument_list|>
name|onlineServers
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|HServerInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// TODO: This is strange to have two maps but HSI above is used on both sides
comment|/**    * Map from full server-instance name to the RPC connection for this server.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HRegionInterface
argument_list|>
name|serverConnections
init|=
operator|new
name|HashMap
argument_list|<
name|String
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
name|ServerMonitor
name|serverMonitorThread
decl_stmt|;
specifier|private
name|int
name|minimumServerCount
decl_stmt|;
specifier|private
specifier|final
name|LogCleaner
name|logCleaner
decl_stmt|;
comment|// Reporting to track master metrics.
specifier|private
specifier|final
name|MasterMetrics
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|DeadServer
name|deadservers
init|=
operator|new
name|DeadServer
argument_list|()
decl_stmt|;
comment|/**    * Dumps into log current stats on dead servers and number of servers    * TODO: Make this a metric; dump metrics into log.    */
class|class
name|ServerMonitor
extends|extends
name|Chore
block|{
name|ServerMonitor
parameter_list|(
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|Stoppable
name|stopper
parameter_list|)
block|{
name|super
argument_list|(
literal|"ServerMonitor"
argument_list|,
name|period
argument_list|,
name|stopper
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
name|int
name|numServers
init|=
name|numServers
argument_list|()
decl_stmt|;
name|int
name|numDeadServers
init|=
name|deadservers
operator|.
name|size
argument_list|()
decl_stmt|;
name|double
name|averageLoad
init|=
name|getAverageLoad
argument_list|()
decl_stmt|;
name|String
name|deadServersList
init|=
name|deadservers
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|numServers
operator|+
literal|" region servers, "
operator|+
name|numDeadServers
operator|+
literal|" dead, average load "
operator|+
name|averageLoad
operator|+
operator|(
operator|(
name|deadServersList
operator|!=
literal|null
operator|&&
name|deadServersList
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|)
condition|?
name|deadServersList
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Constructor.    * @param master    * @param services    */
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
name|int
name|metaRescanInterval
init|=
name|c
operator|.
name|getInt
argument_list|(
literal|"hbase.master.meta.thread.rescanfrequency"
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|this
operator|.
name|minimumServerCount
operator|=
name|c
operator|.
name|getInt
argument_list|(
literal|"hbase.regions.server.count.min"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
operator|new
name|MasterMetrics
argument_list|(
name|master
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverMonitorThread
operator|=
operator|new
name|ServerMonitor
argument_list|(
name|metaRescanInterval
argument_list|,
name|master
argument_list|)
expr_stmt|;
name|String
name|n
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|this
operator|.
name|serverMonitorThread
argument_list|,
name|n
operator|+
literal|".serverMonitor"
argument_list|)
expr_stmt|;
name|this
operator|.
name|logCleaner
operator|=
operator|new
name|LogCleaner
argument_list|(
name|c
operator|.
name|getInt
argument_list|(
literal|"hbase.master.meta.thread.rescanfrequency"
argument_list|,
literal|60
operator|*
literal|1000
argument_list|)
argument_list|,
name|master
argument_list|,
name|c
argument_list|,
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getOldLogDir
argument_list|()
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|logCleaner
argument_list|,
name|n
operator|+
literal|".oldLogCleaner"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Let the server manager know a new regionserver has come online    * @param serverInfo    * @throws IOException    */
name|void
name|regionServerStartup
parameter_list|(
specifier|final
name|HServerInfo
name|serverInfo
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
name|HServerInfo
name|info
init|=
operator|new
name|HServerInfo
argument_list|(
name|serverInfo
argument_list|)
decl_stmt|;
name|String
name|hostAndPort
init|=
name|info
operator|.
name|getServerAddress
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|HServerInfo
name|existingServer
init|=
name|haveServerWithSameHostAndPortAlready
argument_list|(
name|info
operator|.
name|getHostnamePort
argument_list|()
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
literal|"Server start rejected; we already have "
operator|+
name|hostAndPort
operator|+
literal|" registered; existingServer="
operator|+
name|existingServer
operator|+
literal|", newServer="
operator|+
name|info
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
name|getStartCode
argument_list|()
operator|<
name|info
operator|.
name|getStartCode
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Triggering server recovery; existingServer looks stale"
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
name|checkIsDead
argument_list|(
name|info
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|"STARTUP"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Received start message from: "
operator|+
name|info
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|recordNewServer
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HServerInfo
name|haveServerWithSameHostAndPortAlready
parameter_list|(
specifier|final
name|String
name|hostnamePort
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|onlineServers
init|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|HServerInfo
argument_list|>
name|e
range|:
name|this
operator|.
name|onlineServers
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getHostnamePort
argument_list|()
operator|.
name|equals
argument_list|(
name|hostnamePort
argument_list|)
condition|)
block|{
return|return
name|e
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * If this server is on the dead list, reject it with a LeaseStillHeldException    * @param serverName Server name formatted as host_port_startcode.    * @param what START or REPORT    * @throws LeaseStillHeldException    */
specifier|private
name|void
name|checkIsDead
parameter_list|(
specifier|final
name|String
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
operator|!
name|this
operator|.
name|deadservers
operator|.
name|isDeadServer
argument_list|(
name|serverName
argument_list|)
condition|)
return|return;
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
comment|/**    * Adds the HSI to the RS list and creates an empty load    * @param info The region server informations    */
specifier|public
name|void
name|recordNewServer
parameter_list|(
name|HServerInfo
name|info
parameter_list|)
block|{
name|recordNewServer
argument_list|(
name|info
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds the HSI to the RS list    * @param info The region server informations    * @param useInfoLoad True if the load from the info should be used    *                    like under a master failover    */
name|void
name|recordNewServer
parameter_list|(
name|HServerInfo
name|info
parameter_list|,
name|boolean
name|useInfoLoad
parameter_list|,
name|HRegionInterface
name|hri
parameter_list|)
block|{
name|HServerLoad
name|load
init|=
name|useInfoLoad
condition|?
name|info
operator|.
name|getLoad
argument_list|()
else|:
operator|new
name|HServerLoad
argument_list|()
decl_stmt|;
name|String
name|serverName
init|=
name|info
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|info
operator|.
name|setLoad
argument_list|(
name|load
argument_list|)
expr_stmt|;
comment|// TODO: Why did we update the RS location ourself?  Shouldn't RS do this?
comment|// masterStatus.getZooKeeper().updateRSLocationGetWatch(info, watcher);
name|onlineServers
operator|.
name|put
argument_list|(
name|serverName
argument_list|,
name|info
argument_list|)
expr_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|serverConnections
operator|.
name|remove
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serverConnections
operator|.
name|put
argument_list|(
name|serverName
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Called to process the messages sent from the region server to the master    * along with the heart beat.    *    * @param serverInfo    * @param msgs    * @param mostLoadedRegions Array of regions the region server is submitting    * as candidates to be rebalanced, should it be overloaded    * @return messages from master to region server indicating what region    * server should do.    *    * @throws IOException    */
name|HMsg
index|[]
name|regionServerReport
parameter_list|(
specifier|final
name|HServerInfo
name|serverInfo
parameter_list|,
specifier|final
name|HMsg
index|[]
name|msgs
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|mostLoadedRegions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Be careful. This method does returns in the middle.
name|HServerInfo
name|info
init|=
operator|new
name|HServerInfo
argument_list|(
name|serverInfo
argument_list|)
decl_stmt|;
comment|// Check if dead.  If it is, it'll get a 'You Are Dead!' exception.
name|checkIsDead
argument_list|(
name|info
operator|.
name|getServerName
argument_list|()
argument_list|,
literal|"REPORT"
argument_list|)
expr_stmt|;
comment|// If we don't know this server, tell it shutdown.
name|HServerInfo
name|storedInfo
init|=
name|this
operator|.
name|onlineServers
operator|.
name|get
argument_list|(
name|info
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|storedInfo
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received report from unknown server -- telling it "
operator|+
literal|"to "
operator|+
name|HMsg
operator|.
name|Type
operator|.
name|STOP_REGIONSERVER
operator|+
literal|": "
operator|+
name|info
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|HMsg
operator|.
name|STOP_REGIONSERVER_ARRAY
return|;
block|}
comment|// Check startcodes
if|if
condition|(
name|raceThatShouldNotHappenAnymore
argument_list|(
name|storedInfo
argument_list|,
name|info
argument_list|)
condition|)
block|{
return|return
name|HMsg
operator|.
name|STOP_REGIONSERVER_ARRAY
return|;
block|}
for|for
control|(
name|HMsg
name|msg
range|:
name|msgs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received "
operator|+
name|msg
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|msg
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|REGION_SPLIT
case|:
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|handleSplitReport
argument_list|(
name|serverInfo
argument_list|,
name|msg
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|msg
operator|.
name|getDaughterA
argument_list|()
argument_list|,
name|msg
operator|.
name|getDaughterB
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
name|LOG
operator|.
name|error
argument_list|(
literal|"Unhandled msg type "
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
name|HMsg
index|[]
name|reply
init|=
literal|null
decl_stmt|;
name|int
name|numservers
init|=
name|numServers
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|clusterShutdown
condition|)
block|{
if|if
condition|(
name|numservers
operator|<=
literal|2
condition|)
block|{
comment|// Shutdown needs to be staggered; the meta regions need to close last
comment|// in case they need to be updated during the close melee.  If<= 2
comment|// servers left, then these are the two that were carrying root and meta
comment|// most likely (TODO: This presumes unsplittable meta -- FIX). Tell
comment|// these servers can shutdown now too.
name|reply
operator|=
name|HMsg
operator|.
name|STOP_REGIONSERVER_ARRAY
expr_stmt|;
block|}
block|}
return|return
name|processRegionServerAllsWell
argument_list|(
name|info
argument_list|,
name|mostLoadedRegions
argument_list|,
name|reply
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|raceThatShouldNotHappenAnymore
parameter_list|(
specifier|final
name|HServerInfo
name|storedInfo
parameter_list|,
specifier|final
name|HServerInfo
name|reportedInfo
parameter_list|)
block|{
if|if
condition|(
name|storedInfo
operator|.
name|getStartCode
argument_list|()
operator|!=
name|reportedInfo
operator|.
name|getStartCode
argument_list|()
condition|)
block|{
comment|// TODO: I don't think this possible any more.  We check startcodes when
comment|// server comes in on regionServerStartup -- St.Ack
comment|// This state is reachable if:
comment|// 1) RegionServer A started
comment|// 2) RegionServer B started on the same machine, then clobbered A in regionServerStartup.
comment|// 3) RegionServer A returns, expecting to work as usual.
comment|// The answer is to ask A to shut down for good.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Race condition detected: "
operator|+
name|reportedInfo
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|onlineServers
init|)
block|{
name|removeServerInfo
argument_list|(
name|reportedInfo
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|notifyOnlineServers
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    *  RegionServer is checking in, no exceptional circumstances    * @param serverInfo    * @param mostLoadedRegions    * @param msgs    * @return    * @throws IOException    */
specifier|private
name|HMsg
index|[]
name|processRegionServerAllsWell
parameter_list|(
name|HServerInfo
name|serverInfo
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|mostLoadedRegions
parameter_list|,
name|HMsg
index|[]
name|msgs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Refresh the info object and the load information
name|this
operator|.
name|onlineServers
operator|.
name|put
argument_list|(
name|serverInfo
operator|.
name|getServerName
argument_list|()
argument_list|,
name|serverInfo
argument_list|)
expr_stmt|;
name|HServerLoad
name|load
init|=
name|serverInfo
operator|.
name|getLoad
argument_list|()
decl_stmt|;
if|if
condition|(
name|load
operator|!=
literal|null
operator|&&
name|this
operator|.
name|metrics
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|metrics
operator|.
name|incrementRequests
argument_list|(
name|load
operator|.
name|getNumberOfRequests
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// No more piggyback messages on heartbeats for other stuff
return|return
name|msgs
return|;
block|}
comment|/**    * @param serverName    * @return True if we removed server from the list.    */
specifier|private
name|boolean
name|removeServerInfo
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|HServerInfo
name|info
init|=
name|this
operator|.
name|onlineServers
operator|.
name|remove
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
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
name|HServerInfo
name|hsi
range|:
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
name|hsi
operator|.
name|getLoad
argument_list|()
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
comment|/** @return the number of active servers */
specifier|public
name|int
name|numServers
parameter_list|()
block|{
name|int
name|num
init|=
operator|-
literal|1
decl_stmt|;
comment|// This synchronized seems gratuitous.
synchronized|synchronized
init|(
name|this
operator|.
name|onlineServers
init|)
block|{
name|num
operator|=
name|this
operator|.
name|onlineServers
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|num
return|;
block|}
comment|/**    * @param name server name    * @return HServerInfo for the given server address    */
specifier|public
name|HServerInfo
name|getServerInfo
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|this
operator|.
name|onlineServers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * @return Read-only map of servers to serverinfo    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|HServerInfo
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
name|String
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
comment|/**    * @param hsa    * @return The HServerInfo whose HServerAddress is<code>hsa</code> or null    * if nothing found.    */
specifier|public
name|HServerInfo
name|getHServerInfo
parameter_list|(
specifier|final
name|HServerAddress
name|hsa
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|onlineServers
init|)
block|{
comment|// TODO: This is primitive.  Do a better search.
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|HServerInfo
argument_list|>
name|e
range|:
name|this
operator|.
name|onlineServers
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getServerAddress
argument_list|()
operator|.
name|equals
argument_list|(
name|hsa
argument_list|)
condition|)
block|{
return|return
name|e
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|notifyOnlineServers
parameter_list|()
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|onlineServers
init|)
block|{
name|this
operator|.
name|onlineServers
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    * Wait on regionservers to report in    * with {@link #regionServerReport(HServerInfo, HMsg[])} so they get notice    * the master is going down.  Waits until all region servers come back with    * a MSG_REGIONSERVER_STOP.    */
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
name|onlineServers
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on following regionserver(s) to go down "
operator|+
name|this
operator|.
name|onlineServers
operator|.
name|values
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
literal|500
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
name|HServerInfo
name|hsi
parameter_list|)
block|{
comment|// First check a server to expire.  ServerName is of the form:
comment|//<hostname> ,<port> ,<startcode>
name|String
name|serverName
init|=
name|hsi
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|HServerInfo
name|info
init|=
name|this
operator|.
name|onlineServers
operator|.
name|get
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received expiration of "
operator|+
name|hsi
operator|.
name|getServerName
argument_list|()
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
name|hsi
operator|.
name|getServerName
argument_list|()
operator|+
literal|" but server shutdown is already in progress"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Remove the server from the known servers lists and update load info
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
name|hsi
operator|.
name|getServerName
argument_list|()
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
name|deadservers
argument_list|,
name|info
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Added="
operator|+
name|serverName
operator|+
literal|" to dead servers, submitted shutdown handler to be executed"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|canAssignUserRegions
parameter_list|()
block|{
if|if
condition|(
name|minimumServerCount
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
operator|(
name|numServers
argument_list|()
operator|>=
name|minimumServerCount
operator|)
return|;
block|}
specifier|public
name|void
name|setMinimumServerCount
parameter_list|(
name|int
name|minimumServerCount
parameter_list|)
block|{
name|this
operator|.
name|minimumServerCount
operator|=
name|minimumServerCount
expr_stmt|;
block|}
comment|// RPC methods to region servers
comment|/**    * Sends an OPEN RPC to the specified server to open the specified region.    *<p>    * Open should not fail but can if server just crashed.    *<p>    * @param server server to open a region    * @param regionName region to open    */
specifier|public
name|void
name|sendRegionOpen
parameter_list|(
name|HServerInfo
name|server
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
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
name|getServerName
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
comment|/**    * Sends an CLOSE RPC to the specified server to close the specified region.    *<p>    * A region server could reject the close request because it either does not    * have the specified region or the region is being split.    * @param server server to open a region    * @param regionName region to open    * @return true if server acknowledged close, false if not    * @throws NotServingRegionException    */
specifier|public
name|void
name|sendRegionClose
parameter_list|(
name|HServerInfo
name|server
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|NotServingRegionException
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
literal|"Attempting to send CLOSE RPC to server "
operator|+
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|" failed because no RPC connection found to this server"
argument_list|)
expr_stmt|;
return|return;
block|}
name|hri
operator|.
name|closeRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegionInterface
name|getServerConnection
parameter_list|(
name|HServerInfo
name|info
parameter_list|)
block|{
try|try
block|{
name|ServerConnection
name|connection
init|=
name|ServerConnectionManager
operator|.
name|getConnection
argument_list|(
name|this
operator|.
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionInterface
name|hri
init|=
name|serverConnections
operator|.
name|get
argument_list|(
name|info
operator|.
name|getServerName
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
name|info
argument_list|(
literal|"new connection"
argument_list|)
expr_stmt|;
name|hri
operator|=
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|info
operator|.
name|getServerAddress
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|serverConnections
operator|.
name|put
argument_list|(
name|info
operator|.
name|getServerName
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
return|return
name|hri
return|;
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
literal|"Error connecting to region server"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Fatal error connection to RS"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Waits for the minimum number of servers to be running.    */
specifier|public
name|void
name|waitForMinServers
parameter_list|()
block|{
while|while
condition|(
name|numServers
argument_list|()
operator|<
name|minimumServerCount
condition|)
block|{
comment|//        !masterStatus.getShutdownRequested().get()) {
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for enough servers to check in.  Currently have "
operator|+
name|numServers
argument_list|()
operator|+
literal|" but need at least "
operator|+
name|minimumServerCount
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got interrupted waiting for servers to check in, looping"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|List
argument_list|<
name|HServerInfo
argument_list|>
name|getOnlineServersList
parameter_list|()
block|{
comment|// TODO: optimize the load balancer call so we don't need to make a new list
return|return
operator|new
name|ArrayList
argument_list|<
name|HServerInfo
argument_list|>
argument_list|(
name|onlineServers
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isServerOnline
parameter_list|(
name|String
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster shutdown requested"
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterShutdown
operator|=
literal|true
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
block|}
end_class

end_unit

