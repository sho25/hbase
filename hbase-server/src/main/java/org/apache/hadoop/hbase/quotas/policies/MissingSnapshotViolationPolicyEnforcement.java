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
name|java
operator|.
name|util
operator|.
name|List
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
name|fs
operator|.
name|FileSystem
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

begin_comment
comment|/**  * A {@link SpaceViolationPolicyEnforcement} which can be treated as a singleton. When a quota is  * not defined on a table or we lack quota information, we want to avoid creating a policy, keeping  * this path fast.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|MissingSnapshotViolationPolicyEnforcement
extends|extends
name|AbstractViolationPolicyEnforcement
block|{
specifier|private
specifier|static
specifier|final
name|MissingSnapshotViolationPolicyEnforcement
name|SINGLETON
init|=
operator|new
name|MissingSnapshotViolationPolicyEnforcement
argument_list|()
decl_stmt|;
specifier|private
name|MissingSnapshotViolationPolicyEnforcement
parameter_list|()
block|{}
specifier|public
specifier|static
name|SpaceViolationPolicyEnforcement
name|getInstance
parameter_list|()
block|{
return|return
name|SINGLETON
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldCheckBulkLoads
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|computeBulkLoadSize
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|paths
parameter_list|)
throws|throws
name|SpaceLimitingException
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|path
range|:
name|paths
control|)
block|{
name|size
operator|+=
name|getFileSize
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|enable
parameter_list|()
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|disable
parameter_list|()
throws|throws
name|IOException
block|{}
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
annotation|@
name|Override
specifier|public
name|String
name|getPolicyName
parameter_list|()
block|{
return|return
literal|"NoQuota"
return|;
block|}
block|}
end_class

end_unit

