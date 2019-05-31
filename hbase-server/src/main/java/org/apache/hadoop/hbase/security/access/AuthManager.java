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
name|security
operator|.
name|access
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|atomic
operator|.
name|AtomicLong
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
name|AuthUtil
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
name|Cell
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
name|exceptions
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
name|security
operator|.
name|Superusers
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
name|ListMultimap
import|;
end_import

begin_comment
comment|/**  * Performs authorization checks for a given user's assigned permissions.  *<p>  *   There're following scopes:<b>Global</b>,<b>Namespace</b>,<b>Table</b>,<b>Family</b>,  *<b>Qualifier</b>,<b>Cell</b>.  *   Generally speaking, higher scopes can overrides lower scopes,  *   except for Cell permission can be granted even a user has not permission on specified table,  *   which means the user can get/scan only those granted cells parts.  *</p>  * e.g, if user A has global permission R(ead), he can  * read table T without checking table scope permission, so authorization checks alway starts from  * Global scope.  *<p>  *   For each scope, not only user but also groups he belongs to will be checked.  *</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|AuthManager
block|{
comment|/**    * Cache of permissions, it is thread safe.    * @param<T> T extends Permission    */
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
specifier|private
specifier|final
name|Object
name|mutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|T
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|void
name|put
parameter_list|(
name|String
name|name
parameter_list|,
name|T
name|perm
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|perms
init|=
name|cache
operator|.
name|getOrDefault
argument_list|(
name|name
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|perms
operator|.
name|add
argument_list|(
name|perm
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|String
name|name
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
return|return
name|cache
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
name|void
name|clear
parameter_list|()
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|T
argument_list|>
argument_list|>
name|entry
range|:
name|cache
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|PermissionCache
argument_list|<
name|NamespacePermission
argument_list|>
name|NS_NO_PERMISSION
init|=
operator|new
name|PermissionCache
argument_list|<>
argument_list|()
decl_stmt|;
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|TBL_NO_PERMISSION
init|=
operator|new
name|PermissionCache
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Cache for global permission excluding superuser and supergroup.    * Since every user/group can only have one global permission, no need to use PermissionCache.    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|GlobalPermission
argument_list|>
name|globalCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/** Cache for namespace permission. */
specifier|private
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|PermissionCache
argument_list|<
name|NamespacePermission
argument_list|>
argument_list|>
name|namespaceCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/** Cache for table permission. */
specifier|private
name|ConcurrentHashMap
argument_list|<
name|TableName
argument_list|,
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
argument_list|>
name|tableCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
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
name|AuthManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|mtime
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
name|AuthManager
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Update acl info for table.    * @param table name of table    * @param data updated acl data    * @throws IOException exception when deserialize data    */
specifier|public
name|void
name|refreshTableCacheFromWritable
parameter_list|(
name|TableName
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
try|try
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|perms
init|=
name|PermissionStorage
operator|.
name|readPermissions
argument_list|(
name|data
argument_list|,
name|conf
argument_list|)
decl_stmt|;
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
operator|.
name|getName
argument_list|()
argument_list|,
name|PermissionStorage
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
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping permission cache refresh because writable data is empty"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Update acl info for namespace.    * @param namespace namespace    * @param data updated acl data    * @throws IOException exception when deserialize data    */
specifier|public
name|void
name|refreshNamespaceCacheFromWritable
parameter_list|(
name|String
name|namespace
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
try|try
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|perms
init|=
name|PermissionStorage
operator|.
name|readPermissions
argument_list|(
name|data
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|perms
operator|!=
literal|null
condition|)
block|{
name|updateNamespaceCache
argument_list|(
name|namespace
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * Updates the internal global permissions cache.    * @param globalPerms new global permissions    */
specifier|private
name|void
name|updateGlobalCache
parameter_list|(
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|globalPerms
parameter_list|)
block|{
name|globalCache
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|name
range|:
name|globalPerms
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|Permission
name|permission
range|:
name|globalPerms
operator|.
name|get
argument_list|(
name|name
argument_list|)
control|)
block|{
comment|// Before 2.2, the global permission which storage in zk is not right. It was saved as a
comment|// table permission. So here need to handle this for compatibility. See HBASE-22503.
if|if
condition|(
name|permission
operator|instanceof
name|TablePermission
condition|)
block|{
name|globalCache
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|GlobalPermission
argument_list|(
name|permission
operator|.
name|getActions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|globalCache
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|(
name|GlobalPermission
operator|)
name|permission
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|mtime
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
comment|/**    * Updates the internal table permissions cache for specified table.    * @param table updated table name    * @param tablePerms new table permissions    */
specifier|private
name|void
name|updateTableCache
parameter_list|(
name|TableName
name|table
parameter_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|tablePerms
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|cacheToUpdate
init|=
name|tableCache
operator|.
name|getOrDefault
argument_list|(
name|table
argument_list|,
operator|new
name|PermissionCache
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|clearCache
argument_list|(
name|cacheToUpdate
argument_list|)
expr_stmt|;
name|updateCache
argument_list|(
name|tablePerms
argument_list|,
name|cacheToUpdate
argument_list|)
expr_stmt|;
name|tableCache
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|cacheToUpdate
argument_list|)
expr_stmt|;
name|mtime
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
comment|/**    * Updates the internal namespace permissions cache for specified namespace.    * @param namespace updated namespace    * @param nsPerms new namespace permissions    */
specifier|private
name|void
name|updateNamespaceCache
parameter_list|(
name|String
name|namespace
parameter_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|nsPerms
parameter_list|)
block|{
name|PermissionCache
argument_list|<
name|NamespacePermission
argument_list|>
name|cacheToUpdate
init|=
name|namespaceCache
operator|.
name|getOrDefault
argument_list|(
name|namespace
argument_list|,
operator|new
name|PermissionCache
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|clearCache
argument_list|(
name|cacheToUpdate
argument_list|)
expr_stmt|;
name|updateCache
argument_list|(
name|nsPerms
argument_list|,
name|cacheToUpdate
argument_list|)
expr_stmt|;
name|namespaceCache
operator|.
name|put
argument_list|(
name|namespace
argument_list|,
name|cacheToUpdate
argument_list|)
expr_stmt|;
name|mtime
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|clearCache
parameter_list|(
name|PermissionCache
name|cacheToUpdate
parameter_list|)
block|{
name|cacheToUpdate
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|updateCache
parameter_list|(
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|Permission
argument_list|>
name|newPermissions
parameter_list|,
name|PermissionCache
name|cacheToUpdate
parameter_list|)
block|{
for|for
control|(
name|String
name|name
range|:
name|newPermissions
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|Permission
name|permission
range|:
name|newPermissions
operator|.
name|get
argument_list|(
name|name
argument_list|)
control|)
block|{
name|cacheToUpdate
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|permission
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Check if user has given action privilige in global scope.    * @param user user name    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeUserGlobal
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
name|Superusers
operator|.
name|isSuperUser
argument_list|(
name|user
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|authorizeGlobal
argument_list|(
name|globalCache
operator|.
name|get
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
for|for
control|(
name|String
name|group
range|:
name|user
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|authorizeGlobal
argument_list|(
name|globalCache
operator|.
name|get
argument_list|(
name|AuthUtil
operator|.
name|toGroupEntry
argument_list|(
name|group
argument_list|)
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
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|authorizeGlobal
parameter_list|(
name|GlobalPermission
name|permissions
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|permissions
operator|!=
literal|null
operator|&&
name|permissions
operator|.
name|implies
argument_list|(
name|action
argument_list|)
return|;
block|}
comment|/**    * Check if user has given action privilige in namespace scope.    * @param user user name    * @param namespace namespace    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeUserNamespace
parameter_list|(
name|User
name|user
parameter_list|,
name|String
name|namespace
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
name|authorizeUserGlobal
argument_list|(
name|user
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|PermissionCache
argument_list|<
name|NamespacePermission
argument_list|>
name|nsPermissions
init|=
name|namespaceCache
operator|.
name|getOrDefault
argument_list|(
name|namespace
argument_list|,
name|NS_NO_PERMISSION
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorizeNamespace
argument_list|(
name|nsPermissions
operator|.
name|get
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
argument_list|,
name|namespace
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|String
name|group
range|:
name|user
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|authorizeNamespace
argument_list|(
name|nsPermissions
operator|.
name|get
argument_list|(
name|AuthUtil
operator|.
name|toGroupEntry
argument_list|(
name|group
argument_list|)
argument_list|)
argument_list|,
name|namespace
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
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|authorizeNamespace
parameter_list|(
name|Set
argument_list|<
name|NamespacePermission
argument_list|>
name|permissions
parameter_list|,
name|String
name|namespace
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|permissions
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|NamespacePermission
name|permission
range|:
name|permissions
control|)
block|{
if|if
condition|(
name|permission
operator|.
name|implies
argument_list|(
name|namespace
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
return|return
literal|false
return|;
block|}
comment|/**    * Checks if the user has access to the full table or at least a family/qualifier    * for the specified action.    * @param user user name    * @param table table name    * @param action action in one of [Read, Write, Create, Exec, Admin]    * @return true if the user has access to the table, false otherwise    */
specifier|public
name|boolean
name|accessUserTable
parameter_list|(
name|User
name|user
parameter_list|,
name|TableName
name|table
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
name|table
operator|==
literal|null
condition|)
block|{
name|table
operator|=
name|PermissionStorage
operator|.
name|ACL_TABLE_NAME
expr_stmt|;
block|}
if|if
condition|(
name|authorizeUserNamespace
argument_list|(
name|user
argument_list|,
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tblPermissions
init|=
name|tableCache
operator|.
name|getOrDefault
argument_list|(
name|table
argument_list|,
name|TBL_NO_PERMISSION
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasAccessTable
argument_list|(
name|tblPermissions
operator|.
name|get
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
for|for
control|(
name|String
name|group
range|:
name|user
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|hasAccessTable
argument_list|(
name|tblPermissions
operator|.
name|get
argument_list|(
name|AuthUtil
operator|.
name|toGroupEntry
argument_list|(
name|group
argument_list|)
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
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|hasAccessTable
parameter_list|(
name|Set
argument_list|<
name|TablePermission
argument_list|>
name|permissions
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
if|if
condition|(
name|permissions
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|TablePermission
name|permission
range|:
name|permissions
control|)
block|{
if|if
condition|(
name|permission
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
return|return
literal|false
return|;
block|}
comment|/**    * Check if user has given action privilige in table scope.    * @param user user name    * @param table table name    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeUserTable
parameter_list|(
name|User
name|user
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
return|return
name|authorizeUserTable
argument_list|(
name|user
argument_list|,
name|table
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|action
argument_list|)
return|;
block|}
comment|/**    * Check if user has given action privilige in table:family scope.    * @param user user name    * @param table table name    * @param family family name    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeUserTable
parameter_list|(
name|User
name|user
parameter_list|,
name|TableName
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
name|authorizeUserTable
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
comment|/**    * Check if user has given action privilige in table:family:qualifier scope.    * @param user user name    * @param table table name    * @param family family name    * @param qualifier qualifier name    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeUserTable
parameter_list|(
name|User
name|user
parameter_list|,
name|TableName
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
name|table
operator|==
literal|null
condition|)
block|{
name|table
operator|=
name|PermissionStorage
operator|.
name|ACL_TABLE_NAME
expr_stmt|;
block|}
if|if
condition|(
name|authorizeUserNamespace
argument_list|(
name|user
argument_list|,
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|,
name|action
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|PermissionCache
argument_list|<
name|TablePermission
argument_list|>
name|tblPermissions
init|=
name|tableCache
operator|.
name|getOrDefault
argument_list|(
name|table
argument_list|,
name|TBL_NO_PERMISSION
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorizeTable
argument_list|(
name|tblPermissions
operator|.
name|get
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
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
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|String
name|group
range|:
name|user
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|authorizeTable
argument_list|(
name|tblPermissions
operator|.
name|get
argument_list|(
name|AuthUtil
operator|.
name|toGroupEntry
argument_list|(
name|group
argument_list|)
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
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|authorizeTable
parameter_list|(
name|Set
argument_list|<
name|TablePermission
argument_list|>
name|permissions
parameter_list|,
name|TableName
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
name|permissions
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|TablePermission
name|permission
range|:
name|permissions
control|)
block|{
if|if
condition|(
name|permission
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
return|return
literal|false
return|;
block|}
comment|/**    * Check if user has given action privilige in table:family scope.    * This method is for backward compatibility.    * @param user user name    * @param table table name    * @param family family names    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeUserFamily
parameter_list|(
name|User
name|user
parameter_list|,
name|TableName
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
name|tblPermissions
init|=
name|tableCache
operator|.
name|getOrDefault
argument_list|(
name|table
argument_list|,
name|TBL_NO_PERMISSION
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorizeFamily
argument_list|(
name|tblPermissions
operator|.
name|get
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
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
for|for
control|(
name|String
name|group
range|:
name|user
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|authorizeFamily
argument_list|(
name|tblPermissions
operator|.
name|get
argument_list|(
name|AuthUtil
operator|.
name|toGroupEntry
argument_list|(
name|group
argument_list|)
argument_list|)
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
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|authorizeFamily
parameter_list|(
name|Set
argument_list|<
name|TablePermission
argument_list|>
name|permissions
parameter_list|,
name|TableName
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
if|if
condition|(
name|permissions
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|TablePermission
name|permission
range|:
name|permissions
control|)
block|{
if|if
condition|(
name|permission
operator|.
name|implies
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
return|return
literal|false
return|;
block|}
comment|/**    * Check if user has given action privilige in cell scope.    * @param user user name    * @param table table name    * @param cell cell to be checked    * @param action one of action in [Read, Write, Create, Exec, Admin]    * @return true if user has, false otherwise    */
specifier|public
name|boolean
name|authorizeCell
parameter_list|(
name|User
name|user
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Cell
name|cell
parameter_list|,
name|Permission
operator|.
name|Action
name|action
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|Permission
argument_list|>
name|perms
init|=
name|PermissionStorage
operator|.
name|getCellPermissionsForUser
argument_list|(
name|user
argument_list|,
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Perms for user {} in table {} in cell {}: {}"
argument_list|,
name|user
operator|.
name|getShortName
argument_list|()
argument_list|,
name|table
argument_list|,
name|cell
argument_list|,
operator|(
name|perms
operator|!=
literal|null
condition|?
name|perms
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
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
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// We failed to parse the KV tag
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed parse of ACL tag in cell "
operator|+
name|cell
argument_list|)
expr_stmt|;
comment|// Fall through to check with the table and CF perms we were able
comment|// to collect regardless
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Remove given namespace from AuthManager's namespace cache.    * @param ns namespace    */
specifier|public
name|void
name|removeNamespace
parameter_list|(
name|byte
index|[]
name|ns
parameter_list|)
block|{
name|namespaceCache
operator|.
name|remove
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|ns
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove given table from AuthManager's table cache.    * @param table table name    */
specifier|public
name|void
name|removeTable
parameter_list|(
name|TableName
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
comment|/**    * Last modification logical time    * @return time    */
specifier|public
name|long
name|getMTime
parameter_list|()
block|{
return|return
name|mtime
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

