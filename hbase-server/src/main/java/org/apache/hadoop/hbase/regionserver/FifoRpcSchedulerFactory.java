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
name|regionserver
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
name|Abortable
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
name|ipc
operator|.
name|FifoRpcScheduler
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
name|PriorityFunction
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
name|RpcScheduler
import|;
end_import

begin_comment
comment|/**  * Factory to use when you want to use the {@link FifoRpcScheduler}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|FifoRpcSchedulerFactory
implements|implements
name|RpcSchedulerFactory
block|{
annotation|@
name|Override
specifier|public
name|RpcScheduler
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|PriorityFunction
name|priority
parameter_list|,
name|Abortable
name|server
parameter_list|)
block|{
name|int
name|handlerCount
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_HANDLER_COUNT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGION_SERVER_HANDLER_COUNT
argument_list|)
decl_stmt|;
return|return
operator|new
name|FifoRpcScheduler
argument_list|(
name|conf
argument_list|,
name|handlerCount
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
annotation|@
name|Override
specifier|public
name|RpcScheduler
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|PriorityFunction
name|priority
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|conf
argument_list|,
name|priority
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

