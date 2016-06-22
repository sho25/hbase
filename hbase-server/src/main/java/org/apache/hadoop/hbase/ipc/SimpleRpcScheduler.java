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
name|Comparator
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
name|atomic
operator|.
name|AtomicLong
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
name|BoundedPriorityBlockingQueue
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
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_READ_SHARE_CONF_KEY
init|=
literal|"hbase.ipc.server.callqueue.read.ratio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_SCAN_SHARE_CONF_KEY
init|=
literal|"hbase.ipc.server.callqueue.scan.ratio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_HANDLER_FACTOR_CONF_KEY
init|=
literal|"hbase.ipc.server.callqueue.handler.factor"
decl_stmt|;
comment|/**    * The default, 'fifo', has the least friction but is dumb.    * If set to 'deadline', uses a priority queue and deprioritizes long-running scans. Sorting by    * priority comes at a cost, reduced throughput.    */
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_TYPE_CODEL_CONF_VALUE
init|=
literal|"codel"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_TYPE_DEADLINE_CONF_VALUE
init|=
literal|"deadline"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_TYPE_FIFO_CONF_VALUE
init|=
literal|"fifo"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_TYPE_CONF_KEY
init|=
literal|"hbase.ipc.server.callqueue.type"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_TYPE_CONF_DEFAULT
init|=
name|CALL_QUEUE_TYPE_FIFO_CONF_VALUE
decl_stmt|;
comment|/** max delay in msec used to bound the deprioritized requests */
specifier|public
specifier|static
specifier|final
name|String
name|QUEUE_MAX_CALL_DELAY_CONF_KEY
init|=
literal|"hbase.ipc.server.queue.max.call.delay"
decl_stmt|;
comment|// These 3 are only used by Codel executor
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_CODEL_TARGET_DELAY
init|=
literal|"hbase.ipc.server.callqueue.codel.target.delay"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_CODEL_INTERVAL
init|=
literal|"hbase.ipc.server.callqueue.codel.interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_CODEL_LIFO_THRESHOLD
init|=
literal|"hbase.ipc.server.callqueue.codel.lifo.threshold"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|CALL_QUEUE_CODEL_DEFAULT_TARGET_DELAY
init|=
literal|100
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|CALL_QUEUE_CODEL_DEFAULT_INTERVAL
init|=
literal|100
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|double
name|CALL_QUEUE_CODEL_DEFAULT_LIFO_THRESHOLD
init|=
literal|0.8
decl_stmt|;
specifier|private
name|AtomicLong
name|numGeneralCallsDropped
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
name|AtomicLong
name|numLifoModeSwitches
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
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
name|CALL_QUEUE_TYPE_CONF_KEY
argument_list|,
name|CALL_QUEUE_TYPE_DEADLINE_CONF_VALUE
argument_list|)
decl_stmt|;
if|if
condition|(
name|isCodelQueueType
argument_list|(
name|callQueueType
argument_list|)
condition|)
block|{
comment|// update CoDel Scheduler tunables
name|int
name|codelTargetDelay
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CALL_QUEUE_CODEL_TARGET_DELAY
argument_list|,
name|CALL_QUEUE_CODEL_DEFAULT_TARGET_DELAY
argument_list|)
decl_stmt|;
name|int
name|codelInterval
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CALL_QUEUE_CODEL_INTERVAL
argument_list|,
name|CALL_QUEUE_CODEL_DEFAULT_INTERVAL
argument_list|)
decl_stmt|;
name|double
name|codelLifoThreshold
init|=
name|conf
operator|.
name|getDouble
argument_list|(
name|CALL_QUEUE_CODEL_LIFO_THRESHOLD
argument_list|,
name|CALL_QUEUE_CODEL_DEFAULT_LIFO_THRESHOLD
argument_list|)
decl_stmt|;
for|for
control|(
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|queue
range|:
name|callExecutor
operator|.
name|getQueues
argument_list|()
control|)
block|{
if|if
condition|(
name|queue
operator|instanceof
name|AdaptiveLifoCoDelCallQueue
condition|)
block|{
operator|(
operator|(
name|AdaptiveLifoCoDelCallQueue
operator|)
name|queue
operator|)
operator|.
name|updateTunables
argument_list|(
name|codelTargetDelay
argument_list|,
name|codelInterval
argument_list|,
name|codelLifoThreshold
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Comparator used by the "normal callQueue" if DEADLINE_CALL_QUEUE_CONF_KEY is set to true.    * It uses the calculated "deadline" e.g. to deprioritize long-running job    *    * If multiple requests have the same deadline BoundedPriorityBlockingQueue will order them in    * FIFO (first-in-first-out) manner.    */
specifier|private
specifier|static
class|class
name|CallPriorityComparator
implements|implements
name|Comparator
argument_list|<
name|CallRunner
argument_list|>
block|{
specifier|private
specifier|final
specifier|static
name|int
name|DEFAULT_MAX_CALL_DELAY
init|=
literal|5000
decl_stmt|;
specifier|private
specifier|final
name|PriorityFunction
name|priority
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxDelay
decl_stmt|;
specifier|public
name|CallPriorityComparator
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|PriorityFunction
name|priority
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
name|this
operator|.
name|maxDelay
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|QUEUE_MAX_CALL_DELAY_CONF_KEY
argument_list|,
name|DEFAULT_MAX_CALL_DELAY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|CallRunner
name|a
parameter_list|,
name|CallRunner
name|b
parameter_list|)
block|{
name|RpcServer
operator|.
name|Call
name|callA
init|=
name|a
operator|.
name|getCall
argument_list|()
decl_stmt|;
name|RpcServer
operator|.
name|Call
name|callB
init|=
name|b
operator|.
name|getCall
argument_list|()
decl_stmt|;
name|long
name|deadlineA
init|=
name|priority
operator|.
name|getDeadline
argument_list|(
name|callA
operator|.
name|getHeader
argument_list|()
argument_list|,
name|callA
operator|.
name|param
argument_list|)
decl_stmt|;
name|long
name|deadlineB
init|=
name|priority
operator|.
name|getDeadline
argument_list|(
name|callB
operator|.
name|getHeader
argument_list|()
argument_list|,
name|callB
operator|.
name|param
argument_list|)
decl_stmt|;
name|deadlineA
operator|=
name|callA
operator|.
name|timestamp
operator|+
name|Math
operator|.
name|min
argument_list|(
name|deadlineA
argument_list|,
name|maxDelay
argument_list|)
expr_stmt|;
name|deadlineB
operator|=
name|callB
operator|.
name|timestamp
operator|+
name|Math
operator|.
name|min
argument_list|(
name|deadlineB
argument_list|,
name|maxDelay
argument_list|)
expr_stmt|;
return|return
call|(
name|int
call|)
argument_list|(
name|deadlineA
operator|-
name|deadlineB
argument_list|)
return|;
block|}
block|}
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
name|CALL_QUEUE_TYPE_CONF_KEY
argument_list|,
name|CALL_QUEUE_TYPE_FIFO_CONF_VALUE
argument_list|)
decl_stmt|;
name|float
name|callqReadShare
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CALL_QUEUE_READ_SHARE_CONF_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|float
name|callqScanShare
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CALL_QUEUE_SCAN_SHARE_CONF_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|int
name|codelTargetDelay
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CALL_QUEUE_CODEL_TARGET_DELAY
argument_list|,
name|CALL_QUEUE_CODEL_DEFAULT_TARGET_DELAY
argument_list|)
decl_stmt|;
name|int
name|codelInterval
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CALL_QUEUE_CODEL_INTERVAL
argument_list|,
name|CALL_QUEUE_CODEL_DEFAULT_INTERVAL
argument_list|)
decl_stmt|;
name|double
name|codelLifoThreshold
init|=
name|conf
operator|.
name|getDouble
argument_list|(
name|CALL_QUEUE_CODEL_LIFO_THRESHOLD
argument_list|,
name|CALL_QUEUE_CODEL_DEFAULT_LIFO_THRESHOLD
argument_list|)
decl_stmt|;
name|float
name|callQueuesHandlersFactor
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CALL_QUEUE_HANDLER_FACTOR_CONF_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|int
name|numCallQueues
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
name|handlerCount
operator|*
name|callQueuesHandlersFactor
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using "
operator|+
name|callQueueType
operator|+
literal|" as user call queue; numCallQueues="
operator|+
name|numCallQueues
operator|+
literal|"; callQReadShare="
operator|+
name|callqReadShare
operator|+
literal|", callQScanShare="
operator|+
name|callqScanShare
argument_list|)
expr_stmt|;
if|if
condition|(
name|numCallQueues
operator|>
literal|1
operator|&&
name|callqReadShare
operator|>
literal|0
condition|)
block|{
comment|// multiple read/write queues
if|if
condition|(
name|isDeadlineQueueType
argument_list|(
name|callQueueType
argument_list|)
condition|)
block|{
name|CallPriorityComparator
name|callPriority
init|=
operator|new
name|CallPriorityComparator
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|priority
argument_list|)
decl_stmt|;
name|callExecutor
operator|=
operator|new
name|RWQueueRpcExecutor
argument_list|(
literal|"DeadlineRWQ.default"
argument_list|,
name|handlerCount
argument_list|,
name|numCallQueues
argument_list|,
name|callqReadShare
argument_list|,
name|callqScanShare
argument_list|,
name|maxQueueLength
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|,
name|BoundedPriorityBlockingQueue
operator|.
name|class
argument_list|,
name|callPriority
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isCodelQueueType
argument_list|(
name|callQueueType
argument_list|)
condition|)
block|{
name|Object
index|[]
name|callQueueInitArgs
init|=
block|{
name|maxQueueLength
block|,
name|codelTargetDelay
block|,
name|codelInterval
block|,
name|codelLifoThreshold
block|,
name|numGeneralCallsDropped
block|,
name|numLifoModeSwitches
block|}
decl_stmt|;
name|callExecutor
operator|=
operator|new
name|RWQueueRpcExecutor
argument_list|(
literal|"CodelRWQ.default"
argument_list|,
name|handlerCount
argument_list|,
name|numCallQueues
argument_list|,
name|callqReadShare
argument_list|,
name|callqScanShare
argument_list|,
name|AdaptiveLifoCoDelCallQueue
operator|.
name|class
argument_list|,
name|callQueueInitArgs
argument_list|,
name|AdaptiveLifoCoDelCallQueue
operator|.
name|class
argument_list|,
name|callQueueInitArgs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// FifoWFPBQ = FastPathBalancedQueueRpcExecutor
name|callExecutor
operator|=
operator|new
name|RWQueueRpcExecutor
argument_list|(
literal|"FifoRWQ.default"
argument_list|,
name|handlerCount
argument_list|,
name|numCallQueues
argument_list|,
name|callqReadShare
argument_list|,
name|callqScanShare
argument_list|,
name|maxQueueLength
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// multiple queues
if|if
condition|(
name|isDeadlineQueueType
argument_list|(
name|callQueueType
argument_list|)
condition|)
block|{
name|CallPriorityComparator
name|callPriority
init|=
operator|new
name|CallPriorityComparator
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|priority
argument_list|)
decl_stmt|;
name|callExecutor
operator|=
operator|new
name|BalancedQueueRpcExecutor
argument_list|(
literal|"DeadlineBQ.default"
argument_list|,
name|handlerCount
argument_list|,
name|numCallQueues
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|,
name|BoundedPriorityBlockingQueue
operator|.
name|class
argument_list|,
name|maxQueueLength
argument_list|,
name|callPriority
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isCodelQueueType
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
literal|"CodelFPBQ.default"
argument_list|,
name|handlerCount
argument_list|,
name|numCallQueues
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|,
name|AdaptiveLifoCoDelCallQueue
operator|.
name|class
argument_list|,
name|maxQueueLength
argument_list|,
name|codelTargetDelay
argument_list|,
name|codelInterval
argument_list|,
name|codelLifoThreshold
argument_list|,
name|numGeneralCallsDropped
argument_list|,
name|numLifoModeSwitches
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// FifoWFPBQ = FastPathBalancedQueueRpcExecutor
name|callExecutor
operator|=
operator|new
name|FastPathBalancedQueueRpcExecutor
argument_list|(
literal|"FifoWFPBQ.default"
argument_list|,
name|handlerCount
argument_list|,
name|numCallQueues
argument_list|,
name|maxQueueLength
argument_list|,
name|conf
argument_list|,
name|abortable
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
literal|"FifoWFPBQ.priority"
argument_list|,
name|priorityHandlerCount
argument_list|,
literal|2
argument_list|,
name|maxPriorityQueueLength
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
literal|"FifoWFPBQ.replication"
argument_list|,
name|replicationHandlerCount
argument_list|,
literal|1
argument_list|,
name|maxQueueLength
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
specifier|private
specifier|static
name|boolean
name|isDeadlineQueueType
parameter_list|(
specifier|final
name|String
name|callQueueType
parameter_list|)
block|{
return|return
name|callQueueType
operator|.
name|equals
argument_list|(
name|CALL_QUEUE_TYPE_DEADLINE_CONF_VALUE
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isCodelQueueType
parameter_list|(
specifier|final
name|String
name|callQueueType
parameter_list|)
block|{
return|return
name|callQueueType
operator|.
name|equals
argument_list|(
name|CALL_QUEUE_TYPE_CODEL_CONF_VALUE
argument_list|)
return|;
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
name|numGeneralCallsDropped
operator|.
name|get
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
name|numLifoModeSwitches
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

