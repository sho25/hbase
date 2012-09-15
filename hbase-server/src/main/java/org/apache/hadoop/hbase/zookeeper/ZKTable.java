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
name|zookeeper
package|;
end_package

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
name|HashSet
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
name|DeserializationException
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Helper class for table state tracking for use by {@link AssignmentManager}.  * Reads, caches and sets state up in zookeeper.  If multiple read/write  * clients, will make for confusion.  Read-only clients other than  * AssignmentManager interested in learning table state can use the  * read-only utility methods in {@link ZKTableReadOnly}.  *  *<p>To save on trips to the zookeeper ensemble, internally we cache table  * state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKTable
block|{
comment|// A znode will exist under the table directory if it is in any of the
comment|// following states: {@link TableState#ENABLING} , {@link TableState#DISABLING},
comment|// or {@link TableState#DISABLED}.  If {@link TableState#ENABLED}, there will
comment|// be no entry for a table in zk.  Thats how it currently works.
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
name|ZKTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ZooKeeperWatcher
name|watcher
decl_stmt|;
comment|/**    * Cache of what we found in zookeeper so we don't have to go to zk ensemble    * for every query.  Synchronize access rather than use concurrent Map because    * synchronization needs to span query of zk.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
argument_list|>
name|cache
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
argument_list|>
argument_list|()
decl_stmt|;
comment|// TODO: Make it so always a table znode. Put table schema here as well as table state.
comment|// Have watcher on table znode so all are notified of state or schema change.
specifier|public
name|ZKTable
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zkw
parameter_list|)
throws|throws
name|KeeperException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|watcher
operator|=
name|zkw
expr_stmt|;
name|populateTableStates
argument_list|()
expr_stmt|;
block|}
comment|/**    * Gets a list of all the tables set as disabled in zookeeper.    * @throws KeeperException    */
specifier|private
name|void
name|populateTableStates
parameter_list|()
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|this
operator|.
name|watcher
operator|.
name|tableZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
name|state
init|=
name|ZKTableReadOnly
operator|.
name|getTableState
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|child
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
name|this
operator|.
name|cache
operator|.
name|put
argument_list|(
name|child
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Sets the specified table as DISABLED in zookeeper.  Fails silently if the    * table is already disabled in zookeeper.  Sets no watches.    * @param tableName    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|void
name|setDisabledTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
operator|!
name|isDisablingOrDisabledTable
argument_list|(
name|tableName
argument_list|)
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
literal|" state to disabled but was "
operator|+
literal|"not first in disabling state: "
operator|+
name|this
operator|.
name|cache
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets the specified table as DISABLING in zookeeper.  Fails silently if the    * table is already disabled in zookeeper.  Sets no watches.    * @param tableName    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|void
name|setDisablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
operator|!
name|isEnabledOrDisablingTable
argument_list|(
name|tableName
argument_list|)
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
literal|" state to disabling but was "
operator|+
literal|"not first in enabled state: "
operator|+
name|this
operator|.
name|cache
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets the specified table as ENABLING in zookeeper.  Fails silently if the    * table is already disabled in zookeeper.  Sets no watches.    * @param tableName    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|void
name|setEnablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
operator|!
name|isDisabledOrEnablingTable
argument_list|(
name|tableName
argument_list|)
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
literal|" state to enabling but was "
operator|+
literal|"not first in disabled state: "
operator|+
name|this
operator|.
name|cache
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets the specified table as ENABLING in zookeeper atomically    * If the table is already in ENABLING state, no operation is performed    * @param tableName    * @return if the operation succeeds or not    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|boolean
name|checkAndSetEnablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
name|isEnablingTable
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Sets the specified table as ENABLING in zookeeper atomically    * If the table isn't in DISABLED state, no operation is performed    * @param tableName    * @return if the operation succeeds or not    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|boolean
name|checkDisabledAndSetEnablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
operator|!
name|isDisabledTable
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Sets the specified table as DISABLING in zookeeper atomically    * If the table isn't in ENABLED state, no operation is performed    * @param tableName    * @return if the operation succeeds or not    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|boolean
name|checkEnabledAndSetDisablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
name|this
operator|.
name|cache
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
operator|!=
literal|null
operator|&&
operator|!
name|isEnabledTable
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|private
name|void
name|setTableState
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
name|state
parameter_list|)
throws|throws
name|KeeperException
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|watcher
operator|.
name|tableZNode
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|znode
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setState
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|byte
index|[]
name|data
init|=
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|setData
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|znode
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|this
operator|.
name|cache
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isDisabledTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
name|isTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isDisablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
name|isTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|DISABLING
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isEnablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
name|isTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isEnabledTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
return|return
name|isTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isDisablingOrDisabledTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
return|return
name|isDisablingTable
argument_list|(
name|tableName
argument_list|)
operator|||
name|isDisabledTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
specifier|public
name|boolean
name|isEnabledOrDisablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
return|return
name|isEnabledTable
argument_list|(
name|tableName
argument_list|)
operator|||
name|isDisablingTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
specifier|public
name|boolean
name|isDisabledOrEnablingTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
return|return
name|isDisabledTable
argument_list|(
name|tableName
argument_list|)
operator|||
name|isEnablingTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
specifier|private
name|boolean
name|isTableState
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
name|state
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
name|currentState
init|=
name|this
operator|.
name|cache
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
return|return
name|ZKTableReadOnly
operator|.
name|isTableState
argument_list|(
name|currentState
argument_list|,
name|state
argument_list|)
return|;
block|}
block|}
comment|/**    * Deletes the table in zookeeper.  Fails silently if the    * table is not currently disabled in zookeeper.  Sets no watches.    * @param tableName    * @throws KeeperException unexpected zookeeper exception    */
specifier|public
name|void
name|setDeletedTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
if|if
condition|(
name|this
operator|.
name|cache
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
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
name|ZKUtil
operator|.
name|deleteNodeFailSilent
argument_list|(
name|this
operator|.
name|watcher
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|this
operator|.
name|watcher
operator|.
name|tableZNode
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets the ENABLED state in the cache and creates or force updates a node to    * ENABLED state for the specified table    *     * @param tableName    * @throws KeeperException    */
specifier|public
name|void
name|setEnabledTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|KeeperException
block|{
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
expr_stmt|;
block|}
comment|/**    * check if table is present .    *     * @param tableName    * @return true if the table is present    */
specifier|public
name|boolean
name|isTablePresent
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
name|ZooKeeperProtos
operator|.
name|Table
operator|.
name|State
name|state
init|=
name|this
operator|.
name|cache
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
return|return
operator|!
operator|(
name|state
operator|==
literal|null
operator|)
return|;
block|}
block|}
comment|/**    * Gets a list of all the tables set as disabled in zookeeper.    * @return Set of disabled tables, empty Set if none    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getDisabledTables
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|disabledTables
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|cache
init|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|tables
init|=
name|this
operator|.
name|cache
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|table
range|:
name|tables
control|)
block|{
if|if
condition|(
name|isDisabledTable
argument_list|(
name|table
argument_list|)
condition|)
name|disabledTables
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|disabledTables
return|;
block|}
block|}
end_class

end_unit

