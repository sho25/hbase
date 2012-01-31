begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|base
operator|.
name|Function
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
name|VersionedProtocol
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
name|monitoring
operator|.
name|MonitoredRPCHandler
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
name|InetSocketAddress
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_interface
specifier|public
interface|interface
name|RpcServer
block|{
name|void
name|setSocketSendBufSize
parameter_list|(
name|int
name|size
parameter_list|)
function_decl|;
name|void
name|start
parameter_list|()
function_decl|;
name|void
name|stop
parameter_list|()
function_decl|;
name|void
name|join
parameter_list|()
throws|throws
name|InterruptedException
function_decl|;
name|InetSocketAddress
name|getListenerAddress
parameter_list|()
function_decl|;
comment|/** Called for each call.    * @param param writable parameter    * @param receiveTime time    * @return Writable    * @throws java.io.IOException e    */
name|Writable
name|call
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|VersionedProtocol
argument_list|>
name|protocol
parameter_list|,
name|Writable
name|param
parameter_list|,
name|long
name|receiveTime
parameter_list|,
name|MonitoredRPCHandler
name|status
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|setErrorHandler
parameter_list|(
name|HBaseRPCErrorHandler
name|handler
parameter_list|)
function_decl|;
name|void
name|setQosFunction
parameter_list|(
name|Function
argument_list|<
name|Writable
argument_list|,
name|Integer
argument_list|>
name|newFunc
parameter_list|)
function_decl|;
name|void
name|openServer
parameter_list|()
function_decl|;
name|void
name|startThreads
parameter_list|()
function_decl|;
comment|/**    * Needed for delayed calls.  We need to be able to store the current call    * so that we can complete it later.    * @return Call the server is currently handling.    */
name|Delayable
name|getCurrentCall
parameter_list|()
function_decl|;
comment|/**    * Returns the metrics instance for reporting RPC call statistics    */
name|HBaseRpcMetrics
name|getRpcMetrics
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

