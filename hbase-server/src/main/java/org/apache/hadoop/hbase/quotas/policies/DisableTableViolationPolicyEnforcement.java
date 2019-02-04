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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|SpaceViolationPolicy
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
comment|/**  * A {@link SpaceViolationPolicyEnforcement} which disables the table. The enforcement counterpart  * to {@link SpaceViolationPolicy#DISABLE}. This violation policy is different from others as it  * doesn't take action (i.e. enable/disable table) local to the RegionServer, like the other  * ViolationPolicies do. In case of violation, the appropriate action is initiated by the master.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DisableTableViolationPolicyEnforcement
extends|extends
name|DefaultViolationPolicyEnforcement
block|{
annotation|@
name|Override
specifier|public
name|void
name|enable
parameter_list|()
throws|throws
name|IOException
block|{
comment|// do nothing
block|}
annotation|@
name|Override
specifier|public
name|void
name|disable
parameter_list|()
throws|throws
name|IOException
block|{
comment|// do nothing
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
block|{
comment|// If this policy is enacted, then the table is (or should be) disabled.
throw|throw
operator|new
name|SpaceLimitingException
argument_list|(
name|getPolicyName
argument_list|()
argument_list|,
literal|"This table is disabled due to violating a space quota."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPolicyName
parameter_list|()
block|{
return|return
name|SpaceViolationPolicy
operator|.
name|DISABLE
operator|.
name|name
argument_list|()
return|;
block|}
block|}
end_class

end_unit

