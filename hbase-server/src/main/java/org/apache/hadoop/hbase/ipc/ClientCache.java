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
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|io
operator|.
name|HbaseObjectWritable
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Cache a client using its socket factory as the hash key.  * Enables reuse/sharing of clients on a per SocketFactory basis. A client   * establishes certain configuration dependent characteristics like timeouts,   * tcp-keepalive (true or false), etc. For more details on the characteristics,  * look at {@link HBaseClient#HBaseClient(Class, Configuration, SocketFactory)}  * Creation of dynamic proxies to protocols creates the clients (and increments  * reference count once created), and stopping of the proxies leads to clearing  * out references and when the reference drops to zero, the cache mapping is   * cleared.   */
end_comment

begin_class
class|class
name|ClientCache
block|{
specifier|private
name|Map
argument_list|<
name|SocketFactory
argument_list|,
name|HBaseClient
argument_list|>
name|clients
init|=
operator|new
name|HashMap
argument_list|<
name|SocketFactory
argument_list|,
name|HBaseClient
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|ClientCache
parameter_list|()
block|{}
comment|/**    * Construct& cache an IPC client with the user-provided SocketFactory    * if no cached client exists.    *    * @param conf Configuration    * @param factory socket factory    * @return an IPC client    */
specifier|protected
specifier|synchronized
name|HBaseClient
name|getClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SocketFactory
name|factory
parameter_list|)
block|{
return|return
name|getClient
argument_list|(
name|conf
argument_list|,
name|factory
argument_list|,
name|HbaseObjectWritable
operator|.
name|class
argument_list|)
return|;
block|}
specifier|protected
specifier|synchronized
name|HBaseClient
name|getClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SocketFactory
name|factory
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|valueClass
parameter_list|)
block|{
name|HBaseClient
name|client
init|=
name|clients
operator|.
name|get
argument_list|(
name|factory
argument_list|)
decl_stmt|;
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
comment|// Make an hbase client instead of hadoop Client.
name|client
operator|=
operator|new
name|HBaseClient
argument_list|(
name|valueClass
argument_list|,
name|conf
argument_list|,
name|factory
argument_list|)
expr_stmt|;
name|clients
operator|.
name|put
argument_list|(
name|factory
argument_list|,
name|client
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|client
operator|.
name|incCount
argument_list|()
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
comment|/**    * Stop a RPC client connection    * A RPC client is closed only when its reference count becomes zero.    * @param client client to stop    */
specifier|protected
name|void
name|stopClient
parameter_list|(
name|HBaseClient
name|client
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|client
operator|.
name|decCount
argument_list|()
expr_stmt|;
if|if
condition|(
name|client
operator|.
name|isZeroReference
argument_list|()
condition|)
block|{
name|clients
operator|.
name|remove
argument_list|(
name|client
operator|.
name|getSocketFactory
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|client
operator|.
name|isZeroReference
argument_list|()
condition|)
block|{
name|client
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

