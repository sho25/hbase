begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|InetSocketAddress
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
name|HBaseInterfaceAudience
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
name|RSRpcServices
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
name|Pair
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
name|security
operator|.
name|authorize
operator|.
name|PolicyProvider
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
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RpcServerInterface
block|{
name|void
name|start
parameter_list|()
function_decl|;
name|boolean
name|isStarted
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
name|void
name|setSocketSendBufSize
parameter_list|(
name|int
name|size
parameter_list|)
function_decl|;
name|InetSocketAddress
name|getListenerAddress
parameter_list|()
function_decl|;
comment|/**    * @deprecated As of release 1.3, this will be removed in HBase 3.0    */
annotation|@
name|Deprecated
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|BlockingService
name|service
parameter_list|,
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|CellScanner
name|cellScanner
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
comment|/**    * @deprecated As of release 2.0, this will be removed in HBase 3.0    */
annotation|@
name|Deprecated
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|BlockingService
name|service
parameter_list|,
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
name|CellScanner
name|cellScanner
parameter_list|,
name|long
name|receiveTime
parameter_list|,
name|MonitoredRPCHandler
name|status
parameter_list|,
name|long
name|startTime
parameter_list|,
name|int
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|Pair
argument_list|<
name|Message
argument_list|,
name|CellScanner
argument_list|>
name|call
parameter_list|(
name|RpcCall
name|call
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
name|HBaseRPCErrorHandler
name|getErrorHandler
parameter_list|()
function_decl|;
comment|/**    * Returns the metrics instance for reporting RPC call statistics    */
name|MetricsHBaseServer
name|getMetrics
parameter_list|()
function_decl|;
comment|/**    * Add/subtract from the current size of all outstanding calls.  Called on setup of a call to add    * call total size and then again at end of a call to remove the call size.    * @param diff Change (plus or minus)    */
name|void
name|addCallSize
parameter_list|(
name|long
name|diff
parameter_list|)
function_decl|;
comment|/**    * Refresh authentication manager policy.    * @param pp    */
annotation|@
name|VisibleForTesting
name|void
name|refreshAuthManager
parameter_list|(
name|PolicyProvider
name|pp
parameter_list|)
function_decl|;
name|RpcScheduler
name|getScheduler
parameter_list|()
function_decl|;
name|void
name|setRsRpcServices
parameter_list|(
name|RSRpcServices
name|rsRpcServices
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

