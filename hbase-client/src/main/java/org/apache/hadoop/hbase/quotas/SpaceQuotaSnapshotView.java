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
name|util
operator|.
name|Optional
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
comment|/**  * A point-in-time view of a space quota on a table, read only.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|SpaceQuotaSnapshotView
block|{
comment|/**    * Encapsulates the state of a quota on a table. The quota may or may not be in violation. If the    * quota is not in violation, the violation may not be presented. If the quota is in violation,    * there is guaranteed to be presented.    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
interface|interface
name|SpaceQuotaStatusView
block|{
comment|/**      * Returns the violation policy, which may not be presented. It is guaranteed to be presented if      * {@link #isInViolation()} is {@code true}, but may not be presented otherwise.      */
name|Optional
argument_list|<
name|SpaceViolationPolicy
argument_list|>
name|getPolicy
parameter_list|()
function_decl|;
comment|/**      * @return {@code true} if the quota is being violated, {@code false} otherwise.      */
name|boolean
name|isInViolation
parameter_list|()
function_decl|;
block|}
comment|/**    * Returns the status of the quota.    */
name|SpaceQuotaStatusView
name|getQuotaStatus
parameter_list|()
function_decl|;
comment|/**    * Returns the current usage, in bytes, of the target (e.g. table, namespace).    */
name|long
name|getUsage
parameter_list|()
function_decl|;
comment|/**    * Returns the limit, in bytes, of the target (e.g. table, namespace).    */
name|long
name|getLimit
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

