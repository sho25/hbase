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
name|concurrent
operator|.
name|ExecutorService
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
name|CoordinatedStateException
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
name|TableNotEnabledException
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
name|TableNotFoundException
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
name|MetaTableAccessor
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
name|constraint
operator|.
name|ConstraintException
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
name|BulkAssigner
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
name|HMaster
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
name|MasterCoprocessorHost
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
name|TableLockManager
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
name|TableLockManager
operator|.
name|TableLock
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
name|htrace
operator|.
name|Trace
import|;
end_import

begin_comment
comment|/**  * Handler to run disable of a table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DisableTableHandler
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
name|DisableTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|TableLockManager
name|tableLockManager
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|skipTableStateCheck
decl_stmt|;
specifier|private
name|TableLock
name|tableLock
decl_stmt|;
specifier|public
name|DisableTableHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|TableLockManager
name|tableLockManager
parameter_list|,
name|boolean
name|skipTableStateCheck
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|C_M_DISABLE_TABLE
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
name|this
operator|.
name|tableLockManager
operator|=
name|tableLockManager
expr_stmt|;
name|this
operator|.
name|skipTableStateCheck
operator|=
name|skipTableStateCheck
expr_stmt|;
block|}
specifier|public
name|DisableTableHandler
name|prepare
parameter_list|()
throws|throws
name|TableNotFoundException
throws|,
name|TableNotEnabledException
throws|,
name|IOException
block|{
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ConstraintException
argument_list|(
literal|"Cannot disable catalog table"
argument_list|)
throw|;
block|}
comment|//acquire the table write lock, blocking
name|this
operator|.
name|tableLock
operator|=
name|this
operator|.
name|tableLockManager
operator|.
name|writeLock
argument_list|(
name|tableName
argument_list|,
name|EventType
operator|.
name|C_M_DISABLE_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableLock
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// Check if table exists
if|if
condition|(
operator|!
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|this
operator|.
name|server
operator|.
name|getShortCircuitConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|tableName
argument_list|)
throw|;
block|}
comment|// There could be multiple client requests trying to disable or enable
comment|// the table at the same time. Ensure only the first request is honored
comment|// After that, no other requests can be accepted until the table reaches
comment|// DISABLED or ENABLED.
comment|//TODO: reevaluate this since we have table locks now
if|if
condition|(
operator|!
name|skipTableStateCheck
condition|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableStateIfInStates
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" isn't enabled; skipping disable"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|TableNotEnabledException
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|CoordinatedStateException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to ensure that the table will be"
operator|+
literal|" disabling because of a coordination engine issue"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|releaseTableLock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|"UnknownServerName"
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
operator|+
literal|"-"
operator|+
name|tableName
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to disable table "
operator|+
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|MasterCoprocessorHost
name|cpHost
init|=
operator|(
operator|(
name|HMaster
operator|)
name|this
operator|.
name|server
operator|)
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|preDisableTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
name|handleDisableTable
argument_list|()
expr_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postDisableTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
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
literal|"Error trying to disable table "
operator|+
name|this
operator|.
name|tableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CoordinatedStateException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error trying to disable table "
operator|+
name|this
operator|.
name|tableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|releaseTableLock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|releaseTableLock
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|tableLock
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|tableLock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not release the table lock"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|handleDisableTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|CoordinatedStateException
block|{
comment|// Set table disabling flag up in zk.
name|this
operator|.
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|)
expr_stmt|;
name|boolean
name|done
init|=
literal|false
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|// Get list of online regions that are of this table.  Regions that are
comment|// already closed will not be included in this list; i.e. the returned
comment|// list is not ALL regions in a table, its all online regions according
comment|// to the in-memory state on this master.
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|this
operator|.
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|done
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Offlining "
operator|+
name|regions
operator|.
name|size
argument_list|()
operator|+
literal|" regions."
argument_list|)
expr_stmt|;
name|BulkDisabler
name|bd
init|=
operator|new
name|BulkDisabler
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|regions
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|bd
operator|.
name|bulkAssign
argument_list|()
condition|)
block|{
name|done
operator|=
literal|true
expr_stmt|;
break|break;
block|}
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
literal|"Disable was interrupted"
argument_list|)
expr_stmt|;
comment|// Preserve the interrupt.
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
comment|// Flip the table to disabled if success.
if|if
condition|(
name|done
condition|)
name|this
operator|.
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabled table, "
operator|+
name|this
operator|.
name|tableName
operator|+
literal|", is done="
operator|+
name|done
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run bulk disable.    */
class|class
name|BulkDisabler
extends|extends
name|BulkAssigner
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
decl_stmt|;
name|BulkDisabler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|this
operator|.
name|regions
operator|=
name|regions
expr_stmt|;
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
name|regions
control|)
block|{
if|if
condition|(
name|regionStates
operator|.
name|isRegionInTransition
argument_list|(
name|region
argument_list|)
operator|&&
operator|!
name|regionStates
operator|.
name|isRegionInState
argument_list|(
name|region
argument_list|,
name|State
operator|.
name|FAILED_CLOSE
argument_list|)
condition|)
block|{
continue|continue;
block|}
specifier|final
name|HRegionInfo
name|hri
init|=
name|region
decl_stmt|;
name|pool
operator|.
name|execute
argument_list|(
name|Trace
operator|.
name|wrap
argument_list|(
literal|"DisableTableHandler.BulkDisabler"
argument_list|,
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|assignmentManager
operator|.
name|unassign
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|waitUntilDone
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|remaining
init|=
name|timeout
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|server
operator|.
name|isStopped
argument_list|()
operator|&&
name|remaining
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitingTimeForEvents
argument_list|)
expr_stmt|;
name|regions
operator|=
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Disable waiting until done; "
operator|+
name|remaining
operator|+
literal|" ms remaining; "
operator|+
name|regions
argument_list|)
expr_stmt|;
if|if
condition|(
name|regions
operator|.
name|isEmpty
argument_list|()
condition|)
break|break;
name|remaining
operator|=
name|timeout
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
expr_stmt|;
block|}
return|return
name|regions
operator|!=
literal|null
operator|&&
name|regions
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

