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
name|zookeeper
package|;
end_package

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
name|Abortable
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
name|monitoring
operator|.
name|MonitoredTask
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
name|monitoring
operator|.
name|TaskMonitor
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
name|Writables
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
name|Writables
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
name|io
operator|.
name|Writable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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

begin_comment
comment|/**  * Region server schema change tracker. RS uses this tracker to keep track of  * alter schema requests from master and updates the status once the schema change  * is complete.  */
end_comment

begin_class
specifier|public
class|class
name|SchemaChangeTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SchemaChangeTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionServerServices
name|regionServer
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|int
name|sleepTimeMillis
init|=
literal|0
decl_stmt|;
comment|/**    * Constructs a new ZK node tracker.    *<p/>    *<p>After construction, use {@link #start} to kick off tracking.    *    * @param watcher    * @param node    * @param abortable    */
specifier|public
name|SchemaChangeTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
name|RegionServerServices
name|regionServer
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|schemaZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServer
operator|=
name|regionServer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
try|try
block|{
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|listChildrenAndWatchThem
argument_list|(
name|watcher
argument_list|,
name|node
argument_list|)
expr_stmt|;
comment|// Clean-up old in-process schema changes for this RS now?
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
literal|"RegionServer SchemaChangeTracker startup failed with "
operator|+
literal|"KeeperException."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This event will be triggered whenever new schema change request is processed by the    * master. The path will be of the format /hbase/schema/<table name>    * @param path full path of the node whose children have changed    */
annotation|@
name|Override
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"NodeChildrenChanged. Path = "
operator|+
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|watcher
operator|.
name|schemaZNode
argument_list|)
condition|)
block|{
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchThem
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|schemaZNode
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"RS.SchemaChangeTracker: "
operator|+
literal|"Current list of tables with schema change = "
operator|+
name|tables
argument_list|)
expr_stmt|;
if|if
condition|(
name|tables
operator|!=
literal|null
condition|)
block|{
name|handleSchemaChange
argument_list|(
name|tables
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"No tables found for schema change event."
operator|+
literal|" Skipping instant schema refresh"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|String
name|errmsg
init|=
literal|"KeeperException while handling nodeChildrenChanged for path = "
operator|+
name|path
operator|+
literal|" Cause = "
operator|+
name|ke
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|handleSchemaChange
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|tables
parameter_list|)
block|{
for|for
control|(
name|String
name|tableName
range|:
name|tables
control|)
block|{
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing schema change with status for table = "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|handleSchemaChange
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|handleSchemaChange
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|int
name|refreshedRegionsCount
init|=
literal|0
decl_stmt|,
name|onlineRegionsCount
init|=
literal|0
decl_stmt|;
name|MonitoredTask
name|status
init|=
literal|null
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|HRegion
argument_list|>
name|onlineRegions
init|=
name|regionServer
operator|.
name|getOnlineRegions
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|onlineRegions
operator|!=
literal|null
operator|&&
operator|!
name|onlineRegions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|status
operator|=
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
literal|"Region server "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" handling schema change for table = "
operator|+
name|tableName
operator|+
literal|" number of online regions = "
operator|+
name|onlineRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|onlineRegionsCount
operator|=
name|onlineRegions
operator|.
name|size
argument_list|()
expr_stmt|;
name|createStateNode
argument_list|(
name|tableName
argument_list|,
name|onlineRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegion
name|hRegion
range|:
name|onlineRegions
control|)
block|{
name|regionServer
operator|.
name|refreshRegion
argument_list|(
name|hRegion
argument_list|)
expr_stmt|;
name|refreshedRegionsCount
operator|++
expr_stmt|;
block|}
name|SchemaAlterStatus
name|alterStatus
init|=
name|getSchemaAlterStatus
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|alterStatus
operator|.
name|update
argument_list|(
name|SchemaAlterStatus
operator|.
name|AlterState
operator|.
name|SUCCESS
argument_list|,
name|refreshedRegionsCount
argument_list|)
expr_stmt|;
name|updateSchemaChangeStatus
argument_list|(
name|tableName
argument_list|,
name|alterStatus
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|"Refresh schema completed for table name = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" online Regions = "
operator|+
name|onlineRegions
operator|.
name|size
argument_list|()
operator|+
literal|" refreshed Regions = "
operator|+
name|refreshedRegionsCount
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Server "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" has no online regions for table = "
operator|+
name|tableName
operator|+
literal|" Ignoring the schema change request"
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
name|reportAndLogSchemaRefreshError
argument_list|(
name|tableName
argument_list|,
name|onlineRegionsCount
argument_list|,
name|refreshedRegionsCount
argument_list|,
name|ioe
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|reportAndLogSchemaRefreshError
argument_list|(
name|tableName
argument_list|,
name|onlineRegionsCount
argument_list|,
name|refreshedRegionsCount
argument_list|,
name|ke
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|getZKNodeVersion
parameter_list|(
name|String
name|nodePath
parameter_list|)
throws|throws
name|KeeperException
block|{
return|return
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|nodePath
argument_list|)
return|;
block|}
specifier|private
name|void
name|reportAndLogSchemaRefreshError
parameter_list|(
name|String
name|tableName
parameter_list|,
name|int
name|onlineRegionsCount
parameter_list|,
name|int
name|refreshedRegionsCount
parameter_list|,
name|Throwable
name|exception
parameter_list|,
name|MonitoredTask
name|status
parameter_list|)
block|{
try|try
block|{
name|String
name|errmsg
init|=
literal|" Region Server "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" failed during schema change process. Cause = "
operator|+
name|exception
operator|.
name|getCause
argument_list|()
operator|+
literal|" Number of onlineRegions = "
operator|+
name|onlineRegionsCount
operator|+
literal|" Processed regions = "
operator|+
name|refreshedRegionsCount
decl_stmt|;
name|SchemaAlterStatus
name|alterStatus
init|=
name|getSchemaAlterStatus
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|alterStatus
operator|.
name|update
argument_list|(
name|SchemaAlterStatus
operator|.
name|AlterState
operator|.
name|FAILURE
argument_list|,
name|refreshedRegionsCount
argument_list|,
name|errmsg
argument_list|)
expr_stmt|;
name|String
name|nodePath
init|=
name|getSchemaChangeNodePathForTableAndServer
argument_list|(
name|tableName
argument_list|,
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|updateExistingNodeData
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|nodePath
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|alterStatus
argument_list|)
argument_list|,
name|getZKNodeVersion
argument_list|(
name|nodePath
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"reportAndLogSchemaRefreshError() "
operator|+
literal|" Updated child ZKNode with SchemaAlterStatus = "
operator|+
name|alterStatus
operator|+
literal|" for table = "
operator|+
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
condition|)
block|{
name|status
operator|=
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|status
operator|.
name|setStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// Retry ?
name|String
name|errmsg
init|=
literal|"KeeperException while updating the schema change node with "
operator|+
literal|"error status for table = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Cause = "
operator|+
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// retry ??
name|String
name|errmsg
init|=
literal|"IOException while updating the schema change node with "
operator|+
literal|"server name for table = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Cause = "
operator|+
name|ioe
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createStateNode
parameter_list|(
name|String
name|tableName
parameter_list|,
name|int
name|numberOfOnlineRegions
parameter_list|)
throws|throws
name|IOException
block|{
name|SchemaAlterStatus
name|sas
init|=
operator|new
name|SchemaAlterStatus
argument_list|(
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|numberOfOnlineRegions
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating Schema Alter State node = "
operator|+
name|sas
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createSetData
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|getSchemaChangeNodePathForTableAndServer
argument_list|(
name|tableName
argument_list|,
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|sas
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|String
name|errmsg
init|=
literal|"KeeperException while creating the schema change node with "
operator|+
literal|"server name for table = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Message = "
operator|+
name|ke
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|,
name|ke
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SchemaAlterStatus
name|getSchemaAlterStatus
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|byte
index|[]
name|statusBytes
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|getSchemaChangeNodePathForTableAndServer
argument_list|(
name|tableName
argument_list|,
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|statusBytes
operator|==
literal|null
operator|||
name|statusBytes
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SchemaAlterStatus
name|sas
init|=
operator|new
name|SchemaAlterStatus
argument_list|()
decl_stmt|;
name|Writables
operator|.
name|getWritable
argument_list|(
name|statusBytes
argument_list|,
name|sas
argument_list|)
expr_stmt|;
return|return
name|sas
return|;
block|}
specifier|private
name|void
name|updateSchemaChangeStatus
parameter_list|(
name|String
name|tableName
parameter_list|,
name|SchemaAlterStatus
name|schemaAlterStatus
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|sleepTimeMillis
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SchemaChangeTracker sleeping for "
operator|+
name|sleepTimeMillis
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTimeMillis
argument_list|)
expr_stmt|;
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
block|}
block|}
name|ZKUtil
operator|.
name|updateExistingNodeData
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|getSchemaChangeNodePathForTableAndServer
argument_list|(
name|tableName
argument_list|,
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|schemaAlterStatus
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|"Schema change tracker completed for table = "
operator|+
name|tableName
operator|+
literal|" status = "
operator|+
name|schemaAlterStatus
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|e
parameter_list|)
block|{
name|String
name|errmsg
init|=
literal|"KeeperException.NoNodeException while updating the schema "
operator|+
literal|"change node with server name for table = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Cause = "
operator|+
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
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
comment|// Retry ?
name|String
name|errmsg
init|=
literal|"KeeperException while updating the schema change node with "
operator|+
literal|"server name for table = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Cause = "
operator|+
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|String
name|errmsg
init|=
literal|"IOException while updating the schema change node with "
operator|+
literal|"server name for table = "
operator|+
name|tableName
operator|+
literal|" server = "
operator|+
name|regionServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|+
literal|" Cause = "
operator|+
name|ioe
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getSchemaChangeNodePathForTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|schemaZNode
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|private
name|String
name|getSchemaChangeNodePathForTableAndServer
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|regionServerName
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|getSchemaChangeNodePathForTable
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|regionServerName
argument_list|)
return|;
block|}
specifier|public
name|int
name|getSleepTimeMillis
parameter_list|()
block|{
return|return
name|sleepTimeMillis
return|;
block|}
comment|/**    * Set a sleep time in millis before this RS can update it's progress status.    * Used only for test cases to test complex test scenarios such as RS failures and    * RS exemption handling.    * @param sleepTimeMillis    */
specifier|public
name|void
name|setSleepTimeMillis
parameter_list|(
name|int
name|sleepTimeMillis
parameter_list|)
block|{
name|this
operator|.
name|sleepTimeMillis
operator|=
name|sleepTimeMillis
expr_stmt|;
block|}
comment|/**    * Check whether there are any schema change requests that are in progress now for the given table.    * We simply assume that a schema change is in progress if we see a ZK schema node this    * any table. We may revisit for fine grained checks such as check the current alter status    * et al, but it is not required now.    * @return    */
specifier|public
name|boolean
name|isSchemaChangeInProgress
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|schemaChanges
init|=
name|ZKUtil
operator|.
name|listChildrenAndWatchThem
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|watcher
operator|.
name|schemaZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|schemaChanges
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|alterTableName
range|:
name|schemaChanges
control|)
block|{
if|if
condition|(
name|alterTableName
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"isSchemaChangeInProgress. "
operator|+
literal|"KeeperException while getting current schema change progress."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Holds the current alter state for a table. Alter state includes the    * current alter status (INPROCESS, FAILURE, SUCCESS, or IGNORED, current RS    * host name, timestamp of alter request, number of online regions this RS has for    * the given table, number of processed regions and an errorCause in case    * if the RS failed during the schema change process.    *    * RS keeps track of schema change requests per table using the alter status and    * periodically updates the alter status based on schema change status.    */
specifier|public
specifier|static
class|class
name|SchemaAlterStatus
implements|implements
name|Writable
block|{
specifier|public
enum|enum
name|AlterState
block|{
name|INPROCESS
block|,
comment|// Inprocess alter
name|SUCCESS
block|,
comment|// completed alter
name|FAILURE
block|,
comment|// failure alter
name|IGNORED
comment|// Ignore the alter processing.
block|}
specifier|private
name|AlterState
name|currentAlterStatus
decl_stmt|;
comment|// TimeStamp
specifier|private
name|long
name|stamp
decl_stmt|;
specifier|private
name|int
name|numberOfOnlineRegions
decl_stmt|;
specifier|private
name|String
name|errorCause
init|=
literal|" "
decl_stmt|;
specifier|private
name|String
name|hostName
decl_stmt|;
specifier|private
name|int
name|numberOfRegionsProcessed
init|=
literal|0
decl_stmt|;
specifier|public
name|SchemaAlterStatus
parameter_list|()
block|{      }
specifier|public
name|SchemaAlterStatus
parameter_list|(
name|String
name|hostName
parameter_list|,
name|int
name|numberOfOnlineRegions
parameter_list|)
block|{
name|this
operator|.
name|numberOfOnlineRegions
operator|=
name|numberOfOnlineRegions
expr_stmt|;
name|this
operator|.
name|stamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|currentAlterStatus
operator|=
name|AlterState
operator|.
name|INPROCESS
expr_stmt|;
comment|//this.rsToProcess = activeHosts;
name|this
operator|.
name|hostName
operator|=
name|hostName
expr_stmt|;
block|}
specifier|public
name|AlterState
name|getCurrentAlterStatus
parameter_list|()
block|{
return|return
name|currentAlterStatus
return|;
block|}
specifier|public
name|void
name|setCurrentAlterStatus
parameter_list|(
name|AlterState
name|currentAlterStatus
parameter_list|)
block|{
name|this
operator|.
name|currentAlterStatus
operator|=
name|currentAlterStatus
expr_stmt|;
block|}
specifier|public
name|int
name|getNumberOfOnlineRegions
parameter_list|()
block|{
return|return
name|numberOfOnlineRegions
return|;
block|}
specifier|public
name|void
name|setNumberOfOnlineRegions
parameter_list|(
name|int
name|numberOfRegions
parameter_list|)
block|{
name|this
operator|.
name|numberOfOnlineRegions
operator|=
name|numberOfRegions
expr_stmt|;
block|}
specifier|public
name|int
name|getNumberOfRegionsProcessed
parameter_list|()
block|{
return|return
name|numberOfRegionsProcessed
return|;
block|}
specifier|public
name|void
name|setNumberOfRegionsProcessed
parameter_list|(
name|int
name|numberOfRegionsProcessed
parameter_list|)
block|{
name|this
operator|.
name|numberOfRegionsProcessed
operator|=
name|numberOfRegionsProcessed
expr_stmt|;
block|}
specifier|public
name|String
name|getErrorCause
parameter_list|()
block|{
return|return
name|errorCause
return|;
block|}
specifier|public
name|void
name|setErrorCause
parameter_list|(
name|String
name|errorCause
parameter_list|)
block|{
name|this
operator|.
name|errorCause
operator|=
name|errorCause
expr_stmt|;
block|}
specifier|public
name|String
name|getHostName
parameter_list|()
block|{
return|return
name|hostName
return|;
block|}
specifier|public
name|void
name|setHostName
parameter_list|(
name|String
name|hostName
parameter_list|)
block|{
name|this
operator|.
name|hostName
operator|=
name|hostName
expr_stmt|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|AlterState
name|state
parameter_list|,
name|int
name|numberOfRegions
parameter_list|,
name|String
name|errorCause
parameter_list|)
block|{
name|this
operator|.
name|currentAlterStatus
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|numberOfRegionsProcessed
operator|=
name|numberOfRegions
expr_stmt|;
name|this
operator|.
name|errorCause
operator|=
name|errorCause
expr_stmt|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|AlterState
name|state
parameter_list|,
name|int
name|numberOfRegions
parameter_list|)
block|{
name|this
operator|.
name|currentAlterStatus
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|numberOfRegionsProcessed
operator|=
name|numberOfRegions
expr_stmt|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|AlterState
name|state
parameter_list|)
block|{
name|this
operator|.
name|currentAlterStatus
operator|=
name|state
expr_stmt|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|SchemaAlterStatus
name|status
parameter_list|)
block|{
name|this
operator|.
name|currentAlterStatus
operator|=
name|status
operator|.
name|getCurrentAlterStatus
argument_list|()
expr_stmt|;
name|this
operator|.
name|numberOfRegionsProcessed
operator|=
name|status
operator|.
name|getNumberOfRegionsProcessed
argument_list|()
expr_stmt|;
name|this
operator|.
name|errorCause
operator|=
name|status
operator|.
name|getErrorCause
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|currentAlterStatus
operator|=
name|AlterState
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
name|stamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|numberOfOnlineRegions
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|hostName
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|numberOfRegionsProcessed
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|errorCause
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|currentAlterStatus
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|stamp
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numberOfOnlineRegions
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hostName
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numberOfRegionsProcessed
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|errorCause
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|" state= "
operator|+
name|currentAlterStatus
operator|+
literal|", ts= "
operator|+
name|stamp
operator|+
literal|", number of online regions = "
operator|+
name|numberOfOnlineRegions
operator|+
literal|", host= "
operator|+
name|hostName
operator|+
literal|" processed regions = "
operator|+
name|numberOfRegionsProcessed
operator|+
literal|", errorCause = "
operator|+
name|errorCause
return|;
block|}
block|}
block|}
end_class

end_unit

