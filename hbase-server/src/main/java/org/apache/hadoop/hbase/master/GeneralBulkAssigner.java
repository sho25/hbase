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
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
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

begin_comment
comment|/**  * Run bulk assign.  Does one RCP per regionserver passing a  * batch of regions using {@link GeneralBulkAssigner.SingleServerBulkAssigner}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|GeneralBulkAssigner
extends|extends
name|BulkAssigner
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
name|GeneralBulkAssigner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|failedPlans
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|ExecutorService
name|pool
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|bulkPlan
decl_stmt|;
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|final
name|boolean
name|waitTillAllAssigned
decl_stmt|;
specifier|public
name|GeneralBulkAssigner
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|bulkPlan
parameter_list|,
specifier|final
name|AssignmentManager
name|am
parameter_list|,
specifier|final
name|boolean
name|waitTillAllAssigned
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|this
operator|.
name|bulkPlan
operator|=
name|bulkPlan
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|am
expr_stmt|;
name|this
operator|.
name|waitTillAllAssigned
operator|=
name|waitTillAllAssigned
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getThreadNamePrefix
parameter_list|()
block|{
return|return
name|this
operator|.
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|"-GeneralBulkAssigner"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|populatePool
parameter_list|(
name|ExecutorService
name|pool
parameter_list|)
block|{
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
comment|// shut it down later in case some assigner hangs
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|e
range|:
name|this
operator|.
name|bulkPlan
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|pool
operator|.
name|execute
argument_list|(
operator|new
name|SingleServerBulkAssigner
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|this
operator|.
name|assignmentManager
argument_list|,
name|this
operator|.
name|failedPlans
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    *    * @param timeout How long to wait.    * @return true if done.    */
annotation|@
name|Override
specifier|protected
name|boolean
name|waitUntilDone
parameter_list|(
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|regionSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionList
range|:
name|bulkPlan
operator|.
name|values
argument_list|()
control|)
block|{
name|regionSet
operator|.
name|addAll
argument_list|(
name|regionList
argument_list|)
expr_stmt|;
block|}
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
comment|// no more task allowed
name|int
name|serverCount
init|=
name|bulkPlan
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|regionCount
init|=
name|regionSet
operator|.
name|size
argument_list|()
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|rpcWaitTime
init|=
name|startTime
operator|+
name|timeout
decl_stmt|;
while|while
condition|(
operator|!
name|server
operator|.
name|isStopped
argument_list|()
operator|&&
operator|!
name|pool
operator|.
name|isTerminated
argument_list|()
operator|&&
name|rpcWaitTime
operator|>
name|System
operator|.
name|currentTimeMillis
argument_list|()
condition|)
block|{
if|if
condition|(
name|failedPlans
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|pool
operator|.
name|awaitTermination
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|reassignFailedPlans
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|pool
operator|.
name|isTerminated
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"bulk assigner is still running after "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms, shut it down now"
argument_list|)
expr_stmt|;
comment|// some assigner hangs, can't wait any more, shutdown the pool now
name|List
argument_list|<
name|Runnable
argument_list|>
name|notStarted
init|=
name|pool
operator|.
name|shutdownNow
argument_list|()
decl_stmt|;
if|if
condition|(
name|notStarted
operator|!=
literal|null
operator|&&
operator|!
name|notStarted
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|server
operator|.
name|abort
argument_list|(
literal|"some single server assigner hasn't started yet"
operator|+
literal|" when the bulk assigner timed out"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
name|int
name|reassigningRegions
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|!
name|failedPlans
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|reassigningRegions
operator|=
name|reassignFailedPlans
argument_list|()
expr_stmt|;
block|}
name|assignmentManager
operator|.
name|waitForAssignment
argument_list|(
name|regionSet
argument_list|,
name|waitTillAllAssigned
argument_list|,
name|reassigningRegions
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|rpcWaitTime
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|long
name|elapsedTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|String
name|status
init|=
literal|"successfully"
decl_stmt|;
if|if
condition|(
operator|!
name|regionSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|status
operator|=
literal|"with "
operator|+
name|regionSet
operator|.
name|size
argument_list|()
operator|+
literal|" regions still in transition"
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"bulk assigning total "
operator|+
name|regionCount
operator|+
literal|" regions to "
operator|+
name|serverCount
operator|+
literal|" servers, took "
operator|+
name|elapsedTime
operator|+
literal|"ms, "
operator|+
name|status
argument_list|)
expr_stmt|;
block|}
return|return
name|regionSet
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|getTimeoutOnRIT
parameter_list|()
block|{
comment|// Guess timeout.  Multiply the max number of regions on a server
comment|// by how long we think one region takes opening.
name|Configuration
name|conf
init|=
name|server
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|long
name|perRegionOpenTimeGuesstimate
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.bulk.assignment.perregion.open.time"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|int
name|maxRegionsPerServer
init|=
literal|1
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionList
range|:
name|bulkPlan
operator|.
name|values
argument_list|()
control|)
block|{
name|int
name|size
init|=
name|regionList
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
name|maxRegionsPerServer
condition|)
block|{
name|maxRegionsPerServer
operator|=
name|size
expr_stmt|;
block|}
block|}
name|long
name|timeout
init|=
name|perRegionOpenTimeGuesstimate
operator|*
name|maxRegionsPerServer
operator|+
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.rpc.startup.waittime"
argument_list|,
literal|60000
argument_list|)
operator|+
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.bulk.assignment.perregionserver.rpc.waittime"
argument_list|,
literal|30000
argument_list|)
operator|*
name|bulkPlan
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Timeout-on-RIT="
operator|+
name|timeout
argument_list|)
expr_stmt|;
return|return
name|timeout
return|;
block|}
annotation|@
name|Override
specifier|protected
name|UncaughtExceptionHandler
name|getUncaughtExceptionHandler
parameter_list|()
block|{
return|return
operator|new
name|UncaughtExceptionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Assigning regions in "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
specifier|private
name|int
name|reassignFailedPlans
parameter_list|()
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|reassigningRegions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|e
range|:
name|failedPlans
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed assigning "
operator|+
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" regions to server "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|", reassigning them"
argument_list|)
expr_stmt|;
name|reassigningRegions
operator|.
name|addAll
argument_list|(
name|failedPlans
operator|.
name|remove
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RegionStates
name|regionStates
init|=
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|reassigningRegions
control|)
block|{
if|if
condition|(
operator|!
name|regionStates
operator|.
name|isRegionOnline
argument_list|(
name|region
argument_list|)
condition|)
block|{
name|assignmentManager
operator|.
name|invokeAssign
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|reassigningRegions
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Manage bulk assigning to a server.    */
specifier|static
class|class
name|SingleServerBulkAssigner
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|ServerName
name|regionserver
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|failedPlans
decl_stmt|;
name|SingleServerBulkAssigner
parameter_list|(
specifier|final
name|ServerName
name|regionserver
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
specifier|final
name|AssignmentManager
name|am
parameter_list|,
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|failedPlans
parameter_list|)
block|{
name|this
operator|.
name|regionserver
operator|=
name|regionserver
expr_stmt|;
name|this
operator|.
name|regions
operator|=
name|regions
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|am
expr_stmt|;
name|this
operator|.
name|failedPlans
operator|=
name|failedPlans
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
if|if
condition|(
operator|!
name|assignmentManager
operator|.
name|assign
argument_list|(
name|regionserver
argument_list|,
name|regions
argument_list|)
condition|)
block|{
name|failedPlans
operator|.
name|put
argument_list|(
name|regionserver
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed bulking assigning "
operator|+
name|regions
operator|.
name|size
argument_list|()
operator|+
literal|" region(s) to "
operator|+
name|regionserver
operator|.
name|getServerName
argument_list|()
operator|+
literal|", and continue to bulk assign others"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|failedPlans
operator|.
name|put
argument_list|(
name|regionserver
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

