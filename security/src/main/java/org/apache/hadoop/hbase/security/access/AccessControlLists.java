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
name|HColumnDescriptor
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
name|HConstants
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
name|client
operator|.
name|Delete
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
name|Get
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
name|HTable
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
name|Put
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
name|ResultScanner
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
name|Scan
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
name|io
operator|.
name|HbaseObjectWritable
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
name|io
operator|.
name|hfile
operator|.
name|Compression
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
name|InternalScanner
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
name|StoreFile
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
name|util
operator|.
name|Pair
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
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|*
import|;
end_import

begin_comment
comment|/**  * Maintains lists of permission grants to users and groups to allow for  * authorization checks by {@link AccessController}.  *  *<p>  * Access control lists are stored in an "internal" metadata table named  * {@code _acl_}. Each table's permission grants are stored as a separate row,  * keyed by the table name. KeyValues for permissions assignments are stored  * in one of the formats:  *<pre>  * Key                      Desc  * --------                 --------  * user                     table level permissions for a user [R=read, W=write]  * @group                   table level permissions for a group  * user,family              column family level permissions for a user  * @group,family            column family level permissions for a group  * user,family,qualifier    column qualifier level permissions for a user  * @group,family,qualifier  column qualifier level permissions for a group  *</pre>  * All values are encoded as byte arrays containing the codes from the  * {@link org.apache.hadoop.hbase.security.access.TablePermission.Action} enum.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|AccessControlLists
block|{
comment|/** Internal storage table for access control lists */
specifier|public
specifier|static
specifier|final
name|String
name|ACL_TABLE_NAME_STR
init|=
literal|"_acl_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ACL_TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ACL_TABLE_NAME_STR
argument_list|)
decl_stmt|;
comment|/** Column family used to store ACL grants */
specifier|public
specifier|static
specifier|final
name|String
name|ACL_LIST_FAMILY_STR
init|=
literal|"l"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ACL_LIST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ACL_LIST_FAMILY_STR
argument_list|)
decl_stmt|;
comment|/** Table descriptor for ACL internal table */
specifier|public
specifier|static
specifier|final
name|HTableDescriptor
name|ACL_TABLEDESC
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|ACL_TABLE_NAME
argument_list|)
decl_stmt|;
static|static
block|{
name|ACL_TABLEDESC
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|ACL_LIST_FAMILY
argument_list|,
literal|10
argument_list|,
comment|// Ten is arbitrary number.  Keep versions to help debugging.
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
operator|.
name|getName
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|8
operator|*
literal|1024
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
name|StoreFile
operator|.
name|BloomType
operator|.
name|NONE
operator|.
name|toString
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delimiter to separate user, column family, and qualifier in    * _acl_ table info: column keys */
specifier|public
specifier|static
specifier|final
name|char
name|ACL_KEY_DELIMITER
init|=
literal|','
decl_stmt|;
comment|/** Prefix character to denote group names */
specifier|public
specifier|static
specifier|final
name|String
name|GROUP_PREFIX
init|=
literal|"@"
decl_stmt|;
comment|/** Configuration key for superusers */
specifier|public
specifier|static
specifier|final
name|String
name|SUPERUSER_CONF_KEY
init|=
literal|"hbase.superuser"
decl_stmt|;
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AccessControlLists
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Check for existence of {@code _acl_} table and create it if it does not exist    * @param master reference to HMaster    */
specifier|static
name|void
name|init
parameter_list|(
name|MasterServices
name|master
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|master
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|ACL_TABLE_NAME_STR
argument_list|)
condition|)
block|{
name|master
operator|.
name|createTable
argument_list|(
name|ACL_TABLEDESC
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Stores a new table permission grant in the access control lists table.    * @param conf the configuration    * @param tableName the table to which access is being granted    * @param username the user or group being granted the permission    * @param perm the details of the permission being granted    * @throws IOException in the case of an error accessing the metadata table    */
specifier|static
name|void
name|addTablePermission
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|username
parameter_list|,
name|TablePermission
name|perm
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|username
argument_list|)
decl_stmt|;
if|if
condition|(
name|perm
operator|.
name|getFamily
argument_list|()
operator|!=
literal|null
operator|&&
name|perm
operator|.
name|getFamily
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|key
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|Bytes
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
name|ACL_KEY_DELIMITER
block|}
argument_list|,
name|perm
operator|.
name|getFamily
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|perm
operator|.
name|getQualifier
argument_list|()
operator|!=
literal|null
operator|&&
name|perm
operator|.
name|getQualifier
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|key
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|Bytes
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
name|ACL_KEY_DELIMITER
block|}
argument_list|,
name|perm
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|TablePermission
operator|.
name|Action
index|[]
name|actions
init|=
name|perm
operator|.
name|getActions
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|actions
operator|==
literal|null
operator|)
operator|||
operator|(
name|actions
operator|.
name|length
operator|==
literal|0
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No actions associated with user '"
operator|+
name|username
operator|+
literal|"'"
argument_list|)
expr_stmt|;
return|return;
block|}
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|actions
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|actions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|value
index|[
name|i
index|]
operator|=
name|actions
index|[
name|i
index|]
operator|.
name|code
argument_list|()
expr_stmt|;
block|}
name|p
operator|.
name|add
argument_list|(
name|ACL_LIST_FAMILY
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
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
literal|"Writing permission for table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|key
argument_list|)
operator|+
literal|": "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HTable
name|acls
init|=
literal|null
decl_stmt|;
try|try
block|{
name|acls
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|ACL_TABLE_NAME
argument_list|)
expr_stmt|;
name|acls
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|acls
operator|!=
literal|null
condition|)
name|acls
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Removes a previously granted permission from the stored access control    * lists.  The {@link TablePermission} being removed must exactly match what    * is stored -- no wildcard matching is attempted.  Ie, if user "bob" has    * been granted "READ" access to the "data" table, but only to column family    * plus qualifier "info:colA", then trying to call this method with only    * user "bob" and the table name "data" (but without specifying the    * column qualifier "info:colA") will have no effect.    *    * @param conf the configuration    * @param tableName the table of the current permission grant    * @param userName the user or group currently granted the permission    * @param perm the details of the permission to be revoked    * @throws IOException if there is an error accessing the metadata table    */
specifier|static
name|void
name|removeTablePermission
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|userName
parameter_list|,
name|TablePermission
name|perm
parameter_list|)
throws|throws
name|IOException
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|key
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|perm
operator|.
name|getFamily
argument_list|()
operator|!=
literal|null
operator|&&
name|perm
operator|.
name|getFamily
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|key
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|userName
operator|+
name|ACL_KEY_DELIMITER
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|perm
operator|.
name|getFamily
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|perm
operator|.
name|getQualifier
argument_list|()
operator|!=
literal|null
operator|&&
name|perm
operator|.
name|getQualifier
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|key
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|userName
operator|+
name|ACL_KEY_DELIMITER
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|perm
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|+
name|ACL_KEY_DELIMITER
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|perm
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|key
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|userName
operator|+
name|ACL_KEY_DELIMITER
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|perm
operator|.
name|getFamily
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|key
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|userName
argument_list|)
expr_stmt|;
block|}
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
literal|"Removing permission for user '"
operator|+
name|userName
operator|+
literal|"': "
operator|+
name|perm
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|d
operator|.
name|deleteColumns
argument_list|(
name|ACL_LIST_FAMILY
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|HTable
name|acls
init|=
literal|null
decl_stmt|;
try|try
block|{
name|acls
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|ACL_TABLE_NAME
argument_list|)
expr_stmt|;
name|acls
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|acls
operator|!=
literal|null
condition|)
name|acls
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Returns {@code true} if the given region is part of the {@code _acl_}    * metadata table.    */
specifier|static
name|boolean
name|isAclRegion
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|ACL_TABLE_NAME
argument_list|,
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Loads all of the permission grants stored in a region of the {@code _acl_}    * table.    *    * @param aclRegion    * @return    * @throws IOException    */
specifier|static
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|>
name|loadAll
parameter_list|(
name|HRegion
name|aclRegion
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isAclRegion
argument_list|(
name|aclRegion
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can only load permissions from "
operator|+
name|ACL_TABLE_NAME_STR
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|>
name|allPerms
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|// do a full scan of _acl_ table
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|ACL_LIST_FAMILY
argument_list|)
expr_stmt|;
name|InternalScanner
name|iScanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|iScanner
operator|=
name|aclRegion
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|row
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasNext
init|=
name|iScanner
operator|.
name|next
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|perms
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|byte
index|[]
name|table
init|=
literal|null
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|row
control|)
block|{
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
name|table
operator|=
name|kv
operator|.
name|getRow
argument_list|()
expr_stmt|;
block|}
name|Pair
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|permissionsOfUserOnTable
init|=
name|parseTablePermissionRecord
argument_list|(
name|table
argument_list|,
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|permissionsOfUserOnTable
operator|!=
literal|null
condition|)
block|{
name|String
name|username
init|=
name|permissionsOfUserOnTable
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|TablePermission
name|permissions
init|=
name|permissionsOfUserOnTable
operator|.
name|getSecond
argument_list|()
decl_stmt|;
name|perms
operator|.
name|put
argument_list|(
name|username
argument_list|,
name|permissions
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|allPerms
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hasNext
condition|)
block|{
break|break;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|iScanner
operator|!=
literal|null
condition|)
block|{
name|iScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|allPerms
return|;
block|}
comment|/**    * Load all permissions from the region server holding {@code _acl_},    * primarily intended for testing purposes.    */
specifier|static
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|>
name|loadAll
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|>
name|allPerms
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|// do a full scan of _acl_, filtering on only first table region rows
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|ACL_LIST_FAMILY
argument_list|)
expr_stmt|;
name|HTable
name|acls
init|=
literal|null
decl_stmt|;
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|acls
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|ACL_TABLE_NAME
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|acls
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|row
range|:
name|scanner
control|)
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|resultPerms
init|=
name|parseTablePermissions
argument_list|(
name|row
operator|.
name|getRow
argument_list|()
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|allPerms
operator|.
name|put
argument_list|(
name|row
operator|.
name|getRow
argument_list|()
argument_list|,
name|resultPerms
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|acls
operator|!=
literal|null
condition|)
name|acls
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|allPerms
return|;
block|}
comment|/**    * Reads user permission assignments stored in the<code>l:</code> column    * family of the first table row in<code>_acl_</code>.    *    *<p>    * See {@link AccessControlLists class documentation} for the key structure    * used for storage.    *</p>    */
specifier|static
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|getTablePermissions
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* TODO: -ROOT- and .META. cannot easily be handled because they must be      * online before _acl_ table.  Can anything be done here?      */
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
argument_list|,
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
condition|)
block|{
return|return
name|ArrayListMultimap
operator|.
name|create
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|// for normal user tables, we just read the table row from _acl_
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|perms
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|HTable
name|acls
init|=
literal|null
decl_stmt|;
try|try
block|{
name|acls
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|ACL_TABLE_NAME
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|ACL_LIST_FAMILY
argument_list|)
expr_stmt|;
name|Result
name|row
init|=
name|acls
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|row
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|perms
operator|=
name|parseTablePermissions
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No permissions found in "
operator|+
name|ACL_TABLE_NAME_STR
operator|+
literal|" for table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|acls
operator|!=
literal|null
condition|)
name|acls
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|perms
return|;
block|}
comment|/**    * Returns the currently granted permissions for a given table as a list of    * user plus associated permissions.    */
specifier|static
name|List
argument_list|<
name|UserPermission
argument_list|>
name|getUserPermissions
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|allPerms
init|=
name|getTablePermissions
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|UserPermission
argument_list|>
name|perms
init|=
operator|new
name|ArrayList
argument_list|<
name|UserPermission
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
name|allPerms
operator|.
name|entries
argument_list|()
control|)
block|{
name|UserPermission
name|up
init|=
operator|new
name|UserPermission
argument_list|(
name|Bytes
operator|.
name|toBytes
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
operator|.
name|getTable
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getFamily
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getActions
argument_list|()
argument_list|)
decl_stmt|;
name|perms
operator|.
name|add
argument_list|(
name|up
argument_list|)
expr_stmt|;
block|}
return|return
name|perms
return|;
block|}
specifier|private
specifier|static
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|parseTablePermissions
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|Result
name|result
parameter_list|)
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|perms
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
name|result
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|result
operator|.
name|raw
argument_list|()
control|)
block|{
name|Pair
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|permissionsOfUserOnTable
init|=
name|parseTablePermissionRecord
argument_list|(
name|table
argument_list|,
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|permissionsOfUserOnTable
operator|!=
literal|null
condition|)
block|{
name|String
name|username
init|=
name|permissionsOfUserOnTable
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|TablePermission
name|permissions
init|=
name|permissionsOfUserOnTable
operator|.
name|getSecond
argument_list|()
decl_stmt|;
name|perms
operator|.
name|put
argument_list|(
name|username
argument_list|,
name|permissions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|perms
return|;
block|}
specifier|private
specifier|static
name|Pair
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
name|parseTablePermissionRecord
parameter_list|(
name|byte
index|[]
name|table
parameter_list|,
name|KeyValue
name|kv
parameter_list|)
block|{
comment|// return X given a set of permissions encoded in the permissionRecord kv.
name|byte
index|[]
name|family
init|=
name|kv
operator|.
name|getFamily
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|family
argument_list|,
name|ACL_LIST_FAMILY
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|key
init|=
name|kv
operator|.
name|getQualifier
argument_list|()
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|kv
operator|.
name|getValue
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
literal|"Read acl: kv ["
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|key
argument_list|)
operator|+
literal|": "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
comment|// check for a column family appended to the key
comment|// TODO: avoid the string conversion to make this more efficient
name|String
name|username
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
name|username
operator|.
name|indexOf
argument_list|(
name|ACL_KEY_DELIMITER
argument_list|)
decl_stmt|;
name|byte
index|[]
name|permFamily
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|permQualifier
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
operator|&&
name|idx
operator|<
name|username
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|)
block|{
name|String
name|remainder
init|=
name|username
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
decl_stmt|;
name|username
operator|=
name|username
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|idx
argument_list|)
expr_stmt|;
name|idx
operator|=
name|remainder
operator|.
name|indexOf
argument_list|(
name|ACL_KEY_DELIMITER
argument_list|)
expr_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
operator|&&
name|idx
operator|<
name|remainder
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|)
block|{
name|permFamily
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|remainder
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|idx
argument_list|)
argument_list|)
expr_stmt|;
name|permQualifier
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|remainder
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|permFamily
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|remainder
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Pair
argument_list|<
name|String
argument_list|,
name|TablePermission
argument_list|>
argument_list|(
name|username
argument_list|,
operator|new
name|TablePermission
argument_list|(
name|table
argument_list|,
name|permFamily
argument_list|,
name|permQualifier
argument_list|,
name|value
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Writes a set of permissions as {@link org.apache.hadoop.io.Writable} instances    * to the given output stream.    * @param out    * @param perms    * @param conf    * @throws IOException    */
specifier|public
specifier|static
name|void
name|writePermissions
parameter_list|(
name|DataOutput
name|out
parameter_list|,
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|Permission
argument_list|>
name|perms
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
name|perms
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|keys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|perms
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|,
name|List
operator|.
name|class
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes a set of permissions as {@link org.apache.hadoop.io.Writable} instances    * and returns the resulting byte array.    */
specifier|public
specifier|static
name|byte
index|[]
name|writePermissionsAsBytes
parameter_list|(
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|Permission
argument_list|>
name|perms
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
try|try
block|{
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|writePermissions
argument_list|(
operator|new
name|DataOutputStream
argument_list|(
name|bos
argument_list|)
argument_list|,
name|perms
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|bos
operator|.
name|toByteArray
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// shouldn't happen here
name|LOG
operator|.
name|error
argument_list|(
literal|"Error serializing permissions"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Reads a set of permissions as {@link org.apache.hadoop.io.Writable} instances    * from the input stream.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Permission
parameter_list|>
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|readPermissions
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|perms
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|user
init|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|T
argument_list|>
name|userPerms
init|=
operator|(
name|List
operator|)
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|perms
operator|.
name|putAll
argument_list|(
name|user
argument_list|,
name|userPerms
argument_list|)
expr_stmt|;
block|}
return|return
name|perms
return|;
block|}
comment|/**    * Returns whether or not the given name should be interpreted as a group    * principal.  Currently this simply checks if the name starts with the    * special group prefix character ("@").    */
specifier|public
specifier|static
name|boolean
name|isGroupPrincipal
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|name
operator|!=
literal|null
operator|&&
name|name
operator|.
name|startsWith
argument_list|(
name|GROUP_PREFIX
argument_list|)
return|;
block|}
comment|/**    * Returns the actual name for a group principal (stripped of the    * group prefix).    */
specifier|public
specifier|static
name|String
name|getGroupName
parameter_list|(
name|String
name|aclKey
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isGroupPrincipal
argument_list|(
name|aclKey
argument_list|)
condition|)
block|{
return|return
name|aclKey
return|;
block|}
return|return
name|aclKey
operator|.
name|substring
argument_list|(
name|GROUP_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

