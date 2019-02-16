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
name|InetAddress
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
name|CellScanner
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
name|ByteBuffAllocator
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
name|RpcServer
operator|.
name|CallCleanup
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
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
name|RPCProtos
operator|.
name|RequestHeader
import|;
end_import

begin_comment
comment|/**  * Datastructure that holds all necessary to a method invocation and then afterward, carries the  * result.  * @since 2.0.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|NettyServerCall
extends|extends
name|ServerCall
argument_list|<
name|NettyServerRpcConnection
argument_list|>
block|{
name|NettyServerCall
parameter_list|(
name|int
name|id
parameter_list|,
name|BlockingService
name|service
parameter_list|,
name|MethodDescriptor
name|md
parameter_list|,
name|RequestHeader
name|header
parameter_list|,
name|Message
name|param
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|NettyServerRpcConnection
name|connection
parameter_list|,
name|long
name|size
parameter_list|,
name|InetAddress
name|remoteAddress
parameter_list|,
name|long
name|receiveTime
parameter_list|,
name|int
name|timeout
parameter_list|,
name|ByteBuffAllocator
name|bbAllocator
parameter_list|,
name|CellBlockBuilder
name|cellBlockBuilder
parameter_list|,
name|CallCleanup
name|reqCleanup
parameter_list|)
block|{
name|super
argument_list|(
name|id
argument_list|,
name|service
argument_list|,
name|md
argument_list|,
name|header
argument_list|,
name|param
argument_list|,
name|cellScanner
argument_list|,
name|connection
argument_list|,
name|size
argument_list|,
name|remoteAddress
argument_list|,
name|receiveTime
argument_list|,
name|timeout
argument_list|,
name|bbAllocator
argument_list|,
name|cellBlockBuilder
argument_list|,
name|reqCleanup
argument_list|)
expr_stmt|;
block|}
comment|/**    * If we have a response, and delay is not set, then respond immediately. Otherwise, do not    * respond to client. This is called by the RPC code in the context of the Handler thread.    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|sendResponseIfReady
parameter_list|()
throws|throws
name|IOException
block|{
comment|// set param null to reduce memory pressure
name|this
operator|.
name|param
operator|=
literal|null
expr_stmt|;
name|connection
operator|.
name|channel
operator|.
name|writeAndFlush
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

