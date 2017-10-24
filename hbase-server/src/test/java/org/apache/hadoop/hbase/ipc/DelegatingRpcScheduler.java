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

begin_class
specifier|public
class|class
name|DelegatingRpcScheduler
extends|extends
name|RpcScheduler
block|{
specifier|protected
name|RpcScheduler
name|delegate
decl_stmt|;
specifier|public
name|DelegatingRpcScheduler
parameter_list|(
name|RpcScheduler
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|delegate
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|delegate
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
name|delegate
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReplicationQueueLength
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getReplicationQueueLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriorityQueueLength
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getPriorityQueueLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getGeneralQueueLength
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getGeneralQueueLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveRpcHandlerCount
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getActiveRpcHandlerCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|dispatch
parameter_list|(
name|CallRunner
name|task
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|delegate
operator|.
name|dispatch
argument_list|(
name|task
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumGeneralCallsDropped
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getNumGeneralCallsDropped
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumLifoModeSwitches
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getNumLifoModeSwitches
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getWriteQueueLength
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReadQueueLength
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getScanQueueLength
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveWriteRpcHandlerCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveReadRpcHandlerCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveScanRpcHandlerCount
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|CallQueueInfo
name|getCallQueueInfo
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getCallQueueInfo
argument_list|()
return|;
block|}
block|}
end_class

end_unit

