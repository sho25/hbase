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
name|Objects
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
name|protobuf
operator|.
name|TextFormat
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
name|MasterProtos
operator|.
name|SetQuotaRequest
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|QuotaSettings
block|{
specifier|private
specifier|final
name|String
name|userName
decl_stmt|;
specifier|private
specifier|final
name|String
name|namespace
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|regionServer
decl_stmt|;
specifier|protected
name|QuotaSettings
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
name|String
name|regionServer
parameter_list|)
block|{
name|this
operator|.
name|userName
operator|=
name|userName
expr_stmt|;
name|this
operator|.
name|namespace
operator|=
name|namespace
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|regionServer
operator|=
name|regionServer
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|QuotaType
name|getQuotaType
parameter_list|()
function_decl|;
specifier|public
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|userName
return|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|String
name|getNamespace
parameter_list|()
block|{
return|return
name|namespace
return|;
block|}
specifier|public
name|String
name|getRegionServer
parameter_list|()
block|{
return|return
name|regionServer
return|;
block|}
comment|/**    * Converts the protocol buffer request into a QuotaSetting POJO. Arbitrarily    * enforces that the request only contain one "limit", despite the message    * allowing multiple. The public API does not allow such use of the message.    *    * @param request The protocol buffer request.    * @return A {@link QuotaSettings} POJO.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
name|QuotaSettings
name|buildFromProto
parameter_list|(
name|SetQuotaRequest
name|request
parameter_list|)
block|{
name|String
name|username
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasUserName
argument_list|()
condition|)
block|{
name|username
operator|=
name|request
operator|.
name|getUserName
argument_list|()
expr_stmt|;
block|}
name|TableName
name|tableName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasTableName
argument_list|()
condition|)
block|{
name|tableName
operator|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|request
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|namespace
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasNamespace
argument_list|()
condition|)
block|{
name|namespace
operator|=
name|request
operator|.
name|getNamespace
argument_list|()
expr_stmt|;
block|}
name|String
name|regionServer
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasRegionServer
argument_list|()
condition|)
block|{
name|regionServer
operator|=
name|request
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasBypassGlobals
argument_list|()
condition|)
block|{
comment|// Make sure we don't have either of the two below limits also included
if|if
condition|(
name|request
operator|.
name|hasSpaceLimit
argument_list|()
operator|||
name|request
operator|.
name|hasThrottle
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"SetQuotaRequest has multiple limits: "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|request
argument_list|)
argument_list|)
throw|;
block|}
return|return
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
name|username
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|regionServer
argument_list|,
name|request
operator|.
name|getBypassGlobals
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|hasSpaceLimit
argument_list|()
condition|)
block|{
comment|// Make sure we don't have the below limit as well
if|if
condition|(
name|request
operator|.
name|hasThrottle
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"SetQuotaRequests has multiple limits: "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|request
argument_list|)
argument_list|)
throw|;
block|}
comment|// Sanity check on the pb received.
if|if
condition|(
operator|!
name|request
operator|.
name|getSpaceLimit
argument_list|()
operator|.
name|hasQuota
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"SpaceLimitRequest is missing the expected SpaceQuota."
argument_list|)
throw|;
block|}
return|return
name|QuotaSettingsFactory
operator|.
name|fromSpace
argument_list|(
name|tableName
argument_list|,
name|namespace
argument_list|,
name|request
operator|.
name|getSpaceLimit
argument_list|()
operator|.
name|getQuota
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|hasThrottle
argument_list|()
condition|)
block|{
return|return
operator|new
name|ThrottleSettings
argument_list|(
name|username
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|regionServer
argument_list|,
name|request
operator|.
name|getThrottle
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unhandled SetRequestRequest state"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert a QuotaSettings to a protocol buffer SetQuotaRequest.    * This is used internally by the Admin client to serialize the quota settings    * and send them to the master.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
name|SetQuotaRequest
name|buildSetQuotaRequestProto
parameter_list|(
specifier|final
name|QuotaSettings
name|settings
parameter_list|)
block|{
name|SetQuotaRequest
operator|.
name|Builder
name|builder
init|=
name|SetQuotaRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|getUserName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setUserName
argument_list|(
name|settings
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|settings
operator|.
name|getTableName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|settings
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|settings
operator|.
name|getNamespace
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setNamespace
argument_list|(
name|settings
operator|.
name|getNamespace
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|settings
operator|.
name|getRegionServer
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setRegionServer
argument_list|(
name|settings
operator|.
name|getRegionServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|settings
operator|.
name|setupSetQuotaRequest
argument_list|(
name|builder
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Called by toSetQuotaRequestProto()    * the subclass should implement this method to set the specific SetQuotaRequest    * properties.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|protected
specifier|abstract
name|void
name|setupSetQuotaRequest
parameter_list|(
name|SetQuotaRequest
operator|.
name|Builder
name|builder
parameter_list|)
function_decl|;
specifier|protected
name|String
name|ownerToString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|userName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"USER => '"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"', "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"TABLE => '"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"', "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"NAMESPACE => '"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|namespace
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"', "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionServer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"REGIONSERVER => "
argument_list|)
operator|.
name|append
argument_list|(
name|regionServer
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
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
specifier|protected
specifier|static
name|String
name|sizeToString
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|50
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fP"
argument_list|,
operator|(
name|double
operator|)
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|50
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|40
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fT"
argument_list|,
operator|(
name|double
operator|)
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|40
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|30
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fG"
argument_list|,
operator|(
name|double
operator|)
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|30
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|20
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fM"
argument_list|,
operator|(
name|double
operator|)
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|20
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|10
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fK"
argument_list|,
operator|(
name|double
operator|)
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|10
operator|)
argument_list|)
return|;
block|}
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fB"
argument_list|,
operator|(
name|double
operator|)
name|size
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|String
name|timeToString
parameter_list|(
specifier|final
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
switch|switch
condition|(
name|timeUnit
condition|)
block|{
case|case
name|NANOSECONDS
case|:
return|return
literal|"nsec"
return|;
case|case
name|MICROSECONDS
case|:
return|return
literal|"usec"
return|;
case|case
name|MILLISECONDS
case|:
return|return
literal|"msec"
return|;
case|case
name|SECONDS
case|:
return|return
literal|"sec"
return|;
case|case
name|MINUTES
case|:
return|return
literal|"min"
return|;
case|case
name|HOURS
case|:
return|return
literal|"hour"
return|;
case|case
name|DAYS
case|:
return|return
literal|"day"
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid TimeUnit "
operator|+
name|timeUnit
argument_list|)
throw|;
block|}
comment|/**    * Merges the provided settings with {@code this} and returns a new settings    * object to the caller if the merged settings differ from the original.    *    * @param newSettings The new settings to merge in.    * @return The merged {@link QuotaSettings} object or null if the quota should be deleted.    */
specifier|abstract
name|QuotaSettings
name|merge
parameter_list|(
name|QuotaSettings
name|newSettings
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Validates that settings being merged into {@code this} is targeting the same "subject", e.g.    * user, table, namespace.    *    * @param mergee The quota settings to be merged into {@code this}.    * @throws IllegalArgumentException if the subjects are not equal.    */
name|void
name|validateQuotaTarget
parameter_list|(
name|QuotaSettings
name|mergee
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|getUserName
argument_list|()
argument_list|,
name|mergee
operator|.
name|getUserName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Mismatched user names on settings to merge"
argument_list|)
throw|;
block|}
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
name|mergee
operator|.
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Mismatched table names on settings to merge"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|getNamespace
argument_list|()
argument_list|,
name|mergee
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
literal|"Mismatched namespace on settings to merge"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|getRegionServer
argument_list|()
argument_list|,
name|mergee
operator|.
name|getRegionServer
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Mismatched region server on settings to merge"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

