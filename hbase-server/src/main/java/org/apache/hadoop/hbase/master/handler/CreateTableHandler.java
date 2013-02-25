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
name|exceptions
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
name|exceptions
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
name|MetaEditor
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
name|ModifyRegionUtils
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
name|CatalogTracker
name|catalogTracker
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
name|TableLock
name|tableLock
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
name|catalogTracker
operator|=
name|masterServices
operator|.
name|getCatalogTracker
argument_list|()
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
name|getName
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
comment|// Need META availability to create a table
try|try
block|{
if|if
condition|(
name|catalogTracker
operator|.
name|waitForMeta
argument_list|(
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
name|String
name|tableName
init|=
name|this
operator|.
name|hTableDescriptor
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
if|if
condition|(
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|catalogTracker
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
comment|// If we have multiple client threads trying to create the table at the
comment|// same time, given the async nature of the operation, the table
comment|// could be in a state where .META. table hasn't been updated yet in
comment|// the process() function.
comment|// Use enabling state to tell if there is already a request for the same
comment|// table in progress. This will introduce a new zookeeper call. Given
comment|// createTable isn't a frequent operation, that should be ok.
comment|//TODO: now that we have table locks, re-evaluate above
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
name|checkAndSetEnablingTable
argument_list|(
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
name|getNameAsString
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
name|String
name|tableName
init|=
name|this
operator|.
name|hTableDescriptor
operator|.
name|getNameAsString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to create the table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
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
name|getCoprocessorHost
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
name|cpHost
operator|.
name|postCreateTableHandler
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
block|{   }
comment|/**    * Responsible of table creation (on-disk and META) and assignment.    * - Create the table directory and descriptor (temp folder)    * - Create the on-disk regions (temp folder)    *   [If something fails here: we've just some trash in temp]    * - Move the table from temp to the root directory    *   [If something fails here: we've the table in place but some of the rows required    *    present in META. (hbck needed)]    * - Add regions to META    *   [If something fails here: we don't have regions assigned: table disabled]    * - Assign regions to Region Servers    *   [If something fails here: we still have the table in disabled state]    * - Update ZooKeeper with the enabled state    */
specifier|private
name|void
name|handleCreateTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
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
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|tempdir
argument_list|,
name|this
operator|.
name|hTableDescriptor
argument_list|)
expr_stmt|;
name|Path
name|tempTableDir
init|=
operator|new
name|Path
argument_list|(
name|tempdir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Path
name|tableDir
init|=
operator|new
name|Path
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
name|MetaEditor
operator|.
name|addRegionsToMeta
argument_list|(
name|this
operator|.
name|catalogTracker
argument_list|,
name|regionInfos
argument_list|)
expr_stmt|;
comment|// 5. Trigger immediate assignment of the regions in round-robin fashion
try|try
block|{
name|assignmentManager
operator|.
name|getRegionStates
argument_list|()
operator|.
name|createRegionStates
argument_list|(
name|regionInfos
argument_list|)
expr_stmt|;
name|assignmentManager
operator|.
name|assign
argument_list|(
name|regionInfos
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
name|error
argument_list|(
literal|"Caught "
operator|+
name|e
operator|+
literal|" during round-robin assignment"
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
block|}
comment|// 6. Set table enabled flag up in zk.
try|try
block|{
name|assignmentManager
operator|.
name|getZKTable
argument_list|()
operator|.
name|setEnabledTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
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
literal|"Unable to ensure that "
operator|+
name|tableName
operator|+
literal|" will be"
operator|+
literal|" enabled because of a ZooKeeper issue"
argument_list|,
name|e
argument_list|)
throw|;
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
name|String
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
block|}
end_class

end_unit

