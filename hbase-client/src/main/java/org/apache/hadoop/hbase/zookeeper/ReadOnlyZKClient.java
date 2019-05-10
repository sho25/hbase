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
name|zookeeper
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|DEFAULT_ZK_SESSION_TIMEOUT
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|ZK_SESSION_TIMEOUT
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|concurrent
operator|.
name|CompletableFuture
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
name|Delayed
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
name|hadoop
operator|.
name|hbase
operator|.
name|DoNotRetryIOException
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
name|FutureUtils
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
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|Code
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
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

begin_comment
comment|/**  * A very simple read only zookeeper implementation without watcher support.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ReadOnlyZKClient
implements|implements
name|Closeable
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
name|ReadOnlyZKClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RECOVERY_RETRY
init|=
literal|"zookeeper.recovery.retry"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_RECOVERY_RETRY
init|=
literal|30
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RECOVERY_RETRY_INTERVAL_MILLIS
init|=
literal|"zookeeper.recovery.retry.intervalmill"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_RECOVERY_RETRY_INTERVAL_MILLIS
init|=
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KEEPALIVE_MILLIS
init|=
literal|"zookeeper.keep-alive.time"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_KEEPALIVE_MILLIS
init|=
literal|60000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|Code
argument_list|>
name|FAIL_FAST_CODES
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Code
operator|.
name|NOAUTH
argument_list|,
name|Code
operator|.
name|AUTHFAILED
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|connectString
decl_stmt|;
specifier|private
specifier|final
name|int
name|sessionTimeoutMs
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxRetries
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryIntervalMs
decl_stmt|;
specifier|private
specifier|final
name|int
name|keepAliveTimeMs
decl_stmt|;
specifier|private
specifier|static
specifier|abstract
class|class
name|Task
implements|implements
name|Delayed
block|{
specifier|protected
name|long
name|time
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
specifier|public
name|boolean
name|needZk
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|exec
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
block|{     }
specifier|public
name|void
name|connectFailed
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{     }
specifier|public
name|void
name|closed
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Delayed
name|o
parameter_list|)
block|{
name|Task
name|that
init|=
operator|(
name|Task
operator|)
name|o
decl_stmt|;
name|int
name|c
init|=
name|Long
operator|.
name|compare
argument_list|(
name|time
argument_list|,
name|that
operator|.
name|time
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
argument_list|,
name|System
operator|.
name|identityHashCode
argument_list|(
name|that
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDelay
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|time
operator|-
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|Task
name|CLOSE
init|=
operator|new
name|Task
argument_list|()
block|{   }
decl_stmt|;
specifier|private
specifier|final
name|DelayQueue
argument_list|<
name|Task
argument_list|>
name|tasks
init|=
operator|new
name|DelayQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|VisibleForTesting
name|ZooKeeper
name|zookeeper
decl_stmt|;
specifier|private
name|int
name|pendingRequests
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|getId
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"0x%08x"
argument_list|,
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|ReadOnlyZKClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// We might use a different ZK for client access
name|String
name|clientZkQuorumServers
init|=
name|ZKConfig
operator|.
name|getClientZKQuorumServersString
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|clientZkQuorumServers
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|connectString
operator|=
name|clientZkQuorumServers
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|connectString
operator|=
name|ZKConfig
operator|.
name|getZKQuorumServersString
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|sessionTimeoutMs
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|ZK_SESSION_TIMEOUT
argument_list|,
name|DEFAULT_ZK_SESSION_TIMEOUT
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxRetries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|RECOVERY_RETRY
argument_list|,
name|DEFAULT_RECOVERY_RETRY
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryIntervalMs
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|RECOVERY_RETRY_INTERVAL_MILLIS
argument_list|,
name|DEFAULT_RECOVERY_RETRY_INTERVAL_MILLIS
argument_list|)
expr_stmt|;
name|this
operator|.
name|keepAliveTimeMs
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|KEEPALIVE_MILLIS
argument_list|,
name|DEFAULT_KEEPALIVE_MILLIS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connect {} to {} with session timeout={}ms, retries {}, "
operator|+
literal|"retry interval {}ms, keepAlive={}ms"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|,
name|sessionTimeoutMs
argument_list|,
name|maxRetries
argument_list|,
name|retryIntervalMs
argument_list|,
name|keepAliveTimeMs
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|setDaemonThreadRunning
argument_list|(
operator|new
name|Thread
argument_list|(
name|this
operator|::
name|run
argument_list|)
argument_list|,
literal|"ReadOnlyZKClient-"
operator|+
name|connectString
operator|+
literal|"@"
operator|+
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|abstract
class|class
name|ZKTask
parameter_list|<
name|T
parameter_list|>
extends|extends
name|Task
block|{
specifier|protected
specifier|final
name|String
name|path
decl_stmt|;
specifier|private
specifier|final
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
decl_stmt|;
specifier|private
specifier|final
name|String
name|operationType
decl_stmt|;
specifier|private
name|int
name|retries
decl_stmt|;
specifier|protected
name|ZKTask
parameter_list|(
name|String
name|path
parameter_list|,
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
parameter_list|,
name|String
name|operationType
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|future
operator|=
name|future
expr_stmt|;
name|this
operator|.
name|operationType
operator|=
name|operationType
expr_stmt|;
block|}
specifier|protected
specifier|final
name|void
name|onComplete
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|,
name|int
name|rc
parameter_list|,
name|T
name|ret
parameter_list|,
name|boolean
name|errorIfNoNode
parameter_list|)
block|{
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|Task
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|exec
parameter_list|(
name|ZooKeeper
name|alwaysNull
parameter_list|)
block|{
name|pendingRequests
operator|--
expr_stmt|;
name|Code
name|code
init|=
name|Code
operator|.
name|get
argument_list|(
name|rc
argument_list|)
decl_stmt|;
if|if
condition|(
name|code
operator|==
name|Code
operator|.
name|OK
condition|)
block|{
name|future
operator|.
name|complete
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Code
operator|.
name|NONODE
condition|)
block|{
if|if
condition|(
name|errorIfNoNode
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|KeeperException
operator|.
name|create
argument_list|(
name|code
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|future
operator|.
name|complete
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|FAIL_FAST_CODES
operator|.
name|contains
argument_list|(
name|code
argument_list|)
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|KeeperException
operator|.
name|create
argument_list|(
name|code
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|code
operator|==
name|Code
operator|.
name|SESSIONEXPIRED
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} to {} session expired, close and reconnect"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|)
expr_stmt|;
try|try
block|{
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{               }
block|}
if|if
condition|(
name|ZKTask
operator|.
name|this
operator|.
name|delay
argument_list|(
name|retryIntervalMs
argument_list|,
name|maxRetries
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} to {} failed for {} of {}, code = {}, retries = {}"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|,
name|operationType
argument_list|,
name|path
argument_list|,
name|code
argument_list|,
name|ZKTask
operator|.
name|this
operator|.
name|retries
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|ZKTask
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} to {} failed for {} of {}, code = {}, retries = {}, give up"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|,
name|operationType
argument_list|,
name|path
argument_list|,
name|code
argument_list|,
name|ZKTask
operator|.
name|this
operator|.
name|retries
argument_list|)
expr_stmt|;
name|future
operator|.
name|completeExceptionally
argument_list|(
name|KeeperException
operator|.
name|create
argument_list|(
name|code
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closed
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// It may happen that a request is succeeded and the onComplete has been called and pushed
comment|// us into the task queue, but before we get called a close is called and here we will
comment|// fail the request, although it is succeeded actually.
comment|// This is not a perfect solution but anyway, it is better than hang the requests for
comment|// ever, and also acceptable as if you close the zk client before actually getting the
comment|// response then a failure is always possible.
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needZk
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|doExec
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
specifier|final
name|void
name|exec
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
block|{
name|pendingRequests
operator|++
expr_stmt|;
name|doExec
argument_list|(
name|zk
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|delay
parameter_list|(
name|long
name|intervalMs
parameter_list|,
name|int
name|maxRetries
parameter_list|)
block|{
if|if
condition|(
name|retries
operator|>=
name|maxRetries
condition|)
block|{
return|return
literal|false
return|;
block|}
name|retries
operator|++
expr_stmt|;
name|time
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|+
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|intervalMs
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connectFailed
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|delay
argument_list|(
name|retryIntervalMs
argument_list|,
name|maxRetries
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} to {} failed to connect to zk fo {} of {}, retries = {}"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|,
name|operationType
argument_list|,
name|path
argument_list|,
name|retries
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} to {} failed to connect to zk fo {} of {}, retries = {}, give up"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|,
name|operationType
argument_list|,
name|path
argument_list|,
name|retries
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closed
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|byte
index|[]
argument_list|>
name|get
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
name|FutureUtils
operator|.
name|failedFuture
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
argument_list|)
return|;
block|}
name|CompletableFuture
argument_list|<
name|byte
index|[]
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ZKTask
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|path
argument_list|,
name|future
argument_list|,
literal|"get"
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doExec
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
block|{
name|zk
operator|.
name|getData
argument_list|(
name|path
argument_list|,
literal|false
argument_list|,
parameter_list|(
name|rc
parameter_list|,
name|path
parameter_list|,
name|ctx
parameter_list|,
name|data
parameter_list|,
name|stat
parameter_list|)
lambda|->
name|onComplete
argument_list|(
name|zk
argument_list|,
name|rc
argument_list|,
name|data
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|Stat
argument_list|>
name|exists
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
name|FutureUtils
operator|.
name|failedFuture
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
argument_list|)
return|;
block|}
name|CompletableFuture
argument_list|<
name|Stat
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ZKTask
argument_list|<
name|Stat
argument_list|>
argument_list|(
name|path
argument_list|,
name|future
argument_list|,
literal|"exists"
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doExec
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
block|{
name|zk
operator|.
name|exists
argument_list|(
name|path
argument_list|,
literal|false
argument_list|,
parameter_list|(
name|rc
parameter_list|,
name|path
parameter_list|,
name|ctx
parameter_list|,
name|stat
parameter_list|)
lambda|->
name|onComplete
argument_list|(
name|zk
argument_list|,
name|rc
argument_list|,
name|stat
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
specifier|public
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|list
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
name|FutureUtils
operator|.
name|failedFuture
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
argument_list|)
return|;
block|}
name|CompletableFuture
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ZKTask
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|(
name|path
argument_list|,
name|future
argument_list|,
literal|"list"
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doExec
parameter_list|(
name|ZooKeeper
name|zk
parameter_list|)
block|{
name|zk
operator|.
name|getChildren
argument_list|(
name|path
argument_list|,
literal|false
argument_list|,
parameter_list|(
name|rc
parameter_list|,
name|path
parameter_list|,
name|ctx
parameter_list|,
name|children
parameter_list|)
lambda|->
name|onComplete
argument_list|(
name|zk
argument_list|,
name|rc
argument_list|,
name|children
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
specifier|private
name|void
name|closeZk
parameter_list|()
block|{
if|if
condition|(
name|zookeeper
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|zookeeper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{       }
name|zookeeper
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|ZooKeeper
name|getZk
parameter_list|()
throws|throws
name|IOException
block|{
comment|// may be closed when session expired
if|if
condition|(
name|zookeeper
operator|==
literal|null
operator|||
operator|!
name|zookeeper
operator|.
name|getState
argument_list|()
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|zookeeper
operator|=
operator|new
name|ZooKeeper
argument_list|(
name|connectString
argument_list|,
name|sessionTimeoutMs
argument_list|,
name|e
lambda|->
block|{}
argument_list|)
expr_stmt|;
block|}
return|return
name|zookeeper
return|;
block|}
specifier|private
name|void
name|run
parameter_list|()
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
name|Task
name|task
decl_stmt|;
try|try
block|{
name|task
operator|=
name|tasks
operator|.
name|poll
argument_list|(
name|keepAliveTimeMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
continue|continue;
block|}
if|if
condition|(
name|task
operator|==
name|CLOSE
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|task
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|pendingRequests
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"{} to {} inactive for {}ms; closing (Will reconnect when new requests)"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|,
name|keepAliveTimeMs
argument_list|)
expr_stmt|;
name|closeZk
argument_list|()
expr_stmt|;
block|}
continue|continue;
block|}
if|if
condition|(
operator|!
name|task
operator|.
name|needZk
argument_list|()
condition|)
block|{
name|task
operator|.
name|exec
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ZooKeeper
name|zk
decl_stmt|;
try|try
block|{
name|zk
operator|=
name|getZk
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|task
operator|.
name|connectFailed
argument_list|(
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|task
operator|.
name|exec
argument_list|(
name|zk
argument_list|)
expr_stmt|;
block|}
block|}
name|closeZk
argument_list|()
expr_stmt|;
name|DoNotRetryIOException
name|error
init|=
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|stream
argument_list|(
name|tasks
operator|.
name|toArray
argument_list|(
operator|new
name|Task
index|[
literal|0
index|]
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|t
lambda|->
name|t
operator|.
name|closed
argument_list|(
name|error
argument_list|)
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Close zookeeper connection {} to {}"
argument_list|,
name|getId
argument_list|()
argument_list|,
name|connectString
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|CLOSE
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|String
name|getConnectString
parameter_list|()
block|{
return|return
name|connectString
return|;
block|}
block|}
end_class

end_unit

