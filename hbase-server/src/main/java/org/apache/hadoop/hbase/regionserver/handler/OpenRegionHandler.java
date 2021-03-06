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
name|TableDescriptor
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
name|procedure2
operator|.
name|Procedure
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
name|regionserver
operator|.
name|RegionServerServices
operator|.
name|PostOpenDeployContext
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
operator|.
name|RegionStateTransitionContext
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
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
import|;
end_import

begin_comment
comment|/**  * Handles opening of a region on a region server.  *<p>  * This is executed after receiving an OPEN RPC from the master or client.  * @deprecated Keep it here only for compatible  * @see AssignRegionHandler  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|OpenRegionHandler
extends|extends
name|EventHandler
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
name|OpenRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
name|regionInfo
decl_stmt|;
specifier|private
specifier|final
name|TableDescriptor
name|htd
decl_stmt|;
specifier|private
specifier|final
name|long
name|masterSystemTime
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
name|RegionInfo
name|regionInfo
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|long
name|masterSystemTime
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
name|htd
argument_list|,
name|masterSystemTime
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
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|TableDescriptor
name|htd
parameter_list|,
name|long
name|masterSystemTime
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
name|this
operator|.
name|htd
operator|=
name|htd
expr_stmt|;
name|this
operator|.
name|masterSystemTime
operator|=
name|masterSystemTime
expr_stmt|;
block|}
specifier|public
name|RegionInfo
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
name|boolean
name|openSuccessful
init|=
literal|false
decl_stmt|;
specifier|final
name|String
name|regionName
init|=
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
try|try
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
comment|// 2 different difficult situations can occur
comment|// 1) The opening was cancelled. This is an expected situation
comment|// 2) The region is now marked as online while we're suppose to open. This would be a bug.
comment|// Check that this region is not already online
if|if
condition|(
name|this
operator|.
name|rsServices
operator|.
name|getRegion
argument_list|(
name|encodedName
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Region "
operator|+
name|encodedName
operator|+
literal|" was already online when we started processing the opening. "
operator|+
literal|"Marking this new attempt as failed"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Check that we're still supposed to open the region.
comment|// If fails, just return.  Someone stole the region from under us.
if|if
condition|(
operator|!
name|isRegionStillOpening
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Region "
operator|+
name|encodedName
operator|+
literal|" opening cancelled"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Open region.  After a successful open, failures in subsequent
comment|// processing needs to do a close as part of cleanup.
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
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|updateMeta
argument_list|(
name|region
argument_list|,
name|masterSystemTime
argument_list|)
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
return|return;
block|}
if|if
condition|(
operator|!
name|isRegionStillOpening
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// Successful region open, and add it to MutableOnlineRegions
name|this
operator|.
name|rsServices
operator|.
name|addRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|openSuccessful
operator|=
literal|true
expr_stmt|;
comment|// Done!  Successful region open
name|LOG
operator|.
name|debug
argument_list|(
literal|"Opened "
operator|+
name|regionName
operator|+
literal|" on "
operator|+
name|this
operator|.
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Do all clean up here
if|if
condition|(
operator|!
name|openSuccessful
condition|)
block|{
name|doCleanUpOnFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Boolean
name|current
init|=
name|this
operator|.
name|rsServices
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|this
operator|.
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
decl_stmt|;
comment|// Let's check if we have met a race condition on open cancellation....
comment|// A better solution would be to not have any race condition.
comment|// this.rsServices.getRegionsInTransitionInRS().remove(
comment|//  this.regionInfo.getEncodedNameAsBytes(), Boolean.TRUE);
comment|// would help.
if|if
condition|(
name|openSuccessful
condition|)
block|{
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
comment|// Should NEVER happen, but let's be paranoid.
name|LOG
operator|.
name|error
argument_list|(
literal|"Bad state: we've just opened a region that was NOT in transition. Region="
operator|+
name|regionName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Boolean
operator|.
name|FALSE
operator|.
name|equals
argument_list|(
name|current
argument_list|)
condition|)
block|{
comment|// Can happen, if we're
comment|// really unlucky.
name|LOG
operator|.
name|error
argument_list|(
literal|"Race condition: we've finished to open a region, while a close was requested "
operator|+
literal|" on region="
operator|+
name|regionName
operator|+
literal|". It can be a critical error, as a region that"
operator|+
literal|" should be closed is now opened. Closing it now"
argument_list|)
expr_stmt|;
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|doCleanUpOnFailedOpen
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|cleanupFailedOpen
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|rsServices
operator|.
name|reportRegionStateTransition
argument_list|(
operator|new
name|RegionStateTransitionContext
argument_list|(
name|TransitionCode
operator|.
name|FAILED_OPEN
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|,
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|,
operator|-
literal|1
argument_list|,
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Update ZK or META.  This can take a while if for example the    * hbase:meta is not available -- if server hosting hbase:meta crashed and we are    * waiting on it to come back -- so run in a thread and keep updating znode    * state meantime so master doesn't timeout our region-in-transition.    * Caller must cleanup region if this fails.    */
specifier|private
name|boolean
name|updateMeta
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
name|long
name|masterSystemTime
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
argument_list|,
name|masterSystemTime
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Post open deploy task:
comment|//   meta => update meta location in ZK
comment|//   other region => update meta
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
name|isRegionStillOpening
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|signaller
init|)
block|{
try|try
block|{
comment|// Wait for 10 seconds, so that server shutdown
comment|// won't take too long if this thread happens to run.
if|if
condition|(
operator|!
name|signaller
operator|.
name|get
argument_list|()
condition|)
name|signaller
operator|.
name|wait
argument_list|(
literal|10000
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
operator|(
operator|!
name|Thread
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
operator|)
return|;
block|}
comment|/**    * Thread to run region post open tasks. Call {@link #getException()} after the thread finishes    * to check for exceptions running    * {@link RegionServerServices#postOpenDeployTasks(PostOpenDeployContext)}    */
specifier|static
class|class
name|PostOpenDeployTasksThread
extends|extends
name|Thread
block|{
specifier|private
name|Throwable
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
specifier|private
specifier|final
name|long
name|masterSystemTime
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
parameter_list|,
name|long
name|masterSystemTime
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
name|this
operator|.
name|masterSystemTime
operator|=
name|masterSystemTime
expr_stmt|;
block|}
annotation|@
name|Override
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
operator|new
name|PostOpenDeployContext
argument_list|(
name|region
argument_list|,
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|,
name|masterSystemTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
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
decl_stmt|;
name|this
operator|.
name|exception
operator|=
name|e
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|IOException
operator|&&
name|isRegionStillOpening
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|services
argument_list|)
condition|)
block|{
name|server
operator|.
name|abort
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// We're done. Set flag then wake up anyone waiting on thread to complete.
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
name|Throwable
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
comment|/**    * @return Instance of HRegion if successful open else null.    */
specifier|private
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
comment|// Instantiate the region.  This also periodically tickles OPENING
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
name|htd
argument_list|,
name|this
operator|.
name|rsServices
operator|.
name|getWAL
argument_list|(
name|this
operator|.
name|regionInfo
argument_list|)
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
annotation|@
name|Override
specifier|public
name|boolean
name|progress
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isRegionStillOpening
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Open region aborted since it isn't opening any more"
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
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// We failed open. Our caller will see the 'null' return value
comment|// and transition the node back to FAILED_OPEN. If that fails,
comment|// we rely on the Timeout Monitor in the master to reassign.
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
name|t
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
block|{
name|this
operator|.
name|rsServices
operator|.
name|removeRegion
argument_list|(
name|region
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isRegionStillOpening
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|byte
index|[]
name|encodedName
init|=
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
decl_stmt|;
name|Boolean
name|action
init|=
name|rsServices
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|get
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
return|return
name|Boolean
operator|.
name|TRUE
operator|.
name|equals
argument_list|(
name|action
argument_list|)
return|;
comment|// true means opening for RIT
block|}
specifier|private
name|boolean
name|isRegionStillOpening
parameter_list|()
block|{
return|return
name|isRegionStillOpening
argument_list|(
name|regionInfo
argument_list|,
name|rsServices
argument_list|)
return|;
block|}
block|}
end_class

end_unit

