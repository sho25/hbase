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
name|monitoring
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
name|ref
operator|.
name|WeakReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Proxy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|HBaseConfiguration
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|collections4
operator|.
name|queue
operator|.
name|CircularFifoQueue
import|;
end_import

begin_comment
comment|/**  * Singleton which keeps track of tasks going on in this VM.  * A Task here is anything which takes more than a few seconds  * and the user might want to inquire about the status  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TaskMonitor
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
name|TaskMonitor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_TASKS_KEY
init|=
literal|"hbase.taskmonitor.max.tasks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_TASKS
init|=
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RPC_WARN_TIME_KEY
init|=
literal|"hbase.taskmonitor.rpc.warn.time"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_RPC_WARN_TIME
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXPIRATION_TIME_KEY
init|=
literal|"hbase.taskmonitor.expiration.time"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_EXPIRATION_TIME
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MONITOR_INTERVAL_KEY
init|=
literal|"hbase.taskmonitor.monitor.interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MONITOR_INTERVAL
init|=
literal|10
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|static
name|TaskMonitor
name|instance
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxTasks
decl_stmt|;
specifier|private
specifier|final
name|long
name|rpcWarnTime
decl_stmt|;
specifier|private
specifier|final
name|long
name|expirationTime
decl_stmt|;
specifier|private
specifier|final
name|CircularFifoQueue
name|tasks
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|TaskAndWeakRefPair
argument_list|>
name|rpcTasks
decl_stmt|;
specifier|private
specifier|final
name|long
name|monitorInterval
decl_stmt|;
specifier|private
name|Thread
name|monitorThread
decl_stmt|;
name|TaskMonitor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|maxTasks
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_TASKS_KEY
argument_list|,
name|DEFAULT_MAX_TASKS
argument_list|)
expr_stmt|;
name|expirationTime
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|EXPIRATION_TIME_KEY
argument_list|,
name|DEFAULT_EXPIRATION_TIME
argument_list|)
expr_stmt|;
name|rpcWarnTime
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|RPC_WARN_TIME_KEY
argument_list|,
name|DEFAULT_RPC_WARN_TIME
argument_list|)
expr_stmt|;
name|tasks
operator|=
operator|new
name|CircularFifoQueue
argument_list|(
name|maxTasks
argument_list|)
expr_stmt|;
name|rpcTasks
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|monitorInterval
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|MONITOR_INTERVAL_KEY
argument_list|,
name|DEFAULT_MONITOR_INTERVAL
argument_list|)
expr_stmt|;
name|monitorThread
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|MonitorRunnable
argument_list|()
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
name|monitorThread
argument_list|,
literal|"Monitor thread for TaskMonitor"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get singleton instance.    * TODO this would be better off scoped to a single daemon    */
specifier|public
specifier|static
specifier|synchronized
name|TaskMonitor
name|get
parameter_list|()
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|TaskMonitor
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|public
specifier|synchronized
name|MonitoredTask
name|createStatus
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|MonitoredTask
name|stat
init|=
operator|new
name|MonitoredTaskImpl
argument_list|()
decl_stmt|;
name|stat
operator|.
name|setDescription
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|MonitoredTask
name|proxy
init|=
operator|(
name|MonitoredTask
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|stat
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|MonitoredTask
operator|.
name|class
block|}
operator|,
operator|new
name|PassthroughInvocationHandler
argument_list|<>
argument_list|(
name|stat
argument_list|)
block|)
function|;
name|TaskAndWeakRefPair
name|pair
init|=
operator|new
name|TaskAndWeakRefPair
argument_list|(
name|stat
argument_list|,
name|proxy
argument_list|)
decl_stmt|;
if|if
condition|(
name|tasks
operator|.
name|isFull
argument_list|()
condition|)
block|{
name|purgeExpiredTasks
argument_list|()
expr_stmt|;
block|}
name|tasks
operator|.
name|add
parameter_list|(
name|pair
parameter_list|)
constructor_decl|;
return|return
name|proxy
return|;
block|}
end_class

begin_function
specifier|public
specifier|synchronized
name|MonitoredRPCHandler
name|createRPCStatus
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|MonitoredRPCHandler
name|stat
init|=
operator|new
name|MonitoredRPCHandlerImpl
argument_list|()
decl_stmt|;
name|stat
operator|.
name|setDescription
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|MonitoredRPCHandler
name|proxy
init|=
operator|(
name|MonitoredRPCHandler
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|stat
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|MonitoredRPCHandler
operator|.
name|class
block|}
operator|,
operator|new
name|PassthroughInvocationHandler
argument_list|<>
argument_list|(
name|stat
argument_list|)
block|)
function|;
end_function

begin_decl_stmt
name|TaskAndWeakRefPair
name|pair
init|=
operator|new
name|TaskAndWeakRefPair
argument_list|(
name|stat
argument_list|,
name|proxy
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|rpcTasks
operator|.
name|add
argument_list|(
name|pair
argument_list|)
expr_stmt|;
end_expr_stmt

begin_return
return|return
name|proxy
return|;
end_return

begin_function
unit|}    private
specifier|synchronized
name|void
name|warnStuckTasks
parameter_list|()
block|{
if|if
condition|(
name|rpcWarnTime
operator|>
literal|0
condition|)
block|{
specifier|final
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|TaskAndWeakRefPair
argument_list|>
name|it
init|=
name|rpcTasks
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|TaskAndWeakRefPair
name|pair
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|MonitoredTask
name|stat
init|=
name|pair
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|stat
operator|.
name|getState
argument_list|()
operator|==
name|MonitoredTaskImpl
operator|.
name|State
operator|.
name|RUNNING
operator|)
operator|&&
operator|(
name|now
operator|>=
name|stat
operator|.
name|getWarnTime
argument_list|()
operator|+
name|rpcWarnTime
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Task may be stuck: "
operator|+
name|stat
argument_list|)
expr_stmt|;
name|stat
operator|.
name|setWarnTime
argument_list|(
name|now
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_function

begin_function
specifier|private
specifier|synchronized
name|void
name|purgeExpiredTasks
parameter_list|()
block|{
for|for
control|(
name|Iterator
argument_list|<
name|TaskAndWeakRefPair
argument_list|>
name|it
init|=
name|tasks
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|TaskAndWeakRefPair
name|pair
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|MonitoredTask
name|stat
init|=
name|pair
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|isDead
argument_list|()
condition|)
block|{
comment|// The class who constructed this leaked it. So we can
comment|// assume it's done.
if|if
condition|(
name|stat
operator|.
name|getState
argument_list|()
operator|==
name|MonitoredTaskImpl
operator|.
name|State
operator|.
name|RUNNING
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Status "
operator|+
name|stat
operator|+
literal|" appears to have been leaked"
argument_list|)
expr_stmt|;
name|stat
operator|.
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|canPurge
argument_list|(
name|stat
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_function

begin_comment
comment|/**    * Produces a list containing copies of the current state of all non-expired     * MonitoredTasks handled by this TaskMonitor.    * @return A complete list of MonitoredTasks.    */
end_comment

begin_function
specifier|public
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|getTasks
parameter_list|()
block|{
return|return
name|getTasks
argument_list|(
literal|null
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Produces a list containing copies of the current state of all non-expired     * MonitoredTasks handled by this TaskMonitor.    * @param filter type of wanted tasks    * @return A filtered list of MonitoredTasks.    */
end_comment

begin_function
specifier|public
specifier|synchronized
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|getTasks
parameter_list|(
name|String
name|filter
parameter_list|)
block|{
name|purgeExpiredTasks
argument_list|()
expr_stmt|;
name|TaskFilter
name|taskFilter
init|=
name|createTaskFilter
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|MonitoredTask
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|tasks
operator|.
name|size
argument_list|()
operator|+
name|rpcTasks
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|processTasks
argument_list|(
name|tasks
argument_list|,
name|taskFilter
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|processTasks
argument_list|(
name|rpcTasks
argument_list|,
name|taskFilter
argument_list|,
name|results
argument_list|)
expr_stmt|;
return|return
name|results
return|;
block|}
end_function

begin_comment
comment|/**    * Create a task filter according to a given filter type.    * @param filter type of monitored task    * @return a task filter    */
end_comment

begin_function
specifier|private
specifier|static
name|TaskFilter
name|createTaskFilter
parameter_list|(
name|String
name|filter
parameter_list|)
block|{
switch|switch
condition|(
name|TaskFilter
operator|.
name|TaskType
operator|.
name|getTaskType
argument_list|(
name|filter
argument_list|)
condition|)
block|{
case|case
name|GENERAL
case|:
return|return
name|task
lambda|->
name|task
operator|instanceof
name|MonitoredRPCHandler
return|;
case|case
name|HANDLER
case|:
return|return
name|task
lambda|->
operator|!
operator|(
name|task
operator|instanceof
name|MonitoredRPCHandler
operator|)
return|;
case|case
name|RPC
case|:
return|return
name|task
lambda|->
operator|!
operator|(
name|task
operator|instanceof
name|MonitoredRPCHandler
operator|)
operator|||
operator|!
operator|(
operator|(
name|MonitoredRPCHandler
operator|)
name|task
operator|)
operator|.
name|isRPCRunning
argument_list|()
return|;
case|case
name|OPERATION
case|:
return|return
name|task
lambda|->
operator|!
operator|(
name|task
operator|instanceof
name|MonitoredRPCHandler
operator|)
operator|||
operator|!
operator|(
operator|(
name|MonitoredRPCHandler
operator|)
name|task
operator|)
operator|.
name|isOperationRunning
argument_list|()
return|;
default|default:
return|return
name|task
lambda|->
literal|false
return|;
block|}
block|}
end_function

begin_function
specifier|private
specifier|static
name|void
name|processTasks
parameter_list|(
name|Iterable
argument_list|<
name|TaskAndWeakRefPair
argument_list|>
name|tasks
parameter_list|,
name|TaskFilter
name|filter
parameter_list|,
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|results
parameter_list|)
block|{
for|for
control|(
name|TaskAndWeakRefPair
name|task
range|:
name|tasks
control|)
block|{
name|MonitoredTask
name|t
init|=
name|task
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|filter
operator|.
name|filter
argument_list|(
name|t
argument_list|)
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
name|t
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

begin_function
specifier|private
name|boolean
name|canPurge
parameter_list|(
name|MonitoredTask
name|stat
parameter_list|)
block|{
name|long
name|cts
init|=
name|stat
operator|.
name|getCompletionTimestamp
argument_list|()
decl_stmt|;
return|return
operator|(
name|cts
operator|>
literal|0
operator|&&
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|cts
operator|>
name|expirationTime
operator|)
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|dumpAsText
parameter_list|(
name|PrintWriter
name|out
parameter_list|)
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|MonitoredTask
argument_list|>
name|tasks
init|=
name|getTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|MonitoredTask
name|task
range|:
name|tasks
control|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Task: "
operator|+
name|task
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Status: "
operator|+
name|task
operator|.
name|getState
argument_list|()
operator|+
literal|":"
operator|+
name|task
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|running
init|=
operator|(
name|now
operator|-
name|task
operator|.
name|getStartTime
argument_list|()
operator|)
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|getCompletionTimestamp
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|long
name|completed
init|=
operator|(
name|now
operator|-
name|task
operator|.
name|getCompletionTimestamp
argument_list|()
operator|)
operator|/
literal|1000
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Completed "
operator|+
name|completed
operator|+
literal|"s ago"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Ran for "
operator|+
operator|(
name|task
operator|.
name|getCompletionTimestamp
argument_list|()
operator|-
name|task
operator|.
name|getStartTime
argument_list|()
operator|)
operator|/
literal|1000
operator|+
literal|"s"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Running for "
operator|+
name|running
operator|+
literal|"s"
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
block|}
end_function

begin_function
specifier|public
specifier|synchronized
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|monitorThread
operator|!=
literal|null
condition|)
block|{
name|monitorThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
end_function

begin_comment
comment|/**    * This class encapsulates an object as well as a weak reference to a proxy    * that passes through calls to that object. In art form:    *<pre>    *     Proxy<------------------    *       |                       \    *       v                        \    * PassthroughInvocationHandler   |  weak reference    *       |                       /    * MonitoredTaskImpl            /     *       |                     /    * StatAndWeakRefProxy  ------/    *</pre>    * Since we only return the Proxy to the creator of the MonitorableStatus,    * this means that they can leak that object, and we'll detect it    * since our weak reference will go null. But, we still have the actual    * object, so we can log it and display it as a leaked (incomplete) action.    */
end_comment

begin_class
specifier|private
specifier|static
class|class
name|TaskAndWeakRefPair
block|{
specifier|private
name|MonitoredTask
name|impl
decl_stmt|;
specifier|private
name|WeakReference
argument_list|<
name|MonitoredTask
argument_list|>
name|weakProxy
decl_stmt|;
specifier|public
name|TaskAndWeakRefPair
parameter_list|(
name|MonitoredTask
name|stat
parameter_list|,
name|MonitoredTask
name|proxy
parameter_list|)
block|{
name|this
operator|.
name|impl
operator|=
name|stat
expr_stmt|;
name|this
operator|.
name|weakProxy
operator|=
operator|new
name|WeakReference
argument_list|<>
argument_list|(
name|proxy
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MonitoredTask
name|get
parameter_list|()
block|{
return|return
name|impl
return|;
block|}
specifier|public
name|boolean
name|isDead
parameter_list|()
block|{
return|return
name|weakProxy
operator|.
name|get
argument_list|()
operator|==
literal|null
return|;
block|}
block|}
end_class

begin_comment
comment|/**    * An InvocationHandler that simply passes through calls to the original     * object.    */
end_comment

begin_class
specifier|private
specifier|static
class|class
name|PassthroughInvocationHandler
parameter_list|<
name|T
parameter_list|>
implements|implements
name|InvocationHandler
block|{
specifier|private
name|T
name|delegatee
decl_stmt|;
specifier|public
name|PassthroughInvocationHandler
parameter_list|(
name|T
name|delegatee
parameter_list|)
block|{
name|this
operator|.
name|delegatee
operator|=
name|delegatee
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|method
operator|.
name|invoke
argument_list|(
name|delegatee
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
end_class

begin_class
specifier|private
class|class
name|MonitorRunnable
implements|implements
name|Runnable
block|{
specifier|private
name|boolean
name|running
init|=
literal|true
decl_stmt|;
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
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|monitorInterval
argument_list|)
expr_stmt|;
if|if
condition|(
name|tasks
operator|.
name|isFull
argument_list|()
condition|)
block|{
name|purgeExpiredTasks
argument_list|()
expr_stmt|;
block|}
name|warnStuckTasks
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|running
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

begin_interface
specifier|private
interface|interface
name|TaskFilter
block|{
enum|enum
name|TaskType
block|{
name|GENERAL
argument_list|(
literal|"general"
argument_list|)
block|,
name|HANDLER
argument_list|(
literal|"handler"
argument_list|)
block|,
name|RPC
argument_list|(
literal|"rpc"
argument_list|)
block|,
name|OPERATION
argument_list|(
literal|"operation"
argument_list|)
block|,
name|ALL
argument_list|(
literal|"all"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
specifier|private
name|TaskType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
specifier|static
name|TaskType
name|getTaskType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
operator|||
name|type
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ALL
return|;
block|}
name|type
operator|=
name|type
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
for|for
control|(
name|TaskType
name|taskType
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|taskType
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|taskType
return|;
block|}
block|}
return|return
name|ALL
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
name|type
return|;
block|}
block|}
comment|/**      * Filter out unwanted task.      * @param task monitored task      * @return false if a task is accepted, true if it is filtered      */
name|boolean
name|filter
parameter_list|(
name|MonitoredTask
name|task
parameter_list|)
function_decl|;
block|}
end_interface

unit|}
end_unit

