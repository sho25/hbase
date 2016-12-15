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
name|regionserver
operator|.
name|RegionServerServices
import|;
end_import

begin_comment
comment|/**  * RegionServer implementation of {@link SpaceViolationPolicy}.  *  * Implementations must have a public, no-args constructor.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|SpaceViolationPolicyEnforcement
block|{
comment|/**    * Initializes this policy instance.    */
name|void
name|initialize
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
function_decl|;
comment|/**    * Enables this policy. Not all policies have enable actions.    */
name|void
name|enable
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Disables this policy. Not all policies have disable actions.    */
name|void
name|disable
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Checks the given {@link Mutation} against<code>this</code> policy. If the    * {@link Mutation} violates the policy, this policy should throw a    * {@link SpaceLimitingException}.    *    * @throws SpaceLimitingException When the given mutation violates this policy.    */
name|void
name|check
parameter_list|(
name|Mutation
name|m
parameter_list|)
throws|throws
name|SpaceLimitingException
function_decl|;
comment|/**    * Returns a logical name for the {@link SpaceViolationPolicy} that this enforcement is for.    */
name|String
name|getPolicyName
parameter_list|()
function_decl|;
comment|/**    * Returns whether or not compactions on this table should be disabled for this policy.    */
name|boolean
name|areCompactionsDisabled
parameter_list|()
function_decl|;
comment|/**    * Returns the {@link SpaceQuotaSnapshot}<code>this</code> was initialized with.    */
name|SpaceQuotaSnapshot
name|getQuotaSnapshot
parameter_list|()
function_decl|;
comment|/**    * Returns whether thet caller should verify any bulk loads against<code>this</code>.    */
name|boolean
name|shouldCheckBulkLoads
parameter_list|()
function_decl|;
comment|/**    * Checks the file at the given path against<code>this</code> policy and the current    * {@link SpaceQuotaSnapshot}. If the file would violate the policy, a    * {@link SpaceLimitingException} will be thrown.    *    * @param paths The paths in HDFS to files to be bulk loaded.    */
name|void
name|checkBulkLoad
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
function_decl|;
block|}
end_interface

end_unit

