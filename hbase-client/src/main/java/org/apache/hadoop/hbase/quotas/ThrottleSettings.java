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
name|MasterProtos
operator|.
name|SetQuotaRequest
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
class|class
name|ThrottleSettings
extends|extends
name|QuotaSettings
block|{
specifier|private
specifier|final
name|QuotaProtos
operator|.
name|ThrottleRequest
name|proto
decl_stmt|;
name|ThrottleSettings
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
name|ThrottleRequest
name|proto
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
name|proto
operator|=
name|proto
expr_stmt|;
block|}
specifier|public
name|ThrottleType
name|getThrottleType
parameter_list|()
block|{
return|return
name|ProtobufUtil
operator|.
name|toThrottleType
argument_list|(
name|proto
operator|.
name|getType
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|long
name|getSoftLimit
parameter_list|()
block|{
return|return
name|proto
operator|.
name|hasTimedQuota
argument_list|()
condition|?
name|proto
operator|.
name|getTimedQuota
argument_list|()
operator|.
name|getSoftLimit
argument_list|()
else|:
operator|-
literal|1
return|;
block|}
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|proto
operator|.
name|hasTimedQuota
argument_list|()
condition|?
name|ProtobufUtil
operator|.
name|toTimeUnit
argument_list|(
name|proto
operator|.
name|getTimedQuota
argument_list|()
operator|.
name|getTimeUnit
argument_list|()
argument_list|)
else|:
literal|null
return|;
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
name|THROTTLE
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
name|setThrottle
argument_list|(
name|proto
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
literal|"TYPE => THROTTLE"
argument_list|)
expr_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasType
argument_list|()
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", THROTTLE_TYPE => "
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|proto
operator|.
name|getType
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasTimedQuota
argument_list|()
condition|)
block|{
name|QuotaProtos
operator|.
name|TimedQuota
name|timedQuota
init|=
name|proto
operator|.
name|getTimedQuota
argument_list|()
decl_stmt|;
name|builder
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
name|getThrottleType
argument_list|()
condition|)
block|{
case|case
name|REQUEST_NUMBER
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
else|else
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", LIMIT => NONE"
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
specifier|static
name|ThrottleSettings
name|fromTimedQuota
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
name|ThrottleType
name|type
parameter_list|,
name|QuotaProtos
operator|.
name|TimedQuota
name|timedQuota
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
name|builder
operator|.
name|setTimedQuota
argument_list|(
name|timedQuota
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

