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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|ipc
operator|.
name|RpcScheduler
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
name|ipc
operator|.
name|RpcServer
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
name|ClientProtos
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
name|Region
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
name|RegionServerServices
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
name|security
operator|.
name|UserGroupInformation
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Region Server Quota Manager.  * It is responsible to provide access to the quota information of each user/table.  *  * The direct user of this class is the RegionServer that will get and check the  * user/table quota for each operation (put, get, scan).  * For system tables and user/table with a quota specified, the quota check will be a noop.  */
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
name|RegionServerRpcQuotaManager
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
name|RegionServerRpcQuotaManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
name|QuotaCache
name|quotaCache
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|rpcThrottleEnabled
decl_stmt|;
comment|// Storage for quota rpc throttle
specifier|private
name|RpcThrottleStorage
name|rpcThrottleStorage
decl_stmt|;
specifier|public
name|RegionServerRpcQuotaManager
parameter_list|(
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|rpcThrottleStorage
operator|=
operator|new
name|RpcThrottleStorage
argument_list|(
name|rsServices
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|rsServices
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|(
specifier|final
name|RpcScheduler
name|rpcScheduler
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|QuotaUtil
operator|.
name|isQuotaEnabled
argument_list|(
name|rsServices
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing RPC quota support"
argument_list|)
expr_stmt|;
comment|// Initialize quota cache
name|quotaCache
operator|=
operator|new
name|QuotaCache
argument_list|(
name|rsServices
argument_list|)
expr_stmt|;
name|quotaCache
operator|.
name|start
argument_list|()
expr_stmt|;
name|rpcThrottleEnabled
operator|=
name|rpcThrottleStorage
operator|.
name|isRpcThrottleEnabled
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start rpc quota manager and rpc throttle enabled is {}"
argument_list|,
name|rpcThrottleEnabled
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|isQuotaEnabled
argument_list|()
condition|)
block|{
name|quotaCache
operator|.
name|stop
argument_list|(
literal|"shutdown"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|boolean
name|isRpcThrottleEnabled
parameter_list|()
block|{
return|return
name|rpcThrottleEnabled
return|;
block|}
specifier|private
name|boolean
name|isQuotaEnabled
parameter_list|()
block|{
return|return
name|quotaCache
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|switchRpcThrottle
parameter_list|(
name|boolean
name|enable
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isQuotaEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|rpcThrottleEnabled
operator|!=
name|enable
condition|)
block|{
name|boolean
name|previousEnabled
init|=
name|rpcThrottleEnabled
decl_stmt|;
name|rpcThrottleEnabled
operator|=
name|rpcThrottleStorage
operator|.
name|isRpcThrottleEnabled
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Switch rpc throttle from {} to {}"
argument_list|,
name|previousEnabled
argument_list|,
name|rpcThrottleEnabled
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skip switch rpc throttle because previous value {} is the same as current value {}"
argument_list|,
name|rpcThrottleEnabled
argument_list|,
name|enable
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skip switch rpc throttle to {} because rpc quota is disabled"
argument_list|,
name|enable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
name|QuotaCache
name|getQuotaCache
parameter_list|()
block|{
return|return
name|quotaCache
return|;
block|}
comment|/**    * Returns the quota for an operation.    *    * @param ugi the user that is executing the operation    * @param table the table where the operation will be executed    * @return the OperationQuota    */
specifier|public
name|OperationQuota
name|getQuota
parameter_list|(
specifier|final
name|UserGroupInformation
name|ugi
parameter_list|,
specifier|final
name|TableName
name|table
parameter_list|)
block|{
if|if
condition|(
name|isQuotaEnabled
argument_list|()
operator|&&
operator|!
name|table
operator|.
name|isSystemTable
argument_list|()
operator|&&
name|isRpcThrottleEnabled
argument_list|()
condition|)
block|{
name|UserQuotaState
name|userQuotaState
init|=
name|quotaCache
operator|.
name|getUserQuotaState
argument_list|(
name|ugi
argument_list|)
decl_stmt|;
name|QuotaLimiter
name|userLimiter
init|=
name|userQuotaState
operator|.
name|getTableLimiter
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|boolean
name|useNoop
init|=
name|userLimiter
operator|.
name|isBypass
argument_list|()
decl_stmt|;
if|if
condition|(
name|userQuotaState
operator|.
name|hasBypassGlobals
argument_list|()
condition|)
block|{
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
literal|"get quota for ugi="
operator|+
name|ugi
operator|+
literal|" table="
operator|+
name|table
operator|+
literal|" userLimiter="
operator|+
name|userLimiter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|useNoop
condition|)
block|{
return|return
operator|new
name|DefaultOperationQuota
argument_list|(
name|this
operator|.
name|rsServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|userLimiter
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|QuotaLimiter
name|nsLimiter
init|=
name|quotaCache
operator|.
name|getNamespaceLimiter
argument_list|(
name|table
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
decl_stmt|;
name|QuotaLimiter
name|tableLimiter
init|=
name|quotaCache
operator|.
name|getTableLimiter
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|useNoop
operator|&=
name|tableLimiter
operator|.
name|isBypass
argument_list|()
operator|&&
name|nsLimiter
operator|.
name|isBypass
argument_list|()
expr_stmt|;
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
literal|"get quota for ugi="
operator|+
name|ugi
operator|+
literal|" table="
operator|+
name|table
operator|+
literal|" userLimiter="
operator|+
name|userLimiter
operator|+
literal|" tableLimiter="
operator|+
name|tableLimiter
operator|+
literal|" nsLimiter="
operator|+
name|nsLimiter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|useNoop
condition|)
block|{
return|return
operator|new
name|DefaultOperationQuota
argument_list|(
name|this
operator|.
name|rsServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|userLimiter
argument_list|,
name|tableLimiter
argument_list|,
name|nsLimiter
argument_list|)
return|;
block|}
block|}
block|}
return|return
name|NoopOperationQuota
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Check the quota for the current (rpc-context) user.    * Returns the OperationQuota used to get the available quota and    * to report the data/usage of the operation.    * @param region the region where the operation will be performed    * @param type the operation type    * @return the OperationQuota    * @throws RpcThrottlingException if the operation cannot be executed due to quota exceeded.    */
specifier|public
name|OperationQuota
name|checkQuota
parameter_list|(
specifier|final
name|Region
name|region
parameter_list|,
specifier|final
name|OperationQuota
operator|.
name|OperationType
name|type
parameter_list|)
throws|throws
name|IOException
throws|,
name|RpcThrottlingException
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|SCAN
case|:
return|return
name|checkQuota
argument_list|(
name|region
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
return|;
case|case
name|GET
case|:
return|return
name|checkQuota
argument_list|(
name|region
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
return|;
case|case
name|MUTATE
case|:
return|return
name|checkQuota
argument_list|(
name|region
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid operation type: "
operator|+
name|type
argument_list|)
throw|;
block|}
comment|/**    * Check the quota for the current (rpc-context) user.    * Returns the OperationQuota used to get the available quota and    * to report the data/usage of the operation.    * @param region the region where the operation will be performed    * @param actions the "multi" actions to perform    * @return the OperationQuota    * @throws RpcThrottlingException if the operation cannot be executed due to quota exceeded.    */
specifier|public
name|OperationQuota
name|checkQuota
parameter_list|(
specifier|final
name|Region
name|region
parameter_list|,
specifier|final
name|List
argument_list|<
name|ClientProtos
operator|.
name|Action
argument_list|>
name|actions
parameter_list|)
throws|throws
name|IOException
throws|,
name|RpcThrottlingException
block|{
name|int
name|numWrites
init|=
literal|0
decl_stmt|;
name|int
name|numReads
init|=
literal|0
decl_stmt|;
for|for
control|(
specifier|final
name|ClientProtos
operator|.
name|Action
name|action
range|:
name|actions
control|)
block|{
if|if
condition|(
name|action
operator|.
name|hasMutation
argument_list|()
condition|)
block|{
name|numWrites
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|action
operator|.
name|hasGet
argument_list|()
condition|)
block|{
name|numReads
operator|++
expr_stmt|;
block|}
block|}
return|return
name|checkQuota
argument_list|(
name|region
argument_list|,
name|numWrites
argument_list|,
name|numReads
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|/**    * Check the quota for the current (rpc-context) user.    * Returns the OperationQuota used to get the available quota and    * to report the data/usage of the operation.    * @param region the region where the operation will be performed    * @param numWrites number of writes to perform    * @param numReads number of short-reads to perform    * @param numScans number of scan to perform    * @return the OperationQuota    * @throws RpcThrottlingException if the operation cannot be executed due to quota exceeded.    */
specifier|private
name|OperationQuota
name|checkQuota
parameter_list|(
specifier|final
name|Region
name|region
parameter_list|,
specifier|final
name|int
name|numWrites
parameter_list|,
specifier|final
name|int
name|numReads
parameter_list|,
specifier|final
name|int
name|numScans
parameter_list|)
throws|throws
name|IOException
throws|,
name|RpcThrottlingException
block|{
name|Optional
argument_list|<
name|User
argument_list|>
name|user
init|=
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
decl_stmt|;
name|UserGroupInformation
name|ugi
decl_stmt|;
if|if
condition|(
name|user
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|ugi
operator|=
name|user
operator|.
name|get
argument_list|()
operator|.
name|getUGI
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|ugi
operator|=
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getUGI
argument_list|()
expr_stmt|;
block|}
name|TableName
name|table
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|OperationQuota
name|quota
init|=
name|getQuota
argument_list|(
name|ugi
argument_list|,
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|quota
operator|.
name|checkQuota
argument_list|(
name|numWrites
argument_list|,
name|numReads
argument_list|,
name|numScans
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RpcThrottlingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Throttling exception for user="
operator|+
name|ugi
operator|.
name|getUserName
argument_list|()
operator|+
literal|" table="
operator|+
name|table
operator|+
literal|" numWrites="
operator|+
name|numWrites
operator|+
literal|" numReads="
operator|+
name|numReads
operator|+
literal|" numScans="
operator|+
name|numScans
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|quota
return|;
block|}
block|}
end_class

end_unit

