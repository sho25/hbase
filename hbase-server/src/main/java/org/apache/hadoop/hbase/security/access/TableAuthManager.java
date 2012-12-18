begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|access
package|;
end_package

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
name|ArrayListMultimap
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
name|ListMultimap
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
name|KeyValue
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
name|ZooKeeperWatcher
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
name|java
operator|.
name|io
operator|.
name|*
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
name|concurrent
operator|.
name|ConcurrentSkipListMap
import|;
end_import

begin_comment
comment|/**  * Performs authorization checks for a given user's assigned permissions  */
end_comment

begin_class
specifier|public
class|class
name|TableAuthManager
block|{
specifier|private
specifier|static
class|class
name|PermissionCache
parameter_list|<
name|T
extends|extends
name|Permission
parameter_list|>
block|{
comment|/** Cache of user permissions */
specifier|private
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|userCache
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
comment|/** Cache of group permissions */
specifier|private
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|groupCache
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|public
name|List
argument_list|<
name|T
argument_list|>
name|getUser
parameter_list|(
name|String
name|user
parameter_list|)
block|{
return|return
name|userCache
operator|.
name|get
argument_list|(
name|user
argument_list|)
return|;
block|}
specifier|public
name|void
name|putUser
parameter_list|(
name|String
name|user
parameter_list|,
name|T
name|perm
parameter_list|)
block|{
name|userCache
operator|.
name|put
argument_list|(
name|user
argument_list|,
name|perm
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|T
argument_list|>
name|replaceUser
parameter_list|(
name|String
name|user
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|perms
parameter_list|)
block|{
return|return
name|userCache
operator|.
name|replaceValues
argument_list|(
name|user
argument_list|,
name|perms
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|T
argument_list|>
name|getGroup
parameter_list|(
name|String
name|group
parameter_list|)
block|{
return|return
name|groupCache
operator|.
name|get
argument_list|(
name|group
argument_list|)
return|;
block|}
specifier|public
name|void
name|putGroup
parameter_list|(
name|String
name|group
parameter_list|,
name|T
name|perm
parameter_list|)
block|{
name|groupCache
operator|.
name|put
argument_list|(
name|group
argument_list|,
name|perm
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|T
argument_list|>
name|replaceGroup
parameter_list|(
name|String
name|group
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|perms
parameter_list|)
block|{
return|return
name|groupCache
operator|.
name|replaceValues
argument_list|(
name|group
argument_list|,
name|perms
argument_list|)
return|;
block|}
comment|/**      * Returns a combined map of user and group permissions, with group names prefixed by      * {@link AccessControlLists#GROUP_PREFIX}.      */
specifier|public
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|getAllPermissions
parameter_list|()
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|tmp
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|tmp
operator|.
name|putAll
argument_list|(
name|userCache
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|group
range|:
name|groupCache
operator|.
name|keySet
argument_list|()
control|)
block|{
name|tmp
operator|.
name|putAll
argument_list|(
name|AccessControlLists
operator|.
name|GROUP_PREFIX
operator|+
name|group
argument_list|,
name|groupCache
operator|.
name|get
argument_list|(
name|group
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|tmp
return|;
block|}
block|}
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableAuthManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|TableAuthManager
name|instance
decl_stmt|;
comment|/** Cache of global permissions */
specifier|private
specifier|volatile
name|PermissionCache
argument_list|<
name|Permission
argument_list|>
name|globalCache
decl_stmt|;
specifier|private
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
argument_list|>
name|tableCache
init|=
operator|new
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ZKPermissionWatcher
name|zkperms
decl_stmt|;
specifier|private
name|TableAuthManager
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|zkperms
operator|=
operator|new
name|ZKPermissionWatcher
argument_list|(
name|watcher
argument_list|,
name|this
argument_list|,
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|zkperms
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ZooKeeper initialization failed"
argument_list|,
name|ke
argument_list|)
expr_stmt|;
block|}
comment|// initialize global permissions based on configuration
name|globalCache
operator|=
name|initGlobal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns a new {@code PermissionCache} initialized with permission assignments    * from the {@code hbase.superuser} configuration key.    */
specifier|private
name|PermissionCache
argument_list|<
name|Permission
argument_list|>
name|initGlobal
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|User
name|user
init|=
name|User
operator|.
name|getCurrent
argument_list|()
decl_stmt|;
if|if
condition|(
name|user
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to obtain the current user, "
operator|+
literal|"authorization checks for internal operations will not work correctly!"
argument_list|)
throw|;
block|}
name|PermissionCache
argument_list|<
name|Permission
argument_list|>
name|newCache
init|=
operator|new
name|PermissionCache
argument_list|<
name|Permission
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|currentUser
init|=
name|user
operator|.
name|getShortName
argument_list|()
decl_stmt|;
comment|// the system user is always included
name|List
argument_list|<
name|String
argument_list|>
name|superusers
init|=
name|Lists
operator|.
name|asList
argument_list|(
name|currentUser
argument_list|,
name|conf
operator|.
name|getStrings
argument_list|(
name|AccessControlLists
operator|.
name|SUPERUSER_CONF_KEY
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|superusers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|name
range|:
name|superusers
control|)
block|{
if|if
condition|(
name|AccessControlLists
operator|.
name|isGroupPrincipal
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|newCache
operator|.
name|putGroup
argument_list|(
name|AccessControlLists
operator|.
name|getGroupName
argument_list|(
name|name
argument_list|)
argument_list|,
operator|new
name|Permission
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newCache
operator|.
name|putUser
argument_list|(
name|name
argument_list|,
operator|new
name|Permission
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|newCache
return|;
block|}
specifier|public
name|ZKPermissionWatcher
name|getZKPermissionWatcher
parameter_list|()
block|{
return|return
name|this
operator|.
name|zkperms
return|;
block|}
specifier|public
name|void
name|refreshCacheFromWritable
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|data
operator|!=
literal|null
operator|&&
name|data
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|perms
decl_stmt|;
try|try
block|{
name|perms
operator|=
name|AccessControlLists
operator|.
name|readPermissions
argument_list|(
name|data
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|perms
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|table
argument_list|,
name|AccessControlLists
operator|.
name|ACL_GLOBAL_NAME
argument_list|)
condition|)
block|{
name|updateGlobalCache
argument_list|(
name|perms
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updateTableCache
argument_list|(
name|table
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping permission cache refresh because writable data is empty"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Updates the internal global permissions cache    *    * @param userPerms    */
specifier|private
name|void
name|updateGlobalCache
parameter_list|(
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|userPerms
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|Permission
argument_list|>
name|newCache
init|=
literal|null
decl_stmt|;
try|try
block|{
name|newCache
operator|=
name|initGlobal
argument_list|(
name|conf
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
name|TablePermission
argument_list|>
name|entry
range|:
name|userPerms
operator|.
name|entries
argument_list|()
control|)
block|{
if|if
condition|(
name|AccessControlLists
operator|.
name|isGroupPrincipal
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|newCache
operator|.
name|putGroup
argument_list|(
name|AccessControlLists
operator|.
name|getGroupName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Permission
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getActions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newCache
operator|.
name|putUser
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|Permission
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getActions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|globalCache
operator|=
name|newCache
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Never happens
name|LOG
operator|.
name|error
argument_list|(
literal|"Error occured while updating the global cache"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Updates the internal permissions cache for a single table, splitting    * the permissions listed into separate caches for users and groups to optimize    * group lookups.    *     * @param table    * @param tablePerms    */
specifier|private
name|void
name|updateTableCache
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|tablePerms
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|newTablePerms
init|=
operator|new
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|entry
range|:
name|tablePerms
operator|.
name|entries
argument_list|()
control|)
block|{
if|if
condition|(
name|AccessControlLists
operator|.
name|isGroupPrincipal
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|newTablePerms
operator|.
name|putGroup
argument_list|(
name|AccessControlLists
operator|.
name|getGroupName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newTablePerms
operator|.
name|putUser
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|tableCache
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|newTablePerms
argument_list|)
expr_stmt|;
block|}
specifier|private
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|getTablePermissions
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
block|{
if|if
condition|(
operator|!
name|tableCache
operator|.
name|containsKey
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|tableCache
operator|.
name|putIfAbsent
argument_list|(
name|table
argument_list|,
operator|new
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tableCache
operator|.
name|get
argument_list|(
name|table
argument_list|)
return|;
block|}
comment|/**    * Authorizes a global permission    * @param perms    * @param action    * @return    */
specifier|private
name|boolean
name|authorize
parameter_list|(
name|List
argument_list|<
name|Permission
argument_list|>
name|perms
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|perms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Permission
name|p
range|:
name|perms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|implies
argument_list|(
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
elseif|else
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
literal|"No permissions found"
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Authorize a global permission based on ACLs for the given user and the    * user's groups.    * @param user    * @param action    * @return true if known and authorized, false otherwise    */
specifier|public
name|boolean
name|authorize
parameter_list|(
name|User
name|user
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|user
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|authorize
argument_list|(
name|globalCache
operator|.
name|getUser
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
index|[]
name|groups
init|=
name|user
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|groups
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|groups
control|)
block|{
if|if
condition|(
name|authorize
argument_list|(
name|globalCache
operator|.
name|getGroup
argument_list|(
name|group
argument_list|)
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|authorize
parameter_list|(
name|List
argument_list|<
name|TablePermission
argument_list|>
name|perms
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|authorize
argument_list|(
name|perms
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|action
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|authorize
parameter_list|(
name|List
argument_list|<
name|TablePermission
argument_list|>
name|perms
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|perms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TablePermission
name|p
range|:
name|perms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|implies
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
elseif|else
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
literal|"No permissions found for table="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|authorize
parameter_list|(
name|User
name|user
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|KeyValue
name|kv
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tablePerms
init|=
name|tableCache
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|tablePerms
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|TablePermission
argument_list|>
name|userPerms
init|=
name|tablePerms
operator|.
name|getUser
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorize
argument_list|(
name|userPerms
argument_list|,
name|table
argument_list|,
name|kv
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
index|[]
name|groupNames
init|=
name|user
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|groupNames
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|groupNames
control|)
block|{
name|List
argument_list|<
name|TablePermission
argument_list|>
name|groupPerms
init|=
name|tablePerms
operator|.
name|getGroup
argument_list|(
name|group
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorize
argument_list|(
name|groupPerms
argument_list|,
name|table
argument_list|,
name|kv
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|authorize
parameter_list|(
name|List
argument_list|<
name|TablePermission
argument_list|>
name|perms
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|KeyValue
name|kv
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|perms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TablePermission
name|p
range|:
name|perms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|implies
argument_list|(
name|table
argument_list|,
name|kv
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
elseif|else
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
literal|"No permissions for authorize() check, table="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Checks global authorization for a specific action for a user, based on the    * stored user permissions.    */
specifier|public
name|boolean
name|authorizeUser
parameter_list|(
name|String
name|username
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|authorize
argument_list|(
name|globalCache
operator|.
name|getUser
argument_list|(
name|username
argument_list|)
argument_list|,
name|action
argument_list|)
return|;
block|}
comment|/**    * Checks authorization to a given table and column family for a user, based on the    * stored user permissions.    *    * @param username    * @param table    * @param family    * @param action    * @return true if known and authorized, false otherwise    */
specifier|public
name|boolean
name|authorizeUser
parameter_list|(
name|String
name|username
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|authorizeUser
argument_list|(
name|username
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|action
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|authorizeUser
parameter_list|(
name|String
name|username
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
comment|// global authorization supercedes table level
if|if
condition|(
name|authorizeUser
argument_list|(
name|username
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|authorize
argument_list|(
name|getTablePermissions
argument_list|(
name|table
argument_list|)
operator|.
name|getUser
argument_list|(
name|username
argument_list|)
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|action
argument_list|)
return|;
block|}
comment|/**    * Checks authorization for a given action for a group, based on the stored    * permissions.    */
specifier|public
name|boolean
name|authorizeGroup
parameter_list|(
name|String
name|groupName
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|authorize
argument_list|(
name|globalCache
operator|.
name|getGroup
argument_list|(
name|groupName
argument_list|)
argument_list|,
name|action
argument_list|)
return|;
block|}
comment|/**    * Checks authorization to a given table and column family for a group, based    * on the stored permissions.     * @param groupName    * @param table    * @param family    * @param action    * @return true if known and authorized, false otherwise    */
specifier|public
name|boolean
name|authorizeGroup
parameter_list|(
name|String
name|groupName
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
comment|// global authorization supercedes table level
if|if
condition|(
name|authorizeGroup
argument_list|(
name|groupName
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|authorize
argument_list|(
name|getTablePermissions
argument_list|(
name|table
argument_list|)
operator|.
name|getGroup
argument_list|(
name|groupName
argument_list|)
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|action
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|authorize
parameter_list|(
name|User
name|user
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|authorizeUser
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
index|[]
name|groups
init|=
name|user
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|groups
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|groups
control|)
block|{
if|if
condition|(
name|authorizeGroup
argument_list|(
name|group
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|authorize
parameter_list|(
name|User
name|user
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|authorize
argument_list|(
name|user
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
literal|null
argument_list|,
name|action
argument_list|)
return|;
block|}
comment|/**    * Returns true if the given user has a {@link TablePermission} matching up    * to the column family portion of a permission.  Note that this permission    * may be scoped to a given column qualifier and does not guarantee that    * authorize() on the same column family would return true.    */
specifier|public
name|boolean
name|matchPermission
parameter_list|(
name|User
name|user
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tablePerms
init|=
name|tableCache
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|tablePerms
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|TablePermission
argument_list|>
name|userPerms
init|=
name|tablePerms
operator|.
name|getUser
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|userPerms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TablePermission
name|p
range|:
name|userPerms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|matchesFamily
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
name|String
index|[]
name|groups
init|=
name|user
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|groups
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|groups
control|)
block|{
name|List
argument_list|<
name|TablePermission
argument_list|>
name|groupPerms
init|=
name|tablePerms
operator|.
name|getGroup
argument_list|(
name|group
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupPerms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TablePermission
name|p
range|:
name|groupPerms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|matchesFamily
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|matchPermission
parameter_list|(
name|User
name|user
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tablePerms
init|=
name|tableCache
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|tablePerms
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|TablePermission
argument_list|>
name|userPerms
init|=
name|tablePerms
operator|.
name|getUser
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|userPerms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TablePermission
name|p
range|:
name|userPerms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|matchesFamilyQualifier
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
name|String
index|[]
name|groups
init|=
name|user
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|groups
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|groups
control|)
block|{
name|List
argument_list|<
name|TablePermission
argument_list|>
name|groupPerms
init|=
name|tablePerms
operator|.
name|getGroup
argument_list|(
name|group
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupPerms
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TablePermission
name|p
range|:
name|groupPerms
control|)
block|{
if|if
condition|(
name|p
operator|.
name|matchesFamilyQualifier
argument_list|(
name|table
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|remove
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
block|{
name|tableCache
operator|.
name|remove
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|/**    * Overwrites the existing permission set for a given user for a table, and    * triggers an update for zookeeper synchronization.    * @param username    * @param table    * @param perms    */
specifier|public
name|void
name|setUserPermissions
parameter_list|(
name|String
name|username
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|List
argument_list|<
name|TablePermission
argument_list|>
name|perms
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tablePerms
init|=
name|getTablePermissions
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|tablePerms
operator|.
name|replaceUser
argument_list|(
name|username
argument_list|,
name|perms
argument_list|)
expr_stmt|;
name|writeToZooKeeper
argument_list|(
name|table
argument_list|,
name|tablePerms
argument_list|)
expr_stmt|;
block|}
comment|/**    * Overwrites the existing permission set for a group and triggers an update    * for zookeeper synchronization.    * @param group    * @param table    * @param perms    */
specifier|public
name|void
name|setGroupPermissions
parameter_list|(
name|String
name|group
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|List
argument_list|<
name|TablePermission
argument_list|>
name|perms
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tablePerms
init|=
name|getTablePermissions
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|tablePerms
operator|.
name|replaceGroup
argument_list|(
name|group
argument_list|,
name|perms
argument_list|)
expr_stmt|;
name|writeToZooKeeper
argument_list|(
name|table
argument_list|,
name|tablePerms
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeToZooKeeper
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tablePerms
parameter_list|)
block|{
name|byte
index|[]
name|serialized
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|tablePerms
operator|!=
literal|null
condition|)
block|{
name|serialized
operator|=
name|AccessControlLists
operator|.
name|writePermissionsAsBytes
argument_list|(
name|tablePerms
operator|.
name|getAllPermissions
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|zkperms
operator|.
name|writeToZookeeper
argument_list|(
name|table
argument_list|,
name|serialized
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Map
argument_list|<
name|ZooKeeperWatcher
argument_list|,
name|TableAuthManager
argument_list|>
name|managerMap
init|=
operator|new
name|HashMap
argument_list|<
name|ZooKeeperWatcher
argument_list|,
name|TableAuthManager
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|synchronized
specifier|static
name|TableAuthManager
name|get
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|instance
operator|=
name|managerMap
operator|.
name|get
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|TableAuthManager
argument_list|(
name|watcher
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|managerMap
operator|.
name|put
argument_list|(
name|watcher
argument_list|,
name|instance
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
block|}
end_class

end_unit

