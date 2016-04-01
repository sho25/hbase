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
name|procedure
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
name|util
operator|.
name|Bytes
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
comment|/**  * Helper class for schema change procedures  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|MasterDDLOperationHelper
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
name|MasterDDLOperationHelper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MasterDDLOperationHelper
parameter_list|()
block|{}
comment|/**    * Check whether online schema change is allowed from config    **/
specifier|public
specifier|static
name|boolean
name|isOnlineSchemaChangeAllowed
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|env
operator|.
name|getMasterServices
argument_list|()
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
comment|/**    * Check whether a table is modifiable - exists and either offline or online with config set    * @param env MasterProcedureEnv    * @param tableName name of the table    * @throws IOException    */
specifier|public
specifier|static
name|void
name|checkTableModifiable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Checks whether the table exists
if|if
condition|(
operator|!
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
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
name|TableNotFoundException
argument_list|(
name|tableName
argument_list|)
throw|;
block|}
comment|// We only execute this procedure with table online if online schema change config is set.
if|if
condition|(
operator|!
name|env
operator|.
name|getMasterServices
argument_list|()
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
name|DISABLED
argument_list|)
operator|&&
operator|!
name|MasterDDLOperationHelper
operator|.
name|isOnlineSchemaChangeAllowed
argument_list|(
name|env
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotDisabledException
argument_list|(
name|tableName
argument_list|)
throw|;
block|}
block|}
comment|/**    * Remove the column family from the file system    **/
specifier|public
specifier|static
name|void
name|deleteColumnFamilyFromFileSystem
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfoList
parameter_list|,
specifier|final
name|byte
index|[]
name|familyName
parameter_list|,
name|boolean
name|hasMob
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|MasterFileSystem
name|mfs
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing family="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
operator|+
literal|" from table="
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionInfoList
operator|==
literal|null
condition|)
block|{
name|regionInfoList
operator|=
name|ProcedureSyncWait
operator|.
name|getRegionsFromMeta
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionInfoList
control|)
block|{
comment|// Delete the family directory in FS for all the regions one by one
name|mfs
operator|.
name|deleteFamilyFromFS
argument_list|(
name|hri
argument_list|,
name|familyName
argument_list|,
name|hasMob
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Reopen all regions from a table after a schema change operation.    **/
specifier|public
specifier|static
name|boolean
name|reOpenAllRegions
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfoList
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
name|env
operator|.
name|getMasterServices
argument_list|()
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
name|regionInfoList
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
name|AssignmentManager
name|am
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|am
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
name|env
operator|.
name|getMasterServices
argument_list|()
argument_list|,
name|serverToRegions
argument_list|,
name|am
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
comment|/**    * Get the region info list of a table from meta if it is not already known by the caller.    **/
specifier|public
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getRegionInfoList
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfoList
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|regionInfoList
operator|==
literal|null
condition|)
block|{
name|regionInfoList
operator|=
name|ProcedureSyncWait
operator|.
name|getRegionsFromMeta
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
return|return
name|regionInfoList
return|;
block|}
block|}
end_class

end_unit

