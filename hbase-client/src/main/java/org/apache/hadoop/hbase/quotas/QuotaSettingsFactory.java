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
name|List
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
name|shaded
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
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
name|SpaceQuota
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|QuotaSettingsFactory
block|{
specifier|static
class|class
name|QuotaGlobalsSettingsBypass
extends|extends
name|QuotaSettings
block|{
specifier|private
specifier|final
name|boolean
name|bypassGlobals
decl_stmt|;
name|QuotaGlobalsSettingsBypass
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|boolean
name|bypassGlobals
parameter_list|)
block|{
name|super
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|)
expr_stmt|;
name|this
operator|.
name|bypassGlobals
operator|=
name|bypassGlobals
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|QuotaType
name|getQuotaType
parameter_list|()
block|{
return|return
name|QuotaType
operator|.
name|GLOBAL_BYPASS
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setupSetQuotaRequest
parameter_list|(
name|SetQuotaRequest
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|builder
operator|.
name|setBypassGlobals
argument_list|(
name|bypassGlobals
argument_list|)
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
literal|"GLOBAL_BYPASS => "
operator|+
name|bypassGlobals
return|;
block|}
block|}
comment|/* ==========================================================================    *  QuotaSettings from the Quotas object    */
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromUserQuotas
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
return|return
name|fromQuotas
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|quotas
argument_list|)
return|;
block|}
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromUserQuotas
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
return|return
name|fromQuotas
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|quotas
argument_list|)
return|;
block|}
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromUserQuotas
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
name|Quotas
name|quotas
parameter_list|)
block|{
return|return
name|fromQuotas
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
name|namespace
argument_list|,
name|quotas
argument_list|)
return|;
block|}
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromTableQuotas
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
return|return
name|fromQuotas
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|quotas
argument_list|)
return|;
block|}
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromNamespaceQuotas
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
return|return
name|fromQuotas
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|namespace
argument_list|,
name|quotas
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromQuotas
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|Quotas
name|quotas
parameter_list|)
block|{
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|settings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|quotas
operator|.
name|hasThrottle
argument_list|()
condition|)
block|{
name|settings
operator|.
name|addAll
argument_list|(
name|fromThrottle
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|quotas
operator|.
name|getThrottle
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|quotas
operator|.
name|getBypassGlobals
argument_list|()
operator|==
literal|true
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|quotas
operator|.
name|hasSpace
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|fromSpace
argument_list|(
name|tableName
argument_list|,
name|namespace
argument_list|,
name|quotas
operator|.
name|getSpace
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|settings
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|fromThrottle
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|QuotaProtos
operator|.
name|Throttle
name|throttle
parameter_list|)
block|{
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|settings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|throttle
operator|.
name|hasReqNum
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|ThrottleSettings
operator|.
name|fromTimedQuota
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
name|throttle
operator|.
name|getReqNum
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasReqSize
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|ThrottleSettings
operator|.
name|fromTimedQuota
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|ThrottleType
operator|.
name|REQUEST_SIZE
argument_list|,
name|throttle
operator|.
name|getReqSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasWriteNum
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|ThrottleSettings
operator|.
name|fromTimedQuota
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|ThrottleType
operator|.
name|WRITE_NUMBER
argument_list|,
name|throttle
operator|.
name|getWriteNum
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasWriteSize
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|ThrottleSettings
operator|.
name|fromTimedQuota
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|ThrottleType
operator|.
name|WRITE_SIZE
argument_list|,
name|throttle
operator|.
name|getWriteSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasReadNum
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|ThrottleSettings
operator|.
name|fromTimedQuota
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|ThrottleType
operator|.
name|READ_NUMBER
argument_list|,
name|throttle
operator|.
name|getReadNum
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasReadSize
argument_list|()
condition|)
block|{
name|settings
operator|.
name|add
argument_list|(
name|ThrottleSettings
operator|.
name|fromTimedQuota
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|ThrottleType
operator|.
name|READ_SIZE
argument_list|,
name|throttle
operator|.
name|getReadSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|settings
return|;
block|}
specifier|static
name|QuotaSettings
name|fromSpace
parameter_list|(
name|TableName
name|table
parameter_list|,
name|String
name|namespace
parameter_list|,
name|SpaceQuota
name|protoQuota
parameter_list|)
block|{
if|if
condition|(
operator|(
name|table
operator|==
literal|null
operator|&&
name|namespace
operator|==
literal|null
operator|)
operator|||
operator|(
name|table
operator|!=
literal|null
operator|&&
name|namespace
operator|!=
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can only construct SpaceLimitSettings for a table or namespace."
argument_list|)
throw|;
block|}
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
return|return
name|SpaceLimitSettings
operator|.
name|fromSpaceQuota
argument_list|(
name|table
argument_list|,
name|protoQuota
argument_list|)
return|;
block|}
else|else
block|{
comment|// namespace must be non-null
return|return
name|SpaceLimitSettings
operator|.
name|fromSpaceQuota
argument_list|(
name|namespace
argument_list|,
name|protoQuota
argument_list|)
return|;
block|}
block|}
comment|/* ==========================================================================    *  RPC Throttle    */
comment|/**    * Throttle the specified user.    *    * @param userName the user to throttle    * @param type the type of throttling    * @param limit the allowed number of request/data per timeUnit    * @param timeUnit the limit time unit    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|throttleUser
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|ThrottleType
name|type
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|limit
argument_list|,
name|timeUnit
argument_list|)
return|;
block|}
comment|/**    * Throttle the specified user on the specified table.    *    * @param userName the user to throttle    * @param tableName the table to throttle    * @param type the type of throttling    * @param limit the allowed number of request/data per timeUnit    * @param timeUnit the limit time unit    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|throttleUser
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ThrottleType
name|type
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|limit
argument_list|,
name|timeUnit
argument_list|)
return|;
block|}
comment|/**    * Throttle the specified user on the specified namespace.    *    * @param userName the user to throttle    * @param namespace the namespace to throttle    * @param type the type of throttling    * @param limit the allowed number of request/data per timeUnit    * @param timeUnit the limit time unit    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|throttleUser
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
name|ThrottleType
name|type
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
name|namespace
argument_list|,
name|type
argument_list|,
name|limit
argument_list|,
name|timeUnit
argument_list|)
return|;
block|}
comment|/**    * Remove the throttling for the specified user.    *    * @param userName the user    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|unthrottleUser
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Remove the throttling for the specified user on the specified table.    *    * @param userName the user    * @param tableName the table    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|unthrottleUser
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Remove the throttling for the specified user on the specified namespace.    *    * @param userName the user    * @param namespace the namespace    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|unthrottleUser
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
name|namespace
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Throttle the specified table.    *    * @param tableName the table to throttle    * @param type the type of throttling    * @param limit the allowed number of request/data per timeUnit    * @param timeUnit the limit time unit    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|throttleTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ThrottleType
name|type
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|type
argument_list|,
name|limit
argument_list|,
name|timeUnit
argument_list|)
return|;
block|}
comment|/**    * Remove the throttling for the specified table.    *    * @param tableName the table    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|unthrottleTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Throttle the specified namespace.    *    * @param namespace the namespace to throttle    * @param type the type of throttling    * @param limit the allowed number of request/data per timeUnit    * @param timeUnit the limit time unit    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|throttleNamespace
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|ThrottleType
name|type
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|namespace
argument_list|,
name|type
argument_list|,
name|limit
argument_list|,
name|timeUnit
argument_list|)
return|;
block|}
comment|/**    * Remove the throttling for the specified namespace.    *    * @param namespace the namespace    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|unthrottleNamespace
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|)
block|{
return|return
name|throttle
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|namespace
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/* Throttle helper */
specifier|private
specifier|static
name|QuotaSettings
name|throttle
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|ThrottleType
name|type
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|,
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
name|QuotaProtos
operator|.
name|ThrottleRequest
operator|.
name|Builder
name|builder
init|=
name|QuotaProtos
operator|.
name|ThrottleRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setType
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoThrottleType
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timeUnit
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setTimedQuota
argument_list|(
name|ProtobufUtil
operator|.
name|toTimedQuota
argument_list|(
name|limit
argument_list|,
name|timeUnit
argument_list|,
name|QuotaScope
operator|.
name|MACHINE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ThrottleSettings
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
comment|/* ==========================================================================    *  Global Settings    */
comment|/**    * Set the "bypass global settings" for the specified user    *    * @param userName the user to throttle    * @param bypassGlobals true if the global settings should be bypassed    * @return the quota settings    */
specifier|public
specifier|static
name|QuotaSettings
name|bypassGlobals
parameter_list|(
specifier|final
name|String
name|userName
parameter_list|,
specifier|final
name|boolean
name|bypassGlobals
parameter_list|)
block|{
return|return
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
name|userName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|bypassGlobals
argument_list|)
return|;
block|}
comment|/* ==========================================================================    *  FileSystem Space Settings    */
comment|/**    * Creates a {@link QuotaSettings} object to limit the FileSystem space usage for the given table    * to the given size in bytes. When the space usage is exceeded by the table, the provided    * {@link SpaceViolationPolicy} is enacted on the table.    *    * @param tableName The name of the table on which the quota should be applied.    * @param sizeLimit The limit of a table's size in bytes.    * @param violationPolicy The action to take when the quota is exceeded.    * @return An {@link QuotaSettings} object.    */
specifier|public
specifier|static
name|QuotaSettings
name|limitTableSpace
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
name|long
name|sizeLimit
parameter_list|,
specifier|final
name|SpaceViolationPolicy
name|violationPolicy
parameter_list|)
block|{
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|tableName
argument_list|,
name|sizeLimit
argument_list|,
name|violationPolicy
argument_list|)
return|;
block|}
comment|/**    * Creates a {@link QuotaSettings} object to remove the FileSystem space quota for the given    * table.    *    * @param tableName The name of the table to remove the quota for.    * @return A {@link QuotaSettings} object.    */
specifier|public
specifier|static
name|QuotaSettings
name|removeTableSpaceLimit
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|tableName
argument_list|)
return|;
block|}
comment|/**    * Creates a {@link QuotaSettings} object to limit the FileSystem space usage for the given    * namespace to the given size in bytes. When the space usage is exceeded by all tables in the    * namespace, the provided {@link SpaceViolationPolicy} is enacted on all tables in the namespace.    *    * @param namespace The namespace on which the quota should be applied.    * @param sizeLimit The limit of the namespace's size in bytes.    * @param violationPolicy The action to take when the the quota is exceeded.    * @return An {@link QuotaSettings} object.    */
specifier|public
specifier|static
name|QuotaSettings
name|limitNamespaceSpace
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
name|long
name|sizeLimit
parameter_list|,
specifier|final
name|SpaceViolationPolicy
name|violationPolicy
parameter_list|)
block|{
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|namespace
argument_list|,
name|sizeLimit
argument_list|,
name|violationPolicy
argument_list|)
return|;
block|}
comment|/**    * Creates a {@link QuotaSettings} object to remove the FileSystem space quota for the given    * namespace.    *    * @param namespace The namespace to remove the quota on.    * @return A {@link QuotaSettings} object.    */
specifier|public
specifier|static
name|QuotaSettings
name|removeNamespaceSpaceLimit
parameter_list|(
name|String
name|namespace
parameter_list|)
block|{
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|namespace
argument_list|)
return|;
block|}
block|}
end_class

end_unit

