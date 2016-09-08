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
name|ipc
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

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
name|net
operator|.
name|SocketAddress
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|SocketFactory
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
name|conf
operator|.
name|Configuration
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
name|HConstants
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
name|MetricsConnection
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
name|net
operator|.
name|NetUtils
import|;
end_import

begin_comment
comment|/**  * Does RPC against a cluster. Manages connections per regionserver in the cluster.  *<p>  * See HBaseServer  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BlockingRpcClient
extends|extends
name|AbstractRpcClient
argument_list|<
name|BlockingRpcConnection
argument_list|>
block|{
specifier|protected
specifier|final
name|SocketFactory
name|socketFactory
decl_stmt|;
comment|// how to create sockets
comment|/**    * Used in test only. Construct an IPC client for the cluster {@code clusterId} with the default    * SocketFactory    */
annotation|@
name|VisibleForTesting
name|BlockingRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct an IPC client for the cluster {@code clusterId} with the default SocketFactory This    * method is called with reflection by the RpcClientFactory to create an instance    * @param conf configuration    * @param clusterId the cluster id    * @param localAddr client socket bind address.    * @param metrics the connection metrics    */
specifier|public
name|BlockingRpcClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|SocketAddress
name|localAddr
parameter_list|,
name|MetricsConnection
name|metrics
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|clusterId
argument_list|,
name|localAddr
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
name|this
operator|.
name|socketFactory
operator|=
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a connection. Can be overridden by a subclass for testing.    * @param remoteId - the ConnectionId to use for the connection creation.    */
specifier|protected
name|BlockingRpcConnection
name|createConnection
parameter_list|(
name|ConnectionId
name|remoteId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|BlockingRpcConnection
argument_list|(
name|this
argument_list|,
name|remoteId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeInternal
parameter_list|()
block|{   }
block|}
end_class

end_unit

