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
name|regionserver
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
name|regionserver
operator|.
name|HRegion
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
name|RegionServerServices
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
name|CancelableProgressable
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
name|ZKAssign
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

begin_comment
comment|/**  * Handles opening of a region on a region server.  *<p>  * This is executed after receiving an OPEN RPC from the master or client.  */
end_comment

begin_class
specifier|public
class|class
name|OpenRegionHandler
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
name|OpenRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
comment|// We get version of our znode at start of open process and monitor it across
comment|// the total open. We'll fail the open if someone hijacks our znode; we can
comment|// tell this has happened if version is not as expected.
specifier|private
specifier|volatile
name|int
name|version
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|OpenRegionHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
argument_list|(
name|server
argument_list|,
name|rsServices
argument_list|,
name|regionInfo
argument_list|,
name|EventType
operator|.
name|M_RS_OPEN_REGION
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|OpenRegionHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
name|EventType
name|eventType
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|eventType
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
block|}
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
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
specifier|final
name|String
name|name
init|=
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing open of "
operator|+
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
operator|||
name|this
operator|.
name|rsServices
operator|.
name|isStopping
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Server stopping or stopped, skipping open of "
operator|+
name|name
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|String
name|encodedName
init|=
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
comment|// Check that this region is not already online
name|HRegion
name|region
init|=
name|this
operator|.
name|rsServices
operator|.
name|getFromOnlineRegions
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempted open of "
operator|+
name|name
operator|+
literal|" but already online on this server"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// If fails, just return.  Someone stole the region from under us.
comment|// Calling transitionZookeeperOfflineToOpening initalizes this.version.
if|if
condition|(
operator|!
name|transitionZookeeperOfflineToOpening
argument_list|(
name|encodedName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region was hijacked? It no longer exists, encodedName="
operator|+
name|encodedName
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Open region.  After a successful open, failures in subsequent processing
comment|// needs to do a close as part of cleanup.
name|region
operator|=
name|openRegion
argument_list|()
expr_stmt|;
if|if
condition|(
name|region
operator|==
literal|null
condition|)
return|return;
name|boolean
name|failed
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|tickleOpening
argument_list|(
literal|"post_region_open"
argument_list|)
condition|)
block|{
if|if
condition|(
name|updateMeta
argument_list|(
name|region
argument_list|)
condition|)
name|failed
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|failed
operator|||
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
operator|||
name|this
operator|.
name|rsServices
operator|.
name|isStopping
argument_list|()
condition|)
block|{
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|transitionToOpened
argument_list|(
name|region
argument_list|)
condition|)
block|{
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Done!  Successful region open
name|LOG
operator|.
name|debug
argument_list|(
literal|"Opened "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update ZK, ROOT or META.  This can take a while if for example the    * .META. is not available -- if server hosting .META. crashed and we are    * waiting on it to come back -- so run in a thread and keep updating znode    * state meantime so master doesn't timeout our region-in-transition.    * Caller must cleanup region if this fails.    */
specifier|private
name|boolean
name|updateMeta
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
operator|||
name|this
operator|.
name|rsServices
operator|.
name|isStopping
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Object we do wait/notify on.  Make it boolean.  If set, we're done.
comment|// Else, wait.
specifier|final
name|AtomicBoolean
name|signaller
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|PostOpenDeployTasksThread
name|t
init|=
operator|new
name|PostOpenDeployTasksThread
argument_list|(
name|r
argument_list|,
name|this
operator|.
name|server
argument_list|,
name|this
operator|.
name|rsServices
argument_list|,
name|signaller
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|int
name|assignmentTimeout
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.master.assignment.timeoutmonitor.period"
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
comment|// Total timeout for meta edit.  If we fail adding the edit then close out
comment|// the region and let it be assigned elsewhere.
name|long
name|timeout
init|=
name|assignmentTimeout
operator|*
literal|10
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|endTime
init|=
name|now
operator|+
name|timeout
decl_stmt|;
comment|// Let our period at which we update OPENING state to be be 1/3rd of the
comment|// regions-in-transition timeout period.
name|long
name|period
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|assignmentTimeout
operator|/
literal|3
argument_list|)
decl_stmt|;
name|long
name|lastUpdate
init|=
name|now
decl_stmt|;
while|while
condition|(
operator|!
name|signaller
operator|.
name|get
argument_list|()
operator|&&
name|t
operator|.
name|isAlive
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|rsServices
operator|.
name|isStopping
argument_list|()
operator|&&
operator|(
name|endTime
operator|>
name|now
operator|)
condition|)
block|{
name|long
name|elapsed
init|=
name|now
operator|-
name|lastUpdate
decl_stmt|;
if|if
condition|(
name|elapsed
operator|>
name|period
condition|)
block|{
comment|// Only tickle OPENING if postOpenDeployTasks is taking some time.
name|lastUpdate
operator|=
name|now
expr_stmt|;
name|tickleOpening
argument_list|(
literal|"post_open_deploy"
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|signaller
init|)
block|{
try|try
block|{
name|signaller
operator|.
name|wait
argument_list|(
name|period
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// Go to the loop check.
block|}
block|}
name|now
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
comment|// Is thread still alive?  We may have left above loop because server is
comment|// stopping or we timed out the edit.  Is so, interrupt it.
if|if
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|signaller
operator|.
name|get
argument_list|()
condition|)
block|{
comment|// Thread still running; interrupt
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupting thread "
operator|+
name|t
argument_list|)
expr_stmt|;
name|t
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|t
operator|.
name|join
argument_list|()
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
name|warn
argument_list|(
literal|"Interrupted joining "
operator|+
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|ie
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Was there an exception opening the region?  This should trigger on
comment|// InterruptedException too.  If so, we failed.
return|return
operator|!
name|t
operator|.
name|interrupted
argument_list|()
operator|&&
name|t
operator|.
name|getException
argument_list|()
operator|==
literal|null
return|;
block|}
comment|/**    * Thread to run region post open tasks.  Call {@link #getException()} after    * the thread finishes to check for exceptions running    * {@link RegionServerServices#postOpenDeployTasks(HRegion, org.apache.hadoop.hbase.catalog.CatalogTracker, boolean)}.    */
specifier|static
class|class
name|PostOpenDeployTasksThread
extends|extends
name|Thread
block|{
specifier|private
name|Exception
name|exception
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Server
name|server
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|signaller
decl_stmt|;
name|PostOpenDeployTasksThread
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|services
parameter_list|,
specifier|final
name|AtomicBoolean
name|signaller
parameter_list|)
block|{
name|super
argument_list|(
literal|"PostOpenDeployTasks:"
operator|+
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
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
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|signaller
operator|=
name|signaller
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|this
operator|.
name|services
operator|.
name|postOpenDeployTasks
argument_list|(
name|this
operator|.
name|region
argument_list|,
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception running postOpenDeployTasks; region="
operator|+
name|this
operator|.
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|exception
operator|=
name|e
expr_stmt|;
block|}
comment|// We're done.  Set flag then wake up anyone waiting on thread to complete.
name|this
operator|.
name|signaller
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|signaller
init|)
block|{
name|this
operator|.
name|signaller
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * @return Null or the run exception; call this method after thread is done.      */
name|Exception
name|getException
parameter_list|()
block|{
return|return
name|this
operator|.
name|exception
return|;
block|}
block|}
comment|/**    * @param r Region we're working on.    * @return Transition znode to OPENED state.    * @throws IOException     */
specifier|private
name|boolean
name|transitionToOpened
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
specifier|final
name|String
name|name
init|=
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
comment|// Finally, Transition ZK node to OPENED
try|try
block|{
if|if
condition|(
name|ZKAssign
operator|.
name|transitionNodeOpened
argument_list|(
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
argument_list|,
name|this
operator|.
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|this
operator|.
name|version
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Completed the OPEN of region "
operator|+
name|name
operator|+
literal|" but when transitioning from "
operator|+
literal|" OPENING to OPENED got a version mismatch, someone else clashed "
operator|+
literal|"so now unassigning -- closing region"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed transitioning node "
operator|+
name|name
operator|+
literal|" from OPENING to OPENED -- closing region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @return Instance of HRegion if successful open else null.    */
name|HRegion
name|openRegion
parameter_list|()
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Instantiate the region.  This also periodically tickles our zk OPENING
comment|// state so master doesn't timeout this region in transition.
name|region
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|this
operator|.
name|regionInfo
argument_list|,
name|this
operator|.
name|rsServices
operator|.
name|getWAL
argument_list|()
argument_list|,
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|rsServices
argument_list|,
operator|new
name|CancelableProgressable
argument_list|()
block|{
specifier|public
name|boolean
name|progress
parameter_list|()
block|{
comment|// We may lose the znode ownership during the open.  Currently its
comment|// too hard interrupting ongoing region open.  Just let it complete
comment|// and check we still have the znode after region open.
return|return
name|tickleOpening
argument_list|(
literal|"open_region_progress"
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// We failed open.  Let our znode expire in regions-in-transition and
comment|// Master will assign elsewhere.  Presumes nothing to close.
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed open of region="
operator|+
name|this
operator|.
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|region
return|;
block|}
specifier|private
name|void
name|cleanupFailedOpen
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|rsServices
operator|.
name|removeFromOnlineRegions
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Transition ZK node from OFFLINE to OPENING.    * @param encodedName Name of the znode file (Region encodedName is the znode    * name).    * @return True if successful transition.    */
name|boolean
name|transitionZookeeperOfflineToOpening
parameter_list|(
specifier|final
name|String
name|encodedName
parameter_list|)
block|{
comment|// TODO: should also handle transition from CLOSED?
try|try
block|{
comment|// Initialize the znode version.
name|this
operator|.
name|version
operator|=
name|ZKAssign
operator|.
name|transitionNodeOpening
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error transition from OFFLINE to OPENING for region="
operator|+
name|encodedName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|boolean
name|b
init|=
name|isGoodVersion
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|b
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed transition from OFFLINE to OPENING for region="
operator|+
name|encodedName
argument_list|)
expr_stmt|;
block|}
return|return
name|b
return|;
block|}
comment|/**    * Update our OPENING state in zookeeper.    * Do this so master doesn't timeout this region-in-transition.    * @param context Some context to add to logs if failure    * @return True if successful transition.    */
name|boolean
name|tickleOpening
parameter_list|(
specifier|final
name|String
name|context
parameter_list|)
block|{
comment|// If previous checks failed... do not try again.
if|if
condition|(
operator|!
name|isGoodVersion
argument_list|()
condition|)
return|return
literal|false
return|;
name|String
name|encodedName
init|=
name|this
operator|.
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
try|try
block|{
name|this
operator|.
name|version
operator|=
name|ZKAssign
operator|.
name|retransitionNodeOpening
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|this
operator|.
name|regionInfo
argument_list|,
name|this
operator|.
name|server
operator|.
name|getServerName
argument_list|()
argument_list|,
name|this
operator|.
name|version
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|server
operator|.
name|abort
argument_list|(
literal|"Exception refreshing OPENING; region="
operator|+
name|encodedName
operator|+
literal|", context="
operator|+
name|context
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|boolean
name|b
init|=
name|isGoodVersion
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|b
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed refreshing OPENING; region="
operator|+
name|encodedName
operator|+
literal|", context="
operator|+
name|context
argument_list|)
expr_stmt|;
block|}
return|return
name|b
return|;
block|}
specifier|private
name|boolean
name|isGoodVersion
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
operator|!=
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

