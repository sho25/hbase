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
operator|.
name|policies
package|;
end_package

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
name|quotas
operator|.
name|SpaceLimitingException
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
name|SpaceViolationPolicyEnforcement
import|;
end_import

begin_comment
comment|/**  * A {@link SpaceViolationPolicyEnforcement} instance which only checks for bulk loads. Useful for tables  * which have no violation policy. This is the default case for tables, as we want to make sure that  * a single bulk load call would violate the quota.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BulkLoadVerifyingViolationPolicyEnforcement
extends|extends
name|AbstractViolationPolicyEnforcement
block|{
annotation|@
name|Override
specifier|public
name|void
name|enable
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|disable
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|String
name|getPolicyName
parameter_list|()
block|{
return|return
literal|"BulkLoadVerifying"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|areCompactionsDisabled
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|check
parameter_list|(
name|Mutation
name|m
parameter_list|)
throws|throws
name|SpaceLimitingException
block|{}
block|}
end_class

end_unit

