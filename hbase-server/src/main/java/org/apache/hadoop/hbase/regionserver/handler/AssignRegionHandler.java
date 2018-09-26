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
comment|/**  * Handles opening of a region on a region server.  *<p/>  * Just done the same thing with the old {@link OpenRegionHandler}, with some modifications on  * fencing and retrying. But we need to keep the {@link OpenRegionHandler} as is to keep compatible  * with the zk less assignment for 1.x, otherwise it is not possible to do rolling upgrade.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AssignRegionHandler
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
name|AssignRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
name|regionInfo
decl_stmt|;
specifier|private
specifier|final
name|TableDescriptor
name|tableDesc
decl_stmt|;
specifier|private
specifier|final
name|long
name|masterSystemTime
decl_stmt|;
specifier|private
specifier|final
name|RetryCounter
name|retryCounter
decl_stmt|;
specifier|public
name|AssignRegionHandler
parameter_list|(
name|RegionServerServices
name|server
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
annotation|@
name|Nullable
name|TableDescriptor
name|tableDesc
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
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|tableDesc
operator|=
name|tableDesc
expr_stmt|;
name|this
operator|.
name|masterSystemTime
operator|=
name|masterSystemTime
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
specifier|private
name|void
name|cleanUpAndReportFailure
parameter_list|(
name|IOException
name|error
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to open region {}, will report to master"
argument_list|,
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|RegionServerServices
name|rs
init|=
name|getServer
argument_list|()
decl_stmt|;
name|rs
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Boolean
operator|.
name|TRUE
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
name|FAILED_OPEN
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|,
name|masterSystemTime
argument_list|,
name|regionInfo
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to report failed open to master: "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
throw|;
block|}
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
name|String
name|encodedName
init|=
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|byte
index|[]
name|encodedNameBytes
init|=
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
decl_stmt|;
name|String
name|regionName
init|=
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
name|Region
name|onlineRegion
init|=
name|rs
operator|.
name|getRegion
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|onlineRegion
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received OPEN for the region:{}, which is already online"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
comment|// Just follow the old behavior, do we need to call reportRegionStateTransition? Maybe not?
comment|// For normal case, it could happen that the rpc call to schedule this handler is succeeded,
comment|// but before returning to master the connection is broken. And when master tries again, we
comment|// have already finished the opening. For this case we do not need to call
comment|// reportRegionStateTransition any more.
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Open {}"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
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
name|TRUE
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
comment|// The region is opening and this maybe a retry on the rpc call, it is safe to ignore it.
name|LOG
operator|.
name|info
argument_list|(
literal|"Receiving OPEN for the region:{}, which we are already trying to OPEN"
operator|+
literal|" - ignoring this new request for this region."
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The region is closing. This is possible as we will update the region state to CLOSED when
comment|// calling reportRegionStateTransition, so the HMaster will think the region is offline,
comment|// before we actually close the region, as reportRegionStateTransition is part of the
comment|// closing process.
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
name|info
argument_list|(
literal|"Receiving OPEN for the region:{}, which we are trying to close, try again after {}ms"
argument_list|,
name|regionName
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
return|return;
block|}
name|HRegion
name|region
decl_stmt|;
try|try
block|{
name|TableDescriptor
name|htd
init|=
name|tableDesc
operator|!=
literal|null
condition|?
name|tableDesc
else|:
name|rs
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|htd
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Missing table descriptor for "
operator|+
name|regionName
argument_list|)
throw|;
block|}
comment|// pass null for the last parameter, which used to be a CancelableProgressable, as now the
comment|// opening can not be interrupted by a close request any more.
name|region
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|regionInfo
argument_list|,
name|htd
argument_list|,
name|rs
operator|.
name|getWAL
argument_list|(
name|regionInfo
argument_list|)
argument_list|,
name|rs
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|rs
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|cleanUpAndReportFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|rs
operator|.
name|postOpenDeployTasks
argument_list|(
operator|new
name|PostOpenDeployContext
argument_list|(
name|region
argument_list|,
name|masterSystemTime
argument_list|)
argument_list|)
expr_stmt|;
name|rs
operator|.
name|addRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Opened {}"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
name|Boolean
name|current
init|=
name|rs
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"Bad state: we've just opened a region that was NOT in transition. Region={}"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|current
condition|)
block|{
comment|// Should NEVER happen, but let's be paranoid.
name|LOG
operator|.
name|error
argument_list|(
literal|"Bad state: we've just opened a region that was closing. Region={}"
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
block|}
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
literal|"Fatal error occured while opening region {}, aborting..."
argument_list|,
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|getServer
argument_list|()
operator|.
name|abort
argument_list|(
literal|"Failed to open region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" and can not recover"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|AssignRegionHandler
name|create
parameter_list|(
name|RegionServerServices
name|server
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|TableDescriptor
name|tableDesc
parameter_list|,
name|long
name|masterSystemTime
parameter_list|)
block|{
name|EventType
name|eventType
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|eventType
operator|=
name|EventType
operator|.
name|M_RS_CLOSE_META
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|regionInfo
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
operator|||
operator|(
name|tableDesc
operator|!=
literal|null
operator|&&
name|tableDesc
operator|.
name|getPriority
argument_list|()
operator|>=
name|HConstants
operator|.
name|ADMIN_QOS
operator|)
condition|)
block|{
name|eventType
operator|=
name|EventType
operator|.
name|M_RS_OPEN_PRIORITY_REGION
expr_stmt|;
block|}
else|else
block|{
name|eventType
operator|=
name|EventType
operator|.
name|M_RS_OPEN_REGION
expr_stmt|;
block|}
return|return
operator|new
name|AssignRegionHandler
argument_list|(
name|server
argument_list|,
name|regionInfo
argument_list|,
name|tableDesc
argument_list|,
name|masterSystemTime
argument_list|,
name|eventType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

