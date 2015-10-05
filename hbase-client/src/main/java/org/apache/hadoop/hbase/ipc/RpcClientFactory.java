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
name|hbase
operator|.
name|util
operator|.
name|ReflectionUtils
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

begin_comment
comment|/**  * Factory to create a {@link org.apache.hadoop.hbase.ipc.RpcClient}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RpcClientFactory
block|{
specifier|public
specifier|static
specifier|final
name|String
name|CUSTOM_RPC_CLIENT_IMPL_CONF_KEY
init|=
literal|"hbase.rpc.client.impl"
decl_stmt|;
comment|/**    * Private Constructor    */
specifier|private
name|RpcClientFactory
parameter_list|()
block|{   }
comment|/** Helper method for tests only. Creates an {@code RpcClient} without metrics. */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|RpcClient
name|createClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
return|return
name|createClient
argument_list|(
name|conf
argument_list|,
name|clusterId
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Creates a new RpcClient by the class defined in the configuration or falls back to    * RpcClientImpl    * @param conf configuration    * @param clusterId the cluster id    * @param metrics the connection metrics    * @return newly created RpcClient    */
specifier|public
specifier|static
name|RpcClient
name|createClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|MetricsConnection
name|metrics
parameter_list|)
block|{
return|return
name|createClient
argument_list|(
name|conf
argument_list|,
name|clusterId
argument_list|,
literal|null
argument_list|,
name|metrics
argument_list|)
return|;
block|}
comment|/**    * Creates a new RpcClient by the class defined in the configuration or falls back to    * RpcClientImpl    * @param conf configuration    * @param clusterId the cluster id    * @param localAddr client socket bind address.    * @param metrics the connection metrics    * @return newly created RpcClient    */
specifier|public
specifier|static
name|RpcClient
name|createClient
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
name|String
name|rpcClientClass
init|=
name|conf
operator|.
name|get
argument_list|(
name|CUSTOM_RPC_CLIENT_IMPL_CONF_KEY
argument_list|,
name|AsyncRpcClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|rpcClientClass
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|,
name|String
operator|.
name|class
block|,
name|SocketAddress
operator|.
name|class
block|,
name|MetricsConnection
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|conf
block|,
name|clusterId
block|,
name|localAddr
block|,
name|metrics
block|}
argument_list|)
return|;
block|}
block|}
end_class

end_unit

