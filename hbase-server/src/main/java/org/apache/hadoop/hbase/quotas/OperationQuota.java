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
name|List
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
name|client
operator|.
name|Result
import|;
end_import

begin_comment
comment|/**  * Interface that allows to check the quota available for an operation.  */
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
name|OperationQuota
block|{
specifier|public
enum|enum
name|OperationType
block|{
name|MUTATE
block|,
name|GET
block|,
name|SCAN
block|}
comment|/**    * Checks if it is possible to execute the specified operation.    * The quota will be estimated based on the number of operations to perform    * and the average size accumulated during time.    *    * @param numWrites number of write operation that will be performed    * @param numReads number of small-read operation that will be performed    * @param numScans number of long-read operation that will be performed    * @throws RpcThrottlingException if the operation cannot be performed because    *   RPC quota is exceeded.    */
name|void
name|checkQuota
parameter_list|(
name|int
name|numWrites
parameter_list|,
name|int
name|numReads
parameter_list|,
name|int
name|numScans
parameter_list|)
throws|throws
name|RpcThrottlingException
function_decl|;
comment|/** Cleanup method on operation completion */
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Add a get result. This will be used to calculate the exact quota and    * have a better short-read average size for the next time.    */
name|void
name|addGetResult
parameter_list|(
name|Result
name|result
parameter_list|)
function_decl|;
comment|/**    * Add a scan result. This will be used to calculate the exact quota and    * have a better long-read average size for the next time.    */
name|void
name|addScanResult
parameter_list|(
name|List
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|)
function_decl|;
comment|/**    * Add a mutation result. This will be used to calculate the exact quota and    * have a better mutation average size for the next time.    */
name|void
name|addMutation
parameter_list|(
name|Mutation
name|mutation
parameter_list|)
function_decl|;
comment|/** @return the number of bytes available to read to avoid exceeding the quota */
name|long
name|getReadAvailable
parameter_list|()
function_decl|;
comment|/** @return the number of bytes available to write to avoid exceeding the quota */
name|long
name|getWriteAvailable
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

