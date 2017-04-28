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
name|quotas
operator|.
name|SpaceQuotaSnapshot
operator|.
name|SpaceQuotaStatus
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
name|policies
operator|.
name|DefaultViolationPolicyEnforcement
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
name|policies
operator|.
name|DisableTableViolationPolicyEnforcement
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
name|policies
operator|.
name|MissingSnapshotViolationPolicyEnforcement
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
name|policies
operator|.
name|NoInsertsViolationPolicyEnforcement
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
name|policies
operator|.
name|NoWritesCompactionsViolationPolicyEnforcement
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
name|policies
operator|.
name|NoWritesViolationPolicyEnforcement
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
comment|/**  * A factory class for instantiating {@link SpaceViolationPolicyEnforcement} instances.  */
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
name|SpaceViolationPolicyEnforcementFactory
block|{
specifier|private
specifier|static
specifier|final
name|SpaceViolationPolicyEnforcementFactory
name|INSTANCE
init|=
operator|new
name|SpaceViolationPolicyEnforcementFactory
argument_list|()
decl_stmt|;
specifier|private
name|SpaceViolationPolicyEnforcementFactory
parameter_list|()
block|{}
comment|/**    * Returns an instance of this factory.    */
specifier|public
specifier|static
name|SpaceViolationPolicyEnforcementFactory
name|getInstance
parameter_list|()
block|{
return|return
name|INSTANCE
return|;
block|}
comment|/**    * Constructs the appropriate {@link SpaceViolationPolicyEnforcement} for tables that are    * in violation of their space quota.    */
specifier|public
name|SpaceViolationPolicyEnforcement
name|create
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|SpaceQuotaSnapshot
name|snapshot
parameter_list|)
block|{
name|SpaceViolationPolicyEnforcement
name|enforcement
decl_stmt|;
name|SpaceQuotaStatus
name|status
init|=
name|snapshot
operator|.
name|getQuotaStatus
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|status
operator|.
name|isInViolation
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|tableName
operator|+
literal|" is not in violation. Snapshot="
operator|+
name|snapshot
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|status
operator|.
name|getPolicy
argument_list|()
condition|)
block|{
case|case
name|DISABLE
case|:
name|enforcement
operator|=
operator|new
name|DisableTableViolationPolicyEnforcement
argument_list|()
expr_stmt|;
break|break;
case|case
name|NO_WRITES_COMPACTIONS
case|:
name|enforcement
operator|=
operator|new
name|NoWritesCompactionsViolationPolicyEnforcement
argument_list|()
expr_stmt|;
break|break;
case|case
name|NO_WRITES
case|:
name|enforcement
operator|=
operator|new
name|NoWritesViolationPolicyEnforcement
argument_list|()
expr_stmt|;
break|break;
case|case
name|NO_INSERTS
case|:
name|enforcement
operator|=
operator|new
name|NoInsertsViolationPolicyEnforcement
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unhandled SpaceViolationPolicy: "
operator|+
name|status
operator|.
name|getPolicy
argument_list|()
argument_list|)
throw|;
block|}
name|enforcement
operator|.
name|initialize
argument_list|(
name|rss
argument_list|,
name|tableName
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
return|return
name|enforcement
return|;
block|}
comment|/**    * Creates the "default" {@link SpaceViolationPolicyEnforcement} for a table that isn't in    * violation. This is used to have uniform policy checking for tables in and not quotas. This    * policy will still verify that new bulk loads do not exceed the configured quota limit.    *    * @param rss RegionServerServices instance the policy enforcement should use.    * @param tableName The target HBase table.    * @param snapshot The current quota snapshot for the {@code tableName}, can be null.    */
specifier|public
name|SpaceViolationPolicyEnforcement
name|createWithoutViolation
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|SpaceQuotaSnapshot
name|snapshot
parameter_list|)
block|{
if|if
condition|(
name|snapshot
operator|==
literal|null
condition|)
block|{
comment|// If we have no snapshot, this is equivalent to no quota for this table.
comment|// We should do use the (singleton instance) of this policy to do nothing.
return|return
name|MissingSnapshotViolationPolicyEnforcement
operator|.
name|getInstance
argument_list|()
return|;
block|}
comment|// We have a snapshot which means that there is a quota set on this table, but it's not in
comment|// violation of that quota. We need to construct a policy for this table.
name|SpaceQuotaStatus
name|status
init|=
name|snapshot
operator|.
name|getQuotaStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|status
operator|.
name|isInViolation
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|tableName
operator|+
literal|" is in violation. Logic error. Snapshot="
operator|+
name|snapshot
argument_list|)
throw|;
block|}
comment|// We have a unique size snapshot to use. Create an instance for this tablename + snapshot.
name|DefaultViolationPolicyEnforcement
name|enforcement
init|=
operator|new
name|DefaultViolationPolicyEnforcement
argument_list|()
decl_stmt|;
name|enforcement
operator|.
name|initialize
argument_list|(
name|rss
argument_list|,
name|tableName
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
return|return
name|enforcement
return|;
block|}
block|}
end_class

end_unit
