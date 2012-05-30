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
name|ArrayList
import|;
end_import

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
name|ServerManager
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
name|wal
operator|.
name|HLog
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
specifier|private
name|MasterFileSystem
name|fileSystemManager
decl_stmt|;
specifier|private
specifier|final
name|HTableDescriptor
name|hTableDescriptor
decl_stmt|;
specifier|private
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
name|ServerManager
name|serverManager
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
index|[]
name|newRegions
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
name|ServerManager
name|serverManager
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
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|)
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|TableExistsException
throws|,
name|IOException
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
name|serverManager
operator|=
name|serverManager
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
name|catalogTracker
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
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
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
throw|throw
operator|new
name|TableExistsException
argument_list|(
name|tableName
argument_list|)
throw|;
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
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to create the table "
operator|+
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
name|IOException
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
literal|"Error trying to create the table "
operator|+
name|tableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleCreateTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
comment|// TODO: Currently we make the table descriptor and as side-effect the
comment|// tableDir is created.  Should we change below method to be createTable
comment|// where we create table in tmp dir with its table descriptor file and then
comment|// do rename to move it into place?
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|this
operator|.
name|hTableDescriptor
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|batchSize
init|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.master.createtable.batchsize"
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|HLog
name|hlog
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|regionIdx
init|=
literal|0
init|;
name|regionIdx
operator|<
name|this
operator|.
name|newRegions
operator|.
name|length
condition|;
name|regionIdx
operator|++
control|)
block|{
name|HRegionInfo
name|newRegion
init|=
name|this
operator|.
name|newRegions
index|[
name|regionIdx
index|]
decl_stmt|;
comment|// 1. Create HRegion
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|newRegion
argument_list|,
name|this
operator|.
name|fileSystemManager
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|hTableDescriptor
argument_list|,
name|hlog
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|hlog
operator|==
literal|null
condition|)
block|{
name|hlog
operator|=
name|region
operator|.
name|getLog
argument_list|()
expr_stmt|;
block|}
name|regionInfos
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionIdx
operator|%
name|batchSize
operator|==
literal|0
condition|)
block|{
comment|// 2. Insert into META
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
name|regionInfos
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// 3. Close the new region to flush to disk.  Close log file too.
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|hlog
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
if|if
condition|(
name|regionInfos
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
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
block|}
comment|// 4. Trigger immediate assignment of the regions in round-robin fashion
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|serverManager
operator|.
name|createDestinationServersList
argument_list|()
decl_stmt|;
try|try
block|{
name|this
operator|.
name|assignmentManager
operator|.
name|assignUserRegions
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|newRegions
argument_list|)
argument_list|,
name|servers
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught "
operator|+
name|ie
operator|+
literal|" during round-robin assignment"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|ie
argument_list|)
throw|;
block|}
comment|// 5. Set table enabled flag up in zk.
try|try
block|{
name|assignmentManager
operator|.
name|getZKTable
argument_list|()
operator|.
name|setEnabledTable
argument_list|(
name|this
operator|.
name|hTableDescriptor
operator|.
name|getNameAsString
argument_list|()
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
literal|"Unable to ensure that the table will be"
operator|+
literal|" enabled because of a ZooKeeper issue"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

