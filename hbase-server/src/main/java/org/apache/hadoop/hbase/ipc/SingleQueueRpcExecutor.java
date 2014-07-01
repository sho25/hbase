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
name|LinkedBlockingQueue
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

begin_comment
comment|/**  * RPC Executor that uses a single queue for all the requests.  */
end_comment

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
class|class
name|SingleQueueRpcExecutor
extends|extends
name|RpcExecutor
block|{
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|queue
decl_stmt|;
specifier|public
name|SingleQueueRpcExecutor
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
name|maxQueueLength
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|handlerCount
argument_list|,
name|LinkedBlockingQueue
operator|.
name|class
argument_list|,
name|maxQueueLength
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SingleQueueRpcExecutor
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
name|Class
argument_list|<
name|?
extends|extends
name|BlockingQueue
argument_list|>
name|queueClass
parameter_list|,
name|Object
modifier|...
name|initargs
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|handlerCount
argument_list|)
expr_stmt|;
name|queue
operator|=
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
name|queueClass
argument_list|,
name|initargs
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
name|queue
operator|.
name|put
argument_list|(
name|callTask
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQueueLength
parameter_list|()
block|{
return|return
name|queue
operator|.
name|size
argument_list|()
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
name|List
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|queue
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
block|}
end_class

end_unit

