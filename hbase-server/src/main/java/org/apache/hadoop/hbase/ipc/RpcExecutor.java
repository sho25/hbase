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
name|ArrayList
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
name|conf
operator|.
name|Configuration
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
specifier|abstract
class|class
name|RpcExecutor
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
name|RpcExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|activeHandlerCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Thread
argument_list|>
name|handlers
decl_stmt|;
specifier|private
specifier|final
name|int
name|handlerCount
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
name|boolean
name|running
decl_stmt|;
specifier|public
name|RpcExecutor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|handlerCount
parameter_list|)
block|{
name|this
operator|.
name|handlers
operator|=
operator|new
name|ArrayList
argument_list|<
name|Thread
argument_list|>
argument_list|(
name|handlerCount
argument_list|)
expr_stmt|;
name|this
operator|.
name|handlerCount
operator|=
name|handlerCount
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|(
specifier|final
name|int
name|port
parameter_list|)
block|{
name|running
operator|=
literal|true
expr_stmt|;
name|startHandlers
argument_list|(
name|port
argument_list|)
expr_stmt|;
block|}
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
specifier|public
name|int
name|getActiveHandlerCount
parameter_list|()
block|{
return|return
name|activeHandlerCount
operator|.
name|get
argument_list|()
return|;
block|}
comment|/** Returns the length of the pending queue */
specifier|public
specifier|abstract
name|int
name|getQueueLength
parameter_list|()
function_decl|;
comment|/** Add the request to the executor queue */
specifier|public
specifier|abstract
name|void
name|dispatch
parameter_list|(
specifier|final
name|CallRunner
name|callTask
parameter_list|)
throws|throws
name|InterruptedException
function_decl|;
comment|/** Returns the list of request queues */
specifier|protected
specifier|abstract
name|List
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
name|getQueues
parameter_list|()
function_decl|;
specifier|protected
name|void
name|startHandlers
parameter_list|(
specifier|final
name|int
name|port
parameter_list|)
block|{
name|List
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
name|callQueues
init|=
name|getQueues
argument_list|()
decl_stmt|;
name|startHandlers
argument_list|(
literal|null
argument_list|,
name|handlerCount
argument_list|,
name|callQueues
argument_list|,
literal|0
argument_list|,
name|callQueues
operator|.
name|size
argument_list|()
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|startHandlers
parameter_list|(
specifier|final
name|String
name|nameSuffix
parameter_list|,
specifier|final
name|int
name|numHandlers
parameter_list|,
specifier|final
name|List
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
name|callQueues
parameter_list|,
specifier|final
name|int
name|qindex
parameter_list|,
specifier|final
name|int
name|qsize
parameter_list|,
specifier|final
name|int
name|port
parameter_list|)
block|{
specifier|final
name|String
name|threadPrefix
init|=
name|name
operator|+
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|nameSuffix
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numHandlers
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|index
init|=
name|qindex
operator|+
operator|(
name|i
operator|%
name|qsize
operator|)
decl_stmt|;
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
name|callQueues
operator|.
name|get
argument_list|(
name|index
argument_list|)
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
name|threadPrefix
operator|+
literal|"RpcServer.handler="
operator|+
name|handlers
operator|.
name|size
argument_list|()
operator|+
literal|",queue="
operator|+
name|index
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
name|LOG
operator|.
name|debug
argument_list|(
name|threadPrefix
operator|+
literal|" Start Handler index="
operator|+
name|handlers
operator|.
name|size
argument_list|()
operator|+
literal|" queue="
operator|+
name|index
argument_list|)
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
specifier|protected
name|void
name|consumerLoop
parameter_list|(
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|myQueue
parameter_list|)
block|{
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
try|try
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
try|try
block|{
name|activeHandlerCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|task
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|activeHandlerCount
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|interrupted
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
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
block|}
block|}
end_class

end_unit

