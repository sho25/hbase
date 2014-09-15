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
name|Sets
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
name|TableDescriptors
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
name|client
operator|.
name|TableState
import|;
end_import

begin_comment
comment|/**  * This is a helper class used to manage table states.  * States persisted in tableinfo and cached internally.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableStateManager
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
name|TableStateManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TableDescriptors
name|descriptors
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|TableState
operator|.
name|State
argument_list|>
name|tableStates
init|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
specifier|public
name|TableStateManager
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
name|this
operator|.
name|descriptors
operator|=
name|master
operator|.
name|getTableDescriptors
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|all
init|=
name|descriptors
operator|.
name|getAllDescriptors
argument_list|()
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|table
range|:
name|all
operator|.
name|values
argument_list|()
control|)
block|{
name|TableName
name|tableName
init|=
name|table
operator|.
name|getHTableDescriptor
argument_list|()
operator|.
name|getTableName
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
literal|"Adding table state: "
operator|+
name|tableName
operator|+
literal|": "
operator|+
name|table
operator|.
name|getTableState
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|tableStates
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|table
operator|.
name|getTableState
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Set table state to provided.    * Caller should lock table on write.    * @param tableName table to change state for    * @param newState new state    * @throws IOException    */
specifier|public
name|void
name|setTableState
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|TableState
operator|.
name|State
name|newState
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|tableStates
init|)
block|{
name|TableDescriptor
name|descriptor
init|=
name|readDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|descriptor
operator|==
literal|null
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
if|if
condition|(
name|descriptor
operator|.
name|getTableState
argument_list|()
operator|!=
name|newState
condition|)
block|{
name|writeDescriptor
argument_list|(
operator|new
name|TableDescriptor
argument_list|(
name|descriptor
operator|.
name|getHTableDescriptor
argument_list|()
argument_list|,
name|newState
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Set table state to provided but only if table in specified states    * Caller should lock table on write.    * @param tableName table to change state for    * @param newState new state    * @param states states to check against    * @throws IOException    */
specifier|public
name|boolean
name|setTableStateIfInStates
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|TableState
operator|.
name|State
name|newState
parameter_list|,
name|TableState
operator|.
name|State
modifier|...
name|states
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|tableStates
init|)
block|{
name|TableDescriptor
name|descriptor
init|=
name|readDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|descriptor
operator|==
literal|null
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
if|if
condition|(
name|TableState
operator|.
name|isInStates
argument_list|(
name|descriptor
operator|.
name|getTableState
argument_list|()
argument_list|,
name|states
argument_list|)
condition|)
block|{
name|writeDescriptor
argument_list|(
operator|new
name|TableDescriptor
argument_list|(
name|descriptor
operator|.
name|getHTableDescriptor
argument_list|()
argument_list|,
name|newState
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
comment|/**    * Set table state to provided but only if table not in specified states    * Caller should lock table on write.    * @param tableName table to change state for    * @param newState new state    * @param states states to check against    * @throws IOException    */
specifier|public
name|boolean
name|setTableStateIfNotInStates
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|TableState
operator|.
name|State
name|newState
parameter_list|,
name|TableState
operator|.
name|State
modifier|...
name|states
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|tableStates
init|)
block|{
name|TableDescriptor
name|descriptor
init|=
name|readDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|descriptor
operator|==
literal|null
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
if|if
condition|(
operator|!
name|TableState
operator|.
name|isInStates
argument_list|(
name|descriptor
operator|.
name|getTableState
argument_list|()
argument_list|,
name|states
argument_list|)
condition|)
block|{
name|writeDescriptor
argument_list|(
operator|new
name|TableDescriptor
argument_list|(
name|descriptor
operator|.
name|getHTableDescriptor
argument_list|()
argument_list|,
name|newState
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
specifier|public
name|boolean
name|isTableState
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|TableState
operator|.
name|State
modifier|...
name|states
parameter_list|)
block|{
name|TableState
operator|.
name|State
name|tableState
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tableState
operator|=
name|getTableState
argument_list|(
name|tableName
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
literal|"Unable to get table state, probably table not exists"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|tableState
operator|!=
literal|null
operator|&&
name|TableState
operator|.
name|isInStates
argument_list|(
name|tableState
argument_list|,
name|states
argument_list|)
return|;
block|}
specifier|public
name|void
name|setDeletedTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableState
operator|.
name|State
name|remove
init|=
name|tableStates
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|remove
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Moving table "
operator|+
name|tableName
operator|+
literal|" state to deleted but was "
operator|+
literal|"already deleted"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isTablePresent
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getTableState
argument_list|(
name|tableName
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Return all tables in given states.    *    * @param states filter by states    * @return tables in given states    * @throws IOException    */
specifier|public
name|Set
argument_list|<
name|TableName
argument_list|>
name|getTablesInStates
parameter_list|(
name|TableState
operator|.
name|State
modifier|...
name|states
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|TableName
argument_list|>
name|rv
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|TableName
argument_list|,
name|TableState
operator|.
name|State
argument_list|>
name|entry
range|:
name|tableStates
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|TableState
operator|.
name|isInStates
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|states
argument_list|)
condition|)
name|rv
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|rv
return|;
block|}
specifier|public
name|TableState
operator|.
name|State
name|getTableState
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableState
operator|.
name|State
name|tableState
init|=
name|tableStates
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableState
operator|==
literal|null
condition|)
block|{
name|TableDescriptor
name|descriptor
init|=
name|readDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|descriptor
operator|!=
literal|null
condition|)
name|tableState
operator|=
name|descriptor
operator|.
name|getTableState
argument_list|()
expr_stmt|;
block|}
return|return
name|tableState
return|;
block|}
comment|/**    * Write descriptor in place, update cache of states.    * Write lock should be hold by caller.    *    * @param descriptor what to write    */
specifier|private
name|void
name|writeDescriptor
parameter_list|(
name|TableDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|descriptor
operator|.
name|getHTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|TableState
operator|.
name|State
name|state
init|=
name|descriptor
operator|.
name|getTableState
argument_list|()
decl_stmt|;
name|descriptors
operator|.
name|add
argument_list|(
name|descriptor
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" written descriptor for state "
operator|+
name|state
argument_list|)
expr_stmt|;
name|tableStates
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" updated state to "
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
comment|/**    * Read current descriptor for table, update cache of states.    *    * @param table descriptor to read    * @return descriptor    * @throws IOException    */
specifier|private
name|TableDescriptor
name|readDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptor
name|descriptor
init|=
name|descriptors
operator|.
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|descriptor
operator|==
literal|null
condition|)
name|tableStates
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
else|else
name|tableStates
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|descriptor
operator|.
name|getTableState
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|descriptor
return|;
block|}
block|}
end_class

end_unit

