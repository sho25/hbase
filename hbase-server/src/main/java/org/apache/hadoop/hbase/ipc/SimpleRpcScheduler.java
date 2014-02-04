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
name|List
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
name|LinkedBlockingQueue
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
name|HConstants
import|;
end_import

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
name|Strings
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * A scheduler that maintains isolated handler pools for general, high-priority and replication  * requests.  */
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
name|SimpleRpcScheduler
implements|implements
name|RpcScheduler
block|{
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|int
name|handlerCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|priorityHandlerCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|replicationHandlerCount
decl_stmt|;
specifier|private
specifier|final
name|PriorityFunction
name|priority
decl_stmt|;
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|callQueue
decl_stmt|;
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|priorityCallQueue
decl_stmt|;
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|replicationQueue
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|running
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Thread
argument_list|>
name|handlers
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|/** What level a high priority call is at. */
specifier|private
specifier|final
name|int
name|highPriorityLevel
decl_stmt|;
comment|/**    * @param conf    * @param handlerCount the number of handler threads that will be used to process calls    * @param priorityHandlerCount How many threads for priority handling.    * @param replicationHandlerCount How many threads for replication handling.    * @param highPriorityLevel    * @param priority Function to extract request priority.    */
specifier|public
name|SimpleRpcScheduler
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|handlerCount
parameter_list|,
name|int
name|priorityHandlerCount
parameter_list|,
name|int
name|replicationHandlerCount
parameter_list|,
name|PriorityFunction
name|priority
parameter_list|,
name|int
name|highPriorityLevel
parameter_list|)
block|{
name|int
name|maxQueueLength
init|=
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
decl_stmt|;
name|this
operator|.
name|handlerCount
operator|=
name|handlerCount
expr_stmt|;
name|this
operator|.
name|priorityHandlerCount
operator|=
name|priorityHandlerCount
expr_stmt|;
name|this
operator|.
name|replicationHandlerCount
operator|=
name|replicationHandlerCount
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
name|this
operator|.
name|highPriorityLevel
operator|=
name|highPriorityLevel
expr_stmt|;
name|this
operator|.
name|callQueue
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|(
name|maxQueueLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|priorityCallQueue
operator|=
name|priorityHandlerCount
operator|>
literal|0
condition|?
operator|new
name|LinkedBlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|(
name|maxQueueLength
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|replicationQueue
operator|=
name|replicationHandlerCount
operator|>
literal|0
condition|?
operator|new
name|LinkedBlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|(
name|maxQueueLength
argument_list|)
else|:
literal|null
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
name|this
operator|.
name|port
operator|=
name|context
operator|.
name|getListenerAddress
argument_list|()
operator|.
name|getPort
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
name|running
operator|=
literal|true
expr_stmt|;
name|startHandlers
argument_list|(
name|handlerCount
argument_list|,
name|callQueue
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|priorityCallQueue
operator|!=
literal|null
condition|)
block|{
name|startHandlers
argument_list|(
name|priorityHandlerCount
argument_list|,
name|priorityCallQueue
argument_list|,
literal|"Priority."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|replicationQueue
operator|!=
literal|null
condition|)
block|{
name|startHandlers
argument_list|(
name|replicationHandlerCount
argument_list|,
name|replicationQueue
argument_list|,
literal|"Replication."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|startHandlers
parameter_list|(
name|int
name|handlerCount
parameter_list|,
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|callQueue
parameter_list|,
name|String
name|threadNamePrefix
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|handlerCount
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
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
name|consumerLoop
argument_list|(
name|callQueue
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|t
operator|.
name|setName
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|threadNamePrefix
argument_list|)
operator|+
literal|"RpcServer.handler="
operator|+
name|i
operator|+
literal|",port="
operator|+
name|port
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|handlers
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|running
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|Thread
name|handler
range|:
name|handlers
control|)
block|{
name|handler
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatch
parameter_list|(
name|CallRunner
name|callTask
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|RpcServer
operator|.
name|Call
name|call
init|=
name|callTask
operator|.
name|getCall
argument_list|()
decl_stmt|;
name|int
name|level
init|=
name|priority
operator|.
name|getPriority
argument_list|(
name|call
operator|.
name|header
argument_list|,
name|call
operator|.
name|param
argument_list|)
decl_stmt|;
if|if
condition|(
name|priorityCallQueue
operator|!=
literal|null
operator|&&
name|level
operator|>
name|highPriorityLevel
condition|)
block|{
name|priorityCallQueue
operator|.
name|put
argument_list|(
name|callTask
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|replicationQueue
operator|!=
literal|null
operator|&&
name|level
operator|==
name|HConstants
operator|.
name|REPLICATION_QOS
condition|)
block|{
name|replicationQueue
operator|.
name|put
argument_list|(
name|callTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|callQueue
operator|.
name|put
argument_list|(
name|callTask
argument_list|)
expr_stmt|;
comment|// queue the call; maybe blocked here
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getGeneralQueueLength
parameter_list|()
block|{
return|return
name|callQueue
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
name|priorityCallQueue
operator|==
literal|null
condition|?
literal|0
else|:
name|priorityCallQueue
operator|.
name|size
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
return|return
name|replicationQueue
operator|==
literal|null
condition|?
literal|0
else|:
name|replicationQueue
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
name|void
name|consumerLoop
parameter_list|(
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|myQueue
parameter_list|)
block|{
while|while
condition|(
name|running
condition|)
block|{
try|try
block|{
name|CallRunner
name|task
init|=
name|myQueue
operator|.
name|take
argument_list|()
decl_stmt|;
name|task
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

