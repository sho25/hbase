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
name|regionserver
operator|.
name|handler
package|;
end_package

begin_import
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
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
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|Region
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
name|RetryCounter
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
comment|/**  * Handles closing of a region on a region server.  *<p/>  * Just done the same thing with the old {@link CloseRegionHandler}, with some modifications on  * fencing and retrying. But we need to keep the {@link CloseRegionHandler} as is to keep compatible  * with the zk less assignment for 1.x, otherwise it is not possible to do rolling upgrade.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UnassignRegionHandler
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
name|UnassignRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|encodedName
decl_stmt|;
specifier|private
specifier|final
name|long
name|closeProcId
decl_stmt|;
comment|// If true, the hosting server is aborting. Region close process is different
comment|// when we are aborting.
comment|// TODO: not used yet, we still use the old CloseRegionHandler when aborting
specifier|private
specifier|final
name|boolean
name|abort
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|destination
decl_stmt|;
specifier|private
specifier|final
name|RetryCounter
name|retryCounter
decl_stmt|;
specifier|public
name|UnassignRegionHandler
parameter_list|(
name|RegionServerServices
name|server
parameter_list|,
name|String
name|encodedName
parameter_list|,
name|long
name|closeProcId
parameter_list|,
name|boolean
name|abort
parameter_list|,
annotation|@
name|Nullable
name|ServerName
name|destination
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
name|encodedName
operator|=
name|encodedName
expr_stmt|;
name|this
operator|.
name|closeProcId
operator|=
name|closeProcId
expr_stmt|;
name|this
operator|.
name|abort
operator|=
name|abort
expr_stmt|;
name|this
operator|.
name|destination
operator|=
name|destination
expr_stmt|;
name|this
operator|.
name|retryCounter
operator|=
name|HandlerUtil
operator|.
name|getRetryCounter
argument_list|()
expr_stmt|;
block|}
specifier|private
name|RegionServerServices
name|getServer
parameter_list|()
block|{
return|return
operator|(
name|RegionServerServices
operator|)
name|server
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
name|RegionServerServices
name|rs
init|=
name|getServer
argument_list|()
decl_stmt|;
name|byte
index|[]
name|encodedNameBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
name|Boolean
name|previous
init|=
name|rs
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|encodedNameBytes
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|previous
condition|)
block|{
comment|// This could happen as we will update the region state to OPEN when calling
comment|// reportRegionStateTransition, so the HMaster will think the region is online, before we
comment|// actually open the region, as reportRegionStateTransition is part of the opening process.
name|long
name|backoff
init|=
name|retryCounter
operator|.
name|getBackoffTimeAndIncrementAttempts
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received CLOSE for the region: {}, which we are already "
operator|+
literal|"trying to OPEN. try again after {}ms"
argument_list|,
name|encodedName
argument_list|,
name|backoff
argument_list|)
expr_stmt|;
name|rs
operator|.
name|getExecutorService
argument_list|()
operator|.
name|delayedSubmit
argument_list|(
name|this
argument_list|,
name|backoff
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received CLOSE for the region: {}, which we are already trying to CLOSE,"
operator|+
literal|" but not completed yet"
argument_list|,
name|encodedName
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|HRegion
name|region
init|=
operator|(
name|HRegion
operator|)
name|rs
operator|.
name|getRegion
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Received CLOSE for a region {} which is not online, and we're not opening/closing."
argument_list|,
name|encodedName
argument_list|)
expr_stmt|;
name|rs
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|encodedNameBytes
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|regionName
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Close {}"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
if|if
condition|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// XXX: The behavior is a bit broken. At master side there is no FAILED_CLOSE state, so if
comment|// there are exception thrown from the CP, we can not report the error to master, and if here
comment|// we just return without calling reportRegionStateTransition, the TRSP at master side will
comment|// hang there for ever. So here if the CP throws an exception out, the only way is to abort
comment|// the RS...
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preClose
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|region
operator|.
name|close
argument_list|(
name|abort
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// XXX: Is this still possible? The old comment says about split, but now split is done at
comment|// master side, so...
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't close region {}, was already closed during close()"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
name|rs
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|encodedNameBytes
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
return|return;
block|}
name|rs
operator|.
name|removeRegion
argument_list|(
name|region
argument_list|,
name|destination
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|rs
operator|.
name|reportRegionStateTransition
argument_list|(
operator|new
name|RegionStateTransitionContext
argument_list|(
name|TransitionCode
operator|.
name|CLOSED
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|,
name|closeProcId
argument_list|,
operator|-
literal|1
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to report close to master: "
operator|+
name|regionName
argument_list|)
throw|;
block|}
name|rs
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|encodedNameBytes
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Closed {}"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|handleException
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Fatal error occured while closing region {}, aborting..."
argument_list|,
name|encodedName
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|getServer
argument_list|()
operator|.
name|abort
argument_list|(
literal|"Failed to close region "
operator|+
name|encodedName
operator|+
literal|" and can not recover"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|UnassignRegionHandler
name|create
parameter_list|(
name|RegionServerServices
name|server
parameter_list|,
name|String
name|encodedName
parameter_list|,
name|long
name|closeProcId
parameter_list|,
name|boolean
name|abort
parameter_list|,
annotation|@
name|Nullable
name|ServerName
name|destination
parameter_list|)
block|{
comment|// Just try our best to determine whether it is for closing meta. It is not the end of the world
comment|// if we put the handler into a wrong executor.
name|Region
name|region
init|=
name|server
operator|.
name|getRegion
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
name|EventType
name|eventType
init|=
name|region
operator|!=
literal|null
operator|&&
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|?
name|EventType
operator|.
name|M_RS_CLOSE_META
else|:
name|EventType
operator|.
name|M_RS_CLOSE_REGION
decl_stmt|;
return|return
operator|new
name|UnassignRegionHandler
argument_list|(
name|server
argument_list|,
name|encodedName
argument_list|,
name|closeProcId
argument_list|,
name|abort
argument_list|,
name|destination
argument_list|,
name|eventType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

