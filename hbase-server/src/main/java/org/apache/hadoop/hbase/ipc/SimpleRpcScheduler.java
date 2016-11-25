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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|ConfigurationObserver
import|;
end_import

begin_comment
comment|/**  * The default scheduler. Configurable. Maintains isolated handler pools for general ('default'),  * high-priority ('priority'), and replication ('replication') requests. Default behavior is to  * balance the requests across handlers. Add configs to enable balancing by read vs writes, etc.  * See below article for explanation of options.  * @see<a href="http://blog.cloudera.com/blog/2014/12/new-in-cdh-5-2-improvements-for-running-multiple-workloads-on-a-single-hbase-cluster/">Overview on Request Queuing</a>  */
end_comment

begin_class
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
class|class
name|SimpleRpcScheduler
extends|extends
name|RpcScheduler
implements|implements
name|ConfigurationObserver
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SimpleRpcScheduler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|PriorityFunction
name|priority
decl_stmt|;
specifier|private
specifier|final
name|RpcExecutor
name|callExecutor
decl_stmt|;
specifier|private
specifier|final
name|RpcExecutor
name|priorityExecutor
decl_stmt|;
specifier|private
specifier|final
name|RpcExecutor
name|replicationExecutor
decl_stmt|;
comment|/** What level a high priority call is at. */
specifier|private
specifier|final
name|int
name|highPriorityLevel
decl_stmt|;
specifier|private
name|Abortable
name|abortable
init|=
literal|null
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
name|Abortable
name|server
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
decl_stmt|;
name|int
name|maxPriorityQueueLength
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|RpcScheduler
operator|.
name|IPC_SERVER_PRIORITY_MAX_CALLQUEUE_LENGTH
argument_list|,
name|maxQueueLength
argument_list|)
decl_stmt|;
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
name|abortable
operator|=
name|server
expr_stmt|;
name|String
name|callQueueType
init|=
name|conf
operator|.
name|get
argument_list|(
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_CONF_KEY
argument_list|,
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_CONF_DEFAULT
argument_list|)
decl_stmt|;
name|float
name|callqReadShare
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|RWQueueRpcExecutor
operator|.
name|CALL_QUEUE_READ_SHARE_CONF_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|callqReadShare
operator|>
literal|0
condition|)
block|{
comment|// at least 1 read handler and 1 write handler
name|callExecutor
operator|=
operator|new
name|RWQueueRpcExecutor
argument_list|(
literal|"deafult.RWQ"
argument_list|,
name|Math
operator|.
name|max
argument_list|(
literal|2
argument_list|,
name|handlerCount
argument_list|)
argument_list|,
name|maxQueueLength
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|,
name|server
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|RpcExecutor
operator|.
name|isFifoQueueType
argument_list|(
name|callQueueType
argument_list|)
condition|)
block|{
name|callExecutor
operator|=
operator|new
name|FastPathBalancedQueueRpcExecutor
argument_list|(
literal|"deafult.FPBQ"
argument_list|,
name|handlerCount
argument_list|,
name|maxPriorityQueueLength
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|,
name|server
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|callExecutor
operator|=
operator|new
name|BalancedQueueRpcExecutor
argument_list|(
literal|"deafult.BQ"
argument_list|,
name|handlerCount
argument_list|,
name|maxQueueLength
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|,
name|server
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Create 2 queues to help priorityExecutor be more scalable.
name|this
operator|.
name|priorityExecutor
operator|=
name|priorityHandlerCount
operator|>
literal|0
condition|?
operator|new
name|FastPathBalancedQueueRpcExecutor
argument_list|(
literal|"priority.FPBQ"
argument_list|,
name|priorityHandlerCount
argument_list|,
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_FIFO_CONF_VALUE
argument_list|,
name|maxPriorityQueueLength
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|replicationExecutor
operator|=
name|replicationHandlerCount
operator|>
literal|0
condition|?
operator|new
name|FastPathBalancedQueueRpcExecutor
argument_list|(
literal|"replication.FPBQ"
argument_list|,
name|replicationHandlerCount
argument_list|,
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_FIFO_CONF_VALUE
argument_list|,
name|maxQueueLength
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
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
name|this
argument_list|(
name|conf
argument_list|,
name|handlerCount
argument_list|,
name|priorityHandlerCount
argument_list|,
name|replicationHandlerCount
argument_list|,
name|priority
argument_list|,
literal|null
argument_list|,
name|highPriorityLevel
argument_list|)
expr_stmt|;
block|}
comment|/**    * Resize call queues;    * @param conf new configuration    */
annotation|@
name|Override
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|callExecutor
operator|.
name|resizeQueues
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|priorityExecutor
operator|!=
literal|null
condition|)
block|{
name|priorityExecutor
operator|.
name|resizeQueues
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|replicationExecutor
operator|!=
literal|null
condition|)
block|{
name|replicationExecutor
operator|.
name|resizeQueues
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|String
name|callQueueType
init|=
name|conf
operator|.
name|get
argument_list|(
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_CONF_KEY
argument_list|,
name|RpcExecutor
operator|.
name|CALL_QUEUE_TYPE_CONF_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|RpcExecutor
operator|.
name|isCodelQueueType
argument_list|(
name|callQueueType
argument_list|)
condition|)
block|{
name|callExecutor
operator|.
name|onConfigurationChange
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|callExecutor
operator|.
name|start
argument_list|(
name|port
argument_list|)
expr_stmt|;
if|if
condition|(
name|priorityExecutor
operator|!=
literal|null
condition|)
name|priorityExecutor
operator|.
name|start
argument_list|(
name|port
argument_list|)
expr_stmt|;
if|if
condition|(
name|replicationExecutor
operator|!=
literal|null
condition|)
name|replicationExecutor
operator|.
name|start
argument_list|(
name|port
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
name|callExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|priorityExecutor
operator|!=
literal|null
condition|)
name|priorityExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|replicationExecutor
operator|!=
literal|null
condition|)
name|replicationExecutor
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
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
name|getHeader
argument_list|()
argument_list|,
name|call
operator|.
name|param
argument_list|,
name|call
operator|.
name|getRequestUser
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|priorityExecutor
operator|!=
literal|null
operator|&&
name|level
operator|>
name|highPriorityLevel
condition|)
block|{
return|return
name|priorityExecutor
operator|.
name|dispatch
argument_list|(
name|callTask
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|replicationExecutor
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
return|return
name|replicationExecutor
operator|.
name|dispatch
argument_list|(
name|callTask
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|callExecutor
operator|.
name|dispatch
argument_list|(
name|callTask
argument_list|)
return|;
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
name|callExecutor
operator|.
name|getQueueLength
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
name|priorityExecutor
operator|==
literal|null
condition|?
literal|0
else|:
name|priorityExecutor
operator|.
name|getQueueLength
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
name|replicationExecutor
operator|==
literal|null
condition|?
literal|0
else|:
name|replicationExecutor
operator|.
name|getQueueLength
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
name|callExecutor
operator|.
name|getActiveHandlerCount
argument_list|()
operator|+
operator|(
name|priorityExecutor
operator|==
literal|null
condition|?
literal|0
else|:
name|priorityExecutor
operator|.
name|getActiveHandlerCount
argument_list|()
operator|)
operator|+
operator|(
name|replicationExecutor
operator|==
literal|null
condition|?
literal|0
else|:
name|replicationExecutor
operator|.
name|getActiveHandlerCount
argument_list|()
operator|)
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
name|callExecutor
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
name|callExecutor
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
name|callExecutor
operator|.
name|getWriteQueueLength
argument_list|()
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
name|callExecutor
operator|.
name|getReadQueueLength
argument_list|()
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
name|callExecutor
operator|.
name|getScanQueueLength
argument_list|()
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
name|callExecutor
operator|.
name|getActiveWriteHandlerCount
argument_list|()
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
name|callExecutor
operator|.
name|getActiveReadHandlerCount
argument_list|()
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
name|callExecutor
operator|.
name|getActiveScanHandlerCount
argument_list|()
return|;
block|}
block|}
end_class

end_unit

