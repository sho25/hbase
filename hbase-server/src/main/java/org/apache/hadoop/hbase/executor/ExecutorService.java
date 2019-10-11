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
name|executor
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
name|io
operator|.
name|Writer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadInfo
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|ConcurrentMap
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
name|Executors
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
name|AtomicLong
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
name|monitoring
operator|.
name|ThreadMonitoring
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|Lists
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
name|Maps
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
name|util
operator|.
name|concurrent
operator|.
name|ListenableFuture
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
name|util
operator|.
name|concurrent
operator|.
name|ListeningScheduledExecutorService
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
name|util
operator|.
name|concurrent
operator|.
name|MoreExecutors
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * This is a generic executor service. This component abstracts a  * threadpool, a queue to which {@link EventType}s can be submitted,  * and a<code>Runnable</code> that handles the object that is added to the queue.  *  *<p>In order to create a new service, create an instance of this class and  * then do:<code>instance.startExecutorService("myService");</code>.  When done  * call {@link #shutdown()}.  *  *<p>In order to use the service created above, call  * {@link #submit(EventHandler)}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExecutorService
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
name|ExecutorService
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// hold the all the executors created in a map addressable by their names
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Executor
argument_list|>
name|executorMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Name of the server hosting this executor service.
specifier|private
specifier|final
name|String
name|servername
decl_stmt|;
specifier|private
specifier|final
name|ListeningScheduledExecutorService
name|delayedSubmitTimer
init|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"Event-Executor-Delay-Submit-Timer"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * Default constructor.    * @param servername Name of the hosting server.    */
specifier|public
name|ExecutorService
parameter_list|(
specifier|final
name|String
name|servername
parameter_list|)
block|{
name|this
operator|.
name|servername
operator|=
name|servername
expr_stmt|;
block|}
comment|/**    * Start an executor service with a given name. If there was a service already    * started with the same name, this throws a RuntimeException.    * @param name Name of the service to start.    */
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|startExecutorService
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|maxThreads
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|executorMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"An executor service with the name "
operator|+
name|name
operator|+
literal|" is already running!"
argument_list|)
throw|;
block|}
name|Executor
name|hbes
init|=
operator|new
name|Executor
argument_list|(
name|name
argument_list|,
name|maxThreads
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|executorMap
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|hbes
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"An executor service with the name "
operator|+
name|name
operator|+
literal|" is already running (2)!"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting executor service name="
operator|+
name|name
operator|+
literal|", corePoolSize="
operator|+
name|hbes
operator|.
name|threadPoolExecutor
operator|.
name|getCorePoolSize
argument_list|()
operator|+
literal|", maxPoolSize="
operator|+
name|hbes
operator|.
name|threadPoolExecutor
operator|.
name|getMaximumPoolSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isExecutorServiceRunning
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|this
operator|.
name|executorMap
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|this
operator|.
name|delayedSubmitTimer
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Executor
argument_list|>
name|entry
range|:
name|this
operator|.
name|executorMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|Runnable
argument_list|>
name|wasRunning
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|threadPoolExecutor
operator|.
name|shutdownNow
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|wasRunning
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|+
literal|" had "
operator|+
name|wasRunning
operator|+
literal|" on shutdown"
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|executorMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|Executor
name|getExecutor
parameter_list|(
specifier|final
name|ExecutorType
name|type
parameter_list|)
block|{
return|return
name|getExecutor
argument_list|(
name|type
operator|.
name|getExecutorName
argument_list|(
name|this
operator|.
name|servername
argument_list|)
argument_list|)
return|;
block|}
name|Executor
name|getExecutor
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Executor
name|executor
init|=
name|this
operator|.
name|executorMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
name|executor
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|ThreadPoolExecutor
name|getExecutorThreadPool
parameter_list|(
specifier|final
name|ExecutorType
name|type
parameter_list|)
block|{
return|return
name|getExecutor
argument_list|(
name|type
argument_list|)
operator|.
name|getThreadPoolExecutor
argument_list|()
return|;
block|}
specifier|public
name|void
name|startExecutorService
parameter_list|(
specifier|final
name|ExecutorType
name|type
parameter_list|,
specifier|final
name|int
name|maxThreads
parameter_list|)
block|{
name|String
name|name
init|=
name|type
operator|.
name|getExecutorName
argument_list|(
name|this
operator|.
name|servername
argument_list|)
decl_stmt|;
if|if
condition|(
name|isExecutorServiceRunning
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Executor service "
operator|+
name|toString
argument_list|()
operator|+
literal|" already running on "
operator|+
name|this
operator|.
name|servername
argument_list|)
expr_stmt|;
return|return;
block|}
name|startExecutorService
argument_list|(
name|name
argument_list|,
name|maxThreads
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize the executor lazily, Note if an executor need to be initialized lazily, then all    * paths should use this method to get the executor, should not start executor by using    * {@link ExecutorService#startExecutorService(ExecutorType, int)}    */
specifier|public
name|ThreadPoolExecutor
name|getExecutorLazily
parameter_list|(
name|ExecutorType
name|type
parameter_list|,
name|int
name|maxThreads
parameter_list|)
block|{
name|String
name|name
init|=
name|type
operator|.
name|getExecutorName
argument_list|(
name|this
operator|.
name|servername
argument_list|)
decl_stmt|;
return|return
name|executorMap
operator|.
name|computeIfAbsent
argument_list|(
name|name
argument_list|,
parameter_list|(
name|executorName
parameter_list|)
lambda|->
operator|new
name|Executor
argument_list|(
name|executorName
argument_list|,
name|maxThreads
argument_list|)
argument_list|)
operator|.
name|getThreadPoolExecutor
argument_list|()
return|;
block|}
specifier|public
name|void
name|submit
parameter_list|(
specifier|final
name|EventHandler
name|eh
parameter_list|)
block|{
name|Executor
name|executor
init|=
name|getExecutor
argument_list|(
name|eh
operator|.
name|getEventType
argument_list|()
operator|.
name|getExecutorServiceType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|executor
operator|==
literal|null
condition|)
block|{
comment|// This happens only when events are submitted after shutdown() was
comment|// called, so dropping them should be "ok" since it means we're
comment|// shutting down.
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot submit ["
operator|+
name|eh
operator|+
literal|"] because the executor is missing."
operator|+
literal|" Is this process shutting down?"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|executor
operator|.
name|submit
argument_list|(
name|eh
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Submit the handler after the given delay. Used for retrying.
specifier|public
name|void
name|delayedSubmit
parameter_list|(
name|EventHandler
name|eh
parameter_list|,
name|long
name|delay
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|ListenableFuture
argument_list|<
name|?
argument_list|>
name|future
init|=
name|delayedSubmitTimer
operator|.
name|schedule
argument_list|(
parameter_list|()
lambda|->
name|submit
argument_list|(
name|eh
argument_list|)
argument_list|,
name|delay
argument_list|,
name|unit
argument_list|)
decl_stmt|;
name|future
operator|.
name|addListener
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to submit the event handler {} to executor"
argument_list|,
name|eh
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|MoreExecutors
operator|.
name|directExecutor
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ExecutorStatus
argument_list|>
name|getAllExecutorStatuses
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ExecutorStatus
argument_list|>
name|ret
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Executor
argument_list|>
name|e
range|:
name|executorMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ret
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Executor instance.    */
specifier|static
class|class
name|Executor
block|{
comment|// how long to retain excess threads
specifier|static
specifier|final
name|long
name|keepAliveTimeInMillis
init|=
literal|1000
decl_stmt|;
comment|// the thread pool executor that services the requests
specifier|final
name|TrackingThreadPoolExecutor
name|threadPoolExecutor
decl_stmt|;
comment|// work queue to use - unbounded queue
specifier|final
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|q
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicLong
name|seqids
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|long
name|id
decl_stmt|;
specifier|protected
name|Executor
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|maxThreads
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|seqids
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
comment|// create the thread pool executor
name|this
operator|.
name|threadPoolExecutor
operator|=
operator|new
name|TrackingThreadPoolExecutor
argument_list|(
name|maxThreads
argument_list|,
name|maxThreads
argument_list|,
name|keepAliveTimeInMillis
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|q
argument_list|)
expr_stmt|;
comment|// name the threads for this threadpool
name|ThreadFactoryBuilder
name|tfb
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|tfb
operator|.
name|setNameFormat
argument_list|(
name|this
operator|.
name|name
operator|+
literal|"-%d"
argument_list|)
expr_stmt|;
name|tfb
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPoolExecutor
operator|.
name|setThreadFactory
argument_list|(
name|tfb
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Submit the event to the queue for handling.      * @param event      */
name|void
name|submit
parameter_list|(
specifier|final
name|EventHandler
name|event
parameter_list|)
block|{
comment|// If there is a listener for this type, make sure we call the before
comment|// and after process methods.
name|this
operator|.
name|threadPoolExecutor
operator|.
name|execute
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
name|TrackingThreadPoolExecutor
name|getThreadPoolExecutor
parameter_list|()
block|{
return|return
name|threadPoolExecutor
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|id
operator|+
literal|"-"
operator|+
name|name
return|;
block|}
specifier|public
name|ExecutorStatus
name|getStatus
parameter_list|()
block|{
name|List
argument_list|<
name|EventHandler
argument_list|>
name|queuedEvents
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Runnable
name|r
range|:
name|q
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|r
operator|instanceof
name|EventHandler
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Non-EventHandler "
operator|+
name|r
operator|+
literal|" queued in "
operator|+
name|name
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|queuedEvents
operator|.
name|add
argument_list|(
operator|(
name|EventHandler
operator|)
name|r
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RunningEventStatus
argument_list|>
name|running
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Thread
argument_list|,
name|Runnable
argument_list|>
name|e
range|:
name|threadPoolExecutor
operator|.
name|getRunningTasks
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Runnable
name|r
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|r
operator|instanceof
name|EventHandler
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Non-EventHandler "
operator|+
name|r
operator|+
literal|" running in "
operator|+
name|name
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|running
operator|.
name|add
argument_list|(
operator|new
name|RunningEventStatus
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|EventHandler
operator|)
name|r
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ExecutorStatus
argument_list|(
name|this
argument_list|,
name|queuedEvents
argument_list|,
name|running
argument_list|)
return|;
block|}
block|}
comment|/**    * A subclass of ThreadPoolExecutor that keeps track of the Runnables that    * are executing at any given point in time.    */
specifier|static
class|class
name|TrackingThreadPoolExecutor
extends|extends
name|ThreadPoolExecutor
block|{
specifier|private
name|ConcurrentMap
argument_list|<
name|Thread
argument_list|,
name|Runnable
argument_list|>
name|running
init|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
specifier|public
name|TrackingThreadPoolExecutor
parameter_list|(
name|int
name|corePoolSize
parameter_list|,
name|int
name|maximumPoolSize
parameter_list|,
name|long
name|keepAliveTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|)
block|{
name|super
argument_list|(
name|corePoolSize
argument_list|,
name|maximumPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|afterExecute
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|super
operator|.
name|afterExecute
argument_list|(
name|r
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|running
operator|.
name|remove
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|beforeExecute
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Runnable
name|r
parameter_list|)
block|{
name|Runnable
name|oldPut
init|=
name|running
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|r
argument_list|)
decl_stmt|;
assert|assert
name|oldPut
operator|==
literal|null
operator|:
literal|"inconsistency for thread "
operator|+
name|t
assert|;
name|super
operator|.
name|beforeExecute
argument_list|(
name|t
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return a map of the threads currently running tasks      * inside this executor. Each key is an active thread,      * and the value is the task that is currently running.      * Note that this is not a stable snapshot of the map.      */
specifier|public
name|ConcurrentMap
argument_list|<
name|Thread
argument_list|,
name|Runnable
argument_list|>
name|getRunningTasks
parameter_list|()
block|{
return|return
name|running
return|;
block|}
block|}
comment|/**    * A snapshot of the status of a particular executor. This includes    * the contents of the executor's pending queue, as well as the    * threads and events currently being processed.    *    * This is a consistent snapshot that is immutable once constructed.    */
specifier|public
specifier|static
class|class
name|ExecutorStatus
block|{
specifier|final
name|Executor
name|executor
decl_stmt|;
specifier|final
name|List
argument_list|<
name|EventHandler
argument_list|>
name|queuedEvents
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RunningEventStatus
argument_list|>
name|running
decl_stmt|;
name|ExecutorStatus
parameter_list|(
name|Executor
name|executor
parameter_list|,
name|List
argument_list|<
name|EventHandler
argument_list|>
name|queuedEvents
parameter_list|,
name|List
argument_list|<
name|RunningEventStatus
argument_list|>
name|running
parameter_list|)
block|{
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|queuedEvents
operator|=
name|queuedEvents
expr_stmt|;
name|this
operator|.
name|running
operator|=
name|running
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|EventHandler
argument_list|>
name|getQueuedEvents
parameter_list|()
block|{
return|return
name|queuedEvents
return|;
block|}
specifier|public
name|List
argument_list|<
name|RunningEventStatus
argument_list|>
name|getRunning
parameter_list|()
block|{
return|return
name|running
return|;
block|}
comment|/**      * Dump a textual representation of the executor's status      * to the given writer.      *      * @param out the stream to write to      * @param indent a string prefix for each line, used for indentation      */
specifier|public
name|void
name|dumpTo
parameter_list|(
name|Writer
name|out
parameter_list|,
name|String
name|indent
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
literal|"Status for executor: "
operator|+
name|executor
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
literal|"=======================================\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
name|queuedEvents
operator|.
name|size
argument_list|()
operator|+
literal|" events queued, "
operator|+
name|running
operator|.
name|size
argument_list|()
operator|+
literal|" running\n"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|queuedEvents
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
literal|"Queued:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|EventHandler
name|e
range|:
name|queuedEvents
control|)
block|{
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
literal|"  "
operator|+
name|e
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|write
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|running
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
literal|"Running:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|RunningEventStatus
name|stat
range|:
name|running
control|)
block|{
name|out
operator|.
name|write
argument_list|(
name|indent
operator|+
literal|"  Running on thread '"
operator|+
name|stat
operator|.
name|threadInfo
operator|.
name|getThreadName
argument_list|()
operator|+
literal|"': "
operator|+
name|stat
operator|.
name|event
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|ThreadMonitoring
operator|.
name|formatThreadInfo
argument_list|(
name|stat
operator|.
name|threadInfo
argument_list|,
name|indent
operator|+
literal|"  "
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * The status of a particular event that is in the middle of being    * handled by an executor.    */
specifier|public
specifier|static
class|class
name|RunningEventStatus
block|{
specifier|final
name|ThreadInfo
name|threadInfo
decl_stmt|;
specifier|final
name|EventHandler
name|event
decl_stmt|;
specifier|public
name|RunningEventStatus
parameter_list|(
name|Thread
name|t
parameter_list|,
name|EventHandler
name|event
parameter_list|)
block|{
name|this
operator|.
name|threadInfo
operator|=
name|ThreadMonitoring
operator|.
name|getThreadInfo
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

