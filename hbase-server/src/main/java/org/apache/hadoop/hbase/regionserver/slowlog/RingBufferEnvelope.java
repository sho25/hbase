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
name|regionserver
operator|.
name|slowlog
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
name|ipc
operator|.
name|RpcCall
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
comment|/**  * An envelope to carry payload in the slow log ring buffer that serves as online buffer  * to provide latest TooSlowLog  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|RingBufferEnvelope
block|{
specifier|private
name|RpcLogDetails
name|rpcLogDetails
decl_stmt|;
comment|/**    * Load the Envelope with {@link RpcCall}    *    * @param rpcLogDetails all details of rpc call that would be useful for ring buffer    *   consumers    */
specifier|public
name|void
name|load
parameter_list|(
name|RpcLogDetails
name|rpcLogDetails
parameter_list|)
block|{
name|this
operator|.
name|rpcLogDetails
operator|=
name|rpcLogDetails
expr_stmt|;
block|}
comment|/**    * Retrieve current rpcCall details {@link RpcLogDetails} available on Envelope and    * free up the Envelope    *    * @return Retrieve rpc log details    */
specifier|public
name|RpcLogDetails
name|getPayload
parameter_list|()
block|{
specifier|final
name|RpcLogDetails
name|rpcLogDetails
init|=
name|this
operator|.
name|rpcLogDetails
decl_stmt|;
name|this
operator|.
name|rpcLogDetails
operator|=
literal|null
expr_stmt|;
return|return
name|rpcLogDetails
return|;
block|}
block|}
end_class

end_unit

