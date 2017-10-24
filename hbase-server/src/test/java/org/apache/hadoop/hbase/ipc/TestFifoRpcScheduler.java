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
name|CategoryBasedTimeout
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
name|hadoop
operator|.
name|hbase
operator|.
name|monitoring
operator|.
name|MonitoredRPCHandlerImpl
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
name|testclassification
operator|.
name|LargeTests
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
name|testclassification
operator|.
name|RPCTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|*
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RPCTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestFifoRpcScheduler
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
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
name|TestFifoRpcScheduler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AtomicInteger
name|callExecutionCount
decl_stmt|;
specifier|private
specifier|final
name|RpcScheduler
operator|.
name|Context
name|CONTEXT
init|=
operator|new
name|RpcScheduler
operator|.
name|Context
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InetSocketAddress
name|getListenerAddress
parameter_list|()
block|{
return|return
name|InetSocketAddress
operator|.
name|createUnresolved
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|1000
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|callExecutionCount
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ThreadPoolExecutor
name|disableHandlers
parameter_list|(
name|RpcScheduler
name|scheduler
parameter_list|)
block|{
name|ThreadPoolExecutor
name|rpcExecutor
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Field
name|ExecutorField
init|=
name|scheduler
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"executor"
argument_list|)
decl_stmt|;
name|ExecutorField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scheduler
operator|.
name|start
argument_list|()
expr_stmt|;
name|rpcExecutor
operator|=
operator|(
name|ThreadPoolExecutor
operator|)
name|ExecutorField
operator|.
name|get
argument_list|(
name|scheduler
argument_list|)
expr_stmt|;
name|rpcExecutor
operator|.
name|setMaximumPoolSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|rpcExecutor
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rpcExecutor
operator|.
name|setCorePoolSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rpcExecutor
operator|.
name|setKeepAliveTime
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|)
expr_stmt|;
comment|// Wait for 2 seconds, so that idle threads will die
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"No such field exception:"
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Illegal access exception:"
operator|+
name|e
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
name|error
argument_list|(
literal|"Interrupted exception:"
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|rpcExecutor
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCallQueueInfo
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ThreadPoolExecutor
name|rpcExecutor
decl_stmt|;
name|RpcScheduler
name|scheduler
init|=
operator|new
name|FifoRpcScheduler
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|scheduler
operator|.
name|init
argument_list|(
name|CONTEXT
argument_list|)
expr_stmt|;
comment|// Set number of handlers to a minimum value
name|disableHandlers
argument_list|(
name|scheduler
argument_list|)
expr_stmt|;
name|int
name|totalCallMethods
init|=
literal|30
decl_stmt|;
name|int
name|unableToDispatch
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|totalCallMethods
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|CallRunner
name|task
init|=
name|createMockTask
argument_list|()
decl_stmt|;
name|task
operator|.
name|setStatus
argument_list|(
operator|new
name|MonitoredRPCHandlerImpl
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|scheduler
operator|.
name|dispatch
argument_list|(
name|task
argument_list|)
condition|)
block|{
name|unableToDispatch
operator|++
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|CallQueueInfo
name|callQueueInfo
init|=
name|scheduler
operator|.
name|getCallQueueInfo
argument_list|()
decl_stmt|;
name|int
name|executionCount
init|=
name|callExecutionCount
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|callQueueSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|callQueueName
range|:
name|callQueueInfo
operator|.
name|getCallQueueNames
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|calledMethod
range|:
name|callQueueInfo
operator|.
name|getCalledMethodNames
argument_list|(
name|callQueueName
argument_list|)
control|)
block|{
name|callQueueSize
operator|+=
name|callQueueInfo
operator|.
name|getCallMethodCount
argument_list|(
name|callQueueName
argument_list|,
name|calledMethod
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|totalCallMethods
operator|-
name|unableToDispatch
argument_list|,
name|callQueueSize
operator|+
name|executionCount
argument_list|)
expr_stmt|;
name|scheduler
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|private
name|CallRunner
name|createMockTask
parameter_list|()
block|{
name|ServerCall
name|call
init|=
name|mock
argument_list|(
name|ServerCall
operator|.
name|class
argument_list|)
decl_stmt|;
name|CallRunner
name|task
init|=
name|mock
argument_list|(
name|CallRunner
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|task
operator|.
name|getRpcCall
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|call
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|callExecutionCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|task
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
name|task
return|;
block|}
block|}
end_class

end_unit

