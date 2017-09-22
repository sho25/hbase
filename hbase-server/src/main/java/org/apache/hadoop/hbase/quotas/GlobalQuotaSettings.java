begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|HBaseInterfaceAudience
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
name|quotas
operator|.
name|QuotaSettingsFactory
operator|.
name|QuotaGlobalsSettingsBypass
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
operator|.
name|Builder
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
name|shaded
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

begin_comment
comment|/**  * An object which captures all quotas types (throttle or space) for a subject (user, table, or  * namespace). This is used inside of the HBase RegionServer to act as an analogy to the  * ProtocolBuffer class {@link Quotas}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|GlobalQuotaSettings
extends|extends
name|QuotaSettings
block|{
specifier|private
specifier|final
name|QuotaProtos
operator|.
name|Throttle
name|throttleProto
decl_stmt|;
specifier|private
specifier|final
name|Boolean
name|bypassGlobals
decl_stmt|;
specifier|private
specifier|final
name|QuotaProtos
operator|.
name|SpaceQuota
name|spaceProto
decl_stmt|;
specifier|protected
name|GlobalQuotaSettings
parameter_list|(
name|String
name|username
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|namespace
parameter_list|,
name|QuotaProtos
operator|.
name|Quotas
name|quotas
parameter_list|)
block|{
name|this
argument_list|(
name|username
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
operator|(
name|quotas
operator|!=
literal|null
operator|&&
name|quotas
operator|.
name|hasThrottle
argument_list|()
condition|?
name|quotas
operator|.
name|getThrottle
argument_list|()
else|:
literal|null
operator|)
argument_list|,
operator|(
name|quotas
operator|!=
literal|null
operator|&&
name|quotas
operator|.
name|hasBypassGlobals
argument_list|()
condition|?
name|quotas
operator|.
name|getBypassGlobals
argument_list|()
else|:
literal|null
operator|)
argument_list|,
operator|(
name|quotas
operator|!=
literal|null
operator|&&
name|quotas
operator|.
name|hasSpace
argument_list|()
condition|?
name|quotas
operator|.
name|getSpace
argument_list|()
else|:
literal|null
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|GlobalQuotaSettings
parameter_list|(
name|String
name|userName
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|namespace
parameter_list|,
name|QuotaProtos
operator|.
name|Throttle
name|throttleProto
parameter_list|,
name|Boolean
name|bypassGlobals
parameter_list|,
name|QuotaProtos
operator|.
name|SpaceQuota
name|spaceProto
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
name|throttleProto
operator|=
name|throttleProto
expr_stmt|;
name|this
operator|.
name|bypassGlobals
operator|=
name|bypassGlobals
expr_stmt|;
name|this
operator|.
name|spaceProto
operator|=
name|spaceProto
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|QuotaType
name|getQuotaType
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setupSetQuotaRequest
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
comment|// ThrottleSettings should be used instead for setting a throttle quota.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This class should not be used to generate a SetQuotaRequest."
argument_list|)
throw|;
block|}
specifier|protected
name|QuotaProtos
operator|.
name|Throttle
name|getThrottleProto
parameter_list|()
block|{
return|return
name|this
operator|.
name|throttleProto
return|;
block|}
specifier|protected
name|Boolean
name|getGlobalBypass
parameter_list|()
block|{
return|return
name|this
operator|.
name|bypassGlobals
return|;
block|}
specifier|protected
name|QuotaProtos
operator|.
name|SpaceQuota
name|getSpaceProto
parameter_list|()
block|{
return|return
name|this
operator|.
name|spaceProto
return|;
block|}
comment|/**    * Constructs a new {@link Quotas} message from {@code this}.    */
specifier|protected
name|Quotas
name|toQuotas
parameter_list|()
block|{
name|QuotaProtos
operator|.
name|Quotas
operator|.
name|Builder
name|builder
init|=
name|QuotaProtos
operator|.
name|Quotas
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|getThrottleProto
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setThrottle
argument_list|(
name|getThrottleProto
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getGlobalBypass
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setBypassGlobals
argument_list|(
name|getGlobalBypass
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getSpaceProto
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setSpace
argument_list|(
name|getSpaceProto
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|GlobalQuotaSettings
name|merge
parameter_list|(
name|QuotaSettings
name|other
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Validate the quota subject
name|validateQuotaTarget
argument_list|(
name|other
argument_list|)
expr_stmt|;
comment|// Propagate the Throttle
name|QuotaProtos
operator|.
name|Throttle
operator|.
name|Builder
name|throttleBuilder
init|=
operator|(
name|throttleProto
operator|==
literal|null
condition|?
literal|null
else|:
name|throttleProto
operator|.
name|toBuilder
argument_list|()
operator|)
decl_stmt|;
if|if
condition|(
name|other
operator|instanceof
name|ThrottleSettings
condition|)
block|{
if|if
condition|(
name|throttleBuilder
operator|==
literal|null
condition|)
block|{
name|throttleBuilder
operator|=
name|QuotaProtos
operator|.
name|Throttle
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
block|}
name|ThrottleSettings
name|otherThrottle
init|=
operator|(
name|ThrottleSettings
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|otherThrottle
operator|.
name|proto
operator|.
name|hasType
argument_list|()
condition|)
block|{
name|QuotaProtos
operator|.
name|ThrottleRequest
name|otherProto
init|=
name|otherThrottle
operator|.
name|proto
decl_stmt|;
if|if
condition|(
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
if|if
condition|(
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|validateTimedQuota
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
name|otherProto
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
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttleBuilder
operator|.
name|setReqNum
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttleBuilder
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
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttleBuilder
operator|.
name|setReqSize
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttleBuilder
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
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttleBuilder
operator|.
name|setWriteNum
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttleBuilder
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
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttleBuilder
operator|.
name|setWriteSize
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttleBuilder
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
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttleBuilder
operator|.
name|setReadNum
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttleBuilder
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
name|otherProto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|throttleBuilder
operator|.
name|setReadSize
argument_list|(
name|otherProto
operator|.
name|getTimedQuota
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|throttleBuilder
operator|.
name|clearReadSize
argument_list|()
expr_stmt|;
block|}
break|break;
block|}
block|}
else|else
block|{
name|clearThrottleBuilder
argument_list|(
name|throttleBuilder
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|clearThrottleBuilder
argument_list|(
name|throttleBuilder
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Propagate the space quota portion
name|QuotaProtos
operator|.
name|SpaceQuota
operator|.
name|Builder
name|spaceBuilder
init|=
operator|(
name|spaceProto
operator|==
literal|null
condition|?
literal|null
else|:
name|spaceProto
operator|.
name|toBuilder
argument_list|()
operator|)
decl_stmt|;
if|if
condition|(
name|other
operator|instanceof
name|SpaceLimitSettings
condition|)
block|{
if|if
condition|(
name|spaceBuilder
operator|==
literal|null
condition|)
block|{
name|spaceBuilder
operator|=
name|QuotaProtos
operator|.
name|SpaceQuota
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
block|}
name|SpaceLimitSettings
name|settingsToMerge
init|=
operator|(
name|SpaceLimitSettings
operator|)
name|other
decl_stmt|;
name|QuotaProtos
operator|.
name|SpaceLimitRequest
name|spaceRequest
init|=
name|settingsToMerge
operator|.
name|getProto
argument_list|()
decl_stmt|;
comment|// The message contained the expect SpaceQuota object
if|if
condition|(
name|spaceRequest
operator|.
name|hasQuota
argument_list|()
condition|)
block|{
name|SpaceQuota
name|quotaToMerge
init|=
name|spaceRequest
operator|.
name|getQuota
argument_list|()
decl_stmt|;
comment|// Validate that the two settings are for the same target.
comment|// SpaceQuotas either apply to a table or a namespace (no user spacequota).
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|settingsToMerge
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|&&
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|getNamespace
argument_list|()
argument_list|,
name|settingsToMerge
operator|.
name|getNamespace
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot merge "
operator|+
name|settingsToMerge
operator|+
literal|" into "
operator|+
name|this
argument_list|)
throw|;
block|}
if|if
condition|(
name|quotaToMerge
operator|.
name|getRemove
argument_list|()
condition|)
block|{
comment|// Update the builder to propagate the removal
name|spaceBuilder
operator|.
name|setRemove
argument_list|(
literal|true
argument_list|)
operator|.
name|clearSoftLimit
argument_list|()
operator|.
name|clearViolationPolicy
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Add the new settings to the existing settings
name|spaceBuilder
operator|.
name|mergeFrom
argument_list|(
name|quotaToMerge
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Boolean
name|bypassGlobals
init|=
name|this
operator|.
name|bypassGlobals
decl_stmt|;
if|if
condition|(
name|other
operator|instanceof
name|QuotaGlobalsSettingsBypass
condition|)
block|{
name|bypassGlobals
operator|=
operator|(
operator|(
name|QuotaGlobalsSettingsBypass
operator|)
name|other
operator|)
operator|.
name|getBypass
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|throttleBuilder
operator|==
literal|null
operator|&&
operator|(
name|spaceBuilder
operator|==
literal|null
operator|||
operator|(
name|spaceBuilder
operator|.
name|hasRemove
argument_list|()
operator|&&
name|spaceBuilder
operator|.
name|getRemove
argument_list|()
operator|)
operator|)
operator|&&
name|bypassGlobals
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|GlobalQuotaSettings
argument_list|(
name|getUserName
argument_list|()
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getNamespace
argument_list|()
argument_list|,
operator|(
name|throttleBuilder
operator|==
literal|null
condition|?
literal|null
else|:
name|throttleBuilder
operator|.
name|build
argument_list|()
operator|)
argument_list|,
name|bypassGlobals
argument_list|,
operator|(
name|spaceBuilder
operator|==
literal|null
condition|?
literal|null
else|:
name|spaceBuilder
operator|.
name|build
argument_list|()
operator|)
argument_list|)
return|;
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"GlobalQuota: "
argument_list|)
expr_stmt|;
if|if
condition|(
name|throttleProto
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|ThrottleType
argument_list|,
name|TimedQuota
argument_list|>
name|throttleQuotas
init|=
name|buildThrottleQuotas
argument_list|(
name|throttleProto
argument_list|)
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|" { TYPE => THROTTLE "
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|ThrottleType
argument_list|,
name|TimedQuota
argument_list|>
name|entry
range|:
name|throttleQuotas
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|ThrottleType
name|type
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
specifier|final
name|TimedQuota
name|timedQuota
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"{THROTTLE_TYPE => "
argument_list|)
operator|.
name|append
argument_list|(
name|type
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", LIMIT => "
argument_list|)
expr_stmt|;
if|if
condition|(
name|timedQuota
operator|.
name|hasSoftLimit
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|REQUEST_NUMBER
case|:
case|case
name|WRITE_NUMBER
case|:
case|case
name|READ_NUMBER
case|:
name|builder
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%dreq"
argument_list|,
name|timedQuota
operator|.
name|getSoftLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|REQUEST_SIZE
case|:
case|case
name|WRITE_SIZE
case|:
case|case
name|READ_SIZE
case|:
name|builder
operator|.
name|append
argument_list|(
name|sizeToString
argument_list|(
name|timedQuota
operator|.
name|getSoftLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
elseif|else
if|if
condition|(
name|timedQuota
operator|.
name|hasShare
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%.2f%%"
argument_list|,
name|timedQuota
operator|.
name|getShare
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|timeToString
argument_list|(
name|ProtobufUtil
operator|.
name|toTimeUnit
argument_list|(
name|timedQuota
operator|.
name|getTimeUnit
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|timedQuota
operator|.
name|hasScope
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", SCOPE => "
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|timedQuota
operator|.
name|getScope
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"} } "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" {} "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bypassGlobals
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" { GLOBAL_BYPASS => "
operator|+
name|bypassGlobals
operator|+
literal|" } "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|spaceProto
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" { TYPE => SPACE"
argument_list|)
expr_stmt|;
if|if
condition|(
name|getTableName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", TABLE => "
argument_list|)
operator|.
name|append
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getNamespace
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", NAMESPACE => "
argument_list|)
operator|.
name|append
argument_list|(
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|spaceProto
operator|.
name|getRemove
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", REMOVE => "
argument_list|)
operator|.
name|append
argument_list|(
name|spaceProto
operator|.
name|getRemove
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", LIMIT => "
argument_list|)
operator|.
name|append
argument_list|(
name|spaceProto
operator|.
name|getSoftLimit
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|", VIOLATION_POLICY => "
argument_list|)
operator|.
name|append
argument_list|(
name|spaceProto
operator|.
name|getViolationPolicy
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|" } "
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|Map
argument_list|<
name|ThrottleType
argument_list|,
name|TimedQuota
argument_list|>
name|buildThrottleQuotas
parameter_list|(
name|Throttle
name|proto
parameter_list|)
block|{
name|HashMap
argument_list|<
name|ThrottleType
argument_list|,
name|TimedQuota
argument_list|>
name|quotas
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasReadNum
argument_list|()
condition|)
block|{
name|quotas
operator|.
name|put
argument_list|(
name|ThrottleType
operator|.
name|READ_NUMBER
argument_list|,
name|proto
operator|.
name|getReadNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasReadSize
argument_list|()
condition|)
block|{
name|quotas
operator|.
name|put
argument_list|(
name|ThrottleType
operator|.
name|READ_SIZE
argument_list|,
name|proto
operator|.
name|getReadSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasReqNum
argument_list|()
condition|)
block|{
name|quotas
operator|.
name|put
argument_list|(
name|ThrottleType
operator|.
name|REQUEST_NUMBER
argument_list|,
name|proto
operator|.
name|getReqNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasReqSize
argument_list|()
condition|)
block|{
name|quotas
operator|.
name|put
argument_list|(
name|ThrottleType
operator|.
name|REQUEST_SIZE
argument_list|,
name|proto
operator|.
name|getReqSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasWriteNum
argument_list|()
condition|)
block|{
name|quotas
operator|.
name|put
argument_list|(
name|ThrottleType
operator|.
name|WRITE_NUMBER
argument_list|,
name|proto
operator|.
name|getWriteNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasWriteSize
argument_list|()
condition|)
block|{
name|quotas
operator|.
name|put
argument_list|(
name|ThrottleType
operator|.
name|WRITE_SIZE
argument_list|,
name|proto
operator|.
name|getWriteSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|quotas
return|;
block|}
specifier|private
name|void
name|clearThrottleBuilder
parameter_list|(
name|QuotaProtos
operator|.
name|Throttle
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|builder
operator|.
name|clearReadNum
argument_list|()
expr_stmt|;
name|builder
operator|.
name|clearReadSize
argument_list|()
expr_stmt|;
name|builder
operator|.
name|clearReqNum
argument_list|()
expr_stmt|;
name|builder
operator|.
name|clearReqSize
argument_list|()
expr_stmt|;
name|builder
operator|.
name|clearWriteNum
argument_list|()
expr_stmt|;
name|builder
operator|.
name|clearWriteSize
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

