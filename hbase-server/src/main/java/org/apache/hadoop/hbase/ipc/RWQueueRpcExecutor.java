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
name|Random
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
name|commons
operator|.
name|lang
operator|.
name|ArrayUtils
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|Action
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MultiRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|RegionAction
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
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
operator|.
name|RequestHeader
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
name|ReflectionUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_comment
comment|/**  * RPC Executor that uses different queues for reads and writes.  * Each handler has its own queue and there is no stealing.  */
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
name|RWQueueRpcExecutor
extends|extends
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
name|RWQueueRpcExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
name|queues
decl_stmt|;
specifier|private
specifier|final
name|Random
name|balancer
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|writeHandlersCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|readHandlersCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|numWriteQueues
decl_stmt|;
specifier|private
specifier|final
name|int
name|numReadQueues
decl_stmt|;
specifier|public
name|RWQueueRpcExecutor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|handlerCount
parameter_list|,
specifier|final
name|int
name|numQueues
parameter_list|,
specifier|final
name|float
name|readShare
parameter_list|,
specifier|final
name|int
name|maxQueueLength
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|handlerCount
argument_list|,
name|numQueues
argument_list|,
name|readShare
argument_list|,
name|maxQueueLength
argument_list|,
name|LinkedBlockingQueue
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RWQueueRpcExecutor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|handlerCount
parameter_list|,
specifier|final
name|int
name|numQueues
parameter_list|,
specifier|final
name|float
name|readShare
parameter_list|,
specifier|final
name|int
name|maxQueueLength
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|BlockingQueue
argument_list|>
name|readQueueClass
parameter_list|,
name|Object
modifier|...
name|readQueueInitArgs
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|calcNumWriters
argument_list|(
name|handlerCount
argument_list|,
name|readShare
argument_list|)
argument_list|,
name|calcNumReaders
argument_list|(
name|handlerCount
argument_list|,
name|readShare
argument_list|)
argument_list|,
name|calcNumWriters
argument_list|(
name|numQueues
argument_list|,
name|readShare
argument_list|)
argument_list|,
name|calcNumReaders
argument_list|(
name|numQueues
argument_list|,
name|readShare
argument_list|)
argument_list|,
name|LinkedBlockingQueue
operator|.
name|class
argument_list|,
operator|new
name|Object
index|[]
block|{
name|maxQueueLength
block|}
argument_list|,
name|readQueueClass
argument_list|,
name|ArrayUtils
operator|.
name|addAll
argument_list|(
operator|new
name|Object
index|[]
block|{
name|maxQueueLength
block|}
argument_list|,
name|readQueueInitArgs
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RWQueueRpcExecutor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|writeHandlers
parameter_list|,
specifier|final
name|int
name|readHandlers
parameter_list|,
specifier|final
name|int
name|numWriteQueues
parameter_list|,
specifier|final
name|int
name|numReadQueues
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|BlockingQueue
argument_list|>
name|writeQueueClass
parameter_list|,
name|Object
index|[]
name|writeQueueInitArgs
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|BlockingQueue
argument_list|>
name|readQueueClass
parameter_list|,
name|Object
index|[]
name|readQueueInitArgs
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|writeHandlers
operator|+
name|readHandlers
argument_list|,
name|numWriteQueues
operator|+
name|numReadQueues
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeHandlersCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|writeHandlers
argument_list|,
name|numWriteQueues
argument_list|)
expr_stmt|;
name|this
operator|.
name|readHandlersCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|readHandlers
argument_list|,
name|numReadQueues
argument_list|)
expr_stmt|;
name|this
operator|.
name|numWriteQueues
operator|=
name|numWriteQueues
expr_stmt|;
name|this
operator|.
name|numReadQueues
operator|=
name|numReadQueues
expr_stmt|;
name|queues
operator|=
operator|new
name|ArrayList
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
argument_list|(
name|writeHandlersCount
operator|+
name|readHandlersCount
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|name
operator|+
literal|" writeQueues="
operator|+
name|numWriteQueues
operator|+
literal|" writeHandlers="
operator|+
name|writeHandlersCount
operator|+
literal|" readQueues="
operator|+
name|numReadQueues
operator|+
literal|" readHandlers="
operator|+
name|readHandlersCount
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numWriteQueues
condition|;
operator|++
name|i
control|)
block|{
name|queues
operator|.
name|add
argument_list|(
operator|(
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|writeQueueClass
argument_list|,
name|writeQueueInitArgs
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numReadQueues
condition|;
operator|++
name|i
control|)
block|{
name|queues
operator|.
name|add
argument_list|(
operator|(
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|readQueueClass
argument_list|,
name|readQueueInitArgs
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|startHandlers
parameter_list|(
specifier|final
name|int
name|port
parameter_list|)
block|{
name|startHandlers
argument_list|(
literal|".write"
argument_list|,
name|writeHandlersCount
argument_list|,
name|queues
argument_list|,
literal|0
argument_list|,
name|numWriteQueues
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|startHandlers
argument_list|(
literal|".read"
argument_list|,
name|readHandlersCount
argument_list|,
name|queues
argument_list|,
name|numWriteQueues
argument_list|,
name|numReadQueues
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatch
parameter_list|(
specifier|final
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
name|queueIndex
decl_stmt|;
if|if
condition|(
name|isWriteRequest
argument_list|(
name|call
operator|.
name|getHeader
argument_list|()
argument_list|,
name|call
operator|.
name|param
argument_list|)
condition|)
block|{
name|queueIndex
operator|=
name|balancer
operator|.
name|nextInt
argument_list|(
name|numWriteQueues
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|queueIndex
operator|=
name|numWriteQueues
operator|+
name|balancer
operator|.
name|nextInt
argument_list|(
name|numReadQueues
argument_list|)
expr_stmt|;
block|}
name|queues
operator|.
name|get
argument_list|(
name|queueIndex
argument_list|)
operator|.
name|put
argument_list|(
name|callTask
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isWriteRequest
parameter_list|(
specifier|final
name|RequestHeader
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|)
block|{
comment|// TODO: Is there a better way to do this?
name|String
name|methodName
init|=
name|header
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
if|if
condition|(
name|methodName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"multi"
argument_list|)
operator|&&
name|param
operator|instanceof
name|MultiRequest
condition|)
block|{
name|MultiRequest
name|multi
init|=
operator|(
name|MultiRequest
operator|)
name|param
decl_stmt|;
for|for
control|(
name|RegionAction
name|regionAction
range|:
name|multi
operator|.
name|getRegionActionList
argument_list|()
control|)
block|{
for|for
control|(
name|Action
name|action
range|:
name|regionAction
operator|.
name|getActionList
argument_list|()
control|)
block|{
if|if
condition|(
name|action
operator|.
name|hasMutation
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQueueLength
parameter_list|()
block|{
name|int
name|length
init|=
literal|0
decl_stmt|;
for|for
control|(
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|queue
range|:
name|queues
control|)
block|{
name|length
operator|+=
name|queue
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
name|getQueues
parameter_list|()
block|{
return|return
name|queues
return|;
block|}
comment|/*    * Calculate the number of writers based on the "total count" and the read share.    * You'll get at least one writer.    */
specifier|private
specifier|static
name|int
name|calcNumWriters
parameter_list|(
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|float
name|readShare
parameter_list|)
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|count
operator|-
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
name|count
operator|*
name|readShare
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/*    * Calculate the number of readers based on the "total count" and the read share.    * You'll get at least one reader.    */
specifier|private
specifier|static
name|int
name|calcNumReaders
parameter_list|(
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|float
name|readShare
parameter_list|)
block|{
return|return
name|count
operator|-
name|calcNumWriters
argument_list|(
name|count
argument_list|,
name|readShare
argument_list|)
return|;
block|}
block|}
end_class

end_unit

