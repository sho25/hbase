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
name|client
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
name|ipc
operator|.
name|HBaseRpcController
import|;
end_import

begin_comment
comment|/**  * Implementations make an rpc call against a RegionService via a protobuf Service.  * Implement #rpcCall(RpcController) and then call {@link #call(int)} to  * trigger the rpc. The {@link #call(int)} eventually invokes your  * #rpcCall(RpcController) meanwhile saving you having to write a bunch of  * boilerplate. The {@link #call(int)} implementation is from {@link RpcRetryingCaller} so rpcs are  * retried on fail.  *  *<p>TODO: this class is actually tied to one region, because most of the paths make use of  *       the regioninfo part of location when building requests. The only reason it works for  *       multi-region requests (e.g. batch) is that they happen to not use the region parts.  *       This could be done cleaner (e.g. having a generic parameter and 2 derived classes,  *       RegionCallable and actual RegionServerCallable with ServerName.  * @param<T> the class that the ServerCallable handles  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|NoncedRegionServerCallable
parameter_list|<
name|T
parameter_list|>
extends|extends
name|ClientServiceCallable
argument_list|<
name|T
argument_list|>
block|{
specifier|private
specifier|final
name|long
name|nonce
decl_stmt|;
comment|/**    * @param connection Connection to use.    * @param tableName Table name to which<code>row</code> belongs.    * @param row The row we want in<code>tableName</code>.    */
specifier|public
name|NoncedRegionServerCallable
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|HBaseRpcController
name|rpcController
parameter_list|,
name|int
name|priority
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|row
argument_list|,
name|rpcController
argument_list|,
name|priority
argument_list|)
expr_stmt|;
name|this
operator|.
name|nonce
operator|=
name|getConnection
argument_list|()
operator|.
name|getNonceGenerator
argument_list|()
operator|.
name|newNonce
argument_list|()
expr_stmt|;
block|}
name|long
name|getNonceGroup
parameter_list|()
block|{
return|return
name|getConnection
argument_list|()
operator|.
name|getNonceGenerator
argument_list|()
operator|.
name|getNonceGroup
argument_list|()
return|;
block|}
name|long
name|getNonce
parameter_list|()
block|{
return|return
name|this
operator|.
name|nonce
return|;
block|}
block|}
end_class

end_unit

