begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
package|;
end_package

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

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ZKMetricsListener
block|{
comment|/**    * An AUTHFAILED Exception was seen.    */
name|void
name|registerAuthFailedException
parameter_list|()
function_decl|;
comment|/**    * A CONNECTIONLOSS Exception was seen.    */
name|void
name|registerConnectionLossException
parameter_list|()
function_decl|;
comment|/**    * A DATAINCONSISTENCY Exception was seen.    */
name|void
name|registerDataInconsistencyException
parameter_list|()
function_decl|;
comment|/**    * An INVALIDACL Exception was seen.    */
name|void
name|registerInvalidACLException
parameter_list|()
function_decl|;
comment|/**    * A NOAUTH Exception was seen.    */
name|void
name|registerNoAuthException
parameter_list|()
function_decl|;
comment|/**    * A OPERATIONTIMEOUT Exception was seen.    */
name|void
name|registerOperationTimeoutException
parameter_list|()
function_decl|;
comment|/**    * A RUNTIMEINCONSISTENCY Exception was seen.    */
name|void
name|registerRuntimeInconsistencyException
parameter_list|()
function_decl|;
comment|/**    * A SESSIONEXPIRED Exception was seen.    */
name|void
name|registerSessionExpiredException
parameter_list|()
function_decl|;
comment|/**    * A SYSTEMERROR Exception was seen.    */
name|void
name|registerSystemErrorException
parameter_list|()
function_decl|;
comment|/**    * A ZooKeeper API Call failed.    */
name|void
name|registerFailedZKCall
parameter_list|()
function_decl|;
comment|/**    * Register the latency incurred for read operations.    */
name|void
name|registerReadOperationLatency
parameter_list|(
name|long
name|latency
parameter_list|)
function_decl|;
comment|/**    * Register the latency incurred for write operations.    */
name|void
name|registerWriteOperationLatency
parameter_list|(
name|long
name|latency
parameter_list|)
function_decl|;
comment|/**    * Register the latency incurred for sync operations.    */
name|void
name|registerSyncOperationLatency
parameter_list|(
name|long
name|latency
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

