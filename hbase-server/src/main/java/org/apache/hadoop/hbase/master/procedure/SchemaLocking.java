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
name|HashMap
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
name|function
operator|.
name|Function
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
name|master
operator|.
name|locking
operator|.
name|LockProcedure
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
name|procedure2
operator|.
name|LockAndQueue
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
name|procedure2
operator|.
name|LockType
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
name|procedure2
operator|.
name|LockedResource
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
name|procedure2
operator|.
name|LockedResourceType
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
name|procedure2
operator|.
name|Procedure
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
name|ImmutableMap
import|;
end_import

begin_comment
comment|/**  *<p>  * Locks on namespaces, tables, and regions.  *</p>  *<p>  * Since LockAndQueue implementation is NOT thread-safe, schedLock() guards all calls to these  * locks.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SchemaLocking
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|LockAndQueue
argument_list|>
name|serverLocks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LockAndQueue
argument_list|>
name|namespaceLocks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|LockAndQueue
argument_list|>
name|tableLocks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Single map for all regions irrespective of tables. Key is encoded region name.
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LockAndQueue
argument_list|>
name|regionLocks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LockAndQueue
argument_list|>
name|peerLocks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LockAndQueue
name|metaLock
init|=
operator|new
name|LockAndQueue
argument_list|()
decl_stmt|;
specifier|private
parameter_list|<
name|T
parameter_list|>
name|LockAndQueue
name|getLock
parameter_list|(
name|Map
argument_list|<
name|T
argument_list|,
name|LockAndQueue
argument_list|>
name|map
parameter_list|,
name|T
name|key
parameter_list|)
block|{
name|LockAndQueue
name|lock
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|lock
operator|==
literal|null
condition|)
block|{
name|lock
operator|=
operator|new
name|LockAndQueue
argument_list|()
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|lock
argument_list|)
expr_stmt|;
block|}
return|return
name|lock
return|;
block|}
name|LockAndQueue
name|getTableLock
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|getLock
argument_list|(
name|tableLocks
argument_list|,
name|tableName
argument_list|)
return|;
block|}
name|LockAndQueue
name|removeTableLock
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|tableLocks
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
return|;
block|}
name|LockAndQueue
name|getNamespaceLock
parameter_list|(
name|String
name|namespace
parameter_list|)
block|{
return|return
name|getLock
argument_list|(
name|namespaceLocks
argument_list|,
name|namespace
argument_list|)
return|;
block|}
name|LockAndQueue
name|getRegionLock
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
block|{
return|return
name|getLock
argument_list|(
name|regionLocks
argument_list|,
name|encodedRegionName
argument_list|)
return|;
block|}
name|LockAndQueue
name|getMetaLock
parameter_list|()
block|{
return|return
name|metaLock
return|;
block|}
name|LockAndQueue
name|removeRegionLock
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
block|{
return|return
name|regionLocks
operator|.
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
return|;
block|}
name|LockAndQueue
name|getServerLock
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|getLock
argument_list|(
name|serverLocks
argument_list|,
name|serverName
argument_list|)
return|;
block|}
name|LockAndQueue
name|getPeerLock
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|getLock
argument_list|(
name|peerLocks
argument_list|,
name|peerId
argument_list|)
return|;
block|}
name|LockAndQueue
name|removePeerLock
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
return|return
name|peerLocks
operator|.
name|remove
argument_list|(
name|peerId
argument_list|)
return|;
block|}
specifier|private
name|LockedResource
name|createLockedResource
parameter_list|(
name|LockedResourceType
name|resourceType
parameter_list|,
name|String
name|resourceName
parameter_list|,
name|LockAndQueue
name|queue
parameter_list|)
block|{
name|LockType
name|lockType
decl_stmt|;
name|Procedure
argument_list|<
name|?
argument_list|>
name|exclusiveLockOwnerProcedure
decl_stmt|;
name|int
name|sharedLockCount
decl_stmt|;
if|if
condition|(
name|queue
operator|.
name|hasExclusiveLock
argument_list|()
condition|)
block|{
name|lockType
operator|=
name|LockType
operator|.
name|EXCLUSIVE
expr_stmt|;
name|exclusiveLockOwnerProcedure
operator|=
name|queue
operator|.
name|getExclusiveLockOwnerProcedure
argument_list|()
expr_stmt|;
name|sharedLockCount
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|lockType
operator|=
name|LockType
operator|.
name|SHARED
expr_stmt|;
name|exclusiveLockOwnerProcedure
operator|=
literal|null
expr_stmt|;
name|sharedLockCount
operator|=
name|queue
operator|.
name|getSharedLockCount
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Procedure
argument_list|<
name|?
argument_list|>
argument_list|>
name|waitingProcedures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|procedure
range|:
name|queue
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|procedure
operator|instanceof
name|LockProcedure
operator|)
condition|)
block|{
continue|continue;
block|}
name|waitingProcedures
operator|.
name|add
argument_list|(
name|procedure
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|LockedResource
argument_list|(
name|resourceType
argument_list|,
name|resourceName
argument_list|,
name|lockType
argument_list|,
name|exclusiveLockOwnerProcedure
argument_list|,
name|sharedLockCount
argument_list|,
name|waitingProcedures
argument_list|)
return|;
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|addToLockedResources
parameter_list|(
name|List
argument_list|<
name|LockedResource
argument_list|>
name|lockedResources
parameter_list|,
name|Map
argument_list|<
name|T
argument_list|,
name|LockAndQueue
argument_list|>
name|locks
parameter_list|,
name|Function
argument_list|<
name|T
argument_list|,
name|String
argument_list|>
name|keyTransformer
parameter_list|,
name|LockedResourceType
name|resourcesType
parameter_list|)
block|{
name|locks
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|isLocked
argument_list|()
argument_list|)
operator|.
name|map
argument_list|(
name|e
lambda|->
name|createLockedResource
argument_list|(
name|resourcesType
argument_list|,
name|keyTransformer
operator|.
name|apply
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|lockedResources
operator|::
name|add
argument_list|)
expr_stmt|;
block|}
comment|/**    * List lock queues.    * @return the locks    */
name|List
argument_list|<
name|LockedResource
argument_list|>
name|getLocks
parameter_list|()
block|{
name|List
argument_list|<
name|LockedResource
argument_list|>
name|lockedResources
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|addToLockedResources
argument_list|(
name|lockedResources
argument_list|,
name|serverLocks
argument_list|,
name|sn
lambda|->
name|sn
operator|.
name|getServerName
argument_list|()
argument_list|,
name|LockedResourceType
operator|.
name|SERVER
argument_list|)
expr_stmt|;
name|addToLockedResources
argument_list|(
name|lockedResources
argument_list|,
name|namespaceLocks
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|LockedResourceType
operator|.
name|NAMESPACE
argument_list|)
expr_stmt|;
name|addToLockedResources
argument_list|(
name|lockedResources
argument_list|,
name|tableLocks
argument_list|,
name|tn
lambda|->
name|tn
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|LockedResourceType
operator|.
name|TABLE
argument_list|)
expr_stmt|;
name|addToLockedResources
argument_list|(
name|lockedResources
argument_list|,
name|regionLocks
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|LockedResourceType
operator|.
name|REGION
argument_list|)
expr_stmt|;
name|addToLockedResources
argument_list|(
name|lockedResources
argument_list|,
name|peerLocks
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|LockedResourceType
operator|.
name|PEER
argument_list|)
expr_stmt|;
name|addToLockedResources
argument_list|(
name|lockedResources
argument_list|,
name|ImmutableMap
operator|.
name|of
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|metaLock
argument_list|)
argument_list|,
name|tn
lambda|->
name|tn
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|LockedResourceType
operator|.
name|META
argument_list|)
expr_stmt|;
return|return
name|lockedResources
return|;
block|}
comment|/**    * @return {@link LockedResource} for resource of specified type& name. null if resource is not    *         locked.    */
name|LockedResource
name|getLockResource
parameter_list|(
name|LockedResourceType
name|resourceType
parameter_list|,
name|String
name|resourceName
parameter_list|)
block|{
name|LockAndQueue
name|queue
decl_stmt|;
switch|switch
condition|(
name|resourceType
condition|)
block|{
case|case
name|SERVER
case|:
name|queue
operator|=
name|serverLocks
operator|.
name|get
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|resourceName
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|NAMESPACE
case|:
name|queue
operator|=
name|namespaceLocks
operator|.
name|get
argument_list|(
name|resourceName
argument_list|)
expr_stmt|;
break|break;
case|case
name|TABLE
case|:
name|queue
operator|=
name|tableLocks
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|resourceName
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|REGION
case|:
name|queue
operator|=
name|regionLocks
operator|.
name|get
argument_list|(
name|resourceName
argument_list|)
expr_stmt|;
break|break;
case|case
name|PEER
case|:
name|queue
operator|=
name|peerLocks
operator|.
name|get
argument_list|(
name|resourceName
argument_list|)
expr_stmt|;
break|break;
case|case
name|META
case|:
name|queue
operator|=
name|metaLock
expr_stmt|;
default|default:
name|queue
operator|=
literal|null
expr_stmt|;
break|break;
block|}
return|return
name|queue
operator|!=
literal|null
condition|?
name|createLockedResource
argument_list|(
name|resourceType
argument_list|,
name|resourceName
argument_list|,
name|queue
argument_list|)
else|:
literal|null
return|;
block|}
comment|/**    * Removes all locks by clearing the maps. Used when procedure executor is stopped for failure and    * recovery testing.    */
name|void
name|clear
parameter_list|()
block|{
name|serverLocks
operator|.
name|clear
argument_list|()
expr_stmt|;
name|namespaceLocks
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tableLocks
operator|.
name|clear
argument_list|()
expr_stmt|;
name|regionLocks
operator|.
name|clear
argument_list|()
expr_stmt|;
name|peerLocks
operator|.
name|clear
argument_list|()
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
literal|"serverLocks="
operator|+
name|filterUnlocked
argument_list|(
name|this
operator|.
name|serverLocks
argument_list|)
operator|+
literal|", namespaceLocks="
operator|+
name|filterUnlocked
argument_list|(
name|this
operator|.
name|namespaceLocks
argument_list|)
operator|+
literal|", tableLocks="
operator|+
name|filterUnlocked
argument_list|(
name|this
operator|.
name|tableLocks
argument_list|)
operator|+
literal|", regionLocks="
operator|+
name|filterUnlocked
argument_list|(
name|this
operator|.
name|regionLocks
argument_list|)
operator|+
literal|", peerLocks="
operator|+
name|filterUnlocked
argument_list|(
name|this
operator|.
name|peerLocks
argument_list|)
operator|+
literal|", metaLocks="
operator|+
name|filterUnlocked
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|metaLock
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|String
name|filterUnlocked
parameter_list|(
name|Map
argument_list|<
name|?
argument_list|,
name|LockAndQueue
argument_list|>
name|locks
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"{"
argument_list|)
decl_stmt|;
name|int
name|initialLength
init|=
name|sb
operator|.
name|length
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|LockAndQueue
argument_list|>
name|entry
range|:
name|locks
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isLocked
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
name|initialLength
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

