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
operator|.
name|assignment
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|ConcurrentMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
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
name|TableName
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
name|DoNotRetryRegionException
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
name|RegionOfflineException
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
name|UnexpectedStateException
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
name|procedure2
operator|.
name|ProcedureEvent
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
name|EnvironmentEdgeManager
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Current Region State. Most fields are synchronized with meta region, i.e, we will update meta  * immediately after we modify this RegionStateNode, and usually under the lock. The only exception  * is {@link #lastHost}, which should not be used for critical condition.  *<p/>  * Typically, the only way to modify this class is through {@link TransitRegionStateProcedure}, and  * we will record the TRSP along with this RegionStateNode to make sure that there could at most one  * TRSP. For other operations, such as SCP, we will first get the lock, and then try to schedule a  * TRSP. If there is already one, then the solution will be different:  *<ul>  *<li>For SCP, we will update the region state in meta to tell the TRSP to retry.</li>  *<li>For DisableTableProcedure, as we have the xlock, we can make sure that the TRSP has not been  * executed yet, so just unset it and attach a new one. The original one will quit immediately when  * executing.</li>  *<li>For split/merge, we will fail immediately as there is no actual operations yet so no  * harm.</li>  *<li>For EnableTableProcedure/TruncateTableProcedure, we can make sure that there will be no TRSP  * attached with the RSNs.</li>  *<li>For other procedures, you'd better use ReopenTableRegionsProcedure. The RTRP will take care  * of lots of corner cases when reopening regions.</li>  *</ul>  *<p/>  * Several fields are declared with {@code volatile}, which means you are free to get it without  * lock, but usually you should not use these fields without locking for critical condition, as it  * will be easily to introduce inconsistency. For example, you are free to dump the status and show  * it on web without locking, but if you want to change the state of the RegionStateNode by checking  * the current state, you'd better have the lock...  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionStateNode
implements|implements
name|Comparable
argument_list|<
name|RegionStateNode
argument_list|>
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
name|RegionStateNode
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|AssignmentProcedureEvent
extends|extends
name|ProcedureEvent
argument_list|<
name|RegionInfo
argument_list|>
block|{
specifier|public
name|AssignmentProcedureEvent
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|final
name|Lock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
name|regionInfo
decl_stmt|;
specifier|private
specifier|final
name|ProcedureEvent
argument_list|<
name|?
argument_list|>
name|event
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|RegionInfo
argument_list|,
name|RegionStateNode
argument_list|>
name|ritMap
decl_stmt|;
comment|// volatile only for getLastUpdate and test usage, the upper layer should sync on the
comment|// RegionStateNode before accessing usually.
specifier|private
specifier|volatile
name|TransitRegionStateProcedure
name|procedure
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|ServerName
name|regionLocation
init|=
literal|null
decl_stmt|;
comment|// notice that, the lastHost will only be updated when a region is successfully CLOSED through
comment|// UnassignProcedure, so do not use it for critical condition as the data maybe stale and unsync
comment|// with the data in meta.
specifier|private
specifier|volatile
name|ServerName
name|lastHost
init|=
literal|null
decl_stmt|;
comment|/**    * A Region-in-Transition (RIT) moves through states. See {@link State} for complete list. A    * Region that is opened moves from OFFLINE => OPENING => OPENED.    */
specifier|private
specifier|volatile
name|State
name|state
init|=
name|State
operator|.
name|OFFLINE
decl_stmt|;
comment|/**    * Updated whenever a call to {@link #setRegionLocation(ServerName)} or    * {@link #setState(State, State...)}.    */
specifier|private
specifier|volatile
name|long
name|lastUpdate
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|openSeqNum
init|=
name|HConstants
operator|.
name|NO_SEQNUM
decl_stmt|;
name|RegionStateNode
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|ConcurrentMap
argument_list|<
name|RegionInfo
argument_list|,
name|RegionStateNode
argument_list|>
name|ritMap
parameter_list|)
block|{
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|event
operator|=
operator|new
name|AssignmentProcedureEvent
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|ritMap
operator|=
name|ritMap
expr_stmt|;
block|}
comment|/**    * @param update new region state this node should be assigned.    * @param expected current state should be in this given list of expected states    * @return true, if current state is in expected list; otherwise false.    */
specifier|public
name|boolean
name|setState
parameter_list|(
specifier|final
name|State
name|update
parameter_list|,
specifier|final
name|State
modifier|...
name|expected
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isInState
argument_list|(
name|expected
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|this
operator|.
name|state
operator|=
name|update
expr_stmt|;
name|this
operator|.
name|lastUpdate
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**    * Put region into OFFLINE mode (set state and clear location).    * @return Last recorded server deploy    */
specifier|public
name|ServerName
name|offline
parameter_list|()
block|{
name|setState
argument_list|(
name|State
operator|.
name|OFFLINE
argument_list|)
expr_stmt|;
return|return
name|setRegionLocation
argument_list|(
literal|null
argument_list|)
return|;
block|}
comment|/**    * Set new {@link State} but only if currently in<code>expected</code> State (if not, throw    * {@link UnexpectedStateException}.    */
specifier|public
name|void
name|transitionState
parameter_list|(
specifier|final
name|State
name|update
parameter_list|,
specifier|final
name|State
modifier|...
name|expected
parameter_list|)
throws|throws
name|UnexpectedStateException
block|{
if|if
condition|(
operator|!
name|setState
argument_list|(
name|update
argument_list|,
name|expected
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
literal|"Expected "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
operator|+
literal|" so could move to "
operator|+
name|update
operator|+
literal|" but current state="
operator|+
name|getState
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|isInState
parameter_list|(
specifier|final
name|State
modifier|...
name|expected
parameter_list|)
block|{
if|if
condition|(
name|expected
operator|!=
literal|null
operator|&&
name|expected
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|boolean
name|expectedState
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|expectedState
operator||=
operator|(
name|getState
argument_list|()
operator|==
name|expected
index|[
name|i
index|]
operator|)
expr_stmt|;
block|}
return|return
name|expectedState
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|isStuck
parameter_list|()
block|{
return|return
name|isInState
argument_list|(
name|State
operator|.
name|FAILED_OPEN
argument_list|)
operator|&&
name|getProcedure
argument_list|()
operator|!=
literal|null
return|;
block|}
specifier|public
name|boolean
name|isInTransition
parameter_list|()
block|{
return|return
name|getProcedure
argument_list|()
operator|!=
literal|null
return|;
block|}
specifier|public
name|long
name|getLastUpdate
parameter_list|()
block|{
name|TransitRegionStateProcedure
name|proc
init|=
name|this
operator|.
name|procedure
decl_stmt|;
return|return
name|proc
operator|!=
literal|null
condition|?
name|proc
operator|.
name|getLastUpdate
argument_list|()
else|:
name|lastUpdate
return|;
block|}
specifier|public
name|void
name|setLastHost
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|lastHost
operator|=
name|serverName
expr_stmt|;
block|}
specifier|public
name|void
name|setOpenSeqNum
parameter_list|(
specifier|final
name|long
name|seqId
parameter_list|)
block|{
name|this
operator|.
name|openSeqNum
operator|=
name|seqId
expr_stmt|;
block|}
specifier|public
name|ServerName
name|setRegionLocation
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|ServerName
name|lastRegionLocation
init|=
name|this
operator|.
name|regionLocation
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
operator|&&
name|serverName
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Tracking when we are set to null "
operator|+
name|this
argument_list|,
operator|new
name|Throwable
argument_list|(
literal|"TRACE"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|regionLocation
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|lastUpdate
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
return|return
name|lastRegionLocation
return|;
block|}
specifier|public
name|void
name|setProcedure
parameter_list|(
name|TransitRegionStateProcedure
name|proc
parameter_list|)
block|{
assert|assert
name|this
operator|.
name|procedure
operator|==
literal|null
assert|;
name|this
operator|.
name|procedure
operator|=
name|proc
expr_stmt|;
name|ritMap
operator|.
name|put
argument_list|(
name|regionInfo
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|unsetProcedure
parameter_list|(
name|TransitRegionStateProcedure
name|proc
parameter_list|)
block|{
assert|assert
name|this
operator|.
name|procedure
operator|==
name|proc
assert|;
name|this
operator|.
name|procedure
operator|=
literal|null
expr_stmt|;
name|ritMap
operator|.
name|remove
argument_list|(
name|regionInfo
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TransitRegionStateProcedure
name|getProcedure
parameter_list|()
block|{
return|return
name|procedure
return|;
block|}
specifier|public
name|ProcedureEvent
argument_list|<
name|?
argument_list|>
name|getProcedureEvent
parameter_list|()
block|{
return|return
name|event
return|;
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
specifier|public
name|TableName
name|getTable
parameter_list|()
block|{
return|return
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isSystemTable
parameter_list|()
block|{
return|return
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
return|;
block|}
specifier|public
name|ServerName
name|getLastHost
parameter_list|()
block|{
return|return
name|lastHost
return|;
block|}
specifier|public
name|ServerName
name|getRegionLocation
parameter_list|()
block|{
return|return
name|regionLocation
return|;
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
name|getOpenSeqNum
parameter_list|()
block|{
return|return
name|openSeqNum
return|;
block|}
specifier|public
name|int
name|getFormatVersion
parameter_list|()
block|{
comment|// we don't have any format for now
comment|// it should probably be in regionInfo.getFormatVersion()
return|return
literal|0
return|;
block|}
specifier|public
name|RegionState
name|toRegionState
parameter_list|()
block|{
return|return
operator|new
name|RegionState
argument_list|(
name|getRegionInfo
argument_list|()
argument_list|,
name|getState
argument_list|()
argument_list|,
name|getLastUpdate
argument_list|()
argument_list|,
name|getRegionLocation
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|RegionStateNode
name|other
parameter_list|)
block|{
comment|// NOTE: RegionInfo sort by table first, so we are relying on that.
comment|// we have a TestRegionState#testOrderedByTable() that check for that.
return|return
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|getRegionInfo
argument_list|()
argument_list|,
name|other
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|getRegionInfo
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|RegionStateNode
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|compareTo
argument_list|(
operator|(
name|RegionStateNode
operator|)
name|other
argument_list|)
operator|==
literal|0
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
name|toDescriptiveString
argument_list|()
return|;
block|}
specifier|public
name|String
name|toShortString
parameter_list|()
block|{
comment|// rit= is the current Region-In-Transition State -- see State enum.
return|return
name|String
operator|.
name|format
argument_list|(
literal|"rit=%s, location=%s"
argument_list|,
name|getState
argument_list|()
argument_list|,
name|getRegionLocation
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|String
name|toDescriptiveString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s, table=%s, region=%s"
argument_list|,
name|toShortString
argument_list|()
argument_list|,
name|getTable
argument_list|()
argument_list|,
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|checkOnline
parameter_list|()
throws|throws
name|DoNotRetryRegionException
block|{
name|RegionInfo
name|ri
init|=
name|getRegionInfo
argument_list|()
decl_stmt|;
name|State
name|s
init|=
name|state
decl_stmt|;
if|if
condition|(
name|s
operator|!=
name|State
operator|.
name|OPEN
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryRegionException
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" is no OPEN; state="
operator|+
name|s
argument_list|)
throw|;
block|}
if|if
condition|(
name|ri
operator|.
name|isSplitParent
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryRegionException
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" is not online (splitParent=true)"
argument_list|)
throw|;
block|}
if|if
condition|(
name|ri
operator|.
name|isSplit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryRegionException
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" has split=true"
argument_list|)
throw|;
block|}
if|if
condition|(
name|ri
operator|.
name|isOffline
argument_list|()
condition|)
block|{
comment|// RegionOfflineException is not instance of DNRIOE so wrap it.
throw|throw
operator|new
name|DoNotRetryRegionException
argument_list|(
operator|new
name|RegionOfflineException
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|lock
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|unlock
parameter_list|()
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
