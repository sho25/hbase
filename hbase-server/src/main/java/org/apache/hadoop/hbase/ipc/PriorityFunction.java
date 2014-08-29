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
comment|/**  * Function to figure priority of incoming request.  */
end_comment

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
name|PriorityFunction
block|{
comment|/**    * Returns the 'priority type' of the specified request.    * The returned value is mainly used to select the dispatch queue.    * @param header    * @param param    * @return Priority of this request.    */
name|int
name|getPriority
parameter_list|(
name|RequestHeader
name|header
parameter_list|,
name|Message
name|param
parameter_list|)
function_decl|;
comment|/**    * Returns the deadline of the specified request.    * The returned value is used to sort the dispatch queue.    * @param header    * @param param    * @return Deadline of this request. 0 now, otherwise msec of 'delay'    */
name|long
name|getDeadline
parameter_list|(
name|RequestHeader
name|header
parameter_list|,
name|Message
name|param
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

