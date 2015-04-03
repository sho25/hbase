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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|List
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HTableDescriptor
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
name|NotAllMetaRegionsOnlineException
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
name|TableExistsException
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
name|RegionReplicaUtil
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
name|TableState
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
name|ipc
operator|.
name|RpcServer
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
name|MasterFileSystem
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
name|MasterServices
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
name|security
operator|.
name|User
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
name|security
operator|.
name|UserProvider
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
name|FSTableDescriptors
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
name|FSUtils
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
name|ModifyRegionUtils
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
name|ServerRegionReplicaUtil
import|;
end_import

begin_comment
comment|/**  * Handler to create a table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CreateTableHandler
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
name|CreateTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|MasterFileSystem
name|fileSystemManager
decl_stmt|;
specifier|protected
specifier|final
name|HTableDescriptor
name|hTableDescriptor
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
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
name|HRegionInfo
index|[]
name|newRegions
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
specifier|final
name|TableLock
name|tableLock
decl_stmt|;
specifier|private
name|User
name|activeUser
decl_stmt|;
specifier|public
name|CreateTableHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|MasterFileSystem
name|fileSystemManager
parameter_list|,
name|HTableDescriptor
name|hTableDescriptor
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
index|[]
name|newRegions
parameter_list|,
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|C_M_CREATE_TABLE
argument_list|)
expr_stmt|;
name|this
operator|.
name|fileSystemManager
operator|=
name|fileSystemManager
expr_stmt|;
name|this
operator|.
name|hTableDescriptor
operator|=
name|hTableDescriptor
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|newRegions
operator|=
name|newRegions
expr_stmt|;
name|this
operator|.
name|masterServices
operator|=
name|masterServices
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableLockManager
operator|=
name|masterServices
operator|.
name|getTableLockManager
argument_list|()
expr_stmt|;
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
name|this
operator|.
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|EventType
operator|.
name|C_M_CREATE_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CreateTableHandler
name|prepare
parameter_list|()
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|TableExistsException
throws|,
name|IOException
block|{
name|int
name|timeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.client.catalog.timeout"
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
comment|// Need hbase:meta availability to create a table
try|try
block|{
if|if
condition|(
name|server
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|waitMetaRegionLocation
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|timeout
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NotAllMetaRegionsOnlineException
argument_list|()
throw|;
block|}
comment|// If we are creating the table in service to an RPC request, record the
comment|// active user for later, so proper permissions will be applied to the
comment|// new table by the AccessController if it is active
name|this
operator|.
name|activeUser
operator|=
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|activeUser
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|activeUser
operator|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
operator|.
name|getCurrent
argument_list|()
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
literal|"Interrupted waiting for meta availability"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|InterruptedIOException
name|ie
init|=
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|ie
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ie
throw|;
block|}
comment|//acquire the table write lock, blocking. Make sure that it is released.
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
name|TableName
name|tableName
init|=
name|this
operator|.
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|this
operator|.
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableExistsException
argument_list|(
name|tableName
argument_list|)
throw|;
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
name|this
operator|.
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
name|TableName
name|tableName
init|=
name|this
operator|.
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Create table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|HMaster
name|master
init|=
operator|(
operator|(
name|HMaster
operator|)
name|this
operator|.
name|server
operator|)
decl_stmt|;
try|try
block|{
specifier|final
name|MasterCoprocessorHost
name|cpHost
init|=
name|master
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
name|preCreateTableHandler
argument_list|(
name|this
operator|.
name|hTableDescriptor
argument_list|,
name|this
operator|.
name|newRegions
argument_list|)
expr_stmt|;
block|}
name|handleCreateTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|completed
argument_list|(
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|activeUser
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|cpHost
operator|.
name|postCreateTableHandler
argument_list|(
name|hTableDescriptor
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error trying to create the table "
operator|+
name|tableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|master
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
try|try
block|{
operator|(
operator|(
name|HMaster
operator|)
name|this
operator|.
name|server
operator|)
operator|.
name|getMasterQuotaManager
argument_list|()
operator|.
name|removeTableFromNamespaceQuota
argument_list|(
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error trying to update namespace quota "
operator|+
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
name|completed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Called after that process() is completed.    * @param exception null if process() is successful or not null if something has failed.    */
specifier|protected
name|void
name|completed
parameter_list|(
specifier|final
name|Throwable
name|exception
parameter_list|)
block|{
name|releaseTableLock
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table, "
operator|+
name|this
operator|.
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
operator|+
literal|", creation "
operator|+
operator|(
name|exception
operator|==
literal|null
condition|?
literal|"successful"
else|:
literal|"failed. "
operator|+
name|exception
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Responsible of table creation (on-disk and META) and assignment.    * - Create the table directory and descriptor (temp folder)    * - Create the on-disk regions (temp folder)    *   [If something fails here: we've just some trash in temp]    * - Move the table from temp to the root directory    *   [If something fails here: we've the table in place but some of the rows required    *    present in META. (hbck needed)]    * - Add regions to META    *   [If something fails here: we don't have regions assigned: table disabled]    * - Assign regions to Region Servers    *   [If something fails here: we still have the table in disabled state]    * - Update ZooKeeper with the enabled state    */
specifier|private
name|void
name|handleCreateTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
throws|,
name|CoordinatedStateException
block|{
name|Path
name|tempdir
init|=
name|fileSystemManager
operator|.
name|getTempDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|fileSystemManager
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// 1. Create Table Descriptor
comment|// using a copy of descriptor, table will be created enabling first
name|TableDescriptor
name|underConstruction
init|=
operator|new
name|TableDescriptor
argument_list|(
name|this
operator|.
name|hTableDescriptor
argument_list|)
decl_stmt|;
name|Path
name|tempTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|tempdir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
operator|(
call|(
name|FSTableDescriptors
call|)
argument_list|(
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
argument_list|)
operator|)
operator|.
name|createTableDescriptorForTableDirectory
argument_list|(
name|tempTableDir
argument_list|,
name|underConstruction
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|fileSystemManager
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// 2. Create Regions
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
name|handleCreateHdfsRegions
argument_list|(
name|tempdir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// 3. Move Table temp directory to the hbase root location
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|tempTableDir
argument_list|,
name|tableDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to move table from temp="
operator|+
name|tempTableDir
operator|+
literal|" to hbase root="
operator|+
name|tableDir
argument_list|)
throw|;
block|}
comment|// populate descriptors cache to be visible in getAll
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|updateTableState
argument_list|(
name|this
operator|.
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionInfos
operator|!=
literal|null
operator|&&
name|regionInfos
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// 4. Add regions to META
name|addRegionsToMeta
argument_list|(
name|regionInfos
argument_list|,
name|hTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
expr_stmt|;
comment|// 5. Add replicas if needed
name|regionInfos
operator|=
name|addReplicas
argument_list|(
name|hTableDescriptor
argument_list|,
name|regionInfos
argument_list|)
expr_stmt|;
comment|// 6. Setup replication for region replicas if needed
if|if
condition|(
name|hTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
operator|>
literal|1
condition|)
block|{
name|ServerRegionReplicaUtil
operator|.
name|setupRegionReplicaReplication
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|// 7. Trigger immediate assignment of the regions in round-robin fashion
name|ModifyRegionUtils
operator|.
name|assignRegions
argument_list|(
name|assignmentManager
argument_list|,
name|regionInfos
argument_list|)
expr_stmt|;
block|}
comment|// 8. Enable table
name|assignmentManager
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
expr_stmt|;
comment|// 9. Update the tabledescriptor cache.
operator|(
operator|(
name|HMaster
operator|)
name|this
operator|.
name|server
operator|)
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create any replicas for the regions (the default replicas that was    * already created is passed to the method)    * @param hTableDescriptor descriptor to use    * @param regions default replicas    * @return the combined list of default and non-default replicas    */
specifier|protected
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|addReplicas
parameter_list|(
name|HTableDescriptor
name|hTableDescriptor
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|int
name|numRegionReplicas
init|=
name|hTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|numRegionReplicas
operator|<=
literal|0
condition|)
block|{
return|return
name|regions
return|;
block|}
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hRegionInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
operator|(
name|numRegionReplicas
operator|+
literal|1
operator|)
operator|*
name|regions
operator|.
name|size
argument_list|()
argument_list|)
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
name|regions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<=
name|numRegionReplicas
condition|;
name|j
operator|++
control|)
block|{
name|hRegionInfos
operator|.
name|add
argument_list|(
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|regions
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|hRegionInfos
operator|.
name|addAll
argument_list|(
name|regions
argument_list|)
expr_stmt|;
return|return
name|hRegionInfos
return|;
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
comment|/**    * Create the on-disk structure for the table, and returns the regions info.    * @param tableRootDir directory where the table is being created    * @param tableName name of the table under construction    * @return the list of regions created    */
specifier|protected
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|handleCreateHdfsRegions
parameter_list|(
specifier|final
name|Path
name|tableRootDir
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ModifyRegionUtils
operator|.
name|createRegions
argument_list|(
name|conf
argument_list|,
name|tableRootDir
argument_list|,
name|hTableDescriptor
argument_list|,
name|newRegions
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Add the specified set of regions to the hbase:meta table.    */
specifier|protected
name|void
name|addRegionsToMeta
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
parameter_list|,
name|int
name|regionReplication
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|addRegionsToMeta
argument_list|(
name|this
operator|.
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regionInfos
argument_list|,
name|regionReplication
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

