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
name|TableName
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
name|SpaceLimitRequest
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

begin_comment
comment|/**  * A {@link QuotaSettings} implementation for configuring filesystem-use quotas.  */
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
class|class
name|SpaceLimitSettings
extends|extends
name|QuotaSettings
block|{
specifier|private
specifier|final
name|SpaceLimitRequest
name|proto
decl_stmt|;
name|SpaceLimitSettings
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|long
name|sizeLimit
parameter_list|,
name|SpaceViolationPolicy
name|violationPolicy
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|validateSizeLimit
argument_list|(
name|sizeLimit
argument_list|)
expr_stmt|;
name|proto
operator|=
name|buildProtoAddQuota
argument_list|(
name|sizeLimit
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|violationPolicy
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a {@code SpaceLimitSettings} to remove a space quota on the given {@code tableName}.    */
name|SpaceLimitSettings
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|proto
operator|=
name|buildProtoRemoveQuota
argument_list|()
expr_stmt|;
block|}
name|SpaceLimitSettings
parameter_list|(
name|String
name|namespace
parameter_list|,
name|long
name|sizeLimit
parameter_list|,
name|SpaceViolationPolicy
name|violationPolicy
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|namespace
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|validateSizeLimit
argument_list|(
name|sizeLimit
argument_list|)
expr_stmt|;
name|proto
operator|=
name|buildProtoAddQuota
argument_list|(
name|sizeLimit
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|violationPolicy
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a {@code SpaceLimitSettings} to remove a space quota on the given {@code namespace}.    */
name|SpaceLimitSettings
parameter_list|(
name|String
name|namespace
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|namespace
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|proto
operator|=
name|buildProtoRemoveQuota
argument_list|()
expr_stmt|;
block|}
name|SpaceLimitSettings
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
name|namespace
parameter_list|,
name|SpaceLimitRequest
name|req
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|proto
operator|=
name|req
expr_stmt|;
block|}
comment|/**    * Build a {@link SpaceLimitRequest} protobuf object from the given {@link SpaceQuota}.    *    * @param protoQuota The preconstructed SpaceQuota protobuf    * @return A protobuf request to change a space limit quota    */
specifier|private
name|SpaceLimitRequest
name|buildProtoFromQuota
parameter_list|(
name|SpaceQuota
name|protoQuota
parameter_list|)
block|{
return|return
name|SpaceLimitRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setQuota
argument_list|(
name|protoQuota
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Builds a {@link SpaceQuota} protobuf object given the arguments.    *    * @param sizeLimit The size limit of the quota.    * @param violationPolicy The action to take when the quota is exceeded.    * @return The protobuf SpaceQuota representation.    */
specifier|private
name|SpaceLimitRequest
name|buildProtoAddQuota
parameter_list|(
name|long
name|sizeLimit
parameter_list|,
name|SpaceViolationPolicy
name|violationPolicy
parameter_list|)
block|{
return|return
name|buildProtoFromQuota
argument_list|(
name|SpaceQuota
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSoftLimit
argument_list|(
name|sizeLimit
argument_list|)
operator|.
name|setViolationPolicy
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoViolationPolicy
argument_list|(
name|violationPolicy
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Builds a {@link SpaceQuota} protobuf object to remove a quota.    *    * @return The protobuf SpaceQuota representation.    */
specifier|private
name|SpaceLimitRequest
name|buildProtoRemoveQuota
parameter_list|()
block|{
return|return
name|SpaceLimitRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setQuota
argument_list|(
name|SpaceQuota
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRemove
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a copy of the internal state of<code>this</code>    */
name|SpaceLimitRequest
name|getProto
parameter_list|()
block|{
return|return
name|proto
operator|.
name|toBuilder
argument_list|()
operator|.
name|build
argument_list|()
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
name|SPACE
return|;
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
comment|// TableName/Namespace are serialized in QuotaSettings
name|builder
operator|.
name|setSpaceLimit
argument_list|(
name|proto
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a {@link SpaceLimitSettings} from the provided protobuf message and tablename.    *    * @param tableName The target tablename for the limit.    * @param proto The protobuf representation.    * @return A QuotaSettings.    */
specifier|static
name|SpaceLimitSettings
name|fromSpaceQuota
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|QuotaProtos
operator|.
name|SpaceQuota
name|proto
parameter_list|)
block|{
name|validateProtoArguments
argument_list|(
name|proto
argument_list|)
expr_stmt|;
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|tableName
argument_list|,
name|proto
operator|.
name|getSoftLimit
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|toViolationPolicy
argument_list|(
name|proto
operator|.
name|getViolationPolicy
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Constructs a {@link SpaceLimitSettings} from the provided protobuf message and namespace.    *    * @param namespace The target namespace for the limit.    * @param proto The protobuf representation.    * @return A QuotaSettings.    */
specifier|static
name|SpaceLimitSettings
name|fromSpaceQuota
parameter_list|(
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|QuotaProtos
operator|.
name|SpaceQuota
name|proto
parameter_list|)
block|{
name|validateProtoArguments
argument_list|(
name|proto
argument_list|)
expr_stmt|;
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|namespace
argument_list|,
name|proto
operator|.
name|getSoftLimit
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|toViolationPolicy
argument_list|(
name|proto
operator|.
name|getViolationPolicy
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Validates that the provided protobuf SpaceQuota has the necessary information to construct    * a {@link SpaceLimitSettings}.    *    * @param proto The protobuf message to validate.    */
specifier|static
name|void
name|validateProtoArguments
parameter_list|(
specifier|final
name|QuotaProtos
operator|.
name|SpaceQuota
name|proto
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|proto
argument_list|)
operator|.
name|hasSoftLimit
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot handle SpaceQuota without a soft limit"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|proto
operator|.
name|hasViolationPolicy
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot handle SpaceQuota without a violation policy"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|getNamespace
argument_list|()
argument_list|,
name|proto
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|SpaceLimitSettings
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// o is non-null and an instance of SpaceLimitSettings
name|SpaceLimitSettings
name|other
init|=
operator|(
name|SpaceLimitSettings
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|other
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getNamespace
argument_list|()
argument_list|,
name|other
operator|.
name|getNamespace
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|proto
argument_list|,
name|other
operator|.
name|proto
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"TYPE => SPACE"
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
name|sb
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
name|sb
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
name|proto
operator|.
name|getQuota
argument_list|()
operator|.
name|getRemove
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", REMOVE => "
argument_list|)
operator|.
name|append
argument_list|(
name|proto
operator|.
name|getQuota
argument_list|()
operator|.
name|getRemove
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", LIMIT => "
argument_list|)
operator|.
name|append
argument_list|(
name|sizeToString
argument_list|(
name|proto
operator|.
name|getQuota
argument_list|()
operator|.
name|getSoftLimit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", VIOLATION_POLICY => "
argument_list|)
operator|.
name|append
argument_list|(
name|proto
operator|.
name|getQuota
argument_list|()
operator|.
name|getViolationPolicy
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|QuotaSettings
name|merge
parameter_list|(
name|QuotaSettings
name|newSettings
parameter_list|)
block|{
if|if
condition|(
name|newSettings
operator|instanceof
name|SpaceLimitSettings
condition|)
block|{
name|SpaceLimitSettings
name|settingsToMerge
init|=
operator|(
name|SpaceLimitSettings
operator|)
name|newSettings
decl_stmt|;
comment|// The message contained the expect SpaceQuota object
if|if
condition|(
name|settingsToMerge
operator|.
name|proto
operator|.
name|hasQuota
argument_list|()
condition|)
block|{
name|SpaceQuota
name|quotaToMerge
init|=
name|settingsToMerge
operator|.
name|proto
operator|.
name|getQuota
argument_list|()
decl_stmt|;
if|if
condition|(
name|quotaToMerge
operator|.
name|getRemove
argument_list|()
condition|)
block|{
return|return
name|settingsToMerge
return|;
block|}
else|else
block|{
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
name|newSettings
operator|+
literal|" into "
operator|+
name|this
argument_list|)
throw|;
block|}
comment|// Create a builder from the old settings
name|SpaceQuota
operator|.
name|Builder
name|mergedBuilder
init|=
name|this
operator|.
name|proto
operator|.
name|getQuota
argument_list|()
operator|.
name|toBuilder
argument_list|()
decl_stmt|;
comment|// Build a new SpaceQuotas object from merging in the new settings
return|return
operator|new
name|SpaceLimitSettings
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|getNamespace
argument_list|()
argument_list|,
name|buildProtoFromQuota
argument_list|(
name|mergedBuilder
operator|.
name|mergeFrom
argument_list|(
name|quotaToMerge
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|// else, we don't know what to do, so return the original object
block|}
return|return
name|this
return|;
block|}
comment|// Helper function to validate sizeLimit
specifier|private
name|void
name|validateSizeLimit
parameter_list|(
name|long
name|sizeLimit
parameter_list|)
block|{
if|if
condition|(
name|sizeLimit
operator|<
literal|0L
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Size limit must be a non-negative value."
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

