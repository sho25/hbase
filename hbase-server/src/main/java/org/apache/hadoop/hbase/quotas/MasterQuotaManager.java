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
name|HashSet
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
name|classification
operator|.
name|InterfaceStability
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
name|Coprocessor
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
name|DoNotRetryIOException
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
name|coprocessor
operator|.
name|BaseMasterObserver
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
name|master
operator|.
name|handler
operator|.
name|CreateTableHandler
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
name|MasterProtos
operator|.
name|SetQuotaRequest
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
name|MasterProtos
operator|.
name|SetQuotaResponse
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
name|QuotaProtos
operator|.
name|Quotas
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
name|QuotaProtos
operator|.
name|Throttle
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
name|QuotaProtos
operator|.
name|TimedQuota
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
name|QuotaProtos
operator|.
name|ThrottleRequest
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
name|QuotaProtos
operator|.
name|ThrottleType
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
name|QuotaProtos
operator|.
name|QuotaScope
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
name|HBaseProtos
operator|.
name|TimeUnit
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

begin_comment
comment|/**  * Master Quota Manager.  * It is responsible for initialize the quota table on the first-run and  * provide the admin operations to interact with the quota table.  *  * TODO: FUTURE: The master will be responsible to notify each RS of quota changes  * and it will do the "quota aggregation" when the QuotaScope is CLUSTER.  */
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
name|MasterQuotaManager
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
name|MasterQuotaManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
name|NamedLock
argument_list|<
name|String
argument_list|>
name|namespaceLocks
decl_stmt|;
specifier|private
name|NamedLock
argument_list|<
name|TableName
argument_list|>
name|tableLocks
decl_stmt|;
specifier|private
name|NamedLock
argument_list|<
name|String
argument_list|>
name|userLocks
decl_stmt|;
specifier|private
name|boolean
name|enabled
init|=
literal|false
decl_stmt|;
specifier|public
name|MasterQuotaManager
parameter_list|(
specifier|final
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
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
comment|// If the user doesn't want the quota support skip all the initializations.
if|if
condition|(
operator|!
name|QuotaUtil
operator|.
name|isQuotaEnabled
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Quota support disabled"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Create the quota table if missing
if|if
condition|(
operator|!
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|masterServices
operator|.
name|getShortCircuitConnection
argument_list|()
argument_list|,
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Quota table not found. Creating..."
argument_list|)
expr_stmt|;
name|createQuotaTable
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing quota support"
argument_list|)
expr_stmt|;
name|namespaceLocks
operator|=
operator|new
name|NamedLock
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|tableLocks
operator|=
operator|new
name|NamedLock
argument_list|<
name|TableName
argument_list|>
argument_list|()
expr_stmt|;
name|userLocks
operator|=
operator|new
name|NamedLock
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|enabled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{   }
specifier|public
name|boolean
name|isQuotaEnabled
parameter_list|()
block|{
return|return
name|enabled
return|;
block|}
specifier|private
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|masterServices
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
comment|/* ==========================================================================    *  Admin operations to manage the quota table    */
specifier|public
name|SetQuotaResponse
name|setQuota
parameter_list|(
specifier|final
name|SetQuotaRequest
name|req
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|checkQuotaSupport
argument_list|()
expr_stmt|;
if|if
condition|(
name|req
operator|.
name|hasUserName
argument_list|()
condition|)
block|{
name|userLocks
operator|.
name|lock
argument_list|(
name|req
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|req
operator|.
name|hasTableName
argument_list|()
condition|)
block|{
name|setUserQuota
argument_list|(
name|req
operator|.
name|getUserName
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|req
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|req
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|req
operator|.
name|hasNamespace
argument_list|()
condition|)
block|{
name|setUserQuota
argument_list|(
name|req
operator|.
name|getUserName
argument_list|()
argument_list|,
name|req
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|req
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setUserQuota
argument_list|(
name|req
operator|.
name|getUserName
argument_list|()
argument_list|,
name|req
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|userLocks
operator|.
name|unlock
argument_list|(
name|req
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|req
operator|.
name|hasTableName
argument_list|()
condition|)
block|{
name|TableName
name|table
init|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|req
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|tableLocks
operator|.
name|lock
argument_list|(
name|table
argument_list|)
expr_stmt|;
try|try
block|{
name|setTableQuota
argument_list|(
name|table
argument_list|,
name|req
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|tableLocks
operator|.
name|unlock
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|req
operator|.
name|hasNamespace
argument_list|()
condition|)
block|{
name|namespaceLocks
operator|.
name|lock
argument_list|(
name|req
operator|.
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|setNamespaceQuota
argument_list|(
name|req
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|req
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|namespaceLocks
operator|.
name|unlock
argument_list|(
name|req
operator|.
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"a user, a table or a namespace must be specified"
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|SetQuotaResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
name|void
name|setUserQuota
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|SetQuotaRequest
name|req
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|setQuota
argument_list|(
name|req
argument_list|,
operator|new
name|SetQuotaOperations
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Quotas
name|fetch
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|addUserQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|deleteUserQuota
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preSetUserQuota
argument_list|(
name|userName
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postSetUserQuota
argument_list|(
name|userName
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setUserQuota
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|,
specifier|final
name|SetQuotaRequest
name|req
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|setQuota
argument_list|(
name|req
argument_list|,
operator|new
name|SetQuotaOperations
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Quotas
name|fetch
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|addUserQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
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
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|deleteUserQuota
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preSetUserQuota
argument_list|(
name|userName
argument_list|,
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
name|postApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postSetUserQuota
argument_list|(
name|userName
argument_list|,
name|table
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setUserQuota
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|SetQuotaRequest
name|req
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|setQuota
argument_list|(
name|req
argument_list|,
operator|new
name|SetQuotaOperations
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Quotas
name|fetch
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|getUserQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
name|namespace
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|addUserQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
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
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|deleteUserQuota
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|userName
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preSetUserQuota
argument_list|(
name|userName
argument_list|,
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
name|postApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postSetUserQuota
argument_list|(
name|userName
argument_list|,
name|namespace
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setTableQuota
parameter_list|(
specifier|final
name|TableName
name|table
parameter_list|,
specifier|final
name|SetQuotaRequest
name|req
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|setQuota
argument_list|(
name|req
argument_list|,
operator|new
name|SetQuotaOperations
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Quotas
name|fetch
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|getTableQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|addTableQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
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
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|deleteTableQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preSetTableQuota
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
name|postApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postSetTableQuota
argument_list|(
name|table
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setNamespaceQuota
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|SetQuotaRequest
name|req
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|setQuota
argument_list|(
name|req
argument_list|,
operator|new
name|SetQuotaOperations
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Quotas
name|fetch
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|QuotaUtil
operator|.
name|getNamespaceQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|addNamespaceQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
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
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaUtil
operator|.
name|deleteNamespaceQuota
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|preSetNamespaceQuota
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
name|postApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
block|{
name|masterServices
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|postSetNamespaceQuota
argument_list|(
name|namespace
argument_list|,
name|quotas
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setQuota
parameter_list|(
specifier|final
name|SetQuotaRequest
name|req
parameter_list|,
specifier|final
name|SetQuotaOperations
name|quotaOps
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|req
operator|.
name|hasRemoveAll
argument_list|()
operator|&&
name|req
operator|.
name|getRemoveAll
argument_list|()
operator|==
literal|true
condition|)
block|{
name|quotaOps
operator|.
name|preApply
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|quotaOps
operator|.
name|delete
argument_list|()
expr_stmt|;
name|quotaOps
operator|.
name|postApply
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Apply quota changes
name|Quotas
name|quotas
init|=
name|quotaOps
operator|.
name|fetch
argument_list|()
decl_stmt|;
name|quotaOps
operator|.
name|preApply
argument_list|(
name|quotas
argument_list|)
expr_stmt|;
name|Quotas
operator|.
name|Builder
name|builder
init|=
operator|(
name|quotas
operator|!=
literal|null
operator|)
condition|?
name|quotas
operator|.
name|toBuilder
argument_list|()
else|:
name|Quotas
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|req
operator|.
name|hasThrottle
argument_list|()
condition|)
name|applyThrottle
argument_list|(
name|builder
argument_list|,
name|req
operator|.
name|getThrottle
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|req
operator|.
name|hasBypassGlobals
argument_list|()
condition|)
name|applyBypassGlobals
argument_list|(
name|builder
argument_list|,
name|req
operator|.
name|getBypassGlobals
argument_list|()
argument_list|)
expr_stmt|;
comment|// Submit new changes
name|quotas
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
if|if
condition|(
name|QuotaUtil
operator|.
name|isEmptyQuota
argument_list|(
name|quotas
argument_list|)
condition|)
block|{
name|quotaOps
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|quotaOps
operator|.
name|update
argument_list|(
name|quotas
argument_list|)
expr_stmt|;
block|}
name|quotaOps
operator|.
name|postApply
argument_list|(
name|quotas
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
interface|interface
name|SetQuotaOperations
block|{
name|Quotas
name|fetch
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|delete
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|update
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|preApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|postApply
parameter_list|(
specifier|final
name|Quotas
name|quotas
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/* ==========================================================================    *  Helpers to apply changes to the quotas    */
specifier|private
name|void
name|applyThrottle
parameter_list|(
specifier|final
name|Quotas
operator|.
name|Builder
name|quotas
parameter_list|,
specifier|final
name|ThrottleRequest
name|req
parameter_list|)
throws|throws
name|IOException
block|{
name|Throttle
operator|.
name|Builder
name|throttle
decl_stmt|;
if|if
condition|(
name|req
operator|.
name|hasType
argument_list|()
operator|&&
operator|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
operator|||
name|quotas
operator|.
name|hasThrottle
argument_list|()
operator|)
condition|)
block|{
comment|// Validate timed quota if present
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
name|validateTimedQuota
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
comment|// apply the new settings
name|throttle
operator|=
name|quotas
operator|.
name|hasThrottle
argument_list|()
condition|?
name|quotas
operator|.
name|getThrottle
argument_list|()
operator|.
name|toBuilder
argument_list|()
else|:
name|Throttle
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|req
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|REQUEST_NUMBER
case|:
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttle
operator|.
name|setReqNum
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttle
operator|.
name|clearReqNum
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|REQUEST_SIZE
case|:
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttle
operator|.
name|setReqSize
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttle
operator|.
name|clearReqSize
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|WRITE_NUMBER
case|:
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttle
operator|.
name|setWriteNum
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttle
operator|.
name|clearWriteNum
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|WRITE_SIZE
case|:
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttle
operator|.
name|setWriteSize
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttle
operator|.
name|clearWriteSize
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|READ_NUMBER
case|:
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttle
operator|.
name|setReadNum
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttle
operator|.
name|clearReqNum
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|READ_SIZE
case|:
if|if
condition|(
name|req
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttle
operator|.
name|setReadSize
argument_list|(
name|req
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttle
operator|.
name|clearReadSize
argument_list|()
expr_stmt|;
block|}
break|break;
block|}
name|quotas
operator|.
name|setThrottle
argument_list|(
name|throttle
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|quotas
operator|.
name|clearThrottle
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|applyBypassGlobals
parameter_list|(
specifier|final
name|Quotas
operator|.
name|Builder
name|quotas
parameter_list|,
name|boolean
name|bypassGlobals
parameter_list|)
block|{
if|if
condition|(
name|bypassGlobals
condition|)
block|{
name|quotas
operator|.
name|setBypassGlobals
argument_list|(
name|bypassGlobals
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|quotas
operator|.
name|clearBypassGlobals
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|validateTimedQuota
parameter_list|(
specifier|final
name|TimedQuota
name|timedQuota
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|timedQuota
operator|.
name|getSoftLimit
argument_list|()
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The throttle limit must be greater then 0, got "
operator|+
name|timedQuota
operator|.
name|getSoftLimit
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/* ==========================================================================    *  Helpers    */
specifier|private
name|void
name|checkQuotaSupport
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"quota support disabled"
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|createQuotaTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegionInfo
name|newRegions
index|[]
init|=
operator|new
name|HRegionInfo
index|[]
block|{
operator|new
name|HRegionInfo
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
block|}
decl_stmt|;
name|masterServices
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
operator|new
name|CreateTableHandler
argument_list|(
name|masterServices
argument_list|,
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|,
name|QuotaUtil
operator|.
name|QUOTA_TABLE_DESC
argument_list|,
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|newRegions
argument_list|,
name|masterServices
argument_list|)
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|NamedLock
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
name|HashSet
argument_list|<
name|T
argument_list|>
name|locks
init|=
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|lock
parameter_list|(
specifier|final
name|T
name|name
parameter_list|)
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|locks
init|)
block|{
while|while
condition|(
name|locks
operator|.
name|contains
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|locks
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
name|locks
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|unlock
parameter_list|(
specifier|final
name|T
name|name
parameter_list|)
block|{
synchronized|synchronized
init|(
name|locks
init|)
block|{
name|locks
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|locks
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

