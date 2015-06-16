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
name|FileNotFoundException
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|HRegionLocation
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
name|InvalidFamilyOperationException
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
name|client
operator|.
name|Connection
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
name|RegionLocator
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
name|master
operator|.
name|BulkReOpen
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
name|zookeeper
operator|.
name|MetaTableLocator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_comment
comment|/**  * Base class for performing operations against tables.  * Checks on whether the process can go forward are done in constructor rather  * than later on in {@link #process()}.  The idea is to fail fast rather than  * later down in an async invocation of {@link #process()} (which currently has  * no means of reporting back issues once started).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|TableEventHandler
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
name|TableEventHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|MasterServices
name|masterServices
decl_stmt|;
specifier|protected
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|protected
name|TableLock
name|tableLock
decl_stmt|;
specifier|private
name|boolean
name|isPrepareCalled
init|=
literal|false
decl_stmt|;
specifier|public
name|TableEventHandler
parameter_list|(
name|EventType
name|eventType
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Server
name|server
parameter_list|,
name|MasterServices
name|masterServices
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
name|masterServices
operator|=
name|masterServices
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|TableEventHandler
name|prepare
parameter_list|()
throws|throws
name|IOException
block|{
comment|//acquire the table write lock, blocking
name|this
operator|.
name|tableLock
operator|=
name|masterServices
operator|.
name|getTableLockManager
argument_list|()
operator|.
name|writeLock
argument_list|(
name|tableName
argument_list|,
name|eventType
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
try|try
block|{
name|this
operator|.
name|masterServices
operator|.
name|checkTableModifiable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotDisabledException
name|ex
parameter_list|)
block|{
if|if
condition|(
name|isOnlineSchemaChangeAllowed
argument_list|()
operator|&&
name|eventType
operator|.
name|isOnlineSchemaChangeSupported
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring table not disabled exception "
operator|+
literal|"for supporting online schema changes."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|ex
throw|;
block|}
block|}
name|prepareWithTableLock
argument_list|()
expr_stmt|;
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
name|this
operator|.
name|isPrepareCalled
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Called from prepare() while holding the table lock. Subclasses    * can do extra initialization, and not worry about the releasing    * the table lock. */
specifier|protected
name|void
name|prepareWithTableLock
parameter_list|()
throws|throws
name|IOException
block|{   }
specifier|private
name|boolean
name|isOnlineSchemaChangeAllowed
parameter_list|()
block|{
return|return
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getBoolean
argument_list|(
literal|"hbase.online.schema.update.enable"
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isPrepareCalled
condition|)
block|{
comment|//For proper table locking semantics, the implementor should ensure to call
comment|//TableEventHandler.prepare() before calling process()
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Implementation should have called prepare() first"
argument_list|)
throw|;
block|}
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Handling table operation "
operator|+
name|eventType
operator|+
literal|" on table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
decl_stmt|;
if|if
condition|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|hris
operator|=
operator|new
name|MetaTableLocator
argument_list|()
operator|.
name|getMetaRegions
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hris
operator|=
name|MetaTableAccessor
operator|.
name|getTableRegions
argument_list|(
name|server
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
name|handleTableOperation
argument_list|(
name|hris
argument_list|)
expr_stmt|;
if|if
condition|(
name|eventType
operator|.
name|isOnlineSchemaChangeSupported
argument_list|()
operator|&&
name|this
operator|.
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
if|if
condition|(
name|reOpenAllRegions
argument_list|(
name|hris
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed table operation "
operator|+
name|eventType
operator|+
literal|" on table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error on reopening the regions"
argument_list|)
expr_stmt|;
block|}
block|}
name|completed
argument_list|(
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error manipulating table "
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
literal|"Error manipulating table "
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
finally|finally
block|{
name|releaseTableLock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
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
specifier|public
name|boolean
name|reOpenAllRegions
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|done
init|=
literal|false
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Bucketing regions by region server..."
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionLocations
init|=
literal|null
decl_stmt|;
name|Connection
name|connection
init|=
name|this
operator|.
name|masterServices
operator|.
name|getConnection
argument_list|()
decl_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|regionLocations
operator|=
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
expr_stmt|;
block|}
comment|// Convert List<HRegionLocation> to Map<HRegionInfo, ServerName>.
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|hri2Sn
init|=
operator|new
name|TreeMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|location
range|:
name|regionLocations
control|)
block|{
name|hri2Sn
operator|.
name|put
argument_list|(
name|location
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|location
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TreeMap
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serverToRegions
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|reRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|ServerName
name|sn
init|=
name|hri2Sn
operator|.
name|get
argument_list|(
name|hri
argument_list|)
decl_stmt|;
comment|// Skip the offlined split parent region
comment|// See HBASE-4578 for more information.
if|if
condition|(
literal|null
operator|==
name|sn
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skip "
operator|+
name|hri
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|!
name|serverToRegions
operator|.
name|containsKey
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|LinkedList
argument_list|<
name|HRegionInfo
argument_list|>
name|hriList
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
name|serverToRegions
operator|.
name|put
argument_list|(
name|sn
argument_list|,
name|hriList
argument_list|)
expr_stmt|;
block|}
name|reRegions
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|serverToRegions
operator|.
name|get
argument_list|(
name|sn
argument_list|)
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Reopening "
operator|+
name|reRegions
operator|.
name|size
argument_list|()
operator|+
literal|" regions on "
operator|+
name|serverToRegions
operator|.
name|size
argument_list|()
operator|+
literal|" region servers."
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|setRegionsToReopen
argument_list|(
name|reRegions
argument_list|)
expr_stmt|;
name|BulkReOpen
name|bulkReopen
init|=
operator|new
name|BulkReOpen
argument_list|(
name|this
operator|.
name|server
argument_list|,
name|serverToRegions
argument_list|,
name|this
operator|.
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
if|if
condition|(
name|bulkReopen
operator|.
name|bulkReOpen
argument_list|()
condition|)
block|{
name|done
operator|=
literal|true
expr_stmt|;
break|break;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Timeout before reopening all regions"
argument_list|)
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
literal|"Reopen was interrupted"
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
return|return
name|done
return|;
block|}
comment|/**    * Gets a TableDescriptor from the masterServices.  Can Throw exceptions.    *    * @return Table descriptor for this table    * @throws TableExistsException    * @throws FileNotFoundException    * @throws IOException    */
specifier|public
name|TableDescriptor
name|getTableDescriptor
parameter_list|()
throws|throws
name|FileNotFoundException
throws|,
name|IOException
block|{
name|TableDescriptor
name|htd
init|=
name|this
operator|.
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|tableName
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
literal|"HTableDescriptor missing for "
operator|+
name|tableName
argument_list|)
throw|;
block|}
return|return
name|htd
return|;
block|}
name|byte
index|[]
name|hasColumnFamily
parameter_list|(
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|byte
index|[]
name|cf
parameter_list|)
throws|throws
name|InvalidFamilyOperationException
block|{
if|if
condition|(
operator|!
name|htd
operator|.
name|hasFamily
argument_list|(
name|cf
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidFamilyOperationException
argument_list|(
literal|"Column family '"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
operator|+
literal|"' does not exist"
argument_list|)
throw|;
block|}
return|return
name|cf
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|handleTableOperation
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
throws|,
name|CoordinatedStateException
function_decl|;
block|}
end_class

end_unit

