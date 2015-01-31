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
name|Comparator
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|ClusterManager
operator|.
name|ServiceType
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
name|client
operator|.
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|RegionLocator
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
name|ServerInfo
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
name|ClientProtos
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
name|MasterProtos
operator|.
name|MasterService
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
name|Threads
import|;
end_import

begin_comment
comment|/**  * Manages the interactions with an already deployed distributed cluster (as opposed to  * a pseudo-distributed, or mini/local cluster). This is used by integration and system tests.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DistributedHBaseCluster
extends|extends
name|HBaseCluster
block|{
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
name|ClusterManager
name|clusterManager
decl_stmt|;
specifier|public
name|DistributedHBaseCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ClusterManager
name|clusterManager
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterManager
operator|=
name|clusterManager
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|admin
operator|=
name|this
operator|.
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|this
operator|.
name|initialClusterStatus
operator|=
name|getClusterStatus
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setClusterManager
parameter_list|(
name|ClusterManager
name|clusterManager
parameter_list|)
block|{
name|this
operator|.
name|clusterManager
operator|=
name|clusterManager
expr_stmt|;
block|}
specifier|public
name|ClusterManager
name|getClusterManager
parameter_list|()
block|{
return|return
name|clusterManager
return|;
block|}
comment|/**    * Returns a ClusterStatus for this HBase cluster    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|ClusterStatus
name|getClusterStatus
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|admin
operator|.
name|getClusterStatus
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterStatus
name|getInitialClusterStatus
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|initialClusterStatus
return|;
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
if|if
condition|(
name|this
operator|.
name|admin
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|connection
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|connection
operator|.
name|isClosed
argument_list|()
condition|)
block|{
name|this
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|getAdminProtocol
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
operator|(
name|ClusterConnection
operator|)
name|this
operator|.
name|connection
operator|)
operator|.
name|getAdmin
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|getClientProtocol
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
operator|(
name|ClusterConnection
operator|)
name|this
operator|.
name|connection
operator|)
operator|.
name|getClient
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startRegionServer
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting RS on: "
operator|+
name|hostname
argument_list|)
expr_stmt|;
name|clusterManager
operator|.
name|start
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
name|hostname
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|killRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting RS: "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|clusterManager
operator|.
name|kill
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|,
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stopRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping RS: "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|clusterManager
operator|.
name|stop
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|,
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitForRegionServerToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|waitForServiceToStop
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
name|serverName
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|waitForServiceToStop
parameter_list|(
name|ServiceType
name|service
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting service:"
operator|+
name|service
operator|+
literal|" to stop: "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|<
name|timeout
condition|)
block|{
if|if
condition|(
operator|!
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|service
argument_list|,
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|,
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
condition|)
block|{
return|return;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"did timeout waiting for service to stop:"
operator|+
name|serverName
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|MasterService
operator|.
name|BlockingInterface
name|getMasterAdminService
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
operator|(
name|ClusterConnection
operator|)
name|this
operator|.
name|connection
operator|)
operator|.
name|getMaster
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startMaster
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting Master on: "
operator|+
name|hostname
operator|+
literal|":"
operator|+
name|port
argument_list|)
expr_stmt|;
name|clusterManager
operator|.
name|start
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|hostname
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|killMaster
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting Master: "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|clusterManager
operator|.
name|kill
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|,
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stopMaster
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping Master: "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|clusterManager
operator|.
name|stop
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|,
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitForMasterToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|waitForServiceToStop
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|serverName
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|waitForActiveAndReadyMaster
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|<
name|timeout
condition|)
block|{
try|try
block|{
name|getMasterAdminService
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|m
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Master not started yet "
operator|+
name|m
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperConnectionException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to connect to ZK "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerHoldingRegion
parameter_list|(
name|TableName
name|tn
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionLocation
name|regionLoc
init|=
literal|null
decl_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tn
argument_list|)
init|)
block|{
name|regionLoc
operator|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionLoc
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot find region server holding region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|regionName
argument_list|)
operator|+
literal|", start key ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|HRegionInfo
operator|.
name|getStartKey
argument_list|(
name|regionName
argument_list|)
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|client
init|=
operator|(
operator|(
name|ClusterConnection
operator|)
name|this
operator|.
name|connection
operator|)
operator|.
name|getAdmin
argument_list|(
name|regionLoc
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|ServerInfo
name|info
init|=
name|ProtobufUtil
operator|.
name|getServerInfo
argument_list|(
name|client
argument_list|)
decl_stmt|;
return|return
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|info
operator|.
name|getServerName
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitUntilShutDown
parameter_list|()
block|{
comment|// Simply wait for a few seconds for now (after issuing serverManager.kill
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented yet"
argument_list|)
throw|;
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
comment|// not sure we want this
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented yet"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDistributedCluster
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|restoreClusterStatus
parameter_list|(
name|ClusterStatus
name|initial
parameter_list|)
throws|throws
name|IOException
block|{
name|ClusterStatus
name|current
init|=
name|getClusterStatus
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - started"
argument_list|)
expr_stmt|;
comment|// do a best effort restore
name|boolean
name|success
init|=
literal|true
decl_stmt|;
name|success
operator|=
name|restoreMasters
argument_list|(
name|initial
argument_list|,
name|current
argument_list|)
operator|&
name|success
expr_stmt|;
name|success
operator|=
name|restoreRegionServers
argument_list|(
name|initial
argument_list|,
name|current
argument_list|)
operator|&
name|success
expr_stmt|;
name|success
operator|=
name|restoreAdmin
argument_list|()
operator|&
name|success
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - done"
argument_list|)
expr_stmt|;
return|return
name|success
return|;
block|}
specifier|protected
name|boolean
name|restoreMasters
parameter_list|(
name|ClusterStatus
name|initial
parameter_list|,
name|ClusterStatus
name|current
parameter_list|)
block|{
name|List
argument_list|<
name|IOException
argument_list|>
name|deferred
init|=
operator|new
name|ArrayList
argument_list|<
name|IOException
argument_list|>
argument_list|()
decl_stmt|;
comment|//check whether current master has changed
specifier|final
name|ServerName
name|initMaster
init|=
name|initial
operator|.
name|getMaster
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|ServerName
operator|.
name|isSameHostnameAndPort
argument_list|(
name|initMaster
argument_list|,
name|current
operator|.
name|getMaster
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - Initial active master : "
operator|+
name|initMaster
operator|.
name|getHostAndPort
argument_list|()
operator|+
literal|" has changed to : "
operator|+
name|current
operator|.
name|getMaster
argument_list|()
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
comment|// If initial master is stopped, start it, before restoring the state.
comment|// It will come up as a backup master, if there is already an active master.
try|try
block|{
if|if
condition|(
operator|!
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|initMaster
operator|.
name|getHostname
argument_list|()
argument_list|,
name|initMaster
operator|.
name|getPort
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - starting initial active master at:"
operator|+
name|initMaster
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
name|startMaster
argument_list|(
name|initMaster
operator|.
name|getHostname
argument_list|()
argument_list|,
name|initMaster
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// master has changed, we would like to undo this.
comment|// 1. Kill the current backups
comment|// 2. Stop current master
comment|// 3. Start backup masters
for|for
control|(
name|ServerName
name|currentBackup
range|:
name|current
operator|.
name|getBackupMasters
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|ServerName
operator|.
name|isSameHostnameAndPort
argument_list|(
name|currentBackup
argument_list|,
name|initMaster
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - stopping backup master: "
operator|+
name|currentBackup
argument_list|)
expr_stmt|;
name|stopMaster
argument_list|(
name|currentBackup
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - stopping active master: "
operator|+
name|current
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
name|stopMaster
argument_list|(
name|current
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
comment|// wait so that active master takes over
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// if we fail to start the initial active master, we do not want to continue stopping
comment|// backup masters. Just keep what we have now
name|deferred
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
comment|//start backup masters
for|for
control|(
name|ServerName
name|backup
range|:
name|initial
operator|.
name|getBackupMasters
argument_list|()
control|)
block|{
try|try
block|{
comment|//these are not started in backup mode, but we should already have an active master
if|if
condition|(
operator|!
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
name|backup
operator|.
name|getHostname
argument_list|()
argument_list|,
name|backup
operator|.
name|getPort
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - starting initial backup master: "
operator|+
name|backup
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
name|startMaster
argument_list|(
name|backup
operator|.
name|getHostname
argument_list|()
argument_list|,
name|backup
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|deferred
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|//current master has not changed, match up backup masters
name|Set
argument_list|<
name|ServerName
argument_list|>
name|toStart
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerName
argument_list|>
argument_list|(
operator|new
name|ServerNameIgnoreStartCodeComparator
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|toKill
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerName
argument_list|>
argument_list|(
operator|new
name|ServerNameIgnoreStartCodeComparator
argument_list|()
argument_list|)
decl_stmt|;
name|toStart
operator|.
name|addAll
argument_list|(
name|initial
operator|.
name|getBackupMasters
argument_list|()
argument_list|)
expr_stmt|;
name|toKill
operator|.
name|addAll
argument_list|(
name|current
operator|.
name|getBackupMasters
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|current
operator|.
name|getBackupMasters
argument_list|()
control|)
block|{
name|toStart
operator|.
name|remove
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ServerName
name|server
range|:
name|initial
operator|.
name|getBackupMasters
argument_list|()
control|)
block|{
name|toKill
operator|.
name|remove
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ServerName
name|sn
range|:
name|toStart
control|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
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
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - starting initial backup master: "
operator|+
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
name|startMaster
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
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|deferred
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|ServerName
name|sn
range|:
name|toKill
control|)
block|{
try|try
block|{
if|if
condition|(
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|ServiceType
operator|.
name|HBASE_MASTER
argument_list|,
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
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - stopping backup master: "
operator|+
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
name|stopMaster
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|deferred
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|deferred
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Restoring cluster - restoring region servers reported "
operator|+
name|deferred
operator|.
name|size
argument_list|()
operator|+
literal|" errors:"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|deferred
operator|.
name|size
argument_list|()
operator|&&
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|deferred
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|deferred
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|private
specifier|static
class|class
name|ServerNameIgnoreStartCodeComparator
implements|implements
name|Comparator
argument_list|<
name|ServerName
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|ServerName
name|o1
parameter_list|,
name|ServerName
name|o2
parameter_list|)
block|{
name|int
name|compare
init|=
name|o1
operator|.
name|getHostname
argument_list|()
operator|.
name|compareToIgnoreCase
argument_list|(
name|o2
operator|.
name|getHostname
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
name|compare
operator|=
name|o1
operator|.
name|getPort
argument_list|()
operator|-
name|o2
operator|.
name|getPort
argument_list|()
expr_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
return|return
literal|0
return|;
block|}
block|}
specifier|protected
name|boolean
name|restoreRegionServers
parameter_list|(
name|ClusterStatus
name|initial
parameter_list|,
name|ClusterStatus
name|current
parameter_list|)
block|{
name|Set
argument_list|<
name|ServerName
argument_list|>
name|toStart
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerName
argument_list|>
argument_list|(
operator|new
name|ServerNameIgnoreStartCodeComparator
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|toKill
init|=
operator|new
name|TreeSet
argument_list|<
name|ServerName
argument_list|>
argument_list|(
operator|new
name|ServerNameIgnoreStartCodeComparator
argument_list|()
argument_list|)
decl_stmt|;
name|toStart
operator|.
name|addAll
argument_list|(
name|initial
operator|.
name|getBackupMasters
argument_list|()
argument_list|)
expr_stmt|;
name|toKill
operator|.
name|addAll
argument_list|(
name|current
operator|.
name|getBackupMasters
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|current
operator|.
name|getServers
argument_list|()
control|)
block|{
name|toStart
operator|.
name|remove
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ServerName
name|server
range|:
name|initial
operator|.
name|getServers
argument_list|()
control|)
block|{
name|toKill
operator|.
name|remove
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|IOException
argument_list|>
name|deferred
init|=
operator|new
name|ArrayList
argument_list|<
name|IOException
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|toStart
control|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
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
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - starting initial region server: "
operator|+
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
name|startRegionServer
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
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|deferred
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|ServerName
name|sn
range|:
name|toKill
control|)
block|{
try|try
block|{
if|if
condition|(
name|clusterManager
operator|.
name|isRunning
argument_list|(
name|ServiceType
operator|.
name|HBASE_REGIONSERVER
argument_list|,
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
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster - stopping initial region server: "
operator|+
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
name|stopRegionServer
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|deferred
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|deferred
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Restoring cluster - restoring region servers reported "
operator|+
name|deferred
operator|.
name|size
argument_list|()
operator|+
literal|" errors:"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|deferred
operator|.
name|size
argument_list|()
operator|&&
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|deferred
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|deferred
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|protected
name|boolean
name|restoreAdmin
parameter_list|()
throws|throws
name|IOException
block|{
comment|// While restoring above, if the HBase Master which was initially the Active one, was down
comment|// and the restore put the cluster back to Initial configuration, HAdmin instance will need
comment|// to refresh its connections (otherwise it will return incorrect information) or we can
comment|// point it to new instance.
try|try
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"While closing the old connection"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|admin
operator|=
name|this
operator|.
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added new HBaseAdmin"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

