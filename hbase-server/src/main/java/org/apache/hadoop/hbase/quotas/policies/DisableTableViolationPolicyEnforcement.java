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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|TableNotDisabledException
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
name|TableNotEnabledException
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
comment|/**  * A {@link SpaceViolationPolicyEnforcement} which disables the table. The enforcement  * counterpart to {@link SpaceViolationPolicy#DISABLE}.  */
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
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DisableTableViolationPolicyEnforcement
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|enable
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Starting disable of "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|getRegionServerServices
argument_list|()
operator|.
name|getClusterConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Disable is complete for "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TableNotEnabledException
name|tnee
parameter_list|)
block|{
comment|// The state we wanted it to be in.
block|}
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
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Starting enable of "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|getRegionServerServices
argument_list|()
operator|.
name|getClusterConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Enable is complete for "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TableNotDisabledException
name|tnde
parameter_list|)
block|{
comment|// The state we wanted it to be in
block|}
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
