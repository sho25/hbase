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
name|OperationQuota
operator|.
name|OperationType
import|;
end_import

begin_comment
comment|/**  * Internal interface used to interact with the user/table quota.  */
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
name|QuotaLimiter
block|{
comment|/**    * Checks if it is possible to execute the specified operation.    *    * @param estimateWriteSize the write size that will be checked against the available quota    * @param estimateReadSize the read size that will be checked against the available quota    * @throws ThrottlingException thrown if not enough avialable resources to perform operation.    */
name|void
name|checkQuota
parameter_list|(
name|long
name|estimateWriteSize
parameter_list|,
name|long
name|estimateReadSize
parameter_list|)
throws|throws
name|ThrottlingException
function_decl|;
comment|/**    * Removes the specified write and read amount from the quota.    * At this point the write and read amount will be an estimate,    * that will be later adjusted with a consumeWrite()/consumeRead() call.    *    * @param writeSize the write size that will be removed from the current quota    * @param readSize the read size that will be removed from the current quota    */
name|void
name|grabQuota
parameter_list|(
name|long
name|writeSize
parameter_list|,
name|long
name|readSize
parameter_list|)
function_decl|;
comment|/**    * Removes or add back some write amount to the quota.    * (called at the end of an operation in case the estimate quota was off)    */
name|void
name|consumeWrite
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
comment|/**    * Removes or add back some read amount to the quota.    * (called at the end of an operation in case the estimate quota was off)    */
name|void
name|consumeRead
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
comment|/** @return true if the limiter is a noop */
name|boolean
name|isBypass
parameter_list|()
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

