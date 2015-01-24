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
name|namespace
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
name|HBaseIOException
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
name|NamespaceDescriptor
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
name|quotas
operator|.
name|QuotaExceededException
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
comment|/**  * The Class NamespaceAuditor performs checks to ensure operations like table creation  * and region splitting preserve namespace quota. The namespace quota can be specified  * while namespace creation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|NamespaceAuditor
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|NamespaceAuditor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|NS_AUDITOR_INIT_TIMEOUT
init|=
literal|"hbase.namespace.auditor.init.timeout"
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_NS_AUDITOR_INIT_TIMEOUT
init|=
literal|120000
decl_stmt|;
specifier|private
name|NamespaceStateManager
name|stateManager
decl_stmt|;
specifier|private
name|MasterServices
name|masterServices
decl_stmt|;
specifier|public
name|NamespaceAuditor
parameter_list|(
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|this
operator|.
name|masterServices
operator|=
name|masterServices
expr_stmt|;
name|stateManager
operator|=
operator|new
name|NamespaceStateManager
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|stateManager
operator|.
name|start
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|int
name|timeout
init|=
name|masterServices
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|NS_AUDITOR_INIT_TIMEOUT
argument_list|,
name|DEFAULT_NS_AUDITOR_INIT_TIMEOUT
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|stateManager
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
if|if
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
operator|+
literal|1000
operator|>
name|timeout
condition|)
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
literal|"Timed out waiting for namespace auditor to be initialized."
argument_list|)
throw|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
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
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"NamespaceAuditor started."
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check quota to create table.    * We add the table information to namespace state cache, assuming the operation will    * pass. If the operation fails, then the next time namespace state chore runs    * namespace state cache will be corrected.    *    * @param tName - The table name to check quota.    * @param regions - Number of regions that will be added.    * @throws IOException Signals that an I/O exception has occurred.    */
specifier|public
name|void
name|checkQuotaToCreateTable
parameter_list|(
name|TableName
name|tName
parameter_list|,
name|int
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|stateManager
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
comment|// We do this check to fail fast.
if|if
condition|(
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|this
operator|.
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableExistsException
argument_list|(
name|tName
argument_list|)
throw|;
block|}
name|stateManager
operator|.
name|checkAndUpdateNamespaceTableCount
argument_list|(
name|tName
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|checkTableTypeAndThrowException
argument_list|(
name|tName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkTableTypeAndThrowException
parameter_list|(
name|TableName
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|name
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Namespace auditor checks not performed for table "
operator|+
name|name
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HBaseIOException
argument_list|(
name|name
operator|+
literal|" is being created even before namespace auditor has been initialized."
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|checkQuotaToSplitRegion
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|stateManager
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Split operation is being performed even before namespace auditor is initialized."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|!
name|stateManager
operator|.
name|checkAndUpdateNamespaceRegionCount
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|QuotaExceededException
argument_list|(
literal|"Region split not possible for :"
operator|+
name|hri
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" as quota limits are exceeded "
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|addNamespace
parameter_list|(
name|NamespaceDescriptor
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|stateManager
operator|.
name|addNamespace
argument_list|(
name|ns
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deleteNamespace
parameter_list|(
name|String
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
name|stateManager
operator|.
name|deleteNamespace
argument_list|(
name|namespace
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|removeFromNamespaceUsage
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|stateManager
operator|.
name|removeTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|removeRegionFromNamespaceUsage
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|stateManager
operator|.
name|removeRegionFromTable
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
comment|/**    * Used only for unit tests.    * @param namespace The name of the namespace    * @return An instance of NamespaceTableAndRegionInfo    */
annotation|@
name|VisibleForTesting
name|NamespaceTableAndRegionInfo
name|getState
parameter_list|(
name|String
name|namespace
parameter_list|)
block|{
if|if
condition|(
name|stateManager
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
return|return
name|stateManager
operator|.
name|getState
argument_list|(
name|namespace
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Checks if namespace auditor is initialized. Used only for testing.    *    * @return true, if is initialized    */
specifier|public
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
name|stateManager
operator|.
name|isInitialized
argument_list|()
return|;
block|}
block|}
end_class

end_unit

