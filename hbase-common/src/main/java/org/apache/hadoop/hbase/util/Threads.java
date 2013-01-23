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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|ThreadFactory
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Thread Utility  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|Threads
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Threads
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|poolNumber
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|/**    * Utility method that sets name, daemon status and starts passed thread.    * @param t thread to run    * @return Returns the passed Thread<code>t</code>.    */
specifier|public
specifier|static
name|Thread
name|setDaemonThreadRunning
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
block|{
return|return
name|setDaemonThreadRunning
argument_list|(
name|t
argument_list|,
name|t
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Utility method that sets name, daemon status and starts passed thread.    * @param t thread to frob    * @param name new name    * @return Returns the passed Thread<code>t</code>.    */
specifier|public
specifier|static
name|Thread
name|setDaemonThreadRunning
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
name|setDaemonThreadRunning
argument_list|(
name|t
argument_list|,
name|name
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Utility method that sets name, daemon status and starts passed thread.    * @param t thread to frob    * @param name new name    * @param handler A handler to set on the thread.  Pass null if want to    * use default handler.    * @return Returns the passed Thread<code>t</code>.    */
specifier|public
specifier|static
name|Thread
name|setDaemonThreadRunning
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|UncaughtExceptionHandler
name|handler
parameter_list|)
block|{
name|t
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|t
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
comment|/**    * Shutdown passed thread using isAlive and join.    * @param t Thread to shutdown    */
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
block|{
name|shutdown
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shutdown passed thread using isAlive and join.    * @param joinwait Pass 0 if we're to wait forever.    * @param t Thread to shutdown    */
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|long
name|joinwait
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
return|return;
while|while
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|(
name|joinwait
argument_list|)
expr_stmt|;
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
name|t
operator|.
name|getName
argument_list|()
operator|+
literal|"; joinwait="
operator|+
name|joinwait
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param t Waits on the passed thread to die dumping a threaddump every    * minute while its up.    * @throws InterruptedException    */
specifier|public
specifier|static
name|void
name|threadDumpingIsAlive
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
return|return;
block|}
while|while
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|t
operator|.
name|join
argument_list|(
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|ReflectionUtils
operator|.
name|printThreadInfo
argument_list|(
operator|new
name|PrintWriter
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|,
literal|"Automatic Stack Trace every 60 seconds waiting on "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param millis How long to sleep for in milliseconds.    */
specifier|public
specifier|static
name|void
name|sleep
parameter_list|(
name|long
name|millis
parameter_list|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|millis
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Sleeps for the given amount of time even if interrupted. Preserves    * the interrupt status.    * @param msToWait the amount of time to sleep in milliseconds    */
specifier|public
specifier|static
name|void
name|sleepWithoutInterrupt
parameter_list|(
specifier|final
name|long
name|msToWait
parameter_list|)
block|{
name|long
name|timeMillis
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|endTime
init|=
name|timeMillis
operator|+
name|msToWait
decl_stmt|;
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|timeMillis
operator|<
name|endTime
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|endTime
operator|-
name|timeMillis
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|interrupted
operator|=
literal|true
expr_stmt|;
block|}
name|timeMillis
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
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
block|}
comment|/**    * Create a new CachedThreadPool with a bounded number as the maximum     * thread size in the pool.    *     * @param maxCachedThread the maximum thread could be created in the pool    * @param timeout the maximum time to wait    * @param unit the time unit of the timeout argument    * @param threadFactory the factory to use when creating new threads    * @return threadPoolExecutor the cachedThreadPool with a bounded number     * as the maximum thread size in the pool.     */
specifier|public
specifier|static
name|ThreadPoolExecutor
name|getBoundedCachedThreadPool
parameter_list|(
name|int
name|maxCachedThread
parameter_list|,
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|ThreadFactory
name|threadFactory
parameter_list|)
block|{
name|ThreadPoolExecutor
name|boundedCachedThreadPool
init|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|maxCachedThread
argument_list|,
name|maxCachedThread
argument_list|,
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
name|threadFactory
argument_list|)
decl_stmt|;
comment|// allow the core pool threads timeout and terminate
name|boundedCachedThreadPool
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|boundedCachedThreadPool
return|;
block|}
comment|/**    * Returns a {@link java.util.concurrent.ThreadFactory} that names each created thread uniquely,    * with a common prefix.    * @param prefix The prefix of every created Thread's name    * @return a {@link java.util.concurrent.ThreadFactory} that names threads    */
specifier|public
specifier|static
name|ThreadFactory
name|getNamedThreadFactory
parameter_list|(
specifier|final
name|String
name|prefix
parameter_list|)
block|{
name|SecurityManager
name|s
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
specifier|final
name|ThreadGroup
name|threadGroup
init|=
operator|(
name|s
operator|!=
literal|null
operator|)
condition|?
name|s
operator|.
name|getThreadGroup
argument_list|()
else|:
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getThreadGroup
argument_list|()
decl_stmt|;
return|return
operator|new
name|ThreadFactory
argument_list|()
block|{
specifier|final
name|AtomicInteger
name|threadNumber
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|poolNumber
init|=
name|Threads
operator|.
name|poolNumber
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
specifier|final
name|ThreadGroup
name|group
init|=
name|threadGroup
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
specifier|final
name|String
name|name
init|=
name|prefix
operator|+
literal|"-pool-"
operator|+
name|poolNumber
operator|+
literal|"-thread-"
operator|+
name|threadNumber
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
return|return
operator|new
name|Thread
argument_list|(
name|group
argument_list|,
name|r
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/**    * Same as {#newDaemonThreadFactory(String, UncaughtExceptionHandler)},    * without setting the exception handler.    */
specifier|public
specifier|static
name|ThreadFactory
name|newDaemonThreadFactory
parameter_list|(
specifier|final
name|String
name|prefix
parameter_list|)
block|{
return|return
name|newDaemonThreadFactory
argument_list|(
name|prefix
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Get a named {@link ThreadFactory} that just builds daemon threads.    * @param prefix name prefix for all threads created from the factory    * @param handler unhandles exception handler to set for all threads    * @return a thread factory that creates named, daemon threads with    *         the supplied exception handler and normal priority    */
specifier|public
specifier|static
name|ThreadFactory
name|newDaemonThreadFactory
parameter_list|(
specifier|final
name|String
name|prefix
parameter_list|,
specifier|final
name|UncaughtExceptionHandler
name|handler
parameter_list|)
block|{
specifier|final
name|ThreadFactory
name|namedFactory
init|=
name|getNamedThreadFactory
argument_list|(
name|prefix
argument_list|)
decl_stmt|;
return|return
operator|new
name|ThreadFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
name|Thread
name|t
init|=
name|namedFactory
operator|.
name|newThread
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|t
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|t
operator|.
name|isDaemon
argument_list|()
condition|)
block|{
name|t
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|t
operator|.
name|getPriority
argument_list|()
operator|!=
name|Thread
operator|.
name|NORM_PRIORITY
condition|)
block|{
name|t
operator|.
name|setPriority
argument_list|(
name|Thread
operator|.
name|NORM_PRIORITY
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

