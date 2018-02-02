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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReadWriteLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|exceptions
operator|.
name|IllegalArgumentIOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|NonNull
import|;
end_import

begin_import
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Result
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
comment|/**  * This is a helper class used to manage table states.  * States persisted in tableinfo and cached internally.  * TODO: Cache state. Cut down on meta looksups.  */
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TableStateManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|master
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
name|master
operator|=
name|master
expr_stmt|;
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
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|updateMetaState
argument_list|(
name|tableName
argument_list|,
name|newState
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Set table state to provided but only if table in specified states    * Caller should lock table on write.    * @param tableName table to change state for    * @param newState new state    * @param states states to check against    * @return null if succeed or table state if failed    * @throws IOException    */
specifier|public
name|TableState
operator|.
name|State
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
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|TableState
name|currentState
init|=
name|readMetaState
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentState
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
name|currentState
operator|.
name|inStates
argument_list|(
name|states
argument_list|)
condition|)
block|{
name|updateMetaState
argument_list|(
name|tableName
argument_list|,
name|newState
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|currentState
operator|.
name|getState
argument_list|()
return|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
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
name|TableState
name|currentState
init|=
name|readMetaState
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentState
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
name|currentState
operator|.
name|inStates
argument_list|(
name|states
argument_list|)
condition|)
block|{
name|updateMetaState
argument_list|(
name|tableName
argument_list|,
name|newState
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
try|try
block|{
name|TableState
operator|.
name|State
name|tableState
init|=
name|getTableState
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
return|return
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
literal|"Unable to get table "
operator|+
name|tableName
operator|+
literal|" state"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
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
return|return;
name|MetaTableAccessor
operator|.
name|deleteTableState
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
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
name|readMetaState
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
specifier|final
name|TableState
operator|.
name|State
modifier|...
name|states
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
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
name|MetaTableAccessor
operator|.
name|fullScanTables
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
operator|new
name|MetaTableAccessor
operator|.
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|TableState
name|tableState
init|=
name|MetaTableAccessor
operator|.
name|getTableState
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableState
operator|!=
literal|null
operator|&&
name|tableState
operator|.
name|inStates
argument_list|(
name|states
argument_list|)
condition|)
name|rv
operator|.
name|add
argument_list|(
name|tableState
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|rv
return|;
block|}
annotation|@
name|NonNull
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
name|currentState
init|=
name|readMetaState
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentState
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
return|return
name|currentState
operator|.
name|getState
argument_list|()
return|;
block|}
specifier|protected
name|void
name|updateMetaState
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
if|if
condition|(
name|TableState
operator|.
name|State
operator|.
name|DISABLING
operator|.
name|equals
argument_list|(
name|newState
argument_list|)
operator|||
name|TableState
operator|.
name|State
operator|.
name|DISABLED
operator|.
name|equals
argument_list|(
name|newState
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentIOException
argument_list|(
literal|"Cannot disable the meta table; "
operator|+
name|newState
argument_list|)
throw|;
block|}
comment|// Otherwise, just return; no need to set ENABLED on meta -- it is always ENABLED.
return|return;
block|}
name|MetaTableAccessor
operator|.
name|updateTableState
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|newState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Nullable
specifier|protected
name|TableState
name|readMetaState
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
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
return|return
operator|new
name|TableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
return|;
block|}
return|return
name|MetaTableAccessor
operator|.
name|getTableState
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|TableDescriptors
name|tableDescriptors
init|=
name|master
operator|.
name|getTableDescriptors
argument_list|()
decl_stmt|;
name|Connection
name|connection
init|=
name|master
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|fixTableStates
argument_list|(
name|tableDescriptors
argument_list|,
name|connection
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|fixTableStates
parameter_list|(
name|TableDescriptors
name|tableDescriptors
parameter_list|,
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|allDescriptors
init|=
name|tableDescriptors
operator|.
name|getAllDescriptors
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|TableState
argument_list|>
name|states
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|MetaTableAccessor
operator|.
name|fullScanTables
argument_list|(
name|connection
argument_list|,
operator|new
name|MetaTableAccessor
operator|.
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|TableState
name|state
init|=
name|MetaTableAccessor
operator|.
name|getTableState
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
name|states
operator|.
name|put
argument_list|(
name|state
operator|.
name|getTableName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|state
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TableDescriptor
argument_list|>
name|entry
range|:
name|allDescriptors
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|table
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|states
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|table
operator|+
literal|" has no state, assuming ENABLED"
argument_list|)
expr_stmt|;
name|MetaTableAccessor
operator|.
name|updateTableState
argument_list|(
name|connection
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

