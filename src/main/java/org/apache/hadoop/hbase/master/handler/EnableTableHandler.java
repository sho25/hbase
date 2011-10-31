begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|TableNotDisabledException
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
name|catalog
operator|.
name|CatalogTracker
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
name|catalog
operator|.
name|MetaReader
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Handler to run enable of a table.  */
end_comment

begin_class
specifier|public
class|class
name|EnableTableHandler
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
name|EnableTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableNameStr
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|CatalogTracker
name|ct
decl_stmt|;
specifier|public
name|EnableTableHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|boolean
name|skipTableStateCheck
parameter_list|)
throws|throws
name|TableNotFoundException
throws|,
name|TableNotDisabledException
throws|,
name|IOException
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|C_M_ENABLE_TABLE
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
name|tableNameStr
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|ct
operator|=
name|catalogTracker
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
comment|// Check if table exists
if|if
condition|(
operator|!
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|catalogTracker
argument_list|,
name|this
operator|.
name|tableNameStr
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
throw|;
block|}
comment|// There could be multiple client requests trying to disable or enable
comment|// the table at the same time. Ensure only the first request is honored
comment|// After that, no other requests can be accepted until the table reaches
comment|// DISABLED or ENABLED.
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
name|getZKTable
argument_list|()
operator|.
name|checkDisabledAndSetEnablingTable
argument_list|(
name|this
operator|.
name|tableNameStr
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableNameStr
operator|+
literal|" isn't disabled; skipping enable"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|TableNotDisabledException
argument_list|(
name|this
operator|.
name|tableNameStr
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to ensure that the table will be"
operator|+
literal|" enabling because of a ZooKeeper issue"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
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
name|tableNameStr
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
literal|"Attemping to enable the table "
operator|+
name|this
operator|.
name|tableNameStr
argument_list|)
expr_stmt|;
name|handleEnableTable
argument_list|()
expr_stmt|;
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
literal|"Error trying to enable the table "
operator|+
name|this
operator|.
name|tableNameStr
argument_list|,
name|e
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
literal|"Error trying to enable the table "
operator|+
name|this
operator|.
name|tableNameStr
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleEnableTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
comment|// I could check table is disabling and if so, not enable but require
comment|// that user first finish disabling but that might be obnoxious.
comment|// Set table enabling flag up in zk.
name|this
operator|.
name|assignmentManager
operator|.
name|getZKTable
argument_list|()
operator|.
name|setEnablingTable
argument_list|(
name|this
operator|.
name|tableNameStr
argument_list|)
expr_stmt|;
name|boolean
name|done
init|=
literal|false
decl_stmt|;
comment|// Get the regions of this table. We're done when all listed
comment|// tables are onlined.
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsInMeta
decl_stmt|;
name|regionsInMeta
operator|=
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|this
operator|.
name|ct
argument_list|,
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|int
name|countOfRegionsInTable
init|=
name|regionsInMeta
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|regionsToAssign
argument_list|(
name|regionsInMeta
argument_list|)
decl_stmt|;
name|int
name|regionsCount
init|=
name|regions
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionsCount
operator|==
literal|0
condition|)
block|{
name|done
operator|=
literal|true
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Table has "
operator|+
name|countOfRegionsInTable
operator|+
literal|" regions of which "
operator|+
name|regionsCount
operator|+
literal|" are offline."
argument_list|)
expr_stmt|;
name|BulkEnabler
name|bd
init|=
operator|new
name|BulkEnabler
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|regions
argument_list|,
name|countOfRegionsInTable
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
literal|"Enable was interrupted"
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
block|}
comment|// Flip the table to enabled.
if|if
condition|(
name|done
condition|)
name|this
operator|.
name|assignmentManager
operator|.
name|getZKTable
argument_list|()
operator|.
name|setEnabledTable
argument_list|(
name|this
operator|.
name|tableNameStr
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabled table is done="
operator|+
name|done
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param regionsInMeta This datastructure is edited by this method.    * @return The<code>regionsInMeta</code> list minus the regions that have    * been onlined; i.e. List of regions that need onlining.    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsToAssign
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsInMeta
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|onlineRegions
init|=
name|this
operator|.
name|assignmentManager
operator|.
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|regionsInMeta
operator|.
name|removeAll
argument_list|(
name|onlineRegions
argument_list|)
expr_stmt|;
return|return
name|regionsInMeta
return|;
block|}
comment|/**    * Run bulk enable.    */
class|class
name|BulkEnabler
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
comment|// Count of regions in table at time this assign was launched.
specifier|private
specifier|final
name|int
name|countOfRegionsInTable
decl_stmt|;
name|BulkEnabler
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
parameter_list|,
specifier|final
name|int
name|countOfRegionsInTable
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
name|this
operator|.
name|countOfRegionsInTable
operator|=
name|countOfRegionsInTable
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
throws|throws
name|IOException
block|{
name|boolean
name|roundRobinAssignment
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|roundRobinAssignment
condition|)
block|{
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
name|assignmentManager
operator|.
name|isRegionInTransition
argument_list|(
name|region
argument_list|)
operator|!=
literal|null
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
name|assign
argument_list|(
name|hri
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|assignmentManager
operator|.
name|assignUserRegionsToOnlineServers
argument_list|(
name|regions
argument_list|)
expr_stmt|;
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
literal|"Assignment was interrupted"
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
name|int
name|lastNumberOfRegions
init|=
name|this
operator|.
name|countOfRegionsInTable
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
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|isDone
argument_list|(
name|regions
argument_list|)
condition|)
break|break;
comment|// Punt on the timeout as long we make progress
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|>
name|lastNumberOfRegions
condition|)
block|{
name|lastNumberOfRegions
operator|=
name|regions
operator|.
name|size
argument_list|()
expr_stmt|;
name|timeout
operator|+=
name|waitingTimeForEvents
expr_stmt|;
block|}
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
name|isDone
argument_list|(
name|regions
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isDone
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
return|return
name|regions
operator|!=
literal|null
operator|&&
name|regions
operator|.
name|size
argument_list|()
operator|>=
name|this
operator|.
name|countOfRegionsInTable
return|;
block|}
block|}
block|}
end_class

end_unit

