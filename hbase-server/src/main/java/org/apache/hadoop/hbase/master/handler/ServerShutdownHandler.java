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
name|master
operator|.
name|handler
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
name|io
operator|.
name|InterruptedIOException
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
name|locks
operator|.
name|Lock
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
name|executor
operator|.
name|EventHandler
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
name|executor
operator|.
name|EventType
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
name|AssignmentManager
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
name|DeadServer
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
name|MasterFileSystem
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
name|master
operator|.
name|RegionState
operator|.
name|State
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
name|RegionStates
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
name|ServerManager
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
name|SplitLogTask
operator|.
name|RecoveryMode
import|;
end_import

begin_comment
comment|/**  * Process server shutdown.  * Server-to-handle must be already in the deadservers lists.  See  * {@link ServerManager#expireServer(ServerName)}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ServerShutdownHandler
extends|extends
name|EventHandler
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
name|ServerShutdownHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|protected
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|protected
specifier|final
name|DeadServer
name|deadServers
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|shouldSplitHlog
decl_stmt|;
comment|// whether to split HLog or not
specifier|protected
specifier|final
name|int
name|regionAssignmentWaitTimeout
decl_stmt|;
specifier|public
name|ServerShutdownHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|DeadServer
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|boolean
name|shouldSplitHlog
parameter_list|)
block|{
name|this
argument_list|(
name|server
argument_list|,
name|services
argument_list|,
name|deadServers
argument_list|,
name|serverName
argument_list|,
name|EventType
operator|.
name|M_SERVER_SHUTDOWN
argument_list|,
name|shouldSplitHlog
argument_list|)
expr_stmt|;
block|}
name|ServerShutdownHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|DeadServer
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
name|EventType
name|type
parameter_list|,
specifier|final
name|boolean
name|shouldSplitHlog
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|this
operator|.
name|deadServers
operator|=
name|deadServers
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|deadServers
operator|.
name|isDeadServer
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|this
operator|.
name|serverName
operator|+
literal|" is NOT in deadservers; it should be!"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|shouldSplitHlog
operator|=
name|shouldSplitHlog
expr_stmt|;
name|this
operator|.
name|regionAssignmentWaitTimeout
operator|=
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|LOG_REPLAY_WAIT_REGION_TIMEOUT
argument_list|,
literal|15000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getInformativeName
parameter_list|()
block|{
if|if
condition|(
name|serverName
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" for "
operator|+
name|serverName
return|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|getInformativeName
argument_list|()
return|;
block|}
block|}
comment|/**    * @return True if the server we are processing was carrying<code>hbase:meta</code>    */
name|boolean
name|isCarryingMeta
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|serverName
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|hasLogReplayWork
init|=
literal|false
decl_stmt|;
specifier|final
name|ServerName
name|serverName
init|=
name|this
operator|.
name|serverName
decl_stmt|;
try|try
block|{
comment|// We don't want worker thread in the MetaServerShutdownHandler
comment|// executor pool to block by waiting availability of hbase:meta
comment|// Otherwise, it could run into the following issue:
comment|// 1. The current MetaServerShutdownHandler instance For RS1 waits for the hbase:meta
comment|//    to come online.
comment|// 2. The newly assigned hbase:meta region server RS2 was shutdown right after
comment|//    it opens the hbase:meta region. So the MetaServerShutdownHandler
comment|//    instance For RS1 will still be blocked.
comment|// 3. The new instance of MetaServerShutdownHandler for RS2 is queued.
comment|// 4. The newly assigned hbase:meta region server RS3 was shutdown right after
comment|//    it opens the hbase:meta region. So the MetaServerShutdownHandler
comment|//    instance For RS1 and RS2 will still be blocked.
comment|// 5. The new instance of MetaServerShutdownHandler for RS3 is queued.
comment|// 6. Repeat until we run out of MetaServerShutdownHandler worker threads
comment|// The solution here is to resubmit a ServerShutdownHandler request to process
comment|// user regions on that server so that MetaServerShutdownHandler
comment|// executor pool is always available.
comment|//
comment|// If AssignmentManager hasn't finished rebuilding user regions,
comment|// we are not ready to assign dead regions either. So we re-queue up
comment|// the dead server for further processing too.
name|AssignmentManager
name|am
init|=
name|services
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|isCarryingMeta
argument_list|()
comment|// hbase:meta
operator|||
operator|!
name|am
operator|.
name|isFailoverCleanupDone
argument_list|()
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|processDeadServer
argument_list|(
name|serverName
argument_list|,
name|this
operator|.
name|shouldSplitHlog
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Wait on meta to come online; we need it to progress.
comment|// TODO: Best way to hold strictly here?  We should build this retry logic
comment|// into the MetaTableAccessor operations themselves.
comment|// TODO: Is the reading of hbase:meta necessary when the Master has state of
comment|// cluster in its head?  It should be possible to do without reading hbase:meta
comment|// in all but one case. On split, the RS updates the hbase:meta
comment|// table and THEN informs the master of the split via zk nodes in
comment|// 'unassigned' dir.  Currently the RS puts ephemeral nodes into zk so if
comment|// the regionserver dies, these nodes do not stick around and this server
comment|// shutdown processing does fixup (see the fixupDaughters method below).
comment|// If we wanted to skip the hbase:meta scan, we'd have to change at least the
comment|// final SPLIT message to be permanent in zk so in here we'd know a SPLIT
comment|// completed (zk is updated after edits to hbase:meta have gone in).  See
comment|// {@link SplitTransaction}.  We'd also have to be figure another way for
comment|// doing the below hbase:meta daughters fixup.
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
try|try
block|{
name|server
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|waitMetaRegionLocation
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
name|hris
operator|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getServerRegions
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
break|break;
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
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Server is stopped"
argument_list|)
throw|;
block|}
comment|// delayed to set recovery mode based on configuration only after all outstanding splitlogtask
comment|// drained
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|setLogRecoveryMode
argument_list|()
expr_stmt|;
name|boolean
name|distributedLogReplay
init|=
operator|(
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getLogRecoveryMode
argument_list|()
operator|==
name|RecoveryMode
operator|.
name|LOG_REPLAY
operator|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|shouldSplitHlog
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting logs for "
operator|+
name|serverName
operator|+
literal|" before assignment."
argument_list|)
expr_stmt|;
if|if
condition|(
name|distributedLogReplay
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Mark regions in recovery before assignment."
argument_list|)
expr_stmt|;
name|MasterFileSystem
name|mfs
init|=
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|mfs
operator|.
name|prepareLogReplay
argument_list|(
name|serverName
argument_list|,
name|hris
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|splitLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|logSplit
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping log splitting for "
operator|+
name|serverName
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
name|resubmit
argument_list|(
name|serverName
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
comment|// Clean out anything in regions in transition.  Being conservative and
comment|// doing after log splitting.  Could do some states before -- OPENING?
comment|// OFFLINE? -- and then others after like CLOSING that depend on log
comment|// splitting.
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsInTransition
init|=
name|am
operator|.
name|processServerShutdown
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Reassigning "
operator|+
operator|(
operator|(
name|hris
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|hris
operator|.
name|size
argument_list|()
operator|)
operator|+
literal|" region(s) that "
operator|+
operator|(
name|serverName
operator|==
literal|null
condition|?
literal|"null"
else|:
name|serverName
operator|)
operator|+
literal|" was carrying (and "
operator|+
name|regionsInTransition
operator|.
name|size
argument_list|()
operator|+
literal|" regions(s) that were opening on this server)"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|toAssignRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
name|toAssignRegions
operator|.
name|addAll
argument_list|(
name|regionsInTransition
argument_list|)
expr_stmt|;
comment|// Iterate regions that were on this server and assign them
if|if
condition|(
name|hris
operator|!=
literal|null
operator|&&
operator|!
name|hris
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|RegionStates
name|regionStates
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
if|if
condition|(
name|regionsInTransition
operator|.
name|contains
argument_list|(
name|hri
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|String
name|encodedName
init|=
name|hri
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|Lock
name|lock
init|=
name|am
operator|.
name|acquireRegionLock
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
try|try
block|{
name|RegionState
name|rit
init|=
name|regionStates
operator|.
name|getRegionTransitionState
argument_list|(
name|hri
argument_list|)
decl_stmt|;
if|if
condition|(
name|processDeadRegion
argument_list|(
name|hri
argument_list|,
name|am
argument_list|)
condition|)
block|{
name|ServerName
name|addressFromAM
init|=
name|regionStates
operator|.
name|getRegionServerOfRegion
argument_list|(
name|hri
argument_list|)
decl_stmt|;
if|if
condition|(
name|addressFromAM
operator|!=
literal|null
operator|&&
operator|!
name|addressFromAM
operator|.
name|equals
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
condition|)
block|{
comment|// If this region is in transition on the dead server, it must be
comment|// opening or pending_open, which should have been covered by AM#processServerShutdown
name|LOG
operator|.
name|info
argument_list|(
literal|"Skip assigning region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" because it has been opened in "
operator|+
name|addressFromAM
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|rit
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|rit
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|rit
operator|.
name|isOnServer
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// Skip regions that are in transition on other server
name|LOG
operator|.
name|info
argument_list|(
literal|"Skip assigning region in transition on other server"
operator|+
name|rit
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Reassigning region with rs = "
operator|+
name|rit
argument_list|)
expr_stmt|;
name|regionStates
operator|.
name|updateRegionState
argument_list|(
name|hri
argument_list|,
name|State
operator|.
name|OFFLINE
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|regionStates
operator|.
name|isRegionInState
argument_list|(
name|hri
argument_list|,
name|State
operator|.
name|SPLITTING_NEW
argument_list|,
name|State
operator|.
name|MERGING_NEW
argument_list|)
condition|)
block|{
name|regionStates
operator|.
name|updateRegionState
argument_list|(
name|hri
argument_list|,
name|State
operator|.
name|OFFLINE
argument_list|)
expr_stmt|;
block|}
name|toAssignRegions
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rit
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|rit
operator|.
name|isPendingCloseOrClosing
argument_list|()
operator|&&
name|am
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLED
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|)
operator|||
name|am
operator|.
name|getReplicasToClose
argument_list|()
operator|.
name|contains
argument_list|(
name|hri
argument_list|)
condition|)
block|{
comment|// If the table was partially disabled and the RS went down, we should clear the RIT
comment|// and remove the node for the region.
comment|// The rit that we use may be stale in case the table was in DISABLING state
comment|// but though we did assign we will not be clearing the znode in CLOSING state.
comment|// Doing this will have no harm. See HBASE-5927
name|regionStates
operator|.
name|updateRegionState
argument_list|(
name|hri
argument_list|,
name|State
operator|.
name|OFFLINE
argument_list|)
expr_stmt|;
name|am
operator|.
name|offlineDisabledRegion
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"THIS SHOULD NOT HAPPEN: unexpected region in transition "
operator|+
name|rit
operator|+
literal|" not to be assigned by SSH of server "
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
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
block|}
try|try
block|{
name|am
operator|.
name|assign
argument_list|(
name|toAssignRegions
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught "
operator|+
name|ie
operator|+
literal|" during round-robin assignment"
argument_list|)
expr_stmt|;
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|ie
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|shouldSplitHlog
operator|&&
name|distributedLogReplay
condition|)
block|{
comment|// wait for region assignment completes
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|toAssignRegions
control|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|am
operator|.
name|waitOnRegionToClearRegionsInTransition
argument_list|(
name|hri
argument_list|,
name|regionAssignmentWaitTimeout
argument_list|)
condition|)
block|{
comment|// Wait here is to avoid log replay hits current dead server and incur a RPC timeout
comment|// when replay happens before region assignment completes.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region "
operator|+
name|hri
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" didn't complete assignment in time"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Caught "
operator|+
name|ie
operator|+
literal|" during waitOnRegionToClearRegionsInTransition"
argument_list|)
throw|;
block|}
block|}
comment|// submit logReplay work
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
name|LogReplayHandler
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|this
operator|.
name|services
argument_list|,
name|this
operator|.
name|deadServers
argument_list|,
name|this
operator|.
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
name|hasLogReplayWork
operator|=
literal|true
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|deadServers
operator|.
name|finish
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hasLogReplayWork
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished processing of shutdown of "
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|resubmit
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
name|IOException
name|ex
parameter_list|)
throws|throws
name|IOException
block|{
comment|// typecast to SSH so that we make sure that it is the SSH instance that
comment|// gets submitted as opposed to MSSH or some other derived instance of SSH
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
operator|(
name|ServerShutdownHandler
operator|)
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed log splitting for "
operator|+
name|serverName
operator|+
literal|", will retry"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
comment|/**    * Process a dead region from a dead RS. Checks if the region is disabled or    * disabling or if the region has a partially completed split.    * @param hri    * @param assignmentManager    * @return Returns true if specified region should be assigned, false if not.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|processDeadRegion
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|tablePresent
init|=
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTablePresent
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tablePresent
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The table "
operator|+
name|hri
operator|.
name|getTable
argument_list|()
operator|+
literal|" was deleted.  Hence not proceeding."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// If table is not disabled but the region is offlined,
name|boolean
name|disabled
init|=
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
decl_stmt|;
if|if
condition|(
name|disabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The table "
operator|+
name|hri
operator|.
name|getTable
argument_list|()
operator|+
literal|" was disabled.  Hence not proceeding."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|hri
operator|.
name|isOffline
argument_list|()
operator|&&
name|hri
operator|.
name|isSplit
argument_list|()
condition|)
block|{
comment|//HBASE-7721: Split parent and daughters are inserted into hbase:meta as an atomic operation.
comment|//If the meta scanner saw the parent split, then it should see the daughters as assigned
comment|//to the dead server. We don't have to do anything.
return|return
literal|false
return|;
block|}
name|boolean
name|disabling
init|=
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|)
decl_stmt|;
if|if
condition|(
name|disabling
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The table "
operator|+
name|hri
operator|.
name|getTable
argument_list|()
operator|+
literal|" is disabled.  Hence not assigning region"
operator|+
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

