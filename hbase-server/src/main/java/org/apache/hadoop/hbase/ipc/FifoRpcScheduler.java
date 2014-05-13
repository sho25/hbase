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
name|DaemonThreadFactory
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
name|util
operator|.
name|concurrent
operator|.
name|ArrayBlockingQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadPoolExecutor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|CallRunner
import|;
end_import

begin_comment
comment|/**  * A very simple {@code }RpcScheduler} that serves incoming requests in order.  *  * This can be used for HMaster, where no prioritization is needed.  */
end_comment

begin_class
specifier|public
class|class
name|FifoRpcScheduler
implements|implements
name|RpcScheduler
block|{
specifier|private
specifier|final
name|int
name|handlerCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxQueueLength
decl_stmt|;
specifier|private
name|ThreadPoolExecutor
name|executor
decl_stmt|;
specifier|public
name|FifoRpcScheduler
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|handlerCount
parameter_list|)
block|{
name|this
operator|.
name|handlerCount
operator|=
name|handlerCount
expr_stmt|;
name|this
operator|.
name|maxQueueLength
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"ipc.server.max.callqueue.length"
argument_list|,
name|handlerCount
operator|*
name|RpcServer
operator|.
name|DEFAULT_MAX_CALLQUEUE_LENGTH_PER_HANDLER
argument_list|)
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
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|executor
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|handlerCount
argument_list|,
name|handlerCount
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|ArrayBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|(
name|maxQueueLength
argument_list|)
argument_list|,
operator|new
name|DaemonThreadFactory
argument_list|(
literal|"FifoRpcScheduler.handler"
argument_list|)
argument_list|,
operator|new
name|ThreadPoolExecutor
operator|.
name|CallerRunsPolicy
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|this
operator|.
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatch
parameter_list|(
specifier|final
name|CallRunner
name|task
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|task
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getGeneralQueueLength
parameter_list|()
block|{
return|return
name|executor
operator|.
name|getQueue
argument_list|()
operator|.
name|size
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
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReplicationQueueLength
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
name|getActiveRpcHandlerCount
parameter_list|()
block|{
return|return
name|executor
operator|.
name|getActiveCount
argument_list|()
return|;
block|}
block|}
end_class

end_unit

