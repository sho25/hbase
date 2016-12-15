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
name|commons
operator|.
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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

begin_comment
comment|/**  * A point-in-time view of a space quota on a table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SpaceQuotaSnapshot
block|{
specifier|private
specifier|static
specifier|final
name|SpaceQuotaSnapshot
name|NO_SUCH_SNAPSHOT
init|=
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|SpaceQuotaStatus
operator|.
name|notInViolation
argument_list|()
argument_list|,
literal|0
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SpaceQuotaStatus
name|quotaStatus
decl_stmt|;
specifier|private
specifier|final
name|long
name|usage
decl_stmt|;
specifier|private
specifier|final
name|long
name|limit
decl_stmt|;
comment|/**    * Encapsulates the state of a quota on a table. The quota may or may not be in violation.    * If it is in violation, there will be a non-null violation policy.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|SpaceQuotaStatus
block|{
specifier|private
specifier|static
specifier|final
name|SpaceQuotaStatus
name|NOT_IN_VIOLATION
init|=
operator|new
name|SpaceQuotaStatus
argument_list|(
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|SpaceViolationPolicy
name|policy
decl_stmt|;
specifier|final
name|boolean
name|inViolation
decl_stmt|;
specifier|public
name|SpaceQuotaStatus
parameter_list|(
name|SpaceViolationPolicy
name|policy
parameter_list|)
block|{
name|this
operator|.
name|policy
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|policy
argument_list|)
expr_stmt|;
name|this
operator|.
name|inViolation
operator|=
literal|true
expr_stmt|;
block|}
specifier|private
name|SpaceQuotaStatus
parameter_list|(
name|SpaceViolationPolicy
name|policy
parameter_list|,
name|boolean
name|inViolation
parameter_list|)
block|{
name|this
operator|.
name|policy
operator|=
name|policy
expr_stmt|;
name|this
operator|.
name|inViolation
operator|=
name|inViolation
expr_stmt|;
block|}
comment|/**      * The violation policy which may be null. Is guaranteed to be non-null if      * {@link #isInViolation()} is<code>true</code>, and<code>false</code>      * otherwise.      */
specifier|public
name|SpaceViolationPolicy
name|getPolicy
parameter_list|()
block|{
return|return
name|policy
return|;
block|}
comment|/**      *<code>true</code> if the quota is being violated,<code>false</code> otherwise.      */
specifier|public
name|boolean
name|isInViolation
parameter_list|()
block|{
return|return
name|inViolation
return|;
block|}
comment|/**      * Returns a singleton referring to a quota which is not in violation.      */
specifier|public
specifier|static
name|SpaceQuotaStatus
name|notInViolation
parameter_list|()
block|{
return|return
name|NOT_IN_VIOLATION
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|new
name|HashCodeBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|policy
operator|==
literal|null
condition|?
literal|0
else|:
name|policy
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
name|inViolation
argument_list|)
operator|.
name|toHashCode
argument_list|()
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
operator|instanceof
name|SpaceQuotaStatus
condition|)
block|{
name|SpaceQuotaStatus
name|other
init|=
operator|(
name|SpaceQuotaStatus
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|policy
argument_list|,
name|other
operator|.
name|policy
argument_list|)
operator|&&
name|inViolation
operator|==
name|other
operator|.
name|inViolation
return|;
block|}
return|return
literal|false
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
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"[policy="
argument_list|)
operator|.
name|append
argument_list|(
name|policy
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", inViolation="
argument_list|)
operator|.
name|append
argument_list|(
name|inViolation
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|QuotaProtos
operator|.
name|SpaceQuotaStatus
name|toProto
parameter_list|(
name|SpaceQuotaStatus
name|status
parameter_list|)
block|{
name|QuotaProtos
operator|.
name|SpaceQuotaStatus
operator|.
name|Builder
name|builder
init|=
name|QuotaProtos
operator|.
name|SpaceQuotaStatus
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setInViolation
argument_list|(
name|status
operator|.
name|inViolation
argument_list|)
expr_stmt|;
if|if
condition|(
name|status
operator|.
name|isInViolation
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setPolicy
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoViolationPolicy
argument_list|(
name|status
operator|.
name|getPolicy
argument_list|()
argument_list|)
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
specifier|public
specifier|static
name|SpaceQuotaStatus
name|toStatus
parameter_list|(
name|QuotaProtos
operator|.
name|SpaceQuotaStatus
name|proto
parameter_list|)
block|{
if|if
condition|(
name|proto
operator|.
name|getInViolation
argument_list|()
condition|)
block|{
return|return
operator|new
name|SpaceQuotaStatus
argument_list|(
name|ProtobufUtil
operator|.
name|toViolationPolicy
argument_list|(
name|proto
operator|.
name|getPolicy
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|NOT_IN_VIOLATION
return|;
block|}
block|}
block|}
specifier|public
name|SpaceQuotaSnapshot
parameter_list|(
name|SpaceQuotaStatus
name|quotaStatus
parameter_list|,
name|long
name|usage
parameter_list|,
name|long
name|limit
parameter_list|)
block|{
name|this
operator|.
name|quotaStatus
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|quotaStatus
argument_list|)
expr_stmt|;
name|this
operator|.
name|usage
operator|=
name|usage
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
comment|/**    * Returns the status of the quota.    */
specifier|public
name|SpaceQuotaStatus
name|getQuotaStatus
parameter_list|()
block|{
return|return
name|quotaStatus
return|;
block|}
comment|/**    * Returns the current usage, in bytes, of the target (e.g. table, namespace).    */
specifier|public
name|long
name|getUsage
parameter_list|()
block|{
return|return
name|usage
return|;
block|}
comment|/**    * Returns the limit, in bytes, of the target (e.g. table, namespace).    */
specifier|public
name|long
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|new
name|HashCodeBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|quotaStatus
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
name|usage
argument_list|)
operator|.
name|append
argument_list|(
name|limit
argument_list|)
operator|.
name|toHashCode
argument_list|()
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
operator|instanceof
name|SpaceQuotaSnapshot
condition|)
block|{
name|SpaceQuotaSnapshot
name|other
init|=
operator|(
name|SpaceQuotaSnapshot
operator|)
name|o
decl_stmt|;
return|return
name|quotaStatus
operator|.
name|equals
argument_list|(
name|other
operator|.
name|quotaStatus
argument_list|)
operator|&&
name|usage
operator|==
name|other
operator|.
name|usage
operator|&&
name|limit
operator|==
name|other
operator|.
name|limit
return|;
block|}
return|return
literal|false
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
argument_list|(
literal|32
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"SpaceQuotaSnapshot[policy="
argument_list|)
operator|.
name|append
argument_list|(
name|quotaStatus
argument_list|)
operator|.
name|append
argument_list|(
literal|", use="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|usage
argument_list|)
operator|.
name|append
argument_list|(
literal|"bytes/"
argument_list|)
operator|.
name|append
argument_list|(
name|limit
argument_list|)
operator|.
name|append
argument_list|(
literal|"bytes]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// ProtobufUtil is in hbase-client, and this doesn't need to be public.
specifier|public
specifier|static
name|SpaceQuotaSnapshot
name|toSpaceQuotaSnapshot
parameter_list|(
name|QuotaProtos
operator|.
name|SpaceQuotaSnapshot
name|proto
parameter_list|)
block|{
return|return
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|SpaceQuotaStatus
operator|.
name|toStatus
argument_list|(
name|proto
operator|.
name|getStatus
argument_list|()
argument_list|)
argument_list|,
name|proto
operator|.
name|getUsage
argument_list|()
argument_list|,
name|proto
operator|.
name|getLimit
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|QuotaProtos
operator|.
name|SpaceQuotaSnapshot
name|toProtoSnapshot
parameter_list|(
name|SpaceQuotaSnapshot
name|snapshot
parameter_list|)
block|{
return|return
name|QuotaProtos
operator|.
name|SpaceQuotaSnapshot
operator|.
name|newBuilder
argument_list|()
operator|.
name|setStatus
argument_list|(
name|SpaceQuotaStatus
operator|.
name|toProto
argument_list|(
name|snapshot
operator|.
name|getQuotaStatus
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setUsage
argument_list|(
name|snapshot
operator|.
name|getUsage
argument_list|()
argument_list|)
operator|.
name|setLimit
argument_list|(
name|snapshot
operator|.
name|getLimit
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a singleton that corresponds to no snapshot information.    */
specifier|public
specifier|static
name|SpaceQuotaSnapshot
name|getNoSuchSnapshot
parameter_list|()
block|{
return|return
name|NO_SUCH_SNAPSHOT
return|;
block|}
block|}
end_class

end_unit

