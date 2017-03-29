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
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|classification
operator|.
name|InterfaceStability
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
name|ClusterStatusProtos
import|;
end_import

begin_comment
comment|/**  * State of a Region while undergoing transitions.  * This class is immutable.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionState
block|{
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
enum|enum
name|State
block|{
name|OFFLINE
block|,
comment|// region is in an offline state
name|PENDING_OPEN
block|,
comment|// same as OPENING, to be removed
name|OPENING
block|,
comment|// server has begun to open but not yet done
name|OPEN
block|,
comment|// server opened region and updated meta
name|PENDING_CLOSE
block|,
comment|// same as CLOSING, to be removed
name|CLOSING
block|,
comment|// server has begun to close but not yet done
name|CLOSED
block|,
comment|// server closed region and updated meta
name|SPLITTING
block|,
comment|// server started split of a region
name|SPLIT
block|,
comment|// server completed split of a region
name|FAILED_OPEN
block|,
comment|// failed to open, and won't retry any more
name|FAILED_CLOSE
block|,
comment|// failed to close, and won't retry any more
name|MERGING
block|,
comment|// server started merge a region
name|MERGED
block|,
comment|// server completed merge a region
name|SPLITTING_NEW
block|,
comment|// new region to be created when RS splits a parent
comment|// region but hasn't be created yet, or master doesn't
comment|// know it's already created
name|MERGING_NEW
block|;
comment|// new region to be created when RS merges two
comment|// daughter regions but hasn't be created yet, or
comment|// master doesn't know it's already created
comment|/**      * Convert to protobuf ClusterStatusProtos.RegionState.State      */
specifier|public
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
name|convert
parameter_list|()
block|{
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
name|rs
decl_stmt|;
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|OFFLINE
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|OFFLINE
expr_stmt|;
break|break;
case|case
name|PENDING_OPEN
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|PENDING_OPEN
expr_stmt|;
break|break;
case|case
name|OPENING
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|OPENING
expr_stmt|;
break|break;
case|case
name|OPEN
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|OPEN
expr_stmt|;
break|break;
case|case
name|PENDING_CLOSE
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|PENDING_CLOSE
expr_stmt|;
break|break;
case|case
name|CLOSING
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|CLOSING
expr_stmt|;
break|break;
case|case
name|CLOSED
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|CLOSED
expr_stmt|;
break|break;
case|case
name|SPLITTING
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|SPLITTING
expr_stmt|;
break|break;
case|case
name|SPLIT
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|SPLIT
expr_stmt|;
break|break;
case|case
name|FAILED_OPEN
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|FAILED_OPEN
expr_stmt|;
break|break;
case|case
name|FAILED_CLOSE
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|FAILED_CLOSE
expr_stmt|;
break|break;
case|case
name|MERGING
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|MERGING
expr_stmt|;
break|break;
case|case
name|MERGED
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|MERGED
expr_stmt|;
break|break;
case|case
name|SPLITTING_NEW
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|SPLITTING_NEW
expr_stmt|;
break|break;
case|case
name|MERGING_NEW
case|:
name|rs
operator|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
operator|.
name|MERGING_NEW
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|""
argument_list|)
throw|;
block|}
return|return
name|rs
return|;
block|}
comment|/**      * Convert a protobuf HBaseProtos.RegionState.State to a RegionState.State      *      * @return the RegionState.State      */
specifier|public
specifier|static
name|State
name|convert
parameter_list|(
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|State
name|protoState
parameter_list|)
block|{
name|State
name|state
decl_stmt|;
switch|switch
condition|(
name|protoState
condition|)
block|{
case|case
name|OFFLINE
case|:
name|state
operator|=
name|OFFLINE
expr_stmt|;
break|break;
case|case
name|PENDING_OPEN
case|:
name|state
operator|=
name|PENDING_OPEN
expr_stmt|;
break|break;
case|case
name|OPENING
case|:
name|state
operator|=
name|OPENING
expr_stmt|;
break|break;
case|case
name|OPEN
case|:
name|state
operator|=
name|OPEN
expr_stmt|;
break|break;
case|case
name|PENDING_CLOSE
case|:
name|state
operator|=
name|PENDING_CLOSE
expr_stmt|;
break|break;
case|case
name|CLOSING
case|:
name|state
operator|=
name|CLOSING
expr_stmt|;
break|break;
case|case
name|CLOSED
case|:
name|state
operator|=
name|CLOSED
expr_stmt|;
break|break;
case|case
name|SPLITTING
case|:
name|state
operator|=
name|SPLITTING
expr_stmt|;
break|break;
case|case
name|SPLIT
case|:
name|state
operator|=
name|SPLIT
expr_stmt|;
break|break;
case|case
name|FAILED_OPEN
case|:
name|state
operator|=
name|FAILED_OPEN
expr_stmt|;
break|break;
case|case
name|FAILED_CLOSE
case|:
name|state
operator|=
name|FAILED_CLOSE
expr_stmt|;
break|break;
case|case
name|MERGING
case|:
name|state
operator|=
name|MERGING
expr_stmt|;
break|break;
case|case
name|MERGED
case|:
name|state
operator|=
name|MERGED
expr_stmt|;
break|break;
case|case
name|SPLITTING_NEW
case|:
name|state
operator|=
name|SPLITTING_NEW
expr_stmt|;
break|break;
case|case
name|MERGING_NEW
case|:
name|state
operator|=
name|MERGING_NEW
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unhandled state "
operator|+
name|protoState
argument_list|)
throw|;
block|}
return|return
name|state
return|;
block|}
block|}
specifier|private
specifier|final
name|long
name|stamp
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|State
name|state
decl_stmt|;
comment|// The duration of region in transition
specifier|private
name|long
name|ritDuration
decl_stmt|;
specifier|public
name|RegionState
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|State
name|state
parameter_list|)
block|{
name|this
argument_list|(
name|region
argument_list|,
name|state
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionState
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|State
name|state
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
argument_list|(
name|region
argument_list|,
name|state
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionState
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|State
name|state
parameter_list|,
name|long
name|stamp
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
argument_list|(
name|region
argument_list|,
name|state
argument_list|,
name|stamp
argument_list|,
name|serverName
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionState
parameter_list|(
name|HRegionInfo
name|region
parameter_list|,
name|State
name|state
parameter_list|,
name|long
name|stamp
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|long
name|ritDuration
parameter_list|)
block|{
name|this
operator|.
name|hri
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|stamp
operator|=
name|stamp
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|ritDuration
operator|=
name|ritDuration
expr_stmt|;
block|}
specifier|public
name|State
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|long
name|getStamp
parameter_list|()
block|{
return|return
name|stamp
return|;
block|}
specifier|public
name|HRegionInfo
name|getRegion
parameter_list|()
block|{
return|return
name|hri
return|;
block|}
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|serverName
return|;
block|}
specifier|public
name|long
name|getRitDuration
parameter_list|()
block|{
return|return
name|ritDuration
return|;
block|}
comment|/**    * Update the duration of region in transition    * @param previousStamp previous RegionState's timestamp    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
name|void
name|updateRitDuration
parameter_list|(
name|long
name|previousStamp
parameter_list|)
block|{
name|this
operator|.
name|ritDuration
operator|+=
operator|(
name|this
operator|.
name|stamp
operator|-
name|previousStamp
operator|)
expr_stmt|;
block|}
comment|/**    * PENDING_CLOSE (to be removed) is the same as CLOSING    */
specifier|public
name|boolean
name|isClosing
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|PENDING_CLOSE
operator|||
name|state
operator|==
name|State
operator|.
name|CLOSING
return|;
block|}
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|CLOSED
return|;
block|}
comment|/**    * PENDING_OPEN (to be removed) is the same as OPENING    */
specifier|public
name|boolean
name|isOpening
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|PENDING_OPEN
operator|||
name|state
operator|==
name|State
operator|.
name|OPENING
return|;
block|}
specifier|public
name|boolean
name|isOpened
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|OPEN
return|;
block|}
specifier|public
name|boolean
name|isOffline
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|OFFLINE
return|;
block|}
specifier|public
name|boolean
name|isSplitting
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|SPLITTING
return|;
block|}
specifier|public
name|boolean
name|isSplit
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|SPLIT
return|;
block|}
specifier|public
name|boolean
name|isSplittingNew
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|SPLITTING_NEW
return|;
block|}
specifier|public
name|boolean
name|isFailedOpen
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|FAILED_OPEN
return|;
block|}
specifier|public
name|boolean
name|isFailedClose
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|FAILED_CLOSE
return|;
block|}
specifier|public
name|boolean
name|isMerging
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|MERGING
return|;
block|}
specifier|public
name|boolean
name|isMerged
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|MERGED
return|;
block|}
specifier|public
name|boolean
name|isMergingNew
parameter_list|()
block|{
return|return
name|state
operator|==
name|State
operator|.
name|MERGING_NEW
return|;
block|}
specifier|public
name|boolean
name|isOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|serverName
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|equals
argument_list|(
name|sn
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isMergingOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
name|isMerging
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isMergingNewOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
name|isMergingNew
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isMergingNewOrOpenedOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isMergingNew
argument_list|()
operator|||
name|isOpened
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isMergingNewOrOfflineOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isMergingNew
argument_list|()
operator|||
name|isOffline
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isSplittingOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
name|isSplitting
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isSplittingNewOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
name|isSplittingNew
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isSplittingOrOpenedOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isSplitting
argument_list|()
operator|||
name|isOpened
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isSplittingOrSplitOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isSplitting
argument_list|()
operator|||
name|isSplit
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isClosingOrClosedOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isClosing
argument_list|()
operator|||
name|isClosed
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isOpeningOrFailedOpenOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isOpening
argument_list|()
operator|||
name|isFailedOpen
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isOpeningOrOpenedOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
operator|(
name|isOpening
argument_list|()
operator|||
name|isOpened
argument_list|()
operator|)
return|;
block|}
specifier|public
name|boolean
name|isOpenedOnServer
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|isOnServer
argument_list|(
name|sn
argument_list|)
operator|&&
name|isOpened
argument_list|()
return|;
block|}
comment|/**    * Check if a region state can transition to offline    */
specifier|public
name|boolean
name|isReadyToOffline
parameter_list|()
block|{
return|return
name|isMerged
argument_list|()
operator|||
name|isSplit
argument_list|()
operator|||
name|isOffline
argument_list|()
operator|||
name|isSplittingNew
argument_list|()
operator|||
name|isMergingNew
argument_list|()
return|;
block|}
comment|/**    * Check if a region state can transition to online    */
specifier|public
name|boolean
name|isReadyToOnline
parameter_list|()
block|{
return|return
name|isOpened
argument_list|()
operator|||
name|isSplittingNew
argument_list|()
operator|||
name|isMergingNew
argument_list|()
return|;
block|}
comment|/**    * Check if a region state is one of offline states that    * can't transition to pending_close/closing (unassign/offline)    */
specifier|public
name|boolean
name|isUnassignable
parameter_list|()
block|{
return|return
name|isUnassignable
argument_list|(
name|state
argument_list|)
return|;
block|}
comment|/**    * Check if a region state is one of offline states that    * can't transition to pending_close/closing (unassign/offline)    */
specifier|public
specifier|static
name|boolean
name|isUnassignable
parameter_list|(
name|State
name|state
parameter_list|)
block|{
return|return
name|state
operator|==
name|State
operator|.
name|MERGED
operator|||
name|state
operator|==
name|State
operator|.
name|SPLIT
operator|||
name|state
operator|==
name|State
operator|.
name|OFFLINE
operator|||
name|state
operator|==
name|State
operator|.
name|SPLITTING_NEW
operator|||
name|state
operator|==
name|State
operator|.
name|MERGING_NEW
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
literal|"{"
operator|+
name|hri
operator|.
name|getShortNameToLog
argument_list|()
operator|+
literal|" state="
operator|+
name|state
operator|+
literal|", ts="
operator|+
name|stamp
operator|+
literal|", server="
operator|+
name|serverName
operator|+
literal|"}"
return|;
block|}
comment|/**    * A slower (but more easy-to-read) stringification    */
specifier|public
name|String
name|toDescriptiveString
parameter_list|()
block|{
name|long
name|relTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|stamp
decl_stmt|;
return|return
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" state="
operator|+
name|state
operator|+
literal|", ts="
operator|+
operator|new
name|Date
argument_list|(
name|stamp
argument_list|)
operator|+
literal|" ("
operator|+
operator|(
name|relTime
operator|/
literal|1000
operator|)
operator|+
literal|"s ago)"
operator|+
literal|", server="
operator|+
name|serverName
return|;
block|}
comment|/**    * Convert a RegionState to an HBaseProtos.RegionState    *    * @return the converted HBaseProtos.RegionState    */
specifier|public
name|ClusterStatusProtos
operator|.
name|RegionState
name|convert
parameter_list|()
block|{
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|Builder
name|regionState
init|=
name|ClusterStatusProtos
operator|.
name|RegionState
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|regionState
operator|.
name|setRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
name|regionState
operator|.
name|setState
argument_list|(
name|state
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
name|regionState
operator|.
name|setStamp
argument_list|(
name|getStamp
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|regionState
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Convert a protobuf HBaseProtos.RegionState to a RegionState    *    * @return the RegionState    */
specifier|public
specifier|static
name|RegionState
name|convert
parameter_list|(
name|ClusterStatusProtos
operator|.
name|RegionState
name|proto
parameter_list|)
block|{
return|return
operator|new
name|RegionState
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|proto
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|,
name|State
operator|.
name|convert
argument_list|(
name|proto
operator|.
name|getState
argument_list|()
argument_list|)
argument_list|,
name|proto
operator|.
name|getStamp
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Check if two states are the same, except timestamp    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|RegionState
name|tmp
init|=
operator|(
name|RegionState
operator|)
name|obj
decl_stmt|;
return|return
name|tmp
operator|.
name|hri
operator|.
name|equals
argument_list|(
name|hri
argument_list|)
operator|&&
name|tmp
operator|.
name|state
operator|==
name|state
operator|&&
operator|(
operator|(
name|serverName
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|equals
argument_list|(
name|tmp
operator|.
name|serverName
argument_list|)
operator|)
operator|||
operator|(
name|tmp
operator|.
name|serverName
operator|==
literal|null
operator|&&
name|serverName
operator|==
literal|null
operator|)
operator|)
return|;
block|}
comment|/**    * Don't count timestamp in hash code calculation    */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|(
name|serverName
operator|!=
literal|null
condition|?
name|serverName
operator|.
name|hashCode
argument_list|()
operator|*
literal|11
else|:
literal|0
operator|)
operator|+
name|hri
operator|.
name|hashCode
argument_list|()
operator|+
literal|5
operator|*
name|state
operator|.
name|ordinal
argument_list|()
return|;
block|}
block|}
end_class

end_unit

