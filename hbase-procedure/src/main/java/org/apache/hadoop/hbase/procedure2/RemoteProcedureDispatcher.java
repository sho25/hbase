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
name|procedure2
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
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

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
name|Set
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
name|Callable
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
name|ConcurrentHashMap
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
name|DelayQueue
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
name|Future
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
name|FutureTask
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
name|AtomicBoolean
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
name|hadoop
operator|.
name|hbase
operator|.
name|procedure2
operator|.
name|util
operator|.
name|DelayedUtil
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
name|procedure2
operator|.
name|util
operator|.
name|DelayedUtil
operator|.
name|DelayedContainerWithTimestamp
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
name|procedure2
operator|.
name|util
operator|.
name|DelayedUtil
operator|.
name|DelayedWithTimeout
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
name|procedure2
operator|.
name|util
operator|.
name|StringUtils
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
name|EnvironmentEdgeManager
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
import|;
end_import

begin_comment
comment|/**  * A procedure dispatcher that aggregates and sends after elapsed time or after we hit  * count threshold. Creates its own threadpool to run RPCs with timeout.  *<ul>  *<li>Each server queue has a dispatch buffer</li>  *<li>Once the dispatch buffer reaches a threshold-size/time we send<li>  *</ul>  *<p>Call {@link #start()} and then {@link #submitTask(Callable)}. When done,  * call {@link #stop()}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|RemoteProcedureDispatcher
parameter_list|<
name|TEnv
parameter_list|,
name|TRemote
extends|extends
name|Comparable
parameter_list|<
name|TRemote
parameter_list|>
parameter_list|>
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
name|RemoteProcedureDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|THREAD_POOL_SIZE_CONF_KEY
init|=
literal|"hbase.procedure.remote.dispatcher.threadpool.size"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_THREAD_POOL_SIZE
init|=
literal|128
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DISPATCH_DELAY_CONF_KEY
init|=
literal|"hbase.procedure.remote.dispatcher.delay.msec"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_DISPATCH_DELAY
init|=
literal|150
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DISPATCH_MAX_QUEUE_SIZE_CONF_KEY
init|=
literal|"hbase.procedure.remote.dispatcher.max.queue.size"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_QUEUE_SIZE
init|=
literal|32
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|running
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|TRemote
argument_list|,
name|BufferNode
argument_list|>
name|nodeMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|TRemote
argument_list|,
name|BufferNode
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|operationDelay
decl_stmt|;
specifier|private
specifier|final
name|int
name|queueMaxSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|corePoolSize
decl_stmt|;
specifier|private
name|TimeoutExecutorThread
name|timeoutExecutor
decl_stmt|;
specifier|private
name|ThreadPoolExecutor
name|threadPool
decl_stmt|;
specifier|protected
name|RemoteProcedureDispatcher
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|corePoolSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|THREAD_POOL_SIZE_CONF_KEY
argument_list|,
name|DEFAULT_THREAD_POOL_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationDelay
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|DISPATCH_DELAY_CONF_KEY
argument_list|,
name|DEFAULT_DISPATCH_DELAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|queueMaxSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|DISPATCH_MAX_QUEUE_SIZE_CONF_KEY
argument_list|,
name|DEFAULT_MAX_QUEUE_SIZE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|start
parameter_list|()
block|{
if|if
condition|(
name|running
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Already running"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting procedure remote dispatcher; threads="
operator|+
name|this
operator|.
name|corePoolSize
operator|+
literal|", queueMaxSize="
operator|+
name|this
operator|.
name|queueMaxSize
operator|+
literal|", operationDelay="
operator|+
name|this
operator|.
name|operationDelay
argument_list|)
expr_stmt|;
comment|// Create the timeout executor
name|timeoutExecutor
operator|=
operator|new
name|TimeoutExecutorThread
argument_list|()
expr_stmt|;
name|timeoutExecutor
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Create the thread pool that will execute RPCs
name|threadPool
operator|=
name|Threads
operator|.
name|getBoundedCachedThreadPool
argument_list|(
name|corePoolSize
argument_list|,
literal|60L
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|getUncaughtExceptionHandler
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|running
operator|.
name|getAndSet
argument_list|(
literal|false
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping procedure remote dispatcher"
argument_list|)
expr_stmt|;
comment|// send stop signals
name|timeoutExecutor
operator|.
name|sendStopSignal
argument_list|()
expr_stmt|;
name|threadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|join
parameter_list|()
block|{
assert|assert
operator|!
name|running
operator|.
name|get
argument_list|()
operator|:
literal|"expected not running"
assert|;
comment|// wait the timeout executor
name|timeoutExecutor
operator|.
name|awaitTermination
argument_list|()
expr_stmt|;
name|timeoutExecutor
operator|=
literal|null
expr_stmt|;
comment|// wait for the thread pool to terminate
name|threadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|threadPool
operator|.
name|awaitTermination
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Waiting for thread-pool to terminate"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while waiting for thread-pool termination"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|UncaughtExceptionHandler
name|getUncaughtExceptionHandler
parameter_list|()
block|{
return|return
operator|new
name|UncaughtExceptionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to execute remote procedures "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
comment|// ============================================================================================
comment|//  Node Helpers
comment|// ============================================================================================
comment|/**    * Add a node that will be able to execute remote procedures    * @param key the node identifier    */
specifier|public
name|void
name|addNode
parameter_list|(
specifier|final
name|TRemote
name|key
parameter_list|)
block|{
assert|assert
name|key
operator|!=
literal|null
operator|:
literal|"Tried to add a node with a null key"
assert|;
specifier|final
name|BufferNode
name|newNode
init|=
operator|new
name|BufferNode
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|nodeMap
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|newNode
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a remote rpc. Be sure to check result for successful add.    * @param key the node identifier    * @return True if we successfully added the operation.    */
specifier|public
name|boolean
name|addOperationToNode
parameter_list|(
specifier|final
name|TRemote
name|key
parameter_list|,
name|RemoteProcedure
name|rp
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
comment|// Key is remote server name. Be careful. It could have been nulled by a concurrent
comment|// ServerCrashProcedure shutting down outstanding RPC requests. See remoteCallFailed.
return|return
literal|false
return|;
block|}
assert|assert
name|key
operator|!=
literal|null
operator|:
literal|"found null key for node"
assert|;
name|BufferNode
name|node
init|=
name|nodeMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|node
operator|.
name|add
argument_list|(
name|rp
argument_list|)
expr_stmt|;
comment|// Check our node still in the map; could have been removed by #removeNode.
return|return
name|nodeMap
operator|.
name|containsValue
argument_list|(
name|node
argument_list|)
return|;
block|}
comment|/**    * Remove a remote node    * @param key the node identifier    */
specifier|public
name|boolean
name|removeNode
parameter_list|(
specifier|final
name|TRemote
name|key
parameter_list|)
block|{
specifier|final
name|BufferNode
name|node
init|=
name|nodeMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|node
operator|.
name|abortOperationsInQueue
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// ============================================================================================
comment|//  Task Helpers
comment|// ============================================================================================
specifier|protected
name|Future
argument_list|<
name|Void
argument_list|>
name|submitTask
parameter_list|(
name|Callable
argument_list|<
name|Void
argument_list|>
name|task
parameter_list|)
block|{
return|return
name|threadPool
operator|.
name|submit
argument_list|(
name|task
argument_list|)
return|;
block|}
specifier|protected
name|Future
argument_list|<
name|Void
argument_list|>
name|submitTask
parameter_list|(
name|Callable
argument_list|<
name|Void
argument_list|>
name|task
parameter_list|,
name|long
name|delay
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
specifier|final
name|FutureTask
argument_list|<
name|Void
argument_list|>
name|futureTask
init|=
operator|new
name|FutureTask
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|timeoutExecutor
operator|.
name|add
argument_list|(
operator|new
name|DelayedTask
argument_list|(
name|futureTask
argument_list|,
name|delay
argument_list|,
name|unit
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|futureTask
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|remoteDispatch
parameter_list|(
name|TRemote
name|key
parameter_list|,
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|operations
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|void
name|abortPendingOperations
parameter_list|(
name|TRemote
name|key
parameter_list|,
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|operations
parameter_list|)
function_decl|;
comment|/**    * Data structure with reference to remote operation.    */
specifier|public
specifier|static
specifier|abstract
class|class
name|RemoteOperation
block|{
specifier|private
specifier|final
name|RemoteProcedure
name|remoteProcedure
decl_stmt|;
specifier|protected
name|RemoteOperation
parameter_list|(
specifier|final
name|RemoteProcedure
name|remoteProcedure
parameter_list|)
block|{
name|this
operator|.
name|remoteProcedure
operator|=
name|remoteProcedure
expr_stmt|;
block|}
specifier|public
name|RemoteProcedure
name|getRemoteProcedure
parameter_list|()
block|{
return|return
name|remoteProcedure
return|;
block|}
block|}
comment|/**    * Remote procedure reference.    */
specifier|public
interface|interface
name|RemoteProcedure
parameter_list|<
name|TEnv
parameter_list|,
name|TRemote
parameter_list|>
block|{
comment|/**      * For building the remote operation.      */
name|RemoteOperation
name|remoteCallBuild
parameter_list|(
name|TEnv
name|env
parameter_list|,
name|TRemote
name|remote
parameter_list|)
function_decl|;
comment|/**      * Called when the executeProcedure call is failed.      */
name|void
name|remoteCallFailed
parameter_list|(
name|TEnv
name|env
parameter_list|,
name|TRemote
name|remote
parameter_list|,
name|IOException
name|exception
parameter_list|)
function_decl|;
comment|/**      * Called when RS tells the remote procedure is succeeded through the      * {@code reportProcedureDone} method.      */
name|void
name|remoteOperationCompleted
parameter_list|(
name|TEnv
name|env
parameter_list|)
function_decl|;
comment|/**      * Called when RS tells the remote procedure is failed through the {@code reportProcedureDone}      * method.      * @param error the error message      */
name|void
name|remoteOperationFailed
parameter_list|(
name|TEnv
name|env
parameter_list|,
name|String
name|error
parameter_list|)
function_decl|;
block|}
comment|/**    * Account of what procedures are running on remote node.    * @param<TEnv>    * @param<TRemote>    */
specifier|public
interface|interface
name|RemoteNode
parameter_list|<
name|TEnv
parameter_list|,
name|TRemote
parameter_list|>
block|{
name|TRemote
name|getKey
parameter_list|()
function_decl|;
name|void
name|add
parameter_list|(
name|RemoteProcedure
argument_list|<
name|TEnv
argument_list|,
name|TRemote
argument_list|>
name|operation
parameter_list|)
function_decl|;
name|void
name|dispatch
parameter_list|()
function_decl|;
block|}
specifier|protected
name|ArrayListMultimap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|RemoteOperation
argument_list|>
name|buildAndGroupRequestByType
parameter_list|(
specifier|final
name|TEnv
name|env
parameter_list|,
specifier|final
name|TRemote
name|remote
parameter_list|,
specifier|final
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|remoteProcedures
parameter_list|)
block|{
specifier|final
name|ArrayListMultimap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|RemoteOperation
argument_list|>
name|requestByType
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|RemoteProcedure
name|proc
range|:
name|remoteProcedures
control|)
block|{
name|RemoteOperation
name|operation
init|=
name|proc
operator|.
name|remoteCallBuild
argument_list|(
name|env
argument_list|,
name|remote
argument_list|)
decl_stmt|;
name|requestByType
operator|.
name|put
argument_list|(
name|operation
operator|.
name|getClass
argument_list|()
argument_list|,
name|operation
argument_list|)
expr_stmt|;
block|}
return|return
name|requestByType
return|;
block|}
specifier|protected
parameter_list|<
name|T
extends|extends
name|RemoteOperation
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|fetchType
parameter_list|(
specifier|final
name|ArrayListMultimap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|RemoteOperation
argument_list|>
name|requestByType
parameter_list|,
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
block|{
return|return
operator|(
name|List
argument_list|<
name|T
argument_list|>
operator|)
name|requestByType
operator|.
name|removeAll
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|// ============================================================================================
comment|//  Timeout Helpers
comment|// ============================================================================================
specifier|private
specifier|final
class|class
name|TimeoutExecutorThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|DelayQueue
argument_list|<
name|DelayedWithTimeout
argument_list|>
name|queue
init|=
operator|new
name|DelayQueue
argument_list|<
name|DelayedWithTimeout
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|TimeoutExecutorThread
parameter_list|()
block|{
name|super
argument_list|(
literal|"ProcedureDispatcherTimeoutThread"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
specifier|final
name|DelayedWithTimeout
name|task
init|=
name|DelayedUtil
operator|.
name|takeWithoutInterrupt
argument_list|(
name|queue
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|==
literal|null
operator|||
name|task
operator|==
name|DelayedUtil
operator|.
name|DELAYED_POISON
condition|)
block|{
comment|// the executor may be shutting down, and the task is just the shutdown request
continue|continue;
block|}
if|if
condition|(
name|task
operator|instanceof
name|DelayedTask
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|(
operator|(
name|DelayedTask
operator|)
name|task
operator|)
operator|.
name|getObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|(
operator|(
name|BufferNode
operator|)
name|task
operator|)
operator|.
name|dispatch
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|add
parameter_list|(
specifier|final
name|DelayedWithTimeout
name|delayed
parameter_list|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|delayed
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|remove
parameter_list|(
specifier|final
name|DelayedWithTimeout
name|delayed
parameter_list|)
block|{
name|queue
operator|.
name|remove
argument_list|(
name|delayed
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|sendStopSignal
parameter_list|()
block|{
name|queue
operator|.
name|add
argument_list|(
name|DelayedUtil
operator|.
name|DELAYED_POISON
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|awaitTermination
parameter_list|()
block|{
try|try
block|{
specifier|final
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|isAlive
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|sendStopSignal
argument_list|()
expr_stmt|;
name|join
argument_list|(
literal|250
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|>
literal|0
operator|&&
operator|(
name|i
operator|%
literal|8
operator|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Waiting termination of thread "
operator|+
name|getName
argument_list|()
operator|+
literal|", "
operator|+
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|getName
argument_list|()
operator|+
literal|" join wait got interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// ============================================================================================
comment|//  Internals Helpers
comment|// ============================================================================================
comment|/**    * Node that contains a set of RemoteProcedures    */
specifier|protected
specifier|final
class|class
name|BufferNode
extends|extends
name|DelayedContainerWithTimestamp
argument_list|<
name|TRemote
argument_list|>
implements|implements
name|RemoteNode
argument_list|<
name|TEnv
argument_list|,
name|TRemote
argument_list|>
block|{
specifier|private
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|operations
decl_stmt|;
specifier|protected
name|BufferNode
parameter_list|(
specifier|final
name|TRemote
name|key
parameter_list|)
block|{
name|super
argument_list|(
name|key
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TRemote
name|getKey
parameter_list|()
block|{
return|return
name|getObject
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|add
parameter_list|(
specifier|final
name|RemoteProcedure
name|operation
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|operations
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|operations
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|setTimeout
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|+
name|operationDelay
argument_list|)
expr_stmt|;
name|timeoutExecutor
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|operations
operator|.
name|add
argument_list|(
name|operation
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|operations
operator|.
name|size
argument_list|()
operator|>
name|queueMaxSize
condition|)
block|{
name|timeoutExecutor
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|dispatch
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|dispatch
parameter_list|()
block|{
if|if
condition|(
name|operations
operator|!=
literal|null
condition|)
block|{
name|remoteDispatch
argument_list|(
name|getKey
argument_list|()
argument_list|,
name|operations
argument_list|)
expr_stmt|;
name|this
operator|.
name|operations
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|abortOperationsInQueue
parameter_list|()
block|{
if|if
condition|(
name|operations
operator|!=
literal|null
condition|)
block|{
name|abortPendingOperations
argument_list|(
name|getKey
argument_list|()
argument_list|,
name|operations
argument_list|)
expr_stmt|;
name|this
operator|.
name|operations
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", operations="
operator|+
name|this
operator|.
name|operations
return|;
block|}
block|}
comment|/**    * Delayed object that holds a FutureTask.    * used to submit something later to the thread-pool.    */
specifier|private
specifier|static
specifier|final
class|class
name|DelayedTask
extends|extends
name|DelayedContainerWithTimestamp
argument_list|<
name|FutureTask
argument_list|<
name|Void
argument_list|>
argument_list|>
block|{
specifier|public
name|DelayedTask
parameter_list|(
specifier|final
name|FutureTask
argument_list|<
name|Void
argument_list|>
name|task
parameter_list|,
specifier|final
name|long
name|delay
parameter_list|,
specifier|final
name|TimeUnit
name|unit
parameter_list|)
block|{
name|super
argument_list|(
name|task
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|+
name|unit
operator|.
name|toMillis
argument_list|(
name|delay
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

