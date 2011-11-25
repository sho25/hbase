begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|thrift
package|;
end_package

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
name|ExecutorService
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
name|RejectedExecutionException
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
name|SynchronousQueue
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TThreadPoolServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * A bounded thread pool server customized for HBase.  */
end_comment

begin_class
specifier|public
class|class
name|TBoundedThreadPoolServer
extends|extends
name|TServer
block|{
specifier|private
specifier|static
specifier|final
name|String
name|QUEUE_FULL_MSG
init|=
literal|"Queue is full, closing connection"
decl_stmt|;
comment|/**    * The "core size" of the thread pool. New threads are created on every    * connection until this many threads are created.    */
specifier|public
specifier|static
specifier|final
name|String
name|MIN_WORKER_THREADS_CONF_KEY
init|=
literal|"hbase.thrift.minWorkerThreads"
decl_stmt|;
comment|/**    * This default core pool size should be enough for many test scenarios. We    * want to override this with a much larger number (e.g. at least 200) for a    * large-scale production setup.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MIN_WORKER_THREADS
init|=
literal|16
decl_stmt|;
comment|/**    * The maximum size of the thread pool. When the pending request queue    * overflows, new threads are created until their number reaches this number.    * After that, the server starts dropping connections.    */
specifier|public
specifier|static
specifier|final
name|String
name|MAX_WORKER_THREADS_CONF_KEY
init|=
literal|"hbase.thrift.maxWorkerThreads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_WORKER_THREADS
init|=
literal|1000
decl_stmt|;
comment|/**    * The maximum number of pending connections waiting in the queue. If there    * are no idle threads in the pool, the server queues requests. Only when    * the queue overflows, new threads are added, up to    * hbase.thrift.maxQueuedRequests threads.    */
specifier|public
specifier|static
specifier|final
name|String
name|MAX_QUEUED_REQUESTS_CONF_KEY
init|=
literal|"hbase.thrift.maxQueuedRequests"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_QUEUED_REQUESTS
init|=
literal|1000
decl_stmt|;
comment|/**    * Default amount of time in seconds to keep a thread alive. Worker threads    * are stopped after being idle for this long.    */
specifier|public
specifier|static
specifier|final
name|String
name|THREAD_KEEP_ALIVE_TIME_SEC_CONF_KEY
init|=
literal|"hbase.thrift.threadKeepAliveTimeSec"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_THREAD_KEEP_ALIVE_TIME_SEC
init|=
literal|60
decl_stmt|;
comment|/**    * Time to wait after interrupting all worker threads. This is after a clean    * shutdown has been attempted.    */
specifier|public
specifier|static
specifier|final
name|int
name|TIME_TO_WAIT_AFTER_SHUTDOWN_MS
init|=
literal|5000
decl_stmt|;
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
name|TBoundedThreadPoolServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|Args
extends|extends
name|TThreadPoolServer
operator|.
name|Args
block|{
name|int
name|maxQueuedRequests
decl_stmt|;
name|int
name|threadKeepAliveTimeSec
decl_stmt|;
specifier|public
name|Args
parameter_list|(
name|TServerTransport
name|transport
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|transport
argument_list|)
expr_stmt|;
name|minWorkerThreads
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MIN_WORKER_THREADS_CONF_KEY
argument_list|,
name|DEFAULT_MIN_WORKER_THREADS
argument_list|)
expr_stmt|;
name|maxWorkerThreads
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_WORKER_THREADS_CONF_KEY
argument_list|,
name|DEFAULT_MAX_WORKER_THREADS
argument_list|)
expr_stmt|;
name|maxQueuedRequests
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_QUEUED_REQUESTS_CONF_KEY
argument_list|,
name|DEFAULT_MAX_QUEUED_REQUESTS
argument_list|)
expr_stmt|;
name|threadKeepAliveTimeSec
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|THREAD_KEEP_ALIVE_TIME_SEC_CONF_KEY
argument_list|,
name|DEFAULT_THREAD_KEEP_ALIVE_TIME_SEC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"min worker threads="
operator|+
name|minWorkerThreads
operator|+
literal|", max worker threads="
operator|+
name|maxWorkerThreads
operator|+
literal|", max queued requests="
operator|+
name|maxQueuedRequests
return|;
block|}
block|}
comment|/** Executor service for handling client connections */
specifier|private
name|ExecutorService
name|executorService
decl_stmt|;
comment|/** Flag for stopping the server */
specifier|private
specifier|volatile
name|boolean
name|stopped
decl_stmt|;
specifier|private
name|Args
name|serverOptions
decl_stmt|;
specifier|public
name|TBoundedThreadPoolServer
parameter_list|(
name|Args
name|options
parameter_list|)
block|{
name|super
argument_list|(
name|options
argument_list|)
expr_stmt|;
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|executorQueue
decl_stmt|;
if|if
condition|(
name|options
operator|.
name|maxQueuedRequests
operator|>
literal|0
condition|)
block|{
name|executorQueue
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|(
name|options
operator|.
name|maxQueuedRequests
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|executorQueue
operator|=
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|ThreadFactoryBuilder
name|tfb
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
decl_stmt|;
name|tfb
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|tfb
operator|.
name|setNameFormat
argument_list|(
literal|"thrift-worker-%d"
argument_list|)
expr_stmt|;
name|executorService
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|options
operator|.
name|minWorkerThreads
argument_list|,
name|options
operator|.
name|maxWorkerThreads
argument_list|,
name|DEFAULT_THREAD_KEEP_ALIVE_TIME_SEC
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|executorQueue
argument_list|,
name|tfb
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|serverOptions
operator|=
name|options
expr_stmt|;
block|}
specifier|public
name|void
name|serve
parameter_list|()
block|{
try|try
block|{
name|serverTransport_
operator|.
name|listen
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|ttx
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error occurred during listening."
argument_list|,
name|ttx
argument_list|)
expr_stmt|;
return|return;
block|}
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Thread
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-shutdown-hook"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|TBoundedThreadPoolServer
operator|.
name|this
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|stopped
operator|=
literal|false
expr_stmt|;
while|while
condition|(
operator|!
name|stopped
operator|&&
operator|!
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
name|TTransport
name|client
init|=
literal|null
decl_stmt|;
try|try
block|{
name|client
operator|=
name|serverTransport_
operator|.
name|accept
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|ttx
parameter_list|)
block|{
if|if
condition|(
operator|!
name|stopped
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Transport error when accepting message"
argument_list|,
name|ttx
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
comment|// The server has been stopped
break|break;
block|}
block|}
name|ClientConnnection
name|command
init|=
operator|new
name|ClientConnnection
argument_list|(
name|client
argument_list|)
decl_stmt|;
try|try
block|{
name|executorService
operator|.
name|execute
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|rex
parameter_list|)
block|{
if|if
condition|(
name|client
operator|.
name|getClass
argument_list|()
operator|==
name|TSocket
operator|.
name|class
condition|)
block|{
comment|// We expect the client to be TSocket.
name|LOG
operator|.
name|warn
argument_list|(
name|QUEUE_FULL_MSG
operator|+
literal|" from "
operator|+
operator|(
operator|(
name|TSocket
operator|)
name|client
operator|)
operator|.
name|getSocket
argument_list|()
operator|.
name|getRemoteSocketAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|QUEUE_FULL_MSG
argument_list|,
name|rex
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|shutdownServer
argument_list|()
expr_stmt|;
block|}
comment|/**    * Loop until {@link ExecutorService#awaitTermination} finally does return    * without an interrupted exception. If we don't do this, then we'll shut    * down prematurely. We want to let the executor service clear its task    * queue, closing client sockets appropriately.    */
specifier|private
name|void
name|shutdownServer
parameter_list|()
block|{
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|long
name|msLeftToWait
init|=
name|serverOptions
operator|.
name|stopTimeoutUnit
operator|.
name|toMillis
argument_list|(
name|serverOptions
operator|.
name|stopTimeoutVal
argument_list|)
decl_stmt|;
name|long
name|timeMillis
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for up to "
operator|+
name|msLeftToWait
operator|+
literal|" ms to finish processing"
operator|+
literal|" pending requests"
argument_list|)
expr_stmt|;
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|msLeftToWait
operator|>=
literal|0
condition|)
block|{
try|try
block|{
name|executorService
operator|.
name|awaitTermination
argument_list|(
name|msLeftToWait
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ix
parameter_list|)
block|{
name|long
name|timePassed
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|timeMillis
decl_stmt|;
name|msLeftToWait
operator|-=
name|timePassed
expr_stmt|;
name|timeMillis
operator|+=
name|timePassed
expr_stmt|;
name|interrupted
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Interrupting all worker threads and waiting for "
operator|+
name|TIME_TO_WAIT_AFTER_SHUTDOWN_MS
operator|+
literal|" ms longer"
argument_list|)
expr_stmt|;
comment|// This will interrupt all the threads, even those running a task.
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
name|TIME_TO_WAIT_AFTER_SHUTDOWN_MS
argument_list|)
expr_stmt|;
comment|// Preserve the interrupted status.
if|if
condition|(
name|interrupted
condition|)
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Thrift server shutdown complete"
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
name|stopped
operator|=
literal|true
expr_stmt|;
name|serverTransport_
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
specifier|private
class|class
name|ClientConnnection
implements|implements
name|Runnable
block|{
specifier|private
name|TTransport
name|client
decl_stmt|;
comment|/**      * Default constructor.      *      * @param client Transport to process      */
specifier|private
name|ClientConnnection
parameter_list|(
name|TTransport
name|client
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
comment|/**      * Loops on processing a client forever      */
specifier|public
name|void
name|run
parameter_list|()
block|{
name|TProcessor
name|processor
init|=
literal|null
decl_stmt|;
name|TTransport
name|inputTransport
init|=
literal|null
decl_stmt|;
name|TTransport
name|outputTransport
init|=
literal|null
decl_stmt|;
name|TProtocol
name|inputProtocol
init|=
literal|null
decl_stmt|;
name|TProtocol
name|outputProtocol
init|=
literal|null
decl_stmt|;
try|try
block|{
name|processor
operator|=
name|processorFactory_
operator|.
name|getProcessor
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|inputTransport
operator|=
name|inputTransportFactory_
operator|.
name|getTransport
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|outputTransport
operator|=
name|outputTransportFactory_
operator|.
name|getTransport
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|inputProtocol
operator|=
name|inputProtocolFactory_
operator|.
name|getProtocol
argument_list|(
name|inputTransport
argument_list|)
expr_stmt|;
name|outputProtocol
operator|=
name|outputProtocolFactory_
operator|.
name|getProtocol
argument_list|(
name|outputTransport
argument_list|)
expr_stmt|;
comment|// we check stopped_ first to make sure we're not supposed to be shutting
comment|// down. this is necessary for graceful shutdown.
while|while
condition|(
operator|!
name|stopped
operator|&&
name|processor
operator|.
name|process
argument_list|(
name|inputProtocol
argument_list|,
name|outputProtocol
argument_list|)
condition|)
block|{}
block|}
catch|catch
parameter_list|(
name|TTransportException
name|ttx
parameter_list|)
block|{
comment|// Assume the client died and continue silently
block|}
catch|catch
parameter_list|(
name|TException
name|tx
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Thrift error occurred during processing of message."
argument_list|,
name|tx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|x
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error occurred during processing of message."
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|inputTransport
operator|!=
literal|null
condition|)
block|{
name|inputTransport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|outputTransport
operator|!=
literal|null
condition|)
block|{
name|outputTransport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

