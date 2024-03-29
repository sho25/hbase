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
name|Collections
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

begin_comment
comment|/**  * A class to ease dealing with tables that have and do not have violation policies  * being enforced. This class is immutable, expect for {@code locallyCachedPolicies}.  *  * The {@code locallyCachedPolicies} are mutable given the current {@code activePolicies}  * and {@code snapshots}. It is expected that when a new instance of this class is  * instantiated, we also want to invalidate those previously cached policies (as they  * may now be invalidate if we received new quota usage information).  */
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
name|ActivePolicyEnforcement
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|activePolicies
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|snapshots
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rss
decl_stmt|;
specifier|private
specifier|final
name|SpaceViolationPolicyEnforcementFactory
name|factory
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|locallyCachedPolicies
decl_stmt|;
specifier|public
name|ActivePolicyEnforcement
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|activePolicies
parameter_list|,
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|snapshots
parameter_list|,
name|RegionServerServices
name|rss
parameter_list|)
block|{
name|this
argument_list|(
name|activePolicies
argument_list|,
name|snapshots
argument_list|,
name|rss
argument_list|,
name|SpaceViolationPolicyEnforcementFactory
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ActivePolicyEnforcement
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|activePolicies
parameter_list|,
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|snapshots
parameter_list|,
name|RegionServerServices
name|rss
parameter_list|,
name|SpaceViolationPolicyEnforcementFactory
name|factory
parameter_list|)
block|{
name|this
operator|.
name|activePolicies
operator|=
name|activePolicies
expr_stmt|;
name|this
operator|.
name|snapshots
operator|=
name|snapshots
expr_stmt|;
name|this
operator|.
name|rss
operator|=
name|rss
expr_stmt|;
name|this
operator|.
name|factory
operator|=
name|factory
expr_stmt|;
comment|// Mutable!
name|this
operator|.
name|locallyCachedPolicies
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns the proper {@link SpaceViolationPolicyEnforcement} implementation for the given table.    * If the given table does not have a violation policy enforced, a "no-op" policy will    * be returned which always allows an action.    *    * @see #getPolicyEnforcement(TableName)    */
specifier|public
name|SpaceViolationPolicyEnforcement
name|getPolicyEnforcement
parameter_list|(
name|Region
name|r
parameter_list|)
block|{
return|return
name|getPolicyEnforcement
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|r
argument_list|)
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns the proper {@link SpaceViolationPolicyEnforcement} implementation for the given table.    * If the given table does not have a violation policy enforced, a "no-op" policy will    * be returned which always allows an action.    *    * @param tableName The table to fetch the policy for.    * @return A non-null {@link SpaceViolationPolicyEnforcement} instance.    */
specifier|public
name|SpaceViolationPolicyEnforcement
name|getPolicyEnforcement
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|SpaceViolationPolicyEnforcement
name|policy
init|=
name|activePolicies
operator|.
name|get
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|policy
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|locallyCachedPolicies
init|)
block|{
comment|// When we don't have an policy enforcement for the table, there could be one of two cases:
comment|//  1) The table has no quota defined
comment|//  2) The table is not in violation of its quota
comment|// In both of these cases, we want to make sure that access remains fast and we minimize
comment|// object creation. We can accomplish this by locally caching policies instead of creating
comment|// a new instance of the policy each time.
name|policy
operator|=
name|locallyCachedPolicies
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// We have already created/cached the enforcement, use it again. `activePolicies` and
comment|// `snapshots` are immutable, thus this policy is valid for the lifetime of `this`.
if|if
condition|(
name|policy
operator|!=
literal|null
condition|)
block|{
return|return
name|policy
return|;
block|}
comment|// Create a PolicyEnforcement for this table and snapshot. The snapshot may be null
comment|// which is OK.
name|policy
operator|=
name|factory
operator|.
name|createWithoutViolation
argument_list|(
name|rss
argument_list|,
name|tableName
argument_list|,
name|snapshots
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// Cache the policy we created
name|locallyCachedPolicies
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|policy
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|policy
return|;
block|}
comment|/**    * Returns an unmodifiable version of the active {@link SpaceViolationPolicyEnforcement}s.    */
specifier|public
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|getPolicies
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|activePolicies
argument_list|)
return|;
block|}
comment|/**    * Returns an unmodifiable version of the policy enforcements that were cached because they are    * not in violation of their quota.    */
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceViolationPolicyEnforcement
argument_list|>
name|getLocallyCachedPolicies
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|locallyCachedPolicies
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
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": "
operator|+
name|activePolicies
return|;
block|}
block|}
end_class

end_unit

