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
name|quotas
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
name|Mutation
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
name|Table
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
name|BloomType
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
name|EnvironmentEdgeManager
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
operator|.
name|Quotas
import|;
end_import

begin_comment
comment|/**  * Helper class to interact with the quota table  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|QuotaUtil
extends|extends
name|QuotaTableUtil
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
name|QuotaUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|QUOTA_CONF_KEY
init|=
literal|"hbase.quota.enabled"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|QUOTA_ENABLED_DEFAULT
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|READ_CAPACITY_UNIT_CONF_KEY
init|=
literal|"hbase.quota.read.capacity.unit"
decl_stmt|;
comment|// the default one read capacity unit is 1024 bytes (1KB)
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_READ_CAPACITY_UNIT
init|=
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITE_CAPACITY_UNIT_CONF_KEY
init|=
literal|"hbase.quota.write.capacity.unit"
decl_stmt|;
comment|// the default one write capacity unit is 1024 bytes (1KB)
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_WRITE_CAPACITY_UNIT
init|=
literal|1024
decl_stmt|;
comment|/** Table descriptor for Quota internal table */
specifier|public
specifier|static
specifier|final
name|HTableDescriptor
name|QUOTA_TABLE_DESC
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|QUOTA_TABLE_NAME
argument_list|)
decl_stmt|;
static|static
block|{
name|QUOTA_TABLE_DESC
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|QUOTA_FAMILY_INFO
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|ROW
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|QUOTA_TABLE_DESC
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|QUOTA_FAMILY_USAGE
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|ROW
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Returns true if the support for quota is enabled */
specifier|public
specifier|static
name|boolean
name|isQuotaEnabled
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|QUOTA_CONF_KEY
argument_list|,
name|QUOTA_ENABLED_DEFAULT
argument_list|)
return|;
block|}
comment|/* =========================================================================    *  Quota "settings" helpers    */
specifier|public
specifier|static
name|void
name|addTableQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|getTableRowKey
argument_list|(
name|table
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteTableQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|getTableRowKey
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|addNamespaceQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|getNamespaceRowKey
argument_list|(
name|namespace
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteNamespaceQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|getNamespaceRowKey
argument_list|(
name|namespace
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|addUserQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|user
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|getUserRowKey
argument_list|(
name|user
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|addUserQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|user
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|getUserRowKey
argument_list|(
name|user
argument_list|)
argument_list|,
name|getSettingsQualifierForUserTable
argument_list|(
name|table
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|addUserQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|user
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|getUserRowKey
argument_list|(
name|user
argument_list|)
argument_list|,
name|getSettingsQualifierForUserNamespace
argument_list|(
name|namespace
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteUserQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|getUserRowKey
argument_list|(
name|user
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteUserQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|user
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|getUserRowKey
argument_list|(
name|user
argument_list|)
argument_list|,
name|getSettingsQualifierForUserTable
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteUserQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|user
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|getUserRowKey
argument_list|(
name|user
argument_list|)
argument_list|,
name|getSettingsQualifierForUserNamespace
argument_list|(
name|namespace
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|addRegionServerQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|regionServer
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|getRegionServerRowKey
argument_list|(
name|regionServer
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|deleteRegionServerQuota
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|String
name|regionServer
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|getRegionServerRowKey
argument_list|(
name|regionServer
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|addQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|addQuotas
argument_list|(
name|connection
argument_list|,
name|rowKey
argument_list|,
name|QUOTA_QUALIFIER_SETTINGS
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|addQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|Quotas
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|QUOTA_FAMILY_INFO
argument_list|,
name|qualifier
argument_list|,
name|quotasToData
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|doPut
argument_list|(
name|connection
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|deleteQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteQuotas
argument_list|(
name|connection
argument_list|,
name|rowKey
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|deleteQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|delete
operator|.
name|addColumns
argument_list|(
name|QUOTA_FAMILY_INFO
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
block|}
name|doDelete
argument_list|(
name|connection
argument_list|,
name|delete
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
name|fetchUserQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|nowTs
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Result
index|[]
name|results
init|=
name|doGet
argument_list|(
name|connection
argument_list|,
name|gets
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|UserQuotaState
argument_list|>
name|userQuotas
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|results
operator|.
name|length
argument_list|)
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
name|results
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|key
init|=
name|gets
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRow
argument_list|()
decl_stmt|;
assert|assert
name|isUserRowKey
argument_list|(
name|key
argument_list|)
assert|;
name|String
name|user
init|=
name|getUserFromRowKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
specifier|final
name|UserQuotaState
name|quotaInfo
init|=
operator|new
name|UserQuotaState
argument_list|(
name|nowTs
argument_list|)
decl_stmt|;
name|userQuotas
operator|.
name|put
argument_list|(
name|user
argument_list|,
name|quotaInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
index|[
name|i
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
assert|assert
name|Bytes
operator|.
name|equals
argument_list|(
name|key
argument_list|,
name|results
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
assert|;
try|try
block|{
name|parseUserResult
argument_list|(
name|user
argument_list|,
name|results
index|[
name|i
index|]
argument_list|,
operator|new
name|UserQuotasVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visitUserQuotas
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|namespace
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|quotaInfo
operator|.
name|setQuotas
argument_list|(
name|namespace
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitUserQuotas
parameter_list|(
name|String
name|userName
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|quotaInfo
operator|.
name|setQuotas
argument_list|(
name|table
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitUserQuotas
parameter_list|(
name|String
name|userName
parameter_list|,
name|Quotas
name|quotas
parameter_list|)
block|{
name|quotaInfo
operator|.
name|setQuotas
argument_list|(
name|quotas
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Unable to parse user '"
operator|+
name|user
operator|+
literal|"' quotas"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|userQuotas
operator|.
name|remove
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|userQuotas
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|TableName
argument_list|,
name|QuotaState
argument_list|>
name|fetchTableQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fetchGlobalQuotas
argument_list|(
literal|"table"
argument_list|,
name|connection
argument_list|,
name|gets
argument_list|,
operator|new
name|KeyFromRow
argument_list|<
name|TableName
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TableName
name|getKeyFromRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
assert|assert
name|isTableRowKey
argument_list|(
name|row
argument_list|)
assert|;
return|return
name|getTableFromRowKey
argument_list|(
name|row
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
name|fetchNamespaceQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fetchGlobalQuotas
argument_list|(
literal|"namespace"
argument_list|,
name|connection
argument_list|,
name|gets
argument_list|,
operator|new
name|KeyFromRow
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|getKeyFromRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
assert|assert
name|isNamespaceRowKey
argument_list|(
name|row
argument_list|)
assert|;
return|return
name|getNamespaceFromRowKey
argument_list|(
name|row
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|QuotaState
argument_list|>
name|fetchRegionServerQuotas
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fetchGlobalQuotas
argument_list|(
literal|"regionServer"
argument_list|,
name|connection
argument_list|,
name|gets
argument_list|,
operator|new
name|KeyFromRow
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|getKeyFromRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
assert|assert
name|isRegionServerRowKey
argument_list|(
name|row
argument_list|)
assert|;
return|return
name|getRegionServerFromRowKey
argument_list|(
name|row
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|>
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaState
argument_list|>
name|fetchGlobalQuotas
parameter_list|(
specifier|final
name|String
name|type
parameter_list|,
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|,
specifier|final
name|KeyFromRow
argument_list|<
name|K
argument_list|>
name|kfr
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|nowTs
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Result
index|[]
name|results
init|=
name|doGet
argument_list|(
name|connection
argument_list|,
name|gets
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|K
argument_list|,
name|QuotaState
argument_list|>
name|globalQuotas
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|results
operator|.
name|length
argument_list|)
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
name|results
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|byte
index|[]
name|row
init|=
name|gets
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|K
name|key
init|=
name|kfr
operator|.
name|getKeyFromRow
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|QuotaState
name|quotaInfo
init|=
operator|new
name|QuotaState
argument_list|(
name|nowTs
argument_list|)
decl_stmt|;
name|globalQuotas
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|quotaInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
index|[
name|i
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
assert|assert
name|Bytes
operator|.
name|equals
argument_list|(
name|row
argument_list|,
name|results
index|[
name|i
index|]
operator|.
name|getRow
argument_list|()
argument_list|)
assert|;
name|byte
index|[]
name|data
init|=
name|results
index|[
name|i
index|]
operator|.
name|getValue
argument_list|(
name|QUOTA_FAMILY_INFO
argument_list|,
name|QUOTA_QUALIFIER_SETTINGS
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
continue|continue;
try|try
block|{
name|Quotas
name|quotas
init|=
name|quotasFromData
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|quotaInfo
operator|.
name|setQuotas
argument_list|(
name|quotas
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
literal|"Unable to parse "
operator|+
name|type
operator|+
literal|" '"
operator|+
name|key
operator|+
literal|"' quotas"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|globalQuotas
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|globalQuotas
return|;
block|}
specifier|private
specifier|static
interface|interface
name|KeyFromRow
parameter_list|<
name|T
parameter_list|>
block|{
name|T
name|getKeyFromRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
function_decl|;
block|}
comment|/* =========================================================================    *  HTable helpers    */
specifier|private
specifier|static
name|void
name|doPut
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|doDelete
parameter_list|(
specifier|final
name|Connection
name|connection
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
init|)
block|{
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* =========================================================================    *  Data Size Helpers    */
specifier|public
specifier|static
name|long
name|calculateMutationSize
parameter_list|(
specifier|final
name|Mutation
name|mutation
parameter_list|)
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|entry
range|:
name|mutation
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|size
operator|+=
name|cell
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|size
return|;
block|}
specifier|public
specifier|static
name|long
name|calculateResultSize
parameter_list|(
specifier|final
name|Result
name|result
parameter_list|)
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|result
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|size
operator|+=
name|cell
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
specifier|public
specifier|static
name|long
name|calculateResultSize
parameter_list|(
specifier|final
name|List
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|)
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|result
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|size
operator|+=
name|cell
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

