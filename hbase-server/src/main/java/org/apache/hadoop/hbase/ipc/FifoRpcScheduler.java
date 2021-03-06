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
name|BlockingQueue
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|util
operator|.
name|Threads
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|internal
operator|.
name|StringUtil
import|;
end_import

begin_comment
comment|/**  * A very simple {@code }RpcScheduler} that serves incoming requests in order.  *  * This can be used for HMaster, where no prioritization is needed.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FifoRpcScheduler
extends|extends
name|RpcScheduler
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FifoRpcScheduler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|int
name|handlerCount
decl_stmt|;
specifier|protected
specifier|final
name|int
name|maxQueueLength
decl_stmt|;
specifier|protected
specifier|final
name|AtomicInteger
name|queueSize
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|protected
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
name|RpcScheduler
operator|.
name|IPC_SERVER_MAX_CALLQUEUE_LENGTH
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Using {} as user call queue; handlerCount={}; maxQueueLength={}"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|handlerCount
argument_list|,
name|maxQueueLength
argument_list|)
expr_stmt|;
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
argument_list|<>
argument_list|(
name|maxQueueLength
argument_list|)
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
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
specifier|private
specifier|static
class|class
name|FifoCallRunner
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|CallRunner
name|callRunner
decl_stmt|;
name|FifoCallRunner
parameter_list|(
name|CallRunner
name|cr
parameter_list|)
block|{
name|this
operator|.
name|callRunner
operator|=
name|cr
expr_stmt|;
block|}
name|CallRunner
name|getCallRunner
parameter_list|()
block|{
return|return
name|callRunner
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|callRunner
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
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
return|return
name|executeRpcCall
argument_list|(
name|executor
argument_list|,
name|queueSize
argument_list|,
name|task
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|executeRpcCall
parameter_list|(
specifier|final
name|ThreadPoolExecutor
name|executor
parameter_list|,
specifier|final
name|AtomicInteger
name|queueSize
parameter_list|,
specifier|final
name|CallRunner
name|task
parameter_list|)
block|{
comment|// Executors provide no offer, so make our own.
name|int
name|queued
init|=
name|queueSize
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxQueueLength
operator|>
literal|0
operator|&&
name|queued
operator|>=
name|maxQueueLength
condition|)
block|{
name|queueSize
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|executor
operator|.
name|execute
argument_list|(
operator|new
name|FifoCallRunner
argument_list|(
name|task
argument_list|)
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
name|setStatus
argument_list|(
name|RpcServer
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|run
argument_list|()
expr_stmt|;
name|queueSize
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
literal|true
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
annotation|@
name|Override
specifier|public
name|int
name|getActiveGeneralRpcHandlerCount
parameter_list|()
block|{
return|return
name|getActiveRpcHandlerCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActivePriorityRpcHandlerCount
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
name|getActiveReplicationRpcHandlerCount
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
name|getActiveMetaPriorityRpcHandlerCount
parameter_list|()
block|{
return|return
literal|0
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
literal|0
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
literal|0
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
name|int
name|getMetaPriorityQueueLength
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
name|String
name|queueName
init|=
literal|"Fifo Queue"
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|methodCount
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|methodSize
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|CallQueueInfo
name|callQueueInfo
init|=
operator|new
name|CallQueueInfo
argument_list|()
decl_stmt|;
name|callQueueInfo
operator|.
name|setCallMethodCount
argument_list|(
name|queueName
argument_list|,
name|methodCount
argument_list|)
expr_stmt|;
name|callQueueInfo
operator|.
name|setCallMethodSize
argument_list|(
name|queueName
argument_list|,
name|methodSize
argument_list|)
expr_stmt|;
name|updateMethodCountAndSizeByQueue
argument_list|(
name|executor
operator|.
name|getQueue
argument_list|()
argument_list|,
name|methodCount
argument_list|,
name|methodSize
argument_list|)
expr_stmt|;
return|return
name|callQueueInfo
return|;
block|}
specifier|protected
name|void
name|updateMethodCountAndSizeByQueue
parameter_list|(
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|queue
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|methodCount
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|methodSize
parameter_list|)
block|{
for|for
control|(
name|Runnable
name|r
range|:
name|queue
control|)
block|{
name|FifoCallRunner
name|mcr
init|=
operator|(
name|FifoCallRunner
operator|)
name|r
decl_stmt|;
name|RpcCall
name|rpcCall
init|=
name|mcr
operator|.
name|getCallRunner
argument_list|()
operator|.
name|getRpcCall
argument_list|()
decl_stmt|;
name|String
name|method
init|=
name|getCallMethod
argument_list|(
name|mcr
operator|.
name|getCallRunner
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtil
operator|.
name|isNullOrEmpty
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|method
operator|=
literal|"Unknown"
expr_stmt|;
block|}
name|long
name|size
init|=
name|rpcCall
operator|.
name|getSize
argument_list|()
decl_stmt|;
name|methodCount
operator|.
name|put
argument_list|(
name|method
argument_list|,
literal|1
operator|+
name|methodCount
operator|.
name|getOrDefault
argument_list|(
name|method
argument_list|,
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|methodSize
operator|.
name|put
argument_list|(
name|method
argument_list|,
name|size
operator|+
name|methodSize
operator|.
name|getOrDefault
argument_list|(
name|method
argument_list|,
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|String
name|getCallMethod
parameter_list|(
specifier|final
name|CallRunner
name|task
parameter_list|)
block|{
name|RpcCall
name|call
init|=
name|task
operator|.
name|getRpcCall
argument_list|()
decl_stmt|;
if|if
condition|(
name|call
operator|!=
literal|null
operator|&&
name|call
operator|.
name|getMethod
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|call
operator|.
name|getMethod
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

