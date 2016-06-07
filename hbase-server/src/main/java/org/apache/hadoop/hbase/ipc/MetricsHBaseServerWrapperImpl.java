begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_class
specifier|public
class|class
name|MetricsHBaseServerWrapperImpl
implements|implements
name|MetricsHBaseServerWrapper
block|{
specifier|private
name|RpcServer
name|server
decl_stmt|;
name|MetricsHBaseServerWrapperImpl
parameter_list|(
name|RpcServer
name|server
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
block|}
specifier|private
name|boolean
name|isServerStarted
parameter_list|()
block|{
return|return
name|this
operator|.
name|server
operator|!=
literal|null
operator|&&
name|this
operator|.
name|server
operator|.
name|isStarted
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTotalQueueSize
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|callQueueSize
operator|.
name|get
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
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|getScheduler
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|getScheduler
argument_list|()
operator|.
name|getGeneralQueueLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReplicationQueueLength
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|getScheduler
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|getScheduler
argument_list|()
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
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|getScheduler
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|getScheduler
argument_list|()
operator|.
name|getPriorityQueueLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumOpenConnections
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|connectionList
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|connectionList
operator|.
name|size
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
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|getScheduler
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|getScheduler
argument_list|()
operator|.
name|getActiveRpcHandlerCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumGeneralCallsDropped
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|getScheduler
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|getScheduler
argument_list|()
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
if|if
condition|(
operator|!
name|isServerStarted
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|getScheduler
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|server
operator|.
name|getScheduler
argument_list|()
operator|.
name|getNumLifoModeSwitches
argument_list|()
return|;
block|}
block|}
end_class

end_unit

