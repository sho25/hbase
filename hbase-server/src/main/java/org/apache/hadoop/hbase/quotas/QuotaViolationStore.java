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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|HRegionInfo
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
name|generated
operator|.
name|QuotaProtos
operator|.
name|SpaceQuota
import|;
end_import

begin_comment
comment|/**  * A common interface for computing and storing space quota observance/violation for entities.  *  * An entity is presently a table or a namespace.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|QuotaViolationStore
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**    * The current state of a table with respect to the policy set forth by a quota.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|ViolationState
block|{
name|IN_VIOLATION
block|,
name|IN_OBSERVANCE
block|,   }
comment|/**    * Fetch the Quota for the given {@code subject}. May be null.    *    * @param subject The object for which the quota should be fetched    */
name|SpaceQuota
name|getSpaceQuota
parameter_list|(
name|T
name|subject
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns the current {@link ViolationState} for the given {@code subject}.    *    * @param subject The object which the quota violation state should be fetched    */
name|ViolationState
name|getCurrentState
parameter_list|(
name|T
name|subject
parameter_list|)
function_decl|;
comment|/**    * Computes the target {@link ViolationState} for the given {@code subject} and    * {@code spaceQuota}.    *    * @param subject The object which to determine the target quota violation state of    * @param spaceQuota The quota "definition" for the {@code subject}    */
name|ViolationState
name|getTargetState
parameter_list|(
name|T
name|subject
parameter_list|,
name|SpaceQuota
name|spaceQuota
parameter_list|)
function_decl|;
comment|/**    * Filters the provided<code>regions</code>, returning those which match the given    *<code>subject</code>.    *    * @param subject The filter criteria. Only regions belonging to this parameter will be returned    */
name|Iterable
argument_list|<
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|Long
argument_list|>
argument_list|>
name|filterBySubject
parameter_list|(
name|T
name|subject
parameter_list|)
function_decl|;
comment|/**    * Persists the current {@link ViolationState} for the {@code subject}.    *    * @param subject The object which the {@link ViolationState} is being persisted for    * @param state The current {@link ViolationState} of the {@code subject}    */
name|void
name|setCurrentState
parameter_list|(
name|T
name|subject
parameter_list|,
name|ViolationState
name|state
parameter_list|)
function_decl|;
comment|/**    * Updates {@code this} with the latest snapshot of filesystem use by region.    *    * @param regionUsage A map of {@code HRegionInfo} objects to their filesystem usage in bytes    */
name|void
name|setRegionUsage
parameter_list|(
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Long
argument_list|>
name|regionUsage
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

